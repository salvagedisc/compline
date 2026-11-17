# In-game tests

List set aside on 2026-10-25 for when there is time to play. None of this has been checked with a player yet: the pack starts on a server, but nobody has played on it.

Updated on 2026-11-14 after the second cleanup ([0026](decisions/0026-second-cleanup.md)): the title and loading screens are the game's own, and the tests of Shine and of the rain, storm and sunlight effects are gone. Updated on 2026-11-13, after No Man's Land left the pack ([0019](decisions/0019-leaving-no-mans-land.md)): the checks that needed it (Friendly Moon session, its fauna, the alchemists' ruins, the Meeting Point) are gone, and checks for Regions Unexplored and Geophilic are added. Neither mod has been seen in game.

It is split into **8 sessions of about one hour**, in the order the game brings them up. Tick items as you go. When something goes wrong, note its number and attach the log (Modrinth App → Compline instance → Logs, or `crash-reports/`) to the report.

The code in brackets, such as [K1], points to the detailed test in the repository. A French copy of this checklist is kept for the team outside the repository.

## Before starting

- [ ] Import the latest `.mrpack` (Actions tab of the repository → latest build → artifact) into the Modrinth App, with **8 GB of memory** for the instance.
- [ ] Create a **Creative world with commands allowed**, seed `compline`. A new world per session if possible: many settings are only read at world creation.
- [ ] Keep F3 handy: it shows the biome (useful for the clearings).

## Session 1: first launch and first steps

- [ ] The game is in French, Fresh Animations and Better Leaves are active and no `compline-*` pack is listed, graphics set to "Fancy" (« Détaillés »), music at 40%.
- [ ] First launch: no narrator voice and no accessibility screen; creating then reopening a world shows no experimental settings warning; no Adorable Hamster Pets bell on the HUD or in the inventory, no update notice ([0024](decisions/0024-removing-the-house-resource-packs.md)).
- [ ] The log shows no `compline`, KubeJS or mixin errors. It should contain « Module ciel chargé » (sky module loaded), « Module xp chargé » (xp module loaded), « Module horizon chargé » (horizon module loaded), « Module outils chargé » (tools module loaded) [C10, X1, H1, F1, D1, L1].
- [ ] The `compline` music pack is selected in Options → Music & Sounds → True Adaptive Music, with no "Problems detected" [M1, M2, M7].
- [ ] Music: after the first silence, a track of the game plays from the new list (30 tracks, no resource pack needed) and the others follow in the clearing, at night, underground and aboard [M7].
- [ ] No « Hamster Tips » book is handed out on first login or at the first hamster seen (it can still be crafted).
- [ ] Distant Horizons writes no compatibility, render distance, garbage collector, object pool, replay or slow generation warning at start-up or in chat; only the low-memory warning may show, and only if Java lacks memory.
- [ ] The title screen and the loading screen (F3+T) are the game's own, with the Minecraft logo.
- [ ] **First gestures**: bare hands do not break a log or stone, the hint is in French; flint from gravel, the Twigs pebble knapped into a rock, knapped stone axe, 2 planks per log with the axe, flint campfire, no wooden tools in EMI, the « Premiers gestes » quests [G1 to G12].
- [ ] No XP bar, on foot or on horseback (the jump gauge stays) [X7].
- [ ] F5: only first person and the over-the-shoulder view, free camera with the mouse.
- [ ] Open the inventory and a chest: the 3D menu from Spatial GUI, the camera moves to the shoulder. Books and the atlas stay flat.
- [ ] EMI: R and U on an item open its recipes; JEI stays invisible. Quark's inventory sorting works.
- [ ] **Pack sort**: no Chipped, Beautify! or Twigs table in EMI, Twigs cobblestone bricks (mossy, cracked, slabs, stairs, walls) present, Supplementaries sconces present, Steam 'n' Rails reduced to a few tracks and plain locometal, Every Compat without Quark or Ghost woods; no food values in tooltips [TR1, TR2, TR9].
- [ ] Vanilla animals move with Fresh Animations (pig, cow, chicken, sheep) [F2].
- [ ] Stone, a torch or bread in hand: the item is drawn and the hand stays still (only a light sway); scrolling the hotbar lowers and raises the hand once per change [A1-A3].

## Session 2: the mist and the clearings

> Since [0024](decisions/0024-removing-the-house-resource-packs.md) the mist is ATMOSPHERICS' own preset, with no profile for the clearings: the tests below that expect the mist to thin or end at a given distance wait for a new mist. The rest (messages, biome, colours, far view, spawns) still applies.

- [ ] **Quest journal** (Questlog): opens from the inventory; five chapters in French; guides complete when read; the tannery and machines quests appear with a hide or a Create part; placing the vigil fire and the lantern completes their quests [N1 to N5].
- [ ] Place a **vigil fire** (*feu de veille*) in a misty forest: « La brume se retire. » (the mist withdraws), the mist thins and the grass turns vivid within a few seconds [K1].
- [ ] F3 inside the disc and at its edge: the real biome everywhere (never `compline:clairiere`: since `compline` 0.8.0 a clearing no longer changes the biome); the vivid grass stops on a round edge [K2].
- [ ] A **clearing lantern** (*lanterne de clairière*) in a snowy taiga (bluish green, snow still falls), a fire in a savanna (yellow green) [K3].
- [ ] A clearing on a coast: the ocean and the beach keep their mist; a river crossing it clears [K4]. Note whether a Streams brook stays misty [W10].
- [ ] Leave the fire, go away, come back, restart: the clearing stays [K5].
- [ ] **Break** the fire, then the lantern: « La brume revient. » (the mist returns) and the mist comes back over their disc; a fire broken inside a lantern's disc changes nothing [K12, K13]. A charged dissipator broken: same over 64 blocks [K14].
- [ ] Place a lantern and watch: the grass turns green ring by ring from the lantern outwards, cloud puffs drift away and the mist thins over a few seconds; break it: the reverse, from the edge inwards [K16].
- [ ] The new fire (stone brazier, pale flame) and lantern (standing), by day and at night: neither looks like a vanilla campfire or lantern [K15].
- [ ] A clearing opened in a Regions Unexplored biome (for example a redwood forest, a bayou, a steppe): the mist lifts and the grass turns vivid, as in a vanilla biome, and F3 still shows the Regions Unexplored biome [K1, K2]. Biomes from the `compline:peut_devenir_clairiere` tag only: a beach, a desert or an ocean refuses, with its message.
- [ ] « Clairière » (clearing) music after 30 s [K6].
- [ ] **Far view**: from a clearing on a hill, the Distant Horizons landscape shows in the distance, with no seam [K7, H3]. In the mist, no distant landscape shows, even from a summit [H2].
- [ ] **Mist ring**: from that clearing, the landscape always dissolves into the haze before about 500 blocks by day and 260 at night: no far mountain as a pale silhouette against the sky [H13, H14]. Back in the mist, no frozen relief at the edge of the terrain [H15].
- [ ] Enter and leave the clearing: visibility changes smoothly [H4]. At sunrise and sunset, the colour of the distance follows the sky [H5].
- [ ] Underwater, blinded, in powder snow: no sharp landscape in the distance [H6].
- [ ] ATMOSPHERICS clouds (two layers) pass in front of the distance, with no duplicates; they take the colour of the mist and darken at night and in the rain [H9, R7]. The colours of the distance follow the seasons [H12].
- [ ] The three recipes (fire, lantern, dissipator) in EMI, with French names and tooltips [K11]. Provisional vanilla ingredients since 2026-11-13: fire = honeycomb, two smooth stone slabs, charcoal, smooth stone; lantern = two Overgeared iron plates, two glass panes, an amethyst shard. Craft both and tell whether they feel right.
- [ ] **A clearing keeps its biome**: compare a clearing with the same biome ten blocks outside: same temperature (Cold Sweat), same rain or snow, same crop humidity and growth (Ecliptic Seasons), and the grass and leaves follow the season in both [K10]. The animals of the biome spawn in the clearing as elsewhere [K18]. A night in a clearing: no monster spawns inside; they do outside and can walk in [K17]. F3+T inside a clearing: the vivid colours stay [K20].
- [ ] **Sky without shader**: sun, moon and stars bigger and brighter [R8]; no wind streaks or tumbleweeds from ATMOSPHERICS [R9]. (The wind, the swaying plants and the campfire smoke are tested under Brumaire, SH5 and SH14.)

### The look (Brumaire)

> Brumaire is on from the first launch ([shader](plan/shader.md)), on a machine with a graphics card (not an Apple Silicon Mac). K turns it off. Note the frame rate of each profile next to the one without shader, and on the weakest machine of the group.

- [ ] **It loads** [SH1]: on a new installation the shader is on at the first world; no red message in the chat; K turns it off and the game shows the vanilla look with the mist of ATMOSPHERICS.
- [ ] **Mist** [SH2]: it lies on the ground, the tree tops and the hills come out of it; the horizon is always closed by mist, with Distant Horizons too; no mist in a cave.
- [ ] **Clearing** [SH3]: place a Vigil Fire, then a Clearing Lantern: the mist opens with the wave; from inside the view carries far, from outside the clearing is a clear pocket; breaking the block brings the mist back.
- [ ] **Light** [SH4]: shadows of trees, animals and Create contraptions; shafts of light under a canopy in the morning; lanterns and torches glow at night; the moonlit night stays readable.
- [ ] **Wind** [SH5]: grass, flowers, crops and leaves move with the wind and bend when a gust passes; grass and flowers lie down under the player's steps and around a passing animal; rain and snow slant with the wind, more under a storm; list the plants of the biome mods that stay still.
- [ ] **Smoke** [SH14]: a campfire's smoke rises then bends with the wind; three campfires side by side make one larger plume; a soul campfire's smoke is bluish and bluer as it rises; under a storm it lies down more; a Farm & Charm stove on a campfire still hides its smoke.
- [ ] **Water** [SH6]: waves, the bottom fades with depth, the sky is mirrored at a low angle; under water the view is blue-green.
- [ ] **Water, reflections and bottom** [SH13]: on a river at noon, the banks and trees are mirrored in hard-edged patches that follow the waves; shallows show the bottom with moving threads of light, deep water its grey-green colour; at sunset a glint path on the side of the sun. Dive: the world above shows through a circle at the surface, the bottom is lit by the threads. Look through a window at a lake (glass in front of water) and note anything odd. Note the frame rate with `Reflets du paysage` at 0, 20 and 32.
- [ ] **Rain** [SH15]: in a plain under rain, thin light-grey streaks; after a minute the ground is darker and shines at a low angle, puddles on flat ground mirror the trees and the sky, with rings from the drops; far away, faint rain streaks over the landscape; under trees and roofs the ground stays dry; in a desert or a snowy biome nothing gets wet. After the rain the ground dries within a few minutes. In a storm, a bolt is a bright line and the landscape flashes. Note the frame rate with `Reflets des flaques` at 0 and 12.
- [ ] **Snow, sand, ice** [SH16]: in a snowy taiga in clear weather, the tops of the spruces are white on their bright needles with a ragged band down their sides, ledges and fences are capped, snow keeps its grain in the sun and a few grains glitter; a frozen lake is light blue and mirrors the trees; packed ice shines without ripples. In a desert at noon the sand stays warm, not white, a few grains glitter and the horizon trembles a little. Leaving the snowy biome, the snow on the trees fades within about ten seconds. `Neige sur les arbres` at 0 removes it.
- [ ] **Anti-aliasing** [SH17]: looking at the edge of a roof, a fence or a tree against the sky, `Anticrénelage` at Fort smooths the stair steps of the silhouettes, while the pixels of the textures stay sharp; at Non the steps come back. Note the frame rate at Non and Très fort.
- [ ] **Image** [SH18]: at noon in a plain or a forest, the near air is clear and the grass is a warm olive green, not bluish; sand in a desert at noon stays yellow and snow keeps its grain; shadows under trees stay green rather than grey; at dawn and dusk the mist still lies in the valleys and glows toward the sun. `Verts olive` at 0 gives back the greens of the game.
- [ ] **Coloured light** [SH19]: at night, a torch lights the ground in orange, a soul lantern in blue, lava in red, a sea lantern in bluish white, a verdant froglight in green; between a torch and a soul torch the two colours mix; a wall stops the colour as it stops the light. Holding a soul lantern in a dark room lights the floor in blue, a torch in orange. `Lumière colorée` at Non gives back the warm light everywhere. Note the frame rate with the setting on and off.
- [ ] **Shader rain** [SH20]: under rain, thin grey drops one pixel wide fall slanted by the wind, none under a roof or a dense tree; standing under an overhang, the rain falls just beyond its edge; within a minute or two the rain grows heavier then lighter as showers pass, and the far curtains thicken on one side. In a snowy biome, small square flakes drift down. `Pluie du shader` at Non gives back the game's rain. Note the frame rate in rain with the setting on and off.
- [ ] **Footprints** [SH21]: walk across sand and a snowy field: a trail of prints, left then right, with a darker hollow (bluish on snow) and one lit edge; a wolf or a fox leaves its own trail; flying over the snow leaves none. After about four minutes the trail has faded, sooner under rain. `Traces de pas` at Non removes them. Note the frame rate with it on and off.
- [ ] **Frost and leaves** [SH22]: in a taiga before sunrise in clear weather, grass and leaves under the open sky are speckled with white rime; by mid-morning it is gone; in the plains there is little, in a desert none. Walking along a hedge or on top of a tree, the leaves next to the player quiver and lean away; a sheep against a hedge does the same. Particular's falling leaves still fall. `Givre` at Non removes the rime.
- [ ] **Under water** [SH23]: dive in a lake by day: near the surface the water is clear blue-green and darkens deeper down; looking up toward the sun, rays fan down and ripple, with dark gaps under a boat or a dock; small specks drift and twinkle. At night the rays are gone. `Neige marine` at Non removes the specks. Note the frame rate under water with it on and off.
- [ ] **Create, Sable, Aeronautics** [SH7]: a water wheel, a moving contraption, an airship and levitite are lit and shaded like the rest; note anything translucent drawn in the wrong order.
- [ ] **Camera** [SH9]: in the over-the-shoulder view, the character still fades when the camera comes close to it (on the bench a spectator's head stayed opaque).
- [ ] **Sky and air** [SH10]: at noon a pale blue sky, a soft halo around the sun; at sunset the horizon reddens on the side of the sun and the far mist glows with it; at night a dark blue sky with the moon, landscape still readable, darker at new moon. In a swamp the mist turns olive, in a snowy biome bluish, in a desert thin and sandy, each change over a few seconds.
- [ ] **Shadows and hollows** [SH11]: the shadow of a tall tree is soft at its edges, the shadow of a fence post sharp; under a canopy, at the foot of a wall and in a doorway the light is darker, never a dark halo around a tree seen against the sky; turning `Occlusion d'écran` to 0 removes it. Note the frame rate with it at 0, 10 and 16.
- [ ] **Clouds** [SH12]: by day, separate cumulus with darker bases drift with the wind; their shadows cross the ground; under rain the sky closes and turns grey; at sunset they glow on the side of the sun; at night the moon lights them. From an airship above 230, fly through a cloud. Note the frame rate with `Nuages en volume` at 0, 16 and 24.
- [ ] **Settings** [SH8]: the four profiles change the frame rate (note it for each, from Très léger to Beau); the names are in French; "Verts olive" at 0 gives back the greens of the game.

## Session 3: the night and the moon

- [ ] Clear night: a single moon, in its place, with the right phase [C1].
- [ ] `/ec setLunarEvent enhancedcelestials2defaultlunarevents:blue_moon`, then harvest moon and super moon: colour and size specific to each, with no vanilla moon behind [C2, C3].
- [ ] Rain then thunderstorm: the moon fades with the sky [C4]. Underwater and blinded: no double moon, no vanishing moon [C5].
- [ ] Frames per second at night: no visible difference with the `ciel` (sky) module turned off [C11].
- [ ] `/ec lunarForecast`: no blood moon.
- [ ] Several moons can show together if Enhanced Celestials rolls them: the Friendly Moon rule "one moon at a time" no longer applies. Note what you see.
- [ ] Nights are truly dark (More Darkness), auroras in cold biomes.

## Session 4: survival (thirst, anvil, leather, health, altitude)

- [ ] **Cooking by hand** (Farmer's Delight, Hands-On Delight): craft a cooking pot and a stove, cut a cabbage and raw beef on the cutting board with a flint knife (tomatoes and onions go whole into the pot), cook a soup in the pot over the stove while stirring, with Farm & Charm's tomato and onion; in EMI, Farm & Charm's stove, pot, roaster and crafting bowl are gone and its dishes (goulash, nettle tea, farmer's bread, roasted corn) show in Farmer's Delight's cooking pot, butter and dough at the workbench; a sandwich, a cookie and a pumpkin pie can no longer be made at the workbench and are assembled on the cutting board; bread: dough kneaded on the board (wheat and a water bucket), then baked in the skillet; the stirring stars give extra servings (judge whether it is too generous). Fill the skillet, then a keg. Texts in French. Then Sneak + use on the pot: the Farmer's Delight window still opens.
- [ ] **Vanilla stations with the hands** (Cinematic Interactions): craft on the workbench top, load a furnace by hand, repair at the vanilla anvil, cut at the stonecutter, read a book on a lectern, sit at a campfire; Sneak + use opens the usual window. Texts and settings screen in French; Punchy and the shoulder view stay correct; Overgeared's smithing anvil keeps its own interface.
- [ ] **Merged items** (`fusions.js`): Farm & Charm's mincer turns a porkchop into Farmer's Delight's bacon and wheat into Create's wheat flour; a Hybrid Aquatic fish is cut with a knife on the board (fish meat and bone meal), not at the workbench; a log cut on the board gives Tannery's bark, which makes tannic powder; a boar drops Tannery's fat; Farmer's Delight's safety net and rope fence take Supplementaries' flax rope; in EMI no second bacon, minced beef, dough, flour, rope, copper nugget or diamond shard; Farm & Charm's « Morue au beurre et aux légumes » reads differently from the vanilla « Morue cuite ».
- [ ] **Kitchen and Create** (Create: Central Kitchen): a deployer holding a knife cuts on the cutting board, a mechanical arm feeds the cooking pot and takes the dishes out.
- [ ] **Bag** (Reliable Backpacks): craft it from 8 tanned leather, wear it on the back (Curios slot, not the chestplate), look at it with Fresh Animations, 3D Skin Layers and the shoulder view, place it on the ground and on an airship, dye it [B1 to B4].
- [ ] **Stacks**: logs and ingots stop at 16, planks and stone at 32, seeds at 64, chests at 8, Create machines at 4, water bottles at 8; tools never stack. A Farmer's Delight cooking pot (its stack of servings) and the Create crusher (18 nuggets) empty without losing anything. Compare `config/sensible_stackables/config.toml` with the generated file [P1 to P5].
- [ ] **Sleeping bag** (Comforts): sleep in it, the spawn point does not move; is it cold in the snow (Cold Sweat)?
- [ ] **Fishing** (Starcatcher, [0034](decisions/0034-starcatcher-fishing.md)): craft a hook and a bobber, then Starcatcher's rod; in EMI no vanilla fishing rod, the carrot on a stick asks for Starcatcher's rod. Fish a river, a lake and the sea: the minigame opens, a perfect catch gives more, a miss can lose the fish; Hybrid Aquatic's fish come out where they live (no piranha in a pond) [PE1]. Open the catalogue: entries for Hybrid Aquatic, Naturalist (bass, catfish at night, anglerfish, blobfish, piranha) and Spawn (bluefish), none for the End [PE2]. Put a Hybrid Aquatic hook on the rod, swap attachments in the tackle box [PE3]. Below y = -40, fish in lava with a rod that can: Nether catches such as the magma fish come out [PE4]. Build an aquarium and put a catch in it, a fish plaque on a wall: the Naturalist and Spawn fish show their model [PE5]. Cut a catch on the board into a fillet and cook a Starcatcher's Delight dish in the pot; no selling bin in EMI; texts in French [PE6].
- [ ] **Anvil without levels**: rename, repair with ingots, merge two enchanted pickaxes. Green cost « Usure : n » (wear: n), the item can be taken. At 40: « Trop usé ! » (too worn!) [X2, X3]. Blue moon: the cost drops [X8].
- [ ] **No XP anywhere**: mine coal, smelt iron, kill, breed, fish: no orb; `/xp add @s 10 points` changes nothing [XP3].
- [ ] Immersive Enchanting: copy an ancient book, transmute it with 4 amethyst shards [XP6].
- [ ] **Tannery**: a cow and a horse give their hide; the hide dries on a tanning rack; stripping a log with an axe gives bark, two barks give tannic powder; dry hide in the tanning barrel → leather; the barrel messages are in French; EMI shows no satchel [T].
- [ ] **Health**: die 6 times → 9, 8, 7, 6, 5, then 5 hearts, with the messages [V1]. `/give @s compline:eclat_de_vie 3`: each shard restores one heart, refused at 10 hearts [V3]. Log out and back in: health stays [V2].
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
- [ ] **Chorusoatl**: find its nest in a prismachasm (`/locate structure compline:chorusoatl`): a floor of deepslate prismoss and one Chorusoatl. Can it fly there, is the place too dangerous, does the nest look right in the cave. Its egg hatches in full darkness.
- [ ] **Amber**: an amber crater and a big one (`/locate structure ec_ad:crater`, `ec_ad:big_crater`): whole, no floating block around, not too many per kilometre. Archie's guidebook and his trades read in French. Ask Archie about the Obsidiceratops and the Chorusoatl: he names the redstone caves and the prismachasm. Tame a Lushsaur and craft its four pieces without killing one: the master advancement and the Lush Totem recipe come; his Lush Core trades open after the taming. Lay a Chorusoatl egg on prismoss in the dark near prismarite: it hatches.
- [ ] **Zoo** (Archie's guidebook): J opens the guidebook on the zoo; from Archie's pages, the green bookmark leads back to it. Look at a fox, photograph a cow, tame a wolf, catch a cod in a bucket: one stamp each time, with a sound. With three stamps, stay a minute near the species: it is mastered. The stamps stay after death [ZO1 to ZO6].
- [ ] **Armour patina** (`patine` module): wear iron armour and walk, sprint over dirt, fight: the boots get dirty first, earth stains then a few rust specks; jump into water and it is clean in a few seconds. A zombie's armour is already dirty; dyed leather keeps its colour [PA1 to PA5].

## Session 6: terrain, Create and airships

- [ ] Explore 3000 blocks with the atlas: large continents, mountain ranges, valleys; Regions Unexplored biomes present, and the relief reads well on a world with Geophilic's vanilla biomes [W1].
- [ ] Rename a book "Antique Atlas" at an anvil: it takes the look of the atlas and shows the open map when held in first person. Two players on a server see each other's explored map and markers once they share it.
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
- [ ] Approach a monument: a life shard and a message, only once [V10]. On approach the music fades out [M6].
- [ ] Ruin chests: no XP vial; life shards in the vanilla chests and archaeology sites of [sante](plan/sante.md#sources-and-rates), at the provisional rates, and how often one is found in an hour of exploring [XP5, V7].
- [ ] **Edge of the world**: on foot, by boat and in flight the mist turns you back at 6,000 blocks from the centre, with a warning 64 blocks before; an airship is not stopped yet, a message asks to turn back; sailing around the continent, the coast stays away from the edge; north cold, south hot [Z1 to Z6].
- [ ] A Nether portal does not light; no eye of ender; `/locate structure minecraft:stronghold` finds nothing.
- [ ] Exposure camera: take and develop a photo; Exposure: Space, photograph the moon.
- [ ] **Ores and progression**: no diamond ore except rough diamonds beside deep lava; no steel ingot or template in chests; 3 iron + 1 coal → 3 crude steel; a diamond template when a coniferous monument wakes; black steel from steel and magnetite, the master anvil, diamond mined only with a black steel pickaxe; dinosaur armour forged at the anvil with its Cold Sweat bonus, no Chorusoatl teleport; ancient debris beside deep lava and orichalcum through Overgeared's chain; a master pickaxe gives more ore [PG1 to PG10, AN1 to AN5].
- [ ] **Pack sort, world**: calcite crushed into levitite powder; no Wroughtnaut chamber, Frostmaw, Umvuthana grove or Companions! structure, the monastery is still there [TR3, TR11].
- [ ] **Rain and storm** (`/weather rain`, then `/weather thunder`): rain curtains in the distance, the new lightning and sky flash, leaves carried by the wind and torn off by the storm, rain sounds by material, drops on the screen and drips on plants [AV1 to AV8].
- [ ] **Sunlight** (`/time set 1000` in a dense forest, then by a lake): glints on the water; footprints in snow and sand, waves on a beach [AV9 to AV11].

## Session 8: multiplayer server (with a friend)

- [ ] On a dedicated server: the thirst bar updates, the anvil does not roll back [S7, X4].
- [ ] Lootr: everyone finds the chest full. Monument: the second player also gets their shard [V7, V10].
- [ ] Envelope: name a mailbox at no cost, send a letter by pigeon [XP7].
- [ ] `/dh pregen` on the server, then a player explores: no holes in the far landscape. Note the generation speed with spark [W9]. (Only if a server owner adds Chunky: its `start` and the same check [H11].)
- [ ] Simple Backups: one backup after an hour of play, and one when everyone leaves.
- [ ] On Apple Silicon Mac: one hour of play with the far view and no Java crash [H10].
- [ ] spark (`/spark profiler start --force-java-sampler` on a Mac, async-profiler crashes there) for 10 min with 2 or 4 players: share the link so the cost can be reviewed [M10].

## After the tests

Once the results are in:

1. Fix what broke, then apply the **updates held back**: Spatial GUI 1.9, Supplementaries 4, Friends & Foes 5, Envelope 0.8, Icy's Better Horses 2.1 (the revive XP cost has to be replaced), Cold Sweat 2.4.4 (once Altitude follows it), Lithium (to remove if it freezes with Sable), Subtle Effects and More Darkness (client-side settings to read in game).
2. Compare the hand-written Quark config with the one the game generates.
3. Publish version `0.1.0` (tag and release), then pregenerate the permanent world with `/dh pregen`.

## Repository housekeeping

- [ ] Delete the old branches, all already merged into `main`: `test/agriculture`, `test/faune`, `test/structures`, `feat/idea-mods`, `feat/safety-net-and-updates`, `feat/tectonic-streams-distant-horizons`. Branches can only be deleted by the repository owner; the cloud environment gets a 403. On GitHub: *Branches* tab, trash-can icon.

## Where the codes come from

| Code | Detailed list |
| --- | --- |
| C (sky), X (anvil), H (far view), W (terrain), K (clearings), A (held items), Z (world edge and disc), ZO (zoo), FA (house wildlife), AN (black steel) | [plan/mod-compline.md](plan/mod-compline.md#in-game-tests) |
| G | [plan/premiers-gestes.md](plan/premiers-gestes.md#in-game-tests) |
| SH (house shader pack) | this file, session 2; context in [plan/shader.md](plan/shader.md) |
| L, F (archived, No Man's Land left the pack) | [archive/plan/lune-amie.md](archive/plan/lune-amie.md), [archive/plan/faune-nml.md](archive/plan/faune-nml.md#in-game-tests); only F6 (no rabbit) and L2 (no blood moon) still apply |
| D | [plan/doublons.md](plan/doublons.md#in-game-tests) |
| M (numbered items of "To test in game") | [plan/musique.md](plan/musique.md#to-test-in-game) |
| I (numbered items) | [archive/resource-packs/compline-interface.md](archive/resource-packs/compline-interface.md#limits-and-in-game-tests) (pack removed; nothing of it still applies) |
| XP (numbered items of "Remaining to test in game") | [plan/xp.md](plan/xp.md#remaining-to-test-in-game) |
| T | [plan/tannerie.md](plan/tannerie.md#what-remains-to-be-tested) |
| V (bullets in order, V1 = first) | [plan/sante.md](plan/sante.md#what-remains-to-be-tested-in-game) |
| N (quests) | [plan/quetes.md](plan/quetes.md#in-game-tests) |
| B (bags), P (stacks), J (day length) | [decisions/0012](decisions/0012-sacs-piles-jours.md#conditions-and-tests) |
| R (wind, light and sky) | [decisions/0016](decisions/0016-vent-et-lumiere.md#conditions-and-tests) |
| AV (rain, storm and sunlight) | Removed by [0026](decisions/0026-second-cleanup.md); [decisions/0017](decisions/0017-averse-et-rayons.md#in-game-checks) is kept as history |
| PE (fishing) | this file, session 4; context in [decisions/0034](decisions/0034-starcatcher-fishing.md) |
| PG (ores and progression) | [plan/progression.md](plan/progression.md#in-game-tests) |
| TR (pack sort) | [plan/tri-du-pack.md](plan/tri-du-pack.md#in-game-tests) |

Tests without a code come from the mods added on 2026-10-25 and from [the mod registry](mods.md).
