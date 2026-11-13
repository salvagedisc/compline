# 5. Create, construction and infrastructure: verification of 2026-10-18

Verification of plan v2 (chapters 10 and 11, KubeJS drafts, chapters 03, 06 and 07) for Create and its addons, KubeJS and its extensions, building mods and infrastructure.

**Method**
- Versions taken from the Modrinth API on 2026-10-18, filter `neoforge` + `1.21.1`.
- 30 jars opened in `scratchpad/jars/` (own folders: `jars/<slug>/` and `jars/s5/<slug>/`): `javap`, JSON from `data/` and `assets/`, `mods.toml`.
- GitHub was closed during this pass (proxy 403, MCP limited to `salvagedisc/tilth`). The Aeronautics issues cited in chapter 06 therefore **could not be reread**.
- Markers: "jar" = read in the jar; "Modrinth" = API; "inferred" = reasoning without testing.

---

## 1. What is confirmed

### 1.1 NeoForge 1.21.1 versions on Modrinth

| Mod | In the repository | Latest version | Required dependencies (Modrinth + `neoforge.mods.toml`) | Note |
|---|---|---|---|---|
| Create | 6.0.10 | 6.0.10 (2026-04-21) | — | up to date |
| Create Aeronautics | 1.3.2 | 1.3.2 (2026-08-29) | Sable, Create | "bundled" jar: contains Aeronautics, Simulated and Offroad (`META-INF/jarjar`) |
| Sable | 2.0.5 | **2.0.6** (2026-10-03) | Create and imguimc optional; embeds Veil 4.3.2 and Sable Companion | fix for entities in fluids |
| Steam 'n' Rails | 0.3.0-beta.2 | 0.3.0-beta.2 (beta) | Create | third-party mod tracks only register if the mod is loaded (`TrackCompatUtils` → `Mods.isLoaded`, jar) |
| Create Ore Excavation | — | 1.21.1-1.6.8 | Create ≥ 6.0.7; KubeJS ≥ build.295 and KubeJS Create ≥ build.14 optional | 15 default veins, including 4 from the Nether (jar) |
| KubeJS | 2101.7.2-build.377 | same | Rhino ≥ 2101.2.7-build.81; JEI, REI and **EMI ≥ 1.1.22** optional | native EMI integration (`integration/emi`, jar) |
| KubeJS Create | — | 2101.3.1-build.18 (**beta**, 2025-11-12) | KubeJS ≥ build.285, Create ≥ 6.0.7 | compatible with Create 6.0.10 |
| MoreJS | — | 0.16.0 (2025-10-13) | KubeJS | |
| LootJS | — | 3.7.0 (2026-04-29) | KubeJS (optional on the jar side) | server only |
| Quark | 4.1-485 | **4.1-487** (2026-10-05) | Zeta [1, 1.2); **Biolith [3.0.0, 3.1.0)** (the repository has 3.0.14, OK); Lootr optional | |
| Supplementaries | 3.9.9 | **4.0.2** (2026-10-16) | Moonlight ≥ 3.6.4; Sodium < 0.8.12 declared incompatible (the pack has 0.8.13) | major version jump: all sections of `supplementaries-common.toml` still exist in the 4.0.2 jar |
| Amendments | — | 1.21-2.1.11 | Moonlight | contains a folder `assets/amendments/blockstates/twigs` (native link with Twigs) |
| Moonlight | 3.6.9 | **3.7.2** | — | |
| Handcrafted | — | 4.0.3 (2025-03) | Resourceful Lib ≥ 3.0.0 (already there) | |
| Macaw's Roofs | — | 2.3.2 | — | |
| Macaw's Windows | — | 2.4.2 | — | |
| Twigs | — | 1.21.1-3.1.2 | — | |
| Every Compat | — | 1.21-2.11.54 (2026-10-17) | Moonlight ≥ 3.1.0 | |
| FallingTree | — | 1.21.1.11 | none (Cloth Config optional, client) | a single jar carries Fabric, Forge and NeoForge classes, with a valid `neoforge.mods.toml` |
| Leaves Be Gone | — | v21.1.1 | **Puzzles Lib** | server only |
| Visual Workbench | — | 21.1.2 | Puzzles Lib ≥ 21.1.48 | |
| Easy Anvils | — | v21.1.0 (2024-09) | Puzzles Lib | |
| Easy Magic | — | v21.1.4 | Puzzles Lib | |
| Puzzles Lib | — | 21.1.64 | — | new library (1.45 MB) |
| Traveler's Backpack | — | 1.21.1-10.1.39 (slug **`travelersbackpack`**, no hyphen) | none (Curios, Accessories, EMI, JEI, LambDynamicLights… optional) | |
| Curios | 9.5.1 | 9.5.1 | — | |
| Default Options | — | 21.1.8 | **Balm** ≥ 21.0.39 (new library, 0.78 MB) | client only |
| Simple Backups | — | 1.21-4.0.30 (**beta**) | — | |
| GraveStone | — | 1.0.40 | Jade optional | |
| Neruina | — | 3.3.5 (2026-10-17) | **Configurable** ≥ 3.5.1 (new library) | |
| spark | 1.10.124 | 1.10.124 | — | |
| Lootr | — | 1.11.38.127 | — | |
| Entity Culling | 1.11.2 | **1.11.3** (2026-10-08) | — | client |
| EMI | — | 1.1.24 | — | |
| No Man's Land | **1.5.12** | 1.6.1 (2026-10-14) | Biolith | 1.6.1 was read: to be checked on the version finally chosen |

New libraries brought by this part: Puzzles Lib (4 mods), Balm (Default Options), Configurable (Neruina). The building mods (Handcrafted, Macaw's, Twigs, Every Compat) add none.

### 1.2 The world's woods (jars)

- **No Man's Land has 4 complete wood types**: maple (`maple`), pine (`pine`), walnut (`walnut`) and willow (`willow`). Each has planks, logs, wood, stripped versions, doors, trapdoors, signs, fences, **seats, cabinets (`cabinet`) and bookshelves**. The `petrified_log` has no planks: it is not a wood type. There are 11 foliages: maple, red maple, autumn oak, yellow birch, pale cherry, frosted, pine, walnut, willow, apple and pear (NML's `minecraft:leaves` tag).
- **NML already planned for Every Compat**: the jar contains `assets/everycomp/textures/block/c/nomansland/palettes/<wood>_window.png`, the Create window palettes for its 4 woods. Every Compat cites `nomansland` in `HardcodedBlockType`, but only to exclude the `curiosities` and `mctb` modules, which are not in the pack.
- **Quark adds no growing wood**: `Ancient Wood`, `Azalea Wood` and `Blossom Trees` are cut in `quark-common.toml`. But **Zeta registers their blocks anyway**, because it is the `loadBus` that carries registration, for all modules; `setEnabled` only removes the subscription to the `playBus` (classes `ZetaModule` and `ZetaModuleManager`, Zeta 1.1-40 jar). Every Compat and Steam 'n' Rails therefore see three Quark woods: `quark:ancient`, `quark:azalea` and `quark:blossom`. Steam 'n' Rails' jar even has tracks `track_quark_ancient/azalea/blossom` (inferred: it generates them).

### 1.3 What Every Compat actually generates (`modules/*` classes of the jar)

| EC module | Blocks per wood | Mod in the pack? |
|---|---|---|
| Handcrafted | 15: chair, table, bench, couch, cupboard, desk, dining bench, drawer, decorated bed, nightstand, shelf, sideboard, counter, corner and pillar trims | yes |
| Macaw's Roofs | 14 roofs | yes |
| Macaw's Windows | 18: windows, 4-pane windows, parapets, shutters, louvers, blinds, curtain rods, stripped variants | yes |
| Twigs | 1: the table | yes |
| Quark | 8 to 9: chest, trapped chest, ladder, post, stripped post, vertical planks, vertical slab, hollow log, bookshelf. Plus, **per foliage**, a hedge and a leaf carpet | yes |
| Create | 2: window and window pane | yes |
| Friends & Foes | 1: beehive | yes (already in the pack) |
| Supplementaries | 1: direction sign (`way_sign_<wood>`, generated by Moonlight) | yes |

Estimate (inferred from the number of entries, to be counted in game):
- **NML woods**: about 60 blocks per wood type, i.e. about 240, plus 16 to 22 hedges and leaf carpets (Moonlight cannot necessarily tie the "autumn" and "fruit" foliages to a wood type): **250 to 270 blocks**, each with its item.
- **Quark's ghost woods**: about 50 blocks each, i.e. **about 150 blocks for trees that never grow**.

Every Compat has two configuration levels (classes `ModEntriesConfigs` and `UnsafeDisablerConfigs`):
- **The normal level** hides blocks and cuts their recipes, but the blocks stay registered: "only hide stuff from tabs and disable their recipes".
- **The "hazardous" level** has blacklists per `woodtype`, `leavestype`, `module` and `entryset`. These lists really prevent registration.

### 1.4 Weight in blocks (blockstates counted in the jars)

| Mod | Blocks | Items (models) |
|---|---|---|
| Steam 'n' Rails | 1,440 files, of which 1,110 outside tracks; tracks actually present: vanilla woods, Quark ×3, andesite, etc., ×3 gauges | 1,866 |
| Quark (everything is registered, disabled modules included) | 809 | 991 |
| Create | 643 | 751 |
| Macaw's Roofs | 605 | 607 |
| No Man's Land | 376 | — |
| Macaw's Windows | 328 | 326 |
| Supplementaries | 276 | 304 |
| Handcrafted | 267 | 285 |
| Twigs | 256 | 259 |
| Aeronautics + Simulated + Offroad | 43 + 95 + 3 | — |
| Traveler's Backpack | 60 | 85 |
| Amendments | 41 | — |
| Every Compat (generated) | about 400 (1.3) | same |

These mods alone make **about 5,500 blocks**. The v2 additions for building alone (Handcrafted, Macaw's ×2, Twigs, Every Compat) bring **about 1,850**, of which 605 roofs (Macaw's roofs exist in stone, in 16-colour terracotta, etc.).

For EMI, that is about 2,000 more entries. EMI sorts them into groups (11.7), but they must be grouped or hidden, otherwise the index drowns (section 2.6).

For memory, the cost comes mostly from **block states** and baked models: roofs and stairs have many states. ModernFix (already there) loads models on demand, and that is what limits the damage (inferred). **To be measured with spark**, with and without Macaw's Roofs.

### 1.5 Creations, KubeJS and its extensions (jars)

- **KubeJS 2101.7.2 events available** (interfaces `plugin/builtin/event/*`):

  | Group | Events |
  |---|---|
  | `BlockEvents` | `rightClicked`, `placed`, `broken`, `drops`, `randomTick`, **`blockEntityTick`**, `picked`… |
  | `ItemEvents` | `rightClicked`, `crafted`, `modifyTooltips`… |
  | `PlayerEvents` | `loggedIn`, `tick`, `inventoryChanged`, `chestOpened`, **`stageAdded`/`stageRemoved`**… |
  | `EntityEvents` | `death`, `beforeHurt`, `afterHurt`, `checkSpawn`, `spawned`, `entityDrops` |
  | `ServerEvents` | `recipes`, `tags`, `tick`, `loaded`, `modifyRecipeResult`… |
  | `RecipeViewerEvents` | `addEntries`, `removeEntries`, `removeEntriesCompletely`, `groupEntries`, `addInformation`, `removeCategories`, `removeRecipes` |

- **`NativeEvents.onEvent(Class, handler)`** exists (`NativeEventWrapper`). The parameter is a **`Class`**: `Java.loadClass('…')` must be passed if conversion from a string does not happen. `net.neoforged` is allowed by `kubejs.classfilter.txt`, except `fml`, `accesstransformer` and `coremod`. Mod classes (Create, Wildex…) are not filtered.
- **Tiered recipes no longer exist**: `KubeRecipe.stage()` throws the error "recipe.stage() is no longer supported by default due to vanilla changes!". The `ModifyCraftingItemKubeEvent` event (the one behind `modifyResult`) has **no player**, only the grid. A recipe therefore cannot be locked per player in pure KubeJS. Per-player stages remain (`player.stages`), and LootJS can read them (`hasAnyStage`).
- **Ingredient actions**: `keepIngredient`, `damageIngredient` and `replaceIngredient` exist (`KubeRecipe`, `recipe/ingredientaction/*`).
- **Blocks with a block entity**: `BlockEntityInfo` offers `serverTicking`, `tickFrequency`, `attach` (inventory, tank, energy) and `eventHandler`.
- **KubeJS Create 2101.3.1**:
  - Create recipe schemas, builders for "sequenced" items and sandpaper;
  - events `SpecialFluidHandler`, `SpecialSpoutHandler` and `BoilerHeaterHandler`.
  - **No kinetic block builder**: impossible to create in KubeJS a block that consumes stress.
- **LootJS 3.7**:
  - `LootJS.modifiers(event => event.addTableModifier(…))` and `LootJS.lootTables`;
  - conditions `randomChance`, `matchStructure`, `matchBiome`, **`hasAnyStage`**, `matchPlayer`; actions `addLoot`, `removeLoot`, `replaceLoot`, `playerAction`, `customAction`.
  - The `.when(c => c.randomChance(0.5))` syntax of the `butin.js` draft is LootJS 2's: in 3, one chains `.randomChance(0.5).addLoot(…)`.
- **MoreJS 0.16**: the group is called **`MoreJS`**, not `MoreJSEvents` (the `survivants.js` draft is wrong). Events: `villagerTrades`, `wandererTrades`, `updateOffer`, `structureLoad`, `structureAfterPlace`, `teleport`, `enchantmentTableChanged`, `playerEnchant`…
- **Create Ore Excavation 1.6.8**: `event.recipes.createoreexcavation.vein(…)` with `alwaysFinite`, `biomeWhitelist`, `biomeBlacklist`, `placement`, `veinSize`, `priority`; `coeutil` binding (jar). The 15 default veins (`recipe/ore_vein_type/*.json`) have `"finite":"default"` and `biomeWhitelist: minecraft:is_overworld`. They must be **removed** (`event.remove({type:'createoreexcavation:vein'})`) before adding Tilth's.
- **Wildex 3.0.0** exposes `de.coldfang.wildex.integration.kubejs.WildexKubeJsScriptBridge.onDiscoveryChanged(cb)` and `onCompleted(cb)`. The received parameter carries `playerUuid`, `playerName`, `mobId`, `discovered`, `total`, `percentScaled` and `isComplete`. `zoo.js` is feasible (section 2.5).

### 1.6 Wood and tools (jars)

- **FallingTree** (configuration `fallingtree.json`, classes `config/real/*`):
  - Modes: `INSTANTANEOUS`, `FALL_ITEM`, `FALL_BLOCK`, `FALL_ALL_BLOCK`, `SHIFT_DOWN`.
  - Defaults: `breakMode=INSTANTANEOUS`, `maxSize=100` with `maxSizeAction=ABORT`, `minimumLeavesAroundRequired=1`, **`leavesBreaking=true`**, `adjacentStopMode=STOP_ALL`.
  - An axe is an **`instanceof AxeItem`**. Overgeared's axes (`steel_axe`, `copper_axe`) are `new AxeItem` (`ModItems`): they work without any tag.
  - Logs are recognised through `minecraft:logs`, and NML puts its 4 logs there.
- **Create saw**:
  - `CRecipes` defaults: `allowShapelessInMixer=true`, `allowShapedSquareInPress=true`, `allowRegularCraftingInCrafter=true`, `allowStonecuttingOnSaw=true`.
  - The saw processes `create:cutting` recipes. The `LogStrippingFakeRecipes` only serve to display stripping with the deployer.
  - **NML ships its saw recipes in the wrong place**: `data/create/cutting/compat/nomansland/*.json`, whereas in 1.21.1 they must be in `data/create/recipe/…`. NML does use the right path for `data/create/recipe/filling/honeyed_apple.json`. These 8 recipes (log → stripped log, wood → stripped wood) therefore do not load (inferred from the path, to be confirmed in EMI). There is in any case **no "stripped → planks" recipe** for NML, neither in NML nor in Create 6.0.10 (its `recipe/cutting/compat/` folder does not cover `nomansland`).
  - Tree felling by the saw (`TreeCutter`) relies on the `logs` and `leaves` tags: it works with NML.

### 1.7 Aeronautics and Sable (jars)

- **Block mass on Sable**: tags `sable:light`, `super_light`, `half_volume`, `quarter_volume`, `heavy`, `super_heavy`, `weightless`, plus `physics_block_properties/*.json` (`sable:mass`).
  - Sable already covers planks, logs, slabs, stairs, Quark (vertical slabs, hedges) and Supplementaries (jar, blackboard…).
  - NML adds its own tags `sable:light/heavy` and `simulated:non_movable` (moon basin, candle and bell).
  - **Handcrafted, Macaw's, Twigs, Every Compat and Traveler's Backpack have nothing**: they weigh like a "normal" full block (inferred).
- **No integration** in Aeronautics, Simulated or Sable with Lootr, Traveler's Backpack, GraveStone or Entity Culling (string search in the three nested jars and in Sable). Aeronautics has mixins for Sodium and Iris (`mixin/render/iris/WorldRenderingPhaseMixin`), which confirms that the Iris question is known to its team.
- **Entity Culling 1.11.0 to 1.11.3**: "Fix NeoForge not using the getRenderBoundingBox method for BlockEntities" (Modrinth changelog). This is precisely the kind of cause that makes a contraption's block entities disappear. The repository has 1.11.2, which already contains this fix: **the test of 06 §6.1 must be redone with 1.11.3 before removing the mod**. Configuration keys (jar): `tickCulling`, `tickCullingWhitelist`, `skipBlockEntityCulling`, `blockEntityWhitelist`, `entityWhitelist`, `solidLeaves`.

### 1.8 Infrastructure (jars)

- **GraveStone** (`GraveUtils.getGraveStoneLocation`):
  - The grave goes **to the point of death** if the block is replaceable. Otherwise it **goes up**. There is no downward search.
  - Server configuration: `strict_placement`, `replaceable_blocks`, `only_owners_can_break`, `enable_obituary`, `spawn_ghost`, `friendly_ghost`, `break_pickup`, `sneak_pickup`.
- **Traveler's Backpack** (classes `TravelersBackpackConfig$*`). Useful keys:
  - `enableSleepingBag`, `enableSleepingBagSpawnPoint`, `quickSleepingBag`;
  - `backpackDeathPlace`, `backpackForceDeathPlace`, **`sendBackpackCoordinatesMessage`**;
  - `enableCraftingUpgrade`, `backSlotIntegration`;
  - `spawnEntitiesWithBackpack`, `enableVillagerTrade`, `enableLoot`;
  - `voidProtection`, `invulnerableBackpack`.
- **Visual Workbench**: `VisualCraftingMenu extends CraftingMenu`. It therefore respects `doLimitedCrafting`.
- **The Traveler's Backpack bag, on the other hand, does not respect it**: its crafting upgrade calls `ResultContainer.setRecipeUsed(RecipeHolder)` (the version with no player or check), and not `setRecipeUsed(Level, ServerPlayer, RecipeHolder)` (`BackpackBaseMenu.slotChangedCraftingGrid`).
- **Overgeared**
  - Its anvils are separate blocks (`AbstractSmithingAnvilBlockEntity`). The tag `overgeared:iron_anvil_bases` contains `minecraft:anvil`: the vanilla anvil serves as a base. Easy Anvils replaces the vanilla anvil with a block with an inventory (mixin `AnvilBlockMixin`). Both touch the same block: to be tested (section 5).
  - **Overgeared has its own blueprint system**: `overgeared:blueprint` ("Smithing Blueprint"), with a tool type and a quality.
    - Forge recipes accept `"blueprint": [types]` and `"requires_blueprint": true`.
    - Types can be added in `availableToolTypes`, plus a language key (Overgeared's `doc.txt`).
    - This is a serious lead for engineer blueprints (section 2.3).

---

## 2. The links to build

### 2.1 Wood, the common thread (§10.7)

| Link | What | Tool |
|---|---|---|
| Tree → logs | FallingTree in `FALL_BLOCK`. Raise `maxSize` to 200–250 (NML's large trees and `ancient_tree` probably exceed 100; with `ABORT`, only one log would fall); keep `minimumLeavesAroundRequired=1` (NML's fallen trunks, `fallen_tree*`, and log houses do not fall) and `adjacentStopMode=STOP_ALL` | `config/fallingtree.json` |
| Leaves | **FallingTree already makes leaves fall** (`leavesBreaking=true`). Leaves Be Gone is superfluous, and it is on Aeronautics' blacklist (section 3) | remove Leaves Be Gone |
| Axes | Nothing to do: Overgeared's axes are `AxeItem`s | — |
| Sawmill | Write NML's 16 saw recipes: log → stripped, wood → stripped wood, stripped → 6 planks, stripped wood → 6 planks, for maple, pine, walnut and willow | `ServerEvents.recipes` + `event.recipes.create.cutting([...], 'nomansland:stripped_maple_log')` (KubeJS Create) |
| Mechanical felling | The Create saw fells NML trees (tags): this is the "industry" stage of wood | native |
| Furniture in every wood | Every Compat for NML's 4 woods; **blacklist of Quark's woods** (section 2.2) | `everycomp` (hazardous level) |
| Wooden rails | Steam 'n' Rails has no maple, pine, walnut or willow track. Not feasible in KubeJS (needs Create's `TrackMaterial` API) | optional custom mod (section 4.3) |
| Mass on contraptions | Light furniture and roofs on an airship | `ServerEvents.tags('block')`: `sable:light` ← `#handcrafted:…`, `/^mcwroofs:.*/`, `/^mcwwindows:.*/`, `/^everycomp:.*/`; `sable:half_volume` ← chairs, tables, roofs. The exact tags are to be read with `/kubejs dump` |
| Showing the work | Visual Workbench (crafting table), Easy Anvils (vanilla anvil), Easy Magic (enchanting table) | native |

### 2.2 Building duplicates: what to keep

Proposed rule: **NML's style first** (it is the world), then the most specialised mod.

| Block | Sources | Proven conflict | Decision |
|---|---|---|---|
| Stone bricks (+ mossy, cracked, slabs, stairs, walls) | NML **and** Twigs | Same recipe, 4 stones in a square → 4 (`nomansland:cobblestone_bricks` and `twigs:cobblestone_bricks`, jars) | Keep NML; remove the `twigs:*cobblestone_brick*` recipes and hide them in EMI |
| Gravel bricks | Supplementaries (active) **and** Twigs | Same recipe, 4 gravel in a square (jars) | Keep Twigs (it has slab, stairs and wall); `building.gravel_bricks.enabled=false` in Supplementaries |
| Thatch | Quark Thatch (active) **and** NML (`thatch`, slab, stairs) | 4 wheat: in a square for Quark, shapeless for NML, hence the same grid (jars) | Cut `Thatch` in Quark |
| Bookshelves per wood | Quark Variant Bookshelves (active) **and** NML, which has them for **all** vanilla woods and its own | Same pattern, 3 planks / 3 books / 3 planks (jars) | Cut `Variant Bookshelves` in Quark |
| Sconces | Supplementaries `sconce` (active) **and** NML `sconce_torch` (+ soul, and unlit versions) | Same shape: nugget, torch, nugget above a nugget (jars) | Keep NML (its torches go out); `building.sconce.enabled=false` |
| Pebbles, seashells | NML `pebbles`, `seashells` **and** Twigs `pebble`, 4 seashells, generated in the world | Double ground decoration (Twigs biome modifiers `add_pebbles`, `add_land_sea_shells`, `add_water_sea_shells`) | Neutralise these 3 modifiers (`{"type":"neoforge:none"}` in `kubejs/data/twigs/neoforge/biome_modifier/`); **keep the twigs** (`add_twigs`), which NML does not have, and add NML's forests to the `#twigs:spawns_twig` tag if they are not in `#minecraft:is_forest` |
| Twigs stones (rhyolite, schist, bloodstone) | Twigs generates rhyolite **across the whole Overworld** (`add_ore_rhyolite`), plus schist and bloodstone | Irreversible worldgen, on top of NML's siltstone and quartzite | To be decided before the world: cut these 3 generations (keep the blocks, craftable) |
| Tables | Twigs tables (one per wood) **and** Handcrafted (table, sideboard, nightstand) | Duplicate role | Cut Every Compat's `twigs` module (hazardous level) and hide Twigs tables |
| Roofs | Macaw's Roofs **and** Quark Shingles (coloured shingles, active) | Duplicate role, no recipe conflict | Optional: cut `Shingles` |
| Tuff slabs, stairs and walls | Twigs `tuff_slab/stairs/wall` **and** vanilla 1.21 | Twigs has no recipe for them (legacy blocks) | Hide them in EMI |
| Handcrafted trophies | Creeper, skeleton, spider, witch, pillager, vindicator, evoker, wither skeleton, phantom, silverfish, blaze… | The creatures are gone (0003) | Hide them in EMI, or remove their recipes |
| Shelves | Supplementaries `item_shelf` **and** Handcrafted `shelf` | Different roles (one displayed item, or four) | Keep both |
| Seats, cabinets | NML (`seat`, `cabinet`) **and** Handcrafted | Different styles | Keep both |

Every Compat config (hazardous file; exact name to be found at first launch):
- `woodtype` blacklist: `quark:ancient`, `quark:azalea`, `quark:blossom`. This saves about 150 blocks. The same cleanup applies to Steam 'n' Rails tracks: they must be hidden, since it registers them as soon as Quark is loaded.
- `module` blacklist: `twigs`.
- `dynamic_assets_generation_mode = CACHED`, so as not to regenerate textures at each startup (jar option).

### 2.3 Engineer blueprints (§10.10): four ways, one recommendation

| Option | How | What leaks | Cost |
|---|---|---|---|
| **A. `doLimitedCrafting`** (current draft) | `recipe give *` on login, then `recipe take` of the locked recipes; reading the blueprint gives them back | 1) **Recipe advancements give recipes back**: Create has 1,071 of them, for example `create:recipes/misc/crafting/kinetics/mechanical_press` gives the press as soon as one **holds an andesite casing** (jar). Each advancement concerned must be overridden in `kubejs/data/<mod>/advancement/recipes/…`. 2) The **Traveler's Backpack bag** ignores it: cut `enableCraftingUpgrade`. 3) Create's **mechanical crafters, mixer and press**: no player, hence no control (`allowRegularCraftingInCrafter=true` by default). 4) Any other modded grid. 5) Quark `Automatic Recipe Unlock` is **still `true`** in the repository | KubeJS only, but fragile |
| **B. The blueprint placed on the crafting table** | The blueprint becomes a **kept** ingredient of the locked recipes: `event.shaped('create:mechanical_press', [...], {..., P: 'tilth:plan_atelier'}).keepIngredient('tilth:plan_atelier')` | Knowledge is no longer personal: it is **physical**. The blueprint can be lent and can be lost (consistent with a permanent world among friends). A copy of the blueprint can be added (paper + Supplementaries ancient ink) | KubeJS only, robust. The mixer and press do not craft irregular-pattern recipes. Visual Workbench shows the blueprint placed on the table, in 3D |
| **C. Overgeared blueprints** | Add types (`rail`, `vapeur`, `aerostat`…) to `availableToolTypes`, then `requires_blueprint: true` on the forge recipes of the `tilth:*` parts that already lock the machines (`progression.js`). The blueprint found in a ruin is an `overgeared:blueprint` of that type, given by LootJS | Need to check that the type-selection screen does not offer these types to the player (packet `SelectToolTypeC2SPacket`); otherwise anyone can make the blueprint | Configuration + LootJS. **A single blueprint system** in the pack, instead of two that look alike ("Smithing Blueprint" versus "engineer blueprint") |
| **D. AStages** (`astages` 2.5.3, NeoForge 1.21.1, Modrinth) | Per-player recipe locks, with KubeJS integration; mixins on `CraftingMenu`, `CrafterBlock`, furnaces, stonecutter, forge | Heavy: 0.98 MB, "all rights reserved" licence, 26,000 downloads. It places rendering mixins (`ASectionCompiler`, `ALevelChunkSection`) that are a risk with Sodium 0.8 and Sable | Not adopted without testing |
| **E. Custom mod `tilth-plans`** | Section 4.2 | no known leak | 7 to 10 classes |

**Recommendation: B right away, C to test, E if per-player knowledge is wanted.**
- B requires no Java, works in every grid, shows in EMI (a blueprint is required, you do not know where it is) and ties Visual Workbench into the system.
- The `doLimitedCrafting` gamerule then leaves `init.mcfunction`. It affects **all** the pack's recipes, even those outside blueprints.

Where to find the blueprints (NML 1.6.1 loot tables, read in the structures' `.nbt` files):

| Loot table | Where it is used | Proposed blueprint |
|---|---|---|
| `nomansland:chests/small_dungeon_core` | Cores of small dungeons | "Workshop" and "steam" blueprints |
| `nomansland:chests/redstone_parts` | Trapped room, redstone and copper | "Rail" blueprint: the mechanical theme fits |
| `nomansland:chests/water_well` | Deep wells | "Drilling" blueprint (COE), since one goes underground there |
| `nomansland:chests/sniffer_monument` | Monument | Rare blueprint: "aerostat" |
| Alchemist's ruins, underground shelters, temples, bell sanctuaries | — | With LootJS `matchStructure(...)` on `minecraft:chests/simple_dungeon`, which they reuse |

With Lootr, the loot context contains the player who opens the chest (inferred). `hasAnyStage('plan_rail')` therefore avoids giving back a blueprint already read, if A or E is chosen.

### 2.4 The Dissipator (tier 64 of the clearings, §10.3)

KubeJS Create cannot create a kinetic block (1.5). Two routes:

1. **KubeJS alone: the Dissipator is a head placed in front of a Create fan**
   - Block `tilth:dissipateur` with a block entity: `.blockEntity(be => { be.serverTicking(); be.tickFrequency(20) })`.
   - In `BlockEvents.blockEntityTick('tilth:dissipateur', e => …)`, the neighbouring block is read. If it is a `create:encased_fan` turned towards the head, `getSpeed()` is read on its block entity (public method of `KineticBlockEntity`, which KubeJS's class filter does not block).
   - Beyond 128 rpm for N minutes (counter in the block entity's data), the conversion of the 64 radius starts.
   - **The fan is what consumes the stress**: a real installation is needed to run it.
   - Limits: no engineer's goggles info on the head, no Ponder, and a call to an internal Create class to recheck at each update.
2. **Custom mod** (section 4.1): a real kinetic block with a stress cost, goggles info, a Ponder, and the conversion in Java.

Recommendation: **1 for prototyping** (and to check that the idea appeals), 2 only if the prototype holds and the full "Create machine" gesture is wanted.

### 2.5 The real state of the planned KubeJS scripts

| # | Script | Feasible in KubeJS 2101? | Exact event or tool | Correction needed |
|---|---|---|---|---|
| 1 | `tilth:init` + `load.json` | Yes (datapack) | tag `minecraft:load` | Remove `doLimitedCrafting` if option B is chosen |
| 2 | `no_end_no_nether.js` | Yes | `ServerEvents.recipes`, `BlockEvents.rightClicked`, `NativeEvents.onEvent` | Pass `Java.loadClass('net.neoforged.neoforge.event.level.BlockEvent$PortalSpawnEvent')` if the string is not accepted (the parameter is a `Class`) |
| 3 | `spawns_safety.js` | Yes | `EntityEvents.checkSpawn` (earlier and cheaper) or `EntityEvents.spawned` | — |
| 4 | `saisons_cold_sweat.js` | Outside the scope of this report | — | see the survival report |
| 5 | `recipes/remplacements.js` | Yes | `event.recipes.create.mixing/splashing` (KubeJS Create) | — |
| 6 | `cuisine_unifiee.js` | Yes | `ServerEvents.recipes`, `ServerEvents.tags` | — |
| 7 | `objets_tilth.js` + `overgeared_pieces.js` | Yes | `StartupEvents.registry('item')`; type `overgeared:forging` (fields `blueprint`, `requires_blueprint`, `hammering`, `minimum_quality`…, Overgeared's `doc.txt`) | Use `event.custom({type:'overgeared:forging', …})` |
| 8 | `recipes/progression.js` | Yes | `replaceInput`, `shaped`, `create.sequenced_assembly` | The steam engine takes `#c:plates/gold` (jar): replacing it with steel is correct |
| 9 | `plans_ingenieur.js` + `loot/butin.js` | **Option B: yes; option A: with leaks** | `shaped(...).keepIngredient(...)`; `LootJS.modifiers` + `addTableModifier('nomansland:chests/…').randomChance(x).addLoot(...)` | Rewrite `butin.js` in LootJS 3 syntax; target NML's tables (2.3) |
| 10 | `survivants.js` | Yes | `NativeEvents.onEvent(BabyEntitySpawnEvent)`; **`MoreJS.villagerTrades`** | Rename `MoreJSEvents` to `MoreJS` |
| 11 | Village pools | Datapack | — | — |
| 12 | `gisements_coe.js` | Yes | `event.recipes.createoreexcavation.vein/drilling` | **First remove the 15 default veins**; `.drill(...)` expects an item from the tag `#createoreexcavation:drills` |
| 14 | `visionneuse.js` | Yes, **with EMI** | `RecipeViewerEvents.removeEntriesCompletely('item', …)` and `groupEntries` (KubeJS has its EMI plugin) | Add the duplicates from 2.2, Quark woods, Quark and Nether tracks of Steam 'n' Rails, trophies |
| 17 | `clairieres.js` | Yes (coherent draft) | `BlockEvents.placed`, `server.scheduleInTicks`, `/fillbiome` | The Dissipator goes through 2.4 |
| 18 | `zoo.js` | **Yes** | `Java.loadClass('de.coldfang.wildex.integration.kubejs.WildexKubeJsScriptBridge').onDiscoveryChanged(p => { if (p.discovered >= 25) … })` (Wildex jar) | Check in game that the callback arrives server-side (the parameter carries `playerUuid`) |
| new | `bois.js` | Yes | `create.cutting` (2.1) | — |
| new | `tags_engins.js` | Yes | `ServerEvents.tags('block')` towards `sable:*`, `create:non_movable`, `simulated:non_movable` | — |
| new | `doublons.js` | Yes | `ServerEvents.recipes` (`remove`) + `RecipeViewerEvents` | List from 2.2 |

**Custom mods worth the effort**: the list is in 4.

### 2.6 EMI and the index

- Group by family with `RecipeViewerEvents.groupEntries`: all Macaw roofs, all windows, all furniture of a wood type, all coloured sheet metal ("locometal") of Steam 'n' Rails, which make 17 colours × about 20 shapes.
- Everything that is registered but useless (Quark woods, Quark and Nether tracks, trophies, Twigs tuff, disabled Quark modules) goes through `removeEntriesCompletely`. **It must be checked whether EMI shows items from disabled Quark modules**: Zeta removes them from the creative tabs, but EMI may also list the registry (section 5).

---

## 3. Conflicts and risks

1. **Leaves Be Gone is both added (11.3) and on Aeronautics' blacklist** (06 §6.1, technical research A4, issue #866, not reread for lack of GitHub access). It is also useless, since FallingTree already breaks leaves. **To be removed from 11.3.**
2. **The Traveler's Backpack bag**
   - Its crafting grid bypasses `doLimitedCrafting` (1.8).
   - `sendBackpackCoordinatesMessage` displays **coordinates on death**: contrary to the "never coordinates" rule. Set to `false`.
   - `backpackDeathPlace` places the bag as a block, next to the GraveStone grave: two items to recover, perhaps in mid-air (point 3). Decide who keeps the bag: TB (`backpackDeathPlace=true`), or GraveStone if the bag goes in the Curios slot (`backSlotIntegration`). To be tested.
   - `spawnEntitiesWithBackpack` and `enableVillagerTrade`: to be cut or confined to survivors.
3. **Dying aboard an airship**: GraveStone places the grave at the position of death, in the real world, and never goes down (1.8). The blocks of a Sable contraption live elsewhere, in a "sub-level". The grave therefore floats in the sky, where the player died (inferred), and may end up in the path of a contraption. A test is needed. Possible fix: small mod (4.4).
4. **Lootr chests on a contraption**: Lootr has no rule for Create or Sable (only `MixinSchematicPrinter` touches Create). Assembling a ruin chest into a contraption moves a block entity with per-player inventories (risk of duplication or loss, inferred). Put `lootr:lootr_chest`, `lootr_barrel`, `lootr_trapped_chest`, `lootr_inventory` and `lootr_shulker` in `create:non_movable` and `simulated:non_movable` (NML already uses the latter tag).
5. **Entity Culling**: do not remove it before redoing the test with 1.11.3 (1.7). With Better Leaves, also look at `solidLeaves`.
6. **Duplicate recipes** (2.2): without action, the player gets one block or the other at random depending on recipe loading.
7. **Every Compat** generates about 150 blocks for three Quark woods that do not exist in the world. Without a blacklist, they stay forever in the world's registry. Removing them later leaves "missing" entries on load (without breaking the world, but to be done **before** the first real world).
8. **Easy Anvils and Overgeared** both touch the vanilla anvil (`AnvilBlockMixin` on one side, `iron_anvil_bases` on the other): to be tested (section 5).
9. **Easy Magic and MoreJS** both modify the enchanting table (`EnchantmentMenu` and MoreJS's `enchantmentTable*` events). No conflict as long as no MoreJS script is written on enchanting (inferred).
10. **Beta versions**: Steam 'n' Rails 0.3.0-beta.2, KubeJS Create 2101.3.1-build.18 (not updated since 2025-11), Simple Backups 4.0.30. To be frozen on a permanent world, upgrading only after a backup and a test.
11. **Supplementaries goes from 3.9.9 to 4.0.2** (major version). The repository's configuration keys still exist, but the configuration generated at first launch must be reread.
12. **NML's saw recipes do not load** (1.6). To be reported to NML's author; in the meantime, `bois.js`.

---

## 4. To adapt, remove or add in the plan

### 4.1 Custom mod `tilth-dissipateur` (only if the KubeJS prototype of 2.4 convinces)
- **Role**: a real Create kinetic block. It consumes stress (for example 16 su/rpm, adjustable), shows its state in engineer's goggles, has a Ponder, and launches the conversion of the 64 radius in Java (same method as `/fillbiome`, which already sends clients the biome update packet).
- **Size**: 8 to 12 classes.
  - Block (subclass of Create's `KineticBlock`/`DirectionalKineticBlock`), block entity (`KineticBlockEntity`) and item.
  - Registration of the stress impact; rendering (transmission shaft + model); optional Ponder scene.
  - Conversion service split by ticks; configuration; data (recipe, loot, tags).
- Dependency: Create 6.0.10 (Registrate API). The simplest is to start from the Create addon template for NeoForge 1.21.1.

### 4.2 Custom mod `tilth-plans` (only if per-player knowledge is wanted)
- **Role**: per-player knowledge, without leaks.
  - Player attachment: set of blueprints read.
  - Blueprint item with a "blueprint identifier" component.
  - Files `data/tilth/plans/*.json`: blueprint → recipes.
  - Mixin on `ResultContainer#setRecipeUsed(Level, ServerPlayer, RecipeHolder)` and on the TB bag grid.
  - Small EMI plugin that displays "Blueprint required: …" on the recipe; admin command.
- **Size**: 7 to 10 classes.

### 4.3 Custom mod `tilth-voies` (optional, later)
- **Role**: Steam 'n' Rails tracks in maple, pine, walnut and willow, through Create's `TrackMaterial` API, with NML's textures.
- **Size**: 3 to 5 classes, plus models and textures.
- Makes the rail network a real extension of the region's wood.

### 4.4 Mini-mod `tilth-tombe` (if test 5.3 fails)
- **Role**: mixin on `GraveUtils.getGraveStoneLocation` to descend to the first solid ground when death occurs in the air (airship, fall).
- **Size**: 2 classes.
- Alternative without code: `strict_placement` is not enough, since the search never goes down.

> All four can live in **a single `tilth` mod**, of which 4.1 would be the core. This is simpler to maintain than one mod per feature.

### 4.5 Changes to the plan

| Where | Change | Reason |
|---|---|---|
| 11.3 | **Remove Leaves Be Gone** | Aeronautics blacklist and duplicate of FallingTree's `leavesBreaking` |
| 10.6 / 10.7 | Every Compat: blacklist of the 3 Quark woods and the `twigs` module | About 165 useless blocks avoided |
| 10.7 "The tree" | Overgeared's axes need no tag; add "sawmill: `bois.js`" | FallingTree jar; misplaced NML recipes |
| 10.12 #6 | FallingTree: `FALL_BLOCK`, `maxSize` 200–250, `leavesBreaking=true` | Default 100 + `ABORT` too low for NML |
| 0005 (Quark) | Cut `Thatch`, `Variant Bookshelves`, **`Automatic Recipe Unlock`**; optional: `Shingles` | Proven duplicates; blueprints |
| 0006 (Supplementaries) | Cut `sconce` and `gravel_bricks` | Proven duplicates with NML and Twigs |
| Twigs (new) | Neutralise `add_pebbles`, `add_land_sea_shells`, `add_water_sea_shells`; decide on `add_ore_rhyolite/schist/bloodstone` | Duplicates NML's decoration; irreversible worldgen |
| 07 #1, #9 / 08.4 | Blueprints: option B (blueprint kept on the crafting table) instead of `doLimitedCrafting` | Proven leaks (Create recipe advancements, TB bag, crafters) |
| 07 #10 | `MoreJS.villagerTrades` | Group name read in the jar |
| 07 #12 | Remove COE's default veins | 15 infinite veins by default |
| 07 #18 | Write `zoo.js` with `WildexKubeJsScriptBridge` | API read in the jar |
| 06 §6.1 | Redo the Entity Culling test with 1.11.3 before any removal | Fix on block entities under NeoForge since 1.11.0 |
| TB config | `sendBackpackCoordinatesMessage=false`, `enableCraftingUpgrade=false` (or option B for blueprints), `spawnEntitiesWithBackpack=false` | No coordinates; blueprints; no equipped monsters |
| Tags (new) | `tags_engins.js`: Sable masses for furniture and roofs; Lootr non-movable | Realistic contraptions; no duplication |
| Versions | Upgrade Sable to 2.0.6, Quark to 4.1-487, Moonlight to 3.7.2; Supplementaries 4.0.2 only after rereading its configuration; NML 1.6.1 (to be seen with the world report) | Modrinth |
| Libraries | Add Puzzles Lib, Balm (client), Configurable | Required dependencies |

---

## 5. Questions that require an in-game test

1. **Every Compat**: `/kubejs dump registry` or search `@everycomp` in EMI. How many blocks for NML? Do the "autumn" and "fruit" foliages have their hedges? Do Quark's woods disappear with the blacklist?
2. Does **EMI** show items from disabled Quark modules, and the Quark or Nether tracks of Steam 'n' Rails?
3. **Death aboard** an airship in flight: where do the grave and the TB bag go? Does the grave block the path of a contraption?
4. **Entity Culling 1.11.3** with an assembled airship carrying block entities (chest, Handcrafted, Supplementaries): bug still there? If not, are `skipBlockEntityCulling` or `blockEntityWhitelist` enough?
5. **Lootr**: does a ruin chest in `create:non_movable` properly refuse Sable assembly? Do NML's pots (`ancient_pot`, alchemist pots) go through Lootr? If they have their own block entity, probably not, and a blueprint in one would be "first come, first served".
6. **Create saw**: is an NML log block processed before `bois.js`? This would confirm or not that NML's recipes do not load.
7. **FallingTree**: NML's largest tree with `maxSize` 250 and `FALL_BLOCK`. Measure the cost with spark: the number of falling block entities and the TPS.
8. **Easy Anvils and Overgeared**: turn a vanilla anvil carrying items (Easy Anvils) into an Overgeared forge anvil. Are the items lost?
9. **Overgeared blueprints (option C)**: does the "Empty Blueprint" screen let one choose a custom type (`rail`…)? If so, option C does not keep the secret.
10. **Dissipator prototype**: can the neighbouring fan's `getSpeed()` be read from `BlockEvents.blockEntityTick`? Does the 64-block conversion pass without a TPS hiccup?
11. **Sable masses**: does a furnished airship (Handcrafted, Macaw roofs) still fly with the `light` and `half_volume` tags? And without these tags?
12. **TB's sleeping bag aboard** a moving contraption: sleep, respawn point (`enableSleepingBagSpawnPoint`), teleport on waking.
13. **Memory**: spark `heapsummary` or F3 with and without Macaw's Roofs (605 blocks). It is the biggest item of v2; keep it only if it does not cost too much.
