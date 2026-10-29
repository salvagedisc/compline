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
| [0009](0009-xp.md) | Removal of experience | Kept; `xp` module of the `fallow` mod 0.2.0 |
| [0010](0010-musique.md) | Music | True Adaptive Music and the `fallow` pack kept; Reactive Music rejected |
| [0011](0011-tectonic-streams-distant-horizons.md) | Terrain and far view | Tectonic + Streams Reflowing on NML; Distant Horizons hidden by the mist, `horizon` module |
| [0012](0012-sacs-piles-jours.md) | Bags, stack sizes and day length | Reliable Backpacks and Comforts, Sensible Stackables, Better Days 30/15; Ecliptic Seasons kept |
| [0013](0013-vent-et-lumiere.md) | Wind, light and sky | Where Winds Blow and Shine, no shader pack; Better Clouds removed, ATMOSPHERICS draws clouds, sun, moon and stars |

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
