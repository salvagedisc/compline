# Verification 2: food, heat and seasons, around Kaleidoscope Cookery

Checked on 2026-10-18. The jars were opened, the JSON read and the classes disassembled with `javap`. Version numbers come from the Modrinth API (NeoForge, 1.21.1). The extracted jars are kept outside the repository, one folder per mod and version.

Notation of the evidence:
- **[jar]**: JSON or class read in the jar;
- **[MR]**: version or changelog read on Modrinth;
- **[deduced]**: reasoning from the code, without an in-game test.

## Contents

0. Versions checked
1. What is confirmed, mod by mod
2. The links to build (what, with which tool)
3. Conflicts and risks
4. What to adapt, remove or add in the plan
5. Questions to settle with an in-game test

---

## 0. Versions checked [MR]

| Mod | In the pack | Latest 1.21.1 NeoForge version | Remark |
|---|---|---|---|
| Kaleidoscope Cookery | 1.5.1 | **1.6.0** (2026-09-30) | 1.6.0: the millstone accepts any creature more than one block tall (blacklist instead of whitelist), "Quick Knife" enchantment for the knife, the chopping board gives several results |
| KaleidoscopeCookery-Automation | 1.0.2 | 1.0.2 | Requires KC ≥ 1.2.1 and Create ≥ 6.0.6 |
| Kaleidoscope Compat | — (addition planned) | **2.9.7-Patch.2** (2026-10-10) | Requires KC ≥ 1.4.0. Uses **Resourceful Config**, already in the pack (3.0.11), without declaring it as a dependency [jar: `com.teamresourceful.resourcefulconfig` annotations in `MainConfig`] |
| Let's Do Farm & Charm | 1.1.25 | **1.1.29** (2026-10-16) | 1.1.27 adds a **chopping board** and **choppers** (iron, diamond, netherite), two duplicates of KC [MR changelog, jar 1.1.29] |
| Freeze It and Heat It | — (addition planned) | 4.1.1 (2026-09-19) | Requires `cold_sweat` in `[2.3, 3)`: compatible with 2.4.3.1 and 2.4.4 [jar `neoforge.mods.toml`] |
| Cold Sweat | 2.4.3.1 | 2.4.4 (2026-10-16) | 2.4.4 reworks water temperature (freezing point per biome) |
| Ecliptic Seasons | 0.15.0-rc-3-1 | **0.15.4.1** (beta, 2026-10-16) | 0.15.4 fixes cold-region crops that grew as in summer at the start of winter |
| Thirst Was Reclaimed | 3.0.6 | 3.0.6 | modid `thirst`, package `cn.mlus.thirst` |
| Traveler's Backpack | — (addition planned) | 10.1.39 (2026-08-29) | — |

---

## 1. What is confirmed

### 1.1 Kaleidoscope Cookery 1.5.1

**Stations and heat** [jar]:
- The **wok** (`kaleidoscope_cookery:pot`), the **stockpot** (`stockpot`), the **steamer** (`steamer`) and the **teapot** (`teapot`) look for heat in the **block just below** (`hasHeatSource`).
- The rule is the same for all four:
  - if the block has the `lit` property, it must be lit;
  - otherwise, it must be in the tag `kaleidoscope_cookery:heat_source_blocks_without_lit`, which contains fire, soul fire, lava, magma block and `#farmersdelight:heat_sources`.
- KC itself provides `farmersdelight:heat_sources`, which contains its stove: the tag loads even without Farmer's Delight.
- Consequence: **a lit vanilla campfire is enough** for the wok, the stockpot and the teapot. So is Farm & Charm's stove, because it uses `BlockStateProperties.LIT` [jar F&C `StoveBlock`]. Even a lit candle is enough [deduced].
- The **KC stove** (`stove`) is lit with a flint and steel or a fire charge, without fuel, and burns without limit. It goes out if it rains on it (`isRainingAt` at the block above, in `randomTick`). Ecliptic's local rain therefore decides whether it stays lit [jar].

**"Warmth" effect** [jar `WarmthEffect`, lang]:
- About twenty dishes give the effect `kaleidoscope_cookery:warmth`.
- It regenerates health near a lit block or a block of the tag `warmth_heat_source_blocks`.
- It does not touch Cold Sweat's temperature.

**Other effects**:
- `tundra_strider`: fast on snow. It is a true winter dish with Ecliptic.
- `mustard` (creepers flee) and `sulfur` (phantoms flee) have **no effect** in Tilth, since these monsters are removed.

**Crops**: tomato, chili, lettuce, rice (+ wild rice), tea plant.
- Seeds drop from grass (`minecraft:blocks/short_grass`) **only if the straw hat is worn** (loot modifier `straw_hat_seed_drop`).

**Season tags** [jar]:
- `sereneseasons:{spring,summer,autumn}_crops`, as block and as item:
  - tomato: summer, autumn;
  - chili: summer, autumn;
  - rice: summer, autumn;
  - lettuce: spring, autumn;
  - tea plant: spring, summer, autumn.
- Ecliptic **humidity** tags, `eclipticseasons:crops/<humidity>`, for example rice `humid_humid`, tomato `dry_average`.
- Ecliptic **does read the `sereneseasons:*` tags** and converts them [jar Ecliptic `CompatCropHookInternal`]. KC crops are therefore seasonal without doing anything.

**JSON recipe types** [jar, file count]:

| Type | Recipes |
|---|---|
| `kaleidoscope_cookery:pot` | 198 |
| `flex_pot` | 27 |
| `stockpot` | 77 |
| `flex_stockpot` | 28 |
| `millstone` | 85 |
| `bamboo_tray` | 28, with `subtype` `drying` or `wetting` and `duration` |
| `chopping_board` | 16 |
| `teapot` | 7 |
| `steamer` | 4 |
| `rice_bowl` | 5 |

**Bamboo tray**:
- It dries under open sky and without rain (`canSeeSky`, `isRainingAt`) [jar].
- **No dried meat recipe** exists by default: drying only targets tea, kelp, mud, rotten flesh (which gives leather) and sponge.

**KubeJS API** [jar `ModKubeJSPlugin`]:
- Recipe schemas in the `kaleidoscope_cookery` namespace: `pot`, `flex_pot`, `chopping_board`, `stockpot`, `flex_stockpot`, `millstone`, `steamer`, `bamboo_tray`.
- **No schema for the teapot**: go through `event.custom({...})`.
- Bindings `KCookery` and `KCookerySoupBase` (soup bases).

Schema fields:

| Schema | Fields |
|---|---|
| `pot` | `result, ingredients, carrier, time, stir_fry_count` |
| `stockpot` | `result, ingredients, soup_base, time, carrier, cooking_texture, …` |
| `bamboo_tray` | `result, ingredient, subtype, duration` |
| `steamer` | `result, ingredient, cook_tick` |
| `chopping_board` | `result, ingredient, model_id, cut_count` |

**Public Java events**: `StockpotMatchRecipeEvent`, `MillstoneMatchRecipeEvent`, `RecipeItemEvent`, `SickleHarvestEvent`. KubeJS only listens to them with `NativeEvents` [deduced].

**Millstone** [jar]:
- It is pulled by an animal, chosen by the tag `millstone_bindable` (donkey, mule, horse, llama, cow, sheep, goat, villager) and the datamap `millstone_bindable_data`.
- In 1.6.0, any creature more than one block tall can pull it, except for a blacklist [MR].

**Excess content for Tilth**:
- Chairs, tables, stools, bench and "Eight Immortals table", in vanilla wood, plus the Every Compat variants (compatibility in KC).
- Scarecrow, straw hat and farmer's clothing.
- Added village houses (`AddVillageStructuresEvent`).

### 1.2 KaleidoscopeCookery-Automation 1.0.2 [jar]

- Interaction points for the **Create mechanical arm**: wok, stockpot, steamer, shawarma spit, table, fruit basket.
- Link with Create 6 packages: the recipe keeps the package address (`PackageSender`).
- 8 `create:cutting` recipes that produce KC's meat cuts.
- No configuration.

### 1.3 Kaleidoscope Compat 2.9.7-Patch.2: what it really does [jar]

**Datapack mode** (`datapack_mode`, default value **COMPAT**):
- **COMPAT**: adds the F&C items to the `c:` tags, so that they are interchangeable (F&C lettuce in `c:crops/lettuce`, F&C bacon in `c:foods/raw_bacon`, F&C chicken cuts in `c:foods/raw_chicken`).
  - Adds to the F&C mincer the KC tomato and lettuce → KC seeds recipes.
  - Several recipes target `farmersdelight:*` or `candlelight:*`: they fail, with an error in the log and no other effect.
- **UNITE**: removes duplicates. **Dangerous without Farmer's Delight**:
  - the pack `unite_farm_and_charm` replaces F&C recipes with versions that produce `farmersdelight:onion`, `:bacon`, `:chicken_cuts` and `:raw_pasta`;
  - these recipes fail, and F&C's originals disappear with them (same path, priority pack);
  - it **cuts the generation** of F&C's wild carrots, potatoes, beetroots, **onions** and tomatoes (`rarity_filter` 9999999999), assuming that Farmer's Delight provides them;
  - **in Tilth, the onion would become impossible to find** [deduced from the JSON].
- **NONE**: only the soup bases.

**Setting `cooking_pot_recipes_disabled`** (category `farm_and_charm`, false by default):
- Despite its name, it does not disable F&C's cooking recipes.
- It adds a datapack that **cancels the crafting** of the block `farm_and_charm:cooking_pot`, with the condition `forge:false`, and puts it in the tag `kaleidoscope_cookery:united`, which hides it from the creative tab and from JEI.
- The `farm_and_charm:pot_cooking` recipes remain, and **Compat replays them in the KC stockpot** (`afterStockpotRecipeMatch` on `StockpotMatchRecipeEvent.Post`, if `farm_and_charm_compat_enabled`, true by default).
- F&C's soups, teas (nettle, plantain, strawberry), yeast and goulash are therefore cooked in the KC stockpot.

**Thirst** (`thirst_compat_enabled`, true by default):
- Registers the hydration of **all KC soups**, **all KC teas** (read in `TeacupRegistry`) and the Kaleidoscope Tavern drinks.
- Goes through `cn.mlus.thirst.api.ThirstHelper`, present in TWR 3.0.6 [jar of both mods].

**Create**:
- Mechanical arm for wok, stockpot, steamer, millstone, spit, teapot (`create.arm.*_enabled`).
- Interaction on **Create contraptions** for 16 KC blocks (`create.contraption.*`): a kitchen in a wagon without Kaleidoscope Contraption.

**Miscellaneous**:
- Fix for sickle harvesting with Quark (`quark.sickle_harvest_fix_enabled`).
- Scarecrow that repels phantoms (no effect here).
- Blacklist for the "Transmutation Lunch Bag".
- AppleSkin, Curios and Accessories compatibilities.

**What Compat does not settle**:
- The tomato and lettuce duplicates remain in COMPAT mode.
- The F&C stove, roaster, crafting bowl, mincer, scarecrow, the new chopping board and the F&C choppers are not touched.
- The hiding only targets **JEI** (`compat/jei/RecipeHiding`). Compat's **EMI** plugin only adds one category (barbecue).
- **Compat does not know that KC-Automation exists**: no reference to `kaleidoscopecookery_automation` in the jar.

### 1.4 Let's Do Farm & Charm 1.1.25 (and 1.1.29) [jar]

**Recipe types**: `mincer` (53), `drying` (19, silo and window sill), `pot_cooking` (10), `stove` (9), `roaster` (9), `crafting_bowl` (4). 1.1.27 adds `cutting_board` (36) and `cutting_board_assembly` (9).

**Crops**: barley, oats, corn, strawberry, onion, tomato and lettuce (duplicates of KC), plus 13 wild plants.

**Seasons: the F&C tags are misplaced.**
- They are in `data/sereneseasons/block/*.json` and `data/sereneseasons/item/*.json`, **without the `tags/` folder**. They are therefore not tags, and Ecliptic does not read them. Same mistake in 1.1.25 and in 1.1.29.
- The `data/farm_and_charm/seasons/*.json` files (coefficients per season) are useless under NeoForge [deduced].
- **F&C crops grow all year round with Ecliptic** as long as Tilth does not tag them.

**NeoForge configuration**: `enableBonemealEffect`, `fertilizedSoilRange`, `waterSprinklerRange`, `enableRainGrowthEffect`, `rainGrowthMultiplier`, `feedingTroughRange`, `enableFertilizer`, taming and effects of the horse, chicken, dog and cat, nutritional values of each dish [jar `FarmAndCharmNeoForgeConfig`].

**Feeder**: its food is the tag `farm_and_charm:feeding_trough_food`, which contains `#minecraft:villager_plantable_seeds`. KC adds its seeds to it.

**Fertiliser**: the silo recipe `fertilizer_from_rotten_flesh` already exists (rotten flesh → fertiliser).

### 1.5 Freeze It and Heat It 4.1.1 [jar]

**Mechanic**: each food carries a temperature. Depending on the place, it becomes fresh, then slightly, almost or completely frozen or rotten. Eating an altered food gives a penalty (effect `fiahi:shiver`). A completely rotten food becomes a leftover (`fiahi:leftover_meat` or `fiahi:leftover_vegetable`, 65% compostable), according to data-driven `fiahi:food_rotting` recipes.

**Where it measures temperature**:
- **Player inventory**: the player's `CORE` temperature in Cold Sweat (`InventoryMixin`).
- **Item on the ground**: `WorldHelper.getTemperatureAt`.
- **Containers**: at each world tick, walks through the `Container` block entities of loaded chunks (`ForgeEventHandler.onLevelTick`, `collectContainerPositionsByChunk`).

**Exemptions**: Cold Sweat's boiler and icebox (`STABLE_TEMPERATURE_CONTAINERS`); they bring the food back to the fresh state thanks to FIAHI's mixins.

**Provisions bag** (`fiahi:food_pouch`): it keeps its own temperature and can go into the icebox or the boiler.

**Configuration** (`fiahi-common-config`): `NEVER_FROZEN_FOODS`, `NEVER_ROTTEN_FOODS`, `ENABLE_FROZEN`, `ENABLE_ROTTEN`, `TEMPERATURE_CHECKER_INTERVAL`, `TEMPERATURE_BALANCE_RATE`, `FROZEN_SPEED_MULTIPLIER`, `ROTTEN_SPEED_MULTIPLIER`, `STABLE_TEMPERATURE_CONTAINERS`.

**No KubeJS API.** It is adjusted through configuration, tags and `fiahi:food_rotting` recipes.

### 1.6 Cold Sweat 2.4.3.1 [jar]

**Default hot blocks** (`coldsweat/world.toml`, "Block Temperatures"):

| Block | Setting | Reading |
|---|---|---|
| Lit boiler | `15 °F, range 7, max 36` | — |
| Frosted icebox | `-15 °F` | — |
| Lava, lava cauldron | `30 °F, range 7` | — |
| `#minecraft:fire` | `25 °F, range 7` | — |
| `#minecraft:campfires` | `["#minecraft:campfires", 25, 7, "f", 50, "lit=true", "", 400]` | **+25 °F, 7 blocks, cumulative effect max 50 °F** |
| Magma | `20 °F` | — |
| Ices | negative | — |
| `create:blaze_burner` | depending on its state | — |

- Lit vanilla furnaces, smokers and blast furnaces: by code (`FurnaceBlockTemp`, `AbstractFurnaceBlock` + `lit`).
- **KC and F&C stoves do not count.**

**File-based data**: `data/<ns>/cold_sweat/{block/block_temp, item/food, item/fuel, world/biome_temp, world/temp_region, entity/...}`.

**KubeJS API** [jar `KubePlugin`, `KubeBindings`, `ModRegistriesEventJS`]:
- **`coldsweat` binding**:
  - `getTemperature(entity, trait)`, `setTemperature`;
  - `convertTemperature(value, from, to, absolute)`;
  - **`createModifier(id)`**: **a single argument**, it looks in the registry;
  - `addModifier(entity, modifier, trait)`;
  - `getModifiers`, `getTrait`;
  - `getBlockTemperature(LevelBlock)`, `getBiomeTemperature(level, pos)`, `getTemperatureAt(level, pos)`.
- **`ColdSweatEvents` group** (server scripts): `registries`, `gatherDefaultModifiers`, `temperatureChanged`, `addModifier`, `applyInsulation`.
- **`registries`** offers:
  - `addBlockTemperature(temp, units, b => b.blocks(...).state('lit', true).range(r).maxEffect(m).fades(true))`;
  - `addFoodTemperature(f => f.items(...).temperature(t).duration(d))`;
  - `addBiomeTemperature` and `addBiomeOffset`, `addStructureTemperature`;
  - `addHearthFuel`, `addBoilerFuel`, `addIceboxFuel`;
  - **`addTempModifier(id, data => temp => newTemp)`**, which creates a real Cold Sweat modifier whose JS function is called at every calculation.

**Key point for the seasons** [jar `WorldHelper.getTemperatureAt`, `getDummyPlayer`]:
- The temperature of a place is computed with a "ghost player" that receives the **default modifiers** (`EntityTempManager.gatherTempModifiers`, which triggers `DefaultTempModifiersEvent`, relayed to KubeJS by `gatherDefaultModifiers`).
- A seasonal modifier added in `gatherDefaultModifiers` on the `world` trait therefore applies **both**:
  - to players;
  - to the container temperature read by FIAHI;
  - to the `getTemperatureAt` binding [deduced from the bytecode].

**Native seasons**: only Serene Seasons (`SereneSeasonsTempModifier`, settings "Summer/Autumn/Winter/Spring Temperatures"). **Nothing for Ecliptic.**

**Thirst compatibility**: water purity is kept in the waterskin (`CompatManager$Thirst`).

**Icebox**: it accepts `#c:foods` (`cold_sweat:icebox_valid`). KC dishes (tag `kaleidoscope_cookery:meals`) are probably not in `c:foods` [deduced: KC only tags `c:foods/raw_*` and `cooked_*` sub-tags].

**Sleep**: the "Sleep Check Override Blocks" list only contains `#comforts:sleeping_bags` by default.

### 1.7 Ecliptic Seasons (0.15.0-rc3 in the pack, 0.15.4.1 read) [jar]

**Crops, data-driven**:
- `data/eclipticseasons/eclipticseasons/crop/seasons/<combination>.json` gives a growth chance per solar term; same principle for `crop/humidity/<h1>_<h2>.json`.
- The targets are **block tags** `eclipticseasons:crops/<spring|summer|…|spring_summer|…|all_seasons>` and `eclipticseasons:crops/<humidity>`.
- Item tags of the same name.
- Exemptions: `crops/unaffected_by_seasons`, `not_killed_by_climate`.

**Seasonal breeding**: entity tags `eclipticseasons:breed/<seasons>`.
- For example, cow, sheep, pig, horse, donkey, llama and frog are in `spring_summer`; wolf and bee in `spring`.
- **No modded animal is tagged.**

**Agricultural climates**: `agro_climate` (temperate, hot, cold…). The `eclipticseasons:agro/*` biome tags are empty by default; the choice is made by code according to the biome's temperature [deduced].

**Configuration**: `HeatStroke`, `EnableSeasonalCrop`, `EnableCropHumidityControl`, `RestrictBoneMeal`, `ForceCompatMode`, greenhouses, `EnableSeasonalBreed`, `EnableTimeBreed`, `EnableSeasonalBee`, `EnableSeasonalFishing`, `FishingSeasons`, `IceAndSnow`, `WaterFreezesInFrozenBiomes`, `SnowKeepInSnowyBiomes`…

**KubeJS**:
- `EclipticSeasonsApi`, which is the **instance** of the API: `getSeason(level)`, `getSubSeason(level)`, `getSolarTerm(level)`, `getSeasonSignal(level, pos)` (season of the local climate), `isRainAt`, `getAdjustedHumidity`, `isSnowAt`…
- `EclipticSeasonsBindings`.
- Enumeration `Season$Sub`: `EARLY_SPRING` … `LATE_WINTER`, `NONE`. The names in the draft are correct.
- **Ecliptic does not change the vanilla temperature of biomes** (`MixinBiome` only touches rain): Cold Sweat therefore sees no season without a bridge.

### 1.8 Thirst Was Reclaimed 3.0.6 [jar]

**Files**: `config/thirst/common.toml`, `item_settings.toml` ("Drinks" and "Foods" lists in the format `[["id", hydration, quenching]]`, plus a blacklist), `keyword.toml`, `container.toml`.

**Default keywords**:
- drinks: `drink|juice|tea|soda|coffee|wine|beer|cider|…`;
- soups: `soup|stew|porridge`;
- fruits: `…berry|berries|apple|melon…`.
- F&C teas (`nettle_tea_cup`…) and F&C soups (`barley_soup`…) are therefore **already recognised**.
- The **F&C strawberry is not**: "strawberry" does not match the pattern, which requires a separator before "berry".

**Default values**: the KC tomato and a few KC soups are already listed there.

**Link with Cold Sweat**: TWR reads the **body** temperature (`Temperature.get(…, BODY)`) to speed up thirst. A hotter summer through the seasonal bridge therefore gives more thirst, without adding anything.

**Purification**: `campfire_cooking` and furnace recipes for bottle, bucket and terracotta bowl; Create sand filter.

**API**:
- `ThirstHelper.addDrink(item, h, q)` and `addFood`, static;
- NeoForge event `RegisterThirstValueEvent`.

### 1.9 Traveler's Backpack 10.1.39 [jar]

**Upgrades**: tanks, crafting table, **furnace**, **smoker**, blast furnace, pickup, jukebox, magnet, trash can, **automatic feeding**, restocking, lantern (dynamic light with LambDynamicLights).

**Server configuration**:
- per upgrade: `enableTanksUpgrade`, `enableCraftingUpgrade`, `enableFurnaceUpgrade`, `enableSmokerUpgrade`, `enableBlastFurnaceUpgrade`, `feedingUpgradeSettings.enableFeedingUpgrade`, `enableLanternUpgrade`…;
- `enableSleepingBag`, `quickSleepingBag`, `enableSleepingBagSpawnPoint`.

**Sleeping bag**: blocks `travelersbackpack:<colour>_sleeping_bag`, plus the item tag `travelersbackpack:sleeping_bags`.

**Hose**: one can drink the tank water (`WaterEffect`), but the effect only extinguishes fire and gives an effect depending on the biome's temperature. **It only knows Tough As Nails (`compat/toughasnails`), not TWR.** Drinking from the hose therefore does not quench thirst in Tilth [deduced].

**Inventory**: the placed backpack (`BackpackBlockEntity`) **does not implement `Container`**, and the worn backpack is not in the player's `Inventory`. **FIAHI therefore never ages the food stored in the backpack** [deduced].

---

## 2. The links to build

### 2.1 Heat: cooking → Cold Sweat

| What | Tool | Detail |
|---|---|---|
| Lit KC stove and F&C stove warm the room | KubeJS `ColdSweatEvents.registries`, or JSON `data/tilth/cold_sweat/block/block_temp/` | `event.addBlockTemperature(12, 'f', b => b.blocks('kaleidoscope_cookery:stove','farm_and_charm:stove').state('lit', true).range(4).maxEffect(20).fades(true))`. A **weaker** effect than the campfire (25 °F, 7 blocks), because the KC stove **burns without fuel**: it must not replace Cold Sweat's hearth |
| Vigil Fire (tier 1 of the clearings) | Tag + Cold Sweat KubeJS | The draft makes it a simple KubeJS block, without a `lit` state. Add it to `farmersdelight:heat_sources` (so that the wok recognises it), to `kaleidoscope_cookery:warmth_heat_source_blocks` and to an `addBlockTemperature` entry (~20 °F, 6 blocks). It does not purify water: only the vanilla campfire carries the TWR recipes |
| Boiling cauldrons, braziers, fireplaces (10.7) | Cold Sweat config or KubeJS | These blocks come from Amendments and Supplementaries, not from KC: they are outside this verification. Only blocks with a `lit` or `boiling` state can be hooked in with `.state(...)` |
| Hot dishes that warm, cold dishes that cool | KubeJS `event.addFoodTemperature(...)` | Hot dishes: KC soups (`borscht`, `pork_bone_soup`, `lamb_and_radish_soup`, `laba_congee`…), F&C soups, KC teas. Cold dishes: `cold_*_sashimi`, `frost_lamb_chop`, `cold_cut_ham_slices`. Finally gives meaning to KC's "Warmth" effect in winter. To calibrate with `coldsweat.convertTemperature` |
| Icebox and boiler accept KC and F&C dishes | Tags `cold_sweat:icebox_valid`, `cold_sweat:boiler_valid` | Add `#kaleidoscope_cookery:meals`, `#kaleidoscope_cookery:feasts` and the F&C dishes. Without this, the icebox only takes `#c:foods` |

### 2.2 Seasons: Ecliptic → Cold Sweat, FIAHI, thirst

**Rewrite `saisons_cold_sweat.js`.** The only copy is the draft `plan/kubejs-brouillons/server_scripts/saisons_cold_sweat.js`; no branch of the repository contains it (`test/agriculture` only has `.gitkeep` files in `pack/kubejs/`).

**What is wrong in the draft**:
- `coldsweat.createModifier('tilth:season', offset)` does not exist: a single argument, read in the registry.
- The tick every 1200 ticks only touches players.
- The container temperature, hence FIAHI, does not see the season.

**Good architecture** (KubeJS is enough, [jar]):

```js
// server_scripts/saisons_cold_sweat.js (sketch, values in °C to calibrate)
const TILTH_SAISON_C = { EARLY_SPRING: -2, MID_SPRING: 0, LATE_SPRING: 1, EARLY_SUMMER: 3, MID_SUMMER: 6,
  LATE_SUMMER: 4, EARLY_AUTUMN: 1, MID_AUTUMN: -2, LATE_AUTUMN: -4, EARLY_WINTER: -6, MID_WINTER: -8, LATE_WINTER: -6 }

ColdSweatEvents.registries(event => {
  event.addTempModifier('tilth:saison', data => {
    const level = data.entity().level()
    if (String(level.dimension()) !== 'minecraft:overworld') return t => t
    const sub = String(EclipticSeasonsApi.getSubSeason(level))
    const off = coldsweat.convertTemperature(TILTH_SAISON_C[sub] ?? 0, 'c', 'mc', false)
    return t => t + off
  })
})

// Added to every tracked entity AND to the "ghost player" that computes the temperature of a place.
ColdSweatEvents.gatherDefaultModifiers(event => {
  event.addModifier('world', coldsweat.createModifier('tilth:saison'))
})
```

**What this script links on its own**:
1. The perceived temperature, according to the season.
2. FIAHI's freezing and rotting according to the season, in the inventory (body temperature) **and** in chests (temperature of the place).
3. TWR's thirst, which depends on body temperature.
4. Water that freezes, if Cold Sweat's freeze handling is active (see 3.5).

**Finer variant**: use `getSeasonSignal(level, pos)` for hot and cold climates. This function returns a `Season`, not a sub-season, hence fewer nuances.

**Ecliptic**: `HeatStroke = false` (already planned).

### 2.3 Preservation: FIAHI in the game

| Link | Tool | Detail |
|---|---|---|
| Preserved foods never rotten | FIAHI config `NEVER_ROTTEN_FOODS` and `NEVER_FROZEN_FOODS` | KC dried tea, dried kelp, bread, rice cooked in a bamboo tube (to choose). The configuration expects **identifiers**, not tags: generate the list |
| Leftovers → fertiliser | JSON recipe `farm_and_charm:drying` | `fiahi:leftover_meat` and `fiahi:leftover_vegetable` give `farm_and_charm:fertilizer`, like the existing recipe for rotten flesh. The loop closes: waste feeds the field |
| Specific rotting | `fiahi:food_rotting` recipes (`event.custom`) | For example, the rotten KC tomato becomes `farm_and_charm:rotten_tomato`, if that item is kept |
| Travel rations | FIAHI provisions bag | It slows freezing and rotting. It is the expedition item |
| KC "Preservation" effect | Test | It cancels the penalties of rotten flesh; check whether it also cancels `fiahi:shiver` |

### 2.4 Crops and seasons: Ecliptic tags

**F&C**: datapack or KubeJS, `ServerEvents.tags('block')` **and** `('item')`.
- The crop **blocks** must be tagged: `farm_and_charm:barley_crop`, `oat_crop`, `corn_crop`, `strawberry_crop`, `onion_crop`, and, if they are kept, `tomato_crop` and `lettuce_crop`.
- The seeds (items) must also be tagged, for tooltips.
- The draft `cuisine_unifiee.js` only tags `sereneseasons:*` **items**. Ecliptic decides growth by the **block**: `eclipticseasons:crops/<season>` must be added on the blocks.

Proposed distribution:

| Crop | Seasons | Humidity |
|---|---|---|
| Barley | `spring_summer` | `dry_average` |
| Oats | `spring_summer_autumn` (hardy) | `average_moist` |
| Corn | `summer` | `average_moist` |
| Strawberry | `spring_summer` | `average_moist` |
| Onion | `spring_autumn` | `dry_average` |
| **Winter** | nothing outdoors: Ecliptic greenhouse or stores | — |

**KC**: already tagged, nothing to do.

**Clearing** (`tilth:clairiere`, temperature 0.7, precipitation 0.6):
- It will fall into Ecliptic's **temperate** climate [deduced: empty `agro` tags, choice by temperature].
- Rice (`humid_humid`) will grow poorly there: it is a swamp crop.
- Add the biome to the `eclipticseasons:seasonal` tags, seasonal colour, so that it changes hue with the seasons, if desired.

### 2.5 Thirst: drinks from both mods

| Source | Already set? | To do |
|---|---|---|
| KC soups and teas | **Yes**, through Kaleidoscope Compat (`thirst_compat_enabled = true`) | Nothing |
| F&C soups and teas | **Yes**, through TWR keywords | Nothing, or fine values in `item_settings.toml` |
| F&C strawberry, F&C tomato, KC fruit platters | KC tomato yes (TWR default), the rest no | `item_settings.toml` → Foods |
| F&C well water | No | Outside this verification (TWR block purity). The well pumps a source 6 blocks below it (F&C 1.1.26) |
| Traveler's Backpack tanks | **No**, the hose does not quench thirst | Custom mod (see 2.9), or tanks used only to fill bottles and waterskins |
| Cold Sweat waterskin | TWR purity kept, `CompatManager$Thirst` | Test: does it quench thirst with TWR? |

### 2.6 Food for the zoo and farm animals

Everything is driven by tags [jar of the fauna mods]:
- **Vanilla 1.21.1**: `minecraft:cow_food`, `sheep_food`, `goat_food` += F&C barley and oats; `pig_food` += F&C corn; `chicken_food` += `farm_and_charm:kernels` and F&C seeds (KC already adds its seeds).
- **Naturalist**: `naturalist:bird_food_items`, `duck_food_items`, `boar_food_items`, `capybara_food_items`, `turkey_food_items`… (vanilla seeds only) += F&C and KC seeds, KC rice.
- **Alex's Mobs**: 51 tags `alexsmobs:*_breedables` (gazelle: wheat, moose: dandelion…) += F&C oats and barley for the herbivores.
- **Untitled Duck Mod**: `duck_breeding_food` and `goose_breeding_food` already include `#c:seeds`, hence F&C and KC seeds: **already linked**.
- **No Man's Land**: `nomansland:deer_food` (pumpkin, apple, pear) += F&C strawberry; `moose_food` = golden carrot.
- **Critters and Companions**: `red_panda_food` = bamboo, `otter_food` = fish. Nothing to add.

**Seasonal breeding**: tags `eclipticseasons:breed/<seasons>` for the zoo species.
- Deer, bears and birds in spring, for example.
- The entity identifiers are to be read in each mod.
- Without a tag, the species breeds all year round [deduced].

**KC millstone pulled by the zoo animals**:
- in 1.5.1, add the desired entities to `kaleidoscope_cookery:millstone_bindable` and to the datamap `millstone_bindable_data` (speed);
- in 1.6.0, it is automatic for any tall animal.

**F&C feeder**: it puts animals in love. Remove its recipe (`event.remove({ output: 'farm_and_charm:feeding_trough' })`), as the draft plans. The drinking trough can stay.

### 2.7 Duplicates to remove and what to keep

| Domain | KC | F&C | Keep | How |
|---|---|---|---|---|
| Tomato | `tomato`, `tomato_seed`, `tomato_crop` | `tomato`, `tomato_seeds`, `tomato_crop` on a rope, `rotten_tomato`, `wild_tomatoes` | **KC** | Compat in COMPAT mode makes the two interchangeable (`c:crops/tomato`). Remove the recipe `farm_and_charm:seeds_tomato`. With LootJS, make KC seeds drop from F&C's wild tomatoes (this keeps the discovery in the world). Hide the F&C items in EMI |
| Lettuce | `lettuce`, `lettuce_seed` | `lettuce`, `lettuce_seeds`, `wild_lettuce` | **KC** | Same treatment |
| Flour | `flour` (millstone) | `flour` (mincer) | **KC** | Change the result of `farm_and_charm:mincer/flour` to KC (like UNITE mode, but without that mode). F&C recipes read `#farm_and_charm:flour` = `#c:flour`: KC flour passes |
| Dough | `raw_dough` | `dough` (crafting bowl + yeast) | **KC** | Change the result of `farm_and_charm:crafting_bowl/dough` to `kaleidoscope_cookery:raw_dough` (as UNITE does) |
| Stockpot | `stockpot` | `cooking_pot` | **KC** | Compat: `cooking_pot_recipes_disabled = true` (removes crafting; recipes are replayed in the KC stockpot) |
| Stove | `stove` | `stove` (bread oven: farmer's bread, cake, grilled corn, roasted dishes) | **Both** | KC has **no bread oven**. The F&C stove is also a heat source for the wok. Removing it, as the draft proposes, would lose 9 dishes; keep it as the farm's "oven" |
| Roaster, pan | `pot` (wok) | `roaster` | **KC** | Remove the roaster and **port its 9 recipes** to `kaleidoscope_cookery:pot` (native KubeJS schema) |
| Crafting bowl | — | `crafting_bowl` | To decide | Only source of F&C dough and of animal feed (before 1.1.27). Keep it as long as F&C stays at 1.1.25 |
| Board, knives | `chopping_board`, kitchen knives | `cutting_board`, iron, diamond, netherite choppers (**1.1.27 and later**) | **KC** | Stay on **F&C 1.1.25**, or, if moving to 1.1.29, remove the board and the choppers and port the 36 cutting recipes (flowers → dye, animal feed…) to `kaleidoscope_cookery:chopping_board` |
| Millstone, mincer | `millstone` (pulled by an animal) | `mincer` (manual, 53 recipes, including ores, wood stripping, seeds) | **Both** | Different roles; only flour is duplicated (see above) |
| Drying | Bamboo tray | Window sill, silo (`drying`) | Both | The silo makes fertiliser and bone meal; the tray makes tea |
| Scarecrow | `scarecrow` | `scarecrow` (makes crops grow) | **F&C** | Remove the KC recipe `kaleidoscope_cookery:scarecrow` |
| Teas | Teapot (7 teas) | Pot teas (nettle, plantain, strawberry) and `tea_jug` | Both | F&C teas go into the KC stockpot thanks to Compat |
| Straw | `straw_block` | `straw_stable_floor` | Both | Different roles |
| Furniture | Chairs, tables, stools, bench | — | To decide with Handcrafted (10.6) | Duplicate of **Handcrafted**. Proposal: keep the **cook's stool**, useful to KC, and hide the rest |

**Hiding in EMI**: Compat only hides for JEI. A KubeJS client script is needed (`RecipeViewerEvents.removeEntries`; KubeJS 2101's support for EMI remains to be checked with ProbeJS), or an EMI index file in a resource pack.

**Compat mode**: stay in **COMPAT** (default). **Never** switch to UNITE without Farmer's Delight (see 1.3).

### 2.8 Cooking while travelling and in the clearings

**The typical bivouac** (everything is verified in the jars):
- Vanilla campfire: Cold Sweat heat, TWR water purification (`campfire_cooking` recipes), vanilla cooking.
- The **KC wok** placed on top: it sees the fire (`lit`). Oil and the spatula are needed.
- The **KC teapot** on the fire gives teas that quench thirst (Compat).
- The Traveler's Backpack **sleeping bag** to sleep.

**In Traveler's Backpack**:
- Disable `enableFurnaceUpgrade` and `enableSmokerUpgrade`: a furnace and a smoker in the pack short-circuit the campfire and KC.
- Also disable `enableFeedingUpgrade`: eating remains a gesture.
- Keep `enableCraftingUpgrade`, `enableTanksUpgrade` and `enableLanternUpgrade` (LambDynamicLights).
- To be validated with the maintainer.

**Sleeping in the cold**:
- Add the 16 blocks `travelersbackpack:*_sleeping_bag` to Cold Sweat's "Sleep Check Override Blocks" (`world.toml`).
- Leave "Check Sleeping Conditions" active: one does not sleep in deep winter without a fire. It is the right link.

**Clearings**:
- The Vigil Fire serves as a heat and cooking source (see 2.1).
- The biome `tilth:clairiere` receives the season through the bridge (2.2), like the other biomes.
- Its sheep and rabbits feed on F&C crops (2.6).
- To avoid a temperature jump when entering a clearing, give the biome, in Cold Sweat, the same temperature as the plains (`addBiomeTemperature`), or a slight bonus ("life returns").

### 2.9 Is KubeJS enough?

| Link | Do KubeJS or data suffice? |
|---|---|
| Heat sources (KC, F&C, Vigil Fire), hot and cold dishes, icebox | **Yes** (`ColdSweatEvents.registries`, tags) |
| Season → Cold Sweat → FIAHI and TWR | **Yes**, with `addTempModifier` + `gatherDefaultModifiers`. Risk: a modifier defined in a server script does not exist client-side (see 3.1) |
| F&C season tags, seasonal breeding, animal food | **Yes** (tags, datapack) |
| Duplicates, ported recipes, wild plant loot | **Yes** (KubeJS recipes, LootJS, KC schemas) |
| Hiding in EMI | Probably (KubeJS); otherwise resource pack |
| **FIAHI in Traveler's Backpack** | **No.** The backpack is neither a `Container` nor in the `Inventory`. A mixin or a Java tick applying `IFrozenRottenFood.tick` to the `BackpackWrapper` is needed, with an insulation coefficient |
| **Drinking tank water with TWR** | **No.** A Java `EffectFluid` is needed for Traveler's Backpack water that calls `ThirstHelper` and takes purity into account |

**Proposal: a small custom mod `tilth-liens`** (NeoForge 21.1, Java) that would do three things:
1. Age the food in the backpack (worn and placed) according to the temperature of the wearer or the place, at a reduced speed: the backpack insulates a little, the FIAHI provisions bag remains better.
2. Make the backpack hose quench thirst according to TWR, with the purity of the tank water.
3. Optionally, port the Cold Sweat seasonal modifier to Java, if the KubeJS version causes a client-side or performance problem.

Without this mod, the backpack becomes a **perfect refrigerator**, and its tanks only serve to carry water. This can be accepted, but it must be decided.

---

## 3. Conflicts and risks

1. **Seasonal bridge client-side**:
   - `ColdSweatEvents.registries` and `gatherDefaultModifiers` are **server** events [jar `EventGroup.server`].
   - If Cold Sweat computes or deserialises modifiers client-side (thermometer display), an identifier `tilth:saison` unknown to the client can cause errors or a wrong display.
   - To test in multiplayer; fallback: custom mod (2.9).
2. **KC-Automation and Kaleidoscope Compat both declare a Create arm interaction point** for the wok, the stockpot, the steamer and the spit [jar, neither knows the other].
   - Proposal: set `create.arm.pot_enabled`, `stockpot_enabled`, `steamer_enabled` and `shawarma_spit_enabled` to `false` in Compat.
   - Keep the millstone and the teapot in Compat, which KC-Automation does not handle.
   - Keep KC-Automation (Create packages, table, basket).
3. **Compat's UNITE mode**: it breaks F&C's bacon, chicken, pasta and onion sack, and removes wild onions (1.3). Stay in COMPAT.
4. **Condition `forge:false`** of Compat's datapack: under NeoForge 21.1, it is either accepted or an error. In both cases, the crafting of the F&C stockpot disappears, since the file replaces the original. To check once in game (log).
5. **Water freezing**: Cold Sweat ("Custom Freezing Behavior" in 2.4.3.1, reworked in 2.4.4) and Ecliptic (`IceAndSnow`, `WaterFreezesInFrozenBiomes`) both handle water freezing. Choose **Ecliptic** for seasonal ice, and turn off Cold Sweat's behaviour.
6. **KC stove without fuel**: free and infinite heat. Hence the weak effect proposed in 2.1. Otherwise, it replaces Cold Sweat's hearth.
7. **Versions**:
   - KC 1.6.0 changes the millstone (which Compat modifies by mixin, `MillstoneBlockEntityMixin`) and the chopping board;
   - F&C 1.1.27 and later adds duplicates;
   - Ecliptic is in beta.
   - Move **KC to 1.6.0 and Ecliptic to 0.15.4.1** on a branch, but **keep F&C at 1.1.25** as long as the board and choppers are not dealt with.
8. **FIAHI and performance**: FIAHI walks through the block entities of all loaded chunks at every world tick (`onLevelTick`).
   - Set `TEMPERATURE_CHECKER_INTERVAL` high enough.
   - Measure with spark on a base full of chests.
   - With `simulation-distance=6`, the load remains bounded [deduced].
9. **FIAHI and Create**: food on belts and in depots (non-`Container`) is not tracked. Not serious.
10. **Ecliptic's local rain**: it puts out open-air KC stoves and stops tray drying. This is intended, but to be explained to the player (KC Ponder, codex).
11. **KC content out of tone**:
    - KC village houses;
    - "Nether" and "End" dishes (`blaze_lamb_chop`, `end_style_sashimi`…);
    - furniture duplicated with Handcrafted.
    - To hide or keep rare: a decision of tone, not technical.

---

## 4. To adapt, remove or add in the plan

| Where | Change | Reason |
|---|---|---|
| 04 §4.7 and draft `cuisine_unifiee.js` | **Do not** remove the F&C stove. Remove the roaster by porting its recipes to the KC wok. Remove the **KC** scarecrow. Replace the `sereneseasons:*` item tags with `eclipticseasons:crops/*` tags on the **blocks** | KC has no oven. The F&C tags are broken (wrong folder). Ecliptic judges blocks |
| 04 §4.7, Kaleidoscope Compat row | `datapack_mode = COMPAT` (explicit), `cooking_pot_recipes_disabled = true`, `farm_and_charm_compat_enabled = true`, `thirst_compat_enabled = true`, `create.arm.{pot,stockpot,steamer,shawarma_spit}_enabled = false` | What each setting really does (1.3); conflict with KC-Automation |
| 04 §4.3 and 10.5 "Kaleidoscope Compat merges duplicates" | Correct it. It makes duplicate items interchangeable; it only removes them in UNITE mode, unusable here. Removing duplicates remains to be done in KubeJS and LootJS (2.7) | Accuracy |
| 04 §4.6 and draft `saisons_cold_sweat.js` | Rewrite with `addTempModifier` + `gatherDefaultModifiers` (2.2) | Wrong API in the draft; the right model also gives the season to FIAHI and TWR |
| 10.7, "Heat" row | Specify: KC and F&C stoves through Cold Sweat KubeJS (weak effect), Vigil Fire through tag, hot dishes through `addFoodTemperature` | They do not count by default |
| 10.7, "Seasons" row | "Freeze It and Heat It tags" does not exist. FIAHI is adjusted through its configuration (lists of identifiers) and receives the season through the Cold Sweat bridge | FIAHI has no season tags |
| 10.7, "Eating and drinking" row | KC soups and teas: nothing to do (Compat). F&C teas and soups: TWR keywords | Already automatic |
| 10.5, Traveler's Backpack row | Add the settings: furnace, smoker and feeding upgrades off; sleeping bags in Cold Sweat's sleep list | Cooking while travelling through the campfire |
| 10.10, scripts | Add `chaleur_cold_sweat.js` (heat sources, dishes, icebox), `faune_nourriture.js` (husbandry tags), `saisons_cultures.js` (Ecliptic F&C tags and seasonal breeding); `garde_manger.js` stays removed | The links from 2.1 to 2.6 |
| New | **Custom mod `tilth-liens`** (2.9), if the backpack is not to be a refrigerator and its tanks are not to be useless for drinking | Impossible in KubeJS |
| Versions | KC → 1.6.0, Ecliptic → 0.15.4.1 on a test branch. F&C stays at 1.1.25. Cold Sweat 2.4.3.1 until 2.4.4 is tested | 1.3, 3.7 |
| Zoo (10.4) | Husbandry and seasonal breeding tags (2.6); KC millstone pulled by animals | Links the zoo to the farm |

---

## 5. To settle with an in-game test

1. Does the `tilth:saison` modifier defined in KubeJS display correctly client-side (Cold Sweat thermometer), in single player then on a dedicated server?
2. Does this modifier reach the temperature of a chest: does food rot faster in summer than in winter in a FIAHI chest?
3. With both mods active, which Create arm wins on the wok: KC-Automation or Compat? And after turning off Compat's arms?
4. Does the log show that `forge:false` is accepted? Has the F&C stockpot disappeared from the crafting table?
5. Do the F&C recipes replayed in the KC stockpot appear in EMI?
6. Does Kaleidoscope Compat 2.9.7-Patch.2 run with KC 1.6.0 (millstone mixin)?
7. With the `eclipticseasons:crops/*` tags placed on the F&C blocks, are Ecliptic's tooltip and the actual growth seasonal?
8. KC rice in the clearing: which climate and humidity does Ecliptic assign to the biome `tilth:clairiere`?
9. Does food age in Traveler's Backpack (worn and placed)? Expected: no.
10. Does the Cold Sweat waterskin quench thirst with TWR? The backpack hose does not, to confirm.
11. Does KC's "Preservation" effect cancel the `fiahi:shiver` penalty?
12. Does the straw hat make KC seeds drop from No Man's Land grass (vanilla `short_grass` table, or NML's own grass)?
13. FIAHI in a base of 200 chests: cost in tick time (spark), depending on `TEMPERATURE_CHECKER_INTERVAL`.
14. Water freezing: which mod wins between Cold Sweat and Ecliptic, and are there ice / water flip-flops?
