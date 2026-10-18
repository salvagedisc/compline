# Recherche : pilier Exploration (Fallow, 1.21.1 NeoForge)

> Rapport de chercheur, 2026-10-10. Tient compte du registre mis à jour (46 mods, décisions 0002 à 0007). **Retenus, donc pas reproposés** : Sparse Structures, Icy's Better Horses, Supplementaries (corde/poulie, panneaux de direction posés à la main, encre antique, marqueurs de carte ; localisation de structures désactivée), Quark (essentiel). **Écartés, donc pas reproposés** : YUNG's Better *, Structory et Structory: Towers, Explorify, Moog's Voyager, Trek, Adorabuild, When Dungeons Arise, Dungeons and Taverns, Explorer's Compass, Nature's Compass, Waystones, Terralith seul.
>
> **Limites de la recherche** : modrinth.com, l'API et le CDN Modrinth sont inaccessibles depuis cette session. La disponibilité vient d'extraits WebSearch (pages de versions Modrinth et miroirs comme BoxToPlay, SkinMC, mado-hosting, qui recopient les fichiers Modrinth) et de la lecture directe de sources GitHub (raw.githubusercontent). **Le quota de recherches web (200, partagé entre tous les agents) a été épuisé en cours de route.** Ces sujets n'ont donc pas pu être vérifiés : mods de tentes, escalade hors ParCool, bateaux, événements dynamiques, Distant Horizons côté serveur. Ils sont signalés comme tels.
>
> Légende de disponibilité : **oui** (fichier 1.21.1 NeoForge vu sur une page Modrinth ou un miroir Modrinth) · **probable** (indices forts, fichier Modrinth non vu) · **non vérifié** · **non** (CurseForge seul ou pas de build).

---

## 1. Synthèse

| # | Mod | Dispo Modrinth 1.21.1 NF | Côté | Verdict |
|---|---|---|---|---|
| 1 | Lootr | oui | CS | **socle** |
| 2 | Paraglider | probable (build 21.1.5 dans le source) | CS | **socle** |
| 3 | Create Aeronautics Structures | oui (1.21.1–1.21.11) | CS | **recommandé** (priorité 0004) |
| 4 | Create: New Beginnings | oui (0.1.1, 1.21.1–1.21.4) | CS | **recommandé** (priorité 0004) |
| 5 | Create: Let The Adventure Begin | oui (datapack 4.0.3) | S | **recommandé, à tester** (priorité 0004) |
| 6 | MCG's RPG Campsites | oui (1.0.0) | CS | **recommandé** (survivants) |
| 7 | Cinch's Abandoned Houses | probable | CS | **recommandé** |
| 8 | Exposure | oui (1.9.18) | CS | **recommandé** |
| 9 | Traveler's Backpack | oui (10.1.38) | CS | **recommandé** |
| 10 | Comforts | oui (9.0.3+1.21.1) | CS | **recommandé** |
| 11 | Traveler's Titles | probable (v5.1.3 + YUNG's API) | C | **recommandé** |
| 12 | Dead Reckoning | non vérifié (Fabric, via Connector) | C | **recommandé si dispo** |
| 13 | AA4 Atlas (addon Antique Atlas) | non vérifié | CS | **recommandé si dispo** |
| 14 | ParCool! | oui (4.0.0.5) | CS | **recommandé, bridé** |
| 15 | Better Archeology | oui (1.3.4, peut-être 1.3.8) | CS | **recommandé, à tester** |
| 16 | Streams Reflowing (avec NML) | oui (2.13.1, 1.21.1–1.21.5) | S | **à tester en priorité** |
| 17 | Create: Structures Arise | oui (176.49.48) | CS | optionnel |
| 18 | Create: Rustic Structures | oui (1.0.1) | CS | optionnel |
| 19 | IDAS (+ Integrated API) | oui | CS | optionnel (déjà « à tester ») |
| 20 | Integrated Villages | non vérifié côté NeoForge (fichier NeoForge 1.3.2 sur CurseForge) | CS | optionnel |
| 21 | ChoiceTheorem's Overhauled Village (CTOV) | oui (v3.6.3) | CS | optionnel |
| 22 | Towers of the Wild Modded | oui (1.0.9, 1.21–1.21.11) | S | optionnel (à montrer, risque de goût) |
| 23 | Antique Trains (addon AA4) | non vérifié | C | optionnel |
| 24 | The Athenaeum | non vérifié (CurseForge NF 1.21.1) | CS | optionnel |
| 25 | Explorer's Journal (KayDev) | non vérifié | CS | optionnel |
| 26 | NeoCompass / Compass Plus | oui (NF 1.21.1) | C | optionnel (repli de Dead Reckoning) |
| 27 | Distant Horizons | (déjà « à tester ») | CS | recommandé côté exploration, coût à mesurer |
| 28 | LootJS | non vérifié | S | optionnel (outil) |
| 29 | Hooked / Yori3o's Grappling Hooks | oui | CS | écarté pour l'instant |
| 30 | Sophisticated Backpacks | oui (3.26.6) | CS | écarté (doublon de TB) |
| 31 | Wandering Villagers | oui (1.0.1) | S | écarté (casse la rareté des survivants) |
| 32 | 80 % Abandoned Villages | non (1.21 / 1.20.6, pas de 1.21.1 vu) | S | écarté (on l'écrit nous-mêmes) |
| 33 | Create: Dynamic Village | non (CurseForge seul) | CS | écarté (Modrinth) |
| 34 | SWEM / SWEM Lite | non vérifié | CS | écarté (doublon d'Icy's) |
| 35 | Hoofprint | non vérifié | C | écarté (doublon de frontend AA4) |

---

## 2. Fiches détaillées

### A. Structures intégrant Create (recherche prioritaire de la décision 0004)

**Constat** : aucun mod trouvé ne **remplace** toutes les structures vanilla par des versions Create (comme en septembre). Le plus proche est un assemblage : *Create: New Beginnings* (ajouts non destructifs aux villages) + *Create Aeronautics Structures* (ruines d'ingénieurs, épaves) + *Let The Adventure Begin* (petites structures autonomes), avec nos propres remplacements dans les pools de villages, injectés par **Lithostitched** (déjà présent comme dépendance de Hybrid Aquatic).

#### 1. Create Aeronautics Structures
- Slug Modrinth : projet `PZIWeQhv` (compatibilité affichée 1.21.1–1.21.11). Preuves : https://modrinth.com/project/PZIWeQhv · https://www.curseforge.com/minecraft/mc-mods/create-aeronautics-structures
- Dispo : **oui** (Modrinth, NeoForge 1.21.1). Statut bêta (1.1.0-Beta-3.1 corrige un crash serveur à la lecture de la config), développement actif.
- Côté : CS. Dépendances : Create 6.0.10, Create Aeronautics ≥ 1.1.3, Sable ≥ 1.1.3, GeckoLib 4.8.4 (tous déjà dans le pack).
- Mécanique : ruines de **maisons d'ingénieur**, bateaux abandonnés, dirigeables flottants, ballons brisés (ruine non fonctionnelle) et épaves de dirigeables « à récupérer ». La maison d'ingénieur abrite un **marchand ingénieur pacifique** (`villager_create_engineer`) qui vend arbres, roues dentées, alliage d'andésite, presses. L'auteur dit : « atmosphère seulement, pas d'avantage de jeu ».
- Intégration : colle parfaitement au concept « survivants ». L'ingénieur est un survivant tout trouvé, à ramener dans la colonie. L'épave de dirigeable est une silhouette dans la brume, la récompense rêvée du pilier Exploration.
- Conflits et config : vérifier que l'ingénieur ne casse pas Overgeared ni la progression Create. Filtrer ses échanges en KubeJS (pas de presse ni de pièces avancées vendues tôt). Les « robber balloons » (ballons de brigands) pourraient être hostiles : à vérifier et désactiver si ce sont des mobs.
- KubeJS/datapack : loot des épaves remplacé par des **plans** (objets « savoir » qui débloquent les recettes Aeronautics, voir §3). Rareté via Sparse Structures (×5–6).
- **Verdict : recommandé**, à tester en premier.

#### 2. Create: New Beginnings
- Slug : projet `IAnP4np7`. Preuves : https://modrinth.com/mod/IAnP4np7 · https://www.curseforge.com/minecraft/mc-mods/create-new-beginnings
- Dispo : **oui** (0.1.1, NeoForge 1.21.1–1.21.4, mars 2025, compatible Create 6). Version précoce, peu mise à jour.
- Côté : CS. Dépendance : Create.
- Mécanique : ajoute de manière **non destructive** des structures Create aux villages vanilla (« remnants of the past » : vestiges, tables de loot propres, structures par type de village) pour enseigner les mécaniques Create.
- Intégration : c'est le plus proche de la demande « villages vanilla version Create ». Comme les villages deviendront des ruines (0004), il faudra vérifier si les pièces s'injectent aussi dans les pools « zombie » (abandonnés). Sinon, les recopier dans ces pools avec un modificateur Lithostitched `add_template_pool_elements`.
- Conflits : risque faible (ajout non destructif, compatible CTOV et autres). Inconnu : contient-il des contraptions assemblées (Create gère mal les contraptions générées) ?
- **Verdict : recommandé** (à tester avec les villages abandonnés forcés).

#### 3. Create: Let The Adventure Begin
- Slug : `create-let-the-adventure-begin` (datapack Modrinth, environ 1 M de téléchargements). Preuve : miroir BoxToPlay des datapacks Modrinth « create-let-the-adventure-begin ».
- Dispo : **oui** (4.0.3, 1.20.1–1.21.1, tous loaders car datapack).
- Côté : serveur (datapack ; à placer via packwiz dans `kubejs/data` ou via un mod de chargement global de datapacks).
- Mécanique : structures « inspirées de Create, équilibrées, proches du vanilla, qui récompensent l'exploration ».
- À faire : ouvrir l'archive et lister `data/*/worldgen/structure_set` et `loot_table` (liste non trouvée). Retirer les structures trop « donjon ».
- **Verdict : recommandé, à tester.** Risque de goût et de volume, comme pour Explorify : ne garder que la moitié la plus sobre.

#### 4. Create: Structures Arise
- Slug : `create-structures-arise`. Preuves : https://modrinth.com/project/9enMEvoc/version/HSECDqTu · miroir BoxToPlay « create-structures-arise ».
- Dispo : **oui** (176.49.48 NeoForge 1.21.1). Un changelog (174.47.46) exige **AnvilCore 10.1** : dépendance à confirmer dans le .jar.
- Mécanique : 27 à 28 structures Create (moulins, ateliers, etc.).
- Risque : volume (28 structures) contraire au principe « une structure est une découverte ». Qualité inégale selon les retours communautaires (non vérifié).
- **Verdict : optionnel.** À n'envisager que si New Beginnings et Let The Adventure Begin ne suffisent pas, avec désactivation par structure via Sparse Structures ou datapack.

#### 5. Create: Rustic Structures
- Slug : `create-rustic-structures`. Preuve : miroir BoxToPlay « create-rustic-structures ».
- Dispo : **oui** (1.0.0 et 1.0.1 NeoForge 1.21.1).
- Mécanique : structures fonctionnelles et décoratives en blocs Create, ton « rustique » (moulins, fermes mécanisées).
- **Verdict : optionnel.** Le ton rustique colle à la DA, mais doublon partiel avec New Beginnings : en choisir un après comparaison visuelle.

#### 6. IDAS et Integrated Villages
- IDAS : https://modrinth.com/mod/idas, NeoForge 1.21.1 (idas-1.13.5+1.21.1-neoforge, avril 2026). Exige Integrated API, Create, Quark, Supplementaries (tous présents sauf Integrated API). Configuration **par datapack** : spacing et separation dans les `structure_set`, et un datapack de configuration officiel existe.
- Integrated Villages : https://modrinth.com/project/integrated-villages. Le fichier `integrated_villages-1.3.2+1.21.1-neoforge.jar` existe sur CurseForge, mais la page Modrinth n'annonce que « Forge & Fabric » : **NeoForge sur Modrinth non vérifié**. Exige Integrated API, Create, Supplementaries.
- Problème : IDAS est orienté donjons et loot (RPG), et ses structures sont grandes, donc bruyantes. Integrated Villages ajoute des villages **peuplés**, l'inverse du concept « survivants ».
- **Verdict : optionnel.** IDAS reste « à tester plus tard ». Pour Integrated Villages, ne garder que si on peut forcer leurs variantes en ruines.

#### 7. Create: Dynamic Village
- 4 métiers Create (ingénieur mécanique, ingénieur hydraulique, mécanicien de train, mineur), 20 bâtiments. **CurseForge seul** (v0.7-1.21.1) : https://curseforge.com/minecraft/mc-mods/dynamic-village
- **Verdict : écarté** (absent de Modrinth). L'idée des métiers peut être reprise en KubeJS pour les survivants (métiers personnalisés).

### B. Ruines, villages abandonnés et survivants

#### 8. MCG's RPG Campsites
- Slug : `mcgs-rpg-campsites`. Preuves : miroir BoxToPlay des mods Modrinth (1.0.0, NeoForge 1.21.1) · https://www.curseforge.com/minecraft/mc-mods/mcgs-rpg-campsites
- Côté : CS. Blocs vanilla uniquement.
- Mécanique : **8 campements, dont 7 avec un villageois** (boucher, fermier, cartographe, clerc, forgeron d'outils, armurier, bibliothécaire) et 1 abandonné. Chaque camp a un coffre de loot de village ; l'un contient un portail en ruine. Plaines, taïga, savane. L'auteur recommande `/locate`, ce qui prouve leur rareté.
- Intégration : c'est **exactement le concept « survivants »** de la décision 0004. Un réfugié isolé avec son feu de camp, à ramener à la colonie (Quark « Villagers Follow Emeralds » est déjà activé).
- Config et datapack : retirer le portail en ruine et le coffre de loot de village, ou le remplacer par un journal (lore). Rareté ×4–6. Injecter une **botte de foin sous le feu de camp** (fumée de signal visible de très loin : le phare naturel du survivant, voir §4).
- Conflit : le cartographe vend des cartes d'exploration vanilla (cartes de structures). Les filtrer en KubeJS (cohérent avec 0006).
- **Verdict : recommandé.**

#### 9. Cinch's Abandoned Houses
- Slug : `cinchs-abandoned-houses`. Preuves : https://www.mado-hosting.com/minecraft-mods/cinchs-abandoned-houses (build NeoForge 1.21.x couvrant 1.21 et 1.21.1, version 2.0, mai 2026) · https://www.curseforge.com/minecraft/mc-mods/cinchcraft
- Dispo : **probable** (miroir de type Modrinth ; page Modrinth non vue).
- Mécanique : maisons isolées à différents stades de délabrement, loot de début de partie modeste. Petites, silencieuses : le « lieu » plutôt que le donjon.
- Intégration : loot remplacé par des journaux d'anciens habitants, des outils usés (Overgeared) et parfois un plan. Rareté ×2–3.
- **Verdict : recommandé.**

#### 10. Villages abandonnés forcés (datapack maison, pas de mod)
- Vanilla possède pour chaque type de village (plaines, désert, savane, taïga, neige) une variante **« zombie »** dont les centres mènent à des maisons délabrées (pools `minecraft:village/<type>/zombie/...`).
- Mise en œuvre :
  1. Écraser `data/minecraft/worldgen/template_pool/village/<type>/town_centers.json` pour ne garder que les centres `zombie`.
  2. Les pièces abandonnées contiennent des **zombies villageois** dans leurs NBT. Les hostiles vanilla étant supprimés, intercepter en KubeJS (`EntityEvents.spawned` sur `minecraft:zombie_villager`, annulé dans une structure). Éventuellement, 1 % de chance de les remplacer par un survivant terré.
  3. Bloquer la génération des golems de fer et des chats de village si l'on veut un vide total.
- Le datapack « 80 % Abandoned Villages » (1.21/1.20.6, non vu en 1.21.1) prouve le principe, mais il vaut mieux l'écrire soi-même.
- **CTOV** (https://modrinth.com/project/35BiK2KV, v3.6.3 NeoForge 1.21.1, configs de fréquence et d'activation des villages) apporterait des villages par biome plus variés, donc des ruines plus variées. Cela suppose de forcer aussi ses variantes abandonnées, s'il en a : à vérifier dans le .jar. **Optionnel.**

#### 11. Wandering Villagers
- 1.0.1 NeoForge 1.21.1, gamerule `DoWanderingVillagers` (nom non confirmé à la source).
- Fait apparaître des villageois près des joueurs : cela **supprime la découverte** du survivant. **Écarté.**

### C. Lieux, savoirs et récompenses non matérielles

#### 12. Better Archeology
- Projet `zCh7omyG`. Preuves : https://modrinth.com/project/zCh7omyG/version/XlJUda6G · miroir BoxToPlay (1.3.8 NeoForge 1.21.1 listée, non confirmée sur Modrinth).
- Dispo : **oui** (1.3.4 NeoForge 1.21.1, licence MIT).
- Mécanique (de mémoire, **à vérifier dans le .jar**) : fossiles assemblables en squelettes d'exposition, artefacts « non identifiés » à identifier sur une table d'archéologie, nouveaux sites (tunnels, ruines enfouies), outils d'excavation.
- Intégration : transforme le pinceau vanilla en **métier**, et les fossiles décorent un musée de colonie. Les artefacts peuvent porter le lore de la civilisation disparue.
- À désactiver : les artefacts qui donnent des bonus de combat ou des enchantements puissants (concurrents d'Overgeared).
- **Verdict : recommandé, à tester** (contenu exact à inventorier).

#### 13. The Athenaeum
- Preuve : https://www.curseforge.com/minecraft/mc-mods/the-athenaeum (Fabric/NeoForge 1.21–1.21.1). Dépendances : Architectury (présent), YACL (présent), ChimericLib. **Modrinth non vérifié.**
- Livres écrits générés par datapack et ajoutés aux tables de loot (bibliothèques de stronghold et manoirs : à rediriger, puisque les strongholds sont supprimés).
- **Verdict : optionnel.** Un datapack maison (`written_book` avec le composant `minecraft:written_book_content`, injecté par le modificateur NeoForge `neoforge:add_table`) fait la même chose avec **notre** lore, sans dépendance.

#### 14. Explorer's Journal (KayDev)
- Journal universel (journal intime, encyclopédie, atlas, statistiques) qui se remplit en découvrant mobs, biomes et structures. Détection dynamique par registres NeoForge. https://www.modpackindex.com/mod/207136/explorers-journals
- **Modrinth et 1.21.1 non vérifiés.** Risques : son onglet « Atlas » peut afficher des coordonnées et doublonne AA4.
- **Verdict : optionnel**, seulement si l'onglet atlas et les coordonnées sont désactivables.

#### 15. Exposure (photographie)
- Slug : `exposure`. Preuves : https://github.com/mortuusars/Exposure (branche 1.21.1, NeoForge, Forge, Fabric) · miroir BoxToPlay « exposure » (1.9.18 NeoForge 1.21.1).
- Côté : CS. Pas de dépendance lourde connue.
- Mécanique : appareils argentiques, pellicules noir et blanc ou couleur (16 vues, la couleur est plus dure à fabriquer et à développer), viseur (focale, vitesse, flash, grilles), accessoires (lampe de redstone en flash, longue-vue en objectif, vitre en filtre). Développement puis tirage dans un **lightroom** (papier + colorants), album photo.
- Intégration : en fait un **métier** (chimie, papier, colorants) et une **preuve de découverte** : la photo du sommet, de l'épave ou de la créature rare, accrochée au mur de la base. Le bassin de colorants peut passer par Create.
- Idée forte (à vérifier dans le .jar) : Exposure enregistre des données sur chaque vue (entités cadrées, biome, position, auteur) et déclenche des critères d'avancement. On peut alors créer des **primes photographiques** en KubeJS (« photographier un Frostmaw » → plan ou récompense). Vérifier aussi que **l'infobulle des photos n'affiche pas de coordonnées**.
- **Verdict : recommandé.**

#### 16. Lootr
- Slug : `lootr` (projet `EltpO5cN`). Preuve : https://modrinth.com/mod/lootr/version/1.21.1-1.11.36.107 (1.21.1-1.11.37.122 la plus récente vue).
- Dispo : **oui**. Côté CS.
- Mécanique : chaque coffre généré a un contenu **par joueur**. En multijoueur, celui qui arrive second trouve encore quelque chose.
- Config (`lootr-common.toml`, clés de mémoire **à vérifier**) : pas de rafraîchissement ni de décroissance (chaque découverte est unique), protection contre la casse activée, conversion des structures de mods activée.
- Interaction : FogRule (écarté) exigeait Lootr. Lootr rend aussi les loots maison par joueur (journaux de lore « personnels »).
- **Verdict : socle.**

### D. Traversée et endurance

#### 17. Paraglider (Tictim)
- Slug Modrinth : `paragliders` (page vue avec des builds 26.1). Le dépôt https://github.com/Tictim/Paraglider a une branche `release/1.21.1` : `gradle.properties` donne **mod_id `paraglider`, version 21.1.5, NeoForge 21.1.186+**. SkinMC (miroir Modrinth) liste des builds NeoForge 1.21.1 21.1.2 et 21.1.3.
- Dispo : **probable** (fichier 1.21.1 sur Modrinth non vu directement).
- Côté : CS.
- Mécanique BotW complète : planeur, **roue d'endurance**, **courants ascendants** sur les sources de vent (feux de camp, réglables par datapack), orbes spirituels, statues de la Déesse et statues cornues (échange orbes ↔ réceptacles d'endurance ou cœurs), statues ajoutées aux villages.
- Config lue dans le source (branche 1.21.1) :
  - `paraglider-common` → `features.spiritOrbGens`, `features.heartContainers`, `features.staminaVessels`, `features.villageStructures` (true par défaut).
  - `paraglider-server` → `updraft` (true), `paraglidingSpeed` (1.0), `paragliderDurability` (0 = sans usure), `paragliderInTowersOfTheWild`, `spiritOrbs.enderDragonDropsVessel`, `spiritOrbs.witherDropsVessel`, `spiritOrbs.raidGivesVessel`, `spiritOrbs.elderGuardianDropsSpiritOrb`, `spiritOrbs.spawnerSpiritOrbDrops` (1.0), `spiritOrbs.trialSpiritOrbDrops` (2.0), `spiritOrbs.ominousTrialSpiritOrbDrops` (4.0), `spiritOrbs.spiritOrbLoots` (true), `vessels.startingHearts` (10), `vessels.maxHeartContainers` (20), `vessels.startingStamina` (1000), `vessels.maxStaminaVessels` (10), `vessels.staminaIncreasePerVessel` (200), `stamina.paraglidingConsumesStamina` (true), `stamina.runningAndSwimmingConsumesStamina` (false).
- Réglages proposés pour Fallow :
  - `heartContainers = false` (pas de cœurs supplémentaires : la survie reste environnementale).
  - `raidGivesVessel = false`, `witherDropsVessel = false` (pas de Wither ni de raids voulus).
  - `spawnerSpiritOrbDrops = 0`, `trial*SpiritOrbDrops = 0` (les chambres d'épreuves seront supprimées ou vidées).
  - `stamina.runningAndSwimmingConsumesStamina = true` (la course et la nage deviennent un choix).
  - `paragliderDurability` ≈ 600 (réparation chez Overgeared).
  - `spiritOrbLoots = true`, mais orbes retirés des coffres vanilla et injectés **seulement** dans les sanctuaires et monuments de NML, via loot modifier.
  - `villageStructures = true` si l'on recopie les statues dans les pools de villages abandonnés : statue de la Déesse dans une ruine = sanctuaire.
- Recette du planeur : la verrouiller derrière Overgeared (toile + armature forgée), ou la trouver dans une épave Aeronautics.
- **Verdict : socle.** C'est le cœur de la traversée BotW et il remplace l'Elytra supprimée.

#### 18. ParCool!
- Projet `Fsvx2bdR`. Preuves : https://modrinth.com/project/Fsvx2bdR · miroir BoxToPlay (1.21.1-4.0.0.5 NeoForge).
- Dispo : **oui**. Côté CS.
- Mécanique : parkour (accroche aux rebords, escalade de rebord, roulade, saut de mur, glissade murale, course murale, esquive, ramper, plongeon…). Endurance interne, **ou endurance fournie par Paraglider** (compatibilité officielle 1.20.1 et 1.21.1, à régler dans la config).
- Bridage : désactiver côté serveur les actions « cinéma » (course murale horizontale et verticale, esquive, flips, saut chargé, cat leap, fast run), et garder **accroche au rebord, escalade de rebord, saut de mur, roulade de réception, vault, ramper**. On obtient l'escalade BotW sans supermanoeuvres. ParCool a aussi des **tyroliennes** (à comparer avec la corde et la poulie de Supplementaries ; en garder une seule logique).
- Conflit : animations du joueur possiblement en conflit avec d'autres mods d'animation (aucun dans le pack). Vérifier ParCool avec les contraptions Sable (marcher sur un dirigeable en mouvement).
- **Verdict : recommandé, bridé.**

#### 19. Grappins : Hooked (2.0.2 NF 1.21.1), Yori3o's Grappling Hooks (4.7.0-beta.2 NF 1.21.1)
- Disponibles, mais un grappin rend la verticalité triviale et fait doublon avec ParCool + Paraglider. BotW n'en a pas.
- **Verdict : écarté pour l'instant.** À rouvrir comme outil d'ingénieur tardif (forgé chez Overgeared) si l'escalade s'avère frustrante.

#### 20. Montures et bateaux
- **Icy's Better Horses** est déjà retenu (lien, sifflet, races, maison). Ne rien empiler dessus : SWEM et SWEM Lite sont **écartés** (doublons, et disponibilité Modrinth 1.21.1 non vérifiée).
- Bateaux : Create Aeronautics + Sable couvrent les navires. Aucun mod de bateau à ajouter (non recherché, quota épuisé). Streams Reflowing pousse les bateaux vanilla avec ses courants : le comportement des navires Sable dans ces courants reste à tester.

### E. Navigation sans coordonnées

#### 21. Masquer les coordonnées (vanilla)
- `/gamerule reducedDebugInfo true` masque XYZ, le bloc visé, le biome et les raccourcis F3+B, F3+G et les bordures de chunks, pour tous les joueurs. Préréglage dans le datapack de monde, ou en KubeJS `ServerEvents.loaded` → `server.runCommandSilent('gamerule reducedDebugInfo true')`.
- Compléments :
  - Option `showCoordinates` de vanilla : sans objet en Java.
  - Supplementaries : « coordonnées à la boussole » déjà désactivées (0006).
  - Commandes `/waypoints` et `/landmarks` de Surveyor : vérifier si elles impriment des coordonnées et restreindre leur niveau de permission.
  - Exposure : vérifier les infobulles.
  - Écran d'Antique Atlas : vérifier qu'il n'affiche pas les coordonnées au survol.
  - Écran de mort : vanilla n'affiche pas les coordonnées. La boussole de récupération suffit.

#### 22. Dead Reckoning (addon Surveyor)
- https://github.com/sisby-folk/dead-reckoning (ex-« Antique Atlas Compass HUD », inspiré de Compass Ribbon).
- **Ruban de boussole façon Skyrim** dans le HUD, avec les marqueurs Surveyor colorés. Chaque élément est désactivable. Le ruban peut n'apparaître **que si l'on tient une boussole**, ou en a une dans la barre ou l'inventaire.
- Dispo 1.21.1 sur Modrinth : **non vérifié**. Mod Fabric, il passerait par Connector comme AA4.
- Réglage proposé : afficher le ruban seulement **boussole en main**, points cardinaux seuls, **marqueurs désactivés**. Les marqueurs sur le ruban seraient des waypoints déguisés, ce qui est interdit. Au mieux, n'afficher que la marque « maison ».
- **Verdict : recommandé si disponible.** Repli : **NeoCompass** (HUD boussole NeoForge 1.21.1, client, sans coordonnées) ou **Compass Plus** (2.0.0, NeoForge 1.21.1 : HUD vanilla avec cardinaux, spawn, lodestone, dernière mort).

#### 23. AA4 Atlas et Antique Trains (addons Antique Atlas)
- **AA4 Atlas** donne à l'atlas un vrai identifiant d'objet et une **recette**. Combiné à l'option AA4 « l'atlas doit être dans l'inventaire pour ouvrir la carte », la carte devient un **objet** qu'on fabrique, qu'on porte et qu'on peut perdre. **Recommandé si disponible** (non vérifié).
- **Antique Trains** affiche vraisemblablement le réseau Create sur l'atlas (non vérifié). **Optionnel.**
- Réglages Surveyor (`config/surveyor.toml`) :
  - `globalSharing=false` : chacun dessine sa carte, et le partage passe par `/surveyor share` ou, mieux, par la **copie vers et depuis les cartes vanilla** que Surveyor sait faire. Ramener une carte remplie à la base devient un geste de cartographe.
  - Section `Builtins` : garder les marqueurs de portail désactivés (Nether à statuer) et le marqueur de tombe au choix.
- Config AA4 (`config/antique-atlas.toml`) : liste des structures marquées automatiquement (après les avoir vues). Marquer les monuments NML, les épaves et les camps, mais **pas** les petites ruines (la découverte doit rester personnelle).

#### 24. Traveler's Titles
- v5.1.3 NeoForge 1.21.1 + YUNG's API. Client. Preuves : https://www.curseforge.com/projects/1015155 · https://gdlauncher.com/mods/modrinth/travelers-titles (référence Modrinth).
- Dispo : **probable**.
- Affiche le **nom du biome en titre** en y entrant (façon BotW ou Skyrim), avec un son personnalisable par biome dans un resource pack (espace de noms `travelerstitles`). Compense le biome masqué par `reducedDebugInfo`.
- Ce n'est **pas** un « YUNG's Better * » : c'est un HUD, pas des structures. On peut renommer les biomes de NML en français évocateur via un resource pack.
- **Verdict : recommandé.**

### F. Campement et inventaire

#### 25. Traveler's Backpack
- Projet `rlloIFEV`. Preuve : miroir BoxToPlay (10.1.38 NeoForge 1.21.1).
- Dispo : **oui**. Côté CS.
- Sac porté et visible sur le dos, **deux réservoirs de fluide** (eau), grille d'artisanat, emplacements d'outils, **sac de couchage déployable**, variantes à capacités.
- Config à vérifier dans le .jar :
  - désactiver les **capacités** des variantes (effets magiques) ;
  - vérifier que le sac de couchage **ne fixe pas** le point de réapparition ;
  - vérifier que les réservoirs **conservent la pureté** de l'eau de Thirst Was Reclaimed (sinon c'est une gourde infinie qui contourne Thirst Canteen).
- Recettes : à verrouiller par Overgeared (cuir tanné, boucles forgées).
- **Verdict : recommandé.**
- **Sophisticated Backpacks** (3.26.6 + Sophisticated Core) est **écarté** : ses améliorations (aimant, compactage, alimentation, filtres) font de la logistique de stockage en doublon avec Create.

#### 26. Comforts
- Projet `SaCpeal4`. Preuve : https://modrinth.com/project/SaCpeal4/version/duBFOcde (9.0.3+1.21.1 NeoForge).
- Dispo : **oui**. Côté CS.
- **Sacs de couchage** (dormir sans changer de point de réapparition) et **hamacs** (passer du jour à la nuit).
- Doublon partiel avec le sac de couchage de Traveler's Backpack : garder Comforts pour la nuit sur le terrain, et désactiver celui de TB si la config le permet.
- **Verdict : recommandé.**

#### 27. Tentes, feux de camp, cairns
- **Non recherchés** (quota épuisé). Pistes sans mod :
  - feu de camp vanilla + courants ascendants Paraglider (le camp devient un point d'envol) ;
  - chaleur du feu dans Cold Sweat ;
  - cairns = pierres et dalles posées par les joueurs (Quark Hedges, Wooden Posts) ;
  - panneaux de direction de Supplementaries (déjà retenus) ;
  - drapeaux Supplementaries visibles de loin.
- À rechercher dans une prochaine passe : un mod de tentes 1.21.1 NeoForge sur Modrinth.

### G. Géographie et rivières

#### 28. Streams Reflowing (combiné à NML, pas en preset concurrent)
- Projet `oLS8HdJ1`, auteur NiceJohn (sans lien avec le Streams original de delvr). Preuves : https://modrinth.com/project/oLS8HdJ1/version/5STkQGAk · https://www.modpackindex.com/mod/157991/streams-reflowing
- Dispo : **oui** (2.13.1, NeoForge 1.21.1–1.21.5, mis à jour récemment). Côté serveur (génération).
- Mécanique : creuse des **ruisseaux qui suivent le relief**, s'accumulent en lacs et se jettent dans l'océan. **Courant directionnel sur l'eau vanilla** (pas de fluide custom depuis 2.6.0, donc les mods qui lisent l'eau restent compatibles). Bateaux et objets flottants emportés par le courant, y compris sur serveur dédié (2.8.4). Cascades. Argile dans les lits de ruisseau. Fusion propre avec les étangs (2.12.1).
- **Point clé pour la décision 0001** : Streams Reflowing semble être une couche de **creusement indépendante de la source de biomes**. Il pourrait donc s'ajouter à **NML** au lieu d'exiger Terralith. NML garderait l'ambiance, et Streams apporterait les rivières utiles à Create (roues à eau) et à la navigation. « Terralith seul » étant écarté, **NML + Streams Reflowing** est le premier test à faire.
- Risques :
  - interaction avec les rivières de NML et Biolith (doubles lits) ;
  - comportement des navires Sable/Aeronautics dans le courant : poussés, ignorés, ou instables ;
  - coût de génération (amélioré en 2.8.4) ;
  - les roues à eau de Create lisent-elles le courant de Streams ?
- **Verdict : à tester en priorité** (branche `test/streams-nml`).

#### 29. Distant Horizons (déjà « à tester »)
- Pour l'exploration, c'est le meilleur « je me demande ce qu'il y a là-bas » : silhouettes de montagnes et de tours dans la brume. À coupler avec ATMOSPHERICS (le brouillard lointain doit voiler les LOD, pas les révéler net).
- Risques Sable et coût serveur : non recherchés, quota épuisé.

#### 30. Towers of the Wild Modded
- Slug `totw-modded`. Dispo : **oui** (1.0.9, NeoForge 1.21–1.21.11, janvier 2026).
- Tours d'observation inspirées des tours Sheikah. Paraglider a une compatibilité native (`paragliderInTowersOfTheWild`, qui place le planeur dans les coffres des tours).
- Contre : proche de « Structory: Towers », écarté par goût, et des tours visibles de loin vont contre « rares et silencieuses ».
- **Verdict : optionnel**, à montrer en capture à l'utilisateur. Si retenu : rareté ×6, tours en ruine uniquement, coffre = planeur.

---

## 3. Récompenses d'exploration : savoirs plutôt que diamants

Outils de mise en œuvre, sans nouveau mod :
- Modificateur NeoForge `neoforge:add_table` (global loot modifier, datapack) pour **ajouter** des tables à des loots existants sans les écraser. Pour retirer, `LootJS` est plus confortable (Modrinth 1.21.1 **non vérifié**) ; à défaut, surcharge JSON.
- **Lithostitched** (déjà présent) : `add_template_pool_elements` pour injecter des pièces (survivants, statues Paraglider, vestiges Create) dans les pools de villages abandonnés et de NML sans les écraser. Modificateurs de structure sets.
- Vanilla `doLimitedCrafting` + KubeJS `player.unlockRecipe` / advancements.

Système proposé :
1. **Plans d'ingénieur** (objet KubeJS, un par palier : roue hydraulique, cuve, train, hélice, ballon…). Trouvés dans les épaves Aeronautics, les maisons d'ingénieur et les vestiges New Beginnings. Clic droit = débloque un lot de recettes Create ou Aeronautics, et les **plans d'Overgeared** pour les pièces forgées (voir le chercheur Civilisation). **Conflit** : le module Quark « Automatic Recipe Unlock », activé en 0005, débloque tout. Il faut le **désactiver** et activer `doLimitedCrafting` sur une liste restreinte (recettes clés seulement, pas tout le jeu).
2. **Orbes spirituels Paraglider** seulement dans les sanctuaires et monuments NML, échangés à une statue de la Déesse (trouvée en ruine) contre de l'endurance. C'est la boucle « shrine » de BotW.
3. **Journaux et lore** : livres écrits maison (encre antique de Supplementaries pour l'aspect), par joueur grâce à Lootr. Ils racontent la civilisation disparue et désignent vaguement d'autres lieux (« au-delà de la deuxième crête, vers le soleil couchant »), sans coordonnées.
4. **Primes photographiques** Exposure (si les données de vue le permettent).
5. **Survivants** : la récompense la plus précieuse. Un villageois qualifié qui débloque un métier dans la colonie.
6. **Fossiles et artefacts** (Better Archeology) pour le musée de colonie.
7. **Advancements de découverte** (prédicat `location` + `structure`, ou biome) : titre, ligne de journal, aucune ressource.

---

## 4. Système d'exploration cohérent

### Boucle de jeu
1. **Voir** : depuis la base, une silhouette dans la brume (Distant Horizons + ATMOSPHERICS), une colonne de fumée de feu de camp (survivant), une épave de dirigeable.
2. **S'orienter** sans coordonnées : soleil et étoiles, boussole en main (ruban Dead Reckoning, points cardinaux seuls), panneaux de direction posés par les joueurs, drapeaux et cairns, nom du biome à l'entrée (Traveler's Titles).
3. **Traverser** :
   - à pied, avec l'endurance Paraglider partagée avec ParCool : escalade de rebord, saut de mur ;
   - en planeur, depuis les hauteurs et les courants ascendants des feux de camp ;
   - à cheval (Icy's Better Horses), en bateau sur les rivières de Streams, plus tard en train ou en dirigeable.
4. **Camper** : sac Traveler's Backpack (eau, outils), sac de couchage Comforts, feu de camp (chaleur Cold Sweat et courant ascendant).
5. **Découvrir** un lieu rare et silencieux : sanctuaire NML, maison abandonnée, village en ruine, vestige Create, épave, camp de survivant.
6. **Rapporter** : la carte (AA4 dessinée à la marche, copiée sur carte vanilla pour la partager), des photos (Exposure), un plan, un journal, des orbes, un survivant.
7. **Convertir** à la base : le plan débloque une recette, le survivant devient un habitant, les orbes deviennent de l'endurance, les photos décorent le mur. La frontière recule, et les nouvelles machines (rail, dirigeable) permettent d'aller plus loin.

### Qui fait quoi
| Fonction | Mod |
|---|---|
| Carte (brouillard de guerre) | Antique Atlas 4 + Surveyor (+ AA4 Atlas) |
| Direction | boussole vanilla + Dead Reckoning (ou NeoCompass) |
| Repérage de région | Traveler's Titles |
| Lieux d'ambiance | No Man's Land (socle) + Cinch's Abandoned Houses + villages abandonnés forcés |
| Lieux « civilisation perdue » | Create Aeronautics Structures + Create: New Beginnings + Let The Adventure Begin (tri) |
| Survivants | MCG's RPG Campsites + ingénieur Aeronautics + injections maison |
| Loot multijoueur | Lootr |
| Traversée | Paraglider (endurance) + ParCool (bridé) + Icy's Better Horses + Sable |
| Campement | Traveler's Backpack + Comforts + feu de camp |
| Preuves et souvenirs | Exposure, Better Archeology |
| Rivières | Streams Reflowing (à tester sur NML) |

### Rareté des structures (Sparse Structures, `spreadFactor` global, défaut 2)
- Global : **3**.
- Surcharges par structure, valeurs de départ :
  - petits lieux NML : 1,5–2 ;
  - Cinch's : 3 ;
  - villages abandonnés : 4 ;
  - Create New Beginnings et Let The Adventure Begin : 4–5 ;
  - campements de survivants : 6 ;
  - épaves et maisons d'ingénieur Aeronautics : 6 ;
  - ToTW (si retenu) : 6.
- Le format exact des surcharges par structure dans la version 1.21.1 reste à lire dans le .jar.
- **Désactiver** (datapack `structure_set` vide ou `biome` tag vide) :
  - Strongholds et End : déjà décidés.
  - Chambres d'épreuves : spawners hostiles.
  - Avant-postes de pillards, manoirs : hostiles et RPG.
  - Monuments océaniques et cabanes de sorcière : vider ou supprimer (cf. 0004).
  - Villes anciennes : à statuer. Ambiance forte mais Warden : désactiver, ou garder vides comme lieu « étrange ».
- Garder les épaves de navire, les ruines océaniques et les puits du désert (silencieux).
- Règle de silence : aucune structure ne doit produire de son, de lumière forte ni de mob hostile. Le seul signal à distance autorisé est la **fumée de feu de camp** d'un survivant (botte de foin sous le feu, injectée dans les camps).

### Cacher les coordonnées (récapitulatif)
1. `reducedDebugInfo true` (gamerule forcée au démarrage du serveur).
2. Coordonnées à la boussole de Supplementaries : off (fait).
3. Vérifier AA4, Surveyor, Exposure et Explorer's Journal pour tout affichage de coordonnées.
4. Retirer `/locate`, `/tp` et `/waypoints` aux non-ops (niveau de permission).
5. Cartes d'exploration : cartographe vanilla, cartographe de MCG, Pathfinder Maps de Quark (off, fait), cartes d'aventurier de Supplementaries (off, fait).

### À désactiver ou configurer (récapitulatif)
- Paraglider :
  - `heartContainers=false`, `raidGivesVessel=false`, `witherDropsVessel=false` ;
  - `spawnerSpiritOrbDrops=0`, `trialSpiritOrbDrops=0`, `ominousTrialSpiritOrbDrops=0` ;
  - `runningAndSwimmingConsumesStamina=true`, `paragliderDurability>0` ;
  - orbes dans les loots NML uniquement.
- ParCool : seules l'accroche au rebord, l'escalade de rebord, le saut de mur, la roulade, le vault et ramper restent actifs ; endurance fournie par Paraglider.
- Quark : **désactiver Automatic Recipe Unlock** si le système de plans est adopté.
- Surveyor : `globalSharing=false`. AA4 : atlas requis dans l'inventaire, liste restreinte de structures marquées.
- Dead Reckoning : boussole requise, marqueurs off.
- Lootr : pas de rafraîchissement ni de décroissance, protection contre la casse.
- Traveler's Backpack : capacités off, sac de couchage sans point de réapparition, pureté de l'eau à contrôler.
- MCG Campsites : retirer le portail en ruine, filtrer les échanges du cartographe, ajouter une botte de foin sous le feu de camp.
- Create Aeronautics Structures : filtrer les échanges de l'ingénieur, vérifier les « robber balloons ».
- Villages : centres `zombie` uniquement, et suppression des zombies villageois en KubeJS.

---

## 5. Incertitudes à vérifier dans les .jar
1. **Paraglider** : présence du build 1.21.1 sur Modrinth (slug `paragliders`). Liste des sources de vent par défaut (datapack `paraglider/wind`?) et définition des structures de statues (villages et souterrains).
2. **ParCool** : nom exact des clés de limitation serveur et de l'option « endurance alternative = Paraglider ». Présence de la tyrolienne en 1.21.1.
3. **Create Aeronautics Structures** : nature des « robber balloons » (mobs ?), tables de loot, configuration de rareté, échanges de `villager_create_engineer`.
4. **Create: New Beginnings** : liste des pièces et pools ciblés (`minecraft:village/*/houses` seulement, ou aussi `zombie/*` ?), contraptions assemblées ou non.
5. **Let The Adventure Begin** : inventaire des `structure_set` et des loots. **Structures Arise** : dépendance AnvilCore.
6. **Sparse Structures 1.21.1** : format des surcharges par structure dans `sparsestructures.json5`.
7. **MCG's RPG Campsites** : identifiants des structures et du loot, et si les villageois ont des métiers verrouillés (pas de changement de profession au poste de travail).
8. **Cinch's Abandoned Houses** : présence sur Modrinth en 1.21.1 NeoForge.
9. **Traveler's Backpack** : clés de config (capacités, sac de couchage, réservoirs), et interaction des réservoirs avec la pureté de Thirst Was Reclaimed.
10. **Comforts** : le sac de couchage pose-t-il un point de réapparition ? Possibilité de désactiver celui de TB.
11. **Exposure** : données de vue (entités, biome, position), critères d'avancement exposés, et affichage éventuel de coordonnées dans l'infobulle.
12. **Better Archeology** : liste des artefacts, bonus de combat à couper, structures ajoutées.
13. **Dead Reckoning, AA4 Atlas, Antique Trains** : builds 1.21.1 (Fabric via Connector) sur Modrinth.
14. **AA4 et Surveyor** : coordonnées affichées sur l'écran de carte ? Permission de `/surveyor share` ? Copie vers cartes vanilla fonctionnelle sous Connector ?
15. **Streams Reflowing + NML** : génération combinée (lits doubles ?), navires Sable dans le courant, roues à eau de Create.
16. **Lootr** : clés exactes (`lootr-common.toml`).
17. **Traveler's Titles** : présence Modrinth 1.21.1 NeoForge (v5.1.3), et version de YUNG's API exigée.
18. **Quark** : effet de la désactivation d'Automatic Recipe Unlock sur le livre de recettes, et cohabitation avec `doLimitedCrafting`.

## 6. Non couvert (quota de recherche épuisé : prochaine passe)
Mods de tentes, escalade alternative, bateaux, mods d'« événements d'exploration », sentiers qui s'usent au passage, risques Distant Horizons + Sable côté serveur, LootJS 1.21.1, Explorer's Journal et The Athenaeum sur Modrinth.

## Sources principales
- Paraglider : https://github.com/Tictim/Paraglider (branche `release/1.21.1` : `gradle.properties`, `config/ServerConfig.java`, `config/CommonConfig.java`) · https://skinmc.net/project/paragliders · https://modrinth.com/mod/paragliders/version/Ix3nj2fZ
- ParCool : https://modrinth.com/project/Fsvx2bdR · https://www.curseforge.com/minecraft/mc-mods/parcool
- Lootr : https://modrinth.com/mod/lootr/version/1.21.1-1.11.36.107 · https://github.com/noobanidus/Lootr
- Exposure : https://github.com/mortuusars/Exposure · https://www.feed-the-mods.com/exposure-mod/
- Create Aeronautics Structures : https://modrinth.com/project/PZIWeQhv · https://www.curseforge.com/minecraft/mc-mods/create-aeronautics-structures
- Create: New Beginnings : https://modrinth.com/mod/IAnP4np7 · https://www.curseforge.com/minecraft/mc-mods/create-new-beginnings
- Create: Structures Arise : https://modrinth.com/project/9enMEvoc/version/HSECDqTu
- Create: Rustic Structures, Let The Adventure Begin, Traveler's Backpack, Sophisticated Backpacks, Better Archeology, MCG's RPG Campsites, Wandering Villagers : miroirs BoxToPlay des fiches Modrinth (www.boxtoplay.com/…/modrinth-mods/… et …/modrinth-datapacks/…)
- IDAS / Integrated Villages : https://modrinth.com/mod/idas · https://modrinth.com/project/integrated-villages · https://www.curseforge.com/minecraft/mc-mods/integrated-villages/files/7815319
- CTOV : https://modrinth.com/project/35BiK2KV · https://www.curseforge.com/minecraft/mc-mods/choicetheorems-overhauled-village/files/7653085
- Comforts : https://modrinth.com/project/SaCpeal4/version/duBFOcde
- Traveler's Titles : https://www.curseforge.com/projects/1015155 · https://gdlauncher.com/mods/modrinth/travelers-titles
- Antique Atlas 4 / Surveyor / Dead Reckoning : https://github.com/sisby-folk/antique-atlas · https://github.com/sisby-folk/surveyor · https://github.com/sisby-folk/dead-reckoning
- Streams Reflowing : https://modrinth.com/project/oLS8HdJ1/version/5STkQGAk · https://www.modpackindex.com/mod/157991/streams-reflowing
- Sparse Structures : https://modrinth.com/mod/sparsestructures · https://www.modpackindex.com/mod/47512/sparse-structures
- Towers of the Wild Modded : https://modrinth.com/project/m5T5xmUy/version/Rt96zVC1
- Cinch's Abandoned Houses : https://www.mado-hosting.com/minecraft-mods/cinchs-abandoned-houses
- The Athenaeum : https://www.curseforge.com/minecraft/mc-mods/the-athenaeum
- Explorer's Journal : https://www.modpackindex.com/mod/207136/explorers-journals
- Create: Dynamic Village : https://curseforge.com/minecraft/mc-mods/dynamic-village
- Grappins : https://www.boxtoplay.com/en/minecraft-hosting/minecraft-server/modrinth-mods/hooked-30557 · …/yori3os-grappling-hooks-39516
- reducedDebugInfo : https://feedback.minecraft.net/hc/en-us/community/posts/39283635611021
