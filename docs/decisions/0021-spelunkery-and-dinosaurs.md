# 0021 — Spelunkery and Archie's Dinosaurs

- **Date**: 2026-11-13
- **Status**: kept (the team's request of 2026-11-13); started on the test server and in a headless test client, not played

## Context
The team asked for more life and more to do underground, and for dinosaurs, on the terrain chosen in [0019](0019-leaving-no-mans-land.md): Spelunkery and Archie's Dinosaurs. Both were written for a world with a Nether and an End, with experience and without Regions Unexplored. In Fallow the End is gone and the Nether is closed ([0004](0004-structures.md)), there is no experience ([0009](0009-xp.md)), nothing shows a position in numbers or locates things for the player ([0006](0006-supplementaries.md), [pack sort](../plan/tri-du-pack.md)).

Seen on the test server with both mods on top of the pack:
- Spelunkery rewrites `IceBlock.melt`. Streams Reflowing hooks into the same method to thaw a frozen stream back into stream water, and Mixin refused its hook: a stream frozen by winter would have thawed into still water.
- Two of the six dinosaurs live in closed dimensions (Obsidiceratops in the crimson forest, Chorusoatl in the End) and the Lushsaur only in lush caves, which Tectonic makes rare.
- Archie's Dinosaurs depends on Astemir Re-Lib, whose author publishes snapshot builds several times a day and withdraws the previous ones from Modrinth within a day: a pack file pointing at one of them stops downloading.
- Spelunkery 0.4.4 ships its ore drops as plain data: its `enable_rough_gems`, `enable_raw_nuggets` and `enable_mores` settings do nothing in this version (111 "Failure adding generated resources" lines at each start, harmless).

## Decision
Both mods join the pack, with these adjustments.

**Spelunkery** (0.4.4)
- Kept as the mod makes it: stone-variant ores, rough gems to polish at the grindstone, raw nuggets, salt, nephrite as a stone, cave mushrooms, sluices and channels, wooden rails, rope ladder, glowsticks, hammer and chisel, item magnet, compactor, parachute, Mine-o-mite.
- Sorted out, like every other mod ([pack sort](../plan/tri-du-pack.md#spelunkery-and-archies-dinosaurs)): the stored experience (nephrite charm, carved nephrite, siphon, fountain, diode), the depth gauge and the magnetic compass (height and position in numbers), the tuning fork and the echo fork (they locate geodes and creatures), what needs the Nether, the End or netherite, the ores and nuggets of metals the pack does not have.
- `slimes_spawn_in_all_caves = false`: slimes keep their vanilla places. Provisional, to be confirmed by the team.
- `fallow` 0.9.1 gives the streams their thaw back ([compatibility fix](../plan/mod-fallow.md#streams-reflowing-and-spelunkery-the-ice-of-a-stream)).
- Its biome lists are extended to Regions Unexplored by `biome_rules.py`, like those of the other mods.

**Archie's Dinosaurs** (1.0.4)
- The Lushsaur also lives in the bioshroom caves.
- The Obsidiceratops moves to the redstone caves: its structure is placed like the Lushsaur's (`underground` instead of `in_nether`, which finds no place in the Overworld).
- The Chorusoatl moves to the prismachasm: its spawn tag and the blocks it accepts under its feet. Its egg already hatches outside the End, in full darkness.
- Therimoss (jungles), Coralosaur (warm ocean, and Hybrid Aquatic's coral reef), Sculkraptor (deep dark), the amber craters and fossils and the trader Archie are left as the mod makes them. Sparse Structures already spreads the craters by two.
- Only the netherite claws are sorted out.
- Astemir Re-Lib 1.2.985 is committed as a jar in `pack/mods/` (MIT licence, in the jar). `npm run check-deps` reads the jars of that folder too.

## Consequences
- Archie's Dinosaurs is young (first published on 2026-10-28), All Rights Reserved, without public source or issue tracker, and its library moves every day. An update of the mod may ask for a newer library: update both together, by hand. If the mod is abandoned it leaves the pack with its blocks; this has to be settled before the permanent world starts.
- The three moved dinosaurs live in places the mod did not plan. Seen on the test server: the Lushsaur and the Obsidiceratops are generated in their caves. Not seen: the natural spawn of the Chorusoatl in the prismachasm, and how any of them behaves there ([tests](../tests-en-jeu.md)).
- Sulfur has no source, so Spelunkery's gunpowder recipe is removed; saltpeter stays a desert stone.
- Diamonds, emeralds, lapis and redstone now come out rough and are polished at a grindstone; Create's crushing wheels still give the finished gem.

## Rejected
- Opening the Nether for two dinosaurs and for sulfur: it would undo decision 0004.
- Cutting the Obsidiceratops and the Chorusoatl like the Nether and End creatures of Alex's Mobs: the team asked for this mod, and both fit a cave biome that had no large creature.
- Astemir Re-Lib through packwiz like the other mods: the file disappears from Modrinth within a day.
- Moving Spelunkery's Nether geodes and sulfur into the Overworld: Create already gives quartz and netherrack, and the sulfur patch only replaces soul sand.
