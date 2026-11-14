# 0028 — A house shader pack written from scratch

- **Date**: 2026-11-14
- **Status**: under test on `feat/house-shader` (first milestone: it runs on the full pack; not played)

## Context
Since [0024](0024-removing-the-house-resource-packs.md) and [0026](0026-second-cleanup.md) the mist is ATMOSPHERICS' own preset and the house effects are gone. The small effects tried since October (resource packs, particles, a redrawn lightning) never reached the look the vision asks for: a mist that lies on the land, lifts around the clearings and lets the light through. The maintainer asked which route gives something both light and beautiful: a fork of BSL or Complementary, a resource pack, or an effect mod.

## Options
| Criterion | Fork of Complementary | Fork of BSL | Resource pack or effect mods | Shader pack written from scratch |
|---|---|---|---|---|
| Licence | A fork is allowed only under conditions on how its code is produced, on its name and on its look; the unmodified pack may not ship with changed settings | All rights reserved, no fork without the author's agreement | Free | Ours |
| Mist that follows the clearings | Possible, inside a large code base | Not allowed | No: the fog of the game is one distance for the whole view | Yes, designed for it |
| Cost on the machine | A full deferred pipeline, heavier than what the pack needs | Same | None | Chosen pass by pass |
| Work | Reading and reshaping someone else's pack under licence constraints | — | Already tried three times | Everything to write, nothing to work around |

## Decision
The pack gets its own shader pack, **Brumaire** (working name), written from scratch in `pack/shaderpacks/Brumaire/`. No code comes from another shader pack.

- **Mods**: Iris 1.8.14-beta.1 (the only build for Sodium 0.8.13), Colorwheel (Flywheel, so Create, under a shader pack) and Iris Veil Compat (Veil, so Sable and Aeronautics; alpha).
- **Off by default**: Iris does not support Apple Silicon Macs officially. Default Options writes `config/iris.properties` on first launch with Brumaire selected and shaders off; a player turns them on in the video settings or with K.
- **Light on the machine**: one lighting pass in the geometry programs, one shadow map, one full-screen pass for the mist, one for the tone curve. No deferred lighting, no temporal pass. Three profiles (Léger, Équilibré, Beau).
- **The mist is the shader's**: a layer that thins with height above the ground around the player, holes where a clearing is open, and a ring at the horizon that never lifts, with or without Distant Horizons. Under a shader pack the fog of the game and ATMOSPHERICS' preset are not drawn.
- **The `rendu` module of the `compline` mod** gives the shader what GLSL cannot read: the open clearings, the height of the ground, and the wind of Where Winds Blow (its own plant sway is off under a shader pack, the shader redoes it). Without the mod the shader pack still works, with fallbacks.
- **Olive greens** (point 5 of the cleanup list) are a setting of the shader, applied to the colour of terrain.

Details and settings: [shader](../plan/shader.md).

## Rejected
- Fork of Complementary: its licence (clause 1.3) sets conditions on a fork that this project does not meet.
- Fork of BSL: all rights reserved.
- Complementary unmodified with the pack's settings: its licence forbids shipping it with changed settings, and its mist cannot follow the clearings.
- Resource pack or effect mods alone: the route of 0016 to 0018, removed in 0024 and 0026.

## Consequences
- 142 Modrinth mods. A player who never turns the shader on sees the game as before.
- With the shader on, ATMOSPHERICS no longer draws the mist: its preset only matters with the shader off. The `horizon` module's fog ring on Distant Horizons is likewise replaced by the shader's ring.
- Modded plants only wave if their block is listed in `block.properties` or carries a vanilla tag (flowers, saplings, crops, leaves): the list has to grow with the pack.
- Not done yet: translucent Flywheel models are not sorted (Colorwheel's order-independent mode is off), reflections on water show the sky only, and there is no Nether or End variant (the pack does not open those dimensions).
