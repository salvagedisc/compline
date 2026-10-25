# Research: Exploration pillar (Fallow, 1.21.1 NeoForge)

> Researcher's report, 2026-10-10. Takes into account the updated registry (46 mods, decisions 0002 to 0007). **Retained, therefore not proposed again**: Sparse Structures, Icy's Better Horses, Supplementaries (rope/pulley, hand-placed direction signs, ancient ink, map markers; structure location disabled), Quark (essentials). **Rejected, therefore not proposed again**: YUNG's Better *, Structory and Structory: Towers, Explorify, Moog's Voyager, Trek, Adorabuild, When Dungeons Arise, Dungeons and Taverns, Explorer's Compass, Nature's Compass, Waystones, Terralith alone.
>
> **Limits of the research**: modrinth.com, the Modrinth API and CDN were unreachable during the research. Availability comes from WebSearch excerpts (Modrinth version pages and mirrors such as BoxToPlay, SkinMC, mado-hosting, which copy Modrinth files) and from direct reading of GitHub sources (raw.githubusercontent). **The web search quota (200) ran out along the way.** The following topics therefore could not be verified: tent mods, climbing outside ParCool, boats, dynamic events, Distant Horizons server-side. They are flagged as such.
>
> Availability legend: **yes** (1.21.1 NeoForge file seen on a Modrinth page or a Modrinth mirror) · **probable** (strong hints, Modrinth file not seen) · **unverified** · **no** (CurseForge only or no build).

---

## 1. Summary

| # | Mod | Modrinth availability 1.21.1 NF | Side | Verdict |
|---|---|---|---|---|
| 1 | Lootr | yes | CS | **core** |
| 2 | Paraglider | probable (build 21.1.5 in the source) | CS | **core** |
| 3 | Create Aeronautics Structures | yes (1.21.1–1.21.11) | CS | **recommended** (priority 0004) |
| 4 | Create: New Beginnings | yes (0.1.1, 1.21.1–1.21.4) | CS | **recommended** (priority 0004) |
| 5 | Create: Let The Adventure Begin | yes (datapack 4.0.3) | S | **recommended, to test** (priority 0004) |
| 6 | MCG's RPG Campsites | yes (1.0.0) | CS | **recommended** (survivors) |
| 7 | Cinch's Abandoned Houses | probable | CS | **recommended** |
| 8 | Exposure | yes (1.9.18) | CS | **recommended** |
| 9 | Traveler's Backpack | yes (10.1.38) | CS | **recommended** |
| 10 | Comforts | yes (9.0.3+1.21.1) | CS | **recommended** |
| 11 | Traveler's Titles | probable (v5.1.3 + YUNG's API) | C | **recommended** |
| 12 | Dead Reckoning | unverified (Fabric, via Connector) | C | **recommended if available** |
| 13 | AA4 Atlas (Antique Atlas addon) | unverified | CS | **recommended if available** |
| 14 | ParCool! | yes (4.0.0.5) | CS | **recommended, restricted** |
| 15 | Better Archeology | yes (1.3.4, maybe 1.3.8) | CS | **recommended, to test** |
| 16 | Streams Reflowing (with NML) | yes (2.13.1, 1.21.1–1.21.5) | S | **to test first** |
| 17 | Create: Structures Arise | yes (176.49.48) | CS | optional |
| 18 | Create: Rustic Structures | yes (1.0.1) | CS | optional |
| 19 | IDAS (+ Integrated API) | yes | CS | optional (already "to test") |
| 20 | Integrated Villages | unverified on the NeoForge side (NeoForge 1.3.2 file on CurseForge) | CS | optional |
| 21 | ChoiceTheorem's Overhauled Village (CTOV) | yes (v3.6.3) | CS | optional |
| 22 | Towers of the Wild Modded | yes (1.0.9, 1.21–1.21.11) | S | optional (to show, taste risk) |
| 23 | Antique Trains (AA4 addon) | unverified | C | optional |
| 24 | The Athenaeum | unverified (CurseForge NF 1.21.1) | CS | optional |
| 25 | Explorer's Journal (KayDev) | unverified | CS | optional |
| 26 | NeoCompass / Compass Plus | yes (NF 1.21.1) | C | optional (fallback for Dead Reckoning) |
| 27 | Distant Horizons | (already "to test") | CS | recommended on the exploration side, cost to measure |
| 28 | LootJS | unverified | S | optional (tool) |
| 29 | Hooked / Yori3o's Grappling Hooks | yes | CS | rejected for now |
| 30 | Sophisticated Backpacks | yes (3.26.6) | CS | rejected (duplicate of TB) |
| 31 | Wandering Villagers | yes (1.0.1) | S | rejected (breaks survivor rarity) |
| 32 | 80% Abandoned Villages | no (1.21 / 1.20.6, no 1.21.1 seen) | S | rejected (we write it ourselves) |
| 33 | Create: Dynamic Village | no (CurseForge only) | CS | rejected (Modrinth) |
| 34 | SWEM / SWEM Lite | unverified | CS | rejected (duplicate of Icy's) |
| 35 | Hoofprint | unverified | C | rejected (duplicate of AA4 frontend) |

---

## 2. Detailed sheets

### A. Structures integrating Create (priority research of decision 0004)

**Finding**: no mod found **replaces** all vanilla structures with Create versions (as in September). The closest is an assembly: *Create: New Beginnings* (non-destructive additions to villages) + *Create Aeronautics Structures* (engineer ruins, wrecks) + *Let The Adventure Begin* (small standalone structures), with our own replacements in the village pools, injected by **Lithostitched** (already present as a dependency of Hybrid Aquatic).

#### 1. Create Aeronautics Structures
- Modrinth slug: project `PZIWeQhv` (displayed compatibility 1.21.1–1.21.11). Evidence: https://modrinth.com/project/PZIWeQhv · https://www.curseforge.com/minecraft/mc-mods/create-aeronautics-structures
- Availability: **yes** (Modrinth, NeoForge 1.21.1). Beta status (1.1.0-Beta-3.1 fixes a server crash when reading the config), active development.
- Side: CS. Dependencies: Create 6.0.10, Create Aeronautics ≥ 1.1.3, Sable ≥ 1.1.3, GeckoLib 4.8.4 (all already in the pack).
- Mechanic: ruins of **engineer houses**, abandoned boats, floating airships, broken balloons (non-functional ruin) and airship wrecks "to salvage". The engineer's house shelters a **peaceful engineer merchant** (`villager_create_engineer`) who sells shafts, cogwheels, andesite alloy, presses. The author says: "atmosphere only, no gameplay advantage".
- Integration: fits the "survivors" concept perfectly. The engineer is a ready-made survivor, to bring back to the colony. The airship wreck is a silhouette in the mist, the dream reward of the Exploration pillar.
- Conflicts and config: check that the engineer does not break Overgeared or Create progression. Filter his trades in KubeJS (no press or advanced parts sold early). The "robber balloons" (bandit balloons) might be hostile: to check and disable if they are mobs.
- KubeJS/datapack: wreck loot replaced by **blueprints** ("knowledge" items that unlock Aeronautics recipes, see §3). Rarity via Sparse Structures (×5–6).
- **Verdict: recommended**, to test first.

#### 2. Create: New Beginnings
- Slug: project `IAnP4np7`. Evidence: https://modrinth.com/mod/IAnP4np7 · https://www.curseforge.com/minecraft/mc-mods/create-new-beginnings
- Availability: **yes** (0.1.1, NeoForge 1.21.1–1.21.4, March 2025, compatible with Create 6). Early version, rarely updated.
- Side: CS. Dependency: Create.
- Mechanic: adds Create structures to vanilla villages in a **non-destructive** way ("remnants of the past": remains, own loot tables, structures per village type) to teach Create mechanics.
- Integration: it is the closest to the request for "Create version of vanilla villages". Since villages will become ruins (0004), we need to check whether the pieces are also injected into the "zombie" (abandoned) pools. Otherwise, copy them into those pools with a Lithostitched `add_template_pool_elements` modifier.
- Conflicts: low risk (non-destructive addition, compatible with CTOV and others). Unknown: does it contain assembled contraptions (Create handles generated contraptions poorly)?
- **Verdict: recommended** (to test with forced abandoned villages).

#### 3. Create: Let The Adventure Begin
- Slug: `create-let-the-adventure-begin` (Modrinth datapack, about 1M downloads). Evidence: BoxToPlay mirror of the Modrinth datapacks "create-let-the-adventure-begin".
- Availability: **yes** (4.0.3, 1.20.1–1.21.1, all loaders since it is a datapack).
- Side: server (datapack; to place via packwiz in `kubejs/data` or via a global datapack loading mod).
- Mechanic: structures "inspired by Create, balanced, close to vanilla, that reward exploration".
- To do: open the archive and list `data/*/worldgen/structure_set` and `loot_table` (list not found). Remove the structures that are too "dungeon".
- **Verdict: recommended, to test.** Taste and volume risk, as with Explorify: keep only the most sober half.

#### 4. Create: Structures Arise
- Slug: `create-structures-arise`. Evidence: https://modrinth.com/project/9enMEvoc/version/HSECDqTu · BoxToPlay mirror "create-structures-arise".
- Availability: **yes** (176.49.48 NeoForge 1.21.1). A changelog (174.47.46) requires **AnvilCore 10.1**: dependency to confirm in the .jar.
- Mechanic: 27 to 28 Create structures (mills, workshops, etc.).
- Risk: volume (28 structures) contrary to the principle "a structure is a discovery". Uneven quality according to community feedback (unverified).
- **Verdict: optional.** To consider only if New Beginnings and Let The Adventure Begin are not enough, with per-structure disabling via Sparse Structures or datapack.

#### 5. Create: Rustic Structures
- Slug: `create-rustic-structures`. Evidence: BoxToPlay mirror "create-rustic-structures".
- Availability: **yes** (1.0.0 and 1.0.1 NeoForge 1.21.1).
- Mechanic: functional and decorative structures in Create blocks, "rustic" tone (mills, mechanized farms).
- **Verdict: optional.** The rustic tone fits the art direction, but it partially duplicates New Beginnings: choose one after a visual comparison.

#### 6. IDAS and Integrated Villages
- IDAS: https://modrinth.com/mod/idas, NeoForge 1.21.1 (idas-1.13.5+1.21.1-neoforge, April 2026). Requires Integrated API, Create, Quark, Supplementaries (all present except Integrated API). Configuration **by datapack**: spacing and separation in the `structure_set`s, and an official configuration datapack exists.
- Integrated Villages: https://modrinth.com/project/integrated-villages. The file `integrated_villages-1.3.2+1.21.1-neoforge.jar` exists on CurseForge, but the Modrinth page only announces "Forge & Fabric": **NeoForge on Modrinth unverified**. Requires Integrated API, Create, Supplementaries.
- Problem: IDAS is dungeon- and loot-oriented (RPG), and its structures are large, hence noisy. Integrated Villages adds **populated** villages, the opposite of the "survivors" concept.
- **Verdict: optional.** IDAS stays "to test later". For Integrated Villages, keep only if their ruined variants can be forced.

#### 7. Create: Dynamic Village
- 4 Create professions (mechanical engineer, hydraulic engineer, train mechanic, miner), 20 buildings. **CurseForge only** (v0.7-1.21.1): https://curseforge.com/minecraft/mc-mods/dynamic-village
- **Verdict: rejected** (absent from Modrinth). The idea of professions can be taken up in KubeJS for the survivors (custom professions).

### B. Ruins, abandoned villages and survivors

#### 8. MCG's RPG Campsites
- Slug: `mcgs-rpg-campsites`. Evidence: BoxToPlay mirror of Modrinth mods (1.0.0, NeoForge 1.21.1) · https://www.curseforge.com/minecraft/mc-mods/mcgs-rpg-campsites
- Side: CS. Vanilla blocks only.
- Mechanic: **8 campsites, 7 of them with a villager** (butcher, farmer, cartographer, cleric, toolsmith, armorer, librarian) and 1 abandoned. Each camp has a village loot chest; one contains a ruined portal. Plains, taiga, savanna. The author recommends `/locate`, which proves their rarity.
- Integration: this is **exactly the "survivors" concept** of decision 0004. An isolated refugee with their campfire, to bring back to the colony (Quark "Villagers Follow Emeralds" is already enabled).
- Config and datapack: remove the ruined portal and the village loot chest, or replace it with a journal (lore). Rarity ×4–6. Inject a **hay bale under the campfire** (signal smoke visible from very far away: the survivor's natural beacon, see §4).
- Conflict: the cartographer sells vanilla exploration maps (structure maps). Filter them in KubeJS (consistent with 0006).
- **Verdict: recommended.**

#### 9. Cinch's Abandoned Houses
- Slug: `cinchs-abandoned-houses`. Evidence: https://www.mado-hosting.com/minecraft-mods/cinchs-abandoned-houses (NeoForge 1.21.x build covering 1.21 and 1.21.1, version 2.0, May 2026) · https://www.curseforge.com/minecraft/mc-mods/cinchcraft
- Availability: **probable** (Modrinth-type mirror; Modrinth page not seen).
- Mechanic: isolated houses at various stages of decay, modest early-game loot. Small, silent: the "place" rather than the dungeon.
- Integration: loot replaced by journals of former inhabitants, worn tools (Overgeared) and occasionally a blueprint. Rarity ×2–3.
- **Verdict: recommended.**

#### 10. Forced abandoned villages (in-house datapack, no mod)
- For each village type (plains, desert, savanna, taiga, snow), vanilla has a **"zombie"** variant whose centers lead to dilapidated houses (pools `minecraft:village/<type>/zombie/...`).
- Implementation:
  1. Overwrite `data/minecraft/worldgen/template_pool/village/<type>/town_centers.json` to keep only the `zombie` centers.
  2. The abandoned pieces contain **zombie villagers** in their NBT. Since vanilla hostiles are removed, intercept in KubeJS (`EntityEvents.spawned` on `minecraft:zombie_villager`, cancelled inside a structure). Optionally, a 1% chance to replace them with a hidden survivor.
  3. Block the generation of iron golems and village cats if total emptiness is wanted.
- The "80% Abandoned Villages" datapack (1.21/1.20.6, not seen for 1.21.1) proves the principle, but it is better to write it ourselves.
- **CTOV** (https://modrinth.com/project/35BiK2KV, v3.6.3 NeoForge 1.21.1, village frequency and activation configs) would bring more varied villages per biome, hence more varied ruins. This presupposes also forcing its abandoned variants, if it has any: to check in the .jar. **Optional.**

#### 11. Wandering Villagers
- 1.0.1 NeoForge 1.21.1, gamerule `DoWanderingVillagers` (name not confirmed at the source).
- Spawns villagers near players: this **removes the discovery** of the survivor. **Rejected.**

### C. Places, knowledge and non-material rewards

#### 12. Better Archeology
- Project `zCh7omyG`. Evidence: https://modrinth.com/project/zCh7omyG/version/XlJUda6G · BoxToPlay mirror (1.3.8 NeoForge 1.21.1 listed, not confirmed on Modrinth).
- Availability: **yes** (1.3.4 NeoForge 1.21.1, MIT license).
- Mechanic (from memory, **to verify in the .jar**): fossils assemblable into display skeletons, "unidentified" artifacts to identify at an archaeology table, new sites (tunnels, buried ruins), excavation tools.
- Integration: turns the vanilla brush into a **profession**, and fossils decorate a colony museum. Artifacts can carry the lore of the vanished civilization.
- To disable: artifacts that give combat bonuses or powerful enchantments (competitors to Overgeared).
- **Verdict: recommended, to test** (exact content to inventory).

#### 13. The Athenaeum
- Evidence: https://www.curseforge.com/minecraft/mc-mods/the-athenaeum (Fabric/NeoForge 1.21–1.21.1). Dependencies: Architectury (present), YACL (present), ChimericLib. **Modrinth unverified.**
- Written books generated by datapack and added to loot tables (stronghold libraries and mansions: to redirect, since strongholds are removed).
- **Verdict: optional.** An in-house datapack (`written_book` with the `minecraft:written_book_content` component, injected by the NeoForge modifier `neoforge:add_table`) does the same thing with **our** lore, without a dependency.

#### 14. Explorer's Journal (KayDev)
- Universal journal (diary, encyclopedia, atlas, statistics) that fills in as mobs, biomes and structures are discovered. Dynamic detection through NeoForge registries. https://www.modpackindex.com/mod/207136/explorers-journals
- **Modrinth and 1.21.1 unverified.** Risks: its "Atlas" tab may display coordinates and duplicates AA4.
- **Verdict: optional**, only if the atlas tab and coordinates can be disabled.

#### 15. Exposure (photography)
- Slug: `exposure`. Evidence: https://github.com/mortuusars/Exposure (1.21.1 branch, NeoForge, Forge, Fabric) · BoxToPlay mirror "exposure" (1.9.18 NeoForge 1.21.1).
- Side: CS. No known heavy dependency.
- Mechanic: film cameras, black-and-white or color film (16 frames, color is harder to make and develop), viewfinder (focal length, speed, flash, grids), accessories (redstone lamp as flash, spyglass as lens, glass pane as filter). Development then printing in a **lightroom** (paper + dyes), photo album.
- Integration: makes it a **profession** (chemistry, paper, dyes) and a **proof of discovery**: the photo of the summit, the wreck or the rare creature, hung on the base's wall. The dye basin can go through Create.
- Strong idea (to verify in the .jar): Exposure records data on each frame (framed entities, biome, position, author) and triggers advancement criteria. **Photographic bounties** can then be created in KubeJS ("photograph a Frostmaw" → blueprint or reward). Also check that **the photo tooltip does not display coordinates**.
- **Verdict: recommended.**

#### 16. Lootr
- Slug: `lootr` (project `EltpO5cN`). Evidence: https://modrinth.com/mod/lootr/version/1.21.1-1.11.36.107 (1.21.1-1.11.37.122 the most recent seen).
- Availability: **yes**. Side CS.
- Mechanic: every generated chest has **per-player** contents. In multiplayer, whoever arrives second still finds something.
- Config (`lootr-common.toml`, keys from memory **to verify**): no refresh or decay (each discovery is unique), break protection enabled, mod structure conversion enabled.
- Interaction: FogRule (rejected) required Lootr. Lootr also makes in-house loot per-player ("personal" lore journals).
- **Verdict: core.**

### D. Crossing and stamina

#### 17. Paraglider (Tictim)
- Modrinth slug: `paragliders` (page seen with 26.1 builds). The repository https://github.com/Tictim/Paraglider has a `release/1.21.1` branch: `gradle.properties` gives **mod_id `paraglider`, version 21.1.5, NeoForge 21.1.186+**. SkinMC (Modrinth mirror) lists NeoForge 1.21.1 builds 21.1.2 and 21.1.3.
- Availability: **probable** (1.21.1 file on Modrinth not seen directly).
- Side: CS.
- Full BotW mechanic: glider, **stamina wheel**, **updrafts** over wind sources (campfires, adjustable by datapack), spirit orbs, Goddess statues and horned statues (orbs ↔ stamina vessels or hearts exchange), statues added to villages.
- Config read from the source (1.21.1 branch):
  - `paraglider-common` → `features.spiritOrbGens`, `features.heartContainers`, `features.staminaVessels`, `features.villageStructures` (true by default).
  - `paraglider-server` → `updraft` (true), `paraglidingSpeed` (1.0), `paragliderDurability` (0 = no wear), `paragliderInTowersOfTheWild`, `spiritOrbs.enderDragonDropsVessel`, `spiritOrbs.witherDropsVessel`, `spiritOrbs.raidGivesVessel`, `spiritOrbs.elderGuardianDropsSpiritOrb`, `spiritOrbs.spawnerSpiritOrbDrops` (1.0), `spiritOrbs.trialSpiritOrbDrops` (2.0), `spiritOrbs.ominousTrialSpiritOrbDrops` (4.0), `spiritOrbs.spiritOrbLoots` (true), `vessels.startingHearts` (10), `vessels.maxHeartContainers` (20), `vessels.startingStamina` (1000), `vessels.maxStaminaVessels` (10), `vessels.staminaIncreasePerVessel` (200), `stamina.paraglidingConsumesStamina` (true), `stamina.runningAndSwimmingConsumesStamina` (false).
- Proposed settings for Fallow:
  - `heartContainers = false` (no extra hearts: survival stays environmental).
  - `raidGivesVessel = false`, `witherDropsVessel = false` (no Wither or raids wanted).
  - `spawnerSpiritOrbDrops = 0`, `trial*SpiritOrbDrops = 0` (trial chambers will be removed or emptied).
  - `stamina.runningAndSwimmingConsumesStamina = true` (running and swimming become a choice).
  - `paragliderDurability` ≈ 600 (repair at Overgeared).
  - `spiritOrbLoots = true`, but orbs removed from vanilla chests and injected **only** into NML's sanctuaries and monuments, via loot modifier.
  - `villageStructures = true` if the statues are copied into the abandoned village pools: a Goddess statue in a ruin = sanctuary.
- Glider recipe: lock it behind Overgeared (canvas + forged frame), or find it in an Aeronautics wreck.
- **Verdict: core.** It is the heart of the BotW crossing and it replaces the removed Elytra.

#### 18. ParCool!
- Project `Fsvx2bdR`. Evidence: https://modrinth.com/project/Fsvx2bdR · BoxToPlay mirror (1.21.1-4.0.0.5 NeoForge).
- Availability: **yes**. Side CS.
- Mechanic: parkour (ledge grab, ledge climb, roll, wall jump, wall slide, wall run, dodge, crawl, dive…). Internal stamina, **or stamina supplied by Paraglider** (official compatibility 1.20.1 and 1.21.1, to set in the config).
- Restriction: disable server-side the "cinematic" actions (horizontal and vertical wall run, dodge, flips, charged jump, cat leap, fast run), and keep **ledge grab, ledge climb, wall jump, landing roll, vault, crawl**. This gives BotW climbing without super-maneuvers. ParCool also has **ziplines** (to compare with Supplementaries' rope and pulley; keep only one logic).
- Conflict: player animations possibly in conflict with other animation mods (none in the pack). Check ParCool with Sable contraptions (walking on a moving airship).
- **Verdict: recommended, restricted.**

#### 19. Grappling hooks: Hooked (2.0.2 NF 1.21.1), Yori3o's Grappling Hooks (4.7.0-beta.2 NF 1.21.1)
- Available, but a grappling hook makes verticality trivial and duplicates ParCool + Paraglider. BotW has none.
- **Verdict: rejected for now.** To reopen as a late engineer's tool (forged at Overgeared) if climbing proves frustrating.

#### 20. Mounts and boats
- **Icy's Better Horses** is already retained (lead, whistle, breeds, house). Nothing to stack on it: SWEM and SWEM Lite are **rejected** (duplicates, and Modrinth 1.21.1 availability unverified).
- Boats: Create Aeronautics + Sable cover ships. No boat mod to add (not researched, quota exhausted). Streams Reflowing pushes vanilla boats with its currents: the behavior of Sable ships in those currents remains to be tested.

### E. Navigation without coordinates

#### 21. Hiding coordinates (vanilla)
- `/gamerule reducedDebugInfo true` hides XYZ, the targeted block, the biome and the shortcuts F3+B, F3+G and chunk borders, for all players. Preset in the world datapack, or in KubeJS `ServerEvents.loaded` → `server.runCommandSilent('gamerule reducedDebugInfo true')`.
- Complements:
  - Vanilla `showCoordinates` option: irrelevant in Java.
  - Supplementaries: "compass coordinates" already disabled (0006).
  - Surveyor's `/waypoints` and `/landmarks` commands: check whether they print coordinates and restrict their permission level.
  - Exposure: check the tooltips.
  - Antique Atlas screen: check that it does not display coordinates on hover.
  - Death screen: vanilla does not display coordinates. The recovery compass is enough.

#### 22. Dead Reckoning (Surveyor addon)
- https://github.com/sisby-folk/dead-reckoning (formerly "Antique Atlas Compass HUD", inspired by Compass Ribbon).
- **Skyrim-style compass ribbon** in the HUD, with colored Surveyor markers. Every element can be disabled. The ribbon can appear **only when holding a compass**, or having one in the hotbar or inventory.
- 1.21.1 availability on Modrinth: **unverified**. A Fabric mod, it would go through Connector like AA4.
- Proposed setting: show the ribbon only **with a compass in hand**, cardinal points only, **markers disabled**. Markers on the ribbon would be disguised waypoints, which is forbidden. At best, show only the "home" mark.
- **Verdict: recommended if available.** Fallback: **NeoCompass** (NeoForge 1.21.1 compass HUD, client, no coordinates) or **Compass Plus** (2.0.0, NeoForge 1.21.1: vanilla HUD with cardinals, spawn, lodestone, last death).

#### 23. AA4 Atlas and Antique Trains (Antique Atlas addons)
- **AA4 Atlas** gives the atlas a real item identifier and a **recipe**. Combined with the AA4 option "the atlas must be in the inventory to open the map", the map becomes an **item** that is crafted, carried and can be lost. **Recommended if available** (unverified).
- **Antique Trains** presumably displays the Create network on the atlas (unverified). **Optional.**
- Surveyor settings (`config/surveyor.toml`):
  - `globalSharing=false`: everyone draws their own map, and sharing goes through `/surveyor share` or, better, through the **copy to and from vanilla maps** that Surveyor can do. Bringing a filled map back to the base becomes a cartographer's gesture.
  - `Builtins` section: keep portal markers disabled (Nether to be settled) and the grave marker as desired.
- AA4 config (`config/antique-atlas.toml`): list of structures marked automatically (after having been seen). Mark NML monuments, wrecks and camps, but **not** small ruins (discovery must stay personal).

#### 24. Traveler's Titles
- v5.1.3 NeoForge 1.21.1 + YUNG's API. Client. Evidence: https://www.curseforge.com/projects/1015155 · https://gdlauncher.com/mods/modrinth/travelers-titles (Modrinth reference).
- Availability: **probable**.
- Displays the **biome name as a title** on entering it (BotW or Skyrim style), with a sound customizable per biome in a resource pack (`travelerstitles` namespace). Compensates for the biome hidden by `reducedDebugInfo`.
- It is **not** a "YUNG's Better *": it is a HUD, not structures. NML biomes can be renamed to evocative French names through a resource pack.
- **Verdict: recommended.**

### F. Camping and inventory

#### 25. Traveler's Backpack
- Project `rlloIFEV`. Evidence: BoxToPlay mirror (10.1.38 NeoForge 1.21.1).
- Availability: **yes**. Side CS.
- Backpack worn and visible on the back, **two fluid tanks** (water), crafting grid, tool slots, **deployable sleeping bag**, capacity variants.
- Config to check in the .jar:
  - disable the variants' **abilities** (magical effects);
  - check that the sleeping bag **does not set** the respawn point;
  - check that the tanks **keep the purity** of Thirst Was Reclaimed water (otherwise it is an infinite canteen that bypasses Thirst Canteen).
- Recipes: to be locked by Overgeared (tanned leather, forged buckles).
- **Verdict: recommended.**
- **Sophisticated Backpacks** (3.26.6 + Sophisticated Core) is **rejected**: its upgrades (magnet, compacting, feeding, filters) make storage logistics that duplicates Create.

#### 26. Comforts
- Project `SaCpeal4`. Evidence: https://modrinth.com/project/SaCpeal4/version/duBFOcde (9.0.3+1.21.1 NeoForge).
- Availability: **yes**. Side CS.
- **Sleeping bags** (sleep without changing the respawn point) and **hammocks** (skip from day to night).
- Partial duplicate with Traveler's Backpack's sleeping bag: keep Comforts for nights in the field, and disable TB's if the config allows.
- **Verdict: recommended.**

#### 27. Tents, campfires, cairns
- **Not researched** (quota exhausted). Mod-free leads:
  - vanilla campfire + Paraglider updrafts (the camp becomes a takeoff point);
  - fire heat in Cold Sweat;
  - cairns = stones and slabs placed by players (Quark Hedges, Wooden Posts);
  - Supplementaries direction signs (already retained);
  - Supplementaries flags, visible from afar.
- To research in a next pass: a 1.21.1 NeoForge tent mod on Modrinth.

### G. Geography and rivers

#### 28. Streams Reflowing (combined with NML, not as a competing preset)
- Project `oLS8HdJ1`, author NiceJohn (unrelated to delvr's original Streams). Evidence: https://modrinth.com/project/oLS8HdJ1/version/5STkQGAk · https://www.modpackindex.com/mod/157991/streams-reflowing
- Availability: **yes** (2.13.1, NeoForge 1.21.1–1.21.5, recently updated). Server side (generation).
- Mechanic: digs **streams that follow the terrain**, pool into lakes and flow into the ocean. **Directional current on vanilla water** (no custom fluid since 2.6.0, so mods that read water stay compatible). Boats and floating items carried by the current, including on a dedicated server (2.8.4). Waterfalls. Clay in stream beds. Clean merge with ponds (2.12.1).
- **Key point for decision 0001**: Streams Reflowing appears to be a carving layer **independent of the biome source**. It could therefore be added to **NML** instead of requiring Terralith. NML would keep the atmosphere, and Streams would provide the rivers useful for Create (water wheels) and for navigation. Since "Terralith alone" is rejected, **NML + Streams Reflowing** is the first test to run.
- Risks:
  - interaction with the rivers of NML and Biolith (double beds);
  - behavior of Sable/Aeronautics ships in the current: pushed, ignored, or unstable;
  - generation cost (improved in 2.8.4);
  - do Create's water wheels read Streams' current?
- **Verdict: to test first** (`test/streams-nml` branch).

#### 29. Distant Horizons (already "to test")
- For exploration, it is the best "I wonder what is over there": silhouettes of mountains and towers in the mist. To be paired with ATMOSPHERICS (distant fog must veil the LODs, not reveal them sharply).
- Sable risks and server cost: not researched, quota exhausted.

#### 30. Towers of the Wild Modded
- Slug `totw-modded`. Availability: **yes** (1.0.9, NeoForge 1.21–1.21.11, January 2026).
- Observation towers inspired by Sheikah towers. Paraglider has native compatibility (`paragliderInTowersOfTheWild`, which places the glider in the towers' chests).
- Against: close to "Structory: Towers", rejected on taste, and towers visible from afar go against "rare and silent".
- **Verdict: optional**, to show in a screenshot. If retained: rarity ×6, ruined towers only, chest = glider.

---

## 3. Exploration rewards: knowledge rather than diamonds

Implementation tools, with no new mod:
- NeoForge modifier `neoforge:add_table` (global loot modifier, datapack) to **add** tables to existing loot without overwriting it. To remove, `LootJS` is more convenient (Modrinth 1.21.1 **unverified**); otherwise, JSON override.
- **Lithostitched** (already present): `add_template_pool_elements` to inject pieces (survivors, Paraglider statues, Create remains) into the abandoned village and NML pools without overwriting them. Structure set modifiers.
- Vanilla `doLimitedCrafting` + KubeJS `player.unlockRecipe` / advancements.

Proposed system:
1. **Engineer blueprints** (KubeJS item, one per tier: water wheel, tank, train, propeller, balloon…). Found in Aeronautics wrecks, engineer houses and New Beginnings remains. Right-click = unlocks a batch of Create or Aeronautics recipes, and **Overgeared blueprints** for forged parts (see the Civilization research). **Conflict**: Quark's "Automatic Recipe Unlock" module, enabled in 0005, unlocks everything. It must be **disabled** and `doLimitedCrafting` enabled on a restricted list (key recipes only, not the whole game).
2. **Paraglider spirit orbs** only in NML sanctuaries and monuments, traded to a Goddess statue (found in ruins) for stamina. This is BotW's "shrine" loop.
3. **Journals and lore**: in-house written books (Supplementaries' ancient ink for the look), per player thanks to Lootr. They tell of the vanished civilization and vaguely point to other places ("beyond the second ridge, toward the setting sun"), without coordinates.
4. **Exposure photographic bounties** (if the frame data allows).
5. **Survivors**: the most precious reward. A qualified villager who unlocks a profession in the colony.
6. **Fossils and artifacts** (Better Archeology) for the colony museum.
7. **Discovery advancements** (`location` + `structure` predicate, or biome): title, journal line, no resource.

---

## 4. A coherent exploration system

### Gameplay loop
1. **See**: from the base, a silhouette in the mist (Distant Horizons + ATMOSPHERICS), a column of campfire smoke (survivor), an airship wreck.
2. **Orient** without coordinates: sun and stars, compass in hand (Dead Reckoning ribbon, cardinal points only), direction signs placed by players, flags and cairns, biome name on entry (Traveler's Titles).
3. **Cross**:
   - on foot, with Paraglider stamina shared with ParCool: ledge climb, wall jump;
   - by glider, from high places and campfire updrafts;
   - on horseback (Icy's Better Horses), by boat on Streams rivers, later by train or airship.
4. **Camp**: Traveler's Backpack (water, tools), Comforts sleeping bag, campfire (Cold Sweat heat and updraft).
5. **Discover** a rare, silent place: NML sanctuary, abandoned house, ruined village, Create remains, wreck, survivor camp.
6. **Bring back**: the map (AA4 drawn on foot, copied to a vanilla map to share it), photos (Exposure), a blueprint, a journal, orbs, a survivor.
7. **Convert** at the base: the blueprint unlocks a recipe, the survivor becomes a resident, the orbs become stamina, the photos decorate the wall. The frontier recedes, and the new machines (rail, airship) allow going further.

### Who does what
| Function | Mod |
|---|---|
| Map (fog of war) | Antique Atlas 4 + Surveyor (+ AA4 Atlas) |
| Direction | vanilla compass + Dead Reckoning (or NeoCompass) |
| Region landmark | Traveler's Titles |
| Atmosphere places | No Man's Land (core) + Cinch's Abandoned Houses + forced abandoned villages |
| "Lost civilization" places | Create Aeronautics Structures + Create: New Beginnings + Let The Adventure Begin (sorted) |
| Survivors | MCG's RPG Campsites + Aeronautics engineer + in-house injections |
| Multiplayer loot | Lootr |
| Crossing | Paraglider (stamina) + ParCool (restricted) + Icy's Better Horses + Sable |
| Camp | Traveler's Backpack + Comforts + campfire |
| Proofs and souvenirs | Exposure, Better Archeology |
| Rivers | Streams Reflowing (to test on NML) |

### Structure rarity (Sparse Structures, global `spreadFactor`, default 2)
- Global: **3**.
- Per-structure overrides, starting values:
  - small NML places: 1.5–2;
  - Cinch's: 3;
  - abandoned villages: 4;
  - Create New Beginnings and Let The Adventure Begin: 4–5;
  - survivor camps: 6;
  - Aeronautics wrecks and engineer houses: 6;
  - ToTW (if retained): 6.
- The exact format of per-structure overrides in the 1.21.1 version remains to be read in the .jar.
- **Disable** (empty `structure_set` datapack or empty `biome` tag):
  - Strongholds and End: already decided.
  - Trial chambers: hostile spawners.
  - Pillager outposts, mansions: hostile and RPG.
  - Ocean monuments and witch huts: empty or remove (cf. 0004).
  - Ancient cities: to be settled. Strong atmosphere but Warden: disable, or keep empty as a "strange" place.
- Keep shipwrecks, ocean ruins and desert wells (silent).
- Silence rule: no structure may produce sound, strong light or a hostile mob. The only long-distance signal allowed is a survivor's **campfire smoke** (hay bale under the fire, injected into the camps).

### Hiding coordinates (recap)
1. `reducedDebugInfo true` (gamerule forced at server startup).
2. Supplementaries compass coordinates: off (done).
3. Check AA4, Surveyor, Exposure and Explorer's Journal for any coordinate display.
4. Remove `/locate`, `/tp` and `/waypoints` from non-ops (permission level).
5. Exploration maps: vanilla cartographer, MCG's cartographer, Quark's Pathfinder Maps (off, done), Supplementaries adventurer maps (off, done).

### To disable or configure (recap)
- Paraglider:
  - `heartContainers=false`, `raidGivesVessel=false`, `witherDropsVessel=false`;
  - `spawnerSpiritOrbDrops=0`, `trialSpiritOrbDrops=0`, `ominousTrialSpiritOrbDrops=0`;
  - `runningAndSwimmingConsumesStamina=true`, `paragliderDurability>0`;
  - orbs in NML loot only.
- ParCool: only ledge grab, ledge climb, wall jump, roll, vault and crawl remain active; stamina supplied by Paraglider.
- Quark: **disable Automatic Recipe Unlock** if the blueprint system is adopted.
- Surveyor: `globalSharing=false`. AA4: atlas required in the inventory, restricted list of marked structures.
- Dead Reckoning: compass required, markers off.
- Lootr: no refresh or decay, break protection.
- Traveler's Backpack: abilities off, sleeping bag without respawn point, water purity to check.
- MCG Campsites: remove the ruined portal, filter the cartographer's trades, add a hay bale under the campfire.
- Create Aeronautics Structures: filter the engineer's trades, check the "robber balloons".
- Villages: `zombie` centers only, and removal of zombie villagers in KubeJS.

---

## 5. Uncertainties to check in the .jar files
1. **Paraglider**: presence of the 1.21.1 build on Modrinth (slug `paragliders`). List of default wind sources (`paraglider/wind` datapack?) and definition of the statue structures (villages and underground).
2. **ParCool**: exact name of the server limitation keys and of the "alternative stamina = Paraglider" option. Presence of the zipline in 1.21.1.
3. **Create Aeronautics Structures**: nature of the "robber balloons" (mobs?), loot tables, rarity configuration, trades of `villager_create_engineer`.
4. **Create: New Beginnings**: list of pieces and targeted pools (`minecraft:village/*/houses` only, or also `zombie/*`?), assembled contraptions or not.
5. **Let The Adventure Begin**: inventory of `structure_set`s and loot. **Structures Arise**: AnvilCore dependency.
6. **Sparse Structures 1.21.1**: format of per-structure overrides in `sparsestructures.json5`.
7. **MCG's RPG Campsites**: structure and loot identifiers, and whether villagers have locked professions (no profession change at the workstation).
8. **Cinch's Abandoned Houses**: presence on Modrinth for 1.21.1 NeoForge.
9. **Traveler's Backpack**: config keys (abilities, sleeping bag, tanks), and interaction of the tanks with Thirst Was Reclaimed purity.
10. **Comforts**: does the sleeping bag set a respawn point? Possibility of disabling TB's.
11. **Exposure**: frame data (entities, biome, position), exposed advancement criteria, and possible display of coordinates in the tooltip.
12. **Better Archeology**: list of artifacts, combat bonuses to cut, added structures.
13. **Dead Reckoning, AA4 Atlas, Antique Trains**: 1.21.1 builds (Fabric via Connector) on Modrinth.
14. **AA4 and Surveyor**: coordinates displayed on the map screen? Permission of `/surveyor share`? Copy to vanilla maps working under Connector?
15. **Streams Reflowing + NML**: combined generation (double beds?), Sable ships in the current, Create water wheels.
16. **Lootr**: exact keys (`lootr-common.toml`).
17. **Traveler's Titles**: Modrinth 1.21.1 NeoForge presence (v5.1.3), and required YUNG's API version.
18. **Quark**: effect of disabling Automatic Recipe Unlock on the recipe book, and coexistence with `doLimitedCrafting`.

## 6. Not covered (search quota exhausted: next pass)
Tent mods, alternative climbing, boats, "exploration event" mods, trails that wear in with passage, Distant Horizons + Sable server-side risks, LootJS 1.21.1, Explorer's Journal and The Athenaeum on Modrinth.

## Main sources
- Paraglider: https://github.com/Tictim/Paraglider (branch `release/1.21.1`: `gradle.properties`, `config/ServerConfig.java`, `config/CommonConfig.java`) · https://skinmc.net/project/paragliders · https://modrinth.com/mod/paragliders/version/Ix3nj2fZ
- ParCool: https://modrinth.com/project/Fsvx2bdR · https://www.curseforge.com/minecraft/mc-mods/parcool
- Lootr: https://modrinth.com/mod/lootr/version/1.21.1-1.11.36.107 · https://github.com/noobanidus/Lootr
- Exposure: https://github.com/mortuusars/Exposure · https://www.feed-the-mods.com/exposure-mod/
- Create Aeronautics Structures: https://modrinth.com/project/PZIWeQhv · https://www.curseforge.com/minecraft/mc-mods/create-aeronautics-structures
- Create: New Beginnings: https://modrinth.com/mod/IAnP4np7 · https://www.curseforge.com/minecraft/mc-mods/create-new-beginnings
- Create: Structures Arise: https://modrinth.com/project/9enMEvoc/version/HSECDqTu
- Create: Rustic Structures, Let The Adventure Begin, Traveler's Backpack, Sophisticated Backpacks, Better Archeology, MCG's RPG Campsites, Wandering Villagers: BoxToPlay mirrors of the Modrinth pages (www.boxtoplay.com/…/modrinth-mods/… and …/modrinth-datapacks/…)
- IDAS / Integrated Villages: https://modrinth.com/mod/idas · https://modrinth.com/project/integrated-villages · https://www.curseforge.com/minecraft/mc-mods/integrated-villages/files/7815319
- CTOV: https://modrinth.com/project/35BiK2KV · https://www.curseforge.com/minecraft/mc-mods/choicetheorems-overhauled-village/files/7653085
- Comforts: https://modrinth.com/project/SaCpeal4/version/duBFOcde
- Traveler's Titles: https://www.curseforge.com/projects/1015155 · https://gdlauncher.com/mods/modrinth/travelers-titles
- Antique Atlas 4 / Surveyor / Dead Reckoning: https://github.com/sisby-folk/antique-atlas · https://github.com/sisby-folk/surveyor · https://github.com/sisby-folk/dead-reckoning
- Streams Reflowing: https://modrinth.com/project/oLS8HdJ1/version/5STkQGAk · https://www.modpackindex.com/mod/157991/streams-reflowing
- Sparse Structures: https://modrinth.com/mod/sparsestructures · https://www.modpackindex.com/mod/47512/sparse-structures
- Towers of the Wild Modded: https://modrinth.com/project/m5T5xmUy/version/Rt96zVC1
- Cinch's Abandoned Houses: https://www.mado-hosting.com/minecraft-mods/cinchs-abandoned-houses
- The Athenaeum: https://www.curseforge.com/minecraft/mc-mods/the-athenaeum
- Explorer's Journal: https://www.modpackindex.com/mod/207136/explorers-journals
- Create: Dynamic Village: https://curseforge.com/minecraft/mc-mods/dynamic-village
- Grappling hooks: https://www.boxtoplay.com/en/minecraft-hosting/minecraft-server/modrinth-mods/hooked-30557 · …/yori3os-grappling-hooks-39516
- reducedDebugInfo: https://feedback.minecraft.net/hc/en-us/community/posts/39283635611021
