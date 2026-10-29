# In-game tests

List set aside on 2026-10-25 for when there is time to play. None of this has been checked with a player yet: the pack starts on a server, but nobody has played on it.

It is split into **8 sessions of about one hour**, in the order the game brings them up. Tick items as you go. When something goes wrong, note its number and attach the log (Modrinth App → Fallow instance → Logs, or `crash-reports/`) to the report.

The code in brackets, such as [K1], points to the detailed test in the repository. A French copy of this checklist is kept for the team outside the repository.

## Before starting

- [ ] Import the latest `.mrpack` (Actions tab of the repository → latest build → artifact) into the Modrinth App, with **8 GB of memory** for the instance.
- [ ] Create a **Creative world with commands allowed**, seed `fallow`. A new world per session if possible: many settings are only read at world creation.
- [ ] Keep F3 handy: it shows the biome (useful for the clearings).

## Session 1: first launch and first steps

- [ ] The game is in French, the Fresh Animations, `fallow-interface` and `fallow-ambiance` packs are active, graphics set to "Fancy" (« Détaillés »), music at 40%.
- [ ] The log shows no `fallow`, KubeJS or mixin errors. It should contain « Module ciel chargé » (sky module loaded), « Module sac ignoré » (bag module skipped: Traveler's Backpack has left the pack), « Module xp chargé » (xp module loaded), « Module horizon chargé » (horizon module loaded) [C10, S9, X1, H1, F1, D1, L1].
- [ ] The `fallow` music pack is selected in Options → Music & Sounds → True Adaptive Music, with no "Problems detected" [M1, M2, M7].
- [ ] The title screen shows the Fallow logo over the misty forest, and the loading screen (F3+T) is moss green with the logo and a lantern-coloured bar [I6].
- [ ] Each screen's name stands centred over its top edge on its plate, in the screen's colours (furnace, crafting table, anvil, chest renamed in an anvil, trader), nothing overlapping [I1, E1-E3].
- [ ] No XP bar, on foot or on horseback (the jump gauge stays) [X7].
- [ ] F5: only first person and the over-the-shoulder view, free camera with the mouse.
- [ ] Open the inventory and a chest: the 3D menu from Spatial GUI, the camera moves to the shoulder. Books and the atlas stay flat.
- [ ] EMI: R and U on an item open its recipes; JEI stays invisible. Quark's inventory sorting works.
- [ ] Vanilla animals move with Fresh Animations (pig, cow, chicken, sheep) [F2].

## Session 2: the mist and the clearings

- [ ] **Quest journal** (Questlog): opens from the inventory; four chapters in French; guides complete when read; the tannery and machines quests appear with a raw hide or a Create part; placing the vigil fire and the lantern completes their quests [N1 to N5].
- [ ] Place a **vigil fire** (*feu de veille*) in a misty forest: « La brume se retire. » (the mist withdraws), the mist thins and the grass turns vivid within a few seconds [K1].
- [ ] F3 at the edge of the disc: `fallow:clairiere` inside, the old biome outside, a round edge [K2].
- [ ] A **clearing lantern** (*lanterne de clairière*) in a snowy taiga (snow still falls), a fire in a savanna [K3].
- [ ] A clearing on a coast: the ocean and the beach keep their biome; a river crossing it becomes clearing [K4]. Note whether a Streams brook stays misty [W10].
- [ ] Break the fire, leave, come back, restart: the clearing stays [K5].
- [ ] « Clairière » (clearing) music after 30 s [K6].
- [ ] **Far view**: from a clearing on a hill, the Distant Horizons landscape shows in the distance, with no seam [K7, H3]. In the mist, no distant landscape shows, even from a summit [H2].
- [ ] Enter and leave the clearing: visibility changes smoothly [H4]. At sunrise and sunset, the colour of the distance follows the sky [H5].
- [ ] Underwater, blinded, in powder snow: no sharp landscape in the distance [H6].
- [ ] Better Clouds clouds pass in front of the distance, with no duplicates [H9]. The colours of the distance follow the seasons [H12].
- [ ] The three recipes (fire, lantern, dissipator) in EMI, with French names and tooltips [K11].
- [ ] Crops and temperature in a clearing: things grow, the temperature stays that of the old biome [K10].
- [ ] **Wind and light** (Where Winds Blow, Shine): a No Man's Land forest under rain then thunder, grass and leaves sway, rain slants; FPS before and after [R1, R2]. A clearing at sunset: halo on the sky and the fires, the mist still around [R3]. Snow and desert: no extra fog [R4]. An airship in flight with lanterns: no halo glitch, lanterns sway [R5]. Distant landscape stays still [R6].

## Session 3: the night and the moon

- [ ] Clear night: a single moon, in its place, with the right phase [C1].
- [ ] `/ec setLunarEvent enhancedcelestials2defaultlunarevents:blue_moon`, then harvest moon and super moon: colour and size specific to each, with no vanilla moon behind [C2, C3].
- [ ] Rain then thunderstorm: the moon fades with the sky [C4]. Underwater and blinded: no double moon, no vanishing moon [C5].
- [ ] Frames per second at night: no visible difference with the `ciel` (sky) module turned off [C11].
- [ ] `/ec lunarForecast`: no blood moon [L2].
- [ ] An alchemist ruin: look at the engraving for 2 s (zoom and sound), sleep within 3 days, see the dream through to the end (advancement « Premier contact » (first contact)) [L3, L4]. Break jars: the crumpled note sometimes drops, in French [L11].
- [ ] At the Meeting Point (*Point de rencontre*, `/meetingpoint`), at night: the face of the Friendly Moon (*Lune Amie*) appears, with no coloured moon beside it [L5, C6]. With a forced blue moon, it sets in 2 s with a message [L6].
- [ ] Offerings (bone, flower, empty map, turtle scute), then put out one candle, then all of them: sentences in French, then an angry moon [L8, L9].
- [ ] A rainy night at the Meeting Point: does the Friendly Moon appear? [L12]
- [ ] Nights are truly dark (More Darkness), auroras in cold biomes.

## Session 4: survival (thirst, anvil, leather, health, altitude)

- [ ] **Bag** (Reliable Backpacks): craft it from 8 tanned leather, wear it on the back (Curios slot, not the chestplate), look at it with Fresh Animations, 3D Skin Layers and the shoulder view, place it on the ground and on an airship, dye it [B1 to B4].
- [ ] **Stacks**: logs and ingots stop at 16, planks and stone at 32, seeds at 64, chests at 8, Create machines at 4, water bottles at 8; tools never stack. A Kaleidoscope pot (9 dishes) and the Create crusher (18 nuggets) empty without losing anything. Compare `config/sensible_stackables/config.toml` with the generated file [P1 to P5].
- [ ] **Sleeping bag** (Comforts): sleep in it, the spawn point does not move; is it cold in the snow (Cold Sweat)?
- [ ] **Anvil without levels**: rename, repair with ingots, merge two enchanted pickaxes. Green cost « Usure : n » (wear: n), the item can be taken. At 40: « Trop usé ! » (too worn!) [X2, X3]. Blue moon: the cost drops [X8].
- [ ] **No XP anywhere**: mine coal, smelt iron, kill, breed, fish: no orb; `/xp add @s 10 points` changes nothing [XP3].
- [ ] Immersive Enchanting: copy an ancient book, transmute it with 4 amethyst shards [XP6].
- [ ] **Tannery**: a cow and a horse give a raw hide; hide + ashes at the crafting table; charcoal on a campfire gives ashes; prepared hide on the bamboo tray in the rain or under a dripstone → leather; EMI shows both steps. Note whether bamboo is too rare [T].
- [ ] **Health**: die 6 times → 9, 8, 7, 6, 5, then 5 hearts, with the messages [V1]. `/give @s fallow:eclat_de_vie 3`: each shard restores one heart, refused at 10 hearts [V3]. Log out and back in: health stays [V2].
- [ ] **Altitude** (Cold Sweat: Altitude): it gets colder climbing a big Tectonic summit, less so under shelter.
- [ ] Ecliptic seasons: they change the crops, with no heat stroke and no chat message.
- [ ] **Day length** (Better Days): time a day (30 min) and a night (15 min); the sky moves smoothly, also for a friend on the server; sleeping skips the night and Ecliptic counts the day; the Enhanced Celestials moon rises at dusk; the night music starts with the night [J1 to J5].

## Session 5: wildlife tour

- [ ] Forest, taiga, savanna, jungle, swamp, rivers: bear, tiger, elephant, rhinoceros, monitor lizard, alligator, Naturalist snake, without their Alex's Mobs duplicates [D2].
- [ ] Oceans and reefs: Hybrid Aquatic sharks, jellyfish, orcas, octopuses; Naturalist anglerfish, blobfish, isopod, ray; Alex's Mobs giant squid [D3]. Lobster texture correct [D8].
- [ ] Flowering meadows: only Longwings butterflies [D5]. Lush caves: C&C otter, no dragonfly or snail [D4].
- [ ] No deer, goose, bass or NML turtle, no rabbit, no Frienderman (`/summon nomansland:frienderman` disappears) [F3, F6, F10].
- [ ] EMI and Wildex: nothing from the removed creatures (« venaison » (venison), « crocodile » (crocodile), « homard » (lobster), « lapin » (rabbit)…) [F7, F8, D6, D7].
- [ ] No vanilla monsters in the Overworld, even at night, no patrol, no phantom. Never more than 8 hostile creatures around you.
- [ ] Wildex: a new species is recorded; its pages look good with the chosen theme [I3].

## Session 6: terrain, Create and airships

- [ ] Explore 3000 blocks with the atlas: large continents, mountain ranges, valleys; NML biomes present [W1].
- [ ] Highest summit below Y 320, an airship clears the range [W5]. In winter, snow consistent with altitude [W6].
- [ ] Boat and Sable hull on a brook: the hull drifts, the airship in flight does not [W7]. Create pump and boiler on a brook [W8].
- [ ] **Dissipator**: nothing at 16 rpm; at 64 the engineer's goggles climb to 100% and a 64 disc is converted; on overload the charge resets to zero [K8]. On an airship: note what happens [K9].
- [ ] Crush raw iron ore: no XP nugget, the rest is there [XP4].
- [ ] Airship: wind sound that follows speed; a recorded route between two stations (Automated Logistics); blocks from other mods that have weight and float (Physics Compat).
- [ ] The atlas shows rails, stations and ships (Antique Transport). A lantern on a vehicle lights the ground as it passes.
- [ ] Music aboard an airship in flight [M4].

## Session 7: monuments and exploration

- [ ] The six Monoliths (`/locate structure ancient_remnants:…`) are whole, on the ground, not cut by water [W3]. The Meeting Point is on reachable land, between 1000 and 2500 blocks from the centre [W2, L13].
- [ ] Approach a monument: a life shard and a message, only once [V10]. Music *I Walk With Ghosts* on approach [M6].
- [ ] Ruin chests: no XP vial; a life shard about one time in five in the wells [XP5, V7].
- [ ] Enough alchemist ruins to find an engraving without searching for hours [L14].
- [ ] A Nether portal does not light; no eye of ender; `/locate structure minecraft:stronghold` finds nothing.
- [ ] Exposure camera: take and develop a photo; Exposure: Space, photograph the moon.

## Session 8: multiplayer server (with a friend)

- [ ] On a dedicated server: the thirst bar updates, the anvil does not roll back [S7, X4].
- [ ] Lootr: everyone finds the chest full. Monument: the second player also gets their shard [V7, V10].
- [ ] Friendly Moon: nothing changes for a player far from the Meeting Point [L7]. Envelope: name a mailbox at no cost, send a letter by pigeon [XP7].
- [ ] Chunky `start` on the server, then a player explores: no holes in the far landscape [H11]. Note the generation speed with spark [W9].
- [ ] Simple Backups: one backup after an hour of play, and one when everyone leaves.
- [ ] On Apple Silicon Mac: one hour of play with the far view and no Java crash [H10].
- [ ] spark (`/spark profiler`) for 10 min with 2 or 4 players: share the link so the cost can be reviewed [M10].

## After the tests

Once the results are in:

1. Fix what broke, then apply the **updates held back**: Polytone 5.3.1 with Spatial GUI 1.9, Supplementaries 4, Friends & Foes 5, Envelope 0.8, Icy's Better Horses 2.1 (the revive XP cost has to be replaced), Cold Sweat 2.4.4 (once Altitude follows it), Lithium (to remove if it freezes with Sable), Subtle Effects and More Darkness (client-side settings to read in game).
2. Compare the hand-written Quark config with the one the game generates.
3. Publish version `0.1.0` (tag and release), then pregenerate the permanent world with Chunky.

## Repository housekeeping

- [ ] Delete the old branches, all already merged into `main`: `test/agriculture`, `test/faune`, `test/structures`, `feat/idea-mods`, `feat/safety-net-and-updates`, `feat/tectonic-streams-distant-horizons`. Branches can only be deleted by the repository owner; the cloud environment gets a 403. On GitHub: *Branches* tab, trash-can icon.

## Where the codes come from

| Code | Detailed list |
| --- | --- |
| C (sky), S (bag), X (anvil), H (far view), W (terrain), K (clearings), E (screen names) | [plan/mod-fallow.md](plan/mod-fallow.md#in-game-tests) |
| L | [plan/lune-amie.md](plan/lune-amie.md) |
| F | [plan/faune-nml.md](plan/faune-nml.md#in-game-tests) |
| D | [plan/doublons.md](plan/doublons.md#in-game-tests) |
| M (numbered items of "To test in game") | [plan/musique.md](plan/musique.md#to-test-in-game) |
| I (numbered items) | [plan/fallow-interface.md](plan/fallow-interface.md#limits-and-in-game-tests) |
| XP (numbered items of "Remaining to test in game") | [plan/xp.md](plan/xp.md#remaining-to-test-in-game) |
| T | [plan/tannerie.md](plan/tannerie.md#what-remains-to-be-tested) |
| V (bullets in order, V1 = first) | [plan/sante.md](plan/sante.md#what-remains-to-be-tested-in-game) |
| N (quests) | [plan/quetes.md](plan/quetes.md#in-game-tests) |
| B (bags), P (stacks), J (day length) | [decisions/0012](decisions/0012-sacs-piles-jours.md#conditions-and-tests) |
| R (wind and light) | [decisions/0013](decisions/0013-vent-et-lumiere.md#conditions-and-tests) |

Tests without a code come from the mods added on 2026-10-25 and from [the mod registry](mods.md).
