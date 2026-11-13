# 11. Rendering and comfort, without shaders

> **Check of 2026-10-18: [chapter 12](12-verification.md) corrects this chapter** (Easy Anvils, Leaves Be Gone, Dynamic Surroundings and Entity Culling are dropped, Polytone without lightmap, camera order, Overgeared, Kaleidoscope Compat, Monoliths).

> Added on 2026-10-18, at the maintainer's request: improve rendering without a texture pack or shaders, around ATMOSPHERICS, with effects and particles, and comfort mods. Complements [10.6](10-recentrage.md#106-the-finish-in-the-manner-of-realism-craft).
>
> All versions cited were checked on Modrinth on 2026-10-18 for **NeoForge 1.21.1** (resource packs for 1.21.1).

## 11.1 The principle: one mod per role

Without shaders, rendering is built in layers. Each layer has **a single** responsible mod, otherwise two mods fight over the fog or the clouds and the result flickers.

| Layer | Responsible | What it handles |
|---|---|---|
| Sky, mist, clouds, stars | **ATMOSPHERICS** (already retained) | Fog per biome, day and night, "Story Mode" clouds, comets, stars |
| Light and colors | **Polytone** (new) | Lightmaps (the color of the night, of torches), grass, leaf and water color per biome, particle color |
| Hand-held light | LambDynamicLights (already retained) | The lantern lights up around the player |
| Air and water | **Particular Reforged** (new) | Splashes, waterfalls, falling leaves, cave dust, ripples in the water |
| Fire, smoke, steam, bodies | Subtle Effects (already retained) | Sparks, chimney smoke, steam, dust under footsteps, heartbeat |
| Creatures | **Fresh Animations** (new, resource pack) | Vanilla creatures blink, turn their heads, breathe |
| World sounds | AmbientSounds + Sound Physics Remastered (already retained) | Ambience per biome, echo and muffling |
| Gesture sounds | **Sounds** (new) | 170 interface, item and block sounds |

## 11.2 The comparisons

### Mist and clouds: ATMOSPHERICS alone

| Candidate | Verdict | Why |
|---|---|---|
| **ATMOSPHERICS** | Kept | It carries all the mist mechanics (10.3) and its presets ship in a resource pack |
| Fog (IMB11) | Rejected | Fog per biome and per weather: exactly ATMOSPHERICS' role, the two would contradict each other |
| Better Clouds | Rejected | Beautiful volumetric clouds, but ATMOSPHERICS already has its "Story Mode" clouds, and under the mist they are barely seen |
| Particle Rain | Still rejected | It redraws the rain and adds its own fog during downpours; it would fight with ATMOSPHERICS and with Ecliptic's snow |
| Dynamic Surroundings (fog) | Feature disabled | See below: its footsteps and waterfalls are kept, not its fog |

### Light: Polytone rather than a new engine

| Candidate | Verdict | Why |
|---|---|---|
| **Polytone** | Retained | Client-only, no dependency, by the same author as Moonlight, Supplementaries and Amendments (already in the pack). Everything goes through a Tilth resource pack |
| Vibrancy | Rejected | Colored light and shadows, but development is paused and it replaces the lighting engine: risk with Sodium 0.8 and Sable |
| Gleam, Colorful Lighting | Rejected | Same risk, very little used (fewer than 8,000 downloads) |

What Polytone does for Tilth, in the `tilth-ambiance` resource pack (the same one that carries the ATMOSPHERICS presets):
- **The mist biomes**: grass, leaves and water slightly desaturated, a slightly blue night.
- **The clearing** (`tilth:clairiere`): a warmer lightmap and vivid colors. When the mist withdraws, the light changes too: this is the most important moment of the pack.
- **The white moon**: a brighter, silvery night (to prototype: we need to see whether a lightmap can follow an Enhanced Celestials event; otherwise the moon keeps its own colors).

### Particles: Particular Reforged

| Candidate | Verdict | Why |
|---|---|---|
| **Particular Reforged** | Retained | A single mod for water (3D splashes, waterfalls, rain ripples, popping bubbles), falling leaves, cave dust and fireflies. It declares **compatibility with Create Aeronautics and Sable** on Modrinth. 5.3 million downloads |
| Subtle Effects | Kept | It covers different ground (fire, smoke, steam, bodies). Its fireflies are disabled so there is only one source |
| Falling Leaves Plus | Fallback | Leaves per tree species (acorns, cones, flakes) and wind depending on the weather, by Fuzs (Puzzles Lib already there). To be taken if Particular does not make the leaves of No Man's Land trees fall |
| Falling Leaves, Ambient Leaves | Rejected | Duplicates of Particular |
| Make Bubbles Pop, Cave Dust | Rejected | Particular already does it (and Cave Dust does not exist for NeoForge) |
| Wakes, Effective, Visuality, Eating Animation | Impossible | Fabric only |
| Physics Mod | Rejected | Heavy, closed source, ragdolls and collapsing blocks: out of tone, and risk with Sable |

### Creatures: Fresh Animations comes in

| Element | Verdict | Detail |
|---|---|---|
| **Fresh Animations** 1.10.4 | Retained (the maintainer likes it) | Resource pack, with **Entity Model Features** 3.3.12 and **Entity Texture Features** 7.2.6 |
| FA: Details, Objects, Emissive | Retained | Details on mobs, object animations (boats, minecarts), eyes that glow at night in the mist |
| **FA: Player Extension** | Retained (maintainer's choice, 2026-10-18) | The player is animated in the same style as the creatures. It **replaces Not Enough Animations**, and therefore First-person Model, which depends on it |
| Fresh Moves | Rejected | Another player-animation pack: duplicate of Player Extension |

Three points to check on `test/finition`:
1. **No Man's Land already remodels** pigs, cows and squids. We need to see which one wins. If NML wins, Fresh Animations animates everything else (sheep, wolves, foxes, horses, chickens, rabbits…) and that is already a lot.
2. **The player in first person**: without First-person Model, the player's body is no longer visible when looking down. **Real Camera** (beta, requires Cloth Config) draws the player model in first person, hence with the Player Extension animations. To try; otherwise we keep the vanilla arms.
3. **Quark's animal variants** ("Variant Animal Textures", active): ETF usually handles them, but we need to check that the textures stay properly placed on the Fresh Animations models.

The fauna of other mods (Naturalist, Alex's Mobs, Critters and Companions…) already has its own GeckoLib animations: Fresh Animations does not touch it.

### Body and items

| Candidate | Verdict | Why |
|---|---|---|
| **3D Skin Layers** | Retained | Skin layers in relief. To check with the Player Extension player model |
| Real Camera | To test | Seeing one's body in first person (point 2 above) |
| **Camera Overhaul** | Retained, set low (11.7) | The camera tilts when turning or strafing, reacts to jumps and falls, sways gently when idle |
| MB-3D Items (`mb3d-items-pack`) | Already in 10.6 | The correct Modrinth identifier is `mb3d-items-pack` |
| Better Lanterns | Rejected | Amendments already draws lanterns in 3D; the two models would replace each other |
| Hold My Items | Impossible | Fabric only |

### Sounds

| Candidate | Verdict | Why |
|---|---|---|
| **Sounds** (IMB11) | Retained | 170 sounds for the interface, items and blocks: opening a bag, setting down an object, turning a page. This is the "touch" of Realism Craft. Client-only, LTS version for 1.21.1; requires MRU (new library) and YACL (already there) |
| ~~Dynamic Surroundings~~ (OreCruncher, the original author) | Dropped (12.1) | Only three features were kept: **footsteps** that sound according to the ground (a light version of Presence Footsteps), **waterfalls** (sound) and the **aurora** in cold biomes. Fog, biome sounds (AmbientSounds) and reverb (Sound Physics) are disabled |
| Presence Footsteps | Replaced | No NeoForge 1.21.1 version on Modrinth; Dynamic Surroundings covers footsteps |
| Drip Sounds | Optional | The sound of drops falling in caves; still in beta |
| Auroras | Fallback | If Dynamic Surroundings does not fit, this mod only does the aurora (last updated 2024) |

## 11.3 Comfort

Rule: first enable what **Quark** already does (it is in the pack), and only then add a mod.

### In Quark, to change

| Module | Today | Proposed | Why |
|---|---|---|---|
| Automatic Tool Restock | off | **on** | The broken tool is replaced by the next one in the inventory. Avoids Stack Refill |

Already active and kept: **inventory sorting** (button in the inventory and chests, kept at the maintainer's request), quick transfer, chest search, item counter, right-click harvesting, improved ladders, emotes, locked rotation.

### To add

| Mod | Side | What it brings | Link with the rest |
|---|---|---|---|
| **Pick Up Notifier** | C (S optional) | A small discreet list of picked-up items, which fades away | Puzzles Lib, already there for Visual Workbench |
| ~~Leaves Be Gone~~ (dropped, 12.1) | S | The leaves of a felled tree disappear quickly | Goes with FallingTree |
| ~~Easy Anvils~~ (dropped, 12.1: breaks Overgeared's anvil) | CS | Items stay on the anvil, in 3D, and the "too expensive" message disappears | Same family as Visual Workbench: each workstation shows what is being done on it. **To check with Overgeared** |
| **Easy Magic** | CS | Items stay on the enchanting table, enchantment rerolls | Same family |
| **Seamless Loading Screen** | C | When returning to the world, the loading screen shows the last view | The permanent world resumes where it was left |
| **Dynamic FPS** | C | The game slows down when in the background | Comfort and battery |
| Chat Heads | C | The player's head next to their message | Optional, useful among friends |

### Rejected, and why

| Mod | Reason |
|---|---|
| Jade, AppleSkin | Already rejected: a name on everything you look at and numbers on hunger break the mystery |
| Just Zoom, Zoomify | The spyglass is Wildex's discovery tool; a free zoom would make it useless |
| Waystones | Teleportation erases travel, the rail network and airships |
| Carry On | Incompatible with Aeronautics (chapter 6) |
| Inventory Profiles Next, Inventory Essentials, Crafting Tweaks, Stack Refill, Right Click Harvest | Quark already does it |
| Better Third Person | Shoulder Surfing Reloaded is retained |
| Shulker Box Tooltip | The End is closed: no shulkers |
| Enchantment Descriptions | Two more libraries for information found in EMI |

## 11.4 Performance

The additions are almost all client-side and light. Two safeguards:
- **The particle budget**: Particular and Subtle Effects each add particles. Subtle Effects has a particle render distance; it is set below the mist limit.
- **More Culling** (requires Cloth Config): to try in order to offset the cost of Better Leaves in forests. It changes how blocks are hidden, so it is tested with Sodium 0.8 and an assembled airship, like Entity Culling.

## 11.5 Figures

| | Mods | Resource packs |
|---|---|---|
| Rendering | Polytone, Particular Reforged, EMF, ETF, 3D Skin Layers, Sounds (+ MRU): **7**, and Not Enough Animations and First-person Model are dropped | Fresh Animations + Details, Objects, Emissive, Player Extension: **5** |
| Comfort | Pick Up Notifier, Leaves Be Gone, Easy Anvils, Easy Magic, Seamless Loading Screen, Dynamic FPS: **6** | — |
| To test | Dynamic Surroundings, Real Camera and More Culling (+ Cloth Config), Falling Leaves Plus as a fallback | — |
| Rejected on 2026-10-18 | Dragged's Simulated Tree Chopper ("that's too much", the maintainer) | — |

The pack would go from about 110 to about 123 files, almost all of the additions being client-side and disableable.

## 11.6 Archie's Dinos (archieblocks.com)

What it is: 7 dinosaurs, each with its own habitat (lush caves, jungle, warm ocean, Nether, Deep Dark ×2, End), taming by ritual, mounts, breeding, a merchant NPC (Archie), a stamp guide, 6 armors, 8 weapons and tools, an end boss. **Java: Forge and Fabric, Minecraft 1.20.1 only**, free, "coming soon" on CurseForge and Modrinth. Bedrock: paid add-on.

**Moving to 1.20.1: no.** Tilth would lose Create Aeronautics, Sable and No Man's Land, which exist only for NeoForge 1.21.1 (and NML will not be ported, according to its authors). The pack is built around them.

**Porting it to 1.21.1: feasible, but not without the authors.**
- Technically, a move from Forge 1.20.1 to NeoForge 1.21.1 touches item components (end of NBT on items), registries, networking and the GeckoLib version. It is a big job, followed by maintenance at every update of the original mod.
- Legally, the authors' **permission** is required to redistribute a port, even among friends in a modpack. The license is not yet published, and the Bedrock version is paid: a refusal is possible.
- The right first step: ask the authors (Eternal Creations and Archie Blocks) whether a NeoForge 1.21.1 version is planned, or whether they accept a port.

**And in Tilth?** Even ported, it would fit poorly as is:
- 2 species out of 7 live in the Nether or the End, which Tilth closes;
- the boss, armors and weapons go against the "almost empty" world;
- its stamp guide duplicates the Wildex bestiary, and Archie is a merchant NPC, which was rejected with Easy NPC;
- decision 0003 already set prehistory aside.

What could come in: the **4 Overworld species** (Lushsaur, Therimoss, Coralosaur, Sculkraptor; not the Sculksaur, which is the boss) as rare zoo animals, with one Wildex page each, without the boss or the equipment. To be revisited when the mod is released and its license is known.

## 11.7 Camera, recipes and bestiary (requests of 2026-10-18)

### Camera Overhaul: yes, set low

Version 2.1.2 for NeoForge 1.21.1, client-only, requires Cloth Config (already planned for Real Camera and More Culling), GPL license. Each effect has its own intensity in `cameraoverhaul.toml`, and 0 turns it off. This is exactly the "feel of the gesture" of Realism Craft.

Proposed settings, delivered by Default Options:
- tilt when turning and strafing: low;
- pitch on jumps and falls: medium;
- explosion shake: kept;
- sway when idle: kept, very gentle.

To check on `test/finition`: aboard an airship or a Create train (does the tilt add to the vehicle's movements?), and with Real Camera, which also moves the camera. If the two get in each other's way, Camera Overhaul takes precedence over Real Camera.

### Spatial GUI: menus in the world

Version 1.8 for NeoForge 1.21.1 (released on 2026-10-17), client-only, requires YACL (already there). Menus are displayed as a 3D panel in front of the player. In **third-person mode**, the camera slides to shoulder level when a chest or the bag is opened, and the character turns toward what they are looking at: this is what was requested.

Proposed settings:
- third-person mode for chests, the bag, the workbench and furnaces;
- curved screen for the inventory, which stays readable with EMI's column next to it;
- **disabled** for books and notebooks (Wildex, Ancient Remnants codex, Patchouli, Modonomicon) and for the atlas, which read better flat;
- light world blur, or off, so that the mist stays visible behind the menu.

To check: four mods now affect the camera (Shoulder Surfing Reloaded, Camera Overhaul, Spatial GUI, and Real Camera on trial). They are tested together on `test/finition`. If two get in each other's way, the proposed priority order is Spatial GUI, Camera Overhaul, Shoulder Surfing, then Real Camera.

### EMI instead of JEI

Version 1.1.24 for NeoForge 1.21.1, no dependency, MIT license. What Tilth does with it:
- **Groups and categories**: items are grouped (all planks, all Handcrafted furniture, all Twigs blocks) and the index is organized by theme rather than by mod. Everything goes through a datapack and KubeJS, which can talk to EMI.
- **The recipe tree**: EMI computes everything needed to build a Create machine, step by step. It replaces a notebook of calculations.
- **The mystery**: recipes that come from blueprints found in the ruins (10.10) can be hidden in EMI for everyone: they are learned by reading the blueprint. EMI cannot show them player by player according to their progress.

Point to check: some mods only have a JEI integration. EMI reads them if JEI is also installed; in that case JEI stays in the pack, invisible, as a mere bridge. We will know by opening the recipes of Create Aeronautics, Kaleidoscope Cookery, Farm & Charm and Overgeared.

### The Wildex pages

The Wildex 3.0.0 jar was opened. What can and cannot be changed:

| Element | Modifiable? | How |
|---|---|---|
| The book image (5 themes: vintage, modern, jungle, runes, steampunk) | **Yes** | Each theme is a 1024×704 image (`assets/wildex/textures/gui/wildex_<theme>.png`) that a resource pack replaces. A **Tilth naturalist's notebook** can be drawn |
| Tab icons, trophy background | Yes | 64×64 and 128×128 images in the same folder |
| Layout of the areas, text color | No | Hard-coded; the new drawing must keep the same areas and tones close to the theme it replaces |
| Page content | No | Generated automatically: statistics, loot, spawn locations, breeding food. No free text or per-creature illustration; the creature is shown in 3D |
| KubeJS link | Events only | Wildex notifies KubeJS when a creature is discovered or when the bestiary is completed, without being able to write into the pages |

Archie's Dinos pages are hand-illustrated, species by species. Wildex cannot do that. Two ways to get close:
1. **Recommended: an illustrated notebook alongside Wildex.** Patchouli and Modonomicon are already in the pack and accept drawn pages, free text and a custom cover. At each discovery, the Wildex event goes through KubeJS and unlocks the creature's page in the notebook. Wildex remains the automatic register (the zoo, the trophies), and the notebook carries the narrative, the drawings and the clues. The cost is mostly artistic: one illustration per major species, not per mob.
2. **Modify Wildex itself.** Its license (CC BY-NC 4.0) allows a non-commercial modified version, with attribution; a "Notes" tab with text and an image per creature is feasible. But every update of the mod would have to be followed: to keep for later.

In both cases, we start with a redrawn Wildex theme, which costs only one image.

**Choice of 2026-10-18: we stay on Wildex, with one of its original themes.** No ready-made theme for Wildex or for Modonomicon exists on Modrinth, and the maintainer cannot draw the pages. The illustrated notebook and the modified Wildex are therefore set aside until someone provides the drawings. A simple touch-up remains possible without drawing: tinting or desaturating the image of an existing theme to match the colors of the mist.

For comparison, the Ancient Remnants codex does not use Patchouli: it is a screen coded by the author (Obscuria). Each page is a data file (title, text, 350×210 image, biomes), which suggests that a datapack could add pages to it; to be tested.

## 11.8 Ancient Remnants: Monoliths

Added at the maintainer's request. Version 1.5.2 for NeoForge 1.21.1 (published on 2026-10-18), client and server, requires Fragmentum (Obscuria's library, 4.5 million downloads). A mod written for the CurseForge ModJam "Echoes of the Past", still very young (about 500 downloads).

What the jar contains (opened on 2026-10-18):
- **6 monuments**, each tied to a biome: three monoliths (hunter, warrior, sentinel) and three monuments (cherry, oak, conifers).
- At the center, an **Elderheart** guarded by creatures. It grants a **blessing**: hunter's agility, warrior's strength, sentinel's defense, vitality at low health, build reach, abundant harvest.
- A **Monolith Codex** filled with research fragments scattered across the world, left by a wandering trader. Each monument has its history page.
- Spacing and biomes adjustable in the config or through biome tags.

Why it belongs: it is exactly the "almost empty and enigmatic" world. The monuments are rare, you hear them before seeing them (a rising hum), and the mod refuses to explain everything: you piece the story together yourself. The codex is a long-term goal, like the bestiary.

Proposed settings and links:
- **Biomes**: attach each monument to No Man's Land biomes through tags (the default values are added by the code, and NML biomes may not be in them). The cherry monument only makes sense if a cherry biome exists.
- **Rarity**: wide spacing, in line with Sparse Structures. A monument must remain an event.
- **The mist**: to prototype, an awakened Elderheart could open a clearing (10.3) around the monument. The event to hook into KubeJS must be found in the code.
- **The guardians**: they follow rule 0003 (rare, territorial hostiles), so they stay within the monument.
- **The blessings**: check their duration and strength in play. "Build reach" and "abundant harvest" fit well with the rest of the pack; strength and agility must not make the fauna trivial.

To check on a test branch: generation alongside NML and Sparse Structures, and the stability of such a recent mod on a permanent world. If the mod is removed afterward, monuments already generated lose their blocks and leave riddled ruins: it must therefore be tested before opening the world.
