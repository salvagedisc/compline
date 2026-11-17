# Ores and armour progression

The team's study of 2026-11-15, decided on 2026-11-17 ([0035](../decisions/0035-ore-progression.md)). The goal: stay on iron, then Overgeared steel, for a long while; make each step after it a place to reach rather than a lucky chest. Overgeared's quality (from poor to master) keeps the steel plateau alive: a masterwork steel chestplate beats a poorly forged diamond one.

Ladder, from the bench:

| Step | Armour (total) | Toughness | Tools (durability, speed) | How it is reached |
|---|---|---|---|---|
| Iron (Overgeared) | 15 | 0 | 250, 6 | Forge |
| Steel (Overgeared) | 17 | 1 per piece | 500, 7 | Alloy furnace: 3 iron + 1 coal → 3 crude steel |
| Black steel (`compline`) | 19 | 1.5 per piece | 900, 7.5 | Steel, Spelunkery magnetite and charcoal; master anvil (next step) |
| Diamond | 20 | 2 per piece | 1561, 8 | Diamonds near deep lava, the template from the coniferous monument, a black steel piece as base |
| Orichalcum (renamed netherite) | 20 + | 3 per piece | — | The burning heart (planned) |

Dinosaur armours are a side branch: forged at the anvil from plates, with quality, each set opening one part of the world (planned).

## Steel and diamonds become rare (first step)

- **No steel ingot or diamond template in vanilla chests.** Overgeared adds them to 28 loot tables (50 % steel in a mineshaft, 15 % template). The 26 modifiers that exist in 1.6.19 are overridden with a chance of 0 in `kubejs/data/overgeared/loot_modifiers/`.
- **Steel costs more iron**: 3 iron + 1 coal → 3 crude steel at the alloy furnace (was 4), in both alloy furnaces; the furnace takes four ingredients at most.
- **Diamonds only near lava.** The biome modifier `compline:diamants_de_la_lave` removes the four vanilla diamond features (`ore_diamond`, `_medium`, `_large`, `_buried`; Spelunkery's stone variants hang on them). What remains is Spelunkery's `diamond_ore_lava`: rough diamond blocks beside lava, from y −64 to 16. On the bench, 256 chunks: about 5,500 vanilla diamond ores before, 0 after; 20 rough diamond blocks (9 rough diamonds each) in both cases.
- **The diamond template is a place.** When the Elderheart of a coniferous monument wakes (its Frostmaw is dead), every player within 64 blocks gets one `overgeared:diamond_upgrade_smithing_template`, with a message (`monuments.js`, `MON_SAVOIR`). It is copied at the workbench as before (7 diamonds and the base metal), for friends who were not there.

Known ways around: Create drills and Mine-o-mite ignore tool tiers; villager trades and structure chests can still hold diamonds.

## In-game tests

- [ ] PG1. In a new world, `/locate` a deep cave: no diamond ore in plain stone or deepslate; rough diamond blocks next to lava below y 16.
- [ ] PG2. A mineshaft or dungeon chest never holds a steel ingot or a diamond template.
- [ ] PG3. Alloy furnace: 3 iron ingots + 1 coal give 3 crude steel.
- [ ] PG4. Kill the Frostmaw of a coniferous monument and wake its Elderheart: a diamond template and the gold message, for each player nearby; nothing at the other monuments.
