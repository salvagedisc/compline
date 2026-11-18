# 0014 — Tannery mod instead of the scripted tannery

- **Date**: 2026-10-29
- **Status**: replaced on 2026-11-18 by [0038](0038-house-tannery.md) (house tannery); kept from 2026-10-29 (team choice), checked on a test server

## Context
Since 2026-10-19 the pack had its own tannery in KubeJS: animals dropped a raw hide (two items reusing the rabbit hide texture), prepared with ashes at the crafting table, then soaked on Kaleidoscope Cookery's bamboo tray in the rain or under a dripstone. Seeing the texture, the team thought it only covered the rabbit and pointed to an existing mod. [Tannery: Better Leather](https://modrinth.com/mod/tannery-better-leather) (Beckstation, NeoForge 1.21.1, 1.0.2 of 2026-06-26, All Rights Reserved, about 1,000 downloads) has hides per animal, a tanning rack, a tanning barrel, bark from stripped logs, sinew, fat, studded leather and satchels. Jar read: Tannery 1.0.2.

## Options
| Criterion | Tannery mod | Keep the scripted tannery |
|---|---|---|
| Items | Five hides with their own textures, dry hide, bark, tannic powder, fat, sinew, jerky, hardened and studded leather | Two tinted vanilla textures |
| Workstations | Tanning rack, hanging rack, barrel, tanner's table: something to build | Bamboo tray, rain or dripstone |
| Ties to the pack | Bark from stripping logs (axe, [0013](0013-premiers-gestes.md)), sinew as string, fat torches | Ashes from the campfire, seasons through the rain |
| Animals | Five vanilla animals; the others need loot scripts | Every leather animal, by script |
| Translation | English only: 74 texts plus two hard-coded barrel messages | Ours |

## Decision
Tannery replaces the scripted tannery. Our two items, the ash recipe, the tray recipe and the raw hide tag are removed. Every leather animal of the pack drops the closest of Tannery's five hides, keeping its quantities and Looting bonus; pigs, sheep, goats and NML deer get one. The six vanilla tables that Tannery overrides are put back from `pack/kubejs/data`, so the Looting bonus and cooked meat stay. Every strippable log of the pack gives bark. The satchels are removed: Reliable Backpacks is the pack's only bag ([0012](0012-sacs-piles-jours.md)). French texts are added, and the barrel's two hard-coded messages are replaced on the client. Details: [tannery](../plan/tannerie.md).

## Rejected
- Keeping the scripted tannery with real textures: less to build, nothing to place, and Tannery already does what the team expected.
- Tannery's satchels: a second bag next to Reliable Backpacks.
