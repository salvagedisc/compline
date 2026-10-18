# Fallow : plan du modpack

Minecraft **1.21.1**, NeoForge 21.1.251, packwiz, mods venant uniquement de Modrinth. Le plan s'appuie sur la [vision](../vision.md) et les [décisions](../decisions/README.md) ; la liste complète des mods, avec leur statut, est dans le [registre](../mods.md).

> **État au 2026-10-18.** Le plan n'est pas encore appliqué à `pack/` : il attend la validation de l'équipe (décisions du [10.12](10-recentrage.md#1012-décisions-à-valider-remplace-le-84) et du [12.5](12-verification.md#125-décisions-qui-sajoutent-suite-du-1012)). Rien n'a encore été testé en jeu.

## Ce que veut Fallow

Un monde permanent, presque vide et énigmatique, dans l'esprit du trailer de No Man's Land. La brume se lève par endroits et par moments, là où l'on fait revenir la vie (les clairières). Des objectifs longs : le zoo, le bestiaire, le codex des monuments, le réseau ferré et aérien, la maison. Une finition soignée façon Realism Craft, sans shaders ni textures réalistes, où chaque système se branche sur les autres.

## Ordre de lecture

| Chapitre | Contenu |
|---|---|
| [10-recentrage.md](10-recentrage.md) | **Le plan en vigueur** : la brume qui se lève, les objectifs, la liste du pack, la finition, les liens entre systèmes, les scripts, les décisions 1 à 13 |
| [11-rendu-et-confort.md](11-rendu-et-confort.md) | Rendu sans shaders, caméra, menus, EMI, Wildex, Ancient Remnants, Archie's Dinos |
| [12-verification.md](12-verification.md) | **Vérification complète du 2026-10-18** : elle corrige les chapitres 10 et 11, détaille les liens entre mods (Overgeared, Kaleidoscope, Create, monde, rendu), propose le mod custom `fallow` et les décisions 14 à 18 |
| [recherche/verif-2026-10-18/](recherche/verif-2026-10-18/) | Les six rapports de la vérification (le 6 traite des gardiens des monuments), avec les preuves (jars ouverts, versions, tickets) et la liste des tests en jeu |
| [kubejs-brouillons/](kubejs-brouillons/) | Brouillons de scripts et de données, rangés comme `pack/kubejs/` ; à corriger selon le chapitre 12 avant usage |

## Historique (v1 du 2026-10-10)

Les chapitres 01 à 09 sont la première version du plan, jugée trop éloignée de la vision le 2026-10-11. Ils restent pour leurs détails techniques ; quand ils contredisent les chapitres 10 à 12, ce sont ces derniers qui comptent.

| Fichier | Contenu |
|---|---|
| [01-liste-des-mods.md](01-liste-des-mods.md) | Liste v1 (remplacée par le [registre](../mods.md)) |
| [02-exploration.md](02-exploration.md) | Exploration, structures, navigation sans coordonnées |
| [03-civilisation.md](03-civilisation.md) | Addons Create, minerai, progression, Nether |
| [04-vie-quotidienne.md](04-vie-quotidienne.md) | Eau, champs, élevage, cuisine |
| [05-faune-et-ambiance.md](05-faune-et-ambiance.md) | Suppression des monstres vanilla, plafonds, doublons |
| [06-technique-et-serveur.md](06-technique-et-serveur.md) | Alertes, socle technique, serveur, sauvegardes |
| [07-kubejs-et-datapack.md](07-kubejs-et-datapack.md) | Organisation des scripts et des données |
| [08-mise-en-place.md](08-mise-en-place.md) | Phases de mise en place, vérifications dans les jars |
| [09-disponibilite-modrinth.md](09-disponibilite-modrinth.md) | Relevé Modrinth du 2026-10-10 |
| [recherche/](recherche/) | Les cinq recherches de la v1 et le relevé brut Modrinth |

## Après validation

Chaque choix validé est appliqué à `pack/` selon [CONTRIBUTING.md](../../CONTRIBUTING.md) : un mod par commit, configs dans `pack/config/` ou `pack/defaultconfigs/`, scripts dans `pack/kubejs/`, et une fiche dans `docs/decisions/` pour chaque arbitrage.
