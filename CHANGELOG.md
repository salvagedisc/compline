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
  - tannery: animals drop a raw hide, prepared with ash then soaked on Kaleidoscope's bamboo tray ([tannery](docs/plan/tannerie.md));
  - no more experience: anvil without levels (wear), hidden bar, no orbs, vials and nuggets removed ([0009](docs/decisions/0009-xp.md)).
- Mod `fallow` 0.2.0: `xp` module.
- Music: True Adaptive Music and the `fallow` pack (`pack/trueadaptivemusicpacks/fallow/`): 11 situations (mist, clearing, seasons, moon, airship, underground, monument…), long silences, muted during bosses; 29 tracks chosen by the team: vanilla and No Man's Land, the Supernova resource pack, 10 free tracks by Scott Buckley, Kevin MacLeod and HoliznaCC0 with their credits; `musique.js` script, `fallow:brumeux` tag, TAM interface in French, pack active and music at 40% on first launch ([0010](docs/decisions/0010-musique.md)).
- Rendering without shaders: coloured light from fires, souls and moonlight (Polytone), moon texture in the style of ATMOSPHERICS' sun, Contraption Lights, Wakes Reforged, Puddles & Floods, Rainbows, Better Clouds; "Fancy" graphics on first launch ([fallow-ambiance](docs/plan/fallow-ambiance.md)).
- First-person gestures: Punchy! 2.8g, set to a restrained level, with EMF configured so that it alone animates the arm.
- Lamb Lanterns: a lantern carried at the hip gives light.
- Interface: Dark Coffee GUI and Modded Coffee GUI packs, active on first launch. Hunger bar from Dark Coffee GUI - Extras (Normal Hunger) in place of the coffee beans.

### Removed
- Untitled Duck Mod (team choice): the Naturalist duck returns, and the No Man's Land goose's offering line now targets that duck.
- AA4 Atlas: compiled for Antique Atlas 2, it called `AtlasScreen.prepareToOpen()` with the old return type and crashed the game when opening the atlas (crash report from testing). The atlas opens with the Antique Atlas key.
- Entity Culling: breaks Create Aeronautics and Sable contraptions.
- Clumps: experience orbs no longer exist.

### Changed
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
- Kaleidoscope Cookery 1.5.1 → 1.6.0: 1.5.1 crashed the game on loading with Kaleidoscope Compat 2.9.7 (`MillstoneBlockEntityMixin` mixin), observed on the test server.

### Fixed
- No Man's Land: the scutes offering line never loaded (condition on a non-existent item, `nomansland:tortoise_scute`).
