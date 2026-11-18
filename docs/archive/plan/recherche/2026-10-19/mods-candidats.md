# Candidate mods of 2026-10-19

Mission: The maintainer proposed eight mods. For each one: should it be integrated, how, what to keep and what to cut to avoid duplicates and overload. Specific questions: can Identity Mask be adapted to NeoForge, is it compatible with Fresh Animations, and can only some features of Kingdoms: Villager Retaliation be enabled?

Method: Modrinth API (NeoForge 1.21.1), CurseForge page, downloaded files read (`unzip`, `javap`, JSON, NBT) without running anything, mod wikis. **Nothing was tested in game.** Each claim carries its source: [Modrinth], [CF], [jar], [wiki], [web], [inferred].

## In brief

| Mod | Available for NeoForge 1.21.1 | Verdict | Main reason |
|---|---|---|---|
| Identity Mask | No: CurseForge only, Forge 1.20.1, ARR | Do not integrate, port or rewrite | Mechanic turned against vanilla monsters and living villages, which the pack removes |
| Kingdoms: Villager Retaliation | Yes, beta (`1.0.0-beta.13-hotfix.1`), + `owo-lib` and `toucan` | Test in reduced mode, with an abandonment criterion | Gives value to each survivor and blocks breeding through the config; but hiring, party, quests and economy cannot be switched off through the config |
| Dreamshift | No: Forge and Fabric 1.20.1, requires Immersive Portals | Do not integrate | Horror mod; No Man's Land's dream already plays this role |
| Animalia Exotica | Yes (1.0.3) | Do not integrate | 4 species out of 6 are duplicates, vanilla biomes hard-coded |
| Fish'n'Ships | Yes (`mc1.21.1-1.1.0-neoforge`), no new dependency | To test | Real water transport mechanic before Create; ship and trawl to be decided |
| Sunken Spires | Yes (`1.0+mod`) | Do not integrate as is | All 11 structures spawn guardians and drowned; a filtered variant is possible |
| Trek | Yes (beta B0.6.2) | Do not integrate (0004 confirmed) | 113 structures, pillagers, no NML biome, replaces villages and the stronghold |
| Creature Feature | Yes (1.2.3.3), + Blueprint | Do not integrate | 23 monsters, unsettling tone, no option to turn off spawns |

**Identity Mask on NeoForge.** Not possible cleanly. The licence ("All rights reserved") forbids forking or redistributing without the author's written consent, and no source code is published. The mod has 216 classes and 40 mixins written for Forge 1.20.1: moving to 1.21.1 would be a rewrite. Sinytra Connector only loads Fabric mods. The idea could be rewritten in the `fallow` mod, but it only serves to fool vanilla monsters and living villagers, which are absent from Fallow.

**Identity Mask and Fresh Animations.** Nothing to test in 1.21.1. From the code, the mask is drawn as a layer on the head of the player model and should follow the FA Player Extension animation; but its mixins touch the same layers (armour, head, cape, held item, animations) as EMF and FA Player Extension, and 3D Skin Layers would probably not follow the borrowed skin [inferred].

**Villager Retaliation, feature by feature.** A single file, `config/villagerretaliation.json5`, shared by client and server. Can be switched off one by one: retaliation, reputation, attack on sight, loot, names and HUD, notifications, each dialogue effect, gifts, moods, study, duels, player raids, villager combat, "downed" state, enriched trading, and breeding (`social.enableVanillaVillagerBreeding=false`). Cannot be switched off through the config: hiring (13 professions and a builder), the party, quests (only by datapack) and the economy (only by removing recipes). Details and values to ship in 2.1.

## What to do next

- Fish'n'Ships: branch `test/fish-n-ships`; keep canoe, sailboat, rafts and dinghy; decide on the ship (duplicates Aeronautics) and the trawl (passive fishing with enchanted books, which short-circuits [0008](../../../../decisions/0008-enchantement.md)).
- Villager Retaliation: branch `test/villageois` with the reduced config of 2.1; abandon if hiring and the HUD remain present. Blocking breeding and filtering trades remain doable in KubeJS ([0004](../../../../decisions/0004-structures.md)).
- Sunken Spires: only if silent underwater ruins are wanted, with the filtered variant of 3.3.
- The other five: one line in "Removed or ruled out" of the [register](../../../../mods.md) once the maintainer has decided.

## 1. Identity Mask and Creature Feature
### 1.1 Identity Mask

#### Identity

- Author: RetroParadox (CurseForge, id 1666070), created 2026-08-28, a single file: `id_mask-1.0.0.jar`, beta, about 17,500 downloads. [CF]
- Loader and version: **Forge 1.20.1 only** (`loaderVersion="[47,)"`, `minecraft [1.20.1,1.21)`). No Fabric version, no NeoForge, no 1.21.1. [jar] [CF]
- Licence: **All rights reserved** (`license="All rights reserved"` in `mods.toml`). [jar]
- Source code: no repository found. The CurseForge page gives no source link; the web search finds nothing; the GitHub search could not be done (API blocked during this research pass). [CF] [web]
- Modrinth: **absent**. The CurseForge page points to `modrinth.com/mod/identity-mask`, but the API answers 404 (not published, or not yet approved). The search "identity mask" does not find it. The author's only Modrinth project is `alien-evolution-first-person-model` (Forge 1.20.1, ARR). [Modrinth]

#### What the mod does

- A mask (item `id_mask`, helmet or Curios head slot) projects a **hologram**: the player looks like another player (skin downloaded from the Mojang API) or like a creature. Health, hitbox, reach and speed do not change. [CF] [jar]
- Projectable creatures: **zombie, villager, enderman** only ("alien" locked, waiting for the Alien Evolution mod). [jar] (strings of `Projections`, `MobTransformRenderer`)
- Effects: as a zombie, monsters no longer target you; as a villager, trade discounts; "act mode" (footsteps, sounds and movements of the creature); suspicion bar above "smart" mobs; visible fake armour. [CF]
- Smart mobs by default: creeper, witch, enderman, pillager, vindicator, evoker, illusioner, piglin, piglin brute; list extensible in the config (`extraSmartMobs`). Mobs from other mods are fooled "without configuration". [jar] [CF]
- Obtaining it: a "masked chicken" spawns in villages around the player and drops a broken mask, repaired at the anvil; a "face mask" can be crafted. [jar] (`VillageMaskedChickenSpawner`, `AnvilRepairHandler`, `DeathDropHandler`)
- Tone: "advanced holographic technology", aliens, Discord panel in the menu. [CF] [jar]

#### (a) Port or adaptation to NeoForge 1.21.1

- **Licence**: ARR forbids forking, or decompiling in order to republish or redistribute, without the author's written consent. A public port is only possible with their permission. [jar] [inferred]
- **Size**: 216 classes, about 1 MB of bytecode, **40 mixins** (20 common ones on aiming AI: skeleton, blaze, ghast, wither, witch, crossbow, `LineOfSight`, `LookControl`; 20 client ones on rendering: armour, head, cape and held-item layers, `AnimationUtils`, player skin, `GameRenderer`), shaders and screen effects. Embeds MixinExtras 0.3.5. Optional dependency: Curios. [jar]
- **Gap 1.20.1 → 1.21.1**: ItemStack NBT replaced by data components, networking rewritten (payloads), `getSkinTextureLocation` replaced by `getSkin()`, packages `net.minecraftforge` → `net.neoforged`, mixin targets to be readjusted. For a mod so dependent on mixins, this is a rewrite rather than a port. [jar] [inferred]
- **Sinytra Connector**: only loads **Fabric** mods. Identity Mask is a Forge 1.20.1 mod: Connector can do nothing with it. The pack also forbids any Fabric mod outside the Surveyor family (`docs/plan/06-technique-et-serveur.md`). [jar] [inferred]
- **Reimplementation in `mods-custom/fallow/`**: the idea is not protected, only the code is; a clean version can be written without reusing the code. But the mod's appeal rests on vanilla monsters (fooling them, making them suspicious) and on living villages, two things that Fallow removes (no vanilla monsters, empty ruined villages). What would remain is a cosmetic projection of a player or villager and a stealth effect against the rare modded hostiles, which would weaken precisely the territorial creatures the vision calls for. The cost (aiming AI of every mob, rendering, networking) is out of proportion. [inferred]
- For Fallow, the question of the port is therefore theoretical: even ported, the mod fails the filter (no integration with the pack's systems, mechanic turned against absent mobs, "hologram/alien" tone contrary to the art direction). [inferred]

#### (b) Compatibility with Fresh Animations (EMF/ETF)

No test possible: the mod does not exist for 1.21.1. Points noted in the code:
- The mask is rendered by its own layer (`MaskHeadLayer`, item model on the head of the `PlayerModel`) or via Curios. EMF animates the parts of the player model; a layer that follows `head` should follow the animated head of FA Player Extension, with no guarantee. [jar] [inferred]
- The projection as a player replaces the texture returned by `AbstractClientPlayer`: FA Player Extension would keep its model with the other skin. 3D Skin Layers reads the skin texture to build its relief layers; risk of misaligned layers or of the old skin. [jar] [inferred]
- The projection as a creature cancels `RenderPlayerEvent.Pre` and draws a "proxy" mob; if that proxy goes through vanilla rendering, EMF would apply FA to it. [jar] [inferred]
- Mixins on `HumanoidArmorLayer`, `CustomHeadLayer`, `CapeLayer`, `ItemInHandLayer`, `AnimationUtils`: the same areas touched by EMF and FA Player Extension; conflicts plausible. [jar] [inferred]

#### Recommendation: do not integrate

- Absent from Modrinth, Forge 1.20.1 only, ARR, no source: blocking according to `CONTRIBUTING.md`.
- Do not port or reimplement it: the mechanic targets monsters and villages that the pack removes.
- Nothing to configure. If the wish is only cosmetic (taking on another player's appearance), two Modrinth NeoForge 1.21.1 leads exist, not studied here: `disguiseheads` (tr7zw, author of 3D Skin Layers, client only, tr7zw proprietary licence) and `player-disguise` (MPL-2.0, 893 downloads). [Modrinth]
- To add to "Removed or ruled out" in `docs/mods.md` if a record is wanted: "Identity Mask: CurseForge only, Forge 1.20.1, ARR; mechanic turned against vanilla monsters".

### 1.2 Creature Feature

#### Identity

- Slug `creature-feature`, author AtiredDev (with "Team Galena"), licence **MIT**, source announced as `github.com/TerriblyBadCoder/creaturefeature` (not verified: GitHub unreachable during this research pass). [Modrinth] [jar]
- **NeoForge 1.21.1 only**, side CS (client and server required). Latest version **1.2.3.3** (2026-08-27), 6 versions in August, about 46,000 downloads. [Modrinth]
- Dependencies: **Blueprint** (`blueprint`, required, absent from the pack); **Sable** optional (already in the pack). Optional compat with Oreganized and Caverns & Chasms (loot). NeoForge ≥ 21.1.228: compatible with 21.1.251. [Modrinth] [jar]

#### Content

- The title and `mods.toml` say it: "**23 Hostile creatures**". All living entities are registered in the `MONSTER` category. [Modrinth] [jar]
- Creatures and spawns (biome modifiers in the jar): [jar] [Modrinth]
  - Surface: Minds (zombie with a giant brain, savannas), Blossom (flower zombie, jungles), Fend (flowers, 3 variants including an archer), Coat of Arms (swamps, burst fire), Mockingbird (mountains, "strangely humanoid"), Vertigo (dark forest, taigas), Runaway (beaches), Stained Glass (deserts), Toadstool (plains, **enabled only if Sable is present**, hence active in Fallow).
  - Caves and the whole Overworld (`#c:is_overworld`): Sinister (mass and mirror), Detritus (zombie that creates degraded copies of itself), Canary, Beauty (lush caves), Pathogen (heals the undead).
  - Nether and End: Machination, Fiend, Friend, Eeper, Blitz, MinedFlayer, NoThing, Dreamweaver, Saint Solis. The End is removed in Fallow.
- Tone: strange and often unsettling (NoThing, a blurry silhouette that screams then charges; Detritus "a normal person"; Friend "your friend, that is"), post-processing shaders, mixins on the camera and interface (`CameraMixin`, `CFPostChainMixin`, `IamActivelyHavingaMigraineMixin`), effects that force third person or spin the camera. This is light horror or "weird", close to what the vision excludes. [Modrinth] [jar]
- Also adds about 47 recipes (shell blocks, tiles, feathers, flintlock, heavy axe, armour trim materials), 2 music discs, advancements. [jar]

#### Configurability

- The config does **not** allow disabling a creature or its spawns: it only offers remodels (Friend, Mockingbird), a post-processing fix, and settings for Dreamweaver, Pathogen, Saint Solis and Fiend. [jar] (`Config`)
- Spawns can be cut by datapack (override the 27 files `data/creaturefeature/neoforge/biome_modifier/*.json` with `neoforge:none` in `pack/kubejs/data/creaturefeature/`) or by In Control!. Entities, items and recipes remain registered. [jar] [inferred]

#### Overlaps and rendering

- No species duplicate with Naturalist, Alex's Mobs Continued, Critters and Companions or Friends & Foes: these are invented monsters, not animals. But it **adds 23 hostiles**, the opposite of "few hostiles, rare, territorial"; several are zombie variants, whereas vanilla zombies are removed. [Modrinth] [inferred]
- No GeckoLib: Java models (`ModelLayerLocation`, `ModelPart`), no `geo` file or Bedrock animation. [jar]
- Fresh Animations only covers vanilla mobs: nothing for these creatures. EMF could apply purpose-written OptiFine-CEM models to them, but none exist. Of no interest to FA. [jar] [inferred]

#### Recommendation: do not integrate

- Fails the filter: content (23 monsters) rather than mechanic; numerous hostiles spread across the whole Overworld instead of rare and territorial ones; a tone that borders on horror; a third of the content in the removed End; one more dependency (Blueprint); camera and post-processing mixins that may clash with ATMOSPHERICS, Shoulder Surfing and Camera Overhaul [inferred].
- If one or two creatures were wanted anyway (for example Vertigo in the dark forest or Mockingbird in the mountains as a rare, territorial hostile): `packwiz mr add creature-feature` (side both) with `blueprint`, then override all biome modifiers except the chosen ones, lower their weight (Vertigo 18, Mockingbird 22 today) and confine them with In Control!. The unneeded recipes and items would also have to be hidden in EMI. At that price, better not to add it.
- To add to "Removed or ruled out": "Creature Feature: 23 monsters, unsettling tone, no option to turn off spawns".

## 2. Kingdoms: Villager Retaliation and Dreamshift
### 2.1 Kingdoms: Villager Retaliation! (`villager-retaliation`)

#### Versions and dependencies
- Only NeoForge 1.21.1 is published. Latest version: `1.0.0-beta.13-hotfix.1` (2026-08-18), 17 versions, **all beta**. Licence ARR, but the author allows use in a modpack. Client **and** server side [Modrinth].
- Beta 13 changes the AI and the save format: the author asks to back up the world before updating. They will not add compatibility patches before 1.0.0 [Modrinth]. This is a risk for a permanent world.
- Required: NeoForge ≥ 21.1.238 (the pack is on 21.1.251, hence compatible), **oωo-lib** (`owo-lib`, ≥ 0.12.15.1-beta.6; current version `0.12.15.6-beta.1+1.21`, in beta), **toucanLib** (`toucan`, ≥ 0.5.0) and Architectury (already in the pack) [jar `neoforge.mods.toml`, Modrinth, repo]. owo-lib and toucanLib would be two new libraries.
- Optional: EMI, JEI, REI, Curios, Accessories, EMF/ETF, Second Wind [jar].

#### Features
The mod extends the vanilla villager without replacing the entity. It places 39 server mixins and 20 client mixins on the villager's brain, trading, panic, the bell, breeding, navigation and the merchant screen [jar, Modrinth].
1. **Retaliation**: villagers and golems attack the offending player. The "Despised" are attacked on sight [Modrinth, jar].
2. **Reputation** per villager and per player, in 9 tiers, with gossip, moods and village memory [Modrinth].
3. **Interaction screen**: dialogues, zooming camera, cinema bars, "blip" voices [jar].
4. **Gifts** and keepsakes [jar].
5. **Hiring**: 13 professions (combat, hunting, mining, lumberjack, farming, fishing, alchemy, craftsman, livestock, cooking, smelting, courier, nitwit) and a builder that follows blueprints. All managed with a clipboard, with wages [Modrinth].
6. **Economy**: each villager's personal purse, Payment Box, Sell Box with daily prices [Modrinth].
7. **Party** of up to 4 villagers: follow, stay, guard, pick up. They can ride horses [Modrinth].
8. **Player-led raids** (ominous banner worn as a helmet and goat horn) and **duels** [Modrinth].
9. **Quests**: 85 announced, 278 files. Journal on J, tracking on K [Modrinth, jar].
10. **Study** of a villager, with a reward [jar].
11. **"Downed" state**: the villager falls instead of dying, then gets back up [jar].
12. **Villager combat**: they hit hostiles, pick up weapons, and clerics throw potions [jar].
13. **Loot** on the death of villagers and wandering traders, and death messages [jar].

#### Can only some features be enabled? Only partly.
**File**: a single one, `config/villagerretaliation.json5`, in oωo format (`@Config(name="villagerretaliation")`) [jar]. The file name comes from the oωo convention [inferred]. This file is neither client nor server: it serves both, and the server pushes its values to connected players (`syncServerConfigToConnectedPlayers`). On first launch, the mod migrates the old `villagerretaliation-common.toml` and `-client.toml` [jar]. The sections are nested (`"general": { ... }`). Defaults read in the constructors [jar]:

| Feature | Switch (default) | Independent? |
|---|---|---|
| Retaliation | `general.enableVillagerRetaliation` (true); details in `retaliation.*` | Yes |
| Reputation | `general.enableVillagerReputation` (true); `reputation.reputationDecayEnabled` (true); `reputation.enableReputationTradePricing` (true) | Yes |
| Link with vanilla gossip | `general.enableVanillaGossipIntegration` (true) | Yes |
| Attack on sight of the "Despised" | `general.enableDespisedKillOnSight` (true) | Yes |
| Loot and death messages | `general.enableVillagerDrops`, `enableWanderingTraderDrops`, `enableVillagerDeathMessages` (true); `wanderer.*` | Yes |
| Names above heads, icon in the trade screen | `general.showVillagerNameTags` (true), `showTradeGuiReputationIcon` (true), `reputationChangeDisplayMode` (HUD) | Yes |
| Floating texts and murmurs | `notifications.enableWorldTextNotifications`, `enableAmbientMurmurs`, `enableSleepIndicators`, `enableDamageAlerts`, `enableCombatAlerts`, `enableTradeAndGiftWorldText` (all true) | Yes |
| Interaction screen | `dialogue.enableInteractionScreen` (true). Effects can be switched off one by one: `enableDialogueCameraFocus`, `enableNormalDialogueCameraFocus`, `enableDialogueCinematicBars`, `enableForcedDialogue`, `enableContainerForcedDialogue`, `enableRetaliationForcedDialogue`, `enableDialogueBlipAudio`, `freezeVillagerDuringDialogue` (true) | Yes |
| Gifts | `gifts.enableVillagerGifts`, `enableHighReputationGifts`, `enableGiftKeepsakes` (true) | Yes |
| Social, moods | `social.enableVillagerSocialGraph`, `enableVillagerMoods`, `enableSocialAttribute*` (true) | Yes |
| **Breeding** | `social.enableVanillaVillagerBreeding` (true). When false, all breeding is blocked (`BREEDING_DISABLED`, mixin `VillagerMakeLoveMixin`) [jar] | Yes |
| Study | `study.enabled` (true) | Yes |
| Duels | `duels.enabled` (true) | Yes |
| Player raids | `playerRaids.enabled` (true) | Yes |
| "Downed" state | `combat.enableVillagerDownedState` (true); `allVillagersUseDownedState` (false); `partyVillagersUseDownedState` (true) | Yes |
| Villager combat | `combat.villagersTargetHostileMobs`, `villagersRetaliateAgainstHostileMobs`, `villagersStandGroundAgainstHostileMobs`, `*FightBack` per profession, `clericsUsePotions`, `hostileTierHarassThrowEnabled`, `villagersPickUpGroundWeapons` (all true) | Yes |
| Enriched trading | `trade.enableSkillTradeOverhaul` (**false**): high-tier equipment, enchantments up to level 3; `trade.enableSpecialOrders` (true); `trade.disableVillagerWalletLimit` (false) | Yes |
| Hunger, famine | `balance.hungerEffectAffectsVillagers` (true), `enableVillagerStarvationDamage` (false) | Yes |
| **Hiring (13 professions, builder)** | **No switch**; only `balance.hiredContract*` (costs) and `enableHiredWorkSkillGrowth` | **No** |
| **Party** | **No switch** | **No** |
| **Quests** | Only the indicators can be switched off: `quest.showQuestIndicators`, `enableQuestItemShaderHighlights`. To remove the offers, a datapack removes the `villagerretaliation:quest_board` pool (`remove: true` with `id`, [wiki]) or replaces its file with `"enabled": false` (the field exists [jar], effect to be tested) | By datapack only |
| **Economy (Payment Box, Sell Box)** | No switch. The recipes are removed (`data/villagerretaliation/recipe/payment_box.json`, `sell_box.json`) in KubeJS, and prices are disabled file by file with `enabled: false` [wiki] | By KubeJS and datapack |

- No gamerule is added [jar].
- Datapack tags [jar]: `watched_containers`, `village_terrain`, `currency` (emerald), `assignable_mounts`, `protected_civilians`, `village_allegiance_holders` (villager, zombie villager, iron golem), `allegiance_exempt`, `#villagerretaliation:village_footprint` (= `#minecraft:village`). Dialogues, names, gifts, prices and quests all come from JSON and are replaced by datapack [jar, wiki].
- Turning off `dialogue.enableInteractionScreen` restores the vanilla right click and also cuts the clipboard, blueprint and filter interactions [jar]. The player would then no longer have access to hiring, the party, quests or gifts [inferred, to be tested]. The Hire and Party buttons are written in the Java screen, not in data [jar].

#### What clashes with the vision
- **Quests spawn vanilla monsters.** The encounters in `quests/_shared/encounters/*.json` create spiders, skeletons and pillagers. Quests ask to kill some (`mob_kill` on `minecraft:pillager`) or to go to the End (`end_survey`, `end_city_margin`, `end_city_survey`, `ender_freight`) [jar]. In Control! may not block these scripted spawns [inferred].
- **Retaliation, attack on sight, player raids and duels** create combat and social tension. The pack wants survival through the environment.
- **Hiring** automates mining, lumberjacking, farming and cooking. It duplicates Create and KC-Automation, and resembles Create Villager Labor, already ruled out [repo].
- **The interface is heavy**: reputation HUD, names, floating texts, quest item highlighting, dialogue camera. It breaks the mystery for the same reason as Jade [repo]. The dialogue camera also risks fighting with Shoulder Surfing and Camera Overhaul [inferred].
- **XP rewards** from quests [wiki] and mixin `ExperienceOrbMixin`: the XP bar no longer exists in the pack ([0009]) [jar, repo].
- **What serves the "survivors"**: a villager with a name, a memory and a relationship becomes a person, and every survivor counts. The native breeding block settles point 4 of [0004] without a script. The "downed" state for all protects these precious villagers. The party ("Follow") and mounts help with point 3 (bringing them back), but the party cannot be disabled [jar, inferred].
- **Ruined villages**: without villagers, retaliation and chest monitoring have nothing to do. The mod recognises a village by its members (`VillageMembership`: centre, radius, members, `vanillaVillageAtOrigin`), so the survivors' colony would probably become a tracked "village" [jar, inferred, to be tested].

#### Conflicts with the pack
- **Quark**: `Villagers Follow Emeralds = true` [repo]. The mod also rewrites the item sensor and the brain tick (`NearestItemSensorMixin`, `VillagerBrainTickMixin`). The two may get in each other's way, and "Follow" doubles the emerald [inferred, to be tested].
- **Lootr**: `watched_containers` only contains vanilla blocks. Lootr chests are therefore not monitored against theft [jar, inferred]. This has no effect in empty ruins; a tag is enough if needed.
- **Overgeared** and the trade filter (KubeJS): keep `trade.enableSkillTradeOverhaul=false`, otherwise the mod sells enchanted high-tier equipment. The craftsman and smelting professions could craft without Overgeared's forge if they go through vanilla recipes [jar, inferred]. The merchant screen and purse mixins touch the area of the trades filtered by MoreJS [jar, to be tested].
- **Friends & Foes**: the copper golem is not in `village_allegiance_holders`, and the beekeeper profession would take the generic dialogues [inferred]. No known hard conflict.
- **Companions!, Mowzie's Mobs**: with `villagersTargetHostileMobs=true`, villagers would go and attack these creatures and die [inferred].
- **Supplementaries**: no overlap found; the Supplementaries jar was not read [inferred].

#### Recommendation: to be tested, in reduced mode, with an abandonment criterion
The mod only partly passes the filter. It brings a mechanic (the memory and value of each villager), but it duplicates Create (hiring), adds combat and content (quests), and these are precisely the parts that cannot be switched off through the config. Test it on a `test/villageois` branch:
- packwiz: `packwiz mr add villager-retaliation`; dependencies `owo-lib` and `toucan`; side **CS**.
- `config/villagerretaliation.json5`: only the keys to change.
  - `general`: `enableVillagerRetaliation=false`, `enableDespisedKillOnSight=false`, `showVillagerNameTags=false`, `showTradeGuiReputationIcon=false`, `reputationChangeDisplayMode="OFF"` (values: HUD, CHAT, OFF), `enableVillagerDrops=false`, `enableWanderingTraderDrops=false`.
  - `notifications`: the six `enable*` keys set to false.
  - `quest`: `showQuestIndicators=false`, `enableQuestItemShaderHighlights=false`.
  - `dialogue`: `enableDialogueCameraFocus=false`, `enableNormalDialogueCameraFocus=false`, `enableDialogueCinematicBars=false`, `enableContainerForcedDialogue=false`, `enableRetaliationForcedDialogue=false`.
  - `social`: `enableVanillaVillagerBreeding=false`.
  - `combat`: `allVillagersUseDownedState=true`, `villagersTargetHostileMobs=false`, `villagersStandGroundAgainstHostileMobs=false`, `hostileTierHarassThrowEnabled=false`, `villagersPickUpGroundWeapons=false`, `wanderingTradersTargetHostileMobs=false`.
  - `duels.enabled=false`, `playerRaids.enabled=false`, `study.enabled=false` (it gives rewards), `trade.enableSkillTradeOverhaul=false` (default, to be frozen), `trade.enableSpecialOrders=false`.
- Datapack: remove the `villagerretaliation:quest_board` pool. KubeJS: remove the `payment_box`, `sell_box`, `clipboard` and `blueprint_checklist` recipes. Removing `clipboard` probably makes hiring unusable, since it is managed with the clipboard [inferred, to be tested].
- Keep it if the test shows survivors with a personality, without HUD or combat, and hiring out of reach. Otherwise, **do not integrate**: blocking breeding and filtering trades remain doable in KubeJS, as [0004] provides.

### 2.2 Dreamshift (`dreamshift`)

- **No NeoForge 1.21.1 version.** The mod only exists for Forge and Fabric **1.20.1** (latest version `0.1.4.3.1`, 2026-08-27). ARR, CS [Modrinth].
- Required: Architectury, Cloth Config and **Immersive Portals** 5.2.x [jar `mods.toml`]. Immersive Portals has no NeoForge 1.21.1 version on Modrinth [Modrinth]. The mod is therefore **unusable in Fallow**.
- What it does: it describes itself as an "atmospheric horror mod" [Modrinth]. It adds "slips" (falling in the manner of the backrooms, at random or from the void), "slippages" (cracks in walls, with faces) and fractured tools (after heavy use of the crafting table or furnace). All of this leads to 36 "Dreamspaces" (dreamcore, liminal spaces, Route 66, Level 7 Flooded…) in two dimensions, `dreamspace` and `deep_dreamspace` [Modrinth, jar].
- Horror: the `still_life` entity moves when not looked at, corrupts the player's skin and can become aggressive (disabled by default). There is also an observer, a "ceiling leg" and a zombie pigman. A "memory" retains the player's chunks and items. The mod also changes the title screen [jar].
- Config: `config/dreamshift.json` (AutoConfig) [jar]. Defaults: `enableRealitySlips=true`, `allowSlipFromRandom=true`, `randomSlipBaseChance=0.0025` every 200 ticks, `stillLifeEnabled=true`, `stillLifeCanBecomeAggressive=false`, `enableComplexMemory=true`, `enableSlippageFaces=true`, `enableFracturedTool=true`, `enableMainMenuTheme=true`, `enableDebugCommands=true` [jar].
- Sleep is not the trigger. The bed mixins only serve inside the Dreamspaces [jar]. There is therefore no direct overlap with Traveler's Backpack's sleeping bag. The overlap is with No Man's Land, which already has a dream dimension, that of the Friendly Moon's dream ([lune-amie]). There is none with Enhanced Celestials [repo, inferred].
- Vision: it is the opposite of "no horror or jumpscares". It breaks immersion (involuntary teleportation, modern and liminal spaces) and would duplicate NML's dream. Sanity: Renewed and TheWatcher were ruled out for the same reason [repo].

#### Recommendation: do not integrate
It does not run on NeoForge 1.21.1, its Immersive Portals dependency is not ported, and it is a horror mod. To add to the ruled-out list in `docs/mods.md`: "Horror mod, 1.20.1 only, requires Immersive Portals; NML's dream covers this role".

## 3. Animalia Exotica, Fish'n'Ships, Sunken Spires, Trek
### Summary

| Mod | 1.21.1 NeoForge | Size | Verdict |
|---|---|---|---|
| Animalia Exotica | 1.0.3 (2026-09-07) | 4.2 MB | **Do not integrate**: 4 species out of 6 are duplicates, vanilla biomes hard-coded |
| Fish'n'Ships | 1.1.0 (2026-09-12) | 1.4 MB | **To test**: real transport mechanic, no new dependency |
| Sunken Spires | 1.0+mod (2025-10-19) | 0.76 MB | **Do not integrate as is**; to be tested only in filtered form |
| Trek | B0.6.2+mod (beta, 2026-06-16) | 12.5 MB | **Do not integrate**: the rejection in 0004 stands |

---

### 3.1 Animalia Exotica (`animalia-exotica`, modId `animalia`)

**Versions and dependencies.** 1.0.2 and 1.0.3, NeoForge only, 1.21.1 [Modrinth]. Depends on GeckoLib `[4.9.2,5.0)` and NeoForge `>= 21.1.249` [jar]. GeckoLib 4.9.3 is already in the pack. ARR licence, about 4,500 downloads, the first 1.21.1 file dates from 2026-09-04 [Modrinth]. The project is very young.

**Content** [jar]:
- 6 creatures: elephant, jaguar, crocodile, hippopotamus, rhinoceros, moho (bird, AMBIENT category, lays eggs).
- Items: ivory and its range of blocks (bricks, pillars, slabs, walls), figurines, decorated rhinoceros horn, jaguar hide and cape, hide rug, hippopotamus hide boots, bottled hippopotamus sweat, crocodile tooth and egg, "water dagger", 3 paintings (Linnaeus, Darwin), 26 achievements.
- The description announces more than 50 animals to come (giraffe, ostrich, bear, Komodo dragon, python, tiger, bison…) [Modrinth]. Almost all already exist in Naturalist or Alex's Mobs.

**Hostility.** No horror. The crocodile has hunting and attack goals (`CrocodileMeleeAttackGoal`, `NearestAttackableTargetGoal`); the hippopotamus gets angry (achievements `anger_hippo` and `pacify_hippo`) [jar]. The elephant chases and breaks blocks (`ElephantChaseBlockBreaker`, tag `elephant_breakable`), as does the rhinoceros (tag `rhinoceros_breakable`) [jar]. This poses a risk of damage to bases, to be checked.

**Spawning.** It goes through `neoforge:add_spawns` with **vanilla biome identifiers written one by one** (`minecraft:savanna`, `jungle`, `desert`, `mangrove_swamp`, `beach`, `warm_ocean`…), plus a coded modifier that relies on the vanilla `Biomes` class [jar]. It uses no tag. NML biomes (bayou, prairie, tropical_beach…) therefore receive nothing. Vanilla biomes remain in the world with NML, so the animals still appear in savannas and jungles [inferred, from report verif-2026-10-18/4]. Weights are high: crocodile 20 in savanna, moho 25 in jungle.

**Configuration.** Common config `ENABLE_<SPECIES>_SPAWNING` per species, one weight per habitat (0 turns that habitat off), and a client option against the elephant's camera shake [jar]. These options only govern natural spawning: entities, items and recipes remain.

**Duplicates with the pack.**
| Animalia | Already covered by | Choice already made (plan 05 (`docs/plan/05-faune-et-ambiance.md`)) |
|---|---|---|
| Elephant | Alex's Mobs, Naturalist | AM keeps the elephant (caravan), Naturalist's is cut |
| Rhinoceros | Naturalist, AM | Naturalist |
| Hippopotamus | Naturalist | Naturalist |
| Crocodile | Naturalist alligator, AM crocodile and caiman | Naturalist alligator |
| Jaguar | none (AM has tiger and snow leopard, Naturalist has the lion) | new |
| Moho | none | new |

**The 5-question filter.** Gameplay: low. Integration: low (hard-coded biomes, no NML tags). 100 h: no. Mechanic: no, it is content ("adds animals"). Duplicates: 4 out of 6.

**Recommendation: do not integrate.** Only the jaguar and moho would remain, at the price of a very young ARR mod, 4 species to disable and surplus ivory items. To be revisited only if a future version brings a species with no equivalent and a real mechanic. If so: `animalia-exotica`, side CS, `ENABLE_ELEPHANT/RHINOCEROS/HIPPO/CROCODILE_SPAWNING = false`, and removal in KubeJS of the ivory recipes and spawn eggs.

---

### 3.2 Fish'n'Ships (`fish-n-ships`, modId `fish_n_ships`)

**Versions and dependencies.** `mc1.21.1-1.1.0-neoforge` (2026-09-12) and 1.0.0 [Modrinth]. Depends on GeckoLib `>= 4.7.4`, Hybrid API (`hapi`) `>= 1.1.0`, Kotlin for Forge (loader `[4,)`), NeoForge `>= 21.1.150` [jar]. **Everything is already in the pack**: GeckoLib 4.9.3, Hybrid API 1.1.2, Kotlin for Forge 5.12.0. Same studio as Hybrid Aquatic (Hybrid Labs). ARR licence, about 23,700 downloads.

**Content** [Modrinth, jar]:
- **Canoe**: 3 seats, or 2 seats and a chest, or 1 seat and a double chest.
- **Sailboat**: a banner serves as the sail. With the sail lowered, it moves on its own, even without a passenger. Parcels or animals can thus be sent over the water.
- **Raft**: 4 seats, cannot be steered, towed on a lead. **Supply raft**: 66 stacks, no passenger.
- **Dinghy** (1.1.0): 5 seats, or 3 seats and a double chest. In the jar, only the driftwood variant has a recipe; the other woods have an item but no recipe [jar]. This is probably an oversight by the author [inferred].
- **Ship**: large boat on which you can walk, built-in storage. It burns furnace fuel (coal, charcoal). Recipe: copper, blast furnace, chest boat, iron and planks.
- Ship upgrades: **icebreaker** (breaks `hapi:breakable_ice`) and **trawl**. The trawl fishes by itself using the **vanilla `FISHING`** loot table [jar].
- Crimson and warped wood boats; all Hybrid Aquatic driftwood variants.
- Supported woods: the 10 vanilla woods and driftwood. **No NML wood.** Every Compat 2.11.54 has no Fish'n'Ships module [jar].

**Hostility.** None. No worldgen, no creature.

**Configuration.** The jar contains no config file [jar]. The only possible tuning goes through recipes, in KubeJS.

**Integration and duplicates.**
- For the zoo and animal transport (plan 10 (`docs/plan/10-recentrage.md`), lune-amie (`docs/plan/lune-amie.md`)): the towed raft and the canoe carry animals over water before Create. This is the "boat" link of the zoo loop.
- The automatic sailboat opens up a small early-game logistics, between the vanilla boat and Create.
- **Overlap with Create Aeronautics and Sable**: the ship is a ready-made vehicle, coal-powered. It competes with the boats that players build with Aeronautics [inferred]. Nobody has tested these entities on a Sable sub-structure (raft towed from an airship, canoe placed on a Sable ship).
- **Trawl**: it gives passive fishing with the vanilla table, which contains enchanted books. This short-circuits the "found books" loop of Immersive Enchanting (0008 (`docs/decisions/0008-enchantement.md`)) and makes fishing automatic [inferred].
- Hybrid Aquatic: compatibility is planned by the author (driftwood) [Modrinth changelog].

**The 5-question filter.** Gameplay: yes (crossing, transporting). Integration: good (zoo, Hybrid Aquatic, no new dependency). 100 h: medium (early and mid-game tool, later relayed by Aeronautics). Mechanic: yes (autonomous sailboat, towed raft). Duplicates: partial, mostly the ship.

**Recommendation: to be tested**, on a `test/fish-n-ships` branch.
- Slug `fish-n-ships`, version `mc1.21.1-1.1.0-neoforge`, side CS.
- To keep: canoe, sailboat, raft, supply raft, dinghy.
- To be decided in testing, with recipe removal in KubeJS if needed: the **ship** (duplicates Aeronautics), the **trawl** (passive fishing; or else filter the `minecraft:gameplay/fishing` table with LootJS, already present), and the **icebreaker**.
- Test points: vehicles on or near a Sable sub-structure, transporting an animal by raft, behaviour in NML rivers, possible conflict with ItemPhysic and Camera Overhaul in a canoe.

---

### 3.3 Sunken Spires (`sunken-spires`)

**Versions.** Two 1.0 files from 2025-10-19, compatible with 1.21 to 1.21.10, including 1.21.1: a datapack `Sunken_Spires_1.21.zip` and a **mod version `1.0+mod`** for NeoForge, Forge, Fabric and Quilt [Modrinth]. The mod version is a datapack packed into a jar by Modrinth ("Generated by Modrinth"). It contains no class and its data is identical to that of the zip (empty `diff -r`) [jar]. `pack_format` 48, hence valid in 1.21.1. No dependency. ARR licence, about 790,000 downloads.
**Delivery.** No need for KubeJS: packwiz references `sunken-spires` (file `1.0+mod`) as a mod, side CS (single-player's integrated server needs it). The `pack/kubejs/data/` route (CONTRIBUTING) remains possible by copying the data, but it is pointless and the ARR licence suits it poorly.

**Content.** 11 structures in the `joshie` namespace, each in its own structure set (spacing/separation in chunks) [jar]:
| Structure | Spacing | Notable content |
|---|---|---|
| colossuem (sic) | 96/32 | chests, equipped drowned mini-boss (unique helmet) |
| conduit_ruins | 64/16 | conduit and prismarine rings |
| floating_spikes | 80/24 | circle of rings |
| large_fossil | 180/32 | rib cage, active conduit |
| ocean_temple | 96/24 | 4 chests |
| prismarine_forge | 72/34 | 2 chests, **elder guardian**, ancient debris |
| prismarine_trial_chamber | 108/32 | **17 trial spawners**, 18 vaults, drowned, guardians, elder guardians |
| small_fossil | 120/24 | archaeology |
| spires | 250/30 | archaeology, one chest |
| sunken_city | 160/24 | 5 houses, chests |
| trident_shrine | 180/80 | a trident |

**Hostility.** This is the major flaw. **All 11 structures** have a "monster" `spawn_overrides` that spawns **guardians and drowned** across their whole footprint (city: weight 120/220, groups of 3 to 5) [jar]. Drowned, guardians and elder guardians are also placed in the NBT [jar]. The pack forbids vanilla monsters, and plan 02 (`docs/plan/02-exploration.md`) specifies that "no structure produces a hostile creature" and that trial chambers are to be disabled. No horror in the strict sense: the atmosphere is "vanished civilisation", rather in the pack's tone.

**Biomes.** All structures use a single tag, `#joshie:has_structure/conduit_ruins`. It contains `#minecraft:is_ocean` plus optional Terralith, BoP, BYG… tags [jar]. NML adds no ocean, so oceans stay vanilla and the tag works [inferred, from report verif-2026-10-18/4]. Hybrid Aquatic keeps its biomes cut, so no conflict.

**Configuration.** There is no config. Sparse Structures applies, since the sets are `random_spread`: with a global `spreadFactor` of 3, the city goes from about 160 to about 480 chunks [inferred]. To cut a structure, the shared tag is not enough. The `biomes` field (empty tag) **and** `spawn_overrides` (removed) must be overridden in `pack/kubejs/data/joshie/worldgen/structure/sunken_spires/<name>.json`. The entities placed in the NBT must also be removed, through the KubeJS safety net or In Control! planned in plan 05 for vanilla monsters [inferred].

**Duplicates.** Vanilla ocean monuments become "ruins without guardians" (plan 02). Three Sunken Spires structures are prismarine variants of them (temple, forge, trial chamber). Vanilla trial chambers must also be cut. Hybrid Aquatic covers marine wildlife.

**The 5-question filter.** Gameplay: low (loot, archaeology). Integration: medium (vanilla oceans, archaeology shared with NML). 100 h: the silent ruins are, the arenas are not. Mechanic: no. Duplicates: with the monuments.

**Recommendation: do not integrate as is.** Variant **to be tested** if silent underwater discoveries are wanted:
- slug `sunken-spires`, file `1.0+mod`, CS;
- keep **conduit_ruins, floating_spikes, large_fossil, small_fossil, spires, trident_shrine**, and possibly sunken_city and ocean_temple, all without `spawn_overrides`;
- cut **colossuem, prismarine_forge, prismarine_trial_chamber** (boss, elder guardian, trial spawners);
- that is about 11 override files, plus the anti-hostile safety net. The cost is real for a 0.76 MB pack.

---

### 3.4 Trek (`trek`): reassessment

**Versions.** B0.6.2 (beta), datapack and `B0.6.2+mod` (code-free Modrinth jar), a single file for 1.21 to 26.2, `pack_format` 48 to 107, no overlay [Modrinth, jar]. The same JSON serves 1.21.1 to 26.x: it must be checked at load that nothing breaks in 1.21.1 [inferred]. MIT licence, 12.5 MB.

**Content** [jar]:
- **113 surface structures** in 5 sets: very_common 16/8 (rocks, plains), common 24/12 (13 structures), **medium 30/12 (76 structures)**, rare 48/24 (16), very_rare 96/48 (3). Examples: camps, houses, towers, forts, churches, cemetery, "haunted_house", smoking altar, bone dragon, floating islands, castle, Anubis pyramid, sunken city, wandering traders.
- **7 Trek villages** (plains, desert, savanna, snow, taiga, swamp, cherry grove) added to the `minecraft:villages` set, which is **replaced** (spacing 48/20).
- **Vanilla overrides**: pillager outposts, mansions, `stronghold` ("stronghold_big", tag `eye_of_ender_located`), End cities. `monster_room` and `monster_room_deep` are set to `count 0` and replaced by Trek structures with spawners, in sets with spacing 1.
- Nether and End structures.

**Hostility.** `spawn_overrides` add pillagers (11 structures), vindicators (6), strays, husks, witch, illusioner, skeleton, slime. The NBT place villagers, zombies, ravager, evoker and 5 spawners [jar]. Several structures are pillager-themed (`base_pillager`, `ship_pillager`, `pillager_mansion`…).

**Biomes and NML.** The Trek tags are **explicit lists** of vanilla, Terralith, BoP, Nature's Spirit and Regions Unexplored biomes. They use neither `#minecraft:is_forest` nor the `c:` tags, and **cite no NML biome** (0 occurrences) [jar]. The wiki confirms it: without hand-added compatibility, "Trek structures will simply not appear in the new biomes" [web]. With NML, Trek only populates the remaining vanilla biomes. Nothing breaks, but the distribution is incoherent: nothing in NML forests, everything in vanilla plains and savannas [inferred].

**Configurability.** There is no config. The 113 structures share 58 biome tags, so emptying one tag cuts several structures [jar]. To keep a selection, the **5 files `trek/worldgen/structure_set/overworld/*.json`** must be overridden with a whitelist. It is also necessary to: restore `minecraft:villages`, the outposts and the strongholds to plan 0004, restore or delete the `monster_room`s, and neutralise the `spawn_overrides` of the structures kept. Sparse Structures acts on these `random_spread` sets. On the other hand, the monster-room sets with spacing 1 remain dense [inferred].

**Conflicts with the pack.** Trek replaces the village set, whereas 0004 wants ruined villages by overriding the vanilla centres. It overrides the stronghold and the End, which the pack removes: it will be necessary to ensure that the KubeJS data comes after Trek's [inferred]. Its tone (merchants, living villages, pillagers, "haunted house") goes against the "survivors" concept and the absence of hostiles.

**Recommendation: do not integrate.** The rejection in 0004 (volume) is confirmed. A selection remains possible, but 5 sets would have to be rewritten, 4 vanilla overrides countered, NML biomes reassigned by hand and spawns cleaned up. The cost is disproportionate for 12.5 MB of content with no mechanic. If a few small ambience ruins are wanted, NML and Ancient Remnants already provide them.

---

### Unverified points
- The real behaviour of Fish'n'Ships vehicles near Sable sub-structures.
- Trek loading in 1.21.1, since the same JSON serves up to 26.2.
- The ability of In Control! or KubeJS to block entities placed by structure NBT (drowned, guardians): the lead is to be confirmed in game.
- The block-breaking behaviour of Animalia's elephant and rhinoceros (only class and tag names indicate it).
