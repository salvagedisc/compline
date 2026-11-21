import { readdir, readFile } from 'node:fs/promises';
import { join } from 'node:path';

const quotedIds = (text) => [...text.matchAll(/'([a-z0-9_.-]+:[a-z0-9_/.-]+)'/g)].map((match) => match[1]);

// Item ids written by hand in the KubeJS removal lists: the `*_OBJETS*` lists of the server scripts,
// the EMI removal arrays of the client scripts, and both sides of the FUSIONS map.
export function listedItems(file, source) {
  const ids = [];
  for (const list of source.matchAll(/const \w*_OBJETS\w* = \[([\s\S]*?)\]/g)) ids.push(...quotedIds(list[1]));
  for (const list of source.matchAll(/removeEntriesCompletely\('item'[\s\S]*?\[([\s\S]*?)\]\.forEach/g)) {
    ids.push(...quotedIds(list[1]));
  }
  const fusions = source.match(/const FUSIONS = \{([\s\S]*?)\n\}/);
  if (fusions) ids.push(...quotedIds(fusions[1]));
  return [...new Set(ids)].map((id) => ({ id, file }));
}

export async function readListedItems(kubejsDir) {
  const found = [];
  for (const side of ['server_scripts', 'client_scripts']) {
    const dir = join(kubejsDir, side);
    for (const name of (await readdir(dir)).filter((n) => n.endsWith('.js')).sort()) {
      found.push(...listedItems(`${side}/${name}`, await readFile(join(dir, name), 'utf8')));
    }
  }
  return found;
}

// Ids that are not items in the registries the server wrote. An id whose mod is missing is reported
// too: a list should not name a mod the pack no longer has.
export function unknownItems(listed, registries) {
  const items = new Set(registries.item);
  const blocks = new Set(registries.block);
  const mods = new Set(registries.mods);
  return listed.filter(({ id }) => !items.has(id)).map(({ id, file }) => {
    const mod = id.split(':')[0];
    const why = !mods.has(mod) ? `mod ${mod} absent` : blocks.has(id) ? 'bloc sans objet' : 'objet inconnu';
    return { id, file, why };
  });
}

// Creature ids written by hand in the `*_CREATURES*` lists of the server scripts (creatures cut from the
// world): a typo would leave the creature spawning without any error.
export function listedCreatures(file, source) {
  const ids = [];
  for (const list of source.matchAll(/const \w*_CREATURES\w* = \[([\s\S]*?)\]/g)) ids.push(...quotedIds(list[1]));
  return [...new Set(ids)].map((id) => ({ id, file }));
}

export async function readListedCreatures(kubejsDir) {
  const found = [];
  const dir = join(kubejsDir, 'server_scripts');
  for (const name of (await readdir(dir)).filter((n) => n.endsWith('.js')).sort()) {
    found.push(...listedCreatures(`server_scripts/${name}`, await readFile(join(dir, name), 'utf8')));
  }
  return found;
}

export function unknownCreatures(listed, registries) {
  const entities = new Set(registries.entity_type);
  const mods = new Set(registries.mods);
  return listed.filter(({ id }) => !entities.has(id)).map(({ id, file }) => {
    const mod = id.split(':')[0];
    return { id, file, why: !mods.has(mod) ? `mod ${mod} absent` : 'créature inconnue' };
  });
}
