# Roadmap

Status as of 2026-10-26. The project is split into sub-projects, each with its own spec → plan → implementation cycle (see [conception/](conception/)).

| # | Sub-project | Status |
|---|---|---|
| 1 | Repository & tooling: packwiz, sync from the instance, CI, release | ✅ Done ([spec](conception/specs/2026-09-23-repo-outillage-design.md)) |
| 2 | System choices: farming, fauna, structures, atmosphere, performance | ✅ Decisions 0001 to 0011 made and applied to `pack/`; every mod of the [registry](mods.md) marked ✅ is in the pack |
| 3 | Rules layer: configs, KubeJS scripts, datapack data, custom mod | 🟡 Configs for most systems, 19 KubeJS scripts, custom mod `fallow` 0.4.0 (sky, bag, xp, far view, clearings, dissipator); survivors, zoo, monument guardians and the seasons bridge still to write |
| 4 | Server: host, deployment, backups | 🟡 Backups (Simple Backups) and Neruina in the pack; host and deployment not started |

## Where the pack stands

The pack starts on a dedicated server, but **nobody has played it yet**. The next step is the play sessions of [tests-en-jeu.md](tests-en-jeu.md): eight sessions of about an hour, which gather every in-game test of the plan pages. Everything frozen at world creation (Tectonic, Streams Reflowing, clearings) must pass before the permanent world is pregenerated.

| Date | Milestone |
|---|---|
| 2026-09-23 | Repository and tooling, base pack of 12 mods |
| 2026-10-10 | Cleanup, decisions 0002 to 0007 merged, plan v1 |
| 2026-10-11 and 10-18 | Refocus on the No Man's Land trailer: an almost empty world, the mist that lifts, long-term goals, a polished and connected finish |
| 2026-10-19 | The team's 57 choices applied; everything in French; Wayfarer ideas (Envelope, Friendly Moon, health, tannery, no XP); music; `fallow` 0.1.0 to 0.2.1 |
| 2026-10-25 | Tectonic, Streams Reflowing and Distant Horizons (0011); safety net and 24 updates; six mods from the review; `fallow` 0.4.0 with clearings and the dissipator |
| 2026-10-26 | Research on what is done elsewhere; eight next modules specified; simpler bag, stacks by kind of item, 30-minute days and 15-minute nights (0012) |

## Before the first release (`v0.1.0`)

- [ ] Run the [in-game tests](tests-en-jeu.md) and fix what breaks.
- [ ] Compare `pack/config/quark-common.toml` with the config generated on first launch (the keys were inferred from the Quark 4.1 classes, [decision 0005](decisions/0005-quark.md)).
- [ ] Turn `[Unreleased]` in the changelog into `[0.1.0]` and place the tag.
- [ ] Pregenerate the permanent world once the terrain tests (W) pass, with `/dh pregen` rather than Chunky (Chunky leaves holes in the far view; to confirm with H11).

## Updates held until the in-game tests

These updates exist on Modrinth but change something that must first be seen in game.

| Mod | In the pack | Held version | Why wait |
|---|---|---|---|
| Polytone + Spatial GUI | 5.1.3 + 1.8 | 5.3.1 + 1.9 | Go together; the colours and lights of `fallow-ambiance` must be checked first |
| Supplementaries | 3.9.9 | 4.x | Major version; our config (0006) must be re-read |
| Friends & Foes | 4.0.27 | 5.x | Major version; the creatures disabled by config must stay disabled |
| Envelope | 0.7.5 | 0.8 | Its JEI mixin crashed once (see the changelog); to retest with JEI 19.51 |
| Icy's Better Horses | 2.0.1 | 2.1.x | Revival costs experience levels, which no longer exist ([0009](decisions/0009-xp.md)): a replacement cost is needed |
| Cold Sweat | 2.4.3.1 | 2.4.4 | Breaks Cold Sweat: Altitude until Altitude follows |
| Lithium | 0.15.4 | — | To remove if the world freezes with Sable airships |
| Subtle Effects, More Darkness | 1.15.1, 2.0.1 | — | Client keys to read in game before tuning ([0007](decisions/0007-ambiance-performance.md)) |

## Open work items arising from the decisions

### Rules and configs (sub-project 3)
- [ ] **Ecliptic Seasons → Cold Sweat bridge** in KubeJS: seasonal temperature felt by the player. Fallback: Serene Seasons ([0002](decisions/0002-agriculture.md)).
- [ ] **Farm & Charm confined to the farm**: hide or merge its cooking stations and its ingredients that duplicate Kaleidoscope Cookery ([0002](decisions/0002-agriculture.md)).
- [ ] Lock KC-Automation's automation behind progression ([0002](decisions/0002-agriculture.md)).
- [ ] **"Survivor" villagers**: ruined villages, rare survivors to bring back, breeding blocked, trades filtered (no Mending, no bypassing Overgeared) ([0004](decisions/0004-structures.md)).
- [ ] **Monument guardians**: Wroughtnaut, Frostmaw, Sculptor and Sacred Pontiff at four Monoliths, blessing after their defeat (decision 15 of [12.5](plan/12-verification.md#125-decisions-that-are-added-continuing-1012)); proposed, not written.
- [ ] **The zoo**: a measurable long-term goal (species housed, aquariums), still without a mechanic.
- [ ] **Traveler's Notebook** (*Carnet du voyageur*): a Modonomicon book of guides and quests unlocked by advancements, which explains the mechanics (clearings, dissipator, tannery, health, the future fanal) ([livre-de-quetes](plan/recherche/2026-10-26/livre-de-quetes.md)).
- [ ] In Control!: `spawn.json` has no rule for villagers and wandering traders, unlike what the survivors plan assumes; to settle with the `survivants` module.
- [ ] **White moon**: draft in [kubejs-brouillons/](plan/kubejs-brouillons/), needs a sound and texts; the `ciel` module already draws it.
- [ ] Subtle Effects: turn off superfluous effects ([0007](decisions/0007-ambiance-performance.md)).
- [x] Remove hostile vanilla monsters from the Overworld; spawn table per biome; Friends & Foes, hamsters and Companions! tamed by config; End removed and Nether closed ([0003](decisions/0003-faune.md), [0004](decisions/0004-structures.md)). To check in game.
- [x] Clearings and the dissipator: module `clairiere` and `dissipateur` of the `fallow` mod 0.4.0 ([mod-fallow](plan/mod-fallow.md)).

### Custom `fallow` mod
- [ ] A Ponder scene for the dissipator.
- [ ] Remove the `sac` module in 0.5.0: Traveler's Backpack has left the pack ([0012](decisions/0012-sacs-piles-jours.md)).
- [ ] The eight modules approved on 2026-10-26, in this order: `voile`, `zoo`, `vie`, `fanal`, `eclaircie`, `sillage`, `echos`, `survivants` ([modules-fallow](plan/modules-fallow.md)). The zoo and survivor items above are now these modules.

### Pending research (sub-project 2)
- [ ] Demanding animal husbandry (needs, diseases, genetics): Animal Husbandry 0.6.0 to re-evaluate ([0002](decisions/0002-agriculture.md)).
- [ ] A mod that replaces vanilla structures with Create-integrated versions; candidates: Create: Structures Arise, Create Rustic Structures ([0004](decisions/0004-structures.md)).
- [ ] Separate Sable Render Distance, now that Distant Horizons is in ([0007](decisions/0007-ambiance-performance.md)).
- [x] ~~Food preservation~~: dropped by the team on 2026-10-18 (Freeze It and Heat It removed).
- [x] Terrain: Tectonic + Streams Reflowing on No Man's Land; Distant Horizons driven by the mist ([0011](decisions/0011-tectonic-streams-distant-horizons.md)).

### Repository housekeeping
- [ ] Delete the merged branches `test/agriculture`, `test/faune`, `test/structures`, `feat/idea-mods`, `feat/safety-net-and-updates` and `feat/tectonic-streams-distant-horizons` (only the owner can: the cloud environment gets a 403).

### Watch list
- Sinytra Connector (beta) runs Antique Atlas 4 and Surveyor, two Fabric mods: keep an eye on it at every update.
- Steam 'n' Rails is an unofficial NeoForge port (beta).
- Immersive Enchanting, KubeJS Create, In Control! and Simple Backups are betas.
