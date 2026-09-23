import { copyFile, mkdir, readdir, readFile } from 'node:fs/promises';
import { dirname, join } from 'node:path';

async function listFiles(dir, prefix = '') {
  let entries;
  try { entries = await readdir(join(dir, prefix), { withFileTypes: true }); } catch { return []; }
  const out = [];
  for (const e of entries) {
    const rel = join(prefix, e.name);
    if (e.isDirectory()) out.push(...await listFiles(dir, rel));
    else out.push(rel);
  }
  return out;
}

async function readOrNull(path) {
  try { return await readFile(path); } catch { return null; }
}

export async function diffConfigs(srcDir, dstDir) {
  const result = [];
  for (const rel of (await listFiles(srcDir)).sort()) {
    const dst = await readOrNull(join(dstDir, rel));
    if (dst === null) result.push({ rel, status: 'new' });
    else if (!dst.equals(await readFile(join(srcDir, rel)))) result.push({ rel, status: 'modified' });
  }
  return result;
}

export async function copyConfigs(srcDir, dstDir, entries) {
  for (const { rel } of entries) {
    await mkdir(dirname(join(dstDir, rel)), { recursive: true });
    await copyFile(join(srcDir, rel), join(dstDir, rel));
  }
}
