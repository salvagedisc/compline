# 0042 — One interaction language for the stations

- **Date**: 2026-11-21
- **Status**: settled (the team asked for it on 2026-11-21 and left the details to the work). Follows [0040](0040-removing-cinematic-interactions.md).

## Context
With Cinematic Interactions gone, the pack mixes windows (workbench, furnaces, knapping, Overgeared's anvil, enchanting, trades, the emissaries' dialogue) with work done by hand in the world (Hands-On Delight's cooking, the house tannery, the forge's strikes) and two timing tests (Overgeared's strikes, Starcatcher's minigame, plus Hands-On Delight's stirring stars). Each one came with its own habits: the pot waited for the spoon, the tool hints said « tu » where every other text says « vous », and every window took 300 ms of camera move and 300 ms of animation each way through Spatial GUI. The question was how to keep mechanics that are alike in vanilla from each working in its own way, without making play slower.

## Options
| Option | For | Against |
|---|---|---|
| Everything in the world (scenes for every station) | One style | What 0040 removed: everyday play slowed down |
| Everything in windows (drop Hands-On Delight, Starcatcher's minigame) | One style, fastest | Loses the gestures the team chose for cooking and fishing |
| **Three registers with a grammar each** | Keeps the chosen gestures, keeps vanilla windows fast, gives every new mechanic a rule | Some mods keep small differences of their own |

## Decision
Three registers, described in [interactions](../plan/interactions.md): **menu** (choosing, storing, machines, books, trades, dialogue), **gesture** (putting, working with the tool, reading with an empty hand, sneak for the other way) and **craft skill** (a click, three words « Parfait ! / Bien ! / Raté ! », a bonus with a quick path, never a gate). Changed on 2026-11-21:

- Hands-On Delight: `stirring_required=false`. The pot cooks on its own in Farmer's Delight's time; stirring still cooks it all at once and earns the stars.
- Spatial GUI: for new installs, camera move and scale animation at 150 ms instead of 300, fade at 100 ms, no closing animation (`pack/config/defaultoptions/extra/config/spatial-gui.json5`, copied by Default Options only when the player has no such file).
- The `outils` hints of the `fallow` mod say « vous » (« Il vous faut une hache. », « Votre outil n'est pas assez solide. »), carried by the next build of the jar.

## Consequences
- New stations, mods and modules name their register and follow its grammar; the checklist is in the plan page.
- The emissaries' dialogue is a menu and follows the menu's pace.
- In-game tests IN1 to IN5 in [interactions](../plan/interactions.md#in-game-tests).
