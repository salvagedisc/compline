# 0016 — Wind, light and sky, without shaders

- **Date**: 2026-10-29
- **Status**: kept (the team's choice of 2026-10-29); nothing seen in game yet

## Context
After the first in-game tests (2026-10-28), the team found the world bland wherever the mist is gone, and asked for wind, light, or a light shader that would match the rest of the pack. The mist drawn by ATMOSPHERICS is the core of the pack: the `horizon` and `clairiere` modules of the `fallow` mod are built on it.

## Options
| Criterion | Without shaders: Where Winds Blow + Shine | Shader: Iris + Complementary Reimagined (low profile) |
|---|---|---|
| Wind | Grass, crops, flowers and leaves sway; slanted rain, drifting snow, smoke, banners, lanterns, chains; No Man's Land wind profiles | Waving foliage, depending on the pack |
| Light | Bloom, coloured light, rim light; no sun shafts | Shadows, reflections, sun shafts |
| ATMOSPHERICS mist | Untouched | Very likely replaced by the shader's own fog; the mist ring would have to be rebuilt in the shader |
| Aeronautics and Sable | No known issue | Needs Iris 1.8.14-beta.1, Colorwheel and Iris Veil Compat (alpha); Aeronautics still lists visual issues with Iris |
| Other rendering mods | Kept | Where Winds Blow turns its sway off and Shine its bloom under a shader pack; Polytone colours are usually ignored |
| Cost on Apple Silicon | Low | High: the only Apple Silicon benchmark found (M1 Pro, 2023, no mods) goes from 498 to 61 FPS with Complementary |

## Decision
**Where Winds Blow 2.1.0** (+ ToucanLib) for the wind and **Shine 2.0.2** for the light; no shader pack.

- One mod per layer, as in [0007](0007-ambiance-performance.md): `pack/config/where_winds_blow-client.toml` turns off its snow and sandstorm fog (ATMOSPHERICS owns the mist) and its plant colour regions (Polytone owns the vegetation colours). Every other key keeps its default.
- Where Winds Blow is the only wind system: Windblown and Living Wind, which also rewrite foliage rendering, must never be added next to it. It conflicts with Chunks Fade In (not in the pack).
- Its common config adds grass variants (flat, dry, dead, wild wheat) to world generation through biome modifiers. Kept on for now; to be confirmed before the permanent world is created (`enableDenseGrassWorldgen` in `where_winds_blow-common.toml`).
- Shine starts in "bloom only" mode; its coloured-light and rim-light presets are tried in game with its editor, then the chosen settings are copied into the pack. Shine 2.0.2 supports Sodium 0.8.12 and 0.8.13; entity and particle bloom does not work in this version.
- Both mods' texts are translated into French in `pack/kubejs/assets/`.

**Sky and clouds (team's choice of 2026-10-29).** The clouds did not match the mist. Read in the jars: Better Clouds cancels the vanilla `renderClouds`, where ATMOSPHERICS tints its clouds (mist colour, dusk, rain, "Story Mode" clouds); and ATMOSPHERICS cancels `renderSky` and draws the whole Overworld sky itself. So:
- **Better Clouds is removed**: ATMOSPHERICS alone draws the clouds.
- Preset `fallow_v4.json` (replaces `fallow_v3.json`, same biome profiles): second cloud layer at 210 (opacity 0.55, slower, wider), clouds mixed 45 % with the mist colour, darkened 60 % at night, automatic horizon blend; sun and moon 1.35× (bounds 0.25-3); stars 1,100 (4-1,500), brightness 1.25 (0-2), size 0.8 (0.5-3); ATMOSPHERICS' own wind streaks and desert tumbleweeds off, as Where Winds Blow already draws both.
- On 2026-10-29, `fallow_v5.json` replaces it: same sky and clouds, plus snow and dust storms, warmer dry biomes and a thicker mist ([ambiance](../plan/fallow-ambiance.md#snow-and-dust-storms-v5-2026-10-29)).
- **Enhanced Celestials stays**: it can already enlarge and recolour the moon (`moon_size`, texture and colour modifiers); replacing it would mean rebuilding the Friendly Moon and the `ciel` module.
- Auroras stays for aurorae; Distant Horizons keeps its own clouds off.

## Rejected
- Iris + a shader pack (Complementary Reimagined, MakeUp Ultra Fast): threatens the mist, switches off the two mods above, heavy on Mac. Could come back later as an option turned off by default, once the mist ring with Distant Horizons is fixed.
- Photon: not compatible with Apple GPUs. BSL: may not be redistributed in a modpack.
- Windblown (sway only, released 2026-10-17), Living Wind (a new release almost every day): fallbacks only.
- Windy: decorative wind streaks only. Aeronautics: Winds and Weather: sail gameplay, nothing moves on the ground. Weather, Storms & Tornadoes: heavy, its particle clouds would fight ATMOSPHERICS.
- Cirrus (replaces ATMOSPHERICS' fog and sky), Simple Clouds (OpenGL 4.3, so not on macOS; Sodium 0.8 bug #265), Nuit, Stellar View, Caelum (skyboxes, inert since ATMOSPHERICS cancels the vanilla sky).
- Vibrancy: refuses Sodium 0.8, development paused. Gleam, FilterLab: too young, later.

## Conditions and tests
- R1. The game starts with Sodium 0.8.13, Distant Horizons 3.3.3 and Sable; no warning from Where Winds Blow or Shine.
- R2. A No Man's Land forest under rain then thunder: sway, slanted rain, leaves; FPS before and after.
- R3. A clearing at sunset: Shine's bloom on the sky and the fires, with the mist around.
- R4. Snow and desert biomes: no extra fog on top of ATMOSPHERICS.
- R5. An Aeronautics airship in flight with lanterns: no bloom glitch on the Sable sub-level, lanterns sway.
- R6. Distant Horizons LODs: they stay still (sway applies to near terrain only).
- R7. Clouds: two layers, mist-coloured in misty biomes, orange at dusk, dark at night and in the rain; mountains rise through the lower layer.
- R8. Sun, moon and stars: bigger and brighter, still believable; the Enhanced Celestials moon is drawn over ATMOSPHERICS' moon at the right size.
- R9. No ATMOSPHERICS wind streaks or tumbleweeds next to Where Winds Blow's.
