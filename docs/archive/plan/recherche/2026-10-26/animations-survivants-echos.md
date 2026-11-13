# Tilth: animations of the survivors and echoes, and compatibility with Fresh Animations

Date: 26 October 2026. Research for the `tilth` module (NeoForge 1.21.1).

## Short answer

Yes, we can have animations compatible with Fresh Animations (FA), with no third-party mod and without copying a single FA file. Our creature only needs to use the vanilla player model layer (`ModelLayers.PLAYER` or `PLAYER_SLIM`). EMF picks its `.jem` files from this layer, not from the entity type. The FA Player Extension animations would therefore apply by themselves. No existing mod is a better fit: all of them bring a gameplay system that the pack does not want.

What is verified: the EMF source code (master branch) and the contents of the FA and FA Player Extension archives. What is not: the behavior in game. The tests are listed in point 5.

## 1. How EMF and ETF choose models

**EMF intercepts the layer, not the entity.** Its mixin `MixinEntityModelLoader` hooks into `EntityModelSet.bakeLayer`. It receives the `ModelLayerLocation` and replaces the baked model. In `EMFManager`, the file name comes from the layer path (`layer.model().getPath()`). The code does not look for the entity type identifier for mobs. Only block entities have a table per type.

**Added mods: supported.** The README says EMF does "little to no hardcoding" of vanilla models, which opens CEM to mod entities. FEATURES.md states that EMF handles "all modded entity models whose factory goes through the entity model loader". The `allowedCEM` setting is `ALL` by default (the pack does not provide an EMF config). It was added in 3.1 to separate known and unknown (mostly modded) models.

**File layout.** In `EMFDirectoryHandler`, the final path is `<namespace>:optifine/cem/<name>.jem` or `<namespace>:emf/cem/<name>.jem`, with a subfolder variant `<name>/<name>.jem`. For vanilla, the namespace is `minecraft`. For a modded layer `tilth:survivant#main`, it would be `assets/tilth/optifine/cem/survivant.jem` (or `emf/cem/`). The name is lowercased, characters outside `a-z0-9/._-` become `_`. The EMF 2.0 changelog confirms that the old `modded` folder was replaced by these namespaces.

**Unknown model.** EMF explores the model, maps each part to its own name, and can export a sample `.jem` into `emf/export/assets/<namespace>/optifine/cem/`. This is the tool for writing animations specific to our creatures.

**Player case.** EMF maps `player` and `player_slim` to a biped part set plus `jacket`, `left_sleeve`, `right_pants`, `cloak`, etc. `player_slim` falls back to `player.jem` if missing (changelog 2.3.0). The 2.4.4 changelog fixes a crash where "an entity was wrongly taken for a player": so the case of non-player entities with a player model does exist.

**ETF.** According to the README and the Modrinth page, mod entities work "as long as they extend `LivingEntityRenderer` and use it correctly", so if the mod uses the vanilla rendering code. `HumanoidMobRenderer` derives from it.

**Examples of mods that rely on this mechanism.** Who's There? (MIT) renders its "Ghost Players" with `HumanoidMobRenderer` and `ModelLayers.PLAYER`: I saw it in the jar classes. This is the exact precedent for the proposed method. I found no mod that explicitly announces FA compatibility for its own layers. The "EMF Compat" extension packs (Take a Seat, Supplementaries) exist, but I only saw them in a search summary.

**Verification limit.** I read the EMF master branch. The pack uses EMF 3.3.12 for 1.21 (`entity-model-features.pw.toml`). The mechanism is old (the 2.0 changelog describes it), but I did not read the 1.21 branch.

## 2. Fresh Animations license

Source: Modrinth pages of FA and FA Player Extension, plus the file `FAterms&conditions.txt` in the FAPE archive. All three say the same thing.

- FreshLX is the author and holds the copyright. The terms "may change at any time". Those of the official pages (CurseForge, Modrinth, Planet Minecraft) prevail over any file in the pack. The author "has the final say".
- The assets covered are the custom entity models (`.jem`, `.jpm`), the model animations and the icons.
- Allowed: including the pack in a modpack or a server. Modifying it for unshared personal use. Remixing it, transforming it and building on it "in your own resource pack, within reason, if credit is given", even while making money.
- Forbidden: redistributing the pack, porting it to Bedrock, including unmodified assets in one's own pack without permission, or putting them in a paid product without permission.
- Credit required: pack name and link to an official page.
- Modrinth shows the FAPE license as "Licensed ARR". The "early access" content (Ko-fi, including the torch and lantern poses) is marked "Do not distribute" in FA's `early-access.txt`. Never to be shipped.

**What this means for us.**
1. Depending on the presence of FA: yes, we already have it in the pack through packwiz (Modrinth link, no redistribution). The `tilth` mod must not require it. Without EMF, the creature plays the vanilla animations.
2. Shipping our own `.jem` and `.jpm` copied or adapted from FA: the permission covers a "resource pack" with credit. A mod jar is not literally a resource pack. When in doubt, put these files in a resource pack of the repository (`pack/resourcepacks/`), with credit, or ask FreshLX for agreement. I recommend asking, the wording "within reason" is vague.
3. The vanilla layer route avoids all of this: we ship no FA file.

## 3. Reusing vanilla types

**Villager or trader variant.** FA has `villager.jem` and `wandering_trader.jem` (folder `assets/minecraft/optifine/cem/` of FA 1.11). The animations depend on the file, not on the entity type: only a variable `is_wanderer` set in each `.jem` tells them apart. A `ModelLayers.VILLAGER` or `WANDERING_TRADER` layer on our own entity would therefore receive these animations. But the model stays the villager's: big nose, crossed arms. This is exactly the caricature that the maintainer wants to avoid. Using the real entities (`minecraft:villager`, `minecraft:wandering_trader`) also raises problems: professions, random trades, trader despawn, llamas, and the other mods that expect them. I did not find the two in `pack/config/incontrol/spawn.json` (only `zombie_villager`). Plan 03 mentions a gamerule `doTraderSpawning false`.

**Own entity with the player model.** This is the best option.
- The FAPE files (`player.jem`, `player_slim.jem`, `a_player_*.jpm`) have no condition on the entity type: I found neither `entity_type` nor `is_player` in them. They use `LivingEntity` variables: `limb_swing`, `is_riding`, `is_sneaking`, `is_in_water`, `is_swinging_right_arm`, `is_using_item`, etc. Fifteen `nbt(...)` calls (inventory, held item, `abilities.flying`) are specific to the player. On an NPC they give default values. Only these effects need watching (shield, map, flight).
- So: FAPE does not animate "only real players" at the mechanism level. It animates anything rendered that bakes the `player` or `player_slim` layer. I did not test it in game.
- The EMF 2.0.1 setting "apply player models only to your own player" is to be checked: it might not affect NPCs, but a player who enables it would see the difference.
- The FAPE pack in place does not provide the lantern pose (early access). The lantern is held in hand, with the vanilla pose of the item.

**Echoes.** A "lantern bearer" echo can be the same rendering as the survivor, with a dark faceless texture, translucent rendering, no collision. A slow "procession" can use vanilla horse layers (`ModelLayers.HORSE`, animated by FA's `horse.jem`) in front of a cart drawn by us. The mobile menhir needs no FA animation. Particles alone do not give an animated silhouette: to rule out, except for the dissolve.

## 4. Existing mods (NeoForge 1.21.1)

Versions and licenses: Modrinth API of 26/10/2026, completed by opening the pages and the jars.

| Mod | 1.21.1 NeoForge version | License | Animations | Fit |
|---|---|---|---|---|
| Guard Villagers | 2.4.12 | code MIT, textures ARR | own layer `guardvillagers:guard_steve`, no word on EMF | Village guards. Combat, patrol. No |
| MCA Reborn | 7.7.36 beta 3 | GPL-3.0 | model derived from `PlayerModel`, layer `mca`, no FA files | Marriages, children, villages. Too much system. No |
| Villagers Reborn | 1.0.9b | MIT on Modrinth, "All Rights Reserved" in the jar | own human models, animations credited to a named animator | Replaces villagers, pillagers, traders. Relations, marriage. Contradicts FA on these mobs. No |
| Modern Companions (ex Human Companions) | 2.0 | GPL-3.0 | not specified | RPG companions with levels and soul gems. No |
| Villager Recruits | no 1.21.1 version | ARR | not specified | Forge only, 1.18 to 1.20.1. Excluded |
| Easy NPC | 7.17.1 | MIT | not verified | Already rejected by the maintainer |
| CustomNPCs-Unofficial | 20241226 | CC BY-NC 3.0 | not verified | Last 1.21.1 jar from December 2024, non-commercial clause. No |
| Taterzens | 1.21.6 only | MIT | not verified | No NeoForge. Excluded |
| Ghost (already in the pack) | 1.3.4 | ARR on Modrinth, GPLv3 in the jar | `.geo.json` models and JSON animations, not EMF | Small tameable ghosts, kodama, haunted trees, night in the forest. Cute and with no human silhouette. Not suited to echoes |
| Who's There? Ghost Players | 1.4.1 | MIT | `ModelLayers.PLAYER` (technical precedent) | Invisible cave entities with usernames, "White Eyes" stalks the player. Tone too threatening. To cite as a code example only |
| Cloud Revive (Wandering Spirits) | 1.1.0 beta 1 | Apache-2.0 | own Java model and animation | Decorative natural spirits, no glow, no collision, invulnerable. Good tone reference, but small spirits, not a human silhouette |

Finding: no mod combines human NPCs, an empty atmosphere and FA animations. The pack has already removed Easy NPC. Keeping the gameplay (following the lantern, living in a clearing, filtered trade) in `tilth` is necessary anyway.

## 5. Recommendation

### Survivors: own entity on the player layer

- Type `tilth:survivant`, AI written in the module. Renderer: `HumanoidMobRenderer<Survivant, PlayerModel<Survivant>>`, with `context.bakeLayer(ModelLayers.PLAYER)` (wide arms) or `PLAYER_SLIM`. This is the Who's There? method. Add the vanilla armor layers only if the NPCs are equipped. FAPE's armor `.jem` files already exist (`player_inner_armor.jem`, etc.).
- Ship nothing from FA. If EMF is absent, the vanilla animations play.
- Differentiate the 12 silhouettes without touching the animations: a 64x64 texture per character (coat and hood painted on the `jacket` layer), scale between 0.92 and 1.08 in the renderer, helmet or hood as a head item, held item (lantern, staff, bag), thin arms for some. Avoid extra render layers (skirt, cape): they would not follow the parts animated by EMF.
- If one day we want our own animations (sitting by the ember, fear), two routes: an own layer `tilth:survivant#main` with a hand-written `.jem` in `assets/tilth/optifine/cem/` (we lose the FAPE animations), or keep the player layer and drive crouching and sitting position by the vanilla state of the entity (see tests).

### Echoes: same rendering, ghost version

- Lantern bearer: the same renderer, almost black faceless texture, translucent render type, alpha that decreases during the dissolve. Client-side entity only, no collision (neither `isPickable` nor push), with a short lifetime. How to create a client-only entity is to be checked in the Minecraft 1.21.1 code: I could not read it here.
- Slow cart: own model (two wheels, one box) animated by simple Java code, in front of a vanilla horse layer if we want the step animated by FA.
- Menhir: own model, no animation, advance by position.
- Stay discreet: no sudden sound, no gaze, following the rules already set in `fanal-et-echos.md`.

### To test in game

1. Open the EMF config, model list: check that `tilth:survivant` appears, and that the layer is indeed `minecraft:player#main`. Export the sample `.jem`.
2. Spawn a survivor next to a real player: walking, running, standing still. Compare the movements.
3. `is_sneaking`: does the NPC's crouched state (pose `CROUCHING`) trigger the FAPE animation?
4. `is_riding`: make the NPC sit through an invisible seat or the vanilla state, and see whether the FAPE pose plays.
5. Lantern in hand: does the arm stay coherent?
6. Remove EMF and ETF: the NPC is displayed with the vanilla animations, with no crash.
7. Disable the setting "player models for my player only" then re-enable it: effect on NPCs.
8. Dedicated server with two players: each player sees their own echoes.
9. Shield, map, flight: no strange movement coming from the player-specific `nbt(...)` variables.
10. Armor and helmet put on the NPC.

## Sources (opened pages and files)

- https://modrinth.com/mod/fa-player-extension (terms of use, features)
- https://modrinth.com/resourcepack/fresh-animations (terms of use, versions)
- FA Player Extension 1.1 archive: https://cdn.modrinth.com/data/TAIMVZCL/versions/Wj7NeGjP/FA%2BPlayer-v1.1.zip (`FAterms&conditions.txt`, `player.jem`, `a_player_*.jpm`)
- Fresh Animations 1.11 archive: https://cdn.modrinth.com/data/50dA9Sha/versions/KYOqeb1P/FreshAnimations_v1.11_1.20%2B.zip (`villager.jem`, `wandering_trader.jem`, `early-access.txt`)
- https://modrinth.com/mod/entity-model-features
- https://modrinth.com/mod/entitytexturefeatures
- https://github.com/Traben-0/Entity_Model_Features
- https://github.com/Traben-0/Entity_Model_Features/blob/master/FEATURES.md
- https://raw.githubusercontent.com/Traben-0/Entity_Model_Features/master/README.md
- https://raw.githubusercontent.com/Traben-0/Entity_Model_Features/master/CHANGELOG.MD
- https://raw.githubusercontent.com/Traben-0/Entity_Model_Features/master/src/main/java/traben/entity_model_features/EMFManager.java
- https://raw.githubusercontent.com/Traben-0/Entity_Model_Features/master/src/main/java/traben/entity_model_features/utils/EMFDirectoryHandler.java
- https://raw.githubusercontent.com/Traben-0/Entity_Model_Features/master/src/main/java/traben/entity_model_features/models/EMFModelMappings.java
- https://raw.githubusercontent.com/Traben-0/Entity_Model_Features/master/src/main/java/traben/entity_model_features/config/EMFConfig.java
- https://raw.githubusercontent.com/Traben-0/Entity_Model_Features/master/src/main/java/traben/entity_model_features/mixin/mixins/MixinEntityModelLoader.java
- https://github.com/Traben-0/Entity_Texture_Features
- https://modrinth.com/mod/guard-villagers
- https://modrinth.com/mod/villagers-reborn
- https://modrinth.com/mod/ghost
- https://modrinth.com/mod/whos-there-ghost-players
- https://modrinth.com/mod/cloud_revive
- https://modrinth.com/project/428soLwa (Modern Companions)
- https://modrinth.com/mod/villager-recruits
- Modrinth API (versions, licenses): https://api.modrinth.com/v2/project/{guard-villagers, minecraft-comes-alive-reborn, villagers-reborn, whos-there-ghost-players, ghost, easy-npc, customnpcs-unofficial, taterzens, villager-recruits, cloud_revive}
- Jars inspected (classes, mods.toml): Guard Villagers 2.4.12, MCA 7.7.36, Villagers Reborn 1.0.9b, Who's There? 1.4.1, Ghost 1.3.4, Cloud Revive 1.1.0 beta 1
- Tilth repository: `docs/mods.md`, `pack/config/incontrol/spawn.json`, `pack/mods/entity-model-features.pw.toml`, `docs/plan/03-civilisation.md`

Not opened (search summaries only): Entity Player Compat, EMF Compat Take a Seat, EMF Compat Supplementaries, CustomNPCs-Unofficial, Easy NPC, Taterzens, MCA (Modrinth pages).
