# Recherche : socle technique, serveur, QoL et outillage KubeJS (Fallow, 1.21.1 NeoForge)

> Périmètre recentré après la mise à jour du coordinateur. Je ne repropose pas ce qui est déjà retenu (Sodium 0.8.13, ImmediatelyFast, Entity Culling, ModernFix, FerriteCore, Lithium, Clumps, Structure Layout Optimizer, Chunky, spark, Connector + FFAPI) ni ce qui est écarté (C2ME, Noisium, MoreCulling, Ksyxis). Je signale en revanche **deux risques sur des mods déjà retenus** (Entity Culling, Distant Horizons).
>
> **Limites de la recherche.** Modrinth et CurseForge sont inaccessibles en lecture directe. Ma vérification s'est faite par WebSearch (extraits Modrinth/CF), par les dépôts GitHub et par les traqueurs de bugs d'Aeronautics, de Sable et de Connector. **Le quota WebSearch commun à la session a été épuisé en cours de recherche.** Pour la seconde moitié des mods, la disponibilité indiquée vient donc des liens Modrinth trouvés sur leurs dépôts GitHub et de mes connaissances, et elle porte la mention « probable » ou « non vérifié ». C'est à confirmer au moment du `packwiz mr add`, qui échoue tout de suite si la version 1.21.1 NeoForge n'existe pas.

---

## 0. Alertes prioritaires

| # | Alerte | Gravité | Action |
|---|---|---|---|
| A1 | **Entity Culling casse les engins Aeronautics/Sable.** Issues ouvertes [Simulated-Project#1347](https://github.com/Creators-of-Aeronautics/Simulated-Project/issues/1347) et [sable#1414](https://github.com/ryanhcode/sable/issues/1414) : les blocs à block entity (arbres, levier analogique, éolienne) d'un dirigeable assemblé deviennent inutilisables. [#974](https://github.com/Creators-of-Aeronautics/Simulated-Project/issues/974) : les voiles d'hélice tremblent, ce qui fait penser au *tick culling*. Aucune réponse des mainteneurs. | **Haute** | Tester d'abord `tickCulling: false` et `skipBlockEntityCulling: true` dans `config/entityculling.json` (clés à confirmer dans le .jar). Si le bug persiste, **retirer Entity Culling**. Il ne sert qu'au client et se retire sans risque. Hypothèse : les sous-niveaux Sable vivent dans une zone « plot » éloignée, si bien que le raycast d'occlusion les juge invisibles. |
| A2 | **`allow-flight=true` est obligatoire** dans `server.properties`. Sans ce réglage, un joueur debout sur un dirigeable ou une contraption en mouvement est expulsé (« Flying is not enabled »). | Haute | À mettre dans la configuration du serveur. |
| A3 | **JEI ≥ 19.44.0.406 fait planter Aeronautics < 1.3.2** ([#1405](https://github.com/Creators-of-Aeronautics/Simulated-Project/issues/1405), corrigé en 1.3.2). Le pack a la 1.3.2. | Moyenne | Ne jamais rétrograder Aeronautics sous 1.3.2 si JEI est récent. |
| A4 | **Incompatibilités Aeronautics connues** à transmettre aux autres chercheurs : Does It Tick (#974), Flowing Fluids (#846), Leaves Be Gone (#866), Flashback (#867), Tpsum (#1406), Refurbished Furniture (énergie, #908), Carry On (impossible de prendre un bloc posé sur un vaisseau, #848). | Moyenne | Liste noire du pack. |
| A5 | **Distant Horizons désactive le brouillard vanilla par défaut** (option « Disable Vanilla Fog » de la section Fog ; clé exacte à confirmer). Avec ATMOSPHERICS, la brume disparaîtrait. | Moyenne (branche DH) | Mettre l'option à `false` et régler le brouillard lointain propre à DH (voir §3). |
| A6 | **Sodium 0.8.x n'est compatible avec aucune version de « Sodium Options API »** (page de version [mc1.21.1-0.8.12](https://modrinth.com/mod/sodium/version/mc1.21.1-0.8.12-beta.1-neoforge)). | Faible | Écarter Sodium Extra, Reese's Sodium Options et tout mod qui en dépend. |

---

## 1. Fiches mods candidats (38)

Légende : **Dispo** = version 1.21.1 NeoForge sur Modrinth. **S** = socle · **R** = recommandé · **O** = optionnel · **X** = écarté.

### 1.1 Serveur, stabilité, sauvegardes

**1. Neruina — Ticking Entity Fixer** · `neruina` · [GitHub](https://github.com/Bawnorton/Neruina) · [Modrinth](https://modrinth.com/mod/neruina)
- Dispo : probable (le dépôt contient une build NeoForge et renvoie vers Modrinth ; version 1.21.1 non vérifiée directement). Côté serveur, à installer des deux côtés (messages). Aucune dépendance.
- Rôle : quand une entité, une block entity ou un objet lève une exception à chaque tick, Neruina le met en pause au lieu de faire planter le serveur. Indispensable pour un monde permanent avec Create, Sable et 70 mods. Les entités suivies le restent après un redémarrage.
- Config `config/neruina.json` : `auto_kill_ticking_entities=false` (on garde la pause pour pouvoir enquêter), `ticking_exception_threshold=10` (crash volontaire si les erreurs s'emballent), `min_permission_level_for_commands=2`. Option « Report » : rapport automatique sur GitHub, à désactiver si on ne veut rien envoyer.
- **S**.

**2. Simple Backups** · `simple-backups` · ChaosTheDude
- Dispo : non vérifié (quota épuisé). D'après mes connaissances, il est sur Modrinth avec des builds NeoForge 1.21.x. Côté serveur.
- Rôle : sauvegardes ZIP planifiées, en mode complet ou « modulaire » (différentiel depuis la dernière complète).
- Config (clés à confirmer) : `backupType` (FULL / MODULAR), `timer` en minutes, `fullBackupTimer`, `backupsToKeep`, `compressionLevel`, `outputPath`, `sendMessages`, `maxDiskSize`, sauvegarde seulement si des joueurs sont connectés.
- **S**, comme premier niveau de sauvegarde. Le niveau hors machine est décrit au §6.

**3. FTB Backups 2 / 3** · [FTB-Backups-2](https://github.com/FTBTeam/FTB-Backups-2) · [FTB-Backups-3](https://github.com/FTBTeam/FTB-Backups-3)
- FTB Backups 2 est en LTS jusqu'à 1.21.5. La version 3 (1.21.1+) n'est distribuée que sur CurseForge d'après son README.
- **X** : ne respecte pas la contrainte Modrinth. Textile Backup ne fonctionne que sous Fabric (**X**).

**4. Crash Assistant** · `crash-assistant` · [GitHub](https://github.com/KostromDan/Crash-Assistant) · [Modrinth](https://modrinth.com/mod/crash-assistant)
- Dispo : probable (le dépôt couvre Forge/NeoForge/Fabric, branches « 1.20.6-1.21.4 »). Côté client.
- Rôle : après un crash, une fenêtre affiche les logs, permet de les téléverser en un clic et propose une analyse et des correctifs automatiques. Elle prévient aussi si le jeu tourne sur la carte graphique intégrée, une cause fréquente de lag chez les amis. Le texte d'aide est configurable (lien Discord, consignes).
- **R**.

**5. Packet Fixer** · `packet-fixer`
- Dispo : non vérifié. Côté client et serveur.
- Rôle : relève les limites de taille de paquet et de NBT. Les erreurs « Payload may not be larger than… » ou les expulsions arrivent souvent avec les schémas Create, les grands assemblages Sable et les gros inventaires.
- **O** : à ajouter dès le premier symptôme. Gain nul tant que le problème n'apparaît pas.

**6. ServerCore** (NeoForge 1.5.19+1.21.1 d'après un miroir Modrinth)
- Rôle : distance de vue dynamique, *activation range* des entités, optimisations des ticks.
- **X** pour l'instant : l'*activation range* et le ralentissement des ticks risquent de casser les contraptions, les trains et les animaux Create. Lithium couvre déjà l'essentiel. À ne reconsidérer que si spark montre une saturation serveur, et alors avec l'*activation range* coupée.

**7. Open Loader / Global Packs**
- **X** : inutile. KubeJS charge déjà `kubejs/data/**` comme datapack et `kubejs/assets/**` comme resource pack. Le seul cas qui le justifierait est un datapack worldgen pas pris en compte à la création du monde : à vérifier au premier lancement (§7).

### 1.2 Chunkloading et revendications

**8. Create: Power Loader** · `create-power-loader` · [GitHub](https://github.com/hlysine/create_power_loader) (déjà « à tester »)
- Dispo : le README annonce Create 6 sur 1.21.1, avec un lien Modrinth. Le portage NeoForge 1.21.1 y est crédité ; à confirmer. Côté client et serveur.
- Rôle : chargeurs de chunks qui tournent à l'énergie rotative. Version andésite : 1 chunk. Version laiton : de 1×1 à 5×5. Ils fonctionnent au sol, sur les trains et sur les contraptions, peuvent s'attacher aux gares, se commutent par contraption controls, et leurs recettes peuvent être remplacées par datapack.
- Intégration : **sert l'« entretien » au lieu de le casser**. Si la rotation s'arrête, le chunk se décharge. Il faut donc une vraie source d'énergie : moulin, machine à vapeur, éolienne.
- À configurer : nombre maximal par joueur, rayon du chargeur en laiton, coût en contrainte. À tester : un chargeur posé sur un **sous-niveau Sable** (dirigeable) suit-il le vaisseau ? Les sous-niveaux ne sont pas des contraptions Create, la réponse n'est donc pas garantie.
- **R** : c'est le seul mécanisme de chargement de chunks du pack.

**9. Open Parties and Claims (OPAC)** · `open-parties-and-claims` · [GitHub](https://github.com/thexaero/open-parties-and-claims)
- Dispo : probable (il couvre NeoForge et renvoie vers Modrinth). Côté client et serveur. **N'affiche aucune carte** : son interface passe par les cartes Xaero, absentes ici. Gestion par commandes et écran.
- Rôle : groupes, protection des bases, chargement de chunks.
- Config `serverconfig/openpartiesandclaims-server.toml` : limiter le nombre de claims par joueur et **mettre à 0 les chunks chargés par claim**, pour que Power Loader reste le seul moyen de garder des chunks chargés (clés à lire dans le .jar).
- **O** : utile seulement si le groupe veut se protéger des accidents. Entre amis, on peut s'en passer.

**10. FTB Chunks** — **X** : il fournit une grande carte et une minimap avec waypoints. On peut désactiver la minimap, mais la carte plein écran reste, ce qui contredit la vision. Il dépend aussi de FTB Library, distribuée surtout sur CurseForge.

### 1.3 Visionneuse de recettes, infobulles, inventaire

**11. JEI** · `jei`
- Dispo : oui (versions 19.x NeoForge 1.21.1, citées dans [#1405](https://github.com/Creators-of-Aeronautics/Simulated-Project/issues/1405)). Côté client et serveur.
- Rôle : Create 6, Aeronautics (mixin `silence_jei`), Overgeared, Kaleidoscope Cookery et Farm & Charm fournissent tous un plugin JEI natif. KubeJS 2101 intègre `jei`, `emi` et `rei` (dossier `integration/` de la branche [2101](https://github.com/KubeJS-Mods/KubeJS/tree/2101/src/main/java/dev/latvian/mods/kubejs/integration)), ce qui permet de masquer les entrées et d'ajouter des infos.
- **S**. C'est la solution qui fait le moins de pièces mobiles, et la combinaison testée par l'équipe Aeronautics.

**12. EMI** · `emi` · [GitHub](https://github.com/emilyploszaj/emi)
- Dispo : probable (il couvre NeoForge). Côté client et serveur.
- Rôle : arbre de recettes, favoris et craft par lot, très utile pour les longues chaînes Overgeared → Create. Les plugins JEI sont lus via JEMI si JEI est présent.
- **O** : à essayer sur une branche, avec JEI installé en même temps (JEMI). Risque : le mixin JEI d'Aeronautics. KubeJS le prend en charge.

**13. Jade** · `jade`
- Dispo : oui d'après mes connaissances (15.x NeoForge 1.21.1) ; le README pointe vers CurseForge et la documentation. Côté client, avec une partie serveur pour les données.
- Rôle : affiche le nom du bloc, l'outil requis et l'état des machines. Il complète les lunettes Create.
- Réglages : dans `config/jade/plugins.json`, **vérifier qu'il n'y a pas de plugin de coordonnées** (Jade en a un, désactivé par défaut, à verrouiller) et couper la vie des mobs (`entity_health`), qui va contre l'idée de « peu de combat ». `jade.json` → `displayMode` sur LITE ou HOLD_KEY pour ne pas encombrer l'écran. Côté serveur, regarder si `server-plugin-overrides.json` existe pour imposer ces réglages (à confirmer).
- **R**.

**14. AppleSkin** · `appleskin` — Dispo : probable. Côté client et serveur.
- Rôle : prévisualisation de la faim et de la saturation, utile pour comparer les plats de Kaleidoscope Cookery. Thirst Was Reclaimed a sa propre barre.
- **R**.

**15. Mouse Tweaks** · `mouse-tweaks` — Dispo : probable. Côté client.
- Rôle : glisser-déposer et molette. Complète le tri et l'*Easy Transferring* de Quark, déjà actifs.
- **R** : léger, aucun conflit connu.

**16. Controlling + Searchables** · `controlling`, `searchables` — Dispo : probable. Côté client.
- Rôle : recherche et repérage des conflits de touches. Quark, Create, Aeronautics, Antique Atlas, la voix et les compagnons créeront forcément des conflits.
- **R**.

**17. Configured** (+ **Catalogue**) · `configured`, `catalogue` — Dispo : probable. Côté client et serveur.
- NeoForge 21.1 sait déjà afficher les configs client et commune. Configured ajoute l'**édition en jeu des configs serveur et de monde** par un opérateur, pratique pour régler Cold Sweat ou Ecliptic sans relancer.
- Configured **O** (utile à l'admin). Catalogue **X** : la liste de mods de NeoForge suffit.

**18. Polymorph** · `polymorph` — Dispo : non vérifié.
- Rôle : choisir entre deux recettes en conflit.
- **O** : à n'ajouter que si les conflits Overgeared / Farm & Charm / Kaleidoscope Cookery ne se règlent pas tous en KubeJS. Mieux vaut supprimer le conflit que l'arbitrer.

**19. Inventory Profiles Next / Inventory Essentials** — **X** : Quark fait déjà le tri et l'*Easy Transferring*. Inventory Profiles Next tire aussi Kotlin et libIPN.

### 1.4 Tombe et mort

**20. GraveStone Mod** · `gravestone-mod` · [GitHub](https://github.com/henkelmax/gravestone)
- Dispo : probable (henkelmax publie sur Modrinth ; NeoForge 1.21.1 non confirmé directement). Côté client et serveur.
- Rôle : un bloc tombe à l'endroit de la mort. Option de récupération en s'accroupissant, objets remis dans leurs emplacements, commande `/restore` pour l'admin.
- **À désactiver : la nécrologie** (« Obituary »), qui **affiche les coordonnées** de la mort, et l'**apparition de fantôme**. À activer : tombe cassable par son seul propriétaire.
- Synergie : Antique Atlas 4 marque déjà les tombes des joueurs sur la carte, avec un style configurable (README). Le joueur retrouve sa mort sur l'atlas, sans coordonnées.
- **R**.

**21. Corpse** (henkelmax) · `corpse` — Le corps devient une entité qui porte l'inventaire. C'est immersif, mais une entité posée sur un sous-niveau Sable en mouvement présente un risque. Sa fiche donne aussi les coordonnées.
- **O**, en remplacement de GraveStone : un seul des deux.

**22. Corail Tombstone** — **X** : distribué uniquement sur CurseForge à ma connaissance (non vérifié). Il ajoute en plus des sorts, de la magie et des téléportations, contraires à la vision. **YIGD** — **X** : orienté Fabric.

### 1.5 Communication et immersion sociale

**23. Simple Voice Chat** · `simple-voice-chat` · [GitHub](https://github.com/henkelmax/simple-voice-chat)
- Dispo : probable (téléchargements hébergés sur Modrinth). Côté client et serveur, sur le **port UDP 24454** à ouvrir.
- Synergie : Sound Physics Remastered, déjà dans le pack, applique la réverbération et l'occlusion aux voix. La vallée et la grotte s'entendent aussi dans la voix.
- Config `config/voicechat/voicechat-server.properties` (clés à confirmer) : `max_voice_distance` vers 32-40 (on s'entend de près seulement, ce qui renforce l'isolement), `whisper_distance` 12-16, `enable_groups`.
- Décision à prendre : les groupes permettent de parler à n'importe quelle distance. Pour la vision, je propose `enable_groups=false`, la communication lointaine passant par un objet (point 24).
- **R**.

**24. Addon « radio » pour Simple Voice Chat** (talkie-walkie, radios) — nom et disponibilité **non vérifiés**, à rechercher.
- Idée peu connue : la communication à distance devient un objet diégétique à fabriquer et à alimenter, ce qui crée un lien entre colonies.
- **O / à rechercher**.

**25. Emotecraft** — **X** : doublon avec les Emotes de Quark, déjà actives.

### 1.6 Animations et caméra (client)

**26. First-person Model** · `first-person-model` (tr7zw) — Dispo : probable. Côté client. On voit son corps en vue subjective, ce qui donne de la présence physique.
- À tester : la caméra assise sur les sièges Aeronautics. Le cas Figura a déjà posé problème ([#1323](https://github.com/Creators-of-Aeronautics/Simulated-Project/issues/1323)).
- **O**.

**27. Not Enough Animations** · `not-enough-animations` (tr7zw) — Côté client. Animations de manger, de carte et d'échelle à la 3ᵉ personne. Elles vont bien avec l'atlas tenu en main.
- **O**.

**28. Eating Animation** — Non vérifié sur NeoForge 1.21.1. **O** (Not Enough Animations couvre une partie).

**29. Shoulder Surfing Reloaded** · `shoulder-surfing-reloaded` — Probable. Côté client. Caméra par-dessus l'épaule, agréable pour les paysages et l'exploration.
- À tester avec la caméra des engins.
- **O**. Better Third Person : non vérifié sur NeoForge 1.21.1 (**X**). Hold My Items : non vérifié (**X**).

### 1.7 Menus et habillage du pack

**30. FancyMenu (+ Drippy Loading Screen)** · `fancymenu`, `drippy-loading-screen`
- Probable. Côté client. Dépendances (Konkrete, Melody) à confirmer.
- Rôle : menu titre et écran de chargement personnalisés.
- **O** (plus tard). En attendant, la solution sans mod est au §5.4 : panorama et splashes via `kubejs/assets`.

**31. Default Options** (BlayTheNinth) · `default-options` — Non vérifié.
- Rôle : livre des `options.txt` et des touches **par défaut** sans écraser ceux des joueurs à chaque mise à jour : distance de rendu, désactivation de l'option « Improved Transparency » de Sodium (demandée par ATMOSPHERICS), touches dédoublonnées.
- Il évite de versionner `options.txt` avec packwiz.
- **R**.

### 1.8 Outillage KubeJS et guide

**32. LootJS** · `lootjs` · [GitHub](https://github.com/AlmostReliable/lootjs) (branche 1.21.1, NeoForge)
- Côté serveur, sur les deux côtés dans le pack.
- Rôle : retirer élytres et yeux de l'Ender de toutes les tables, butin des ruines cohérent avec le thème « survivants », drops des créatures.
- **S**.

**33. MoreJS** · `morejs` · [GitHub](https://github.com/AlmostReliable/morejs) (branche « 1.21 », NeoForge)
- 1.21.1 non confirmé. Il ajoute des événements, notamment sur les **échanges des villageois et du marchand ambulant**.
- Rôle : filtrer les échanges (pas de Mending, pas de contournement d'Overgeared), comme l'exige le concept « survivants » de la décision 0004.
- **R**.

**34. KubeJS Create** · `kubejs-create` · [GitHub](https://github.com/KubeJS-Mods/KubeJS-Create) (lien `mc=2101`, NeoForge)
- Compatibilité avec Create 6.0.10 à vérifier.
- Rôle : schémas `event.recipes.create.*` (mixage, pressage, assemblage séquencé) pour lier Overgeared, Create et Kaleidoscope Cookery, et verrouiller l'automatisation derrière la progression.
- **S**.

**35. PonderJS / ponders personnalisés**
- Avec Create 6, Ponder est devenu une bibliothèque séparée. Le support par KubeJS (dans KubeJS Create 2101 ou un addon) est **non vérifié**.
- **O** : des ponders pour les mécaniques propres au pack (pont saisons → Cold Sweat, chargeurs Power Loader).

**36. ProbeJS** · [GitHub](https://github.com/Prunoideae/ProbeJS) (branche 1.21)
- Autocomplétion dans VSCode : extension, `/probejs` puis dump, ouvrir l'instance dans VSCode.
- **Outil de développement uniquement, à ne pas livrer dans `pack/`.** À installer dans l'instance « Fallow test ».
- **R (dev)**.

**37. Almost Unified** · `almostunified` · [GitHub](https://github.com/AlmostReliable/almostunified) (branche 1.21.1, NeoForge/Fabric)
- Unification par tags `c:` avec priorité par mod, masquage des doublons dans la visionneuse, unification des recettes et du butin.
- Intérêt fort pour la décision 0002 : il peut unifier **les doublons d'ingrédients Farm & Charm / Kaleidoscope Cookery** (tomate, laitue, farine, pâte) si les deux mods partagent les mêmes tags `c:`, en donnant la priorité à Kaleidoscope Cookery. Il peut aussi unifier les métaux Overgeared / Create (plaques, pépites, lingots de cuivre et de laiton…).
- Point à étudier dans les .jar : les tags exposés par chaque mod.
- **R**.

**38. Guide du pack : Patchouli ou Modonomicon** (déjà présents comme dépendances)
- Coût nul : les deux sont déjà dans le pack.
- **Modonomicon** : entrées débloquées par progrès, multiblocs, et c'est le moteur du livre d'Icy's Better Horses. **Patchouli** : plus simple, livres en datapack et resource pack, donc en `kubejs/data` et `kubejs/assets`.
- Proposition : un « Carnet du colon » sous Patchouli. Ses chapitres s'ouvrent avec les progrès vanilla personnalisés : eau, première forge, premier moulin, premier rail, premier vol.
- **GuideME** ([GitHub](https://github.com/AppliedEnergistics/GuideME)) : guide en Markdown avec scènes 3D et recherche, mais pas de lien Modrinth dans le README. **O**. Akashic Tome : non vérifié, **X**.

**Quêtes.** FTB Quests renvoie seulement vers CurseForge dans son README (**X** selon la contrainte). Je recommande à la place **des progrès vanilla en datapack** (`kubejs/data/fallow/advancement/…`), affichés avec **Better Advancements** (`better-advancements`, non vérifié, **O**). Ils jouent le rôle d'un journal sans liste de tâches façon RPG, ce qui colle au « pas de fin, on perfectionne ».

### 1.9 Distant Horizons (retenu sous condition, « en attente ») : réglages pour la brume

- **DH** : la version 2.3.0-b pour 1.21.1 a apporté le serveur dédié, la génération de LOD côté serveur, une config séparée client/commun/serveur, la correction du brouillard vertical et un fondu entre DH et le rendu vanilla ([changelog 2.3.0-b](https://modrinth.com/mod/distanthorizons/version/2.3.0-b-1.21.1)). Il existe une 2.4.0-b pour 1.21.1.
- **Brume et DH sont compatibles, et même complémentaires.** DH a son propre brouillard lointain (début, fin, densité, type exponentiel, brouillard de hauteur). Avec un brouillard exponentiel dense, les LOD apparaissent comme des **silhouettes délavées** de montagnes à 500 blocs, ce qui correspond exactement à la DA « brume, distance, silhouettes ».
- Règles :
  1. Couper « Disable Vanilla Fog » pour que le brouillard proche d'ATMOSPHERICS reste.
  2. Brouillard DH exponentiel, démarrage tôt.
  3. Rayon des LOD modéré (64 à 128 chunks) : avec un brouillard dense, un rayon plus grand coûte cher sans rien montrer.
- **Separate Sable Render Distance (SSRD)** ([Modrinth](https://modrinth.com/mod/ssrd)) est indispensable avec DH : sinon les dirigeables disparaissent au-delà de la distance vanilla. Il demande DH et Sodium côté client. La distance maximale se règle par la gamerule `ssrdMaxRenderDistance` (128 par défaut) et respecte le plafond de chargement forcé. Défaut connu : on voit les sous-niveaux à travers les LOD avec certains shaders (pas de shader ici).
- **Sound Physics Aeronautics** ([moddex](https://moddex.gg/mod/sound-physics-aeronautics)) peut utiliser les données DH pour le son des hélices lointaines. **O**, très confidentiel et cohérent avec la DA.
- Coût : la génération de LOD côté serveur s'ajoute à Chunky. La prégénération (§6) sert aussi à DH, puisque les LOD se calculent plus vite à partir de chunks existants.

---

## 2. Compatibilité Sinytra Connector (Antique Atlas 4 + Surveyor)

- Les tickets Connector sur Antique Atlas et Surveyor sont **tous fermés** : #1729 (Continuity/AA/Surveyor, « fixed in dev », février 2026), #1398, #1392, #1309 (écran AA4 flou), #1554 (Farsight).
- Tickets ouverts sans rapport avec le pack : MiniHUD, owo-lib/Jankson, Charm Forked. Le #2277 (Curios) vient en fait de Bonded, absent du pack ([#2277](https://github.com/Sinytra/Connector/issues/2277)).
- Sodium 0.6.13 a déjà amélioré la compatibilité avec les mods Fabric chargés via Connector.
- Règles :
  1. **Figer** les versions de Connector et de FFAPI ensemble ; ne mettre à jour que par paire.
  2. **N'ajouter aucun autre mod Fabric** via Connector : chaque mod ajouté augmente le risque. Tous les candidats ci-dessus existent en NeoForge natif.
  3. `config/connector.json` : la protection `enableMixinSafeguard` existe (2.0.0-beta.17) ; la laisser active.
  4. Après chaque mise à jour, tester en priorité l'ouverture de l'atlas, `/surveyor share` et les tombes sur la carte.
- Incertitude : la compatibilité Connector avec ModernFix et Lithium n'est documentée nulle part. Elle marche en pratique (le pack tourne), mais il faut surveiller le log de démarrage pour les avertissements de mixin.

---

## 3. Masquer les coordonnées et le debug

1. **Gamerule `reducedDebugInfo true`.** Le F3 n'affiche plus XYZ, bloc, chunk ni biome. D'après mes connaissances, elle bloque aussi F3+B (hitboxes) et F3+G (bordures de chunks) ; à vérifier en jeu (minecraft.wiki inaccessible). Elle est stockée dans `level.dat` et s'applique à la création via la fonction `load` (§5.3).
2. **Fuites à boucher, mod par mod :**
   - Supplementaries : coordonnées à la boussole, déjà désactivées (décision 0006).
   - GraveStone : nécrologie à désactiver.
   - Jade : plugin de coordonnées à désactiver.
   - Distant Horizons : lignes F3 (position LOD ?) à vérifier.
   - Antique Atlas 4 et Surveyor : affichage de coordonnées au survol ? Le README n'en parle pas, à vérifier.
   - Aeronautics : un instrument qui affiche la position (altimètre, navigation) ? L'altitude seule reste acceptable.
3. **Aucun mod tiers de « F3 restreint » n'est nécessaire** : la gamerule suffit. Rien n'empêche techniquement un ami d'installer une minimap côté client. Cela relève du pacte entre joueurs : packwiz livre la même liste à tous, et c'est à écrire dans le README.
4. La **boussole de récupération** vanilla (vers le lieu de mort) et l'atlas restent les seuls outils d'orientation, ce qui est cohérent.

---

## 4. Supprimer proprement l'End et les élytres (décision 0004)

Approche en trois couches, sans mod dédié (« bloqueur de dimension » non vérifié et inutile) :

1. **Worldgen, plus aucun stronghold** : `pack/kubejs/data/minecraft/tags/worldgen/biome/has_structure/stronghold.json` :
   ```json
   { "replace": true, "values": [] }
   ```
   Doublon possible : la config de Sparse Structures peut aussi neutraliser `minecraft:stronghold`. Sans stronghold, l'œil de l'Ender n'a plus de cible.
2. **Règles KubeJS (serveur)** :
   ```js
   // server_scripts/no_end.js
   ServerEvents.recipes(e => { e.remove({ output: 'minecraft:ender_eye' }); e.remove({ output: 'minecraft:end_crystal' }) })
   BlockEvents.rightClicked('minecraft:end_portal_frame', e => { if (e.item.id == 'minecraft:ender_eye') e.cancel() })
   // filet de sécurité : interdit tout voyage vers l'End (événement NeoForge natif)
   NativeEvents.onEvent('net.neoforged.neoforge.event.entity.EntityTravelToDimensionEvent', e => {
     if (String(e.getDimension().location()) == 'minecraft:the_end') e.setCanceled(true)
   })
   ```
   La disponibilité de `NativeEvents` dans KubeJS 2101 et le nom exact de l'événement sont à confirmer dans le .jar.
3. **LootJS** : retirer `minecraft:elytra` et `minecraft:ender_eye` de toutes les tables, pour couvrir les structures de mods.
   ```js
   LootJS.modifiers(e => { e.addTableModifier(/.*/).removeLoot('minecraft:elytra').removeLoot('minecraft:ender_eye') })
   ```
   Syntaxe LootJS 3 à vérifier.
4. **Visionneuse** : masquer élytres, œil de l'Ender, blocs de l'End et shulkers via `RecipeViewerEvents` (KubeJS 2101 prend en charge JEI, EMI et REI).

La dimension existe toujours techniquement, mais reste inaccessible et ne se génère jamais. Le dragon n'est jamais initialisé.

---

## 5. Outillage, configs et habillage

### 5.1 Configs : `config/` ou `defaultconfigs/`
- `config/` : configs client et communes, globales. Packwiz les écrase à chaque mise à jour. N'y mettre que ce qu'on impose à tous.
- `defaultconfigs/` : modèles des **configs serveur par monde** (`<monde>/serverconfig/*-server.toml`), copiés **uniquement à la création du monde** ou si le fichier manque. Exemples : Create, GraveStone, OPAC, Power Loader, Ecliptic ?
  - **Conséquence** : pour un monde permanent déjà créé, toute modification d'une config serveur doit **aussi** être copiée à la main (ou par script de déploiement) dans `world/serverconfig/`.
  - Prévoir un `scripts/deploy-serverconfig` qui synchronise `pack/defaultconfigs` vers `world/serverconfig` avant le démarrage.
- `options.txt` et les touches : via Default Options (fiche 31), pas via packwiz.

### 5.2 Organisation KubeJS proposée
```
kubejs/startup_scripts/   registres (rares)
kubejs/server_scripts/    00_constants.js, no_end.js, recipes/*.js (par mod), loot/*.js, trades.js (MoreJS), spawns.js, seasons_coldsweat.js
kubejs/client_scripts/    recipe_viewer.js (masquage), tooltips.js
kubejs/data/fallow/       function/init.mcfunction, advancement/**, patchouli_books/**
kubejs/data/minecraft/    tags/worldgen/** (stronghold vide), structure_set (villages)
kubejs/assets/            textures/gui/title/background/panorama_*.png, texts/splashes.txt, patchouli_books (contenu)
```

### 5.3 Gamerules appliquées automatiquement
`kubejs/data/fallow/function/init.mcfunction`, plus `kubejs/data/minecraft/tags/function/load.json` → `["fallow:init"]` :
```
gamerule reducedDebugInfo true
gamerule doInsomnia false
gamerule doPatrolSpawning false
gamerule disableRaids true
gamerule doTraderSpawning false
gamerule playersSleepingPercentage 50
gamerule keepInventory false
gamerule spectatorsGenerateChunks false
gamerule announceAdvancements true
gamerule logAdminCommands true
```
- `doTraderSpawning false` : le marchand ambulant contredit les « survivants rares ». Le réactiver si MoreJS le transforme en survivant.
- `mobGriefing` reste `true` : les moutons ont besoin de manger l'herbe et les villageois de cultiver.
- `naturalRegeneration` est à arbitrer avec le pilier survie.

### 5.4 Habillage sans mod
Panorama du menu (captures de brume de NML) dans `kubejs/assets/minecraft/textures/gui/title/background/panorama_0..5.png`, et `assets/minecraft/texts/splashes.txt` en français. FancyMenu plus tard.

### 5.5 Packwiz côté serveur
- Script de démarrage : `java -jar packwiz-installer-bootstrap.jar -g -s server https://<hôte>/pack/pack.toml`, puis lancement de NeoForge.
  - L'hôte peut être un fichier brut de la release GitHub ou GitHub Pages ; une URL raw de la branche `main` livrerait des changements non publiés.
  - Les mods `side = "client"` sont ignorés.
- **Auditer le champ `side`** : Crash Assistant, Mouse Tweaks, Controlling, Searchables, First-person Model, Not Enough Animations, Shoulder Surfing, FancyMenu et Default Options en `client`. Simple Voice Chat, Jade, AppleSkin et GraveStone en `both`. ProbeJS absent du pack.

---

## 6. Proposition de socle technique

### 6.1 Liste finale proposée (en plus des retenus)
| Statut | Mods |
|---|---|
| **Socle** | Neruina, Simple Backups, JEI, LootJS, KubeJS Create |
| **Recommandés** | Create: Power Loader, Jade, AppleSkin, Mouse Tweaks, Controlling + Searchables, GraveStone Mod, Simple Voice Chat, Crash Assistant, Default Options, MoreJS, Almost Unified, ProbeJS (dev seulement) |
| **Optionnels** | EMI, Open Parties and Claims (sans chunks chargés), Configured, Polymorph, Packet Fixer, Corpse (à la place de GraveStone), First-person Model, Not Enough Animations, Shoulder Surfing Reloaded, FancyMenu + Drippy, Better Advancements, GuideME, addon radio pour la voix, Sound Physics Aeronautics, Distant Horizons + SSRD (branche dédiée) |
| **Écartés** | FTB Backups 2/3, FTB Chunks, FTB Quests (CurseForge), Corail Tombstone, YIGD, ServerCore (pour l'instant), Open Loader / Global Packs, Catalogue, Inventory Profiles Next, Emotecraft, Better Third Person, Hold My Items, Akashic Tome, Carry On (#848, et il banalise la logistique des expéditions et de l'escorte), Sodium Extra / Reese's Sodium Options (A6), Embeddium (Sodium officiel exigé par Sable) |

À réévaluer d'urgence parmi les retenus : **Entity Culling** (A1).

### 6.2 `server.properties`
```
allow-flight=true                 # A2, indispensable pour Aeronautics et les contraptions
view-distance=10                  # la brume masque de toute façon le lointain ; 12 au maximum
simulation-distance=6             # ticks Create/fermes autour des joueurs ; 8 si spark le permet
white-list=true
enforce-whitelist=true
spawn-protection=0
enable-command-block=false
max-tick-time=120000              # tolère les pics de génération sans tuer le serveur ; Neruina gère les entités
sync-chunk-writes=true            # sécurité d'un monde permanent avant la performance
network-compression-threshold=256
difficulty=normal                 # hostiles vanilla supprimés par ailleurs
entity-broadcast-range-percentage=100
```
- Port UDP 24454 ouvert pour Simple Voice Chat.
- JVM serveur : Java 21, `-Xms8G -Xmx8G` (10G avec DH côté serveur), G1 avec les flags d'Aikar, ou `-XX:+UseZGC -XX:+ZGenerational` si la machine a au moins 4 cœurs.
- Client : 6 Go, 8 Go avec DH.

### 6.3 Prégénération (Chunky, déjà retenu)
- Avant l'ouverture : `chunky world minecraft:overworld`, `chunky shape circle`, `chunky center 0 0`, `chunky radius 4000`, `chunky start`.
  - Environ 200 000 chunks : quelques heures avec NML et les structures, et 6 à 10 Go de disque (estimation).
  - Activer `continueOnRestart` dans la config Chunky.
- Nether (s'il est gardé) : rayon de 1 000.
- Étendre par paliers (6 000, 8 000) quand la frontière progresse.
- Bordure de monde : `worldborder set 50000` (rayon de 25 000) pour borner le disque et DH tout en restant « immense ».
- Lancer Chunky **après** le réglage final de la graine et des configs worldgen (NML, Sparse Structures, tag stronghold).

### 6.4 Plan de sauvegarde (trois niveaux)
1. **Dans le jeu (Simple Backups)** : modulaire toutes les 60 min si des joueurs sont connectés, complète toutes les 24 h, 48 sauvegardes conservées, sur un disque ou volume local distinct du monde.
2. **Hôte (restic ou borg, hors machine)** : chaque nuit `rcon save-off`, puis `save-all flush`, snapshot de `world/`, `config/`, `defaultconfigs/` et `kubejs/`, puis `save-on`. Rétention : 7 quotidiennes, 4 hebdomadaires, 6 mensuelles, envoyées hors site (Backblaze, S3, NAS).
3. **Avant chaque mise à jour du pack** (fusion dans `main` et release) : sauvegarde complète manuelle étiquetée avec la version du pack. Retour arrière possible à la paire monde + pack.
4. **Test de restauration** mensuel sur une copie locale.

### 6.5 Redémarrage
- Une unité systemd (`Restart=on-failure`) suffit, avec un redémarrage quotidien à 6 h et avertissements RCON à T-10 et T-1 min. Aucun mod nécessaire.
- Diagnostic : spark (`/spark profiler`, `/spark tickmonitor`, `/spark health`), déjà retenu.

---

## 7. Incertitudes à vérifier dans les .jar et au premier lancement
1. **Entity Culling** : noms exacts des clés (`tickCulling`, `skipBlockEntityCulling`, `blockEntityWhitelist`) et effet sur un dirigeable assemblé. Si le bug reste, retrait.
2. **Distant Horizons** : clé exacte « Disable Vanilla Fog » et options de brouillard lointain dans `DistantHorizons.toml` ; lignes F3 ; cohabitation avec ATMOSPHERICS.
3. **SSRD** : son curseur fonctionne-t-il avec Sodium 0.8.13 ?
4. **Simple Backups** : noms des clés et prise en charge 1.21.1 NeoForge sur Modrinth.
5. **GraveStone** : clés nécrologie, fantôme et propriétaire dans `gravestone-server.toml` ; comportement d'une mort sur un sous-niveau Sable (où le bloc est-il posé ?).
6. **Jade** : présence d'un plugin de coordonnées et d'un fichier de surcharge côté serveur.
7. **Power Loader** : version NeoForge 1.21.1 sur Modrinth, clés serveur (limites, rotation minimale) et comportement sur un sous-niveau Sable.
8. **OPAC** : clés pour limiter les claims et désactiver le chargement de chunks.
9. **KubeJS 2101** : existence de `NativeEvents.onEvent` et prise en compte de `kubejs/data` (tags worldgen) **dès la création du monde** sur serveur dédié. Sinon, plan B : datapack dans `world/datapacks/` avant la première génération.
10. **LootJS 3 / MoreJS 1.21.1** : syntaxe exacte et événements d'échanges.
11. **KubeJS Create** : compatibilité Create 6.0.10, support Ponder.
12. **Almost Unified** : tags `c:` réellement partagés entre Farm & Charm et Kaleidoscope Cookery (tomate, laitue, farine, pâte) et entre Overgeared et Create (métaux).
13. **`reducedDebugInfo`** : blocage effectif de F3+B et F3+G en 1.21.1 ; Antique Atlas et Surveyor n'affichent pas de coordonnées.
14. **Sinytra Connector** : avertissements de mixin au démarrage avec ModernFix et Lithium ; versions Connector et FFAPI figées ensemble.
15. Disponibilité Modrinth 1.21.1 NeoForge, **non vérifiée par recherche** (quota épuisé) : Simple Backups, Packet Fixer, Polymorph, Default Options, Eating Animation, Better Advancements, FancyMenu et Drippy, Configured, addon radio. Le `packwiz mr add` tranchera.

---

## Sources
- Sodium 1.21.1 NeoForge, compatibilité Aeronautics, Sable et Veil, incompatibilité Sodium Options API : https://modrinth.com/mod/sodium/version/mc1.21.1-0.8.12-beta.1-neoforge · https://modrinth.com/mod/sodium/version/mc1.21.1-0.8.12-alpha.2-neoforge
- Sable et Sodium : https://github.com/Creators-of-Aeronautics/Simulated-Project/issues/1220 · Iris et Sodium 0.9 : https://github.com/IrisShaders/Iris/issues/3243
- Entity Culling et Aeronautics/Sable : https://github.com/Creators-of-Aeronautics/Simulated-Project/issues/1347 · https://github.com/Creators-of-Aeronautics/Simulated-Project/issues/974 · https://github.com/ryanhcode/sable/issues/1414 · https://github.com/tr7zw/EntityCulling
- JEI et Aeronautics : https://github.com/Creators-of-Aeronautics/Simulated-Project/issues/1405 · liste des incompatibilités : https://github.com/Creators-of-Aeronautics/Simulated-Project/issues?q=is%3Aissue+incompatible
- Create Aeronautics: Compatibility (correctifs Sable) : https://modrinth.com/mod/create-aeronautics-compatability
- Lithium NeoForge 1.21.1 : https://modrinth.com/mod/lithium/version/mc1.21.1-0.15.0-neoforge
- Distant Horizons 2.3.0-b : https://modrinth.com/mod/distanthorizons/version/2.3.0-b-1.21.1 · SSRD : https://modrinth.com/mod/ssrd · Sound Physics Aeronautics : https://moddex.gg/mod/sound-physics-aeronautics
- Connector : https://github.com/Sinytra/Connector/issues?q=is%3Aissue+atlas · https://github.com/Sinytra/Connector/issues/2277
- Antique Atlas : https://github.com/Sisby-Folk/antique-atlas
- Neruina : https://github.com/Bawnorton/Neruina · FTB Backups 2 et 3 : https://github.com/FTBTeam/FTB-Backups-2 · https://github.com/FTBTeam/FTB-Backups-3 · FTB Quests : https://github.com/FTBTeam/FTB-Quests
- OPAC : https://github.com/thexaero/open-parties-and-claims · Power Loader : https://github.com/hlysine/create_power_loader
- GraveStone : https://github.com/henkelmax/gravestone · Simple Voice Chat : https://github.com/henkelmax/simple-voice-chat
- EMI : https://github.com/emilyploszaj/emi · Jade : https://github.com/Snownee/Jade · Carry On : https://github.com/Tschipp/CarryOn · Crash Assistant : https://github.com/KostromDan/Crash-Assistant
- KubeJS 2101 (intégrations emi/jei/rei) : https://github.com/KubeJS-Mods/KubeJS/tree/2101/src/main/java/dev/latvian/mods/kubejs/integration · KubeJS Create : https://github.com/KubeJS-Mods/KubeJS-Create · LootJS : https://github.com/AlmostReliable/lootjs · MoreJS : https://github.com/AlmostReliable/morejs · Almost Unified : https://github.com/AlmostReliable/almostunified · ProbeJS : https://github.com/Prunoideae/ProbeJS · GuideME : https://github.com/AppliedEnergistics/GuideME
- Embeddium (fin de développement, Sodium officiel recommandé) : https://craftdownunder.co/guides/mods/embeddium
