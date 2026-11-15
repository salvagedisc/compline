# Pack sort: mods, creatures, items and blocks

Request from the maintainer, 2026-10-29: check every mod, creature, item and block of the pack and decide what to disable, remove or change. The inventory was read from the 153 jars (blocks, items, creatures, structures, recipes, default configs), then checked against the registry of a test server (7,557 items). The wildlife sort of [doublons.md](doublons.md) and the animation sort (PR #19) are not reopened. Choices made by the maintainer on 2026-11-01: every proposal accepted, except Chipped, which leaves the pack entirely.

**Nothing here has been seen in game yet**: the test server loads every script without error, the in-game checks are TR1 to TR12 below, also in [tests-en-jeu.md](../tests-en-jeu.md).

## Mods removed

| Mod | Why |
|---|---|
| Chipped (and Athena, which only Chipped needed) | 6,980 blocks, 58 % of the pack's blocks, including 1,110 Nether and End variants nothing can supply; heavy loading |
| Beautify! | Almost everything overlaps another mod (Supplementaries chandeliers and rope, Amendments hanging pot, Handcrafted books); it also adds a botanist villager selling decor |

Chunky left the pack on 2026-11-14 ([0026](../decisions/0026-second-cleanup.md)): the world is pregenerated with `/dh pregen`, or a server owner adds Chunky to the server (the `horizon` fix for it stays).

## Defects fixed

| # | Defect | Fix |
|---|---|---|
| 1 | Aeronautics levitite could not be made: its powder came only from crushed end stone | Crushing recipe on calcite (half returned), `tri_recettes.js` |
| 2 | The Farm & Charm mincer tripled ores (iron ore → 3 raw iron) and gave stone and wood without breaking anything | Only its meat recipes (MEAT, 8), flour, raw pasta and melon slices are kept: 13 of 59 |
| 3 | The Kaleidoscope Compat setting that disables the Farm & Charm cooking pot recipes was never shipped | `config/kaleidoscope_compat.jsonc` |
| 4 | About 30 of the 46 Immersive Enchanting cost files asked for Nether or End items | 28 files rewritten in `kubejs/data/immersiveenchanting/` with Create parts, blocks and sea loot; Soul Speed disabled (no soul sand) |
| 5 | Twigs rhyolite was generated everywhere | Its biome modifier set to `neoforge:none`; Twigs pebbles are kept (the first gestures use them) |
| 6 | Mowzie's Wroughtnaut and Frostmaw structures were active, while the monument guardians must stay unique | Empty structure sets |
| 7 | Five Companions! structures bring vanilla monsters (factory, bear tower, Baba Yaga's hut, imp tent, the Pontiff's temple) | Empty structure sets |
| 8 | In Control! targeted `alexsmobs:cave_centipede`, which does not exist | `alexsmobs:centipede_head` |
| 9 | The Supplementaries globe showed coordinates | `show_coordinates = false` |
| 10 | Quark's Food Tooltips showed hunger and saturation; a dead `Terrablender Add Region` key | Disabled, key removed |
| 11 | Icy's Better Horses: Paint at maximum bond marks structures, the harness glides like an elytra, the Andalusian saves from a death | `paint_structures`, `stabilizer` and `andalusian_save` set to `no` in `config/icys-better-horses.json` |

## Frozen before the permanent world

- **Every Compat**: `config/everycomp-hazardous.toml` blacklists Quark's woods, Ghost's haunted wood and leaves, and the Twigs module. Every Compat registers its blocks at startup: a block removed after the world exists turns into air.
- **Steam 'n' Rails**: `registerMissingTracks = false` written in `config/railways-common.toml` (already the default) so that the 534 compatibility tracks stay out even if the default changes.
- **Where Winds Blow**: grass halved (dense, medium and sparse 4.0, tall 2.0, fern 1.0, patches 0.5), wild wheat off (Farm & Charm has its own).
- **Supplementaries galleon**: off.
- **Mowzie's Umvuthana**: groves cut (a masked tribe attacking in groups), the monastery kept (a rare, enigmatic sun temple).

## Settings

| Mod | Change |
|---|---|
| Quark | Off: Thatch, Variant Bookshelves, More Brick Types, Shingles, Compressed Blocks, Industrial Palette, Nether Brick Fence Gate (duplicates of Twigs, Create, Farm & Charm and, at the time, of No Man's Land), Automatic Recipe Unlock (knowledge must be found), Emotes, Camera (Exposure is the pack's camera). Framed Glass is kept now that Chipped is gone |
| Supplementaries | Off: sack (Reliable Backpacks), sconces (they were off while No Man's Land had its own; enabled again on 2026-11-13), gravel bricks (Twigs), netherite door and trapdoor, pancake, candy, altimeter (height in numbers), slice map (a cave radar). Flute range 24 blocks instead of 64. Rope arrow carries 8 ropes instead of 32 |
| Amendments | Off: thrown fire charges, dragon charge, skull piles and skull candles |
| Overgeared | Dragon breath arrow recipe off |
| Exposure | Interplanar projector off (`projecting_enabled = false`): it loads images from the Internet |
| Ecliptic Seasons | Farm & Charm and hamster crops now follow the seasons: barley, oat, strawberry, green beans in spring and summer; corn and tomato in summer and autumn; lettuce and onion in spring and autumn; cucumber in summer |
| Naturalist | Lions, tigers, brown and black bears attack on sight at night and no rule covered them: In Control! now caps each species at 2 per player in the Overworld, so they stay rare rather than changing their behaviour |

## Items removed

`kubejs/server_scripts/tri.js` removes the recipes of 313 items and takes them out of every loot table; `client_scripts/tri.js` hides the same list from EMI (the `kubejs-lists` test checks that both lists match).

| Mod | Items |
|---|---|
| Create, Aeronautics, Simulated | Potato cannon and mounted potato cannon, worldshaper, plunger launcher, creative physics staff, netherite backtank and diving gear, crushed ores of the 9 metals absent from the pack, experience nugget and block |
| Overgeared | Nether alloy furnace, netherite alloy, Nether tool cast, heated silver, tier A and B anvils |
| Farmer's Delight, Cold Sweat | Nether salad, crimson and warped cabinets, netherite knife; hoglin hide and armour (Kaleidoscope Cookery's Nether dishes and furniture until 2026-11-15) |
| Exposure, Envelope, Amendments, Lootr | Interplanar projectors; crimson and warped pigeonholes, charred pigeon egg; dragon charge; Lootr shulker and trophy |
| Supplementaries | The items switched off above |
| Handcrafted | 19 trophies (12 of creatures that no longer exist), the oven, berry jam, 30 crimson and warped furniture pieces |
| Twigs | 11 tables (Handcrafted has them), bloodstone and other Nether blocks, duplicates of vanilla tuff, rhyolite (no longer generated). Its cobblestone bricks (with the mossy and cracked ones, slabs, stairs and walls) were removed on 2026-11-01 as duplicates of No Man's Land's; they are back since 2026-11-13 |
| Alex's Mobs | Dimensional carver and its shards, void worm items, echolocator, endolocator and fish finder, tarantula hawk elytra and wings, the vanilla elytra, items and spawn eggs of the Nether and End creatures, 13 technical items |
| Companions! | Recall platform, respawn totem, Nether and End coins, netherite chains and dagger, soul gem and furnace, shade altars, mage, holy robe and crystallized blood gear, spell books, arms |
| Mowzie's Mobs | Boss weapons and armour (Wroughtnaut axe and helmet, earthrend gauntlet, sculptor staff, sol visage, geomancer set): the guardians give their blessing and nothing else; technical items. The ice crystal stays |
| Other wildlife mods | Hybrid Aquatic `uni`, Naturalist silverfish and endermite, netherite hamster armour and trim, Critters and Companions dragonfly wing and acorn hat (the hamster's stays) |

### Spelunkery and Archie's Dinosaurs

Added on 2026-11-13 ([0021](../decisions/0021-spelunkery-and-dinosaurs.md)) and sorted by the same rules, in the same two scripts (`TRI_OBJETS_SPELUNKERY`, `TRI_OBJETS_DINOSAURES`).

| Mod | Removed | Why |
|---|---|---|
| Spelunkery | Nephrite charm, carved nephrite, nephrite siphon, fountain and diode | They store and give back experience ([0009](../decisions/0009-xp.md)); nephrite stays a stone to build with |
| Spelunkery | Depth gauge, magnetic compass | Height and position in numbers, like the altimeter and the globe's coordinates |
| Spelunkery | Tuning fork, echo fork | They locate amethyst geodes and creatures through walls, like the echolocator |
| Spelunkery | Compression blast miner, sulfur and its block, End stone pebble, crown of fools, eggplant, iron pick on a stick | Netherite, soul sand valleys, the End and the Wither are out of reach ([0004](../decisions/0004-structures.md)); the last two are unfinished items without a source. The miner also breaks bedrock |
| Spelunkery | Raw lead, silver, bismuth and tin nuggets, bismuth nugget, jade shards, the 12 lead, silver and jade ores, copper nugget | Metals and mods the pack does not have; Create's copper nugget is the one in use |
| Spelunkery | Recipe `spelunkery:gunpowder` | It asked for sulfur |
| Archie's Dinosaurs | Netherite theri claws | Netherite |

Kept on purpose: the rope ladder (tangle roots and sticks; it rolls down like scaffolding and does not lift loads, so the pulley keeps its place), the parachute (it breaks a fall, it does not fly), the item magnet, the handheld compactor, Mine-o-mite, the trader Archie (his map is a blank one and his compass the vanilla one).

The ids were checked against the registry of the test server; the ore drops were read there too (`/loot spawn … mine`): a diamond ore gives a rough diamond, a deepslate iron ore raw iron and raw iron nuggets.

**Steam 'n' Rails** (about 1,110 entries): two patterns in the same scripts remove the recipes and hide from EMI the tracks of every wood except oak, spruce and birch (andesite, monorail and tieless tracks stay), the blackstone, ender and phantom tracks, and every dyed locometal and hazard stripe block. The paint brush still paints placed blocks; about 215 entries remain.

The Overgeared plates stay next to Create's sheets: the iron and copper armour of the Overgeared anvil asks for `overgeared:iron_plate` by item, not by tag.

## Recipe levels

The maintainer asked whether a mod changes how these levels play; otherwise we do it ourselves. Research of 2026-11-01 (Modrinth, CurseForge, jars):

- **Forge**: Overgeared 1.6.19 already overrides 43 vanilla tool and armour recipes (forged heads, plates, steel for diamond). The only metal objects still made at the workbench were the kitchen knives (Kaleidoscope's, Farmer's Delight's since 2026-11-15): iron now takes an Overgeared plate, gold a forged gold blade, diamond goes through the smithing table with the Overgeared diamond template, like every diamond tool. Hybrid Aquatic coral and seashell tools stay (sea material, not metal); mod armours are made of creature parts and stay.
- **Rope and climbing**: no mod replaces Supplementaries' rope. Rope costs more (3 flax for 2 ropes instead of 3), the rope arrow carries 8, and the pulley needs a Create cogwheel instead of an iron ingot, so loads cannot be lifted before the workshop. Pick Climber and Yori3o's Grappling Hooks can be tried later.
- **Cooking**: no mod makes the furnace or the campfire less efficient. Cooked meats stay as they are; Well Seasoned and Cook Your Food can be tried later.

## Checks on the test server

- 17/17 server scripts loaded, no error; the empty structure sets and the new recipes raise no warning.
- Checked in the loaded recipe list: the 7 `compline:tri/*` recipes are present, the old rope, pulley and knife recipes are gone, 13 mincer recipes remain, no recipe makes a jungle track, a dyed locometal block, a potato cannon or a Twigs table; oak tracks and plain locometal still have theirs.
- Every edited setting survives NeoForge's config correction pass (partial files are completed, our values are kept).
- The registry has 7,557 items without Chipped and Beautify!; the ids of every list come from it.

## In-game tests

1. **TR1** EMI: no Chipped or Beautify! entries, no Twigs table; Steam 'n' Rails shows oak, spruce, birch, andesite, monorail and tieless tracks and plain locometal only.
2. **TR2** Every Compat: no variant made from Quark's or Ghost's woods, no Twigs module; note how many `everycomp` entries EMI shows (about 135 expected).
3. **TR3** Levitite: calcite in a Create crusher gives levitite powder and calcite back; the levitite blend can then be made.
4. **TR4** Mincer: iron ore, cobblestone and logs are refused; beef still gives minced beef, wheat flour.
5. **TR5** Immersive Enchanting: Breach I asks for a propeller, Protection IV for 4 sturdy sheets; Soul Speed is not offered.
6. **TR6** Rope: 3 flax give 2 ropes, a rope arrow holds 8; the pulley needs a cogwheel; the iron kitchen knife needs a forged plate, the diamond one the smithing table and the Overgeared template.
7. **TR7** Seasons: corn and tomatoes do not grow in spring, cucumbers only in summer.
8. **TR8** Naturalist: around one player, never more than 2 lions, tigers, brown or black bears of each species.
9. **TR9** No coordinates on the Supplementaries globe, no food values in Quark tooltips, no galleon at sea.
10. **TR10** Icy's Better Horses: Paint at maximum bond does not mark structures, the harness does not glide, the Andalusian does not save from a death.
11. **TR11** Structures: no Wroughtnaut chamber, Frostmaw, Umvuthana grove or Companions! structure in a new world; the Umvuthana monastery still exists.
12. **TR12** To decide in game: does the Hybrid Aquatic depth charge break blocks, what do the ominous conch and hook summon, are Ghost's ghosts and the giant crabs hostile and where, does the Naturalist whistle teleport like Icy's.
13. **TR13** Spelunkery in EMI: no nephrite charm, siphon or fountain, no depth gauge, magnetic compass, tuning fork or echo fork, no sulfur, no lead, silver or jade ore; gunpowder has no recipe with saltpeter. A diamond ore gives a rough diamond, the grindstone polishes it.
14. **TR14** To decide in game: is the rope ladder too cheap next to the rope, does the echo of the forks deserve to come back.
