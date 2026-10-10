import { readdir, readFile } from 'node:fs/promises';
import { join } from 'node:path';

// Minimal reader for the flat subset of TOML that packwiz writes in .pw.toml files.
export function parsePwToml(text) {
  const sections = {};
  let current = '';
  for (const raw of text.split(/\r?\n/)) {
    const line = raw.trim();
    if (!line || line.startsWith('#')) continue;
    const header = line.match(/^\[([^\]]+)\]$/);
    if (header) { current = header[1]; continue; }
    const kv = line.match(/^([\w-]+)\s*=\s*"(.*)"$/);
    if (kv) (sections[current] ??= {})[kv[1]] = kv[2];
  }
  const root = sections[''] ?? {};
  const download = sections.download ?? {};
  const modrinth = sections['update.modrinth'] ?? {};
  return {
    name: root.name, filename: root.filename, side: root.side,
    url: download.url,
    hashFormat: download['hash-format'], hash: download.hash,
    projectId: modrinth['mod-id'] ?? null, versionId: modrinth.version ?? null,
  };
}

export async function readPackMods(packDir) {
  const modsDir = join(packDir, 'mods');
  const files = (await readdir(modsDir)).filter((f) => f.endsWith('.pw.toml')).sort();
  return Promise.all(files.map(async (f) => ({
    key: f.slice(0, -'.pw.toml'.length),
    ...parsePwToml(await readFile(join(modsDir, f), 'utf8')),
  })));
}
