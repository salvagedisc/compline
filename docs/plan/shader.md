# Brumaire, the house shader pack

Living page of the shader pack shipped in `pack/shaderpacks/Brumaire/` (decision [0028](../decisions/0028-house-shader.md)). "Brumaire" is a working name.

## Turning it on

Shaders are **off by default**. In game: Options > Video Settings > Shader Packs, or K (toggle), O (list), R (reload). Iris keeps a player's settings in `shaderpacks/Brumaire.txt`. Iris does not officially support Apple Silicon Macs: there the pack is meant to be played with the shader off.

## How a frame is built

| Step | Programs | What it does |
|---|---|---|
| Shadow map | `shadow`, `clrwl_shadow` | Sun or moon view, bent so the centre is finer; plants wave as in the main view; water casts no shadow |
| Geometry | `gbuffers_*`, `dh_terrain`, `dh_water`, `clrwl_gbuffers` | Every surface is lit where it is drawn: sun or moon with the shadow map, sky light, block light, the game's ambient occlusion |
| Sky | `deferred` | Paints the sky and a round sun behind the terrain; the moon and stars of the game (and of mods) are kept and added |
| Mist | `composite` | The mist, the light shafts read in the shadow map, the water seen from below |
| Screen | `final` | Glow of the bright lights, exposure, tone curve, grade |

All the geometry programs include `program/solid.vsh` and `program/solid.fsh` with one `PROGRAM_*` define each; the top-level files only hold that define.

| File | Content |
|---|---|
| `lib/settings.glsl` | Every setting shown in the Iris menu |
| `lib/common.glsl` | Time of day, colours of the light, sky, olive greens |
| `lib/shadow.glsl`, `lib/light.glsl` | Shadow map lookup, lighting of a surface |
| `lib/mist.glsl` | The mist: layer, clearings, far ring, colour |
| `lib/wind.glsl` | Movement of a plant vertex |
| `block.properties` | Which blocks wave, which are water, which glow |
| `lang/` | Names of the settings, French and English |

## The mist

- A layer whose density halves about every 17 blocks above the ground around the player (`MIST_HEIGHT`), integrated exactly along each ray: no steps, no noise.
- Each open clearing (the four nearest) removes the mist inside a vertical cylinder of its radius; the opening wave is followed.
- Standing in a clearing lifts part of the mist of the whole view (`MIST_LIFT`, 70 %).
- A ring closes the horizon at the render distance, or at the Distant Horizons distance capped by `MIST_RING_MAX` (512 blocks). It does not lift.
- No mist underground; more at night, at dawn and in the rain.

## What the `compline` mod gives the shader (`rendu` module)

A mixin on Iris adds uniforms any program may declare. Without the mod they read zero and the shader falls back.

| Uniform | Content | Fallback |
|---|---|---|
| `brumaireLink` | 1 when the module runs | 0 |
| `brumaireMist` | x: share of mist lifted around the camera; y: smoothed height of the ground | no lift; a little under the camera |
| `brumaireClearing0..3` | Centre seen from the camera, radius, strength of the four nearest clearings | no hole |
| `brumaireWind` | Direction, strength and turbulence of Where Winds Blow | a steady wind, stronger in the rain |
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
- Reflections of the terrain on water.
