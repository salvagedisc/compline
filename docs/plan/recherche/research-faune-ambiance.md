# Recherche — Faune, créatures rares, contrôle des spawns et ambiance (Fallow, 1.21.1 NeoForge)

> Recentré après la mise à jour du coordinateur : la faune et l'ambiance de base sont **déjà retenues** (Naturalist 2.0.4, Alex's Mobs Continued 2.2.2, Hybrid Aquatic 1.7.3, Mowzie's Mobs 1.8.2, Critters and Companions 2.7.0, Untitled Duck Mod, Friends&Foes 4.0.27, Adorable Hamster Pets, Companion, Companions!, Ghost ; ATMOSPHERICS, AmbientSounds, Sound Physics Remastered, Subtle Effects, LambDynamicLights 4.8.11, More Darkness). Je ne les repropose pas. Ce rapport porte sur ce qui manque :
> 1. suppression des monstres vanilla et contrôle des spawns, avec une table par biome ;
> 2. créatures rares et ambiance complémentaires (en privilégiant les mods peu connus) ;
> 3. météo, ciel, musique, curiosités ;
> 4. réglages concrets des mods déjà retenus.
>
> **Limites de la recherche.** modrinth.com, son API et son CDN sont bloqués, donc aucun .jar n'a pu être ouvert. La disponibilité vient d'extraits de recherche (pages de versions Modrinth, miroirs BoxToPlay, Mado, ModDex, ModpackIndex) et des sources GitHub publiques. Le **quota de recherche web partagé (200) a été épuisé** en cours de route. Ces sujets n'ont donc pas pu être vérifiés et sont marqués « non vérifié » : aurores et éclipses, torches qui s'éteignent, Bosses of Mass Destruction, Tide, Extreme Sound Muffler. Une relance plus tard pourra les compléter.

---

## 0. Ce qu'il faut retenir

1. **Supprimer les hostiles vanilla est une chaîne, pas un réglage.** Il y a six vecteurs d'apparition différents :
   - les listes de spawn par biome ;
   - les `spawn_overrides` des structures ;
   - les spawners ;
   - les gamerules (patrouilles, phantoms, raids, warden, marchand) ;
   - les conversions (sorcière par la foudre, piège de chevaux squelettes) ;
   - les entités placées à la génération (manoirs, villages zombies, monuments).

   Je propose trois couches : un **datapack de biome modifiers** (zéro coût), **In Control!** pour les règles fines, et un **filet KubeJS** qui annule tout ce qui passe encore.
2. **Effet de bord majeur : la cap MONSTER (70) devient entièrement disponible pour les hostiles moddés.** Sont concernés : Foliaath, Naga et Grottol de Mowzie's, les centipèdes, Guster et Rocky Roller d'AM, le Mauler et le Barnacle de F&F, les requins Hybrid… Sans plafond, ces « créatures rares » deviennent des hordes. Il faut **plafonner par mod et par joueur** et **interdire l'apparition dans la lumière**. La base reste alors littéralement un îlot de lumière.
3. **Des ressources disparaissent avec les monstres vanilla**, et il faut une recette de remplacement en KubeJS. Les plus critiques :
   - la **slime ball** : Super Glue de Create, sticky piston, sticker, laisse ;
   - le **Blaze** : le Blaze Burner de Create se remplit en capturant un blaze vivant, et le laiton passe par le mélange chauffé ;
   - la poudre à canon ;
   - la ficelle, en partie couverte par le broyage de la laine dans Create ;
   - le phantom membrane.

   À traiter dans le sous-projet 3 avant toute ouverture du serveur.
4. **Nouveautés recommandées :**
   - **In Control!** (contrôle des spawns) ;
   - **Cosy Critters & Creepy Crawlies** : oiseaux en vol qui se dispersent, papillons de nuit attirés par les lumières. C'est exactement « silhouettes + îlot de lumière » ;
   - **Enhanced Celestials** : lunes de moisson et lune bleue, lune de sang désactivée ;
   - **Simple Voice Chat** : voix de proximité, avec la réverbération de Sound Physics ;
   - **Presence Not Position** : musique et titres adaptatifs à l'entrée d'un lieu ou d'une base. Très peu connu, à vérifier.
5. **À tester sur branche :**
   - **Simple Clouds** : nuages et orages visibles de loin. Il utilise des **compute shaders**, donc probablement **pas de macOS**, et entre en conflit avec la météo d'Ecliptic en mode serveur ;
   - **Longwings** ;
   - **Particular Reforged**.

   **Weather2** (tornades) est écarté pour l'instant : il est en conflit avec Ecliptic Seasons.

---

## 1. Suppression des monstres vanilla et contrôle des spawns

### 1.1 Les six vecteurs et la couche qui traite chacun

| Vecteur | Exemples | Traitement proposé | Couche |
|---|---|---|---|
| Listes de spawn des biomes | zombie, squelette, creeper, araignée, enderman, sorcière, slime, husk, stray, bogged, drowned | `neoforge:remove_spawns` sur `#minecraft:is_overworld` | Datapack (`pack/kubejs/data/fallow/neoforge/biome_modifier/`) |
| `spawn_overrides` des structures | gardiens (monument), sorcière (cabane), pillards (avant-poste), *Nether : forteresse* | In Control `deny` (il s'applique aussi aux overrides) + filet KubeJS ; à défaut, surcharge JSON des structures | In Control / KubeJS |
| Spawners | donjons `monster_room`, araignées bleues des mineshafts, trial spawners (breeze, etc.) | `neoforge:remove_features` sur `minecraft:monster_room` et `monster_room_deep` (étape `underground_structures`) ; trial chambers : à retirer via Sparse Structures ou à garder vides | Datapack + Sparse Structures |
| Événements gérés par gamerule | phantoms, patrouilles, raids, warden, marchand ambulant (concept « survivants ») | `doInsomnia false`, `doPatrolSpawning false`, `disableRaids true`, `doWardenSpawning false`, `doTraderSpawning false` (garder `doMobSpawning true` pour les animaux) | KubeJS `ServerEvents.loaded` → commandes |
| Conversions | villageois frappé par la foudre → sorcière ; piège de chevaux squelettes en orage ; piglin → piglin zombifié dans l'Overworld ; et côté F&F, `enableZombieHorseTrap` | Filet KubeJS (annulation à l'arrivée dans le monde) + config F&F | KubeJS |
| Entités placées à la génération | vindicateurs et évocateurs des manoirs, gardiens anciens, zombies-villageois des villages zombies (concept « ruines »), silverfish des blocs infestés | Filet KubeJS (`EntityEvents.spawned` annulé) ; `neoforge:remove_features` sur `minecraft:ore_infested` | KubeJS + datapack |

**Exemples (syntaxe à valider au premier lancement).**

`pack/kubejs/data/fallow/neoforge/biome_modifier/remove_vanilla_hostiles.json`
```json
{
  "type": "neoforge:remove_spawns",
  "biomes": "#minecraft:is_overworld",
  "entity_types": [
    "minecraft:zombie", "minecraft:zombie_villager", "minecraft:husk", "minecraft:drowned",
    "minecraft:skeleton", "minecraft:stray", "minecraft:bogged", "minecraft:creeper",
    "minecraft:spider", "minecraft:cave_spider", "minecraft:enderman", "minecraft:witch",
    "minecraft:slime", "minecraft:phantom", "minecraft:silverfish", "minecraft:skeleton_horse"
  ]
}
```
`.../remove_dungeons.json`
```json
{ "type": "neoforge:remove_features", "biomes": "#minecraft:is_overworld",
  "features": ["minecraft:monster_room", "minecraft:monster_room_deep", "minecraft:ore_infested"],
  "steps": ["underground_structures", "underground_ores"] }
```
**Astuce clé.** Pour désactiver le biome modifier d'un **autre mod** sans toucher à sa config, on surcharge son fichier au même chemin avec `{ "type": "neoforge:none" }`. Exemple : `data/naturalist/neoforge/biome_modifier/remove_forest_pigs.json`.

Naturalist enregistre ses animaux par un modifier codé (`naturalist:add_animals`, une seule entrée) piloté par sa config. Ses trois autres modifiers (`remove_farm_animals_savanna`, `remove_farm_animals_swamp`, `remove_forest_pigs`) retirent les animaux de ferme vanilla. À garder, c'est cohérent avec l'élevage (source : GitHub Crispytwig/naturalist, branche `1.21.1-NeoForge`).

**Filet KubeJS** (`pack/kubejs/server_scripts/fallow/no_vanilla_hostiles.js`, API KubeJS 2101 à valider) :
```js
const BANNED = ['minecraft:zombie','minecraft:zombie_villager','minecraft:husk','minecraft:drowned',
  'minecraft:skeleton','minecraft:stray','minecraft:bogged','minecraft:creeper','minecraft:spider',
  'minecraft:cave_spider','minecraft:enderman','minecraft:witch','minecraft:slime','minecraft:phantom',
  'minecraft:silverfish','minecraft:guardian','minecraft:elder_guardian','minecraft:pillager',
  'minecraft:vindicator','minecraft:evoker','minecraft:ravager','minecraft:vex','minecraft:illusioner',
  'minecraft:breeze','minecraft:warden','minecraft:skeleton_horse','minecraft:zombie_horse','minecraft:endermite']
EntityEvents.spawned(event => {
  const e = event.entity
  if (!BANNED.includes(String(e.type))) return
  if (String(e.level.dimension) !== 'minecraft:overworld') return   // Nether : décision séparée
  if (e.tags.contains('fallow_allowed')) return                    // /summon admin avec Tags:["fallow_allowed"]
  event.cancel()
})
ServerEvents.loaded(event => {
  ['doInsomnia false','doPatrolSpawning false','disableRaids true','doWardenSpawning false','doTraderSpawning false']
    .forEach(r => event.server.runCommandSilent(`gamerule ${r}`))
})
```

### 1.2 Mods de contrôle des spawns

| Mod | Slug / preuve | 1.21.1 NeoForge Modrinth | Côté | Rôle | Verdict |
|---|---|---|---|---|---|
| **In Control!** (McJty) | projet `KpICtuVx` — [v1.21-10.2.6](https://modrinth.com/project/KpICtuVx/version/TBI4EWjs) ; changelog [GitHub branche `1.21_neo`](https://github.com/McJtyMods/InControl) jusqu'à 10.3.0 | **Oui** (10.2.6, bêta) | S | Règles `spawn.json` et `spawner.json` : mob, mod, biome et tags de biome, lumière, hauteur, ciel visible, météo, structure, `mincount`/`maxcount`, **`mindist`/`maxdist` au joueur le plus proche** (ajouté en 10.2.5), `random`, phases. Depuis 10.3.0, **conditions sur les données persistantes KubeJS**. 10.1.3 ajoute une action `command` | **Recommandé, quasi socle.** C'est le seul outil qui plafonne les hostiles moddés par mod ou par joueur et les écarte de la lumière sans écrire 300 lignes de JS. Serveur uniquement |
| Spawn Balance Utility | [CurseForge](https://www.curseforge.com/minecraft/mc-mods/spawn-balance-utility/files/7341253) | **Non** (Fabric 1.21.5–1.21.11) | — | Poids de spawn par biome et structure | **Écarté** |
| NeoForge natif (`remove_spawns`, `add_spawns`, `add_spawn_costs`, `remove_features`, `none`) | Doc NeoForge | Intégré | S | Ajouts et retraits sans mod. `add_spawn_costs` (`energy_budget`/`charge`) **espace les individus d'une même espèce**, idéal pour les prédateurs territoriaux | **Socle** (déjà disponible via KubeJS `data/`) |
| KubeJS (déjà présent) | — | Oui | S | Filet de sécurité, remplacements de butin, gamerules, ponts de données | **Socle** |

**Règles In Control proposées** (`config/incontrol/spawn.json`). La sémantique de `mincount` et `maxdist` est à confirmer : dans la doc McJty, une règle `deny` se déclenche quand toutes ses conditions sont vraies.
```json
[
  { "dimension": "minecraft:overworld", "mob": ["minecraft:zombie","minecraft:skeleton","minecraft:creeper","minecraft:spider","minecraft:enderman","minecraft:witch","minecraft:slime","minecraft:guardian","minecraft:pillager"], "result": "deny" },

  { "mod": "mowziesmobs", "minlight": 8, "result": "deny" },
  { "mod": "mowziesmobs", "maxdist": 40, "result": "deny" },
  { "mod": "mowziesmobs", "mincount": { "amount": 2, "mod": "mowziesmobs", "perplayer": true }, "result": "deny" },

  { "mob": ["alexsmobs:cave_centipede","alexsmobs:rocky_roller","alexsmobs:guster"], "minlight": 8, "result": "deny" },
  { "mob": ["alexsmobs:cave_centipede","alexsmobs:rocky_roller","alexsmobs:guster"], "mincount": { "amount": 3, "perplayer": true, "hostile": true }, "result": "deny" },

  { "mob": ["friendsandfoes:mauler","friendsandfoes:barnacle"], "random": 0.6, "result": "deny" },
  { "hostile": true, "dimension": "minecraft:overworld", "mincount": { "amount": 8, "perplayer": true, "hostile": true }, "result": "deny" }
]
```
Lecture : aucune anomalie dans la lumière (≥ 8), ni à moins de 40 blocs d'un joueur. Elles apparaissent donc *au loin, dans la brume*, et on les voit venir. Au plus 2 créatures de Mowzie's par joueur, et un plafond global de 8 hostiles par joueur qui remplace la cap vanilla de 70.

### 1.3 Ressources vanilla perdues → remplacements KubeJS (sous-projet 3)

| Ressource | Usage dans le pack | Source perdue | Remplacement proposé |
|---|---|---|---|
| **Slime ball** | Super Glue, Sticker, sticky piston de Create (essentiel pour Aeronautics), laisse (escorte des survivants) | Slime | Mélange Create : kelp + farine (KC/F&C) + eau, ou résine d'arbre via Farm & Charm. À vérifier dans le jar d'AM : la *banana slug slime* sert-elle de slime ? |
| **Blaze vivant / blaze rod** | **Blaze Burner** de Create (capture d'un blaze → mélange chauffé → **laiton**), alambic | Blaze (Nether) | Décision Nether à prendre. Sinon, recette KubeJS d'un Blaze Burner « allumé » (Overgeared : forge + charbon + noyau de magma) |
| Poudre à canon | TNT, fusées, Supplementaries | Creeper, sorcière, ghast | Mélange Create : charbon de bois + salpêtre (nouvel item, ou gravier lavé) + soufre |
| Ficelle | Arcs, laine, cannes à pêche | Araignée | Broyage de la laine dans Create (vanilla Create) ; lin de Supplementaries → ficelle |
| Os / poudre d'os | Agriculture | Squelette | Poissons, composteur ; ajouter `minecraft:bone` aux tables de butin des grands animaux Naturalist/AM (surcharge de `data/<mod>/loot_table/entities/*.json`) |
| Phantom membrane | Potion de chute lente, réparation d'Elytra (hors pack) | Phantom | À vérifier : une recette Aeronautics l'utilise-t-elle (parachute, voile) ? |
| Prismarine, éponge | Déco, drainage | Gardiens | Les monuments vidés restent des ruines sous-marines pillables ; l'éponge reste dans les salles |
| Perle de l'Ender | — (End supprimé) | Enderman | Rien, sauf si une recette moddée l'exige (à contrôler par un grep sur les recettes des jars) |

---

## 2. Table d'apparition par biome (proposition)

Elle est construite par **familles de tags** (`#minecraft:is_*`, `#c:is_*`) pour s'appliquer aux biomes de No Man's Land sans les connaître un par un. Les espèces sont à recenser dans les jars, surtout Naturalist 2.x (47 animaux d'après sa page) et Hybrid Aquatic. **Règle de dédoublonnage : une espèce = un mod.**

| Famille | Faune de base | Complément | Petite faune / compagnons | Anomalies et territoriaux (rares, plafonnés) | Ambiance (brume ATMOSPHERICS, sons) |
|---|---|---|---|---|---|
| Forêts tempérées, bouleaux | Naturalist : cerf, ours, renard et lapin forestiers, oiseaux, papillons, escargots | AM : raton laveur, corbeau, colibri | C&C (furet, coccinelle, scarabée), canards (UDM) | Rascal de F&F en mineshaft | Brume matinale légère, oiseaux le jour, grillons la nuit |
| Forêt noire | Naturalist : ours, lucioles | — | Ghost (petits fantômes) | **Lantern de Mowzie's** (passive, lumineuse : *étrangeté sans horreur*) | Brume épaisse permanente, sons étouffés |
| Taïga, forêts enneigées | Naturalist : cerf, renard | AM : élan, léopard des neiges | Pingouin de F&F (côtes glacées) | Frostmaw de Mowzie's (**très** rare) ; Froststalker d'AM (meute de 2 au plus) | Vent, neige d'Ecliptic |
| Marais, mangroves | Naturalist : alligator, lucioles, libellules | AM : grenouille de pluie, mudskipper | Canards et oies (UDM) | — | Brume dense au sol, coassements, gouttes |
| Rivières, lacs | Naturalist : bass, poisson-chat | AM : terrapin | C&C : loutre, koï | — | Brume de rivière à l'aube (ATMOSPHERICS, réglage jour/nuit) |
| Plaines, prairies | Naturalist : oiseaux, serpents | AM : bison | Moobloom de F&F (prairies fleuries, rare) | — | Vent, insectes |
| Savane | Naturalist : lion, zèbre, girafe, rhinocéros, hippopotame, vautour | **AM : éléphant** (monture à coffre = caravane) | — | Mauler de F&F (poids 32 → 8) | Chaleur, cigales |
| Désert, badlands | Naturalist : lézard, tortue terrestre, serpent à sonnette | AM : roadrunner, gerboise | — | Guster d'AM (tempête de sable vivante), Mauler | Vent sec, sable |
| Jungle | Naturalist : serpents | AM : gorille, tigre, toucan, anaconda | — | Foliaath de Mowzie's ; Umvuthana / sun temple : **à discuter** | Brume humide, pluie (Ecliptic) |
| Montagnes, pics | — | AM : pygargue à tête blanche, **Sunbird** (rarissime) | — | **Sculptor** de Mowzie's (structure de sommet), Naga sur les falaises côtières | Vent fort, horizon lointain (Distant Horizons à l'étude) |
| Océans, côtes | Hybrid Aquatic (base marine) | AM : orque, cachalot *si absents d'Hybrid* | Crabe de F&F (plages) | Requins Hybrid (territoriaux), Barnacle de F&F (rare) | Ressac, mouettes |
| Grand fond | Hybrid Aquatic (créatures abyssales) | AM : calmar géant *si absent d'Hybrid* | — | Karkinos et créatures profondes d'Hybrid | Silence, sons graves (AmbientSounds) |
| Grottes (luxuriantes, stalagmites) | — | **Underminer d'AM** (fantôme mineur passif), Rocky Roller | Glare de F&F (repère l'obscurité : synergie avec More Darkness) | Grottol de Mowzie's (fuit ; lâche un diamant), mille-pattes d'AM, Wroughtnaut de Mowzie's (salle-structure) | Gouttes, écho (Sound Physics) |
| Champignons | — | AM : Bunfungus (rare) | — | — | Brume colorée |

**Doublons à trancher (une seule version par espèce)** :

| Espèce | À garder | À couper | Clé connue |
|---|---|---|---|
| Canard | Untitled Duck Mod | Naturalist | `naturalist-common` → `disable_mobs.duck_removed = true` |
| Ours | Naturalist | Grizzly d'AM | Config AM Continued (clé à lire dans le jar) |
| Crocodiliens | Alligator de Naturalist | Crocodile d'AM | idem |
| Serpent à sonnette | Naturalist (`snake_removed` couvre aussi le serpent corail) | AM | idem |
| Éléphant | **AM** (mécanique de caravane) | Naturalist | `elephant_removed = true` |
| Rhinocéros | Naturalist | AM | — |
| Poisson-chat | Naturalist | AM | — |
| Geai bleu | Naturalist (`bird_removed` coupe *tous* ses oiseaux, ne pas l'activer) | AM | — |
| Requin-marteau, calmar, baudroie | Hybrid Aquatic | AM ; Naturalist 2.0 (baudroie) | Baudroie de Naturalist : clé 2.x à confirmer dans le jar |
| Pingouin | F&F (`enablePenguinSpawn`) **ou** Naturalist 2.x s'il en ajoute | l'autre | — |
| Lucioles | Entités Naturalist dans les marais (capturables en bocal Supplementaries) | Particules Subtle Effects/Particular dans les marais | — |
| Papillons | Naturalist (pollinisation des cultures = mécanique) | Longwings si ajouté (ou l'inverse) | — |

**Créatures d'AM à couper pour la DA** (horreur, comique, End/Nether hors sujet) : Murmur, Farseer, Skelewag, Mouche, Cafard (gag des maracas), et par sécurité toutes les créatures de l'End (Void Worm, Mimicube, Cosmaw, Enderiophage, Spectre…). Les créatures du Nether dépendent de la décision Nether.

**Réglages des mods déjà retenus (clés vérifiées sur les sources GitHub publiques)**

- **Naturalist** (`NaturalistConfig`, section `disable_mobs`, booléens `*_removed`, faux par défaut) : `alligator, bass, bear, bird, boar, butterfly, catfish, deer, dragonfly, duck, elephant, firefly, forest_fox, forest_rabbit, giraffe, hippo, lion, lizard, rhino, snail, snake, tortoise, vulture, zebra`. La config n'a **aucun poids de spawn** : la rareté se règle par In Control ou `add_spawn_costs`. ⚠️ Ces clés viennent de la branche GitHub `1.21.1-NeoForge`, qui liste 24 animaux. La 2.0.4 annonce 47 animaux : nouvelles clés probables (baudroie, fourmis…), à lire dans le jar.
- **Friends&Foes** (`FriendsAndFoesConfig`, branche master, proche de la 4.0.27) :
  - **Couper** : `enableIceologer=false`, `enableIceologerSpawn=false`, `enableIceologerInRaids=false`, `generateIceologerCabinStructure=false` ; `enableIllusioner=false`, `enableIllusionerInRaids=false`, `generateIllusionerShackStructure=false`, `generateIllusionerTrainingGroundsStructure=false` ; `enableWildfire=false`, `generateCitadelStructure=false` ; `enableZombieHorseTrap=false` ; `enableBeekeeperVillagerProfession=false` et `generateBeekeeperAreaStructureInVillages=false` (pas de villages intacts) ; `generateCopperGolemWorkstationStructureInVillages=false`.
  - **Garder et doser** : Crab (`crabSpawnWeight` 14 → 6) ; Glare (`enableGlareGriefing` : à évaluer) ; Rascal (4) ; Moobloom (4 → 2) ; Penguin ; Mauler (`maulerSavannaSpawnWeight` 32 → 8, `maulerBadlandsSpawnWeight` 16 → 6, `maulerDesertSpawnWeight` 8 → 4) ; Barnacle (4 → 2).
  - **À noter** : `generateTuffGolemInStronghold` ne sert plus à rien, puisque les strongholds sont supprimés. Le Tuff Golem devient introuvable : prévoir une autre source ou un butin. `generateCopperGolemInAncientCity` et `enableCopperGolemsInTrialChambers` dépendent du sort de ces structures.
- **Mowzie's Mobs** (`mowziesmobs-common.toml`, dépôt source non public, noms de mémoire **à confirmer**) :
  - par créature, `spawn_config` (`spawn_rate`, taille de groupe, `biome_config` avec tags, liste blanche et liste noire, `dimensions`, hauteur min/max, besoin d'obscurité) ;
  - par structure, `generation_config` (distance et séparation, biomes) ;
  - `combat_config` (multiplicateurs de vie et de dégâts).

  Proposition :
  - Lantern et Grottol : rares mais présents ;
  - Foliaath : jungle seulement ;
  - Naga : falaises côtières, ×0,3 ;
  - Frostmaw : ×0,2 ;
  - structures (Wroughtnaut, sun temple, Sculptor) : laissées à Sparse Structures ;
  - Umvuthana : décision DA (tribus masquées hostiles) ;
  - Bilokosa (nouveauté 1.8) : à identifier dans le jar.
- **Alex's Mobs Continued** (CodxLib, pas Citadel) : config par créature (poids, et probablement des fichiers de biomes par créature comme dans AM d'origine). Structure **à lire dans le jar 2.2.2** ; c'est l'élément le plus incertain.
- **Hybrid Aquatic, C&C, UDM, Ghost** : poids et toggles par créature probables, non vérifiés.

---

## 3. Mods candidats complémentaires (36 évalués)

Légende de disponibilité : **Oui** = page de version Modrinth 1.21.1 NeoForge vue dans les résultats ; **Probable** = miroir Modrinth ou index tiers ; **Non vérifié** = rien de concluant (le quota de recherche était épuisé).

### 3.1 Petite faune d'ambiance, oiseaux, insectes

| # | Mod (slug) | Preuve | 1.21.1 NeoForge Modrinth | Côté / dépendances | Effet et intégration | Conflits, réglages | Verdict |
|---|---|---|---|---|---|---|---|
| 1 | **Cosy Critters & Creepy Crawlies** (`cosy-critters`, PigCart) | [v0.2.0+1.21.1-neoforge](https://modrinth.com/mod/cosy-critters/version/v0.2.0+1.21.1-neoforge) | **Oui** | C, aucune dépendance | Oiseaux-particules qui se rassemblent dans le ciel, se posent et **s'envolent quand un véhicule, un mob ou un projectile approche**. **Papillons de nuit autour des sources de lumière exposées**. Petites araignées qui sortent des toiles. Silhouettes au loin, base = îlot de lumière : en plein dans la DA, sans coût serveur | Doublon partiel avec les oiseaux Naturalist (qui sont des entités) : les deux se complètent (particules au loin, entités au sol). Chaque effet se coupe (`/cosycritters`) | **Recommandé** |
| 2 | World Alive | [Modrinth](https://modrinth.com/mod/world-alive) | **Non** (Fabric 1.21.11/26.x) | C | Dérivé de Cosy Critters : papillons et oiseaux par biome | — | Écarté (indisponible) ; à surveiller |
| 3 | Atmospheric Fauna | [Modrinth `yd14I2wN`](https://modrinth.com/mod/yd14I2wN) | **Non** (Fabric) | C | Corbeaux-particules | — | Écarté |
| 4 | **Longwings** | [version Modrinth](https://modrinth.com/mod/longwings/version/h13ko2fP), [CurseForge](https://www.curseforge.com/minecraft/mc-mods/longwings/files/7997964) | **Oui** (0.9.5-neoforge, avril 2026) | CS | Plus de 70 papillons et papillons de nuit à rareté et habitats réalistes, **manuel de collection**, filets, mangeoire à eau sucrée. Fait de la collection un vrai passe-temps de naturaliste (zoo, vivarium) | Doublon des papillons Naturalist (couper l'un des deux, Naturalist pollinise). ARR, mais l'auteur autorise les modpacks avec crédit | **Optionnel** (branche faune) |
| 5 | Neo Guan Niao – Bird Watching | [Modrinth `neoguanniao`](https://modrinth.com/mod/neoguanniao) | **Probable** | CS | Oiseaux (perruche, moineau, bihoreau, tourterelle, pigeon) avec œufs, reproduction, **guide d'observation**, économie de plumes | Moineau en doublon de Naturalist ; espèces asiatiques ; peu de contenu | Optionnel faible |
| 6 | Backyard Birds | [Modrinth `pNndCV1F`](https://modrinth.com/project/pNndCV1F), [ModDex](https://moddex.gg/mod/backyard-birds) | **Non vérifié** (ModDex indique Forge) | CS | Plus de 60 oiseaux, nichoirs, mangeoires, bains, **jumelles**, progression technique à base de corbeaux | Les jumelles servent l'exploration, mais la progression technique fait doublon avec Create. ARR, code fermé | À vérifier (versions), sinon écarté |
| 7 | Fowl Play | [Modrinth `fowlplay`](https://modrinth.com/mod/fowlplay), [Mado](https://www.mado-hosting.com/minecraft-mods/fowlplay) | **Probable** (1.2.3, août 2026) | CS | Rouge-gorge, corbeau, mouette, pigeon, pingouin aux comportements de vol réalistes | L'auteur prévient : « en développement, peut **corrompre les mondes** ». Incompatible avec un monde permanent | **Écarté** |
| 8 | Realistic Bird Behavior | [BoxToPlay](https://www.boxtoplay.com/en/minecraft-hosting/minecraft-server/modrinth-mods/realistic-bird-behavior-39225) | Probable | S | Perroquets reproductibles | Gain marginal | Écarté |
| 9 | Glimmering Gardens | [ModDex](https://moddex.gg/mod/glimmering-gardens) | Non vérifié | CS | Lucioles-entités (marais, bouleaux, prairies), capturables en bouteille | Doublon des lucioles Naturalist et Subtle Effects | Écarté |
| 10 | Firefly Bush Backport | [Mado](https://www.mado-hosting.com/minecraft-mods/firefly-bush-backport) | Probable (v1.1.1) | S | Buisson à lucioles de la 1.21.5 (particules et son la nuit) | Léger, vanilla-like ; recoupe Subtle Effects | Optionnel faible |
| 11 | Vanilla Fireflies (datapack) | [Modrinth](https://modrinth.com/mod/vanilla-fireflies/version/YgsKRgbd) | Oui mais **archivé** | S | — | — | Écarté |
| 12 | Leaf Litter | [ModpackIndex](https://www.modpackindex.com/mod/98031/leaf-litter) | Non vérifié | CS | Litière de feuilles 1.21.5, sans génération naturelle | Simple déco | Écarté |

### 3.2 Grande faune et créatures rares (en dehors des mods retenus)

| # | Mod | Preuve | Dispo | Effet | Verdict |
|---|---|---|---|---|---|
| 13 | Whisperwoods (feux follets, papillons de nuit, **Hirschgeist**, Hidebehind) | [Modrinth](https://modrinth.com/mod/whisperwoods) | **Non** (1.20.1 Forge/Fabric) | La DA idéale (cerf-esprit dans la brume), mais pas de portage 1.21.1 | Écarté ; **à surveiller** |
| 14 | Wisp & Willow | [CurseForge](https://www.curseforge.com/minecraft/mc-mods/wisp-willow), [ModDex (fiche Modrinth)](https://moddex.gg/mod/wisp-willow-mr-a6zCFWRP) | Probable (NeoForge 1.21.x, 1.4.2) | Magie des esprits de la vieille forêt (autel, craie, grimoire, rituels) | Écarté : ajoute un **système de magie** hors des trois piliers |
| 15 | Mo' Creatures Reborn / Neo Mo' Creatures / Mo' Creatures Port | [ModpackIndex](https://www.modpackindex.com/mod/206150/mo-creatures-reborn-neoforge) | Non vérifié (portages récents, peu téléchargés) | Bestiaire de DrZhark | Écarté : doublons, immaturité |
| 16 | ZAWA | [CurseForge](https://www.curseforge.com/minecraft/mc-mods/zoo-wild-animals-rebuild/files/2989666) | **Non** (1.12.2) | Zoo réaliste | Écarté |
| 17 | AND / Thero's Wilds | [Mado](https://www.mado-hosting.com/minecraft-mods/and) | Non vérifié, WIP | Animaux menacés, faune variée | Écarté |
| 18 | Wan's Ancient Beasts | [ModDex](https://moddex.gg/mod/wans-ancient-beasts) | Bêta | Préhistoire | Écarté (préhistoire abandonnée) |
| 19 | Bosses of Mass Destruction / L_Ender's Cataclysm / Ice and Fire / Born in Chaos / Deeper and Darker | — | Non vérifié pour BoMD ; les autres existent mais sont hors DA | Boss RPG, dragons, horreur, dimension sculk | **Écartés** (RPG/horreur). Mowzie's couvre déjà les « boss discrets » |

**Conclusion faune** : le bestiaire retenu suffit largement. Les vraies lacunes sont **l'ambiance vivante sans coût serveur** (Cosy Critters) et l'**étrangeté non horrifique**, déjà présente dans les mods retenus : Lantern, Grottol, Underminer, Sunbird, Rascal, Glare, Ghost. Il faut la **mettre en scène par la rareté** plutôt qu'ajouter un mod.

### 3.3 Météo, ciel, célestes

| # | Mod | Preuve | Dispo | Côté / dépendances | Effet | Conflits, réglages | Verdict |
|---|---|---|---|---|---|---|---|
| 20 | **Enhanced Celestials** (`enhanced-celestials`, CorgiTaco) | [6.0.2.5-neoforge](https://modrinth.com/mod/enhanced-celestials/version/6.0.2.5-neoforge) | **Oui** | CS ; CorgiLib + Data Anchor | Événements lunaires pilotés par datapack : **lune de moisson** (croissance des cultures), lune bleue (chance), super-lunes, **lune de sang** (+ spawns). Chaque événement règle couleurs, effets, spawns, sommeil et butin | **Désactiver la lune de sang** (horde, ton horreur). Garder moisson et lune bleue (synergie avec l'agriculture et Ecliptic). Créer en datapack une « Lune pâle » : ciel décoloré, Lantern et Ghost plus fréquents, aucun hostile en plus. À tester : interaction avec More Darkness (luminosité lunaire) et avec la durée du jour d'Ecliptic. Le **dépôt GitHub est archivé** : la ligne active est Enhanced Celestials 2 | **Recommandé** |
| 21 | Enhanced Celestials 2 (Core + Default Lunar Events) | [ModpackIndex](https://www.modpackindex.com/mod/166913/enhanced-celestials-2-core) | Non vérifié sur Modrinth (NeoForge 1.21.1 d'après l'index) | CS | Réécriture du planificateur et du chargeur de datapacks | À préférer à la v1 si disponible sur Modrinth | À vérifier |
| 22 | **Simple Clouds** (`simple-clouds`) | [Modrinth](https://modrinth.com/project/simple-clouds), [GitHub](https://github.com/nonamecrackers2/simple-clouds) | **Oui** (0.7.3+1.21.1) | C ou CS | Types de nuages générés sur GPU, **orages qui assombrissent l'horizon et se voient de loin**, foudre et tonnerre retardé. Météo localisée avec le mod côté serveur. Éditeur de nuages exportable en datapack | **Compute shaders → OpenGL 4.3 : très probablement incompatible macOS** (à vérifier chez chaque joueur). Bêta, coût en FPS. Avec le mod côté serveur, la météo localisée remplace la météo globale : **conflit avec Ecliptic Seasons**. Proposition : **client seul** (Ecliptic garde la météo). Recoupe les nuages d'ATMOSPHERICS (en couper un). Iris non requis | **Optionnel, branche dédiée** |
| 23 | Weather, Storms & Tornadoes (Weather2, `weather-storms-tornadoes`) | [Modrinth 1.21.0-2.8.7](https://modrinth.com/mod/weather-storms-tornadoes/version/YuNAxTMO) | **Oui** | CS ; CoroUtil | Fronts orageux, tornades, tempêtes de sable, vent. La « survie par l'environnement » au sens propre | **Prend le contrôle de la météo → conflit frontal avec Ecliptic**, doublons avec Particle Rain et Simple Clouds, coût serveur. Réaction des engins Sable/Aeronautics aux tornades inconnue | **Écarté pour l'instant** (à rouvrir si Ecliptic tombe) |
| 24 | Expanded Weather2 Dynamics | [CurseForge](https://www.curseforge.com/minecraft/mc-mods/expanded-weather2-dynamics) | Non vérifié, alpha | CS | Extension de Weather2 | — | Écarté |
| 25 | Particle Rain (*déjà en attente*) | [v4-beta.6 Modrinth](https://modrinth.com/mod/particle-rain/version/v4-beta.6+1.21.1-neoforge), [beta.10 CurseForge](https://www.curseforge.com/minecraft/mc-mods/particle-rain/files/8055433) | Oui (beta.6 ; beta.10 de mai 2026 confirmée sur CurseForge seulement) | C | Pluie et neige en particules. La beta.10 **rétablit un effet de brume (« haze ») désactivé par défaut** et laisse passer la pluie à travers barrières et clôtures | Purement visuel : compatible avec Ecliptic tant que celui-ci décide de la météo. Activer la brume pour les orages, vérifier qu'il n'y a pas de double rendu de neige avec Ecliptic | Recommandé à tester |

Aurores et éclipses : **non vérifié** (quota épuisé). Je ne connais aucun mod NeoForge 1.21.1 confirmé ; un événement Enhanced Celestials coloré (« ciel vert » en biome enneigé) peut servir d'ersatz.

### 3.4 Sons, voix, musique

| # | Mod | Preuve | Dispo | Côté | Effet | Conflits, réglages | Verdict |
|---|---|---|---|---|---|---|---|
| 26 | **Simple Voice Chat** (`simple-voice-chat`) | [BoxToPlay (miroir Modrinth)](https://www.boxtoplay.com/en-ba/minecraft-hosting/minecraft-server/modrinth-mods/simple-voice-chat-9) | **Oui** (1.21.1-2.6.x) | CS ; port **UDP 24454** à ouvrir sur le serveur dédié | Voix de proximité : on s'entend à distance réelle, la voix porte dans les vallées. Sound Physics Remastered est « optimisé pour SVC » (réverbération et occlusion sur la voix) | Rien à désactiver. Groupes possibles pour les expéditions lointaines. Idée Create : radios (addon à rechercher, non vérifié) | **Recommandé** |
| 27 | **Presence Not Position** | [ModDex](https://moddex.gg/mod/presence-not-position), [ModpackIndex](https://www.modpackindex.com/mod/175210/presence-not-position) | **Non vérifié** (NeoForge 1.21.1, 1.2.1 ; fiche Modrinth non trouvée) | CS | Détecte l'entrée dans une structure, un biome, une dimension ou **« la maison »** et affiche titre, illustration, son et **musique adaptative**, via resource pack ou KubeJS. Un « lieu découvert » à la Breath of the Wild, et un thème musical de la base (l'îlot) | Les titres ne doivent pas révéler les structures : n'afficher que des noms poétiques, ou seulement la musique. Mod très jeune et confidentiel | **Recommandé si présent sur Modrinth**, sinon à surveiller |
| 28 | Biome Music | [CurseForge 3.9](https://www.curseforge.com/minecraft/mc-mods/biome-music/files/8302329) | Non vérifié sur Modrinth (NeoForge 1.21.1, juin 2026) | C | Musique vanilla par biome, détection des grottes | Recoupe Presence Not Position | Optionnel (repli) |
| 29 | Dynamic Music Reneoforged | [CurseForge](https://www.curseforge.com/minecraft/mc-mods/dynamic-music-reneoforged) | Non vérifié | C | Pools de musique contextuels (`SelectMusicEvent`) | Environ 9 téléchargements | Écarté |
| 30 | Fight Music | [CurseForge](https://www.curseforge.com/minecraft/mc-mods/fight-music) | — | C | Musique de combat | Contraire au ton | Écarté |
| 31 | Dynamic Surroundings (OreCruncher) | [GitHub](https://github.com/OreCruncher/DynamicSurroundingsFabric), [version Modrinth](https://modrinth.com/project/H7fshfpD/version/aFsN201L) | **Oui** (0.4.2, janvier 2025) ; Architectury | C | Ambiances, pas, cascades, vapeur | Doublon d'AmbientSounds + Subtle Effects (+ HUD à couper) ; peu maintenu | Écarté |
| 32 | Dynamic Surroundings Rebirth | [CurseForge](https://www.curseforge.com/minecraft/mc-mods/dynamic-surroundings-rebirth) | Non vérifié | C | Portage communautaire | idem | Écarté |
| 33 | *(info nouvelle)* Presence Footsteps x Sable | [Mado](https://www.mado-hosting.com/minecraft-mods/presence-footsteps-x-sable), [PF NeoForge](https://modrinth.com/project/JIEwmDVI/version/f3SxKzof) | Probable (1.0, NeoForge 1.21.1, MIT) | C | Patch : les bruits de pas de Presence Footsteps jouent **sur les engins Sable et Aeronautics** | PF a été écarté en 0007. Ce patch est une **donnée nouvelle** si la décision est rouverte | Information seulement |

Idée AmbientSounds (retenu) : ses ambiances sont définies en JSON de resource pack, avec des conditions de biome, d'heure, d'altitude, de pluie et de lumière. On peut y ajouter des **sons lointains rares et non identifiables**, sans jumpscare : grincement métallique près des ruines NML, cloche étouffée dans la brume de marais, chant de baleine au large la nuit, sifflet de train fantôme très lointain. C'est le moyen le moins cher de créer une « étrangeté diffuse ».

### 3.5 Éclairage et particules

| # | Mod | Preuve | Dispo | Effet | Verdict |
|---|---|---|---|---|---|
| 34 | Sodium Dynamic Lights | [version Modrinth](https://modrinth.com/mod/sodium-dynamic-lights/version/XI0WLXdn) | Oui (NeoForge 1.0.10) | Lumière dynamique branchée sur Sodium | **Repli** si LambDynamicLights casse avec Sodium 0.8.13. LDL 4.8.9 était annoncée comme la dernière pour 1.21, mais le pack a déjà la 4.8.11 : maintenance minimale |
| 35 | True Darkness Biomes / Truly Dark / DarkAroundMe | [Mado](https://www.mado-hosting.com/minecraft-mods/true-darkness-biomes), [ModpackIndex](https://www.modpackindex.com/mod/105503/truly-dark) | Probable (NeoForge 1.21.1) | Obscurité réelle (par biome pour TDB) | Écartés : doublons de More Darkness. TDB à garder en tête si More Darkness déçoit (obscurité **par biome** : forêt noire plus sombre) |
| 36 | Particular ✨ Reforged | [CurseForge 1.5.5 (juin 2026)](https://www.curseforge.com/minecraft/mc-mods/particular-reforged/files/8275091) | **Non vérifié sur Modrinth** (seul un vieux projet « Particular » 1.1.4 y apparaît) | **Cascades** d'écume au pied des chutes, éclaboussures, lucioles configurables (biomes, blocs, couleurs), feuilles qui tombent, poussière | Optionnel si présent sur Modrinth. Recoupe Subtle Effects (couper les effets en double) et Falling Leaves (déjà écarté) |

Idée LambDynamicLights (retenu) : la 4.x accepte des **luminances d'entités définies par resource pack**. On peut faire éclairer réellement le Lantern de Mowzie's, les lucioles Naturalist et le Glare. La rencontre devient un îlot de lumière mobile dans la nuit noire de More Darkness.

### 3.6 Compatibilité avec le rendu (Iris/Oculus, Sodium)

Aucun candidat **n'exige Iris ni Oculus**. Points d'attention :
- **Simple Clouds** : compute shaders (macOS exclu, probablement), et non compatible avec les packs de shaders.
- **ATMOSPHERICS** : ne pas activer « Improved Transparency » dans Sodium (déjà noté).
- **LambDynamicLights et Sodium Dynamic Lights** : dépendent de l'API de Sodium. Tester avec Sodium 0.8.13.
- **Particle Rain, Cosy Critters, Enhanced Celestials** : simple rendu de particules et de ciel, pas de dépendance à Sodium connue.
- **Distant Horizons** (à l'étude) : son propre brouillard lointain chevauche celui d'ATMOSPHERICS. À régler ensemble.

---

## 4. Système cohérent proposé

**Écologie (serveur).**
1. Un datapack `fallow` retire les hostiles vanilla des biomes, les donjons et les blocs infestés.
2. La surcharge `neoforge:none` neutralise les modifiers indésirables d'autres mods.
3. `add_spawn_costs` espace les prédateurs territoriaux : ours, lions, tigres, requins.
4. **In Control!** :
   - interdit les anomalies dans la lumière (≥ 8) et à moins de 40 blocs d'un joueur ;
   - plafonne Mowzie's à 2 par joueur ;
   - plafonne les hostiles à 8 par joueur, au lieu des 70 de la cap vanilla.
5. Le **filet KubeJS** annule toute entité vanilla bannie qui entre dans l'Overworld (conversions, entités placées à la génération, overrides) et fixe les gamerules.
6. Les configs des mods coupent les doublons (table du §2) et les créatures hors DA.

**Vie visible (client).**
- Naturalist, AM, Hybrid et les compagnons au sol ;
- **Cosy Critters** dans le ciel et autour des lampes ;
- Subtle Effects pour les détails ;
- Longwings en option pour les collectionneurs.

**Atmosphère (client).**
- **ATMOSPHERICS** fait la brume par biome, avec des presets jour et nuit livrés dans `pack/config` ;
- **More Darkness** : vraies nuits ;
- **LambDynamicLights** (lumière des torches et des créatures lumineuses) ;
- **AmbientSounds** enrichi d'ambiances rares et lointaines ;
- **Sound Physics Remastered** et **Simple Voice Chat** pour la voix dans l'espace ;
- **Particle Rain**, avec la brume d'orage activée.

**Phénomènes.**
- **Ecliptic Seasons** est le seul cerveau météo ;
- **Enhanced Celestials** : moisson, lune bleue et une « Lune pâle » maison ; lune de sang coupée ;
- **Presence Not Position** pour la musique des lieux et de la maison, s'il est sur Modrinth, sinon Biome Music ;
- **Simple Clouds** en client seul, sur une branche de test, pour les orages vus de loin.

**Structures vidées = ruines silencieuses.** Monuments sans gardiens, avant-postes sans pillards, manoirs sans illageois, cabanes sans sorcière, villages zombies sans zombies (concept des survivants). Ancient Cities et Trial Chambers sont à retirer via Sparse Structures, ou à garder vides avec le butin des coffres-coffrets à revoir en KubeJS.

**Désactivations récapitulées.**
- **Gamerules** : `doInsomnia`, `doPatrolSpawning`, `doWardenSpawning` et `doTraderSpawning` à `false`, `disableRaids` à `true`.
- **F&F** : Iceologer, Illusioner, Wildfire et Citadel, piège de chevaux zombies, apiculteur, structures de village.
- **Naturalist** : `duck_removed`, `elephant_removed`.
- **AM** : grizzly, crocodile, serpent à sonnette, rhinocéros, poisson-chat, geai bleu, requin-marteau, Murmur, Farseer, Skelewag, mouche, cafard, créatures de l'End.
- **Enhanced Celestials** : lune de sang.

---

## 5. Incertitudes à lever dans les .jar et en jeu

1. **Naturalist 2.0.4** : liste réelle des 47 animaux et nouvelles clés `*_removed` (le GitHub n'en montre que 24) ; pingouin, baudroie, fourmis ?
2. **Alex's Mobs Continued 2.2.2** (CodxLib) : format de la config de spawn (poids par créature ? fichiers de biomes ?), liste des créatures conservées par rapport à AM d'origine, *banana slug slime* → slime ball.
3. **Mowzie's 1.8.2** : noms exacts des clés `spawn_config`/`generation_config`, catégorie de spawn de chaque créature (MONSTER / CREATURE / AMBIENT), nature du **Bilokosa**, butin du Grottol (diamant).
4. **Hybrid Aquatic 1.7.3** : liste des espèces (cachalot, orque, calmar géant, baudroie ?) pour trancher avec AM ; config de spawn.
5. **Recettes** : grep des jars (Create, Aeronautics, Steam 'n' Rails, Overgeared, KC, F&C, Supplementaries) sur `slime_ball`, `blaze_rod`, `gunpowder`, `ender_pearl`, `phantom_membrane`, `string`, `bone`, pour dimensionner les remplacements KubeJS. **Blaze Burner et décision Nether en priorité.**
6. **KubeJS 2101.7** : nom et annulabilité de `EntityEvents.spawned`/`checkSpawn`, accès à `entity.tags` et au type de spawn (pour laisser passer œufs et commandes).
7. **In Control 10.x** : sémantique exacte de `mincount`/`maxcount` (avec `perplayer`) et de `mindist`/`maxdist` ; prise en compte des `spawn_overrides` de structures.
8. **Trial Chambers 1.21.1** : les configs des trial spawners sont dans les templates (pas encore en données comme en 1.21.2+). Les retirer est plus simple que les modifier.
9. **Enhanced Celestials** : v1 (archivée) ou v2 sur Modrinth 1.21.1 ? Interactions avec la durée du jour d'Ecliptic et avec More Darkness.
10. **Simple Clouds** : support macOS (OpenGL 4.3) chez chaque joueur ; superposition avec les nuages d'ATMOSPHERICS ; mode client seul + Ecliptic.
11. **Presence Not Position**, **Particular Reforged**, **Biome Music** : existence d'une fiche Modrinth 1.21.1 NeoForge (non trouvée).
12. **Non vérifiés faute de quota** (à relancer) : mods d'aurores et d'éclipses, torches qui s'éteignent (« îlot de lumière » à entretenir), Tide (pêche par espèce, saison et météo, avec carnet), Extreme Sound Muffler (usines Create bruyantes), radios ou talkies-walkies pour SVC, Bosses of Mass Destruction.

---

## Sources principales
- In Control : [Modrinth KpICtuVx](https://modrinth.com/project/KpICtuVx/version/TBI4EWjs), [GitHub McJtyMods/InControl](https://github.com/McJtyMods/InControl) (changelog `1.21_neo`)
- Naturalist : [GitHub Crispytwig/naturalist (1.21.1-NeoForge)](https://github.com/Crispytwig/naturalist), [BoxToPlay](https://www.boxtoplay.com/en/minecraft-hosting/minecraft-server/modrinth-mods/naturalist-117)
- Friends&Foes : [GitHub Faboslav/friends-and-foes](https://github.com/Faboslav/friends-and-foes)
- Mowzie's Mobs 1.8.2 : [Modrinth](https://modrinth.com/mod/mowzies-mobs/version/QfxYwK0i)
- Cosy Critters : [Modrinth](https://modrinth.com/mod/cosy-critters/version/v0.2.0+1.21.1-neoforge) ; World Alive : [Modrinth](https://modrinth.com/mod/world-alive)
- Longwings : [Modrinth](https://modrinth.com/mod/longwings/version/h13ko2fP) ; Neo Guan Niao : [Modrinth](https://modrinth.com/mod/neoguanniao) ; Backyard Birds : [Modrinth](https://modrinth.com/project/pNndCV1F) ; Fowl Play : [Modrinth](https://modrinth.com/mod/fowlplay)
- Enhanced Celestials : [Modrinth 6.0.2.5](https://modrinth.com/mod/enhanced-celestials/version/6.0.2.5-neoforge), [GitHub (archivé)](https://github.com/CorgiTaco/Enhanced-Celestials)
- Simple Clouds : [Modrinth](https://modrinth.com/project/simple-clouds), [GitHub](https://github.com/nonamecrackers2/simple-clouds)
- Weather2 : [Modrinth](https://modrinth.com/mod/weather-storms-tornadoes/version/YuNAxTMO)
- Particle Rain : [Modrinth](https://modrinth.com/mod/particle-rain/version/v4-beta.6+1.21.1-neoforge), [CurseForge](https://www.curseforge.com/minecraft/mc-mods/particle-rain/files/8055433)
- Dynamic Surroundings : [GitHub](https://github.com/OreCruncher/DynamicSurroundingsFabric)
- Presence Not Position : [ModDex](https://moddex.gg/mod/presence-not-position) ; Biome Music : [CurseForge](https://www.curseforge.com/minecraft/mc-mods/biome-music/files/8302329)
- LambDynamicLights : [Modrinth 4.8.9](https://modrinth.com/mod/lambdynamiclights/version/4.8.9+1.21.1) ; Sodium Dynamic Lights : [Modrinth](https://modrinth.com/mod/sodium-dynamic-lights/version/XI0WLXdn)
- Presence Footsteps x Sable : [Mado](https://www.mado-hosting.com/minecraft-mods/presence-footsteps-x-sable)
- Particular Reforged : [CurseForge](https://www.curseforge.com/minecraft/mc-mods/particular-reforged/files/8275091)
- Subtle Effects : [GitHub](https://github.com/MincraftEinstein/SubtleEffects)
