# Recherche : pilier « Civilisation » (Fallow, 1.21.1 NeoForge, Create 6.0.10)

> Périmètre : artisanat → atelier → industrie → rail → aviation → colonies.
> Base retenue (registre du 2026-10-10, 46 mods) : Overgeared 1.6.19, Create 6.0.10, Aeronautics 1.3.2 + Sable 2.0.5, Steam 'n' Rails (port NeoForge 0.3.0-beta.2), KaleidoscopeCookery-Automation, Supplementaries et Quark (essentiel seulement), Modonomicon et Patchouli (déjà là comme dépendances), KubeJS. À tester : Copycats+, Connected, Integrated Farming, Power Loader, IDAS.
> **Limites de la recherche** : modrinth.com, l'API Modrinth et curseforge.com sont bloqués en accès direct. Le **quota WebSearch commun à la session (200 requêtes) a été épuisé** en cours de recherche. Les disponibilités viennent donc des extraits de recherche (pages de versions Modrinth, miroirs BoxToPlay et Mado qui recopient Modrinth) et de GitHub. « ✔ Modrinth » veut dire qu'un extrait de page de version Modrinth en 1.21.1 NeoForge a été vu. « ~ miroir » veut dire que seul un miroir l'affiche. « ? » veut dire non vérifié.

---

## 0. Ce qu'il faut retenir

1. **Create 6 fournit déjà la moitié du pilier « civilisation »**, sans aucun addon. Les colis, adresses, boîtes postales, frogports, *stock ticker* et *stock keeper* forment le stockage en réseau. Les boutiques en nappe (*table cloth*) gèrent la vente entre joueurs. Les boîtes postales posées aux gares transforment les trains en poste. Il faut donc écarter les mods de stockage « magique » (Tom's, Sophisticated), les mods de livraison par drone et les mods de courrier par ender : ils feraient doublon avec ce système et contourneraient le rail.
2. **Peu d'addons Create, chacun avec un rôle distinct** :
   - socle : **Copycats+** (construction, engins), **Connected** (mécanique fine, config à élaguer), **Factory Logistics** (fluides en colis), **Ore Excavation** (gisements à épuiser), **Bells & Whistles** (gares) ;
   - palier combustion : **Diesel Generators** ;
   - électricité : **Crafts & Additions** en option, à trancher.
   - écartés : Big Cannons, New Age, TFMG, Enchantment Industry, Hypertubes, Mobile Packages, Central Kitchen.
3. **Overgeared reste le métier de la qualité.** Create apporte la quantité. Règle KubeJS proposée : une machine Create ne produit jamais mieux qu'une pièce de qualité « moyenne ». Les pièces critiques (tête de foreuse, bogies, moteurs d'avion) demandent une pièce forgée à la main, de qualité minimale ou avec un plan maîtrisé.
4. **Minerai** : minerai vanilla raréfié et regroupé, pioche de prospection au début, puis gisements profonds et **finis** de Create Ore Excavation (API KubeJS complète). Un gisement épuisé oblige à repartir vers une nouvelle frontière : c'est le moteur des colonies lointaines.
5. **Colonies** : pas de MineColonies dans le socle. Ses citoyens apparaissent tout seuls (contraire à la décision 0004), il impose des raids et pèse lourd sur le serveur. Il garde une place en branche d'essai. Proposition : villages vanilla en ruines (datapack) ; survivants scénarisés avec **Easy NPC** ; villageois secourus transformés en ouvriers avec **Villager Workers** (dispo 1.21.1 ? non vérifié) ; avant-postes protégés par **Open Parties and Claims**.
6. **Quêtes** : FTB Quests n'existe que sur CurseForge, et Heracles/Odyssey n'a pas de version 1.21.1 trouvée. Proposition : **un carnet Modonomicon** (déjà dans le pack), dont les pages s'ouvrent par des **succès personnalisés** (datapack KubeJS). Cela n'ajoute aucun mod.

---

## 1. Addons Create (sans empilement)

Compatibilité Aeronautics/Sable : aucune liste officielle n'existe. Le dépôt [Simulated-Project](https://github.com/Creators-of-Aeronautics/Simulated-Project/issues) ne compte aucun ticket ouvert nommant Copycats+, Connected, S'n'R ou C&A (page 1 de la recherche). Des correctifs tiers existent : [Create Aeronautics: Compatibility](https://www.curseforge.com/minecraft/mc-mods/create-aeronautics-compatability), qui corrige Bits 'n' Bobs, Etched, Sleep Tight…, et SableCat ([modrinth.com/mod/sablecat](https://modrinth.com/mod/sablecat)), qui répare les sous-niveaux corrompus et serait utile sur un monde permanent. Des addons de « poids » existent aussi pour Sable : Copycats+ Aeronautics Weight, Sable: Weighted – TFMG. On en déduit que **chaque bloc d'addon posé sur un engin doit avoir une masse définie** : point à tester.

| # | Mod (slug) | Dispo 1.21.1 NeoForge / Create | Côté, dépendances | Mécanique apportée | Conflits, config | Verdict |
|---|---|---|---|---|---|---|
| 1 | **Create: Copycats+** (`copycats`) | ~ miroir : 3.0.7+mc.1.21.1-neoforge (août 2026) ; Create 6 ([Mado](https://www.mado-hosting.com/en/minecraft-mods/copycats)) | CS, Create | Formes copiables (planches, marches, poutres, escaliers, vitres…) qui suivent les engins. Create 6.0.8 a corrigé leur rendu sur les engins. Sculpter la coque d'un dirigeable ou d'une locomotive avec n'importe quel bloc. | Fait doublon avec les copycats de Connected (voir #2) et avec FramedBlocks (#44, écarté). Prévoir un addon de poids pour Sable. | **Socle** |
| 2 | **Create: Connected** (`create-connected`, Vg5TIO6d) | ✔ Modrinth : 1.1.9+ pour Create 6.0.7+, dernière vue 1.3.2-mc1.21.1 ([GitHub](https://github.com/hlysine/create_connected)) | CS, Create | Ingénierie fine : embrayages (centrifuge, roue libre, surcharge), goupille de cisaillement (casse en surcharge, très bien en début de partie), frein, boîte de vitesses en laiton, silo vertical, cuve horizontale-chaudière, chute en laiton, catalyseurs de ventilateur, tableau de bord des sièges (utile en avion et en train). | **À désactiver** (bascule par fonction et par catégorie, voir [Configs](https://github.com/hlysine/create_connected/wiki/Configs)) : tous les **copycats** (« Automatic Copycat Migration » vers Copycats+), la **Kinetic Battery** (énergie portable qui contredit l'entretien des réseaux), à discuter : Linked Transmitter et Wildcard. Réglages serveur : `Vessel Boiler Stress Multiplier` et `Vessel Max Level` pour l'équilibre de la vapeur. Incompatible avec Extended Cogwheels. | **Socle (élagué)** |
| 3 | **Create Deco** (`create-deco`) | ~ miroir : 2.1.3 1.21.1 NeoForge (avril 2026) | CS, Create | Vocabulaire industriel : passerelles, barreaux, lampes, portes blindées, briques industrielles. C'est du contenu. | Recoupe Copycats+ et Rechiseled: Create. Prendre l'un **ou** l'autre. | Optionnel (préférer Rechiseled: Create, #23) |
| 4 | **Create: Bells & Whistles** (`bellsandwhistles`) | ~ v0.4.7-mc1.21.1 NeoForge selon un miroir ; Modrinth affiche 1.21.1, mais sa description ne cite que Fabric et Forge → **NeoForge sur Modrinth à confirmer** ([Modrinth](https://modrinth.com/mod/bellsandwhistles)) | CS, Create | Gares : quais, bordures, rambardes, portes de wagon, signalétique. C'est ce qui fait d'une voie une **gare**. | Compatibilité avec le port S'n'R à tester. | Recommandé (pilier rail) |
| 5 | **Create: Enchantment Industry** (`create-enchantment-industry`) | ? : 2.4.2 pour Create 6.0.10 NeoForge sur un miroir ; plusieurs projets et forks sur Modrinth | CS | Expérience liquide, enchantement mécanique, imprimante de livres | L'imprimante **duplique les livres enchantés** (contraire à 0004 : pas de Mending ni de contournement). Industrialise l'enchantement alors que le pack ne mise pas sur le combat. | **Écarté** |
| 6 | **Create Crafts & Additions** (`createaddition`, kU1G12Nn) | ✔ Modrinth : 1.5.8 pour Create 6.0.8 ; 1.5.11 ajoute la **compatibilité d'amarrage Aeronautics** et corrige un crash Aeronautics ([version](https://modrinth.com/project/kU1G12Nn/version/cKTFwyEF)) | CS, Create | Électricité FE : alternateur, moteur, **fils et pylônes** (énergie à distance), accumulateurs, laminoir (fils, tiges), brûleur à blaze liquide, biocarburant. | Change la logique « une roue par machine » : il faut des lignes électriques. Désactiver la bobine Tesla (recharge d'objets) et éventuellement le chargeur. | **Optionnel** (palier tardif, à trancher, §6) |
| 7 | **Create Factory Logistics** (QMGO17gm) | ✔ Modrinth : 1.6.0 1.21.1 NeoForge, client et serveur ([version](https://modrinth.com/project/QMGO17gm/version/4Drrlfu3)) | CS, Create 6 | **Fluides en colis** : bocal, embouteilleuse, jauge de fluide. On peut expédier eau potable, lait, huile ou carburant par train ou dirigeable. S'intègre à Thirst et au diesel. | Aucun connu. | **Socle** |
| 8 | **Create: Numismatics** (`numismatics`) | ✔ Modrinth : 1.0.19 NeoForge 1.21.1 pour Create 6.0.7 ; 1.0.20 sur un miroir | CS, Create | Pièces à valeurs (spur → sun), banques, distributeurs, **vendeurs**, **dépositaires** (péage, ticket de train), cartes. | Fait doublon avec les boutiques natives de Create 6 (qui acceptent n'importe quel objet comme prix). D'où vient la monnaie ? Proposition KubeJS : frapper des pièces à la presse à partir de métal, pour une monnaie adossée au métal. | **Optionnel** (si une économie entre colonies prend forme) |
| 9 | **Create Big Cannons** (`create-big-cannons`) | ✔ 5.11.x NeoForge 1.21.1, Create 6.0.7+. Attention : la 5.11.4 **casse les blocs de munitions existants** ([CurseForge](https://www.curseforge.com/minecraft/mc-mods/create-big-cannons/files/8169551/dependencies)) | CS, + Ritchie's Projectile Lib | Artillerie | Guerre, sans ennemis à combattre ; contraire à la DA (Supplementaries a déjà son canon désactivé). | **Écarté** |
| 10 | **Create: Diesel Generators** (`create-diesel-generators`, ZM3tt6p1) | ✔ Modrinth : 1.21.1-1.3.15 NeoForge ([version](https://modrinth.com/project/ZM3tt6p1/version/UoPH8lO1)) | CS, Create | Moteurs diesel (petit, modulaire, énorme), distillation (gros distillateurs en lot), carburants, pompe à pétrole. **Palier combustion** entre vapeur et aviation. Carburant pour locomotives (S'n'R gère le carburant liquide) et, à tester, pour les moteurs d'engin. | Recoupe TFMG et New Age (écartés). Vérifier dans le jar si le pétrole est un gisement propre au mod ou une ressource injectable par COE (#13). | **Recommandé** |
| 11 | **Create: New Age** (`create-new-age`, FTeXqI9v) | ✔ Modrinth : 1.1.7a+mc1.21.1 NeoForge ; 1.2.0 sur un miroir | CS | Électricité, **réacteur nucléaire**, solaire, aimants | Doublon de C&A, nucléaire hors DA | **Écarté** |
| 12 | **Create: The Factory Must Grow** (`create-tfmg`) | ~ 1.2.0 (1.21–1.21.11 NeoForge) sur un miroir | CS | Pétrole, acier, aluminium, électricité à tension, moteurs | Absorbe à lui seul acier, électricité et diesel. Doublon d'Overgeared (acier), de Diesel et de C&A : l'empilement interdit. | **Écarté** |
| 13 | **Create Ore Excavation** (`create-ore-excavation`, ResbpANg) | ✔ Modrinth : 1.21.1-1.6.4, puis 1.6.6–1.6.8 NeoForge sur un miroir | CS, Create | **Gisements cachés** (un par chunk), détecteur de gisement, foreuse et extracteur de fluide entraînés par rotation. Gisements **finis ou infinis** au choix. API KubeJS : `vein().placement().biomeWhitelist().veinSize().alwaysFinite()`, `drilling().drill().fluid().stress()`, `extracting()`, tag `#createoreexcavation:drills` ([GitHub](https://github.com/tom5454/Create-Ore-Excavation)) | Passer **tous les gisements en fini** dans la config. Le détecteur affiche « gisement / taille » sans coordonnées : compatible avec la vision. | **Socle** |
| 14 | COE Deposits (addon) | ~ 0.1.6 1.21.1 NeoForge ([SkinMC](https://skinmc.net/index.php/project/create-ore-excavation-deposits)) | CS, COE | Gisements multi-chunks (2–40 chunks, formes Perlin) : de vrais **bassins miniers** qui justifient une ville minière. | Jeune. Le Better Finder (radar) est à écarter s'il affiche des positions. | Optionnel (très intéressant) |
| 15 | **Create: Power Loader** (`create-power-loader`, wPQ6GgFE) | ~ 2.0.5-mc1.21.1 NeoForge, Create 6 | CS, Create | Chargeurs de chunks andésite (1×1) et laiton (jusqu'à 5×5), au sol, sur train ou sur engin, réglables côté serveur | Recoupe la fonction *forceload* d'OPAC. **Proposition** : autoriser seulement les trains et engins (les convois roulent quand personne n'est là) ; chargeur au sol en laiton seulement et coûteux. | Recommandé, sous conditions (à tester) |
| 16 | Create: Central Kitchen (btq68HMO) | ✔ 2.5.0 pour Create 6.0.10, mais **l'intégration KC a été retirée** au profit de Kaleidoscope Compat et KC Automation | CS | Automatisation de Farmer's Delight | FD est écarté | **Écarté** |
| 17 | Kaleidoscope Contraption | ✗ seulement 1.20.1 Forge, Create 6.0.8+ ([CurseForge](https://www.curseforge.com/minecraft/mc-mods/kaleidoscope-contraption)) | CS | Blocs de KC sur engins : **voiture-restaurant**, cuisine de dirigeable | Pas de version 1.21.1 | À surveiller |
| 18 | Create: Hypertubes (`hypertube`, ATDdrG1y) | ✔ 0.2.4 Modrinth ; 0.6.0 sur CurseForge seulement | CS | Tubes de transport rapide pour joueurs | Quasi-téléportation : contourne le rail | **Écarté** |
| 19 | Create: Trading Floor (WROfLLvn) | ✔ Modrinth : 3.0.16 NeoForge 1.21.1 | CS | Comptoir qui fait commercer automatiquement les villageois | Les survivants deviennent des distributeurs : contraire à 0004 | **Écarté** |
| 20 | Create: Interiors (`interiors`) | ? : 0.6.1 NeoForge 1.21.1 selon ModDex | CS | Fauteuils et banquettes de train, sièges sur engins | Poids sur Sable à vérifier | Optionnel (aménagement des wagons) |
| 21 | Create: Mobile Packages (1vXRfEHZ) | ✔ 1.21.1 NeoForge, Create 6.0.9+ | CS | Robo-abeilles livreuses, **stock ticker portable** | Livraison par les airs et accès au stock à distance : contourne le rail et l'aviation | **Écarté** (Create: Phantom aussi) |
| 22 | Create: Metallurgy (`create-metallurgy`) | ~ 1.0.3-1.21.1 pour Create 6.0.10 NeoForge | CS | Fonderie : fusion, mélangeur de fonderie, table et bassin de coulée | Doublon partiel des alliages et moules d'Overgeared | Optionnel, à étudier. Par défaut, préférer KubeJS (mélangeur chauffé de Create → alliages Overgeared) |
| 23 | **Rechiseled: Create** (E6867niZ) | ✔ Modrinth : 1.1.1 NeoForge 1.21.1, Create 6+ ([version](https://modrinth.com/project/E6867niZ/version/TzPaH8MY)) | CS, Rechiseled + Fusion | Variantes ciselées des blocs Create (industriel cohérent) | Voir #43 | Recommandé si Rechiseled est retenu |
| 24 | Aeronautics: Transmission & Linkage | ~ compatible Sable 2.0.x (juin 2026) | CS | Transmissions et tringleries entre le monde et l'engin | Jeune | Optionnel (aviation avancée) |
| 25 | SableCat / Aeronautics Compatibility | ✔ SableCat sur Modrinth (dispo 1.21.1 ?) | S/CS | Outils pour récupérer des sous-niveaux corrompus ou abandonnés ; correctifs d'autres mods | Le mod de compatibilité sera archivé quand Sable intégrera ses correctifs | Recommandé côté serveur après lecture du jar |

Rappel : **Create: Integrated Farming** est déjà « à tester ». Cuisine sur engin : à surveiller (#17).

---

## 2. Overgeared ↔ Create : métallurgie, minerai, prospection

### Ce qu'apporte Overgeared (pages du mod et miroirs)
- Enclume de pierre (sneak + clic droit sur de la pierre) puis enclume de forgeron ; forge avec un mini-jeu d'adresse.
- **Plans** (*blueprints*) : chacun porte un niveau de qualité, un nombre d'usages, une progression, un type d'outil et une **qualité finale maximale**. Les copier sur un plan vierge donne une copie de moindre qualité.
- **Acier**, palier complet entre fer et diamant. L'amélioration en acier se fait à la table de forgeron, avec un modèle trouvé dans des structures (→ à placer dans les ruines et les camps de survivants).
- Moules : argile (fragile, début) et Nether (tardif). Ils dupliquent des têtes d'outil en perdant de la qualité.
- Recettes `overgeared:alloy_smelting` (4 entrées) et `nether_alloy_smelting` (9 entrées).
- Addons communautaires : Overgeared x Epic Knights et OvergearedXSimplySwords ; un dépôt de recettes : [BlueAsclepias/overgeared-recipes](https://github.com/BlueAsclepias/overgeared-recipes). **Aucun pont Overgeared ↔ Create n'existe** : tout passera par KubeJS.

### Principe d'articulation : « l'artisan fait la qualité, l'usine fait la quantité »
| Étape | Overgeared (main) | Create (machine) | Règle KubeJS |
|---|---|---|---|
| Lingots chauds | Forge et marteau | Brûleur à blaze sous dépôt ou ventilateur de « chauffe » (tester) | Le chauffage industriel est permis ; la mise en forme de qualité reste manuelle |
| Plaques et têtes | Enclume, qualité selon le plan | Presse mécanique → **qualité « mauvaise » à « correcte » au plus** | Sortie de presse avec un composant de qualité fixé (clé exacte du composant à lire dans le jar) |
| Alliages | Fonderie d'alliage (4 entrées) : bronze, laiton, acier | Mélangeur chauffé, puis surchauffé | **Laiton** : d'abord à la fonderie Overgeared, ensuite au mélangeur (débit ×4). **Acier en masse** : mélangeur surchauffé (brûleur + combustible de blaze/diesel) |
| Pièces critiques | Forgées à la main, qualité ≥ « bonne » | — | Tête de foreuse COE, bogies et essieux de train, carter de moteur d'avion : recettes qui exigent l'**item forgé** (tag `fallow:forged_quality_good`) |
| Outils et armes | Seule source d'outils de qualité | Pas d'outils en métal fabriqués par Create (déploiement, assemblage séquencé) | Supprimer les recettes vanilla d'outils et d'armures en fer, or, diamant et acier (selon la config Overgeared) et les échanges de villageois correspondants |

### Minerai et prospection : « veines réelles »
| # | Mod (slug) | Dispo | Mécanique | Verdict |
|---|---|---|---|---|
| 26 | **Prospecting Picks** | ~ 1.2.1 MC 1.21.1 NeoForge (janv. 2026) ([Mado](https://www.mado-hosting.com/minecraft-mods/prospecting-picks)) | Pioche de prospection : sonde un rayon configurable autour du bloc de pierre frappé ; détecte les veines par leurs blocs de minerai brut ; minerais définis en config (tag `c:ores/*`). Conçue pour aller avec des amas **plus grands et plus rares**. | **Recommandé** (début de partie) |
| 27 | GeoVein | ? 1.21.1 NeoForge (ModDex) ; Modrinth non vérifié | Énormes gisements gradués : plus on approche du cœur, meilleur est le minerai | Optionnel. Fait doublon avec COE Deposits : prendre l'un ou l'autre |
| 28 | Advanced Ore Deposits | ~ 1.0.1 1.21.1 NeoForge | Blocs de gisement qui se régénèrent jusqu'à épuisement de leur réserve | Optionnel (moins bon que COE fini) |
| 29 | Ore Deposits (surface) | ~ 1.1 (1.21.1, multi-loader) | Affleurements en surface qui révèlent le minerai | Optionnel : indice visuel pour l'exploration (« je vois de la rouille sur la falaise ») |
| 30 | Create: Rock & Stone, Create: Ore Deposits | CurseForge / WIP « pas prêt pour la survie » | — | **Écartés** |

**Schéma proposé** :
1. Datapack : minerais vanilla dispersés réduits (≈ −60 %). On garde les grandes veines vanilla de cuivre et de fer (1.18+), qui deviennent la cible de la prospection. Dans No Man's Land, les affleurements (Ore Deposits, optionnel) servent d'indices.
2. Début : pioche de prospection, minage manuel, forge Overgeared.
3. Industrie : détecteur COE, puis foreuse à tête **forgée** (gate Overgeared). Gisements **finis** (`.alwaysFinite()`, taille ×5–8 de la base) et **whitelists de biomes** : le zinc dans les biomes humides, l'étain dans les montagnes… La géographie dicte où fonder les colonies.
4. Épuisement → nouveau gisement lointain → voie ferrée → colonie minière.

```js
// server_scripts/coe_veins.js (syntaxe 1.21 d'après le README COE)
ServerEvents.recipes(e => {
  e.recipes.createoreexcavation.vein('{"text":"Gisement de zinc"}', 'create:raw_zinc')
    .placement(64, 24, 91827).biomeWhitelist('#c:is_wet/overworld') // tag à vérifier
    .alwaysFinite().veinSize(4, 8).id('fallow:vein_zinc')
  e.recipes.createoreexcavation.drilling(
      [coeutil.processingOutput('create:raw_zinc', 1), coeutil.processingOutput('minecraft:gravel', 0.3)],
      'fallow:vein_zinc', 400)
    .drill('fallow:forged_drill_head').stress(512)
})
```

---

## 3. Colonies et PNJ (avec la décision 0004 « survivants »)

### Concept
Les villages vanilla deviennent des **ruines vides**. On découvre des **survivants rares et uniques** (ermite, ingénieur, réfugiés). On les **ramène** à cheval, en charrette (Icy's), en train ou en dirigeable, guidés par le module Quark « Villagers Follow Emeralds ». Ils deviennent la **main-d'œuvre** d'une colonie. Les « colonies lointaines » sont des avant-postes de joueurs reliés par rail ou par air (mine sur un gisement COE, comptoir, aérodrome), peuplés de survivants et protégés par des claims.

| # | Mod (slug) | Dispo 1.21.1 NeoForge | Mécanique | Conflits | Verdict |
|---|---|---|---|---|---|
| 31 | **Easy NPC** (`easy-npc`) | ~ 7.9.0 1.21.1 NeoForge (Mado) ; fichier `easy_npc_bundle-neoforge-1.21.1-6.25.0.jar` vu sur CurseForge ; Modrinth probable, non vérifié | PNJ scénarisés : dialogues, **échanges définis à la main**, apparence, actions et conditions, sans redémarrage. Parfait pour les **survivants uniques** : l'ermite qui vend un plan Overgeared contre de la nourriture, l'ingénieur qui raconte l'effondrement. | Ne passe pas par les échanges de villageois : aucun Mending possible | **Recommandé** |
| 32 | **Villager Workers** (talhanation) | ? : dépôt [talhanation/workers](https://github.com/talhanation) actif, version 1.21.1 non confirmée | Un **villageois secouru devient ouvrier** (mineur, bûcheron, fermier, pêcheur, berger, éleveurs, marchand) avec zone de travail, coffre et besoin de nourriture. C'est l'usage exact de 0004. | Fermier ou éleveur automatiques : à encadrer (élevage exigeant voulu en 0002) | **Recommandé si la 1.21.1 NeoForge existe** (à vérifier en priorité) |
| 33 | Villager Recruits (`villager-recruits`) | ? : la page Modrinth existe ([modrinth.com/mod/villager-recruits](https://modrinth.com/mod/villager-recruits)), version 1.21.1 non confirmée | Recrues avec solde et nourriture, escortes, patrouilles, factions | Orienté armée ; recrues achetées contre émeraudes (désactiver leur apparition spontanée) | Optionnel : escortes de convoi seulement |
| 34 | MineColonies (`minecolonies`) | ✔ projet Modrinth ; 1.21.1 en **snapshot** (`minecolonies-1.1.1349-1.21.1-snapshot`) selon ModDex | Ville entière : bâtisseurs, université, guerriers ; plus Structurize, BlockUI, Domum Ornamentum, MultiPiston | Citoyens générés par la mairie (contraire aux survivants rares) ; **raids** de barbares et pirates (désactivables, clé à lire dans le jar) ; poids serveur ; style de construction imposé | **Écarté du socle**. Branche `test/minecolonies` si l'envie revient : citoyens initiaux 0, naissances désactivées, recrutement seulement par les **visiteurs de la taverne** (ce qui ressemble aux survivants) |
| 35 | Guard Villagers (`guard-villagers`) | ~ 2.4.10 1.21.1 NeoForge (le projet Modrinth Ap53t4bc est un port **Fabric**, piège) | Gardes en groupe de six dans les villages | Plus de villages vivants ; doublon de Recruits | **Écarté** |
| 36 | Taterzens | ✗ Fabric seulement (dernier Forge en 1.19) | — | — | **Écarté** |
| 37 | Villagers Reborn | ~ plusieurs versions 1.21.1 NeoForge (miroir) | Remplace les modèles des villageois par des **humains** (fonctions vanilla conservées) | Cohérence visuelle avec Easy NPC à vérifier | Optionnel, intéressant pour la DA (survivants crédibles) |
| 38 | MCA Reborn | ? (quota de recherche épuisé) | Villageois humains, mariage, **reproduction** | Contraire à 0004 | **Écarté** |
| 39 | **Open Parties and Claims** (`open-parties-and-claims`) | ✔ Modrinth : `neoforge-1.21.1-0.27.5` ([version](https://modrinth.com/mod/open-parties-and-claims/version/neoforge-1.21.1-0.27.5)) | Groupes et claims ; **les mods Xaero ne sont pas requis** (intégration optionnelle) ; gestion par commandes et interface | Ne pas installer Xaero. Régler la limite de claims et le *forceload* (cohérence avec Power Loader) | **Recommandé** (protection des colonies) |

### Mise en œuvre (sous-projet 3)
- **Villages en ruines** : remplacer dans le datapack les *template pools* `minecraft:village/*/villagers` par un pool vide, et/ou forcer les pools « zombie » des villages (bâtiments délabrés). Les zombies ne naissent pas puisque les hostiles sont bloqués. Autre option : vider `data/minecraft/worldgen/structure_set/villages.json` et générer ses propres ruines. Sparse Structures règle la rareté.
- **Pas de reproduction** :
```js
// server_scripts/survivants.js
NativeEvents.onEvent('net.neoforged.neoforge.event.entity.living.BabyEntitySpawnEvent', e => {
  if (e.parentA?.type == 'minecraft:villager') e.setCanceled(true)
})
```
- **Pas de Mending dans les échanges** : en 1.21 les enchantements sont des données. On redéfinit les tags `data/minecraft/tags/enchantment/tradeable.json` et `on_traded_equipment.json` avec `"replace": true`, sans `minecraft:mending`.
- **Pas de contournement d'Overgeared** : retirer les échanges du forgeron d'outils, de l'armurier et du forgeron d'armes qui vendent des outils et armures en métal, via `VillagerTradesEvent` (NeoForge, depuis `NativeEvents`) ou un addon KubeJS d'échanges (MoreJS 1.21.1 ? non vérifié). On garde les échanges qui achètent des produits de ferme ou d'atelier : le survivant devient un débouché pour la production.
- **Camps de survivants** : structures de datapack (2-3 PNJ Easy NPC + un villageois vanilla). Elles peuvent cacher le **modèle de forgeron d'acier** d'Overgeared.

---

## 4. Construction et décoration (ce qui en fait un métier)
Déjà retenus : Quark (essentiel), Supplementaries (essentiel), Copycats+ (§1).

| # | Mod | Dispo | Apport | Verdict |
|---|---|---|---|---|
| 40 | Amendments | ~ 2.1.7 1.21.1 NeoForge | Retouches vanilla (lanternes murales, chaudrons de mélange, panneaux suspendus) | **Optionnel, bas** : jugé « empilement » lors de l'écart d'Integrated Mowzie's ; ne pas relancer |
| 41 | Handcrafted | ~ 4.0.3 1.21.1 NeoForge, + Resourceful Lib (déjà présent) | 250 meubles, du contenu | Optionnel. Il existe un patch « Furniture with Create and Sable compat » (Mado) : les meubles sur engin posent problème sans lui |
| 42 | Macaw's (Roofs, Bridges, Doors, Windows…) | **Signal négatif** : Windows montré en 1.21.1 **Forge** et en 1.21.8 NeoForge seulement ; seuls des addons tiers (Aurora, Betters) existent en 1.21.1 NeoForge | Toits, ponts | À vérifier mod par mod. Probablement **indisponible** en 1.21.1 NeoForge sur Modrinth |
| 43 | **Rechiseled** (+ Fusion pour les textures connectées) | ✔ Rechiseled: Create 1.1.1 NeoForge 1.21.1 ; LittleBuilders 2 (modpack 1.21.1) embarque Rechiseled et Fusion | Ciseau : variantes de blocs, textures connectées ; addon Create cohérent | **Recommandé** (un seul mod de variantes ; préféré à Chipped, qui a des établis et des milliers de blocs) |
| 44 | FramedBlocks (`framedblocks`) | ✔ 10.5.3 sur Modrinth (10.6.2 sur un miroir) 1.21.1 NeoForge | Blocs à habiller | Doublon de Copycats+, qui suit les engins | **Écarté** |
| 45 | LittleTiles | ? (présent dans LittleBuilders 2 en 1.21.1) ; CreativeCore déjà là | Micro-blocs | Coût serveur, comportement sur engins inconnu | **Écarté** (ou optionnel sur une branche) |
| 46 | **Immersive Paintings** (`immersive-paintings`) | ✔ Modrinth : 0.7.8 NeoForge 1.21.1, CS ([version](https://modrinth.com/project/6txNkua3/version/DeOfrXC3)) | Tableaux d'images téléversées ; option pour couper les tableaux fournis avec le mod | Recommandé avec Camerapture |
| 47 | **Camerapture** (`camerapture`) | ✔ Modrinth : 1.10.6+mc1.21.1-neoforge ([version](https://modrinth.com/mod/camerapture/version/1.10.6+mc1.21.1-neoforge)) | Appareil photo : **les photos deviennent des objets** à encadrer. On rapporte des clichés d'expédition et on construit la mémoire du monde (complète Antique Atlas) | **Recommandé** (peu connu, très dans l'esprit) |
| 48 | Domum Ornamentum | Seulement avec MineColonies | Combinaison de textures à l'établi d'architecte | Écarté (suit MineColonies) |
| 49 | Baguettes de construction (Construction Wand, Building Gadgets) | — | Raccourci | **Écarté**. Le canon à schémas de Create, qui consomme les matériaux, est la « construction industrielle » ; la truelle de Quark suffit |

---

## 5. Stockage, logistique, économie, courrier, protection

| # | Mod | Dispo | Analyse | Verdict |
|---|---|---|---|---|
| — | **Create 6, natif** | inclus | Coffres-forts, silo (Connected), colis + packager + frogport + chaîne de transport, **stock ticker + stock keeper**, **jauges d'usine**, **boutiques en nappe** (prix dans n'importe quel objet), **boîtes postales aux gares** (les trains portent les colis par adresse) | **Socle**. C'est le système de stockage, de commerce **et de poste** |
| 50 | Sophisticated Storage | ~ Core 1.21.1 NeoForge, page Modrinth non confirmée | Améliorations magnétiques, vide, compactage, plus un addon Create Integration | **Écarté** (raccourcis « magiques », doublon des coffres-forts) |
| 51 | Tom's Simple Storage | ? | Terminal en réseau (magique) | **Écarté** (doublon du stock ticker) |
| 52 | Lightman's Currency | ~ 2.3.0.4g 1.21.1 NeoForge | Économie lourde avec terminaux et réseau | **Écarté** (Numismatics, plus « Create », en option) |
| 53 | **Envelope** (Pigeon Mail, `envelope`) | ✔ page Modrinth 1.21.1 ; 0.7.2–0.7.5 NeoForge 1.21.1 sur des miroirs ; **pas encore fini** selon le wiki | Lettres, paquets, **pigeons voyageurs**, boîtes aux lettres. Le courrier du début, avant le rail : écrire à un ami à l'autre bout du monde | Optionnel (à tester). À configurer : délai et portée réalistes ; poids d'un paquet limité, pour que le colis par train reste la voie du fret |
| 54 | Ender Mail Reborn | ✔ 1.21.1 NeoForge (port) | Livreur de l'Ender, adresse par coordonnées | **Écarté** (Ender et coordonnées) |
| 55 | Mighty Mail (MrCrayfish) | ? loader | Boîte postale vers n'importe quelle boîte | **Écarté** (instantané) |
| 56 | Post System | 1.21.1 NeoForge | Exige Lightman's et RegionGuard | **Écarté** |
| 39 | Open Parties and Claims | ✔ | voir §3 | **Recommandé** |

---

## 6. Progression technique et guidage

### Faut-il l'électricité ?
- **Option A, tout mécanique** (recommandée par défaut) : bois et eau → andésite → laiton → vapeur → **diesel** → aviation. L'énergie se transporte par arbres, trains de carburant (Factory Logistics) et chaudières locales. Chaque colonie produit sa propre énergie : c'est de la géographie, et cela renforce les colonies.
- **Option B, électricité tardive** avec **Crafts & Additions** (signal positif côté Aeronautics). Les **lignes électriques** traversent le paysage : une silhouette de civilisation dans la brume. Le risque est que l'énergie ne soit plus un problème local. Si on la garde : fils coûteux (laminoir + cuivre + caoutchouc ou résine), pas de bobine Tesla, rendement de l'alternateur réduit.
- Décision à prendre en jeu. New Age et TFMG restent écartés dans les deux cas.

### Quêtes
| Mod | Dispo | Verdict |
|---|---|---|
| FTB Quests | **CurseForge seulement** (FTB ne publie pas sur Modrinth) ; 2101.1.28 NeoForge sur CF | **Écarté** (contrainte Modrinth) |
| Heracles / Odyssey Quests | 1.20.1 ; port NeoForge non officiel pour 26.2+ seulement | **Écarté** (pas de 1.21.1) |
| **Modonomicon** (déjà présent) + **succès personnalisés** (datapack dans `kubejs/data/fallow/advancement`) | ✔ | **Socle** : un « carnet d'ingénieur ». Chaque chapitre (Forge, Atelier, Vapeur, Logistique, Rail, Combustion, Aviation, Colonies) s'ouvre sur un succès ; avec des schémas multiblocs. Aucun mod en plus. Ponder (Create, KC) sert de tutoriel en jeu |

---

## 7. Arbre de progression proposé

| Âge | Objectif de jeu | Mods | Matériaux clés | Verrous KubeJS (à écrire) |
|---|---|---|---|---|
| **0. Arrivée** | Cabane, feu, outils de pierre | vanilla, Supplementaries | pierre, silex, cuivre de surface | Pas d'outils en fer à l'établi. Succès « Premier abri » → chapitre 1 du carnet |
| **1. Forge** | Enclume de pierre → forge → premier fer forgé, bronze | **Overgeared**, Prospecting Picks | fer brut, cuivre, étain(?) et charbon ; bronze à la fonderie | Recettes d'outils et d'armures vanilla retirées ; plans de base trouvés dans les ruines ; la pioche de prospection exige une tête forgée |
| **2. Atelier** | Roue à eau, moulin, presse, mélangeur | **Create** (andésite), **Connected** (goupille de cisaillement, embrayages), Copycats+ | alliage d'andésite, plaques de fer | `create:andesite_alloy` à la main avec un **clou ou rivet forgé** (Overgeared) ; presse et mélangeur demandent un « marteau d'atelier » forgé de qualité ≥ correcte |
| **3. Laiton et vapeur** | Moteur à vapeur, brûleurs, premiers convoyeurs | Create (laiton), Rechiseled: Create | **laiton** (Overgeared → mélangeur), **acier** Overgeared | Moteur à vapeur et chaudière : plaques d'**acier** à la place de l'or ; mécanisme de précision : rouage forgé « bonne » qualité. Succès « Pression » |
| **4. Logistique et mine** | Colis, frogports, stock, gisements | Create 6, **Factory Logistics**, **Ore Excavation** (+ Deposits) | têtes de foreuse forgées, laiton, acier | Foreuse COE : `.drill('fallow:forged_drill_head')` ; gisements **finis**, liste blanche de biomes ; Integrated Farming éventuel ici |
| **5. Rail** | Première ligne, gares, poste ferroviaire | **Steam 'n' Rails**, **Bells & Whistles**, Interiors (opt.), Power Loader (trains seulement) | acier (rails, bogies), laiton | `create:track` et bogies S'n'R avec **acier** ; carter de train avec un essieu forgé ; boîte postale en gare = courrier entre colonies |
| **6. Combustion** | Pétrole, distillation, locomotives diesel | **Diesel Generators** (+ C&A en option B) | brut (gisement fluide COE ou pétrole du mod), acier, laiton | Moteurs diesel avec bloc moteur forgé en acier ; carburant expédié en bocaux ou cuves (Factory Logistics) |
| **7. Aviation** | Montgolfière, puis dirigeable, puis avion à hélice | **Create Aeronautics + Sable** (+ Transmission & Linkage en option) | laiton, acier, toile (lin de Supplementaries), carburant | Brûleur à air chaud au laiton (âge 3+) ; hélices et paliers de propulsion avec **pales forgées « excellente »** (vraie maîtrise de la forge) ; pierres flottantes (levitite ?) trouvées en exploration seulement |
| **8. Colonies lointaines** | Avant-postes, survivants, commerce | **Easy NPC**, Villager Workers (?), **OPAC**, boutiques Create 6 (+ Numismatics en option), Envelope (opt.), Camerapture et Immersive Paintings | nourriture (KC) comme salaire, survivants | Reproduction bloquée, tags d'enchantement sans Mending, échanges de métal retirés, villages en ruines ; claims limités par joueur |

### Recettes à modifier (liste de départ)
`create:andesite_alloy`, `create:mechanical_press`, `create:mechanical_mixer`, `create:precision_mechanism` (assemblage séquencé), `create:steam_engine`, `create:fluid_tank`, `create:track`, `create:railway_casing`, `create:controls`, les bogies de `railways:*`, la foreuse et le détecteur de COE, les moteurs `createdieselgenerators:*`, les hélices, paliers, brûleurs et ballons d'`aeronautics:*`, les fils `createaddition:*` (si option B), les outils et armures vanilla en métal (suppression), plus les échanges de villageois du même genre.

---

## 8. Incertitudes à vérifier dans les .jar
1. **Overgeared** : identifiant du composant de qualité (pour l'imposer en sortie de presse), types de recettes exposés à KubeJS (`overgeared:forging` ? `alloy_smelting`), config qui désactive les outils vanilla, présence d'étain et de bronze ou non, loot du modèle d'acier.
2. **Create Aeronautics** : nom et origine de la « magic floating rock » (levitite ?), identifiants des hélices et brûleurs, carburant accepté par les moteurs.
3. **Diesel Generators** : pétrole = gisement propre (chunks à huile) ou fluide ajoutable par COE (`extracting`) ? Identifiant du fluide.
4. **COE** : clé de config du mode fini par défaut ; tags de biomes valides pour la liste blanche avec NML et Biolith.
5. **Create Connected** : clés exactes de la *Feature Toggle* (copycats, kinetic_battery, linked_transmitter).
6. **Power Loader** : clés serveur par mode (sol, train, engin).
7. **S'n'R port** : compatibilité de Bells & Whistles et d'Interiors avec le port NeoForge 0.3.0-beta.
8. **Sable** : masse des blocs Copycats+, Connected et Interiors (faut-il un addon de poids ?).
9. **Villager Workers et Recruits** : existence d'une version 1.21.1 NeoForge sur Modrinth (**prioritaire** pour la partie colonies).
10. **Easy NPC** : présence sur Modrinth (bundle NeoForge 1.21.1), syntaxe des échanges et dialogues pour les fournir en datapack.
11. **Bells & Whistles** : le fichier NeoForge est-il bien sur Modrinth et pas seulement sur CurseForge ?
12. **Macaw's** : y a-t-il une seule build 1.21.1 NeoForge sur Modrinth ?
13. **Villages** : noms des template pools à vider (vérifier avec NML, qui pourrait modifier les villages) ; interaction avec Sparse Structures.
14. **OPAC** : clés `forceload` et limites de claims ; absence de dépendance Xaero.
15. **Envelope** : délai, portée et capacité des pigeons configurables ?
16. **Points non vérifiés faute de quota de recherche** : GeoVein, MCA Reborn, Tom's Storage, Chipped 1.21.1, Create Interiors sur Modrinth, MoreJS 1.21.1.

## Sources principales
[Create Connected (GitHub)](https://github.com/hlysine/create_connected) · [Configs](https://github.com/hlysine/create_connected/wiki/Configs) · [Create Ore Excavation (GitHub)](https://github.com/tom5454/Create-Ore-Excavation) · [Port S'n'R 1.21.1](https://github.com/Porters-of-Railways/Railway-1.21.1) · [Simulated-Project issues](https://github.com/Creators-of-Aeronautics/Simulated-Project/issues) · [C&A 1.5.x](https://modrinth.com/project/kU1G12Nn/version/cKTFwyEF) · [Factory Logistics 1.6.0](https://modrinth.com/project/QMGO17gm/version/4Drrlfu3) · [Diesel Generators 1.3.15](https://modrinth.com/project/ZM3tt6p1/version/UoPH8lO1) · [New Age](https://modrinth.com/project/FTeXqI9v/version/jKQGO6gz) · [COE](https://modrinth.com/mod/ResbpANg) · [Power Loader](https://modrinth.com/project/wPQ6GgFE) · [Central Kitchen 2.5.0](https://modrinth.com/project/btq68HMO/version/kmHFmmpV) · [Kaleidoscope Contraption](https://www.curseforge.com/minecraft/mc-mods/kaleidoscope-contraption) · [Hypertubes](https://modrinth.com/project/ATDdrG1y/version/nDtH1hZL) · [Trading Floor](https://modrinth.com/project/WROfLLvn/version/akJ4A9Zh) · [Mobile Packages](https://modrinth.com/mod/create-mobile-packages) · [Big Cannons](https://modrinth.com/mod/GWp4jCJj) · [Enchantment Industry](https://modrinth.com/project/create-enchantment-industry) · [Bells & Whistles](https://modrinth.com/mod/bellsandwhistles) · [Rechiseled: Create](https://modrinth.com/project/E6867niZ/version/TzPaH8MY) · [Immersive Paintings 0.7.8](https://modrinth.com/project/6txNkua3/version/DeOfrXC3) · [Camerapture](https://modrinth.com/mod/camerapture/version/1.10.6+mc1.21.1-neoforge) · [OPAC 0.27.5](https://modrinth.com/mod/open-parties-and-claims/version/neoforge-1.21.1-0.27.5) · [Envelope](https://modrinth.com/mod/envelope) · [MineColonies](https://modrinth.com/mod/minecolonies) · [Villager Recruits](https://modrinth.com/mod/villager-recruits) · [talhanation (GitHub)](https://github.com/talhanation) · [Guard Villagers (port Fabric)](https://modrinth.com/project/Ap53t4bc) · [Overgeared](https://modrinth.com/project/overgeared) · [Prospecting Picks](https://www.mado-hosting.com/minecraft-mods/prospecting-picks) · [COE Deposits](https://skinmc.net/index.php/project/create-ore-excavation-deposits) · [Copycats+](https://www.mado-hosting.com/en/minecraft-mods/copycats) · [FTB Quests NeoForge (CF)](https://www.curseforge.com/minecraft/mc-mods/ftb-quests-forge/files/8520042) · [Heracles port 26.2](https://modrinth.com/project/fx73ivjN) · [Aeronautics Compatibility](https://www.curseforge.com/minecraft/mc-mods/create-aeronautics-compatability) · [SableCat](https://modrinth.com/mod/sablecat)
