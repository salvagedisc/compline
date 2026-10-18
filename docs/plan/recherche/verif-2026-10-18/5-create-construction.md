# 5. Create, construction et infrastructure : vérification du 2026-10-18

Vérification du plan v2 (chapitres 10 et 11, brouillons KubeJS, chapitres 03, 06 et 07) pour Create et ses addons, KubeJS et ses extensions, les mods de construction et l'infrastructure.

**Méthode**
- Versions relevées sur l'API Modrinth le 2026-10-18, filtre `neoforge` + `1.21.1`.
- 30 jars ouverts dans `scratchpad/jars/` (mes dossiers : `jars/<slug>/` et `jars/s5/<slug>/`) : `javap`, JSON de `data/` et `assets/`, `mods.toml`.
- GitHub est fermé à cette session (proxy 403, MCP limité à `salvagedisc/fallow`). Les tickets d'Aeronautics cités au chapitre 06 n'ont donc **pas pu être relus**.
- Repères : « jar » = lu dans le jar ; « Modrinth » = API ; « déduit » = raisonnement sans test.

---

## 1. Ce qui est confirmé

### 1.1 Versions NeoForge 1.21.1 sur Modrinth

| Mod | Dans le dépôt | Dernière version | Dépendances requises (Modrinth + `neoforge.mods.toml`) | Note |
|---|---|---|---|---|
| Create | 6.0.10 | 6.0.10 (2026-04-21) | — | à jour |
| Create Aeronautics | 1.3.2 | 1.3.2 (2026-08-29) | Sable, Create | jar « bundled » : contient Aeronautics, Simulated et Offroad (`META-INF/jarjar`) |
| Sable | 2.0.5 | **2.0.6** (2026-10-03) | Create et imguimc optionnels ; embarque Veil 4.3.2 et Sable Companion | correctif des entités sur les fluides |
| Steam 'n' Rails | 0.3.0-beta.2 | 0.3.0-beta.2 (bêta) | Create | les voies des mods tiers ne s'enregistrent que si le mod est chargé (`TrackCompatUtils` → `Mods.isLoaded`, jar) |
| Create Ore Excavation | — | 1.21.1-1.6.8 | Create ≥ 6.0.7 ; KubeJS ≥ build.295 et KubeJS Create ≥ build.14 optionnels | 15 gisements par défaut, dont 4 du Nether (jar) |
| KubeJS | 2101.7.2-build.377 | idem | Rhino ≥ 2101.2.7-build.81 ; JEI, REI et **EMI ≥ 1.1.22** optionnels | intégration EMI native (`integration/emi`, jar) |
| KubeJS Create | — | 2101.3.1-build.18 (**bêta**, 2025-11-12) | KubeJS ≥ build.285, Create ≥ 6.0.7 | compatible avec Create 6.0.10 |
| MoreJS | — | 0.16.0 (2025-10-13) | KubeJS | |
| LootJS | — | 3.7.0 (2026-04-29) | KubeJS (optionnel côté jar) | serveur seul |
| Quark | 4.1-485 | **4.1-487** (2026-10-05) | Zeta [1, 1.2) ; **Biolith [3.0.0, 3.1.0)** (le dépôt a 3.0.14, OK) ; Lootr optionnel | |
| Supplementaries | 3.9.9 | **4.0.2** (2026-10-16) | Moonlight ≥ 3.6.4 ; Sodium < 0.8.12 déclaré incompatible (le pack a 0.8.13) | saut de version majeure : toutes les sections de `supplementaries-common.toml` existent encore dans le jar 4.0.2 |
| Amendments | — | 1.21-2.1.11 | Moonlight | contient un dossier `assets/amendments/blockstates/twigs` (lien natif avec Twigs) |
| Moonlight | 3.6.9 | **3.7.2** | — | |
| Handcrafted | — | 4.0.3 (2025-03) | Resourceful Lib ≥ 3.0.0 (déjà là) | |
| Macaw's Roofs | — | 2.3.2 | — | |
| Macaw's Windows | — | 2.4.2 | — | |
| Twigs | — | 1.21.1-3.1.2 | — | |
| Every Compat | — | 1.21-2.11.54 (2026-10-17) | Moonlight ≥ 3.1.0 | |
| FallingTree | — | 1.21.1.11 | aucune (Cloth Config optionnel, client) | un même jar porte des classes Fabric, Forge et NeoForge, avec un `neoforge.mods.toml` valide |
| Leaves Be Gone | — | v21.1.1 | **Puzzles Lib** | serveur seul |
| Visual Workbench | — | 21.1.2 | Puzzles Lib ≥ 21.1.48 | |
| Easy Anvils | — | v21.1.0 (2024-09) | Puzzles Lib | |
| Easy Magic | — | v21.1.4 | Puzzles Lib | |
| Puzzles Lib | — | 21.1.64 | — | nouvelle bibliothèque (1,45 Mo) |
| Traveler's Backpack | — | 1.21.1-10.1.39 (slug **`travelersbackpack`**, sans tiret) | aucune (Curios, Accessories, EMI, JEI, LambDynamicLights… optionnels) | |
| Curios | 9.5.1 | 9.5.1 | — | |
| Default Options | — | 21.1.8 | **Balm** ≥ 21.0.39 (nouvelle bibliothèque, 0,78 Mo) | client seul |
| Simple Backups | — | 1.21-4.0.30 (**bêta**) | — | |
| GraveStone | — | 1.0.40 | Jade optionnel | |
| Neruina | — | 3.3.5 (2026-10-17) | **Configurable** ≥ 3.5.1 (nouvelle bibliothèque) | |
| spark | 1.10.124 | 1.10.124 | — | |
| Lootr | — | 1.11.38.127 | — | |
| Entity Culling | 1.11.2 | **1.11.3** (2026-10-08) | — | client |
| EMI | — | 1.1.24 | — | |
| No Man's Land | **1.5.12** | 1.6.1 (2026-10-14) | Biolith | j'ai lu la 1.6.1 : à vérifier sur la version retenue |

Bibliothèques nouvelles qu'apporte cette partie : Puzzles Lib (4 mods), Balm (Default Options), Configurable (Neruina). Les mods de construction (Handcrafted, Macaw's, Twigs, Every Compat) n'en ajoutent aucune.

### 1.2 Les bois du monde (jars)

- **No Man's Land a 4 essences complètes** : érable (`maple`), pin (`pine`), noyer (`walnut`) et saule (`willow`). Chacune a planches, bûches, bois, version écorcée, portes, trappes, panneaux, palissades, **sièges, armoires (`cabinet`) et bibliothèques**. Le `petrified_log` n'a pas de planches : ce n'est pas une essence. Les feuillages sont 11 : érable, érable rouge, chêne d'automne, bouleau jaune, cerisier pâle, givré, pin, noyer, saule, pommier et poirier (tag `minecraft:leaves` de NML).
- **NML a déjà prévu Every Compat** : le jar contient `assets/everycomp/textures/block/c/nomansland/palettes/<bois>_window.png`, les palettes des fenêtres Create pour ses 4 bois. Every Compat cite `nomansland` dans `HardcodedBlockType`, mais seulement pour exclure les modules `curiosities` et `mctb`, qui ne sont pas dans le pack.
- **Quark n'ajoute aucun bois qui pousse** : `Ancient Wood`, `Azalea Wood` et `Blossom Trees` sont coupés dans `quark-common.toml`. Mais **Zeta enregistre quand même leurs blocs**, car c'est le `loadBus` qui porte l'enregistrement, pour tous les modules ; `setEnabled` ne retire que l'abonnement au `playBus` (classes `ZetaModule` et `ZetaModuleManager`, jar Zeta 1.1-40). Every Compat et Steam 'n' Rails voient donc trois bois Quark : `quark:ancient`, `quark:azalea` et `quark:blossom`. Le jar de Steam 'n' Rails a d'ailleurs des voies `track_quark_ancient/azalea/blossom` (déduit : il les génère).

### 1.3 Ce qu'Every Compat génère réellement (classes `modules/*` du jar)

| Module EC | Blocs par essence | Mod dans le pack ? |
|---|---|---|
| Handcrafted | 15 : chaise, table, banc, canapé, placard, bureau, banc de salle à manger, commode, lit décoré, table de chevet, étagère, desserte, comptoir, moulures d'angle et de pilier | oui |
| Macaw's Roofs | 14 toits | oui |
| Macaw's Windows | 18 : fenêtres, fenêtres 4 carreaux, parapets, volets, persiennes, stores, tringles, variantes écorcées | oui |
| Twigs | 1 : la table | oui |
| Quark | 8 à 9 : coffre, coffre piégé, échelle, poteau, poteau écorcé, planches verticales, dalle verticale, bûche creuse, bibliothèque. Plus, **par feuillage**, une haie et un tapis de feuilles | oui |
| Create | 2 : fenêtre et vitre | oui |
| Friends & Foes | 1 : ruche | oui (déjà dans le pack) |
| Supplementaries | 1 : panneau de direction (`way_sign_<bois>`, généré par Moonlight) | oui |

Estimation (déduite du nombre d'entrées, à compter en jeu) :
- **Bois de NML** : environ 60 blocs par essence, soit environ 240, plus 16 à 22 haies et tapis de feuilles (Moonlight ne sait pas forcément rattacher les feuillages « automne » et « fruits » à une essence) : **250 à 270 blocs**, chacun avec son objet.
- **Bois fantômes de Quark** : environ 50 blocs chacun, soit **environ 150 blocs pour des arbres qui ne poussent jamais**.

Every Compat a deux niveaux de configuration (classes `ModEntriesConfigs` et `UnsafeDisablerConfigs`) :
- **Le niveau normal** cache des blocs et coupe leurs recettes, mais les blocs restent enregistrés : « only hide stuff from tabs and disable their recipes ».
- **Le niveau « hazardous »** a des listes noires par `woodtype`, `leavestype`, `module` et `entryset`. Ces listes empêchent vraiment l'enregistrement.

### 1.4 Le poids en blocs (blockstates comptés dans les jars)

| Mod | Blocs | Objets (modèles) |
|---|---|---|
| Steam 'n' Rails | 1 440 fichiers, dont 1 110 hors voies ; voies réellement présentes : bois vanilla, Quark ×3, andésite, etc., ×3 écartements | 1 866 |
| Quark (tout est enregistré, modules coupés compris) | 809 | 991 |
| Create | 643 | 751 |
| Macaw's Roofs | 605 | 607 |
| No Man's Land | 376 | — |
| Macaw's Windows | 328 | 326 |
| Supplementaries | 276 | 304 |
| Handcrafted | 267 | 285 |
| Twigs | 256 | 259 |
| Aeronautics + Simulated + Offroad | 43 + 95 + 3 | — |
| Traveler's Backpack | 60 | 85 |
| Amendments | 41 | — |
| Every Compat (généré) | environ 400 (1.3) | autant |

À eux seuls, ces mods font **environ 5 500 blocs**. Les seuls ajouts de la v2 pour la construction (Handcrafted, Macaw's ×2, Twigs, Every Compat) en apportent **environ 1 850**, dont 605 toits (les toits de Macaw existent en pierre, en terre cuite de 16 couleurs, etc.).

Pour EMI, c'est environ 2 000 entrées de plus. EMI les range par groupes (11.7), mais il faut les grouper ou les cacher, sinon l'index se noie (section 2.6).

Pour la mémoire, le coût vient surtout des **états de blocs** et des modèles cuits : les toits et les escaliers ont beaucoup d'états. ModernFix (déjà là) charge les modèles à la demande, et c'est ce qui limite la casse (déduit). **À mesurer avec spark**, avec et sans Macaw's Roofs.

### 1.5 Créations, KubeJS et ses extensions (jars)

- **Événements KubeJS 2101.7.2 disponibles** (interfaces `plugin/builtin/event/*`) :

  | Groupe | Événements |
  |---|---|
  | `BlockEvents` | `rightClicked`, `placed`, `broken`, `drops`, `randomTick`, **`blockEntityTick`**, `picked`… |
  | `ItemEvents` | `rightClicked`, `crafted`, `modifyTooltips`… |
  | `PlayerEvents` | `loggedIn`, `tick`, `inventoryChanged`, `chestOpened`, **`stageAdded`/`stageRemoved`**… |
  | `EntityEvents` | `death`, `beforeHurt`, `afterHurt`, `checkSpawn`, `spawned`, `entityDrops` |
  | `ServerEvents` | `recipes`, `tags`, `tick`, `loaded`, `modifyRecipeResult`… |
  | `RecipeViewerEvents` | `addEntries`, `removeEntries`, `removeEntriesCompletely`, `groupEntries`, `addInformation`, `removeCategories`, `removeRecipes` |

- **`NativeEvents.onEvent(Class, handler)`** existe (`NativeEventWrapper`). Le paramètre est une **`Class`** : il faut passer `Java.loadClass('…')` si la conversion depuis une chaîne ne se fait pas. `net.neoforged` est autorisé par `kubejs.classfilter.txt`, sauf `fml`, `accesstransformer` et `coremod`. Les classes des mods (Create, Wildex…) ne sont pas filtrées.
- **Les recettes à palier n'existent plus** : `KubeRecipe.stage()` lève l'erreur « recipe.stage() is no longer supported by default due to vanilla changes! ». L'événement `ModifyCraftingItemKubeEvent` (celui de `modifyResult`) n'a **pas de joueur**, seulement la grille. On ne peut donc pas verrouiller une recette par joueur en KubeJS pur. Les paliers par joueur restent (`player.stages`), et LootJS sait les lire (`hasAnyStage`).
- **Actions sur ingrédients** : `keepIngredient`, `damageIngredient` et `replaceIngredient` existent (`KubeRecipe`, `recipe/ingredientaction/*`).
- **Blocs avec block entity** : `BlockEntityInfo` propose `serverTicking`, `tickFrequency`, `attach` (inventaire, réservoir, énergie) et `eventHandler`.
- **KubeJS Create 2101.3.1** :
  - schémas de recettes Create, constructeurs d'objets « séquencés » et de papier de verre ;
  - événements `SpecialFluidHandler`, `SpecialSpoutHandler` et `BoilerHeaterHandler`.
  - **Aucun constructeur de bloc cinétique** : impossible de créer en KubeJS un bloc qui consomme du stress.
- **LootJS 3.7** :
  - `LootJS.modifiers(event => event.addTableModifier(…))` et `LootJS.lootTables` ;
  - conditions `randomChance`, `matchStructure`, `matchBiome`, **`hasAnyStage`**, `matchPlayer` ; actions `addLoot`, `removeLoot`, `replaceLoot`, `playerAction`, `customAction`.
  - La syntaxe `.when(c => c.randomChance(0.5))` du brouillon `butin.js` est celle de LootJS 2 : en 3, on chaîne `.randomChance(0.5).addLoot(…)`.
- **MoreJS 0.16** : le groupe s'appelle **`MoreJS`**, pas `MoreJSEvents` (le brouillon `survivants.js` est faux). Événements : `villagerTrades`, `wandererTrades`, `updateOffer`, `structureLoad`, `structureAfterPlace`, `teleport`, `enchantmentTableChanged`, `playerEnchant`…
- **Create Ore Excavation 1.6.8** : `event.recipes.createoreexcavation.vein(…)` avec `alwaysFinite`, `biomeWhitelist`, `biomeBlacklist`, `placement`, `veinSize`, `priority` ; binding `coeutil` (jar). Les 15 gisements par défaut (`recipe/ore_vein_type/*.json`) ont `"finite":"default"` et `biomeWhitelist: minecraft:is_overworld`. Il faut les **supprimer** (`event.remove({type:'createoreexcavation:vein'})`) avant d'ajouter ceux de Fallow.
- **Wildex 3.0.0** expose `de.coldfang.wildex.integration.kubejs.WildexKubeJsScriptBridge.onDiscoveryChanged(cb)` et `onCompleted(cb)`. Le paramètre reçu porte `playerUuid`, `playerName`, `mobId`, `discovered`, `total`, `percentScaled` et `isComplete`. `zoo.js` est faisable (section 2.5).

### 1.6 Le bois et les outils (jars)

- **FallingTree** (configuration `fallingtree.json`, classes `config/real/*`) :
  - Modes : `INSTANTANEOUS`, `FALL_ITEM`, `FALL_BLOCK`, `FALL_ALL_BLOCK`, `SHIFT_DOWN`.
  - Défauts : `breakMode=INSTANTANEOUS`, `maxSize=100` avec `maxSizeAction=ABORT`, `minimumLeavesAroundRequired=1`, **`leavesBreaking=true`**, `adjacentStopMode=STOP_ALL`.
  - Une hache, c'est un **`instanceof AxeItem`**. Les haches d'Overgeared (`steel_axe`, `copper_axe`) sont des `new AxeItem` (`ModItems`) : elles marchent sans aucun tag.
  - Les bûches se reconnaissent par `minecraft:logs`, et NML y met ses 4 bûches.
- **Scie Create** :
  - Défauts de `CRecipes` : `allowShapelessInMixer=true`, `allowShapedSquareInPress=true`, `allowRegularCraftingInCrafter=true`, `allowStonecuttingOnSaw=true`.
  - La scie traite les recettes `create:cutting`. Les `LogStrippingFakeRecipes` ne servent qu'à l'affichage de l'écorçage au déployeur.
  - **NML livre ses recettes de scie au mauvais endroit** : `data/create/cutting/compat/nomansland/*.json`, alors qu'en 1.21.1 il faut `data/create/recipe/…`. NML utilise d'ailleurs le bon chemin pour `data/create/recipe/filling/honeyed_apple.json`. Ces 8 recettes (bûche → bûche écorcée, bois → bois écorcé) ne se chargent donc pas (déduit du chemin, à confirmer dans EMI). Il n'existe de toute façon **aucune recette « écorcé → planches »** pour NML, ni chez NML ni chez Create 6.0.10 (son dossier `recipe/cutting/compat/` ne couvre pas `nomansland`).
  - L'abattage d'arbres par la scie (`TreeCutter`) s'appuie sur les tags `logs` et `leaves` : il marche avec NML.

### 1.7 Aeronautics et Sable (jars)

- **Masse des blocs sur Sable** : tags `sable:light`, `super_light`, `half_volume`, `quarter_volume`, `heavy`, `super_heavy`, `weightless`, plus `physics_block_properties/*.json` (`sable:mass`).
  - Sable couvre déjà les planches, bûches, dalles, escaliers, Quark (dalles verticales, haies) et Supplementaries (bocal, tableau noir…).
  - NML ajoute ses propres tags `sable:light/heavy` et `simulated:non_movable` (bassin, bougie et cloche lunaires).
  - **Handcrafted, Macaw's, Twigs, Every Compat et Traveler's Backpack n'ont rien** : ils pèsent comme un bloc plein « normal » (déduit).
- **Aucune intégration** dans Aeronautics, Simulated ni Sable avec Lootr, Traveler's Backpack, GraveStone ou Entity Culling (recherche de chaînes dans les trois jars imbriqués et dans Sable). Aeronautics a des mixins pour Sodium et Iris (`mixin/render/iris/WorldRenderingPhaseMixin`), ce qui confirme que la question d'Iris est connue de l'équipe.
- **Entity Culling 1.11.0 à 1.11.3** : « Fix NeoForge not using the getRenderBoundingBox method for BlockEntities » (changelog Modrinth). C'est précisément le genre de cause qui fait disparaître les block entities d'un engin. Le dépôt a la 1.11.2, qui contient déjà ce correctif : **le test de 06 §6.1 est à refaire avec 1.11.3 avant de retirer le mod**. Clés de configuration (jar) : `tickCulling`, `tickCullingWhitelist`, `skipBlockEntityCulling`, `blockEntityWhitelist`, `entityWhitelist`, `solidLeaves`.

### 1.8 Infrastructure (jars)

- **GraveStone** (`GraveUtils.getGraveStoneLocation`) :
  - La tombe va **au point de la mort** si le bloc est remplaçable. Sinon elle **monte**. Il n'y a aucune recherche vers le bas.
  - Configuration serveur : `strict_placement`, `replaceable_blocks`, `only_owners_can_break`, `enable_obituary`, `spawn_ghost`, `friendly_ghost`, `break_pickup`, `sneak_pickup`.
- **Traveler's Backpack** (classes `TravelersBackpackConfig$*`). Clés utiles :
  - `enableSleepingBag`, `enableSleepingBagSpawnPoint`, `quickSleepingBag` ;
  - `backpackDeathPlace`, `backpackForceDeathPlace`, **`sendBackpackCoordinatesMessage`** ;
  - `enableCraftingUpgrade`, `backSlotIntegration` ;
  - `spawnEntitiesWithBackpack`, `enableVillagerTrade`, `enableLoot` ;
  - `voidProtection`, `invulnerableBackpack`.
- **Visual Workbench** : `VisualCraftingMenu extends CraftingMenu`. Il respecte donc `doLimitedCrafting`.
- **Le sac de Traveler's Backpack, lui, ne le respecte pas** : son amélioration d'artisanat appelle `ResultContainer.setRecipeUsed(RecipeHolder)` (la version sans joueur ni contrôle), et non `setRecipeUsed(Level, ServerPlayer, RecipeHolder)` (`BackpackBaseMenu.slotChangedCraftingGrid`).
- **Overgeared**
  - Ses enclumes sont des blocs à part (`AbstractSmithingAnvilBlockEntity`). Le tag `overgeared:iron_anvil_bases` contient `minecraft:anvil` : l'enclume vanilla sert de base. Easy Anvils remplace l'enclume vanilla par un bloc à inventaire (mixin `AnvilBlockMixin`). Les deux touchent le même bloc : à tester (section 5).
  - **Overgeared a son propre système de plans** : `overgeared:blueprint` (« Smithing Blueprint »), avec un type d'outil et une qualité.
    - Les recettes de forge acceptent `"blueprint": [types]` et `"requires_blueprint": true`.
    - On peut ajouter des types dans `availableToolTypes`, plus une clé de langue (`doc.txt` d'Overgeared).
    - C'est une piste sérieuse pour les plans d'ingénieur (section 2.3).

---

## 2. Les liens à construire

### 2.1 Le bois, fil conducteur (§10.7)

| Lien | Quoi | Outil |
|---|---|---|
| Arbre → bûches | FallingTree en `FALL_BLOCK`. Monter `maxSize` à 200–250 (les grands arbres de NML et `ancient_tree` dépassent sans doute 100 ; avec `ABORT`, seule une bûche tomberait) ; garder `minimumLeavesAroundRequired=1` (les troncs couchés de NML, `fallen_tree*`, et les maisons en rondins ne tombent pas) et `adjacentStopMode=STOP_ALL` | `config/fallingtree.json` |
| Feuilles | **FallingTree fait déjà tomber les feuilles** (`leavesBreaking=true`). Leaves Be Gone est en trop, et il est sur la liste noire d'Aeronautics (section 3) | retirer Leaves Be Gone |
| Haches | Rien à faire : les haches d'Overgeared sont des `AxeItem` | — |
| Scierie | Écrire les 16 recettes de scie de NML : bûche → écorcée, bois → bois écorcé, écorcée → 6 planches, bois écorcé → 6 planches, pour l'érable, le pin, le noyer et le saule | `ServerEvents.recipes` + `event.recipes.create.cutting([...], 'nomansland:stripped_maple_log')` (KubeJS Create) |
| Abattage mécanique | La scie Create abat les arbres de NML (tags) : c'est l'étape « industrie » du bois | natif |
| Mobilier dans chaque bois | Every Compat pour les 4 bois de NML ; **liste noire des bois de Quark** (section 2.2) | `everycomp` (niveau hazardous) |
| Voies ferrées en bois | Steam 'n' Rails n'a pas de voie en érable, pin, noyer ni saule. Pas faisable en KubeJS (il faut l'API `TrackMaterial` de Create) | mod custom optionnel (section 4.3) |
| Masse sur engin | Mobilier et toits légers sur un dirigeable | `ServerEvents.tags('block')` : `sable:light` ← `#handcrafted:…`, `/^mcwroofs:.*/`, `/^mcwwindows:.*/`, `/^everycomp:.*/` ; `sable:half_volume` ← chaises, tables, toits. Les tags exacts sont à lire avec `/kubejs dump` |
| Montrer le travail | Visual Workbench (établi), Easy Anvils (enclume vanilla), Easy Magic (table d'enchantement) | natif |

### 2.2 Doublons de construction : quoi garder

Règle proposée : **le style de NML d'abord** (c'est le monde), puis le mod le plus spécialisé.

| Bloc | Sources | Conflit prouvé | Décision |
|---|---|---|---|
| Briques de pierres (+ moussues, fissurées, dalles, escaliers, murets) | NML **et** Twigs | Même recette, 4 pierres en carré → 4 (`nomansland:cobblestone_bricks` et `twigs:cobblestone_bricks`, jars) | Garder NML ; supprimer les recettes `twigs:*cobblestone_brick*` et les cacher dans EMI |
| Briques de gravier | Supplementaries (actif) **et** Twigs | Même recette, 4 graviers en carré (jars) | Garder Twigs (il a dalle, escalier et muret) ; `building.gravel_bricks.enabled=false` dans Supplementaries |
| Chaume | Quark Thatch (actif) **et** NML (`thatch`, dalle, escalier) | 4 blés : en carré chez Quark, en vrac chez NML, donc la même grille (jars) | Couper `Thatch` dans Quark |
| Bibliothèques par bois | Quark Variant Bookshelves (actif) **et** NML, qui en a pour **tous** les bois vanilla et les siens | Même motif, 3 planches / 3 livres / 3 planches (jars) | Couper `Variant Bookshelves` dans Quark |
| Appliques | Supplementaries `sconce` (actif) **et** NML `sconce_torch` (+ soul, et versions éteintes) | Même forme : pépite, torche, pépite au-dessus d'une pépite (jars) | Garder NML (ses torches s'éteignent) ; `building.sconce.enabled=false` |
| Galets, coquillages | NML `pebbles`, `seashells` **et** Twigs `pebble`, 4 coquillages, générés dans le monde | Double décor au sol (biome modifiers de Twigs `add_pebbles`, `add_land_sea_shells`, `add_water_sea_shells`) | Neutraliser ces 3 modifiers (`{"type":"neoforge:none"}` dans `kubejs/data/twigs/neoforge/biome_modifier/`) ; **garder les brindilles** (`add_twigs`), que NML n'a pas, et ajouter les forêts de NML au tag `#twigs:spawns_twig` si elles ne sont pas dans `#minecraft:is_forest` |
| Pierres de Twigs (rhyolite, schiste, pierre de sang) | Twigs génère de la rhyolite **dans tout l'Overworld** (`add_ore_rhyolite`), plus du schiste et de la pierre de sang | Worldgen non réversible, en plus de la siltite et de la quartzite de NML | À décider avant le monde : couper ces 3 générations (garder les blocs, fabricables) |
| Tables | Tables de Twigs (une par bois) **et** Handcrafted (table, desserte, table de chevet) | Doublon de rôle | Couper le module `twigs` d'Every Compat (niveau hazardous) et cacher les tables Twigs |
| Toits | Macaw's Roofs **et** Quark Shingles (bardeaux colorés, actif) | Doublon de rôle, sans conflit de recette | Optionnel : couper `Shingles` |
| Dalles, escaliers et murets de tuf | Twigs `tuff_slab/stairs/wall` **et** vanilla 1.21 | Twigs n'a pas de recette pour eux (blocs hérités) | Les cacher dans EMI |
| Trophées Handcrafted | Creeper, squelette, araignée, sorcière, pillard, vindicateur, évocateur, wither squelette, phantom, poisson d'argent, blaze… | Les créatures ont disparu (0003) | Les cacher dans EMI, ou supprimer leurs recettes |
| Étagères | Supplementaries `item_shelf` **et** Handcrafted `shelf` | Rôles différents (un objet exposé, ou quatre) | Garder les deux |
| Sièges, armoires | NML (`seat`, `cabinet`) **et** Handcrafted | Styles différents | Garder les deux |

Config d'Every Compat (fichier hazardous ; nom exact à relever au premier lancement) :
- Liste noire `woodtype` : `quark:ancient`, `quark:azalea`, `quark:blossom`. On gagne environ 150 blocs. Le même nettoyage vaut pour les voies de Steam 'n' Rails : il faut les cacher, car il les enregistre dès que Quark est chargé.
- Liste noire `module` : `twigs`.
- `dynamic_assets_generation_mode = CACHED`, pour ne pas régénérer les textures à chaque démarrage (option du jar).

### 2.3 Les plans d'ingénieur (§10.10) : quatre façons, une recommandation

| Option | Comment | Ce qui fuit | Coût |
|---|---|---|---|
| **A. `doLimitedCrafting`** (brouillon actuel) | `recipe give *` à la connexion, puis `recipe take` des recettes verrouillées ; lire le plan les rend | 1) **Les succès-recettes rendent les recettes** : Create en a 1 071, par exemple `create:recipes/misc/crafting/kinetics/mechanical_press` donne la presse dès qu'on **tient un boîtier d'andésite** (jar). Il faut écraser chaque succès concerné dans `kubejs/data/<mod>/advancement/recipes/…`. 2) Le **sac de Traveler's Backpack** l'ignore : couper `enableCraftingUpgrade`. 3) **Assembleurs mécaniques, mélangeur et presse** de Create : pas de joueur, donc pas de contrôle (`allowRegularCraftingInCrafter=true` par défaut). 4) Toute autre grille moddée. 5) Quark `Automatic Recipe Unlock` est **toujours à `true`** dans le dépôt | KubeJS seul, mais fragile |
| **B. Le plan posé sur l'établi** | Le plan devient un ingrédient **gardé** des recettes verrouillées : `event.shaped('create:mechanical_press', [...], {..., P: 'fallow:plan_atelier'}).keepIngredient('fallow:plan_atelier')` | Le savoir n'est plus personnel : il est **physique**. Le plan se prête et peut se perdre (cohérent avec un monde permanent entre amis). On peut ajouter une copie du plan (papier + encre antique de Supplementaries) | KubeJS seul, robuste. Le mélangeur et la presse ne fabriquent pas de recettes à motif irrégulier. Visual Workbench montre le plan posé sur l'établi, en 3D |
| **C. Les plans d'Overgeared** | Ajouter des types (`rail`, `vapeur`, `aerostat`…) dans `availableToolTypes`, puis `requires_blueprint: true` sur les recettes de forge des pièces `fallow:*` qui verrouillent déjà les machines (`progression.js`). Le plan trouvé dans une ruine est un `overgeared:blueprint` de ce type, donné par LootJS | Il faut vérifier que l'écran de choix du type ne propose pas ces types au joueur (paquet `SelectToolTypeC2SPacket`) ; sinon tout le monde peut se faire le plan | Configuration + LootJS. **Un seul système de plans** dans le pack, au lieu de deux qui se ressemblent (« Smithing Blueprint » contre « plan d'ingénieur ») |
| **D. AStages** (`astages` 2.5.3, NeoForge 1.21.1, Modrinth) | Verrous de recettes par joueur, avec intégration KubeJS ; mixins sur `CraftingMenu`, `CrafterBlock`, fours, tailleur de pierre, forge | Lourd : 0,98 Mo, licence « tous droits réservés », 26 000 téléchargements. Il pose des mixins de rendu (`ASectionCompiler`, `ALevelChunkSection`) qui sont un risque avec Sodium 0.8 et Sable | Non retenu sans test |
| **E. Mod custom `fallow-plans`** | Section 4.2 | aucune fuite connue | 7 à 10 classes |

**Recommandation : B tout de suite, C à tester, E si l'équipe tient à un savoir personnel.**
- B ne demande aucun Java, marche dans toutes les grilles, se voit dans EMI (un plan est nécessaire, on ne sait pas où il est) et lie Visual Workbench au système.
- La gamerule `doLimitedCrafting` sort alors de `init.mcfunction`. Elle touche **toutes** les recettes du pack, même hors plans.

Où trouver les plans (tables de butin de NML 1.6.1, lues dans les fichiers `.nbt` des structures) :

| Table de butin | Où elle sert | Plan proposé |
|---|---|---|
| `nomansland:chests/small_dungeon_core` | Cœurs des petits donjons | Plans « atelier » et « vapeur » |
| `nomansland:chests/redstone_parts` | Salle piégée, redstone et cuivre | Plan « rail » : le thème mécanique colle |
| `nomansland:chests/water_well` | Puits profonds | Plan « forage » (COE), car on y descend sous terre |
| `nomansland:chests/sniffer_monument` | Monument | Plan rare : « aérostat » |
| Ruines de l'alchimiste, abris souterrains, temples, sanctuaires de cloche | — | Avec `matchStructure(...)` de LootJS sur `minecraft:chests/simple_dungeon`, qu'ils réutilisent |

Avec Lootr, le contexte de butin contient le joueur qui ouvre le coffre (déduit). `hasAnyStage('plan_rail')` évite donc de redonner un plan déjà lu, si l'on choisit A ou E.

### 2.4 Le Dissipateur (palier 64 des clairières, §10.3)

KubeJS Create ne sait pas créer de bloc cinétique (1.5). Deux voies :

1. **KubeJS seul : le Dissipateur est une tête posée devant un ventilateur Create**
   - Bloc `fallow:dissipateur` avec block entity : `.blockEntity(be => { be.serverTicking(); be.tickFrequency(20) })`.
   - Dans `BlockEvents.blockEntityTick('fallow:dissipateur', e => …)`, on lit le bloc voisin. Si c'est un `create:encased_fan` tourné vers la tête, on lit `getSpeed()` sur son block entity (méthode publique de `KineticBlockEntity`, que le filtre de classes de KubeJS ne bloque pas).
   - Au-delà de 128 rpm pendant N minutes (compteur dans les données du block entity), la conversion du rayon de 64 démarre.
   - **C'est le ventilateur qui consomme le stress** : il faut une vraie installation pour le faire tourner.
   - Limites : pas d'info aux lunettes d'ingénieur sur la tête, pas de Ponder, et un appel à une classe interne de Create à revérifier à chaque mise à jour.
2. **Mod custom** (section 4.1) : un vrai bloc cinétique avec un coût en stress, l'info aux lunettes, un Ponder, et la conversion en Java.

Recommandation : **1 pour prototyper** (et vérifier que l'idée plaît), 2 seulement si le prototype tient et qu'on veut le geste « machine Create » complet.

### 2.5 L'état réel des scripts KubeJS prévus

| # | Script | Faisable en KubeJS 2101 ? | Événement ou outil exact | Correction nécessaire |
|---|---|---|---|---|
| 1 | `fallow:init` + `load.json` | Oui (datapack) | tag `minecraft:load` | Retirer `doLimitedCrafting` si l'option B est choisie |
| 2 | `no_end_no_nether.js` | Oui | `ServerEvents.recipes`, `BlockEvents.rightClicked`, `NativeEvents.onEvent` | Passer `Java.loadClass('net.neoforged.neoforge.event.level.BlockEvent$PortalSpawnEvent')` si la chaîne n'est pas acceptée (le paramètre est une `Class`) |
| 3 | `spawns_safety.js` | Oui | `EntityEvents.checkSpawn` (plus tôt et moins cher) ou `EntityEvents.spawned` | — |
| 4 | `saisons_cold_sweat.js` | Hors de mon périmètre | — | voir le rapport sur la survie |
| 5 | `recipes/remplacements.js` | Oui | `event.recipes.create.mixing/splashing` (KubeJS Create) | — |
| 6 | `cuisine_unifiee.js` | Oui | `ServerEvents.recipes`, `ServerEvents.tags` | — |
| 7 | `objets_fallow.js` + `overgeared_pieces.js` | Oui | `StartupEvents.registry('item')` ; type `overgeared:forging` (champs `blueprint`, `requires_blueprint`, `hammering`, `minimum_quality`…, `doc.txt` d'Overgeared) | Utiliser `event.custom({type:'overgeared:forging', …})` |
| 8 | `recipes/progression.js` | Oui | `replaceInput`, `shaped`, `create.sequenced_assembly` | Le moteur à vapeur prend `#c:plates/gold` (jar) : le remplacement par l'acier est correct |
| 9 | `plans_ingenieur.js` + `loot/butin.js` | **Option B : oui ; option A : avec fuites** | `shaped(...).keepIngredient(...)` ; `LootJS.modifiers` + `addTableModifier('nomansland:chests/…').randomChance(x).addLoot(...)` | Réécrire `butin.js` en syntaxe LootJS 3 ; viser les tables de NML (2.3) |
| 10 | `survivants.js` | Oui | `NativeEvents.onEvent(BabyEntitySpawnEvent)` ; **`MoreJS.villagerTrades`** | Renommer `MoreJSEvents` en `MoreJS` |
| 11 | Pools de villages | Datapack | — | — |
| 12 | `gisements_coe.js` | Oui | `event.recipes.createoreexcavation.vein/drilling` | **D'abord supprimer les 15 gisements par défaut** ; `.drill(...)` attend un objet du tag `#createoreexcavation:drills` |
| 14 | `visionneuse.js` | Oui, **avec EMI** | `RecipeViewerEvents.removeEntriesCompletely('item', …)` et `groupEntries` (KubeJS a son plugin EMI) | Y ajouter les doublons de 2.2, les bois de Quark, les voies Quark et du Nether de Steam 'n' Rails, les trophées |
| 17 | `clairieres.js` | Oui (brouillon cohérent) | `BlockEvents.placed`, `server.scheduleInTicks`, `/fillbiome` | Le Dissipateur passe par 2.4 |
| 18 | `zoo.js` | **Oui** | `Java.loadClass('de.coldfang.wildex.integration.kubejs.WildexKubeJsScriptBridge').onDiscoveryChanged(p => { if (p.discovered >= 25) … })` (jar Wildex) | Vérifier en jeu que le rappel arrive côté serveur (le paramètre porte `playerUuid`) |
| nouveau | `bois.js` | Oui | `create.cutting` (2.1) | — |
| nouveau | `tags_engins.js` | Oui | `ServerEvents.tags('block')` vers `sable:*`, `create:non_movable`, `simulated:non_movable` | — |
| nouveau | `doublons.js` | Oui | `ServerEvents.recipes` (`remove`) + `RecipeViewerEvents` | Liste de 2.2 |

**Mods custom qui valent le coup** : la liste est au 4.

### 2.6 EMI et l'index

- Grouper par famille avec `RecipeViewerEvents.groupEntries` : tous les toits Macaw, toutes les fenêtres, tous les meubles d'une essence, toutes les tôles colorées (« locometal ») de Steam 'n' Rails, qui font 17 couleurs × environ 20 formes.
- Tout ce qui est enregistré mais inutile (bois de Quark, voies Quark et du Nether, trophées, tuf de Twigs, modules Quark coupés) passe par `removeEntriesCompletely`. **Il faut vérifier si EMI montre les objets des modules Quark coupés** : Zeta les retire des onglets créatifs, mais EMI peut aussi lister le registre (section 5).

---

## 3. Conflits et risques

1. **Leaves Be Gone est à la fois ajouté (11.3) et sur la liste noire d'Aeronautics** (06 §6.1, recherche technique A4, ticket #866, pas relu faute d'accès à GitHub). Il est en plus inutile, puisque FallingTree casse déjà les feuilles. **À retirer du 11.3.**
2. **Le sac de Traveler's Backpack**
   - Sa grille d'artisanat contourne `doLimitedCrafting` (1.8).
   - `sendBackpackCoordinatesMessage` affiche **des coordonnées à la mort** : contraire à la règle « jamais de coordonnées ». À mettre à `false`.
   - `backpackDeathPlace` pose le sac comme un bloc, à côté de la tombe GraveStone : deux objets à récupérer, peut-être en l'air (point 3). Décider qui garde le sac : TB (`backpackDeathPlace=true`), ou GraveStone si le sac va dans l'emplacement Curios (`backSlotIntegration`). À tester.
   - `spawnEntitiesWithBackpack` et `enableVillagerTrade` : à couper ou à cantonner aux survivants.
3. **Mourir à bord d'un dirigeable** : GraveStone pose la tombe à la position de la mort, dans le monde réel, et ne descend jamais (1.8). Les blocs d'un engin Sable vivent ailleurs, dans un « sous-niveau ». La tombe flotte donc dans le ciel, là où le joueur est mort (déduit), et peut se trouver sur la route d'un engin. Il faut un test. Correctif possible : petit mod (4.4).
4. **Coffres Lootr sur un engin** : Lootr n'a aucune règle pour Create ou Sable (seul `MixinSchematicPrinter` touche Create). Assembler un coffre de ruine dans un engin déplace un block entity aux inventaires par joueur (risque de duplication ou de perte, déduit). Mettre `lootr:lootr_chest`, `lootr_barrel`, `lootr_trapped_chest`, `lootr_inventory` et `lootr_shulker` dans `create:non_movable` et `simulated:non_movable` (NML se sert déjà de ce dernier tag).
5. **Entity Culling** : ne pas le retirer avant de refaire le test avec 1.11.3 (1.7). Avec Better Leaves, regarder aussi `solidLeaves`.
6. **Recettes en double** (2.2) : sans action, le joueur obtient l'un ou l'autre bloc au hasard du chargement des recettes.
7. **Every Compat** génère environ 150 blocs pour trois bois Quark qui n'existent pas dans le monde. Sans liste noire, ils restent pour toujours dans le registre du monde. Les retirer ensuite laisse des entrées « manquantes » au chargement (sans casser le monde, mais à faire **avant** le premier vrai monde).
8. **Easy Anvils et Overgeared** touchent tous deux l'enclume vanilla (`AnvilBlockMixin` d'un côté, `iron_anvil_bases` de l'autre) : à tester (section 5).
9. **Easy Magic et MoreJS** modifient tous deux la table d'enchantement (`EnchantmentMenu` et les événements `enchantmentTable*` de MoreJS). Pas de conflit tant qu'on n'écrit pas de script MoreJS sur l'enchantement (déduit).
10. **Versions bêta** : Steam 'n' Rails 0.3.0-beta.2, KubeJS Create 2101.3.1-build.18 (pas mis à jour depuis 2025-11), Simple Backups 4.0.30. À figer sur un monde permanent, en ne montant de version qu'après sauvegarde et test.
11. **Supplementaries passe de 3.9.9 à 4.0.2** (version majeure). Les clés de la configuration du dépôt existent toujours, mais il faut relire la configuration générée au premier lancement.
12. **Les recettes de scie de NML ne se chargent pas** (1.6). À signaler à l'auteur de NML ; en attendant, `bois.js`.

---

## 4. À adapter, supprimer ou ajouter dans le plan

### 4.1 Mod custom `fallow-dissipateur` (seulement si le prototype KubeJS de 2.4 convainc)
- **Rôle** : un vrai bloc cinétique Create. Il consomme du stress (par exemple 16 su/rpm, réglable), affiche son état aux lunettes d'ingénieur, a un Ponder, et lance la conversion du rayon de 64 en Java (même méthode que `/fillbiome`, qui renvoie déjà aux clients le paquet de mise à jour des biomes).
- **Taille** : 8 à 12 classes.
  - Bloc (sous-classe de `KineticBlock`/`DirectionalKineticBlock` de Create), block entity (`KineticBlockEntity`) et objet.
  - Enregistrement de l'impact en stress ; rendu (arbre de transmission + modèle) ; scène Ponder optionnelle.
  - Service de conversion découpé par ticks ; configuration ; données (recette, butin, tags).
- Dépendance : Create 6.0.10 (API Registrate). Le plus simple est de partir du modèle d'addon Create pour NeoForge 1.21.1.

### 4.2 Mod custom `fallow-plans` (seulement si l'équipe veut un savoir personnel)
- **Rôle** : savoir par joueur, sans fuite.
  - Attachement joueur : ensemble des plans lus.
  - Objet plan avec un composant « identifiant du plan ».
  - Fichiers `data/fallow/plans/*.json` : plan → recettes.
  - Mixin sur `ResultContainer#setRecipeUsed(Level, ServerPlayer, RecipeHolder)` et sur la grille du sac TB.
  - Petit plugin EMI qui affiche « Plan requis : … » sur la recette ; commande d'administration.
- **Taille** : 7 à 10 classes.

### 4.3 Mod custom `fallow-voies` (optionnel, plus tard)
- **Rôle** : voies Steam 'n' Rails en érable, pin, noyer et saule, à travers l'API `TrackMaterial` de Create, avec les textures de NML.
- **Taille** : 3 à 5 classes, plus les modèles et textures.
- Fait du réseau ferré un vrai prolongement du bois de la région.

### 4.4 Mini-mod `fallow-tombe` (si le test 5.3 échoue)
- **Rôle** : mixin sur `GraveUtils.getGraveStoneLocation` pour descendre jusqu'au premier sol solide quand la mort a lieu en l'air (dirigeable, chute).
- **Taille** : 2 classes.
- Alternative sans code : `strict_placement` ne suffit pas, puisque la recherche ne descend jamais.

> Les quatre peuvent vivre dans **un seul mod `fallow`**, dont 4.1 serait le cœur. C'est plus simple à maintenir qu'un mod par fonction.

### 4.5 Modifications du plan

| Où | Changement | Raison |
|---|---|---|
| 11.3 | **Retirer Leaves Be Gone** | Liste noire d'Aeronautics et doublon de `leavesBreaking` de FallingTree |
| 10.6 / 10.7 | Every Compat : liste noire des 3 bois de Quark et du module `twigs` | Environ 165 blocs inutiles évités |
| 10.7 « L'arbre » | Les haches d'Overgeared n'ont besoin d'aucun tag ; ajouter « scierie : `bois.js` » | Jar FallingTree ; recettes de NML mal placées |
| 10.12 #6 | FallingTree : `FALL_BLOCK`, `maxSize` 200–250, `leavesBreaking=true` | Défaut 100 + `ABORT` trop bas pour NML |
| 0005 (Quark) | Couper `Thatch`, `Variant Bookshelves`, **`Automatic Recipe Unlock`** ; optionnel : `Shingles` | Doublons prouvés ; plans |
| 0006 (Supplementaries) | Couper `sconce` et `gravel_bricks` | Doublons prouvés avec NML et Twigs |
| Twigs (nouveau) | Neutraliser `add_pebbles`, `add_land_sea_shells`, `add_water_sea_shells` ; décider de `add_ore_rhyolite/schist/bloodstone` | Doublon du décor de NML ; worldgen non réversible |
| 07 #1, #9 / 08.4 | Plans : option B (plan gardé sur l'établi) au lieu de `doLimitedCrafting` | Fuites prouvées (succès-recettes de Create, sac TB, assembleurs) |
| 07 #10 | `MoreJS.villagerTrades` | Nom du groupe lu dans le jar |
| 07 #12 | Supprimer les gisements par défaut de COE | 15 gisements infinis par défaut |
| 07 #18 | Écrire `zoo.js` avec `WildexKubeJsScriptBridge` | API lue dans le jar |
| 06 §6.1 | Refaire le test d'Entity Culling avec 1.11.3 avant tout retrait | Correctif sur les block entities sous NeoForge depuis 1.11.0 |
| Config TB | `sendBackpackCoordinatesMessage=false`, `enableCraftingUpgrade=false` (ou option B des plans), `spawnEntitiesWithBackpack=false` | Pas de coordonnées ; plans ; pas de monstres équipés |
| Tags (nouveau) | `tags_engins.js` : masses Sable du mobilier et des toits ; Lootr non déplaçable | Engins réalistes ; pas de duplication |
| Versions | Monter Sable en 2.0.6, Quark en 4.1-487, Moonlight en 3.7.2 ; Supplementaries 4.0.2 seulement après relecture de sa configuration ; NML 1.6.1 (à voir avec le rapport sur le monde) | Modrinth |
| Bibliothèques | Ajouter Puzzles Lib, Balm (client), Configurable | Dépendances requises |

---

## 5. Questions qui demandent un test en jeu

1. **Every Compat** : `/kubejs dump registry` ou recherche `@everycomp` dans EMI. Combien de blocs pour NML ? Les feuillages « automne » et « fruits » ont-ils leurs haies ? Les bois de Quark disparaissent-ils avec la liste noire ?
2. **EMI** montre-t-il les objets des modules Quark coupés, et les voies Quark ou du Nether de Steam 'n' Rails ?
3. **Mort à bord** d'un dirigeable en vol : où vont la tombe et le sac TB ? La tombe bloque-t-elle la route d'un engin ?
4. **Entity Culling 1.11.3** avec un dirigeable assemblé portant des block entities (coffre, Handcrafted, Supplementaries) : bug toujours là ? Sinon, `skipBlockEntityCulling` ou `blockEntityWhitelist` suffisent-ils ?
5. **Lootr** : un coffre de ruine dans `create:non_movable` refuse-t-il bien l'assemblage Sable ? Les pots de NML (`ancient_pot`, pots d'alchimiste) passent-ils par Lootr ? S'ils ont un block entity à eux, ce n'est sans doute pas le cas, et un plan y serait « premier arrivé, premier servi ».
6. **Scie Create** : un bloc de bûche de NML est-il traité avant `bois.js` ? Cela confirmerait ou non que les recettes de NML ne se chargent pas.
7. **FallingTree** : le plus grand arbre de NML avec `maxSize` 250 et `FALL_BLOCK`. Mesurer le coût avec spark : le nombre d'entités de blocs qui tombent et les TPS.
8. **Easy Anvils et Overgeared** : transformer une enclume vanilla qui porte des objets (Easy Anvils) en enclume de forge Overgeared. Les objets sont-ils perdus ?
9. **Plans d'Overgeared (option C)** : l'écran de l'« Empty Blueprint » laisse-t-il choisir un type personnalisé (`rail`…) ? Si oui, l'option C ne garde pas le secret.
10. **Dissipateur prototype** : `getSpeed()` du ventilateur voisin se lit-il depuis `BlockEvents.blockEntityTick` ? La conversion de 64 blocs passe-t-elle sans à-coup de TPS ?
11. **Masses Sable** : un dirigeable meublé (Handcrafted, toits Macaw) vole-t-il encore avec les tags `light` et `half_volume` ? Et sans ces tags ?
12. **Le sac de couchage de TB à bord** d'un engin en mouvement : sommeil, point de réapparition (`enableSleepingBagSpawnPoint`), téléportation au réveil.
13. **Mémoire** : spark `heapsummary` ou F3 avec et sans Macaw's Roofs (605 blocs). C'est le plus gros poste de la v2 ; le garder seulement s'il ne coûte pas trop.
