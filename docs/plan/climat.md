# The climate that weighs

The team's study of 2026-11-17 ("fonctions à ajouter"), chosen the same evening: the seasons, the weather and food should be felt by the body, so that winter, a storm or a hot meal change what the player does. Cold Sweat already gives the pack a body temperature; the `compline` mod's `climat` module ([mod-compline](mod-compline.md#climat-module)) plugs the other systems into it. Three steps, one pull request each:

1. **Seasons**: Ecliptic Seasons' season warms or cools the body (this page, first step).
2. **Storms**: snow and sand storms (planned).
3. **Hot dishes**: Farmer's Delight's soups and stews warm, cold dishes cool (planned).

## Seasons (first step)

**Before.** Cold Sweat 2.4.3.1 only knows Serene Seasons (`SereneSeasonsTempModifier`, `FetchSeasonsModsEvent`, the seasonal settings of its world config); Ecliptic Seasons 0.15.5 does not know Cold Sweat. Both were checked in the jars on 2026-11-17: in Compline, winter froze the water and killed the crops but the body felt nothing.

**Now.** A temperature modifier `compline:saison` sits right after Cold Sweat's biome modifier, where Cold Sweat puts its Serene Seasons one, for every entity with a temperature (the player, and the animals Cold Sweat follows). It adds the season's offset to the biome's temperature.

- **Where the offset comes from.** Ecliptic splits the year into 24 solar terms of 3 days each (`eclipticseasons-common.toml`: a term lasts about 2 h 15 of play, a year about 54 h). For each biome and term, Ecliptic keeps a temperature offset, the one that decides where water freezes and snow falls; a temperate biome goes from −0.45 at Lesser Cold to +0.25 at Greater Heat. The module reads that offset (`BiomeClimateManager`) instead of inventing a curve, so a biome Ecliptic configures apart stays apart for the body too.
- **No jump at the turn of a term.** The offset slides from the current term to the next over its three days: at the middle of the first day it is a sixth of the way, at the middle of the last day five sixths.
- **Units.** Ecliptic's offset is in biome units; Cold Sweat counts in the same unit (1 = 25 °C). It is multiplied by `seasonStrength` (1.5): winter takes about 15 to 17 °C off a temperate biome, high summer adds about 9 °C. Serene Seasons' defaults in Cold Sweat are of the same order (−0.6 and +0.6).
- **Only where there are seasons.** Outside the dimensions where Ecliptic counts seasons (the Nether, the End), the offset is 0.
- **Rhythm.** Refreshed every 3 seconds for a player, every 20 seconds for an animal, as Cold Sweat does for Serene Seasons.

Config (`compline-common.toml`, `[climat]` section): `enabled` (true), `seasonStrength` (1.5, from 0 to 4; 0: no season).

**Checked** on the bench on 2026-11-17 (development server with Cold Sweat 2.4.3.1 and Ecliptic Seasons 0.15.5, a player joined): `/temp debug Dev world` lists `compline:saison` right after `cold_sweat:biomes`. At the Spring Equinox it adds 0; set to Winter Solstice (`/eclipticseasons solar set 64`, second day of the term) it turns 0.56 into −0.07, an offset of −0.64 (−0.425 from Ecliptic × 1.5), and the body temperature starts to fall; at Greater Heat it adds +0.30; in the Nether it adds nothing.

## In-game tests

| # | Test | Expected |
|---|---|---|
| CL1 | In a temperate biome, `/eclipticseasons solar set 64` (winter solstice), wait a few seconds, `/temp debug @s world` | `compline:saison` after `cold_sweat:biomes`, about −0.64; Cold Sweat's gauge falls outdoors without warm clothes |
| CL2 | `/eclipticseasons solar set 34` (greater heat) in a desert at noon | About +0.3 more than in spring: the heat becomes a danger without shade or water |
| CL3 | Play through a whole term change (or `/eclipticseasons solar add 1` three times) | The offset moves a little each day, never in one jump |
| CL4 | Go to the Nether in winter | `compline:saison` adds 0 there |
| CL5 | Set `seasonStrength = 0` in `compline-common.toml`, then back to 1.5 | The season stops counting, then counts again, without restarting |
