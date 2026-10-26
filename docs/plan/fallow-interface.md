# The `fallow-interface` resource pack

Fallow's interface: vanilla screens, the screens of the mods, the HUD and the Questlog journal, in one style. It replaced Dark Coffee GUI and Modded Coffee GUI on 2026-10-26 (all rights reserved, and their author does not answer).

## Sources

The maintainer asked to start from an existing pack that may be modified, then adjust it to the pack's mods. After comparing about thirty interface packs for 1.21.1, the chosen mix is:

| Pack | Author | Licence | What it brings |
|---|---|---|---|
| GUI Revision 2.2.0 | Vixel | CC BY-NC-SA 4.0 | Vanilla screens and HUD |
| GUI Revision Compats 43.0 | Fyoncle | CC BY-NC-SA 4.0 | Screens of Supplementaries, Amendments, Quark, Cold Sweat, Curios, EMI, JEI, Exposure, Farm & Charm, Immersive Enchanting, Overgeared, Chipped, Naturalist, Entity Texture Features |
| Hollow GUI 2 | Melee | CC BY 4.0 | The frieze along the top of the panels |
| Questlog | Infernal Studios | Apache-2.0 | The journal's layout (redrawn) |

Rejected: Overgrown Flowery GUI (its licence forbids publishing a modified version), STONEBORN and Whimscape (all rights reserved), CozyUI+ (GPL-3, but its high-definition look feels like an app rather than Minecraft).

Licence of the result: CC BY-NC-SA 4.0, like GUI Revision. Fallow must therefore not be used commercially (no paid access, no revenue sharing on the pack). The credits are in `pack/resourcepacks/fallow-interface/CREDITS.md`.

## Build

`scripts/interface/build.py` (Python 3 + Pillow, authoring tool, not run by CI) downloads the pinned releases (checked by SHA-1), keeps vanilla and the namespaces of the pack's mods, and writes the pack. Run `npm run check-fr` once before, so that the mod jars are in `.cache/jars`. Then `packwiz refresh` in `pack/`.

What the script changes:

- **Colours.** GUI Revision uses a handful of greys; each one maps to Fallow's palette: mist panels `#CBCAB6`, sage slots `#828C7A`, bone highlights `#E8E5D0`, moss darks and outline `#1A1F1C`.
- **Screens in other colours.** GUI Revision paints a few vanilla screens in wood or purple (crafting table, loom, cartography table, enchanting table, shulker box); they are first brought back to its greys, so that they take the same palette. The same goes for Chipped's workbench and Quark's backpack, whose leather pocket becomes a darker mist pocket with bone stitches.
- **Slots.** Every slot (vanilla, compats and recoloured mods, the hotbar too) is redrawn as a rounded well: dark moss outline, a two-row shadow at the top, a light bottom edge. A placeholder drawn inside a slot (the loom's banner and dye) is kept.
- **Details.** Hollow GUI's frieze is laid along the top of every 176-pixel vanilla panel, behind slots and title plates. Every large panel gets a regular, staggered pattern of small leaves (every 12 pixels), one shade from the panel, kept clear of slots, plates and edges.
- **Highlights.** The selected hotbar slot and the offhand slot lose GUI Revision's gold for the Questlog's lantern light; the traders' crimson and gold banner becomes moss and bone.
- **Titles.** GUI Revision hides the game's container titles (`container.*` and `merchant.trades` set to a blank) and draws a few titles into the textures. The blanks are kept in `fr_fr` and `en_us`, and the drawn titles are redone in French with the letters cut from GUI Revision's own plates: FOURNEAU, HAUT FOURNEAU, FUMOIR, ÉTABLI, ENCHANTEMENT, ÉCHANGES. The same goes for Supplementaries' traders: ÉCHANGES on the banner, and their red "TRADE OFFER" box with warning signs becomes a GUI Revision title plate reading OFFRE.
- **Other mods.** Screens of mods without a compat that use vanilla's greys (Envelope, Kaleidoscope Cookery, Alex's Mobs, Mowzie's Mobs, No Man's Land, hamsters, Companions!, Critters and Companions, TreeChop) are recoloured in the same tones; the greys a mod adds of its own follow the same ramp. Create and its addons keep their own style, as do the config libraries and the books (Patchouli, Modonomicon, Wildex).
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

Before this pack, two drawn-from-scratch iterations (2026-10-19, then two on 2026-10-26: leather and parchment, then moss slate) were found not clean enough; the maintainer then asked to start from a free-licensed pack, and picked the GUI Revision + Hollow GUI mix. A first review then asked for a regular background pattern instead of scattered details, rounder slots, and every screen in harmony: hence the leaf pattern, the rounded slots and the greyed screens.
