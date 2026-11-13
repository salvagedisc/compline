# Decisions

One record per settled decision: the context, the options compared, the choice and its conditions. A record is not rewritten after the fact; if a decision changes, a new record replaces it and the old one moves to the status "superseded by NNNN".

| No. | Subject | Status |
|---|---|---|
| [0001](0001-nml-vs-terralith.md) | Worldgen: No Man's Land vs Terralith + Streams Reflowing | Superseded by 0011 |
| [0002](0002-agriculture.md) | Farming & animal husbandry | Farm & Charm, Ecliptic Seasons, KC-Automation kept; animal husbandry and preservation pending |
| [0003](0003-faune.md) | Fauna | Naturalist, Alex's Mobs Continued, Hybrid Aquatic, Mowzie's Mobs and companions kept |
| [0004](0004-structures.md) | Structures & villagers | Sparse Structures kept, End removed, "survivors" concept |
| [0005](0005-quark.md) | Quark: the essentials only | Config provided |
| [0006](0006-supplementaries.md) | Supplementaries: the essentials only | Config provided |
| [0007](0007-ambiance-performance.md) | Atmosphere & performance | Kept; Distant Horizons kept by 0011 |
| [0008](0008-enchantement.md) | Enchanting | Immersive Enchanting kept; Quark's Matrix as fallback |
| [0009](0009-xp.md) | Removal of experience | Kept; `xp` module of the `tilth` mod 0.2.0 |
| [0010](0010-musique.md) | Music | True Adaptive Music and the `tilth` pack kept; Reactive Music rejected |
| [0011](0011-tectonic-streams-distant-horizons.md) | Terrain and far view | Tectonic + Streams Reflowing; Distant Horizons hidden by the mist, `horizon` module; the "on NML" part superseded by 0019 |
| [0012](0012-sacs-piles-jours.md) | Bags, stack sizes and day length | Reliable Backpacks and Comforts, Sensible Stackables, Better Days 30/15; Ecliptic Seasons kept |
| [0013](0013-premiers-gestes.md) | First gestures: tools needed for logs and rock | `outils` module of the `tilth` mod and Overgeared's knapping; no new mod |
| [0014](0014-tannery.md) | Tannery mod instead of the scripted tannery | Tannery: Better Leather, hides for every leather animal, satchels removed |
| [0015](0015-textures.md) | Texture pack for the world | Vanilla Evolved as `tilth-textures`, without its sky, foliage colours and creatures; mod textures left as they are |
| [0016](0016-vent-et-lumiere.md) | Wind, light and sky | Where Winds Blow and Shine, no shader pack; Better Clouds removed, ATMOSPHERICS draws clouds, sun, moon and stars |
| [0017](0017-averse-et-rayons.md) | Rain, storm and sunlight | Seven client mods (rain sounds and drops, distant thunder, drips, footprints, waves), olive greens, `averse` and `rayons` modules of the `tilth` mod |
| [0018](0018-effets-en-pack-de-ressources.md) | Effects in the resource pack | Rain colour, lightning through the mist and sun rays move into `tilth-ambiance` (core shaders, Polytone post shader); `tilth` 0.6.3 keeps what needs new geometry or game data |
| [0019](0019-leaving-no-mans-land.md) | Leaving No Man's Land | Tectonic + Regions Unexplored + Geophilic kept (Lithosphere compared and set aside on 2026-11-13), world of radius 6,000; not played |
| [0020](0020-clearings-keep-their-biome.md) | Clearings keep their biome | A clearing no longer replaces the biome: the server syncs a list of discs, the client shows the mist lifted and the vivid colours; climate, seasons, crops and fauna stay those of the place (`tilth` 0.8.0) |
| [0021](0021-spelunkery-and-dinosaurs.md) | Spelunkery and Archie's Dinosaurs | Both join the pack; the Nether and End dinosaurs move to cave biomes, Spelunkery is sorted like the other mods, the streams keep their thaw, the dinosaurs' library is kept as a jar |
| [0023](0023-rename-to-tilth.md) | The pack is renamed Tilth | `fallow` was taken on Modrinth: pack, custom mod, namespaces, resource packs, logo and repository become Tilth / `tilth`; worlds made before lose the mod's blocks |

## Template

```markdown
# NNNN — Subject

- **Date**: YYYY-MM-DD
- **Status**: kept / under test on `test/<subject>` / pending / superseded by NNNN

## Context
Why the question arises, what the vision requires.

## Options
| Criterion | Option A | Option B |
|---|---|---|

## Decision
The choice, and the conditions that go with it (configs, KubeJS scripts, settings).

## Rejected
- Mod: reason.
```
