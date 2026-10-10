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
| Naturalist | ✅ | CS | Faune | Faune réaliste de base (décision 0003) |
| Alex's Mobs Continued | ✅ | CS | Faune | Variété ; apparition gérée espèce par espèce (décision 0003) |
| Hybrid Aquatic | ✅ | CS | Faune marine | Océans, créatures des profondeurs rares (décision 0003) |
| Mowzie's Mobs | ✅ | CS | Anomalies | Créatures uniques très rares (décision 0003) |
| Critters and Companions | ✅ | CS | Compagnons | Petite faune apprivoisable (décision 0003) |
| Untitled Duck Mod | ✅ | CS | Compagnons / ferme | Canards et oies (décision 0003) |
| Friends & Foes | ✅ | CS | Compagnons | Créatures pacifiques du mob vote ; hostiles à désactiver en config (décision 0003) |
| Adorable Hamster Pets | ✅ | CS | Compagnons | Génétique, variantes ; flair à diamants à désactiver si possible (décision 0003) |
| Companion | ✅ | CS | QoL animaux | Les animaux apprivoisés ne se perdent plus (décision 0003) |
| Companions! | ✅ | CS | Compagnons | 10 compagnons à pouvoirs ; hostiles, armes et boss à encadrer (décision 0003) |
| Ghost | ✅ | CS | Compagnons | Petits fantômes (décision 0003) |
| Sparse Structures | ✅ | CS | Structures | Espacement global des structures (décision 0004) |
| Quark | ✅ | CS | QoL / construction | L'essentiel uniquement (décision 0005) |
| Supplementaries | ✅ | CS | Décoration / QoL | L'essentiel uniquement (décision 0006) |
| ATMOSPHERICS | ✅ | C | Ambiance | Brouillard, ciel, brume par biome (décision 0007) |
| AmbientSounds | ✅ | C | Ambiance | Ambiances sonores (décision 0007) |
| Sound Physics Remastered | ✅ | CS | Ambiance | Réverbération, occlusion (décision 0007) |
| Subtle Effects | ✅ | CS | Ambiance | Particules de détail (décision 0007) |
| LambDynamicLights | ✅ | C | Ambiance | Lumière dynamique (décision 0007) |
| More Darkness | ✅ | CS | Ambiance | Vraies nuits, grottes noires (décision 0007) |
| Sodium | ✅ | C | Performance | Rendu (décision 0007) |
| ImmediatelyFast | ✅ | C | Performance | Rendu (décision 0007) |
| Entity Culling | ✅ | C | Performance | Rendu (décision 0007) |
| ModernFix | ✅ | CS | Performance | Chargement (décision 0007) |
| FerriteCore | ✅ | CS | Performance | Mémoire (décision 0007) |
| Lithium | ✅ | CS | Performance | Logique serveur (décision 0007) |
| Chunky | ✅ | CS | Serveur | Prégénération (décision 0007) |
| spark | ✅ | CS | Serveur | Profilage (décision 0007) |
| Clumps | ✅ | CS | Performance | Orbes d'XP (décision 0007) |
| Structure Layout Optimizer | ✅ | CS | Performance | Génération des structures (décision 0007) |
| Icy's Better Horses | ✅ | CS | Voyage | Chevaux compagnons : races, classes, lien, charrettes |

## Dépendances (ajoutées automatiquement)
| Mod | Côté | Requis par |
|---|---|---|
| Biolith | CS | No Man's Land |
| McQoy | C | Surveyor |
| YetAnotherConfigLib (YACL) | CS | McQoy |
| Rhino | CS | KubeJS |
| Better Advanced Tooltips | CS | KubeJS |
| Architectury API | CS | Farm & Charm, Critters and Companions |
| CodxLib | CS | Alex's Mobs Continued |
| GeckoLib | CS | Hybrid Aquatic, Mowzie's Mobs, Critters and Companions |
| Kotlin for Forge | CS | Hybrid Aquatic |
| Lithostitched | CS | Hybrid Aquatic |
| Curios API | CS | Hybrid Aquatic |
| Hybrid API | CS | Hybrid Aquatic |
| Resourceful Lib | CS | Friends & Foes |
| Kiwi | CS | Companion |
| Knight Lib | CS | Companions! |
| Zeta | CS | Quark |
| Moonlight Lib | CS | Supplementaries |
| Modonomicon | CS | Icy's Better Horses (dépendance non déclarée sur Modrinth) |
| Fzzy Config | CS | Adorable Hamster Pets (non déclarée) |
| Patchouli | CS | Adorable Hamster Pets (non déclarée) |
| Sinytra Connector | CS | Antique Atlas 4, Surveyor (mods Fabric) — en bêta, sous surveillance |
| Forgified Fabric API | CS | Antique Atlas 4, Surveyor (via Connector) |
| CreativeCore | CS | AmbientSounds |
| Resourceful Config | CS | Structure Layout Optimizer |

## À tester
| Mod | Rôle | Question à trancher |
|---|---|---|
| Thirst Canteen | Gourde | Utile avec Thirst Was Reclaimed ? |
| Serene Seasons | Saisons | Repli si le pont Ecliptic → Cold Sweat échoue |
| Create: Integrated Farming | Automatisation | Moissonneuse, filet de pêche ; plus tard |
| Create: Copycats+ | Construction | Probable |
| Create: Connected | QoL Create | Probable |
| Distant Horizons | Rendu lointain | Coût GPU/RAM/serveur ; branche dédiée (décision 0007) |
| Separate Sable Render Distance | Performance Aeronautics | Bêta (décision 0007) |
| Particle Rain | Ambiance | Bêta, doublon possible avec Ecliptic (décision 0007) |
| Terralith + Streams Reflowing | Worldgen | Preset concurrent de NML (décision 0001) |
| IDAS (+ Integrated API) | Structures Create/Quark | Plus tard (décision 0004) ; dépendances Quark + Supplementaries déjà présentes |
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
| Animal Husbandry | Mod jugé peu fiable ; mécanique d'élevage exigeant toujours voulue → alternative à trouver |
| Spoiled | Mod jugé peu fiable ; mécanique de conservation toujours voulue → alternative à trouver |
| Let's Do Bakery | Doublon de la farine/pâte de KC |
| Let's Do Meadow | Biomes propres, conflit probable avec NML |
| Graze | Rend l'élevage autonome |
| Integrated Mowzie's Mobs | Exige Quark + Supplementaries + Amendments |
| Let's Do Wilder Nature | Style cubique, bêta, doublon de Naturalist |
| Spawn / Environmental | Ajoutent des biomes (conflit NML) |
| Jurassic Saga / Jurassic Revived / Jurassic Reborn | Préhistoire abandonnée pour le moment |
| YUNG's Better * / Structory / Structory: Towers | Pas au goût de l'utilisateur (décision 0004) |
| Explorify / Moog's Voyager / Trek / Adorabuild | Trop de structures (décision 0004) |
| When Dungeons Arise / Dungeons and Taverns | Orientation RPG (décision 0004) |
| Explorer's Compass / Nature's Compass / Waystones | Contraires à la vision |
| Fog / FogRule | Doublon d'ATMOSPHERICS / alpha (décision 0007) |
| C2ME / Noisium / MoreCulling / Ksyxis | Alpha, pas à jour ou gain marginal (décision 0007) |
| Critters n' Crawlers | Doublon de Naturalist, créature d'horreur |
