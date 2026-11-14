# 0017 — Rain, storm and sunlight at the level of the best shader packs

- **Date**: 2026-11-01
- **Status**: kept (the team's choice of 2026-11-01); seen only in a headless test client loaded with the weather mods alone (the full pack does not start in the development environment), not played; removed by [0026](0026-second-cleanup.md)

## Context
The team compared the pack with Recrafted, a paid resource pack with its own core shaders. Its rain, its storms and its moving leaves are more worked than ours, even where we have a similar element. Recrafted itself cannot be used: no licence, Minecraft 26.x only, and its core shaders replace the terrain shaders that Sodium already replaces. The team asked to reach that level with what the pack can carry, without treating what is already in place as enough.

What was missing, effect by effect (read from Recrafted's published videos and from the jars of the pack):

| Effect | Before | Gap |
|---|---|---|
| Rain drops | Where Winds Blow slants the vanilla rain | Short blue drops, same colour in every light, nothing beyond 10 blocks |
| Distant rain | None | No shower crossing the valley |
| Lightning | Vanilla | Thick, regular, no branches; no flash in the sky |
| Thunder | Vanilla | Same sound near and far |
| Rain on things | AmbientSounds, Drip Sounds | Same sound on metal, glass, leaves and water |
| Drops on leaves and eaves | None | |
| Splashes and screen drops | Particular (ripples) | No splash on the ground, no drop on the screen |
| Leaves | Particular drops them straight down; Where Winds Blow sways the blocks | Leaves ignore the wind and vanish 5 s after landing; storms do not tear them off |
| Grass | Where Winds Blow bends it | Still in calm weather; nothing kicked up when running |
| Snow and mud | None | No footprints |
| Shores | None | No waves breaking on the beach |
| Sunlight | Shine (bloom) | No shaft of light under the trees, no glint on water |
| Colours | NML and vanilla greens | Greener and colder than Recrafted's olive and ochre |

## Decision
**Mods (client only, all translated into French):**
- Atmospheric Rain Backport 1.2: drops on the screen when looking up in the rain.
- Cool Rain Reforged 1.0.2 (+ Sable: Cool Rain 1.0.1, so that rain sounds right on airships): the sound of rain on metal, glass, fabric, leaves and water, muffled indoors.
- Distant Thunders 1.1.0: thunder delayed and dulled with distance.
- Dynamic Surroundings Rebirth 1.4.2, reduced to three effects in `config/dsrebirth/dsrebirth.json`: drops forming on plants, water dripping from eaves and leaves after the rain, morning dew. Its fog, sky, sounds, footprints, compass, aurora and numbers are off: other mods of the pack own them. Its icicles are off too: they write a line to the log every five seconds.
- Soft Imprints 2.8.0: footprints in snow, sand and mud.
- Wavify 1.2.1: waves breaking on shores and rivers.

**Settings:**
- Particular drops leaves a little more often (`spawnChance` 45 instead of 60).
- Where Winds Blow sways foliage a little even in calm weather (`clearWeatherSwayStrength` 0.35), and more when the player passes (1.3) or the wind blows (1.25).
- `compline-ambiance` draws a new rain drop (`scripts/ambiance/weather.py`): thin, long, pale streaks instead of short blue drops.
- Greens pulled toward olive: the vanilla grass and foliage colour maps (`scripts/ambiance/palette.py`, a quarter of the way toward olive, saturation × 0.85) and No Man's Land's biome colours in Polytone (15 % toward olive, saturation × 0.92). The clearings keep their frank green: they must stand out.
- Shine stays in bloom-only mode: Polytone already colours the light of fires and candles, a second coloured light would double it. Its bloom is set low in `config/shine.json` (see Revision below).

**`compline` mod 0.6.0 (0.6.1 after the revision), two client modules** ([module page](../plan/mod-compline.md#averse-module)):
- `averse`: the rain takes the colour of the mist; light rain curtains from 24 to 84 blocks, slanted by the wind; a redrawn lightning bolt (thin white zigzag, halos, branches) and a brief flash of the sky; Particular's leaves carried by Where Winds Blow's wind, more of them torn off in storms, and left on the ground for 25 s; grass bits kicked up when running through tall grass.
- `rayons`: sun shafts falling through gaps in the canopy, stronger in thick mist, absent at night and in the rain; glints of sun on water along the path between the sun and the player.

Every effect has its own switch in `compline-client.toml`. The mixins on Where Winds Blow and Particular apply only when those mods are present, and fail silently if their classes change.

## Consequences
- Seven more client mods. Dynamic Surroundings Rebirth is large for three effects; if it costs frames, it is the first to go.
- The rain curtains and the shafts add a few hundred quads per frame; the leaves read the wind once per tick.
- Logo: the team does not like the three sketches inspired by the Minecraft logo family; put aside for now.

## Revision (2026-11-01, `compline` 0.6.1)
The first game on 0.6.0 showed three faults, all reproduced in a test instance running the whole pack:
- **Glowing blobs** in daylight. Shine had never been configured: its default bloom (strength 8, threshold 0.15, radius 500) made every sunlit surface glow, and the glints on water even more. `config/shine.json` now sets strength 2, threshold 0.75, radius 160 and a 48-block bloom distance: no blob in daylight, a light halo around lanterns and torches at night. The glints are dimmer too.
- **Yellow rain against the sun.** The gold back-light turned the whole rain yellow and more opaque. Removed: the drops are a light grey taken from the mist, at 70 % opacity.
- **Too much rain.** The drop texture has seven short streaks instead of nine long doubled ones, and fainter; the curtains start at 24 blocks instead of 14 and are half as opaque.

Visual Effects+ (Polytone resource pack, all rights reserved, 1.3.1 for 1.21.1) was considered as a replacement; see the thread of 2026-11-01.

## Revision (2026-11-03, `compline` 0.6.3)
The rain colour, the fog of the lightning and the sun shafts moved into the resource pack `compline-ambiance`: see [0018](0018-effets-en-pack-de-ressources.md). AV1 now reads « averse : pluie de Where Winds Blow, feuilles de Particular dans le vent »; AV10's shafts are the rays of RP1.

## In-game checks
Commands: `/weather rain`, `/weather thunder`, `/time set 1000` (morning), `/time set 23500` (dawn).
- AV1. The log shows « averse : pluie de Where Winds Blow teintée, feuilles de Particular dans le vent » and no mixin error from `compline`.
- AV2. Rain in a misty forest: thin pale streaks, the colour of the mist, the landscape visible through them; facing the sun, they stay grey, never yellow.
- AV3. Rain in a clearing or on a hill: curtains of rain drift in the distance and lean with the wind; none underground; FPS before and after (`curtains = false`).
- AV4. Thunder: the bolt is a thin white zigzag with branches, the sky flashes briefly, distant thunder comes later and duller than near thunder.
- AV5. Under a tree in calm weather leaves fall almost straight and stay on the ground about 25 s; in a storm many more fly off sideways with the wind.
- AV6. Running through tall grass kicks up a few bits of grass.
- AV7. Rain sounds differ on a metal roof, glass, wool and water, and are muffled indoors; on an assembled airship too.
- AV8. Drops on the screen when looking up in the rain; drops on plants and drips from eaves during and after the rain; dew at dawn; no icicles; nothing else from Dynamic Surroundings (no fog, no compass, no footstep sounds, no damage numbers).
- AV9. Footprints in snow, sand and mud; waves break on a beach and on a river.
- AV10. Morning in a dense forest with gaps: sun shafts lean like the sun, stronger in thick mist, gone at night and in the rain. A lake facing a low sun: a glittering path between the sun and the player.
- AV11. Greens: vanilla forests and No Man's Land biomes a little more olive; clearings still frank green.
