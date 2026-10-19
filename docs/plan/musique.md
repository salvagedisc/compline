# La musique : True Adaptive Music et le paquet `fallow`

> Créé le 2026-10-19 d'après la [recherche musique](recherche/2026-10-19/musique.md) et la [décision 0010](../decisions/0010-musique.md). **Rien n'a été entendu en jeu.** Le format du paquet a été lu dans le code source de True Adaptive Music 2.9.3 (GPL-3.0, [dépôt](https://github.com/LilTOJustice/TrueAdaptiveMusic)) ; les 29 morceaux sont ceux que l'équipe a gardés à l'écoute le 2026-10-19 ; les silences sont choisis sur le papier, à régler à l'oreille.

## Le principe

Peu de musique, de longs silences, et des morceaux qui changent avec la situation. La brume est presque muette, les clairières sont l'endroit où la musique revient, le voyage en dirigeable a ses morceaux. Près d'un monument d'Ancient Remnants, un seul morceau à l'approche, puis la nappe du Cœur ancien. Pendant les combats contre les gardiens de Mowzie's Mobs, la musique se tait.

## Ce que contient le pack

```
pack/
├── mods/true-adaptive-music.pw.toml           TAM 2.9.3, client et serveur
├── trueadaptivemusicpacks/fallow/             le paquet, sous forme de dossier
│   ├── rules.json                             l'arbre des situations (voir plus bas)
│   ├── options.json                           morceaux joués jusqu'au bout
│   ├── meta.json                              aucun mod-pont requis
│   └── predicates/*.json                      7 conditions évaluées par le serveur
├── resourcepacks/supernova-pack.pw.toml       Supernova (bouncytorch), depuis Modrinth
├── resourcepacks/fallow-ambiance/
│   ├── assets/fallow/sounds.json              un évènement sonore par morceau
│   ├── assets/fallow/sounds/music/*.ogg       les 10 morceaux libres (44 Mo)
│   └── CREDITS.md                             titres, auteurs, licences, sources
├── kubejs/server_scripts/musique.js           tags joueur : monument, lune, à bord
├── kubejs/data/fallow/tags/worldgen/biome/brumeux.json
│                                              les biomes de la famille « Brumeux »
├── kubejs/assets/trueadaptivemusic/lang/fr_fr.json
│                                              l'interface de TAM en français
└── config/defaultoptions/
    ├── extra/config/trueadaptivemusic/trueadaptivemusic.json
    │                                          paquet `fallow` choisi au premier lancement
    └── options.txt                            volume musique à 40 %
```

**Pourquoi un dossier et pas un zip.** TAM charge aussi bien un dossier qu'un zip (`MusicPack.fromDirectory`). Un dossier se lit et se compare dans git. L'éditeur intégré fonctionne dessus : « Modifier le paquet » crée une copie `fallow.new` ; « Exporter » en fait `fallow.zip`. Pour reverser une modification dans le dépôt, il faut recopier `rules.json` (et les `predicates/`) de `fallow.new` vers `pack/trueadaptivemusicpacks/fallow/`, sans le zip.

**D'où viennent les 29 morceaux.** Chacun est un évènement de `sounds.json` :
- 12 sont déjà dans le jeu et ne sont pas copiés : 8 vanilla (C418, Lena Raine, Kumi Tanioka, Aaron Cherof) et 4 de No Man's Land (`nomad`, `drifter`, `anachronistic`, le disque `guidance`) ;
- 3 viennent du pack de ressources *Supernova* (`rose_field`, `supernova`, `cliff_hanger`), téléchargé depuis Modrinth et activé par Default Options, juste sous `fallow-ambiance`. Sa licence (CC-BY-NC-SA 4.0) permet l'usage dans un pack entièrement gratuit ; nous ne le recopions pas ;
- 10 sont libres et livrés dans `fallow-ambiance` : 6 de Scott Buckley et 3 de Kevin MacLeod (CC-BY 4.0, licence lue sur leurs pages le 2026-10-19), 1 de HoliznaCC0 (CC0). Les crédits sont dans `CREDITS.md`, à côté des fichiers.

**Préparation des morceaux libres.** MP3 officiels des auteurs (320 kbit/s), convertis avec ffmpeg : volume ramené à −23 LUFS en deux passes (`loudnorm` linéaire, crête à −1,5 dB), fondu de 7 s à l'entrée et de 11 s à la sortie, OGG Vorbis qualité 3 à 44,1 kHz stéréo, sans métadonnées ni pochette. Repère : les morceaux de C418 mesurent de −27 à −29 LUFS, *Aerie* de Lena Raine −16 LUFS.

## Comment TAM lit le paquet (lu dans le code)

- Chaque tick, TAM parcourt l'arbre depuis la racine : le **premier** enfant dont le prédicat est vrai l'emporte, puis il descend vers l'enfant vrai le plus profond. Un nœud qui a plusieurs prédicats est vrai si l'un d'eux l'est.
- `trackDelay` et `trackDelayNoise` sont en secondes : silence de `trackDelay ± trackDelayNoise` après chaque morceau. `enterDelay` attend avant le premier morceau d'un nœud, `exitDelay` après l'avoir quitté.
- `persistentNodeMusic = true` (`options.json`) : un morceau va au bout même si l'on change de nœud ; cela évite les coupures en lisière. `ignorePersistence` inverse ce réglage pour un nœud : le morceau en cours s'efface.
- `vanillaMusic` : TAM laisse jouer la musique vanilla du lieu, donc celle des biomes de No Man's Land.
- Un morceau est un évènement sonore (`fallow:music.sweden`) résolu par le gestionnaire de sons du client. Un évènement déclaré dans un pack de ressources suffit (`SoundEventSoundInstance`).
- Un prédicat de biome accepte un tag : si l'identifiant est un tag du registre, TAM teste l'appartenance (`BiomeIdentifier.matches`). Les tags de biomes sont envoyés au client, donc `fallow:brumeux` marche sans serveur.
- Un prédicat `custom` lit un fichier de `predicates/`. Le client en envoie le texte au serveur, qui le décode comme une **condition de butin vanilla** (`LootItemCondition.CODEC`) et l'évalue avec le joueur comme `this` et sa position comme origine. Il faut donc TAM sur le serveur. La condition est réévaluée dix fois moins souvent qu'un prédicat ordinaire (`tickRate` × 10).

## L'arbre

Les morceaux libres sont longs (4 à 10 min) : avec ces silences, on entend environ 6 à 7 morceaux par heure en clairière (morceaux courts), 3 à 4 la nuit, 2 à 3 dans la brume [déduit : silence moyen + durée moyenne des morceaux du nœud]. Le vanilla en joue 3 à 4 par heure.

La sélection vient de la page d'écoute (fiche de l'équipe du 2026-10-19). Deux choix sont à moi, faute de consigne : la répartition des 6 morceaux « Saisons » entre les quatre saisons (deux par saison, *Floating Dream* et *Echo in the Wind* partagés), et la pluie, qui n'a plus de nœud propre (aucun morceau retenu pour elle) : sous la pluie jouent la nuit ou la saison.

| Ordre | Nœud | Prédicat | Morceaux | Silence (s) | Autres réglages |
|---|---|---|---|---|---|
| — | Racine | toujours vrai | floating_dream, supernova, cicadas, echo_in_the_wind, hiraeth, permafrost | 480 ± 240 | — |
| 1 | Écran titre | `title_screen` | musique vanilla du menu | — | `vanillaMusic` |
| 2 | Monument | `custom` `monument.json` | i_walk_with_ghosts | 1200 ± 300 | `ignorePersistence` : le morceau en cours s'efface |
| 3 | Boss | `boss` (toute barre de boss) | aucun | — | `ignorePersistence` : le thème de Mowzie's joue seul |
| 4 | Nether | `dimension` `minecraft:the_nether` | musique vanilla | — | `vanillaMusic` |
| 5 | Lune | `custom` `lune.json` | lullabies_3, lightless_dawn | 600 ± 300 | entrée 60 s |
| 6 | À bord | `custom` `a_bord.json` | aerie, featherfall, cliff_hanger | 240 ± 120 | sortie 30 s |
| 7 | Sous terre | `height` sous y = 40 | drifter, infinite_amethyst | 600 ± 300 | — |
| 8 | Clairière | `biome` `fallow:clairiere`, `_froide`, `_chaude` | nomad, sweden, wet_hands, mice_on_venus, key, komorebi, left_to_bloom, rose_field, guidance | 300 ± 180 | entrée 30 s |
| 8.1 | … la nuit | `night` | subwoofer_lullaby, the_long_dark, meditation_impromptu_02 | 600 ± 300 | entrée 30 s |
| 9 | Brume | `biome` `fallow:brumeux` | anachronistic, hymn_to_the_dawn, long_note_four | 900 ± 300 | entrée 120 s |
| 10 | Nuit | `night` | subwoofer_lullaby, the_long_dark, meditation_impromptu_02 | 720 ± 300 | — |
| 11 | Jour | `day` | — | — | `requireChildren` : sans saison connue, retour à la racine |
| 11.1 | Printemps | `custom` `printemps.json` | floating_dream, supernova | 480 ± 240 | — |
| 11.2 | Été | `custom` `ete.json` | cicadas, echo_in_the_wind | 480 ± 240 | — |
| 11.3 | Automne | `custom` `automne.json` | hiraeth, echo_in_the_wind | 480 ± 240 | — |
| 11.4 | Hiver | `custom` `hiver.json` | permafrost, floating_dream | 480 ± 240 | — |

Les clairières (`fallow:clairiere*`) ne sont encore qu'en brouillon ([10](10-recentrage.md)) : tant que ces biomes n'existent pas, le nœud 8 reste faux, sans erreur (un biome absent ne correspond à rien).

## Les prédicats du serveur

| Fichier | Condition | Source de l'état |
|---|---|---|
| `monument.json`, `lune.json`, `a_bord.json` | `minecraft:entity_properties`, tag `fallow_monument`, `fallow_lune` ou `fallow_a_bord` sur le joueur | `musique.js` |
| `printemps.json`, `ete.json`, `automne.json`, `hiver.json` | `eclipticseasons:season`, `require.season` = `spring`, `summer`, `autumn`, `winter` | Ecliptic Seasons 0.15.4.1 (`SeasonCondition`, enum `Season` sérialisé en minuscules) |

`musique.js` met les tags à jour toutes les 2 s :
- `fallow_monument` : un Cœur ancien (`ancient_remnants:elderheart`) à moins de 96 blocs ; le morceau en cours s'efface, *I Walk With Ghosts* joue une fois, puis 15 à 25 minutes de silence laissent la nappe du Cœur ancien (audible à 256 blocs) ;
- `fallow_lune` : l'évènement lunaire de la nuit n'est pas la lune ordinaire (mêmes appels qu'Enhanced Celestials dans `lune.js`), pour les joueurs de l'Overworld ;
- `fallow_a_bord` : `Sable.HELPER.getTrackingOrVehicleSubLevel(joueur)` n'est pas nul (debout sur un engin ou assis dedans).

Le tag `fallow:brumeux` reprend les 25 biomes du profil « Brumeux » du preset ATMOSPHERICS ([fallow-ambiance](fallow-ambiance.md)) : si ce profil change, le tag doit suivre.

## Interface en français

TAM n'a pas de fichier de langue anglais : ses textes sont des replis écrits dans le code. `kubejs/assets/trueadaptivemusic/lang/fr_fr.json` traduit les 111 textes à clé fixe (menus, options, messages d'erreur). Restent en anglais : les noms des prédicats, des évènements et des réglages de nœud dans l'éditeur (clés construites à l'exécution), et quatre messages qui insèrent un nombre ou un chemin dans le texte de repli (`warning_count`, `error_count`, `filetype_warning`, `existing_edit`) : les traduire ferait perdre ce nombre. `check-fr` ne voit pas TAM, faute de fichier anglais à comparer.

## À tester en jeu

1. Au premier lancement, le paquet `fallow` est-il choisi (copie de Default Options avant la lecture des options de TAM) ? Sinon, le choisir à la main dans Options → Musique et sons → True Adaptive Music, et chercher un autre moyen.
2. Le paquet se charge-t-il sans « Problèmes détectés » ? Les 32 évènements `fallow:music.*` apparaissent-ils dans la liste des morceaux de l'éditeur ?
3. Prédicats du serveur : `eclipticseasons:season` est-il accepté (saison en minuscules) ? Les tags du joueur sont-ils lus par `entity_properties` ? Activer « Affichage de débogage » dans les options de TAM pour voir le nœud choisi.
4. `musique.js` : `fallow_a_bord` est-il posé debout sur le pont d'un dirigeable en vol ? `fallow_lune` pendant une lune bleue (`/setLunarEvent`) ?
5. Silences : combien de morceaux par heure dans la brume et en clairière ? Les morceaux persistent-ils en lisière ? Le volume des morceaux libres (−23 LUFS) se tient-il entre C418 et Lena Raine ?
6. Boss : face au Wroughtnaut ou à Frostmaw, le thème de Mowzie's joue-t-il seul ? Monument : *I Walk With Ghosts* remplace-t-il le morceau en cours à l'approche, et la nappe du Cœur ancien reste-t-elle audible ?
7. *Supernova* est-il actif (Options → Packs de ressources) ? Sans lui, ses trois morceaux restent muets.
8. Disque `guidance` de No Man's Land dans un jukebox : TAM baisse-t-il sa musique ?
9. Sur macOS (pas de ffmpeg livré par TAM) : aucun message d'erreur, puisque tout est en ogg ?
10. spark : coût des requêtes de prédicats à 4 joueurs, et de la recherche du Cœur ancien dans un rayon de 96 blocs.
11. Écoute en jeu : déplacer un morceau d'un nœud à l'autre se fait dans `rules.json` ou dans l'éditeur de TAM.
