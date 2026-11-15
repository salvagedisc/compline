> Archived since 2026-11-13: No Man's Land left the pack ([decision 0019](../../decisions/0019-leaving-no-mans-land.md)). This page is kept as history.

# The Friendly Moon: the central mystery of Compline

Idea 2 taken from Wayfarer ([research of 2026-10-19](recherche/2026-10-19/wayfarer.md), recommendation 2). No Man's Land (NML 1.6.1) already contains a quest: a moon that talks. Compline makes it its main thread, and has it coexist with the moons of Enhanced Celestials, including the white moon. This document settles points 6 and 18 of [report 4](recherche/verif-2026-10-18/4-monde-et-objectifs.md).

> **Status as of 2026-10-19.** Everything comes from reading the jars (NML 1.6.1, Enhanced Celestials 6.0.2.6 then Enhanced Celestials 2 Core 2.0.3.3, ATMOSPHERICS 2.6.5, KubeJS 2101.7.2, LootJS 3.7.0), decompiled with `javap` and Vineflower. A first pass on the test server without a player validated the server side ([results](#test-server-2026-10-19)). **Nothing has been tested with a player.** The list of [tests](#what-remains-to-be-checked-in-game) says what to check.

## In brief

- The Friendly Moon is not a randomly drawn event. It lives in a place, the **Meeting Point**, and shows itself there **every night** to whoever comes to see it.
- You get there by exploring: a **moon carving** at the bottom of the alchemists' ruins, then a **dream**, then a **shadow in the sky** that shows the way. No coordinates.
- **Only one moon at a time.** If an Enhanced Celestials moon is up when the Friendly Moon appears, that moon sets for the night. Script: `pack/kubejs/server_scripts/lune.js`.
- The **blood moon** and the **super blood moon** are removed, as the plan intended. They prevented sleeping, and therefore dreaming.
- The **white moon** stays rare: about 3 nights in 1,000. Caution: it is **not yet in the pack**. It exists only as a draft (see [below](#the-white-moon)).

## The Friendly Moon, for a player

1. **The ruins.** The alchemists' ruins are underground, in cave biomes, between heights 10 and 40. The heart of each ruin opens onto a moon sanctuary, with a large carving in the shape of a moon face. Smaller carvings also decorate some ruins. A crumpled note, sometimes found in the jars, mentions it (Compline addition).
2. **The carving.** It is enough to look at the carving for 2 seconds, within 24 blocks. The image zooms and a sound rings out. Nothing else announces it.
3. **The dream.** During the next 3 game days (counted while connected), a player sleeping in a bed has a dream: an avenue of monoliths, with no interface bar, unable to run. At the end, a pool. You have to look up at the moon and watch it for 10 seconds. Leaving the avenue wakes the player with no result. Achievement: "First Contact".
4. **The shadow.** For the 8 nights following the dream, a moon shadow appears in the sky, towards the Meeting Point. It fades when you arrive. After 8 nights, it no longer returns: you have to go back and look at a carving, then dream again.
5. **The Meeting Point.** A circle of menhirs around a pool and candles, on the surface, between 1,000 and 2,500 blocks from the centre of the world. There is **only one per world**, shared by all players.
6. **The awakening.** At night, within 16 blocks of the pool, the player looks at the moon. A sleeping face appears, then wakes. The night darkens, the candles light up and the Friendly Moon greets the player. Achievement: "Friendship".
7. **The offerings.** An item thrown into the pool, or an animal placed on it, rises in a beam of light. The Friendly Moon comments on it: 80 lines, for items (bones, flowers, ancient bell, oath sword…) and creatures (deer, goose, turtle, bee…). A blank map becomes a map of the pool, and a map that is already filled gains a marker there. These are memories and confidences, not rewards.
8. **The ascension.** You bring a **Companion** (NML's little mushroom being) to the pool. The Friendly Moon lifts it for 15 seconds and it becomes a star in the sky. It then leaves the "Guidance" disc by Samuel Organ. Achievement: "Friend of My Friend".

What upsets it:
- Putting out the candles: it becomes troubled. If all of them are put out, it gets angry.
- The **Bad Omen**, or offering an NML **trinket**: it gets angry with that player until morning.
- A jukebox playing near the pool: it asks for it to be stopped and ejects the disc.

What it gives: three achievements, the "Guidance" disc, one more star in the sky per Companion raised, a map of the pool and above all **dialogues**. It gives no equipment. It is a mystery to listen to, not a shop.

## How it works (read from the jar)

| Element | Value | Class |
|---|---|---|
| Night | From nightfall (time ≈ 13,900) to dawn (≈ 22,100): `getTimeOfDay` between 0.3 and 0.7. No moon phase, no randomness, no weather condition | `FriendMoon.isNightTime` |
| Presence | Player within 16 blocks of the pool (cube), looking at the moon | `FriendMoon.appearConditionsMet`, `FriendMoonRenderer` |
| Departure | More than 60 ticks out of range: farewell line; death has its own lines | `FriendMoon.tick` |
| Carving | 40-tick gaze at 24 blocks at most; opens a 72,000-tick dream window (3 days) | `MoonCarvingBlockEntity` |
| Dream | Bed, full sleep; 200-tick gaze towards the moon; separate dream dimension | `MoonlightDreamType`, `DreamManager` |
| Shadow | 8 nights counted after the dream | `CosmicBodyState` (`MAX_COSMIC_BODY_DAYS = 8`) |
| Ascension | 300 ticks; the Companion loses health (never below 1.5) with bone sounds; reward after 20 ticks | `FriendMoon.specialInteraction`, `completeAscension` |
| Place | Structure `nomansland:meeting_point`, own placement (single instance), biomes `#minecraft:is_overworld` | `MeetingPointStructure` |

### The companions

- **The Companion** (`nomansland:buddy`) is born in mushroom rings, on **mushroom islands** only (`mushroom_fields`). It can follow players, and it is made happy by feeding it. Bringing it to the Meeting Point is a real journey: boat, train or airship.
- **The Frienderman** replaces 1% of the endermen that spawn naturally in the Overworld. Compline removes endermen, so **it will not appear**. Its mask exchange also requires the Vanity mod, which is absent. It is not needed for the quest.

### The config keys

All in `config/nomansland-common.toml` (**common** config, not serverconfig), section `[meeting_point]`:

| Key | Default | Compline | Why |
|---|---|---|---|
| `minMeetingPointDistance` | 1000 | 1000 | Written to be visible. A real expedition, without being out of reach |
| `maxMeetingPointDistance` | 2500 | 2500 | Same |
| `friendMoonDimensions` | `[]` (all dimensions with the Overworld type) | `["minecraft:overworld"]` | The only dimension where Enhanced Celestials has moons. This avoids another dimension counting by mistake |

The rest of the Friendly Moon has no settings: the night window, the 3 days, the 8 nights and the radii are written in the code.

Test commands (permission 2): `/meetingpoint` (where the Point is), `/meetingpoint set <pos>`, `/meetingpoint reset`, `/dream <player> nomansland:friend_moon_dream start` or `clear`.

## Enhanced Celestials 2: how it chooses its moons

Since 2026-10-19, the pack uses **Enhanced Celestials 2**: Core 2.0.3.3 and Default Lunar Events 2.0.1.3, which replace Enhanced Celestials 6.0.2.6 (the two cannot coexist). Read in the jars, not yet seen in game.

- No config file for the moons: everything goes through datapacks, under `data/<ns>/enhancedcelestials2core/lunar/`. An event (`event/`) is only a list of modifiers (`event_modifier/`: colour, size, texture, sound, texts, spawns, sleep…). Its rarity is separate, in `event_probability/`: per dimension, a **weight**, a priority and rules (`min_nights_between`, `valid_moon_phase`).
- Ids: the ordinary moon is `enhancedcelestials2core:default`, the supplied moons are `enhancedcelestials2defaultlunarevents:<id>` (`blood_moon`, `blue_moon`, `harvest_moon`, `super_moon` and their `super_` variants).
- Overworld (`data/minecraft/enhancedcelestials2core/lunar/dimension_settings/overworld.json`): the lunar night starts at 12500 (13000 before) and at least 3 days are needed between two moons (2 before).
- As before, a moon only occurs under a clear sky; Compline is humid, so its moons will be rare.
- Only setting supplied: `config/enhancedcelestials2core/translations.json` set to `DISABLED`. By default, the mod downloads community translations and **replaces** existing texts, including ours (`pack/kubejs/assets/enhancedcelestials2core/`, `…defaultlunarevents/`).

### Active moons in Compline

| Moon | Effect | In Compline |
|---|---|---|
| Harvest moon, super harvest moon | More abundant harvests | Kept |
| Blue moon, super blue moon | Blue colour | Kept |
| Super moon | Bigger moon | Kept |
| Blood moon, super blood moon | More monsters, **sleep forbidden** | **Removed** |
| White moon (Compline draft) | Pale moon, nothing hostile | Absent |

The frequencies simulated for Enhanced Celestials 6 (about 6 nights in 100 for blue and harvest moons) no longer apply: EC2 draws with weights. They need to be measured again with `/ec lunarForecast` on a new world.

The blood moon is removed by two files that empty its dimension list (`"dimensions": {}`): `pack/kubejs/data/enhancedcelestials2defaultlunarevents/enhancedcelestials2core/lunar/event_probability/blood_moon.json` and `super_blood_moon.json`.

### The white moon

The draft `docs/plan/kubejs-brouillons/data/compline/enhancedcelestials/lunar/event/lune_blanche.json` is in the Enhanced Celestials 6 format and no longer loads. To enable it, it must be rewritten in the EC2 format: an event `compline:lune_blanche` (list of modifiers), its modifiers (pale colour, size, name and announcement text, no `minecraft:ambient.cave` loop, which would be unsettling, [music](recherche/2026-10-19/musique.md)), and a probability on the Overworld (full moon, at least 10 nights apart, a weight to tune against the weights of the supplied moons, 5 to 10).

## The chosen coexistence

**The problem.** The Friendly Moon shows itself every night, in one place. An Enhanced Celestials moon falls on a given night, everywhere. The two can therefore fall on the same night. It is not only a matter of image. When the Friendly Moon appears, ATMOSPHERICS hands the sky back to vanilla. Enhanced Celestials then redraws its coloured moon next to the Friendly Moon's face, tints the night and plays its sound loop over the dialogues.

**Config alone: impossible.** NML only sets the dimensions and the distance of the Point. Enhanced Celestials does not know NML and schedules its moons 100 days in advance. No setting links the two.

**KubeJS script: retained.** `lune.js` checks every 2 seconds, in the Overworld:
1. whether an Enhanced Celestials moon is up (almost always no: the check stops there);
2. whether the Friendly Moon is awake, or whether a player is within 64 blocks (flat distance) of the Meeting Point. We do not wait for the awakening, because the sleeping face already appears before it.

If both are true, the script makes the same call as the `/ec setLunarEvent` command towards the ordinary moon (`LunarForecast.setLunarEventTonight`). The night's moon is cut, the rest of the forecast does not change. Enhanced Celestials displays its own ending message (for example "The "Blue Moon" sets..."), and every player in the Overworld reads in their action bar: "The moon grows pale. Another presence occupies the sky tonight." The following moons remain scheduled at their date.

Why the Friendly Moon wins: it is the central mystery, and it only comes if a player makes the journey. The white moon stays rare and returns later. The price: the moon sets **for the whole server**, even for a player who stayed at home. For them, it is a sign that someone is at the Meeting Point.

If a script error occurs (a class renamed after an update), the script stops once and for all and writes it to the log. A Java exception can escape the `catch`: KubeJS then writes it every 2 seconds in `logs/kubejs/server.log`, without crashing the server.

### Proposal for the `compline` mod (not coded, last resort)

If the maintainer prefers the white moon to continue for the other players, the solution goes through the client, in the `ciel` module. Option `ciel.luneAmieSeule`: as long as NML draws the Friendly Moon (`FriendMoonRenderer.getFriendMoonOpacity() > 0`, the value ATMOSPHERICS already reads), the module draws the ordinary Enhanced Celestials moon. It also cuts its night tint and its sound loop, **for that player only**. The server changes nothing and the moon stays up elsewhere. The cost: 1 or 2 more client mixins on `ECWorldRenderer` and `LunarSoundHandler`, to be reviewed at each update of the two mods. The KubeJS script is enough as long as nobody complains.

## Entering the thread through exploration

- **Already in NML.** The heart of each alchemists' ruin leads to a moon sanctuary. Underground exploration therefore leads by itself to the carving, and the "First Contact" achievement follows that of the ruins.
- **Added: a crumpled note.** In the "alchemy" jars of the ruins (`nomansland:chests/alchemist_pot_alchemy`), 4% chance per jar, on top of the normal loot. It is a vanilla sheet of paper with a name and three lines of text: "At the bottom of the ruins, a moon face is carved into the stone. / I looked at it for a long time. The following nights, I dreamt of her. / Since then, a shadow in the sky shows me a path." It says what to do without quite saying it. Jars refill, so a lost note comes back. Texts: keys `compline.lune.*`.
- **Ideas not done, to keep.**
  - *Monoliths*: the dream and the Meeting Point are made of monoliths and menhirs. A page of the Ancient Remnants codex, or a fragment found near a monolith, could speak of a "moon that listens". This is to be linked to the planned work on codex fragments in NML ruins.
  - *Clearings*: a clearing opened at night could let the moon's shadow be seen more clearly. This requires the `compline` mod, so it is not lightweight.

## French texts

- The 7 dialogue pools have a `fr_fr` in the jar: `contextual` 8, `greeting` 17, `leaving` 14, `negative` 26, `offering` 80, `passive` 48, `special` 2. The keys are complete.
- `offering`: Compline already supplies the complete file (the jar was missing 9 lines). It gains the `scute` line. The jar has a dialogue file for scutes (turtle, NML turtle, armadillo), but no language text, even in English. Without a key, the game displayed the English sentence from the data file.
- `negative`: new complete file, copied from the jar, where the only line left in English ("Oopsie.") becomes "Oupsi.".
- The achievements, the blocks (Gravure de la lune, Bassin du clair de lune, Bougie du clair de lune), the Companion and the disc are translated in NML's main `fr_fr`.
- Compline's texts (`compline.lune.efface`, `compline.lune.note*`) go into `pack/kubejs/assets/compline/lang/` (language files merged separately).
- `npm run check-fr` does not read the dialogue pools: to be rechecked at each NML version upgrade.

## Under ATMOSPHERICS

The first in-game test (2026-10-28) found that the Friendly Moon never appeared at the Meeting Point. ATMOSPHERICS replaces the vanilla sky and gives it back to NML only once the Friendly Moon is visible, while NML can only make it visible from inside the vanilla sky. The same lock hid the shadow that shows the way after the dream. The `ciel` module of `compline` 0.4.10 makes NML's call itself after ATMOSPHERICS' sky, which breaks the lock, and leaves the sky to the dream ([mechanism](../../plan/mod-compline.md#the-friendly-moon-under-atmospherics)).

## What remains to be checked in game

| # | Test | Expected |
|---|---|---|
| L1 | Log at startup | No `lune.js` error. Enhanced Celestials lists the possible Overworld moons, without `blood_moon` or `super_blood_moon` |
| L2 | `/ec lunarForecast` on a new world | No blood moon in the forecast. **Done on the test server** |
| L3 | Alchemists' ruin, look at the carving for 2 s | Zoom and sound. Sleeping within the 3 days starts the dream |
| L4 | Dream to the end | Achievement "First Contact". The following night, the shadow in the sky towards the Point (compare with `/meetingpoint`). Before `compline` 0.4.10 the shadow could not show under ATMOSPHERICS (test C14) |
| L5 | Meeting Point, at night, look at the moon | The face appears then wakes, as with NML alone, with ATMOSPHERICS and the `ciel` module (tests C6 and C13 of the [`compline` mod](../../plan/mod-compline.md)). No coloured Enhanced Celestials moon next to it. **Failed on 2026-10-28** (nothing appeared): ATMOSPHERICS prevented NML from ever drawing the Friendly Moon; fixed in `compline` 0.4.10 (checked in a development client with NML and ATMOSPHERICS only), to be tested again in the pack |
| L6 | `/ec setLunarEvent enhancedcelestials2defaultlunarevents:blue_moon`, then go to the Point | Within 2 s at most: Enhanced Celestials message and Compline message, the blue moon fades. `/ec lunarForecast` shows the rest unchanged. The setting itself is validated on the server; the messages and detection of a real player remain |
| L7 | Same with a player far from the Point | Nothing changes for them as long as nobody is at the Point |
| L8 | Offerings: bone, flower, blank map, turtle scute | Lines in French, map of the pool, `scute` line in French |
| L9 | Put out one candle, then all | "Negative" lines in French, then angry moon |
| L10 | Bring a Companion from a mushroom island | Ascension, "Guidance" disc, achievement "Friend of My Friend". Check that it can be transported (lead, boat, train, airship) |
| L11 | Break "alchemy" jars | The crumpled note sometimes drops, name and lines in French |
| L12 | A rainy night at the Point | Does the Friendly Moon appear in the rain? And does the Enhanced Celestials moon that returns when the rain stops set properly? |
| L13 | Real distance of the Point with Compline's generation | Between 1,000 and 2,500 blocks from the centre, on accessible terrain |
| L14 | Frequency of the alchemists' ruins | Enough ruins to find a carving (Sparse Structures could make them rarer, [12-verification](12-verification.md)) |

## Test server (2026-10-19)

These results date from Enhanced Celestials 6.0.2.6; with EC2, redo at least L1, L2, L5 and L6.

Headless NeoForge server, new world (seed "compline"), **without a player**. The Enhanced Celestials commands are written `/ec setLunarEvent …` and `/ec lunarForecast` (or `/enhancedcelestials …`), not `/setLunarEvent` alone.

| Point | Result |
|---|---|
| `lune.js` at load | 6/6 server scripts loaded, 0 errors. The two Java classes (Enhanced Celestials, `FriendMoon`) load |
| `lune.js` in use | Three errors found and fixed: a `const` in the `try` ("redeclaration of var level"); `key == null` on a `ResourceKey`, which throws a NullPointerException outside the `catch`; `ResourceKey.equals()` not found from Rhino. Plus `level.players()`, which KubeJS takes for a property: the script goes through `server.getPlayerList()`. After the fix: no more errors, by day or by night |
| NML config | `config/nomansland-common.toml` read and completed by NeoForge, `friendMoonDimensions = ["minecraft:overworld"]` kept |
| Enhanced Celestials datapack | No error. Log: "Possible lunar events for dimension "minecraft:overworld" are [super_blue_moon, blue_moon, super_moon, super_harvest_moon, harvest_moon]", so no more blood moon. `/ec lunarForecast`: 14 blue or harvest moons in 100 days, no blood moon |
| Meeting Point | `/meetingpoint`: (616, −1288), about 1,430 blocks from the centre |
| Moon setting | Night (`/time set 14000`) and blue moon forced. Without a player, the moon stays up, as expected. With detection forced to true (temporary change, removed afterwards), the script sets it in 2 s at most: log "enhancedcelestials:blue_moon couchée", and the day 0 moon disappears from `/ec lunarForecast` while the following ones remain |
| Crumpled note | 300 draws of `/loot spawn … loot nomansland:chests/alchemist_pot_alchemy`: at least 13 notes (expected ≈ 12). The item carries `custom_name` `{"translate":"compline.lune.note"}` and three translatable grey `lore` lines |

Not verifiable without a player: detection of a player near the Point, the messages (Enhanced Celestials and Compline), the sky rendering, the dream and the dialogues.

## Files

| File | Role |
|---|---|
| `pack/config/nomansland-common.toml` | `friendMoonDimensions` set to the Overworld; Point distances written as is |
| `pack/kubejs/server_scripts/lune.js` | Only one moon at a time; the crumpled note (LootJS) |
| `pack/kubejs/data/enhancedcelestials2defaultlunarevents/enhancedcelestials2core/lunar/event_probability/blood_moon.json`, `super_blood_moon.json` | Blood moons removed from the Overworld |
| `pack/config/enhancedcelestials2core/translations.json` | No download of translations that would overwrite ours |
| `pack/kubejs/assets/nomansland/lang/nomansland/dialogue_pools/offering/fr_fr.json` | `scute` line added |
| `pack/kubejs/assets/nomansland/lang/nomansland/dialogue_pools/negative/fr_fr.json` | Complete pool, "Oopsie." translated |

## Questions for the maintainer

1. The Friendly Moon makes the current moon set **for the whole server**. Is that acceptable, or is the client-side solution of the `compline` mod needed?
2. Should the white moon be enabled now? It is enough to copy the draft into `pack/kubejs/data/compline/` and give it a sound and texts.
3. The Frienderman will not appear, since endermen are removed. Should it be made to appear another way (In Control!), or left aside?
