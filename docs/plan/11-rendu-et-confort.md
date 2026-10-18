# 11. Rendu et confort, sans shaders

> **Vérification du 2026-10-18 : le [chapitre 12](12-verification.md) corrige ce chapitre** (Easy Anvils, Leaves Be Gone, Dynamic Surroundings et Entity Culling sortent, Polytone sans lightmap, ordre des caméras, Overgeared, Kaleidoscope Compat, Monoliths).

> Ajouté le 2026-10-18, en réponse à l'équipe : améliorer le rendu sans pack de textures ni shaders, autour d'ATMOSPHERICS, avec des effets et des particules, et des mods de confort. Complète le [10.6](10-recentrage.md#106-la-finition-façon-realism-craft).
>
> Toutes les versions citées ont été vérifiées sur Modrinth le 2026-10-18 pour **NeoForge 1.21.1** (les packs de ressources pour 1.21.1).

## 11.1 Le principe : un mod par rôle

Sans shaders, le rendu se construit par couches. Chaque couche a **un seul** mod responsable, sinon deux mods se battent pour le brouillard ou les nuages et le résultat clignote.

| Couche | Responsable | Ce qu'il règle |
|---|---|---|
| Ciel, brume, nuages, étoiles | **ATMOSPHERICS** (déjà retenu) | Brouillard par biome, jour et nuit, nuages « Story Mode », comètes, étoiles |
| Lumière et couleurs | **Polytone** (nouveau) | Lightmaps (la couleur de la nuit, des torches), couleur de l'herbe, des feuilles et de l'eau par biome, couleur des particules |
| Lumière tenue en main | LambDynamicLights (déjà retenu) | La lanterne éclaire autour du joueur |
| Air et eau | **Particular Reforged** (nouveau) | Éclaboussures, cascades, feuilles qui tombent, poussière des grottes, ronds dans l'eau |
| Feu, fumée, vapeur, corps | Subtle Effects (déjà retenu) | Étincelles, fumée des cheminées, vapeur, poussière sous les pas, battement de cœur |
| Créatures | **Fresh Animations** (nouveau, pack de ressources) | Les créatures vanilla clignent des yeux, tournent la tête, respirent |
| Sons du monde | AmbientSounds + Sound Physics Remastered (déjà retenus) | Ambiance par biome, écho et étouffement |
| Sons des gestes | **Sounds** (nouveau) | 170 sons d'interface, d'objets et de blocs |

## 11.2 Les comparaisons

### Brume et nuages : ATMOSPHERICS seul

| Candidat | Verdict | Pourquoi |
|---|---|---|
| **ATMOSPHERICS** | Gardé | Il porte toute la mécanique de la brume (10.3) et ses presets se livrent dans un pack de ressources |
| Fog (IMB11) | Écarté | Brouillard par biome et par météo : exactement le rôle d'ATMOSPHERICS, les deux se contrediraient |
| Better Clouds | Écarté | Beaux nuages volumétriques, mais ATMOSPHERICS a déjà ses nuages « Story Mode », et sous la brume on les voit peu |
| Particle Rain | Toujours écarté | Il redessine la pluie et ajoute son propre brouillard pendant les averses ; il se battrait avec ATMOSPHERICS et avec la neige d'Ecliptic |
| Dynamic Surroundings (brouillard) | Fonction coupée | Voir plus bas : on garde ses pas et ses cascades, pas son brouillard |

### Lumière : Polytone plutôt qu'un nouveau moteur

| Candidat | Verdict | Pourquoi |
|---|---|---|
| **Polytone** | Retenu | Client seul, aucune dépendance, du même auteur que Moonlight, Supplementaries et Amendments (déjà dans le pack). Tout passe par un pack de ressources Fallow |
| Vibrancy | Écarté | Lumière colorée et ombres, mais développement en pause et remplace le moteur de lumière : risque avec Sodium 0.8 et Sable |
| Gleam, Colorful Lighting | Écartés | Même risque, très peu utilisés (moins de 8 000 téléchargements) |

Ce que Polytone fait pour Fallow, dans le pack de ressources `fallow-ambiance` (le même qui porte les presets ATMOSPHERICS) :
- **Les biomes de brume** : herbe, feuilles et eau un peu désaturées, une nuit légèrement bleue.
- **La clairière** (`fallow:clairiere`) : une lightmap plus chaude et des couleurs franches. Quand la brume se retire, la lumière change aussi : c'est le moment le plus important du pack.
- **La lune blanche** : une nuit plus claire et argentée (à prototyper : il faut voir si une lightmap peut suivre un évènement d'Enhanced Celestials ; sinon la lune garde ses propres couleurs).

### Particules : Particular Reforged

| Candidat | Verdict | Pourquoi |
|---|---|---|
| **Particular Reforged** | Retenu | Un seul mod pour l'eau (éclaboussures en 3D, cascades, ronds de pluie, bulles qui éclatent), les feuilles qui tombent, la poussière des grottes et les lucioles. Il déclare une **compatibilité avec Create Aeronautics et Sable** sur Modrinth. 5,3 millions de téléchargements |
| Subtle Effects | Gardé | Il couvre un autre terrain (feu, fumée, vapeur, corps). On coupe ses lucioles pour qu'il n'y en ait qu'une source |
| Falling Leaves Plus | Repli | Feuilles par espèce d'arbre (glands, cônes, flocons) et vent selon la météo, par Fuzs (Puzzles Lib déjà là). À prendre si Particular ne fait pas tomber les feuilles des arbres de No Man's Land |
| Falling Leaves, Ambient Leaves | Écartés | Doublons de Particular |
| Make Bubbles Pop, Cave Dust | Écartés | Particular le fait déjà (et Cave Dust n'existe pas pour NeoForge) |
| Wakes, Effective, Visuality, Eating Animation | Impossibles | Fabric seulement |
| Physics Mod | Écarté | Lourd, code fermé, ragdolls et blocs qui s'écroulent : hors ton, et risque avec Sable |

### Créatures : Fresh Animations entre

| Élément | Verdict | Détail |
|---|---|---|
| **Fresh Animations** 1.10.4 | Retenu (L'équipe l'aime) | Pack de ressources, avec **Entity Model Features** 3.3.12 et **Entity Texture Features** 7.2.6 |
| FA : Details, Objects, Emissive | Retenus | Détails sur les mobs, animations des objets (bateaux, wagons), yeux qui brillent la nuit dans la brume |
| **FA : Player Extension** | Retenu (choix de l'équipe, 2026-10-18) | Le joueur est animé dans le même style que les créatures. Il **remplace Not Enough Animations**, et donc First-person Model, qui en dépend |
| Fresh Moves | Écarté | Autre pack d'animation du joueur : doublon de Player Extension |

Trois points à vérifier sur `test/finition` :
1. **No Man's Land remodèle déjà** les cochons, les vaches et les calmars. Il faut voir lequel l'emporte. Si NML gagne, Fresh Animations anime tout le reste (moutons, loups, renards, chevaux, poulets, lapins…) et c'est déjà beaucoup.
2. **Le joueur en vue subjective** : sans First-person Model, on ne voit plus son corps en regardant vers le bas. **Real Camera** (bêta, demande Cloth Config) dessine le modèle du joueur en vue subjective, donc avec les animations de Player Extension. À essayer ; sinon on garde les bras vanilla.
3. **Les variantes d'animaux de Quark** (« Variant Animal Textures », actif) : ETF les gère d'habitude, mais il faut regarder si les textures restent bien placées sur les modèles de Fresh Animations.

La faune des autres mods (Naturalist, Alex's Mobs, Critters and Companions…) a déjà ses propres animations GeckoLib : Fresh Animations ne la touche pas.

### Corps et objets

| Candidat | Verdict | Pourquoi |
|---|---|---|
| **3D Skin Layers** | Retenu | Les couches du skin en relief. À vérifier avec le modèle du joueur de Player Extension |
| Real Camera | À tester | Voir son corps en vue subjective (point 2 ci-dessus) |
| **Camera Overhaul** | Retenu, réglé bas (11.7) | La caméra penche quand on tourne ou qu'on se décale, réagit aux sauts et aux chutes, se balance doucement à l'arrêt |
| MB-3D Items (`mb3d-items-pack`) | Déjà au 10.6 | Le bon identifiant Modrinth est `mb3d-items-pack` |
| Better Lanterns | Écarté | Amendments dessine déjà les lanternes en 3D ; les deux modèles se remplaceraient |
| Hold My Items | Impossible | Fabric seulement |

### Sons

| Candidat | Verdict | Pourquoi |
|---|---|---|
| **Sounds** (IMB11) | Retenu | 170 sons pour l'interface, les objets et les blocs : ouvrir un sac, poser un objet, tourner une page. C'est le « toucher » de Realism Craft. Client seul, version LTS pour 1.21.1 ; demande MRU (nouvelle bibliothèque) et YACL (déjà là) |
| ~~Dynamic Surroundings~~ (OreCruncher, l'auteur d'origine) | Sorti (12.1) | On ne garde que trois fonctions : **les pas** qui sonnent selon le sol (une version légère de Presence Footsteps), **les cascades** (son) et **l'aurore** dans les biomes froids. Le brouillard, les sons de biome (AmbientSounds) et la réverbération (Sound Physics) sont coupés |
| Presence Footsteps | Remplacé | Aucune version NeoForge 1.21.1 sur Modrinth ; Dynamic Surroundings couvre les pas |
| Drip Sounds | Optionnel | Le son des gouttes qui tombent dans les grottes ; encore en bêta |
| Auroras | Repli | Si Dynamic Surroundings ne convient pas, ce mod ne fait que l'aurore (dernière mise à jour 2024) |

## 11.3 Le confort

Règle : d'abord activer ce que **Quark** sait déjà faire (il est dans le pack), ensuite seulement ajouter un mod.

### Dans Quark, à changer

| Module | Aujourd'hui | Proposé | Pourquoi |
|---|---|---|---|
| Automatic Tool Restock | coupé | **activé** | L'outil cassé est remplacé par le suivant de l'inventaire. Évite Stack Refill |

Déjà actifs et gardés : **le tri d'inventaire** (bouton dans l'inventaire et les coffres, gardé à la demande de l'équipe), le transfert rapide, recherche dans les coffres, compteur d'objets, récolte au clic droit, échelles améliorées, émotes, rotation verrouillée.

### À ajouter

| Mod | Côté | Ce qu'il apporte | Lien avec le reste |
|---|---|---|---|
| **Pick Up Notifier** | C (S optionnel) | Une petite liste discrète des objets ramassés, qui s'efface | Puzzles Lib, déjà là pour Visual Workbench |
| ~~Leaves Be Gone~~ (sorti, 12.1) | S | Les feuilles d'un arbre coupé disparaissent vite | Va avec FallingTree |
| ~~Easy Anvils~~ (sorti, 12.1 : casse l'enclume d'Overgeared) | CS | Les objets restent posés sur l'enclume, en 3D, et le « trop cher » disparaît | Même famille que Visual Workbench : chaque poste de travail montre ce qu'on y fait. **À vérifier avec Overgeared** |
| **Easy Magic** | CS | Les objets restent sur la table d'enchantement, relance des enchantements | Même famille |
| **Seamless Loading Screen** | C | Quand on revient dans le monde, l'écran de chargement montre la dernière vue | Le monde permanent reprend là où on l'a laissé |
| **Dynamic FPS** | C | Le jeu ralentit quand il est en arrière-plan | Confort et batterie |
| Chat Heads | C | La tête du joueur à côté de son message | Optionnel, utile entre amis |

### Écartés, et pourquoi

| Mod | Raison |
|---|---|
| Jade, AppleSkin | Déjà écartés : un nom sur tout ce qu'on regarde et des chiffres sur la faim cassent le mystère |
| Just Zoom, Zoomify | La longue-vue est l'outil de découverte de Wildex ; un zoom gratuit la rendrait inutile |
| Waystones | La téléportation efface le voyage, le réseau ferré et les dirigeables |
| Carry On | Incompatible avec Aeronautics (chapitre 6) |
| Inventory Profiles Next, Inventory Essentials, Crafting Tweaks, Stack Refill, Right Click Harvest | Quark le fait déjà |
| Better Third Person | Shoulder Surfing Reloaded est retenu |
| Shulker Box Tooltip | L'End est fermé : pas de shulkers |
| Enchantment Descriptions | Deux bibliothèques de plus pour une info qu'on trouve dans EMI |

## 11.4 Performance

Les ajouts sont presque tous côté client et légers. Deux garde-fous :
- **Le budget de particules** : Particular et Subtle Effects ajoutent chacun des particules. Subtle Effects a une distance de rendu des particules ; on la règle sous la limite de la brume.
- **More Culling** (demande Cloth Config) : à essayer pour compenser le coût de Better Leaves en forêt. Il change la façon de cacher les blocs, donc il se teste avec Sodium 0.8 et un dirigeable assemblé, comme Entity Culling.

## 11.5 Chiffres

| | Mods | Packs de ressources |
|---|---|---|
| Rendu | Polytone, Particular Reforged, EMF, ETF, 3D Skin Layers, Sounds (+ MRU) : **7**, et Not Enough Animations et First-person Model sortent | Fresh Animations + Details, Objects, Emissive, Player Extension : **5** |
| Confort | Pick Up Notifier, Leaves Be Gone, Easy Anvils, Easy Magic, Seamless Loading Screen, Dynamic FPS : **6** | — |
| À tester | Dynamic Surroundings, Real Camera et More Culling (+ Cloth Config), Falling Leaves Plus en repli | — |
| Écarté le 2026-10-18 | Dragged's Simulated Tree Chopper (« c'est de trop », l'équipe) | — |

Le pack passerait d'environ 110 à environ 123 fichiers, dont presque tout ce qui s'ajoute est client et désactivable.

## 11.6 Archie's Dinos (archieblocks.com)

Ce que c'est : 7 dinosaures avec chacun son habitat (grottes luxuriantes, jungle, océan chaud, Nether, Deep Dark ×2, End), apprivoisement par rituel, monture, reproduction, un PNJ marchand (Archie), un guide à tampons, 6 armures, 8 armes et outils, un boss de fin. **Java : Forge et Fabric, Minecraft 1.20.1 seulement**, gratuit, « bientôt » sur CurseForge et Modrinth. Bedrock : add-on payant.

**Passer en 1.20.1 : non.** Fallow perdrait Create Aeronautics, Sable et No Man's Land, qui n'existent qu'en NeoForge 1.21.1 (et NML ne sera pas porté, d'après ses auteurs). Le pack est construit autour d'eux.

**Le porter en 1.21.1 avec l'assistant : faisable, mais pas sans les auteurs.**
- Techniquement, un passage de Forge 1.20.1 à NeoForge 1.21.1 touche les composants d'objets (fin du NBT sur les objets), les registres, le réseau et la version de GeckoLib. C'est un gros chantier, puis une maintenance à chaque mise à jour du mod d'origine.
- Juridiquement, il faut **la permission** de l'équipe pour redistribuer un portage, même entre amis dans un modpack. La licence n'est pas encore publiée, et la version Bedrock est payante : un refus est possible.
- Le bon premier pas : demander à l'équipe (Eternal Creations et Archie Blocks) si une version NeoForge 1.21.1 est prévue, ou s'ils acceptent un portage.

**Et dans Fallow ?** Même porté, il entrerait mal tel quel :
- 2 espèces sur 7 vivent dans le Nether ou l'End, que Fallow ferme ;
- le boss, les armures et les armes vont contre le monde « presque vide » ;
- son guide à tampons double le bestiaire Wildex, et Archie est un PNJ marchand, ce qu'on a écarté avec Easy NPC ;
- la décision 0003 a déjà mis la préhistoire de côté.

Ce qui pourrait entrer : les **4 espèces de l'Overworld** (Lushsaur, Therimoss, Coralosaur, Sculkraptor ; pas le Sculksaur, qui est le boss) comme animaux rares du zoo, avec une page Wildex chacune, sans le boss ni l'équipement. À rediscuter quand le mod sortira et qu'on connaîtra sa licence.

## 11.7 Caméra, recettes et bestiaire (demandes du 2026-10-18)

### Camera Overhaul : oui, réglé bas

Version 2.1.2 pour NeoForge 1.21.1, client seul, demande Cloth Config (déjà prévu pour Real Camera et More Culling), licence GPL. Chaque effet a son intensité dans `cameraoverhaul.toml`, et 0 le coupe. C'est exactement le « ressenti du geste » de Realism Craft.

Réglages proposés, livrés par Default Options :
- inclinaison en tournant et en se décalant : faible ;
- tangage aux sauts et aux chutes : moyen ;
- secousse des explosions : gardée ;
- balancement à l'arrêt : gardé, très doux.

À vérifier sur `test/finition` : à bord d'un dirigeable ou d'un train Create (l'inclinaison s'ajoute-t-elle aux mouvements du véhicule ?), et avec Real Camera, qui bouge aussi la caméra. Si les deux se gênent, Camera Overhaul passe avant Real Camera.

### Spatial GUI : les menus dans le monde

Version 1.8 pour NeoForge 1.21.1 (sortie le 2026-10-17), client seul, demande YACL (déjà là). Les menus s'affichent comme un panneau en 3D devant le joueur. En **mode troisième personne**, la caméra glisse au niveau de l'épaule à l'ouverture d'un coffre ou du sac, et le personnage se tourne vers ce qu'il regarde : c'est ce que l'équipe a demandé.

Réglages proposés :
- mode troisième personne pour les coffres, le sac, l'établi et les fours ;
- écran incurvé pour l'inventaire, qui reste lisible avec la colonne d'EMI à côté ;
- **désactivé** pour les livres et les carnets (Wildex, codex d'Ancient Remnants, Patchouli, Modonomicon) et pour l'atlas, qui se lisent mieux à plat ;
- flou du monde léger, ou coupé, pour que la brume reste visible derrière le menu.

À vérifier : quatre mods touchent maintenant la caméra (Shoulder Surfing Reloaded, Camera Overhaul, Spatial GUI, et Real Camera à l'essai). Ils se testent ensemble sur `test/finition`. Si deux se gênent, l'ordre de priorité proposé est Spatial GUI, Camera Overhaul, Shoulder Surfing, puis Real Camera.

### EMI à la place de JEI

Version 1.1.24 pour NeoForge 1.21.1, aucune dépendance, licence MIT. Ce que Fallow en fait :
- **Groupes et catégories** : les objets se regroupent (toutes les planches, tous les meubles Handcrafted, tous les blocs Twigs) et l'index se range par thème plutôt que par mod. Tout passe par un pack de données et KubeJS, qui sait parler à EMI.
- **L'arbre de recettes** : EMI calcule tout ce qu'il faut pour fabriquer une machine Create, étape par étape. Il remplace un carnet de calculs.
- **Le mystère** : les recettes qui viennent des plans trouvés dans les ruines (10.10) peuvent être cachées dans EMI pour tout le monde : on les apprend en lisant le plan. EMI ne sait pas les montrer joueur par joueur selon sa progression.

Point à vérifier : certains mods n'ont qu'une intégration JEI. EMI les lit si JEI est aussi installé ; dans ce cas JEI reste dans le pack, invisible, comme simple pont. On le saura en ouvrant les recettes de Create Aeronautics, Kaleidoscope Cookery, Farm & Charm et Overgeared.

### Les pages de Wildex

J'ai ouvert le jar de Wildex 3.0.0. Ce qui se change et ce qui ne se change pas :

| Élément | Modifiable ? | Comment |
|---|---|---|
| L'image du livre (5 thèmes : vintage, modern, jungle, runes, steampunk) | **Oui** | Chaque thème est une image de 1024×704 (`assets/wildex/textures/gui/wildex_<thème>.png`) qu'un pack de ressources remplace. On peut dessiner un **carnet de naturaliste Fallow** |
| Icônes des onglets, fond des trophées | Oui | Images de 64×64 et 128×128 dans le même dossier |
| Disposition des zones, couleur du texte | Non | Codées en dur ; le nouveau dessin doit garder les mêmes zones et des tons proches du thème qu'il remplace |
| Contenu des pages | Non | Généré tout seul : statistiques, butin, lieux d'apparition, nourriture d'élevage. Pas de texte libre ni d'illustration par créature ; la créature est montrée en 3D |
| Lien KubeJS | Événements seulement | Wildex prévient KubeJS quand on découvre une créature ou qu'on termine le bestiaire, sans pouvoir écrire dans les pages |

Les pages d'Archie's Dinos sont illustrées à la main, espèce par espèce. Wildex ne peut pas faire ça. Deux façons de s'en approcher :
1. **Recommandé : un carnet illustré à côté de Wildex.** Patchouli et Modonomicon sont déjà dans le pack et acceptent des pages dessinées, du texte libre et une couverture sur mesure. À chaque découverte, l'événement Wildex passe par KubeJS et débloque la page de la créature dans le carnet. Wildex reste le registre automatique (le zoo, les trophées), le carnet porte le récit, les dessins et les indices. Le coût est surtout artistique : une illustration par espèce importante, pas par mob.
2. **Modifier Wildex lui-même.** Sa licence (CC BY-NC 4.0) permet une version modifiée non commerciale, en citant l'auteur ; un onglet « Notes » avec texte et image par créature est faisable avec l'assistant. Mais il faudrait suivre chaque mise à jour du mod : à garder pour plus tard.

Dans les deux cas, on commence par un thème Wildex redessiné, qui ne coûte qu'une image.

**Choix du 2026-10-18 : on reste sur Wildex, avec un de ses thèmes d'origine.** Aucun thème tout fait pour Wildex ni pour Modonomicon n'existe sur Modrinth, et l'équipe ne peut pas dessiner les pages. Le carnet illustré et le Wildex modifié sont donc écartés tant que personne ne fournit les dessins. Une retouche simple reste possible sans dessiner : teinter ou désaturer l'image d'un thème existant pour l'accorder aux couleurs de la brume.

Pour comparer, le codex d'Ancient Remnants n'utilise pas Patchouli : c'est un écran codé par l'auteur (Obscuria). Chaque page est un fichier de données (titre, texte, image de 350×210, biomes), ce qui laisse penser qu'un pack de données pourrait y ajouter des pages ; c'est à tester.

## 11.8 Ancient Remnants: Monoliths

Ajouté à la demande de l'équipe. Version 1.5.2 pour NeoForge 1.21.1 (publiée le 2026-10-18), client et serveur, demande Fragmentum (bibliothèque d'Obscuria, 4,5 millions de téléchargements). Mod écrit pour la ModJam CurseForge « Echoes of the Past », encore très jeune (environ 500 téléchargements).

Ce que contient le jar (ouvert le 2026-10-18) :
- **6 monuments**, chacun lié à un biome : trois monolithes (chasseur, guerrier, sentinelle) et trois monuments (cerisier, chêne, conifères).
- Au centre, un **Elderheart** gardé par des créatures. Il donne une **bénédiction** : agilité du chasseur, force du guerrier, défense de la sentinelle, vitalité à faible santé, portée de construction, récolte abondante.
- Un **Monolith Codex** qu'on remplit avec des fragments de recherche dispersés dans le monde, laissés par un marchand ambulant. Chaque monument a sa page d'histoire.
- Espacement et biomes réglables dans la config ou par tags de biome.

Pourquoi il a sa place : c'est exactement le monde « presque vide et énigmatique ». Les monuments sont rares, on les entend avant de les voir (un bourdonnement qui monte), et le mod refuse de tout expliquer : on reconstitue l'histoire soi-même. Le codex est un objectif long, comme le bestiaire.

Réglages et liens proposés :
- **Biomes** : rattacher chaque monument aux biomes de No Man's Land par tag (les valeurs par défaut sont ajoutées par le code, et les biomes NML n'y sont peut-être pas). Le monument du cerisier n'a de sens que si un biome de cerisiers existe.
- **Rareté** : espacement large, en accord avec Sparse Structures. Un monument doit rester un évènement.
- **La brume** : à prototyper, un Elderheart réveillé pourrait ouvrir une clairière (10.3) autour du monument. Il faut trouver dans le code l'évènement à brancher sur KubeJS.
- **Les gardiens** : ils suivent la règle 0003 (hostiles rares et territoriaux), donc ils restent dans le monument.
- **Les bénédictions** : vérifier leur durée et leur force en jeu. « Portée de construction » et « récolte abondante » vont bien avec le reste du pack ; la force et l'agilité ne doivent pas rendre la faune triviale.

À vérifier sur une branche de test : la génération à côté de NML et de Sparse Structures, et la stabilité d'un mod aussi récent sur un monde permanent. Si on retire le mod après coup, les monuments déjà générés perdent leurs blocs et laissent des ruines trouées : il faut donc le tester avant d'ouvrir le monde.
