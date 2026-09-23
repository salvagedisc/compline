// Mods are matched by Modrinth project id, so a version bump is one "changed" entry, not remove + add.
export function diffMods(instanceMods, packMods) {
  const packById = new Map(packMods.filter((m) => m.projectId).map((m) => [m.projectId, m]));
  const seen = new Set();
  const result = { added: [], removed: [], changed: [], unknown: [], unchanged: [], serverOnly: [] };
  for (const mod of instanceMods) {
    if (!mod.projectId) { result.unknown.push(mod); continue; }
    seen.add(mod.projectId);
    const existing = packById.get(mod.projectId);
    if (!existing) result.added.push(mod);
    else if (existing.versionId !== mod.versionId) result.changed.push({ from: existing, to: mod });
    else result.unchanged.push(existing);
  }
  // The Modrinth App skips server-only files on import, so their absence from a client instance means nothing.
  for (const [id, mod] of packById) {
    if (seen.has(id)) continue;
    if (mod.side === 'server') result.serverOnly.push(mod);
    else result.removed.push(mod);
  }
  return result;
}
