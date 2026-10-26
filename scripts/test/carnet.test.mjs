import { test } from 'node:test';
import assert from 'node:assert/strict';
import { readFile, readdir } from 'node:fs/promises';
import { existsSync } from 'node:fs';
import { dirname, join } from 'node:path';
import { fileURLToPath } from 'node:url';

const root = join(dirname(fileURLToPath(import.meta.url)), '..', '..');
const kubejs = join(root, 'pack', 'kubejs');
const book = join(kubejs, 'data', 'fallow', 'modonomicon', 'books', 'carnet');

async function jsonFiles(dir) {
  const out = [];
  for (const e of await readdir(dir, { withFileTypes: true })) {
    const p = join(dir, e.name);
    if (e.isDirectory()) out.push(...await jsonFiles(p));
    else if (e.name.endsWith('.json')) out.push(p);
  }
  return out;
}

function keys(value, out = []) {
  if (typeof value === 'string' && value.startsWith('book.fallow.carnet.')) out.push(value);
  else if (value && typeof value === 'object') Object.values(value).forEach((v) => keys(v, out));
  return out;
}

test('every text key of the Carnet du voyageur has a French translation', async () => {
  const fr = JSON.parse(await readFile(join(kubejs, 'assets', 'fallow', 'lang', 'fr_fr.json'), 'utf8'));
  const missing = [];
  for (const file of await jsonFiles(book)) {
    for (const key of keys(JSON.parse(await readFile(file, 'utf8')))) if (!fr[key]) missing.push(key);
  }
  assert.deepEqual(missing, []);
});

test('entries point to existing categories, parents and advancements', async () => {
  const entries = join(book, 'entries');
  const ids = new Set();
  const all = [];
  for (const file of await jsonFiles(entries)) {
    const rel = file.slice(entries.length + 1, -'.json'.length).split(/[\\/]/).join('/');
    ids.add(`fallow:${rel}`);
    all.push([rel, JSON.parse(await readFile(file, 'utf8'))]);
  }
  for (const [rel, entry] of all) {
    assert.equal(entry.id, `fallow:${rel}`, rel);
    const cat = entry.category.split(':')[1];
    assert.ok(existsSync(join(book, 'categories', `${cat}.json`)), `${rel}: category ${cat}`);
    for (const p of entry.parents ?? []) assert.ok(ids.has(p.entry), `${rel}: parent ${p.entry}`);
    const adv = entry.condition?.advancement_id;
    if (adv) {
      const [ns, path] = adv.split(':');
      assert.ok(existsSync(join(kubejs, 'data', ns, 'advancement', `${path}.json`)), `${rel}: ${adv}`);
    }
  }
});
