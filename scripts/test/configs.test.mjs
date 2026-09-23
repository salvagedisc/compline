import { test } from 'node:test';
import assert from 'node:assert/strict';
import { mkdtemp, mkdir, writeFile, readFile } from 'node:fs/promises';
import { tmpdir } from 'node:os';
import { join } from 'node:path';
import { diffConfigs, copyConfigs } from '../sync/configs.mjs';

test('detects new and modified config files recursively, then copies them', async () => {
  const src = await mkdtemp(join(tmpdir(), 'cfg-src-'));
  const dst = await mkdtemp(join(tmpdir(), 'cfg-dst-'));
  await mkdir(join(src, 'coldsweat'));
  await writeFile(join(src, 'coldsweat', 'main.toml'), 'a = 2');
  await writeFile(join(src, 'same.toml'), 'x');
  await writeFile(join(src, 'changed.toml'), 'new');
  await writeFile(join(dst, 'same.toml'), 'x');
  await writeFile(join(dst, 'changed.toml'), 'old');
  const entries = await diffConfigs(src, dst);
  assert.deepEqual(entries, [
    { rel: 'changed.toml', status: 'modified' },
    { rel: join('coldsweat', 'main.toml'), status: 'new' },
  ]);
  await copyConfigs(src, dst, entries);
  assert.equal(await readFile(join(dst, 'coldsweat', 'main.toml'), 'utf8'), 'a = 2');
  assert.equal(await readFile(join(dst, 'changed.toml'), 'utf8'), 'new');
});
