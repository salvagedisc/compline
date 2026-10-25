# 7. Rules layer: KubeJS scripts and datapack data

> **History (plan v1, 2026-10-10).** Superseded by chapters [10](10-recentrage.md) to [12](12-verification.md), the [registry](../mods.md) and the [decisions](../decisions/README.md): several mods listed here have since been dropped. Kept for its reasoning.

Everything that ties the mods together goes through KubeJS (already in the pack) and through data loaded from `pack/kubejs/data/` (KubeJS loads it as an active datapack on all worlds). The **drafts** are in [kubejs-brouillons/](kubejs-brouillons/), laid out as in `pack/kubejs/`: they show the form and the logic, but **no identifier is guaranteed** until the .jar files have been read. That is the job of the .jar study session ([8.3](08-mise-en-place.md#83-what-to-read-in-the-jar-files)).

## 7.1 Organisation

```
pack/kubejs/
├── startup_scripts/
│   └── objets_fallow.js            engineer blueprints, forged parts, ember core, saltpetre
├── server_scripts/
│   ├── 00_fallow_constants.js      shared lists (banned hostiles, blueprints → recipes)
│   ├── no_end_no_nether.js         End and Nether closed
│   ├── spawns_safety.js            vanilla monster safety net
│   ├── survivants.js               breeding blocked, trades filtered (MoreJS)
│   ├── plans_ingenieur.js          recipe unlocking through blueprints
│   ├── saisons_cold_sweat.js       Ecliptic Seasons → Cold Sweat bridge
│   ├── garde_manger.js             slow spoilage, cellar, cooler
│   ├── recipes/
│   │   ├── progression.js          Overgeared → Create → rail → aviation locks
│   │   ├── remplacements.js        slime, gunpowder, Blaze Burner, quartz
│   │   ├── gisements_coe.js        finite deposits per biome
│   │   ├── cuisine_unifiee.js      Farm & Charm confined to the farm, season tags
│   │   └── overgeared_pieces.js    forge recipes for fallow:* parts (recipe types to be read)
│   └── loot/
│       └── butin.js                elytra and eye removed, bones, blueprints, orbs (LootJS)
├── client_scripts/
│   └── visionneuse.js              entries hidden in JEI
├── data/
│   ├── fallow/function/init.mcfunction          gamerules on load
│   ├── minecraft/tags/function/load.json        → fallow:init
│   ├── fallow/neoforge/biome_modifier/*.json    monsters, dungeons, infested blocks removed
│   ├── minecraft/tags/worldgen/biome/has_structure/stronghold.json   no more strongholds
│   ├── minecraft/tags/enchantment/{tradeable,on_traded_equipment}.json   no more traded Mending
│   ├── minecraft/worldgen/template_pool/village/*/town_centers.json  ruined villages (to be written)
│   ├── minecraft/worldgen/structure_set/*.json  structures removed (to be written)
│   ├── fallow/advancement/**                    settler's notebook achievements (to be written)
│   └── <mod>/neoforge/biome_modifier/*.json     {"type":"neoforge:none"} to neutralise a modifier
└── assets/
    ├── fallow/…                    textures and French names of the items
    ├── ambientsounds/…             rare distant sounds (see 5.6)
    └── minecraft/…                 menu panorama, splashes in French
pack/config/incontrol/spawn.json    In Control! rules
```

## 7.2 The scripts, in the order to write them

| # | Script or data | What it does | Depends on | Draft |
|---|---|---|---|---|
| 1 | `fallow:init` + `load.json` | World gamerules, including `reducedDebugInfo` and `doLimitedCrafting` | — | ✅ |
| 2 | `no_end_no_nether.js` + stronghold tag | End and Nether closed, eye recipes removed | `NativeEvents` | ✅ |
| 3 | Biome modifiers + `spawns_safety.js` + `incontrol/spawn.json` | No more vanilla monsters, modded hostiles capped | In Control! | ✅ |
| 4 | `saisons_cold_sweat.js` | Seasonal felt temperature | Ecliptic and Cold Sweat bindings | ✅ |
| 5 | `recipes/remplacements.js` | Slime, gunpowder, Blaze Burner, quartz | KubeJS Create | ✅ |
| 6 | `recipes/cuisine_unifiee.js` + Kaleidoscope Compat | Farm & Charm confined to the farm, KC as the only cooking system, feeder removed | Kaleidoscope Compat | ✅ |
| 7 | `objets_fallow.js` + `recipes/overgeared_pieces.js` | Forged parts that lock the machines | Overgeared recipe types | partial |
| 8 | `recipes/progression.js` | Locks by age | 7, KubeJS Create | ✅ |
| 9 | `plans_ingenieur.js` + `loot/butin.js` | Blueprints found while exploring that unlock recipes | 7, LootJS, Quark Auto Unlock cut | ✅ |
| 10 | `survivants.js` + enchantment tags | Survivors: no breeding, filtered trades | MoreJS | ✅ |
| 11 | Village pools + structure sets | Ruined villages, structures removed | Sparse Structures | to be written |
| 12 | `recipes/gisements_coe.js` | Finite deposits per biome | COE | ✅ |
| 13 | ~~`garde_manger.js`~~ (removed in v2) + Cattlestrophic breeding config | Livestock feed | Cattlestrophic | config only |
| 14 | `visionneuse.js` | Hide what does not exist | — | ✅ |
| 15 | Achievements + Modonomicon notebook | Settler's guide by age | — | to be written |
| 16 | Assets: AmbientSounds sounds, LambDynamicLights luminances, ATMOSPHERICS presets, panorama | Ambience | — | to be written |
| 17 | `clairieres.js` (startup + server) + `fallow:clairiere` biome + ATMOSPHERICS preset ([10.3](10-recentrage.md#103-the-mist-that-lifts)) | Clearing fires: the mist recedes around the base | ATMOSPHERICS, Overgeared, Create | ✅ (draft) |
| 18 | `zoo.js` + `lune_blanche.json` | Wildex bestiary rewards; white moon | Wildex Bestiary, Enhanced Celestials | moon ✅ (draft), zoo to be written |

Numbers 1 to 6 must exist **before the first serious test world**: without them, the generated world is not Fallow's (monsters, strongholds, basic recipes).

## 7.3 API points to confirm

These points condition several drafts; they can be checked in one evening with ProbeJS in the test instance:
- Does `NativeEvents.onEvent` exist in KubeJS 2101.7, and does it accept inner classes (`BlockEvent$PortalSpawnEvent`)?
- Is `EntityEvents.spawned` cancellable, and is `entity.tags` accessible?
- `RecipeViewerEvents.removeEntries` (KubeJS 2101's JEI integration): exact name.
- Is `kubejs/data` taken into account for **worldgen from world creation** on a dedicated server? Otherwise, plan B: the same folder copied into `world/datapacks/` before the first generation.
- LootJS 3 and MoreJS syntax for 1.21.1; compatibility of KubeJS Create with Create 6.0.10.
- Overgeared: identifier of the quality component and the recipe types exposed.
