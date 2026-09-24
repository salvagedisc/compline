# 0004 — Structures & villageois

- **Date** : 2026-09-24
- **Statut** : en cours sur `test/structures`

## Principe
Une structure est une découverte (« attends… c'est quoi ça ? »), pas un décor répétitif. Il faut malgré tout assez de diversité pour avoir l'impression de toujours découvrir quelque chose.

## Décisions
- **End supprimé** entièrement (dimension, portails, Elytra), **strongholds supprimés** par conséquence. Mise en œuvre : datapack, sous-projet 3.
- **Sparse Structures ✅** : espacement global de toutes les structures (vanilla et mods), réglable par structure.
- **No Man's Land** fournit déjà des structures d'ambiance (menhirs, ruines, sanctuaires, puits, monuments).
- **Écartés** : YUNG's Better *, Structory, Structory: Towers (pas au goût de l'utilisateur), Explorify, Moog's Voyager, Trek, Adorabuild (volume), When Dungeons Arise, Dungeons and Taverns (RPG), boussoles de localisation, Waystones.

## En recherche
- Un mod qui **remplace toutes les structures vanilla par des versions intégrant Create** (évoqué par l'utilisateur, non retrouvé sur Modrinth au 2026-09-24).
- Candidats Create identifiés en attendant : Create: Let The Adventure Begin, Create Aeronautics Structures, Create: Structures Arise, Create Rustic Structures.

## Villageois : concept « survivants » ✅
1. Aucun village intact : les villages vanilla deviennent des ruines abandonnées (forcer la variante « village zombie », vide sans zombies, ou retirer les villageois).
2. Survivants rares et isolés (ermite, ingénieur, campement de 2-3 réfugiés) à découvrir.
3. Le joueur les ramène dans sa colonie (laisse, bateau, train, dirigeable) : ils deviennent la population.
4. Chaque villageois est précieux : reproduction bloquée ou très lente, échanges filtrés en KubeJS (pas de Mending, pas de contournement d'Overgeared).

Mise en œuvre : structures de survivants en datapack + règles KubeJS (sous-projet 3).

## Conséquence de la suppression des hostiles vanilla
Monuments océaniques, cabanes de sorcière et avant-postes deviennent vides : à supprimer, repeupler avec nos créatures, ou garder quelques mobs vanilla localement (sous-projet 3).
