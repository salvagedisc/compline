# 0018 — Weather and light effects moved into the resource pack

- **Date**: 2026-11-03
- **Status**: kept (the team's request of 2026-11-03); rain colour and sun rays seen in a headless test client (Sodium, Polytone, Where Winds Blow, ATMOSPHERICS, Distant Horizons, `tilth`), not played; the `averse` and `rayons` modules are removed by [0026](0026-second-cleanup.md) (the resource pack left in [0024](0024-removing-the-house-resource-packs.md))

## Context
The team pointed out that Recrafted (MR_CHOO) reaches its look with a single resource pack: no mod, no shader pack. [0017](0017-averse-et-rayons.md) had reached a similar level with mods and two Java modules of the `tilth` mod (`averse`, `rayons`). The question: what can a resource pack do on its own in Tilth, and what can it take back from the Java code?

How a resource pack draws effects without a shader pack:
- **Core shaders.** A resource pack may replace the game's own shaders (`assets/minecraft/shaders/core/`). That is where Recrafted works. Every vertex and pixel of a given kind of geometry goes through them, so a pack can recolour, fade or move what the game draws, and recognise a texture by a marker texel.
- **What it cannot do there.** Sodium draws the terrain with its own shaders, so the terrain core shaders of a pack are ignored (Recrafted targets vanilla 1.21.5+ and 26.x, where nothing replaces them). A core shader also cannot read the depth of the scene, nor create geometry the game did not send, nor know where the wind blows or where the water is.
- **Post effects through Polytone.** Polytone 5.1.3, already in the pack, lets a resource pack add full-screen post shaders with the depth buffer (`assets/<ns>/polytone/post_shaders/`). That is what screen-space sun rays need. Sunbathing (MehVahdJukaar, MIT) shows the technique on 1.21.1.

What each pass sees in Tilth (read from the jars): Where Winds Blow draws its rain with the vanilla `particle` shader and `textures/environment/rain.png`; the lightning (vanilla, and `tilth`'s own bolt) uses `rendertype_lightning`; Polytone patches the lightmap line of core shaders for its coloured lights, so a replaced shader must keep that line as the game writes it.

## Decision
`tilth-ambiance` (already the top pack, active on first launch) carries three effects:
- **Rain in the mist's colour** (`shaders/core/particle.vsh`, `particle.fsh`): `rain.png` carries a marker texel at (0, 0) (`scripts/ambiance/weather.py`); when the bound texture is 1:4 and carries it, the drops lose Where Winds Blow's yellowish tint and take a light grey with a third of the fog colour, at 70 % opacity. Every other particle is drawn as before. Replaces `averse.RainLook` and the mixin on Where Winds Blow.
- **Lightning through the mist** (`shaders/core/rendertype_lightning.fsh`): the fog fade starts 96 blocks further and ends 320 blocks further. Works for the game's bolt and for `tilth`'s; `averse.Bolt` no longer moves the fog itself.
- **Sun rays** (`polytone/post_shaders/rayons.json`, `shaders/program/rayons.fsh`): a radial blur of the open sky around the sun and the moon, cut by the depth buffer, so leaves, trunks and ridges break it into rays. Warm near the horizon, paler at noon, faint moon rays, off in the rain and outside the Overworld. Three settings in the Polytone screen of the pack (on/off, sun, moon). Adapted from Sunbathing (MIT, see `CREDITS.md`). Replaces `rayons.SunShafts` (and `sun_shaft.png`).

`tilth` 0.6.3 removes what the pack now does: `RainLook`, `WwbRainMixin`, `SunShafts`, the `rainTint` and `shafts` settings; `shaftStrength` becomes `glintStrength`.

What stays in Java, and why:

| Effect | Why a resource pack cannot do it |
|---|---|
| Rain curtains 24 to 84 blocks away | New geometry beyond Where Winds Blow's 10 blocks, placed on the ground where it rains |
| Tilth's lightning shape (zigzag, branches) and the sky flash | The game sends a fixed bolt; the flash needs to know whether the camera sees the sky |
| Leaves carried by the wind, torn off by storms; grass bits | Need Where Winds Blow's wind and new particles |
| Glints on water | Need to know where the water is; the terrain is drawn by Sodium |

## Consequences
- Less Java to maintain; the three effects can be tuned by editing a text file and reloading resources (F3+T).
- The rain shader depends on Where Winds Blow keeping the vanilla particle shader and `rain.png`. If it changes, the rain keeps Where Winds Blow's own colour; nothing breaks.
- The sun rays depend on Polytone's post shaders. The Polytone update to 5.3.x (waiting for the in-game tests) keeps them; its shadow maps were removed in 5.3.0, which we do not use.
- A broken core shader makes the game drop every resource pack at load: any change to these files needs a launch.
- The rays only show when facing the sun: the 0.6.2 shafts were also visible from the side. In the test client the rays read clearly through a leaf wall; the side view is lost.

## Rejected
- Rewriting the terrain core shaders (wet surfaces, waving grass, reflections): Sodium ignores them.
- Moving the rain curtains into the particle vertex shader (pushing the outer columns outward): a rescaled copy of the same rain, identical on screen.
- Adding Sunbathing as is: lens flare and depth of field do not fit Tilth, and its sun colour is too orange for the mist.

## In-game checks
Commands: `/weather clear`, `/time set 1500`, `/weather rain`, `/weather thunder`, `/summon lightning_bolt ~ ~ ~-30`.
- RP1. Facing a low sun through foliage: rays fan out from the sun between the leaves, warm in the morning; none in the rain; faint at night under the moon. Polytone screen of `tilth-ambiance`: the three settings, in French.
- RP2. Rain in a misty forest: pale grey streaks in the mist's colour, never yellow; splashes and other particles unchanged; coloured lights of Polytone still colour particles near a fire.
- RP3. Lightning 30 to 60 blocks away in thick mist: the bolt stays visible.
- RP4. FPS with and without the rays (setting in the Polytone screen) in a dense forest.
