> Archived since 2026-11-13: No Man's Land left the pack ([decision 0019](../../decisions/0019-leaving-no-mans-land.md)). This page is kept as history.

# No Man's Land wildlife goes back to vanilla

Request from the maintainer, 2026-10-19: No Man's Land's animals do not go with Fresh Animations; we keep the vanilla animals, and cut the other creatures and the items that no longer have a source. Everything below is read from the jars (NML 1.6.1, Mixed Litter 1.2.2 which it bundles, Fresh Animations v1.11, Wildex 3.0.0, KubeJS 2101.7.2). **Nothing has been tested in game yet.**

## What NML does to the animals

- **Remodels**: Mixed Litter, bundled in NML, replaces the rendering of eight vanilla animals (pig, cow, mooshroom, chicken, sheep, rabbit, squid, glow squid). Fresh Animations no longer animates them.
- **Textures**: NML applies its own textures to eleven other vanilla animals (bee, cat, cod, dolphin, donkey, fox, goat, husk, salmon, slime, sea turtle), plus two wolf variants and one frog variant.
- **Creatures**: deer, goose, bar à serpe (a fish) and land tortoise, added by biome modifiers and by burrows in caves. NML's own biomes only list vanilla mobs.
- **Frienderman**: it replaces 1% of the natural endermen in the Overworld, with no setting in NML's config.
- The moose (`moose`) is not registered in 1.6.1; `buried`, `cave_carp` and `fat_joint` only have a language key.

## What Fallow does

| What | Where | How |
|---|---|---|
| Remodels cut | `pack/config/mixed_litter-startup.toml` | the six `[remodels]` keys set to `false` |
| NML textures cut | `pack/kubejs/data/nomansland/mixed_litter/variant_groups/` (11 files) | copy from the jar with `"unobtainable": true` at the head of the conditions |
| Golden and "timber" wolves, mud frog | `pack/kubejs/data/nomansland/tags/worldgen/biome/entity/` | tags emptied (`"replace": true`) |
| Deer, goose, tortoise, burrows | `pack/kubejs/data/nomansland/neoforge/biome_modifier/` | biome modifiers replaced by `neoforge:none` |
| Bass (bar à serpe) | `…/biome_modifier/spawns/add_spawns_to_swamps.json` | only the cod remains |
| Frienderman | `pack/kubejs/server_scripts/faune_nml.js` | `EntityEvents.spawned` cancelled: the enderman it replaces is already cancelled by NML, so nothing is left |
| 15 items with no source | `faune_nml.js` (server and client) | recipes removed, items hidden from EMI; creative mode remains an admin tool |
| 11 items removed (batch B) | `faune_nml.js` | recipes removed, items removed from all loot tables, hidden from EMI |
| Rabbit | `pack/kubejs/data/fallow/neoforge/biome_modifier/sans_lapin.json`, `sans_lapin.js` (server and client) | spawns removed from `#minecraft:is_overworld`; items removed from recipes, loot and EMI |
| `scute` offering line | `pack/kubejs/data/nomansland/nomansland/dialogue_pools/offering/scute.json` | impossible condition removed (see below) |
| Goose and tortoise offering lines | `…/offering/entity/goose.json`, `tortoise.json` | redirected to creatures that are present |

The 15 items: spawn eggs for the deer, goose, bass and tortoise; raw and cooked venison; raw, cooked, bucketed and barrelled bass; sturdy scute, shell and tortoise egg; the pot debug stick (`pot_debug_stick`) and the creative tab icon (`no_mans_globe`). The only recipes that consume these items also produce them.

We keep the Buddy (`buddy`, quest of the [Friendly Moon](lune-amie.md)), the living pot in the alchemists' ruins and the dreaming player.

**Batch B** (decision of the maintainer, 2026-10-19): horse meat (`raw_horse`, `horse_steak`), frog legs (`frog_leg`, `cooked_frog_leg`) and seven dishes (`pancake`, `maple_tart`, `sweet_tart`, `trail_mix`, `syruped_pear`, `honeyed_apple`, `hardtack`). NML adds them to vanilla loot through modifiers that draw on its own tables (`entities/horse_addition`, `chests/hardtack_addition`…): removing the item from all tables is enough.

**Explosives** (decision of the maintainer, 2026-10-29): NML's explosives come back, they had been removed in batch B along with a vanilla TNT recipe. NML's own items and recipes apply again: the explosive (paper, gunpowder, sand) thrown by hand, which mines ores (`explode_ore` advancement, restored); TNT from four explosives (`minecraft:tnt` overridden by NML); fire and ink bombs (gunpowder, string, resin or ink sac); the incendiary arrow (arrows and a bottle of resin oil). They are found again in NML's dungeon and pot loot.

**Rabbit** (same decision): Fresh Animations v1.11 has no model for it; without the remodel, it looked out of place. It is removed from the game. NML lists its biomes in `#minecraft:is_overworld`, so a single `neoforge:remove_spawns` biome modifier covers the whole Overworld. Its six items (egg, raw and cooked meat, stew, foot, hide) are taken out of the recipes and of all tables, including the cat's morning gift. Consequences: the potion of leaping can no longer be brewed (rabbit's foot), leather no longer comes from rabbit hides, Alex's Mobs' Bunfungus can no longer be born from a rabbit.

**`scute` line**: NML 1.6.1 makes it conditional on the item `nomansland:tortoise_scute`, which does not exist (the scute is called `sturdy_scute`). The lines are datapack registries, which skip an entry whose `neoforge:conditions` fail: it never loaded, and the Friendly Moon's test L8 failed. The override removes the condition and keeps the sea turtle and armadillo scutes. Test: `scripts/test/nml-dialogues.test.mjs`.

**Offering lines**: the tortoise's targets `naturalist:tortoise`, with the same text. The goose's targets Naturalist's duck (`naturalist:duck`), which lives in the same rivers and swamps, since Untitled Duck Mod is removed. The duck does not attack: « Bête bruyante et furieuse » ("Loud and furious beast") becomes « Bête bruyante et têtue » ("Loud and stubborn beast"), in English (`goose.json`) as well as in French. Test: `scripts/test/nml-dialogues.test.mjs`.

## Open questions

1. `tannerie_butin.js` still cites `nomansland:entities/moose` (not registered) and the deer table (no longer used). No effect in game.

## In-game tests

On a new world: registry data is read when the world is created.

| # | Test | Expected |
|---|---|---|
| F1 | Server and client logs at launch | No error on `mixed_litter`, `biome_modifier`, `variant_groups`, `faune_nml.js` |
| F2 | `/summon` pig, cow, mooshroom, chicken, sheep, squid, glow squid | Vanilla models animated by Fresh Animations |
| F3 | Forest, taiga, river, swamp, jungle, badlands, NML forests | No deer, goose, bass or tortoise; cod still in the swamps |
| F4 | Caves, including lush caves | No more tortoise burrows |
| F5 | Fox, goat, donkey, wolf in `autumnal_forest` | Vanilla textures; no golden or "timber" wolf, no mud frog |
| F6 | `/summon nomansland:frienderman` | It disappears immediately |
| F7 | EMI: search "venison", "bass", "tortoise", "rabbit", "horse", "pancake" | Nothing |
| F9 | Mushroom island | Rings and Buddy present; ascension (L10) unchanged |
| F10 | Desert, snowy plains, flower meadow, grove | No rabbit |
| F11 | Kill a horse, a frog; open dungeon, igloo and shipwreck chests | No horse meat, no legs, no pancake |
| F12 | Crafting table: paper, gunpowder and sand in a column, then four explosives in a square | One explosive, then one TNT; 5 gunpowder and 4 sand in the vanilla pattern give nothing |
| F15 | Throw an explosive at an ore; craft a fire bomb, an ink bomb and incendiary arrows | The ore drops, the `explode_ore` advancement appears in French; the three items work and have French names |
| F13 | Offer a sea turtle scute to the Friendly Moon (L8) | The line "A precious gift from a wise creature" |
| F14 | Show a Naturalist tortoise, then a Naturalist duck, to the Friendly Moon | The tortoise and goose lines |
