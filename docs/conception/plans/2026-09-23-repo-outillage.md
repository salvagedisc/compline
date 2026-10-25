# Fallow — Repo & tooling: implementation plan


**Goal:** Turn the `salvagedisc/fallow` repo into the packwiz source of truth of the Fallow modpack, with a base pack, documentation, a synchronisation script from the Modrinth App and CI that produces/publishes the `.mrpack`.

**Architecture:** The pack lives in `pack/` (packwiz root). A dependency-free Node.js script (`scripts/sync/`) compares the Modrinth App test instance with the pack via the Modrinth API and drives `packwiz`. Two GitHub Actions workflows: `build.yml` (check + export on every push) and `release.yml` (release on tag, Modrinth step disabled by default).

**Tech Stack:** packwiz (Go, pinned commit), Minecraft 1.21.1, NeoForge 21.1.251, Node.js ≥ 20 (native `node:test`, `fetch`, `crypto`), GitHub Actions, Modrinth API v2.

**Spec:** `docs/conception/specs/2026-09-23-repo-outillage-design.md`

## Global Constraints

- Minecraft **1.21.1**, loader **NeoForge 21.1.251** (pinned in `pack/pack.toml`).
- Every mod must come from **Modrinth** and exist for 1.21.1 NeoForge; an unavailable mod is flagged, never replaced by a third-party source.
- Only `pack/` ends up in the `.mrpack`; docs, scripts and CI never do.
- Datapack data in `pack/kubejs/data/fallow/`, no `datapacks/` folder.
- Mod updates one by one; never `packwiz update --all`.
- Commit messages: `feat(mods): add <Name>`, `chore(mods): update <Name> x → y`, `feat(mods)!: remove <Name>`, `docs: …`, `ci: …`, `feat(sync): …`.
- The sync script **never modifies** the Modrinth App instance.
- No token is handled by the scripts or the setup: `MODRINTH_TOKEN` is created and entered by the maintainer.
- packwiz pinned to commit `ef87d964f8cbd52b3b13ea42453ef322290e2b9e`.
- Docs and user messages in French; code identifiers in English.

## Review Focus

- **Mod disabled in the Modrinth App** (`xxx.jar.disabled`): must be considered absent from the instance, not a mod to add. → Task 5 test.
- **Jar absent from Modrinth** (mod imported by hand): must be listed as "unknown / non-compliant" and trigger neither an addition nor a removal. → Task 6 test.
- **Same project, other version**: must appear as a single version change, not as a removal + an addition of two different mods. → Task 6 test.
- **Instance path with spaces and accents** (`C:\Users\Prénom Nom\…\profiles\Fallow test`): the script must find and read it. → Task 5 test.
- **Network / Modrinth API failure**: the script stops before any packwiz command (no partial application). → Task 7 test.

---

## File structure

```
.gitattributes                 ← LF normalisation (avoids CRLF warnings)
.gitignore                     ← dist/, node_modules/
package.json                   ← "type": "module", "test" script
pack/
  pack.toml, index.toml        ← packwiz
  .packwizignore               ← excludes .gitkeep
  mods/*.pw.toml
  config/.gitkeep, defaultconfigs/.gitkeep
  kubejs/{startup_scripts,server_scripts,client_scripts}/.gitkeep
  kubejs/data/fallow/.gitkeep
scripts/
  sync-from-instance.mjs       ← CLI (argument parsing, confirmation, orchestration)
  sync/
    pwtoml.mjs                 ← reading the pack's .pw.toml files
    instance.mjs               ← instance location + jar hashing
    modrinth.mjs               ← Modrinth API calls (injectable fetch)
    diff.mjs                   ← instance ↔ pack diff
    commands.mjs               ← diff → packwiz arguments
    configs.mjs                ← config diff/copy
  test/*.test.mjs              ← node:test tests
docs/vision.md, docs/mods.md, docs/decisions/0001-nml-vs-terralith.md
README.md, CHANGELOG.md
.github/workflows/build.yml, .github/workflows/release.yml
```

---

### Task 1: Install packwiz locally

**Files:** none (machine tooling).

**Interfaces:**
- Produces: `packwiz` command available in the PATH.

- [ ] **Step 1: Ask the maintainer for approval** to install Go via scoop (`scoop install go`, ~70 MB, bucket `main`) then compile packwiz from `github.com/packwiz/packwiz`. Download nothing without an explicit "yes".

- [ ] **Step 2: Install**

```bash
scoop install go
go install github.com/packwiz/packwiz@ef87d964f8cbd52b3b13ea42453ef322290e2b9e
```

- [ ] **Step 3: Verify**

Run: `"$(go env GOPATH)/bin/packwiz" --help`
Expected: displays the help with the subcommands `init`, `modrinth`, `refresh`, `remove`.
If `packwiz` is not found directly, add `%USERPROFILE%\go\bin` to the user PATH (ask the maintainer) and reopen the shell.

---

### Task 2: Repo and pack skeleton

**Files:**
- Create: `.gitattributes`, `.gitignore`, `package.json`, `pack/.packwizignore`, `pack/pack.toml` (via packwiz), `pack/index.toml` (via packwiz), the `.gitkeep` files listed above.

**Interfaces:**
- Consumes: `packwiz` (Task 1).
- Produces: `pack/pack.toml` with `name = "Fallow"`, `version = "0.1.0"`, `[versions] minecraft = "1.21.1"`, `neoforge = "21.1.251"`.

- [ ] **Step 1: Repo files**

`.gitattributes`:
```
* text=auto eol=lf
*.jar binary
*.mrpack binary
*.png binary
```

`.gitignore`:
```
dist/
node_modules/
*.mrpack
```

`package.json`:
```json
{
  "name": "fallow-tools",
  "private": true,
  "type": "module",
  "engines": { "node": ">=20" },
  "scripts": {
    "test": "node --test scripts/test/",
    "sync": "node scripts/sync-from-instance.mjs"
  }
}
```

- [ ] **Step 2: Initialise packwiz**

```bash
mkdir -p pack && cd pack
packwiz init --name "Fallow" --author "salvagedisc" --version "0.1.0" \
  --mc-version 1.21.1 --modloader neoforge --neoforge-version 21.1.251 -r
```
Expected: `pack/pack.toml` and `pack/index.toml` created. Open `pack.toml` and check that `minecraft = "1.21.1"` and `neoforge = "21.1.251"` are present under `[versions]`.

- [ ] **Step 3: Directory tree and exclusions**

```bash
mkdir -p pack/config pack/defaultconfigs pack/kubejs/startup_scripts pack/kubejs/server_scripts pack/kubejs/client_scripts pack/kubejs/data/fallow
for d in config defaultconfigs kubejs/startup_scripts kubejs/server_scripts kubejs/client_scripts kubejs/data/fallow; do touch "pack/$d/.gitkeep"; done
```

`pack/.packwizignore`:
```
**/.gitkeep
```

- [ ] **Step 4: Verify the export**

```bash
cd pack && packwiz refresh && packwiz mr export -o ../dist/test.mrpack && cd ..
unzip -p dist/test.mrpack modrinth.index.json
```
Expected: JSON containing `"minecraft": "1.21.1"` and `"neoforge": "21.1.251"` in `dependencies`, `"files": []`, and `unzip -l dist/test.mrpack` lists **no** `.gitkeep`.

- [ ] **Step 5: Commit**

```bash
git add -A && git commit -m "chore: scaffold packwiz pack (MC 1.21.1, NeoForge 21.1.251)"
```

---

### Task 3: Base pack (retained mods)

**Files:**
- Create: `pack/mods/*.pw.toml` (generated by packwiz), `pack/index.toml` (updated).

**Interfaces:**
- Consumes: initialised pack (Task 2).
- Produces: one `.pw.toml` per mod with `[update.modrinth] mod-id` and `version`, used by `pwtoml.mjs` (Task 5).

Modrinth slugs verified on 2026-09-23 (all available for 1.21.1 NeoForge):

| Mod | Slug |
|---|---|
| No Man's Land | `no-mans-land` |
| Overgeared | `overgeared` |
| Create | `create` |
| Create Aeronautics | `create-aeronautics` |
| Sable | `sable` |
| Steam 'n' Rails (NeoForge port) | `create-steam-n-rails-1.21.1` |
| Kaleidoscope Cookery | `kaleidoscope-cookery` |
| Antique Atlas 4 | `antique-atlas-4` |
| Surveyor Map Framework | `surveyor` |
| Cold Sweat | `cold-sweat` |
| Thirst Was Reclaimed | `thirst-was-reclaimed` |
| KubeJS | `kubejs` |

- [ ] **Step 1: Add each mod, one commit per mod**

```bash
cd pack
for slug in create sable create-aeronautics create-steam-n-rails-1.21.1 no-mans-land overgeared kaleidoscope-cookery surveyor antique-atlas-4 cold-sweat thirst-was-reclaimed kubejs; do
  packwiz -y mr add "$slug" || { echo "ÉCHEC: $slug"; break; }
  git -C .. add -A pack
  git -C .. commit -q -m "feat(mods): add $slug"
done
cd ..
```
`-y` automatically accepts required dependencies; they are included in the commit of the mod that pulls them in. If a slug fails: stop and report the mod to the maintainer (do not look for an alternative).

- [ ] **Step 2: Verify the sides**

Run: `grep -H '^side' pack/mods/*.pw.toml`
Expected: each mod has `side = "both"`, `"client"` or `"server"`. None of the retained mods is supposed to be `client` only; if one is, report it.

- [ ] **Step 3: Verify index consistency**

Run: `cd pack && packwiz refresh && git status --porcelain`
Expected: no output from `git status`.

- [ ] **Step 4: Export and launch test (manual, maintainer)**

```bash
cd pack && packwiz mr export -o ../dist/Fallow-0.1.0-dev.mrpack && cd ..
```
Ask the maintainer: Modrinth App → "+" → *Import from file* → `dist/Fallow-0.1.0-dev.mrpack`, name the instance **`Fallow test`**, launch, create a world, walk for 2 minutes. They report: launch OK / crash (provide `logs/latest.log` or the crash report).
Expected: world created without a crash. In case of a crash: read the log, identify the faulty mod, report it — do not fix by changing version without approval.

---

### Task 4: Documentation

**Files:**
- Create: `docs/vision.md`, `docs/mods.md`, `docs/decisions/0001-nml-vs-terralith.md`, `README.md`, `CHANGELOG.md`.

**Interfaces:** none (text).

- [ ] **Step 1: `docs/vision.md`**

```markdown
# Fallow — Vision

## Pitch
A huge, damp and almost abandoned world. A handful of players arrive, settle, then slowly push back the frontier: cabin, farm, hamlet, railway, factory, airfield, distant colonies. There is no end: the permanent world is for perfecting one's installations.

## Art direction
- Mist, distance, silhouettes: the world is hard to read from afar.
- Solitude and diffuse strangeness, **without horror** or jumpscares.
- The base is an island of light; moving away from it means losing one's bearings.
- No imposed shader (Create Aeronautics has known problems with Iris): the atmosphere comes from worldgen, fog, sounds and lighting.

## Three pillars
1. **Exploration** — "I wonder what's over there", not "I need X diamonds". Inspiration: Zelda Breath of the Wild. No coordinates: the map is built (Antique Atlas).
2. **Civilisation** — Crafting (Overgeared) → workshop → industry (Create) → rail (Steam 'n' Rails) → aviation (Aeronautics).
3. **Daily life** — Water → farming → animal husbandry → cooking (Kaleidoscope Cookery) → preservation → expedition.

## Survival
Harder through the environment (temperature, thirst, seasons under study), not through combat. Few hostile creatures, rare, territorial and polished (quality models and animations). No vanilla monsters.

## Prohibitions
- Minimap, waypoints, teleportation.
- Hostile vanilla monsters, difficulty through hordes.
- End / Elytra (probable).
- Stacking Create addons or overlapping food mods.

## Working rules
- Every suggestion is a hypothesis: we really compare before deciding.
- 5-question filter for any mod: does it change gameplay? does it integrate with the other systems? does it stay interesting after 100 h? does it bring a mechanic rather than content? does it avoid duplicates?
- "Add 200 items" is not an argument; "turns an activity into a craft" is.
```

- [ ] **Step 2: `docs/mods.md`**

```markdown
# Mod registry

Statuses: ✅ retained (in `pack/mods/`) · 🟡 to test · ❌ rejected.
Side: C = client, S = server, CS = both.

## Foundation
| Mod | Status | Side | Role | Rationale |
|---|---|---|---|---|
| No Man's Land | ✅ | CS | Worldgen | Wild and coherent atmosphere, better for solitude (see decision 0001) |
| Overgeared | ✅ | CS | Crafting | Forge, blueprints, alloys: physical and progressive fabrication |
| Create | ✅ | CS | Industry | Technological backbone |
| Create Aeronautics + Sable | ✅ | CS | Aviation | Civilisation pillar; requires 1.21.1 NeoForge |
| Create: Steam 'n' Rails | ✅ | CS | Rail | Complete railway infrastructure |
| Kaleidoscope Cookery | ✅ | CS | Cooking | Immersive cooking, main food system |
| Antique Atlas 4 + Surveyor | ✅ | CS | Map | Map that builds itself, replaces coordinates and minimap |
| Cold Sweat | ✅ | CS | Temperature | Environmental survival |
| Thirst Was Reclaimed | ✅ | CS | Thirst | Environmental survival |
| KubeJS | ✅ | CS | Rules | Coherence layer (scripts + datapack data) |

## To test
| Mod | Role | Question to settle |
|---|---|---|
| Thirst Canteen | Canteen | Useful with Thirst Was Reclaimed? |
| Serene Seasons | Seasons | Interaction with Cold Sweat, NML and Kaleidoscope |
| Naturalist | Fauna | Competitor of Alex's Mobs Continued, not an automatic addition |
| Alex's Mobs Continued | Fauna | Same |
| Mowzie's Mobs (+ Integrated Mowzie's Mobs) | Rare creatures | Rare "anomalies" layer |
| Create: Copycats+ | Building | Probable |
| Create: Connected | Create QoL | Probable |
| Distant Horizons | Distant rendering | Client and server performance |
| Terralith + Streams Reflowing | Worldgen | Competing preset to NML (decision 0001) |
| Farm & Charm / other Let's Do | Farming | Next design step: farming |
| Create: Power Loader | Chunk loading | Risk of breaking the infrastructure upkeep logic |

## Rejected
| Mod | Reason |
|---|---|
| Tectonic | Replaced by the NML vs Terralith comparison |
| Downhill Rivers | Same |
| Prehistorum | Poor-quality models |
| Farmer's Delight (+ Create FD addons) | Kaleidoscope Cookery is the main food system |
| Legendary Survival Overhaul | Cold Sweat + Thirst Was Reclaimed retained |
| Minimaps / waypoints | Contrary to the vision |
```

- [ ] **Step 3: `docs/decisions/0001-nml-vs-terralith.md`**

```markdown
# 0001 — Worldgen: No Man's Land vs Terralith + Streams Reflowing

- **Date**: 2026-09 (preliminary design)
- **Status**: NML retained; Terralith + Streams Reflowing to test as a competing preset

## Context
Worldgen carries the atmosphere (solitude, strangeness) and exploration. Tectonic and Downhill Rivers were rejected in favour of this comparison. The two options are not combined: they are two competing presets.

## Comparison
| Criterion | NML | Terralith + Streams |
|---|---|---|
| Unsettling atmosphere / solitude | ★★★★★ | ★★★★☆ |
| Aesthetic coherence | ★★★★★ | ★★★★☆ |
| Diversity / spectacle | ★★★★☆ | ★★★★★ |
| Geographic realism, rivers | ★★★☆☆ | ★★★★★ |
| Stack simplicity | ★★★★★ | ★★★★☆ |

## Decision
NML wins on the experience sought ("I am somewhere I perhaps should not be"). Terralith + Streams Reflowing wins on geography: coherent rivers useful to Create, currents compatible with Sable/Aeronautics. To be re-evaluated in a real test on a `test/terralith-vs-nml` branch.
```

- [ ] **Step 4: `CHANGELOG.md`**

```markdown
# Changelog

Format: [Keep a Changelog](https://keepachangelog.com/fr/1.1.0/). Versions: `0.x` prototype, `1.0.0` launch of the permanent world.

## [Unreleased]

## [0.1.0]
### Added
- Base pack: No Man's Land, Overgeared, Create, Create Aeronautics + Sable, Steam 'n' Rails, Kaleidoscope Cookery, Antique Atlas 4 + Surveyor, Cold Sweat, Thirst Was Reclaimed, KubeJS.
```

- [ ] **Step 5: `README.md`**

````markdown
# Fallow

**Minecraft 1.21.1 / NeoForge** modpack for a permanent world among friends: exploration in the mist, colonies, Create and aviation, long-term farming.

## Play
1. Install the [Modrinth App](https://modrinth.com/app).
2. Accept the *Share* invitation received, **or** download the `.mrpack` of the latest [release](../../releases) then *+ → Import from file*.

## Develop
- Pack: `pack/` (managed by [packwiz](https://packwiz.infra.link/)).
- Vision: `docs/vision.md` · Mod registry: `docs/mods.md` · Decisions: `docs/decisions/`.
- Bring back the mods tested in the "Fallow test" instance:

```bash
npm run sync -- --instance "Fallow test"
```
````

- [ ] **Step 6: Commit**

```bash
git add docs README.md CHANGELOG.md && git commit -m "docs: add vision, mod registry, decision 0001, README and changelog"
```

---

### Task 5: Sync — reading the pack and the instance

**Files:**
- Create: `scripts/sync/pwtoml.mjs`, `scripts/sync/instance.mjs`, `scripts/test/pwtoml.test.mjs`, `scripts/test/instance.test.mjs`

**Interfaces:**
- Produces:
  - `parsePwToml(text: string) → { name: string, filename: string, side: string, hashFormat: string, hash: string, projectId: string|null, versionId: string|null }`
  - `readPackMods(packDir: string) → Promise<Array<PackMod>>` where `PackMod = parsePwToml(...) & { key: string }` (`key` = file name without `.pw.toml`, used by `packwiz remove`).
  - `resolveInstanceDir(nameOrPath: string, env = process.env) → string`
  - `readInstanceJars(instanceDir: string) → Promise<Array<{ filename: string, sha512: string }>>` (ignores anything that does not end in `.jar`, hence `*.jar.disabled`).

- [ ] **Step 1: Tests (fail)**

`scripts/test/pwtoml.test.mjs`:
```js
import { test } from 'node:test';
import assert from 'node:assert/strict';
import { mkdtemp, mkdir, writeFile } from 'node:fs/promises';
import { tmpdir } from 'node:os';
import { join } from 'node:path';
import { parsePwToml, readPackMods } from '../sync/pwtoml.mjs';

const SAMPLE = `name = "Create"
filename = "create-1.21.1-6.0.6.jar"
side = "both"

[download]
url = "https://cdn.modrinth.com/data/LNytGWDc/versions/abc/create.jar"
hash-format = "sha512"
hash = "deadbeef"

[update]
[update.modrinth]
mod-id = "LNytGWDc"
version = "abc123"
`;

test('parsePwToml extracts fields', () => {
  assert.deepEqual(parsePwToml(SAMPLE), {
    name: 'Create', filename: 'create-1.21.1-6.0.6.jar', side: 'both',
    hashFormat: 'sha512', hash: 'deadbeef', projectId: 'LNytGWDc', versionId: 'abc123',
  });
});

test('parsePwToml without modrinth section returns null ids', () => {
  const r = parsePwToml('name = "X"\nfilename = "x.jar"\nside = "both"\n[download]\nhash-format = "sha1"\nhash = "aa"\n');
  assert.equal(r.projectId, null);
  assert.equal(r.versionId, null);
});

test('readPackMods reads every .pw.toml with its key', async () => {
  const dir = await mkdtemp(join(tmpdir(), 'fallow-pack-'));
  await mkdir(join(dir, 'mods'));
  await writeFile(join(dir, 'mods', 'create.pw.toml'), SAMPLE);
  await writeFile(join(dir, 'mods', 'notes.txt'), 'ignored');
  const mods = await readPackMods(dir);
  assert.equal(mods.length, 1);
  assert.equal(mods[0].key, 'create');
  assert.equal(mods[0].projectId, 'LNytGWDc');
});
```

`scripts/test/instance.test.mjs`:
```js
import { test } from 'node:test';
import assert from 'node:assert/strict';
import { mkdtemp, mkdir, writeFile } from 'node:fs/promises';
import { tmpdir } from 'node:os';
import { join } from 'node:path';
import { createHash } from 'node:crypto';
import { resolveInstanceDir, readInstanceJars } from '../sync/instance.mjs';

test('resolveInstanceDir maps a name to the Modrinth App profiles folder', () => {
  const env = { APPDATA: 'C:\\Users\\Prénom Nom\\AppData\\Roaming' };
  assert.equal(
    resolveInstanceDir('Fallow test', env),
    join('C:\\Users\\Prénom Nom\\AppData\\Roaming', 'ModrinthApp', 'profiles', 'Fallow test'),
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
```

- [ ] **Step 2: Verify the failure**

Run: `npm test`
Expected: FAIL (`Cannot find module '../sync/pwtoml.mjs'`).

- [ ] **Step 3: Implementation**

`scripts/sync/pwtoml.mjs`:
```js
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
```

`scripts/sync/instance.mjs`:
```js
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

export async function readInstanceJars(instanceDir) {
  const modsDir = join(instanceDir, 'mods');
  const files = (await readdir(modsDir)).filter((f) => f.endsWith('.jar')).sort();
  return Promise.all(files.map(async (filename) => ({ filename, sha512: await sha512File(join(modsDir, filename)) })));
}
```

- [ ] **Step 4: Verify success**

Run: `npm test`
Expected: PASS (6 tests).

- [ ] **Step 5: Commit**

```bash
git add scripts package.json && git commit -m "feat(sync): read pack mods and instance jars"
```

---

### Task 6: Sync — Modrinth API and diff

**Files:**
- Create: `scripts/sync/modrinth.mjs`, `scripts/sync/diff.mjs`, `scripts/test/modrinth.test.mjs`, `scripts/test/diff.test.mjs`

**Interfaces:**
- Consumes: `readInstanceJars` → `{filename, sha512}[]`, `readPackMods` → `PackMod[]` (Task 5).
- Produces:
  - `lookupVersions(sha512s: string[], { fetch }) → Promise<Map<sha512, { projectId, versionId, versionNumber }>>` (throws an error if the HTTP response is not 2xx).
  - `lookupProjects(ids: string[], { fetch }) → Promise<Map<id, { slug, title }>>`
  - `diffMods(instanceMods, packMods) → { added: InstMod[], removed: PackMod[], changed: Array<{ from: PackMod, to: InstMod }>, unknown: Array<{filename}>, unchanged: PackMod[] }` where `InstMod = { filename, sha512, projectId, versionId, versionNumber }` and `projectId === null` for a jar unknown to Modrinth.

- [ ] **Step 1: Tests (fail)**

`scripts/test/modrinth.test.mjs`:
```js
import { test } from 'node:test';
import assert from 'node:assert/strict';
import { lookupVersions, lookupProjects } from '../sync/modrinth.mjs';

const fakeFetch = (status, body, calls = []) => async (url, init) => {
  calls.push({ url, init });
  return { ok: status < 300, status, json: async () => body };
};

test('lookupVersions posts sha512 hashes and maps the response', async () => {
  const calls = [];
  const fetch = fakeFetch(200, { h1: { id: 'v1', project_id: 'p1', version_number: '1.0' } }, calls);
  const map = await lookupVersions(['h1', 'h2'], { fetch });
  assert.equal(calls[0].url, 'https://api.modrinth.com/v2/version_files');
  assert.deepEqual(JSON.parse(calls[0].init.body), { hashes: ['h1', 'h2'], algorithm: 'sha512' });
  assert.match(calls[0].init.headers['User-Agent'], /fallow/);
  assert.deepEqual(map.get('h1'), { projectId: 'p1', versionId: 'v1', versionNumber: '1.0' });
  assert.equal(map.has('h2'), false);
});

test('lookupVersions throws on HTTP error', async () => {
  await assert.rejects(lookupVersions(['h1'], { fetch: fakeFetch(503, {}) }), /Modrinth API 503/);
});

test('lookupProjects maps ids to slug/title', async () => {
  const map = await lookupProjects(['p1'], { fetch: fakeFetch(200, [{ id: 'p1', slug: 'create', title: 'Create' }]) });
  assert.deepEqual(map.get('p1'), { slug: 'create', title: 'Create' });
});
```

`scripts/test/diff.test.mjs`:
```js
import { test } from 'node:test';
import assert from 'node:assert/strict';
import { diffMods } from '../sync/diff.mjs';

const pack = (key, projectId, versionId) => ({ key, name: key, projectId, versionId });
const inst = (filename, projectId, versionId) => ({ filename, sha512: filename, projectId, versionId, versionNumber: versionId });

test('classifies added, removed, changed, unchanged and unknown', () => {
  const d = diffMods(
    [inst('create.jar', 'P1', 'v2'), inst('sable.jar', 'P2', 'v1'), inst('new.jar', 'P4', 'v1'), inst('manual.jar', null, null)],
    [pack('create', 'P1', 'v1'), pack('sable', 'P2', 'v1'), pack('old', 'P3', 'v1')],
  );
  assert.deepEqual(d.added.map((m) => m.filename), ['new.jar']);
  assert.deepEqual(d.removed.map((m) => m.key), ['old']);
  assert.equal(d.changed.length, 1);
  assert.equal(d.changed[0].from.key, 'create');
  assert.equal(d.changed[0].to.versionId, 'v2');
  assert.deepEqual(d.unchanged.map((m) => m.key), ['sable']);
  assert.deepEqual(d.unknown.map((m) => m.filename), ['manual.jar']);
});

test('pack mods without modrinth id are never marked removed', () => {
  const d = diffMods([], [pack('local', null, null)]);
  assert.deepEqual(d.removed, []);
});
```

- [ ] **Step 2: Verify the failure**

Run: `npm test`
Expected: FAIL (modules not found).

- [ ] **Step 3: Implementation**

`scripts/sync/modrinth.mjs`:
```js
const API = 'https://api.modrinth.com/v2';
const HEADERS = { 'User-Agent': 'salvagedisc/fallow sync-from-instance', 'Content-Type': 'application/json' };

async function call(fetch, url, init) {
  const res = await fetch(url, { ...init, headers: HEADERS });
  if (!res.ok) throw new Error(`Modrinth API ${res.status} on ${url}`);
  return res.json();
}

export async function lookupVersions(sha512s, { fetch = globalThis.fetch } = {}) {
  const body = await call(fetch, `${API}/version_files`, {
    method: 'POST', body: JSON.stringify({ hashes: sha512s, algorithm: 'sha512' }),
  });
  return new Map(Object.entries(body).map(([hash, v]) => [hash, { projectId: v.project_id, versionId: v.id, versionNumber: v.version_number }]));
}

export async function lookupProjects(ids, { fetch = globalThis.fetch } = {}) {
  if (ids.length === 0) return new Map();
  const body = await call(fetch, `${API}/projects?ids=${encodeURIComponent(JSON.stringify(ids))}`, { method: 'GET' });
  return new Map(body.map((p) => [p.id, { slug: p.slug, title: p.title }]));
}
```

`scripts/sync/diff.mjs`:
```js
export function diffMods(instanceMods, packMods) {
  const packById = new Map(packMods.filter((m) => m.projectId).map((m) => [m.projectId, m]));
  const seen = new Set();
  const result = { added: [], removed: [], changed: [], unknown: [], unchanged: [] };
  for (const mod of instanceMods) {
    if (!mod.projectId) { result.unknown.push(mod); continue; }
    seen.add(mod.projectId);
    const existing = packById.get(mod.projectId);
    if (!existing) result.added.push(mod);
    else if (existing.versionId !== mod.versionId) result.changed.push({ from: existing, to: mod });
    else result.unchanged.push(existing);
  }
  for (const [id, mod] of packById) if (!seen.has(id)) result.removed.push(mod);
  return result;
}
```

- [ ] **Step 4: Verify success**

Run: `npm test`
Expected: PASS (11 tests).

- [ ] **Step 5: Commit**

```bash
git add scripts && git commit -m "feat(sync): identify instance mods via Modrinth API and diff with pack"
```

---

### Task 7: Sync — packwiz commands, configs and CLI

**Files:**
- Create: `scripts/sync/commands.mjs`, `scripts/sync/configs.mjs`, `scripts/sync-from-instance.mjs`, `scripts/test/commands.test.mjs`, `scripts/test/configs.test.mjs`, `scripts/test/cli.test.mjs`

**Interfaces:**
- Consumes: everything above.
- Produces:
  - `toPackwizCommands(diff) → string[][]` (each entry = arguments passed to `packwiz`).
  - `diffConfigs(srcDir, dstDir) → Promise<Array<{ rel: string, status: 'new'|'modified' }>>`
  - `copyConfigs(srcDir, dstDir, entries) → Promise<void>`
  - `run({ argv, env, fetch, exec, confirm, log, packDir }) → Promise<number>` (exit code; all dependencies injectable for tests).

- [ ] **Step 1: Tests (fail)**

`scripts/test/commands.test.mjs`:
```js
import { test } from 'node:test';
import assert from 'node:assert/strict';
import { toPackwizCommands } from '../sync/commands.mjs';

test('builds remove then add commands, pinning exact versions', () => {
  const cmds = toPackwizCommands({
    added: [{ projectId: 'P4', versionId: 'v9' }],
    removed: [{ key: 'old' }],
    changed: [{ from: { key: 'create' }, to: { projectId: 'P1', versionId: 'v2' } }],
    unknown: [{ filename: 'manual.jar' }], unchanged: [],
  });
  assert.deepEqual(cmds, [
    ['remove', 'old'],
    ['remove', 'create'],
    ['-y', 'modrinth', 'add', 'https://modrinth.com/mod/P1/version/v2'],
    ['-y', 'modrinth', 'add', 'https://modrinth.com/mod/P4/version/v9'],
  ]);
});
```

`scripts/test/configs.test.mjs`:
```js
import { test } from 'node:test';
import assert from 'node:assert/strict';
import { mkdtemp, mkdir, writeFile, readFile } from 'node:fs/promises';
import { tmpdir } from 'node:os';
import { join } from 'node:path';
import { diffConfigs, copyConfigs } from '../sync/configs.mjs';

test('detects new and modified config files recursively, then copies them', async () => {
  const src = await mkdtemp(join(tmpdir(), 'cfg-src-'));
  const dst = await mkdtemp(join(tmpdir(), 'cfg-dst-'));
  await mkdir(join(src, 'coldsweat'));
  await writeFile(join(src, 'coldsweat', 'main.toml'), 'a = 2');
  await writeFile(join(src, 'same.toml'), 'x');
  await writeFile(join(src, 'changed.toml'), 'new');
  await writeFile(join(dst, 'same.toml'), 'x');
  await writeFile(join(dst, 'changed.toml'), 'old');
  const entries = await diffConfigs(src, dst);
  assert.deepEqual(entries, [
    { rel: 'changed.toml', status: 'modified' },
    { rel: join('coldsweat', 'main.toml'), status: 'new' },
  ]);
  await copyConfigs(src, dst, entries);
  assert.equal(await readFile(join(dst, 'coldsweat', 'main.toml'), 'utf8'), 'a = 2');
  assert.equal(await readFile(join(dst, 'changed.toml'), 'utf8'), 'new');
});
```

`scripts/test/cli.test.mjs`:
```js
import { test } from 'node:test';
import assert from 'node:assert/strict';
import { mkdtemp, mkdir, writeFile } from 'node:fs/promises';
import { tmpdir } from 'node:os';
import { join } from 'node:path';
import { run } from '../sync-from-instance.mjs';

async function fixture() {
  const root = await mkdtemp(join(tmpdir(), 'fallow-cli-'));
  const packDir = join(root, 'pack');
  const inst = join(root, 'Fallow test');
  await mkdir(join(packDir, 'mods'), { recursive: true });
  await mkdir(join(inst, 'mods'), { recursive: true });
  await writeFile(join(inst, 'mods', 'new.jar'), 'NEW');
  return { packDir, inst };
}

test('network failure aborts before any packwiz command', async () => {
  const { packDir, inst } = await fixture();
  const exec = [];
  const code = await run({
    argv: ['--instance', inst, '--yes'], env: {}, packDir,
    fetch: async () => ({ ok: false, status: 500, json: async () => ({}) }),
    exec: (args) => { exec.push(args); return 0; }, confirm: async () => true, log: () => {},
  });
  assert.equal(code, 1);
  assert.deepEqual(exec, []);
});

test('declined confirmation runs nothing', async () => {
  const { packDir, inst } = await fixture();
  const exec = [];
  const fetch = async (url, init) => ({
    ok: true, status: 200,
    json: async () => url.includes('version_files')
      ? Object.fromEntries(JSON.parse(init.body).hashes.map((h) => [h, { id: 'v1', project_id: 'P9', version_number: '1.0' }]))
      : [{ id: 'P9', slug: 'newmod', title: 'New Mod' }],
  });
  const code = await run({ argv: ['--instance', inst], env: {}, packDir, fetch, exec: (a) => { exec.push(a); return 0; }, confirm: async () => false, log: () => {} });
  assert.equal(code, 0);
  assert.deepEqual(exec, []);
});

test('--dry-run prints commands without running them', async () => {
  const { packDir, inst } = await fixture();
  const exec = [];
  const logs = [];
  const fetch = async (url, init) => ({
    ok: true, status: 200,
    json: async () => url.includes('version_files')
      ? Object.fromEntries(JSON.parse(init.body).hashes.map((h) => [h, { id: 'v1', project_id: 'P9', version_number: '1.0' }]))
      : [{ id: 'P9', slug: 'newmod', title: 'New Mod' }],
  });
  const code = await run({ argv: ['--instance', inst, '--dry-run'], env: {}, packDir, fetch, exec: (a) => { exec.push(a); return 0; }, confirm: async () => true, log: (m) => logs.push(m) });
  assert.equal(code, 0);
  assert.deepEqual(exec, []);
  assert.ok(logs.some((l) => l.includes('+ New Mod')));
  assert.ok(logs.some((l) => l.includes('modrinth add https://modrinth.com/mod/P9/version/v1')));
});
```

- [ ] **Step 2: Verify the failure**

Run: `npm test`
Expected: FAIL (modules not found).

- [ ] **Step 3: Implementation**

`scripts/sync/commands.mjs`:
```js
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
```

`scripts/sync/configs.mjs`:
```js
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
```

`scripts/sync-from-instance.mjs`:
```js
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
  --instance  Nom de l'instance Modrinth App (défaut : "Fallow test") ou chemin absolu
  --configs   Rapatrie aussi les fichiers de config/ modifiés
  --dry-run   Affiche le diff et les commandes sans rien appliquer
  --yes       N'attend pas de confirmation`;

function parseArgs(argv) {
  const opts = { instance: 'Fallow test', configs: false, dryRun: false, yes: false, help: false };
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
  if (commands.length > 0 && exec(['refresh']) !== 0) { log('Échec : packwiz refresh'); return 1; }
  await copyConfigs(join(instanceDir, 'config'), join(packDir, 'config'), configEntries);
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
```


- [ ] **Step 4: Verify success**

Run: `npm test`
Expected: PASS (16 tests).

- [ ] **Step 5: Real test against the "Fallow test" instance (Task 3)**

Ask the maintainer to add any mod (e.g. `Create: Copycats+`) to the instance via the Modrinth App.
Run: `npm run sync -- --instance "Fallow test" --dry-run`
Expected: a line `+ Create: Copycats+ …`, `= 12+ mod(s) inchangé(s)` (+ dependencies), no `-`. Then without `--dry-run`, answer `o` and check that `pack/mods/copycats.pw.toml` appears. If `packwiz modrinth add <version url>` is not accepted by the pinned version, replace the URL with `--project-id <P> --version-id <V>` in `commands.mjs` and its test. **Then undo this test mod** (`git checkout -- pack && git clean -fd pack`): it is not retained.

- [ ] **Step 6: Commit**

```bash
git add scripts && git commit -m "feat(sync): add sync-from-instance CLI with packwiz commands and config copy"
```

---

### Task 8: CI `build.yml`

**Files:**
- Create: `.github/workflows/build.yml`

**Interfaces:**
- Produces: artifact `Fallow-<version>-<sha7>` containing the `.mrpack`.

- [ ] **Step 1: Workflow**

```yaml
name: build

on:
  push:
    branches: ['**']
  pull_request:

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4

      - uses: actions/setup-node@v4
        with:
          node-version: 22
      - run: npm test

      - uses: actions/setup-go@v5
        with:
          go-version: stable
      - name: Install packwiz
        run: go install github.com/packwiz/packwiz@ef87d964f8cbd52b3b13ea42453ef322290e2b9e

      - name: Check index is up to date
        working-directory: pack
        run: |
          packwiz refresh
          git diff --exit-code -- . || { echo "::error::index.toml pas à jour : lancer 'packwiz refresh' dans pack/"; exit 1; }

      - name: Export .mrpack
        id: export
        working-directory: pack
        run: |
          version=$(sed -n 's/^version = "\(.*\)"/\1/p' pack.toml)
          name="Fallow-${version}-${GITHUB_SHA::7}"
          mkdir -p ../dist
          packwiz mr export -o "../dist/${name}.mrpack"
          echo "name=${name}" >> "$GITHUB_OUTPUT"

      - uses: actions/upload-artifact@v4
        with:
          name: ${{ steps.export.outputs.name }}
          path: dist/*.mrpack
```

- [ ] **Step 2: Push and verify**

```bash
git add .github && git commit -m "ci: build and export .mrpack on every push"
git push
gh run watch --exit-status
```
Expected: green run; `gh run view --log` shows `npm test` OK and the artifact `Fallow-0.1.0-xxxxxxx` is listed by `gh run view`.

---

### Task 9: CI `release.yml` and release v0.1.0

**Files:**
- Create: `.github/workflows/release.yml`

**Interfaces:**
- Consumes: `CHANGELOG.md` (section `## [X.Y.Z]`), `pack/pack.toml` (`version`).

- [ ] **Step 1: Workflow**

```yaml
name: release

on:
  push:
    tags: ['v*']

permissions:
  contents: write

jobs:
  release:
    runs-on: ubuntu-latest
    env:
      MODRINTH_TOKEN: ${{ secrets.MODRINTH_TOKEN }}
      MODRINTH_PROJECT_ID: ${{ vars.MODRINTH_PROJECT_ID }}
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-go@v5
        with:
          go-version: stable
      - run: go install github.com/packwiz/packwiz@ef87d964f8cbd52b3b13ea42453ef322290e2b9e

      - name: Check tag matches pack version
        id: meta
        run: |
          version=$(sed -n 's/^version = "\(.*\)"/\1/p' pack/pack.toml)
          [ "v${version}" = "${GITHUB_REF_NAME}" ] || { echo "::error::tag ${GITHUB_REF_NAME} ≠ pack.toml ${version}"; exit 1; }
          echo "version=${version}" >> "$GITHUB_OUTPUT"
          awk -v v="${version}" '$0 ~ "^## \\[" v "\\]" {f=1; next} /^## \[/ {f=0} f' CHANGELOG.md > notes.md
          [ -s notes.md ] || { echo "::error::section [${version}] absente du CHANGELOG"; exit 1; }

      - name: Export .mrpack
        working-directory: pack
        run: |
          mkdir -p ../dist
          packwiz mr export -o "../dist/Fallow-${{ steps.meta.outputs.version }}.mrpack"

      - name: GitHub release
        env:
          GH_TOKEN: ${{ github.token }}
        run: gh release create "${GITHUB_REF_NAME}" dist/*.mrpack --title "Fallow ${{ steps.meta.outputs.version }}" --notes-file notes.md

      # Disabled until the Modrinth project exists: define the variable MODRINTH_PROJECT_ID
      # and the secret MODRINTH_TOKEN (created by the maintainer) to enable it.
      # Before enabling, check the expected fields for a modpack: https://docs.modrinth.com/api/operations/createversion/
      - name: Publish to Modrinth
        if: env.MODRINTH_TOKEN != '' && env.MODRINTH_PROJECT_ID != ''
        run: |
          version="${{ steps.meta.outputs.version }}"
          case "$version" in 0.*) type=beta ;; *) type=release ;; esac
          data=$(jq -n --arg p "$MODRINTH_PROJECT_ID" --arg v "$version" --arg t "$type" --rawfile c notes.md \
            '{project_id:$p, name:("Fallow " + $v), version_number:$v, changelog:$c, version_type:$t,
              game_versions:["1.21.1"], loaders:["neoforge"], dependencies:[], featured:true, file_parts:["file"]}')
          curl --fail-with-body -sS -X POST https://api.modrinth.com/v2/version \
            -H "Authorization: ${MODRINTH_TOKEN}" \
            -H "User-Agent: salvagedisc/fallow release" \
            -F "data=${data}" \
            -F "file=@dist/Fallow-${version}.mrpack;type=application/x-modrinth-modpack+zip"
```

- [ ] **Step 2: Commit and push**

```bash
git add .github && git commit -m "ci: release workflow (GitHub release + optional Modrinth publish)"
git push && gh run watch --exit-status
```

- [ ] **Step 3: Tag v0.1.0 — only if the launch test of Task 3 succeeded**

Ask the maintainer for confirmation, then:
```bash
git tag -a v0.1.0 -m "Fallow 0.1.0 — pack de base"
git push origin v0.1.0
gh run watch --exit-status
gh release view v0.1.0
```
Expected: release `Fallow 0.1.0` with `Fallow-0.1.0.mrpack` and the CHANGELOG notes; the "Publish to Modrinth" step appears **skipped**.

- [ ] **Step 4: Update the CHANGELOG**

Check that `## [Unreleased]` is indeed present at the top (already the case); nothing to commit if unchanged.
