# Brumaire, the house shader pack

Living page of the shader pack shipped in `pack/shaderpacks/Brumaire/` (decisions [0028](../decisions/0028-house-shader.md) and [0031](../decisions/0031-brumaire-is-the-look.md)). It is the look of the pack. "Brumaire" is a working name.

## Turning it on

Brumaire is **on from the first launch** (`config/iris.properties`, written once by Default Options). In game: K turns it off or on, O lists the shader packs, R reloads it; Options > Video Settings > Shader Packs holds its settings and profiles. Iris keeps a player's settings in `shaderpacks/Brumaire.txt`. Iris does not support Apple Silicon Macs: there the pack is played without shader, with the vanilla look and the mist of ATMOSPHERICS.

## How a frame is built

| Step | Programs | What it does |
|---|---|---|
| Shadow map | `shadow`, `clrwl_shadow` | Sun or moon view, bent so the centre is finer; plants wave as in the main view; water casts no shadow but marks itself (`shadowcolor0`), so the bottom knows it lies under water |
| Sky table | `prepare.csh` (compute) | The light scattered by the atmosphere in every direction around the sun, lit by the sun and the moon, in a 192×108 table; its last row holds the mean light of the sky and what the air leaves of the sun and the moon |
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
| `lib/mist.glsl` | The mist: layer, clearings, far ring, colour |
| `lib/water.glsl` | Colour, murk and waves of the water, caustics |
| `lib/reflect.glsl` | Screen reflections, the mirrored sky, the veil of air on a reflection (water and puddles) |
| `lib/rain.glsl` | Wet ground, puddles and their ripples, rain curtains |
| `lib/cover.glsl` | Snow laid on trees and ledges, the glitter of snow and sand |
| `lib/wind.glsl` | Movement of a plant vertex, grass pushed aside, slant of the rain |
| `lib/smaa.glsl`, `program/smaa.*`, `textures/smaa_*.png` | SMAA by Jorge Jimenez et al. (MIT, `textures/SMAA-LICENSE.txt`), unchanged but for its encoding, its lookup tables, and the three passes that use it |
| `block.properties` | Which blocks wave, which are water, which glow, which are snow, ice or sand |
| `lang/` | Names of the settings, French and English |

## Sky, light and air

- **The sky is physical**: air scatters blue (Rayleigh), fine haze scatters around the sun (Mie), ozone keeps the blue of the zenith at twilight. The sunset reddens by itself; the sun and the moon light the same atmosphere, the moon by its phase. A summer sky is never deep blue.
- **The light follows the sky**: the colour of the sun is what the air leaves of it (white at noon, orange then red near the horizon); the light of the sky is its mean over the vault; at night the eye sees blue and the night never goes fully black.
- **The air of the biome** (Iris' `rainfall` and `temperature` of the player's biome, eased over about ten seconds): a humid biome has a paler sky, more haze in the distance and more mist, olive in a swamp or a jungle; a cold one has a bluish mist; a hot dry one has dust, a yellowed sky and less mist.
- **Distant air** (`AIR_DENSITY`): the far landscape fades toward the low sky, blue first, into a warm grey rather than a pure sky blue.
- **Light in the air** (`LIGHT_SHAFTS`, `SHAFT_STRENGTH`): the air itself scatters a little sun, mostly forward; where foliage or a ridge cuts it in the shadow map, shafts. Only in the open.
- **The eye adapts**: the image brightens at night (up to 2.2×) and underground (1.5×).
- **Soft shadows** (`SHADOW_SOFTNESS`): the shader first looks how far the shadow caster is, and widens the edge with that distance, as under a tall tree. Leaves in the shade of other leaves still get some sun through the foliage.
- **Screen occlusion** (`AO_SAMPLES`, `AO_RADIUS`, `AO_STRENGTH`): only the light of the sky and of blocks is darkened, never the sun; half as much on foliage. Off in the Light profile.
- **Tone curve that keeps the hue**: the curve is applied to the intensity of a colour (a mix of its luminance and its strongest channel) instead of to each channel, so sand, snow or a sunlit field keep their colour instead of bleaching; only what still exceeds white fades to it, like film.
- **Grade** (calibrated on Recrafted screenshots, 2026-11-17): greens pulled toward a warm olive (`OLIVE`), a warm sun and a less blue sky light, shadows that keep their colour (more saturation in the darks than in the lights), a matte image (blacks barely lifted).

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
- Rain and snow slant with the wind, more in a storm.
- Without the mod, a steady fallback wind, stronger in the rain.

## Water

- **The waves** come from a few crossing wave trains pushed by the pack's wind (`WATER_WAVES`). With `WATER_PIXELS` they are read on the 1/16 block grid of the textures and their slope goes in steps: the reflections break into hard-edged patches, like a drawn texture.
- **What shows through**: the bottom, shifted by the waves, fades with the thickness of water crossed (red first) into the colour of the water, which is the biome's pulled toward a grey-green (`WATER_MURK`). Shallows show the bottom, a deep river shows its body.
- **What it mirrors**: the reflected ray is followed on screen until it meets the banks, the trees or a boat (`WATER_REFLECTIONS` steps; what lies under the surface cannot be mirrored); elsewhere the sky with its mist and the clouds at mid-height of their layer. More at a low angle, as in reality. The sun leaves a glint on the ripples, cut by shadows and clouds.
- **Under the surface**: the sun reaches the bottom weakened and tinted by the water above it (read in the shadow map), gathered by the waves into moving threads (`WATER_CAUSTICS`).
- **From below**: the world above shows through a circle (Snell's window), bent by the waves; outside it the surface mirrors the water and the bottom. The view under water fades into the same colour as from above, a little further.
- Distant Horizons water uses the same colour and the sky reflection, without the screen reflections.

## Rain

- **The drops** of the game are drawn as thin light-grey streaks lit by the sky, fainter than the game's blue ones and fading right in front of the eye; snow keeps its flakes.
- **Wet ground**: where it rains (the biome under the player has rain, not snow, read through `biome_precipitation`) and a surface sees the sky, it darkens and shines at a low angle (`WETNESS`). Iris smooths the rain into `wetness`: the ground soaks in about a minute and dries in a few minutes after the rain stops. Leaves, plants and glowing blocks stay as they are.
- **Puddles** gather on flat ground in patches with hard edges on the 1/16 block grid, larger as the ground soaks (`PUDDLES`, `PUDDLE_AMOUNT`). They mirror the landscape like the water, on screen (`WET_REFLECTIONS` steps) or the sky, and the drops draw rings on them. The terrain marks them in the alpha of `colortex1`; `deferred1` draws their reflection.
- **Rain curtains**: beyond the game's drops, thin falling streaks over the distant landscape, leaning with the wind (`RAIN_CURTAINS`).
- **The air** under rain turns a slightly warm grey and the mist thickens a little.
- **Lightning**: the bolt (`gbuffers_lightning`) is a bright blue-white line that glows; while it strikes, the sky and the whole landscape flash, less when it falls far away (`lightningBoltPosition`).

## Snow, sand and ice

- **Snow on trees and ledges**: while the player stands in a biome where it snows (`biome_precipitation` 2, smoothed over ten seconds into `snowHere`), the tops of blocks and leaves that see the sky turn white on the 1/16 grid. On leaves the snow holds on the bright needles and leaves the dark hollows, and a band of three to five pixels runs down their sides with a ragged lower edge (`SNOW_COVER`). The whole view follows the player's biome: a forest seen from a snowy biome is frosted too.
- **Snow** blocks are dimmed a little, so the snow keeps its grain in full sun instead of burning white; a few grains glitter where the sun reaches, and change from time to time.
- **Sand** keeps its warm colour in the sun (darker and slightly richer than the game's texture, otherwise it reads as snow) and glitters more sparsely.
- **Ice**: clear ice is drawn with the water, light blue, with the sky and the landscape mirrored in it, more at a low angle; packed and blue ice shine like a tarnished mirror (screen reflection, no ripples).
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
