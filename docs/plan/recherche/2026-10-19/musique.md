# Musique : moteur et morceaux pour Fallow (2026-10-19)

Mission : L'équipe veut de la musique « dans le style de ce que l'on construit, avec de belles transitions », sans perdre le silence. Deux volets : le **moteur** (quand et comment la musique joue) et la **musique** (les morceaux, et leur licence).

Sources des preuves :
- **[Modrinth]** : API Modrinth, filtre NeoForge 1.21.1, lue le 2026-10-19.
- **[jar X]** : jar téléchargé et ouvert dans le dossier de travail `scratchpad/jars/<mod>/` ; classes lues avec `javap`, JSON lus.
- **[wiki]** : page officielle citée.
- **[déduit]** : raisonnement à partir des preuves, sans test.

## En bref

**Combinaison recommandée** :
- **Moteur** : **True Adaptive Music** (TAM) 2.9.3, côté client et côté serveur.
- **Musique** : un **paquet musical Fallow** (`trueadaptivemusicpacks/fallow.zip`). Il réutilise d'abord des morceaux déjà présents dans le jeu, sans rien redistribuer : les 3 pistes de No Man's Land, une sélection de C418, et l'album *Supernova* depuis Modrinth. Il y ajoute 8 à 12 morceaux libres (CC-BY 4.0 ou CC0) que l'on embarque avec leurs crédits.
- **Liens** : un petit script KubeJS et des prédicats vanilla. Ils relient la musique aux clairières, à la brume, aux saisons, à la lune blanche, aux monuments et aux dirigeables.

**Repli** :
- **Moteur** : la musique vanilla, avec **Music Moods** (0,33 Mo) pour les fondus enchaînés.
- **Contextes** : les fichiers **`background_music` d'Ecliptic Seasons** (saison, moment de la journée, pluie, biome) et le champ `music` du biome `fallow:clairiere`.
- **Musique** : les mêmes morceaux, déclarés dans un pack de ressources.

Ce repli ne sait pas réagir à la lune, aux monuments ni aux dirigeables.

**Reactive Music reste écarté**, mais pour d'autres raisons que le silence de principe. Son silence maximal entre deux morceaux est de 2 minutes, et ce plafond est codé en dur. Son jar pèse 295 Mo, à cause de 61 MP3 celtiques qu'on ne peut pas retirer. Il coupe toute la musique vanilla, donc aussi celle de No Man's Land et d'Ecliptic. Enfin, il ne connaît ni les structures, ni les lunes, ni les saisons (détails en 1.3).

## 1. Ce qui est confirmé

### 1.1 Ce que le pack fait déjà avec la musique (sans mod de musique)

| Élément | Ce qu'il fait | Preuve |
|---|---|---|
| **No Man's Land 1.6.1** | Embarque **3 morceaux originaux** : `nomad` (225 s), `drifter` (145 s) et `anachronistic` (133 s), plus le disque `guidance` (222 s). Ses biomes portent un champ `music`, avec un délai vanilla de 12 000 à 24 000 ticks (10 à 20 min) : `alchemist_ruins` → `nomansland:music.overworld.alchemist_ruins` (`nomad` en poids 25, `replace_current_music: true`) ; `caves` et `cave_depths` → `drifter` ; forêts → `minecraft:music.overworld.forest` ; marais → `swamp`, etc. `boreal_forest`, `prairie`, `maple_grove`, `dream`, `frozen_shore` et `tropical_beach` n'ont pas de musique propre. Licence de NML : *All Rights Reserved*. On peut lire ses morceaux en jeu, pas les copier | [jar NML 1.6.1] `data/nomansland/worldgen/biome/*.json`, `assets/nomansland/sounds.json` ; [Modrinth] licence |
| **Ancient Remnants** | Chaque monument a sa **propre nappe sonore** : `ancient_remnants:ambient.monolith` (2 pistes, 235 s et 76 s, stéréo). Elle est jouée par l'entité **`ancient_remnants:elderheart`** comme son *AMBIENT*, en boucle, avec un volume qui baisse avec la distance jusqu'à **256 blocs**. Le mod n'a pas de musique au sens strict | [jar AR] `ElderheartSoundInstance` (`SoundSource.AMBIENT`, `looping`, 256.0f), `Elderheart` |
| **Enhanced Celestials 6.0.2.6** | Le champ `client_settings.sound_track` d'un événement lunaire joue une **boucle d'ambiance** (`BiomeAmbientSoundsHandler.LoopSoundInstance`, avec fondu d'entrée). Ce n'est pas de la musique : cela passe à côté du `MusicManager` et cohabite donc avec n'importe quel moteur. Boucles livrées : `blue_moon` (44 s), `harvest_moon` (36 s), `blood_moon` (41 s). **Pas de condition de butin** (*loot condition*) pour savoir quelle lune est active | [jar EC] `LunarSoundHandler`, `LunarEventClientSettings`, `assets/enhancedcelestials/sounds.json` |
| **Ecliptic Seasons 0.15.4.1** | A **son propre système de musique saisonnière**. Des fichiers JSON de pack de ressources, `assets/<ns>/eclipticseasons/background_music/*.json`, se règlent avec les champs `start`/`end` (terme solaire), `season`, `special_days`, `day`, `time_period` (DAWN, DAY, DUSK, NIGHT, MIDNIGHT), `rain`, `climate`, `biomes`, `ignored_biomes`, `weight`, `priority`, et un objet `music` vanilla (`sound`, `min_delay`, `max_delay`). Branchement : mixin sur `Minecraft.getSituationalMusic`. Il a aussi une **condition de butin `eclipticseasons:season`** (`require` : `season`, `solar_term`, `start`/`end`, `start_season`/`end_season`, `climate`) | [jar Ecliptic 0.15.4.1] `SeasonalBackgroundMusic`, `SeasonalBackgroundMusicSelectManager`, `MixinMinecraftClient`, `SeasonCondition$Slice`, exemples `resourcepacks/example/.../christmas.json` |
| **Mowzie's Mobs 1.8.2** | Thèmes de boss (Wroughtnaut, Frostmaw, Sculpteur en plusieurs parties, Umvuthi), réglés par l'option client `playBossMusic`. Au début d'un combat, il arrête le `MusicManager` (`stopPlaying`) | [jar Mowzie's] `sounds.json`, `ConfigHandler$Client`, `ClientProxy` |
| **AmbientSounds 6.3.8** | Ne touche pas à la musique. Ses seuls mixins sont `OggAudioStreamMixin` et `SoundBufferLibraryAccessor` | [jar AmbientSounds] `ambientsounds.mixins.json`, classes sans référence à la musique |
| **Sound Physics Remastered** | Laisse la musique tranquille. Un son placé en (0, 0, 0), c'est-à-dire un son relatif comme la musique ou une boucle d'ambiance, reçoit `setDefaultEnvironment`, donc ni écho ni étouffement | [jar SPR] `SoundPhysics.evaluateEnvironment` (test x = y = z = 0) |

Conséquence : sans rien ajouter, le pack joue déjà de la musique vanilla toutes les 10 à 20 minutes, avec trois morceaux de NML. Les monuments ont leur nappe, et les combats de boss Mowzie's ont leur thème. **Le problème n'est pas le silence : il manque des transitions, des contextes et des morceaux choisis.**

### 1.2 Les moteurs étudiés

| Mod | Version 1.21.1 NeoForge | Poids | Dépendances | Ce qu'il sait faire | Verdict |
|---|---|---|---|---|---|
| **True Adaptive Music** (`true-adaptive-music`) | 2.9.3+1.21.1, *release* du 2026-10-02 (13 versions en 1.21.1) | 11,3 Mo | Kotlin for Forge ≥ 5.12 (**déjà dans le pack**, 5.12.0) ; API intégrée | Arbre de nœuds, chacun avec ses prédicats. 42 types de prédicats, dont biome, structure, morceau de structure, heure, jour/nuit, phase de lune, météo, hauteur, combat, boss, monture, effet de statut, équipe, scoreboard, et **prédicat personnalisé = condition de butin vanilla évaluée sur le serveur**. Par nœud : `trackDelay` (secondes), `trackDelayNoise` (± secondes), `enterDelay`, `exitDelay`, `disableFading`, `loopMusic`, `parallelMusic`, `vanillaMusic`, couche `ambience`. Option de paquet `persistentNodeMusic`. Fondu enchaîné de 75 ticks (3,75 s). Formats acceptés : ogg (mp3, flac et wav via ffmpeg) | **Retenu** |
| Music Moods (`music-moods`) | 0.7.0-beta.1, *bêta* du 2026-09-28 | 0,33 Mo | aucune (SpruceUI et MixinSquared intégrés) | Fondu enchaîné de la musique **vanilla** quand une musique de situation la remplace (`fadeInTicks`, `fadeOutTicks`, 30 s par défaut [Modrinth]), musique ininterrompue entre dimensions, pause. Réglages de remplacement *Never/Allow/Always*. Le texte Modrinth déconseille de le faire tourner via Sinytra Connector, mais il existe en NeoForge natif | **Repli** (avec Ecliptic) |
| Reactive Music (`reactive-music`) | 1.3.5+1.21.1-neoforge, *release* du 2026-07-29 | **294,8 Mo** | aucune (YACL n'est plus demandé dans `neoforge.mods.toml`) | Songpacks YAML : événements fixes, plus `biome=`, `biometag=`, `dim=`, `block=` | Écarté (1.3) |
| Music and Melody (`music-and-melody`) | 21.1-r4.3-neoforge, du 2026-09-04 | **246 Mo** (60 ogg) | Cloth Config | Événements JSON de pack de ressources : DIMENSION, BIOME, BIOME_TAG, STRUCTURE, STRUCTURE_TAG, TIME, WEATHER, BOSSBAR, RANDOM_CHANCE, ALL_OF/ANY_OF/NOT… Lecteur de musique, albums, fonctions en ligne. Musiques **ARR**, à ne pas redistribuer | Écarté : trop lourd, pas de lune, de saison ni de prédicat serveur |
| The Immersive Music Mod (`immersivemusicmod`) | 1.1.0, du 2025-11-20 | **155 Mo** (147 pistes, 369 min) | Cloth Config (Mod Menu optionnel) | Listes par biome (vanilla, Terralith, BOP, etc., **aucun biome NML**) et par structure vanilla. `minDelay` 120 s, `maxDelay` 300 s (plafond 600), fondu de 5 s. Surcharge possible par `config/timm/biome_playlists.json` | Écarté comme moteur. Musique de Gold Eagle « entre C418 et les compositeurs récents », mais sous droits |
| Music Triggers (`music-triggers`) | 7.0.4, **alpha** du 2025-05-05 | 31,6 Mo | The Impossible Library ; client **et** serveur | Déclencheurs saison (**Serene Seasons seulement**), lune bleue et lune moisson (Enhanced Celestials), structure, commande, zones, effet… | Écarté : alpha, sans Ecliptic, configuration lourde |
| Mini Music Tweaks, Simple Music Control, Constant Music, Music Delay Reducer, Who Stopped The Music | 1.8.0 / 1.5.5 / 1.0.6 / 2.1.1 / 1.0.0 | < 0,3 Mo | — (SMC : Fzzy Config, déjà là) | Ils **raccourcissent** le silence entre morceaux, ajoutent des notifications « en cours de lecture », des fondus de jukebox | Hors sujet : ils vont contre le silence |
| Music Player (zoma1101), Music Events | 1.1.0 / 1.1.4 | 0,07 / 0,28 Mo | — | Conditions simples (biome, nuit, combat, météo) ; Music Events est ARR, 648 téléchargements | Trop jeunes ou trop pauvres |
| Battle Music | 1.2.2 | 113,7 Mo | — | Musique de combat (Dungeons, Story Mode) | Contraire à la vision |
| Absents en NeoForge 1.21.1 | — | — | — | Biome Music et Music Control (Fabric seulement) ; « Dynamic Music », « Better Music », « Fade Music », « No More Silent Gaps », « Minecraft Music Overhaul », « Soundtrack of the World », « Calm/Relaxed Music » n'existent pas sous ces slugs | [Modrinth] 404 ou Fabric seulement |
| Ambient Environment | 18.0.0.2 | 0,05 Mo | — | **Pas de la musique** : variation de couleur de l'herbe et de l'eau | Hors sujet |

Preuves : [Modrinth] versions, tailles et dépendances (identifiants résolus : `ordsPcFz` = Kotlin for Forge, `9s6osm5g` = Cloth Config, `hYykXjDp` = Fzzy Config, `ZyFWSztU` = The Impossible Library) ; [jar TAM] `MusicTreeNode$Parameters`, `MusicPackOptions`, `MusicManager` (`MAIN_CROSSFADE_TICKS = 75`), dossier `client/trigger/predicate/types`, textes d'aide `trackDelay` (« wait this many seconds ») et `trackDelayNoise` (« randomly + or − this many seconds ») ; [wiki TAM] pages *For Creators* et *Modded Predicates* ; [jar Music Moods] `en_us.json`, `music-moods.mixin.json` ; [jar M&M] `Event$ConditionType`, `LICENSE_Music And Melody` ; [jar TIMM] `Config` (valeurs par défaut), `BiomePlaylist` (`FMLPaths.CONFIGDIR/timm/biome_playlists.json`) ; [jar Music Triggers] `TriggerSeason` (« sereneseasons »), `TriggerBluemoon` (« enhancedcelestials »).

### 1.3 Reactive Music, réexaminé sans a priori

| Point | Constat | Preuve |
|---|---|---|
| Silence entre morceaux | `getMusicDelay` renvoie NONE = 0, SHORT = 250, NORMAL = 900 ou LONG = 2400 ticks : **2 minutes au plus**, sans part de hasard. Fondu de sortie fixe de 150 ticks (7,5 s) | [jar RM] `ReactiveMusic.getMusicDelay`, `FADE_DURATION = 150` |
| Musique livrée | Le songpack intégré *Adventure Redefined* (Vindsvept, Adrian von Ziegler, Mufaya ; « copyright free (non-commercially) ») compte 61 MP3 et **283 Mo** dans le jar. Il est celtique et fantasy, donc loin de Fallow, et on ne peut pas le retirer | [jar RM] `musicpack/ReactiveMusic.yaml`, `musicpack/music/` |
| Conditions | Événements fixes : DAY, NIGHT, SUNRISE, SUNSET, RAIN, SNOW, STORM, UNDERWATER, UNDERGROUND, DEEP_UNDERGROUND, HIGH_UP, MINECART, BOAT, HORSE, FISHING, DYING, BOSS, VILLAGE, NEARBY_MOBS, HOME… Plus `biome=`, `biometag=`, `dim=`, `block=`. **Aucune structure, lune, saison ni condition côté serveur** | [jar RM] `SongpackEventType`, chaînes `biometag=` / `dim=` / `block=` |
| Moteur | Mixin `MusicManager.tick` en HEAD avec annulation : **toute la musique vanilla est coupée**, donc aussi NML, Ecliptic et les boucles de musique de jeu. Lecture par JLayer et JavaSound, hors du moteur sonore du jeu. Au premier lancement, il **baisse le volume général** (« Forcing master volume to a lower default ») | [jar RM] `MusicTrackerMixin`, `PlayerThread`, chaînes de `ReactiveMusic` |

Verdict : la décision du 2026-10-11 tient, mais les raisons changent. Il ne s'agit plus de « AmbientSounds suffit », mais de **délais trop courts, d'un poids énorme et de contextes trop pauvres**. Ce qu'il fait bien (nuit, pluie, bateau, hauteur), TAM le fait aussi, avec des silences réglables.

### 1.4 La musique elle-même

**Licences et modpack.** packwiz ne recopie pas les fichiers Modrinth : le lanceur les télécharge depuis Modrinth. Un pack de ressources Modrinth sous droits peut donc être *référencé*, si son auteur l'accepte. En revanche, tout fichier audio placé dans **notre** dépôt (paquet TAM, pack `fallow-ambiance`) doit être libre : CC0, CC-BY avec crédit, ou une licence qui l'autorise expressément [déduit du fonctionnement de packwiz et des licences].

| Source | Style | Licence | Utilisation possible | Preuve |
|---|---|---|---|---|
| **NML** : `nomad`, `drifter`, `anachronistic`, `guidance` | Celui du trailer, par définition | ARR, déjà installé | **Lire** via ses événements sonores, sans copier | [jar NML] |
| **Vanilla** (C418, Lena Raine, Aaron Cherof, Kumi Tanioka) | La référence demandée | Mojang, déjà dans le client | Nouveaux événements `fallow:*` qui pointent vers les fichiers vanilla (`music/game/sweden`, `wet_hands`, `key`, `oxygene`, `dry_hands`, `mice_on_venus`, `komorebi`, `left_to_bloom`, `featherfall`, `aerie`, `echo_in_the_wind`…), sans redistribuer | [déduit] ; noms de fichiers vus dans les packs listés ci-dessous |
| **Supernova** (`supernova-ost`, Modrinth) | Hommage à C418, 6 pistes : *jang*, *albeit*, *burning konkrete*, *rose field*, *cliff hanger*, *supernova* | CC-BY-NC-SA 4.0 ; « usage dans des packs **entièrement gratuits** » | Référencer depuis Modrinth (35,6 Mo, fichiers `assets/minecraft/sounds/music/game/*.ogg`). Version qui n'écrase pas la musique vanilla | [Modrinth] page et liste du zip |
| Music Redefined (`music-redefined`) | Musique « calme et aérienne » d'UnperishedFoods | ARR, mais « modpacks OK si téléchargé directement depuis Modrinth » | Possible, mais elle **remplace** les fichiers vanilla (`music/game/key.ogg`, etc.), donc elle se mélange à C418 ; 88,8 Mo | [Modrinth] page et liste du zip |
| **Scott Buckley** | Cinématique et ambient : piano, cordes, nappes | **CC-BY 4.0**, crédit obligatoire | Embarquer avec crédit | [wiki] scottbuckley.com.au (licence citée par la recherche web ; site injoignable depuis le conteneur) |
| **HoliznaCC0** (Free Music Archive) | Ambient et lo-fi ; albums *Nature Spirit*, *Lullabies For The End Of The World*… | **CC0 / domaine public** | Embarquer librement (créditer par courtoisie) | [FMA] page de l'artiste : « completely Public Domain » |
| Kevin MacLeod (incompetech) | Très varié, quelques pièces calmes | CC-BY 4.0 (licence « Standard » gratuite avec crédit) | Embarquer avec crédit ; choisir avec soin, ses titres sont très connus | [incompetech] page des licences |
| Komiku (FMA) | Bandes-son de « jeux imaginaires », souvent rétro | « Use it how you want » (CC0) | Seulement les pièces calmes | [FMA] page de l'artiste |
| The Immersive Music Mod (Gold Eagle), Music and Melody, Reactive Music | Proche par endroits (TIMM) | Sous droits ou non commercial | Seulement en installant leur mod, qui est aussi un moteur concurrent | [jars] |
| Evengard (`evengard-original-fantasy-music-resource-pack`) | Fantasy orchestrale | Annoncé CC0, mais le zip (365 Mo) **contient une copie des fichiers vanilla** (`assets/minecraft/minecraft/blockstates/…`) et écrase les noms des pistes vanilla | **Rejeté** : licence douteuse, poids | [Modrinth] liste du zip |
| Cheryltje (*Across The World*), The 5ireMan *Music Reimagined*, Piano Music Overhaul | Ambient et piano à la C418 | CC-BY-NC-ND | Référençables, mais pas modifiables ; 5ireMan ne vise que 1.16 | [Modrinth] |

Kai Engel et Lee Rosevere, souvent cités, n'ont pas pu être vérifiés : la page FMA renvoie 404, et le Bandcamp de Kai Engel affiche « all rights reserved » sur certains titres. **Ne pas les utiliser sans vérifier chaque titre.**

## 2. Les liens à construire

### 2.1 Le moteur : True Adaptive Music, réglé pour le silence

Installation :
- `true-adaptive-music` 2.9.3 côté **client et serveur**. Les prédicats « structure », « scoreboard » et « personnalisé » exigent TAM chez l'hôte. Le mod l'affiche (« Host must have TAM installed! ») et les liste dans `REQUIRES_SERVER_SUPPORT_TRIGGERS` [jar TAM `Constants`].
- Le paquet `trueadaptivemusicpacks/fallow.zip` se place à la racine du jeu, à côté de `resourcepacks/` [wiki *For Users*]. packwiz le livre comme fichier ordinaire du pack.
- Le choix du paquet actif est enregistré dans `config/trueadaptivemusic/` (`OPTIONS_DIR`) [jar TAM]. À livrer aussi, pour que le paquet Fallow soit actif dès le premier lancement (fichier exact à repérer au premier test).
- **Écrire le paquet avec l'éditeur intégré**, puis versionner le zip exporté. Le format interne (`rules.json`, `options.json`, `meta.json`, `predicates/`, `assets/`) est connu par les constantes du jar, mais pas son schéma détaillé [jar TAM `Constants`].

Prédicats personnalisés (dossier `predicates/` du paquet). Le client envoie leur texte ; le serveur le décode avec `LootItemCondition.CODEC` et l'évalue avec `ORIGIN` = position du joueur et `THIS_ENTITY` = joueur [jar TAM, réseau `CustomPredicateQueryPayload`]. Ce sont donc des **conditions de butin vanilla ordinaires** :

```json
// predicates/clairiere.json
{ "condition": "minecraft:location_check", "predicate": { "biomes": "fallow:clairiere" } }
// predicates/brume.json  (tag de datapack à créer, les familles « brumeuses » du preset ATMOSPHERICS)
{ "condition": "minecraft:location_check", "predicate": { "biomes": "#fallow:brumeux" } }
// predicates/hiver.json  (condition d'Ecliptic ; casse de la valeur à vérifier)
{ "condition": "eclipticseasons:season", "require": { "season": "winter" } }
// predicates/a_bord.json, lune_blanche.json, monument.json : tags posés par KubeJS (2.2)
{ "condition": "minecraft:entity_properties", "entity": "this",
  "predicate": { "nbt": "{Tags:[\"fallow_a_bord\"]}" } }
```

Arbre proposé. Le premier nœud vrai l'emporte, puis le moteur descend vers l'enfant le plus profond dont le prédicat est vrai [wiki *For Creators*]. Option de paquet **`persistentNodeMusic` = vrai** : chaque morceau va au bout, ce qui évite les coupures quand on longe une lisière.

| Ordre | Nœud | Prédicat | Musique | Silence entre morceaux (`trackDelay` ± `trackDelayNoise`) | Autres réglages | Raison |
|---|---|---|---|---|---|---|
| 1 | Monument | tag `fallow_monument` | **aucune** | — | `ignorePersistence` | Laisse chanter la nappe de l'Elderheart, audible jusqu'à 256 blocs |
| 2 | Boss | prédicat **Boss** (barre de boss) | **aucune** | — | `ignorePersistence` | Laisse jouer les thèmes de Mowzie's, qui arrêtent seulement le `MusicManager` vanilla |
| 3 | Lune blanche | tag `fallow_lune_blanche` | 1 ou 2 pièces très dépouillées (piano lointain, nappe) | 600 ± 300 s | `enterDelay` 60 s | Une nuit étrange ; la boucle d'Enhanced Celestials fait le reste |
| 4 | À bord | tag `fallow_a_bord` | « Voyage » : Supernova *cliff hanger*, C418 *aerie* / *featherfall*, 2 pièces libres aériennes | 240 ± 120 s | `exitDelay` 30 s | Le voyage en dirigeable est un moment fort |
| 5 | Sous terre | prédicat **Hauteur** < 40, ou nœud `vanillaMusic` | `vanillaMusic` : NML `drifter` et les grottes vanilla | celui de NML | — | Garde la musique de grotte de NML |
| 6 | Clairière (jour / nuit en enfants) | `clairiere.json` | « Foyer » : NML `nomad`, C418 *sweden* / *wet_hands* / *key*, Supernova *rose field*, 3 ou 4 pièces libres de piano et de cordes | jour 300 ± 180 s ; nuit 600 ± 300 s | `enterDelay` 30 s | La récompense : l'endroit où la musique revient |
| 7 | Brume | `brume.json` | Presque rien : NML `anachronistic` et `drifter`, une nappe libre | **900 ± 300 s** | `enterDelay` 120 s | La brume, c'est le silence, puis une note de temps en temps |
| 8 | Pluie | prédicat **Météo** | Pièces douces, ou aucune | 720 ± 300 s | — | AmbientSounds porte la pluie |
| 9 | Nuit | **NightTime** | 3 ou 4 pièces lentes | 720 ± 300 s | — | — |
| 10 | Jour, avec 4 enfants saison | **DayTime** + `printemps.json`, `ete.json`, `automne.json`, `hiver.json` | Une couleur par saison : printemps clair, été chaud, automne mélancolique, hiver piano seul | 480 ± 240 s | — | Relie Ecliptic sans KubeJS |
| — | Racine | toujours vrai | Pool général calme | 480 ± 240 s | — | Valeur par défaut |

Avec ces réglages, on entend en moyenne de 2 à 3 morceaux par heure, et 1 par heure dans la brume [déduit : délai moyen + durée moyenne d'environ 3 min]. Le vanilla en joue 3 à 4 par heure (10 à 20 min de délai).

Le fondu enchaîné de TAM est fixe (3,75 s). Pour des transitions **belles** plutôt que brèves, il faut **cuire les fondus dans les fichiers** : 6 à 8 s d'entrée, 10 à 12 s de sortie. Voir 2.3.

### 2.2 KubeJS : les états que le client ne voit pas

Fichier `kubejs/server_scripts/musique.js`, événement **`PlayerEvents.tick`**, exécuté toutes les 40 ticks. Le script pose ou retire trois tags joueur que lisent les prédicats TAM :

```js
// Fallow : états musicaux (tags joueur) lus par les prédicats du paquet TAM « fallow »
const Sable = Java.loadClass('dev.ryanhcode.sable.Sable')
const EC = Java.loadClass('dev.corgitaco.enhancedcelestials.EnhancedCelestials')
PlayerEvents.tick(event => {
  const p = event.player
  if (p.age % 40 != 0) return
  const tag = (t, on) => { if (on) p.addTag(t); else p.removeTag(t) }
  // À bord d'un engin Sable (debout dessus ou assis)
  tag('fallow_a_bord', Sable.HELPER.getTrackingOrVehicleSubLevel(p) != null)
  // Lune active : objet LunarEvent ; son identifiant exact est à lire au premier test (println)
  const lune = EC.lunarForecastWorldData(p.level).map(d => d.currentLunarEvent()).orElse(null)
  tag('fallow_lune_blanche', lune != null && `${lune}`.includes('lune_blanche'))
  // Près d'un monument : son Elderheart est à moins de 96 blocs
  tag('fallow_monument', p.level.getEntitiesWithin(p.boundingBox.inflate(96))
        .some(e => e.type == 'ancient_remnants:elderheart'))
})
```

Preuves :
- `ActiveSableCompanion.getTrackingOrVehicleSubLevel(Entity)` et `Sable.HELPER` [jar Sable].
- `EnhancedCelestials.lunarForecastWorldData(Level)` et `EnhancedCelestialsLunarForecastWorldData.currentLunarEvent()` [jar EC].
- Identifiant `elderheart` [jar AR, `AncientRemnantsEntityTypes`].
- Méthodes KubeJS `getEntitiesWithin` et `boundingBox` : [déduit], à vérifier.
- Coût : trois tests par joueur toutes les 2 s, négligeable [déduit].

Datapack (`kubejs/data/fallow/tags/worldgen/biome/brumeux.json`) : la liste des biomes de NML de la famille « brumeuse » du preset ATMOSPHERICS (forêts denses, marais, tourbières, taïga, vallées ; plan 10, couche 1).

### 2.3 Le paquet musical Fallow : où trouver les morceaux, comment l'assembler

1. **Sélection** (25 à 30 morceaux au total) :
   - déjà présents : 4 de NML, 8 à 10 de C418 parmi les plus calmes, les 6 de Supernova ;
   - 8 à 12 morceaux libres : Scott Buckley (piano, cordes, nappes) et HoliznaCC0 (*Nature Spirit* et pièces ambient), un ou deux Kevin MacLeod s'il le faut.
   - L'équipe écoute et tranche. Critère : pas de percussion, pas de mélodie héroïque, des silences dans le morceau.
2. **Les morceaux déjà dans le jeu** : un pack de ressources `fallow-ambiance` (déjà prévu) avec un `assets/fallow/sounds.json` qui déclare des événements `fallow:music.foyer`, `fallow:music.voyage`, `fallow:music.brume`… Ils pointent vers `nomansland:music/nomad`, `minecraft:music/game/sweden`, `minecraft:music/game/rose-field` (Supernova), etc., avec `"stream": true`. Aucun fichier audio n'est copié [déduit du format `sounds.json`, vu dans NML et EC].
3. **Les morceaux libres** : dans `assets/` du paquet TAM (ou dans `fallow-ambiance`), avec un `CREDITS.md`. Pour chaque CC-BY : titre, auteur, lien, licence. Conversion avec ffmpeg :
   - OGG Vorbis 44,1 kHz stéréo, qualité 3 (environ 110 kbit/s, 2,5 Mo pour 3 min) ;
   - normalisation autour de −20 LUFS, pour rester au niveau des pistes vanilla ;
   - `afade` d'entrée de 6 à 8 s et de sortie de 10 à 12 s.

   Poids visé : 30 à 40 Mo [déduit].
4. **Boucles d'ambiance** (pas de la musique) : une nappe libre de 40 à 60 s, bouclée sans couture, pour la **lune blanche** (`fallow:ambient.lune_blanche`, à mettre dans `sound_track`).

### 2.4 Les autres réglages

| Quoi | Où | Réglage | Raison |
|---|---|---|---|
| Thèmes de boss | `config/mowziesmobs-client.toml` (nom exact à confirmer) | `playBossMusic = true` | Musique de Rotch Gwylt, juste pour les gardiens ; le nœud TAM « Boss » se tait |
| Volume musique | `options.txt` livré par Default Options | `soundCategory_music` vers 0,4 | La musique reste en retrait des ambiances [déduit] |
| Musique d'Ecliptic | — | Aucun fichier `background_music` côté TAM. Ils ne servent que dans le repli | TAM annule `MusicManager.startPlaying` [jar TAM `MusicManagerMixin`] ; la musique d'Ecliptic ne passe que par les nœuds `vanillaMusic` [déduit] |
| Brouillon de la lune blanche | `lune_blanche.json` | Remplacer `"sound_id": "minecraft:ambient.cave"` par `fallow:ambient.lune_blanche`, ou supprimer `sound_track` | Voir 3 : une boucle de bruits de grotte serait inquiétante |

### 2.5 Le repli (si TAM pose problème)

- **Music Moods** 0.7.0-beta.1 : `fadeInTicks` et `fadeOutTicks` vers 200 (10 s), « Replace current track » sur `Allow`, « Always play music » désactivé.
- **Ecliptic** : quatre fichiers `assets/fallow/eclipticseasons/background_music/<saison>.json` dans `fallow-ambiance` (champs `season`, `time_period`, `rain`, `biomes`, et `music` avec `min_delay` 9600 / `max_delay` 24000).
- **Clairière** : ajouter à `clairiere.json` (datapack) `"music": {"sound": "fallow:music.foyer", "min_delay": 6000, "max_delay": 18000, "replace_current_music": false}`.
- **Musique générale** : `fallow-ambiance` redéfinit `minecraft:music.game` avec la sélection.
- Dans ce mode, ni lune, ni monument, ni dirigeable [déduit].

## 3. Conflits et risques

| Risque | Gravité | Détail | Parade |
|---|---|---|---|
| TAM embarque des exécutables | Moyenne | `ffmpeg`, `ffprobe` et `giga_grabber` (Windows et Linux, aucun pour macOS) dans `assets/trueadaptivemusicextensions/`, environ 20 Mo dans le jar. Ils servent à importer des fichiers non-ogg [jar TAM `ExtensionConstants`]. Un antivirus peut les signaler | Livrer **uniquement de l'ogg**. Vérifier en test qu'aucun binaire n'est extrait sans import |
| Mainteneur unique de TAM (« my first ever mod »), Kotlin | Moyenne | Mises à jour fréquentes (2.9.3 le 2026-10-02) | Le repli 2.5 réutilise les mêmes morceaux |
| Bouton « Get More Packs » de TAM (en ligne) | Faible | Un joueur peut changer de paquet | Choix personnel ; le paquet Fallow est livré actif |
| Prédicats personnalisés évalués par le serveur | Faible | Le client envoie le texte du prédicat, le serveur l'évalue (fréquence inconnue) | Mesurer avec spark ; 5 à 8 prédicats au plus |
| Mowzie's et TAM | Moyenne | Mowzie's n'arrête que le `MusicManager` vanilla. La musique de TAM continuerait par-dessus son thème [déduit] | Nœud « Boss » muet, en 2e position |
| Monument et musique | Faible | La nappe de l'Elderheart (catégorie AMBIENT) se superposerait à un morceau | Nœud « Monument » muet, déclenché à 96 blocs |
| Brouillon EC `sound_track: minecraft:ambient.cave` | Moyenne pour la vision | Le son d'EC est une **boucle** (`LoopSoundInstance`). `ambient.cave` en boucle donnerait des bruits de grotte sans fin pendant la lune blanche [jar EC + déduit] | Nappe Fallow, ou pas de `sound_track` |
| Music Moods (repli) | Faible à moyenne | Bêta ; Ecliptic a aussi un mixin sur `MusicManager` (`nextSongDelay`, `tick`) | Test du repli |
| Supernova sous CC-BY-NC-SA | Faible | Interdit tout usage commercial | Fallow est gratuit ; référencer depuis Modrinth, sans modifier |
| Sound Physics, AmbientSounds | Aucune | Musique et boucles en (0, 0, 0), donc non traitées ; AmbientSounds ne touche pas à la musique | — |
| Jukebox et disques (`guidance` de NML) | Inconnue | TAM a un `SoundManagerMixin` ; on ignore s'il se tait quand un disque joue | Test |

## 4. Ce qu'il faut changer dans le plan

**Ajouter :**
- **True Adaptive Music** 2.9.3 (`true-adaptive-music`, CS ; dépendance Kotlin for Forge déjà là) dans « Ambiance, rendu et son ». Raison : seul moteur qui relie la musique aux mécaniques Fallow, par conditions serveur, avec des silences longs et aléatoires.
- **Paquet `trueadaptivemusicpacks/fallow.zip`** (à écrire) et `kubejs/server_scripts/musique.js`.
- **`supernova-ost`** (pack de ressources, référencé depuis Modrinth, version qui n'écrase pas la musique vanilla) : ⚪ optionnel, après écoute.
- **Une ligne dans `docs/mods.md`** pour le paquet Fallow, avec ses crédits.

**Compléter :**
- **`fallow-ambiance`** : `assets/fallow/sounds.json` (événements `fallow:music.*` qui pointent vers NML, vanilla et Supernova ; `fallow:ambient.lune_blanche`) et `CREDITS.md`.
- **`lune_blanche.json`** : remplacer `minecraft:ambient.cave`.
- **Datapack** : tag `fallow:brumeux`.

**Mettre à jour :**
- **Reactive Music** dans « Sortis ou écartés » : remplacer la raison par « silence de 2 min au plus, 295 Mo de musiques celtiques imposées, coupe la musique de NML et d'Ecliptic, aucune condition de lune, de saison ni de structure (2026-10-19) ».
- **Plan 01, 05, 08 et 09** : ils recommandent encore Reactive Music en phase 6 ; le marquer « remplacé par TAM ».
- **Repli** à noter : Music Moods + Ecliptic `background_music` (2.5).

**Ne pas ajouter** : Music and Melody, The Immersive Music Mod, Music Triggers, Battle Music, Evengard, ni les mods qui raccourcissent le silence.

## 5. À tester en jeu

1. TAM 2.9.3 démarre-t-il avec le pack complet (Sinytra Connector, Kotlin for Forge 5.12.0), sur un client et sur un serveur dédié ?
2. Le prédicat personnalisé `eclipticseasons:season` est-il accepté (valeur `"winter"` ou `"WINTER"`) ? `location_check` sur `fallow:clairiere` réagit-il juste après un `/fillbiome` ?
3. Délais : avec `trackDelay` 900 ± 300 dans la brume, combien de morceaux par heure ? La persistance évite-t-elle les coupures en lisière de clairière ?
4. Nœud « Boss » muet pendant un combat contre Frostmaw ou le Wroughtnaut : le thème de Mowzie's joue-t-il seul ?
5. Nœud « Monument » : le morceau s'efface-t-il quand on approche à 96 blocs, avant que la nappe de l'Elderheart ne domine ?
6. Script KubeJS : `getTrackingOrVehicleSubLevel` vaut-il non-nul debout sur le pont d'un dirigeable en vol ? Que renvoie `${lune}` pour la lune blanche ?
7. Les événements `fallow:music.*` déclarés en pack de ressources apparaissent-ils dans la liste de morceaux de l'éditeur TAM ?
8. Disque `guidance` de NML dans un jukebox : TAM baisse-t-il sa musique ?
9. Sur macOS (pas de ffmpeg livré pour ce système) : aucun message d'erreur si tout est en ogg ?
10. spark : coût des requêtes de prédicats personnalisés avec 4 joueurs.
11. Repli : Music Moods + Ecliptic `background_music`, sans TAM. Le fondu de 10 s fonctionne-t-il, et Ecliptic choisit-il bien la piste d'hiver ?
12. Écoute avec l'équipe de la sélection (Supernova, Scott Buckley, HoliznaCC0) avant de figer le paquet.
