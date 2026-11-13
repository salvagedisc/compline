#!/usr/bin/env node
// Vérifie que tout texte des mods du pack a une traduction française : celle du mod, ou la nôtre
// dans pack/kubejs/assets/<ns>/lang/fr_fr.json. Contrôle aussi les variables (%s, {0}, §x).
// Usage : npm run check-fr [-- <cacheDir>]
import { mkdir, readFile, readdir, writeFile } from 'node:fs/promises';
import { dirname, join } from 'node:path';
import { fileURLToPath } from 'node:url';
import { readPackMods } from './sync/pwtoml.mjs';
import { listEntries, readEntry } from './deps/zip.mjs';
import { compareNamespace, langEntry, parseLang } from './fr/lang.mjs';

const root = join(dirname(fileURLToPath(import.meta.url)), '..');
const packDir = join(root, 'pack');
const cacheDir = process.argv[2] ?? join(root, '.cache', 'jars');
await mkdir(cacheDir, { recursive: true });

async function loadJar(mod) {
  const path = join(cacheDir, mod.filename);
  try { return await readFile(path); } catch { /* not cached yet */ }
  const res = await fetch(mod.url, { headers: { 'User-Agent': 'salvagedisc/fallow check-fr' } });
  if (!res.ok) throw new Error(`download ${mod.filename}: HTTP ${res.status}`);
  const buf = Buffer.from(await res.arrayBuffer());
  await writeFile(path, buf);
  return buf;
}

const en = {};
const modFr = {};
function scan(buf, depth = 0) {
  for (const name of listEntries(buf)) {
    const lang = langEntry(name);
    if (lang) {
      const target = lang.locale === 'en_us' ? en : modFr;
      Object.assign((target[lang.namespace] ??= {}), parseLang(readEntry(buf, name).toString('utf8')) ?? {});
    } else if (name.endsWith('.jar') && depth < 2) {
      scan(readEntry(buf, name), depth + 1);
    }
  }
}

const mods = await readPackMods(packDir);
for (const mod of mods) scan(await loadJar(mod));
for (const file of await readdir(join(packDir, 'mods'))) {
  if (file.endsWith('.jar')) scan(await readFile(join(packDir, 'mods', file)));
}

const assetsDir = join(packDir, 'kubejs', 'assets');
const ours = {};
let errors = 0;
for (const ns of await readdir(assetsDir)) {
  let text;
  try { text = await readFile(join(assetsDir, ns, 'lang', 'fr_fr.json'), 'utf8'); } catch { continue; }
  const parsed = parseLang(text);
  if (!parsed) { console.log(`JSON INVALIDE ${ns}`); errors++; continue; }
  ours[ns] = parsed;
}

let total = 0;
let missingTotal = 0;
for (const ns of Object.keys(en).sort()) {
  const { badPlaceholders, missing } = compareNamespace(en[ns], modFr[ns] ?? {}, ours[ns] ?? {});
  total += Object.keys(en[ns]).length;
  missingTotal += missing.length;
  for (const key of badPlaceholders) console.log(`VARIABLES ${ns} ${key}`);
  errors += badPlaceholders.length;
  if (missing.length) console.log(`MANQUANT ${ns} : ${missing.length} texte(s), ex. ${missing.slice(0, 3).join(', ')}`);
}

// Keys for other namespaces are allowed: Every Compat, for instance, reads Moonlight's wood_type.* keys.
console.log(`${mods.length} mods, ${total} textes anglais, ${missingTotal} sans traduction, ${errors} erreur(s)`);
process.exit(errors || missingTotal ? 1 : 0);
