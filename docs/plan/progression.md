# Ores and armour progression

The team's study of 2026-11-15, decided on 2026-11-17 ([0035](../decisions/0035-ore-progression.md)). The goal: stay on iron, then Overgeared steel, for a long while; make each step after it a place to reach rather than a lucky chest. Overgeared's quality (from poor to master) keeps the steel plateau alive: a masterwork steel chestplate beats a poorly forged diamond one.

Ladder, from the bench:

| Step | Armour (total) | Toughness | Tools (durability, speed) | How it is reached |
|---|---|---|---|---|
| Iron (Overgeared) | 15 | 0 | 250, 6 | Forge |
| Steel (Overgeared) | 17 | 1 per piece | 500, 7 | Alloy furnace: 3 iron + 1 coal → 3 crude steel |
| Black steel (`compline`) | 19 | 1.5 per piece | 900, 7.5 | Steel, Spelunkery magnetite and charcoal; master anvil |
| Diamond | 20 | 2 per piece | 1561, 8 | Diamonds near deep lava, the template from the coniferous monument, a black steel piece as base |
| Orichalcum (renamed netherite) | 20 + | 3 per piece | — | The burning heart (planned) |

Dinosaur armours are a side branch: forged at the anvil with steel plates, with quality, each set opening one part of the world.

## Steel and diamonds become rare (first step)

- **No steel ingot or diamond template in vanilla chests.** Overgeared adds them to 28 loot tables (50 % steel in a mineshaft, 15 % template). The 26 modifiers that exist in 1.6.19 are overridden with a chance of 0 in `kubejs/data/overgeared/loot_modifiers/`.
- **Steel costs more iron**: 3 iron + 1 coal → 3 crude steel at the alloy furnace (was 4), in both alloy furnaces; the furnace takes four ingredients at most.
- **Diamonds only near lava.** The biome modifier `compline:diamants_de_la_lave` removes the four vanilla diamond features (`ore_diamond`, `_medium`, `_large`, `_buried`; Spelunkery's stone variants hang on them). What remains is Spelunkery's `diamond_ore_lava`: rough diamond blocks beside lava, from y −64 to 16. On the bench, 256 chunks: about 5,500 vanilla diamond ores before, 0 after; 20 rough diamond blocks (9 rough diamonds each) in both cases.
- **The diamond template is a place.** When the Elderheart of a coniferous monument wakes (its Frostmaw is dead), every player within 64 blocks gets one `overgeared:diamond_upgrade_smithing_template`, with a message (`monuments.js`, `MON_SAVOIR`). It is copied at the workbench as before (7 diamonds and the base metal), for friends who were not there.

## Black steel (second step)

Overgeared has no black steel; it is the pack's own, after waccyy's Fantasia, and the `compline` mod's `acier_noir` module ([mod-compline](mod-compline.md#acier_noir-module)).

- **Raw black steel**: a steel ingot, two Spelunkery raw magnetite (from its magnetite geodes, obsidian shells deep underground) and a charcoal at the alloy furnace. Heated in a blast furnace, hammered on any smithing anvil into a heated ingot, cooled in water. Nine ingots make a block.
- **The master anvil**: Overgeared's tier A anvil is turned on (`overgeared-common.toml`, `enableTierA`) and named « Enclume de maître ». It is made by sneaking and right-clicking a block of black steel with a smithing hammer, the only block in `overgeared:tier_a_anvil_bases`. Plates (1 ingot → 2), the four heads, the sword blade and the armour are forged there only, with Overgeared's quality; tool heads need the tool's blueprint, as for steel.
- **Diamond needs black steel.** Diamond ores and Spelunkery's rough diamond block (`compline:needs_black_steel_tool`) are refused to every tier below black steel, steel included. Steel can now mine obsidian, which Overgeared had refused to it with everything that needs a diamond tool.
- **Diamond pieces start from black steel**: the ten smithing recipes take a black steel piece instead of a steel one, and copying the template takes a black steel ingot.

## Dinosaur armour (third step)

Archie's Dinosaurs armour is a side branch, not a step above steel: 10 to 18 armour, no toughness, each full set with its power. It is forged now, no longer crafted: the 24 workbench recipes are replaced (same ids, `kubejs/data/ec_ad/recipe/`) by Overgeared forging recipes on the smithing anvil, with quality. Each piece takes fewer of the creature's parts and some steel plates: helmet 4 parts and 1 plate, chestplate 5 and 3, leggings 6 and 1, boots 2 and 2 (17 parts and 7 plates for a set, was 24 parts). The stats are the mod's (its server config `armor_settings`), Overgeared's quality adds to them.

Each set opens part of the world, in the spirit of Breath of the Wild, through Cold Sweat set bonuses (`kubejs/data/compline/cold_sweat/item/insulator/`, the tooltip says it):

| Set | Opens | Cold Sweat bonus (full set) | The mod's own power |
|---|---|---|---|
| Obsidiceratops | The burning heart (lava, heat) | Burning point +1.0, heat dampening 0.5 | Walks on lava, immune to fire |
| Chorusoatl | Heights and cold (the Elderheart of a floating monolith, about 86 blocks up) | Freezing point −1.0 | Slow falling; **its 45-block teleport is removed** (`compline` mixin `dinos.ChorusoatlArmorMixin`) |
| Coralosaur | The depths | Water no longer chills, freezing point −0.4 | Full ocean adaptation, allied guardians |
| Therimoss | Arid lands and long roads | Burning point +0.6 | Less hunger, burst of speed |
| Sculksaur | Ancient cities in silence (the Warden is kept there only) | — | Silent steps, sound decoy |
| Lushsaur | Shy wildlife (zoo photos) | — | Camouflage when sneaking |

Known ways around: Create drills and Mine-o-mite ignore tool tiers; villager trades and structure chests can still hold diamonds.

## In-game tests

- [ ] PG1. In a new world, `/locate` a deep cave: no diamond ore in plain stone or deepslate; rough diamond blocks next to lava below y 16.
- [ ] PG2. A mineshaft or dungeon chest never holds a steel ingot or a diamond template.
- [ ] PG3. Alloy furnace: 3 iron ingots + 1 coal give 3 crude steel.
- [ ] PG4. Kill the Frostmaw of a coniferous monument and wake its Elderheart: a diamond template and the gold message, for each player nearby; nothing at the other monuments.
- [ ] PG5. The black steel chain, the master anvil and the diamond rule: tests AN1 to AN5 of [mod-compline](mod-compline.md#black-steel).
- [ ] PG6. Forge an Obsidiceratops chestplate on the smithing anvil (5 horns, 3 steel plates): it has a quality; the workbench recipe is gone. Wear the full set in a hot place: Cold Sweat's heat stays bearable; the tooltip names the bonus.
- [ ] PG7. Full Chorusoatl set, sneak and press the armour key: no teleport, the « no ability » message; slow falling stays; the tooltip no longer offers the teleport.
