# 0039 — The pack's name: Fallow, under the `fallow-pack` slug

- **Date**: 2026-11-18
- **Status**: kept (the maintainer's choice of 2026-11-18)

## Context
Fallow is one ordinary word for a field left to rest that is woken again, with the seasons, the fallow deer and a quiet melancholy in it. The `fallow` project slug is taken on Modrinth, where the pack is published, so the name had to be checked before release:

- On Modrinth only the slug is unique, not the project title. The `fallow` slug belongs to a Fabric seasons mod (one version, June 2026, a handful of downloads) whose status is *withheld*: it does not show in search.
- On CurseForge, no project uses the name, and the `fallow` slug is free in every Minecraft category.
- A narrative adventure game called *Fallow* (2021) exists on Steam; it is another medium and another genre. No official trademark register was searched.

## Decision
The pack is published as **Fallow** under the slug `fallow-pack` on Modrinth and `fallow` on CurseForge. Everything else uses `fallow`: pack name and exports (`Fallow-*.mrpack`), custom mod (mod id, namespace and package, `mods-custom/fallow/`, `Fallow*` classes, `config/fallow-*.toml`), datapack and asset namespaces, music pack, Questlog chapters, the shader pack's texts and the documentation. The repositories are `salvagedisc/fallow` and `salvagedisc/fallow-atlas`.

## Consequences
- Registry ids are `fallow:…` (`fallow:dissipateur`…). `0.x` worlds and test instances made before 2026-11-18 used other ids and must be recreated; no alias is kept.
- The custom mod's id matches the Modrinth seasons mod's, which is a Fabric mod for Minecraft 26.1: the two can never be installed together.
- Records 0023 and 0027, about names used before this one, are withdrawn.
