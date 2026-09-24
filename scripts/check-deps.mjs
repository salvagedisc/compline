#!/usr/bin/env node
// Vérifie que chaque dépendance requise déclarée DANS les jars du pack est fournie par le pack
// (Modrinth ne déclare pas toujours toutes les dépendances). Usage : npm run check-deps [-- <cacheDir>]
import { mkdir, readFile, writeFile } from 'node:fs/promises';
import { dirname, join } from 'node:path';
import { fileURLToPath } from 'node:url';
import { readPackMods } from './sync/pwtoml.mjs';
import { listEntries, readEntry } from './deps/zip.mjs';
import { parseModsToml, findMissing } from './deps/modstoml.mjs';

const root = join(dirname(fileURLToPath(import.meta.url)), '..');
const packDir = join(root, 'pack');
const cacheDir = process.argv[2] ?? join(root, '.cache', 'jars');
await mkdir(cacheDir, { recursive: true });

async function loadJar(mod) {
  const path = join(cacheDir, mod.filename);
  try { return await readFile(path); } catch { /* not cached yet */ }
  const res = await fetch(mod.url, { headers: { 'User-Agent': 'salvagedisc/fallow check-deps' } });
  if (!res.ok) throw new Error(`download ${mod.filename}: HTTP ${res.status}`);
  const buf = Buffer.from(await res.arrayBuffer());
  await writeFile(path, buf);
  return buf;
}

const provided = new Set();
const deps = [];
const addToml = (jarName, buf, nestedOnly) => {
  const toml = readEntry(buf, 'META-INF/neoforge.mods.toml');
  if (!toml) return;
  const parsed = parseModsToml(toml.toString('utf8'));
  parsed.mods.forEach((id) => provided.add(id));
  if (!nestedOnly) deps.push(...parsed.deps.map((d) => ({ ...d, jar: jarName })));
};

for (const mod of await readPackMods(packDir)) {
  const buf = await loadJar(mod);
  addToml(mod.filename, buf, false);
  // Jar-in-jar: embedded mods count as provided.
  for (const nested of listEntries(buf).filter((n) => /^META-INF\/jarjar\/.+\.jar$/.test(n))) {
    addToml(nested, readEntry(buf, nested), true);
  }
}

const missing = findMissing(provided, deps);
for (const m of missing) console.log(`MANQUANT ${m.dep} ${m.range ?? ''} (requis par ${m.jar})`);
console.log(`${provided.size} mods fournis, ${deps.filter((d) => d.type === 'required').length} dépendances requises vérifiées, ${missing.length} manquante(s)`);
process.exit(missing.length ? 1 : 0);
