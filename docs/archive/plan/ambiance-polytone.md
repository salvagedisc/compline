# compline-ambiance, the look without a shader pack (archived)

> Removed on 2026-11-15 with Polytone and the `relief` module ([0031](../../decisions/0031-brumaire-is-the-look.md)): the look now comes from Brumaire. Kept as a record of what was tried.

Living page of the resource pack shipped in `pack/resourcepacks/compline-ambiance/` (decision [0029](../../decisions/0029-look-without-shader.md)). It is **on by default** and needs Polytone. The model for the look is Recrafted by MR_CHOO (a paid resource pack for the current versions of the game, without a licence: none of its files are used).

## What it does

One post-processing chain (`assets/compline/polytone/post_chains/rendu.json`, `assets/compline/post_effect/rendu.json`) runs on the finished image of the game, in the Overworld:

| Pass | Program | Size | What it does |
|---|---|---|---|
| Sun shafts | `rayons`, then `flou` twice | 640×360 | Light spreading from the sun or the moon between the leaves, read on the screen |
| Glow | `seuil`, then `flou` twice | 480×270 | The bright parts of the image (torches, lava, sun) bleed a little |
| Final | `tons` | full | Air, sun shadows, water reflections, low mist, under water, grade (see below) |
| Copy | `copie` | full | Writes the result back |

The final pass, in order:

1. **Air**: a warm veil on the side of the sun and cooler distances, growing with distance. The mist itself stays ATMOSPHERICS' preset.
2. **Sun shadows**: trees, hills and walls cast a shadow, the faces turned to the sun are lighter than the others, and the ground under a canopy gets half-block flecks of sun that open and close slowly. Softer in the mist (60 %), full where a clearing has lifted it; none in the rain, at night or beyond 112 blocks.
3. **Sky on the water**: where the view crosses a water surface (the `relief` texture says which columns hold water and at what height), the water mirrors the sky with a Fresnel term (little when seen from above, a lot at a grazing angle) and small ripples, livelier in the rain. The reflected sky is read from the image itself when the reflected direction lands on sky pixels on the screen, so it keeps ATMOSPHERICS' mist, dusk and clouds; otherwise an estimate from the time of day. The sun leaves a glint on the ripples.
4. **Low mist**: a thin mist that lies below about 70 (densest under 63, sea level), thicker at dawn and dusk and in the rain. It is integrated along the view ray from the height, paints the terrain only (the sky keeps ATMOSPHERICS' mist) and stays well under the main mist.
5. **Under water**: the water takes the colour of the biome (water fog colour given by the mod), pulled towards the pond green of the pack and desaturated so that a sea, a swamp and a river still differ. That colour decides what the water absorbs: each channel fades at its own rate (the dominant colour carries furthest, red dies first), more in the rain. The light falls with depth below the surface (the mod gives the surface height above the eye); light scattered by the water replaces the scene along the view, brighter when looking up. Floating specks (one hashed cell of 1.5 blocks in two holds a speck that sinks slowly, 18 steps, up to 20 blocks) catch the light. Seen from below, the surface shows the sky inside a circle (about 48° from the vertical, Snell's window) and turns into a dark mirror outside it; a bank or a tree beyond the surface stays visible. Beams from the surface and moving caustics on the bottom stay, fading with depth.
6. **Grade**: greens pulled towards olive, cool shadows and warm lights, bluish nights, golden dusks, grey rain, a darker day under a storm, a slightly softer contrast, corners barely darkened.

Two core shaders of the game are also replaced (`assets/minecraft/shaders/core/`): rain drops are paler and thinner (`particle`, recognised by a marker texel in `textures/environment/rain.png`), and lightning shows through the mist (`rendertype_lightning`).

## How the shadows work without a shadow map

A post effect only sees the screen and its depth. It cannot know that a tree outside the view hides the sun. The `relief` module of the `compline` mod ([mod](../../plan/mod-compline.md#relief-module)) keeps a 256×256 texture, `compline:textures/relief.png`, with the height of the surface (leaves included) of every column within 128 blocks of the camera. For each pixel the final pass rebuilds the position in the world from the depth, then walks towards the sun through that height field (26 steps, longer and longer, soft edge that widens with distance).

Limits of a height field: a point under an overhang or a canopy is always in shade, even when a low sun would reach it from the side; animals, players and Create contraptions cast no shadow; nothing beyond 128 blocks casts one. Polytone documents a real shadow map for post chains, which its 1.21.1 build (5.3.8) does not have yet: the day it does, the walk can be replaced.

Without the mod the pack still loads: the placeholder texture of the pack has no header, so shadows, reflections and the underwater look stay off and the rest works. The same texture carries the water data of the camera (biome colour, surface above the eye) in two more header texels.

The view ray is derived from the near plane, not the far plane: Distant Horizons pushes the far plane of the game so far that the usual far-plane reconstruction loses a quarter of the distance.

## Settings

Options > Resource Packs > the brush button (Polytone), section **Compline**. Names and tooltips are in French.

| Entry | Default | Effect |
|---|---|---|
| `rendu` | on | The whole chain; off gives back the image of the game |
| `ombres` | 1 (0 to 1.5) | Strength of the sun shadows |
| `rayons` | 1 (0 to 2) | Sun shafts |
| `halo` | 1 (0 to 2) | Glow of the bright lights |
| `air` | 1 (0 to 2) | Warm veil and cool distances |
| `tons` | 1 (0 to 2) | Grade |
| `olive` | 1 (0 to 2) | Olive greens |
| `eau` | 1 (0 to 1) | The water seen from inside; 0 keeps the game's water |
| `reflets` | 1 (0 to 2) | Sky and sun glint on the water |
| `brume` | 1 (0 to 2) | Low mist |

## With the other mods

- **Brumaire** (Iris): Polytone keeps running its chains under a shader pack. The `relief` module tells the pack when Iris draws the frame, and the final pass then copies the image untouched, so the two looks never add up.
- **Distant Horizons**: its distant terrain is not in the depth of the game, so it gets the grade and the sun shafts but no shadow and no air veil.
- **More Darkness**: untouched; the pack ships no lightmap, which would switch it off.
- **Where Winds Blow** already bends grass and leaves away from a player or an animal (`enableFoliageInteractivity`, on by default); **Particular Reforged** already drops and floats leaves.

## Compared with Recrafted

| Recrafted | Here |
|---|---|
| Sun with cast shadows, dappled light | Yes (height field, flecks under the leaves) |
| Light shafts, custom fog, colour maps | Shafts yes; the mist is ATMOSPHERICS', plus a low mist of ours; olive greens in the grade, no colour map |
| Underwater rendering | Yes: biome colour, absorption per colour, light falling with depth, floating specks, Snell's window. Not done: caustics that follow real waves, entity wakes, a mirror that shows the real bottom |
| Reflections on water | Sky and sun glint; not the scenery (no top-down map of the world) |
| Rain and storm look | Paler rain, lightning through the mist, grey and darker grade; no ground splashes of its own (Particular Reforged has them) |
| Interactive foliage, floating leaves | Where Winds Blow and Particular Reforged |
| Armor that gets dirty and washes off in water | Not yet: it needs code in the `compline` mod |
| Leaf litter, moths, petals, snow and sand storms | Not yet |

## Left to do

- Play with it on a machine with a graphics card: frame rate with and without (`rendu` off), shadows while walking, the sea shore, a village.
- Armor weathering, leaf litter, snow (the study of Recrafted's files of 2026-11-15 orders them in batches: this page covers batch 1, water and air).
- Lakes above sea level: the low mist uses a fixed base at 63, so a mountain lake gets none.
- A real shadow map when Polytone ships it on 1.21.1.
