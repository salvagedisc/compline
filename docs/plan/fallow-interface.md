# The `fallow-interface` resource pack

Request from the maintainer on 2026-10-26: give Fallow its own interface "in the style of Dark Coffee GUI", starting with the Questlog journal and the HUD, since the author of Dark Coffee does not answer and the pack needs something it owns. **Nothing has been seen in game yet**: the previews were composed from the textures.

## What it contains

`pack/resourcepacks/fallow-interface/`, drawn by `scripts/interface/generate.py` (Python 3 + Pillow, authoring tool, not run by CI). Run it again after changing a shape or a colour, then `packwiz refresh` in `pack/`.

- **Questlog journal** (`assets/questlog/textures/gui/`): roasted-leather cover with a brass trim and parchment pages with ink corner flourishes (`questlog.png`, `quest_page.png`, nine-slice 275×166, border 16); parchment buttons that light up like a lantern when hovered; tapered ink rules; leather toast; wax-red "important" mark; linen and wine-leather chapter bookmarks; search bar labelled « Rechercher » (the original texture had "Search Quests" written in it); magnifying glass with misty glass; small leather journal as the inventory button.
- **HUD** (`assets/minecraft/textures/gui/sprites/hud/`): leather hotbar with recessed slots and brass rivets, lantern-lit selection frame, off-hand slot; hearts in every state (normal, poisoned, withered, frozen, absorbing, hardcore with a stitched seam, blinking, mount); round loaves of bread for food (stale grey-green under Hunger); chestplates for armour; misty air bubbles.

Every UV region and drawable area follows the originals, read from the Questlog jar (`QuestlogGuiSet`, `QuestlogOpenButton`) and from vanilla 1.21.1, so text and items land where the code expects them.

## Style

The palette is Fallow's own: leather `#482624` (dark `#321A1A`, light `#623832`), brass `#A07C58`, parchment `#D6C2A0` / `#E6D7BA`, ink `#3A2418`, lantern `#DEA250`, wine `#742824`, mist `#9CAAAC`. It sits next to Dark Coffee's wine-brown so the two packs can coexist while Dark Coffee still dresses the containers. Shapes differ on purpose: recessed slots instead of raised pads, continuous brass trim instead of corner ornaments, bread instead of coffee beans, a lantern frame for the selection.

Questlog writes its text in black by default (`0xFF000000`), so the pages stay light. A quest can still change its colours with `text_color`, `title_color` and `progress_text_color`.

## Licences

The textures are original. Questlog is Apache-2.0, which allows changing its look; Dark Coffee GUI is all rights reserved, so no pixel of it is reused (only the idea of a warm brown interface). The label « Rechercher » uses the letter shapes of Minecraft's font.

GUI Revision (CC BY-NC-SA 4.0) and GUI Revision Compats (same licence, 140+ mods) were found as a fallback base that may legally be modified; their English titles are drawn into the textures and would need redrawing in French.

## Order

Default Options enables it after Dark Coffee, Modded Coffee and Normal Hunger, and before Supernova and `fallow-ambiance`: its HUD therefore replaces the one of Dark Coffee and Normal Hunger.

## Next steps

1. In-game check: hotbar and hearts at GUI scales 2, 3 and 4; Questlog at the default and custom panel sizes (the edges of the nine-slice are stretched).
2. If the style is kept: the containers (inventory, chests, furnaces, crafting) and the mod screens, to replace Dark Coffee and Modded Coffee entirely.
3. Thirst Was Reclaimed and Cold Sweat draw their own HUD icons; to be matched once the style is settled.

## History

A first `fallow-interface` (2026-10-19) recoloured the Kaleidoscope, Overgeared and Wildex screens to Dark Coffee's colours, then added Fallow-drawn slots and arrows; neither convinced the maintainer and it was withdrawn the same day.
