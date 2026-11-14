# Compline — Repo & tooling (sub-project 1)

- **Date**: 2026-09-23
- **Status**: under review
- **Repo**: `salvagedisc/compline` (GitHub, private)

## 1. Context

Compline is a Minecraft modpack for a permanent world played among friends on a rented dedicated server. Direction: exploration (Breath of the Wild style), Create + Aeronautics, settlement building, long-term farming and animal husbandry, a misty, solitary and strange atmosphere.

Fixed constraints:

- Minecraft **1.21.1** + **NeoForge** (required by Create Aeronautics).
- **All mods must exist on Modrinth** for 1.21.1 NeoForge.
- **Pinned** versions: the stability of the permanent world takes priority over novelty.

The overall project is split into sub-projects, each with its own spec → plan → implementation cycle:

1. **Repo & tooling** — *this document*.
2. Choice of systems (farming, fauna, structures, atmosphere…).
3. Rules layer (KubeJS + datapack data).
4. Server (host, deployment, backups).

## 2. Goal

Have a repo that is the pack's **source of truth**: a versioned, readable mod list, configs, scripts, documentation of decisions — and that automatically produces a `.mrpack` installable in the Modrinth App, then publishable on Modrinth.

### Success criteria

- The private repo exists and contains the structure described in §4.
- `packwiz` manages the pack; `pack.toml` pins Minecraft 1.21.1 and a precise NeoForge version.
- The base pack contains the **retained** mods (§6) and launches in the Modrinth App (a world can be created without a crash).
- A push to GitHub produces a `.mrpack` downloadable from the Actions tab.
- A `vX.Y.Z` tag creates a GitHub release containing the `.mrpack`.
- The sync script detects the mods added/removed in the test instance and applies them to the pack after confirmation.
- `docs/vision.md` and `docs/mods.md` reflect the current state of the decisions.

### Out of scope

- Choosing new mods (sub-project 2).
- Gameplay KubeJS scripts and datapack data (sub-project 3) — only the directory tree is created.
- Host, server deployment, backups (sub-project 4).
- Actual publication on Modrinth (requires moderation approval and a token supplied by the maintainer) — the workflow is ready but disabled.

## 3. Tool: packwiz

Chosen over two alternatives:

| Option | Verdict |
|---|---|
| **packwiz** — one TOML file per mod, native `.mrpack` export | ✅ Retained: the repo really is the source of truth, readable diffs, standard, CI-compatible |
| Modrinth App as source + unpacked export in the repo | ❌ The repo becomes an archive, overwhelming exports, no editing from the repo |
| Home-made script on the Modrinth API | ❌ Reimplements packwiz, needless maintenance |

Local installation: official binary (CI artifact of the `packwiz/packwiz` repo) or `go install`. Any download is subject to the maintainer's approval. In CI: installation via `go install` on a pinned commit.

## 4. Repo structure

```
compline/
├── pack/                     ← packwiz root, the only content of the .mrpack
│   ├── pack.toml             ← name, version, MC 1.21.1, pinned NeoForge
│   ├── index.toml            ← generated (packwiz refresh)
│   ├── mods/                 ← one .pw.toml per mod
│   ├── config/               ← mod configs
│   ├── defaultconfigs/       ← per-world configs
│   └── kubejs/
│       ├── startup_scripts/
│       ├── server_scripts/
│       ├── client_scripts/
│       └── data/compline/      ← "datapack" data loaded by KubeJS
├── scripts/
│   └── sync-from-instance    ← bridge Modrinth App instance → repo
├── docs/
│   ├── vision.md             ← philosophy of the pack
│   ├── mods.md               ← mod registry (status + rationale)
│   ├── decisions/            ← one record per decision made
│   └── conception/specs/      ← design specs
├── .github/workflows/
│   ├── build.yml
│   └── release.yml
├── README.md                 ← installation for players
└── CHANGELOG.md
```

Decisions:

- **Pack in `pack/`**: docs and CI never end up in the `.mrpack` (`.packwizignore` as a complement).
- **Datapacks via `kubejs/data/`**: NeoForge does not load global datapacks without an extra mod; KubeJS (already retained) loads this folder as an active datapack on all worlds.
- **Only retained mods are in `pack/mods/`**; candidates are listed in `docs/mods.md` and tested in the instance or on a `test/*` branch.

## 5. Workflow

### Versions

- `0.x`: prototype, disposable worlds.
- `1.0.0`: launch of the permanent world.
- After `1.0.0`, any change that removes blocks/items or modifies worldgen = **major version** + mandatory world backup before updating.
- Mod updates **one by one**, never `packwiz update --all`.

### Branches

- `main`: always playable; the `vX.Y.Z` tags are placed on it.
- `test/<topic>`: experiments (e.g. `test/serene-seasons`), merged or abandoned.
- No mandatory PR as long as the project is run by one person.

### Adding a mod

1. Check its Modrinth 1.21.1 NeoForge availability.
2. Run it through the 5-question filter (does it change gameplay? does it integrate? does it last 100+ h? does it add a mechanic? does it avoid duplicates?).
3. Add it (`packwiz mr add` or the sync script) and define its side (client / server / both).
4. Update `docs/mods.md`; create a `docs/decisions/` record if a comparison took place.
5. One commit per mod: `feat(mods): add <Name>` / `chore(mods): update <Name> x → y` / `feat(mods)!: remove <Name>`.

### Test instance and synchronisation

The maintainer experiments in a **"Compline – test"** instance of the Modrinth App (free addition/removal of mods).

`scripts/sync-from-instance`:

1. Locates the instance folder (`%APPDATA%/ModrinthApp/profiles/<name>/`), configurable path.
2. Computes the SHA-1 of each `.jar` in `mods/`.
3. Queries the Modrinth API (`POST /v2/version_files`) to identify project + version.
4. Compares with `pack/mods/*.pw.toml` and displays the diff (additions, removals, version changes, jars unknown to Modrinth flagged as non-compliant).
5. After confirmation, runs the corresponding `packwiz mr add` / `packwiz remove`.
6. `--configs` option: copies the modified `config/` files from the instance to `pack/config/` (diff displayed before copying).

The script never modifies the instance. Implemented in Node.js (already installed on the machine), without external dependencies if possible.

Reverse direction: `packwiz mr export` → import the `.mrpack` into the Modrinth App.

Test among friends in the `0.x` phase: `.mrpack` imported into the Modrinth App then shared via the instance's **Share** tab.

## 6. Contents of the base pack

Mods **retained** at the end of the preliminary design (to be checked on Modrinth for 1.21.1 NeoForge at the time of adding; any unavailable mod is flagged and not added):

| Domain | Mod |
|---|---|
| Worldgen | No Man's Land |
| Crafting | Overgeared |
| Industry | Create |
| Aviation | Create Aeronautics + Sable |
| Rail | Create: Steam 'n' Rails |
| Cooking | Kaleidoscope Cookery |
| Map | Antique Atlas 4 + Surveyor |
| Temperature | Cold Sweat |
| Thirst | Thirst Was Reclaimed |
| Scripts | KubeJS |

Plus the dependencies required by these mods. Optimisation mods (client and server) are a choice for sub-project 2; they are not added here.

`docs/mods.md` also lists the mods **to test** and **rejected** from the preliminary design, with their rationale.

## 7. Automation

### `build.yml` — on push and PR

1. Installs packwiz (pinned commit).
2. Checks that `index.toml` is up to date (`packwiz refresh` produces no diff).
3. `packwiz mr export` → `Compline-<version>-<sha>.mrpack`.
4. Publishes the file as a run artifact.

### `release.yml` — on `v*` tag

1. Checks that the tag version matches `pack.toml`.
2. Exports the `.mrpack`.
3. Creates a GitHub release with the `.mrpack` and the matching section of `CHANGELOG.md`.
4. **Modrinth step (disabled)**: publishes the version to the Modrinth project (Unlisted) via the API. Enabled only if the `MODRINTH_PROJECT_ID` variable and the `MODRINTH_TOKEN` secret exist. The token is created and entered by the maintainer.

### Server pack

The `.mrpack` carries the client/server information of each mod. Since the repo is private, `packwiz-installer` (which reads the pack by URL) cannot be used; the server will be installed from the `.mrpack` (host's tool or `mrpack-install`). Details: sub-project 4.

## 8. Documentation

- `docs/vision.md`: pitch, art direction (mist, solitude, strangeness without horror), three pillars (Exploration / Civilisation / Daily life), game loop, prohibitions (minimap, teleportation, vanilla hostiles, End/Elytra, hordes, stacking addons), working rules (real comparisons, 5-question filter).
- `docs/mods.md`: table per domain — mod, status (✅ retained / 🟡 to test / ❌ rejected), side, role, rationale.
- `docs/decisions/`: first record `0001-nml-vs-terralith.md` summarising the comparison already made (NML retained, Terralith + Streams Reflowing as a competing preset to test).
- `README.md`: short presentation and installation (`.mrpack` import / Share link).
- `CHANGELOG.md`: Keep a Changelog format.

## 9. Risks

| Risk | Mitigation |
|---|---|
| A retained mod has no 1.21.1 NeoForge version on Modrinth | Flagged, not added, decision deferred to sub-project 2 |
| Conflict/crash at launch of the base pack | Mandatory launch test before tag `v0.1.0`; mod-by-mod bisection |
| Instance jar absent from Modrinth | The script flags it as non-compliant instead of ignoring it |
| Update breaking the permanent world | Major-version rule + backup (§5) |
| Private repo incompatible with packwiz-installer | Server installation via `.mrpack` (§7) |
