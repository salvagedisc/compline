# Music: engine and tracks for Tilth (2026-10-19)

Mission: The maintainer wants music "in the style of what we are building, with fine transitions", without losing the silence. Two parts: the **engine** (when and how the music plays) and the **music** (the tracks, and their licence).

Sources of evidence:
- **[Modrinth]**: Modrinth API, NeoForge 1.21.1 filter, read on 2026-10-19.
- **[jar X]**: jar downloaded and opened in the working folder `scratchpad/jars/<mod>/`; classes read with `javap`, JSON read.
- **[wiki]**: official page cited.
- **[inferred]**: reasoning from the evidence, without testing.

## In brief

**Recommended combination**:
- **Engine**: **True Adaptive Music** (TAM) 2.9.3, client and server side.
- **Music**: a **Tilth music pack** (`trueadaptivemusicpacks/tilth.zip`). It first reuses tracks already in the game, without redistributing anything: the 3 No Man's Land tracks, a selection of C418, and the *Supernova* album from Modrinth. It adds 8 to 12 free tracks (CC-BY 4.0 or CC0), shipped with their credits.
- **Links**: a small KubeJS script and vanilla predicates. They tie the music to clearings, mist, seasons, the white moon, monuments and airships.

**Fallback**:
- **Engine**: vanilla music, with **Music Moods** (0.33 MB) for crossfades.
- **Contexts**: the **`background_music` files of Ecliptic Seasons** (season, time of day, rain, biome) and the `music` field of the `tilth:clairiere` biome.
- **Music**: the same tracks, declared in a resource pack.

This fallback cannot react to the moon, monuments or airships.

**Reactive Music stays ruled out**, but for reasons other than the silence in principle. Its maximum silence between two tracks is 2 minutes, and this ceiling is hard-coded. Its jar weighs 295 MB, because of 61 Celtic MP3s that cannot be removed. It cuts all vanilla music, hence also that of No Man's Land and Ecliptic. Finally, it knows neither structures, moons nor seasons (details in 1.3).

## 1. What is confirmed

### 1.1 What the pack already does with music (without a music mod)

| Element | What it does | Evidence |
|---|---|---|
| **No Man's Land 1.6.1** | Ships **3 original tracks**: `nomad` (225 s), `drifter` (145 s) and `anachronistic` (133 s), plus the `guidance` disc (222 s). Its biomes carry a `music` field, with a vanilla delay of 12,000 to 24,000 ticks (10 to 20 min): `alchemist_ruins` → `nomansland:music.overworld.alchemist_ruins` (`nomad` at weight 25, `replace_current_music: true`); `caves` and `cave_depths` → `drifter`; forests → `minecraft:music.overworld.forest`; swamps → `swamp`, etc. `boreal_forest`, `prairie`, `maple_grove`, `dream`, `frozen_shore` and `tropical_beach` have no music of their own. NML licence: *All Rights Reserved*. Its tracks can be played in game, not copied | [jar NML 1.6.1] `data/nomansland/worldgen/biome/*.json`, `assets/nomansland/sounds.json`; [Modrinth] licence |
| **Ancient Remnants** | Each monument has its **own sound bed**: `ancient_remnants:ambient.monolith` (2 tracks, 235 s and 76 s, stereo). It is played by the **`ancient_remnants:elderheart`** entity as its *AMBIENT* sound, looping, with a volume that drops with distance up to **256 blocks**. The mod has no music in the strict sense | [jar AR] `ElderheartSoundInstance` (`SoundSource.AMBIENT`, `looping`, 256.0f), `Elderheart` |
| **Enhanced Celestials 6.0.2.6** | The `client_settings.sound_track` field of a lunar event plays an **ambience loop** (`BiomeAmbientSoundsHandler.LoopSoundInstance`, with fade-in). It is not music: it bypasses the `MusicManager` and therefore coexists with any engine. Loops shipped: `blue_moon` (44 s), `harvest_moon` (36 s), `blood_moon` (41 s). **No loot condition** to know which moon is active | [jar EC] `LunarSoundHandler`, `LunarEventClientSettings`, `assets/enhancedcelestials/sounds.json` |
| **Ecliptic Seasons 0.15.4.1** | Has **its own seasonal music system**. Resource-pack JSON files, `assets/<ns>/eclipticseasons/background_music/*.json`, are set with the fields `start`/`end` (solar term), `season`, `special_days`, `day`, `time_period` (DAWN, DAY, DUSK, NIGHT, MIDNIGHT), `rain`, `climate`, `biomes`, `ignored_biomes`, `weight`, `priority`, and a vanilla `music` object (`sound`, `min_delay`, `max_delay`). Hook: mixin on `Minecraft.getSituationalMusic`. It also has a **loot condition `eclipticseasons:season`** (`require`: `season`, `solar_term`, `start`/`end`, `start_season`/`end_season`, `climate`) | [jar Ecliptic 0.15.4.1] `SeasonalBackgroundMusic`, `SeasonalBackgroundMusicSelectManager`, `MixinMinecraftClient`, `SeasonCondition$Slice`, examples `resourcepacks/example/.../christmas.json` |
| **Mowzie's Mobs 1.8.2** | Boss themes (Wroughtnaut, Frostmaw, multi-part Sculptor, Umvuthi), controlled by the client option `playBossMusic`. At the start of a fight, it stops the `MusicManager` (`stopPlaying`) | [jar Mowzie's] `sounds.json`, `ConfigHandler$Client`, `ClientProxy` |
| **AmbientSounds 6.3.8** | Does not touch music. Its only mixins are `OggAudioStreamMixin` and `SoundBufferLibraryAccessor` | [jar AmbientSounds] `ambientsounds.mixins.json`, classes with no reference to music |
| **Sound Physics Remastered** | Leaves the music alone. A sound placed at (0, 0, 0), i.e. a relative sound such as music or an ambience loop, receives `setDefaultEnvironment`, hence no echo or muffling | [jar SPR] `SoundPhysics.evaluateEnvironment` (test x = y = z = 0) |

Consequence: with nothing added, the pack already plays vanilla music every 10 to 20 minutes, with three NML tracks. Monuments have their sound bed, and Mowzie's boss fights have their theme. **The problem is not silence: transitions, contexts and chosen tracks are missing.**

### 1.2 The engines studied

| Mod | Version 1.21.1 NeoForge | Size | Dependencies | What it can do | Verdict |
|---|---|---|---|---|---|
| **True Adaptive Music** (`true-adaptive-music`) | 2.9.3+1.21.1, *release* of 2026-10-02 (13 versions in 1.21.1) | 11.3 MB | Kotlin for Forge ≥ 5.12 (**already in the pack**, 5.12.0); API built in | Tree of nodes, each with its predicates. 42 predicate types, including biome, structure, structure piece, time, day/night, moon phase, weather, height, combat, boss, mount, status effect, team, scoreboard, and **custom predicate = vanilla loot condition evaluated on the server**. Per node: `trackDelay` (seconds), `trackDelayNoise` (± seconds), `enterDelay`, `exitDelay`, `disableFading`, `loopMusic`, `parallelMusic`, `vanillaMusic`, `ambience` layer. Pack option `persistentNodeMusic`. Crossfade of 75 ticks (3.75 s). Accepted formats: ogg (mp3, flac and wav via ffmpeg) | **Chosen** |
| Music Moods (`music-moods`) | 0.7.0-beta.1, *beta* of 2026-09-28 | 0.33 MB | none (SpruceUI and MixinSquared built in) | Crossfade of **vanilla** music when a situational music replaces it (`fadeInTicks`, `fadeOutTicks`, 30 s by default [Modrinth]), uninterrupted music between dimensions, pause. Replacement settings *Never/Allow/Always*. The Modrinth text advises against running it through Sinytra Connector, but it exists natively for NeoForge | **Fallback** (with Ecliptic) |
| Reactive Music (`reactive-music`) | 1.3.5+1.21.1-neoforge, *release* of 2026-07-29 | **294.8 MB** | none (YACL is no longer requested in `neoforge.mods.toml`) | YAML songpacks: fixed events, plus `biome=`, `biometag=`, `dim=`, `block=` | Ruled out (1.3) |
| Music and Melody (`music-and-melody`) | 21.1-r4.3-neoforge, of 2026-09-04 | **246 MB** (60 ogg) | Cloth Config | Resource-pack JSON events: DIMENSION, BIOME, BIOME_TAG, STRUCTURE, STRUCTURE_TAG, TIME, WEATHER, BOSSBAR, RANDOM_CHANCE, ALL_OF/ANY_OF/NOT… Music player, albums, online features. **ARR** music, not to be redistributed | Ruled out: too heavy, no moon, season or server predicate |
| The Immersive Music Mod (`immersivemusicmod`) | 1.1.0, of 2025-11-20 | **155 MB** (147 tracks, 369 min) | Cloth Config (Mod Menu optional) | Lists per biome (vanilla, Terralith, BOP, etc., **no NML biome**) and per vanilla structure. `minDelay` 120 s, `maxDelay` 300 s (ceiling 600), 5 s fade. Can be overridden through `config/timm/biome_playlists.json` | Ruled out as an engine. Gold Eagle music "between C418 and recent composers", but under copyright |
| Music Triggers (`music-triggers`) | 7.0.4, **alpha** of 2025-05-05 | 31.6 MB | The Impossible Library; client **and** server | Triggers for season (**Serene Seasons only**), blue moon and harvest moon (Enhanced Celestials), structure, command, zones, effect… | Ruled out: alpha, no Ecliptic, heavy configuration |
| Mini Music Tweaks, Simple Music Control, Constant Music, Music Delay Reducer, Who Stopped The Music | 1.8.0 / 1.5.5 / 1.0.6 / 2.1.1 / 1.0.0 | < 0.3 MB | — (SMC: Fzzy Config, already there) | They **shorten** the silence between tracks, add "now playing" notifications, jukebox fades | Off topic: they go against the silence |
| Music Player (zoma1101), Music Events | 1.1.0 / 1.1.4 | 0.07 / 0.28 MB | — | Simple conditions (biome, night, combat, weather); Music Events is ARR, 648 downloads | Too young or too poor |
| Battle Music | 1.2.2 | 113.7 MB | — | Combat music (Dungeons, Story Mode) | Contrary to the vision |
| Absent on NeoForge 1.21.1 | — | — | — | Biome Music and Music Control (Fabric only); "Dynamic Music", "Better Music", "Fade Music", "No More Silent Gaps", "Minecraft Music Overhaul", "Soundtrack of the World", "Calm/Relaxed Music" do not exist under these slugs | [Modrinth] 404 or Fabric only |
| Ambient Environment | 18.0.0.2 | 0.05 MB | — | **Not music**: variation in grass and water colour | Off topic |

Evidence: [Modrinth] versions, sizes and dependencies (resolved identifiers: `ordsPcFz` = Kotlin for Forge, `9s6osm5g` = Cloth Config, `hYykXjDp` = Fzzy Config, `ZyFWSztU` = The Impossible Library); [jar TAM] `MusicTreeNode$Parameters`, `MusicPackOptions`, `MusicManager` (`MAIN_CROSSFADE_TICKS = 75`), folder `client/trigger/predicate/types`, help texts `trackDelay` ("wait this many seconds") and `trackDelayNoise` ("randomly + or − this many seconds"); [wiki TAM] pages *For Creators* and *Modded Predicates*; [jar Music Moods] `en_us.json`, `music-moods.mixin.json`; [jar M&M] `Event$ConditionType`, `LICENSE_Music And Melody`; [jar TIMM] `Config` (default values), `BiomePlaylist` (`FMLPaths.CONFIGDIR/timm/biome_playlists.json`); [jar Music Triggers] `TriggerSeason` ("sereneseasons"), `TriggerBluemoon` ("enhancedcelestials").

### 1.3 Reactive Music, reexamined without prejudice

| Point | Finding | Evidence |
|---|---|---|
| Silence between tracks | `getMusicDelay` returns NONE = 0, SHORT = 250, NORMAL = 900 or LONG = 2400 ticks: **2 minutes at most**, with no random share. Fixed fade-out of 150 ticks (7.5 s) | [jar RM] `ReactiveMusic.getMusicDelay`, `FADE_DURATION = 150` |
| Music shipped | The built-in songpack *Adventure Redefined* (Vindsvept, Adrian von Ziegler, Mufaya; "copyright free (non-commercially)") has 61 MP3s and **283 MB** in the jar. It is Celtic and fantasy, hence far from Tilth, and cannot be removed | [jar RM] `musicpack/ReactiveMusic.yaml`, `musicpack/music/` |
| Conditions | Fixed events: DAY, NIGHT, SUNRISE, SUNSET, RAIN, SNOW, STORM, UNDERWATER, UNDERGROUND, DEEP_UNDERGROUND, HIGH_UP, MINECART, BOAT, HORSE, FISHING, DYING, BOSS, VILLAGE, NEARBY_MOBS, HOME… Plus `biome=`, `biometag=`, `dim=`, `block=`. **No structure, moon, season or server-side condition** | [jar RM] `SongpackEventType`, strings `biometag=` / `dim=` / `block=` |
| Engine | Mixin `MusicManager.tick` at HEAD with cancellation: **all vanilla music is cut**, hence also NML, Ecliptic and the game's music loops. Playback through JLayer and JavaSound, outside the game's sound engine. On first launch, it **lowers the master volume** ("Forcing master volume to a lower default") | [jar RM] `MusicTrackerMixin`, `PlayerThread`, strings of `ReactiveMusic` |

Verdict: the decision of 2026-10-11 stands, but the reasons change. It is no longer "AmbientSounds is enough", but **delays too short, an enormous size and contexts too poor**. What it does well (night, rain, boat, height), TAM also does, with adjustable silences.

### 1.4 The music itself

**Licences and modpack.** packwiz does not copy Modrinth files: the launcher downloads them from Modrinth. A copyrighted Modrinth resource pack can therefore be *referenced*, if its author accepts. On the other hand, any audio file placed in **our** repository (TAM pack, `tilth-ambiance` pack) must be free: CC0, CC-BY with credit, or a licence that expressly allows it [inferred from how packwiz and the licences work].

| Source | Style | Licence | Possible use | Evidence |
|---|---|---|---|---|
| **NML**: `nomad`, `drifter`, `anachronistic`, `guidance` | That of the trailer, by definition | ARR, already installed | **Play** via its sound events, without copying | [jar NML] |
| **Vanilla** (C418, Lena Raine, Aaron Cherof, Kumi Tanioka) | The requested reference | Mojang, already in the client | New `tilth:*` events pointing to the vanilla files (`music/game/sweden`, `wet_hands`, `key`, `oxygene`, `dry_hands`, `mice_on_venus`, `komorebi`, `left_to_bloom`, `featherfall`, `aerie`, `echo_in_the_wind`…), without redistributing | [inferred]; file names seen in the packs listed below |
| **Supernova** (`supernova-ost`, Modrinth) | Tribute to C418, 6 tracks: *jang*, *albeit*, *burning konkrete*, *rose field*, *cliff hanger*, *supernova* | CC-BY-NC-SA 4.0; "use in **entirely free** packs" | Reference from Modrinth (35.6 MB, files `assets/minecraft/sounds/music/game/*.ogg`). Version that does not overwrite vanilla music | [Modrinth] page and zip listing |
| Music Redefined (`music-redefined`) | "Calm and airy" music by UnperishedFoods | ARR, but "modpacks OK if downloaded directly from Modrinth" | Possible, but it **replaces** the vanilla files (`music/game/key.ogg`, etc.), so it mixes with C418; 88.8 MB | [Modrinth] page and zip listing |
| **Scott Buckley** | Cinematic and ambient: piano, strings, pads | **CC-BY 4.0**, credit required | Ship with credit | [wiki] scottbuckley.com.au (licence cited by the web search; site unreachable from the container) |
| **HoliznaCC0** (Free Music Archive) | Ambient and lo-fi; albums *Nature Spirit*, *Lullabies For The End Of The World*… | **CC0 / public domain** | Ship freely (credit as a courtesy) | [FMA] artist page: "completely Public Domain" |
| Kevin MacLeod (incompetech) | Very varied, a few calm pieces | CC-BY 4.0 (free "Standard" licence with credit) | Ship with credit; choose with care, his titles are very well known | [incompetech] licence page |
| Komiku (FMA) | Soundtracks for "imaginary games", often retro | "Use it how you want" (CC0) | Only the calm pieces | [FMA] artist page |
| The Immersive Music Mod (Gold Eagle), Music and Melody, Reactive Music | Close in places (TIMM) | Copyrighted or non-commercial | Only by installing their mod, which is also a competing engine | [jars] |
| Evengard (`evengard-original-fantasy-music-resource-pack`) | Orchestral fantasy | Announced as CC0, but the zip (365 MB) **contains a copy of the vanilla files** (`assets/minecraft/minecraft/blockstates/…`) and overwrites the vanilla track names | **Rejected**: doubtful licence, size | [Modrinth] zip listing |
| Cheryltje (*Across The World*), The 5ireMan *Music Reimagined*, Piano Music Overhaul | Ambient and piano in the C418 manner | CC-BY-NC-ND | Can be referenced, but not modified; 5ireMan only targets 1.16 | [Modrinth] |

Kai Engel and Lee Rosevere, often cited, could not be verified: the FMA page returns 404, and Kai Engel's Bandcamp shows "all rights reserved" on some titles. **Do not use them without checking each title.**

## 2. The links to build

### 2.1 The engine: True Adaptive Music, tuned for silence

Installation:
- `true-adaptive-music` 2.9.3 on **client and server**. The "structure", "scoreboard" and "custom" predicates require TAM on the host. The mod displays this ("Host must have TAM installed!") and lists them in `REQUIRES_SERVER_SUPPORT_TRIGGERS` [jar TAM `Constants`].
- The `trueadaptivemusicpacks/tilth.zip` pack is placed at the game root, next to `resourcepacks/` [wiki *For Users*]. packwiz ships it as an ordinary pack file.
- The choice of the active pack is saved in `config/trueadaptivemusic/` (`OPTIONS_DIR`) [jar TAM]. To be shipped too, so that the Tilth pack is active from the first launch (exact file to be identified at the first test).
- **Write the pack with the built-in editor**, then version the exported zip. The internal format (`rules.json`, `options.json`, `meta.json`, `predicates/`, `assets/`) is known from the jar's constants, but not its detailed schema [jar TAM `Constants`].

Custom predicates (`predicates/` folder of the pack). The client sends their text; the server decodes it with `LootItemCondition.CODEC` and evaluates it with `ORIGIN` = the player's position and `THIS_ENTITY` = the player [jar TAM, network `CustomPredicateQueryPayload`]. They are therefore **ordinary vanilla loot conditions**:

```json
// predicates/clairiere.json
{ "condition": "minecraft:location_check", "predicate": { "biomes": "tilth:clairiere" } }
// predicates/brume.json  (datapack tag to create, the "misty" families of the ATMOSPHERICS preset)
{ "condition": "minecraft:location_check", "predicate": { "biomes": "#tilth:brumeux" } }
// predicates/hiver.json  (Ecliptic condition; case of the value to be checked)
{ "condition": "eclipticseasons:season", "require": { "season": "winter" } }
// predicates/a_bord.json, lune_blanche.json, monument.json: tags set by KubeJS (2.2)
{ "condition": "minecraft:entity_properties", "entity": "this",
  "predicate": { "nbt": "{Tags:[\"tilth_a_bord\"]}" } }
```

Proposed tree. The first true node wins, then the engine descends to the deepest child whose predicate is true [wiki *For Creators*]. Pack option **`persistentNodeMusic` = true**: each track plays to the end, which avoids cut-offs when walking along an edge.

| Order | Node | Predicate | Music | Silence between tracks (`trackDelay` ± `trackDelayNoise`) | Other settings | Reason |
|---|---|---|---|---|---|---|
| 1 | Monument | tag `tilth_monument` | **none** | — | `ignorePersistence` | Lets the Elderheart's sound bed sing, audible up to 256 blocks |
| 2 | Boss | **Boss** predicate (boss bar) | **none** | — | `ignorePersistence` | Lets Mowzie's themes play, which only stop the vanilla `MusicManager` |
| 3 | White moon | tag `tilth_lune_blanche` | 1 or 2 very bare pieces (distant piano, pad) | 600 ± 300 s | `enterDelay` 60 s | A strange night; the Enhanced Celestials loop does the rest |
| 4 | Aboard | tag `tilth_a_bord` | "Journey": Supernova *cliff hanger*, C418 *aerie* / *featherfall*, 2 airy free pieces | 240 ± 120 s | `exitDelay` 30 s | Airship travel is a highlight |
| 5 | Underground | **Height** predicate < 40, or `vanillaMusic` node | `vanillaMusic`: NML `drifter` and the vanilla caves | NML's own | — | Keeps NML's cave music |
| 6 | Clearing (day / night as children) | `clairiere.json` | "Hearth": NML `nomad`, C418 *sweden* / *wet_hands* / *key*, Supernova *rose field*, 3 or 4 free piano and string pieces | day 300 ± 180 s; night 600 ± 300 s | `enterDelay` 30 s | The reward: the place where music comes back |
| 7 | Mist | `brume.json` | Almost nothing: NML `anachronistic` and `drifter`, a free pad | **900 ± 300 s** | `enterDelay` 120 s | The mist is silence, then an occasional note |
| 8 | Rain | **Weather** predicate | Soft pieces, or none | 720 ± 300 s | — | AmbientSounds carries the rain |
| 9 | Night | **NightTime** | 3 or 4 slow pieces | 720 ± 300 s | — | — |
| 10 | Day, with 4 season children | **DayTime** + `printemps.json`, `ete.json`, `automne.json`, `hiver.json` | One colour per season: bright spring, warm summer, melancholic autumn, solo piano winter | 480 ± 240 s | — | Ties in Ecliptic without KubeJS |
| — | Root | always true | General calm pool | 480 ± 240 s | — | Default value |

With these settings, one hears 2 to 3 tracks per hour on average, and 1 per hour in the mist [inferred: average delay + average duration of about 3 min]. Vanilla plays 3 to 4 per hour (10 to 20 min delay).

TAM's crossfade is fixed (3.75 s). For **beautiful** rather than brief transitions, the **fades must be baked into the files**: 6 to 8 s in, 10 to 12 s out. See 2.3.

### 2.2 KubeJS: the states the client does not see

File `kubejs/server_scripts/musique.js`, **`PlayerEvents.tick`** event, run every 40 ticks. The script sets or removes three player tags that the TAM predicates read:

```js
// Tilth: music states (player tags) read by the predicates of the "tilth" TAM pack
const Sable = Java.loadClass('dev.ryanhcode.sable.Sable')
const EC = Java.loadClass('dev.corgitaco.enhancedcelestials.EnhancedCelestials')
PlayerEvents.tick(event => {
  const p = event.player
  if (p.age % 40 != 0) return
  const tag = (t, on) => { if (on) p.addTag(t); else p.removeTag(t) }
  // Aboard a Sable contraption (standing on it or seated)
  tag('tilth_a_bord', Sable.HELPER.getTrackingOrVehicleSubLevel(p) != null)
  // Active moon: LunarEvent object; its exact identifier is to be read at the first test (println)
  const lune = EC.lunarForecastWorldData(p.level).map(d => d.currentLunarEvent()).orElse(null)
  tag('tilth_lune_blanche', lune != null && `${lune}`.includes('lune_blanche'))
  // Near a monument: its Elderheart is within 96 blocks
  tag('tilth_monument', p.level.getEntitiesWithin(p.boundingBox.inflate(96))
        .some(e => e.type == 'ancient_remnants:elderheart'))
})
```

Evidence:
- `ActiveSableCompanion.getTrackingOrVehicleSubLevel(Entity)` and `Sable.HELPER` [jar Sable].
- `EnhancedCelestials.lunarForecastWorldData(Level)` and `EnhancedCelestialsLunarForecastWorldData.currentLunarEvent()` [jar EC].
- Identifier `elderheart` [jar AR, `AncientRemnantsEntityTypes`].
- KubeJS methods `getEntitiesWithin` and `boundingBox`: [inferred], to be checked.
- Cost: three tests per player every 2 s, negligible [inferred].

Datapack (`kubejs/data/tilth/tags/worldgen/biome/brumeux.json`): the list of NML biomes of the "misty" family of the ATMOSPHERICS preset (dense forests, swamps, bogs, taiga, valleys; plan 10, layer 1).

### 2.3 The Tilth music pack: where to find the tracks, how to assemble it

1. **Selection** (25 to 30 tracks in total):
   - already present: 4 from NML, 8 to 10 from C418 among the calmest, the 6 from Supernova;
   - 8 to 12 free tracks: Scott Buckley (piano, strings, pads) and HoliznaCC0 (*Nature Spirit* and ambient pieces), one or two Kevin MacLeod if needed.
   - The maintainer listens and decides. Criterion: no percussion, no heroic melody, silences within the track.
2. **Tracks already in the game**: a `tilth-ambiance` resource pack (already planned) with an `assets/tilth/sounds.json` that declares `tilth:music.foyer`, `tilth:music.voyage`, `tilth:music.brume`… events. They point to `nomansland:music/nomad`, `minecraft:music/game/sweden`, `minecraft:music/game/rose-field` (Supernova), etc., with `"stream": true`. No audio file is copied [inferred from the `sounds.json` format, seen in NML and EC].
3. **Free tracks**: in the TAM pack's `assets/` (or in `tilth-ambiance`), with a `CREDITS.md`. For each CC-BY: title, author, link, licence. Conversion with ffmpeg:
   - OGG Vorbis 44.1 kHz stereo, quality 3 (about 110 kbit/s, 2.5 MB for 3 min);
   - normalisation around −20 LUFS, to stay at the level of the vanilla tracks;
   - `afade` in of 6 to 8 s and out of 10 to 12 s.

   Target size: 30 to 40 MB [inferred].
4. **Ambience loops** (not music): a free pad of 40 to 60 s, looped seamlessly, for the **white moon** (`tilth:ambient.lune_blanche`, to go in `sound_track`).

### 2.4 The other settings

| What | Where | Setting | Reason |
|---|---|---|---|
| Boss themes | `config/mowziesmobs-client.toml` (exact name to be confirmed) | `playBossMusic = true` | Rotch Gwylt music, just for the guardians; the TAM "Boss" node stays silent |
| Music volume | `options.txt` shipped by Default Options | `soundCategory_music` around 0.4 | Music stays behind the ambiences [inferred] |
| Ecliptic's music | — | No `background_music` file on the TAM side. They only serve in the fallback | TAM cancels `MusicManager.startPlaying` [jar TAM `MusicManagerMixin`]; Ecliptic's music only passes through `vanillaMusic` nodes [inferred] |
| White moon draft | `lune_blanche.json` | Replace `"sound_id": "minecraft:ambient.cave"` with `tilth:ambient.lune_blanche`, or remove `sound_track` | See 3: a loop of cave noises would be unsettling |

### 2.5 The fallback (if TAM causes problems)

- **Music Moods** 0.7.0-beta.1: `fadeInTicks` and `fadeOutTicks` around 200 (10 s), "Replace current track" on `Allow`, "Always play music" disabled.
- **Ecliptic**: four files `assets/tilth/eclipticseasons/background_music/<season>.json` in `tilth-ambiance` (fields `season`, `time_period`, `rain`, `biomes`, and `music` with `min_delay` 9600 / `max_delay` 24000).
- **Clearing**: add to `clairiere.json` (datapack) `"music": {"sound": "tilth:music.foyer", "min_delay": 6000, "max_delay": 18000, "replace_current_music": false}`.
- **General music**: `tilth-ambiance` redefines `minecraft:music.game` with the selection.
- In this mode, no moon, no monument, no airship [inferred].

## 3. Conflicts and risks

| Risk | Severity | Detail | Countermeasure |
|---|---|---|---|
| TAM ships executables | Medium | `ffmpeg`, `ffprobe` and `giga_grabber` (Windows and Linux, none for macOS) in `assets/trueadaptivemusicextensions/`, about 20 MB in the jar. They serve to import non-ogg files [jar TAM `ExtensionConstants`]. An antivirus may flag them | Ship **ogg only**. Check in testing that no binary is extracted without an import |
| Single maintainer of TAM ("my first ever mod"), Kotlin | Medium | Frequent updates (2.9.3 on 2026-10-02) | The fallback in 2.5 reuses the same tracks |
| TAM's "Get More Packs" button (online) | Low | A player can change pack | Personal choice; the Tilth pack is shipped active |
| Custom predicates evaluated by the server | Low | The client sends the predicate text, the server evaluates it (frequency unknown) | Measure with spark; 5 to 8 predicates at most |
| Mowzie's and TAM | Medium | Mowzie's only stops the vanilla `MusicManager`. TAM's music would carry on over its theme [inferred] | Silent "Boss" node, in 2nd position |
| Monument and music | Low | The Elderheart's sound bed (AMBIENT category) would overlap a track | Silent "Monument" node, triggered at 96 blocks |
| EC draft `sound_track: minecraft:ambient.cave` | Medium for the vision | EC's sound is a **loop** (`LoopSoundInstance`). `ambient.cave` on a loop would give endless cave noises during the white moon [jar EC + inferred] | Tilth pad, or no `sound_track` |
| Music Moods (fallback) | Low to medium | Beta; Ecliptic also has a mixin on `MusicManager` (`nextSongDelay`, `tick`) | Test the fallback |
| Supernova under CC-BY-NC-SA | Low | Forbids any commercial use | Tilth is free; reference from Modrinth, without modifying |
| Sound Physics, AmbientSounds | None | Music and loops at (0, 0, 0), hence not processed; AmbientSounds does not touch music | — |
| Jukebox and discs (NML `guidance`) | Unknown | TAM has a `SoundManagerMixin`; it is not known whether it goes quiet when a disc plays | Test |

## 4. What to change in the plan

**Add:**
- **True Adaptive Music** 2.9.3 (`true-adaptive-music`, CS; Kotlin for Forge dependency already there) under "Ambience, rendering and sound". Reason: the only engine that ties music to Tilth's mechanics, through server conditions, with long and random silences.
- **`trueadaptivemusicpacks/tilth.zip` pack** (to be written) and `kubejs/server_scripts/musique.js`.
- **`supernova-ost`** (resource pack, referenced from Modrinth, version that does not overwrite vanilla music): ⚪ optional, after listening.
- **A line in `docs/mods.md`** for the Tilth pack, with its credits.

**Complete:**
- **`tilth-ambiance`**: `assets/tilth/sounds.json` (`tilth:music.*` events pointing to NML, vanilla and Supernova; `tilth:ambient.lune_blanche`) and `CREDITS.md`.
- **`lune_blanche.json`**: replace `minecraft:ambient.cave`.
- **Datapack**: tag `tilth:brumeux`.

**Update:**
- **Reactive Music** under "Removed or ruled out": replace the reason with "silence of 2 min at most, 295 MB of imposed Celtic music, cuts NML's and Ecliptic's music, no moon, season or structure condition (2026-10-19)".
- **Plans 01, 05, 08 and 09**: they still recommend Reactive Music in phase 6; mark it "replaced by TAM".
- **Fallback** to note: Music Moods + Ecliptic `background_music` (2.5).

**Do not add**: Music and Melody, The Immersive Music Mod, Music Triggers, Battle Music, Evengard, nor the mods that shorten the silence.

## 5. To test in game

1. Does TAM 2.9.3 start with the full pack (Sinytra Connector, Kotlin for Forge 5.12.0), on a client and on a dedicated server?
2. Is the custom predicate `eclipticseasons:season` accepted (value `"winter"` or `"WINTER"`)? Does `location_check` on `tilth:clairiere` react right after a `/fillbiome`?
3. Delays: with `trackDelay` 900 ± 300 in the mist, how many tracks per hour? Does persistence avoid cut-offs at the edge of a clearing?
4. Silent "Boss" node during a fight against Frostmaw or the Wroughtnaut: does the Mowzie's theme play alone?
5. "Monument" node: does the track fade out when approaching to 96 blocks, before the Elderheart's sound bed dominates?
6. KubeJS script: is `getTrackingOrVehicleSubLevel` non-null when standing on the deck of an airship in flight? What does `${lune}` return for the white moon?
7. Do the `tilth:music.*` events declared in a resource pack appear in the track list of the TAM editor?
8. NML `guidance` disc in a jukebox: does TAM lower its music?
9. On macOS (no ffmpeg shipped for this system): no error message if everything is ogg?
10. spark: cost of the custom predicate queries with 4 players.
11. Fallback: Music Moods + Ecliptic `background_music`, without TAM. Does the 10 s fade work, and does Ecliptic pick the winter track correctly?
12. Listening to the selection (Supernova, Scott Buckley, HoliznaCC0) with the maintainer before freezing the pack.
