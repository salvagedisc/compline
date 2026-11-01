# Mod registry

Complete list of the pack, updated on 2026-10-25 (late evening, after PRs #1 to #3): what is in `pack/mods/`, what remains to be tested and what is dropped. Every mod listed ✅ is in the pack today; nothing is waiting to be added. Updates held back until the in-game tests are listed in the [roadmap](feuille-de-route.md#updates-held-until-the-in-game-tests); the tests themselves are grouped in [tests-en-jeu.md](tests-en-jeu.md). The reasoning behind each choice is in the [plan](plan/README.md), especially chapters [10](plan/10-recentrage.md), [11](plan/11-rendu-et-confort.md) and [12](plan/12-verification.md).

Statuses:
- ✅ in the pack (`pack/mods/`);
- ➕ to add (kept by the plan, not yet in `pack/mods/`);
- 🟡 to test before deciding;
- ⚪ optional;
- ❌ leaves the pack.

Side: C = client, S = server, CS = both. Versions checked on Modrinth for NeoForge 1.21.1 on 2026-10-25.

## World and exploration

| Mod | Slug | Status | Side | Role |
|---|---|---|---|---|
| No Man's Land | `no-mans-land` | ✅ (1.6.3) | CS | The world: biomes, ruins ([0011](decisions/0011-tectonic-streams-distant-horizons.md)); its animals and creatures are disabled, vanilla animals remain ([NML fauna](plan/faune-nml.md)) |
| Tectonic | `tectonic` | ✅ (3.0.28, not seen in game) | CS | The terrain under NML's biomes: continents, mountain ranges, valleys, underground rivers; height -64 to 320, never to change once the world exists; requires Lithostitched without declaring it ([0011](decisions/0011-tectonic-streams-distant-horizons.md)) |
| Streams Reflowing | `streams-reflowing` | ✅ (2.15.2, not seen in game) | CS | Streams and lakes that follow the relief, currents for boats and Sable hulls; world settings frozen at creation ([0011](decisions/0011-tectonic-streams-distant-horizons.md)) |
| Sparse Structures | `sparsestructures` | ✅ | CS | Spaced-out structures ([0004](decisions/0004-structures.md)) |
| Ancient Remnants: Monoliths | `ancient-remnants` | ✅ | CS | 6 rare monuments, blessings, codex to piece together; guardians drawn from Mowzie's Mobs and Companions! ([12](plan/12-verification.md#the-monument-guardians-report-6)) |
| Lootr | `lootr` | ✅ | CS | Each player finds the chests full |
| Enhanced Celestials 2 (Core + Default Lunar Events) | `enhanced-celestials-2-core`, `enhanced-celestials-2-default-lunar-events` | ✅ | CS | Rare moons; blood moons removed, a single moon at a time with the Friendly Moon ([friendly moon](plan/lune-amie.md)). Replaces Enhanced Celestials 6; Shader Support addon useless without Iris |
| Antique Atlas 4 | `antique-atlas-4` | ✅ | C | Hand-drawn map, no coordinates |
| Antique Transport | `antique-transport` | ✅ (added 2026-10-25) | CS | Create trains, tracks, stations and Sable ships appear on the atlas |
| Surveyor Map Framework | `surveyor` | ✅ | CS | Shared exploration for the atlas |
| Icy's Better Horses | `icys-better-horses` | ✅ (2.0.1; 2.1.x held, see the roadmap) | CS | Horses, breeds, bond, carts |

## Long-term goals

| Mod | Slug | Status | Side | Role |
|---|---|---|---|---|
| Questlog | `questlog` | ✅ (added 2026-10-26) | CS | Book-style journal of quests and guides, by chapter, editable in game ([quetes](plan/quetes.md)) |
| Exposure | `exposure` | ✅ | CS | Analogue photography: keep a place, discover a species |
| Exposure: Space | `exposure-space` | ✅ (added 2026-10-25) | CS | Astrophotography: 200 celestial objects to photograph and catalogue; French names translated |

## Civilisation and technology

| Mod | Slug | Status | Side | Role |
|---|---|---|---|---|
| Create | `create` | ✅ | CS | Technology |
| Create Aeronautics + Sable | `create-aeronautics`, `sable` | ✅ (Sable 2.0.6) | CS | Airships; requires NeoForge 1.21.1 |
| Create Aeronautics: Automated Logistics | `create-aeronautics-automated-logistics` | ✅ (added 2026-10-25) | CS | Recorded airship routes, airship stations, transponders: an air network as a long-term goal |
| Sable: Physics Compat | `sablecompat` | ✅ (added 2026-10-25) | CS | Mass, buoyancy and friction for the blocks of about 100 mods on Sable ships (its `sable:bouncy` tag fails to load, harmless) |
| Aeronautics Wind Sound | `aeronautics-wind-sound` | ✅ (added 2026-10-25) | C | Wind in flight, by speed and shelter |
| Create: Steam 'n' Rails | `create-steam-n-rails-1.21.1` | ✅ | CS | The railway (beta) |
| Overgeared | `overgeared` | ✅ | CS | Forging: blast furnace, hammering, quenching, quality, blueprints |
| KaleidoscopeCookery-Automation | `kaleidoscopecookery-automation` | ✅ | CS | Create arm on cooking stations ([0002](decisions/0002-agriculture.md)) |

## Daily life

| Mod | Slug | Status | Side | Role |
|---|---|---|---|---|
| Kaleidoscope Cookery | `kaleidoscope-cookery` | ✅ (1.6.0) | CS | Cooking |
| [Let's Do] Farm & Charm | `lets-do-farm-charm` | ✅ (stay on 1.1.25) | CS | The farm and the oven |
| Kaleidoscope Compat | `kaleidoscope-compat` | ✅ | CS | Makes the duplicates of the two cooking mods interchangeable |
| Ecliptic Seasons | `ecliptic-seasons` | ✅ (0.15.5) | CS | Seasons, farming only (`AGRICULTURE`), no heat stroke: Cold Sweat owns the temperature |
| Cold Sweat | `cold-sweat` | ✅ (stays on 2.4.3.1 for Altitude) | CS | Temperature |
| Cold Sweat: Altitude | `cold-sweat-altitude` | ✅ (added 2026-10-25) | CS | Colder as you climb, sheltered areas, windproof lining; supports airships |
| Thirst Was Reclaimed | `thirst-was-reclaimed` | ✅ | CS | Thirst |
| Tannery: Better Leather | `tannery-better-leather` | ✅ (1.0.2, added 2026-10-29, not seen in game) | CS | The tannery: hides, tanning rack, bark and tanning barrel, sinew, fat, studded leather; satchels removed, translated into French ([0014](decisions/0014-tannery.md), [tannery](plan/tannerie.md)) |
| Reliable Backpacks | `reliable-backpacks` | ✅ (added 2026-10-26) | CS | A single simple bag, three rows, worn on the back (Curios), placeable ([0012](decisions/0012-sacs-piles-jours.md)) |
| Comforts | `comforts` | ✅ (back on 2026-10-26) | CS | Sleeping bag and hammock ([0012](decisions/0012-sacs-piles-jours.md)) |
| Sensible Stackables | `sensible-stackables` | ✅ (added 2026-10-26) | CS | Stack sizes by kind of item, set by the `fallow:pile/N` tags ([0012](decisions/0012-sacs-piles-jours.md)) |
| Better Days | `betterdays` | ✅ (added 2026-10-26) | CS | 30 minutes of day, 15 of night ([0012](decisions/0012-sacs-piles-jours.md)) |
| Envelope | `envelope` | ✅ | CS | Mail by pigeon between players, lost letters to find (idea taken from Wayfarer, [research](plan/recherche/2026-10-19/wayfarer.md)) |

## Fauna

| Mod | Slug | Status | Side | Role |
|---|---|---|---|---|
| Naturalist | `naturalist` | ✅ | CS | Realistic base fauna ([0003](decisions/0003-faune.md)) |
| Alex's Mobs Continued | `alexs-mobs-continued` | ✅ | CS | Variety, species by species |
| Hybrid Aquatic | `hybrid-aquatic` | ✅ (biomes disabled) | CS | Oceans |
| Mowzie's Mobs | `mowzies-mobs` | ✅ | CS | Unique, very rare creatures; Wroughtnaut, Frostmaw and Sculptor guard three monuments; natural spawns rarer (`extra_rarity`), only in the dark, two per player at most; Wroughtnaut chambers, Frostmaw and Umvuthana groves no longer generated, boss weapons removed ([pack sort](plan/tri-du-pack.md)) |
| Longwings | `longwings` | ✅ | CS | Butterflies to collect |

## Companions

| Mod | Slug | Status | Side | Role |
|---|---|---|---|---|
| Critters and Companions | `critters-and-companions` | ✅ | CS | Tameable small fauna |
| Friends & Foes | `friends-and-foes-forge` | ✅ | CS | Peaceful creatures from the mob vote; Iceologer, Illusioner, Wildfire, their structures and the zombie horse trap disabled, fewer Maulers and crabs (`pack/config/friendsandfoes.json`) |
| Adorable Hamster Pets | `adorable-hamster-pets` | ✅ | CS | Hamsters; throwing, item stealing, diamond sniffing and redstone fever disabled |
| Companion | `companion` | ✅ | CS | Tamed animals no longer get lost |
| Companions! | `companions-mod` | ✅ | CS | Companions with powers; the Sacred Pontiff guards the oak monument; wild spawns about three times rarer |
| Ghost | `ghost` | ✅ | CS | Little ghosts |

## Building and workshops

| Mod | Slug | Status | Side | Role |
|---|---|---|---|---|
| Quark | `quark` | ✅ (4.1-487) | CS | The essentials, selected modules ([0005](decisions/0005-quark.md)) |
| Supplementaries | `supplementaries` | ✅ | CS | The essentials ([0006](decisions/0006-supplementaries.md)) |
| Amendments | `amendments` | ✅ | CS | 3D lanterns, signs, cauldrons |
| Handcrafted | `handcrafted` | ✅ | CS | Furniture |
| Twigs | `twigs` | ✅ | CS | Small decor; rhyolite generation off, tables, Nether blocks and duplicates removed ([pack sort](plan/tri-du-pack.md)) |
| Every Compat | `every-compat` | ✅ | CS | Furniture and decor in every wood, including NML's |
| HT's TreeChop | `treechop` | ✅ | CS | Chop a tree block by block, the trunk falls last |
| Visual Workbench | `visual-workbench` | ✅ | CS | Items stay laid out on the workbench |
| Immersive Enchanting | `immersive-enchanting` | ✅ (beta) | CS | Enchanting becomes lore: ancient books found, shelved near the table, levels paid in materials ([0008](decisions/0008-enchantement.md)); structures disabled, French translation |

## Body, camera and gestures

| Mod | Slug | Status | Side | Role |
|---|---|---|---|---|
| Entity Model Features + Entity Texture Features | `entity-model-features`, `entitytexturefeatures` | ✅ | C | Engine of Fresh Animations |
| 3D Skin Layers | `3dskinlayers` | ✅ | C | Skin with relief |
| Shoulder Surfing Reloaded | `shoulder-surfing-reloaded` | ✅ | C | A single third-person view, over the shoulder, camera freely controlled with the mouse (F5: first-person or shoulder) |
| Spatial GUI | `spatial-gui` | ✅ | C | 3D menus, shoulder camera on opening |
| Camera Overhaul | `cameraoverhaul` | ✅ | C | The camera follows movement (set low) |
| ItemPhysic | `itemphysic` | ✅ | C | Dropped items fall, roll and float |
| Smooth Swapping | `smooth-swapping` | ✅ | C | Items slide around in the inventory |
| Punchy! | `punchy-fpa` | ✅ (2.8g, pinned version) | C | Gestures and animated arms in first person, set to a restrained level; alone in animating the first-person arm (EMF option), blacklist for Create and Aeronautics items and Exposure photos |

## Atmosphere, rendering and sound

| Mod | Slug | Status | Side | Role |
|---|---|---|---|---|
| ATMOSPHERICS | `atmospherics` | ✅ | C | Mist, sky, clouds (two layers), sun, moon and stars per biome ([0007](decisions/0007-ambiance-performance.md)) |
| Polytone | `polytone` | ✅ | C | Colour of grass, leaves and water, coloured light from fires and candles ([page](plan/fallow-ambiance.md)) |
| Particular Reforged | `particular-reforged` | ✅ | C | Water, waterfalls, falling leaves, cave dust |
| Subtle Effects | `subtle-effects` | ✅ | CS | Fire, smoke, steam, body |
| LambDynamicLights | `lambdynamiclights` | ✅ | C | Light held in hand |
| More Darkness | `more-darkness` | ✅ | CS | Real nights |
| AmbientSounds | `ambientsounds` | ✅ | C | Biome sounds |
| Sound Physics Remastered | `sound-physics-remastered` | ✅ | CS | Echo, muffling |
| True Adaptive Music | `true-adaptive-music` | ✅ | CS | Music by situation (mist, clearing, season, moon, airship), long silences ([0010](decisions/0010-musique.md)) |
| `fallow` music pack | — | ✅ (`pack/trueadaptivemusicpacks/`, [page](plan/musique.md)) | CS | The tree of situations and the 29 tracks chosen by the team |
| Sounds | `sound` | ✅ | C | 170 interface, item and block sounds |
| Auroras | `auroras` | ✅ | C | Aurora in cold biomes |
| Contraption Lights | `contraption-lights` | ✅ | CS | A lantern placed on an airship or contraption lights up the world as it passes (LambDynamicLights mode, colour disabled) |
| Wakes Reforged | `wakes-reforged` | ✅ | CS | Wakes behind boats, swimmers and Sable contraptions |
| Puddles & Floods | `puddles-floods` | ✅ | CS | Puddles in the rain and at snowmelt, floods in hollows |
| Rainbows | `rainboows` | ✅ | C | Rainbow after the rain |
| Where Winds Blow | `where-winds-blow` | ✅ (2.1.0, not seen in game) | CS | Wind: grass, crops, leaves and lanterns sway, slanted rain, drifting snow and smoke, No Man's Land wind profiles; its fog is off, ATMOSPHERICS owns the mist ([0016](decisions/0016-vent-et-lumiere.md)) |
| Shine | `shine` | ✅ (2.0.2, not seen in game) | C | Bloom and coloured light without shaders ([0016](decisions/0016-vent-et-lumiere.md)) |
| Distant Horizons | `distanthorizons` | ✅ (3.3.3, not seen in game) | CS | Distant landscape (LODs), hidden by the mist and revealed where it is lifted by the `horizon` module of the `fallow` mod ([0011](decisions/0011-tectonic-streams-distant-horizons.md)) |
| Falling Leaves Plus | `falling-leaves-plus` | ⚪ | C | Fallback if Particular does not handle NML's leaves |

## Interface and comfort

| Mod | Slug | Status | Side | Role |
|---|---|---|---|---|
| EMI | `emi` | ✅ | C (S optional) | Recipe viewer, without panels or search (`pack/defaultconfigs/emi.css`): R and U on an item open its recipes |
| JEI | `jei` | ✅ (hidden, stay on 19.51.0.418) | CS | Bridge for mods that only have a JEI plugin; from 19.52, Envelope 0.7.5's mixin no longer finds its target and the game crashes when entering a world |
| Better Advanced Tooltips | `better-advanced-tooltips` | ✅ | CS | Tooltips; KubeJS bundles it anyway, so it cannot leave the pack |
| Pick Up Notifier | `pick-up-notifier` | ✅ | C | Discreet list of picked-up items |
| Mouse Tweaks | `mouse-tweaks` | ✅ | C | Mouse-driven inventory |
| Controlling + Searchables | `controlling`, `searchables` | ✅ | C | Search in key bindings |
| Default Options | `default-options` | ✅ | C | Key bindings and video settings shipped with the pack |
| Chat Heads | `chat-heads` | ✅ | C | Player head in the chat |
| FancyMenu + Drippy Loading Screen | `fancymenu`, `drippy-loading-screen` (+ `konkrete`, `melody`) | ✅ (3.9.14, 3.1.5; added 2026-10-26, not seen in game) | C | Fallow title screen (logo) and loading screen (moss, lantern bar); layouts written by `scripts/interface/menus.py`, editor hidden ([interface](plan/fallow-interface.md)) |
| Lamb Lanterns | `lamb-lanterns` | ✅ | CS | A lantern carried at the hip (Curios slot) gives light as if held in hand |

## Rules and scripts

| Mod | Slug | Status | Side | Role |
|---|---|---|---|---|
| KubeJS | `kubejs` | ✅ | CS | Scripts and datapack data |
| KubeJS Create | `kubejs-create` | ✅ | CS | Create recipes in script (beta) |
| LootJS | `lootjs` | ✅ | S | Loot of ruins and monuments |
| MoreJS | `morejs` | ✅ | CS | Extra events for KubeJS |
| In Control! | `in-control` | ✅ | S | Spawn rules (beta): no vanilla monsters in the Overworld, at most 8 hostile creatures per player, Mowzie's and cave creatures of Alex's Mobs only in the dark (`pack/config/incontrol/spawn.json`) |
| Mod `fallow` 0.4.10 | `mods-custom/fallow/` | ✅ (not tested with a player) | CS | What scripts cannot do: `ciel` (moon and Friendly Moon visible under ATMOSPHERICS, mist), `sac` (hose that quenches thirst), `xp` (anvil without levels, hidden bar and the bars above it lowered, [0009](decisions/0009-xp.md)), `horizon` (Distant Horizons follows the mist, [0011](decisions/0011-tectonic-streams-distant-horizons.md)), `clairiere` (vigil fire and clearing lantern lift the mist), `dissipateur` (Create kinetic block, radius 64) , `titres` (each screen's name on a plate over its top edge, client) and `outils` (wood and rock need the right tool, [first gestures](plan/premiers-gestes.md)) modules ([mod-fallow](plan/mod-fallow.md)) |

## Performance and technical

| Mod | Slug | Status | Side | Role |
|---|---|---|---|---|
| Sodium | `sodium` | ✅ | C | Rendering |
| ImmediatelyFast | `immediatelyfast` | ✅ | C | Rendering |
| ModernFix | `modernfix` | ✅ | CS | Loading |
| FerriteCore | `ferrite-core` | ✅ | CS | Memory |
| Lithium | `lithium` | ✅ | CS | Server logic |
| Structure Layout Optimizer | `structure-layout-optimizer` | ✅ | CS | Structure generation |
| Chunky | `chunky` | ✅ | CS | Pregeneration; leaves once test H11 has confirmed `/dh pregen` ([pack sort](plan/tri-du-pack.md)) |
| spark | `spark` | ✅ | CS | Profiling |
| Dynamic FPS | `dynamic-fps` | ✅ | C | The game slows down in the background |
| Neruina | `neruina` | ✅ | CS | Prevents a stuck entity from crashing the world |
| Simple Backups | `simple-backups` | ✅ | CS | Backups every hour of play, last 24 kept, plus one when everyone leaves (beta) |
| Crash Assistant | `crash-assistant` | ✅ | C | Helps read crashes |
| Sinytra Connector + Forgified Fabric API | `connector`, `forgified-fabric-api` | ✅ | CS | Run Antique Atlas and Surveyor (beta, under watch) |

## Resource packs

| Pack | Slug | Status | Role |
|---|---|---|---|
| Fresh Animations | `fresh-animations` | ✅ | Animated creatures |
| FA: Details, Objects, Emissive | `fresh-animations-details`, `fresh-animations-objects`, `fresh-animations-emissive` | ✅ | Details, animated objects, glowing eyes |
| FA: Player Extension | `fa-player-extension` | ✅ | The player in the same style |
| Motschen's Better Leaves | `better-leaves` | ✅ | Bushy foliage |
| Supernova (Original Music Pack) | `supernova-ost` | ✅ (version that does not overwrite vanilla music) | Three tracks of the music pack (CC-BY-NC-SA 4.0, [music](plan/musique.md)) |
| `fallow-interface` | — | ✅ (`pack/resourcepacks/`, [page](plan/fallow-interface.md)) | Fallow's interface, built from GUI Revision and GUI Revision Compats: vanilla screens, 14 mods' screens, HUD and Questlog journal in mist and sage, titles in French ([credits](../pack/resourcepacks/fallow-interface/CREDITS.md)) |
| `fallow-textures` | — | ✅ (`pack/resourcepacks/`, [page](plan/fallow-textures.md), added 2026-10-29, not seen in game) | Blocks and items from Vanilla Evolved (CC BY-NC-SA 4.0), without its sky, foliage colours and creatures ([credits](../pack/resourcepacks/fallow-textures/CREDITS.md), [0015](decisions/0015-textures.md)) |
| `fallow-ambiance` | — | ✅ (`pack/resourcepacks/`, [page](plan/fallow-ambiance.md)) | ATMOSPHERICS presets, Polytone colours and lights, moon texture, Particular entries for NML, sound events and 10 free tracks of the music pack ([credits](../pack/resourcepacks/fallow-ambiance/CREDITS.md)) |

## Libraries

Already present: AlfinoLib, Configurable (Neruina), Architectury API, Balm, Cloth Config, CorgiLib, Data Anchor, Fragmentum, MRU, Puzzles Lib, CodxLib, CreativeCore, Curios API, Fzzy Config, GeckoLib, Hybrid API, Kiwi, Knight Lib, Kotlin for Forge, Lithostitched, Biolith, McQoy, Modonomicon (required by Icy's Better Horses; its stable handbook has no recipe and is hidden from EMI), Moonlight Lib, Patchouli, Resourceful Config, Resourceful Lib, Rhino, YACL, Zeta, ToucanLib (Where Winds Blow).

## Dropped or rejected

| Mod | Reason |
|---|---|
| Chipped (with Athena), Beautify! | Removed on 2026-11-01: 6,980 blocks for Chipped, including 1,110 Nether and End variants; Beautify! duplicates Supplementaries, Amendments and Handcrafted ([pack sort](plan/tri-du-pack.md)) |
| Bosses of Mass Destruction, Create Ore Excavation, GraveStone Mod, Cosy Critters & Creepy Crawlies, Macaw's Roofs, Macaw's Windows, Real Camera, MB-3D Items | Removed by the team on 2026-10-19 |
| FallingTree, ItemPhysic Lite | Replaced by HT's TreeChop and ItemPhysic (team choice on 2026-10-19) |
| Entity Culling | Breaks Aeronautics contraptions; removed on 2026-10-19 |
| AA4 Atlas | Crashes the game when opening the atlas with Antique Atlas 3 (`AtlasScreen.prepareToOpen()` no longer returns the screen); removed on 2026-10-19, the atlas opens with its key |
| Clumps | Grouped XP orbs, which no longer exist ([0009](decisions/0009-xp.md)); removed on 2026-10-19 |
| Freeze It and Heat It | Removed by the team on 2026-10-18: no food preservation |
| Easy Anvils | Replaces the anvil that Overgeared needs ([12](plan/12-verification.md)) |
| Leaves Be Gone | On Aeronautics' blacklist |
| Dynamic Surroundings | Touches the sound engine like Sound Physics; duplicates |
| Dragged's Simulated Tree Chopper | "It's too much" (the team) |
| Not Enough Animations, First-person Model | Replaced by FA: Player Extension (team choice) |
| More Culling, Fog, Particle Rain | Fight with ATMOSPHERICS |
| Better Clouds | Draws the clouds in place of ATMOSPHERICS, whose clouds follow the mist, dusk and rain; removed on 2026-10-29 ([0016](decisions/0016-vent-et-lumiere.md)) |
| Cirrus, Simple Clouds, Nuit, Stellar View, Caelum | Cirrus replaces ATMOSPHERICS' fog and sky; Simple Clouds needs OpenGL 4.3 (not on macOS); ATMOSPHERICS cancels the vanilla sky, so skybox mods have no effect ([0016](decisions/0016-vent-et-lumiere.md)) |
| Vibrancy, Gleam, Colorful Lighting, Physics Mod | Lighting or physics engines that are risky with Sodium 0.8 and Sable |
| Capture Nets, Paraglider, GUI Compass, Simple Voice Chat, Hardcore Revival, Create Aeronautics Structures, Cinch's Abandoned Houses, Cattlestrophic!, Easy NPC | Removed by the team on 2026-10-18 |
| Dark Coffee GUI, Dark Coffee GUI - Extras, Modded Coffee GUI | All rights reserved and the author does not answer; replaced on 2026-10-26 by `fallow-interface`, built from free-licensed packs |
| Untitled Duck Mod | Removed by the team on 2026-10-19; Naturalist's duck replaces it ([NML fauna](plan/faune-nml.md)) |
| Traveler's Backpack, Sophisticated Backpacks | Too many upgrades and screens; Reliable Backpacks chosen on 2026-10-26 ([0012](decisions/0012-sacs-piles-jours.md)) |
| ParCool! | Disabled at the team's request |
| Sanity: Renewed, TheWatcher | The world is enigmatic, not dark |
| Jade, AppleSkin | Break the mystery |
| Just Zoom, Zoomify | The vanilla spyglass is enough |
| Wildex Bestiary | Removed by the team on 2026-10-29; the `zoo` module keeps its own ledger ([modules-fallow](plan/modules-fallow.md)) |
| Waystones, Explorer's Compass, Nature's Compass | Erase the journey |
| Carry On | Incompatible with Aeronautics |
| Separate Sable Render Distance | To evaluate with Distant Horizons (needs 1.8.7, beta) |
| Downhill Rivers, Terralith | Streams Reflowing and Tectonic chosen instead ([0011](decisions/0011-tectonic-streams-distant-horizons.md)) |
| Drip Sounds | Beta build for 1.21.8, duplicates AmbientSounds and Sounds in caves; removed on 2026-10-25 |
| Seamless Loading Screen | Last build 2024-10, Sodium overwrites the screen it draws; removed on 2026-10-25 |
| Mezz config | Orphan library (only JEI ≥ 19.55 needs it, the pack is on 19.51); removed on 2026-10-25 |
| Copycats+, Connected, Factory Logistics, Bells & Whistles, Diesel Generators, Power Loader, Prospecting Picks, Rechiseled | Too many Create addons |
| Create Villager Labor, Open Parties and Claims | Useless among friends |
| Salt's Animal Farm, Animal Genders, Kaleidoscope Tavern, Thirst Canteen, Tide, Nutritional Balance | One more gauge or activity for a role already covered |
| Reactive Music | Silence between tracks capped at 2 min, 295 MB jar, cuts vanilla music ([research](plan/recherche/2026-10-19/musique.md)) |
| Easy Magic | Replaces the vanilla table that the enchanting overhaul depends on ([research](plan/recherche/2026-10-19/enchantement.md)) |
| Better Archeology, Traveler's Titles, Create: New Beginnings, Let The Adventure Begin, MCG's RPG Campsites | Content without a strong mechanic, or breaks the mystery |
| Farmer's Delight (+ addons) | Kaleidoscope Cookery is the pack's cooking |
| Legendary Survival Overhaul | Cold Sweat and Thirst Was Reclaimed kept |
| Minimaps, waypoints | Contrary to the vision |
| Animal Husbandry, Spoiled | Judged unreliable |
| Let's Do Bakery, Meadow, Wilder Nature | Duplicates or biomes in conflict with NML |
| Graze | Makes animal husbandry self-sufficient |
| Integrated Mowzie's Mobs | Too many dependencies |
| Spawn, Environmental | Add biomes (NML conflict) |
| Prehistorum, Jurassic Saga, Jurassic Revived, Jurassic Reborn | Prehistory set aside for now |
| YUNG's Better *, Structory, Explorify, Moog's Voyager, Trek, Adorabuild, When Dungeons Arise, Dungeons and Taverns | Too many structures or RPG orientation ([0004](decisions/0004-structures.md)) |
| C2ME, Noisium, Ksyxis, FogRule | Alpha, not up to date or marginal gain ([0007](decisions/0007-ambiance-performance.md)) |
| Critters n' Crawlers | Duplicates Naturalist, horror creature |
