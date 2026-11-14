# 8. Setup: phases, checks, decisions

> The phases remain valid for the v2 mods. The decisions in 8.4 are replaced by [10.12](10-recentrage.md#1012-decisions-to-validate-replaces-84).

Each phase follows [CONTRIBUTING.md](https://github.com/salvagedisc/compline/blob/main/CONTRIBUTING.md): one `test/<topic>` branch, one mod per commit, config and scripts in `pack/`, a record in `docs/decisions/` for each trade-off, and green CI (`check-deps`, `.mrpack` export) before merging. **`packwiz mr add` settles Modrinth availability**: the research could not establish it for every mod (see [README](../../plan/README.md)), and a mod absent for 1.21.1 NeoForge is replaced by the fallback given in the relevant chapter.

## 8.1 Phases

| Phase | Branch | Content | Exit criterion |
|---|---|---|---|
| **0. Current foundation** | `main` | Launch test of the current pack (already in the `v0.1.0` agenda); Quark config compared with the generated one; **Entity Culling tested on an assembled airship** | World created without a crash; Entity Culling kept (tuned) or removed; tag `v0.1.0` |
| **1. Base rules** | `test/regles-de-base` | Neruina, JEI, In Control!, LootJS, KubeJS Create, MoreJS, Almost Unified; scripts 1 to 6 of [7.2](07-kubejs-et-datapack.md#72-the-scripts-in-the-order-to-write-them) | No vanilla monster in 3 nights of play; End and Nether inaccessible; Super Glue craftable; F3 without coordinates |
| **2. Final world** | `test/streams-nml`, then `test/structures-create` | Streams Reflowing on NML; Lootr, Create Aeronautics Structures, New Beginnings, Let The Adventure Begin (sorted), MCG's Campsites, Cinch's Abandoned Houses; ruined villages; Sparse Structures settings | Decision 0001 closed; 1,000-block radius explored with no noisy structure or vanilla mob; seed chosen |
| **2 bis. Far distance** | `test/distant-horizons` | Distant Horizons + Separate Sable Render Distance, mist settings | Silhouettes in the mist without prohibitive performance loss for the least-equipped player |
| **3. Crossing** | `test/traversee` | Paraglider, ParCool (restricted), Traveler's Backpack, Comforts, Traveler's Titles, Exposure, Better Archeology, GUI Compass, AA4 Atlas | A two-day expedition without coordinates, return to base with map and photos |
| **4. Civilization** | `test/civilisation` | Copycats+, Create Connected (pruned), Factory Logistics, Ore Excavation, Prospecting Picks, Bells & Whistles, Diesel Generators, Power Loader, Rechiseled (+ Create, Fusion); scripts 7 to 9 and 12 | Ages 0 to 7 can be played in order, without shortcuts, solo |
| **5. Daily life** | `test/vie-quotidienne` | Kaleidoscope Compat, Cattlestrophic!, Salt's Animal Farm, Animal Genders, Freeze It and Heat It, Kaleidoscope Tavern, Thirst Canteen, Tide, Nutritional Balance, Hardcore Revival; seasons bridge, unified cooking, Pantry | A full Ecliptic year played: winter felt, a herd that needs care, necessary reserves |
| **6. Fauna & atmosphere** | `test/faune-ambiance` | Configs from 5.3; Cosy Critters, Enhanced Celestials, Reactive Music (songpack chosen) (replaced by True Adaptive Music, [0010](../../decisions/0010-musique.md)), Particle Rain; sounds and luminances | Each biome has its fauna without duplicates; no hordes; the night is dark |
| **7. Colonies** | `test/colonies` | Easy NPC, Create Villager Labor, OPAC; script 10; written survivors | One survivor brought back, settled, at work; no Mending or metal tool trade |
| **8. Server & comfort** | `test/serveur` | Jade, AppleSkin, Mouse Tweaks, Controlling + Searchables, GraveStone, Simple Voice Chat, Crash Assistant, Default Options, Simple Backups; `server.properties`, backups, `scripts/deploy-serverconfig`; settler's notebook | Restoring a backup tested; all players connect with voice |
| **Launch** | `main` | Chunky pregeneration, CHANGELOG, tag `v1.0.0` | Permanent world open |

Phases 3, 4 and 6 are independent once phase 2 is closed and can proceed in parallel. Phase 1 must precede all the others: without it, every test is run in a world that is not Compline's.

## 8.2 For each mod added

1. `packwiz mr add <slug>` (or the sync script from the instance): it fails immediately if 1.21.1 NeoForge does not exist.
2. Set the `side` field (client only for Crash Assistant, Mouse Tweaks, Controlling, Searchables, Default Options, Cosy Critters, Traveler's Titles, ATMOSPHERICS…).
3. `npm run check-deps` for undeclared dependencies.
4. Apply the settings of the relevant chapter ("Settings to apply" tables); note in the decision record the keys that had to be corrected.
5. Update `docs/mods.md`.

## 8.3 What to read in the .jar files

The .jar study session starts with these points, ordered by what they unblock.

**Blocking for phase 1**
1. KubeJS 2101.7: `NativeEvents`, cancellation of `EntityEvents.spawned`, `RecipeViewerEvents`, support for `kubejs/data` for worldgen.
2. Recipes of all .jar files that use `slime_ball`, `blaze_rod`, `blaze_powder`, `gunpowder`, `ender_pearl`, `phantom_membrane`, `string`, `bone`, `quartz`, and any Nether item.
3. In Control! 10.x: semantics of `mincount` (with `perplayer`) and of `maxdist`.
4. Entity Culling: keys `tickCulling`, `skipBlockEntityCulling`.

**Blocking for phase 2**
5. Sparse Structures 1.21.1: format of per-structure overrides.
6. Create Aeronautics Structures: nature of the "robber balloons", loot tables, engineer trades.
7. Create: New Beginnings: targeted pools (including the "zombie" variants?).
8. Let The Adventure Begin: structure inventory for sorting.
9. No Man's Land: does it modify villages? Names of the template pools to override.
10. Streams Reflowing + NML: combined generation.

**Blocking for phases 3 to 7**
11. Overgeared: quality component, recipe types, vanilla tool config, presence of tin and bronze, steel pattern loot, Nether molds and alloys.
12. Paraglider: 1.21.1 build on Modrinth; ParCool: limitation keys and Paraglider stamina option.
13. Traveler's Backpack: capabilities, sleeping bag, water purity in tanks.
14. Exposure: photo data, coordinates in the tooltip.
15. Create Connected, Ore Excavation, Power Loader, OPAC: config keys cited in chapters 3 and 6.
16. Diesel Generators: origin of oil.
17. Create Aeronautics: identifiers of propellers, burners and balloons; accepted fuel; floating material.
18. Naturalist 2.0.4 (47 animals), Alex's Mobs Continued 2.2.2 (spawn config format), Mowzie's 1.8.2, Hybrid Aquatic: species lists and keys.
19. Almost Unified: `c:` tags shared between Farm & Charm and Kaleidoscope Cookery, and between Overgeared and Create.
20. ~~Actual presence on Modrinth~~: verified on 2026-10-10, see [9](09-disponibilite-modrinth.md). Still to check in the .jar: the recipes of Create Villager Labor, the tracks of Reactive Music, and that KubeJS Create 2101.3.1 (beta, November 2025) runs with Create 6.0.10.

Points specific to daily life are listed in [4.8](04-vie-quotidienne.md#48-to-check-in-the-jar-files).

## 8.4 Decisions to validate together

For each one, the plan applies the default shown; it is enough to say whether to keep it.

| # | Decision | Default retained by the plan | Alternative |
|---|---|---|---|
| 1 | **The Nether** | Closed like the End, with replacement recipes ([3.8](03-civilisation.md#38-the-nether-closed-by-default)) | Open as a late expedition, with its vanilla monsters kept local |
| 2 | Rivers | NML + Streams Reflowing, if the test passes ([2.6](02-exploration.md#26-rivers-streams-reflowing-on-no-mans-land)) | Keep the comparison with Terralith |
| 3 | Entity Culling | Tuned, removed if it still breaks contraptions | — |
| 4 | Electricity | Stay mechanical and diesel (option A) | Crafts & Additions in the late game |
| 5 | Engineer blueprints | System adopted, Quark Automatic Recipe Unlock disabled, `doLimitedCrafting` | Free recipes, progression only through forged-part locks |
| 6 | Ancient cities | Kept empty, without Warden | Removed |
| 7 | Photography | Exposure (a profession) | Camerapture (simpler) |
| 8 | Colonies | Easy NPC + Create Villager Labor, without MineColonies | Easy NPC survivors only, without automated work |
| 9 | Voice | Simple Voice Chat, groups disabled | Groups allowed for expeditions |
| 10 | Mowzie's Umvuthana (masked hostile tribes) | Disabled | Kept very rare |
| 11 | Towers of the Wild Modded | Not retained (close to Structory: Towers) | Very rare ruined towers, glider in the chest |
| 12 | Natural regeneration | Kept; Hardcore Revival, hunger and thirst are enough | Disabled |
| 13 | Animal husbandry | Three small mods (Cattlestrophic, Salt's, Animal Genders) | Vanilla husbandry + F&C buildings only |
| 14 | Preservation | Freeze It and Heat It + Pantry limited to expedition rations | Pantry extended to all food |
| 15 | Fishing | Tide | River Fishing (more demanding, adds a fisherman NPC) |
| 16 | Wine and cellar | Kaleidoscope Tavern instead of Vinery | Nothing for now |

