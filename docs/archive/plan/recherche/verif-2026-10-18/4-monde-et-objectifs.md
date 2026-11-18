# 4 — World, fauna and long-term goals (verification of 2026-10-18)

Scope: No Man's Land (NML), Sparse Structures, Lithostitched, Biolith, Ancient Remnants: Monoliths (AR) + Fragmentum, Wildex Bestiary, Exposure, Antique Atlas / AA4 Atlas / Surveyor, Lootr, Enhanced Celestials, In Control!, all the fauna and companions, and the transport of animals by Create, Steam 'n' Rails and Create Aeronautics / Sable.

Method: jars downloaded from Modrinth and inspected (JSON from `data/` and `assets/`, `javap` on the classes). The pack is read from the repository's `origin/main`, the drafts in `plan/kubejs-brouillons/`. Nothing was modified in the repository.

Notation of the evidence:
- **[jar X v]**: read in the jar of that version;
- **[Modrinth]**: version verified through the API;
- **[pack]**: `origin/main`;
- **[plan]**: chapters of the plan;
- **[deduced]**: reasoning, not yet verified in game.

Standing decision: **all the fauna and companions stay**. This report proposes no mod removal, only settings.

---

## 0. Summary in ten points

1. **AR's monuments do not read a tag supplied by a datapack.** Their biomes come from the config `config/obscuria/ancient_remnants-common.toml`, injected at load time into an empty tag. **Sparse Structures does not space them out**, for lack of a `spacing` key in their JSON. Everything is therefore set in AR's config.
2. **The monuments' guardians are vanilla `minecraft:breeze` spawners**, and the breeze is in `global.FALLOW.bannedVanilla`. As written, the drafts therefore empty the monuments of their guardians. A decision is needed.
3. **The Elderheart is an entity, not a block.** Approaching within 16 blocks is enough: the blessing is given every 100 ticks. No event is emitted. The clearing must therefore go through KubeJS polling.
4. **Blessings:** only one at a time, permanent, kept on death, replaced by the next. The warrior monolith's is almost null in Fallow, because of the few monsters.
5. **Codex fragments almost never drop.** One gets 0.2% per mob killed by a player, and the "outpost" table is linked to nothing. **The "wandering trader" of §11.8 does not exist.** The fragments must be injected ourselves, into NML's loot (Lootr included).
6. **Clearings through `/fillbiome`: feasible.** Necessary corrections:
   - columns aligned on 4 and full height;
   - three temperature variants, for Ecliptic;
   - filter tag completed with the NML biomes;
   - spawns set through Naturalist tags and In Control.
7. **Zoo:** no lead-free net is needed.
   - Means: lead; boat for the small ones; **Create seat on a train or Sable airship** (no size limit); Icy's 2.1.4 cart.
   - Wildex does not know about enclosures. An "enclosure plaque" is needed in KubeJS, with the Wildex bridge.
8. **Wildex:**
   - Without an exclusion list, completion is impossible: Nether and End mobs, banned vanilla monsters, mobs cut in config.
   - The Analyzer is a shortcut to cut.
   - The Spyglass Pulse has no switch.
9. **Hybrid Aquatic adds nine biomes through Lithostitched**, including a `tropical_river` that replaces `minecraft:river`. This is contrary to the criterion of 0003 ("add biomes: conflict with NML"). They are cut through its config, not by removing the mod.
10. **Fauna duplicates:** a dozen species exist two or three times (deer, moose, goose, tortoise, tiger, sharks, crabs, etc.). They are settled through config or biome modifier, without removing a mod. Wildex takes them into account.

---

## 1. What is confirmed

### 1.1 Versions and presence in the pack

Mods present in `pack/mods/` [pack], compared with the latest NeoForge 1.21.1 version on Modrinth [Modrinth]:

| Mod | Pack | Modrinth | Remark |
|---|---|---|---|
| No Man's Land | 1.5.12 | **1.6.1** (2026-10-14) | analysed on 1.6.1; the entities already exist in 1.5.12 |
| Friends & Foes | 4.0.27 | 5.0.2 | same config keys |
| Icy's Better Horses | 2.0.1 | **2.1.4** | **carts that pick up mobs** (useful for the zoo) |
| Naturalist | 2.0.4 | 2.0.5 | |
| Alex's Mobs Continued | 2.2.2 | 2.2.3 | |
| Hybrid Aquatic | 1.7.3 | 1.7.4 | |
| Companions! | 1.3.4 | 1.3.6 | |
| Sable | 2.0.5 | 2.0.6 | |
| Ecliptic Seasons | 0.15.0-rc-3-1 | 0.15.4.1 | |
| Lithostitched | 1.8.0 | 1.8.1 | |
| Antique Atlas 4 | file `antique-atlas-3.1.2+1.21.jar`, `side = "client"` | | Fabric via Connector |
| Surveyor | 1.2.4, `side = "both"` | | |

**Planned in the plan but absent from the pack** [pack + Modrinth]: Lootr 1.11.38.127, In Control! 10.3.0 (beta), Exposure 1.9.19, Wildex 3.0.0, Ancient Remnants 1.5.2 + Fragmentum 5.4.0, Enhanced Celestials 6.0.2.6, AA4 Atlas 1.1.2, LootJS 3.7.0. KubeJS is at 2101.7.2.

### 1.2 No Man's Land 1.6.1 [jar NML 1.6.1]

- **Bundled libraries (jarjar)**: Biolith 3.0.11, mixed-litter 1.2.2, sable-companion. TerraBlender is marked "discouraged".
- **Placement:** Biolith does partial `replaceOverworld`s. **The vanilla biomes remain**, including cherry grove, badlands, ice_spikes and deep_frozen_ocean, those of the default AR monuments.
- **The 24 biomes:**
  - alchemist_ruins (underground, no spawns), autumnal_forest, bayou, blackwater_river, bog, boreal_forest
  - cave_depths, caves, dark_swamp, dark_taiga, desert_river
  - dream (black biome, outside `#is_overworld`)
  - frozen_shore, frozen_woods, lavender_field, lush_river, maple_forest, maple_grove, mud_beach
  - old_growth_forest, old_growth_forest_clearing, old_growth_forest_edge, prairie, tropical_beach
- **Tags provided by NML:**

  | Tag | NML biomes |
  |---|---|
  | `minecraft:is_forest` | autumnal_forest, dark_swamp, boreal_forest, maple_forest, maple_grove, `#nomansland:old_growth_forest` |
  | `minecraft:is_taiga` | dark_taiga, frozen_woods |
  | `minecraft:is_river` | the 3 NML rivers |
  | `c:is_swamp` | bayou, bog, dark_swamp, blackwater_river |
  | `c:is_plains` | prairie, lavender_field |
  | `c:is_floral` | lavender_field |
  | `c:is_cold/overworld` | bog, dark_taiga, maple_grove, frozen_woods, frozen_shore |
  | `c:is_hot/overworld` | bayou, prairie, lush_river, desert_river, tropical_beach |
  | `c:is_spooky` | dark_swamp, dark_taiga |
  | `nomansland:has_dense_fog` | `#c:is_swamp`, jungle, bamboo_jungle, mushroom_fields, frozen_woods |

  There are also `c:is_rare`, `nomansland:caves` and `nomansland:old_growth_forest`.
- **Structures** (structure sets, spacing in chunks):

  | Set | Spacing / separation |
  |---|---|
  | alchemist_ruins | 50/32 |
  | bell_sanctuaries (custom placement) | 42/16 |
  | creature_monuments (drowned, creeper, sniffer) | 50/28 |
  | deep_wells | 46/18 |
  | menhirs (large, menhir, lone) | 46/18 |
  | desert_ruins, desert_wells | 16/5 |
  | small_dungeons | 10/4 |

  meeting_points has a custom placement set by NML's config (min and max distance). forest_temples and underground_shelters are disabled.
- **Loot:**
  - chests:
    - sniffer_monument → `nomansland:chests/sniffer_monument`;
    - deep_well → `nomansland:chests/water_well`;
    - small_dungeon → `small_dungeon_core`, `minecraft:chests/simple_dungeon`, `redstone_parts`, `dungeon_garbage`, `dungeon_kitchen`, plus a dispenser;
  - suspicious gravel through the `append_loot` processor: `nomansland:archaeology/alchemist_ruins` (ruins and bell sanctuaries) and `nomansland:archaeology/well`;
  - **alchemist jars** (custom blocks, `pot_patch`): tables `chests/alchemist_pot_*` and `ancient_pot_cave`. The `RegeneratingPotsData` class refills them; no config key sets this regeneration.
  - NML GLM: warding_effigy and hardtack added to vanilla chests.
- **NML's small dungeons contain vanilla spider, skeleton and zombie spawners** [template JSON].
- **NML entities:** billhook_bass, buddy, buried, cave_carp, deer, frienderman, goose, living_pot, living_urn, moose, tortoise.
  - The deer spawns in `#c:is_forest` (weight 8) and in `#c:is_taiga` (weight 10).
  - The goose spawns on rivers and in swamps; the tortoise in jungle, badlands, plains…
  - **No spawn biome modifier for the moose** was found: the folder is empty. To see in game.
- **NML's own fog** (NMLConfig, category `fog_modifiers`): `fogModifiers`, `foggyBiomeFogModifier`, `cavesBiomeFogModifier`, `deepDarkFogModifier`, `surfaceFogModifier`, `surfaceFogIntensity`.
- **NML has its own "Friend Moon"**: a talking moon, dreams, the Meeting Point, the key `friendMoonDimensions`. It is distinct from Enhanced Celestials.

### 1.3 Ancient Remnants 1.5.2 [jar AR 1.5.2 + Fragmentum 5.4.0]

**Biomes and spacing**

- Config: `config/obscuria/ancient_remnants-common.toml` (class `CommonConfig`). Per monument: `<id>_placement_spacing = 100`, `<id>_placement_separation = 20`, and a list of biomes.
- Default biomes:

  | Monument | Default biomes | Blessing (tag `ancient_remnants:blessing/<id>`) |
  |---|---|---|
  | hunter_monolith | `minecraft:badlands` | hunter_instincts |
  | warrior_monolith | `minecraft:ice_spikes` | warrior_wrath |
  | sentinel_monolith | `minecraft:deep_frozen_ocean` | sentinel_aegis |
  | cherry_monument | `minecraft:cherry_grove` | desperate_vitality |
  | oakwood_monument | `#minecraft:is_forest` | bountiful_harvest |
  | coniferous_monument | `#minecraft:is_taiga` | architect_reach |

- **The tag `ancient_remnants:has_structure/<id>` is empty in the jar.** Comment in the file: `"Default values are added programmatically from config"`. `TagPostProcessors.register` (Fragmentum) fills it from the config. Consequences:
  - a datapack can **add** biomes to this tag;
  - to **remove** a default biome, the config must be used (`replace: true` has no effect on an addition made afterwards; deduced).
- **Placement:** type `ancient_remnants:configurable_random_spread` (subclass of `RandomSpreadStructurePlacement`), with `"spacing_config": "@OAKWOOD_MONUMENT_PLACEMENT_SPACING"` and **no `spacing` key**. Sparse Structures' `MakeStructuresSparse` mixin only rewrites the JSON keys `spacing` and `separation` when they exist. **Sparse Structures therefore does not touch AR** [jar Sparse Structures + AR JSON].

**Structures and guardians**

- Jigsaw structures of size 7. The monoliths start at `start_height` 20; the monuments start at `absolute -24`, projected onto `WORLD_SURFACE_WG`. `max_distance_from_center` 128, `spawn_overrides` empty.
- **Guardians**: vanilla `minecraft:breeze` spawners, in each monument except the sentinel monolith. Settings: `MinSpawnDelay 200`, `MaxSpawnDelay 800`, `SpawnCount 4`, `MaxNearbyEntities 6`, `RequiredPlayerRange 16`.
- The `processor_list`s `monolith.json` and `monument.json` only do a `block_ignore` (air, structure_block). A datapack can override them at the same path.

**The Elderheart**

- It is an invulnerable **entity** (`ancient_remnants:elderheart`): `hurt` returns false.
- NBT `PossibleBlessings` = `ancient_remnants:<monument>`, i.e. the blessings tag.
- Every 100 ticks, the nearest player within a radius of **16 blocks** receives the blessing: title "Blessing Acquired", then `setBlessing`. **No NeoForge event is emitted**, and there is no item to use.
- Its ambient sound is audible up to 256 blocks.

**Blessings**

- Only one at a time, stored in the player's NBT (`AncientRemnantsBlessing`).
- **Permanent and kept on death** (`onRestore`). Another blessing replaces it.
- The effect is reapplied every tick for 62 ticks.
- Op command: `/ancient_remnants blessing set|remove <player>`.

| Blessing | Exact effect (bytecode) |
|---|---|
| hunter_instincts | indirect damage (projectiles) × (1 + easeInCirc(distance/64)): up to ×2 at 64 blocks |
| warrior_wrath | melee damage × (1 + easeInCirc(number of `Monster`s within 12 blocks / 12)) |
| sentinel_aegis | armour + 20 × easeInCirc(1 − HP/maxHP) |
| desperate_vitality | below 33% HP: Regeneration for 200 ticks |
| architect_reach | +3 `BLOCK_INTERACTION_RANGE` |
| bountiful_harvest | 50% chance of doubled harvest on mature crops (`CropBlock` at max age, or age 3) |

**Codex and fragments**

- The fragment item is `ancient_remnants:research_scrap`, with the component `ancient_remnants:stored_research`: a **list** of page ids.
- Pages form a datapack registry: `data/<ns>/ancient_remnants/research/<id>.json`, with `biomes` (a tag), `texture` (350×210), `header`, `content` and `right_page`. The six original pages point to `ancient_remnants:has_structure/<id>`.
- Recipes: `monolith_codex` (book + amethyst + deepslate), `monolith_codex_restoration` (codex + fragment), `paper_from_research_scrap` (fragment → 3 paper).
- **Only active source of fragments: `ResearchSystem.dropExtraDeathLoot`.**
  - A mob killed by a player drops a fragment with a **0.2%** chance (`nextFloat > 0.002 → return`).
  - The page drawn is one of those whose `biomes` tag contains the biome of the place.
- **The table `ancient_remnants:loot_table/extensions/pillager_outpost.json` (50% `random_research_scrap`) is linked to nothing.** No global loot modifier exists in AR or in Fragmentum (grep on `loot_modifiers`).
- The table `ancient_remnants:random_research_scrap` draws **one page at random among the six**. It can be used as is.
- **No wandering trader exists in AR.** The statement in §11.8 ("fragments left by a wandering trader") is false.

### 1.4 Wildex 3.0.0 [jar Wildex 3.0.0]

**Events**

- NeoForge events:
  - `de.coldfang.wildex.api.event.WildexDiscoveryChangedEvent`: player, mobId, discoveredCount, totalCount, completionPercentScaled;
  - `de.coldfang.wildex.api.event.WildexCompletedEvent`.
- **Built-in KubeJS bridge**: `de.coldfang.wildex.integration.kubejs.WildexKubeJsScriptBridge`.
  - Methods `onDiscoveryChanged(cb)` and `onCompleted(cb)`. Internal names: `wildex.discovery_changed` and `wildex.completed`; `"*"` listens to all of them.
  - Fields received: apiVersion, playerUuid, playerName, mobId, discovered, total, percentScaled, isComplete.
  - `clearAll` is called at `ServerAboutToStart` and at each datapack resync: **one must register from `server_scripts`**, which are reloaded at the same time.
- API: `WildexApi.getDiscoveredCount`, `getTotalCount`, `getCompletionPercentScaled`, `isComplete`.
- Scoreboard objectives: `wildex_discovered`, `wildex_total`, `wildex_percent`, `wildex_complete`.

**Common config**

| Key | Default |
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
| shareOffersPaymentEnabled | true (emeralds, max 64) |
| kubejsBridgeEnabled | true |
| exposureDiscoveryEnabled | true |
| debugMode | false |

There are also the lists `excludedModIds` (namespace or id) and `excludedVariantMobIds`.

**Discovery and blocks**

- Discovery sources: KILL, SPYGLASS, ANALYZER, DEBUG, plus Exposure and sharing.
- **Exposure**: `onFrameAdded(FrameAddedEvent)` discovers each entity in the frame **when the picture is taken**, not when it is developed.
- **Analyzer** (copper + gold + book): one places in it an item dropped by a mob, powers it with redstone, and it discovers a still-unknown creature that drops this item.
- **Pedestal** (amethyst + polished basalt + book): it displays a hologram of the creature.
- **Spyglass Pulse**: unlocked on completion, **no config key**. Left click with the spyglass: radius 32, recharge of 300 ticks, probably a glowing effect.
- **No notion of enclosure or captivity.** The total counts all non-excluded living entity types (deduced: it is computed from the registry, not from actual spawns).

### 1.5 Exposure 1.9.19 [jar Exposure 1.9.19]

- Events:
  - `io.github.mortuusars.exposure.neoforge.api.event.FrameAddedEvent`: `getEntitiesInFrame`, `getPositionsInFrame`, `getFrame`, `getCameraHolderEntity`;
  - `ModifyFrameExtraDataEvent`.
- The Frame's `extraData` contains BIOME, **STRUCTURES** (list of ids), POSITION, DAY_TIME, WEATHER, IN_CAVE, UNDERWATER…
- Advancement criterion `exposure:frame_exposed`, for example `{"frame":{"entities_in_frame":[{"type":"naturalist:bear"}]}}`. The predicate also accepts `extra_data`.
- Exposure adds its own GLMs to vanilla chests.

### 1.6 Lootr 1.11.38.127 [jar Lootr]

- Converts: chests, barrels, trapped chests, shulkers, **`minecraft:decorated_pot`**, **suspicious gravel and sand**, frames.
- **NML's custom jars are not converted**: they are not decorated_pots.
- Config: `decay_*`, `refresh_*`, `loot_table_blacklist`, `disable_break`, `enable_break`, `blast_immune`, `randomise_seed`, `team_loot`, `perform_piecewise_check`… Structure tags: blacklist, refresh, decay.
- Global loot modifiers, hence LootJS and `neoforge:add_table`, also apply to the draws that Lootr makes for each player [deduced: Lootr calls the table normally with the player's context].

### 1.7 Antique Atlas 3.1.2 ("Antique Atlas 4"), AA4 Atlas 1.1.2, Surveyor 1.2.4 [jars]

- **Antique Atlas** reads structure markers in `assets/<ns>/atlas/structure/{start,type,tag,piece}/<id>.json`, in the form `{"markers":"antique_atlas:structure/…"}` or `{"textures":…}`, and biome tiles in `assets/<ns>/atlas/biome/<biome>.json`.
  - Client config: `requireItem`, `structureMarkers` (Map), `graveStyle`, `emptyHandling`…
  - These are **assets**: a resource pack is needed, not a datapack.
- **AA4 Atlas**: `environment "*"` (client and server), book + compass recipe, `KeybindLockMixin`. It makes the atlas a real item.
- **Surveyor**: `globalSharing`, `networking.positions`, `discoveryMessages`, `playerDeathWaypoints`, sharing of structures and landmarks. It is what provides Antique Atlas with the discovered structures.

### 1.8 In Control! 10.3.0 [jar]

Keywords present:

- **place**: `structure`, `hasstructure`, `structuretags`, `biome`, `biometags`;
- **spawn origin**: `spawner`, `spawntype`, `kubejs`;
- **counting**: `mincount`, `maxcount`;
- **others**: `nodespawn`, `customname`…

### 1.9 Transport: Create 6.0.10, Sable 2.0.6, Icy's Better Horses 2.1.4 [jars]

**Create seat**

- `SeatBlock.canBePickedUp` accepts **any `LivingEntity`**, except Shulker, Player and the entities of the tag `create:ignore_seat` (= `#c:capturing_not_supported`).
- Hostiles only if `seatHostileMobs`. **No size limit.**
- A leashed animal can be placed on a seat by right click (`getLeashed`).

**Sable**

- The tag `sable:retain_in_sub_level` contains `create:seat` and the contraptions.
- Minecarts are in `destroy_when_leaving_plot`.

**Icy's Better Horses 2.1.4**

- **Cart** `horse_cart` that picks up mobs.
  - Config `cart_pickup`, with passive, neutral and hostile filters.
  - Small cart: 4 seats; large, for draft horses: 6.
- Whistle that teleports the horse if it is far away, "Send Home", resurrection against XP.
- **The horse registry displays coordinates.**

**Companion (Snownee)**: `follow.petForceTeleportingWhenChunkUnload`, `portalTeleportingPets`, `injuredStatus.*`.

### 1.10 Fauna: verified content [jars]

**Naturalist 2.0.5**

- Species:
  - mammals: bear, black_bear, boar, capybara, deer, elephant, giraffe, hedgehog, hippo, lion, mammoth, mole, rat, rhino, tiger, zebra;
  - birds: bird (bluejay, canary, cardinal, finch, robin, sparrow), duck, ostrich, turkey, vulture;
  - reptiles: alligator, komodo, lizard, snake (coral, rattlesnake), tortoise;
  - small creatures: ant, butterfly, caterpillar, desert_scorpion, jungle_scorpion, dragonfly, firefly, snail;
  - aquatic life: anglerfish, bass, blobfish, catfish, clam, crab, giant_isopod, great_white_shark, jellyfish, piranha, ray, starfish, whale.
- Config: `<mob>_removed`, `remove_all_bugs`. Spawns through tags `naturalist:has_<mob>` and biome modifier `naturalist:add_animals`.
- Hunting tags: `naturalist:lion_hostiles` (**includes `minecraft:horse`**), `tiger_hostiles`, `bear_hostiles`, `alligator_hostiles`, `deer_predators`…
- Buckets: anglerfish, bass, blobfish, catfish, duck, giant_isopod, jellyfish, piranha, ray, starfish.

**Alex's Mobs Continued 2.2.3**: grizzly_bear, tiger, elephant, rhinoceros, komodo_dragon, crocodile, caiman, moose, bison, cachalot_whale, orca, hammerhead_shark, frilled_shark, giant_squid, lobster, blobfish, catfish, rattlesnake, anaconda, blue_jay… The **capybara is not in AM**: only Naturalist has one.

**Hybrid Aquatic 1.7.4**

- About 130 aquatic species, **with no bucket at all**.
- **Adds biomes through Lithostitched** (`replace_partially`): coral_reef, deep_coral_reef, trenches, sulfuric_caves, tide_pools, seagrass_bed, red_meadow (underwater), deep_warm_ocean and **tropical_river, which replaces `minecraft:river`**.
- Switches in `BiomeConfig`: `enableBiomes`, `generateTropicalRiver`, `generateTidePools`, `generateSulfuricCave`, `generateRedMeadow`…

**Critters and Companions**: dragonfly, dumbo_octopus, ferret, jumping_spider, koi, ladybug, leaf_insect, otter, red_panda, roly_poly, sea_bunny, shima_enaga, snail, stag_beetle, stick_bug, weevil. The dragonfly and the aquatic species have `canBeLeashed` at false.

**The other fauna mods**

- **Friends & Foes**: barnacle, copper_golem, crab, glare, iceologer, illusioner, mauler, moobloom, penguin, rascal, tuff_golem, wildfire.
- **Mowzie's Mobs**: the sculptor cannot be leashed. Structures: frostmaw, monasteries, umvuthana groves, wrought chambers.
- **Companions!**: many hostiles and bosses (sacred_pontiff, shade_maw, soul_mage, illager_golem…).
- **Ghost**: ghost, kodama, small_ghost, haunted boats.
- **Untitled Duck Mod**: duck, goose.
- **Adorable Hamster Pets**: hamster, hampter.

### 1.11 Ecliptic Seasons 0.15.4.1 [jar]

- `agro_climate` types: temperate, cold, hot.
- The jar's `seasonal` and `agro/warm` tags are empty: classification is done in code, by the biome's temperature [deduced].
- `biome_rain_effect` adds fog under rain.

### 1.12 `/fillbiome` in 1.21.1 [deduced, known vanilla code]

- Limit set by the gamerule `commandModificationBlockLimit` (32,768 by default), counted on the volume of the box.
- Biomes live in 4×4×4 cells: the box is rounded to these cells.
- The `replace` filter accepts a biome tag.
- The server sends the modified biomes back to clients (`ClientboundChunksBiomesPacket`). ATMOSPHERICS' mist, read per biome client-side, therefore changes without reconnecting.

### 1.13 KubeJS 2101.7.2 [jar]

`NativeEvents.onEvent(class, callback)` is bound for all script types (`BuiltinKubeJSPlugin`). Exposure's `FrameAddedEvent` and the Wildex events can therefore be listened to without a custom mod.

---

## 2. Concrete links to build

### 2.1 Ancient Remnants ↔ NML biomes (config)

In `config/obscuria/ancient_remnants-common.toml`, **replace** the default lists. Copy the exact key names from the file generated at first launch; only the spacing keys were noted word for word.

| Monument | Proposed biomes | Reason |
|---|---|---|
| hunter_monolith | `#minecraft:is_badlands`, `minecraft:desert`, `nomansland:prairie`, `minecraft:savanna`, `minecraft:savanna_plateau` | Wide open spaces: the blessing (shot range) makes sense there |
| warrior_monolith | `minecraft:ice_spikes`, `minecraft:snowy_plains`, `minecraft:snowy_slopes`, `minecraft:grove` | Keeps the cold theme, while making it findable |
| sentinel_monolith | `minecraft:deep_frozen_ocean`, `minecraft:deep_cold_ocean` (+ `nomansland:frozen_shore` to test) | The monolith is underwater; `frozen_shore` is only interesting if the structure holds on a beach |
| cherry_monument | `minecraft:cherry_grove`, `nomansland:lavender_field`, `minecraft:flower_forest`, `minecraft:meadow` | The cherry grove alone is too rare with NML |
| oakwood_monument | explicit list: `minecraft:forest`, `minecraft:birch_forest`, `minecraft:flower_forest`, `nomansland:old_growth_forest`, `nomansland:old_growth_forest_edge`, `nomansland:maple_forest`, `nomansland:autumnal_forest` | `#minecraft:is_forest` would include `nomansland:dark_swamp` (swamp, `c:is_spooky`) |
| coniferous_monument | `#minecraft:is_taiga`, `nomansland:boreal_forest`, `minecraft:grove` | `boreal_forest` is in `is_forest` at NML, not in `is_taiga` |

- Leave `nomansland:old_growth_forest_clearing` to NML's natural clearings [deduced].
- **Spacing:** go from 100/20 to **64 to 80 / 16 to 24**, in AR's config and not in Sparse Structures (§1.3). For an "almost empty world" map, 100 chunks (1,600 blocks) between two monuments of the same type is very far. There are six types: at 72 chunks, one meets a monument of a given type about every 1,150 blocks [deduced].
- **Useful side effect:** the codex pages read this same tag (`"biomes": "ancient_remnants:has_structure/<id>"`). Fragments drawn on a mob's death will therefore automatically follow the new biomes [page JSON].

### 2.2 The guardians (breezes): to decide

Finding: `00_fallow_constants.js` bans `minecraft:breeze`, and `spawns_safety.js` cancels `EntityEvents.spawned` for any banned entity in the Overworld. **The monuments' spawners will therefore run empty.** Three options:

- **A. Keep the breezes, only there** (recommended if a guardian is wanted):
  - in `spawns_safety.js`, let the breeze through when it comes out of a spawner (`event.entity.persistentData` cannot be used here; test `entity.spawnType === 'SPAWNER'`, or an Elderheart within 32 blocks) [deduced];
  - on the In Control side, rule `{"mob":"minecraft:breeze","spawner":true,"structure":"ancient_remnants:…","result":"allow"}` placed before the denials.
  - A breeze is a Trial Chamber mob (wind, jumping): 4 at a time is a real fight early in the game.
- **B. Replace the spawner**: override `data/ancient_remnants/worldgen/processor_list/monument.json` (and `monolith.json`) by adding a `minecraft:rule` processor, `output_state` spawner with `block_entity_modifier` `minecraft:append_static`, to put another mob in `SpawnData`. Candidates:
  - a themed guardian from Mowzie's or Companions!;
  - more simply, **no guardian**, replacing the spawner with a decorative block.

  Plan a test, since `append_static` merges and does not replace (deduced).
- **C. No guardian**: accept the empty spawner (it costs almost nothing) or remove it with a `block_ignore` of `minecraft:spawner` in the processor_list. It is the most faithful to the "almost empty world, without horror".

The recommendation is **C**, or **B** with a discreet mob. A vanilla breeze clashes with the art direction and contradicts the exclusion list.

### 2.3 Clearing triggered by the Elderheart (KubeJS)

The Elderheart emits no event (§1.3): it is detected by polling.

```js
// server_scripts/clairieres_elderheart.js — sketch
ServerEvents.tick(event => {
  if (event.server.tickCount % 100 !== 0) return
  event.server.players.forEach(p => {
    if (String(p.level.dimension) !== 'minecraft:overworld') return
    const box = p.boundingBox.inflate(16)
    p.level.getEntitiesWithin(box).forEach(e => {
      if (String(e.type) !== 'ancient_remnants:elderheart') return
      if (e.tags.contains('fallow_eveille')) return
      e.addTag('fallow_eveille')                    // only once per monument
      global.fallowOuvrirClairiere(p.level, e.blockPosition(), 32, p)
    })
  })
})
```

- `global.fallowOuvrirClairiere` takes over the loop from `clairieres.js`, to be factored out.
- The entity tag is saved with the entity: the clearing opens only once [deduced].
- Radius 32: same tier as the lantern. Own message: "La brume se retire autour du monument."
- Cost: one entity search in a 33-block box per player, every 5 s. It is negligible.

### 2.4 Clearings: rewrite the draft `clairieres.js`

**Alignment and height**

- Round the centre and the columns to multiples of 4: `x0 = Math.floor(c.x/4)*4`.
- Move to **8×8 columns over the whole useful height**: from `c.y − 16` up to 319. An 8 × 336 × 8 column is 21,504 blocks, under the limit of 32,768.
- Reason: with the current draft (−24/+40), the mist returns for a player standing on a hill 50 blocks above the fire, since the fog depends on the biome at the camera's position [deduced].
- The `replace` filter protects underground biomes, which have other ids.

**Three biome variants, for Ecliptic and snow**

| Biome | Temperature | Replaces |
|---|---|---|
| `fallow:clairiere_froide` | ≈ 0.0 | `#fallow:clairiere_source_froide` = `#c:is_cold/overworld` + cold `#minecraft:is_taiga` (snowy_taiga, grove…) |
| `fallow:clairiere` | 0.7 | `#fallow:peut_devenir_clairiere` |
| `fallow:clairiere_chaude` | ≈ 0.95, `has_precipitation` true | `#fallow:clairiere_source_chaude` = land `#c:is_hot/overworld` |

- Order of passes for each column: cold, then hot, then temperate. Each pass only touches its filter; this goes from 4 to 12 commands per tick (deduced, low cost).
- Without these variants, a clearing in the snowy taiga no longer has snow in winter, and a clearing in the hot prairie changes agricultural climate.

**Tag `fallow:peut_devenir_clairiere` to complete**

- Add: `#minecraft:is_forest` (already present), `#minecraft:is_taiga`, `#c:is_plains`, `#c:is_swamp` **except** `blackwater_river`, `nomansland:old_growth_forest_clearing` and `nomansland:mud_beach`.
- Exclude: all rivers, beaches, oceans, `#nomansland:caves`, `nomansland:alchemist_ruins` and `nomansland:dream`.
- A tag cannot exclude. An **explicit list** is therefore needed rather than `#c:is_swamp`, which contains `blackwater_river` [NML JSON].

**Spawns in the clearing**

- Do **not** put the clearings in `#c:is_forest` or `#minecraft:is_forest`: they would receive NML's deer and the AR monuments. This is not necessarily wanted.
- Add them to the chosen `naturalist:has_<mob>`, for example butterfly, firefly, rabbit, hedgehog, birds. This is how they become "verdant".
- They only take the spawns in small doses: CREATUREs appear mostly at chunk generation, then only in slow passes, every 400 ticks [deduced].
- **In Control**: rule `{"biome":["fallow:clairiere","fallow:clairiere_froide","fallow:clairiere_chaude"],"hostile":true,"result":"deny"}`. A biome without a `monster` list is enough for vanilla, but not for mods that spawn through their own code (Mowzie's, lunar events…).

**The rest**

- **ATMOSPHERICS**: one preset per clearing variant (light mist).
- **Ancient Remnants**: add the three clearings to the `has_structure` tags **only** if fragments are also to drop in the clearings. Otherwise, they "cut" the fragments [deduced from §1.3].

**A better approach?** The biome is the only key read at once by ATMOSPHERICS' mist, spawns, seasons and the map. `/fillbiome` therefore remains the right tool: written once, no continuous cost. The only flaw is the edge in 4-block steps. **A small custom mod** would only be justified for a **gradient edge**: a client mixin on the fog calculation that interpolates according to the distance to the centre of the known clearings (list synchronised by the server). It is optional, and to be reserved for the finish.

### 2.5 Zoo: moving, keeping, feeding

**Moving, without Capture Nets**

| Means | For whom | Evidence |
|---|---|---|
| Lead | most land animals; **not** the dragonfly nor C&C's aquatic ones, nor Mowzie's sculptor | `canBeLeashed` [jars] |
| Boat | small animals only: the width must be smaller than the boat's, and no aquatic animal | [deduced, vanilla code] |
| **Create seat** on a Steam 'n' Rails train or a Sable airship | **any animal**, even the elephant or the giraffe; hold the animal on a lead then right-click the seat | `SeatBlock.canBePickedUp` + `sable:retain_in_sub_level` [jars] |
| Icy's cart (4 or 6 seats) | passives, plus neutrals and hostiles depending on the `cart_pickup` config | [jar 2.1.4]: update from 2.0.1 |
| "Follow" + teleportation | tamed animals (vanilla, Naturalist, C&C, Companions!…), helped by Companion | `follow.petForceTeleportingWhenChunkUnload` [jar] |
| Bucket | Naturalist's bucketable species (§1.10) and vanilla fish | [jar] |

- Do **not** put animals in minecarts on an airship: minecarts are destroyed when leaving Sable's area (`destroy_when_leaving_plot`).
- **Hybrid Aquatic has no bucket.** Two options:
  - leave its species out of the zoo: they only count for the bestiary, by spyglass or by photo;
  - or create a KubeJS "vivarium bucket" item limited to Hybrid's small fish: `ItemEvents.entityInteracted`, saving the NBT in the item, then respawning on clicking water. It is feasible, but amounts to a mini-capture: to decide [deduced].

**Keeping (persistence)**

- The enclosure plaque (§2.6) calls `entity.setPersistenceRequired()` on each registered animal [deduced: vanilla `Mob` method].
- A name given with a name tag also works.
- It was not reliably checked which species disappear on their own: see §5.

**Feeding.** The Wildex sheets already give the husbandry food. Nothing to build, except making sure that Naturalist's and AM's foods exist in Fallow's food chain (report 2, Kaleidoscope).

**Escapes and fights**

- **Predators**: lion (`lion_hostiles` includes the horse), tiger, bear, alligator at Naturalist; grizzly, tiger, crocodile, anaconda at AM.
- Either separate enclosures; or a datapack override of the `naturalist:*_hostiles` tags to remove the horse, the donkey and the farm animals from them (deduced).
- Keep hunting against wild fauna, so that the world stays alive.
- **Likely escapes**:
  - flyers: Naturalist's birds, vulture, butterflies, dragonflies, glare, AM's blue jay, etc. A closed aviary is needed;
  - climbers and jumpers: lizards? C&C's jumping spider;
  - NML's frienderman (enderman variant: possible teleportation);
  - large animals facing simple fences (giraffe).
- All this is to be tested (§5).

### 2.6 Wildex ↔ zoo: the "enclosure plaque" (KubeJS)

Wildex does not recognise enclosures (§1.4): the goal is built alongside.

1. **Memorise the discoveries** (server_scripts, re-registered at each reload):

   ```js
   const Bridge = Java.loadClass('de.coldfang.wildex.integration.kubejs.WildexKubeJsScriptBridge')
   Bridge.onDiscoveryChanged(d => {
     const p = Utils.server.getPlayer(d.playerUuid); if (!p) return
     const set = p.persistentData.getCompound('fallow_wildex'); set.putBoolean(d.mobId, true)
     p.persistentData.put('fallow_wildex', set)
     global.fallowPaliersBestiaire(p, d.discovered)       // milestones of 25 / 50 / 100 / 150 species
   })
   ```

   The form of the callback (Java interface or function) remains to be checked at the first attempt. It is also possible to go through `NativeEvents.onEvent('de.coldfang.wildex.api.event.WildexDiscoveryChangedEvent', e => …)`.
2. **Block `fallow:plaque_enclos`** (startup: `StartupEvents.registry('block')`). On right click (`BlockEvents.rightClicked`):
   - list the `Animal`s within a radius of 24 blocks;
   - keep those that are leashed, named, tamed or already registered;
   - make them persistent;
   - count the **distinct species** already discovered in Wildex (`fallow_wildex`);
   - store the result in the world data (`server.persistentData`), per plaque.
3. **Zoo score** = number of distinct species across all of the player's plaques.
   - Wild species that cannot be tamed (deer, moose, bison, giraffe…) are worth more, since they have to be transported (§2.5).
   - Bonus if the plaque is in a clearing: `level.getBiome(pos)` is `fallow:clairiere*`.
4. **Rewards** (score or `discovered` milestones):
   - AR blessing chosen through `/ancient_remnants blessing set`;
   - engineer blueprints (`plans_ingenieur.js`);
   - lantern or dissipator tier (§10.3);
   - titles and chapter advancements.

   The plan provides a lantern at 25 species (§10.10): to keep.

### 2.7 Exposure ↔ Wildex ↔ Ancient Remnants

- **Exposure → Wildex**: nothing to build (`exposureDiscoveryEnabled = true`). Discovery happens when the picture is taken.
- **Exposure → AR**: `NativeEvents.onEvent('io.github.mortuusars.exposure.neoforge.api.event.FrameAddedEvent', e => …)`. If the frame's STRUCTURES `extraData` contain `ancient_remnants:<monument>` and the photographer does not yet have that page, they receive an `ancient_remnants:research_scrap[ancient_remnants:stored_research=["ancient_remnants:<monument>"]]`.
  - It must be verified that the event is indeed raised server-side. Wildex listens to it to discover mobs, so it is probable [deduced].
  - It is **the** "explore, photograph, understand" loop of the vision.
- **Exposure → advancements**: `exposure:frame_exposed` with `entities_in_frame` for photo challenges (bear, whale, Elderheart), without a script.

### 2.8 Codex fragments in loot (LootJS or datapack, Lootr-compatible)

Datapack without a script, with `neoforge:add_table` (GLM type present in NeoForge 21.1; deduced, to confirm in the NeoForge jar):

```json
{ "type": "neoforge:add_table",
  "conditions": [
    { "condition": "neoforge:loot_table_id", "loot_table_id": "nomansland:chests/sniffer_monument" },
    { "condition": "minecraft:random_chance", "chance": 0.35 } ],
  "table": "ancient_remnants:random_research_scrap" }
```

This file is to be declared in `data/neoforge/loot_modifiers/global_loot_modifiers.json`. The equivalent can be done with LootJS in `loot/butin.js`: `event.addTableModifier('nomansland:chests/sniffer_monument').addLoot(…)`.

Where to inject:

| Source | Chance | Why |
|---|---|---|
| `nomansland:archaeology/alchemist_ruins` | 25% | Archaeology: slow gesture, consistent; Lootr converts suspicious gravel, so each player has their share |
| `nomansland:archaeology/well` | 15% | same |
| `nomansland:chests/sniffer_monument`, chests of the menhirs and creature monuments | 30 to 40% | enigmatic places |
| `nomansland:chests/alchemist_pot_*` | 5% | the jars refill: low chance, otherwise they can be closed in a loop |
| `nomansland:chests/water_well` | 10% | |

- **Fallow pages**: add entries `data/fallow/ancient_remnants/research/<id>.json` (text and 350×210 texture), with `biomes` pointing to NML tags. The codex then also tells the story of Fallow's world; the same tables distribute them.
- **Recipe `paper_from_research_scrap`**: remove it (`ServerEvents.recipes` → `event.remove({id:'ancient_remnants:paper_from_research_scrap'})`), since it destroys the goal [deduced].
- Keep the 0.2% drop on killed mobs: it is a rare bonus.

### 2.9 Engineer blueprints and NML / Lootr loot

- The blueprints of `plans_ingenieur.js` are read on right click and consumed. With Lootr, each player finds their own copy in the same chest: this is good for a server.
- Hook into `loot/butin.js`:
  - `fallow:plan_atelier` in `nomansland:chests/small_dungeon_core` and `redstone_parts`;
  - `fallow:plan_vapeur` in `sniffer_monument`.
- Lootr: `loot_table_blacklist` empty; do not set `decay`. Enable `refresh` only for NML's small dungeons, if renewable loot is wanted.

### 2.10 Antique Atlas ↔ ruins (resource pack + config)

- **Resources** in a resource pack of the pack (folder `resourcepacks/` or KubeJS `assets/`):
  - `assets/nomansland/atlas/structure/start/<id>.json` for bell_sanctuary, menhirs, creature_monuments, alchemist_ruins and deep_well;
  - `assets/ancient_remnants/atlas/structure/start/<id>.json` for the six monuments, each with its own marker;
  - `assets/nomansland/atlas/biome/<biome>.json` so that the 24 NML biomes have tiles, otherwise the atlas draws them with a default tile.
- Also create tiles for the three `fallow:clairiere*`, so that the clearings show on the map. This rewards the "lifted mist" map [deduced].
- **Add AA4 Atlas 1.1.2**: the atlas becomes an item (book + compass), with `requireItem = true`, and Antique Atlas goes from `side = "client"` to `both`, to be checked. A map that has to be crafted and carried fits the vision.
- **Surveyor**: `globalSharing = false` (everyone discovers their own world); `discoveryMessages = true` for monuments ("Vous découvrez…"); `playerDeathWaypoints = true`.

### 2.11 Fauna: settling duplicates without removing a mod

One version per species and per biome, according to 0003:

| Species | Keep | Cut the spawning of | Tool |
|---|---|---|---|
| Deer | NML (NML biomes) | Naturalist `deer_removed` or `naturalist:has_deer` emptied | config / tag |
| Moose | AM, for lack of an NML spawn found | — | check in game |
| Goose | NML (rivers, swamps) | Duck Mod: keep the duck, cut the goose (biome modifier `neoforge:remove_spawns`) | datapack |
| Land tortoise | Naturalist | NML tortoise (biome modifier) | datapack |
| Tiger, elephant, rhinoceros, komodo | Naturalist | AM, through its spawn config species by species | AM config |
| Bear | Naturalist (bear, black_bear) | AM grizzly | AM config |
| Sharks | Naturalist great_white + Hybrid | AM hammerhead (and frilled_shark if Hybrid has the equivalent) | config |
| Orca | Hybrid or AM, according to the model chosen | the other | config |
| Blobfish | Naturalist (bucketable) | AM, Hybrid | config |
| Crab | Naturalist (beaches) + F&F (already classed "peaceful") | Hybrid crab | Hybrid config |
| Otter | C&C | Hybrid otter | config |
| Snail, dragonfly | C&C | Naturalist | Naturalist config |
| Catfish, blue jay, rattlesnake, lobster, giant squid | only one (Naturalist or Hybrid) | the other | config |
| Mammoth (Naturalist) | — | `mammoth_removed` (prehistory ruled out in 0003) | config |

- **Cut** = natural spawning cut. The entities remain in the registry, hence in Wildex's total: **each species cut must enter `excludedModIds` or a list of excluded ids**, otherwise completion is impossible.
- **Wildex exclusion list** to write:
  - the vanilla mobs of `bannedVanilla`;
  - Nether and End (blaze, ghast, piglin, hoglin, strider, magma_cube, wither_skeleton, zoglin, piglin_brute, ender_dragon, shulker, endermite, wither, warden);
  - the cut F&F hostiles (iceologer, illusioner, wildfire);
  - the disabled Companions! bosses;
  - the duplicates cut above.

  `keepCompletionAfterNewMobs = false` can stay.

### 2.12 Wildex settings to write (common config)

| Key | Value | Reason |
|---|---|---|
| `shareOffersPaymentEnabled` | false | no emerald economy in Fallow |
| `newEntryXpAmount` | 5, or 0 if XP is used elsewhere (Icy's carts, enchantments) | |
| `spyglassDiscoveryRange` | ≈ 48 | 98 blocks is discovering without approaching |
| `hiddenMode`, `requireBookForKeybind`, `giveBookOnFirstJoin` | true | default |

Remove the Analyzer recipe (`ServerEvents.recipes`): discovering a mob by dropping an item short-circuits observation. It can also be locked behind a zoo milestone.

---

## 3. Conflicts and risks

1. **Breezes against `bannedVanilla`** (§2.2): monuments without guardians, or else a vanilla mob outside the art direction. To decide.
2. **Spawners of NML's small dungeons** (spider, skeleton, zombie): the In Control rule `"mob":[…zombie…skeleton…spider…],"result":"deny"` and `spawns_safety.js` cancel them. The spawners will therefore run empty [deduced].
   - This is consistent with "no vanilla monsters".
   - They can also be replaced by a processor, on the model of §2.2 B, with a Fallow hostile.
3. **Sparse Structures** spaces out NML (sets with a `spacing` key) **but not AR**. With `spreadFactor 3` (§02), NML's small dungeons go from 10 to about 30 chunks, and the alchemist ruins from 50 to about 150 chunks, i.e. 2,400 blocks. They risk becoming too rare [deduced].
   - Put an exception `nomansland:*` in Sparse Structures;
   - or a factor of 1.5 to 2.
4. **Hybrid Aquatic adds biomes**, contrary to the criterion of 0003. Its `tropical_river` replaces `minecraft:river`: tropical rivers in the middle of the taiga? NML has its own rivers.
   - Set `generateTropicalRiver = false` at the minimum.
   - Evaluate `enableBiomes = false`. The reefs are lost: do the reef species still spawn in `warm_ocean`? To test.
5. **NML fog against ATMOSPHERICS**: two mist systems on the same biomes (`has_dense_fog`). Set `fogModifiers = false` in NML, or its intensities to 0, and leave ATMOSPHERICS sole master. A clearing is not in `has_dense_fog`: NML's mist stops there too. But if both systems remain active, neighbouring biomes accumulate both mists, and the contrast at the edge of the clearing becomes hard to set (deduced).
6. **NML's Friend Moon against Enhanced Celestials**: two moon systems. Keep the Friend Moon (talking moon, dreams) as a mystery, or cut it with `friendMoonDimensions = []`. Check that it does not trigger on the same night as a white moon.
7. **Icy's horse registry displays coordinates**, which contradicts the "map to make yourself" spirit. It is minor; cut it if the config allows, to check.
8. **NML jars that refill**: it is a loot farm if fragments or blueprints are put in them. Low chances (§2.8).
9. **Wildex's Spyglass Pulse**: no switch. It reveals mobs within 32 blocks, but only after completion: it can be accepted as a final reward. Otherwise, a small custom mod with a mixin that cancels the action (five lines).
10. **Wildex's Analyzer** is a shortcut (§2.12).
11. **Warrior blessing almost null**: it counts the `Monster`s within 12 blocks, and Fallow has very few. Replace its content by datapack (`data/ancient_remnants/tags/ancient_remnants/blessing/warrior_monolith.json`, `replace: true`) with another existing blessing. A new one cannot be created without a mod [jar].
12. **Single, replaced blessing**: visiting a second monument overwrites the first. This permanent choice must be **announced** to the player (codex text or KubeJS message), otherwise it is frustrating [jar + deduced].
13. **Clearings and fragments**: the clearings take areas out of the page tags, so they no longer give fragments on mobs (§2.4).
14. **Predators against horses and livestock**: `lion_hostiles` contains `minecraft:horse` (§2.5).
15. **In Control 10.3.0 is in beta**, to be watched. The caps in `spawn.json` all have `perplayer: true`, which is good.
16. **Updates**: NML 1.6.1 and Icy's 2.1.4 change world generation or content. Do them **before** fixing the permanent world.

---

## 4. What to adapt, remove or add in the plan

| Where | Change | Reason |
|---|---|---|
| §11.8 | **Remove** "fragments left by a wandering trader"; **replace** with: fragments in NML archaeology and chests (§2.8), monument photo (§2.7), rare 0.2% drop | the trader does not exist [jar AR] |
| §10.3 / `clairieres.js` | columns aligned on 4, full height (`c.y−16` to 319); 3 variants; explicit filter tag with the NML biomes; Elderheart trigger | §2.3 and §2.4 |
| `clairiere.json` | add `clairiere_froide` and `clairiere_chaude`; add the chosen Naturalist tags; anti-hostile In Control rule | Ecliptic, spawns |
| `00_fallow_constants.js` / `spawns_safety.js` | decision on the breezes (§2.2) | direct conflict with AR |
| §10.10 and new script | `zoo.js` → enclosure plaque + Wildex bridge; `wildex_bestiaire.js` for the milestones | Wildex has no enclosures |
| §02 (Sparse Structures) | exception or reduced factor for NML; **remove** the idea of spacing AR through Sparse Structures; AR spacing in its own config | §1.3, §3.3 |
| `loot/butin.js` | fragments (`random_research_scrap`) and blueprints in the NML tables listed in §2.8 and §2.9 | |
| Recipes | remove `ancient_remnants:paper_from_research_scrap` and the Wildex Analyzer recipe | long-term goals |
| AR config | biomes and spacing (§2.1); warrior blessing tag (§3.11) | |
| Wildex config | exclusions (§2.11) and settings (§2.12) | completion possible |
| Hybrid config | `generateTropicalRiver = false`, and `enableBiomes` to evaluate | 0003 |
| NML config | `fogModifiers` off (ATMOSPHERICS alone); decision on the Friend Moon | §3.5, §3.6 |
| Fauna configs | duplicates table (§2.11) | 0003, one version per species |
| Pack: **add** | Lootr, In Control!, Exposure, Wildex, AR + Fragmentum, Enhanced Celestials, LootJS (all planned), **AA4 Atlas** | atlas as an item |
| Pack: **update** | NML 1.6.1, Icy's 2.1.4 (zoo carts), F&F 5.0.2, Ecliptic 0.15.4.1 and the minor versions of §1.1 | before fixing the world |
| Resource pack | Antique Atlas markers (NML, AR) and tiles for NML biomes and clearings | §2.10 |
| Custom mod (optional) | 1) gradient clearing edge (client mixin on fog); 2) cutting the Spyglass Pulse | only if the finish requires it |

No custom mod is **necessary** for these links: config, tags, datapack, resource pack and KubeJS are enough.

---

## 5. Questions that call for an in-game test

1. The exact keys of the biome list in `ancient_remnants-common.toml`, and whether a tag (`#minecraft:is_badlands`) is accepted there.
2. Where the monuments land in NML biomes: a 7-piece jigsaw monument on NML terrain, a sentinel monolith in `frozen_shore`.
3. `spawns_safety.js`: `event.cancel()` on a spawner breeze; does the spawner keep running empty? Is an `entity.spawnType` needed for the exception (§2.2 A)? Does the `append_static` override replace `SpawnData` (§2.2 B)?
4. Elderheart polling: does `getEntitiesWithin` find it? Does the `fallow_eveille` tag survive chunk unloading?
5. `/fillbiome` launched by `runCommandSilent`: is the return of the biomes to the client immediate (ATMOSPHERICS mist)? How long for a radius of 64 at full height? (about 200 columns × 3 passes, at 4 columns per tick, i.e. 50 ticks)
6. Ecliptic in the three clearings: does snow fall and melt as in the original biome? Do crops grow at the same pace?
7. Wildex's KubeJS bridge: the exact form of the callback accepted by `onDiscoveryChanged` (JS function or interface); does it hold after `/reload`?
8. Exposure `FrameAddedEvent`: is it raised server-side? Does `STRUCTURES` contain `ancient_remnants:*` when photographing a monument up close?
9. Lootr: does it convert the suspicious gravel of the NML ruins placed by `append_loot`? Do the GLMs apply properly to each player's draw?
10. Create seat: does an elephant or a giraffe on a train or Sable airship seat stay seated in flight and on chunk unloading?
11. Icy's cart 2.1.4: does it pick up Naturalist's and AM's animals? Which sizes?
12. Disappearance: which species (Naturalist, AM, C&C, NML) disappear on their own when one moves away, and does `setPersistenceRequired()` suffice?
13. Escapes: flyers, climbers, frienderman; fence height sufficient for giraffe and elephant.
14. Fights: predators against livestock in a dense zoo; does the override of the `*_hostiles` tags work?
15. Hybrid with `enableBiomes = false`: do its reef species still spawn?
16. NML fog off: does ATMOSPHERICS alone give the right mist in `has_dense_fog`?
17. NML moose: does it spawn anywhere? No biome modifier was found.
18. Friend Moon and Enhanced Celestials' white moon: coexistence on the same night.
19. Sparse Structures with `spreadFactor 3`: real density of NML ruins on a 10,000 × 10,000 map (Chunky + Antique Atlas).
20. Antique Atlas under Connector: does it read the markers `assets/<ns>/atlas/structure/start/` for NeoForge structures (NML, AR)? Does AA4 Atlas make the item mandatory?
