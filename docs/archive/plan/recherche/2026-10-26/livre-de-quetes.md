# Quest and guide book for Tilth

Research of 2026-10-26. Target: Minecraft 1.21.1, NeoForge 21.1.265. Versions and dates come from the Modrinth API, queried the same day. The jars of Heracles, GuideME, Modonomicon and KubeJS were opened to check the details. What was not verified is marked "to test".

## 1. Which mod does the maintainer have in mind?

The maintainer talks about a "not FTB Quests" mod that opens a book-shaped menu where quests and guides are set. Two serious candidates.

- **Heracles**, by Terrarium (Team Resourceful). On Modrinth, it is now called **Odyssey Quests** (slug `odyssey-quests`). It is a tree-shaped quest book, with an in-game editor. It has a "Quest Book" item and an "Open Quests" key, filed in the "Project Odyssey" category. It is probably this one, since "Odyssey" and "book" are joined there.
- **Modonomicon**. A book with categories and entries, with a tree view like quests. It is already in the pack.

Important correction. Plan 03-civilisation (§3.7) says that "Heracles and Odyssey have no 1.21.1 version". This is no longer true. Odyssey Quests 2.0.0 for NeoForge 1.21.1 came out on 2026-10-04. 2.0.3 dates from 2026-10-18. All are **beta**.

"Lavender" is not a quest mod. It is a guide library (Fabric and Quilt only, last updated 2025-01-20). It is the base of Knowlogy Book. On NeoForge, Sinytra Connector would be needed. To rule out.

## 2. Comparison

| Mod | 1.21.1 NeoForge | Latest version | License | Quests with tasks | Rich guides |
|---|---|---|---|---|---|
| Modonomicon | yes | 1.120.7, 2026-09-25 (stable) | MIT + CC-BY-4.0 (visuals) | no, only unlock conditions | yes |
| Patchouli | yes | 93, 2026-03-08 (stable) | CC-BY-NC-SA-3.0 | no | yes |
| Heracles / Odyssey Quests | yes | 2.0.3, 2026-10-18 (beta) | MIT | yes | simple text |
| GuideME | yes | 21.1.20, 2026-10-22 (stable) | multiple free licenses | no | yes, the richest |
| Hardcore Questing Mode | yes | 5.20.0, 2026-10-23 (stable) | LGPL-3.0 | yes, life-based | no |
| JustQuests | yes | 0.4.1, 2026-10-23 | GPL-3.0 | yes, simple | no |
| Akashic Tome | yes | 1.8-30, 2026-03-17 | CC-BY-NC-SA-3.0 | no | no (groups books) |
| Knowlogy (Lavender) | via Connector | 0.9.1 beta, 2025-07-09 | GPL-3.0 | no | yes |
| FTB Quests | yes | absent from Modrinth | | yes | |

FTB Quests cannot be found on Modrinth (the slug gives 404). The repository already noted this. Better Questing is also absent from Modrinth.

### Modonomicon
- Data-driven book (datapack), categories, entries, pages. Two views: tree or index.
- Page types read in the jar: text, image, item showcase, entity, recipes (crafting, smelting, forge, etc.), multiblock. The multiblock has a preview in the page and in the world (doc "Multiblocks").
- **Unlock conditions** (doc "Unlock Conditions"): `advancement`, `entry_read`, `entry_unlocked`, `mod_loaded`, `and`, `or`, `true`, `false`, `category_has_visible_entries`. A single condition per entry, so `and` and `or` to combine. They apply to a page, an entry or a category.
- Our Java mod can register its own condition (doc "Custom Conditions") with `LoaderRegistry.registerConditionLoader`.
- No dedicated KubeJS. But KubeJS gives advancements (see §5), so the link exists.
- No native tasks or rewards. An entry can run a command (class `BookCommand` in the jar).
- Progress saved **per player** (table by UUID), not per team.
- Format: JSON in a datapack, path `data/<ns>/modonomicon/books/` (read in `BookDataManager`). The doc advises against writing the JSON by hand and recommends datagen, because formatted text is painful there. Texts go through language keys with extended Markdown.
- Item: `modonomicon:modonomicon`, tied to a book. Test commands: `/modonomicon reload`, plus commands to reset unlocks (classes present in the jar).
- French: the interface is already translated in the pack (`pack/kubejs/assets/modonomicon/lang/fr_fr.json`, 216 keys).
- Limit: the 1.21.1 doc is "no longer maintained", but the mod is (updated on 2026-10-13).

### Patchouli
- Classic guide books, multiblocks with a "Visualize" button. Locking by `advancement` on an entry or a page (doc "Advancement locking"). Locked pages are hidden with no hint.
- Translation by language folder (`fr_fr/` next to `en_us/`). Reload with Shift + click on the pencil.
- No quests, no tree view. Already present for other mods (Alex's Mobs, hamster, Ecliptic Seasons).
- Giving the book: hidden advancement + loot table (doc "Giving new").

### Heracles / Odyssey Quests
- Quest tree, in-game editor, quests distributed through the `config` folder. Depends on Resourceful Lib, already in the pack.
- Tasks read in the jar: advancement, biome, interaction with a block, entity or item, dimension change, item gathering, entity kill, location, recipe, statistic, structure, XP, composite, checkbox (`CheckTask`, test on the player's NBT).
- Rewards: item, loot table, XP, command, choice.
- **Our Java mod can add a task**: `QuestTasks.register(QuestTaskType)` is public. There is also a team registry (`TeamProviders`).
- Quest hidden until its parents are done ("Hidden Until" setting) and unlock notification.
- Interface: no French translation in the jar (en, zh, es_mx). 145 keys to translate. Quest texts are entered directly in the files: in French, no key needed.
- Risks: beta three weeks old, teams to test (per-player progress by default, to check), exact path of the quest files to confirm in game.
- Quest text is short. No multiblock, no recipes in page.

### GuideME
- The AE2 guide library, usable alone. Pages in **Markdown** in a resource pack (`guides/<space>/<path>/*.md`), 3D scenes from structure files, inline recipes, links between pages, G key on an item tooltip.
- Item `guideme:guide` tied to a guide by a component. Given by command (the doc says nothing about a recipe).
- Translation by `_fr_fr/` folder with fallback to the default page. The interface has 36 keys, in English only.
- **No locking by progress** in the doc consulted. Everything is visible from the start, which ruins the mysteries.
- No quests.

### Others
- **Hardcore Questing Mode**: very recent 1.21.1 port. Life-based quests, in-game editor. Our pack already keeps the heart per death: to avoid, it doubles the rule.
- **JustQuests**: quest book on the J key, datapack quests, teams. Young (4,100 downloads), designed for generated quests. Not our tone.
- **Akashic Tome**: a single item that holds the documentation books of other mods. Useful if we keep several Patchouli books. Compatibility with Modonomicon: to test.
- **Modopedia** (MIT, 1.1.11), **Guide** (deaddiesel, 1.7.0) and **Ageratum**: recent alternatives to Patchouli, small communities. No reason to prefer them.
- **Chapters** and **History Stages**: locking of recipes and items, not books.
- **Lexicon**: encyclopedia of vanilla Minecraft, all rights reserved. Off topic.

## 3. How other packs do it

- **Create: Above and Beyond**: FTB Quests (CurseForge page opened, version 1.16.5, Forge, last updated 2021-12-15).
- **Craftoria, ATM10**: packs guided by a quest book (the search result on Craftoria mentions more than 2000 quests). None of the opened pages names the mod; the maintainer cites them as FTB Quests packs, which I could not confirm.
- **Create: No Man's Land** (PhasMC pack, Modrinth, 1.21.1) and **Wayfarer dark**: I opened their file index. No quest or guide mod. Gameplay is learned through Ponder and through play.
- Consequence: atmosphere packs without quests have no book. Packs with quests use FTB Quests, not available here.

## 4. What makes a good book

These principles come from "progressive disclosure" (Wikipedia: information arrives when the task needs it) and from the tone wanted in `docs/vision.md`.

- Short pages: a title, two or three sentences, an image or a diagram. No wall of text. French is longer than English.
- One idea per page. One gesture to do per entry.
- Images and multiblock previews rather than description.
- Progressive discovery: an entry appears when the player needs it, not before.
- No spoilers on the mysteries (monuments, Lune amie (Friendly Moon), echoes). The book says how to do it, not what it is. Mystery entries open after the find, with a vague sentence.
- A visible clue for locked entries ("Requiert : allumer un fanal" (Requires: light a fanal)), without revealing the content.
- No markers or coordinates (forbidden by the vision).
- Never hide a vital survival rule (thirst, cold): these entries are always visible.

## 5. Recommendation

**One mod for the book: Modonomicon.** It is stable, under a free license, already in the pack, already translated on the interface side, with multiblock preview (useful for the fanal) and unlock by advancement. Unlocking by advancement plays the role of quests: the player does a gesture, the entry opens.

**Role 2, optional: Heracles (Odyssey Quests)**, only if the team wants a real list of objectives with checkboxes and rewards. It is in beta and its French translation is still to be done. I propose a trial on the test server before deciding. Do not add GuideME: two books, two translation systems, and no progress.

Do not install Hardcore Questing Mode, JustQuests, Lavender or Knowlogy.

### Structure of the book "Carnet du voyageur" (Traveler's Notebook)

One category per chapter, seven to ten entries maximum each. Order and opening condition:

1. **Arriver** (Arriving): getting oriented, the inventory, first gestures. Open from the start.
2. **La brume et les clairières** (The mist and the clearings): why the mist, how to lift it (watch fire, lantern, dissipateur (dissipator)). Opens on seeing it or on crafting the first block.
3. **Le fanal** (The beacon): building, feeding, reading its glow. Multiblock page. Opens at the first lantern. Module `fanal` not yet written.
4. **Survivre** (Surviving): thirst, temperature, health, bag. Always visible.
5. **Cuisine et tannerie** (Cooking and tannery): Opens at the first hide or the first furnace.
6. **Forge** (Forge): Overgeared, quenching. Opens at the first ore.
7. **Create et dirigeables** (Create and airships): gears, stress, Aeronautics, dissipator on board. Opens at the first wheel or the first craft.
8. **Zoo et bestiaire** (Zoo and bestiary): Wildex, enclosures, photos. Opens at the first species.
9. **Monuments** (Monuments): vague page, filled in by discovery.
10. **Lune amie** (Friendly Moon): a single sentence, the rest after the first encounter.

A "Journal des modules" (Module journal) tab can be added for each module delivered (the roadmap plans for it).

## 6. Technical path

All files go in `pack/kubejs/`, which KubeJS loads as a datapack and resource pack (already the case for the rest of the pack).

| What | Where |
|---|---|
| Book, categories, entries | `pack/kubejs/data/tilth/modonomicon/books/carnet/...` (start from the Modonomicon demo book, exact structure to copy) |
| French texts | `pack/kubejs/assets/tilth/lang/fr_fr.json`, keys with Markdown |
| Images | `pack/kubejs/assets/tilth/textures/gui/carnet/` (small, one per page) |
| Unlock advancements | `pack/kubejs/data/tilth/advancement/carnet/*.json`, without a `display` block, so no toast and no pollution of Better Advancements |
| Fanal multiblock | multiblock JSON in the same datapack |

**Unlocking by progress.**
1. For most entries, an advancement with a vanilla trigger is enough (item obtained, location, consumption).
2. For a particular gesture, a KubeJS script calls `player.unlockAdvancement('tilth:carnet/fanal')` (method `kjs$unlockAdvancement`, verified in the KubeJS 2101.7.2 jar).
3. For the modules of the `tilth` mod, the Java mod registers a criterion trigger, for example `tilth:evenement`, called when the fanal is lit. The datapack then keeps the logic. Another option: a custom Modonomicon condition.
4. The entry carries `"condition": {"type": "modonomicon:advancement", "advancement_id": "tilth:carnet/fanal"}`.

**Giving the book.** Hidden advancement on the first tick with a loot table `advancement_reward` (method of the Patchouli guide "Giving new", to adapt to the Modonomicon item). Exact name of the book component: to check with the autocompletion of `/give`.

**Translating.** All text in `fr_fr` keys, never in component JSON. `npm run check-fr` reads the jars of the pack; add the check of our book's keys to the script. Follow the glossary of `docs/plan/traduction.md`. For Heracles, if kept: `pack/kubejs/assets/heracles/lang/fr_fr.json` (145 keys) and quest texts in French directly in the files.

**Test.** `/modonomicon reload`, then the unlock reset commands to replay the progression. To add to `docs/tests-en-jeu.md`: opening the book, locked entry with a readable tooltip, fanal multiblock, page length in French, connection time with the book.

**Performance.** The book data is synced to clients at connection (message `SyncBookDataMessage`). No measurement done. Keep the book under about a hundred entries and images small.

## Sources

- https://modrinth.com/mod/modonomicon
- https://api.modrinth.com/v2/project/modonomicon (also patchouli, guideme, odyssey-quests, akashic-tome, knowlogy, modopedia, guide-mod, hqm, justquests, lavender, chapters, kubejs)
- https://klikli-dev.github.io/modonomicon/
- https://klikli-dev.github.io/modonomicon/docs/1.21.1/intro
- https://klikli-dev.github.io/modonomicon/docs/1.21.1/getting-started/
- https://klikli-dev.github.io/modonomicon/docs/1.21.1/getting-started/step-by-step-with-datapack/
- https://klikli-dev.github.io/modonomicon/docs/1.21.1/basics/
- https://klikli-dev.github.io/modonomicon/docs/1.21.1/basics/unlock-conditions/
- https://klikli-dev.github.io/modonomicon/docs/1.21.1/basics/unlock-conditions/advancement-condition/
- https://klikli-dev.github.io/modonomicon/docs/1.21.1/basics/page-types/
- https://klikli-dev.github.io/modonomicon/docs/1.21.1/multiblocks/
- https://klikli-dev.github.io/modonomicon/docs/1.21.1/advanced/
- https://klikli-dev.github.io/modonomicon/docs/1.21.1/advanced/custom-conditions/
- https://klikli-dev.github.io/modonomicon/docs/1.21.1/advanced/localization/
- https://vazkiimods.github.io/Patchouli/docs/patchouli-basics/getting-started
- https://vazkiimods.github.io/Patchouli/docs/patchouli-basics/advancement-locking
- https://vazkiimods.github.io/Patchouli/docs/patchouli-basics/giving-new
- https://vazkiimods.github.io/Patchouli/docs/patchouli-basics/multiblocks
- https://modrinth.com/mod/odyssey-quests
- https://github.com/terrarium-earth/Heracles
- Jar `heracles-neoforge-1.21.1-2.0.3.jar` (Modrinth), opened and decompiled with javap
- https://modrinth.com/mod/guideme
- https://guideme.appliedenergistics.org/
- https://guideme.appliedenergistics.org/data-driven-guides
- https://guideme.appliedenergistics.org/authoring/
- https://guideme.appliedenergistics.org/translation
- https://guideme.appliedenergistics.org/open-guide-hotkey
- https://guideme.appliedenergistics.org/converting-patchouli
- Jars `guideme-21.1.20.jar`, `modonomicon-1.21.1-neoforge-1.120.7.jar`, `kubejs-neoforge-2101.7.2-build.377.jar` (Modrinth)
- https://modrinth.com/mod/knowlogy
- https://craftdownunder.co/guides/mods/ftb-quests
- https://www.curseforge.com/minecraft/modpacks/create-above-and-beyond
- https://www.curseforge.com/minecraft/modpacks/all-the-mods-10
- https://modrinth.com/project/phasmcs-no-mans-land and https://modrinth.com/project/wayfarer-dark (`modrinth.index.json` index opened)
- https://en.wikipedia.org/wiki/Progressive_disclosure
- Local repository: `docs/vision.md`, `docs/plan/03-civilisation.md` (§3.7), `docs/plan/modules-tilth.md`, `docs/plan/traduction.md`, `docs/plan/mod-tilth.md`
