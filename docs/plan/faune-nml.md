# La faune de No Man's Land revient au vanilla

Demande de l'équipe du 2026-10-19 : les animaux de No Man's Land ne vont pas avec Fresh Animations ; on garde les animaux vanilla, on coupe les autres créatures et les objets qui n'ont plus de source. Tout ce qui suit est lu dans les jars (NML 1.6.1, Mixed Litter 1.2.2 qu'il embarque, Fresh Animations v1.11, Wildex 3.0.0, KubeJS 2101.7.2). **Rien n'a encore été testé en jeu.**

## Ce que fait NML aux animaux

- **Remodèles** : Mixed Litter, embarqué dans NML, remplace le rendu de huit animaux vanilla (cochon, vache, mooshroom, poulet, mouton, lapin, calmar, calmar luisant). Fresh Animations ne les anime plus.
- **Textures** : NML pose ses propres textures sur onze autres animaux vanilla (abeille, chat, morue, dauphin, âne, renard, chèvre, husk, saumon, slime, tortue de mer), plus deux variantes de loup et une de grenouille.
- **Créatures** : cerf, oie, bar à serpe et tortue terrestre, ajoutés par des biome modifiers et par des terriers dans les grottes. Les biomes de NML eux-mêmes ne listent que des mobs vanilla.
- **Frienderman** : il remplace 1 % des endermen naturels de l'Overworld, sans réglage dans la config de NML.
- L'élan (`moose`) n'est pas enregistré en 1.6.1 ; `buried`, `cave_carp` et `fat_joint` n'ont qu'une clé de langue.

## Ce que fait Fallow

| Quoi | Où | Comment |
|---|---|---|
| Remodèles coupés | `pack/config/mixed_litter-startup.toml` | les six clés `[remodels]` à `false` |
| Textures NML coupées | `pack/kubejs/data/nomansland/mixed_litter/variant_groups/` (11 fichiers) | copie du jar avec `"unobtainable": true` en tête des conditions |
| Loups dorés et « timber », grenouille de boue | `pack/kubejs/data/nomansland/tags/worldgen/biome/entity/` | tags vidés (`"replace": true`) |
| Cerf, oie, tortue, terriers | `pack/kubejs/data/nomansland/neoforge/biome_modifier/` | biome modifiers remplacés par `neoforge:none` |
| Bar à serpe | `…/biome_modifier/spawns/add_spawns_to_swamps.json` | seule la morue reste |
| Frienderman | `pack/kubejs/server_scripts/faune_nml.js` | `EntityEvents.spawned` annulé : l'enderman qu'il remplace est déjà annulé par NML, il n'y a donc plus rien |
| 15 objets sans source | `faune_nml.js` (serveur et client) | recettes retirées, objets cachés d'EMI ; le créatif reste un outil d'admin |
| Wildex | `pack/config/wildex/wildex-common.toml` | `[wildexList] excludedModIds` : les cinq créatures et le lapin, absents de l'interface, des découvertes et du taux de complétion |
| 15 objets retirés (lot B) | `faune_nml.js` | recettes retirées, objets retirés de toutes les tables de butin, cachés d'EMI |
| TNT | `faune_nml.js` | recette vanilla 1.21.1 remise sous l'id `minecraft:tnt` |
| Succès « explode_ore » | `pack/kubejs/data/nomansland/advancement/main/explode_ore.json` | retiré par une condition `neoforge:false` |
| Lapin | `pack/kubejs/data/fallow/neoforge/biome_modifier/sans_lapin.json`, `sans_lapin.js` (serveur et client) | apparitions retirées de `#minecraft:is_overworld` ; objets retirés des recettes, des butins et d'EMI |
| Réplique d'offrande `scute` | `pack/kubejs/data/nomansland/nomansland/dialogue_pools/offering/scute.json` | condition impossible retirée (voir plus bas) |
| Répliques d'offrande de l'oie et de la tortue | `…/offering/entity/goose.json`, `tortoise.json` | redirigées vers des créatures présentes |

Les 15 objets : œufs d'apparition du cerf, de l'oie, du bar et de la tortue ; venaison crue et cuite ; bar cru, cuit, en seau et en tonneau ; écaille robuste, carapace et œuf de tortue ; le bâton de débogage des pots (`pot_debug_stick`) et l'icône de l'onglet créatif (`no_mans_globe`). Les seules recettes qui consomment ces objets les produisent aussi.

On garde le Compagnon (`buddy`, quête de la [Lune Amie](lune-amie.md)), le pot vivant des ruines des alchimistes et le joueur rêveur.

**Lot B** (décision de l'équipe du 2026-10-19) : viande de cheval (`raw_horse`, `horse_steak`), cuisses de grenouille (`frog_leg`, `cooked_frog_leg`), explosifs et flèche (`firebomb`, `ink_bomb`, `explosive`, `incendiary_arrow`) et sept plats (`pancake`, `maple_tart`, `sweet_tart`, `trail_mix`, `syruped_pear`, `honeyed_apple`, `hardtack`). NML les ajoute aux butins vanilla par des modificateurs qui tirent ses propres tables (`entities/horse_addition`, `chests/hardtack_addition`…) : retirer l'objet de toutes les tables suffit. Deux conséquences :
- NML remplace la recette de la TNT par quatre explosifs. Sans explosif, la recette vanilla est remise.
- Le succès `explode_ore` (miner un minerai avec un explosif) ne peut plus s'obtenir : il est retiré.

**Lapin** (même décision) : Fresh Animations v1.11 n'a pas de modèle pour lui ; sans le remodèle, il détonnait. Il est retiré du jeu. NML inscrit ses biomes dans `#minecraft:is_overworld`, donc un seul biome modifier `neoforge:remove_spawns` couvre tout l'Overworld. Ses six objets (œuf, viande crue et cuite, ragoût, patte, peau) sortent des recettes et de toutes les tables, dont le cadeau matinal du chat. Conséquences : la potion de saut ne se brasse plus (patte de lapin), le cuir ne vient plus des peaux de lapin, le Champilièvre d'Alex's Mobs ne peut plus naître d'un lapin.

**Réplique `scute`** : NML 1.6.1 la conditionne à l'objet `nomansland:tortoise_scute`, qui n'existe pas (l'écaille s'appelle `sturdy_scute`). Les répliques sont des registres de datapack, qui sautent une entrée dont les `neoforge:conditions` échouent : elle ne se chargeait jamais, et le test L8 de la Lune Amie échouait. La surcharge retire la condition et garde les écailles de tortue de mer et de tatou. Test : `scripts/test/nml-dialogues.test.mjs`.

**Répliques d'offrande** : celle de la tortue vise `naturalist:tortoise`, avec le même texte. Celle de l'oie vise le canard de Naturalist (`naturalist:duck`), qui vit dans les mêmes rivières et marais, puisqu'Untitled Duck Mod est retiré. Le canard n'attaque pas : « Bête bruyante et furieuse » devient « Bête bruyante et têtue », en anglais (`goose.json`) comme en français. Test : `scripts/test/nml-dialogues.test.mjs`.

## Questions ouvertes

1. `tannerie_butin.js` cite encore `nomansland:entities/moose` (non enregistré) et la table du cerf (plus utilisée). Sans effet en jeu.

## Tests en jeu

Sur un monde neuf : les données de registre se lisent à la création du monde.

| # | Test | Attendu |
|---|---|---|
| F1 | Logs serveur et client au lancement | Aucune erreur sur `mixed_litter`, `biome_modifier`, `variant_groups`, `faune_nml.js` |
| F2 | `/summon` cochon, vache, mooshroom, poulet, mouton, calmar, calmar luisant | Modèles vanilla animés par Fresh Animations |
| F3 | Forêt, taïga, rivière, marais, jungle, badlands, forêts de NML | Ni cerf, ni oie, ni bar, ni tortue ; morue toujours dans les marais |
| F4 | Grottes, dont les grottes luxuriantes | Plus de terriers de tortue |
| F5 | Renard, chèvre, âne, loup en `autumnal_forest` | Textures vanilla ; aucun loup doré ou « timber », aucune grenouille de boue |
| F6 | `/summon nomansland:frienderman` | Il disparaît aussitôt |
| F7 | EMI : chercher « venaison », « bar », « tortue », « lapin », « cheval », « explosif », « crêpe » | Rien |
| F8 | Wildex | Aucune entrée pour les cinq créatures ni pour le lapin |
| F9 | Île aux champignons | Ronds et Compagnon présents ; ascension (L10) inchangée |
| F10 | Désert, plaines enneigées, prairie fleurie, bosquet | Aucun lapin |
| F11 | Tuer un cheval, une grenouille ; ouvrir des coffres de donjon, d'igloo, d'épave | Ni viande de cheval, ni cuisses, ni galette, ni bombe |
| F12 | Établi : 5 poudres à canon et 4 sables | Une TNT |
| F13 | Offrir une écaille de tortue de mer à la Lune Amie (L8) | La réplique « A precious gift from a wise creature » |
| F14 | Montrer une tortue de Naturalist, puis un canard de Naturalist, à la Lune Amie | Les répliques de la tortue et de l'oie |
