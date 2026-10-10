# Fallow

[![build](https://github.com/salvagedisc/fallow/actions/workflows/build.yml/badge.svg)](https://github.com/salvagedisc/fallow/actions/workflows/build.yml)

Modpack Minecraft **1.21.1 / NeoForge** pour un monde permanent entre amis : exploration dans la brume, colonies, Create et aviation, agriculture et élevage sur le long terme. Pas de minimap, pas de téléportation, pas de monstres vanilla : la difficulté vient de l'environnement.

| | |
|---|---|
| Minecraft | 1.21.1 |
| Loader | NeoForge 21.1.251 |
| Outil | [packwiz](https://packwiz.infra.link/) → export `.mrpack` (Modrinth) |
| Version | `0.x` (prototype, mondes jetables) ; `1.0.0` = lancement du monde permanent |
| Contenu | voir le [registre des mods](docs/mods.md) |

## Jouer

1. Installer le [Modrinth App](https://modrinth.com/app).
2. Accepter l'invitation *Share* reçue, **ou** télécharger le `.mrpack` de la dernière [release](../../releases) (ou l'artefact du dernier [build](../../actions/workflows/build.yml)) puis *+ → Import from file*.

## Le dépôt

```
fallow/
├── pack/                  ← racine packwiz : seul contenu du .mrpack
│   ├── pack.toml          ← nom, version, Minecraft et NeoForge figés
│   ├── index.toml         ← généré par `packwiz refresh`, ne pas éditer
│   ├── mods/              ← un .pw.toml par mod (source Modrinth)
│   ├── config/            ← configs des mods (communes)
│   ├── defaultconfigs/    ← configs appliquées à chaque nouveau monde
│   └── kubejs/            ← couche de règles : scripts startup/server/client
│       └── data/fallow/   ← données datapack chargées par KubeJS
├── scripts/               ← outillage Node.js sans dépendance (+ tests)
├── docs/
│   ├── vision.md          ← philosophie du pack
│   ├── mods.md            ← registre : retenus, à tester, écartés
│   ├── feuille-de-route.md ← sous-projets et chantiers ouverts
│   ├── plan/              ← plan complet du modpack (mods, configs, scripts)
│   ├── decisions/         ← une fiche par décision tranchée
│   └── conception/        ← specs et plans d'implémentation
├── .github/workflows/     ← build (chaque push) et release (tag vX.Y.Z)
├── CONTRIBUTING.md        ← conventions de travail
└── CHANGELOG.md
```

## Développer

Prérequis : Node.js ≥ 20 et [packwiz](https://packwiz.infra.link/installation/).

| Commande | Rôle |
|---|---|
| `npm test` | Tests de l'outillage |
| `npm run check-deps` | Vérifie que les dépendances déclarées dans les jars sont fournies par le pack (aussi en CI) |
| `npm run sync -- --instance "Fallow test"` | Rapatrie les mods (et `--configs`) testés dans l'instance du Modrinth App |
| `cd pack && packwiz refresh` | Régénère `index.toml` après toute modification de `pack/` |
| `cd pack && packwiz mr export` | Produit le `.mrpack` localement |

Le fonctionnement complet (ajout d'un mod, branches de test, versions, release) est décrit dans [CONTRIBUTING.md](CONTRIBUTING.md). La documentation est indexée dans [docs/](docs/README.md).
