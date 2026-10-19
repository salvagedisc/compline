# 1. Liste des mods proposée

> Remplacé par le [recentrage du 2026-10-11](10-recentrage.md) : ce tableau décrit la v1.

Vue d'ensemble de tous les mods, retenus et proposés. Chaque chapitre donne le rôle détaillé, les réglages et les preuves.

Légende du statut :
- **✅ retenu** : déjà dans `pack/mods/` (décisions 0001 à 0007) ;
- **➕ socle** : à ajouter, le plan s'appuie dessus ;
- **➕ recommandé** : à ajouter, apporte une mécanique claire ;
- **🧪 branche** : à tester sur une branche `test/*` avant décision ;
- **◻ optionnel** : à montrer, ajout possible plus tard.

Colonne « Modrinth » : vérifiée le 2026-10-10 sur l'API Modrinth. **oui** = fichier 1.21.1 NeoForge publié ; **Connector** = fichier multi-chargeurs de la famille Surveyor, chargé par Sinytra Connector ; **absent** = rien pour 1.21.1 NeoForge. Slugs, versions et dépendances : [9. Disponibilité sur Modrinth](09-disponibilite-modrinth.md).

## En chiffres

| | Mods |
|---|---|
| Déjà retenus | 46 (+ 24 dépendances) |
| Socle à ajouter | 16 |
| Recommandés à ajouter | 40 |
| À tester sur branche | 8 |
| Pack visé au lancement | environ 100 mods, plus leurs dépendances |

## Monde et exploration ([2](02-exploration.md))

| Mod | Statut | Côté | Modrinth | Rôle |
|---|---|---|---|---|
| No Man's Land | ✅ retenu | CS | oui | Worldgen |
| Sparse Structures | ✅ retenu | CS | oui | Espacement des structures |
| Antique Atlas 4 + Surveyor | ✅ retenu | C / CS | oui | Carte qui se dessine en marchant |
| Icy's Better Horses | ✅ retenu | CS | oui | Chevaux, charrettes |
| **Lootr** | ➕ socle | CS | oui | Coffres par joueur |
| **Paraglider** | ➕ socle | CS | oui | Planeur, endurance, courants ascendants |
| **ParCool!** (bridé) | ➕ recommandé | CS | oui | Escalade de rebord, saut de mur |
| **Create Aeronautics Structures** | ➕ recommandé | CS | oui | Épaves, maisons d'ingénieur |
| **Create: New Beginnings** | ➕ recommandé | CS | oui | Vestiges Create dans les villages |
| **Create: Let The Adventure Begin** (trié) | ➕ recommandé | S | oui | Petites structures Create |
| **MCG's RPG Campsites** | ➕ recommandé | CS | oui | Camps de survivants |
| **Cinch's Abandoned Houses** | ➕ recommandé | CS | oui | Maisons abandonnées |
| **Exposure** | ➕ recommandé | CS | oui | Photographie argentique |
| **Traveler's Backpack** | ➕ recommandé | CS | oui | Sac d'expédition |
| **Comforts** | ➕ recommandé | CS | oui | Sacs de couchage, hamacs |
| **Traveler's Titles** (+ YUNG's API) | ➕ recommandé | C | oui | Nom de la région à l'entrée |
| **GUI Compass** (remplace Dead Reckoning, absent) | ➕ recommandé | C | oui | Direction seule, boussole dans l'inventaire (repli : NeoCompass) |
| **AA4 Atlas** | ➕ recommandé | CS | Connector | L'atlas devient un objet |
| Streams Reflowing | 🧪 `test/streams-nml` | S | oui | Rivières à courant sur NML |
| Distant Horizons + Separate Sable Render Distance | 🧪 `test/distant-horizons` | CS | oui | Silhouettes lointaines |
| Better Archeology | 🧪 | CS | oui | Fossiles, artefacts |
| CTOV, Towers of the Wild Modded, Create: Structures Arise, Create: Rustic Structures, IDAS, Antique Trains | ◻ optionnel | — | oui (Antique Trains : Connector) | Voir 2.2 |

## Civilisation ([3](03-civilisation.md))

| Mod | Statut | Côté | Modrinth | Rôle |
|---|---|---|---|---|
| Overgeared | ✅ retenu | CS | oui | Forge, qualité, alliages |
| Create | ✅ retenu | CS | oui | Industrie |
| Create Aeronautics + Sable | ✅ retenu | CS | oui | Aviation |
| Steam 'n' Rails (port NeoForge) | ✅ retenu | CS | oui | Rail |
| Quark, Supplementaries | ✅ retenu (essentiel) | CS | oui | Confort, construction |
| **Create: Copycats+** | ➕ socle | CS | oui | Formes copiables sur engins |
| **Create: Connected** (élagué) | ➕ socle | CS | oui | Mécanique fine |
| **Create Factory Logistics** | ➕ socle | CS | oui | Fluides en colis |
| **Create Ore Excavation** | ➕ socle | CS | oui | Gisements finis |
| **Prospecting Picks** | ➕ recommandé | CS | oui | Prospection en début de partie |
| **Create: Bells & Whistles** | ➕ recommandé | CS | oui | Gares |
| **Create: Diesel Generators** | ➕ recommandé | CS | oui | Palier combustion |
| **Create: Power Loader** | ➕ recommandé | CS | oui | Chargement de chunks à l'énergie |
| **Rechiseled** + **Rechiseled: Create** (+ Fusion) | ➕ recommandé | CS | oui | Variantes de blocs |
| **Easy NPC** | ➕ recommandé | CS | oui | Survivants scénarisés |
| **Create Villager Labor** (remplace Villager Workers, absent) | 🧪 `test/colonies` | CS | oui | Survivants aux postes des chaînes Create |
| **Open Parties and Claims** | ➕ recommandé | CS | oui | Protection des colonies |
| Create Crafts & Additions, Numismatics, COE Deposits, Create: Interiors, Transmission & Linkage, SableCat, Villagers Reborn, Aeronautics: Winds and Weather | ◻ optionnel | — | oui | Voir 3.2 et 3.6 |

## Vie quotidienne ([4](04-vie-quotidienne.md))

| Mod | Statut | Côté | Modrinth | Rôle |
|---|---|---|---|---|
| Kaleidoscope Cookery + KC-Automation | ✅ retenu | CS | oui | Cuisine |
| Farm & Charm | ✅ retenu | CS | oui | Infrastructure de ferme |
| Ecliptic Seasons | ✅ retenu | CS | oui | Saisons |
| Cold Sweat, Thirst Was Reclaimed | ✅ retenu | CS | oui | Température, soif |
| **Kaleidoscope Compat** | ➕ socle | CS | oui | Fusion KC ↔ Farm & Charm |
| **Cattlestrophic!** | ➕ socle | CS | oui | Faim et vieillesse des animaux |
| **Salt's Animal Farm** | ➕ socle | CS | oui | Confort, abri, maladie |
| **Freeze It and Heat It** | ➕ socle | CS | oui | Nourriture qui gèle ou se gâte |
| **Animal Genders** | ➕ recommandé | CS | oui | Mâles et femelles |
| **Kaleidoscope Tavern** | ➕ recommandé | CS | oui | Vigne, brasserie, cave |
| **Thirst Canteen** | ➕ recommandé | CS | oui | Gourdes |
| **Tide** | ➕ recommandé | CS | oui | Pêche |
| **Nutritional Balance** | ➕ recommandé | CS | oui | Nutrition |
| **Hardcore Revival** | ➕ recommandé | CS | oui | Réanimation en coop |
| **AppleSkin** | ➕ recommandé | CS | oui | Aperçu faim et saturation |
| River Fishing | 🧪 contre Tide | CS | oui | Pêche simulation |
| Kaleidoscope Contraption, Kaleidoscope Orchard, Kaleidoscope Grilling, Fisherman's Trap, Butchery, Aquatic Breeding, Complicated Bees, Nourished, Sleep Quality | ◻ optionnel | — | oui | Voir 4.3 |

## Faune et ambiance ([5](05-faune-et-ambiance.md))

| Mod | Statut | Côté | Modrinth | Rôle |
|---|---|---|---|---|
| Naturalist, Alex's Mobs Continued, Hybrid Aquatic, Mowzie's Mobs | ✅ retenu | CS | oui | Faune |
| Critters and Companions, Untitled Duck Mod, Friends & Foes, Adorable Hamster Pets, Companions!, Ghost, Companion | ✅ retenu | CS | oui | Compagnons |
| ATMOSPHERICS, AmbientSounds, LambDynamicLights | ✅ retenu | C | oui | Brume, sons, lumière |
| Sound Physics Remastered, Subtle Effects, More Darkness | ✅ retenu | CS | oui | Réverbération, particules, nuits |
| **Cosy Critters & Creepy Crawlies** | ➕ recommandé | C | oui | Oiseaux et papillons de nuit |
| **Enhanced Celestials** | ➕ recommandé | CS | oui | Lunes (sans lune de sang) |
| **Simple Voice Chat** | ➕ recommandé | CS | oui | Voix de proximité |
| **Reactive Music** (remplace Presence Not Position, absent) (remplacé par True Adaptive Music, [0010](../decisions/0010-musique.md)) | ➕ recommandé | C | oui | Musique selon le lieu, l'heure et le danger ; songpack à choisir |
| Particle Rain | 🧪 | C | oui | Pluie en particules |
| Simple Clouds | 🧪 client seul | C | oui | Orages visibles de loin |
| Longwings | ◻ optionnel | CS | oui | Papillons à collectionner |

## Socle technique et serveur ([6](06-technique-et-serveur.md))

| Mod | Statut | Côté | Modrinth | Rôle |
|---|---|---|---|---|
| Sodium, ImmediatelyFast, ModernFix, FerriteCore, Lithium, Clumps, Structure Layout Optimizer, Chunky, spark | ✅ retenu | C / CS | oui | Performance, serveur |
| Entity Culling | ✅ retenu, **sous surveillance** | C | oui | Casse les engins Aeronautics : à régler ou retirer |
| Sinytra Connector + Forgified Fabric API | ✅ retenu (dépendance) | CS | oui | Pour Antique Atlas et Surveyor |
| KubeJS | ✅ retenu | CS | oui | Règles |
| **Neruina** | ➕ socle | CS | oui | Entités qui plantent mises en pause |
| **Simple Backups** | ➕ socle | S | oui | Sauvegardes dans le jeu |
| **JEI** | ➕ socle | CS | oui | Visionneuse de recettes |
| **In Control!** | ➕ socle | S | oui | Règles de spawn |
| **LootJS** | ➕ socle | CS | oui | Butins |
| **KubeJS Create** | ➕ socle | CS | oui | Recettes Create en KubeJS |
| **MoreJS** | ➕ recommandé | CS | oui | Échanges des villageois |
| **Almost Unified** | ➕ recommandé | CS | oui | Unification par tags |
| **Jade** | ➕ recommandé | CS | oui | Infobulles de blocs |
| **Mouse Tweaks**, **Controlling**, **Searchables** | ➕ recommandé | C | oui | Confort |
| **GraveStone Mod** | ➕ recommandé | CS | oui | Tombes |
| **Crash Assistant** | ➕ recommandé | C | oui | Aide après un crash |
| **Default Options** | ➕ recommandé | C | oui | Réglages par défaut |
| ProbeJS | outil de dev (pas dans `pack/`) | — | — | Autocomplétion KubeJS |
| EMI, Polymorph, Packet Fixer, First-person Model, Not Enough Animations, Shoulder Surfing Reloaded, FancyMenu + Drippy, Better Advancements, Sound Physics Aeronautics | ◻ optionnel | — | oui | Voir 6.2 |

## Nouveaux écartés (à ajouter au registre)

| Mod | Raison |
|---|---|
| Sophisticated Backpacks, Sophisticated Storage, Tom's Simple Storage | Stockage « magique » en doublon de Create 6 |
| Grappins (Hooked, Yori3o) | Verticalité triviale |
| Wandering Villagers | Villageois qui apparaissent près des joueurs |
| SWEM | Doublon d'Icy's Better Horses |
| Create: Dynamic Village, FTB Quests, FTB Backups 3, Animania Reborn, Serene Seasons API Stub, MineColonies | CurseForge seulement (pour 1.21.1) |
| Camerapture, Immersive Paintings | Doublons d'Exposure |
| Create Big Cannons, Create: Hypertubes, Create: Mobile Packages, Create: Trading Floor | Contraires à la vision |
| Create: New Age, Create: The Factory Must Grow, Create: Metallurgy, Create Deco | Doublons |
| Create: Enchantment Industry | Duplique les livres enchantés |
| Create: Central Kitchen | Intégration KC retirée |
| FramedBlocks, LittleTiles, Domum Ornamentum, baguettes de construction | Doublons ou raccourcis |
| Lightman's Currency, Ender Mail, Mighty Mail | Doublon ou instantané |
| Guard Villagers, Taterzens, MCA Reborn | Contraires au concept des survivants |
| FTB Chunks | Carte plein écran |
| Corail Tombstone, YIGD | Magie, téléportation ; Fabric |
| ServerCore | Risque de casser Create |
| Open Loader, Global Packs, Catalogue, Inventory Profiles Next, Emotecraft, Carry On, Sodium Extra, Embeddium | Inutiles, doublons ou incompatibles |
| Weather2, Fowl Play, Wisp & Willow, Dynamic Surroundings, True Darkness Biomes, Truly Dark | Conflit avec Ecliptic, risque de corruption, magie, doublons |
| Let's Do Vinery | Doublon de Kaleidoscope Tavern |
| ShelfLife, SpoiledZ, Food Spoiling, Diet, Hourglass | Hors Modrinth ou pas de 1.21.1 |
| Dead Reckoning, Biome Music, Configured | Fabric seulement en 1.21.1 |
| Villager Workers (Hired Workers), Villager Recruits, Presence Not Position | Pas de fichier 1.21.1 NeoForge sur Modrinth |
| Create Wells, Just a Well, First Aid New, Butchercraft, Barnyard Buddies, Animal Feeding Trough | Doublons, config lourde ou reproduction autonome |

**Liste noire** (incompatibles avec Aeronautics) : Does It Tick, Flowing Fluids, Leaves Be Gone, Flashback, Tpsum, Refurbished Furniture, Carry On.
