# Research: "Civilization" pillar (Tilth, 1.21.1 NeoForge, Create 6.0.10)

> Scope: crafting → workshop → industry → rail → aviation → colonies.
> Baseline retained (registry of 2026-10-10, 46 mods): Overgeared 1.6.19, Create 6.0.10, Aeronautics 1.3.2 + Sable 2.0.5, Steam 'n' Rails (NeoForge port 0.3.0-beta.2), KaleidoscopeCookery-Automation, Supplementaries and Quark (essentials only), Modonomicon and Patchouli (already there as dependencies), KubeJS. To test: Copycats+, Connected, Integrated Farming, Power Loader, IDAS.
> **Limits of the research**: modrinth.com, the Modrinth API and curseforge.com are blocked for direct access. The **shared WebSearch quota (200 queries) ran out** during the research. Availability therefore comes from search excerpts (Modrinth version pages, BoxToPlay and Mado mirrors that copy Modrinth) and from GitHub. "✔ Modrinth" means an excerpt of a Modrinth version page for 1.21.1 NeoForge was seen. "~ mirror" means only a mirror shows it. "?" means unverified.

---

## 0. Key takeaways

1. **Create 6 already provides half of the "civilization" pillar**, without any addon. Packages, addresses, mailboxes, frogports, the *stock ticker* and the *stock keeper* form the networked storage. Tablecloth shops (*table cloth*) handle selling between players. Mailboxes placed at stations turn trains into a postal service. "Magic" storage mods (Tom's, Sophisticated), drone-delivery mods and ender-mail mods should therefore be rejected: they would duplicate this system and bypass the rail.
2. **Few Create addons, each with a distinct role**:
   - core: **Copycats+** (building, contraptions), **Connected** (fine mechanics, config to be pruned), **Factory Logistics** (fluids in packages), **Ore Excavation** (deposits to exhaust), **Bells & Whistles** (stations);
   - combustion tier: **Diesel Generators**;
   - electricity: **Crafts & Additions** as an option, to be decided.
   - rejected: Big Cannons, New Age, TFMG, Enchantment Industry, Hypertubes, Mobile Packages, Central Kitchen.
3. **Overgeared remains the craft of quality.** Create brings quantity. Proposed KubeJS rule: a Create machine never produces better than an "average" quality part. Critical parts (drill head, bogies, airplane engines) require a hand-forged part, of minimum quality or with a mastered blueprint.
4. **Ore**: vanilla ore made rarer and clustered, a prospecting pickaxe at the start, then deep, **finite** deposits from Create Ore Excavation (full KubeJS API). An exhausted deposit forces a move to a new frontier: this is the engine of distant colonies.
5. **Colonies**: no MineColonies in the core. Its citizens appear on their own (contrary to decision 0004), it imposes raids and weighs heavily on the server. It keeps a place as a trial branch. Proposal: ruined vanilla villages (datapack); scripted survivors with **Easy NPC**; rescued villagers turned into workers with **Villager Workers** (available for 1.21.1? unverified); outposts protected by **Open Parties and Claims**.
6. **Quests**: FTB Quests exists only on CurseForge, and no 1.21.1 version of Heracles/Odyssey was found. Proposal: **a Modonomicon notebook** (already in the pack), whose pages open through **custom advancements** (KubeJS datapack). This adds no mod.

---

## 1. Create addons (no stacking)

Aeronautics/Sable compatibility: no official list exists. The [Simulated-Project](https://github.com/Creators-of-Aeronautics/Simulated-Project/issues) repository has no open ticket naming Copycats+, Connected, S'n'R or C&A (page 1 of the search). Third-party fixes exist: [Create Aeronautics: Compatibility](https://www.curseforge.com/minecraft/mc-mods/create-aeronautics-compatability), which fixes Bits 'n' Bobs, Etched, Sleep Tight…, and SableCat ([modrinth.com/mod/sablecat](https://modrinth.com/mod/sablecat)), which repairs corrupted sub-levels and would be useful on a permanent world. "Weight" addons also exist for Sable: Copycats+ Aeronautics Weight, Sable: Weighted – TFMG. From this we infer that **every addon block placed on a contraption must have a defined mass**: a point to test.

| # | Mod (slug) | Availability 1.21.1 NeoForge / Create | Side, dependencies | Mechanic brought | Conflicts, config | Verdict |
|---|---|---|---|---|---|---|
| 1 | **Create: Copycats+** (`copycats`) | ~ mirror: 3.0.7+mc.1.21.1-neoforge (August 2026); Create 6 ([Mado](https://www.mado-hosting.com/en/minecraft-mods/copycats)) | CS, Create | Copyable shapes (planks, slabs, beams, stairs, panes…) that follow contraptions. Create 6.0.8 fixed their rendering on contraptions. Sculpt the hull of an airship or a locomotive with any block. | Duplicates Connected's copycats (see #2) and FramedBlocks (#44, rejected). Plan a weight addon for Sable. | **Core** |
| 2 | **Create: Connected** (`create-connected`, Vg5TIO6d) | ✔ Modrinth: 1.1.9+ for Create 6.0.7+, latest seen 1.3.2-mc1.21.1 ([GitHub](https://github.com/hlysine/create_connected)) | CS, Create | Fine engineering: clutches (centrifugal, freewheel, overload), shear pin (breaks on overload, very good early game), brake, brass gearbox, vertical silo, horizontal boiler tank, brass chute, fan catalysts, seat dashboard (useful on planes and trains). | **To disable** (toggle per feature and per category, see [Configs](https://github.com/hlysine/create_connected/wiki/Configs)): all **copycats** ("Automatic Copycat Migration" to Copycats+), the **Kinetic Battery** (portable energy that contradicts network maintenance), to discuss: Linked Transmitter and Wildcard. Server settings: `Vessel Boiler Stress Multiplier` and `Vessel Max Level` for steam balance. Incompatible with Extended Cogwheels. | **Core (pruned)** |
| 3 | **Create Deco** (`create-deco`) | ~ mirror: 2.1.3 1.21.1 NeoForge (April 2026) | CS, Create | Industrial vocabulary: catwalks, bars, lamps, armored doors, industrial bricks. This is content. | Overlaps with Copycats+ and Rechiseled: Create. Take one **or** the other. | Optional (prefer Rechiseled: Create, #23) |
| 4 | **Create: Bells & Whistles** (`bellsandwhistles`) | ~ v0.4.7-mc1.21.1 NeoForge according to a mirror; Modrinth shows 1.21.1, but its description cites only Fabric and Forge → **NeoForge on Modrinth to be confirmed** ([Modrinth](https://modrinth.com/mod/bellsandwhistles)) | CS, Create | Stations: platforms, curbs, railings, train doors, signage. This is what makes a track a **station**. | Compatibility with the S'n'R port to test. | Recommended (rail pillar) |
| 5 | **Create: Enchantment Industry** (`create-enchantment-industry`) | ?: 2.4.2 for Create 6.0.10 NeoForge on a mirror; several projects and forks on Modrinth | CS | Liquid experience, mechanical enchanting, book printer | The printer **duplicates enchanted books** (contrary to 0004: no Mending or workaround). Industrializes enchanting although the pack does not bet on combat. | **Rejected** |
| 6 | **Create Crafts & Additions** (`createaddition`, kU1G12Nn) | ✔ Modrinth: 1.5.8 for Create 6.0.8; 1.5.11 adds **Aeronautics docking compatibility** and fixes an Aeronautics crash ([version](https://modrinth.com/project/kU1G12Nn/version/cKTFwyEF)) | CS, Create | FE electricity: alternator, motor, **wires and pylons** (remote energy), accumulators, rolling mill (wires, rods), liquid blaze burner, biofuel. | Changes the "one wheel per machine" logic: power lines are needed. Disable the Tesla coil (item charging) and possibly the charger. | **Optional** (late tier, to be decided, §6) |
| 7 | **Create Factory Logistics** (QMGO17gm) | ✔ Modrinth: 1.6.0 1.21.1 NeoForge, client and server ([version](https://modrinth.com/project/QMGO17gm/version/4Drrlfu3)) | CS, Create 6 | **Fluids in packages**: jar, bottler, fluid gauge. Drinking water, milk, oil or fuel can be shipped by train or airship. Integrates with Thirst and diesel. | None known. | **Core** |
| 8 | **Create: Numismatics** (`numismatics`) | ✔ Modrinth: 1.0.19 NeoForge 1.21.1 for Create 6.0.7; 1.0.20 on a mirror | CS, Create | Coins with values (spur → sun), banks, ATMs, **vendors**, **depositors** (toll, train ticket), cards. | Duplicates Create 6's native shops (which accept any item as the price). Where does the currency come from? KubeJS proposal: stamp coins at the press from metal, for a metal-backed currency. | **Optional** (if an economy between colonies takes shape) |
| 9 | **Create Big Cannons** (`create-big-cannons`) | ✔ 5.11.x NeoForge 1.21.1, Create 6.0.7+. Beware: 5.11.4 **breaks existing ammunition blocks** ([CurseForge](https://www.curseforge.com/minecraft/mc-mods/create-big-cannons/files/8169551/dependencies)) | CS, + Ritchie's Projectile Lib | Artillery | War, with no enemies to fight; contrary to the art direction (Supplementaries already has its cannon disabled). | **Rejected** |
| 10 | **Create: Diesel Generators** (`create-diesel-generators`, ZM3tt6p1) | ✔ Modrinth: 1.21.1-1.3.15 NeoForge ([version](https://modrinth.com/project/ZM3tt6p1/version/UoPH8lO1)) | CS, Create | Diesel engines (small, modular, huge), distillation (large batch distillers), fuels, oil pump. **Combustion tier** between steam and aviation. Fuel for locomotives (S'n'R handles liquid fuel) and, to test, for contraption engines. | Overlaps with TFMG and New Age (rejected). Check in the jar whether oil is a deposit specific to the mod or a resource injectable by COE (#13). | **Recommended** |
| 11 | **Create: New Age** (`create-new-age`, FTeXqI9v) | ✔ Modrinth: 1.1.7a+mc1.21.1 NeoForge; 1.2.0 on a mirror | CS | Electricity, **nuclear reactor**, solar, magnets | Duplicate of C&A, nuclear is outside the art direction | **Rejected** |
| 12 | **Create: The Factory Must Grow** (`create-tfmg`) | ~ 1.2.0 (1.21–1.21.11 NeoForge) on a mirror | CS | Oil, steel, aluminum, voltage electricity, engines | Alone absorbs steel, electricity and diesel. Duplicate of Overgeared (steel), Diesel and C&A: stacking forbidden. | **Rejected** |
| 13 | **Create Ore Excavation** (`create-ore-excavation`, ResbpANg) | ✔ Modrinth: 1.21.1-1.6.4, then 1.6.6–1.6.8 NeoForge on a mirror | CS, Create | **Hidden deposits** (one per chunk), deposit detector, rotation-driven drill and fluid extractor. Deposits **finite or infinite** at will. KubeJS API: `vein().placement().biomeWhitelist().veinSize().alwaysFinite()`, `drilling().drill().fluid().stress()`, `extracting()`, tag `#createoreexcavation:drills` ([GitHub](https://github.com/tom5454/Create-Ore-Excavation)) | Switch **all deposits to finite** in the config. The detector displays "deposit / size" without coordinates: compatible with the vision. | **Core** |
| 14 | COE Deposits (addon) | ~ 0.1.6 1.21.1 NeoForge ([SkinMC](https://skinmc.net/index.php/project/create-ore-excavation-deposits)) | CS, COE | Multi-chunk deposits (2–40 chunks, Perlin shapes): true **mining basins** that justify a mining town. | Young. The Better Finder (radar) is to be rejected if it shows positions. | Optional (very interesting) |
| 15 | **Create: Power Loader** (`create-power-loader`, wPQ6GgFE) | ~ 2.0.5-mc1.21.1 NeoForge, Create 6 | CS, Create | Andesite (1×1) and brass (up to 5×5) chunk loaders, on the ground, on a train or on a contraption, adjustable server-side | Overlaps with OPAC's *forceload* function. **Proposal**: allow only trains and contraptions (convoys run when nobody is there); ground loader in brass only and costly. | Recommended, with conditions (to test) |
| 16 | Create: Central Kitchen (btq68HMO) | ✔ 2.5.0 for Create 6.0.10, but **the KC integration was removed** in favor of Kaleidoscope Compat and KC Automation | CS | Farmer's Delight automation | FD is rejected | **Rejected** |
| 17 | Kaleidoscope Contraption | ✗ only 1.20.1 Forge, Create 6.0.8+ ([CurseForge](https://www.curseforge.com/minecraft/mc-mods/kaleidoscope-contraption)) | CS | KC blocks on contraptions: **dining car**, airship kitchen | No 1.21.1 version | To watch |
| 18 | Create: Hypertubes (`hypertube`, ATDdrG1y) | ✔ 0.2.4 Modrinth; 0.6.0 on CurseForge only | CS | Fast transport tubes for players | Near-teleportation: bypasses the rail | **Rejected** |
| 19 | Create: Trading Floor (WROfLLvn) | ✔ Modrinth: 3.0.16 NeoForge 1.21.1 | CS | Counter that makes villagers trade automatically | Survivors become vending machines: contrary to 0004 | **Rejected** |
| 20 | Create: Interiors (`interiors`) | ?: 0.6.1 NeoForge 1.21.1 according to ModDex | CS | Train armchairs and benches, seats on contraptions | Weight on Sable to check | Optional (railcar fittings) |
| 21 | Create: Mobile Packages (1vXRfEHZ) | ✔ 1.21.1 NeoForge, Create 6.0.9+ | CS | Delivery robo-bees, **portable stock ticker** | Delivery by air and remote access to stock: bypasses the rail and aviation | **Rejected** (Create: Phantom too) |
| 22 | Create: Metallurgy (`create-metallurgy`) | ~ 1.0.3-1.21.1 for Create 6.0.10 NeoForge | CS | Foundry: melting, foundry mixer, casting table and basin | Partial duplicate of Overgeared's alloys and molds | Optional, to study. By default, prefer KubeJS (heated Create mixer → Overgeared alloys) |
| 23 | **Rechiseled: Create** (E6867niZ) | ✔ Modrinth: 1.1.1 NeoForge 1.21.1, Create 6+ ([version](https://modrinth.com/project/E6867niZ/version/TzPaH8MY)) | CS, Rechiseled + Fusion | Chiseled variants of Create blocks (consistent industrial) | See #43 | Recommended if Rechiseled is retained |
| 24 | Aeronautics: Transmission & Linkage | ~ compatible with Sable 2.0.x (June 2026) | CS | Transmissions and linkages between the world and the contraption | Young | Optional (advanced aviation) |
| 25 | SableCat / Aeronautics Compatibility | ✔ SableCat on Modrinth (available for 1.21.1?) | S/CS | Tools to recover corrupted or abandoned sub-levels; fixes for other mods | The compatibility mod will be archived once Sable integrates its fixes | Recommended server-side after reading the jar |

Reminder: **Create: Integrated Farming** is already "to test". Cooking on contraptions: to watch (#17).

---

## 2. Overgeared ↔ Create: metallurgy, ore, prospecting

### What Overgeared brings (mod pages and mirrors)
- Stone anvil (sneak + right-click on stone), then smith's anvil; forge with a skill mini-game.
- **Blueprints**: each carries a quality level, a number of uses, a progression, a tool type and a **maximum final quality**. Copying them onto a blank blueprint gives a lower-quality copy.
- **Steel**, a full tier between iron and diamond. The steel upgrade is done at the smithing table, with a pattern found in structures (→ to place in the ruins and survivor camps).
- Molds: clay (fragile, early) and Nether (late). They duplicate tool heads while losing quality.
- Recipes `overgeared:alloy_smelting` (4 inputs) and `nether_alloy_smelting` (9 inputs).
- Community addons: Overgeared x Epic Knights and OvergearedXSimplySwords; a recipe repository: [BlueAsclepias/overgeared-recipes](https://github.com/BlueAsclepias/overgeared-recipes). **No Overgeared ↔ Create bridge exists**: everything will go through KubeJS.

### Articulation principle: "the craftsman makes quality, the factory makes quantity"
| Step | Overgeared (hand) | Create (machine) | KubeJS rule |
|---|---|---|---|
| Hot ingots | Forge and hammer | Blaze burner under a basin or "heating" fan (test) | Industrial heating is allowed; quality shaping stays manual |
| Plates and heads | Anvil, quality depending on the blueprint | Mechanical press → **"poor" to "decent" quality at most** | Press output with a fixed quality component (exact component key to read in the jar) |
| Alloys | Alloy foundry (4 inputs): bronze, brass, steel | Heated, then superheated mixer | **Brass**: first at the Overgeared foundry, then at the mixer (rate ×4). **Steel in bulk**: superheated mixer (burner + blaze/diesel fuel) |
| Critical parts | Hand-forged, quality ≥ "good" | — | COE drill head, train bogies and axles, airplane engine casing: recipes requiring the **forged item** (tag `tilth:forged_quality_good`) |
| Tools and weapons | Only source of quality tools | No metal tools made by Create (deploying, sequenced assembly) | Remove the vanilla tool and armor recipes in iron, gold, diamond and steel (according to the Overgeared config) and the corresponding villager trades |

### Ore and prospecting: "real veins"
| # | Mod (slug) | Availability | Mechanic | Verdict |
|---|---|---|---|---|
| 26 | **Prospecting Picks** | ~ 1.2.1 MC 1.21.1 NeoForge (Jan. 2026) ([Mado](https://www.mado-hosting.com/minecraft-mods/prospecting-picks)) | Prospecting pickaxe: probes a configurable radius around the struck stone block; detects veins by their raw ore blocks; ores defined in config (tag `c:ores/*`). Designed to go with **larger and rarer** clusters. | **Recommended** (early game) |
| 27 | GeoVein | ? 1.21.1 NeoForge (ModDex); Modrinth unverified | Huge graded deposits: the closer to the core, the better the ore | Optional. Duplicates COE Deposits: take one or the other |
| 28 | Advanced Ore Deposits | ~ 1.0.1 1.21.1 NeoForge | Deposit blocks that regenerate until their reserve is exhausted | Optional (worse than finite COE) |
| 29 | Ore Deposits (surface) | ~ 1.1 (1.21.1, multi-loader) | Surface outcrops that reveal the ore | Optional: visual clue for exploration ("I see rust on the cliff") |
| 30 | Create: Rock & Stone, Create: Ore Deposits | CurseForge / WIP "not ready for survival" | — | **Rejected** |

**Proposed scheme**:
1. Datapack: scattered vanilla ores reduced (≈ −60%). The large vanilla copper and iron veins (1.18+) are kept and become the target of prospecting. In No Man's Land, outcrops (Ore Deposits, optional) serve as clues.
2. Start: prospecting pickaxe, manual mining, Overgeared forge.
3. Industry: COE detector, then drill with a **forged** head (Overgeared gate). **Finite** deposits (`.alwaysFinite()`, size ×5–8 of the base) and **biome whitelists**: zinc in wet biomes, tin in the mountains… Geography dictates where to found colonies.
4. Depletion → new distant deposit → railway → mining colony.

```js
// server_scripts/coe_veins.js (1.21 syntax according to the COE README)
ServerEvents.recipes(e => {
  e.recipes.createoreexcavation.vein('{"text":"Gisement de zinc"}', 'create:raw_zinc')
    .placement(64, 24, 91827).biomeWhitelist('#c:is_wet/overworld') // tag to check
    .alwaysFinite().veinSize(4, 8).id('tilth:vein_zinc')
  e.recipes.createoreexcavation.drilling(
      [coeutil.processingOutput('create:raw_zinc', 1), coeutil.processingOutput('minecraft:gravel', 0.3)],
      'tilth:vein_zinc', 400)
    .drill('tilth:forged_drill_head').stress(512)
})
```

---

## 3. Colonies and NPCs (with decision 0004 "survivors")

### Concept
The vanilla villages become **empty ruins**. **Rare, unique survivors** are discovered (hermit, engineer, refugees). They are **brought back** on horseback, by cart (Icy's), by train or by airship, guided by the Quark module "Villagers Follow Emeralds". They become the **workforce** of a colony. The "distant colonies" are player outposts linked by rail or by air (mine on a COE deposit, trading post, airfield), populated by survivors and protected by claims.

| # | Mod (slug) | Availability 1.21.1 NeoForge | Mechanic | Conflicts | Verdict |
|---|---|---|---|---|---|
| 31 | **Easy NPC** (`easy-npc`) | ~ 7.9.0 1.21.1 NeoForge (Mado); file `easy_npc_bundle-neoforge-1.21.1-6.25.0.jar` seen on CurseForge; Modrinth probable, unverified | Scripted NPCs: dialogues, **hand-defined trades**, appearance, actions and conditions, without a restart. Perfect for **unique survivors**: the hermit who sells an Overgeared blueprint in exchange for food, the engineer who tells of the collapse. | Does not go through villager trades: no Mending possible | **Recommended** |
| 32 | **Villager Workers** (talhanation) | ?: [talhanation/workers](https://github.com/talhanation) repository active, 1.21.1 version unconfirmed | A **rescued villager becomes a worker** (miner, lumberjack, farmer, fisherman, shepherd, breeders, merchant) with work zone, chest and food needs. This is exactly the use case of 0004. | Automatic farmer or breeder: to be supervised (demanding husbandry wanted in 0002) | **Recommended if 1.21.1 NeoForge exists** (to verify first) |
| 33 | Villager Recruits (`villager-recruits`) | ?: the Modrinth page exists ([modrinth.com/mod/villager-recruits](https://modrinth.com/mod/villager-recruits)), 1.21.1 version unconfirmed | Recruits with pay and food, escorts, patrols, factions | Army-oriented; recruits bought with emeralds (disable their spontaneous spawning) | Optional: convoy escorts only |
| 34 | MineColonies (`minecolonies`) | ✔ Modrinth project; 1.21.1 in **snapshot** (`minecolonies-1.1.1349-1.21.1-snapshot`) according to ModDex | A whole town: builders, university, warriors; plus Structurize, BlockUI, Domum Ornamentum, MultiPiston | Citizens generated by the town hall (contrary to rare survivors); barbarian and pirate **raids** (disableable, key to read in the jar); server weight; imposed building style | **Rejected from the core**. `test/minecolonies` branch if the desire returns: initial citizens 0, births disabled, recruitment only through **tavern visitors** (which resembles survivors) |
| 35 | Guard Villagers (`guard-villagers`) | ~ 2.4.10 1.21.1 NeoForge (the Modrinth project Ap53t4bc is a **Fabric** port, a trap) | Guards in groups of six in villages | More living villages; duplicate of Recruits | **Rejected** |
| 36 | Taterzens | ✗ Fabric only (last Forge in 1.19) | — | — | **Rejected** |
| 37 | Villagers Reborn | ~ several 1.21.1 NeoForge versions (mirror) | Replaces villager models with **humans** (vanilla functions kept) | Visual consistency with Easy NPC to check | Optional, interesting for the art direction (credible survivors) |
| 38 | MCA Reborn | ? (search quota exhausted) | Human villagers, marriage, **reproduction** | Contrary to 0004 | **Rejected** |
| 39 | **Open Parties and Claims** (`open-parties-and-claims`) | ✔ Modrinth: `neoforge-1.21.1-0.27.5` ([version](https://modrinth.com/mod/open-parties-and-claims/version/neoforge-1.21.1-0.27.5)) | Groups and claims; **Xaero mods are not required** (optional integration); management through commands and interface | Do not install Xaero. Set the claim limit and *forceload* (consistency with Power Loader) | **Recommended** (colony protection) |

### Implementation (sub-project 3)
- **Ruined villages**: replace in the datapack the `minecraft:village/*/villagers` *template pools* with an empty pool, and/or force the village "zombie" pools (dilapidated buildings). Zombies do not spawn since hostiles are blocked. Another option: empty `data/minecraft/worldgen/structure_set/villages.json` and generate its own ruins. Sparse Structures sets the rarity.
- **No reproduction**:
```js
// server_scripts/survivants.js
NativeEvents.onEvent('net.neoforged.neoforge.event.entity.living.BabyEntitySpawnEvent', e => {
  if (e.parentA?.type == 'minecraft:villager') e.setCanceled(true)
})
```
- **No Mending in trades**: in 1.21 enchantments are data. Redefine the tags `data/minecraft/tags/enchantment/tradeable.json` and `on_traded_equipment.json` with `"replace": true`, without `minecraft:mending`.
- **No bypass of Overgeared**: remove the trades of the toolsmith, armorer and weaponsmith that sell metal tools and armor, via `VillagerTradesEvent` (NeoForge, from `NativeEvents`) or a KubeJS trade addon (MoreJS 1.21.1? unverified). Trades that buy farm or workshop products are kept: the survivor becomes an outlet for production.
- **Survivor camps**: datapack structures (2-3 Easy NPC NPCs + one vanilla villager). They can hide Overgeared's **steel smith pattern**.

---

## 4. Building and decoration (what makes it a craft)
Already retained: Quark (essentials), Supplementaries (essentials), Copycats+ (§1).

| # | Mod | Availability | Contribution | Verdict |
|---|---|---|---|---|
| 40 | Amendments | ~ 2.1.7 1.21.1 NeoForge | Vanilla tweaks (wall lanterns, mixing cauldrons, hanging signs) | **Optional, low**: judged "stacking" when Integrated Mowzie's was set aside; do not relaunch |
| 41 | Handcrafted | ~ 4.0.3 1.21.1 NeoForge, + Resourceful Lib (already present) | 250 furniture items, content | Optional. A patch "Furniture with Create and Sable compat" exists (Mado): furniture on contraptions is a problem without it |
| 42 | Macaw's (Roofs, Bridges, Doors, Windows…) | **Negative signal**: Windows shown for 1.21.1 **Forge** and 1.21.8 NeoForge only; only third-party addons (Aurora, Betters) exist for 1.21.1 NeoForge | Roofs, bridges | To check mod by mod. Probably **unavailable** for 1.21.1 NeoForge on Modrinth |
| 43 | **Rechiseled** (+ Fusion for connected textures) | ✔ Rechiseled: Create 1.1.1 NeoForge 1.21.1; LittleBuilders 2 (1.21.1 modpack) ships Rechiseled and Fusion | Chisel: block variants, connected textures; consistent Create addon | **Recommended** (a single variant mod; preferred over Chipped, which has workbenches and thousands of blocks) |
| 44 | FramedBlocks (`framedblocks`) | ✔ 10.5.3 on Modrinth (10.6.2 on a mirror) 1.21.1 NeoForge | Blocks to dress up | Duplicate of Copycats+, which follows contraptions | **Rejected** |
| 45 | LittleTiles | ? (present in LittleBuilders 2 for 1.21.1); CreativeCore already there | Micro-blocks | Server cost, behavior on contraptions unknown | **Rejected** (or optional on a branch) |
| 46 | **Immersive Paintings** (`immersive-paintings`) | ✔ Modrinth: 0.7.8 NeoForge 1.21.1, CS ([version](https://modrinth.com/project/6txNkua3/version/DeOfrXC3)) | Paintings from uploaded images; option to disable the paintings supplied with the mod | Recommended with Camerapture |
| 47 | **Camerapture** (`camerapture`) | ✔ Modrinth: 1.10.6+mc1.21.1-neoforge ([version](https://modrinth.com/mod/camerapture/version/1.10.6+mc1.21.1-neoforge)) | Camera: **photos become items** to frame. Expedition snapshots are brought back and the world's memory is built (complements Antique Atlas) | **Recommended** (little known, very much in the spirit) |
| 48 | Domum Ornamentum | Only with MineColonies | Texture combination at the architect's table | Rejected (follows MineColonies) |
| 49 | Building wands (Construction Wand, Building Gadgets) | — | Shortcut | **Rejected**. Create's schematic cannon, which consumes materials, is the "industrial construction"; Quark's trowel is enough |

---

## 5. Storage, logistics, economy, mail, protection

| # | Mod | Availability | Analysis | Verdict |
|---|---|---|---|---|
| — | **Create 6, native** | included | Vaults, silo (Connected), packages + packager + frogport + transport chain, **stock ticker + stock keeper**, **factory gauges**, **tablecloth shops** (price in any item), **mailboxes at stations** (trains carry packages by address) | **Core**. This is the storage, trade **and mail** system |
| 50 | Sophisticated Storage | ~ Core 1.21.1 NeoForge, Modrinth page unconfirmed | Magnetic upgrades, void, compacting, plus a Create Integration addon | **Rejected** ("magic" shortcuts, duplicate of vaults) |
| 51 | Tom's Simple Storage | ? | Networked terminal (magic) | **Rejected** (duplicate of the stock ticker) |
| 52 | Lightman's Currency | ~ 2.3.0.4g 1.21.1 NeoForge | Heavy economy with terminals and network | **Rejected** (Numismatics, more "Create", as an option) |
| 53 | **Envelope** (Pigeon Mail, `envelope`) | ✔ Modrinth page 1.21.1; 0.7.2–0.7.5 NeoForge 1.21.1 on mirrors; **not yet finished** according to the wiki | Letters, parcels, **carrier pigeons**, mailboxes. Early-game mail, before the rail: writing to a friend at the other end of the world | Optional (to test). To configure: realistic delay and range; parcel weight limited, so that train package freight remains the freight route |
| 54 | Ender Mail Reborn | ✔ 1.21.1 NeoForge (port) | Ender courier, address by coordinates | **Rejected** (Ender and coordinates) |
| 55 | Mighty Mail (MrCrayfish) | ? loader | Mailbox to any mailbox | **Rejected** (instantaneous) |
| 56 | Post System | 1.21.1 NeoForge | Requires Lightman's and RegionGuard | **Rejected** |
| 39 | Open Parties and Claims | ✔ | see §3 | **Recommended** |

---

## 6. Technical progression and guidance

### Is electricity needed?
- **Option A, all mechanical** (recommended by default): wood and water → andesite → brass → steam → **diesel** → aviation. Energy is carried by shafts, fuel trains (Factory Logistics) and local boilers. Each colony produces its own energy: this is geography, and it strengthens the colonies.
- **Option B, late-game electricity** with **Crafts & Additions** (positive signal on the Aeronautics side). **Power lines** cross the landscape: a silhouette of civilization in the mist. The risk is that energy is no longer a local problem. If kept: costly wires (rolling mill + copper + rubber or resin), no Tesla coil, reduced alternator efficiency.
- Decision to be made in play. New Age and TFMG stay rejected in both cases.

### Quests
| Mod | Availability | Verdict |
|---|---|---|
| FTB Quests | **CurseForge only** (FTB does not publish on Modrinth); 2101.1.28 NeoForge on CF | **Rejected** (Modrinth constraint) |
| Heracles / Odyssey Quests | 1.20.1; unofficial NeoForge port for 26.2+ only | **Rejected** (no 1.21.1) |
| **Modonomicon** (already present) + **custom advancements** (datapack in `kubejs/data/tilth/advancement`) | ✔ | **Core**: an "engineer's notebook". Each chapter (Forge, Workshop, Steam, Logistics, Rail, Combustion, Aviation, Colonies) opens on an advancement; with multiblock diagrams. No extra mod. Ponder (Create, KC) serves as an in-game tutorial |

---

## 7. Proposed progression tree

| Age | Gameplay goal | Mods | Key materials | KubeJS locks (to write) |
|---|---|---|---|---|
| **0. Arrival** | Cabin, fire, stone tools | vanilla, Supplementaries | stone, flint, surface copper | No iron tools at the crafting table. Advancement "First shelter" → notebook chapter 1 |
| **1. Forge** | Stone anvil → forge → first forged iron, bronze | **Overgeared**, Prospecting Picks | raw iron, copper, tin(?) and coal; bronze at the foundry | Vanilla tool and armor recipes removed; basic blueprints found in the ruins; the prospecting pickaxe requires a forged head |
| **2. Workshop** | Water wheel, mill, press, mixer | **Create** (andesite), **Connected** (shear pin, clutches), Copycats+ | andesite alloy, iron plates | `create:andesite_alloy` by hand with a **forged nail or rivet** (Overgeared); press and mixer require a forged "workshop hammer" of quality ≥ decent |
| **3. Brass and steam** | Steam engine, burners, first conveyors | Create (brass), Rechiseled: Create | **brass** (Overgeared → mixer), Overgeared **steel** | Steam engine and boiler: **steel** plates instead of gold; precision mechanism: forged gear of "good" quality. Advancement "Pressure" |
| **4. Logistics and mining** | Packages, frogports, stock, deposits | Create 6, **Factory Logistics**, **Ore Excavation** (+ Deposits) | forged drill heads, brass, steel | COE drill: `.drill('tilth:forged_drill_head')`; **finite** deposits, biome whitelist; possible Integrated Farming here |
| **5. Rail** | First line, stations, rail mail | **Steam 'n' Rails**, **Bells & Whistles**, Interiors (opt.), Power Loader (trains only) | steel (rails, bogies), brass | `create:track` and S'n'R bogies with **steel**; train casing with a forged axle; station mailbox = mail between colonies |
| **6. Combustion** | Oil, distillation, diesel locomotives | **Diesel Generators** (+ C&A in option B) | crude (COE fluid deposit or mod oil), steel, brass | Diesel engines with a forged steel engine block; fuel shipped in jars or tanks (Factory Logistics) |
| **7. Aviation** | Hot-air balloon, then airship, then propeller plane | **Create Aeronautics + Sable** (+ Transmission & Linkage optional) | brass, steel, canvas (Supplementaries flax), fuel | Hot-air burner in brass (age 3+); propellers and propulsion bearings with **forged "excellent" blades** (real mastery of the forge); floating stones (levitite?) found only while exploring |
| **8. Distant colonies** | Outposts, survivors, trade | **Easy NPC**, Villager Workers (?), **OPAC**, Create 6 shops (+ Numismatics optional), Envelope (opt.), Camerapture and Immersive Paintings | food (KC) as wages, survivors | Reproduction blocked, enchantment tags without Mending, metal trades removed, ruined villages; claims limited per player |

### Recipes to modify (starting list)
`create:andesite_alloy`, `create:mechanical_press`, `create:mechanical_mixer`, `create:precision_mechanism` (sequenced assembly), `create:steam_engine`, `create:fluid_tank`, `create:track`, `create:railway_casing`, `create:controls`, the `railways:*` bogies, the COE drill and detector, the `createdieselgenerators:*` engines, the propellers, bearings, burners and balloons of `aeronautics:*`, the `createaddition:*` wires (if option B), vanilla metal tools and armor (removal), plus villager trades of the same kind.

---

## 8. Uncertainties to check in the .jar files
1. **Overgeared**: identifier of the quality component (to impose it on press output), recipe types exposed to KubeJS (`overgeared:forging`? `alloy_smelting`), config that disables vanilla tools, presence of tin and bronze or not, loot of the steel pattern.
2. **Create Aeronautics**: name and origin of the "magic floating rock" (levitite?), identifiers of propellers and burners, fuel accepted by the engines.
3. **Diesel Generators**: oil = own deposit (oil chunks) or fluid addable by COE (`extracting`)? Fluid identifier.
4. **COE**: config key for finite mode by default; valid biome tags for the whitelist with NML and Biolith.
5. **Create Connected**: exact keys of the *Feature Toggle* (copycats, kinetic_battery, linked_transmitter).
6. **Power Loader**: server keys per mode (ground, train, contraption).
7. **S'n'R port**: compatibility of Bells & Whistles and Interiors with the NeoForge port 0.3.0-beta.
8. **Sable**: mass of Copycats+, Connected and Interiors blocks (is a weight addon needed?).
9. **Villager Workers and Recruits**: existence of a 1.21.1 NeoForge version on Modrinth (**priority** for the colonies part).
10. **Easy NPC**: presence on Modrinth (NeoForge 1.21.1 bundle), syntax of trades and dialogues to supply them in a datapack.
11. **Bells & Whistles**: is the NeoForge file really on Modrinth and not only on CurseForge?
12. **Macaw's**: is there a single 1.21.1 NeoForge build on Modrinth?
13. **Villages**: names of the template pools to empty (check with NML, which might modify villages); interaction with Sparse Structures.
14. **OPAC**: `forceload` keys and claim limits; absence of Xaero dependency.
15. **Envelope**: are pigeon delay, range and capacity configurable?
16. **Points unverified for lack of search quota**: GeoVein, MCA Reborn, Tom's Storage, Chipped 1.21.1, Create Interiors on Modrinth, MoreJS 1.21.1.

## Main sources
[Create Connected (GitHub)](https://github.com/hlysine/create_connected) · [Configs](https://github.com/hlysine/create_connected/wiki/Configs) · [Create Ore Excavation (GitHub)](https://github.com/tom5454/Create-Ore-Excavation) · [Port S'n'R 1.21.1](https://github.com/Porters-of-Railways/Railway-1.21.1) · [Simulated-Project issues](https://github.com/Creators-of-Aeronautics/Simulated-Project/issues) · [C&A 1.5.x](https://modrinth.com/project/kU1G12Nn/version/cKTFwyEF) · [Factory Logistics 1.6.0](https://modrinth.com/project/QMGO17gm/version/4Drrlfu3) · [Diesel Generators 1.3.15](https://modrinth.com/project/ZM3tt6p1/version/UoPH8lO1) · [New Age](https://modrinth.com/project/FTeXqI9v/version/jKQGO6gz) · [COE](https://modrinth.com/mod/ResbpANg) · [Power Loader](https://modrinth.com/project/wPQ6GgFE) · [Central Kitchen 2.5.0](https://modrinth.com/project/btq68HMO/version/kmHFmmpV) · [Kaleidoscope Contraption](https://www.curseforge.com/minecraft/mc-mods/kaleidoscope-contraption) · [Hypertubes](https://modrinth.com/project/ATDdrG1y/version/nDtH1hZL) · [Trading Floor](https://modrinth.com/project/WROfLLvn/version/akJ4A9Zh) · [Mobile Packages](https://modrinth.com/mod/create-mobile-packages) · [Big Cannons](https://modrinth.com/mod/GWp4jCJj) · [Enchantment Industry](https://modrinth.com/project/create-enchantment-industry) · [Bells & Whistles](https://modrinth.com/mod/bellsandwhistles) · [Rechiseled: Create](https://modrinth.com/project/E6867niZ/version/TzPaH8MY) · [Immersive Paintings 0.7.8](https://modrinth.com/project/6txNkua3/version/DeOfrXC3) · [Camerapture](https://modrinth.com/mod/camerapture/version/1.10.6+mc1.21.1-neoforge) · [OPAC 0.27.5](https://modrinth.com/mod/open-parties-and-claims/version/neoforge-1.21.1-0.27.5) · [Envelope](https://modrinth.com/mod/envelope) · [MineColonies](https://modrinth.com/mod/minecolonies) · [Villager Recruits](https://modrinth.com/mod/villager-recruits) · [talhanation (GitHub)](https://github.com/talhanation) · [Guard Villagers (Fabric port)](https://modrinth.com/project/Ap53t4bc) · [Overgeared](https://modrinth.com/project/overgeared) · [Prospecting Picks](https://www.mado-hosting.com/minecraft-mods/prospecting-picks) · [COE Deposits](https://skinmc.net/index.php/project/create-ore-excavation-deposits) · [Copycats+](https://www.mado-hosting.com/en/minecraft-mods/copycats) · [FTB Quests NeoForge (CF)](https://www.curseforge.com/minecraft/mc-mods/ftb-quests-forge/files/8520042) · [Heracles port 26.2](https://modrinth.com/project/fx73ivjN) · [Aeronautics Compatibility](https://www.curseforge.com/minecraft/mc-mods/create-aeronautics-compatability) · [SableCat](https://modrinth.com/mod/sablecat)
