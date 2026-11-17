# Ores and armour progression

The team's study of 2026-11-15, decided on 2026-11-17 ([0035](../decisions/0035-ore-progression.md)). The goal: stay on iron, then Overgeared steel, for a long while; make each step after it a place to reach rather than a lucky chest. Overgeared's quality (from poor to master) keeps the steel plateau alive: a masterwork steel chestplate beats a poorly forged diamond one.

Ladder, from the bench:

| Step | Armour (total) | Toughness | Tools (durability, speed) | How it is reached |
|---|---|---|---|---|
| Iron (Overgeared) | 15 | 0 | 250, 6 | Forge |
| Steel (Overgeared) | 17 | 1 per piece | 500, 7 | Alloy furnace: 3 iron + 1 coal → 3 crude steel |
| Black steel (`compline`) | 19 | 1.5 per piece | 900, 7.5 | Steel, Spelunkery magnetite and charcoal; master anvil |
| Diamond | 20 | 2 per piece | 1561, 8 | Diamonds near deep lava, the template from the coniferous monument, a black steel piece as base |
| Orichalcum (netherite, renamed in French) | 20 | 3 per piece | 2031, 9 | Ancient debris beside deep lava, Overgeared's master forging, the template from Archie |

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

## The burning heart and orichalcum (fourth step)

Netherite comes to the Overworld, deep down, and is called **orichalque** in French (the team's choice: a name that does not point to a closed world). Only the French texts change: vanilla items, Overgeared's alloy, Starcatcher's upgraded rod (lava fishing still needs an orichalcum rod or the amethyst hook), EMI's tag names (`kubejs/assets/*/lang/fr_fr.json`); the English stays "netherite".

- **Ancient debris beside deep lava**, mined with a diamond pickaxe (black steel cannot: the chain is black steel, diamond, orichalcum). `compline:debris_pres_de_la_lave` (24 tries per chunk between y −64 and −40, kept only where lava lies within 2 or 3 blocks) places `compline:debris_du_coeur`: Overgeared-sized clusters of up to 3 ancient debris in stone, deepslate, magma, basalt or blackstone, never touching air. On the bench, 256 chunks: 99 ancient debris (about 25 ingots). Regions Unexplored's scorching caves were meant as a second source, but `/locate` finds none on the disc.
- **Overgeared's netherite chain is back**: 4 netherite scraps and 4 gold in the alloy furnace for netherite, now made from a blast furnace, deepslate bricks and magma blocks (« Four à alliage de magma »), the heated alloy hammered with a master-level minigame (a failure gives the scraps back). Removed from the sort lists in `tri.js`.
- **The template comes from Archie**: once the Obsidiceratops is tamed (it lives in the redstone caves), Archie trades one netherite upgrade template for 8 to 10 obsidian horns, once. It is copied at the workbench with a magma block instead of netherrack.
- **Heat**: the Obsidiceratops set (fire immunity, Cold Sweat heat bonus) is the way to stay down there; lava already heats the air in Cold Sweat. To be tuned in game.

## Mining: the forge shows in the yield (fifth step)

As a well-stirred pot gives more servings in the kitchen, a well-forged pickaxe gives more ore. The `compline` mod's `minage` module adds the loot modifier `compline:rendement_de_la_pioche`: an ore (`#c:ores`) broken with an Overgeared tool of expert, perfect or master quality gives each item of its drop one more piece, with a chance of 10, 20 or 30 %. Nothing with silk touch or on other blocks. On the bench, 150 iron ores with a master pickaxe gave 1.31 raw iron each (1.00 without quality).

Still to come in this step (the study's lot 3): veins that are places, with outcrops on the surface above them (Dynamic Ore Veins and Surface Samples, to be measured on the bench with Spelunkery). Panning already follows the biome: Spelunkery's sluices give gold in badlands, lapis in deserts, emeralds on jagged peaks.

Known ways around: Create drills and Mine-o-mite ignore tool tiers; villager trades and structure chests can still hold diamonds.

## In-game tests

- [ ] PG1. In a new world, `/locate` a deep cave: no diamond ore in plain stone or deepslate; rough diamond blocks next to lava below y 16.
- [ ] PG2. A mineshaft or dungeon chest never holds a steel ingot or a diamond template.
- [ ] PG3. Alloy furnace: 3 iron ingots + 1 coal give 3 crude steel.
- [ ] PG4. Kill the Frostmaw of a coniferous monument and wake its Elderheart: a diamond template and the gold message, for each player nearby; nothing at the other monuments.
- [ ] PG5. The black steel chain, the master anvil and the diamond rule: tests AN1 to AN5 of [mod-compline](mod-compline.md#black-steel).
- [ ] PG6. Forge an Obsidiceratops chestplate on the smithing anvil (5 horns, 3 steel plates): it has a quality; the workbench recipe is gone. Wear the full set in a hot place: Cold Sweat's heat stays bearable; the tooltip names the bonus.
- [ ] PG7. Full Chorusoatl set, sneak and press the armour key: no teleport, the « no ability » message; slow falling stays; the tooltip no longer offers the teleport.
- [ ] PG8. Deep below, beside a lava lake under y −40: ancient debris inside the rock, never in the open; smelted into scraps (« Fragments d'orichalque »).
- [ ] PG9. Craft the « Four à alliage de magma », alloy 4 scraps and 4 gold, hammer the heated alloy on a smithing anvil (master minigame): an orichalcum ingot. Archie sells the template after the Obsidiceratops is tamed.
- [ ] PG10. Mine iron ore with a master-quality pickaxe, then with a plain one: about a third more raw iron with the first; silk touch gives the ore block only.
