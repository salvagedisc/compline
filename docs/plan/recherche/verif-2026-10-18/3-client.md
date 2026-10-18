# Vérification 3 : la pile client (rendu, caméra, interface)

> Vérification du 2026-10-18 sur les §10.6 et le chapitre 11. Toutes les versions ont été relues sur Modrinth le 2026-10-18 (NeoForge 1.21.1). Les jars ont été ouverts dans le scratchpad et leurs mixins lus avec `javap`. Les fichiers `*.mixins.json` ne suffisaient pas toujours, alors toutes les classes ont été parcourues (Camera Overhaul charge ses mixins par un plugin, LambDynamicLights les range dans des jars imbriqués).
>
> Légende des preuves : **[MR]** version ou dépendance relue sur Modrinth, **[jar]** classe ou fichier du jar inspecté, **[wiki]** page de wiki officielle, **[issue]** issue GitHub lue, **[déduit]** raisonnement à partir de ce qui précède.

---

## 1. Ce qui est confirmé

### 1.1 Versions disponibles (NeoForge 1.21.1, au 2026-10-18)

| Mod | Pack actuel | Dernière version | Remarque | Preuve |
|---|---|---|---|---|
| Sodium | 0.8.13 | 0.8.13 | Sable demande ≥ 0.8.12 | [MR] [jar Sable `neoforge.mods.toml`] |
| ImmediatelyFast | 1.6.14 | 1.6.15 | mise à jour mineure | [MR] |
| Entity Culling | 1.11.2 | 1.11.3 | casse Aeronautics, voir 3.1 | [MR] [issue] |
| ModernFix | 5.27.24 | 5.27.26 | mise à jour mineure | [MR] |
| ATMOSPHERICS | 2.6.5 | 2.6.5.2 | correctifs | [MR] |
| Polytone | absent | 1.21-5.1.3 (2026-10-16) | **8 versions en 10 jours** depuis la 5.0.0 du 2026-10-06 ; la dernière 4.x est la 4.5.3 (2026-09-26) | [MR] |
| Particular Reforged | absent | 1.5.7 | dépendances **optionnelles** Create, Aeronautics, Sable, TFC, Iris | [MR] |
| Subtle Effects | 1.14.3 | 1.15.1 | | [MR] |
| LambDynamicLights | 4.8.11 | 4.8.11 | | [MR] |
| More Darkness | 2.0.1 | 2.0.1 | demande YACL | [MR] |
| Entity Model Features | absent | 3.3.12 | demande ETF | [MR] |
| Entity Texture Features | absent | 7.2.6 | | [MR] |
| Fresh Animations | absent | 1.10.4 (bêta) | pack de ressources | [MR] |
| FA Extensions (lot) | absent | 1.8.1 ; Player Extension 1.1.0, Details 2.2.1, Objects 2.1.2, Emissive 1.6.0 | | [MR] |
| 3D Skin Layers | absent | 1.11.3 | a un mixin de compatibilité EMF (`EMFModelPartMixin`) | [MR] [jar] |
| Real Camera | absent | 0.7.8-beta | incompatible (Modrinth) avec les armures GeckoLib, OptiFine, Armourer's Workshop ; le jar a une compat Sable (`SableCompat`) | [MR] [jar] |
| Camera Overhaul | absent | 2.1.2 | demande Cloth Config | [MR] |
| Shoulder Surfing Reloaded | absent | 5.2.0 | incompatible avec betterthirdperson, camerautils, nimble, valkyrienskies ≤ 2.4.11, customcameraview | [MR] [jar `neoforge.mods.toml`] |
| Spatial GUI | absent | 1.8 (2026-10-17) | demande YACL ; listes d'activation par écran ; mixin de compat Polytone | [MR] [jar] |
| Smooth Swapping | absent | 0.9.3.2 bêta (2025-11) | | [MR] |
| ItemPhysic Lite | absent | 1.6.13 | | [MR] |
| MB-3D Items (`mb3d-items-pack`) | absent | 2.4.0 | pack de ressources | [MR] |
| Motschen's Better Leaves | absent | 9.6 | gère `nomansland` et `quark` | [MR] [jar du pack] |
| More Culling | absent | 1.0.10 | demande Cloth ; incompatible Sodium < 0.8.0 | [MR] [jar] |
| EMI | absent | 1.1.24 | | [MR] |
| JEI | absent | 19.64.0.455 | utile comme pont, voir 1.6 | [MR] |
| Sounds | absent | 2.4.22+lts | demande MRU et YACL | [MR] |
| Dynamic Surroundings (OreCruncher) | absent | 0.4.6 (slug `dynamicsurroundingsfabric`) | | [MR] |
| AmbientSounds | 6.3.8 | 6.3.10 | | [MR] |
| Sound Physics Remastered | 1.5.1 | 1.5.1 (alpha) | | [MR] |
| Seamless Loading Screen | absent | 2.2.1 (2024-10-01, version unique) | | [MR] |
| Default Options | absent | 21.1.8 | demande Balm | [MR] |
| No Man's Land | 1.5.12 | **1.6.1** (2026-10-14) | embarque Mixed Litter 1.2.2 et sable-companion 1.6.0 | [MR] [jar] |
| Sable | 2.0.5 | 2.0.6 | incompatible scalablelux et sablecompanion > 1.6.0 ; embarque Veil | [MR] [jar] |
| Enhanced Celestials | — | 6.0.2.6 | | [MR] |

Sinytra Connector 2.0.0-beta.17 et Forgified Fabric API sont dans le pack [dépôt `pack/mods/`]. Aucun mod Fabric n'est nécessaire dans cette pile.

### 1.2 NML contre Fresh Animations : NML gagne, par Mixed Litter

- NML ne remodèle pas les mobs lui-même : il **embarque Mixed Litter** 1.2.2 (même équipe) [jar NML, `META-INF/jarjar`].
- Mixed Litter remplace le renderer Java de chaque espèce : `PigRemodelRenderer`, `CowRemodelRenderer`, `MooshroomRemodelRenderer`, `ChickenRemodelRenderer`, `SheepRemodelRenderer`, `RabbitRemodelRenderer`, `SquidRemodelRenderer`, choisis au rendu par `RemodelRenderers.get` [jar Mixed Litter].
- Réglage : `config/mixed_litter-startup.toml` (type STARTUP, donc relu au lancement seulement). Les clés `chickenRemodel`, `cowRemodel`, `pigRemodel`, `rabbitRemodel`, `sheepRemodel` et celle du calmar sont **toutes à `true` par défaut**, chacune avec sa liste d'entités (vaches : `cow`, `mooshroom`, `buzzier_bees:moobloom` ; calmars : `squid`, `glow_squid`) [jar].
- **Conclusion.** Avec les réglages par défaut, NML l'emporte sur le **cochon, la vache, la mooshroom, le poulet, le mouton, le lapin, le calmar et le calmar luisant**. Ces renderers n'utilisent pas les couches vanilla et Fresh Animations n'a aucun `.jem` pour elles : FA ne les anime pas [jar FA, déduit].
- Le plan sous-estime le recouvrement. Il parle de « cochons, vaches et calmars », mais **poulet, mouton et lapin** sont aussi pris par NML. FA anime donc le reste : loups, renards, chats, chevaux, chèvres, tortues, villageois, monstres, objets.
- Pour donner une espèce à FA, il suffit de passer la clé Mixed Litter de cette espèce à `false`.

Variantes de texture de NML : `data/nomansland/mixed_litter/variants/` couvre abeille, chat, poulet, morue, vache, dauphin, âne, renard (renard crème selon le biome), chèvre, husk, mooshroom, cochon (13 variantes), saumon, mouton, slime, calmar et tortue [jar NML]. Elles sont injectées par `RenderEntityVariantMixin`, un `ModifyVariable` au `STORE` de `getTextureLocation` dans `LivingEntityRenderer.getRenderType`, **donc sur tous les mobs, remodelés ou non** [jar Mixed Litter].

### 1.3 ATMOSPHERICS : la clairière est reconnue, à une condition

- Les presets se livrent dans un pack de ressources, sous `assets/atmospherics/preset/*.json` (lecture `listResources("preset")`, namespace `atmospherics`, suffixe `.json`) [jar].
- Ils sont **appliqués tout seuls**, une fois par empreinte, quand aucun écran n'est ouvert (`tickResourcePackPresetPrompt` → `applyPresetAndReload`) [jar]. L'empreinte est la liste des ID de presets : un preset modifié sous le même nom **n'est pas réappliqué**. Il faut le renommer à chaque version (`fallow_v1.json`, `fallow_v2.json`…) [jar, déduit].
- Le preset actif est recopié dans `config/ambientfog/biome_fog.json`. Les presets locaux vont dans `config/ambientfog/presets/` [jar].
- Les biomes sont indexés par **chaîne d'ID**, donc `fallow:clairiere` est accepté tel quel. Un biome absent du preset reçoit un profil par défaut (`computeIfAbsent`), copié sur `minecraft:plains` [jar].
- Le preset de base ne contient que 65 biomes vanilla, **aucun biome NML** [jar]. Le preset Fallow doit les nommer un par un, sans quoi toutes les forêts de NML ont la brume des plaines.
- Le biome est lu par `ClientLevel.getBiome` à la position du joueur, mélangé sur une grille de cellules de 4 blocs (`BiomeBlendUtil`, `GRID_RADIUS` 1, pas de 4) [jar].

### 1.4 /fillbiome : la brume suit, avec un temps de retard

- Le client reçoit les nouveaux biomes par `ClientboundChunksBiomesPacket` [déduit du code vanilla].
- Le cache de `BiomeBlendUtil` est indexé par cellule et **n'est pas vidé** quand un biome change [jar].
- Résultat : debout sans bouger dans la clairière fraîchement créée, la brume reste. Elle change dès que le joueur entre dans une **autre cellule de 4 blocs**, puis glisse doucement (lissage exponentiel, `tau` 0,9986 par image) [jar, déduit].
- Pour la mise en scène « la brume se lève », c'est plutôt un avantage, puisque le joueur marche. Pour une clairière qui s'ouvre **autour** d'un joueur immobile, il faut le mod custom de 4.3.

### 1.5 Ce qu'ATMOSPHERICS touche dans le rendu

| Cible | Injection | Preuve |
|---|---|---|
| `FogRenderer.setupColor`, `setupFog` | Inject à `TAIL` | [jar] |
| `ClientLevel.getSkyColor`, `getSunriseColor` | Inject à `RETURN` | [jar] |
| Nuages | Redirect `getCloudColor` et `getCloudHeight`, `ModifyArg` dans `buildClouds` | [jar] |
| `LevelRenderer.renderSky` | **Réécrit : Inject à `HEAD` avec annulation**, sauf sous l'eau, sous effet (cécité…), ciel non normal, et deux cas NML (Friend Moon) | [jar] |
| Sodium | coupe tout seul l'occlusion du brouillard de Sodium, règle la distance des nuages | [jar] |
| NML | lit la Friend Moon de NML par réflexion | [jar] |

### 1.6 EMI : qui n'a qu'un plugin JEI

| Plugin JEI seulement (EMI les lit par JEMI si JEI est installé) | Plugin EMI natif |
|---|---|
| Create (`CreateJEI`) | Overgeared (**EMI seulement**, pas de JEI) |
| Create Aeronautics / Simulated (`AeroJEI`, `SimulatedJEI`) | Kaleidoscope Cookery et KC Compat |
| Cold Sweat | Curios |
| Alex's Mobs | Supplementaries |
| Critters and Companions | Kiwi |
| Ecliptic | KubeJS |
| Quark / Zeta | Traveler's Backpack |
| Patchouli, Modonomicon | Visual Workbench |
| Knight Lib | AStages (masquage par joueur) |
| Create Ore Excavation | |
| Exposure | |
| Farm & Charm **1.1.25** (la 1.1.29 a un plugin EMI) | |

[jar de chaque mod : classes `@JeiPlugin` et `@EmiEntrypoint`]. EMI contient le pont JEMI (`dev/emi/emi/jemi`) [jar EMI]. **JEI doit donc rester dans le pack**, sans que son interface soit visible.

### 1.7 Ce que KubeJS sait faire avec EMI

KubeJS 2101 implémente `RecipeViewerEvents` pour EMI [jar KubeJS] :
- `addEntries`, `addInformation`, `removeCategories`, `removeEntries`, `removeEntriesCompletely` ;
- `removeRecipes`, envoyé par le serveur (`removedGlobalRecipes`, `categoryData`), ce qui marche en multijoueur.
- **`groupEntries` n'est pas pris en charge pour EMI** : EMI 1.1.24 n'a pas d'API de groupes d'objets [jar EMI, aucune classe de groupe dans l'API]. Le §11.7 se trompe sur ce point.

Ce qu'EMI sait faire avec un pack de données ou de ressources, sans KubeJS [jar EMI, chargeurs de données] :
- `emi/index/stacks/*.json` : `disable`, `added` avec `after` (placer un objet après un autre, pour un ordre par thème), `removed`, `filters` ;
- `emi/category/properties/*.json` : ordre et icône des catégories ;
- `emi/recipe/filters/*.json` : cacher des recettes ;
- `emi/recipe/additions/*.json` : ajouter des pages `emi:info` (texte) et `emi:world_interaction` (« clic droit sur X avec Y donne Z ») ;
- `emi/aliases/*.json` : noms de recherche de remplacement ;
- l'option d'index `creative`, `registered` ou `creative_plus_registered` : avec `creative`, l'ordre suit les **onglets créatifs**, que KubeJS sait créer et remplir (`StartupEvents.modifyCreativeTab`, `StartupEvents.registry('creative_mode_tab')`).

Le regroupement visuel « toutes les planches » se fait donc par l'ordre (onglets créatifs ou `added`/`after`) et par la recherche, pas par des groupes repliables.

**Correction du plan : le masquage par joueur existe.** AStages 2.5.3 a un plugin EMI et un plugin JEI qui cachent les objets et recettes selon les étapes de chaque joueur [jar AStages]. Le §11.7 dit le contraire. Attention : AStages est en « All Rights Reserved » [MR], ce qui interdit de le modifier ou de le redistribuer hors Modrinth.

---

## 2. Les liens concrets à construire

| # | Lien | Outil | Détail |
|---|---|---|---|
| L1 | La clairière dans ATMOSPHERICS | pack de ressources `fallow-ambiance`, `assets/atmospherics/preset/fallow_v1.json` | Une entrée `fallow:clairiere` (brume quasi nulle, lumière franche) et **une entrée par biome NML**. Renommer le fichier à chaque version (1.3) |
| L2 | La clairière dans AmbientSounds | tag de biome dans un datapack KubeJS (`kubejs/data/minecraft/tags/worldgen/biome/is_forest.json` ou tag `c:`) | AmbientSounds associe les sons par tag (`#is_forest`) ou par morceau de nom [jar]. Sans tag, la clairière est silencieuse |
| L3 | La clairière dans les couleurs | le JSON du biome (`grass_color`, `foliage_color`, `water_color`, `effects`) | C'est la voie la plus sûre. Polytone peut l'affiner par `biome_modifiers`, sans `fog_color` ni `sky_color` (3.3) |
| L4 | Mixed Litter et Fresh Animations | `config/mixed_litter-startup.toml`, livré par Default Options | Choisir espèce par espèce. Proposition : NML garde cochon, vache, mooshroom, mouton et calmar (le style du trailer), FA prend poulet et lapin s'ils plaisent mieux en jeu |
| L5 | Variantes de NML sur les modèles FA | `config/entity_model_features.json` (`modelsNamesDisabled`) ou surcharge des fichiers `data/nomansland/mixed_litter/variants/<espèce>.json` par datapack | Pour chaque mob animé par FA **et** à variante NML (chat, renard, chèvre, tortue, dauphin, âne…), soit on coupe le modèle FA, soit on vide la variante NML. À trancher après le test T3 |
| L6 | Une seule source de brume | `config/nomansland-client.toml`, section `[fog_modifiers]` (config CLIENT de `NMLConfig`) : `fogModifiers = false`, ou `surfaceFogModifier`, `foggyBiomeFogModifier`, `cavesBiomeFogModifier`, `deepDarkFogModifier` à false | NML ajoute son propre brouillard [jar NML]. ATMOSPHERICS doit être le seul responsable (§11.1) |
| L7 | Une seule source de variantes | `pack/config/quark-common.toml`, `"Variant Animal Textures" = false` (ligne 173) | Doublon de NML ; ses textures cassent les UV de FA (3.5) |
| L8 | Une seule source de lucioles, cascades, poussière | configs Particular, Subtle Effects, NML | Voir le tableau 3.7 : un responsable par effet |
| L9 | Feuilles de NML dans Particular | config de Particular (correspondance bloc de feuilles → particule) | Aucun bois NML n'y est listé [jar] : saule, noyer, érable tombent en feuille générique. Ajouter les entrées, ou garder Falling Leaves Plus en repli |
| L10 | Ordre de l'index EMI par thème | `kubejs/assets/fallow/emi/index/stacks/fallow.json` (`added` + `after`), ou index `creative` + onglets créatifs KubeJS | Remplace les « groupes » du plan |
| L11 | Gestes Create visibles dans EMI | `emi/recipe/additions/*.json` de type `emi:world_interaction` et `emi:info` | Pour les recettes « dans le monde » de Fallow (clairière, rituels, plans des ruines) qu'aucun mod ne déclare |
| L12 | Recettes des plans cachées puis apprises | AStages (EMI) **ou** `RecipeViewerEvents.removeRecipes` (KubeJS, pour tous) | AStages permet le « par joueur » ; KubeJS seul cache pour tout le monde |
| L13 | Réglages livrés une fois | Default Options : `config/defaultoptions/extra/` | Les fichiers ne sont copiés **que s'ils manquent** [jar] : ils servent de point de départ et le joueur garde ses choix. Les packs de ressources (`fallow-ambiance`, FA, MB-3D, Better Leaves) s'activent par défaut (`DefaultResourcePacksHandler`) [jar] |
| L14 | Camera Overhaul en première personne | `cameraoverhaul.toml` : `general.enableInThirdPerson = false`, contextes `vehicles` et `mounts` à 0 | Laisse la troisième personne à SSR, et évite que la caméra penche à bord des dirigeables (3.4) |
| L15 | Spatial GUI loin des livres | sa liste d'écrans | Exclure Wildex, le codex d'Ancient Remnants, Patchouli, Modonomicon, l'atlas, et les écrans d'EMI |
| L16 | Lune d'Enhanced Celestials sous ATMOSPHERICS | **mod custom** (4.3) | KubeJS ne peut pas toucher au rendu du ciel |

---

## 3. Conflits et risques

### 3.1 Entity Culling casse Aeronautics : à retirer

- Issues ouvertes, sans réponse des mainteneurs : Simulated-Project **#1347** (ouverte le 2026-07-30), **#974** (voiles d'hélice qui tremblent, reconfirmée le 2026-09-09, correctif PR #1427 pas fusionné), sable **#1414** [issue].
- Les listes blanches (`entityWhitelist`, `blockEntityWhitelist`, `tickCullingWhitelist`, `skipBlockEntityCulling`) ne visent que des types d'entités ou de blocs. Les sous-niveaux de Sable ne s'y prêtent pas [jar, déduit].
- Sodium fait déjà le tri des chunks et ATMOSPHERICS cache le lointain sous la brume. Le gain d'Entity Culling est faible dans un monde presque vide [déduit].

### 3.2 Polytone : utile, mais il éteint trois autres mods

- Polytone **annule `LightTexture.updateLightTexture` à `HEAD`** dès qu'une lightmap personnalisée est active (`LightTextureMixin`) [jar 5.1.3].
- Cela coupe d'un coup :
  - l'assombrissement de **More Darkness**, qui s'injecte juste avant l'envoi de la texture [jar More Darkness] ;
  - la teinte de lune d'**Enhanced Celestials**, injectée sur `blockLightRedFlicker` [jar EC] ;
  - la vraie obscurité et la Friend Moon de **NML** (son `LightTextureMixin`, actif si delightmap est absent ; la vraie obscurité ne joue que pendant les rêves) [jar NML].
- En 1.21.1, les lightmaps de Polytone sont **par dimension** : le champ `biomeLightmaps` existe mais n'est jamais lu [jar]. Une lightmap « clairière » par biome n'existe donc pas. Le script du ciel (`sky_getter`) peut lire `p.biome()` [wiki Scripting-Expressions] : une lightmap de dimension qui change selon le biome du joueur reste possible, mais elle annule toujours les trois mods ci-dessus.
- La fonction `season()` de Polytone ne connaît que Serene Seasons et Fabric Seasons, **pas Ecliptic** [jar].
- Une **colormap de bloc** Polytone remplace les lambdas de `BlockColors`, dans lesquelles Ecliptic injecte ses couleurs de saison. L'herbe et les feuilles perdraient leurs saisons. Un `biome_modifier` (couleur d'herbe du biome) reste compatible [jar Ecliptic, déduit].
- Polytone 5.x ajoute des lumières colorées (`OFF`, `BUILT_IN` par défaut, `HYBRID`, `VEIL`), des voxels, des ombres et des compute shaders. En `BUILT_IN` ou `HYBRID`, il **patche le shader de chunk de Sodium** [jar]. C'est le genre de changement qui casse avec Sable, qui embarque Veil et rend ses sous-niveaux à part [déduit]. Réglage : `<dossier du jeu>/polytone_options.json`, clé `coloredLightsBackend` [jar].
- Rythme : 8 versions en 10 jours, avec un moteur de lumière neuf. La 4.5.3 a déjà les lumières colorées, sans le patch du shader Sodium [MR] [jar 4.5.3].

### 3.3 Polytone contre ATMOSPHERICS : ATMOSPHERICS gagne

- Polytone change la couleur du brouillard par un `WrapOperation` sur `gaussianSampleVec3` dans `setupColor`, et celle du ciel par un `WrapOperation` sur `getSkyColor` [jar].
- ATMOSPHERICS réécrit ces valeurs plus tard (`TAIL` et `RETURN`) [jar]. Ce qu'écrit Polytone est écrasé [déduit].
- Donc dans le pack `fallow-ambiance`, **pas de `fog_color`, `sky_color` ni `fog_radius` côté Polytone**. Polytone ne garde que l'herbe, les feuilles, l'eau et les particules.

### 3.4 Quatre mods sur la caméra

Cibles relevées dans les jars :

| Cible | Shoulder Surfing | Camera Overhaul | Spatial GUI | Real Camera | Sable |
|---|---|---|---|---|---|
| `Camera.setup` | Inject `HEAD` et après `setPosition`, Redirect `move(FFF)` | Inject à l'appel `getMaxZoom` (3e pers.) et à `RETURN` (1re pers.) | Inject `TAIL` | Inject `RETURN` | Redirect `setRotation`, WrapOperation `setPosition`, Inject après `rotationYXZ` |
| `Camera.setRotation` | ModifyArg de `rotationYXZ` | | | | |
| `Camera.setPosition` | | | | | WrapMethod |
| `GameRenderer.pick` | **Redirect** `getEntityHitResult` | | | **WrapOperation** sur le même appel | |
| `GameRenderer.bobView` / `bobHurt` | Redirect `bobView` | Inject `bobHurt` `HEAD` | | | |
| `GameRenderer.render` / `getFov` | | | Redirect `ClientHooks.drawScreen` et `PostChain.process`, ModifyReturnValue `getFov` | | |

[jar de chaque mod]

- **Sable a cinq mixins de compat écrits pour Shoulder Surfing** (dont `getMaxZoom`) [jar Sable]. SSR est le seul mod de caméra que Sable connaît. C'est un bon signe pour la troisième personne à bord.
- **Risque n° 1 : SSR et Real Camera sur `GameRenderer.pick`.** Un Redirect et un WrapOperation sur le même appel : selon l'ordre d'application, l'un échoue au chargement ou l'un perd son effet (la visée) [déduit]. Test T7.
- **Risque n° 2 : Camera Overhaul à bord.** Ses contextes `vehicles` et `mounts` font pencher la caméra selon la vitesse. Sable tourne déjà la caméra avec le sous-niveau. Les deux s'additionnent [déduit]. À 0 sur les véhicules (L14).
- Spatial GUI n'agit que menu ouvert (à `TAIL` de `setup`) : il passe après les autres et ne les gêne pas en jeu normal [déduit].
- Real Camera est en bêta, incompatible avec les armures GeckoLib (beaucoup de mods d'armures en 1.21.1 en utilisent), et doublonne SSR sur la visée [MR] [déduit].

### 3.5 Les textures d'animaux : trois mods sur le même point

- NML (`RenderEntityVariantMixin`), Quark (`Variant Animal Textures` : mixins sur `CowRenderer`, `PigRenderer`, `ChickenRenderer`, lapin, abeille, dauphin, grenouille, lama, slime, allay, tatou, tous sur `getTextureLocation`) et ETF changent la texture d'un mob [jar NML] [jar Quark] [déduit pour ETF].
- Les textures de NML et de Quark sont dessinées pour le modèle vanilla. FA livre ses propres textures (chat, renard, chèvre, tortue, loup…) avec d'autres UV. Une variante NML ou Quark posée sur un modèle FA risque des **morceaux de texture mal placés** [déduit]. Test T3.
- Quark est un doublon pur de NML et se coupe (L7).

### 3.6 Les sons : Dynamic Surroundings contre Sound Physics

- Dynamic Surroundings 0.4.6 a ses propres « sons améliorés » (réverbération, occlusion). Lui et Sound Physics Remastered s'injectent tous deux dans `Library.init` (OpenAL), `Channel` et `SoundEngine` [jar DS] [jar SPR]. Même avec la fonction coupée dans la config, les mixins restent chargés [déduit]. Risque fort de sons cassés ou de crash au lancement.
- Ses « pas » **ne remplacent pas Presence Footsteps** : ce sont des accents (bruit de l'armure, sol mouillé, grincements), posés sur les pas vanilla [jar DS, `dsconfigs`]. Le plan le surestime.
- Ses ambiances de biome (`assets/dsurround/dsconfigs/biomes.json`, remplaçable par pack de ressources) doublonnent AmbientSounds [jar].
- Ses autres fonctions doublonnent aussi :
  - cascades : sons, particules, brume et écume (Particular et Subtle Effects font déjà les particules) ;
  - lucioles ;
  - sons de la barre d'objets (Sounds les fait) ;
  - brouillard (`enableFogEffects…`, contre ATMOSPHERICS).
- Ce qu'il apporte de vraiment neuf : **l'aurore et le son des cascades** (AmbientSounds n'a pas de cascade [jar AmbientSounds]).

### 3.7 Les particules : qui fait quoi

| Effet | Particular | Subtle Effects | Dyn. Surroundings | NML | ATMOSPHERICS | Responsable proposé |
|---|---|---|---|---|---|---|
| Lucioles | oui | oui | oui | — | — | **Particular** (comme le plan) |
| Cascades | `cascades`, `waterfallSpray` | `waterfall_cloud`, `mist`, `droplet` | oui | — | — | **Particular** ; couper chez Subtle Effects |
| Poussière de grotte | `caveDust` | — | — | `cave_dust` | `cave_fog` | **ATMOSPHERICS** pour la brume, **Particular** pour la poussière ; couper NML |
| Ronds dans l'eau | oui | oui | oui | — | — | **Particular** |
| Feuilles qui tombent | oui (pas de bois NML) | — | — | — | — | Particular + entrées NML (L9) |

[jar de chaque mod, noms des options]

### 3.8 ATMOSPHERICS efface le ciel des autres

Comme `renderSky` est annulé à `HEAD` (1.5), **tout ce qui s'injecte dans le `renderSky` vanilla disparaît** :
- la couleur, la taille et la texture de la lune d'**Enhanced Celestials** (lune blanche, lune de sang…) [jar EC : injects dans `renderSky`] ;
- les soleils doubles (« sun dogs ») et la haute atmosphère de **NML** [jar NML].

Seule la Friend Moon de NML est gérée par ATMOSPHERICS lui-même [jar]. Les événements d'EC jouent toujours (apparitions, effets), mais leur lune ne se voit plus [déduit]. Test T1.

### 3.9 Petits conflits

- **More Culling** : son `cloudCulling` annule `buildClouds` à `HEAD`, ce qui court-circuite le `ModifyArg` d'ATMOSPHERICS sur les nuages [jar]. Il a aussi un `leavesCullingMode` qui se discute avec Better Leaves. Il avait déjà été écarté en décision 0007 [dépôt `docs/decisions/0007`].
- **Seamless Loading Screen contre Sodium** : Sodium écrase (`Overwrite`) `LevelLoadingScreen.renderChunks`. La retouche de couleur de fond de Seamless, posée dans une lambda de cette méthode, est perdue [jar]. Seulement cosmétique, l'écran garde la capture du monde [déduit]. Version unique de 2024, plus maintenue [MR].
- **MB-3D Items et Amendments** : MB-3D redessine en 3D lanternes, torches, longue-vue, boussoles, cartes et seaux [jar]. Amendments dessine déjà les lanternes en main. Selon l'ordre des packs, l'un remplace l'autre [déduit]. Mettre MB-3D sous Amendments, ou retirer ses lanternes.
- **Spatial GUI et Polytone** : Spatial GUI a un mixin sur `LightmapsManager.setupForGUI` de Polytone [jar]. C'est prévu, mais seulement pour la version visée par l'auteur [déduit].
- **Real Camera et 3D Skin Layers / Player Extension** : le corps en vue subjective passe par le renderer du joueur. 3D Skin Layers a un mixin EMF [jar], Real Camera n'en a pas [jar]. Test T6.

---

## 4. Ce qu'il faut adapter, supprimer ou ajouter

### 4.1 Supprimer

| Quoi | Raison |
|---|---|
| **Entity Culling** | Bugs ouverts avec Aeronautics et Sable, sans réponse (3.1). Gain faible sous la brume |
| **Quark « Variant Animal Textures »** | Doublon de NML, et casse les UV de FA (3.5, L7) |
| **Brouillard de NML** (`fogModifiers`) | Deux sources de brume, contraire au §11.1 (L6) |
| **Dynamic Surroundings** | Réverbération en conflit avec SPR, pas qui ne remplacent pas Presence Footsteps, presque tout en doublon (3.6). Garder **Auroras** si l'aurore compte (déjà en repli au §11.2). Pour le son des cascades, une entrée AmbientSounds liée à la présence d'eau qui coule autour du joueur [déduit, à vérifier dans le moteur d'AmbientSounds], ou rien |
| **More Culling** | Déjà écarté en 0007, et casse les nuages d'ATMOSPHERICS (3.9). Si on le reprend : `cloudCulling = false` |
| **Real Camera** (à l'essai) | Premier à retirer en cas de gêne : bêta, doublon de SSR sur la visée, incompatible GeckoLib (3.4) |

### 4.2 Adapter

| Point du plan | Changement | Raison |
|---|---|---|
| §11.1 et §11.2 « Polytone : lightmaps » | Polytone **sans lightmap**, seulement `biome_modifiers` (herbe, feuilles, eau) et couleurs de particules. `coloredLightsBackend = OFF`, ou Polytone **4.5.3 figé** le temps que la 5.x se stabilise | La lightmap éteint More Darkness, la lune d'EC et la nuit de NML. Pas de lightmap par biome en 1.21.1. Patch du shader Sodium en 5.x (3.2) |
| §11.2 « la clairière : lightmap plus chaude » | Faire la lumière de la clairière **par ATMOSPHERICS** (brume levée, couleur du ciel et du brouillard) et par les **couleurs du biome JSON** | C'est le seul responsable du ciel qui ne se fait pas écraser (3.3) |
| §11.2 « la lune blanche : nuit argentée » | Abandonner la piste Polytone ; passer par le mod custom (4.3) | La lune d'EC disparaît sous ATMOSPHERICS (3.8) |
| §11.2 Fresh Animations « Si NML gagne… » | NML gagne sur **8 espèces** : cochon, vache, mooshroom, poulet, mouton, lapin, calmar, calmar luisant. Le choix se fait espèce par espèce dans `mixed_litter-startup.toml` (L4) | 1.2 |
| §11.2 Dynamic Surroundings « les pas » | Retirer. Les pas restent vanilla (plus le « toucher » de Sounds) | 3.6 |
| §11.2 Particular et Subtle Effects | Couper aussi chez Subtle Effects les cascades et les ronds d'eau, et chez NML `cave_dust` | 3.7 |
| §11.7 Camera Overhaul « réglé bas » | Ajouter `enableInThirdPerson = false` et 0 sur `vehicles` / `mounts` | 3.4, L14 |
| §11.7 ordre de priorité | Voir 4.4 : **Spatial GUI, Shoulder Surfing, Camera Overhaul, Real Camera** (SSR passe avant Camera Overhaul) | Sable est écrit pour SSR ; Camera Overhaul doit lui laisser la 3e personne |
| §11.7 EMI « Groupes » | Remplacer par « ordre par thème » (index `creative` + onglets KubeJS, ou `added`/`after`) et alias de recherche | EMI 1.1.24 n'a pas de groupes ; KubeJS ne gère pas `groupEntries` pour EMI (1.7) |
| §11.7 EMI « pas par joueur » | Corriger : AStages le fait dans EMI | 1.7 |
| §11.7 « JEI reste comme pont » | Confirmé, et **obligatoire** : Create, Aeronautics, Cold Sweat, Ecliptic, Quark, Exposure… n'ont qu'un plugin JEI | 1.6 |
| Pack actuel | Monter NML en 1.6.1, Sable en 2.0.6, ATMOSPHERICS en 2.6.5.2 ; Farm & Charm en 1.1.29 (plugin EMI) | [MR] |
| Preset ATMOSPHERICS | Nommer tous les biomes NML, versionner le nom du fichier | 1.3 |
| MB-3D Items | Placer sous Amendments dans l'ordre des packs | 3.9 |

### 4.3 Ajouter : un petit mod custom « fallow-ciel » (client, NeoForge 1.21.1)

KubeJS ne peut rien sur le rendu du ciel, de la lune ni sur le cache d'ATMOSPHERICS. Un mod client d'une centaine de lignes, avec deux mixins, ferait trois choses :
1. **Rendre la lune d'Enhanced Celestials sous ATMOSPHERICS.** Après le ciel d'ATMOSPHERICS, lire l'événement lunaire actif dans l'API client d'EC et redessiner la lune avec sa couleur, sa taille et sa texture.
2. **Vider le cache de `BiomeBlendUtil`** à la réception d'un `ClientboundChunksBiomesPacket`, pour que la brume réagisse à un `/fillbiome` même si le joueur ne bouge pas.
3. Plus tard, si besoin : **teinter légèrement la nuit pendant la lune blanche**, en multipliant la lightmap après More Darkness, au lieu d'une lightmap Polytone qui l'annulerait.

Les deux premiers points sont nécessaires si la lune blanche et la clairière « qui s'ouvre » restent dans le plan. Le mod dépend des classes internes d'ATMOSPHERICS et d'EC : il faudra le revoir à chaque mise à jour de ces deux mods.

### 4.4 Ordre de priorité des mods de caméra

1. **Spatial GUI** : n'agit que menu ouvert, à la fin de `Camera.setup`. Il gagne quand un menu est ouvert.
2. **Shoulder Surfing Reloaded** : seul maître de la troisième personne. C'est le seul que Sable connaît (5 mixins de compat).
3. **Camera Overhaul** : première personne seulement, à pied (`enableInThirdPerson = false`, véhicules à 0).
4. **Real Camera** : le dernier, et le premier retiré en cas de conflit. Sans lui, on garde les bras vanilla en vue subjective.

### 4.5 Ce qui manque pour la finition « Realism Craft »

- **Les pas selon le sol** : rien sur NeoForge 1.21.1 ne remplace Presence Footsteps (3.6 pour Dynamic Surroundings, §11.2 du plan pour Presence Footsteps). Les sons de pas des blocs de NML et de Twigs restent ceux de leur matériau. Piste de mod custom si l'équipe y tient : jouer un son de pas par tag de bloc. Pas prioritaire.
- **Un seul pack `fallow-ambiance`** qui porte ensemble les presets ATMOSPHERICS, les `biome_modifiers` Polytone, les entrées de feuilles Particular s'il les lit en ressources, et les surcharges EMI côté client. Activé par Default Options (L13).
- **La lune vue dans le ciel**, aujourd'hui perdue (3.8, 4.3).

---

## 5. Questions à tester en jeu (branche `test/finition`)

| # | Test | Ce qu'on regarde |
|---|---|---|
| T1 | ATMOSPHERICS + Enhanced Celestials, nuit de lune blanche et de lune de sang | La lune change-t-elle de couleur ou de taille ? (attendu : non, 3.8) |
| T2 | `/fillbiome` en clairière, joueur immobile puis qui marche de 4 blocs | Délai avant que la brume se lève ; douceur de la transition (1.4) |
| T3 | FA + NML : chat, renard, chèvre, tortue, dauphin, âne de plusieurs variantes NML | Textures bien placées sur le modèle FA ? (3.5, L5) |
| T4 | `mixed_litter-startup.toml` avec `chickenRemodel = false`, puis `rabbitRemodel = false` | FA prend-il bien le relais ? Lequel plaît le plus ? (L4) |
| T5 | Polytone 5.1.3 avec `coloredLightsBackend` par défaut, à bord d'un dirigeable Aeronautics | Le sous-niveau Sable s'affiche-t-il ? Lumière des chunks correcte ? Sinon OFF ou 4.5.3 (3.2) |
| T6 | Real Camera + Player Extension + 3D Skin Layers, en regardant vers le bas | Le corps animé et les couches du skin apparaissent-ils ? |
| T7 | SSR + Real Camera : viser une entité et un bloc en troisième personne | Le chargement passe-t-il ? La visée suit-elle le réticule ? (3.4) |
| T8 | Camera Overhaul à bord d'un dirigeable et d'un train Create, avec `vehicles` à 0 puis par défaut | La caméra penche-t-elle deux fois ? |
| T9 | Spatial GUI sur coffre, sac (Traveler's Backpack), établi, puis Wildex et Patchouli | Le menu passe-t-il bien, les livres restent-ils à plat ? |
| T10 | EMI + JEI (pont) : recettes Create, Aeronautics, Cold Sweat, Ecliptic, Overgeared, Kaleidoscope | Toutes visibles dans EMI ? Pas d'interface JEI à l'écran ? (1.6) |
| T11 | AStages dans EMI : un objet lié à une étape non débloquée | Caché pour ce joueur seulement ? (L12) |
| T12 | Better Leaves sur une grande forêt NML, en distance de rendu normale du serveur | Images par seconde (§10.6) |
| T13 | Particular dans une forêt de saules et d'érables NML | Feuilles génériques ou propres ? (L9) |
| T14 | More Darkness + Polytone **sans** lightmap | La nuit reste-t-elle vraiment noire ? (3.2) |
| T15 | Si Dynamic Surroundings est quand même essayé : avec SPR, au lancement puis dans une grotte | Crash, son coupé ou double écho ? (3.6) |
