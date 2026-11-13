# 6 — A guardian for the Ancient Remnants: Monoliths monuments

Check of 2026-10-18. Scope: replace the `minecraft:breeze` spawners of Ancient Remnants 1.5.2 (AR) with a guardian consistent with Tilth (decision 0003: rare, territorial hostiles, no horror).

Method:
- AR 1.5.2 jar already extracted (`<work dir>/jars/ar/x`): the 41 `.nbt` files were read with a small NBT reader written for the purpose (Python `-I`);
- pack jars (`origin/main`, extracted into `<work dir>/jars/c-*`) inspected with `javap`: Mowzie's Mobs 1.8.2, Companions! 1.3.4, Friends & Foes 4.0.27, Alex's Mobs Continued 2.2.2, Hybrid Aquatic 1.7.3, Ghost 1.3.4, Quark, NML 1.5.12, KubeJS 2101.7, In Control;
- Modrinth API (NeoForge 1.21.1 search, about 60 requests); jars downloaded and opened for Bosses of Mass Destruction, Legendary Monsters, EEEAB's Mobs [Unofficial], LAir's Bosses and The Gadyn.

Nothing was modified in the repository.

---

## Summary

- **There is one breeze spawner per monument, except at the sentinel monolith.** It is placed in a corridor or on a pedestal, 9 to 18 blocks from the Elderheart. At the hunter monolith, the corridor is only 2 blocks high, and the Elderheart rooms are narrow: bosses must be placed on the ground, outside.
- **The Elderheart can be "sealed".** If it no longer has an NBT key `Blessing`, it gives nothing (bytecode of `tick()`). A script can therefore remove the blessing, then give it back when the guardian falls. This is what gives the guardian meaning: without it, you walk past it and the blessing arrives at 16 blocks.
- **There is no need to add a mod.** Mowzie's Mobs and Companions!, already in the pack, have sleeping guardians fully in tone: a hero's armor that awakens when struck, a sleeping ice giant, a sculptor-monk who sets a trial, a moss pontiff awakened by hand.
- **The best method is a KubeJS script**, which extends the polling already planned for the clearing (report 4, §2.3). It spawns the guardian only once, seals the Elderheart and releases it when the guardian dies. It then opens the clearing and gives the codex page. A small datapack override that removes the spawners is also needed.
- **If a new mod is really wanted**, the first choice is **Bosses of Mass Destruction** (Forge/NeoForge 1.3.3, 1.95 MB), of which only the **Obsidilith** would be kept: a living obsidian monolith, its four structures being disabled by tags. It is an option, not a need.

---

## 1. What is confirmed

### 1.1 Where the spawners and the Elderheart are [AR 1.5.2 `.nbt`]

The six structures are jigsaws. Their pieces use two shared processor_lists: `ancient_remnants:monolith` for the three monoliths, `ancient_remnants:monument` for the three monuments. Each contains only a `block_ignore` (air, structure_block). Each pool contains a single piece, and none has `spawn_overrides`.

| Monument | Piece containing the spawner | Spawner (pos. in the piece) | Elderheart (piece, pos.) | Distance |
|---|---|---|---|---|
| hunter_monolith | `hunter_monolith/middle` | (5, 18, 2) | same piece, (5, 18, 15) | 13 blocks |
| warrior_monolith | `warrior_monolith/middle` | (5, 42, 8), `Delay 601` | same piece, (7, 37, 16) | about 9.6 blocks |
| sentinel_monolith | **no spawner** | — | `sentinel_monolith/middle`, (6, 42, 16) | — |
| cherry_monument | `cherry_monument/top_3` | (4, 11, 29) | same piece, (18, 21, 29) | about 17 blocks |
| oakwood_monument | `oakwood_monument/top_0` | (8, 13, 1) | same piece, (21, 25, 1) | about 17.7 blocks |
| coniferous_monument | `coniferous_monument/bottom_1` | (24, 36, 14) | other piece: `top_3`, (23, 28, 29) | in another piece |

- **NBT of each spawner**: `SpawnData.entity.id = minecraft:breeze`, `SpawnCount 4`, `MaxNearbyEntities 6`, `RequiredPlayerRange 16`, `MinSpawnDelay 200`, `MaxSpawnDelay 800`, `SpawnRange 4`, `SpawnPotentials []`.
- **Free volume** around the hunter monolith's spawner: a corridor 2 blocks high, between the floor (`cracked_deepslate_tiles`) and the ceiling (cross-section view of the `.nbt`).
- **The monuments' spawners** are placed on a `chiseled_tuff_bricks` pedestal.
- **The Elderheart rooms** have 11 to 23 blocks of free height, but only 3 to 9 blocks of width at human height (computed on the `.nbt`).
- **Consequence: a boss fits neither in the spawner's place, nor in the heart of the room.** It must be placed on the ground, at the foot or entrance of the monument [deduced].
- **The monoliths float** (`start_height absolute 20`, projected onto `WORLD_SURFACE_WG`). The monuments start from `absolute -24`.
- **The jar's lore (lang `en_us`)** gives the tone of each place:
  - hunter: desert hunter tribes, agility and precision;
  - warrior: the strength of heroes, present in myths;
  - sentinel: monks who fled needless fighting;
  - cherry: a gift of divine twins, springs of life, healing;
  - oak: elven manuscripts, fertility;
  - conifers: a temple built by the dwarves of the taiga and mountains, architecture and mining.

### 1.2 The Elderheart can be sealed [AR 1.5.2 `Elderheart` bytecode]

- `tick()`: `if (tickCount % 100 == 0 && blessing != null)`, then it looks for `getNearestPlayer(this, 16.0)` and calls `setBlessing`. **If `blessing` is `null`, it gives nothing.**
- `readAdditionalSaveData` reads `PossibleBlessings` (a tag) and `Blessing`. `CompoundTagAdapter.readValue` returns `null` if the key is absent (`contains`, otherwise `aconst_null`).
- The blessing is drawn **once**, in `finalizeSpawn`, which the `MixinStructureTemplate` mixin calls when the structure is placed. It is then stored in the `Blessing` NBT.
- KubeJS 2101.7 exposes `entity.nbt` for reading and writing (`kjs$getNbt` / `kjs$setNbt`, which calls `load`), `persistentData`, `EntityEvents.spawned` and `EntityEvents.death` [KubeJS jar: `EntityKJS`, `EntityEvents`].
- **Hence**: removing `Blessing` seals the Elderheart, and putting it back releases it. `PossibleBlessings` (`ancient_remnants:<monument>`) tells the script which type of monument it is.

### 1.3 The candidates in the current pack

Status in Tilth (`origin/main`): none of these mobs is in `bannedVanilla` (which lists only vanilla). No config for these mods is in `pack/config` (only `quark-common.toml` and `supplementaries-common.toml` are there). The only existing rules are the In Control drafts (`plan/kubejs-brouillons/config/incontrol/spawn.json`):
- Mowzie's: `minlight 8` → deny, `maxdist 40` → deny, at most 2 per player;
- Alex's Mobs Guster: `minlight 8` → deny, at most 3;
- Mauler: 60% denied.

**These rules do not apply to a summon by command.** In Control reads `"when"`, which is `POSITION` by default (`SpawnRule` in the In Control jar). The `POSITION` case corresponds to the `MobSpawnEvent.PositionCheck` event, which natural spawning and spawners trigger, but not `/summon` [deduced from the In Control and NeoForge code].

#### The retained guardians

**Ferrous Wroughtnaut** (Mowzie's, `mowziesmobs:ferrous_wroughtnaut`)
- **Look and tone**: a giant iron armor holding an axe. It stays motionless until struck, then returns to its place. It is not a monster: it is the guardian of an ancient room.
- **Boss**: yes. Key `ferrous_wroughtnaut.has_boss_bar`; also `heals_out_of_battle` and `reset_health_when_respawn` [jar config]. 40 HP, 30 damage [`createAttributes`]. It is vulnerable only in certain phases [Mowzie's wiki].
- **In Tilth**: present, no config. An "anomaly" mob of 0003.
- **How it appears**: through a `wrought_chamber` structure, which contains the entity in the `.nbt` (`spacing 15`, `separation 5`, tag `has_mowzie_structure` = `#c:is_overworld`). `/summon` can also be used.
- **Tied to a single monument**: yes.
  - It returns to its place thanks to the `restPos` NBT, and it is dormant as long as `active` is false (`addAdditionalSaveData`).
  - `requiresCustomPersistence()` returns `true`: it never despawns.

**Frostmaw** (Mowzie's, `mowziesmobs:frostmaw`)
- **Look and tone**: a great snow yeti, which sleeps until disturbed. It is a legendary beast, not a horror monster.
- **Boss**: yes. Key `frostmaw.has_boss_bar`; also `stealable_ice_crystal`. 250 HP, 10 damage, tracking range of 50 blocks.
- **In Tilth**: present, no config. Plan 05 proposes a ×0.2 multiplier.
- **How it appears**: through the `frostmaw_spawn` structure, whose `.nbt` contains `{"id":"mowziesmobs:frostmaw","has_crystal":1}`, or through `/summon`.
- **Tied to a single monument**: yes, with a reservation.
  - `requiresCustomPersistence()` returns `getHasCrystal()`: it stays persistent **only if it has its crystal**.
  - It must therefore be summoned with `PersistenceRequired:1b`.

**Tongbi, the Sculptor** (Mowzie's, `mowziesmobs:sculptor`)
- **Look and tone**: a stone-sculptor monk. In exchange for an item, he proposes a trial: climbing within a time limit a course of rocks that he makes rise. He fights only if attacked. He is enigmatic and ancient, and it is a trial, not a fight.
- **Boss**: yes (`has_boss_bar`), with a trial timer. 140 HP.
- **Settings** [jar config]:
  - `test_height`: 60 by default, from 1 to 500;
  - `test_time_limit`: 400 s;
  - `trade_which_item` and `trade_how_many` (1 to 64);
  - `disappear_after_reward`: he disappears after the reward;
  - `heals_out_of_battle`.
- **Reward**: the table `mowziesmobs:entities/sculptor_test` (Earthrend Gauntlet), which can be overridden.
- **In Tilth**: present, no config.
- **How he appears**: through the `monastery` structure (mountain pillar), or through `/summon`.
- **Tied to a single monument**: yes, persistent (`requiresCustomPersistence` = `true`). He needs open sky over the trial height (message "Test is blocked!").

**Sacred Pontiff / His Holiness** (Companions!, `companions:sacred_pontiff`)
- **Look and tone**: a high priest in a green moss robe, with a gold crown and mask and a wooden staff (texture `sacred_pontiff_0.png`). He stays **inert** (state 0) until **a player touches him by hand**: `mobInteract` then triggers `cycleState` and the `PONTIFF_ACTIVATE` sound. His second form **petrifies** on dying (textures `his_holiness_petrification0..20`). It looks like a temple statue awakening.
- **Boss**: yes, red bar `TrackedServerBossEvent`, in two phases. `setDarkenScreen(true)` and `setCreateWorldFog(true)` are **hard-coded**. He has boss music (`IBossMusicProvider`, no config key in `KnightLibConfig`).
- **Default hit points**: about 225 then 350 (fields `SACRED_PONTIFF_MAX_LIFE` / `HIS_HOLINESS_MAX_LIFE` of `CompanionsConfig`; the order is deduced).
- **In Tilth**: present. According to 0003, the Companions! bosses are "to disable or make very rare". One per monument is "very rare".
- **How he appears**: through the `companions_monkey_temple` structure (entity in the `.nbt`; biomes `MONKEY_TEMPLE_STRUCTURE_BIOMES = "minecraft:jungle"`), or through `/summon`.
- **Tied to a single monument**: yes (`removeWhenFarAway` returns `false`).

#### The rejected candidates

**Umvuthi, the Sun Bird** (Mowzie's)
- **Look and tone**: a chief of a masked tribe, with Umvuthana. Tribal caricature; plan 08 already makes it an art direction question.
- **Boss**: yes (`has_boss_bar`).
- **In Tilth**: present, and the question remains open (08, row 10).
- **How it appears**: through the `umvuthana_grove` structure.
- **Verdict: rejected** for tone.

**Naga, Foliaath, Grottol, Lantern** (Mowzie's)
- **Look and tone**:
  - Naga: flying cliff serpent;
  - Foliaath: jungle carnivorous plant;
  - Grottol and Lantern: small peaceful creatures.
- **Boss**: no.
- **In Tilth**: present.
- **How they appear**: natural spawning.
- **Verdict: rejected**, they are not guardians. The Grottol (which eats ores) would be a nod to the dwarf monument, nothing more.

**Guster** (Alex's Mobs)
- **Look and tone**: a small desert sand tornado. It is the closest equivalent to the breeze.
- **Boss**: no.
- **In Tilth**: limited by In Control.
- **How it appears**: naturally; one could also put one in the spawner.
- **Verdict: possible variant** for the hunter monolith, but as a spawner (it respawns) and not as a unique guardian.

**Spectre, Farseer, Murmur, Skreecher** (Alex's Mobs)
- **Verdict: rejected**, horror tone.

**Underminer** (Alex's Mobs)
- **Look and tone**: a ghost of a dwarf miner [deduced from the name and the wiki].
- **Boss**: no. Peaceful.
- **Verdict: rejected as a guardian.** It could be kept as an ambient presence near the conifer monument.

**Tuff Golem** (Friends & Foes)
- **Look and tone**: a tuff statue that comes alive, peaceful. The monuments are made of tuff.
- **Boss**: no.
- **In Tilth**: appears only in strongholds, which are removed (plan 05).
- **Verdict: to keep as living decor**, not as a guardian.

**Wildfire, Iceologer, Illusioner** (Friends & Foes)
- **Boss**: Wildfire, yes.
- **In Tilth**: already to be disabled (0003).
- **Verdict: rejected.**

**Karkinos** (Hybrid Aquatic, `hybrid_aquatic:karkinos`)
- **Look and tone**: a giant crab (folder `entity/miniboss`) that summons minions (`Summoning`).
- **Boss**: mini-boss with a bar (`BossEvent` in `KarkinosEntity`).
- **In Tilth**: present.
- **How it appears**: through `/summon`.
- **Verdict: option** for the sentinel monolith (underwater). The sentinel and its aegis go with a carapace; but the monks' lore rather says "no fighting".

**Kodama** (Ghost)
- **Look and tone**: small tree spirits, Japanese [name; spawn hard-coded in `ghosts_mob_spawns`].
- **Boss**: no.
- **Verdict: gentle presence** possible at the cherry monument, to check in game.

**Living pot, living urn, buried** (NML)
- **Look and tone**: small mobs of the alchemist ruins [entity names].
- **Verdict: rejected**, too small for a guardian.

**Forgotten** (Quark)
- **Look and tone**: a ghost skeleton.
- **Verdict: rejected**, undead.

### 1.4 The new mods checked [Modrinth, NeoForge 1.21.1]

**Bosses of Mass Destruction (Forge/NeoForge)**
- **Version**: 1.3.3 (2026-07-17), `BOMD-NeoForge-1.21-1.3.3.jar`, 1.95 MB.
- **Dependencies**: `cerbons-api` 1.3.0 (0.25 MB), `cloth-config` 15.0.140 (1.16 MB), GeckoLib (already present).
- **What it adds**: 4 bosses (Night Lich, Obsidilith, Nether Gauntlet, Void Blossom) and 4 structures.
  - The structures' biomes are **datapack tags** (`has_structure/<id>.json`): everything can be disabled.
  - The Obsidilith is an obsidian obelisk with purple runes (jar texture).
  - Settings (`ObsidilithConfig`): 300 HP, 14 armor, 16 damage, 1,000 XP, `spawnPillarOnDeath` (true by default), `anvilAttackExplosionStrength` (4).
  - **It places obsidian**: `PillarAction` calls `setBlockAndUpdate(OBSIDIAN)`.
  - It does not despawn (`preventDespawnExceptPeaceful`).
- **Verdict**: **first choice if a mod is added**. Only the Obsidilith is kept, as a monolith guardian. Reservation: purple color, End atmosphere, and blocks placed in the monument.

**Legendary Monsters**
- **Version**: 2.2.3 (2026-09-17), 44.75 MB, no dependency, ARR license.
- **What it adds**: about 37 entities and 14 structures. Among them, Overgrown Colossus (mossy temple), Frostbitten Golem, Dune Sentinel and Ancient Guardian are thematically ideal. It also contains headless knights and abominations.
- **Disableable?** Yes in theory: structures through `has_structure/*` tags, spawns through the `bomber` and `chorusling` biome_modifiers.
- **Verdict: rejected.** It is exactly the "20 bosses and dungeons" case: too heavy.

**Ivar's Bosses**
- **Version**: 1.0.0 (2026-07-12), 0.84 MB, MIT, a single version.
- **What it adds**: Fang, "guardian of the forest" summoned by ritual. But also Spider Mother, Fenrir and Dracula, and reworks of the Warden and the Dragon.
- **Verdict: rejected** (horror, rituals).

**LAir's Bosses**
- **Version**: 1.0.0 (2024-12-08), 0.43 MB, made with MCreator.
- **What it adds**: the Forest Spirit, triggered by an "eco-score", and Zeus with the Olympus structure.
- **Verdict: rejected** (abandoned, MCreator).

**The Gadyn**
- **Version**: 1.0.0, 0.28 MB, MCreator, ARR license.
- **What it adds**: a neutral titan that appears naturally (biome_modifier).
- **Verdict: rejected** (unknown quality).

**WhirlWind**
- **Version**: 1.21.1 (2024-12-23).
- **What it adds**: a breeze variant.
- **Verdict: rejected**, it has the same flaw as the breeze.

**EEEAB's Mobs [Unofficial]**
- **Version**: 1.0.2 (2026-09-04), 29.3 MB.
- **What it adds**: Nameless Guardian, but also corpses, blood and "Bloody Acolytes" (jar lang).
- **Verdict: rejected** (gore, and it is an unofficial port).

**Mebahel's Creatures – Dwarven Automatons**
- **Version**: 1.4.0 (2026-10-06), a **Fabric** jar, usable on NeoForge through Connector.
- **Dependencies**: Mebahel's API and Mebahel's Elder Scrolls Core.
- **What it adds**: a whole faction of dwarven automatons, ruins, equipment.
- **Verdict: rejected** (too much content, ARR license), although the theme fits ("the machines of a lost civilization guard its ruins").

**L_Ender's Cataclysm, Bosses'Rise, Aquamirae**
- **Verdict: rejected.** These are dozens of bosses and dungeons, and Aquamirae has a horror atmosphere.

---

## 2. The concrete links to build

### 2.1 Which guardian for which monument

A different guardian per type is consistent, since AR's lore gives each place an identity.

| Monument | Guardian | Why | Blessing sealed? |
|---|---|---|---|
| warrior_monolith | **Ferrous Wroughtnaut** | "The monolith of heroes": the armor of a warrior of old still keeps watch. He sleeps until the first blow and returns to his place. | yes, until his death |
| coniferous_monument | **Frostmaw** | The temple of the dwarves of the taiga and mountains; the Frostmaw is the beast of the snows, sleeping. | yes, until his death |
| hunter_monolith | **Tongbi, the Sculptor** | "Agility and precision": his trial is an agility course, not a fight. | yes, until the trial is passed (he disappears) or his death |
| oakwood_monument | **Sacred Pontiff** | A moss priest, in the mossy elven temple. One chooses to awaken him. He petrifies on dying. | yes, until his death |
| cherry_monument | **no combat guardian** (possibly Kodamas) | "Monument of life", which heals. A boss there would contradict the text. | no |
| sentinel_monolith | **none** (option: Karkinos) | It has no spawner, and its monks "fled needless fighting". | no |

That makes 4 guardians at most, all unique, asleep or passive until provoked, and who do not leave their monument. This is the definition of "rare and territorial" (0003).

### 2.2 Removing the breeze spawners (datapack)

**Override the two processor_lists at the same path**: `data/ancient_remnants/worldgen/processor_list/monolith.json` and `monument.json`. The original `block_ignore` is kept and the spawner is replaced with a block from the monument's palette:

```json
{
  "processors": [
    { "processor_type": "minecraft:block_ignore",
      "blocks": [ { "Name": "minecraft:air" }, { "Name": "minecraft:structure_block" } ] },
    { "processor_type": "minecraft:rule",
      "rules": [ {
        "input_predicate":    { "predicate_type": "minecraft:block_match", "block": "minecraft:spawner" },
        "location_predicate": { "predicate_type": "minecraft:always_true" },
        "output_state":       { "Name": "minecraft:chiseled_tuff" }
      } ] }
  ]
}
```

- The spawner's NBT stays attached to the block, but `chiseled_tuff` has no block entity: it is ignored on placement [deduced from `StructureTemplate`].
- Do not use a simple `block_ignore` of the spawner: the terrain block from before would stay in place, and would leave a hole or stone in the middle of the pillar [deduced].
- To be done **before** generating the world: monuments already generated keep their spawners.

### 2.3 Spawning the guardian, sealing and releasing (KubeJS)

This is the recommended method. It merges with `clairieres_elderheart.js` (report 4, §2.3) and requires neither a modified binary file nor a custom mod.

```js
// server_scripts/gardiens_monuments.js — SKETCH to test (KubeJS 2101.7)
const UUID = Java.loadClass('java.util.UUID')
const GARDIENS = {
  'ancient_remnants:warrior_monolith':    { id: 'mowziesmobs:ferrous_wroughtnaut', nbt: '' },
  'ancient_remnants:coniferous_monument': { id: 'mowziesmobs:frostmaw',            nbt: 'has_crystal:1b,' },
  'ancient_remnants:hunter_monolith':     { id: 'mowziesmobs:sculptor',            nbt: '', cielOuvert: true },
  'ancient_remnants:oakwood_monument':    { id: 'companions:sacred_pontiff',       nbt: '' }
}

// (1) Seal: as soon as the Elderheart enters the world (structure placement or chunk load)
EntityEvents.spawned('ancient_remnants:elderheart', event => {
  const e = event.entity, pd = e.persistentData
  if (pd.contains('tilth_type')) return                     // already handled
  const nbt = e.nbt
  const type = String(nbt.getString('PossibleBlessings'))
  pd.putString('tilth_type', type)
  if (!GARDIENS[type] || !nbt.contains('Blessing')) return
  pd.putString('tilth_benediction', nbt.getString('Blessing'))
  event.server.scheduleInTicks(1, () => { const n = e.nbt; n.remove('Blessing'); e.nbt = n })
})

// (2) Awaken: the first time a player comes within 48 blocks
ServerEvents.tick(event => {
  if (event.server.tickCount % 100 !== 0) return
  event.server.players.forEach(p => {
    p.level.getEntitiesWithin(p.boundingBox.inflate(48)).forEach(e => {
      if (String(e.type) !== 'ancient_remnants:elderheart') return
      const pd = e.persistentData, g = GARDIENS[String(pd.getString('tilth_type'))]
      if (!g) return
      if (!pd.contains('tilth_gardien')) {
        const pos = global.tilthSolDuGardien(p.level, e.blockPosition(), g.cielOuvert) // ground at the foot of the monument
        const lien = 'tilth_g_' + e.uuid.toString().substring(0, 8)
        event.server.runCommandSilent(`execute in ${p.level.dimension} run summon ${g.id} ${pos.x} ${pos.y} ${pos.z} {${g.nbt}PersistenceRequired:1b,Tags:["tilth_gardien","${lien}"]}`)
        const garde = p.level.getEntitiesWithin(AABB.ofSize(pos, 8, 8, 8)).find(x => x.tags.contains(lien))
        if (garde) { garde.persistentData.putString('tilth_elderheart', e.uuid.toString()); pd.putString('tilth_gardien', garde.uuid.toString()) }
      } else if (pd.contains('tilth_benediction') && g.cielOuvert) {
        // Sculptor: disappeared after the reward, without dying → trial passed
        if (!p.level.getEntity(UUID.fromString(pd.getString('tilth_gardien')))) global.tilthLibererElderheart(e, p)
      }
    })
  })
})

// (3) Release: on the guardian's death
EntityEvents.death(event => {
  const g = event.entity
  if (!g.tags.contains('tilth_gardien')) return
  const eh = event.level.getEntity(UUID.fromString(g.persistentData.getString('tilth_elderheart')))
  if (eh) global.tilthLibererElderheart(eh, event.source.player)
})

global.tilthLibererElderheart = (eh, joueur) => {
  const pd = eh.persistentData
  if (!pd.contains('tilth_benediction')) return
  const n = eh.nbt; n.putString('Blessing', pd.getString('tilth_benediction')); eh.nbt = n
  pd.remove('tilth_benediction')
  global.tilthOuvrirClairiere(eh.level, eh.blockPosition(), 32, joueur)   // report 4, §2.3
  eh.block.popItem(Item.of('ancient_remnants:research_scrap',
    `[ancient_remnants:stored_research=["${pd.getString('tilth_type')}"]]`)) // this monument's page
}
```

What this script ties together:
- **the mist**: the clearing opens when the guardian falls, not on mere approach. For the cherry and the sentinel, with no guardian, the approach trigger of report 4 is kept. The Pontiff darkens the sky and thickens the fog during the fight (hard-coded): the mist lifting at his death becomes a high point;
- **the codex**: the monument's page as a reward (component `ancient_remnants:stored_research`, report 4 §1.3);
- **the Elderheart**: the blessing becomes the guardian's prize.

`global.tilthSolDuGardien` remains to be written:
- test 16 directions at 10–20 blocks from the Elderheart;
- take `level.getHeight('motion_blocking_no_leaves', x, z)`;
- check 4 blocks of air above, and `canSeeSky` for the Sculptor;
- fall back to the Elderheart's position minus 4 blocks if nothing fits [deduced].

### 2.4 Configs and datapack accompanying the guardians

**Mowzie's Mobs** (common config):
- `ferrous_wroughtnaut`, `frostmaw` and `sculptor`: `has_boss_bar` to true, `heals_out_of_battle` to true, `reset_health_when_respawn` to true;
- `sculptor.test_height` to about 30 (instead of 60); `test_time_limit` to keep;
- `sculptor.trade_which_item`: propose an item from the place's story, for example `minecraft:amethyst_shard` (already in the codex recipe). `ancient_remnants:research_scrap` can be tried, but the item is consumed: to test;
- `sculptor.disappear_after_reward` to **true**. The script uses it to detect the passed trial.

**Making the guardians unique** (datapack):
- override `data/mowziesmobs/worldgen/structure/wrought_chamber.json` and `frostmaw_spawn.json` with `"biomes": "#tilth:aucun"`, an empty tag;
- do the same for `monastery.json`, if the Sculptor should exist only at the monolith;
- Companions!: set `MONKEY_TEMPLE_STRUCTURE_BIOMES` to a nonexistent biome, to test (the value is a string, `"minecraft:jungle"` by default);
- otherwise, the pack will also have Wroughtnauts in underground rooms, about every 15 chunks (`wrought_chambers`, `spacing 15`).

**Rewards** (loot tables, optional):
- override `mowziesmobs:entities/sculptor_test` if the Earthrend Gauntlet is not wanted;
- `ferrous_wroughtnaut` gives the Wrought Helm and the axe; `frostmaw` gives an ice_crystal and a disc.

**Wildex, Exposure**:
- one bestiary page per guardian;
- the advancement `exposure:frame_exposed` (`entities_in_frame`), for example "photograph the awakened Pontiff".

---

## 3. Conflicts and risks

1. **Bypassing the guardian.** Without the seal, you walk within 16 blocks of the Elderheart and receive the blessing without doing anything. The Wroughtnaut does not move until struck, and the Pontiff until touched. The seal (§2.3) settles this point.
2. **Space.** Bosses do not fit in the 2-block corridors where the spawners are (§1.1). They are placed on the ground, outside, and the offsets must be adjusted type by type in game.
3. **The Frostmaw's persistence.** It is persistent only with its crystal. `PersistenceRequired:1b` is therefore mandatory, otherwise the guardian may disappear without dying, and the seal stays closed forever.
   - Safeguard to add: an op command `/function tilth:liberer_elderheart`, or a test "guardian not found for more than N days".
4. **`setNbt` on the Elderheart.** This calls `Entity.load` on a living entity. To test: position, UUID, sound, and the blessing actually null.
5. **`EntityEvents.spawned`.** It must be verified that it is raised for an entity **loaded from disk**, or at the first load of a chunk pregenerated by Chunky. Otherwise, the seal must be done in the polling, with a radius of at least 48 blocks, since a sprinting player covers about 28 blocks in 5 s.
6. **The Sacred Pontiff is heavy.**
   - Darkened screen, fog and boss music are not adjustable. To cut the music, a resource pack with an empty sound is needed.
   - About 575 HP in total, in two phases.
   - For an early game, it is hard: to balance with `SACRED_PONTIFF_MAX_LIFE` / `HIS_HOLINESS_MAX_LIFE`.
7. **The Sculptor needs open sky** over the whole height of the trial. His geomancies create rock entities: check that they do not break monolith blocks. No `setBlock` was found in the Sculptor, Wroughtnaut or Frostmaw classes.
8. **Damage to the monument.** No `setBlock` was found in the Pontiff's classes (including the fire rings). The oak monument's leaves and fire must still be checked.
   - The Obsidilith (if BoMD is added) **places obsidian**: `spawnPillarOnDeath=false` and `anvilAttackExplosionStrength=0` limit the damage, without eliminating it.
9. **In Control.** If a spawner were chosen instead (variant B, §4), spawning would go through `PositionCheck`, and the Mowzie's rules in the drafts would block it (`minlight 8` in a lit monument). With `/summon`, this is not the case (§1.3).
10. **AR updates.** The script depends on `PossibleBlessings`, `Blessing` and the id `ancient_remnants:elderheart`, and on the processor_list of the current paths. If AR changes its pieces, it is enough to recheck these points; no `.nbt` is copied into Tilth.
11. **Multiplayer.** There is one guardian per monument, once. After its death, the blessing is free for everyone, which is consistent with a permanent world.

---

## 4. To adapt, remove or add in the plan

**To replace**
- **Report 4, §2.2 (options A, B, C)**: replace with "processor_list that replaces the spawners + unique guardian through KubeJS + Elderheart seal" (§2.2 and 2.3 here).
  - Option A (keep the breeze) is to be abandoned: it contradicts the exclusion list and the art direction.
  - Option C (no guardian) remains true only for the cherry and the sentinel.
- **Report 4, §2.3**: the clearing opens at the **release** of the Elderheart, and not at the first approach, when the monument has a guardian.
- **Chapter 12, row 15 of the table** ("Guardians: none"): put "4 unique guardians, taken from Mowzie's and Companions!".
- **Chapter 11 §11.8**: "At the center, an Elderheart guarded by creatures" becomes "guarded, depending on the monument, by a unique guardian that must be defeated or whose trial must be passed".

**To add**
- **Plan 05 §5.3 (Mowzie's)**: disable the natural generation of `wrought_chamber`, `frostmaw_spawn` and, possibly, `monastery` (§2.4). Mowzie's figures then become monument guardians, no longer just anomalies.
- **Decision 0003**: add a line "Guardians of the AR monuments: Wroughtnaut, Frostmaw, Sculptor (Mowzie's), Sacred Pontiff (Companions!); one per monument, never in natural spawning".
- **Configs** to create in `pack/config`: Mowzie's common config (§2.4) and Companions!'s (Pontiff hit points, temple biomes).
- **Datapack** `kubejs/data/`:
  - `ancient_remnants/worldgen/processor_list/{monolith,monument}.json`;
  - the overrides of `mowziesmobs/worldgen/structure/*.json`;
  - the empty tag `tilth:aucun`.

**Not to do**
- No new mod. If the maintainer wants a "real" boss at the hunter monolith instead of the Sculptor's trial, BoMD is added (Obsidilith only, four structures disabled by tags).

**Variants, if the script were to fail**
- **B, without a script**:
  - override the 5 template_pools that contain a spawner (`cherry_monument/top_3`, `coniferous_monument/bottom_1`, `hunter_monolith/middle`, `oakwood_monument/top_0`, `warrior_monolith/middle`) so that they point to a processor_list specific to each type;
  - in each, a `spawner` → `spawner` rule with `block_entity_modifier` `minecraft:append_static` (`SpawnData`, `SpawnCount 1`, `MaxNearbyEntities 1`);
  - limit: a spawner makes the mob **respawn endlessly**. This is acceptable for a small guardian (Gusters at the hunter monolith), not for a boss.
- **C, entity in the `.nbt`** (as Mowzie's does): rewrite 5 AR `.nbt` files. This poses three problems: a binary to maintain, Obscuria's OML license, and rooms that are too narrow. Not recommended.
- **D, trial spawner** placed by a processor: it is almost a vanilla "only once", but with the trial chamber cage, a rearm delay and loot keys. It breaks the art direction. Not recommended.
- **Custom mod**: unnecessary here. KubeJS is enough, since the Elderheart is sealed through NBT.

---

## 5. Questions to test in game

1. Is `EntityEvents.spawned('ancient_remnants:elderheart')` raised at the first load of a monument, and also after Chunky pregeneration? Does the seal (`e.nbt = n` without `Blessing`) really prevent the "Blessing Acquired" title?
2. Is the blessing properly returned at the guardian's death, and does the Elderheart stay intact (sound, rays, position) after the two `setNbt` calls?
3. Placement positions at the foot of the 4 guarded monuments: does the boss fit? Does the Wroughtnaut set its `restPos` when summoned by `/summon`? Does the Frostmaw sleep on arrival (try `active:0b`)?
4. The Sculptor near the hunter monolith, with `test_height` 30: does the trial start (no "Test is blocked!")? Does `disappear_after_reward` properly trigger the release?
5. Sacred Pontiff near the oak monument:
   - balance in early and mid game;
   - rendering of the darkened screen with More Darkness and ATMOSPHERICS;
   - fire on the leaves;
   - music.
6. Do the In Control rules (drafts) and `spawns_safety.js` let the four summons through (expected: yes)?
7. With Mowzie's natural structures disabled by `#tilth:aucun`, does no Wroughtnaut room or wild Frostmaw appear any more (`/locate structure`)?
8. The spawner replaced by `chiseled_tuff`: correct rendering in the 5 pieces (no hole, no terrain block)?

---

## Recommendation

**First choice among the current mods: nothing to add.** A different guardian is used per monument type, taken from Mowzie's Mobs 1.8.2 and Companions! 1.3.4, which are already in the pack:
- **Ferrous Wroughtnaut** at the warrior monolith (sleeping armor, boss bar, returns to his place);
- **Frostmaw** at the conifer monument (sleeping snow beast, summoned with `has_crystal` and `PersistenceRequired`);
- **Tongbi the Sculptor** at the hunter monolith (an agility trial rather than a fight);
- **Sacred Pontiff** at the oak monument (a moss statue that one chooses to awaken, and that petrifies on dying);
- **no guardian** at the cherry or the sentinel, as their lore requires.

The evidence is in §1.3: NBT `active`, `restPos` and `has_crystal`, `requiresCustomPersistence`, the keys `has_boss_bar`, `test_height`, `disappear_after_reward`, the Pontiff's `mobInteract`, and his petrification textures.

**Method**:
- a **processor_list** that replaces the breeze spawners with chiseled tuff (datapack);
- a **KubeJS script** that:
  - seals the Elderheart by removing `Blessing` (it gives nothing if `blessing == null`, according to the bytecode of `tick()`);
  - spawns the guardian only once when a player approaches;
  - releases the blessing at the guardian's death, or when the trial is passed;
  - then opens the clearing and gives the monument's codex page.

This is the only method that guarantees a unique guardian per monument without rewriting AR's `.nbt` files, that bypasses the In Control rules, and that ties guardian, blessing, mist and codex into a single gameplay moment.

**First choice of new mod, if one is needed**: **Bosses of Mass Destruction (Forge/NeoForge) 1.3.3** (1.95 MB, with cerbons-api and cloth-config), reduced to the **Obsidilith**:
- its 4 structures are disabled by datapack tags;
- it is a living obsidian monolith, literally in line with the theme, with a boss bar and 300 HP;
- it does not despawn.

Reservations: it places obsidian, it has a purple End tint, and its three other bosses (including an undead lich) remain in the jar. It is recommended only if the maintainer prefers a real fight to the Sculptor's trial. Legendary Monsters would be thematically ideal (Overgrown Colossus, Dune Sentinel, Frostbitten Golem), but its 44.75 MB and 14 structures make it exactly the "20 bosses and dungeons" mod to be avoided.
