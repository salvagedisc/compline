# 6. Technical foundation, comfort and server

> **History (plan v1, 2026-10-10).** Superseded by chapters [10](10-recentrage.md) to [12](12-verification.md), the [registry](../../mods.md) and the [decisions](../../decisions/README.md): several mods listed here have since been dropped. Kept for its reasoning.

What already exists (0007): Sodium 0.8.13, ImmediatelyFast, Entity Culling, ModernFix, FerriteCore, Lithium, Clumps, Structure Layout Optimizer, Chunky, spark; Sinytra Connector + Forgified Fabric API for Antique Atlas and Surveyor. This chapter adds the **stability of a permanent world**, **comfort compatible with the vision**, the **KubeJS tooling** and the **server configuration**.

Detail, evidence and keys: [recherche/research-technique.md](recherche/research-technique.md).

## 6.1 Alerts

1. **Entity Culling breaks Aeronautics/Sable contraptions.** Three open tickets with no reply ([Simulated-Project#1347](https://github.com/Creators-of-Aeronautics/Simulated-Project/issues/1347), [#974](https://github.com/Creators-of-Aeronautics/Simulated-Project/issues/974), [sable#1414](https://github.com/ryanhcode/sable/issues/1414)): the block-entity blocks of an assembled airship become unusable, and propeller blades shake. To test first: `tickCulling: false` and `skipBlockEntityCulling: true` in `config/entityculling.json`. If the bug persists, **remove Entity Culling**: it is client-only, so removal is risk-free.
2. **`allow-flight=true` is essential** in `server.properties`. Without it, a player standing on an airship or a moving contraption is kicked.
3. **Aeronautics must stay at 1.3.2 or later**: before that, a recent JEI makes it crash ([#1405](https://github.com/Creators-of-Aeronautics/Simulated-Project/issues/1405)).
4. **Blacklist** (incompatible with Aeronautics): Does It Tick, Flowing Fluids, Leaves Be Gone, Flashback, Tpsum, Refurbished Furniture (energy), Carry On.
5. **Sodium 0.8.x rejects Sodium Options API**: no Sodium Extra, no Reese's Sodium Options, and no mod that depends on them.
6. **Sinytra Connector**: pin Connector and FFAPI together, update them as a pair, and add **no Fabric mod outside the Surveyor family** (AA4 Atlas and Antique Trains are published for NeoForge by their authors and go through Connector like Antique Atlas). The Connector tickets on Antique Atlas and Surveyor are all closed. After each update, test the atlas, `/surveyor share` and the graves on the map. Dead Reckoning, Fabric only, is replaced by GUI Compass, which is native ([2](02-exploration.md)).

## 6.2 Proposed mods

| Mod | Role | Side | Availability | Proposed status |
|---|---|---|---|---|
| **Neruina** | Pauses an entity that crashes on every tick instead of bringing the server down | CS | yes | core |
| **Simple Backups** | Scheduled backups, full and differential | S | yes | core (fallback: host script only) |
| **JEI** | Recipe viewer; native plugin in Create, Aeronautics, Overgeared, KC, F&C; tested by the Aeronautics team | CS | yes | core |
| **In Control!** | Spawn rules ([5.2](05-faune-et-ambiance.md#52-controlling-modded-creatures)) | S | yes (10.2.6) | core |
| **LootJS** | Remove elytra and eyes of Ender from all loot; ruin loot | CS | yes | core |
| **KubeJS Create** | Create recipes in KubeJS: the Overgeared → Create → KC link | CS | yes | core |
| **MoreJS** | Villager trade events (survivor rules) | CS | 1.21.1 not confirmed | recommended; fallback `NativeEvents` |
| **Almost Unified** | Unifies duplicates through `c:` tags: F&C / KC ingredients (KC priority), Overgeared / Create metals | CS | yes | recommended |
| **Jade** | Block name, required tool, machine state | CS | yes | recommended (coordinates and mob health plugins disabled) |
| **AppleSkin** | Hunger and saturation preview: compare KC dishes | CS | yes | recommended |
| **Mouse Tweaks** | Mouse drag-and-drop, complements Quark | C | yes | recommended |
| **Controlling** + **Searchables** | Key binding search and conflicts | C | yes | recommended |
| **GraveStone Mod** | Grave instead of death; Antique Atlas already marks graves on the map | CS | yes | recommended (obituary and ghost disabled) |
| **Simple Voice Chat** | Proximity voice (UDP port 24454) | CS | yes | recommended |
| **Crash Assistant** | Help window after a crash, log upload, built-in graphics card alert | C | yes | recommended |
| **Default Options** | Book of default settings and key bindings without overwriting players' own | C | yes | recommended |
| **ProbeJS** | KubeJS autocompletion in VSCode | dev | — | in the test instance only, not in `pack/` |

**Optional**: EMI (with JEI), Polymorph (only if a recipe conflict resists KubeJS), Packet Fixer (at the first "Payload too large"), First-person Model, Not Enough Animations, Shoulder Surfing Reloaded, FancyMenu + Drippy Loading Screen (theming, later), Better Advancements, Sound Physics Aeronautics, a "radio" addon for Simple Voice Chat (to be found: long-distance communication as a craftable object).

**Rejected**:
- FTB Backups 3, FTB Quests: CurseForge only.
- FTB Chunks: keeps a full-screen map.
- Corail Tombstone: magic and teleportation; YIGD: Fabric-oriented.
- ServerCore: its *activation range* risks breaking Create.
- Open Loader, Global Packs: KubeJS already loads `kubejs/data` and `kubejs/assets`.
- Catalogue, Inventory Profiles Next (duplicate of Quark sorting), Emotecraft (duplicate of Quark emotes), Better Third Person and Hold My Items (unverified), Carry On (incompatible with Aeronautics), Sodium Extra (alert 5), Embeddium (Sable requires Sodium).

## 6.3 Comfort without breaking the vision

| Setting | Reason |
|---|---|
| Jade: coordinates plugin locked to off, mob health disabled, `displayMode` LITE or hold a key | No coordinates, little combat |
| GraveStone: obituary (it displays coordinates) and ghost disabled; grave breakable by its owner | No coordinates |
| Simple Voice Chat: `max_voice_distance` 32 to 40, whisper 12 to 16, **groups disabled** | Players hear each other up close; long-distance communication will go through an object |
| Default Options: render distance, Sodium's "Improved Transparency" disabled (for ATMOSPHERICS), key bindings deduplicated | Consistent settings from the first launch |
| Almost Unified: Kaleidoscope Cookery priority over Farm & Charm for tomato, lettuce, flour, dough | Condition of decision 0002 |

## 6.4 Distant Horizons

DH stays on its branch, but the research removes the main worry: **DH and the mist go well together**. With a dense exponential DH fog and a moderate radius, the LODs become **washed-out silhouettes** in the distance, exactly the art direction.

Settings on `test/distant-horizons`:
1. **Disable "Disable Vanilla Fog"** (enabled by default): otherwise the ATMOSPHERICS mist disappears.
2. Exponential DH fog, starting early.
3. LOD radius of 64 to 128 chunks: beyond that, the mist hides everything and the cost explodes.
4. **Separate Sable Render Distance** becomes mandatory: without it, airships disappear beyond the vanilla distance (gamerule `ssrdMaxRenderDistance`).
5. Memory: 10 GB server, 8 GB client.

## 6.5 Server configuration

**`server.properties`**

```properties
allow-flight=true            # essential for Aeronautics
view-distance=10             # the mist hides the distance; 12 at most
simulation-distance=6        # 8 if spark allows it
white-list=true
enforce-whitelist=true
spawn-protection=0
enable-command-block=false
max-tick-time=120000         # tolerates generation spikes; Neruina handles entities
sync-chunk-writes=true       # safety for a permanent world
difficulty=normal
```

**Gamerules** (function `tilth:init` run on load, see [7](07-kubejs-et-datapack.md)):
`reducedDebugInfo true`, `doInsomnia false`, `doPatrolSpawning false`, `disableRaids true`, `doWardenSpawning false`, `doTraderSpawning false`, `playersSleepingPercentage 50`, `keepInventory false`, `spectatorsGenerateChunks false`, `announceAdvancements true`, `logAdminCommands true`. `mobGriefing` stays `true` (sheep, villagers). `naturalRegeneration` is to be settled together with survival ([4](04-vie-quotidienne.md)).

**Memory**: Java 21, 8 GB for the server (10 GB with DH), 6 to 8 GB per client. G1 with Aikar's flags, or generational ZGC on a machine with at least 4 cores.

**Pregeneration (Chunky)**: only after the final seed and worldgen settings (NML, Streams, Sparse Structures, stronghold tag). Circle with a 4,000-block radius, `continueOnRestart` enabled, then steps of 6,000 and 8,000 as the border advances. World border at 50,000 (radius 25,000) to bound disk usage.

**Backups, three levels**:
1. **In game** (Simple Backups): differential every hour if players are connected, full every day, 48 kept, on a disk separate from the world.
2. **Host** (restic or borg): every night `save-off`, `save-all flush`, snapshot of `world/`, `config/`, `defaultconfigs/`, `kubejs/`, then `save-on`. Retention of 7 daily, 4 weekly, 6 monthly, sent off the machine.
3. **Before each pack release**: full backup labeled with the pack version. Restore test once a month.

**Restart**: systemd unit (`Restart=on-failure`) with a daily restart and RCON warnings at 10 min and 1 min. No mod needed.

**Installation**: `packwiz-installer-bootstrap -g -s server` pointed at the `pack.toml` of a **release**, not at `main`. Mods with `side = "client"` are ignored on the server side: check the `side` field of each new client mod (Crash Assistant, Mouse Tweaks, Controlling, Searchables, Default Options, Cosy Critters…).

**Server configs of a permanent world**: `defaultconfigs/` is copied into the world **only when it is created**. For a world that already exists, a small script (`scripts/deploy-serverconfig`) is needed that copies `pack/defaultconfigs` to `world/serverconfig` before each start. This is an addition to the repository tooling.

## 6.6 Config organization

- `pack/config/`: client and common configs, imposed on everyone, overwritten at each pack update.
- `pack/defaultconfigs/`: per-world server configs (Create, GraveStone, OPAC, Power Loader…).
- `options.txt` and key bindings: via Default Options, never versioned directly.
