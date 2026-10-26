# Traveler's Notebook (*Carnet du voyageur*)

A Modonomicon book of guides, in French, that explains the mechanics of Fallow as the player meets them. Choice and research: [livre-de-quetes](recherche/2026-10-26/livre-de-quetes.md). First version of 2026-10-26: four chapters, 16 entries, nothing seen in game yet.

## Rules for writing it
- Short pages: a title and two or three sentences. One idea per page.
- It says how to do things, never where to go and never what a mystery is. No coordinates.
- An entry that waits for a gesture stays visible, greyed, with its condition in the tooltip (« Requiert : obtenir une peau brute »). Vital survival rules (thirst, cold, seasons) are always open.
- All text goes through `book.fallow.carnet.*` keys in `pack/kubejs/assets/fallow/lang/fr_fr.json`. `npm test` checks that every key exists, and that categories, parents and advancements exist.

## Files

| What | Where |
|---|---|
| Book, chapters, entries | `pack/kubejs/data/fallow/modonomicon/books/carnet/` (`book.json`, `categories/`, `entries/<chapter>/`) |
| Unlock advancements, hidden (no `display`, so no toast) | `pack/kubejs/data/fallow/advancement/carnet/` |
| Texts | `pack/kubejs/assets/fallow/lang/fr_fr.json` |
| Book given at first login, recipe (book + charcoal) | `pack/kubejs/server_scripts/carnet.js` |

The book item is `modonomicon:modonomicon` with the component `modonomicon:book_id="fallow:carnet"` (green cover).

## Contents

| Chapter | Entries | Opens |
|---|---|---|
| Arriver (Arriving) | Ce carnet, Un monde presque vide, Le jour et la nuit, Le sac et les piles, Les cœurs perdus, Sans expérience, Au long cours | Always |
| Survivre (Surviving) | La soif, Le froid et le chaud, Les saisons | Always |
| La brume (The mist) | La brume, Feu de veille, Lanterne de clairière, Dissipateur, Les clairières | Always, except the dissipator: first Create part (`fallow:carnet/create`) |
| Métiers (Crafts) | La tannerie, Les machines | First raw hide (`fallow:carnet/peau`), first Create part |

## Next
- The `fanal` chapter with a multiblock page, when the module exists; then one entry per new module (zoo, survivors, clearing spell).
- Monuments and the Friendly Moon: one vague sentence each, opened after the first encounter (an advancement granted by `lune.js` and `sante_butin.js`).
- Forge (Overgeared), cooking (Kaleidoscope Cookery), the atlas, the bestiary.
- If the team wants real objectives with checkboxes and rewards: a trial of Odyssey Quests (beta) on the test server.

## In-game tests
- N1. The book is in the inventory at first login, only once; the recipe (book + charcoal) gives it back.
- N2. The four chapters open; the texts are in French and fit the pages.
- N3. The tannery and machines entries are greyed with their tooltip, and open when a raw hide or a Create part is picked up.
- N4. The recipe pages of the three clearing blocks show the right recipes.
- N5. `/modonomicon reload` reloads the book; the reset command replays the unlocks.
