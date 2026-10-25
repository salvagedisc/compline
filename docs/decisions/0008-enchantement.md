# 0008 — Enchanting

- **Date**: 2026-10-19
- **Status**: kept (team choice); not tested in game

## Context
The team wants enchanting to become a real stage of the game, like forging with Overgeared and cooking with Kaleidoscope, rather than a mere cost in levels. Full research: [enchantement.md](../plan/recherche/2026-10-19/enchantement.md).

## Options
| Criterion | Immersive Enchanting | Matrix Enchanting (Quark) | Enchanting Overhauled |
|---|---|---|---|
| Loop | Find ancient books, shelve them near the table, pay each level in materials | Coin mini-game on a grid | Altar awakened by a tome that was found |
| Link to the world | Strong: books in loot, costs in the pack's materials | Weak: nothing to find | Medium |
| Weight | 7.1 MB + AlfinoLib | 0 (already in Quark) | Large combat rebalancing |
| Risks | Young, a single author, no EMI plugin | None | Mixin on Overgeared's smithing table, ARR licence |

## Decision
**Immersive Enchanting** 6.0.1 (+ AlfinoLib), with:
- its three "ancient library" structures disabled (`pack/kubejs/data/immersiveenchanting/tags/worldgen/biome/`, tags emptied): books will come from the No Man's Land ruins and the monuments;
- Mending already disabled by the mod (`"enabled": false` in its cost file): it would cancel the upkeep of Overgeared tools;
- a French translation (`pack/kubejs/assets/immersiveenchanting/lang/fr_fr.json`), the mod being English-only.

To do next (see the research): cost files for the enchantments of the pack's other mods (otherwise they are disabled), books in the loot of the NML ruins, one lore item per Ancient Remnants monument, books at Wildex tiers.

## Rejected
- Easy Magic: replaces the vanilla enchanting table that Immersive Enchanting depends on.
- Quark's Matrix Enchanting: remains the fallback if Immersive Enchanting disappoints in game.
- Enchanting Overhauled, Apothic Enchanting, Ars Nouveau, Iron's Spells, Occultism, Malum, Create: Enchantment Industry: too RPG, too heavy or contrary to the tone.
