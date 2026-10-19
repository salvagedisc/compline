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
- Choix de l'équipe du 2026-10-19 :
  - monde et objectifs : Ancient Remnants: Monoliths (+ Fragmentum), Lootr, Enhanced Celestials, AA4 Atlas, Wildex Bestiary, Exposure ;
  - vie quotidienne et faune : Kaleidoscope Compat, Traveler's Backpack, Longwings ;
  - construction : Amendments, Handcrafted, Chipped, Beautify!, Twigs, Every Compat, HT's TreeChop, Visual Workbench ;
  - corps et caméra : Entity Model Features, Entity Texture Features, 3D Skin Layers, Shoulder Surfing Reloaded (une seule vue à l'épaule, caméra libre), Spatial GUI, Camera Overhaul (réglé bas), ItemPhysic, Smooth Swapping ;
  - rendu et son : Polytone, Particular Reforged, Sounds, Auroras, Drip Sounds ; packs Fresh Animations (+ Details, Objects, Emissive, Player Extension) et Motschen's Better Leaves, actifs au premier lancement via Default Options ;
  - interface : EMI, JEI (listes cachées), Pick Up Notifier, Seamless Loading Screen, Mouse Tweaks, Controlling + Searchables, Default Options, Chat Heads ;
  - scripts : KubeJS Create, LootJS, MoreJS, In Control!.
- Outillage : `npm run sync` rapatrie les mods de l'instance de test, `npm run check-deps` vérifie les dépendances réelles des jars ; CI `build` (export `.mrpack` à chaque push) et `release` (sur tag).

### Retiré
- Entity Culling : casse les engins Create Aeronautics et Sable.

### Modifié
- Dépôt réorganisé : `README` et `CONTRIBUTING` réécrits, index de la documentation, feuille de route, emplacement `docs/plan/` pour le plan du modpack, `docs/` renommé `docs/conception/`, registre des mods classé par domaine.
- Branches `test/agriculture`, `test/faune` et `test/structures` fusionnées dans `main`.
