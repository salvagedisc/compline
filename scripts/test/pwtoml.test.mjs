import { test } from 'node:test';
import assert from 'node:assert/strict';
import { mkdtemp, mkdir, writeFile } from 'node:fs/promises';
import { tmpdir } from 'node:os';
import { join } from 'node:path';
import { parsePwToml, readPackMods } from '../sync/pwtoml.mjs';

const SAMPLE = `name = "Create"
filename = "create-1.21.1-6.0.6.jar"
side = "both"

[download]
url = "https://cdn.modrinth.com/data/LNytGWDc/versions/abc/create.jar"
hash-format = "sha512"
hash = "deadbeef"

[update]
[update.modrinth]
mod-id = "LNytGWDc"
version = "abc123"
`;

test('parsePwToml extracts fields', () => {
  assert.deepEqual(parsePwToml(SAMPLE), {
    name: 'Create', filename: 'create-1.21.1-6.0.6.jar', side: 'both',
    hashFormat: 'sha512', hash: 'deadbeef', projectId: 'LNytGWDc', versionId: 'abc123',
  });
});

test('parsePwToml without modrinth section returns null ids', () => {
  const r = parsePwToml('name = "X"\nfilename = "x.jar"\nside = "both"\n[download]\nhash-format = "sha1"\nhash = "aa"\n');
  assert.equal(r.projectId, null);
  assert.equal(r.versionId, null);
});

test('readPackMods reads every .pw.toml with its key', async () => {
  const dir = await mkdtemp(join(tmpdir(), 'fallow-pack-'));
  await mkdir(join(dir, 'mods'));
  await writeFile(join(dir, 'mods', 'create.pw.toml'), SAMPLE);
  await writeFile(join(dir, 'mods', 'notes.txt'), 'ignored');
  const mods = await readPackMods(dir);
  assert.equal(mods.length, 1);
  assert.equal(mods[0].key, 'create');
  assert.equal(mods[0].projectId, 'LNytGWDc');
});
