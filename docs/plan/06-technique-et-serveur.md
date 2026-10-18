# 6. Socle technique, confort et serveur

Ce qui existe déjà (0007) : Sodium 0.8.13, ImmediatelyFast, Entity Culling, ModernFix, FerriteCore, Lithium, Clumps, Structure Layout Optimizer, Chunky, spark ; Sinytra Connector + Forgified Fabric API pour Antique Atlas et Surveyor. Ce chapitre ajoute la **stabilité d'un monde permanent**, le **confort compatible avec la vision**, l'**outillage KubeJS** et la **configuration du serveur**.

Détail, preuves et clés : [recherche/research-technique.md](recherche/research-technique.md).

## 6.1 Alertes

1. **Entity Culling casse les engins Aeronautics/Sable.** Trois tickets ouverts sans réponse ([Simulated-Project#1347](https://github.com/Creators-of-Aeronautics/Simulated-Project/issues/1347), [#974](https://github.com/Creators-of-Aeronautics/Simulated-Project/issues/974), [sable#1414](https://github.com/ryanhcode/sable/issues/1414)) : les blocs à block entity d'un dirigeable assemblé deviennent inutilisables, les voiles d'hélice tremblent. À tester d'abord : `tickCulling: false` et `skipBlockEntityCulling: true` dans `config/entityculling.json`. Si le bug persiste, **retirer Entity Culling** : il ne sert qu'au client, le retrait est sans risque.
2. **`allow-flight=true` est indispensable** dans `server.properties`. Sans lui, un joueur debout sur un dirigeable ou une contraption en mouvement est expulsé.
3. **Aeronautics doit rester en 1.3.2 ou plus** : avant, JEI récent le fait planter ([#1405](https://github.com/Creators-of-Aeronautics/Simulated-Project/issues/1405)).
4. **Liste noire** (incompatibles avec Aeronautics) : Does It Tick, Flowing Fluids, Leaves Be Gone, Flashback, Tpsum, Refurbished Furniture (énergie), Carry On.
5. **Sodium 0.8.x refuse Sodium Options API** : pas de Sodium Extra, Reese's Sodium Options ni de mod qui en dépend.
6. **Sinytra Connector** : figer Connector et FFAPI ensemble, mettre à jour par paire, et n'ajouter **aucun mod Fabric hors de la famille Surveyor** (AA4 Atlas et Antique Trains sont publiés pour NeoForge par leurs auteurs et passent par Connector comme Antique Atlas). Les tickets Connector sur Antique Atlas et Surveyor sont tous fermés. Après chaque mise à jour, tester l'atlas, `/surveyor share` et les tombes sur la carte. Dead Reckoning, Fabric seulement, est remplacé par GUI Compass, natif ([2](02-exploration.md)).

## 6.2 Mods proposés

| Mod | Rôle | Côté | Dispo | Statut proposé |
|---|---|---|---|---|
| **Neruina** | Met en pause une entité qui plante à chaque tick au lieu de faire tomber le serveur | CS | oui | socle |
| **Simple Backups** | Sauvegardes planifiées, complètes et différentielles | S | oui | socle (repli : script hôte seul) |
| **JEI** | Visionneuse de recettes ; plugin natif dans Create, Aeronautics, Overgeared, KC, F&C ; testé par l'équipe Aeronautics | CS | oui | socle |
| **In Control!** | Règles de spawn ([5.2](05-faune-et-ambiance.md#52-contrôler-les-créatures-moddées)) | S | oui (10.2.6) | socle |
| **LootJS** | Retirer élytres et yeux de l'Ender de tous les butins ; loot des ruines | CS | oui | socle |
| **KubeJS Create** | Recettes Create en KubeJS : le lien Overgeared → Create → KC | CS | oui | socle |
| **MoreJS** | Événements d'échanges des villageois (règles des survivants) | CS | 1.21.1 non confirmée | recommandé ; repli `NativeEvents` |
| **Almost Unified** | Unifie les doublons par tags `c:` : ingrédients F&C / KC (priorité KC), métaux Overgeared / Create | CS | oui | recommandé |
| **Jade** | Nom du bloc, outil requis, état des machines | CS | oui | recommandé (plugin coordonnées et vie des mobs coupés) |
| **AppleSkin** | Faim et saturation en aperçu : comparer les plats de KC | CS | oui | recommandé |
| **Mouse Tweaks** | Glisser-déposer à la souris, complète Quark | C | oui | recommandé |
| **Controlling** + **Searchables** | Recherche et conflits de touches | C | oui | recommandé |
| **GraveStone Mod** | Tombe au lieu de la mort ; Antique Atlas marque déjà les tombes sur la carte | CS | oui | recommandé (nécrologie et fantôme coupés) |
| **Simple Voice Chat** | Voix de proximité (port UDP 24454) | CS | oui | recommandé |
| **Crash Assistant** | Fenêtre d'aide après un crash, téléversement des logs, alerte carte graphique intégrée | C | oui | recommandé |
| **Default Options** | Livre des réglages et touches par défaut sans écraser ceux des joueurs | C | oui | recommandé |
| **ProbeJS** | Autocomplétion KubeJS dans VSCode | dev | — | dans l'instance de test seulement, pas dans `pack/` |

**Optionnels** : EMI (avec JEI), Polymorph (seulement si un conflit de recettes résiste à KubeJS), Packet Fixer (au premier « Payload too large »), First-person Model, Not Enough Animations, Shoulder Surfing Reloaded, FancyMenu + Drippy Loading Screen (habillage, plus tard), Better Advancements, Sound Physics Aeronautics, un addon « radio » pour Simple Voice Chat (à trouver : la communication lointaine comme objet à fabriquer).

**Écartés** :
- FTB Backups 3, FTB Quests : CurseForge seulement.
- FTB Chunks : garde une carte plein écran.
- Corail Tombstone : magie et téléportation ; YIGD : orienté Fabric.
- ServerCore : son *activation range* risque de casser Create.
- Open Loader, Global Packs : KubeJS charge déjà `kubejs/data` et `kubejs/assets`.
- Catalogue, Inventory Profiles Next (doublon du tri Quark), Emotecraft (doublon des emotes Quark), Better Third Person et Hold My Items (non vérifiés), Carry On (incompatible avec Aeronautics), Sodium Extra (alerte 5), Embeddium (Sable exige Sodium).

## 6.3 Confort sans casser la vision

| Réglage | Raison |
|---|---|
| Jade : plugin de coordonnées verrouillé à off, vie des mobs coupée, `displayMode` LITE ou maintien d'une touche | Pas de coordonnées, peu de combat |
| GraveStone : nécrologie (elle affiche les coordonnées) et fantôme coupés ; tombe cassable par son propriétaire | Pas de coordonnées |
| Simple Voice Chat : `max_voice_distance` 32 à 40, chuchotement 12 à 16, **groupes désactivés** | On s'entend de près ; la communication lointaine passera par un objet |
| Default Options : distance de rendu, « Improved Transparency » de Sodium désactivée (pour ATMOSPHERICS), touches dédoublonnées | Réglages cohérents dès la première ouverture |
| Almost Unified : priorité Kaleidoscope Cookery sur Farm & Charm pour tomate, laitue, farine, pâte | Condition de la décision 0002 |

## 6.4 Distant Horizons

DH reste sur sa branche, mais la recherche lève la principale inquiétude : **DH et la brume vont bien ensemble**. Avec un brouillard DH exponentiel dense et un rayon modéré, les LOD deviennent des **silhouettes délavées** au loin, exactement la direction artistique.

Réglages sur `test/distant-horizons` :
1. **Désactiver « Disable Vanilla Fog »** (activé par défaut) : sinon la brume d'ATMOSPHERICS disparaît.
2. Brouillard DH exponentiel, qui démarre tôt.
3. Rayon des LOD de 64 à 128 chunks : au-delà, la brume cache tout et le coût explose.
4. **Separate Sable Render Distance** devient obligatoire : sans lui, les dirigeables disparaissent au-delà de la distance vanilla (gamerule `ssrdMaxRenderDistance`).
5. Mémoire : 10 Go serveur, 8 Go client.

## 6.5 Configuration du serveur

**`server.properties`**

```properties
allow-flight=true            # indispensable pour Aeronautics
view-distance=10             # la brume masque le lointain ; 12 au maximum
simulation-distance=6        # 8 si spark le permet
white-list=true
enforce-whitelist=true
spawn-protection=0
enable-command-block=false
max-tick-time=120000         # tolère les pics de génération ; Neruina gère les entités
sync-chunk-writes=true       # sécurité d'un monde permanent
difficulty=normal
```

**Gamerules** (fonction `fallow:init` exécutée au chargement, voir [7](07-kubejs-et-datapack.md)) :
`reducedDebugInfo true`, `doInsomnia false`, `doPatrolSpawning false`, `disableRaids true`, `doWardenSpawning false`, `doTraderSpawning false`, `playersSleepingPercentage 50`, `keepInventory false`, `spectatorsGenerateChunks false`, `announceAdvancements true`, `logAdminCommands true`. `mobGriefing` reste à `true` (moutons, villageois). `naturalRegeneration` est à arbitrer avec la survie ([4](04-vie-quotidienne.md)).

**Mémoire** : Java 21, 8 Go pour le serveur (10 Go avec DH), 6 à 8 Go par client. G1 avec les flags d'Aikar, ou ZGC générationnel sur une machine d'au moins 4 cœurs.

**Prégénération (Chunky)** : seulement après le réglage final de la graine et du worldgen (NML, Streams, Sparse Structures, tag stronghold). Cercle de 4 000 blocs de rayon, `continueOnRestart` activé, puis paliers de 6 000 et 8 000 quand la frontière avance. Bordure de monde à 50 000 (rayon 25 000) pour borner le disque.

**Sauvegardes, trois niveaux** :
1. **Dans le jeu** (Simple Backups) : différentielle toutes les heures si des joueurs sont connectés, complète chaque jour, 48 conservées, sur un disque distinct du monde.
2. **Hôte** (restic ou borg) : chaque nuit `save-off`, `save-all flush`, snapshot de `world/`, `config/`, `defaultconfigs/`, `kubejs/`, puis `save-on`. Rétention 7 quotidiennes, 4 hebdomadaires, 6 mensuelles, envoyées hors machine.
3. **Avant chaque release du pack** : sauvegarde complète étiquetée avec la version du pack. Test de restauration une fois par mois.

**Redémarrage** : unité systemd (`Restart=on-failure`) avec redémarrage quotidien et avertissements RCON à 10 min et 1 min. Aucun mod nécessaire.

**Installation** : `packwiz-installer-bootstrap -g -s server` pointé sur le `pack.toml` d'une **release**, pas sur `main`. Les mods `side = "client"` sont ignorés côté serveur : vérifier le champ `side` de chaque nouveau mod client (Crash Assistant, Mouse Tweaks, Controlling, Searchables, Default Options, Cosy Critters…).

**Configs serveur d'un monde permanent** : `defaultconfigs/` n'est copié dans le monde **qu'à sa création**. Pour un monde qui existe déjà, il faut un petit script (`scripts/deploy-serverconfig`) qui copie `pack/defaultconfigs` vers `world/serverconfig` avant chaque démarrage. C'est un ajout à l'outillage du dépôt.

## 6.6 Organisation des configs

- `pack/config/` : configs client et communes, imposées à tous, écrasées à chaque mise à jour du pack.
- `pack/defaultconfigs/` : configs serveur par monde (Create, GraveStone, OPAC, Power Loader…).
- `options.txt` et touches : via Default Options, jamais versionnés directement.
