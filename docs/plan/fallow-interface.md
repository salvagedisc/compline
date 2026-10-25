# The `fallow-interface` resource pack

Request from the maintainer on 2026-10-19: match the look of Kaleidoscope, Overgeared and Wildex to Dark Coffee GUI, which no pack covers. Formats and colours were read from the jars and the textures; **nothing has been seen in game.**

> **Removed from the pack on 2026-10-19.** Neither the recoloured version nor the one with Fallow-drawn elements won the maintainer over. A version made with the actual elements of Dark Coffee and Modded Coffee has been proposed to their author (RestlessPip, through the GitHub issues of `darkcoffeegui`): since Dark Coffee is "all rights reserved", it will only enter the pack if he integrates it into Modded Coffee GUI or allows us to distribute it. In the meantime, these screens keep their original grey. The rest of this page describes the removed version; the licences and limits remain valid.

## What it contains

`pack/resourcepacks/fallow-interface/`, 24 textures, same sizes and same transparency as the originals:
- **Overgeared** (14): the 9 screens (anvils, furnaces, fletching table, blueprint workbench, stonecutting), the forging mini-game bar, the EMI anvil background and the 4 filter buttons of the recipe book;
- **Kaleidoscope** (8): the recipe backgrounds shown in EMI; only the slots and arrows change, the block drawings stay;
- **Wildex** (2): the book background (VINTAGE theme, the default) and the red trophy background.

**Drawn elements** (2026-10-19, at the maintainer's request, to go beyond colours): a small library of elements specific to Fallow, applied to every Overgeared screen and to the Kaleidoscope backgrounds, pixel-exact to the original layout:
- recessed 18×18 slot and large 26×26 slot: dark edge at the top and left, light edge at the bottom and right, 2 px inner shadow, cut corners;
- progress arrow with a rounded tail, light ridge on top and dark on the bottom; the filled arrow follows the same mask;
- background with short horizontal fibres and a few dark dots;
- frame with a dark outline, light trim at the top and left, shadow at the bottom and right;
- 2 px groove between the screen and the player inventory.

Dark Coffee is "all rights reserved": none of its textures is copied. Its slots are raised pads with rounded corners, its arrow an ornate diamond, its background coffee beans; ours take up the spirit without reproducing them (15% of the pixels outside flat areas coincide on a furnace compared with ours, no 6×6 block in common). Checked by script: same sizes and same transparency as the originals, every slot of the original covered by a drawn slot at the same rectangle, no slot added or moved, empty and filled arrows superimposed. On the transparent Kaleidoscope and EMI backgrounds, the arrows keep their original silhouette with our shading, and the block drawings are byte-identical. Slots stay contiguous in the grids: spacing them out would move them.

Colour mapping, taken from Dark Coffee (values, no pixels): background `#C6C6C6` → `#451D21`, outline → `#2A0614`, bevels → trim `#B18469`, slot interior `#8B8B8B` → `#815648` (shadow `#693F35`), slot bottom edge → `#582E2B`, progress arrow → `#F1BA75`. On the Wildex book, steel becomes brass and the wood leans towards brown; the pages stay light, because its text is written in `#2B1A10` in the code.

The flames, the yellow bar and the green zones of the mini-game do not change: they must stay readable.

## Licences

All three mods allow publishing modified textures: Kaleidoscope under CC BY-NC-SA 4.0 (our files under the same licence), Overgeared under MIT, Wildex under CC BY-NC 4.0. The details and the MIT notice are in the pack's `CREDITS.md`. The "NC" clauses forbid any commercial use: no monetisation of Fallow on Modrinth as long as these textures are in the pack. The exact holder of Overgeared's MIT licence could not be verified (no LICENSE file in the jar); the declared author is StirDrem.

## Order

Default Options enables it on first launch, after Dark Coffee and Modded Coffee and before `fallow-ambiance`. It shares no file with them. It contains no shader: it would replace the Dark Coffee one, which recolours the titles.

## Limits and in-game tests

1. **Screen titles.** They are written in `0x404040` by the code. The Dark Coffee text shader turns them into `#B08569`, but it looks for a slightly different value (0.248 versus 0.251); if it misses them, the titles are unreadable on the brown background, on vanilla screens as on Overgeared ones. Opening a furnace is enough to see it.
2. **Kaleidoscope text in EMI**: `#555555` on the brown background, hard to read. No texture fixes it.
3. **Wildex**: its palette and fills are written in the code; only the background changes. Another option without textures: `designStyle = "STEAMPUNK"` in its client config, a theme that is already brass and brown.
4. At each update of these mods, compare their interface textures with ours: a texture that changes size or layout must be redone.
