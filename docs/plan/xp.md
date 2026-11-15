# Removing XP: study and removal plan

Idea 5 from Wayfarer ([research](../archive/plan/recherche/2026-10-19/wayfarer.md), recommendation 5). The maintainer asked for a study before any removal, then chose to **remove XP**.

> **Status as of 2026-10-19: applied, not yet tested in game.** The removal plan is written in `pack/` and in the `compline` mod 0.2.0, which compiles. What was done, what deviates from the plan and what remains to be verified are in [Application](#application-2026-10-19). The study below comes from reading the pack's jars (`.cache/jars/`, `javap`), the Minecraft 1.21.1 code patched by NeoForge 21.1.251 (Gradle cache of the `compline` mod) and the Modrinth API.

## Summary

1. XP now has only **four uses** in Compline: the anvil, the copying and transmutation of Immersive Enchanting ancient books, and the address of an Envelope mailbox. Enchanting itself is already paid for in materials.
2. Three are handled by data or config. **The anvil is not**: Minecraft refuses to hand over the item if the player has fewer levels than the cost, and this cost is always at least 1. A small mixin is needed in the `compline` mod.
3. Cutting XP is done in a single place: cancel the spawning of orbs and every point gain (KubeJS and MoreJS). The bar is hidden in the same module of the `compline` mod. No Modrinth mod does it cleanly in French.
4. The player loses a gauge and an invisible currency. They also lose a few minor bonuses: the blue moon (XP ×2), the lapis backpack, Create's nuggets and the 5 XP per Wildex species. No mod read crashes at 0 XP: every new player starts at 0.
5. **Recommendation: remove XP entirely**, in a single version, as soon as the `xp` module of the `compline` mod is written and tested. The removal touches **16 essential files**, plus 3 optional settings and 5 doc pages.

## How the study was done

- The 124 jars of `pack/mods/` (plus their nested jars) were scanned for XP symbols: `ExperienceOrb`, `giveExperiencePoints`, `giveExperienceLevels`, `experienceLevel`, `xpReward`, `popExperience`, `getExpDrop`, `VanillaGuiLayers.EXPERIENCE_BAR`. 35 mods reference them. Each useful case was opened with `javap`.
- The jar data (recipes, loot tables, advancements, language files) was read for `experience`, `experience_nugget` and `experience_bottle`.
- The code of the anvil, the grindstone, the interface and the player comes from the Minecraft sources patched by NeoForge 21.1.251.
- The KubeJS, MoreJS and LootJS event names were read in their jars (KubeJS 2101.7.2-build.377, MoreJS 0.16.0, LootJS 3.7.0).

## Who produces XP

Once the orbs are cancelled and the gains blocked (step 2 of the plan), **all these sources go out at once**. The table only says what disappears and whether an extra setting is needed.

| Source | How the XP arrives | What removal changes | Replacement or setting | File or key |
|---|---|---|---|---|
| Mining (vanilla) | Orbs from coal, diamond, emerald, lapis, redstone, quartz ores, sculk blocks and spawners | Nothing left | None | — |
| Smelting (vanilla) | `experience` field of each furnace, smoker and blast furnace recipe, returned as orbs at the output | Nothing left | None | — |
| Creatures killed | `xpReward` and `LivingExperienceDropEvent`: Mowzie's Mobs (13 creatures), Alex's Mobs, Friends & Foes, Adorable Hamster Pets | Nothing left | None | — |
| Husbandry | Orbs on breeding: vanilla, Alex's Mobs (caiman, crocodile, platypus, turtle), Naturalist (egg layers), Hybrid Aquatic (`hapi`), Friends & Foes (crab) | Nothing left | None | — |
| Fishing, trading, grindstone | Orbs from fishing, villager trades and grindstone disenchanting | Nothing left | None | — |
| Advancements | `experience` reward: Icy's Better Horses (250, 100, 100), Exposure (`wildlife_archivist`, 150), vanilla "challenge" advancements | Nothing left; the advancement stays | None | — |
| Experience bottles | Found in loot: Twigs, Critters and Companions (ferret digging), Envelope (`valuables`), Hybrid Aquatic (crate), Supplementaries (safe), vanilla chests | The thrown bottle gives nothing: a dead item | Remove the bottle from all loot | LootJS, `pack/kubejs/server_scripts/sans_xp.js` |
| **Create** 6.0.10 | **Experience nugget**: 75% chance in **160 crushing recipes** (29 load in this pack) (ores and raw blocks); the nugget is used for orbs. **Experience block**: 9 nuggets. Drills and saws: orbs from broken blocks (`BlockHelper.popExperience`). **No liquid XP** in this version (that is Create: Enchantment Industry, absent from the pack) | The nugget becomes an item that is consumed for nothing | Remove the nugget from the 160 crushing outputs, and the two recipes of the block | `ServerEvents.recipes`, `sans_xp.js` |
| Overgeared | Alloy foundry, casting foundry and Nether foundry: `giveExperiencePoints` and orbs at the output | Nothing left | None | — |
| Farm & Charm | The stove (`StoveBlockEntity`) returns orbs; the "Rested" effect says "Increases experience points gained" | No more orbs. The effect has no code (`RestedEffect` is empty): only its description lies | New description | Key `effect.farm_and_charm.rested.description` in `pack/kubejs/assets/farm_and_charm/lang/fr_fr.json` |
| Farmer's Delight (replaced Kaleidoscope Cookery on 2026-11-15) | The cooking pot and the cutting board return orbs (`experience` of their recipes) | Nothing left (orbs cancelled) | None | — |
| Exposure | Darkroom: 2, 4 or 5 points per photo printed | Nothing left | Optional: set to 0 | `pack/defaultconfigs/exposure-server.toml`, `[lightroom]` `experience_black_and_white`, `experience_color`, `experience_chromatic` = 0 |
| Wildex | 5 points per new species (`newEntryRewardsXp = true`, `newEntryXpAmount = 5`) | Nothing left | Optional: cut; the reward will come from the zoo script ([07](../archive/plan/07-kubejs-et-datapack.md), line 18) | `pack/config/wildex/wildex-common.toml`, `[general]` `newEntryRewardsXp = false` |
| Envelope | Crafting service parcels (`PACKAGE_EXPERIENCE`), returned as orbs on opening | Nothing left | None | — |
| Enhanced Celestials | Blue moon: XP ×2; super blue moon: ×4 (`xp_amplifier`) | The XP bonus disappears. The moon keeps the luck (`minecraft:luck`) and the beacon range ×1.5 | None; see the anvil below | — |
| Traveler's Backpack | Lapis backpack: 15% chance to double the XP picked up | Dead ability | Optional: remove it from the list | `pack/defaultconfigs/travelersbackpack-server.toml`, `[server.backpackAbilities]` `allowedAbilities`, without `travelersbackpack:lapis` |
| Supplementaries | Tap on an XP jar, Red Merchant | Nothing left | None | — |
| Others | Ghost, Critters and Companions ferret, Alex's Mobs phantom pickaxe, Curios, Moonlight, `/xp` command | Nothing left | None | — |
| **Clumps** | This mod groups orbs together | It is no longer of any use | **Remove it from the pack** | Delete `pack/mods/clumps.pw.toml` |

Until 2026-11-13 the table also listed No Man's Land (turtle orbs on breeding; pots, large jars and the anchor). The mod left the pack ([0019](../decisions/0019-leaving-no-mans-land.md)). The experience sources of Regions Unexplored have not been examined.

## Who consumes XP

| Source | What breaks without XP | Replacement | File, key, value |
|---|---|---|---|
| **Anvil** (vanilla): rename, repair, combine | **Everything.** `AnvilMenu.mayPickup` requires `experienceLevel >= cost` and `cost > 0`. Renaming costs at least 1 level: at level 0, you can no longer name a tag for an animal, nor repair | **`xp` module of the `compline` mod**: a mixin on `mayPickup` ignores levels. The anvil keeps its cost in materials (ingots, sacrificed item) and its "Too expensive!" limit (40), which becomes **wear**: an item repaired too often no longer goes through the anvil, the grindstone or Overgeared's forge is needed. The label changes: "Usure : %1$s" ("Wear") and "Trop usé !" ("Too worn!"). Optional: refuse the merging of enchantments (`AnvilUpdateEvent` event), so that enchanting stays at the table | `mods-custom/compline/…/mixin/xp/AnvilMenuMixin.java`; keys `container.repair.cost` and `container.repair.expensive` in `pack/kubejs/assets/minecraft/lang/fr_fr.json` |
| Grindstone (vanilla) | Nothing: it gives XP, it does not take any | **Keep.** Overgeared uses it to restore durability (`grindingToggle`), without XP | — |
| Enchanting table | Nothing: Immersive Enchanting replaces the vanilla menu. The 43 cost files of the vanilla enchantments have **no** `xp_levels`: a material and lapis are paid | **Keep** | — |
| **Immersive Enchanting**: copy an ancient book | `replicate.json`: book and quill **+ 10 levels**. `Cost.test` requires `experienceLevel >= xp_levels`: nobody copies any more. Yet enchanting research counts on it to pass knowledge on to a friend | Book and quill alone, `xp_levels` at 0. The price remains the fuel (1 lapis) and having the book put away. Only one item can be put per cost slot | `pack/kubejs/data/immersiveenchanting/enchantment_costs/immersiveenchanting/replicate.json`: `{"levels": {"1": [{"item_cost": {"item": "minecraft:writable_book", "count": 1}}]}}` |
| **Immersive Enchanting**: transmute an ancient book | `transmute.json`: nothing (`minecraft:air`) **+ 10 levels**. Same blockage | A material instead, for example 4 amethyst shards (starting value, to be tuned in game) | `…/immersiveenchanting/transmute.json`: `{"levels": {"1": [{"item_cost": {"item": "minecraft:amethyst_shard", "count": 4}}]}}` |
| **Envelope**: name or rename a mailbox | 3 levels by default (`address_experience_levels_cost`, from 0 to 128). Without levels, no address, hence no mail | Set to 0. The game then tests "0 levels or more": it is always true, and the screen no longer displays a cost (it only draws it if the cost is positive) | `pack/defaultconfigs/envelope-server.toml`: `[mailbox]` `address_experience_levels_cost = 0` |
| Quark 4.1-485 | Nothing. The modules that touch XP are cut: Matrix Enchanting (`oddities = false`), Ancient Tomes, Color Runes, Ancient Wood (the fruit that gives XP), Game Nerfs (`experimental = false`). Hotbar Changer is only drawn above the bar | **Keep** | `pack/config/quark-common.toml`, nothing to change |
| Create 6.0.10 | Nothing consumes XP. No liquid XP or XP spout | See the producers (nuggets) | — |
| Supplementaries 3.9.9 | "Bottle XP" on an enchanting table (`[tweaks.bottle_xp]`): **off by default** (`enabled = false`) and absent from our config. The cannon already hides the bar when aiming | **Keep** off | `pack/config/supplementaries-common.toml`, do not add `bottle_xp` |
| Overgeared 1.6.19 | Nothing. Forge anvils, hammering, quenching, blueprints and expertise tiers do not use XP | **Keep** | — |
| Ancient Remnants 1.5.2 (blessings) | Nothing. Neither the classes nor the data of the jar mention XP | **Keep** | — |
| Traveler's Backpack | Nothing consumes. Only the lapis backpack gains XP (see the producers) | **Keep** | — |
| Wildex 3.0.0 | Nothing consumes. A creature's sheet displays "XP:" for what it yields | **Keep**; display detail | — |
| Exposure 1.9.19 | Nothing consumes | **Keep** | — |
| Alex's Mobs: transmutation table | Costs levels, but its recipe requires a **Nether star**, and the Nether is closed | **Keep**: unobtainable | — |
| Enhanced Celestials | The blue moon halves the anvil cost (divides by 4 for the super blue moon) | **Keep**: with the `xp` module, it **reduces the wear** of the anvil. It still has a role | — |
| Mending | It repairs with orbs | Already disabled ([0008](../decisions/0008-enchantement.md)) | — |
| Death | The player loses their levels and drops orbs | Nothing left to lose. The death screen still shows "Score: 0" | Optional: empty `deathScreen.score.value` in `pack/kubejs/assets/minecraft/lang/fr_fr.json` |
| Commands | `/xp`, `/enchant` (operators) | `/xp add … points` is blocked by the script; `/xp add … levels` still goes through (`LevelChange` event, not covered by MoreJS) | Nothing, unless levels are to be blocked too (see step 2) |
| Books and guides | The hamster guide mentions "anvils and enchanted books"; the "Goals" page of the Icy's Better Horses manual has an experience bottle as its icon; the lapis backpack tooltip mentions experience | Nothing breaks; these are texts | Only to be revised if they get in the way in game |
| Cold Sweat, GeckoLib, Spatial GUI, ModernFix, Steam 'n' Rails | They read or move the bar (action bar layout, rendering, orb colour) | Nothing breaks: at 0, these paths are those of a new player | — |

## How to remove XP cleanly

### Stop XP from arriving

There is **no gamerule** for XP: `doMobLoot` would also cut loot. Two locks are enough, both in `pack/kubejs/server_scripts/sans_xp.js`:

1. **Cancel the orbs.** `EntityEvents.spawned('minecraft:experience_orb', event => event.cancel())`. The KubeJS 7 event comes from `EntityJoinLevelEvent`; it is targeted by type and cancellable (`hasResult`). It only runs for orbs: its cost is negligible. It covers mining, smelting, creatures, husbandry, fishing, the grindstone, bottles, nuggets and mods.
2. **Block direct gains.** Some mods give XP without an orb: Wildex, Overgeared, advancements, `/xp`. MoreJS relays the NeoForge event `PlayerXpEvent.XpChange`, which `Player.giveExperiencePoints` sends before any gain: `MoreJS.playerXpChange(event => { if (event.amount > 0) event.cancel() })`. The event group is called `MoreJS` in the jar; it is a server event, cancellable. **Only** positive gains are blocked: a spend (negative) must remain possible.

For levels given all at once (`/xp add … levels`), only the native event `PlayerXpEvent$LevelChange` stops them: KubeJS's `NativeEvents.onEvent(...)` can listen to it. This is only useful against commands: to keep in reserve.

### Hide the bar

The level stays at 0, so the green number is never displayed (`renderExperienceLevel` only draws if the level is positive). The empty bar remains.

- **Chosen: the `xp` module of the `compline` mod.** It cancels the `VanillaGuiLayers.EXPERIENCE_BAR` and `EXPERIENCE_LEVEL` layers in `RenderGuiLayerEvent.Pre`. The horse jump gauge is another layer (`JUMP_METER`): it stays. The module already contains the anvil mixin, and the mod is installed on both sides.
- Fallback without Java: a script in `pack/kubejs/client_scripts/` with `NativeEvents.onEvent('net.neoforged.neoforge.client.event.RenderGuiLayerEvent$Pre', …)`. It works, but it goes through JavaScript at every layer and every frame.
- Mods searched on Modrinth for NeoForge 1.21.1 (API `/project/<slug>/version`, on 2026-10-19). **None is suitable**, and none has French:

| Mod | Version | Languages | Why not |
|---|---|---|---|
| HideHUD (`hidehud`) | 1.3fix (2026-06-07) | No language file; keys named in English in the code ("Toogle HideHUD") | It only hides the bar when the interface has been toggled (`ToggleHUDProcedure.isGuiHidden`) |
| HUD Manager (`hud-manager`) | 1.5.1 | English, Chinese | No setting for the XP bar alone ("Hotbar Group") |
| Hudless (`hudless`) | 2.0.6 | English, Japanese, Russian | Hides the interface according to conditions, with an animation; CC-BY-ND licence |
| Fixed Anvil Repair Cost (`fixed-anvil-repair-cost`) | 3.6 | — | For the anvil: "a value of 0 keeps the vanilla behaviour", so it still costs levels; ARR licence and depends on Collective |

Visible effect: hearts and hunger stay at their height, and a small gap remains in place of the bar. These gauges can be lowered (NeoForge's `Gui.leftHeight` and `rightHeight`): this is a finishing touch, to try in game.

### Existing levels

0.x worlds are disposable ([CONTRIBUTING](../../CONTRIBUTING.md)). For a test world that already has XP, the script resets everything to zero on login: `PlayerEvents.loggedIn(event => event.player.setXp(0))`. In KubeJS 7, `setXp` resets the total, the progress and the level to 0, then adds 0 points. Nothing needs to be converted: once the anvil, Immersive Enchanting and Envelope are settled, a level is of no further use.

## Removal plan

Each step lists the files touched. Steps 1 and 2 must come out **in the same version**: without the module, the anvil no longer works.

1. **`xp` module of the `compline` mod** (version 0.2.0).
   - `mods-custom/compline/src/main/java/io/github/salvagedisc/compline/mixin/xp/AnvilMenuMixin.java` (new): in `AnvilMenu.mayPickup`, ignore `player.experienceLevel` when the module is active. Keep the "cost > 0" test and the limit of 40. The mixin touches vanilla code. It does not interfere with that of Enhanced Celestials, which targets `createResult` (cost calculation).
   - `mods-custom/compline/src/main/java/io/github/salvagedisc/compline/xp/XpModule.java` (new): client side, cancel the two bar layers. Optional on the server side: refuse in `AnvilUpdateEvent` the merging of two enchanted items, so that enchanting goes through the table.
   - `mods-custom/compline/src/main/resources/compline.mixins.json`: add `xp.AnvilMenuMixin` to the `mixins` list (both sides). This mixin targets no mod: nothing to add in `ComplineMixinPlugin`.
   - `mods-custom/compline/src/main/java/io/github/salvagedisc/compline/ComplineModule.java`: entry `XP("xp", null, …)`, no required mod.
   - `mods-custom/compline/src/main/java/io/github/salvagedisc/compline/ComplineConfig.java`: `xp.enabled` (true), `xp.hideBar` (true), `xp.blockEnchantMerge` (to be decided), in `compline-common.toml`.
   - `mods-custom/compline/gradle.properties`: `mod_version=0.2.0`.
   - `pack/mods/compline-0.1.0.jar` replaced by `pack/mods/compline-0.2.0.jar`.
2. **Cut the sources**: `pack/kubejs/server_scripts/sans_xp.js` (new). Draft, to be verified on the test server:
   ```js
   // No more experience orbs, no more point gains.
   EntityEvents.spawned('minecraft:experience_orb', event => event.cancel())
   MoreJS.playerXpChange(event => { if (event.amount > 0) event.cancel() })
   PlayerEvents.loggedIn(event => event.player.setXp(0))

   // Create's experience nuggets no longer come out of crushing.
   ServerEvents.recipes(event => {
     const filtre = { type: 'create:crushing', output: 'create:experience_nugget' }
     const recettes = []
     event.findRecipes(filtre).forEach(r => recettes.push([r.getId(), JSON.parse(r.json.toString())]))
     event.remove(filtre)
     recettes.forEach(([id, json]) => {
       json.results = json.results.filter(o => o.id !== 'create:experience_nugget')
       event.custom(json).id(id)
     })
     event.remove({ id: 'create:crafting/materials/experience_block' })
     event.remove({ id: 'create:crafting/materials/experience_nugget_from_block' })
   })

   // No more experience bottles in chests and loot.
   LootJS.modifiers(event => {
     event.addTableModifier(/.*/).removeLoot('minecraft:experience_bottle')
   })
   ```
   The Create part copies each recipe without the nugget. Check in game that the 29 recipes keep their other outputs and their duration. If the copying fails, the fallback is to leave the nugget and hide it in EMI; it would remain a useless item.
3. **Replace the consumers.**
   - `pack/kubejs/data/immersiveenchanting/enchantment_costs/immersiveenchanting/replicate.json` (new): book and quill, no levels.
   - `pack/kubejs/data/immersiveenchanting/enchantment_costs/immersiveenchanting/transmute.json` (new): 4 amethyst shards, no levels.
   - `pack/defaultconfigs/envelope-server.toml`: `[mailbox]` `address_experience_levels_cost = 0`. If this file already exists, only the key is added.
4. **Texts** (kept in a separate language file and merged afterwards, never written directly into `pack/kubejs/assets/`):
   - `pack/kubejs/assets/minecraft/lang/fr_fr.json` and `en_us.json` (new): `container.repair.cost` → "Usure : %1$s" (Wear); `container.repair.expensive` → "Trop usé !" (Too worn!); optional: `deathScreen.score.value` empty.
   - `pack/kubejs/assets/farm_and_charm/lang/fr_fr.json` (already exists): `effect.farm_and_charm.rested.description` → for example "Un bon repos." (A good rest) (the effect has no code).
   - The config labels of the `xp` module, if NeoForge's config menu displays them.
5. **Remove Clumps**: delete `pack/mods/clumps.pw.toml`, then `packwiz refresh` (updates `pack/index.toml`) and `npm run check-deps`.
6. **Optional settings**, tidy but covered by step 2:
   - `pack/config/wildex/wildex-common.toml`: `[general]` `newEntryRewardsXp = false` (the file lives in the `wildex/` subfolder);
   - `pack/defaultconfigs/exposure-server.toml`: `[lightroom]` the three `experience_*` at 0;
   - `pack/defaultconfigs/travelersbackpack-server.toml`: `[server.backpackAbilities]` `allowedAbilities` without `travelersbackpack:lapis`.
7. **Documentation**: `docs/mods.md` (Clumps goes out, the `compline` mod gains the `xp` module), `docs/plan/mod-compline.md` (`xp` module and its tests), `CHANGELOG.md`, a decision `docs/decisions/0009-xp.md`, and the status of this page.
8. **In-game tests**, with two players:
   - mine coal, smelt iron, kill a creature, breed two cows: no orbs;
   - open a ruin chest: no experience bottle;
   - name a tag and repair a pickaxe at the anvil, at level 0; watch "Usure" (Wear) climb up to "Trop usé !" (Too worn!);
   - copy and transmute an ancient book;
   - name an Envelope mailbox;
   - crush iron ore with Create: no nugget, the other outputs are there;
   - discover a species in Wildex, take a photo, earn an Icy's Better Horses advancement: the bar does not move;
   - check that the bar has disappeared, on foot, on horseback (the jump gauge stays) and when aiming with the Supplementaries cannon.

Total: **16 essential files** (7 in `pack/` with `index.toml`, 3 language files, 6 in the `compline` mod), 3 optional settings, 5 doc pages.

## What the player gains and loses

**They gain:**
- a calmer screen: one fewer gauge, below gauges that already matter (temperature, thirst, seasons);
- a less "video game" world: you no longer kill and mine for a green bar; mining is for mining, smelting is for smelting;
- knowledge that carries weight: enchanting rests entirely on the books found and the materials of the world, as [decision 0008](../decisions/0008-enchantement.md) wants;
- a simpler anvil: you rename and repair with metal, without level calculations. Its "wear" gives items a lifespan and sends you back to Overgeared's forge.

**They lose:**
- the "free" anvil repair for those who had levels: it remains limited by wear;
- the blue moon's XP bonus (it keeps the luck and, thanks to the module, eases wear);
- the lapis backpack's ability;
- Create's nuggets and the experience block (the block was a luminous decoration);
- the 5 XP per Wildex species, which will be replaced by zoo rewards;
- the small pleasure of tinkling orbs. It is a familiar sound of the game: its absence will be noticed for the first few hours.

## Risks

- **The anvil mixin** is the only real risk. It touches vanilla code and must run on both sides: the client also refuses to take the item if it thinks the player does not have enough levels. Client and server must therefore have the same version of the `compline` mod. No mod in the pack patches `mayPickup`. Enhanced Celestials patches `createResult`, a different method. Easy Anvils is already rejected.
- **API names.** `EntityEvents.spawned`, `MoreJS.playerXpChange`, `PlayerEvents.loggedIn`, `findRecipes` and `LootJS.modifiers` are read in the jars, not yet tried. The format of the recipe copied by `event.custom` is the most fragile part.
- **Mods that crash at 0 XP**: none found. Every new player is at level 0, so these paths are the most played. The `getXpNeededForNextLevel` calculation is 7 at level 0, never 0: no division by zero.
- **Farms**: an XP farm is no longer of any use, which goes in the direction of Compline. Loot farms (wool, meat, crops) do not change. The only real automatic producer was Create's crushing.
- **A mod added later** that pays in levels (enchanting, repair, teleportation) would become unusable. The adding filter must ask the question: "does it pay in XP?".
- **Going back** is easy: delete `sans_xp.js` and set `xp.enabled = false`. The levels come back with the game, nothing is lost in the world.
- **Flaw already present, unrelated to XP**: the anvil accepts an Immersive Enchanting ancient book as a book (it tests the `stored_enchantments` component), but it reads the enchantments through `EnchantmentHelper.getEnchantmentsForCrafting`, which only reads this component on the vanilla enchanted book. The anvil should therefore apply nothing. This is deduced from the code: to be verified in game, with or without XP.

## Recommendation

**Remove XP entirely**: orbs, gains, bar, and the four consumers replaced.

- "Remove only the bar and the orbs" does not hold. Direct gains (advancements, Wildex, Overgeared) would fill an invisible gauge. The anvil and Envelope would or would not work, without the player knowing why.
- "Keep" costs nothing, but XP no longer has a role: enchanting is already paid for in materials. It remains a grind reward, contrary to the exploration wanted by the [vision](../vision.md).
- The real cost is low: a two-class module in the `compline` mod, a script, two data files and a config line. The only delicate point, the anvil, comes out improved.

Advised order: write and test the `xp` module, then release steps 1 to 5 in the same 0.x version.

### Maintainer's decisions

1. The anvil **can still merge** two enchanted items: `xp.blockEnchantMerge = false` (the setting exists, off). The "Too expensive!" limit (40) becomes wear.
2. Transmuting an ancient book costs **4 amethyst shards**.
3. The anvil cost is called "**Usure : %1$s**" (Wear), its limit "**Trop usé !**" (Too worn!).

## Application (2026-10-19)

### Files written

| Step | File | Contents |
|---|---|---|
| 1 | `mods-custom/compline/src/main/java/io/github/salvagedisc/compline/mixin/xp/AnvilMenuMixin.java` | `@ModifyExpressionValue` (MixinExtras) on the read of `player.experienceLevel` in `AnvilMenu.mayPickup`: equals `Integer.MAX_VALUE` when `xp.enabled`. The "cost > 0" test and the limit of 40 (in `createResult`) do not move |
| 1 | `…/compline/xp/XpModule.java`, `…/compline/xp/XpClient.java` | `AnvilUpdateEvent` cancelled if `xp.blockEnchantMerge` and the right-hand item carries enchantments; on the client, `RenderGuiLayerEvent.Pre` cancelled for `EXPERIENCE_BAR` and `EXPERIENCE_LEVEL` if `xp.hideBar` |
| 1 | `ComplineModule.java`, `ComplineConfig.java`, `Compline.java`, `compline.mixins.json`, `neoforge.mods.toml`, `gradle.properties` | Module `XP("xp", null, …)` with no required mod; `[xp]` `enabled = true`, `hideBar = true`, `blockEnchantMerge = false` in `compline-common.toml`; `xp.AnvilMenuMixin` in `mixins` (both sides); version 0.2.0 |
| 1 | `pack/mods/compline-0.2.0.jar` (replaces `compline-0.1.0.jar`) | Jar from `./gradlew build`. Its entry in `pack/index.toml` (name and sha256) is updated by hand |
| 2 | `pack/kubejs/server_scripts/sans_xp.js` | Orbs cancelled, positive gains blocked, XP reset to 0 on login, nuggets removed from Create crushing, experience block recipes removed, bottles removed from all loot tables |
| 3 | `pack/kubejs/data/immersiveenchanting/enchantment_costs/immersiveenchanting/replicate.json`, `transmute.json` | Book and quill without levels; 4 amethyst shards without levels (`xp_levels` is 0 by default in the `Cost` codec) |
| 3 | `pack/defaultconfigs/envelope-server.toml` | `[mailbox]` `address_experience_levels_cost = 0` (key read in `Config$Server` of Envelope 0.7.5, `SERVER` config) |
| 4 | separate language file (merged afterwards) | `container.repair.cost`, `container.repair.expensive`, `effect.farm_and_charm.rested.description`, in French and English. Language keys are global: the first two go into `pack/kubejs/assets/minecraft/lang/`, the third into `pack/kubejs/assets/farm_and_charm/lang/` |
| 6 | `pack/config/wildex/wildex-common.toml` (new, partial) | `[general]` `newEntryRewardsXp = false` |
| 6 | `pack/defaultconfigs/exposure-server.toml` (new, partial) | `[lightroom]` the three `experience_*` at 0 |
| 6 | `pack/defaultconfigs/travelersbackpack-server.toml` (new, partial) | `[server.backpackAbilities]` `allowedAbilities`: the default list of 10.1.39, without `travelersbackpack:lapis` |

The partial config files follow the pack's practice (`supplementaries-common.toml`): NeoForge adds the missing keys on first launch.

### Deviations from the plan

- **Create nuggets**: the script goes through all `create:crushing` recipes (`forEachRecipe`) and reads their JSON, instead of the `output` filter, whose support by the KubeJS Create schema is uncertain. It first removes each recipe, then recreates it under the same identifier without the nugget and without `neoforge:conditions` (KubeJS 7 places the added recipes after the originals: the identifier is not lost). A recipe that had only the nugget would simply be removed; Create 6.0.10 has none (160 recipes read, all with another output).
- **Bottles**: removed from the tables themselves (`LootJS.lootTables`, `forEachTable`, `removeItem`), as `tannerie_butin.js` does, instead of a modifier applied to each roll.
- **Anvil label**: it goes through the vanilla keys, so it stays "Usure" (Wear) even with `xp.enabled = false`. The `compline` mod has no language file (no registered config screen): nothing to translate in the mod.
- **Death screen**: `deathScreen.score.value` is not emptied. A value without `%s` would make `npm run check-fr` fail (variable check); "Score : 0" stays.
- **Done separately**: Clumps removed and `packwiz refresh` run (step 5). **Remaining**: `docs/mods.md`, `CHANGELOG.md` and the decision `docs/decisions/0009-xp.md` (step 7).

### Remaining to test in game

On the test server, then with two players (list of step 8):

1. **Startup**: log "Module xp chargé (aucun mod requis)", no `compline` mixin warning (`AnvilMenuMixin` must find its target); KubeJS log with no error in `sans_xp.js`, with "[sans_xp] 29 recettes de broyage recopiées sans pépite d'expérience" (see [Test server](#test-server-2026-10-19)).
2. **Anvil at level 0**: name a tag, repair a pickaxe with ingots, merge two enchanted pickaxes. The cost is displayed in green ("Usure : n") and the item can be taken; at 40, "Trop usé !". With `xp.enabled = false`, the anvil asks for levels again. With `xp.blockEnchantMerge = true`, an enchanted book on the right no longer gives a result.
3. **Sources cut**: mine coal, smelt iron, kill a creature, breed two cows, fish: no orbs. `/xp add @s 10 points`: nothing changes. Wildex, Exposure darkroom, Icy's Better Horses advancements: the bar does not move.
4. **Create**: crush raw iron ore; no nugget, the other outputs and the duration are there; EMI no longer shows the nugget in crushing, nor a recipe for the experience block. Most fragile point: the copying by `event.custom`.
5. **Loot**: ruin chests, Hybrid Aquatic crate, Supplementaries safe, Envelope `valuables` parcel, ferret digging: no experience bottle. A bottle coming from elsewhere (trader) gives nothing.
6. **Immersive Enchanting**: copy an ancient book (book and quill, no levels); transmute it with 4 amethyst shards in the cost slot.
7. **Envelope**: name a mailbox in a new world (`serverconfig/envelope-server.toml` copied from `defaultconfigs`), with no cost displayed.
8. **Interface**: no more bar or number, on foot, on horseback (the jump gauge stays) and when aiming with the Supplementaries cannon. Look at the small gap under the hearts and hunger (possible finishing touch: lower `Gui.leftHeight` and `rightHeight`).
9. **Test world with XP**: on login, levels reset to 0.
10. **Lapis backpack**: no more ability (`allowedAbilities` read from the new world).

### Test server (2026-10-19)

Dedicated server without a player (NeoForge 21.1.251, new world, 98 jars, Clumps removed), launched with `boot.sh --fresh --keep` then stopped. What can be verified without a player:

| Check | Result |
|---|---|
| Startup | "Done" in 17 s. "Module xp chargé (aucun mod requis)", then "xp : enclume sans niveaux…". No `compline` mixin warning. `AnvilMenu` is loaded at startup (`MenuType.ANVIL`): with `required` and `defaultRequire: 1`, a failed `AnvilMenuMixin` would have crashed the server |
| KubeJS scripts | `startup.log`: 2/2 scripts, 0 errors. `server.log`: 6/6 scripts, 0 errors, 0 warnings; "Reloaded with no KubeJS errors" after `/reload`. The 81 warnings in `server.log` are recipes from other mods that it cannot read ("Falling back to vanilla"), unrelated to `sans_xp.js` |
| Create crushing | "29 recettes de broyage recopiées", "0 failed recipes". **29 and not 160**: the other 131 nugget recipes in Create 6.0.10 carry a `neoforge:tag_empty` condition (ores from other mods) and do not load in this pack. The export (`/kubejs export debug`) shows `raw_iron` with the single output `create:crushed_raw_iron` and the same duration (400). For `deepslate_gold_ore` and `nether_quartz_ore`, the outputs and the duration are the original ones, minus the nugget. No exported recipe contains `experience_nugget`. The two experience block recipes are removed |
| Orbs | `summon minecraft:experience_orb … {Value:10}` answers "Summoned", but no orb exists afterwards (`execute if entity @e[type=experience_orb]`: fails). Same for an experience bottle thrown by `summon` |
| Bottles in loot | 40 rolls each of `minecraft:chests/shipwreck_treasure`, `crittersandcompanions:gameplay/digging/common` and `envelope:packages/lost_mail/valuables` (`loot spawn`): 687 items including 92 emeralds, **no** experience bottle |
| Configs | `envelope-server.toml`: `address_experience_levels_cost = 0`; `exposure-server.toml`: the three `experience_*` at 0; `travelersbackpack-server.toml`: `allowedAbilities` without `lapis`; `wildex/wildex-common.toml`: `newEntryRewardsXp = false`. NeoForge completed the partial files ("Incorrect key" warnings, expected) without touching the written values. On this server, NeoForge 21.1 stores server configs in `config/`, not in `world/serverconfig/` |
| `/xp add @a 10 points` | "No player was found": to be redone with a player |

**Remaining to test with a player**: the anvil at level 0 (rename, repair, merge, "Usure" (Wear), "Trop usé !" (Too worn!) at 40); `/xp add … points` and direct gains (advancements, Wildex, darkroom); the reset to 0 on login; a creature killed by a player; the copying and transmutation of Immersive Enchanting (the files follow the jar's codec, but the mod writes nothing at load); the address of an Envelope mailbox; the bar hidden on the client.
