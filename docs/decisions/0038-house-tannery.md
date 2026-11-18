# 0038 — A house tannery instead of the Tannery mod

- **Date**: 2026-11-18
- **Status**: kept (the team's choice of 2026-11-18); checked on a dedicated server of the full pack with a client, not played. Replaces [0014](0014-tannery.md).

## Context
Since 2026-10-29 leather came from the Tannery: Better Leather mod (Beckstation, All Rights Reserved, MCreator, about 1,000 downloads). The inventory of 2026-11-14 listed it among the mods to redo in house: it replaces six vanilla loot tables (losing Looting and the cooked meat), writes two messages in hard-coded English, ships satchels and studded leather the pack hides, and an MCreator mod with one author is likely to break or stop at the next version. The team asked for a house tannery on 2026-11-18.

## Options
| Criterion | A. Keep Tannery | B. House `tannerie` module |
|---|---|---|
| Steps | Dry on a rack, then one click in the barrel | Scrape with a knife, soak in a vat with bark, dry on a frame |
| Place matters | No (the rack dries anywhere) | Yes: sun, shade, fire, rain and winter change the drying |
| Pack fixes needed | Six vanilla tables put back, barrel messages translated on the client, satchels hidden, bark tag filled by script | None |
| Licence | All Rights Reserved | The pack's own code |
| Items | Five hides, dry hide, bark, tannic powder, fat, sinew, jerky, hardened, treated and studded leather, satchels | Raw hide, scraped hide, wet leather; bark and fat from Farmer's Delight and Naturalist |

## Decision
Option B, in `compline` 0.34.0 ([tannery](../plan/tannerie.md)). One raw hide for every animal, three blocks, and the drying ties into the place: a frame in the sun, a shed by the fire, nothing in the rain. It reuses what the pack has: Farmer's Delight's knives and bark, Naturalist's fat, the `compline:foyers` hearths of rest by the hearth, Ecliptic Seasons' snow for winter.

Gone with Tannery: sinew (string comes from spiders and flax), jerky, hardened, treated and studded leather and the tanner's table (armour is Overgeared's forge), fat blocks. Fat now makes torches and candles.

## In-game tests
TA1 to TA7, in [tannerie.md](../plan/tannerie.md#in-game-tests).
