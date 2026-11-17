# 0034 — Fishing with Starcatcher

- **Date**: 2026-11-17
- **Status**: kept (the team's choice of 2026-11-17); checked on the test bench (dedicated server and client of the full pack), not played

## Context
Cooking became a gesture with Farmer's Delight and Hands-On Delight ([0030](0030-farmers-delight-kitchen.md)), forging a minigame with Overgeared. Fishing was still vanilla: cast, wait, click. Hybrid Aquatic, already in the pack, adds things around fishing (hooks held in the off hand, crates, crab pots, messages in bottles) but not to the act itself, and its loot modifier swaps half of the vanilla catches for a Hybrid Aquatic fish drawn at random, with no regard to biome or fresh and salt water: a piranha or a deep-sea anglerfish could come out of a pond.

## Options
Read in the jars on 2026-11-17, all for NeoForge 1.21.1:

| Criterion | A. Hybrid Aquatic alone | B. Tide 2.1.1 | C. Starcatcher 3.3.10 |
|---|---|---|---|
| Act of fishing | Vanilla | Timing minigame per fish | Skill minigame per fish, with gimmicks (vanishing, moving, freezing sweet spots) |
| Where fish bite | Anywhere | Biome, salt water, depth, temperature, season | Biome, depth, time of day, weather, light, bait, structure |
| Collection | None | Journal | Catalogue, trophies, aquarium, plaques, tournaments |
| Rod | Vanilla + off-hand hook | Angling table: hook, line, bobber | Rod with hook, bobber and bait, tackle box, rod skins |
| Hybrid Aquatic | — | 50 fish built in | 64 entries through an addon (Hybrid Aquatic + Starcatcher Compatibility) |
| Licence | All Rights Reserved | MPL-2.0 | Custom (free except for Fabric ports); referenced on Modrinth, never redistributed |

## Decision
Option C (the team, 2026-11-17: "very complete, customisable and polished"). Starcatcher 3.3.10, Hybrid Aquatic + Starcatcher Compatibility 0.6.3, Starcatcher's Delight 3.2 (fish dishes in Farmer's Delight's pot and on its board).

Fitted to Compline:

- **One rod**: the vanilla fishing rod merges into Starcatcher's (`fusions.js`): its recipe leaves, the carrot and the warped fungus on a stick ask for Starcatcher's rod, loot gives it. Starcatcher's rod needs a hook (iron) and a bobber (planks, wool). Hybrid Aquatic's hooks go on it through the addon.
- **Catches fitted to the world** (`scripts/fishing/starcatcher_entries.py`, which writes `pack/kubejs/data/*/starcatcher/fish/`):
  - the End catches (10) are removed, since the End no longer exists ([0004](0004-structures.md));
  - the Nether lava catches (13, three of them rod-skin templates) are fished in the Overworld's lava below y = -40, like the dinosaurs of the closed dimensions moved underground;
  - one model per species ([duplicates](../plan/doublons.md)): Starcatcher's stonefish and sea bass leave for Hybrid Aquatic's; Hybrid Aquatic's anglerfish, blobfish and piranha give Naturalist's; its stingray, ocean sunfish and seahorse, cut for other mods' models without a fish item, are no longer fished;
  - three fish of the pack that no entry knew become catches: Naturalist's bass (rivers, forests, swamps) and catfish (rivers and swamps, at night), Spawn's bluefish (open ocean, beaches).
- **Biomes**: `scripts/biomes/biome_rules.py` gives Starcatcher's biome tags that list only vanilla biomes (cold lakes, warm ocean, rivers…) the Regions Unexplored, Hybrid Aquatic and Spawn biomes that resemble them.
- **Bare hands at the start** ([first steps](../plan/premiers-gestes.md)): no rod and no guide given on joining (`config/starcatcher-server.toml`, `give_rod` and `give_guide` off); both are crafted. Bait is worms, from bone meal on farmland.
- **Leather only through the tannery**: the fished old boot no longer turns into leather (`tannerie.js`).
- **No money**: the selling bin (wd's Selling Bin, nested in Starcatcher) has no currency set and is removed (`tri.js`).
- **Cooking**: Starcatcher's Delight also ships Farm & Charm pot recipes; `cuisine.js` no longer copies them, since the Farmer's Delight pot already has every dish.
- **French**: 367 texts translated (Starcatcher's Delight 206, Selling Bin 48, the addon 43, LibTooltips 23, Starcatcher 21, Fancy Tab Sections 13, Tiny Multiblock Lib 13); Starcatcher ships its own French for the rest.

## Rejected
- **Tide 2**: open licence and built-in compatibility with Hybrid Aquatic and Ecliptic Seasons, but less content; recommended at first, the team preferred Starcatcher.
- Aquaculture 2 (no minigame, duplicated fish), Fish of Thieves (creatures in double, Sea of Thieves look), Fisherman's Haven (generic).
- [Let's Do] Lili's Lucky Lures: passive traps and fish pools, possible later once checked next to Starcatcher.

## Conditions and tests
- **Checked on the test bench on 2026-11-17** (dedicated server and client of the full pack, offline player): the server starts with the 17 KubeJS server scripts without error; 224 recipes merged (the carrot on a stick takes Starcatcher's rod), no recipe left for the vanilla rod, the selling bin, the old boot to leather or the Farm & Charm copies of the fish dishes. Fishing a river: the minigame opens on a bite (wheel, sweet spots, Space); with the minigame off, 30 casts gave Hybrid Aquatic's sunfish and Starcatcher's river fish (driftfin, obidontiee, elderscale, silverveil perch). The catalogue opens in French, lists Naturalist's bass (« Bar cru ») among the fish of the area and announces Spawn's bluefish (« Tassergal ») and Naturalist's anglerfish (« Baudroie »); the End entries, the stingray, the ocean sunfish and the seahorse no longer exist in the registry. Deep lava (y = -50) with a rod that survives lava: 10 casts gave cerberay, embergill, magma fish, scalding pike and willish (moved from the Nether) and a molten deepslate crab. Screenshots in the project files (`peche-2026-11-17/`).
- Lava fishing needs a rod that survives lava: Starcatcher's amethyst hook (amethyst is found in the Overworld) or a netherite-upgraded rod. In ten casts, two legendary cerberays: the deep lava has few entries, as the Nether had; to watch in game.
- Not seen with a real player: winning the minigame by hand (the bench runs at about two frames per second), the aquarium, the plaques, the tackle box, the tournaments, the Hybrid Aquatic hooks on the rod (tests PE1 to PE6 in [tests-en-jeu.md](../tests-en-jeu.md)).
- The addon still lists the removed stingray, ocean sunfish and seahorse in its treasure data map: three harmless lines in the server log at start-up.
- The guidebook's headings (« All about fishing », « Fishes in the area », « All fishes ») are drawn in its textures and stay in English.
- Seasons (Ecliptic Seasons) do not change catches: Starcatcher has no season condition.
