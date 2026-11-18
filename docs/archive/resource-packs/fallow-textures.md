# fallow-textures

> **Archived on 2026-11-13.** The maintainer removed the three house resource packs (`fallow-interface`, `fallow-ambiance`, `fallow-textures`) from the pack ([0024](../../decisions/0024-removing-the-house-resource-packs.md)). This page records what they contained and how the mods read them, should any part come back.

The world's blocks and items, from **Vanilla Evolved** by Vixel, the artist behind GUI Revision (the base of [fallow-interface](fallow-interface.md)). Chosen by the team on 2026-10-29 ([decision 0015](../../decisions/0015-textures.md)) after a comparison of the full texture packs whose licence allows a modified copy, with previews rendered from their 1.21.1 files.

## What the pack holds

`pack/resourcepacks/fallow-textures/` is written by `scripts/textures/build.py`, which downloads Vanilla Evolved 1.17.0 (the release for 1.21 and 1.21.1), checks its hash and keeps 1,404 of its 1,650 files: 671 block textures, 257 item textures, 259 block and item models, 29 blockstates (random rotations and variants of grass, flowers, mushrooms, dirt, sand), particles, armour trims, map, chests, chest boats and shulker boxes.

Left out on purpose, so that the pack does not undo earlier choices:

| Left out | Why |
|---|---|
| Sun, moon, rain, snow | The moon stays vanilla (team's choice of 2026-10-29); ATMOSPHERICS draws the sun above any pack; rain and snow stay as Where Winds Blow expects them |
| Foliage colour map | Grass and foliage colours come from Polytone, biome by biome ([fallow-ambiance](fallow-ambiance.md)) |
| Creature textures | Fresh Animations is drawn on the vanilla ones |
| English language file | It only renamed one flower |
| OptiFine files (connected textures, emissive maps, custom items, map colours) | No mod of the pack reads them |
| `models/block/observer_on.json` | Its parent model is missing from the release: a powered observer would show the missing-model texture |

## Order of the packs

Default Options enables it on first launch, **lowest of the pack's own packs**: right above the mods' resources, under Better Leaves, Fresh Animations and its extensions, `fallow-interface` and `fallow-ambiance`. Better Leaves keeps its bushy leaves, Fresh Animations Objects keeps its animated objects, and the interface stays ours.

## Licence

CC BY-NC-SA 4.0, like Vanilla Evolved and `fallow-interface`. The pack cannot be used commercially; the credits are in `CREDITS.md` inside the pack. The two packs could be merged one day without a licence problem.

## Next steps

1. **Mod blocks.** About 25,000 block and item textures of 48 mods keep their vanilla-based colours. Every Compat rebuilds its wood variants from the active pack's planks and logs, so those follow by themselves. For the rest, an idea on hold until the team approves it (the prototype showed too many defects): a `teinte` module of the `fallow` mod would recolour the mods' textures at load time, on each player's computer, from a pattern learnt on the vanilla materials (each wood, each stone, dirt, metals): nothing of the mods (most of them under closed licences) is redistributed, and a mod added later follows on its own. A prototype gave good results on Chipped, Twigs, Handcrafted and Supplementaries' stone, and not enough on Create's brass and gears.
2. **Hand redraws, in batches**, once the in-game test Y7 shows which mod blocks clash most, of the most visible textures in Vanilla Evolved's style: Regions Unexplored's woods and stones, Create's casings and cogwheels, Fallow's fires and lanterns.

## In-game tests

- **Y1** The pack is active on first launch, in the order above (Options → Resource Packs).
- **Y2** Grass, flowers and mushrooms show Vanilla Evolved's models and random variants; Where Winds Blow still makes them sway.
- **Y3** Leaves stay bushy (Better Leaves) and keep Polytone's colours in each biome, clearings included.
- **Y4** The moon is vanilla, the sun is ATMOSPHERICS', rain and snow look as before.
- **Y5** Creatures keep Fresh Animations; chests open with Fresh Animations Objects and show Vanilla Evolved's wood.
- **Y6** A powered observer shows no missing texture; no missing-model or missing-texture warning about `fallow-textures` in the log.
- **Y7** In the mist, at night and in a clearing, blocks of the mods (Chipped, Regions Unexplored, Create) next to vanilla blocks: note the ones that clash most, for the hand redraws.
