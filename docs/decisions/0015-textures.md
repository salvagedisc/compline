# 0015 — Texture pack for the world

- **Date**: 2026-10-29
- **Status**: kept (team's choice of 2026-10-29); nothing seen in game yet

## Context
The team was not convinced by the pack's own resource packs, which only dress the interface and the atmosphere, and asked for a full texture pack (blocks and items) that fits the pack's style and whose licence allows changing it, and whether the blocks of every mod could then be adapted to it. Realistic textures and shaders were already refused, and the moon stays vanilla. The candidates were compared with previews rendered from their 1.21.1 files.

## Options
| Pack | Licence | Fit | Coverage in 1.21.1 |
|---|---|---|---|
| Pixel Perfection Fidelity (+ Legacy) | CC BY-SA 4.0 | Desaturated, medieval, closest to the mist | Full |
| **Vanilla Evolved** | CC BY-NC-SA 4.0 | Vanilla with more relief and some 3D models, by the artist of GUI Revision | 671 blocks, 257 items; the rest stays vanilla |
| Spectral | CC BY-NC-SA 4.0 | Warm, redrawn from scratch | Unfinished, almost no items |
| Soartex Grove | CC BY-NC-SA 4.0 | Smooth 64x, little detail | Full blocks, clashes with 16x mod textures |
| Faithful 32x | Faithful licence v4 | Vanilla at twice the resolution | Full, 32x |

No pack covers Create, Supplementaries, Twigs, Handcrafted, Chipped or No Man's Land. Mizuno's 16 Craft, Whimscape, Bare Bones, Excalibur, Stay True, Ovo's Rustic Redemption, Dokucraft, Jolicraft and Soartex Fanver were set aside for their licences.

## Decision
**Vanilla Evolved**, as the `fallow-textures` pack ([page](../plan/fallow-textures.md)): built by `scripts/textures/build.py` from the pinned 1.17.0 release, without the sky, the foliage colour map, the creature textures, the language file and the OptiFine files; enabled on first launch under every other pack of ours. Same licence and same artist as `fallow-interface`.

Follow-ups, in this order: a `teinte` module of the `fallow` mod that recolours the mods' textures at load time on each player's computer; hand redraws of the most visible mod textures (No Man's Land, Create, Fallow's fires and lanterns).

## Rejected
- Pixel Perfection Fidelity: recommended in the comparison, not chosen by the team.
- Spectral, Soartex Grove, Faithful 32x: unfinished, or a style or resolution that clashes with the mist and the 16x mods.
- Packs under closed licences: a modified copy cannot ship in the pack.
