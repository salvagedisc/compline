# 0026 — Second cleanup: house effects, menus and leftovers

- **Date**: 2026-11-14
- **Status**: kept (the maintainer's choices of 2026-11-14 on the cleanup list); not played

## Context
After [0024](0024-removing-the-house-resource-packs.md), the maintainer was given the list of what was left to remove or tidy. He chose to remove every listed item and to tidy the leftovers, except the new mist, which waits for a study of the rendering route (shader pack, resource pack or effect mods).

## Decision
**Removed from the pack**
- The house title and loading screens: FancyMenu, Drippy Loading Screen and their libraries Konkrete and Melody, the layouts in `pack/config/fancymenu/` and their generator `scripts/menu/`. The game shows its own menu again, logo included.
- The `averse` module of the `tilth` mod (rain curtains, the redrawn lightning and sky flash, leaves blown by the wind, grass bits) and the `rayons` module (glints on water), with their textures and their generator `scripts/mod/weather.py`: `tilth` 0.11.0. Rain, storms and foliage are those of the game and of Where Winds Blow.
- The rain mods of [0017](0017-averse-et-rayons.md): Atmospheric Rain Backport, Cool Rain Reforged and Sable: Cool Rain, Distant Thunders, Soft Imprints, Wavify; and Dynamic Surroundings Rebirth.
- Shine (bloom) and Polytone (no pack used it since 0024).
- The Supernova resource pack and its three tracks.
- The separate copy of Biolith: Quark ships Biolith 3.0.14 inside its jar and asks for 3.0.x; no other mod of the pack declares it.
- Chunky. A server owner who wants to pregenerate the world adds it to the server; the `horizon.ChunkyAccessorMixin` fix stays and only applies when Chunky is there.
- The translations of every removed mod and the `.gitkeep` files of folders that are no longer empty.

**Tidied**
- Distant Horizons no longer writes start-up and chat warnings about mod compatibility, render distance, garbage collector, object pools, replays or slow world generation. The low-memory warning stays: it tells a player to give Java more memory.
- Adorable Hamster Pets no longer hands out its "Hamster Tips" book (in English) on first login or at the first hamster seen; it can still be crafted.
- The adaptive music draws on 30 tracks of the game, with no file added: four for the root, two at the approach of a monument (`ancestry`, `eld_unknown`), four aboard, three underground, eight in a clearing and three there at night, four at night elsewhere, three per season. The sound events live in the `tilth` mod (`assets/tilth/sounds.json`).
- Comments and documents that still described the removed items.

## Consequences
- 15 mods and one resource pack fewer: 139 Modrinth mods, one library kept as a jar, the `tilth` mod and the three atlas ports; 6 resource packs.
- The look of the game is now the one of its mods: ATMOSPHERICS with its own preset, Where Winds Blow, Particular, Subtle Effects, Better Leaves and Fresh Animations. The colours of grass and foliage stay those of the game and the biome mods until the rendering route is chosen.
- The in-game tests of the removed items (AV, R1–R6 for Shine, the menu test of session 1) no longer apply.
