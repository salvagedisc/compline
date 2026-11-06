# 3. Civilisation: from the forge to the airship, then to the colonies

> **History (plan v1, 2026-10-10).** Superseded by chapters [10](10-recentrage.md) to [12](12-verification.md), the [registry](../../mods.md) and the [decisions](../../decisions/README.md): several mods listed here have since been dropped. Kept for its reasoning.

What already exists: Overgeared, Create 6.0.10, Create Aeronautics 1.3.2 + Sable, Steam 'n' Rails (NeoForge beta port), KaleidoscopeCookery-Automation, Supplementaries and Quark (essentials). This chapter sets **who does what** between the craftsman and the factory, picks a small number of Create addons without stacking, and proposes a **progression tree** locked in KubeJS.

Mod-by-mod detail, evidence and keys: [recherche/research-civilisation.md](recherche/research-civilisation.md).

## 3.1 The principle: the craftsman makes quality, the factory makes quantity

- **Overgeared** remains the only source of quality tools and armor. No metal tool comes out of the crafting table or a Create machine.
- **Create** mass-produces intermediate parts, but a press never produces better than "decent" quality.
- **Critical parts** require a hand-forged part of minimum quality: drill head, bogies and axles, engine block, propeller blades.
- No Overgeared ↔ Create bridge exists: everything goes through KubeJS ([7](07-kubejs-et-datapack.md)).

**Create 6 already covers storage, trade and mail.** Packages, frogports, the *stock ticker* and the *stock keeper* form the networked storage. Tablecloth shops allow selling between players with any item as the price. Mailboxes placed at stations turn trains into a postal service. No "magic" storage mod, aerial delivery or instant mail is therefore added.

## 3.2 Selected Create addons (no stacking)

| Mod | Distinct role | Available 1.21.1 NeoForge Modrinth | Proposed status |
|---|---|---|---|
| **Create: Copycats+** | Copyable shapes that follow contraptions: sculpting airship hulls and locomotives | mirror (3.0.7) | core |
| **Create: Connected** (pruned) | Fine mechanics: clutches, shear pin, brake, gearbox, silo, boiler tank, seat dashboard | yes (1.3.2) | core |
| **Create Factory Logistics** | Fluids in packages: drinking water, milk, fuel shipped by train or airship | yes (1.6.0) | core |
| **Create Ore Excavation** | Hidden, **finite** deposits, rotation-driven drill, full KubeJS API | yes (1.6.x) | core |
| **Create: Bells & Whistles** | Platforms, railings, train doors: what makes a track a station | NeoForge on Modrinth to be confirmed | recommended |
| **Create: Diesel Generators** | Combustion tier: engines, distillation, oil pump, locomotive fuel | yes (1.3.15) | recommended |
| **Create: Power Loader** | Only way to keep chunks loaded, powered by rotational energy. Allowed on trains and contraptions; on the ground, brass version only and costly | mirror (2.0.5) | recommended, with conditions |
| **Rechiseled** + **Rechiseled: Create** (+ Fusion) | A single block-variant mod, with connected textures and a consistent industrial vocabulary | yes | recommended |

**Optional** (to be decided in play):
- **Create Crafts & Additions**: late-game electricity, with a positive signal on the Aeronautics side. This is option B of [3.5](#35-electricity-staying-mechanical-by-default).
- **Create: Numismatics**: currency stamped at the press from metal, if an economy between colonies takes shape.
- **COE Deposits**: multi-chunk deposits, true mining basins that justify a mining town.
- **Create: Interiors**: train benches and contraption seats (mass on Sable to be checked).
- **Aeronautics: Transmission & Linkage**: advanced aviation.
- **SableCat**: server tools to repair corrupted Sable sub-levels, useful on a permanent world.

**Rejected**:
- Create Big Cannons: war, with no enemy to fight.
- Create: Hypertubes: near-teleportation that bypasses the rail.
- Create: New Age and The Factory Must Grow: duplicates of Overgeared (steel), Diesel and Crafts & Additions.
- Create: Enchantment Industry: its printer duplicates enchanted books (contrary to 0004).
- Create: Trading Floor: survivors would become vending machines.
- Create: Mobile Packages: delivery by air and remote access to stock.
- Create: Central Kitchen: it removed its Kaleidoscope Cookery integration.
- Create Deco: overlaps with Copycats+ and Rechiseled: Create.
- Create: Metallurgy: duplicate of Overgeared's alloys and molds.
- FramedBlocks (duplicate of Copycats+), LittleTiles (server cost), Domum Ornamentum (follows MineColonies), building wands (shortcut).
- Sophisticated Storage, Tom's Storage, Lightman's Currency, Ender Mail, Mighty Mail: duplicates of Create 6 or instantaneous.

**Cooking on a contraption**: Kaleidoscope Contraption (dining car) does exist for 1.21.1 NeoForge on Modrinth according to the daily-life research (1.0.3), contrary to what CurseForge showed; optional, see [4](04-vie-quotidienne.md). **Also worth a look**: *Aeronautics: Winds and Weather* (1.3.2), which adds real wind for sails and mills.

**Sable compatibility**: every addon block placed on a contraption must have a defined mass. To be tested for Copycats+, Connected and Interiors; "weight" addons exist if needed.

## 3.3 Ore and prospecting: real veins

1. **Datapack**: scattered vanilla ore reduced by about 60%. The large copper and iron veins (1.18+) remain and become the target of prospecting.
2. **Early game**: **Prospecting Picks** (mirror 1.2.1, recommended). The pickaxe probes the rock around the struck block and reports veins. Its head is forged at Overgeared.
3. **Industry**: detector then drill from Create Ore Excavation, with a **forged** drill head. **Finite** deposits **tied to biomes**: zinc in wet biomes, tin in the mountains… Geography dictates where to found colonies.
4. **Depletion**: an exhausted deposit forces a move further out. New railway, new mining colony: this is the engine of distant colonies.

Optional: Ore Deposits (surface outcrops as visual clues), GeoVein (unverified, duplicate of COE Deposits).

## 3.4 Progression tree

| Age | What the player does | Mods | Main lock (KubeJS) |
|---|---|---|---|
| **0. Arrival** | Cabin, fire, stone tools | vanilla, Supplementaries | No iron tools at the crafting table |
| **1. Forge** | Stone anvil, forge, first forged iron, bronze | Overgeared, Prospecting Picks | Vanilla tools and armor removed; prospecting pickaxe with forged head; basic blueprints in the ruins |
| **2. Workshop** | Water wheel, mill, press, mixer | Create (andesite), Connected, Copycats+ | Andesite alloy by hand with a forged rivet; press and mixer with a forged "workshop hammer" of quality ≥ decent |
| **3. Brass and steam** | Steam engine, burners, conveyors | Create (brass), Rechiseled: Create | Brass first at the Overgeared foundry, then at the mixer; steam engine with **steel** plates; precision mechanism with a good-quality forged gear |
| **4. Logistics and mining** | Packages, frogports, stock, deposits | Create 6, Factory Logistics, Ore Excavation | COE drill with forged head; finite deposits by biome |
| **5. Rail** | First line, stations, rail mail | Steam 'n' Rails, Bells & Whistles, Power Loader (trains) | Steel rails and bogies; forged axle; "railway" engineer blueprint found while exploring |
| **6. Combustion** | Oil, distillation, diesel locomotives | Diesel Generators (+ Crafts & Additions in option B) | Forged steel engine block; fuel shipped in jars or tanks |
| **7. Aviation** | Hot-air balloon, airship, propeller plane | Create Aeronautics + Sable | Hot-air burner in brass; propeller blades forged at **excellent** quality; "aerostat" blueprint found in a wreck |
| **8. Distant colonies** | Outposts, survivors, trade | Easy NPC, Create Villager Labor (to be tested), OPAC, Create 6 shops | Survivor rules (0004), limited claims |

Each age opens a chapter of the **settler's notebook** ([3.7](#37-guiding-without-quests)).

**Recipes to modify** (starting list): `create:andesite_alloy`, `create:mechanical_press`, `create:mechanical_mixer`, `create:precision_mechanism`, `create:steam_engine`, `create:fluid_tank`, `create:track`, `create:railway_casing`, `create:controls`, `railways:*` bogies, COE drill and detector, `createdieselgenerators:*` engines, `aeronautics:*` propellers, bearings, burners and balloons, vanilla metal tools and armor (removal), villager trades of the same kind.

## 3.5 Electricity: staying mechanical by default

- **Option A, selected by default**: wood and water, andesite, brass, steam, **diesel**, aviation. Energy is carried by transmission shafts and fuel trains. Each colony produces its own energy: this is geography, and it strengthens the colonies.
- **Option B**: late-game electricity with Crafts & Additions. Power lines cross the landscape, a silhouette of civilization in the mist. The risk is that energy stops being a local problem. If adopted: costly wires, no Tesla coil, a less efficient alternator.
- To be decided after playing through age 6.

## 3.6 Colonies and survivors

**Concept (decision 0004)**: the vanilla villages are empty ruins ([2.3](02-exploration.md#23-structures-rarity-silence-ruins)). Rare survivors are discovered and brought back (lead, Icy's cart, train, airship, Quark "Villagers Follow Emeralds"), and they become the workforce of a colony. A distant colony is a player outpost linked by rail or by air, populated by survivors and protected by claims.

| Mod | Role | Availability | Proposed status |
|---|---|---|---|
| **Easy NPC** | Unique scripted survivors: dialogues, hand-written trades (no Mending possible), appearance | yes (7.14.0, with Easy NPC Core and Config UI) | recommended |
| **Create Villager Labor** | A rescued villager sits at a Create workstation and runs part of the chain. It replaces Villager Workers, absent in 1.21.1 (became Hired Workers, 1.21.8 only). This is not an autonomous worker who mines or cuts wood: the survivor works at the factory. Its own recipes need balancing | yes (1.5.3.1) | `test/colonies` branch |
| **Open Parties and Claims** | Groups and outpost protection, without the Xaero map. Chunk loading per claim set to 0 (Power Loader remains the only one) | yes (0.32.7) | recommended |
| Villagers Reborn | Human-looking villagers: credible survivors | yes (1.0.9b) | optional |
| ~~Villager Recruits~~ | Convoy escorts | absent (Forge 1.20.1 at most) | removed |

**Rejected**:
- MineColonies: no 1.21.1 file on Modrinth (only Forge 1.18.2), so excluded by the "Modrinth only" rule. In addition: citizens generated by the town hall (contrary to rare survivors), raids, server weight, imposed style. A trial branch is not possible until it is published on Modrinth.
- Guard Villagers: living villages.
- Taterzens: Fabric only.
- MCA Reborn: reproduction, contrary to 0004.

**KubeJS rules for survivors**:
- villager breeding blocked;
- `minecraft:mending` removed from the `tradeable` and `on_traded_equipment` tags (in 1.21, enchantments are data);
- metal tool and armor trades removed (toolsmith, armorer, weaponsmith); trades that **buy** farm or workshop products are kept: the survivor becomes an outlet;
- wandering trader disabled (`doTraderSpawning false`).

## 3.7 Guiding without quests

FTB Quests exists only on CurseForge. (Correction of 2026-10-26: Heracles and Odyssey Quests do exist for 1.21.1 NeoForge, see [livre-de-quetes](recherche/2026-10-26/livre-de-quetes.md); the quest journal finally went to Questlog, see [quetes](../../plan/quetes.md).) I propose a **settler's notebook** under **Modonomicon** (already present as a dependency), with one chapter per age: Forge, Workshop, Steam, Logistics, Rail, Combustion, Aviation, Colonies. Each chapter opens with a **custom advancement** in a datapack. The Ponder scenes of Create and Kaleidoscope Cookery serve as in-game tutorials. No mod to add. Better Advancements is optional for display.

## 3.8 The Nether: closed by default

This point had not been settled and affects civilization: Create's Blaze Burner (mixer heating, hence brass and steel in bulk) is filled by capturing a live blaze, and the Nether is a 1-to-8 travel shortcut that bypasses the rail and aviation.

**Proposed default: Nether closed** (inert portals, like the End), with replacement recipes:
- Blaze Burner: KubeJS recipe with an "ember heart" forged at Overgeared (forge + coal + magma block);
- quartz: check whether Create already produces some in the Overworld (diorite crushing), otherwise a crusher recipe;
- Overgeared "Nether" molds and alloys: reassign to Overworld materials, or cut;
- netherite: removed (steel and Overgeared quality are the high end).

This is the decision that changes the most recipes: it comes first among the decisions to validate ([8](08-mise-en-place.md#84-decisions-to-validate-together)).

## 3.9 Settings to apply

| Mod | Setting | Reason |
|---|---|---|
| Create Connected | Copycats disabled with automatic migration to Copycats+; Kinetic Battery disabled; Linked Transmitter to be discussed; `Vessel Boiler Stress Multiplier` and `Vessel Max Level` to be balanced | No duplicates, no portable energy |
| Create Ore Excavation | All deposits in finite mode; biome whitelists; forged drill head (tag `#createoreexcavation:drills`) | Deposits run out, geography matters |
| Power Loader | Loaders on trains and contraptions allowed; on the ground, brass only, number limited per player | Maintaining installations stays meaningful |
| OPAC | Chunk loading per claim = 0; number of claims limited; do not install Xaero | Power Loader remains the only chunk loader |
| Diesel Generators | Check whether oil is a deposit of the mod or a fluid injectable by COE | A single deposit logic |
| Easy NPC | Survivors written in a datapack (dialogues, trades) | Unique survivors |
| Overgeared | Vanilla metal tool recipes removed (config or KubeJS); steel pattern injected into survivor loot | Overgeared as the sole tool maker |
