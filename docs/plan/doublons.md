# Doublons de faune : un seul modèle par espèce

Demande de l'équipe du 2026-10-19 : quand deux mods ont la même espèce, comparer les modèles et garder le meilleur. Aucune coupe pour raison de style : Companions! reste en entier. Untitled Duck Mod est retiré. Comparaison faite sur les jars (modèles reconstruits et rendus à la même échelle, os, animations, seaux, apparitions), sans les exécuter. **Rien n'a été testé en jeu.** Elle remplace les choix de doublons du [5.3](05-faune-et-ambiance.md) et du [5.7](05-faune-et-ambiance.md).

Fresh Animations n'anime aucune créature moddée : la cohérence avec le vanilla se juge sur les proportions et le type d'animation. Naturalist 2.0 anime par images clés, comme le vanilla 1.21 ; Alex's Mobs surtout par sinusoïdes. Pour le zoo, à modèle égal, la version qui se met en seau l'emporte (Hybrid Aquatic n'a aucun seau à poisson).

## Verdicts

| Espèce | Gardé | Coupé | Raison |
|---|---|---|---|
| Ours brun | Naturalist | Alex's Mobs (grizzly) | 12 animations, états (sommeil, flair), style vanilla 1.21 |
| Tigre | Naturalist | Alex's Mobs | Proportions, palette, variantes, animations |
| Éléphant | Naturalist | Alex's Mobs | Plus d'os et d'animations ; il a aussi selle et coffre |
| Rhinocéros | Naturalist | Alex's Mobs | Deux cornes, charge animée |
| Varan de Komodo | Naturalist | Alex's Mobs | Animations à images clés |
| Crocodilien | Naturalist (alligator) | Alex's Mobs (crocodile) | Même rôle dans les marais ; le caïman d'Alex's Mobs, autre espèce, reste |
| Serpent à sonnette | Naturalist | Alex's Mobs | 10 animations contre une |
| Geai bleu | Naturalist (variante d'oiseau) | Alex's Mobs | Échelle cohérente avec ses autres passereaux |
| Poisson-chat | Naturalist | Alex's Mobs | Échelle vanilla, seau |
| Baudroie | Naturalist | Hybrid Aquatic | Modèle plus soigné, leurre animé, seau |
| Blobfish | Naturalist | Alex's Mobs, Hybrid Aquatic | Silhouette la plus lisible, seau |
| Isopode géant | Naturalist | Hybrid Aquatic | Plus d'os et d'animations, seau |
| Piranha | Naturalist | Hybrid Aquatic | Palette naturelle, seau |
| Raie | Naturalist (3 espèces) | Hybrid Aquatic (pastenague) | Seau, s'enfouit ; la raie manta d'Hybrid Aquatic, autre espèce, reste |
| Grand requin blanc | Hybrid Aquatic | Naturalist | Silhouette fuselée, cohérent avec ses autres requins |
| Requin-marteau | Hybrid Aquatic | Alex's Mobs | Plus détaillé, même famille |
| Méduse | Hybrid Aquatic | Naturalist | Fine, translucide, série cohérente |
| Cténophore | Hybrid Aquatic | Alex's Mobs | Texture animée irisée |
| Étoile de mer | Hybrid Aquatic | Naturalist | Proportions, trois espèces ; on perd les 4 blocs d'étoile de Naturalist |
| Orque | Hybrid Aquatic | Alex's Mobs | Bébé, variantes, images clés |
| Homard | Hybrid Aquatic | Alex's Mobs | Plus détaillé (à vérifier en jeu : sa texture est une carte de teintes) |
| Poisson volant | Hybrid Aquatic | Alex's Mobs | Plus fin, proche du vanilla |
| Pupfish | Hybrid Aquatic | Alex's Mobs | Échelle de la morue vanilla |
| Poulpe | Hybrid Aquatic | Alex's Mobs (poulpe mimétique) | Plus soigné |
| Requin lézard | Alex's Mobs | Hybrid Aquatic | Plus expressif, seau |
| Calmar géant | Alex's Mobs | Hybrid Aquatic | Modèles proches ; celui d'Alex's Mobs vit dans les océans profonds vanilla, celui d'Hybrid Aquatic seulement dans ses fosses, dont la coupe est prévue (12.1) |
| Loutre | Critters and Companions | Hybrid Aquatic | Plus d'animations, meilleure silhouette |
| Poulpe Dumbo | Critters and Companions | Hybrid Aquatic | Celui d'Hybrid Aquatic n'apparaît nulle part |
| Libellule | Naturalist | Critters and Companions | Échelle (celle de C&C fait deux blocs d'envergure) |
| Escargot | Naturalist | Critters and Companions | Plus discret et plus animé |
| Papillon | Longwings | Naturalist (et sa chenille) | Silhouettes lisibles, 70 espèces à collectionner |
| Crabe | Friends & Foes, crabes d'Hybrid Aquatic | Naturalist | Design du vote Mojang ; les crabes d'Hybrid Aquatic sont d'autres espèces |
| Canard | Naturalist | — | Seul canard depuis le retrait d'Untitled Duck Mod |

Gardés malgré un rôle proche, parce que ce sont des espèces différentes : baleine de Naturalist et cachalot d'Alex's Mobs, fourmi et fourmi coupe-feuille, autruche et émeu, koï de C&C et carpe d'Hybrid Aquatic, limaces de mer, lézard et caméléon de Cold Sweat, sanglier et tusklin, glare et lantern, les tortues.

## Comment

| Mod | Où | Comment |
|---|---|---|
| Naturalist | `pack/config/naturalist-server.toml` | `_removed = true` par espèce, en sections (`aquatic_mobs`, `land_mobs`, `bugs`) ; l'espèce n'apparaît plus et disparaît du monde. Les lapins des forêts que Naturalist ajoute sont coupés aussi |
| Hybrid Aquatic | `pack/kubejs/data/hybrid_aquatic/neoforge/biome_modifier/` | ses 25 fichiers d'apparition par espèce remplacés par `neoforge:none` |
| Alex's Mobs, Critters and Companions | `pack/kubejs/data/fallow/neoforge/biome_modifier/doublons.json` | `neoforge:remove_spawns` sur `#minecraft:is_overworld` ; pas de config partielle, leurs formats (CodxLib, YACL) n'ont pas été testés avec un fichier incomplet |
| Tous | `pack/kubejs/server_scripts/doublons.js` | entrée dans le monde annulée pour les créatures coupées (grottes luxuriantes de C&C, biomes hors du tag) ; 78 objets sans source retirés des recettes et des butins (dont le seau de homard du phoque) |
| Tous | `pack/kubejs/client_scripts/doublons.js` | les mêmes objets cachés d'EMI |
| Wildex | `pack/config/wildex/wildex-common.toml` | créatures coupées exclues, sinon le bestiaire ne se complète pas |
| Alex's Mobs | `pack/kubejs/data/alexsmobs/tags/entity_type/roadrunner_targets.json` | le grand coureur chasse le serpent de Naturalist, comme il chassait le serpent à sonnette d'Alex's Mobs |

Un test (`scripts/test/kubejs-lists.test.mjs`) vérifie que les objets retirés côté serveur et cachés côté client sont les mêmes.

Conséquences : trois sources de boules de slime disparaissent (crachat du Komodo, méduse de Naturalist, fiole de bave de C&C) ; l'escargot de Naturalist en lâche encore.

## À trancher

- Le 5.3 coupait aussi, pour la direction artistique, le Murmur, le Farseer, le Skelewag, la mouche et le cafard d'Alex's Mobs. Avec la règle « aucune coupe de style », rien n'est fait.
- Décisions du plan hors doublons, non appliquées ici : les hostiles de Friends & Foes (iceologer, illusioner, wildfire, décision 0003) et le mammouth de Naturalist (préhistoire écartée).
- Les biomes d'Hybrid Aquatic sont encore actifs (leur coupe est prévue au 12.1). Ses 34 espèces réservées à ces biomes en dépendent.

## Tests en jeu

Sur un monde neuf.

| # | Test | Attendu |
|---|---|---|
| D1 | Logs au lancement | Aucune erreur sur `naturalist-server.toml`, `doublons.json`, les biome modifiers d'Hybrid Aquatic, `doublons.js` |
| D2 | Forêt, taïga, savane, jungle, marais, rivières | Ours, tigre, éléphant, rhinocéros, Komodo, alligator, serpent de Naturalist ; aucun de leurs doubles d'Alex's Mobs |
| D3 | Océans, récifs, fosses | Requins, méduses, orques, poulpes d'Hybrid Aquatic ; baudroie, blobfish, isopode, raie de Naturalist ; calmar géant d'Alex's Mobs |
| D4 | Grottes luxuriantes, rivières | Ni libellule ni escargot de C&C ; loutre de C&C |
| D5 | Prairies fleuries | Papillons de Longwings seulement |
| D6 | EMI : « crocodile », « homard », « étoile de mer », « libellule » | Rien des créatures coupées |
| D7 | Wildex | Aucune entrée des créatures coupées |
| D8 | Homard d'Hybrid Aquatic | Texture correcte (la carte de teintes n'a pas pu être rendue hors du jeu) |
