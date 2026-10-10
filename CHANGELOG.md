# Changelog

Format : [Keep a Changelog](https://keepachangelog.com/fr/1.1.0/). Versions : `0.x` prototype, `1.0.0` lancement du monde permanent.

## [Unreleased]

Aucune version n'a encore été publiée : tout ce qui suit formera la `0.1.0`.

### Ajouté
- Pack de base : No Man's Land, Overgeared, Create, Create Aeronautics + Sable, Steam 'n' Rails, Kaleidoscope Cookery, Antique Atlas 4 + Surveyor, Cold Sweat, Thirst Was Reclaimed, KubeJS.
- Agriculture : [Let's Do] Farm & Charm (+ Architectury API), Ecliptic Seasons, KaleidoscopeCookery-Automation.
- Faune : Naturalist, Alex's Mobs Continued, Hybrid Aquatic, Mowzie's Mobs, Critters and Companions.
- Compagnons : Untitled Duck Mod, Friends & Foes, Adorable Hamster Pets, Companion, Companions!, Ghost.
- Structures : Sparse Structures.
- QoL / construction : Quark et Supplementaries, réduits à l'essentiel (configs fournies).
- Voyage : Icy's Better Horses.
- Ambiance : ATMOSPHERICS, AmbientSounds, Sound Physics Remastered, Subtle Effects, LambDynamicLights, More Darkness.
- Performance : Sodium, ImmediatelyFast, Entity Culling, ModernFix, FerriteCore, Lithium, Clumps, Structure Layout Optimizer ; serveur : Chunky, spark.
- Outillage : `npm run sync` rapatrie les mods de l'instance de test, `npm run check-deps` vérifie les dépendances réelles des jars ; CI `build` (export `.mrpack` à chaque push) et `release` (sur tag).

### Modifié
- Dépôt réorganisé : `README` et `CONTRIBUTING` réécrits, index de la documentation, feuille de route, emplacement `docs/plan/` pour le plan du modpack, `docs/` renommé `docs/conception/`, registre des mods classé par domaine.
- Branches `test/agriculture`, `test/faune` et `test/structures` fusionnées dans `main`.
