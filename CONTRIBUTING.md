# Contributing to Fallow

Project guide: what the repository is, how to work in it, what lives outside it. It applies to everyone who contributes.

## Purpose

Fallow is a Minecraft **1.21.1 / NeoForge 21.1.265** modpack built with packwiz: a permanent, almost empty world in the spirit of the No Man's Land trailer (the mod itself left the pack on 2026-11-13), where the mist lifts wherever life is brought back ([vision](docs/vision.md)). The repository is the **source of truth** for the pack: mod list, configs, scripts, the custom `fallow` mod, its resource packs and the decisions behind them.

## Principles

- **Stability before novelty**: versions are pinned and updated one by one, never with `packwiz update --all`.
- **Every mod comes from Modrinth** and exists for Minecraft 1.21.1 / NeoForge. An unavailable mod is flagged, never replaced by a third-party source.
- **Only `pack/` ends up in the `.mrpack`**: docs, scripts and CI never go into it.

## Stack and commands

Requirements: Node.js ≥ 20, [packwiz](https://packwiz.infra.link/installation/), JDK 21 for the custom mod, Python 3 + Pillow for the resource pack scripts. The Node.js tooling has no dependencies (no `npm install`).

| Command | Purpose | CI |
|---|---|---|
| `npm test` | Tooling tests (`scripts/test/*.test.mjs`) | yes |
| `npm run check-deps` | Dependencies declared in the jars are provided by the pack (Modrinth does not always declare them) | yes |
| `npm run check-fr` | Every text of every mod has a French translation and keeps its variables ([translation](docs/plan/traduction.md)) | yes |
| `npm run sync -- --instance "Fallow test"` | Compares the Modrinth App test instance with the pack and applies the packwiz commands after confirmation (`--configs`, `--dry-run`) | no |
| `cd pack && packwiz refresh` | Regenerates `index.toml` after any change to `pack/` (CI fails if it is stale) | yes |
| `cd pack && packwiz mr export` | Builds the `.mrpack` locally | yes |
| `cd mods-custom/fallow && ./gradlew build` | Builds the custom `fallow` mod; copy the jar to `pack/mods/` and bump `mod_version` in `gradle.properties` | yes |

Manual scripts, run from the repository root and committed with their output (never run in CI):

| Script | Output |
|---|---|
| `python3 scripts/ambiance/weather.py` | Rain, rain curtain and glint textures (`fallow-ambiance`, `fallow` mod) |
| `python3 scripts/ambiance/palette.py <neoforge client resources jar>` | Olive grass and foliage colour maps; the jar is in `mods-custom/fallow/build/moddev/artifacts/` |
| `python3 scripts/ambiance/biomes.py <regions-unexplored jar>` | Mist profiles of the Regions Unexplored biomes in the ATMOSPHERICS preset, Polytone modifiers `ru_<biome>.json`, misty biomes of the `fallow:brumeux` tag; rerun after each update of the mod ([ambiance](docs/plan/fallow-ambiance.md)) |
| `python3 scripts/ambiance/biome_rules.py` | Biome tags of other mods (horse breeds, Naturalist animals, pigeons, fish) and Where Winds Blow profiles extended to the Regions Unexplored biomes, from the same table as `biomes.py`; reads the jar cache filled by `npm run check-deps`; rerun after adding a mod with biome rules or updating Regions Unexplored |
| `python3 scripts/interface/build.py` | The `fallow-interface` pack from GUI Revision (calls `menus.py`, which can also run alone) |
| `python3 scripts/textures/build.py` | The `fallow-textures` pack from Vanilla Evolved |

## Layout

| Content | Location |
|---|---|
| Mod (packwiz metadata) | `pack/mods/<slug>.pw.toml` |
| Shared config of a mod / config applied to every new world | `pack/config/` / `pack/defaultconfigs/` |
| KubeJS scripts: registries and items / recipes, rules, spawns / display, EMI, tooltips | `pack/kubejs/startup_scripts/`, `server_scripts/`, `client_scripts/` |
| French translation of a mod (language files, books) | `pack/kubejs/assets/<namespace>/` |
| Datapack data (worldgen, loot tables, tags, structures) | `pack/kubejs/data/fallow/` (or `data/<namespace>/` to override a mod) |
| In-house resource packs | `pack/resourcepacks/fallow-ambiance/`, `fallow-interface/`, `fallow-textures/` |
| Custom `fallow` mod (sources) | `mods-custom/fallow/` ([mod-fallow](docs/plan/mod-fallow.md)) |
| Tooling and its tests / resource pack scripts | `scripts/` + `scripts/test/` / `scripts/ambiance/`, `scripts/interface/`, `scripts/textures/` |
| Vision, registry, roadmap, in-game tests | `docs/vision.md`, `docs/mods.md`, `docs/feuille-de-route.md`, `docs/tests-en-jeu.md` |
| Living page of a system (mod, pack, translation, quests…) | `docs/plan/` |
| Settled decision (comparison, choice) | `docs/decisions/NNNN-subject.md` |
| Frozen plans, research notes, unused drafts | `docs/archive/` (read-only history) |

Datapacks go through KubeJS: NeoForge does not load a global datapack without an additional mod, whereas `kubejs/data/` is active in every world.

## Conventions

- Docs, commit messages and code identifiers in **English**; in-game text in **French** (the language of each translation file). KubeJS script comments and Fallow's own texts are in French.
- Documentation file names may stay in French (`feuille-de-route.md`, `tannerie.md`…): they name the systems as the team says them in game. New files follow the same rule; do not rename existing ones, links depend on them.
- Never commit local tool or editor configuration, or personal data (names, e-mail addresses, local paths); list local-only files in `.git/info/exclude`.

**Adding, updating or removing a mod**

1. Check its availability on Modrinth for 1.21.1 NeoForge.
2. Run it through the **5-question filter** ([vision](docs/vision.md)): does it change the gameplay? does it fit in? does it hold up for 100 hours? does it bring a mechanic? does it avoid overlaps?
3. Add it: `cd pack && packwiz mr add <slug>` (or `npm run sync`), then check its side (`client` / `server` / `both`).
4. Run `npm run check-deps`, then `npm run check-fr`: all of the mod's text must be in French.
5. Update [docs/mods.md](docs/mods.md) and, if a comparison took place, create a record in [docs/decisions/](docs/decisions/README.md).
6. Add a line to `CHANGELOG.md` (`[Unreleased]` section).
7. **One commit per mod**: `feat(mods): add <slug>`, `chore(mods): update <slug> x → y`, `feat(mods)!: remove <slug>`. Other prefixes: `feat(config)`, `feat(kubejs)`, `feat(sync)`, `docs`, `ci`, `chore`.

**Branches and PRs**

- `main` is **always playable**; the `vX.Y.Z` tags are placed on it. Every push to `main` refreshes the `dev` pre-release ([dev-sync](docs/dev-sync.md)).
- Work happens on descriptive branches (`feat/…`, `fix/…`, `docs/…`; experiments on `test/<subject>`), merged into `main` by PR or abandoned. Merged branches are deleted automatically.
- Every push and every PR runs `build`: tests, dependency and translation checks, up-to-date index, custom mod build, `.mrpack` export as an artifact. A PR is merged only when it is green.
- Two PRs that touch `pack/index.toml` conflict only on `pack/pack.toml`, whose `hash` is the sha256 of `pack/index.toml`: run `packwiz refresh` after rebasing.

**Versions and release**

- `0.x`: prototype, disposable worlds. `1.0.0`: launch of the permanent world. After `1.0.0`, any change that removes blocks or items or modifies worldgen is a **major version** and requires a world backup before updating.
- To publish: move the `[Unreleased]` section of `CHANGELOG.md` under `[X.Y.Z]`, set the same version in `pack/pack.toml`, run `packwiz refresh`, then place the `vX.Y.Z` tag on `main`. The `release` workflow checks that they match, exports the `.mrpack` and creates the GitHub release. Publication to Modrinth stays disabled until `MODRINTH_PROJECT_ID` (variable) and `MODRINTH_TOKEN` (secret, entered by the owner) exist. The interface pack is CC BY-NC-SA, so the Modrinth project must keep monetisation off.

**Test instance**

In-game testing is done in a **"Fallow test"** instance of the Modrinth App. `npm run sync` never modifies the instance. In the other direction: `packwiz mr export` (or the `dev` pre-release), then import the `.mrpack` into the Modrinth App.

## Outside Git

Ignored by `.gitignore` and never committed: `dist/` and `*.mrpack` (local exports; releases and the `dev` pre-release hold the built packs), `mods-custom/fallow/build/`, `mods-custom/fallow/run/` (Gradle dev client and server, with their worlds), `.gradle/`, `node_modules/`, `.cache/` (downloads of the resource pack scripts), `__pycache__/`. The only build output that is committed is the `fallow` jar in `pack/mods/`. Secrets live in the GitHub repository settings, never in files.

## Current state

As of 2026-11-13: pack `0.1.0` unreleased (`[Unreleased]` in the changelog), 156 Modrinth mods plus the custom `fallow` mod 0.7.0, decisions 0001 to 0019, CI green on `main`. No Man's Land left the pack ([0019](docs/decisions/0019-leaving-no-mans-land.md)). Geophilic and Regions Unexplored have not been seen in game. The pack starts on a dedicated server but **nobody has played it yet**: next step is the eight play sessions of [tests-en-jeu.md](docs/tests-en-jeu.md), then the `v0.1.0` tag. Open work is tracked in the [roadmap](docs/feuille-de-route.md).
