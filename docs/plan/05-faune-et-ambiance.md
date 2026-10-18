# 5. Faune, écologie et ambiance

Ce qui existe déjà (décisions 0003 et 0007) : Naturalist, Alex's Mobs Continued, Hybrid Aquatic, Mowzie's Mobs, six mods de compagnons et Companion ; ATMOSPHERICS, AmbientSounds, Sound Physics Remastered, Subtle Effects, LambDynamicLights, More Darkness. Le bestiaire suffit largement. Ce qui manque, c'est la **chaîne qui supprime les monstres vanilla**, le **contrôle des créatures moddées** une fois la place libérée, et quelques touches d'ambiance.

Détail, preuves et clés : [recherche/research-faune-ambiance.md](recherche/research-faune-ambiance.md).

## 5.1 Supprimer les monstres vanilla

Ils arrivent par six voies ; chacune a sa parade.

| Voie | Exemples | Parade |
|---|---|---|
| Listes de spawn des biomes | zombie, squelette, creeper, araignée, enderman, sorcière, slime, noyé, bogged | Biome modifier `neoforge:remove_spawns` sur `#minecraft:is_overworld` |
| `spawn_overrides` des structures | gardiens, sorcière de cabane, pillards d'avant-poste | Règles In Control! + filet KubeJS |
| Spawners | donjons, araignées des mines, trial spawners | `neoforge:remove_features` sur `monster_room` et `monster_room_deep` ; chambres d'épreuves retirées |
| Gamerules | phantoms, patrouilles, raids, warden, marchand | `doInsomnia false`, `doPatrolSpawning false`, `disableRaids true`, `doWardenSpawning false`, `doTraderSpawning false` |
| Conversions | villageois foudroyé → sorcière, piège de chevaux squelettes, `enableZombieHorseTrap` de Friends & Foes | Filet KubeJS + config F&F |
| Entités placées à la génération | illageois des manoirs, zombies des villages abandonnés, silverfish des blocs infestés | Filet KubeJS ; `remove_features` sur `ore_infested` |

Trois couches, de la moins chère à la plus fine :
1. **Datapack** (`kubejs/data/fallow/neoforge/biome_modifier/`) : retrait des spawns, des donjons et des blocs infestés. Pour neutraliser un biome modifier d'un autre mod sans toucher à sa config, on surcharge son fichier avec `{"type": "neoforge:none"}`.
2. **In Control!** (nouveau, socle, serveur seulement) pour les règles fines.
3. **Filet KubeJS** : toute entité vanilla bannie qui entre dans l'Overworld est annulée, sauf si elle porte le tag `fallow_allowed` (invocation par un admin).

## 5.2 Contrôler les créatures moddées

**Effet de bord majeur** : sans monstres vanilla, toute la limite d'hostiles (70) revient aux créatures moddées : créatures de Mowzie's, mille-pattes et Guster d'Alex's Mobs, Mauler et Barnacle de Friends & Foes, requins d'Hybrid Aquatic. Sans plafond, les « créatures rares » forment des hordes.

Règles In Control! proposées (`config/incontrol/spawn.json`, sémantique exacte à confirmer) :
- aucune créature hostile moddée à une **lumière ≥ 8** : la base est littéralement un îlot de lumière ;
- aucune anomalie Mowzie's à **moins de 40 blocs** d'un joueur : on les aperçoit au loin, dans la brume ;
- au plus **2 créatures de Mowzie's par joueur** ;
- au plus **8 hostiles par joueur** au total.

Pour espacer les prédateurs territoriaux (ours, lions, tigres, requins), le biome modifier `neoforge:add_spawn_costs` donne à chaque individu une « charge » qui repousse ses congénères.

## 5.3 Une espèce, un mod

Doublons à couper (table complète par biome dans la recherche) :

| Espèce | On garde | On coupe | Clé connue |
|---|---|---|---|
| Canard | Untitled Duck Mod | Naturalist | `disable_mobs.duck_removed = true` |
| Éléphant | Alex's Mobs (monture à coffre : une caravane) | Naturalist | `disable_mobs.elephant_removed = true` |
| Ours | Naturalist | Grizzly d'Alex's Mobs | à lire dans le .jar AM |
| Crocodiliens | Alligator de Naturalist | Crocodile d'AM | idem |
| Serpent à sonnette, rhinocéros, poisson-chat, geai bleu | Naturalist | AM | idem |
| Requin-marteau, calmar, baudroie | Hybrid Aquatic | AM ; baudroie de Naturalist 2.x | idem |
| Pingouin | Friends & Foes (ou Naturalist 2.x s'il en ajoute) | l'autre | — |

**Créatures d'Alex's Mobs à couper pour la direction artistique** : Murmur, Farseer, Skelewag, mouche, cafard, et toutes les créatures de l'End (Void Worm, Mimicube, Cosmaw, Enderiophage, Spectre…). Celles du Nether partent avec lui si le Nether est fermé ([3.8](03-civilisation.md#38-le-nether--fermé-par-défaut)).

**Friends & Foes** (clés lues dans le code source) :
- à couper : `enableIceologer`, `enableIceologerSpawn`, `enableIceologerInRaids`, `generateIceologerCabinStructure`, `enableIllusioner`, `enableIllusionerInRaids`, `generateIllusionerShackStructure`, `generateIllusionerTrainingGroundsStructure`, `enableWildfire`, `generateCitadelStructure`, `enableZombieHorseTrap`, `enableBeekeeperVillagerProfession`, `generateBeekeeperAreaStructureInVillages`, `generateCopperGolemWorkstationStructureInVillages` ;
- à doser : `crabSpawnWeight` 14 → 6, Moobloom 4 → 2, `maulerSavannaSpawnWeight` 32 → 8, `maulerBadlandsSpawnWeight` 16 → 6, `maulerDesertSpawnWeight` 8 → 4, Barnacle 4 → 2 ;
- à noter : le **Tuff Golem** n'apparaît que dans les strongholds, supprimés. Il faut lui donner une autre source (ruines, butin) ou l'accepter introuvable.

**Mowzie's Mobs** (clés à confirmer dans le .jar) : Lantern et Grottol rares mais présents ; Foliaath en jungle seulement ; Naga sur les falaises côtières (×0,3) ; Frostmaw ×0,2 ; les structures (Wroughtnaut, temple du soleil, Sculptor) passent par Sparse Structures. Les Umvuthana (tribus masquées hostiles) sont une décision de direction artistique.

**Adorable Hamster Pets** : flair à diamants à désactiver si la config le permet. **Companions!** : créatures hostiles, armes et boss désactivés ou rendus très rares (0003).

## 5.4 Ressources perdues avec les monstres

À remplacer en KubeJS **avant d'ouvrir le serveur** :

| Ressource | Pourquoi elle compte | Remplacement proposé |
|---|---|---|
| **Slime ball** | Super Glue, Sticker et piston collant de Create : indispensables à Aeronautics ; laisse des survivants | Mélange Create (varech + farine + eau) ; à vérifier : la *banana slug slime* d'Alex's Mobs peut-elle servir ? |
| **Blaze** | Blaze Burner de Create | Voir le Nether ([3.8](03-civilisation.md#38-le-nether--fermé-par-défaut)) |
| Poudre à canon | TNT, fusées | Mélange Create : charbon de bois, salpêtre (gravier lavé), soufre |
| Ficelle | Arcs, cannes à pêche | Laine broyée (Create) ; lin de Supplementaries |
| Os | Poudre d'os, agriculture | Ajoutés au butin des grands animaux Naturalist et AM |
| Membrane de phantom | Potion de chute lente | Vérifier si une recette Aeronautics l'utilise |

Un passage sur les recettes de tous les .jar (`slime_ball`, `blaze_rod`, `gunpowder`, `ender_pearl`, `phantom_membrane`, `string`, `bone`) dimensionnera la liste.

## 5.5 Ambiance : ce qu'on ajoute

| Mod | Effet | Dispo | Statut proposé |
|---|---|---|---|
| **Cosy Critters & Creepy Crawlies** | Oiseaux-particules qui se posent et s'envolent à l'approche ; papillons de nuit autour des lampes ; petites araignées des toiles. Silhouettes au loin, base = îlot de lumière, sans coût serveur | oui (client) | recommandé |
| **Enhanced Celestials** | Lune de moisson (croissance), lune bleue ; **lune de sang coupée** ; une « Lune pâle » maison en datapack (ciel décoloré, Lantern et Ghost plus fréquents, aucun hostile de plus) | oui (6.0.2.5 ; la v1 est archivée, vérifier la v2) | recommandé |
| **Simple Voice Chat** | Voix de proximité ; Sound Physics réverbère les voix dans les vallées et les grottes | oui | recommandé ([6](06-technique-et-serveur.md)) |
| **Reactive Music** | Remplace Presence Not Position (introuvable) et Biome Music (Fabric seulement). Musique choisie selon le lieu, l'heure, le danger, l'intérieur ou l'extérieur. Ses morceaux fantasy et celtiques ne collent pas forcément à Fallow : garder les règles, changer le songpack ou en écrire un sobre | oui (1.3.5, client, utilise YACL déjà présent) | recommandé ; à écouter en phase 6 |
| **Particle Rain** | Pluie et neige en particules, brume d'orage | déjà « à tester » | à tester, Ecliptic garde la météo |
| **Simple Clouds** | Orages qui assombrissent l'horizon, visibles de loin | oui (0.7.3) | branche dédiée, **client seul** (sinon conflit avec Ecliptic) ; compute shaders, probablement pas de macOS |
| Longwings | 70 papillons à collectionner, manuel, filets | oui | optionnel (doublon des papillons Naturalist) |

**Écartés** : Weather2 (prend le contrôle de la météo, conflit avec Ecliptic), Fowl Play (l'auteur prévient d'un risque de corruption des mondes), Whisperwoods (pas de version 1.21.1, à surveiller : c'est la direction artistique idéale), Wisp & Willow (système de magie), Dynamic Surroundings (doublon d'AmbientSounds), Spawn Balance Utility (Fabric seulement), True Darkness Biomes et Truly Dark (doublons de More Darkness).

**À savoir** : un patch « Presence Footsteps x Sable » existe pour NeoForge 1.21.1 et fait jouer les bruits de pas sur les engins. Presence Footsteps avait été écarté en 0007 ; c'est une donnée nouvelle si la décision est rouverte.

## 5.6 Mettre en scène l'étrangeté sans ajouter de mod

- **AmbientSounds** : ses ambiances sont des JSON de resource pack, avec conditions de biome, d'heure, d'altitude, de pluie et de lumière. On y ajoute des **sons lointains rares et non identifiables** : grincement métallique près des ruines, cloche étouffée dans la brume du marais, chant de baleine au large la nuit, sifflet de train très lointain. Aucun jumpscare.
- **LambDynamicLights** : luminances d'entités par resource pack. Le Lantern de Mowzie's, les lucioles de Naturalist et le Glare éclairent vraiment : un îlot de lumière mobile dans la nuit noire de More Darkness.
- **ATMOSPHERICS** : presets de brume par biome, jour et nuit, livrés dans `pack/config`. Ne pas activer « Improved Transparency » dans Sodium.
- **Structures vidées** : monuments sans gardiens, avant-postes sans pillards, cabanes sans sorcière, villages sans zombies. Des ruines silencieuses.

## 5.7 Réglages récapitulés

| Mod | Réglage |
|---|---|
| Naturalist | `duck_removed = true`, `elephant_removed = true` ; ne pas activer `bird_removed` (coupe tous les oiseaux). Aucun poids de spawn dans la config : la rareté passe par In Control! |
| Alex's Mobs Continued | Couper grizzly, crocodile, serpent à sonnette, rhinocéros, poisson-chat, geai bleu, requin-marteau, Murmur, Farseer, Skelewag, mouche, cafard, créatures de l'End (format de config à lire dans le .jar) |
| Friends & Foes | Voir 5.3 |
| Mowzie's Mobs | Voir 5.3 |
| Enhanced Celestials | Lune de sang désactivée ; « Lune pâle » ajoutée |
| Subtle Effects | Couper les effets superflus (0007) et les doublons de lucioles |
| Entity Culling | **Risque** : casse les engins Aeronautics ([6.1](06-technique-et-serveur.md#61-alertes)) |
