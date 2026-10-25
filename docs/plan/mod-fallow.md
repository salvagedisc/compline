# The custom `fallow` mod

The mod proposed in [12.4](12-verification.md#124-the-custom-fallow-mod) and retained by decision 17 of [12.5](12-verification.md#125-decisions-that-are-added-continuing-1012). It is made of modules that are enabled one by one. Version 0.3.0 contains the **ciel** (sky), **sac** (bag), **xp** and **horizon** (Distant Horizons) modules.

> **Status as of 2026-10-25.** Version 0.3.0 compiles (`./gradlew build`, against Enhanced Celestials 2 and Distant Horizons 3.3.3) and the full pack starts on a dedicated server with it, but **nothing has been tested in game**. Everything below comes from reading the code of the targeted mods (jars opened with `javap`). The list of [in-game tests](#in-game-tests) says what must be checked before relying on it.

## Building and updating

| What | Where |
|---|---|
| Sources | `mods-custom/fallow/`: official ModDevGradle 1.21.1 MDK, Gradle wrapper 9.2.1, Java 21 |
| Java package | `io.github.salvagedisc.fallow` |
| Jar in the pack | `pack/mods/fallow-<version>.jar`, a raw jar (no `.pw.toml`) |
| CI | `mod-fallow` job of `.github/workflows/build.yml`: it compiles the mod and keeps the jar as an artifact |

To release a version:

```sh
cd mods-custom/fallow
./gradlew build                     # jar in build/libs/fallow-<version>.jar
cp build/libs/fallow-<version>.jar ../../pack/mods/   # remove the old jar
cd ../../pack && packwiz refresh
```

The version is set in `gradle.properties` (`mod_version`). The targeted mods are used only at compile time (`compileOnly`, Modrinth Maven repository). `build.gradle` reads their version from `pack/mods/<slug>.pw.toml`: the mod is always compiled against the exact versions in the pack, and an update of one of these mods shows up at the next compilation.

## Principles

- **One module = one switch.** Each module has its `enabled` boolean in the config: `config/fallow-client.toml` for `ciel`, `config/fallow-common.toml` for `sac` and `xp`. The values are re-read at each use, so a change in game takes effect immediately.
- **A module loads only if its mods are present.** At startup, `Fallow` checks with `ModList` the mods each module depends on. It writes "Module X chargé" or "Module X ignoré" to the log. Mixins go through `FallowMixinPlugin`, which applies them only if the targeted mods are in `LoadingModList`. The only hard dependency is NeoForge.
- **No copied code.** The mod goes through the targeted mods' public APIs and through targeted mixins, on vanilla code when possible.

| Module | Side | Required mods | Mixins |
|---|---|---|---|
| `ciel` | client | ATMOSPHERICS; Enhanced Celestials for the moon | 4, all client |
| `sac` | both (the effect runs on the server) | Traveler's Backpack and Thirst Was Reclaimed | none |
| `xp` | both (the anvil on both sides, the bar on the client) | none | 1, common (`xp.AnvilMenuMixin`, on vanilla code) |
| `horizon` | both (the fog on the client, the Chunky fix on both sides) | Distant Horizons (Chunky for its fix) | 3: `horizon.FogRendererMixin` and `horizon.GLStateMixin` client, `horizon.ChunkyAccessorMixin` common |

## `ciel` module

### Enhanced Celestials' moon under ATMOSPHERICS

**The problem.** ATMOSPHERICS cancels `LevelRenderer.renderSky` (injection at the start) and redraws the whole sky. Its moon still uses the vanilla `moon_phases.png` texture, at size 20 and in white. Enhanced Celestials' mixins on `renderSky` therefore no longer run: the white moon, the blood moon and the other events keep the appearance of the vanilla moon. The white moon is also smaller (14) than the vanilla moon: redrawing it on top is not enough, ATMOSPHERICS' moon must be hidden first.

**The mechanism** (`MoonOverlay` and three mixins):

1. `LevelRendererMixin` wraps the call to `renderSky` in `renderLevel` (`@WrapOperation`). Just before the call, the module arms itself if the dimension has Enhanced Celestials lunar data.
2. The same mixin spots the first `Runnable.run()` of `renderSky`: this is the fog setup, at the start of the vanilla body. If it runs, ATMOSPHERICS has not taken over (it does not do so in No Man's Land dimensions or during the Friend Moon); Enhanced Celestials then draws its moon itself, and `fallow` touches nothing.
3. Otherwise, `RenderSystemMixin` replaces, during ATMOSPHERICS' sky only, the `moon_phases.png` texture with a transparent texture (`fallow:textures/environment/hidden_moon.png`). ATMOSPHERICS' moon quad no longer draws anything.
4. After `renderSky`, if the moon was indeed hidden, `fallow` draws the Enhanced Celestials one at the same place, with the same matrices:
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
  Java.loadClass('io.github.salvagedisc.fallow.api.FallowSky').clearFogCache()
  ```

  It returns `true` if the cache was cleared, or scheduled to be cleared on the render thread. It returns `false` if ATMOSPHERICS is absent, if the module is off or if we are on a server. `FallowSky.isFogCacheAvailable()` answers the same question without clearing anything. KubeJS may require the class to be allowed in its class filter. A server script that wants to trigger the clearing on players' machines must send them a packet, for example with `NetworkEvents.dataReceived` on the client side.

The cache is cleared by `BiomeBlendUtil.resetCache()`, a private ATMOSPHERICS method, reached through a mixin accessor (`@Invoker`).

## `sac` module

**The mechanism.** Traveler's Backpack has a fluid effect API (`api.fluids.EffectFluid`). Each effect registered for a fluid runs when the player drinks that fluid through the hose. `fallow` adds the `fallow:thirst_water` effect on water, next to the backpack's own, which puts out fire. The backpack empties its effect registry during its common initialization: the `fallow` effect is therefore registered at the end of loading (`FMLLoadCompleteEvent`). The log announces it with "sac : effet fallow:thirst_water enregistré". The module has no mixin.

At each sip, on the server:

1. the purity of the tank's water is read through the Thirst Was Reclaimed API (`WaterPurity.getPurity`);
2. the effects of that purity apply (`WaterPurity.givePurityEffects`: nausea, poison, depending on the TWR config). If the water makes the player sick and TWR does not quench thirst in that case (`quenchThirstWhenDebuffed`), we stop there;
3. otherwise the player gains the hydration and thirst quenching of a bottle of water (`minecraft:potion`) according to TWR (6 hydration by default), multiplied by `sac.bottlesPerDrink`.

Config: `sac.enabled` (true by default) and `sac.bottlesPerDrink` (1 to 4, default 1).

### What is deduced from the code

The purity of the tank's water does exist: TWR stores it in the `thirst:purity` component of the `FluidStack`, from 0 (dirty) to 3 (purified). What the jars show:

- **A sip removes 1000 mB.** The hose removes the largest amount requested by the fluid's effects (1000 for water). With less than 1000 mB in the tank, nothing happens.
- **A bucket keeps its purity.** The backpack fills its tanks with `FluidUtil`; TWR rewrites the passage through `FluidBucketWrapper` and adds the bucket's purity there.
- **A mixture keeps the lowest purity.** The backpack's tanks are NeoForge `FluidTank`s, which TWR also modifies.
- **Water drawn by the hose in the world counts as purified.** The suction mode fills the tank with `new FluidStack(water, 1000)`, with no purity component. But TWR reads a missing component as purified water (`MISSING_COMPONENT_PURITY = 3`). It even removes the component when it equals 3, so "missing" and "purified" cannot be told apart. The module follows TWR's rule: **water drawn directly from a source is clean**. This is a shortcut that avoids purifying water, to be settled after the in-game test. Possible fix: a mixin targeted at the hose's suction, which would give the water the purity of the block drawn.

## `xp` module

Fallow no longer has experience ([xp.md](xp.md)). Orbs and point gains are disabled by `pack/kubejs/server_scripts/sans_xp.js`. The module does the two things KubeJS cannot do cleanly.

**The anvil without levels.** Vanilla only returns the item if `player.experienceLevel >= cost` and `cost > 0` (`AnvilMenu.mayPickup`). `AnvilMenuMixin` replaces, in that method only, the read of `experienceLevel` with `Integer.MAX_VALUE` (MixinExtras `@ModifyExpressionValue`). The rest of the computation does not change:

- the cost is still at least 1, and the limit of 40 set by `createResult` remains. It becomes **wear**: "Usure : n" (wear: n), then "Trop usé !" (too worn!) (vanilla keys `container.repair.cost` and `container.repair.expensive`, translated in `pack/kubejs/assets/minecraft/lang/`);
- the materials (ingots, sacrificed item) are still taken; `onTake` still removes `cost` levels, which does nothing at 0;
- Enhanced Celestials' blue moon, which divides the cost in `createResult`, therefore reduces wear.

The mixin applies on both sides: the client also refuses to take the item if it thinks the levels are missing, and it colors the cost red. Client and server must have the same version of the mod and the same `xp.enabled` value. No mod in the pack patches `mayPickup`.

**Enchantment merging (optional).** With `xp.blockEnchantMerge`, `XpModule` cancels `AnvilUpdateEvent` when the right-hand item carries enchantments (enchanted book or enchanted item): the anvil gives no result, and enchanting goes through the table. Off by default: the maintainer keeps merging.

**The hidden bar.** On the client, `XpClient` cancels the `VanillaGuiLayers.EXPERIENCE_BAR` and `EXPERIENCE_LEVEL` layers in `RenderGuiLayerEvent.Pre`. The horse jump meter (`JUMP_METER`) is another layer and stays. Hearts and hunger keep their height: a small gap remains where the bar was.

Config (`fallow-common.toml`, `[xp]` section): `enabled` (true), `hideBar` (true), `blockEnchantMerge` (false). The last two only matter if `enabled` is true. To roll back: `enabled = false` and delete `sans_xp.js`.

## `horizon` module

Decision [0011](../decisions/0011-tectonic-streams-distant-horizons.md). Distant Horizons (DH) 3.3.3, API 7.2.0, read with `javap`.

### The far landscape follows the mist

**The problem.** DH draws its LODs (simplified far terrain) after the normal terrain, then applies its own fog to them, in a full-screen pass. That fog is measured as a fraction of the LOD radius and knows nothing about ATMOSPHERICS. By default DH also pushes the vanilla fog out to infinity (`MixinFogRenderer`), which would erase the pack's mist: the shipped config sets `enableVanillaFog = true`.

**The mechanism.**
1. `FogRendererMixin` reads, at the end of `FogRenderer.setupFog` for the terrain (`FOG_TERRAIN`), the fog start and end left in `RenderSystem` (`TerrainFog`). Its priority (1500) places it after ATMOSPHERICS and DH at the same point: it reads the final mist, already smoothed over time by ATMOSPHERICS.
2. After DH's initialisation (`DhApiAfterDhInitEvent`), `HorizonClient` registers two DH events, if the DH API major version is 7.
3. `DhApiBeforeFogRenderEvent`, every frame: `HorizonFog` sets DH's far fog to linear, from `start / R` to `end / R` (R = LOD radius in blocks, read from DH's config), thickness 0 to 1. A LOD at distance d is therefore as fogged as a block of terrain at the same distance.
4. `DhApiBeforeRenderEvent`: when the mist ends before the edge of the normal terrain (render distance × 16 blocks), every LOD is beyond the end of the mist, so invisible; the module cancels the whole LOD rendering of the frame. In misty areas DH costs no GPU time.
5. Underwater, in lava or powder snow, blinded or in darkness, the module does nothing: DH already drowns its LODs.

**What it gives with the `fallow_v3` preset.** Misty (end 52) and veiled (end 96) families: no LOD drawn. Clearings (end 500 by day, 260 at night): the landscape appears progressively beyond 128 blocks. The transition follows ATMOSPHERICS' smoothing when walking in or out of a clearing.

**Fallback.** The shipped `DistantHorizons.toml` has `farFogMin = farFogMax = 1`: without the module (switched off, error, other API version) every LOD is fully in the fog. No regression: the pack looks as it did without DH.

Config (`fallow-client.toml`, `[horizon]` section): `enabled` (true), `skipHiddenLods` (true).

### Two fixes for DH 3.3.3

- **Crash when creating the world with Chunky** (`ChunkyAccessorMixin`, common). DH binds to Chunky's events on the first chunk loaded. On NeoForge the spawn chunks load before Chunky has started: `ChunkyProvider.get()` throws "Chunky is not loaded" and the server crashes during `createLevels` (observed on the test server, dedicated server; single player goes through the same code). The mixin catches the error and puts DH's `listenerBound` flag back to false: DH tries again on the next chunk, once Chunky is ready. Verified: with it, the full pack starts.
- **Native memory overflow on Apple Silicon** (`GLStateMixin`, client). `GLState.saveState` reads `GL_POLYGON_MODE` with `glGetInteger`, which reserves one integer; Apple's OpenGL driver writes two, which corrupts native memory and makes the JVM crash at random (DH issue #1346, fixed upstream after 3.3.3). The mixin reads that value into an array of two integers and keeps the first. Not verified on a Mac.

Both use `require = 0`: if a future DH no longer has the targeted code, they do nothing.

## In-game tests

None of this has been done. To be run on an instance of the pack, then ticked off here.

### Sky

| # | Test | Expected |
|---|---|---|
| C1 | Normal clear night, in the Overworld | A single moon, in its place, with the right phase. Same with a scale and rotation changed in the ATMOSPHERICS config |
| C2 | `/setLunarEvent` to the white moon (may require a clear sky) | The white moon visible, smaller, without the vanilla moon behind it |
| C3 | Blood moon, blue moon, harvest moon, super moon (size 40) | Color and size specific to each event, and fade at the transition |
| C4 | Rain, then thunderstorm | The moon fades with the rain, like the rest of the sky |
| C5 | Underwater, in powder snow, with blindness | No double moon and no vanishing moon |
| C6 | No Man's Land dimension, Friend Moon | The sky in these cases stays that of their mod (log without `fallow` error) |
| C7 | `ciel.redrawMoon = false`, then `ciel.enabled = false` | Back to ATMOSPHERICS' vanilla moon |
| C8 | `/fillbiome` around a stationary player, with `clearFogOnBiomeUpdate` true then false | The mist changes immediately, then only after a step |
| C9 | KubeJS call of `FallowSky.clearFogCache()` | Returns `true`, the mist is recomputed |
| C10 | Log at startup | "Module ciel chargé", no `fallow` mixin warning |
| C11 | Frames per second at night, with and without the module | No visible difference |
| C12 | Instance without Enhanced Celestials, then without ATMOSPHERICS | The game starts; moon mixins not applied, or module ignored |

### Bag

| # | Test | Expected |
|---|---|---|
| S1 | Bucket of dirty water (filled at a source) emptied into the tank, then drunk through the hose | TWR's dirty water effects; quenches thirst or not depending on its config |
| S2 | Bucket of purified water (furnace or campfire) | Quenches thirst, without negative effect |
| S3 | Water drawn by the hose from a lake | Quenches thirst like purified water (shortcut to settle, see above) |
| S4 | Mixture of dirty and purified water in a tank | The lowest purity wins |
| S5 | One sip, with `bottlesPerDrink` at 1 then at 3 | 1000 mB removed; 6 then 18 hydration (TWR defaults) |
| S6 | Drinking while on fire | The fire is still put out (backpack effect) |
| S7 | Dedicated server | The thirst bar updates on the player's side |
| S8 | `sac.enabled = false` | The hose no longer quenches thirst |
| S9 | Log at startup | "Module sac chargé" then "sac : effet fallow:thirst_water enregistré" |
| S10 | Tank with less than 1000 mB | Nothing happens, as before |

### XP

| # | Test | Expected |
|---|---|---|
| X1 | Log at startup | "Module xp chargé (aucun mod requis)", no `fallow` mixin warning |
| X2 | At level 0: naming a tag, repairing a pickaxe with ingots, merging two enchanted pickaxes | Cost in green ("Usure : n"), the item can be taken, the materials are consumed |
| X3 | Repairing the same item up to wear 40 | "Trop usé !", no result (except in creative) |
| X4 | Dedicated server | The client takes the item without the slot rolling back |
| X5 | `xp.enabled = false` | The anvil asks for levels again, the bar returns |
| X6 | `xp.blockEnchantMerge = true`, enchanted book on the right | No result; a repair with ingots still works |
| X7 | On foot, on horseback, aiming a Supplementaries cannon | No XP bar or number; the jump meter stays on horseback |
| X8 | Enhanced Celestials blue moon | The displayed cost goes down |

### Horizon

| # | Test | Expected |
|---|---|---|
| H1 | Log at startup, client and dedicated server | "Module horizon chargé", then on the client "horizon : brume lointaine de Distant Horizons 3.3.3 reliée"; no `fallow` mixin warning; the server reaches "Done" |
| H2 | Misty forest, then veiled plain, F3 | The mist looks as without DH; no LOD visible, even from a summit |
| H3 | `/fillbiome` a clearing (or `/locate biome` once clearings exist), stand in it | The far landscape appears beyond 128 blocks, without a seam at the edge of the terrain |
| H4 | Walk from the mist into a clearing and back | Visibility changes smoothly, no popping |
| H5 | Sunrise and sunset in a clearing | LOD fog colour follows the sky (DH reads the world fog colour) |
| H6 | Underwater, blindness, powder snow | No sharp LOD in the distance |
| H7 | `horizon.enabled = false` | All LODs hidden (shipped config) |
| H8 | `skipHiddenLods` true then false, in the mist, F3 | Same image; fewer FPS with false |
| H9 | Better Clouds in a clearing | Clouds drawn over the LODs, no double clouds |
| H10 | Mac Apple Silicon, 1 hour of play with DH | No JVM crash (`hs_err_pid*.log`) |
| H11 | Chunky `start` on the server, then a player exploring | DH pauses its generation during Chunky and resumes; no holes in the LODs (DH #607) |
| H12 | Seasons (Ecliptic) and Polytone colours | LODs take the colours of the nearby terrain |

### Terrain (Tectonic, Streams Reflowing)

Started on the test server on 2026-10-25: the `tectonic` datapack is enabled, NML biomes, the Meeting Point and the Monoliths are found by `/locate`. The Meeting Point of seed `fallow` is 2,898 blocks from spawn, beyond the 2,500 of `nomansland-common.toml`: to watch with the larger Tectonic continents.

| # | Test | Expected |
|---|---|---|
| W1 | Explore 3,000 blocks (atlas) | Large continents, mountain ranges, river valleys; NML biomes present (`/locate biome` for each) |
| W2 | `/locate structure nomansland:meeting_point` on three seeds | Always on reachable land |
| W3 | The six Monoliths (`/locate structure ancient_remnants:…`) | Whole, on the ground, not cut by water |
| W4 | A Streams stream in a swamp | It carries `nomansland:blackwater_river` |
| W5 | Highest peak (F3) | Y ≤ 320; airships can cross a range (otherwise lower `vertical_scale`) |
| W6 | Winter (Ecliptic) at Y 70 and Y 150 | Snow consistent with altitude; streams freeze like rivers |
| W7 | Boat, Sable hull on a stream, airship above | The hull drifts (`vesselDriftSpeed`), the airship in flight does not |
| W8 | Create pump and boiler on a stream | Water accepted, Create channels do not overflow |
| W9 | Chunky radius 1,000 with spark | Generation speed noted, compared with NML alone |
| W10 | A clearing crossed by a stream | Note whether the stream stays misty (river biome not convertible); if so add the river biomes to `fallow:peut_devenir_clairiere` |

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
- Minecraft and NeoForge: the read of `Player.experienceLevel` in `AnvilMenu.mayPickup`, the names of the `EXPERIENCE_BAR` and `EXPERIENCE_LEVEL` layers, the cancellation of `AnvilUpdateEvent`.

## Next modules

In the order of [12.4](12-verification.md#124-the-custom-fallow-mod): **dissipator** (Create kinetic block, after the KubeJS prototype), then, depending on the tests, grave, tracks, blueprints and finishing touches. Each will have its section here, its switch and its tests.
