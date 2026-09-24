# Fallow

Modpack Minecraft **1.21.1 / NeoForge** pour un monde permanent entre amis : exploration dans la brume, colonies, Create et aviation, agriculture sur le long terme.

## Jouer
1. Installer le [Modrinth App](https://modrinth.com/app).
2. Accepter l'invitation *Share* reçue, **ou** télécharger le `.mrpack` de la dernière [release](../../releases) puis *+ → Import from file*.

## Développer
- Pack : `pack/` (géré par [packwiz](https://packwiz.infra.link/)).
- Vision : `docs/vision.md` · Registre des mods : `docs/mods.md` · Décisions : `docs/decisions/`.
- Vérifier les dépendances réelles des jars : `npm run check-deps` (aussi exécuté en CI).
- Rapatrier les mods testés dans l'instance « Fallow test » :

```bash
npm run sync -- --instance "Fallow test"
```
