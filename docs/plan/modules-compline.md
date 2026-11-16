# Planned modules of the `compline` mod

Eight modules approved by the team on 2026-10-26, to be written after the six that exist ([mod-compline.md](mod-compline.md)). They all come from the same idea: **the mist hides, life lifts it, and what is hidden does not need to be drawn**. Each design below was compared with what other games and mods do well; the full research is in [recherche/2026-10-26/](../archive/plan/recherche/2026-10-26/).

Nothing here is written yet. Each module will follow the rules of the existing ones: one module, one switch in the config; loaded only if the mods it needs are present; no copied code; its own section and tests in [mod-compline.md](mod-compline.md) once it exists.

## Overview

| Module | What the player experiences | Taken from | Size |
|---|---|---|---|
| `voile` | Nothing visible: creatures, animated blocks and particles beyond the mist are no longer drawn | Sodium's fog occlusion, the lesson of Entity Culling | Small |
| `zoo` | Archie's four stamps (spot, photograph, tame or house, master) for every species, inside Archie's guidebook; enclosures measured; zoo milestones change the clearing | Menagerie, Pokémon Legends: Arceus, Planet Zoo, Red Dead Redemption 2 | Large |
| `vie` | A clearing grows with the variety of what is done in it, in a visible wave, without a gauge | Dragon Quest Builders, Valheim, Ōkami | Medium |
| `fanal` | An amber glow pierces the mist at night up to 600 blocks; players find their way home by it | Journey, Sea of Thieves, Death Stranding | Medium |
| `eclaircie` | A few mornings per season the whole map can be seen; red sky, barometer and a sound announce it the day before | Enhanced Celestials, The Long Dark | Medium |
| `sillage` | The dissipator on an airship opens a clear bubble that travels with it | Valheim's wisplight | Medium |
| `echos` | Very rarely, a lantern-bearing silhouette walks towards a place worth finding, then dissolves | Dark Souls, Outer Wilds, Hollow Knight | Medium |
| `survivants` | Twelve written survivors, each with a story; hold out the lantern, they live only in clearings | Spiritfarer, Kingdom Two Crowns | Large |

## Order of work

1. `voile`: small, measurable, and it lightens the pack before anything is added.
2. `zoo` then `vie`: together they give the pack its long-term goal and make the clearings grow with it.
3. `fanal`: navigation without coordinates, useful as soon as one leaves the base.
4. `eclaircie`: the great Distant Horizons spectacle, once the far view passes the H tests.
5. `sillage`, after test K9 (the dissipator on a ship).
6. `echos` and `survivants`, the most delicate to tune, last; their look depends on the animation study ([recherche/2026-10-26/](../archive/plan/recherche/2026-10-26/)).

The guide book (see the [roadmap](../feuille-de-route.md)) gets a page for each module as it lands.

## Shared pieces

- **One clarity distance.** `voile`, `eclaircie` and `sillage` read the same client-side value: the effective end of the mist, including a clear morning or a nearby ship bubble. Things reappear on their own when the mist lifts.
- **The clearing is the hub.** `vie`, `zoo` and `survivants` store their data per clearing (`SavedData`), fed by events, never by scanning chunks. A clearing is identified by its source block (fire, lantern, dissipator).
- **Sodium recomputes visibility whenever the fog end changes**: every transition (clear morning, growth wave, ship bubble) moves in steps (every 5 ticks), not every frame.

## `voile`: do not draw what the mist hides

**Why.** Beyond 60 to 120 blocks nothing can be seen, but the game draws everything. Entity Culling did this work and had to go: its open issue #299 confirms that players on a Sable ship vanish and reappear.

**Design.**
- First, check Sodium's *Use Fog Occlusion*: its code reads `RenderSystem.getShaderFogEnd()` and the fog colour, and culls terrain only if the fog alpha is exactly 1.0. Log both values with ATMOSPHERICS active. If they qualify, terrain culling is free.
- Then one distance, `fogEnd + margin` (8 to 16 blocks), applied to entity rendering (`EntityRenderDispatcher.shouldRender`), block entity rendering and particle spawning.
- Mandatory exceptions: anything on a Sable sub-level, anything inside a clearing, the player's own vehicle and passengers, leashed animals.
- Particles: Particle Core or Smart Particles may already be enough; otherwise the same distance in `ParticleEngine`.

**To verify first.** The Sodium log above; frame rate with spark in a misty forest and near a busy base, before and after; an airship in flight.

## `zoo`: the stamps of Archie's guidebook

Revised on 2026-11-15: the zoo lives **inside Archie's guidebook** (Archie's Dinosaurs), not in a ledger or a book of its own. The maintainer wants one book for all the fauna, with the guidebook's four stamps and its way of following creatures applied to every species, and no extra menu ([0021](../decisions/0021-spelunkery-and-dinosaurs.md)).

**Lessons.** Menagerie (a zoo modpack on Forge 1.20.1) goes beyond animals (cave, prehistoric and abyssal creatures, a "challenge" section for predators) and pays research papers according to the creature's difficulty; but enclosures are ticked by hand, nothing measures their quality, and almost nothing helps find species. Pokémon Legends: Arceus rewards observing behaviours; Planet Zoo says what is wrong and why; Red Dead Redemption 2 counts study without killing and excludes missable animals; Animal Crossing's museum changes with each donation and its empty cases show what is missing; Monster Hunter's tracks lead to the beast.

**How the guidebook works** (read in the 1.0.4 jar).
- Each dinosaur has four stamps on the guidebook's badge page: **spot**, **kill**, **tame**, **master**. A stamp is lit when the vanilla advancement `ec_ad:<stamp>_<dino>` is done; the page asks the client advancement list.
- **Spot** uses the trigger `ec_ad:spotted_entity`: any living entity within 25 blocks (scaled by its visibility), in the player's view and line of sight. It fires for every creature, not only dinosaurs, so a spot advancement can be written for any species in data.
- **Master** needs the other stamps plus the creature's gear and unlocks the recipe of its totem, which can only be placed with that advancement. Since [#60](https://github.com/salvagedisc/compline/pull/60) mastery no longer asks for a kill, except for the Sculksaur, and Archie's material trades open with the tame stamp.
- Archie's trades are data (`ec_ad:archie_trade` recipes) and can require an advancement: he can reward stamps.
- The guidebook's pages are XML read from the resources (`assets/ec_ad/guidebook/<language>.xml`): categories, labels, images, recipes, popups. A resource pack can add or replace categories, but the pages are static.
- The badge page is hard-coded: an enum of the seven dinosaurs and, for each, the stamps it shows (the Sculksaur has no tame stamp). Adding a species there needs code.
- The mod is All Rights Reserved, without public source: overriding its data and resources is fine, copying its code, textures or whole guidebook files into the repository is not.

**Built in `compline` 0.17.0** (module `zoo`, details in [mod-compline](mod-compline.md#zoo-module)).
- **Four stamps per species**, kept per player (a data attachment, kept on death), not as advancements: hundreds of advancements would fill the advancement screen and its toasts, and the guidebook is the only place they are read.
  - **Apercevoir**: the species in the player's field of view (a 110° cone), unobstructed, within 25 blocks, checked every half second; Archie's rule, applied to every creature.
  - **Photographier**: the species in a photo taken with an Exposure camera (`FrameAddedEvent`, its entities in frame).
  - **Apprivoiser ou héberger**: tamed (`AnimalTameEvent`), a young one born of the player (`BabyEntitySpawnEvent`), or caught in a water bucket. Only for species that can be kept (tameable, breedable or bucketable); the others have no third stamp, like the Sculksaur has no tame stamp.
  - **Maîtriser**: the other stamps of the species, then one minute of observation (in view within 16 blocks). No kill anywhere.
  - A new stamp shows in the action bar with a pen sound; a mastered species gets a line in the chat.
- **The species list** is computed by the server at start: every mob that spawns naturally in an Overworld biome (biome modifiers of the mods included), minus the tag `compline:zoo/absents` (the vanilla monsters In Control refuses, checked by a test against its config) and `compline:zoo/exclus` (the dinosaurs, which keep Archie's own page, and the villagers, traders and golems); `compline:zoo/inclus` adds a species by hand. A mob seen outside the list (born of a structure, an egg) joins it when spotted, and the world remembers it. The habitat comes from the nature of the creature (monster, water, flying, land), unless a tag `compline:zoo/terre|eaux|ciel|creatures` says otherwise.
- **In the guidebook**: a fourth bookmark at the bottom, Archie's gold one turned green with his footprint, right of the clasp (the mirror of Archie's bookmarks, clear of his page arrows), opens a zoo category placed before the credits: a welcome spread (Archie's word, what each stamp asks, the habitats with what has been spotted and mastered) then the species of each habitat, seven per spread laid out like Archie's stamp page, each with its creature drawn in 3D and fitted to its frame (an ink silhouette and "???" until spotted), its name on a ribbon and its stamps, with what each asks on hover. Tabs on the left lead to each habitat. The images are Archie's own, read from his mod at launch and reworked (text and icons erased, recoloured per habitat, our signs added: Exposure's camera for the photo, silhouettes of game items for the habitats); nothing of the mod is copied. A key (J by default, "Ouvrir le guide d'Archie") opens the guidebook without holding it.
- **Archie's pages corrected**: the Obsidiceratops and Chorusoatl texts that still named the Nether and the End are replaced at load time from `assets/compline/archie/guide/<language>.json` (positions in the XML, each checked against a word of the original text); the paintings of the old biomes stay.

**Not built yet.**
- Pages per species with a text (behaviour, habitat, food), in the guidebook's style.
- Housing in an enclosure measured by the module (four criteria: usable space, preferred terrain, shelter, food, each with its cause in plain words; group size as the social setting; no manual validation), as a second way to earn the third stamp.
- **Tracks**: rare tracks (feathers, prints, nests) that fade over time and a vague hint in French help find a species without a compass, in the spirit of Archie's "I've tracked every dino in these lands". Archie is the keeper's voice: one line from him at each milestone.
- Players of the same clearing add up to one zoo; a species counts once. Milestones (5, 15, 30, 50 species mastered) change the place, not the inventory: the clearing grows (`vie`), a music opens, a monument shows its empty plinths. Nothing missable blocks completion.
- Other mods behind the guidebook: Alex's Mobs' animal dictionary stays hidden; Questlog keeps one chapter that points to the guidebook and its milestones.
- Kept out: catching rare creatures, money, visitors, random crates, cleanliness or disease gauges.

**Risks.** The guidebook graft (two mixins on Archie's `GuidebookData` and `CategoryTabs`) targets a closed, very young mod and may have to be redone when it updates; the module logs and skips what it cannot find. If the mod goes away, the stamps stay in the players' data and only the display has to be rebuilt.

## `vie`: the clearing grows when it is lived in

**Lessons.** Dragon Quest Builders' town level only goes up and each level is a small celebration; Valheim's comfort and Animal Crossing's island rating count variety, not quantity; Ōkami and Ori make nature return before the player's eyes. To avoid: the sculk catalyst, fed by deaths, hence by mob farms.

**Design.**
- The life counter is fed by **variety**: the first harvest of each plant, the first birth of each species, each zoo species, one night slept per player. Each source has a daily cap per clearing.
- Against farms: only actions of a present, active player count; no hopper, redstone or fake player; a birth counts if the animal was fed by hand and while the clearing is under an animal threshold; no death counts.
- Spaced thresholds (for example 20, 50, 100, 180, 300), +8 blocks each, up to the source's cap (48 for the fire, 96 for the lantern). The counter never goes down.
- Crossing a threshold is an **event**: the mist recedes in a ring over 30 to 60 seconds, the grass turns green, petals rise, a note of the clearing music opens. No number on screen: the vigil fire's flame grows brighter, and one line ("La clairière respire plus largement.") warns that a threshold is near.
- Life belongs to the clearing, with a per-player daily contribution cap.

**To verify first.** Ring conversion in small batches per tick with the code of `clairiere`.

## `fanal`: a glow that pierces the mist

**Lessons.** Journey's mountain and Elden Ring's Erdtree show that a single, always-visible landmark orients a whole journey; Sea of Thieves' lanterns tell from afar that someone is there; Death Stranding limits how many player structures it shows. The vanilla beacon beam is cut by fog, and no mod found shows a light through mist: we need our own rendering.

**Design.**
- Built on high ground (wood, Overgeared forged iron, glass lens), it burns dry wood, oil or charcoal: one load lasts a night or two; unlit, it is a dark block.
- **Amber** glow (readable in blue-grey mist), a diffuse column of 60 to 120 blocks, wider at the base, with a slow breathing that flickers as fuel runs low.
- Visible from 300 to 600 blocks, full at night, faint by day, stronger the denser the mist around the viewer. Everyone sees it; only the nearest few are drawn. Heard only up close (crackling, wind). For an airship at night, above the mist layer, it becomes a sharp point.
- It does not lift the mist: it guides, the clearings open.

**To verify first.** The server sends the list of lit beacons; the client draws them after the ATMOSPHERICS sky, without fog, as `ciel` does for the moon, including beyond loaded chunks and over Distant Horizons. This is the first prototype to build.

## `eclaircie`: clear mornings

**Lessons.** Enhanced Celestials' rare moons are awaited events; The Long Dark's auroras make players go out on purpose; Advanced Weather's barometer shows a trend and a confidence, while Random Weather had to remove its own because random weather cannot be forecast; Sea of Thieves announces big changes by sound first.

**Design.**
- Two or three times per season, the mist lifts everywhere for a morning, over 5 to 10 minutes, then falls back. More in winter, fewer in autumn.
- Since it is scheduled, the forecast can be exact, in three signs: a red sky at dusk the day before, the **barometer** (block or item) rising with its trend arrow, a discreet sound at sunset.
- During it only: an advancement "Vu d'en haut" (seen from above) taken at altitude, a short musical theme, once.

**To verify first.** One clarity value from 0 to 1, held by the server and synchronised, driving ATMOSPHERICS, Distant Horizons and `voile`. The far view of unvisited regions must exist: Distant Horizons generation on both sides and a pregeneration with `/dh pregen start overworld 0 0 <radius>` (Chunky, which left the pack on 2026-11-14, leaves holes in the LODs according to the Distant Horizons documentation; a server owner may still add it).

## `sillage`: the dissipator on board

**Lessons.** Valheim's wisplight opens a small clear bubble with a soft edge, the strongest wins where two overlap, and the radius stays small to keep tension; Sea of Thieves removed free clarity from a simple fire on board. No mod opens a moving clear bubble.

**Design.**
- On the ground the dissipator converts terrain for good. On a Sable airship it opens a **clear bubble** of 64 blocks with a 16-block soft edge that travels with the ship, without converting anything. It costs stress; two dissipators do not add up.
- Two steps: first the fog end follows the camera's distance to the bubble (enough for the pilot and passengers); then, if worth it, a real bubble seen from outside, drawn in Sodium's fog shader.

**To verify first.** Read the ship pose from Sable on the client; keep the distance Sodium uses for culling wide enough (`max(mist end, distance to bubble + radius)`), otherwise the terrain of a distant ship's bubble would be culled.

## `echos`: silhouettes of the world before

**Lessons.** Dark Souls and Elden Ring phantoms pass without any effect on the world; Outer Wilds' signalscope gives a sound that grows when aimed right, never a distance; Ghost of Tsushima's wind shows the exact target, which is the limit to avoid; Hollow Knight's Cornifer is heard humming before he is seen.

**Design.**
- Very rare (once every 2 to 4 hours of play in dense mist, never twice at the same place), a dark faceless silhouette appears at 40 to 80 blocks: a lantern bearer, a slow cart, a menhir's shadow. One small warm light makes it gentle. Humming or muffled steps, never a sudden sound.
- It walks a few dozen blocks towards a place worth finding, then dissolves into the mist if approached within 15 blocks, without turning round. It sometimes stops by a detail on the ground: a lost Envelope letter, an unlit lantern. Never a line, a distance or an exact target.
- Six to eight kinds, each tied to a kind of place (the cart leads to ruins, the lantern bearer to monuments): players learn to read them. More frequent on full-moon nights, never in daylight. Each player sees their own, without collision; a command turns them off.

**To verify first.** Target choice on the server without searching structures too far; client-side rendering (see the animation study).

## `survivants`: survivors to bring back

**Lessons.** Spiritfarer's few passengers each have a story, a taste and moments unlocked by care; Kingdom Two Crowns recruits with a single gesture; Dragon Quest Builders' residents arrive as the town grows; Death Stranding is about connecting isolated shelters; The Long Dark and Outward make encounters precious by rarity. To avoid: endless trade rerolls (Easy Villagers), discontent gauges (Frostpunk), refugee floods (RimWorld).

**Design.**
- Ten to twelve survivors written by hand in French, each with a former trade, an object, a taste (a plant, a music, a corner of the clearing) and three story moments unlocked by time spent in the clearing and a simple offering.
- Found without a marker, from a discreet hint near a ruin: thin smoke, a lit window, a song, a cloth tied to a branch. Their place depends on the world seed. Sitting by an ember, a survivor looks up when a player approaches with a lit lantern; **holding out the lantern** is the only gesture to make them follow.
- They settle and work only **in a clearing**; outside they crouch and fall silent. They never die: abandoned, they return to their ember. Each brings one trade and one fixed, filtered trade table (no Mending, nothing that bypasses Overgeared, no reroll), favouring comfort and exploration over power.
- One survivor per life threshold, four to six per clearing at most, with a world cap. A very lively clearing sometimes attracts one, announced by smoke on the horizon. Nobody owns a survivor.

**To verify first.** In Control! blocks vanilla villagers: survivors must be our own creature or a clean exception. Their model and animations: see the animation study.

## Optimisation outside the mod

| Lead | What it does | Status |
|---|---|---|
| Sodium fog occlusion | Terrain hidden by the mist is not drawn | To check with ATMOSPHERICS (alpha 1.0) |
| Distant Horizons on the server | The server generates the far view once and sends it | Generation on both sides; `/dh pregen` (Chunky only if a server owner adds it) |
| Far-view resolution | Lower `maxHorizontalResolution` | Cost undocumented; judge by eye |
| Settings | Lower `entityDistanceScaling`; `view-distance` 10, `simulation-distance` 6, `allow-flight=true`; 6 GB server, 8 GB per player | Usual values, confirm with spark |
| Lithium | Out if it freezes the world with Sable | Watched |
