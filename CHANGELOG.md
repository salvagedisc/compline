# Changelog

Format: [Keep a Changelog](https://keepachangelog.com/fr/1.1.0/). Versions: `0.x` prototype, `1.0.0` launch of the permanent world.

## [Unreleased]

No version has been published yet: everything below will make up `0.1.0`.

### Added
- Base pack: No Man's Land, Overgeared, Create, Create Aeronautics + Sable, Steam 'n' Rails, Kaleidoscope Cookery, Antique Atlas 4 + Surveyor, Cold Sweat, Thirst Was Reclaimed, KubeJS.
- Farming: [Let's Do] Farm & Charm (+ Architectury API), Ecliptic Seasons, KaleidoscopeCookery-Automation.
- Fauna: Naturalist, Alex's Mobs Continued, Hybrid Aquatic, Mowzie's Mobs, Critters and Companions.
- Companions: Untitled Duck Mod, Friends & Foes, Adorable Hamster Pets, Companion, Companions!, Ghost.
- Structures: Sparse Structures.
- QoL / building: Quark and Supplementaries, reduced to the essentials (configs provided).
- Travel: Icy's Better Horses.
- Atmosphere: ATMOSPHERICS, AmbientSounds, Sound Physics Remastered, Subtle Effects, LambDynamicLights, More Darkness.
- Performance: Sodium, ImmediatelyFast, Entity Culling, ModernFix, FerriteCore, Lithium, Structure Layout Optimizer; server: Chunky, spark.
- Team choices of 2026-10-19:
  - world and goals: Ancient Remnants: Monoliths (+ Fragmentum), Lootr, Enhanced Celestials, AA4 Atlas, Wildex Bestiary, Exposure;
  - daily life and fauna: Kaleidoscope Compat, Traveler's Backpack, Longwings;
  - building: Amendments, Handcrafted, Chipped, Beautify!, Twigs, Every Compat, HT's TreeChop, Visual Workbench;
  - body and camera: Entity Model Features, Entity Texture Features, 3D Skin Layers, Shoulder Surfing Reloaded (a single over-the-shoulder view, free camera), Spatial GUI, Camera Overhaul (set low), ItemPhysic, Smooth Swapping;
  - rendering and sound: Polytone, Particular Reforged, Sounds, Auroras, Drip Sounds; Fresh Animations packs (+ Details, Objects, Emissive, Player Extension) and Motschen's Better Leaves, active on first launch through Default Options;
  - interface: EMI, JEI (lists hidden), Pick Up Notifier, Seamless Loading Screen, Mouse Tweaks, Controlling + Searchables, Default Options, Chat Heads;
  - scripts: KubeJS Create, LootJS, MoreJS, In Control!.
- Enchanting: Immersive Enchanting (+ AlfinoLib), the mod's structures disabled, French translation ([0008](docs/decisions/0008-enchantement.md)).
- Tooling: `npm run sync` brings the mods of the test instance back into the repository, `npm run check-deps` checks the real dependencies of the jars; `build` CI (`.mrpack` export on every push) and `release` (on tag).
- Custom mod `fallow` 0.1.0 (`mods-custom/fallow/`, jar in `pack/mods/`): sky module (Enhanced Celestials moon visible under ATMOSPHERICS, mist cache cleared after a biome change) and bag module (the Traveler's Backpack hose quenches thirst in Thirst Was Reclaimed). Not tested in game; the `build` CI compiles it.
- Everything in French: game in French on first launch (Default Options); translation of the missing texts of 94 mod namespaces (≈25,300 texts) and of their books (Alex's Mobs dictionary, hamster guide, Ecliptic Seasons chronicle) in `pack/kubejs/assets/`; `npm run check-fr` (also run in CI) rejects an untranslated text or a lost variable ([translation](docs/plan/traduction.md)).
- Ideas taken from Wayfarer, the No Man's Land team's modpack ([research](docs/plan/recherche/2026-10-19/wayfarer.md)):
  - Envelope (mail by pigeon, lost letters), translated into French;
  - the Friendly Moon of No Man's Land as a main thread: crumpled note in the alchemists' jars, a single moon at a time with Enhanced Celestials, blood moons removed ([friendly moon](docs/plan/lune-amie.md));
  - maximum health: one heart lost per death, floor of 5 hearts, life shards to find by exploring ([health](docs/plan/sante.md));
  - tannery: animals drop a hide instead of leather ([tannery](docs/plan/tannerie.md)), made with the Tannery mod since 2026-10-29 (see Changed);
  - no more experience: anvil without levels (wear), hidden bar, no orbs, vials and nuggets removed ([0009](docs/decisions/0009-xp.md)).
- Mod `fallow` 0.2.0: `xp` module.
- Music: True Adaptive Music and the `fallow` pack (`pack/trueadaptivemusicpacks/fallow/`): 11 situations (mist, clearing, seasons, moon, airship, underground, monument…), long silences, muted during bosses; 29 tracks chosen by the team: vanilla and No Man's Land, the Supernova resource pack, 10 free tracks by Scott Buckley, Kevin MacLeod and HoliznaCC0 with their credits; `musique.js` script, `fallow:brumeux` tag, TAM interface in French, pack active and music at 40% on first launch ([0010](docs/decisions/0010-musique.md)).
- Rendering without shaders: coloured light from fires, souls and moonlight (Polytone), moon texture in the style of ATMOSPHERICS' sun, Contraption Lights, Wakes Reforged, Puddles & Floods, Rainbows, Better Clouds; "Fancy" graphics on first launch ([fallow-ambiance](docs/plan/fallow-ambiance.md)).
- First-person gestures: Punchy! 2.8g, set to a restrained level, with EMF configured so that it alone animates the arm.
- Lamb Lanterns: a lantern carried at the hip gives light.
- Terrain: Tectonic and Streams Reflowing under No Man's Land's biomes; Monoliths protected from streams ([0011](docs/decisions/0011-tectonic-streams-distant-horizons.md)).
- Far view: Distant Horizons, hidden by the mist and revealed in clearings by the new `horizon` module of the `fallow` mod 0.3.0, which also fixes two Distant Horizons 3.3.3 crashes (world creation with Chunky, Apple Silicon Macs); French translation of Distant Horizons and Tectonic ([0011](docs/decisions/0011-tectonic-streams-distant-horizons.md)).
- Safety net for the permanent world: Simple Backups (one backup per hour of play, last 24 kept), Neruina (+ Configurable: a crashing entity is stopped instead of the world), Crash Assistant, Dynamic FPS.
- End removed and Nether closed: no ender eyes, no end crystals, Nether portals do not light, no travel to either dimension, no strongholds ([0004](docs/decisions/0004-structures.md)).
- In Control!: at most 8 hostile creatures per player; Mowzie's Mobs and the cave creatures of Alex's Mobs only in the dark.
- Clearings, the mist that lifts: `fallow` 0.4.0 adds the vigil fire (radius 16), the clearing lantern (32) and the dissipator, a Create kinetic block (64, 32 RPM, 8 stress units per RPM). They turn the biomes around them into `fallow:clairiere` (cold and warm variants) for good: light mist, vivid grass, clearing music, far view. Operators: `/fallow clairiere <radius>`.
- Ideas of the review of 2026-10-25: Cold Sweat: Altitude, Antique Transport, Create Aeronautics: Automated Logistics, Sable: Physics Compat, Aeronautics Wind Sound, Exposure: Space, all translated into French.
- Team choices of 2026-10-26 ([0012](docs/decisions/0012-sacs-piles-jours.md)): Reliable Backpacks (a single bag on the back), Comforts (sleeping bag and hammock), Sensible Stackables with the `fallow:pile/N` tags (stacks of 64, 32, 16, 8 or 4 by kind of item), Better Days (30 minutes of day, 15 of night); all translated into French.
- Questlog: a book-style journal of quests and guides in French, four chapters and 16 quests that appear as the player progresses; its interface translated ([quetes](docs/plan/quetes.md)). It replaces the Modonomicon notebook of the same day.
- Specification of the eight next modules of the `fallow` mod (`docs/plan/modules-fallow.md`) and the research of 2026-10-26 (`docs/plan/recherche/2026-10-26/`).

- Interface: `fallow-interface` pack, built by `scripts/interface/build.py` from GUI Revision and GUI Revision Compats (CC BY-NC-SA 4.0), keeping GUI Revision's panels in relief, trays and corner ornaments: vanilla screens, the screens of 14 mods and the HUD in Fallow's mist, with a family of colours per kind of screen (wood, forge, moss, dusk, the End's purple) at the same brightness so that the relief stays, wood and purple screens brought back to its greys first; Fallow's details over them (`details.py`): one leaf pattern under the slots, rounded slots without cast shadow, tiled wells behind the player and the mounts, the crafting table's frame on the other crafting grids, a slim fletched arrow and a slim plus everywhere (JEI and EMI included), progress in lantern light; a hotbar with rounded ends, buttons, tabs, lists and toasts in the same tones with highlights in lantern light, the crafter's screen and the widgets GUI Revision lacks added from the game's textures, vanilla-grey screens of 5 other mods whose licence allows it recoloured, only the textures the game and the mods read, a pack icon, the menus' shading and separators in the same tones and a title screen panorama of a clearing in the mist (three variants drawn by `panorama.py`), widgets redrawn as sage plaques and moss wells, Questlog journal drawn in the same style ([page](docs/plan/fallow-interface.md)).
- World textures: `fallow-textures` pack, built by `scripts/textures/build.py` from Vanilla Evolved 1.17.0 by Vixel (CC BY-NC-SA 4.0), the artist of GUI Revision: blocks, items, particles, chests and shulker boxes, without its sky, foliage colour map, creature textures, language file and OptiFine files; enabled on first launch under every other pack of ours ([0015](docs/decisions/0015-textures.md), [page](docs/plan/fallow-textures.md)).
- Title and loading screens: FancyMenu and Drippy Loading Screen (+ Konkrete, Melody), client only, with the Fallow logo on the title screen and a moss loading screen with a lantern-coloured bar ([page](docs/plan/fallow-interface.md)).
- Screen names: `titres` module of the `fallow` mod 0.4.4, each container screen's name centred over its top edge on GUI Revision's plate, in its engraved lettering completed for French, in the screen's colours; it follows the game's language, a renamed chest and the trader's level; the look comes from the interface pack ([page](docs/plan/fallow-interface.md), [module](docs/plan/mod-fallow.md#titres-module)). Corner ornaments of four GUI Revision families on every panel, never under a widget or the plate; selected tabs and choices in lantern light, the advancements' three frame shapes, blinking heart containers, empty-slot placeholders and the game's icons in Fallow's tones, a plain moss veil behind the menus and riveted buttons filled with lantern light on hover.
- HUD without the XP bar: `fallow` 0.4.1 lowers hearts, armor, hunger, air and thirst by 5 pixels into the space the bar left (not on a horse, where the jump meter takes it); Cold Sweat's body temperature icon follows through its client config.
- First gestures: `outils` module of the `fallow` mod, wood (raw or worked: planks, furniture, chests) and rock no longer break by hand, plants and crops still do (a hint says which tool is needed); wooden tools removed, a log gives 2 planks with an axe in the grid (the axe wears), the campfire is lit with flint, a Twigs pebble becomes an Overgeared knappable rock, leaves drop a few more sticks; « Premiers gestes » Questlog chapter, 10 quests ([page](docs/plan/premiers-gestes.md), [0013](docs/decisions/0013-premiers-gestes.md)).
- Wind and light without shaders ([0016](docs/decisions/0016-vent-et-lumiere.md)): Where Winds Blow (+ ToucanLib), grass and leaves that sway, slanted rain, drifting snow and smoke, its own fog turned off for ATMOSPHERICS; Shine, bloom and coloured light. Both translated into French.

### Removed
- Icy's Better Horses' stable handbook (Modonomicon): no recipe, hidden from EMI; Modonomicon stays only because the mod requires it. Its essentials move to a new Questlog chapter, Voyager (two guides).
- Wildex Bestiary (team choice of 2026-10-29), with its config and its translation: the `zoo` module will keep its own ledger of species.
- The `fallow-ambiance` moon texture (team's choice of 2026-10-29): the vanilla moon is back.
- Better Clouds: it drew the clouds in place of ATMOSPHERICS, so they ignored the mist, dusk and rain ([0016](docs/decisions/0016-vent-et-lumiere.md)).
- Hollow GUI's curls at the top of the panels, lozenges and grooves added to the screens, and the leaf pattern behind the menus (review of 2026-10-26: back to GUI Revision's own drawing).
- Dark Coffee GUI, Dark Coffee GUI - Extras and Modded Coffee GUI (all rights reserved, author unreachable): replaced by `fallow-interface`.
- Traveler's Backpack (team choice of 2026-10-26: too complicated), with its server config and its translation; the `sac` module of the `fallow` mod switches itself off and goes in 0.5.0.
- Drip Sounds (beta build for 1.21.8, duplicates AmbientSounds and Sounds) and Seamless Loading Screen (unmaintained, overwritten by Sodium).
- Untitled Duck Mod (team choice): the Naturalist duck returns, and the No Man's Land goose's offering line now targets that duck.
- AA4 Atlas: compiled for Antique Atlas 2, it called `AtlasScreen.prepareToOpen()` with the old return type and crashed the game when opening the atlas (crash report from testing). The atlas opens with the Antique Atlas key.
- Entity Culling: breaks Create Aeronautics and Sable contraptions.
- Clumps: experience orbs no longer exist.
- MezzConfig: orphan library, required only by JEI versions newer than the pack's.

### Changed
- ATMOSPHERICS preset `fallow_v4`: a second, higher and thinner cloud layer; clouds take the colour of the mist, darken at night and blend into the horizon; sun and moon 1.35× (1.1 before); more, brighter and larger stars; its wind streaks and tumbleweeds off, Where Winds Blow owns the wind ([0016](docs/decisions/0016-vent-et-lumiere.md)).
- Tannery: the scripted tannery (raw hide, ashes, bamboo tray) is replaced by the Tannery: Better Leather mod 1.0.2: every leather animal drops one of its five hides, which dry on a tanning rack and turn into leather in a tanning barrel with tannic powder from bark; every strippable log gives bark; the vanilla tables it overrides are put back; satchels removed; translated into French ([0014](docs/decisions/0014-tannery.md)).
- Documentation brought up to date on 2026-10-25: feature overview in the README, vision rewritten around the mist that lifts and the long-term goals, roadmap with the updates held until the tests, every in-game test gathered by play session in `docs/tests-en-jeu.md`.
- Low-risk updates: Sable 2.0.6, Quark 4.1-487, Moonlight 3.7.2, No Man's Land 1.6.3, Ecliptic Seasons 0.15.5, Contraption Lights 1.5.5, CreativeCore 2.13.51, ModernFix 5.27.27, ImmediatelyFast 1.6.15, Knight Lib 2.0.5, Companions! 1.3.6, Hybrid Aquatic 1.7.4 and Hybrid API 1.1.3, Naturalist 2.0.7, Alex's Mobs Continued 2.2.4, Traveler's Backpack 10.1.40, AmbientSounds 6.3.11, Lithostitched 1.8.1, Puzzles Lib, Modonomicon, Forgified Fabric API, Every Compat, Subtle Effects 1.15.1, Sound Physics Remastered 1.5.2; NeoForge 21.1.265.
- Ecliptic Seasons set to farming only (`AGRICULTURE`), heat stroke and chat notices off.
- Ecliptic Seasons leaves the day length to Better Days (`DynamicDaylightDuration = false`), 3 days per term instead of 7 so that a term still lasts about 2 h 15.
- Friends & Foes: Iceologer, Illusioner, Wildfire, their structures and the zombie horse trap disabled by config; fewer Maulers, crabs and Mooblooms.
- Adorable Hamster Pets: throwing, item stealing, diamond sniffing and redstone fever disabled.
- Companions! and Mowzie's Mobs: natural spawns rarer.
- Quark: variant animal textures off (No Man's Land and Fresh Animations own them).
- No Man's Land fauna: Mixed Litter's remodels and NML's textures on vanilla animals are disabled, for Fresh Animations; deer, goose, _bar à serpe_ (a fish), turtle and Frienderman no longer spawn, are removed from the Wildex, and their 15 items no longer have a recipe or a place in EMI ([NML fauna](docs/plan/faune-nml.md)).
- No Man's Land, items removed: horse meat, frog legs, explosives, fire arrow and seven dishes; TNT goes back to its vanilla recipe. The rabbit is removed from the game (Fresh Animations has no model for it). The turtle's offering line targets the Naturalist one.
- Fauna: a single model per species. 32 duplicates compared; 17 Alex's Mobs creatures, 9 from Hybrid Aquatic, 2 from Critters and Companions and 6 from Naturalist are disabled, removed from the Wildex, and their 78 sourceless items removed ([duplicates](docs/plan/doublons.md)).
- Fauna removed by decision of the plan: Murmur, Farseer, Skelewag, Alex's Mobs fly and cockroach, Friends & Foes iceologer, illusioner and wildfire with their structures, Naturalist mammoth ([duplicates](docs/plan/doublons.md)).
- Hybrid Aquatic: its tropical river is disabled (it replaced part of the vanilla rivers); its underwater biomes remain ([duplicates](docs/plan/doublons.md)).
- Supplementaries: its mob-head vision filter is disabled, Subtle Effects already does the same.
- Vanilla monsters: In Control! prevents them from spawning in the Overworld (22 creatures, from zombies to pillagers), including No Man's Land biomes and structures; they still spawned during the first test.
- Enhanced Celestials 6.0.2.6 → Enhanced Celestials 2 (Core 2.0.3.3 + Default Lunar Events 2.0.1.3): new Java packages and new data format; `lune.js`, `musique.js` and the `ciel` module of the `fallow` mod (0.2.1) ported, blood moons removed in the new format, French translations, download of community translations disabled.
- JEI 19.69.0.456 (beta) → 19.51.0.418: since 19.52, JEI sends the recipe transfer from a different method; the mandatory mixin of Envelope 0.7.5 no longer finds its target and the game crashed when entering a world (crash report from the first test). 46 more JEI texts translated.
- Repository reorganised: `README` and `CONTRIBUTING` rewritten, documentation index, roadmap, `docs/plan/` location for the modpack plan, `docs/` renamed `docs/conception/`, mod registry sorted by domain.
- Branches `test/agriculture`, `test/faune` and `test/structures` merged into `main`.
- No Man's Land 1.5.12 → 1.6.1: ancestral tools (ritual pickaxe, oath sword, bronze bell and chisel), lunar and solar tiles; new texts translated.
- Vigil fire and clearing lantern (`fallow` 0.4.12): their own models and textures (a stone brazier with a pale flame, a standing lantern of blackened iron around a glowing quartzite crystal) and new recipes (resin oil bottle and polished stone; quartzite cluster, glass panes and iron plates). They looked like the vanilla campfire and lantern.
- ATMOSPHERICS preset `fallow_v3`: the mist of the clearings ends at 500 blocks by day and 260 at night (124 and 84 before), so that the far landscape can be seen there.
- Duplicate `docs/plans/` and `docs/specs/` removed (copies of `docs/conception/`).
- Kaleidoscope Cookery 1.5.1 → 1.6.0: 1.5.1 crashed the game on loading with Kaleidoscope Compat 2.9.7 (`MillstoneBlockEntityMixin` mixin), observed on the test server.

### Fixed
- The Friendly Moon never appeared at the Meeting Point, nor its shadow in the sky after the dream: ATMOSPHERICS gives the sky back to No Man's Land only once the Friendly Moon is visible, which only No Man's Land's own sky can make happen. The `ciel` module of `fallow` 0.4.10 draws them after ATMOSPHERICS' sky until it hands over, and leaves the sky to the moonlight dream ([module](docs/plan/mod-fallow.md#the-friendly-moon-under-atmospherics)).
- With Distant Horizons, the mist of a clearing never closed: far mountains drowned in the mist stayed visible as pale silhouettes against the sky up to the end of the LOD radius. The `horizon` module of `fallow` 0.4.11 dissolves the far landscape into the sky at the end of the mist, capped by the new client option `horizon.ringMaxDistance` (512 blocks), so a mist ring always remains. It also skips Distant Horizons' terrain-to-LOD fade when LODs are not drawn, which could blend a frozen LOD image into the mist ([module](docs/plan/mod-fallow.md#horizon-module)).
- Breaking a vigil fire, a clearing lantern or a charged dissipator left its clearing in place. With `fallow` 0.4.12 the mist comes back over its disc, except where another source still holds it; `/fallow brume <radius>` does the same for operators ([module](docs/plan/mod-fallow.md#clairiere-module)).
- Placing or breaking a clearing source changed the mist and the grass at once. With `fallow` 0.4.12 the clearing spreads ring by ring from the source (and shrinks from the edge when it is removed), with cloud puffs, and the mist thins or thickens over several seconds.
- No Man's Land: the scutes offering line never loaded (condition on a non-existent item, `nomansland:tortoise_scute`).
