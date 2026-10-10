# 0007 — Ambiance & performance

- **Date** : 2026-09-24
- **Statut** : retenus (testés sur `test/structures`, fusionnés dans `main` le 2026-10-10) ; Distant Horizons, Separate Sable Render Distance et Particle Rain en attente

## Ambiance (sans shader : Aeronautics a des problèmes connus avec Iris)
| Mod | Côté | Rôle |
|---|---|---|
| **ATMOSPHERICS** | C | Brouillard, ciel, nuages, brume réglables par biome, jour/nuit séparés ; presets partageables. Ne pas activer « Improved Transparency » dans Sodium. |
| **AmbientSounds** (+ CreativeCore) | C | Ambiances sonores par biome, heure, altitude (vent, oiseaux, insectes, gouttes dans les grottes) |
| **Sound Physics Remastered** | CS | Réverbération et occlusion : les grottes et vallées sonnent comme telles. Version 1.21.1 marquée alpha mais très répandue. |
| **Subtle Effects** | CS | Détails de particules (étincelles, fumée, poussière) ; certains effets à couper (config) |
| **LambDynamicLights** | C | La torche tenue en main éclaire |
| **More Darkness** | CS | Nuits réglées par la lune, grottes noires, adaptation de l'œil. Jeune (14 k) mais client : réversible |

Écartés : Fog (plus mis à jour depuis 2025-10, doublon d'ATMOSPHERICS), FogRule (alpha, exige Lootr), Presence Footsteps, Falling Leaves, Visuality, Cave Dust, True Darkness (pas de version NeoForge 1.21.1).

## Performance
| Mod | Côté | Rôle / compatibilité vérifiée |
|---|---|---|
| **Sodium** 0.8.13 | C | Rendu ; Sable exige ≥ 0.8.12, Create ≥ 0.6.9 |
| **ImmediatelyFast**, **Entity Culling** | C | Rendu immédiat, masquage des entités invisibles |
| **ModernFix**, **FerriteCore** | CS | Temps de chargement, mémoire |
| **Lithium** 0.15.4 | CS | Logique serveur ; Create exige ≥ 0.14.7 |
| **Chunky** | CS | Prégénération du monde (serveur) |
| **spark** | CS | Profilage (diagnostic des ralentissements) |
| **Clumps** | CS | Fusion des orbes d'expérience |
| **Structure Layout Optimizer** (+ Resourceful Config) | CS | Génération des grandes structures |

Écartés : C2ME (alpha), Noisium (pas à jour), MoreCulling (gain marginal), Alternate Current (peu de redstone), Ksyxis.

## En attente
- **Distant Horizons** : voir une montagne à des centaines de blocs sert directement l'exploration, mais coûte cher (GPU, RAM, génération côté serveur). Compatible Ecliptic. À tester sur sa propre branche.
- **Separate Sable Render Distance** (bêta) : distance de rendu dédiée aux engins Aeronautics.
- **Particle Rain** (bêta) : particules de pluie/neige et brume d'orage ; possible doublon avec les effets d'Ecliptic.
