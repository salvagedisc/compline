# 0029 — The default look comes from a house resource pack, not from the shader pack

- **Date**: 2026-11-14
- **Status**: replaced by [0031](0031-brumaire-is-the-look.md) on 2026-11-15 (the pack and Polytone left)

## Context
[0028](0028-house-shader.md) gave the pack a shader pack, Brumaire, off by default: Iris does not support Apple Silicon Macs and a shader pack costs frames on every machine. The maintainer asked whether a resource pack with visual effects would not serve the goal better ("optimise as much as possible while improving the visuals") and named Recrafted by MR_CHOO as the model.

Recrafted itself cannot ship: version 0.2.1 only exists for the current versions of the game (it relies on their post-processing), it is sold by subscription and carries no licence. Its author's older free pack (Clearview) used Polytone, which exists for 1.21.1 and gives a resource pack post-processing chains with the depth of the scene, the sun angle and settings.

## Options
| Criterion | Brumaire by default | Recrafted as is | House resource pack with Polytone |
|---|---|---|---|
| Runs on every machine of the group | No (Macs) | No (other game version) | Yes |
| Cost | A full shader pipeline | — | A few full-screen passes |
| Licence | Ours | None granted | Ours |
| Sun shadows | Real shadow map | Yes | Approximate: a height field given by the `compline` mod |
| Mist that follows the clearings | Yes | No | ATMOSPHERICS' preset and the `clairiere` module, as before |

## Decision
The default look is a house resource pack, **`compline-ambiance`**, in `pack/resourcepacks/`, on at first launch (Default Options). Everything in it is written for this pack; Recrafted is the model, none of its files are used.

- **Polytone** comes back (removed in [0026](0026-second-cleanup.md) when no pack used it).
- One post chain: sun shafts, glow, air, sun shadows, under water, grade. Two core shaders: paler rain, lightning through the mist.
- **Sun shadows without a shadow map**: the new `relief` module of the `compline` mod (0.13.0, client, needs Polytone) publishes the height of the surface around the camera in a texture, and the pack walks through it towards the sun. The same texture tells the pack when the camera is under water.
- Settings in Polytone's screen, in French; `rendu` off gives back the image of the game.
- **Brumaire stays**, off by default, for the players who want real shadows and water and have the machine for it.

Details: [ambiance](../archive/plan/ambiance-polytone.md).

## Rejected
- Recrafted as is: wrong game version, paid, no licence.
- A Polytone lightmap or coloured lights: a lightmap switches off More Darkness and the Enhanced Celestials moons, and Polytone's coloured light needs OpenGL 4.3 (not on macOS) and stays vanilla on Sodium terrain.
- Camera-in-water test in Polytone's expressions (`c.hasFluid()`): in 5.3.8 the camera context caches the block it first sees and never refreshes it.

## Consequences
- 141 Modrinth mods, 7 resource packs.
- The shadows are an approximation (see the limits in [ambiance](../archive/plan/ambiance-polytone.md)); the frame rate has not been measured on a graphics card.
- Without the `compline` mod the pack keeps everything but the shadows and the underwater look.
- Still missing from the model: armor weathering, leaf litter, snow and sand effects.
