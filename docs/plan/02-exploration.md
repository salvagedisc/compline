# 2. Exploration : « je me demande ce qu'il y a là-bas »

Ce qui existe déjà : No Man's Land (worldgen et lieux d'ambiance), Sparse Structures, Antique Atlas 4 + Surveyor, Icy's Better Horses, Supplementaries (corde, poulie, panneaux posés à la main, encre antique), Quark (essentiel). Ce chapitre ajoute la **traversée façon Breath of the Wild**, des **lieux de civilisation perdue**, des **récompenses qui sont des savoirs**, et une **navigation sans coordonnées**.

Détail mod par mod, preuves et clés : [recherche/research-exploration.md](recherche/research-exploration.md).

## 2.1 La boucle de jeu

1. **Voir.** Depuis la base, une silhouette dans la brume : un sommet, une épave de dirigeable, une colonne de fumée de feu de camp (un survivant).
2. **S'orienter sans coordonnées.** Soleil et étoiles, boussole en main, panneaux et drapeaux posés par les joueurs, nom de la région à l'entrée (Traveler's Titles).
3. **Traverser.** À pied avec une jauge d'endurance (Paraglider), en escaladant les rebords (ParCool bridé), en planeur depuis les hauteurs et les courants ascendants des feux de camp, à cheval, en bateau, plus tard en train et en dirigeable.
4. **Camper.** Sac à dos (eau, outils), sac de couchage, feu (chaleur Cold Sweat et point d'envol).
5. **Découvrir** un lieu rare et silencieux : sanctuaire NML, maison abandonnée, village en ruine, vestige Create, épave, camp de survivant.
6. **Rapporter** : la carte dessinée en marchant, des photos, un plan d'ingénieur, un journal, des orbes, un survivant.
7. **Convertir** à la base : le plan débloque une recette, le survivant devient un habitant, les orbes deviennent de l'endurance. Les nouvelles machines permettent d'aller plus loin.

## 2.2 Mods proposés

| Mod | Rôle dans la boucle | Dispo 1.21.1 NeoForge Modrinth | Statut proposé |
|---|---|---|---|
| **Lootr** | Coffres générés par joueur : en multijoueur, le second arrivé trouve encore quelque chose | oui (1.11.37.122) | socle |
| **Paraglider** | Planeur, roue d'endurance, courants ascendants, statues et orbes façon BotW. Remplace l'Elytra supprimée | oui (21.1.5, `paragliders`) | socle |
| **ParCool!** (bridé) | Accroche et escalade de rebord, saut de mur, roulade, vault, ramper. Endurance fournie par Paraglider | oui (4.0.0.5) | recommandé |
| **Create Aeronautics Structures** | Maisons d'ingénieur en ruine (un ingénieur pacifique y vit : un survivant), épaves de dirigeables et de bateaux, ballons brisés | oui (bêta) | recommandé |
| **Create: New Beginnings** | Vestiges Create ajoutés aux villages sans écraser les pièces vanilla | oui (0.1.1) | recommandé |
| **Create: Let The Adventure Begin** | Petites structures « Create » autonomes (datapack) | oui (4.0.3) | recommandé, à trier : ne garder que les plus sobres |
| **MCG's RPG Campsites** | 8 campements rares dont 7 avec un villageois de métier : le concept « survivants » de la décision 0004 | oui (1.0.0) | recommandé |
| **Cinch's Abandoned Houses** | Maisons isolées à différents stades de délabrement | oui (2.0.1) | recommandé |
| **Exposure** | Photographie argentique : pellicule, développement en chambre noire, tirage. Un métier et une preuve de découverte | oui (1.9.18) | recommandé |
| **Traveler's Backpack** | Sac porté avec deux réservoirs d'eau et des emplacements d'outils | oui (10.1.38) | recommandé |
| **Comforts** | Sacs de couchage (sans changer de point de réapparition) et hamacs | oui (9.0.3) | recommandé |
| **Traveler's Titles** (+ YUNG's API) | Nom du biome en titre à l'entrée. Compense le biome masqué par `reducedDebugInfo`. C'est un affichage, pas des structures YUNG's | oui (5.1.3) | recommandé |
| **Better Archeology** | Fossiles à assembler, artefacts à identifier : le pinceau devient un métier, la colonie a un musée | oui (1.3.4) | à tester |
| **GUI Compass** | Direction affichée en haut de l'écran seulement avec une boussole dans l'inventaire, sans coordonnées (`guiCompassFormat = "F"`). Remplace Dead Reckoning, qui n'existe qu'en Fabric | oui (5.0, natif) | recommandé ; repli NeoCompass, toujours affiché |
| **AA4 Atlas** | L'atlas devient un objet fabriqué qu'on porte et qu'on peut perdre | oui (1.1.2, via Connector comme Antique Atlas) | recommandé |
| **Streams Reflowing** | Ruisseaux qui suivent le relief, courant réel sur l'eau vanilla, cascades. Semble s'ajouter à NML sans Terralith | oui (2.13.1) | **branche `test/streams-nml` en priorité** (voir 2.6) |
| **Distant Horizons** + **Separate Sable Render Distance** | Silhouettes lointaines dans la brume | déjà « à tester » | branche `test/distant-horizons` (réglages en [6](06-technique-et-serveur.md#64-distant-horizons)) |

**Optionnels** (à montrer en capture avant de décider) : CTOV (ruines de villages plus variées), Towers of the Wild Modded (tours BotW, compatibilité native Paraglider ; proche de Structory: Towers, écarté par goût), Create: Structures Arise et Create: Rustic Structures (si New Beginnings ne suffit pas), IDAS (déjà « à tester »), Antique Trains (réseau Create sur l'atlas), The Athenaeum et Explorer's Journal (non vérifiés).

**Écartés** :
- Sophisticated Backpacks : améliorations de stockage en doublon de Create.
- Grappins (Hooked, Yori3o) : rendent la verticalité triviale.
- Wandering Villagers : fait apparaître des villageois près des joueurs, ce qui tue la découverte.
- SWEM : doublon d'Icy's Better Horses.
- Create: Dynamic Village : CurseForge seulement.
- Camerapture : doublon d'Exposure, qui apporte en plus un vrai métier (voir [3](03-civilisation.md)).

## 2.3 Structures : rareté, silence, ruines

**Principe** : aucune structure ne produit de son, de lumière forte ni de créature hostile. Le seul signal visible de loin est la fumée du feu de camp d'un survivant.

Réglages Sparse Structures (format des surcharges par structure à lire dans le .jar) :

| Groupe | `spreadFactor` |
|---|---|
| Global | 3 |
| Petits lieux NML (menhirs, puits, sanctuaires) | 1,5 à 2 |
| Cinch's Abandoned Houses | 3 |
| Villages abandonnés | 4 |
| New Beginnings, Let The Adventure Begin | 4 à 5 |
| Camps de survivants, épaves et maisons d'ingénieur | 6 |

**À désactiver** (structure set vide en datapack) : strongholds (déjà décidé), chambres d'épreuves, avant-postes de pillards, manoirs. **À vider** plutôt que supprimer : monuments océaniques (ruines sous-marines sans gardiens), cabanes de sorcière. **Villes anciennes** : je propose de les garder **vides** (sans Warden ni capteurs sculk hurleurs) comme lieu étrange du grand fond ; à valider en jeu. Les épaves, ruines océaniques et puits du désert restent tels quels.

**Villages en ruines** (concept 0004), datapack maison plutôt qu'un mod :
1. Surcharger `minecraft:village/<type>/town_centers` pour ne garder que les centres de la variante « zombie », qui mènent aux maisons délabrées.
2. Annuler en KubeJS les zombies villageois placés dans ces pièces (le filet de [5.1](05-faune-et-ambiance.md#51-supprimer-les-monstres-vanilla)). Option : 1 chance sur 100 de les remplacer par un survivant terré.
3. Retirer les golems de fer et les chats de village.
4. Injecter avec Lithostitched (déjà présent) des pièces maison : statue de Paraglider dans une ruine (= sanctuaire), vestige Create, coffre-journal.

**Camps de survivants** (MCG's RPG Campsites) : retirer le portail en ruine, remplacer le coffre de loot de village par un journal, filtrer les cartes d'exploration du cartographe, ajouter une **botte de foin sous le feu de camp** pour que la fumée monte haut.

## 2.4 Récompenses : des savoirs plutôt que des diamants

| Récompense | Où on la trouve | Ce qu'elle débloque | Outil |
|---|---|---|---|
| **Plans d'ingénieur** (objets KubeJS, un par palier) | Épaves Aeronautics, maisons d'ingénieur, vestiges New Beginnings | Un lot de recettes Create, Steam 'n' Rails ou Aeronautics ([3.4](03-civilisation.md#34-arbre-de-progression)) | `doLimitedCrafting` sur une liste restreinte + `player.unlockRecipes` |
| **Modèle de forge d'acier** (Overgeared) | Camps de survivants, ruines | L'amélioration acier | Loot modifier NeoForge `neoforge:add_table` |
| **Orbes spirituels** (Paraglider) | Uniquement sanctuaires et monuments NML | Échangés à une statue contre de l'endurance | Loot modifier + config Paraglider |
| **Journaux et lore** | Maisons, villages en ruine, camps (par joueur avec Lootr) | Récit de la civilisation disparue, indications vagues (« au-delà de la deuxième crête, vers le couchant ») | Livres écrits en datapack |
| **Photos** (Exposure) | Prises par le joueur | Primes en KubeJS si les données de vue le permettent (« photographier un Frostmaw ») | À vérifier dans le .jar |
| **Fossiles, artefacts** | Sites Better Archeology | Musée de la colonie | — |
| **Survivants** | Camps, maisons d'ingénieur, ruines | Un habitant qualifié : un métier dans la colonie ([3.6](03-civilisation.md#36-colonies-et-survivants)) | Règles KubeJS |
| **Succès de découverte** | Entrée dans un lieu ou un biome | Une page du carnet du colon, aucune ressource | Advancements datapack |

**Conflit à régler** : le module Quark « Automatic Recipe Unlock » (activé en 0005) débloque toutes les recettes et rend les plans inutiles. **Je propose de le désactiver.** Seules les recettes clés sont verrouillées ; le reste du livre de recettes fonctionne normalement.

## 2.5 Naviguer sans coordonnées

1. Gamerule `reducedDebugInfo true`, forcée au chargement du monde ([7](07-kubejs-et-datapack.md)). Elle masque XYZ, bloc, chunk et biome dans le F3.
2. Fuites à boucher : coordonnées à la boussole de Supplementaries (déjà coupées), nécrologie de GraveStone, plugin de coordonnées de Jade, infobulles d'Exposure, écran d'Antique Atlas, lignes F3 de Distant Horizons. À vérifier une par une au premier lancement.
3. Commandes `/locate`, `/tp`, `/waypoints` et `/landmarks` réservées aux opérateurs.
4. **Surveyor** : `globalSharing=false`. Chacun dessine sa carte ; le partage passe par la copie vers une carte vanilla, qu'on rapporte à la base.
5. **Antique Atlas** : atlas requis dans l'inventaire pour ouvrir la carte. Ne marquer automatiquement que les monuments NML, les épaves et les camps ; les petites ruines restent une découverte personnelle.
6. **GUI Compass** : `guiCompassFormat = "F"` (direction seule, jamais X, Y, Z), `mustHaveCompassInInventory = true`, `compassPositionIsCenter = true`. Config client, livrée par Default Options.
7. Rien n'empêche techniquement un ami d'installer une minimap côté client : c'est un pacte entre joueurs, à écrire dans le README.

## 2.6 Rivières : Streams Reflowing sur No Man's Land

La décision 0001 opposait NML à Terralith + Streams Reflowing. La recherche montre que Streams Reflowing **creuse ses ruisseaux indépendamment de la source de biomes** et pose un courant sur l'eau vanilla. On pourrait donc avoir **l'ambiance de NML et les rivières de Streams** en même temps, sans Terralith.

Test à mener sur `test/streams-nml` avant la prégénération :
- double lit avec les rivières de NML et de Biolith ?
- navires Sable et bateaux vanilla dans le courant : poussés, ignorés ou instables ?
- les roues à eau de Create tournent-elles plus vite dans le courant ?
- coût de génération (Chunky sur un rayon de 1 000 blocs, mesuré avec spark).

Si le test est concluant, la décision 0001 devient « NML + Streams Reflowing » et la comparaison avec Terralith n'a plus lieu d'être.

## 2.7 Réglages à appliquer

| Mod | Fichier | Réglage | Raison |
|---|---|---|---|
| Paraglider | `paraglider-common.toml` | `features.heartContainers = false` | La survie reste environnementale |
| Paraglider | `paraglider-server.toml` | `spiritOrbs.raidGivesVessel = false`, `spiritOrbs.witherDropsVessel = false` | Pas de raids ni de Wither |
| Paraglider | idem | `spiritOrbs.spawnerSpiritOrbDrops = 0`, `spiritOrbs.trialSpiritOrbDrops = 0`, `spiritOrbs.ominousTrialSpiritOrbDrops = 0` | Orbes seulement dans les sanctuaires NML |
| Paraglider | idem | `stamina.runningAndSwimmingConsumesStamina = true` | La course et la nage deviennent un choix |
| Paraglider | idem | `paragliderDurability = 600` | Réparation chez Overgeared |
| Paraglider | idem | `updraft = true`, `features.villageStructures = true` | Feux de camp = points d'envol ; statues recopiées dans les ruines |
| ParCool | config serveur | Garder : accroche, escalade de rebord, saut de mur, roulade, vault, ramper. Couper : course murale, esquive, flips, saut chargé, cat leap, fast run. Endurance = Paraglider | Escalade BotW sans super-manœuvres |
| Lootr | `lootr-common.toml` | Pas de rafraîchissement ni de décroissance, protection contre la casse, conversion des structures de mods | Chaque découverte est unique |
| Traveler's Backpack | config commune | Capacités des variantes coupées ; sac de couchage sans point de réapparition ; vérifier que les réservoirs gardent la pureté de l'eau de Thirst | Pas d'effets magiques ni de gourde infinie |
| Surveyor | `surveyor.toml` | `globalSharing = false` | Carte personnelle |
| Antique Atlas | `antique-atlas.toml` | Atlas requis dans l'inventaire ; liste restreinte de structures marquées | La carte est un objet |
| Quark | `quark-common.toml` | Automatic Recipe Unlock = false | Les plans d'ingénieur ont un sens |
| MCG's Campsites | datapack | Portail en ruine retiré, botte de foin sous le feu, échanges du cartographe filtrés | Concept survivants |
| Create Aeronautics Structures | KubeJS | Échanges de l'ingénieur filtrés ; vérifier la nature des « robber balloons » | Pas de raccourci de progression |

Les noms de clés de Paraglider viennent du code source de la branche `release/1.21.1`. Les autres sont à confirmer dans les .jar.
