# 0045 — Awakening: enchanting as a deed

- **Date**: 2026-11-21
- **Status**: kept (the team's choice of 2026-11-21); replaces [0008](0008-enchantement.md); checked on the full pack's dedicated server and in a development client (awakening, choice, forgetting, shard, loot, trades), not played

## Context
The team wants enchanting to be very rare, a real achievement, so that losing an enchanted item is frightening. With Immersive Enchanting ([0008](0008-enchantement.md)), an ancient book placed near the table unlocked its enchantment for good and for everyone, without being used up, and could be copied for a book and quill. Enchanted gear also came out of a score of loot tables (Supplementaries urns, Friends & Foes citadels, Wilder Nature bounties, Enhanced Celestials dungeons, shipwrecks, ruined portals) and from villagers. Nothing was rare.

## Options
| Criterion | A. Immersive Enchanting, hardened | B. Deeds and great beasts (house) | C. Relics only |
|---|---|---|---|
| How an enchantment is earned | A found book, used up at the table | The item's own deeds; a shard from a great beast | Found already enchanted, unique items |
| Achievement | Finding a book | Using this very item for hours; winning a fight | Luck |
| Losing the item | Hurts | Hurts most: hours lived with it | Hurts |
| Mods | Keeps Immersive Enchanting and AlfinoLib | Removes both | Removes both |
| Code | Medium (mixins on another mod) | Large (a `fallow` module) | Small, many texts |

No mod for NeoForge 1.21.1 makes enchanting rare (study of 2026-11-21): Enchanting Infuser, Easy Magic and Create Enchantment Industry make it easier; Apothic Enchanting, Relics and Curse of Pandora make it an RPG; Artifacts has the right idea (rare treasures) in a comic tone; Awakened Items has the right idea in a tiny, unmaintained mod; Enchancement is Fabric only; Tetra has no 1.21.1 version.

## Decision
Option B, the `eveil` module of `fallow` 0.44.0 ([eveil](../plan/eveil.md)):

- an item counts its deeds and awakens past a threshold (3,000 points, then ×3); at the enchanting table it shows two enchantments drawn from its deeds and the player picks one;
- the number of awakenings follows Overgeared's forging quality (1, 2 for expert and perfect, 3 for masterwork);
- seven great beasts (Frostmaw, Ferrous Wroughtnaut, Umvuthi, Naga, the Sculptor, Sculksaur, Obsidiceratops) leave a shard that lays one strong enchantment;
- at the end of its durability an item forgets its last awakening and must earn it again;
- nothing comes out of loot or trade enchanted, the anvil no longer merges enchanted items (`blockEnchantMerge` now true by default), the vanilla table window never opens;
- Immersive Enchanting and AlfinoLib leave the pack, with their 46 cost files and their translation.

## Rejected
- Option A: the book remains a thing to find, not something done; the study's fallback.
- Option C: the player does nothing to earn the power.
- Enchantment Disabler (MIT): it would have stripped loot and trades too, but the module's loot modifier and trade filter do it in a few lines without another mod.
