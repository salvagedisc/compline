# 0024 — The house resource packs leave the pack

- **Date**: 2026-11-13
- **Status**: kept (the maintainer's choice of 2026-11-13)

## Context
Three resource packs were made for the pack and shipped as folders in `pack/resourcepacks/`, enabled on first launch by Default Options:

- `tilth-interface`: every vanilla and modded screen redrawn after GUI Revision (PR #16 and #17), with the name plates of the `titres` module;
- `tilth-textures`: blocks and items from Vanilla Evolved ([0015](0015-textures.md));
- `tilth-ambiance`: the ATMOSPHERICS preset (the mist biome by biome, storms, clouds), Polytone colours of grass, foliage and water, coloured fire light, the rain tint, the lightning that pierces the mist, the sun shafts ([0017](0017-averse-et-rayons.md), [0018](0018-effets-en-pack-de-ressources.md)), and the 10 free tracks of the music pack ([0010](0010-musique.md)).

The maintainer does not like them and asked to remove everything about them, mist and free tracks included.

## Decision
- The three folders are deleted, with their build scripts (`scripts/interface/` except the menus, `scripts/ambiance/`, `scripts/textures/`) and their entries in `defaultoptions-common.toml`.
- The `titres` module of the custom mod (name plates drawn from `tilth-interface`) is removed: `tilth` 0.10.1.
- The title and loading screens are FancyMenu and Drippy layouts in `pack/config/fancymenu/`, not a resource pack: they stay. Their generator moves to `scripts/menu/` (`menus.py`, `title/`).
- The music pack keeps its 15 tracks from the game and from Supernova. Their sound events (`tilth:music.*`) move from `tilth-ambiance` into the custom mod (`assets/tilth/sounds.json`, no audio). The 10 free tracks go; the mist and moon nodes, left without tracks, are removed; the monument node stays silent.
- The pages that described the packs move to [docs/archive/resource-packs/](../archive/resource-packs/).

## Consequences
- The mist falls back to ATMOSPHERICS' own preset, much lighter, with no profile for the clearing biomes: lighting a clearing no longer changes the mist on screen until a new mist is chosen.
- Screens, blocks and items look as their mods draw them. Rain, lightning and foliage take the game's colours. The Java effects of the `averse` and `rayons` modules (rain curtains, lightning shape, leaves in the wind, glints on water) stay.
- Players who already had the packs enabled lose them on the next update: Minecraft drops a selected pack that no longer exists.
- The in-game tests of the three packs (docs/tests-en-jeu.md) are removed.
