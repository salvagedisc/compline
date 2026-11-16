# Contributing to Compline

Project guide: what the repository is, how to work in it, what lives outside it. It applies to everyone who contributes.

## Purpose

Compline is a Minecraft **1.21.1 / NeoForge 21.1.265** modpack built with packwiz: a permanent, almost empty world in the spirit of the No Man's Land trailer (the mod itself left the pack on 2026-11-13), where the mist lifts wherever life is brought back ([vision](docs/vision.md)). The repository is the **source of truth** for the pack: mod list, configs, scripts, the custom `compline` mod, its resource packs and the decisions behind them.

## Principles

- **Stability before novelty**: versions are pinned and updated one by one, never with `packwiz update --all`.
- **Every mod comes from Modrinth** and exists for Minecraft 1.21.1 / NeoForge. An unavailable mod is flagged, never replaced by a third-party source. Exceptions are jars committed in `pack/mods/` with a recorded decision (see Outside Git).
- **Only `pack/` ends up in the `.mrpack`**: docs, scripts and CI never go into it.

## Stack and commands

Requirements: Node.js ≥ 20, [packwiz](https://packwiz.infra.link/installation/), JDK 21 for the custom mod, Python 3 + Pillow for the resource pack scripts. The Node.js tooling has no dependencies (no `npm install`).

| Command | Purpose | CI |
|---|---|---|
| `npm test` | Tooling tests (`scripts/test/*.test.mjs`) | yes |
| `npm run check-deps` | Dependencies declared in the jars are provided by the pack (Modrinth does not always declare them) | yes |
| `npm run check-fr` | Every text of every mod has a French translation and keeps its variables ([translation](docs/plan/traduction.md)) | yes |
| `npm run gen-creative` | Regenerates `pack/kubejs/startup_scripts/onglets_creatifs.js` (removed items out of the creative tabs) after a change to a removal list of the KubeJS server scripts | checked by `npm test` |
| `npm run sync -- --instance "Compline test"` | Compares the Modrinth App test instance with the pack and applies the packwiz commands after confirmation (`--configs`, `--dry-run`) | no |
| `cd pack && packwiz refresh` | Regenerates `index.toml` after any change to `pack/` (CI fails if it is stale) | yes |
| `cd pack && packwiz mr export` | Builds the `.mrpack` locally | yes |
| `cd mods-custom/compline && ./gradlew build` | Builds the custom `compline` mod; copy the jar to `pack/mods/` and bump `mod_version` in `gradle.properties` | yes |

Manual scripts, run from the repository root and committed with their output (never run in CI):

| Script | Output |
|---|---|
| `python3 scripts/biomes/biome_rules.py` | Biome tags of other mods (horse breeds, Naturalist animals, pigeons, fish) extended to the Regions Unexplored biomes and to those of `OTHER_MODELS`, from the tables of `scripts/biomes/models.py`; stops when a mod of the pack has a biome no table knows; reads the jar cache filled by `npm run check-deps`; rerun after adding a mod with biomes or biome rules, or updating Regions Unexplored |
| `python3 scripts/terrain/disc.py [key=value ...]` | The disc world (`pack/kubejs/data/compline/`): the two Lithostitched modifiers that shape the continent and its climate, and `monde/bord.json`, the edge read by the `bord` module; rerun to change the size, the coast or the edge ([0022](docs/decisions/0022-disc-world.md)) |
| `python3 scripts/structures/chorusoatl_nest.py` | The nest of the Chorusoatl (`pack/kubejs/data/compline/structure/chorusoatl.nbt`): a floor, an air pocket and the animal, like the nests of Archie's Dinosaurs ([0021](docs/decisions/0021-spelunkery-and-dinosaurs.md)) |

## Layout

| Content | Location |
|---|---|
| Mod (packwiz metadata) | `pack/mods/<slug>.pw.toml` |
| Shared config of a mod / config applied to every new world | `pack/config/` / `pack/defaultconfigs/` |
| KubeJS scripts: registries and items / recipes, rules, spawns / display, EMI, tooltips | `pack/kubejs/startup_scripts/`, `server_scripts/`, `client_scripts/` |
| French translation of a mod (language files, books) | `pack/kubejs/assets/<namespace>/` |
| Datapack data (worldgen, loot tables, tags, structures) | `pack/kubejs/data/compline/` (or `data/<namespace>/` to override a mod) |
| Custom `compline` mod (sources) | `mods-custom/compline/` ([mod-compline](docs/plan/mod-compline.md)) |
| House shader pack (GLSL, block list, settings names) | `pack/shaderpacks/Brumaire/` ([shader](docs/plan/shader.md)); comments in French |
| Tooling and its tests / generators | `scripts/` + `scripts/test/` / `scripts/biomes/`; `scripts/structures/`, `scripts/terrain/` for generated world data |
| Vision, registry, roadmap, in-game tests | `docs/vision.md`, `docs/mods.md`, `docs/feuille-de-route.md`, `docs/tests-en-jeu.md` |
| Living page of a system (mod, pack, translation, quests…) | `docs/plan/` |
| Settled decision (comparison, choice) | `docs/decisions/NNNN-subject.md` |
| Frozen plans, research notes, unused drafts | `docs/archive/` (read-only history) |

Datapacks go through KubeJS: NeoForge does not load a global datapack without an additional mod, whereas `kubejs/data/` is active in every world.

## Conventions

- Docs, commit messages and code identifiers in **English**; in-game text in **French** (the language of each translation file). KubeJS script comments and Compline's own texts are in French.
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

In-game testing is done in a **"Compline test"** instance of the Modrinth App. `npm run sync` never modifies the instance. In the other direction: `packwiz mr export` (or the `dev` pre-release), then import the `.mrpack` into the Modrinth App.

## Outside Git

Ignored by `.gitignore` and never committed: `dist/` and `*.mrpack` (local exports; releases and the `dev` pre-release hold the built packs), `mods-custom/compline/build/`, `mods-custom/compline/run/` (Gradle dev client and server, with their worlds), `.gradle/`, `node_modules/`, `.cache/` (downloads of the scripts), `__pycache__/`. The only build outputs that are committed are the jars in `pack/mods/`: the `compline` jar, and the NeoForge ports of Surveyor, Antique Atlas and Antique Transport, built from the public repository `salvagedisc/compline-atlas` (LGPL), Astemir Re-Lib (withdrawn from Modrinth by its author, [0021](docs/decisions/0021-spelunkery-and-dinosaurs.md)) and Cinematic Interactions (MIT, CurseForge only, [0030](docs/decisions/0030-farmers-delight-kitchen.md)). Secrets live in the GitHub repository settings, never in files.

## Current state

As of 2026-11-15: pack `0.1.0` unreleased (`[Unreleased]` in the changelog), 142 Modrinth mods, one library and Cinematic Interactions kept as jars, the custom `compline` mod 0.20.2 and the three atlas ports, decisions 0001 to 0031, CI green on `main`. The look comes from the house shader pack Brumaire, on by default with Iris ([0028](docs/decisions/0028-house-shader.md), [0031](docs/decisions/0031-brumaire-is-the-look.md)), which is taking every effect of Recrafted one step at a time; it runs on the full pack on a bench without a graphics card, nobody has played with it. The house resource pack `compline-ambiance` and Polytone left on 2026-11-15. The house resource packs were removed on 2026-11-13 ([0024](docs/decisions/0024-removing-the-house-resource-packs.md)) and the house screens, weather effects and a few leftover mods on 2026-11-14 ([0026](docs/decisions/0026-second-cleanup.md)): the mist is ATMOSPHERICS' own preset until a new one is chosen. No Man's Land left the pack ([0019](docs/decisions/0019-leaving-no-mans-land.md)). Geophilic and Regions Unexplored have not been seen in game. The pack starts on a dedicated server but **nobody has played it yet**: next step is the eight play sessions of [tests-en-jeu.md](docs/tests-en-jeu.md), then the `v0.1.0` tag. Open work is tracked in the [roadmap](docs/feuille-de-route.md).
