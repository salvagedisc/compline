# Fallow: modpack plan

Minecraft **1.21.1**, NeoForge 21.1.265, packwiz, mods from Modrinth only. The plan builds on the [vision](../vision.md) and the [decisions](../decisions/README.md); the full list of mods, with their status, is in the [registry](../mods.md).

> **Status as of 2026-10-25.** The plan is applied to `pack/`: every mod of the [registry](../mods.md) marked ✅ is in the pack, and the clearings and the dissipator exist in the custom [`fallow` mod](mod-fallow.md) 0.4.0. Still to write: the survivors, the zoo, the monument guardians, the white moon and the seasons bridge ([roadmap](../feuille-de-route.md)). The pack starts on a dedicated server, but nothing has been played yet: the tests are grouped in [tests-en-jeu.md](../tests-en-jeu.md).

## What Fallow is aiming for

A permanent world, almost empty and enigmatic, in the spirit of the No Man's Land trailer. The mist lifts in places and at certain moments, wherever life is brought back (the clearings). Long-term goals: the zoo, the bestiary, the monument codex, the rail and air network, the house. A polished finish in the style of Realism Craft, without shaders or realistic textures, where every system plugs into the others.

## Reading order

| Chapter | Contents |
|---|---|
| [10-recentrage.md](10-recentrage.md) | **The plan in force**: the lifting mist, the goals, the pack list, the finish, the links between systems, the scripts, decisions 1 to 13 |
| [11-rendu-et-confort.md](11-rendu-et-confort.md) | Rendering without shaders, camera, menus, EMI, Wildex, Ancient Remnants, Archie's Dinos |
| [12-verification.md](12-verification.md) | **Full verification of 2026-10-18**: it corrects chapters 10 and 11, details the links between mods (Overgeared, Kaleidoscope, Create, world, rendering), proposes the custom `fallow` mod and decisions 14 to 18 |
| [fallow-ambiance.md](fallow-ambiance.md) | The in-house resource pack: ATMOSPHERICS preset, Polytone colours, chosen values and settings to adjust in game |
| [fallow-interface.md](fallow-interface.md) | The Kaleidoscope, Overgeared and Wildex screens in Dark Coffee colours: withdrawn, proposed to the author of Dark Coffee; licences and limits |
| [mod-fallow.md](mod-fallow.md) | The custom `fallow` mod 0.4.0 (modules `ciel`, `sac`, `xp`, `horizon`, `clairiere` and `dissipateur`): how it works, what is deduced from the code, in-game tests |
| [modules-fallow.md](modules-fallow.md) | The eight planned modules of the `fallow` mod (2026-10-26): mist culling, zoo, growing clearings, beacon, clear mornings, airship bubble, echoes, survivors; what other games do well and what Fallow takes |
| [lune-amie.md](lune-amie.md) | The Friendly Moon from No Man's Land as the main thread: the path to the Meeting Point, coexistence with Enhanced Celestials, the removed blood moons |
| [sante.md](sante.md) | Maximum health: one heart lost per death (floor of 5), life shards found while exploring |
| [doublons.md](doublons.md) | One model per species: comparison of duplicates across Naturalist, Alex's Mobs, Hybrid Aquatic, Critters and Companions, Longwings and Friends & Foes, cuts, in-game tests |
| [faune-nml.md](faune-nml.md) | The No Man's Land fauna goes back to vanilla: remodels and textures cut, deer, goose, bass, turtle and Frienderman removed, in-game tests |
| [tannerie.md](tannerie.md) | The tannery: raw hide, ash-prepared hide, soaking on the bamboo tray |
| [musique.md](musique.md) | The music: True Adaptive Music, the `fallow` pack tree, the server predicates, in-game tests ([0010](../decisions/0010-musique.md)) |
| [xp.md](xp.md) | Removing experience: study of the sources and uses, implementation ([0009](../decisions/0009-xp.md)) |
| [traduction.md](traduction.md) | The French translation: where our texts live, the `check-fr` check, the glossary, what stays in English |
| [recherche/verif-2026-10-18/](recherche/verif-2026-10-18/) | The six verification reports (the sixth covers the monument guardians), with the evidence (opened jars, versions, tickets) and the list of in-game tests |
| [carnet.md](carnet.md) | The Traveler's Notebook: the Modonomicon guide book, its chapters and how to extend it |
| [recherche/2026-10-26/](recherche/2026-10-26/) | Eight research reports of 2026-10-26: what is done elsewhere for each next module, animations of survivors and echoes, seasons and day length, the quest book, stacks and bags |
| [kubejs-brouillons/](kubejs-brouillons/) | Draft scripts and data, laid out like `pack/kubejs/`; to be corrected according to chapter 12 before use |

## History (v1 of 2026-10-10)

Chapters 01 to 09 are the first version of the plan, judged too far from the vision on 2026-10-11. They remain for their technical details; where they contradict chapters 10 to 12, the latter prevail.

| File | Contents |
|---|---|
| [01-liste-des-mods.md](01-liste-des-mods.md) | v1 list (replaced by the [registry](../mods.md)) |
| [02-exploration.md](02-exploration.md) | Exploration, structures, navigation without coordinates |
| [03-civilisation.md](03-civilisation.md) | Create addons, ore, progression, Nether |
| [04-vie-quotidienne.md](04-vie-quotidienne.md) | Water, fields, animal husbandry, cooking |
| [05-faune-et-ambiance.md](05-faune-et-ambiance.md) | Removal of vanilla monsters, caps, duplicates |
| [06-technique-et-serveur.md](06-technique-et-serveur.md) | Alerts, technical foundation, server, backups |
| [07-kubejs-et-datapack.md](07-kubejs-et-datapack.md) | Organisation of scripts and data |
| [08-mise-en-place.md](08-mise-en-place.md) | Implementation phases, checks in the jars |
| [09-disponibilite-modrinth.md](09-disponibilite-modrinth.md) | Modrinth survey of 2026-10-10 |
| [recherche/](recherche/) | The five research notes of v1 and the raw Modrinth survey |

## After approval

Each approved choice is applied to `pack/` following [CONTRIBUTING.md](../../CONTRIBUTING.md): one mod per commit, configs in `pack/config/` or `pack/defaultconfigs/`, scripts in `pack/kubejs/`, and a record in `docs/decisions/` for each trade-off.
