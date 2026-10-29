# First gestures

With bare hands, you gather but you do not cut. A trunk needs an axe, rock needs a pickaxe, and the first tool is knapped from stone. Dirt, sand, gravel, leaves and plants stay breakable by hand, otherwise nobody could start. Decision: [0013](../decisions/0013-premiers-gestes.md).

Status: written on 2026-10-29. Checked on a test server (NeoForge with KubeJS, LootJS, Overgeared, Twigs, No Man's Land): the scripts load without error, 15 planks recipes are rewritten (11 vanilla, 4 NML), an oak log and a stone axe give 2 planks and give back the axe with 1 damage, the campfire takes flint, the wooden tools have no recipe. Not yet tested with a player.

## The chain, for a player

1. **Bare hands.** Pick up NML pebbles (one chance in two of flint), gravel (flint, rarer), Twigs twigs (two sticks each, in forests, taigas and swamps), leaves (a stick 8% of the time). A trunk or a rock does not give way: the action bar says « Il te faut une hache » / « Il te faut une pioche » ("you need an axe / a pickaxe").
2. **Knapped stone.** Flint used on bare `minecraft:stone` gives a knappable rock (Overgeared, 30% per strike, the flint breaks 15% of the time). A Twigs pebble also turns into a knappable rock in the inventory grid. Right-click with the rock opens Overgeared's knapping grid: axe, pickaxe, shovel, hoe heads, sword blade, hammer head. Head + stick in the 2 × 2 grid = the stone tool (Overgeared's own recipes).
3. **Wood.** The stone axe fells the tree block by block (TreeChop); each chop costs durability. A log and an axe in the grid give **2 planks**; the axe stays in the grid and loses one point. Create's mechanical saw later gives 6 planks per log.
4. **Fire.** A campfire is three sticks, three logs and **a flint** instead of coal: fire on the first day, before the furnace. It cooks, purifies water (Thirst Was Reclaimed) and turns charcoal into ashes for the [tannery](tannerie.md).
5. **Furnace, copper, iron, steel.** All Overgeared, unchanged: furnace from cobblestone; copper hammer (2 copper ingots, 2 sticks); crouch + right-click on stone with a hammer = stone smithing anvil; clay casts fired in the furnace; blast furnace, heated ingots forged on the stone anvil; smithing anvil and alloy furnace for steel.

## The rule

The `outils` module of the `fallow` mod ([mod-fallow](mod-fallow.md#outils-module)). A block needs a tool when it is in the `fallow:outil_requis` block tag (`#minecraft:logs`) or when it would drop nothing without the correct tool (vanilla `requiresCorrectToolForDrops`: stone, ores, bricks, metal blocks, lanterns). Without the correct tool in the main hand, the mining speed is 0 on both sides (no ghost block), harvesting is refused, and a hint shows at most every two seconds. A tool of the right kind but too weak gets « Ton outil n'est pas assez solide » ("your tool is not strong enough"). The `fallow:sans_outil` tag keeps snow breakable by hand. Creative mode and fake players (Create deployers) are not affected.

Crafted wooden blocks (planks, furniture, chests) stay breakable by hand: a player can take down what they built. To make them need an axe too, add `#minecraft:planks` (and furniture tags) to `fallow:outil_requis` in a datapack; no recompilation.

## The recipes

`pack/kubejs/server_scripts/premiers_gestes.js`:

| What | Before | After |
|---|---|---|
| Wooden tools (5) | Craftable | Recipes removed, removed from chest loot, hidden from EMI (`client_scripts/premiers_gestes.js`) |
| Log → planks | 4 by hand | `PREMIERS_GESTES_PLANCHES_A_LA_HACHE` (2) with any axe in the grid, damaged by 1; every single-log shapeless planks recipe is rewritten (vanilla, NML, other woods), id `fallow:premiers_gestes/planches/<original id>`; the log reports how many |
| Campfire | Sticks + logs + coal | Sticks + logs + flint |
| Twigs pebble | 4 → cobblestone | Kept, plus 1 → `overgeared:knappable_rock` (`fallow:premiers_gestes/galet_a_tailler`) |
| Leaves broken | Stick 2% | Plus an extra stick at `PREMIERS_GESTES_BATONS_FEUILLES` (6%) |

Unchanged on purpose: Overgeared's stone, copper, iron and steel tools; the vigil fire and clearing lantern recipes (reviewed with the mist settings).

## Quests

New chapter « Premiers gestes » (order 1, after Arriver; the other chapters move down by one), one quest per gesture ([quests](quetes.md)): Mains nues (guide), Le silex, La pierre à tailler, La première hache, Des planches (crafting table), Le feu (place a campfire), La pioche, Le four, Le marteau et l'enclume de pierre (copper hammer), La forge (guide, appears with the first iron ingot).

## Rejected

- [Survivalist Essentials](https://modrinth.com/mod/survivalist-essentials) (MIT): configurable tool rule, saws, knife and plant fibre, loose rocks, bandages and bleeding. A second stone and rock system next to Overgeared, bandages duplicating NML's, one more book.
- [No Tree Punching Remastered](https://modrinth.com/mod/no-tree-punching-remastered) (GPL-3.0): flint and copper tools, rocks, ceramic vessel. Copper tools duplicate Overgeared's, the vessel duplicates the bag.
- [Primity](https://modrinth.com/mod/primity) (own licence): flint tools, cast iron, harder blocks. Cast iron duplicates Overgeared's steel chain.

## In-game tests

| Code | Test | Expected |
|---|---|---|
| G1 | New world, bare hands on a log, then on stone | Nothing breaks; « Il te faut une hache », then « Il te faut une pioche », in French, not spammed |
| G2 | Stone pickaxe on a diamond ore | « Ton outil n'est pas assez solide », the ore does not break |
| G3 | Dirt, sand, gravel, leaves, snow, flowers by hand | Break as in vanilla |
| G4 | Walk around spawn | A flint within about 5 minutes (NML pebbles, gravel) |
| G5 | Flint on stone, knap an axe head, head + stick | A stone axe; the tree falls with TreeChop; note the durability used per tree |
| G6 | Oak log + axe; NML maple log + axe | 2 planks each, the axe stays in the grid with one point less; the server log says how many planks recipes were rewritten |
| G7 | Craft and place a campfire with flint | Lit campfire; the « Le feu » quest completes |
| G8 | Twigs pebble in the grid | A knappable rock |
| G9 | EMI search "bois" | No wooden tools |
| G10 | Questlog | « Premiers gestes » after Arriver; each quest appears after the previous one and completes |
| G11 | Time to the first furnace | Aim for 20 to 30 minutes |
| G12 | Create deployer holding nothing in front of a log | Not blocked by the rule (fake player) |
