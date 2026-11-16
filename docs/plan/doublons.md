# Wildlife duplicates: one model per species

Request from the maintainer, 2026-10-19: when two mods have the same species, compare the models and keep the best one. No cuts for reasons of style: Companions! stays in full. Untitled Duck Mod is removed. The comparison was made on the jars (models rebuilt and rendered at the same scale, bones, animations, buckets, spawns), without running them. **Nothing has been tested in game.** It replaces the duplicate choices in [5.3](../archive/plan/05-faune-et-ambiance.md) and [5.7](../archive/plan/05-faune-et-ambiance.md).

Fresh Animations animates no modded creature: consistency with vanilla is judged on proportions and type of animation. Naturalist 2.0 animates with keyframes, like vanilla 1.21; Alex's Mobs mostly with sinusoids. For the zoo, with equal models, the version that can be put in a bucket wins (Hybrid Aquatic has no fish bucket).

## Verdicts

| Species | Kept | Cut | Reason |
|---|---|---|---|
| Brown bear | Naturalist | Alex's Mobs (grizzly) | 12 animations, states (sleep, sniffing), vanilla 1.21 style |
| Tiger | Naturalist | Alex's Mobs | Proportions, palette, variants, animations |
| Elephant | Naturalist | Alex's Mobs | More bones and animations; it also has a saddle and a chest |
| Rhinoceros | Naturalist | Alex's Mobs | Two horns, animated charge |
| Komodo dragon | Naturalist | Alex's Mobs | Keyframe animations |
| Crocodilian | Naturalist (alligator) | Alex's Mobs (crocodile) | Same role in the swamps; Alex's Mobs' caiman, a different species, stays |
| Rattlesnake | Naturalist | Alex's Mobs | 10 animations against one |
| Blue jay | Naturalist (bird variant) | Alex's Mobs | Scale consistent with its other songbirds |
| Catfish | Naturalist | Alex's Mobs | Vanilla scale, bucket |
| Anglerfish | Naturalist | Hybrid Aquatic | More polished model, animated lure, bucket |
| Blobfish | Naturalist | Alex's Mobs, Hybrid Aquatic | Most readable silhouette, bucket |
| Giant isopod | Naturalist | Hybrid Aquatic | More bones and animations, bucket |
| Piranha | Naturalist | Hybrid Aquatic | Natural palette, bucket |
| Ray | Naturalist (3 species) | Hybrid Aquatic (stingray) | Bucket, burrows into the sand; Hybrid Aquatic's manta ray, a different species, stays |
| Great white shark | Hybrid Aquatic | Naturalist | Streamlined silhouette, consistent with its other sharks |
| Hammerhead shark | Hybrid Aquatic | Alex's Mobs | More detailed, same family |
| Jellyfish | Hybrid Aquatic | Naturalist | Thin, translucent, consistent series |
| Comb jelly | Hybrid Aquatic | Alex's Mobs | Iridescent animated texture |
| Starfish | Hybrid Aquatic | Naturalist | Proportions, three species; Naturalist's 4 starfish blocks are lost |
| Orca | Hybrid Aquatic | Alex's Mobs | Baby, variants, keyframes |
| Lobster | Hybrid Aquatic | Alex's Mobs | More detailed (to check in game: its texture is a tint map) |
| Flying fish | Hybrid Aquatic | Alex's Mobs | Thinner, close to vanilla |
| Pupfish | Hybrid Aquatic | Alex's Mobs | Scale of the vanilla cod |
| Octopus | Hybrid Aquatic | Alex's Mobs (mimic octopus) | More polished |
| Frilled shark | Alex's Mobs | Hybrid Aquatic | More expressive, bucket |
| Giant squid | Alex's Mobs | Hybrid Aquatic | Similar models; Alex's Mobs' lives in the vanilla deep oceans, Hybrid Aquatic's only in its trenches, which are due to be cut (12.1) |
| Otter | Critters and Companions | Hybrid Aquatic | More animations, better silhouette |
| Dumbo octopus | Critters and Companions | Hybrid Aquatic | Hybrid Aquatic's does not spawn anywhere |
| Dragonfly | Naturalist | Critters and Companions | Scale (C&C's has a two-block wingspan) |
| Snail | Naturalist | Critters and Companions | More discreet and more animated |
| Butterfly | Longwings | Naturalist (and its caterpillar) | Readable silhouettes, 70 species to collect |
| Crab | Friends & Foes, Hybrid Aquatic's crabs | Naturalist | Design from the Mojang vote; Hybrid Aquatic's crabs are other species |
| Duck | Naturalist (until 2026-11-17, then Hybrid Birds) | — | Only duck since Untitled Duck Mod was removed |

Kept despite a similar role, because they are different species: Naturalist's whale and Alex's Mobs' sperm whale, ant and leafcutter ant, ostrich and emu, C&C's koi and Hybrid Aquatic's carp, sea slugs, Cold Sweat's lizard and chameleon, boar and tusklin, glare and lantern, the turtles.

## How

| Mod | Where | How |
|---|---|---|
| Naturalist | `pack/config/naturalist-server.toml` | `_removed = true` per species, in sections (`aquatic_mobs`, `land_mobs`, `bugs`); the species no longer spawns and disappears from the world. The forest rabbits that Naturalist adds are cut as well |
| Hybrid Aquatic | `pack/kubejs/data/hybrid_aquatic/neoforge/biome_modifier/` | its 25 per-species spawn files replaced by `neoforge:none` |
| Alex's Mobs, Critters and Companions | `pack/kubejs/data/compline/neoforge/biome_modifier/doublons.json` | `neoforge:remove_spawns` on `#minecraft:is_overworld`; no partial config, since their formats (CodxLib, YACL) have not been tested with an incomplete file |
| All | `pack/kubejs/server_scripts/doublons.js` | joining the world cancelled for the cut creatures (C&C's lush caves, biomes outside the tag); 78 items with no source removed from recipes and loot (including the seal's lobster bucket) |
| All | `pack/kubejs/client_scripts/doublons.js` | the same items hidden from EMI |
| Alex's Mobs | `pack/kubejs/data/alexsmobs/tags/entity_type/roadrunner_targets.json` | the roadrunner hunts Naturalist's snake, as it used to hunt Alex's Mobs' rattlesnake |

A test (`scripts/test/kubejs-lists.test.mjs`) checks that the items removed server-side and hidden client-side are the same.

Consequences: three sources of slimeballs disappear (Komodo spit, Naturalist's jellyfish, C&C's slime vial); Naturalist's snail still drops them.

## Other creatures removed

Older decisions of the plan, confirmed by the maintainer on 2026-10-19:
- Alex's Mobs: Murmur, Farseer, Skelewag, fly, cockroach (art direction, 5.3);
- Friends & Foes: iceologer, illusioner, wildfire (hostiles, decision 0003), and their four structures (iceologer's cabin, illusioner's cabin and training ground, wildfire's citadel), whose biome tags are emptied;
- Naturalist: mammoth (prehistory ruled out, 0003).

Same method as for the duplicates: `naturalist-server.toml`, `pack/kubejs/data/compline/neoforge/biome_modifier/faune_coupee.json`, `faune_coupee.js` (server and client, 22 items including the sinew whip, the transmutation table and the wildfire crown, which can only be crafted from items dropped by these creatures). The Alex's Mobs advancements tied to these creatures stay in their tab without being obtainable: some are parents of other advancements that can still be earned.

## Creatures with weak animations (2026-10-28)

Request from the maintainer after in-game tests: some animals look poor next to the vanilla animals animated by Fresh Animations. The wildlife stays; only the weakest are cut. Each creature was rated from its jar (number of animations, animated bones, keyframes and states such as sleeping, sitting or eating); the full rating, mod by mod, is in the team's document. The maintainer chose to cut the eight weakest and the borderline cases:
- Alex's Mobs: banana slug, alligator snapping turtle, mungus, triops, sunbird, guster (one or two sine loops, no state, no keyframe); rocky roller, frilled shark, potoo (borderline);
- Friends & Foes: moobloom (vanilla cow model, which Fresh Animations does not animate) and glare, also disabled by `enableMoobloomSpawn` and `enableGlareSpawn = false` in `pack/config/friendsandfoes.json`;
- Critters and Companions: koi (4 bones, 2 animations), sea bunny;
- Hybrid Aquatic: sea urchin, comb jelly, garden eel, nautilus, sea slug, sea cucumber, starfish (1 to 7 bones, a few keyframes); their 25 spawn files are replaced by `neoforge:none` in `pack/kubejs/data/hybrid_aquatic/neoforge/biome_modifier/`, and the nautilus, which only spawns from the trench and deep reef biome files, is covered by `faune_coupee.json`.

Same method as above: `faune_coupee.json`, `faune_coupee.js` (server and client, 43 more items: eggs, banana slug slime and its blocks, spiked scute and spiked turtle shell, mungal spores, triops bucket and eggs, mosquito repellent stew, guster eye, pocket sand, gustmaker, koi and its bucket, rocky shell and rock shell chestplate, frilled shark bucket, sea bunny bucket, slime bottle and slime block, sea urchin spine, thrown starfish); the guster and the rocky roller are taken out of the In Control! darkness rules.

Consequences:
- the comb jelly, the frilled shark and the starfish disappear from the game, since their duplicates were already cut;
- the buttercup stays (it also grows in the world), as do the shark teeth (seal gifts) and the vanilla nautilus shell;
- the moobloom's hide loot in `tannerie_butin.js` no longer has any effect;
- Hybrid Aquatic's argonaut is a craftable submarine, not a creature: it stays.

## Zoo mods (2026-11-17)

Koopa's Critters, Spawn, Hybrid Birds and WilderNature joined for the zoo ([0033](../decisions/0033-zoo-animal-mods.md)). Same rule: one model per species, compared from the jars (parts and cubes, number of animations and keyframes, states such as sleep or eat, buckets, variants). Nothing has been seen in game.

| Species | Kept | Cut | Reason |
|---|---|---|---|
| Herring | Hybrid Aquatic | Spawn | Keyframed idle, swim and flop against a procedural 4-part fish |
| Tuna | Hybrid Aquatic | Spawn (and its egg) | Four tuna species, eat, bite and run |
| Barracuda | Hybrid Aquatic | Spawn | Idle, eat, bite, run and flop against one swim cycle |
| Ocean sunfish | Spawn | Hybrid Aquatic | Six times the keyframes, baby stage; Hybrid Aquatic's river sunfish, another species, stays |
| Seahorse | Spawn | Hybrid Aquatic | Four body shapes, many colours, bucket |
| Octopus | Spawn | Hybrid Aquatic | Walks on the sea floor, hides in ducts; Hybrid Aquatic's three species are lost |
| Spider crab | Spawn | Hybrid Aquatic | More cubes, attack and shoot states |
| Manatee | Spawn (sea cow) | Hybrid Aquatic | Grazes seagrass and gets covered in algae; Hybrid Aquatic's dugong, another species, stays |
| Seal | Spawn | Alex's Mobs | 18 keyframed animations (surf, applause, lying), baby model, against procedural movement |
| Goblin shark | Koopa's Critters | Hybrid Aquatic | Bucket, more keyframes, lives in the deep oceans rather than the trenches |
| Manta ray | Koopa's Critters | Hybrid Aquatic | About 17 times the keyframes |
| Clam | Naturalist | Spawn | Its meat feeds the kitchen (merged with Hybrid Aquatic's, `fusions.js`); Spawn's clam cases leave with it |
| Snail | Naturalist | Spawn | Naturalist's snail is the slimeball source of the pack; Spawn's shell tiles leave with it |
| Ant | Naturalist | Spawn (and its anthills) | Same animation volume; one anthill system |
| Hamster | Adorable Hamster Pets | Spawn | 76 animations against procedural movement |
| Bison | WilderNature | Alex's Mobs | Keyframed set with sleep and snort; bison fur and its blocks leave |
| Raccoon | WilderNature | Alex's Mobs | Washes, opens doors, sleeps; the raccoon tail and the frontier cap leave |
| Seagull | Hybrid Birds | Alex's Mobs | Keyframed swim and flight, same set as the other sea birds |
| Turkey | Hybrid Birds | Naturalist, WilderNature | Flies, fat and stuffed models, Turducken |
| Duck | Hybrid Birds | Naturalist | Flies, baby set, goes into the Turducken with the turkey; Naturalist's bucket and sixteen colours are lost |
| Boar, deer, elephant, giraffe, hedgehog, hippo, lion | Naturalist | WilderNature | More parts and keyframes on most, one consistent savanna; WilderNature's sleep states are lost |

How: Hybrid Aquatic's per-species files replaced by `neoforge:none` (25 more), the same for Spawn's (`kubejs/data/spawn/neoforge/biome_modifier/`), WilderNature's spawn weights set to 0 (`config/wildernature-common.toml`), Naturalist's turkey and duck in `naturalist-server.toml`, Alex's Mobs' seal, bison, raccoon and seagull (and WilderNature's, as a second lock) in `doublons.json`; `doublons.js` cancels the creatures and removes 76 more items with no source left (spawn eggs, foods, buckets, Spawn's clam cases and shell tiles, Alex's Mobs' bison fur, raccoon tail and frontier cap, WilderNature's deer trophy and turkey meat), hidden from EMI on the client side.

The zoo lists a species from the biome spawn lists, and some cut creatures stayed there (Alex's Mobs' crocodile and frilled shark, C&C's koi, which come in by other paths and are cancelled on joining). Every creature of `doublons.js` and `faune_coupee.js` is now in `tags/entity_type/zoo/exclus.json`.

## Hybrid Aquatic biomes (2026-10-19)

They worked beside No Man's Land (checked on 2026-10-19), which also placed its biomes through Biolith; beside Regions Unexplored they have not been seen in game. 12.1 wanted to cut them in the name of decision 0003 (no biomes that change the face of the world). Decision of the maintainer: keep its other 13 biomes, almost all underwater (reefs, trenches, seagrass beds, red meadow, deep warm ocean, tide pools, sulphurous caves), with the 34 species that only spawn there, and cut only the **tropical river**, which replaced part of the vanilla rivers, as far as the taigas.

`pack/config/hybrid_aquatic.json`: `generateTropicalRiver = false`. The mod reads this file through a codec that requires all fields and data version 8 (otherwise it saves the file and replaces it with its default values): the file is therefore complete, with default values read from the 1.7.3 jar. `spawn_configuration` is empty: the list is only used for the mod's data generation, not in game. At every mod update, check `data_version`.

## In-game tests

On a new world.

| # | Test | Expected |
|---|---|---|
| D1 | Logs at launch | No error on `naturalist-server.toml`, `doublons.json`, Hybrid Aquatic's biome modifiers, `doublons.js` |
| D2 | Forest, taiga, savanna, jungle, swamp, rivers | Naturalist's bear, tiger, elephant, rhinoceros, Komodo, alligator, snake; none of their Alex's Mobs doubles |
| D3 | Oceans, reefs, trenches | Hybrid Aquatic's sharks, jellyfish, orcas, octopuses; Naturalist's anglerfish, blobfish, isopod, ray; Alex's Mobs' giant squid |
| D4 | Lush caves, rivers | Neither C&C's dragonfly nor snail; C&C's otter |
| D5 | Flower meadows | Longwings butterflies only |
| D6 | EMI: "crocodile", "lobster", "starfish", "dragonfly" | Nothing from the cut creatures |
| D8 | Hybrid Aquatic's lobster | Correct texture (the tint map could not be rendered outside the game) |
| D9 | Snowy biomes, dark forests, Nether | No iceologer, illusioner or wildfire, nor their structures; no Murmur, Farseer, Skelewag, fly or cockroach; no mammoth |
| D10 | `/locate biome hybrid_aquatic:tropical_river`; `/locate biome hybrid_aquatic:coral_reef` | Not found; found |
| D11 | Old growth taiga, swamp, mushroom fields, desert by night, flower meadow, ponds, lush caves, jungle, caves, oceans and trenches; EMI: "slug", "triops", "guster", "koi", "rocky", "sea bunny", "urchin", "starfish" | None of the creatures cut on 2026-10-28, none of their items |
