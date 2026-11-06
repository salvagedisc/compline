# Enchanting: which mod can turn it into a real gameplay phase?

> Research of 2026-10-19, at the maintainer's request: before settling on Easy Magic, look for a mod that changes the way enchanting works in Minecraft, so as to have a new gameplay phase, as Overgeared and Kaleidoscope do.
> Sources: Modrinth API (search with the `neoforge` + `1.21.1` filters, then `/project/<slug>/version?loaders=["neoforge"]&game_versions=["1.21.1"]` for each candidate), jars downloaded and opened (`javap`, `strings`, JSON from `data/` and `assets/`), the `origin/main` repository (`docs/mods.md`, `pack/config/quark-common.toml`, `pack/mods/*.pw.toml`), the 2026-10-18 reports (`verif-2026-10-18/1-overgeared.md`, `4-monde-et-objectifs.md`), chapters 11 and 12 of the plan.
> "[jar]" = read in the jar; "[API]" = Modrinth API; "deduced" = conclusion drawn from the code or data, without an in-game test.

---

## 0. The essentials

1. **Recommendation: Immersive Enchanting** (`immersive-enchanting`, 6.0.1 for NeoForge 1.21.1, 7.1 MB of which 6.8 MB is two music discs, plus AlfinoLib 267 KB). Enchanting becomes a **library of knowledge**: one finds **Ancient Books** ("Knowledge of Sharpness") in ruins, stores them in **chiseled bookshelves** around the table, and each level is paid for in **materials** (a list per enchantment, in JSON) rather than by chance. Enchanted books disappear from loot and trades: they are **replaced by Ancient Books**. It is the exact equivalent of Overgeared's blueprints: one finds a piece of knowledge, keeps it, copies it.
2. **Fallback: Quark's Matrix Enchanting Table** (module `Matrix Enchanting`), **already in the Quark 4.1 jar** but turned off (`oddities = false`). It is the only real **mini-game** found: one draws pieces with lapis and XP, rotates them, places them on a grid, merges two pieces to raise the level, and **coloured candles** (or any block, via config) steer the draw. Zero MB added, French translation included. It lacks the "knowledge found" dimension.
3. **Easy Magic must not enter the pack**, whichever choice is made: it replaces the vanilla table block with its own, whereas Immersive Enchanting (like Quark Matrix and Enchanting Overhauled) hooks onto the vanilla table. Its only contribution (the table keeps the item and the lapis) is already in Immersive Enchanting and in Quark Matrix.
4. **No conflict with Overgeared**: Immersive Enchanting touches neither the anvil, nor the smithing table, nor the grindstone (only 4 mixins: table, table entity, `ItemStack` for the tooltip, villager trades). Overgeared does not touch enchanting. Overgeared's tools are in the vanilla tags (`minecraft:pickaxes`...), so they are enchantable.
5. **Everything is linked through data**, without a custom mod: costs in JSON (fauna, flora, Overgeared steel), the mod's own loot modifiers to put **themed** books into the NML ruins (archaeology included), KubeJS to give a book on defeating an Ancient Remnants guardian and at Wildex milestones.
6. Second fallback, riskier: **Enchanting Overhauled** (an altar awakened with a Tome that has been found, "themed" enchantments granted by the blocks placed around it). A very good idea for a place, but little used (4,100 downloads), ARR licence, and it rebalances a lot of things (3 enchantments per item, maximum level III, protection only on the chestplate, XP curve).

---

## 1. What is confirmed

### 1.1 The Modrinth sweep

Searches made with the filters `categories:neoforge` + `versions:1.21.1` (about forty queries: enchanting, enchantment, rune, engraving, socket, infusion, ritual, altar, glyph, tome, research, scholar, grimoire, pedestal, ancient library...). More than 430 projects reviewed; those that touch enchanting are classified below. NeoForge 1.21.1 versions checked one by one [API].

**Serious candidates (jar opened)**

| Mod | NF 1.21.1 version | Size | Dependencies | What it really does | Verdict |
|---|---|---|---|---|---|
| **Immersive Enchanting** | 6.0.1, 2026-08-07 | 7.1 MB | AlfinoLib ≥ 1.2.0 | Ancient Books to store, material costs, per-item enchantment tree (§1.2) | **Choice** |
| **Quark: Matrix Enchanting** | in Quark 4.1-485 (already in the pack) | 0 | none | Grid mini-game of pieces, influence of candles and chosen blocks (§1.3) | **Fallback** |
| **Enchanting Overhauled** | 2.1.6, 2026-06-01 | 668 KB | none | Altar to awaken with a Tome, themes from neighbouring blocks, major rebalancing (§1.4) | 2nd fallback |
| Apothic Enchanting | 1.21.1-1.6.4, 2026-10-08 | 5.3 MB | Placebo + Apothic Attributes | Bookshelves with statistics (Eterna, Quanta, Arcana), infusion at the table, new enchantments; 20 mixins [jar] | Ruled out: heavy, rebalances very high, Apothic Attributes reworks attributes (RPG side) |
| Modest Magic | 1.4.2, 2026-09-28 | 421 KB | EMI, Cloth Config (optional) | Altar + pedestals (infusion, enchanting, **summoning** of golems and the Wither), tablets placed at the smithing table, **one level** per enchantment; no shared mixin [jar] | Ruled out: flat progression (level I only), summonings out of tone |
| Enchantment Infusion | 1.3.4, 2026-10-12 | 172 KB | none | Table + pedestals in the style of Ars Nouveau, JSON recipes per enchantment and level | Ruled out: **requires NeoForge ≥ 21.1.252**, the pack has 21.1.251 (`neoforge.mods.toml` of the jar) |
| Easy Magic | v21.1.4, 2025-10-26 | 141 KB | Puzzles Lib | Comfort: the table keeps the item and the lapis, reroll button, enchantment preview; **replaces the vanilla block** (`BlockConversionHandler`, `EnchantmentTableWithInventoryBlock`) [jar] | **Remove from the plan** (point 0.3) |
| Enchanting Infuser | v21.1.4, 2026-03-30 | 195 KB | Puzzles Lib | New table where one chooses one's enchantments for a fixed price | Ruled out: removes chance without creating a phase |
| Create: Enchantment Industry | 2.5.4b (**beta**), 2026-10-09 | 1.6 MB | Create, Create: Dragons Plus | Blaze enchanter, liquid XP, book printer, blaze forger [jar, lang] | Ruled out: automates what we want to make precious, and the rule "not too many Create addons" |

**Ruled out on description and API**

| Mod | Reason (API evidence) |
|---|---|
| Ars Nouveau (5.13.3, 19.4 MB) | A whole system of spells, glyphs, familiars: too much |
| Iron's Spells 'n Spellbooks (3.16.3, 13.6 MB) | Spell RPG with schools of magic |
| Occultism (1.224.5, 6.8 MB, 4 dependencies) | Demon-summoning rituals: tone |
| Hexerei (0.5.0.3, 10.2 MB, 2025) | Witchcraft, cauldrons, brooms: tone, no enchanting |
| Enchanted: Witchcraft (4.2.7, 3 MB, 3 dependencies) | Full witchcraft |
| Hexalia (1.3.8.1, 3.2 MB) | Herbalism and "folk" rituals: pretty but not enchanting |
| Nature's Aura (41.10, 1.5 MB) | World aura and natural altars; does not touch enchanting |
| Botania | **No NeoForge 1.21.1** (latest: 1.20.1) |
| Malum (1.8.2, **32.8 MB**) | Creature souls, dark tone |
| Eidolon: Repraised (0.5.0.7, 4 MB) | Necromancy |
| Forbidden & Arcanus (2.6.1, 3.3 MB + Valhelsia Core) | Hephaestus forge, dark tone |
| Theurgy (1.78.0, 5.2 MB) | Transmutation alchemy |
| Reactive Alchemy (10.2.1, 1.5 MB) | Experimental alchemy, no enchanting |
| Spectrum (1.12.9, **97 MB**) | Enormous |
| Grimoire of Gaia (alpha), L_Ender's Cataclysm (71.7 MB) | Off topic (creatures, bosses) |
| Tome of Blood: Rebirth | No NeoForge 1.21.1 (Forge 1.20.1) |
| Better Enchanting, Enchanting System Overhaul | Fabric or Forge 1.20 only |
| "Enchantment Tables Revamped" | Not found on Modrinth |
| Enchantment Descriptions (21.1.11) | Tooltips only, two libraries (Prickle, Bookshelf); already ruled out in chapter 11. Immersive Enchanting displays its own descriptions |
| Runes of Power, Runeweaver, Runology (alpha) | Runes with active abilities: RPG |
| Craftable Enchanted Books, Enchantment Custom Table, Enchanting Plus+ (beta), Improved Enchanting (beta), Advanced Enchanting Table (2024) | Crafting or comfort stations, little maintained, no phase |
| Grind Enchantments, Dis-Enchanting Table, Infusion Table, Better Chiseled Bookshelf | Small tools (disenchanting, bookshelf power) |

### 1.2 Immersive Enchanting, read in the jar

Jar: `beta-immersiveenchanting-6.0.1-neoforge-1.21.1.jar` (the name contains "beta", but the API lists it as `release`), sha512 `9c65fcb0544a20e1…` [API]. Version published on 2026-08-07; the changelog fixes bugs numbered up to #76 ("XP levels now subtract correctly [#74]") and 26.1 ports follow in September: the mod is **maintained** [API].

| Point | Value | Evidence |
|---|---|---|
| Dependencies | NeoForge ≥ 21.1.209 (the pack has 21.1.251), Minecraft 1.21.1, **AlfinoLib ≥ 1.2.0** (267 KB, no mixin) | `META-INF/neoforge.mods.toml` of both jars |
| Mixins | 4: `EnchantingTableBlockMixin` (replaces the menu of the **vanilla** table), `EnchantingTableBlockEntityMixin` (the table **keeps its items**), `ItemStackMixin` (tooltip), `VillagerTradeMixin` (removes books from trades) | `immersiveenchanting.mixins.json`, `javap` |
| Ancient Book | Item `immersiveenchanting:ancient_book`, carries the enchantment in the vanilla component `minecraft:stored_enchantments` (deduced from `EnchantmentUtil.setStoredEnchantment` and `DataComponents.STORED_ENCHANTMENTS`); component `immersiveenchanting:replicated` on copies | `RemoveEnchantedBooksModifier`, `ModDataComponents` |
| Unlocking | The book must be **in a chiseled bookshelf** (`ChiseledBookShelfBlockEntity`) within reach of the table: 2 blocks in X and Z, 3 in Y by default (adjustable up to 8) | `BookshelfChecker`, `ServerConfig` (`bookshelfSearchX/Y/Z`) |
| Paying | One file per enchantment: `data/immersiveenchanting/enchantment_costs/<mod>/<enchantment>.json`, `{"enabled": true, "levels": {"1": [{"item_cost": {...}, "xp_levels": n}]}}`. `enabled` is optional (true by default), and so is `xp_levels`. The fuel (lapis, tag `neoforge:enchanting_fuels`) is paid on top: 1 per level | `CostData` (`optionalFieldOf("enabled", true)`), `Cost.xpLevels()`, `enchanting_fuels.json` |
| Default costs | Sharpness: cactus → amethyst → prismarine → quartz → echo; Protection: armadillo scute → turtle scute → nautilus → shulker shell; Fortune: golden apple → emerald block → **pitcher plant** (the sniffer's); Mending: Nether star | `enchantment_costs/minecraft/*.json` (43 files) |
| Enchantment without a file | **Disabled**, with a warning at launch | lang `warn.cost_files_missing`, command `disabled_enchantments` |
| Copying a book | "Replicate": a book and quill + 10 levels; the copy can no longer be transmuted | `replicate.json`, lang `not_transmutable` |
| Transmuting | Changes the enchantment of an ancient book (10 levels), only if a book of the same enchantment is already stored | `transmute.json`, lang `transmute_hint` |
| Removing an enchantment | "Hold to remove": hold the click to remove it from an item (adjustable) | lang, `allowEnchantmentRemoval = true` |
| Mystery | Locked enchantments are displayed in the Standard Galactic Alphabet (`obfuscateLockedEnchantments = true` by default) | `ServerConfig` |
| Loot | Enchanted books in **all** loot are **replaced** by ancient books (`REPLACE` mode by default; `REMOVE` possible). Plus 22 modifiers that add them to vanilla chests, with two modes: `random_enchantment_from_enabled` or `random_enchantment_from_list` + list | `loot_modifiers/*.json`, `ServerConfig$EnchantedBookLootMode` |
| Trades | No more enchanted books from villagers (`disableEnchantedBookTrades = true`) | `ServerConfig` |
| Structures | 3 "ancient libraries" (desert, jungle, End), placed on the vanilla tags `has_structure/desert_pyramid`, `jungle_temple`, `end_city`, spacing 50; a processor fills their chiseled bookshelves | `worldgen/`, `FillChiseledBookshelfProcessor` |
| Interface | Two tabs: "Enchanting" (the level tree for the placed item, zoomable) and "Ancient Books" (unlocked / locked filter, total): a **collection** whose progress can be seen | lang `tab.book`, `filter.unlocked`, `label.total_enchantments` |
| Sounds | Node hover sounds adjustable through data (`node_sounds.json`) | `datapack/node_sounds` |
| Config | **SERVER** (`ModConfig.Type.SERVER`), hence `serverconfig/immersiveenchanting-server.toml` in the world; to be shipped in `defaultconfigs/` | `ImmersiveEnchanting.registerConfig` |
| Commands | `disabled_enchantments`, `enabled_enchantments`, `generate_empty_datapack` (generates the cost files to fill in), `give_random_ancient_book` | `command/` |
| Languages | **en_us only** | `assets/immersiveenchanting/lang/` |
| EMI / JEI | No plugin (no `dev/emi` or `mezz/jei` class); costs are read in the mod's interface | `grep` of the jar |
| Weight | Code and data: 550 KB; `arcane_memories.ogg` 3.7 MB and `biblioclasm.ogg` 3.1 MB (two discs, streamed only if they play). The scan of the bookshelves only happens when the table is opened (5 × 7 × 5 blocks by default): **negligible** (deduced) | file sizes |

Note: the Modrinth page still says that Mending is "unobtainable" and adjustable by menu. In 6.0.1, no class mentions Mending; only its cost file exists (Nether star). It is therefore turned off with `"enabled": false` [jar].

### 1.3 Quark Matrix Enchanting, read in the Quark 4.1-485 jar

The chapter on decision 0005 does not mention it because the module lives in the `oddities` category, turned off in `pack/config/quark-common.toml` (`oddities = false`).

| Point | Value | Evidence |
|---|---|---|
| Presence | `org/violetmoon/quark/addons/oddities/module/MatrixEnchantingModule` in the pack's jar; its neighbours: Backpack, Crate, Magnets, Pipes, Tiny Potato, Totem of Holding | `c-quark` [jar] |
| Mechanic | The table keeps the item and the lapis. One **generates** pieces with lapis (4 pieces per lapis) and levels, **rotates** them, **places** them, **removes** them on a grid, and **merges** two pieces of the same enchantment to raise a level | `OPER_ADD/PLACE/REMOVE/ROTATE/MERGE`, lang `quark.jei.hint.matrix_enchanting` |
| Place | Bookshelves: up to 15; 3 pieces without a bookshelf. **Coloured candles** placed on the bookshelves: each colour pushes a list of enchantments (white → Unbreaking, orange → Fire Protection...); placed on soul sand, they repel | `maxBookshelves = 15`, `baseMaxPieceCount = 3`, `influencesList`, `soulCandlesInvert` |
| Chosen blocks | `statesToInfluences`: any block state can influence, format `"block[state];strength;colour;enchantments"`; `IEnchantmentInfluencer` API for other mods | module config texts |
| Books | Possible (1 base piece); treasures forbidden by default (`allowTreasures = false`), enchantments outside the tag `minecraft:in_enchanting_table` forbidden | same |
| Conversion | The vanilla table becomes a matrix table when placed (`automaticallyConvert = true`) | same |
| Languages | Quark has `fr_fr.json`: "Table d'enchantement matricielle" | `assets/quark/lang/` |
| Quark's other enchanting modules | `Ancient Tomes` **off**, `Color Runes` **off**, `Uses For Curses` **off**, `Soul Candles` (enchanting power of soul candles) **off**, `Improved Tooltips` **on** (with `enchantingTooltips`: a book's tooltip shows the items concerned), `Variant Bookshelves` **on** (bookshelves of every wood) | `pack/config/quark-common.toml`, module strings |

### 1.4 Enchanting Overhauled, read in the jar (2nd fallback)

- **The altar** (`deactivated_enchanting_table`: obsidian, amethyst, red carpet) awakens when an **Enchanted Tome** is placed on it; it becomes a vanilla table that keeps the tome (`TomeStorageAccessor`), with lightning and particles (can be turned off: `activation_effects`). An ordinary book causes an explosion of strength 5 ("arcane retribution", can be turned off). The vanilla table recipe gives a "troubled altar" [jar: `DeactivatedEnchantingTableBlock`, `recipe/*.json`, `Config`].
- **The themes**: 11 `enchantment_theme/*.json` files. A theme only offers its enchantments if its blocks are around the table: blue ice → Frost Walker and Silk Touch; wet sponge → the sea; honeycomb → Bane of Arthropods; heavy core → Breach, Wind Burst... Everything is in data [jar].
- **The Tomes** are found in chests (modifier `tome_injection`) and in 5 "library ruins" where an **evoker** appears [jar: `worldgen/structure/*.json`].
- **Rebalancing**: 3 enchantments per item, maximum level III, Protection cut into physical and magical and reserved for the chestplate, linear XP curve, anvil without XP but **without books**. Almost everything is adjustable in `config/enchanting_overhauled.properties`, except the enchantment definitions it rewrites in `data/minecraft/enchantment/` (39 files) [jar].
- **Risks**: 25 mixins including `ItemStack`, `Player` (XP curve), `CombatRules`, `ServerLevel`; one mixin on `ItemCombinerMenu`, the class of the vanilla **smithing table** that Overgeared uses to move to diamond (Overgeared's anvil, for its part, is a `RecipeBookMenu` and is not touched); ARR licence; 4,100 downloads; no fr_fr (de, en, tr) [jar, API].

### 1.5 Overgeared does not touch enchanting

- Overgeared's mixins: cooling, stacking, player, potions, still. **None** on `AnvilMenu`, `EnchantmentMenu`, `ItemCombinerMenu`; no listening to `AnvilUpdateEvent` [jar `overgeared-neoforge-1.21.1-1.6.19`].
- Its tools and armours are added to the vanilla tags `minecraft:pickaxes`, `swords`, `head_armor`... (`data/minecraft/tags/item/`), which the `minecraft:enchantable/*` tags include: they are enchantable (deduced). Its tiers have an enchantability value (`ModToolTiers.getEnchantmentValue`).
- The vanilla anvil remains usable as long as it is not converted to the hammer: repair, renaming, merging two enchanted items. Immersive Enchanting does not touch it.

### 1.6 The enchantments of the pack's other mods

A cost file will be needed for each of them, otherwise Immersive Enchanting disables it [jar]:

| Mod | Enchantments | Proposal |
|---|---|---|
| Kaleidoscope Cookery 1.6.0 | `quick_knife`, `sweep` | Costs in cooking products (the knife stays a cooking matter) |
| Alex's Mobs Continued | `serpentfriend`, `straddle_jump`, `lavawax`, `board_return` | Costs in loot from the creature concerned, or `enabled: false` |
| Create | `potato_recovery`, `capacity` | Brass, precision mechanism |
| Supplementaries | `stasis` | Cost or disabled |

Source: `data/*/enchantment/*.json` in the pack's jars.

---

## 2. The concrete links to build

### 2.1 The gameplay loop (Immersive Enchanting)

1. **Find a piece of knowledge.** One brushes the sand of the NML alchemist ruins, opens the ancient jars, the wells, the heart of the dungeons, the sniffer monument; one defeats a monolith's guardian; one fills the bestiary. Each time, an **Ancient Book** with an unreadable title.
2. **Store it at home.** One builds a small library: a table, chiseled bookshelves within 2 blocks (3 in height), candles, lanterns. Each book placed can be seen on the shelf, and the title becomes readable in the table.
3. **Gather what it costs.** Each level asks for materials from the world: fauna loot (Wildex), seasonal plants, Overgeared steel, Create brass for high levels, plus one lapis per level and little XP.
4. **Enchant.** One places the forged item on the table, chooses the node in the tree, pays. An enchantment can be removed by holding the click, in order to try another.
5. **Pass it on.** One copies a book (book and quill + 10 levels) for a friend or a second base; a duplicate book that has been found can be transmuted into a piece of knowledge one already has.

The parallel with Overgeared is direct: **blueprint found → drawing table → forge**, **book found → bookshelf → table**.

### 2.2 What is kept from vanilla, what is cut

| Element | Decision | Tool |
|---|---|---|
| Vanilla enchanting table | **Kept** as a block (recipe unchanged: diamonds, obsidian, book). Its menu is replaced by Immersive Enchanting | none |
| Table randomness | Disappears | native |
| Enchanted books (loot, fishing, trades) | Replaced by ancient books / removed | config `enchantedBookLootMode = "REPLACE"`, `disableEnchantedBookTrades = true` (defaults) |
| Vanilla anvil | Kept: repair, renaming. It no longer has books to apply | none |
| Mending | **Cut**: it would cancel Overgeared's upkeep (grindstone that repairs while losing 5% durability, "broken" items) | `kubejs/data/immersiveenchanting/enchantment_costs/minecraft/mending.json` = `{"enabled": false, "levels": {}}` |
| IE libraries (structures) | Cut: the pack prefers the NML ruins and Sparse Structures; the music discs only come out with them (deduced) | `data/immersiveenchanting/tags/worldgen/biome/ancient_library_{desert,jungle,end}_biomes.json` = `{"replace": true, "values": []}` |
| Easy Magic | **Not added** | `docs/mods.md` |
| Quark Matrix, Ancient Tomes | Stay off | `quark-common.toml` unchanged |

### 2.3 Files to write

| What | Where | Tool |
|---|---|---|
| Add the mod | `pack/mods/immersive-enchanting.pw.toml`, `pack/mods/alfinolib.pw.toml` | `packwiz modrinth add immersive-enchanting` (pulls AlfinoLib) |
| Server config | `pack/defaultconfigs/immersiveenchanting-server.toml`: defaults kept; `bookshelfSearchY = 3`; decide `allowEnchantmentRemoval` | config |
| Fallow costs | `kubejs/data/immersiveenchanting/enchantment_costs/minecraft/*.json` (43) + the 9 enchantments of the other mods (§1.6). Start from `/immersiveenchanting generate_empty_datapack` | datapack |
| Books in the NML ruins | `kubejs/data/fallow/loot_modifiers/savoirs_*.json` of type `immersiveenchanting:ancient_book_loot_modifier`, condition `neoforge:loot_table_id` + `minecraft:random_chance`, and `kubejs/data/neoforge/loot_modifiers/global_loot_modifiers.json` (`replace: false`) | datapack (the mod's modifier, **no LootJS needed**) |
| Books from guardians and the bestiary | `kubejs/server_scripts/savoirs.js` | KubeJS (§2.5, §2.6) |
| Translation | `kubejs/assets/immersiveenchanting/lang/fr_fr.json` ("Livre ancien", "Savoir de", "Reproduire", "Transmuter"...) | KubeJS assets |
| Registry | `docs/mods.md`: Immersive Enchanting ➕ under "Building and workshops", AlfinoLib under libraries, Easy Magic ❌ | doc |

### 2.4 The NML ruins (example tables)

Tables read in the NML 1.6.1 jar: `nomansland:archaeology/alchemist_ruins`, `remains_dungeon`, `sniffer_monument`, `trinket`, `well`; `nomansland:chests/ancient_pot_cave`, `small_dungeon_core`, `sniffer_monument`, `water_well`, `alchemist_pot_*` [jar]. Proposal, to be tuned in game:

| Place | Knowledge (mode `random_enchantment_from_list`) | Why |
|---|---|---|
| Alchemist ruins (archaeology, with the brush) | Efficiency, Unbreaking, Protection, Silk Touch | The brushing gesture yields the knowledge |
| Sniffer monument | Fortune, Looting, Luck of the Sea | The pitcher plant already pays for Fortune III |
| Wells (`well`, `water_well`) | Respiration, Aqua Affinity, Depth Strider | |
| Dungeons (`remains_dungeon`, `small_dungeon_core`) | Sharpness, Smite, Knockback, Projectile Protection | |
| Cave jars | Feather Falling, Blast Protection | |

With Lootr, each player finds their own book in chests (deduced: Lootr generates loot per player, loot modifiers apply to each generation).

### 2.5 Ancient Remnants: one piece of knowledge per monument

The guardians script (chapter 12, report 6) returns the blessing and gives the codex page when the guardian dies. A chosen ancient book is added to it:

```js
// server_scripts/savoirs.js — sketch (component syntax to check in game)
const livre = (ench) => Item.of('immersiveenchanting:ancient_book',
  `[minecraft:stored_enchantments={levels:{"${ench}":1}}]`)
// in the guardian's victory callback (same script as the clearing):
// player.give(livre('minecraft:power'))
```

| Monument (guardian) | Proposed knowledge |
|---|---|
| Hunter monolith (Sculptor) | Power, Infinity, Piercing |
| Warrior monolith (Wroughtnaut) | Sharpness, Sharpening |
| Sentinel monolith (none) | Protection, Frost Walker |
| Cherry monument (none) | Feather Falling, Swift Sneak |
| Oak monument (Sacred Pontiff) | Fortune, Efficiency |
| Conifer monument (Frostmaw) | Silk Touch, Unbreaking |

For monuments without a guardian, the book can go through the same Elderheart polling as the clearing (report 4, §2.3), the first time a player receives the blessing.

### 2.6 Wildex: the bestiary opens up knowledge

Wildex has a KubeJS bridge (`WildexKubeJsScriptBridge.onDiscoveryChanged`, report 4 §1). At certain milestones (`discoveredCount` 10, 25, 50, or completion of a group), a book is given: Bane of Arthropods after the insects, Impaling after marine fauna. And the XP from discoveries (`newEntryXpAmount = 5`) pays the small `xp_levels` of the costs: **exploring feeds enchanting**.

### 2.7 Costs: fauna, seasons, forge

Principles for rewriting the 43 files (to be validated with the maintainer):
- **Fauna**: every other level asks for animal loot (scales, feathers, leather, fur from Naturalist or Alex's Mobs), not monster loot: the pack has very little of it.
- **Seasons**: Efficiency and Fortune levels ask for flowers or crops from Ecliptic Seasons, Farm & Charm or Kaleidoscope that only grow in one season (list to be drawn from the season tags).
- **Forge**: high levels of weapons and armour ask for Overgeared **steel plates** (`overgeared:steel_plate`), those of tools for Create **brass**.
- **Lapis**: one per level (default). **XP**: low or none.

### 2.8 What not to link

- **Moons** of Enhanced Celestials: nothing in Immersive Enchanting reads the moon. A link would require a "lunar" material made by KubeJS, and moon detection already goes through the future `fallow` mod (chapter 12.4). To keep for later.
- **Overgeared quality**: no hook. Limiting enchantments according to quality would require a small mod (mixin on IE's menu). Not necessary.
- **Ancient Remnants codex**: do not use "Research Scrap" as a cost: these are the codex pages.

### 2.9 If the Quark Matrix fallback is taken

- `pack/config/quark-common.toml`: `oddities = true` in `[categories]`, then an `[oddities]` section with `"Matrix Enchanting" = true` and `false` for Backpack, Crate, Magnets, Pipes, Tiny Potato, Totem of Holding.
- Fallow influences through `statesToInfluences`: NML blocks, clearing lantern, creature skulls...
- Rare knowledge: remove Silk Touch, Fortune, Looting, Mending from the tag `minecraft:in_enchanting_table` (datapack) so that they only come from **books** that have been found (LootJS in the NML ruins), applied at the vanilla anvil.
- Easy Magic stays out of the pack: Quark's conversion expects `minecraft:enchanting_table`, which Easy Magic replaces (deduced).

---

## 3. Conflicts and risks

| Risk | Severity | Detail and remedy |
|---|---|---|
| Easy Magic + Immersive Enchanting | High | Two mods that replace the table (Easy Magic by converting the block, IE by a mixin on the vanilla table): do not add Easy Magic [jar of both] |
| Young mod, single author | Medium | "My first mod" (Modrinth page), 37,000 downloads, but maintained in 2026 (6.0.1, bugs #74 and #76 fixed) [API]. Fallback ready: Quark Matrix |
| Enchantments of other mods disabled | Medium | Without a cost file, they disappear: write the 9 files (§1.6) |
| Mending | Medium | Enabled by default if the book is found (Nether star): cut it |
| Merging enchanted items at the anvil | Low | Two enchanted items found on creatures can still be combined. Rare in Fallow |
| 3D interface | Low | IE's screen is an interface of its own: to test with Spatial GUI and Shoulder Surfing |
| No EMI | Low | Costs are not in EMI; IE's interface shows them on hover. Quark's tooltip (`enchantingTooltips`) remains |
| No French | Low | `fr_fr.json` file to ship, as for Overgeared |
| Weight | Low | 7.4 MB in total, of which 6.8 MB of music played on demand; 4 mixins; scan on opening only (deduced) |
| Quark `Variant Bookshelves` | Info | These bookshelves are not chiseled: they do not store ancient books. Only chiseled bookshelves count (`ChiseledBookShelfBlockEntity`) [jar] |
| Enchanting Overhauled (if chosen) | Medium | Mixin on `ItemCombinerMenu` = smithing table, used by Overgeared for diamond; evokers in its ruins; combat rebalancing |

---

## 4. What to adapt, remove or add in the plan

| Where | Change | Reason |
|---|---|---|
| `docs/mods.md`, "Building and workshops" | **Easy Magic** ➕ → ❌ "Replaced by Immersive Enchanting, which also keeps the item on the table" | §0.3, §3 |
| `docs/mods.md` | **Immersive Enchanting** ➕ (CS): "Enchanting through ancient books that are found, stored and paid for in materials"; **AlfinoLib** under "To add with the mods above" | §1.2 |
| Chapter 11, comfort table (Easy Magic row) | Strike out Easy Magic, refer to this report | same |
| Chapter 10 §10.7 (links) | Add the chain "NML ruin / AR guardian / Wildex milestone → ancient book → bookshelf → table" | §2 |
| Chapter 10 §10.10 (scripts) | Add `savoirs.js` (guardians, Wildex) and the cost and loot datapack | §2.3 |
| Decision to write | `docs/decisions/0008-enchantement.md`: Immersive Enchanting, Quark Matrix fallback, Easy Magic out, Mending cut | traceability |
| Decision 0005 (Quark) | Note that Matrix Enchanting exists in 4.1 and stays off with `oddities` | §1.3 |

---

## 5. Questions that call for an in-game test

1. **Interface**: does IE's screen open properly with Spatial GUI (3D menus) and Shoulder Surfing? Readable in French?
2. **Book given by KubeJS**: is `Item.of('immersiveenchanting:ancient_book', '[minecraft:stored_enchantments={levels:{"minecraft:power":1}}]')` recognised as knowledge once stored? (Otherwise: `/immersiveenchanting give_random_ancient_book` and read the component with `/data get`.)
3. **NML archaeology**: does an IE loot modifier on `nomansland:archaeology/alchemist_ruins` give a book with the brush?
4. **Lootr**: does each player find their book in the same chest?
5. Does **`"enabled": false`** turn off Mending without a warning at launch?
6. **Overgeared**: do the quality ("Expert", "Master") and the "broken" state remain after enchanting and after removing an enchantment?
7. **Range**: at what real distance does a chiseled bookshelf count (2 / 3 / 2)? Is a 5 × 5 room enough?
8. **Multiplayer**: the library is shared (the books are in blocks); do copying and transmutation work on a server?
9. **Loot**: are the enchanted books that remain (creatures, fishing) all replaced?
10. **spark**: no measurable cost when opening the table with 10 full chiseled bookshelves?
11. **Quark Matrix fallback** (if tested): does the `statesToInfluences` config accept a partial block state, or does it need all its states?
