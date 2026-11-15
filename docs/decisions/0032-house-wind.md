# 0032 — The pack's own wind; Where Winds Blow leaves

- **Date**: 2026-11-15
- **Status**: accepted (maintainer's choice of 2026-11-15); updates the wind part of [0016](0016-vent-et-lumiere.md)

## Context
Since [0031](0031-brumaire-is-the-look.md), Brumaire draws the look of the pack. Where Winds Blow turns its own plant sway off under a shader pack (it goes through Sodium's shaders, which Iris replaces): Brumaire already redrew the sway and only read the wind's direction, strength and gusts from the mod, through the `rendu` module. What remained of Where Winds Blow under the shader was its wind state, its campfire smoke and grass variants in world generation. The maintainer asked for a house system able to do everything, as Recrafted does, and for campfire smoke identical to Better Smoke, which only exists for Fabric.

## Decision
- **The `vent` module of the `compline` mod (0.16.0)** computes the pack's wind on the client: a direction that turns slowly and a strength that follows the weather and the altitude, both derived from the world time so every player of a server sees the same wind, and gust fronts that cross the landscape. The `rendu` module gives it to Brumaire as before (`brumaireWind`, `brumaireGust0..1`).
- **Campfire smoke after Better Smoke** (MIT, Fabric only), written for the pack without its code: the vanilla smoke particle led by the wind once it has risen, one larger and longer plume over several campfires side by side, a bluish smoke over a soul campfire, bluer as it rises. Switch `vent.enabled`.
- **Brumaire** bends grass and flowers away from the feet of the four living beings nearest the camera (`brumairePush0..3`, new) and slants rain and snow with the wind.
- **Where Winds Blow 2.1.0 and ToucanLib leave**, with their configs, French texts and the `ru_*` wind profiles written by `scripts/biomes/biome_rules.py`. Their grass variants leave world generation: the maintainer does not want them redone.

## Consequences
- Without the shader pack, plants no longer sway and rain no longer slants; the smoke still follows the wind.
- Lanterns, chains and banners no longer sway (Where Winds Blow did it under the vanilla renderer only).
- Tests SH5 and SH14 in [tests-en-jeu](../tests-en-jeu.md).
