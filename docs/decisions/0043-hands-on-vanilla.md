# 0043 — Hands-On Vanilla for the vanilla stations

- **Date**: 2026-11-21
- **Status**: settled (the team's choice of 2026-11-21). Amends [0040](0040-removing-cinematic-interactions.md) and [0042](0042-interaction-language.md).

## Context
The team saw a crafting table worked by hand, with a recipe book lying beside it, in the style of Hands-On Delight. A first answer was a prototype of our own (`etabli` module, `fallow` 0.40.0, off by default: clicks on Visual Workbench's grid and a notebook block). The team then asked for the work to follow Hands-On Delight's model exactly, so that every station of the pack plays alike. Studying that model led to the mod that had been seen: Hands-On Vanilla, by the author of Hands-On Delight, built on the same library (Diegesis), MIT, client-side, published only on CurseForge.

## Options
| Option | For | Against |
|---|---|---|
| Keep the prototype and extend it | Ours | Another way of doing things beside Hands-On Delight's, the opposite of 0042 |
| Rewrite Hands-On Delight's model in the `fallow` mod | Ours, same model | About 15,000 lines to redo for what already exists |
| **Hands-On Vanilla as it is** | The exact model, same author, same library, 13 stations | A jar embedded in the pack, as Cinematic Interactions was |

## Decision
Hands-On Vanilla 1.2.2 joins the pack as a jar in `pack/mods/` (MIT, CurseForge project 1809623). The `etabli` prototype and its notebook leave the `fallow` mod (0.41.0). Settings:

- Visual Workbench leaves the pack: it turned the vanilla crafting table into a block of its own, which Hands-On Vanilla does not open, and the pack has no other crafting table for it. Puzzles Lib stays for Pick Up Notifier.
- Off in `config/handson_vanilla.properties`: the enchanting table (Immersive Enchanting opens a menu of its own there, [0008](0008-enchantement.md)) and the grindstone (Spelunkery's gems and Overgeared's grinding are done on it with an item in hand). Both keep their windows until they are tried together in game.
- French names for the six tabs of the book (`kubejs/assets/handson_vanilla/lang/fr_fr.json`).

## Consequences
- Crafting, smelting (furnace, smoker, blast furnace), stonecutting, smithing, brewing, the anvil, the lectern, the loom and the cartography table move from the menu register to the gesture register of [interactions](../plan/interactions.md).
- The anvil works with the pack's anvil without experience: no level cost is shown and the repair goes through (seen on the bench).
- Embedded jars now include Hands-On Vanilla. Modrinth's check of embedded files held back the first version with Cinematic Interactions; the MIT licence is the permission to give it.
- The pack's own stations (tannery, forge) can later be built on Diegesis the same way.
