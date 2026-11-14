# 4. Daily life: water, fields, herd, cooking, stores

> **History (plan v1, 2026-10-10).** Superseded by chapters [10](10-recentrage.md) to [12](12-verification.md), the [registry](../../mods.md) and the [decisions](../../decisions/README.md): several mods listed here have since been dropped. Kept for its reasoning.

What already exists (0002): Kaleidoscope Cookery (the only cooking system), Farm & Charm (farm infrastructure), Ecliptic Seasons, KaleidoscopeCookery-Automation, Cold Sweat, Thirst Was Reclaimed. Decision 0002 left two gaps: **demanding animal husbandry** without Animal Husbandry and **food preservation** without Spoiled. This chapter fills them, and settles how Farm & Charm and Kaleidoscope Cookery coexist.

Details, evidence and keys: [recherche/research-vie.md](recherche/research-vie.md).

## 4.1 The gameplay loop

Safe water → seasonal fields → fodder → a herd that is fed, sheltered and kept in pairs → products → KC cooking → preservation → expedition rations (canteen, provisions bag, sleeping bag) → exploration. In winter, whatever was not set aside runs short.

## 4.2 What the selected mods already do

Not to be bought again elsewhere (checked in the source code):
- **Kaleidoscope Cookery** already provides the **season tags** (Serene Seasons, Ecliptic humidity) and nutrition tags for its crops, and a **bamboo tray** that dries in the sun and gets damp in the rain. It has its own KubeJS schemas: `bamboo_tray`, `chopping_board`, `millstone`, `pot`, `steamer`, `stockpot`.
- **Ecliptic Seasons** handles day length, **seasonal breeding** (`EnableSeasonalBreed`), **seasonal bees** and **seasonal fishing**. It exposes `EclipticSeasonsApi` to KubeJS (`getSeason`, `getSubSeason`, `getSolarTerm`, `getAdjustedHumidity`...). Its `HeatStroke` duplicates Cold Sweat: **to be disabled**.
- **Cold Sweat 2.4** exposes `coldsweat.getTemperatureAt`, `createModifier`, `addModifier`... to KubeJS. It has an icebox and a boiler, and native compatibility with Thirst Was Reclaimed, Supplementaries, Sable and Aeronautics.
- **Thirst Was Reclaimed**: four purity levels, sickness, purification over a campfire and in a furnace, **Create sand filter**.
- **Farm & Charm**: note that its **feeder puts animals in love automatically**: this is the autonomous breeding that decision 0002 rejects.

## 4.3 Proposed mods

| Mod | Role | Availability 1.21.1 NeoForge Modrinth | Proposed status |
|---|---|---|---|
| **Kaleidoscope Compat** | Merges KC ↔ Farm & Charm duplicates through `c:` tags; replays the F&C cooking pot recipes in the KC stockpot; option that turns off the F&C pot. **This is the condition of 0002, ready-made** | yes (2.9.7) | foundation |
| **Cattlestrophic!** | Hunger and old age for animals; wild (little loot, no leash) and domestic (starve if not fed) | yes (1.3.0) | foundation, to be tested |
| **Salt's Animal Farm** | Comfort, shelter, hidden weight, sickness from neglect, panic. Adds no items: the F&C barn and straw become gameplay | yes (0.2) | foundation, to be tested over a long period (young mod) |
| **Animal Genders** | One male and one female are needed to breed; only females lay eggs or give milk | yes (1.2.0) | recommended |
| **Freeze It and Heat It** | Cold Sweat addon: food freezes in the cold or spoils in the heat, repairable at the icebox or the boiler | yes (4.1.0) | foundation, if it loads with Cold Sweat 2.4.3.1 |
| **"Pantry" script** | Slow spoilage in KubeJS (see 4.5) | — | foundation (script) |
| **Kaleidoscope Tavern** | Same team as KC: vines, treading, barrels with 7 quality levels, cellar, ageing | yes (1.2.0) | recommended, **in place of Vinery** |
| **Thirst Canteen** | Canteens for Thirst Was Reclaimed | yes (3.7) | recommended |
| **Tide** (+ Tide Extra Compatibility) | Fishing by species according to biome, depth, time and weather; baits; journal | yes (2.1.1) | recommended |
| **Nutritional Balance** | Nutrients by tag, malnutrition and excess: KC's variety becomes useful | yes (7.0.3) | recommended |
| **Hardcore Revival** | Downed instead of dead; teammates revive | yes | recommended |
| **Comforts** | Sleeping bags and hammocks (already proposed in [2](02-exploration.md)) | yes | recommended |
| **AppleSkin** | Hunger and saturation preview (already proposed in [6](06-technique-et-serveur.md)) | yes | recommended |

**Optional**:
- River Fishing: a true freshwater simulator (assembled rod, overfishing, ice fishing), but it adds a fisherman NPC. To be compared with Tide on a branch; keep only one.
- Fisherman's Trap: bait trap.
- Butchery: carcasses, bleeding, hide drying rack (output tuned to vanilla).
- Aquatic Breeding: the only real genetics found for 1.21.1 (tropical fish).
- Kaleidoscope Contraption: KC cooking on Create contraptions, hence a dining car (on airships, to be checked with Sable).
- Kaleidoscope Orchard: demanding fruit trees, still very young (0.0.2).
- Kaleidoscope Grilling: later.
- Complicated Bees: bee genetics, without the species that produce ore.
- Nourished: alternative to Nutritional Balance.
- Sleep Quality or Healthy Sleep: only one of the two.
- Animal Weights: fallback if Salt's proves unstable.

**Ruled out**:
- Let's Do Vinery: duplicate of Kaleidoscope Tavern.
- Animania Reborn: CurseForge only, to be watched.
- ShelfLife: GitHub only.
- SpoiledZ, Food Spoiling: no 1.21.1 version.
- Diet: stuck on 1.20.1; Spice of Life: Onion.
- Hourglass: no 1.21.1 version, and Ecliptic already handles day length.
- Create Wells, Just a Well: duplicates of the F&C well.
- First Aid New: heavy config, competes with Hardcore Revival.
- Butchercraft (depends on Extra Delight), Barnyard Buddies (content), Animal Feeding Trough (autonomous breeding).
- Serene Seasons API Stub (Ecliptic Seasons Bridge): it would let Cold Sweat see Ecliptic's seasons without a script, but it is only on CurseForge.

## 4.4 Who does what

| Function | Mods | What is switched off |
|---|---|---|
| Climate | Ecliptic + KubeJS bridge to Cold Sweat | `HeatStroke = false` |
| Water | Thirst Was Reclaimed + Thirst Canteen + F&C well; Create sand filter in the industrial era | — |
| Fields | KC + F&C + Ecliptic seasons and humidity | `enableRainGrowthEffect` reduced (it stacks with Ecliptic's humidity) |
| Cooking | KC alone (with Kaleidoscope Compat) | F&C stations and dishes |
| Drinks | Kaleidoscope Tavern | Vinery, Molotov cocktail |
| Livestock | Cattlestrophic + Salt's + Animal Genders + Ecliptic seasonal breeding + F&C buildings | Automatic breeding by the F&C feeder; Quark feeder (already off) |
| Fishing | Tide (or River Fishing after comparison) | Ecliptic seasonal fishing to be tuned so the two do not stack |
| Bees | Ecliptic (+ Complicated Bees as an option) | — |
| Preservation | Freeze It and Heat It + Pantry (KC drying, cellar, icebox, Tavern barrels) | — |
| Nutrition | Nutritional Balance + AppleSkin | — |
| Health and rest | Hardcore Revival + Comforts | — |

## 4.5 The "Pantry" (KubeJS script)

With no reliable mod on Modrinth, slow spoilage is written in KubeJS, modelled on ShelfLife:
1. Tags `compline:perishable/fast`, `compline:perishable/medium`, `compline:perishable/slow` and `compline:preserved`.
2. The production date is rounded **to the day** (`custom_data.compline_day`), so that stacks from the same day merge.
3. The calculation only runs when a container is opened, on pickup, and roughly every 30 s for the player's inventory: no per-tick cost.
4. Location matters: container temperature via `coldsweat.getTemperatureAt` (cold cellar ×0.3, icebox ×0.1), faster in summer via `EclipticSeasonsApi`.
5. Spoiled food becomes rotten flesh or compost, which goes back into F&C's fertilisers.
6. Plain tooltip ("fresh", "to be eaten soon", "spoiled"), with no precise countdown.
7. Processing is rewarded: dried on KC's bamboo tray (open sky, no rain, hence seasonal), salted, put in a barrel, the food moves into `compline:preserved`.

**Gradual rollout**: start with the **expedition rations** only, then extend if the mechanic works out.

## 4.6 Ecliptic Seasons → Cold Sweat bridge

First script of sub-project 3 (decision 0002). Ecliptic exposes the season and the solar term, Cold Sweat exposes the creation of temperature modifiers. The script reads the sub-term once per game minute and applies a seasonal modifier to the player (for example −0.4 in deep winter, +0.3 at the height of summer, in Cold Sweat units to be calibrated). The draft is in [kubejs-brouillons/server_scripts/saisons_cold_sweat.js](../kubejs-brouillons/server_scripts/saisons_cold_sweat.js). If the bridge fails in game, the fallback remains Serene Seasons (native integration with Cold Sweat).

## 4.7 Settings to apply

| Mod | Setting | Reason |
|---|---|---|
| Ecliptic Seasons | `HeatStroke = false`; `EnableSeasonalBreed = true`; `EnableSeasonalFishing` to be tuned together with Tide | No overlap with Cold Sweat; seasonal breeding |
| Kaleidoscope Compat | `farm_and_charm_compat_enabled = true`, `cooking_pot_recipes_disabled = true` | KC is the only cooking system (0002) |
| Farm & Charm | Feeder recipe removed, or love mode cancelled in KubeJS; `enableRainGrowthEffect` reduced | No autonomous breeding |
| Cattlestrophic | `acceptedFoodItems` with F&C's barley, oats, corn, fodder and grain and KC's rice; JSON for Untitled Duck Mod; decide the status of animals already present | Field → fodder → herd loop |
| Salt's Animal Farm | List of frightening mobs rewritten with the Naturalist and Alex's Mobs predators; soft blocks: KC straw, F&C barn floor | Vanilla hostiles no longer exist |
| Animal Genders | Exclude hamsters (own genetics); check the F&C henhouse | — |
| Freeze It and Heat It | Whitelist aligned with `compline:perishable/*`; dried foods exempted | A single preservation logic |
| Kaleidoscope Tavern | Molotov cocktail disabled | No incendiary weapon |
| Thirst Was Reclaimed | `item-settings.toml`: KC and Tavern drinks; F&C well water "acceptable", collected rain "purified" | Consistent water |
| Nutritional Balance | Check that it correctly derives nutrients from KC recipes; tag them otherwise | — |
| Complicated Bees (if kept) | Ore species removed | Overgeared balance |

**Natural regeneration** (decision 12 of [8.4](08-mise-en-place.md#84-decisions-to-validate-together)): proposed default, **keep it** (`naturalRegeneration true`). Hardcore Revival, hunger and thirst are enough to make survival demanding without turning every injury into a chore.

## 4.8 To check in the .jar files

1. Freeze It and Heat It: accepted Cold Sweat versions, containers supported, keys.
2. Salt's Animal Farm: keys, animals from other mods, performance, stacking with Cattlestrophic.
3. Cattlestrophic: identifier of its feeder, pre-existing animals, ducks.
4. Animal Genders: F&C henhouse, exclusions.
5. Kaleidoscope Compat: list of merged items, compatibility with KC 1.5.1 (or move to 1.6.0).
6. KC 1.5.1: does it already contain the season tags, or is 1.6.0 needed?
7. Kaleidoscope Tavern: cellar, Molotov, thirst.
8. Tide or River Fishing: duplicates with Hybrid Aquatic, fisherman NPC.
9. Thirst Canteen 3.7 with TWR 3.0.6.
10. Ecliptic: `ForceCompatMode` with F&C; stacking of seasonal breeding with the feeder.
11. F&C well: water purity for TWR.
