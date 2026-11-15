# Seasons and day length (research of 2026-10-26)

Compline pack, Minecraft 1.21.1, NeoForge 21.1.265. The mention "jar" means: read with `javap`. Nothing is tested in game.

## Short answers

- **A.** Ecliptic is not "better" at everything. It is richer (snow, DH, KubeJS API). Serene is simpler and better integrated with Cold Sweat, Polytone and Better Days. For Compline: **keep Ecliptic**, Serene as a fallback.
- **B.** **Better Days** (the only maintained 1.21.1 NeoForge mod that sets day and night separately) with `DynamicDaylightDuration = false` on the Ecliptic side. A gamerule alone is not enough: sleep and sky synchronization would have to be rewritten.

## A. Ecliptic Seasons versus Serene Seasons

### Comparison

| Criterion | Ecliptic Seasons 0.15.5 | Serene Seasons 10.1.0.9 |
|---|---|---|
| Calendar | 24 solar terms, 7 days per term in the pack | 12 sub-seasons, 8 days by default (96 days per year) |
| Snow | Progressive accumulation and melting, dedicated rendering or vanilla layers (`Snow Behavior`) | Snow and ice generated in winter, melting by rolls (`melt_percent`, `melt_rolls`) |
| Weather | Solar calendar, alternating rain/fair weather (0.15.3). No more local weather per biome: the plains drive it, each biome shows rain or snow depending on its temperature | Rain and storm delays per sub-season (`min_rain_time`...) |
| Crops | Growth chance per term, tags `eclipticseasons:crops/*`. Humidity and greenhouses | Tags `sereneseasons:*_crops`, `fertility.toml` (slowed, blocked or breaks), tag `greenhouse_glass` |
| Visuals | Grass, leaves, particles, sounds, models, snow | Grass, foliage, birch (color config per sub-season since 10.1.0.8) |
| Day length | Yes, per term: 9000 to 15000 ticks of day (jar) | No |
| Temperature | Does not change the vanilla temperature of biomes. SURVIVAL mode: heat stroke | Adjusts biome temperature (`biome_temp_adjustment`) |
| KubeJS | Native plugin: globals `EclipticSeasonsApi` and `EclipticSeasonsBindings` | No plugin (jar) |
| Cold Sweat 2.4.3.1 | No native support | Native support (see below) |

### Two facts to correct in decision 0002

1. In `AGRICULTURE` mode, **crop humidity is turned off**. Version 0.15.1 moved it to `SURVIVAL` (changelog, and jar: `enableCropHumidityControl` enabled only at `SURVIVAL`). What remains: growing seasons, greenhouses and restricted bonemeal.
2. `DynamicDaylightDuration` is `true` by default and is not tied to the simulation level. So the day already varies with the seasons in the pack.

### Stability and maintenance

- Ecliptic: more than 25 1.21.1 versions between 2026-07-20 and 2026-10-25. 0.15.0-rc-3 is marked "release" on Modrinth, 0.15.1 to 0.15.5 "beta". Eight open tickets, none about 1.21.1, Cold Sweat, Create or DH. The "Version support" page classes 1.21.1 as "Transitional stable release": fixes and low-cost backports, **progressive feature freeze**. New features go toward 26.x.
- Serene: 85 open tickets, including recent crashes (#611). In 1.21.1: 10.1.0.3 (February 2025), then 10.1.0.7 to 10.1.0.9 (August and September 2026), all marked beta. A very established project (7.7 million downloads).
- Performance: no comparative measurement found. Ecliptic announces mixin optimizations (0.14.90) and a DH LOD update that "may stutter". Serene does less. To measure in game.

### Cold Sweat

- Cold Sweat 2.4.3.1 contains `SereneSeasonsTempModifier` (id `sereneseasons:season`) and "Summer/Autumn/Winter/Spring Temperatures" settings (jar). Defaults read: summer [0.4, 0.6, 0.4], winter [-0.4, -0.6, -0.4].
- **Nothing for Ecliptic** (no occurrence in the jar). Cold Sweat exposes an event `FetchSeasonsModsEvent` (Java, not KubeJS) to declare another seasons mod.
- The planned KubeJS bridge is feasible: Ecliptic provides `EclipticSeasonsApi.getSubSeason(level)` with no class filter, and Cold Sweat accepts `addTempModifier` in `gatherDefaultModifiers` (repository reports).
- **Third route to note**: "Serene Seasons API Stub (Ecliptic Seasons Bridge)", on CurseForge. It declares itself `sereneseasons`, redirects the API to Ecliptic, and the author lists Cold Sweat as "verified". Reservations: self-declared, absent from Modrinth, page centered on 26.x files, incompatible with the real Serene (same identifier).

### Other mods in the pack

- **Kaleidoscope Cookery**: its crops carry `sereneseasons:*` tags. They work with Serene as is, and with Ecliptic by conversion (`CompatCropHookInternal`, repository report).
- **Farm & Charm**: its season files are in the wrong folder. Its crops grow all year with both mods. Tags must be written anyway. Ecliptic only provides tags for vanilla and Farmer's Delight (jar).
- **Create**: no conflict noted. Its machines act on normal crops, subject to the growth rules.
- **Polytone 5.1.3**: its `season()` function knows Serene and Fabric Seasons, not Ecliptic (classes `SereneSeasonsCompat`, `FabricSeasonsCompat`). A Polytone block colormap overrides Ecliptic's seasonal colors; a `biome_modifier` stays compatible. Ecliptic ticket #163 open. Fresh Animations: not studied.
- **Distant Horizons**: Ecliptic has a dedicated module (seasonal LOD update, enabled by default). For Serene, the only mod found ("Serene Seasons x Distant Horizons") is **Fabric only**. On NeoForge, the LODs would stay without seasons (not verified).

### Recommendation

**Keep Ecliptic Seasons.** It brings what Serene lacks here: seasonal LODs, snow that builds up, KubeJS API, loot condition for True Adaptive Music. The price is the dependence on a 1.21.1 line that is freezing, and the Cold Sweat bridge to write. Keep the Serene fallback if the bridge fails in game.

### Cost of a switch to Serene

- **Translations**: loss of `pack/kubejs/assets/eclipticseasons/` (955 `fr_fr` keys, 9 chronicle files). Serene already has a `fr_fr` (a few keys).
- **True Adaptive Music**: the 4 predicates `printemps/ete/automne/hiver.json` (spring/summer/autumn/winter) use the condition `eclipticseasons:season`. Serene has none. The player would have to be tagged from `musique.js` with `SeasonHelper` and the tag read in TAM.
- **Configs**: `eclipticseasons-common.toml` replaced by Serene's `seasons.toml` and `fertility.toml` (`day_duration`, `sub_season_duration`). Loss: center-block greenhouses, variable day length, progressive snow.
- **Tags**: rewrite the F&C crop tags as `sereneseasons`. Kaleidoscope's already work.
- **Gains**: no more Cold Sweat bridge, Polytone `season()` works, Better Days is officially supported.

## B. Days longer than nights

### Vanilla durations

A cycle is 24000 ticks, or 20 minutes. Day 0 to 12000 (10 min), dusk 12000 to 13000 (50 s), dark night 13000 to 23000 (about 8 min 20 s), dawn 23000 to 24000 (50 s). So the dark night is a little over 8 minutes, not 7.

### Mods found (NeoForge 1.21.1)

| Mod | Day and night separate | Method | Remark |
|---|---|---|---|
| **Better Days 3.3.6.3** (2026-04-24) | **Yes** | Time speed per phase, accelerated sleep | LGPL, fork of Hourglass, client and server, depends on WhiteNoise |
| OpticManager 1.3.3 (2025-04) | Yes (listing) | Not read in detail | Depends on CreativeCore |
| Day Length 2.0.0, Extended Time 1.0.0, Time Adjustment 2.2.0, Daylight Dynamics, Extra Long Days | No (one single setting) | Uniform speed; Extended Time: server-side accumulator, handles `doDaylightCycle` | Time Adjustment and Daylight Dynamics: all rights reserved |
| Longer Days | Pauses (adds minutes) | Freezes the day at fixed hours | Frozen sky, not smooth |
| Hourglass | Not applicable | No longer maintained | Better Days is its successor (Better Days page) |

Nothing useful found under the names "Time Control" or "Daylight Mod". Dynamic Daytime (duration according to days played) was not read.

### How Better Days works (jar 3.3.6.3)

- It **does not change the tick speed**. Each server tick, it adds a fraction to `dayTime` (decimal accumulator), cancels the vanilla +1 (`vanillaTimeCompensation`) and sends the time to clients, which interpolate. It respects `doDaylightCycle`. No mixin in the jar.
- So crops, furnaces, hoppers, Create movement, monster spawns per tick: **unchanged**. The "time effects" that would speed up these systems are all at `NEVER` by default (`randomTickEffect`, `blockEntityEffect`, `potionEffect`, `hungerEffect`). Only `weatherEffect` is `SLEEPING`: the weather advances faster during sleep.
- The formula: speed = 10 / minutes. The "day" goes from `dayStart` (23500) to `nightStart` (12500), that is **13000 ticks**, and the "night" is **11000 ticks**. So "30 minutes" actually gives 32.5 minutes of day. This must be corrected (see below).
- Sleep: the speed rises with the share of players in bed (`sleepSpeedMax` 110). It can be turned off (`enableSleepFeature = false`) to keep vanilla.
- Limits: Overworld only. Conflicts: Quark "Improved Sleeping" (disabled or absent from the pack's file), Immersive Snow, Steve's Realistic Sleep. Open tickets: #79 (WhiteNoise sync error at connection, no solution), #84 (fewer monsters if you sleep), #82 and **#76 (Ecliptic support)**.

### Do Ecliptic and Serene change the day length?

- **Ecliptic yes, but only the sky.** The jar shows `MixinLevelTimeAccess` which replaces `getTimeOfDay` when `DynamicDaylightDuration` is true. It does **not** touch the clock: the cycle stays at 24000 ticks (hard-coded constant). The sun is re-centered around noon (tick 6000): sunrise at `6000 - D/2`, sunset at `6000 + D/2`, where D is 9000 (winter) to 15000 (summer).
- Its calendar reads `dayTime` modulo 24000 and detects the passing of midnight. So it follows a slowed clock, and also sleep.
- **Serene does not change the day length.** Its `day_duration` config says it is "to be used with another mod that adjusts the real length of the day".
- **Ecliptic + Better Days conflict**: no crash found, but the Better Days author says (ticket #76) that Ecliptic's work on day and night "alters sleep", that he does not know how to integrate it and that the Ecliptic developer refused his requests. Official support: no.
- **Phase disagreement**: Better Days changes speed at fixed ticks (12500 and 23500), whereas Ecliptic moves sunrise and sunset. Example with 30/15: in winter (D = 9000) the sun is up for about 21 minutes against 24 minutes of darkness. The day would be shorter than the night, the opposite of the wish. Hence the recommendation to turn off `DynamicDaylightDuration`.

### Other interactions

- **Enhanced Celestials 2**: everything is in `dayTime` (`day_length` 24000, `night_start_time` 12500 in `overworld.json`), so aligned with Better Days' `nightStart`. Moons are rarer in real time (a day lasts 2.25 times longer in 30/15).
- **Crops and furnaces**: unchanged per tick. But Ecliptic's calendar counts in days, so seasons get longer in real time: compensate with `LastingDaysOfEachTerm` (values below).
- **Create**: machines run on ticks, unchanged. "Time of day" train schedules follow the game clock, so more slowly in real time.
- **Monsters**: the per-tick rate is unchanged. With 30/15, the night share falls from 46% to 33% of real time, about 27% fewer monsters per hour, but longer nights. Villagers: schedules based on `dayTime`, so slower in real time.
- **True Adaptive Music**: `day` and `night` predicates read on `getDayTime() % 24000` (night from 13000). No equality event is used in the pack. The nodes follow the clock.
- **ATMOSPHERICS**: reads `getTimeOfDay`, `getSunAngle` and `getDayTime() % 24000`, with windows of 180 to 1800 ticks (`DayCycleWindowUtil`). Everything follows the clock. A mix of raw time and time re-centered by Ecliptic as long as the dynamic day stays active: another reason to turn it off.
- **Distant Horizons**: no time dependency found. Fewer season changes in real time, so fewer LOD refreshes.
- **Cold Sweat**: its day/night curve follows `dayTime` (`Hottest Time` 6000, `Coldest Time` 18000). It stretches with the clock.

### Can it be done without a mod?

Yes, but less well. With `doDaylightCycle false` and a KubeJS script that adds to `dayTime` each tick, we get the right speed. What is missing:
- **sleep**: the vanilla skip to morning only happens if `doDaylightCycle` is true (to check in game). So we would have to write our own skip to morning;
- **smoothness**: the client no longer advances on its own, the sky updates once per second, without interpolation (known vanilla behavior, not tested here);
- **robustness**: rounding, `/time set`, saving the remainder.

A Java `jour` (day) module in the `compline` mod would fix the smoothness, but this would be rewriting Better Days. To keep in reserve if it fails in game.

Sketch (KubeJS 7 API to confirm):

```js
const vJour = 13000 / (30 * 1200);  // 0.3611 tick per tick
const vNuit = 11000 / (15 * 1200);  // 0.6111
let reste = 0;
// each tick, Overworld, doDaylightCycle = false:
// t = dayTime % 24000 ; v = (t >= 12500 && t < 23500) ? vNuit : vJour
// reste += v ; n = Math.floor(reste) ; reste -= n ; setDayTime(dayTime + n)
```

### Recommendation and values

**Better Days + `DynamicDaylightDuration = false`**, vanilla sleep kept (the pack already sets `playersSleepingPercentage 50`).

`config/betterdays-common.toml`, option 30 min of day / 15 min of night (45 min cycle, 2.25 times vanilla):

```toml
[time]
speedMethod = "MINUTES"
daySpeedMinutes = 27.7     # 13000 ticks, gives 30.0 real minutes
nightSpeedMinutes = 16.4   # 11000 ticks, gives 15.0 real minutes
dayStart = 23500.0
nightStart = 12500.0
[sleep]
enableSleepFeature = false
```

Option 60 min / 30 min (90 min cycle, 4.5 times vanilla): `daySpeedMinutes = 55.4`, `nightSpeedMinutes = 32.7`.

Equivalent in `RATIO`: 30/15 gives `daySpeed = 0.3611` and `nightSpeed = 0.6111`; 60/30 gives `0.1806` and `0.3056`. Check: 13000 / (0.3611 × 20) = 1800 s.

`config/eclipticseasons-common.toml`:

```toml
[Season]
DynamicDaylightDuration = false   # section to confirm in the generated file
LastingDaysOfEachTerm = 3         # 45 min: term of 2 h 15, year of about 54 h
# for 60/30: LastingDaysOfEachTerm = 2 (term of 3 h, year of 72 h)
```

Without this setting, a term of 7 days would last 5 h 15 (30/15) or 10 h 30 (60/30) and the year more than 5 days (126 h) or 10 days (252 h). Current vanilla gives 2 h 20 per term and 56 h per year.

### To test in game

1. A full cycle: the real durations measured (30/15) and the smoothness of the sky in multiplayer.
2. The WhiteNoise error (#79) when a client connects to the server.
3. Ecliptic's day counter during an accelerated sleep (speed up to 110).
4. The EC2 moon that really starts at sunset, and TAM `night` nodes (night at 13000, so 500 ticks after the speed change).
5. Fallback: Extended Time or Day Length at a single speed if Better Days causes problems (we lose the asymmetry), or the script described above.

## Sources

Opened pages:
- https://ecliptic-seasons.readthedocs.io/en/latest/en/
- https://ecliptic-seasons.readthedocs.io/en/latest/en/configuration/
- https://ecliptic-seasons.readthedocs.io/en/latest/en/season/
- https://ecliptic-seasons.readthedocs.io/en/latest/en/climate/
- https://ecliptic-seasons.readthedocs.io/en/latest/en/custom/visuals/
- https://ecliptic-seasons.readthedocs.io/en/latest/en/modpack_guide/
- https://ecliptic-seasons.readthedocs.io/en/latest/en/troubleshooting/
- https://ecliptic-seasons.readthedocs.io/en/latest/en/version_support/
- https://github.com/TeamTeaMC/Ecliptic-Seasons/issues
- https://github.com/Glitchfiend/SereneSeasons/issues
- https://github.com/wendall911/BetterDays
- https://github.com/wendall911/BetterDays/issues
- https://github.com/wendall911/BetterDays/issues/76
- https://github.com/wendall911/BetterDays/issues/79
- https://github.com/wendall911/BetterDays/issues/84
- https://www.curseforge.com/minecraft/mc-mods/serene-seasons-api-stub-ecliptic-seasons-bridge
- https://modrinth.com/mod/serene-seasons-x-distant-horizons

Modrinth API (listings, versions, changelogs, jars):
- https://api.modrinth.com/v2/project/ecliptic-seasons (and `/version`)
- https://api.modrinth.com/v2/project/serene-seasons (and `/version`)
- https://api.modrinth.com/v2/project/cold-sweat (and `/version`)
- https://api.modrinth.com/v2/project/betterdays, `day-length`, `time-adjustment`, `extendedtime`, `extra-long-days`, `opticmanager`, `daylight-dynamics`, `longer-days`, `dynamic-daytime`, `enhanced-celestials-2-core`, `true-adaptive-music`

Jars read (javap): Ecliptic Seasons 0.15.5, Serene Seasons 10.1.0.9, Cold Sweat 2.4.3.1, Better Days 3.3.6.3, Enhanced Celestials 2 Core 2.0.3.3, True Adaptive Music 2.9.3, Polytone 5.1.3, ATMOSPHERICS 2.6.5. Repository files: `docs/decisions/0002-agriculture.md`, `pack/config/eclipticseasons-common.toml`, `docs/plan/recherche/verif-2026-10-18/` (2-kaleidoscope, 3-client), `pack/trueadaptivemusicpacks/compline/`.

Searches only, not opened (clues to check): listing of the Serene/Ecliptic stub on moddex.gg (403), result lists on the stub's 1.21.1 files.
