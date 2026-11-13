#!/usr/bin/env node
import { spawnSync } from 'node:child_process';
import { createInterface } from 'node:readline/promises';
import { dirname, join } from 'node:path';
import { fileURLToPath } from 'node:url';
import { readPackMods } from './sync/pwtoml.mjs';
import { resolveInstanceDir, readInstanceJars } from './sync/instance.mjs';
import { lookupVersions, lookupProjects } from './sync/modrinth.mjs';
import { diffMods } from './sync/diff.mjs';
import { toPackwizCommands } from './sync/commands.mjs';
import { diffConfigs, copyConfigs } from './sync/configs.mjs';

const USAGE = `Usage: npm run sync -- --instance <nom|chemin> [--configs] [--dry-run] [--yes]
  --instance  Nom de l'instance Modrinth App (défaut : "Tilth test") ou chemin absolu
  --configs   Rapatrie aussi les fichiers de config/ modifiés
  --dry-run   Affiche le diff et les commandes sans rien appliquer
  --yes       N'attend pas de confirmation`;

function parseArgs(argv) {
  const opts = { instance: 'Tilth test', configs: false, dryRun: false, yes: false, help: false };
  for (let i = 0; i < argv.length; i++) {
    const a = argv[i];
    if (a === '--instance') opts.instance = argv[++i];
    else if (a === '--configs') opts.configs = true;
    else if (a === '--dry-run') opts.dryRun = true;
    else if (a === '--yes') opts.yes = true;
    else if (a === '--help' || a === '-h') opts.help = true;
    else throw new Error(`Argument inconnu : ${a}`);
  }
  return opts;
}

export async function run({ argv, env, fetch, exec, confirm, log, packDir }) {
  let opts;
  try { opts = parseArgs(argv); } catch (e) { log(e.message); log(USAGE); return 2; }
  if (opts.help) { log(USAGE); return 0; }

  const instanceDir = resolveInstanceDir(opts.instance, env);
  let diff, titles;
  try {
    const jars = await readInstanceJars(instanceDir);
    if (jars.length === 0) throw new Error(`aucun mod actif dans ${instanceDir} (mauvaise instance ?)`);
    const versions = await lookupVersions(jars.map((j) => j.sha512), { fetch });
    const instanceMods = jars.map((j) => ({ ...j, ...(versions.get(j.sha512) ?? { projectId: null, versionId: null, versionNumber: null }) }));
    diff = diffMods(instanceMods, await readPackMods(packDir));
    titles = await lookupProjects([...diff.added, ...diff.changed.map((c) => c.to)].map((m) => m.projectId), { fetch });
  } catch (e) {
    log(`Erreur : ${e.message}`);
    log('Aucune modification appliquée.');
    return 1;
  }

  const title = (m) => titles.get(m.projectId)?.title ?? m.projectId;
  log(`Instance : ${instanceDir}`);
  for (const m of diff.added) log(`  + ${title(m)} ${m.versionNumber}`);
  for (const m of diff.removed) log(`  - ${m.name}`);
  for (const c of diff.changed) log(`  ~ ${c.from.name} → ${c.to.versionNumber}`);
  for (const m of diff.unknown) log(`  ! ${m.filename} : introuvable sur Modrinth (non conforme, ignoré)`);
  for (const m of diff.serverOnly) log(`  s ${m.name} : mod serveur, non vérifiable dans une instance client (conservé)`);
  log(`  = ${diff.unchanged.length} mod(s) inchangé(s)`);

  const commands = toPackwizCommands(diff);
  const configEntries = opts.configs ? await diffConfigs(join(instanceDir, 'config'), join(packDir, 'config')) : [];
  for (const c of configEntries) log(`  config ${c.status === 'new' ? '+' : '~'} ${c.rel}`);
  for (const args of commands) log(`  $ packwiz ${args.join(' ')}`);

  if (commands.length === 0 && configEntries.length === 0) { log('Rien à synchroniser.'); return 0; }
  if (opts.dryRun) return 0;
  if (!opts.yes && !(await confirm('Appliquer ces changements ? (o/N) '))) { log('Annulé.'); return 0; }

  for (const args of commands) {
    if (exec(args) !== 0) { log(`Échec : packwiz ${args.join(' ')}`); return 1; }
  }
  // Configs are copied before refresh so index.toml lists them.
  await copyConfigs(join(instanceDir, 'config'), join(packDir, 'config'), configEntries);
  if (exec(['refresh']) !== 0) { log('Échec : packwiz refresh (restaurer avec : git restore pack/)'); return 1; }
  log('Terminé. Pense à mettre à jour docs/mods.md et à committer un mod par commit.');
  return 0;
}

const isMain = process.argv[1] && fileURLToPath(import.meta.url) === process.argv[1];
if (isMain) {
  const packDir = join(dirname(fileURLToPath(import.meta.url)), '..', 'pack');
  const rl = createInterface({ input: process.stdin, output: process.stdout });
  const code = await run({
    argv: process.argv.slice(2), env: process.env, fetch: globalThis.fetch, packDir,
    exec: (args) => spawnSync('packwiz', args, { cwd: packDir, stdio: 'inherit', shell: process.platform === 'win32' }).status,
    confirm: async (q) => /^o(ui)?$/i.test((await rl.question(q)).trim()),
    log: (m) => console.log(m),
  });
  rl.close();
  process.exit(code);
}
