# 0040 — Removing Cinematic Interactions

- **Date**: 2026-11-20
- **Status**: settled (the team's choice of 2026-11-20). Amends [0030](0030-farmers-delight-kitchen.md).

## Context
Cinematic Interactions (Southware, MIT) turned the vanilla stations into first-person scenes. It is only published on CurseForge, so its jar was committed in `pack/mods/` and embedded in the `.mrpack`. The first version sent to the Modrinth project, `0.1.0-alpha.1`, was held back by Modrinth's check of embedded files, which asks for proof of permission for every file it does not know. The mod had not been seen in game.

## Decision
The mod leaves the pack with its French texts. The vanilla stations open their usual windows again; Farmer's Delight, Hands-On Delight and Diegesis are unchanged, as they do not depend on it.

## Consequences
- The jars still embedded are the `fallow` mod, the three atlas ports (LGPL, sources in `salvagedisc/fallow-atlas`) and Astemir Re-Lib (a file still listed on Modrinth).
- In-game test of the vanilla stations removed from [tests-en-jeu.md](../tests-en-jeu.md).
