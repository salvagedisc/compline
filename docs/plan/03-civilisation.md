# 3. Civilisation : de la forge au dirigeable, puis aux colonies

Ce qui existe déjà : Overgeared, Create 6.0.10, Create Aeronautics 1.3.2 + Sable, Steam 'n' Rails (port NeoForge bêta), KaleidoscopeCookery-Automation, Supplementaries et Quark (essentiel). Ce chapitre fixe **qui fait quoi** entre l'artisan et l'usine, choisit un petit nombre d'addons Create sans empilement, et propose un **arbre de progression** verrouillé en KubeJS.

Détail mod par mod, preuves et clés : [recherche/research-civilisation.md](recherche/research-civilisation.md).

## 3.1 Le principe : l'artisan fait la qualité, l'usine fait la quantité

- **Overgeared** reste la seule source d'outils et d'armures de qualité. Aucun outil en métal ne sort de l'établi ni d'une machine Create.
- **Create** produit en masse les pièces intermédiaires, mais une presse ne sort jamais mieux qu'une qualité « correcte ».
- Les **pièces critiques** demandent une pièce forgée à la main, de qualité minimale : tête de foreuse, bogies et essieux, bloc moteur, pales d'hélice.
- Aucun pont Overgeared ↔ Create n'existe : tout passe par KubeJS ([7](07-kubejs-et-datapack.md)).

**Create 6 couvre déjà le stockage, le commerce et la poste.** Colis, frogports, *stock ticker* et *stock keeper* forment le stockage en réseau. Les boutiques en nappe permettent de vendre entre joueurs avec n'importe quel objet comme prix. Les boîtes postales posées en gare font des trains un service postal. Aucun mod de stockage « magique », de livraison aérienne ou de courrier instantané n'est donc ajouté.

## 3.2 Addons Create retenus (sans empilement)

| Mod | Rôle distinct | Dispo 1.21.1 NeoForge Modrinth | Statut proposé |
|---|---|---|---|
| **Create: Copycats+** | Formes copiables qui suivent les engins : sculpter coques de dirigeable et locomotives | miroir (3.0.7) | socle |
| **Create: Connected** (élagué) | Mécanique fine : embrayages, goupille de cisaillement, frein, boîte de vitesses, silo, cuve-chaudière, tableau de bord des sièges | oui (1.3.2) | socle |
| **Create Factory Logistics** | Fluides en colis : eau potable, lait, carburant expédiés par train ou dirigeable | oui (1.6.0) | socle |
| **Create Ore Excavation** | Gisements cachés et **finis**, foreuse entraînée par rotation, API KubeJS complète | oui (1.6.x) | socle |
| **Create: Bells & Whistles** | Quais, rambardes, portes de wagon : ce qui fait d'une voie une gare | NeoForge sur Modrinth à confirmer | recommandé |
| **Create: Diesel Generators** | Palier combustion : moteurs, distillation, pompe à pétrole, carburant des locomotives | oui (1.3.15) | recommandé |
| **Create: Power Loader** | Seul moyen de garder des chunks chargés, à l'énergie rotative. Autorisé sur les trains et les engins ; au sol, version laiton seulement et coûteuse | miroir (2.0.5) | recommandé, sous conditions |
| **Rechiseled** + **Rechiseled: Create** (+ Fusion) | Un seul mod de variantes de blocs, avec textures connectées et vocabulaire industriel cohérent | oui | recommandé |

**Optionnels** (à trancher en jeu) :
- **Create Crafts & Additions** : l'électricité tardive, avec un signal positif côté Aeronautics. C'est l'option B du [3.5](#35-électricité--rester-mécanique-par-défaut).
- **Create: Numismatics** : monnaie frappée à la presse à partir de métal, si une économie entre colonies prend forme.
- **COE Deposits** : gisements multi-chunks, de vrais bassins miniers qui justifient une ville minière.
- **Create: Interiors** : banquettes de train et sièges d'engin (masse sur Sable à vérifier).
- **Aeronautics: Transmission & Linkage** : aviation avancée.
- **SableCat** : outils serveur pour réparer des sous-niveaux Sable corrompus, utile sur un monde permanent.

**Écartés** :
- Create Big Cannons : la guerre, sans ennemi à combattre.
- Create: Hypertubes : quasi-téléportation qui contourne le rail.
- Create: New Age et The Factory Must Grow : doublons d'Overgeared (acier), de Diesel et de Crafts & Additions.
- Create: Enchantment Industry : son imprimante duplique les livres enchantés (contraire à 0004).
- Create: Trading Floor : les survivants deviendraient des distributeurs automatiques.
- Create: Mobile Packages : livraison par les airs et accès au stock à distance.
- Create: Central Kitchen : il a retiré son intégration Kaleidoscope Cookery.
- Create Deco : recoupe Copycats+ et Rechiseled: Create.
- Create: Metallurgy : doublon des alliages et moules d'Overgeared.
- FramedBlocks (doublon de Copycats+), LittleTiles (coût serveur), Domum Ornamentum (suit MineColonies), baguettes de construction (raccourci).
- Sophisticated Storage, Tom's Storage, Lightman's Currency, Ender Mail, Mighty Mail : doublons de Create 6 ou instantanés.

**Cuisine sur engin** : Kaleidoscope Contraption (wagon-restaurant) existe bien en 1.21.1 NeoForge sur Modrinth d'après la recherche vie quotidienne (1.0.3), au contraire de ce que montrait CurseForge ; optionnel, voir [4](04-vie-quotidienne.md). **À regarder aussi** : *Aeronautics: Winds and Weather* (1.3.2), qui ajoute un vent réel pour les voiles et les moulins.

**Compatibilité Sable** : chaque bloc d'addon posé sur un engin doit avoir une masse définie. À tester pour Copycats+, Connected et Interiors ; des addons « poids » existent si besoin.

## 3.3 Minerai et prospection : des veines réelles

1. **Datapack** : minerai vanilla dispersé réduit d'environ 60 %. Les grandes veines de cuivre et de fer (1.18+) restent et deviennent la cible de la prospection.
2. **Début de partie** : **Prospecting Picks** (miroir 1.2.1, recommandé). La pioche sonde la roche autour du bloc frappé et signale les veines. Sa tête est forgée chez Overgeared.
3. **Industrie** : détecteur puis foreuse de Create Ore Excavation, avec une tête de foreuse **forgée**. Gisements **finis** et **liés aux biomes** : zinc dans les biomes humides, étain en montagne… La géographie dicte où fonder les colonies.
4. **Épuisement** : un gisement épuisé oblige à repartir plus loin. Nouvelle voie ferrée, nouvelle colonie minière : c'est le moteur des colonies lointaines.

Optionnels : Ore Deposits (affleurements de surface comme indices visuels), GeoVein (non vérifié, doublon de COE Deposits).

## 3.4 Arbre de progression

| Âge | Ce que le joueur fait | Mods | Verrou principal (KubeJS) |
|---|---|---|---|
| **0. Arrivée** | Cabane, feu, outils de pierre | vanilla, Supplementaries | Pas d'outils en fer à l'établi |
| **1. Forge** | Enclume de pierre, forge, premier fer forgé, bronze | Overgeared, Prospecting Picks | Outils et armures vanilla retirés ; pioche de prospection avec tête forgée ; plans de base dans les ruines |
| **2. Atelier** | Roue à eau, moulin, presse, mélangeur | Create (andésite), Connected, Copycats+ | Alliage d'andésite à la main avec un rivet forgé ; presse et mélangeur avec un « marteau d'atelier » forgé de qualité ≥ correcte |
| **3. Laiton et vapeur** | Moteur à vapeur, brûleurs, convoyeurs | Create (laiton), Rechiseled: Create | Laiton d'abord à la fonderie Overgeared, puis au mélangeur ; moteur à vapeur avec des plaques d'**acier** ; mécanisme de précision avec un rouage forgé de bonne qualité |
| **4. Logistique et mine** | Colis, frogports, stock, gisements | Create 6, Factory Logistics, Ore Excavation | Foreuse COE avec tête forgée ; gisements finis par biome |
| **5. Rail** | Première ligne, gares, poste ferroviaire | Steam 'n' Rails, Bells & Whistles, Power Loader (trains) | Rails et bogies en acier ; essieu forgé ; plan d'ingénieur « chemin de fer » trouvé en exploration |
| **6. Combustion** | Pétrole, distillation, locomotives diesel | Diesel Generators (+ Crafts & Additions en option B) | Bloc moteur forgé en acier ; carburant expédié en bocaux ou cuves |
| **7. Aviation** | Montgolfière, dirigeable, avion à hélice | Create Aeronautics + Sable | Brûleur à air chaud au laiton ; pales d'hélice forgées de qualité **excellente** ; plan « aérostat » trouvé dans une épave |
| **8. Colonies lointaines** | Avant-postes, survivants, commerce | Easy NPC, Create Villager Labor (à tester), OPAC, boutiques Create 6 | Règles des survivants (0004), claims limités |

Chaque âge ouvre un chapitre du **carnet du colon** ([3.7](#37-guider-sans-quêtes)).

**Recettes à modifier** (liste de départ) : `create:andesite_alloy`, `create:mechanical_press`, `create:mechanical_mixer`, `create:precision_mechanism`, `create:steam_engine`, `create:fluid_tank`, `create:track`, `create:railway_casing`, `create:controls`, bogies `railways:*`, foreuse et détecteur de COE, moteurs `createdieselgenerators:*`, hélices, paliers, brûleurs et ballons `aeronautics:*`, outils et armures vanilla en métal (suppression), échanges de villageois du même genre.

## 3.5 Électricité : rester mécanique par défaut

- **Option A, retenue par défaut** : bois et eau, andésite, laiton, vapeur, **diesel**, aviation. L'énergie se transporte par arbres de transmission et par trains de carburant. Chaque colonie produit sa propre énergie : c'est de la géographie, et cela renforce les colonies.
- **Option B** : électricité tardive avec Crafts & Additions. Les lignes électriques traversent le paysage, une silhouette de civilisation dans la brume. Le risque est que l'énergie cesse d'être un problème local. Si on la prend : fils coûteux, pas de bobine Tesla, alternateur moins efficace.
- À trancher après avoir joué l'âge 6.

## 3.6 Colonies et survivants

**Concept (décision 0004)** : les villages vanilla sont des ruines vides ([2.3](02-exploration.md#23-structures--rareté-silence-ruines)). On découvre des survivants rares, on les ramène (laisse, charrette Icy's, train, dirigeable, Quark « Villagers Follow Emeralds »), et ils deviennent la main-d'œuvre d'une colonie. Une colonie lointaine est un avant-poste de joueurs relié par rail ou par air, peuplé de survivants et protégé par des claims.

| Mod | Rôle | Dispo | Statut proposé |
|---|---|---|---|
| **Easy NPC** | Survivants uniques scénarisés : dialogues, échanges écrits à la main (aucun Mending possible), apparence | oui (7.14.0, avec Easy NPC Core et Config UI) | recommandé |
| **Create Villager Labor** | Un villageois secouru s'assoit à un poste de travail Create et fait tourner une partie de la chaîne. Il remplace Villager Workers, absent en 1.21.1 (devenu Hired Workers, 1.21.8 seulement). Ce n'est pas un ouvrier autonome qui mine ou coupe du bois : le survivant travaille à l'usine. Ses recettes propres sont à équilibrer | oui (1.5.3.1) | branche `test/colonies` |
| **Open Parties and Claims** | Groupes et protection des avant-postes, sans carte Xaero. Chargement de chunks par claim mis à 0 (Power Loader reste seul) | oui (0.32.7) | recommandé |
| Villagers Reborn | Villageois à apparence humaine : survivants crédibles | oui (1.0.9b) | optionnel |
| ~~Villager Recruits~~ | Escortes de convoi | absent (Forge 1.20.1 au plus) | retiré |

**Écartés** :
- MineColonies : pas de fichier 1.21.1 sur Modrinth (seulement Forge 1.18.2), donc exclu par la règle « Modrinth seulement ». En plus : citoyens générés par la mairie (contraire aux survivants rares), raids, poids serveur, style imposé. Une branche d'essai n'est pas possible tant qu'il n'est pas publié sur Modrinth.
- Guard Villagers : villages vivants.
- Taterzens : Fabric seulement.
- MCA Reborn : reproduction, contraire à 0004.

**Règles KubeJS des survivants** :
- reproduction des villageois bloquée ;
- `minecraft:mending` retiré des tags `tradeable` et `on_traded_equipment` (en 1.21, les enchantements sont des données) ;
- échanges d'outils et d'armures en métal retirés (forgeron d'outils, armurier, forgeron d'armes) ; on garde les échanges qui **achètent** des produits de ferme ou d'atelier : le survivant devient un débouché ;
- marchand ambulant désactivé (`doTraderSpawning false`).

## 3.7 Guider sans quêtes

FTB Quests n'existe que sur CurseForge, Heracles et Odyssey n'ont pas de version 1.21.1. Je propose un **carnet du colon** sous **Modonomicon** (déjà présent comme dépendance), avec un chapitre par âge : Forge, Atelier, Vapeur, Logistique, Rail, Combustion, Aviation, Colonies. Chaque chapitre s'ouvre par un **succès personnalisé** en datapack. Les Ponder de Create et de Kaleidoscope Cookery servent de tutoriel en jeu. Aucun mod à ajouter. Better Advancements est optionnel pour l'affichage.

## 3.8 Le Nether : fermé par défaut

Ce point n'avait pas été tranché et touche la civilisation : le Blaze Burner de Create (chauffe du mélangeur, donc laiton et acier en masse) se remplit en capturant un blaze vivant, et le Nether est un raccourci de voyage de 1 pour 8 qui contourne le rail et l'aviation.

**Défaut proposé : Nether fermé** (portails inertes, comme l'End), avec des recettes de remplacement :
- Blaze Burner : recette KubeJS avec un « cœur de braise » forgé chez Overgeared (forge + charbon + bloc de magma) ;
- quartz : vérifier si Create en produit déjà dans l'Overworld (broyage de la diorite), sinon recette au broyeur ;
- moules et alliages « Nether » d'Overgeared : réassigner à des matériaux de l'Overworld, ou couper ;
- netherite : supprimée (l'acier et la qualité Overgeared sont le haut de gamme).

C'est la décision qui change le plus de recettes : elle figure en tête des décisions à valider ([8](08-mise-en-place.md#84-décisions-à-valider-ensemble)).

## 3.9 Réglages à appliquer

| Mod | Réglage | Raison |
|---|---|---|
| Create Connected | Copycats désactivés avec migration automatique vers Copycats+ ; Kinetic Battery désactivée ; Linked Transmitter à discuter ; `Vessel Boiler Stress Multiplier` et `Vessel Max Level` à équilibrer | Pas de doublon, pas d'énergie portable |
| Create Ore Excavation | Tous les gisements en mode fini ; listes blanches de biomes ; tête de foreuse forgée (tag `#createoreexcavation:drills`) | Les gisements s'épuisent, la géographie compte |
| Power Loader | Chargeurs sur trains et engins autorisés ; au sol, laiton seulement, nombre limité par joueur | L'entretien des installations garde un sens |
| OPAC | Chargement de chunks par claim = 0 ; nombre de claims limité ; ne pas installer Xaero | Power Loader reste le seul chargeur |
| Diesel Generators | Vérifier si le pétrole est un gisement du mod ou un fluide injectable par COE | Une seule logique de gisements |
| Easy NPC | Survivants écrits en datapack (dialogues, échanges) | Survivants uniques |
| Overgeared | Recettes d'outils vanilla en métal retirées (config ou KubeJS) ; modèle d'acier injecté dans les loots de survivants | Overgeared seul fabricant d'outils |
