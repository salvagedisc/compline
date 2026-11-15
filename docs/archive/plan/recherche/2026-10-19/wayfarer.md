# Wayfarer, the modpack and server of the No Man's Land team

Research of 2026-10-19 for Compline. Legend: **[V]** verified (link in the line or in the sources), **[D]** deduced from what is seen or heard in the videos, without written confirmation. "Already there" means present in Compline as described by `docs/mods.md`. **Correction of 2026-10-19:** the pack still contains NML **1.5.12**; what comes from 1.6 (ancestral tools, oath sword) only arrives with the upgrade to 1.6.1, already planned.

## Summary

1. Wayfarer is a **private modpack** (about 200 mods, NeoForge 1.21.1) made by **Monad Modding**, the No Man's Land team (Farcr, tazer…), with an **official SMP server** opened around July 2026. It is accessed through the "Wayfarer" Patreon tier; the "Discoverer" tier only gives pre-releases **[V]**.
2. A large part of what is seen in the videos comes from **NML 1.5 and 1.6**, so **Compline already has it**: the talking Friendly Moon, the alchemist ruins, the ancestral tools, the effigies, the bandages, the strata, the ancient jars **[V]**.
3. The rest mixes **public mods** (Envelope, Tree Physics, Sable Ragdolls, Tolerable Creepers, Clutter No More, Supplementaries, Farmer's Delight, Exposure, Distant Horizons) and **in-house content** that cannot be found online: Bronze Age, bloomery, max health lost on death and "life shards", rift sediment, "anomalies", dense ores, no XP **[D]**.
4. The YouTuber says the mod list is "public", but **it was not found anywhere**; it is probably on the Discord, which requires an account **[D]**. The transcript of GreenLikeable's video about the SMP could not be retrieved either.
5. To retain for Compline: **Envelope** (pigeon mail and lost letters), **the Friendly Moon quest already in NML**, **a max health that drops on death and is regained by exploring** (KubeJS), a **tannery** (KubeJS) and, to be studied, **XP removed**.

## What Wayfarer is

- **Who makes it**: Monad Modding (renamed "Monad Mods" on 2026-08-24). The Patreon page describes itself as "The Developers behind No Man's Land, Wayfarer & More". The team also makes Clutter No More, Tree Physics, Vanity, Tolerable Creepers, Glow, Mixed Litter and, since September 2026, Brewin' and Chewin' **[V]** ([Patreon](https://www.patreon.com/monad_modding), [announcement of the new name](https://www.patreon.com/monad_modding/posts/fresh-coat-of-167571384), [NML team on Modrinth](https://modrinth.com/mod/no-mans-land)). On Modrinth, the NML team consists of Farcr (owner) and tazer **[V]**.
- **What it is**: a "handmade" modpack built around NML, which touches visuals, progression and the generation of all dimensions **[V]** (description of [video 2](https://www.youtube.com/watch?v=WpwAmPJzb7A)). The YouTuber speaks of about 200 mods **[D]**. The pack updates itself: relaunching the game is enough **[V]** (video 1, 04:10).
- **Access and price**: reserved for Patreon members. The "Wayfarer" tier cost 10 USD. It went up after the SMP opened, and the "Discoverer" tier was created for those who only want the pre-releases **[V]** ([post of 2026-07-20](https://www.patreon.com/monad_modding/posts/wayfarer-rank-164357333)). The YouTuber speaks of "$20 in a single payment" **[V]**, whereas the post speaks of a subscription: **the current price is not confirmed**. He also says the payment will only last for the duration of development **[V]** (description of video 3). On 2026-10-19, the campaign has 793 members, of which 195 are paying **[V]** (Patreon public API).
- **Status**: in active development, with frequent updates **[V]**. Brewin' and Chewin' 5.0 was "already playable on the Wayfarer SMP" on 2026-09-12 **[V]** ([post](https://www.patreon.com/monad_modding/posts/brewin-and-5-0-3-169338425)).
- **The server**: no `/spawn`, render distance of about 8 chunks, 10 to 15 players connected at the time of the visit, push-to-talk voice chat. The players built a shared village with mail and a zipline, and left abandoned houses with notes ("we left for richer lands, we left our resources for those who need them") **[V]** (video 1, 61:30 to 65:00). The Monad Modding Discord has 4,162 members **[V]** (public metadata of [the invite](https://discord.gg/JW4utPut9x)).
- **Not found**: the mod list, the server address, seasons or world resets, written rules, a wiki page. The "Wayfarer SMP" server on [minecraftservers.org](https://minecraftservers.org/server/689670) is an **unrelated namesake** (vanilla client, quests and a subway).

## Feature table

"Available to us" column: NeoForge 1.21.1 checked through the Modrinth API on 2026-10-19 for each mod cited.

### World and mystery (NML)

| Feature | Source | Available to us | How to integrate it | Opinion |
|---|---|---|---|---|
| **Friendly Moon**: a talking moon, dreams, "companions" (Buddy, Frienderman), an ascension ritual; it is the main quest of the "Friendship Update" | NML 1.5 **[V]** ([posts](https://www.patreon.com/monad_modding/posts/nml-1-5-0-build-155392325), languages of the 1.6.1 jar) | **Already there** (NML 1.6.1). Dialogues translated into French **[V]** | Settle the pending decision (conflict with Enhanced Celestials, see [report 4](../verif-2026-10-18/4-monde-et-objectifs.md)) in favour of the Friendly Moon | **Yes**. It is the pack's enigmatic thread and it is free |
| **Alchemist ruins** (underground ancestral ruins, "strangely peaceful"), ruin maps | NML 1.5 **[V]** | **Already there** | Keep; slip fragments of the monolith codex into them (LootJS, already planned for the NML ruins) | Yes |
| **Ancestral tools**: a ritual pickaxe that "resonates" to listen to the earth, a pacifist oath sword, an ancient bronze chisel and bell, with dialogues | NML 1.6 **[V]** ([RC1](https://www.patreon.com/monad_modding/posts/no-mans-land-1-6-170725863)) | **Already there**. **47 texts of 1.6 without French** **[V]** (including the ritual pickaxe and the sword) | Upgrade NML to 1.6.1, then translate into `pack/kubejs/assets/nomansland/lang/fr_fr.json` (`npm run check-fr` will flag them) | Yes, to be translated |
| **Warding effigies**: no hostile creature spawns around them | NML **[V]** ([post](https://www.patreon.com/monad_modding/posts/warding-effigies-125052343)) | **Already there** | Nothing | Little effect in Compline, which has few monsters |
| **Bandages** (healing, antidote, potion effects) with animation | NML 1.5 **[V]** | **Already there**, in French | Nothing | Yes, fits survival through the environment |
| **Rock strata and rare veins**: granite, diorite and andesite rare but in large deposits | NML 1.5 **[V]** (classes `StrataStateProvider`, `OreVeinSystem` of the jar) | **Already there** | Nothing | Yes |
| **Ancient jars** that must be broken, archaeology in the ruins | NML 1.5 + vanilla **[V]** | **Already there** | Nothing | Yes |
| Parhelia, deer (antlers that fall off), tortoises, resin tap, incendiary bombs, wooden scaffolding, thatch, straw bed | NML **[V]** | **Already there** | Nothing | Yes |
| Signs that indicate a village and its distance ("700 blocks that way") | Not found (absent from NML's files) **[D]** | No | — | No: Compline's villages are ruins |

### Progression and crafting

| Feature | Source | Available to us | How to integrate it | Opinion |
|---|---|---|---|---|
| **Stone Age**: no wooden tools, pebbles and flint picked up from the ground, flint knife, bone pickaxe that also tills | In-house **[D]** (absent from NML and Modrinth) | Equivalent: **Overgeared**'s stone knapping | Already covered by Overgeared | Nothing to add |
| **Bronze Age**: tin (only above sea level) and copper melted in the terracotta crucible, ingot moulds that wear out, bronze tools equivalent to iron | In-house **[D]** | Equivalent: **Overgeared**'s moulds and alloy furnaces | Already covered | Nothing to add |
| **Iron from the bloomery** (packed mud, straw, bone ash, firestarter), bloom broken with a hammer; blast furnace in "clinker" bricks; coke steel | In-house **[D]** | Equivalent: **Overgeared**'s blast furnace and steel | Already covered | Nothing to add |
| **Dense ores** that can only be broken with a mechanical drill (Create) | In-house **[D]** | No | — | No: resembles Create Ore Excavation, removed by the maintainer |
| **Cinnabar → mercury → gyroscopic mechanism** (a part for flying contraptions) | In-house recipe on Create Aeronautics **[D]** | Feasible | KubeJS recipe | Small idea: links mining to aviation |
| **Leather to tan**: raw hide (cows, deer) + pig wax, or ash + rotten flesh; leather armour serves as the base for plated armours | In-house **[D]** (video 1, 18:00 and 52:00) | Feasible | KubeJS: raw hide as loot, leather in the cauldron or barrel | **Good light idea** (recommendation 4) |
| **Handmade paper** (bark → paste in the mortar → paper); a map requires a writing kit (ink, feather, vial) and a scribe's table | In-house **[D]** | Feasible | KubeJS on Antique Atlas's atlas recipe | Possible, to keep in reserve |
| **No XP bar** or progress displayed; enchanting exists but requires diamonds | In-house or setting **[D]** (video 1, 34:12; video 2, 10:30) | Feasible | KubeJS (remove the orbs) + hide the bar; check Immersive Enchanting, the anvil and Envelope (address paid in levels, adjustable) | **To be studied** (recommendation 5) |
| Hammers that mine 3×3 | Not found **[D]** | — | — | No |
| Farmer's Delight cooking (cutting board, sandwiches, effects) | Farmer's Delight **[V]** (video 1) | Yes | — | No: Compline's cooking is Kaleidoscope |
| Brewin' and Chewin' 5.0 (wine, ageing barrel) | Monad mod **[V]** | `4.5.0` (5.0 is in pre-release) | — | No: extension of Farmer's Delight |
| Bags that slow you down, bundles, rope, rope arrow, flax → string, lunch basket, bubble blower | **Supplementaries** **[D]** (identical names) | **Already there** (repository decision 0006, `docs/decisions/0006-supplementaries.md`) | Nothing | — |
| Camera, black-and-white film, framed photo | **Exposure** **[D]** | **Already there** | Nothing | — |
| Change the shape of a block with left Alt (stairs, slab, vertical slab, step) | [Clutter No More](https://modrinth.com/mod/clutternomore) (Monad) **[V]** | `3.0.2`, **French included** | Add a mod | Comfort; duplicates Quark's vertical slabs. Later |
| Style table: weapon and armour skins | [Vanity: Core](https://modrinth.com/mod/vanity-core) **[V]** | `5.0.3` | — | No: cosmetic |
| Safety net, scaffolding frames, sleds, shutters; rings ("solar ring"); building grid (C key) | **Not found** | — | — | — |

### Body, death and creatures

| Feature | Source | Available to us | How to integrate it | Opinion |
|---|---|---|---|---|
| **Max health drops with each death**, down to 5 hearts; it is regained with **life shards** found in **flesh geodes**, more frequent under the oceans | In-house **[D]** (video 1, 42:20; video 2, 57:56 and 63:33; video 3, 77:00 and 85:37) | Similar mod: [Unhealthy Dying](https://modrinth.com/mod/unhealthy-dying) `3.2.2` **[V]**, but there health is regained **by killing**, and it only has English and Russian | **KubeJS**: max health modifier on death, "shard" item in the geodes, the NML ruins, the clearings or as a monolith blessing | **Strong** (recommendation 3): death matters, and exploration repairs it |
| The player leaves a **body** that keeps their inventory | Probably [Sable: Ragdoll Corpse](https://modrinth.com/mod/sable-ragdoll-corpse) **[D]** | `0.3.0` alpha | — | No: alpha, and close to GraveStone, which was removed |
| Creatures in **ragdoll** on death (one has to hit a second time for the loot) | Probably [Sable: Ragdolls](https://modrinth.com/mod/sable-ragdolls) **[D]** | `0.7.5` | — | No: ragdolls were judged "out of tone" (chapter 11) |
| **Falling trees** with real physics, that float, and that can be walked on | [Tree Physics](https://modrinth.com/mod/tree-physics) (Monad, requires Sable) **[V]** | `neoforge-2.4`, English only | Would replace HT's TreeChop | No: very close to Dragged's Simulated Tree Chopper ("that's too much") |
| Creepers that release "creepies" | [Tolerable Creepers](https://modrinth.com/mod/tolerable-creepers) (Monad) **[V]** | `2.2.4` | — | No: no vanilla monsters in Compline |
| "**Anomalies**": silhouettes that copy the player and their equipment, very resistant, that drop "anomalous quicksilver" | In-house **[D]** (video 3, 10:00 and 72:00) | No | — | **No**: the YouTuber finds them "terrifying". It is horror |

### Nether (closed in Compline)

| Feature | Source | Available to us | How to integrate it | Opinion |
|---|---|---|---|---|
| Portal lit with "rift sediment" (dropped by endermites); the ignited sediment shows the nearest ruined portal | In-house **[D]** (video 3, 52:54) | No | — | No: Nether closed, and it is a structure compass |
| Redone fortresses, glowstone crystals, fire tornadoes | [Glow](https://modrinth.com/mod/glows) (Monad) **[V]** for the crystals, the rest not found | — | — | No: Nether closed |

### Living together

| Feature | Source | Available to us | How to integrate it | Opinion |
|---|---|---|---|---|
| **Pigeon mail**: letters, parcels, seals, mailbox addresses, cash on delivery; **lost letters** in a collapsed postal relay above the clouds, pigeon lofts | [Envelope](https://modrinth.com/mod/envelope) (mortuusars, the author of Exposure) **[V]**, found thanks to the letters read in video 2 (19:48 to 25:00) | `0.7.5` (2026-08-15), GPL-3. **No French** (English, Japanese, Russian, Chinese) **[V]** | Add a mod; translate about 29 KB of texts into `pack/kubejs/assets/envelope/lang/fr_fr.json`; adjust the level cost of an address and the speed of the pigeons | **Recommendation 1** |
| Zipline on the server | Probably [Ziplines: Rezipped](https://modrinth.com/mod/ziplines-rezipped) **[D]** | `1.5.3`, English and Japanese | — | Medium: rail and airships already cover the links |
| Notes and letters left in the abandoned houses | Server players **[V]** | Yes | Nothing to install: Supplementaries' notice boards or Envelope's letters are enough | Yes, to encourage among friends |
| Distant Horizons | **[V]** (video 2, 29:37) | — | — | No: already ruled out |
| Voice chat | **[V]** | — | — | No: refused (Simple Voice Chat) |
| Pack updated by itself at launch | **[V]** (video 1) | Yes | `packwiz-installer-bootstrap`, already planned server-side ([06](../../06-technique-et-serveur.md)); client-side, the private repo prevents it | Technical note |
| Very long days ("the day lasted an hour") | **Not found** **[D]** (video 1, 25:20 and 52:00) | — | Ecliptic Seasons already sets the day length | To check |
| Player names displayed only when useful | [Subtle Name Tags](https://modrinth.com/mod/subtle-name-tags) (Monad) **[V]**; **not seen in the videos** | `0.3` (2025) | Add a client mod | Small bonus: silhouettes in the mist rather than labels |

## Ranked recommendations

1. **Envelope: pigeon mail** (new mod). Compline describes a handful of players who end up living far from one another. Envelope gives them a slow, physical link, without teleportation: a pigeon takes time depending on the distance, can be hunted by a fox, and only goes through mailboxes that have a name, not coordinates. It adds a species to Wildex and an exchange of items between settlements. The lost letters of the collapsed postal relay tell, in small pieces, of those who lived there before: enigma without darkness. It has a fix for Sable, so a mailbox on an airship should work **[D]**. To do: translate into French, set the cost of an address in XP (`MAILBOX_ADDRESS_EXPERIENCE_LEVELS_COST`), decide whether the relay above the clouds stays (it slightly breaks the "almost empty") and test with Sable and Aeronautics.
2. **Keep the Friendly Moon quest of NML 1.6.1** (already there, no mod to add). It is the heart of what makes Wayfarer "enigmatic": a talking moon, dreams, peaceful ruins and tools with a mysterious past. The pending decision against Enhanced Celestials must be settled (for example, the white moon stays rare and the Friendly Moon stays in dreams), the 47 texts of 1.6 translated, and the alchemist ruins made into a place where fragments of the monolith codex are found.
3. **A max health that drops on death and is regained by exploring** (KubeJS script). Death finally has a price without one more fight, and repairing it pushes one to set out: life shards deep in caves or in geodes under the sea, in the NML ruins, as a monolith blessing, or in the calm of a clearing. No new gauge, only the hearts. Better than Unhealthy Dying, where health is regained by killing. To settle: a floor (5 hearts like Wayfarer), a loss of one heart per death, then a trial with two players.
4. **A tannery** (KubeJS script). Cows, NML's deer and horses give a raw hide. Leather requires wax, or ash and rotten flesh, and time. Leather becomes a small trade tied to fauna and husbandry, which feeds the pack, the saddle and the armours. Light, in French through our own texts. To check beforehand: the leather recipes of Overgeared and Traveler's Backpack.
5. **Remove XP** (KubeJS and hiding the bar), to be studied. Compline already pays for enchanting in materials, so the XP bar has almost no role left. Removing it takes away a gauge, makes the world less "video game" and gives more value to knowledge found (books, blueprints). Risks to list beforehand: the anvil, Quark, Create, Immersive Enchanting, Envelope and XP farms. If the cost is too high, XP is kept and the idea dropped.

Ruled out: the anomalies (horror), the dense ores (as with Create Ore Excavation), Tree Physics (as with Dragged's), ragdolls and the body (out of tone), the whole Nether (closed), Tolerable Creepers (no vanilla monsters), Brewin' and Chewin' (Farmer's Delight), Distant Horizons and voice chat (already refused).

## Addendum: the Monad Modding Discord (2026-10-19)

A later pass read the Wayfarer channel of the Monad Modding Discord. The full report, with the list of 224 mods, is in the project's shared folder (`wayfarer-discord.md`). Only what changes our conclusions is kept here.

- **Confirmed by the Monad team**:
  - no XP or enchantments, and the anvil upgrades tools with metal;
  - one heart lost at each death, down to 5, regained with gems found in underground flesh geodes;
  - hides to tan, probably through KubeJS;
  - flint, bronze, iron then steel;
  - the End is closed, and Create is only there for Aeronautics.
- **Corrected**: the "anomalies" appear nowhere on the Discord. The Friendly Moon only appears there in jokes, but it is indeed a mechanic of No Man's Land 1.6.1, verified in the jar ([lune-amie.md](../../../../plan/lune-amie.md)).
- **The Monad team's method**: an in-house mod, *Wayfarer Core*, with many mixins, plus KubeJS and LootJS for recipes and loot. It is the same approach as Compline, with our `compline` mod and our scripts.
- **What we do not do**: Farcr asks that Wayfarer not be copied, and *Wayfarer Core* will never be published. Compline takes neither code nor resources: only gameplay ideas, written in our own way (names, rates, sources), with public mods. The Monad team also says it encourages addons.
- **Ideas in reserve**:
  - a writing tool that wears out, required for signs and books;
  - wax taken from the honeycomb or from pork;
  - a cap on items on the ground instead of their disappearance;
  - gold tools with Silk Touch built in;
  - Luck replacing Fortune;
  - tools damaged in dungeons;
  - no mcfunction functions in datapacks, for performance.

## Sources

Videos by uncstatus (title and channel verified through oEmbed, description and automatic English transcript retrieved):
- Part 1: [This Mod Makes Minecraft Feel Brand New](https://www.youtube.com/watch?v=31qxa34zFz8), 67 min, published about 2 months ago.
- Part 2: [This Modpack feels like Minecraft Premium](https://www.youtube.com/watch?v=WpwAmPJzb7A), 79 min, about 1 month ago.
- Part 3: [This is My New Favorite Modpack of All Time](https://youtu.be/9PXN0emzmnI), 86 min, 2026-08-29. No chapters.

Others:
- GreenLikeable, [This Amazing SMP is Reinventing Minecraft](https://www.youtube.com/watch?v=LQYShHl1448), 2026-08-17: only the description is readable (YouTube blocked the transcript).
- Patreon Monad Modding: [page](https://www.patreon.com/monad_modding), [Wayfarer tier](https://www.patreon.com/monad_modding/posts/wayfarer-rank-164357333), [new name](https://www.patreon.com/monad_modding/posts/fresh-coat-of-167571384), [Brewin' and Chewin' 5.0](https://www.patreon.com/monad_modding/posts/brewin-and-5-0-3-169338425), [NML 1.6 RC1](https://www.patreon.com/monad_modding/posts/no-mans-land-1-6-170725863), [first Wayfarer pre-release of NML 1.5](https://www.patreon.com/monad_modding/posts/no-mans-land-out-152709685), [effigies](https://www.patreon.com/monad_modding/posts/warding-effigies-125052343). Posts reserved for members only show their teaser.
- [No Man's Land on Modrinth](https://modrinth.com/mod/no-mans-land); jar `nomansland-1.6.1.jar` opened to check items, classes and translations.
- Mods verified on the Modrinth API (NeoForge 1.21.1): [Envelope](https://modrinth.com/mod/envelope) (jar opened: languages, settings, structures), [Tree Physics](https://modrinth.com/mod/tree-physics), [Sable: Ragdolls](https://modrinth.com/mod/sable-ragdolls), [Sable: Ragdoll Corpse](https://modrinth.com/mod/sable-ragdoll-corpse), [Tolerable Creepers](https://modrinth.com/mod/tolerable-creepers), [Glow](https://modrinth.com/mod/glows), [Clutter No More](https://modrinth.com/mod/clutternomore), [Vanity: Core](https://modrinth.com/mod/vanity-core), [Brewin' and Chewin'](https://modrinth.com/mod/brewin-and-chewin), [Ziplines: Rezipped](https://modrinth.com/mod/ziplines-rezipped), [Unhealthy Dying](https://modrinth.com/mod/unhealthy-dying), [Subtle Name Tags](https://modrinth.com/mod/subtle-name-tags), [Overgeared](https://modrinth.com/mod/overgeared).
- Monad Modding Discord: only the name, description and member count, through [the invite](https://discord.gg/JW4utPut9x). The content requires an account.
- Unrelated namesake: [Wayfarer SMP on minecraftservers.org](https://minecraftservers.org/server/689670).
- Searches with no useful result: Reddit (inaccessible), CurseForge (not consulted, only cited by descriptions), "rift sediment", "flesh geode", "anomalous quicksilver", "sturdy mineral block".

Drafts, transcripts and jars: kept outside the repository.
