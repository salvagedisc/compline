# Santé maximale : la perdre en mourant, la regagner en explorant

Idée 3 tirée de Wayfarer ([recherche du 2026-10-19](recherche/2026-10-19/wayfarer.md), recommandation 3). La mort coûte quelque chose sans ajouter de combat, et pour réparer cette perte il faut repartir explorer.

## La règle, pour un joueur

- Vous commencez avec 10 cœurs.
- Chaque mort vous retire 1 cœur de santé maximale. Au réveil, un message vous dit combien il vous en reste.
- Vous ne descendez jamais sous 5 cœurs.
- Un **Éclat de vie** vous rend 1 cœur et le remplit. Un clic droit suffit. Vous ne dépassez jamais 10 cœurs : avec une santé entière, l'éclat refuse de servir et le dit.
- On trouve les éclats en explorant : dans les ruines de No Man's Land, sous la mer et près des Cœurs anciens des monuments. Aucune créature n'en donne, et aucune recette n'en fabrique.
- L'infobulle de l'éclat montre vos cœurs actuels (par exemple « Cœurs : 7 / 10 »).
- Vos cœurs perdus restent perdus après une déconnexion ou un redémarrage du serveur.

## Fichiers

| Fichier | Rôle |
|---|---|
| `pack/kubejs/startup_scripts/sante.js` | L'objet `fallow:eclat_de_vie` : texture vanilla `minecraft:item/quartz` teintée en rouge doux, pile de 16, rareté « peu commun » |
| `pack/kubejs/server_scripts/sante.js` | La perte à la mort, le réveil, l'usage de l'éclat, la récompense du Cœur ancien |
| `pack/kubejs/server_scripts/sante_butin.js` | Les tables de butin et leurs taux (LootJS) |
| `pack/kubejs/client_scripts/sante.js` | L'infobulle : la ligne fixe et le compteur de cœurs |
| Langues | Clés `item.fallow.eclat_de_vie*` et `fallow.sante.*`, à fusionner dans `pack/kubejs/assets/fallow/lang/` |

Aucune table de butin n'est remplacée et aucun PNG n'est ajouté. Le modèle d'objet est généré par KubeJS à partir de la texture vanilla.

## Fonctionnement technique

- **État** : le nombre de cœurs perdus est un entier `fallow_sante_perdus` dans les données persistantes KubeJS du joueur. Elles sont enregistrées avec le joueur (clé NBT `KubeJSPersistentData`), et KubeJS les recopie dans le nouveau joueur à chaque réapparition.
- **Effet** : un seul modificateur `fallow:mort` de l'attribut `minecraft:generic.max_health`, de type `add_value`, valant −2 × cœurs perdus. Il est transitoire : le script le repose à la connexion (`PlayerEvents.loggedIn`), au clonage (`PlayerEvents.cloned`) et au réveil (`PlayerEvents.respawned`). En 1.21.1, une mort ne recopie que les valeurs de base des attributs (`assignBaseValues`), donc les modificateurs disparaissent. Il faut les reposer.
- **Mort** : elle est comptée dans `PlayerEvents.cloned` quand `keepData` est faux, c'est-à-dire quand NeoForge signale `wasDeath`. Un totem d'immortalité empêche la mort, donc il n'y a pas de perte.
- **Éclat** : `ItemEvents.rightClicked`. Il rend 1 cœur et soigne 2 points. L'objet a un temps de recharge d'une seconde. Le joueur fait un geste du bras, entend le son vanilla `block.amethyst_block.chime` et voit des particules de cœur. En créatif, l'éclat n'est pas consommé.
- **Cœur ancien** : les monuments d'Ancient Remnants n'ont **aucun coffre**. Les gabarits de structure du jar 1.5.2 ne contiennent que des spawners, et AR n'a pas de table de coffre. La récompense vient donc de l'entité `ancient_remnants:elderheart` : toutes les 5 secondes, un joueur à 16 blocs ou moins d'un Cœur ancien qu'il n'a jamais approché reçoit un éclat. C'est le rayon dans lequel AR donne sa bénédiction. Le script retient l'UUID de chaque Cœur ancien dans `fallow_sante_coeurs_anciens`, donc un monument donne un éclat par joueur, une seule fois.

## Sources et taux

Un taux est la chance qu'**un tirage** de la table donne un éclat. Un tirage, c'est un coffre ouvert, une jarre brisée ou un bloc suspect brossé. Avec Lootr, chaque joueur a son propre tirage pour chaque coffre.

### Coffres et jarres (l'éclat s'ajoute au butin)

| Table | Où | Taux |
|---|---|---|
| `nomansland:chests/small_dungeon_core` | Cœur des petits donjons souterrains (espacement 10/4, structure fréquente) | 10 % |
| `nomansland:chests/water_well` | Fond des puits profonds (46/18) | 20 % |
| `nomansland:chests/sniffer_monument` | Monument du renifleur (jungle, 50/28) | 25 % |
| `nomansland:chests/alchemist_pot_treasure` | Jarres « trésor » des ruines d'alchimiste et des sanctuaires à cloche. Elles se regarnissent, d'où le taux bas | 3 % |
| `minecraft:chests/shipwreck_treasure` | Coffre au trésor des épaves | 15 % |
| `minecraft:chests/shipwreck_supply` | Coffre de provisions des épaves | 5 % |
| `minecraft:chests/underwater_ruin_big` | Grandes ruines océaniques | 12 % |
| `minecraft:chests/underwater_ruin_small` | Petites ruines océaniques | 5 % |

### Archéologie (l'éclat remplace la trouvaille)

Un bloc suspect ne rend qu'un objet : quand l'éclat sort, il remplace l'objet prévu.

| Table | Où | Taux |
|---|---|---|
| `nomansland:archaeology/alchemist_ruins` | Gravier suspect des ruines d'alchimiste et des sanctuaires à cloche | 3 % |
| `nomansland:archaeology/well` | Gravier suspect des puits profonds | 5 % |
| `nomansland:archaeology/sniffer_monument` | Monument du renifleur | 8 % |
| `minecraft:archaeology/ocean_ruin_cold` | Ruines océaniques froides | 4 % |
| `minecraft:archaeology/ocean_ruin_warm` | Ruines océaniques chaudes | 4 % |

### Cœurs anciens (Ancient Remnants)

Il y en a un par monument (6 types, espacement 100/20) : 1 éclat au premier passage de chaque joueur.

### Écartées

- `minecraft:chests/simple_dungeon` : NML l'emploie dans les salles des petits donjons, qui comptent plusieurs coffres. L'éclat y serait trop courant.
- `nomansland:chests/ancient_pot_cave` : ces jarres sont partout dans les grottes, ce ne sont pas des ruines.
- `nomansland:archaeology/trinket` et `alchemist_maps` : ce sont des sous-tables que NML greffe sur l'archéologie vanilla (`trail_ruins`, `desert_well`, `desert_pyramid`, `ocean_ruin_*`). `remains_dungeon` n'est référencée par aucun gabarit ni aucun processeur du jar 1.6.1.
- `minecraft:archaeology/desert_well` : NML s'en sert pour ses puits du désert, très fréquents (16/5).
- Les autres monuments de NML (creeper, noyé), les menhirs et les ruines du désert : leurs gabarits n'ont aucun coffre lié à une table.

### Ordre de grandeur visé

Un joueur qui explore régulièrement trouve un éclat **toutes les 2 à 3 heures**. Dans une heure d'exploration typique, il visite environ un ou deux petits donjons (environ 0,15 éclat), un puits ou une ruine d'alchimiste avec ses jarres et ses graviers (environ 0,2 à 0,3) et, de temps en temps, une épave. C'est une estimation à vérifier en jeu : Sparse Structures écarte encore les structures (facteur par défaut), sauf celles d'Ancient Remnants.

## Régler les nombres

Tous les nombres sont des constantes nommées en tête de fichier.

- `server_scripts/sante.js` :
  - `SANTE_COEURS_DEPART` (10), `SANTE_COEURS_PLANCHER` (5) ;
  - `SANTE_COEURS_PERDUS_PAR_MORT` (1), `SANTE_COEURS_RENDUS_PAR_ECLAT` (1) ;
  - `SANTE_SOIN_A_L_USAGE` (2 points), `SANTE_DELAI_ECLAT` (20 ticks) ;
  - `SANTE_COEUR_ANCIEN_ECLATS` (1), `SANTE_COEUR_ANCIEN_RAYON` (16), `SANTE_COEUR_ANCIEN_INTERVALLE` (100 ticks).
- `server_scripts/sante_butin.js` : les objets `SANTE_BUTIN_COFFRES` et `SANTE_BUTIN_ARCHEOLOGIE`, sous la forme table → chance (de 0 à 1). Pour couper une source, retirez sa ligne.
- `client_scripts/sante.js` : `SANTE_CLIENT_COEURS_DEPART` doit rester égal à `SANTE_COEURS_DEPART`.
- Après une modification : `/reload` pour les scripts serveur et le butin, F3+T pour l'infobulle. Le script de démarrage (l'objet) demande un redémarrage.
- **Ne renommez pas** `fallow:mort` ni les clés `fallow_sante_*` sur un monde en cours : les joueurs perdraient leur état.

## Compatibilité avec les autres sources de santé maximale

J'ai cherché `MAX_HEALTH` et `max_health` dans tous les jars du pack.

- **Ancient Remnants** : aucune bénédiction ne touche la santé maximale. Vitalité désespérée donne Régénération sous 33 % de la santé, et Égide du sentinelle donne de l'armure.
- **Cold Sweat, Overgeared, Kaleidoscope Cookery, Thirst Was Reclaimed** : rien sur la santé maximale du joueur. Cold Sweat ne s'en sert que pour son caméléon.
- **Traveler's Backpack** : le sac à dos du Warden ajoute +4 avec son propre identifiant `travelersbackpack:warden_backpack_max_health`. Les deux modificateurs s'additionnent.
- **Effet vanilla Bonus de vie** : il a lui aussi son propre modificateur, et ils s'additionnent.
- Le script ne lit et ne modifie que `fallow:mort`. Le plafond de 10 cœurs ne vaut que pour l'éclat : un bonus d'une autre source peut toujours dépasser 10.

## Vérifié sur le serveur de test (2026-10-19, sans joueur)

- 0 erreur KubeJS : 2/2 scripts de démarrage, 5/5 scripts serveur. `fallow:eclat_de_vie` est enregistré : `/summon item` renvoie `item.fallow.eclat_de_vie`.
- Butin, éclats obtenus par `/loot spawn` : `shipwreck_treasure` 10/60 puis 10/40, `small_dungeon_core` 8/100, `water_well` 10/60, `sniffer_monument` 19/60, `alchemist_pot_treasure` 3/200, `shipwreck_supply` 10/120, `underwater_ruin_big` 13/100, `underwater_ruin_small` 4/120. Les taux réels sont proches des taux prévus.
- Archéologie : au plus un objet par tirage (197/200, 149/150, 120/120, 149/150 et 150/150 entités), avec 2 à 7 % d'éclats. Aucun avertissement « too many items ».
- API essayées sur un cochon :
  - `modifyAttribute(..., 'add_value')` avec des chaînes fonctionne. Le reposer remplace l'ancien modificateur, et `removeAttribute` le retire. `getAttribute(...).getModifier('fallow:mort').amount()` lit bien la valeur.
  - `persistentData` : `getInt`, `putInt`, `getBoolean`, `getString` et `remove` fonctionnent.
  - `getEntitiesWithin(AABB.of(...))` et `.type` fonctionnent. Ils voient un `ancient_remnants:elderheart` invoqué.
  - La santé n'est **pas** ramenée automatiquement au nouveau maximum : le script le fait lui-même.
- Deux pièges de Rhino, corrigés dans les scripts :
  - `const` dans un bloc `try` lève « redeclaration of var » : le client utilise `let` ;
  - `entity.distanceTo(autre)` est ambigu : la distance est calculée à la main.
- Pas vérifiable sans joueur : la mort, le réveil, l'éclat au clic droit (`cooldowns`, `swing`, `heal`), la récompense du Cœur ancien dans `PlayerEvents.tick`, l'infobulle et la teinte (côté client), et les jarres de NML brisées en jeu.

## Ce qui reste à tester en jeu

- [ ] Mourir 6 fois : 9, 8, 7, 6, 5, puis 5 cœurs. Le message au réveil s'affiche, et celui du plancher aussi.
- [ ] Se déconnecter et se reconnecter, puis redémarrer le serveur : la santé maximale reste la même.
- [ ] `/give @s fallow:eclat_de_vie 3` : chaque éclat rend un cœur plein. À 10 cœurs, l'éclat est refusé avec un message, et il n'est pas consommé.
- [ ] L'infobulle affiche « Rend un cœur perdu. » et le bon compteur après une mort et après un éclat.
- [ ] La teinte rouge de l'objet, en main et dans l'inventaire.
- [ ] Deux éclats, un dans chaque main : un seul est consommé par clic.
- [ ] Butin : `/loot give @s loot nomansland:chests/water_well`, à répéter. L'éclat doit sortir environ une fois sur cinq. Même essai avec un coffre de Lootr, à deux joueurs.
- [ ] Archéologie : brosser du gravier suspect dans une ruine d'alchimiste. L'objet obtenu doit être unique, et il n'y a pas d'avertissement « too many items » dans le journal. Sur `ocean_ruin_*`, le bibelot que NML ajoute (0,2 %) peut tomber en même temps que l'éclat : il faut voir lequel des deux reste.
- [ ] Les épaves et les ruines océaniques existent-elles dans un monde NML ? Tester avec `/locate structure minecraft:shipwreck` et `minecraft:ocean_ruin_cold`.
- [ ] Cœur ancien : approcher un monument et recevoir un éclat et un message. Revenir, et ne rien recevoir de plus. Un second joueur reçoit le sien.
- [ ] Avec le sac à dos du Warden ou l'effet Bonus de vie : les deux bonus s'additionnent, et le compteur reste juste.
- [ ] Mesurer sur 2 à 3 heures d'exploration réelle combien d'éclats tombent, puis ajuster les taux.
