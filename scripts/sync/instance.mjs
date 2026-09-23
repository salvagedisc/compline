import { createHash } from 'node:crypto';
import { createReadStream } from 'node:fs';
import { readdir } from 'node:fs/promises';
import { isAbsolute, join } from 'node:path';

export function resolveInstanceDir(nameOrPath, env = process.env) {
  if (isAbsolute(nameOrPath) || /^[A-Za-z]:[\\/]/.test(nameOrPath)) return nameOrPath;
  return join(env.APPDATA, 'ModrinthApp', 'profiles', nameOrPath);
}

function sha512File(path) {
  return new Promise((resolve, reject) => {
    const hash = createHash('sha512');
    createReadStream(path).on('data', (c) => hash.update(c)).on('error', reject)
      .on('end', () => resolve(hash.digest('hex')));
  });
}

// Only *.jar counts: the Modrinth App disables a mod by renaming it to *.jar.disabled.
export async function readInstanceJars(instanceDir) {
  const modsDir = join(instanceDir, 'mods');
  const files = (await readdir(modsDir)).filter((f) => f.endsWith('.jar')).sort();
  return Promise.all(files.map(async (filename) => ({ filename, sha512: await sha512File(join(modsDir, filename)) })));
}
