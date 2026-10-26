# The `fallow-interface` resource pack

Request from the maintainer on 2026-10-26: give Fallow its own interface "in the style of Dark Coffee GUI", starting with the Questlog journal and the HUD, since the author of Dark Coffee does not answer and the pack needs something it owns. **Nothing has been seen in game yet**: the previews were composed from the textures.

## What it contains

`pack/resourcepacks/fallow-interface/`, drawn by `scripts/interface/generate.py` (Python 3 + Pillow, authoring tool, not run by CI). Run it again after changing a shape or a colour, then `packwiz refresh` in `pack/`.

- **Questlog journal** (`assets/questlog/textures/gui/`): moss-slate cover sown with sprouting seeds, bone trim with curled corners, linen pages with a near-invisible seed pattern and ink corner flourishes (`questlog.png`, `quest_page.png`, nine-slice 275×166, border 16); rounded lichen buttons that light up like a lantern when hovered; ink rules with a leaf in the middle; a lit lantern as the "important" mark; lichen and rust chapter bookmarks with a stitched hem; search bar labelled « Rechercher » (the original texture had "Search Quests" drawn in English); magnifying glass with misty glass; moss-bound journal with a lantern clasp as the inventory button.
- **HUD** (`assets/minecraft/textures/gui/sprites/hud/`): sown hotbar with raised rounded slots and lantern beads, rounded lantern selection, off-hand slot; hearts with a glint and three tones of shade in every state (poisoned, withered, frozen, absorbing, hardcore stitched, blinking, mount); loaves of bread (a cut loaf shows its crumb, stale grey-green under Hunger); chestplates with brass buckles; air bubbles with a glint.

Every UV region and drawable area follows the originals, read from the Questlog jar (`QuestlogGuiSet`, `QuestlogOpenButton`) and from vanilla 1.21.1, so text and items land where the code expects them. `inventory()` in the script draws the player inventory in the same style as a preview; it is not written into the pack yet.

## Style

Second iteration (2026-10-26), after the maintainer asked for Dark Coffee's attention to detail without its colours: rounder shapes, detailed icons, a background pattern like Coffee's coffee beans. Palette: slate `#29322E` sown with `#2F3934`, bone `#B0B09A`, lichen tiles `#808C7C`, linen `#E0DAC6`, ink `#282E28`, lantern `#E2A64E`, rust `#8A3E2A`, mist `#A0B2B6`. The pattern is a staggered sprouting seed, the field left fallow, four levels above the ground so it shows without competing with items.

A light variant in the spirit of GUI Revision ("Brume claire": light cards, moss band, flat sunken slots) was drawn for comparison and not kept in the pack.

Questlog writes its text in black by default (`0xFF000000`), so the pages stay light. A quest can still change its colours with `text_color`, `title_color` and `progress_text_color`. Vanilla container titles are written in `0x404040`: on dark panels they rely on Dark Coffee's text shader today, and will need one of Fallow's own when Dark Coffee leaves.

## Licences

The textures are original. Questlog is Apache-2.0, which allows changing its look; Dark Coffee GUI is all rights reserved, so no pixel of it is reused (only the idea of a warm brown interface). The label « Rechercher » uses the letter shapes of Minecraft's font.

GUI Revision (CC BY-NC-SA 4.0) and GUI Revision Compats (same licence, 140+ mods) were found as a fallback base that may legally be modified; their English titles are drawn into the textures and would need redrawing in French.

## Order

Default Options enables it after Dark Coffee, Modded Coffee and Normal Hunger, and before Supernova and `fallow-ambiance`: its HUD therefore replaces the one of Dark Coffee and Normal Hunger.

## Next steps

1. In-game check: hotbar and hearts at GUI scales 2, 3 and 4; Questlog at the default and custom panel sizes (the edges of the nine-slice are stretched).
2. If the style is kept: the containers (inventory, chests, furnaces, crafting) and the mod screens, plus a text shader for the titles, to replace Dark Coffee and Modded Coffee entirely.
3. Thirst Was Reclaimed and Cold Sweat draw their own HUD icons; to be matched once the style is settled.

## History

The first iteration of 2026-10-26 (leather and parchment in Dark Coffee's browns) was found correct but lacking detail. A first `fallow-interface` (2026-10-19) recoloured the Kaleidoscope, Overgeared and Wildex screens to Dark Coffee's colours, then added Fallow-drawn slots and arrows; neither convinced the maintainer and it was withdrawn the same day.
