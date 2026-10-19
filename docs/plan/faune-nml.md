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
| Wildex | `pack/config/wildex/wildex-common.toml` | `[wildexList] excludedModIds` : les cinq créatures, absentes de l'interface, des découvertes et du taux de complétion |

Les 15 objets : œufs d'apparition du cerf, de l'oie, du bar et de la tortue ; venaison crue et cuite ; bar cru, cuit, en seau et en tonneau ; écaille robuste, carapace et œuf de tortue ; le bâton de débogage des pots (`pot_debug_stick`) et l'icône de l'onglet créatif (`no_mans_globe`). Les seules recettes qui consomment ces objets les produisent aussi.

On garde le Compagnon (`buddy`, quête de la [Lune Amie](lune-amie.md)), le pot vivant des ruines des alchimistes et le joueur rêveur.

**Limite connue** : Fresh Animations v1.11 n'a pas de modèle pour le lapin. Sans le remodèle, il redevient le lapin vanilla, sans animation.

## Questions ouvertes

1. Lot B, à désactiver ou non : viande de cheval, cuisses de grenouille, explosifs et flèches incendiaires, plats de NML.
2. Les répliques d'offrande de l'oie et de la tortue visent des créatures coupées : les rediriger vers `untitledduckmod:goose` et `naturalist:tortoise` ?
3. La réplique d'offrande `scute` de NML exige l'objet `nomansland:tortoise_scute`, qui n'existe pas (l'écaille s'appelle `sturdy_scute`). Si le chargeur applique la condition, elle ne se charge jamais, et le test L8 de la Lune Amie échoue.
4. `tannerie_butin.js` cite encore `nomansland:entities/moose` (non enregistré) et la table du cerf (plus utilisée). Sans effet en jeu.

## Tests en jeu

Sur un monde neuf : les données de registre se lisent à la création du monde.

| # | Test | Attendu |
|---|---|---|
| F1 | Logs serveur et client au lancement | Aucune erreur sur `mixed_litter`, `biome_modifier`, `variant_groups`, `faune_nml.js` |
| F2 | `/summon` cochon, vache, mooshroom, poulet, mouton, calmar, calmar luisant, lapin | Modèles vanilla animés par Fresh Animations ; lapin vanilla figé |
| F3 | Forêt, taïga, rivière, marais, jungle, badlands, forêts de NML | Ni cerf, ni oie, ni bar, ni tortue ; morue toujours dans les marais |
| F4 | Grottes, dont les grottes luxuriantes | Plus de terriers de tortue |
| F5 | Renard, chèvre, âne, loup en `autumnal_forest` | Textures vanilla ; aucun loup doré ou « timber », aucune grenouille de boue |
| F6 | `/summon nomansland:frienderman` | Il disparaît aussitôt |
| F7 | EMI : chercher « venaison », « bar », « tortue » | Rien |
| F8 | Wildex | Aucune entrée pour les cinq créatures |
| F9 | Île aux champignons | Ronds et Compagnon présents ; ascension (L10) inchangée |
