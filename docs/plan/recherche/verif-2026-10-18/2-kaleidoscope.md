# Vérification 2 : nourriture, chaleur et saisons, autour de Kaleidoscope Cookery

Vérifié le 2026-10-18. J'ai ouvert les jars, lu les JSON et désassemblé les classes avec `javap`. Les numéros de version viennent de l'API Modrinth (NeoForge, 1.21.1). Les jars sont extraits sous `scratchpad/jars/<mod>-<version>/x/`.

Notation des preuves :
- **[jar]** : JSON ou classe lus dans le jar ;
- **[MR]** : version ou changelog lus sur Modrinth ;
- **[déduit]** : raisonnement à partir du code, sans test en jeu.

## Sommaire

0. Versions vérifiées
1. Ce qui est confirmé, mod par mod
2. Les liens à construire (quoi, avec quel outil)
3. Conflits et risques
4. Ce qu'il faut adapter, supprimer ou ajouter dans le plan
5. Questions à trancher par un test en jeu

---

## 0. Versions vérifiées [MR]

| Mod | Dans le pack | Dernière version 1.21.1 NeoForge | Remarque |
|---|---|---|---|
| Kaleidoscope Cookery | 1.5.1 | **1.6.0** (2026-09-30) | 1.6.0 : le moulin accepte toute créature de plus d'un bloc de haut (liste noire au lieu d'une liste blanche), enchantement « Quick Knife » pour le couteau, la planche à découper donne plusieurs résultats |
| KaleidoscopeCookery-Automation | 1.0.2 | 1.0.2 | Demande KC ≥ 1.2.1 et Create ≥ 6.0.6 |
| Kaleidoscope Compat | — (ajout prévu) | **2.9.7-Patch.2** (2026-10-10) | Demande KC ≥ 1.4.0. Utilise **Resourceful Config**, déjà dans le pack (3.0.11), sans le déclarer comme dépendance [jar : annotations `com.teamresourceful.resourcefulconfig` dans `MainConfig`] |
| Let's Do Farm & Charm | 1.1.25 | **1.1.29** (2026-10-16) | Le 1.1.27 ajoute une **planche à découper** et des **hachoirs** (fer, diamant, netherite), deux doublons de KC [MR changelog, jar 1.1.29] |
| Freeze It and Heat It | — (ajout prévu) | 4.1.1 (2026-09-19) | Demande `cold_sweat` dans `[2.3, 3)` : compatible avec 2.4.3.1 et 2.4.4 [jar `neoforge.mods.toml`] |
| Cold Sweat | 2.4.3.1 | 2.4.4 (2026-10-16) | 2.4.4 refond la température de l'eau (point de gel par biome) |
| Ecliptic Seasons | 0.15.0-rc-3-1 | **0.15.4.1** (bêta, 2026-10-16) | 0.15.4 corrige les cultures des régions froides qui poussaient comme en été au début de l'hiver |
| Thirst Was Reclaimed | 3.0.6 | 3.0.6 | modid `thirst`, paquet `cn.mlus.thirst` |
| Traveler's Backpack | — (ajout prévu) | 10.1.39 (2026-08-29) | — |

---

## 1. Ce qui est confirmé

### 1.1 Kaleidoscope Cookery 1.5.1

**Postes et chaleur** [jar] :
- Le **wok** (`kaleidoscope_cookery:pot`), la **marmite** (`stockpot`), le **cuiseur vapeur** (`steamer`) et la **théière** (`teapot`) cherchent la chaleur dans le **bloc juste en dessous** (`hasHeatSource`).
- La règle est la même pour les quatre :
  - si le bloc a la propriété `lit`, il doit être allumé ;
  - sinon, il doit être dans le tag `kaleidoscope_cookery:heat_source_blocks_without_lit`, qui contient feu, feu des âmes, lave, bloc de magma et `#farmersdelight:heat_sources`.
- KC fournit lui-même `farmersdelight:heat_sources`, qui contient son fourneau : le tag se charge même sans Farmer's Delight.
- Conséquence : **un feu de camp vanilla allumé suffit** pour le wok, la marmite et la théière. Le fourneau de Farm & Charm aussi, car il utilise `BlockStateProperties.LIT` [jar F&C `StoveBlock`]. Même une bougie allumée suffit [déduit].
- Le **fourneau KC** (`stove`) s'allume au briquet ou à la boule de feu, sans combustible, et brûle sans limite. Il s'éteint s'il pleut sur lui (`isRainingAt` au bloc du dessus, dans `randomTick`). La pluie locale d'Ecliptic décide donc s'il reste allumé [jar].

**Effet « Chaleur »** [jar `WarmthEffect`, lang] :
- Une vingtaine de plats donnent l'effet `kaleidoscope_cookery:warmth`.
- Il régénère la vie près d'un bloc allumé ou d'un bloc du tag `warmth_heat_source_blocks`.
- Il ne touche pas la température de Cold Sweat.

**Autres effets** :
- `tundra_strider` : rapide sur la neige. C'est un vrai plat d'hiver avec Ecliptic.
- `mustard` (les creepers fuient) et `sulfur` (les phantoms fuient) n'ont **aucun effet** dans Fallow, puisque ces monstres sont supprimés.

**Cultures** : tomate, piment, laitue, riz (+ riz sauvage), théier.
- Les graines tombent de l'herbe (`minecraft:blocks/short_grass`) **seulement si l'on porte le chapeau de paille** (modificateur de butin `straw_hat_seed_drop`).

**Tags de saison** [jar] :
- `sereneseasons:{spring,summer,autumn}_crops`, en bloc et en objet :
  - tomate : été, automne ;
  - piment : été, automne ;
  - riz : été, automne ;
  - laitue : printemps, automne ;
  - théier : printemps, été, automne.
- Tags d'**humidité** Ecliptic, `eclipticseasons:crops/<humidité>`, par exemple riz `humid_humid`, tomate `dry_average`.
- Ecliptic **lit bien les tags `sereneseasons:*`** et les convertit [jar Ecliptic `CompatCropHookInternal`]. Les cultures KC sont donc saisonnières sans rien faire.

**Types de recettes JSON** [jar, décompte des fichiers] :

| Type | Recettes |
|---|---|
| `kaleidoscope_cookery:pot` | 198 |
| `flex_pot` | 27 |
| `stockpot` | 77 |
| `flex_stockpot` | 28 |
| `millstone` | 85 |
| `bamboo_tray` | 28, avec `subtype` `drying` ou `wetting` et `duration` |
| `chopping_board` | 16 |
| `teapot` | 7 |
| `steamer` | 4 |
| `rice_bowl` | 5 |

**Plateau en bambou** :
- Il sèche au ciel ouvert et sans pluie (`canSeeSky`, `isRainingAt`) [jar].
- **Aucune recette de viande séchée** n'existe par défaut : le séchage ne vise que le thé, le kelp, la boue, la chair putréfiée (qui donne du cuir) et l'éponge.

**API KubeJS** [jar `ModKubeJSPlugin`] :
- Schémas de recettes dans l'espace `kaleidoscope_cookery` : `pot`, `flex_pot`, `chopping_board`, `stockpot`, `flex_stockpot`, `millstone`, `steamer`, `bamboo_tray`.
- **Pas de schéma pour la théière** : passer par `event.custom({...})`.
- Liaisons `KCookery` et `KCookerySoupBase` (bases de soupe).

Champs des schémas :

| Schéma | Champs |
|---|---|
| `pot` | `result, ingredients, carrier, time, stir_fry_count` |
| `stockpot` | `result, ingredients, soup_base, time, carrier, cooking_texture, …` |
| `bamboo_tray` | `result, ingredient, subtype, duration` |
| `steamer` | `result, ingredient, cook_tick` |
| `chopping_board` | `result, ingredient, model_id, cut_count` |

**Événements Java publics** : `StockpotMatchRecipeEvent`, `MillstoneMatchRecipeEvent`, `RecipeItemEvent`, `SickleHarvestEvent`. KubeJS ne les écoute qu'avec `NativeEvents` [déduit].

**Moulin** [jar] :
- Il est tiré par une bête, choisie par le tag `millstone_bindable` (âne, mule, cheval, lama, vache, mouton, chèvre, villageois) et le datamap `millstone_bindable_data`.
- En 1.6.0, toute créature de plus d'un bloc de haut peut le tirer, sauf liste noire [MR].

**Contenu en trop pour Fallow** :
- Chaises, tables, tabourets, banc et « table des huit immortels », en bois vanilla, plus les variantes d'Every Compat (compatibilité dans KC).
- Épouvantail, chapeau de paille et vêtements de fermier.
- Maisons de village ajoutées (`AddVillageStructuresEvent`).

### 1.2 KaleidoscopeCookery-Automation 1.0.2 [jar]

- Points d'interaction du **bras mécanique Create** : wok, marmite, cuiseur vapeur, broche à shawarma, table, corbeille à fruits.
- Lien avec les colis de Create 6 : la recette garde l'adresse du colis (`PackageSender`).
- 8 recettes `create:cutting` qui produisent les morceaux de viande KC.
- Pas de configuration.

### 1.3 Kaleidoscope Compat 2.9.7-Patch.2 : ce qu'il fait vraiment [jar]

**Mode des datapacks** (`datapack_mode`, valeur par défaut **COMPAT**) :
- **COMPAT** : ajoute les objets F&C aux tags `c:`, pour qu'ils soient interchangeables (laitue F&C dans `c:crops/lettuce`, bacon F&C dans `c:foods/raw_bacon`, morceaux de poulet F&C dans `c:foods/raw_chicken`).
  - Ajoute au hachoir F&C (mincer) les recettes tomate et laitue KC → graines KC.
  - Plusieurs recettes visent `farmersdelight:*` ou `candlelight:*` : elles échouent, avec une erreur dans le journal et sans autre effet.
- **UNITE** : supprime les doublons. **Dangereux sans Farmer's Delight** :
  - le pack `unite_farm_and_charm` remplace des recettes F&C par des versions qui produisent `farmersdelight:onion`, `:bacon`, `:chicken_cuts` et `:raw_pasta` ;
  - ces recettes échouent, et les originales de F&C disparaissent avec elles (même chemin, pack prioritaire) ;
  - il **coupe la génération** des carottes, pommes de terre, betteraves, **oignons** et tomates sauvages de F&C (`rarity_filter` 9999999999), en supposant que Farmer's Delight les fournit ;
  - **dans Fallow, l'oignon deviendrait introuvable** [déduit des JSON].
- **NONE** : seulement les bases de soupe.

**Réglage `cooking_pot_recipes_disabled`** (catégorie `farm_and_charm`, faux par défaut) :
- Malgré son nom, il ne désactive pas les recettes de cuisson de F&C.
- Il ajoute un datapack qui **annule la fabrication** du bloc `farm_and_charm:cooking_pot`, avec la condition `forge:false`, et le met dans le tag `kaleidoscope_cookery:united`, qui le cache de l'onglet créatif et de JEI.
- Les recettes `farm_and_charm:pot_cooking` restent, et **Compat les rejoue dans la marmite KC** (`afterStockpotRecipeMatch` sur `StockpotMatchRecipeEvent.Post`, si `farm_and_charm_compat_enabled`, vrai par défaut).
- Les soupes, les thés (ortie, plantain, fraise), la levure et le goulash de F&C se cuisinent donc dans la marmite KC.

**Soif** (`thirst_compat_enabled`, vrai par défaut) :
- Enregistre l'hydratation de **toutes les soupes KC**, de **tous les thés KC** (lus dans `TeacupRegistry`) et des boissons de Kaleidoscope Tavern.
- Passe par `cn.mlus.thirst.api.ThirstHelper`, présent dans TWR 3.0.6 [jar des deux mods].

**Create** :
- Bras mécanique pour wok, marmite, cuiseur vapeur, moulin, broche, théière (`create.arm.*_enabled`).
- Interaction sur **engins Create** pour 16 blocs KC (`create.contraption.*`) : une cuisine dans un wagon sans Kaleidoscope Contraption.

**Divers** :
- Correctif de la récolte à la faucille avec Quark (`quark.sickle_harvest_fix_enabled`).
- Épouvantail qui repousse les phantoms (sans effet ici).
- Liste noire du « Transmutation Lunch Bag ».
- Compatibilités AppleSkin, Curios et Accessories.

**Ce que Compat ne règle pas** :
- Les doublons de tomate et de laitue restent en mode COMPAT.
- Le fourneau, la rôtissoire, le bol de pétrissage, le hachoir, l'épouvantail F&C, la nouvelle planche à découper et les hachoirs de F&C ne sont pas touchés.
- Le masquage ne vise que **JEI** (`compat/jei/RecipeHiding`). Le plugin **EMI** de Compat n'ajoute qu'une catégorie (barbecue).
- **Compat ne sait pas que KC-Automation existe** : aucune référence à `kaleidoscopecookery_automation` dans le jar.

### 1.4 Let's Do Farm & Charm 1.1.25 (et 1.1.29) [jar]

**Types de recettes** : `mincer` (53), `drying` (19, silo et rebord de fenêtre), `pot_cooking` (10), `stove` (9), `roaster` (9), `crafting_bowl` (4). Le 1.1.27 ajoute `cutting_board` (36) et `cutting_board_assembly` (9).

**Cultures** : orge, avoine, maïs, fraise, oignon, tomate et laitue (doublons de KC), plus 13 plantes sauvages.

**Saisons : les tags F&C sont mal rangés.**
- Ils sont dans `data/sereneseasons/block/*.json` et `data/sereneseasons/item/*.json`, **sans le dossier `tags/`**. Ce ne sont donc pas des tags, et Ecliptic ne les lit pas. Même erreur en 1.1.25 et en 1.1.29.
- Les fichiers `data/farm_and_charm/seasons/*.json` (coefficients par saison) ne servent à rien sous NeoForge [déduit].
- **Les cultures F&C poussent toute l'année avec Ecliptic** tant que Fallow ne les tague pas.

**Configuration NeoForge** : `enableBonemealEffect`, `fertilizedSoilRange`, `waterSprinklerRange`, `enableRainGrowthEffect`, `rainGrowthMultiplier`, `feedingTroughRange`, `enableFertilizer`, taming et effets du cheval, de la poule, du chien et du chat, valeurs nutritives de chaque plat [jar `FarmAndCharmNeoForgeConfig`].

**Mangeoire** : sa nourriture est le tag `farm_and_charm:feeding_trough_food`, qui contient `#minecraft:villager_plantable_seeds`. KC y ajoute ses graines.

**Engrais** : la recette silo `fertilizer_from_rotten_flesh` existe déjà (chair putréfiée → engrais).

### 1.5 Freeze It and Heat It 4.1.1 [jar]

**Mécanique** : chaque aliment porte une température. Selon le lieu, il devient frais, puis légèrement, presque ou complètement gelé ou pourri. Manger un aliment altéré donne un malus (effet `fiahi:shiver`). Un aliment complètement pourri devient un reste (`fiahi:leftover_meat` ou `fiahi:leftover_vegetable`, compostables à 65 %), selon des recettes `fiahi:food_rotting` pilotées par les données.

**Où il mesure la température** :
- **Inventaire du joueur** : température `CORE` du joueur dans Cold Sweat (`InventoryMixin`).
- **Objet au sol** : `WorldHelper.getTemperatureAt`.
- **Conteneurs** : à chaque tick de monde, parcourt les block entities `Container` des chunks chargés (`ForgeEventHandler.onLevelTick`, `collectContainerPositionsByChunk`).

**Exemptions** : la chaudière et la glacière de Cold Sweat (`STABLE_TEMPERATURE_CONTAINERS`) ; elles ramènent l'aliment à l'état frais grâce aux mixins de FIAHI.

**Sac à provisions** (`fiahi:food_pouch`) : il garde une température propre et peut aller dans la glacière ou la chaudière.

**Configuration** (`fiahi-common-config`) : `NEVER_FROZEN_FOODS`, `NEVER_ROTTEN_FOODS`, `ENABLE_FROZEN`, `ENABLE_ROTTEN`, `TEMPERATURE_CHECKER_INTERVAL`, `TEMPERATURE_BALANCE_RATE`, `FROZEN_SPEED_MULTIPLIER`, `ROTTEN_SPEED_MULTIPLIER`, `STABLE_TEMPERATURE_CONTAINERS`.

**Pas d'API KubeJS.** On le règle par la configuration, les tags et les recettes `fiahi:food_rotting`.

### 1.6 Cold Sweat 2.4.3.1 [jar]

**Blocs chauds par défaut** (`coldsweat/world.toml`, « Block Temperatures ») :

| Bloc | Réglage | Lecture |
|---|---|---|
| Chaudière allumée | `15 °F, portée 7, max 36` | — |
| Glacière givrée | `-15 °F` | — |
| Lave, chaudron de lave | `30 °F, portée 7` | — |
| `#minecraft:fire` | `25 °F, portée 7` | — |
| `#minecraft:campfires` | `["#minecraft:campfires", 25, 7, "f", 50, "lit=true", "", 400]` | **+25 °F, 7 blocs, effet cumulé max 50 °F** |
| Magma | `20 °F` | — |
| Glaces | négatif | — |
| `create:blaze_burner` | selon son état | — |

- Fours, fumoirs et hauts-fourneaux vanilla allumés : par code (`FurnaceBlockTemp`, `AbstractFurnaceBlock` + `lit`).
- **Les fourneaux KC et F&C ne comptent pas.**

**Données par fichiers** : `data/<ns>/cold_sweat/{block/block_temp, item/food, item/fuel, world/biome_temp, world/temp_region, entity/...}`.

**API KubeJS** [jar `KubePlugin`, `KubeBindings`, `ModRegistriesEventJS`] :
- **Liaison `coldsweat`** :
  - `getTemperature(entity, trait)`, `setTemperature` ;
  - `convertTemperature(valeur, de, vers, absolu)` ;
  - **`createModifier(id)`** : **un seul argument**, il cherche dans le registre ;
  - `addModifier(entity, modifier, trait)` ;
  - `getModifiers`, `getTrait` ;
  - `getBlockTemperature(LevelBlock)`, `getBiomeTemperature(level, pos)`, `getTemperatureAt(level, pos)`.
- **Groupe `ColdSweatEvents`** (scripts serveur) : `registries`, `gatherDefaultModifiers`, `temperatureChanged`, `addModifier`, `applyInsulation`.
- **`registries`** offre :
  - `addBlockTemperature(temp, unités, b => b.blocks(...).state('lit', true).range(r).maxEffect(m).fades(true))` ;
  - `addFoodTemperature(f => f.items(...).temperature(t).duration(d))` ;
  - `addBiomeTemperature` et `addBiomeOffset`, `addStructureTemperature` ;
  - `addHearthFuel`, `addBoilerFuel`, `addIceboxFuel` ;
  - **`addTempModifier(id, data => temp => nouvelleTemp)`**, qui crée un vrai modificateur Cold Sweat dont la fonction JS est appelée à chaque calcul.

**Point clé pour les saisons** [jar `WorldHelper.getTemperatureAt`, `getDummyPlayer`] :
- La température d'un lieu se calcule avec un « joueur fantôme » qui reçoit les **modificateurs par défaut** (`EntityTempManager.gatherTempModifiers`, qui déclenche `DefaultTempModifiersEvent`, relayé à KubeJS par `gatherDefaultModifiers`).
- Un modificateur saisonnier ajouté dans `gatherDefaultModifiers` sur le trait `world` s'applique donc **à la fois** :
  - aux joueurs ;
  - à la température des conteneurs lue par FIAHI ;
  - à la liaison `getTemperatureAt` [déduit du bytecode].

**Saisons natives** : seulement Serene Seasons (`SereneSeasonsTempModifier`, réglages « Summer/Autumn/Winter/Spring Temperatures »). **Rien pour Ecliptic.**

**Compatibilité Thirst** : la pureté de l'eau est gardée dans l'outre (`CompatManager$Thirst`).

**Glacière** : elle accepte `#c:foods` (`cold_sweat:icebox_valid`). Les plats KC (tag `kaleidoscope_cookery:meals`) ne sont probablement pas dans `c:foods` [déduit : KC ne tague que des sous-tags `c:foods/raw_*` et `cooked_*`].

**Sommeil** : la liste « Sleep Check Override Blocks » ne contient par défaut que `#comforts:sleeping_bags`.

### 1.7 Ecliptic Seasons (0.15.0-rc3 dans le pack, 0.15.4.1 lu) [jar]

**Cultures, pilotées par les données** :
- `data/eclipticseasons/eclipticseasons/crop/seasons/<combinaison>.json` donne une chance de croissance par terme solaire ; même principe pour `crop/humidity/<h1>_<h2>.json`.
- Les cibles sont des **tags de bloc** `eclipticseasons:crops/<spring|summer|…|spring_summer|…|all_seasons>` et `eclipticseasons:crops/<humidité>`.
- Tags d'objet de même nom.
- Exemptions : `crops/unaffected_by_seasons`, `not_killed_by_climate`.

**Reproduction saisonnière** : tags d'entité `eclipticseasons:breed/<saisons>`.
- Par exemple, vache, mouton, cochon, cheval, âne, lama et grenouille sont en `spring_summer` ; loup et abeille en `spring`.
- **Aucun animal moddé n'est tagué.**

**Climats agricoles** : `agro_climate` (tempéré, chaud, froid…). Les tags de biome `eclipticseasons:agro/*` sont vides par défaut ; le choix se fait par code selon la température du biome [déduit].

**Configuration** : `HeatStroke`, `EnableSeasonalCrop`, `EnableCropHumidityControl`, `RestrictBoneMeal`, `ForceCompatMode`, serres, `EnableSeasonalBreed`, `EnableTimeBreed`, `EnableSeasonalBee`, `EnableSeasonalFishing`, `FishingSeasons`, `IceAndSnow`, `WaterFreezesInFrozenBiomes`, `SnowKeepInSnowyBiomes`…

**KubeJS** :
- `EclipticSeasonsApi`, qui est l'**instance** de l'API : `getSeason(level)`, `getSubSeason(level)`, `getSolarTerm(level)`, `getSeasonSignal(level, pos)` (saison du climat local), `isRainAt`, `getAdjustedHumidity`, `isSnowAt`…
- `EclipticSeasonsBindings`.
- Énumération `Season$Sub` : `EARLY_SPRING` … `LATE_WINTER`, `NONE`. Les noms du brouillon sont justes.
- **Ecliptic ne change pas la température vanilla des biomes** (`MixinBiome` ne touche que la pluie) : Cold Sweat ne voit donc aucune saison sans pont.

### 1.8 Thirst Was Reclaimed 3.0.6 [jar]

**Fichiers** : `config/thirst/common.toml`, `item_settings.toml` (listes « Drinks » et « Foods » au format `[["id", hydratation, désaltération]]`, plus une liste noire), `keyword.toml`, `container.toml`.

**Mots-clés par défaut** :
- boissons : `drink|juice|tea|soda|coffee|wine|beer|cider|…` ;
- soupes : `soup|stew|porridge` ;
- fruits : `…berry|berries|apple|melon…`.
- Les thés F&C (`nettle_tea_cup`…) et les soupes F&C (`barley_soup`…) sont donc **déjà reconnus**.
- La **fraise F&C ne l'est pas** : « strawberry » ne correspond pas au motif, qui exige un séparateur avant « berry ».

**Valeurs par défaut** : la tomate KC et quelques soupes KC y figurent déjà.

**Lien avec Cold Sweat** : TWR lit la température **corporelle** (`Temperature.get(…, BODY)`) pour accélérer la soif. Un été plus chaud par le pont saisonnier donne donc plus de soif, sans rien ajouter.

**Purification** : recettes `campfire_cooking` et four pour bouteille, seau et bol en terre cuite ; filtre à sable Create.

**API** :
- `ThirstHelper.addDrink(item, h, q)` et `addFood`, statiques ;
- événement NeoForge `RegisterThirstValueEvent`.

### 1.9 Traveler's Backpack 10.1.39 [jar]

**Améliorations** : réservoirs, établi, **four**, **fumoir**, haut-fourneau, ramassage, juke-box, aimant, poubelle, **alimentation automatique**, réapprovisionnement, lanterne (lumière dynamique avec LambDynamicLights).

**Configuration serveur** :
- par amélioration : `enableTanksUpgrade`, `enableCraftingUpgrade`, `enableFurnaceUpgrade`, `enableSmokerUpgrade`, `enableBlastFurnaceUpgrade`, `feedingUpgradeSettings.enableFeedingUpgrade`, `enableLanternUpgrade`… ;
- `enableSleepingBag`, `quickSleepingBag`, `enableSleepingBagSpawnPoint`.

**Sac de couchage** : blocs `travelersbackpack:<couleur>_sleeping_bag`, plus le tag d'objet `travelersbackpack:sleeping_bags`.

**Tuyau** : on peut boire l'eau des réservoirs (`WaterEffect`), mais l'effet ne fait qu'éteindre le feu et donner un effet selon la température du biome. **Il ne connaît que Tough As Nails (`compat/toughasnails`), pas TWR.** Boire au tuyau ne désaltère donc pas dans Fallow [déduit].

**Inventaire** : le sac posé (`BackpackBlockEntity`) **n'implémente pas `Container`**, et le sac porté n'est pas dans l'`Inventory` du joueur. **FIAHI ne fait donc jamais vieillir la nourriture rangée dans le sac** [déduit].

---

## 2. Les liens à construire

### 2.1 Chaleur : cuisine → Cold Sweat

| Quoi | Outil | Détail |
|---|---|---|
| Fourneau KC et fourneau F&C allumés réchauffent la pièce | KubeJS `ColdSweatEvents.registries`, ou JSON `data/fallow/cold_sweat/block/block_temp/` | `event.addBlockTemperature(12, 'f', b => b.blocks('kaleidoscope_cookery:stove','farm_and_charm:stove').state('lit', true).range(4).maxEffect(20).fades(true))`. Effet **plus faible** que le feu de camp (25 °F, 7 blocs), parce que le fourneau KC **brûle sans combustible** : il ne doit pas remplacer le foyer (hearth) de Cold Sweat |
| Feu de veille (palier 1 des clairières) | Tag + KubeJS Cold Sweat | Le brouillon en fait un bloc KubeJS simple, sans état `lit`. L'ajouter à `farmersdelight:heat_sources` (pour que le wok le reconnaisse), à `kaleidoscope_cookery:warmth_heat_source_blocks` et à une entrée `addBlockTemperature` (~20 °F, 6 blocs). Il ne purifie pas l'eau : seul le feu de camp vanilla porte les recettes TWR |
| Chaudrons qui bouillent, braseros, cheminées (10.7) | Config ou KubeJS Cold Sweat | Ces blocs viennent d'Amendments et de Supplementaries, pas de KC : ils sont hors de cette vérification. Seuls les blocs ayant un état `lit` ou `boiling` se branchent avec `.state(...)` |
| Plats chauds qui réchauffent, plats froids qui rafraîchissent | KubeJS `event.addFoodTemperature(...)` | Plats chauds : soupes KC (`borscht`, `pork_bone_soup`, `lamb_and_radish_soup`, `laba_congee`…), soupes F&C, thés KC. Plats froids : `cold_*_sashimi`, `frost_lamb_chop`, `cold_cut_ham_slices`. Donne enfin un sens à l'effet « Chaleur » de KC en hiver. À étalonner avec `coldsweat.convertTemperature` |
| Glacière et chaudière acceptent les plats KC et F&C | Tags `cold_sweat:icebox_valid`, `cold_sweat:boiler_valid` | Ajouter `#kaleidoscope_cookery:meals`, `#kaleidoscope_cookery:feasts` et les plats F&C. Sans cela, la glacière ne prend que `#c:foods` |

### 2.2 Saisons : Ecliptic → Cold Sweat, FIAHI, soif

**Réécrire `saisons_cold_sweat.js`.** Le seul exemplaire est le brouillon `plan/kubejs-brouillons/server_scripts/saisons_cold_sweat.js` ; aucune branche du dépôt ne le contient (`test/agriculture` n'a que des `.gitkeep` dans `pack/kubejs/`).

**Ce qui ne va pas dans le brouillon** :
- `coldsweat.createModifier('fallow:season', offset)` n'existe pas : un seul argument, lu dans le registre.
- Le tick toutes les 1200 ticks ne touche que les joueurs.
- La température des conteneurs, donc FIAHI, ne voit pas la saison.

**Bonne architecture** (KubeJS suffit, [jar]) :

```js
// server_scripts/saisons_cold_sweat.js (esquisse, valeurs en °C à étalonner)
const FALLOW_SAISON_C = { EARLY_SPRING: -2, MID_SPRING: 0, LATE_SPRING: 1, EARLY_SUMMER: 3, MID_SUMMER: 6,
  LATE_SUMMER: 4, EARLY_AUTUMN: 1, MID_AUTUMN: -2, LATE_AUTUMN: -4, EARLY_WINTER: -6, MID_WINTER: -8, LATE_WINTER: -6 }

ColdSweatEvents.registries(event => {
  event.addTempModifier('fallow:saison', data => {
    const level = data.entity().level()
    if (String(level.dimension()) !== 'minecraft:overworld') return t => t
    const sub = String(EclipticSeasonsApi.getSubSeason(level))
    const off = coldsweat.convertTemperature(FALLOW_SAISON_C[sub] ?? 0, 'c', 'mc', false)
    return t => t + off
  })
})

// Ajouté à toute entité suivie ET au « joueur fantôme » qui calcule la température d'un lieu.
ColdSweatEvents.gatherDefaultModifiers(event => {
  event.addModifier('world', coldsweat.createModifier('fallow:saison'))
})
```

**Ce que ce script relie à lui seul** :
1. La température ressentie, selon la saison.
2. Le gel et le pourrissement de FIAHI selon la saison, dans l'inventaire (température du corps) **et** dans les coffres (température du lieu).
3. La soif de TWR, qui dépend de la température du corps.
4. L'eau qui gèle, si la gestion du gel par Cold Sweat est active (voir 3.5).

**Variante plus fine** : utiliser `getSeasonSignal(level, pos)` pour les climats chaud et froid. Cette fonction rend une `Season`, pas une sous-saison, donc moins de nuances.

**Ecliptic** : `HeatStroke = false` (déjà prévu).

### 2.3 Conservation : FIAHI dans le jeu

| Lien | Outil | Détail |
|---|---|---|
| Aliments préservés jamais pourris | Config FIAHI `NEVER_ROTTEN_FOODS` et `NEVER_FROZEN_FOODS` | Thé séché KC, varech séché, pain, riz cuit en tube de bambou (à choisir). La configuration attend des **identifiants**, pas des tags : générer la liste |
| Restes → engrais | Recette JSON `farm_and_charm:drying` | `fiahi:leftover_meat` et `fiahi:leftover_vegetable` donnent `farm_and_charm:fertilizer`, comme la recette existante pour la chair putréfiée. La boucle se ferme : le gâchis nourrit le champ |
| Pourriture spécifique | Recettes `fiahi:food_rotting` (`event.custom`) | Par exemple, la tomate KC pourrie devient `farm_and_charm:rotten_tomato`, si on garde cet objet |
| Rations de voyage | Sac à provisions FIAHI | Il ralentit le gel et le pourrissement. C'est l'objet des expéditions |
| Effet KC « Préservation » | Test | Il annule les malus de la chair putréfiée ; vérifier s'il annule aussi `fiahi:shiver` |

### 2.4 Cultures et saisons : tags Ecliptic

**F&C** : datapack ou KubeJS, `ServerEvents.tags('block')` **et** `('item')`.
- Il faut taguer les **blocs** de culture : `farm_and_charm:barley_crop`, `oat_crop`, `corn_crop`, `strawberry_crop`, `onion_crop`, et, si on les garde, `tomato_crop` et `lettuce_crop`.
- Il faut aussi taguer les graines (objets), pour les infobulles.
- Le brouillon `cuisine_unifiee.js` ne tague que des **objets** `sereneseasons:*`. Ecliptic décide de la croissance par le **bloc** : il faut ajouter `eclipticseasons:crops/<saison>` sur les blocs.

Répartition proposée :

| Culture | Saisons | Humidité |
|---|---|---|
| Orge | `spring_summer` | `dry_average` |
| Avoine | `spring_summer_autumn` (rustique) | `average_moist` |
| Maïs | `summer` | `average_moist` |
| Fraise | `spring_summer` | `average_moist` |
| Oignon | `spring_autumn` | `dry_average` |
| **Hiver** | rien dehors : serre d'Ecliptic ou réserves | — |

**KC** : déjà tagué, rien à faire.

**Clairière** (`fallow:clairiere`, température 0.7, précipitations 0.6) :
- Elle tombera dans le climat **tempéré** d'Ecliptic [déduit : tags `agro` vides, choix par la température].
- Le riz (`humid_humid`) y poussera mal : c'est une culture de marais.
- Ajouter le biome aux tags `eclipticseasons:seasonal`, couleur saisonnière, pour qu'il change de teinte avec les saisons, si on le veut.

### 2.5 Soif : boissons des deux mods

| Source | Déjà réglé ? | À faire |
|---|---|---|
| Soupes et thés KC | **Oui**, par Kaleidoscope Compat (`thirst_compat_enabled = true`) | Rien |
| Soupes et thés F&C | **Oui**, par les mots-clés TWR | Rien, ou valeurs fines dans `item_settings.toml` |
| Fraise F&C, tomate F&C, plateaux de fruits KC | Tomate KC oui (défaut TWR), le reste non | `item_settings.toml` → Foods |
| Eau du puits F&C | Non | Hors de cette vérification (pureté de bloc TWR). Le puits pompe une source à 6 blocs sous lui (F&C 1.1.26) |
| Réservoirs de Traveler's Backpack | **Non**, le tuyau ne désaltère pas | Mod custom (voir 2.9), ou réservoirs utilisés seulement pour remplir bouteilles et outres |
| Outre de Cold Sweat | Pureté TWR gardée, `CompatManager$Thirst` | Test : désaltère-t-elle avec TWR ? |

### 2.6 Nourriture des animaux du zoo et de la ferme

Tout est piloté par des tags [jar des mods de faune] :
- **Vanilla 1.21.1** : `minecraft:cow_food`, `sheep_food`, `goat_food` += orge et avoine F&C ; `pig_food` += maïs F&C ; `chicken_food` += `farm_and_charm:kernels` et graines F&C (KC y ajoute déjà ses graines).
- **Naturalist** : `naturalist:bird_food_items`, `duck_food_items`, `boar_food_items`, `capybara_food_items`, `turkey_food_items`… (graines vanilla seulement) += graines F&C et KC, riz KC.
- **Alex's Mobs** : 51 tags `alexsmobs:*_breedables` (gazelle : blé, élan : pissenlit…) += avoine et orge F&C pour les herbivores.
- **Untitled Duck Mod** : `duck_breeding_food` et `goose_breeding_food` incluent déjà `#c:seeds`, donc les graines F&C et KC : **déjà relié**.
- **No Man's Land** : `nomansland:deer_food` (citrouille, pomme, poire) += fraise F&C ; `moose_food` = carotte dorée.
- **Critters and Companions** : `red_panda_food` = bambou, `otter_food` = poissons. Rien à ajouter.

**Reproduction saisonnière** : tags `eclipticseasons:breed/<saisons>` pour les espèces du zoo.
- Cerfs, ours et oiseaux au printemps, par exemple.
- Les identifiants d'entités sont à lire dans chaque mod.
- Sans tag, l'espèce se reproduit toute l'année [déduit].

**Moulin KC tiré par les bêtes du zoo** :
- en 1.5.1, ajouter les entités voulues à `kaleidoscope_cookery:millstone_bindable` et au datamap `millstone_bindable_data` (vitesse) ;
- en 1.6.0, c'est automatique pour toute bête haute.

**Mangeoire F&C** : elle met les animaux en amour. Retirer sa recette (`event.remove({ output: 'farm_and_charm:feeding_trough' })`), comme le prévoit le brouillon. L'abreuvoir peut rester.

### 2.7 Doublons à supprimer et ce qu'il faut garder

| Domaine | KC | F&C | Garder | Comment |
|---|---|---|---|---|
| Tomate | `tomato`, `tomato_seed`, `tomato_crop` | `tomato`, `tomato_seeds`, `tomato_crop` sur corde, `rotten_tomato`, `wild_tomatoes` | **KC** | Compat en mode COMPAT rend les deux interchangeables (`c:crops/tomato`). Retirer la recette `farm_and_charm:seeds_tomato`. Avec LootJS, faire tomber des graines KC des tomates sauvages F&C (on garde ainsi la découverte dans le monde). Cacher les objets F&C dans EMI |
| Laitue | `lettuce`, `lettuce_seed` | `lettuce`, `lettuce_seeds`, `wild_lettuce` | **KC** | Même traitement |
| Farine | `flour` (moulin) | `flour` (hachoir) | **KC** | Changer le résultat de `farm_and_charm:mincer/flour` en KC (comme le mode UNITE, mais sans ce mode). Les recettes F&C lisent `#farm_and_charm:flour` = `#c:flour` : la farine KC passe |
| Pâte | `raw_dough` | `dough` (bol de pétrissage + levure) | **KC** | Changer le résultat de `farm_and_charm:crafting_bowl/dough` en `kaleidoscope_cookery:raw_dough` (comme le fait UNITE) |
| Marmite | `stockpot` | `cooking_pot` | **KC** | Compat : `cooking_pot_recipes_disabled = true` (retire la fabrication ; les recettes sont rejouées dans la marmite KC) |
| Fourneau | `stove` | `stove` (four à pain : pain de fermier, gâteau, maïs grillé, plats rôtis) | **Les deux** | KC n'a **pas de four à pain**. Le fourneau F&C est aussi une source de chaleur pour le wok. Le retirer, comme le propose le brouillon, ferait perdre 9 plats ; le garder comme « four » de la ferme |
| Rôtissoire, poêle | `pot` (wok) | `roaster` | **KC** | Retirer la rôtissoire et **porter ses 9 recettes** vers `kaleidoscope_cookery:pot` (schéma KubeJS natif) |
| Bol de pétrissage | — | `crafting_bowl` | À trancher | Seule source de pâte F&C et des aliments pour animaux (avant 1.1.27). Le garder tant que F&C reste en 1.1.25 |
| Planche, couteaux | `chopping_board`, couteaux de cuisine | `cutting_board`, hachoirs fer, diamant, netherite (**1.1.27 et plus**) | **KC** | Rester en **F&C 1.1.25**, ou, si on monte en 1.1.29, retirer la planche et les hachoirs et porter les 36 recettes de découpe (fleurs → teinture, aliments pour animaux…) vers `kaleidoscope_cookery:chopping_board` |
| Moulin, hachoir | `millstone` (tiré par une bête) | `mincer` (manuel, 53 recettes, dont minerais, écorçage du bois, graines) | **Les deux** | Rôles différents ; seule la farine est en double (voir plus haut) |
| Séchage | Plateau en bambou | Rebord de fenêtre, silo (`drying`) | Les deux | Le silo fait l'engrais et la poudre d'os ; le plateau fait le thé |
| Épouvantail | `scarecrow` | `scarecrow` (fait pousser) | **F&C** | Retirer la recette KC `kaleidoscope_cookery:scarecrow` |
| Thés | Théière (7 thés) | Thés au pot (ortie, plantain, fraise) et `tea_jug` | Les deux | Les thés F&C passent dans la marmite KC grâce à Compat |
| Paille | `straw_block` | `straw_stable_floor` | Les deux | Rôles différents |
| Mobilier | Chaises, tables, tabourets, banc | — | À trancher avec Handcrafted (10.6) | Doublon de **Handcrafted**. Proposition : garder le **tabouret de cuisinier**, utile à KC, et cacher le reste |

**Masquer dans EMI** : Compat ne masque que pour JEI. Il faut un script client KubeJS (`RecipeViewerEvents.removeEntries` ; la prise en charge d'EMI par KubeJS 2101 reste à vérifier avec ProbeJS), ou un fichier d'index EMI dans un pack de ressources.

**Mode Compat** : rester en **COMPAT** (défaut). Ne **jamais** passer en UNITE sans Farmer's Delight (voir 1.3).

### 2.8 Cuisine en voyage et dans les clairières

**Le bivouac type** (tout est vérifié dans les jars) :
- Feu de camp vanilla : chaleur Cold Sweat, purification de l'eau TWR (recettes `campfire_cooking`), cuisson vanilla.
- Le **wok KC** posé dessus : il voit le feu (`lit`). Il faut de l'huile et la spatule.
- La **théière KC** sur le feu donne des thés qui désaltèrent (Compat).
- Le **sac de couchage** de Traveler's Backpack pour dormir.

**Dans Traveler's Backpack** :
- Désactiver `enableFurnaceUpgrade` et `enableSmokerUpgrade` : un four et un fumoir dans le sac court-circuitent le feu de camp et KC.
- Désactiver aussi `enableFeedingUpgrade` : manger reste un geste.
- Garder `enableCraftingUpgrade`, `enableTanksUpgrade` et `enableLanternUpgrade` (LambDynamicLights).
- À valider avec l'équipe.

**Dormir au froid** :
- Ajouter les 16 blocs `travelersbackpack:*_sleeping_bag` à « Sleep Check Override Blocks » de Cold Sweat (`world.toml`).
- Laisser « Check Sleeping Conditions » actif : on ne dort pas en plein hiver sans feu. C'est le bon lien.

**Clairières** :
- Le feu de veille sert de source de chaleur et de cuisson (voir 2.1).
- Le biome `fallow:clairiere` reçoit la saison par le pont (2.2), comme les autres biomes.
- Ses moutons et lapins se nourrissent des cultures F&C (2.6).
- Pour éviter un saut de température en entrant dans une clairière, donner au biome, dans Cold Sweat, la même température que la plaine (`addBiomeTemperature`), ou un léger bonus (« la vie revient »).

### 2.9 KubeJS suffit-il ?

| Liaison | KubeJS ou données suffisent ? |
|---|---|
| Sources de chaleur (KC, F&C, feu de veille), plats chauds et froids, glacière | **Oui** (`ColdSweatEvents.registries`, tags) |
| Saison → Cold Sweat → FIAHI et TWR | **Oui**, avec `addTempModifier` + `gatherDefaultModifiers`. Risque : un modificateur défini dans un script serveur n'existe pas côté client (voir 3.1) |
| Tags de saison F&C, reproduction saisonnière, nourriture des animaux | **Oui** (tags, datapack) |
| Doublons, recettes portées, butin des plantes sauvages | **Oui** (recettes KubeJS, LootJS, schémas KC) |
| Masquage dans EMI | Probablement (KubeJS) ; sinon pack de ressources |
| **FIAHI dans Traveler's Backpack** | **Non.** Le sac n'est ni un `Container` ni dans l'`Inventory`. Il faut un mixin ou un tick Java qui applique `IFrozenRottenFood.tick` au `BackpackWrapper`, avec un coefficient d'isolation |
| **Boire l'eau des réservoirs avec TWR** | **Non.** Il faut une `EffectFluid` Java pour l'eau de Traveler's Backpack qui appelle `ThirstHelper` et tient compte de la pureté |

**Proposition : un petit mod custom `fallow-liens`** (NeoForge 21.1, Java) qui ferait trois choses :
1. Faire vieillir la nourriture du sac (porté et posé) selon la température du porteur ou du lieu, à vitesse réduite : le sac isole un peu, le sac à provisions FIAHI reste meilleur.
2. Faire désaltérer le tuyau du sac selon TWR, avec la pureté de l'eau du réservoir.
3. En option, porter en Java le modificateur saisonnier Cold Sweat, si la version KubeJS pose un problème côté client ou de performance.

Sans ce mod, le sac devient un **réfrigérateur parfait**, et ses réservoirs ne servent qu'à transporter l'eau. On peut l'accepter, mais il faut le décider.

---

## 3. Conflits et risques

1. **Pont saisonnier côté client** :
   - `ColdSweatEvents.registries` et `gatherDefaultModifiers` sont des événements **serveur** [jar `EventGroup.server`].
   - Si Cold Sweat calcule ou désérialise les modificateurs côté client (affichage du thermomètre), un identifiant `fallow:saison` inconnu du client peut provoquer des erreurs ou un affichage faux.
   - À tester en multijoueur ; repli : mod custom (2.9).
2. **KC-Automation et Kaleidoscope Compat déclarent tous deux un point de bras Create** pour le wok, la marmite, le cuiseur vapeur et la broche [jar, aucun ne connaît l'autre].
   - Proposition : régler dans Compat `create.arm.pot_enabled`, `stockpot_enabled`, `steamer_enabled` et `shawarma_spit_enabled` sur `false`.
   - Garder dans Compat le moulin et la théière, que KC-Automation ne gère pas.
   - Garder KC-Automation (colis Create, table, corbeille).
3. **Mode UNITE de Compat** : il casse le bacon, le poulet, les pâtes et le sac d'oignons de F&C, et supprime les oignons sauvages (1.3). Rester en COMPAT.
4. **Condition `forge:false`** du datapack de Compat : sous NeoForge 21.1, elle est soit acceptée, soit en erreur. Dans les deux cas, la fabrication de la marmite F&C disparaît, puisque le fichier remplace l'original. À vérifier une fois en jeu (journal).
5. **Gel de l'eau** : Cold Sweat (« Custom Freezing Behavior » en 2.4.3.1, refonte en 2.4.4) et Ecliptic (`IceAndSnow`, `WaterFreezesInFrozenBiomes`) gèrent tous deux le gel de l'eau. Choisir **Ecliptic** pour la glace saisonnière, et couper le comportement de Cold Sweat.
6. **Fourneau KC sans combustible** : chaleur gratuite et infinie. D'où l'effet faible proposé en 2.1. Sinon, il remplace le foyer de Cold Sweat.
7. **Versions** :
   - KC 1.6.0 change le moulin (que Compat modifie par mixin, `MillstoneBlockEntityMixin`) et la planche à découper ;
   - F&C 1.1.27 et plus ajoute des doublons ;
   - Ecliptic est en bêta.
   - Monter **KC en 1.6.0 et Ecliptic en 0.15.4.1** sur une branche, mais **garder F&C en 1.1.25** tant que la planche et les hachoirs ne sont pas traités.
8. **FIAHI et performance** : FIAHI parcourt chaque tick de monde les block entities de tous les chunks chargés (`onLevelTick`).
   - Régler `TEMPERATURE_CHECKER_INTERVAL` assez haut.
   - Mesurer avec spark sur une base pleine de coffres.
   - Avec `simulation-distance=6`, la charge reste bornée [déduit].
9. **FIAHI et Create** : la nourriture sur les tapis et dans les dépôts (non `Container`) n'est pas suivie. Sans gravité.
10. **Pluie locale d'Ecliptic** : elle éteint les fourneaux KC à ciel ouvert et arrête le séchage au plateau. C'est voulu, mais à expliquer au joueur (Ponder de KC, codex).
11. **Contenu KC hors du ton** :
    - maisons de village KC ;
    - plats « Nether » et « End » (`blaze_lamb_chop`, `end_style_sashimi`…) ;
    - mobilier en double avec Handcrafted.
    - À masquer ou à garder rares : décision de ton, pas technique.

---

## 4. À adapter, supprimer ou ajouter dans le plan

| Où | Changement | Raison |
|---|---|---|
| 04 §4.7 et brouillon `cuisine_unifiee.js` | **Ne pas** retirer le fourneau F&C. Retirer la rôtissoire en portant ses recettes vers le wok KC. Retirer l'épouvantail **KC**. Remplacer les tags `sereneseasons:*` d'objet par des tags `eclipticseasons:crops/*` sur les **blocs** | KC n'a pas de four. Les tags F&C sont cassés (mauvais dossier). Ecliptic juge les blocs |
| 04 §4.7, ligne Kaleidoscope Compat | `datapack_mode = COMPAT` (explicite), `cooking_pot_recipes_disabled = true`, `farm_and_charm_compat_enabled = true`, `thirst_compat_enabled = true`, `create.arm.{pot,stockpot,steamer,shawarma_spit}_enabled = false` | Ce que fait réellement chaque réglage (1.3) ; conflit avec KC-Automation |
| 04 §4.3 et 10.5 « Kaleidoscope Compat fusionne les doublons » | Le corriger. Il rend interchangeables les objets en double ; il ne les supprime que dans le mode UNITE, inutilisable ici. La suppression des doublons reste à faire en KubeJS et LootJS (2.7) | Exactitude |
| 04 §4.6 et brouillon `saisons_cold_sweat.js` | Réécrire avec `addTempModifier` + `gatherDefaultModifiers` (2.2) | Mauvaise API dans le brouillon ; le bon modèle donne aussi la saison à FIAHI et à TWR |
| 10.7, ligne « La chaleur » | Préciser : fourneaux KC et F&C par KubeJS Cold Sweat (effet faible), feu de veille par tag, plats chauds par `addFoodTemperature` | Ils ne comptent pas par défaut |
| 10.7, ligne « Les saisons » | « tags Freeze It and Heat It » n'existe pas. FIAHI se règle par sa configuration (listes d'identifiants) et reçoit la saison par le pont Cold Sweat | FIAHI n'a pas de tags de saison |
| 10.7, ligne « Manger et boire » | Soupes et thés KC : rien à faire (Compat). Thés et soupes F&C : mots-clés TWR | Déjà automatique |
| 10.5, ligne Traveler's Backpack | Ajouter les réglages : améliorations four, fumoir et alimentation coupées ; sacs de couchage dans la liste de sommeil Cold Sweat | Cuisine en voyage par le feu de camp |
| 10.10, scripts | Ajouter `chaleur_cold_sweat.js` (sources de chaleur, plats, glacière), `faune_nourriture.js` (tags d'élevage), `saisons_cultures.js` (tags Ecliptic F&C et reproduction saisonnière) ; `garde_manger.js` reste retiré | Les liens de 2.1 à 2.6 |
| Nouveau | **Mod custom `fallow-liens`** (2.9), si l'on refuse que le sac soit un réfrigérateur et que ses réservoirs soient inutiles pour boire | Impossible en KubeJS |
| Versions | KC → 1.6.0, Ecliptic → 0.15.4.1 sur une branche de test. F&C reste en 1.1.25. Cold Sweat 2.4.3.1 tant que 2.4.4 n'est pas testé | 1.3, 3.7 |
| Zoo (10.4) | Tags d'élevage et de reproduction saisonnière (2.6) ; moulin KC tiré par les bêtes | Relie le zoo à la ferme |

---

## 5. À trancher par un test en jeu

1. Le modificateur `fallow:saison` défini en KubeJS s'affiche-t-il correctement côté client (thermomètre Cold Sweat), en solo puis sur serveur dédié ?
2. Ce modificateur atteint-il bien la température d'un coffre : un aliment pourrit-il plus vite en été qu'en hiver dans un coffre FIAHI ?
3. Avec les deux mods actifs, quel bras Create l'emporte sur le wok : KC-Automation ou Compat ? Et après avoir coupé les bras de Compat ?
4. Le journal montre-t-il que `forge:false` est accepté ? La marmite F&C a-t-elle disparu de la table de fabrication ?
5. Les recettes F&C rejouées dans la marmite KC s'affichent-elles dans EMI ?
6. Kaleidoscope Compat 2.9.7-Patch.2 tourne-t-il avec KC 1.6.0 (mixin du moulin) ?
7. Avec les tags `eclipticseasons:crops/*` posés sur les blocs F&C, l'infobulle d'Ecliptic et la croissance réelle sont-elles saisonnières ?
8. Riz KC dans la clairière : quel climat et quelle humidité Ecliptic attribue-t-il au biome `fallow:clairiere` ?
9. La nourriture vieillit-elle dans Traveler's Backpack (porté et posé) ? Je m'attends à ce que non.
10. L'outre de Cold Sweat désaltère-t-elle avec TWR ? Le tuyau du sac ne désaltère pas, à confirmer.
11. L'effet « Préservation » de KC annule-t-il le malus `fiahi:shiver` ?
12. Le chapeau de paille fait-il tomber des graines KC de l'herbe de No Man's Land (table `short_grass` vanilla, ou herbe propre à NML) ?
13. FIAHI dans une base de 200 coffres : coût en temps de tick (spark), selon `TEMPERATURE_CHECKER_INTERVAL`.
14. Gel de l'eau : quel mod gagne entre Cold Sweat et Ecliptic, et y a-t-il des allers-retours glace / eau ?
