# 0039 — The pack is called Fallow again

- **Date**: 2026-11-18
- **Status**: kept (the maintainer's choice of 2026-11-18); replaces the names chosen in [0023](0023-rename-to-tilth.md) and [0027](0027-rename-to-compline.md)

## Context
The maintainer did not identify with Compline and still preferred Fallow: one ordinary word for a field left to rest that is woken again, with the seasons, the fallow deer and a quiet melancholy in it. Fallow had been left in [0023](0023-rename-to-tilth.md) only because the `fallow` slug was taken on Modrinth. A new check on 2026-11-18 showed that this does not block the name:

- On Modrinth only the slug is unique, not the project title. The `fallow` slug belongs to a Fabric seasons mod (one version, June 2026, a handful of downloads) whose status is *withheld*: it does not show in search.
- On CurseForge, no project uses the name, and the `fallow` slug is free in every Minecraft category.
- A narrative adventure game called *Fallow* (2021) exists on Steam; it is another medium and another genre. No official trademark register was searched.

## Decision
Every mention of Compline becomes Fallow, identifiers included, with the same scope as [0027](0027-rename-to-compline.md): pack name and exports (`Fallow-*.mrpack`), custom mod (mod id, namespace and package `fallow`, `mods-custom/fallow/`, `Fallow*` classes, `config/fallow-*.toml`, version 0.35.0), datapack and asset namespaces, music pack, Questlog chapters, the shader pack's texts, documentation. Records 0023 and 0027 keep their text and file names as the history of the earlier renames.

The pack is published as **Fallow** under the slug `fallow-pack` on Modrinth and `fallow` on CurseForge. The GitHub repositories are renamed `salvagedisc/fallow` and `salvagedisc/fallow-atlas` by the maintainer; GitHub redirects the old addresses.

## Consequences
- Every registry id changes again (`compline:dissipateur` becomes `fallow:dissipateur`…). `0.x` worlds are disposable, so no alias is kept.
- An existing test instance must be imported again from the `dev` pre-release; the old `config/compline-*.toml` files left in it are no longer read.
- The custom mod's id matches the Modrinth seasons mod's, which is a Fabric mod for Minecraft 26.1: the two can never be installed together.
