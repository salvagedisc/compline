# 12. Vérification complète du 2026-10-18

> Demandée par l'équipe : tout revoir pour adapter, modifier, supprimer ou ajouter, en cherchant à fond **comment relier les mods**, surtout Overgeared et Kaleidoscope. Si les scripts ne suffisent pas, on peut écrire des mods custom.
>
> Ce chapitre **corrige les chapitres 10 et 11** là où ils se contredisent. Les preuves (jars ouverts, versions Modrinth, tickets) sont dans les cinq rapports de `recherche/verif-2026-10-18/` :
> [1 Overgeared](recherche/verif-2026-10-18/1-overgeared.md) · [2 Kaleidoscope, saisons, chaleur, soif](recherche/verif-2026-10-18/2-kaleidoscope.md) · [3 rendu, caméra, interface](recherche/verif-2026-10-18/3-client.md) · [4 monde, faune, objectifs](recherche/verif-2026-10-18/4-monde-et-objectifs.md) · [5 Create, construction, scripts](recherche/verif-2026-10-18/5-create-construction.md)
>
> Rien n'a été testé en jeu : chaque rapport finit par sa liste de tests (77 au total).

## 12.1 Ce que la vérification change

### Ce qui sort

| Mod ou fonction | Pourquoi | Preuve |
|---|---|---|
| **Freeze It and Heat It** | Retiré par l'équipe le 2026-10-18 : pas de conservation de la nourriture | — |
| **Easy Anvils** | Il remplace l'enclume vanilla par la sienne, or Overgeared ne transforme que `minecraft:anvil` en enclume de forgeron : on ne pourrait plus forger. L'enclume d'Overgeared montre déjà ses objets en 3D | Rapport 1 |
| **Leaves Be Gone** | Sur la liste noire d'Aeronautics, et FallingTree casse déjà les feuilles | Rapport 5 |
| **Dynamic Surroundings** | Il touche le moteur de son comme Sound Physics (risque fort), ses pas ne valent pas Presence Footsteps, le reste fait doublon. Auroras reste en repli pour l'aurore | Rapport 3 |
| **Entity Culling** | Trois tickets ouverts sans réponse chez Aeronautics et Sable. Le rapport 5 note un correctif des block entities depuis 1.11.0 : un dernier test avec la 1.11.3 sur un dirigeable assemblé, puis retrait s'il casse | Rapports 3 et 5 |
| **La lightmap de Polytone** | Elle coupe More Darkness, la teinte des lunes d'Enhanced Celestials et la nuit de NML, et n'existe pas par biome en 1.21.1. Polytone ne garde que l'herbe, les feuilles et l'eau | Rapport 3 |
| **Quark « Variant Animal Textures »** | Doublon des variantes de NML, et décale les textures de Fresh Animations | Rapport 3 |
| **Quark « Thatch » et « Variant Bookshelves »** | NML a les mêmes blocs avec la même recette | Rapport 5 |
| **Le brouillard propre à NML** | ATMOSPHERICS en est le seul responsable (`nomansland-client.toml`, `[fog_modifiers]`) | Rapports 3 et 4 |
| **Les biomes d'Hybrid Aquatic** | 9 biomes, dont une rivière tropicale qui remplace la rivière vanilla, contre la décision 0003 : coupés par sa config | Rapport 4 |
| **L'Analyzer de Wildex** | Un raccourci qui révèle les créatures sans les avoir vues | Rapport 4 |
| **`doLimitedCrafting`** | Trois fuites : les 1 071 succès de Create rendent les recettes, la grille du sac l'ignore, les machines Create n'ont pas de joueur | Rapport 5 |
| Améliorations four, fumoir et alimentation du sac | Contournent la cuisine et la forge | Rapport 2 |

### Ce qui s'ajoute

| Ajout | Rôle |
|---|---|
| **JEI, caché** | Pont pour EMI : Create, Aeronautics, Cold Sweat, Ecliptic, Quark, Exposure, Alex's Mobs et Farm & Charm n'ont qu'un plugin JEI (rapport 3). Overgeared et Kaleidoscope ont déjà un plugin EMI |
| **AA4 Atlas** | Confirmé : l'atlas devient un objet (rapport 4) |
| Bibliothèques Puzzles Lib, Balm, Configurable, MRU, Cloth Config | Demandées par les ajouts du chapitre 11 (rapports 3 et 5) |
| **Un mod custom `fallow`** | Ce que les scripts ne savent pas faire (12.4) |
| Traduction française d'Overgeared (`fr_fr.json`) | Le mod n'en a pas (rapport 1) |

### Ce que le plan disait de faux

| Où | Faux | Vrai |
|---|---|---|
| 10.3, 10.10 | Une « forge » et des alliages (bronze, laiton) dans Overgeared | Pas de bloc forge : on chauffe au haut fourneau vanilla, on martèle sur l'enclume (mini-jeu), on trempe, on polit. Le seul alliage est l'acier. Le plan d'Overgeared trouvé en ruine sert à passer au diamant |
| 06 | Overgeared a un plugin JEI | Il a un plugin EMI, et la compatibilité Sable/Aeronautics |
| 10.5 | Kaleidoscope Compat fusionne les doublons | Il les rend interchangeables ; son mode « UNITE » casse le bacon, les pâtes et les oignons de Farm & Charm |
| 10.7 | Freeze It and Heat It a des tags de saison | Il se règle par listes d'identifiants dans sa config, sans API KubeJS ; la saison passe par Cold Sweat |
| `saisons_cold_sweat.js` | — | Mauvaise API : le bon schéma est `addTempModifier` + `gatherDefaultModifiers('world')` (code dans le rapport 2) |
| 10.4 | La récompense « Spyglass Pulse » de Wildex est coupée | Il n'y a pas d'interrupteur : l'accepter, ou un petit mixin dans `fallow` |
| 11.1 | Polytone fait une lumière plus chaude dans les clairières | Pas de lightmap par biome en 1.21.1 |
| 11.7 | EMI regroupe les objets, et ne cache pas les recettes joueur par joueur | EMI 1.1.24 n'a pas de groupes (rapport 3 ; le rapport 5 pense que KubeJS sait le faire : à tester). AStages cache bien par joueur, mais sa licence est fermée et il touche le rendu des chunks |
| 11.8 | Un marchand ambulant laisse les fragments du codex de Monoliths | Il n'existe pas. Seule source : 0,2 % par mob tué |
| 11.8 | Les monuments ont des gardiens | Ce sont des spawners de breeze, que Fallow bannit : les monuments sont vides |
| 11.8 | Les biomes des monuments se règlent par datapack | Par la config `config/obscuria/ancient_remnants-common.toml` ; un datapack peut ajouter, pas retirer. Sparse Structures ne les espace pas |
| 11.7 | Ordre des caméras : Spatial GUI, Camera Overhaul, Shoulder Surfing, Real Camera | Spatial GUI, **Shoulder Surfing** (Sable a 5 mixins écrits pour lui), Camera Overhaul en première personne seulement et à 0 dans les véhicules, Real Camera en dernier (il touche la visée comme Shoulder Surfing) |
| 10.6, 11.2 | NML remodèle cochons, vaches et calmars | 8 espèces (cochon, vache, mooshroom, poulet, mouton, lapin, calmar, calmar luisant), via Mixed Litter qu'il embarque ; on choisit espèce par espèce dans `mixed_litter-startup.toml` lesquelles laisser à Fresh Animations |

## 12.2 Les liens, système par système

Chaque ligne est une config, un tag, un datapack ou un script ; le rapport cité donne le détail.

### La forge (Overgeared), rapport 1

- **Le martelage reste manuel** : la qualité (`poor` à `master`) vient du mini-jeu. Create apporte la chaleur et la quantité : la presse fait les plaques, le mélangeur l'acier. On interdit le chauffage des lingots au ventilateur sur lave (suffixe `_manual_only`).
- **Verrou gratuit** : le fer demande une pioche en cuivre, le zinc de Create une pioche en fer. Le laiton passe donc après la forge sans script.
- **Pièces critiques** : un essieu ou une pale d'hélice peut exiger une qualité minimale (ingrédient NeoForge `neoforge:components`).
- **Les plans** : les plans d'Overgeared d'un type caché (`lanterne`, `rail`, `aerostat`…) deviennent la condition pour forger les pièces qui verrouillent les machines. La Lanterne de clairière en devient une (recette dans le rapport 1).
- **Nether fermé** : le four du Nether, le fourneau de coulée et la netherite sont cachés ; le gabarit diamant entre dans le butin des ruines de NML.
- **Chaleur** : le haut fourneau réchauffe déjà pour Cold Sweat ; le four à alliage s'ajoute. Le soufflet de Supplementaries accélère le haut fourneau.
- **L'équipement des autres mods** en métal (couteaux de Kaleidoscope compris) passe par la forge ; les trophées de créatures restent à l'établi.

### La cuisine, les saisons et le corps (Kaleidoscope et les autres), rapport 2

- **Chaleur** : un feu de camp ou un fourneau Farm & Charm chauffe le wok, la marmite, le cuiseur et la théière de Kaleidoscope. Cold Sweat ne compte pas ces fourneaux : on les ajoute avec un effet faible. Le Feu de veille des clairières reçoit le tag de chaleur de Kaleidoscope et une entrée Cold Sweat : on peut y cuisiner et s'y réchauffer.
- **Saisons** : les fichiers de saison de Farm & Charm sont au mauvais endroit, ses cultures poussent toute l'année. On pose les tags `eclipticseasons:crops/*`. Celles de Kaleidoscope sont déjà saisonnières.
- **Un seul pont saison vers Cold Sweat** donne la saison aux joueurs et à la soif de Thirst Was Taken, qui lit la température du corps.
- **Doublons** : on garde les tomates, la laitue, la farine et la pâte de Kaleidoscope, le four de Farm & Charm ; la rôtissoire Farm & Charm et l'épouvantail Kaleidoscope sortent ; la marmite Farm & Charm aussi, ses recettes passent dans celle de Kaleidoscope. Farm & Charm reste en 1.1.25 (la 1.1.27 ajoute une planche à découper et des hachoirs en double).
- **Animaux** : leur nourriture se règle par tags, leur reproduction par saison avec `eclipticseasons:breed/*`.
- **Le sac** : son tuyau ne désaltère pas avec Thirst Was Taken ; seul du Java le corrige (12.4). Freeze It and Heat It étant retiré, la nourriture ne se gâte nulle part, ce qui règle aussi le « sac réfrigérateur ».

### Le monde et les objectifs, rapport 4

- **Clairières** : `/fillbiome` alignée sur 4 blocs et sur toute la hauteur (8×336×8). Trois variantes du biome (froide, tempérée, chaude) pour qu'Ecliptic garde les saisons, une liste explicite des biomes NML convertibles, les spawns réglés par les tags `naturalist:has_*` et une règle In Control! contre les hostiles. La brume ne se met à jour que quand le joueur change de cellule de 4 blocs (rapport 3).
- **Monoliths** :
  - les 6 monuments sont rattachés à des biomes NML, avec un espacement de 64 à 80 chunks ;
  - l'Elderheart est une entité sans événement. Un script qui le surveille lance la clairière autour du monument réveillé ;
  - les bénédictions sont permanentes, une à la fois, gardées à la mort. Celle du guerrier est presque nulle dans Fallow et peut être remplacée par une autre via son tag ;
  - les fragments du codex entrent dans le butin des ruines NML (coffres et archéologie), et la photo d'un monument avec Exposure en donne un. On peut ajouter des pages Fallow au codex par datapack.
- **Zoo** :
  - **transport** : laisse, bateau pour les petits animaux, et surtout le **siège Create**, sur un train ou un dirigeable, qui accepte n'importe quel animal ;
  - **réglages** : la charrette d'Icy's passe en 2.1.4. Les animaux d'Hybrid Aquatic n'ont pas de seau, et le lion attaque les chevaux ;
  - **plaque d'enclos** : Wildex ne reconnaît pas d'enclos. Une plaque KubeJS compte les espèces présentes et se branche sur le rappel `onDiscoveryChanged` de Wildex pour les paliers ;
  - **complétion** : sans liste d'exclusions (Nether, End, monstres bannis, doublons coupés), le bestiaire ne peut pas être complété.
- **Doublons de faune** : environ 14 espèces existent en double. Le rapport 4 dit laquelle garder, par config ou biome modifier, sans retirer aucun mod (décision de l'équipe).
- **Autres réglages** : la Friend Moon de NML entre en concurrence avec Enhanced Celestials, les spawners vanilla des donjons NML tourneront à vide, et les jarres NML se regarnissent. `spreadFactor 3` risque de rendre les ruines NML très rares.

### Create et la construction, rapport 5

- **Les plans d'ingénieur, nouvelle méthode** : le plan est un **ingrédient gardé** de la recette (`keepIngredient`). Il reste dans la grille, se voit posé sur l'établi grâce à Visual Workbench, marche dans les machines Create, se prête et peut se perdre. Les plans d'Overgeared couvrent les pièces forgées. Le savoir n'est plus personnel mais physique, ce qui va bien avec un monde permanent entre amis.
- **Le bois** : les recettes de scie de NML ne se chargent pas (mauvais dossier). Un `bois.js` en écrit 16. FallingTree reconnaît déjà les haches d'Overgeared ; sa taille maximale (100) monte pour les grands arbres de NML.
- **Doublons de construction** : règle « le style de NML d'abord ». Le tableau 2.2 du rapport 5 tranche bloc par bloc (briques, chaume, bibliothèques, appliques, galets…). La rhyolite de Twigs, générée dans tout l'Overworld, est coupée.
- **Every Compat** : liste noire pour les bois Quark désactivés (Ancient, Azalea, Blossom), qui génèrent quand même environ 150 blocs, et pour son module Twigs. Les mods de construction ajoutent environ 1 850 blocs, à mesurer avec spark.
- **Dirigeables** :
  - les coffres Lootr sont marqués immobiles ;
  - le mobilier et le sac reçoivent une masse Sable par tags ;
  - le message de coordonnées du sac à la mort est coupé ;
  - la tombe GraveStone d'une mort en vol flotte probablement dans le ciel : à tester.
- **Le Dissipateur** : KubeJS Create ne crée pas de bloc cinétique. Un prototype KubeJS, une tête posée devant un ventilateur Create qui lit sa vitesse (c'est le ventilateur qui consomme le stress), permet de juger l'idée. Le vrai bloc va dans `fallow`.
- **Scripts à corriger** : le groupe MoreJS s'appelle `MoreJS` ; LootJS 3 n'a plus `.when()` ; `NativeEvents.onEvent` attend une classe ; il faut supprimer les 15 gisements par défaut de Create Ore Excavation.

### Le rendu, rapport 3

- **ATMOSPHERICS efface le ciel des autres mods** : la lune d'Enhanced Celestials (donc la lune blanche) et les soleils doubles de NML ne se voient plus. C'est le plus gros trou trouvé côté client.
- **Ses presets** : les biomes NML doivent y être nommés un par un, et le fichier renommé à chaque version (`fallow_v1`, `fallow_v2`…) pour être réappliqué.
- **Polytone** : il ne garde que l'herbe, les feuilles et l'eau. Sa 5.x modifie le rendu des chunks de Sodium : on la fige après test, ou on prend la 4.5.3.
- **Petits réglages** :
  - MB-3D passe sous Amendments, pour ses lanternes ;
  - Particular n'a pas d'entrée pour les feuilles de NML, qu'il faut ajouter ;
  - Sodium efface le fond de Seamless Loading Screen (cosmétique ; le mod n'est plus maintenu).

## 12.3 Versions à monter avant de figer le monde

NML 1.6.1, Sable 2.0.6, Quark 4.1-487, Moonlight 3.7.2, Kaleidoscope Cookery 1.6.0, Ecliptic 0.15.4.1, Icy's Better Horses 2.1.4, Friends & Foes. Cold Sweat reste en 2.4.3.1 et Farm & Charm en 1.1.25 jusqu'à test. Supplementaries 4.0.2 est une nouvelle version majeure : relire sa config avant de monter. KubeJS Create et Steam 'n' Rails sont en bêta.

## 12.4 Le mod custom `fallow`

Les scripts et les données couvrent presque tout. Ce qui reste demande du Java. Je propose **un seul mod `fallow`** (NeoForge 1.21.1), découpé en modules qu'on active un par un, plutôt que plusieurs petits mods.

| Module | Ce qu'il fait | Pourquoi pas en script | Taille | Priorité |
|---|---|---|---|---|
| **ciel** (client) | Redessine la lune d'Enhanced Celestials, donc la lune blanche, par-dessus le ciel d'ATMOSPHERICS ; vide le cache de brume après une conversion en clairière | ATMOSPHERICS remplace tout le dessin du ciel | 4 à 6 classes, des mixins | **Haute** : sinon la lune blanche est invisible |
| **dissipateur** | Vrai bloc cinétique Create : consomme du stress, s'affiche aux lunettes d'ingénieur, a son Ponder, convertit le rayon de 64 | KubeJS Create ne crée pas de bloc cinétique | 8 à 12 classes | Moyenne : après le prototype KubeJS |
| **sac** | Le tuyau de Traveler's Backpack désaltère (Thirst Was Taken) | Les deux mods ne se parlent pas, aucun événement exposé | 2 à 3 classes | Moyenne |
| **tombe** | La tombe descend au sol quand on meurt en vol | GraveStone pose la tombe au point de mort | 2 classes | Seulement si le test échoue |
| **voies** | Voies Steam 'n' Rails dans les bois de NML | Steam 'n' Rails ne génère que pour les bois qu'il connaît | 3 à 5 classes | Basse |
| **plans** | Un savoir des plans propre à chaque joueur, sans fuite | KubeJS 2101 ne verrouille plus une recette par joueur | 7 à 10 classes | Seulement si l'équipe veut un savoir personnel plutôt que physique |
| **finitions** | Vraies étincelles aux coups de marteau d'Overgeared ; couper le Spyglass Pulse de Wildex | Pas d'événement côté KubeJS | 2 à 4 classes | Basse |

Le mod se construit dans le dépôt, sous un dossier à part (`mods-custom/fallow/`), avec la CI GitHub pour le compiler. Il ne touche jamais au code des autres mods, seulement par mixins ciblés et par leurs API.

## 12.5 Décisions qui s'ajoutent (suite du 10.12)

| # | Sujet | Défaut retenu | Alternative |
|---|---|---|---|
| 14 | Plans d'ingénieur | Plan physique, gardé dans la recette, plus les plans d'Overgeared pour les pièces forgées | Savoir personnel par joueur (module `plans`) |
| 15 | Gardiens des monuments | Aucun : le monument est vide et silencieux | Une exception pour la breeze, ou un autre gardien placé par la structure |
| 16 | Lune blanche visible | Module `ciel` du mod `fallow` | Abandonner la lune blanche |
| 17 | Mod custom `fallow` | Oui, modules `ciel`, `dissipateur` et `sac` d'abord | Seulement des scripts, en acceptant les trous listés |
| 18 | Boire au tuyau du sac | Module `sac` | Le réservoir du sac ne sert qu'à transporter de l'eau |

## 12.6 Ordre de travail proposé

1. Monter les versions (12.3) sur une branche de test et lancer le pack tel quel.
2. Faire les tests qui changent ce qu'on peut construire : le rappel Wildex, le déclenchement serveur d'Exposure, un gros animal assis pendant un vol, la lune avec ATMOSPHERICS, Entity Culling sur un dirigeable.
3. Écrire les configs et les scripts corrigés (rapports 1, 2, 4 et 5), un système à la fois.
4. Commencer `fallow` par le module `ciel`.
