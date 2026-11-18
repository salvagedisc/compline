# 0035 — Ores and armour progression

- **Date**: 2026-11-17
- **Status**: kept (the team's choices of 2026-11-15 to 2026-11-17); built in steps, see [progression](../plan/progression.md)

## Context
Overgeared's steel arrives fast: chests give steel ingots and diamond templates, and diamonds are as common as in vanilla. The team wants to stay on iron and steel for a long while, to bring netherite into the Overworld, and to rethink mining the way the forge and the kitchen were.

## Options
| Question | Kept | Set aside |
|---|---|---|
| Steel plateau | Lives through Overgeared's quality; steel and templates out of chests | A new tier per material |
| Diamond | Only near deep lava (Spelunkery), template from the coniferous monument, mined with a black steel pickaxe | Vanilla rates |
| Between steel and diamond | Black steel (`fallow` items, Overgeared recipes, master anvil) | Nothing |
| Netherite | "Burning heart" (Regions Unexplored's Inferno caves and lava below −40), renamed orichalcum in French | Oh My Meteors meteorites |
| Dinosaur armour | Side branch forged at the anvil with quality; each set opens part of the world (Cold Sweat insulators); Chorusoatl teleport removed by code | Rare top tier |
| Mining | Prospecting (outcrops, panning) | Prospector's hammer, shoring and cave-ins, Create Ore Excavation, dense ores |

## Decision
All of the "kept" column, one pull request per step:

1. Steel and diamonds become rare (this record).
2. Black steel, needed to mine diamond.
3. Dinosaur armour at the anvil.
4. The burning heart and orichalcum.
5. Prospecting.

Each step is checked on the test server before it is merged; textures made for these steps are provisional until the team approves them.

## Conditions
Create drills and Mine-o-mite ignore tool tiers: they stay as late-game shortcuts. Diamonds from villagers and structure chests are not touched.
