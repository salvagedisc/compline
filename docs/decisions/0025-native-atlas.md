# 0025 — The atlas runs natively, without Connector

- **Date**: 2026-11-13
- **Status**: kept (the team's choice of 2026-11-13); tried with a test client connected to a dedicated server, both on the full pack; not played

## Context
Antique Atlas 4, Surveyor and Antique Transport only exist for Fabric. Their authors target NeoForge through Sinytra Connector and Forgified Fabric API, and no native port exists. Connector was the pack's last beta core mod: its 1.21.1 branch has had no release since 2026-08-15, and Moonlight (base of Supplementaries, Amendments, Every Compat and Polytone) warns at start-up that Fabric API "can easily lead to poor compatibility and other bizarre issues".

The three mods are small (about 17,000 lines of Java, 12 mixins) and LGPL-licensed, with light use of Fabric API (networking, events, commands, reload listeners, one key mapping).

## Options
| Criterion | A. Keep Connector | B. Map Atlases | C. Port the three mods |
|---|---|---|---|
| The hand-drawn atlas | Kept | Replaced by vanilla-style maps | Kept, same saves and resource packs |
| Connector in the pack | Yes (beta, no 1.21.1 update) | No | No |
| Work | None | Configuration | About two sessions, then rare upstream merges |
| Obligation | None | None | LGPL: publish the port's source |

The team refused Map Atlases. Remnants Minimap and Mapwright, the native maps found, change what the map is (Elden Ring theme, or a map the player draws).

## Decision
Option C. The ports live in their own public repository, `salvagedisc/fallow-atlas`, as the LGPL requires; the jars are committed in `pack/mods/` like the `fallow` jar.

- Surveyor 1.2.4, Antique Atlas 3.1.2 and Antique Transport 1.3.0, version suffix `-neoforge.1`.
- Mod ids, packets, configs and save formats are unchanged, so maps and waypoints explored before the port load as they are (seen on the server with data written by the Fabric builds; not tried with a client's own save).
- Connector is removed. **Forgified Fabric API stays**: Puddles & Floods is a NeoForge mod but declares it as a required dependency and calls `FabricLoader` to detect Iris. Moonlight's warning remains until Puddles & Floods drops that dependency.
- Antique Transport's ship teleport is removed, button, packet and server receivers (the team's choice of 2026-11-14). Upstream registered the packet twice, and the receiver that took effect let any player teleport.
- The atlas book is recognised by its name, "Antique Atlas", whatever the language: the French strings keep that name ("Atlas ancien" made the renaming hint wrong). Punchy leaves `minecraft:book` to the vanilla hand, otherwise the held atlas is drawn as a plain item instead of the open map.

## Consequences
- Upstream updates are merged by hand into `fallow-atlas`. Antique Atlas was last released in January 2026, Surveyor in May.
- Seen on the test bench: the client joins a dedicated server, terrain syncs, the atlas opens with the M key, a book renamed "Antique Atlas" shows the map when held, the client map is saved on disconnect.
- Still to check in game: structures are discovered, markers and waypoints sync between players, Create trains and Sable ships show.
