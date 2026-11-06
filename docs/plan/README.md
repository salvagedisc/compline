# Fallow: system pages

Minecraft **1.21.1**, NeoForge 21.1.265, packwiz, mods from Modrinth only. The plan builds on the [vision](../vision.md) and the [decisions](../decisions/README.md); the full list of mods, with their status, is in the [registry](../mods.md).

> One page per system, kept up to date as the pack changes. The plan chapters 01 to 12 (v1 of 2026-10-10, the refocus of 2026-10-11 and the verification of 2026-10-18), the research notes and the KubeJS drafts are frozen in [archive/](../archive/README.md): where they disagree with a page here, the registry or a decision, the latter prevail. Open work is in the [roadmap](../feuille-de-route.md), in-game tests in [tests-en-jeu.md](../tests-en-jeu.md).

## What Fallow is aiming for

A permanent world, almost empty and enigmatic, in the spirit of the No Man's Land trailer. The mist lifts in places and at certain moments, wherever life is brought back (the clearings). Long-term goals: the zoo, the bestiary, the monument codex, the rail and air network, the house. A polished finish in the style of Realism Craft, without shaders or realistic textures, where every system plugs into the others.

## Pages

| Page | Contents |
|---|---|
| [fallow-ambiance.md](fallow-ambiance.md) | The in-house resource pack: ATMOSPHERICS preset, Polytone colours, chosen values and settings to adjust in game |
| [fallow-interface.md](fallow-interface.md) | Fallow's interface built from GUI Revision: sources, licences, build script, next steps |
| [fallow-textures.md](fallow-textures.md) | The world's blocks and items from Vanilla Evolved: what is kept and left out, order of the packs, next steps for the mods' blocks |
| [mod-fallow.md](mod-fallow.md) | The custom `fallow` mod 0.5.0 (modules `ciel`, `xp`, `horizon`, `clairiere`, `dissipateur`, `titres` and `outils`): how it works, what is deduced from the code, in-game tests |
| [modules-fallow.md](modules-fallow.md) | The eight planned modules of the `fallow` mod (2026-10-26): mist culling, zoo, growing clearings, beacon, clear mornings, airship bubble, echoes, survivors; what other games do well and what Fallow takes |
| [lune-amie.md](lune-amie.md) | The Friendly Moon from No Man's Land as the main thread: the path to the Meeting Point, coexistence with Enhanced Celestials, the removed blood moons |
| [sante.md](sante.md) | Maximum health: one heart lost per death (floor of 5), life shards found while exploring |
| [doublons.md](doublons.md) | One model per species: comparison of duplicates across Naturalist, Alex's Mobs, Hybrid Aquatic, Critters and Companions, Longwings and Friends & Foes, cuts, in-game tests |
| [faune-nml.md](faune-nml.md) | The No Man's Land fauna goes back to vanilla: remodels and textures cut, deer, goose, bass, turtle and Frienderman removed, in-game tests |
| [premiers-gestes.md](premiers-gestes.md) | First gestures: no logs or rock by hand, knapped stone tools (Overgeared), planks with an axe, flint campfire, the « Premiers gestes » quests ([0013](../decisions/0013-premiers-gestes.md)) |
| [tri-du-pack.md](tri-du-pack.md) | Pack sort of 2026-11-01: Chipped and Beautify! removed, 11 defects fixed (levitite, mincer, enchanting costs, structures), settings, 313 items and about 1,110 Steam 'n' Rails variants removed, recipe levels (rope, pulley, kitchen knives) |
| [tannerie.md](tannerie.md) | The tannery with the Tannery mod: hides for every leather animal, tanning rack, bark, tanning barrel |
| [musique.md](musique.md) | The music: True Adaptive Music, the `fallow` pack tree, the server predicates, in-game tests ([0010](../decisions/0010-musique.md)) |
| [xp.md](xp.md) | Removing experience: study of the sources and uses, implementation ([0009](../decisions/0009-xp.md)) |
| [traduction.md](traduction.md) | The French translation: where our texts live, the `check-fr` check, the glossary, what stays in English |
| [quetes.md](quetes.md) | Quests and guides with Questlog: chapters, how to write a quest, what comes next |

## After approval

Each approved choice is applied to `pack/` following [CONTRIBUTING.md](../../CONTRIBUTING.md): one mod per commit, configs in `pack/config/` or `pack/defaultconfigs/`, scripts in `pack/kubejs/`, and a record in `docs/decisions/` for each trade-off.
