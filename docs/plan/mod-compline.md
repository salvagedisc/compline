# The custom `compline` mod

The mod proposed in [12.4](../archive/plan/12-verification.md#124-the-custom-compline-mod) and retained by decision 17 of [12.5](../archive/plan/12-verification.md#125-decisions-that-are-added-continuing-1012). It is made of modules that are enabled one by one. Version 0.16.0 contains the **ciel** (sky), **xp**, **horizon** (Distant Horizons), **clairiere** (clearings), **dissipateur** (dissipator), **outils** (tools), **monuments**, **bord** (world edge), **rendu** (shader link), **relief** (height of the world for the resource pack) and **zoo** (stamps for every creature in Archie's guidebook) modules, plus [compatibility fixes](#compatibility-fixes) (Punchy! and Sensible Stackables, Streams Reflowing and Spelunkery, Lithostitched). The **titres** module (each screen's name on a plate drawn by the `compline-interface` pack) left in 0.10.1 with that pack ([0024](../decisions/0024-removing-the-house-resource-packs.md)); the **averse** (rain, storm, leaves in the wind) and **rayons** (glints on water) modules left in 0.11.0, with their textures ([0026](../decisions/0026-second-cleanup.md)): rain, storms and foliage are those of the game and of Where Winds Blow.

> **Status as of 2026-10-26.** Version 0.4.1 compiles (`./gradlew build`, against Enhanced Celestials 2, Distant Horizons 3.3.3 and Create 6.0.10) and the full pack starts on a dedicated server with it; the clearing conversion and the dissipator were checked on that server with commands. **Nothing has been tested in game with a player**. Everything below comes from reading the code of the targeted mods (jars opened with `javap`). The list of [in-game tests](#in-game-tests) says what must be checked before relying on it.

## Building and updating

| What | Where |
|---|---|
| Sources | `mods-custom/compline/`: official ModDevGradle 1.21.1 MDK, Gradle wrapper 9.2.1, Java 21 |
| Java package | `io.github.salvagedisc.compline` |
| Jar in the pack | `pack/mods/compline-<version>.jar`, a raw jar (no `.pw.toml`) |
| CI | `mod-compline` job of `.github/workflows/build.yml`: it compiles the mod and keeps the jar as an artifact |

To release a version:

```sh
cd mods-custom/compline
./gradlew build                     # jar in build/libs/compline-<version>.jar
cp build/libs/compline-<version>.jar ../../pack/mods/   # remove the old jar
cd ../../pack && packwiz refresh
```

The version is set in `gradle.properties` (`mod_version`). The targeted mods are used only at compile time (`compileOnly`, Modrinth Maven repository). `build.gradle` reads their version from `pack/mods/<slug>.pw.toml`: the mod is always compiled against the exact versions in the pack, and an update of one of these mods shows up at the next compilation.

## Principles

- **One module = one switch.** Each module has its `enabled` boolean in the config: `config/compline-client.toml` for `ciel`, `config/compline-common.toml` for `xp`. The values are re-read at each use, so a change in game takes effect immediately.
- **A module loads only if its mods are present.** At startup, `Compline` checks with `ModList` the mods each module depends on. It writes "Module X chargé" or "Module X ignoré" to the log. Mixins go through `ComplineMixinPlugin`, which applies them only if the targeted mods are in `LoadingModList`. The only hard dependency is NeoForge.
- **No copied code.** The mod goes through the targeted mods' public APIs and through targeted mixins, on vanilla code when possible.

| Module | Side | Required mods | Mixins |
|---|---|---|---|
| `ciel` | client | ATMOSPHERICS; Enhanced Celestials for the moon | 4, all client (6 before 0.7.0) |
| `xp` | both (the anvil on both sides, the bar on the client) | none | 1, common (`xp.AnvilMenuMixin`, on vanilla code) |
| `horizon` | both (the fog on the client, the Chunky fix on both sides) | Distant Horizons (Chunky for its fix, only if a server owner adds it) | 5: `horizon.FogRendererMixin`, `horizon.VanillaFadeMixin`, `horizon.FarFadeMixin` and `horizon.GLStateMixin` client, `horizon.ChunkyAccessorMixin` common |
| `clairiere` | both (the blocks and the list of clearings on the server, their look on the client) | none; ATMOSPHERICS, True Adaptive Music and Sodium each for its part of the look | 5, all client: `clairiere.ClientLevelTintMixin`, `clairiere.BiomeLookMixin` (vanilla code), `clairiere.SodiumTintMixin`, `clairiere.AtmosphericsBiomeMixin`, `clairiere.MusicBiomeMixin` |
| `dissipateur` | both | Create | none |
| `bord` | both (everything happens on the server) | none; Sable for the players aboard a ship | none |
| `rendu` | client | Iris; Where Winds Blow for the wind | 1, client (`rendu.IrisUniformsMixin`) |
| `zoo` | both (stamps and species list on the server, the guidebook pages on the client) | none for the stamps; Archie's Dinosaurs for the pages, Exposure for the photo stamp | 2, client: `zoo.GuidebookDataMixin`, `zoo.CategoryTabsMixin` |
| (compatibility fix) | client | Punchy! and Sensible Stackables | 1, client (`compat.PunchyEquipMixin`) |
| (compatibility fix) | both | Streams Reflowing and Spelunkery | 1 (`compat.IceThawMixin`) |
| (compatibility fix) | both | Lithostitched (used by Regions Unexplored) | 1 (`compat.InStructureMixin`) |

## `ciel` module

### Enhanced Celestials' moon under ATMOSPHERICS

**The problem.** ATMOSPHERICS cancels `LevelRenderer.renderSky` (injection at the start) and redraws the whole sky. Its moon still uses the vanilla `moon_phases.png` texture, at size 20 and in white. Enhanced Celestials' mixins on `renderSky` therefore no longer run: the white moon, the blood moon and the other events keep the appearance of the vanilla moon. The white moon is also smaller (14) than the vanilla moon: redrawing it on top is not enough, ATMOSPHERICS' moon must be hidden first.

**The mechanism** (`MoonOverlay` and three mixins):

1. `LevelRendererMixin` wraps the call to `renderSky` in `renderLevel` (`@WrapOperation`). Just before the call, the module arms itself if the dimension has Enhanced Celestials lunar data.
2. The same mixin spots the first `Runnable.run()` of `renderSky`: this is the fog setup, at the start of the vanilla body. If it runs, ATMOSPHERICS has not taken over; Enhanced Celestials then draws its moon itself, and `compline` touches nothing.
3. Otherwise, `RenderSystemMixin` replaces, during ATMOSPHERICS' sky only, the `moon_phases.png` texture with a transparent texture (`compline:textures/environment/hidden_moon.png`). ATMOSPHERICS' moon quad no longer draws anything.
4. After `renderSky`, if the moon was indeed hidden, `compline` draws the Enhanced Celestials one at the same place, with the same matrices:
   - the texture of the current event;
   - the size blended between the previous and the current event, as Enhanced Celestials does;
   - the color, through `ECWorldRenderer.changeMoonColor`, the function Enhanced Celestials calls in the vanilla sky (it also handles fading by rain);
   - the scale (`moonTextureScale`) and rotation (`moonTextureRotation`) set in the ATMOSPHERICS config.

If a step is missing (ATMOSPHERICS does not draw a moon, Enhanced Celestials absent…), nothing is hidden and nothing is drawn. A drawing error is logged once, without crashing the rendering.

Config: `ciel.redrawMoon` (true by default).

**Known limitations.**
- The redrawn moon comes after all of ATMOSPHERICS' sky, hence on top of its clouds or its sky veil if it draws any after the moon. To be checked in game.
- It is not drawn when it is below the horizon (time-based test, with a margin for large moons).

### Clearing ATMOSPHERICS' mist cache

ATMOSPHERICS computes the mist from the biomes around the player. It keeps the result in a cache (`BiomeBlendUtil`) as long as the player stays in the same 4-block cell. If the biomes change under a stationary player (conversion into a clearing, `/fillbiome`), the mist only reacts on the next movement.

- **Automatic**: `ClientPacketListenerMixin` clears the cache every time the server sends new biomes (`ClientboundChunksBiomesPacket`, the one `/fillbiome` produces). Config: `ciel.clearFogOnBiomeUpdate` (true by default).
- **By hand**: a public static method, usable later from KubeJS (client scripts):

  ```js
  Java.loadClass('io.github.salvagedisc.compline.api.ComplineSky').clearFogCache()
  ```

  It returns `true` if the cache was cleared, or scheduled to be cleared on the render thread. It returns `false` if ATMOSPHERICS is absent, if the module is off or if we are on a server. `ComplineSky.isFogCacheAvailable()` answers the same question without clearing anything. KubeJS may require the class to be allowed in its class filter. A server script that wants to trigger the clearing on players' machines must send them a packet, for example with `NetworkEvents.dataReceived` on the client side.

The cache is cleared by `BiomeBlendUtil.resetCache()`, a private ATMOSPHERICS method, reached through a mixin accessor (`@Invoker`).

### The Friendly Moon under ATMOSPHERICS

Removed in 0.7.0. History: in the first in-game test (2026-10-28) the Friendly Moon of No Man's Land never appeared, because ATMOSPHERICS and that mod each waited for the other to start the sky. `compline` 0.4.10 (2026-10-29) made the call itself (`FriendMoonRelay`, mixins `ciel.FriendMoonSkyMixin` and `ciel.AtmosphericsDreamMixin`, config key `ciel.friendMoon`), and it was checked in a development client.

No Man's Land left the pack on 2026-11-13 ([0019](../decisions/0019-leaving-no-mans-land.md)): `compline` 0.7.0 removes `FriendMoonRelay`, both mixins and the `friendMoon` key. The module keeps the moon of Enhanced Celestials and the mist cache. The description of the mechanism is in the archived [friendly moon page](../archive/plan/lune-amie.md).

## `xp` module

Compline no longer has experience ([xp.md](xp.md)). Orbs and point gains are disabled by `pack/kubejs/server_scripts/sans_xp.js`. The module does the two things KubeJS cannot do cleanly.

**The anvil without levels.** Vanilla only returns the item if `player.experienceLevel >= cost` and `cost > 0` (`AnvilMenu.mayPickup`). `AnvilMenuMixin` replaces, in that method only, the read of `experienceLevel` with `Integer.MAX_VALUE` (MixinExtras `@ModifyExpressionValue`). The rest of the computation does not change:

- the cost is still at least 1, and the limit of 40 set by `createResult` remains. It becomes **wear**: "Usure : n" (wear: n), then "Trop usé !" (too worn!) (vanilla keys `container.repair.cost` and `container.repair.expensive`, translated in `pack/kubejs/assets/minecraft/lang/`);
- the materials (ingots, sacrificed item) are still taken; `onTake` still removes `cost` levels, which does nothing at 0;
- Enhanced Celestials' blue moon, which divides the cost in `createResult`, therefore reduces wear.

The mixin applies on both sides: the client also refuses to take the item if it thinks the levels are missing, and it colors the cost red. Client and server must have the same version of the mod and the same `xp.enabled` value. No mod in the pack patches `mayPickup`.

**Enchantment merging (optional).** With `xp.blockEnchantMerge`, `XpModule` cancels `AnvilUpdateEvent` when the right-hand item carries enchantments (enchanted book or enchanted item): the anvil gives no result, and enchanting goes through the table. Off by default: the maintainer keeps merging.

**The hidden bar.** On the client, `XpClient` cancels the `VanillaGuiLayers.EXPERIENCE_BAR` and `EXPERIENCE_LEVEL` layers in `RenderGuiLayerEvent.Pre`. The horse jump meter (`JUMP_METER`) is another layer and stays. The space the bar left is taken back: `Gui.leftHeight` and `rightHeight`, reset to 39 at each frame, are set to 34 in the same event, just before the health and food layers read them. Hearts, armor, hunger, air and what mods stack above them with these fields (Thirst Was Reclaimed's thirst) come down 5 pixels, leaving one pixel above the hotbar's selection frame. Not while the jump meter is shown (on a horse), where everything keeps its vanilla height. Cold Sweat places its body temperature icon by itself: `pack/config/coldsweat/client.toml` moves it down by the same 5 pixels (`Body Temperature Icon Offset`, `Body Temperature Readout Offset`).

Config (`compline-common.toml`, `[xp]` section): `enabled` (true), `hideBar` (true), `blockEnchantMerge` (false). The last two only matter if `enabled` is true. To roll back: `enabled = false` and delete `sans_xp.js`.

## `horizon` module

Decision [0011](../decisions/0011-tectonic-streams-distant-horizons.md). Distant Horizons (DH) 3.3.3, API 7.2.0, read with `javap`.

### The far landscape follows the mist

**The problem.** DH draws its LODs (simplified far terrain) after the normal terrain, then applies its own fog to them, in a full-screen pass. That fog is measured as a fraction of the LOD radius and knows nothing about ATMOSPHERICS. By default DH also pushes the vanilla fog out to infinity (`MixinFogRenderer`), which would erase the pack's mist: the shipped config sets `enableVanillaFog = true`.

**The mechanism.**
1. `FogRendererMixin` reads, at the end of `FogRenderer.setupFog` for the terrain (`FOG_TERRAIN`), the fog start and end left in `RenderSystem` (`TerrainFog`). Its priority (1500) places it after ATMOSPHERICS and DH at the same point: it reads the final mist, already smoothed over time by ATMOSPHERICS.
2. After DH's initialisation (`DhApiAfterDhInitEvent`), `HorizonClient` registers two DH events, if the DH API major version is 7.
3. `DhApiBeforeFogRenderEvent`, every frame: `HorizonFog` sets DH's far fog to linear, from `start / R` to `end / R` (R = LOD radius in blocks, read from DH's config), thickness 0 to 1. A LOD at distance d is therefore as fogged as a block of terrain at the same distance.
4. `DhApiBeforeRenderEvent`: when the mist ends before the edge of the normal terrain (render distance × 16 blocks), every LOD is beyond the end of the mist, so invisible; the module cancels the whole LOD rendering of the frame. In misty areas DH costs no GPU time.
5. `VanillaFadeMixin` skips, in those same frames, DH's fade between the normal terrain and the LODs (`ClientApi.renderFadeOpaque` and `renderFadeTransparent`). That fade blends the edge of the normal terrain with DH's LOD texture, which is only cleared and redrawn by the LOD rendering the module has just cancelled: it would blend in the last LOD image drawn, frozen, for example the clearing's mountains after walking back into the mist.
6. **The mist ring** (`FarFadeMixin`). A LOD drowned in the mist takes the mist colour, which is not the colour of the sky behind it: without more, the far mountains stay visible as mist-coloured silhouettes up to the end of the LOD radius (1,536 blocks), and the clearing has no horizon. DH already has a far fade that dissolves LODs into the sky, from 50 % to 90 % of its far clip plane. The mixin hands it the ring distance instead: the LODs dissolve into the sky from 55 % to 100 % of the end of the mist. The ring is also capped (`ringMaxDistance`, 512 blocks): even where the mist ends further away (a future preset), the landscape always closes in a ring. The far fog of step 3 uses the same capped end.
7. Underwater, in lava or powder snow, blinded or in darkness, the module does nothing: DH already drowns its LODs.

**What it gives with the `compline_v3` preset.** Misty (end 52) and veiled (end 96) families: no LOD drawn. Clearings (end 500 by day, 260 at night): the landscape appears progressively beyond the normal terrain, then dissolves into the sky between 280 and 500 blocks by day (145 and 260 at night). The transition follows ATMOSPHERICS' smoothing when walking in or out of a clearing.

**Checked in game on 2026-10-29** (development client with only DH 3.3.3, ATMOSPHERICS and `compline`, `compline_v3` preset, render distance 12, LOD radius 48 chunks, software OpenGL): from a hill in a clearing, a mountain range about 700 blocks away was visible as a silhouette before the ring, and is gone with it; the landscape up to about 450 blocks stays visible and dissolves into the haze; same result at dusk with the 260-block night mist. The frozen-image case of step 5 comes from reading DH's code; it was not seen on that client.

**Fallback.** The shipped `DistantHorizons.toml` has `farFogMin = farFogMax = 1`: without the module (switched off, error, other API version) every LOD is fully in the fog. No regression: the pack looks as it did without DH.

Config (`compline-client.toml`, `[horizon]` section): `enabled` (true), `skipHiddenLods` (true), `ringMaxDistance` (512 blocks, 64 to 4,096).

### Two fixes for DH 3.3.3

- **Crash when creating the world with Chunky** (`ChunkyAccessorMixin`, common). Chunky left the pack on 2026-11-14 ([0026](../decisions/0026-second-cleanup.md)); the fix stays and applies only if a server owner adds Chunky to the server. DH binds to Chunky's events on the first chunk loaded. On NeoForge the spawn chunks load before Chunky has started: `ChunkyProvider.get()` throws "Chunky is not loaded" and the server crashes during `createLevels` (observed on the test server, dedicated server; single player goes through the same code). The mixin catches the error and puts DH's `listenerBound` flag back to false: DH tries again on the next chunk, once Chunky is ready. Verified: with it, the full pack starts.
- **Native memory overflow on Apple Silicon** (`GLStateMixin`, client). `GLState.saveState` reads `GL_POLYGON_MODE` with `glGetInteger`, which reserves one integer; Apple's OpenGL driver writes two, which corrupts native memory and makes the JVM crash at random (DH issue #1346, fixed upstream after 3.3.3). The mixin reads that value into an array of two integers and keeps the first. Not verified on a Mac.

Both use `require = 0`: if a future DH no longer has the targeted code, they do nothing.

## `clairiere` module

Plan [10.3](../archive/plan/10-recentrage.md#103-the-mist-that-lifts), layer 3: the mist lifts where the players decide. It replaces the KubeJS drafts `clairieres.js` and `clairiere.json` of `kubejs-brouillons/`.

**Blocks.**

| Block | Radius | Recipe |
|---|---|---|
| Vigil Fire (`compline:feu_de_veille`), a stone brazier on a foot with embers and a pale flame | 16 | A honeycomb, two smooth stone slabs, a charcoal, a smooth stone (provisional, to be confirmed by the team; before 0.7.0: a No Man's Land resin oil bottle, polished stone slabs, polished stone) |
| Clearing Lantern (`compline:lanterne_de_clairiere`), a standing lantern of blackened iron around a glowing crystal | 32 | Two Overgeared iron plates, two glass panes, an amethyst shard (provisional, to be confirmed by the team; before 0.7.0: a No Man's Land quartzite cluster) |
| Dissipator (`compline:dissipateur`, `dissipateur` module) | 64 | Encased fan, brass casing, propeller, two precision mechanisms |

Both have their own model and textures since 0.4.12 (`textures/block/feu_de_veille_*`, `lanterne_de_clairiere_*`; the flame is animated and, like the embers and the crystal, lit at full brightness through NeoForge's per-face light). Before, the fire was a cobblestone campfire and the lantern the vanilla lantern, and players mixed them up with those.

**A clearing does not touch the world** (0.8.0). Until 0.7.0 a clearing replaced the biome of its disc with one of three clearing biomes. Those have no tags and their own climate, so everything a mod decides from the biome changed with them: the rain and the crop humidity of Ecliptic Seasons, the temperature of Cold Sweat, the fauna, the atlas tile. Since 0.8.0 the biome stays the one the generator placed. The server only keeps the list of clearings (centre, radius, opening time) in the world's saved data (`compline_clairieres`, `ClearingSources`) and sends it to the clients (`ClearingSync`: the whole list at login, then each opening and closing). F3, `/locate biome`, the seasons, the crops, the temperature and the animals all see the real biome.

**What a clearing changes.** Only the biomes of the `compline:peut_devenir_clairiere` tag clear: land biomes and rivers, not oceans, beaches, deserts or caves, which keep their mist inside the disc. The tag lists 59 surface biomes of Regions Unexplored next to the vanilla ones; the tag `compline:sans_outil` has `regions_unexplored:cattail` among the plants that need no tool.

- On the server, one rule: monsters do not spawn naturally in a clearing (`MobSpawnEvent.PositionCheck`, natural spawns of the monster category). Spawners, raids and monsters walking in are untouched.
- On the client (`ClearingClient`), what is seen and heard takes the look of a clearing biome, chosen from the temperature of the real biome: `compline:clairiere_froide` below 0.3, `compline:clairiere_chaude` above 1.0, `compline:clairiere` otherwise. The three biomes still exist in the registry, but only as looks: nothing is generated or converted into them.
  - **Mist.** ATMOSPHERICS reads the biome around the player with `ClientLevel.getBiome`; `AtmosphericsBiomeMixin` hands it the look biome for the mist, the air haze, the weather and the horizon veil, so the `compline_v3` clearing profile applies (40 → 500 blocks by day) and Distant Horizons shows the far landscape (`horizon` module). Its biome particles (desert dust, tumbleweeds) keep the real biome.
  - **Colours.** Where a block tint is computed (`ClientLevel.calculateBlockTint`, and Sodium's `LevelColorCache.updateColorBuffers`), the colour rule still receives the real biome, but for the time of the call its base grass, foliage and water colours are those of the look biome (`ClearingClient.tint`, a per-thread marker read by `BiomeLookMixin` in `Biome.getGrassColor`, `getFoliageColor` and `getWaterColor`). The seasonal tint Ecliptic Seasons adds from the real biome therefore stays: a clearing is vivid and still turns with the seasons of its biome. The hook is on the call and not on the three `BiomeColors` constants, because Ecliptic Seasons and Polytone both replace those. Distant Horizons colours its far terrain per biome, not per position: seen from far away, a clearing keeps the colours of its biome.
  - **Music.** `MusicBiomeMixin` gives True Adaptive Music the look biome, so node 8 of the [music](musique.md) plays as before.

**The mist comes back** (0.4.12, reworked in 0.8.0). When a fire or a lantern leaves the world (broken, blown up, replaced: `onRemove`), or a charged dissipator is broken (`destroy`, called by Create), its clearing is removed from the list and the clients close it. Where another clearing still covers the ground, nothing changes. The nearest player within 8 blocks reads "La brume revient."

**The transition** (0.4.12, client side since 0.8.0). A clearing does not appear at once: its radius grows ring by ring, 4 blocks every `ringTicks` ticks (6), from the centre outwards when the mist lifts, from the edge inwards when it comes back. A lantern (32 blocks) takes about 2.7 seconds, a dissipator (64 blocks) about 5. The wave is computed on each client from the game time of the opening or the closing, so a player who arrives in the middle of it sees it at the right place and nothing is lost if the server stops. Each ring redraws the chunks it crosses and sends a few cloud puffs (`CLOUD` particles, at most 160 per ring, within 160 blocks of the edge) that drift outwards when the mist leaves and inwards when it returns. `AtmosphericsFadeMixin` slows the fog distance for 15 seconds after each ring (`FogCache.slowFade`): the fog density (1 / end distance) moves towards its target at a bounded speed, so going from full mist to a clear sky takes `clearingFadeSeconds` (10; the visible mist goes in the first 4 or 5). Without it, ATMOSPHERICS fades the distance linearly in about a second and the mist seems to vanish in the first few per cent.

**Worlds made before 0.8.0** still hold clearing biome cells. Their sources are read from the same saved data (opening time 0), so the mist stays lifted there, and the old cells count as clear ground. `/compline brume <radius>` gives them back to the biome the generator places there (`OldClearings`, `BiomeSource.getNoiseBiome` with the world's sampler, 2 chunks per tick); placing the fire or the lantern again then opens a clearing of the new kind.

Limits: a Create contraption that picks up a charged dissipator removes it without `destroy`, so its clearing stays. A player without the mod (or with the module off) sees no clearing.

Operators can open a clearing by command: `/compline clairiere <radius>` around the position of the command (4 to 256), and give the mist back with `/compline brume <radius>`, which closes the clearings whose centre is in the disc.

Config (`compline-common.toml`, `[clairiere]` section): `enabled` (true; false keeps the blocks but they no longer open anything, and monsters spawn again), `ringTicks` (6, ticks between two rings; the server sends it to the clients). Client (`compline-client.toml`, `[ciel]` section): `clearingFadeSeconds` (10; 0 gives back the ATMOSPHERICS fade).

## `dissipateur` module

The third tier, a real Create kinetic block (plan [12.4](../archive/plan/12-verification.md#124-the-custom-compline-mod)). A brass casing crossed by a shaft on the axis chosen when placing it. It needs 32 RPM or more (`SpeedLevel.MEDIUM`) and consumes 8 stress units per RPM, shown by engineer's goggles. After ten seconds of continuous rotation without overstress it converts the disc of radius 64 around it, once, until it is broken; the goggles show the progress, then "The mist has lifted around it". It keeps blowing white mist while it turns. Broken, it gives the mist back to its disc; placed elsewhere, it charges again and converts where it is. No Ponder scene yet.

Verified on the test server on 2026-10-25: driven by a creative motor at 64 RPM it is part of the Create network (stress 512), charges, converts and keeps `ComplineDone` after a restart (the check of that time, `execute if biome … compline:clairiere`, no longer applies: since 0.8.0 the biome does not change).

## `outils` module

Server and client, no mod required ([first gestures](premiers-gestes.md)). With bare hands, you gather but you do not cut. A block needs a tool when it is in the `compline:outil_requis` block tag (`#minecraft:logs` and `#minecraft:mineable/axe`, shipped in the jar) or when it would drop nothing without the correct tool (`BlockState.requiresCorrectToolForDrops()`: stone, ores, bricks, metal blocks). If the main-hand item is not correct for it (`ItemStack.isCorrectToolForDrops`), `PlayerEvent.BreakSpeed` sets the speed to 0 on both sides, so the client never predicts a break the server refuses, and `PlayerEvent.HarvestCheck` refuses the drops. On the client, a hint shows in the action bar at most every 40 ticks: « Il te faut une hache / une pioche / une pelle / une houe / un outil », chosen from the vanilla `mineable/*` tags, or « Ton outil n'est pas assez solide » when the held item is of the right kind but too weak. The `compline:sans_outil` tag keeps what grows (`#minecraft:sword_efficient`, bamboo, azaleas, a few mod plants) and snow breakable by hand. Creative players and `FakePlayer`s (Create deployers) are left alone. Both tags can be extended by datapack without recompiling.

Config (`compline-common.toml`, `[outils]` section): `enabled` (true; false gives vanilla breaking back; client and server must agree).

## `monuments` module

Client and server, no mod required. Registers the structure type `compline:terrain_checked`: it wraps another structure and only places it where the ground suits it (slope, water, room under a floating base, ceiling of the world). The Ancient Remnants structures use it; rules, values and measurements are in [monuments](monuments.md). The type is always registered, because the pack's structure files name it and a world does not load if a type is missing. No config switch: the rules are in the structure files.

## `bord` module

Client and server, no mod required; everything happens on the server. The world is a disc ([0022](../decisions/0022-disc-world.md)) and this module keeps its edge.

- **The edge** is a circle in the Overworld, read at each data load from `data/compline/monde/bord.json` (`center_x`, `center_z`, `radius`). [`scripts/terrain/disc.py`](../../scripts/terrain/disc.py) writes that file with the shape of the continent, so the two always agree. Without the file the module does nothing; the log says which case applies ("bord : cercle de rayon 6000.0 autour de 0.0, 0.0").
- **A player who passes the circle** is brought back two blocks inside it, on the line to the centre, at the same height, with the message « La brume ne laisse passer personne. » above the hotbar. A player on a boat, a horse or any other mount is brought back with it: the server moves the root vehicle, stops it and tells the riding clients (`ClientboundMoveVehiclePacket`, `ClientboundSetEntityMotionPacket`), because a ridden vehicle is steered by the client.
- **64 blocks before the edge**, a message every five seconds: « La brume s'épaissit. Le monde s'arrête un peu plus loin. »
- **Nothing is drawn.** Beyond the edge there is only open sea, and a wall would break the mist.
- **Spectators pass.** Creative players do not.
- **Ships of Sable** (airships of Create Aeronautics). Sable keeps the blocks of a ship in a plot grid far from the world, and a player seated aboard has coordinates there: `Ships` asks Sable for the real position (`SableCompanion.projectOutOfSubLevel`, found by reflection, no dependency). A player aboard a ship is **not** brought back, because the ship would stay behind and be lost; a message says « Le navire a quitté le monde connu. Faites demi-tour. » Stopping the ship itself at the edge is the next step ([next modules](#next-modules)).
- **Switch**: `bord.enabled` in `config/compline-common.toml`.

**Checked** in the test client on 2026-11-13 (mod 0.10.0, full pack, a world whose new chunks follow the disc): the first message shows at 5,990; a player teleported to 6,010 is back at 5,998; a player swimming east from 5,994 ends at 5,998; a boat driven east from 5,990 for 14 seconds is at 5,998.4 with its rider still seated and the second message on screen; a spectator teleported to 6,010 stays there. The log says that Sable was found. Not checked: a horse, elytra, an airship (tests Z2 to Z4).

## `rendu` module

Client, needs Iris. It gives the house shader pack ([shader](shader.md)) what GLSL cannot read. `rendu.IrisUniformsMixin` adds, at the end of Iris' `CommonUniforms.generalCommonUniforms`, uniforms that any program of a shader pack may declare; `ShaderUniforms` computes them once per frame:

- `brumaireLink`: 1 when the module runs, so the shader knows the others are live;
- `brumaireMist`: x is the share of mist lifted around the camera (0 in the mist, 1 eight blocks inside a clearing, eased over 0.8 s); y is the height of the ground around the camera (world surface on five columns, never above the camera, eased over 2 s), where the shader lays the foot of its mist;
- `brumaireClearing0..3`: the four clearings whose edge is nearest (centre seen from the camera, radius with the opening wave, strength), only for biomes that clear;
- `brumaireWind` and `brumaireGust0..1`: direction, strength, turbulence and the two nearest gust fronts of Where Winds Blow, read in its `DynamicWindManager` (`WindLink`, loaded only when the mod is there; a failure is logged once and the shader keeps its fallback wind).

Switch: `rendu.enabled` in `config/compline-client.toml`. Another shader pack never declares these uniforms and is not affected.

## `zoo` module

Client and server. Design and reasons in [modules-compline](modules-compline.md#zoo-the-stamps-of-archies-guidebook); this section is the reference for what the code does.

- **Stamps** (`Stamp`): `apercevoir` (spot), `photographier` (photograph), `heberger` (tame or house), `maitriser` (master). They are kept per player in the `compline:zoo` data attachment (`ZooRecord`: a bit mask per species and the ticks of observation), copied on death, and sent to the client with the `compline:zoo` payload (`ZooSync`: the whole list at login and after `/reload`, one species at a time after).
- **Spot and observe** (`Zoo.tick`, every 10 ticks): each `Mob` within `spotDistance` blocks, inside a 55° half-cone around the look direction and in line of sight, is spotted; within 16 blocks it also counts as observed. Any stamp also gives the spot stamp.
- **Photograph** (`ZooPhoto`, only when Exposure is present): each entity of Exposure's `FrameAddedEvent` (server side) is photographed by the player who took the picture.
- **Tame or house**: `AnimalTameEvent`, `BabyEntitySpawnEvent` caused by a player, or a water bucket used on a `Bucketable`. Only for species that are `OwnableEntity`, `Animal` or `Bucketable`; the others never show that stamp.
- **Master**: given as soon as the species has its other stamps and `observeSeconds` of observation.
- **Species** (`ZooSpecies`, at `ServerStartedEvent` and after each `/reload`): the mobs of the spawn settings of every Overworld biome, minus the entity type tags `compline:zoo/absents` and `compline:zoo/exclus`, plus `compline:zoo/inclus` and the species found by sight outside the list (saved in the `compline_zoo` world data). One sample of each type, made in the Overworld, gives its habitat: the tag `compline:zoo/<terre|eaux|ciel|creatures>` if any, else `Enemy` → creatures, water mob → eaux, `FlyingAnimal` or `AmbientCreature` → ciel, else terre. The tags are in `pack/kubejs/data/compline/tags/entity_type/zoo/`.
- **Guidebook** (client, only with Archie's Dinosaurs): `zoo.GuidebookDataMixin` adds the `compline_zoo` category before the credits when `GuidebookData.loadData` returns, and patches the parsed XML of Archie's pages (`GuidePatches`, from `assets/compline/archie/guide/<language>.json`: page, label index and a word that must be in the original text, else nothing changes). `zoo.CategoryTabsMixin` adds the green bookmark at the bottom and the left tabs of the habitats. The pages are our widgets (`ZooIntroWidget`, `ZooSpreadWidget`) on Archie's UI classes; creatures are drawn with `InventoryScreen.renderEntityInInventory`, tinted with ink until spotted; tooltips go through `Screen.setTooltipForNextRenderPass`, because a page is drawn into a buffer that would cut them. The images come from `scripts/zoo/textures.py`.
- **Key**: "Ouvrir le guide d'Archie" (J by default, because Iris and KubeJS already use K; category Compline) opens the guidebook without holding it.
- **Commands** (level 2): `/compline zoo especes` (species per habitat), `/compline zoo tamponner <players> <species> <stamp>`, `/compline zoo effacer <players>`.

Config (`compline-common.toml`, `[zoo]` section): `enabled` (true), `spotDistance` (25), `observeSeconds` (60).

**Checked** in the test client on 2026-11-15 (mod 0.16.0 with Archie's Dinosaurs, no other mod): 41 species (19 land, 8 water, 2 sky, 12 creatures); the bookmark opens the welcome spread, a habitat tab opens its species with silhouettes and the given stamps, tooltips show over the book; the Obsidiceratops page names the redstone caves. Not checked: the full pack's species list, the photo stamp with Exposure, taming, the bucket, mastery by observation (tests ZO1 to ZO6).

## Compatibility fixes

Fixes between two mods of the pack, outside any module: they have no switch and apply as soon as both mods are present. The fix for the inverted bell message of No Man's Land (`compat.InvertedBellMessageMixin`, 0.4.x) was removed in 0.7.0 with the mod.

### Punchy! and Sensible Stackables: the hand that kept jumping

**Symptom** (reported on 2026-10-29): with most items in hand, the first-person hand went down and up again without end and the item never showed. Tools, weapons and armour (items with durability) were not affected.

**Cause.** Punchy! plays its "hand out, hand in" animation when the held item changes. To decide, `HandEquipStateMachine.hasItemChanged` walks the vanilla components of the new stack and compares each value with the one `get` returns on the previous stack. Sensible Stackables gives each item the pack's stack size by wrapping `PatchedDataComponentMap.get` for `minecraft:max_stack_size`, but walking the components still yields the item's base value. For stone, the walk gives 64 and `get` gives 32 (decision 0012): the item "changes" every tick and Punchy! restarts the animation every tick. Items whose stack size the pack does not change (the `compline:pile/64` tag, items with durability) never differ, hence the tools that stayed steady.

**Fix.** `compat.PunchyEquipMixin` makes Punchy!'s `shouldIgnoreComponentForEquipDiff` also ignore `minecraft:max_stack_size`, as it already ignores wear (`minecraft:damage`). A stack size never changes what the hand holds, so no real item change is missed. Applied only with Punchy! (`punchy`) and Sensible Stackables (`sensible_stackables`).

**Checked** in a development client with only Punchy! 2.8g, Sensible Stackables and their libraries, with the pack's configs: before the fix, a probe logging every tick showed `max_stack_size` read as 64 by the walk and 32 by `get` with stone in hand, and the hand stayed lowered and moving with no stone drawn; after it, the stone is drawn and only Punchy!'s light idle sway remains. An iron sword was steady in both cases.

### Streams Reflowing and Spelunkery: the ice of a stream

**Symptom** (test server log, 2026-11-13): `Mixin apply for mod streamsreflowing failed streamsreflowing.mixins.json:IceMeltMixin`, as soon as Spelunkery is in the pack.

**Cause.** When ice melts, Streams Reflowing replaces the water that `IceBlock.melt` places with stream water where a stream runs (`StreamIceMelt.meltsInto`), so that a stream frozen by winter flows again in spring. Spelunkery's `IceMixin` rewrites `melt` in full (it also lets salt melt ice). Mixin does not let a mixin inject into a method another mixin of the same priority has merged, and both have the default priority: Streams Reflowing's hook was dropped and the ice of a stream thawed into still water.

**Fix** (0.9.1). `compat.IceThawMixin`, priority 1100, wraps the same `setBlockAndUpdate` call inside the method Spelunkery merged and asks Streams Reflowing's own `StreamIceMelt.meltsInto` (looked up by reflection) which water to place. Salt still melts ice. `require = 0`; applied only with Streams Reflowing (`streamsreflowing`) and Spelunkery (`spelunkery`).

**Checked** on the test server: the debug log shows `Mixing compat.IceThawMixin from compline.mixins.json into net.minecraft.world.level.block.IceBlock` with no error after it. The thaw itself has not been seen in game (test W11). If Streams Reflowing renames `StreamIceMelt`, the log says "Dégel des ruisseaux : règle de Streams Reflowing introuvable" and ice melts into plain water.

### Lithostitched and Regions Unexplored: generation that stalls

**Symptom.** A Chunky pregeneration slows to one or two chunks per second, or stops for minutes, with no error in the log. Seen four times on the terrain bench on 2026-11-12 and again on the full pack on 2026-11-13 (stopped at 32 % of 10,201 chunks).

**Cause.** Eight cave decorations of Regions Unexplored 0.6.3 (lava deltas and falls, dropleaves, cave bioshrooms, redstone buds and bulbs, ash vents, prismarite clusters) are filtered by the block predicate `lithostitched:in_structure`, up to a hundred times per chunk. In Lithostitched 1.8.1 that predicate reads the neighbouring chunks and their structures from the server level (`ServerLevel.getChunk`, then `ServerLevel.structureManager()`), while it runs on the world generation thread: each read is handed to the server thread and waited for. When the server thread is busy, which is the case during a pregeneration (it spends its spare time in `ChunkMap.processUnloads`), it answers about one read per tick. The thread dump shows `Worker-Main` parked in `ServerChunkCache.getChunk`, called from `InStructurePredicate.test`. Nothing here is specific to Chunky: a player exploring cave biomes can meet the same slowdown.

**Fix** (0.9.2). `compat.InStructureMixin` answers the predicate from the region being generated when there is one (`WorldGenRegion.getChunk`, as the game does for its own features): the structure references of the chunk and of its direct neighbours and the structure starts up to eight chunks away are already there, and no thread is waited for. Outside generation (a command, bone meal) the original code runs. `require = 0`; applied only with Lithostitched.

**Checked** on the test server, same world and same request (square of radius 800, 10,201 chunks, full pack): without the fix the task stopped at 3,259 chunks; with it the 10,201 chunks were generated in 13 min 06 s at a steady 12 to 15 chunks per second, with no error in the log. The debug log shows `Mixing compat.InStructureMixin from compline.mixins.json into dev.worldgen.lithostitched.worldgen.blockpredicate.InStructurePredicate`. A structure start nine chunks away, seen from a neighbouring chunk, is outside the region and is left out: a first version read it and stopped the generation with "Requested chunk unavailable during world generation".

## In-game tests

None of this has been done. These tests are also gathered with those of the other pages, in play-session order, in [tests-en-jeu.md](../tests-en-jeu.md).

### Sky

| # | Test | Expected |
|---|---|---|
| C1 | Normal clear night, in the Overworld | A single moon, in its place, with the right phase. Same with a scale and rotation changed in the ATMOSPHERICS config |
| C2 | `/setLunarEvent` to the white moon (may require a clear sky) | The white moon visible, smaller, without the vanilla moon behind it |
| C3 | Blood moon, blue moon, harvest moon, super moon (size 40) | Color and size specific to each event, and fade at the transition |
| C4 | Rain, then thunderstorm | The moon fades with the rain, like the rest of the sky |
| C5 | Underwater, in powder snow, with blindness | No double moon and no vanishing moon |
| C7 | `ciel.redrawMoon = false`, then `ciel.enabled = false` | Back to ATMOSPHERICS' vanilla moon |
| C8 | `/fillbiome` around a stationary player, with `clearFogOnBiomeUpdate` true then false | The mist changes immediately, then only after a step |
| C9 | KubeJS call of `ComplineSky.clearFogCache()` | Returns `true`, the mist is recomputed |
| C10 | Log at startup | "Module ciel chargé", no `compline` mixin warning |
| C11 | Frames per second at night, with and without the module | No visible difference |
| C12 | Instance without Enhanced Celestials, then without ATMOSPHERICS | The game starts; moon mixins not applied, or module ignored |

### XP

| # | Test | Expected |
|---|---|---|
| X1 | Log at startup | "Module xp chargé (aucun mod requis)", no `compline` mixin warning |
| X2 | At level 0: naming a tag, repairing a pickaxe with ingots, merging two enchanted pickaxes | Cost in green ("Usure : n"), the item can be taken, the materials are consumed |
| X3 | Repairing the same item up to wear 40 | "Trop usé !", no result (except in creative) |
| X4 | Dedicated server | The client takes the item without the slot rolling back |
| X5 | `xp.enabled = false` | The anvil asks for levels again, the bar returns |
| X6 | `xp.blockEnchantMerge = true`, enchanted book on the right | No result; a repair with ingots still works |
| X7 | On foot, on horseback, aiming a Supplementaries cannon | No XP bar or number; the jump meter stays on horseback |
| X8 | On foot, with thirst and Cold Sweat shown; then on a horse | On foot, hearts and hunger sit just above the hotbar, thirst above hunger, Cold Sweat's icon between them, nothing overlaps; on a horse, the bars go back up above the jump meter |
| X8 | Enhanced Celestials blue moon | The displayed cost goes down |

### Horizon

| # | Test | Expected |
|---|---|---|
| H1 | Log at startup, client and dedicated server | "Module horizon chargé", then on the client "horizon : brume lointaine de Distant Horizons 3.3.3 reliée"; no `compline` mixin warning; the server reaches "Done" |
| H2 | Misty forest, then veiled plain, F3 | The mist looks as without DH; no LOD visible, even from a summit |
| H3 | `/fillbiome` a clearing (or `/locate biome` once clearings exist), stand in it | The far landscape appears beyond 128 blocks, without a seam at the edge of the terrain |
| H4 | Walk from the mist into a clearing and back | Visibility changes smoothly, no popping |
| H5 | Sunrise and sunset in a clearing | LOD fog colour follows the sky (DH reads the world fog colour) |
| H6 | Underwater, blindness, powder snow | No sharp LOD in the distance |
| H7 | `horizon.enabled = false` | All LODs hidden (shipped config) |
| H8 | `skipHiddenLods` true then false, in the mist, F3 | Same image; fewer FPS with false |
| H9 | ATMOSPHERICS clouds in a clearing | Clouds drawn over the LODs, no double clouds |
| H10 | Mac Apple Silicon, 1 hour of play with DH | No JVM crash (`hs_err_pid*.log`) |
| H11 | Only if a server owner adds Chunky: Chunky `start` on the server, then a player exploring | DH pauses its generation during Chunky and resumes; no holes in the LODs (DH #607) |
| H12 | Seasons (Ecliptic) | LODs take the colours of the nearby terrain |
| H13 | From a hill in a clearing, by day, look at mountains more than 600 blocks away | They are not visible: the landscape dissolves into the haze before 500 blocks, no mist-coloured silhouette against the sky |
| H14 | Same place at night, then `ringMaxDistance = 128` in `compline-client.toml` | The ring closes at 260 blocks, then at 128 |
| H15 | Spend a minute in a clearing on a hill, then walk back into the mist and turn around | No frozen relief at the edge of the terrain in the mist |

### Terrain (Tectonic, Streams Reflowing)

Started on the test server on 2026-10-25 (with No Man's Land): the `tectonic` datapack was enabled and the Monoliths were found by `/locate`. Since 2026-11-13 the biomes are Regions Unexplored's and Geophilic's ([0019](../decisions/0019-leaving-no-mans-land.md)): the same tests apply, and the terrain of the new base has not been seen in game.

| # | Test | Expected |
|---|---|---|
| W1 | Explore 3,000 blocks (atlas) | Large continents, mountain ranges, river valleys; Regions Unexplored biomes present (`/locate biome` for a few) |
| W3 | The six Ancient Remnants monuments (`/locate structure ancient_remnants:…`) | Whole, on the ground, not cut by water, monoliths clear of hills ([monuments](monuments.md)) |
| W4 | A Streams stream in a swamp | Note which biome it carries; no error in the log |
| W5 | Highest peak (F3) | Y ≤ 320; airships can cross a range (otherwise lower `vertical_scale`) |
| W6 | Winter (Ecliptic) at Y 70 and Y 150 | Snow consistent with altitude; streams freeze like rivers |
| W7 | Boat, Sable hull on a stream, airship above | The hull drifts (`vesselDriftSpeed`), the airship in flight does not |
| W8 | Create pump and boiler on a stream | Water accepted, Create channels do not overflow |
| W9 | Pregeneration of radius 1,000 (`/dh pregen`, or Chunky if added) with spark | Generation speed noted, compared with Tectonic alone; the rate never falls to one or two chunks per second for minutes (`compat.InStructureMixin`) |
| W10 | A clearing crossed by a stream | Note whether the stream stays misty (river biome not convertible); if so add the river biomes to `compline:peut_devenir_clairiere` |
| W11 | A stream frozen by winter, then spring; ice placed on a stream next to a torch | The ice thaws into flowing stream water, not into a still pool. The log still shows Streams Reflowing's `IceMeltMixin` warning at start: expected, `compat.IceThawMixin` does its work |

### Clearings

| # | Test | Expected |
|---|---|---|
| K1 | Place a vigil fire in a misty forest | "La brume se retire." above the hotbar; within seconds the mist becomes light around you, the grass turns vivid, without reconnecting |
| K2 | F3 inside the disc, then at its edge | The real biome inside and outside (never `compline:clairiere`); the vivid grass stops on a round edge of about 16 blocks |
| K3 | Lantern in a snowy taiga, fire in a savanna | Cold look (bluish green, snow still falls), warm look (yellow green) |
| K4 | Clearing on a coast | The ocean and the beach keep their mist and colours; a river crossing the disc clears |
| K5 | Leave the fire in place, leave and come back, restart the server | The clearing stays |
| K12 | Break the fire, then a lantern | "La brume revient."; the mist comes back over its disc within seconds |
| K13 | A fire inside a lantern's disc, break the fire; then the lantern | Nothing changes; then the whole disc goes back to the mist |
| K14 | Charged dissipator, broken | The mist comes back over 64 blocks |
| K15 | The fire and the lantern side by side, by day and at night | Neither looks like a vanilla campfire or lantern; the pale flame flickers, the embers and the crystal glow |
| K16 | Place a lantern, wait, then break it, watching the edge of the disc | The grass turns green ring by ring from the lantern outwards, cloud puffs drift away and the mist thins over 4 or 5 seconds; on breaking, the reverse, from the edge inwards |
| K6 | Clearing music | Node "Clairière" of True Adaptive Music after 30 s |
| K7 | Far view from a clearing on a hill | Distant Horizons landscape beyond 128 blocks (H3) |
| K8 | Dissipator driven at 16 RPM, then 64 RPM, then overstressed | Nothing at 16; at 64 the goggles count up to 100 % and the disc of 64 converts; overstress resets the charge |
| K9 | Dissipator on an airship (Sable) | Note what happens: it converts where the ship is when it finishes charging |
| K10 | Ecliptic Seasons and Cold Sweat in a clearing, compared with the same biome ten blocks outside | Same temperature, same rain or snow, same crop humidity and growth; the grass and leaves follow the season in both |
| K17 | A night in a clearing, then a night just outside | No monster spawns inside; they do outside and can walk in |
| K18 | The biome's animals in a clearing | The fauna of the real biome spawns as elsewhere (not the farm animals of the former clearing biome) |
| K19 | A world made before 0.8.0 with a clearing: `/compline brume 64` on it, then place the lantern again | F3 shows the generator's biome again, then the clearing opens as in K1 |
| K20 | F3+T (resource reload) and a season change inside a clearing | The vivid colours stay |
| K11 | Recipes in EMI | The three recipes show, with their French names and tooltips, and the new vanilla ingredients (the fire: honeycomb, smooth stone slabs, charcoal, smooth stone; the lantern: iron plates, glass panes, amethyst shard) |

### World edge and disc

| # | Test | Expected |
|---|---|---|
| Z1 | Walk or swim to the edge (`/tp @s 5990 70 0`, then east) | A message 64 blocks before; at 6,000 the player is set back two blocks, again and again, with the second message; no fall, no damage |
| Z2 | The same in a boat, then on a horse along a beach near the edge if one exists | The boat or the horse comes back with its rider, who stays seated |
| Z3 | The same with elytra or in creative flight | Stopped the same way; a spectator passes |
| Z4 | An airship flown past the edge | The pilot stays aboard, the ship is not stopped (known limit), the message asks to turn back |
| Z5 | Sail around the continent | The coast never comes closer than about 150 blocks to the edge; no land beyond the edge with Distant Horizons |
| Z6 | Walk from the north coast to the south coast | Cold in the north, hot in the south, with no straight limit between climates |
| Z7 | A new world with another seed | Still one continent on the disc; the inland seas differ |

### Tools

The G tests of [premiers-gestes](premiers-gestes.md#in-game-tests).

### Zoo

| # | Test | Expected |
|---|---|---|
| ZO1 | `/compline zoo especes` in a world of the full pack | Wildlife of the mods counted, no vanilla monster refused by In Control, no dinosaur, villager or golem |
| ZO2 | Look at a fox from 20 blocks, then turn away | The spot stamp in the action bar with a sound, once; nothing for a fox behind a wall or behind you |
| ZO3 | Take a photo of a cow with an Exposure camera | The photograph stamp for the cow (and the spot stamp if missing) |
| ZO4 | Tame a wolf, breed two cows, catch a cod in a water bucket | The third stamp for each; a creeper or a bat shows no third stamp in the guidebook |
| ZO5 | With the three other stamps, stay a minute near the species | The master stamp and a gold line in the chat; the hover shows the seconds counted before that |
| ZO6 | J, then the green bookmark; each left tab; die and come back | The welcome spread, the habitats, the stamps kept after death; Archie's Obsidiceratops and Chorusoatl pages name no Nether or End |

### Held items

| # | Test | Expected |
|---|---|---|
| A1 | Hold stone, dirt, a torch, bread, then stand still for 10 seconds | The item is drawn in hand and stays there; only Punchy!'s light sway |
| A2 | Scroll the hotbar between those items and a sword | One lowering then raising of the hand per change, no more |
| A3 | Place stone blocks until the stack shrinks, eat bread | No hand-out animation when the count drops; the eating animation plays normally |

## Maintenance

At each update of ATMOSPHERICS, Enhanced Celestials, Traveler's Backpack, Thirst Was Reclaimed or NeoForge:

1. recompile the mod (the CI does it). An error shows that an API has changed;
2. launch the game and read the log: a mixin that no longer finds its target leaves a warning there;
3. rerun the tests of the module concerned.

Known fragile points:
- ATMOSPHERICS: its mixin at the head of `renderSky`, the `moon_phases.png` texture, the name of `BiomeBlendUtil.resetCache`;
- Enhanced Celestials 2: `ECWorldRenderer.changeMoonColor`, `LunarEvent.getMoonTextureLocation` and `getMoonSize` (mod id `enhancedcelestials2core`);
- Traveler's Backpack: the `EffectFluid` constructor and the moment it empties its registry;
- Thirst Was Reclaimed: `WaterPurity` and `ThirstHelper`.
- Distant Horizons: the `DhApiBeforeFogRenderEvent` and `DhApiBeforeRenderEvent` events (API 7), `GLState.saveState`, `AbstractChunkyAccessor.tryRunFirstTimeSetup` and its `listenerBound` field.
- Punchy!: the `HandEquipStateMachine.shouldIgnoreComponentForEquipDiff` method (class `punchy.client.state.HandEquipStateMachine`). If an update of Punchy! or Sensible Stackables fixes the mismatch on its side, the mixin can go.
- Create: `KineticBlockEntity` (`write`/`read`, `addToGoggleTooltip`), `RotatedPillarKineticBlock`, `BlockStressValues.IMPACTS`; the compile classpath pins Ponder and Flywheel to the versions Create bundles (`build.gradle`).
- Clearings: the names of the ATMOSPHERICS methods that read the biome (`BiomeBlendUtil.collect`, `AtmosphericEnvironmentVisuals.getBiomeId`, `AirHazeBiomeSettings.smooth`, `HorizonHazeCometRenderer.renderHaze` and `getCurrentBiomeSettings`), True Adaptive Music's `BiomePredicate.test`, Sodium's `LevelColorCache.updateColorBuffers`. All use `require = 0`: a renamed method leaves that part of the look off (mist, music or colours) without an error, so check K1, K6 and K2 after an update.
- Minecraft and NeoForge: `ClientLevel.calculateBlockTint`, `Biome.getGrassColor`, `getFoliageColor` and `getWaterColor`, `MobSpawnEvent.PositionCheck`, `ChunkAccess.fillBiomesFromNoise` and `ChunkMap.resendBiomesForChunks` (clearings, the last two for old worlds only); the read of `Player.experienceLevel` in `AnvilMenu.mayPickup`, the names of the `EXPERIENCE_BAR` and `EXPERIENCE_LEVEL` layers, the cancellation of `AnvilUpdateEvent`.

## Next modules

Eight modules approved on 2026-10-26 (`voile`, `zoo` (built in 0.16.0), `vie`, `fanal`, `eclaircie`, `sillage`, `echos`, `survivants`) are designed in [modules-compline.md](modules-compline.md), with their order of work. The modules of [12.4](../archive/plan/12-verification.md#124-the-custom-compline-mod) not yet written (grave, tracks, blueprints, finishing touches) stay possible. Each module will have its section here, its switch and its tests.

**World edge and ships** (after 0.10.0). The `bord` module lets a player aboard a Sable ship pass the edge. Next step: stop the ship itself, by removing the outward part of its speed near the edge (`RigidBodyHandle.addLinearAndAngularVelocity`) and, past the edge, moving it back (`RigidBodyHandle.teleport`). It needs an airship built in a test world to be checked.
