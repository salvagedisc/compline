# Quests and guides (Questlog)

A book-style quest journal, in French, that explains the mechanics of Fallow as the player meets them and gives a few goals. The team chose **Questlog** on 2026-10-26: it opens as a menu (the inventory button or the key under Esc), shows quests by chapter, and can be edited in game. It replaces the first idea of a Modonomicon book carried as an item ("too constraining"). The research that compared the candidates is [livre-de-quetes](recherche/2026-10-26/livre-de-quetes.md); Questlog was not in it.

Questlog 3.5.0 (Apache-2.0, InfernalStudios, requires Cloth Config). Documentation: moddedmc.wiki, project `questlog`.

## Rules for writing it
- One quest per gesture or per idea; a description of two or three short paragraphs.
- It says how to do things, never where to go and never what a mystery is. No coordinates.
- Guides use the `questlog:read` objective (opening the quest completes it). Real quests use a gesture: obtain an item, place a block.
- A quest appears when its requirements are met (`requirements`): always (`visit_dimension` overworld), after another quest (`quest_complete`) or when an item is first picked up (`item_obtain`).
- No rewards: the reward is knowing how (no experience in the pack, [0009](../decisions/0009-xp.md)).
- Texts are written in French directly in the files (`translatable` stays false). The mod's own interface is translated in `pack/kubejs/assets/questlog/lang/fr_fr.json`.
- `npm test` checks that every quest points to an existing chapter and to existing quests.

## Files

| What | Where |
|---|---|
| Chapters (`name`, `icon`, `order`, one `default_chapter`) | `pack/config/questlog/chapters/<id>.json`, id `questlog:<id>` |
| Quests | `pack/config/questlog/quests/<id>.json`, id `questlog:<id>` |

Quests made with the in-game editor are saved in the same folder: bring them back into the repository with `npm run sync`.

## Contents

| Chapter | Quests |
|---|---|
| Arriver (Arriving) | Bienvenue, Le jour et la nuit, Un sac sur le dos (obtain the backpack), Les cœurs perdus, Sans expérience, Au long cours |
| Premiers gestes (First gestures) | Mains nues, Le silex, La pierre à tailler, La première hache, Des planches, Le feu (place a campfire), La pioche, Le four, Le marteau et l'enclume de pierre, La forge (guide, with the first iron ingot) ([first gestures](premiers-gestes.md)) |
| Survivre (Surviving) | La soif, Le froid et le chaud, Les saisons |
| La brume (The mist) | La brume, Un feu de veille (place one), Une lanterne de clairière (place one), Les clairières, Le dissipateur (place one, after the machines guide) |
| Métiers (Crafts) | La tannerie (appears with the first hide; obtain a dry hide, tannic powder, then leather), Les machines (appears with the first Create part) |

## Next
- A `fanal` quest line when the module exists; then one per new module (zoo, survivors, clearing spell).
- Monuments and the Friendly Moon: one vague quest each, appearing with `visit_structure` or after the first encounter.
- Forge (Overgeared), cooking (Kaleidoscope Cookery), the atlas, the bestiary.
- Hover images (`[text](image:...)`) for the gestures that are hard to describe.

## In-game tests
- N1. The journal opens from the inventory and with its key; the five chapters show in French, Arriver first, then Premiers gestes.
- N2. Guides complete when opened; "Un sac sur le dos" completes when the bag is crafted.
- N3. La tannerie appears with the first hide, Les machines with the first shaft, cogwheel or andesite alloy.
- N4. Placing a vigil fire, then a lantern, completes their quests; the dissipator quest appears after both the lantern and the machines.
- N5. A quest written with the in-game editor lands in `config/questlog/quests/`.
