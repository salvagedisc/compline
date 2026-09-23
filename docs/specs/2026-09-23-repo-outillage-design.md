# Fallow — Repo & outillage (sous-projet 1)

- **Date** : 2026-09-23
- **Statut** : en revue
- **Repo** : `salvagedisc/fallow` (GitHub, privé)

## 1. Contexte

Fallow est un modpack Minecraft pour un monde permanent joué entre amis sur un serveur dédié loué. Direction : exploration (façon Breath of the Wild), Create + Aeronautics, établissement de colonies, agriculture/élevage sur le long terme, ambiance brumeuse, solitaire et étrange.

Contraintes fixes :

- Minecraft **1.21.1** + **NeoForge** (imposé par Create Aeronautics).
- **Tous les mods doivent exister sur Modrinth** pour 1.21.1 NeoForge.
- Versions **figées** : la stabilité du monde permanent prime sur la nouveauté.

Le projet global est découpé en sous-projets, chacun avec son propre cycle spec → plan → implémentation :

1. **Repo & outillage** — *ce document*.
2. Choix des systèmes (agriculture, faune, structures, ambiance…).
3. Couche de règles (KubeJS + données datapack).
4. Serveur (hébergeur, déploiement, sauvegardes).

## 2. Objectif

Disposer d'un repo qui soit la **source de vérité** du pack : liste de mods versionnée et lisible, configs, scripts, documentation des décisions — et qui produise automatiquement un `.mrpack` installable dans le Modrinth App, puis publiable sur Modrinth.

### Critères de réussite

- Le repo privé existe et contient la structure décrite en §4.
- `packwiz` gère le pack ; `pack.toml` fige Minecraft 1.21.1 et une version précise de NeoForge.
- Le pack de base contient les mods **retenus** (§6) et se lance dans le Modrinth App (création d'un monde sans crash).
- Un push sur GitHub produit un `.mrpack` téléchargeable depuis l'onglet Actions.
- Un tag `vX.Y.Z` crée une release GitHub contenant le `.mrpack`.
- Le script de synchronisation détecte les mods ajoutés/retirés dans l'instance de test et les applique au pack après confirmation.
- `docs/vision.md` et `docs/mods.md` reflètent l'état actuel des décisions.

### Hors périmètre

- Choix de nouveaux mods (sous-projet 2).
- Scripts KubeJS de gameplay et données datapack (sous-projet 3) — seule l'arborescence est créée.
- Hébergeur, déploiement serveur, sauvegardes (sous-projet 4).
- Publication effective sur Modrinth (nécessite validation de modération et jeton fourni par l'utilisateur) — le workflow est prêt mais désactivé.

## 3. Outil : packwiz

Choisi face à deux alternatives :

| Option | Verdict |
|---|---|
| **packwiz** — un fichier TOML par mod, export `.mrpack` natif | ✅ Retenu : repo réellement source de vérité, diffs lisibles, standard, compatible CI |
| Modrinth App source + export décompressé dans le repo | ❌ Le repo devient une archive, exports écrasants, pas d'édition depuis le repo |
| Script maison sur l'API Modrinth | ❌ Réimplémente packwiz, maintenance inutile |

Installation locale : binaire officiel (artefact CI du repo `packwiz/packwiz`) ou `go install`. Tout téléchargement est soumis à l'accord de l'utilisateur. En CI : installation via `go install` sur un commit épinglé.

## 4. Structure du repo

```
fallow/
├── pack/                     ← racine packwiz, seul contenu du .mrpack
│   ├── pack.toml             ← nom, version, MC 1.21.1, NeoForge figé
│   ├── index.toml            ← généré (packwiz refresh)
│   ├── mods/                 ← un .pw.toml par mod
│   ├── config/               ← configs des mods
│   ├── defaultconfigs/       ← configs par monde
│   └── kubejs/
│       ├── startup_scripts/
│       ├── server_scripts/
│       ├── client_scripts/
│       └── data/fallow/      ← données "datapack" chargées par KubeJS
├── scripts/
│   └── sync-from-instance    ← pont instance Modrinth App → repo
├── docs/
│   ├── vision.md             ← philosophie du pack
│   ├── mods.md               ← registre des mods (statut + justification)
│   ├── decisions/            ← une fiche par décision tranchée
├── .github/workflows/
│   ├── build.yml
│   └── release.yml
├── README.md                 ← installation pour les joueurs
└── CHANGELOG.md
```

Décisions :

- **Pack dans `pack/`** : la doc et la CI ne finissent jamais dans le `.mrpack` (`.packwizignore` en complément).
- **Datapacks via `kubejs/data/`** : NeoForge ne charge pas de datapacks globaux sans mod supplémentaire ; KubeJS (déjà retenu) charge ce dossier comme un datapack actif sur tous les mondes.
- **Seuls les mods retenus sont dans `pack/mods/`** ; les candidats sont listés dans `docs/mods.md` et testés dans l'instance ou sur une branche `test/*`.

## 5. Cycle de travail

### Versions

- `0.x` : prototype, mondes jetables.
- `1.0.0` : lancement du monde permanent.
- Après `1.0.0`, tout changement qui retire des blocs/objets ou modifie le worldgen = **version majeure** + sauvegarde obligatoire du monde avant mise à jour.
- Mises à jour de mods **une par une**, jamais `packwiz update --all`.

### Branches

- `main` : toujours jouable ; les tags `vX.Y.Z` y sont posés.
- `test/<sujet>` : essais (ex. `test/serene-seasons`), fusionnés ou abandonnés.
- Pas de PR obligatoire tant que le projet est mené seul.

### Ajout d'un mod

1. Vérifier sa disponibilité Modrinth 1.21.1 NeoForge.
2. Le passer au filtre des 5 questions (change le gameplay ? s'intègre ? tient 100 h+ ? apporte une mécanique ? évite les doublons ?).
3. L'ajouter (`packwiz mr add` ou script de sync) et définir son côté (client / serveur / both).
4. Mettre à jour `docs/mods.md` ; créer une fiche `docs/decisions/` si une comparaison a eu lieu.
5. Un commit par mod : `feat(mods): add <Nom>` / `chore(mods): update <Nom> x → y` / `feat(mods)!: remove <Nom>`.

### Instance de test et synchronisation

L'utilisateur expérimente dans une instance **« Fallow – test »** du Modrinth App (ajout/retrait de mods libre).

`scripts/sync-from-instance` :

1. Localise le dossier de l'instance (`%APPDATA%/ModrinthApp/profiles/<nom>/`), chemin configurable.
2. Calcule le SHA-1 de chaque `.jar` de `mods/`.
3. Interroge l'API Modrinth (`POST /v2/version_files`) pour identifier projet + version.
4. Compare avec `pack/mods/*.pw.toml` et affiche le diff (ajouts, retraits, changements de version, jars inconnus de Modrinth signalés comme non conformes).
5. Après confirmation, exécute les `packwiz mr add` / `packwiz remove` correspondants.
6. Option `--configs` : copie les fichiers de `config/` modifiés de l'instance vers `pack/config/` (diff affiché avant copie).

Le script ne modifie jamais l'instance. Implémentation en Node.js (déjà installé sur la machine), sans dépendance externe si possible.

Sens inverse : `packwiz mr export` → import du `.mrpack` dans le Modrinth App.

Test entre amis en phase `0.x` : `.mrpack` importé dans le Modrinth App puis partagé via l'onglet **Share** de l'instance.

## 6. Contenu du pack de base

Mods **retenus** à l'issue de la conception préalable (à vérifier sur Modrinth pour 1.21.1 NeoForge au moment de l'ajout ; tout mod indisponible est signalé et non ajouté) :

| Domaine | Mod |
|---|---|
| Worldgen | No Man's Land |
| Artisanat | Overgeared |
| Industrie | Create |
| Aviation | Create Aeronautics + Sable |
| Rail | Create: Steam 'n' Rails |
| Cuisine | Kaleidoscope Cookery |
| Carte | Antique Atlas 4 + Surveyor |
| Température | Cold Sweat |
| Soif | Thirst Was Reclaimed |
| Scripts | KubeJS |

Plus les dépendances requises par ces mods. Les mods d'optimisation (client et serveur) sont un choix du sous-projet 2 ; ils ne sont pas ajoutés ici.

`docs/mods.md` liste aussi les mods **à tester** et **écartés** issus de la conception préalable, avec leur justification.

## 7. Automatisation

### `build.yml` — sur push et PR

1. Installe packwiz (commit épinglé).
2. Vérifie que `index.toml` est à jour (`packwiz refresh` ne produit aucun diff).
3. `packwiz mr export` → `Fallow-<version>-<sha>.mrpack`.
4. Publie le fichier comme artefact de la run.

### `release.yml` — sur tag `v*`

1. Vérifie que la version du tag correspond à `pack.toml`.
2. Exporte le `.mrpack`.
3. Crée une release GitHub avec le `.mrpack` et la section correspondante du `CHANGELOG.md`.
4. **Étape Modrinth (désactivée)** : publie la version sur le projet Modrinth (Unlisted) via l'API. Activée seulement si la variable `MODRINTH_PROJECT_ID` et le secret `MODRINTH_TOKEN` existent. Le jeton est créé et saisi par l'utilisateur lui-même.

### Pack serveur

Le `.mrpack` porte l'information client/serveur de chaque mod. Le repo étant privé, `packwiz-installer` (qui lit le pack par URL) n'est pas utilisable ; le serveur sera installé à partir du `.mrpack` (outil de l'hébergeur ou `mrpack-install`). Détails : sous-projet 4.

## 8. Documentation

- `docs/vision.md` : pitch, DA (brume, solitude, étrangeté sans horreur), trois piliers (Exploration / Civilisation / Vie quotidienne), boucle de jeu, interdits (minimap, téléportation, hostiles vanilla, End/Elytra, hordes, empilement d'addons), règles de travail (comparaisons réelles, filtre des 5 questions).
- `docs/mods.md` : tableau par domaine — mod, statut (✅ retenu / 🟡 à tester / ❌ écarté), côté, rôle, justification.
- `docs/decisions/` : première fiche `0001-nml-vs-terralith.md` résumant la comparaison déjà faite (NML retenu, Terralith + Streams Reflowing en preset concurrent à tester).
- `README.md` : présentation courte et installation (import du `.mrpack` / lien Share).
- `CHANGELOG.md` : format Keep a Changelog.

## 9. Risques

| Risque | Parade |
|---|---|
| Un mod retenu n'a pas de version 1.21.1 NeoForge sur Modrinth | Signalé, non ajouté, décision reportée au sous-projet 2 |
| Conflit/crash au lancement du pack de base | Test de lancement obligatoire avant tag `v0.1.0` ; bisection mod par mod |
| Jar de l'instance absent de Modrinth | Le script le signale comme non conforme au lieu de l'ignorer |
| Mise à jour cassant le monde permanent | Règle de version majeure + sauvegarde (§5) |
| Repo privé incompatible avec packwiz-installer | Installation serveur par `.mrpack` (§7) |
