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
