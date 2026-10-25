# Fallow — Repository & tooling (sub-project 1)

- **Date**: 2026-09-23
- **Status**: under review
- **Repo**: `salvagedisc/fallow` (GitHub, private)

## 1. Context

Fallow is a Minecraft modpack for a permanent world played with friends on a rented dedicated server. Direction: exploration (Breath of the Wild style), Create + Aeronautics, settlement building, long-term farming/animal husbandry, a misty, solitary and strange atmosphere.

Fixed constraints:

- Minecraft **1.21.1** + **NeoForge** (imposed by Create Aeronautics).
- **All mods must exist on Modrinth** for 1.21.1 NeoForge.
- **Pinned** versions: the stability of the permanent world takes precedence over novelty.

The overall project is split into sub-projects, each with its own spec → plan → implementation cycle:

1. **Repository & tooling** — *this document*.
2. System choices (farming, fauna, structures, atmosphere…).
3. Rules layer (KubeJS + datapack data).
4. Server (host, deployment, backups).

## 2. Objective

Have a repository that is the **source of truth** for the pack: a versioned and readable mod list, configs, scripts, documentation of the decisions — and that automatically produces a `.mrpack` installable in the Modrinth App, then publishable on Modrinth.

### Success criteria

- The private repository exists and contains the structure described in §4.
- `packwiz` manages the pack; `pack.toml` pins Minecraft 1.21.1 and a specific NeoForge version.
- The base pack contains the **kept** mods (§6) and launches in the Modrinth App (creating a world without a crash).
- A push to GitHub produces a `.mrpack` downloadable from the Actions tab.
- A `vX.Y.Z` tag creates a GitHub release containing the `.mrpack`.
- The sync script detects the mods added/removed in the test instance and applies them to the pack after confirmation.
- `docs/vision.md` and `docs/mods.md` reflect the current state of the decisions.

### Out of scope

- Choosing new mods (sub-project 2).
- Gameplay KubeJS scripts and datapack data (sub-project 3) — only the directory tree is created.
- Host, server deployment, backups (sub-project 4).
- Actual publication on Modrinth (requires moderation approval and a token provided by the maintainer) — the workflow is ready but disabled.

## 3. Tool: packwiz

Chosen over two alternatives:

| Option | Verdict |
|---|---|
| **packwiz** — one TOML file per mod, native `.mrpack` export | ✅ Kept: the repository is truly the source of truth, readable diffs, standard, CI-compatible |
| Modrinth App as source + unzipped export into the repository | ❌ The repository becomes an archive, overwriting exports, no editing from the repository |
| Home-made script on the Modrinth API | ❌ Reimplements packwiz, pointless maintenance |

Local installation: official binary (CI artifact of the `packwiz/packwiz` repository) or `go install`. Any download is subject to the maintainer's approval. In CI: installation via `go install` on a pinned commit.

## 4. Repository structure

```
fallow/
├── pack/                     ← packwiz root, only content of the .mrpack
│   ├── pack.toml             ← name, version, MC 1.21.1, NeoForge pinned
│   ├── index.toml            ← generated (packwiz refresh)
│   ├── mods/                 ← one .pw.toml per mod
│   ├── config/               ← mod configs
│   ├── defaultconfigs/       ← per-world configs
│   └── kubejs/
│       ├── startup_scripts/
│       ├── server_scripts/
│       ├── client_scripts/
│       └── data/fallow/      ← "datapack" data loaded by KubeJS
├── scripts/
│   └── sync-from-instance    ← bridge Modrinth App instance → repository
├── docs/
│   ├── vision.md             ← philosophy of the pack
│   ├── mods.md               ← mod registry (status + justification)
│   ├── decisions/            ← one record per settled decision
├── .github/workflows/
│   ├── build.yml
│   └── release.yml
├── README.md                 ← installation for players
└── CHANGELOG.md
```

Decisions:

- **Pack in `pack/`**: docs and CI never end up in the `.mrpack` (`.packwizignore` as a complement).
- **Datapacks via `kubejs/data/`**: NeoForge does not load global datapacks without an additional mod; KubeJS (already kept) loads this folder as a datapack active in every world.
- **Only kept mods are in `pack/mods/`**; candidates are listed in `docs/mods.md` and tested in the instance or on a `test/*` branch.

## 5. Workflow

### Versions

- `0.x`: prototype, disposable worlds.
- `1.0.0`: launch of the permanent world.
- After `1.0.0`, any change that removes blocks/items or modifies worldgen = **major version** + mandatory world backup before updating.
- Mod updates **one by one**, never `packwiz update --all`.

### Branches

- `main`: always playable; the `vX.Y.Z` tags are placed on it.
- `test/<subject>`: experiments (e.g. `test/serene-seasons`), merged or abandoned.
- No mandatory PR as long as the project is run by a single person.

### Adding a mod

1. Check its availability on Modrinth 1.21.1 NeoForge.
2. Run it through the 5-question filter (does it change the gameplay? does it fit in? does it hold up for 100+ hours? does it bring a mechanic? does it avoid overlaps?).
3. Add it (`packwiz mr add` or the sync script) and set its side (client / server / both).
4. Update `docs/mods.md`; create a `docs/decisions/` record if a comparison took place.
5. One commit per mod: `feat(mods): add <Name>` / `chore(mods): update <Name> x → y` / `feat(mods)!: remove <Name>`.

### Test instance and synchronisation

The maintainer experiments in a **"Fallow – test"** instance of the Modrinth App (free adding/removal of mods).

`scripts/sync-from-instance`:

1. Locates the instance folder (`%APPDATA%/ModrinthApp/profiles/<name>/`), path configurable.
2. Computes the SHA-1 of each `.jar` in `mods/`.
3. Queries the Modrinth API (`POST /v2/version_files`) to identify project + version.
4. Compares with `pack/mods/*.pw.toml` and prints the diff (additions, removals, version changes, jars unknown to Modrinth flagged as non-compliant).
5. After confirmation, runs the corresponding `packwiz mr add` / `packwiz remove` commands.
6. `--configs` option: copies the modified `config/` files from the instance to `pack/config/` (diff shown before copying).

The script never modifies the instance. Implemented in Node.js (already installed on the machine), with no external dependency if possible.

In the other direction: `packwiz mr export` → import of the `.mrpack` into the Modrinth App.

Testing among friends during the `0.x` phase: `.mrpack` imported into the Modrinth App then shared via the instance's **Share** tab.

## 6. Base pack contents

Mods **kept** after the preliminary design (to be checked on Modrinth for 1.21.1 NeoForge at the time of adding; any unavailable mod is flagged and not added):

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

`docs/mods.md` also lists the mods **to test** and **rejected** from the preliminary design, with their justification.

## 7. Automation

### `build.yml` — on push and PR

1. Installs packwiz (pinned commit).
2. Checks that `index.toml` is up to date (`packwiz refresh` produces no diff).
3. `packwiz mr export` → `Fallow-<version>-<sha>.mrpack`.
4. Publishes the file as a run artifact.

### `release.yml` — on `v*` tag

1. Checks that the tag version matches `pack.toml`.
2. Exports the `.mrpack`.
3. Creates a GitHub release with the `.mrpack` and the corresponding section of `CHANGELOG.md`.
4. **Modrinth step (disabled)**: publishes the version to the Modrinth project (Unlisted) via the API. Enabled only if the variable `MODRINTH_PROJECT_ID` and the secret `MODRINTH_TOKEN` exist. The token is created and entered by the maintainer.

### Server pack

The `.mrpack` carries the client/server information of each mod. As the repository is private, `packwiz-installer` (which reads the pack by URL) cannot be used; the server will be installed from the `.mrpack` (host's tool or `mrpack-install`). Details: sub-project 4.

## 8. Documentation

- `docs/vision.md`: pitch, art direction (mist, solitude, strangeness without horror), three pillars (Exploration / Civilisation / Daily life), game loop, prohibitions (minimap, teleportation, vanilla hostiles, End/Elytra, hordes, addon pile-up), working rules (real comparisons, 5-question filter).
- `docs/mods.md`: table per domain — mod, status (✅ kept / 🟡 to test / ❌ rejected), side, role, justification.
- `docs/decisions/`: first record `0001-nml-vs-terralith.md` summarising the comparison already made (NML kept, Terralith + Streams Reflowing as a competing preset to test).
- `README.md`: short presentation and installation (import of the `.mrpack` / Share link).
- `CHANGELOG.md`: Keep a Changelog format.

## 9. Risks

| Risk | Mitigation |
|---|---|
| A kept mod has no 1.21.1 NeoForge version on Modrinth | Flagged, not added, decision postponed to sub-project 2 |
| Conflict/crash at launch of the base pack | Mandatory launch test before the `v0.1.0` tag; mod-by-mod bisection |
| Instance jar absent from Modrinth | The script flags it as non-compliant instead of ignoring it |
| Update breaking the permanent world | Major version rule + backup (§5) |
| Private repository incompatible with packwiz-installer | Server installation via `.mrpack` (§7) |
