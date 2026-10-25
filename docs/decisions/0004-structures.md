# 0004 — Structures & villagers

- **Date**: 2026-09-24
- **Status**: Sparse Structures kept (tested on `test/structures`, merged into `main` on 2026-10-10); datapack/KubeJS implementation and a Create structures mod under research

## Principle
A structure is a discovery ("wait… what is that?"), not repetitive scenery. There still needs to be enough diversity to feel that one is always discovering something.

## Decisions
- **End removed** entirely (dimension, portals, Elytra), **strongholds removed** as a consequence. Implementation: datapack, sub-project 3.
- **Sparse Structures ✅**: global spacing of all structures (vanilla and modded), adjustable per structure.
- **No Man's Land** already provides atmospheric structures (menhirs, ruins, sanctuaries, wells, monuments).
- **Rejected**: YUNG's Better *, Structory, Structory: Towers (not to the maintainer's taste), Explorify, Moog's Voyager, Trek, Adorabuild (volume), When Dungeons Arise, Dungeons and Taverns (RPG), locator compasses, Waystones.

## Under research
- A mod that **replaces all vanilla structures with Create-integrated versions** (mentioned by the maintainer, not found on Modrinth as of 2026-09-24).
- Create candidates identified in the meantime: Create: Let The Adventure Begin, Create Aeronautics Structures, Create: Structures Arise, Create Rustic Structures.

## Villagers: the "survivors" concept ✅
1. No intact village: vanilla villages become abandoned ruins (force the "zombie village" variant, empty without zombies, or remove the villagers).
2. Rare, isolated survivors (hermit, engineer, camp of 2-3 refugees) to discover.
3. The player brings them back to their settlement (leash, boat, train, airship): they become the population.
4. Every villager is precious: breeding blocked or very slow, trades filtered in KubeJS (no Mending, no bypassing Overgeared).

Implementation: survivor structures in datapack + KubeJS rules (sub-project 3).

## Consequence of removing vanilla hostiles
Ocean monuments, witch huts and outposts become empty: to be removed, repopulated with our creatures, or kept with a few vanilla mobs locally (sub-project 3).
