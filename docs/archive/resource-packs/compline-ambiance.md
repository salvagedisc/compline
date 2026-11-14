# The `compline-ambiance` resource pack

> **Archived on 2026-11-13.** The maintainer removed the three house resource packs (`compline-interface`, `compline-ambiance`, `compline-textures`) from the pack ([0024](../../decisions/0024-removing-the-house-resource-packs.md)). This page records what they contained and how the mods read them, should any part come back.

> Created on 2026-10-19 from [11.1-11.2](../plan/11-rendu-et-confort.md), [12.1-12.2](../plan/12-verification.md) and [report 3](../plan/recherche/verif-2026-10-18/3-client.md). **Nothing has been seen in game.** The formats were read in the installed jars (ATMOSPHERICS 2.6.5, Polytone 1.21-5.1.3, Particular 1.5.7, No Man's Land 1.5.12, Default Options 21.1.8); the values, on the other hand, are paper choices, to be tuned on screen.

> **Updated on 2026-11-13.** No Man's Land left the pack ([0019](../../decisions/0019-leaving-no-mans-land.md)): the preset became `compline_v6.json` (same global settings as `compline_v5.json`; the 24 profiles of No Man's Land removed, 78 profiles of Regions Unexplored added), with the mist profiles and colours of Regions Unexplored written by `scripts/ambiance/biomes.py`. The same day it became `compline_v7.json`: 16 biomes of other mods had no profile at all, so no mist (the 14 of Hybrid Aquatic, the stream of Streams Reflowing, Quark's glimmering weald); they now copy a vanilla profile too (`OTHER_MODELS`). Nothing has been seen in game with Regions Unexplored. The dated sections below that speak of No Man's Land are kept as history.

## What it contains

```
pack/resourcepacks/compline-ambiance/
├── pack.mcmeta                                    pack_format 34 (1.21.1)
└── assets/
    ├── atmospherics/preset/compline_v7.json          the mist biome by biome, the storms, the sand, the clouds, sun, moon and stars
    ├── compline/polytone/biome_modifiers/*.json      grass, foliage, water, snowflakes (69 files: 65 `ru_*`, 3 clearings, `vanilla_neige`)
    ├── compline/polytone/colored_lights/*.json       colour of fire and soul lights
    ├── compline/sounds.json                         events for the music pack tracks ([musique](../../plan/musique.md))
    └── compline/sounds/music/*.ogg                  10 free tracks, credits in CREDITS.md
```

It is enabled on first launch by Default Options (`pack/config/defaultoptions-common.toml`, last, hence above the other packs). Nothing is set outside the pack: the fog config of No Man's Land (`pack/config/nomansland-client.toml`) went with the mod, so ATMOSPHERICS alone is responsible for the mist.

## How the mods read it (read from the jars)

**ATMOSPHERICS.**
- It lists `assets/atmospherics/preset/**.json` in the active packs.
- The **first** of the sorted list is applied automatically, in game, with no screen open, once per fingerprint (the list of preset IDs).
- The file is a complete configuration: it **replaces** `config/ambientfog/biome_fog.json`, including the player's tweaks.
- Consequences:
  - a single preset in the pack;
  - to ship a new version, **rename** the file (`compline_v7.json` → `compline_v8.json`, as `compline_v6.json` became `compline_v7.json` on 2026-11-13) and delete the old one, otherwise it is not reapplied;
  - each new version overwrites the players' ATMOSPHERICS settings.
- The file starts from the mod's original preset (version 43, all global settings kept). Only `presetName`, `presetAuthor` and the biome profiles change.
- The mod's bounds are respected: intensity 0-4, density 0.2-5, veil 0-3, particles 0-3.

**Polytone.**
- It reads `assets/<namespace>/polytone/biome_modifiers/*.json`.
- The files are in the `compline` namespace with explicit `targets`, as the mod advises so as not to overwrite another pack.
- Fields used: `grass_color`, `foliage_color`, `water_color` (`#rrggbb` string).
- No `fog_color`, `sky_color`, `fog_start` or `fog_end`: ATMOSPHERICS rewrites them after Polytone (report 3, 3.3).
- No lightmap: it would switch off More Darkness and the Enhanced Celestials moon (3.2).
- The clearings target `{"id": "compline:clairiere", "required": false}`. Since `compline` 0.8.0 these three biomes are only the look of a clearing: the world keeps its real biome, and the mod lends their base colours to it where a clearing is open ([mod-compline](../../plan/mod-compline.md#clairiere-module)). Without the mod, Polytone ignores them without error.

**Particular Reforged: nothing to ship.**
- The leaf → particle mapping is **written in the code** (`CommonClass.clientSetup`). It is read neither from a pack nor from its config.
- Unknown blocks are recognised by their name. Read with No Man's Land 1.5.12: its `pine_leaves` took the spruce particle, `yellow_birch_leaves` the birch one, and its 9 other foliages the oak leaf, tinted by the colour of their texture. The leaves of Regions Unexplored have not been examined.
- Particular does have willow and maple particles, but linking them to other mods' leaves requires Java: a call to `CommonClass.registerLeafData` from the `compline` mod (12.4), or Falling Leaves Plus as a fallback (11.2).
- L9 of report 3 ("Particular config") must therefore be corrected: this config does not exist.

## The chosen values

**Three mist families.** Distances in blocks, day then night. The planned view distance is 8 chunks, i.e. 128 blocks (10.12, decision 10).

| Family | Start → end (day) | Start → end (night) | Horizon veil | Mist particles |
|---|---|---|---|---|
| **Misty** | 0 → 40 (v5; 52 before) | -2 → 26 (v5; 34 before) | 3 / 3 | strong (1.6) |
| **Veiled** | 4 → 72 (v5; 6 → 96 before) | 0 → 44 (v5; 56 before) | 2.5 / 2.2 | medium (1.15) |
| **Clearing** | 40 → 500 (v3; 28 → 124 before) | 16 → 260 (v3; 8 → 84 before) | 1.5 / 1.2 | off |

- 10.3 spoke of "open" biomes. The maintainer wants a thick mist in general: they become **veiled**, still misty but markedly less so.
- Only the clearings open up the view. Since v3 (2026-10-25, [0011](../../decisions/0011-tectonic-streams-distant-horizons.md)) their mist ends at 500 blocks by day, well beyond the 128 blocks of terrain: Distant Horizons draws the landscape beyond, fogged with the same curve by the `horizon` module of the `compline` mod ([mod-compline](../../plan/mod-compline.md#horizon-module)). Without Distant Horizons, the edge of the terrain shows at 128 blocks in a clearing: the price of the far view.

**Regions Unexplored biomes** (`regions_unexplored:*`, 78 profiles in the preset). Each one copies the profile of the vanilla biome it resembles: the table `MODELS` of `scripts/ambiance/biomes.py` gives the pairs (for example `redwoods` copies `old_growth_pine_taiga`, `bayou` copies `swamp`, `steppe` copies `savanna`, `tundra` copies `snowy_plains`). The family follows from the model, so a biome is misty when its model ends its daytime mist at 40 blocks.

| Family | Regions Unexplored biomes | Vanilla (Overworld, surface) |
|---|---|---|
| Misty (34) | `alpha_grove`, `ashen_woodland`, `autumnal_maple_forest`, `deciduous_forest`, `magnolia_woodland`, `maple_forest`, `mauve_hills`, `old_growth_forest`, `orchard`, `temperate_grove`, `willow_forest`, `wisteria_grove`, `silver_birch_forest`, `windswept_maple_forest`, `bamboo_forest`, `blackwood_taiga`, `boreal_taiga`, `golden_boreal_taiga`, `pine_taiga`, `old_growth_boreal_taiga`, `old_growth_golden_boreal_taiga`, `redwoods`, `sparse_redwoods`, `cold_boreal_taiga`, `cold_deciduous_forest`, `frozen_pine_taiga`, `pine_slopes`, `bayou`, `old_growth_bayou`, `fen`, `fungal_fen`, `marsh`, `eucalyptus_forest`, `rainforest` | forests, taigas, `dark_forest`, `grove`, swamps, jungles, `mushroom_fields` |
| Veiled (33) | `sparse_rainforest`, `tropics`, `barley_fields`, `clover_plains`, `flower_fields`, `grassland`, `poppy_fields`, `prairie`, `pumpkin_fields`, `shrubland`, `highland_fields`, `rocky_meadow`, `baobab_savanna`, `dry_bushland`, `steppe`, `joshua_desert`, `saguaro_desert`, `arid_mountains`, `outback`, `frozen_tundra`, `tundra`, `icy_heights`, `spires`, `mountains`, `towering_cliffs`, `chalk_cliffs`, `grassy_beach`, `gravel_beach`, `cold_river`, `muddy_river`, `tropical_river`, `hyacinth_deeps`, `rocky_reef` | plains, meadow, `flower_forest`, `cherry_grove`, savannas, deserts, badlands, mountains, snow, beaches, rivers, oceans |
| Clearing | none | `compline:clairiere`, `compline:clairiere_froide`, `compline:clairiere_chaude` |
| Cave and Nether (11) | `ancient_delta`, `bioshroom_caves`, `prismachasm`, `redstone_caves`, `inferno`, `scorching_caves`, `blackstone_basin`, `glistering_meadow`, `infernal_holt`, `mycotoxic_undergrowth`, `redstone_abyss` (profile of vanilla `lush_caves`, `dripstone_caves` or `nether_wastes`, as the script pairs them) | vanilla caves, Nether, End: original preset |

**Biomes of the other mods** (16 profiles, table `OTHER_MODELS` of the same script). Hybrid Aquatic's trenches, reefs, seagrass bed, red meadow and deep warm ocean copy the vanilla ocean of their temperature, its tide pools the beach, its tropical river and the Streams Reflowing stream the river, its sulfuric caves and Quark's glimmering weald a vanilla cave. Their colours stay those of their mod. `biome_rules.py` stops if a mod of the pack has a biome that is in neither table.

Vanilla surface biomes also receive the mist, and so does every Regions Unexplored biome: a biome the preset does not know gets no mist at all, and would act as a clearing. When the mod is updated and adds a biome, `biomes.py` stops with a message until `MODELS` is completed.

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
- Regions Unexplored surface biomes get one modifier each, `ru_<biome>.json` (65 files), written by `scripts/ambiance/biomes.py`, which reads the colours in the biome files of the mod's jar:
  - misty family: grass and foliage saturation × 0.72 and brightness × 0.95;
  - veiled family: saturation × 0.85 and brightness × 0.98;
  - water: saturation × 0.80, only where the biome sets its own water colour (vanilla blue is left alone, so that the water does not change at a vanilla border);
  - snowflakes (`minecraft:snowflake`, probability 0.004) in snowy biomes;
  - a grass colour is not written when the biome uses a grass colour modifier (swamp-like), which would ignore a fixed colour;
  - cave and Nether biomes get no modifier.
- The same script rewrites the Regions Unexplored part of the `compline:brumeux` biome tag (the misty family: 34 biomes, 49 with the 15 vanilla ones), which the music pack reads ([musique](../../plan/musique.md)).
- Clearings, in a frank green:
  - temperate: grass `#74c255`, foliage `#5fae3f`, water `#3d86c9`;
  - cold: `#79b878`, `#5f9f62`, `#3f7fc6`;
  - warm: `#9cc456`, `#7fae3c`, `#3a92c4`.
- Vanilla biomes are not touched: their grass follows the temperature palette, which a fixed colour would overwrite.
- Usage, from the repository root, after each update of Regions Unexplored: `python3 scripts/ambiance/biomes.py <regions-unexplored jar>`. The preset must then be renamed by hand to be applied again. Run manually, committed with its output, never in CI.
- The same table carries the biome rules of the other mods: `python3 scripts/ambiance/biome_rules.py` adds each Regions Unexplored surface biome (and each surface biome of `OTHER_MODELS`, outside the tags of its own mod) to the mod tags that name its vanilla model (horse breeds, Naturalist animals, pigeons, fish) and writes the matching Where Winds Blow profiles (`ru_<name>.json`). It reads the jar cache of `npm run check-deps`. Placements chosen by hand (a dinosaur moved to a cave biome, [0021](../../decisions/0021-spelunkery-and-dinosaurs.md)) are in its `PLACED` table.

The values are paper choices, not seen in game. Until 2026-11-13 the same rule was applied by hand to 20 `nml_*.json` files for No Man's Land's biomes (for example `old_growth_forest` grass `#87b44c` → `#7e9d56`); they were deleted with the mod.

**Kept as they are**: the original preset's global settings, including the two Sodium flags set to `true`. These are the mod's default values: it therefore does not touch `sodium-options.json`.

## Sand and snow (v2, 2026-10-19)

Request: more sand in the desert, flakes in snowy biomes. Read from the jars (ATMOSPHERICS 2.6.5, Polytone, Ecliptic Seasons, Subtle Effects, Particular), not seen in game.

**Sand: ATMOSPHERICS.** It already has dust and sandstorms for `desert`, the savannas and the badlands (list written in its code; `nomansland:desert_river` is not in it). The `compline_v1` preset kept the original values, where dust in clear weather is almost invisible.

| `compline_v2.json` key | v1 | v2 | Mod bound |
|---|---|---|---|
| `sandstormWeather.clear` count / speed / size | 0.1 / 0.1 / 1.0 | 1.0 / 0.5 / 1.15 | 2.5 / 1 / 1.35 |
| `sandstormWeather.rain` count / speed / size | 4.0 / 1.0 / 1.0 | 6.0 / 1.3 / 1.2 | 15 / 2.5 / 6 |
| `sandstormWeather.thunder` count / speed / size | 6.95 / 1.55 / 1.0 | 9.0 / 1.8 / 1.2 | 15 / 2.5 / 6 |
| desert `airHaze`: intensity / frequency / size / colour | 1.15 / 1.0 / 1.15 | 1.5 / 1.4 / 1.35, `#d8c8a0` | 3 / 3 / 3 |
| `airHaze` of the 3 badlands | same | 1.5 / 1.4 / 1.35, `#d2b48c` | same |

Storms follow the rain as seen by the client. With Ecliptic's per-biome weather, their frequency needs checking in game; if it rains during a storm, set `NoRainInDeserts = true` in `config/eclipticseasons-common.toml`.

**Snow: Polytone.** No installed mod makes flakes fall (Ecliptic can only attenuate vanilla snow). The `particle` field of a Polytone `biome_modifier` adds a vanilla ambient particle, as No Man's Land did for the dust in its caves:
- `vanilla_neige.json`: `minecraft:snowflake`, probability 0.004, on `snowy_plains`, `ice_spikes`, `snowy_taiga`, `snowy_slopes`, `frozen_peaks`, `jagged_peaks`, `grove`, `snowy_beach`, `frozen_river`;
- same block added to three No Man's Land biomes (`nml_frozen_woods`, `nml_frozen_shore`, `nml_maple_grove`) and a sand particle for `nml_desert_river.json`: these files were deleted on 2026-11-13; `biomes.py` now adds the snowflakes to the snowy Regions Unexplored biomes.

Limits: the densities are paper choices, and the flakes also appear under roofs and in the caves of these biomes.

## Snow and dust storms (v5, 2026-10-29)

Request after the first tests: ATMOSPHERICS closer to the mod's presentation screenshots, with real effects in snowy and sandy biomes (the feeling of being lost in a cloud of snow or dust), and a thicker mist elsewhere. Built on `compline_v4` (sky and clouds of [0016](../../decisions/0016-vent-et-lumiere.md)). **Checked on the development client** (ATMOSPHERICS and Distant Horizons only, software OpenGL): ice spikes, savanna plateau, desert and forest, each in clear weather, rain and thunder; screenshots in the project files.

**How ATMOSPHERICS handles weather** (read in `AtmosphericEnvironmentVisuals`): with rain or thunder, the fog start and end move towards the absolute `fogStart`/`fogEnd` of the weather settings, in proportion to the rain and thunder levels, and the fog colour towards the rain or thunder colour. A biome's `weatherOverride` replaces the global settings entirely while the camera is in that biome. The rain level is global: in a snowy biome it is snowfall, in a dry biome (no precipitation) it is ATMOSPHERICS' dust weather, with its sandstorm particles in deserts, savannas and badlands. Where Winds Blow ties its blizzards (thunder in a snowy biome) and desert storms (rain in a desert or badlands) to the same levels, so its snow sheets and dust line up with this fog. Its own storm fog stays off (`snowFogMode`, `sandstormFogMode`): ATMOSPHERICS sets the fog last and would override it anyway.

| | Clear | Rain | Thunder | Colours (fog, rain → thunder) |
|---|---|---|---|---|
| Global (all other surface biomes) | biome value | 0 → 34 | 0 → 22 | grey `#8e989e` → `#5e666c` (the original preset's blue-violet `#424881` before) |
| Snow (15 biomes: snowy plains, slopes, taiga, beach, ice spikes, peaks, grove, frozen river and oceans, and four No Man's Land biomes until v6) | biome value | 0 → 30 | 0 → 10 (whiteout) | `#d3dce5` → `#b9c4cf` |
| Desert (and the No Man's Land desert river until v6) | biome value | 0 → 24 | 0 → 10 | ochre `#d4b07a` → `#b88f5c` |
| Badlands (3) | biome value | 0 → 24 | 0 → 10 | red sand `#c99a70` → `#a8704a` |
| Savannas (3) | biome value | 0 → 30 | 0 → 14 | gold `#cdb586` → `#ad9264` |
| Clearings | 500 / 260 | 0 → 200 | 0 → 120 (cold clearing 120, 60: snow still falls there) | grey |

- v4 had a single global rain end at 166 blocks: rain lifted the mist of every misty biome (52 blocks) and turned it blue. It now thickens it.
- **Colours in clear weather**, closer to the mod's screenshots but muted: desert fog `#d6bf93`, badlands `#cfa784`, savannas `#cdbb8a` (sky and haze tinted the same way); swamps `#869a78` and jungles `#8a9e7e`, greener; snow `#c3ced8`, a little bluer. Temperate biomes keep their grey-green: it is the Silent Hill mist.
- **Air particles** (`airHaze`): desert and badlands 2.2 intensity, 2.0 rate, 1.6 size; savannas 1.8 / 1.6 / 1.45; snow biomes 1.9 / 1.8 / 1.4 in white `#eef3f7` (1.1 to 1.6 before).
- **Sandstorm particles**: rain count 8 and size 1.3 (6 and 1.2 before), thunder count 12, size 1.4, speed 2.0 (9, 1.2, 1.8).

## Coloured lights and moon (2026-10-19)

To compensate for the absence of shaders, read from the jars (Polytone 5.1.3, ATMOSPHERICS 2.6.5, Enhanced Celestials 2), not seen in game.

**Coloured lights: Polytone.** It reads `assets/<namespace>/polytone/colored_lights/*.json`: `target_type`, `targets` (ids or `#tag`) and `colored_light` (`color`, then optional `radius` and `brightness`). Its built-in engine (`BUILT_IN`, the default) tints block light on blocks, entities and particles, without Iris. This is not a lightmap: More Darkness is not affected.
- `feu.json`: torches, lantern, campfire, fire, candles (`#minecraft:candles`) and Supplementaries sconces, `#ffb36b`;
- `ames.json`: the same blocks in their soul versions, `#8fd3e8`;
- `clair_de_lune.json` (the candle and moonlight basin of No Man's Land, `#dde6f0`) was deleted on 2026-11-13 with the mod.

Contraption Lights' coloured light stays off (its default value): Polytone alone carries the colour.

**The moon.** The sun comes from ATMOSPHERICS' built-in pack: a warm 8 px square and eight very faint amber rays, on a black background (the blend is additive). Enhanced Celestials 2 moons have no texture of their own: all of them tint `moon_phases.png`. The pack's own moon (a pale square with cold rays, in the style of that sun) was removed on 2026-10-29 at the team's request: the vanilla moon is back, drawn 1.35× by ATMOSPHERICS. A sun and moon resource pack can replace both textures; Enhanced Celestials keeps tinting whichever moon is active.

## Rain and olive greens (2026-11-01)

Part of [0017](../../decisions/0017-averse-et-rayons.md), the answer to Recrafted's rain and colours.

**Rain drop.** Where Winds Blow draws the vanilla `textures/environment/rain.png`, slanted by the wind. `scripts/ambiance/weather.py` (Pillow, deterministic) draws a new one: 64 × 256, seven thin streaks of one pixel, 18 to 40 pixels long, pale grey-white, brightest toward the falling end, plus four faint ones for depth (sparse on purpose: Where Winds Blow stacks the texture in several layers). The `averse` module of the `compline` mod then tints them with the mist colour. The same script draws the module's textures (rain curtain, sun shaft, glint on water).

**Greens.**
- Vanilla biomes: `scripts/ambiance/palette.py` reads the game's grass and foliage colour maps from the NeoForge client resources jar and pulls each entry a quarter of the way toward olive (hue 68°), saturation × 0.85, brightness × 0.96. The map still varies with temperature and humidity; Ecliptic Seasons' seasonal tint applies on top.
- No Man's Land biomes (history): their grass and foliage in `nml_*.json` were pulled 15 % toward olive, saturation × 0.92 on 2026-11-01. The `ru_*` files that replaced them use only the factors of the rule above; the olive pull is not applied to them.
- Clearings keep their frank green: they must stand out from the misty land around them.

## What remains to be tuned in game

1. **Colour rendering.** The original preset uses very saturated mist colours (`#20afff` for plains): ATMOSPHERICS probably darkens or blends them according to the time. The greys chosen here may come out too dull or too light [inferred].
   - Tune with the ATMOSPHERICS editor.
   - Export, then save as `compline_v8.json` while deleting `compline_v7.json`.
2. **Thickness.** 52 blocks by day and 34 by night in forest: is it still playable? Are monuments and ruins visible early enough?
3. **The transition at the edge of a clearing.** The mist changes in 4-block cells, with smoothing (report 3, 1.4 and test T2).
4. **The clearing looks** (`compline:clairiere` and its two variants, in the `compline` mod): their JSON colours match those of Polytone; check in game that the Polytone values are the ones shown (change one and reload with F3+T).
5. **Ecliptic.** Do the seasons still tint the grass and leaves of the recoloured Regions Unexplored biomes? Report 3 thinks so, since we go through the biome colour and not a block colormap [inferred].
6. **The family of each Regions Unexplored biome**: the `MODELS` pairs are paper choices. Walk through the biomes and note those whose mist or colours do not suit their look; change the pair in `scripts/ambiance/biomes.py` and run it again.
7. **Caves**: the cave biomes of Regions Unexplored copy vanilla cave profiles; compare with Particular's dust.
8. **Polytone coloured lights**: check the night tints, with More Darkness, and on an assembled Sable airship (Polytone modifies Sodium's chunk shader). The engine stays `BUILT_IN` (`polytone_options.json`); `VEIL` and `HYBRID` are to be avoided.
9. **Regions Unexplored's leaves in Particular** (test T13): to look at; with No Man's Land, 9 foliages out of 11 fell back on the generic oak leaf. If this is a nuisance, go through the `compline` mod or Falling Leaves Plus (see above).
10. **The moon**: size and brightness next to the sun, tint of the EC2 moons, ordinary moon at night in the mist.
