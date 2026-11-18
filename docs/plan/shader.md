# Brumaire, the house shader pack

Living page of the shader pack shipped in `pack/shaderpacks/Brumaire/` (decisions [0028](../decisions/0028-house-shader.md) and [0031](../decisions/0031-brumaire-is-the-look.md)). It is the look of the pack. "Brumaire" is a working name.

## Turning it on

Brumaire is **on from the first launch** (`config/iris.properties`, written once by Default Options). In game: K turns it off or on, O lists the shader packs, R reloads it; Options > Video Settings > Shader Packs holds its settings and profiles. Iris keeps a player's settings in `shaderpacks/Brumaire.txt`. Iris does not support Apple Silicon Macs: there the pack is played without shader, with the vanilla look and the mist of ATMOSPHERICS.

## How a frame is built

| Step | Programs | What it does |
|---|---|---|
| Shadow map | `shadow`, `clrwl_shadow` | Sun or moon view, bent so the centre is finer; plants wave as in the main view; water casts no shadow but marks itself (`shadowcolor0`), so the bottom knows it lies under water; stained glass casts no shadow either but leaves the colour it gives the light (`shadowcolor1`) |
| Coloured light, shelter | `shadow`, `shadowcomp.csh`, `shadowcomp1.csh` (compute) | The shadow pass also files every terrain block within 64 blocks horizontally and 32 vertically into a 128×64×128 volume (`voxelImg`): solid, letting light through, or glowing with its colour; the compute pass spreads the light one block per frame, like the game's light, into two volumes used in turn (`lightImgA`, `lightImgB`); a second compute pass finds in each column the top of what stops the rain (`shelterImg`) |
| Sky table | `prepare.csh` (compute) | The light scattered by the atmosphere in every direction around the sun, lit by the sun and the moon, in a 192×108 table; its last row holds the mean light of the sky and what the air leaves of the sun and the moon |
| Footprints | `prepare1.csh` (compute) | Each of the four living beings nearest the camera (`brumairePush0..3`) sets a foot every 0.7 block, left then right, into a 1024×1024 map at 1/16 block that wraps every 64 blocks (`trackImg`); `trackStateImg` keeps where each set its last foot |
| Wakes | `prepare2.csh`, `prepare3.csh` (compute) | A damped wave equation on a 512×512 map at 1/8 block that wraps every 64 blocks, two half steps per frame (`wakeImgA` to `wakeImgB` and back): height, speed and foam; the water is hollowed under each boat, swimmer or wading animal (`brumaireWake0..5`) and given back as it moves on, so the wave equation turns the released hollow into rings when still and a V wake with foam when moving; steep ripples froth; reset flat far from the camera |
| Geometry | `gbuffers_*`, `dh_terrain`, `dh_water`, `clrwl_gbuffers` | Every surface is lit where it is drawn: sun or moon with soft shadows, sky light, block light, the game's ambient occlusion; solid surfaces also write their normal and their share of indirect light (`colortex1`) |
| Screen occlusion | `deferred` (half size), `deferred1` | Hollows, foliage and the foot of walls receive less sky light; smoothed without bleeding across edges; wet ground and puddles take their reflection; then the sky and a round sun are painted behind the terrain, the moon and stars of the game (and of mods) kept; the finished opaque scene is copied to `colortex6` for the water |
| Water | `gbuffers_water` | The surface is drawn whole where it lies: the bottom found at the end of the refracted ray (read in `colortex6`), dimmed along that path while the water scatters light into it, the banks and the sky mirrored in it, blurred where the ripples are finer than the pixel, the sun's glint and the foam |
| Clouds | `composite` (half size) | Volumetric clouds marched through a 3D noise computed once at load (`setup.csh`) |
| Air and mist | `composite1` | The far landscape takes the colour of the low sky, the mist, the light shafts read in the shadow map, the rain curtains beyond the game's drops, the water seen from below |
| Grade | `composite2` | Glow of the bright lights, exposure, tone curve, grade, into an 8-bit image (`colortex7`) |
| Temporal anti-aliasing | `composite3` | The image blended with its history (`colortex10`), brought back to where each point stood in the previous frame |
| Motion blur | `composite4` | The image streaked along the path each point took on screen since the previous frame |
| Anti-aliasing | `composite5`, `composite6`, `final` | SMAA: the edges of shapes (`colortex8`), their blending weights (`colortex9`), then the blend with the neighbours on screen |

All the geometry programs include `program/solid.vsh` and `program/solid.fsh` with one `PROGRAM_*` define each; the top-level files only hold that define.

| File | Content |
|---|---|
| `lib/settings.glsl` | Every setting shown in the Iris menu |
| `lib/common.glsl` | Time of day, colours of the light read in the sky table, sky, olive greens |
| `lib/atmosphere.glsl`, `program/sky.csh` | The atmosphere: constants, layout of the sky table, the program that fills it |
| `lib/shadow.glsl`, `lib/light.glsl` | Soft shadows, lighting of a surface |
| `program/ao.fsh`, `program/deferred1.fsh` | Screen occlusion and light bounce, contact shadows, sky |
| `lib/clouds.glsl`, `program/noise.csh`, `program/clouds.fsh` | Cloud kinds and density, cirrus, cloud shadows, the 3D noise, the cloud march |
| `lib/skyoptics.glsl`, `lib/nightsky.glsl` | Cirrus lighting, ice halos, rainbow; stars, Milky Way, moon aureole |
| `program/mist.fsh` | Clouds laid on the frame, distant air, mist, light shafts, under water |
| `lib/underwater.glsl` | Rays under the surface, marine snow |
| `lib/motes.glsl` | Suspended specks, one per block cell drawn by lot, walked cell by cell along the view ray (marine snow, dust in the light) |
| `lib/mist.glsl` | The mist: layer, clearings, far ring, colour |
| `lib/waves.glsl` | The wave spectrum: Gerstner trains in three bands, their slope, fold (Jacobian) and curvature (Hessian), filtered to the pixel, carried by the current |
| `lib/water.glsl` | Colour, extinction and scattering of the water, exact Fresnel, GGX, single scattering along a path, caustics from the wave curvature |
| `lib/waterfoam.glsl` | What surrounds a point of the water (bank, falling water, open water, read in the block volume), surf, churn under falls, whitecaps, rain on water, waterfall streaks, foam lace |
| `lib/watershade.glsl` | The water surface seen from above and below: refraction march, rough reflections, sun glint, foam |
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
- **Distant shadows** (`FAR_SHADOWS`: off, 16, 24 or 32 steps; off in the Very light profile): beyond the shadow map, hills, cliffs and forests still shade the far landscape, Distant Horizons included. The terrain the camera sees (`gbuffers_terrain` and `dh_terrain`, one pixel in four) writes into `farHeightImg` the highest surface seen in each column of two blocks; the map covers 4,096 blocks around the player and keeps what it has seen when the view turns away (each cell carries the rest of its position, so a cell taken over by a nearer column is recognised). A far point then climbs toward the sun over that map, with steps that lengthen up to 512 blocks, and is shaded where the relief passes in front, with a penumbra that widens with the distance of what hides it. Plants and vines are left out of the map. The map only keeps the highest surface seen: a tree cut far away keeps its shadow until the player has gone some 2,000 blocks away.
- **Light in the air** (`LIGHT_SHAFTS`, `SHAFT_STRENGTH`): the air itself scatters a little sun, mostly forward; where foliage or a ridge cuts it in the shadow map, shafts. Only in the open.
- **Dust in the light** (`AIR_MOTES`): specks of one block pixel float in about one block in three within 12 blocks; only those the shadow map says the sun reaches are drawn, warm with the light, faint with the sun behind and bright against it, and only against a dark background: in front of the sky or sunlit ground a speck is lost (the background is read in the shadow map), except straight against the sun. A beam through a window or a gap in the canopy shows its dust; the shade and the night show none. Off in the rain and in `Très léger`.
- **The eye adapts**: the image brightens at night (up to 2.2×) and underground (1.5×).
- **Soft shadows** (`SHADOW_SOFTNESS`, `SHADOW_SEARCH`): the shader first looks how far the shadow caster is (samples near the centre weigh more, so a distant edge does not blur a crisp shadow), and widens the edge with that distance, as under a tall tree.
- **Light through foliage** (`LEAF_TRANSLUCENCY`): the same search tells how much foliage stands between a leaf and the sun; the light that passes is fainter for each block crossed and tinted by the green of the leaves it went through, so a thin crown glows and a dense one stays dark inside. Against the light, leaves glow green, more where the canopy in front is thin.
- **Contact shadows** (`CONTACT_SHADOWS`, `deferred1`): within 48 blocks a short trace toward the sun on screen finds the small things the shadow map is too coarse for (grass, ledges, slabs, objects on a table); only the direct part of the light is darkened.
- **Sky light from a hemisphere**: faces turned up get the zenith, faces turned sideways the paler horizon, faces turned down the sun sent back by the ground (a warm green-brown), instead of one ambient colour scaled by the normal.
- **Light bounce** (`SSGI`, `SSGI_STRENGTH`, `deferred`): from each pixel, a few rays drawn by the cosine of the surface are walked on screen with lengthening steps over some four blocks; the first surface they meet sends back the light it shows in the lit image, received by the colour of the surface (written by the opaque programs into `colortex4`). A sunlit red floor turns the foot of a white wall pink, a sunlit wall brightens the ground in its shade, lava reddens the rock around it. Rays that leave the screen bring nothing back, so the bounce fades at the screen edges and beyond 96 blocks; it is smoothed with the occlusion, without crossing edges or faces.
- **Screen occlusion** (`AO_SAMPLES`, `AO_RADIUS`, `AO_STRENGTH`): only the light of the sky and of blocks is darkened, never the sun; half as much on foliage. Off in the Light profile.
- **Tone curve that keeps the hue**: the curve is applied to the intensity of a colour (a mix of its luminance and its strongest channel) instead of to each channel, so sand, snow or a sunlit field keep their colour instead of bleaching; only what still exceeds white fades to it, like film.
- **Grade** (calibrated on Recrafted screenshots, 2026-11-17): greens pulled toward a warm olive (`OLIVE`), a warm sun and a less blue sky light, shadows that keep their colour (more saturation in the darks than in the lights), a matte image (blacks barely lifted).

## Coloured light

- **Blocks light in their colour** (`COLORED_LIGHT`, off in the Very light profile): torches, lanterns, fire and campfires orange; soul torches, lanterns and fire blue; lava and magma red-orange; glowstone, shroomlight, lamps and ochre froglights warm yellow; sea lanterns, end rods and pearlescent froglights bluish white; verdant froglights and glow lichen green; amethyst, crying obsidian and portals violet; redstone red. Lights of other mods keep the warm colour of a flame.
- **How**: the shadow pass files the terrain around the camera into a volume; a compute pass spreads the light block by block like the game's own (one level lost per block, stopped by solid blocks, through glass, plants and partial blocks). Each cell keeps the colour of its strongest light; two lights of the same strength mix. Only the colour comes from the volume: the brightness stays the game's, which already knows what blocks the light.
- **Shaped block light** (`BLOCK_LIGHT_SHAPE`): the slope of the light level in the volume tells where the light comes from, so faces turned toward a torch get more of it than those turned away; flames (and lights of the flame colour) waver softly, each block on its own rhythm.
- **Light in hand**: what the player holds lights around them in its colour, a little further than a placed block, even without a dynamic-light mod; with LambDynamicLights the brighter of the two wins, never both.
- Beyond the volume (64 blocks around, Distant Horizons terrain) block light keeps the warm colour of a flame.
- **Stained glass light** (`GLASS_TINT`, off in the Very light profile): sunlight and moonlight that pass stained glass take its colour, on what they light and in the shafts of light in the air; the more opaque the pane, the purer the colour, and dark or tinted glass holds most of the light back. Read from the shadow map, so it reaches as far as the sun's shadows. It needs Iris Veil Compat 0.3.0: 0.4.0 leaves water and stained glass out of the shadow map.

## Clouds

- **One layer, several kinds**: from `CLOUD_HEIGHT` (220) to 90 blocks above, shaped by a 64³ noise that repeats without seams (smooth noise hollowed by cells for the shape, finer cells for the frayed edges), computed once when the pack loads. A slow weather map drifting with the wind decides, place by place, how covered the sky is and which kind grows there (`CLOUD_TYPES`): flat stratocumulus banks that stay low and even, round cumulus that billow up to two thirds of the layer, and under thunder towers that reach the top of the layer and spread into anvils. Light rain flattens them into a grey sheet, thunder raises them.
- **Shape**: flat bases, a sharp threshold so cumulus have clear edges with blue sky between. The fine cells, twisted by a slow swirl so their grid never shows, fray the bottoms and make cauliflower tops (the pattern is inverted with height). The upper wind pushes the top of the layer further than its base: towers lean downwind.
- **Light** (`CLOUD_LIGHT_STEPS`, `CLOUD_MULTISCATTER`): from each sample a cone of steps that double in length is walked toward the sun or the moon (fine detail on the first two), plus one look far ahead so a tower shades its neighbour. Multiple scattering is approximated by octaves, each less extinguished, less forward and weaker than the one before, so light glows through the heart of a cumulus instead of leaving it black. The droplet phase has a narrow forward peak (silver lining against the sun), a wide lobe and a backward lobe. The powder term darkens thin edges seen against the sun. The sky lights the tops more than the bellies, less under another cloud; the sunlit ground sends a warm-green bounce under the bellies.
- **Lightning** (`CLOUD_LIGHTNING`): a strike lights the column of cloud it crosses; under thunder, sheet lightning flickers in random cells of 400 blocks.
- **Aerial perspective**: far clouds fade into the low sky at the distance where they are actually seen (mean of the march weighted by what is taken), not where the ray enters the layer.
- **Cirrus** (`CLOUD_CIRRUS`): 760 blocks above the layer, a thin veil of ice streaks stretched along the upper wind (three octaves, the fine pulled by the coarse), coming and going over hours, more in humid weather and before rain. Ice scatters strongly forward: bright near the sun, barely brighter than the sky away from it, pink at sunset. It softens the sun on the ground a little.
- Pushed by the pack's wind; the coverage (`CLOUD_COVERAGE`) grows in humid biomes and in the rain, where they turn grey. The mist ring still closes the horizon.
- Their shadows pass over the ground (`CLOUD_SHADOWS`).
- The game's clouds are not drawn; with `CLOUD_STEPS` at 0 they come back.

## Sky optics and night sky

- **Ice halos** (`SKY_HALO`, `lib/skyoptics.glsl`): where cirrus crosses the sun or the moon, the 22° halo (red inside, sharp inner edge, fading outward) and a fainter 46° halo; with a low light, sun dogs at the same height left and right of it, a little beyond 22°.
- **Rainbow** (`SKY_RAINBOW`): around the antisolar point when the ground is still wet (`wetness`) and the rain has almost gone, with the sun low enough for the bow to stand above the horizon: the primary bow (40.5° to 42.4°, red outside), the secondary (50° to 53.5°, reversed, about a third as bright), a darker Alexander band between them and a brighter sky under the primary. It shows in patches, stronger low down where rain still falls.
- **Stars** (`SKY_STARS`, `lib/nightsky.glsl`): three layers drawn on a cube around the sky, each cell holding at most one star at a random place, with a brightness drawn from a steep law (a few bright, many faint) and a colour from orange to blue-white. They are fixed in a frame that turns with the sun around the celestial pole (tilted by `sunPathRotation`), so the night sky rotates. They twinkle, more near the horizon, and fade through the thicker air there. The game's stars are no longer drawn; at `Du jeu` they come back.
- **Milky Way** (`SKY_MILKY_WAY`): a band tilted across the celestial equator with a ragged width, a warmer bulge toward its centre, grain from unresolved stars, dark dust lanes slightly off its middle, and more faint stars along it. A full moon pales it and drowns the faintest stars.
- **Moon aureole**: haze scatters a soft glow around the moon, wider in humid biomes and in rain.
- Clouds and cirrus are laid over the stars and the bows (front to back: the bow, the cloud layer, the cirrus veil with its halos).

## Wind

- Grass, flowers, crops, leaves and vines move with the pack's wind (`vent` module of the `compline` mod): a slow swell travelling downwind and a quicker quiver, stronger in the gusts that cross the landscape (`WIND_STRENGTH`). Sheltered plants (little sky light) stay still.
- Grass and flowers lie down and part around the feet of the four living beings nearest the camera, the player included, even out of the wind.
- Leaves brushed by one of those beings (walking along a hedge, standing on a canopy) lean away from it and quiver quickly while it stays against them, even out of the wind. Falling leaves stay Particular's particles: textured, they read better than leaves drawn by the shader, which were tried and dropped.
- Rain and snow slant with the wind, more in a storm.
- Without the mod, a steady fallback wind, stronger in the rain.

## Water

- **The waves** are a spectrum of Gerstner wave trains in three bands (`lib/waves.glsl`): a swell of 6 to 20 blocks, wind waves of 1.2 to 6 blocks and ripples of a quarter block to one block (`WATER_WAVE_COUNT` trains, 8 to 28, shared a quarter, a half and a quarter). Each train follows the deep-water dispersion (a long wave travels faster) and its crest wavers along its length. The directions are fixed once and spread around the circle; the wind only gives more height to the trains that go its way (narrowly for the swell, loosely for the ripples), so nothing slides when the wind turns. The swell needs open water: eight rays walk the water around each point through the block volume, and a pond enclosed by its banks keeps only the wind waves (`WATER_SWELL`). Waves flatten over shallows. The Gerstner displacement gathers the water under the crests and sharpens them (`WATER_CHOP`); the Jacobian of that displacement tells where the surface folds over. Trains finer than the pixel fade out and their slope becomes roughness, so distant water blurs instead of shimmering. `WATER_WAVES` scales them all. With `WATER_PIXELS` the waves are read on the 1/16 block grid of the textures and their slope goes in steps: the reflections break into hard-edged patches, like a drawn texture.
- **Current** (`WATER_FLOW`): the slope of a flowing water face gives the downhill direction; the wave pattern is carried along it in two phases half a period apart that take turns, so it flows forever without stretching, and the ripples line up with the current. Fast water foams.
- **What shows through**: the refracted ray is followed on screen down to the bottom (`WATER_REFRACTION` steps then five halvings; 0 falls back to a cheap offset), so the bottom shifts like through real water and nothing above the surface (a boat, a plant) leaks into it. Along that path the light of the bottom is dimmed (red first) and the water adds the light it scatters itself, integrated exactly for one scattering: the sun that went down through the surface, more toward the sun (forward scattering) and less looking straight down, and the sky light. The colour is the biome's pulled toward a grey-green (`WATER_MURK`). Shallows show the bottom, a deep river shows its body; looking toward a low sun the crests glow green where the light passes through them.
- **What it mirrors**: exact Fresnel for water (a little boosted, the water reads first by its reflections). The reflected ray is followed on screen until it meets the banks, the trees or a boat (`WATER_REFLECTIONS` steps; what lies under the surface cannot be mirrored). The crossing point is refined, then checked: a ray that passed behind a trunk or a bank without touching it goes on instead of stretching that object downward. Where the screen does not show what is mirrored (the bank behind the player, a tree above the frame, something hidden), the ray goes on block by block through the 128 × 64 × 128 volume around the camera that the shadow pass fills for the coloured light, and the first block it meets gives its average colour (top or side, kept by the shadow pass from the smallest mip of its texture, biome tint included), lit by the sun with its shadow, the sky unless a block covers it within eight blocks and the coloured light around it; a light block glows in the reflection (`VOXEL_REFLECTIONS` blocks, also for ice and puddles). Only masses and colours, no texture detail, which is enough at the distance where the screen no longer shows the thing; small plants and torches are left out. Beyond the volume, the sky with its mist and the clouds at mid-height of their layer. Where the ripples are finer than the pixel, or under rain, each frame mirrors along a microfacet drawn from a GGX distribution of that roughness, and the anti-aliasing averages them into a blurred reflection (`WATER_BLUR`).
- **The sun's glint** is a GGX microfacet highlight, never narrower than the sun's disc, cut by shadows and clouds and dulled by rain: a path of glitter toward a low sun, hard-edged plates with `WATER_PIXELS`.
- **Wakes and foam**: what crosses the water ripples it (`WATER_WAKES`): a boat, a swimmer or an animal wading in pushes the surface down, still it leaves small rings, moving a wake whose ripples bend the reflections and a trail of foam that fades in a few seconds; ripples that break also froth. Along the banks (`WATER_FOAM`, solid blocks at water level found in the block volume, within two blocks) a fringe breathes against the shore and bands of foam roll in from open water and die on it, stronger with wind and swell. Under a waterfall the water churns: foam and rings around where it lands (`WATER_FALLS`). In strong wind or storms on open water, crests where the surface folds over break into whitecaps, and the foam trails behind them for a second or two (`WATER_WHITECAPS`). All foam is drawn as a lace of cells that opens when there is little and closes into a sheet when there is much, on the pixel grid with `WATER_PIXELS`; it takes the daylight and the sky, without reflection.
- **Rain on water** (`WATER_RAIN_RINGS`): drops ring the surface at two sizes and flick a dot of foam where they hit.
- **Waterfalls**: a side face of water with water above it (the block volume marks water, bit 12) is falling water: thin, whitened by the air it carries, streaked by dashes that run down each 1/16 column at their own speed. The edge of water that merely flows only trembles.
- **Under the surface**: the sun reaches the bottom weakened and tinted by the water above it (read in the shadow map). The caustics come from the waves themselves: the light of a patch of surface lands on a patch deformed by the surface's curvature, and its brightness is divided by the area; the strength is set from the average curvature so the threads close up from the first block of water, softer on calm water (`WATER_CAUSTICS`). Red and blue bend a little differently: the edges of the threads catch a rainbow (`WATER_DISPERSION`).
- **From below**: the world above shows through a circle (Snell's window), bent by the waves, its edge darkening; beyond the critical angle the surface reflects everything (total internal reflection) and mirrors the water and the bottom. The view under water scatters the same light as from above along the eye's path, a little further, brighter toward the surface and the sun; it stays clear near the surface and darkens with each block of water over the eye (the sky light at the eye).
- **Diving**: rays come down from the surface, slanted by refraction, cut by the shadow of what floats or overhangs and rippling with the caustics, brightest when looking toward the light (`LIGHT_SHAFTS`, `SHAFT_STRENGTH`). Suspended specks of one block pixel (marine snow) drift slowly in about one block in three within 20 blocks and twinkle, brighter against the light (`WATER_MOTES`).
- Distant Horizons water uses the same scattering, Fresnel and sky reflection, and since its waves are all finer than the pixel their whole slope becomes roughness: a wide path of light under the sun.

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
- **Wet prints**: a being that walks out of water (`brumaireWet`, from the `compline` mod) leaves wet soles on any ground, sand included, snow and ice excepted: darker and slightly glossy, fewer and fainter as its feet dry over some 24 blocks. Each print dries in about two minutes, faster in the sun, from its edges inward. The wetness is kept in the print's texel (`a` = tile + 256 × wetness 0..6). Under rain the ground is already wet and the prints are not drawn.
- **Heat shimmer**: in a dry, hot biome by day, the air near the horizon trembles a few pixels, more with distance (`HEAT_SHIMMER`). A desert also clears the near mist.

## Anti-aliasing and profiles

- **SMAA** (`SMAA`: off, medium, high, ultra; on the first page of the settings) smooths the stair-stepped silhouettes of blocks, plants and creatures. Edges are found on the luminance of the finished image, then kept only where the depth jumps by more than 2 % between neighbours: the pixels of the textures stay sharp, only shapes are smoothed. The grade moved from `final` to `composite2` so that SMAA works on the image as it will be shown.
- **Temporal anti-aliasing** (`TAA`, off in the Very light profile): the camera is shifted by a fraction of a pixel each frame (eight positions), and `composite3` blends the new frame with the history, reprojected from the depth and the previous camera. The history is clipped to the box of the current neighbours (mean and spread in YCoCg) and read with a Catmull-Rom filter, so a moving shape leaves no trail and the history does not soften frame after frame. Pixels of texture larger than a screen pixel keep mostly the current frame and stay sharp; the full blend only applies where the texture's pixels become finer than the screen, in the distance, where they shimmered. `final` then gives back a little of the texture grain, clamped to the neighbouring colours so no light rim appears. The hand is not reprojected, the sky only follows the camera's rotation.
- **Motion blur** (`MOTION_BLUR`: off, light, medium, strong, very strong; medium by default, off in the Very light profile): `composite4` takes twelve samples along the path each point took on screen since the previous frame (`lib/reproject.glsl`, shared with the temporal anti-aliasing), over a quarter of a frame per step of the setting, so the streak follows the frame rate like a film camera's shutter. It runs after the temporal anti-aliasing so the history stays sharp. The streak is capped at 6 % of the screen height, tiny motions (walking, head bob) barely blur, the hand never blurs and never bleeds into the landscape. Only the camera's motion counts: an animal running across a still view stays sharp.
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
