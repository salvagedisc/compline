const versionUrl = (m) => `https://modrinth.com/mod/${m.projectId}/version/${m.versionId}`;

// Removals first so a version change never leaves two copies of the same mod.
export function toPackwizCommands(diff) {
  return [
    ...diff.removed.map((m) => ['remove', m.key]),
    ...diff.changed.map((c) => ['remove', c.from.key]),
    ...diff.changed.map((c) => ['-y', 'modrinth', 'add', versionUrl(c.to)]),
    ...diff.added.map((m) => ['-y', 'modrinth', 'add', versionUrl(m)]),
  ];
}
