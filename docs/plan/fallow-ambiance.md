# Le pack de ressources `fallow-ambiance`

> Créé le 2026-10-19 d'après le [11.1-11.2](11-rendu-et-confort.md), le [12.1-12.2](12-verification.md) et le [rapport 3](recherche/verif-2026-10-18/3-client.md). **Rien n'a été vu en jeu.** Les formats ont été lus dans les jars installés (ATMOSPHERICS 2.6.5, Polytone 1.21-5.1.3, Particular 1.5.7, No Man's Land 1.5.12, Default Options 21.1.8) ; les valeurs, elles, sont des choix sur le papier, à régler à l'écran.

## Ce qu'il contient

```
pack/resourcepacks/fallow-ambiance/
├── pack.mcmeta                                    pack_format 34 (1.21.1)
└── assets/
    ├── atmospherics/preset/fallow_v1.json          la brume, biome par biome
    ├── fallow/polytone/biome_modifiers/*.json      herbe, feuillage, eau (23 fichiers)
    └── fallow/sounds.json                         évènements des morceaux du paquet musical ([musique](musique.md))
```

Il est activé au premier lancement par Default Options (`pack/config/defaultoptions-common.toml`, en dernier, donc au-dessus des autres packs). Hors du pack, un seul réglage : `pack/config/nomansland-client.toml` coupe le brouillard de No Man's Land (`[fog_modifiers] fogModifiers = false`) pour qu'ATMOSPHERICS soit seul responsable de la brume (L6 du rapport 3). C'est une config de NML, pas d'ATMOSPHERICS : elle ne peut pas aller dans un pack de ressources.

## Comment les mods le lisent (lu dans les jars)

**ATMOSPHERICS.**
- Il liste `assets/atmospherics/preset/**.json` dans les packs actifs.
- Le **premier** de la liste triée est appliqué tout seul, en jeu, sans écran ouvert, une fois par empreinte (la liste des ID de presets).
- Le fichier est une configuration complète : il **remplace** `config/ambientfog/biome_fog.json`, y compris les retouches du joueur.
- Conséquences :
  - un seul preset dans le pack ;
  - pour livrer une nouvelle version, **renommer** le fichier (`fallow_v2.json`) et supprimer l'ancien, sinon il n'est pas réappliqué ;
  - chaque nouvelle version écrase les réglages ATMOSPHERICS des joueurs.
- Le fichier part du preset d'origine du mod (version 43, tous les réglages globaux gardés). Seuls `presetName`, `presetAuthor` et les profils de biomes changent.
- Les bornes du mod sont respectées : intensité 0-4, densité 0,2-5, voile 0-3, particules 0-3.

**Polytone.**
- Il lit `assets/<namespace>/polytone/biome_modifiers/*.json`.
- Les fichiers sont dans l'espace `fallow` avec des `targets` explicites, comme le conseille le mod pour ne pas écraser un autre pack.
- Champs utilisés : `grass_color`, `foliage_color`, `water_color` (chaîne `#rrggbb`).
- Aucun `fog_color`, `sky_color`, `fog_start` ou `fog_end` : ATMOSPHERICS les réécrit après Polytone (rapport 3, 3.3).
- Aucune lightmap : elle couperait More Darkness, la lune d'Enhanced Celestials et la nuit de NML (3.2).
- Les clairières visent `{"id": "fallow:clairiere", "required": false}` : tant que le biome n'existe pas (il n'est qu'en brouillon), Polytone l'ignore sans erreur.

**Particular Reforged : rien à livrer.**
- La correspondance feuille → particule est **écrite dans le code** (`CommonClass.clientSetup`). Elle n'est lue ni dans un pack, ni dans sa config.
- Les blocs inconnus sont reconnus par leur nom :
  - `pine_leaves` de NML prend la particule d'épicéa ;
  - `yellow_birch_leaves` celle du bouleau ;
  - les 9 autres feuillages prennent la feuille de chêne, teintée par la couleur de leur texture.
- Particular a bien des particules de saule et d'érable, mais les relier aux feuilles de NML demande du Java : un appel à `CommonClass.registerLeafData` depuis le mod `fallow` (12.4), ou Falling Leaves Plus en repli (11.2).
- Le L9 du rapport 3 (« config de Particular ») est donc à corriger : cette config n'existe pas.

## Les valeurs choisies

**Trois familles de brume.** Distances en blocs, jour puis nuit. La distance de vue prévue est de 8 chunks, soit 128 blocs (10.12, décision 10).

| Famille | Début → fin (jour) | Début → fin (nuit) | Voile d'horizon | Particules de brume |
|---|---|---|---|---|
| **Brumeux** | 0 → 52 | -2 → 34 | 3 / 3 | fortes (1,6) |
| **Voilé** | 6 → 96 | 0 → 56 | 2,5 / 2,2 | moyennes (1,15) |
| **Clairière** | 28 → 124 | 8 → 84 | 1,5 / 1,2 | coupées |

- Le 10.3 parlait de biomes « dégagés ». L'équipe veut une brume épaisse en général : ils deviennent **voilés**, encore brumeux mais nettement moins.
- Seules les clairières ouvrent la vue. Leur 124 blocs reste sous la distance de vue, pour cacher le bord des chunks.

| Famille | Biomes de No Man's Land | Vanilla (Overworld, en surface) |
|---|---|---|
| Brumeux | `old_growth_forest`, `old_growth_forest_edge`, `autumnal_forest`, `dark_taiga`, `boreal_forest`, `frozen_woods`, `dark_swamp`, `bog`, `bayou`, `blackwater_river` | forêts, taïgas, `dark_forest`, `grove`, marais, jungles, `mushroom_fields` |
| Voilé | `old_growth_forest_clearing` (la clairière naturelle de NML), `maple_forest`, `maple_grove`, `lavender_field`, `prairie`, `frozen_shore`, `tropical_beach`, `desert_river`, `lush_river`, `mud_beach` | plaines, prairie, `flower_forest`, `cherry_grove`, savanes, déserts, badlands, montagnes, neige, plages, rivières, océans |
| Clairière | — | `fallow:clairiere`, `fallow:clairiere_froide`, `fallow:clairiere_chaude` |
| Grotte | `caves`, `cave_depths`, `alchemist_ruins` (brume gris-brun, 72 blocs) | grottes vanilla, Nether, End : preset d'origine |
| Désactivé | `dream` : profil `enabled: false`, NML garde le rendu de son rêve | — |

Les biomes vanilla en surface reçoivent aussi la brume : sans cela, une plaine vanilla ferait une clairière (le preset d'origine ne connaît aucun biome NML et laisse 350 blocs aux plaines).

**Couleurs de brume** (jour / nuit) :
- par climat :
  - tempéré `#a4aea8` / `#161b1f` (gris vert) ;
  - froid `#b9c3cb` / `#171c24` ;
  - marais `#8f9a86` / `#12160f` ;
  - sec `#bdb6a2` / `#1a1a18` ;
  - océan `#9fb0b8` / `#121820` ;
- le ciel du jour reste proche de la brume, pour que l'horizon se dissolve ;
- les clairières passent à une brume claire `#c3d7e2` (froide `#cfdde8`, chaude `#dcd8c4`) et un vrai ciel bleu `#7fa9d6`, avec des nuages blancs. C'est là que la lumière change, faute de lightmap par biome.

**Polytone : herbe, feuillage, eau.**
- Biomes NML de surface :
  - brumeux : saturation × 0,72 et luminosité × 0,95 ;
  - voilés : saturation × 0,85 et luminosité × 0,98 ;
  - eau : saturation × 0,80.
- `dark_swamp` et `blackwater_river` gardent leur herbe : leur modificateur « swamp » ignorerait une couleur fixe.
- Clairières, d'un vert franc :
  - tempérée : herbe `#74c255`, feuillage `#5fae3f`, eau `#3d86c9` ;
  - froide : `#79b878`, `#5f9f62`, `#3f7fc6` ;
  - chaude : `#9cc456`, `#7fae3c`, `#3a92c4`.
- Les biomes vanilla ne sont pas touchés : leur herbe suit la palette de température, qu'une couleur fixe écraserait.

| Biome NML | Famille | Herbe | Feuillage | Eau |
|---|---|---|---|---|
| `autumnal_forest` | brumeux | `#a2a44b` → `#8f9053` | `#bca549` → `#a59553` | `#3b8ab8` → `#4583a6` |
| `bayou` | brumeux | `#67bd39` → `#67a248` | `#69aa2f` → `#65913d` | `#24ab87` → `#30997d` |
| `blackwater_river` | brumeux | inchangée | `#6a7039` → `#5f633d` | `#3a7a6a` → `#3e7064` |
| `bog` | brumeux | `#958455` → `#857959` | `#83774c` → `#756d50` | `#547a5c` → `#55735b` |
| `boreal_forest` | brumeux | `#919d51` → `#838b57` | `#b69b58` → `#a48f5d` | `#2f628f` → `#375e81` |
| `dark_swamp` | brumeux | inchangée | `#638a2c` → `#5c7736` | `#4c7b67` → `#4e7363` |
| `dark_taiga` | brumeux | `#709968` → `#6f8b69` | `#5e8057` → `#5d7458` | `#3b7eb8` → `#4579a6` |
| `frozen_woods` | brumeux | `#acbebf` → `#a5b3b4` | `#acbebf` → `#a5b3b4` | `#4066b0` → `#4966a0` |
| `old_growth_forest` et `_edge` | brumeux | `#87b44c` → `#7e9d56` | `#7d953f` → `#728247` | `#3b8ab8` → `#4583a6` |
| `old_growth_forest_clearing` | voilé | `#87b44c` → `#83a952` | `#7d953f` → `#788c44` | `#3b8ab8` → `#4583a6` |
| `maple_forest` | voilé | `#87ab50` → `#83a155` | `#819e49` → `#7c954e` | `#3b8ab8` → `#4583a6` |
| `maple_grove` | voilé | `#7bba8d` → `#7cb38c` | `#548d6c` → `#57866b` | `#4066b0` → `#4966a0` |
| `lavender_field` | voilé | `#73bf67` → `#74b66a` | `#79ac4f` → `#77a254` | `#3b72b8` → `#4570a6` |
| `prairie` | voilé | `#c0b155` → `#b6a959` | `#a8ab54` → `#9fa159` | `#3b8ab8` → `#4583a6` |
| `frozen_shore` | voilé | `#9bbaa4` → `#99b5a1` | `#548d6c` → `#57866b` | `#4066b0` → `#4966a0` |
| `tropical_beach` | voilé | `#7cbc52` → `#7bb256` | `#72b335` → `#70a63d` | `#3eaacf` → `#47a0be` |
| `desert_river` | voilé | `#a3bd73` → `#9fb575` | `#91aa2f` → `#899e37` | `#3eaacf` → `#47a0be` |
| `lush_river` | voilé | `#67bd39` → `#68b042` | `#5ea723` → `#5d9a2c` | `#35aaa0` → `#3f9a92` |
| `mud_beach` | voilé | `#806a41` → `#796644` | `#6a7039` → `#656a3c` | `#598a98` → `#5c828d` |

**Gardés tels quels** : les réglages globaux du preset d'origine, dont les deux drapeaux Sodium à `true`. Ce sont les valeurs par défaut du mod : il ne retouche donc pas `sodium-options.json`.

## Ce qui reste à régler en jeu

1. **Le rendu des couleurs.** Le preset d'origine utilise des couleurs de brume très saturées (`#20afff` pour les plaines) : ATMOSPHERICS les assombrit ou les mélange sans doute selon l'heure. Les gris choisis ici peuvent sortir trop ternes ou trop clairs [déduit].
   - Régler avec l'éditeur d'ATMOSPHERICS.
   - Exporter, puis enregistrer sous `fallow_v2.json` en supprimant `fallow_v1.json`.
2. **L'épaisseur.** 52 blocs de jour et 34 de nuit en forêt : est-ce encore jouable ? Les monuments et les ruines se voient-ils assez tôt ?
3. **La transition au bord d'une clairière.** La brume change par cellules de 4 blocs, avec un lissage (rapport 3, 1.4 et test T2). Inutile à tester avant que le biome `fallow:clairiere` existe dans `pack/kubejs/data/`.
4. **Le biome des clairières**, quand il sera créé :
   - aligner les couleurs de son JSON (et de ses deux variantes du rapport 4) sur celles de Polytone ;
   - vérifier que Polytone les applique bien à un biome de datapack reçu du serveur.
5. **Ecliptic.** Les saisons teintent-elles encore l'herbe et les feuilles des biomes NML recolorés ? Le rapport 3 le pense, puisqu'on passe par la couleur du biome et non par une colormap de bloc [déduit].
6. **Le rêve de NML** : le profil désactivé rend-il bien la main à NML [déduit du code] ?
7. **Les grottes de NML** : brume gris-brun à 72 blocs, à comparer avec la poussière de Particular.
8. **Polytone 5.x** : `coloredLightsBackend` (fichier `polytone_options.json` à la racine du jeu) reste à trancher avec le test T5. Ce pack ne s'en sert pas.
9. **Les feuilles de NML dans Particular** (test T13) : chêne générique pour 9 feuillages sur 11. Si cela gêne, passer par le mod `fallow` ou par Falling Leaves Plus (voir plus haut).
