# The tannery

Idea 4 from Wayfarer ([research](recherche/2026-10-19/wayfarer.md), recommendation 4). Leather no longer drops ready-made from animals: it takes a hide, ashes and time. Leather becomes a small trade tied to wildlife, livestock and weather.

Status: validated on the test server on 2026-10-19 (scripts error-free, loot checked with `/loot spawn`, recipes: 4 added, 5 removed). Not yet tested with a client: tooltips, tray, EMI. Kaleidoscope Cookery 1.6.0 keeps the same tray format.

## The chain, for a player

1. **Get a hide.** A cow, horse, deer, moose or zebra no longer drops leather but a **raw hide**. The tooltip says what to do with it.
2. **Find ashes.** Supplementaries ashes are left behind when a wooden block burns, or when a creature dies in fire. The simplest way: put **charcoal on a campfire**, which yields a handful of ashes after 30 seconds.
3. **Flesh and salt.** At the crafting table, a raw hide and ashes give a **prepared hide**.
4. **Soak.** The prepared hide is placed on a **bamboo tray** (Kaleidoscope Cookery) and left to soak. The tray wets what it holds **in the rain**, or **under a dripstone stalactite** when a block of water lies above it, as when filling a cauldron. After 5 minutes of soaking, the hide becomes **leather**. A whole stack soaks at once.

To tan all year round, build a small tannery: water, below it a dripstone block with a stalactite, then the tray. Otherwise, wait for rain, which depends on the season (Ecliptic Seasons).

The rabbit has been removed from the game since 2026-10-19 ([faune-nml](faune-nml.md)): there is no more rabbit hide leather.

## The animals concerned

The leather, or the hide that used to smelt into leather, becomes a raw hide. The original quantities and Looting bonus stay the same.

| Mod | Animals | Loot table |
|---|---|---|
| Vanilla | cow, mooshroom, horse, donkey, mule, llama, trader llama | `minecraft:entities/cow`, `mooshroom`, `horse`, `donkey`, `mule`, `llama`, `trader_llama` |
| Vanilla | hoglin (Nether closed, no effect) | `minecraft:entities/hoglin` |
| Icy's Better Horses | all 15 horse breeds | their tables point to `minecraft:entities/horse` |
| No Man's Land | moose | `nomansland:entities/moose` |
| No Man's Land | deer (did not drop leather: hide added, 0 or 1, plus 0 to 1 per Looting level) | `nomansland:entities/deer` |
| Naturalist | deer, zebra (leather) | `naturalist:entities/deer`, `zebra` |
| Naturalist | boar, elephant, rhinoceros, Komodo dragon ("hide") | `naturalist:entities/boar`, `elephant`, `rhino`, `komodo_dragon` |
| Hybrid Aquatic | dugong, manatee | `hybrid_aquatic:entities/dugong`, `manatee` |
| Friends & Foes | Moobloom | `friendsandfoes:entities/moobloom` |
| Quark | foxhound (Nether, no effect) | `quark:entities/foxhound` |
| Alex's Mobs | kangaroo: keeps its hide, but two kangaroo hides give a raw hide instead of leather | recipe |

Checked and found to have no leather, hence unchanged: Critters and Companions, Adorable Hamster Pets, Mowzie's Mobs, Companions!, Ghost, Longwings, and the rest of Alex's Mobs (the bison and the bear give fur, which was not used for leather). Naturalist furs (bear, lion, tiger, mammoth…) also stay.

The script writes to the server log any creature table that still drops leather (`[tannerie] créature qui lâche encore du cuir`, i.e. "creature that still drops leather"). A new wildlife mod will show up there immediately.

## The recipes

Added (`pack/kubejs/server_scripts/tannerie.js`):

| Identifier | Where | Recipe |
|---|---|---|
| `fallow:tannerie/echarner_et_saler` | crafting table | raw hide (tag `#fallow:peaux_brutes`) + ashes (`#c:dusts/ash`) → prepared hide |
| `fallow:tannerie/tremper` | bamboo tray, wetting | prepared hide → leather, 6000 ticks |
| `fallow:tannerie/cendres_de_charbon` | campfire | charcoal → ashes, 600 ticks |
| `fallow:tannerie/peau_de_kangourou` | crafting table | 2 kangaroo hides → raw hide |
| `fallow:tannerie/fumer` (switched off) | campfire | prepared hide → leather, 2400 ticks; fallback if bamboo is too rare, to be switched on with `TANNERIE_SECOURS_FUMAGE = true` |

The `#fallow:peaux_brutes` tag (`pack/kubejs/data/fallow/tags/item/peaux_brutes.json`) contains the raw hide and the Naturalist hide, so that Naturalist hides from an existing world still work.

Removed, because they gave leather too easily or outside the chain:

| Recipe | What it did |
|---|---|
| `kaleidoscope_cookery:bamboo_tray/drying/rotten_flesh_to_leather` | rotten flesh dried in the sun → leather |
| `envelope:mailing/automated_supply_service/leather` | 6 rotten flesh sent by pigeon → leather |
| `naturalist:leather_from_smelting_hide` | Naturalist hide in the furnace → leather in 10 seconds |
| `alexsmobs:kangaroo_hide_to_leather` | 2 kangaroo hides → leather (replaced, see above) |
| `kaleidoscope_cookery:bamboo_tray/wetting/leather_to_rotten_flesh` | wet leather rots into rotten flesh. This is not a source, but the tray gave back tanned leather at the next rain if it stayed on it |

Kept, because they recycle leather that already exists or come from rare loot:
- four rabbit hides → leather (vanilla, recipe copied identically by Naturalist), no effect since the rabbit was removed;
- Create: saddle at the mill, horse armours at the crusher; Farm & Charm: leather horse armour at the chopper; Quark: bound leather block → 9 leathers;
- Cold Sweat: hoglin hide → 2 leathers (the Nether is closed, the hide cannot be found);
- leather in a few chests: lost Envelope letters, NML alchemists' pots, the loft of the illusioner's shack (Friends & Foes), vanilla fishing.

## Uses of leather

All keep `minecraft:leather` and do not change: Traveler's Backpack (52 recipes, including the bags and upgrades), Supplementaries (bellows), vanilla books, the Wildex bestiary, Quark (seed pouch, backpack), Alex's Mobs, Cold Sweat (waterskin), Hybrid Aquatic, Icy's Better Horses, Kaleidoscope Cookery (farmer's outfit, lunch bag), Companions!, Create (toolbox), Aeronautics (aviator goggles), NML (pouch, straw bed). None depends on a removed leather source.

Overgeared 1.6.19 has **no leather recipe**: its armours are forged from metal plates. "Plated armours" on a leather base are a Wayfarer idea, not an Overgeared one.

Leather is needed early: the Wildex bestiary (2 leathers) and the first book. This is the reason for the switched-off fallback described above.

## Why the bamboo tray

The Kaleidoscope Cookery tray is the only container in the pack that makes time pass on a placed item, with two modes: it dries what it holds in the sun and wets it in the rain or under a stalactite. It already made leather (with rotten flesh). It needs no new mod, ties into the weather and the seasons, and a stalactite tannery is a small build. The other routes are worse: a click on a water cauldron is instantaneous, Create's washing and depot come too late in the progression, and the Farm & Charm silo only dries.

## Files

- `pack/kubejs/startup_scripts/tannerie.js`: the `fallow:peau_brute` and `fallow:peau_preparee` items (vanilla texture `minecraft:item/rabbit_hide`, pinkish or grey tint, no PNG).
- `pack/kubejs/server_scripts/tannerie.js`: recipes, removals, tooltips, check of the remaining leather recipes.
- `pack/kubejs/server_scripts/tannerie_butin.js`: the loot (LootJS).
- `pack/kubejs/data/fallow/tags/item/peaux_brutes.json`: the raw hide tag.
- Texts: names and tooltips in French and English, to be merged into `pack/kubejs/assets/fallow/lang/`.

## What remains to be tested

At test server startup (KubeJS log):
- the two items register with `.texture()` and `.color(0, …)` (KubeJS 7); the merged language file takes precedence over the English name that KubeJS generates;
- `LootJS.lootTables`, `hasLootTable`, `getLootTable(...).replaceItem(...)`, `createPool`, `LootEntry.of(...).setCount([0, 1]).applyEnchantmentBonus([0, 1])` pass without error, and no `table absente` ("table missing") line appears;
- the `[tannerie] recette donnant du cuir` ("recipe giving leather") lines list only the recipes kept above, and `créature qui lâche encore du cuir` lists nothing;
- `event.custom` accepts the `kaleidoscope_cookery:bamboo_tray` recipe and `event.campfireCooking(...).cookingTime(...)` passes;
- `ItemEvents.modifyTooltips` in a server script does reach the client (KubeJS 7 synchronises these tooltips); `Text.gray(Text.translatable(...))` displays translated.

In game:
- kill a cow, an Icy's Better Horses horse and an NML deer: each drops a raw hide, with Looting III too;
- the crafting table accepts the raw hide and ashes, and also a Naturalist hide;
- charcoal on a campfire gives ashes;
- the prepared hide becomes leather on the tray in the rain, then under a water-fed stalactite; the leather no longer rots when it stays on the tray;
- the tray accepts a whole stack and returns as many leathers;
- EMI shows both steps and no longer the removed recipes;
- check that bamboo can be found without searching too long in an NML world (bamboo jungles), and that dripstone exists in the caves. If bamboo is too rare, switch on `TANNERIE_SECOURS_FUMAGE`;
- after the Kaleidoscope Cookery upgrade to 1.6.0, check that the `kaleidoscope_cookery:bamboo_tray` type and the `subtype` field have not changed;
- tune `TANNERIE_DUREE_TREMPAGE` (5 minutes) once played.
