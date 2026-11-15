# Research: Wildlife, rare creatures, spawn control and ambience (Compline, 1.21.1 NeoForge)

> Refocused after the project update: base wildlife and ambience are **already chosen** (Naturalist 2.0.4, Alex's Mobs Continued 2.2.2, Hybrid Aquatic 1.7.3, Mowzie's Mobs 1.8.2, Critters and Companions 2.7.0, Untitled Duck Mod, Friends&Foes 4.0.27, Adorable Hamster Pets, Companion, Companions!, Ghost; ATMOSPHERICS, AmbientSounds, Sound Physics Remastered, Subtle Effects, LambDynamicLights 4.8.11, More Darkness). They are not proposed again. This report covers what is missing:
> 1. removal of vanilla monsters and spawn control, with a per-biome table;
> 2. complementary rare creatures and ambience (favouring little-known mods);
> 3. weather, sky, music, curiosities;
> 4. concrete settings for the mods already chosen.
>
> **Limits of the research.** modrinth.com, its API and its CDN are blocked, so no .jar could be opened. Availability comes from search excerpts (Modrinth version pages, BoxToPlay, Mado, ModDex and ModpackIndex mirrors) and from public GitHub sources. The **shared web search quota (200) ran out** along the way. These topics could therefore not be verified and are marked "unverified": auroras and eclipses, torches that go out, Bosses of Mass Destruction, Tide, Extreme Sound Muffler. A later rerun can complete them.

---

## 0. Key takeaways

1. **Removing vanilla hostiles is a chain, not a setting.** There are six different spawn vectors:
   - biome spawn lists;
   - structure `spawn_overrides`;
   - spawners;
   - gamerules (patrols, phantoms, raids, warden, trader);
   - conversions (witch from lightning, skeleton horse trap);
   - entities placed at generation (mansions, zombie villages, monuments).

   Three layers are proposed: a **biome modifier datapack** (zero cost), **In Control!** for the fine-grained rules, and a **KubeJS safety net** that cancels anything that still gets through.
2. **Major side effect: the MONSTER cap (70) becomes entirely available to modded hostiles.** Concerned: Mowzie's Foliaath, Naga and Grottol, AM's centipedes, Guster and Rocky Roller, F&F's Mauler and Barnacle, Hybrid sharks… Without a ceiling, these "rare creatures" become hordes. They must be **capped per mod and per player** and **forbidden from spawning in light**. The base then remains literally an island of light.
3. **Resources disappear along with vanilla monsters**, and a replacement recipe is needed in KubeJS. The most critical:
   - the **slime ball**: Create's Super Glue, sticky piston, sticker, lead;
   - the **Blaze**: Create's Blaze Burner is filled by capturing a live blaze, and brass goes through heated mixing;
   - gunpowder;
   - string, partly covered by wool crushing in Create;
   - the phantom membrane.

   To be handled in subproject 3 before any server opening.
4. **Recommended additions:**
   - **In Control!** (spawn control);
   - **Cosy Critters & Creepy Crawlies**: flying birds that scatter, moths attracted to lights. This is exactly "silhouettes + island of light";
   - **Enhanced Celestials**: harvest moons and blue moon, blood moon disabled;
   - **Simple Voice Chat**: proximity voice, with Sound Physics reverberation;
   - **Presence Not Position**: adaptive music and titles on entering a place or base. Very little known, to be checked.
5. **To test on a branch:**
   - **Simple Clouds**: clouds and storms visible from afar. It uses **compute shaders**, hence probably **no macOS**, and conflicts with Ecliptic's weather in server mode;
   - **Longwings**;
   - **Particular Reforged**.

   **Weather2** (tornadoes) is ruled out for now: it conflicts with Ecliptic Seasons.

---

## 1. Removing vanilla monsters and controlling spawns

### 1.1 The six vectors and the layer that handles each

| Vector | Examples | Proposed treatment | Layer |
|---|---|---|---|
| Biome spawn lists | zombie, skeleton, creeper, spider, enderman, witch, slime, husk, stray, bogged, drowned | `neoforge:remove_spawns` on `#minecraft:is_overworld` | Datapack (`pack/kubejs/data/compline/neoforge/biome_modifier/`) |
| Structure `spawn_overrides` | guardians (monument), witch (hut), pillagers (outpost), *Nether: fortress* | In Control `deny` (it also applies to overrides) + KubeJS safety net; failing that, JSON override of the structures | In Control / KubeJS |
| Spawners | `monster_room` dungeons, mineshaft cave spiders, trial spawners (breeze, etc.) | `neoforge:remove_features` on `minecraft:monster_room` and `monster_room_deep` (`underground_structures` step); trial chambers: remove via Sparse Structures or keep empty | Datapack + Sparse Structures |
| Events handled by gamerule | phantoms, patrols, raids, warden, wandering trader ("survivors" concept) | `doInsomnia false`, `doPatrolSpawning false`, `disableRaids true`, `doWardenSpawning false`, `doTraderSpawning false` (keep `doMobSpawning true` for animals) | KubeJS `ServerEvents.loaded` → commands |
| Conversions | villager struck by lightning → witch; skeleton horse trap in thunderstorm; piglin → zombified piglin in the Overworld; and on the F&F side, `enableZombieHorseTrap` | KubeJS safety net (cancellation on entering the world) + F&F config | KubeJS |
| Entities placed at generation | mansion vindicators and evokers, elder guardians, zombie villagers of zombie villages ("ruins" concept), silverfish of infested blocks | KubeJS safety net (`EntityEvents.spawned` cancelled); `neoforge:remove_features` on `minecraft:ore_infested` | KubeJS + datapack |

**Examples (syntax to be validated at first launch).**

`pack/kubejs/data/compline/neoforge/biome_modifier/remove_vanilla_hostiles.json`
```json
{
  "type": "neoforge:remove_spawns",
  "biomes": "#minecraft:is_overworld",
  "entity_types": [
    "minecraft:zombie", "minecraft:zombie_villager", "minecraft:husk", "minecraft:drowned",
    "minecraft:skeleton", "minecraft:stray", "minecraft:bogged", "minecraft:creeper",
    "minecraft:spider", "minecraft:cave_spider", "minecraft:enderman", "minecraft:witch",
    "minecraft:slime", "minecraft:phantom", "minecraft:silverfish", "minecraft:skeleton_horse"
  ]
}
```
`.../remove_dungeons.json`
```json
{ "type": "neoforge:remove_features", "biomes": "#minecraft:is_overworld",
  "features": ["minecraft:monster_room", "minecraft:monster_room_deep", "minecraft:ore_infested"],
  "steps": ["underground_structures", "underground_ores"] }
```
**Key trick.** To disable **another mod's** biome modifier without touching its config, override its file at the same path with `{ "type": "neoforge:none" }`. Example: `data/naturalist/neoforge/biome_modifier/remove_forest_pigs.json`.

Naturalist registers its animals through a coded modifier (`naturalist:add_animals`, a single entry) driven by its config. Its three other modifiers (`remove_farm_animals_savanna`, `remove_farm_animals_swamp`, `remove_forest_pigs`) remove vanilla farm animals. To keep, this is consistent with livestock farming (source: GitHub Crispytwig/naturalist, branch `1.21.1-NeoForge`).

**KubeJS safety net** (`pack/kubejs/server_scripts/compline/no_vanilla_hostiles.js`, KubeJS 2101 API to be validated):
```js
const BANNED = ['minecraft:zombie','minecraft:zombie_villager','minecraft:husk','minecraft:drowned',
  'minecraft:skeleton','minecraft:stray','minecraft:bogged','minecraft:creeper','minecraft:spider',
  'minecraft:cave_spider','minecraft:enderman','minecraft:witch','minecraft:slime','minecraft:phantom',
  'minecraft:silverfish','minecraft:guardian','minecraft:elder_guardian','minecraft:pillager',
  'minecraft:vindicator','minecraft:evoker','minecraft:ravager','minecraft:vex','minecraft:illusioner',
  'minecraft:breeze','minecraft:warden','minecraft:skeleton_horse','minecraft:zombie_horse','minecraft:endermite']
EntityEvents.spawned(event => {
  const e = event.entity
  if (!BANNED.includes(String(e.type))) return
  if (String(e.level.dimension) !== 'minecraft:overworld') return   // Nether: separate decision
  if (e.tags.contains('compline_allowed')) return                    // admin /summon with Tags:["compline_allowed"]
  event.cancel()
})
ServerEvents.loaded(event => {
  ['doInsomnia false','doPatrolSpawning false','disableRaids true','doWardenSpawning false','doTraderSpawning false']
    .forEach(r => event.server.runCommandSilent(`gamerule ${r}`))
})
```

### 1.2 Spawn control mods

| Mod | Slug / evidence | 1.21.1 NeoForge Modrinth | Side | Role | Verdict |
|---|---|---|---|---|---|
| **In Control!** (McJty) | project `KpICtuVx` — [v1.21-10.2.6](https://modrinth.com/project/KpICtuVx/version/TBI4EWjs); changelog [GitHub branch `1.21_neo`](https://github.com/McJtyMods/InControl) up to 10.3.0 | **Yes** (10.2.6, beta) | S | `spawn.json` and `spawner.json` rules: mob, mod, biome and biome tags, light, height, sky visible, weather, structure, `mincount`/`maxcount`, **`mindist`/`maxdist` to the nearest player** (added in 10.2.5), `random`, phases. Since 10.3.0, **conditions on KubeJS persistent data**. 10.1.3 adds a `command` action | **Recommended, almost core.** It is the only tool that caps modded hostiles per mod or per player and keeps them out of light without writing 300 lines of JS. Server only |
| Spawn Balance Utility | [CurseForge](https://www.curseforge.com/minecraft/mc-mods/spawn-balance-utility/files/7341253) | **No** (Fabric 1.21.5–1.21.11) | — | Spawn weights per biome and structure | **Ruled out** |
| Native NeoForge (`remove_spawns`, `add_spawns`, `add_spawn_costs`, `remove_features`, `none`) | NeoForge docs | Built in | S | Additions and removals without a mod. `add_spawn_costs` (`energy_budget`/`charge`) **spaces out individuals of the same species**, ideal for territorial predators | **Core** (already available via KubeJS `data/`) |
| KubeJS (already present) | — | Yes | S | Safety net, loot replacements, gamerules, data bridges | **Core** |

**Proposed In Control rules** (`config/incontrol/spawn.json`). The semantics of `mincount` and `maxdist` are to be confirmed: in McJty's docs, a `deny` rule triggers when all its conditions are true.
```json
[
  { "dimension": "minecraft:overworld", "mob": ["minecraft:zombie","minecraft:skeleton","minecraft:creeper","minecraft:spider","minecraft:enderman","minecraft:witch","minecraft:slime","minecraft:guardian","minecraft:pillager"], "result": "deny" },

  { "mod": "mowziesmobs", "minlight": 8, "result": "deny" },
  { "mod": "mowziesmobs", "maxdist": 40, "result": "deny" },
  { "mod": "mowziesmobs", "mincount": { "amount": 2, "mod": "mowziesmobs", "perplayer": true }, "result": "deny" },

  { "mob": ["alexsmobs:cave_centipede","alexsmobs:rocky_roller","alexsmobs:guster"], "minlight": 8, "result": "deny" },
  { "mob": ["alexsmobs:cave_centipede","alexsmobs:rocky_roller","alexsmobs:guster"], "mincount": { "amount": 3, "perplayer": true, "hostile": true }, "result": "deny" },

  { "mob": ["friendsandfoes:mauler","friendsandfoes:barnacle"], "random": 0.6, "result": "deny" },
  { "hostile": true, "dimension": "minecraft:overworld", "mincount": { "amount": 8, "perplayer": true, "hostile": true }, "result": "deny" }
]
```
Reading: no anomaly in light (≥ 8), nor within 40 blocks of a player. They therefore appear *in the distance, in the mist*, and you see them coming. At most 2 Mowzie's creatures per player, and a global ceiling of 8 hostiles per player that replaces the vanilla cap of 70.

### 1.3 Lost vanilla resources → KubeJS replacements (subproject 3)

| Resource | Use in the pack | Lost source | Proposed replacement |
|---|---|---|---|
| **Slime ball** | Create's Super Glue, Sticker, sticky piston (essential for Aeronautics), lead (escorting survivors) | Slime | Create mixing: kelp + flour (KC/F&C) + water, or tree resin via Farm & Charm. To check in the AM jar: does the *banana slug slime* work as slime? |
| **Live blaze / blaze rod** | Create's **Blaze Burner** (capturing a blaze → heated mixing → **brass**), brewing stand | Blaze (Nether) | Nether decision to be made. Otherwise, a KubeJS recipe for a "lit" Blaze Burner (Overgeared: forge + charcoal + magma core) |
| Gunpowder | TNT, rockets, Supplementaries | Creeper, witch, ghast | Create mixing: charcoal + saltpetre (new item, or washed gravel) + sulphur |
| String | Bows, wool, fishing rods | Spider | Wool crushing in Create (vanilla Create); Supplementaries flax → string |
| Bone / bone meal | Agriculture | Skeleton | Fish, composter; add `minecraft:bone` to the loot tables of large Naturalist/AM animals (override of `data/<mod>/loot_table/entities/*.json`) |
| Phantom membrane | Slow falling potion, Elytra repair (outside the pack) | Phantom | To check: does an Aeronautics recipe use it (parachute, sail)? |
| Prismarine, sponge | Decoration, draining | Guardians | Emptied monuments remain lootable underwater ruins; the sponge stays in the rooms |
| Ender pearl | — (End removed) | Enderman | Nothing, unless a modded recipe requires it (to check by grepping the jars' recipes) |

---

## 2. Spawn table by biome (proposal)

It is built by **tag families** (`#minecraft:is_*`, `#c:is_*`) so as to apply to No Man's Land biomes without knowing them one by one. Species are to be inventoried in the jars, especially Naturalist 2.x (47 animals according to its page) and Hybrid Aquatic. **Deduplication rule: one species = one mod.**

| Family | Base wildlife | Complement | Small fauna / companions | Anomalies and territorial creatures (rare, capped) | Ambience (ATMOSPHERICS mist, sounds) |
|---|---|---|---|---|---|
| Temperate forests, birch | Naturalist: deer, bear, forest fox and rabbit, birds, butterflies, snails | AM: raccoon, raven, hummingbird | C&C (ferret, ladybug, beetle), ducks (UDM) | F&F Rascal in mineshafts | Light morning mist, birds by day, crickets at night |
| Dark forest | Naturalist: bear, fireflies | — | Ghost (little ghosts) | **Mowzie's Lantern** (passive, luminous: *strangeness without horror*) | Permanent thick mist, muffled sounds |
| Taiga, snowy forests | Naturalist: deer, fox | AM: moose, snow leopard | F&F penguin (icy coasts) | Mowzie's Frostmaw (**very** rare); AM's Froststalker (pack of 2 at most) | Wind, Ecliptic snow |
| Swamps, mangroves | Naturalist: alligator, fireflies, dragonflies | AM: rain frog, mudskipper | Ducks and geese (UDM) | — | Dense ground mist, croaking, drips |
| Rivers, lakes | Naturalist: bass, catfish | AM: terrapin | C&C: otter, koi | — | River mist at dawn (ATMOSPHERICS, day/night setting) |
| Plains, meadows | Naturalist: birds, snakes | AM: bison | F&F Moobloom (flower meadows, rare) | — | Wind, insects |
| Savanna | Naturalist: lion, zebra, giraffe, rhinoceros, hippopotamus, vulture | **AM: elephant** (mount with a chest = caravan) | — | F&F Mauler (weight 32 → 8) | Heat, cicadas |
| Desert, badlands | Naturalist: lizard, tortoise, rattlesnake | AM: roadrunner, jerboa | — | AM's Guster (living sandstorm), Mauler | Dry wind, sand |
| Jungle | Naturalist: snakes | AM: gorilla, tiger, toucan, anaconda | — | Mowzie's Foliaath; Umvuthana / sun temple: **to be discussed** | Humid mist, rain (Ecliptic) |
| Mountains, peaks | — | AM: bald eagle, **Sunbird** (extremely rare) | — | Mowzie's **Sculptor** (summit structure), Naga on coastal cliffs | Strong wind, distant horizon (Distant Horizons under study) |
| Oceans, coasts | Hybrid Aquatic (marine base) | AM: orca, sperm whale *if absent from Hybrid* | F&F crab (beaches) | Hybrid sharks (territorial), F&F Barnacle (rare) | Surf, gulls |
| Deep sea | Hybrid Aquatic (abyssal creatures) | AM: giant squid *if absent from Hybrid* | — | Hybrid's Karkinos and deep creatures | Silence, low sounds (AmbientSounds) |
| Caves (lush, dripstone) | — | AM's **Underminer** (passive miner ghost), Rocky Roller | F&F Glare (detects darkness: synergy with More Darkness) | Mowzie's Grottol (flees; drops a diamond), AM's centipede, Mowzie's Wroughtnaut (room-structure) | Drips, echo (Sound Physics) |
| Mushroom | — | AM: Bunfungus (rare) | — | — | Coloured mist |

**Duplicates to settle (a single version per species)**:

| Species | Keep | Cut | Known key |
|---|---|---|---|
| Duck | Untitled Duck Mod | Naturalist | `naturalist-common` → `disable_mobs.duck_removed = true` |
| Bear | Naturalist | AM grizzly | AM Continued config (key to be read in the jar) |
| Crocodilians | Naturalist alligator | AM crocodile | same |
| Rattlesnake | Naturalist (`snake_removed` also covers the coral snake) | AM | same |
| Elephant | **AM** (caravan mechanic) | Naturalist | `elephant_removed = true` |
| Rhinoceros | Naturalist | AM | — |
| Catfish | Naturalist | AM | — |
| Blue jay | Naturalist (`bird_removed` cuts *all* its birds, do not enable it) | AM | — |
| Hammerhead shark, squid, anglerfish | Hybrid Aquatic | AM; Naturalist 2.0 (anglerfish) | Naturalist anglerfish: 2.x key to be confirmed in the jar |
| Penguin | F&F (`enablePenguinSpawn`) **or** Naturalist 2.x if it adds one | the other | — |
| Fireflies | Naturalist entities in swamps (catchable in a Supplementaries jar) | Subtle Effects/Particular particles in swamps | — |
| Butterflies | Naturalist (crop pollination = mechanic) | Longwings if added (or the reverse) | — |

**AM creatures to cut for art direction** (horror, comic, End/Nether off-topic): Murmur, Farseer, Skelewag, Fly, Cockroach (maracas gag), and as a precaution all End creatures (Void Worm, Mimicube, Cosmaw, Enderiophage, Spectre…). Nether creatures depend on the Nether decision.

**Settings of the mods already chosen (keys verified against public GitHub sources)**

- **Naturalist** (`NaturalistConfig`, section `disable_mobs`, booleans `*_removed`, false by default): `alligator, bass, bear, bird, boar, butterfly, catfish, deer, dragonfly, duck, elephant, firefly, forest_fox, forest_rabbit, giraffe, hippo, lion, lizard, rhino, snail, snake, tortoise, vulture, zebra`. The config has **no spawn weight**: rarity is set through In Control or `add_spawn_costs`. ⚠️ These keys come from the GitHub branch `1.21.1-NeoForge`, which lists 24 animals. 2.0.4 announces 47 animals: new keys likely (anglerfish, ants…), to be read in the jar.
- **Friends&Foes** (`FriendsAndFoesConfig`, master branch, close to 4.0.27):
  - **Cut**: `enableIceologer=false`, `enableIceologerSpawn=false`, `enableIceologerInRaids=false`, `generateIceologerCabinStructure=false`; `enableIllusioner=false`, `enableIllusionerInRaids=false`, `generateIllusionerShackStructure=false`, `generateIllusionerTrainingGroundsStructure=false`; `enableWildfire=false`, `generateCitadelStructure=false`; `enableZombieHorseTrap=false`; `enableBeekeeperVillagerProfession=false` and `generateBeekeeperAreaStructureInVillages=false` (no intact villages); `generateCopperGolemWorkstationStructureInVillages=false`.
  - **Keep and tune**: Crab (`crabSpawnWeight` 14 → 6); Glare (`enableGlareGriefing`: to be assessed); Rascal (4); Moobloom (4 → 2); Penguin; Mauler (`maulerSavannaSpawnWeight` 32 → 8, `maulerBadlandsSpawnWeight` 16 → 6, `maulerDesertSpawnWeight` 8 → 4); Barnacle (4 → 2).
  - **To note**: `generateTuffGolemInStronghold` is no longer useful, since strongholds are removed. The Tuff Golem becomes unobtainable: plan another source or a loot drop. `generateCopperGolemInAncientCity` and `enableCopperGolemsInTrialChambers` depend on the fate of those structures.
- **Mowzie's Mobs** (`mowziesmobs-common.toml`, source repository not public, names from memory **to be confirmed**):
  - per creature, `spawn_config` (`spawn_rate`, group size, `biome_config` with tags, whitelist and blacklist, `dimensions`, min/max height, darkness requirement);
  - per structure, `generation_config` (distance and separation, biomes);
  - `combat_config` (health and damage multipliers).

  Proposal:
  - Lantern and Grottol: rare but present;
  - Foliaath: jungle only;
  - Naga: coastal cliffs, ×0.3;
  - Frostmaw: ×0.2;
  - structures (Wroughtnaut, sun temple, Sculptor): left to Sparse Structures;
  - Umvuthana: art-direction decision (hostile masked tribes);
  - Bilokosa (new in 1.8): to be identified in the jar.
- **Alex's Mobs Continued** (CodxLib, not Citadel): per-creature config (weight, and probably per-creature biome files as in the original AM). Structure **to be read in the 2.2.2 jar**; this is the most uncertain element.
- **Hybrid Aquatic, C&C, UDM, Ghost**: per-creature weights and toggles likely, not verified.

---

## 3. Complementary candidate mods (36 assessed)

Availability legend: **Yes** = Modrinth 1.21.1 NeoForge version page seen in the results; **Probable** = Modrinth mirror or third-party index; **Unverified** = nothing conclusive (the search quota was exhausted).

### 3.1 Ambient small fauna, birds, insects

| # | Mod (slug) | Evidence | 1.21.1 NeoForge Modrinth | Side / dependencies | Effect and integration | Conflicts, settings | Verdict |
|---|---|---|---|---|---|---|---|
| 1 | **Cosy Critters & Creepy Crawlies** (`cosy-critters`, PigCart) | [v0.2.0+1.21.1-neoforge](https://modrinth.com/mod/cosy-critters/version/v0.2.0+1.21.1-neoforge) | **Yes** | C, no dependency | Particle birds that gather in the sky, land and **fly off when a vehicle, mob or projectile approaches**. **Moths around exposed light sources**. Small spiders that come out of webs. Silhouettes in the distance, base = island of light: squarely in the art direction, no server cost | Partial duplicate with Naturalist birds (which are entities): the two complement each other (particles in the distance, entities on the ground). Each effect can be turned off (`/cosycritters`) | **Recommended** |
| 2 | World Alive | [Modrinth](https://modrinth.com/mod/world-alive) | **No** (Fabric 1.21.11/26.x) | C | Derived from Cosy Critters: butterflies and birds by biome | — | Ruled out (unavailable); to watch |
| 3 | Atmospheric Fauna | [Modrinth `yd14I2wN`](https://modrinth.com/mod/yd14I2wN) | **No** (Fabric) | C | Particle crows | — | Ruled out |
| 4 | **Longwings** | [Modrinth version](https://modrinth.com/mod/longwings/version/h13ko2fP), [CurseForge](https://www.curseforge.com/minecraft/mc-mods/longwings/files/7997964) | **Yes** (0.9.5-neoforge, April 2026) | CS | More than 70 butterflies and moths with realistic rarity and habitats, **collection handbook**, nets, sugar-water feeder. Makes collecting a real naturalist's hobby (zoo, vivarium) | Duplicate of Naturalist butterflies (cut one of the two, Naturalist pollinates). ARR, but the author allows modpacks with credit | **Optional** (wildlife branch) |
| 5 | Neo Guan Niao – Bird Watching | [Modrinth `neoguanniao`](https://modrinth.com/mod/neoguanniao) | **Probable** | CS | Birds (parakeet, sparrow, night heron, turtle dove, pigeon) with eggs, breeding, **observation guide**, feather economy | Sparrow duplicates Naturalist; Asian species; little content | Weak optional |
| 6 | Backyard Birds | [Modrinth `pNndCV1F`](https://modrinth.com/project/pNndCV1F), [ModDex](https://moddex.gg/mod/backyard-birds) | **Unverified** (ModDex says Forge) | CS | More than 60 birds, nest boxes, feeders, baths, **binoculars**, crow-based technical progression | The binoculars serve exploration, but the technical progression duplicates Create. ARR, closed source | To verify (versions), otherwise ruled out |
| 7 | Fowl Play | [Modrinth `fowlplay`](https://modrinth.com/mod/fowlplay), [Mado](https://www.mado-hosting.com/minecraft-mods/fowlplay) | **Probable** (1.2.3, August 2026) | CS | Robin, crow, gull, pigeon, penguin with realistic flight behaviour | The author warns: "in development, may **corrupt worlds**". Incompatible with a permanent world | **Ruled out** |
| 8 | Realistic Bird Behavior | [BoxToPlay](https://www.boxtoplay.com/en/minecraft-hosting/minecraft-server/modrinth-mods/realistic-bird-behavior-39225) | Probable | S | Breedable parrots | Marginal gain | Ruled out |
| 9 | Glimmering Gardens | [ModDex](https://moddex.gg/mod/glimmering-gardens) | Unverified | CS | Firefly entities (swamps, birches, meadows), catchable in a bottle | Duplicates Naturalist and Subtle Effects fireflies | Ruled out |
| 10 | Firefly Bush Backport | [Mado](https://www.mado-hosting.com/minecraft-mods/firefly-bush-backport) | Probable (v1.1.1) | S | Firefly bush from 1.21.5 (particles and sound at night) | Light, vanilla-like; overlaps Subtle Effects | Weak optional |
| 11 | Vanilla Fireflies (datapack) | [Modrinth](https://modrinth.com/mod/vanilla-fireflies/version/YgsKRgbd) | Yes but **archived** | S | — | — | Ruled out |
| 12 | Leaf Litter | [ModpackIndex](https://www.modpackindex.com/mod/98031/leaf-litter) | Unverified | CS | 1.21.5 leaf litter, no natural generation | Simple decoration | Ruled out |

### 3.2 Large fauna and rare creatures (outside the chosen mods)

| # | Mod | Evidence | Availability | Effect | Verdict |
|---|---|---|---|---|---|
| 13 | Whisperwoods (wisps, moths, **Hirschgeist**, Hidebehind) | [Modrinth](https://modrinth.com/mod/whisperwoods) | **No** (1.20.1 Forge/Fabric) | The ideal art direction (spirit deer in the mist), but no 1.21.1 port | Ruled out; **to watch** |
| 14 | Wisp & Willow | [CurseForge](https://www.curseforge.com/minecraft/mc-mods/wisp-willow), [ModDex (Modrinth page)](https://moddex.gg/mod/wisp-willow-mr-a6zCFWRP) | Probable (NeoForge 1.21.x, 1.4.2) | Old-forest spirit magic (altar, chalk, grimoire, rituals) | Ruled out: adds a **magic system** outside the three pillars |
| 15 | Mo' Creatures Reborn / Neo Mo' Creatures / Mo' Creatures Port | [ModpackIndex](https://www.modpackindex.com/mod/206150/mo-creatures-reborn-neoforge) | Unverified (recent ports, few downloads) | DrZhark's bestiary | Ruled out: duplicates, immaturity |
| 16 | ZAWA | [CurseForge](https://www.curseforge.com/minecraft/mc-mods/zoo-wild-animals-rebuild/files/2989666) | **No** (1.12.2) | Realistic zoo | Ruled out |
| 17 | AND / Thero's Wilds | [Mado](https://www.mado-hosting.com/minecraft-mods/and) | Unverified, WIP | Endangered animals, varied fauna | Ruled out |
| 18 | Wan's Ancient Beasts | [ModDex](https://moddex.gg/mod/wans-ancient-beasts) | Beta | Prehistory | Ruled out (prehistory dropped) |
| 19 | Bosses of Mass Destruction / L_Ender's Cataclysm / Ice and Fire / Born in Chaos / Deeper and Darker | — | Unverified for BoMD; the others exist but are off art direction | RPG bosses, dragons, horror, sculk dimension | **Ruled out** (RPG/horror). Mowzie's already covers the "discreet bosses" |

**Wildlife conclusion**: the chosen bestiary is more than sufficient. The real gaps are **living ambience with no server cost** (Cosy Critters) and **non-horror strangeness**, already present in the chosen mods: Lantern, Grottol, Underminer, Sunbird, Rascal, Glare, Ghost. It should be **staged through rarity** rather than by adding a mod.

### 3.3 Weather, sky, celestial bodies

| # | Mod | Evidence | Availability | Side / dependencies | Effect | Conflicts, settings | Verdict |
|---|---|---|---|---|---|---|---|
| 20 | **Enhanced Celestials** (`enhanced-celestials`, CorgiTaco) | [6.0.2.5-neoforge](https://modrinth.com/mod/enhanced-celestials/version/6.0.2.5-neoforge) | **Yes** | CS; CorgiLib + Data Anchor | Datapack-driven lunar events: **harvest moon** (crop growth), blue moon (luck), super moons, **blood moon** (+ spawns). Each event sets colours, effects, spawns, sleep and loot | **Disable the blood moon** (horde, horror tone). Keep harvest and blue moons (synergy with farming and Ecliptic). Create a "Pale Moon" as a datapack: washed-out sky, more frequent Lantern and Ghost, no extra hostiles. To test: interaction with More Darkness (moonlight brightness) and with Ecliptic's day length. The **GitHub repository is archived**: the active line is Enhanced Celestials 2 | **Recommended** |
| 21 | Enhanced Celestials 2 (Core + Default Lunar Events) | [ModpackIndex](https://www.modpackindex.com/mod/166913/enhanced-celestials-2-core) | Unverified on Modrinth (NeoForge 1.21.1 according to the index) | CS | Rewrite of the scheduler and datapack loader | To be preferred over v1 if available on Modrinth | To verify |
| 22 | **Simple Clouds** (`simple-clouds`) | [Modrinth](https://modrinth.com/project/simple-clouds), [GitHub](https://github.com/nonamecrackers2/simple-clouds) | **Yes** (0.7.3+1.21.1) | C or CS | GPU-generated cloud types, **storms that darken the horizon and are visible from afar**, lightning and delayed thunder. Localised weather with the mod on the server side. Cloud editor exportable as a datapack | **Compute shaders → OpenGL 4.3: very probably incompatible with macOS** (to check with each player). Beta, FPS cost. With the mod on the server side, localised weather replaces global weather: **conflict with Ecliptic Seasons**. Proposal: **client only** (Ecliptic keeps the weather). Overlaps ATMOSPHERICS clouds (cut one). Iris not required | **Optional, dedicated branch** |
| 23 | Weather, Storms & Tornadoes (Weather2, `weather-storms-tornadoes`) | [Modrinth 1.21.0-2.8.7](https://modrinth.com/mod/weather-storms-tornadoes/version/YuNAxTMO) | **Yes** | CS; CoroUtil | Storm fronts, tornadoes, sandstorms, wind. "Survival through the environment" in the literal sense | **Takes control of the weather → head-on conflict with Ecliptic**, duplicates Particle Rain and Simple Clouds, server cost. Reaction of Sable/Aeronautics contraptions to tornadoes unknown | **Ruled out for now** (to reopen if Ecliptic falls) |
| 24 | Expanded Weather2 Dynamics | [CurseForge](https://www.curseforge.com/minecraft/mc-mods/expanded-weather2-dynamics) | Unverified, alpha | CS | Weather2 extension | — | Ruled out |
| 25 | Particle Rain (*already pending*) | [v4-beta.6 Modrinth](https://modrinth.com/mod/particle-rain/version/v4-beta.6+1.21.1-neoforge), [beta.10 CurseForge](https://www.curseforge.com/minecraft/mc-mods/particle-rain/files/8055433) | Yes (beta.6; beta.10 of May 2026 confirmed on CurseForge only) | C | Rain and snow as particles. Beta.10 **restores a mist effect ("haze"), disabled by default** and lets rain through barriers and fences | Purely visual: compatible with Ecliptic as long as it decides the weather. Enable the haze for storms, check there is no double snow rendering with Ecliptic | Recommended to test |

Auroras and eclipses: **unverified** (quota exhausted). No confirmed NeoForge 1.21.1 mod is known; a coloured Enhanced Celestials event ("green sky" in a snowy biome) can serve as a substitute.

### 3.4 Sounds, voice, music

| # | Mod | Evidence | Availability | Side | Effect | Conflicts, settings | Verdict |
|---|---|---|---|---|---|---|---|
| 26 | **Simple Voice Chat** (`simple-voice-chat`) | [BoxToPlay (Modrinth mirror)](https://www.boxtoplay.com/en-ba/minecraft-hosting/minecraft-server/modrinth-mods/simple-voice-chat-9) | **Yes** (1.21.1-2.6.x) | CS; **UDP port 24454** to open on the dedicated server | Proximity voice: you hear each other at real distance, voice carries in valleys. Sound Physics Remastered is "optimised for SVC" (reverberation and occlusion on voice) | Nothing to disable. Groups possible for distant expeditions. Create idea: radios (addon to research, unverified) | **Recommended** |
| 27 | **Presence Not Position** | [ModDex](https://moddex.gg/mod/presence-not-position), [ModpackIndex](https://www.modpackindex.com/mod/175210/presence-not-position) | **Unverified** (NeoForge 1.21.1, 1.2.1; Modrinth page not found) | CS | Detects entering a structure, biome, dimension or **"home"** and displays title, illustration, sound and **adaptive music**, via resource pack or KubeJS. A Breath of the Wild-style "discovered place", and a musical theme for the base (the island) | Titles must not reveal structures: show only poetic names, or only the music. Very young and obscure mod | **Recommended if present on Modrinth**, otherwise to watch |
| 28 | Biome Music | [CurseForge 3.9](https://www.curseforge.com/minecraft/mc-mods/biome-music/files/8302329) | Unverified on Modrinth (NeoForge 1.21.1, June 2026) | C | Vanilla music by biome, cave detection | Overlaps Presence Not Position | Optional (fallback) |
| 29 | Dynamic Music Reneoforged | [CurseForge](https://www.curseforge.com/minecraft/mc-mods/dynamic-music-reneoforged) | Unverified | C | Contextual music pools (`SelectMusicEvent`) | About 9 downloads | Ruled out |
| 30 | Fight Music | [CurseForge](https://www.curseforge.com/minecraft/mc-mods/fight-music) | — | C | Combat music | Contrary to the tone | Ruled out |
| 31 | Dynamic Surroundings (OreCruncher) | [GitHub](https://github.com/OreCruncher/DynamicSurroundingsFabric), [Modrinth version](https://modrinth.com/project/H7fshfpD/version/aFsN201L) | **Yes** (0.4.2, January 2025); Architectury | C | Ambiences, footsteps, waterfalls, steam | Duplicates AmbientSounds + Subtle Effects (+ HUD to turn off); little maintained | Ruled out |
| 32 | Dynamic Surroundings Rebirth | [CurseForge](https://www.curseforge.com/minecraft/mc-mods/dynamic-surroundings-rebirth) | Unverified | C | Community port | same | Ruled out |
| 33 | *(new information)* Presence Footsteps x Sable | [Mado](https://www.mado-hosting.com/minecraft-mods/presence-footsteps-x-sable), [PF NeoForge](https://modrinth.com/project/JIEwmDVI/version/f3SxKzof) | Probable (1.0, NeoForge 1.21.1, MIT) | C | Patch: Presence Footsteps footstep sounds play **on Sable and Aeronautics contraptions** | PF was ruled out in 0007. This patch is **new information** if the decision is reopened | Information only |

AmbientSounds idea (chosen): its ambiences are defined as resource-pack JSON, with conditions on biome, time, altitude, rain and light. We can add **rare, unidentifiable distant sounds**, with no jumpscare: a metallic creak near NML ruins, a muffled bell in the swamp mist, a whale song far out at sea at night, a very distant ghost train whistle. This is the cheapest way to create "diffuse strangeness".

### 3.5 Lighting and particles

| # | Mod | Evidence | Availability | Effect | Verdict |
|---|---|---|---|---|---|
| 34 | Sodium Dynamic Lights | [Modrinth version](https://modrinth.com/mod/sodium-dynamic-lights/version/XI0WLXdn) | Yes (NeoForge 1.0.10) | Dynamic light hooked into Sodium | **Fallback** if LambDynamicLights breaks with Sodium 0.8.13. LDL 4.8.9 was announced as the last for 1.21, but the pack already has 4.8.11: minimal maintenance |
| 35 | True Darkness Biomes / Truly Dark / DarkAroundMe | [Mado](https://www.mado-hosting.com/minecraft-mods/true-darkness-biomes), [ModpackIndex](https://www.modpackindex.com/mod/105503/truly-dark) | Probable (NeoForge 1.21.1) | Real darkness (per biome for TDB) | Ruled out: duplicates of More Darkness. Keep TDB in mind if More Darkness disappoints (**per-biome** darkness: darker dark forest) |
| 36 | Particular ✨ Reforged | [CurseForge 1.5.5 (June 2026)](https://www.curseforge.com/minecraft/mc-mods/particular-reforged/files/8275091) | **Unverified on Modrinth** (only an old "Particular" 1.1.4 project appears there) | Foam **waterfalls** at the foot of falls, splashes, configurable fireflies (biomes, blocks, colours), falling leaves, dust | Optional if present on Modrinth. Overlaps Subtle Effects (cut duplicate effects) and Falling Leaves (already ruled out) |

LambDynamicLights idea (chosen): 4.x accepts **entity luminances defined by resource pack**. Mowzie's Lantern, Naturalist fireflies and the Glare can be made to really give off light. The encounter becomes a mobile island of light in the pitch-black night of More Darkness.

### 3.6 Compatibility with rendering (Iris/Oculus, Sodium)

No candidate **requires Iris or Oculus**. Points of attention:
- **Simple Clouds**: compute shaders (macOS excluded, probably), and not compatible with shader packs.
- **ATMOSPHERICS**: do not enable "Improved Transparency" in Sodium (already noted).
- **LambDynamicLights and Sodium Dynamic Lights**: depend on Sodium's API. Test with Sodium 0.8.13.
- **Particle Rain, Cosy Critters, Enhanced Celestials**: plain particle and sky rendering, no known dependency on Sodium.
- **Distant Horizons** (under study): its own distant fog overlaps that of ATMOSPHERICS. To be tuned together.

---

## 4. Proposed coherent system

**Ecology (server).**
1. A `compline` datapack removes vanilla hostiles from biomes, dungeons and infested blocks.
2. The `neoforge:none` override neutralises unwanted modifiers from other mods.
3. `add_spawn_costs` spaces out territorial predators: bears, lions, tigers, sharks.
4. **In Control!**:
   - forbids anomalies in light (≥ 8) and within 40 blocks of a player;
   - caps Mowzie's at 2 per player;
   - caps hostiles at 8 per player, instead of the vanilla cap of 70.
5. The **KubeJS safety net** cancels any banned vanilla entity entering the Overworld (conversions, entities placed at generation, overrides) and sets the gamerules.
6. Mod configs cut the duplicates (table in §2) and the creatures outside the art direction.

**Visible life (client).**
- Naturalist, AM, Hybrid and the companions on the ground;
- **Cosy Critters** in the sky and around lamps;
- Subtle Effects for details;
- Longwings as an option for collectors.

**Atmosphere (client).**
- **ATMOSPHERICS** does the mist by biome, with day and night presets shipped in `pack/config`;
- **More Darkness**: real nights;
- **LambDynamicLights** (light from torches and luminous creatures);
- **AmbientSounds** enriched with rare, distant ambiences;
- **Sound Physics Remastered** and **Simple Voice Chat** for voice in space;
- **Particle Rain**, with the storm haze enabled.

**Phenomena.**
- **Ecliptic Seasons** is the only weather brain;
- **Enhanced Celestials**: harvest, blue moon and a home-made "Pale Moon"; blood moon cut;
- **Presence Not Position** for the music of places and the home, if it is on Modrinth, otherwise Biome Music;
- **Simple Clouds** client only, on a test branch, for storms seen from afar.

**Emptied structures = silent ruins.** Monuments without guardians, outposts without pillagers, mansions without illagers, huts without a witch, zombie villages without zombies (survivors concept). Ancient Cities and Trial Chambers are to be removed via Sparse Structures, or kept empty with the chest loot to be revised in KubeJS.

**Summary of deactivations.**
- **Gamerules**: `doInsomnia`, `doPatrolSpawning`, `doWardenSpawning` and `doTraderSpawning` to `false`, `disableRaids` to `true`.
- **F&F**: Iceologer, Illusioner, Wildfire and Citadel, zombie horse trap, beekeeper, village structures.
- **Naturalist**: `duck_removed`, `elephant_removed`.
- **AM**: grizzly, crocodile, rattlesnake, rhinoceros, catfish, blue jay, hammerhead shark, Murmur, Farseer, Skelewag, fly, cockroach, End creatures.
- **Enhanced Celestials**: blood moon.

---

## 5. Uncertainties to resolve in the .jar files and in game

1. **Naturalist 2.0.4**: actual list of the 47 animals and new `*_removed` keys (GitHub shows only 24); penguin, anglerfish, ants?
2. **Alex's Mobs Continued 2.2.2** (CodxLib): spawn config format (weight per creature? biome files?), list of creatures kept compared with the original AM, *banana slug slime* → slime ball.
3. **Mowzie's 1.8.2**: exact names of the `spawn_config`/`generation_config` keys, spawn category of each creature (MONSTER / CREATURE / AMBIENT), nature of the **Bilokosa**, Grottol loot (diamond).
4. **Hybrid Aquatic 1.7.3**: list of species (sperm whale, orca, giant squid, anglerfish?) to settle with AM; spawn config.
5. **Recipes**: grep the jars (Create, Aeronautics, Steam 'n' Rails, Overgeared, KC, F&C, Supplementaries) for `slime_ball`, `blaze_rod`, `gunpowder`, `ender_pearl`, `phantom_membrane`, `string`, `bone`, to size the KubeJS replacements. **Blaze Burner and the Nether decision first.**
6. **KubeJS 2101.7**: name and cancellability of `EntityEvents.spawned`/`checkSpawn`, access to `entity.tags` and to the spawn type (to let eggs and commands through).
7. **In Control 10.x**: exact semantics of `mincount`/`maxcount` (with `perplayer`) and of `mindist`/`maxdist`; handling of structure `spawn_overrides`.
8. **Trial Chambers 1.21.1**: trial spawner configs are in the templates (not yet in data as in 1.21.2+). Removing them is simpler than modifying them.
9. **Enhanced Celestials**: v1 (archived) or v2 on Modrinth 1.21.1? Interactions with Ecliptic's day length and with More Darkness.
10. **Simple Clouds**: macOS support (OpenGL 4.3) for each player; overlap with ATMOSPHERICS clouds; client-only mode + Ecliptic.
11. **Presence Not Position**, **Particular Reforged**, **Biome Music**: existence of a Modrinth 1.21.1 NeoForge page (not found).
12. **Unverified for lack of quota** (to rerun): aurora and eclipse mods, torches that go out ("island of light" to maintain), Tide (fishing by species, season and weather, with a logbook), Extreme Sound Muffler (noisy Create factories), radios or walkie-talkies for SVC, Bosses of Mass Destruction.

---

## Main sources
- In Control: [Modrinth KpICtuVx](https://modrinth.com/project/KpICtuVx/version/TBI4EWjs), [GitHub McJtyMods/InControl](https://github.com/McJtyMods/InControl) (changelog `1.21_neo`)
- Naturalist: [GitHub Crispytwig/naturalist (1.21.1-NeoForge)](https://github.com/Crispytwig/naturalist), [BoxToPlay](https://www.boxtoplay.com/en/minecraft-hosting/minecraft-server/modrinth-mods/naturalist-117)
- Friends&Foes: [GitHub Faboslav/friends-and-foes](https://github.com/Faboslav/friends-and-foes)
- Mowzie's Mobs 1.8.2: [Modrinth](https://modrinth.com/mod/mowzies-mobs/version/QfxYwK0i)
- Cosy Critters: [Modrinth](https://modrinth.com/mod/cosy-critters/version/v0.2.0+1.21.1-neoforge); World Alive: [Modrinth](https://modrinth.com/mod/world-alive)
- Longwings: [Modrinth](https://modrinth.com/mod/longwings/version/h13ko2fP); Neo Guan Niao: [Modrinth](https://modrinth.com/mod/neoguanniao); Backyard Birds: [Modrinth](https://modrinth.com/project/pNndCV1F); Fowl Play: [Modrinth](https://modrinth.com/mod/fowlplay)
- Enhanced Celestials: [Modrinth 6.0.2.5](https://modrinth.com/mod/enhanced-celestials/version/6.0.2.5-neoforge), [GitHub (archived)](https://github.com/CorgiTaco/Enhanced-Celestials)
- Simple Clouds: [Modrinth](https://modrinth.com/project/simple-clouds), [GitHub](https://github.com/nonamecrackers2/simple-clouds)
- Weather2: [Modrinth](https://modrinth.com/mod/weather-storms-tornadoes/version/YuNAxTMO)
- Particle Rain: [Modrinth](https://modrinth.com/mod/particle-rain/version/v4-beta.6+1.21.1-neoforge), [CurseForge](https://www.curseforge.com/minecraft/mc-mods/particle-rain/files/8055433)
- Dynamic Surroundings: [GitHub](https://github.com/OreCruncher/DynamicSurroundingsFabric)
- Presence Not Position: [ModDex](https://moddex.gg/mod/presence-not-position); Biome Music: [CurseForge](https://www.curseforge.com/minecraft/mc-mods/biome-music/files/8302329)
- LambDynamicLights: [Modrinth 4.8.9](https://modrinth.com/mod/lambdynamiclights/version/4.8.9+1.21.1); Sodium Dynamic Lights: [Modrinth](https://modrinth.com/mod/sodium-dynamic-lights/version/XI0WLXdn)
- Presence Footsteps x Sable: [Mado](https://www.mado-hosting.com/minecraft-mods/presence-footsteps-x-sable)
- Particular Reforged: [CurseForge](https://www.curseforge.com/minecraft/mc-mods/particular-reforged/files/8275091)
- Subtle Effects: [GitHub](https://github.com/MincraftEinstein/SubtleEffects)
