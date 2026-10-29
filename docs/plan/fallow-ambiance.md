# The `fallow-ambiance` resource pack

> Created on 2026-10-19 from [11.1-11.2](11-rendu-et-confort.md), [12.1-12.2](12-verification.md) and [report 3](recherche/verif-2026-10-18/3-client.md). **Nothing has been seen in game.** The formats were read in the installed jars (ATMOSPHERICS 2.6.5, Polytone 1.21-5.1.3, Particular 1.5.7, No Man's Land 1.5.12, Default Options 21.1.8); the values, on the other hand, are paper choices, to be tuned on screen.

## What it contains

```
pack/resourcepacks/fallow-ambiance/
├── pack.mcmeta                                    pack_format 34 (1.21.1)
└── assets/
    ├── atmospherics/preset/fallow_v4.json          the mist biome by biome, the sand, the clouds, sun, moon and stars
    ├── fallow/polytone/biome_modifiers/*.json      grass, foliage, water (23 files)
    ├── fallow/polytone/colored_lights/*.json       colour of fire, soul and moonlight
    ├── minecraft/textures/environment/moon_phases.png   the moon, in the style of the ATMOSPHERICS sun
    ├── fallow/sounds.json                         events for the music pack tracks ([musique](musique.md))
    └── fallow/sounds/music/*.ogg                  10 free tracks, credits in CREDITS.md
```

It is enabled on first launch by Default Options (`pack/config/defaultoptions-common.toml`, last, hence above the other packs). Outside the pack, a single setting: `pack/config/nomansland-client.toml` turns off No Man's Land's fog (`[fog_modifiers] fogModifiers = false`) so that ATMOSPHERICS alone is responsible for the mist (L6 of report 3). This is an NML config, not an ATMOSPHERICS one: it cannot go in a resource pack.

## How the mods read it (read from the jars)

**ATMOSPHERICS.**
- It lists `assets/atmospherics/preset/**.json` in the active packs.
- The **first** of the sorted list is applied automatically, in game, with no screen open, once per fingerprint (the list of preset IDs).
- The file is a complete configuration: it **replaces** `config/ambientfog/biome_fog.json`, including the player's tweaks.
- Consequences:
  - a single preset in the pack;
  - to ship a new version, **rename** the file (`fallow_v4.json` → `fallow_v5.json`) and delete the old one, otherwise it is not reapplied;
  - each new version overwrites the players' ATMOSPHERICS settings.
- The file starts from the mod's original preset (version 43, all global settings kept). Only `presetName`, `presetAuthor` and the biome profiles change.
- The mod's bounds are respected: intensity 0-4, density 0.2-5, veil 0-3, particles 0-3.

**Polytone.**
- It reads `assets/<namespace>/polytone/biome_modifiers/*.json`.
- The files are in the `fallow` namespace with explicit `targets`, as the mod advises so as not to overwrite another pack.
- Fields used: `grass_color`, `foliage_color`, `water_color` (`#rrggbb` string).
- No `fog_color`, `sky_color`, `fog_start` or `fog_end`: ATMOSPHERICS rewrites them after Polytone (report 3, 3.3).
- No lightmap: it would switch off More Darkness, the Enhanced Celestials moon and NML's night (3.2).
- The clearings target `{"id": "fallow:clairiere", "required": false}`: as long as the biome does not exist (it is only a draft), Polytone ignores it without error.

**Particular Reforged: nothing to ship.**
- The leaf → particle mapping is **written in the code** (`CommonClass.clientSetup`). It is read neither from a pack nor from its config.
- Unknown blocks are recognised by their name:
  - NML's `pine_leaves` takes the spruce particle;
  - `yellow_birch_leaves` the birch one;
  - the 9 other foliages take the oak leaf, tinted by the colour of their texture.
- Particular does have willow and maple particles, but linking them to NML's leaves requires Java: a call to `CommonClass.registerLeafData` from the `fallow` mod (12.4), or Falling Leaves Plus as a fallback (11.2).
- L9 of report 3 ("Particular config") must therefore be corrected: this config does not exist.

## The chosen values

**Three mist families.** Distances in blocks, day then night. The planned view distance is 8 chunks, i.e. 128 blocks (10.12, decision 10).

| Family | Start → end (day) | Start → end (night) | Horizon veil | Mist particles |
|---|---|---|---|---|
| **Misty** | 0 → 52 | -2 → 34 | 3 / 3 | strong (1.6) |
| **Veiled** | 6 → 96 | 0 → 56 | 2.5 / 2.2 | medium (1.15) |
| **Clearing** | 40 → 500 (v3; 28 → 124 before) | 16 → 260 (v3; 8 → 84 before) | 1.5 / 1.2 | off |

- 10.3 spoke of "open" biomes. The maintainer wants a thick mist in general: they become **veiled**, still misty but markedly less so.
- Only the clearings open up the view. Since v3 (2026-10-25, [0011](../decisions/0011-tectonic-streams-distant-horizons.md)) their mist ends at 500 blocks by day, well beyond the 128 blocks of terrain: Distant Horizons draws the landscape beyond, fogged with the same curve by the `horizon` module of the `fallow` mod ([mod-fallow](mod-fallow.md#horizon-module)). Without Distant Horizons, the edge of the terrain shows at 128 blocks in a clearing: the price of the far view.

| Family | No Man's Land biomes | Vanilla (Overworld, surface) |
|---|---|---|
| Misty | `old_growth_forest`, `old_growth_forest_edge`, `autumnal_forest`, `dark_taiga`, `boreal_forest`, `frozen_woods`, `dark_swamp`, `bog`, `bayou`, `blackwater_river` | forests, taigas, `dark_forest`, `grove`, swamps, jungles, `mushroom_fields` |
| Veiled | `old_growth_forest_clearing` (NML's natural clearing), `maple_forest`, `maple_grove`, `lavender_field`, `prairie`, `frozen_shore`, `tropical_beach`, `desert_river`, `lush_river`, `mud_beach` | plains, meadow, `flower_forest`, `cherry_grove`, savannas, deserts, badlands, mountains, snow, beaches, rivers, oceans |
| Clearing | — | `fallow:clairiere`, `fallow:clairiere_froide`, `fallow:clairiere_chaude` |
| Cave | `caves`, `cave_depths`, `alchemist_ruins` (grey-brown mist, 72 blocks) | vanilla caves, Nether, End: original preset |
| Disabled | `dream`: profile `enabled: false`, NML keeps the rendering of its dream | — |

Vanilla surface biomes also receive the mist: without it, a vanilla plain would act as a clearing (the original preset knows no NML biome and leaves 350 blocks to plains).

**Mist colours** (day / night):
- by climate:
  - temperate `#a4aea8` / `#161b1f` (grey-green);
  - cold `#b9c3cb` / `#171c24`;
  - swamp `#8f9a86` / `#12160f`;
  - dry `#bdb6a2` / `#1a1a18`;
  - ocean `#9fb0b8` / `#121820`;
- the daytime sky stays close to the mist, so that the horizon dissolves;
- the clearings switch to a light mist `#c3d7e2` (cold `#cfdde8`, warm `#dcd8c4`) and a real blue sky `#7fa9d6`, with white clouds. This is where the light changes, for lack of a per-biome lightmap.

**Polytone: grass, foliage, water.**
- NML surface biomes:
  - misty: saturation × 0.72 and brightness × 0.95;
  - veiled: saturation × 0.85 and brightness × 0.98;
  - water: saturation × 0.80.
- `dark_swamp` and `blackwater_river` keep their grass: their "swamp" modifier would ignore a fixed colour.
- Clearings, in a frank green:
  - temperate: grass `#74c255`, foliage `#5fae3f`, water `#3d86c9`;
  - cold: `#79b878`, `#5f9f62`, `#3f7fc6`;
  - warm: `#9cc456`, `#7fae3c`, `#3a92c4`.
- Vanilla biomes are not touched: their grass follows the temperature palette, which a fixed colour would overwrite.

| NML biome | Family | Grass | Foliage | Water |
|---|---|---|---|---|
| `autumnal_forest` | misty | `#a2a44b` → `#8f9053` | `#bca549` → `#a59553` | `#3b8ab8` → `#4583a6` |
| `bayou` | misty | `#67bd39` → `#67a248` | `#69aa2f` → `#65913d` | `#24ab87` → `#30997d` |
| `blackwater_river` | misty | unchanged | `#6a7039` → `#5f633d` | `#3a7a6a` → `#3e7064` |
| `bog` | misty | `#958455` → `#857959` | `#83774c` → `#756d50` | `#547a5c` → `#55735b` |
| `boreal_forest` | misty | `#919d51` → `#838b57` | `#b69b58` → `#a48f5d` | `#2f628f` → `#375e81` |
| `dark_swamp` | misty | unchanged | `#638a2c` → `#5c7736` | `#4c7b67` → `#4e7363` |
| `dark_taiga` | misty | `#709968` → `#6f8b69` | `#5e8057` → `#5d7458` | `#3b7eb8` → `#4579a6` |
| `frozen_woods` | misty | `#acbebf` → `#a5b3b4` | `#acbebf` → `#a5b3b4` | `#4066b0` → `#4966a0` |
| `old_growth_forest` and `_edge` | misty | `#87b44c` → `#7e9d56` | `#7d953f` → `#728247` | `#3b8ab8` → `#4583a6` |
| `old_growth_forest_clearing` | veiled | `#87b44c` → `#83a952` | `#7d953f` → `#788c44` | `#3b8ab8` → `#4583a6` |
| `maple_forest` | veiled | `#87ab50` → `#83a155` | `#819e49` → `#7c954e` | `#3b8ab8` → `#4583a6` |
| `maple_grove` | veiled | `#7bba8d` → `#7cb38c` | `#548d6c` → `#57866b` | `#4066b0` → `#4966a0` |
| `lavender_field` | veiled | `#73bf67` → `#74b66a` | `#79ac4f` → `#77a254` | `#3b72b8` → `#4570a6` |
| `prairie` | veiled | `#c0b155` → `#b6a959` | `#a8ab54` → `#9fa159` | `#3b8ab8` → `#4583a6` |
| `frozen_shore` | veiled | `#9bbaa4` → `#99b5a1` | `#548d6c` → `#57866b` | `#4066b0` → `#4966a0` |
| `tropical_beach` | veiled | `#7cbc52` → `#7bb256` | `#72b335` → `#70a63d` | `#3eaacf` → `#47a0be` |
| `desert_river` | veiled | `#a3bd73` → `#9fb575` | `#91aa2f` → `#899e37` | `#3eaacf` → `#47a0be` |
| `lush_river` | veiled | `#67bd39` → `#68b042` | `#5ea723` → `#5d9a2c` | `#35aaa0` → `#3f9a92` |
| `mud_beach` | veiled | `#806a41` → `#796644` | `#6a7039` → `#656a3c` | `#598a98` → `#5c828d` |

**Kept as they are**: the original preset's global settings, including the two Sodium flags set to `true`. These are the mod's default values: it therefore does not touch `sodium-options.json`.

## Sand and snow (v2, 2026-10-19)

Request: more sand in the desert, flakes in snowy biomes. Read from the jars (ATMOSPHERICS 2.6.5, Polytone, Ecliptic Seasons, Subtle Effects, Particular), not seen in game.

**Sand: ATMOSPHERICS.** It already has dust and sandstorms for `desert`, the savannas and the badlands (list written in its code; `nomansland:desert_river` is not in it). The `fallow_v1` preset kept the original values, where dust in clear weather is almost invisible.

| `fallow_v2.json` key | v1 | v2 | Mod bound |
|---|---|---|---|
| `sandstormWeather.clear` count / speed / size | 0.1 / 0.1 / 1.0 | 1.0 / 0.5 / 1.15 | 2.5 / 1 / 1.35 |
| `sandstormWeather.rain` count / speed / size | 4.0 / 1.0 / 1.0 | 6.0 / 1.3 / 1.2 | 15 / 2.5 / 6 |
| `sandstormWeather.thunder` count / speed / size | 6.95 / 1.55 / 1.0 | 9.0 / 1.8 / 1.2 | 15 / 2.5 / 6 |
| desert `airHaze`: intensity / frequency / size / colour | 1.15 / 1.0 / 1.15 | 1.5 / 1.4 / 1.35, `#d8c8a0` | 3 / 3 / 3 |
| `airHaze` of the 3 badlands | same | 1.5 / 1.4 / 1.35, `#d2b48c` | same |

Storms follow the rain as seen by the client. With Ecliptic's per-biome weather, their frequency needs checking in game; if it rains during a storm, set `NoRainInDeserts = true` in `config/eclipticseasons-common.toml`.

**Snow: Polytone.** No installed mod makes flakes fall (Ecliptic can only attenuate vanilla snow). The `particle` field of a Polytone `biome_modifier` adds a vanilla ambient particle, as NML does for the dust in its caves:
- `vanilla_neige.json`: `minecraft:snowflake`, probability 0.004, on `snowy_plains`, `ice_spikes`, `snowy_taiga`, `snowy_slopes`, `frozen_peaks`, `jagged_peaks`, `grove`, `snowy_beach`, `frozen_river`;
- same block added to `nml_frozen_woods`, `nml_frozen_shore` and `nml_maple_grove`;
- `nml_desert_river.json`: sand `minecraft:falling_dust`, probability 0.002, since ATMOSPHERICS ignores this biome.

Limits: the densities are paper choices, and the flakes also appear under roofs and in the caves of these biomes.

## Coloured lights and moon (2026-10-19)

To compensate for the absence of shaders, read from the jars (Polytone 5.1.3, ATMOSPHERICS 2.6.5, Enhanced Celestials 2), not seen in game.

**Coloured lights: Polytone.** It reads `assets/<namespace>/polytone/colored_lights/*.json`: `target_type`, `targets` (ids or `#tag`) and `colored_light` (`color`, then optional `radius` and `brightness`). Its built-in engine (`BUILT_IN`, the default) tints block light on blocks, entities and particles, without Iris. This is not a lightmap: More Darkness and NML's night are not affected.
- `feu.json`: torches, lantern, campfire, fire, candles (`#minecraft:candles`) and NML sconces, `#ffb36b`;
- `ames.json`: the same blocks in their soul versions, `#8fd3e8`;
- `clair_de_lune.json`: NML's candle and moonlight basin, `#dde6f0`.

Contraption Lights' coloured light stays off (its default value): Polytone alone carries the colour.

**The moon.** The sun comes from ATMOSPHERICS' built-in pack: a warm 8 px square and eight very faint amber rays, on a black background (the blend is additive). Enhanced Celestials 2 moons have no texture of their own: all of them tint `moon_phases.png`. The pack provides one in the same style: a 4×2 sheet of 128×64, a pale 12 px square (same apparent size as the sun once ATMOSPHERICS' scale is applied), the dark part barely visible, eight very faint cold rays. The phases follow the vanilla order (checked against the game texture). It dresses the ordinary moon, all EC2 moons (which keep their tint) and the vanilla moon outside ATMOSPHERICS.

## What remains to be tuned in game

1. **Colour rendering.** The original preset uses very saturated mist colours (`#20afff` for plains): ATMOSPHERICS probably darkens or blends them according to the time. The greys chosen here may come out too dull or too light [inferred].
   - Tune with the ATMOSPHERICS editor.
   - Export, then save as `fallow_v5.json` while deleting `fallow_v4.json`.
2. **Thickness.** 52 blocks by day and 34 by night in forest: is it still playable? Are monuments and ruins visible early enough?
3. **The transition at the edge of a clearing.** The mist changes in 4-block cells, with smoothing (report 3, 1.4 and test T2). No use testing before the `fallow:clairiere` biome exists in `pack/kubejs/data/`.
4. **The clearing biome**, once created:
   - align the colours of its JSON (and of its two variants from report 4) with those of Polytone;
   - check that Polytone does apply them to a datapack biome received from the server.
5. **Ecliptic.** Do the seasons still tint the grass and leaves of the recoloured NML biomes? Report 3 thinks so, since we go through the biome colour and not a block colormap [inferred].
6. **NML's dream**: does the disabled profile really hand control back to NML [inferred from the code]?
7. **NML's caves**: grey-brown mist at 72 blocks, to compare with Particular's dust.
8. **Polytone coloured lights**: check the night tints, with More Darkness, and on an assembled Sable airship (Polytone modifies Sodium's chunk shader). The engine stays `BUILT_IN` (`polytone_options.json`); `VEIL` and `HYBRID` are to be avoided.
9. **NML's leaves in Particular** (test T13): generic oak for 9 foliages out of 11. If this is a nuisance, go through the `fallow` mod or Falling Leaves Plus (see above).
10. **The moon**: size and brightness next to the sun, tint of the EC2 moons, ordinary moon at night in the mist.
