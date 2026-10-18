# 1. Overgeared : étude du jar et liens avec le pack

> Vérification du 2026-10-18. Jar étudié : `overgeared-neoforge-1.21.1-1.6.19.jar`, celui de `pack/mods/overgeared.pw.toml`. Son sha512 commence par `06b7d48d89a3134…`, comme dans le toml.
> Sources : l'API Modrinth (projet `overgeared`, id `SQL3X2Ky`), les classes lues avec `javap`, les JSON du jar et le [document officiel pour les créateurs de modpacks](https://docs.google.com/document/d/1dUjVXHALGE7b625DYa-cVr0Y6vJkh3NFya0gcDSE_C8) (lien donné sur la page Modrinth). J'ai aussi ouvert, pour comparer, les jars d'Easy Anvils, Visual Workbench, FallingTree, Traveler's Backpack et Subtle Effects, et ceux de 22 mods du pack (Create, Cold Sweat, Supplementaries, Quark, la faune…).
> « déduit » = conclusion tirée du code ou des données, sans test en jeu.

---

## 0. L'essentiel

1. **Il n'y a pas de bloc « forge ».** On chauffe le métal dans le **haut fourneau vanilla**, ou dans le fourneau pour le cuivre. Ensuite on le **martèle sur l'enclume de forgeron** avec un mini-jeu, on le **trempe** dans l'eau et on le **polit** à la meule. La forge du plan est donc un **atelier à monter** : haut fourneau, enclume, eau, meule, plus le soufflet de Supplementaries.
2. **Overgeared remplace déjà les recettes vanilla.** On ne fabrique plus ses outils à l'établi : on assemble une tête forgée et un bâton. Le diamant, le seau, le chaudron, les cisailles, le briquet, les flèches et la netherite passent par lui. La suppression prévue dans `progression.js` ne sert à rien : elle vise `crafting_shaped`, qui n'existe plus pour ces objets.
3. **Pas de bronze, pas d'étain, pas de laiton.** Le seul alliage du four à alliage est l'acier brut (3 fers + 1 charbon). Les chapitres 03 et 08 se trompent sur ce point.
4. **Le « modèle trouvé dans les ruines » n'est pas un modèle d'acier.** C'est le **gabarit d'amélioration en diamant** (acier → diamant à la table de forgeron). L'acier se fabrique, lui, sans rien trouver.
5. **Tout se règle en données.** Les recettes, les tags, les attributs par qualité et les types de plans se règlent en JSON, donc en datapack ou avec `event.custom()` de KubeJS. Le mod n'a **ni plugin KubeJS, ni JEI, ni pont avec Create**. Il fournit un **plugin EMI**, un plugin Polymorph et une compatibilité **Sable/Aeronautics**.
6. **Les plans d'Overgeared remplacent en grande partie les plans KubeJS du §10.10.** Un plan d'Overgeared d'un type caché peut être la condition pour forger une pièce : la Lanterne de clairière, un essieu, une pale d'hélice. Il se trouve en exploration, se copie et s'améliore avec l'usage.
7. **KubeJS et des datapacks suffisent** pour tous les liens. Un petit mod n'est utile que pour un détail de finition : des étincelles à chaque coup de marteau au lieu des étoiles « coup critique » (§2.13).
8. **Il n'y a pas de traduction française** (`fr_fr.json` absent). Il faut la livrer dans le pack.

---

## 1. Ce qui est confirmé

### 1.1 Version et dépendances

| Point | Valeur | Preuve |
|---|---|---|
| Dernière version NeoForge 1.21.1 | `neoforge-1.21.1-1.6.19`, release, 2026-09-21 : **c'est celle du pack** | API Modrinth `/project/overgeared/version?loaders=["neoforge"]&game_versions=["1.21.1"]` |
| Dépendances obligatoires | NeoForge ≥ 21.1.227 (le pack a la 21.1.251), Minecraft 1.21.1 | `META-INF/neoforge.mods.toml` |
| Dépendances facultatives | `quark` ≥ 4, `dragonsurvival` | idem |
| Embarqué | `sable-companion-common-1.21.1-1.6.0.jar` (jar-in-jar) | `META-INF/jarjar/` |
| Licence | MIT ; source sur GitHub `phuccom000/Overgeared-1.20.1` | API Modrinth |
| Config | une seule config **COMMON** (classe `ServerConfig`, donc fichier `config/overgeared-common.toml`, nom à confirmer au premier lancement) et une config client | `OvergearedMod` : `registerConfig(COMMON, ServerConfig.SERVER_CONFIG)` |
| Langues | de_de, en_us, es_mx, ja_jp, pl_pl, pt_br, ru_ru, vi_vn, zh_cn. **Pas de fr_fr** | `assets/overgeared/lang/` |
| Mixins | serveur et commun seulement (refroidissement dans les conteneurs, les entités et l'inventaire ; empilement ; joueur ; potions) | `overgeared.mixins.json` |
| Changements utiles | 1.6.13 : état « cassé » et compatibilité Sable/Aeronautics corrigée ; 1.6.14 : refroidissement dans l'eau sur une structure Sable ; 1.6.17 : marteaux par palier d'enclume ; 1.6.18.1 : compatibilité Clockwork ; 1.6.19 : le métal chaud brûle comme un bloc de magma | changelogs Modrinth |

### 1.2 La chaîne du forgeron (lue dans les recettes du jar)

| Étape | Ce qui se passe | Données |
|---|---|---|
| **Silex et pierre** | Clic droit avec un silex sur de la pierre : on obtient un caillou (`overgeared:knappable_rock`). La pierre devient de la pierre taillée et le silex peut casser | `rock_interactions/flint_on_stone.json` (30 % de chance d'obtenir le caillou, 15 % de casser le silex) |
| **Taille** | On taille les têtes d'outils en pierre dans une grille 3 × 3 (`overgeared:rock_knapping`), puis tête + bâton = outil en pierre vanilla | `recipe/stone_*_head.json`, `minecraft/recipe/stone_pickaxe.json` |
| **Enclume de pierre** | Clic droit accroupi sur `minecraft:stone` avec un marteau → enclume de pierre, de palier « stone », qui monte au plus à « Well Forged » et s'use (`max_uses`) | tag `overgeared:stone_anvil_bases` = `minecraft:stone` |
| **Marteau** | Le marteau de cuivre se fabrique à l'établi (cuivre + bâtons) ou avec une tête coulée. Le marteau d'acier vient d'une tête forgée | `copper_smithing_hammer_2.json`, `smithing_hammer.json` |
| **Enclume de forgeron** | Clic droit accroupi sur une **enclume vanilla** `minecraft:anvil` (31 fers) avec un marteau → `overgeared:smithing_anvil`, de palier « iron ». Elle **tombe** comme l'enclume vanilla (classe `Fallable`) et **affiche les objets posés en 3D** (`SmithingAnvilBlockEntityRenderer`) | tag `overgeared:iron_anvil_bases` = `minecraft:anvil` seul (ni ébréchée ni endommagée) |
| **Chauffe** | Fer : **haut fourneau seulement** (minerai, fer brut ou lingot → `heated_iron_ingot`). Le haut fourneau vanilla ne donne plus de lingot de fer froid. Cuivre et argent : fourneau ou haut fourneau. Acier brut : haut fourneau | `heated_*_from_blasting_*.json`, `minecraft/recipe/iron_ingot_from_blasting_*.json` (condition `neoforge:false`) |
| **Métal chaud** | Le métal chaud brûle si on le tient sans pinces (pinces en bois avec des bâtons, en fer ou en acier). Il refroidit en 1200 ticks (60 s) dans l'inventaire ou d'un coup dans l'eau. Il fume et brûle aussi les créatures | config `heatedItemCooldownTicks = 1200`, tags `overgeared:heated_metals` et `overgeared:tongs`, recettes `overgeared:cooling` |
| **Martelage** | On place les ingrédients dans la grille 3 × 3 de l'enclume, avec le plan dans sa case s'il y en a un. Clic droit au marteau pour lancer le **mini-jeu** : une flèche, une zone parfaite qui rétrécit, un nombre de coups (`hammering`). Le score donne la qualité | `ForgingRecipe$Serializer`, config `perfectQualityScore = 0.9`, `expert = 0.6`, `well = 0.3`, `masterQualityChance = 0.05` |
| **Trempe et polissage** | Une tête chaude doit être trempée (eau) puis polie à la meule (`overgeared:grindstones` = meule vanilla). Sinon elle reste « non polie » et sa qualité maximale baisse | `need_quenching`, `has_polishing`, tags `overgeared:grindable` et `overgeared:grinded` |
| **Assemblage** | Tête + bâton à l'établi, recette `overgeared:crafting_shapeless` qui **reporte la qualité** de la tête sur l'outil | `minecraft/recipe/iron_pickaxe.json` |
| **Acier** | Four à alliage en briques (`overgeared:alloy_furnace` = haut fourneau entouré de briques) : 3 lingots de fer + 1 charbon → 4 aciers bruts. On les chauffe au haut fourneau, on les martèle (3 coups) en acier chaud, puis on les refroidit en lingots d'acier | `crude_steel_from_alloying.json`, `heated_steel_ingot.json`, `steel_ingot_from_cooling.json` |
| **Armures** | Plaques forgées (`overgeared:iron_plate`, `copper_plate`, `steel_plate`, 3 coups, sans qualité), puis armure forgée avec qualité (5 coups) | `iron_chestplate.json`, `iron_plate.json` |
| **Diamant** | Plus aucune recette d'outil en diamant à l'établi : elles donnent de l'air. Il faut l'objet en acier, un diamant et le **gabarit d'amélioration en diamant** à la table de forgeron. Le gabarit se trouve dans des coffres et se copie avec 7 diamants et 1 acier (on en obtient 2) | `steel_*_to_diamond_*.json`, `diamond_upgrade_smithing_template.json`, `loot_modifiers/diamond_upgrade_from_*.json` |
| **Netherite** | Alliage au four à alliage du Nether (briques du Nether : 4 débris + 4 or), puis martelage de niveau maître (en cas d'échec : 4 débris) | `heated_netherite_alloy.json`, `minecraft/recipe/netherite_ingot.json` |
| **Moules** | Moule d'argile cuit au fourneau. On le remplit de pépites ou de lingots (barème `material_settings`) et on le cuit au fourneau ou au haut fourneau (`cast_smelting`, `cast_blasting`). La tête qui sort perd en qualité. Le moule du Nether et le **fourneau de coulée** (briques du Nether rouges + 1 diamant) sont pour la fin de partie | `casting_furnace.json`, `*_from_cast_*.json`, config `castingToggle = true` |
| **Plans** | Papier ×3 + teinture bleue → plan vierge ; plan vierge + établi → **table à dessin**. Sur la table, le plan vierge devient un plan d'un type d'outil, qui fixe la **qualité maximale** du résultat. Le plan monte de qualité à l'usage (`poorMaxUse`, `wellMaxUse`, `expertMaxUse`). Plan vierge + plan → copie de moindre qualité | `empty_blueprint.json`, `drafting_table.json`, `blueprint_cloning/*.json`, composant `overgeared:blueprint_data` {quality, tool_type, uses} |
| **Flèches** | La table d'empennage reçoit une interface (`overgeared:fletching`) : flèches en fer, en acier, en diamant, trempées ou persistantes | `minecraft/recipe/arrow.json` |
| **Objets cassés** | À 0 de durabilité, un objet peut rester « cassé » au lieu de disparaître, selon sa qualité (de 50 % à 75 % pour well et expert ; autres valeurs dans la config) | config `enableQualityBreakSystem = true`, `BrokenHelper` |
| **Réparation** | La meule répare : +10 % de durabilité par passage, −5 % de durabilité maximale | config `grindingToggle`, `damageRestore = 0.1`, `durabilityReduce = 0.05` |

Les objets vanilla passés par la forge (`data/minecraft/recipe/`) :
- outils et armures en fer, en or et en pierre ;
- les recettes de diamant, désactivées ;
- seau (3 fers, enclume de pierre suffit), chaudron (**plaques d'acier**), cisailles ;
- **briquet** (silex + lingot d'acier) ;
- flèches, flèches spectrales et flèches trempées ;
- lingot de netherite.

### 1.3 Paliers des outils (tags du jar)

| Palier | Peut miner | Preuve |
|---|---|---|
| Bois, pierre, or | **pas le minerai de fer** (`overgeared:needs_copper_tool` = minerais de fer et blocs de fer) | `minecraft/tags/block/incorrect_for_stone_tool.json` |
| Cuivre (nouveau) | le fer, mais pas `minecraft:needs_iron_tool` : diamant, or, redstone et **zinc de Create** | `incorrect_for_copper_tool.json` ; Create : `needs_iron_tool` = minerai de zinc |
| Fer | tout, sauf `overgeared:needs_steel_tool` (obsidienne, obsidienne pleureuse) | `incorrect_for_iron_tool.json` |
| Acier | tout, sauf `minecraft:needs_diamond_tool` | `incorrect_for_steel_tool.json` |

Déduit : **le zinc, donc le laiton de Create, demande une pioche en fer forgée.** C'est un verrou naturel « forge avant Create », sans script.

### 1.4 Qualité : données et effets

- Le composant `overgeared:forging_quality` est une énumération `StringRepresentable` : `poor`, `well`, `expert`, `perfect`, `master`. Source : `ModComponents`, `ForgingQuality`.
- Les autres composants : `heated`, `polished`, `blueprint_data`, `cast_data`, `creator` (le nom du forgeron dans la description), `failed_result`, `blueprint_required`, `heated_time`, `tipped_uses`, `reduced_grind_count`, `lingering_status`.
- **Les attributs par qualité se règlent en données** (`data/<ns>/quality_attributes/*.json`).
  - Cibles possibles : `weapon` (toute classe `TieredItem` ou `ProjectileWeaponItem`), `armor` (`ArmorItem`), `item`, `item_tag`, `item_all`.
  - Opérations : `add`, `mult_base`, `mult_total`.
  - Source : `QualityTarget$TargetType`, `QualityAttributeHandler` (`ItemAttributeModifierEvent`).
  - Valeurs par défaut :
    - dégâts : de −1 (poor) à +3 (master) ;
    - armure : de −1 à +2 ;
    - durabilité : ×0,7 à ×1,6 ;
    - vitesse de minage : ×0,7 à ×1,5.
- **La qualité marche sur les objets de n'importe quel mod**, dès que le composant est présent.
- **Butin** : le modificateur global `overgeared:all_tools_give_quality`, sans condition, donne une qualité tirée au hasard à **tout objet qui s'use et sort d'une table de butin**. Cela vaut pour les coffres, Lootr et les créatures, et donc aussi pour les objets de boss de Mowzie's (tables `mowziesmobs:entities/ferrous_wroughtnaut`, `umvuthi`, `frostmaw`) et le butin des ruines de NML.
  - Poids par défaut : poor 50, well 30, et ainsi de suite.
  - Exclusions : tag `overgeared:quality_blacklist`.
  - Source : `QualityLootModifier`, `global_loot_modifiers.json`.
- **Butin en plus** :
  - `steel_ingot_from_*` et `diamond_upgrade_from_*` dans les coffres vanilla : mineshaft, donjon, stronghold, pyramide du désert, épave, manoir, temple de la jungle, cité antique, avant-poste, trésor enfoui ;
  - flèches en fer dans le distributeur du temple de la jungle.
- **Échanges** :
  - armurier, forgeron d'outils et forgeron d'armes : échanges vanilla enveloppés avec une qualité, plus des échanges d'objets forgés et d'acier contre des émeraudes ;
  - marchand ambulant : vend des plans.
  - Source : `ModEvents.onVillagerTrades`, `onWanderingTraderTrades`.

### 1.5 Ce qui se règle en données (sans code)

| Dossier ou tag | Rôle |
|---|---|
| `data/<ns>/recipe/` | 21 sérialiseurs : `forging`, `rock_knapping`, `alloy_smelting`, `shaped_alloy_smelting`, `nether_alloy_smelting`, `shaped_nether_alloy_smelting`, `casting`, `cast_smelting`, `cast_blasting`, `nbt_smelting`, `nbt_blasting`, `cooling`, `grinding`, `fletching`, `item_to_tooltype`, `crafting_shapeless`, `crafting_cloning`, `crafting_cast`, `crafting_initial_cast`, `campfire_heating` (`ModRecipeSerializers`) |
| `data/overgeared/blueprint_tooltypes/` | types de plans en plus (`{"tooltypes":[…]}`) |
| `casting_tooltypes/`, `material_settings/` | quantités des moules et valeur des matériaux |
| `quality_attributes/` | effets de la qualité |
| `rock_interactions/` | clic droit d'un objet sur un bloc → un objet tombe (le caillou) |
| `knapping_resources/` | ce qu'on peut tailler, avec sa texture et son son |
| `durability_blacklist/`, `grinding_blacklist/`, `broken_blacklist/` | exclusions |
| tags d'objets | `heated_metals`, `hot_items`, `tongs`, `smithing_hammers` (et `stone_`, `iron_`, `tier_a_`, `tier_b_smithing_hammers`), `tool_parts`, `quality_blacklist`, `knappables`, `grindable`, `grinded` |
| tags de blocs | `smithing_anvil`, `stone_anvil_bases`, `iron_anvil_bases`, `tier_a_anvil_bases`, `tier_b_anvil_bases`, `grindstones`, `needs_copper_tool`, `needs_steel_tool` |

**Champs d'une recette `overgeared:forging`** (sérialiseur et doc officielle) :
- `pattern`, `key` : chaque clé est un ingrédient, avec `requires_heated` et `transfer_nbt` en option ;
- `result` et `result_failed` (en 1.21.1, `{"id": …}` et non `{"item": …}`) ;
- `hammering`, `tier` (`stone`, `iron`, `above_a`, `above_b` ; `iron` par défaut) ;
- `blueprint` (liste de types), `requires_blueprint` ;
- `has_quality`, `minimum_quality` (clé lue dans le jar : `minimumQuality`), `quality_difficulty` ;
- `needs_minigame`, `has_polishing`, `need_quenching` ;
- `category` (`TOOL_HEADS`, `ARMORS`, `misc`…), `show_notification`.

**Types de plans** : config `availableToolTypes` (épée, hache, pioche, pelle, houe) et `hiddenToolTypes`. Un type caché existe mais **n'apparaît pas sur la table à dessin**, d'après le commentaire de la config. Il faut aussi la clé de langue `tooltype.overgeared.<type>`.

Les autres réglages utiles, avec leur valeur par défaut :
- `enableMinigame = true` ;
- `enableBlueprintForging = true` ;
- `ingredientsDefineQuality` (la qualité d'un ingrédient limite celle du résultat) ;
- `maxAnvilDistance` ;
- `enableTieredHammerForging = false`, `enableTierA = false`, `enableTierB = false` ;
- `coolingBlockEntityBlacklist` (fourneau et haut fourneau vanilla) ;
- `enableLootQuality = true` et ses poids ;
- `failOnWellQualityChance = 0.1`, `failOnExpertQualityChance = 0.05`.

### 1.6 Intégrations présentes dans le jar

| Intégration | Présente ? | Preuve |
|---|---|---|
| **EMI** | **Oui** : forge, taille, alliages, coulée, refroidissement, meule, empennage, table à dessin, copie de plans, moules | `compat/emi/` (30 classes, dont `OvergearedEmiPlugin`). Depuis 1.6.16, les paliers A et B sont cachés dans EMI par défaut |
| JEI | **Non** en 1.21.1. Les clés `jei.*` du fichier de langue sont des restes. Le chapitre 06 (« plugin natif JEI dans Overgeared ») est faux, mais EMI est retenu de toute façon | aucune classe JEI ; changelog beta-v2 : « I switched over to EMI instead of JEI » |
| KubeJS | Non (aucun plugin). Les recettes s'écrivent avec `event.custom({...})`, ou en JSON dans `kubejs/data/` | aucune classe `kubejs` |
| Create | Non. Seuls des tags `c:` sont partagés (§2.1) | — |
| Quark | Remplace les 5 recettes de Quark « Better Stone Tools » par tête + bâton | `data/quark/recipe/tweaks/crafting/utility/better_stone_tools/` |
| Sable / Aeronautics | Oui (refroidissement dans l'eau sur une structure Sable) | `compat/sable/SableCompat`, jarjar `sable-companion` |
| Polymorph, Dragon Survival | Oui (mods hors du pack) | `compat/polymorph/` |
| Capacité d'objets (`Capabilities.ItemHandler.BLOCK`) | Exposée sur le four à alliage, le four à alliage du Nether et **les 4 enclumes** : entonnoirs, entonnoirs de Create et bras mécaniques peuvent les remplir et les vider | `ModCapabilities` |

---

## 2. Les liens concrets à construire

Les noms de fichiers suivent l'arborescence du chapitre 07.

### 2.1 Create : qui fait quoi

**Principe** : le **martelage reste manuel**, et c'est le mod qui l'impose. Le mini-jeu se joue côté client et la qualité vient du score (changelog beta-v9 : le mini-jeu démarre côté serveur, mais se joue côté client). Aucune machine ne peut produire une qualité. Create peut en revanche prendre en charge tout ce qui est **quantité et chaleur**.

| Lien | Quoi | Outil |
|---|---|---|
| **Chauffe industrielle** | Le **traitement en masse « blasting » de Create** (ventilateur devant de la lave ou un brûleur) lit les recettes `minecraft:smelting` **et** `minecraft:blasting`. Un lingot de fer ou du fer brut devant le ventilateur devient donc un `heated_iron_ingot`, et l'acier brut un acier brut chaud. Rien à écrire. *Preuve : `AllFanProcessingTypes$BlastingType` lit `RecipeType.SMELTING` et `RecipeType.BLASTING`.* | Aucun. Pour l'interdire, recréer ces recettes de chauffe avec un id qui finit par **`_manual_only`** : Create les ignore dans toutes ses machines (`AllRecipeTypes.CAN_BE_AUTOMATED`, chaîne `_manual_only` confirmée dans le jar) |
| **Acier en masse** | Mélangeur chauffé : 3 lingots de fer + 1 charbon → 4 aciers bruts (même rapport que le four à alliage). Avec un brûleur surchauffé, on peut sortir directement de l'acier chaud | `recipes/forge_create.js` : `event.recipes.create.mixing('4x overgeared:crude_steel', ['3x minecraft:iron_ingot','minecraft:coal']).heated()` (KubeJS Create) |
| **Four à alliage automatisé** | Un entonnoir ou un bras mécanique nourrit le four à alliage, qui expose sa capacité d'objets | Aucun. À tester (§5) |
| **Laiton** (plan 03 §3.4 : « d'abord à la fonderie Overgeared ») | Overgeared n'en fait pas. On ajoute une recette `overgeared:alloy_smelting` : 3 cuivres + 1 zinc → 4 `create:brass_ingot`, avec un temps de cuisson long. Le mélangeur reste la voie rapide | `event.custom({type:'overgeared:alloy_smelting', ingredients:[…], result:{id:'create:brass_ingot', count:4}, cookingtime:300})` |
| **Plaques** | `#c:plates/iron` contient `create:iron_sheet` (tag de Create) **et** `overgeared:iron_plate` (tag d'Overgeared). Les 16 recettes de Create qui demandent `#c:plates/iron` acceptent donc déjà les plaques forgées à la main. Dans l'autre sens, les armures d'Overgeared demandent l'objet exact `overgeared:iron_plate` | Option conseillée : réécrire les 4 armures en fer, les 4 en cuivre et les 4 en acier avec `{"tag":"c:plates/iron"}` (puis copper, steel). La **presse fait les plaques** (quantité), l'enclume fait **l'armure** (qualité). `event.replaceInput({type:'overgeared:forging'}, 'overgeared:iron_plate', '#c:plates/iron')`, à vérifier : `replaceInput` doit traverser les clés de ce type personnalisé, sinon réécrire en `event.custom` |
| **Plaques d'acier à la presse** | `create:pressing` : `#c:ingots/steel` → `overgeared:steel_plate`. Create ne le fait que pour Immersive Engineering (`compat/immersiveengineering/plate_steel.json`, condition `mod_loaded`) | `event.recipes.create.pressing('overgeared:steel_plate', '#c:ingots/steel')` |
| **Moteur à vapeur, rails** (`progression.js`) | `#c:plates/steel` et `#c:nuggets/steel` **existent bien** (tags d'Overgeared). Les lignes du brouillon sont justes | — |
| **Pièces critiques avec une qualité minimale** | Une forge `overgeared:forging` avec `has_quality: true` sur les objets KubeJS `fallow:rivet_forge`, `fallow:essieu_forge`, `fallow:pale_helice_forgee`… les marque du composant `overgeared:forging_quality`. Dans la recette Create ou à l'établi, l'ingrédient filtre la qualité avec l'ingrédient NeoForge **`neoforge:components`** (`{"type":"neoforge:components","items":"fallow:pale_helice_forgee","components":{"overgeared:forging_quality":"perfect"},"strict":false}`). Pour « au moins excellent », regrouper plusieurs valeurs dans `neoforge:compound` | Datapack ou `event.custom`. À vérifier : qu'EMI et l'assemblage séquencé de Create affichent et acceptent ces ingrédients (déduit : Create 6 lit le codec vanilla `Ingredient`, qui comprend les ingrédients NeoForge) |
| **Déployeur sur l'enclume** | Une recette sans qualité ni mini-jeu (plaques, seau) avance au clic droit au marteau (`increaseForgingProgress`). Un déployeur muni d'un marteau pourrait donc forger les plaques tout seul | À tester. S'il faut l'interdire : `BlockEvents.rightClicked` en KubeJS, annuler si `event.player.isFake()` et que le bloc est dans `#overgeared:smithing_anvil` |
| **Outils fabriqués par Create** | Aucune recette de Create ne fabrique d'outil en métal (analyse du jar Create 6.0.10 : seulement clé à molette, lunettes, scaphandre en cuivre, épée en carton) | — |

### 2.2 Create Ore Excavation

Le mod n'est pas encore dans le pack : identifiants à lire dans son jar au moment de l'ajout.
- La foreuse peut exiger une tête forgée : `overgeared:steel_pickaxe_head` filtrée par qualité avec `neoforge:components` (§2.1), plutôt qu'un nouvel objet `fallow:tete_foreuse_forgee`. On réutilise ainsi la vraie tête de pioche, et sa qualité.
- Déduit : si les foreuses de COE s'usent, une foreuse issue d'une forge `has_quality: true` gagne le **bonus de durabilité** de sa qualité. Le mixin `IItemStackExtensionMixin` agit sur tout objet qui s'use et porte le composant. À tester.
- Le minerai brut extrait passe ensuite par la chaîne normale : haut fourneau ou ventilateur, puis enclume.

### 2.3 Create Aeronautics et Sable

- La compatibilité est dans le mod (1.6.13 et 1.6.14 : on peut refroidir dans l'eau ou dans un chaudron posé sur une structure Sable).
- Les pales d'hélice « qualité excellente » du chapitre 03 se font comme les pièces critiques (§2.1).
- Risque : l'enclume de forgeron **tombe** comme une enclume. Sur un engin assemblé, voir si elle chute ou blesse (§5).

### 2.4 La Lanterne de clairière (palier 32, §10.3)

On remplace « pièces forgées chez Overgeared » par une vraie recette de forge qui donne directement le bloc KubeJS :
- **Startup** : le bloc `fallow:lanterne_de_clairiere` est déjà dans `clairieres.js`.
- **Config** `overgeared-common.toml` :
  - `availableToolTypes = ["sword","axe","pickaxe","shovel","hoe","lanterne"]` ;
  - `hiddenToolTypes = ["lanterne"]`, pour qu'aucune table à dessin ne la produise.
- **Langue** : `kubejs/assets/overgeared/lang/fr_fr.json` → `"tooltype.overgeared.lanterne": "Lanterne de clairière"`.
- **Recette** (`recipes/forge_fallow.js`, `event.custom`) :

```json
{
  "type": "overgeared:forging",
  "category": "misc",
  "tier": "iron",
  "blueprint": ["lanterne"],
  "requires_blueprint": true,
  "has_quality": false,
  "needs_minigame": true,
  "hammering": 8,
  "pattern": ["PGP", "GLG", "PSP"],
  "key": {
    "P": {"item": "overgeared:steel_plate"},
    "G": {"item": "minecraft:glass_pane"},
    "L": {"item": "minecraft:lantern"},
    "S": {"item": "overgeared:heated_steel_ingot", "requires_heated": true}
  },
  "result": {"id": "fallow:lanterne_de_clairiere"},
  "result_failed": {"id": "overgeared:steel_plate", "count": 2},
  "show_notification": true
}
```

- **Le plan** : `zoo.js` (§10.10, après 25 espèces) donne un `overgeared:blueprint` avec le composant `overgeared:blueprint_data = {quality:"poor", tool_type:"lanterne", uses:0}`. On peut aussi le cacher dans une ruine (§2.5).
- **Ce que le joueur ressent** : une enclume de forgeron (31 fers), de l'acier, un mini-jeu réussi, un plan gagné. C'est un vrai palier.

### 2.5 Les plans trouvés dans les ruines (§10.10)

Proposition : **séparer** deux familles de plans.
- **Ce qui se forge** (pièces critiques, lanterne, têtes spéciales) : **plans d'Overgeared de types cachés**, un type par plan : `atelier`, `rail`, `aerostat`, `helice`, `lanterne`…
  - La recette `overgeared:forging` les exige (`requires_blueprint: true`).
  - Le plan **fixe la qualité maximale**, ce qui récompense l'exploration, et il **monte de qualité à l'usage**.
  - Il **se copie** (plan vierge + plan) en perdant un niveau : on peut le partager entre amis.
  - Les machines Create qui demandent ces pièces sont verrouillées **en cascade**. On n'a plus besoin de `doLimitedCrafting`, de `recipe give/take` ni de couper « Automatic Recipe Unlock » de Quark.
- **Ce qui ne se forge pas** : `plans_ingenieur.js` ne reste utile que si une recette d'établi doit être verrouillée sans pièce forgée.
- **Butin** : LootJS (`loot/butin.js`), par exemple :

```js
LootJS.lootTables(event => {
  event.getLootTable('nomansland:chests/small_dungeon_core')
    ?.firstPool()
    .addEntry(LootEntry.of(Item.of('overgeared:blueprint',
      '[overgeared:blueprint_data={quality:"poor",tool_type:"rail",uses:0}]')).when(c => c.randomChance(0.3)))
})
```

La syntaxe exacte de LootJS 3 est à valider. Repli sans LootJS : un `neoforge:add_table` qui pointe vers une petite table avec `minecraft:set_components`. Les tables de NML existent (lues dans le jar 1.5.12) :
- `nomansland:chests/small_dungeon_core`, `dungeon_garbage`, `dungeon_kitchen`, `redstone_parts`, `water_well` ;
- `alchemist_pot_*`, `ancient_pot_cave`, `sniffer_monument` ;
- `nomansland:archaeology/remains_dungeon`.

**Le gabarit d'amélioration en diamant** doit lui aussi entrer dans ces tables. Overgeared ne l'ajoute qu'aux structures vanilla, dont plusieurs sont retirées par le plan : strongholds (décision 0004) et donjons (`remove_dungeons.json`).

### 2.6 Outils et armures des autres mods : par la forge ou pas ?

Je n'ai regardé que les recettes qui donnent un outil, une arme ou une armure (script de scan, jars du pack).

| Mod | Objet | Ce qui se passe | Proposition |
|---|---|---|---|
| Kaleidoscope Cookery | couteaux de cuisine en fer, en or et en diamant, `kitchen_shovel`, bottes et jambières de fermier (fer) | à l'établi, avec des lingots | **À la forge** : retirer la recette d'établi et ajouter une `overgeared:forging` (fer chaud + bâton, `has_quality: true`). Les couteaux sont sans doute des `TieredItem` : ils prendraient alors la qualité et ses dégâts (déduit). Pour le diamant, garder l'établi avec un couteau en fer forgé au centre, ou une table de forgeron. C'est le lien le plus visible entre la cuisine et la forge |
| Mowzie's Mobs | `naga_fang_dagger` (fer + croc de naga) ; boss : hache et casque du Wroughtnaut, Sol Visage… | établi ; butin des boss par tables d'entités | Dague : laisser à l'établi (le croc est le trophée) ou la forger, au choix. Boss : ils reçoivent **déjà** une qualité au hasard par le modificateur global. Les ajouter à `overgeared:quality_blacklist` si on ne veut pas d'un « Poorly Forged » sur un trophée de boss |
| Alex's Mobs | coiffe d'élan (fer), plastron de crocodile, bottes de géocoucou, bouclier des profondeurs… | établi, à partir de morceaux de créatures | Laisser : ce sont des trophées, pas du travail du métal |
| Friends & Foes | couronne du Wildfire ; récompense du Rascal (outils vanilla) | établi ; table de butin | La récompense prend une qualité toute seule. Rien à faire |
| Cold Sweat | armures en fourrure de chèvre, en peau de hoglin, en caméléon | établi, sans métal | Laisser |
| Hybrid Aquatic | outils en corail et en coquillage, **scaphandre en cuivre** | établi | Outils : laisser. Scaphandre : à la forge si on veut être cohérent avec le cuivre (optionnel) |
| Create | casque et bottes de plongée en cuivre | établi | Laisser (équipement d'atelier) ou forger, au choix |
| Critters & Companions | armure de libellule en fer, en or et en diamant | établi | Laisser (armure d'animal) |
| Adorable Hamster Pets | armures de hamster par gabarits | établi et table de forgeron | Laisser |
| Companions | équipement « crystallized blood » (à partir de netherite), chapeau de mage (à partir d'un casque en diamant), `netherite_dagger` | établi | Avec le Nether fermé, ce qui demande de la netherite est **inaccessible**. Le cacher dans EMI ou le réassigner. Le chapeau de mage passe déjà par un casque en diamant, donc par la forge |
| Quark | marteau expérimental | catégorie expérimentale coupée | — |

**Règle proposée** : passent par la forge les armes, les outils et les armures **en métal destinés au joueur**. Les trophées de créatures, les armures d'animaux et les vêtements restent à l'établi. Leur qualité, quand ils en ont une, vient du butin.

### 2.7 FallingTree

- Le jar de FallingTree 1.21.1.11 détecte les haches par `instanceof AxeItem` (`ItemWrapper.isAxe`).
- Les haches d'Overgeared (`steel_axe`, `copper_axe`) sont des **`AxeItem` vanilla** (`ModItems` : 2 × `new AxeItem`), et les haches vanilla en fer ou en or viennent de têtes forgées. **Toutes comptent**, sans tag.
- Lien automatique : chaque bûche coûte de la durabilité, et la qualité multiplie la durabilité. Une hache « Masterwork » abat plus d'arbres (déduit).
- Point à tester : une hache à l'état « cassé » d'Overgeared.

### 2.8 Cold Sweat : la forge réchauffe

- **Haut fourneau, fourneau, fumoir allumés** : Cold Sweat les gère déjà (`FurnaceBlockTemp` vise `AbstractFurnaceBlock` avec `LIT`). Rien à faire.
- **Four à alliage et fourneau de coulée** : ils ne dérivent pas de `AbstractFurnaceBlock` (classe `BaseEntityBlock`), mais leur bloc a l'état `lit` (blockstate). Deux façons de les ajouter :
  - en datapack, dans `data/fallow/cold_sweat/block/block_temp/forge.json` (dossier de registre `block/block_temp` confirmé dans `ModRegistries`). Champs : `block` (avec l'état `lit: true`), `temperature`, `range`, `max_effect`, `fade`, `units`. Le format exact du sous-objet `block` est à recopier du wiki de Cold Sweat ;
  - en KubeJS : `ColdSweatEvents.registries(e => e.addBlockTemperature(temp, 'F', b => b.blocks('overgeared:alloy_furnace','overgeared:casting_furnace').state('lit', true).range(5).maxEffect(…)))`. La signature `addBlockTemperature(double, String, Consumer)` et le constructeur sont lus dans `ModRegistriesEventJS` et `BlockTempBuilderJS`.
- **Lingot chaud tenu en main** : registre `item/item_temp`. Le tag `#overgeared:heated_metals` peut réchauffer un peu le joueur qui le porte. C'est un joli détail : forger au froid, l'hiver, devient un abri.
- **Armures en acier et en cuivre** : Cold Sweat ne les connaît pas. Les ajouter dans `item/insulator` comme l'armure en fer (à régler).

### 2.9 Supplementaries et Amendments

- **Le soufflet** (activé dans `supplementaries-common.toml`) accélère les blocs du tag `supplementaries:bellows_tickable`, qui contient déjà le haut fourneau.
  - Ajouter `overgeared:alloy_furnace` et `overgeared:casting_furnace` à ce tag en datapack. Le soufflet devient la pièce visible de la forge (à tester : `tickFurnaces` doit appeler le ticker d'un bloc d'un autre mod).
  - Source : `data/supplementaries/tags/block/bellows_tickable.json`, `BellowsBlockTile.tickFurnaces`.
- **Le robinet** remplit le chaudron de trempe. Mais le chaudron exige des **plaques d'acier** forgées. Avant cela, on trempe dans un bloc d'eau.
- **Piédestal et étagère** : on y expose les pièces de maître. C'est la vitrine du forgeron.
- **Amendments** dessine les lanternes vanilla en 3D. Déduit : un bloc KubeJS de type simple ne recevra pas ce rendu. Si la Lanterne de clairière doit se balancer comme une lanterne, il faut un bloc KubeJS qui hérite de `LanternBlock`. À vérifier dans les types de blocs de KubeJS 2101, sinon le modèle reste statique.

### 2.10 Traveler's Backpack

- Les emplacements d'outils acceptent `travelersbackpack:acceptable_tools` = `#c:tools` + pioche en diamant. Overgeared range dans `c:tools` ses outils en acier et en cuivre, ses **marteaux** et ses **pinces**. Tout entre, sans réglage.
- **Amélioration « haut fourneau » du sac** : elle permet de chauffer du fer **en voyage**. Il faut choisir :
  - soit c'est voulu (forge de campagne) ;
  - soit on l'interdit, en retirant sa recette ou en l'empêchant d'accepter le fer.

  Le refroidissement dans un conteneur (mixin `ContainerCoolingMixin`) fera refroidir les lingots dans le sac (déduit).

### 2.11 Quark

- Overgeared remplace les recettes « Better Stone Tools » de Quark. Rien à faire.
- **Variant Furnaces** (activé) : les fourneaux en ardoise des abîmes et en pierre noire ont leur propre type d'entité de bloc. Il manque peut-être dans `coolingBlockEntityBlacklist`, qui ne liste que `minecraft:furnace` et `minecraft:blast_furnace` : un lingot de cuivre chaud refroidirait dans le fourneau. Ajouter son id dans la config, à lire en jeu.
- **Automatic Tool Restock** (que le §11.3 veut activer) : avec le système « cassé » d'Overgeared, l'outil à 0 de durabilité **ne disparaît pas toujours**. Le réapprovisionnement, qui se déclenche quand l'outil casse, risque de ne pas suivre (§5).
- **Automatic Recipe Unlock** : inutile à couper si on suit le §2.5 (plans d'Overgeared).

### 2.12 EMI

- Natif (§1.6). Les recettes ajoutées par datapack ou `event.custom` du même type s'affichent toutes seules : le plugin lit le gestionnaire de recettes par type (déduit).
- Cacher dans EMI ce qui n'existe pas dans Fallow : netherite, four à alliage du Nether, moule du Nether, outils en diamant à l'établi (recettes d'air).
- Réglage : `kubejs/client_scripts`, `RecipeViewerEvents.removeEntries` (KubeJS 2101), à valider.

### 2.13 Sons, Particular, Subtle Effects : les étincelles

- Les coups de marteau d'Overgeared font jaillir des particules **`CRIT`** : des étoiles blanches, comme un coup critique. Ce n'est pas « Realism Craft ». *Preuve : `ParticleUtils`, `BaseSmithingAnvilBlock` ne citent que `ParticleTypes.CRIT` ; le métal chaud fume (`SMOKE`) ; les fours à alliage font flamme et fumée.*
- Subtle Effects a « Anvil Use Particles », « Grindstone Use Particles » et « Smithing Table Use Particles ». Déduit : ces options suivent les usages vanilla (le menu de l'enclume, le menu de la meule), pas le martelage ni le polissage d'Overgeared, qui passent par un paquet réseau et un clic sur le bloc. Les **étincelles de fourneau** (`furnaceSparks`) marchent sur le haut fourneau vanilla, donc sur la forge.
- KubeJS ne peut pas intercepter les coups du mini-jeu : ils passent par le paquet `PacketSendCounterC2SPacket`, pas par un clic sur le bloc.
- **Petit mod custom (optionnel)** : un mixin client sur la création des particules de l'enclume remplacerait `CRIT` par des étincelles (`minecraft:lava`, `small_flame`) et des escarbilles, et pourrait jouer un son du mod Sounds. Une cinquantaine de lignes. C'est **le seul lien qui demande du code**.
- Polytone pourrait recolorer `CRIT` en orange, mais **partout**, y compris en combat. À éviter.
- Sounds (IMB11) : aucun conflit attendu. Overgeared a ses propres sons (`overgeared:anvil_hit`, `forging_complete`, `forging_failed`), que Sounds ne touche pas.

### 2.14 Visual Workbench et Easy Anvils

- **Visual Workbench** (jar 21.1.2) : il convertit le bloc d'établi en son propre bloc à inventaire (`BlockConversionHandler`, `CraftingTableWithInventoryBlock`). Overgeared ne dépend pas du bloc d'établi : la table à dessin se fabrique avec l'**objet** `minecraft:crafting_table`, qui ne change pas. **Pas de conflit.**
- **Easy Anvils** : **conflit réel** (§3.1).

### 2.15 Ce qu'il faut traduire et documenter

- `kubejs/assets/overgeared/lang/fr_fr.json` : les 294 clés de `en_us.json`, plus les types de plans cachés.
- Le carnet du colon (Modonomicon), chapitre « Forge » :
  - le caillou ;
  - l'enclume de pierre ;
  - le marteau de cuivre ;
  - le fer qui exige une pioche en cuivre ;
  - le haut fourneau, les pinces, la trempe, la meule ;
  - l'enclume de forgeron ;
  - l'acier ;
  - les plans.

---

## 3. Conflits et risques

### 3.1 Easy Anvils (§11.3)

- Easy Anvils 21.1.0 **remplace chaque bloc `AnvilBlock`** par une copie à inventaire, enregistrée sous `easyanvils:<namespace>/<chemin>`, donc `easyanvils:minecraft/anvil`. L'objet d'enclume vanilla pose ensuite ce nouveau bloc, et les enclumes déjà posées sont converties dès qu'on les utilise. *Preuve : `BlockConversionHandler.onRegistryEntryAdded` (`instanceof AnvilBlock`, id construit avec `"\u0001/\u0001"`), `onUseBlock`, `AnvilWithInventoryBlock`.*
- Or **Overgeared ne transforme en enclume de forgeron que les blocs du tag `overgeared:iron_anvil_bases` = `minecraft:anvil`**. Avec Easy Anvils, le joueur pose une enclume, la frappe au marteau… et rien ne se passe. **Il ne peut plus obtenir l'enclume de forgeron.**
- Correctif possible : ajouter `easyanvils:minecraft/anvil` au tag (datapack). Restent deux inconnues : quel mod garde le clic droit accroupi avec un marteau, et ce que deviennent les objets stockés dans l'enclume d'Easy Anvils quand Overgeared remplace le bloc.
- **Ce qu'Easy Anvils apporte est déjà dans Overgeared** :
  - l'enclume de forgeron montre déjà ses objets en 3D (`SmithingAnvilBlockEntityRenderer`) ;
  - la réparation passe par la meule.

  Il ne resterait que le renommage et la fin du « trop cher » sur l'enclume vanilla, qui n'est plus qu'une étape vers l'enclume de forgeron.
- **Recommandation : retirer Easy Anvils du §11.3.** Si on le garde, ajouter le tag et passer un test dédié.
- L'enclume de forgeron d'Overgeared n'est pas une `AnvilBlock` (elle hérite de `BaseEntityBlock`) : Easy Anvils ne la touche pas.

### 3.2 Le Nether fermé (décision 10.12-9)

Plusieurs pièces deviennent impossibles à fabriquer :
- **four à alliage du Nether** (briques du Nether) ;
- **fourneau de coulée** (briques du Nether rouges + diamant) ;
- **moule du Nether** ;
- **alliage de netherite** et **lingot de netherite** ;
- l'équipement « crystallized blood » de Companions.

Ce qu'on fait :
- les moules d'argile cuisent au fourneau et au haut fourneau, donc la coulée reste possible sans fourneau de coulée ;
- réassigner la recette du fourneau de coulée à des matériaux du monde normal (briques de boue, briques d'ardoise des abîmes) ou la cacher ;
- cacher le four du Nether (l'acier brut se fait déjà au four en briques) et la netherite.

### 3.3 Les forgerons villageois (`survivants.js`)

- Overgeared **ajoute** des échanges d'objets forgés et d'acier, et **enveloppe** les échanges vanilla des armuriers, forgerons d'outils et forgerons d'armes (`VillagerTradesEvent`).
- `MoreJSEvents.villagerTrades` avec `removeVanillaTrades` peut ne pas toucher aux échanges ajoutés par Overgeared, selon l'ordre des écouteurs. Vérifier en jeu, et utiliser au besoin `removeModdedTrades` ou un `removeTrades` filtré.
- Le marchand ambulant vend des plans, mais il est coupé (`doTraderSpawning false`). Rien à faire.

### 3.4 Qualité aléatoire sur tout le butin

- Toute table de butin donne une qualité aux objets qui s'usent : coffres, Lootr, boss, la récompense du Rascal.
- C'est cohérent, mais un plastron « Perfectly Forged » trouvé tôt court-circuite la forge.
- Régler les poids de qualité du butin (favoriser poor et well) et mettre les trophées de boss dans `overgeared:quality_blacklist` si on le souhaite.

### 3.5 Début de partie plus long que prévu

- Le minerai de fer demande une **pioche en cuivre**.
- Le seau, les cisailles et le chaudron passent par l'enclume. Le briquet et le chaudron demandent de l'**acier**.
- Le feu de veille (palier 1, « à la main ») ne doit donc pas demander de briquet. Les feux de camp se posent allumés.
- Les mods qui demandent un seau (Thirst, Kaleidoscope Cookery) attendent la première enclume de pierre.

### 3.6 Divers

- **Chauffe par ventilateur** : des lingots chauds sur un tapis roulant de Create. Le refroidissement des objets transportés n'est pas couvert par les mixins d'Overgeared (`ItemEntity`, conteneurs, inventaire) : ils resteraient chauds sur le tapis (déduit). Et les ramasser sans pinces fait mal.
- **Enclume qui tombe**, sur un engin Sable ou sous un pont : risque de dégâts et de casse (`onLand`, `getFallDamageSource`).
- **Doublons de plaques** : `overgeared:iron_plate` et `create:iron_sheet`, la plaque de cuivre et la feuille de cuivre. Sans Almost Unified (retiré au §10.5), les deux existent ; le tag `c:plates/*` les relie. Les cacher dans EMI ne s'impose pas.
- **Pierre de base** : `stone_anvil_bases` = `minecraft:stone` seul. Les pierres de NML ou de Quark (calcaire, jaspe…) ne donnent pas d'enclume. Accepter, ou étendre le tag.
- **Mises à jour** : le mod demande de **supprimer la config après une mise à jour** si quelque chose casse (page Modrinth). Livrer notre `overgeared-common.toml` dans `pack/config/` et le comparer à chaque version.

---

## 4. Ce qu'il faut adapter, supprimer ou ajouter dans le plan

| Endroit | Changement | Raison |
|---|---|---|
| 03 §3.1 et §3.4, âge 1 | « Enclume de pierre, forge, premier fer forgé, **bronze** » → « silex et caillou, enclume de pierre, marteau de cuivre, **pioche en cuivre pour le fer**, haut fourneau et pinces, enclume de forgeron, **acier** » | Pas de bronze ni d'étain dans Overgeared (§1.2) |
| 03 §3.4, âge 3 | « Laiton d'abord à la fonderie Overgeared » : garder l'idée, mais écrire la recette `overgeared:alloy_smelting` (§2.1) | Overgeared n'a pas de laiton |
| 02, tableau du butin ; 03 §3.6 et §3.9 | « Modèle de forge d'acier » → « **gabarit d'amélioration en diamant** » (`overgeared:diamond_upgrade_smithing_template`), à ajouter aux tables de NML | C'est l'amélioration acier → diamant. L'acier n'a pas de modèle |
| 03 §3.9, ligne Overgeared ; `progression.js`, âge 0-1 | Supprimer « recettes d'outils vanilla en métal retirées (config ou KubeJS) » et la boucle `event.remove(... crafting_shaped)` | Overgeared le fait déjà. Les outils vanilla en fer et en or **restent** comme objets forgés. Aucune option de config ne fait ça |
| 06, tableau des socles (JEI) | « plugin natif dans … Overgeared » : faux pour JEI ; plugin **EMI** | §1.6 |
| 07, incertitudes et `overgeared_pieces.js` | Remplir : composant `overgeared:forging_quality` ; types de recette (§1.5) ; KubeJS via `event.custom` | §1.4 et §1.5 |
| 08, étape 11 | Fermée : composant, types de recette, outils vanilla, étain (absent), butin du gabarit, Nether (§3.2) | — |
| `objets_fallow.js` | Garder `fallow:rivet_forge`, `essieu_forge`, `pale_helice_forgee`… comme **produits d'une forge `has_quality: true`**. Remplacer `fallow:tete_foreuse_forgee` par `overgeared:steel_pickaxe_head`, filtrée par qualité. Les objets `fallow:plan_*` deviennent des **plans d'Overgeared de types cachés** (§2.5) | Réutiliser les mécaniques du mod au lieu de les doubler |
| `plans_ingenieur.js` | Réduire à rien, ou au seul cas d'une recette d'établi sans pièce forgée. Le verrou passe par `requires_blueprint` | Plus robuste que `recipe give/take` et `doLimitedCrafting` |
| `zoo.js` (§10.10) | Récompense des 25 espèces = `overgeared:blueprint` de type `lanterne` | §2.4 |
| §10.3, palier 2 | « Pièces forgées chez Overgeared » → « forgée sur l'**enclume de forgeron** à partir d'un plan de lanterne, de plaques et d'acier chaud » (§2.4) | Une recette précise |
| §10.7, « La chaleur » | Ajouter : haut fourneau (déjà géré) et four à alliage (datapack Cold Sweat) ; lingots chauds en main (`item_temp`) | §2.8 |
| §10.7, nouveau lien « La forge » | Haut fourneau + soufflet (tag `bellows_tickable`) + enclume + eau ou chaudron + meule ; ventilateur de Create pour la chauffe ; mélangeur pour l'acier ; presse pour les plaques ; qualité des pièces lue par `neoforge:components` | §2.1, §2.9 |
| §10.7, « L'arbre » | Confirmé : toutes les haches (`AxeItem`) comptent, sans tag | §2.7 |
| §11.3 | **Retirer Easy Anvils**, ou le garder avec le tag `overgeared:iron_anvil_bases` et un test | §3.1 |
| §11.3, Quark Automatic Tool Restock | Activer, mais le tester avec l'état « cassé » d'Overgeared | §2.11 |
| Nouveau `pack/config/overgeared-common.toml` | Types de plans cachés ; poids de la qualité du butin ; `coolingBlockEntityBlacklist` (+ fourneaux de Quark) ; `enableQualityBreakSystem` à décider | §1.5 |
| Nouveau `kubejs/assets/overgeared/lang/fr_fr.json` | Traduction française | §1.1 |
| Nouveau `recipes/forge_create.js` | Mélangeur → acier brut ; presse → plaque d'acier ; laiton au four à alliage ; armures acceptant `#c:plates/*` ; option `_manual_only` | §2.1 |
| Nouveau `recipes/forge_fallow.js` | Lanterne, pièces critiques, couteaux de Kaleidoscope Cookery à la forge | §2.4, §2.6 |
| Nouveau `data/fallow/cold_sweat/block/block_temp/forge.json` et tag `supplementaries:bellows_tickable` | Chaleur et soufflet | §2.8, §2.9 |
| Décisions | Une fiche `docs/decisions/00xx-overgeared.md` : forge manuelle et chaleur industrielle ; plans d'Overgeared ; Easy Anvils ; Nether | Règle du dépôt |

**Faut-il un mod custom ?** **Non** pour les liens de jeu : datapack, config, tags et KubeJS (`event.custom`, KubeJS Create, LootJS, l'API KubeJS de Cold Sweat) couvrent tout. **Oui, en option**, pour un seul détail de finition : un petit mod client qui remplace les étincelles `CRIT` du martelage par des escarbilles et des étincelles orange (§2.13). Si un jour il faut qu'une machine Create produise un objet avec une qualité imposée, et que Create ne sait pas mettre de composant dans ses résultats, ce mod pourrait aussi s'en charger. Ce n'est pas nécessaire avec le principe du §2.1.

---

## 5. Questions qui demandent un test en jeu

1. **Lanterne avec plan caché** : un type ajouté à `availableToolTypes` et à `hiddenToolTypes` est-il reconnu ? Une recette avec `requires_blueprint: true` et `has_quality: false` exige-t-elle bien le plan ? Le plan donné par commande ou par butin avec `blueprint_data` fonctionne-t-il ?
2. **Le composant de qualité** s'écrit-il `overgeared:forging_quality="expert"` ? L'ingrédient `neoforge:components` est-il accepté et affiché par l'établi, l'assemblage séquencé de Create et EMI ?
3. **Chauffe au ventilateur de Create** : du fer devant de la lave donne-t-il bien `heated_iron_ingot` ? Refroidit-il sur un tapis, dans un dépôt, dans un entonnoir ? Le suffixe `_manual_only` bloque-t-il ce traitement ?
4. **Déployeur avec un marteau** sur l'enclume (recette de plaque) : la forge avance-t-elle ? Les entonnoirs et bras mécaniques remplissent-ils l'enclume et le four à alliage ?
5. **Easy Anvils** (si on le garde) : avec le tag élargi, la conversion en enclume de forgeron marche-t-elle ? Quel mod garde le clic accroupi ? Les objets dans l'enclume sont-ils perdus ?
6. **`event.replaceInput`** traverse-t-il les clés d'une `overgeared:forging` (plaques → `#c:plates/iron`) ?
7. **Soufflet** sur le four à alliage (après ajout au tag) : accélère-t-il vraiment ?
8. **Cold Sweat** : la chaleur du four à alliage et des lingots chauds en main ; l'isolation des armures en acier et en cuivre.
9. **Quark Automatic Tool Restock** avec un outil qui passe à l'état « cassé » au lieu de disparaître. **FallingTree** avec une hache cassée.
10. **Fourneaux de Quark** : un lingot de cuivre chaud y refroidit-il ? Quel est l'id de leur entité de bloc pour `coolingBlockEntityBlacklist` ?
11. **Échanges des forgerons** : après `survivants.js`, l'armurier et le forgeron d'outils vendent-ils encore des objets forgés par Overgeared ?
12. **Engin Sable** : poser une enclume de forgeron, forger en vol, tremper dans un chaudron à bord ; l'enclume tombe-t-elle au désassemblage ?
13. **Amélioration « haut fourneau » de Traveler's Backpack** : chauffe-t-elle le fer, et les lingots refroidissent-ils dans le sac ?
14. **Mini-jeu à plusieurs** : `maxAnvilDistance`, enclume occupée par un autre joueur. Un ami peut-il regarder sans gêner ?
15. **Couteaux de Kaleidoscope Cookery** passés à la forge : prennent-ils la qualité (classe `TieredItem`) et gardent-ils leur fonction de découpe ?
