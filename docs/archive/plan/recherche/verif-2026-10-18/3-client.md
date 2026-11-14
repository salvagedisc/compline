# Verification 3: the client stack (rendering, camera, interface)

> Verification of 2026-10-18 on §10.6 and chapter 11. All versions were reread on Modrinth on 2026-10-18 (NeoForge 1.21.1). The jars were opened in the scratchpad and their mixins read with `javap`. The `*.mixins.json` files were not always enough, so all classes were scanned (Camera Overhaul loads its mixins through a plugin, LambDynamicLights stores them in nested jars).
>
> Evidence legend: **[MR]** version or dependency reread on Modrinth, **[jar]** class or file of the inspected jar, **[wiki]** official wiki page, **[issue]** GitHub issue read, **[inferred]** reasoning from the above.

---

## 1. What is confirmed

### 1.1 Available versions (NeoForge 1.21.1, as of 2026-10-18)

| Mod | Current pack | Latest version | Note | Evidence |
|---|---|---|---|---|
| Sodium | 0.8.13 | 0.8.13 | Sable requires ≥ 0.8.12 | [MR] [jar Sable `neoforge.mods.toml`] |
| ImmediatelyFast | 1.6.14 | 1.6.15 | minor update | [MR] |
| Entity Culling | 1.11.2 | 1.11.3 | breaks Aeronautics, see 3.1 | [MR] [issue] |
| ModernFix | 5.27.24 | 5.27.26 | minor update | [MR] |
| ATMOSPHERICS | 2.6.5 | 2.6.5.2 | fixes | [MR] |
| Polytone | absent | 1.21-5.1.3 (2026-10-16) | **8 versions in 10 days** since 5.0.0 of 2026-10-06; the last 4.x is 4.5.3 (2026-09-26) | [MR] |
| Particular Reforged | absent | 1.5.7 | **optional** dependencies Create, Aeronautics, Sable, TFC, Iris | [MR] |
| Subtle Effects | 1.14.3 | 1.15.1 | | [MR] |
| LambDynamicLights | 4.8.11 | 4.8.11 | | [MR] |
| More Darkness | 2.0.1 | 2.0.1 | requires YACL | [MR] |
| Entity Model Features | absent | 3.3.12 | requires ETF | [MR] |
| Entity Texture Features | absent | 7.2.6 | | [MR] |
| Fresh Animations | absent | 1.10.4 (beta) | resource pack | [MR] |
| FA Extensions (bundle) | absent | 1.8.1; Player Extension 1.1.0, Details 2.2.1, Objects 2.1.2, Emissive 1.6.0 | | [MR] |
| 3D Skin Layers | absent | 1.11.3 | has an EMF compatibility mixin (`EMFModelPartMixin`) | [MR] [jar] |
| Real Camera | absent | 0.7.8-beta | incompatible (Modrinth) with GeckoLib armours, OptiFine, Armourer's Workshop; the jar has Sable compat (`SableCompat`) | [MR] [jar] |
| Camera Overhaul | absent | 2.1.2 | requires Cloth Config | [MR] |
| Shoulder Surfing Reloaded | absent | 5.2.0 | incompatible with betterthirdperson, camerautils, nimble, valkyrienskies ≤ 2.4.11, customcameraview | [MR] [jar `neoforge.mods.toml`] |
| Spatial GUI | absent | 1.8 (2026-10-17) | requires YACL; per-screen activation lists; Polytone compat mixin | [MR] [jar] |
| Smooth Swapping | absent | 0.9.3.2 beta (2025-11) | | [MR] |
| ItemPhysic Lite | absent | 1.6.13 | | [MR] |
| MB-3D Items (`mb3d-items-pack`) | absent | 2.4.0 | resource pack | [MR] |
| Motschen's Better Leaves | absent | 9.6 | handles `nomansland` and `quark` | [MR] [pack jar] |
| More Culling | absent | 1.0.10 | requires Cloth; incompatible with Sodium < 0.8.0 | [MR] [jar] |
| EMI | absent | 1.1.24 | | [MR] |
| JEI | absent | 19.64.0.455 | useful as a bridge, see 1.6 | [MR] |
| Sounds | absent | 2.4.22+lts | requires MRU and YACL | [MR] |
| Dynamic Surroundings (OreCruncher) | absent | 0.4.6 (slug `dynamicsurroundingsfabric`) | | [MR] |
| AmbientSounds | 6.3.8 | 6.3.10 | | [MR] |
| Sound Physics Remastered | 1.5.1 | 1.5.1 (alpha) | | [MR] |
| Seamless Loading Screen | absent | 2.2.1 (2024-10-01, single version) | | [MR] |
| Default Options | absent | 21.1.8 | requires Balm | [MR] |
| No Man's Land | 1.5.12 | **1.6.1** (2026-10-14) | embeds Mixed Litter 1.2.2 and sable-companion 1.6.0 | [MR] [jar] |
| Sable | 2.0.5 | 2.0.6 | incompatible with scalablelux and sablecompanion > 1.6.0; embeds Veil | [MR] [jar] |
| Enhanced Celestials | — | 6.0.2.6 | | [MR] |

Sinytra Connector 2.0.0-beta.17 and Forgified Fabric API are in the pack [repository `pack/mods/`]. No Fabric mod is needed in this stack.

### 1.2 NML versus Fresh Animations: NML wins, through Mixed Litter

- NML does not remodel mobs itself: it **embeds Mixed Litter** 1.2.2 (same team) [jar NML, `META-INF/jarjar`].
- Mixed Litter replaces the Java renderer of each species: `PigRemodelRenderer`, `CowRemodelRenderer`, `MooshroomRemodelRenderer`, `ChickenRemodelRenderer`, `SheepRemodelRenderer`, `RabbitRemodelRenderer`, `SquidRemodelRenderer`, chosen at render time by `RemodelRenderers.get` [jar Mixed Litter].
- Setting: `config/mixed_litter-startup.toml` (type STARTUP, hence reread at launch only). The keys `chickenRemodel`, `cowRemodel`, `pigRemodel`, `rabbitRemodel`, `sheepRemodel` and the squid one are **all `true` by default**, each with its entity list (cows: `cow`, `mooshroom`, `buzzier_bees:moobloom`; squids: `squid`, `glow_squid`) [jar].
- **Conclusion.** With default settings, NML wins on the **pig, cow, mooshroom, chicken, sheep, rabbit, squid and glow squid**. These renderers do not use the vanilla layers and Fresh Animations has no `.jem` for them: FA does not animate them [jar FA, inferred].
- The plan underestimates the overlap. It speaks of "pigs, cows and squids", but **chicken, sheep and rabbit** are also taken by NML. FA therefore animates the rest: wolves, foxes, cats, horses, goats, turtles, villagers, monsters, items.
- To give a species to FA, it is enough to set that species' Mixed Litter key to `false`.

NML texture variants: `data/nomansland/mixed_litter/variants/` covers bee, cat, chicken, cod, cow, dolphin, donkey, fox (cream fox depending on the biome), goat, husk, mooshroom, pig (13 variants), salmon, sheep, slime, squid and turtle [jar NML]. They are injected by `RenderEntityVariantMixin`, a `ModifyVariable` at the `STORE` of `getTextureLocation` in `LivingEntityRenderer.getRenderType`, **hence on all mobs, remodelled or not** [jar Mixed Litter].

### 1.3 ATMOSPHERICS: the clearing is recognised, on one condition

- Presets are shipped in a resource pack, under `assets/atmospherics/preset/*.json` (read through `listResources("preset")`, namespace `atmospherics`, suffix `.json`) [jar].
- They are **applied automatically**, once per fingerprint, when no screen is open (`tickResourcePackPresetPrompt` → `applyPresetAndReload`) [jar]. The fingerprint is the list of preset IDs: a preset modified under the same name **is not reapplied**. It must be renamed at each version (`compline_v1.json`, `compline_v2.json`…) [jar, inferred].
- The active preset is copied into `config/ambientfog/biome_fog.json`. Local presets go in `config/ambientfog/presets/` [jar].
- Biomes are indexed by **ID string**, so `compline:clairiere` is accepted as is. A biome absent from the preset receives a default profile (`computeIfAbsent`), copied from `minecraft:plains` [jar].
- The base preset contains only 65 vanilla biomes, **no NML biome** [jar]. The Compline preset must name them one by one, otherwise all NML forests get the mist of the plains.
- The biome is read by `ClientLevel.getBiome` at the player's position, blended on a grid of 4-block cells (`BiomeBlendUtil`, `GRID_RADIUS` 1, step of 4) [jar].

### 1.4 /fillbiome: the mist follows, with a delay

- The client receives the new biomes through `ClientboundChunksBiomesPacket` [inferred from vanilla code].
- The `BiomeBlendUtil` cache is indexed by cell and is **not flushed** when a biome changes [jar].
- Result: standing still in the freshly created clearing, the mist stays. It changes as soon as the player enters **another 4-block cell**, then glides gently (exponential smoothing, `tau` 0.9986 per frame) [jar, inferred].
- For the "mist lifts" staging, this is rather an advantage, since the player walks. For a clearing that opens **around** a motionless player, the custom mod of 4.3 is needed.

### 1.5 What ATMOSPHERICS touches in rendering

| Target | Injection | Evidence |
|---|---|---|
| `FogRenderer.setupColor`, `setupFog` | Inject at `TAIL` | [jar] |
| `ClientLevel.getSkyColor`, `getSunriseColor` | Inject at `RETURN` | [jar] |
| Clouds | Redirect `getCloudColor` and `getCloudHeight`, `ModifyArg` in `buildClouds` | [jar] |
| `LevelRenderer.renderSky` | **Rewritten: Inject at `HEAD` with cancellation**, except underwater, under an effect (blindness…), non-normal sky, and two NML cases (Friend Moon) | [jar] |
| Sodium | turns off Sodium's fog occlusion by itself, sets the cloud distance | [jar] |
| NML | reads NML's Friend Moon by reflection | [jar] |

### 1.6 EMI: who has only a JEI plugin

| JEI plugin only (EMI reads them through JEMI if JEI is installed) | Native EMI plugin |
|---|---|
| Create (`CreateJEI`) | Overgeared (**EMI only**, no JEI) |
| Create Aeronautics / Simulated (`AeroJEI`, `SimulatedJEI`) | Kaleidoscope Cookery and KC Compat |
| Cold Sweat | Curios |
| Alex's Mobs | Supplementaries |
| Critters and Companions | Kiwi |
| Ecliptic | KubeJS |
| Quark / Zeta | Traveler's Backpack |
| Patchouli, Modonomicon | Visual Workbench |
| Knight Lib | AStages (per-player hiding) |
| Create Ore Excavation | |
| Exposure | |
| Farm & Charm **1.1.25** (1.1.29 has an EMI plugin) | |

[jar of each mod: `@JeiPlugin` and `@EmiEntrypoint` classes]. EMI contains the JEMI bridge (`dev/emi/emi/jemi`) [jar EMI]. **JEI must therefore stay in the pack**, without its interface being visible.

### 1.7 What KubeJS can do with EMI

KubeJS 2101 implements `RecipeViewerEvents` for EMI [jar KubeJS]:
- `addEntries`, `addInformation`, `removeCategories`, `removeEntries`, `removeEntriesCompletely`;
- `removeRecipes`, sent by the server (`removedGlobalRecipes`, `categoryData`), which works in multiplayer.
- **`groupEntries` is not supported for EMI**: EMI 1.1.24 has no item group API [jar EMI, no group class in the API]. §11.7 is wrong on this point.

What EMI can do with a data or resource pack, without KubeJS [jar EMI, data loaders]:
- `emi/index/stacks/*.json`: `disable`, `added` with `after` (place an item after another, for a themed order), `removed`, `filters`;
- `emi/category/properties/*.json`: order and icon of categories;
- `emi/recipe/filters/*.json`: hide recipes;
- `emi/recipe/additions/*.json`: add `emi:info` (text) and `emi:world_interaction` ("right-click X with Y gives Z") pages;
- `emi/aliases/*.json`: replacement search names;
- the index option `creative`, `registered` or `creative_plus_registered`: with `creative`, the order follows the **creative tabs**, which KubeJS can create and fill (`StartupEvents.modifyCreativeTab`, `StartupEvents.registry('creative_mode_tab')`).

The visual grouping "all planks" is therefore done through order (creative tabs or `added`/`after`) and through search, not through collapsible groups.

**Correction to the plan: per-player hiding exists.** AStages 2.5.3 has an EMI plugin and a JEI plugin that hide items and recipes according to each player's stages [jar AStages]. §11.7 says the opposite. Beware: AStages is "All Rights Reserved" [MR], which forbids modifying or redistributing it outside Modrinth.

---

## 2. The concrete links to build

| # | Link | Tool | Detail |
|---|---|---|---|
| L1 | The clearing in ATMOSPHERICS | resource pack `compline-ambiance`, `assets/atmospherics/preset/compline_v1.json` | An entry `compline:clairiere` (almost no mist, frank light) and **one entry per NML biome**. Rename the file at each version (1.3) |
| L2 | The clearing in AmbientSounds | biome tag in a KubeJS datapack (`kubejs/data/minecraft/tags/worldgen/biome/is_forest.json` or `c:` tag) | AmbientSounds associates sounds by tag (`#is_forest`) or by name fragment [jar]. Without a tag, the clearing is silent |
| L3 | The clearing in colours | the biome JSON (`grass_color`, `foliage_color`, `water_color`, `effects`) | This is the safest route. Polytone can refine it through `biome_modifiers`, without `fog_color` or `sky_color` (3.3) |
| L4 | Mixed Litter and Fresh Animations | `config/mixed_litter-startup.toml`, shipped by Default Options | Choose species by species. Proposal: NML keeps pig, cow, mooshroom, sheep and squid (the trailer's style), FA takes chicken and rabbit if they look better in game |
| L5 | NML variants on FA models | `config/entity_model_features.json` (`modelsNamesDisabled`) or override of the files `data/nomansland/mixed_litter/variants/<species>.json` by datapack | For each mob animated by FA **and** with an NML variant (cat, fox, goat, turtle, dolphin, donkey…), either the FA model is cut, or the NML variant is emptied. To be settled after test T3 |
| L6 | A single source of mist | `config/nomansland-client.toml`, section `[fog_modifiers]` (CLIENT config of `NMLConfig`): `fogModifiers = false`, or `surfaceFogModifier`, `foggyBiomeFogModifier`, `cavesBiomeFogModifier`, `deepDarkFogModifier` to false | NML adds its own fog [jar NML]. ATMOSPHERICS must be the only one responsible (§11.1) |
| L7 | A single source of variants | `pack/config/quark-common.toml`, `"Variant Animal Textures" = false` (line 173) | Duplicates NML; its textures break FA's UVs (3.5) |
| L8 | A single source of fireflies, waterfalls, dust | Particular, Subtle Effects, NML configs | See table 3.7: one owner per effect |
| L9 | NML leaves in Particular | Particular config (leaf block → particle mapping) | No NML wood is listed there [jar]: willow, walnut, maple fall as generic leaves. Add the entries, or keep Falling Leaves Plus as a fallback |
| L10 | EMI index order by theme | `kubejs/assets/compline/emi/index/stacks/compline.json` (`added` + `after`), or `creative` index + KubeJS creative tabs | Replaces the plan's "groups" |
| L11 | Create gestures visible in EMI | `emi/recipe/additions/*.json` of type `emi:world_interaction` and `emi:info` | For Compline's "in-world" recipes (clearing, rituals, ruin blueprints) that no mod declares |
| L12 | Blueprint recipes hidden then learned | AStages (EMI) **or** `RecipeViewerEvents.removeRecipes` (KubeJS, for everyone) | AStages allows "per player"; KubeJS alone hides for everyone |
| L13 | Settings shipped once | Default Options: `config/defaultoptions/extra/` | Files are copied **only if missing** [jar]: they serve as a starting point and the player keeps their choices. Resource packs (`compline-ambiance`, FA, MB-3D, Better Leaves) are enabled by default (`DefaultResourcePacksHandler`) [jar] |
| L14 | Camera Overhaul in first person | `cameraoverhaul.toml`: `general.enableInThirdPerson = false`, contexts `vehicles` and `mounts` at 0 | Leaves third person to SSR, and prevents the camera from tilting aboard airships (3.4) |
| L15 | Spatial GUI away from books | its screen list | Exclude Wildex, the Ancient Remnants codex, Patchouli, Modonomicon, the atlas, and EMI's screens |
| L16 | Enhanced Celestials moon under ATMOSPHERICS | **custom mod** (4.3) | KubeJS cannot touch sky rendering |

---

## 3. Conflicts and risks

### 3.1 Entity Culling breaks Aeronautics: to be removed

- Open issues, unanswered by the maintainers: Simulated-Project **#1347** (opened 2026-07-30), **#974** (trembling propeller sails, reconfirmed 2026-09-09, fix PR #1427 not merged), sable **#1414** [issue].
- The whitelists (`entityWhitelist`, `blockEntityWhitelist`, `tickCullingWhitelist`, `skipBlockEntityCulling`) only target entity or block types. Sable sub-levels do not lend themselves to this [jar, inferred].
- Sodium already sorts chunks and ATMOSPHERICS hides the distance under mist. Entity Culling's gain is small in an almost empty world [inferred].

### 3.2 Polytone: useful, but it switches off three other mods

- Polytone **cancels `LightTexture.updateLightTexture` at `HEAD`** as soon as a custom lightmap is active (`LightTextureMixin`) [jar 5.1.3].
- This cuts out at once:
  - the darkening of **More Darkness**, which injects just before the texture is sent [jar More Darkness];
  - the moon tint of **Enhanced Celestials**, injected on `blockLightRedFlicker` [jar EC];
  - the true darkness and the Friend Moon of **NML** (its `LightTextureMixin`, active if delightmap is absent; true darkness only plays during dreams) [jar NML].
- In 1.21.1, Polytone's lightmaps are **per dimension**: the `biomeLightmaps` field exists but is never read [jar]. A per-biome "clearing" lightmap therefore does not exist. The sky script (`sky_getter`) can read `p.biome()` [wiki Scripting-Expressions]: a dimension lightmap that changes with the player's biome remains possible, but it still cancels the three mods above.
- Polytone's `season()` function only knows Serene Seasons and Fabric Seasons, **not Ecliptic** [jar].
- A Polytone **block colormap** replaces the `BlockColors` lambdas, in which Ecliptic injects its season colours. Grass and leaves would lose their seasons. A `biome_modifier` (biome grass colour) remains compatible [jar Ecliptic, inferred].
- Polytone 5.x adds coloured lights (`OFF`, `BUILT_IN` by default, `HYBRID`, `VEIL`), voxels, shadows and compute shaders. In `BUILT_IN` or `HYBRID`, it **patches Sodium's chunk shader** [jar]. This is the kind of change that breaks with Sable, which embeds Veil and renders its sub-levels separately [inferred]. Setting: `<game folder>/polytone_options.json`, key `coloredLightsBackend` [jar].
- Pace: 8 versions in 10 days, with a brand-new light engine. 4.5.3 already has coloured lights, without the Sodium shader patch [MR] [jar 4.5.3].

### 3.3 Polytone versus ATMOSPHERICS: ATMOSPHERICS wins

- Polytone changes the fog colour through a `WrapOperation` on `gaussianSampleVec3` in `setupColor`, and the sky colour through a `WrapOperation` on `getSkyColor` [jar].
- ATMOSPHERICS rewrites these values later (`TAIL` and `RETURN`) [jar]. What Polytone writes is overwritten [inferred].
- So in the `compline-ambiance` pack, **no `fog_color`, `sky_color` or `fog_radius` on the Polytone side**. Polytone keeps only grass, leaves, water and particles.

### 3.4 Four mods on the camera

Targets found in the jars:

| Target | Shoulder Surfing | Camera Overhaul | Spatial GUI | Real Camera | Sable |
|---|---|---|---|---|---|
| `Camera.setup` | Inject `HEAD` and after `setPosition`, Redirect `move(FFF)` | Inject at the `getMaxZoom` call (3rd person) and at `RETURN` (1st person) | Inject `TAIL` | Inject `RETURN` | Redirect `setRotation`, WrapOperation `setPosition`, Inject after `rotationYXZ` |
| `Camera.setRotation` | ModifyArg of `rotationYXZ` | | | | |
| `Camera.setPosition` | | | | | WrapMethod |
| `GameRenderer.pick` | **Redirect** `getEntityHitResult` | | | **WrapOperation** on the same call | |
| `GameRenderer.bobView` / `bobHurt` | Redirect `bobView` | Inject `bobHurt` `HEAD` | | | |
| `GameRenderer.render` / `getFov` | | | Redirect `ClientHooks.drawScreen` and `PostChain.process`, ModifyReturnValue `getFov` | | |

[jar of each mod]

- **Sable has five compat mixins written for Shoulder Surfing** (including `getMaxZoom`) [jar Sable]. SSR is the only camera mod Sable knows. This is a good sign for third person aboard.
- **Risk no. 1: SSR and Real Camera on `GameRenderer.pick`.** A Redirect and a WrapOperation on the same call: depending on the order of application, one fails at load or one loses its effect (aiming) [inferred]. Test T7.
- **Risk no. 2: Camera Overhaul aboard.** Its `vehicles` and `mounts` contexts tilt the camera according to speed. Sable already rotates the camera with the sub-level. The two add up [inferred]. Set to 0 on vehicles (L14).
- Spatial GUI only acts with a menu open (at `TAIL` of `setup`): it comes after the others and does not interfere with them in normal play [inferred].
- Real Camera is in beta, incompatible with GeckoLib armours (many armour mods in 1.21.1 use them), and duplicates SSR on aiming [MR] [inferred].

### 3.5 Animal textures: three mods on the same point

- NML (`RenderEntityVariantMixin`), Quark (`Variant Animal Textures`: mixins on `CowRenderer`, `PigRenderer`, `ChickenRenderer`, rabbit, bee, dolphin, frog, llama, slime, allay, armadillo, all on `getTextureLocation`) and ETF change a mob's texture [jar NML] [jar Quark] [inferred for ETF].
- NML and Quark textures are drawn for the vanilla model. FA ships its own textures (cat, fox, goat, turtle, wolf…) with other UVs. An NML or Quark variant placed on an FA model risks **misplaced texture pieces** [inferred]. Test T3.
- Quark is a pure duplicate of NML and is cut (L7).

### 3.6 Sounds: Dynamic Surroundings versus Sound Physics

- Dynamic Surroundings 0.4.6 has its own "enhanced sounds" (reverberation, occlusion). It and Sound Physics Remastered both inject into `Library.init` (OpenAL), `Channel` and `SoundEngine` [jar DS] [jar SPR]. Even with the feature turned off in the config, the mixins remain loaded [inferred]. Strong risk of broken sounds or a crash at launch.
- Its "footsteps" **do not replace Presence Footsteps**: they are accents (armour noise, wet ground, creaks), laid over vanilla footsteps [jar DS, `dsconfigs`]. The plan overestimates it.
- Its biome ambiences (`assets/dsurround/dsconfigs/biomes.json`, replaceable by resource pack) duplicate AmbientSounds [jar].
- Its other features also duplicate:
  - waterfalls: sounds, particles, mist and foam (Particular and Subtle Effects already do the particles);
  - fireflies;
  - hotbar sounds (Sounds does them);
  - fog (`enableFogEffects…`, versus ATMOSPHERICS).
- What it brings that is really new: **the aurora and the sound of waterfalls** (AmbientSounds has no waterfall [jar AmbientSounds]).

### 3.7 Particles: who does what

| Effect | Particular | Subtle Effects | Dyn. Surroundings | NML | ATMOSPHERICS | Proposed owner |
|---|---|---|---|---|---|---|
| Fireflies | yes | yes | yes | — | — | **Particular** (as in the plan) |
| Waterfalls | `cascades`, `waterfallSpray` | `waterfall_cloud`, `mist`, `droplet` | yes | — | — | **Particular**; cut in Subtle Effects |
| Cave dust | `caveDust` | — | — | `cave_dust` | `cave_fog` | **ATMOSPHERICS** for mist, **Particular** for dust; cut NML |
| Water ripples | yes | yes | yes | — | — | **Particular** |
| Falling leaves | yes (no NML wood) | — | — | — | — | Particular + NML entries (L9) |

[jar of each mod, option names]

### 3.8 ATMOSPHERICS erases the sky of the others

Since `renderSky` is cancelled at `HEAD` (1.5), **everything that injects into the vanilla `renderSky` disappears**:
- the colour, size and texture of the **Enhanced Celestials** moon (white moon, blood moon…) [jar EC: injects in `renderSky`];
- the double suns ("sun dogs") and the upper atmosphere of **NML** [jar NML].

Only NML's Friend Moon is handled by ATMOSPHERICS itself [jar]. EC events still play (spawns, effects), but their moon is no longer visible [inferred]. Test T1.

### 3.9 Minor conflicts

- **More Culling**: its `cloudCulling` cancels `buildClouds` at `HEAD`, which bypasses ATMOSPHERICS' `ModifyArg` on clouds [jar]. It also has a `leavesCullingMode` that is debatable with Better Leaves. It had already been ruled out in decision 0007 [repository `docs/decisions/0007`].
- **Seamless Loading Screen versus Sodium**: Sodium overwrites (`Overwrite`) `LevelLoadingScreen.renderChunks`. Seamless's background colour tweak, placed in a lambda of this method, is lost [jar]. Only cosmetic, the screen keeps the world capture [inferred]. Single 2024 version, no longer maintained [MR].
- **MB-3D Items and Amendments**: MB-3D redraws lanterns, torches, spyglass, compasses, maps and buckets in 3D [jar]. Amendments already draws held lanterns. Depending on pack order, one replaces the other [inferred]. Put MB-3D under Amendments, or remove its lanterns.
- **Spatial GUI and Polytone**: Spatial GUI has a mixin on Polytone's `LightmapsManager.setupForGUI` [jar]. This is planned, but only for the version the author targets [inferred].
- **Real Camera and 3D Skin Layers / Player Extension**: the first-person body goes through the player renderer. 3D Skin Layers has an EMF mixin [jar], Real Camera has none [jar]. Test T6.

---

## 4. What to adapt, remove or add

### 4.1 Remove

| What | Reason |
|---|---|
| **Entity Culling** | Open bugs with Aeronautics and Sable, unanswered (3.1). Small gain under the mist |
| **Quark "Variant Animal Textures"** | Duplicates NML, and breaks FA's UVs (3.5, L7) |
| **NML fog** (`fogModifiers`) | Two sources of mist, contrary to §11.1 (L6) |
| **Dynamic Surroundings** | Reverberation conflicting with SPR, footsteps that do not replace Presence Footsteps, almost everything duplicated (3.6). Keep **Auroras** if the aurora matters (already a fallback in §11.2). For the sound of waterfalls, an AmbientSounds entry tied to the presence of flowing water around the player [inferred, to be checked in AmbientSounds' engine], or nothing |
| **More Culling** | Already ruled out in 0007, and breaks ATMOSPHERICS clouds (3.9). If taken back: `cloudCulling = false` |
| **Real Camera** (on trial) | First to remove if it causes trouble: beta, duplicates SSR on aiming, GeckoLib-incompatible (3.4) |

### 4.2 Adapt

| Plan point | Change | Reason |
|---|---|---|
| §11.1 and §11.2 "Polytone: lightmaps" | Polytone **without lightmap**, only `biome_modifiers` (grass, leaves, water) and particle colours. `coloredLightsBackend = OFF`, or Polytone **4.5.3 frozen** while 5.x stabilises | The lightmap switches off More Darkness, EC's moon and NML's night. No per-biome lightmap in 1.21.1. Sodium shader patch in 5.x (3.2) |
| §11.2 "the clearing: warmer lightmap" | Make the clearing's light **through ATMOSPHERICS** (lifted mist, sky and fog colour) and through the **biome JSON colours** | It is the only owner of the sky that does not get overwritten (3.3) |
| §11.2 "the white moon: silvery night" | Drop the Polytone route; go through the custom mod (4.3) | EC's moon disappears under ATMOSPHERICS (3.8) |
| §11.2 Fresh Animations "If NML wins…" | NML wins on **8 species**: pig, cow, mooshroom, chicken, sheep, rabbit, squid, glow squid. The choice is made species by species in `mixed_litter-startup.toml` (L4) | 1.2 |
| §11.2 Dynamic Surroundings "footsteps" | Remove. Footsteps stay vanilla (plus Sounds' "touch") | 3.6 |
| §11.2 Particular and Subtle Effects | Also cut waterfalls and water ripples in Subtle Effects, and `cave_dust` in NML | 3.7 |
| §11.7 Camera Overhaul "set low" | Add `enableInThirdPerson = false` and 0 on `vehicles` / `mounts` | 3.4, L14 |
| §11.7 priority order | See 4.4: **Spatial GUI, Shoulder Surfing, Camera Overhaul, Real Camera** (SSR comes before Camera Overhaul) | Sable is written for SSR; Camera Overhaul must leave it 3rd person |
| §11.7 EMI "Groups" | Replace with "order by theme" (`creative` index + KubeJS tabs, or `added`/`after`) and search aliases | EMI 1.1.24 has no groups; KubeJS does not handle `groupEntries` for EMI (1.7) |
| §11.7 EMI "not per player" | Correct: AStages does it in EMI | 1.7 |
| §11.7 "JEI stays as a bridge" | Confirmed, and **mandatory**: Create, Aeronautics, Cold Sweat, Ecliptic, Quark, Exposure… have only a JEI plugin | 1.6 |
| Current pack | Upgrade NML to 1.6.1, Sable to 2.0.6, ATMOSPHERICS to 2.6.5.2; Farm & Charm to 1.1.29 (EMI plugin) | [MR] |
| ATMOSPHERICS preset | Name all NML biomes, version the file name | 1.3 |
| MB-3D Items | Place under Amendments in the pack order | 3.9 |

### 4.3 Add: a small custom mod "compline-ciel" (client, NeoForge 1.21.1)

KubeJS can do nothing about sky or moon rendering, nor about ATMOSPHERICS' cache. A client mod of about a hundred lines, with two mixins, would do three things:
1. **Render the Enhanced Celestials moon under ATMOSPHERICS.** After ATMOSPHERICS' sky, read the active lunar event in EC's client API and redraw the moon with its colour, size and texture.
2. **Flush the `BiomeBlendUtil` cache** on receipt of a `ClientboundChunksBiomesPacket`, so that the mist reacts to a `/fillbiome` even if the player does not move.
3. Later, if needed: **lightly tint the night during the white moon**, by multiplying the lightmap after More Darkness, instead of a Polytone lightmap that would cancel it.

The first two points are necessary if the white moon and the clearing that "opens up" stay in the plan. The mod depends on the internal classes of ATMOSPHERICS and EC: it will have to be reviewed at each update of those two mods.

### 4.4 Priority order of the camera mods

1. **Spatial GUI**: only acts with a menu open, at the end of `Camera.setup`. It wins when a menu is open.
2. **Shoulder Surfing Reloaded**: sole master of third person. It is the only one Sable knows (5 compat mixins).
3. **Camera Overhaul**: first person only, on foot (`enableInThirdPerson = false`, vehicles at 0).
4. **Real Camera**: last, and the first removed in case of conflict. Without it, vanilla arms are kept in first-person view.

### 4.5 What is missing for the "Realism Craft" finish

- **Footsteps by ground type**: nothing on NeoForge 1.21.1 replaces Presence Footsteps (3.6 for Dynamic Surroundings, §11.2 of the plan for Presence Footsteps). The footstep sounds of NML and Twigs blocks stay those of their material. A custom mod lead if the maintainer wants it: play a footstep sound per block tag. Not a priority.
- **A single `compline-ambiance` pack** carrying together the ATMOSPHERICS presets, the Polytone `biome_modifiers`, the Particular leaf entries if it reads them as resources, and the client-side EMI overrides. Enabled by Default Options (L13).
- **The moon seen in the sky**, currently lost (3.8, 4.3).

---

## 5. Questions to test in game (branch `test/finition`)

| # | Test | What we look at |
|---|---|---|
| T1 | ATMOSPHERICS + Enhanced Celestials, night of white moon and blood moon | Does the moon change colour or size? (expected: no, 3.8) |
| T2 | `/fillbiome` in a clearing, player motionless then walking 4 blocks | Delay before the mist lifts; smoothness of the transition (1.4) |
| T3 | FA + NML: cat, fox, goat, turtle, dolphin, donkey with several NML variants | Textures correctly placed on the FA model? (3.5, L5) |
| T4 | `mixed_litter-startup.toml` with `chickenRemodel = false`, then `rabbitRemodel = false` | Does FA take over properly? Which looks better? (L4) |
| T5 | Polytone 5.1.3 with default `coloredLightsBackend`, aboard an Aeronautics airship | Is the Sable sub-level displayed? Chunk lighting correct? Otherwise OFF or 4.5.3 (3.2) |
| T6 | Real Camera + Player Extension + 3D Skin Layers, looking down | Do the animated body and skin layers appear? |
| T7 | SSR + Real Camera: aim at an entity and a block in third person | Does loading succeed? Does aiming follow the crosshair? (3.4) |
| T8 | Camera Overhaul aboard an airship and a Create train, with `vehicles` at 0 then default | Does the camera tilt twice? |
| T9 | Spatial GUI on a chest, backpack (Traveler's Backpack), crafting table, then Wildex and Patchouli | Does the menu come through correctly, do books stay flat? |
| T10 | EMI + JEI (bridge): Create, Aeronautics, Cold Sweat, Ecliptic, Overgeared, Kaleidoscope recipes | All visible in EMI? No JEI interface on screen? (1.6) |
| T11 | AStages in EMI: an item tied to a locked stage | Hidden for that player only? (L12) |
| T12 | Better Leaves in a large NML forest, at the server's normal render distance | Frames per second (§10.6) |
| T13 | Particular in an NML willow and maple forest | Generic or dedicated leaves? (L9) |
| T14 | More Darkness + Polytone **without** lightmap | Does the night stay truly black? (3.2) |
| T15 | If Dynamic Surroundings is tried anyway: with SPR, at launch then in a cave | Crash, muted sound or double echo? (3.6) |
