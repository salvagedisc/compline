# 10. Recentrage : un monde presque vide et énigmatique

> **Vérification du 2026-10-18 : le [chapitre 12](12-verification.md) corrige ce chapitre** (Easy Anvils, Leaves Be Gone, Dynamic Surroundings et Entity Culling sortent, Polytone sans lightmap, ordre des caméras, Overgeared, Kaleidoscope Compat, Monoliths).

Ce chapitre **remplace la liste du chapitre 1** et les « décisions à valider » du 8.4. Les chapitres 2 à 7 restent la référence technique (configs, clés, scripts) pour les mods qui restent.

Historique des retours de l'équipe :
- **2026-10-11** : Sanity: Renewed retiré ; faune, compagnons et Traveler's Backpack gardés ; ParCool reste dehors ; la référence d'ambiance est **le trailer de No Man's Land**.
- **2026-10-18** :
  - Retirés : Capture Nets, Paraglider, GUI Compass, Simple Voice Chat, Hardcore Revival, Create Aeronautics Structures, Cinch's Abandoned Houses, Cattlestrophic! et Easy NPC.
  - Nouvelle exigence : une **finition soignée façon Realism Craft**, en usage, construction, modèles et ambiance, et **tout relié** pour une expérience fluide (10.6 et 10.7).

## 10.1 Ce qui a dérivé

La v1 visait environ 100 mods et 140 fichiers avec les bibliothèques. Chaque mod passait le filtre des 5 questions, mais le filtre jugeait les mods un par un, jamais l'ensemble. Elle empilait :
- trop d'addons Create ;
- trois mods de structures pour le même rôle ;
- quatre jauges de survie ;
- des outils de vue lointaine qui annulent la brume.

## 10.2 L'idée de base

1. **Un monde permanent et fluide.** Du contenu, mais pas trop : chaque mod doit changer la façon de jouer, être stable et rester léger. Ce qu'on construit reste, et les ressources s'épuisent.
2. **L'ambiance du trailer de No Man's Land.** De grands paysages dans la brume, des ruines, des menhirs, un voyageur seul. Le monde est **presque vide et énigmatique**, pas sombre.
3. **La brume se lève.** Elle se dissipe par endroits et par moments, et surtout **là où le joueur fait revenir la vie**.
4. **Des objectifs à long terme.** Un zoo, un bestiaire complet, des clairières, un réseau ferré, un aérodrome, un atlas entier.
5. **Une finition soignée et liée.** Parcourir le monde, construire, manger, couper un arbre, ouvrir son sac : chaque geste doit être agréable à voir et à faire. Les mods doivent se parler, et non cohabiter.

**Filtre en plus des 5 questions de la vision** :
- **Poids** : est-ce que ça change ce que le joueur décide, vise ou ressent à chaque geste ?
- **Ton** : est-ce que ça garde le monde presque vide et énigmatique, sans l'encombrer ni l'assombrir ?
- **Lien** : est-ce que ça se branche sur ce qui existe déjà (bois, température, cuisine, faune, carte) ?
- **Coût** : est-ce stable, léger, et sans risque pour un monde de plusieurs années ?

## 10.3 La brume qui se lève

### Couche 1 : le monde (ATMOSPHERICS, déjà retenu)

ATMOSPHERICS règle le brouillard, le ciel et la brume **par biome**, avec des réglages séparés pour le jour et la nuit. Ses presets peuvent être livrés dans un pack de ressources (`assets/atmospherics/preset/<nom>.json`).

Le preset Fallow sépare les biomes de No Man's Land en deux familles :
- **Brumeux** : forêts denses, marais, tourbières, taïga, vallées.
- **Dégagés** : prairies, champs de fleurs, forêts d'érables, hauts plateaux.

### Couche 2 : le ciel (Enhanced Celestials)

La lune de sang est retirée. La lune moisson et la lune bleue sont gardées, et une **lune blanche** s'ajoute : un ciel pâle, une nuit étrange, rien d'hostile. Brouillon : `kubejs-brouillons/data/fallow/enhancedcelestials/lunar/event/lune_blanche.json`.

### Couche 3 : les clairières (mécanique Fallow, KubeJS)

Le joueur pose un **feu de clairière**. Le script convertit le biome dans un rayon autour de lui en **`fallow:clairiere`** avec la commande vanilla `/fillbiome`. Ce biome a :
- une brume dégagée (preset ATMOSPHERICS) ;
- une herbe et un feuillage d'un vert franc ;
- des apparitions d'animaux ;
- les tags Ecliptic d'un biome tempéré.

| Palier | Objet | Rayon | Fabrication |
|---|---|---|---|
| 1 | Feu de veille | 16 blocs | À la main : pierre, charbon de bois, résine de No Man's Land |
| 2 | Lanterne de clairière | 32 blocs | Pièces forgées chez Overgeared |
| 3 | Dissipateur | 64 blocs | Atelier Create : engrenages, ventilateur, laiton |

Le changement est permanent et appliqué une seule fois, par colonnes de 8 × 64 × 8 étalées sur plusieurs ticks : il ne coûte rien en continu. Les rivières, océans et grottes ne sont pas touchés.

Brouillons : `startup_scripts/clairieres.js`, `server_scripts/clairieres.js`, `data/fallow/worldgen/biome/clairiere.json`.

À vérifier en jeu :
- ATMOSPHERICS reconnaît-il un biome de datapack dans un preset ?
- Les clients voient-ils la nouvelle couleur sans se reconnecter ?
- Ecliptic accepte-t-il le biome ?

## 10.4 Les objectifs à long terme

| Objectif | Comment | Mods |
|---|---|---|
| **Le zoo** | Trouver les espèces, les ramener vivantes, leur bâtir un enclos qui ressemble à leur biome dans une clairière. Les animaux se ramènent en laisse, en bateau, en charrette Icy's ou en wagon Create | Faune et compagnons retenus, Icy's Better Horses, Create, clairières |
| **Le bestiaire** | Découvrir chaque espèce à la longue-vue ou en la **photographiant** avec Exposure. Un socle affiche l'hologramme de chaque espèce découverte : c'est la vitrine du zoo | **Wildex Bestiary** |
| **Les clairières** | Repousser la brume autour de la base, puis des avant-postes, palier par palier | Script Fallow |
| **Le réseau** | Voie ferrée entre les clairières, puis un aérodrome | Create, Steam 'n' Rails, Create Aeronautics |
| **L'atlas** | Une carte qui se dessine en marchant, sans coordonnées | Antique Atlas 4, AA4 Atlas |
| **La maison** | Une base meublée, couverte de vrais toits, dans le bois de la région | Handcrafted, Macaw's, Twigs, Every Compat (10.6) |
| **Les lignées** | Des chevaux élevés sur des générations | Icy's Better Horses |

**Wildex Bestiary** (`wildex-bestiary`, 3.0.0, aucune dépendance) :
- La découverte se fait à la longue-vue, au combat ou par une photo Exposure.
- La progression est par joueur et stockée côté serveur.
- Le mod s'intègre à KubeJS.
- Sa récompense de fin, « Spyglass Pulse », montre les créatures à travers les murs : elle est coupée.

## 10.5 Le pack

### Les mods déjà retenus restent tous

Les 46 mods de `pack/mods/` restent, faune et compagnons compris. Les règles de la décision 0003 s'appliquent toujours : une espèce par mod, hostiles rares et territoriaux, plafonds In Control!.

### Ce qui s'ajoute pour le jeu

| Mod | Rôle |
|---|---|
| **Traveler's Backpack** | Le sac d'expédition. Il a son **propre sac de couchage** à déployer, ce qui rend Comforts inutile (retiré) |
| **Wildex Bestiary** | Le bestiaire, colonne vertébrale du zoo |
| **AA4 Atlas** | L'atlas devient un objet qu'on porte et qu'on peut perdre |
| **Exposure** | La photo argentique : garder la trace d'un lieu, découvrir une espèce pour le bestiaire |
| **Enhanced Celestials** | Lunes rares, dont la lune blanche |
| **Lootr** | Chaque joueur trouve les coffres pleins ; aucun contenu |
| **Create Ore Excavation** | Gisements finis : il faut repartir plus loin |
| **Kaleidoscope Compat** | Fusionne Kaleidoscope Cookery et Farm & Charm : retire des doublons |
| ~~Freeze It and Heat It~~ | Retiré par l'équipe le 2026-10-18 : la nourriture ne se gâte pas |
| **Ancient Remnants: Monoliths** | Demandé par l'équipe le 2026-10-18 : de rares monuments d'une civilisation oubliée, un codex à reconstituer page par page (11.8) |

Infrastructure, sans contenu visible : In Control!, LootJS, KubeJS Create, MoreJS, Neruina, Simple Backups, EMI (choix de l'équipe, à la place de JEI ; 11.7), GraveStone, Default Options, Crash Assistant, Mouse Tweaks, Controlling (+ Searchables).

Optionnels, à montrer en jeu avant d'ajouter : Cosy Critters (oiseaux qui s'envolent, papillons de nuit autour des lampes) et Longwings (papillons à collectionner, donc une page de plus au bestiaire).

### Ce qui sort

| Sorti | Raison |
|---|---|
| Capture Nets, Paraglider, GUI Compass, Simple Voice Chat, Hardcore Revival, Create Aeronautics Structures, Cinch's Abandoned Houses, Cattlestrophic!, Easy NPC | Retirés par l'équipe le 2026-10-18 |
| Comforts | Doublon du sac de couchage de Traveler's Backpack |
| ParCool! | Désactivé, comme demandé |
| Sanity: Renewed, TheWatcher | Le monde est énigmatique, pas sombre |
| Better Archeology | Du contenu sans mécanique forte |
| Create: New Beginnings, Let The Adventure Begin, MCG's RPG Campsites, optionnels de structures | Les ruines de No Man's Land et les structures vanilla espacées suffisent |
| Traveler's Titles | Le nom de la région à l'écran casse le mystère |
| Distant Horizons, Separate Sable Render Distance, Simple Clouds, Particle Rain | La brume rend la vue lointaine inutile : c'est la plus grosse économie de performance |
| Streams Reflowing | Worldgen lourd et non réversible sur un monde permanent |
| Copycats+, Connected, Factory Logistics, Bells & Whistles, Diesel Generators, Power Loader, Prospecting Picks, Rechiseled (+ Create, Fusion) | L'empilement d'addons Create que la vision interdit ; COE est le seul gardé |
| Create Villager Labor, Open Parties and Claims | Colonies peuplées et protections : inutiles entre amis |
| Salt's Animal Farm, Animal Genders, Kaleidoscope Tavern, Thirst Canteen, Tide, Nutritional Balance, AppleSkin | Une jauge ou une activité de plus pour un rôle déjà couvert |
| Reactive Music | AmbientSounds suffit ; le silence fait partie de l'ambiance |
| Almost Unified, Jade | Moins de doublons à unifier ; Jade nomme tout ce qu'on regarde |

## 10.6 La finition, façon Realism Craft

Realism Craft (Spark Universe, Bedrock seulement) d'après son wiki officiel :
- des animations du joueur et des modèles 3D pour les outils ;
- des éclairages dynamiques et des effets visuels ;
- des sons retravaillés et des animaux réalistes avec variantes par biome ;
- beaucoup de **décor naturel** (troncs couchés, cailloux, branches au sol, mousse pendante, champignons sur les arbres) ;
- un **mobilier** complet (chaises, tables, armoires, rideaux), des tentes, des cordes et une scie à arbres.

Rien de tout ça n'existe pour Java. On reconstruit la même sensation par couches, avec des mods qui s'appuient presque tous sur des bibliothèques **déjà dans le pack**.

Une limite tient dès le départ : **pas de textures réalistes ni de shaders**. Iris a des problèmes connus avec Create Aeronautics, et un pack de textures en 128× casserait le style de No Man's Land. La finition passe par le geste, les modèles, la construction et le son, dans le style « vanilla soigné » de NML.

| Couche | Mod | Ce qu'il apporte | Côté | Dépendance |
|---|---|---|---|---|
| **Le corps** | **Fresh Animations : Player Extension** (choix de l'équipe, à la place de Not Enough Animations et First-person Model) | Le joueur est animé dans le même style que les créatures ; Real Camera, à tester, le montre aussi en vue subjective (11.2) | C | Entity Model Features |
| | **Shoulder Surfing Reloaded** | Une caméra à l'épaule soignée pour la troisième personne | C | aucune |
| **Les objets** | **ItemPhysic Lite** | Les objets lâchés se posent à plat au sol au lieu de flotter en tournant | C | CreativeCore (déjà là) |
| | **Visual Workbench** | Les ingrédients restent posés sur l'établi, en 3D | CS | Puzzles Lib |
| | **Amendments** | Lanternes en 3D dans la main et accrochées aux murs avec un balancement, enseignes qui oscillent, chaudrons qui bouillonnent, tapis sur les escaliers ; réglé, comme Supplementaries, sur l'essentiel | CS | Moonlight (déjà là) |
| | Pack de ressources **MB-3D Items** | Chaque objet vanilla devient un modèle 3D dans le style de Minecraft | C | aucune |
| **Construire** | **Handcrafted** | Mobilier soigné : chaises où l'on s'assoit, tables, armoires, lits superposés, coussins et draps colorés | CS | Resourceful Lib (déjà là) |
| | **Macaw's Roofs** + **Macaw's Windows** | De vrais toits en pente au lieu d'escaliers ; fenêtres, volets, rideaux | CS | aucune |
| | **Twigs** | Brindilles, galets, roches, tuiles et briques : le petit décor au sol que Realism Craft met partout | CS | aucune |
| | **Every Compat** | Génère tout le mobilier, les toits, les fenêtres et les blocs Twigs **dans chaque bois du monde**, y compris le saule, le noyer et l'érable de No Man's Land et les bois de Quark | CS | Moonlight (déjà là) |
| **La nature** | **FallingTree** (mode « blocs qui tombent ») | Couper le pied d'un arbre le fait tomber au sol, avec une animation ; seules les haches comptent, Overgeared compris | S | aucune |
| | Pack de ressources **Motschen's Better Leaves** | Des feuillages touffus, compatible avec beaucoup de mods | C | aucune |
| **L'interface** | **Smooth Swapping** | Les objets glissent d'une case à l'autre dans l'inventaire | C | aucune |

Déjà retenus et qui font partie de la finition : LambDynamicLights, Subtle Effects, AmbientSounds, Sound Physics Remastered, ATMOSPHERICS, More Darkness, les remodèles d'animaux de No Man's Land.

**Fresh Animations entre** (L'équipe l'aime), avec Entity Model Features, Entity Texture Features et trois extensions officielles. Le rendu sans shaders (Polytone, Particular Reforged, Sounds…) et le confort sont détaillés au [chapitre 11](11-rendu-et-confort.md).

**À vérifier sur une branche `test/finition`** :
- **Fresh Animations face à No Man's Land**, qui remodèle déjà les cochons, les vaches et les calmars : voir lequel l'emporte.
- **Les pas** : Presence Footsteps n'a pas de version NeoForge 1.21.1 ; Dynamic Surroundings, réduit aux pas, aux cascades et à l'aurore, le remplace (11.2).
- **Better Leaves** : vérifier le coût en images par seconde sur les grandes forêts de NML.

## 10.7 Tout relier

C'est le travail qui fait la différence entre une liste de mods et un monde fluide. Chaque lien est une config, un tag ou un script, pas un mod de plus.

| Lien | Ce qu'on règle | Où |
|---|---|---|
| **Le bois** | Every Compat donne toutes les variantes dans chaque essence ; une seule liste de bois, celle du monde | Every Compat, automatique |
| **La chaleur** | Les fourneaux de Kaleidoscope Cookery, les chaudrons qui bouillent, les braseros et les cheminées réchauffent selon Cold Sweat | Config Cold Sweat (sources de chaleur par bloc) |
| **Les saisons** | Ecliptic règle la température ressentie (script existant), la neige, les cultures de Farm & Charm et Kaleidoscope Cookery | `saisons_cold_sweat.js`, tags `eclipticseasons:crops/*` |
| **Manger et boire** | Un seul système de cuisine (Kaleidoscope Compat) ; les soupes et les boissons de Kaleidoscope hydratent pour Thirst Was Reclaimed | Config Thirst, tags |
| **La faune** | Une espèce par mod, des apparitions par biome, des animaux qui reviennent dans les clairières, un bestiaire qui lit la photo | In Control!, tags de biome, Wildex + Exposure |
| **Le geste** | La lanterne tenue éclaire (LambDynamicLights), s'anime (Amendments) et se voit en vue subjective si Real Camera passe le test | Configs client |
| **L'arbre** | FallingTree ne marche qu'avec des haches, dont celles d'Overgeared ; les bûches de NML sont reconnues | Tags `minecraft:logs` et axes |
| **La carte** | Antique Atlas marque les ruines de NML et les gisements de COE découverts ; jamais de coordonnées | Config Surveyor et Antique Atlas |
| **Le butin** | Les plans d'ingénieur et les objets rares sortent des ruines de NML et des structures vanilla, par joueur (Lootr) | LootJS (`loot/butin.js`) |
| **L'interface** | Un seul visualiseur de recettes (EMI, rangé par catégorie), des touches sans conflit livrées par Default Options, les mêmes réglages vidéo pour tous | Default Options, Controlling |

## 10.8 En chiffres

| | v1 | v2 du 2026-10-18 |
|---|---|---|
| Mods de jeu (hors ambiance, technique et finition) | environ 70 | **37** (28 retenus + 9 ajoutés) |
| Mods de finition | 0 | **11** (Not Enough Animations et First-person Model remplacés par Player Extension), plus 3 packs de ressources |
| Fichiers dans `pack/mods/` avec bibliothèques | environ 140 | **environ 110** |
| Jauges de survie | faim, soif, température, nutrition | faim, soif, température |
| Addons Create ajoutés | 8 | 1 (COE) |
| Distance de vue serveur | 10, plus Distant Horizons à l'essai | **8**, simulation 6, sous la brume |

## 10.9 Performance

- `view-distance=8`, `simulation-distance=6`. La brume d'ATMOSPHERICS finit avant le bord des chunks chargés.
- La faune reste : les plafonds In Control! du [5.3](05-faune-et-ambiance.md) sont indispensables.
- La finition est presque toute côté client et légère (animations, modèles, glissements). Les deux points à mesurer avec spark sont les feuillages touffus et FallingTree sur les très grands arbres.
- Les clairières ne coûtent qu'au moment de la pose.
- Monde prégénéré avec Chunky, frontière au bord de la prégénération.

## 10.10 Scripts KubeJS qui changent

| Script | Sort |
|---|---|
| `garde_manger.js`, `raison.js` | **Retirés** |
| `plans_ingenieur.js` + `loot/butin.js` | Les plans ne viennent plus des épaves de Create Aeronautics Structures : ils sortent des ruines et des donjons de No Man's Land et des structures vanilla |
| **17. `clairieres.js`** (startup + server) + biome `fallow:clairiere` + preset ATMOSPHERICS | Les feux de clairière (10.3) |
| **18. `zoo.js`** | Récompenses des étapes du bestiaire Wildex, par exemple le plan de la Lanterne de clairière après 25 espèces |
| Tous les autres | Gardés |

## 10.11 La vision

La ligne de `docs/vision.md` « Solitude et étrangeté diffuse, sans horreur ni jumpscare » reste juste. Je propose de la préciser :

> Solitude dans un monde **presque vide et énigmatique**, dans l'esprit du trailer de No Man's Land : brume, grands paysages, traces de ceux qui sont partis. La brume se lève par endroits et par moments, et là où l'on fait revenir la vie. Chaque geste est soigné et chaque système se branche sur les autres. Sans horreur ni jumpscare.

Et dans « Règles de travail » : « Le pack se juge en entier : chaque ajout doit en valoir le coût en performance, en ton, et se relier à ce qui existe. »

## 10.12 Décisions à valider (remplace le 8.4)

| # | Sujet | Défaut retenu | Alternative |
|---|---|---|---|
| 1 | Clairières | Permanentes, 3 paliers (16, 32, 64 blocs) | Réversibles quand on casse le feu |
| 2 | Bestiaire | Wildex (photo Exposure, socles holographiques) | Field Guide (plantes en plus, mais exige Immersive Overlays, qui montre les coordonnées) |
| 3 | Mobilier | Handcrafted | Macaw's Furniture ou Another Furniture |
| 4 | Construction | Macaw's Roofs + Windows, Twigs | Ajouter Macaw's Doors et Fences |
| 5 | Objets en 3D | MB-3D Items | Objets plats d'origine |
| 6 | Arbres | FallingTree, mode « blocs qui tombent » | Mode « le tronc descend » (moins de raccourci) ou pas de FallingTree |
| 7 | Animations | Fresh Animations + Details, Objects, Emissive et Player Extension (le joueur aussi, choix de l'équipe) | Seulement les remodèles de NML |
| 8 | Lune blanche | Gardée, sans effet hostile | Seulement les lunes d'origine, sans la lune de sang |
| 9 | Nether | Fermé (inchangé) | Ouvert |
| 10 | Distance de vue 8 sous la brume | Oui | 10 |
| 11 | Rendu sans shaders | Polytone, Particular Reforged, Sounds, 3D Skin Layers (11.1) | ATMOSPHERICS et Subtle Effects seuls |
| 12 | Confort | 6 mods (11.3) et la restauration automatique des outils de Quark | Quark seul |
| 13 | Archie's Dinos | Rester en 1.21.1 ; demander à l'équipe une version NeoForge ou la permission de porter (11.6) | Passer en 1.20.1 (perd Aeronautics, Sable et NML) |
