# Monuments: Ancient Remnants on the new terrain

The six monuments of Ancient Remnants: Monoliths (AR) 1.5.2 are the pack's large landmarks. This page says where they stand on the Tectonic + Regions Unexplored + Geophilic terrain, how they are kept off bad ground, and what they are for in a survival game.

## What the mod brings (read in the 1.5.2 jar)

| Monument | Shape | Blessing (one slot per player, kept after death) | Mod's default biome |
|---|---|---|---|
| Hunter monolith | Stone monolith floating 20 blocks above the ground, about 126 tall; the Elderheart sits in a window about 86 blocks up | *Hunter Instincts*: ranged damage grows with distance (up to 64 blocks) | `minecraft:badlands` |
| Warrior monolith | Same, floating | *Warrior Wrath*: damage grows with the number of monsters within 12 blocks | `minecraft:ice_spikes` |
| Sentinel monolith | Same, floating | *Sentinel Aegis*: damage reduction | `minecraft:deep_frozen_ocean` |
| Cherry monument | Temple about 70 × 60 blocks, sunk 24 blocks into the ground, 96 tall | *Desperate Vitality*: Regeneration below 33% health | `minecraft:cherry_grove` |
| Oakwood monument | Same, oak and moss | *Bountiful Harvest*: more from crops and livestock | `#minecraft:is_forest` |
| Coniferous monument | Same, spruce | *Architect's Reach*: +3 block reach | `#minecraft:is_taiga` |

- The Elderheart blesses the nearest player within 16 blocks, every 5 seconds. A player holds one blessing at a time: visiting another monument replaces it.
- Monuments have **no chest**. Five of the six had a **breeze spawner**; breezes are banned in Compline.
- The **Monolith Codex** (book, amethyst, deepslate) holds one page per monument. Each page is a research scrap, which tells the monument's lore and the biomes where it stands. Scraps dropped only at 0.2% per mob killed, and from pillager outposts.
- Placement is AR's own `ancient_remnants:configurable_random_spread`, set in `config/obscuria/ancient_remnants-common.toml` (spacing 100 chunks, separation 20, biome lists). A datapack can add biomes to the tags but not remove the config's defaults. Sparse Structures does not touch these sets.

## What was wrong on our terrain

Measured on 2026-11-13 on a headless server with the pack's server mods, seed `compline`, within 6,000 blocks of 0,0, the size of the planned disc, which is not in `main` yet (counted with `/locate` from a grid of points, chunks generated only up to structure starts):

| Monument | Default settings | Compline settings |
|---|---|---|
| Hunter | 1 | 4 |
| Warrior | 0 | 7 |
| Sentinel | 2, both over the open sea | 3 |
| Cherry | 0 | 7 |
| Oakwood | 6 | 5 |
| Coniferous | 2 | 5 |
| **Total** | **11** | **31** |

- Compline settings: 31 monuments, about one every 3.6 km² of the disc; each kind has 3 to 7, spread over the disc, so changing blessing stays a journey.
- The disc has almost no ice spikes (7 samples out of 27,600) and no desert (1 sample), so two monuments never appeared and a third was rare.
- On Tectonic's relief, the ground under a 70-block monument often varies by 30 to 100 blocks. Of the first 59 candidate sites with wider biomes, 39 were on a steep slope, 5 in water, 2 had a hill under a floating monolith and 1 would have gone through the ceiling of the world.

## What Compline changes

### Biomes and spacing (`pack/config/obscuria/ancient_remnants-common.toml`)

| Monument | Biomes | Spacing / separation (chunks) |
|---|---|---|
| Hunter | `#minecraft:is_badlands`, `#minecraft:is_savanna`, desert; RU dry bushland, outback, baobab savanna, Joshua and saguaro deserts, steppe | 44 / 11 |
| Warrior | snowy plains, ice spikes, snowy taiga, snowy slopes; RU tundra, frozen tundra, frozen pine taiga, cold boreal taiga | 40 / 10 |
| Sentinel | meadow, windswept hills, gravelly hills and forest; RU highland fields, rocky meadow, pine slopes, mauve hills | 40 / 10 |
| Cherry | cherry grove, flower forest; RU magnolia woodland, wisteria grove, orchard, poppy fields, flower fields | 22 / 6 |
| Oakwood | forest, dark forest, birch forests; RU deciduous, old growth, maple, autumnal maple, silver birch, temperate grove, willow forest | 64 / 16 |
| Coniferous | `#minecraft:is_taiga`, grove; RU boreal, blackwood, old growth and golden boreal taigas | 36 / 9 |

The lists follow the lore of each codex page (desert hunters, legends of the cold north, secluded monks, springs and blossoming trees, forest peoples, taiga and mountain dwarves). The codex reads its biome hints from the same tags, so it names the right biomes.

### Only on suitable ground (custom mod `compline`, module `monuments`)

`compline:terrain_checked` is a structure type that wraps another structure. AR's six structures are redefined with it in `pack/kubejs/data/ancient_remnants/worldgen/structure/`, with the original jigsaw definition inside, so their identifiers do not change (`/locate structure ancient_remnants:…`, the music, the Streams Reflowing tag). The monument is first assembled as usual; the ground under its footprint plus 4 blocks is then read every 8 blocks from the noise, before any block is generated. A site that fails gives no monument in that cell.

| Rule | Monoliths | Monuments |
|---|---|---|
| `max_height_difference` (highest minus lowest ground) | 40 | 26 |
| `max_water_share` | 0 | 5% |
| `min_clearance` (ground stays this far under the floating base) | 6 | — |
| `top_margin` (under the ceiling of the world) | 4 | 4 |

The values sit in the structure files and change without recompiling. A refused site is logged at `DEBUG` level. With these values, about half of the candidate sites are refused, mostly for slope. The check runs once per candidate cell, during structure placement; `/locate` uses it too.

### Spawners

The processor lists `ancient_remnants:monolith` and `ancient_remnants:monument` turn the spawner into chiseled tuff.

## What the monuments are for

1. **A choice of path.** The blessing is a lasting trait (one at a time, kept after death). Changing it means travelling to another monument. The floating monoliths keep their Elderheart about 86 blocks up: reaching it is a building or airship goal.
2. **Sleeping hearts and their guardians** (decision 15 of [12.5](../archive/plan/12-verification.md), report 6). Four Elderhearts are asleep: they give nothing until their guardian is beaten. The guardian rises on the ground, between the player and the monument, when a player comes within 64 blocks; at its death the heart awakens and blesses as usual. A guardian that disappears without dying can come back after a minute. When the coniferous monument wakes, each player within 64 blocks also gets a diamond template ([progression](progression.md)).

   | Monument | Guardian |
   |---|---|
   | Warrior monolith | Ferrous Wroughtnaut (Mowzie's Mobs) |
   | Coniferous monument | Frostmaw (Mowzie's Mobs) |
   | Hunter monolith | Umvuthi, the sun chief of the savanna tribe (Mowzie's Mobs) |
   | Oakwood monument | Sacred Pontiff (Companions!) |
   | Cherry monument, sentinel monolith | None: the monument of life and the peaceful monks |

   Report 6 had the Sculptor for the hunter. Umvuthi is taken instead: he leads a hunting tribe of hot lands, which is the hunter page's story, and he is a fight that ends with a death the script can see, whereas the Sculptor's trial ends without one. The guardians drop nothing more than before ([tri-du-pack](tri-du-pack.md): their weapons and armour are out of the loot).
3. **A life shard** on each player's first visit to an Elderheart ([sante](sante.md)), and the monument music ([musique](musique.md)).
4. **The codex as a trail.** Research scraps also come from ruins and wrecks, so the codex fills up by exploring and points to the biomes where each monument stands (no coordinates, in keeping with the atlas):

   | Table | Chance |
   |---|---|
   | `chests/simple_dungeon` | 25% |
   | `chests/abandoned_mineshaft` | 10% |
   | `chests/ruined_portal` | 20% |
   | `chests/igloo_chest` | 50% |
   | `chests/shipwreck_map` | 40% |
   | `chests/underwater_ruin_big` / `_small` | 30% / 15% |
   | `chests/jungle_temple` / `desert_pyramid` | 50% / 25% |
   | `archaeology/trail_ruins_common` (replaces the find) | 5% |

   The archaeology tables already used by [sante](sante.md) are left alone, since both replace the find.

## Files

| File | Role |
|---|---|
| `mods-custom/compline/…/monuments/` | `MonumentsModule`, `TerrainCheckedStructure` (compline 0.9.0) |
| `pack/config/obscuria/ancient_remnants-common.toml` | Biomes, spacing, separation |
| `pack/kubejs/data/ancient_remnants/worldgen/structure/*.json` | The six structures wrapped in `compline:terrain_checked` |
| `pack/kubejs/data/ancient_remnants/worldgen/processor_list/*.json` | Spawner → chiseled tuff |
| `pack/kubejs/server_scripts/monuments.js` | Sleeping hearts, guardians, codex pages in loot |
| `pack/kubejs/assets/compline/lang/*.json` | `compline.monument.*` messages |

How it works: the Elderheart keeps its blessing in the NBT key `Blessing`; without it, it blesses nobody. When a guarded heart enters the world, the script moves that value into its persistent data (`compline_benediction`) and removes the key; on the guardian's death it writes it back and marks the heart `compline_eveille`. The guardian carries the tag `compline_gardien` and its heart's UUID. If the heart is not loaded when the guardian dies, its UUID waits in the server's persistent data and the heart awakens when it loads.

## Verified on the test server (2026-11-13, no player)

- The census above, before and after, on fresh worlds of seed `compline`.
- A warrior monolith generated in full: no spawner left, its chiseled tuff in place; its Elderheart about 100 blocks above the ground, without `Blessing`, its blessing kept in `compline_benediction`.
- The four guardians created at the foot of their monument (Wroughtnaut, Frostmaw, Umvuthi, Sacred Pontiff); a Wroughtnaut killed: its heart gets `Blessing` back and `compline_eveille`. No KubeJS error.
- Not testable without a player: the guardian rising as a player approaches, the messages, the blessing itself, the codex pages in loot.

## Still to see in game

- The look of a monument on the 26-block limit, and of a monolith over a valley.
- Each guardian's fight where it rises, Umvuthi without his throne, the Frostmaw awake or asleep.
- The counts on the final disc: the census above is on the current terrain, before the disc. To be run again once the disc is in `main`.
