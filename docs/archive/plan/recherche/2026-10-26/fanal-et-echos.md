# Fanal and Échos: design references

Research of 26 October 2026 for the `fanal` (light beacon) and `echos` (distant silhouettes) modules of the `compline` mod. Everything cited comes from pages opened during the research. What could not be verified is flagged.

## 1. What exists in Minecraft

**The vanilla beacon beam.** The wiki says the beam rises up to 2048 blocks high. It is visible from any loaded chunk, up to 512 blocks in Java. Since 25w05a, it is displayed beyond 16 chunks, up to the render distance, and looks thicker from afar. Stained glass colors the beam, opaque blocks cut it. The page says nothing about fog or clouds. Conclusion: vanilla gives a landmark, but it is a straight, sharp, "gadget" line, which contradicts a world of mist. We need a light of our own.

**Beam mods.** Decorative Beacon (Forge 1.20.1) places beams without a pyramid, colorable, which can pass through blocks. Ytgld Better Beacon changes the beam rendering. Lantern Glow adds a large rotating light for lighthouses. None of these pages talks about fog or long range. I found no mod that does "glow visible through the mist". The field is open, but there is no model to copy. I could not verify the Supplementaries flares (the search gave nothing), nor the Create lamps, nor any "smoke signals" mods.

**Mist and distance.** Atmospherics (client only) sets fog, sky, clouds, haze and stars per biome, with transitions and day/night settings. Distant Horizons extends the view to 256+ chunks and records the LODs. Its page does not talk about fog. FogControl (Forge 1.20.1, so off target) claims that the Distant Horizons fog "continues where vanilla fog ends", seamlessly. Point to test: a glow drawn by our code must stay visible where the mist is densest, including beyond the vanilla render distance.

**Navigation without interface.** Antique Atlas 4 is client only, 1.21.1 NeoForge, built on Surveyor. Structures only appear once looked at or walked on. Sharing goes through `/surveyor share`. An option lets you require the atlas item in the inventory. Structure markers are configurable, so everything can be turned off. This is the right frame: the map fills with what you have seen. Waystones gives a teleport network between activated blocks, with scrolls and stones. It is the opposite of diegetic travel: distances are skipped, so the mist no longer exists. Explorer's Compass locates structures anywhere and shows the info on the HUD. I found no written review of these compasses. The flaw is structural: an arrow toward a known target removes the question "where?", which is the whole game. For Dead Reckoning, nothing verified.

**Non-hostile apparitions.** Cloud Revive (NeoForge 1.21.1) has "Wandering Spirits". Natural spirits do not glow, do not interact with anything, are invulnerable, have no collision, and are purely decorative. Ghost Mod adds friendly, tameable ghosts. None does what `echos` wants: suggest a place. No "rare ambient event" mod found. Herobrine, Alex's Caves, The Graveyard, Sculk and Ancient City echoes: not verified here, so not used.

## 2. Other games, the 9 most instructive

### Valheim, Mistlands (Wisplight)
**What it does.** A thick mist covers the biome. The Wisplight is a small blue orb that follows the player and clears the mist around them. The Wisp Torch clears the mist around a point, with no fuel, but only at ground level.
**Good.** The mist is a real challenge, and the tool is a light.
**To avoid.** Players complain about frustration (Steam threads). The EIP guide advises placing map markers everywhere, because you get lost. The WispReach mod exists precisely to reduce the mist.
**For Compline.** The fanal must be the opposite: the mist hides the ground, but not the light. Option: a small radius of clearer mist around the beacon.

### Outer Wilds, the signal scope
**What it does.** You listen to signals, follow them, find the source. According to the creative director (Mobius blog, 2016), the tool "never worked 100%".
**Good.** Separate channels per source. A "natural" channel that rewards exploration. A distance indicator, described as a "game changer".
**To avoid.** Players aimed at nearby rocks while the signal was on the other side of the planet. Direction without distance equals confusion.
**For Compline.** A signal must have its own sound or visual identity. For `echos`, give distance through intensity (opacity, volume), never through a number.

### Dark Souls and Elden Ring
**What it does.** Messages with a limited vocabulary, placed where they were written. Bloodstains that replay the last seconds of a dead player as a ghost. Ghosts only show actions, have no effect on the world, and appear with a certain probability (arXiv 2204.10949). The Erdtree is a landmark visible from almost everywhere (mapmaster.io calls it the "main compass").
**Good.** Non-interactive ghosts, so no risk. Anonymous camaraderie.
**To avoid.** Messages sometimes misleading, humor misunderstood, possible toxicity.
**For Compline.** An echo can replay a trace, never interact. A closed vocabulary prevents abuse. The single big landmark (menhir, tree) helps orient without a map.

### Journey
**What it does.** The mountain is visible all along and serves as a goal. An anonymous companion, without text or voice, only a chant (Wikipedia, Game Developer).
**Good.** No names, no chat, no competition. Communication through a single sound creates a bond. Minimalism: remove everything that does not serve the emotion.
**To avoid.** A "karma" system was cut because it was too preachy and exploitable.
**For Compline.** The fanal is the private "mountain" of each group: a fixed point that says "come home". No numeric reward for lighting it.

### Death Stranding
**What it does.** Structures, signs and paths of other players appear in your world. Heavily walked paths get smoother. "Likes" reward the builder.
**Good.** You feel accompanied without direct contact. The amount of visible content is capped, which limits clutter and abuse (Gfinity, CheatCC).
**To avoid.** Secondary source only, no interview with the game's director found.
**For Compline.** A friend's fanal is a trace left for others. A cap on visible beacons prevents a saturated sky.

### Breath of the Wild
**What it does.** Unique places (Lost Woods, ruins, mazes), towers, Korok seeds.
**Good.** The Game Developer article says the mystery comes from anticipation, not from discovery. It recommends breaking an established pattern, at the right moment, and keeping the fiction mysterious but the controls predictable.
**To avoid.** Fixed templates, signs that are too clear, random loot unrelated to the place. Repeated Koroks become predictable. I could not verify the "triangle rule" or the "allure of the unknown" in an open source.
**For Compline.** An echo must stay rare and varied. If you always see the same one, the charm dies.

### Sea of Thieves
**What it does.** The lantern lights up and is used to light beacons on the large islands, "visible from afar on the seas" (official community wiki). Some riddles ask you to raise the lantern at a landmark.
**Good.** The physical act (lighting, raising) is the game gesture.
**To avoid.** I could not verify the Skull Fort clouds or the ship lighting at night.
**For Compline.** Lighting the fanal is a ritual, by hand, with a visible fuel.

### Ghost of Tsushima
**What it does.** The wind guides, particles fall toward the objective. Golden birds lead off the paths. The game's creative director: "the wind is under the player's control" and "curiosity is constantly rewarded". The wind came from an idea of atmosphere, not of navigation.
**Good.** The guidance is atmosphere. Ignoring the guide still pays.
**To avoid.** The wind is an exact guide to the objective, so almost a compass. The echo must not be one.
**For Compline.** Strong idea: echoes born from atmosphere (silhouette, lantern), and following is a choice.

### Hollow Knight, Cornifer
**What it does.** The cartographer hums. The sound grows stronger as you approach. Papers on the ground lead to him. If he has left, you find a letter that points to his shop.
**Good.** A soft sound clue, a visual thread, a way out without punishment. In Deepnest, he does not hum, because he is afraid (wiki).
**For Compline.** An echo can hum. A distant hum plus a detail on the ground (a lost letter) is enough as "proof" that you are following the right thread.

### Shadow of the Colossus
**What it does.** The sword raised to the sun draws a beam that narrows toward the target. It is not a radar, you must stop to use it, and it fails under dense cover (Game Developer). Each defeated colossus leaves a ray toward the clouds, a permanent landmark.
**Good.** The cost of use (stopping, risk) makes the clue precious. The author notes that the game "wants you to get a little lost".
**For Compline.** A light beam as a landmark, with a cost. The rays left after a feat make luminous "tombs": this could be considered for memorial fanals.

### Others, briefly
Subnautica: beacons are placed by the player, a pod's signal can disappear (Steam threads, no design source). Proteus: music is born from the landscape, no goal, nothing about apparitions in the opened article. Dear Esther: not verified.

## 3. Recommendations for the fanal and echos modules

### Fanal
1. **Color.** A warm amber, between wood fire and honey (around #FFB45A at the core, #FF9A3C at the edge). The lens glass can tint it slightly. No white, no rainbow: amber is readable in the blue-gray mist.
2. **Shape.** Not a sharp line like the vanilla beam, but a diffuse glow in a low column, wider at the base, that stretches upward and fades. Drawn additively, with no fog function, so that it stays visible where the mist swallows everything else. To prototype: the vanilla beam rendering is cut by the fog, so we need our own rendering in the sky.
3. **Flicker.** Slow, irregular, two superposed frequencies (a breathing of about 6 s plus a slight tremor). A fanal with low fuel flickers more. This way the glow tells the state of the beacon without text.
4. **Height.** Column of 60 to 120 blocks above the lens, not 2048. Higher, it looks like a technical beacon. Lower, it disappears behind the terrain. So building on a high point really matters.
5. **Range.** 300 to 600 blocks in misty weather, farther in clear weather. Beyond the render distance, the glow is drawn client-side from a list of fanals received from the server, not from the loaded world. Since Distant Horizons does not show animated blocks, we must not depend on it. To test with the Distant Horizons fog.
6. **Visible at night and only in the mist.** By day, a very faint halo. At night, the glow is full. The denser the mist the player is in, the stronger it is, like a lighthouse.
7. **Fuel.** Dry wood, oil or coal. A load lasts one or two game nights. Unlit, the fanal is a dead, dark block, so there is upkeep, like a real lantern. No infinite fuel.
8. **Who sees it.** Everyone, like a fire in the night. But the builder gets a private detail: the light sound of their own beacon. A team map option is useless here, the glow is the goal.
9. **Cap.** Like Death Stranding, limit to a few fanals displayed at once (the nearest or the most intense), so as not to saturate the sky.
10. **Airship.** At night, the glow is an aerial lighthouse. Out of the mist layer, higher up, it becomes a sharp point. It must stay visible above the mist, so drawn without occlusion by the Atmospherics cloud or haze. Add a "flash" version that can be seen from very high.
11. **Clearings.** The fanal does not clear the mist (otherwise the mystery is lost). If there are clearings (sort of a Wisp Torch), the glow is simply sharper there. No mist pushed back around the beacon.
12. **Sound.** A distant fire crackle and a breath of wind audible only within a few dozen blocks. The fanal is seen from afar, heard up close.

### Echos
1. **Frequency.** Very rare: one apparition per player every 2 to 4 hours of play in dense mist, with a minimum delay of several game days between two in the same place. The Game Developer essay on BOTW insists: breaking a pattern must come after it is established. So echoes must surprise, not repeat.
2. **Appearance.** Dark silhouette, faceless, at 40 to 80 blocks, seen through the mist: lantern bearer, slow cart, shadow of a menhir that barely moves. Avoid any sharp human shape. A single small warm glow (lantern) makes the scene soft rather than threatening.
3. **Not a compass.** Never a line, a directed particle or a repetition. The echo walks toward a place a few dozen blocks away, then fades if not followed. The target place can also be just "next to" the direction, never exact. This is the Ghost of Tsushima flaw (exact guide) to avoid.
4. **Not scary.** No sudden sound, no close-up, no gaze. The echo moves away, never approaches. It fades by dissolving into the mist when you are within 15 blocks, without turning round. A hybrid of Cornifer and the Dark Souls ghosts: calm presence, no effect on the world.
5. **How it guides.** A few dozen blocks, direction only, no distance shown. "Pursuit" option: if it stops in front of a detail on the ground (a paper, an unlit lantern), that is the proof. The player can give up at no loss.
6. **Audio.** A very soft hum, or muffled steps, or the creak of a cart. The sound rises a little on approach (Cornifer idea), but stops short at the fade. No stinger.
7. **Lore.** The lost letters of Envelope: the echo carries or drops a letter. The Monolithes (Monoliths): the echo walks along one. La Lune amicale (the Friendly Moon): the echo appears better on a full moon night, never in broad daylight. The fanal: the echo may pass near an unlit fanal, like a ghost that remembers.
8. **Multiplayer.** Each player sees their own echoes (client-side, deterministic by seed and player). Two close friends can see the same one if they follow it together. No conflict: an echo has no collision or interaction (Cloud Revive shows this is feasible).
9. **Diversity.** At least 6 to 8 variants, each tied to a target type (unvisited ruin, monument, letter). If players learn "the cart always leads to a ruin", that is fine, because it is a reading code, not a compass.
10. **Tuning.** A command or a gamerule to turn echoes off, and a test mode that shows them often, to calibrate.

## Limits of this research
Not verified: Supplementaries flares, Create lamps, smoke mods, Dead Reckoning, Herobrine, Alex's Caves, The Graveyard, Ancient City echoes, Skull Fort, Dear Esther, BOTW "triangle rule". The range, height and frequency figures above are design proposals, not facts found.

## Sources
- https://minecraft.wiki/w/Beacon
- https://www.curseforge.com/minecraft/mc-mods/decorative-beacon
- https://www.curseforge.com/minecraft/mc-mods/fogcontrol
- https://www.mado-hosting.com/minecraft-mods/atmospherics
- https://modrinth.com/mod/distanthorizons
- https://modrinth.com/mod/antique-atlas-4
- https://www.curseforge.com/minecraft/mc-mods/waystones
- https://modrinth.com/mod/cloud_revive
- https://eip.gg/valheim/guides/10-tips-to-master-the-mistlands/
- https://commonsensegamer.com/valheim-mistlands-how-to-see-through-mist/
- https://www.mobiusdigitalgames.com/news/separating-the-signal-from-the-noise
- https://ar5iv.labs.arxiv.org/html/2204.10949
- https://mapmaster.io/games/elden-ring/guides/Landmark
- https://www.gamedeveloper.com/game-platforms/in-depth-hunicke-and-chen-talk-tgc-s-intriguing-new-i-journey-i-
- https://en.wikipedia.org/wiki/Journey_(2012_video_game)
- https://www.gfinityesports.com/article/how-death-stranding-2s-strand-type-online-system-redefines-co-op
- https://www.gamedeveloper.com/design/designing-for-a-sense-of-mystery-and-wonder
- https://seaofthieves.wiki.gg/wiki/Lantern
- https://geekculture.co/geek-interview-ghost-of-tsushima-nate-fox
- https://www.hollowknight.wiki/w/Cornifer
- https://www.gamedeveloper.com/design/navigation-in-shadow-of-the-colossus
- https://en.wikipedia.org/wiki/Proteus_(video_game)

Pages read only through a search summary (not opened): Ytgld Better Beacon, Lantern Glow, Ghost Mod, Explorer's Compass, Subnautica (Steam threads).
