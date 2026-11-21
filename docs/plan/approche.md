# The approach: the wind carries the scent

Idea 2 of the study of features to add (2026-11-17), chosen by the team on 2026-11-18. Shy wild animals notice the player by scent, sight or hearing, raise their head, then flee with their herd. Coming up against the wind, crouched, lets a player get close enough for the zoo's photo or observation; the spyglass spots a species from much further away. It changes how animals behave, never their models.

Status: checked on a dedicated server of the full pack with one player on 2026-11-18 (`fallow` 0.32.0), not played.

## The rule, for a player

- **Scent**: when the wind blows from the player toward an animal (within 70° of the wind's direction), it smells them up to about 32 blocks in a moderate wind, further in a strong wind, less under rain, and more in the middle of the plume than at its edge. Against the wind or across it, no scent at all.
- **Sight**: an animal that sees the player with nothing in between notices them at 14 blocks standing, about 6 crouched, 18 sprinting, and only 60 % as far in the dark.
- **Hearing**: 6 blocks walking, 12 sprinting, under 2 crouched, through walls and grass.
- **Alert, then flight**: an animal that notices the player from afar first stops and stares for about a second and a half. A player who stops or crouches can stay unseen; one who keeps coming makes it bolt. Close by, it bolts at once. It runs away for about seven seconds, longer while it still perceives the player, and is warier (a quarter further) while it is on edge. Animals of the same species within 12 blocks flee with it.
- **A word now and then**: when an animal flees, the action bar may say what gave the player away naming the species (« Cerf : le vent lui a porté votre odeur. », « Cerf : vous avez été vu. Accroupissez-vous pour approcher. », « Cerf : vous avez été entendu. »), at most once every 90 seconds.
- **Reading the wind**: the wind is the pack's own (the `vent` module): campfire smoke leans with it, and under Brumaire grass and leaves bend with it. The server computes the same wind as every client from the world time and weather.
- **The spyglass**: looking through it, an animal at the centre of the image (within 8°) earns the zoo's « Apercevoir » stamp up to 64 blocks away instead of 25.

Never concerned: tamed, leashed, ridden or AI-less animals, and every species outside the tag below (farm animals, predators, fish, insects).

## Which animals

`fallow:farouches` (entity type tag, in the `fallow` mod's data; a datapack or `pack/kubejs/data/fallow/tags/entity_type/farouches.json` can extend it). Every entry outside vanilla is optional, so a mod that leaves the pack does not break the tag.

| Mod | Species |
|---|---|
| Vanilla | fox, ocelot, rabbit (removed from the game, kept in case it returns) |
| Naturalist | deer, zebra, giraffe, ostrich, turkey, lizard |
| Alex's Mobs | gazelle, moose, kangaroo, roadrunner |
| WilderNature | deer, bison, beaver, squirrel, swift fox, raccoon, turkey |
| Koopa's Critters | giant muntjac, mountain viscacha, hyrax, Tibetan fox, California quail, rock ptarmigan, helmeted guineafowl |
| Hybrid Birds | peacock, guinea fowl, turkey |
| Critters and Companions | red panda |

Bears, big cats, boars, elephants, rhinos and other animals that stand their ground keep their own behaviour.

## How it works

See [mod-fallow.md](mod-fallow.md#approche-module). In short: every half second, for each player in survival, each animal of the tag within reach asks whether it perceives them; a flee goal added on top of its own goals stops it (alert) or runs it away from the player's last known position (`DefaultRandomPos.getPosAway`, speed ×1.5). The game's panic when hit keeps its place.

Config (`fallow-common.toml`, `[approche]`): `enabled` (true), `scentRange` (32), `sightRange` (14), `hearingRange` (6), `hints` (true); `[zoo]` `spyglassDistance` (64).

## Checked on the bench

Dedicated server with every mod of the pack, one player standing still in a grassland, a Naturalist deer summoned at a given distance along the wind computed from the world time:

| Deer | Player | Result |
|---|---|---|
| 24 blocks downwind | standing still | flees: 24 → 54 blocks in 14 s |
| 24 blocks across the wind | standing still | stays (24.0 → 24.4) |
| 24 blocks upwind | standing still | not alarmed, grazes away slowly (24 → 34) |
| 11 blocks upwind, in view | standing | flees at once (11 → 20) |
| 8 blocks upwind, in view | crouched | stays, then wanders (8 → 11) |
| 8 blocks upwind, in view | standing | stares about 6 s, then flees (8 → 17) |

Not checked on the bench: the herd fleeing together, the hint messages, the spyglass stamp at 64 blocks, the other mods' species.

## In-game tests

| # | Test | Expected |
|---|---|---|
| AP1 | Find a herd of deer in a plain; light a campfire to read the wind; walk toward them with the smoke blowing toward them | They flee from 25 to 35 blocks away, all together; « Cerf : le vent lui a porté votre odeur. » |
| AP2 | Go round them and come back against the wind, standing | They raise their head at about 14 blocks and stare; keep walking: they bolt |
| AP3 | Same, crouched | You get within 6 or 7 blocks without them fleeing; enough for the zoo's observation (16 blocks) and a photo |
| AP4 | Sprint toward an animal across the wind | It hears you at about 12 blocks |
| AP5 | A tamed fox, a leashed animal, a cow, a bear | None of them flees by scent |
| AP6 | Look at a new species through the spyglass, 40 to 60 blocks away | The « Apercevoir » stamp, as at 25 blocks without it |
| AP7 | Turn `[approche] enabled` off | Animals behave as before, at once |
