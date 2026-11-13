# Research — "Daily life" pillar & environmental survival (Tilth, 1.21.1 NeoForge)

> Report adjusted to the up-to-date registry (46 mods, decisions 0002–0007): Farm & Charm, Ecliptic Seasons, KC-Automation **already retained**; Animal Husbandry, Spoiled, Let's Do Bakery/Meadow, Graze **rejected** (not proposed again).
> Priorities handled first: **demanding animal husbandry** (replacement for Animal Husbandry) and **preservation** (replacement for Spoiled).

## 0. Method and reliability of the evidence

- modrinth.com / api.modrinth.com / curseforge / moddex / modpackindex / mado-hosting: **unreachable** from the research environment (proxy 403 / DNS). The shared web-search quota (200) was **exhausted during the research**.
- Modrinth availability verified through two indirect but reliable sources:
  1. **Mirror of the Modrinth API** `qacow37/prismnix` on GitHub (`packages/projects/<id>.nix`: slug → Modrinth id → `neoforge-1.21.1` keys + .jar name). Snapshot **somewhat dated** (e.g. KC is at 1.4.1 there while the pack has 1.5.1): an absence may mean "released more recently", a presence is proof.
  2. **packwiz** files of other modpacks on GitHub pointing to `cdn.modrinth.com/data/<id>/…neoforge…1.21.1…`.
- Descriptions read through the Modrinth content mirror `Voxelum/xmcl-community-content-i18n-extra` (translations of the Modrinth page) and through the GitHub source code when it exists (KC, Tavern, Ecliptic, Cold Sweat, TWR, F&C, Kaleidoscope Compat, Nutritional Balance, Nourished, First Aid New).
- Notation: **MR✔** = 1.21.1 NeoForge present on Modrinth (id + file) · **MR?** = not verified · **CF only** = CurseForge only (blocking).

---

## 1. What the already retained mods ALREADY do (not to be bought again elsewhere)

| Retained mod | Mechanics useful to the pillar (verified in the code) |
|---|---|
| **Kaleidoscope Cookery** (1.5.1 in the pack, 1.6.0 on GitHub) | Crops: tomato, lettuce, chilli, rice (+ wild rice), tea plant. Stations: stove, pan/wok, stockpot, steamer, millstone, chopping board, shawarma spit, teapot, **bamboo tray (sun drying / moistening in the rain)**, enamel basin, string of chillies and mushrooms (drying decoration). **Already provides the Serene Seasons, Ecliptic Seasons (humidity) and Diet tags** for its crops. Native KubeJS schemas: `bamboo_tray`, `chopping_board`, `millstone`, `pot`, `steamer`, `stockpot`. Native compat: Create, Farmer's Delight, Harvest with Ease, EveryComp, Jade, Ponder, Sable (entity tags). **No** preservation/cooler mechanic. |
| **Ecliptic Seasons** | 24 solar terms, **dynamic day length** (`DynamicDaylightDuration`), snow that accumulates/melts, weather per term, season + **humidity** crops, greenhouses, humidifier, "Season Core", agricultural prayer ritual, **seasonal breeding** (`EnableSeasonalBreed`, `EnableTimeBreed`), **seasonal bees** (`EnableSeasonalBee`, `BeePollinateSeasons`, `BeeActiveSeasons`), **seasonal fishing** (`EnableSeasonalFishing`, `FishingSeasons`, `LessFishInThunder`), `HeatStroke` (heatstroke, **duplicate of Cold Sweat → disable**), `RestrictBoneMeal`. Reads most **Serene Seasons tags**. Native KubeJS plugin: `EclipticSeasonsApi` bindings (getSeason, getSubSeason, getSolarTerm, isRainAt, getAdjustedHumidity, getSpecialDays…) and `EclipticSeasonsBindings`. |
| **Farm & Charm** | Fertiliser, fertilised soil/dirt, plough, seeder, pitchfork, scarecrow (growth), sprinkler, wooden well (`timber_well`, water table + rain), silos, cart, **feeding trough** (`feeding_trough`), **drinking trough**, nest and **chicken coop**, horse fodder, chicken grain, dog/cat food + bowl, barley/oat/corn/strawberry/onion crops (+ tomato, lettuce duplicated with KC), wild plants. ⚠ The feeding trough **puts animals in love automatically** (`ApproachFeedingTroughGoal` → `setInLove`) = autonomous breeding (contrary to function B). NeoForge config keys: `enableRainGrowthEffect`, `rainGrowthMultiplier`, `bigCropChance`, `enableBonemealEffect`, `fertilizedSoilRange`, `scarecrowGrowthInterval`, `scarecrowRange`, `waterSprinklerRange`, `wellGroundwaterDepth`, `wellRainFillChance`, `feedingTroughRange`, `enableChickenEffects`, `chickenCoopMaxChickens`, `chickenCoopMaxEggs`, `enableHorseTaming/Effects`, feeding times. |
| **Thirst Was Reclaimed** (modid `thirst`) | 4 purity levels (stagnant source = dirty, flowing water +1, altitude >100 / caves <48 = "slightly dirty"), illness (nausea + hunger, possible poison), campfire purification +1 / furnace +2, **Create sand filter** (+1 per pass), thirst modulated by biome temperature/humidity and **by Cold Sweat** if present, terracotta bowls, `item-settings.toml` / `keyword.toml` to add drinks. |
| **Cold Sweat 2.4** | Native compat with `thirst` (TWR), `spoiled`, `sereneseasons`, `supplementaries`, `sable`, `create_aeronautics`, Create. `coldsweat.*` KubeJS bindings: `getTemperatureAt(level,pos)`, `getBiomeTemperature`, `getBlockTemperature`, `createModifier`, `addModifier`, `getTemperature(entity,trait)`… (useful for the seasons bridge AND for a "cold cellar" preservation). **Boiler** and **Icebox** blocks. |

---

## 2. Summary table of candidates

| # | Mod | Modrinth slug / id | MR 1.21.1 NF | Domain | Verdict |
|---|---|---|---|---|---|
| 1 | **Salt's Animal Farm** | `salts-animal-farm` / X2m1QNPV | ✔ 0.2 | Husbandry (comfort, weight, disease) | **Core** (to test) |
| 2 | **Cattlestrophic!** | `cattlestrophic!` / xbg5Ic9n | ✔ 1.3.0 | Husbandry (hunger, ageing, wild/domestic) | **Core** (to test) |
| 3 | **Animal Genders** | `animal-genders` / qONca06E | ✔ 1.2.0 | Husbandry (sexes) | **Recommended** |
| 4 | Animal Weights | `animal-weight` / L8hKhAzB | ✔ 1.1.0 | Husbandry (weight) | Fallback for #1 |
| 5 | Butchery | `butchery` / 6YfOmC9t | ✔ 5.2 | Animal products (carcass, hides) | Optional |
| 6 | Aquatic Breeding | `aquatic-breeding` / g326EYcN | ✔ 1.0.2 | Fish farming, tropical fish genetics | Optional |
| 7 | Backported Animal Variants | `backported-animal-variants` / pyFjCT3F | ✔ 0.2 | Biome variants (visual genetics) | Optional |
| 8 | Animania Reborn | — | **CF only** | Full husbandry | Rejected (Modrinth) — to watch |
| 9 | [BUB] Gender | — | MR? (absent from the mirror) | Sexes + sterility | Rejected unless on Modrinth |
| 10 | Barnyard Buddies / Butchercraft | 3lBMOcwT / MT2Xm0ih | ✔ | Breeds (content) / butchery (depends on Extra Delight) | Rejected |
| 11 | **Freeze It and Heat It (FIAHI)** | `freeze-it-and-heat-it` / 6jlFKPwR | ✔ 4.1.0 | Preservation (Cold Sweat addon) | **Core** (conditional) |
| 12 | **KubeJS "Pantry" system** | — | — | Preservation (expiry, cellar, drying) | **Core** (script) |
| 13 | ShelfLife / SpoiledZ / Food Spoiling | — | ✘ | Expiry | Rejected (not on Modrinth / not 1.21.1) |
| 14 | **Kaleidoscope Compat** | `kaleidoscope-compat` / Xm1eP54z | ✔ 2.9.7 | KC ↔ F&C/Vinery/Tavern merge | **Core** |
| 15 | **Kaleidoscope Tavern** | `kaleidoscopetavern` / r9RZvhiJ | ✔ 1.2.0 | Vine, brewery, cellar, ageing | **Recommended** (replaces Vinery) |
| 16 | Kaleidoscope Contraption | `kaleidoscope-contraption` / JCCAvv9N | ✔ 1.0.3 | KC cooking on Create contraptions | Optional (expeditions) |
| 17 | Kaleidoscope Orchard | `kaleidoscope-orchard` / UQJIT5UV | ✔ 0.0.2 | Fruit trees (water + soil at the roots) | Optional / to watch |
| 18 | Kaleidoscope Grilling | `kaleidoscope-grilling` / LPZkep5R | ✔ 1.1.1 | Oil, grilling, new crops | Optional (later) |
| 19 | Let's Do Vinery | `lets-do-vinery` / 1DWmBJVA | ✔ 1.5.3 | Wine | Rejected (Tavern duplicate) |
| 20 | **Tide** (+ Tide Extra Compatibility) | `tide` / die1AF7i (+ goMfyYSL) | ✔ 2.1.1 (+2.3.0) | Fishing | **Recommended** |
| 21 | **River Fishing** | `river-fishing` / 5acJO3EU | ✔ 0.9.0 | "Simulator" freshwater fishing | **To compare with Tide** |
| 22 | Fisherman's Trap | `fishermens-trap` / ltRggnDx | ✔ 3.1.0 | Baited trap | Optional |
| 23 | **Thirst Canteen** | `thirst-canteen` / GV0PyXSv | ✔ 3.7 | TWR canteens | **Recommended** |
| 24 | Create Wells / Just a Well | PNcaol17 / qs7zlvyh | ✔ | Wells | Rejected (F&C duplicate, Create stacking) |
| 25 | **Nutritional Balance** | `nutritional-balance` / OECzAr94 | ✔ 1.21.1-7.0.3 | Nutrition by nutrients | **Recommended** |
| 26 | Nourished (+ MariesLib) | `nourished` / gXy6YNys | ✔ 0.2.7-beta | 6-group nutrition + "gut health" | Optional (alternative to #25) |
| 27 | Spice of Life: Onion / Diet | eHGYGKJz / — | ✔ / ✘ (Diet blocked on 1.20.1) | Variety | Rejected |
| 28 | AppleSkin | `appleskin` / EsAfCjCV | ✔ 3.0.9 | Tooltips (TWR uses it) | Recommended (QoL) |
| 29 | **Comforts** | `comforts` / SaCpeal4 | ✔ 9.0.5 | Sleeping bags, hammocks | **Recommended** |
| 30 | Sleep Quality | `sleepquality` / S8fvNvSw | ✔ 2.0.1 | Sleep quality (roof, light, weather) | Optional |
| 31 | Healthy Sleep | `healthy-sleep` / 3aMt7SZp | ✔ (v4) | Sleep score, healing at rest | Optional (alternative to #30) |
| 32 | Steve's Realistic Sleep / Campfire Resting | ZPywkPEo / k9O8NrbO | ✔ | Accelerated time in bed / resting at the fire | Optional (to test with Ecliptic) |
| 33 | Hourglass / Day Length | 1ZqmoFFP / MrMHdkQ6 | ✘ / ✔ | Day length | Rejected (Ecliptic does it) |
| 34 | **Hardcore Revival** | `hardcore-revival` / HqKoXaXz | ✔ 21.1.x | Downed state / co-op revival | **Recommended** |
| 35 | First Aid New | `first-aid-new` / yNbhkSj2 | ✔ 1.3.1 | Localised health | Optional (heavy config) |
| 36 | Complicated Bees | `complicated-bees` / uhqEKIQS | ✔ 3.6.1 | Diploid genetic beekeeping | Optional (big piece) |
| 37 | Realistic Bees / Buzzier Bees / Productive Bees | tG6HkcWx / b7vOFSIp / jH6iiqkd | ✔ | Bees | Optional / rejected / rejected |
| 38 | Seasonal Let's Do / Seasonal Integration | yFIPn94r / w3gipv9f | ✔ | F&C season tags | Inspiration (copy the tags) |
| 39 | Serene Seasons (fallback) | `serene-seasons` / e0bNACJD | ✔ 10.1.0.x | Seasons | Fallback (decision 0002) |

---

## 3. Detailed sheets

### 3.1 DEMANDING ANIMAL HUSBANDRY (priority 1) — replace Animal Husbandry with a **combination of three targeted small mods**

No single reliable mod on Modrinth 1.21.1 NeoForge covers needs + diseases + genetics (Animal Husbandry 0.4.x rejected; Animania Reborn = CurseForge only, 26 commits, 0 stars; Genetic Animals / Realistic Horse Genetics / Animal Genetics = no 1.21.1). Proposal: **split the function** into independent layers, each replaceable.

#### Salt's Animal Farm — `salts-animal-farm` (X2m1QNPV) — MR✔ `salts_animal_farm-1.21.1-NeoForge-0.2.jar`
- Evidence: Modrinth mirror (key `neoforge-1.21.1`); Modrinth description (xmcl mirror). Side: both (server logic). Dependencies: none declared.
- **Mechanic** (no item added): cows, mooshrooms, pigs, sheep, chickens and rabbits seek, day and night, **shade/sun, nearby water, space without overcrowding, fellow animals, light at night, soft bedding, a view of the starry sky**. Hidden **weight**: comfort → weight gain → more loot; stress/neglect → loss → **illness** (slowness, no more breeding, no more production) until cured. **Rain**: animals seek shelter, staying in the rain makes them lose weight → the barn really matters. **Fear/panic** at predators, blows, the sight of a slaughter (high difficulty). Easy/Normal/Hard presets; configurable: animals concerned, scary mobs, "soft bedding" blocks, behaviour in the rain, delays, weight bounds. Weight readable by right-clicking with an empty hand.
- Integration: turns **farm architecture** into gameplay (F&C barn `stable_floor`/`straw_stable_floor`, drinking trough, KC straw `StrawBlocks`); Ecliptic's seasonal rain makes shelter vital in autumn; Naturalist/Alex's Mobs predators = "scary mobs" (vanilla hostiles are removed, the **list must be rewritten**).
- Conflicts: overlaps with Animal Weights (keep only one of the two). Young (0.2) → long server test (performance of comfort scans).
- KubeJS/datapack: "soft" block tag = KC straw + F&C barn floors + `wheat_pile`; loot tables multiplied by weight → check the interaction with Butchery (carcasses).
- **Verdict: core (to test)** — it is exactly "turning husbandry into a craft" without adding content.

#### Cattlestrophic! — `cattlestrophic!` (xbg5Ic9n) — MR✔ `cattlestrophic-1.3.0.jar` (MIT licence)
- Mechanic: **hunger and ageing**. Animals born from an egg/spawn = **wild** (age, do not starve, less loot, cannot be leashed, breeding less certain and slower); animals born from breeding = **domestic** (live longer, more loot, **starve if not fed**). Feeding by hand ("animal food") or **feeding trough** (radius, quantity, max number of animals). Particles when age >75/90% or hunger <20%. Inspection tool (creative).
- Keys: `enableMod`, `enableAnimalSettings`, `hungerDepletionRate`, `agingRate`, `showHungerIndicators`, `showAgeIndicators`, `feedingTroughRange`, `feedingAmount`, `maxAnimalsPerTrough`, `breedingCooldownTicks`, `growthTimeTicks`, `wildMaxAge`, `domesticatedMaxAge`, `preventLeashingWildAnimals`, `wildAnimalBreedChance`, `wildBreedingExtraCooldownTicks`, `wildAnimalLootPenaltyMultiplier`, `domesticatedAnimalLootBonusMultiplier`; per animal: `config/cattlestrophic_animal_settings/<modid>/<entity>.json` (`acceptedFoodItems`, `hungerDepletionRateOverride`, `wild/domesticatedMaxAgeOverride`, `breedingCooldownTicksOverride`, `growthTimeTicksOverride`, `feedingAmountOverride`). Third-party tool: JSON generator (GitHub Qdeeme/Cattlestrophic_json_creator).
- Integration: **farming → fodder → husbandry** loop (`acceptedFoodItems` = F&C barley/oat, `horse_fodder`, `chicken_feed`, straw). JSON files for the ducks/geese of Untitled Duck Mod and the Naturalist animals we want to breed.
- Conflicts: the F&C feeding trough (auto breeding) — see §5. `preventLeashingWildAnimals=true` forces real domestication (capture/lure two wild animals, make them breed).
- **Verdict: core (to test)** — brings the "you have to feed" constraint that Salt's was missing.

#### Animal Genders — `animal-genders` (qONca06E) — MR✔ `animalgenders-1.21.1-neoforge-1.2.0.jar` — GitHub acorsicanfrog/animalgenders
- Mechanic: visible male/female sex; **breeding = male + female pair**; **only females lay / are milked** (chickens, cows, goats). Applies to any subclass of `Animal` (hence to modded animals). Config: entity list, male/female ratio at natural spawn. Licence: credit requested.
- Integration: makes herd management real (keeping a male, selecting productive females), readable with Jade.
- Conflicts: F&C chicken coop (chickens stored in the block?) → check in the .jar that the coop's laying respects sex; Adorable Hamster Pets has its own genetics (exclude it from the list).
- **Verdict: recommended.** ([BUB] Gender is richer — sterility, item restrictions — but absent from the Modrinth mirror: only to be used if confirmed on Modrinth.)

#### Animal Weights — `animal-weight` (L8hKhAzB) — MR✔ 1.1.0
- Weight 1–8 according to 4 criteria (light ≥14, water within 4 blocks, grass/moss, ≤7 fellow animals in 5×5); weight 0 = sick (green, no more breeding); **loot and XP multiplied by weight** (too generous by default). Command `/animalweights`.
- **Verdict: fallback** if Salt's Animal Farm proves unstable (simpler, less rich, no rain/fear).

#### Butchery — `butchery` (6YfOmC9t) — MR✔ `butchery-5.2-neoforge-1.21.1.jar` (ARR)
- Mechanic: killing with a butcher's knife → **carcass**; hook (bleeding), **hide drying rack (wet + salt → leather)**, butcher's table (cutting into pieces, vanilla or configurable Farmer's Delight output), campfire grill, grinder. Separate Alex's Mobs addon.
- Integration: "butcher's craft", rare and worked leather (consistent with Overgeared), meat for KC.
- Conflicts: adds meat cuts → **set the output to vanilla** (not FD) and tag `c:foods/raw_meat` for KC; salt source to be checked.
- **Verdict: optional** (good mechanic, but extra content to align with KC).

#### Aquatic Breeding — `aquatic-breeding` (g326EYcN) — MR✔ `fisheggs-1.0.2+neoforge.jar`
- Fish attracted by **fish meal** → eggs → 1–3 fry; **tropical fish inherit colours/patterns with mutations (3072 variants)**; squids hunt live prey and breed; edible tentacles.
- **Verdict: optional** — the only real "genetics" found for 1.21.1, applied to fish farming (colony ponds).

#### Backported Animal Variants — `backported-animal-variants` (pyFjCT3F) — MR✔ 0.2
- 1.21.5 biome variants of cows/pigs/chickens (cold/temperate/warm). Inheritance from parents: **not verified**. **Verdict: optional** (light "visual" genetics).

#### Rejected in husbandry
- **Animania Reborn** (GitHub Steveaaaaa/Animania-Reborn, NeoForge 1.21.1, breeds, needs, mood, cheese, vehicles): **CurseForge only**, very young → rejected, to watch.
- **Butchercraft**: recipes via Extra Delight (Farmer's Delight family). **Barnyard Buddies**: breeds = content. **Hungry Cows**: Fabric only. **BreedingControl**: redundant with KubeJS. **Animal Feeding Trough**: autonomous breeding.

### 3.2 PRESERVATION (priority 2) — replace Spoiled

State of the 1.21.1 NeoForge market: **Spoiled** (rejected); **ShelfLife** (xiaofeng96301/ShelfLife — excellent event-driven design driven by datapack, cooler, temperature/humidity) but **GitHub only, 9 commits, not on Modrinth**; **SpoiledZ**, **Food Spoiling**, **Spoilage Enhanced**: no 1.21.1 NeoForge. Proposal in two tiers.

#### Freeze It and Heat It (FIAHI) — `freeze-it-and-heat-it` (6jlFKPwR) — MR✔ `fiahi-4.1.0.jar` (keys `neoforge-1.21` and `neoforge-1.21.1`, GPL-3.0) — dependency: **Cold Sweat**
- Mechanic: food (inventory **and containers**) **freezes** in the cold or **spoils** in the heat; it is brought back to normal with Cold Sweat's **boiler** or **icebox**, otherwise eating an altered food gives negative effects. **Provisions bag** that slows freezing and spoiling. Config: enable/disable freezing and spoiling, speeds, **whitelist** of excluded foods. Made for the modpack *The Winter Frontier*. Active development (~25 versions).
- Integration: links preservation, **Cold Sweat temperature** and **Ecliptic seasons** (summer spoils, winter freezes expedition rations); the icebox becomes the colony's "fridge".
- Conflicts/limits: not time-based expiry (it is a reversible thermal state); exact compatibility with Cold Sweat 2.4.3.1 **to be verified** in `neoforge.mods.toml`.
- KubeJS: whitelist = "preserved" foods (dried, salted, jars, Tavern bottles) → rewards processing.
- **Verdict: conditional core** (if it loads with Cold Sweat 2.4; otherwise go to the script alone).

#### KubeJS "Pantry" system (slow expiry, cellar, drying) — **core (home-made script)**
Inspired by the ShelfLife architecture (avoids the cost of one tick per item):
1. **Tags**: `tilth:perishable/fast` (raw meat and fish, milk), `…/medium` (fruit, vegetables, KC dishes), `…/slow` (bread, cheese); `tilth:preserved` (dried on the KC bamboo tray, salted, smoked, jars, Tavern bottles).
2. **Timestamp per game day**: `custom_data.tilth_day = floor(gameTime/24000)` set at crafting, pickup and container exit. **Rounded to the day** so stacks from the same day merge (the classic flaw of these mods).
3. **Calculation only on events**: container opening, pickup, and player inventory every ~30 s. A chest never opened does not rot (accepted trade-off, like ShelfLife).
4. **Location factor**: `coldsweat.getTemperatureAt(level, pos)` of the container → **cold cellar** (underground) ×0.3; **Cold Sweat icebox** ×0.1; in the sun in summer ×1.5 (`EclipticSeasonsApi.getSeason`).
5. **Result**: the expired item becomes `minecraft:rotten_flesh`, `farm_and_charm:rotten_tomato` or compost (F&C `compost`) → it will loop back to F&C fertilisers.
6. Client **tooltip**: "fresh / to eat soon / spoiled", without a precise countdown (sobriety of the art direction).
7. **Processing = reward**: KubeJS recipes for the **KC bamboo tray** (schema `result` / `ingredient` / `subtype: "drying"` / `duration`) → dried meat, dried fish, dried fruit. Drying requires an open sky and no rain (KC code): it becomes **seasonal** with Ecliptic's weather.
- Risks: performance (to profile with spark), Create (belts/depots), auto-storage. **Start with expedition rations only** (provisions bag, Supplementaries lunch box) before extending to all food.

### 3.3 Farming & Kaleidoscope ecosystem

#### Kaleidoscope Compat — `kaleidoscope-compat` (Xm1eP54z) — MR✔ `kaleidoscope_compat-2.9.7-neoforge+mc1.21.1.jar` — GitHub SShakusora/Kaleidoscope-Compat (branch 1.21.1-NeoForge, 120 commits, 2.10.0 in progress)
- Mechanic: **unifies the KC items that overlap with those of other mods** (`c:` tags), and compat with Farmer's Delight, **[Let's Do] Farm & Charm**, Bakeries, Overweight Farming, Drop the Meat, Vinery, Tavern, Crock Pot… For F&C: the **F&C pot recipes are replayed in the KC stockpot** (`afterStockpotRecipeMatch`); options `farm_and_charm_compat_enabled` and **`cooking_pot_recipes_disabled`** (disables all F&C pot recipes). The straw hat makes Vinery seeds drop. Also adds `sable` (entities) and `create` (blocks) tags.
- **It is the "ready-made" execution of the condition of decision 0002** (KC as the only cooking system, merged ingredients).
- **Verdict: core.** The exact list of merged items (tomato, lettuce, flour, dough) remains to be verified in the .jar and completed in KubeJS.

#### Kaleidoscope Tavern (official, same team as KC) — `kaleidoscopetavern` (r9RZvhiJ) — MR✔ `kaleidoscopetavern-1.2.0-neoforge+mc1.21.1.jar` — GitHub KaleidoscopeMods/KaleidoscopeTavern (branch 1.21.1)
- Mechanic: **trellis vine** (and wild vine to find in the world), **pressing vat**, **barrels** (multi-step brewing, **7 quality levels**), **cellar cabinet**, tap, bar, shaker/cocktails (1.2.0), glassware. Already provides the `eclipticseasons`, `sereneseasons`, `diet` and `c` tags.
- Integration: gives a **long-term specialisation** (cellar, ageing, quality) that fits the "we perfect" pillar; drinks → TWR (hydration via `item-settings.toml`).
- To disable: **Molotov cocktail** (`MolotovBlock`: incendiary weapon, off-tone).
- **Verdict: recommended** in place of Vinery (vine/wine duplicate). The World Liquor (real brands) and Bloodwine addons are rejected.

#### Kaleidoscope Contraption — `kaleidoscope-contraption` (JCCAvv9N) — MR✔ 1.0.3
- KC stations (wok, stockpot, spit, steamer, stove) **work on Create contraptions** (trains, lifts): kitchen car, airship kitchen (to check: Aeronautics goes through Sable sub-levels, not Create contraptions, whereas KC already provides Sable tags).
- **Verdict: optional** ("expedition" pillar, to test with Steam 'n' Rails).

#### Kaleidoscope Orchard — `kaleidoscope-orchard` (UQJIT5UV) — MR✔ 0.0.2 (MIT, unofficial author)
- Fruit trees whose planting requires **water and good soil around the roots**; strawberry, orange, kiwi, blueberry; fruit pies and wines. Version 0.0.x. **Verdict: optional / to watch** (good mechanic, zero maturity; risk of duplicating the F&C strawberry).

#### Kaleidoscope Grilling — `kaleidoscope-grilling` (LPZkep5R) — MR✔ 1.1.1 (BSD-3)
- Rapeseed → **oil pressing**, spiced oils, grilling (hand-composed skewers, "signature" skewers), new crops (onion = F&C duplicate, sweet potato, houttuynia, Sichuan pepper tree). **Verdict: optional later** (mostly content; oil could serve as a processing chain).

#### Seasonal Let's Do (yFIPn94r, MR✔ 1.5.0+1.21) / Seasonal Integration (w3gipv9f, datapack)
- Serene Seasons tags for Let's Do crops. Ecliptic reads these tags, but Seasonal Let's Do may require the Serene Seasons mod (to verify). Seasonal Integration points to the *Serene Seasons API Stub* (**CurseForge only**). **Verdict: take inspiration, copy the tags into the KubeJS datapack** (zero dependencies).

#### Rejected in farming
Let's Do Vinery (Tavern duplicate); Hearth & Harvest (depends on Farmer's Delight); Crops Love Rain (F&C already has `enableRainGrowthEffect`); Fruitful Fun / AgriCraft / Dynamic Trees (no 1.21.1 NeoForge in the mirror); Snow! Real Magic (Modrinth OK but duplicate of Ecliptic's snow); KubeJS Kaleidoscope (KC already has its native KubeJS schemas); Ponder for KC (integrated into KC since 1.2.1); Kaleidoscope Doll/Doll Workshop/End/Nether/Twilight/Chinese Food (content).

### 3.4 Water

#### Thirst Canteen — `thirst-canteen` (GV0PyXSv) — MR✔ `ThirstCanteen-1.21.1-3.7.jar`
- Canteens (several tiers) for the Thirst family; the page announces its transition to TWR. TWR keeps the modid `thirst` → the dependency should be satisfied, **version range to be verified** in the .jar.
- Integration: water becomes an **expedition logistics** (purify before leaving). Cold Sweat's canteen (hot or cold water for temperature) keeps a different role.
- **Verdict: recommended** (answers the registry's "to test" question).

Rejected: Create Wells (Create well by rotation: duplicate of the F&C well, stacking of Create addons), Just a Well (duplicate), Water Flasks (intended for TFC), Healthy Water (gadget).
Consistency ideas (KubeJS): the **F&C well** water comes out "acceptable" (filtered water table), harvested rainwater "purified", and the **TWR sand filter** acts as a treatment plant in the Create era.

### 3.5 Fishing

#### Tide — `tide` (die1AF7i) — MR✔ `tide-neoforge-1.21.1-2.1.1.jar` (+ Tide Extra Compatibility goMfyYSL, datapack 2.3.0)
- Many species by biome, depth, time and weather, baits, upgradable rods, **fishing journal** (collection), mini-game. The Extra Compatibility datapack makes the fish of **Critters and Companions (koi)** and **Alex's Mobs** (already in the pack) catchable.
- Conflicts: Hybrid Aquatic has its own fauna; check fish duplicates and the `c:foods/raw_fish` tags used by KC. Ecliptic `EnableSeasonalFishing` modifies vanilla fishing and may stack with Tide → to be tuned.
- **Verdict: recommended** (covers freshwater and sea, fits the pack's fauna).

#### River Fishing — `river-fishing` (5acJO3EU) — MR✔ `riverfishing-neoforge-0.9.0+1.21.1.jar` (depends on Architectury, already present)
- A real **craft**: **assembled** rod (tip, reel, real-diameter line, rigging, numbered hooks), natural baits versus lures, **groundbaiting**, fight with risk of breakage, 30 species, **living population (overfishing, repopulation in spring)**, ice fishing (auger + mormyshka), bait breeding (worms, maggots), aquarium, trophies, filleting. Freshwater only. Seasonality via Serene Seasons (inactive with Ecliptic without a bridge).
- Risks: 0.9.0; adds an NPC "fisherman-settler" (to be weighed against the "survivors" concept of decision 0004).
- **Verdict: to compare with Tide on a dedicated branch**: deeper and more original, less mature. Do not keep both (fish duplicates).

#### Fisherman's Trap — `fishermens-trap` (ltRggnDx) — MR✔ 3.1.0
- Submerged trap in an aquatic biome, bait depending on the species (bread = universal): passive but conditional fishing. **Optional** (good transition towards soft automation).

Rejected: Aquaculture 2 (content, neptunium), Fish of Thieves, Stardew Fishing, Fishing Real, Fishing Overhaul (duplicates or gadgets).

### 3.6 Beekeeping
- **Ecliptic** already handles the seasonal activity of bees (`EnableSeasonalBee`, pollination and outing seasons) → **core by configuration**.
- **Complicated Bees** (`complicated-bees`, uhqEKIQS, MR✔ `complicated_bees-1.21.1-3.6.1.jar`): Forestry-style **diploid** genetics, crossbreeding and mutations, wild hives, tools, entirely driven by datapack. By default, it also produces resources (ores, diamonds, netherite), which breaks Overgeared → **remove these species by datapack** and keep honey, wax, pollen, propolis. **Optional (big piece)**: it is the real "breeding genetics" available.
- Realistic Bees (Serilum, needs Collective): smaller and more numerous bees, purely visual → optional. Buzzier Bees (content, Blueprint) and Productive Bees (resources) → rejected.

### 3.7 Nutrition
- **Nutritional Balance** (`nutritional-balance`, OECzAr94, MR✔ `nutritionalbalance-1.21.1-7.0.3.jar`, GPL-3, GitHub dannydjdk/Nutritional-Balance, default branch 1.21.1, 2021 project): nutrients defined by tags (datapack), targets, **malnutrition** and **excess** zones (bonus if everything is balanced, light penalties otherwise), "non-essential" sugar, vegetables never in excess, hot reload. **Walks through crafting ingredients** to deduce nutrients → **check that it follows KC recipes (pot, stockpot)**; otherwise tag the dishes in KubeJS. **Recommended**: rewards varied farming and husbandry.
- **Nourished** (`nourished`, gXy6YNys, MR✔ 0.2.7-beta, MIT, requires MariesLib + Cloth Config): 6 groups (cereals, vegetables, protein, fruit, sugars, dairy) Diet-style, anti-repetition memory, **gut health (eating raw = penalty)**, Cold Sweat/Ecliptic/Serene Seasons compat, KubeJS events (`NourishedEvents.nutrientChanged`). **Optional**, richer but beta alternative.
- Rejected: Diet (blocked on 1.20.1; the `diet` tags provided by KC are useless), Spice of Life: Onion (duplicate). **AppleSkin** recommended (TWR displays hydration via AppleSkin).

### 3.8 Health
- **Hardcore Revival** (`hardcore-revival`, HqKoXaXz, MR✔ 21.1.x): downed instead of dead, teammates revive → **recommended** for a world among friends far from the base.
- **First Aid New** (`first-aid-new`, yNbhkSj2, MR✔ `firstaid-1.3.1-hotfix…+neoforge1.21.1`, GitHub maoruiQa/FIrst-Aid-New, 1.3.3 on GitHub): HP per limb, injury penalties, dosed healing, loss of consciousness and revival, but also adrenaline, morphine and addiction, defibrillator (military tone). **Optional**, only if adrenaline and opioids are cut; it duplicates Hardcore Revival (choose one).
- Light illnesses: **already covered** without a dedicated mod (dirty water TWR, sick animals Salt's, hypothermia and heatstroke Cold Sweat, raw → gut with Nourished).

### 3.9 Sleep & time
- **Comforts** (`comforts`, SaCpeal4, MR✔ 9.0.5): sleeping bags (do not move the respawn point), hammocks (daytime nap) → **recommended** (expeditions, camps).
- **Sleep Quality** (S8fvNvSw, MR✔ 2.0.1): effects on waking depending on bed type, light, presence of a roof and weather → optional. **Healthy Sleep** (3aMt7SZp): sleep score, healing according to hours slept, "well rested", impossible to sleep without fatigue → optional (choose one of the two).
- **Steve's Realistic Sleep** (ZPywkPEo, MR✔ 1.12.1): sleep speeds up time instead of skipping it (crops and furnaces advance) → to test with Ecliptic's day length and `ClearAfterSleep`. **Campfire Resting** (k9O8NrbO, MR✔ 1.6.0): waiting at the campfire (with a chance of "backache") → optional.
- **Hourglass**: no 1.21.1 version in the Modrinth mirror (last: 1.20) → **rejected**; Ecliptic already handles long nights in winter (`DynamicDaylightDuration`). Day Length: duplicate of Ecliptic.

---

## 4. Proposal for a "coherent system" for the pillar

| Function | Who does what | What we cut |
|---|---|---|
| Calendar, climate | **Ecliptic** (terms, weather, snow, day length) → KubeJS bridge to **Cold Sweat** (already decided) | Ecliptic `HeatStroke=false` (Cold Sweat handles it) |
| Water | **TWR** (purity, Create filter) + **Thirst Canteen** (canteens) + **F&C well** | — |
| Fields | **KC** (cooking crops) + **F&C** (cereals, soil, fertiliser, scarecrow, sprinkler, silo) + **Ecliptic** seasonality/humidity + Ecliptic greenhouses | F&C: cooking and duplicates (via **Kaleidoscope Compat** `cooking_pot_recipes_disabled=true` + KubeJS); `enableRainGrowthEffect` to reduce or cut (Ecliptic's humidity is enough) |
| Specialisation | **Kaleidoscope Tavern** (vine, barrels, cellar, quality) | Vinery, Molotov |
| Husbandry | **Cattlestrophic** (feed, age, domesticate) + **Salt's Animal Farm** (comfort, shelter, weight, disease) + **Animal Genders** (pairs, productive females) + Ecliptic (seasonal breeding) + F&C infrastructure (barn, chicken coop, drinking trough) | Auto breeding of the **F&C feeding trough** (recipe removed, or trough = Cattlestrophic feeding only); Quark Feeding Trough (already cut) |
| Fishing | **Tide** (or **River Fishing** after comparison) + Fisherman's Trap (option) | Ecliptic `EnableSeasonalFishing` to tune so as not to stack with Tide |
| Bees | Ecliptic (seasons) [+ Complicated Bees without resource species, option] | — |
| Cooking | **KC** alone | F&C stove/cooking_pot/roaster/crafting_bowl + dishes |
| Preservation | **FIAHI** (thermal freezing/spoiling, Cold Sweat icebox/boiler) + **KubeJS Pantry script** (slow expiry, cellar, KC bamboo tray drying, Tavern barrels) | — |
| Nutrition | **Nutritional Balance** (or Nourished) + AppleSkin | — |
| Health, rest | **Hardcore Revival**, **Comforts** [+ Sleep Quality] | — |

**Targeted game loop**: safe water → seasonal fields → fodder → fed, sheltered herd with managed pairs → products (milk and eggs from females, meat, leather) → KC cooking → preservation (drying, salting, icebox, cellar) → expedition rations (canteen, provisions bag, sleeping bag) → exploration.

---

## 5. KubeJS / datapack integration ideas (first scripts)
1. **F&C seasonal tags**: `sereneseasons:{spring,summer,autumn,winter}_crops` (blocks and seeds) and `eclipticseasons:crops/<min>_<max>` (format found in KC: `average_moist`, `dry_average`, `humid_humid`, `moist_humid`) for barley, oat, corn, strawberry, onion and wild plants.
2. **KC ↔ F&C merge** (in addition to Kaleidoscope Compat): hide F&C tomato, lettuce, flour and dough; F&C wild plant loot table → KC seeds; remove the recipes of the F&C cooking stations.
3. **F&C feeding trough**: remove the recipe, or cancel `setInLove` via `NativeEvents` (`BabyEntitySpawnEvent` / `AnimalTameEvent`) out of season or if the animal is hungry (Cattlestrophic).
4. **Cattlestrophic**: `acceptedFoodItems` JSON = `farm_and_charm:barley/oat/corn/horse_fodder/chicken_feed`, `kaleidoscope_cookery:rice_panicle`; JSON for `untitledduckmod:*`.
5. **Salt's Animal Farm**: "scary mobs" list = Naturalist and Alex's Mobs predators; soft blocks = KC straw, F&C barn floors.
6. **TWR** `item-settings.toml`: KC teas and milk tea, Tavern drinks (alcohol = barely hydrating), KC soups.
7. **Drying**: `kaleidoscope_cookery:bamboo_tray` recipes (`subtype: "drying"`) for dried meat, dried fish, dried fruit; add these products to `tilth:preserved` and to the FIAHI whitelist.
8. **Nutritional Balance**: tag the KC and Tavern dishes if the recipe traversal does not cover them.
9. **Seasons → Cold Sweat bridge**: `EclipticSeasonsApi.getSubSeason(level)` → `coldsweat.createModifier/addModifier` (already planned).
10. **Pantry** (§3.2): use `coldsweat.getTemperatureAt` for the cellar effect.

---

## 6. Uncertainties to verify in the .jars
1. **FIAHI 4.1.0**: Cold Sweat version range in `neoforge.mods.toml` (2.4.3.1 accepted?), exact nature of the "spoiling" (reversible or not), config keys, containers taken into account (Create, KC, Supplementaries).
2. **Salt's Animal Farm 0.2**: config keys, modded animals supported, cost of comfort scans (spark), interaction with Cattlestrophic's weight.
3. **Cattlestrophic**: id of the feeding trough block, wild/domestic status of animals already in the world, support for Untitled Duck ducks.
4. **Animal Genders**: F&C chicken coop (does laying respect sex?), exclusion list (hamsters, companions).
5. **Kaleidoscope Compat**: exact list of merged F&C items; compatibility with KC 1.5.1 and 1.6.0.
6. **KC 1.5.1** (in the pack): does it already contain the Ecliptic, Serene Seasons and Diet tags seen in 1.6.0? Should we upgrade to 1.6.0?
7. **Kaleidoscope Tavern**: how the cellar and ageing work, disabling the Molotov, thirst compat.
8. **Tide / River Fishing**: `c:foods/raw_fish` tags read by KC, duplicates with Hybrid Aquatic, role of the River Fishing fisherman NPC.
9. **Thirst Canteen 3.7**: does the `thirst` dependency accept TWR 3.0.6?
10. **Nutritional Balance**: does it follow the KC recipe types? Tag path (`c:nutrient/…` or `forge:nutrient/…`)?
11. **Ecliptic**: effect of `ForceCompatMode` on untagged F&C crops; stacking of `EnableSeasonalBreed` with the F&C feeding trough; does Seasonal Let's Do require Serene Seasons?
12. **F&C well**: which TWR purity for the water it supplies? Does `enableRainGrowthEffect` stack with Ecliptic's humidity?
13. **Sleep**: are Comforts, Steve's Realistic Sleep and Ecliptic's `ClearAfterSleep` compatible?
14. **Butchery**: FD or vanilla output by default, source of salt.
15. **Complicated Bees**: removal of resource species by datapack; does Ecliptic apply to its bees (own entity)?
16. Dated Modrinth mirror: reconfirm the latest 1.21.1 version of each mod at the time of adding with packwiz.

**Blockers outside Modrinth identified**: Serene Seasons API Stub (Ecliptic Bridge), Ecliptic Seasons MultiMod Patch and Ecliptic Seasons Bundles (CurseForge only), Animania Reborn (CurseForge only), ShelfLife (GitHub only), [BUB] Gender (not verified).

**Out of domain, to pass on**: *Aeronautics: Winds and Weather* (`aeronautics-winds-and-weather`, MR✔ 1.3.2): real wind depending on terrain, altitude and weather, ship sails and windmills. Interesting for the Civilisation pillar.

---

## Sources
- KC: https://github.com/KaleidoscopeMods/KaleidoscopeCookery (branch `1.21.1-neoforge`: `build.gradle`, `compat/`, `src/generated/resources/data/{sereneseasons,eclipticseasons,diet}`, `BambooTrayBlockEntity`)
- Tavern: https://github.com/KaleidoscopeMods/KaleidoscopeTavern (branch 1.21.1) · Modrinth https://modrinth.com/mod/kaleidoscopetavern
- Ecliptic: https://github.com/TeamTeaMC/Ecliptic-Seasons (branch `neoforge-1.21.x`, `CommonConfig.java`, `compat/kubejs/KubeJSPluginImpl.java`, `api/EclipticSeasonsApi.java`) · https://modrinth.com/mod/ecliptic-seasons
- Cold Sweat: https://github.com/Momo-Softworks/Cold-Sweat (branch 1.21-FG, `CompatManager.java`, `compat/kubejs/KubeBindings.java`)
- TWR: https://github.com/mlus-asuka/Thirst-Was-Reclaimed (+ wiki: Water-Purity, Purification, Temperature, Drinks)
- Farm & Charm: https://github.com/Let-s-Do-Collection/FarmAndCharm (branch 1.21: `ObjectRegistry`, `FarmAndCharmNeoForgeConfig`, `ApproachFeedingTroughGoal`)
- Kaleidoscope Compat: https://github.com/SShakusora/Kaleidoscope-Compat · https://modrinth.com/mod/kaleidoscope-compat
- Nutritional Balance: https://github.com/dannydjdk/Nutritional-Balance · Nourished: https://github.com/kgbcupcake/nourished · First Aid New: https://github.com/maoruiQa/FIrst-Aid-New · Animania Reborn: https://github.com/Steveaaaaa/Animania-Reborn · ShelfLife: https://github.com/xiaofeng96301/ShelfLife
- Modrinth API mirror (versions, loaders): https://github.com/qacow37/prismnix (`packages/projects/<id>.nix`); Modrinth description mirror: https://github.com/Voxelum/xmcl-community-content-i18n-extra (`ru/<id>.json`)
- Modrinth pages (not directly accessible): https://modrinth.com/mod/salts-animal-farm · https://modrinth.com/mod/cattlestrophic! · https://modrinth.com/mod/animal-genders · https://modrinth.com/mod/animal-weight · https://modrinth.com/mod/freeze-it-and-heat-it · https://modrinth.com/mod/tide · https://modrinth.com/mod/river-fishing · https://modrinth.com/mod/thirst-canteen · https://modrinth.com/mod/comforts · https://modrinth.com/mod/hardcore-revival · https://modrinth.com/mod/nutritional-balance · https://modrinth.com/mod/complicated-bees · https://modrinth.com/mod/butchery · https://modrinth.com/mod/aquatic-breeding · https://modrinth.com/mod/kaleidoscope-contraption · https://modrinth.com/mod/kaleidoscope-orchard
- Web searches (before the quota ran out): Animal Husbandry, Spoiled and Cattlestrophic on CurseForge/Modrinth; Ecliptic Seasons on CurseForge (0.14.4, August 2026).
