# Music: True Adaptive Music and the `fallow` pack

> Created on 2026-10-19 from the [music research](../archive/plan/recherche/2026-10-19/musique.md) and [decision 0010](../decisions/0010-musique.md). **Nothing has been heard in game.** The pack format was read in the source code of True Adaptive Music 2.9.3 (GPL-3.0, [repository](https://github.com/LilTOJustice/TrueAdaptiveMusic)); the 29 tracks of that day (25 since 2026-11-13) are those kept by the maintainer after listening on 2026-10-19; the silences are paper choices, to be tuned by ear.

## The principle

Little music, long silences, and tracks that change with the situation. The mist is almost mute, clearings are where music comes back, airship travel has its own tracks. Near an Ancient Remnants monument, a single track on approach, then the Elderheart's drone. During fights against Mowzie's Mobs guardians, the music goes quiet.

## What the pack contains

```
pack/
├── mods/true-adaptive-music.pw.toml           TAM 2.9.3, client and server
├── trueadaptivemusicpacks/fallow/             the pack, as a folder
│   ├── rules.json                             the tree of situations (see below)
│   ├── options.json                           tracks played to the end
│   ├── meta.json                              no bridge mod required
│   └── predicates/*.json                      7 conditions evaluated by the server
├── resourcepacks/supernova-pack.pw.toml       Supernova (bouncytorch), from Modrinth
├── resourcepacks/fallow-ambiance/
│   ├── assets/fallow/sounds.json              one sound event per track
│   ├── assets/fallow/sounds/music/*.ogg       the 10 free tracks (44 MB)
│   └── CREDITS.md                             titles, authors, licences, sources
├── kubejs/server_scripts/musique.js           player tags: monument, moon, aboard
├── kubejs/data/fallow/tags/worldgen/biome/brumeux.json
│                                              the biomes of the "Misty" family
├── kubejs/assets/trueadaptivemusic/lang/fr_fr.json
│                                              TAM's interface in French
└── config/defaultoptions/
    ├── extra/config/trueadaptivemusic/trueadaptivemusic.json
    │                                          `fallow` pack chosen on first launch
    └── options.txt                            music volume at 40%
```

**Why a folder and not a zip.** TAM loads a folder as well as a zip (`MusicPack.fromDirectory`). A folder can be read and compared in git. The built-in editor works on it: "Edit pack" creates a copy `fallow.new`; "Export" turns it into `fallow.zip`. To feed a change back into the repository, copy `rules.json` (and `predicates/`) from `fallow.new` to `pack/trueadaptivemusicpacks/fallow/`, without the zip.

**Where the 25 tracks come from** (29 until 2026-11-13: the four tracks of No Man's Land, `nomad`, `drifter`, `anachronistic` and the `guidance` disc, left with the mod, [0019](../decisions/0019-leaving-no-mans-land.md)). Each one is an event in `sounds.json`:
- 8 are already in the game and are not copied: vanilla (C418, Lena Raine, Kumi Tanioka, Aaron Cherof);
- 3 come from the *Supernova* resource pack (`rose_field`, `supernova`, `cliff_hanger`), downloaded from Modrinth and enabled by Default Options, just below `fallow-ambiance`. Its licence (CC-BY-NC-SA 4.0) allows use in an entirely free pack; we do not copy it;
- 10 are free and shipped in `fallow-ambiance`: 6 by Scott Buckley and 3 by Kevin MacLeod (CC-BY 4.0, licence read on their pages on 2026-10-19), 1 by HoliznaCC0 (CC0). The credits are in `CREDITS.md`, next to the files.

**Preparation of the free tracks.** Official MP3s from the authors (320 kbit/s), converted with ffmpeg: volume brought to −23 LUFS in two passes (linear `loudnorm`, peak at −1.5 dB), 7 s fade-in and 11 s fade-out, OGG Vorbis quality 3 at 44.1 kHz stereo, no metadata or cover art. Reference: C418 tracks measure −27 to −29 LUFS, Lena Raine's *Aerie* −16 LUFS.

## How TAM reads the pack (read from the code)

- Every tick, TAM walks the tree from the root: the **first** child whose predicate is true wins, then it descends to the deepest true child. A node with several predicates is true if any one of them is.
- `trackDelay` and `trackDelayNoise` are in seconds: silence of `trackDelay ± trackDelayNoise` after each track. `enterDelay` waits before the first track of a node, `exitDelay` after leaving it.
- `persistentNodeMusic = true` (`options.json`): a track plays to the end even if the node changes; this avoids cut-offs at borders. `ignorePersistence` reverses this setting for a node: the current track fades out.
- `vanillaMusic`: TAM lets the vanilla music of the place play, hence that of the biomes of the world.
- A track is a sound event (`fallow:music.sweden`) resolved by the client's sound manager. An event declared in a resource pack is enough (`SoundEventSoundInstance`).
- A biome predicate accepts a tag: if the identifier is a registry tag, TAM tests membership (`BiomeIdentifier.matches`). Biome tags are sent to the client, so `fallow:brumeux` works without a server.
- A `custom` predicate reads a file from `predicates/`. The client sends its text to the server, which decodes it as a **vanilla loot condition** (`LootItemCondition.CODEC`) and evaluates it with the player as `this` and their position as origin. TAM is therefore needed on the server. The condition is re-evaluated ten times less often than an ordinary predicate (`tickRate` × 10).

## The tree

The free tracks are long (4 to 10 min): with these silences, you hear about 6 to 7 tracks per hour in a clearing (short tracks), 3 to 4 at night, 2 to 3 in the mist [inferred: average silence + average duration of the node's tracks]. Vanilla plays 3 to 4 per hour.

The selection comes from the listening page (the maintainer's sheet of 2026-10-19). Two choices were made for lack of instructions: the split of the 6 "Seasons" tracks among the four seasons (two per season, *Floating Dream* and *Echo in the Wind* shared), and rain, which no longer has a node of its own (no track was selected for it): under rain, the night or season tracks play.

| Order | Node | Predicate | Tracks | Silence (s) | Other settings |
|---|---|---|---|---|---|
| — | Root | always true | floating_dream, supernova, cicadas, echo_in_the_wind, hiraeth, permafrost | 480 ± 240 | — |
| 1 | Title screen | `title_screen` | vanilla menu music | — | `vanillaMusic` |
| 2 | Monument | `custom` `monument.json` | i_walk_with_ghosts | 1200 ± 300 | `ignorePersistence`: the current track fades out |
| 3 | Boss | `boss` (any boss bar) | none | — | `ignorePersistence`: the Mowzie's theme plays alone |
| 4 | Nether | `dimension` `minecraft:the_nether` | vanilla music | — | `vanillaMusic` |
| 5 | Moon | `custom` `lune.json` | lullabies_3, lightless_dawn | 600 ± 300 | enter 60 s |
| 6 | Aboard | `custom` `a_bord.json` | aerie, featherfall, cliff_hanger | 240 ± 120 | exit 30 s |
| 7 | Underground | `height` below y = 40 | infinite_amethyst | 600 ± 300 | — |
| 8 | Clearing | `biome` `fallow:clairiere`, `_froide`, `_chaude` (the look of a clearing, see below) | sweden, wet_hands, mice_on_venus, key, komorebi, left_to_bloom, rose_field | 300 ± 180 | enter 30 s |
| 8.1 | … at night | `night` | subwoofer_lullaby, the_long_dark, meditation_impromptu_02 | 600 ± 300 | enter 30 s |
| 9 | Mist | `biome` `fallow:brumeux` | hymn_to_the_dawn, long_note_four | 900 ± 300 | enter 120 s |
| 10 | Night | `night` | subwoofer_lullaby, the_long_dark, meditation_impromptu_02 | 720 ± 300 | — |
| 11 | Day | `day` | — | — | `requireChildren`: with no known season, back to the root |
| 11.1 | Spring | `custom` `printemps.json` | floating_dream, supernova | 480 ± 240 | — |
| 11.2 | Summer | `custom` `ete.json` | cicadas, echo_in_the_wind | 480 ± 240 | — |
| 11.3 | Autumn | `custom` `automne.json` | hiraeth, echo_in_the_wind | 480 ± 240 | — |
| 11.4 | Winter | `custom` `hiver.json` | permafrost, floating_dream | 480 ± 240 | — |

Since `fallow` 0.8.0 a clearing no longer changes the biome of the world: the three `fallow:clairiere*` biomes are only its look. The `clairiere` module hands True Adaptive Music the look biome where the player stands in a clearing ([mod-fallow](mod-fallow.md#clairiere-module)), so node 8 is written as before. Without the mod, node 8 stays false, without error.

## The server predicates

| File | Condition | Source of the state |
|---|---|---|
| `monument.json`, `lune.json`, `a_bord.json` | `minecraft:entity_properties`, tag `fallow_monument`, `fallow_lune` or `fallow_a_bord` on the player | `musique.js` |
| `printemps.json`, `ete.json`, `automne.json`, `hiver.json` | `eclipticseasons:season`, `require.season` = `spring`, `summer`, `autumn`, `winter` | Ecliptic Seasons 0.15.4.1 (`SeasonCondition`, `Season` enum serialised in lower case) |

`musique.js` updates the tags every 2 s:
- `fallow_monument`: an Elderheart (`ancient_remnants:elderheart`) within 96 blocks; the current track fades out, *I Walk With Ghosts* plays once, then 15 to 25 minutes of silence leave the Elderheart's drone (audible at 256 blocks);
- `fallow_lune`: the night's lunar event is not the ordinary moon (same calls as Enhanced Celestials in `lune.js`), for Overworld players;
- `fallow_a_bord`: `Sable.HELPER.getTrackingOrVehicleSubLevel(player)` is not null (standing on a contraption or seated in it).

The `fallow:brumeux` tag takes up the 25 biomes of the "Misty" profile of the ATMOSPHERICS preset ([fallow-ambiance](fallow-ambiance.md)): if that profile changes, the tag must follow.

## Interface in French

TAM has no English language file: its texts are fallbacks written in the code. `kubejs/assets/trueadaptivemusic/lang/fr_fr.json` translates the 111 texts with fixed keys (menus, options, error messages). Still in English: the names of predicates, events and node settings in the editor (keys built at runtime), and four messages that insert a number or a path into the fallback text (`warning_count`, `error_count`, `filetype_warning`, `existing_edit`): translating them would lose that number. `check-fr` does not see TAM, for lack of an English file to compare against.

## To test in game

1. On first launch, is the `fallow` pack chosen (Default Options copy before TAM reads its options)? If not, choose it by hand in Options → Music & Sounds → True Adaptive Music, and look for another way.
2. Does the pack load without "Problems detected"? Do the 25 `fallow:music.*` events appear in the editor's track list?
3. Server predicates: is `eclipticseasons:season` accepted (season in lower case)? Are the player's tags read by `entity_properties`? Enable "Debug display" in TAM's options to see the chosen node.
4. `musique.js`: is `fallow_a_bord` set when standing on the deck of an airship in flight? `fallow_lune` during a blue moon (`/setLunarEvent`)?
5. Silences: how many tracks per hour in the mist and in a clearing? Do tracks persist at borders? Does the volume of the free tracks (−23 LUFS) hold up between C418 and Lena Raine?
6. Boss: facing the Wroughtnaut or Frostmaw, does the Mowzie's theme play alone? Monument: does *I Walk With Ghosts* replace the current track on approach, and does the Elderheart's drone stay audible?
7. Is *Supernova* active (Options → Resource Packs)? Without it, its three tracks stay silent.
8. (Removed on 2026-11-13: the No Man's Land `guidance` disc no longer exists.)
9. On macOS (no ffmpeg shipped by TAM): no error message, since everything is ogg?
10. spark: cost of the predicate queries with 4 players, and of the Elderheart search within a 96-block radius.
11. Listening in game: moving a track from one node to another is done in `rules.json` or in TAM's editor.
