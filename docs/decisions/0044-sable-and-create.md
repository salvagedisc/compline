# 0044 — Sable and Create machines

- **Date**: 2026-11-21
- **Status**: kept (six mods added); Loconautics pending its Modrinth release; Sound Physics Aeronautics, SSRD and Sable Ragdolls Patch held for the team.

## Context
The team asked for the mods that bring Sable, the physics library of Create Aeronautics, into Create's machines and the rest of the pack, Create: Loconautics given as an example. Sable 2.0.6, Create Aeronautics 1.3.3, Sable: Physics Compat and the two ragdoll mods were already in the pack. Research of 2026-11-21: Modrinth searched for every 1.21.1 NeoForge project naming Sable or Aeronautics (about 160), each candidate's jar opened (mod ids, dependencies, mixin targets, config classes) and its overlap with the pack checked.

## Options
| Need | Candidates | Kept |
|---|---|---|
| Mechanical arm, encased fan, basin, water wheel on a ship | Sable Beyond (MIT), Aeronautics: Addition (ARR, arm only) | **Sable Beyond** |
| Plough and roller on a ship | Plough & Roll! (MIT, source), Create Aeronautics Roller Fix (ARR, roadbed only) | **Plough & Roll!** |
| Display link on a ship | Aeronautics Display Link Fix (MIT) | **kept** |
| Duplication on assembly | Sable Fix Mod (MIT: containers, item handlers, attached blocks, crops), Sable Assembly Fix (MIT: non-clearable block entities only) | **Sable Fix Mod** |
| Mods of the pack on a ship | Create Aeronautics: Compatibility (Alex's Mobs path-finding), Furniture with Create and Sable compat (Handcrafted, Quark, Every Compat seats) | **both** |
| Trains as physics bodies | Create: Loconautics, Create: Train Functionality, Ferronautics | none yet |
| Temperature on a ship | Cold Sweat Sable Compat, Cold Sweat x Aeronautics | none: Cold Sweat 2.4.1+ does it |
| Mass of modded blocks | MassWeightCompat, Sable Weighted | none: Sable: Physics Compat covers the pack's mods |

## Decision
- Six mods join the pack, all from Modrinth: Sable Beyond 0.5.0, Plough & Roll! 1.2.0, Aeronautics Display Link Fix 1.3, Sable Fix Mod 1.1.0 (server only), Create Aeronautics: Compatibility 1.1.3, Furniture with Create and Sable compat 5.0.
- Sable Beyond settings (`config/sable_beyond/`): the push of flowing fluids on sub-levels is off, since Streams Reflowing already carries hulls along its currents ([0011](0011-tectonic-streams-distant-horizons.md)); its buttons on the title and pause screens are off. Its entity mass, dynamic mass and basin options stay off as shipped.
- French texts for the three mods that ship English only (`kubejs/assets/<namespace>/lang/fr_fr.json`).

## Rejected or pending
- **Create: Loconautics** (0.1.4 beta): each train car becomes a Sable sub-level that can collide and derail; it supports Steam 'n' Rails boilers. Published only on CurseForge for now (its README already links a Modrinth page that is not yet live), and its art is all rights reserved, so its jar cannot be embedded. To add as soon as it is on Modrinth.
- **Create: Train Functionality**: CurseForge only, no source.
- **Ferronautics**: halted, superseded by Loconautics.
- **Sound Physics Aeronautics**: a fork that replaces Sound Physics Remastered (same mod id) and adds ship acoustics, Doppler and far propellers; it brings 924 new texts to translate and a wing windrush that overlaps Aeronautics Wind Sound. Held for the team's choice.
- **Separate Sable Render Distance**, **Sable Ragdolls Patch**: client-side; to try in game first (the patch fixes Punchy, Curios and LambDynamicLights with the ragdolls).
- Aeronautics: Addition, Create Aeronautics Roller Fix, Sable Assembly Fix: covered by the kept mods. MassWeightCompat, Sable Weighted: duplicate Sable: Physics Compat. Create: Sable Train Overlay Compat: needs a map mod the pack does not have.
