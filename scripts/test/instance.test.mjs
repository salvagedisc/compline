import { test } from 'node:test';
import assert from 'node:assert/strict';
import { mkdtemp, mkdir, writeFile } from 'node:fs/promises';
import { tmpdir } from 'node:os';
import { join } from 'node:path';
import { createHash } from 'node:crypto';
import { resolveInstanceDir, readInstanceJars } from '../sync/instance.mjs';

test('resolveInstanceDir maps a name to the Modrinth App profiles folder', () => {
  const env = { APPDATA: 'C:\\Users\\utilisateur\\AppData\\Roaming' };
  assert.equal(
    resolveInstanceDir('Fallow test', env),
    join('C:\\Users\\utilisateur\\AppData\\Roaming', 'ModrinthApp', 'profiles', 'Fallow test'),
  );
});

test('resolveInstanceDir keeps absolute paths', () => {
  assert.equal(resolveInstanceDir('D:\\inst', {}), 'D:\\inst');
});

test('readInstanceJars hashes .jar files and skips disabled ones (path with spaces and accents)', async () => {
  const root = await mkdtemp(join(tmpdir(), 'fallow inst é-'));
  await mkdir(join(root, 'mods'));
  await writeFile(join(root, 'mods', 'a.jar'), 'AAA');
  await writeFile(join(root, 'mods', 'b.jar.disabled'), 'BBB');
  await writeFile(join(root, 'mods', 'readme.txt'), 'x');
  const jars = await readInstanceJars(root);
  assert.deepEqual(jars, [{ filename: 'a.jar', sha512: createHash('sha512').update('AAA').digest('hex') }]);
});
