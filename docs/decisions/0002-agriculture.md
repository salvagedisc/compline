# 0002 — Farming & animal husbandry

- **Date**: 2026-09-23
- **Status**: cooking superseded by [0030](0030-farmers-delight-kitchen.md) on 2026-11-15 (Farmer's Delight replaces Kaleidoscope Cookery, Create: Central Kitchen replaces KaleidoscopeCookery-Automation; Farm & Charm's cooking stations are removed and their recipes moved to Farmer's Delight). Farm & Charm, Ecliptic Seasons and KaleidoscopeCookery-Automation kept (tested on `test/agriculture`, merged into `main` on 2026-10-10); animal husbandry and preservation pending

## Expected functions of a farm
Kept: **A** processing (production chains), **B** demanding animal husbandry, **C** seasonality, **D** preservation, **F** late Create automation.
Not kept: E, specialisation between players (not a goal in itself).

## A — Farm infrastructure: Farm & Charm ✅
Kaleidoscope Cookery brings crops, a mill and a scarecrow, but no farm infrastructure. Farm & Charm brings fertiliser, fertilised soil, sprinklers, silos, a plough, a cart, a well, a chicken coop, a cowshed, and new grains.

**Condition**: Farm & Charm is confined to the farm. Its cooking stations (stove, cauldron, pan, rotisserie) and its ingredients that duplicate KC (tomato, lettuce, flour, dough) will be hidden or merged in KubeJS (sub-project 3). KC remains the only cooking system.

## B — Animal husbandry: pending
Animal Husbandry (needs, diseases, genetics) matches the requirement but is young (0.4.2). **Mechanic wanted, mod refused**: Animal Husbandry raises concerns (maturity). Look for an alternative later; in the meantime, vanilla animal husbandry + Farm & Charm infrastructure.

## C — Seasons: Ecliptic Seasons ✅ (conditional)
| | Serene Seasons | Ecliptic Seasons |
|---|---|---|
| Maturity | ★★★★★ | ★★★☆☆ (rc, 1.21.1 in maintenance) |
| Immersion | ★★★☆☆ | ★★★★★ (24 solar terms, snow that accumulates and melts, day length) |
| Crops | Season | Season + humidity + greenhouses |
| Cold Sweat | Native integration | None → KubeJS bridge |

**Condition**: a KubeJS bridge from Ecliptic to Cold Sweat (seasonal temperature felt by the player). Feasibility checked: Ecliptic exposes `EclipticSeasonsApi` (`getSeason`, `getSubSeason`, `getSolarTerm`), and Cold Sweat allows temperature modifiers to be registered in KubeJS. First script of sub-project 3. If the bridge fails in game: fall back to Serene Seasons.

## D — Preservation: pending
Spoiled (data-driven, Cold Sweat cooler compatible) was proposed but refused: the mod raises concerns. **Mechanic wanted**; look for an alternative later.

## F — Automation: KaleidoscopeCookery-Automation ✅
Lets Create's mechanical arm feed KC's stations. Locking automation behind progression belongs to sub-project 3. Create: Integrated Farming remains to be evaluated later.

## Rejected
- Let's Do Bakery: duplicates KC's dough/flour.
- Let's Do Meadow: adds biomes, likely conflict with No Man's Land.
- Graze, automatic drinking troughs: make animal husbandry self-sufficient, the opposite of B.
- Vinery, Kaleidoscope Tavern: extensions to revisit once the foundation is validated.

## Corrections (2026-10-26)
Read in the Ecliptic Seasons 0.15.5 jar for [0012](0012-sacs-piles-jours.md): in the `AGRICULTURE` mode the pack uses, crop humidity is off, so C's "season + humidity" is only "season + greenhouses" here. The day length changes by solar term only while `DynamicDaylightDuration` is true (its default); the pack turns it off, Better Days sets the day length.
