# Contribuer à Fallow

Conventions de travail du dépôt. Elles valent pour les humains comme pour les agents.

## Principes

- **Le dépôt est la source de vérité** du pack : liste de mods, configs, scripts et décisions.
- **Stabilité avant nouveauté** : versions figées, mises à jour une par une, jamais `packwiz update --all`.
- **Tout mod vient de Modrinth** et existe pour Minecraft 1.21.1 / NeoForge. Un mod indisponible est signalé, jamais remplacé par une source tierce.
- **Seul `pack/` finit dans le `.mrpack`** : la doc, les scripts et la CI n'y entrent jamais.
- Docs et messages en **français** ; identifiants de code en anglais.

## Où ranger quoi

| Contenu | Emplacement |
|---|---|
| Mod (métadonnées packwiz) | `pack/mods/<slug>.pw.toml` |
| Config commune d'un mod | `pack/config/` |
| Config appliquée à chaque nouveau monde (serverconfig) | `pack/defaultconfigs/` |
| Script KubeJS (registres, items, tags) | `pack/kubejs/startup_scripts/` |
| Script KubeJS (recettes, règles serveur, apparitions) | `pack/kubejs/server_scripts/` |
| Script KubeJS (affichage, JEI/EMI, tooltips) | `pack/kubejs/client_scripts/` |
| Données datapack (worldgen, loot tables, tags, structures) | `pack/kubejs/data/fallow/` (ou `data/<namespace>/` pour surcharger un mod) |
| Outillage (Node.js, sans dépendance) | `scripts/` + tests dans `scripts/test/` |
| Plan complet du modpack | `docs/plan/` |
| Décision tranchée (comparaison, choix) | `docs/decisions/NNNN-sujet.md` |
| Spec ou plan d'implémentation d'un sous-projet | `docs/conception/specs/`, `docs/conception/plans/` |

Les datapacks passent par KubeJS : NeoForge ne charge pas de datapack global sans mod supplémentaire, alors que `kubejs/data/` est actif sur tous les mondes.

## Ajouter, mettre à jour ou retirer un mod

1. Vérifier sa disponibilité sur Modrinth pour 1.21.1 NeoForge.
2. Le passer au **filtre des 5 questions** ([vision](docs/vision.md)) : change-t-il le gameplay ? s'intègre-t-il ? tient-il 100 h ? apporte-t-il une mécanique ? évite-t-il les doublons ?
3. L'ajouter : `cd pack && packwiz mr add <slug>` (ou `npm run sync`), puis vérifier son côté (`client` / `server` / `both`).
4. Lancer `npm run check-deps` : Modrinth ne déclare pas toujours toutes les dépendances.
5. Mettre à jour [docs/mods.md](docs/mods.md) et, si une comparaison a eu lieu, créer une fiche dans [docs/decisions/](docs/decisions/README.md).
6. Ajouter une ligne au `CHANGELOG.md` (section `[Unreleased]`).
7. **Un commit par mod** :
   - `feat(mods): add <slug>`
   - `chore(mods): update <slug> x → y`
   - `feat(mods)!: remove <slug>`

Autres préfixes : `feat(config): …`, `feat(kubejs): …`, `feat(sync): …`, `docs: …`, `ci: …`, `chore: …`.

`index.toml` est généré : après toute modification de `pack/`, lancer `packwiz refresh` dans `pack/`. La CI échoue si l'index n'est pas à jour.

## Branches et PR

- `main` est **toujours jouable** ; les tags `vX.Y.Z` y sont posés.
- Les essais se font sur `test/<sujet>` (ex. `test/distant-horizons`), puis sont fusionnés dans `main` par PR ou abandonnés. Une branche fusionnée est supprimée.
- Chaque push et chaque PR lancent `build` : tests, vérification des dépendances, index à jour, export du `.mrpack` en artefact. Une PR n'est fusionnée qu'au vert.

## Versions et release

- `0.x` : prototype, mondes jetables.
- `1.0.0` : lancement du monde permanent.
- Après `1.0.0`, tout changement qui retire des blocs ou objets ou modifie le worldgen est une **version majeure** et impose une sauvegarde du monde avant mise à jour.

Pour publier : passer la section `[Unreleased]` du `CHANGELOG.md` sous `[X.Y.Z]`, mettre la même version dans `pack/pack.toml`, `packwiz refresh`, puis poser le tag `vX.Y.Z` sur `main`. Le workflow `release` vérifie la concordance, exporte le `.mrpack` et crée la release GitHub. La publication Modrinth reste désactivée tant que `MODRINTH_PROJECT_ID` (variable) et `MODRINTH_TOKEN` (secret, saisi par le propriétaire) n'existent pas.

## Instance de test

Les essais en jeu se font dans une instance **« Fallow test »** du Modrinth App. `npm run sync -- --instance "Fallow test"` compare l'instance au pack (via l'API Modrinth), affiche le diff et applique les commandes packwiz après confirmation ; `--configs` copie aussi les configs modifiées, `--dry-run` montre le diff sans rien appliquer. Le script ne modifie jamais l'instance.

Sens inverse : `packwiz mr export` puis import du `.mrpack` dans le Modrinth App.
