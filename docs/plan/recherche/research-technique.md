# Research: technical foundation, server, QoL and KubeJS tooling (Fallow, 1.21.1 NeoForge)

> Scope refocused after the plan update. This does not re-propose what is already selected (Sodium 0.8.13, ImmediatelyFast, Entity Culling, ModernFix, FerriteCore, Lithium, Clumps, Structure Layout Optimizer, Chunky, spark, Connector + FFAPI) nor what is ruled out (C2ME, Noisium, MoreCulling, Ksyxis). It does, however, flag **two risks on mods already selected** (Entity Culling, Distant Horizons).
>
> **Limits of the research.** Modrinth and CurseForge cannot be read directly. Verification was done through web searches (Modrinth/CF excerpts), GitHub repositories and the bug trackers of Aeronautics, Sable and Connector. **The shared search quota ran out during the research.** For the second half of the mods, the availability given therefore comes from the Modrinth links found on their GitHub repositories and from general knowledge, and is marked "probable" or "not verified". To be confirmed at the time of `packwiz mr add`, which fails immediately if the 1.21.1 NeoForge version does not exist.

---

## 0. Priority alerts

| # | Alert | Severity | Action |
|---|---|---|---|
| A1 | **Entity Culling breaks Aeronautics/Sable contraptions.** Open issues [Simulated-Project#1347](https://github.com/Creators-of-Aeronautics/Simulated-Project/issues/1347) and [sable#1414](https://github.com/ryanhcode/sable/issues/1414): blocks with a block entity (trees, analogue lever, wind turbine) on an assembled airship become unusable. [#974](https://github.com/Creators-of-Aeronautics/Simulated-Project/issues/974): propeller sails shake, which suggests *tick culling*. No reply from the maintainers. | **High** | First test `tickCulling: false` and `skipBlockEntityCulling: true` in `config/entityculling.json` (keys to confirm in the .jar). If the bug persists, **remove Entity Culling**. It is client-only and can be removed without risk. Hypothesis: Sable sub-levels live in a remote "plot" area, so that the occlusion raycast judges them invisible. |
| A2 | **`allow-flight=true` is mandatory** in `server.properties`. Without this setting, a player standing on an airship or a moving contraption is kicked ("Flying is not enabled"). | High | To be put in the server configuration. |
| A3 | **JEI ≥ 19.44.0.406 crashes Aeronautics < 1.3.2** ([#1405](https://github.com/Creators-of-Aeronautics/Simulated-Project/issues/1405), fixed in 1.3.2). The pack has 1.3.2. | Medium | Never downgrade Aeronautics below 1.3.2 if JEI is recent. |
| A4 | **Known Aeronautics incompatibilities** to pass on to the other research tracks: Does It Tick (#974), Flowing Fluids (#846), Leaves Be Gone (#866), Flashback (#867), Tpsum (#1406), Refurbished Furniture (energy, #908), Carry On (impossible to pick up a block placed on a ship, #848). | Medium | Pack blacklist. |
| A5 | **Distant Horizons disables vanilla fog by default** ("Disable Vanilla Fog" option in the Fog section; exact key to confirm). With ATMOSPHERICS, the mist would disappear. | Medium (DH branch) | Set the option to `false` and adjust DH's own distant fog (see §3). |
| A6 | **Sodium 0.8.x is compatible with no version of "Sodium Options API"** (version page [mc1.21.1-0.8.12](https://modrinth.com/mod/sodium/version/mc1.21.1-0.8.12-beta.1-neoforge)). | Low | Rule out Sodium Extra, Reese's Sodium Options and any mod that depends on them. |

---

## 1. Candidate mod sheets (38)

Legend: **Avail.** = 1.21.1 NeoForge version on Modrinth. **S** = foundation · **R** = recommended · **O** = optional · **X** = ruled out.

### 1.1 Server, stability, backups

**1. Neruina — Ticking Entity Fixer** · `neruina` · [GitHub](https://github.com/Bawnorton/Neruina) · [Modrinth](https://modrinth.com/mod/neruina)
- Avail.: probable (the repository contains a NeoForge build and points to Modrinth; 1.21.1 version not verified directly). Server-side, to install on both sides (messages). No dependencies.
- Role: when an entity, a block entity or an item throws an exception on every tick, Neruina pauses it instead of crashing the server. Essential for a permanent world with Create, Sable and 70 mods. Tracked entities remain so after a restart.
- Config `config/neruina.json`: `auto_kill_ticking_entities=false` (the pause is kept so as to investigate), `ticking_exception_threshold=10` (deliberate crash if errors run away), `min_permission_level_for_commands=2`. "Report" option: automatic report on GitHub, to be disabled if nothing is to be sent.
- **S**.

**2. Simple Backups** · `simple-backups` · ChaosTheDude
- Avail.: not verified (quota exhausted). From general knowledge, it is on Modrinth with NeoForge 1.21.x builds. Server-side.
- Role: scheduled ZIP backups, in full or "modular" mode (differential since the last full one).
- Config (keys to confirm): `backupType` (FULL / MODULAR), `timer` in minutes, `fullBackupTimer`, `backupsToKeep`, `compressionLevel`, `outputPath`, `sendMessages`, `maxDiskSize`, back up only if players are connected.
- **S**, as the first level of backup. The off-machine level is described in §6.

**3. FTB Backups 2 / 3** · [FTB-Backups-2](https://github.com/FTBTeam/FTB-Backups-2) · [FTB-Backups-3](https://github.com/FTBTeam/FTB-Backups-3)
- FTB Backups 2 is LTS up to 1.21.5. Version 3 (1.21.1+) is only distributed on CurseForge according to its README.
- **X**: does not respect the Modrinth constraint. Textile Backup only works on Fabric (**X**).

**4. Crash Assistant** · `crash-assistant` · [GitHub](https://github.com/KostromDan/Crash-Assistant) · [Modrinth](https://modrinth.com/mod/crash-assistant)
- Avail.: probable (the repository covers Forge/NeoForge/Fabric, branches "1.20.6-1.21.4"). Client-side.
- Role: after a crash, a window shows the logs, allows uploading them in one click and offers automatic analysis and fixes. It also warns if the game is running on the integrated graphics card, a frequent cause of lag among friends. The help text is configurable (Discord link, instructions).
- **R**.

**5. Packet Fixer** · `packet-fixer`
- Avail.: not verified. Client and server side.
- Role: raises the packet size and NBT limits. "Payload may not be larger than…" errors or kicks often occur with Create schematics, large Sable assemblies and large inventories.
- **O**: to add at the first symptom. No gain as long as the problem does not appear.

**6. ServerCore** (NeoForge 1.5.19+1.21.1 according to a Modrinth mirror)
- Role: dynamic view distance, entity *activation range*, tick optimisations.
- **X** for now: *activation range* and tick slowdown risk breaking Create contraptions, trains and animals. Lithium already covers most of it. To be reconsidered only if spark shows server saturation, and then with *activation range* turned off.

**7. Open Loader / Global Packs**
- **X**: unnecessary. KubeJS already loads `kubejs/data/**` as a datapack and `kubejs/assets/**` as a resource pack. The only case that would justify it is a worldgen datapack not taken into account at world creation: to check at first launch (§7).

### 1.2 Chunkloading and claims

**8. Create: Power Loader** · `create-power-loader` · [GitHub](https://github.com/hlysine/create_power_loader) (already "to be tested")
- Avail.: the README announces Create 6 on 1.21.1, with a Modrinth link. The NeoForge 1.21.1 port is credited there; to confirm. Client and server side.
- Role: chunk loaders that run on rotational power. Andesite version: 1 chunk. Brass version: from 1×1 to 5×5. They work on the ground, on trains and on contraptions, can be attached to stations, are switched by contraption controls, and their recipes can be replaced by datapack.
- Integration: it **serves "upkeep" instead of breaking it**. If rotation stops, the chunk unloads. A real power source is therefore needed: mill, steam engine, wind turbine.
- To configure: maximum number per player, radius of the brass loader, stress cost. To test: does a loader placed on a **Sable sub-level** (airship) follow the ship? Sub-levels are not Create contraptions, so the answer is not guaranteed.
- **R**: it is the pack's only chunk-loading mechanism.

**9. Open Parties and Claims (OPAC)** · `open-parties-and-claims` · [GitHub](https://github.com/thexaero/open-parties-and-claims)
- Avail.: probable (it covers NeoForge and points to Modrinth). Client and server side. **Shows no map**: its interface goes through the Xaero maps, absent here. Managed through commands and a screen.
- Role: groups, base protection, chunk loading.
- Config `serverconfig/openpartiesandclaims-server.toml`: limit the number of claims per player and **set to 0 the chunks loaded per claim**, so that Power Loader remains the only way to keep chunks loaded (keys to read in the .jar).
- **O**: useful only if the group wants to protect itself against accidents. Among friends, it can be done without.

**10. FTB Chunks** — **X**: it provides a large map and a minimap with waypoints. The minimap can be disabled, but the full-screen map remains, which contradicts the vision. It also depends on FTB Library, distributed mainly on CurseForge.

### 1.3 Recipe viewer, tooltips, inventory

**11. JEI** · `jei`
- Avail.: yes (19.x versions NeoForge 1.21.1, cited in [#1405](https://github.com/Creators-of-Aeronautics/Simulated-Project/issues/1405)). Client and server side.
- Role: Create 6, Aeronautics (mixin `silence_jei`), Overgeared, Kaleidoscope Cookery and Farm & Charm all provide a native JEI plugin. KubeJS 2101 integrates `jei`, `emi` and `rei` (`integration/` folder of the [2101](https://github.com/KubeJS-Mods/KubeJS/tree/2101/src/main/java/dev/latvian/mods/kubejs/integration) branch), which makes it possible to hide entries and add info.
- **S**. It is the solution with the fewest moving parts, and the combination tested by the Aeronautics team.

**12. EMI** · `emi` · [GitHub](https://github.com/emilyploszaj/emi)
- Avail.: probable (it covers NeoForge). Client and server side.
- Role: recipe tree, favourites and batch crafting, very useful for the long Overgeared → Create chains. JEI plugins are read through JEMI if JEI is present.
- **O**: to try on a branch, with JEI installed at the same time (JEMI). Risk: Aeronautics' JEI mixin. KubeJS supports it.

**13. Jade** · `jade`
- Avail.: yes from general knowledge (15.x NeoForge 1.21.1); the README points to CurseForge and the documentation. Client-side, with a server part for the data.
- Role: displays the block name, the required tool and the state of machines. It complements the Create goggles.
- Settings: in `config/jade/plugins.json`, **check that there is no coordinates plugin** (Jade has one, disabled by default, to be locked) and turn off mob health (`entity_health`), which goes against the idea of "little combat". `jade.json` → `displayMode` on LITE or HOLD_KEY so as not to clutter the screen. Server-side, see whether `server-plugin-overrides.json` exists to enforce these settings (to confirm).
- **R**.

**14. AppleSkin** · `appleskin` — Avail.: probable. Client and server side.
- Role: hunger and saturation preview, useful to compare Kaleidoscope Cookery dishes. Thirst Was Reclaimed has its own bar.
- **R**.

**15. Mouse Tweaks** · `mouse-tweaks` — Avail.: probable. Client-side.
- Role: drag-and-drop and scroll wheel. Complements Quark's sorting and *Easy Transferring*, already active.
- **R**: light, no known conflict.

**16. Controlling + Searchables** · `controlling`, `searchables` — Avail.: probable. Client-side.
- Role: search and spotting of key conflicts. Quark, Create, Aeronautics, Antique Atlas, voice and companions will inevitably create conflicts.
- **R**.

**17. Configured** (+ **Catalogue**) · `configured`, `catalogue` — Avail.: probable. Client and server side.
- NeoForge 21.1 can already display client and common configs. Configured adds **in-game editing of server and world configs** by an operator, convenient to adjust Cold Sweat or Ecliptic without relaunching.
- Configured **O** (useful to the admin). Catalogue **X**: NeoForge's mod list is enough.

**18. Polymorph** · `polymorph` — Avail.: not verified.
- Role: choose between two conflicting recipes.
- **O**: to add only if the Overgeared / Farm & Charm / Kaleidoscope Cookery conflicts cannot all be settled in KubeJS. Better to remove the conflict than to arbitrate it.

**19. Inventory Profiles Next / Inventory Essentials** — **X**: Quark already does sorting and *Easy Transferring*. Inventory Profiles Next also pulls in Kotlin and libIPN.

### 1.4 Grave and death

**20. GraveStone Mod** · `gravestone-mod` · [GitHub](https://github.com/henkelmax/gravestone)
- Avail.: probable (henkelmax publishes on Modrinth; NeoForge 1.21.1 not confirmed directly). Client and server side.
- Role: a block falls at the place of death. Recovery option by crouching, items put back into their slots, `/restore` command for the admin.
- **To disable: the obituary** ("Obituary"), which **displays the coordinates** of the death, and the **ghost spawn**. To enable: grave breakable by its owner only.
- Synergy: Antique Atlas 4 already marks players' graves on the map, with a configurable style (README). The player finds their death on the atlas, without coordinates.
- **R**.

**21. Corpse** (henkelmax) · `corpse` — The body becomes an entity that carries the inventory. It is immersive, but an entity placed on a moving Sable sub-level is a risk. Its page also gives the coordinates.
- **O**, as a replacement for GraveStone: only one of the two.

**22. Corail Tombstone** — **X**: distributed only on CurseForge as far as is known (not verified). It also adds spells, magic and teleportation, contrary to the vision. **YIGD** — **X**: Fabric-oriented.

### 1.5 Communication and social immersion

**23. Simple Voice Chat** · `simple-voice-chat` · [GitHub](https://github.com/henkelmax/simple-voice-chat)
- Avail.: probable (downloads hosted on Modrinth). Client and server side, on **UDP port 24454**, which must be opened.
- Synergy: Sound Physics Remastered, already in the pack, applies reverberation and occlusion to voices. The valley and the cave can be heard in the voice too.
- Config `config/voicechat/voicechat-server.properties` (keys to confirm): `max_voice_distance` around 32-40 (players hear each other only at close range, which reinforces isolation), `whisper_distance` 12-16, `enable_groups`.
- Decision to make: groups allow talking at any distance. For the vision, `enable_groups=false` is proposed, with long-distance communication going through an item (point 24).
- **R**.

**24. "Radio" addon for Simple Voice Chat** (walkie-talkie, radios) — name and availability **not verified**, to be researched.
- Little-known idea: long-distance communication becomes a diegetic item to craft and power, which creates a link between settlements.
- **O / to research**.

**25. Emotecraft** — **X**: duplicates Quark's Emotes, already active.

### 1.6 Animations and camera (client)

**26. First-person Model** · `first-person-model` (tr7zw) — Avail.: probable. Client-side. One sees one's body in first person, which gives physical presence.
- To test: the camera seated on Aeronautics seats. The Figura case has already caused problems ([#1323](https://github.com/Creators-of-Aeronautics/Simulated-Project/issues/1323)).
- **O**.

**27. Not Enough Animations** · `not-enough-animations` (tr7zw) — Client-side. Eating, map and ladder animations in third person. They go well with the atlas held in hand.
- **O**.

**28. Eating Animation** — Not verified on NeoForge 1.21.1. **O** (Not Enough Animations covers part of it).

**29. Shoulder Surfing Reloaded** · `shoulder-surfing-reloaded` — Probable. Client-side. Over-the-shoulder camera, pleasant for landscapes and exploration.
- To test with the contraption camera.
- **O**. Better Third Person: not verified on NeoForge 1.21.1 (**X**). Hold My Items: not verified (**X**).

### 1.7 Menus and pack dressing

**30. FancyMenu (+ Drippy Loading Screen)** · `fancymenu`, `drippy-loading-screen`
- Probable. Client-side. Dependencies (Konkrete, Melody) to confirm.
- Role: custom title menu and loading screen.
- **O** (later). Meanwhile, the solution without a mod is in §5.4: panorama and splashes through `kubejs/assets`.

**31. Default Options** (BlayTheNinth) · `default-options` — Not verified.
- Role: ships `options.txt` and **default** keys without overwriting players' own at every update: render distance, disabling Sodium's "Improved Transparency" option (requested by ATMOSPHERICS), de-duplicated keys.
- It avoids versioning `options.txt` with packwiz.
- **R**.

### 1.8 KubeJS tooling and guide

**32. LootJS** · `lootjs` · [GitHub](https://github.com/AlmostReliable/lootjs) (1.21.1 branch, NeoForge)
- Server-side, on both sides in the pack.
- Role: remove elytra and eyes of Ender from all tables, ruin loot consistent with the "survivors" theme, creature drops.
- **S**.

**33. MoreJS** · `morejs` · [GitHub](https://github.com/AlmostReliable/morejs) ("1.21" branch, NeoForge)
- 1.21.1 not confirmed. It adds events, notably on **villager and wandering trader trades**.
- Role: filter trades (no Mending, no bypassing Overgeared), as required by the "survivors" concept of decision 0004.
- **R**.

**34. KubeJS Create** · `kubejs-create` · [GitHub](https://github.com/KubeJS-Mods/KubeJS-Create) (`mc=2101` link, NeoForge)
- Compatibility with Create 6.0.10 to check.
- Role: `event.recipes.create.*` schemas (mixing, pressing, sequenced assembly) to link Overgeared, Create and Kaleidoscope Cookery, and to lock automation behind progression.
- **S**.

**35. PonderJS / custom ponders**
- With Create 6, Ponder has become a separate library. KubeJS support (in KubeJS Create 2101 or an addon) is **not verified**.
- **O**: ponders for the pack's own mechanics (seasons → Cold Sweat bridge, Power Loader loaders).

**36. ProbeJS** · [GitHub](https://github.com/Prunoideae/ProbeJS) (1.21 branch)
- Autocompletion in VSCode: extension, `/probejs` then dump, open the instance in VSCode.
- **Development tool only, not to be shipped in `pack/`.** To install in the "Fallow test" instance.
- **R (dev)**.

**37. Almost Unified** · `almostunified` · [GitHub](https://github.com/AlmostReliable/almostunified) (1.21.1 branch, NeoForge/Fabric)
- Unification through `c:` tags with per-mod priority, hiding of duplicates in the viewer, unification of recipes and loot.
- Strong interest for decision 0002: it can unify **the ingredient duplicates between Farm & Charm / Kaleidoscope Cookery** (tomato, lettuce, flour, dough) if the two mods share the same `c:` tags, giving priority to Kaleidoscope Cookery. It can also unify Overgeared / Create metals (plates, nuggets, copper and brass ingots...).
- Point to study in the .jar files: the tags exposed by each mod.
- **R**.

**38. Pack guide: Patchouli or Modonomicon** (already present as dependencies)
- Zero cost: both are already in the pack.
- **Modonomicon**: entries unlocked by progress, multiblocks, and it is the engine of Icy's Better Horses' book. **Patchouli**: simpler, books in datapack and resource pack, hence in `kubejs/data` and `kubejs/assets`.
- Proposal: a "Settler's Notebook" under Patchouli. Its chapters open with custom vanilla advancements: water, first forge, first mill, first rail, first flight.
- **GuideME** ([GitHub](https://github.com/AppliedEnergistics/GuideME)): Markdown guide with 3D scenes and search, but no Modrinth link in the README. **O**. Akashic Tome: not verified, **X**.

**Quests.** FTB Quests only points to CurseForge in its README (**X** under the constraint). Recommended instead: **vanilla advancements in datapack** (`kubejs/data/fallow/advancement/…`), displayed with **Better Advancements** (`better-advancements`, not verified, **O**). They play the role of a journal without an RPG-style task list, which fits "no end, one perfects".

### 1.9 Distant Horizons (kept conditionally, "pending"): settings for the mist

- **DH**: version 2.3.0-b for 1.21.1 brought the dedicated server, server-side LOD generation, a separate client/common/server config, the fix for vertical fog and a fade between DH and vanilla rendering ([changelog 2.3.0-b](https://modrinth.com/mod/distanthorizons/version/2.3.0-b-1.21.1)). A 2.4.0-b exists for 1.21.1.
- **Mist and DH are compatible, and even complementary.** DH has its own distant fog (start, end, density, exponential type, height fog). With a dense exponential fog, LODs appear as **washed-out silhouettes** of mountains at 500 blocks, which corresponds exactly to the art direction "mist, distance, silhouettes".
- Rules:
  1. Turn off "Disable Vanilla Fog" so that ATMOSPHERICS' near fog remains.
  2. DH fog exponential, starting early.
  3. Moderate LOD radius (64 to 128 chunks): with a dense fog, a larger radius is expensive without showing anything.
- **Separate Sable Render Distance (SSRD)** ([Modrinth](https://modrinth.com/mod/ssrd)) is indispensable with DH: otherwise airships disappear beyond the vanilla distance. It requires DH and Sodium client-side. The maximum distance is set by the gamerule `ssrdMaxRenderDistance` (128 by default) and respects the forced-loading cap. Known flaw: sub-levels are seen through the LODs with some shaders (no shader here).
- **Sound Physics Aeronautics** ([moddex](https://moddex.gg/mod/sound-physics-aeronautics)) can use DH data for the sound of distant propellers. **O**, very discreet and consistent with the art direction.
- Cost: server-side LOD generation adds to Chunky. Pregeneration (§6) also serves DH, since LODs are computed faster from existing chunks.

---

## 2. Sinytra Connector compatibility (Antique Atlas 4 + Surveyor)

- The Connector tickets on Antique Atlas and Surveyor are **all closed**: #1729 (Continuity/AA/Surveyor, "fixed in dev", February 2026), #1398, #1392, #1309 (blurry AA4 screen), #1554 (Farsight).
- Open tickets unrelated to the pack: MiniHUD, owo-lib/Jankson, Charm Forked. #2277 (Curios) actually comes from Bonded, absent from the pack ([#2277](https://github.com/Sinytra/Connector/issues/2277)).
- Sodium 0.6.13 already improved compatibility with Fabric mods loaded through Connector.
- Rules:
  1. **Pin** the Connector and FFAPI versions together; update only in pairs.
  2. **Add no other Fabric mod** through Connector: each mod added increases the risk. All the candidates above exist natively for NeoForge.
  3. `config/connector.json`: the `enableMixinSafeguard` protection exists (2.0.0-beta.17); leave it active.
  4. After each update, test as a priority the opening of the atlas, `/surveyor share` and the graves on the map.
- Uncertainty: Connector compatibility with ModernFix and Lithium is documented nowhere. It works in practice (the pack runs), but the startup log must be watched for mixin warnings.

---

## 3. Hiding coordinates and debug

1. **Gamerule `reducedDebugInfo true`.** F3 no longer shows XYZ, block, chunk or biome. From general knowledge, it also blocks F3+B (hitboxes) and F3+G (chunk borders); to check in game (minecraft.wiki inaccessible). It is stored in `level.dat` and applied at creation through the `load` function (§5.3).
2. **Leaks to plug, mod by mod:**
   - Supplementaries: coordinates on the compass, already disabled (decision 0006).
   - GraveStone: obituary to disable.
   - Jade: coordinates plugin to disable.
   - Distant Horizons: F3 lines (LOD position?) to check.
   - Antique Atlas 4 and Surveyor: coordinates displayed on hover? The README does not mention it, to check.
   - Aeronautics: an instrument that displays position (altimeter, navigation)? Altitude alone remains acceptable.
3. **No third-party "restricted F3" mod is needed**: the gamerule is enough. Nothing technically prevents a friend from installing a minimap client-side. This is a matter of the agreement between players: packwiz delivers the same list to everyone, and it is to be written in the README.
4. The vanilla **recovery compass** (towards the place of death) and the atlas remain the only orientation tools, which is consistent.

---

## 4. Cleanly removing the End and elytra (decision 0004)

Three-layer approach, without a dedicated mod ("dimension blocker" not verified and unnecessary):

1. **Worldgen, no more strongholds**: `pack/kubejs/data/minecraft/tags/worldgen/biome/has_structure/stronghold.json`:
   ```json
   { "replace": true, "values": [] }
   ```
   Possible duplicate: Sparse Structures' config can also neutralise `minecraft:stronghold`. Without a stronghold, the eye of Ender has no target.
2. **KubeJS rules (server)**:
   ```js
   // server_scripts/no_end.js
   ServerEvents.recipes(e => { e.remove({ output: 'minecraft:ender_eye' }); e.remove({ output: 'minecraft:end_crystal' }) })
   BlockEvents.rightClicked('minecraft:end_portal_frame', e => { if (e.item.id == 'minecraft:ender_eye') e.cancel() })
   // safety net: forbid any travel to the End (native NeoForge event)
   NativeEvents.onEvent('net.neoforged.neoforge.event.entity.EntityTravelToDimensionEvent', e => {
     if (String(e.getDimension().location()) == 'minecraft:the_end') e.setCanceled(true)
   })
   ```
   The availability of `NativeEvents` in KubeJS 2101 and the exact name of the event are to be confirmed in the .jar.
3. **LootJS**: remove `minecraft:elytra` and `minecraft:ender_eye` from all tables, to cover mod structures.
   ```js
   LootJS.modifiers(e => { e.addTableModifier(/.*/).removeLoot('minecraft:elytra').removeLoot('minecraft:ender_eye') })
   ```
   LootJS 3 syntax to check.
4. **Viewer**: hide elytra, eye of Ender, End blocks and shulkers through `RecipeViewerEvents` (KubeJS 2101 supports JEI, EMI and REI).

The dimension technically still exists, but remains inaccessible and is never generated. The dragon is never initialised.

---

## 5. Tooling, configs and dressing

### 5.1 Configs: `config/` or `defaultconfigs/`
- `config/`: client and common configs, global. Packwiz overwrites them at every update. Put in it only what is imposed on everyone.
- `defaultconfigs/`: templates of the **per-world server configs** (`<world>/serverconfig/*-server.toml`), copied **only at world creation** or if the file is missing. Examples: Create, GraveStone, OPAC, Power Loader, Ecliptic?
  - **Consequence**: for a permanent world that has already been created, any change to a server config must **also** be copied by hand (or by deployment script) into `world/serverconfig/`.
  - Plan a `scripts/deploy-serverconfig` that syncs `pack/defaultconfigs` to `world/serverconfig` before startup.
- `options.txt` and keys: through Default Options (sheet 31), not through packwiz.

### 5.2 Proposed KubeJS organisation
```
kubejs/startup_scripts/   registries (rare)
kubejs/server_scripts/    00_constants.js, no_end.js, recipes/*.js (per mod), loot/*.js, trades.js (MoreJS), spawns.js, seasons_coldsweat.js
kubejs/client_scripts/    recipe_viewer.js (hiding), tooltips.js
kubejs/data/fallow/       function/init.mcfunction, advancement/**, patchouli_books/**
kubejs/data/minecraft/    tags/worldgen/** (empty stronghold), structure_set (villages)
kubejs/assets/            textures/gui/title/background/panorama_*.png, texts/splashes.txt, patchouli_books (content)
```

### 5.3 Gamerules applied automatically
`kubejs/data/fallow/function/init.mcfunction`, plus `kubejs/data/minecraft/tags/function/load.json` → `["fallow:init"]`:
```
gamerule reducedDebugInfo true
gamerule doInsomnia false
gamerule doPatrolSpawning false
gamerule disableRaids true
gamerule doTraderSpawning false
gamerule playersSleepingPercentage 50
gamerule keepInventory false
gamerule spectatorsGenerateChunks false
gamerule announceAdvancements true
gamerule logAdminCommands true
```
- `doTraderSpawning false`: the wandering trader contradicts the "rare survivors". Re-enable it if MoreJS turns it into a survivor.
- `mobGriefing` stays `true`: sheep need to eat grass and villagers to farm.
- `naturalRegeneration` is to be arbitrated with the survival pillar.

### 5.4 Dressing without a mod
Menu panorama (NML mist screenshots) in `kubejs/assets/minecraft/textures/gui/title/background/panorama_0..5.png`, and `assets/minecraft/texts/splashes.txt` in French. FancyMenu later.

### 5.5 Packwiz server-side
- Startup script: `java -jar packwiz-installer-bootstrap.jar -g -s server https://<host>/pack/pack.toml`, then launch NeoForge.
  - The host can be a raw file of the GitHub release or GitHub Pages; a raw URL of the `main` branch would deliver unpublished changes.
  - Mods with `side = "client"` are ignored.
- **Audit the `side` field**: Crash Assistant, Mouse Tweaks, Controlling, Searchables, First-person Model, Not Enough Animations, Shoulder Surfing, FancyMenu and Default Options as `client`. Simple Voice Chat, Jade, AppleSkin and GraveStone as `both`. ProbeJS absent from the pack.

---

## 6. Proposed technical foundation

### 6.1 Proposed final list (in addition to those already selected)
| Status | Mods |
|---|---|
| **Foundation** | Neruina, Simple Backups, JEI, LootJS, KubeJS Create |
| **Recommended** | Create: Power Loader, Jade, AppleSkin, Mouse Tweaks, Controlling + Searchables, GraveStone Mod, Simple Voice Chat, Crash Assistant, Default Options, MoreJS, Almost Unified, ProbeJS (dev only) |
| **Optional** | EMI, Open Parties and Claims (without loaded chunks), Configured, Polymorph, Packet Fixer, Corpse (in place of GraveStone), First-person Model, Not Enough Animations, Shoulder Surfing Reloaded, FancyMenu + Drippy, Better Advancements, GuideME, radio addon for voice, Sound Physics Aeronautics, Distant Horizons + SSRD (dedicated branch) |
| **Ruled out** | FTB Backups 2/3, FTB Chunks, FTB Quests (CurseForge), Corail Tombstone, YIGD, ServerCore (for now), Open Loader / Global Packs, Catalogue, Inventory Profiles Next, Emotecraft, Better Third Person, Hold My Items, Akashic Tome, Carry On (#848, and it trivialises the logistics of expeditions and escort), Sodium Extra / Reese's Sodium Options (A6), Embeddium (official Sodium required by Sable) |

To be urgently reassessed among those selected: **Entity Culling** (A1).

### 6.2 `server.properties`
```
allow-flight=true                 # A2, essential for Aeronautics and contraptions
view-distance=10                  # the mist hides the distance anyway; 12 at most
simulation-distance=6             # Create/farm ticks around players; 8 if spark allows
white-list=true
enforce-whitelist=true
spawn-protection=0
enable-command-block=false
max-tick-time=120000              # tolerates generation spikes without killing the server; Neruina handles entities
sync-chunk-writes=true            # safety of a permanent world before performance
network-compression-threshold=256
difficulty=normal                 # vanilla hostiles removed elsewhere
entity-broadcast-range-percentage=100
```
- UDP port 24454 open for Simple Voice Chat.
- Server JVM: Java 21, `-Xms8G -Xmx8G` (10G with server-side DH), G1 with Aikar's flags, or `-XX:+UseZGC -XX:+ZGenerational` if the machine has at least 4 cores.
- Client: 6 GB, 8 GB with DH.

### 6.3 Pregeneration (Chunky, already selected)
- Before opening: `chunky world minecraft:overworld`, `chunky shape circle`, `chunky center 0 0`, `chunky radius 4000`, `chunky start`.
  - About 200,000 chunks: a few hours with NML and the structures, and 6 to 10 GB of disk (estimate).
  - Enable `continueOnRestart` in the Chunky config.
- Nether (if kept): radius of 1,000.
- Extend in steps (6,000, 8,000) as the frontier advances.
- World border: `worldborder set 50000` (radius of 25,000) to bound the disk and DH while remaining "immense".
- Run Chunky **after** the final setting of the seed and worldgen configs (NML, Sparse Structures, stronghold tag).

### 6.4 Backup plan (three levels)
1. **In-game (Simple Backups)**: modular every 60 min if players are connected, full every 24 h, 48 backups kept, on a local disk or volume separate from the world.
2. **Host (restic or borg, off-machine)**: every night `rcon save-off`, then `save-all flush`, snapshot of `world/`, `config/`, `defaultconfigs/` and `kubejs/`, then `save-on`. Retention: 7 daily, 4 weekly, 6 monthly, sent off-site (Backblaze, S3, NAS).
3. **Before each pack update** (merge into `main` and release): manual full backup labelled with the pack version. Rollback possible to the world + pack pair.
4. Monthly **restore test** on a local copy.

### 6.5 Restart
- A systemd unit (`Restart=on-failure`) is enough, with a daily restart at 6 am and RCON warnings at T-10 and T-1 min. No mod needed.
- Diagnostics: spark (`/spark profiler`, `/spark tickmonitor`, `/spark health`), already selected.

---

## 7. Uncertainties to check in the .jar files and at first launch
1. **Entity Culling**: exact names of the keys (`tickCulling`, `skipBlockEntityCulling`, `blockEntityWhitelist`) and effect on an assembled airship. If the bug remains, removal.
2. **Distant Horizons**: exact "Disable Vanilla Fog" key and distant fog options in `DistantHorizons.toml`; F3 lines; coexistence with ATMOSPHERICS.
3. **SSRD**: does its slider work with Sodium 0.8.13?
4. **Simple Backups**: key names and 1.21.1 NeoForge support on Modrinth.
5. **GraveStone**: obituary, ghost and owner keys in `gravestone-server.toml`; behaviour of a death on a Sable sub-level (where is the block placed?).
6. **Jade**: presence of a coordinates plugin and of a server-side override file.
7. **Power Loader**: NeoForge 1.21.1 version on Modrinth, server keys (limits, minimum rotation) and behaviour on a Sable sub-level.
8. **OPAC**: keys to limit claims and disable chunk loading.
9. **KubeJS 2101**: existence of `NativeEvents.onEvent` and handling of `kubejs/data` (worldgen tags) **from world creation** on a dedicated server. Otherwise, plan B: datapack in `world/datapacks/` before the first generation.
10. **LootJS 3 / MoreJS 1.21.1**: exact syntax and trade events.
11. **KubeJS Create**: Create 6.0.10 compatibility, Ponder support.
12. **Almost Unified**: `c:` tags actually shared between Farm & Charm and Kaleidoscope Cookery (tomato, lettuce, flour, dough) and between Overgeared and Create (metals).
13. **`reducedDebugInfo`**: effective blocking of F3+B and F3+G in 1.21.1; Antique Atlas and Surveyor display no coordinates.
14. **Sinytra Connector**: mixin warnings at startup with ModernFix and Lithium; Connector and FFAPI versions pinned together.
15. Modrinth 1.21.1 NeoForge availability, **not verified by search** (quota exhausted): Simple Backups, Packet Fixer, Polymorph, Default Options, Eating Animation, Better Advancements, FancyMenu and Drippy, Configured, radio addon. `packwiz mr add` will settle it.

---

## Sources
- Sodium 1.21.1 NeoForge, Aeronautics, Sable and Veil compatibility, Sodium Options API incompatibility: https://modrinth.com/mod/sodium/version/mc1.21.1-0.8.12-beta.1-neoforge · https://modrinth.com/mod/sodium/version/mc1.21.1-0.8.12-alpha.2-neoforge
- Sable and Sodium: https://github.com/Creators-of-Aeronautics/Simulated-Project/issues/1220 · Iris and Sodium 0.9: https://github.com/IrisShaders/Iris/issues/3243
- Entity Culling and Aeronautics/Sable: https://github.com/Creators-of-Aeronautics/Simulated-Project/issues/1347 · https://github.com/Creators-of-Aeronautics/Simulated-Project/issues/974 · https://github.com/ryanhcode/sable/issues/1414 · https://github.com/tr7zw/EntityCulling
- JEI and Aeronautics: https://github.com/Creators-of-Aeronautics/Simulated-Project/issues/1405 · list of incompatibilities: https://github.com/Creators-of-Aeronautics/Simulated-Project/issues?q=is%3Aissue+incompatible
- Create Aeronautics: Compatibility (Sable fixes): https://modrinth.com/mod/create-aeronautics-compatability
- Lithium NeoForge 1.21.1: https://modrinth.com/mod/lithium/version/mc1.21.1-0.15.0-neoforge
- Distant Horizons 2.3.0-b: https://modrinth.com/mod/distanthorizons/version/2.3.0-b-1.21.1 · SSRD: https://modrinth.com/mod/ssrd · Sound Physics Aeronautics: https://moddex.gg/mod/sound-physics-aeronautics
- Connector: https://github.com/Sinytra/Connector/issues?q=is%3Aissue+atlas · https://github.com/Sinytra/Connector/issues/2277
- Antique Atlas: https://github.com/Sisby-Folk/antique-atlas
- Neruina: https://github.com/Bawnorton/Neruina · FTB Backups 2 and 3: https://github.com/FTBTeam/FTB-Backups-2 · https://github.com/FTBTeam/FTB-Backups-3 · FTB Quests: https://github.com/FTBTeam/FTB-Quests
- OPAC: https://github.com/thexaero/open-parties-and-claims · Power Loader: https://github.com/hlysine/create_power_loader
- GraveStone: https://github.com/henkelmax/gravestone · Simple Voice Chat: https://github.com/henkelmax/simple-voice-chat
- EMI: https://github.com/emilyploszaj/emi · Jade: https://github.com/Snownee/Jade · Carry On: https://github.com/Tschipp/CarryOn · Crash Assistant: https://github.com/KostromDan/Crash-Assistant
- KubeJS 2101 (emi/jei/rei integrations): https://github.com/KubeJS-Mods/KubeJS/tree/2101/src/main/java/dev/latvian/mods/kubejs/integration · KubeJS Create: https://github.com/KubeJS-Mods/KubeJS-Create · LootJS: https://github.com/AlmostReliable/lootjs · MoreJS: https://github.com/AlmostReliable/morejs · Almost Unified: https://github.com/AlmostReliable/almostunified · ProbeJS: https://github.com/Prunoideae/ProbeJS · GuideME: https://github.com/AppliedEnergistics/GuideME
- Embeddium (end of development, official Sodium recommended): https://craftdownunder.co/guides/mods/embeddium
