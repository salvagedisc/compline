// Outils purs pour comparer les fichiers de langue en_us et fr_fr.

const LANG_PATH = /^assets\/([^/]+)\/lang\/(en_us|fr_fr)\.json$/;
const PLACEHOLDER = /%(?:\d+\$)?[sdf]|%%|\{\d+\}|§./g;

// Some mods ship lang files with // comments; parse them leniently.
export function parseLang(text) {
  try {
    return JSON.parse(text.replace(/^\s*\/\/.*$/gm, ''));
  } catch {
    return null;
  }
}

export function langEntry(name) {
  const m = LANG_PATH.exec(name);
  return m ? { namespace: m[1], locale: m[2] } : null;
}

export function placeholders(value) {
  return (value.match(PLACEHOLDER) ?? []).sort();
}

// Compares our translations of one namespace with the mod's en_us and fr_fr.
export function compareNamespace(en, modFr, ours) {
  const unknown = [];
  const badPlaceholders = [];
  for (const [key, value] of Object.entries(ours)) {
    if (!(key in en)) { unknown.push(key); continue; }
    if (typeof value !== 'string' || typeof en[key] !== 'string') continue;
    if (placeholders(value).join('\0') !== placeholders(en[key]).join('\0')) badPlaceholders.push(key);
  }
  const missing = Object.keys(en).filter((key) => !(key in modFr) && !(key in ours));
  return { unknown, badPlaceholders, missing };
}

// No Man's Land reads the Friendly Moon's dialogues outside the usual language files: each
// pool has its own assets/<ns>/lang/nomansland/dialogue_pools/<pool>/<locale>.json, keyed by
// the dialogue's path in the datapack registry (data/<ns>/nomansland/dialogue_pools/<pool>/
// <path>.json, "/" turned into "."), whose "text" is shown when no translation matches.
export const DIALOGUE_POOLS = ['greeting', 'passive', 'offering', 'negative', 'contextual', 'leaving', 'special'];
const DIALOGUE_LANG_PATH = /^assets\/[^/]+\/lang\/nomansland\/dialogue_pools\/([^/]+)\/(en_us|fr_fr)\.json$/;
const DIALOGUE_DATA_PATH = /^data\/[^/]+\/nomansland\/dialogue_pools\/([^/]+)\/(.+)\.json$/;
const PLAYER_NAME = /&PlayerName/g;

export function dialogueLangEntry(name) {
  const m = DIALOGUE_LANG_PATH.exec(name);
  return m && DIALOGUE_POOLS.includes(m[1]) ? { pool: m[1], locale: m[2] } : null;
}

export function dialogueDataEntry(name) {
  const m = DIALOGUE_DATA_PATH.exec(name);
  return m && DIALOGUE_POOLS.includes(m[1]) ? { pool: m[1], key: m[2].replaceAll('/', '.') } : null;
}

// Compares the dialogues of one pool. `en` holds every English text the player can see:
// the pool's en_us file and the "text" of each registry entry (which the en_us file overrides).
export function compareDialogues(en, modFr, ours) {
  const fr = { ...modFr, ...ours };
  const missing = [];
  const badPlaceholders = [];
  for (const [key, value] of Object.entries(en)) {
    if (typeof value !== 'string' || !/\p{L}/u.test(value)) continue; // "...", "!" or empty
    if (typeof fr[key] !== 'string') { missing.push(key); continue; }
    if ((fr[key].match(PLAYER_NAME) ?? []).length !== (value.match(PLAYER_NAME) ?? []).length) badPlaceholders.push(key);
  }
  return { missing, badPlaceholders };
}
