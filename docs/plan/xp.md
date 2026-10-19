# Supprimer l'XP : étude et plan de retrait

Idée 5 de Wayfarer ([recherche](recherche/2026-10-19/wayfarer.md), recommandation 5). L'équipe a demandé une étude avant tout retrait, puis a choisi de **retirer l'XP**.

> **État au 2026-10-19 : appliqué, pas encore testé en jeu.** Le plan de retrait est écrit dans `pack/` et dans le mod `fallow` 0.2.0, qui compile. Ce qui a été fait, ce qui s'écarte du plan et ce qui reste à vérifier sont dans [Application](#application-2026-10-19). L'étude qui suit vient de la lecture des jars du pack (`.cache/jars/`, `javap`), du code de Minecraft 1.21.1 patché par NeoForge 21.1.251 (cache Gradle du mod `fallow`) et de l'API Modrinth.

## Résumé

1. L'XP n'a plus que **quatre usages** dans Fallow : l'enclume, la copie et la transmutation des livres anciens d'Immersive Enchanting, et l'adresse d'une boîte aux lettres d'Envelope. L'enchantement lui-même se paie déjà en matériaux.
2. Trois se règlent par des données ou une config. **L'enclume, non** : Minecraft refuse de rendre l'objet si le joueur a moins de niveaux que le coût, et ce coût vaut toujours au moins 1. Il faut un petit mixin dans le mod `fallow`.
3. Couper l'XP se fait en un seul endroit : annuler l'apparition des orbes et tout gain de points (KubeJS et MoreJS). La barre se cache dans le même module du mod `fallow`. Aucun mod de Modrinth ne le fait proprement en français.
4. Le joueur perd une jauge et une monnaie invisible. Il perd aussi quelques bonus mineurs : la lune bleue (XP ×2), le sac en lapis, les pépites de Create et les 5 XP par espèce de Wildex. Aucun mod lu ne plante à 0 XP : tout joueur neuf commence à 0.
5. **Recommandation : retirer entièrement l'XP**, en une seule version, dès que le module `xp` du mod `fallow` est écrit et testé. Le retrait touche **16 fichiers indispensables**, plus 3 réglages facultatifs et 5 pages de doc.

## Comment l'étude a été faite

- Les 124 jars de `pack/mods/` (plus leurs jars imbriqués) ont été parcourus pour les symboles de l'XP : `ExperienceOrb`, `giveExperiencePoints`, `giveExperienceLevels`, `experienceLevel`, `xpReward`, `popExperience`, `getExpDrop`, `VanillaGuiLayers.EXPERIENCE_BAR`. 35 mods y font référence. Chaque cas utile a été ouvert avec `javap`.
- Les données des jars (recettes, tables de butin, succès, fichiers de langue) ont été lues pour `experience`, `experience_nugget` et `experience_bottle`.
- Le code de l'enclume, de la meule, de l'interface et du joueur vient des sources de Minecraft patchées par NeoForge 21.1.251.
- Les noms d'événements KubeJS, MoreJS et LootJS ont été lus dans leurs jars (KubeJS 2101.7.2-build.377, MoreJS 0.16.0, LootJS 3.7.0).

## Qui produit de l'XP

Une fois les orbes annulées et les gains bloqués (étape 2 du plan), **toutes ces sources s'éteignent d'un coup**. Le tableau dit seulement ce qui disparaît et s'il faut un réglage en plus.

| Source | Comment l'XP arrive | Ce que la suppression change | Remplacement ou réglage | Fichier ou clé |
|---|---|---|---|---|
| Minage (vanilla) | Orbes des minerais de charbon, diamant, émeraude, lapis, redstone, quartz, des blocs de sculk et des générateurs | Plus rien | Aucun | — |
| Fonte (vanilla) | Champ `experience` de chaque recette de four, de fumoir et de haut fourneau, rendu en orbes à la sortie | Plus rien | Aucun | — |
| Créatures tuées | `xpReward` et `LivingExperienceDropEvent` : Mowzie's Mobs (13 créatures), Alex's Mobs, Friends & Foes, Adorable Hamster Pets | Plus rien | Aucun | — |
| Élevage | Orbes à la reproduction : vanilla, Alex's Mobs (caïman, crocodile, ornithorynque, tortue), Naturalist (pondeurs), No Man's Land (tortue), Hybrid Aquatic (`hapi`), Friends & Foes (crabe) | Plus rien | Aucun | — |
| Pêche, commerce, meule | Orbes de la pêche, des échanges avec les villageois et du désenchantement à la meule | Plus rien | Aucun | — |
| Succès | Récompense `experience` : Icy's Better Horses (250, 100, 100), Exposure (`wildlife_archivist`, 150), succès « défi » vanilla | Plus rien ; le succès reste | Aucun | — |
| Fioles d'expérience | Trouvées dans le butin : Twigs, Critters and Companions (fouille du furet), Envelope (`valuables`), Hybrid Aquatic (caisse), Supplementaries (coffre-fort), coffres vanilla | La fiole lancée ne donne plus rien : un objet mort | Retirer la fiole de tous les butins | LootJS, `pack/kubejs/server_scripts/sans_xp.js` |
| **Create** 6.0.10 | **Pépite d'expérience** : 75 % de chance dans **160 recettes de broyage** (29 se chargent dans ce pack) (minerais et blocs bruts) ; la pépite s'utilise pour des orbes. **Bloc d'expérience** : 9 pépites. Perceuses et scies : orbes des blocs cassés (`BlockHelper.popExperience`). **Pas d'XP liquide** dans cette version (c'est Create: Enchantment Industry, absent du pack) | La pépite devient un objet qui se consomme pour rien | Retirer la pépite des 160 sorties de broyage, et les deux recettes du bloc | `ServerEvents.recipes`, `sans_xp.js` |
| Overgeared | Fonderie d'alliage, fonderie de moulage et fonderie du Nether : `giveExperiencePoints` et orbes à la sortie | Plus rien | Aucun | — |
| Farm & Charm | Le fourneau (`StoveBlockEntity`) rend des orbes ; l'effet « Reposé » dit « Augmente les points d'expérience gagnés » | Plus d'orbes. L'effet n'a aucun code (`RestedEffect` est vide) : seule sa description ment | Nouvelle description | Clé `effect.farm_and_charm.rested.description` dans `pack/kubejs/assets/farm_and_charm/lang/fr_fr.json` |
| Kaleidoscope Cookery | Aucune référence à l'XP | Rien | — | — |
| Exposure | Chambre noire : 2, 4 ou 5 points par photo tirée | Plus rien | Facultatif : mettre à 0 | `pack/defaultconfigs/exposure-server.toml`, `[lightroom]` `experience_black_and_white`, `experience_color`, `experience_chromatic` = 0 |
| Wildex | 5 points par nouvelle espèce (`newEntryRewardsXp = true`, `newEntryXpAmount = 5`) | Plus rien | Facultatif : couper ; la récompense viendra du script du zoo ([07](07-kubejs-et-datapack.md), ligne 18) | `pack/config/wildex/wildex-common.toml`, `[general]` `newEntryRewardsXp = false` |
| Envelope | Colis du service d'artisanat (`PACKAGE_EXPERIENCE`), rendus en orbes à l'ouverture | Plus rien | Aucun | — |
| Enhanced Celestials | Lune bleue : XP ×2 ; super lune bleue : ×4 (`xp_amplifier`) | Le bonus d'XP disparaît. La lune garde la chance (`minecraft:luck`) et la portée de balise ×1,5 | Aucun ; voir l'enclume plus bas | — |
| Traveler's Backpack | Sac en lapis : 15 % de chance de doubler l'XP ramassée | Capacité morte | Facultatif : la retirer de la liste | `pack/defaultconfigs/travelersbackpack-server.toml`, `[server.backpackAbilities]` `allowedAbilities`, sans `travelersbackpack:lapis` |
| Supplementaries | Robinet sur un bocal d'XP, Marchand rouge | Plus rien | Aucun | — |
| No Man's Land | Pots, grandes jarres, ancre (`getExpDrop`) | Plus rien | Aucun | — |
| Autres | Ghost, furet de Critters and Companions, pioche fantôme d'Alex's Mobs, Curios, Moonlight, commande `/xp` | Plus rien | Aucun | — |
| **Clumps** | Ce mod regroupe les orbes | Il ne sert plus à rien | **Le retirer du pack** | Supprimer `pack/mods/clumps.pw.toml` |

## Qui consomme de l'XP

| Source | Ce qui casse sans XP | Remplacement | Fichier, clé, valeur |
|---|---|---|---|
| **Enclume** (vanilla) : renommer, réparer, combiner | **Tout.** `AnvilMenu.mayPickup` demande `experienceLevel >= coût` et `coût > 0`. Renommer coûte au moins 1 niveau : à 0 niveau, on ne peut plus nommer une étiquette pour un animal, ni réparer | **Module `xp` du mod `fallow`** : un mixin sur `mayPickup` ignore les niveaux. L'enclume garde son coût en matériaux (lingots, objet sacrifié) et sa limite « Trop cher ! » (40), qui devient une **usure** : un objet réparé trop souvent ne passe plus à l'enclume, il faut la meule ou la forge d'Overgeared. Le libellé change : « Usure : %1$s » et « Trop usé ! ». Facultatif : refuser la fusion d'enchantements (événement `AnvilUpdateEvent`), pour que l'enchantement reste à la table | `mods-custom/fallow/…/mixin/xp/AnvilMenuMixin.java` ; clés `container.repair.cost` et `container.repair.expensive` dans `pack/kubejs/assets/minecraft/lang/fr_fr.json` |
| Meule (vanilla) | Rien : elle donne de l'XP, elle n'en prend pas | **À garder.** Overgeared s'en sert pour rendre de la durabilité (`grindingToggle`), sans XP | — |
| Table d'enchantement | Rien : Immersive Enchanting remplace le menu vanilla. Les 43 fichiers de coût des enchantements vanilla n'ont **aucun** `xp_levels` : on paie un matériau et du lapis | **À garder** | — |
| **Immersive Enchanting** : copier un livre ancien | `replicate.json` : livre et plume **+ 10 niveaux**. `Cost.test` exige `experienceLevel >= xp_levels` : plus personne ne copie. Or la recherche sur l'enchantement compte dessus pour transmettre un savoir à un ami | Livre et plume seul, `xp_levels` à 0. Le prix reste le carburant (1 lapis) et le fait d'avoir le livre rangé. On ne peut mettre qu'un objet par emplacement de coût | `pack/kubejs/data/immersiveenchanting/enchantment_costs/immersiveenchanting/replicate.json` : `{"levels": {"1": [{"item_cost": {"item": "minecraft:writable_book", "count": 1}}]}}` |
| **Immersive Enchanting** : transmuter un livre ancien | `transmute.json` : rien (`minecraft:air`) **+ 10 niveaux**. Même blocage | Un matériau à la place, par exemple 4 éclats d'améthyste (valeur de départ, à régler en jeu) | `…/immersiveenchanting/transmute.json` : `{"levels": {"1": [{"item_cost": {"item": "minecraft:amethyst_shard", "count": 4}}]}}` |
| **Envelope** : nommer ou renommer une boîte aux lettres | 3 niveaux par défaut (`address_experience_levels_cost`, de 0 à 128). Sans niveaux, plus d'adresse, donc plus de courrier | Mettre à 0. Le jeu teste alors « 0 niveau ou plus » : c'est toujours vrai, et l'écran n'affiche plus de coût (il ne le dessine que si le coût est positif) | `pack/defaultconfigs/envelope-server.toml` : `[mailbox]` `address_experience_levels_cost = 0` |
| Quark 4.1-485 | Rien. Les modules qui touchent à l'XP sont coupés : Matrix Enchanting (`oddities = false`), Ancient Tomes, Color Runes, Ancient Wood (le fruit qui donne de l'XP), Game Nerfs (`experimental = false`). Hotbar Changer se dessine seulement au-dessus de la barre | **À garder** | `pack/config/quark-common.toml`, rien à changer |
| Create 6.0.10 | Rien ne consomme l'XP. Pas d'XP liquide ni de bec verseur d'XP | Voir les producteurs (pépites) | — |
| Supplementaries 3.9.9 | « Mettre l'XP en fiole » sur une table d'enchantement (`[tweaks.bottle_xp]`) : **coupé par défaut** (`enabled = false`) et absent de notre config. Le canon cache déjà la barre quand on vise | **À garder** coupé | `pack/config/supplementaries-common.toml`, ne pas ajouter `bottle_xp` |
| Overgeared 1.6.19 | Rien. Les enclumes de forge, le martelage, la trempe, les plans et les paliers d'expertise n'utilisent pas l'XP | **À garder** | — |
| Ancient Remnants 1.5.2 (bénédictions) | Rien. Ni les classes ni les données du jar ne parlent d'XP | **À garder** | — |
| Traveler's Backpack | Rien ne consomme. Seul le sac en lapis gagne de l'XP (voir les producteurs) | **À garder** | — |
| Wildex 3.0.0 | Rien ne consomme. La fiche d'une créature affiche « XP : » pour ce qu'elle rapporte | **À garder** ; détail d'affichage | — |
| Exposure 1.9.19 | Rien ne consomme | **À garder** | — |
| Alex's Mobs : table de transmutation | Coûte des niveaux, mais sa recette demande une **étoile du Nether**, et le Nether est fermé | **À garder** : introuvable | — |
| Enhanced Celestials | La lune bleue divise par 2 le coût de l'enclume (par 4 pour la super lune bleue) | **À garder** : avec le module `xp`, elle **réduit l'usure** de l'enclume. Elle a encore un rôle | — |
| Raccommodage | Il répare avec les orbes | Déjà désactivé ([0008](../decisions/0008-enchantement.md)) | — |
| Mort | Le joueur perd ses niveaux et lâche des orbes | Plus rien à perdre. L'écran de mort affiche toujours « Score : 0 » | Facultatif : vider `deathScreen.score.value` dans `pack/kubejs/assets/minecraft/lang/fr_fr.json` |
| Commandes | `/xp`, `/enchant` (opérateurs) | `/xp add … points` est bloqué par le script ; `/xp add … levels` passe encore (événement `LevelChange`, non couvert par MoreJS) | Rien, sauf si l'on veut bloquer aussi les niveaux (voir l'étape 2) |
| Livres et guides | Le guide des hamsters parle d'« enclumes et livres enchantés » ; la page « Objectifs » du manuel d'Icy's Better Horses a une fiole d'expérience pour icône ; l'infobulle du sac en lapis parle d'expérience | Rien ne casse ; ce sont des textes | À reprendre seulement s'ils gênent en jeu |
| Cold Sweat, GeckoLib, Spatial GUI, ModernFix, Polytone, Steam 'n' Rails | Ils lisent ou déplacent la barre (mise en page de la barre d'action, rendu, couleur des orbes) | Rien ne casse : à 0, ces chemins sont ceux d'un joueur neuf | — |

## Comment supprimer l'XP proprement

### Empêcher l'XP d'arriver

Il n'existe **aucune règle de jeu** (gamerule) pour l'XP : `doMobLoot` couperait aussi le butin. Deux verrous suffisent, tous deux dans `pack/kubejs/server_scripts/sans_xp.js` :

1. **Annuler les orbes.** `EntityEvents.spawned('minecraft:experience_orb', event => event.cancel())`. L'événement de KubeJS 7 vient de `EntityJoinLevelEvent` ; il est ciblé par type et annulable (`hasResult`). Il ne s'exécute que pour les orbes : son coût est négligeable. Il couvre le minage, la fonte, les créatures, l'élevage, la pêche, la meule, les fioles, les pépites et les mods.
2. **Bloquer les gains directs.** Certains mods donnent l'XP sans orbe : Wildex, Overgeared, les succès, `/xp`. MoreJS relaie l'événement NeoForge `PlayerXpEvent.XpChange`, que `Player.giveExperiencePoints` envoie avant tout gain : `MoreJS.playerXpChange(event => { if (event.amount > 0) event.cancel() })`. Le groupe d'événements s'appelle `MoreJS` dans le jar ; c'est un événement serveur, annulable. On ne bloque **que** les gains positifs : une dépense (négative) doit rester possible.

Pour les niveaux donnés d'un coup (`/xp add … levels`), seul l'événement natif `PlayerXpEvent$LevelChange` les arrête : `NativeEvents.onEvent(...)` de KubeJS sait l'écouter. Ce n'est utile que contre les commandes : à garder en réserve.

### Cacher la barre

Le niveau reste à 0, donc le chiffre vert ne s'affiche jamais (`renderExperienceLevel` ne dessine que si le niveau est positif). Il reste la barre vide.

- **Choix retenu : le module `xp` du mod `fallow`.** Il annule les couches `VanillaGuiLayers.EXPERIENCE_BAR` et `EXPERIENCE_LEVEL` dans `RenderGuiLayerEvent.Pre`. La jauge de saut à cheval est une autre couche (`JUMP_METER`) : elle reste. Le module contient déjà le mixin de l'enclume, et le mod est installé des deux côtés.
- Repli sans Java : un script `pack/kubejs/client_scripts/` avec `NativeEvents.onEvent('net.neoforged.neoforge.client.event.RenderGuiLayerEvent$Pre', …)`. Il marche, mais il passe dans le JavaScript à chaque couche et à chaque image.
- Mods cherchés sur Modrinth pour NeoForge 1.21.1 (API `/project/<slug>/version`, le 2026-10-19). **Aucun ne convient**, et aucun n'a le français :

| Mod | Version | Langues | Pourquoi non |
|---|---|---|---|
| HideHUD (`hidehud`) | 1.3fix (2026-06-07) | Aucun fichier de langue ; touches nommées en anglais dans le code (« Toogle HideHUD ») | Il ne cache la barre que lorsque l'on a basculé l'interface (`ToggleHUDProcedure.isGuiHidden`) |
| HUD Manager (`hud-manager`) | 1.5.1 | Anglais, chinois | Pas de réglage pour la seule barre d'XP (« Hotbar Group ») |
| Hudless (`hudless`) | 2.0.6 | Anglais, japonais, russe | Cache l'interface selon des conditions, avec une animation ; licence CC-BY-ND |
| Fixed Anvil Repair Cost (`fixed-anvil-repair-cost`) | 3.6 | — | Pour l'enclume : « une valeur de 0 garde le comportement vanilla », donc il coûte toujours des niveaux ; licence ARR et dépend de Collective |

Effet visible : les cœurs et la faim restent à leur hauteur, et un petit vide reste à la place de la barre. On peut abaisser ces jauges (`Gui.leftHeight` et `rightHeight` de NeoForge) : c'est une finition, à essayer en jeu.

### Les niveaux existants

Les mondes 0.x sont jetables ([CONTRIBUTING](../../CONTRIBUTING.md)). Pour un monde de test qui a déjà de l'XP, le script remet tout à zéro à la connexion : `PlayerEvents.loggedIn(event => event.player.setXp(0))`. Dans KubeJS 7, `setXp` remet à 0 le total, la progression et le niveau, puis ajoute 0 point. Rien n'est à convertir : une fois l'enclume, Immersive Enchanting et Envelope réglés, un niveau ne sert plus à rien.

## Plan de retrait

Chaque étape dit les fichiers touchés. Les étapes 1 et 2 doivent sortir **dans la même version** : sans le module, l'enclume ne marche plus.

1. **Module `xp` du mod `fallow`** (version 0.2.0).
   - `mods-custom/fallow/src/main/java/io/github/salvagedisc/fallow/mixin/xp/AnvilMenuMixin.java` (nouveau) : dans `AnvilMenu.mayPickup`, ignorer `player.experienceLevel` quand le module est actif. Garder le test « coût > 0 » et la limite de 40. Le mixin touche du code vanilla. Il ne gêne pas celui d'Enhanced Celestials, qui vise `createResult` (calcul du coût).
   - `mods-custom/fallow/src/main/java/io/github/salvagedisc/fallow/xp/XpModule.java` (nouveau) : côté client, annuler les deux couches de la barre. Facultatif côté serveur : refuser dans `AnvilUpdateEvent` la fusion de deux objets enchantés, pour que l'enchantement passe par la table.
   - `mods-custom/fallow/src/main/resources/fallow.mixins.json` : ajouter `xp.AnvilMenuMixin` à la liste `mixins` (les deux côtés). Ce mixin ne vise aucun mod : rien à ajouter dans `FallowMixinPlugin`.
   - `mods-custom/fallow/src/main/java/io/github/salvagedisc/fallow/FallowModule.java` : entrée `XP("xp", null, …)`, sans mod requis.
   - `mods-custom/fallow/src/main/java/io/github/salvagedisc/fallow/FallowConfig.java` : `xp.enabled` (vrai), `xp.hideBar` (vrai), `xp.blockEnchantMerge` (à décider), dans `fallow-common.toml`.
   - `mods-custom/fallow/gradle.properties` : `mod_version=0.2.0`.
   - `pack/mods/fallow-0.1.0.jar` remplacé par `pack/mods/fallow-0.2.0.jar`.
2. **Couper les sources** : `pack/kubejs/server_scripts/sans_xp.js` (nouveau). Brouillon, à vérifier sur le serveur de test :
   ```js
   // Plus d'orbes d'expérience, plus de gain de points.
   EntityEvents.spawned('minecraft:experience_orb', event => event.cancel())
   MoreJS.playerXpChange(event => { if (event.amount > 0) event.cancel() })
   PlayerEvents.loggedIn(event => event.player.setXp(0))

   // Les pépites d'expérience de Create ne sortent plus du broyage.
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

   // Plus de fioles d'expérience dans les coffres et les butins.
   LootJS.modifiers(event => {
     event.addTableModifier(/.*/).removeLoot('minecraft:experience_bottle')
   })
   ```
   La partie Create recopie chaque recette sans la pépite. Vérifier en jeu que les 29 recettes gardent leurs autres sorties et leur durée. Si la recopie échoue, le repli est de laisser la pépite et de la cacher dans EMI ; elle resterait un objet inutile.
3. **Remplacer les consommateurs.**
   - `pack/kubejs/data/immersiveenchanting/enchantment_costs/immersiveenchanting/replicate.json` (nouveau) : livre et plume, sans niveaux.
   - `pack/kubejs/data/immersiveenchanting/enchantment_costs/immersiveenchanting/transmute.json` (nouveau) : 4 éclats d'améthyste, sans niveaux.
   - `pack/defaultconfigs/envelope-server.toml` : `[mailbox]` `address_experience_levels_cost = 0`. Si l'agent d'Envelope a déjà créé ce fichier, on ajoute seulement la clé.
4. **Textes** (passés au coordinateur dans `wayfarer-impl/xp-lang.json`, jamais écrits directement dans `pack/kubejs/assets/`) :
   - `pack/kubejs/assets/minecraft/lang/fr_fr.json` et `en_us.json` (nouveaux) : `container.repair.cost` → « Usure : %1$s » ; `container.repair.expensive` → « Trop usé ! » ; facultatif : `deathScreen.score.value` vide.
   - `pack/kubejs/assets/farm_and_charm/lang/fr_fr.json` (existe déjà) : `effect.farm_and_charm.rested.description` → par exemple « Un bon repos. » (l'effet n'a aucun code).
   - Les libellés de config du module `xp`, si le menu de config de NeoForge les affiche.
5. **Retirer Clumps** : supprimer `pack/mods/clumps.pw.toml`, puis `packwiz refresh` (met à jour `pack/index.toml`) et `npm run check-deps`.
6. **Réglages facultatifs**, propres mais couverts par l'étape 2 :
   - `pack/config/wildex/wildex-common.toml` : `[general]` `newEntryRewardsXp = false` (le fichier vit dans le sous-dossier `wildex/`) ;
   - `pack/defaultconfigs/exposure-server.toml` : `[lightroom]` les trois `experience_*` à 0 ;
   - `pack/defaultconfigs/travelersbackpack-server.toml` : `[server.backpackAbilities]` `allowedAbilities` sans `travelersbackpack:lapis`.
7. **Documentation** : `docs/mods.md` (Clumps sort, le mod `fallow` gagne le module `xp`), `docs/plan/mod-fallow.md` (module `xp` et ses tests), `CHANGELOG.md`, une décision `docs/decisions/0009-xp.md`, et l'état de cette page.
8. **Tests en jeu**, à deux :
   - miner du charbon, fondre du fer, tuer une créature, élever deux vaches : aucune orbe ;
   - ouvrir un coffre de ruine : pas de fiole d'expérience ;
   - nommer une étiquette et réparer une pioche à l'enclume, à 0 niveau ; voir « Usure » monter jusqu'à « Trop usé ! » ;
   - copier et transmuter un livre ancien ;
   - nommer une boîte aux lettres d'Envelope ;
   - broyer du minerai de fer avec Create : pas de pépite, les autres sorties sont là ;
   - découvrir une espèce dans Wildex, tirer une photo, obtenir un succès d'Icy's Better Horses : la barre ne bouge pas ;
   - vérifier que la barre a disparu, à pied, à cheval (la jauge de saut reste) et en visant au canon de Supplementaries.

Bilan : **16 fichiers indispensables** (7 dans `pack/` avec `index.toml`, 3 de langue, 6 dans le mod `fallow`), 3 réglages facultatifs, 5 pages de doc.

## Ce que le joueur gagne et perd

**Il gagne :**
- un écran plus calme : une jauge de moins, sous des jauges qui comptent déjà (température, soif, saisons) ;
- un monde moins « jeu vidéo » : on ne tue plus et on ne mine plus pour une barre verte ; miner sert à miner, fondre sert à fondre ;
- un savoir qui pèse : l'enchantement tient tout entier dans les livres trouvés et les matériaux du monde, comme le veut la [décision 0008](../decisions/0008-enchantement.md) ;
- une enclume plus simple : on renomme et on répare avec du métal, sans calcul de niveaux. Son « usure » donne une durée de vie aux objets et renvoie vers la forge d'Overgeared.

**Il perd :**
- la réparation « gratuite » à l'enclume pour qui avait des niveaux : elle reste limitée par l'usure ;
- le bonus d'XP de la lune bleue (elle garde la chance et, grâce au module, allège l'usure) ;
- la capacité du sac en lapis ;
- les pépites et le bloc d'expérience de Create (le bloc était une décoration lumineuse) ;
- les 5 XP par espèce de Wildex, qui seront remplacés par les récompenses du zoo ;
- le petit plaisir des orbes qui tintent. C'est un son connu du jeu : son absence se remarquera les premières heures.

## Risques

- **Le mixin de l'enclume** est le seul vrai risque. Il touche du code vanilla et doit tourner des deux côtés : le client refuse aussi de prendre l'objet s'il croit que le joueur n'a pas assez de niveaux. Client et serveur doivent donc avoir la même version du mod `fallow`. Aucun mod du pack ne patche `mayPickup`. Enhanced Celestials patche `createResult`, une autre méthode. Easy Anvils est déjà écarté.
- **Les noms de l'API.** `EntityEvents.spawned`, `MoreJS.playerXpChange`, `PlayerEvents.loggedIn`, `findRecipes` et `LootJS.modifiers` sont lus dans les jars, pas encore essayés. Le format de la recette recopiée par `event.custom` est la partie la plus fragile.
- **Les mods qui plantent à 0 XP** : aucun trouvé. Tout joueur neuf est à 0 niveau, donc ces chemins sont les plus joués. Le calcul `getXpNeededForNextLevel` vaut 7 au niveau 0, jamais 0 : pas de division par zéro.
- **Les fermes** : une ferme à XP ne sert plus à rien, ce qui va dans le sens de Fallow. Les fermes à butin (laine, viande, cultures) ne changent pas. Le seul vrai producteur automatique était le broyage de Create.
- **Un mod ajouté plus tard** qui paie en niveaux (enchantement, réparation, téléportation) deviendrait inutilisable. Le filtre d'ajout doit poser la question : « paie-t-il en XP ? ».
- **Revenir en arrière** est facile : supprimer `sans_xp.js` et passer `xp.enabled = false`. Les niveaux reviennent avec le jeu, rien n'est perdu dans le monde.
- **Faille déjà présente, sans lien avec l'XP** : l'enclume accepte un livre ancien d'Immersive Enchanting comme livre (elle teste le composant `stored_enchantments`), mais elle lit les enchantements par `EnchantmentHelper.getEnchantmentsForCrafting`, qui ne lit ce composant que sur le livre enchanté vanilla. L'enclume ne devrait donc rien appliquer. C'est déduit du code : à vérifier en jeu, avec ou sans XP.

## Recommandation

**Retirer entièrement l'XP** : orbes, gains, barre, et les quatre consommateurs remplacés.

- « Retirer seulement la barre et les orbes » ne tient pas. Les gains directs (succès, Wildex, Overgeared) rempliraient une jauge invisible. L'enclume et Envelope marcheraient ou non, sans que le joueur sache pourquoi.
- « Garder » ne coûte rien, mais l'XP n'a plus de rôle : l'enchantement est déjà payé en matériaux. Elle reste une récompense de grind, contraire à l'exploration voulue par la [vision](../vision.md).
- Le coût réel est faible : un module de deux classes dans le mod `fallow`, un script, deux fichiers de données et une ligne de config. Le seul point délicat, l'enclume, en sort amélioré.

Ordre conseillé : écrire et tester le module `xp`, puis sortir les étapes 1 à 5 dans la même version 0.x.

### Décisions de l'équipe

1. L'enclume **peut toujours fusionner** deux objets enchantés : `xp.blockEnchantMerge = false` (le réglage existe, coupé). La limite « Trop cher ! » (40) devient une usure.
2. La transmutation d'un livre ancien coûte **4 éclats d'améthyste**.
3. Le coût de l'enclume s'appelle « **Usure : %1$s** », sa limite « **Trop usé !** ».

## Application (2026-10-19)

### Fichiers écrits

| Étape | Fichier | Contenu |
|---|---|---|
| 1 | `mods-custom/fallow/src/main/java/io/github/salvagedisc/fallow/mixin/xp/AnvilMenuMixin.java` | `@ModifyExpressionValue` (MixinExtras) sur la lecture de `player.experienceLevel` dans `AnvilMenu.mayPickup` : vaut `Integer.MAX_VALUE` quand `xp.enabled`. Le test « coût > 0 » et la limite de 40 (dans `createResult`) ne bougent pas |
| 1 | `…/fallow/xp/XpModule.java`, `…/fallow/xp/XpClient.java` | `AnvilUpdateEvent` annulé si `xp.blockEnchantMerge` et que l'objet de droite porte des enchantements ; chez le client, `RenderGuiLayerEvent.Pre` annulé pour `EXPERIENCE_BAR` et `EXPERIENCE_LEVEL` si `xp.hideBar` |
| 1 | `FallowModule.java`, `FallowConfig.java`, `Fallow.java`, `fallow.mixins.json`, `neoforge.mods.toml`, `gradle.properties` | Module `XP("xp", null, …)` sans mod requis ; `[xp]` `enabled = true`, `hideBar = true`, `blockEnchantMerge = false` dans `fallow-common.toml` ; `xp.AnvilMenuMixin` dans `mixins` (les deux côtés) ; version 0.2.0 |
| 1 | `pack/mods/fallow-0.2.0.jar` (remplace `fallow-0.1.0.jar`) | Jar de `./gradlew build`. Son entrée de `pack/index.toml` (nom et sha256) est mise à jour à la main |
| 2 | `pack/kubejs/server_scripts/sans_xp.js` | Orbes annulées, gains positifs bloqués, XP remise à 0 à la connexion, pépites retirées du broyage de Create, recettes du bloc d'expérience retirées, fioles retirées de toutes les tables de butin |
| 3 | `pack/kubejs/data/immersiveenchanting/enchantment_costs/immersiveenchanting/replicate.json`, `transmute.json` | Livre et plume sans niveaux ; 4 éclats d'améthyste sans niveaux (`xp_levels` vaut 0 par défaut dans le codec `Cost`) |
| 3 | `pack/defaultconfigs/envelope-server.toml` | `[mailbox]` `address_experience_levels_cost = 0` (clé lue dans `Config$Server` d'Envelope 0.7.5, config `SERVER`) |
| 4 | `wayfarer-impl/xp-lang.json` (fusionné par le coordinateur) | `container.repair.cost`, `container.repair.expensive`, `effect.farm_and_charm.rested.description`, en français et en anglais. Les clés de langue sont globales : les deux premières vont dans `pack/kubejs/assets/minecraft/lang/`, la troisième dans `pack/kubejs/assets/farm_and_charm/lang/` |
| 6 | `pack/config/wildex/wildex-common.toml` (nouveau, partiel) | `[general]` `newEntryRewardsXp = false` |
| 6 | `pack/defaultconfigs/exposure-server.toml` (nouveau, partiel) | `[lightroom]` les trois `experience_*` à 0 |
| 6 | `pack/defaultconfigs/travelersbackpack-server.toml` (nouveau, partiel) | `[server.backpackAbilities]` `allowedAbilities` : la liste par défaut de la 10.1.39, sans `travelersbackpack:lapis` |

Les fichiers de config partiels suivent l'usage du pack (`supplementaries-common.toml`, `nomansland-common.toml`) : NeoForge ajoute les clés absentes au premier lancement.

### Écarts au plan

- **Pépites de Create** : le script parcourt toutes les recettes `create:crushing` (`forEachRecipe`) et lit leur JSON, au lieu du filtre `output`, dont la prise en charge par le schéma de KubeJS Create n'est pas sûre. Il retire d'abord chaque recette, puis la recrée sous le même identifiant sans la pépite et sans `neoforge:conditions` (KubeJS 7 range les recettes ajoutées après les originales : l'identifiant ne se perd pas). Une recette qui n'aurait que la pépite serait seulement retirée ; Create 6.0.10 n'en a aucune (160 recettes lues, toutes avec une autre sortie).
- **Fioles** : retirées des tables elles-mêmes (`LootJS.lootTables`, `forEachTable`, `removeItem`), comme le fait `tannerie_butin.js`, au lieu d'un modificateur appliqué à chaque tirage.
- **Libellé de l'enclume** : il passe par les clés vanilla, donc il reste « Usure » même avec `xp.enabled = false`. Le mod `fallow` n'a pas de fichier de langue (pas d'écran de config enregistré) : rien à traduire dans le mod.
- **Écran de mort** : `deathScreen.score.value` n'est pas vidé. Une valeur sans `%s` ferait échouer `npm run check-fr` (contrôle des variables) ; « Score : 0 » reste.
- **Fait par le coordinateur** : Clumps retiré et `packwiz refresh` lancé (étape 5). **Reste** : `docs/mods.md`, `CHANGELOG.md` et la décision `docs/decisions/0009-xp.md` (étape 7).

### Reste à tester en jeu

Sur le serveur de test, puis à deux (liste de l'étape 8) :

1. **Démarrage** : log « Module xp chargé (aucun mod requis) », aucun avertissement de mixin `fallow` (`AnvilMenuMixin` doit trouver sa cible) ; log KubeJS sans erreur dans `sans_xp.js`, avec « [sans_xp] 29 recettes de broyage recopiées sans pépite d'expérience » (voir [Serveur de test](#serveur-de-test-2026-10-19)).
2. **Enclume à 0 niveau** : nommer une étiquette, réparer une pioche avec des lingots, fusionner deux pioches enchantées. Le coût s'affiche en vert (« Usure : n ») et l'objet se prend ; à 40, « Trop usé ! ». Avec `xp.enabled = false`, l'enclume redemande des niveaux. Avec `xp.blockEnchantMerge = true`, un livre enchanté à droite ne donne plus de résultat.
3. **Sources coupées** : miner du charbon, fondre du fer, tuer une créature, élever deux vaches, pêcher : aucune orbe. `/xp add @s 10 points` : rien ne change. Wildex, chambre noire d'Exposure, succès d'Icy's Better Horses : la barre ne bouge pas.
4. **Create** : broyer du minerai de fer brut ; pas de pépite, les autres sorties et la durée sont là ; EMI ne montre plus la pépite dans le broyage, ni de recette pour le bloc d'expérience. Point le plus fragile : la recopie par `event.custom`.
5. **Butin** : coffres de ruine, caisse d'Hybrid Aquatic, coffre-fort de Supplementaries, colis `valuables` d'Envelope, fouille du furet : pas de fiole d'expérience. Une fiole venue d'ailleurs (marchand) ne donne rien.
6. **Immersive Enchanting** : copier un livre ancien (livre et plume, sans niveaux) ; le transmuter avec 4 éclats d'améthyste dans l'emplacement de coût.
7. **Envelope** : nommer une boîte aux lettres dans un monde neuf (`serverconfig/envelope-server.toml` copié depuis `defaultconfigs`), sans coût affiché.
8. **Interface** : plus de barre ni de chiffre, à pied, à cheval (la jauge de saut reste) et en visant au canon de Supplementaries. Regarder le petit vide sous les cœurs et la faim (finition possible : abaisser `Gui.leftHeight` et `rightHeight`).
9. **Monde de test avec de l'XP** : à la connexion, niveaux remis à 0.
10. **Sac en lapis** : plus de capacité (`allowedAbilities` lu depuis le monde neuf).

### Serveur de test (2026-10-19)

Serveur dédié sans joueur (NeoForge 21.1.251, monde neuf, 98 jars, Clumps retiré), lancé par `boot.sh --fresh --keep` puis arrêté. Ce qu'on peut vérifier sans joueur :

| Vérification | Résultat |
|---|---|
| Démarrage | « Done » en 17 s. « Module xp chargé (aucun mod requis) », puis « xp : enclume sans niveaux… ». Aucun avertissement de mixin `fallow`. `AnvilMenu` est chargé dès le démarrage (`MenuType.ANVIL`) : avec `required` et `defaultRequire: 1`, un `AnvilMenuMixin` raté aurait fait planter le serveur |
| Scripts KubeJS | `startup.log` : 2/2 scripts, 0 erreur. `server.log` : 6/6 scripts, 0 erreur, 0 avertissement ; « Reloaded with no KubeJS errors » après `/reload`. Les 81 avertissements de `server.log` sont des recettes d'autres mods qu'il ne sait pas lire (« Falling back to vanilla »), sans lien avec `sans_xp.js` |
| Broyage de Create | « 29 recettes de broyage recopiées », « 0 failed recipes ». **29 et non 160** : les 131 autres recettes à pépite de Create 6.0.10 portent une condition `neoforge:tag_empty` (minerais d'autres mods) et ne se chargent pas dans ce pack. L'export (`/kubejs export debug`) montre `raw_iron` avec la seule sortie `create:crushed_raw_iron` et la même durée (400). Pour `deepslate_gold_ore` et `nether_quartz_ore`, les sorties et la durée sont celles d'origine, moins la pépite. Aucune recette exportée ne contient `experience_nugget`. Les deux recettes du bloc d'expérience sont retirées |
| Orbes | `summon minecraft:experience_orb … {Value:10}` répond « Summoned », mais aucune orbe n'existe ensuite (`execute if entity @e[type=experience_orb]` : échec). Pareil pour une fiole d'expérience lancée par `summon` |
| Fioles dans les butins | 40 tirages chacun de `minecraft:chests/shipwreck_treasure`, `crittersandcompanions:gameplay/digging/common` et `envelope:packages/lost_mail/valuables` (`loot spawn`) : 687 objets dont 92 émeraudes, **aucune** fiole d'expérience |
| Configs | `envelope-server.toml` : `address_experience_levels_cost = 0` ; `exposure-server.toml` : les trois `experience_*` à 0 ; `travelersbackpack-server.toml` : `allowedAbilities` sans `lapis` ; `wildex/wildex-common.toml` : `newEntryRewardsXp = false`. NeoForge a complété les fichiers partiels (avertissements « Incorrect key », attendus) sans toucher aux valeurs écrites. Dans ce serveur, NeoForge 21.1 range les configs serveur dans `config/`, non dans `world/serverconfig/` |
| `/xp add @a 10 points` | « No player was found » : à refaire avec un joueur |

**Reste à tester avec un joueur** : l'enclume à 0 niveau (renommer, réparer, fusionner, « Usure », « Trop usé ! » à 40) ; `/xp add … points` et les gains directs (succès, Wildex, chambre noire) ; la remise à 0 à la connexion ; une créature tuée par un joueur ; la copie et la transmutation d'Immersive Enchanting (les fichiers suivent le codec du jar, mais le mod n'écrit rien au chargement) ; l'adresse d'une boîte Envelope ; la barre cachée chez le client.
