# Registre des mods

Statuts : ✅ retenu (dans `pack/mods/`) · 🟡 à tester · ❌ écarté.
Côté : C = client, S = serveur, CS = les deux.

## Socle
| Mod | Statut | Côté | Rôle | Justification |
|---|---|---|---|---|
| No Man's Land | ✅ | CS | Worldgen | Ambiance sauvage et cohérente, meilleure pour la solitude (voir décision 0001) |
| Overgeared | ✅ | CS | Artisanat | Forge, plans, alliages : fabrication physique et progressive |
| Create | ✅ | CS | Industrie | Colonne vertébrale technologique |
| Create Aeronautics + Sable | ✅ | CS | Aviation | Pilier civilisation ; impose 1.21.1 NeoForge |
| Create: Steam 'n' Rails | ✅ | CS | Rail | Infrastructure ferroviaire complète (port NeoForge non officiel) |
| Kaleidoscope Cookery | ✅ | CS | Cuisine | Cuisine immersive, système alimentaire principal |
| Antique Atlas 4 | ✅ | C | Carte | Carte dessinée qui se construit, remplace coordonnées et minimap |
| Surveyor Map Framework | ✅ | CS | Carte | Backend d'exploration/partage de carte pour Antique Atlas |
| Cold Sweat | ✅ | CS | Température | Survie environnementale |
| Thirst Was Reclaimed | ✅ | CS | Soif | Survie environnementale |
| KubeJS | ✅ | CS | Règles | Couche de cohérence (scripts + données datapack) |

## Dépendances (ajoutées automatiquement)
| Mod | Côté | Requis par |
|---|---|---|
| Biolith | CS | No Man's Land |
| McQoy | C | Surveyor |
| YetAnotherConfigLib (YACL) | CS | McQoy |
| Rhino | CS | KubeJS |
| Better Advanced Tooltips | CS | KubeJS |

## À tester
| Mod | Rôle | Question à trancher |
|---|---|---|
| Thirst Canteen | Gourde | Utile avec Thirst Was Reclaimed ? |
| Serene Seasons | Saisons | Interaction avec Cold Sweat, NML et Kaleidoscope |
| Naturalist | Faune | Concurrent d'Alex's Mobs Continued, pas un ajout automatique |
| Alex's Mobs Continued | Faune | Idem |
| Mowzie's Mobs (+ Integrated Mowzie's Mobs) | Créatures rares | Couche « anomalies » rares |
| Create: Copycats+ | Construction | Probable |
| Create: Connected | QoL Create | Probable |
| Distant Horizons | Rendu lointain | Performance client et serveur |
| Terralith + Streams Reflowing | Worldgen | Preset concurrent de NML (décision 0001) |
| Farm & Charm / autres Let's Do | Agriculture | Prochaine étape de design : agriculture |
| Create: Power Loader | Chunkloading | Risque de casser la logique d'entretien des infrastructures |

## Écartés
| Mod | Raison |
|---|---|
| Tectonic | Remplacé par la comparaison NML vs Terralith |
| Downhill Rivers | Idem |
| Prehistorum | Modèles de mauvaise qualité |
| Farmer's Delight (+ addons Create FD) | Kaleidoscope Cookery est le système alimentaire principal |
| Legendary Survival Overhaul | Cold Sweat + Thirst Was Reclaimed retenus |
| Minimaps / waypoints | Contraire à la vision |
