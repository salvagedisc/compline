# Roadmap

Status as of 2026-10-18. The project is split into sub-projects, each with its own spec → plan → implementation cycle (see [conception/](conception/)).

| # | Sub-project | Status |
|---|---|---|
| 1 | Repository & tooling: packwiz, sync from the instance, CI, release | ✅ Done ([spec](conception/specs/2026-09-23-repo-outillage-design.md)) |
| 2 | System choices: farming, fauna, structures, atmosphere, performance | 🟡 Decisions 0002 to 0007 made; full plan written and checked in [plan/](plan/README.md) (chapters 10 to 12), awaiting validation; target list in the [registry](mods.md) |
| 3 | Rules layer: configs, KubeJS scripts, datapack data | ○ Only the Quark and Supplementaries configs exist |
| 4 | Server: host, deployment, backups | ○ Not started |

## Before the first release (`v0.1.0`)

- [ ] In-game launch test: create a world without a crash using the complete pack.
- [ ] Compare `pack/config/quark-common.toml` with the config generated on first launch (the keys were inferred from the Quark 4.1 classes, [decision 0005](decisions/0005-quark.md)).
- [ ] Turn `[Unreleased]` in the changelog into `[0.1.0]` and place the tag.

## Open work items arising from the decisions

### Rules and configs (sub-project 3)
- [ ] **Ecliptic Seasons → Cold Sweat bridge** in KubeJS: seasonal temperature felt by the player. Fallback: Serene Seasons ([0002](decisions/0002-agriculture.md)).
- [ ] **Farm & Charm confined to the farm**: hide or merge its cooking stations and its ingredients that duplicate Kaleidoscope Cookery ([0002](decisions/0002-agriculture.md)).
- [ ] Lock KC-Automation's automation behind progression ([0002](decisions/0002-agriculture.md)).
- [x] **Remove hostile vanilla monsters** from the Overworld (`pack/config/incontrol/spawn.json`, to be checked in game: patrols, phantoms, spawners); decide the fate of ocean monuments, witch huts and outposts ([0003](decisions/0003-faune.md), [0004](decisions/0004-structures.md)).
- [ ] **Spawn table per biome**: a single version per species between Naturalist and Alex's Mobs, removal of creatures that break the art direction ([0003](decisions/0003-faune.md)).
- [ ] Friends & Foes: keep the peaceful ones, disable the hostile ones. Adorable Hamster Pets: disable the diamond sniffing. Companions!: restrict hostile creatures, weapons and bosses ([0003](decisions/0003-faune.md)).
- [ ] **Remove the End** (dimension, portals, Elytra) and strongholds ([0004](decisions/0004-structures.md)).
- [ ] **"Survivor" villagers**: ruined villages, rare survivors to bring back, breeding blocked, trades filtered (no Mending, no bypassing Overgeared) ([0004](decisions/0004-structures.md)).
- [ ] Subtle Effects: turn off superfluous effects ([0007](decisions/0007-ambiance-performance.md)).

### Pending research (sub-project 2)
- [ ] Demanding animal husbandry (needs, diseases, genetics): alternative to Animal Husbandry ([0002](decisions/0002-agriculture.md)).
- [ ] Food preservation: alternative to Spoiled ([0002](decisions/0002-agriculture.md)).
- [ ] A mod that replaces vanilla structures with Create-integrated versions; candidates: Create: Let The Adventure Begin, Create Aeronautics Structures, Create: Structures Arise, Create Rustic Structures ([0004](decisions/0004-structures.md)).
- [ ] Real-world comparison of NML vs Terralith + Streams Reflowing on `test/terralith-vs-nml` ([0001](decisions/0001-nml-vs-terralith.md)).
- [ ] Distant Horizons, Separate Sable Render Distance, Particle Rain ([0007](decisions/0007-ambiance-performance.md)).
- [ ] Other "to test" candidates from the [registry](mods.md#à-tester).

### Watch list
- Sinytra Connector (beta) runs Antique Atlas 4 and Surveyor, two Fabric mods: keep an eye on it at every update.
- Steam 'n' Rails is an unofficial NeoForge port (beta).
