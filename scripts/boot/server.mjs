#!/usr/bin/env node
// Boot check: starts a dedicated server with every server-side mod, config and KubeJS script of the
// pack, lets the fallow mod write its registries once the world is ready (-Dfallow.bootcheck),
// then fails on a crash, on any error the server or KubeJS logs that scripts/boot/known-errors.txt
// does not accept, and on any item of the KubeJS removal lists that does not exist.
// Usage: node scripts/boot/server.mjs [--jar <fallow jar>] [--dir <work dir>] [--timeout <minutes>]
import { spawn, execFileSync } from 'node:child_process';
import { cp, mkdir, readdir, readFile, rm, writeFile } from 'node:fs/promises';
import { existsSync } from 'node:fs';
import { basename, dirname, join } from 'node:path';
import { fileURLToPath } from 'node:url';
import { parseArgs } from 'node:util';
import { readPackMods } from '../sync/pwtoml.mjs';
import { loadJar } from '../deps/jars.mjs';
import { readListedItems, unknownItems } from './ids.mjs';

const root = join(dirname(fileURLToPath(import.meta.url)), '..', '..');
const packDir = join(root, 'pack');
const { values: args } = parseArgs({
  options: {
    jar: { type: 'string' },
    dir: { type: 'string', default: join(root, '.cache', 'server') },
    timeout: { type: 'string', default: '25' },
    memory: { type: 'string', default: '5G' },
  },
});
const work = args.dir;
const jarCache = join(root, '.cache', 'jars');
const registriesFile = join(work, 'boot-registries.json');
const annotate = (message) => console.log(process.env.GITHUB_ACTIONS ? `::error::${message}` : `ERREUR ${message}`);

const packToml = await readFile(join(packDir, 'pack.toml'), 'utf8');
const neoforge = packToml.match(/^neoforge = "(.+)"$/m)[1];

// 1. NeoForge server, installed once and kept in the work directory.
const argsFile = join('libraries', 'net', 'neoforged', 'neoforge', neoforge, 'unix_args.txt');
if (!existsSync(join(work, argsFile))) {
  await mkdir(work, { recursive: true });
  const installer = join(root, '.cache', `neoforge-${neoforge}-installer.jar`);
  if (!existsSync(installer)) {
    const url = `https://maven.neoforged.net/releases/net/neoforged/neoforge/${neoforge}/neoforge-${neoforge}-installer.jar`;
    const res = await fetch(url);
    if (!res.ok) throw new Error(`NeoForge installer: HTTP ${res.status}`);
    await writeFile(installer, Buffer.from(await res.arrayBuffer()));
  }
  console.log(`Installation du serveur NeoForge ${neoforge}`);
  execFileSync('java', ['-jar', installer, '--install-server', work], { stdio: 'inherit' });
}

// 2. A fresh pack: mods, configs, scripts, and a new world each time.
for (const name of ['mods', 'config', 'defaultconfigs', 'kubejs', 'world', 'logs', 'crash-reports', 'boot-registries.json']) {
  await rm(join(work, name), { recursive: true, force: true });
}
await mkdir(join(work, 'mods'), { recursive: true });
await mkdir(jarCache, { recursive: true });
const mods = (await readPackMods(packDir)).filter((mod) => mod.side !== 'client');
for (const mod of mods) {
  await writeFile(join(work, 'mods', mod.filename), await loadJar(jarCache, mod, 'salvagedisc/fallow boot-check'));
}
for (const name of (await readdir(join(packDir, 'mods'))).filter((n) => n.endsWith('.jar'))) {
  const replaced = args.jar && name.startsWith('fallow-');
  await cp(replaced ? args.jar : join(packDir, 'mods', name), join(work, 'mods', replaced ? basename(args.jar) : name));
}
for (const name of ['config', 'defaultconfigs', 'kubejs']) {
  await cp(join(packDir, name), join(work, name), { recursive: true });
}
await writeFile(join(work, 'eula.txt'), 'eula=true\n');
await writeFile(join(work, 'server.properties'), [
  'online-mode=false', 'level-seed=fallow', 'max-tick-time=-1', 'enable-rcon=false', 'server-port=25599', '',
].join('\n'));
console.log(`${mods.length} mods de Modrinth et ${(await readdir(join(work, 'mods'))).length - mods.length} jars du dépôt`);

// 3. Start it and wait for the fallow mod to stop it.
const started = Date.now();
const code = await new Promise((resolve) => {
  const java = spawn('java', [`-Xmx${args.memory}`, `-Dfallow.bootcheck=${registriesFile}`, `@${argsFile}`, 'nogui'],
    { cwd: work, stdio: ['ignore', 'ignore', 'inherit'] });
  const timer = setTimeout(() => {
    annotate(`le serveur n'est pas prêt après ${args.timeout} minutes`);
    java.kill('SIGKILL');
  }, Number(args.timeout) * 60_000);
  java.on('exit', (exit) => { clearTimeout(timer); resolve(exit); });
});
console.log(`Serveur arrêté (code ${code}) après ${Math.round((Date.now() - started) / 1000)} s`);

// 4. Errors in the logs.
const known = (await readFile(join(root, 'scripts', 'boot', 'known-errors.txt'), 'utf8'))
  .split('\n').map((line) => line.trim()).filter((line) => line && !line.startsWith('#')).map((line) => new RegExp(line));
const errors = new Map();
async function collect(file, pattern) {
  if (!existsSync(file)) return;
  for (const line of (await readFile(file, 'utf8')).split('\n')) {
    if (!pattern.test(line)) continue;
    // Drop the timestamp and the thread name: the same error must read the same on every run.
    const text = line.replace(/^\[[^\]]*\]\s*/, '').replace(/^\[[^\]/]*\/(ERROR|FATAL)\]/, '[$1]').trim();
    const key = `${basename(file)}: ${text}`;
    errors.set(key, (errors.get(key) ?? 0) + 1);
  }
}
await collect(join(work, 'logs', 'latest.log'), /\/(ERROR|FATAL)\]/);
for (const name of existsSync(join(work, 'logs', 'kubejs')) ? await readdir(join(work, 'logs', 'kubejs')) : []) {
  await collect(join(work, 'logs', 'kubejs', name), /\[(ERR|ERROR|FATAL)\s*\]/);
}
let failures = 0;
const accepted = [];
for (const [error, count] of errors) {
  if (known.some((re) => re.test(error))) { accepted.push(error); continue; }
  annotate(`${error}${count > 1 ? ` (x${count})` : ''}`);
  failures++;
}
console.log(`${errors.size} erreur(s) distincte(s) dans les journaux, ${accepted.length} acceptée(s) par known-errors.txt`);

// 5. The world must have started, and every listed item must exist.
if (!existsSync(registriesFile)) {
  const crashes = existsSync(join(work, 'crash-reports')) ? await readdir(join(work, 'crash-reports')) : [];
  annotate(`le serveur ne s'est pas lancé jusqu'au bout${crashes.length ? ` (rapport de plantage : ${crashes.join(', ')})` : ''}`);
  process.exit(1);
}
const registries = JSON.parse(await readFile(registriesFile, 'utf8'));
const missing = unknownItems(await readListedItems(join(packDir, 'kubejs')), registries);
for (const { id, file, why } of missing) annotate(`${file} : ${id} (${why})`);
console.log(`${registries.mods.length} mods chargés, ${registries.item.length} objets, ${registries.recipe.length} recettes ; ${missing.length} identifiant(s) inconnu(s) dans les listes KubeJS`);
process.exit(failures || missing.length ? 1 : 0);
