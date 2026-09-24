const BUILTIN = new Set(['minecraft', 'neoforge', 'forge', 'java']);

const field = (text, key) => text.match(new RegExp(`^\\s*${key}\\s*=\\s*"([^"]*)"`, 'm'))?.[1];

// Reads META-INF/neoforge.mods.toml: mod ids declared in [[mods]] and every [[dependencies.*]] entry.
export function parseModsToml(text) {
  const mods = [];
  const deps = [];
  for (const block of text.split(/^\s*(?=\[\[)/m)) {
    const header = block.match(/^\[\[([^\]]+)\]\]/)?.[1];
    if (header === 'mods') {
      const id = field(block, 'modId');
      if (id) mods.push(id);
    } else if (header?.startsWith('dependencies.')) {
      const dep = field(block, 'modId');
      if (!dep) continue;
      const type = (field(block, 'type') ?? (/mandatory\s*=\s*true/.test(block) ? 'required' : 'optional')).toLowerCase();
      deps.push({ owner: header.slice('dependencies.'.length).replace(/"/g, ''), dep, type, range: field(block, 'versionRange') });
    }
  }
  return { mods, deps };
}

export function findMissing(provided, deps) {
  return deps.filter((d) => d.type === 'required' && !BUILTIN.has(d.dep) && !provided.has(d.dep));
}
