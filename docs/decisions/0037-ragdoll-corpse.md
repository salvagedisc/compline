# 0037 — The body left behind (Sable: Ragdoll Corpse)

- **Date**: 2026-11-18
- **Status**: kept (the team's choice of 2026-11-18, idea 4 of the study of features to add); checked on a dedicated server of the full pack with a client, not played

## Context
Death already costs a heart of maximum health ([health](../plan/sante.md)). The items, on the other hand, were dropped on the ground and despawned after five minutes, which in a large world with mist and no coordinates often meant losing everything: a punishment that adds nothing to decide, only something to redo. The study of 2026-11-17 proposed to keep the inventory in a body the player must go back for.

## Options
| Criterion | A. Vanilla drops | B. Sable: Ragdoll Corpse 0.4.0 | C. Corpse (henkelmax) |
|---|---|---|---|
| Inventory | Scattered, gone after 5 minutes | Kept in the body, no time limit by default | Kept in a corpse entity |
| The body | None | A ragdoll simulated by Sable (already in the pack for airships): it falls, rolls, can be dragged, thrown, hoisted by an airship | A static lying model |
| Curios (bag, accessories) | Dropped | Kept (Curios and Accessories support) | Kept |
| Finding it | Nothing | The vanilla recovery compass points to the last body; the atlas grave marker (Surveyor) | Its own death history screen |
| Licence and reach | — | All rights reserved, about 165,000 downloads, updated 2026-11-11 | All rights reserved, 4.8 million |

## Decision
Option B. Sable is already loaded for Create Aeronautics, and a body that tumbles down a slope or that friends carry home with an airship fits the pack better than a frozen model. It needs **Sable: Ragdolls** 0.8.1 (Apache-2.0), the ragdoll library of the same author.

Fitted to Fallow (`pack/config/sable_player_ragdoll-server.toml`):

- **No self-ragdoll**: the library lets a player throw themselves to the ground with a key (H by default); `allowManualTrigger = false` keeps the body for death only. Explosions can still knock a player down as a ragdoll, with the library's own impact damage.
- **Debug log off** (`debugLogging = false`); the rest stays at the defaults of both mods: bodies never despawn while they hold something, an emptied body vanishes after 1.5 seconds, even an empty inventory leaves a body.
- **French**: neither mod ships it; `pack/kubejs/assets/{ragdoll_corpse,sable_player_ragdoll}/lang/fr_fr.json` translate their 49 texts (« Corps de %s », « Laisser le corps »).

Checked on the bench (dedicated server with every mod of the pack and a client): a player killed with iron, bread and a leather cap leaves `#1 | Dev | … | stacks=4` in `/ragdollcorpse list`, no item lies on the ground, the « You wake up weaker » heart loss still happens, Surveyor adds its grave landmark. Mobs that die leave no ragdoll (the library's mob ragdolls are only spawned by its API and commands). Opening the body by hand was not checked: the bench client renders too slowly to aim at it.

Admin commands, if a body is lost (fallen out of the world, beyond the edge of the disc): `/ragdollcorpse list`, `/ragdollcorpse recover <n>`, `/ragdollcorpse recover-missing`, `/ragdollcorpse clean`.

## In-game tests
- **RC1**: die with a full inventory, armour and the bag worn on the back: nothing falls, the body lies there; right-click opens « Corps de <name> » with everything, armour and bag included; take it all: the body vanishes.
- **RC2**: drag the body with the hands, throw it down a slope; carry it with a small airship.
- **RC3**: die far away, respawn: the recovery compass (if crafted) and the atlas grave point to it; log out and back in, restart the server: the body is still there with its content.
- **RC4**: die in lava and in deep water: the body survives and can be fished out or found; `/ragdollcorpse list` shows it.
- **RC5**: the H key does nothing (no self-ragdoll); a creeper explosion may throw the player down for a moment.
