# Playing the latest main

Two ways to play what is on `main` without asking for an export. Both are refreshed by the `dev` workflow on every push to `main`.

## 1. Automatic update at each launch (public repository)

packwiz-installer reads the pack from GitHub Pages and brings the instance up to date before Minecraft starts: only what changed is downloaded, and mods removed from the pack are removed from the instance.

It needs a public URL for `pack/`, so it works once the repository is public and **Settings → Pages → Source** is set to **GitHub Actions**. The pack is then served at `https://salvagedisc.github.io/fallow/pack.toml`.

Setup in the Modrinth App, once:

1. Put [`scripts/dev/fallow-update.sh`](../scripts/dev/fallow-update.sh) in a folder whose path has no space, for example `~/Claude/Fallow/`.
2. Create an empty instance: *+ → Custom*, Minecraft **1.21.1**, loader **NeoForge**, version **21.1.265** (the one in `pack/pack.toml`).
3. Instance → *Settings → Launch hooks*: turn on *Custom game launch hooks*, then in *Pre-launch*: `/bin/sh /Users/<name>/Claude/Fallow/fallow-update.sh`.
4. Play: a packwiz window shows the download, then the game starts.

Notes:

- Use a new, empty instance. An instance imported from an older `.mrpack` can keep jars the pack no longer has (for example an older `fallow-x.y.z.jar`), which packwiz-installer does not know about and will not remove.
- The NeoForge version is not handled by packwiz-installer: when `pack/pack.toml` changes it, change it in the instance settings too.
- Offline or if GitHub is down, the script lets the game start with the installed version.
- A config changed by hand in the instance is overwritten when the pack changes that same file.
- Pages takes about a minute to update after a merge.

## 2. The `.mrpack` of the latest main (any visibility)

The [`dev` pre-release](../../releases/tag/dev) always holds `Fallow-dev.mrpack`, built from the latest `main`. Download it and use *+ → Import from file* in the Modrinth App. Each import creates a new instance: copy `saves/` across if a world must be kept.
