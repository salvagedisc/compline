import { test } from 'node:test';
import assert from 'node:assert/strict';
import { readFile } from 'node:fs/promises';
import { dirname, join } from 'node:path';
import { fileURLToPath } from 'node:url';

const packDir = join(dirname(fileURLToPath(import.meta.url)), '..', '..', 'pack');

// Without a version line, Minecraft upgrades options.txt from data version 0: the
// pre-1.13 key-binding fix then fails on modern values such as "key.mouse.middle",
// which the Modrinth App merges into this template, and every option is discarded.
test('shipped options.txt declares the data version of 1.21.1', async () => {
  const text = await readFile(join(packDir, 'config', 'defaultoptions', 'options.txt'), 'utf8');
  const version = text.split('\n').find((line) => line.startsWith('version:'));
  assert.equal(version, 'version:3955');
});
