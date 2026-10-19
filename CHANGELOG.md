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
- Performance : Sodium, ImmediatelyFast, Entity Culling, ModernFix, FerriteCore, Lithium, Structure Layout Optimizer ; serveur : Chunky, spark.
- Choix de l'équipe du 2026-10-19 :
  - monde et objectifs : Ancient Remnants: Monoliths (+ Fragmentum), Lootr, Enhanced Celestials, AA4 Atlas, Wildex Bestiary, Exposure ;
  - vie quotidienne et faune : Kaleidoscope Compat, Traveler's Backpack, Longwings ;
  - construction : Amendments, Handcrafted, Chipped, Beautify!, Twigs, Every Compat, HT's TreeChop, Visual Workbench ;
  - corps et caméra : Entity Model Features, Entity Texture Features, 3D Skin Layers, Shoulder Surfing Reloaded (une seule vue à l'épaule, caméra libre), Spatial GUI, Camera Overhaul (réglé bas), ItemPhysic, Smooth Swapping ;
  - rendu et son : Polytone, Particular Reforged, Sounds, Auroras, Drip Sounds ; packs Fresh Animations (+ Details, Objects, Emissive, Player Extension) et Motschen's Better Leaves, actifs au premier lancement via Default Options ;
  - interface : EMI, JEI (listes cachées), Pick Up Notifier, Seamless Loading Screen, Mouse Tweaks, Controlling + Searchables, Default Options, Chat Heads ;
  - scripts : KubeJS Create, LootJS, MoreJS, In Control!.
- Enchantement : Immersive Enchanting (+ AlfinoLib), structures du mod coupées, traduction française ([0008](docs/decisions/0008-enchantement.md)).
- Outillage : `npm run sync` rapatrie les mods de l'instance de test, `npm run check-deps` vérifie les dépendances réelles des jars ; CI `build` (export `.mrpack` à chaque push) et `release` (sur tag).
- Mod custom `fallow` 0.1.0 (`mods-custom/fallow/`, jar dans `pack/mods/`) : module ciel (lune d'Enhanced Celestials visible sous ATMOSPHERICS, cache de brume vidé après un changement de biome) et module sac (le tuyau de Traveler's Backpack désaltère dans Thirst Was Reclaimed). Non testé en jeu ; CI `build` le compile.
- Tout en français : jeu en français au premier lancement (Default Options) ; traduction des textes manquants de 94 namespaces de mods (≈25 300 textes) et de leurs livres (dictionnaire d'Alex's Mobs, guide des hamsters, chronique d'Ecliptic Seasons) dans `pack/kubejs/assets/` ; `npm run check-fr` (aussi en CI) refuse un texte sans traduction ou une variable perdue ([traduction](docs/plan/traduction.md)).
- Idées reprises de Wayfarer, le modpack de l'équipe de No Man's Land ([recherche](docs/plan/recherche/2026-10-19/wayfarer.md)) :
  - Envelope (courrier par pigeons, lettres perdues), traduit en français ;
  - la Lune Amie de No Man's Land comme fil principal : note froissée dans les jarres des alchimistes, une seule lune à la fois avec Enhanced Celestials, lunes de sang retirées ([lune-amie](docs/plan/lune-amie.md)) ;
  - santé maximale : un cœur perdu par mort, plancher de 5 cœurs, éclats de vie à trouver en explorant ([santé](docs/plan/sante.md)) ;
  - tannerie : les animaux lâchent une peau brute, préparée aux cendres puis trempée sur le plateau en bambou de Kaleidoscope ([tannerie](docs/plan/tannerie.md)) ;
  - plus d'expérience : enclume sans niveaux (usure), barre cachée, aucune orbe, fioles et pépites retirées ([0009](docs/decisions/0009-xp.md)).
- Mod `fallow` 0.2.0 : module `xp`.
- Musique : True Adaptive Music et le paquet `fallow` (`pack/trueadaptivemusicpacks/fallow/`) : 11 situations (brume, clairière, saisons, lune, dirigeable, sous terre, monument…), longs silences, muet pendant les boss ; 29 morceaux choisis par l'équipe : vanilla et No Man's Land, pack de ressources Supernova, 10 morceaux libres de Scott Buckley, Kevin MacLeod et HoliznaCC0 avec leurs crédits ; script `musique.js`, tag `fallow:brumeux`, interface de TAM en français, paquet actif et musique à 40 % au premier lancement ([0010](docs/decisions/0010-musique.md)).

### Retiré
- AA4 Atlas : compilé pour Antique Atlas 2, il appelait `AtlasScreen.prepareToOpen()` avec l'ancien type de retour et faisait planter le jeu à l'ouverture de l'atlas (rapport de plantage du test). L'atlas s'ouvre à la touche d'Antique Atlas.
- Entity Culling : casse les engins Create Aeronautics et Sable.
- Clumps : les orbes d'XP n'existent plus.

### Modifié
- Enhanced Celestials 6.0.2.6 → Enhanced Celestials 2 (Core 2.0.3.3 + Default Lunar Events 2.0.1.3) : nouveaux paquets Java et nouveau format de données ; `lune.js`, `musique.js` et le module `ciel` du mod `fallow` (0.2.1) portés, lunes de sang retirées au nouveau format, traductions françaises, téléchargement des traductions de la communauté coupé.
- JEI 19.69.0.456 (bêta) → 19.51.0.418 : depuis la 19.52, JEI envoie le transfert de recette depuis une autre méthode ; le mixin obligatoire d'Envelope 0.7.5 ne trouve plus sa cible et le jeu plantait en entrant dans un monde (rapport de plantage du premier test). 46 textes de JEI traduits en plus.
- Dépôt réorganisé : `README` et `CONTRIBUTING` réécrits, index de la documentation, feuille de route, emplacement `docs/plan/` pour le plan du modpack, `docs/` renommé `docs/conception/`, registre des mods classé par domaine.
- Branches `test/agriculture`, `test/faune` et `test/structures` fusionnées dans `main`.
- No Man's Land 1.5.12 → 1.6.1 : outils ancestraux (pioche rituelle, épée du serment, cloche et ciseau en bronze), carreaux lunaires et solaires ; textes nouveaux traduits.
- Kaleidoscope Cookery 1.5.1 → 1.6.0 : la 1.5.1 faisait planter le jeu au chargement avec Kaleidoscope Compat 2.9.7 (mixin `MillstoneBlockEntityMixin`), constaté sur le serveur de test.
