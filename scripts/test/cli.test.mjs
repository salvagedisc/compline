import { test } from 'node:test';
import assert from 'node:assert/strict';
import { mkdtemp, mkdir, writeFile } from 'node:fs/promises';
import { tmpdir } from 'node:os';
import { join } from 'node:path';
import { run } from '../sync-from-instance.mjs';

async function fixture() {
  const root = await mkdtemp(join(tmpdir(), 'fallow-cli-'));
  const packDir = join(root, 'pack');
  const inst = join(root, 'Fallow test');
  await mkdir(join(packDir, 'mods'), { recursive: true });
  await mkdir(join(inst, 'mods'), { recursive: true });
  await writeFile(join(inst, 'mods', 'new.jar'), 'NEW');
  return { packDir, inst };
}

// Every hash in the request resolves to project P9 ("New Mod").
const knownModFetch = async (url, init) => ({
  ok: true, status: 200,
  json: async () => url.includes('version_files')
    ? Object.fromEntries(JSON.parse(init.body).hashes.map((h) => [h, { id: 'v1', project_id: 'P9', version_number: '1.0' }]))
    : [{ id: 'P9', slug: 'newmod', title: 'New Mod' }],
});

test('network failure aborts before any packwiz command', async () => {
  const { packDir, inst } = await fixture();
  const exec = [];
  const code = await run({
    argv: ['--instance', inst, '--yes'], env: {}, packDir,
    fetch: async () => ({ ok: false, status: 500, json: async () => ({}) }),
    exec: (args) => { exec.push(args); return 0; }, confirm: async () => true, log: () => {},
  });
  assert.equal(code, 1);
  assert.deepEqual(exec, []);
});

test('declined confirmation runs nothing', async () => {
  const { packDir, inst } = await fixture();
  const exec = [];
  const code = await run({ argv: ['--instance', inst], env: {}, packDir, fetch: knownModFetch, exec: (a) => { exec.push(a); return 0; }, confirm: async () => false, log: () => {} });
  assert.equal(code, 0);
  assert.deepEqual(exec, []);
});

test('--dry-run prints commands without running them', async () => {
  const { packDir, inst } = await fixture();
  const exec = [];
  const logs = [];
  const code = await run({ argv: ['--instance', inst, '--dry-run'], env: {}, packDir, fetch: knownModFetch, exec: (a) => { exec.push(a); return 0; }, confirm: async () => true, log: (m) => logs.push(m) });
  assert.equal(code, 0);
  assert.deepEqual(exec, []);
  assert.ok(logs.some((l) => l.includes('+ New Mod')));
  assert.ok(logs.some((l) => l.includes('modrinth add https://modrinth.com/mod/P9/version/v1')));
});

test('confirmed run executes packwiz commands then refresh', async () => {
  const { packDir, inst } = await fixture();
  const exec = [];
  const code = await run({ argv: ['--instance', inst], env: {}, packDir, fetch: knownModFetch, exec: (a) => { exec.push(a); return 0; }, confirm: async () => true, log: () => {} });
  assert.equal(code, 0);
  assert.deepEqual(exec, [['-y', 'modrinth', 'add', 'https://modrinth.com/mod/P9/version/v1'], ['refresh']]);
});
