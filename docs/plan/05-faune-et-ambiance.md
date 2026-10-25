# 5. Wildlife, ecology and ambience

> **History (plan v1, 2026-10-10).** Superseded by chapters [10](10-recentrage.md) to [12](12-verification.md), the [registry](../mods.md) and the [decisions](../decisions/README.md): several mods listed here have since been dropped. Kept for its reasoning.

What already exists (decisions 0003 and 0007): Naturalist, Alex's Mobs Continued, Hybrid Aquatic, Mowzie's Mobs, six companion mods and Companion; ATMOSPHERICS, AmbientSounds, Sound Physics Remastered, Subtle Effects, LambDynamicLights, More Darkness. The bestiary is more than sufficient. What is missing is the **chain that removes vanilla monsters**, the **control of modded creatures** once the space is freed, and a few touches of ambience.

Details, evidence and keys: [recherche/research-faune-ambiance.md](recherche/research-faune-ambiance.md).

## 5.1 Removing vanilla monsters

They arrive by six routes; each has its countermeasure.

| Route | Examples | Countermeasure |
|---|---|---|
| Biome spawn lists | zombie, skeleton, creeper, spider, enderman, witch, slime, drowned, bogged | Biome modifier `neoforge:remove_spawns` on `#minecraft:is_overworld` |
| Structure `spawn_overrides` | guardians, swamp hut witch, outpost pillagers | In Control! rules + KubeJS safety net |
| Spawners | dungeons, mineshaft spiders, trial spawners | `neoforge:remove_features` on `monster_room` and `monster_room_deep`; trial chambers removed |
| Gamerules | phantoms, patrols, raids, warden, trader | `doInsomnia false`, `doPatrolSpawning false`, `disableRaids true`, `doWardenSpawning false`, `doTraderSpawning false` |
| Conversions | villager struck by lightning → witch, skeleton horse trap, Friends & Foes `enableZombieHorseTrap` | KubeJS safety net + F&F config |
| Entities placed at generation | mansion illagers, zombies in abandoned villages, silverfish in infested blocks | KubeJS safety net; `remove_features` on `ore_infested` |

Three layers, from the cheapest to the most fine-grained:
1. **Datapack** (`kubejs/data/fallow/neoforge/biome_modifier/`): removal of spawns, dungeons and infested blocks. To neutralise another mod's biome modifier without touching its config, its file is overridden with `{"type": "neoforge:none"}`.
2. **In Control!** (new, core, server only) for the fine-grained rules.
3. **KubeJS safety net**: any banned vanilla entity entering the Overworld is cancelled, unless it carries the `fallow_allowed` tag (summoned by an admin).

## 5.2 Controlling modded creatures

**Major side effect**: without vanilla monsters, the entire hostile cap (70) goes to modded creatures: Mowzie's creatures, Alex's Mobs centipedes and Gusters, Friends & Foes Maulers and Barnacles, Hybrid Aquatic sharks. Without a ceiling, "rare creatures" form hordes.

Proposed In Control! rules (`config/incontrol/spawn.json`, exact semantics to be confirmed):
- no hostile modded creature at a **light level ≥ 8**: the base is literally an island of light;
- no Mowzie's anomaly **within 40 blocks** of a player: they are glimpsed in the distance, in the mist;
- at most **2 Mowzie's creatures per player**;
- at most **8 hostiles per player** in total.

To space out territorial predators (bears, lions, tigers, sharks), the `neoforge:add_spawn_costs` biome modifier gives each individual a "charge" that repels its fellows.

## 5.3 One species, one mod

Duplicates to cut (full per-biome table in the research notes):

| Species | Keep | Cut | Known key |
|---|---|---|---|
| Duck | Untitled Duck Mod | Naturalist | `disable_mobs.duck_removed = true` |
| Elephant | Alex's Mobs (mount with a chest: a caravan) | Naturalist | `disable_mobs.elephant_removed = true` |
| Bear | Naturalist | Alex's Mobs grizzly | to be read in the AM .jar |
| Crocodilians | Naturalist alligator | AM crocodile | same |
| Rattlesnake, rhinoceros, catfish, blue jay | Naturalist | AM | same |
| Hammerhead shark, squid, anglerfish | Hybrid Aquatic | AM; Naturalist 2.x anglerfish | same |
| Penguin | Friends & Foes (or Naturalist 2.x if it adds one) | the other | — |

**Alex's Mobs creatures to cut for art direction**: Murmur, Farseer, Skelewag, fly, cockroach, and all End creatures (Void Worm, Mimicube, Cosmaw, Enderiophage, Spectre…). Nether ones leave with it if the Nether is closed ([3.8](03-civilisation.md#38-the-nether-closed-by-default)).

**Friends & Foes** (keys read from the source code):
- to cut: `enableIceologer`, `enableIceologerSpawn`, `enableIceologerInRaids`, `generateIceologerCabinStructure`, `enableIllusioner`, `enableIllusionerInRaids`, `generateIllusionerShackStructure`, `generateIllusionerTrainingGroundsStructure`, `enableWildfire`, `generateCitadelStructure`, `enableZombieHorseTrap`, `enableBeekeeperVillagerProfession`, `generateBeekeeperAreaStructureInVillages`, `generateCopperGolemWorkstationStructureInVillages`;
- to tune: `crabSpawnWeight` 14 → 6, Moobloom 4 → 2, `maulerSavannaSpawnWeight` 32 → 8, `maulerBadlandsSpawnWeight` 16 → 6, `maulerDesertSpawnWeight` 8 → 4, Barnacle 4 → 2;
- to note: the **Tuff Golem** only appears in strongholds, which are removed. It needs another source (ruins, loot) or must be accepted as unobtainable.

**Mowzie's Mobs** (keys to be confirmed in the .jar): Lantern and Grottol rare but present; Foliaath in jungle only; Naga on coastal cliffs (×0.3); Frostmaw ×0.2; structures (Wroughtnaut, Sun Temple, Sculptor) go through Sparse Structures. The Umvuthana (hostile masked tribes) are an art-direction decision.

**Adorable Hamster Pets**: diamond-sniffing to be disabled if the config allows. **Companions!**: hostile creatures, weapons and bosses disabled or made very rare (0003).

## 5.4 Resources lost along with the monsters

To be replaced in KubeJS **before opening the server**:

| Resource | Why it matters | Proposed replacement |
|---|---|---|
| **Slime ball** | Create's Super Glue, Sticker and sticky piston: essential for Aeronautics; leaves survivors | Create mixing (kelp + flour + water); to check: can Alex's Mobs' *banana slug slime* be used? |
| **Blaze** | Create's Blaze Burner | See the Nether ([3.8](03-civilisation.md#38-the-nether-closed-by-default)) |
| Gunpowder | TNT, rockets | Create mixing: charcoal, saltpetre (washed gravel), sulphur |
| String | Bows, fishing rods | Crushed wool (Create); Supplementaries flax |
| Bone | Bone meal, agriculture | Added to the loot of large Naturalist and AM animals |
| Phantom membrane | Slow falling potion | Check whether an Aeronautics recipe uses it |

A pass over the recipes of every .jar (`slime_ball`, `blaze_rod`, `gunpowder`, `ender_pearl`, `phantom_membrane`, `string`, `bone`) will size the list.

## 5.5 Ambience: what we add

| Mod | Effect | Availability | Proposed status |
|---|---|---|---|
| **Cosy Critters & Creepy Crawlies** | Particle birds that land and fly off as you approach; moths around lamps; small web spiders. Silhouettes in the distance, base = island of light, no server cost | yes (client) | recommended |
| **Enhanced Celestials** | Harvest moon (growth), blue moon; **blood moon cut**; a home-made "Pale Moon" as a datapack (washed-out sky, more frequent Lantern and Ghost, no extra hostiles) | yes (6.0.2.5; v1 is archived, check v2) | recommended |
| **Simple Voice Chat** | Proximity voice; Sound Physics reverberates voices in valleys and caves | yes | recommended ([6](06-technique-et-serveur.md)) |
| **Reactive Music** (replaced by True Adaptive Music, [0010](../decisions/0010-musique.md)) | Replaces Presence Not Position (not found) and Biome Music (Fabric only). Music chosen by place, time, danger, indoors or outdoors. Its fantasy and Celtic tracks do not necessarily suit Fallow: keep the rules, change the songpack or write a sober one | yes (1.3.5, client, uses YACL already present) | recommended; to be listened to in phase 6 |
| **Particle Rain** | Rain and snow as particles, storm mist | already "to test" | to test, Ecliptic keeps the weather |
| **Simple Clouds** | Storms that darken the horizon, visible from afar | yes (0.7.3) | dedicated branch, **client only** (otherwise conflicts with Ecliptic); compute shaders, probably no macOS |
| Longwings | 70 butterflies to collect, manual, nets | yes | optional (duplicates Naturalist butterflies) |

**Ruled out**: Weather2 (takes control of the weather, conflicts with Ecliptic), Fowl Play (the author warns of a risk of world corruption), Whisperwoods (no 1.21.1 version, to watch: it is the ideal art direction), Wisp & Willow (magic system), Dynamic Surroundings (duplicates AmbientSounds), Spawn Balance Utility (Fabric only), True Darkness Biomes and Truly Dark (duplicates of More Darkness).

**Worth knowing**: a "Presence Footsteps x Sable" patch exists for NeoForge 1.21.1 and makes footstep sounds play on contraptions. Presence Footsteps had been ruled out in 0007; this is new information if the decision is reopened.

## 5.6 Staging strangeness without adding a mod

- **AmbientSounds**: its ambiences are resource-pack JSON files, with conditions on biome, time, altitude, rain and light. We add **rare, unidentifiable distant sounds**: a metallic creak near ruins, a muffled bell in the marsh mist, a whale song far out at sea at night, a very distant train whistle. No jumpscares.
- **LambDynamicLights**: entity luminances through a resource pack. Mowzie's Lantern, Naturalist fireflies and the Glare really give off light: a mobile island of light in the pitch-black night of More Darkness.
- **ATMOSPHERICS**: fog presets per biome, day and night, shipped in `pack/config`. Do not enable "Improved Transparency" in Sodium.
- **Emptied structures**: monuments without guardians, outposts without pillagers, huts without a witch, villages without zombies. Silent ruins.

## 5.7 Summary of settings

| Mod | Setting |
|---|---|
| Naturalist | `duck_removed = true`, `elephant_removed = true`; do not enable `bird_removed` (cuts all birds). No spawn weight in the config: rarity goes through In Control! |
| Alex's Mobs Continued | Cut grizzly, crocodile, rattlesnake, rhinoceros, catfish, blue jay, hammerhead shark, Murmur, Farseer, Skelewag, fly, cockroach, End creatures (config format to be read in the .jar) |
| Friends & Foes | See 5.3 |
| Mowzie's Mobs | See 5.3 |
| Enhanced Celestials | Blood moon disabled; "Pale Moon" added |
| Subtle Effects | Cut superfluous effects (0007) and firefly duplicates |
| Entity Culling | **Risk**: breaks Aeronautics contraptions ([6.1](06-technique-et-serveur.md#61-alerts)) |
