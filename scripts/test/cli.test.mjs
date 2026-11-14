import { test } from 'node:test';
import assert from 'node:assert/strict';
import { mkdtemp, mkdir, writeFile } from 'node:fs/promises';
import { existsSync } from 'node:fs';
import { tmpdir } from 'node:os';
import { join } from 'node:path';
import { run } from '../sync-from-instance.mjs';

async function fixture() {
  const root = await mkdtemp(join(tmpdir(), 'compline-cli-'));
  const packDir = join(root, 'pack');
  const inst = join(root, 'Compline test');
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

test('--configs copies config files before refresh, even without mod changes', async () => {
  const { packDir, inst } = await fixture();
  await mkdir(join(inst, 'config'), { recursive: true });
  await writeFile(join(inst, 'config', 'coldsweat.toml'), 'a = 1');
  const exec = [];
  const fetch = async (url, init) => ({ ok: true, status: 200, json: async () => (url.includes('version_files') ? {} : []) });
  const code = await run({
    argv: ['--instance', inst, '--configs', '--yes'], env: {}, packDir, fetch, confirm: async () => true, log: () => {},
    exec: (a) => { exec.push({ args: a, copied: existsSync(join(packDir, 'config', 'coldsweat.toml')) }); return 0; },
  });
  assert.equal(code, 0);
  assert.deepEqual(exec, [{ args: ['refresh'], copied: true }]);
});

test('empty instance mods folder aborts instead of proposing to remove the whole pack', async () => {
  const root = await mkdtemp(join(tmpdir(), 'compline-empty-'));
  const packDir = join(root, 'pack');
  const inst = join(root, 'inst');
  await mkdir(join(packDir, 'mods'), { recursive: true });
  await mkdir(join(inst, 'mods'), { recursive: true });
  await writeFile(join(packDir, 'mods', 'create.pw.toml'), 'name = "Create"\nside = "both"\n[update.modrinth]\nmod-id = "P1"\nversion = "v1"\n');
  const exec = [];
  const code = await run({ argv: ['--instance', inst, '--yes'], env: {}, packDir, fetch: knownModFetch, exec: (a) => { exec.push(a); return 0; }, confirm: async () => true, log: () => {} });
  assert.equal(code, 1);
  assert.deepEqual(exec, []);
});
