# Item stacks and backpacks for Tilth

Date: 2026-10-26. Pack: Minecraft 1.21.1, NeoForge 21.1.265, KubeJS 2101.7.2 build 377, Create 6.0.10, Quark 4.1-487, Curios 9.5.1, Traveler's Backpack 10.1.40. The repository was not modified.

Method: opened web pages (list at the end of the document), Modrinth API, and jars downloaded then read with `unzip` and `javap` (Mojang server 1.21.1 with its mappings, NeoForge 21.1.265 sources, KubeJS, Create, Quark, Supplementaries, Reliable Backpacks, Sensible Stackables). What was not verified is marked "to test".

## Summary

- Stacks: the simplest and safest is the **Sensible Stackables 3.1.2** mod (MIT, 2026-10-24). It reads tags, syncs the client, ignores items with durability. A table of `tilth:pile/N` tags in KubeJS decides the categories.
- Bags: **Reliable Backpacks 1.9.1** (MIT, 2026-09-07). A single bag, 3 rows, dyeing, Curios "back" slot. Fallback: Quark bag + Quark Backpack Plus.
- Without Traveler's Backpack, we lose the canteen-tank (the `sac` module), the sleeping bag and a few effects. Replacement plan below.

## A. Stack sizes

### A.1 How it works in 1.21.1

- The size is the component `minecraft:max_stack_size`, from 1 to 99. I read the 1.21.1 server code: the count of an `ItemStack` is read with `intRange(1, 99)` and **no comparison with the max size** at load. So a stack of 64 in a chest stays at 64 if the limit goes down to 16. It no longer merges, but it is not lost. The Stack Size Fix mod describes the same case: the client does not show the real number outside the inventory.
- Same code: `validateComponents` refuses an item that is **both damageable and stackable** ("Item cannot be both damageable and stackable"), and refuses a bag or "bundle" that contains a stack larger than its limit. So never stack tools, weapons, armor, bows.
- NeoForge: `ModifyDefaultComponentsEvent`, on the mod bus. Its javadoc (21.1.265) gives as an example `builder.set(DataComponents.MAX_STACK_SIZE, 16)`. It runs at load, **before tags**: one can only reason there by item or by identifier pattern.
- KubeJS 7: `ItemEvents.modification` in `startup_scripts`. In the jar, `ItemModifications` exposes `kjs$setMaxStackSize(int)`, so `item.maxStackSize = 16` in script. The predicate accepts identifier regex and tags, but tags are not yet bound at startup (to test, surely empty). The KubeJS wiki page for this event is empty ("This page hasn't been written yet"). A change requires a full restart, no `/reload`.

### A.2 Existing mods (1.21.1 NeoForge)

| Mod | Version / date | License | Method | Verdict |
|---|---|---|---|---|
| Sensible Stackables | 3.1.2, 2026-10-24 | MIT | Fzzy Config (already in the pack): items and tags `#c:ingots`, common size. Computed at `TagsUpdatedEvent`, sent to the client by packet. Order: common size, then tags, then items. Durable items are refused with a warning | **Chosen** |
| Stack Size Tweaks | 2026-09-25 | MIT | json5 file by categories or items, restart | No tags, oriented toward increases |
| Stack Config | 2026-10-12 | GPL-3.0 | `stack_config.json`, items and tags, hook on `getMaxStackSize`, no restart | Good fallback, little used (1.2k) |
| Upstack | 2026-06-05 | GPL-3.0 | TOML, items and tags, values 1 to 99, server restart | Fine, "common items" |
| Bigger Stacks, BigStacks, Stack99, StackPlus | various | various | Stack increases | Off topic |

The names cited by the maintainer ("Stack Size Controller", "StackUp", "Realistic Stack Sizes", "Item Stack Size Configurator") give nothing on Modrinth for 1.21.1 NeoForge. A "Stack Size Controller" exists for another game. "Stackable" is `stackable127`, which raises blocks.

### A.3 Traps

Verified or read in the code:
- **Existing stacks**: see A.1. The Tilth world has not been started, so setting the stacks **before** the first game avoids the whole problem.
- **Loot**: `LootTable` splits stacks that are too big (comparison of the count with `getMaxStackSize`, then `Math.min`). Chests, Lootr (same path, to test) and mobs are safe.
- **Recipes**: a result larger than the stack is not rejected at load. The risk is in machines that insert without splitting. So **keep each stack at least equal to the biggest recipe result**. Jar scan: Kaleidoscope Cookery outputs 9 cooked meats per pot, Farm & Charm 16 fences and separators and 12 yeasts, Create 16 glass panes per craft and 18 nuggets from the crusher, Overgeared 9 steel nuggets. Rules: ingots, nuggets, gems, cooked dishes: 16 minimum. Glass panes, rails, bars: 32.
- **Create 6**: vault `vaultCapacity` = 20 slots per block (1 to 2048). The capacity in items goes down with the stacks: plan to raise the value if needed. The package (`PackageItem`) has 9 slots, so 9 stacks of the new size: Frogports carry less per package. Create goes through `getMaxStackSize`, nothing special to set.
- **Sable / Aeronautics**: in the Aeronautics 1.3.2 jar, no storage class. Chests on an airship are ordinary blocks.
- **Client / server**: the default component lives on both sides. A startup script is delivered to both by packwiz. Sensible Stackables syncs by itself.
- **Durability**: see A.1. The mod ignores them.
- **Performance**: a table read once per data load, one access per item afterward. Nothing to fear. The patches of Sensible Stackables are only needed above 99.
- **Bags**: the "bundle" weighs 1/(max stack) per item, so it follows the table (see B).

Not verified (to test in game): Traveler's/Reliable Backpacks with reduced stacks, EMI (display), hoppers with oversized stacks, Overgeared (furnace and anvil outputs).

### A.4 Realism references

- TerraFirmaCraft: **size** decides storage, **weight** decides the stack. According to the TerraFirmaGreg guide (read in search results; the page returned 404 when opened, values to confirm): very light 64, light 32, medium 16, heavy 4, very heavy 1. Blocks are generally "medium". The size names (tiny to huge) and weights are confirmed in TFC's `en_us.json`.
- Vintage Story: logs stack at 16 in the inventory (wiki "Log"). The wiki "Ground Storage" gives stack capacities **on the ground** (ingots 64, planks 48, firewood 32, charcoal 16), not inventory stacks.
- Realism Craft (Bedrock): no reliable source found on its values. So I give no figure about it.

### A.5 Proposed table for Tilth

Idea: three common tiers (64, 32, 16), two heavy (8, 4), one single (1).

| Category | Stack | Examples | Vanilla |
|---|---|---|---|
| Tiny: seeds, nuggets, string, stick, flint, paper, feather, dyes, powders | 64 | `#c:seeds`, `#c:nuggets`, `#c:dyes` | 64 |
| Arrows, bolts, torches, planks, bricks, coal, clay (balls) | 32 | `#minecraft:arrows`, `#minecraft:planks` | 64 |
| Ingots, gems, raw metal, ore blocks | 16 | `#c:ingots`, `#c:gems`, `#c:raw_materials`, `#c:ores` | 64 |
| Trunks, logs | 16 | `#minecraft:logs` | 64 |
| Raw stone, dirt, sand, gravel, clay (blocks) | 16 | `#c:cobblestones`, `#c:stones`, `#c:sands`, `#c:gravels` | 64 |
| Cut stone, bricks, slabs, stairs | 32 | `#minecraft:stone_bricks`, `#minecraft:slabs`, `#minecraft:stairs` | 64 |
| Glass panes, rails, bars, fences | 32 | `#c:glass_panes`, `#minecraft:rails`, `#c:fences` | 64 |
| Glass, wool, carpets, decoration, lanterns | 16 | `#minecraft:wool`, `#c:glass_blocks` | 64 |
| Crops and raw foods | 32 | `#c:crops`, `#c:foods/raw` | 64 |
| Cooked dishes and bread | 16 | cooked meats, Kaleidoscope dishes | 64 |
| Empty bottles | 16 | `minecraft:glass_bottle` | 64 |
| Full bottles (water, honey, potions) | 8 | `minecraft:potion`, TWR bottles | 1 or 16 |
| Full buckets, boats, beds, saddles, minecarts | 1 | unchanged | 1 |
| Create parts: shafts, gears, chains, tubes, mechanisms, casings | 16 | tag `tilth:pile/16` | 64 |
| Create machines, vaults, tanks, Aeronautics engines | 4 | tag `tilth:pile/4` | 64 |
| Metal or gem storage blocks | 4 | `#c:storage_blocks` | 64 |
| Chests, barrels, furnaces, workbenches | 8 | `#c:chests`, `#c:barrels` | 64 |
| Tools, weapons, armor, bows | 1 | unchanged | 1 |
| Rest (common size) | 32 | any item still at 64 | 64 |

To decide: dirt and stone at 16 hamper building. Soft variant: 32.

### A.6 Assignment and approach

1. **Sensible Stackables mod**: `uncapStackSize = false`, `commonStackSize = 32`. In the `items` table, one line per tag: `"#tilth:pile/16" = 16`, etc. (exact name of the TOML file to read at first launch).
2. **Tags in the repository**: `pack/kubejs/data/tilth/tags/item/pile/{1,4,8,16,32}.json`, modeled on `peaux_brutes.json`. Each references the `c:` tags and adds mod items one by one with `{"id": "...", "required": false}`. A modded item without a `c:` tag is caught here, without touching the mod. An item must be in only one stack tag: with two tags, the value retained is not guaranteed (Upstack says so, Sensible Stackables not verified).
3. **Check**: a script `server_scripts/piles.js` goes through the recipes at server start and writes in the log "[piles] recette dont le résultat dépasse la pile" (recipe whose result exceeds the stack) (same style as `tannerie.js`). It also lists the items without a stack tag.
4. **Without a mod**: a script `startup_scripts/piles.js` with `ItemEvents.modification` and identifier regexes (`_ingot$`, `^raw_`, `_planks$`). Works without Java, but requires a restart and does not see tags.
5. **`tilth` module**: only if the regexes become unmanageable. A mixin on `ItemStack.getMaxStackSize` reading a table rebuilt at `TagsUpdatedEvent` would do the same as Sensible Stackables. I advise against it: the mod exists, is active and maintained.

## B. A simpler backpack

### B.1 What Traveler's Backpack brings today

- **Hose and tanks**: the `sac` module of `tilth` (`ThirstHoseEffect`, `SacModule`) is tied to it. Without the bag, the module ignores itself ("Module sac ignoré" (Sac module ignored)), but it becomes dead code.
- **Sleeping bag**: sixteen blocks `travelersbackpack:*_sleeping_bag`. Comforts had been removed as a duplicate.
- **Abilities**: lapis already cut (`xp.md`), Warden bag (+4 hearts, `sante.md`), etc.
- Other traces: `pack/defaultconfigs/travelersbackpack-server.toml`, `pack/kubejs/assets/travelersbackpack/lang/fr_fr.json`, and a comment in `sante.js`. No Cold Sweat sleeping-bag list was written (empty search in `pack/config`).

### B.2 Candidates (all verified on Modrinth or CurseForge)

| Mod | 1.21.1 NeoForge version | License | Size / use | Remarks |
|---|---|---|---|---|
| **Reliable Backpacks** | 1.9.1, 2026-09-07 | MIT | 1 bag, 3 rows by default (adjustable), dyeable, placeable | Chestplate or Curios "back" (read in the jar). No dependency except NeoForge. Forbids shulkers and Supplementaries sacks inside. No French |
| Quark (Backpack module) | 4.1-487 | Oddities: CC-BY-NC-SA 3.0 | Three extra rows | Already in the jar, but category `oddities` cut. Chestplate armor. Recipe with ravager hide (raids cut in Tilth) or, if the flag is off, 4 leather, 2 iron ingots, a chest. Native French "Sac à dos" |
| Quark Backpack Plus | 1.0.3, 2026-05-14 | LGPL-3.0 | Places the Quark bag in Curios "back" | Small mod (3.6k), requires Quark, Quark Oddities (marker) and Curios |
| Packed Up | 1.1.1, 2026-08-09 | All rights reserved (modpacks allowed by the FAQ) | 7 levels, 3 to 8 rows | Too many tiers. Requires two SuperMartijn642 libraries |
| L2 Backpack | 3.1.5, 2026-07-30 | LGPL-2.1 | 9 to 54 slots, upgrades at the smithing table | Requires L2 Library, remote sharing |
| Backpacked (MrCrayfish) | 3.0.5 (CurseForge, 2026-04-03) | LGPL-2.1 | Unlock by **XP** (option: by items), augments, Framework | Against Tilth's "no more XP" rule, and not simple |
| Vercte's Satchels | 1.2.0, 2026-06-15 | MIT | 6 slots under the bar, V key | Very simple, good complement. Curios optional |
| Useful Backpacks | 3.0.2, 2025-05-22 | Apache-2.0 | 3 sizes | Old, requires U Team Core |
| Backpacks! | 1.3.5, 2025-10-03 | Apache-2.0 | Data pack | Shader trick to hide the name: risk with rendering mods |
| Chestpack | 1.5, 2025-03-20 | MIT | Carry a whole chest | Separate idea, not a bag |
| Scout Reforked, Beans Backpacks | 0.0.7 / 0.14.2 beta | MIT | Bags in the inventory screen / "bundle" rules | Too young |
| Supplementaries sack | already there | | 9 slots, placeable, made of linen | Carried in hand, message "Un peu lourd à porter" (A bit heavy to carry). French present |
| Vanilla "bundle" | Experimental in 1.21.1 | | Weight = 1 / max stack | Became normal in 1.21.2 (24w39a). The Bundle Recipe mod (MIT) and the experimental flag would be needed |

Rendering and compatibility: the Reliable Backpacks bag is a player render layer (`BackpackLayer`) and a block render. FA: Player Extension says nothing about bags or Curios on its page; it requires EMF and ETF. **Nothing is confirmed** for Fresh Animations or 3D Skin Layers: to test on the back, while running and while kneeling. The placed bag is an ordinary block, so it can be placed on a Sable airship (to test, with the mass from `sablecompat`).

### B.3 Recommendation

**Reliable Backpacks 1.9.1.** One item, no upgrades, no XP, no library. Config (fields read in the jar, file `reliable_backpacks.json` to confirm at first launch): `enableChestSlot = false` (the bag does not take the armor slot), `enableCuriosIntegration = true`, `enableEntityStealing = false`, `backpackRows = 3` then 4 if the reduced stacks require it.

Recipe: 7 leather (or leather and rope if Farmer's Delight were there, which is not the case). Leather goes through the tannery: good pace. A KubeJS recipe can add a string or an ingot.

Fallback if we want zero new heavy mods: Quark (`oddities = true`, disable Magnets, Pipes, Matrix Enchanting, Tiny Potato, Crate, Totem of Holding; `Enable Ravager Hide = false`) plus Quark Backpack Plus. Drawback: a single-author mod and the Oddities marker.

### B.4 Changes to plan in the repository

1. Remove `pack/mods/travelersbackpack.pw.toml`, `pack/defaultconfigs/travelersbackpack-server.toml`, `pack/kubejs/assets/travelersbackpack/lang/fr_fr.json`. Add `reliable-backpacks.pw.toml` and `pack/kubejs/assets/reliable_backpacks/lang/fr_fr.json`.
2. Translation: `block.reliable_backpacks.backpack` = "Sac à dos" (Backpack), `container.backpack` = "Sac à dos", the subtitles (placed, opened, closed, equipped) and the config labels. The mod only has de, es_mx, ru, sv and en.
3. `sac` module: remove in 0.5.0 (`SacModule`, `ThirstHoseEffect`, `ModIds`, `TilthConfig`, `TilthModule`, `build.gradle`, `neoforge.mods.toml`) and the section of `docs/plan/mod-tilth.md`, its tests S1 to S9, decision 18 of `12-verification.md`.
4. Docs: `docs/mods.md` (lines Traveler's Backpack, Modded Coffee GUI, Comforts), `xp.md` (lapis lines), `sante.md` (Warden bag), `sante.js` (comment), `tannerie.md` (the 52 leather recipes disappear), `10-recentrage.md`, `README.md`, `CHANGELOG.md`.
5. Water while traveling: no more hose. Either the TWR bottles (stacks of 8), or reopen the case of **Thirst Canteen** (LGPL-3.0, 2026-07-17), classed "refused" in `docs/mods.md` but recommended in `04-vie-quotidienne.md`.
6. Sleep: **Comforts 9.0.5+1.21.1** (LGPL-3.0, 2026-06-22) does not change the respawn point (Modrinth page). Its options list an insulated bag and a rest bonus: interaction with Cold Sweat to test. To restore if the sleeping bag is still wanted.
7. Nothing to do for the "refrigerator": Freeze It is already removed.

### B.5 Tests

1. Bag on the back with FA: Player Extension, 3D Skin Layers, Shoulder Surfing.
2. Bag placed on an Aeronautics airship, then in flight.
3. Transfer of reduced stacks without loss, with an oversized stack from an old test.
4. Leather craft through the tannery up to the bag.
5. Create vault and Frogport with the new stacks.

## Sources (opened pages)

- https://docs.neoforged.net/docs/1.21.1/items/datacomponents
- https://kubejs.com/wiki/events/ItemEvents/modification (empty page)
- https://modrinth.com/mod/stacksizetweaks
- https://modrinth.com/mod/sensible-stackables
- https://modrinth.com/mod/stack-config
- https://modrinth.com/mod/upstack
- https://modrinth.com/mod/bigger-stack-size
- https://modrinth.com/mod/bigger-stacks-unofficial
- https://modrinth.com/mod/stacksizefix
- https://raw.githubusercontent.com/TerraFirmaCraft/TerraFirmaCraft/1.21.x/src/main/resources/assets/tfc/lang/en_us.json
- https://wiki.vintagestory.at/index.php/Log
- https://wiki.vintagestory.at/index.php/Ground_Storage/en
- https://minecraft.wiki/w/Bundle
- https://modrinth.com/mod/bundle-recipe
- https://modrinth.com/mod/reliable-backpacks
- https://modrinth.com/mod/packed-up-backpacks
- https://modrinth.com/mod/quark-backpack-plus
- https://modrinth.com/mod/vercte-satchels
- https://modrinth.com/mod/fa-player-extension
- https://www.curseforge.com/minecraft/mc-mods/backpacked
- https://www.curseforge.com/minecraft/mc-mods/backpacked/files/all?version=1.21.1
- https://github.com/MrCrayfish/Backpacked
- https://mrcrayfish.github.io/Backpacked
- https://github.com/MehVahdJukaar/Supplementaries/wiki/Sack (no info on the sack; data read in the jar)
- Modrinth API: https://api.modrinth.com/v2/search, /v2/project/{slug}, /v2/project/{slug}/version, /v2/version/{id} (projects cited above, plus quark-oddities, comforts, backpacked-world-of-color, vanilla-backpacks, l2backpack, useful-backpacks, chestpack-backpacks, beans-backpacks-3, scoutreforked, thirst-canteen)
- Jars and files read: https://maven.neoforged.net/releases/net/neoforged/neoforge/21.1.265/neoforge-21.1.265-sources.jar, https://piston-meta.mojang.com/mc/game/version_manifest_v2.json (server 1.21.1 and mappings), jars KubeJS 2101.7.2-build.377, Create 6.0.10, Create Aeronautics 1.3.2, Quark 4.1-487, Supplementaries 3.9.9, Reliable Backpacks 1.9.1, Sensible Stackables 3.1.2, Kaleidoscope Cookery 1.6.0, Overgeared 1.6.19, Farm & Charm 1.1.25.
- Web searches (results only, pages not opened): TerraFirmaGreg guide "Size and Weight" (page 404 when opened), Vintage Story forum "stack sizes", search "Realism Craft" (nothing useful).
