# Contributing to Fallow

Working conventions for the repository. They apply to everyone who contributes.

## Principles

- **The repository is the source of truth** for the pack: mod list, configs, scripts and decisions.
- **Stability before novelty**: versions are pinned and updated one by one, never with `packwiz update --all`.
- **Every mod comes from Modrinth** and exists for Minecraft 1.21.1 / NeoForge. An unavailable mod is flagged, never replaced by a third-party source.
- **Only `pack/` ends up in the `.mrpack`**: docs, scripts and CI never go into it.
- Docs, commit messages and code identifiers in **English**; in-game text stays in the language of each translation file.
- Documentation file names may stay in French (`feuille-de-route.md`, `tannerie.md`…): they name the systems as the team says them in game. New files follow the same rule; do not rename existing ones, links depend on them.

## Where to put what

| Content | Location |
|---|---|
| Mod (packwiz metadata) | `pack/mods/<slug>.pw.toml` |
| Shared config of a mod | `pack/config/` |
| Config applied to every new world (serverconfig) | `pack/defaultconfigs/` |
| KubeJS script (registries, items, tags) | `pack/kubejs/startup_scripts/` |
| KubeJS script (recipes, server rules, spawns) | `pack/kubejs/server_scripts/` |
| KubeJS script (display, JEI/EMI, tooltips) | `pack/kubejs/client_scripts/` |
| French translation of a mod (language files, books) | `pack/kubejs/assets/<namespace>/` |
| Datapack data (worldgen, loot tables, tags, structures) | `pack/kubejs/data/fallow/` (or `data/<namespace>/` to override a mod) |
| Tooling (Node.js, dependency-free) | `scripts/` + tests in `scripts/test/` |
| Full modpack plan | `docs/plan/` |
| Settled decision (comparison, choice) | `docs/decisions/NNNN-subject.md` |
| Spec or implementation plan of a sub-project | `docs/conception/specs/`, `docs/conception/plans/` |

Datapacks go through KubeJS: NeoForge does not load a global datapack without an additional mod, whereas `kubejs/data/` is active in every world.

## Adding, updating or removing a mod

1. Check its availability on Modrinth for 1.21.1 NeoForge.
2. Run it through the **5-question filter** ([vision](docs/vision.md)): does it change the gameplay? does it fit in? does it hold up for 100 hours? does it bring a mechanic? does it avoid overlaps?
3. Add it: `cd pack && packwiz mr add <slug>` (or `npm run sync`), then check its side (`client` / `server` / `both`).
4. Run `npm run check-deps`: Modrinth does not always declare every dependency.
   Then `npm run check-fr`: all of the mod's text must be in French ([translation](docs/plan/traduction.md)).
5. Update [docs/mods.md](docs/mods.md) and, if a comparison took place, create a record in [docs/decisions/](docs/decisions/README.md).
6. Add a line to `CHANGELOG.md` (`[Unreleased]` section).
7. **One commit per mod**:
   - `feat(mods): add <slug>`
   - `chore(mods): update <slug> x → y`
   - `feat(mods)!: remove <slug>`

Other prefixes: `feat(config): …`, `feat(kubejs): …`, `feat(sync): …`, `docs: …`, `ci: …`, `chore: …`.

`index.toml` is generated: after any change to `pack/`, run `packwiz refresh` in `pack/`. CI fails if the index is out of date.

## Branches and PRs

- `main` is **always playable**; the `vX.Y.Z` tags are placed on it.
- Experiments happen on `test/<subject>` (e.g. `test/distant-horizons`), then are merged into `main` by PR or abandoned. A merged branch is deleted.
- Every push and every PR runs `build`: tests, dependency check, up-to-date index, `.mrpack` export as an artifact. A PR is merged only when it is green.

## Versions and release

- `0.x`: prototype, disposable worlds.
- `1.0.0`: launch of the permanent world.
- After `1.0.0`, any change that removes blocks or items or modifies worldgen is a **major version** and requires a world backup before updating.

To publish: move the `[Unreleased]` section of `CHANGELOG.md` under `[X.Y.Z]`, set the same version in `pack/pack.toml`, run `packwiz refresh`, then place the `vX.Y.Z` tag on `main`. The `release` workflow checks that they match, exports the `.mrpack` and creates the GitHub release. Publication to Modrinth stays disabled until `MODRINTH_PROJECT_ID` (variable) and `MODRINTH_TOKEN` (secret, entered by the owner) exist.

## Test instance

In-game testing is done in a **"Fallow test"** instance of the Modrinth App. `npm run sync -- --instance "Fallow test"` compares the instance with the pack (via the Modrinth API), prints the diff and applies the packwiz commands after confirmation; `--configs` also copies the modified configs, and `--dry-run` shows the diff without applying anything. The script never modifies the instance.

In the other direction: `packwiz mr export`, then import the `.mrpack` into the Modrinth App.
