# In-game tests

List set aside on 2026-10-25 for when there is time to play. None of this has been checked with a player yet: the pack starts on a server, but nobody has played on it.

Updated on 2026-11-13, after No Man's Land left the pack ([0019](decisions/0019-leaving-no-mans-land.md)): the checks that needed it (Friendly Moon session, its fauna, the alchemists' ruins, the Meeting Point) are gone, and checks for Regions Unexplored and Geophilic are added. Neither mod has been seen in game.

It is split into **8 sessions of about one hour**, in the order the game brings them up. Tick items as you go. When something goes wrong, note its number and attach the log (Modrinth App → Tilth instance → Logs, or `crash-reports/`) to the report.

The code in brackets, such as [K1], points to the detailed test in the repository. A French copy of this checklist is kept for the team outside the repository.

## Before starting

- [ ] Import the latest `.mrpack` (Actions tab of the repository → latest build → artifact) into the Modrinth App, with **8 GB of memory** for the instance.
- [ ] Create a **Creative world with commands allowed**, seed `tilth`. A new world per session if possible: many settings are only read at world creation.
- [ ] Keep F3 handy: it shows the biome (useful for the clearings).

## Session 1: first launch and first steps

- [ ] The game is in French, the `tilth-textures`, Fresh Animations, `tilth-interface` and `tilth-ambiance` packs are active [Y1], graphics set to "Fancy" (« Détaillés »), music at 40%.
- [ ] The log shows no `tilth`, KubeJS or mixin errors. It should contain « Module ciel chargé » (sky module loaded), « Module xp chargé » (xp module loaded), « Module horizon chargé » (horizon module loaded), « Module outils chargé » (tools module loaded) [C10, X1, H1, F1, D1, L1].
- [ ] The `tilth` music pack is selected in Options → Music & Sounds → True Adaptive Music, with no "Problems detected" [M1, M2, M7].
- [ ] The title screen shows the Tilth logo over Friedrich's mountains, the right way up, with mist flowing slowly through the valleys and never over the sky or the slopes in front, and the loading screen (F3+T) is moss green with the logo and a lantern-coloured bar [I6].
- [ ] Each screen's name stands centred over its top edge on its plate, in the screen's colours (furnace, crafting table, anvil, chest renamed in an anvil, trader), nothing overlapping [I1, E1-E3].
- [ ] **First gestures**: bare hands do not break a log or stone, the hint is in French; flint from gravel, the Twigs pebble knapped into a rock, knapped stone axe, 2 planks per log with the axe, flint campfire, no wooden tools in EMI, the « Premiers gestes » quests [G1 to G12].
- [ ] No XP bar, on foot or on horseback (the jump gauge stays) [X7].
- [ ] F5: only first person and the over-the-shoulder view, free camera with the mouse.
- [ ] Open the inventory and a chest: the 3D menu from Spatial GUI, the camera moves to the shoulder. Books and the atlas stay flat.
- [ ] EMI: R and U on an item open its recipes; JEI stays invisible. Quark's inventory sorting works.
- [ ] **Pack sort**: no Chipped, Beautify! or Twigs table in EMI, Twigs cobblestone bricks (mossy, cracked, slabs, stairs, walls) present, Supplementaries sconces present, Steam 'n' Rails reduced to a few tracks and plain locometal, Every Compat without Quark or Ghost woods; no food values in tooltips [TR1, TR2, TR9].
- [ ] Vanilla animals move with Fresh Animations (pig, cow, chicken, sheep) [F2].
- [ ] Blocks and items show Vanilla Evolved's textures; grass and flowers have its models and still sway; leaves stay bushy; the moon is vanilla; a powered observer has no missing texture [Y2 to Y6].
- [ ] Stone, a torch or bread in hand: the item is drawn and the hand stays still (only a light sway); scrolling the hotbar lowers and raises the hand once per change [A1-A3].

## Session 2: the mist and the clearings

- [ ] **Quest journal** (Questlog): opens from the inventory; five chapters in French; guides complete when read; the tannery and machines quests appear with a hide or a Create part; placing the vigil fire and the lantern completes their quests [N1 to N5].
- [ ] Place a **vigil fire** (*feu de veille*) in a misty forest: « La brume se retire. » (the mist withdraws), the mist thins and the grass turns vivid within a few seconds [K1].
- [ ] F3 inside the disc and at its edge: the real biome everywhere (never `tilth:clairiere`: since `tilth` 0.8.0 a clearing no longer changes the biome); the vivid grass stops on a round edge [K2].
- [ ] A **clearing lantern** (*lanterne de clairière*) in a snowy taiga (bluish green, snow still falls), a fire in a savanna (yellow green) [K3].
- [ ] A clearing on a coast: the ocean and the beach keep their mist; a river crossing it clears [K4]. Note whether a Streams brook stays misty [W10].
- [ ] Leave the fire, go away, come back, restart: the clearing stays [K5].
- [ ] **Break** the fire, then the lantern: « La brume revient. » (the mist returns) and the mist comes back over their disc; a fire broken inside a lantern's disc changes nothing [K12, K13]. A charged dissipator broken: same over 64 blocks [K14].
- [ ] Place a lantern and watch: the grass turns green ring by ring from the lantern outwards, cloud puffs drift away and the mist thins over a few seconds; break it: the reverse, from the edge inwards [K16].
- [ ] The new fire (stone brazier, pale flame) and lantern (standing), by day and at night: neither looks like a vanilla campfire or lantern [K15].
- [ ] A clearing opened in a Regions Unexplored biome (for example a redwood forest, a bayou, a steppe): the mist lifts and the grass turns vivid, as in a vanilla biome, and F3 still shows the Regions Unexplored biome [K1, K2]. Biomes from the `tilth:peut_devenir_clairiere` tag only: a beach, a desert or an ocean refuses, with its message.
- [ ] « Clairière » (clearing) music after 30 s [K6].
- [ ] **Far view**: from a clearing on a hill, the Distant Horizons landscape shows in the distance, with no seam [K7, H3]. In the mist, no distant landscape shows, even from a summit [H2].
- [ ] **Mist ring**: from that clearing, the landscape always dissolves into the haze before about 500 blocks by day and 260 at night: no far mountain as a pale silhouette against the sky [H13, H14]. Back in the mist, no frozen relief at the edge of the terrain [H15].
- [ ] **Storms** (preset `tilth_v5`): `/weather rain` then `/weather thunder` in a snowy biome, a desert and a savanna: a white snow cloud, then ochre or golden dust, down to about 10 blocks under thunder; in a forest the grey mist closes in instead of lifting; in a clearing the view stays wide. In clear weather the desert and the savanna have a warm haze, the forest's mist ends around 40 blocks.
- [ ] Enter and leave the clearing: visibility changes smoothly [H4]. At sunrise and sunset, the colour of the distance follows the sky [H5].
- [ ] Underwater, blinded, in powder snow: no sharp landscape in the distance [H6].
- [ ] ATMOSPHERICS clouds (two layers) pass in front of the distance, with no duplicates; they take the colour of the mist and darken at night and in the rain [H9, R7]. The colours of the distance follow the seasons [H12].
- [ ] The three recipes (fire, lantern, dissipator) in EMI, with French names and tooltips [K11]. Provisional vanilla ingredients since 2026-11-13: fire = honeycomb, two smooth stone slabs, charcoal, smooth stone; lantern = two Overgeared iron plates, two glass panes, an amethyst shard. Craft both and tell whether they feel right.
- [ ] **A clearing keeps its biome**: compare a clearing with the same biome ten blocks outside: same temperature (Cold Sweat), same rain or snow, same crop humidity and growth (Ecliptic Seasons), and the grass and leaves follow the season in both [K10]. The animals of the biome spawn in the clearing as elsewhere [K18]. A night in a clearing: no monster spawns inside; they do outside and can walk in [K17]. F3+T inside a clearing: the vivid colours stay [K20].
- [ ] **Wind and light** (Where Winds Blow, Shine): a misty forest under rain then thunder, grass and leaves sway, rain slants; FPS before and after [R1, R2]. A clearing at sunset: halo on the sky and the fires, the mist still around [R3]. Snow and desert: no extra fog [R4]. An airship in flight with lanterns: no halo glitch, lanterns sway [R5]. Distant landscape stays still [R6]. Sun, moon and stars bigger and brighter [R8]; no double wind streaks or tumbleweeds [R9].

## Session 3: the night and the moon

- [ ] Clear night: a single moon, in its place, with the right phase [C1].
- [ ] `/ec setLunarEvent enhancedcelestials2defaultlunarevents:blue_moon`, then harvest moon and super moon: colour and size specific to each, with no vanilla moon behind [C2, C3].
- [ ] Rain then thunderstorm: the moon fades with the sky [C4]. Underwater and blinded: no double moon, no vanishing moon [C5].
- [ ] Frames per second at night: no visible difference with the `ciel` (sky) module turned off [C11].
- [ ] `/ec lunarForecast`: no blood moon.
- [ ] Several moons can show together if Enhanced Celestials rolls them: the Friendly Moon rule "one moon at a time" no longer applies. Note what you see.
- [ ] Nights are truly dark (More Darkness), auroras in cold biomes.

## Session 4: survival (thirst, anvil, leather, health, altitude)

- [ ] **Bag** (Reliable Backpacks): craft it from 8 tanned leather, wear it on the back (Curios slot, not the chestplate), look at it with Fresh Animations, 3D Skin Layers and the shoulder view, place it on the ground and on an airship, dye it [B1 to B4].
- [ ] **Stacks**: logs and ingots stop at 16, planks and stone at 32, seeds at 64, chests at 8, Create machines at 4, water bottles at 8; tools never stack. A Kaleidoscope pot (9 dishes) and the Create crusher (18 nuggets) empty without losing anything. Compare `config/sensible_stackables/config.toml` with the generated file [P1 to P5].
- [ ] **Sleeping bag** (Comforts): sleep in it, the spawn point does not move; is it cold in the snow (Cold Sweat)?
- [ ] **Anvil without levels**: rename, repair with ingots, merge two enchanted pickaxes. Green cost « Usure : n » (wear: n), the item can be taken. At 40: « Trop usé ! » (too worn!) [X2, X3]. Blue moon: the cost drops [X8].
- [ ] **No XP anywhere**: mine coal, smelt iron, kill, breed, fish: no orb; `/xp add @s 10 points` changes nothing [XP3].
- [ ] Immersive Enchanting: copy an ancient book, transmute it with 4 amethyst shards [XP6].
- [ ] **Tannery**: a cow and a horse give their hide; the hide dries on a tanning rack; stripping a log with an axe gives bark, two barks give tannic powder; dry hide in the tanning barrel → leather; the barrel messages are in French; EMI shows no satchel [T].
- [ ] **Health**: die 6 times → 9, 8, 7, 6, 5, then 5 hearts, with the messages [V1]. `/give @s tilth:eclat_de_vie 3`: each shard restores one heart, refused at 10 hearts [V3]. Log out and back in: health stays [V2].
- [ ] **Altitude** (Cold Sweat: Altitude): it gets colder climbing a big Tectonic summit, less so under shelter.
- [ ] Ecliptic seasons: they change the crops, with no heat stroke and no chat message.
- [ ] **Day length** (Better Days): time a day (30 min) and a night (15 min); the sky moves smoothly, also for a friend on the server; sleeping skips the night and Ecliptic counts the day; the Enhanced Celestials moon rises at dusk; the night music starts with the night [J1 to J5].
- [ ] **Pack sort, recipes**: mincer refuses ores, stone and logs; Immersive Enchanting costs without Nether items; 3 flax for 2 ropes, rope arrow of 8, pulley with a cogwheel, kitchen knives through the forge; Farm & Charm crops follow the seasons [TR4 to TR7].

## Session 5: wildlife tour

- [ ] Forest, taiga, savanna, jungle, swamp, rivers: bear, tiger, elephant, rhinoceros, monitor lizard, alligator, Naturalist snake, without their Alex's Mobs duplicates [D2].
- [ ] Oceans and reefs: Hybrid Aquatic sharks, jellyfish, orcas, octopuses; Naturalist anglerfish, blobfish, isopod, ray; Alex's Mobs giant squid [D3]. Lobster texture correct [D8].
- [ ] Flowering meadows: only Longwings butterflies [D5]. Lush caves: C&C otter, no dragonfly or snail [D4].
- [ ] No rabbit (it stays removed, Fresh Animations has no model for it) [F6].
- [ ] EMI: nothing from the removed creatures (« venaison » (venison), « crocodile » (crocodile), « homard » (lobster), « lapin » (rabbit)…) [F7, D6].
- [ ] No vanilla monsters in the Overworld, even at night, no patrol, no phantom. Never more than 8 hostile creatures around you.
- [ ] **Pack sort, wildlife**: at most 2 Naturalist lions, tigers or bears of each species near you; Icy's Paint, harness and Andalusian without their banned effects; depth charge, ominous conch and hook, ghosts and giant crabs to judge [TR8, TR10, TR12].
- [ ] **Dinosaurs** ([0021](decisions/0021-spelunkery-and-dinosaurs.md)): a Lushsaur in a lush or bioshroom cave, an Obsidiceratops in a redstone cave (`/locate structure ec_ad:obsidiceratops`), a Therimoss in a jungle, a Coralosaur nest in a warm ocean, a Sculkraptor in the deep dark. Note how each moves and looks next to Fresh Animations, whether the Obsidiceratops finds lava to drink and what it breaks.
- [ ] **Chorusoatl**: find its nest in a prismachasm (`/locate structure tilth:chorusoatl`): a floor of deepslate prismoss and one Chorusoatl. Can it fly there, is the place too dangerous, does the nest look right in the cave. Its egg hatches in full darkness.
- [ ] **Amber**: an amber crater and a big one (`/locate structure ec_ad:crater`, `ec_ad:big_crater`): whole, no floating block around, not too many per kilometre. Archie's guidebook and his trades read in French.

## Session 6: terrain, Create and airships

- [ ] Explore 3000 blocks with the atlas: large continents, mountain ranges, valleys; Regions Unexplored biomes present, and the relief reads well on a world with Geophilic's vanilla biomes [W1].
- [ ] **Floating blocks**: walk or fly over a few kilometres, caves included: no floating island, tree or isolated block, no plant resting on air. The team's measure of 2026-11-12 covered one square kilometre of one seed ([0019](decisions/0019-leaving-no-mans-land.md)); this is the first look by eye.
- [ ] **Mist in Regions Unexplored biomes**: in a forest, a taiga and a bayou the mist is thick (about 40 blocks by day); in a prairie, a steppe, a mountain and a beach it is veiled (about 72); in a cave biome it is the cave mist. No biome is clear without a clearing, none has no mist at all.
- [ ] **Colours next to vanilla biomes**: at a border between a Regions Unexplored biome and a vanilla one, grass, leaves and water do not jump; the snowy Regions Unexplored biomes let snowflakes fall.
- [ ] **French names**: blocks, items and biomes of Regions Unexplored in EMI and F3 are in French (maple = érable, pine = pin, willow = saule, redwood = séquoia, larch = mélèze, cypress = cyprès, palm = palmier, kapok = kapokier, wisteria = glycine, blackwood = bois noir); no English text left.
- [ ] **Biome rules in Regions Unexplored biomes**: horses of Icy's Better Horses in a prairie, a maple forest and a steppe; a Naturalist hedgehog or turkey in a deciduous forest, a capybara in a bayou; the wind of the plains in a prairie and of the swamps in a bayou (grass sway, gusts).
- [ ] **Spelunkery underground** ([0021](decisions/0021-spelunkery-and-dinosaurs.md)): iron, coal and copper ores in andesite, diorite, granite and tuff; rock salt under mountains, a nephrite geode deep down, cave mushrooms. Mine a diamond ore: a rough diamond, polished at the grindstone. Everything reads in French [TR13, TR14].
- [ ] **Thaw of a stream**: ice on a stream melts into flowing stream water [W11].
- [ ] Highest summit below Y 320, an airship clears the range [W5]. In winter, snow consistent with altitude [W6].
- [ ] Boat and Sable hull on a brook: the hull drifts, the airship in flight does not [W7]. Create pump and boiler on a brook [W8].
- [ ] **Dissipator**: nothing at 16 rpm; at 64 the engineer's goggles climb to 100% and a 64 disc is converted; on overload the charge resets to zero [K8]. On an airship: note what happens [K9].
- [ ] Crush raw iron ore: no XP nugget, the rest is there [XP4].
- [ ] Airship: wind sound that follows speed; a recorded route between two stations (Automated Logistics); blocks from other mods that have weight and float (Physics Compat).
- [ ] The atlas shows rails, stations and ships (Antique Transport). A lantern on a vehicle lights the ground as it passes.
- [ ] Music aboard an airship in flight [M4].

## Session 7: monuments and exploration

- [ ] The six Ancient Remnants monuments (`/locate structure ancient_remnants:…`) are whole, on the ground, not cut by water; the floating monoliths clear of any hill; no spawner left [W3, [monuments](plan/monuments.md)].
- [ ] **Guardians**: walk towards a warrior monolith, a coniferous and an oakwood monument and a hunter monolith: the guardian rises between you and the monument with a message; until it dies the Elderheart gives no blessing; after its death a message, then the blessing within 16 blocks. Note how each fight goes (Umvuthi without his throne, the Frostmaw).
- [ ] **Codex**: craft the Monolith Codex; a research scrap found in a dungeon, shipwreck map chest or igloo adds its page, which names the right biomes.
- [ ] Approach a monument: a life shard and a message, only once [V10]. Music *I Walk With Ghosts* on approach [M6].
- [ ] Ruin chests: no XP vial; life shards in the vanilla chests and archaeology sites of [sante](plan/sante.md#sources-and-rates), at the provisional rates, and how often one is found in an hour of exploring [XP5, V7].
- [ ] A Nether portal does not light; no eye of ender; `/locate structure minecraft:stronghold` finds nothing.
- [ ] Exposure camera: take and develop a photo; Exposure: Space, photograph the moon.
- [ ] **Pack sort, world**: calcite crushed into levitite powder; no Wroughtnaut chamber, Frostmaw, Umvuthana grove or Companions! structure, the monastery is still there [TR3, TR11].
- [ ] **Rain and storm** (`/weather rain`, then `/weather thunder`): pale streaks in the mist's colour, never yellow against the sun, rain curtains in the distance, the new lightning and sky flash, leaves carried by the wind and torn off by the storm, rain sounds by material, drops on the screen and drips on plants [AV1 to AV8].
- [ ] **Sunlight** (`/time set 1000` in a dense forest, then by a lake): sun rays between the leaves when facing the sun, glints on the water; footprints in snow and sand, waves on a beach, greens a little more olive [AV9 to AV11, RP1 to RP4 of [0018](decisions/0018-effets-en-pack-de-ressources.md)].

## Session 8: multiplayer server (with a friend)

- [ ] On a dedicated server: the thirst bar updates, the anvil does not roll back [S7, X4].
- [ ] Lootr: everyone finds the chest full. Monument: the second player also gets their shard [V7, V10].
- [ ] Envelope: name a mailbox at no cost, send a letter by pigeon [XP7].
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
| C (sky), X (anvil), H (far view), W (terrain), K (clearings), E (screen names), A (held items) | [plan/mod-tilth.md](plan/mod-tilth.md#in-game-tests) |
| G | [plan/premiers-gestes.md](plan/premiers-gestes.md#in-game-tests) |
| L, F (archived, No Man's Land left the pack) | [archive/plan/lune-amie.md](archive/plan/lune-amie.md), [archive/plan/faune-nml.md](archive/plan/faune-nml.md#in-game-tests); only F6 (no rabbit) and L2 (no blood moon) still apply |
| D | [plan/doublons.md](plan/doublons.md#in-game-tests) |
| M (numbered items of "To test in game") | [plan/musique.md](plan/musique.md#to-test-in-game) |
| I (numbered items) | [plan/tilth-interface.md](plan/tilth-interface.md#limits-and-in-game-tests) |
| XP (numbered items of "Remaining to test in game") | [plan/xp.md](plan/xp.md#remaining-to-test-in-game) |
| T | [plan/tannerie.md](plan/tannerie.md#what-remains-to-be-tested) |
| V (bullets in order, V1 = first) | [plan/sante.md](plan/sante.md#what-remains-to-be-tested-in-game) |
| N (quests) | [plan/quetes.md](plan/quetes.md#in-game-tests) |
| B (bags), P (stacks), J (day length) | [decisions/0012](decisions/0012-sacs-piles-jours.md#conditions-and-tests) |
| R (wind, light and sky) | [decisions/0016](decisions/0016-vent-et-lumiere.md#conditions-and-tests) |
| AV (rain, storm and sunlight) | [decisions/0017](decisions/0017-averse-et-rayons.md#in-game-checks) |
| Y (textures) | [plan/tilth-textures.md](plan/tilth-textures.md#in-game-tests) |
| TR (pack sort) | [plan/tri-du-pack.md](plan/tri-du-pack.md#in-game-tests) |

Tests without a code come from the mods added on 2026-10-25 and from [the mod registry](mods.md).
