# Playing the latest main

Every push to `main` runs the `dev` workflow, which rebuilds the pack and replaces the [`dev` pre-release](../../releases/tag/dev). It always holds `Fallow-dev.mrpack`, built from the latest `main`; its notes give the commit.

1. Open the [`dev` pre-release](../../releases/tag/dev) (signed in to GitHub: the repository is private) and download `Fallow-dev.mrpack`.
2. In the Modrinth App: *+ → Import from file*.

Each import creates a new instance. To keep a world, copy its folder from `saves/` of the old instance to the new one (*Open folder* on the instance).

An automatic update at each launch (packwiz-installer as a Modrinth App pre-launch hook) was considered on 2026-11-01. It needs `pack/` at a public URL, so it was set aside while the repository stays private.
