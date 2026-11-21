# Crafting by hand at the workbench (prototype)

The crafting table worked like Hands-On Delight's stations: the ingredients are laid on the table one by one, with the clicks of the gesture register ([interactions](interactions.md#grammar-of-the-gesture)), and a small notebook beside it shows a recipe as ghosts on the grid. The window stays one click away. Module `etabli` of `fallow` (0.40.0), **off by default** (`etabli.enabled` in `fallow-common.toml`) until it has been tried in game.

Status: prototype, checked on the dev client on 2026-11-21 with Visual Workbench and Puzzles Lib, not played in the full pack.

## Why on Visual Workbench

The pack already has Visual Workbench: every crafting table becomes its block entity, keeps its nine ingredients and draws them on the table, with the result above. The module only adds the clicks; it reads and fills that block entity (a vanilla `Container` of nine slots, through reflection, no compile dependency), so the table, its drawing, its window and EMI's "fill the grid" all stay Visual Workbench's. Without Visual Workbench, the module does not start.

The grid turns a quarter turn toward the nearest player (Visual Workbench's `rotate_ingredients`); `Grille` redoes its maths to know which cell the click aims at (tested without the game for the four quarters). Items lie flat on the table when Visual Workbench's `flat_rendering` (client config) is on, which suits the gestures better; they float above it otherwise. The ghosts follow either way.

## The gestures

On the top of the table:

| Move | Effect |
|---|---|
| Use with an item | One item goes into the aimed cell (or one more onto the same item). A cell holding something else: « Cette case est prise : reprenez d'abord ce qui y est. » |
| Use with an empty hand on a full cell | That cell's stack comes back to the inventory |
| Use with an empty hand on an empty cell with a ghost | The ghost's ingredient comes from the inventory |
| Use with an empty hand elsewhere (empty cell, rim) | The table's window, as before |
| Sneak + use, empty hand | Crafts what the table shows, once per click, into the inventory (remainders such as buckets stay in their cell). Nothing ready and a notebook page open: lays every missing ingredient at once |
| Sneak + use with an item | The game's usual (placing a block on the table) |

Crafting fires the game's crafting event and advancements, as the window does.

## The workbench notebook

« Carnet d'établi »: a book and a coal or charcoal, shapeless. Placed on the ground within two blocks of a crafting table. Empty hand on it turns a page, sneaking turns back. Each page is a recipe that can be made now with what the player carries plus what lies on the table, and that uses everything on the table, sorted by name: put a cobblestone on the table and the notebook offers ten pages (stone slab, stairs, furnace, stone tools, lever, wall). The page shows as a margin note with a book icon (« Page 9 sur 10 : Pioche en pierre ») and its ingredients appear as ghosts in the table's empty cells; the page stays open for that table until another is turned, the player goes 12 blocks away, or logs out.

The notebook is not a second recipe book: browsing every recipe stays EMI's job (one book per domain); it only answers "what can I make here, now".

## Open questions for after the test

- Whether to keep the window on an empty hand, or move it to sneak + use and give an empty hand on an empty cell to something else.
- Whether to turn Visual Workbench's `flat_rendering` on by default.
- The notebook lists pages one at a time; with a full inventory and an empty table it can hold many. Laying one ingredient first narrows it.
- Multiplayer: the grid turns toward the nearest player, so two players at one table may aim different cells than they see.

## In-game tests

| # | Test | Expected |
|---|---|---|
| ET1 | Turn `etabli.enabled` on; right-click a crafting table with an empty hand | The window opens as usual (and the table becomes Visual Workbench's) |
| ET2 | With oak planks in hand, click four cells in a square | One plank lands in each aimed cell; the crafting table shows above |
| ET3 | Sneak + empty hand on the table | One crafting table goes to the inventory; the planks are used |
| ET4 | Empty hand on a cell that holds an item | It comes back to the inventory |
| ET5 | Place the notebook beside the table, lay one cobblestone, click the notebook until « Pioche en pierre » | A margin note names the page; ghost stone and sticks fill the pickaxe's cells |
| ET6 | Sneak + empty hand twice | The missing ingredients are laid, then the pickaxe is crafted |
| ET7 | Walk around the table, lay an item from each side | The item lands under the crosshair from every side |
