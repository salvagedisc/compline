# 0012 — Bags, stack sizes and day length

- **Date**: 2026-10-26
- **Status**: kept (the team's choices of 2026-10-26); nothing seen in game yet
- **Supersedes**: the Traveler's Backpack line of the team's choices of 2026-10-19, and the "Comforts duplicates the sleeping bag" rejection

## Context
The team asked for four things on 2026-10-26: simpler bags than Traveler's Backpack (and not Sophisticated Backpacks), stack sizes that depend on the kind of item ("64 is too much"), days longer than nights (30 min / 15 min, or 1 h / 30 min), and a comparison of Ecliptic Seasons with Serene Seasons. Research: [piles-et-sacs](../plan/recherche/2026-10-26/piles-et-sacs.md) and [saisons-et-duree-des-jours](../plan/recherche/2026-10-26/saisons-et-duree-des-jours.md); jars of Reliable Backpacks 1.9.1, Sensible Stackables 3.1.2, Better Days 3.3.6.3 and Ecliptic Seasons 0.15.5 read with `javap`.

## Decision

**Bags: Reliable Backpacks** replaces Traveler's Backpack. One bag, three rows, dyeable, placeable as a block, worn in the Curios "back" slot. `pack/config/reliable_backpacks.json`: `enableChestSlot = false` (the bag never takes the chestplate slot), `enableEntityStealing = false`. The default recipe (8 leather) is kept: leather comes from the tannery.
- **Comforts** comes back for the sleeping bag and the hammock, which Traveler's Backpack used to bring.
- Lost with Traveler's Backpack: the hose and its tanks. The `sac` module of the `fallow` mod switches itself off without the mod ("Module sac ignoré") and is removed in 0.5.0. Water on the road is carried in bottles, which now stack to 8.

**Stacks: Sensible Stackables.** `pack/config/sensible_stackables/config.toml`: every item that stacked to 64 now stacks to 32 (`commonStackSize`), and the `fallow:pile/N` item tags (`pack/kubejs/data/fallow/tags/item/pile/`) set the exceptions. Items with durability are never touched; the mod syncs the client.

| Tag | Stack | What |
|---|---|---|
| `fallow:pile/64` | 64 | Small things: seeds, nuggets, dyes, string, sticks, powders, feathers, flint, paper |
| (common size) | 32 | Everything else that stacked to 64: building blocks, planks, crops, torches, arrows |
| `fallow:pile/16` | 16 | Ingots, gems, raw metal, ores, logs, glass, wool, cooked meat, fish and bread, empty bottles, Create parts (shafts, gears, casings, pipes, mechanisms) |
| `fallow:pile/8` | 8 | Chests, barrels, storage blocks, workbench and furnaces, potions and water bottles, honey bottles |
| `fallow:pile/4` | 4 | Create machines, vaults and tanks |

Raw building blocks (stone, dirt, sand) stay at 32 so that building is not a chore. Each stack stays at least as large as the biggest recipe output of its item (Kaleidoscope Cookery: 9 dishes, Farm & Charm: 16 fences, Create: 16 panes, 18 nuggets).

**Day length: Better Days, 30 minutes of day and 15 of night.** `pack/config/betterdays-common.toml`: `daySpeed = 0.3611`, `nightSpeed = 0.6111` (13,000 ticks of day in 30 min, 11,000 of night in 15 min). Only the clock moves: crops, furnaces and Create keep their pace. Better Days' own sleep is off, vanilla sleep stays. The values for 1 h / 30 min are in the file's comments.

**Seasons: Ecliptic Seasons is kept.** Serene Seasons does not change the length of days, has no greenhouse and no gradual snow; it stays the fallback. Ecliptic must leave the day length to Better Days: `DynamicDaylightDuration = false`. `LastingDaysOfEachTerm` goes from 7 to 3 so that a term still lasts about 2 h 15 of play.

## Rejected
- Sophisticated Backpacks, Traveler's Backpack: too many upgrades and screens (the team).
- Quark's backpack (+ Quark Backpack Plus): needs Quark's Oddities, a single-author add-on; fallback.
- Packed Up, L2 Backpack, Backpacked: tiers, upgrades or experience.
- Stack Config, Upstack, Stack Size Tweaks: fallbacks for Sensible Stackables.
- A KubeJS clock (`doDaylightCycle false` + script): no smooth sky and no sleep; kept in reserve.
- Better Days' `REALTIME` mode: 12 real hours of day and 12 of night, not tied to the server's clock.

## Conditions and tests
The layout of `sensible_stackables/config.toml` was checked on a test server on 2026-10-26: `items` is a list of `[tag, size]` pairs under `version = 2` (a `[items]` table is rejected and the defaults are used). The layout of `reliable_backpacks.json` was read in the jar: compare it with the file generated on first launch.

Bags:
- B1. Craft from 8 tanned leather; the tannery sets the pace.
- B2. Worn on the back in the Curios slot, never in the chestplate slot; seen with Fresh Animations: Player Extension, 3D Skin Layers and Shoulder Surfing.
- B3. Placed on the ground, then on an Aeronautics airship in flight.
- B4. Comforts' sleeping bag: the spawn point does not move; Cold Sweat in the snow.

Stacks:
- P1. The table above, item by item, in EMI and in the inventory.
- P2. Recipes with large outputs (Kaleidoscope pot, Create crusher, Farm & Charm fences) empty without loss.
- P3. Create vault and Frogport packages with the smaller stacks (`vaultCapacity` to raise if needed).
- P4. Hoppers, Overgeared furnace and anvil outputs.
- P5. An item found in no tag stacks to 32; a tool or armour never stacks.

Day length:
- J1. A day lasts 30 min and a night 15 min, measured; the sky moves smoothly in multiplayer.
- J2. The WhiteNoise sync error (Better Days #79) when a client joins the server.
- J3. Sleep skips the night, and Ecliptic counts the day (Better Days #76: its author does not support Ecliptic).
- J4. The Enhanced Celestials moon starts at dusk; the True Adaptive Music night nodes start with the night.
- J5. Fallback if Better Days misbehaves: a single-speed mod (Extended Time, Day Length) or a KubeJS clock.
