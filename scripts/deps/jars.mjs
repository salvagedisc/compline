import { readFile, writeFile } from 'node:fs/promises';
import { join } from 'node:path';

// Returns the jar of a pack mod, downloading it into the cache directory the first time.
export async function loadJar(cacheDir, mod, userAgent = 'salvagedisc/compline') {
  const path = join(cacheDir, mod.filename);
  try { return await readFile(path); } catch { /* not cached yet */ }
  const res = await fetch(mod.url, { headers: { 'User-Agent': userAgent } });
  if (!res.ok) throw new Error(`download ${mod.filename}: HTTP ${res.status}`);
  const buf = Buffer.from(await res.arrayBuffer());
  await writeFile(path, buf);
  return buf;
}
