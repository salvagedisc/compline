# The custom `fallow` mod

The mod proposed in [12.4](12-verification.md#124-the-custom-fallow-mod) and retained by decision 17 of [12.5](12-verification.md#125-decisions-that-are-added-continuing-1012). It is made of modules that are enabled one by one. Version 0.2.1 contains the **ciel** (sky), **sac** (bag) and **xp** modules.

> **Status as of 2026-10-19.** Version 0.2.1 compiles (`./gradlew build`, against Enhanced Celestials 2), but **nothing has been tested in game**. Everything below comes from reading the code of the targeted mods (jars opened with `javap`). The list of [in-game tests](#in-game-tests) says what must be checked before relying on it.

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
- Minecraft and NeoForge: the read of `Player.experienceLevel` in `AnvilMenu.mayPickup`, the names of the `EXPERIENCE_BAR` and `EXPERIENCE_LEVEL` layers, the cancellation of `AnvilUpdateEvent`.

## Next modules

In the order of [12.4](12-verification.md#124-the-custom-fallow-mod): **dissipator** (Create kinetic block, after the KubeJS prototype), then, depending on the tests, grave, tracks, blueprints and finishing touches. Each will have its section here, its switch and its tests.
