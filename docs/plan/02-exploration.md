# 2. Exploration: "I wonder what's over there"

> **History (plan v1, 2026-10-10).** Superseded by chapters [10](10-recentrage.md) to [12](12-verification.md), the [registry](../mods.md) and the [decisions](../decisions/README.md): several mods listed here have since been dropped. Kept for its reasoning.

What already exists: No Man's Land (worldgen and atmospheric places), Sparse Structures, Antique Atlas 4 + Surveyor, Icy's Better Horses, Supplementaries (rope, pulley, hand-placed signs, antique ink), Quark (essential). This chapter adds **Breath of the Wild-style traversal**, **places of lost civilisation**, **rewards that are knowledge**, and **navigation without coordinates**.

Mod-by-mod detail, evidence and keys: [recherche/research-exploration.md](recherche/research-exploration.md).

## 2.1 The game loop

1. **See.** From the base, a silhouette in the mist: a summit, an airship wreck, a column of campfire smoke (a survivor).
2. **Find your way without coordinates.** Sun and stars, compass in hand, signs and flags placed by players, region name on entry (Traveler's Titles).
3. **Traverse.** On foot with a stamina gauge (Paraglider), climbing ledges (restrained ParCool), gliding from heights and from the updrafts of campfires, on horseback, by boat, later by train and airship.
4. **Camp.** Backpack (water, tools), sleeping bag, fire (Cold Sweat warmth and take-off point).
5. **Discover** a rare, silent place: NML shrine, abandoned house, ruined village, Create remnant, wreck, survivor camp.
6. **Bring back**: the map drawn while walking, photos, an engineer's blueprint, a journal, orbs, a survivor.
7. **Convert** at the base: the blueprint unlocks a recipe, the survivor becomes an inhabitant, the orbs become stamina. The new machines make it possible to go further.

## 2.2 Proposed mods

| Mod | Role in the loop | 1.21.1 NeoForge availability on Modrinth | Proposed status |
|---|---|---|---|
| **Lootr** | Chests generated per player: in multiplayer, the second to arrive still finds something | yes (1.11.37.122) | core |
| **Paraglider** | Glider, stamina wheel, updrafts, BotW-style statues and orbs. Replaces the removed Elytra | yes (21.1.5, `paragliders`) | core |
| **ParCool!** (restrained) | Ledge grab and climb, wall jump, roll, vault, crawl. Stamina provided by Paraglider | yes (4.0.0.5) | recommended |
| **Create Aeronautics Structures** | Ruined engineer houses (a peaceful engineer lives there: a survivor), airship and boat wrecks, broken balloons | yes (beta) | recommended |
| **Create: New Beginnings** | Create remnants added to villages without overwriting vanilla pieces | yes (0.1.1) | recommended |
| **Create: Let The Adventure Begin** | Small standalone "Create" structures (datapack) | yes (4.0.3) | recommended, to be pruned: keep only the most understated |
| **MCG's RPG Campsites** | 8 rare camps, 7 of them with a professional villager: the "survivors" concept of decision 0004 | yes (1.0.0) | recommended |
| **Cinch's Abandoned Houses** | Isolated houses at various stages of decay | yes (2.0.1) | recommended |
| **Exposure** | Analogue photography: film, darkroom development, printing. A craft and proof of discovery | yes (1.9.18) | recommended |
| **Traveler's Backpack** | Worn pack with two water tanks and tool slots | yes (10.1.38) | recommended |
| **Comforts** | Sleeping bags (without changing the respawn point) and hammocks | yes (9.0.3) | recommended |
| **Traveler's Titles** (+ YUNG's API) | Biome name as a title on entry. Compensates for the biome hidden by `reducedDebugInfo`. It is a display, not YUNG's structures | yes (5.1.3) | recommended |
| **Better Archeology** | Fossils to assemble, artefacts to identify: the brush becomes a craft, the colony has a museum | yes (1.3.4) | to test |
| **GUI Compass** | Direction shown at the top of the screen only with a compass in the inventory, no coordinates (`guiCompassFormat = "F"`). Replaces Dead Reckoning, which exists only on Fabric | yes (5.0, native) | recommended; fallback NeoCompass, always displayed |
| **AA4 Atlas** | The atlas becomes a crafted item that you carry and can lose | yes (1.1.2, via Connector like Antique Atlas) | recommended |
| **Streams Reflowing** | Streams that follow the terrain, real current on vanilla water, waterfalls. Seems to add to NML without Terralith | yes (2.13.1) | **branch `test/streams-nml` as a priority** (see 2.6) |
| **Distant Horizons** + **Separate Sable Render Distance** | Distant silhouettes in the mist | already "to test" | branch `test/distant-horizons` (settings in [6](06-technique-et-serveur.md#64-distant-horizons)) |

**Optional** (to be shown in screenshots before deciding): CTOV (more varied village ruins), Towers of the Wild Modded (BotW towers, native Paraglider compatibility; close to Structory: Towers, rejected on taste), Create: Structures Arise and Create: Rustic Structures (if New Beginnings is not enough), IDAS (already "to test"), Antique Trains (Create network on the atlas), The Athenaeum and Explorer's Journal (not verified).

**Rejected**:
- Sophisticated Backpacks: storage upgrades that duplicate Create.
- Grappling hooks (Hooked, Yori3o): make verticality trivial.
- Wandering Villagers: spawns villagers near players, which kills discovery.
- SWEM: duplicate of Icy's Better Horses.
- Create: Dynamic Village: CurseForge only.
- Camerapture: duplicate of Exposure, which also brings a real craft (see [3](03-civilisation.md)).

## 2.3 Structures: rarity, silence, ruins

**Principle**: no structure produces sound, strong light or a hostile creature. The only signal visible from afar is the smoke of a survivor's campfire.

Sparse Structures settings (format of per-structure overrides to be read in the .jar):

| Group | `spreadFactor` |
|---|---|
| Global | 3 |
| Small NML places (menhirs, wells, shrines) | 1.5 to 2 |
| Cinch's Abandoned Houses | 3 |
| Abandoned villages | 4 |
| New Beginnings, Let The Adventure Begin | 4 to 5 |
| Survivor camps, wrecks and engineer houses | 6 |

**To disable** (empty structure set in a datapack): strongholds (already decided), trial chambers, pillager outposts, mansions. **To empty** rather than remove: ocean monuments (underwater ruins without guardians), witch huts. **Ancient cities**: proposed to keep them **empty** (without Warden or shrieking sculk sensors) as a strange place of the deep; to be validated in game. Shipwrecks, ocean ruins and desert wells stay as they are.

**Ruined villages** (concept 0004), a home-made datapack rather than a mod:
1. Override `minecraft:village/<type>/town_centers` to keep only the centres of the "zombie" variant, which lead to the dilapidated houses.
2. Cancel in KubeJS the zombie villagers placed in these pieces (the safety net of [5.1](05-faune-et-ambiance.md#51-removing-vanilla-monsters)). Option: a 1 in 100 chance of replacing them with a hidden survivor.
3. Remove iron golems and village cats.
4. Inject home-made pieces with Lithostitched (already present): Paraglider statue in a ruin (= shrine), Create remnant, journal chest.

**Survivor camps** (MCG's RPG Campsites): remove the ruined portal, replace the village loot chest with a journal, filter the cartographer's exploration maps, add a **hay bale under the campfire** so the smoke rises high.

## 2.4 Rewards: knowledge rather than diamonds

| Reward | Where it is found | What it unlocks | Tool |
|---|---|---|---|
| **Engineer's blueprints** (KubeJS items, one per tier) | Aeronautics wrecks, engineer houses, New Beginnings remnants | A batch of Create, Steam 'n' Rails or Aeronautics recipes ([3.4](03-civilisation.md#34-progression-tree)) | `doLimitedCrafting` on a restricted list + `player.unlockRecipes` |
| **Steel forging template** (Overgeared) | Survivor camps, ruins | The steel upgrade | NeoForge loot modifier `neoforge:add_table` |
| **Spirit orbs** (Paraglider) | Only NML shrines and monuments | Traded to a statue for stamina | Loot modifier + Paraglider config |
| **Journals and lore** | Houses, ruined villages, camps (per player with Lootr) | Story of the vanished civilisation, vague directions ("beyond the second ridge, towards the sunset") | Books written in a datapack |
| **Photos** (Exposure) | Taken by the player | Rewards in KubeJS if the view data allows it ("photograph a Frostmaw") | To check in the .jar |
| **Fossils, artefacts** | Better Archeology sites | Colony museum | — |
| **Survivors** | Camps, engineer houses, ruins | A skilled inhabitant: a trade in the colony ([3.6](03-civilisation.md#36-colonies-and-survivors)) | KubeJS rules |
| **Discovery achievements** | Entering a place or biome | A page of the settler's notebook, no resources | Datapack advancements |

**Conflict to resolve**: the Quark "Automatic Recipe Unlock" module (enabled in 0005) unlocks all recipes and makes the blueprints pointless. **Proposed: disable it.** Only the key recipes are locked; the rest of the recipe book works normally.

## 2.5 Navigating without coordinates

1. Gamerule `reducedDebugInfo true`, forced when the world loads ([7](07-kubejs-et-datapack.md)). It hides XYZ, block, chunk and biome in F3.
2. Leaks to plug: coordinates on the Supplementaries compass (already cut), GraveStone obituary, Jade coordinates plugin, Exposure tooltips, Antique Atlas screen, Distant Horizons F3 lines. To be checked one by one at first launch.
3. Commands `/locate`, `/tp`, `/waypoints` and `/landmarks` reserved for operators.
4. **Surveyor**: `globalSharing=false`. Everyone draws their own map; sharing goes through copying to a vanilla map, which is brought back to the base.
5. **Antique Atlas**: atlas required in the inventory to open the map. Only automatically mark the NML monuments, wrecks and camps; small ruins remain a personal discovery.
6. **GUI Compass**: `guiCompassFormat = "F"` (direction only, never X, Y, Z), `mustHaveCompassInInventory = true`, `compassPositionIsCenter = true`. Client config, delivered by Default Options.
7. Nothing technically prevents a friend from installing a minimap client-side: it is a pact between players, to be written in the README.

## 2.6 Rivers: Streams Reflowing on No Man's Land

Decision 0001 pitted NML against Terralith + Streams Reflowing. The research shows that Streams Reflowing **digs its streams independently of the biome source** and applies a current to vanilla water. We could therefore have **the atmosphere of NML and the rivers of Streams** at the same time, without Terralith.

Test to run on `test/streams-nml` before pregeneration:
- double riverbed with the NML and Biolith rivers?
- Sable ships and vanilla boats in the current: pushed, ignored or unstable?
- do Create water wheels turn faster in the current?
- generation cost (Chunky over a radius of 1,000 blocks, measured with spark).

If the test is conclusive, decision 0001 becomes "NML + Streams Reflowing" and the comparison with Terralith is no longer relevant.

## 2.7 Settings to apply

| Mod | File | Setting | Reason |
|---|---|---|---|
| Paraglider | `paraglider-common.toml` | `features.heartContainers = false` | Survival stays environmental |
| Paraglider | `paraglider-server.toml` | `spiritOrbs.raidGivesVessel = false`, `spiritOrbs.witherDropsVessel = false` | No raids or Wither |
| Paraglider | same | `spiritOrbs.spawnerSpiritOrbDrops = 0`, `spiritOrbs.trialSpiritOrbDrops = 0`, `spiritOrbs.ominousTrialSpiritOrbDrops = 0` | Orbs only in NML shrines |
| Paraglider | same | `stamina.runningAndSwimmingConsumesStamina = true` | Running and swimming become a choice |
| Paraglider | same | `paragliderDurability = 600` | Repair at Overgeared |
| Paraglider | same | `updraft = true`, `features.villageStructures = true` | Campfires = take-off points; statues copied into ruins |
| ParCool | server config | Keep: grab, ledge climb, wall jump, roll, vault, crawl. Cut: wall run, dodge, flips, charged jump, cat leap, fast run. Stamina = Paraglider | BotW-style climbing without super-manoeuvres |
| Lootr | `lootr-common.toml` | No refresh or decay, protection against breaking, conversion of mod structures | Every discovery is unique |
| Traveler's Backpack | common config | Variant capabilities cut; sleeping bag without respawn point; check that the tanks keep the water purity of Thirst | No magical effects or infinite canteen |
| Surveyor | `surveyor.toml` | `globalSharing = false` | Personal map |
| Antique Atlas | `antique-atlas.toml` | Atlas required in the inventory; restricted list of marked structures | The map is an object |
| Quark | `quark-common.toml` | Automatic Recipe Unlock = false | The engineer's blueprints make sense |
| MCG's Campsites | datapack | Ruined portal removed, hay bale under the fire, cartographer trades filtered | Survivors concept |
| Create Aeronautics Structures | KubeJS | Engineer trades filtered; check the nature of the "robber balloons" | No progression shortcut |

The Paraglider key names come from the source code of the `release/1.21.1` branch. The others are to be confirmed in the .jars.
