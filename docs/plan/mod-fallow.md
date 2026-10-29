# The custom `fallow` mod

The mod proposed in [12.4](12-verification.md#124-the-custom-fallow-mod) and retained by decision 17 of [12.5](12-verification.md#125-decisions-that-are-added-continuing-1012). It is made of modules that are enabled one by one. Version 0.4.10 contains the **ciel** (sky), **sac** (bag), **xp**, **horizon** (Distant Horizons), **clairiere** (clearings), **dissipateur** (dissipator), **titres** (screen names) and **outils** (tools) modules. The **titres** module, removed in 0.4.3 when the titles went back into the panels, returns in 0.4.4 in another form: each screen's name centred over its top edge on GUI Revision's plate, in the look the `fallow-interface` pack gives it.

> **Status as of 2026-10-26.** Version 0.4.1 compiles (`./gradlew build`, against Enhanced Celestials 2, Distant Horizons 3.3.3 and Create 6.0.10) and the full pack starts on a dedicated server with it; the clearing conversion and the dissipator were checked on that server with commands. **Nothing has been tested in game with a player**. Everything below comes from reading the code of the targeted mods (jars opened with `javap`). The list of [in-game tests](#in-game-tests) says what must be checked before relying on it.

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
| `ciel` | client | ATMOSPHERICS; Enhanced Celestials for the moon; No Man's Land for the Friendly Moon | 6, all client |
| `sac` | both (the effect runs on the server) | Traveler's Backpack and Thirst Was Reclaimed | none |
| `xp` | both (the anvil on both sides, the bar on the client) | none | 1, common (`xp.AnvilMenuMixin`, on vanilla code) |
| `horizon` | both (the fog on the client, the Chunky fix on both sides) | Distant Horizons (Chunky for its fix) | 3: `horizon.FogRendererMixin` and `horizon.GLStateMixin` client, `horizon.ChunkyAccessorMixin` common |
| `clairiere` | both (blocks on both sides, conversion on the server) | none | none |
| `dissipateur` | both | Create | none |

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

### The Friendly Moon under ATMOSPHERICS

**The problem** (found in the first in-game test, 2026-10-28: the Friendly Moon never appeared at the Meeting Point). No Man's Land draws the Friendly Moon, its shadow in the sky and its fog from mixins inside the vanilla body of `renderSky` (`FriendMoonRenderer.renderFriendMoon` after the moon is drawn). The Friendly Moon's opacity rises only there, in `updateFriendMoonPosition`. ATMOSPHERICS cancels that body and gives it back only when `Atmospherics.isFriendMoonActive()` is true, that is when that opacity is already above 0.001. Neither can start first: the Friendly Moon could never appear, and the shadow that shows the way after the dream was never drawn either. Read in NML 1.6.3 and ATMOSPHERICS 2.6.5; ATMOSPHERICS 2.6.5.2 changes nothing there.

**The mechanism** (`FriendMoonRelay` and two mixins):

1. `FriendMoonSkyMixin` wraps the call to `renderSky`, like the moon's `LevelRendererMixin`, and also wraps the sky fog setup (`skyFogSetup`) it passes. ATMOSPHERICS runs that setup right after cancelling the vanilla body; the vanilla body runs it as its first step, which the mixin spots separately.
2. After `renderSky`, if the fog setup ran but the vanilla body did not, ATMOSPHERICS drew the sky. `fallow` then makes the call NML makes in the vanilla sky: `renderFriendMoon` and `renderFriendShadow`, with the same matrices, then `renderFinalize` (which lifts NML's stencil) and restores the render state.
3. At the Meeting Point at night, the Friendly Moon starts to fade in on this first frame; from the next one, ATMOSPHERICS hands the sky back to vanilla and NML carries on alone, as it was designed to (NML's fog replaces ATMOSPHERICS' mist while the Friendly Moon is up). Away from the Point, during the 8 nights after the dream, the shadow stays drawn by `fallow` over ATMOSPHERICS' sky, which keeps its mist.
4. `AtmosphericsDreamMixin` makes `Atmospherics.isFriendMoonActive()` also true while the moonlight dream is shown, so ATMOSPHERICS leaves the sky and the fog to the dream whatever the order of the two mods' mixins on `renderSky`.

**Checked on 2026-10-29** in a development client (llvmpipe) with only No Man's Land 1.6.3, ATMOSPHERICS 2.6.5 and `fallow` 0.4.10, on a new world (seed "fallow", Point at 616, −1288, pool at 631, 78, −1273), at midnight, 5 blocks from the pool: the face appears, wakes and speaks, and the "Friendship" goal is reached 10 s after joining. With `ciel.friendMoon = false`, nothing appears, as in the game of 2026-10-28. The shadow (C14) and the dream (C15) were not checked.

A drawing error is logged once, without crashing the rendering. Config: `ciel.friendMoon` (true by default; false gives back the old behaviour, where the Friendly Moon does not appear under ATMOSPHERICS).

**Known limitation.** On the single frame where `fallow` fades the Friendly Moon in, it is drawn over ATMOSPHERICS' stars instead of hiding them; the shadow, drawn the whole night away from the Point, darkens the stars behind it instead of masking them.

## `sac` module

> **Inactive since 2026-10-26.** Traveler's Backpack left the pack for Reliable Backpacks ([0012](../decisions/0012-sacs-piles-jours.md)). The module switches itself off ("Module sac ignoré") and is removed in 0.5.0, with its tests S1 to S9.

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

**The hidden bar.** On the client, `XpClient` cancels the `VanillaGuiLayers.EXPERIENCE_BAR` and `EXPERIENCE_LEVEL` layers in `RenderGuiLayerEvent.Pre`. The horse jump meter (`JUMP_METER`) is another layer and stays. The space the bar left is taken back: `Gui.leftHeight` and `rightHeight`, reset to 39 at each frame, are set to 34 in the same event, just before the health and food layers read them. Hearts, armor, hunger, air and what mods stack above them with these fields (Thirst Was Reclaimed's thirst) come down 5 pixels, leaving one pixel above the hotbar's selection frame. Not while the jump meter is shown (on a horse), where everything keeps its vanilla height. Cold Sweat places its body temperature icon by itself: `pack/config/coldsweat/client.toml` moves it down by the same 5 pixels (`Body Temperature Icon Offset`, `Body Temperature Readout Offset`).

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

## `clairiere` module

Plan [10.3](10-recentrage.md#103-the-mist-that-lifts), layer 3: the mist lifts where the players decide. It replaces the KubeJS drafts `clairieres.js` and `clairiere.json` of `kubejs-brouillons/`.

**Blocks.**

| Block | Radius | Recipe |
|---|---|---|
| Vigil Fire (`fallow:feu_de_veille`), a stone hearth with embers | 16 | No Man's Land resin, two charcoal, a log, three cobblestone |
| Clearing Lantern (`fallow:lanterne_de_clairiere`) | 32 | A lantern, two Overgeared iron plates, two resin |
| Dissipator (`fallow:dissipateur`, `dissipateur` module) | 64 | Encased fan, brass casing, propeller, two precision mechanisms |

**Conversion.** Placed by a player, the fire and the lantern change the biomes of a disc around them into a clearing, for good: breaking the block does not bring the mist back. `ClearingConverter` works like `/fillbiome` without its 32,768-block limit. It rewrites the 4 × 4 × 4 biome cells of each chunk over the whole height, two chunks per tick (`chunksPerTick`), then sends the new biomes to the clients. The `ciel` module then empties the ATMOSPHERICS cache, so the mist reacts without the player moving. Only the biomes of the `fallow:peut_devenir_clairiere` tag change: land biomes and rivers, not oceans, beaches or caves. The clearing follows the temperature of the biome it replaces: `fallow:clairiere_froide` below 0.3 (snow keeps falling), `fallow:clairiere_chaude` above 1.0, `fallow:clairiere` otherwise. The three biomes have a light mist (`fallow_v3` preset, 40 → 500 blocks by day), vivid grass (biome and Polytone), the clearing music (node 8 of the [music](musique.md)), farm animals and salmon, and they show the far landscape of Distant Horizons (`horizon` module).

The conversions in progress are lost if the server stops; placing the block again finishes them. Operators can convert by command: `/fallow clairiere <radius>` around the position of the command (4 to 256).

Config (`fallow-common.toml`, `[clairiere]` section): `enabled` (true; false keeps the blocks but they no longer convert anything), `chunksPerTick` (2).

## `dissipateur` module

The third tier, a real Create kinetic block (plan [12.4](12-verification.md#124-the-custom-fallow-mod)). A brass casing crossed by a shaft on the axis chosen when placing it. It needs 32 RPM or more (`SpeedLevel.MEDIUM`) and consumes 8 stress units per RPM, shown by engineer's goggles. After ten seconds of continuous rotation without overstress it converts the disc of radius 64 around it, once; the goggles show the progress, then "The mist has lifted around it". It keeps blowing white mist while it turns. Broken and placed elsewhere, it charges again and converts where it is. No Ponder scene yet.

Verified on the test server on 2026-10-25: driven by a creative motor at 64 RPM it is part of the Create network (stress 512), charges, converts (`execute if biome … fallow:clairiere` true at its position) and keeps `FallowDone` after a restart.

## `titres` module

Client only. Each screen's name leaves the panel for a plate centred over its top edge, which it overhangs by a few pixels, in engraved capitals, as GUI Revision draws the names of five of its screens. Drawing the names in the textures cannot work: a texture is shared by several screens (`generic_54` serves the chest, the barrel and the ender chest), it cannot follow the game's language, and the player can rename a chest. The mod therefore draws the name and the pack decides how it looks.

**What the pack gives** (`fallow-interface`, written by `scripts/interface/build.py`):

- `fallow:titres.json`: for each vanilla screen (by class name) its colour family, the row of its texture where the panel starts (`haut`), how many rows the plate rises above it (`dessus`, 3 by default) and, for the trader, the column of its centre; the mods whose screens take a plate (`mods`, by menu namespace, with their family); the screens to leave alone (`sans`); each family's ink and light; the letters' table (position and width of each character in the atlas).
- The plate of each family, `fallow:titre/<family>` (nine-slice sprite: its two ends and four columns of plate the game tiles to the name's width, a shadow pixel down and to the right).
- The letters, `fallow:textures/gui/titre/lettres.png`: white glyphs five pixels tall (accents above, cedilla below) in 8-pixel cells.

**What the mod does.** `TitresClient`, on `ContainerScreenEvent.Render.Background` (after the panel, before the slots), uppercases the screen's title (with its level for a trader, as the game writes it), measures it with the table, draws the plate 13 pixels wider on each side, then the name twice: in the family's light one row lower, then in its ink, which engraves it. A letter the table lacks falls back to its base letter (NFD), then is skipped. The mixins `AbstractContainerScreenMixin` (on `renderLabels`, at its head) and `OwnLabelsScreenMixin` (the inventory and the trader, which write their labels themselves) stop the game writing the title and the inventory label on those screens; what a screen adds around them (the anvil's cost, the beacon's two labels) stays.

**Which screens.** Every vanilla container screen whose labels come from the game, except the player's inventory and the creative inventory, whose top is taken. A mod's screen takes the plate only if `mods` names its mod, whose panel the pack has dressed with its top on the first row (Cold Sweat, Farm & Charm, Naturalist, Overgeared, Quark, Supplementaries); a screen that writes its labels with a method of its own keeps them, without a plate. Without the pack's table (the pack disabled), the module does nothing and the game writes its labels.

Config (`fallow-client.toml`, `[titres]` section): `enabled` (true; false gives the game its labels back).

## `outils` module

Server and client, no mod required ([first gestures](premiers-gestes.md)). With bare hands, you gather but you do not cut. A block needs a tool when it is in the `fallow:outil_requis` block tag (`#minecraft:logs` and `#minecraft:mineable/axe`, shipped in the jar) or when it would drop nothing without the correct tool (`BlockState.requiresCorrectToolForDrops()`: stone, ores, bricks, metal blocks). If the main-hand item is not correct for it (`ItemStack.isCorrectToolForDrops`), `PlayerEvent.BreakSpeed` sets the speed to 0 on both sides, so the client never predicts a break the server refuses, and `PlayerEvent.HarvestCheck` refuses the drops. On the client, a hint shows in the action bar at most every 40 ticks: « Il te faut une hache / une pioche / une pelle / une houe / un outil », chosen from the vanilla `mineable/*` tags, or « Ton outil n'est pas assez solide » when the held item is of the right kind but too weak. The `fallow:sans_outil` tag keeps what grows (`#minecraft:sword_efficient`, bamboo, azaleas, a few mod plants) and snow breakable by hand. Creative players and `FakePlayer`s (Create deployers) are left alone. Both tags can be extended by datapack without recompiling.

Config (`fallow-common.toml`, `[outils]` section): `enabled` (true; false gives vanilla breaking back; client and server must agree).

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
| C6 | No Man's Land dimension, Friend Moon | The sky in these cases stays that of their mod (log without `fallow` error) |
| C13 | Meeting Point at night (`/meetingpoint`, then within 16 blocks of the pool), look up | The sleeping face fades in, then wakes; the mist gives way to No Man's Land's fog. With `ciel.friendMoon = false`: nothing appears (the bug of 2026-10-28) |
| C14 | The 8 nights after the dream, far from the Point | The moon's shadow in the sky, towards the Point, over ATMOSPHERICS' sky and mist; it fades when arriving |
| C15 | Moonlight dream | The dream's own sky, not ATMOSPHERICS' |
| C7 | `ciel.redrawMoon = false`, then `ciel.enabled = false` | Back to ATMOSPHERICS' vanilla moon |
| C8 | `/fillbiome` around a stationary player, with `clearFogOnBiomeUpdate` true then false | The mist changes immediately, then only after a step |
| C9 | KubeJS call of `FallowSky.clearFogCache()` | Returns `true`, the mist is recomputed |
| C10 | Log at startup | "Module ciel chargé", "Lune Amie de No Man's Land dessinée sous ATMOSPHERICS", no `fallow` mixin warning |
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
| X8 | On foot, with thirst and Cold Sweat shown; then on a horse | On foot, hearts and hunger sit just above the hotbar, thirst above hunger, Cold Sweat's icon between them, nothing overlaps; on a horse, the bars go back up above the jump meter |
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

### Clearings

| # | Test | Expected |
|---|---|---|
| K1 | Place a vigil fire in a misty forest | "La brume se retire." above the hotbar; within seconds the mist becomes light around you, the grass turns vivid, without reconnecting |
| K2 | F3 at the edge of the disc | `fallow:clairiere` inside, the old biome outside, a round edge of about 16 blocks |
| K3 | Lantern in a snowy taiga, fire in a savanna | `clairiere_froide` (snow still falls), `clairiere_chaude` |
| K4 | Clearing on a coast | The ocean and the beach keep their biome; a river crossing the disc becomes a clearing |
| K5 | Break the fire, leave and come back, restart the server | The clearing stays |
| K6 | Clearing music | Node "Clairière" of True Adaptive Music after 30 s |
| K7 | Far view from a clearing on a hill | Distant Horizons landscape beyond 128 blocks (H3) |
| K8 | Dissipator driven at 16 RPM, then 64 RPM, then overstressed | Nothing at 16; at 64 the goggles count up to 100 % and the disc of 64 converts; overstress resets the charge |
| K9 | Dissipator on an airship (Sable) | Note what happens: it converts where the ship is when it finishes charging |
| K10 | Ecliptic seasons and Cold Sweat in a clearing | Crops grow, temperature consistent with the old biome |
| K11 | Recipes in EMI | The three recipes show, with their French names and tooltips |

### Titles

| # | Test | Expected |
|---|---|---|
| E1 | Furnace, crafting table, anvil, enchanting table, brewing stand, beacon, at GUI scales 2, 3 and 4 | The name centred over the top edge on its plate, in the screen's colours, overhanging by 3 pixels; no title or "Inventaire" in the panel; the anvil's cost and the beacon's two labels still there |
| E2 | A chest renamed "Réserve à blé" in an anvil, a double chest, a barrel | Its own name on the plate, accents kept, the plate as wide as the name |
| E3 | Trader at level 1 then 3, wandering trader | "VILLAGEOIS - NOVICE", then the new level; the plate centred on the offers' panel |
| E4 | Recipe book open (crafting table, furnace) | The plate moves with the panel |
| E5 | Game in English | English names, same plates |
| E6 | A Supplementaries, Farm & Charm and Overgeared screen | A mist plate with the screen's name; nothing overlaps the panel's own drawings |
| E7 | `titres.enabled = false`, then `fallow-interface` disabled | The game's labels come back in both cases, no plate |

### Tools

The G tests of [premiers-gestes](premiers-gestes.md#in-game-tests).

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
- Create: `KineticBlockEntity` (`write`/`read`, `addToGoggleTooltip`), `RotatedPillarKineticBlock`, `BlockStressValues.IMPACTS`; the compile classpath pins Ponder and Flywheel to the versions Create bundles (`build.gradle`).
- Minecraft and NeoForge: `ChunkAccess.fillBiomesFromNoise` and `ChunkMap.resendBiomesForChunks` (clearings); the read of `Player.experienceLevel` in `AnvilMenu.mayPickup`, the names of the `EXPERIENCE_BAR` and `EXPERIENCE_LEVEL` layers, the cancellation of `AnvilUpdateEvent`.

## Next modules

Eight modules approved on 2026-10-26 (`voile`, `zoo`, `vie`, `fanal`, `eclaircie`, `sillage`, `echos`, `survivants`) are designed in [modules-fallow.md](modules-fallow.md), with their order of work. The modules of [12.4](12-verification.md#124-the-custom-fallow-mod) not yet written (grave, tracks, blueprints, finishing touches) stay possible. Each module will have its section here, its switch and its tests.
