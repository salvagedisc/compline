# La tannerie

Idée 4 tirée de Wayfarer ([recherche](recherche/2026-10-19/wayfarer.md), recommandation 4). Le cuir ne tombe plus tout fait des animaux : il faut une peau, des cendres et du temps. Le cuir devient un petit métier lié à la faune, à l'élevage et à la météo.

État : validé sur le serveur de test le 2026-10-19 (scripts sans erreur, butin vérifié avec `/loot spawn`, recettes : 4 ajoutées, 5 retirées). Pas encore testé avec un client : infobulles, plateau, EMI. Kaleidoscope Cookery 1.6.0 garde le même format de plateau.

## La chaîne, pour un joueur

1. **Récupérer une peau.** Une vache, un cheval, un cerf, un élan ou un zèbre ne lâche plus de cuir mais une **peau brute**. L'infobulle dit quoi en faire.
2. **Trouver des cendres.** Les cendres de Supplementaries restent quand un bloc de bois brûle, ou quand une créature meurt dans le feu. Le plus simple : poser du **charbon de bois sur un feu de camp**, qui donne une poignée de cendres au bout de 30 secondes.
3. **Écharner et saler.** À l'établi, une peau brute et des cendres donnent une **peau préparée**.
4. **Faire tremper.** On pose la peau préparée sur un **plateau en bambou** (Kaleidoscope Cookery) et on la laisse tremper. Le plateau mouille ce qu'il porte **sous la pluie**, ou **sous un stalactite de pierre de goutte** quand un bloc d'eau se trouve au-dessus, comme pour remplir un chaudron. Au bout de 5 minutes de trempage, la peau devient du **cuir**. Une pile entière trempe d'un coup.

Pour tanner toute l'année, on construit une petite tannerie : de l'eau, en dessous un bloc de pierre de goutte avec un stalactite, puis le plateau. Sinon, on attend la pluie, qui dépend de la saison (Ecliptic Seasons).

Le lapin est retiré du jeu depuis le 2026-10-19 ([faune-nml](faune-nml.md)) : il n'y a plus de cuir de peaux de lapin.

## Les animaux concernés

Le cuir, ou la peau qui cuisait en cuir au four, devient une peau brute. Les quantités et le bonus de Butin d'origine restent les mêmes.

| Mod | Animaux | Table de butin |
|---|---|---|
| Vanilla | vache, champimeuh, cheval, âne, mule, lama, lama de marchand | `minecraft:entities/cow`, `mooshroom`, `horse`, `donkey`, `mule`, `llama`, `trader_llama` |
| Vanilla | hoglin (Nether fermé, sans effet) | `minecraft:entities/hoglin` |
| Icy's Better Horses | les 15 races de chevaux | leurs tables renvoient vers `minecraft:entities/horse` |
| No Man's Land | élan | `nomansland:entities/moose` |
| No Man's Land | cerf (ne lâchait pas de cuir : peau ajoutée, 0 ou 1, plus 0 à 1 par niveau de Butin) | `nomansland:entities/deer` |
| Naturalist | cerf, zèbre (cuir) | `naturalist:entities/deer`, `zebra` |
| Naturalist | sanglier, éléphant, rhinocéros, dragon de Komodo (peau « hide ») | `naturalist:entities/boar`, `elephant`, `rhino`, `komodo_dragon` |
| Hybrid Aquatic | dugong, lamantin | `hybrid_aquatic:entities/dugong`, `manatee` |
| Friends & Foes | lumimeuh | `friendsandfoes:entities/moobloom` |
| Quark | renard de feu (Nether, sans effet) | `quark:entities/foxhound` |
| Alex's Mobs | kangourou : garde sa peau, mais deux peaux de kangourou donnent une peau brute au lieu d'un cuir | recette |

Vérifié sans cuir, donc sans changement : Critters and Companions, Adorable Hamster Pets, Mowzie's Mobs, Companions!, Ghost, Longwings, et le reste d'Alex's Mobs (le bison et l'ours donnent de la fourrure, qui ne servait pas au cuir). Les fourrures de Naturalist (ours, lion, tigre, mammouth…) restent aussi.

Le script écrit dans le journal du serveur toute table de créature qui lâche encore du cuir (`[tannerie] créature qui lâche encore du cuir`). Un nouveau mod de faune s'y verra tout de suite.

## Les recettes

Ajoutées (`pack/kubejs/server_scripts/tannerie.js`) :

| Identifiant | Où | Recette |
|---|---|---|
| `fallow:tannerie/echarner_et_saler` | établi | peau brute (tag `#fallow:peaux_brutes`) + cendres (`#c:dusts/ash`) → peau préparée |
| `fallow:tannerie/tremper` | plateau en bambou, humidification | peau préparée → cuir, 6000 ticks |
| `fallow:tannerie/cendres_de_charbon` | feu de camp | charbon de bois → cendres, 600 ticks |
| `fallow:tannerie/peau_de_kangourou` | établi | 2 peaux de kangourou → peau brute |
| `fallow:tannerie/fumer` (éteinte) | feu de camp | peau préparée → cuir, 2400 ticks ; secours si le bambou est trop rare, à allumer avec `TANNERIE_SECOURS_FUMAGE = true` |

Le tag `#fallow:peaux_brutes` (`pack/kubejs/data/fallow/tags/item/peaux_brutes.json`) contient la peau brute et la peau de Naturalist, pour que les peaux de Naturalist d'un monde existant servent encore.

Retirées, parce qu'elles donnaient du cuir trop facilement ou hors de la chaîne :

| Recette | Ce qu'elle faisait |
|---|---|
| `kaleidoscope_cookery:bamboo_tray/drying/rotten_flesh_to_leather` | chair putréfiée séchée au soleil → cuir |
| `envelope:mailing/automated_supply_service/leather` | 6 chairs putréfiées envoyées par pigeon → cuir |
| `naturalist:leather_from_smelting_hide` | peau de Naturalist au four → cuir en 10 secondes |
| `alexsmobs:kangaroo_hide_to_leather` | 2 peaux de kangourou → cuir (remplacée, voir plus haut) |
| `kaleidoscope_cookery:bamboo_tray/wetting/leather_to_rotten_flesh` | le cuir mouillé pourrit en chair putréfiée. Ce n'est pas une source, mais le plateau rendait le cuir tanné à la pluie suivante s'il restait dessus |

Gardées, parce qu'elles recyclent du cuir déjà fait ou viennent d'un butin rare :
- quatre peaux de lapin → cuir (vanilla, recette reprise à l'identique par Naturalist), sans effet depuis le retrait du lapin ;
- Create : selle au moulin, armures de cheval au concasseur ; Farm & Charm : armure de cheval en cuir au hachoir ; Quark : bloc de cuir lié → 9 cuirs ;
- Cold Sweat : peau de hoglin → 2 cuirs (le Nether est fermé, la peau est introuvable) ;
- cuir dans quelques coffres : lettres perdues d'Envelope, pots des alchimistes de NML, grenier de la cabane de l'illusionniste (Friends & Foes), pêche vanilla.

## Les usages du cuir

Tous gardent `minecraft:leather` et ne changent pas : Traveler's Backpack (52 recettes, dont les sacs et les améliorations), Supplementaries (soufflet), les livres vanilla, le bestiaire Wildex, Quark (sac à graines, sac à dos), Alex's Mobs, Cold Sweat (outre), Hybrid Aquatic, Icy's Better Horses, Kaleidoscope Cookery (tenue de fermier, sac-repas), Companions!, Create (boîte à outils), Aeronautics (lunettes d'aviateur), NML (bourse, lit de paille). Aucune ne dépend d'une source de cuir retirée.

Overgeared 1.6.19 n'a **aucune recette au cuir** : ses armures se forgent avec des plaques de métal. Les « armures plaquées » sur une base de cuir sont une idée de Wayfarer, pas d'Overgeared.

Le cuir est demandé tôt : le bestiaire Wildex (2 cuirs) et le premier livre. C'est la raison du secours éteint décrit plus haut.

## Pourquoi le plateau en bambou

Le plateau de Kaleidoscope Cookery est le seul contenant du pack qui fait passer du temps sur un objet posé, avec deux modes : il sèche ce qu'il porte au soleil et le mouille sous la pluie ou sous un stalactite. Il faisait déjà du cuir (avec de la chair putréfiée). Il ne demande aucun nouveau mod, se branche sur la météo et les saisons, et une tannerie à stalactite est une petite construction. Les autres voies sont moins bonnes : un clic sur un chaudron d'eau est instantané, le lavage et le dépôt de Create arrivent trop tard dans la progression, le silo de Farm & Charm ne fait que sécher.

## Fichiers

- `pack/kubejs/startup_scripts/tannerie.js` : les objets `fallow:peau_brute` et `fallow:peau_preparee` (texture vanilla `minecraft:item/rabbit_hide`, teinte rosée ou grise, aucun PNG).
- `pack/kubejs/server_scripts/tannerie.js` : recettes, retraits, infobulles, contrôle des recettes de cuir restantes.
- `pack/kubejs/server_scripts/tannerie_butin.js` : le butin (LootJS).
- `pack/kubejs/data/fallow/tags/item/peaux_brutes.json` : le tag des peaux brutes.
- Textes : noms et infobulles en français et en anglais, à fusionner dans `pack/kubejs/assets/fallow/lang/`.

## Ce qui reste à tester

Au démarrage du serveur de test (journal KubeJS) :
- les deux objets s'enregistrent avec `.texture()` et `.color(0, …)` (KubeJS 7) ; la langue fusionnée l'emporte sur le nom anglais que KubeJS génère ;
- `LootJS.lootTables`, `hasLootTable`, `getLootTable(...).replaceItem(...)`, `createPool`, `LootEntry.of(...).setCount([0, 1]).applyEnchantmentBonus([0, 1])` passent sans erreur, et aucune ligne `table absente` n'apparaît ;
- les lignes `[tannerie] recette donnant du cuir` ne listent plus que les recettes gardées ci-dessus, et `créature qui lâche encore du cuir` ne liste rien ;
- `event.custom` accepte la recette `kaleidoscope_cookery:bamboo_tray` et `event.campfireCooking(...).cookingTime(...)` passe ;
- `ItemEvents.modifyTooltips` dans un script serveur arrive bien au client (KubeJS 7 synchronise ces infobulles) ; `Text.gray(Text.translatable(...))` s'affiche traduit.

En jeu :
- tuer une vache, un cheval d'Icy's Better Horses et un cerf de NML : chacun lâche une peau brute, avec Butin III aussi ;
- l'établi accepte la peau brute et les cendres, et aussi une peau de Naturalist ;
- le charbon de bois sur un feu de camp donne des cendres ;
- la peau préparée devient du cuir sur le plateau sous la pluie, puis sous un stalactite alimenté en eau ; le cuir ne pourrit plus quand il reste sur le plateau ;
- le plateau accepte une pile entière et rend autant de cuirs ;
- EMI montre les deux étapes et plus les recettes retirées ;
- vérifier que le bambou se trouve sans trop chercher dans un monde NML (jungles de bambou), et que la pierre de goutte existe dans les grottes. Si le bambou est trop rare, allumer `TANNERIE_SECOURS_FUMAGE` ;
- après la montée de Kaleidoscope Cookery en 1.6.0, vérifier que le type `kaleidoscope_cookery:bamboo_tray` et le champ `subtype` n'ont pas changé ;
- régler `TANNERIE_DUREE_TREMPAGE` (5 minutes) une fois joué.
