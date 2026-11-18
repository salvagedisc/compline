import { test } from 'node:test';
import assert from 'node:assert/strict';
import { readFile } from 'node:fs/promises';
import { dirname, join } from 'node:path';
import { fileURLToPath } from 'node:url';

const kubejsDir = join(dirname(fileURLToPath(import.meta.url)), '..', '..', 'pack', 'kubejs');

const quotedIds = (text) => [...text.matchAll(/'([a-z0-9_.-]+:[a-z0-9_/.-]+)'/g)].map((match) => match[1]);

// Server side: every `const <NAME>_OBJETS... = [ ... ]` list of removed items.
function serverItems(source) {
  const lists = [...source.matchAll(/const \w*_OBJETS\w* = \[([\s\S]*?)\]/g)];
  return lists.flatMap((list) => quotedIds(list[1]));
}

// Client side: the array handed to RecipeViewerEvents.removeEntriesCompletely.
function clientItems(source) {
  const body = source.match(/removeEntriesCompletely\('item'[\s\S]*?\[([\s\S]*?)\]\.forEach/);
  assert.ok(body, 'no EMI removal list');
  return quotedIds(body[1]);
}

// Each removed item loses its recipes on the server and is hidden from EMI on the client;
// the lists are written twice because KubeJS server and client scripts cannot share code.
for (const name of ['sans_lapin', 'doublons', 'faune_coupee', 'premiers_gestes', 'tri']) {
  test(`${name}: items removed on the server are the items hidden from EMI`, async () => {
    const server = serverItems(await readFile(join(kubejsDir, 'server_scripts', `${name}.js`), 'utf8'));
    const client = clientItems(await readFile(join(kubejsDir, 'client_scripts', `${name}.js`), 'utf8'));
    assert.ok(server.length > 0);
    assert.deepEqual([...client].sort(), [...server].sort());
  });
}

// Fused items: the keys of the FUSIONS map disappear into another mod's item and are hidden
// from EMI.
test('fusions: fused items on the server are the items hidden from EMI', async () => {
  const source = await readFile(join(kubejsDir, 'server_scripts', 'fusions.js'), 'utf8');
  const map = source.match(/const FUSIONS = \{([\s\S]*?)\n\}/);
  assert.ok(map, 'no FUSIONS map');
  const server = [...map[1].matchAll(/'([a-z0-9_.-]+:[a-z0-9_/.-]+)':/g)].map((match) => match[1]);
  const client = clientItems(await readFile(join(kubejsDir, 'client_scripts', 'fusions.js'), 'utf8'));
  assert.ok(server.length > 0);
  assert.deepEqual([...client].sort(), [...server].sort());
});

// The Steam 'n' Rails variants are removed by pattern, on both sides.
test('tri: Steam \'n\' Rails patterns are the same on the server and in EMI', async () => {
  const patterns = (source) => {
    const list = source.match(/const TRI_MOTIFS = \[([\s\S]*?)\n\]/);
    assert.ok(list, 'no TRI_MOTIFS list');
    return list[1].split('\n').map((line) => line.trim().replace(/,$/, '')).filter(Boolean);
  };
  const server = patterns(await readFile(join(kubejsDir, 'server_scripts', 'tri.js'), 'utf8'));
  const client = patterns(await readFile(join(kubejsDir, 'client_scripts', 'tri.js'), 'utf8'));
  assert.equal(server.length, 2);
  assert.deepEqual(client, server);
});

// The creative tabs drop the same items: the startup script is generated from the lists above.
test('creative tabs: the startup script matches the server lists (npm run gen-creative)', async () => {
  const { buildScript, outputPath } = await import('../kubejs/creative-tabs.mjs');
  assert.equal(await readFile(outputPath, 'utf8'), await buildScript());
});
