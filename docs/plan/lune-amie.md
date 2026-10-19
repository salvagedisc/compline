# La Lune Amie : le mystère central de Fallow

Idée 2 tirée de Wayfarer ([recherche du 2026-10-19](recherche/2026-10-19/wayfarer.md), recommandation 2). No Man's Land (NML 1.6.1) contient déjà une quête : une lune qui parle. Fallow en fait son fil principal, et la fait cohabiter avec les lunes d'Enhanced Celestials, dont la lune blanche. Ce document tranche les points 6 et 18 du [rapport 4](recherche/verif-2026-10-18/4-monde-et-objectifs.md).

> **État au 2026-10-19.** Tout vient de la lecture des jars (NML 1.6.1, Enhanced Celestials 6.0.2.6, ATMOSPHERICS 2.6.5, KubeJS 2101.7.2, LootJS 3.7.0), décompilés avec `javap` et Vineflower. Un premier passage sur le serveur de test sans joueur a validé la partie serveur ([résultats](#serveur-de-test-2026-10-19)). **Rien n'a été testé avec un joueur.** La liste des [tests](#ce-qui-reste-à-vérifier-en-jeu) dit quoi vérifier.

## En bref

- La Lune Amie n'est pas un évènement tiré au hasard. Elle vit dans un lieu, le **Point de rencontre**, et s'y montre **chaque nuit** à qui vient la voir.
- On y arrive par l'exploration : une **gravure de lune** au fond des ruines des alchimistes, puis un **rêve**, puis une **ombre dans le ciel** qui montre le chemin. Aucune coordonnée.
- **Une seule lune à la fois.** Si une lune d'Enhanced Celestials est levée quand la Lune Amie apparaît, cette lune se couche pour la nuit. Script : `pack/kubejs/server_scripts/lune.js`.
- La **lune de sang** et la **super lune de sang** sont retirées, comme le prévoyait le plan. Elles empêchaient de dormir, donc de rêver.
- La **lune blanche** reste rare : environ 3 nuits sur 1 000. Attention : elle n'est **pas encore dans le pack**. Elle n'existe qu'en brouillon (voir [plus bas](#la-lune-blanche)).

## La Lune Amie, pour un joueur

1. **Les ruines.** Les ruines des alchimistes sont sous terre, dans les biomes de grotte, entre les hauteurs 10 et 40. Le cœur de chaque ruine ouvre sur un sanctuaire de la lune, avec une grande gravure en forme de visage de lune. Des gravures plus petites décorent aussi certaines ruines. Une note froissée, trouvée parfois dans les jarres, en parle (ajout Fallow).
2. **La gravure.** Il suffit de regarder la gravure pendant 2 secondes, à moins de 24 blocs. L'image zoome et un son retentit. Rien d'autre ne l'annonce.
3. **Le rêve.** Pendant les 3 jours de jeu suivants (comptés quand il est connecté), le joueur qui dort dans un lit fait un rêve : une allée de monolithes, sans barre d'interface, sans pouvoir courir. Au bout, un bassin. Il faut lever les yeux vers la lune et la regarder 10 secondes. Sortir de l'allée réveille le joueur sans résultat. Succès : « Premier contact ».
4. **L'ombre.** Pendant les 8 nuits qui suivent le rêve, une ombre de lune apparaît dans le ciel, vers le Point de rencontre. Elle s'efface quand on arrive. Après 8 nuits, elle ne revient plus : il faut retourner regarder une gravure, puis rêver de nouveau.
5. **Le Point de rencontre.** Un cercle de menhirs autour d'un bassin et de bougies, à la surface, entre 1 000 et 2 500 blocs du centre du monde. Il y en a **un seul par monde**, partagé par tous les joueurs.
6. **Le réveil.** La nuit, à moins de 16 blocs du bassin, le joueur regarde la lune. Un visage endormi apparaît, puis s'éveille. La nuit s'assombrit, les bougies s'allument et la Lune Amie salue le joueur. Succès : « Amitié ».
7. **Les offrandes.** Un objet jeté dans le bassin, ou un animal posé dessus, monte dans un rayon de lumière. La Lune Amie en parle : 80 répliques, pour des objets (os, fleurs, cloche ancienne, épée du serment…) et des créatures (cerf, oie, tortue, abeille…). Une carte vierge devient une carte du bassin, et une carte déjà remplie y gagne un repère. Ce sont des souvenirs et des confidences, pas des récompenses.
8. **L'ascension.** On amène un **Compagnon** (le petit être champignon de NML) au bassin. La Lune Amie l'élève pendant 15 secondes et il devient une étoile du ciel. Elle laisse alors le disque « Guidance » de Samuel Organ. Succès : « Ami de mon ami ».

Ce qui la contrarie :
- Éteindre les bougies : elle se trouble. Si on les éteint toutes, elle se fâche.
- Le **Mauvais présage**, ou offrir une **babiole** (trinket) de NML : elle se fâche contre ce joueur jusqu'au matin.
- Un juke-box qui joue près du bassin : elle demande de l'arrêter et éjecte le disque.

Ce qu'elle donne : trois succès, le disque « Guidance », une étoile de plus dans le ciel par Compagnon élevé, une carte du bassin et surtout des **dialogues**. Elle ne donne aucun équipement. C'est un mystère à écouter, pas une boutique.

## Comment elle marche (lu dans le jar)

| Élément | Valeur | Classe |
|---|---|---|
| Nuit | De la nuit tombée (temps ≈ 13 900) à l'aube (≈ 22 100) : `getTimeOfDay` entre 0,3 et 0,7. Pas de phase de lune, pas de hasard, pas de condition de météo | `FriendMoon.isNightTime` |
| Présence | Joueur à moins de 16 blocs du bassin (cube), qui regarde la lune | `FriendMoon.appearConditionsMet`, `FriendMoonRenderer` |
| Départ | Plus de 60 ticks hors de portée : réplique d'adieu ; la mort a ses propres répliques | `FriendMoon.tick` |
| Gravure | Regard de 40 ticks à 24 blocs au plus ; ouvre une fenêtre de rêve de 72 000 ticks (3 jours) | `MoonCarvingBlockEntity` |
| Rêve | Lit, sommeil complet ; regard de 200 ticks vers la lune ; dimension de rêve à part | `MoonlightDreamType`, `DreamManager` |
| Ombre | 8 nuits comptées après le rêve | `CosmicBodyState` (`MAX_COSMIC_BODY_DAYS = 8`) |
| Ascension | 300 ticks ; le Compagnon perd de la vie (jamais sous 1,5) avec des bruits d'os ; récompense après 20 ticks | `FriendMoon.specialInteraction`, `completeAscension` |
| Lieu | Structure `nomansland:meeting_point`, placement propre (un seul exemplaire), biomes `#minecraft:is_overworld` | `MeetingPointStructure` |

### Les compagnons

- **Le Compagnon** (`nomansland:buddy`) naît dans des ronds de champignons, sur les **îles aux champignons** seulement (`mushroom_fields`). Il peut suivre les joueurs, et on le rend heureux en le nourrissant. L'amener jusqu'au Point de rencontre est un vrai voyage : bateau, train ou dirigeable.
- **Le Frienderman** remplace 1 % des endermen qui apparaissent naturellement dans l'Overworld. Fallow retire les endermen, donc **il n'apparaîtra pas**. Son échange de masque demande aussi le mod Vanity, absent. Il n'est pas nécessaire à la quête.

### Les clés de config

Toutes dans `config/nomansland-common.toml` (config **commune**, pas de serverconfig), section `[meeting_point]` :

| Clé | Défaut | Fallow | Pourquoi |
|---|---|---|---|
| `minMeetingPointDistance` | 1000 | 1000 | Écrite pour être visible. Une vraie expédition, sans être hors d'atteinte |
| `maxMeetingPointDistance` | 2500 | 2500 | Idem |
| `friendMoonDimensions` | `[]` (toutes les dimensions qui ont le type de l'Overworld) | `["minecraft:overworld"]` | Seule dimension où Enhanced Celestials a des lunes. On évite qu'une autre dimension compte par erreur |

Le reste de la Lune Amie n'a pas de réglage : la fenêtre de nuit, les 3 jours, les 8 nuits et les rayons sont écrits dans le code.

Commandes de test (permission 2) : `/meetingpoint` (où est le Point), `/meetingpoint set <pos>`, `/meetingpoint reset`, `/dream <joueur> nomansland:friend_moon_dream start` ou `clear`.

## Enhanced Celestials : comment il choisit ses lunes

- Il n'a **aucun fichier de config**. Tout passe par des datapacks : les évènements (`data/<ns>/enhancedcelestials/lunar/event/`), les réglages de dimension (`data/minecraft/enhancedcelestials/lunar/dimension_settings/overworld.json`) et des surcharges de probabilité (`lunar/event_probability`).
- Il calcule une **prévision de 100 jours** à l'avance, à partir de la graine du monde. Pour chaque jour, il mélange les évènements, puis il garde le premier qui passe tous les tests : assez de nuits depuis le même évènement, au moins 2 jours depuis n'importe quel évènement, bonne phase de lune, et son tirage de chance.
- S'il n'y a eu aucun évènement depuis 98 jours, il en force un. Une chance à 0 ne suffit donc pas pour retirer une lune : il faut lui retirer sa dimension.
- Les lunes n'ont lieu que par **ciel dégagé** (`requires_clear_skies`). Une nuit de pluie garde la lune ordinaire. Fallow est un monde humide, donc ses lunes seront plus rares que les chiffres ci-dessous.

### Lunes actives dans Fallow

Fréquences simulées sur 2 000 années de 100 nuits, avant l'effet de la pluie.

| Lune | Effet | Avant | Après ce travail |
|---|---|---|---|
| Lune des moissons, super lune des moissons | Récoltes doublées ou quadruplées | ≈ 5,2 et 0,3 nuit / 100 | ≈ 5,9 et 0,4 |
| Lune bleue, super lune bleue | Couleur bleue | ≈ 5,2 et 0,3 | ≈ 5,9 et 0,4 |
| Super lune | Lune deux fois plus grosse | ≈ 0,3 | ≈ 0,4 |
| Lune de sang, super lune de sang | Monstres × 2,25 ou × 4,5, **sommeil interdit** | ≈ 5,2 et 0,3 | **retirées** |
| Lune blanche (brouillon Fallow) | Lune pâle, rien d'hostile, sommeil permis | absente | ≈ 0,3 si on l'ajoute |

La lune de sang est retirée par deux fichiers, copies exactes de ceux du mod avec `"dimension_chances": {}` : `pack/kubejs/data/enhancedcelestials/enhancedcelestials/lunar/event/blood_moon.json` et `super_blood_moon.json`. Un monde déjà créé garde les lunes de sang déjà prévues dans sa prévision.

### La lune blanche

Elle n'existe que dans `docs/plan/kubejs-brouillons/data/fallow/enhancedcelestials/lunar/event/lune_blanche.json`. Pour l'activer, il faut copier ce fichier dans `pack/kubejs/data/fallow/enhancedcelestials/lunar/event/` et retirer sa clé `_brouillon`. Ses réglages de rareté conviennent : chance 0,04, pleine lune seulement (phase 0), au moins 10 nuits d'écart. Cela donne environ une lune blanche toutes les 300 nuits. Deux points restent à corriger dans le brouillon : la boucle sonore `minecraft:ambient.cave` serait angoissante ([musique](recherche/2026-10-19/musique.md)), et elle n'a pas de texte de nom ni d'annonce (`text_components`). Ce dossier `data/fallow/` sort de ce travail.

## La cohabitation choisie

**Le problème.** La Lune Amie se montre chaque nuit, à un endroit. Une lune d'Enhanced Celestials tombe une nuit donnée, partout. Les deux peuvent donc tomber la même nuit. Ce n'est pas qu'une question d'image. Quand la Lune Amie apparaît, ATMOSPHERICS rend la main au ciel vanilla. Enhanced Celestials redessine alors sa lune colorée à côté du visage de la Lune Amie, teinte la nuit et joue sa boucle sonore par-dessus les dialogues.

**Config seule : impossible.** NML ne règle que les dimensions et la distance du Point. Enhanced Celestials ne connaît pas NML et prévoit ses lunes 100 jours à l'avance. Aucun réglage ne relie les deux.

**Script KubeJS : retenu.** `lune.js` vérifie toutes les 2 secondes, dans l'Overworld :
1. si une lune d'Enhanced Celestials est levée (presque toujours non : la vérification s'arrête là) ;
2. si la Lune Amie est éveillée, ou si un joueur se trouve à moins de 64 blocs à plat du Point de rencontre. On n'attend pas l'éveil, car le visage endormi apparaît déjà avant.

Si les deux sont vrais, le script fait le même appel que la commande `/ec setLunarEvent` vers la lune ordinaire. La lune de la nuit est retirée de la prévision. Enhanced Celestials affiche son propre message de fin (par exemple « La "Lune Bleue" se couche... »), et chaque joueur de l'Overworld lit dans sa barre d'action : « La lune pâlit. Une autre présence occupe le ciel cette nuit. » Les lunes suivantes restent prévues à leur date.

Pourquoi la Lune Amie gagne : c'est le mystère central, et elle ne vient que si un joueur fait le voyage. La lune blanche reste rare et revient plus tard. Le prix : la lune se couche **pour tout le serveur**, même pour un joueur resté chez lui. Pour lui, c'est un signe que quelqu'un est au Point de rencontre.

Si une erreur de script survient (une classe renommée après une mise à jour), le script s'arrête une fois pour toutes et l'écrit dans le log. Une exception Java peut échapper au `catch` : KubeJS l'écrit alors toutes les 2 secondes dans `logs/kubejs/server.log`, sans planter le serveur.

### Proposition pour le mod `fallow` (non codée, dernier recours)

Si l'équipe préfère que la lune blanche continue pour les autres joueurs, la solution passe par le client, dans le module `ciel`. Option `ciel.luneAmieSeule` : tant que NML dessine la Lune Amie (`FriendMoonRenderer.getFriendMoonOpacity() > 0`, la valeur qu'ATMOSPHERICS lit déjà), le module dessine la lune ordinaire d'Enhanced Celestials. Il coupe aussi sa teinte de nuit et sa boucle sonore, **chez ce joueur seulement**. Le serveur ne change rien et la lune reste levée ailleurs. Le coût : 1 ou 2 mixins client de plus sur `ECWorldRenderer` et `LunarSoundHandler`, à revoir à chaque mise à jour des deux mods. Le script KubeJS suffit tant que personne ne s'en plaint.

## Entrer dans le fil par l'exploration

- **Déjà dans NML.** Le cœur de chaque ruine des alchimistes mène à un sanctuaire de la lune. L'exploration souterraine mène donc d'elle-même à la gravure, et le succès « Premier contact » suit celui des ruines.
- **Ajouté : une note froissée.** Dans les jarres « alchimie » des ruines (`nomansland:chests/alchemist_pot_alchemy`), 4 % de chance par jarre, en plus du butin normal. C'est une feuille de papier vanilla avec un nom et trois lignes de texte : « Au fond des ruines, un visage de lune est gravé dans la pierre. / Je l'ai regardé longtemps. Les nuits suivantes, j'ai rêvé d'elle. / Depuis, une ombre dans le ciel me montre un chemin. » Elle dit quoi faire sans le dire tout à fait. Les jarres se regarnissent, donc une note perdue revient. Textes : clés `fallow.lune.*`.
- **Idées non faites, à garder.**
  - *Monolithes* : le rêve et le Point de rencontre sont faits de monolithes et de menhirs. Une page du codex d'Ancient Remnants, ou un fragment trouvé près d'un monolithe, pourrait parler d'une « lune qui écoute ». C'est à relier au travail prévu sur les fragments du codex dans les ruines NML.
  - *Clairières* : une clairière ouverte la nuit pourrait laisser voir l'ombre de la lune plus nettement. Cela demande le mod `fallow`, donc ce n'est pas léger.

## Textes en français

- Les 7 pools de dialogues ont un `fr_fr` dans le jar : `contextual` 8, `greeting` 17, `leaving` 14, `negative` 26, `offering` 80, `passive` 48, `special` 2. Les clés sont complètes.
- `offering` : Fallow fournit déjà le fichier complet (il manquait 9 répliques au jar). Il gagne la réplique `scute`. Le jar a un fichier de dialogue pour les écailles (tortue, tortue de NML, tatou), mais aucun texte de langue, même en anglais. Sans clé, le jeu affichait la phrase anglaise du fichier de données.
- `negative` : nouveau fichier complet, copié du jar, où la seule réplique restée en anglais (« Oopsie. ») devient « Oupsi. ».
- Les succès, les blocs (Gravure de la lune, Bassin du clair de lune, Bougie du clair de lune), le Compagnon et le disque sont traduits dans le `fr_fr` principal de NML.
- Les textes de Fallow (`fallow.lune.efface`, `fallow.lune.note*`) vont dans `pack/kubejs/assets/fallow/lang/` (fusion des langues par le coordinateur).
- `npm run check-fr` ne lit pas les pools de dialogues : à recontrôler à chaque montée de version de NML.

## Ce qui reste à vérifier en jeu

| # | Test | Attendu |
|---|---|---|
| L1 | Log au démarrage | Aucune erreur `lune.js`. Enhanced Celestials liste les lunes possibles de l'Overworld, sans `blood_moon` ni `super_blood_moon` |
| L2 | `/ec lunarForecast` sur un monde neuf | Aucune lune de sang dans la prévision. **Fait sur le serveur de test** |
| L3 | Ruine des alchimistes, regarder la gravure 2 s | Zoom et son. Dormir dans les 3 jours lance le rêve |
| L4 | Rêve jusqu'au bout | Succès « Premier contact ». La nuit suivante, l'ombre dans le ciel vers le Point (comparer avec `/meetingpoint`) |
| L5 | Point de rencontre, la nuit, regarder la lune | Le visage apparaît puis s'éveille, comme avec NML seul, avec ATMOSPHERICS et le module `ciel` (test C6 du [mod `fallow`](mod-fallow.md)). Aucune lune colorée d'Enhanced Celestials à côté |
| L6 | `/ec setLunarEvent enhancedcelestials:blue_moon`, puis aller au Point | En 2 s au plus : message d'Enhanced Celestials et message Fallow, la lune bleue s'efface. `/ec lunarForecast` montre la suite inchangée. Le coucher lui-même est validé sur le serveur ; restent les messages et la détection d'un vrai joueur |
| L7 | Même chose avec un joueur loin du Point | Rien ne change pour lui tant que personne n'est au Point |
| L8 | Offrandes : os, fleur, carte vierge, écaille de tortue | Répliques en français, carte du bassin, réplique `scute` en français |
| L9 | Éteindre une bougie, puis toutes | Répliques « négatives » en français, puis lune fâchée |
| L10 | Amener un Compagnon d'une île aux champignons | Ascension, disque « Guidance », succès « Ami de mon ami ». Vérifier qu'on peut le transporter (laisse, bateau, train, dirigeable) |
| L11 | Briser des jarres « alchimie » | La note froissée sort parfois, nom et lignes en français |
| L12 | Une nuit de pluie au Point | La Lune Amie apparaît-elle sous la pluie ? Et la lune d'Enhanced Celestials qui revient quand la pluie cesse se couche-t-elle bien ? |
| L13 | Distance réelle du Point avec la génération de Fallow | Entre 1 000 et 2 500 blocs du centre, sur un terrain accessible |
| L14 | Fréquence des ruines des alchimistes | Assez de ruines pour trouver une gravure (Sparse Structures pourrait les raréfier, [12-verification](12-verification.md)) |

## Serveur de test (2026-10-19)

Serveur NeoForge sans interface, monde neuf (graine « fallow »), **sans joueur**. Les commandes d'Enhanced Celestials s'écrivent `/ec setLunarEvent …` et `/ec lunarForecast` (ou `/enhancedcelestials …`), pas `/setLunarEvent` seul.

| Point | Résultat |
|---|---|
| `lune.js` au chargement | 6/6 scripts serveur chargés, 0 erreur. Les deux classes Java (Enhanced Celestials, `FriendMoon`) se chargent |
| `lune.js` à l'usage | Trois erreurs trouvées et corrigées : un `const` dans le `try` (« redeclaration of var level ») ; `clé == null` sur une `ResourceKey`, qui lève une NullPointerException hors du `catch` ; `ResourceKey.equals()` introuvable depuis Rhino. Plus `level.players()`, que KubeJS prend pour une propriété : le script passe par `server.getPlayerList()`. Après correction : plus aucune erreur, de jour comme de nuit |
| Config NML | `config/nomansland-common.toml` lu et complété par NeoForge, `friendMoonDimensions = ["minecraft:overworld"]` conservé |
| Datapack Enhanced Celestials | Aucune erreur. Log : « Possible lunar events for dimension "minecraft:overworld" are [super_blue_moon, blue_moon, super_moon, super_harvest_moon, harvest_moon] », donc plus de lune de sang. `/ec lunarForecast` : 14 lunes bleues ou des moissons sur 100 jours, aucune lune de sang |
| Point de rencontre | `/meetingpoint` : (616, −1288), soit environ 1 430 blocs du centre |
| Coucher d'une lune | Nuit (`/time set 14000`) et lune bleue forcée. Sans joueur, la lune reste levée, comme prévu. Avec la détection forcée à vrai (modification temporaire, retirée ensuite), le script la couche en 2 s au plus : log « enhancedcelestials:blue_moon couchée », et la lune du jour 0 disparaît de `/ec lunarForecast` alors que les suivantes restent |
| Note froissée | 300 tirages de `/loot spawn … loot nomansland:chests/alchemist_pot_alchemy` : au moins 13 notes (attendu ≈ 12). L'objet porte `custom_name` `{"translate":"fallow.lune.note"}` et trois lignes de `lore` grises traduisibles |

Non vérifiable sans joueur : la détection d'un joueur près du Point, les messages (Enhanced Celestials et Fallow), le rendu du ciel, le rêve et les dialogues.

## Fichiers

| Fichier | Rôle |
|---|---|
| `pack/config/nomansland-common.toml` | `friendMoonDimensions` sur l'Overworld ; distances du Point écrites telles quelles |
| `pack/kubejs/server_scripts/lune.js` | Une seule lune à la fois ; la note froissée (LootJS) |
| `pack/kubejs/data/enhancedcelestials/enhancedcelestials/lunar/event/blood_moon.json`, `super_blood_moon.json` | Lunes de sang retirées de l'Overworld |
| `pack/kubejs/assets/nomansland/lang/nomansland/dialogue_pools/offering/fr_fr.json` | Réplique `scute` ajoutée |
| `pack/kubejs/assets/nomansland/lang/nomansland/dialogue_pools/negative/fr_fr.json` | Pool complet, « Oopsie. » traduit |

## Questions pour l'équipe

1. La Lune Amie fait coucher la lune du moment **pour tout le serveur**. Est-ce que cela vous va, ou faut-il la solution client du mod `fallow` ?
2. Faut-il activer la lune blanche maintenant ? Il suffit de copier le brouillon dans `pack/kubejs/data/fallow/` et de lui donner un son et des textes.
3. Le Frienderman n'apparaîtra pas, puisque les endermen sont retirés. Faut-il le faire apparaître autrement (In Control!), ou le laisser de côté ?
