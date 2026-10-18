# 6 — Un gardien pour les monuments d'Ancient Remnants: Monoliths

Vérification du 2026-10-18. Périmètre : remplacer les spawners de `minecraft:breeze` d'Ancient Remnants 1.5.2 (AR) par un gardien conforme à Fallow (décision 0003 : hostiles rares et territoriaux, pas d'horreur).

Méthode :
- jar AR 1.5.2 déjà extrait (`scratchpad/jars/ar/x`) : les 41 fichiers `.nbt` ont été lus avec un petit lecteur NBT écrit pour l'occasion (Python `-I`) ;
- jars du pack (`origin/main`, extraits dans `scratchpad/jars/c-*`) inspectés au `javap` : Mowzie's Mobs 1.8.2, Companions! 1.3.4, Friends & Foes 4.0.27, Alex's Mobs Continued 2.2.2, Hybrid Aquatic 1.7.3, Ghost 1.3.4, Quark, NML 1.5.12, KubeJS 2101.7, In Control ;
- API Modrinth (recherche NeoForge 1.21.1, environ 60 requêtes) ; jars téléchargés et ouverts pour Bosses of Mass Destruction, Legendary Monsters, EEEAB's Mobs [Unofficial], LAir's Bosses et The Gadyn.

Rien n'a été modifié dans le dépôt.

---

## Résumé

- **Il y a un spawner de breeze par monument, sauf au monolithe sentinelle.** Il est placé dans un couloir ou sur un socle, à 9 à 18 blocs de l'Elderheart. Au monolithe chasseur, le couloir ne mesure que 2 blocs de haut, et les salles des Elderhearts sont étroites : les boss doivent être posés au sol, à l'extérieur.
- **L'Elderheart peut être « scellé ».** S'il n'a plus de clé NBT `Blessing`, il ne donne rien (bytecode de `tick()`). Un script peut donc retirer la bénédiction, puis la rendre quand le gardien tombe. C'est ce qui donne un sens au gardien : sans cela, on passe à côté de lui et la bénédiction arrive à 16 blocs.
- **Il n'est pas nécessaire d'ajouter un mod.** Mowzie's Mobs et Companions!, déjà dans le pack, ont des gardiens endormis tout à fait dans le ton : une armure de héros qui s'éveille quand on la frappe, un géant de glace endormi, un sculpteur-moine qui met à l'épreuve, un pontife de mousse qu'on réveille de la main.
- **La meilleure méthode est un script KubeJS**, qui prolonge le polling déjà prévu pour la clairière (rapport 4, §2.3). Il fait apparaître le gardien une seule fois, scelle l'Elderheart et le libère à la mort du gardien. Il ouvre alors la clairière et donne la page du codex. Il faut en plus une petite surcharge de datapack qui retire les spawners.
- **Si l'on veut vraiment un nouveau mod**, le premier choix est **Bosses of Mass Destruction** (Forge/NeoForge 1.3.3, 1,95 Mo), dont on ne garderait que l'**Obsidilith** : un monolithe d'obsidienne vivant, ses quatre structures étant désactivées par tags. C'est une option, pas un besoin.

---

## 1. Ce qui est confirmé

### 1.1 Où sont les spawners et l'Elderheart [`.nbt` d'AR 1.5.2]

Les six structures sont des jigsaws. Leurs pièces utilisent deux processor_list partagées : `ancient_remnants:monolith` pour les trois monolithes, `ancient_remnants:monument` pour les trois monuments. Chacune ne contient qu'un `block_ignore` (air, structure_block). Chaque pool contient une seule pièce, et aucune n'a de `spawn_overrides`.

| Monument | Pièce qui contient le spawner | Spawner (pos. dans la pièce) | Elderheart (pièce, pos.) | Écart |
|---|---|---|---|---|
| hunter_monolith | `hunter_monolith/middle` | (5, 18, 2) | même pièce, (5, 18, 15) | 13 blocs |
| warrior_monolith | `warrior_monolith/middle` | (5, 42, 8), `Delay 601` | même pièce, (7, 37, 16) | environ 9,6 blocs |
| sentinel_monolith | **aucun spawner** | — | `sentinel_monolith/middle`, (6, 42, 16) | — |
| cherry_monument | `cherry_monument/top_3` | (4, 11, 29) | même pièce, (18, 21, 29) | environ 17 blocs |
| oakwood_monument | `oakwood_monument/top_0` | (8, 13, 1) | même pièce, (21, 25, 1) | environ 17,7 blocs |
| coniferous_monument | `coniferous_monument/bottom_1` | (24, 36, 14) | autre pièce : `top_3`, (23, 28, 29) | sur une autre pièce |

- **NBT de chaque spawner** : `SpawnData.entity.id = minecraft:breeze`, `SpawnCount 4`, `MaxNearbyEntities 6`, `RequiredPlayerRange 16`, `MinSpawnDelay 200`, `MaxSpawnDelay 800`, `SpawnRange 4`, `SpawnPotentials []`.
- **Volume libre** autour du spawner du monolithe chasseur : un couloir de 2 blocs de haut, entre le sol (`cracked_deepslate_tiles`) et le plafond (vue en coupe du `.nbt`).
- **Les spawners des monuments** sont posés sur un socle de `chiseled_tuff_bricks`.
- **Les salles des Elderhearts** ont 11 à 23 blocs de hauteur libre, mais seulement 3 à 9 blocs de large à hauteur d'homme (calcul sur le `.nbt`).
- **Conséquence : un boss ne tient ni à la place du spawner, ni au cœur de la salle.** Il faut le poser au sol, au pied ou à l'entrée du monument [déduit].
- **Les monolithes flottent** (`start_height absolute 20`, projeté sur `WORLD_SURFACE_WG`). Les monuments partent de `absolute -24`.
- **Le lore du jar (lang `en_us`)** donne le ton de chaque lieu :
  - chasseur : tribus de chasseurs du désert, agilité et précision ;
  - guerrier : force des héros, présent dans les mythes ;
  - sentinelle : des moines qui fuyaient les combats inutiles ;
  - cerisier : un don de jumeaux divins, des sources de vie, la guérison ;
  - chêne : des manuscrits elfiques, la fertilité ;
  - conifères : un temple bâti par les nains de la taïga et des montagnes, l'architecture et la mine.

### 1.2 L'Elderheart se laisse sceller [bytecode `Elderheart` d'AR 1.5.2]

- `tick()` : `if (tickCount % 100 == 0 && blessing != null)`, puis il cherche `getNearestPlayer(this, 16.0)` et appelle `setBlessing`. **Si `blessing` vaut `null`, il ne donne rien.**
- `readAdditionalSaveData` lit `PossibleBlessings` (un tag) et `Blessing`. `CompoundTagAdapter.readValue` renvoie `null` si la clé est absente (`contains`, sinon `aconst_null`).
- La bénédiction est tirée **une fois**, dans `finalizeSpawn`, que le mixin `MixinStructureTemplate` appelle à la pose de la structure. Elle est ensuite stockée dans le NBT `Blessing`.
- KubeJS 2101.7 expose `entity.nbt` en lecture et en écriture (`kjs$getNbt` / `kjs$setNbt`, ce qui appelle `load`), `persistentData`, `EntityEvents.spawned` et `EntityEvents.death` [jar KubeJS : `EntityKJS`, `EntityEvents`].
- **Donc** : retirer `Blessing` scelle l'Elderheart, et la remettre le libère. `PossibleBlessings` (`ancient_remnants:<monument>`) dit au script de quel type de monument il s'agit.

### 1.3 Les candidats dans le pack actuel

Statut dans Fallow (`origin/main`) : aucun de ces mobs n'est dans `bannedVanilla` (qui ne liste que du vanilla). Aucune config de ces mods n'est dans `pack/config` (seuls `quark-common.toml` et `supplementaries-common.toml` y figurent). Les seules règles existantes sont les brouillons In Control (`plan/kubejs-brouillons/config/incontrol/spawn.json`) :
- Mowzie's : `minlight 8` → refus, `maxdist 40` → refus, au plus 2 par joueur ;
- Guster d'Alex's Mobs : `minlight 8` → refus, au plus 3 ;
- Mauler : 60 % refusé.

**Ces règles ne s'appliquent pas à une invocation par commande.** In Control lit `"when"`, qui vaut `POSITION` par défaut (`SpawnRule` du jar In Control). Le cas `POSITION` correspond à l'événement `MobSpawnEvent.PositionCheck`, que le spawn naturel et les spawners déclenchent, mais pas `/summon` [déduit du code d'In Control et de NeoForge].

#### Les gardiens retenus

**Ferrous Wroughtnaut** (Mowzie's, `mowziesmobs:ferrous_wroughtnaut`)
- **Allure et ton** : une armure de fer géante qui tient une hache. Elle reste immobile tant qu'on ne la frappe pas, puis revient à sa place. Ce n'est pas un monstre : c'est le gardien d'une salle ancienne.
- **Boss** : oui. Clé `ferrous_wroughtnaut.has_boss_bar` ; aussi `heals_out_of_battle` et `reset_health_when_respawn` [config du jar]. 40 PV, 30 dégâts [`createAttributes`]. Il n'est vulnérable que dans certaines phases [wiki Mowzie's].
- **Dans Fallow** : présent, aucune config. Mob « anomalie » de 0003.
- **Comment il apparaît** : par une structure `wrought_chamber`, qui contient l'entité dans le `.nbt` (`spacing 15`, `separation 5`, tag `has_mowzie_structure` = `#c:is_overworld`). On peut aussi utiliser `/summon`.
- **Lié à un seul monument** : oui.
  - Il revient à sa place grâce au NBT `restPos`, et il est en sommeil tant que `active` est faux (`addAdditionalSaveData`).
  - `requiresCustomPersistence()` renvoie `true` : il ne disparaît jamais.

**Frostmaw** (Mowzie's, `mowziesmobs:frostmaw`)
- **Allure et ton** : un grand yéti des neiges, qui dort jusqu'à ce qu'on le dérange. C'est une bête de légende, pas un monstre d'horreur.
- **Boss** : oui. Clé `frostmaw.has_boss_bar` ; aussi `stealable_ice_crystal`. 250 PV, 10 dégâts, portée de suivi de 50 blocs.
- **Dans Fallow** : présent, aucune config. Le plan 05 propose un multiplicateur ×0,2.
- **Comment il apparaît** : par la structure `frostmaw_spawn`, dont le `.nbt` contient `{"id":"mowziesmobs:frostmaw","has_crystal":1}`, ou par `/summon`.
- **Lié à un seul monument** : oui, avec une réserve.
  - `requiresCustomPersistence()` renvoie `getHasCrystal()` : il ne reste persistant **que s'il a son cristal**.
  - Il faut donc l'invoquer avec `PersistenceRequired:1b`.

**Tongbi, le Sculpteur** (Mowzie's, `mowziesmobs:sculptor`)
- **Allure et ton** : un moine-sculpteur de pierre. Contre un objet, il propose une épreuve : grimper en un temps limité un parcours de rochers qu'il fait surgir. Il ne se bat que si on l'attaque. Il est énigmatique et ancien, et c'est une épreuve, pas un combat.
- **Boss** : oui (`has_boss_bar`), avec une minuterie d'épreuve. 140 PV.
- **Réglages** [config du jar] :
  - `test_height` : 60 par défaut, de 1 à 500 ;
  - `test_time_limit` : 400 s ;
  - `trade_which_item` et `trade_how_many` (1 à 64) ;
  - `disappear_after_reward` : il disparaît après la récompense ;
  - `heals_out_of_battle`.
- **Récompense** : la table `mowziesmobs:entities/sculptor_test` (Earthrend Gauntlet), qu'on peut surcharger.
- **Dans Fallow** : présent, aucune config.
- **Comment il apparaît** : par la structure `monastery` (pilier de montagne), ou par `/summon`.
- **Lié à un seul monument** : oui, persistant (`requiresCustomPersistence` = `true`). Il lui faut le ciel ouvert sur la hauteur de l'épreuve (message « Test is blocked! »).

**Sacred Pontiff / His Holiness** (Companions!, `companions:sacred_pontiff`)
- **Allure et ton** : un grand prêtre en robe de mousse verte, avec une couronne et un masque d'or et un bâton de bois (texture `sacred_pontiff_0.png`). Il reste **inerte** (état 0) jusqu'à ce qu'**un joueur le touche de la main** : `mobInteract` déclenche alors `cycleState` et le son `PONTIFF_ACTIVATE`. Sa seconde forme se **pétrifie** en mourant (textures `his_holiness_petrification0..20`). On croirait une statue de temple qui s'éveille.
- **Boss** : oui, barre rouge `TrackedServerBossEvent`, en deux phases. `setDarkenScreen(true)` et `setCreateWorldFog(true)` sont **codés en dur**. Il a une musique de boss (`IBossMusicProvider`, sans clé de config dans `KnightLibConfig`).
- **Points de vie par défaut** : environ 225 puis 350 (champs `SACRED_PONTIFF_MAX_LIFE` / `HIS_HOLINESS_MAX_LIFE` de `CompanionsConfig` ; l'ordre est déduit).
- **Dans Fallow** : présent. Selon 0003, les boss de Companions! sont « à désactiver ou à rendre très rares ». Un exemplaire par monument, c'est « très rare ».
- **Comment il apparaît** : par la structure `companions_monkey_temple` (entité dans le `.nbt` ; biomes `MONKEY_TEMPLE_STRUCTURE_BIOMES = "minecraft:jungle"`), ou par `/summon`.
- **Lié à un seul monument** : oui (`removeWhenFarAway` renvoie `false`).

#### Les candidats écartés

**Umvuthi, l'Oiseau-Soleil** (Mowzie's)
- **Allure et ton** : un chef de tribu masquée, avec des Umvuthana. Caricature tribale ; le plan 08 en fait déjà une question de direction artistique.
- **Boss** : oui (`has_boss_bar`).
- **Dans Fallow** : présent, et la question reste ouverte (08, ligne 10).
- **Comment il apparaît** : par la structure `umvuthana_grove`.
- **Verdict : écarté** pour le ton.

**Naga, Foliaath, Grottol, Lantern** (Mowzie's)
- **Allure et ton** :
  - Naga : serpent volant des falaises ;
  - Foliaath : plante carnivore de jungle ;
  - Grottol et Lantern : petites créatures pacifiques.
- **Boss** : non.
- **Dans Fallow** : présents.
- **Comment ils apparaissent** : spawn naturel.
- **Verdict : écartés**, ce ne sont pas des gardiens. Le Grottol (qui mange les minerais) ferait un clin d'œil au monument des nains, rien de plus.

**Guster** (Alex's Mobs)
- **Allure et ton** : une petite tornade de sable du désert. C'est l'équivalent le plus proche de la breeze.
- **Boss** : non.
- **Dans Fallow** : limité par In Control.
- **Comment il apparaît** : naturellement ; on pourrait aussi en mettre un dans le spawner.
- **Verdict : variante possible** pour le monolithe chasseur, mais en spawner (il réapparaît) et non en gardien unique.

**Spectre, Farseer, Murmur, Skreecher** (Alex's Mobs)
- **Verdict : écartés**, ton horrifique.

**Underminer** (Alex's Mobs)
- **Allure et ton** : un fantôme de mineur nain [déduit du nom et du wiki].
- **Boss** : non. Pacifique.
- **Verdict : écarté comme gardien.** On pourrait le garder en présence ambiante près du monument des conifères.

**Tuff Golem** (Friends & Foes)
- **Allure et ton** : une statue de tuf qui s'anime, pacifique. Les monuments sont en tuf.
- **Boss** : non.
- **Dans Fallow** : n'apparaît que dans les strongholds, qui sont supprimés (plan 05).
- **Verdict : à garder comme décor vivant**, pas comme gardien.

**Wildfire, Iceologer, Illusioner** (Friends & Foes)
- **Boss** : Wildfire, oui.
- **Dans Fallow** : déjà à désactiver (0003).
- **Verdict : écartés.**

**Karkinos** (Hybrid Aquatic, `hybrid_aquatic:karkinos`)
- **Allure et ton** : un crabe géant (dossier `entity/miniboss`) qui invoque des larbins (`Summoning`).
- **Boss** : mini-boss avec barre (`BossEvent` dans `KarkinosEntity`).
- **Dans Fallow** : présent.
- **Comment il apparaît** : par `/summon`.
- **Verdict : option** pour le monolithe sentinelle (sous l'eau). La sentinelle et son égide vont avec une carapace ; mais le lore des moines dit plutôt « pas de combat ».

**Kodama** (Ghost)
- **Allure et ton** : de petits esprits des arbres, japonais [nom ; spawn codé dans `ghosts_mob_spawns`].
- **Boss** : non.
- **Verdict : présence douce** possible au monument du cerisier, à vérifier en jeu.

**Living pot, living urn, buried** (NML)
- **Allure et ton** : de petits mobs des ruines d'alchimiste [noms d'entités].
- **Verdict : écartés**, trop petits pour un gardien.

**Forgotten** (Quark)
- **Allure et ton** : un squelette fantôme.
- **Verdict : écarté**, mort-vivant.

### 1.4 Les mods nouveaux vérifiés [Modrinth, NeoForge 1.21.1]

**Bosses of Mass Destruction (Forge/NeoForge)**
- **Version** : 1.3.3 (2026-07-17), `BOMD-NeoForge-1.21-1.3.3.jar`, 1,95 Mo.
- **Dépendances** : `cerbons-api` 1.3.0 (0,25 Mo), `cloth-config` 15.0.140 (1,16 Mo), GeckoLib (déjà présent).
- **Ce qu'il ajoute** : 4 boss (Night Lich, Obsidilith, Nether Gauntlet, Void Blossom) et 4 structures.
  - Les biomes des structures sont des **tags de datapack** (`has_structure/<id>.json`) : on peut tout désactiver.
  - L'Obsidilith est un obélisque d'obsidienne à runes violettes (texture du jar).
  - Réglages (`ObsidilithConfig`) : 300 PV, 14 d'armure, 16 dégâts, 1 000 XP, `spawnPillarOnDeath` (vrai par défaut), `anvilAttackExplosionStrength` (4).
  - **Il pose de l'obsidienne** : `PillarAction` appelle `setBlockAndUpdate(OBSIDIAN)`.
  - Il ne disparaît pas (`preventDespawnExceptPeaceful`).
- **Verdict** : **premier choix si l'on ajoute un mod**. On n'en garde que l'Obsidilith, comme gardien-monolithe. Réserve : couleur violette, ambiance de l'End, et des blocs posés dans le monument.

**Legendary Monsters**
- **Version** : 2.2.3 (2026-09-17), 44,75 Mo, aucune dépendance, licence ARR.
- **Ce qu'il ajoute** : environ 37 entités et 14 structures. Parmi elles, Overgrown Colossus (temple moussu), Frostbitten Golem, Dune Sentinel et Ancient Guardian sont thématiquement idéaux. Il contient aussi des chevaliers décapités et des abominations.
- **Désactivable ?** Oui en théorie : structures par tags `has_structure/*`, spawns par les biome_modifier `bomber` et `chorusling`.
- **Verdict : écarté.** C'est exactement le cas « 20 boss et des donjons » : trop lourd.

**Ivar's Bosses**
- **Version** : 1.0.0 (2026-07-12), 0,84 Mo, MIT, une seule version.
- **Ce qu'il ajoute** : Fang, « gardien de la forêt » invoqué par rituel. Mais aussi Spider Mother, Fenrir et Dracula, et des refontes du Warden et du Dragon.
- **Verdict : écarté** (horreur, rituels).

**LAir's Bosses**
- **Version** : 1.0.0 (2024-12-08), 0,43 Mo, fait avec MCreator.
- **Ce qu'il ajoute** : l'Esprit de la forêt, déclenché par un « éco-score », et Zeus avec la structure Olympe.
- **Verdict : écarté** (abandonné, MCreator).

**The Gadyn**
- **Version** : 1.0.0, 0,28 Mo, MCreator, licence ARR.
- **Ce qu'il ajoute** : un titan neutre qui apparaît naturellement (biome_modifier).
- **Verdict : écarté** (qualité inconnue).

**WhirlWind**
- **Version** : 1.21.1 (2024-12-23).
- **Ce qu'il ajoute** : une variante de la breeze.
- **Verdict : écarté**, il a le même défaut que la breeze.

**EEEAB's Mobs [Unofficial]**
- **Version** : 1.0.2 (2026-09-04), 29,3 Mo.
- **Ce qu'il ajoute** : Nameless Guardian, mais aussi des cadavres, du sang et des « Bloody Acolyte » (lang du jar).
- **Verdict : écarté** (gore, et c'est un portage non officiel).

**Mebahel's Creatures – Dwarven Automatons**
- **Version** : 1.4.0 (2026-10-06), un jar **Fabric**, utilisable sur NeoForge par Connector.
- **Dépendances** : Mebahel's API et Mebahel's Elder Scrolls Core.
- **Ce qu'il ajoute** : une faction entière d'automates nains, des ruines, de l'équipement.
- **Verdict : écarté** (trop de contenu, licence ARR), alors que le thème colle (« les machines d'une civilisation perdue gardent ses ruines »).

**L_Ender's Cataclysm, Bosses'Rise, Aquamirae**
- **Verdict : écartés.** Ce sont des dizaines de boss et de donjons, et Aquamirae a une ambiance horrifique.

---

## 2. Les liens concrets à construire

### 2.1 Quel gardien pour quel monument

Un gardien différent par type est cohérent, puisque le lore d'AR donne une identité à chaque lieu.

| Monument | Gardien | Pourquoi | Bénédiction scellée ? |
|---|---|---|---|
| warrior_monolith | **Ferrous Wroughtnaut** | « Le monolithe des héros » : l'armure d'un guerrier d'autrefois veille encore. Il dort jusqu'au premier coup et revient à sa place. | oui, jusqu'à sa mort |
| coniferous_monument | **Frostmaw** | Le temple des nains de la taïga et des montagnes ; le Frostmaw est la bête des neiges, endormie. | oui, jusqu'à sa mort |
| hunter_monolith | **Tongbi, le Sculpteur** | « Agilité et précision » : son épreuve est une course d'agilité, pas un combat. | oui, jusqu'à l'épreuve réussie (il disparaît) ou sa mort |
| oakwood_monument | **Sacred Pontiff** | Un prêtre de mousse, au temple elfique moussu. On choisit de le réveiller. Il se pétrifie en mourant. | oui, jusqu'à sa mort |
| cherry_monument | **aucun gardien de combat** (éventuellement des Kodamas) | « Monument de la vie », qui guérit. Un boss y contredirait le texte. | non |
| sentinel_monolith | **aucun** (option : Karkinos) | Il n'a pas de spawner, et ses moines « fuyaient les combats inutiles ». | non |

Cela fait 4 gardiens au plus, tous uniques, endormis ou passifs jusqu'à ce qu'on les provoque, et qui ne quittent pas leur monument. C'est la définition de « rares et territoriaux » (0003).

### 2.2 Retirer les spawners de breeze (datapack)

**Surcharger les deux processor_list au même chemin** : `data/ancient_remnants/worldgen/processor_list/monolith.json` et `monument.json`. On garde le `block_ignore` d'origine et on remplace le spawner par un bloc de la palette du monument :

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

- Le NBT du spawner reste attaché au bloc, mais `chiseled_tuff` n'a pas d'entité de bloc : il est ignoré à la pose [déduit de `StructureTemplate`].
- Ne pas utiliser un simple `block_ignore` du spawner : le bloc de terrain d'avant resterait à sa place, et laisserait un trou ou de la pierre au milieu du pilier [déduit].
- À faire **avant** de générer le monde : les monuments déjà générés gardent leurs spawners.

### 2.3 Faire apparaître le gardien, sceller et libérer (KubeJS)

C'est la méthode recommandée. Elle fusionne avec `clairieres_elderheart.js` (rapport 4, §2.3) et ne demande ni fichier binaire modifié, ni mod custom.

```js
// server_scripts/gardiens_monuments.js — ESQUISSE à tester (KubeJS 2101.7)
const UUID = Java.loadClass('java.util.UUID')
const GARDIENS = {
  'ancient_remnants:warrior_monolith':    { id: 'mowziesmobs:ferrous_wroughtnaut', nbt: '' },
  'ancient_remnants:coniferous_monument': { id: 'mowziesmobs:frostmaw',            nbt: 'has_crystal:1b,' },
  'ancient_remnants:hunter_monolith':     { id: 'mowziesmobs:sculptor',            nbt: '', cielOuvert: true },
  'ancient_remnants:oakwood_monument':    { id: 'companions:sacred_pontiff',       nbt: '' }
}

// (1) Sceller : dès que l'Elderheart entre dans le monde (pose de la structure ou chargement du chunk)
EntityEvents.spawned('ancient_remnants:elderheart', event => {
  const e = event.entity, pd = e.persistentData
  if (pd.contains('fallow_type')) return                     // déjà traité
  const nbt = e.nbt
  const type = String(nbt.getString('PossibleBlessings'))
  pd.putString('fallow_type', type)
  if (!GARDIENS[type] || !nbt.contains('Blessing')) return
  pd.putString('fallow_benediction', nbt.getString('Blessing'))
  event.server.scheduleInTicks(1, () => { const n = e.nbt; n.remove('Blessing'); e.nbt = n })
})

// (2) Réveiller : la première fois qu'un joueur passe à moins de 48 blocs
ServerEvents.tick(event => {
  if (event.server.tickCount % 100 !== 0) return
  event.server.players.forEach(p => {
    p.level.getEntitiesWithin(p.boundingBox.inflate(48)).forEach(e => {
      if (String(e.type) !== 'ancient_remnants:elderheart') return
      const pd = e.persistentData, g = GARDIENS[String(pd.getString('fallow_type'))]
      if (!g) return
      if (!pd.contains('fallow_gardien')) {
        const pos = global.fallowSolDuGardien(p.level, e.blockPosition(), g.cielOuvert) // sol au pied du monument
        const lien = 'fallow_g_' + e.uuid.toString().substring(0, 8)
        event.server.runCommandSilent(`execute in ${p.level.dimension} run summon ${g.id} ${pos.x} ${pos.y} ${pos.z} {${g.nbt}PersistenceRequired:1b,Tags:["fallow_gardien","${lien}"]}`)
        const garde = p.level.getEntitiesWithin(AABB.ofSize(pos, 8, 8, 8)).find(x => x.tags.contains(lien))
        if (garde) { garde.persistentData.putString('fallow_elderheart', e.uuid.toString()); pd.putString('fallow_gardien', garde.uuid.toString()) }
      } else if (pd.contains('fallow_benediction') && g.cielOuvert) {
        // Sculpteur : disparu après la récompense, sans mourir → épreuve réussie
        if (!p.level.getEntity(UUID.fromString(pd.getString('fallow_gardien')))) global.fallowLibererElderheart(e, p)
      }
    })
  })
})

// (3) Libérer : à la mort du gardien
EntityEvents.death(event => {
  const g = event.entity
  if (!g.tags.contains('fallow_gardien')) return
  const eh = event.level.getEntity(UUID.fromString(g.persistentData.getString('fallow_elderheart')))
  if (eh) global.fallowLibererElderheart(eh, event.source.player)
})

global.fallowLibererElderheart = (eh, joueur) => {
  const pd = eh.persistentData
  if (!pd.contains('fallow_benediction')) return
  const n = eh.nbt; n.putString('Blessing', pd.getString('fallow_benediction')); eh.nbt = n
  pd.remove('fallow_benediction')
  global.fallowOuvrirClairiere(eh.level, eh.blockPosition(), 32, joueur)   // rapport 4, §2.3
  eh.block.popItem(Item.of('ancient_remnants:research_scrap',
    `[ancient_remnants:stored_research=["${pd.getString('fallow_type')}"]]`)) // la page de ce monument
}
```

Ce que relie ce script :
- **la brume** : la clairière s'ouvre quand le gardien tombe, et non à la simple approche. Pour le cerisier et la sentinelle, sans gardien, on garde le déclencheur d'approche du rapport 4. Le Pontiff assombrit le ciel et épaissit le brouillard pendant le combat (codé en dur) : la brume qui se lève à sa mort en devient un moment fort ;
- **le codex** : la page du monument en récompense (composant `ancient_remnants:stored_research`, rapport 4 §1.3) ;
- **l'Elderheart** : la bénédiction devient le prix du gardien.

`global.fallowSolDuGardien` est à écrire :
- tester 16 directions à 10–20 blocs de l'Elderheart ;
- prendre `level.getHeight('motion_blocking_no_leaves', x, z)` ;
- vérifier 4 blocs d'air au-dessus, et `canSeeSky` pour le Sculpteur ;
- retomber sur la position de l'Elderheart moins 4 blocs si rien ne convient [déduit].

### 2.4 Configs et datapack qui accompagnent les gardiens

**Mowzie's Mobs** (config commune) :
- `ferrous_wroughtnaut`, `frostmaw` et `sculptor` : `has_boss_bar` à true, `heals_out_of_battle` à true, `reset_health_when_respawn` à true ;
- `sculptor.test_height` à environ 30 (au lieu de 60) ; `test_time_limit` à garder ;
- `sculptor.trade_which_item` : proposer un objet de l'histoire du lieu, par exemple `minecraft:amethyst_shard` (déjà dans la recette du codex). On peut tenter `ancient_remnants:research_scrap`, mais l'objet est consommé : à tester ;
- `sculptor.disappear_after_reward` à **true**. Le script s'en sert pour détecter l'épreuve réussie.

**Rendre les gardiens uniques** (datapack) :
- surcharger `data/mowziesmobs/worldgen/structure/wrought_chamber.json` et `frostmaw_spawn.json` avec `"biomes": "#fallow:aucun"`, un tag vide ;
- faire de même pour `monastery.json`, si le Sculpteur ne doit exister qu'au monolithe ;
- Companions! : mettre `MONKEY_TEMPLE_STRUCTURE_BIOMES` sur un biome inexistant, à tester (la valeur est une chaîne, `"minecraft:jungle"` par défaut) ;
- sinon, le pack aura aussi des Wroughtnauts en salles souterraines, environ tous les 15 chunks (`wrought_chambers`, `spacing 15`).

**Récompenses** (loot tables, facultatif) :
- surcharger `mowziesmobs:entities/sculptor_test` si l'on ne veut pas de l'Earthrend Gauntlet ;
- `ferrous_wroughtnaut` donne le Wrought Helm et la hache ; `frostmaw` donne un ice_crystal et un disque.

**Wildex, Exposure** :
- une page de bestiaire par gardien ;
- l'advancement `exposure:frame_exposed` (`entities_in_frame`), par exemple « photographier le Pontife éveillé ».

---

## 3. Conflits et risques

1. **Contourner le gardien.** Sans le sceau, on passe à 16 blocs de l'Elderheart et on reçoit la bénédiction sans rien faire. Le Wroughtnaut ne bouge pas tant qu'on ne le frappe pas, et le Pontiff tant qu'on ne le touche pas. Le sceau (§2.3) règle ce point.
2. **La place.** Les boss ne tiennent pas dans les couloirs de 2 blocs où sont les spawners (§1.1). On les pose au sol, à l'extérieur, et il faut ajuster les décalages type par type en jeu.
3. **La persistance du Frostmaw.** Il n'est persistant qu'avec son cristal. `PersistenceRequired:1b` est donc obligatoire, sinon le gardien peut disparaître sans mourir, et le sceau reste fermé à jamais.
   - Garde-fou à ajouter : une commande op `/function fallow:liberer_elderheart`, ou un test « gardien introuvable depuis plus de N jours ».
4. **`setNbt` sur l'Elderheart.** Cela appelle `Entity.load` sur une entité vivante. À tester : position, UUID, son, et la bénédiction réellement nulle.
5. **`EntityEvents.spawned`.** Il faut vérifier qu'il est bien levé pour une entité **chargée depuis le disque**, ou au premier chargement d'un chunk prégénéré par Chunky. Sinon, il faut faire le sceau dans le polling, avec un rayon d'au moins 48 blocs, car un joueur au sprint parcourt environ 28 blocs en 5 s.
6. **Le Sacred Pontiff est lourd.**
   - Écran assombri, brouillard et musique de boss non réglables. Pour couper la musique, il faut un resource pack avec un son vide.
   - Environ 575 PV au total, en deux phases.
   - Pour un début de partie, c'est dur : à équilibrer avec `SACRED_PONTIFF_MAX_LIFE` / `HIS_HOLINESS_MAX_LIFE`.
7. **Le Sculpteur a besoin de ciel ouvert** sur toute la hauteur de l'épreuve. Ses géomancies créent des entités de rochers : vérifier qu'elles ne cassent pas de blocs du monolithe. Aucun `setBlock` n'a été trouvé dans les classes du Sculpteur, du Wroughtnaut ni du Frostmaw.
8. **Dégâts au monument.** Aucun `setBlock` n'a été trouvé dans les classes du Pontiff (anneaux de feu compris). Il faut tout de même vérifier les feuilles du monument du chêne et le feu.
   - L'Obsidilith (si on ajoute BoMD) **pose de l'obsidienne** : `spawnPillarOnDeath=false` et `anvilAttackExplosionStrength=0` limitent les dégâts, sans les supprimer.
9. **In Control.** Si l'on choisissait plutôt un spawner (variante B, §4), le spawn passerait par `PositionCheck`, et les règles Mowzie's des brouillons le bloqueraient (`minlight 8` dans un monument éclairé). Avec `/summon`, ce n'est pas le cas (§1.3).
10. **Les mises à jour d'AR.** Le script dépend de `PossibleBlessings`, de `Blessing` et de l'id `ancient_remnants:elderheart`, et la processor_list des chemins actuels. Si AR change ses pièces, il suffit de revérifier ces points ; aucun `.nbt` n'est copié dans Fallow.
11. **Le multijoueur.** Il y a un gardien par monument, une seule fois. Après sa mort, la bénédiction est libre pour tous, ce qui est cohérent avec un monde permanent.

---

## 4. À adapter, supprimer ou ajouter dans le plan

**À remplacer**
- **Rapport 4, §2.2 (options A, B, C)** : remplacer par « processor_list qui remplace les spawners + gardien unique par KubeJS + sceau de l'Elderheart » (§2.2 et 2.3 ici).
  - L'option A (garder la breeze) est à abandonner : elle contredit la liste d'exclusion et la direction artistique.
  - L'option C (aucun gardien) ne reste vraie que pour le cerisier et la sentinelle.
- **Rapport 4, §2.3** : la clairière s'ouvre à la **libération** de l'Elderheart, et non à la première approche, quand le monument a un gardien.
- **Chapitre 12, ligne 15 du tableau** (« Gardiens : aucun ») : mettre « 4 gardiens uniques, pris dans Mowzie's et Companions! ».
- **Chapitre 11 §11.8** : « Au centre, un Elderheart gardé par des créatures » devient « gardé, selon le monument, par un gardien unique qu'il faut vaincre ou dont il faut réussir l'épreuve ».

**À ajouter**
- **Plan 05 §5.3 (Mowzie's)** : désactiver la génération naturelle de `wrought_chamber`, `frostmaw_spawn` et, éventuellement, `monastery` (§2.4). Les figures de Mowzie's deviennent alors des gardiens de monuments, plus seulement des anomalies.
- **Décision 0003** : ajouter une ligne « Gardiens des monuments AR : Wroughtnaut, Frostmaw, Sculpteur (Mowzie's), Sacred Pontiff (Companions!) ; un par monument, jamais en spawn naturel ».
- **Configs** à créer dans `pack/config` : la config commune de Mowzie's (§2.4) et celle de Companions! (points de vie du Pontiff, biomes du temple).
- **Datapack** `kubejs/data/` :
  - `ancient_remnants/worldgen/processor_list/{monolith,monument}.json` ;
  - les surcharges de `mowziesmobs/worldgen/structure/*.json` ;
  - le tag vide `fallow:aucun`.

**À ne pas faire**
- Pas de nouveau mod. Si l'équipe veut un « vrai » boss au monolithe chasseur à la place de l'épreuve du Sculpteur, on ajoute BoMD (Obsidilith seul, quatre structures désactivées par tags).

**Variantes, si le script devait échouer**
- **B, sans script** :
  - surcharger les 5 template_pools qui contiennent un spawner (`cherry_monument/top_3`, `coniferous_monument/bottom_1`, `hunter_monolith/middle`, `oakwood_monument/top_0`, `warrior_monolith/middle`) pour qu'ils pointent vers une processor_list propre à chaque type ;
  - dans chacune, une règle `spawner` → `spawner` avec `block_entity_modifier` `minecraft:append_static` (`SpawnData`, `SpawnCount 1`, `MaxNearbyEntities 1`) ;
  - limite : un spawner fait **réapparaître le mob sans fin**. C'est acceptable pour un petit gardien (des Gusters au monolithe chasseur), pas pour un boss.
- **C, entité dans le `.nbt`** (comme le fait Mowzie's) : réécrire 5 `.nbt` d'AR. Cela pose trois problèmes : un binaire à maintenir, la licence OML d'Obscuria, et des salles trop étroites. Non recommandé.
- **D, trial spawner** posé par processor : c'est presque un « une seule fois » vanilla, mais avec la cage des chambres d'épreuves, un délai de réarmement et des clés de butin. Il casse la direction artistique. Non recommandé.
- **Mod custom** : inutile ici. KubeJS suffit, car l'Elderheart se scelle par NBT.

---

## 5. Questions à tester en jeu

1. `EntityEvents.spawned('ancient_remnants:elderheart')` est-il levé au premier chargement d'un monument, et aussi après un prégénérage par Chunky ? Le sceau (`e.nbt = n` sans `Blessing`) empêche-t-il vraiment le titre « Blessing Acquired » ?
2. La bénédiction est-elle bien rendue à la mort du gardien, et l'Elderheart reste-t-il intact (son, rayons, position) après les deux `setNbt` ?
3. Positions de pose au pied des 4 monuments gardés : le boss tient-il ? Le Wroughtnaut fixe-t-il son `restPos` quand on l'invoque par `/summon` ? Le Frostmaw dort-il en arrivant (essayer `active:0b`) ?
4. Le Sculpteur près du monolithe chasseur, avec `test_height` 30 : l'épreuve démarre-t-elle (pas de « Test is blocked! ») ? `disappear_after_reward` déclenche-t-il bien la libération ?
5. Sacred Pontiff près du monument du chêne :
   - équilibre en début et milieu de partie ;
   - rendu de l'écran assombri avec More Darkness et ATMOSPHERICS ;
   - feu sur les feuilles ;
   - musique.
6. Les règles In Control (brouillons) et `spawns_safety.js` laissent-elles passer les quatre invocations (attendu : oui) ?
7. Avec les structures naturelles de Mowzie's désactivées par `#fallow:aucun`, plus aucune salle de Wroughtnaut ni aucun Frostmaw sauvage n'apparaît-il (`/locate structure`) ?
8. Le spawner remplacé par `chiseled_tuff` : rendu correct dans les 5 pièces (pas de trou, pas de bloc de terrain) ?

---

## Recommandation

**Premier choix parmi les mods actuels : rien à ajouter.** On utilise un gardien différent par type de monument, pris dans Mowzie's Mobs 1.8.2 et Companions! 1.3.4, qui sont déjà dans le pack :
- **Ferrous Wroughtnaut** au monolithe guerrier (armure endormie, barre de boss, revient à sa place) ;
- **Frostmaw** au monument des conifères (bête des neiges endormie, invoquée avec `has_crystal` et `PersistenceRequired`) ;
- **Tongbi le Sculpteur** au monolithe chasseur (une épreuve d'agilité plutôt qu'un combat) ;
- **Sacred Pontiff** au monument du chêne (statue de mousse qu'on choisit d'éveiller, et qui se pétrifie en mourant) ;
- **aucun gardien** au cerisier ni à la sentinelle, comme le veut leur lore.

Les preuves sont au §1.3 : NBT `active`, `restPos` et `has_crystal`, `requiresCustomPersistence`, les clés `has_boss_bar`, `test_height`, `disappear_after_reward`, `mobInteract` du Pontiff, et ses textures de pétrification.

**Méthode** :
- une **processor_list** qui remplace les spawners de breeze par du tuf ciselé (datapack) ;
- un **script KubeJS** qui :
  - scelle l'Elderheart en retirant `Blessing` (il ne donne rien si `blessing == null`, d'après le bytecode de `tick()`) ;
  - fait apparaître le gardien une seule fois quand un joueur approche ;
  - libère la bénédiction à la mort du gardien, ou quand l'épreuve est réussie ;
  - ouvre alors la clairière et donne la page du codex du monument.

C'est la seule méthode qui garantit un gardien unique par monument sans réécrire les `.nbt` d'AR, qui contourne les règles In Control, et qui relie gardien, bénédiction, brume et codex en un seul moment de jeu.

**Premier choix de mod nouveau, s'il en faut un** : **Bosses of Mass Destruction (Forge/NeoForge) 1.3.3** (1,95 Mo, avec cerbons-api et cloth-config), réduit à l'**Obsidilith** :
- ses 4 structures se désactivent par tags de datapack ;
- c'est un monolithe d'obsidienne vivant, en accord littéral avec le thème, avec une barre de boss et 300 PV ;
- il ne disparaît pas.

Réserves : il pose de l'obsidienne, il a une teinte violette de l'End, et ses trois autres boss (dont une liche mort-vivante) restent dans le jar. Je ne le recommande que si l'équipe préfère un vrai combat à l'épreuve du Sculpteur. Legendary Monsters serait thématiquement idéal (Overgrown Colossus, Dune Sentinel, Frostbitten Golem), mais ses 44,75 Mo et ses 14 structures en font exactement le mod « 20 boss et donjons » qu'on veut éviter.
