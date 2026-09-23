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
| [Let's Do] Farm & Charm | ✅ | CS | Ferme | Infrastructure agricole ; cuisine masquée au profit de KC (décision 0002) |
| Ecliptic Seasons | ✅ | CS | Saisons | 24 termes solaires, neige, humidité, serres ; pont Cold Sweat en KubeJS (décision 0002) |
| KaleidoscopeCookery-Automation | ✅ | CS | Automatisation | Bras mécanique Create sur les stations KC (décision 0002) |

## Dépendances (ajoutées automatiquement)
| Mod | Côté | Requis par |
|---|---|---|
| Biolith | CS | No Man's Land |
| McQoy | C | Surveyor |
| YetAnotherConfigLib (YACL) | CS | McQoy |
| Rhino | CS | KubeJS |
| Better Advanced Tooltips | CS | KubeJS |
| Architectury API | CS | Farm & Charm |

## À tester
| Mod | Rôle | Question à trancher |
|---|---|---|
| Thirst Canteen | Gourde | Utile avec Thirst Was Reclaimed ? |
| Serene Seasons | Saisons | Repli si le pont Ecliptic → Cold Sweat échoue |
| Animal Husbandry | Élevage | Besoins, maladies, génétique ; jeune (0.4.2) — non validé (décision 0002) |
| Spoiled | Conservation | Pourrissement piloté par données — non validé (décision 0002) |
| Create: Integrated Farming | Automatisation | Moissonneuse, filet de pêche ; plus tard |
| Naturalist | Faune | Concurrent d'Alex's Mobs Continued, pas un ajout automatique |
| Alex's Mobs Continued | Faune | Idem |
| Mowzie's Mobs (+ Integrated Mowzie's Mobs) | Créatures rares | Couche « anomalies » rares |
| Create: Copycats+ | Construction | Probable |
| Create: Connected | QoL Create | Probable |
| Distant Horizons | Rendu lointain | Performance client et serveur |
| Terralith + Streams Reflowing | Worldgen | Preset concurrent de NML (décision 0001) |
| Vinery / Kaleidoscope Tavern | Spécialisation | Après validation du socle agricole |
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
| Let's Do Bakery | Doublon de la farine/pâte de KC |
| Let's Do Meadow | Biomes propres, conflit probable avec NML |
| Graze | Rend l'élevage autonome |
