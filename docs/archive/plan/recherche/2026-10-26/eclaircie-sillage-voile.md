# Eclaircie, sillage, voile (clearing, wake, veil): what is done elsewhere, and what is done well

Date: 26 October 2026. Target: Tilth, Minecraft 1.21.1 NeoForge.

Method note. I only cite pages that I really opened. Several topics could not be verified: RDR2, Firewatch, Death Stranding, Subnautica, Weather2, Pretty Rain, the fog culling of Bedrock and OptiFine, Better Render Dragon, the Distant Horizons GitLab wiki (page empty on fetch) and the exact key `maxHorizontalResolution`. They are flagged as "not verified". What comes from my memory is marked "memory".

## 1. Rare sky events, and how to warn the player

### Enhanced Celestials
- What it does: special moons (Blood Moon, Harvest Moon) and many moon add-ons. Available in 1.21.1 NeoForge, client and server. Config by datapack (wiki pages "Editing Lunar Events", "Lunar Text Components", "Client Settings", "Color Settings").
- The good: everything is data-driven, so chance, sky color and text can be set per moon.
- Limit: I found no documented forecast system. The GitHub wiki did not load its detail pages. For Tilth, the rare moon is therefore not a model for "forecast", only for variety.

### Ecliptic Seasons
- Weather by season and region, snow that builds up and melts gradually, days of varying length. Compat with Sodium, Iris, Distant Horizons. No public weather API doc on the page, nothing on fog.
- The good: snow "responds to conditions rather than appearing all at once". This is the right philosophy for the clearing spell: a transition, not a switch.

### Minecraft forecast mods
- Random Weather: a barometer predicted the weather 12,000 ticks ahead (v1.3.3). The author removed it in 1.4 "to keep the weather unpredictable" and to avoid erratic sun/rain flips.
  Lesson: a forecast on random weather often lies. For us the clearing is planned, so the forecast can be exact.
- Advanced Weather: sensors (barometer, thermometer, hygrometer, anemometer), an "archive" that forecasts from your measurements, with a confidence score. The text says that a falling barometer "really means a storm is coming". Config: archive capacity, minimum readings, confidence curve, horizon.
  The good: the player must make a gesture to know. To take: the barometer as an object you consult, with a trend (arrow), not a number.

### Zelda BotW
- Forecast shown next to the minimap (wikis differ on the corner), the weather of an area changes every 4 game hours, sun/cloud/storm/sand icons, forecast screen on the Sheikah Slate. TotK has a weather gauge. Storm and Blood Moon warnings do not appear in the page read.
- The good: a simple icon, always in the same place, readable at a glance.
- To avoid for Tilth: a permanent HUD, which breaks the riddle. Prefer an object to take out.

### The Long Dark, aurora
- The aurora is a rare weather that makes predators more dangerous and temporarily powers electronics. According to the page, it is the only time the lamp recharges. Players call it very rare (one every 30 to 35 days on average according to a Steam thread, with large gaps). The thread describes no warning sign.
- The good: a unique mechanical benefit gives a reason to go out and watch the phenomenon.
- Lesson: rarity without a warning sign makes players miss the event (some sleep through the aurora). Tilth plans a sign the day before: a good choice.

### Sea of Thieves, fog
- The fog covers about a third of a region and often lasts more than a day. Everything outside the ship's immediate surroundings is hidden. The arrival signal is audio: the water goes quiet, a light hum; a patch moved up the trigger of the music and sounds. Counterplay: climb high (lookout) or fire in the air. A fire on board no longer clears the view (patches 2.4.0 and 2.4.1). The producer cites the sound atmosphere (amplified creaks) as half of the effect.
- The good: an audio signal beforehand, a diegetic counterplay (height), a sound that makes the atmosphere.
- Lesson on the sillage (wake): the developers removed the ability to clear the fog with a fire. A source of clarity that is too easy kills the fog.

### Valheim, Mistlands
- See part 2. Note that the mist there is a zone, not an event.

### Making a beautiful memorable moment (synthesis, opened pages + judgment)
- I found nothing sourced on the music or photo mode of these games. My judgment: a sound beforehand (Sea of Thieves), a benefit that encourages going out (Long Dark), a slow transition (Ecliptic Seasons).
- A generic article on volumetric fog recalls that fog gives depth and atmosphere and that it is easy to overuse.

## 2. Moving clear zones and lights against fog

### Valheim, Wisplight
- The Wisplight is a blue orb that follows the player and clears the mist around them. The Wisp Torch clears around a fixed point, with no fuel. The Mistwalker (sword) clears less far. The Wisplight takes the same slot as the Megingjord belt, so it costs carrying capacity. Wisps are harvested at night around a Wisp Fountain.
- Radii: the WispReach mod gives 15 m (Wisplight) and 24 m (Wisp Torch) as the original behavior; MistBender gives 6 m by default and allows 6 to 50 m. The two figures contradict each other: not verified. MistBender notes that a larger radius = more visible scenery = more rendering cost.
- Useful technical detail (WispReach): R1 clear radius, R2 radius where the mist is at full opacity, R3 density. Mist clouds are judged by the distance of their center, so a big cloud crosses the limit. When two zones overlap, the stronger wins.
- To take: a progressive edge (R1 to R2), an overlap by maximum, an equipment cost. To avoid: measuring by cloud center. We will measure per fragment.
- A radius of 64 for us, against 15 to 24 m in Valheim: that is 3 to 4 times larger. Plan a cost (energy, stress, fuel) to keep the tension.

### Sea of Thieves
- Lanterns: cosmetic wiki pages only, nothing says they pierce the fog (and a fire no longer does). The counterplay is height. Not verified for other mechanics.

### Minecraft
- Search for "clear fog bubble" mods: nothing found. Existing mods are global. FoggedUp (1.20.1 Forge, server and client) sets the shape, start and end of fog per player by command (`/setfog start|end|shape`). FogControl (1.20.1) sets global min/max, synchronized, and says the DH fog extends the vanilla fog seamlessly. None does a local zone.
- Aeronautics and lights: nothing found on ship lighthouses in the opened pages (not verified).
- Conclusion: the sillage will be in-house code.

### Death Stranding
- Not verified.

## 3. Fog-based culling

### Sodium, "Use Fog Occlusion" (sources read: code 1.21.1/stable)
- Language key `sodium.options.use_fog_occlusion`: "chunks judged fully hidden by fog are not rendered; may cause artifacts between the sky and the fog".
- Code (1.21.1) in `RenderSectionManager.getEffectiveRenderDistance()`: reads `RenderSystem.getShaderFogColor()` and `RenderSystem.getShaderFogEnd()`. If the color alpha is not exactly 1.0, no culling. Otherwise, search distance = `min(render distance, fogEnd + 0.5)`. Option read through `SodiumClientMod.options().performance.useFogOcclusion`.
- `SodiumWorldRenderer` compares `RenderSystem.getShaderFogEnd()` to the previous value: if it changes, the visibility graph is marked dirty. So varying the fog end every frame restarts the computation every frame.
- `OcclusionCuller`: "cylindrical" test (max of the horizontal distance and the vertical distance), commented as the vanilla algorithm. A spherical fog hides more than this test: the culling is conservative, never too aggressive.
- The real fog is in the fragment shader (`fog.glsl`, `_linearFog`, uniforms `u_FogStart`, `u_FogEnd`, `u_FogColor`): global to the camera, not per zone.
- `dev` branch (recent MC): `FogParameters` with `cullDistance`. The hook will change if Tilth moves up a version.
- ATMOSPHERICS: client mod that sets fog, sky, clouds, per biome with transitions, presets in `config/ambientfog`. The page says nothing about Sodium or DH. I did not find how it pushes the fog. To test in game (see recommendation 10).
- Sodium Extra: fog settings, particles per type, animations. No entity distance option on the opened page. (A search summary, not opened, says its recent versions align chunk culling on its cylindrical fog: to check.)

### Entity Culling (tr7zw) and why it breaks contraptions
- How it works: line-of-sight computation on free CPU threads, hidden entities and block entities skipped, ticks cut on the client side. Separate allowlist for rendering and ticking. Block entities that exceed their box (beacon, Create pulleys) must be listed.
- GitHub issue #299 "Sable Compat Issues" (open): on a Sable ship, other players appear and disappear. Disabling the mod fixes everything (at the cost of about 50% FPS according to the reporter). Sublevels are "plotyard" chunks rendered over the terrain, not normal entities. The maintainer proposes the exclusion list or excluding the entities of the "Create world", and suggests an add-on (Create Nowheel), unconfirmed. Adding `minecraft:player` helps for players, not for a transported sheep.
- Decision already taken (removal): confirmed.

### Other mods and Sable traps
- GpuShift 1.2.4: a block entity budget caused about 1 FPS with Aeronautics on NeoForge 1.21.1. Fix: the budget is skipped during contraption rendering. Lesson: any budget or culling mod must be tested with a ship in flight, especially in descent.
- Sable (README of the repository found): very intrusive library (mixins), sublevels with chunks, entities and block entities with their own position and orientation. Points to "Sable Companion" and the "Sable Developer Wiki", which I did not open. The repository seen is under another account (cxykevin): perhaps a mirror.
- More Culling: client mod, config through Cloth Config, details in its wiki (not opened).
- Particles: Particle Core 0.3.0 (particle render distance relative to block distance, maximum count, visibility culling, tag `particle_core:excluded_particles`, config keys `reduceParticlesByType`, `disableOptimizations`) and Smart Particles (default cap 5000, the farthest skip first, `config/smart_particles/config.json`). The exact keys for distance and maximum are not documented on the page.
- Vanilla entities: the "Entity Distance" option (`entityDistanceScaling`): 100% = 160 blocks according to the wiki (the page lists 75, 100, 125%). I did not verify the range for 1.21.1.
- Bedrock, OptiFine, Better Render Dragon: not verified.

### Distant Horizons 3.x
- Server and client with DH: LODs are sent to each player who has DH and "Distant Generation" enabled; without it, nothing is sent (Modrinth page).
- Guide: check in F3 "Server has full DH support"; same build on both sides; pregen with `/dh pregen start overworld 0 0 64` (radius of 64 chunks), `/dh pregen status`, `/dh pregen stop`; Chunky generates chunks but can leave LOD holes (confirmed by GitLab ticket #607 and by the 3.3.0 changelog: "Chunky and DH together still not recommended").
- Changelog 3.3.0-26.3: generation paused when the server is overloaded or the camera moves fast (adjustable), fast surface generation for 1.19.2+, config `maxTexturedLodDetailLevel` raised from 4 to 8, minimum detail level limited. DH recommends 2 to 4 GB of extra RAM for large settings (CurseForge FAQ).
- Third-party guide: changing address or port can create separate LOD folders; flying or teleporting creates a generation backlog.
- `maxHorizontalResolution`: cost not documented in my sources. Memory: the finer the resolution, the more CPU, memory and disk go up. To measure.

## Recommendations for eclaircie, sillage and voile

**Eclaircie (clearing spell)**
1. A single clarity variable. A float from 0 to 1 on the server side (SavedData, calendar by season), synced to clients, that serves ATMOSPHERICS, DH and the voile at once. Slow change (5 to 10 minutes), like the snow of Ecliptic Seasons.
2. Warn with three signs, not a HUD. Red sky at dusk (ATMOSPHERICS preset the day before), a barometer as an item or block with a trend arrow and confidence (Advanced Weather model), a sound sign at sunset (Sea of Thieves model). Since the clearing is planned, the forecast is exact (counter-example: Random Weather removed its own).
3. A reason to go out. A unique benefit during the clearing (Long Dark model: the lamp only recharges there), a short jingle once, a "Vu d'en haut" (Seen from above) progress given at altitude. No vanilla photo mode: offer the "carnet" (notebook) item instead.
4. Quantize the end of the fog. Sodium restarts its graph at every change of `RenderSystem.getShaderFogEnd()`. During the transition, only change `fogEnd` in steps (for example every 5 ticks or by 2 blocks) to avoid a recomputation per frame.
5. DH must have the data. LODs of never-visited areas only come from the server: DH on both sides, "Distant Generation" active on both sides, pregen with `/dh pregen start overworld 0 0 <radius>` (not Chunky), during off-peak hours. Check "Server has full DH support" in F3.

**Sillage (wake)**
6. Two steps. V1: camera-based measurement. Compute an effective fog end = function of the distance between the camera and the bubble (inside the bubble: clear; over 16 blocks of edge: fade; overlap by maximum). Easy, good for the pilot and passengers. V2: per-fragment fog in Sodium's `fog.glsl` (`_linearFog`), with a uniform of clear spheres. The fragment position must also be passed to the shader (the current shader only has `v_FragDistance`), and DH's own fog must be planned for. High cost, to decide after V1.
7. Culling trap. Sodium culls according to the global `fogEnd`. To see the bubble of a distant ship, the value read by Sodium must be `max(mist fogEnd, distance to the bubble + radius)`, otherwise the chunks of the bubble are culled before being drawn. Keep `fogEnd` wide for culling and do the real fog in the shader (V2).
8. Dissipateur (dissipator) settings. Base radius 64 but with a progressive edge, cost in stress or fuel, only one active dissipator counts (maximum, not sum). Valheim keeps 15 to 24 m to keep the tension; Sea of Thieves removed the free clarity from a fire. No biome conversion: our zone is a uniform, independent of ATMOSPHERICS.
9. Ship position. Read the sublevel pose on the client side in Sable (classes to inspect in its sources; I did not open them), never the world position of the control entity alone.

**Voile (veil)**
10. Sodium test before any code. In a test world, log `RenderSystem.getShaderFogEnd()` and `getShaderFogColor()[3]` in the render loop with ATMOSPHERICS active. If the alpha is 1.0 and `fogEnd` follows the presets, "Use Fog Occlusion" already culls the chunks (key `performance.use_fog_occlusion` in `sodium-options.json`, file name to check). Otherwise no culling takes place and it is up to us to do it. Set the sky and the mist to the same color to avoid the artifacts cited by the tooltip.
11. A single distance function. `distanceVoile = effective fogEnd + margin` (margin 8 to 16), which includes eclaircie and sillage. Entities, block entities and particles use it: when the mist lifts or a ship passes, everything reappears by itself.
12. Mandatory exceptions. Entities and block entities in a Sable sublevel or in a clear zone. Mixin on the display test of `EntityRenderDispatcher` (method `shouldRender`, memory) and on block entity rendering; cull nothing if the object is on a sublevel. Lesson from Entity Culling #299 and from the GpuShift guard. Do not reinstall Entity Culling.
13. Particles: either Particle Core or Smart Particles (cap 5000, distance), or a mixin in `ParticleEngine` with the same `distanceVoile`. Vanilla already ignores non-forced particles far away (about 32 blocks, memory, to check in the mapped sources).
14. Entities: in addition, lower the pack's `entityDistanceScaling` and do not rely on it alone (100% = 160 blocks).
15. DH: keep `maxHorizontalResolution` modest (cost not documented, to measure), server and client RAM with margin, a single build on both sides, stable address so as not to create separate LOD folders. Test each combination once: mist, clearing, ship in flight, descent.

## Sources (opened pages)

- https://github.com/tr7zw/EntityCulling/issues/299
- https://github.com/tr7zw/EntityCulling
- https://modrinth.com/mod/entityculling
- https://www.curseforge.com/minecraft/mc-mods/gpushift/files/8385277
- https://github.com/cxykevin/sable
- https://modrinth.com/mod/moreculling
- https://modrinth.com/mod/particle-core
- https://modrinth.com/mod/particle-core/version/0.3.0+1.21+neoforge
- https://modrinth.com/project/l8rWP4Tc
- https://modrinth.com/mod/sodium-extra
- https://github.com/CaffeineMC/sodium/branches/all
- https://github.com/CaffeineMC/sodium/blob/dev/common/src/main/java/net/caffeinemc/mods/sodium/client/render/chunk/occlusion/OcclusionCuller.java
- https://raw.githubusercontent.com/CaffeineMC/sodium/1.21.1/stable/common/src/main/java/net/caffeinemc/mods/sodium/client/render/chunk/RenderSectionManager.java
- https://raw.githubusercontent.com/CaffeineMC/sodium/1.21.1/stable/common/src/main/java/net/caffeinemc/mods/sodium/client/render/SodiumWorldRenderer.java
- https://raw.githubusercontent.com/CaffeineMC/sodium/1.21.1/stable/common/src/main/resources/assets/sodium/shaders/include/fog.glsl
- https://raw.githubusercontent.com/CaffeineMC/sodium/1.21.1/stable/common/src/main/resources/assets/sodium/shaders/blocks/block_layer_opaque.fsh
- https://raw.githubusercontent.com/CaffeineMC/sodium/1.21.1/stable/common/src/main/resources/assets/sodium/lang/en_us.json
- https://raw.githubusercontent.com/CaffeineMC/sodium/dev/common/src/main/java/net/caffeinemc/mods/sodium/client/render/chunk/RenderSectionManager.java
- https://modrinth.com/mod/atmospherics
- https://modrinth.com/mod/distanthorizons
- https://modrinth.com/mod/distanthorizons/version/3.3.0-26.3
- https://craftdownunder.co/guides/mods/distant-horizons
- https://distanthorizonsguide.com/guides/distant-horizons-server-realms
- https://blog.curseforge.com/distant-horizons-frequently-asked-questions/
- https://gitlab.com/jeseibel/distant-horizons/-/issues/607
- https://modrinth.com/mod/foggedup
- https://www.curseforge.com/minecraft/mc-mods/fogcontrol
- https://modrinth.com/mod/enhanced-celestials
- https://github.com/CorgiTaco-MC/Enhanced-Celestials/wiki
- https://modrinth.com/mod/ecliptic-seasons
- https://www.curseforge.com/minecraft/mc-mods/random-weather
- https://modrinth.com/mod/advanced-weather
- https://zeldawiki.wiki/wiki/Weather
- https://steamcommunity.com/app/305620/discussions/0/1470840994979885994
- https://seaofthieves.wiki.gg/wiki/Fog
- https://wccftech.com/sea-of-thieves-shrouded-spoils-update/amp/
- https://thunderstore.io/c/valheim/p/Slikfoul/WispReach/
- https://thunderstore.io/c/valheim/p/MatSilaw/MatSilawMistBender/
- https://commonsensegamer.com/valheim-mistlands-how-to-see-through-mist/
- https://eip.gg/valheim/guides/10-tips-to-master-the-mistlands/
- https://minecraft.wiki/w/Options
- https://www.wayline.io/blog/top-5-games-volumetric-fog
