# 10. Refocus: an almost empty, enigmatic world

> **Check of 2026-10-18: [chapter 12](12-verification.md) corrects this chapter** (Easy Anvils, Leaves Be Gone, Dynamic Surroundings and Entity Culling are out, Polytone without lightmap, camera order, Overgeared, Kaleidoscope Compat, Monoliths).

This chapter **replaces the list in chapter 1** and the "decisions to validate" in 8.4. Chapters 2 to 7 remain the technical reference (configs, keys, scripts) for the mods that stay.

History of the maintainer's feedback:
- **2026-10-11**: Sanity: Renewed removed; wildlife, companions and Traveler's Backpack kept; ParCool stays out; the reference for the atmosphere is **the No Man's Land trailer**.
- **2026-10-18**:
  - Removed: Capture Nets, Paraglider, GUI Compass, Simple Voice Chat, Hardcore Revival, Create Aeronautics Structures, Cinch's Abandoned Houses, Cattlestrophic! and Easy NPC.
  - New requirement: a **polished finish in the manner of Realism Craft**, in use, building, models and atmosphere, and **everything linked together** for a smooth experience (10.6 and 10.7).

## 10.1 What drifted

The v1 aimed for about 100 mods and 140 files with libraries. Every mod passed the 5-question filter, but the filter judged mods one by one, never the whole. It piled up:
- too many Create addons;
- three structure mods for the same role;
- four survival gauges;
- long-distance view tools that cancel out the mist.

## 10.2 The basic idea

1. **A permanent, smooth world.** Content, but not too much: each mod must change the way the game is played, be stable and stay light. What is built remains, and resources run out.
2. **The atmosphere of the No Man's Land trailer.** Vast landscapes in the mist, ruins, menhirs, a lone traveller. The world is **almost empty and enigmatic**, not dark.
3. **The mist lifts.** It clears in places and at certain times, and above all **where the player brings life back**.
4. **Long-term goals.** A zoo, a complete bestiary, clearings, a rail network, an airfield, an entire atlas.
5. **A polished, connected finish.** Travelling the world, building, eating, cutting down a tree, opening one's pack: every action must be pleasant to look at and to perform. The mods must talk to each other, not merely coexist.

**Filter in addition to the 5 questions of the vision**:
- **Weight**: does it change what the player decides, aims at or feels with every action?
- **Tone**: does it keep the world almost empty and enigmatic, without cluttering or darkening it?
- **Link**: does it plug into what already exists (wood, temperature, cooking, wildlife, map)?
- **Cost**: is it stable, light, and risk-free for a world meant to last several years?

## 10.3 The mist that lifts

### Layer 1: the world (ATMOSPHERICS, already selected)

ATMOSPHERICS sets fog, sky and mist **per biome**, with separate settings for day and night. Its presets can be shipped in a resource pack (`assets/atmospherics/preset/<name>.json`).

The Tilth preset splits the No Man's Land biomes into two families:
- **Misty**: dense forests, swamps, bogs, taiga, valleys.
- **Clear**: meadows, flower fields, maple forests, highlands.

### Layer 2: the sky (Enhanced Celestials)

The blood moon is removed. The harvest moon and the blue moon are kept, and a **white moon** is added: a pale sky, a strange night, nothing hostile. Draft: `kubejs-brouillons/data/tilth/enhancedcelestials/lunar/event/lune_blanche.json`.

### Layer 3: the clearings (Tilth mechanic, KubeJS)

The player places a **clearing fire**. The script converts the biome within a radius around it into **`tilth:clairiere`** with the vanilla `/fillbiome` command. This biome has:
- a clear mist (ATMOSPHERICS preset);
- grass and foliage of a vivid green;
- animal spawns;
- the Ecliptic tags of a temperate biome.

| Tier | Item | Radius | Crafting |
|---|---|---|---|
| 1 | Vigil Fire | 16 blocks | By hand: stone, charcoal, No Man's Land resin |
| 2 | Clearing Lantern | 32 blocks | Forged parts from Overgeared |
| 3 | Dissipator | 64 blocks | Create workshop: gears, fan, brass |

The change is permanent and applied only once, in 8 × 64 × 8 columns spread over several ticks: it costs nothing continuously. Rivers, oceans and caves are not affected.

Drafts: `startup_scripts/clairieres.js`, `server_scripts/clairieres.js`, `data/tilth/worldgen/biome/clairiere.json`.

To check in game:
- Does ATMOSPHERICS recognise a datapack biome in a preset?
- Do clients see the new colour without reconnecting?
- Does Ecliptic accept the biome?

## 10.4 The long-term goals

| Goal | How | Mods |
|---|---|---|
| **The zoo** | Find the species, bring them back alive, build them an enclosure resembling their biome in a clearing. Animals are brought back on a lead, by boat, in an Icy's cart or in a Create wagon | Selected wildlife and companions, Icy's Better Horses, Create, clearings |
| **The bestiary** | Discover each species with the spyglass or by **photographing** it with Exposure. A pedestal displays the hologram of each discovered species: it is the zoo's showcase | **Wildex Bestiary** |
| **The clearings** | Push back the mist around the base, then outposts, tier by tier | Tilth script |
| **The network** | A railway between the clearings, then an airfield | Create, Steam 'n' Rails, Create Aeronautics |
| **The atlas** | A map that draws itself as you walk, without coordinates | Antique Atlas 4 (AA4 Atlas removed on 2026-10-19, see the [registry](../../mods.md)) |
| **The house** | A furnished base, covered with real roofs, in the wood of the region | Handcrafted, Macaw's, Twigs, Every Compat (10.6) |
| **The bloodlines** | Horses bred over generations | Icy's Better Horses |

**Wildex Bestiary** (`wildex-bestiary`, 3.0.0, no dependencies):
- Discovery happens with the spyglass, in combat or through an Exposure photo.
- Progress is per player and stored server-side.
- The mod integrates with KubeJS.
- Its final reward, "Spyglass Pulse", shows creatures through walls: it is turned off.

## 10.5 The pack

### The mods already selected all stay

The 46 mods in `pack/mods/` stay, wildlife and companions included. The rules of decision 0003 still apply: one species per mod, rare and territorial hostiles, In Control! caps.

### What is added for gameplay

| Mod | Role |
|---|---|
| **Traveler's Backpack** | The expedition pack. It has its **own sleeping bag** to deploy, which makes Comforts unnecessary (removed) |
| **Wildex Bestiary** | The bestiary, backbone of the zoo |
| ~~**AA4 Atlas**~~ | Removed on 2026-10-19: incompatible with Antique Atlas 3, made the game crash |
| **Exposure** | Analogue photography: keep a record of a place, discover a species for the bestiary |
| **Enhanced Celestials** | Rare moons, including the white moon |
| **Lootr** | Each player finds the chests full; no content |
| **Create Ore Excavation** | Finite deposits: one has to travel further |
| **Kaleidoscope Compat** | Merges Kaleidoscope Cookery and Farm & Charm: removes duplicates |
| ~~Freeze It and Heat It~~ | Removed by the maintainer on 2026-10-18: food does not spoil |
| **Ancient Remnants: Monoliths** | Requested by the maintainer on 2026-10-18: rare monuments of a forgotten civilisation, a codex to be reconstructed page by page (11.8) |

Infrastructure, with no visible content: In Control!, LootJS, KubeJS Create, MoreJS, Neruina, Simple Backups, EMI (the maintainer's choice, in place of JEI; 11.7), GraveStone, Default Options, Crash Assistant, Mouse Tweaks, Controlling (+ Searchables).

Optional, to be shown in game before adding: Cosy Critters (birds that fly off, moths around lamps) and Longwings (butterflies to collect, hence one more page in the bestiary).

### What goes out

| Removed | Reason |
|---|---|
| Capture Nets, Paraglider, GUI Compass, Simple Voice Chat, Hardcore Revival, Create Aeronautics Structures, Cinch's Abandoned Houses, Cattlestrophic!, Easy NPC | Removed by the maintainer on 2026-10-18 |
| Comforts | Duplicate of Traveler's Backpack's sleeping bag |
| ParCool! | Disabled, as requested |
| Sanity: Renewed, TheWatcher | The world is enigmatic, not dark |
| Better Archeology | Content without a strong mechanic |
| Create: New Beginnings, Let The Adventure Begin, MCG's RPG Campsites, optional structure mods | The No Man's Land ruins and the widely spaced vanilla structures are enough |
| Traveler's Titles | The region name on screen breaks the mystery |
| Distant Horizons, Separate Sable Render Distance, Simple Clouds, Particle Rain | The mist makes long-distance view pointless: this is the biggest performance saving |
| Streams Reflowing | Heavy, irreversible worldgen on a permanent world |
| Copycats+, Connected, Factory Logistics, Bells & Whistles, Diesel Generators, Power Loader, Prospecting Picks, Rechiseled (+ Create, Fusion) | The pile of Create addons that the vision forbids; COE is the only one kept |
| Create Villager Labor, Open Parties and Claims | Populated colonies and claim protection: unnecessary among friends |
| Salt's Animal Farm, Animal Genders, Kaleidoscope Tavern, Thirst Canteen, Tide, Nutritional Balance, AppleSkin | One more gauge or activity for a role already covered |
| Reactive Music | AmbientSounds is enough; silence is part of the atmosphere |
| Almost Unified, Jade | Fewer duplicates to unify; Jade names everything one looks at |

## 10.6 The finish, in the manner of Realism Craft

Realism Craft (Spark Universe, Bedrock only) according to its official wiki:
- player animations and 3D models for tools;
- dynamic lighting and visual effects;
- reworked sounds and realistic animals with per-biome variants;
- a lot of **natural decor** (fallen trunks, pebbles, branches on the ground, hanging moss, mushrooms on trees);
- complete **furniture** (chairs, tables, wardrobes, curtains), tents, ropes and a tree saw.

None of this exists for Java. The same feel is rebuilt in layers, with mods that almost all rely on libraries **already in the pack**.

One limit holds from the start: **no realistic textures and no shaders**. Iris has known problems with Create Aeronautics, and a 128× texture pack would break the style of No Man's Land. The finish comes from the gestures, the models, the building and the sound, in NML's "polished vanilla" style.

| Layer | Mod | What it brings | Side | Dependency |
|---|---|---|---|---|
| **The body** | **Fresh Animations: Player Extension** (the maintainer's choice, in place of Not Enough Animations and First-person Model) | The player is animated in the same style as the creatures; Real Camera, to be tested, also shows it in first person (11.2) | C | Entity Model Features |
| | **Shoulder Surfing Reloaded** | A polished over-the-shoulder camera for third person | C | none |
| **The objects** | **ItemPhysic Lite** | Dropped items lie flat on the ground instead of floating and spinning | C | CreativeCore (already there) |
| | **Visual Workbench** | Ingredients stay laid out on the crafting table, in 3D | CS | Puzzles Lib |
| | **Amendments** | 3D lanterns in the hand and hung on walls with a sway, swinging signs, bubbling cauldrons, carpets on stairs; tuned, like Supplementaries, to the essentials | CS | Moonlight (already there) |
| | **MB-3D Items** resource pack | Every vanilla item becomes a 3D model in Minecraft's style | C | none |
| **Building** | **Handcrafted** | Polished furniture: chairs you can sit on, tables, wardrobes, bunk beds, coloured cushions and sheets | CS | Resourceful Lib (already there) |
| | **Macaw's Roofs** + **Macaw's Windows** | Real sloping roofs instead of stairs; windows, shutters, curtains | CS | none |
| | **Twigs** | Twigs, pebbles, rocks, tiles and bricks: the small ground decor that Realism Craft puts everywhere | CS | none |
| | **Every Compat** | Generates all the furniture, roofs, windows and Twigs blocks **in every wood in the world**, including the willow, walnut and maple of No Man's Land and Quark's woods | CS | Moonlight (already there) |
| **Nature** | **FallingTree** ("falling blocks" mode) | Cutting the foot of a tree makes it fall to the ground, with an animation; only axes count, Overgeared's included | S | none |
| | **Motschen's Better Leaves** resource pack | Bushy foliage, compatible with many mods | C | none |
| **The interface** | **Smooth Swapping** | Items slide from one slot to another in the inventory | C | none |

Already selected and part of the finish: LambDynamicLights, Subtle Effects, AmbientSounds, Sound Physics Remastered, ATMOSPHERICS, More Darkness, the animal remodels of No Man's Land.

**Fresh Animations comes in** (the maintainer likes it), with Entity Model Features, Entity Texture Features and three official extensions. Rendering without shaders (Polytone, Particular Reforged, Sounds...) and comfort are detailed in [chapter 11](11-rendu-et-confort.md).

**To check on a `test/finition` branch**:
- **Fresh Animations against No Man's Land**, which already remodels pigs, cows and squids: see which one wins.
- **Footsteps**: Presence Footsteps has no NeoForge 1.21.1 version; Dynamic Surroundings, reduced to footsteps, waterfalls and the aurora, replaces it (11.2).
- **Better Leaves**: check the cost in frames per second on NML's large forests.

## 10.7 Linking everything

This is the work that makes the difference between a list of mods and a smooth world. Each link is a config, a tag or a script, not one more mod.

| Link | What is set | Where |
|---|---|---|
| **Wood** | Every Compat provides all variants in every species; a single list of woods, the world's | Every Compat, automatic |
| **Heat** | Kaleidoscope Cookery's stoves, boiling cauldrons, braziers and fireplaces warm according to Cold Sweat | Cold Sweat config (heat sources per block) |
| **Seasons** | Ecliptic sets the perceived temperature (existing script), the snow, and the Farm & Charm and Kaleidoscope Cookery crops | `saisons_cold_sweat.js`, `eclipticseasons:crops/*` tags |
| **Eating and drinking** | A single cooking system (Kaleidoscope Compat); Kaleidoscope's soups and drinks hydrate for Thirst Was Reclaimed | Thirst config, tags |
| **Wildlife** | One species per mod, spawns per biome, animals that return to the clearings, a bestiary that reads the photo | In Control!, biome tags, Wildex + Exposure |
| **The gesture** | The held lantern gives light (LambDynamicLights), animates (Amendments) and shows in first person if Real Camera passes the test | Client configs |
| **The tree** | FallingTree only works with axes, including Overgeared's; NML logs are recognised | `minecraft:logs` and axes tags |
| **The map** | Antique Atlas marks NML ruins and discovered COE deposits; never coordinates | Surveyor and Antique Atlas config |
| **Loot** | Engineer blueprints and rare items come out of NML ruins and vanilla structures, per player (Lootr) | LootJS (`loot/butin.js`) |
| **The interface** | A single recipe viewer (EMI, sorted by category), conflict-free keybinds shipped by Default Options, the same video settings for everyone | Default Options, Controlling |

## 10.8 In numbers

| | v1 | v2 of 2026-10-18 |
|---|---|---|
| Gameplay mods (excluding atmosphere, technical and finish) | about 70 | **37** (28 kept + 9 added) |
| Finish mods | 0 | **11** (Not Enough Animations and First-person Model replaced by Player Extension), plus 3 resource packs |
| Files in `pack/mods/` with libraries | about 140 | **about 110** |
| Survival gauges | hunger, thirst, temperature, nutrition | hunger, thirst, temperature |
| Create addons added | 8 | 1 (COE) |
| Server view distance | 10, plus Distant Horizons on trial | **8**, simulation 6, under the mist |

## 10.9 Performance

- `view-distance=8`, `simulation-distance=6`. ATMOSPHERICS' mist ends before the edge of the loaded chunks.
- Wildlife stays: the In Control! caps in [5.3](05-faune-et-ambiance.md) are essential.
- The finish is almost entirely client-side and light (animations, models, sliding). The two points to measure with spark are the bushy foliage and FallingTree on very large trees.
- Clearings only cost something at the moment they are placed.
- World pre-generated using Chunky, border at the edge of the pre-generated area.

## 10.10 KubeJS scripts that change

| Script | Fate |
|---|---|
| `garde_manger.js`, `raison.js` | **Removed** |
| `plans_ingenieur.js` + `loot/butin.js` | Blueprints no longer come from Create Aeronautics Structures wrecks: they come out of the ruins and dungeons of No Man's Land and vanilla structures |
| **17. `clairieres.js`** (startup + server) + `tilth:clairiere` biome + ATMOSPHERICS preset | The clearing fires (10.3) |
| **18. `zoo.js`** | Rewards for the Wildex bestiary milestones, for example the Clearing Lantern blueprint after 25 species |
| All the others | Kept |

## 10.11 The vision

The line in `docs/vision.md` "Solitude and diffuse strangeness, without horror or jumpscares" is still right. It is proposed to refine it:

> Solitude in an **almost empty and enigmatic** world, in the spirit of the No Man's Land trailer: mist, vast landscapes, traces of those who have left. The mist lifts in places and at certain times, and where life is brought back. Every gesture is polished and every system plugs into the others. No horror or jumpscares.

And in "Working rules": "The pack is judged as a whole: every addition must be worth its cost in performance and tone, and must link to what already exists."

## 10.12 Decisions to validate (replaces 8.4)

| # | Subject | Default chosen | Alternative |
|---|---|---|---|
| 1 | Clearings | Permanent, 3 tiers (16, 32, 64 blocks) | Reversible when the fire is broken |
| 2 | Bestiary | Wildex (Exposure photo, holographic pedestals) | Field Guide (adds plants, but requires Immersive Overlays, which shows coordinates) |
| 3 | Furniture | Handcrafted | Macaw's Furniture or Another Furniture |
| 4 | Building | Macaw's Roofs + Windows, Twigs | Add Macaw's Doors and Fences |
| 5 | 3D items | MB-3D Items | Original flat items |
| 6 | Trees | FallingTree, "falling blocks" mode | "Trunk sinks" mode (less of a shortcut) or no FallingTree |
| 7 | Animations | Fresh Animations + Details, Objects, Emissive and Player Extension (the player too, the maintainer's choice) | Only NML's remodels |
| 8 | White moon | Kept, with no hostile effect | Only the original moons, without the blood moon |
| 9 | Nether | Closed (unchanged) | Open |
| 10 | View distance 8 under the mist | Yes | 10 |
| 11 | Rendering without shaders | Polytone, Particular Reforged, Sounds, 3D Skin Layers (11.1) | ATMOSPHERICS and Subtle Effects only |
| 12 | Comfort | 6 mods (11.3) and Quark's automatic tool restocking | Quark alone |
| 13 | Archie's Dinos | Stay on 1.21.1; ask the author for a NeoForge version or permission to port (11.6) | Move to 1.20.1 (loses Aeronautics, Sable and NML) |
