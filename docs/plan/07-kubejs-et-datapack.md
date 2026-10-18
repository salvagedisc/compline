# 7. Couche de règles : scripts KubeJS et données datapack

Tout ce qui lie les mods entre eux passe par KubeJS (déjà dans le pack) et par des données chargées depuis `pack/kubejs/data/` (KubeJS les charge comme un datapack actif sur tous les mondes). Les **brouillons** sont dans [kubejs-brouillons/](kubejs-brouillons/), rangés comme dans `pack/kubejs/` : ils montrent la forme et la logique, mais **aucun identifiant n'est garanti** tant que les .jar n'ont pas été lus. C'est le travail de la séance d'étude des .jar ([8.3](08-mise-en-place.md#83-ce-quil-faut-lire-dans-les-jar)).

## 7.1 Organisation

```
pack/kubejs/
├── startup_scripts/
│   └── objets_fallow.js            plans d'ingénieur, pièces forgées, cœur de braise, salpêtre
├── server_scripts/
│   ├── 00_fallow_constants.js      listes partagées (hostiles bannis, plans → recettes)
│   ├── no_end_no_nether.js         End et Nether fermés
│   ├── spawns_safety.js            filet anti-monstres vanilla
│   ├── survivants.js               reproduction bloquée, échanges filtrés (MoreJS)
│   ├── plans_ingenieur.js          déblocage des recettes par plans
│   ├── saisons_cold_sweat.js       pont Ecliptic Seasons → Cold Sweat
│   ├── garde_manger.js             péremption lente, cave, glacière
│   ├── recipes/
│   │   ├── progression.js          verrous Overgeared → Create → rail → aviation
│   │   ├── remplacements.js        slime, poudre, Blaze Burner, quartz
│   │   ├── gisements_coe.js        gisements finis par biome
│   │   ├── cuisine_unifiee.js      Farm & Charm cantonné à la ferme, tags de saison
│   │   └── overgeared_pieces.js    recettes de forge des pièces fallow:* (types de recette à lire)
│   └── loot/
│       └── butin.js                élytres et œil retirés, os, plans, orbes (LootJS)
├── client_scripts/
│   └── visionneuse.js              entrées masquées dans JEI
├── data/
│   ├── fallow/function/init.mcfunction          gamerules au chargement
│   ├── minecraft/tags/function/load.json        → fallow:init
│   ├── fallow/neoforge/biome_modifier/*.json    monstres, donjons, blocs infestés retirés
│   ├── minecraft/tags/worldgen/biome/has_structure/stronghold.json   plus de strongholds
│   ├── minecraft/tags/enchantment/{tradeable,on_traded_equipment}.json   plus de Mending échangé
│   ├── minecraft/worldgen/template_pool/village/*/town_centers.json  villages en ruines (à écrire)
│   ├── minecraft/worldgen/structure_set/*.json  structures retirées (à écrire)
│   ├── fallow/advancement/**                    succès du carnet du colon (à écrire)
│   └── <mod>/neoforge/biome_modifier/*.json     {"type":"neoforge:none"} pour neutraliser un modifier
└── assets/
    ├── fallow/…                    textures et noms français des objets
    ├── ambientsounds/…             sons lointains rares (voir 5.6)
    └── minecraft/…                 panorama du menu, splashes en français
pack/config/incontrol/spawn.json    règles In Control!
```

## 7.2 Les scripts, dans l'ordre où les écrire

| # | Script ou donnée | Ce qu'il fait | Dépend de | Brouillon |
|---|---|---|---|---|
| 1 | `fallow:init` + `load.json` | Gamerules du monde, dont `reducedDebugInfo` et `doLimitedCrafting` | — | ✅ |
| 2 | `no_end_no_nether.js` + tag stronghold | End et Nether fermés, recettes d'œil retirées | `NativeEvents` | ✅ |
| 3 | Biome modifiers + `spawns_safety.js` + `incontrol/spawn.json` | Plus de monstres vanilla, hostiles moddés plafonnés | In Control! | ✅ |
| 4 | `saisons_cold_sweat.js` | Température saisonnière ressentie | bindings Ecliptic et Cold Sweat | ✅ |
| 5 | `recipes/remplacements.js` | Slime, poudre, Blaze Burner, quartz | KubeJS Create | ✅ |
| 6 | `recipes/cuisine_unifiee.js` + Kaleidoscope Compat | Farm & Charm cantonné à la ferme, KC seul système de cuisine, mangeoire retirée | Kaleidoscope Compat | ✅ |
| 7 | `objets_fallow.js` + `recipes/overgeared_pieces.js` | Pièces forgées qui verrouillent les machines | types de recettes Overgeared | partiel |
| 8 | `recipes/progression.js` | Verrous par âge | 7, KubeJS Create | ✅ |
| 9 | `plans_ingenieur.js` + `loot/butin.js` | Plans trouvés en exploration qui débloquent des recettes | 7, LootJS, Quark Auto Unlock coupé | ✅ |
| 10 | `survivants.js` + tags d'enchantement | Survivants : pas de reproduction, échanges filtrés | MoreJS | ✅ |
| 11 | Pools de villages + structure sets | Villages en ruines, structures retirées | Sparse Structures | à écrire |
| 12 | `recipes/gisements_coe.js` | Gisements finis par biome | COE | ✅ |
| 13 | ~~`garde_manger.js`~~ (retiré en v2) + config d'élevage Cattlestrophic | Aliments du bétail | Cattlestrophic | config seule |
| 14 | `visionneuse.js` | Masquer ce qui n'existe pas | — | ✅ |
| 15 | Succès + carnet Modonomicon | Guide du colon par âge | — | à écrire |
| 16 | Assets : sons AmbientSounds, luminances LambDynamicLights, presets ATMOSPHERICS, panorama | Ambiance | — | à écrire |
| 17 | `clairieres.js` (startup + server) + biome `fallow:clairiere` + preset ATMOSPHERICS ([10.3](10-recentrage.md#103-la-brume-qui-se-lève)) | Feux de clairière : la brume se retire autour de la base | ATMOSPHERICS, Overgeared, Create | ✅ (brouillon) |
| 18 | `zoo.js` + `lune_blanche.json` | Récompenses du bestiaire Wildex ; lune blanche | Wildex Bestiary, Enhanced Celestials | lune ✅ (brouillon), zoo à écrire |

Les numéros 1 à 6 doivent exister **avant le premier monde de test sérieux** : sans eux, le monde généré n'est pas celui de Fallow (monstres, strongholds, recettes de base).

## 7.3 Points d'API à confirmer

Ces points conditionnent plusieurs brouillons ; ils se vérifient en une soirée avec ProbeJS dans l'instance de test :
- `NativeEvents.onEvent` existe-t-il dans KubeJS 2101.7, et accepte-t-il les classes internes (`BlockEvent$PortalSpawnEvent`) ?
- `EntityEvents.spawned` est-il annulable, et `entity.tags` accessible ?
- `RecipeViewerEvents.removeEntries` (intégration JEI de KubeJS 2101) : nom exact.
- `kubejs/data` est-il pris en compte pour le **worldgen dès la création du monde** sur serveur dédié ? Sinon, plan B : le même dossier copié dans `world/datapacks/` avant la première génération.
- Syntaxe LootJS 3 et MoreJS pour 1.21.1 ; compatibilité de KubeJS Create avec Create 6.0.10.
- Overgeared : identifiant du composant de qualité et types de recettes exposés.
