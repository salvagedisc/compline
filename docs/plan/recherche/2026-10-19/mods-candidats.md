# Mods candidats du 2026-10-19

Mission : L'équipe a proposé huit mods. Pour chacun : faut-il l'intégrer, comment, que garder et que couper pour éviter les doublons et la surcharge. Questions précises : peut-on adapter Identity Mask à NeoForge, est-il compatible avec Fresh Animations, et peut-on n'activer que certaines fonctions de Kingdoms: Villager Retaliation ?

Méthode : API Modrinth (NeoForge 1.21.1), page CurseForge, fichiers téléchargés et lus (`unzip`, `javap`, JSON, NBT) sans rien exécuter, wikis des mods. **Rien n'a été testé en jeu.** Chaque affirmation porte sa source : [Modrinth], [CF], [jar], [wiki], [web], [déduit].

## En bref

| Mod | Disponible en NeoForge 1.21.1 | Verdict | Raison principale |
|---|---|---|---|
| Identity Mask | Non : CurseForge seulement, Forge 1.20.1, ARR | Ne pas intégrer, ni porter, ni réécrire | Mécanique tournée contre les monstres vanilla et les villages vivants, que le pack retire |
| Kingdoms: Villager Retaliation | Oui, bêta (`1.0.0-beta.13-hotfix.1`), + `owo-lib` et `toucan` | À tester en mode réduit, avec critère d'abandon | Donne une valeur à chaque survivant et bloque la reproduction par la config ; mais l'embauche, le groupe, les quêtes et l'économie ne se coupent pas par la config |
| Dreamshift | Non : Forge et Fabric 1.20.1, demande Immersive Portals | Ne pas intégrer | Mod d'horreur ; le rêve de No Man's Land tient déjà ce rôle |
| Animalia Exotica | Oui (1.0.3) | Ne pas intégrer | 4 espèces sur 6 en doublon, biomes vanilla écrits en dur |
| Fish'n'Ships | Oui (`mc1.21.1-1.1.0-neoforge`), aucune dépendance nouvelle | À tester | Vraie mécanique de transport sur l'eau avant Create ; navire et chalut à trancher |
| Sunken Spires | Oui (`1.0+mod`) | Ne pas intégrer en l'état | Les 11 structures font apparaître gardiens et noyés ; une variante filtrée est possible |
| Trek | Oui (bêta B0.6.2) | Ne pas intégrer (0004 confirmé) | 113 structures, pillards, aucun biome NML, remplace les villages et le stronghold |
| Creature Feature | Oui (1.2.3.3), + Blueprint | Ne pas intégrer | 23 monstres, ton inquiétant, aucune option pour couper les apparitions |

**Identity Mask sur NeoForge.** Pas possible proprement. La licence (« All rights reserved ») interdit de forker ou de redistribuer sans l'accord écrit de l'auteur, et aucun code source n'est publié. Le mod compte 216 classes et 40 mixins écrits pour Forge 1.20.1 : le passage en 1.21.1 serait une réécriture. Sinytra Connector ne charge que des mods Fabric. On pourrait réécrire l'idée dans le mod `fallow`, mais elle ne sert qu'à tromper des monstres vanilla et des villageois vivants, absents de Fallow.

**Identity Mask et Fresh Animations.** Rien à tester en 1.21.1. D'après le code, le masque se dessine comme une couche sur la tête du modèle joueur et devrait suivre l'animation de FA Player Extension ; mais ses mixins touchent les mêmes couches (armure, tête, cape, objet en main, animations) qu'EMF et FA Player Extension, et 3D Skin Layers ne suivrait sans doute pas le skin emprunté [déduit].

**Villager Retaliation, fonction par fonction.** Un seul fichier, `config/villagerretaliation.json5`, commun au client et au serveur. Se coupent un par un : riposte, réputation, attaque à vue, butin, noms et HUD, notifications, chaque effet de dialogue, cadeaux, humeurs, étude, duels, raids du joueur, combat des villageois, état « à terre », commerce enrichi, et la reproduction (`social.enableVanillaVillagerBreeding=false`). Ne se coupent pas par la config : l'embauche (13 métiers et un bâtisseur), le groupe, les quêtes (seulement par datapack) et l'économie (seulement en retirant des recettes). Détails et valeurs à livrer en 2.1.

## Que faire ensuite

- Fish'n'Ships : branche `test/fish-n-ships` ; garder canoë, voilier, radeaux et dinghy ; décider du navire (doublon d'Aeronautics) et du chalut (pêche passive avec livres enchantés, qui court-circuite [0008](../../../decisions/0008-enchantement.md)).
- Villager Retaliation : branche `test/villageois` avec la config réduite de 2.1 ; abandon si l'embauche et le HUD restent présents. Le blocage de la reproduction et le filtre des échanges restent faisables en KubeJS ([0004](../../../decisions/0004-structures.md)).
- Sunken Spires : seulement si l'on veut des ruines sous-marines silencieuses, avec la variante filtrée de 3.3.
- Les cinq autres : une ligne dans « Sortis ou écartés » du [registre](../../../mods.md) quand l'équipe aura tranché.

## 1. Identity Mask et Creature Feature
### 1.1 Identity Mask

#### Identité

- Auteur : RetroParadox (CurseForge, id 1666070), créé le 2026-08-28, un seul fichier : `id_mask-1.0.0.jar`, bêta, environ 17 500 téléchargements. [CF]
- Chargeur et version : **Forge 1.20.1 uniquement** (`loaderVersion="[47,)"`, `minecraft [1.20.1,1.21)`). Pas de version Fabric, pas de NeoForge, pas de 1.21.1. [jar] [CF]
- Licence : **All rights reserved** (`license="All rights reserved"` dans `mods.toml`). [jar]
- Code source : aucun dépôt trouvé. La page CurseForge ne donne aucun lien source ; la recherche web ne trouve rien ; la recherche GitHub n'a pas pu être faite (API bloquée dans cette session). [CF] [web]
- Modrinth : **absent**. La page CurseForge renvoie vers `modrinth.com/mod/identity-mask`, mais l'API répond 404 (pas publié, ou pas encore approuvé). La recherche « identity mask » ne le trouve pas. Le seul projet Modrinth de l'auteur est `alien-evolution-first-person-model` (Forge 1.20.1, ARR). [Modrinth]

#### Ce que fait le mod

- Un masque (objet `id_mask`, casque ou emplacement de tête Curios) projette un **hologramme** : le joueur ressemble à un autre joueur (skin téléchargé depuis l'API Mojang) ou à une créature. Santé, hitbox, portée et vitesse ne changent pas. [CF] [jar]
- Créatures projetables : **zombie, villageois, enderman** seulement (« alien » verrouillé, attend le mod Alien Evolution). [jar] (chaînes de `Projections`, `MobTransformRenderer`)
- Effets : en zombie, les monstres ne vous ciblent plus ; en villageois, réductions de commerce ; « mode act » (bruits de pas, sons et mouvements de la créature) ; barre de soupçon au-dessus des mobs « malins » ; fausse armure visible. [CF]
- Mobs malins par défaut : creeper, witch, enderman, pillager, vindicator, evoker, illusioner, piglin, piglin brute ; liste extensible en config (`extraSmartMobs`). Les mobs d'autres mods sont trompés « sans réglage ». [jar] [CF]
- Obtention : un « masked chicken » apparaît dans les villages autour du joueur et lâche un masque cassé, réparé à l'enclume ; un « face mask » se fabrique. [jar] (`VillageMaskedChickenSpawner`, `AnvilRepairHandler`, `DeathDropHandler`)
- Ton : « technologie holographique avancée », aliens, panneau Discord dans le menu. [CF] [jar]

#### (a) Portage ou adaptation vers NeoForge 1.21.1

- **Licence** : ARR interdit de forker, décompiler pour republier ou redistribuer sans accord écrit de l'auteur. Un portage public n'est possible qu'avec sa permission. [jar] [déduit]
- **Taille** : 216 classes, environ 1 Mo de bytecode, **40 mixins** (20 communs sur l'IA de visée : squelette, blaze, ghast, wither, sorcière, arbalète, `LineOfSight`, `LookControl` ; 20 client sur le rendu : couches d'armure, de tête, de cape, d'objet en main, `AnimationUtils`, skin du joueur, `GameRenderer`), shaders et effets d'écran. Embarque MixinExtras 0.3.5. Dépendance optionnelle : Curios. [jar]
- **Écart 1.20.1 → 1.21.1** : NBT d'ItemStack remplacé par les data components, réseau réécrit (payloads), `getSkinTextureLocation` remplacé par `getSkin()`, packages `net.minecraftforge` → `net.neoforged`, cibles de mixins à recaler. Pour un mod aussi dépendant des mixins, c'est une réécriture plus qu'un portage. [jar] [déduit]
- **Sinytra Connector** : ne charge que des mods **Fabric**. Identity Mask est un mod Forge 1.20.1 : Connector ne peut rien en faire. Le pack s'interdit d'ailleurs tout mod Fabric hors de la famille Surveyor (`docs/plan/06-technique-et-serveur.md`). [jar] [déduit]
- **Réimplémentation dans `mods-custom/fallow/`** : l'idée n'est pas protégée, seul le code l'est ; on peut écrire une version propre sans reprendre le code. Mais l'intérêt du mod repose sur les monstres vanilla (les tromper, les rendre soupçonneux) et sur les villages vivants, deux choses que Fallow supprime (aucun monstre vanilla, villages en ruines vides). Il resterait une projection cosmétique de joueur ou de villageois et un effet de furtivité contre les rares hostiles moddés, ce qui affaiblirait justement les créatures territoriales voulues par la vision. Le coût (IA de visée de chaque mob, rendu, réseau) est hors de proportion. [déduit]
- Pour Fallow, la question du portage est donc théorique : même porté, le mod échoue au filtre (pas d'intégration aux systèmes du pack, mécanique tournée contre des mobs absents, ton « hologramme/alien » contraire à la DA). [déduit]

#### (b) Compatibilité avec Fresh Animations (EMF/ETF)

Aucun test possible : le mod n'existe pas pour 1.21.1. Points relevés dans le code :
- Le masque est rendu par une couche propre (`MaskHeadLayer`, modèle d'objet sur la tête du `PlayerModel`) ou via Curios. EMF anime les parties du modèle joueur ; une couche qui suit `head` devrait suivre la tête animée de FA Player Extension, sans garantie. [jar] [déduit]
- La projection en joueur remplace la texture renvoyée par `AbstractClientPlayer` : FA Player Extension garderait son modèle avec l'autre skin. 3D Skin Layers lit la texture du skin pour construire ses couches en relief ; risque de couches décalées ou de l'ancien skin. [jar] [déduit]
- La projection en créature annule `RenderPlayerEvent.Pre` et dessine un mob « proxy » ; si ce proxy passe par le rendu vanilla, EMF lui appliquerait FA. [jar] [déduit]
- Mixins sur `HumanoidArmorLayer`, `CustomHeadLayer`, `CapeLayer`, `ItemInHandLayer`, `AnimationUtils` : mêmes zones que celles touchées par EMF et FA Player Extension ; conflits plausibles. [jar] [déduit]

#### Recommandation : ne pas intégrer

- Absent de Modrinth, Forge 1.20.1 seulement, ARR, aucune source : bloquant selon `CONTRIBUTING.md`.
- Ne pas le porter ni le réimplémenter : la mécanique vise des monstres et des villages que le pack retire.
- Rien à configurer. Si l'envie est seulement cosmétique (prendre l'apparence d'un autre joueur), deux pistes Modrinth NeoForge 1.21.1 existent, non étudiées ici : `disguiseheads` (tr7zw, auteur de 3D Skin Layers, client seul, licence propriétaire de tr7zw) et `player-disguise` (MPL-2.0, 893 téléchargements). [Modrinth]
- À ajouter à « Sortis ou écartés » de `docs/mods.md` si l'on veut garder la trace : « Identity Mask : CurseForge seulement, Forge 1.20.1, ARR ; mécanique tournée contre les monstres vanilla ».

### 1.2 Creature Feature

#### Identité

- Slug `creature-feature`, auteur AtiredDev (avec « Team Galena »), licence **MIT**, source annoncée `github.com/TerriblyBadCoder/creaturefeature` (non vérifiée : GitHub inaccessible ici). [Modrinth] [jar]
- **NeoForge 1.21.1 uniquement**, côté CS (client et serveur requis). Dernière version **1.2.3.3** (2026-08-27), 6 versions en août, environ 46 000 téléchargements. [Modrinth]
- Dépendances : **Blueprint** (`blueprint`, requis, absent du pack) ; **Sable** optionnel (déjà dans le pack). Compat optionnelle avec Oreganized et Caverns & Chasms (butin). NeoForge ≥ 21.1.228 : compatible avec 21.1.251. [Modrinth] [jar]

#### Contenu

- Le titre et `mods.toml` le disent : « **23 Hostile creatures** ». Toutes les entités vivantes sont enregistrées en catégorie `MONSTER`. [Modrinth] [jar]
- Créatures et apparitions (biome modifiers du jar) : [jar] [Modrinth]
  - Surface : Minds (zombie au cerveau géant, savanes), Blossom (zombie-fleur, jungles), Fend (fleurs, 3 variantes dont archer), Coat of Arms (marais, tir en rafale), Mockingbird (montagnes, « étrangement humanoïde »), Vertigo (forêt sombre, taïgas), Runaway (plages), Stained Glass (déserts), Toadstool (plaines, **activé seulement si Sable est présent**, donc actif dans Fallow).
  - Grottes et tout l'Overworld (`#c:is_overworld`) : Sinister (masse et miroir), Detritus (zombie qui crée des copies dégradées de lui-même), Canary, Beauty (grottes luxuriantes), Pathogen (soigne les morts-vivants).
  - Nether et End : Machination, Fiend, Friend, Eeper, Blitz, MinedFlayer, NoThing, Dreamweaver, Saint Solis. L'End est supprimé dans Fallow.
- Ton : étrange et souvent inquiétant (NoThing, silhouette floue qui hurle puis fonce ; Detritus « a normal person » ; Friend « your friend, that is »), shaders de post-traitement, mixins sur la caméra et l'interface (`CameraMixin`, `CFPostChainMixin`, `IamActivelyHavingaMigraineMixin`), effets qui forcent la troisième personne ou font tourner la caméra. C'est du horror léger ou du « weird », proche de ce que la vision exclut. [Modrinth] [jar]
- Ajoute aussi environ 47 recettes (blocs de carapace, tuiles, plumes, flintlock, hache lourde, matériaux d'armure trim), 2 disques de musique, des avancements. [jar]

#### Configurabilité

- La config ne permet **pas** de désactiver une créature ni ses apparitions : elle ne propose que des remodèles (Friend, Mockingbird), un correctif de post-traitement, et des réglages de Dreamweaver, Pathogen, Saint Solis et Fiend. [jar] (`Config`)
- On peut couper les apparitions par datapack (surcharger les 27 fichiers `data/creaturefeature/neoforge/biome_modifier/*.json` par `neoforge:none` dans `pack/kubejs/data/creaturefeature/`) ou par In Control!. Les entités, objets et recettes restent enregistrés. [jar] [déduit]

#### Recoupements et rendu

- Pas de doublon d'espèce avec Naturalist, Alex's Mobs Continued, Critters and Companions ou Friends & Foes : ce sont des monstres inventés, pas des animaux. Mais il **ajoute 23 hostiles**, à l'opposé de « peu d'hostiles, rares, territoriaux » ; plusieurs sont des variantes de zombie, alors que les zombies vanilla sont retirés. [Modrinth] [déduit]
- Pas de GeckoLib : modèles Java (`ModelLayerLocation`, `ModelPart`), aucun fichier `geo` ni animation Bedrock. [jar]
- Fresh Animations ne couvre que les mobs vanilla : rien pour ces créatures. EMF pourrait leur appliquer des modèles OptiFine-CEM écrits exprès, mais il n'en existe pas. Sans intérêt pour FA. [jar] [déduit]

#### Recommandation : ne pas intégrer

- Échoue au filtre : contenu (23 monstres) plutôt que mécanique ; hostiles nombreux et répartis dans tout l'Overworld au lieu de rares et territoriaux ; ton qui frôle l'horreur ; un tiers du contenu dans l'End supprimé ; une dépendance de plus (Blueprint) ; mixins caméra et post-traitement qui peuvent heurter ATMOSPHERICS, Shoulder Surfing et Camera Overhaul [déduit].
- Si l'on voulait malgré tout une ou deux créatures (par exemple Vertigo en forêt sombre ou Mockingbird en montagne comme hostile rare et territorial) : `packwiz mr add creature-feature` (côté both) avec `blueprint`, puis surcharger tous les biome modifiers sauf ceux retenus, baisser leur poids (Vertigo 18, Mockingbird 22 aujourd'hui) et les cantonner par In Control!. Il faudrait aussi masquer les recettes et objets inutiles dans EMI. À ce prix, autant ne pas l'ajouter.
- À ajouter à « Sortis ou écartés » : « Creature Feature : 23 monstres, ton inquiétant, aucune option pour couper les apparitions ».

## 2. Kingdoms: Villager Retaliation et Dreamshift
### 2.1 Kingdoms: Villager Retaliation! (`villager-retaliation`)

#### Versions et dépendances
- Seul NeoForge 1.21.1 est publié. Dernière version : `1.0.0-beta.13-hotfix.1` (2026-08-18), 17 versions, **toutes en bêta**. Licence ARR, mais l'auteur autorise l'usage en modpack. Côté client **et** serveur [Modrinth].
- La bêta 13 change l'IA et le format de sauvegarde : l'auteur demande de sauvegarder le monde avant la mise à jour. Il n'ajoutera pas de patchs de compatibilité avant la 1.0.0 [Modrinth]. C'est un risque pour un monde permanent.
- Requis : NeoForge ≥ 21.1.238 (le pack est en 21.1.251, donc compatible), **oωo-lib** (`owo-lib`, ≥ 0.12.15.1-beta.6 ; version actuelle `0.12.15.6-beta.1+1.21`, en bêta), **toucanLib** (`toucan`, ≥ 0.5.0) et Architectury (déjà dans le pack) [jar `neoforge.mods.toml`, Modrinth, repo]. owo-lib et toucanLib seraient deux nouvelles bibliothèques.
- Optionnels : EMI, JEI, REI, Curios, Accessories, EMF/ETF, Second Wind [jar].

#### Fonctions
Le mod étend le villageois vanilla sans remplacer l'entité. Il pose 39 mixins serveur et 20 mixins client sur le cerveau du villageois, le commerce, la panique, la cloche, la reproduction, la navigation et l'écran de marchand [jar, Modrinth].
1. **Riposte** : villageois et golems attaquent le joueur fautif. Les « Despised » sont attaqués à vue [Modrinth, jar].
2. **Réputation** par villageois et par joueur, en 9 paliers, avec commérages, humeurs et mémoire du village [Modrinth].
3. **Écran d'interaction** : dialogues, caméra qui zoome, bandes cinéma, voix en « blips » [jar].
4. **Cadeaux** et souvenirs [jar].
5. **Embauche** : 13 métiers (combat, chasse, mine, bûcheron, ferme, pêche, alchimie, artisan, élevage, cuisine, fonte, coursier, nitwit) et un bâtisseur qui suit des plans. Le tout se gère au presse-papiers, avec salaires [Modrinth].
6. **Économie** : bourse personnelle de chaque villageois, Payment Box, Sell Box à prix journaliers [Modrinth].
7. **Groupe** de 4 villageois maximum : suivre, rester, garder, ramasser. Ils peuvent monter des chevaux [Modrinth].
8. **Raids menés par le joueur** (bannière sinistre portée en casque et corne de chèvre) et **duels** [Modrinth].
9. **Quêtes** : 85 annoncées, 278 fichiers. Journal sur J, suivi sur K [Modrinth, jar].
10. **Étude** d'un villageois, avec récompense [jar].
11. **État « à terre »** : le villageois tombe au lieu de mourir, puis se relève [jar].
12. **Combat des villageois** : ils frappent les hostiles, ramassent des armes, et les clercs lancent des potions [jar].
13. **Butin** à la mort des villageois et des marchands ambulants, et messages de mort [jar].

#### Peut-on n'activer que certaines fonctions ? En partie seulement.
**Fichier** : un seul, `config/villagerretaliation.json5`, au format oωo (`@Config(name="villagerretaliation")`) [jar]. Le nom du fichier vient de la convention oωo [déduit]. Ce fichier n'est ni client ni serveur : il sert aux deux, et le serveur pousse ses valeurs aux joueurs connectés (`syncServerConfigToConnectedPlayers`). Au premier lancement, le mod migre les anciens `villagerretaliation-common.toml` et `-client.toml` [jar]. Les sections sont imbriquées (`"general": { ... }`). Défauts lus dans les constructeurs [jar] :

| Fonction | Interrupteur (défaut) | Indépendant ? |
|---|---|---|
| Riposte | `general.enableVillagerRetaliation` (true) ; détails dans `retaliation.*` | Oui |
| Réputation | `general.enableVillagerReputation` (true) ; `reputation.reputationDecayEnabled` (true) ; `reputation.enableReputationTradePricing` (true) | Oui |
| Lien avec les ragots vanilla | `general.enableVanillaGossipIntegration` (true) | Oui |
| Attaque à vue des « Despised » | `general.enableDespisedKillOnSight` (true) | Oui |
| Butin et messages de mort | `general.enableVillagerDrops`, `enableWanderingTraderDrops`, `enableVillagerDeathMessages` (true) ; `wanderer.*` | Oui |
| Noms au-dessus des têtes, icône dans l'écran de commerce | `general.showVillagerNameTags` (true), `showTradeGuiReputationIcon` (true), `reputationChangeDisplayMode` (HUD) | Oui |
| Textes flottants et murmures | `notifications.enableWorldTextNotifications`, `enableAmbientMurmurs`, `enableSleepIndicators`, `enableDamageAlerts`, `enableCombatAlerts`, `enableTradeAndGiftWorldText` (tous true) | Oui |
| Écran d'interaction | `dialogue.enableInteractionScreen` (true). Les effets se coupent un par un : `enableDialogueCameraFocus`, `enableNormalDialogueCameraFocus`, `enableDialogueCinematicBars`, `enableForcedDialogue`, `enableContainerForcedDialogue`, `enableRetaliationForcedDialogue`, `enableDialogueBlipAudio`, `freezeVillagerDuringDialogue` (true) | Oui |
| Cadeaux | `gifts.enableVillagerGifts`, `enableHighReputationGifts`, `enableGiftKeepsakes` (true) | Oui |
| Social, humeurs | `social.enableVillagerSocialGraph`, `enableVillagerMoods`, `enableSocialAttribute*` (true) | Oui |
| **Reproduction** | `social.enableVanillaVillagerBreeding` (true). À false, toute reproduction est bloquée (`BREEDING_DISABLED`, mixin `VillagerMakeLoveMixin`) [jar] | Oui |
| Étude | `study.enabled` (true) | Oui |
| Duels | `duels.enabled` (true) | Oui |
| Raids du joueur | `playerRaids.enabled` (true) | Oui |
| État « à terre » | `combat.enableVillagerDownedState` (true) ; `allVillagersUseDownedState` (false) ; `partyVillagersUseDownedState` (true) | Oui |
| Combat des villageois | `combat.villagersTargetHostileMobs`, `villagersRetaliateAgainstHostileMobs`, `villagersStandGroundAgainstHostileMobs`, `*FightBack` par métier, `clericsUsePotions`, `hostileTierHarassThrowEnabled`, `villagersPickUpGroundWeapons` (tous true) | Oui |
| Commerce enrichi | `trade.enableSkillTradeOverhaul` (**false**) : équipement de haut rang, enchantements jusqu'au niveau 3 ; `trade.enableSpecialOrders` (true) ; `trade.disableVillagerWalletLimit` (false) | Oui |
| Faim, famine | `balance.hungerEffectAffectsVillagers` (true), `enableVillagerStarvationDamage` (false) | Oui |
| **Embauche (13 métiers, bâtisseur)** | **Aucun interrupteur** ; seulement `balance.hiredContract*` (coûts) et `enableHiredWorkSkillGrowth` | **Non** |
| **Groupe** | **Aucun interrupteur** | **Non** |
| **Quêtes** | Seuls les indicateurs se coupent : `quest.showQuestIndicators`, `enableQuestItemShaderHighlights`. Pour retirer les offres, un datapack retire le pool `villagerretaliation:quest_board` (`remove: true` avec `id`, [wiki]) ou remplace son fichier avec `"enabled": false` (le champ existe [jar], effet à tester) | Par datapack seulement |
| **Économie (Payment Box, Sell Box)** | Aucun interrupteur. On retire les recettes (`data/villagerretaliation/recipe/payment_box.json`, `sell_box.json`) en KubeJS, et on désactive les prix fichier par fichier avec `enabled: false` [wiki] | Par KubeJS et datapack |

- Aucune gamerule n'est ajoutée [jar].
- Tags datapack [jar] : `watched_containers`, `village_terrain`, `currency` (émeraude), `assignable_mounts`, `protected_civilians`, `village_allegiance_holders` (villageois, villageois zombie, golem de fer), `allegiance_exempt`, `#villagerretaliation:village_footprint` (= `#minecraft:village`). Les dialogues, noms, cadeaux, prix et quêtes viennent tous de JSON et se remplacent par datapack [jar, wiki].
- Couper `dialogue.enableInteractionScreen` rend le clic droit vanilla et coupe aussi les interactions au presse-papiers, aux plans et aux filtres [jar]. Le joueur n'aurait donc plus accès à l'embauche, au groupe, aux quêtes ni aux cadeaux [déduit, à tester]. Les boutons Embauche et Groupe sont écrits dans l'écran Java, pas dans des données [jar].

#### Ce qui heurte la vision
- **Les quêtes font apparaître des monstres vanilla.** Les rencontres `quests/_shared/encounters/*.json` créent des araignées, des squelettes et des pillards. Des quêtes demandent d'en tuer (`mob_kill` sur `minecraft:pillager`) ou d'aller dans l'End (`end_survey`, `end_city_margin`, `end_city_survey`, `ender_freight`) [jar]. In Control! ne bloque peut-être pas ces apparitions scriptées [déduit].
- **Riposte, attaque à vue, raids du joueur et duels** font du combat et de la tension sociale. Le pack veut une survie par l'environnement.
- **L'embauche** automatise mine, bûcheronnage, ferme et cuisine. Elle double Create et KC-Automation, et ressemble à Create Villager Labor, déjà écarté [repo].
- **L'interface est chargée** : HUD de réputation, noms, textes flottants, surbrillance des objets de quête, caméra de dialogue. Elle casse le mystère pour la même raison que Jade [repo]. La caméra de dialogue risque aussi de se battre avec Shoulder Surfing et Camera Overhaul [déduit].
- **Récompenses en XP** des quêtes [wiki] et mixin `ExperienceOrbMixin` : la barre d'XP n'existe plus dans le pack ([0009]) [jar, repo].
- **Ce qui sert les « survivants »** : un villageois avec un nom, une mémoire et une relation devient une personne, et chaque survivant compte. Le blocage natif de la reproduction règle le point 4 de [0004] sans script. L'état « à terre » pour tous protège ces villageois précieux. Le groupe (« Suivre ») et les montures aident au point 3 (les ramener), mais le groupe ne se désactive pas [jar, déduit].
- **Villages en ruine** : sans villageois, la riposte et la surveillance des coffres n'ont rien à faire. Le mod reconnaît un village par ses membres (`VillageMembership` : centre, rayon, membres, `vanillaVillageAtOrigin`), donc la colonie de survivants deviendrait sans doute un « village » suivi [jar, déduit, à tester].

#### Conflits avec le pack
- **Quark** : `Villagers Follow Emeralds = true` [repo]. Le mod réécrit aussi le capteur d'objets et le tick du cerveau (`NearestItemSensorMixin`, `VillagerBrainTickMixin`). Les deux peuvent se gêner, et « Suivre » double l'émeraude [déduit, à tester].
- **Lootr** : `watched_containers` ne contient que les blocs vanilla. Les coffres Lootr ne sont donc pas surveillés contre le vol [jar, déduit]. C'est sans effet dans des ruines vides ; il suffit d'un tag si besoin.
- **Overgeared** et filtre des échanges (KubeJS) : garder `trade.enableSkillTradeOverhaul=false`, sinon le mod vend de l'équipement de haut rang enchanté. Les métiers artisan et fonte pourraient fabriquer sans la forge d'Overgeared s'ils passent par les recettes vanilla [jar, déduit]. Les mixins de l'écran de marchand et de la bourse touchent la zone des échanges filtrés par MoreJS [jar, à tester].
- **Friends & Foes** : le golem de cuivre n'est pas dans `village_allegiance_holders`, et le métier d'apiculteur prendrait les dialogues génériques [déduit]. Pas de conflit dur connu.
- **Companions!, Mowzie's Mobs** : avec `villagersTargetHostileMobs=true`, les villageois iraient attaquer ces créatures et mourraient [déduit].
- **Supplementaries** : aucun recouvrement trouvé ; le jar de Supplementaries n'a pas été lu [déduit].

#### Recommandation : à tester, en mode réduit, avec un critère d'abandon
Le mod ne passe le filtre qu'en partie. Il apporte une mécanique (la mémoire et la valeur de chaque villageois), mais il double Create (embauche), il ajoute du combat et du contenu (quêtes), et ce sont justement les parties qu'on ne peut pas couper par la config. Le tester sur une branche `test/villageois` :
- packwiz : `packwiz mr add villager-retaliation` ; dépendances `owo-lib` et `toucan` ; côté **CS**.
- `config/villagerretaliation.json5` : seules les clés à changer.
  - `general` : `enableVillagerRetaliation=false`, `enableDespisedKillOnSight=false`, `showVillagerNameTags=false`, `showTradeGuiReputationIcon=false`, `reputationChangeDisplayMode="OFF"` (valeurs : HUD, CHAT, OFF), `enableVillagerDrops=false`, `enableWanderingTraderDrops=false`.
  - `notifications` : les six clés `enable*` à false.
  - `quest` : `showQuestIndicators=false`, `enableQuestItemShaderHighlights=false`.
  - `dialogue` : `enableDialogueCameraFocus=false`, `enableNormalDialogueCameraFocus=false`, `enableDialogueCinematicBars=false`, `enableContainerForcedDialogue=false`, `enableRetaliationForcedDialogue=false`.
  - `social` : `enableVanillaVillagerBreeding=false`.
  - `combat` : `allVillagersUseDownedState=true`, `villagersTargetHostileMobs=false`, `villagersStandGroundAgainstHostileMobs=false`, `hostileTierHarassThrowEnabled=false`, `villagersPickUpGroundWeapons=false`, `wanderingTradersTargetHostileMobs=false`.
  - `duels.enabled=false`, `playerRaids.enabled=false`, `study.enabled=false` (il donne des récompenses), `trade.enableSkillTradeOverhaul=false` (défaut, à figer), `trade.enableSpecialOrders=false`.
- Datapack : retirer le pool `villagerretaliation:quest_board`. KubeJS : retirer les recettes `payment_box`, `sell_box`, `clipboard` et `blueprint_checklist`. Retirer `clipboard` rend sans doute l'embauche inutilisable, puisqu'elle se gère au presse-papiers [déduit, à tester].
- Le garder si le test montre des survivants qui ont une personnalité, sans HUD ni combat, et une embauche hors d'atteinte. Sinon, **ne pas intégrer** : bloquer la reproduction et filtrer les échanges restent faisables en KubeJS, comme le prévoit [0004].

### 2.2 Dreamshift (`dreamshift`)

- **Pas de version NeoForge 1.21.1.** Le mod n'existe que pour Forge et Fabric **1.20.1** (dernière version `0.1.4.3.1`, 2026-08-27). ARR, CS [Modrinth].
- Requis : Architectury, Cloth Config et **Immersive Portals** 5.2.x [jar `mods.toml`]. Immersive Portals n'a aucune version NeoForge 1.21.1 sur Modrinth [Modrinth]. Le mod est donc **inutilisable dans Fallow**.
- Ce qu'il fait : il se décrit comme un « atmospheric horror mod » [Modrinth]. Il ajoute des « slips » (chute à la manière des backrooms, au hasard ou depuis le vide), des « slippages » (fissures dans les murs, avec des visages) et des outils fracturés (après un usage intensif de l'établi ou du four). Tout cela mène vers 36 « Dreamspaces » (dreamcore, espaces liminaux, Route 66, Level 7 Flooded…) dans deux dimensions, `dreamspace` et `deep_dreamspace` [Modrinth, jar].
- Horreur : l'entité `still_life` bouge quand on ne la regarde pas, corrompt la peau du joueur et peut devenir agressive (désactivé par défaut). Il y a aussi un observateur, une « jambe de plafond » et un zombie pigman. Une « mémoire » retient les chunks et les objets du joueur. Le mod change aussi l'écran titre [jar].
- Config : `config/dreamshift.json` (AutoConfig) [jar]. Défauts : `enableRealitySlips=true`, `allowSlipFromRandom=true`, `randomSlipBaseChance=0.0025` toutes les 200 ticks, `stillLifeEnabled=true`, `stillLifeCanBecomeAggressive=false`, `enableComplexMemory=true`, `enableSlippageFaces=true`, `enableFracturedTool=true`, `enableMainMenuTheme=true`, `enableDebugCommands=true` [jar].
- Le sommeil n'est pas le déclencheur. Les mixins sur le lit ne servent qu'à l'intérieur des Dreamspaces [jar]. Il n'y a donc pas de recouvrement direct avec le sac de couchage de Traveler's Backpack. Le recouvrement est avec No Man's Land, qui a déjà une dimension de rêve, celle du rêve de la Lune Amie ([lune-amie]). Il n'y en a pas avec Enhanced Celestials [repo, déduit].
- Vision : c'est le contraire de « sans horreur ni jumpscare ». Il casse l'immersion (téléportation involontaire, espaces modernes et liminaux) et il ferait doublon avec le rêve de NML. Sanity: Renewed et TheWatcher ont été écartés pour la même raison [repo].

#### Recommandation : ne pas intégrer
Il ne tourne pas sur NeoForge 1.21.1, sa dépendance Immersive Portals n'est pas portée, et c'est un mod d'horreur. À ajouter aux écartés de `docs/mods.md` : « Mod d'horreur, 1.20.1 seulement, demande Immersive Portals ; le rêve de NML couvre ce rôle ».

## 3. Animalia Exotica, Fish'n'Ships, Sunken Spires, Trek
### Synthèse

| Mod | 1.21.1 NeoForge | Poids | Verdict |
|---|---|---|---|
| Animalia Exotica | 1.0.3 (2026-09-07) | 4,2 Mo | **Ne pas intégrer** : 4 espèces sur 6 en doublon, biomes vanilla codés en dur |
| Fish'n'Ships | 1.1.0 (2026-09-12) | 1,4 Mo | **À tester** : mécanique de transport réelle, aucune dépendance nouvelle |
| Sunken Spires | 1.0+mod (2025-10-19) | 0,76 Mo | **Ne pas intégrer en l'état** ; à tester seulement en version filtrée |
| Trek | B0.6.2+mod (bêta, 2026-06-16) | 12,5 Mo | **Ne pas intégrer** : le rejet de 0004 tient |

---

### 3.1 Animalia Exotica (`animalia-exotica`, modId `animalia`)

**Versions et dépendances.** 1.0.2 et 1.0.3, NeoForge seulement, 1.21.1 [Modrinth]. Dépend de GeckoLib `[4.9.2,5.0)` et de NeoForge `>= 21.1.249` [jar]. GeckoLib 4.9.3 est déjà dans le pack. Licence ARR, environ 4 500 téléchargements, le premier fichier 1.21.1 date du 2026-09-04 [Modrinth]. Le projet est très jeune.

**Contenu** [jar] :
- 6 créatures : éléphant, jaguar, crocodile, hippopotame, rhinocéros, moho (oiseau, catégorie AMBIENT, pond des œufs).
- Objets : ivoire et sa gamme de blocs (briques, piliers, dalles, murs), figurines, corne de rhinocéros décorée, peau et cape de jaguar, tapis de peau, bottes en peau d'hippopotame, sueur d'hippopotame en bouteille, dent et œuf de crocodile, « water dagger », 3 tableaux (Linné, Darwin), 26 succès.
- La description annonce plus de 50 animaux à venir (girafe, autruche, ours, varan de Komodo, python, tigre, bison…) [Modrinth]. Presque tous existent déjà dans Naturalist ou Alex's Mobs.

**Hostilité.** Pas d'horreur. Le crocodile a des objectifs de chasse et d'attaque (`CrocodileMeleeAttackGoal`, `NearestAttackableTargetGoal`) ; l'hippopotame se met en colère (succès `anger_hippo` et `pacify_hippo`) [jar]. L'éléphant poursuit et casse des blocs (`ElephantChaseBlockBreaker`, tag `elephant_breakable`), le rhinocéros aussi (tag `rhinoceros_breakable`) [jar]. Ça pose un risque de dégâts sur les bases, à vérifier.

**Apparition.** Elle passe par `neoforge:add_spawns` avec des **identifiants de biomes vanilla écrits un par un** (`minecraft:savanna`, `jungle`, `desert`, `mangrove_swamp`, `beach`, `warm_ocean`…), plus un modificateur codé qui s'appuie sur la classe `Biomes` vanilla [jar]. Il n'utilise aucun tag. Les biomes de NML (bayou, prairie, tropical_beach…) ne reçoivent donc rien. Les biomes vanilla restent dans le monde avec NML, donc les animaux apparaissent quand même en savane et en jungle [déduit, d'après le rapport verif-2026-10-18/4]. Les poids sont élevés : crocodile 20 en savane, moho 25 en jungle.

**Configuration.** Config commune `ENABLE_<ESPÈCE>_SPAWNING` par espèce, un poids par habitat (0 coupe cet habitat), et une option client contre les secousses de caméra de l'éléphant [jar]. Ces options ne règlent que l'apparition naturelle : les entités, objets et recettes restent.

**Doublons avec le pack.**
| Animalia | Déjà couvert par | Choix déjà fait (plan 05 (`docs/plan/05-faune-et-ambiance.md`)) |
|---|---|---|
| Éléphant | Alex's Mobs, Naturalist | AM garde l'éléphant (caravane), celui de Naturalist est coupé |
| Rhinocéros | Naturalist, AM | Naturalist |
| Hippopotame | Naturalist | Naturalist |
| Crocodile | alligator de Naturalist, crocodile et caïman d'AM | alligator de Naturalist |
| Jaguar | aucun (AM a tigre et panthère des neiges, Naturalist a le lion) | nouveau |
| Moho | aucun | nouveau |

**Filtre des 5 questions.** Gameplay : faible. Intégration : faible (biomes en dur, pas de tags NML). 100 h : non. Mécanique : non, c'est du contenu (« ajoute des animaux »). Doublons : 4 sur 6.

**Recommandation : ne pas intégrer.** Il ne resterait que le jaguar et le moho, au prix d'un mod ARR très jeune, de 4 espèces à désactiver et d'objets en ivoire en trop. À revoir seulement si une future version apporte une espèce sans équivalent et une vraie mécanique. Le cas échéant : `animalia-exotica`, côté CS, `ENABLE_ELEPHANT/RHINOCEROS/HIPPO/CROCODILE_SPAWNING = false`, et retrait en KubeJS des recettes d'ivoire et des œufs d'apparition.

---

### 3.2 Fish'n'Ships (`fish-n-ships`, modId `fish_n_ships`)

**Versions et dépendances.** `mc1.21.1-1.1.0-neoforge` (2026-09-12) et 1.0.0 [Modrinth]. Dépend de GeckoLib `>= 4.7.4`, Hybrid API (`hapi`) `>= 1.1.0`, Kotlin for Forge (loader `[4,)`), NeoForge `>= 21.1.150` [jar]. **Tout est déjà dans le pack** : GeckoLib 4.9.3, Hybrid API 1.1.2, Kotlin for Forge 5.12.0. Même studio que Hybrid Aquatic (Hybrid Labs). Licence ARR, environ 23 700 téléchargements.

**Contenu** [Modrinth, jar] :
- **Canoë** : 3 places, ou 2 places et un coffre, ou 1 place et un double coffre.
- **Voilier** : une bannière sert de voile. Voile baissée, il avance seul, même sans passager. On peut ainsi envoyer des colis ou des animaux sur l'eau.
- **Radeau** : 4 places, ne se dirige pas, se tire en laisse. **Radeau de ravitaillement** : 66 piles, sans passager.
- **Dinghy** (1.1.0) : 5 places, ou 3 places et un double coffre. Dans le jar, seule la variante en bois flotté a une recette ; les autres bois ont un objet mais pas de recette [jar]. C'est sans doute un oubli de l'auteur [déduit].
- **Navire** : grand bateau où l'on peut marcher, rangement intégré. Il brûle du combustible de four (charbon, charbon de bois). Recette : cuivre, haut fourneau, bateau à coffre, fer et planches.
- Améliorations du navire : **brise-glace** (casse `hapi:breakable_ice`) et **chalut**. Le chalut pêche tout seul avec la table de butin **vanilla `FISHING`** [jar].
- Bateaux en bois carmin et biscornu ; toutes les variantes en bois flotté de Hybrid Aquatic.
- Bois pris en charge : les 10 bois vanilla et le bois flotté. **Pas de bois NML.** Every Compat 2.11.54 n'a pas de module Fish'n'Ships [jar].

**Hostilité.** Aucune. Pas de worldgen, pas de créature.

**Configuration.** Le jar ne contient aucun fichier de config [jar]. Le seul réglage possible passe par les recettes, en KubeJS.

**Intégration et doublons.**
- Pour le zoo et le transport d'animaux (plan 10 (`docs/plan/10-recentrage.md`), lune-amie (`docs/plan/lune-amie.md`)) : le radeau en laisse et le canoë transportent des animaux sur l'eau avant Create. C'est le chaînon « bateau » de la boucle zoo.
- Le voilier automatique ouvre une petite logistique de début de partie, entre le bateau vanilla et Create.
- **Recouvrement avec Create Aeronautics et Sable** : le navire est un véhicule tout fait, motorisé au charbon. Il fait concurrence aux bateaux que les joueurs construisent avec Aeronautics [déduit]. Personne n'a testé ces entités sur une sous-structure Sable (radeau tiré depuis un dirigeable, canoë posé sur un navire Sable).
- **Chalut** : il donne une pêche passive avec la table vanilla, qui contient des livres enchantés. Ça court-circuite la boucle « livres trouvés » d'Immersive Enchanting (0008 (`docs/decisions/0008-enchantement.md`)) et rend la pêche automatique [déduit].
- Hybrid Aquatic : la compatibilité est prévue par l'auteur (bois flotté) [Modrinth changelog].

**Filtre des 5 questions.** Gameplay : oui (traverser, transporter). Intégration : bonne (zoo, Hybrid Aquatic, aucune dépendance nouvelle). 100 h : moyen (outil de début et de milieu de partie, relayé ensuite par Aeronautics). Mécanique : oui (voilier autonome, radeau en laisse). Doublons : partiel, surtout le navire.

**Recommandation : à tester**, sur une branche `test/fish-n-ships`.
- Slug `fish-n-ships`, version `mc1.21.1-1.1.0-neoforge`, côté CS.
- À garder : canoë, voilier, radeau, radeau de ravitaillement, dinghy.
- À trancher en test, avec retrait de recette en KubeJS si besoin : le **navire** (doublon d'Aeronautics), le **chalut** (pêche passive ; ou bien filtrer la table `minecraft:gameplay/fishing` avec LootJS, déjà présent), et le **brise-glace**.
- Points de test : véhicules sur une sous-structure Sable ou près d'elle, transport d'un animal en radeau, comportement dans les rivières NML, conflit éventuel avec ItemPhysic et Camera Overhaul en canoë.

---

### 3.3 Sunken Spires (`sunken-spires`)

**Versions.** Deux fichiers 1.0 du 2025-10-19, compatibles 1.21 à 1.21.10, dont 1.21.1 : un datapack `Sunken_Spires_1.21.zip` et une **version mod `1.0+mod`** pour NeoForge, Forge, Fabric et Quilt [Modrinth]. La version mod est un datapack emballé dans un jar par Modrinth (« Generated by Modrinth »). Elle ne contient aucune classe et ses données sont identiques à celles du zip (`diff -r` vide) [jar]. `pack_format` 48, donc valable en 1.21.1. Pas de dépendance. Licence ARR, environ 790 000 téléchargements.
**Livraison.** Pas besoin de KubeJS : packwiz référence `sunken-spires` (fichier `1.0+mod`) comme un mod, côté CS (le serveur intégré du solo en a besoin). La voie `pack/kubejs/data/` (CONTRIBUTING) reste possible en recopiant les données, mais c'est inutile et la licence ARR s'y prête mal.

**Contenu.** 11 structures dans l'espace de noms `joshie`, chacune dans son propre structure set (espacement/séparation en chunks) [jar] :
| Structure | Espacement | Contenu notable |
|---|---|---|
| colossuem (sic) | 96/32 | coffres, mini-boss noyé équipé (casque unique) |
| conduit_ruins | 64/16 | conduit et anneaux de prismarine |
| floating_spikes | 80/24 | cercle d'anneaux |
| large_fossil | 180/32 | cage thoracique, conduit actif |
| ocean_temple | 96/24 | 4 coffres |
| prismarine_forge | 72/34 | 2 coffres, **gardien ancien**, débris antiques |
| prismarine_trial_chamber | 108/32 | **17 générateurs d'épreuve**, 18 coffres-forts, noyés, gardiens, gardiens anciens |
| small_fossil | 120/24 | archéologie |
| spires | 250/30 | archéologie, un coffre |
| sunken_city | 160/24 | 5 maisons, coffres |
| trident_shrine | 180/80 | un trident |

**Hostilité.** C'est le défaut majeur. **Les 11 structures** ont un `spawn_overrides` « monster » qui fait apparaître **gardiens et noyés** dans toute leur emprise (cité : poids 120/220, groupes de 3 à 5) [jar]. Des noyés, gardiens et gardiens anciens sont aussi placés dans les NBT [jar]. Le pack interdit les monstres vanilla, et le plan 02 (`docs/plan/02-exploration.md`) précise qu'« aucune structure ne produit de créature hostile » et que les chambres d'épreuves sont à désactiver. Pas d'horreur au sens strict : l'ambiance est « civilisation disparue », plutôt dans le ton du pack.

**Biomes.** Toutes les structures utilisent un seul tag, `#joshie:has_structure/conduit_ruins`. Il contient `#minecraft:is_ocean` plus des tags optionnels Terralith, BoP, BYG… [jar]. NML n'ajoute pas d'océan, donc les océans restent vanilla et le tag fonctionne [déduit, d'après le rapport verif-2026-10-18/4]. Hybrid Aquatic garde ses biomes coupés, donc aucun conflit.

**Configuration.** Il n'y a pas de config. Sparse Structures s'applique, car les sets sont en `random_spread` : avec un `spreadFactor` global de 3, la cité passe d'environ 160 à environ 480 chunks [déduit]. Pour couper une structure, le tag partagé ne suffit pas. Il faut surcharger dans `pack/kubejs/data/joshie/worldgen/structure/sunken_spires/<nom>.json` le champ `biomes` (tag vide) **et** `spawn_overrides` (supprimé). Il faut aussi retirer les entités placées dans les NBT, par le filet KubeJS ou In Control! prévu au plan 05 pour les monstres vanilla [déduit].

**Doublons.** Les monuments océaniques vanilla deviennent des « ruines sans gardiens » (plan 02). Trois structures de Sunken Spires en sont des variantes en prismarine (temple, forge, chambre d'épreuve). Les chambres d'épreuves vanilla doivent aussi être coupées. Hybrid Aquatic couvre la faune marine.

**Filtre des 5 questions.** Gameplay : faible (butin, archéologie). Intégration : moyenne (océans vanilla, archéologie en commun avec NML). 100 h : les ruines silencieuses le sont, les arènes non. Mécanique : non. Doublons : avec les monuments.

**Recommandation : ne pas intégrer en l'état.** Variante **à tester** si l'on veut des découvertes sous-marines silencieuses :
- slug `sunken-spires`, fichier `1.0+mod`, CS ;
- garder **conduit_ruins, floating_spikes, large_fossil, small_fossil, spires, trident_shrine**, et éventuellement sunken_city et ocean_temple, toutes sans `spawn_overrides` ;
- couper **colossuem, prismarine_forge, prismarine_trial_chamber** (boss, gardien ancien, générateurs d'épreuve) ;
- ce sont environ 11 fichiers de surcharge, plus le filet anti-hostiles. Le coût est réel pour un pack de 0,76 Mo.

---

### 3.4 Trek (`trek`) : réexamen

**Versions.** B0.6.2 (bêta), datapack et `B0.6.2+mod` (jar Modrinth sans code), un seul fichier pour 1.21 à 26.2, `pack_format` 48 à 107, sans overlay [Modrinth, jar]. Les mêmes JSON servent de 1.21.1 à 26.x : il faut vérifier au chargement que rien ne casse en 1.21.1 [déduit]. Licence MIT, 12,5 Mo.

**Contenu** [jar] :
- **113 structures de surface** dans 5 sets : very_common 16/8 (rochers, plaines), common 24/12 (13 structures), **medium 30/12 (76 structures)**, rare 48/24 (16), very_rare 96/48 (3). Exemples : camps, maisons, tours, forts, églises, cimetière, « haunted_house », autel fumant, dragon d'os, îles volantes, château, pyramide d'Anubis, cité engloutie, marchands ambulants.
- **7 villages Trek** (plaines, désert, savane, neige, taïga, marais, cerisaie) ajoutés au set `minecraft:villages`, qui est **remplacé** (espacement 48/20).
- **Surcharges vanilla** : avant-postes de pillards, manoirs, `stronghold` (« stronghold_big », tag `eye_of_ender_located`), villes de l'End. `monster_room` et `monster_room_deep` sont mis à `count 0` et remplacés par des structures Trek à générateurs, en sets d'espacement 1.
- Structures du Nether et de l'End.

**Hostilité.** Des `spawn_overrides` ajoutent pillards (11 structures), vindicateurs (6), vagabonds, husks, sorcière, illusionniste, squelette, slime. Les NBT placent villageois, zombies, ravageur, évocateur et 5 générateurs [jar]. Plusieurs structures sont typées pillards (`base_pillager`, `ship_pillager`, `pillager_mansion`…).

**Biomes et NML.** Les tags Trek sont des **listes explicites** de biomes vanilla, Terralith, BoP, Nature's Spirit et Regions Unexplored. Ils n'utilisent ni `#minecraft:is_forest` ni les tags `c:`, et **ne citent aucun biome NML** (0 occurrence) [jar]. Le wiki le confirme : sans compatibilité ajoutée à la main, « les structures Trek n'apparaîtront tout simplement pas dans les nouveaux biomes » [web]. Avec NML, Trek ne peuple que les biomes vanilla restants. Il n'y a pas de casse, mais la répartition est incohérente : rien dans les forêts NML, tout dans les plaines et savanes vanilla [déduit].

**Configurabilité.** Il n'y a pas de config. Les 113 structures se partagent 58 tags de biome, si bien que vider un tag coupe plusieurs structures [jar]. Pour garder une sélection, il faut surcharger les **5 fichiers `trek/worldgen/structure_set/overworld/*.json`** avec une liste blanche. Il faut aussi : rendre `minecraft:villages`, les avant-postes et les strongholds au plan 0004, rétablir ou supprimer les `monster_room`, et neutraliser les `spawn_overrides` des structures gardées. Sparse Structures agit sur ces sets `random_spread`. En revanche, les sets de salles à monstres d'espacement 1 restent denses [déduit].

**Conflits avec le pack.** Trek remplace le set des villages, alors que 0004 veut des villages en ruine en surchargeant les centres vanilla. Il surcharge le stronghold et l'End, que le pack supprime : il faudra s'assurer que les données KubeJS passent après celles de Trek [déduit]. Son ton (marchands, villages vivants, pillards, « haunted house ») va contre le concept « survivants » et l'absence d'hostiles.

**Recommandation : ne pas intégrer.** Le rejet de 0004 (volume) se confirme. Une sélection reste possible, mais il faudrait réécrire 5 sets, contrer 4 surcharges vanilla, réattribuer à la main les biomes NML et nettoyer les apparitions. Le coût est disproportionné pour 12,5 Mo de contenu sans mécanique. Si l'on cherche quelques petites ruines d'ambiance, NML et Ancient Remnants les fournissent déjà.

---

### Points non vérifiés
- Le comportement réel des véhicules Fish'n'Ships près des sous-structures Sable.
- Le chargement de Trek en 1.21.1, puisque le même JSON sert jusqu'à 26.2.
- La capacité d'In Control! ou de KubeJS à bloquer les entités placées par les NBT de structure (noyés, gardiens) : la piste est à confirmer en jeu.
- Le comportement de casse de blocs de l'éléphant et du rhinocéros d'Animalia (seuls les noms de classes et de tags l'indiquent).
