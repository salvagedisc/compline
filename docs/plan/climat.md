# The climate that weighs

The team's study of 2026-11-17 ("fonctions à ajouter"), chosen the same evening: the seasons, the weather and food should be felt by the body, so that winter, a storm or a hot meal change what the player does. Cold Sweat already gives the pack a body temperature; the `compline` mod's `climat` module ([mod-compline](mod-compline.md#climat-module)) plugs the other systems into it. Three steps, one pull request each:

1. **Seasons**: Ecliptic Seasons' season warms or cools the body (this page, first step).
2. **Storms**: snow and sand storms (second step).
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

## Storms (second step)

**What counts as a storm** (`Storm`, the same rule on the server and the client): it must be raining in the world.

- **Snow**: Ecliptic says it snows where the player stands (it decides rain or snow by biome and season, so a temperate forest gets snow in winter). **Snow storm**: snow under thunder.
- **Sand storm**: nothing falls, in a biome that is hot (base temperature 1.0 or more) and has no precipitation in its own data: desert, badlands, savanna outside the rainy season Ecliptic gives it. The rain front becomes a sand wind there, at 60 % without thunder and in full under thunder.
- **Shelter**: a storm only touches a player under the open sky (`canSeeSky` at the eyes). A roof is enough.

**What the body feels** (server):

- **Snow chills and settles.** A second temperature modifier, `compline:tempete`, right after `compline:saison`, refreshed every second for a player: falling snow takes `snowCold` (0.12, about 3 °C) off, two and a half times as much in a snow storm (about 7.5 °C). Snow also settles on the clothes: covered after a minute and a half of snow, or about 36 seconds of storm, which takes up to `snowWet` (0.2, about 5 °C) more. Under a roof it melts in a minute, in twenty seconds where it is mild (above 0.4, fires included) or in water. Cold Sweat only soaks the body in the rain; snow was left out. The amount of settled snow is kept with the modifier, which Cold Sweat saves with the player.
- **Sand makes thirsty** (with Thirst Was Reclaimed): every second in the open, `sandThirst` (0.15) is added to Thirst's exhaustion under thunder, 60 % without: one drop of thirst every 27 seconds on top of the usual thirst. It is added to the exhaustion directly, because Thirst's own `addExhaustion` multiplies it by a factor of place and body temperature that runs away when the body is cold. Heat is not added: the sand hides the sun, and Thirst already drinks faster when Cold Sweat's body is hot.

**What the player sees** (client, `stormVisuals`):

- **Sand**: tawny dust blown along the pack's wind (`vent` module), up to 22 grains a tick around the camera, only where the sky is open; the mist closes to about 22 blocks and takes the colour of sand, darker at night.
- **Snow storm**: blown snowflakes on top of the game's snow; the mist closes to about 36 blocks and turns white.
- The strength follows the game's rain and thunder levels and eases in and out over about four seconds; under a roof the storm is still seen outside, at about a third. The mist goes through ATMOSPHERICS (the `ciel` module's hook on its smoothed fog, `StormView`), so it needs ATMOSPHERICS. **Brumaire draws its own mist** and does not know about storms yet: under the shader pack only the dust and flakes show. Passing the storm to Brumaire is left to the shader's own work.

Config: `compline-common.toml` `[climat]`: `snowCold` (0.12), `snowWet` (0.2), `sandThirst` (0.15); `compline-client.toml` `[climat]`: `stormVisuals` (true).

**Checked** on the bench on 2026-11-18 (development server with Cold Sweat, Ecliptic Seasons and Thirst Was Reclaimed; a client with ATMOSPHERICS joined; winter, `/weather thunder`):

- In a temperate forest under snow, `compline:tempete` took 0.42 off after 20 seconds in the open (0.30 of storm, 0.12 of settled snow); on reaching the savanna the settled snow melted away.
- In the savanna, the server saw a sand storm (Ecliptic: no precipitation; data: no rain; base temperature 2.0); in 75 seconds in the open, thirst fell from 14 to 11, with Thirst's own thirst on top; under a stone roof, the exhaustion stopped moving.
- The mist turned sand-coloured and closed in the savanna (open sky then under a roof) and greyed under the snow storm (with `stormVisuals` off, then on). The bench has no graphics card and runs at a few frames per second: the density of the dust was not judged.

## In-game tests

| # | Test | Expected |
|---|---|---|
| CL1 | In a temperate biome, `/eclipticseasons solar set 64` (winter solstice), wait a few seconds, `/temp debug @s world` | `compline:saison` after `cold_sweat:biomes`, about −0.64; Cold Sweat's gauge falls outdoors without warm clothes |
| CL2 | `/eclipticseasons solar set 34` (greater heat) in a desert at noon | About +0.3 more than in spring: the heat becomes a danger without shade or water |
| CL3 | Play through a whole term change (or `/eclipticseasons solar add 1` three times) | The offset moves a little each day, never in one jump |
| CL4 | Go to the Nether in winter | `compline:saison` adds 0 there |
| CL5 | Set `seasonStrength = 0` in `compline-common.toml`, then back to 1.5 | The season stops counting, then counts again, without restarting |
| CL6 | In winter, `/weather thunder` in a forest, stand in the open for a minute, then go under a roof | `compline:tempete` about −0.3 at once, then down to about −0.5 as snow settles; under the roof the falling snow stops counting and the settled snow melts within a minute |
| CL7 | Snow storm, with and without Brumaire | Without shaders: blown flakes and a white mist at about 36 blocks; with Brumaire: the flakes only |
| CL8 | `/weather thunder` in a desert (or `/weather rain`), stand in the open | Dust blows with the wind, the mist closes to about 20 blocks and turns sand-coloured (without shaders); thirst drops about every 20 to 30 seconds |
| CL9 | Same, under a roof or in a house | Thirst goes back to its usual pace; the storm is still seen outside, fainter |
| CL10 | Savanna in Ecliptic's rainy season | Rain, not sand |
