# 0033 — Four animal mods for the zoo

- **Date**: 2026-11-17
- **Status**: kept (the team's choice of 2026-11-16); starts on a dedicated server, not played. Adds to [0003](0003-faune.md), which had set WilderNature aside

## Context
The zoo of the `compline` mod gives stamps for every creature of the Overworld. The team wants it full of good-looking animals. House remakes of Alex's Mobs creatures were tried on 2026-11-16 (realistic, then in the vanilla style) and turned down: their models and hand-written animations did not reach the level of the best fauna mods. A survey of the NeoForge 1.21.1 fauna mods on Modrinth followed, with a gallery for the team.

## Options
| Criterion | A. House models | B. More fauna mods |
|---|---|---|
| Look | Below Archie and the best mods so far | Vanilla-like models with keyframe animations |
| Species | One at a time | About a hundred at once |
| Work | A model and its animations per creature | Translation, duplicates, zoo tabs |
| Licence | Ours | Referenced on Modrinth, never redistributed (several are All Rights Reserved) |

## Decision
Option B, four mods:

- **Koopa's Critters** 0.5 (rare and threatened species: binturong, Pallas's cat, penguins, takahe, manta ray, goblin shark…). Its termite mounds are off (`config/Koopa's Critters Configurations.toml`): WilderNature's have the termites. Its red panda has no entity in 0.5.
- **Spawn** 4.0.8 with Teal Lib (sea life: sea cow, sunfish, seahorse, octopus, seal, spider crab…). Its creatures stay; **its islands (cold, sandy, tide pool, tropical, volcanic, dodo), octopus cities and ant mounds do not generate** (the team's choice of 2026-11-16): the biome tags of the jigsaw and island structures are emptied in `kubejs/data/spawn/tags/worldgen/biome/`, and the three islands placed by biome name point to the empty tag `compline:aucun`. Its two Biolith biomes stay, like Hybrid Aquatic's underwater biomes: rocky shore (part of the warm beaches) and deep warm ocean. `scripts/biomes/models.py` maps its biomes for the biome rules.
- **Hybrid Birds** 1.3.3 (peacock, swan, goose, pelican, albatross, puffin, guineafowl, turkey, rooster), same author and library as Hybrid Aquatic. Its flamingo is not registered in 1.3.3.
- **[Let's Do] WilderNature** 1.1.8 (bison, raccoon, beaver, cassowary, red wolf, swift fox, squirrel, mini sheep, scorpion, termite, hunting trophies). The vanilla farm animals keep spawning in forests, jungles, savannas and swamps (`config/wildernature-common.toml`).

One model per species, as in [duplicates](../plan/doublons.md#zoo-mods-2026-11-17): about twenty pairs compared from the jars. New birds and sea creatures are sorted into the zoo tabs (`tags/entity_type/zoo/ciel.json`, `eaux.json`), and every cut creature is now in `zoo/exclus`, so no stamp asks for a creature that cannot be met.

**French**: 2,241 texts translated (Spawn 1,003, Koopa's Critters 590, WilderNature 552, Hybrid Birds 72, Foundation 19, Teal Lib 5).

## Rejected
- Animal Garden (37 one-animal mods): the team said no on 2026-11-16.
- Fowl Play: birds in flight, kept as an option if Hybrid Birds is not enough.
- The Let's Do mods with biomes (Meadow, BloomingNature, Alpine Whispers, Beachparty): in conflict with the disc terrain.
