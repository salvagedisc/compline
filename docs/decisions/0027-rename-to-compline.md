# 0027 — The pack is renamed Compline

- **Date**: 2026-11-14
- **Status**: kept (the maintainer's choice of 2026-11-14); replaces the name chosen in [0023](0023-rename-to-tilth.md)

## Context
A day after the rename to Tilth, the maintainer was no longer convinced by the name and asked for other short English names that are unused on Modrinth and CurseForge. Compline (the last office of the day, said at nightfall) fits the pack's evenings by the vigil fire and the mist that settles at dusk. On 2026-11-14 the `compline` slug was free on Modrinth, no CurseForge project and no Steam game used the name, and a web search found no conflicting game or trademark (no official trademark register was searched).

## Decision
Every mention of Tilth becomes Compline, identifiers included, with the same scope as [0023](0023-rename-to-tilth.md): pack name and exports (`Compline-*.mrpack`), custom mod (mod id, namespace and package `compline`, `mods-custom/compline/`, `Compline*` classes, `config/compline-*.toml`, version 0.11.1), datapack and asset namespaces, music pack, Questlog chapters, documentation. Record 0023 keeps its text and file name as the history of the first rename.

The GitHub repository is renamed `salvagedisc/compline` by the maintainer (repository settings cannot be changed from the tooling); GitHub redirects the old address.

## Consequences
- Every registry id changes again (`tilth:dissipateur` becomes `compline:dissipateur`…). `0.x` worlds are disposable, so no alias is kept.
- An existing test instance must be imported again from the `dev` pre-release.
