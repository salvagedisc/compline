# 0020 — Clearings keep their biome

- **Date**: 2026-11-13
- **Status**: kept (the team's request of 2026-11-13); seen in a headless test client with the full pack, not played

## Context
Until `compline` 0.7.0 a vigil fire, a clearing lantern or a dissipator replaced the biome of its disc with one of three clearing biomes (`compline:clairiere`, `clairiere_froide`, `clairiere_chaude`). That was the simplest way to lift the mist: ATMOSPHERICS, Polytone and True Adaptive Music all decide from the biome.

The same is true of everything else. A clearing biome has no tags and its own climate, so inside a clearing:
- Ecliptic Seasons classed the ground by `downfall` alone: other rain, other crop humidity than ten blocks away;
- Cold Sweat fell back on its default temperature;
- the fauna was the clearing's own list (farm animals and salmon), not the biome's;
- Antique Atlas had no tile for it, `/locate biome` and the biome tags of every mod no longer matched.

Players live in clearings. The place where they farm, breed and build was the one place where the rules of the biome they had chosen stopped applying. The team asked on 2026-11-13 that every aspect of a biome stays the same in a clearing.

## Options
| Criterion | A. Keep replacing the biome, add tags and one clearing biome per climate | B. Keep the real biome, show the clearing on the client |
|---|---|---|
| Climate, seasons, crops, fauna | Approximated by 9 or more clearing biomes; every mod rule to copy by hand | Exactly those of the biome, nothing to copy |
| New biomes or mods later | Each one needs its clearing variant and its tags | Nothing to do |
| World data | Biome cells rewritten, restored from the seed on removal | Untouched |
| Code | Server conversion, ring by ring | A synced list of discs, five client mixins |
| Risk | Silent gaps (a rule nobody copied) | A mixin target renamed by an update switches off part of the look |

## Decision
Option B, in `compline` 0.8.0 ([module](../plan/mod-compline.md#clairiere-module)).

- The server keeps the list of clearings and sends it to the clients. The biome is never changed.
- The three clearing biomes stay in the registry as **looks**. On the client, inside a disc, the mist (ATMOSPHERICS), the music (True Adaptive Music) and the base grass, foliage and water colours are those of the look that matches the temperature of the real biome. Seasonal tints still come from the real biome.
- One game rule is kept from the old clearing biomes, because it is what makes a clearing a shelter: monsters do not spawn naturally inside.
- Only the biomes of `compline:peut_devenir_clairiere` clear, as before.

## Consequences
- Crops, seasons, temperature, fauna, atlas tiles and every biome tag behave in a clearing as in the rest of the biome. The farm animals that used to appear in any clearing no longer do: the animals are those of the place.
- The transition (ring by ring, cloud puffs, slow fade of the mist) is now computed on each client from the opening time; nothing is lost when the server stops in the middle of it.
- Seen from far away through Distant Horizons, a clearing has the colours of its biome: the far terrain is coloured per biome, not per position.
- A world made before 0.8.0 keeps its clearing cells until its sources are broken or `/compline brume` is run over them; both give the cells back to the generator's biome.
- The hooks into ATMOSPHERICS, True Adaptive Music and Sodium all use `require = 0`. After an update of one of them, tests K1, K2 and K6 say whether its part of the look still works.

## Rejected
- Option A: it was the first proposal (nine clearing biomes by climate). It only moves the problem: every rule a mod attaches to a biome would have to be copied, and the next mod would break it again without any error.
- Replacing the three `BiomeColors` colour rules: Ecliptic Seasons replaces two of them at startup and Polytone restores them at every resource reload, so the clearing colours were lost (seen in the test client). The hook sits on the calls instead.
