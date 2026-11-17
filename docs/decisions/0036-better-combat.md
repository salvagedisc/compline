# 0036 — Animated combat with Better Combat

- **Date**: 2026-11-17
- **Status**: kept (the team's choice of 2026-11-17); checked on the test bench (client of the full pack), not played

## Context
Combat was still the vanilla click: the arm jerks, the weapon does not move through the air, a sword and an axe hit the same way. Punchy! animates the first-person arm with restraint, Shoulder Surfing shows the body from behind, so the swing of the whole body is what players see most and it was the weakest gesture of the pack.

## Options
| Criterion | A. Keep vanilla | B. Better Combat 2.4.0 |
|---|---|---|
| Swing | One arm jerk | A full body animation per weapon type (sword, axe, spear, mace, trident…), combos of two or three strikes |
| Hit | Whatever is under the crosshair | What the blade sweeps through, so a swing can touch two creatures |
| Two hands | No | The off hand stays idle with a two-handed weapon (spears) |
| Compatibility | — | Punchy! supports it officially; seen working with Shoulder Surfing and Fresh Animations' player model on the bench |
| Licence | — | GPL-3.0, referenced on Modrinth, never redistributed |

## Decision
Option B (the team, 2026-11-17). Better Combat 2.4.0 with its library playerAnimator 2.0.4 (Cloth Config was already there).

Fitted to Compline:

- **Weapons of the pack**: vanilla, Overgeared, Mowzie's Mobs, Hybrid Aquatic and Alex's Mobs weapons are recognised by Better Combat's own data or by its name rules (sword, axe, spear, mace, trident; Farmer's Delight's knives should get the dagger swing by name, not checked on the bench). The black steel sword and axe of the `compline` mod get the sword and axe swings (`pack/kubejs/data/compline/weapon_attributes/`).
- **Tools stay tools**: `config/bettercombat/fallback_compatibility.json` keeps Better Combat's name rules and adds a blacklist so that pickaxes, the smithing hammer, Handcrafted's and Quark's hammers, fishing rods and the forged blades and tool heads (Overgeared, black steel) are never treated as weapons.
- **Settings**: Better Combat's defaults otherwise (arms hidden in first person, where Punchy! already animates them). Its French comes with the mod; the 12 missing texts are in `pack/kubejs/assets/bettercombat/lang/fr_fr.json`.

## Rejected
- **Epic Fight**: a whole combat system with its own stamina, skills and camera, which would fight Shoulder Surfing and the pack's survival pace.
- **Vanilla**: the weakest gesture of the pack in the shoulder view.
