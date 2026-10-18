# 4 — Monde, faune et objectifs longs (vérification du 2026-10-18)

Périmètre : No Man's Land (NML), Sparse Structures, Lithostitched, Biolith, Ancient Remnants: Monoliths (AR) + Fragmentum, Wildex Bestiary, Exposure, Antique Atlas / AA4 Atlas / Surveyor, Lootr, Enhanced Celestials, In Control!, toute la faune et les compagnons, et le transport des animaux par Create, Steam 'n' Rails et Create Aeronautics / Sable.

Méthode : jars téléchargés depuis Modrinth et inspectés (JSON de `data/` et `assets/`, `javap` sur les classes). Le pack est lu dans `origin/main` du dépôt, les brouillons dans `plan/kubejs-brouillons/`. Rien n'a été modifié dans le dépôt.

Notation des preuves :
- **[jar X v]** : lu dans le jar de cette version ;
- **[Modrinth]** : version vérifiée par l'API ;
- **[pack]** : `origin/main` ;
- **[plan]** : chapitres du plan ;
- **[déduit]** : raisonnement, pas encore vérifié en jeu.

Décision tenue : **la faune et les compagnons restent tous**. Ce rapport ne propose aucun retrait de mod, seulement des réglages.

---

## 0. Résumé en dix points

1. **Les monuments d'AR ne lisent pas de tag fourni par un datapack.** Leurs biomes viennent de la config `config/obscuria/ancient_remnants-common.toml`, injectée au chargement dans un tag vide. **Sparse Structures ne les espace pas**, faute de clé `spacing` dans leur JSON. On règle donc tout dans la config d'AR.
2. **Les gardiens des monuments sont des spawners vanilla de `minecraft:breeze`**, et la breeze figure dans `global.FALLOW.bannedVanilla`. Tels qu'ils sont écrits, les brouillons vident donc les monuments de leurs gardiens. Il faut trancher.
3. **L'Elderheart est une entité, pas un bloc.** Il suffit d'approcher à 16 blocs : la bénédiction est donnée toutes les 100 ticks. Aucun événement n'est émis. La clairière doit donc passer par un polling KubeJS.
4. **Bénédictions :** une seule à la fois, permanente, conservée à la mort, remplacée par la suivante. Celle du monolithe guerrier est presque nulle dans Fallow, à cause du peu de monstres.
5. **Les fragments de codex ne tombent presque jamais.** On en obtient 0,2 % par mob tué par un joueur, et la table « avant-poste » n'est reliée à rien. **Le « marchand ambulant » du §11.8 n'existe pas.** Il faut injecter les fragments nous-mêmes, dans les butins de NML (Lootr compris).
6. **Clairières par `/fillbiome` : faisables.** Corrections nécessaires :
   - colonnes alignées sur 4 et pleine hauteur ;
   - trois variantes de température, pour Ecliptic ;
   - tag filtre complété avec les biomes NML ;
   - spawns réglés par tags Naturalist et par In Control.
7. **Zoo :** aucun filet n'est nécessaire.
   - Moyens : laisse ; bateau pour les petits ; **siège Create sur train ou dirigeable Sable** (sans limite de taille) ; charrette d'Icy's 2.1.4.
   - Wildex ne connaît pas les enclos. Il faut une « plaque d'enclos » en KubeJS, avec le pont Wildex.
8. **Wildex :**
   - Sans liste d'exclusions, la complétion est impossible : mobs du Nether et de l'End, monstres vanilla bannis, mobs coupés en config.
   - L'Analyzer est un raccourci à couper.
   - Le Spyglass Pulse n'a pas d'interrupteur.
9. **Hybrid Aquatic ajoute neuf biomes par Lithostitched**, dont une `tropical_river` qui remplace `minecraft:river`. C'est contraire au critère de 0003 (« ajoutent des biomes : conflit NML »). On les coupe par sa config, pas en retirant le mod.
10. **Doublons de faune :** une douzaine d'espèces existent deux ou trois fois (cerf, élan, oie, tortue, tigre, requins, crabes, etc.). On les règle par config ou biome modifier, sans retirer de mod. Wildex en tient compte.

---

## 1. Ce qui est confirmé

### 1.1 Versions et présence dans le pack

Mods présents dans `pack/mods/` [pack], comparés à la dernière version NeoForge 1.21.1 sur Modrinth [Modrinth] :

| Mod | Pack | Modrinth | Remarque |
|---|---|---|---|
| No Man's Land | 1.5.12 | **1.6.1** (2026-10-14) | analysé sur 1.6.1 ; les entités existent déjà en 1.5.12 |
| Friends & Foes | 4.0.27 | 5.0.2 | mêmes clés de config |
| Icy's Better Horses | 2.0.1 | **2.1.4** | **charrettes qui ramassent les mobs** (utile au zoo) |
| Naturalist | 2.0.4 | 2.0.5 | |
| Alex's Mobs Continued | 2.2.2 | 2.2.3 | |
| Hybrid Aquatic | 1.7.3 | 1.7.4 | |
| Companions! | 1.3.4 | 1.3.6 | |
| Sable | 2.0.5 | 2.0.6 | |
| Ecliptic Seasons | 0.15.0-rc-3-1 | 0.15.4.1 | |
| Lithostitched | 1.8.0 | 1.8.1 | |
| Antique Atlas 4 | fichier `antique-atlas-3.1.2+1.21.jar`, `side = "client"` | | Fabric via Connector |
| Surveyor | 1.2.4, `side = "both"` | | |

**Prévus au plan mais absents du pack** [pack + Modrinth] : Lootr 1.11.38.127, In Control! 10.3.0 (bêta), Exposure 1.9.19, Wildex 3.0.0, Ancient Remnants 1.5.2 + Fragmentum 5.4.0, Enhanced Celestials 6.0.2.6, AA4 Atlas 1.1.2, LootJS 3.7.0. KubeJS est en 2101.7.2.

### 1.2 No Man's Land 1.6.1 [jar NML 1.6.1]

- **Bibliothèques incluses (jarjar)** : Biolith 3.0.11, mixed-litter 1.2.2, sable-companion. TerraBlender est marqué « discouraged ».
- **Placement :** Biolith fait des `replaceOverworld` partiels. **Les biomes vanilla restent**, y compris cerisaie, badlands, ice_spikes et deep_frozen_ocean, ceux des monuments AR par défaut.
- **Les 24 biomes :**
  - alchemist_ruins (souterrain, sans spawns), autumnal_forest, bayou, blackwater_river, bog, boreal_forest
  - cave_depths, caves, dark_swamp, dark_taiga, desert_river
  - dream (biome noir, hors `#is_overworld`)
  - frozen_shore, frozen_woods, lavender_field, lush_river, maple_forest, maple_grove, mud_beach
  - old_growth_forest, old_growth_forest_clearing, old_growth_forest_edge, prairie, tropical_beach
- **Tags fournis par NML :**

  | Tag | Biomes NML |
  |---|---|
  | `minecraft:is_forest` | autumnal_forest, dark_swamp, boreal_forest, maple_forest, maple_grove, `#nomansland:old_growth_forest` |
  | `minecraft:is_taiga` | dark_taiga, frozen_woods |
  | `minecraft:is_river` | les 3 rivières NML |
  | `c:is_swamp` | bayou, bog, dark_swamp, blackwater_river |
  | `c:is_plains` | prairie, lavender_field |
  | `c:is_floral` | lavender_field |
  | `c:is_cold/overworld` | bog, dark_taiga, maple_grove, frozen_woods, frozen_shore |
  | `c:is_hot/overworld` | bayou, prairie, lush_river, desert_river, tropical_beach |
  | `c:is_spooky` | dark_swamp, dark_taiga |
  | `nomansland:has_dense_fog` | `#c:is_swamp`, jungle, bamboo_jungle, mushroom_fields, frozen_woods |

  Il existe aussi `c:is_rare`, `nomansland:caves` et `nomansland:old_growth_forest`.
- **Structures** (structure sets, espacement en chunks) :

  | Set | Espacement / séparation |
  |---|---|
  | alchemist_ruins | 50/32 |
  | bell_sanctuaries (placement custom) | 42/16 |
  | creature_monuments (drowned, creeper, sniffer) | 50/28 |
  | deep_wells | 46/18 |
  | menhirs (grand, menhir, lone) | 46/18 |
  | desert_ruins, desert_wells | 16/5 |
  | small_dungeons | 10/4 |

  meeting_points a un placement custom réglé par la config NML (distance min et max). forest_temples et underground_shelters sont désactivés.
- **Butins :**
  - coffres :
    - sniffer_monument → `nomansland:chests/sniffer_monument` ;
    - deep_well → `nomansland:chests/water_well` ;
    - small_dungeon → `small_dungeon_core`, `minecraft:chests/simple_dungeon`, `redstone_parts`, `dungeon_garbage`, `dungeon_kitchen`, plus un dispenser ;
  - gravier suspect par processeur `append_loot` : `nomansland:archaeology/alchemist_ruins` (ruines et sanctuaires à cloche) et `nomansland:archaeology/well` ;
  - **jarres d'alchimiste** (blocs custom, `pot_patch`) : tables `chests/alchemist_pot_*` et `ancient_pot_cave`. La classe `RegeneratingPotsData` les regarnit ; aucune clé de config ne règle cette régénération.
  - GLM NML : warding_effigy et hardtack ajoutés à des coffres vanilla.
- **Les petits donjons NML contiennent des spawners vanilla d'araignée, de squelette et de zombie** [JSON des templates].
- **Entités NML :** billhook_bass, buddy, buried, cave_carp, deer, frienderman, goose, living_pot, living_urn, moose, tortoise.
  - Le cerf apparaît en `#c:is_forest` (poids 8) et en `#c:is_taiga` (poids 10).
  - L'oie apparaît sur les rivières et dans les marais ; la tortue en jungle, badlands, plaines…
  - Je n'ai trouvé **aucun biome modifier de spawn pour l'élan** : le dossier est vide. À voir en jeu.
- **Brouillard propre à NML** (NMLConfig, catégorie `fog_modifiers`) : `fogModifiers`, `foggyBiomeFogModifier`, `cavesBiomeFogModifier`, `deepDarkFogModifier`, `surfaceFogModifier`, `surfaceFogIntensity`.
- **NML a sa propre « Friend Moon »** : une lune qui parle, des rêves, le Meeting Point, la clé `friendMoonDimensions`. Elle est distincte d'Enhanced Celestials.

### 1.3 Ancient Remnants 1.5.2 [jar AR 1.5.2 + Fragmentum 5.4.0]

**Biomes et espacement**

- Config : `config/obscuria/ancient_remnants-common.toml` (classe `CommonConfig`). Par monument : `<id>_placement_spacing = 100`, `<id>_placement_separation = 20`, et une liste de biomes.
- Biomes par défaut :

  | Monument | Biomes par défaut | Bénédiction (tag `ancient_remnants:blessing/<id>`) |
  |---|---|---|
  | hunter_monolith | `minecraft:badlands` | hunter_instincts |
  | warrior_monolith | `minecraft:ice_spikes` | warrior_wrath |
  | sentinel_monolith | `minecraft:deep_frozen_ocean` | sentinel_aegis |
  | cherry_monument | `minecraft:cherry_grove` | desperate_vitality |
  | oakwood_monument | `#minecraft:is_forest` | bountiful_harvest |
  | coniferous_monument | `#minecraft:is_taiga` | architect_reach |

- **Le tag `ancient_remnants:has_structure/<id>` est vide dans le jar.** Commentaire du fichier : `"Default values are added programmatically from config"`. `TagPostProcessors.register` (Fragmentum) l'alimente depuis la config. Conséquences :
  - un datapack peut **ajouter** des biomes à ce tag ;
  - pour **retirer** un biome par défaut, il faut passer par la config (`replace: true` est sans effet sur un ajout fait après coup ; déduit).
- **Placement :** type `ancient_remnants:configurable_random_spread` (sous-classe de `RandomSpreadStructurePlacement`), avec `"spacing_config": "@OAKWOOD_MONUMENT_PLACEMENT_SPACING"` et **sans clé `spacing`**. Le mixin `MakeStructuresSparse` de Sparse Structures ne réécrit que les clés JSON `spacing` et `separation` lorsqu'elles existent. **Sparse Structures ne touche donc pas AR** [jar Sparse Structures + JSON AR].

**Structures et gardiens**

- Structures jigsaw de taille 7. Les monolithes partent de `start_height` 20 ; les monuments partent de `absolute -24`, projeté sur `WORLD_SURFACE_WG`. `max_distance_from_center` 128, `spawn_overrides` vide.
- **Gardiens** : spawners vanilla de `minecraft:breeze`, dans chaque monument sauf le monolithe sentinelle. Réglages : `MinSpawnDelay 200`, `MaxSpawnDelay 800`, `SpawnCount 4`, `MaxNearbyEntities 6`, `RequiredPlayerRange 16`.
- Les `processor_list` `monolith.json` et `monument.json` ne font qu'un `block_ignore` (air, structure_block). Un datapack peut les surcharger au même chemin.

**L'Elderheart**

- C'est une **entité** (`ancient_remnants:elderheart`) invulnérable : `hurt` renvoie false.
- NBT `PossibleBlessings` = `ancient_remnants:<monument>`, c'est-à-dire le tag de bénédictions.
- Toutes les 100 ticks, le joueur le plus proche dans un rayon de **16 blocs** reçoit la bénédiction : titre « Blessing Acquired », puis `setBlessing`. **Aucun événement NeoForge n'est émis**, et il n'y a pas d'objet à utiliser.
- Son ambiant audible jusqu'à 256 blocs.

**Bénédictions**

- Une seule à la fois, stockée dans le NBT du joueur (`AncientRemnantsBlessing`).
- **Permanente et conservée à la mort** (`onRestore`). Une autre bénédiction la remplace.
- L'effet est réappliqué à chaque tick pour 62 ticks.
- Commande op : `/ancient_remnants blessing set|remove <joueur>`.

| Bénédiction | Effet exact (bytecode) |
|---|---|
| hunter_instincts | dégâts indirects (projectiles) × (1 + easeInCirc(distance/64)) : jusqu'à ×2 à 64 blocs |
| warrior_wrath | dégâts de mêlée × (1 + easeInCirc(nombre de `Monster` dans 12 blocs / 12)) |
| sentinel_aegis | armure + 20 × easeInCirc(1 − PV/PVmax) |
| desperate_vitality | sous 33 % de PV : Régénération pendant 200 ticks |
| architect_reach | +3 `BLOCK_INTERACTION_RANGE` |
| bountiful_harvest | 50 % de récolte doublée sur les cultures mûres (`CropBlock` à l'âge max, ou âge 3) |

**Codex et fragments**

- L'objet fragment est `ancient_remnants:research_scrap`, avec le composant `ancient_remnants:stored_research` : une **liste** d'ids de pages.
- Les pages forment un registre de datapack : `data/<ns>/ancient_remnants/research/<id>.json`, avec `biomes` (un tag), `texture` (350×210), `header`, `content` et `right_page`. Les six pages d'origine pointent sur `ancient_remnants:has_structure/<id>`.
- Recettes : `monolith_codex` (livre + améthyste + deepslate), `monolith_codex_restoration` (codex + fragment), `paper_from_research_scrap` (fragment → 3 papiers).
- **Seule source active de fragments : `ResearchSystem.dropExtraDeathLoot`.**
  - Un mob tué par un joueur lâche un fragment avec **0,2 %** de chance (`nextFloat > 0.002 → return`).
  - La page tirée est l'une de celles dont le tag `biomes` contient le biome du lieu.
- **La table `ancient_remnants:loot_table/extensions/pillager_outpost.json` (50 % de `random_research_scrap`) n'est reliée à rien.** Aucun global loot modifier n'existe dans AR ni dans Fragmentum (grep sur `loot_modifiers`).
- La table `ancient_remnants:random_research_scrap` tire **une page au hasard parmi les six**. Elle peut servir telle quelle.
- **Aucun marchand ambulant n'existe dans AR.** L'affirmation du §11.8 (« fragments laissés par un marchand ambulant ») est fausse.

### 1.4 Wildex 3.0.0 [jar Wildex 3.0.0]

**Événements**

- Événements NeoForge :
  - `de.coldfang.wildex.api.event.WildexDiscoveryChangedEvent` : player, mobId, discoveredCount, totalCount, completionPercentScaled ;
  - `de.coldfang.wildex.api.event.WildexCompletedEvent`.
- **Pont KubeJS intégré** : `de.coldfang.wildex.integration.kubejs.WildexKubeJsScriptBridge`.
  - Méthodes `onDiscoveryChanged(cb)` et `onCompleted(cb)`. Noms internes : `wildex.discovery_changed` et `wildex.completed` ; `"*"` les écoute tous.
  - Champs reçus : apiVersion, playerUuid, playerName, mobId, discovered, total, percentScaled, isComplete.
  - `clearAll` est appelé à `ServerAboutToStart` et à chaque resync des datapacks : **il faut s'inscrire depuis `server_scripts`**, qui sont rechargés au même moment.
- API : `WildexApi.getDiscoveredCount`, `getTotalCount`, `getCompletionPercentScaled`, `isComplete`.
- Objectifs de scoreboard : `wildex_discovered`, `wildex_total`, `wildex_percent`, `wildex_complete`.

**Config commune**

| Clé | Défaut |
|---|---|
| hiddenMode | true |
| requireBookForKeybind | true |
| giveBookOnFirstJoin | true |
| spyglassDiscoveryChargeTicks | 28 |
| spyglassDiscoveryRange | ≈ 98, max 512 |
| newEntryRewardsXp | true |
| newEntryXpAmount | 5 |
| keepCompletionAfterNewMobs | false |
| shareOffersEnabled | true |
| shareOffersPaymentEnabled | true (émeraudes, max 64) |
| kubejsBridgeEnabled | true |
| exposureDiscoveryEnabled | true |
| debugMode | false |

Il existe aussi les listes `excludedModIds` (espace de noms ou id) et `excludedVariantMobIds`.

**Découverte et blocs**

- Sources de découverte : KILL, SPYGLASS, ANALYZER, DEBUG, plus Exposure et partage.
- **Exposure** : `onFrameAdded(FrameAddedEvent)` découvre chaque entité du cadre **à la prise de vue**, pas au développement.
- **Analyzer** (cuivre + or + livre) : on y dépose un objet lâché par un mob, on l'alimente en redstone, et il découvre une créature encore inconnue qui lâche cet objet.
- **Pedestal** (améthyste + basalte poli + livre) : il affiche un hologramme de la créature.
- **Spyglass Pulse** : débloqué à la complétion, **sans clé de config**. Clic gauche avec la longue-vue : rayon 32, recharge de 300 ticks, probablement un effet de lueur.
- **Aucune notion d'enclos ni de captivité.** Le total compte tous les types d'entités vivantes non exclus (déduit : il se calcule sur le registre, pas sur les spawns réels).

### 1.5 Exposure 1.9.19 [jar Exposure 1.9.19]

- Événements :
  - `io.github.mortuusars.exposure.neoforge.api.event.FrameAddedEvent` : `getEntitiesInFrame`, `getPositionsInFrame`, `getFrame`, `getCameraHolderEntity` ;
  - `ModifyFrameExtraDataEvent`.
- Les `extraData` de la Frame contiennent BIOME, **STRUCTURES** (liste d'ids), POSITION, DAY_TIME, WEATHER, IN_CAVE, UNDERWATER…
- Critère d'advancement `exposure:frame_exposed`, par exemple `{"frame":{"entities_in_frame":[{"type":"naturalist:bear"}]}}`. Le prédicat accepte aussi `extra_data`.
- Exposure ajoute ses propres GLM à des coffres vanilla.

### 1.6 Lootr 1.11.38.127 [jar Lootr]

- Convertit : coffres, tonneaux, coffres piégés, shulkers, **`minecraft:decorated_pot`**, **gravier et sable suspects**, cadres.
- **Les jarres custom de NML ne sont pas converties** : ce ne sont pas des decorated_pot.
- Config : `decay_*`, `refresh_*`, `loot_table_blacklist`, `disable_break`, `enable_break`, `blast_immune`, `randomise_seed`, `team_loot`, `perform_piecewise_check`… Tags de structures : blacklist, refresh, decay.
- Les global loot modifiers, donc LootJS et `neoforge:add_table`, s'appliquent aussi aux tirages faits par Lootr pour chaque joueur [déduit : Lootr appelle la table normalement avec le contexte du joueur].

### 1.7 Antique Atlas 3.1.2 (« Antique Atlas 4 »), AA4 Atlas 1.1.2, Surveyor 1.2.4 [jars]

- **Antique Atlas** lit les marqueurs de structure dans `assets/<ns>/atlas/structure/{start,type,tag,piece}/<id>.json`, sous la forme `{"markers":"antique_atlas:structure/…"}` ou `{"textures":…}`, et les tuiles de biome dans `assets/<ns>/atlas/biome/<biome>.json`.
  - Config client : `requireItem`, `structureMarkers` (Map), `graveStyle`, `emptyHandling`…
  - Ce sont des **assets** : il faut un resource pack, pas un datapack.
- **AA4 Atlas** : `environment "*"` (client et serveur), recette livre + boussole, `KeybindLockMixin`. Il fait de l'atlas un objet réel.
- **Surveyor** : `globalSharing`, `networking.positions`, `discoveryMessages`, `playerDeathWaypoints`, partage des structures et des repères. C'est lui qui fournit à Antique Atlas les structures découvertes.

### 1.8 In Control! 10.3.0 [jar]

Mots-clés présents :

- **lieu** : `structure`, `hasstructure`, `structuretags`, `biome`, `biometags` ;
- **origine du spawn** : `spawner`, `spawntype`, `kubejs` ;
- **comptage** : `mincount`, `maxcount` ;
- **autres** : `nodespawn`, `customname`…

### 1.9 Transport : Create 6.0.10, Sable 2.0.6, Icy's Better Horses 2.1.4 [jars]

**Siège Create**

- `SeatBlock.canBePickedUp` accepte **toute `LivingEntity`**, sauf Shulker, Player et les entités du tag `create:ignore_seat` (= `#c:capturing_not_supported`).
- Les hostiles seulement si `seatHostileMobs`. **Aucune limite de taille.**
- Un animal tenu en laisse peut être posé sur un siège par clic droit (`getLeashed`).

**Sable**

- Le tag `sable:retain_in_sub_level` contient `create:seat` et les contraptions.
- Les wagonnets sont dans `destroy_when_leaving_plot`.

**Icy's Better Horses 2.1.4**

- **Charrette** `horse_cart` qui ramasse des mobs.
  - Config `cart_pickup`, avec des filtres passif, neutre et hostile.
  - Petite charrette : 4 places ; grande, pour les chevaux de trait : 6.
- Sifflet qui téléporte le cheval s'il est loin, « Send Home », résurrection contre de l'XP.
- **Le registre des chevaux affiche des coordonnées.**

**Companion (Snownee)** : `follow.petForceTeleportingWhenChunkUnload`, `portalTeleportingPets`, `injuredStatus.*`.

### 1.10 Faune : contenu vérifié [jars]

**Naturalist 2.0.5**

- Espèces :
  - mammifères : bear, black_bear, boar, capybara, deer, elephant, giraffe, hedgehog, hippo, lion, mammoth, mole, rat, rhino, tiger, zebra ;
  - oiseaux : bird (bluejay, canary, cardinal, finch, robin, sparrow), duck, ostrich, turkey, vulture ;
  - reptiles : alligator, komodo, lizard, snake (coral, rattlesnake), tortoise ;
  - petites bêtes : ant, butterfly, caterpillar, desert_scorpion, jungle_scorpion, dragonfly, firefly, snail ;
  - vie aquatique : anglerfish, bass, blobfish, catfish, clam, crab, giant_isopod, great_white_shark, jellyfish, piranha, ray, starfish, whale.
- Config : `<mob>_removed`, `remove_all_bugs`. Spawns par tags `naturalist:has_<mob>` et biome modifier `naturalist:add_animals`.
- Tags de chasse : `naturalist:lion_hostiles` (**inclut `minecraft:horse`**), `tiger_hostiles`, `bear_hostiles`, `alligator_hostiles`, `deer_predators`…
- Seaux : anglerfish, bass, blobfish, catfish, duck, giant_isopod, jellyfish, piranha, ray, starfish.

**Alex's Mobs Continued 2.2.3** : grizzly_bear, tiger, elephant, rhinoceros, komodo_dragon, crocodile, caiman, moose, bison, cachalot_whale, orca, hammerhead_shark, frilled_shark, giant_squid, lobster, blobfish, catfish, rattlesnake, anaconda, blue_jay… Le **capybara n'est pas dans AM** : seul Naturalist en a un.

**Hybrid Aquatic 1.7.4**

- Environ 130 espèces aquatiques, **sans aucun seau**.
- **Ajoute des biomes par Lithostitched** (`replace_partially`) : coral_reef, deep_coral_reef, trenches, sulfuric_caves, tide_pools, seagrass_bed, red_meadow (sous-marin), deep_warm_ocean et **tropical_river, qui remplace `minecraft:river`**.
- Interrupteurs dans `BiomeConfig` : `enableBiomes`, `generateTropicalRiver`, `generateTidePools`, `generateSulfuricCave`, `generateRedMeadow`…

**Critters and Companions** : dragonfly, dumbo_octopus, ferret, jumping_spider, koi, ladybug, leaf_insect, otter, red_panda, roly_poly, sea_bunny, shima_enaga, snail, stag_beetle, stick_bug, weevil. La libellule et les espèces aquatiques ont `canBeLeashed` à false.

**Les autres mods de faune**

- **Friends & Foes** : barnacle, copper_golem, crab, glare, iceologer, illusioner, mauler, moobloom, penguin, rascal, tuff_golem, wildfire.
- **Mowzie's Mobs** : le sculptor ne peut pas être attaché. Structures : frostmaw, monasteries, umvuthana groves, wrought chambers.
- **Companions!** : beaucoup d'hostiles et de boss (sacred_pontiff, shade_maw, soul_mage, illager_golem…).
- **Ghost** : ghost, kodama, small_ghost, haunted boats.
- **Untitled Duck Mod** : duck, goose.
- **Adorable Hamster Pets** : hamster, hampter.

### 1.11 Ecliptic Seasons 0.15.4.1 [jar]

- Types `agro_climate` : temperate, cold, hot.
- Les tags `seasonal` et `agro/warm` du jar sont vides : la classification se fait dans le code, par la température du biome [déduit].
- `biome_rain_effect` ajoute du brouillard sous la pluie.

### 1.12 `/fillbiome` en 1.21.1 [déduit, code vanilla connu]

- Limite fixée par la gamerule `commandModificationBlockLimit` (32 768 par défaut), comptée sur le volume de la boîte.
- Les biomes vivent dans des cellules de 4×4×4 : la boîte est arrondie à ces cellules.
- Le filtre `replace` accepte un tag de biome.
- Le serveur renvoie les biomes modifiés aux clients (`ClientboundChunksBiomesPacket`). La brume d'ATMOSPHERICS, lue par biome côté client, change donc sans reconnexion.

### 1.13 KubeJS 2101.7.2 [jar]

`NativeEvents.onEvent(classe, rappel)` est lié pour tous les types de scripts (`BuiltinKubeJSPlugin`). On peut donc écouter `FrameAddedEvent` d'Exposure et les événements Wildex sans mod custom.

---

## 2. Liens concrets à construire

### 2.1 Ancient Remnants ↔ biomes NML (config)

Dans `config/obscuria/ancient_remnants-common.toml`, **remplacer** les listes par défaut. Recopier les noms exacts des clés depuis le fichier généré au premier lancement ; je n'ai relevé que les clés d'espacement mot pour mot.

| Monument | Biomes proposés | Raison |
|---|---|---|
| hunter_monolith | `#minecraft:is_badlands`, `minecraft:desert`, `nomansland:prairie`, `minecraft:savanna`, `minecraft:savanna_plateau` | Grands espaces ouverts : la bénédiction (portée des tirs) y a un sens |
| warrior_monolith | `minecraft:ice_spikes`, `minecraft:snowy_plains`, `minecraft:snowy_slopes`, `minecraft:grove` | Garde le thème du froid, en le rendant trouvable |
| sentinel_monolith | `minecraft:deep_frozen_ocean`, `minecraft:deep_cold_ocean` (+ `nomansland:frozen_shore` à tester) | Le monolithe est sous l'eau ; `frozen_shore` n'est intéressant que si la structure tient sur une plage |
| cherry_monument | `minecraft:cherry_grove`, `nomansland:lavender_field`, `minecraft:flower_forest`, `minecraft:meadow` | La cerisaie seule est trop rare avec NML |
| oakwood_monument | liste explicite : `minecraft:forest`, `minecraft:birch_forest`, `minecraft:flower_forest`, `nomansland:old_growth_forest`, `nomansland:old_growth_forest_edge`, `nomansland:maple_forest`, `nomansland:autumnal_forest` | `#minecraft:is_forest` inclurait `nomansland:dark_swamp` (marais, `c:is_spooky`) |
| coniferous_monument | `#minecraft:is_taiga`, `nomansland:boreal_forest`, `minecraft:grove` | `boreal_forest` est dans `is_forest` chez NML, pas dans `is_taiga` |

- Laisser `nomansland:old_growth_forest_clearing` aux clairières naturelles de NML [déduit].
- **Espacement :** passer de 100/20 à **64 à 80 / 16 à 24**, dans la config d'AR et non dans Sparse Structures (§1.3). Pour une carte « monde presque vide », 100 chunks (1 600 blocs) entre deux monuments du même type, c'est très loin. Il y a six types : à 72 chunks, on croise un monument d'un type donné tous les 1 150 blocs environ [déduit].
- **Effet secondaire utile :** les pages du codex lisent ce même tag (`"biomes": "ancient_remnants:has_structure/<id>"`). Les fragments tirés à la mort d'un mob suivront donc automatiquement les nouveaux biomes [JSON des pages].

### 2.2 Les gardiens (breezes) : trancher

Constat : `00_fallow_constants.js` bannit `minecraft:breeze`, et `spawns_safety.js` annule `EntityEvents.spawned` pour toute entité bannie dans l'Overworld. **Les spawners des monuments tourneront donc à vide.** Trois options :

- **A. Garder les breezes, seulement là** (recommandé si l'on veut un gardien) :
  - dans `spawns_safety.js`, laisser passer la breeze quand elle sort d'un spawner (`event.entity.persistentData` n'est pas utilisable ici ; tester `entity.spawnType === 'SPAWNER'`, ou une proximité d'Elderheart à 32 blocs) [déduit] ;
  - côté In Control, règle `{"mob":"minecraft:breeze","spawner":true,"structure":"ancient_remnants:…","result":"allow"}` placée avant les refus.
  - Une breeze est un mob du Trial Chamber (vent, saut) : 4 à la fois, c'est un vrai combat en début de partie.
- **B. Remplacer le spawner** : surcharger `data/ancient_remnants/worldgen/processor_list/monument.json` (et `monolith.json`) en ajoutant un processeur `minecraft:rule`, `output_state` spawner avec `block_entity_modifier` `minecraft:append_static`, pour mettre un autre mob en `SpawnData`. Candidats :
  - un gardien thématique de Mowzie's ou de Companions! ;
  - plus simplement, **aucun gardien**, en remplaçant le spawner par un bloc décoratif.

  Prévoir un test, car `append_static` fusionne et ne remplace pas (déduit).
- **C. Aucun gardien** : on accepte le spawner vide (il ne coûte presque rien) ou on le retire avec un `block_ignore` de `minecraft:spawner` dans la processor_list. C'est le plus fidèle au « monde presque vide, sans horreur ».

Ma recommandation est **C**, ou **B** avec un mob discret. Une breeze vanilla tranche avec la direction artistique et contredit la liste d'exclusion.

### 2.3 Clairière déclenchée par l'Elderheart (KubeJS)

L'Elderheart n'émet aucun événement (§1.3) : on le détecte par polling.

```js
// server_scripts/clairieres_elderheart.js — esquisse
ServerEvents.tick(event => {
  if (event.server.tickCount % 100 !== 0) return
  event.server.players.forEach(p => {
    if (String(p.level.dimension) !== 'minecraft:overworld') return
    const box = p.boundingBox.inflate(16)
    p.level.getEntitiesWithin(box).forEach(e => {
      if (String(e.type) !== 'ancient_remnants:elderheart') return
      if (e.tags.contains('fallow_eveille')) return
      e.addTag('fallow_eveille')                    // une seule fois par monument
      global.fallowOuvrirClairiere(p.level, e.blockPosition(), 32, p)
    })
  })
})
```

- `global.fallowOuvrirClairiere` reprend la boucle de `clairieres.js`, à factoriser.
- Le tag d'entité est sauvegardé avec l'entité : la clairière ne s'ouvre qu'une fois [déduit].
- Rayon 32 : même palier que la lanterne. Message propre : « La brume se retire autour du monument. »
- Coût : une recherche d'entités dans une boîte de 33 blocs par joueur, toutes les 5 s. C'est négligeable.

### 2.4 Clairières : réécrire le brouillon `clairieres.js`

**Alignement et hauteur**

- Arrondir le centre et les colonnes aux multiples de 4 : `x0 = Math.floor(c.x/4)*4`.
- Passer à des **colonnes de 8×8 sur toute la hauteur utile** : de `c.y − 16` jusqu'à 319. Une colonne de 8 × 336 × 8 fait 21 504 blocs, sous la limite de 32 768.
- Raison : avec le brouillon actuel (−24/+40), la brume revient pour un joueur monté sur une colline de 50 blocs au-dessus du feu, puisque le brouillard dépend du biome à la position de la caméra [déduit].
- Le filtre `replace` protège les biomes souterrains, qui portent d'autres ids.

**Trois variantes de biome, pour Ecliptic et la neige**

| Biome | Température | Remplace |
|---|---|---|
| `fallow:clairiere_froide` | ≈ 0.0 | `#fallow:clairiere_source_froide` = `#c:is_cold/overworld` + `#minecraft:is_taiga` froids (snowy_taiga, grove…) |
| `fallow:clairiere` | 0.7 | `#fallow:peut_devenir_clairiere` |
| `fallow:clairiere_chaude` | ≈ 0.95, `has_precipitation` true | `#fallow:clairiere_source_chaude` = `#c:is_hot/overworld` terrestres |

- Ordre des passes pour chaque colonne : froide, puis chaude, puis tempérée. Chaque passe ne touche que son filtre ; on passe donc de 4 à 12 commandes par tick (déduit, coût faible).
- Sans ces variantes, une clairière dans la taïga enneigée cesse d'avoir de la neige en hiver, et une clairière dans la prairie chaude change de climat agricole.

**Tag `fallow:peut_devenir_clairiere` à compléter**

- Ajouter : `#minecraft:is_forest` (déjà présent), `#minecraft:is_taiga`, `#c:is_plains`, `#c:is_swamp` **sauf** `blackwater_river`, `nomansland:old_growth_forest_clearing` et `nomansland:mud_beach`.
- Exclure : toutes les rivières, plages, océans, `#nomansland:caves`, `nomansland:alchemist_ruins` et `nomansland:dream`.
- Un tag ne sait pas exclure. Il faut donc une **liste explicite** plutôt que `#c:is_swamp`, qui contient `blackwater_river` [JSON NML].

**Spawns dans la clairière**

- Ne **pas** mettre les clairières dans `#c:is_forest` ni `#minecraft:is_forest` : elles recevraient les cerfs NML et les monuments AR. Ce n'est pas nécessairement voulu.
- Les ajouter aux `naturalist:has_<mob>` choisis, par exemple papillon, luciole, lapin, hérisson, oiseaux. C'est ainsi qu'elles deviennent « verdoyantes ».
- Elles ne prennent les spawns qu'à petite dose : les CREATURE apparaissent surtout à la génération du chunk, puis seulement par passes lentes, toutes les 400 ticks [déduit].
- **In Control** : règle `{"biome":["fallow:clairiere","fallow:clairiere_froide","fallow:clairiere_chaude"],"hostile":true,"result":"deny"}`. Un biome sans liste `monster` suffit pour le vanilla, mais pas pour les mods qui font apparaître par leur propre code (Mowzie's, événements lunaires…).

**Le reste**

- **ATMOSPHERICS** : un préréglage par variante de clairière (brume légère).
- **Ancient Remnants** : ajouter les trois clairières aux tags `has_structure` **seulement** si l'on veut que les fragments tombent aussi dans les clairières. Sinon, elles « coupent » les fragments [déduit du §1.3].

**Une meilleure approche ?** Le biome est la seule clé que lisent à la fois la brume d'ATMOSPHERICS, les spawns, les saisons et la carte. `/fillbiome` reste donc le bon outil : écrit une fois, sans coût continu. Le seul défaut est le bord en marches de 4 blocs. **Un petit mod custom** ne se justifierait que pour un **bord en dégradé** : un mixin client sur le calcul du brouillard qui interpole selon la distance au centre des clairières connues (liste synchronisée par le serveur). Il est facultatif, et à réserver à la finition.

### 2.5 Zoo : déplacer, garder, nourrir

**Déplacer, sans Capture Nets**

| Moyen | Pour qui | Preuve |
|---|---|---|
| Laisse | la plupart des terrestres ; **pas** la libellule ni les aquatiques de C&C, ni le sculptor de Mowzie's | `canBeLeashed` [jars] |
| Bateau | petits animaux seulement : la largeur doit être inférieure à celle du bateau, et pas d'animal aquatique | [déduit, code vanilla] |
| **Siège Create** sur un train Steam 'n' Rails ou un dirigeable Sable | **tout animal**, même l'éléphant ou la girafe ; on tient l'animal en laisse puis clic droit sur le siège | `SeatBlock.canBePickedUp` + `sable:retain_in_sub_level` [jars] |
| Charrette d'Icy's (4 ou 6 places) | passifs, plus neutres et hostiles selon la config `cart_pickup` | [jar 2.1.4] : mettre à jour depuis 2.0.1 |
| « Suivre » + téléportation | animaux apprivoisés (vanilla, Naturalist, C&C, Companions!…), aidé par Companion | `follow.petForceTeleportingWhenChunkUnload` [jar] |
| Seau | les espèces à seau de Naturalist (§1.10) et les poissons vanilla | [jar] |

- **Ne pas** mettre d'animaux dans des wagonnets sur un dirigeable : les wagonnets sont détruits en quittant la zone de Sable (`destroy_when_leaving_plot`).
- **Hybrid Aquatic n'a pas de seau.** Deux options :
  - laisser ses espèces hors du zoo : elles ne comptent que pour le bestiaire, par longue-vue ou par photo ;
  - ou créer un objet KubeJS « seau de vivier » limité aux petits poissons Hybrid : `ItemEvents.entityInteracted`, sauvegarde du NBT dans l'objet, puis réapparition au clic sur l'eau. C'est faisable, mais cela revient à une mini-capture : à décider [déduit].

**Garder (persistance)**

- La plaque d'enclos (§2.6) appelle `entity.setPersistenceRequired()` sur chaque animal enregistré [déduit : méthode vanilla de `Mob`].
- Un nom donné avec une étiquette marche aussi.
- Je n'ai pas vérifié de façon fiable quelles espèces disparaissent seules : voir le §5.

**Nourrir.** Les fiches Wildex donnent déjà la nourriture d'élevage. Rien à construire, sinon s'assurer que les nourritures de Naturalist et d'AM existent dans la chaîne alimentaire de Fallow (rapport 2, Kaleidoscope).

**Évasions et bagarres**

- **Prédateurs** : lion (`lion_hostiles` inclut le cheval), tigre, ours, alligator chez Naturalist ; grizzly, tigre, crocodile, anaconda chez AM.
- Soit des enclos séparés ; soit une surcharge par datapack des tags `naturalist:*_hostiles` pour en retirer le cheval, l'âne et les animaux d'élevage (déduit).
- Garder la chasse contre la faune sauvage, pour que le monde reste vivant.
- **Évasions probables** :
  - volants : oiseaux de Naturalist, vautour, papillons, libellules, glare, geai bleu d'AM, etc. Il faut une volière fermée ;
  - grimpeurs et sauteurs : lézards ? araignée sauteuse de C&C ;
  - frienderman de NML (variante d'enderman : possible téléportation) ;
  - grands animaux face à des clôtures simples (girafe).
- Tout cela est à tester (§5).

### 2.6 Wildex ↔ zoo : la « plaque d'enclos » (KubeJS)

Wildex ne reconnaît pas les enclos (§1.4) : on construit l'objectif à côté.

1. **Mémoriser les découvertes** (server_scripts, réinscrit à chaque rechargement) :

   ```js
   const Bridge = Java.loadClass('de.coldfang.wildex.integration.kubejs.WildexKubeJsScriptBridge')
   Bridge.onDiscoveryChanged(d => {
     const p = Utils.server.getPlayer(d.playerUuid); if (!p) return
     const set = p.persistentData.getCompound('fallow_wildex'); set.putBoolean(d.mobId, true)
     p.persistentData.put('fallow_wildex', set)
     global.fallowPaliersBestiaire(p, d.discovered)       // paliers 25 / 50 / 100 / 150 espèces
   })
   ```

   La forme du rappel (interface Java ou fonction) reste à vérifier au premier essai. On peut aussi passer par `NativeEvents.onEvent('de.coldfang.wildex.api.event.WildexDiscoveryChangedEvent', e => …)`.
2. **Bloc `fallow:plaque_enclos`** (startup : `StartupEvents.registry('block')`). Au clic droit (`BlockEvents.rightClicked`) :
   - lister les `Animal` dans un rayon de 24 blocs ;
   - garder ceux qui sont tenus en laisse, nommés, apprivoisés ou déjà enregistrés ;
   - les rendre persistants ;
   - compter les **espèces distinctes** déjà découvertes dans Wildex (`fallow_wildex`) ;
   - stocker le résultat dans les données du monde (`server.persistentData`), par plaque.
3. **Score de zoo** = nombre d'espèces distinctes sur l'ensemble des plaques du joueur.
   - Les espèces sauvages non apprivoisables (cerf, élan, bison, girafe…) valent davantage, car il faut les transporter (§2.5).
   - Bonus si la plaque est dans une clairière : `level.getBiome(pos)` est `fallow:clairiere*`.
4. **Récompenses** (paliers de score ou de `discovered`) :
   - bénédiction d'AR choisie par `/ancient_remnants blessing set` ;
   - plans d'ingénieur (`plans_ingenieur.js`) ;
   - palier de lanterne ou de dissipateur (§10.3) ;
   - titres et advancements de chapitre.

   Le plan prévoit une lanterne à 25 espèces (§10.10) : à garder.

### 2.7 Exposure ↔ Wildex ↔ Ancient Remnants

- **Exposure → Wildex** : rien à construire (`exposureDiscoveryEnabled = true`). La découverte a lieu à la prise de vue.
- **Exposure → AR** : `NativeEvents.onEvent('io.github.mortuusars.exposure.neoforge.api.event.FrameAddedEvent', e => …)`. Si les `extraData` STRUCTURES de la frame contiennent `ancient_remnants:<monument>` et que le photographe n'a pas encore cette page, il reçoit un `ancient_remnants:research_scrap[ancient_remnants:stored_research=["ancient_remnants:<monument>"]]`.
  - Il faut vérifier que l'événement est bien levé côté serveur. Wildex l'écoute pour découvrir des mobs, donc c'est probable [déduit].
  - C'est **la** boucle « explorer, photographier, comprendre » de la vision.
- **Exposure → advancements** : `exposure:frame_exposed` avec `entities_in_frame` pour des défis photo (ours, baleine, Elderheart), sans script.

### 2.8 Fragments du codex dans les butins (LootJS ou datapack, compatible Lootr)

Datapack sans script, avec `neoforge:add_table` (type de GLM présent dans NeoForge 21.1 ; déduit, à confirmer dans le jar NeoForge) :

```json
{ "type": "neoforge:add_table",
  "conditions": [
    { "condition": "neoforge:loot_table_id", "loot_table_id": "nomansland:chests/sniffer_monument" },
    { "condition": "minecraft:random_chance", "chance": 0.35 } ],
  "table": "ancient_remnants:random_research_scrap" }
```

Ce fichier est à déclarer dans `data/neoforge/loot_modifiers/global_loot_modifiers.json`. On peut faire l'équivalent avec LootJS dans `loot/butin.js` : `event.addTableModifier('nomansland:chests/sniffer_monument').addLoot(…)`.

Où injecter :

| Source | Chance | Pourquoi |
|---|---|---|
| `nomansland:archaeology/alchemist_ruins` | 25 % | Archéologie : geste lent, cohérent ; Lootr convertit le gravier suspect, donc chaque joueur a sa part |
| `nomansland:archaeology/well` | 15 % | idem |
| `nomansland:chests/sniffer_monument`, coffres des menhirs et des creature monuments | 30 à 40 % | lieux énigmatiques |
| `nomansland:chests/alchemist_pot_*` | 5 % | les jarres se regarnissent : chance faible, sinon on peut les fermer en boucle |
| `nomansland:chests/water_well` | 10 % | |

- **Pages Fallow** : ajouter des entrées `data/fallow/ancient_remnants/research/<id>.json` (texte et texture 350×210), avec `biomes` pointant sur des tags NML. Le codex raconte alors aussi l'histoire du monde de Fallow ; les mêmes tables les distribuent.
- **Recette `paper_from_research_scrap`** : la retirer (`ServerEvents.recipes` → `event.remove({id:'ancient_remnants:paper_from_research_scrap'})`), car elle détruit l'objectif [déduit].
- Garder la chute de 0,2 % sur les mobs tués : c'est un bonus rare.

### 2.9 Plans d'ingénieur et butins NML / Lootr

- Les plans de `plans_ingenieur.js` sont lus au clic droit et consommés. Avec Lootr, chaque joueur trouve son propre exemplaire dans le même coffre : c'est bien pour un serveur.
- Brancher dans `loot/butin.js` :
  - `fallow:plan_atelier` dans `nomansland:chests/small_dungeon_core` et `redstone_parts` ;
  - `fallow:plan_vapeur` dans `sniffer_monument`.
- Lootr : `loot_table_blacklist` vide ; ne pas mettre `decay`. Activer `refresh` seulement pour les petits donjons NML, si l'on veut un butin renouvelable.

### 2.10 Antique Atlas ↔ ruines (resource pack + config)

- **Ressources** dans un resource pack du pack (dossier `resourcepacks/` ou KubeJS `assets/`) :
  - `assets/nomansland/atlas/structure/start/<id>.json` pour bell_sanctuary, menhirs, creature_monuments, alchemist_ruins et deep_well ;
  - `assets/ancient_remnants/atlas/structure/start/<id>.json` pour les six monuments, chacun avec un marqueur propre ;
  - `assets/nomansland/atlas/biome/<biome>.json` pour que les 24 biomes NML aient des tuiles, sinon l'atlas les dessine en tuile par défaut.
- Créer aussi des tuiles pour les trois `fallow:clairiere*`, afin que les clairières se voient sur la carte. Cela récompense la carte « brume levée » [déduit].
- **Ajouter AA4 Atlas 1.1.2** : l'atlas devient un objet (livre + boussole), avec `requireItem = true`, et Antique Atlas passe de `side = "client"` à `both`, à vérifier. Une carte qu'il faut fabriquer et emporter colle à la vision.
- **Surveyor** : `globalSharing = false` (chacun découvre son monde) ; `discoveryMessages = true` pour les monuments (« Vous découvrez… ») ; `playerDeathWaypoints = true`.

### 2.11 Faune : régler les doublons sans retirer de mod

Une seule version par espèce et par biome, selon 0003 :

| Espèce | Garder | Couper l'apparition de | Outil |
|---|---|---|---|
| Cerf | NML (biomes NML) | Naturalist `deer_removed` ou `naturalist:has_deer` vidé | config / tag |
| Élan | AM, faute de spawn NML trouvé | — | vérifier en jeu |
| Oie | NML (rivières, marais) | Duck Mod : garder le canard, couper l'oie (biome modifier `neoforge:remove_spawns`) | datapack |
| Tortue terrestre | Naturalist | NML tortoise (biome modifier) | datapack |
| Tigre, éléphant, rhinocéros, komodo | Naturalist | AM, par sa config de spawn espèce par espèce | config AM |
| Ours | Naturalist (bear, black_bear) | AM grizzly | config AM |
| Requins | Naturalist great_white + Hybrid | AM hammerhead (et frilled_shark si Hybrid a l'équivalent) | config |
| Orque | Hybrid ou AM, au choix du modèle | l'autre | config |
| Blobfish | Naturalist (à seau) | AM, Hybrid | config |
| Crabe | Naturalist (plages) + F&F (déjà classé « pacifique ») | Hybrid crab | config Hybrid |
| Loutre | C&C | Hybrid otter | config |
| Escargot, libellule | C&C | Naturalist | config Naturalist |
| Poisson-chat, geai bleu, serpent à sonnette, homard, calmar géant | un seul (Naturalist ou Hybrid) | l'autre | config |
| Mammouth (Naturalist) | — | `mammoth_removed` (préhistoire écartée en 0003) | config |

- **Coupure** = apparition naturelle coupée. Les entités restent dans le registre, donc dans le total de Wildex : **chaque espèce coupée doit entrer dans `excludedModIds` ou dans une liste d'ids exclus**, sinon la complétion est impossible.
- **Liste d'exclusions Wildex** à écrire :
  - les mobs vanilla de `bannedVanilla` ;
  - Nether et End (blaze, ghast, piglin, hoglin, strider, magma_cube, wither_skeleton, zoglin, piglin_brute, ender_dragon, shulker, endermite, wither, warden) ;
  - les hostiles de F&F coupés (iceologer, illusioner, wildfire) ;
  - les boss de Companions! désactivés ;
  - les doublons coupés ci-dessus.

  `keepCompletionAfterNewMobs = false` peut rester.

### 2.12 Réglages Wildex à écrire (config commune)

| Clé | Valeur | Raison |
|---|---|---|
| `shareOffersPaymentEnabled` | false | pas d'économie à l'émeraude dans Fallow |
| `newEntryXpAmount` | 5, ou 0 si l'XP sert ailleurs (charrettes d'Icy's, enchantements) | |
| `spyglassDiscoveryRange` | ≈ 48 | 98 blocs, c'est découvrir sans approcher |
| `hiddenMode`, `requireBookForKeybind`, `giveBookOnFirstJoin` | true | défaut |

Retirer la recette de l'Analyzer (`ServerEvents.recipes`) : découvrir un mob en déposant un objet court-circuite l'observation. On peut aussi la verrouiller derrière un palier du zoo.

---

## 3. Conflits et risques

1. **Breezes contre `bannedVanilla`** (§2.2) : monuments sans gardien, ou bien mob vanilla hors direction artistique. À décider.
2. **Spawners des petits donjons NML** (araignée, squelette, zombie) : la règle In Control `"mob":[…zombie…skeleton…spider…],"result":"deny"` et `spawns_safety.js` les annulent. Les spawners tourneront donc à vide [déduit].
   - C'est cohérent avec « aucun monstre vanilla ».
   - On peut aussi les remplacer par processeur, sur le modèle du §2.2 B, avec un hostile de Fallow.
3. **Sparse Structures** espace NML (sets à clé `spacing`) **mais pas AR**. Avec `spreadFactor 3` (§02), les petits donjons NML passent de 10 à environ 30 chunks, et les ruines d'alchimiste de 50 à environ 150 chunks, soit 2 400 blocs. Elles risquent de devenir trop rares [déduit].
   - Mettre une exception `nomansland:*` dans Sparse Structures ;
   - ou un facteur de 1,5 à 2.
4. **Hybrid Aquatic ajoute des biomes**, contraire au critère de 0003. Sa `tropical_river` remplace `minecraft:river` : rivières tropicales en pleine taïga ? NML a ses propres rivières.
   - Passer `generateTropicalRiver = false` au minimum.
   - Évaluer `enableBiomes = false`. On perd les récifs : les espèces récifales apparaissent-elles encore en `warm_ocean` ? À tester.
5. **Brouillard NML contre ATMOSPHERICS** : deux systèmes de brume sur les mêmes biomes (`has_dense_fog`). Mettre `fogModifiers = false` dans NML, ou ses intensités à 0, et laisser ATMOSPHERICS seul maître. Une clairière n'est pas dans `has_dense_fog` : la brume NML s'y arrête aussi. Mais si les deux systèmes restent actifs, les biomes voisins cumulent les deux brumes, et le contraste au bord de la clairière devient difficile à régler (déduit).
6. **Friend Moon de NML contre Enhanced Celestials** : deux systèmes de lunes. Garder la Friend Moon (lune qui parle, rêves) comme mystère, ou la couper par `friendMoonDimensions = []`. Vérifier qu'elle ne se déclenche pas la même nuit qu'une lune blanche.
7. **Le registre de chevaux d'Icy's affiche des coordonnées**, ce qui contredit l'esprit « carte à faire soi-même ». C'est mineur ; le couper si la config le permet, à vérifier.
8. **Jarres NML qui se regarnissent** : c'est une ferme de butin si l'on y met des fragments ou des plans. Chances faibles (§2.8).
9. **Spyglass Pulse de Wildex** : aucun interrupteur. Il révèle les mobs à 32 blocs, mais seulement après complétion : on peut l'accepter comme récompense finale. Sinon, petit mod custom avec un mixin qui annule l'action (cinq lignes).
10. **L'Analyzer de Wildex** est un raccourci (§2.12).
11. **Bénédiction du guerrier presque nulle** : elle compte les `Monster` dans 12 blocs, or Fallow en a très peu. Remplacer son contenu par datapack (`data/ancient_remnants/tags/ancient_remnants/blessing/warrior_monolith.json`, `replace: true`) par une autre bénédiction existante. On ne peut pas en créer de nouvelle sans mod [jar].
12. **Bénédiction unique et remplacée** : visiter un second monument écrase la première. Ce choix permanent doit être **annoncé** au joueur (texte du codex ou message KubeJS), sinon c'est frustrant [jar + déduit].
13. **Clairières et fragments** : les clairières sortent les zones des tags de pages, donc ne donnent plus de fragments sur les mobs (§2.4).
14. **Prédateurs contre chevaux et élevage** : `lion_hostiles` contient `minecraft:horse` (§2.5).
15. **In Control 10.3.0 est en bêta**, à surveiller. Les plafonds de `spawn.json` ont tous `perplayer: true`, c'est bien.
16. **Mises à jour** : NML 1.6.1 et Icy's 2.1.4 changent la génération du monde ou le contenu. Les faire **avant** de figer le monde permanent.

---

## 4. Ce qu'il faut adapter, supprimer ou ajouter dans le plan

| Où | Changement | Raison |
|---|---|---|
| §11.8 | **Supprimer** « fragments laissés par un marchand ambulant » ; **remplacer** par : fragments dans l'archéologie et les coffres NML (§2.8), photo de monument (§2.7), chute rare de 0,2 % | le marchand n'existe pas [jar AR] |
| §10.3 / `clairieres.js` | colonnes alignées sur 4, pleine hauteur (`c.y−16` à 319) ; 3 variantes ; tag filtre explicite avec les biomes NML ; déclencheur Elderheart | §2.3 et §2.4 |
| `clairiere.json` | ajouter `clairiere_froide` et `clairiere_chaude` ; ajouter les tags Naturalist choisis ; règle In Control anti-hostile | Ecliptic, spawns |
| `00_fallow_constants.js` / `spawns_safety.js` | décision sur les breezes (§2.2) | conflit direct avec AR |
| §10.10 et nouveau script | `zoo.js` → plaque d'enclos + pont Wildex ; `wildex_bestiaire.js` pour les paliers | Wildex n'a pas d'enclos |
| §02 (Sparse Structures) | exception ou facteur réduit pour NML ; **retirer** l'idée d'espacer AR par Sparse Structures ; espacement AR dans sa propre config | §1.3, §3.3 |
| `loot/butin.js` | fragments (`random_research_scrap`) et plans dans les tables NML listées au §2.8 et au §2.9 | |
| Recettes | retirer `ancient_remnants:paper_from_research_scrap` et la recette de l'Analyzer Wildex | objectifs longs |
| Config AR | biomes et espacement (§2.1) ; tag de bénédiction du guerrier (§3.11) | |
| Config Wildex | exclusions (§2.11) et réglages (§2.12) | complétion possible |
| Config Hybrid | `generateTropicalRiver = false`, et `enableBiomes` à évaluer | 0003 |
| Config NML | `fogModifiers` off (ATMOSPHERICS seul) ; décision sur la Friend Moon | §3.5, §3.6 |
| Configs de faune | tableau des doublons (§2.11) | 0003, une seule version par espèce |
| Pack : **ajouter** | Lootr, In Control!, Exposure, Wildex, AR + Fragmentum, Enhanced Celestials, LootJS (tous prévus), **AA4 Atlas** | atlas comme objet |
| Pack : **mettre à jour** | NML 1.6.1, Icy's 2.1.4 (charrettes du zoo), F&F 5.0.2, Ecliptic 0.15.4.1 et les versions mineures du §1.1 | avant de figer le monde |
| Resource pack | marqueurs Antique Atlas (NML, AR) et tuiles de biomes NML et clairières | §2.10 |
| Mod custom (facultatif) | 1) bord de clairière en dégradé (mixin client sur le brouillard) ; 2) coupure du Spyglass Pulse | seulement si la finition l'exige |

Aucun mod custom n'est **nécessaire** pour ces liens : config, tags, datapack, resource pack et KubeJS suffisent.

---

## 5. Questions qui demandent un test en jeu

1. Les clés exactes de la liste de biomes dans `ancient_remnants-common.toml`, et le fait qu'un tag (`#minecraft:is_badlands`) y soit accepté.
2. Où tombent les monuments dans les biomes NML : un monument de 7 pièces jigsaw sur un relief NML, un monolithe sentinelle en `frozen_shore`.
3. `spawns_safety.js` : `event.cancel()` sur une breeze de spawner ; le spawner continue-t-il de tourner à vide ? Faut-il un `entity.spawnType` pour l'exception (§2.2 A) ? La surcharge `append_static` remplace-t-elle `SpawnData` (§2.2 B) ?
4. Polling de l'Elderheart : `getEntitiesWithin` le trouve-t-il ? Le tag `fallow_eveille` survit-il au déchargement du chunk ?
5. `/fillbiome` lancé par `runCommandSilent` : le retour des biomes au client est-il immédiat (brume ATMOSPHERICS) ? Combien de temps pour un rayon de 64 en pleine hauteur ? (environ 200 colonnes × 3 passes, à 4 colonnes par tick, soit 50 ticks)
6. Ecliptic dans les trois clairières : la neige tombe-t-elle et fond-elle comme dans le biome d'origine ? Les cultures poussent-elles au même rythme ?
7. Pont KubeJS de Wildex : la forme exacte du rappel accepté par `onDiscoveryChanged` (fonction JS ou interface) ; tient-il après `/reload` ?
8. Exposure `FrameAddedEvent` : est-il levé côté serveur ? `STRUCTURES` contient-il `ancient_remnants:*` quand on photographie un monument de près ?
9. Lootr : convertit-il le gravier suspect des ruines NML placé par `append_loot` ? Les GLM s'appliquent-ils bien au tirage de chaque joueur ?
10. Siège Create : un éléphant ou une girafe sur un siège de train ou de dirigeable Sable reste-t-il assis en vol et au déchargement du chunk ?
11. Charrette d'Icy's 2.1.4 : ramasse-t-elle les animaux de Naturalist et d'AM ? Quelles tailles ?
12. Disparition : quelles espèces (Naturalist, AM, C&C, NML) disparaissent seules quand on s'éloigne, et `setPersistenceRequired()` suffit-il ?
13. Évasions : volants, grimpeurs, frienderman ; hauteur de clôture suffisante pour girafe et éléphant.
14. Bagarres : prédateurs contre bétail dans un zoo dense ; la surcharge des tags `*_hostiles` marche-t-elle ?
15. Hybrid avec `enableBiomes = false` : ses espèces récifales apparaissent-elles encore ?
16. Brouillard NML coupé : ATMOSPHERICS seul donne-t-il la bonne brume dans `has_dense_fog` ?
17. Élan NML : apparaît-il quelque part ? Aucun biome modifier n'a été trouvé.
18. Friend Moon et lune blanche d'Enhanced Celestials : coexistence la même nuit.
19. Sparse Structures avec `spreadFactor 3` : densité réelle des ruines NML sur une carte de 10 000 × 10 000 (Chunky + Antique Atlas).
20. Antique Atlas sous Connector : lit-il les marqueurs `assets/<ns>/atlas/structure/start/` pour des structures NeoForge (NML, AR) ? AA4 Atlas rend-il l'objet obligatoire ?
