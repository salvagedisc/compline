# 0013 — First gestures: tools needed for logs and rock

- **Date**: 2026-10-29
- **Status**: proposed to the team on 2026-10-29; nothing seen in game yet

## Context
The team asked on 2026-10-29 to rethink the recipes and how basic resources are obtained, given the pack's mechanics: it should be impossible to break wood, stone and the like by hand. The pack already had most of a primitive start without using it: Overgeared 1.6.19 knaps stone tool heads from a rock struck off stone with flint and replaces the vanilla stone tool recipes; No Man's Land pebbles drop flint half of the time; Twigs scatters twigs (two sticks each) in forests. What was missing: the rule itself, the removal of wooden tools, and guidance. Jars read: Overgeared 1.6.19, No Man's Land 1.6.3, Twigs 3.1.2, Supplementaries 3.9.9, HT's TreeChop 0.19.3, KubeJS 2101.7.2.

## Options
| Criterion | `outils` module + scripts | Survivalist Essentials | No Tree Punching Remastered | Primity |
|---|---|---|---|---|
| Tool rule | Data-driven tag + vanilla "correct tool" | Configurable lists | Wooden blocks only | Logs only |
| Overlap with Overgeared | None: uses its knapping | Its own rocks and crude tools | Flint and copper tools | Flint tools, cast iron |
| Content added | None | Knives, saws, fibre, bandages, bleeding, book | Rocks, ceramic vessel | Buckler, cast iron |
| Licence | Ours | MIT | GPL-3.0 | Own |

## Decision
No new mod. The `outils` module of the `fallow` mod makes logs (`fallow:outil_requis`) and every block that needs the correct tool for drops unbreakable without that tool, with a hint in the action bar ([mod-fallow](../plan/mod-fallow.md#outils-module)). `pack/kubejs/server_scripts/premiers_gestes.js` removes wooden tools, makes planks cost an axe (2 per log, the axe takes one point of damage; Create's saw keeps 6), lights the campfire with flint instead of coal, turns a Twigs pebble into an Overgeared knappable rock and adds a few sticks to leaves. A « Premiers gestes » Questlog chapter guides each step. Details: [premiers-gestes](../plan/premiers-gestes.md).

Crafted wooden blocks stay breakable by hand (a player can take down what they built); adding them to `fallow:outil_requis` is a datapack change if the team wants it harder.

## Rejected
- Survivalist Essentials, No Tree Punching Remastered, Primity: each brings a second stone-age system next to Overgeared's, plus content the pack already has elsewhere (NML bandages, the bag, Overgeared's copper and steel).
