# 0019 — Leaving No Man's Land: Tectonic + Regions Unexplored + Geophilic

- **Date**: 2026-11-12
- **Status**: kept (the team's choice); not seen in game, measured on one seed over one square kilometre per candidate
- **Supersedes**: the "on No Man's Land" part of [0011](0011-tectonic-streams-distant-horizons.md) (Distant Horizons and the `horizon` module stay); [0001](0001-nml-vs-terralith.md) was already superseded by 0011

## Context
On 2026-11-11 the team decided to leave No Man's Land (NML). It asked for varied biomes, clean transitions between them and no floating blocks.

One world per candidate was created on a test server with the same seed and measured on the same square kilometre. A region-file scanner counted three things:
- visible defects: visible detached clusters (floating islands, trees, isolated blocks);
- distinct biomes;
- plants without support: plants resting on air.

Limits of the measure: one seed, one square kilometre per candidate, and nobody has walked these worlds in game. The numbers say which candidates produce fewer floating blocks and how many biomes they offer. They say nothing of how the land feels to cross.

## Options
Visible defects / distinct biomes / plants without support.

| Candidate | Visible defects | Distinct biomes | Plants without support | Note |
|---|---|---|---|---|
| Tectonic alone | 8 | 14 | 0 | |
| Tectonic + Terralith | 95 | 27 | 10 | 95 visible defects against 7 or 8 for Tectonic alone |
| Terralith alone | 3 | 22 | 16 | 47 % water |
| Tectonic + Biomes O' Plenty | 18 | 25 | 71 | |
| Tectonic + Oh The Biomes We've Gone | 5 | 16 | 14 | |
| Tectonic + Nature's Spirit | 9 | 25 | 0 | |
| Tectonic + Geophilic | 8 | 14 | 0 | Same figures as Tectonic alone |
| Tectonic + Regions Unexplored | 7 | 21 | 0 | |
| **Tectonic + Regions Unexplored + Geophilic (kept)** | 7 | 21 | 0 | |
| Lithosphere alone | 1 | 8 | 10 | 330 detached rock clusters in caves, against about 25 for the Tectonic worlds |

Lithosphere under the same biomes measured 0 visible defects on 2026-11-13. It was not part of the choice of 2026-11-12.

## Decision
**Tectonic 3.0.28 + Regions Unexplored 0.6.3 + Geophilic 3.7, with Streams Reflowing.** The team chose this combination on 2026-11-12.
- Tectonic (already in the pack) keeps the relief.
- Regions Unexplored (`regions-unexplored`, MIT): biomes, new woods and plants, cave biomes. It requires Lithostitched, already in the pack.
- Geophilic (`geophilic`): vanilla biomes reworked, no new block.
- Streams Reflowing stays.
- Side of both new mods: client and server (CS).
- 156 Modrinth mods (155 before: one removed, two added) plus the custom `tilth` mod 0.7.0.

Consequences, all applied with the change:
1. **The Friendly Moon is gone.** It was an NML feature and the pack's main thread. Removed: `pack/kubejs/server_scripts/lune.js` (one moon at a time, the crumpled note in the alchemists' jars), the `tilth.lune.*` texts, the Friendly Moon parts of the `ciel` module of the `tilth` mod (`FriendMoonRelay`, mixins `ciel.FriendMoonSkyMixin` and `ciel.AtmosphericsDreamMixin`, config key `friendMoon`) and the compatibility mixin `compat.InvertedBellMessageMixin`. **What replaces the main thread is not decided.**
2. **`tilth` 0.6.3 → 0.7.0** (`pack/mods/tilth-0.7.0.jar`).
3. **Recipes of the mod**, with vanilla ingredients (provisional, to be confirmed by the team):
   - Vigil Fire `tilth:feu_de_veille`: a honeycomb, two smooth stone slabs, a charcoal, a smooth stone (was: NML resin oil bottle, polished stone slabs, polished stone);
   - Clearing Lantern `tilth:lanterne_de_clairiere`: two Overgeared iron plates, two glass panes, an amethyst shard (was: NML quartzite cluster).
4. **Tags of the mod.** `tilth:peut_devenir_clairiere` lists 59 Regions Unexplored surface biomes (not beaches, deserts, oceans, caves or the Nether) instead of NML's. `tilth:sans_outil` has `regions_unexplored:cattail` instead of NML's plants.
5. **Mist.** The ATMOSPHERICS preset becomes `tilth_v6.json` (was `tilth_v5.json`; same global settings, NML's 24 biome profiles removed, 78 Regions Unexplored profiles added). Each Regions Unexplored biome copies the profile of the vanilla biome it resembles (table `MODELS` of the new manual script `scripts/ambiance/biomes.py`; usage `python3 scripts/ambiance/biomes.py <regions-unexplored jar>`). The same script writes the Polytone colour modifiers `ru_<biome>.json` (65 files replacing the 20 `nml_*.json`): grass and foliage saturation × 0.72 and brightness × 0.95 for the misty family, × 0.85 and × 0.98 for the veiled one; water saturation × 0.80 only where the biome sets its own water colour; snowflake particles in snowy biomes. It also writes the Regions Unexplored part of the `tilth:brumeux` tag (15 vanilla + 34 Regions Unexplored misty biomes = 49). The values are paper choices, not seen in game. The NML fog config `pack/config/nomansland-client.toml` is gone, like `nomansland-common.toml` and `mixed_litter-startup.toml`.
6. **Coloured lights** (Polytone): the moonlight file (NML moonlight candle and basin) is removed; fire and soul lights now target Supplementaries sconces.
7. **Supplementaries sconces** are enabled again (they were off because NML had its own).
8. **Twigs cobblestone bricks** (and mossy, cracked, slabs, stairs, walls) are back: they had been removed as duplicates of NML's.
9. **Music.** The four NML tracks (anachronistic, drifter, guidance, nomad) leave the music pack (`sounds.json` events and `rules.json` playlists). The "underground" situation keeps one track (infinite_amethyst); the "mist" one keeps hymn_to_the_dawn and long_note_four.
10. **Life shards** (`pack/kubejs/server_scripts/sante_butin.js`): the NML loot tables are replaced by vanilla ones of the same order of magnitude (provisional, paper values). Chests: `minecraft:chests/simple_dungeon` 10 %, `jungle_temple` 25 %, `desert_pyramid` 5 %, `shipwreck_treasure` 15 %, `shipwreck_supply` 5 %, `underwater_ruin_big` 12 %, `underwater_ruin_small` 5 %. Archaeology: `trail_ruins_rare` 3 %, `desert_well` 5 %, `ocean_ruin_cold` 4 %, `ocean_ruin_warm` 4 %. The expected frequency (a shard every 2 to 3 hours) has not been recomputed ([sante](../plan/sante.md)).
11. **Tannery loot**: the NML moose and deer lines are gone.
12. **Removed scripts and data**: `faune_nml.js` (server and client), `pack/kubejs/data/nomansland/`, `pack/kubejs/assets/nomansland/`. The rabbit stays removed (Fresh Animations has no model for it).
13. **First gestures**: the NML pebble is gone; flint comes from gravel, and the Twigs pebble still knaps into Overgeared's knappable rock.
14. **Translation.** Regions Unexplored ships no French: `pack/kubejs/assets/regions_unexplored/lang/fr_fr.json` translates its 933 texts (maple = érable, pine = pin, willow = saule, redwood = séquoia, larch = mélèze, cypress = cyprès, palm = palmier, kapok = kapokier, joshua = arbre de Josué, wisteria = glycine, blackwood = bois noir; invented names such as Prismoss and Prismarite stay; Bioshroom = biochampignon). `npm run check-fr` no longer has its special case for the Friendly Moon dialogues (code and tests removed, `scripts/test/nml-dialogues.test.mjs` deleted).

## Still open
- ~~**Lithosphere**, under the same biomes, is to be compared in game before the relief is final.~~ Decided on 2026-11-13, after walking in both test packs: Tectonic stays.
- **The finite disc world**: its radius was set to 6,000 blocks on 2026-11-13, with a border after the coast; the disc itself is still to be built. **The pack's new name** is not decided.
- **The new main thread** of the pack, in place of the Friendly Moon.
- Geophilic and Regions Unexplored have not been seen in game; the figures above come from a scanner, not from a walk.

## Rejected
- Terralith, alone or with Tectonic: with Tectonic, 95 visible defects; alone, 47 % water and 16 plants without support.
- Biomes O' Plenty: 71 plants without support, 18 visible defects.
- Oh The Biomes We've Gone: only 16 biomes and 14 plants without support.
- Nature's Spirit: 9 visible defects, 25 biomes, no plant without support; not retained by the team.
- Geophilic alone on Tectonic: same figures as Tectonic alone (it reworks the vanilla biomes and adds no block); it is kept next to Regions Unexplored, not instead of it.
- Lithosphere alone: 8 biomes and 330 detached rock clusters in caves. Compared in game under the same biomes on 2026-11-13 and not kept.
- Keeping No Man's Land: the team's decision of 2026-11-11.
