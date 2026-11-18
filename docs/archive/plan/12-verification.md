# 12. Full verification of 2026-10-18

> Requested by the maintainer: review everything to adapt, modify, remove or add, looking in depth at **how to link the mods**, especially Overgeared and Kaleidoscope. If scripts are not enough, custom mods can be written.
>
> This chapter **corrects chapters 10 and 11** where they contradict each other. The evidence (opened jars, Modrinth versions, tickets) is in the six reports in `recherche/verif-2026-10-18/`:
> [1 Overgeared](recherche/verif-2026-10-18/1-overgeared.md) · [2 Kaleidoscope, seasons, heat, thirst](recherche/verif-2026-10-18/2-kaleidoscope.md) · [3 rendering, camera, interface](recherche/verif-2026-10-18/3-client.md) · [4 world, fauna, goals](recherche/verif-2026-10-18/4-monde-et-objectifs.md) · [5 Create, building, scripts](recherche/verif-2026-10-18/5-create-construction.md) · [6 monument guardians](recherche/verif-2026-10-18/6-gardiens.md)
>
> Nothing has been tested in game: each report ends with its list of tests.

## 12.1 What the verification changes

### What goes out

| Mod or feature | Why | Evidence |
|---|---|---|
| **Freeze It and Heat It** | Removed by the maintainer on 2026-10-18: no food preservation | — |
| **Easy Anvils** | It replaces the vanilla anvil with its own, but Overgeared only turns `minecraft:anvil` into a smith's anvil: forging would no longer be possible. The Overgeared anvil already shows its items in 3D | Report 1 |
| **Leaves Be Gone** | On the Aeronautics blacklist, and FallingTree already breaks leaves | Report 5 |
| **Dynamic Surroundings** | It touches the sound engine like Sound Physics (high risk), its footsteps are no match for Presence Footsteps, the rest is redundant. Auroras stays as a fallback for the aurora | Report 3 |
| **Entity Culling** | Three open tickets without a reply at Aeronautics and Sable. Report 5 notes a fix for block entities since 1.11.0: one last test with 1.11.3 on an assembled airship, then removal if it breaks | Reports 3 and 5 |
| **The Polytone lightmap** | It cuts More Darkness, the moon tint of Enhanced Celestials and the NML night, and does not exist per biome in 1.21.1. Polytone only keeps grass, leaves and water | Report 3 |
| **Quark "Variant Animal Textures"** | Duplicate of the NML variants, and shifts the Fresh Animations textures | Report 3 |
| **Quark "Thatch" and "Variant Bookshelves"** | NML has the same blocks with the same recipe | Report 5 |
| **NML's own fog** | ATMOSPHERICS is the only thing responsible for it (`nomansland-client.toml`, `[fog_modifiers]`) | Reports 3 and 4 |
| **The Hybrid Aquatic biomes** | 9 biomes, including a tropical river that replaces the vanilla river, against decision 0003: cut through its config | Report 4 |
| **The Wildex Analyzer** | A shortcut that reveals creatures without having seen them | Report 4 |
| **`doLimitedCrafting`** | Three leaks: Create's 1,071 advancements return the recipes, the backpack grid ignores it, Create machines have no player | Report 5 |
| Furnace, smoker and backpack feeding upgrades | Bypass cooking and the forge | Report 2 |

### What is added

| Addition | Role |
|---|---|
| **JEI, hidden** | Bridge for EMI: Create, Aeronautics, Cold Sweat, Ecliptic, Quark, Exposure, Alex's Mobs and Farm & Charm only have a JEI plugin (report 3). Overgeared and Kaleidoscope already have an EMI plugin |
| **AA4 Atlas** | Confirmed: the atlas becomes an item (report 4) |
| Libraries Puzzles Lib, Balm, Configurable, MRU, Cloth Config | Required by the additions of chapter 11 (reports 3 and 5) |
| **A custom `fallow` mod** | What scripts cannot do (12.4) |
| French translation of Overgeared (`fr_fr.json`) | The mod has none (report 1) |

### What the plan got wrong

| Where | Wrong | Right |
|---|---|---|
| 10.3, 10.10 | A "forge" and alloys (bronze, brass) in Overgeared | No forge block: ingots are heated in the vanilla blast furnace, hammered on the anvil (mini-game), quenched, polished. The only alloy is steel. The Overgeared blueprint found in ruins is used to move on to diamond |
| 06 | Overgeared has a JEI plugin | It has an EMI plugin, and Sable/Aeronautics compatibility |
| 10.5 | Kaleidoscope Compat merges duplicates | It makes them interchangeable; its "UNITE" mode breaks Farm & Charm's bacon, pasta and onions |
| 10.7 | Freeze It and Heat It has season tags | It is configured through lists of identifiers in its config, with no KubeJS API; the season goes through Cold Sweat |
| `saisons_cold_sweat.js` | — | Wrong API: the right pattern is `addTempModifier` + `gatherDefaultModifiers('world')` (code in report 2) |
| 10.4 | The Wildex "Spyglass Pulse" reward is cut | There is no switch: accept it, or a small mixin in `fallow` |
| 11.1 | Polytone makes a warmer light in the clearings | No per-biome lightmap in 1.21.1 |
| 11.7 | EMI groups items, and does not hide recipes player by player | EMI 1.1.24 has no groups (report 3; report 5 thinks KubeJS can do it: to be tested). AStages does hide per player, but its licence is closed and it touches chunk rendering |
| 11.8 | A wandering trader leaves the Monoliths codex fragments | It does not exist. Only source: 0.2% per mob killed |
| 11.8 | The monuments have guardians | They are breeze spawners, which Fallow bans. Report 6 proposes guardians drawn from the mods already present |
| 11.8 | The monument biomes are set by datapack | Through the config `config/obscuria/ancient_remnants-common.toml`; a datapack can add, not remove. Sparse Structures does not space them |
| 11.7 | Camera order: Spatial GUI, Camera Overhaul, Shoulder Surfing, Real Camera | Spatial GUI, **Shoulder Surfing** (Sable has 5 mixins written for it), Camera Overhaul in first person only and at 0 in vehicles, Real Camera last (it touches aiming like Shoulder Surfing) |
| 10.6, 11.2 | NML remodels pigs, cows and squids | 8 species (pig, cow, mooshroom, chicken, sheep, rabbit, squid, glow squid), through Mixed Litter which it bundles; which ones to leave to Fresh Animations is chosen species by species in `mixed_litter-startup.toml` |

## 12.2 The links, system by system

Each line is a config, a tag, a datapack or a script; the cited report gives the detail.

### The forge (Overgeared), report 1

- **Hammering stays manual**: quality (`poor` to `master`) comes from the mini-game. Create brings heat and quantity: the press makes the plates, the mixer makes steel. Heating ingots with a fan over lava is forbidden (suffix `_manual_only`).
- **Free lock**: iron requires a copper pickaxe, Create's zinc an iron pickaxe. Brass therefore comes after the forge without any script.
- **Critical parts**: a shaft or a propeller blade can require a minimum quality (NeoForge ingredient `neoforge:components`).
- **Blueprints**: Overgeared blueprints of a hidden type (`lanterne`, `rail`, `aerostat`…) become the condition for forging the parts that lock the machines. The Clearing Lantern becomes one (recipe in report 1).
- **Nether closed**: the Nether furnace, the casting furnace and netherite are hidden; the diamond template enters the loot of NML ruins.
- **Heat**: the blast furnace already heats for Cold Sweat; the alloy furnace is added. The Supplementaries bellows speeds up the blast furnace.
- **Metal equipment from other mods** (Kaleidoscope knives included) goes through the forge; creature trophies stay at the workbench.

### Cooking, seasons and the body (Kaleidoscope and the others), report 2

- **Heat**: a campfire or a Farm & Charm stove heats the Kaleidoscope wok, pot, cooker and teapot. Cold Sweat does not count these stoves: they are added with a weak effect. The clearings' Vigil Fire receives the Kaleidoscope heat tag and a Cold Sweat entry: you can cook and warm up there.
- **Seasons**: Farm & Charm's season files are in the wrong place, its crops grow all year round. The `eclipticseasons:crops/*` tags are applied. Kaleidoscope's are already seasonal.
- **A single season bridge to Cold Sweat** gives the season to players and to the thirst of Thirst Was Taken, which reads body temperature.
- **Duplicates**: we keep Kaleidoscope's tomatoes, lettuce, flour and dough, and Farm & Charm's oven; the Farm & Charm rotisserie and the Kaleidoscope scarecrow go out; so does the Farm & Charm pot, its recipes moving into Kaleidoscope's. Farm & Charm stays on 1.1.25 (1.1.27 adds a duplicate cutting board and choppers).
- **Animals**: their food is set by tags, their breeding by season with `eclipticseasons:breed/*`.
- **The backpack**: its hose does not quench thirst with Thirst Was Taken; only Java fixes it (12.4). With Freeze It and Heat It removed, food does not spoil anywhere, which also settles the "refrigerator backpack".

### The world and the goals, report 4

- **Clearings**: `/fillbiome` aligned to 4 blocks and across the whole height (8×336×8). Three biome variants (cold, temperate, warm) so that Ecliptic keeps the seasons, an explicit list of convertible NML biomes, spawns set by the `naturalist:has_*` tags and an In Control! rule against hostiles. The mist only updates when the player changes 4-block cell (report 3).
- **Monoliths**:
  - the 6 monuments are tied to NML biomes, with a spacing of 64 to 80 chunks;
  - the Elderheart is an entity with no event. A script that watches it starts the clearing around the awakened monument;
  - blessings are permanent, one at a time, kept on death. The warrior's is almost null in Fallow and can be replaced by another through its tag;
  - the codex fragments enter the loot of NML ruins (chests and archaeology), and a photo of a monument with Exposure gives one. Fallow pages can be added to the codex by datapack.
- **Zoo**:
  - **transport**: lead, boat for small animals, and above all the **Create seat**, on a train or an airship, which accepts any animal;
  - **settings**: the Icy's cart moves to 2.1.4. Hybrid Aquatic animals have no bucket, and the lion attacks horses;
  - **enclosure plaque**: Wildex does not recognise enclosures. A KubeJS plaque counts the species present and hooks into Wildex's `onDiscoveryChanged` callback for the tiers;
  - **completion**: without an exclusion list (Nether, End, banned monsters, cut duplicates), the bestiary cannot be completed.
- **Fauna duplicates**: about 14 species exist twice. Report 4 says which to keep, by config or biome modifier, without removing any mod (maintainer's decision).
- **Other settings**: NML's Friend Moon competes with Enhanced Celestials, the vanilla spawners of NML dungeons will run empty, and NML jars refill. `spreadFactor 3` risks making NML ruins very rare.

### The monument guardians, report 6

With Freeze It and Heat It gone, the Ancient Remnants breeze spawners had to be replaced. **No mod to add**: Mowzie's Mobs and Companions! already have sleeping guardians that fit the tone.

| Monument | Guardian | Why |
|---|---|---|
| Warrior monolith | **Ferrous Wroughtnaut** (Mowzie's) | A suit of hero's armour that stays still until struck, and returns to its place |
| Conifer monument | **Frostmaw** (Mowzie's) | A sleeping snow giant, summoned with its crystal and without despawning |
| Hunter monolith | **Tongbi, the Sculptor** (Mowzie's) | A timed agility trial rather than a fight, like the "agility and precision" blessing. It needs an open sky |
| Oak monument | **Sacred Pontiff** (Companions!) | A moss priest awakened by hand, two-phase boss, who petrifies as he dies |
| Cherry monument, sentinel monolith | None | The monument of life and the peaceful monks, as the mod's text says |

- **The blessing waits for victory**: the Elderheart gives nothing without its `Blessing` data. The script removes it on creation, spawns the guardian once when a player approaches, then gives it back on its death (or when the Sculptor leaves after the trial). It then opens the clearing and gives the codex page. It is the same script as the clearing one.
- **The spawners go out by datapack**: the mod's two processor lists (`monolith`, `monument`) replace the spawner with chiseled tuff.
- **The guardians stay unique**: the natural spawns of the Wroughtnaut, the Frostmaw and the Sculptor are cut by datapack, otherwise you would come across a Wroughtnaut room about every 15 chunks. Applied on 2026-11-01 by the [pack sort](../../plan/tri-du-pack.md): empty structure sets for the Wroughtnaut chambers and the Frostmaw, the Umvuthana groves cut, the monastery kept, and the boss weapons and armour removed from loot (the guardians give their blessing and nothing else).
- The Elderheart rooms are narrow: each boss is placed on the ground, outside.
- **If the maintainer prefers a real fight for the Sculptor**: Bosses of Mass Destruction (1.95 MB), of which only the Obsidilith is kept, a living obsidian monolith; its structures are cut by datapack. Caveat: it places obsidian and its purple atmosphere recalls the End. Legendary Monsters would suit the theme but weighs 44.75 MB for 37 creatures: rejected.

### Create and building, report 5

- **Engineer's blueprints, new method**: the blueprint is a **kept ingredient** of the recipe (`keepIngredient`). It stays in the grid, is visible placed on the workbench thanks to Visual Workbench, works in Create machines, can be lent and can be lost. Overgeared blueprints cover forged parts. Knowledge is no longer personal but physical, which suits a permanent world among friends.
- **Wood**: the NML saw recipes do not load (wrong folder). A `bois.js` writes 16 of them. FallingTree already recognises Overgeared axes; its maximum size (100) goes up for NML's large trees.
- **Building duplicates**: rule "NML style first". Table 2.2 of report 5 decides block by block (bricks, thatch, bookshelves, wall lamps, pebbles…). Twigs' rhyolite, generated across the whole Overworld, is cut.
- **Every Compat**: blacklist for the disabled Quark woods (Ancient, Azalea, Blossom), which still generate about 150 blocks, and for its Twigs module. The building mods add about 1,850 blocks, to be measured with spark.
- **Airships**:
  - Lootr chests are marked immovable;
  - furniture and the backpack receive a Sable mass through tags;
  - the backpack's coordinates message on death is cut;
  - the GraveStone grave of a death in flight probably floats in the sky: to be tested.
- **The Dissipator**: KubeJS Create does not create a kinetic block. A KubeJS prototype, a head placed in front of a Create fan that reads its speed (the fan is what consumes the stress), makes it possible to judge the idea. The real block goes into `fallow`.
- **Scripts to fix**: the MoreJS group is called `MoreJS`; LootJS 3 no longer has `.when()`; `NativeEvents.onEvent` expects a class; the 15 default deposits of Create Ore Excavation must be removed.

### Rendering, report 3

- **ATMOSPHERICS erases the sky of other mods**: the Enhanced Celestials moon (hence the white moon) and the NML double suns are no longer visible. This is the biggest gap found on the client side.
- **Its presets**: the NML biomes must be named there one by one, and the file renamed at each version (`fallow_v1`, `fallow_v2`…) to be reapplied.
- **Polytone**: it only keeps grass, leaves and water. Its 5.x modifies Sodium's chunk rendering: pin it after testing, or take 4.5.3.
- **Small settings**:
  - MB-3D goes below Amendments, for its lanterns;
  - Particular has no entry for NML leaves, which must be added;
  - Sodium erases the background of Seamless Loading Screen (cosmetic; the mod is no longer maintained).

## 12.3 Versions to upgrade before pinning the world

NML 1.6.1, Sable 2.0.6, Quark 4.1-487, Moonlight 3.7.2, Kaleidoscope Cookery 1.6.0, Ecliptic 0.15.4.1, Icy's Better Horses 2.1.4, Friends & Foes. Cold Sweat stays on 2.4.3.1 and Farm & Charm on 1.1.25 until tested. Supplementaries 4.0.2 is a new major version: reread its config before upgrading. KubeJS Create and Steam 'n' Rails are in beta.

## 12.4 The custom `fallow` mod

Scripts and data cover almost everything. What remains requires Java. Proposed: **a single `fallow` mod** (NeoForge 1.21.1), split into modules enabled one by one, rather than several small mods.

| Module | What it does | Why not in a script | Size | Priority |
|---|---|---|---|---|
| **ciel** (client) | Redraws the Enhanced Celestials moon, hence the white moon, over the ATMOSPHERICS sky; empties the mist cache after a clearing conversion | ATMOSPHERICS replaces the whole sky rendering | 4 to 6 classes, mixins | **High**: otherwise the white moon is invisible |
| **dissipateur** | Real Create kinetic block: consumes stress, shows on engineer's goggles, has its Ponder, converts the radius of 64 | KubeJS Create does not create a kinetic block | 8 to 12 classes | Medium: after the KubeJS prototype |
| **sac** | The Traveler's Backpack hose quenches thirst (Thirst Was Taken) | The two mods do not talk to each other, no event exposed | 2 to 3 classes | Medium |
| **tombe** | The grave drops to the ground when you die in flight | GraveStone places the grave at the point of death | 2 classes | Only if the test fails |
| **voies** | Steam 'n' Rails tracks in NML woods | Steam 'n' Rails only generates for the woods it knows | 3 to 5 classes | Low |
| **plans** | Blueprint knowledge specific to each player, without leaks | KubeJS 2101 no longer locks a recipe per player | 7 to 10 classes | Only if the maintainer wants personal rather than physical knowledge |
| **finitions** | Real sparks on Overgeared hammer strikes; cut the Wildex Spyglass Pulse | No event on the KubeJS side | 2 to 4 classes | Low |

The mod is built in the repository, in a separate folder (`mods-custom/fallow/`), with GitHub CI to compile it. It never touches the code of other mods, only through targeted mixins and their APIs.

> **Follow-up.** The `ciel` and `sac` modules exist in version 0.1.0, untested in game: see [mod-fallow.md](../../plan/mod-fallow.md). Since the pack's mod is Thirst Was Reclaimed, that is the one `sac` targets.

## 12.5 Decisions that are added (continuing 10.12)

| # | Subject | Default retained | Alternative |
|---|---|---|---|
| 14 | Engineer's blueprints | Physical blueprint, kept in the recipe, plus Overgeared blueprints for forged parts | Personal knowledge per player (`plans` module) |
| 15 | Monument guardians | Wroughtnaut, Frostmaw, Sculptor and Sacred Pontiff, drawn from the present mods; the blessing waits for their defeat (report 6) | Obsidilith from Bosses of Mass Destruction in place of the Sculptor, or empty monuments |
| 16 | Visible white moon | `ciel` module of the `fallow` mod | Give up the white moon |
| 17 | Custom `fallow` mod | Yes, `ciel`, `dissipateur` and `sac` modules first | Scripts only, accepting the listed gaps |
| 18 | Drinking from the backpack hose | `sac` module | The backpack tank only serves to carry water |

## 12.6 Proposed order of work

1. Upgrade the versions (12.3) on a test branch and launch the pack as is.
2. Run the tests that change what can be built: the Wildex callback, the Exposure server trigger, a large animal seated during a flight, the moon with ATMOSPHERICS, Entity Culling on an airship.
3. Write the corrected configs and scripts (reports 1, 2, 4 and 5), one system at a time.
4. Start `fallow` with the `ciel` module.
