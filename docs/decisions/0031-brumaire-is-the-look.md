# 0031 — Brumaire is the look of the pack; the house resource pack leaves

- **Date**: 2026-11-15
- **Status**: accepted (maintainer's choice of 2026-11-15); replaces [0029](0029-look-without-shader.md)

## Context
[0029](0029-look-without-shader.md) made a house resource pack, `compline-ambiance`, the default look, with the house shader pack Brumaire ([0028](0028-house-shader.md)) as an option, mainly because Iris does not support Apple Silicon Macs. After the water and air pass, the maintainer judged the result far from Recrafted, the model named for the look.

A study of Recrafted's files explains the gap. Recrafted draws on the terrain shader itself (Sodium 0.9 lets a resource pack replace it) and on scenes rendered from the sun and from above (Polytone 6.8 viewpoints), which give it a real shadow map and a height map. None of this exists for 1.21.1: Sodium 0.8.13 loads its shaders from its own jar and Polytone 5.3.8 has no viewpoints. A post effect on the finished image cannot reach that level. Moving the pack to the current versions of the game is not possible either: 48 of its 143 mods (Create, Farmer's Delight, Quark, Supplementaries…) have no build for them.

On 1.21.1, only a shader pack draws every surface, has a shadow map and can reach Recrafted's level. The maintainer plays on Windows and no longer counts the Mac.

## Decision
- **Brumaire is on from the first launch** (`config/iris.properties` through Default Options). K turns it off.
- Brumaire takes every effect of Recrafted, written for it, one step at a time, each step checked on the bench before and after; its profiles keep weak machines playable.
- **`compline-ambiance` leaves the pack**, with **Polytone** (no other pack uses it) and the **`relief`** module of the `compline` mod (0.15.0), which only served that pack.
- **ATMOSPHERICS stays** for players who turn the shader off: it is then the only mist. Under Brumaire the mist, sky and clouds are the shader's own.

## Consequences
- An Apple Silicon Mac plays without shader: vanilla look, ATMOSPHERICS mist.
- The frame rate on real hardware must be measured for each profile before the first release ([tests](../tests-en-jeu.md)).
- The page of the removed pack is kept in [the archive](../archive/plan/ambiance-polytone.md).
