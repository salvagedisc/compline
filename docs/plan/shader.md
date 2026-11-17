# Brumaire, the house shader pack

Living page of the shader pack shipped in `pack/shaderpacks/Brumaire/` (decisions [0028](../decisions/0028-house-shader.md) and [0031](../decisions/0031-brumaire-is-the-look.md)). It is the look of the pack. "Brumaire" is a working name.

## Turning it on

Brumaire is **on from the first launch** (`config/iris.properties`, written once by Default Options). In game: K turns it off or on, O lists the shader packs, R reloads it; Options > Video Settings > Shader Packs holds its settings and profiles. Iris keeps a player's settings in `shaderpacks/Brumaire.txt`. Iris does not support Apple Silicon Macs: there the pack is played without shader, with the vanilla look and the mist of ATMOSPHERICS.

## How a frame is built

| Step | Programs | What it does |
|---|---|---|
| Shadow map | `shadow`, `clrwl_shadow` | Sun or moon view, bent so the centre is finer; plants wave as in the main view; water casts no shadow but marks itself (`shadowcolor0`), so the bottom knows it lies under water |
| Coloured light, shelter | `shadow`, `shadowcomp.csh`, `shadowcomp1.csh` (compute) | The shadow pass also files every terrain block within 64 blocks horizontally and 32 vertically into a 128×64×128 volume (`voxelImg`): solid, letting light through, or glowing with its colour; the compute pass spreads the light one block per frame, like the game's light, into two volumes used in turn (`lightImgA`, `lightImgB`); a second compute pass finds in each column the top of what stops the rain (`shelterImg`) |
| Sky table | `prepare.csh` (compute) | The light scattered by the atmosphere in every direction around the sun, lit by the sun and the moon, in a 192×108 table; its last row holds the mean light of the sky and what the air leaves of the sun and the moon |
| Footprints | `prepare1.csh` (compute) | Each of the four living beings nearest the camera (`brumairePush0..3`) sets a foot every 0.7 block, left then right, into a 1024×1024 map at 1/16 block that wraps every 64 blocks (`trackImg`); `trackStateImg` keeps where each set its last foot |
| Wakes | `prepare2.csh`, `prepare3.csh` (compute) | A damped wave equation on a 512×512 map at 1/8 block that wraps every 64 blocks, two half steps per frame (`wakeImgA` to `wakeImgB` and back): height, speed and foam; the water is hollowed under each boat, swimmer or wading animal (`brumaireWake0..5`) and given back as it moves on, so the wave equation turns the released hollow into rings when still and a V wake with foam when moving; steep ripples froth; reset flat far from the camera |
| Geometry | `gbuffers_*`, `dh_terrain`, `dh_water`, `clrwl_gbuffers` | Every surface is lit where it is drawn: sun or moon with soft shadows, sky light, block light, the game's ambient occlusion; solid surfaces also write their normal and their share of indirect light (`colortex1`) |
| Screen occlusion | `deferred` (half size), `deferred1` | Hollows, foliage and the foot of walls receive less sky light; smoothed without bleeding across edges; wet ground and puddles take their reflection; then the sky and a round sun are painted behind the terrain, the moon and stars of the game (and of mods) kept; the finished opaque scene is copied to `colortex6` for the water |
| Water | `gbuffers_water` | The surface is drawn whole where it lies: the bottom seen through it (read in `colortex6`, bent by the waves), dimmed by the thickness of water, the banks and the sky mirrored in it |
| Clouds | `composite` (half size) | Volumetric clouds marched through a 3D noise computed once at load (`setup.csh`) |
| Air and mist | `composite1` | The far landscape takes the colour of the low sky, the mist, the light shafts read in the shadow map, the rain curtains beyond the game's drops, the water seen from below |
| Grade | `composite2` | Glow of the bright lights, exposure, tone curve, grade, into an 8-bit image (`colortex7`) |
| Anti-aliasing | `composite3`, `composite4`, `final` | SMAA: the edges of shapes (`colortex8`), their blending weights (`colortex9`), then the blend with the neighbours on screen |

All the geometry programs include `program/solid.vsh` and `program/solid.fsh` with one `PROGRAM_*` define each; the top-level files only hold that define.

| File | Content |
|---|---|
| `lib/settings.glsl` | Every setting shown in the Iris menu |
| `lib/common.glsl` | Time of day, colours of the light read in the sky table, sky, olive greens |
| `lib/atmosphere.glsl`, `program/sky.csh` | The atmosphere: constants, layout of the sky table, the program that fills it |
| `lib/shadow.glsl`, `lib/light.glsl` | Soft shadows, lighting of a surface |
| `program/ao.fsh`, `program/deferred1.fsh` | Screen occlusion, sky |
| `lib/clouds.glsl`, `program/noise.csh`, `program/clouds.fsh` | Cloud density, cloud shadows, the 3D noise, the cloud march |
| `program/mist.fsh` | Clouds laid on the frame, distant air, mist, light shafts, under water |
| `lib/underwater.glsl` | Rays under the surface, marine snow |
| `lib/motes.glsl` | Suspended specks, one per block cell drawn by lot, walked cell by cell along the view ray (marine snow, dust in the light) |
| `lib/mist.glsl` | The mist: layer, clearings, far ring, colour |
| `lib/water.glsl` | Colour, murk and waves of the water, caustics |
| `lib/reflect.glsl` | Screen reflections, the mirrored sky, the veil of air on a reflection (water and puddles) |
| `lib/voxtrace.glsl` | Off-screen reflections: the reflected ray walked block by block in the coloured-light volume, with the average colour of each block's top and sides |
| `lib/rain.glsl` | Wet ground, puddles and their ripples, rain curtains |
| `lib/precip.glsl`, `program/shelter.csh` | Drops and flakes drawn by the shader, the shelter of roofs and trees, the showers |
| `lib/cover.glsl` | Snow laid on trees and ledges, frost, the glitter of snow and sand |
| `lib/tracks.glsl`, `program/tracks.csh` | Footprints: the map, the stamping of a step, the hollow and its tilted edges on the ground |
| `lib/wind.glsl` | Movement of a plant vertex, grass pushed aside, leaves brushed, slant of the rain |
| `lib/smaa.glsl`, `program/smaa.*`, `textures/smaa_*.png` | SMAA by Jorge Jimenez et al. (MIT, `textures/SMAA-LICENSE.txt`), unchanged but for its encoding, its lookup tables, and the three passes that use it |
| `lib/voxel.glsl`, `program/lightfill.csh` | Coloured light: the volume, the colours of the lights, the spread, the light held in hand |
| `block.properties` | Which blocks wave, which are water, which glow and in which colour, which are snow, ice, sand, glass or partial blocks that let light through |
| `item.properties` | Which held items glow, and in which colour |
| `lang/` | Names of the settings, French and English |

## Sky, light and air

- **The sky is physical**: air scatters blue (Rayleigh), fine haze scatters around the sun (Mie), ozone keeps the blue of the zenith at twilight. The sunset reddens by itself; the sun and the moon light the same atmosphere, the moon by its phase. A summer sky is never deep blue.
- **The light follows the sky**: the colour of the sun is what the air leaves of it (white at noon, orange then red near the horizon); the light of the sky is its mean over the vault; at night the eye sees blue and the night never goes fully black.
- **The air of the biome** (Iris' `rainfall` and `temperature` of the player's biome, eased over about ten seconds): a humid biome has a paler sky, more haze in the distance and more mist, olive in a swamp or a jungle; a cold one has a bluish mist; a hot dry one has dust, a yellowed sky and less mist.
- **Distant air** (`AIR_DENSITY`): the far landscape fades toward the low sky, blue first, into a warm grey rather than a pure sky blue.
- **Light in the air** (`LIGHT_SHAFTS`, `SHAFT_STRENGTH`): the air itself scatters a little sun, mostly forward; where foliage or a ridge cuts it in the shadow map, shafts. Only in the open.
- **Dust in the light** (`AIR_MOTES`): specks of one block pixel float in about one block in three within 12 blocks; only those the shadow map says the sun reaches are drawn, warm with the light, faint with the sun behind and bright against it, and only against a dark background: in front of the sky or sunlit ground a speck is lost (the background is read in the shadow map), except straight against the sun. A beam through a window or a gap in the canopy shows its dust; the shade and the night show none. Off in the rain and in `Très léger`.
- **The eye adapts**: the image brightens at night (up to 2.2×) and underground (1.5×).
- **Soft shadows** (`SHADOW_SOFTNESS`): the shader first looks how far the shadow caster is, and widens the edge with that distance, as under a tall tree. Leaves in the shade of other leaves still get some sun through the foliage.
- **Screen occlusion** (`AO_SAMPLES`, `AO_RADIUS`, `AO_STRENGTH`): only the light of the sky and of blocks is darkened, never the sun; half as much on foliage. Off in the Light profile.
- **Tone curve that keeps the hue**: the curve is applied to the intensity of a colour (a mix of its luminance and its strongest channel) instead of to each channel, so sand, snow or a sunlit field keep their colour instead of bleaching; only what still exceeds white fades to it, like film.
- **Grade** (calibrated on Recrafted screenshots, 2026-11-17): greens pulled toward a warm olive (`OLIVE`), a warm sun and a less blue sky light, shadows that keep their colour (more saturation in the darks than in the lights), a matte image (blacks barely lifted).

## Coloured light

- **Blocks light in their colour** (`COLORED_LIGHT`, off in the Very light profile): torches, lanterns, fire and campfires orange; soul torches, lanterns and fire blue; lava and magma red-orange; glowstone, shroomlight, lamps and ochre froglights warm yellow; sea lanterns, end rods and pearlescent froglights bluish white; verdant froglights and glow lichen green; amethyst, crying obsidian and portals violet; redstone red. Lights of other mods keep the warm colour of a flame.
- **How**: the shadow pass files the terrain around the camera into a volume; a compute pass spreads the light block by block like the game's own (one level lost per block, stopped by solid blocks, through glass, plants and partial blocks). Each cell keeps the colour of its strongest light; two lights of the same strength mix. Only the colour comes from the volume: the brightness stays the game's, which already knows what blocks the light.
- **Light in hand**: what the player holds lights around them in its colour, a little further than a placed block, even without a dynamic-light mod; with LambDynamicLights the brighter of the two wins, never both.
- Beyond the volume (64 blocks around, Distant Horizons terrain) block light keeps the warm colour of a flame.

## Clouds

- One layer of cumulus from `CLOUD_HEIGHT` (220) to 90 blocks above, shaped by a 64³ noise that repeats without seams (smooth noise hollowed by cells for the shape, finer cells for the frayed edges), computed once when the pack loads.
- Flat bases, rounded tops, clear edges with blue sky between; large masses come and go with a slower, stretched slice of the same noise.
- Lit by the sun or the moon through four longer and longer steps toward it (darker undersides, bright edges toward the sun), and by the light of the sky, stronger on their tops.
- Pushed by the pack's wind; the coverage (`CLOUD_COVERAGE`) grows in humid biomes and in the rain, where they turn grey. Far away they fade into the low sky; the mist ring still closes the horizon.
- Their shadows pass over the ground (`CLOUD_SHADOWS`).
- The game's clouds are not drawn; with `CLOUD_STEPS` at 0 they come back.

## Wind

- Grass, flowers, crops, leaves and vines move with the pack's wind (`vent` module of the `compline` mod): a slow swell travelling downwind and a quicker quiver, stronger in the gusts that cross the landscape (`WIND_STRENGTH`). Sheltered plants (little sky light) stay still.
- Grass and flowers lie down and part around the feet of the four living beings nearest the camera, the player included, even out of the wind.
- Leaves brushed by one of those beings (walking along a hedge, standing on a canopy) lean away from it and quiver quickly while it stays against them, even out of the wind. Falling leaves stay Particular's particles: textured, they read better than leaves drawn by the shader, which were tried and dropped.
- Rain and snow slant with the wind, more in a storm.
- Without the mod, a steady fallback wind, stronger in the rain.

## Water

- **The waves** come from a few crossing wave trains pushed by the pack's wind (`WATER_WAVES`). With `WATER_PIXELS` they are read on the 1/16 block grid of the textures and their slope goes in steps: the reflections break into hard-edged patches, like a drawn texture.
- **What shows through**: the bottom, shifted by the waves, fades with the thickness of water crossed (red first) into the colour of the water, which is the biome's pulled toward a grey-green (`WATER_MURK`). Shallows show the bottom, a deep river shows its body.
- **What it mirrors**: the reflected ray is followed on screen until it meets the banks, the trees or a boat (`WATER_REFLECTIONS` steps; what lies under the surface cannot be mirrored); where the screen does not show what is mirrored (the bank behind the player, a tree above the frame, something hidden), the ray goes on block by block through the 128 × 64 × 128 volume around the camera that the shadow pass fills for the coloured light, and the first block it meets gives its average colour (top or side, kept by the shadow pass from the smallest mip of its texture, biome tint included), lit by the sun with its shadow, the sky unless a block covers it within eight blocks and the coloured light around it; a light block glows in the reflection (`VOXEL_REFLECTIONS` blocks, also for ice and puddles). Only masses and colours, no texture detail, which is enough at the distance where the screen no longer shows the thing; small plants and torches are left out. Beyond the volume, the sky with its mist and the clouds at mid-height of their layer. More at a low angle, as in reality. The sun leaves a glint on the ripples, cut by shadows and clouds.
- **Wakes and foam**: what crosses the water ripples it (`WATER_WAKES`): a boat, a swimmer or an animal wading in pushes the surface down, still it leaves small rings, moving a wake whose ripples bend the reflections and a trail of foam pixels that fades in a few seconds; ripples that break also froth. Along the banks, a narrow broken fringe of foam pixels comes and goes against every solid block at water level (`WATER_FOAM`, read in the coloured-light volume). Foam takes the daylight and the sky, without reflection.
- **Under the surface**: the sun reaches the bottom weakened and tinted by the water above it (read in the shadow map), gathered by the waves into moving threads (`WATER_CAUSTICS`).
- **From below**: the world above shows through a circle (Snell's window), bent by the waves; outside it the surface mirrors the water and the bottom. The view under water fades into the same colour as from above, a little further; it stays clear near the surface and darkens with each block of water over the eye (the sky light at the eye).
- **Diving**: rays come down from the surface, slanted by refraction, cut by the shadow of what floats or overhangs and rippling with the same pattern as the caustics, brightest when looking toward the light (`LIGHT_SHAFTS`, `SHAFT_STRENGTH`). Suspended specks of one block pixel (marine snow) drift slowly in about one block in three within 20 blocks and twinkle, brighter against the light (`WATER_MOTES`).
- Distant Horizons water uses the same colour and the sky reflection, without the screen reflections.

## Rain

- **The drops** of the game are drawn as thin light-grey streaks lit by the sky, fainter than the game's blue ones and fading right in front of the eye; snow keeps its flakes.
- **Wet ground**: where it rains (the biome under the player has rain, not snow, read through `biome_precipitation`) and a surface sees the sky, it darkens and shines at a low angle (`WETNESS`). Iris smooths the rain into `wetness`: the ground soaks in about a minute and dries in a few minutes after the rain stops. Leaves, plants and glowing blocks stay as they are.
- **Puddles** gather on flat ground in patches with hard edges on the 1/16 block grid, larger as the ground soaks (`PUDDLES`, `PUDDLE_AMOUNT`). They mirror the landscape like the water, on screen (`WET_REFLECTIONS` steps) or the sky, and the drops draw rings on them. The terrain marks them in the alpha of `colortex1`; `deferred1` draws their reflection.
- **Rain curtains**: beyond the game's drops, thin falling streaks over the distant landscape, leaning with the wind (`RAIN_CURTAINS`).
- **The air** under rain turns a slightly warm grey and the mist thickens a little.
- **Lightning**: the bolt (`gbuffers_lightning`) is a bright blue-white line that glows; while it strikes, the sky and the whole landscape flash, less when it falls far away (`lightningBoltPosition`).

### Rain and snow drawn by the shader

- **Drops and flakes** (`SHADER_RAIN`, off in the Very light profile, where the game's rain stays): the game's rain and snow are hidden and the shader draws its own in `composite1` (`lib/precip.glsl`): one drop or flake per column of three quarters of a block, found by walking the columns the view crosses up to 26 blocks; drops fall on the sixteenth-of-a-block grid like the pixels of the textures, one pixel wide, lit by the sky; flakes are pixel squares that drift. The wind slants them.
- **Shelter**: a compute pass (`shadowcomp1`, `program/shelter.csh`) finds in the coloured-light volume, for each column, the top of the highest block that stops rain (everything but grass, flowers, vines and torches); no drop falls below it, so nothing falls under a roof or a dense tree.
- **Showers**: patches of about a hundred blocks pushed by the wind: the rain grows heavier and lighter as they pass, and the far rain curtains are thicker where it rains hard.
- In a snowy biome (the player's), flakes instead of drops.

## Snow, sand and ice

- **Snow on trees and ledges**: while the player stands in a biome where it snows (`biome_precipitation` 2, smoothed over ten seconds into `snowHere`), the tops of blocks and leaves that see the sky turn white on the 1/16 grid. On leaves the snow holds on the bright needles and leaves the dark hollows, and a band of three to five pixels runs down their sides with a ragged lower edge (`SNOW_COVER`). The whole view follows the player's biome: a forest seen from a snowy biome is frosted too.
- **Snow** blocks are dimmed a little, so the snow keeps its grain in full sun instead of burning white; a few grains glitter where the sun reaches, and change from time to time.
- **Sand** keeps its warm colour in the sun (darker and slightly richer than the game's texture, otherwise it reads as snow) and glitters more sparsely.
- **Ice**: clear ice is drawn with the water, light blue, with the sky and the landscape mirrored in it, more at a low angle; packed and blue ice shine like a tarnished mirror (screen reflection, no ripples).
- **Frost** (`FROST`): in a cool biome (taiga, windswept hills; barely in the plains, never in a warm one) on a clear night, grass blocks, grass, flowers and leaves that see the sky take a pale veil and a rime on their brightest texels, about a third of them at the coldest, following the drawing of the texture on the 1/16 grid. It forms from dusk, holds at sunrise and melts by mid-morning; none under rain, where snow already covers, or near a light.
- **Footprints**: the player and the animals nearest to them leave a print every 0.7 block, left then right, on sand, snow blocks, snow layers and snow-covered ground. A print is a sole four pixels wide on the 1/16 grid, wider at the front than at the heel, turned in the walking direction and sized by the being's width. Its hollow is darker (bluish on snow, brown on sand) and its edges tilt the normal, so one side catches the light and the other falls in shade. A print records the foot's height, so a flying player or a block placed on top shows none. Prints fade over `TRACK_MINUTES` (4 by default), faster under rain or falling snow; the map covers 64 blocks around the camera, so a trail left farther away is forgotten when the player comes back (`TRACKS`).
- **Heat shimmer**: in a dry, hot biome by day, the air near the horizon trembles a few pixels, more with distance (`HEAT_SHIMMER`). A desert also clears the near mist.

## Anti-aliasing and profiles

- **SMAA** (`SMAA`: off, medium, high, ultra; on the first page of the settings) smooths the stair-stepped silhouettes of blocks, plants and creatures. Edges are found on the luminance of the finished image, then kept only where the depth jumps by more than 2 % between neighbours: the pixels of the textures stay sharp, only shapes are smoothed. The grade moved from `final` to `composite2` so that SMAA works on the image as it will be shown.
- **Four profiles**: Très léger (very light: no reflections, no screen occlusion, no shafts, the game's clouds, 1024 shadow map over 64 blocks with hard edges, no anti-aliasing), Léger, Équilibré (the default) and Beau. Every profile sets every option it touches, so switching back and forth restores them.

## The mist

- A layer whose density halves about every 11 blocks above the ground around the player (`MIST_HEIGHT`), integrated exactly along each ray: no steps, no noise.
- Each open clearing (the four nearest) removes the mist inside a vertical cylinder of its radius; the opening wave is followed.
- Standing in a clearing lifts part of the mist of the whole view (`MIST_LIFT`, 70 %).
- A ring closes the horizon at the render distance, or at the Distant Horizons distance capped by `MIST_RING_MAX` (512 blocks). It does not lift.
- No mist underground; barely there at noon (near air stays clear, as in Recrafted), more at night, thickest at dawn and dusk and in the rain; more in a humid biome, almost none near the player in a desert.
- Its light: the light of the sky, and the sun scattered mostly forward (a halo when looking toward the sun). The far part (the ring) takes the colour of the low sky in that direction: at sunset it glows on the side of the sun.

## What the `compline` mod gives the shader (`rendu` module)

A mixin on Iris adds uniforms any program may declare. Without the mod they read zero and the shader falls back.

| Uniform | Content | Fallback |
|---|---|---|
| `brumaireLink` | 1 when the module runs | 0 |
| `brumaireMist` | x: share of mist lifted around the camera; y: smoothed height of the ground | no lift; a little under the camera |
| `brumaireClearing0..3` | Centre seen from the camera, radius, strength of the four nearest clearings | no hole |
| `brumaireWind` | Direction, strength and turbulence of the pack's wind (`vent` module) | a steady wind, stronger in the rain |
| `brumairePush0..3` | Feet and reach of the living beings nearest the camera | no grass pushed aside |
| `brumaireWake0..5`, `brumaireWakeSpeed0..1` | Surface point, half width and speed of the boats and swimmers nearest the camera | no wakes |
| `brumaireGust0..1` | The two gust fronts nearest to passing | no gusts |

## Other mods

- **Distant Horizons**: `dh_terrain` and `dh_water` light the far landscape like the near one; it gives way to real chunks near the camera.
- **Create (Flywheel)**: Colorwheel refuses a shader pack without `clrwl_gbuffers`; ours lights Flywheel models like entities and gives them a shadow.
- **Sable and Aeronautics (Veil)**: Iris Veil Compat builds its programs from ours; levitite compiles against the block, entity and shadow programs.
- **LambDynamicLights** provides held lights; the shader adds none.

## Verified on the bench (full pack, software rendering)

The pack loads without error, day, sunset, night and rain render, a clearing opened with `/compline clairiere 32` opens the mist, a Create water wheel, a cow, glowstone and a lantern are lit at night. Frame rate was not measured: the bench has no graphics card.

## To do

- Play it on real hardware and measure each profile.
- Wave the plants of the biome mods (extend `block.properties`).
- Sort translucent Flywheel models (Colorwheel's `oit`).
- Nether and End variants, if those dimensions open one day: today every dimension uses the Overworld programs.
- Colours of the lunar events of Enhanced Celestials (blood moon…): the shader paints its own sky and ignores them for now. The prompt of its shader add-on is hidden by `config/enhancedcelestials2core/shaders-prompt.json`.
