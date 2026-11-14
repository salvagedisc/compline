# Music: True Adaptive Music and the `tilth` pack

> Created on 2026-10-19 from the [music research](../archive/plan/recherche/2026-10-19/musique.md) and [decision 0010](../decisions/0010-musique.md). **Nothing has been heard in game.** The pack format was read in the source code of True Adaptive Music 2.9.3 (GPL-3.0, [repository](https://github.com/LilTOJustice/TrueAdaptiveMusic)); the tracks (29 on that day, 30 since 2026-11-14, all from the game) were first chosen by the maintainer after listening on 2026-10-19; the silences are paper choices, to be tuned by ear.

## The principle

Little music, long silences, and tracks that change with the situation. Clearings are where music comes back, airship travel has its own tracks. Near an Ancient Remnants monument, the music goes quiet and leaves the Elderheart's drone. During fights against Mowzie's Mobs guardians, the music goes quiet.

## What the pack contains

```
pack/
├── mods/true-adaptive-music.pw.toml           TAM 2.9.3, client and server
├── trueadaptivemusicpacks/tilth/             the pack, as a folder
│   ├── rules.json                             the tree of situations (see below)
│   ├── options.json                           tracks played to the end
│   ├── meta.json                              no bridge mod required
│   └── predicates/*.json                      6 conditions evaluated by the server
├── kubejs/server_scripts/musique.js           player tags: monument, aboard
├── kubejs/assets/trueadaptivemusic/lang/fr_fr.json
│                                              TAM's interface in French
└── config/defaultoptions/
    ├── extra/config/trueadaptivemusic/trueadaptivemusic.json
    │                                          `tilth` pack chosen on first launch
    └── options.txt                            music volume at 40%
```

**Why a folder and not a zip.** TAM loads a folder as well as a zip (`MusicPack.fromDirectory`). A folder can be read and compared in git. The built-in editor works on it: "Edit pack" creates a copy `tilth.new`; "Export" turns it into `tilth.zip`. To feed a change back into the repository, copy `rules.json` (and `predicates/`) from `tilth.new` to `pack/trueadaptivemusicpacks/tilth/`, without the zip.

**Sound events.** Each track is an event declared in `mods-custom/tilth/src/main/resources/assets/tilth/sounds.json` (in the `tilth` mod, no audio: it points at the game's files). Until 2026-11-13 they were in the `tilth-ambiance` resource pack, which also shipped 10 free tracks (Scott Buckley, Kevin MacLeod, HoliznaCC0); the pack left with them ([0024](../decisions/0024-removing-the-house-resource-packs.md)), and the mist and moon nodes, left without tracks, were removed. On 2026-11-14 the Supernova resource pack left too ([0026](../decisions/0026-second-cleanup.md)): the pack now draws on 30 tracks of the game and no file is added.

**Where the 30 tracks come from** (15 until 2026-11-13; 29 before that, with the four tracks of No Man's Land, which left with the mod, [0019](../decisions/0019-leaving-no-mans-land.md)). All 30 are in the game (C418, Lena Raine, Kumi Tanioka, Aaron Cherof and the other composers of the 1.21 music) and are not copied: each is an event of `sounds.json` that points at `minecraft:music/game/<name>`. No resource pack is needed.

## How TAM reads the pack (read from the code)

- Every tick, TAM walks the tree from the root: the **first** child whose predicate is true wins, then it descends to the deepest true child. A node with several predicates is true if any one of them is.
- `trackDelay` and `trackDelayNoise` are in seconds: silence of `trackDelay ± trackDelayNoise` after each track. `enterDelay` waits before the first track of a node, `exitDelay` after leaving it.
- `persistentNodeMusic = true` (`options.json`): a track plays to the end even if the node changes; this avoids cut-offs at borders. `ignorePersistence` reverses this setting for a node: the current track fades out.
- `vanillaMusic`: TAM lets the vanilla music of the place play, hence that of the biomes of the world.
- A track is a sound event (`tilth:music.sweden`) resolved by the client's sound manager. An event declared in a resource pack or a mod's `sounds.json` is enough (`SoundEventSoundInstance`).
- A biome predicate accepts a tag: if the identifier is a registry tag, TAM tests membership (`BiomeIdentifier.matches`). Biome tags are sent to the client.
- A `custom` predicate reads a file from `predicates/`. The client sends its text to the server, which decodes it as a **vanilla loot condition** (`LootItemCondition.CODEC`) and evaluates it with the player as `this` and their position as origin. TAM is therefore needed on the server. The condition is re-evaluated ten times less often than an ordinary predicate (`tickRate` × 10).

## The tree

With these silences, you hear about 6 to 7 tracks per hour in a clearing (short tracks), 3 to 4 at night [inferred: average silence + average duration of the node's tracks]. Vanilla plays 3 to 4 per hour.

The selection comes from the listening page (the maintainer's sheet of 2026-10-19). Two choices were made for lack of instructions: the split of the "Seasons" tracks among the four seasons (three per season, *Floating Dream* in spring and winter), and rain, which no longer has a node of its own (no track was selected for it): under rain, the night or season tracks play.

| Order | Node | Predicate | Tracks | Silence (s) | Other settings |
|---|---|---|---|---|---|
| — | Root | always true | floating_dream, echo_in_the_wind, comforting_memories, an_ordinary_day | 480 ± 240 | — |
| 1 | Title screen | `title_screen` | vanilla menu music | — | `vanillaMusic` |
| 2 | Monument | `custom` `monument.json` | ancestry, eld_unknown | 1200 ± 300 | `ignorePersistence`: the current track fades out |
| 3 | Boss | `boss` (any boss bar) | none | — | `ignorePersistence`: the Mowzie's theme plays alone |
| 4 | Nether | `dimension` `minecraft:the_nether` | vanilla music | — | `vanillaMusic` |
| 6 | Aboard | `custom` `a_bord.json` | aerie, featherfall, wending, stand_tall | 240 ± 120 | exit 30 s |
| 7 | Underground | `height` below y = 40 | infinite_amethyst, deeper, a_familiar_room | 600 ± 300 | — |
| 8 | Clearing | `biome` `tilth:clairiere`, `_froide`, `_chaude` (the look of a clearing, see below) | sweden, wet_hands, mice_on_venus, key, komorebi, left_to_bloom, bromeliad, yakusoku | 300 ± 180 | enter 30 s |
| 8.1 | … at night | `night` | subwoofer_lullaby, oxygene, dry_hands | 600 ± 300 | enter 30 s |
| 10 | Night | `night` | subwoofer_lullaby, oxygene, labyrinthine, firebugs | 720 ± 300 | — |
| 11 | Day | `day` | — | — | `requireChildren`: with no known season, back to the root |
| 11.1 | Spring | `custom` `printemps.json` | floating_dream, bromeliad, one_more_day | 480 ± 240 | — |
| 11.2 | Summer | `custom` `ete.json` | echo_in_the_wind, crescent_dunes, stand_tall | 480 ± 240 | — |
| 11.3 | Autumn | `custom` `automne.json` | comforting_memories, an_ordinary_day, wending | 480 ± 240 | — |
| 11.4 | Winter | `custom` `hiver.json` | floating_dream, endless, watcher | 480 ± 240 | — |

Since `tilth` 0.8.0 a clearing no longer changes the biome of the world: the three `tilth:clairiere*` biomes are only its look. The `clairiere` module hands True Adaptive Music the look biome where the player stands in a clearing ([mod-tilth](mod-tilth.md#clairiere-module)), so node 8 is written as before. Without the mod, node 8 stays false, without error.

## The server predicates

| File | Condition | Source of the state |
|---|---|---|
| `monument.json`, `a_bord.json` | `minecraft:entity_properties`, tag `tilth_monument` or `tilth_a_bord` on the player | `musique.js` |
| `printemps.json`, `ete.json`, `automne.json`, `hiver.json` | `eclipticseasons:season`, `require.season` = `spring`, `summer`, `autumn`, `winter` | Ecliptic Seasons 0.15.4.1 (`SeasonCondition`, `Season` enum serialised in lower case) |

`musique.js` updates the tags every 2 s:
- `tilth_monument`: an Elderheart (`ancient_remnants:elderheart`) within 96 blocks; the current track fades out and 15 to 25 minutes of silence leave the Elderheart's drone (audible at 256 blocks);
- `tilth_a_bord`: `Sable.HELPER.getTrackingOrVehicleSubLevel(player)` is not null (standing on a contraption or seated in it).

## Interface in French

TAM has no English language file: its texts are fallbacks written in the code. `kubejs/assets/trueadaptivemusic/lang/fr_fr.json` translates the 111 texts with fixed keys (menus, options, error messages). Still in English: the names of predicates, events and node settings in the editor (keys built at runtime), and four messages that insert a number or a path into the fallback text (`warning_count`, `error_count`, `filetype_warning`, `existing_edit`): translating them would lose that number. `check-fr` does not see TAM, for lack of an English file to compare against.

## To test in game

1. On first launch, is the `tilth` pack chosen (Default Options copy before TAM reads its options)? If not, choose it by hand in Options → Music & Sounds → True Adaptive Music, and look for another way.
2. Does the pack load without "Problems detected"? Do the 30 `tilth:music.*` events appear in the editor's track list?
3. Server predicates: is `eclipticseasons:season` accepted (season in lower case)? Are the player's tags read by `entity_properties`? Enable "Debug display" in TAM's options to see the chosen node.
4. `musique.js`: is `tilth_a_bord` set when standing on the deck of an airship in flight?
5. Silences: how many tracks per hour in a clearing and at night? Do tracks persist at borders?
6. Boss: facing the Wroughtnaut or Frostmaw, does the Mowzie's theme play alone? Monument: does the current track fade out on approach, and does the Elderheart's drone stay audible?
7. Does every one of the 30 tracks play, with no silent event? (No resource pack carries tracks any more, [0026](../decisions/0026-second-cleanup.md).)
8. (Removed on 2026-11-13: the No Man's Land `guidance` disc no longer exists.)
9. On macOS (no ffmpeg shipped by TAM): no error message, since everything is ogg?
10. spark: cost of the predicate queries with 4 players, and of the Elderheart search within a 96-block radius.
11. Listening in game: moving a track from one node to another is done in `rules.json` or in TAM's editor.
