# 1. Proposed mod list

> Superseded by the [refocus of 2026-10-11](10-recentrage.md): this table describes v1.

Overview of all mods, retained and proposed. Each chapter gives the detailed role, the settings and the evidence.

Status legend:
- **✅ retained**: already in `pack/mods/` (decisions 0001 to 0007);
- **➕ core**: to be added, the plan relies on it;
- **➕ recommended**: to be added, brings a clear mechanic;
- **🧪 branch**: to be tested on a `test/*` branch before a decision;
- **◻ optional**: to be shown, may be added later.

"Modrinth" column: checked on 2026-10-10 against the Modrinth API. **yes** = 1.21.1 NeoForge file published; **Connector** = multi-loader file from the Surveyor family, loaded by Sinytra Connector; **absent** = nothing for 1.21.1 NeoForge. Slugs, versions and dependencies: [9. Availability on Modrinth](09-disponibilite-modrinth.md).

## In numbers

| | Mods |
|---|---|
| Already retained | 46 (+ 24 dependencies) |
| Core to add | 16 |
| Recommended to add | 40 |
| To test on a branch | 8 |
| Target pack at launch | about 100 mods, plus their dependencies |

## World and exploration ([2](02-exploration.md))

| Mod | Status | Side | Modrinth | Role |
|---|---|---|---|---|
| No Man's Land | ✅ retained | CS | yes | Worldgen |
| Sparse Structures | ✅ retained | CS | yes | Structure spacing |
| Antique Atlas 4 + Surveyor | ✅ retained | C / CS | yes | Map that draws itself as you walk |
| Icy's Better Horses | ✅ retained | CS | yes | Horses, carts |
| **Lootr** | ➕ core | CS | yes | Per-player chests |
| **Paraglider** | ➕ core | CS | yes | Glider, stamina, updrafts |
| **ParCool!** (restrained) | ➕ recommended | CS | yes | Ledge climbing, wall jump |
| **Create Aeronautics Structures** | ➕ recommended | CS | yes | Wrecks, engineer houses |
| **Create: New Beginnings** | ➕ recommended | CS | yes | Create remnants in villages |
| **Create: Let The Adventure Begin** (pruned) | ➕ recommended | S | yes | Small Create structures |
| **MCG's RPG Campsites** | ➕ recommended | CS | yes | Survivor camps |
| **Cinch's Abandoned Houses** | ➕ recommended | CS | yes | Abandoned houses |
| **Exposure** | ➕ recommended | CS | yes | Analogue photography |
| **Traveler's Backpack** | ➕ recommended | CS | yes | Expedition pack |
| **Comforts** | ➕ recommended | CS | yes | Sleeping bags, hammocks |
| **Traveler's Titles** (+ YUNG's API) | ➕ recommended | C | yes | Region name on entry |
| **GUI Compass** (replaces Dead Reckoning, absent) | ➕ recommended | C | yes | Direction only, compass in the inventory (fallback: NeoCompass) |
| **AA4 Atlas** | ➕ recommended | CS | Connector | The atlas becomes an item |
| Streams Reflowing | 🧪 `test/streams-nml` | S | yes | Flowing rivers on NML |
| Distant Horizons + Separate Sable Render Distance | 🧪 `test/distant-horizons` | CS | yes | Distant silhouettes |
| Better Archeology | 🧪 | CS | yes | Fossils, artefacts |
| CTOV, Towers of the Wild Modded, Create: Structures Arise, Create: Rustic Structures, IDAS, Antique Trains | ◻ optional | — | yes (Antique Trains: Connector) | See 2.2 |

## Civilisation ([3](03-civilisation.md))

| Mod | Status | Side | Modrinth | Role |
|---|---|---|---|---|
| Overgeared | ✅ retained | CS | yes | Forge, quality, alloys |
| Create | ✅ retained | CS | yes | Industry |
| Create Aeronautics + Sable | ✅ retained | CS | yes | Aviation |
| Steam 'n' Rails (NeoForge port) | ✅ retained | CS | yes | Rail |
| Quark, Supplementaries | ✅ retained (essential) | CS | yes | Comfort, building |
| **Create: Copycats+** | ➕ core | CS | yes | Copyable shapes on contraptions |
| **Create: Connected** (pruned) | ➕ core | CS | yes | Fine mechanics |
| **Create Factory Logistics** | ➕ core | CS | yes | Fluids in packages |
| **Create Ore Excavation** | ➕ core | CS | yes | Finite deposits |
| **Prospecting Picks** | ➕ recommended | CS | yes | Prospecting in the early game |
| **Create: Bells & Whistles** | ➕ recommended | CS | yes | Stations |
| **Create: Diesel Generators** | ➕ recommended | CS | yes | Combustion tier |
| **Create: Power Loader** | ➕ recommended | CS | yes | Chunk loading with energy |
| **Rechiseled** + **Rechiseled: Create** (+ Fusion) | ➕ recommended | CS | yes | Block variants |
| **Easy NPC** | ➕ recommended | CS | yes | Scripted survivors |
| **Create Villager Labor** (replaces Villager Workers, absent) | 🧪 `test/colonies` | CS | yes | Survivors at the stations of Create chains |
| **Open Parties and Claims** | ➕ recommended | CS | yes | Colony protection |
| Create Crafts & Additions, Numismatics, COE Deposits, Create: Interiors, Transmission & Linkage, SableCat, Villagers Reborn, Aeronautics: Winds and Weather | ◻ optional | — | yes | See 3.2 and 3.6 |

## Daily life ([4](04-vie-quotidienne.md))

| Mod | Status | Side | Modrinth | Role |
|---|---|---|---|---|
| Kaleidoscope Cookery + KC-Automation | ✅ retained | CS | yes | Cooking |
| Farm & Charm | ✅ retained | CS | yes | Farm infrastructure |
| Ecliptic Seasons | ✅ retained | CS | yes | Seasons |
| Cold Sweat, Thirst Was Reclaimed | ✅ retained | CS | yes | Temperature, thirst |
| **Kaleidoscope Compat** | ➕ core | CS | yes | KC ↔ Farm & Charm merge |
| **Cattlestrophic!** | ➕ core | CS | yes | Animal hunger and ageing |
| **Salt's Animal Farm** | ➕ core | CS | yes | Comfort, shelter, disease |
| **Freeze It and Heat It** | ➕ core | CS | yes | Food that freezes or spoils |
| **Animal Genders** | ➕ recommended | CS | yes | Males and females |
| **Kaleidoscope Tavern** | ➕ recommended | CS | yes | Vineyard, brewery, cellar |
| **Thirst Canteen** | ➕ recommended | CS | yes | Canteens |
| **Tide** | ➕ recommended | CS | yes | Fishing |
| **Nutritional Balance** | ➕ recommended | CS | yes | Nutrition |
| **Hardcore Revival** | ➕ recommended | CS | yes | Co-op revival |
| **AppleSkin** | ➕ recommended | CS | yes | Hunger and saturation preview |
| River Fishing | 🧪 versus Tide | CS | yes | Simulation fishing |
| Kaleidoscope Contraption, Kaleidoscope Orchard, Kaleidoscope Grilling, Fisherman's Trap, Butchery, Aquatic Breeding, Complicated Bees, Nourished, Sleep Quality | ◻ optional | — | yes | See 4.3 |

## Fauna and atmosphere ([5](05-faune-et-ambiance.md))

| Mod | Status | Side | Modrinth | Role |
|---|---|---|---|---|
| Naturalist, Alex's Mobs Continued, Hybrid Aquatic, Mowzie's Mobs | ✅ retained | CS | yes | Fauna |
| Critters and Companions, Untitled Duck Mod, Friends & Foes, Adorable Hamster Pets, Companions!, Ghost, Companion | ✅ retained | CS | yes | Companions |
| ATMOSPHERICS, AmbientSounds, LambDynamicLights | ✅ retained | C | yes | Mist, sounds, light |
| Sound Physics Remastered, Subtle Effects, More Darkness | ✅ retained | CS | yes | Reverb, particles, nights |
| **Cosy Critters & Creepy Crawlies** | ➕ recommended | C | yes | Birds and moths |
| **Enhanced Celestials** | ➕ recommended | CS | yes | Moons (without blood moon) |
| **Simple Voice Chat** | ➕ recommended | CS | yes | Proximity voice |
| **Reactive Music** (replaces Presence Not Position, absent) (replaced by True Adaptive Music, [0010](../decisions/0010-musique.md)) | ➕ recommended | C | yes | Music according to place, time and danger; songpack to be chosen |
| Particle Rain | 🧪 | C | yes | Rain as particles |
| Simple Clouds | 🧪 client only | C | yes | Storms visible from afar |
| Longwings | ◻ optional | CS | yes | Butterflies to collect |

## Technical foundation and server ([6](06-technique-et-serveur.md))

| Mod | Status | Side | Modrinth | Role |
|---|---|---|---|---|
| Sodium, ImmediatelyFast, ModernFix, FerriteCore, Lithium, Clumps, Structure Layout Optimizer, Chunky, spark | ✅ retained | C / CS | yes | Performance, server |
| Entity Culling | ✅ retained, **under watch** | C | yes | Breaks Aeronautics contraptions: to be configured or removed |
| Sinytra Connector + Forgified Fabric API | ✅ retained (dependency) | CS | yes | For Antique Atlas and Surveyor |
| KubeJS | ✅ retained | CS | yes | Rules |
| **Neruina** | ➕ core | CS | yes | Crashing entities paused |
| **Simple Backups** | ➕ core | S | yes | In-game backups |
| **JEI** | ➕ core | CS | yes | Recipe viewer |
| **In Control!** | ➕ core | S | yes | Spawn rules |
| **LootJS** | ➕ core | CS | yes | Loot |
| **KubeJS Create** | ➕ core | CS | yes | Create recipes in KubeJS |
| **MoreJS** | ➕ recommended | CS | yes | Villager trades |
| **Almost Unified** | ➕ recommended | CS | yes | Unification by tags |
| **Jade** | ➕ recommended | CS | yes | Block tooltips |
| **Mouse Tweaks**, **Controlling**, **Searchables** | ➕ recommended | C | yes | Comfort |
| **GraveStone Mod** | ➕ recommended | CS | yes | Gravestones |
| **Crash Assistant** | ➕ recommended | C | yes | Help after a crash |
| **Default Options** | ➕ recommended | C | yes | Default settings |
| ProbeJS | dev tool (not in `pack/`) | — | — | KubeJS autocompletion |
| EMI, Polymorph, Packet Fixer, First-person Model, Not Enough Animations, Shoulder Surfing Reloaded, FancyMenu + Drippy, Better Advancements, Sound Physics Aeronautics | ◻ optional | — | yes | See 6.2 |

## Newly rejected (to be added to the registry)

| Mod | Reason |
|---|---|
| Sophisticated Backpacks, Sophisticated Storage, Tom's Simple Storage | "Magic" storage that duplicates Create 6 |
| Grappling hooks (Hooked, Yori3o) | Trivial verticality |
| Wandering Villagers | Villagers that spawn near players |
| SWEM | Duplicate of Icy's Better Horses |
| Create: Dynamic Village, FTB Quests, FTB Backups 3, Animania Reborn, Serene Seasons API Stub, MineColonies | CurseForge only (for 1.21.1) |
| Camerapture, Immersive Paintings | Duplicates of Exposure |
| Create Big Cannons, Create: Hypertubes, Create: Mobile Packages, Create: Trading Floor | Contrary to the vision |
| Create: New Age, Create: The Factory Must Grow, Create: Metallurgy, Create Deco | Duplicates |
| Create: Enchantment Industry | Duplicates enchanted books |
| Create: Central Kitchen | KC integration removed |
| FramedBlocks, LittleTiles, Domum Ornamentum, building wands | Duplicates or shortcuts |
| Lightman's Currency, Ender Mail, Mighty Mail | Duplicate or instantaneous |
| Guard Villagers, Taterzens, MCA Reborn | Contrary to the survivors concept |
| FTB Chunks | Full-screen map |
| Corail Tombstone, YIGD | Magic, teleportation; Fabric |
| ServerCore | Risk of breaking Create |
| Open Loader, Global Packs, Catalogue, Inventory Profiles Next, Emotecraft, Carry On, Sodium Extra, Embeddium | Useless, duplicates or incompatible |
| Weather2, Fowl Play, Wisp & Willow, Dynamic Surroundings, True Darkness Biomes, Truly Dark | Conflict with Ecliptic, risk of corruption, magic, duplicates |
| Let's Do Vinery | Duplicate of Kaleidoscope Tavern |
| ShelfLife, SpoiledZ, Food Spoiling, Diet, Hourglass | Outside Modrinth or no 1.21.1 |
| Dead Reckoning, Biome Music, Configured | Fabric only for 1.21.1 |
| Villager Workers (Hired Workers), Villager Recruits, Presence Not Position | No 1.21.1 NeoForge file on Modrinth |
| Create Wells, Just a Well, First Aid New, Butchercraft, Barnyard Buddies, Animal Feeding Trough | Duplicates, heavy config or autonomous breeding |

**Blacklist** (incompatible with Aeronautics): Does It Tick, Flowing Fluids, Leaves Be Gone, Flashback, Tpsum, Refurbished Furniture, Carry On.
