# 0003 — Fauna

- **Date**: 2026-09-23
- **Status**: kept (tested on `test/faune`, merged into `main` on 2026-10-10); prehistory dropped for now

## Criteria
Beautiful 3D models and animations (criterion no. 1), coherent ecology per biome, rare and territorial hostiles rather than nocturnal ones, animals for animal husbandry and the zoo. No vanilla monsters (removal: sub-project 3).

## Layers
| Layer | Mod | Role |
|---|---|---|
| Base fauna | **Naturalist** | Realistic fauna with behaviours (fleeing, hunting, sleeping) |
| Extended fauna | **Alex's Mobs Continued** | Variety and animations; **spawning managed species by species** |
| Oceans | **Hybrid Aquatic** | Marine fauna, rare deep-sea creatures |
| Anomalies | **Mowzie's Mobs** | Unique, very rare creatures |
| Small companions | **Critters and Companions**, **Friends & Foes**, **Adorable Hamster Pets**, **Companions!**, **Ghost** | Small fauna and tameable companions |
| Utility | **Companion** | Tamed animals no longer get lost |

## Condition: Naturalist + Alex's Mobs coexistence
The maintainer's choice, against the initial recommendation (duplicates, third-party port, sometimes whimsical tone). Consequence: in sub-project 3, a spawn table per biome that disables
- duplicates (bears, deer, crocodilians, snakes, birds: a single version per species);
- Alex's Mobs creatures that break the art direction (whimsical or comical ones).

## Duplicates (2026-10-19)
One model per species, the best after comparing models and animations, rather than a per-layer rule: Naturalist wins on land and for several fish, Hybrid Aquatic for sharks, jellyfish, orcas and octopuses. No cuts for reasons of style: Companions! stays in full. Details: [duplicates](../plan/doublons.md).

## Untitled Duck Mod removed (2026-10-19)
Removed by the team. The Naturalist duck returns; the NML goose's offering line now targets that duck.

## No Man's Land (2026-10-19)
Its remodelled and retextured animals do not go with Fresh Animations: back to vanilla animals, and its creatures (deer, goose, _bar à serpe_ (a fish), turtle, Frienderman) are disabled. The Companion stays. Details: [NML fauna](../archive/plan/faune-nml.md).

## Rejected
- Integrated Mowzie's Mobs: requires Quark, Supplementaries and Amendments (content pile-up).
- Let's Do Wilder Nature: blocky style, beta, duplicates Naturalist.
- Spawn, Environmental: add biomes (NML conflict).
- Untamed Wilds (unofficial port): too obscure.
- Alex's Mobs "(1.21.1)": competing port, inactive since January 2026.

## Conditions on companions (sub-project 3 / configs)
- Friends & Foes: keep the peaceful creatures (copper and tuff golems, crab, glare, moobloom), disable the hostile ones (iceologer, illusioner, wildfire…).
- Adorable Hamster Pets: disable the diamond sniffing if the config allows it (a shortcut contrary to exploration).
- Companions!: the maintainer's choice despite the recommendation to the contrary (RPG orientation). Restrict its hostile creatures, weapons and bosses: disable them or make them very rare.

## Dropped for now
- Prehistory (Jurassic Saga, Jurassic Revived, Jurassic Reborn, Shineal's).
