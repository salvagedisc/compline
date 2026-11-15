# The tannery

Idea 4 from Wayfarer ([research](../archive/plan/recherche/2026-10-19/wayfarer.md), recommendation 4). Leather no longer drops ready-made from animals: it takes a hide, bark and time. Since 2026-10-29 the chain is the [Tannery: Better Leather](https://modrinth.com/mod/tannery-better-leather) mod (Beckstation, 1.0.2, All Rights Reserved, used as published), which replaced the pack's own scripted chain (raw hide, ashes, bamboo tray): decision [0014](../decisions/0014-tannery.md).

Status: checked on a test server on 2026-10-29 (NeoForge with KubeJS, LootJS, Tannery and No Man's Land, which has since left the pack, [0019](../decisions/0019-leaving-no-mans-land.md)): eight kills of each vanilla animal give the expected hides, meat, wool and fat; 28 logs and woods give bark, stripped ones do not. Not yet tested with a client: the rack, the barrel, EMI, the translated barrel messages.

## The chain, for a player

1. **Get a hide.** A cow, sheep, goat, pig or horse, and every pack animal that used to give leather, drops one of Tannery's five hides (cow, sheep, goat, pig, horse). Cows, mooshrooms, pigs, sheep and goats also drop a piece of **animal fat**.
2. **Dry it.** Right-click a **tanning rack** (sticks, planks and a hide) or a hanging rack (wooden slabs and a hide) with the hide. It dries in the air: the rack checks every minute, with one chance in four, so about four minutes. The result is a **dry hide**, whatever the animal.
3. **Bark.** Stripping a log with an axe drops **bark** half of the time. Two barks make **tannic powder**. A **tanning barrel** (planks and tannic powder) holds powder for several hides.
4. **Tan.** Right-click the barrel with a dry hide: it becomes **leather**.

Rotten flesh follows the same path: dried on the rack it becomes **dry flesh**, which gives two **sinews** (they count as string, tag `c:strings`), **jerky** in the smoker, or leather in the barrel.

Further on, with Tannery's own items: hardened leather (leather in the furnace), treated leather (leather, tannic powder, fat), the tanner's table (planks, hardened leather, iron) which forges nails from iron nuggets and studded leather from nails and hardened leather, and the studded leather armour. Fat makes two torches with a stick, or a block of fat. Leather and an iron ingot make a saddle.

The rabbit has been removed from the game since 2026-10-19 ([wildlife page, archived](../archive/plan/faune-nml.md)): there is no more rabbit hide leather.

## The animals concerned

Tannery's rack only takes its own five hides, so each animal gets the closest one. All of them dry into the same dry hide. The original quantities and Looting bonus stay the same (`pack/kubejs/server_scripts/tannerie_butin.js`).

| Hide | Animals | Loot table |
|---|---|---|
| Cow | cow, mooshroom, Naturalist elephant and rhinoceros, Hybrid Aquatic dugong and manatee, Friends & Foes Moobloom | `minecraft:entities/cow`, `mooshroom`, `naturalist:entities/elephant`, `rhino`, `hybrid_aquatic:entities/dugong`, `manatee`, `friendsandfoes:entities/moobloom` |
| Horse | horse (and the 15 Icy's Better Horses breeds), donkey, mule, llama, trader llama, Naturalist zebra | `minecraft:entities/horse`, `donkey`, `mule`, `llama`, `trader_llama`, `naturalist:entities/zebra` |
| Goat | goat (added: 0 or 1, plus 0 to 1 per Looting level), Naturalist deer, Komodo dragon, Quark foxhound (Nether, no effect) | `minecraft:entities/goat`, `naturalist:entities/deer`, `komodo_dragon`, `quark:entities/foxhound` |
| Pig | pig (added), Naturalist boar, hoglin (Nether, no effect) | `minecraft:entities/pig`, `naturalist:entities/boar`, `minecraft:entities/hoglin` |
| Sheep | sheep (added) | `minecraft:entities/sheep` |

Alex's Mobs kangaroo: two kangaroo hides make a goat hide (`compline:tannerie/peau_de_kangourou`). A Naturalist hide left over from an existing world makes a cow hide (`compline:tannerie/peau_de_naturalist`).

Tannery ships its own tables for the cow, pig, sheep, goat, horse and donkey. They drop the Looting bonus and the cooked meat of an animal killed by fire. `pack/kubejs/data/minecraft/loot_table/entities/` puts the six vanilla 1.21.1 tables back (KubeJS data loads after the mods), and the script then adds the hides and the fat. No other mod of the pack replaces these six tables.

The script writes to the server log any creature table that still drops leather (`[tannerie] créature qui lâche encore du cuir`, i.e. "creature that still drops leather"). A new wildlife mod shows up there immediately.

## Bark from every wood

Tannery only gives bark for the eight vanilla logs in its `tannery:logs` block tag, and it gives bark on any block of that tag, stripped or not. `pack/kubejs/server_scripts/tannerie.js` adds every log, wood, stem or hyphae that has a `stripped_` version (28 blocks on the test server of 2026-10-29, with No Man's Land's woods; Regions Unexplored's woods now take their place, count to read in the server log), and never a block that is already stripped: otherwise a repeated right-click on the same block would give endless bark. The count is written to the server log (`[tannerie] N bûches donnent de l'écorce`).

Since 2026-11-15 Farmer's Delight's tree bark is merged into Tannery's (`fusions.js`, [0030](../decisions/0030-farmers-delight-kitchen.md)): a log cut with a knife on the cutting board, or sawn by Create, also gives Tannery's bark. Naturalist's fat (boar, whale) is Tannery's animal fat.

## The recipes

Added or changed (`pack/kubejs/server_scripts/tannerie.js`):

| Identifier | Recipe |
|---|---|
| `compline:tannerie/peau_de_kangourou` | 2 kangaroo hides → goat hide (only with Alex's Mobs) |
| `compline:tannerie/peau_de_naturalist` | Naturalist hide → cow hide (only with Naturalist) |

Removed, because they give leather outside the tannery:

| Recipe | What it did |
|---|---|
| `envelope:mailing/automated_supply_service/leather` | 6 rotten flesh sent by pigeon → leather |
| `naturalist:leather_from_smelting_hide` | Naturalist hide in the furnace → leather in 10 seconds |
| `alexsmobs:kangaroo_hide_to_leather` | 2 kangaroo hides → leather (replaced, see above) |

Tannery's small and large satchels are removed (recipes, loot, EMI: `TANNERIE_OBJETS` and `client_scripts/tannerie.js`): Reliable Backpacks already gives the single bag of the pack ([0012](../decisions/0012-sacs-piles-jours.md)).

Kept, because they recycle leather that already exists or come from rare loot: Create's saddle at the mill and horse armours at the crusher, Farm & Charm's leather horse armour at the chopper, Farmer's Delight's leather armour, horse armour and saddle cut back on the cutting board, Quark's bound leather block, Cold Sweat's hoglin hide (Nether closed), leather in a few chests (Envelope's lost letters, the illusioner's shack, vanilla fishing).

Leather uses do not change: every recipe keeps `minecraft:leather`. Overgeared 1.6.19 has no leather recipe: its armours are forged from metal plates.

## French texts

Tannery ships English only. `pack/kubejs/assets/tannery/lang/fr_fr.json` translates its 74 texts (items, blocks, advancements, recipe-viewer pages, tanner's table screen). The barrel writes two messages in hard-coded English (« Barrel needs Tannic Powder », « Barrel Full »): the server sends them as action-bar system messages, and `client_scripts/tannerie.js` replaces them on arrival with `message.compline.tannerie.*` (`NativeEvents` on NeoForge's `ClientChatReceivedEvent.System`).

## Files

- `pack/mods/tannery-better-leather.pw.toml`: the mod.
- `pack/kubejs/server_scripts/tannerie.js`: removed satchels and recipes, kangaroo and Naturalist hides, bark tag, check of the remaining leather recipes.
- `pack/kubejs/server_scripts/tannerie_butin.js`: the loot (LootJS).
- `pack/kubejs/data/minecraft/loot_table/entities/{cow,pig,sheep,goat,horse,donkey}.json`: vanilla tables put back over Tannery's.
- `pack/kubejs/client_scripts/tannerie.js`: satchels hidden from EMI, barrel messages translated.
- `pack/kubejs/assets/tannery/lang/fr_fr.json`, `pack/kubejs/assets/compline/lang/*.json`: texts.
- Quest « La tannerie » (`pack/config/questlog/quests/metiers_tannerie.json`): appears with the first hide; dry hide, tannic powder, leather.

## What remains to be tested

In game, on a new world:
- kill a cow, an Icy's Better Horses horse, a llama and a Naturalist deer: each drops its hide, with Looting III too; a cow killed by fire drops cooked beef;
- a hide on the tanning rack dries in about four minutes; the hanging rack too;
- strip an oak log and a Regions Unexplored log with an axe: bark about half of the time; right-click a stripped log: nothing;
- two barks → tannic powder; barrel; dry hide in the barrel → leather; the barrel messages show in French (« Le tonneau a besoin de poudre tannique. », « Le tonneau est plein. »);
- rotten flesh on the rack → dry flesh → sinews; sinews craft a bow;
- EMI: no satchel; Tannery's info pages in French;
- the server log lists no `créature qui lâche encore du cuir` line, and the `recette donnant du cuir` lines only list the recipes kept above.
