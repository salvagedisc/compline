# 8. Mise en place : phases, vérifications, décisions

> Les phases restent valables pour les mods de la v2. Les décisions du 8.4 sont remplacées par le [10.9](10-recentrage.md#109-décisions-à-valider-remplace-le-84).

Chaque phase suit [CONTRIBUTING.md](https://github.com/salvagedisc/fallow/blob/main/CONTRIBUTING.md) : une branche `test/<sujet>`, un mod par commit, la config et les scripts dans `pack/`, une fiche dans `docs/decisions/` pour chaque arbitrage, et la CI verte (`check-deps`, export `.mrpack`) avant fusion. **Le `packwiz mr add` tranche la disponibilité Modrinth** : la recherche n'a pas pu l'établir pour tous les mods (voir [README](README.md#limites-de-ce-plan)), et un mod absent en 1.21.1 NeoForge est remplacé par son repli indiqué dans le chapitre concerné.

## 8.1 Phases

| Phase | Branche | Contenu | Critère de sortie |
|---|---|---|---|
| **0. Socle actuel** | `main` | Test de lancement du pack actuel (déjà au programme de `v0.1.0`) ; config Quark comparée à celle générée ; **Entity Culling testé sur un dirigeable assemblé** | Monde créé sans crash ; Entity Culling gardé (réglé) ou retiré ; tag `v0.1.0` |
| **1. Règles de base** | `test/regles-de-base` | Neruina, JEI, In Control!, LootJS, KubeJS Create, MoreJS, Almost Unified ; scripts 1 à 6 du [7.2](07-kubejs-et-datapack.md#72-les-scripts-dans-lordre-où-les-écrire) | Aucun monstre vanilla en 3 nuits de jeu ; End et Nether inaccessibles ; Super Glue fabricable ; F3 sans coordonnées |
| **2. Monde définitif** | `test/streams-nml`, puis `test/structures-create` | Streams Reflowing sur NML ; Lootr, Create Aeronautics Structures, New Beginnings, Let The Adventure Begin (trié), MCG's Campsites, Cinch's Abandoned Houses ; villages en ruines ; réglages Sparse Structures | Décision 0001 close ; 1 000 blocs de rayon explorés sans structure bruyante ni mob vanilla ; graine choisie |
| **2 bis. Lointain** | `test/distant-horizons` | Distant Horizons + Separate Sable Render Distance, réglages de brume | Silhouettes dans la brume sans perte de performance rédhibitoire chez le joueur le moins équipé |
| **3. Traversée** | `test/traversee` | Paraglider, ParCool bridé, Traveler's Backpack, Comforts, Traveler's Titles, Exposure, Better Archeology, GUI Compass, AA4 Atlas | Une expédition de deux jours de jeu sans coordonnées, retour à la base avec carte et photos |
| **4. Civilisation** | `test/civilisation` | Copycats+, Create Connected (élagué), Factory Logistics, Ore Excavation, Prospecting Picks, Bells & Whistles, Diesel Generators, Power Loader, Rechiseled (+ Create, Fusion) ; scripts 7 à 9 et 12 | Les âges 0 à 7 se jouent dans l'ordre, sans raccourci, en solo |
| **5. Vie quotidienne** | `test/vie-quotidienne` | Kaleidoscope Compat, Cattlestrophic!, Salt's Animal Farm, Animal Genders, Freeze It and Heat It, Kaleidoscope Tavern, Thirst Canteen, Tide, Nutritional Balance, Hardcore Revival ; pont saisons, cuisine unifiée, Garde-manger | Une année Ecliptic complète jouée : hiver ressenti, troupeau qui demande des soins, réserves nécessaires |
| **6. Faune & ambiance** | `test/faune-ambiance` | Configs de 5.3 ; Cosy Critters, Enhanced Celestials, Reactive Music (songpack choisi), Particle Rain ; sons et luminances | Chaque biome a sa faune sans doublon ; aucune horde ; la nuit est noire |
| **7. Colonies** | `test/colonies` | Easy NPC, Create Villager Labor, OPAC ; script 10 ; survivants écrits | Un survivant ramené, installé, au travail ; aucun échange de Mending ni d'outil en métal |
| **8. Serveur & confort** | `test/serveur` | Jade, AppleSkin, Mouse Tweaks, Controlling + Searchables, GraveStone, Simple Voice Chat, Crash Assistant, Default Options, Simple Backups ; `server.properties`, sauvegardes, `scripts/deploy-serverconfig` ; carnet du colon | Restauration d'une sauvegarde testée ; tous les joueurs se connectent avec la voix |
| **Lancement** | `main` | Prégénération Chunky, CHANGELOG, tag `v1.0.0` | Monde permanent ouvert |

Les phases 3, 4 et 6 sont indépendantes une fois la phase 2 close et peuvent avancer en parallèle. La phase 1 doit précéder toutes les autres : sans elle, chaque test se fait dans un monde qui n'est pas celui de Fallow.

## 8.2 Pour chaque mod ajouté

1. `packwiz mr add <slug>` (ou le script de sync depuis l'instance) : il échoue tout de suite si la 1.21.1 NeoForge n'existe pas.
2. Fixer le champ `side` (client seulement pour Crash Assistant, Mouse Tweaks, Controlling, Searchables, Default Options, Cosy Critters, Traveler's Titles, ATMOSPHERICS…).
3. `npm run check-deps` pour les dépendances non déclarées.
4. Appliquer les réglages du chapitre concerné (tableaux « Réglages à appliquer ») ; noter dans la fiche de décision les clés qui ont dû être corrigées.
5. Mettre à jour `docs/mods.md`.

## 8.3 Ce qu'il faut lire dans les .jar

La séance d'étude des .jar commence par ces points, classés par ce qu'ils débloquent.

**Bloquant pour la phase 1**
1. KubeJS 2101.7 : `NativeEvents`, annulation de `EntityEvents.spawned`, `RecipeViewerEvents`, prise en compte de `kubejs/data` pour le worldgen.
2. Recettes de tous les .jar qui utilisent `slime_ball`, `blaze_rod`, `blaze_powder`, `gunpowder`, `ender_pearl`, `phantom_membrane`, `string`, `bone`, `quartz`, et tout objet du Nether.
3. In Control! 10.x : sémantique de `mincount` (avec `perplayer`) et de `maxdist`.
4. Entity Culling : clés `tickCulling`, `skipBlockEntityCulling`.

**Bloquant pour la phase 2**
5. Sparse Structures 1.21.1 : format des surcharges par structure.
6. Create Aeronautics Structures : nature des « robber balloons », tables de loot, échanges de l'ingénieur.
7. Create: New Beginnings : pools ciblés (y compris les variantes « zombie » ?).
8. Let The Adventure Begin : inventaire des structures pour le tri.
9. No Man's Land : modifie-t-il les villages ? Noms des template pools à surcharger.
10. Streams Reflowing + NML : génération combinée.

**Bloquant pour les phases 3 à 7**
11. Overgeared : composant de qualité, types de recettes, config des outils vanilla, présence d'étain et de bronze, butin du modèle d'acier, moules et alliages du Nether.
12. Paraglider : build 1.21.1 sur Modrinth ; ParCool : clés de limitation et option d'endurance Paraglider.
13. Traveler's Backpack : capacités, sac de couchage, pureté de l'eau dans les réservoirs.
14. Exposure : données des photos, coordonnées dans l'infobulle.
15. Create Connected, Ore Excavation, Power Loader, OPAC : clés de config citées aux chapitres 3 et 6.
16. Diesel Generators : origine du pétrole.
17. Create Aeronautics : identifiants des hélices, brûleurs et ballons ; carburant accepté ; matériau flottant.
18. Naturalist 2.0.4 (47 animaux), Alex's Mobs Continued 2.2.2 (format de config de spawn), Mowzie's 1.8.2, Hybrid Aquatic : listes d'espèces et clés.
19. Almost Unified : tags `c:` partagés entre Farm & Charm et Kaleidoscope Cookery, et entre Overgeared et Create.
20. ~~Présence réelle sur Modrinth~~ : vérifiée le 2026-10-10, voir [9](09-disponibilite-modrinth.md). Reste à voir dans le .jar : les recettes de Create Villager Labor, les pistes de Reactive Music, et que KubeJS Create 2101.3.1 (beta, novembre 2025) tourne avec Create 6.0.10.

Les points propres à la vie quotidienne sont listés en [4.8](04-vie-quotidienne.md#48-à-vérifier-dans-les-jar).

## 8.4 Décisions à valider ensemble

Pour chacune, le plan applique le défaut indiqué ; il suffit de dire si on le garde.

| # | Décision | Défaut retenu par le plan | Alternative |
|---|---|---|---|
| 1 | **Le Nether** | Fermé comme l'End, avec recettes de remplacement ([3.8](03-civilisation.md#38-le-nether--fermé-par-défaut)) | Ouvert comme expédition tardive, avec ses monstres vanilla gardés localement |
| 2 | Rivières | NML + Streams Reflowing, si le test passe ([2.6](02-exploration.md#26-rivières--streams-reflowing-sur-no-mans-land)) | Garder la comparaison avec Terralith |
| 3 | Entity Culling | Réglé, retiré s'il casse toujours les engins | — |
| 4 | Électricité | Rester mécanique et diesel (option A) | Crafts & Additions en fin de partie |
| 5 | Plans d'ingénieur | Système adopté, Quark Automatic Recipe Unlock coupé, `doLimitedCrafting` | Recettes libres, progression par les seuls verrous de pièces forgées |
| 6 | Villes anciennes | Gardées vides, sans Warden | Retirées |
| 7 | Photographie | Exposure (un métier) | Camerapture (plus simple) |
| 8 | Colonies | Easy NPC + Create Villager Labor, sans MineColonies | Survivants Easy NPC seuls, sans travail automatisé |
| 9 | Voix | Simple Voice Chat, groupes désactivés | Groupes autorisés pour les expéditions |
| 10 | Umvuthana de Mowzie's (tribus masquées hostiles) | Désactivés | Gardés très rares |
| 11 | Towers of the Wild Modded | Non retenu (proche de Structory: Towers) | Tours en ruine très rares, planeur dans le coffre |
| 12 | Régénération naturelle | Gardée ; Hardcore Revival, faim et soif suffisent | Coupée |
| 13 | Élevage | Trois petits mods (Cattlestrophic, Salt's, Animal Genders) | Élevage vanilla + bâtiments F&C seulement |
| 14 | Conservation | Freeze It and Heat It + Garde-manger limité aux rations d'expédition | Garde-manger étendu à toute la nourriture |
| 15 | Pêche | Tide | River Fishing (plus exigeant, ajoute un PNJ pêcheur) |
| 16 | Vin et cave | Kaleidoscope Tavern à la place de Vinery | Rien pour l'instant |

