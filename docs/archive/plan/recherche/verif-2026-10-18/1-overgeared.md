# 1. Overgeared: study of the jar and links with the pack

> Check of 2026-10-18. Jar studied: `overgeared-neoforge-1.21.1-1.6.19.jar`, the one in `pack/mods/overgeared.pw.toml`. Its sha512 starts with `06b7d48d89a3134…`, as in the toml.
> Sources: the Modrinth API (project `overgeared`, id `SQL3X2Ky`), the classes read with `javap`, the JSON files in the jar and the [official document for modpack creators](https://docs.google.com/document/d/1dUjVXHALGE7b625DYa-cVr0Y6vJkh3NFya0gcDSE_C8) (link given on the Modrinth page). For comparison, the jars of Easy Anvils, Visual Workbench, FallingTree, Traveler's Backpack and Subtle Effects were also opened, along with those of 22 mods in the pack (Create, Cold Sweat, Supplementaries, Quark, the fauna…).
> "deduced" = conclusion drawn from the code or the data, without an in-game test.

---

## 0. The essentials

1. **There is no "forge" block.** Metal is heated in the **vanilla blast furnace**, or in the furnace for copper. It is then **hammered on the smith's anvil** with a mini-game, **quenched** in water and **polished** at the grindstone. The plan's forge is therefore a **workshop to assemble**: blast furnace, anvil, water, grindstone, plus Supplementaries' bellows.
2. **Overgeared already replaces the vanilla recipes.** Tools are no longer crafted at the crafting table: a forged head and a stick are assembled. Diamond, the bucket, the cauldron, shears, the flint and steel, arrows and netherite go through it. The removal planned in `progression.js` is useless: it targets `crafting_shaped`, which no longer exists for these items.
3. **No bronze, no tin, no brass.** The only alloy of the alloy furnace is crude steel (3 iron + 1 coal). Chapters 03 and 08 are wrong on this point.
4. **The "pattern found in the ruins" is not a steel pattern.** It is the **diamond upgrade template** (steel → diamond at the smithing table). Steel, for its part, is made without finding anything.
5. **Everything is tuned through data.** Recipes, tags, per-quality attributes and blueprint types are set in JSON, hence in a datapack or with KubeJS `event.custom()`. The mod has **no KubeJS plugin, no JEI, and no bridge with Create**. It provides an **EMI plugin**, a Polymorph plugin and **Sable/Aeronautics** compatibility.
6. **Overgeared's blueprints largely replace the KubeJS blueprints of §10.10.** An Overgeared blueprint of a hidden type can be the condition for forging a part: the Clearing Lantern, an axle, a propeller blade. It is found while exploring, can be copied and improves with use.
7. **KubeJS and datapacks are enough** for all the links. A small mod is useful only for one finishing detail: sparks at each hammer blow instead of the "critical hit" stars (§2.13).
8. **There is no French translation** (`fr_fr.json` absent). It must be shipped in the pack.

---

## 1. What is confirmed

### 1.1 Version and dependencies

| Point | Value | Evidence |
|---|---|---|
| Latest NeoForge 1.21.1 version | `neoforge-1.21.1-1.6.19`, release, 2026-09-21: **this is the pack's** | Modrinth API `/project/overgeared/version?loaders=["neoforge"]&game_versions=["1.21.1"]` |
| Required dependencies | NeoForge ≥ 21.1.227 (the pack has 21.1.251), Minecraft 1.21.1 | `META-INF/neoforge.mods.toml` |
| Optional dependencies | `quark` ≥ 4, `dragonsurvival` | same |
| Embedded | `sable-companion-common-1.21.1-1.6.0.jar` (jar-in-jar) | `META-INF/jarjar/` |
| License | MIT; source on GitHub `phuccom000/Overgeared-1.20.1` | Modrinth API |
| Config | a single **COMMON** config (class `ServerConfig`, hence file `config/overgeared-common.toml`, name to confirm at first launch) and a client config | `OvergearedMod`: `registerConfig(COMMON, ServerConfig.SERVER_CONFIG)` |
| Languages | de_de, en_us, es_mx, ja_jp, pl_pl, pt_br, ru_ru, vi_vn, zh_cn. **No fr_fr** | `assets/overgeared/lang/` |
| Mixins | server and common only (cooling in containers, entities and inventory; stacking; player; potions) | `overgeared.mixins.json` |
| Useful changes | 1.6.13: "broken" state and Sable/Aeronautics compatibility fixed; 1.6.14: cooling in water on a Sable structure; 1.6.17: hammers per anvil tier; 1.6.18.1: Clockwork compatibility; 1.6.19: hot metal burns like a magma block | Modrinth changelogs |

### 1.2 The smith's chain (read from the jar's recipes)

| Step | What happens | Data |
|---|---|---|
| **Flint and stone** | Right-click with flint on stone: you get a pebble (`overgeared:knappable_rock`). The stone becomes chiseled stone and the flint may break | `rock_interactions/flint_on_stone.json` (30% chance of getting the pebble, 15% of breaking the flint) |
| **Knapping** | Stone tool heads are knapped in a 3 × 3 grid (`overgeared:rock_knapping`), then head + stick = vanilla stone tool | `recipe/stone_*_head.json`, `minecraft/recipe/stone_pickaxe.json` |
| **Stone anvil** | Sneak right-click on `minecraft:stone` with a hammer → stone anvil, "stone" tier, which rises at most to "Well Forged" and wears out (`max_uses`) | tag `overgeared:stone_anvil_bases` = `minecraft:stone` |
| **Hammer** | The copper hammer is crafted at the crafting table (copper + sticks) or with a cast head. The steel hammer comes from a forged head | `copper_smithing_hammer_2.json`, `smithing_hammer.json` |
| **Smith's anvil** | Sneak right-click on a **vanilla anvil** `minecraft:anvil` (31 iron) with a hammer → `overgeared:smithing_anvil`, "iron" tier. It **falls** like the vanilla anvil (class `Fallable`) and **displays the placed items in 3D** (`SmithingAnvilBlockEntityRenderer`) | tag `overgeared:iron_anvil_bases` = `minecraft:anvil` only (neither chipped nor damaged) |
| **Heating** | Iron: **blast furnace only** (ore, raw iron or ingot → `heated_iron_ingot`). The vanilla blast furnace no longer gives a cold iron ingot. Copper and silver: furnace or blast furnace. Crude steel: blast furnace | `heated_*_from_blasting_*.json`, `minecraft/recipe/iron_ingot_from_blasting_*.json` (condition `neoforge:false`) |
| **Hot metal** | Hot metal burns if held without tongs (wooden tongs with sticks, iron or steel). It cools in 1200 ticks (60 s) in the inventory or instantly in water. It smokes and also burns creatures | config `heatedItemCooldownTicks = 1200`, tags `overgeared:heated_metals` and `overgeared:tongs`, recipes `overgeared:cooling` |
| **Hammering** | Ingredients are placed in the anvil's 3 × 3 grid, with the blueprint in its slot if there is one. Right-click with the hammer to start the **mini-game**: an arrow, a shrinking perfect zone, a number of blows (`hammering`). The score gives the quality | `ForgingRecipe$Serializer`, config `perfectQualityScore = 0.9`, `expert = 0.6`, `well = 0.3`, `masterQualityChance = 0.05` |
| **Quenching and polishing** | A hot head must be quenched (water) then polished at the grindstone (`overgeared:grindstones` = vanilla grindstone). Otherwise it stays "unpolished" and its maximum quality drops | `need_quenching`, `has_polishing`, tags `overgeared:grindable` and `overgeared:grinded` |
| **Assembly** | Head + stick at the crafting table, recipe `overgeared:crafting_shapeless` which **carries over the quality** of the head to the tool | `minecraft/recipe/iron_pickaxe.json` |
| **Steel** | Brick alloy furnace (`overgeared:alloy_furnace` = blast furnace surrounded by bricks): 3 iron ingots + 1 coal → 4 crude steel. They are heated in the blast furnace, hammered (3 blows) into hot steel, then cooled into steel ingots | `crude_steel_from_alloying.json`, `heated_steel_ingot.json`, `steel_ingot_from_cooling.json` |
| **Armor** | Forged plates (`overgeared:iron_plate`, `copper_plate`, `steel_plate`, 3 blows, no quality), then forged armor with quality (5 blows) | `iron_chestplate.json`, `iron_plate.json` |
| **Diamond** | No more diamond tool recipe at the crafting table: they yield air. The steel item, a diamond and the **diamond upgrade template** at the smithing table are needed. The template is found in chests and is copied with 7 diamonds and 1 steel (yielding 2) | `steel_*_to_diamond_*.json`, `diamond_upgrade_smithing_template.json`, `loot_modifiers/diamond_upgrade_from_*.json` |
| **Netherite** | Alloy in the Nether alloy furnace (Nether bricks: 4 debris + 4 gold), then master-level hammering (on failure: 4 debris) | `heated_netherite_alloy.json`, `minecraft/recipe/netherite_ingot.json` |
| **Molds** | Clay mold fired in the furnace. It is filled with nuggets or ingots (`material_settings` scale) and fired in the furnace or blast furnace (`cast_smelting`, `cast_blasting`). The head that comes out loses quality. The Nether mold and the **casting furnace** (red Nether bricks + 1 diamond) are for the end game | `casting_furnace.json`, `*_from_cast_*.json`, config `castingToggle = true` |
| **Blueprints** | Paper ×3 + blue dye → blank blueprint; blank blueprint + crafting table → **drafting table**. On the table, the blank blueprint becomes a blueprint of a tool type, which sets the **maximum quality** of the result. The blueprint rises in quality with use (`poorMaxUse`, `wellMaxUse`, `expertMaxUse`). Blank blueprint + blueprint → lower-quality copy | `empty_blueprint.json`, `drafting_table.json`, `blueprint_cloning/*.json`, component `overgeared:blueprint_data` {quality, tool_type, uses} |
| **Arrows** | The fletching table gets an interface (`overgeared:fletching`): iron, steel, diamond, tipped or lingering arrows | `minecraft/recipe/arrow.json` |
| **Broken items** | At 0 durability, an item can stay "broken" instead of disappearing, depending on its quality (50% to 75% for well and expert; other values in the config) | config `enableQualityBreakSystem = true`, `BrokenHelper` |
| **Repair** | The grindstone repairs: +10% durability per pass, −5% maximum durability | config `grindingToggle`, `damageRestore = 0.1`, `durabilityReduce = 0.05` |

Vanilla items routed through the forge (`data/minecraft/recipe/`):
- iron, gold and stone tools and armor;
- diamond recipes, disabled;
- bucket (3 iron, stone anvil is enough), cauldron (**steel plates**), shears;
- **flint and steel** (flint + steel ingot);
- arrows, spectral arrows and tipped arrows;
- netherite ingot.

### 1.3 Tool tiers (jar tags)

| Tier | Can mine | Evidence |
|---|---|---|
| Wood, stone, gold | **not iron ore** (`overgeared:needs_copper_tool` = iron ores and iron blocks) | `minecraft/tags/block/incorrect_for_stone_tool.json` |
| Copper (new) | iron, but not `minecraft:needs_iron_tool`: diamond, gold, redstone and **Create's zinc** | `incorrect_for_copper_tool.json`; Create: `needs_iron_tool` = zinc ore |
| Iron | everything, except `overgeared:needs_steel_tool` (obsidian, crying obsidian) | `incorrect_for_iron_tool.json` |
| Steel | everything, except `minecraft:needs_diamond_tool` | `incorrect_for_steel_tool.json` |

Deduced: **zinc, hence Create's brass, requires a forged iron pickaxe.** This is a natural "forge before Create" lock, without a script.

### 1.4 Quality: data and effects

- The `overgeared:forging_quality` component is a `StringRepresentable` enumeration: `poor`, `well`, `expert`, `perfect`, `master`. Source: `ModComponents`, `ForgingQuality`.
- The other components: `heated`, `polished`, `blueprint_data`, `cast_data`, `creator` (the smith's name in the description), `failed_result`, `blueprint_required`, `heated_time`, `tipped_uses`, `reduced_grind_count`, `lingering_status`.
- **Per-quality attributes are tuned through data** (`data/<ns>/quality_attributes/*.json`).
  - Possible targets: `weapon` (any `TieredItem` or `ProjectileWeaponItem` class), `armor` (`ArmorItem`), `item`, `item_tag`, `item_all`.
  - Operations: `add`, `mult_base`, `mult_total`.
  - Source: `QualityTarget$TargetType`, `QualityAttributeHandler` (`ItemAttributeModifierEvent`).
  - Default values:
    - damage: from −1 (poor) to +3 (master);
    - armor: from −1 to +2;
    - durability: ×0.7 to ×1.6;
    - mining speed: ×0.7 to ×1.5.
- **Quality works on items from any mod**, as soon as the component is present.
- **Loot**: the global modifier `overgeared:all_tools_give_quality`, unconditional, gives a randomly drawn quality to **every item that wears out and comes from a loot table**. This applies to chests, Lootr and creatures, and therefore also to Mowzie's boss items (tables `mowziesmobs:entities/ferrous_wroughtnaut`, `umvuthi`, `frostmaw`) and NML ruin loot.
  - Default weights: poor 50, well 30, and so on.
  - Exclusions: tag `overgeared:quality_blacklist`.
  - Source: `QualityLootModifier`, `global_loot_modifiers.json`.
- **Extra loot**:
  - `steel_ingot_from_*` and `diamond_upgrade_from_*` in vanilla chests: mineshaft, dungeon, stronghold, desert pyramid, shipwreck, mansion, jungle temple, ancient city, outpost, buried treasure;
  - iron arrows in the jungle temple dispenser.
- **Trades**:
  - armorer, toolsmith and weaponsmith: vanilla trades wrapped with a quality, plus trades of forged items and steel for emeralds;
  - wandering trader: sells blueprints.
  - Source: `ModEvents.onVillagerTrades`, `onWanderingTraderTrades`.

### 1.5 What is tuned through data (no code)

| Folder or tag | Role |
|---|---|
| `data/<ns>/recipe/` | 21 serializers: `forging`, `rock_knapping`, `alloy_smelting`, `shaped_alloy_smelting`, `nether_alloy_smelting`, `shaped_nether_alloy_smelting`, `casting`, `cast_smelting`, `cast_blasting`, `nbt_smelting`, `nbt_blasting`, `cooling`, `grinding`, `fletching`, `item_to_tooltype`, `crafting_shapeless`, `crafting_cloning`, `crafting_cast`, `crafting_initial_cast`, `campfire_heating` (`ModRecipeSerializers`) |
| `data/overgeared/blueprint_tooltypes/` | extra blueprint types (`{"tooltypes":[…]}`) |
| `casting_tooltypes/`, `material_settings/` | mold quantities and material value |
| `quality_attributes/` | quality effects |
| `rock_interactions/` | right-click of an item on a block → an item drops (the pebble) |
| `knapping_resources/` | what can be knapped, with its texture and sound |
| `durability_blacklist/`, `grinding_blacklist/`, `broken_blacklist/` | exclusions |
| item tags | `heated_metals`, `hot_items`, `tongs`, `smithing_hammers` (and `stone_`, `iron_`, `tier_a_`, `tier_b_smithing_hammers`), `tool_parts`, `quality_blacklist`, `knappables`, `grindable`, `grinded` |
| block tags | `smithing_anvil`, `stone_anvil_bases`, `iron_anvil_bases`, `tier_a_anvil_bases`, `tier_b_anvil_bases`, `grindstones`, `needs_copper_tool`, `needs_steel_tool` |

**Fields of an `overgeared:forging` recipe** (serializer and official doc):
- `pattern`, `key`: each key is an ingredient, with optional `requires_heated` and `transfer_nbt`;
- `result` and `result_failed` (in 1.21.1, `{"id": …}` and not `{"item": …}`);
- `hammering`, `tier` (`stone`, `iron`, `above_a`, `above_b`; `iron` by default);
- `blueprint` (list of types), `requires_blueprint`;
- `has_quality`, `minimum_quality` (key read in the jar: `minimumQuality`), `quality_difficulty`;
- `needs_minigame`, `has_polishing`, `need_quenching`;
- `category` (`TOOL_HEADS`, `ARMORS`, `misc`…), `show_notification`.

**Blueprint types**: config `availableToolTypes` (sword, axe, pickaxe, shovel, hoe) and `hiddenToolTypes`. A hidden type exists but **does not appear on the drafting table**, according to the config comment. The language key `tooltype.overgeared.<type>` is also needed.

Other useful settings, with their default value:
- `enableMinigame = true`;
- `enableBlueprintForging = true`;
- `ingredientsDefineQuality` (an ingredient's quality limits the result's);
- `maxAnvilDistance`;
- `enableTieredHammerForging = false`, `enableTierA = false`, `enableTierB = false`;
- `coolingBlockEntityBlacklist` (vanilla furnace and blast furnace);
- `enableLootQuality = true` and its weights;
- `failOnWellQualityChance = 0.1`, `failOnExpertQualityChance = 0.05`.

### 1.6 Integrations present in the jar

| Integration | Present? | Evidence |
|---|---|---|
| **EMI** | **Yes**: forging, knapping, alloys, casting, cooling, grindstone, fletching, drafting table, blueprint copying, molds | `compat/emi/` (30 classes, including `OvergearedEmiPlugin`). Since 1.6.16, tiers A and B are hidden in EMI by default |
| JEI | **No** in 1.21.1. The `jei.*` keys in the language file are leftovers. Chapter 06 ("native JEI plugin in Overgeared") is wrong, but EMI is retained anyway | no JEI class; beta-v2 changelog: "I switched over to EMI instead of JEI" |
| KubeJS | No (no plugin). Recipes are written with `event.custom({...})`, or in JSON in `kubejs/data/` | no `kubejs` class |
| Create | No. Only `c:` tags are shared (§2.1) | — |
| Quark | Replaces Quark's 5 "Better Stone Tools" recipes with head + stick | `data/quark/recipe/tweaks/crafting/utility/better_stone_tools/` |
| Sable / Aeronautics | Yes (cooling in water on a Sable structure) | `compat/sable/SableCompat`, jarjar `sable-companion` |
| Polymorph, Dragon Survival | Yes (mods outside the pack) | `compat/polymorph/` |
| Item capability (`Capabilities.ItemHandler.BLOCK`) | Exposed on the alloy furnace, the Nether alloy furnace and **the 4 anvils**: hoppers, Create funnels and mechanical arms can fill and empty them | `ModCapabilities` |

---

## 2. The concrete links to build

File names follow the tree of chapter 07.

### 2.1 Create: who does what

**Principle**: **hammering stays manual**, and the mod enforces it. The mini-game is played client-side and the quality comes from the score (beta-v9 changelog: the mini-game starts server-side, but is played client-side). No machine can produce a quality. Create can, however, take over everything that is **quantity and heat**.

| Link | What | Tool |
|---|---|---|
| **Industrial heating** | Create's **"blasting" bulk processing** (fan in front of lava or a burner) reads the `minecraft:smelting` **and** `minecraft:blasting` recipes. An iron ingot or raw iron in front of the fan therefore becomes a `heated_iron_ingot`, and crude steel a hot crude steel. Nothing to write. *Evidence: `AllFanProcessingTypes$BlastingType` reads `RecipeType.SMELTING` and `RecipeType.BLASTING`.* | None. To forbid it, recreate these heating recipes with an id ending in **`_manual_only`**: Create ignores them in all its machines (`AllRecipeTypes.CAN_BE_AUTOMATED`, `_manual_only` string confirmed in the jar) |
| **Steel in bulk** | Heated mixer: 3 iron ingots + 1 coal → 4 crude steel (same ratio as the alloy furnace). With a superheated burner, hot steel can come out directly | `recipes/forge_create.js`: `event.recipes.create.mixing('4x overgeared:crude_steel', ['3x minecraft:iron_ingot','minecraft:coal']).heated()` (KubeJS Create) |
| **Automated alloy furnace** | A hopper or mechanical arm feeds the alloy furnace, which exposes its item capability | None. To test (§5) |
| **Brass** (plan 03 §3.4: "first at the Overgeared foundry") | Overgeared does not make any. An `overgeared:alloy_smelting` recipe is added: 3 copper + 1 zinc → 4 `create:brass_ingot`, with a long cooking time. The mixer remains the fast route | `event.custom({type:'overgeared:alloy_smelting', ingredients:[…], result:{id:'create:brass_ingot', count:4}, cookingtime:300})` |
| **Plates** | `#c:plates/iron` contains `create:iron_sheet` (Create tag) **and** `overgeared:iron_plate` (Overgeared tag). The 16 Create recipes that require `#c:plates/iron` therefore already accept hand-forged plates. In the other direction, Overgeared's armors require the exact item `overgeared:iron_plate` | Recommended option: rewrite the 4 iron, 4 copper and 4 steel armors with `{"tag":"c:plates/iron"}` (then copper, steel). The **press makes the plates** (quantity), the anvil makes the **armor** (quality). `event.replaceInput({type:'overgeared:forging'}, 'overgeared:iron_plate', '#c:plates/iron')`, to check: `replaceInput` must traverse the keys of this custom type, otherwise rewrite as `event.custom` |
| **Steel plates at the press** | `create:pressing`: `#c:ingots/steel` → `overgeared:steel_plate`. Create only does this for Immersive Engineering (`compat/immersiveengineering/plate_steel.json`, condition `mod_loaded`) | `event.recipes.create.pressing('overgeared:steel_plate', '#c:ingots/steel')` |
| **Steam engine, rails** (`progression.js`) | `#c:plates/steel` and `#c:nuggets/steel` **do exist** (Overgeared tags). The draft's lines are correct | — |
| **Critical parts with a minimum quality** | An `overgeared:forging` recipe with `has_quality: true` on the KubeJS items `tilth:rivet_forge`, `tilth:essieu_forge`, `tilth:pale_helice_forgee`… marks them with the `overgeared:forging_quality` component. In the Create or crafting table recipe, the ingredient filters the quality with the NeoForge ingredient **`neoforge:components`** (`{"type":"neoforge:components","items":"tilth:pale_helice_forgee","components":{"overgeared:forging_quality":"perfect"},"strict":false}`). For "at least excellent", group several values in `neoforge:compound` | Datapack or `event.custom`. To check: that EMI and Create's sequenced assembly display and accept these ingredients (deduced: Create 6 reads the vanilla `Ingredient` codec, which understands NeoForge ingredients) |
| **Deployer on the anvil** | A recipe without quality or mini-game (plates, bucket) advances with a hammer right-click (`increaseForgingProgress`). A deployer holding a hammer could therefore forge plates on its own | To test. If it must be forbidden: `BlockEvents.rightClicked` in KubeJS, cancel if `event.player.isFake()` and the block is in `#overgeared:smithing_anvil` |
| **Tools made by Create** | No Create recipe makes a metal tool (analysis of the Create 6.0.10 jar: only wrench, goggles, copper diving gear, cardboard sword) | — |

### 2.2 Create Ore Excavation

The mod is not yet in the pack: identifiers to be read in its jar when it is added.
- The drill can require a forged head: `overgeared:steel_pickaxe_head` filtered by quality with `neoforge:components` (§2.1), rather than a new item `tilth:tete_foreuse_forgee`. The real pickaxe head, and its quality, are thus reused.
- Deduced: if COE drills wear out, a drill coming from a `has_quality: true` forging gains the **durability bonus** of its quality. The `IItemStackExtensionMixin` mixin acts on any item that wears out and carries the component. To test.
- The extracted raw ore then goes through the normal chain: blast furnace or fan, then anvil.

### 2.3 Create Aeronautics and Sable

- Compatibility is in the mod (1.6.13 and 1.6.14: cooling in water or in a cauldron placed on a Sable structure is possible).
- The "excellent quality" propeller blades of chapter 03 are made like critical parts (§2.1).
- Risk: the smith's anvil **falls** like an anvil. On an assembled contraption, check whether it drops or injures (§5).

### 2.4 The Clearing Lantern (tier 32, §10.3)

"Parts forged at Overgeared" is replaced by a real forging recipe that directly gives the KubeJS block:
- **Startup**: the block `tilth:lanterne_de_clairiere` is already in `clairieres.js`.
- **Config** `overgeared-common.toml`:
  - `availableToolTypes = ["sword","axe","pickaxe","shovel","hoe","lanterne"]`;
  - `hiddenToolTypes = ["lanterne"]`, so that no drafting table produces it.
- **Language**: `kubejs/assets/overgeared/lang/fr_fr.json` → `"tooltype.overgeared.lanterne": "Lanterne de clairière"`.
- **Recipe** (`recipes/forge_tilth.js`, `event.custom`):

```json
{
  "type": "overgeared:forging",
  "category": "misc",
  "tier": "iron",
  "blueprint": ["lanterne"],
  "requires_blueprint": true,
  "has_quality": false,
  "needs_minigame": true,
  "hammering": 8,
  "pattern": ["PGP", "GLG", "PSP"],
  "key": {
    "P": {"item": "overgeared:steel_plate"},
    "G": {"item": "minecraft:glass_pane"},
    "L": {"item": "minecraft:lantern"},
    "S": {"item": "overgeared:heated_steel_ingot", "requires_heated": true}
  },
  "result": {"id": "tilth:lanterne_de_clairiere"},
  "result_failed": {"id": "overgeared:steel_plate", "count": 2},
  "show_notification": true
}
```

- **The blueprint**: `zoo.js` (§10.10, after 25 species) gives an `overgeared:blueprint` with the component `overgeared:blueprint_data = {quality:"poor", tool_type:"lanterne", uses:0}`. It can also be hidden in a ruin (§2.5).
- **What the player feels**: a smith's anvil (31 iron), steel, a successful mini-game, a blueprint earned. It is a real tier.

### 2.5 Blueprints found in the ruins (§10.10)

Proposal: **separate** two families of blueprints.
- **What is forged** (critical parts, lantern, special heads): **Overgeared blueprints of hidden types**, one type per blueprint: `atelier`, `rail`, `aerostat`, `helice`, `lanterne`…
  - The `overgeared:forging` recipe requires them (`requires_blueprint: true`).
  - The blueprint **sets the maximum quality**, which rewards exploration, and it **rises in quality with use**.
  - It **can be copied** (blank blueprint + blueprint) while losing a level: it can be shared among friends.
  - The Create machines that require these parts are locked **in cascade**. There is no more need for `doLimitedCrafting`, `recipe give/take` or disabling Quark's "Automatic Recipe Unlock".
- **What is not forged**: `plans_ingenieur.js` remains useful only if a crafting table recipe must be locked without a forged part.
- **Loot**: LootJS (`loot/butin.js`), for example:

```js
LootJS.lootTables(event => {
  event.getLootTable('nomansland:chests/small_dungeon_core')
    ?.firstPool()
    .addEntry(LootEntry.of(Item.of('overgeared:blueprint',
      '[overgeared:blueprint_data={quality:"poor",tool_type:"rail",uses:0}]')).when(c => c.randomChance(0.3)))
})
```

The exact LootJS 3 syntax is to be validated. Fallback without LootJS: a `neoforge:add_table` pointing to a small table with `minecraft:set_components`. The NML tables exist (read in the 1.5.12 jar):
- `nomansland:chests/small_dungeon_core`, `dungeon_garbage`, `dungeon_kitchen`, `redstone_parts`, `water_well`;
- `alchemist_pot_*`, `ancient_pot_cave`, `sniffer_monument`;
- `nomansland:archaeology/remains_dungeon`.

**The diamond upgrade template** must also enter these tables. Overgeared only adds it to vanilla structures, several of which are removed by the plan: strongholds (decision 0004) and dungeons (`remove_dungeons.json`).

### 2.6 Tools and armor from other mods: through the forge or not?

Only the recipes that yield a tool, weapon or armor were examined (scan script, pack jars).

| Mod | Item | What happens | Proposal |
|---|---|---|---|
| Kaleidoscope Cookery | iron, gold and diamond kitchen knives, `kitchen_shovel`, farmer's boots and leggings (iron) | at the crafting table, with ingots | **At the forge**: remove the crafting table recipe and add an `overgeared:forging` (hot iron + stick, `has_quality: true`). The knives are probably `TieredItem`s: they would then take the quality and its damage (deduced). For diamond, keep the crafting table with a forged iron knife in the center, or a smithing table. This is the most visible link between cooking and the forge |
| Mowzie's Mobs | `naga_fang_dagger` (iron + naga fang); bosses: Wroughtnaut axe and helmet, Sol Visage… | crafting table; boss loot through entity tables | Dagger: leave at the crafting table (the fang is the trophy) or forge it, as desired. Bosses: they **already** receive a random quality through the global modifier. Add them to `overgeared:quality_blacklist` if a "Poorly Forged" on a boss trophy is unwanted |
| Alex's Mobs | moose headgear (iron), crocodile chestplate, roadrunner boots, depths shield… | crafting table, from creature parts | Leave: these are trophies, not metalwork |
| Friends & Foes | Wildfire crown; Rascal reward (vanilla tools) | crafting table; loot table | The reward takes a quality on its own. Nothing to do |
| Cold Sweat | goat fur, hoglin hide and chameleon armors | crafting table, no metal | Leave |
| Hybrid Aquatic | coral and shell tools, **copper diving gear** | crafting table | Tools: leave. Diving gear: at the forge if consistency with copper is wanted (optional) |
| Create | copper diving helmet and boots | crafting table | Leave (workshop equipment) or forge, as desired |
| Critters & Companions | dragonfly armor in iron, gold and diamond | crafting table | Leave (animal armor) |
| Adorable Hamster Pets | hamster armors by templates | crafting table and smithing table | Leave |
| Companions | "crystallized blood" equipment (from netherite), mage hat (from a diamond helmet), `netherite_dagger` | crafting table | With the Nether closed, anything requiring netherite is **inaccessible**. Hide it in EMI or reassign it. The mage hat already goes through a diamond helmet, hence through the forge |
| Quark | experimental hammer | experimental category disabled | — |

**Proposed rule**: **metal** weapons, tools and armor **intended for the player** go through the forge. Creature trophies, animal armor and clothing stay at the crafting table. Their quality, when they have one, comes from loot.

### 2.7 FallingTree

- The FallingTree 1.21.1.11 jar detects axes by `instanceof AxeItem` (`ItemWrapper.isAxe`).
- Overgeared's axes (`steel_axe`, `copper_axe`) are **vanilla `AxeItem`s** (`ModItems`: 2 × `new AxeItem`), and vanilla iron or gold axes come from forged heads. **They all count**, without a tag.
- Automatic link: each log costs durability, and quality multiplies durability. A "Masterwork" axe fells more trees (deduced).
- Point to test: an axe in Overgeared's "broken" state.

### 2.8 Cold Sweat: the forge warms

- **Lit blast furnace, furnace, smoker**: Cold Sweat already handles them (`FurnaceBlockTemp` targets `AbstractFurnaceBlock` with `LIT`). Nothing to do.
- **Alloy furnace and casting furnace**: they do not derive from `AbstractFurnaceBlock` (class `BaseEntityBlock`), but their block has the `lit` state (blockstate). Two ways to add them:
  - in a datapack, in `data/tilth/cold_sweat/block/block_temp/forge.json` (registry folder `block/block_temp` confirmed in `ModRegistries`). Fields: `block` (with the state `lit: true`), `temperature`, `range`, `max_effect`, `fade`, `units`. The exact format of the `block` sub-object is to be copied from the Cold Sweat wiki;
  - in KubeJS: `ColdSweatEvents.registries(e => e.addBlockTemperature(temp, 'F', b => b.blocks('overgeared:alloy_furnace','overgeared:casting_furnace').state('lit', true).range(5).maxEffect(…)))`. The signature `addBlockTemperature(double, String, Consumer)` and the constructor are read in `ModRegistriesEventJS` and `BlockTempBuilderJS`.
- **Hot ingot held in hand**: registry `item/item_temp`. The tag `#overgeared:heated_metals` can warm the player carrying it a little. A nice detail: forging in the cold, in winter, becomes a refuge.
- **Steel and copper armor**: Cold Sweat does not know them. Add them to `item/insulator` like iron armor (to tune).

### 2.9 Supplementaries and Amendments

- **The bellows** (enabled in `supplementaries-common.toml`) speed up the blocks of the tag `supplementaries:bellows_tickable`, which already contains the blast furnace.
  - Add `overgeared:alloy_furnace` and `overgeared:casting_furnace` to this tag in a datapack. The bellows become the visible piece of the forge (to test: `tickFurnaces` must call the ticker of a block from another mod).
  - Source: `data/supplementaries/tags/block/bellows_tickable.json`, `BellowsBlockTile.tickFurnaces`.
- **The faucet** fills the quenching cauldron. But the cauldron requires forged **steel plates**. Before that, quenching is done in a block of water.
- **Pedestal and shelf**: masterpieces are displayed there. This is the smith's showcase.
- **Amendments** draws vanilla lanterns in 3D. Deduced: a simple-type KubeJS block will not receive this rendering. If the Clearing Lantern must sway like a lantern, a KubeJS block inheriting from `LanternBlock` is needed. To check among KubeJS 2101 block types, otherwise the model stays static.

### 2.10 Traveler's Backpack

- The tool slots accept `travelersbackpack:acceptable_tools` = `#c:tools` + diamond pickaxe. Overgeared puts its steel and copper tools, its **hammers** and its **tongs** in `c:tools`. Everything fits, without tuning.
- **"Blast furnace" upgrade** of the backpack: it allows heating iron **while traveling**. A choice must be made:
  - either it is intended (field forge);
  - or it is forbidden, by removing its recipe or preventing it from accepting iron.

  Cooling in a container (`ContainerCoolingMixin` mixin) will make ingots cool in the backpack (deduced).

### 2.11 Quark

- Overgeared replaces Quark's "Better Stone Tools" recipes. Nothing to do.
- **Variant Furnaces** (enabled): the deepslate and blackstone furnaces have their own block entity type. They may be missing from `coolingBlockEntityBlacklist`, which lists only `minecraft:furnace` and `minecraft:blast_furnace`: a hot copper ingot would cool in the furnace. Add its id to the config, to be read in game.
- **Automatic Tool Restock** (which §11.3 wants to enable): with Overgeared's "broken" system, the tool at 0 durability **does not always disappear**. The restock, which triggers when the tool breaks, may not follow (§5).
- **Automatic Recipe Unlock**: no need to disable it if §2.5 is followed (Overgeared blueprints).

### 2.12 EMI

- Native (§1.6). Recipes added by datapack or `event.custom` of the same type display on their own: the plugin reads the recipe manager by type (deduced).
- Hide in EMI what does not exist in Tilth: netherite, Nether alloy furnace, Nether mold, diamond tools at the crafting table (air recipes).
- Setting: `kubejs/client_scripts`, `RecipeViewerEvents.removeEntries` (KubeJS 2101), to validate.

### 2.13 Sounds, Particular, Subtle Effects: the sparks

- Overgeared's hammer blows make **`CRIT`** particles spurt: white stars, like a critical hit. This is not "Realism Craft". *Evidence: `ParticleUtils`, `BaseSmithingAnvilBlock` cite only `ParticleTypes.CRIT`; hot metal smokes (`SMOKE`); the alloy furnaces produce flame and smoke.*
- Subtle Effects has "Anvil Use Particles", "Grindstone Use Particles" and "Smithing Table Use Particles". Deduced: these options follow vanilla uses (the anvil menu, the grindstone menu), not Overgeared's hammering or polishing, which go through a network packet and a click on the block. The **furnace sparks** (`furnaceSparks`) work on the vanilla blast furnace, hence on the forge.
- KubeJS cannot intercept the mini-game blows: they go through the packet `PacketSendCounterC2SPacket`, not through a click on the block.
- **Small custom mod (optional)**: a client mixin on the creation of the anvil's particles would replace `CRIT` with sparks (`minecraft:lava`, `small_flame`) and embers, and could play a sound from the Sounds mod. About fifty lines. It is **the only link that requires code**.
- Polytone could recolor `CRIT` to orange, but **everywhere**, including in combat. To avoid.
- Sounds (IMB11): no conflict expected. Overgeared has its own sounds (`overgeared:anvil_hit`, `forging_complete`, `forging_failed`), which Sounds does not touch.

### 2.14 Visual Workbench and Easy Anvils

- **Visual Workbench** (jar 21.1.2): it converts the crafting table block into its own inventory block (`BlockConversionHandler`, `CraftingTableWithInventoryBlock`). Overgeared does not depend on the crafting table block: the drafting table is crafted with the **item** `minecraft:crafting_table`, which does not change. **No conflict.**
- **Easy Anvils**: **real conflict** (§3.1).

### 2.15 What to translate and document

- `kubejs/assets/overgeared/lang/fr_fr.json`: the 294 keys of `en_us.json`, plus the hidden blueprint types.
- The settler's notebook (Modonomicon), "Forge" chapter:
  - the pebble;
  - the stone anvil;
  - the copper hammer;
  - iron requiring a copper pickaxe;
  - the blast furnace, tongs, quenching, the grindstone;
  - the smith's anvil;
  - steel;
  - blueprints.

---

## 3. Conflicts and risks

### 3.1 Easy Anvils (§11.3)

- Easy Anvils 21.1.0 **replaces every `AnvilBlock`** with an inventory copy, registered under `easyanvils:<namespace>/<path>`, hence `easyanvils:minecraft/anvil`. The vanilla anvil item then places this new block, and already-placed anvils are converted as soon as they are used. *Evidence: `BlockConversionHandler.onRegistryEntryAdded` (`instanceof AnvilBlock`, id built with `"\u0001/\u0001"`), `onUseBlock`, `AnvilWithInventoryBlock`.*
- Yet **Overgeared only turns into a smith's anvil the blocks of the tag `overgeared:iron_anvil_bases` = `minecraft:anvil`**. With Easy Anvils, the player places an anvil, strikes it with a hammer… and nothing happens. **They can no longer obtain the smith's anvil.**
- Possible fix: add `easyanvils:minecraft/anvil` to the tag (datapack). Two unknowns remain: which mod keeps the sneak right-click with a hammer, and what happens to the items stored in Easy Anvils' anvil when Overgeared replaces the block.
- **What Easy Anvils brings is already in Overgeared**:
  - the smith's anvil already shows its items in 3D (`SmithingAnvilBlockEntityRenderer`);
  - repair goes through the grindstone.

  Only renaming and the end of "too expensive" on the vanilla anvil would remain, which is now only a step toward the smith's anvil.
- **Recommendation: remove Easy Anvils from §11.3.** If it is kept, add the tag and run a dedicated test.
- Overgeared's smith's anvil is not an `AnvilBlock` (it inherits from `BaseEntityBlock`): Easy Anvils does not touch it.

### 3.2 The closed Nether (decision 10.12-9)

Several pieces become impossible to craft:
- **Nether alloy furnace** (Nether bricks);
- **casting furnace** (red Nether bricks + diamond);
- **Nether mold**;
- **netherite alloy** and **netherite ingot**;
- Companions' "crystallized blood" equipment.

What is done:
- clay molds fire in the furnace and blast furnace, so casting remains possible without a casting furnace;
- reassign the casting furnace recipe to overworld materials (mud bricks, deepslate bricks) or hide it;
- hide the Nether furnace (crude steel is already made in the brick furnace) and netherite.

### 3.3 Village smiths (`survivants.js`)

- Overgeared **adds** trades of forged items and steel, and **wraps** the vanilla trades of armorers, toolsmiths and weaponsmiths (`VillagerTradesEvent`).
- `MoreJSEvents.villagerTrades` with `removeVanillaTrades` may not touch the trades added by Overgeared, depending on the listener order. Check in game, and use `removeModdedTrades` or a filtered `removeTrades` if needed.
- The wandering trader sells blueprints, but it is disabled (`doTraderSpawning false`). Nothing to do.

### 3.4 Random quality on all loot

- Every loot table gives a quality to items that wear out: chests, Lootr, bosses, the Rascal reward.
- This is consistent, but a "Perfectly Forged" chestplate found early short-circuits the forge.
- Tune the loot quality weights (favor poor and well) and put boss trophies in `overgeared:quality_blacklist` if desired.

### 3.5 A longer early game than planned

- Iron ore requires a **copper pickaxe**.
- The bucket, shears and cauldron go through the anvil. The flint and steel and the cauldron require **steel**.
- The standby fire (tier 1, "by hand") must therefore not require a flint and steel. Campfires are placed lit.
- Mods that require a bucket (Thirst, Kaleidoscope Cookery) wait for the first stone anvil.

### 3.6 Miscellaneous

- **Fan heating**: hot ingots on a Create conveyor belt. Cooling of transported items is not covered by Overgeared's mixins (`ItemEntity`, containers, inventory): they would stay hot on the belt (deduced). And picking them up without tongs hurts.
- **Falling anvil**, on a Sable contraption or under a bridge: risk of damage and breakage (`onLand`, `getFallDamageSource`).
- **Plate duplicates**: `overgeared:iron_plate` and `create:iron_sheet`, the copper plate and the copper sheet. Without Almost Unified (removed in §10.5), both exist; the `c:plates/*` tag links them. Hiding them in EMI is not required.
- **Base stone**: `stone_anvil_bases` = `minecraft:stone` alone. NML or Quark stones (limestone, jasper…) do not give an anvil. Accept, or extend the tag.
- **Updates**: the mod asks to **delete the config after an update** if something breaks (Modrinth page). Ship our `overgeared-common.toml` in `pack/config/` and compare it at each version.

---

## 4. What to adapt, remove or add in the plan

| Place | Change | Reason |
|---|---|---|
| 03 §3.1 and §3.4, age 1 | "Stone anvil, forge, first forged iron, **bronze**" → "flint and pebble, stone anvil, copper hammer, **copper pickaxe for iron**, blast furnace and tongs, smith's anvil, **steel**" | No bronze or tin in Overgeared (§1.2) |
| 03 §3.4, age 3 | "Brass first at the Overgeared foundry": keep the idea, but write the `overgeared:alloy_smelting` recipe (§2.1) | Overgeared has no brass |
| 02, loot table; 03 §3.6 and §3.9 | "Steel forge pattern" → "**diamond upgrade template**" (`overgeared:diamond_upgrade_smithing_template`), to add to NML tables | It is the steel → diamond upgrade. Steel has no pattern |
| 03 §3.9, Overgeared row; `progression.js`, age 0-1 | Remove "vanilla metal tool recipes removed (config or KubeJS)" and the `event.remove(... crafting_shaped)` loop | Overgeared already does it. Vanilla iron and gold tools **remain** as forged items. No config option does that |
| 06, foundation table (JEI) | "native plugin in … Overgeared": false for JEI; **EMI** plugin | §1.6 |
| 07, uncertainties and `overgeared_pieces.js` | Fill in: component `overgeared:forging_quality`; recipe types (§1.5); KubeJS via `event.custom` | §1.4 and §1.5 |
| 08, step 11 | Closed: component, recipe types, vanilla tools, tin (absent), template loot, Nether (§3.2) | — |
| `objets_tilth.js` | Keep `tilth:rivet_forge`, `essieu_forge`, `pale_helice_forgee`… as **products of a `has_quality: true` forging**. Replace `tilth:tete_foreuse_forgee` with `overgeared:steel_pickaxe_head`, filtered by quality. The `tilth:plan_*` items become **Overgeared blueprints of hidden types** (§2.5) | Reuse the mod's mechanics instead of duplicating them |
| `plans_ingenieur.js` | Reduce to nothing, or to the sole case of a crafting table recipe without a forged part. The lock goes through `requires_blueprint` | More robust than `recipe give/take` and `doLimitedCrafting` |
| `zoo.js` (§10.10) | Reward for the 25 species = `overgeared:blueprint` of type `lanterne` | §2.4 |
| §10.3, tier 2 | "Parts forged at Overgeared" → "forged on the **smith's anvil** from a lantern blueprint, plates and hot steel" (§2.4) | A precise recipe |
| §10.7, "Heat" | Add: blast furnace (already handled) and alloy furnace (Cold Sweat datapack); hot ingots in hand (`item_temp`) | §2.8 |
| §10.7, new link "The forge" | Blast furnace + bellows (tag `bellows_tickable`) + anvil + water or cauldron + grindstone; Create fan for heating; mixer for steel; press for plates; part quality read by `neoforge:components` | §2.1, §2.9 |
| §10.7, "The tree" | Confirmed: all axes (`AxeItem`) count, without a tag | §2.7 |
| §11.3 | **Remove Easy Anvils**, or keep it with the tag `overgeared:iron_anvil_bases` and a test | §3.1 |
| §11.3, Quark Automatic Tool Restock | Enable, but test it with Overgeared's "broken" state | §2.11 |
| New `pack/config/overgeared-common.toml` | Hidden blueprint types; loot quality weights; `coolingBlockEntityBlacklist` (+ Quark furnaces); `enableQualityBreakSystem` to decide | §1.5 |
| New `kubejs/assets/overgeared/lang/fr_fr.json` | French translation | §1.1 |
| New `recipes/forge_create.js` | Mixer → crude steel; press → steel plate; brass at the alloy furnace; armors accepting `#c:plates/*`; `_manual_only` option | §2.1 |
| New `recipes/forge_tilth.js` | Lantern, critical parts, Kaleidoscope Cookery knives at the forge | §2.4, §2.6 |
| New `data/tilth/cold_sweat/block/block_temp/forge.json` and tag `supplementaries:bellows_tickable` | Heat and bellows | §2.8, §2.9 |
| Decisions | A record `docs/decisions/00xx-overgeared.md`: manual forging and industrial heat; Overgeared blueprints; Easy Anvils; Nether | Repository rule |

**Is a custom mod needed?** **No** for the gameplay links: datapack, config, tags and KubeJS (`event.custom`, KubeJS Create, LootJS, Cold Sweat's KubeJS API) cover everything. **Yes, optionally**, for a single finishing detail: a small client mod that replaces the hammering `CRIT` sparks with embers and orange sparks (§2.13). If one day a Create machine must produce an item with an imposed quality, and Create cannot put a component into its results, this mod could also handle it. It is not necessary with the principle of §2.1.

---

## 5. Questions that need an in-game test

1. **Lantern with a hidden blueprint**: is a type added to `availableToolTypes` and `hiddenToolTypes` recognized? Does a recipe with `requires_blueprint: true` and `has_quality: false` really require the blueprint? Does the blueprint given by command or loot with `blueprint_data` work?
2. **The quality component**: is it written `overgeared:forging_quality="expert"`? Is the `neoforge:components` ingredient accepted and displayed by the crafting table, Create's sequenced assembly and EMI?
3. **Heating with a Create fan**: does iron in front of lava really give `heated_iron_ingot`? Does it cool on a belt, in a depot, in a funnel? Does the `_manual_only` suffix block this processing?
4. **Deployer with a hammer** on the anvil (plate recipe): does the forging advance? Do hoppers and mechanical arms fill the anvil and the alloy furnace?
5. **Easy Anvils** (if kept): with the extended tag, does the conversion to a smith's anvil work? Which mod keeps the sneak click? Are the items in the anvil lost?
6. **`event.replaceInput`**: does it traverse the keys of an `overgeared:forging` (plates → `#c:plates/iron`)?
7. **Bellows** on the alloy furnace (after adding to the tag): does it really speed it up?
8. **Cold Sweat**: the heat of the alloy furnace and of hot ingots in hand; the insulation of steel and copper armor.
9. **Quark Automatic Tool Restock** with a tool that goes to the "broken" state instead of disappearing. **FallingTree** with a broken axe.
10. **Quark furnaces**: does a hot copper ingot cool in them? What is the id of their block entity for `coolingBlockEntityBlacklist`?
11. **Smith trades**: after `survivants.js`, do the armorer and toolsmith still sell items forged by Overgeared?
12. **Sable contraption**: place a smith's anvil, forge in flight, quench in a cauldron on board; does the anvil fall on disassembly?
13. **Traveler's Backpack "blast furnace" upgrade**: does it heat iron, and do ingots cool in the backpack?
14. **Multiplayer mini-game**: `maxAnvilDistance`, anvil occupied by another player. Can a friend watch without getting in the way?
15. **Kaleidoscope Cookery knives** moved to the forge: do they take the quality (class `TieredItem`) and keep their cutting function?
