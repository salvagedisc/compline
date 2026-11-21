# Awakening: enchanting as a deed

`fallow` 0.44.0, `eveil` module, decision [0045](../decisions/0045-awakening.md). Enchanting is rare and earned: an item learns from what it has lived through, the strongest powers come from great beasts, and a worn-out item forgets. Nothing comes out of loot or trade already enchanted, and there are no enchanted books. Immersive Enchanting left the pack with this change.

## What the player lives

1. A freshly forged item bears nothing. From its first deed (a block mined, a creature beaten, a blow taken), it counts.
2. Past a threshold it awakens: a sound, a note « … a appris quelque chose : un éveil vous attend à la table d'enchantement », and the item shimmers.
3. Right-click an enchanting table with the item in the main hand: a one-row window shows the item between two enchanted books, the two powers its deeds lean toward. A click picks one; the item takes it (or the next level of one it already has), and its tooltip notes « Efficacité I : jour 143, <player> ».
4. Each further awakening asks three times more than the previous one (3,000, 9,000, 27,000 points with the defaults).
5. Great beasts leave a shard (« Éclat de grande bête (Frostmaw) ») that carries one strong enchantment: at the table, item in the main hand and shard in the other, it is laid once and takes one place.
6. When the item reaches the end of its durability (Overgeared's « broken » state, or one point before breaking), it forgets its last awakening: the enchantment goes back to its former level, a grey note says so, and its deeds drop back to the threshold of the awakening before, so it has to earn it again. It forgets only once until it is repaired.

The two choices are drawn from the item's deeds with a seed tied to the item and to the awakening, so closing and reopening the window shows the same two. An item whose deeds lean only toward powers it can no longer take (maximum level, conflicts) tells « Cet objet n'a plus rien à apprendre ».

## Places by forging quality

An item can carry as many awakenings as its Overgeared quality allows (`[eveil.places]` in `fallow-server.toml`): none or poor or well forged 1, expert or perfect 2, masterwork 3. The number is fixed at the item's first deed and shown in its tooltip (« Éveils : 1 sur 2 »). An item without quality (bow, fishing rod, shield, leather) has 1.

## Deeds

The deeds are data, one file per family of items in `data/fallow/eveil/` of the mod (another namespace can add files). Each entry names the deed, its target (a block, creature or damage tag, or a mode), its points per unit and the enchantments it leans toward, with a weight.

| Family (`objets`) | Deed | Points | Leans toward |
|---|---|---|---|
| Pickaxes | an ore mined (`#c:ores`) | 3 | Fortune 3, Efficiency 1, Unbreaking 1 |
| | a block mined (`#minecraft:mineable/pickaxe`) | 1 | Efficiency 3, Unbreaking 1, Silk Touch 0.5 |
| Axes | a log cut, or a TreeChop chop | 1 | Efficiency 3, Unbreaking 1 |
| | a creature beaten (any, undead, arthropod) | 15 | Sharpness 2, Smite 3, Bane of Arthropods 3 |
| Shovels | a block dug | 1 | Efficiency 3, Unbreaking 1, Silk Touch and Fortune 0.5 |
| Hoes | a crop harvested, a block of `mineable/hoe` | 1 | Fortune 3, Efficiency 3, Unbreaking 1 |
| Swords | a creature beaten | 15 | Sharpness 3, Looting, Sweeping Edge, Knockback, Unbreaking; Smite 4 on undead, Bane 4 on arthropods |
| Knives (`#c:tools/knife`) | a creature beaten | 15 | Backstabbing 2, Sharpness 2, Looting 2, Unbreaking 1 |
| Bows, crossbows, tridents | a creature beaten with it | 15 | Power, Punch, Flame, Infinity; Quick Charge, Piercing, Multishot; Impaling, Loyalty, Riptide, Channeling |
| Helmets | damage taken (per point); seconds under water | 5; 3 | Protection, Projectile Protection; Respiration 3, Aqua Affinity 1 |
| Chestplates, leggings | damage taken, by kind (fire, explosion, projectile) | 5 | Protection, Fire, Blast, Projectile Protection |
| Boots | damage taken; falls; blocks walked; blocks swum | 5; 10; 0.05; 1 | Protection, Feather Falling 4; Unbreaking; Depth Strider |
| Shields | damage blocked | 5 | Unbreaking |
| Fishing rods | a catch (`ItemFishedEvent`) | 30 | Luck of the Sea, Lure, Unbreaking |

A creature counts for its maximum health divided by 20, at most 10: a zombie gives 15 points to a sword, a chicken 3. Walking counts once a second, on the ground, not mounted, at most 12 blocks a second. Mending, Thorns and the other enchantments with no deed have no source.

The deeds of an item are kept by the world (`data/fallow_eveil.dat`, under the item's id), not on the item: its component changes only when it becomes ready, awakens or forgets, so the item in hand does not twitch at every block.

## Great beasts

| Beast | Shard | Chance |
|---|---|---|
| Frostmaw (Mowzie's Mobs) | Frost Walker II | always |
| Ferrous Wroughtnaut (Mowzie's Mobs) | Unbreaking III | always |
| Umvuthi (Mowzie's Mobs) | Smite IV | always |
| Naga (Mowzie's Mobs) | Respiration III | always |
| Tongbi, the Sculptor (Mowzie's Mobs) | Feather Falling IV | always |
| Sculksaur (Archie's Dinosaurs) | Swift Sneak III | one in four |
| Obsidiceratops (Archie's Dinosaurs) | Fire Protection IV | one in four |

Only when a player killed the beast. A shard sets the enchantment at its level (an item that already has it lower goes up), takes one place, and is used up. Each shard is a loot modifier `fallow:eclat_*` in the mod; more can be added as data (type `fallow:eclat_de_bete`).

## Nothing found enchanted

- The loot modifier `fallow:sans_enchantement` runs on every loot table: an item loses its enchantments, an enchanted book becomes a book (chests, Supplementaries urns and galleons, Friends & Foes citadels, Wilder Nature bounties, Enhanced Celestials dungeons, fishing, archaeology, creatures).
- Villagers and the wandering trader no longer offer anything enchanted: such an offer is dropped when it is made.
- The anvil refuses a second item that bears enchantments (`[xp] blockEnchantMerge`, now true by default): powers do not pile up by recycling.
- The vanilla enchanting table has no other use: a right-click never opens its window (Hands-On Vanilla leaves it alone, `enchanting_table=false`).
- The grindstone still strips enchantments; the item's history then drops the lost lines, and the place is free again.

## Settings (`fallow-server.toml`, `[eveil]`)

| Setting | Default | Meaning |
|---|---|---|
| `enabled` | true | Off: no awakening or shard, loot and trades keep their enchantments, the table is the game's own again |
| `seuil` | 3000 | Points for the first awakening |
| `facteur` | 3.0 | Each awakening asks this much more than the previous one |
| `places.none` … `places.master` | 1, 1, 1, 2, 2, 3 | Awakenings per forging quality |

Test commands (operators): `/fallow eveil voir` shows the held item's places, state, quality and points per deed against the next threshold; `/fallow eveil points <n>` adds points to the held item's first deed.

**Checked** on the test bench on 2026-11-21: the full pack's dedicated server starts with the module (15 families of deeds) and no new error. In a development client: a new iron pickaxe counts one point per stone block; with `/fallow eveil points 3000` it shimmers and the note shows; at the table the window shows Unbreaking I and Fortune I, the same two after closing and reopening; a click gives Fortune I with the tooltip line « day 1 » and « Awakenings: 1 of 1 »; a second right-click says the item has given all it could; set to its last point of durability it forgets Fortune I (grey note) and its points fall back to 0; a Frostmaw shard is refused on a sword and gives Frost Walker II to iron boots, then is used up; a stronghold library, a desert temple and an ancient city chest give plain books and an unenchanted hoe and leggings; four novice librarians and two novice weaponsmiths offer no enchanted book or sword. Not checked: Overgeared quality and places, a real kill of a great beast, the anvil, deeds of combat, armour, fishing and TreeChop (tests EV5 to EV9 in play).

## In-game tests

| # | Test | Expected |
|---|---|---|
| EV1 | Mine stone with a new pickaxe, then `/fallow eveil voir` | The deed `pioche.json#1` grows by one per block; the tooltip shows « Éveils : 0 sur N » |
| EV2 | `/fallow eveil points 3000` with that pickaxe | Note « … a appris quelque chose … », sound, the pickaxe shimmers, tooltip « Un éveil attend » |
| EV3 | Right-click an enchanting table with it | A one-row window: two books, the pickaxe in the middle; closing and reopening shows the same two; a click enchants and closes; tooltip line with the day and the player |
| EV4 | Right-click the table with a pickaxe that is not ready, with nothing, with a full item | « Cet objet n'a pas encore assez vécu. », « La table attend un objet qui a vécu. », « Cet objet a déjà donné tout ce qu'il pouvait. » |
| EV5 | Wear an enchanted pickaxe down to its last point (or Overgeared's broken state) | Grey note « À bout de forces, … oublie Efficacité I. », the enchantment goes, `/fallow eveil voir` shows the points brought back; repaired then worn out again, it forgets the next one |
| EV6 | Kill Frostmaw; at the table, boots in hand and the shard in the other hand | A shard « Éclat de grande bête (Frostmaw) » drops; the boots take Frost Walker II, the shard is used up; on a sword, « Cet éclat ne prend pas sur cet objet. » |
| EV7 | Open ten chests (desert temple, mineshaft, shipwreck), fish, trade with a librarian and a weaponsmith | No enchanted item or book anywhere; books appear as plain books |
| EV8 | Forge a masterwork pickaxe and a poorly forged one, then `/fallow eveil voir` after one block each | 3 places and 1 place |
| EV9 | Anvil: an enchanted sword with another enchanted sword | No result |
