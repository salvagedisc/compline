# The `fallow-interface` resource pack

Fallow's interface: vanilla screens, the screens of the mods, the HUD and the Questlog journal, in one style. It replaced Dark Coffee GUI and Modded Coffee GUI on 2026-10-26 (all rights reserved, and their author does not answer).

## Sources

The maintainer asked to start from an existing pack that may be modified, then adjust it to the pack's mods. After comparing about thirty interface packs for 1.21.1, the chosen mix is:

| Pack | Author | Licence | What it brings |
|---|---|---|---|
| GUI Revision 2.2.0 | Vixel | CC BY-NC-SA 4.0 | Vanilla screens and HUD |
| GUI Revision Compats 43.0 | Fyoncle | CC BY-NC-SA 4.0 | Screens of Supplementaries, Amendments, Quark, Cold Sweat, Curios, EMI, JEI, Exposure, Farm & Charm, Immersive Enchanting, Overgeared, Chipped, Naturalist, Entity Texture Features |
| Hollow GUI 2 | Melee | CC BY 4.0 | The two curls at the top of the panels |
| Questlog | Infernal Studios | Apache-2.0 | The journal's layout (redrawn) |

Rejected: Overgrown Flowery GUI (its licence forbids publishing a modified version), STONEBORN and Whimscape (all rights reserved), CozyUI+ (GPL-3, but its high-definition look feels like an app rather than Minecraft).

Licence of the result: CC BY-NC-SA 4.0, like GUI Revision. Fallow must therefore not be used commercially (no paid access, no revenue sharing on the pack). The credits are in `pack/resourcepacks/fallow-interface/CREDITS.md`.

## Build

`scripts/interface/build.py` (Python 3 + Pillow, authoring tool, not run by CI) downloads the pinned releases (checked by SHA-1), keeps vanilla and the namespaces of the pack's mods, and writes the pack. Run `npm run check-fr` once before, so that the mod jars are in `.cache/jars`. Then `packwiz refresh` in `pack/`.

What the script changes:

- **Colours.** GUI Revision uses a handful of greys; each one maps to Fallow's palette: mist panels `#CBCAB6`, sage slots `#828C7A`, bone highlights `#E8E5D0`, moss darks and outline `#1A1F1C`.
- **Screens in other colours.** GUI Revision paints a few vanilla screens in wood or purple (crafting table, loom, cartography table, enchanting table, shulker box); they are first brought back to its greys, so that they take the same palette. The same goes for Chipped's workbench and Quark's backpack, whose leather pocket becomes a darker mist pocket with bone stitches.
- **Slots.** Every slot (vanilla, compats, recoloured mods, the hotbar) is redrawn over its whole cell as a recessed well with rounded corners: an outline a step darker than the well, darker at the top and left and fading towards the ends, a soft two-row shadow under the top edge, and a lighter edge along the bottom and right. There is no light line on top and no near-black edge, and no slot is round. A placeholder drawn inside a slot (the loom's banner and dye, Overgeared's blueprint) is kept. The wells where the player and the mounts stand (and the black ones of mod screens) are deeper, filled edge to edge with a fine two-tone grain, without the inner shadow. Each cut corner takes the colour beside it (a frame, a section border, transparency), so that only where four slots meet does a small cross of panel show, the same on every screen. Marks of the source's old slot art left within two pixels of a slot (light corners, a line along the player's well) are cleared. Every block of 2x2 to 3x3 slots with room around it (the inventory's crafting grid, the backpack's, the dispenser, the crafter) takes the crafting table's frame.
- **Arrows.** GUI Revision's wide arrows and small triangles, and vanilla's wide arrow copied by mods, become one slim engraved arrow; the furnaces' progress arrow (and the copies of it in Overgeared, EMI…) is the same arrow in the Questlog's lantern light. Arrows mods draw in shapes of their own (Envelope, Exposure, Farm & Charm, Kaleidoscope Cookery's straight and bent ones) are found by shape, whatever their colour, and redrawn the same way, at 17x8 where the mod's progress sprite is that small; Exposure's plus and the cartography table's become engraved too.
- **Details.** Every large panel, vanilla or mod, takes the crafting table's decoration: GUI Revision's corner ornament in its four corners and two curls from the middle of Hollow GUI's frieze hanging from the top edge; an ornament that would meet a slot or a drawing is not placed, so none is ever cut. Every large panel is covered by one small leaf pattern on a single grid shared by all textures (every 10 pixels across, 8 down, staggered), one shade from the panel; it runs on between and under the slots, so it is the same on every screen whatever the layout. Where a large well touches a slot (the player in the creative inventory, the horse), the well gives way by a pixel; cells a compat drew slightly differently inside a grid (Farm & Charm, the creative inventory's top row) are found from the grid's pitch and redrawn like the others.
- **HUD.** The hotbar is redrawn as a mist bar with rounded ends and a soft shadow, its slots like those of the screens; the selection and the offhand slot match it, the selection in the Questlog's lantern light. Without the XP bar, the `fallow` mod (0.4.1, `xp` module) lowers hearts, armor, hunger, air and thirst by 5 pixels, and Cold Sweat's body icon follows through its client config ([mod-fallow](mod-fallow.md)).
- **Titles.** GUI Revision hides the game's container titles (`container.*` and `merchant.trades` set to a blank) and draws a few titles into the textures. The blanks are kept in `fr_fr` and `en_us`, and the drawn titles are removed: the plates of the furnaces, the crafting and enchanting tables, the traders' "TRADES" banner and Supplementaries' "TRADE OFFER" box. Text drawn in a texture cannot follow the game's language, and only a few screens had one; no screen shows a name now.
- **Other mods.** Screens of mods without a compat that use vanilla's greys (Envelope, Kaleidoscope Cookery, Alex's Mobs, Mowzie's Mobs, No Man's Land, hamsters, Companions!, Critters and Companions, TreeChop) are recoloured in the same tones; a mod's own plain grey surfaces follow the same ramp. The mod's own drawings (Chipped's tool rack, the 3D blocks of Kaleidoscope's recipes, Companions!' puppet…) keep their colours: only the menu itself is reworked. Create and its addons keep their own style, as do the config libraries and the books (Patchouli, Modonomicon, Wildex).
- **Questlog.** `scripts/interface/questlog.py` draws the journal in the same light style: mist cover with sage trims and curled corners, linen pages, sage tiles, lantern light for what is selected, the search bar written « Rechercher ».

## Order

Default Options enables it above the Fresh Animations packs and below Supernova and `fallow-ambiance`.

## Limits and in-game tests

1. Hidden titles: chests, the inventory and the other vanilla containers show no title, as in GUI Revision; a renamed chest still shows its name.
2. Check the hotbar, hearts and selected slot at GUI scales 2, 3 and 4, and the Questlog at its default and custom panel sizes (the nine-slice edges are stretched).
3. Check the screens of Overgeared, Cold Sweat and Supplementaries, whose compats target the latest versions of those mods.
4. Companions! draws English words into its card game textures (Win, Lost, Card, Stop); they come from the mod itself.
5. Thirst Was Reclaimed and Cold Sweat draw their own HUD icons; to be matched later.

## History

Before this pack, two drawn-from-scratch iterations (2026-10-19, then two on 2026-10-26: leather and parchment, then moss slate) were found not clean enough; the maintainer then asked to start from a free-licensed pack, and picked the GUI Revision + Hollow GUI mix. A first review then asked for a regular background pattern instead of scattered details, rounder slots, and every screen in harmony: hence the leaf pattern, the rounded slots and the greyed screens. A second review (same day) asked for the polish of Dark Coffee GUI without its pixels: no names drawn in textures, slimmer arrows, no light line over the slots, a tighter pattern at the same spacing everywhere, cast shadows, rounded slots, a textured well behind the player, a hotbar with rounded ends, and mods' own drawings left untouched. A third review asked for no dark line over the slots, rounded squares instead of round slots, and a finer well behind the player than the grid: the slots now take Dark Coffee's construction (outline, inner shadow, corner profile) redrawn in Fallow's colours, and the well its fine grain. A fourth review asked that nothing be cut or nearly overlap, and that the bars come down into the space of the XP bar. A fifth review asked for the crafting table's corners and curls on every screen, one leaf pattern that does not move with the slots, and a full grain behind the player without the shadow at its top. A sixth review asked for the mods' arrows, the stray light marks around the player, straight crosses between slots, and the crafting table's frame on the other crafting grids.
