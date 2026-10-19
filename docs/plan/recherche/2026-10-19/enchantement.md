# Enchantement : quel mod pour en faire une vraie phase de jeu ?

> Recherche du 2026-10-19, à la demande de l'équipe : « Pour Easy Magic, fais d'abord une recherche de mod qui change la façon dont on enchante dans Minecraft afin d'avoir une nouvelle phase de jeu comme le font Overgeared et Kaleidoscope. »
> Sources : API Modrinth (recherche avec les filtres `neoforge` + `1.21.1`, puis `/project/<slug>/version?loaders=["neoforge"]&game_versions=["1.21.1"]` pour chaque candidat), jars téléchargés et ouverts (`javap`, `strings`, JSON de `data/` et `assets/`), dépôt `origin/main` (`docs/mods.md`, `pack/config/quark-common.toml`, `pack/mods/*.pw.toml`), rapports du 2026-10-18 (`verif-2026-10-18/1-overgeared.md`, `4-monde-et-objectifs.md`), chapitres 11 et 12 du plan.
> « [jar] » = lu dans le jar ; « [API] » = API Modrinth ; « déduit » = conclusion tirée du code ou des données, sans test en jeu.

---

## 0. L'essentiel

1. **Recommandation : Immersive Enchanting** (`immersive-enchanting`, 6.0.1 pour NeoForge 1.21.1, 7,1 Mo dont 6,8 Mo de deux disques de musique, plus AlfinoLib 267 Ko). L'enchantement devient une **bibliothèque de savoirs** : on trouve des **Livres anciens** (« Savoir de Tranchant ») dans les ruines, on les range dans des **bibliothèques ciselées** autour de la table, et chaque niveau se paie en **matériaux** (une liste par enchantement, en JSON) plutôt qu'en hasard. Les livres enchantés disparaissent du butin et des échanges : ils sont **remplacés par des Livres anciens**. C'est l'équivalent exact des plans d'Overgeared : on trouve un savoir, on le garde, on le copie.
2. **Repli : la Table d'enchantement matricielle de Quark** (module `Matrix Enchanting`), **déjà dans le jar de Quark 4.1** mais coupée (`oddities = false`). C'est le seul vrai **mini-jeu** trouvé : on tire des pièces avec du lapis et de l'XP, on les tourne, on les pose sur une grille, on fusionne deux pièces pour monter de niveau, et des **bougies de couleur** (ou n'importe quel bloc, par config) orientent le tirage. Zéro Mo ajouté, traduction française incluse. Il lui manque la dimension « savoir trouvé ».
3. **Easy Magic ne doit pas entrer dans le pack**, quel que soit le choix : il remplace le bloc de table vanilla par le sien, alors qu'Immersive Enchanting (comme Quark Matrix et Enchanting Overhauled) s'accroche à la table vanilla. Son seul apport (la table garde l'objet et le lapis) est déjà dans Immersive Enchanting et dans Quark Matrix.
4. **Aucun conflit avec Overgeared** : Immersive Enchanting ne touche ni l'enclume, ni la table de forgeron, ni la meule (4 mixins seulement : table, entité de table, `ItemStack` pour l'infobulle, échanges des villageois). Overgeared ne touche pas l'enchantement. Les outils d'Overgeared sont dans les tags vanilla (`minecraft:pickaxes`…), donc enchantables.
5. **Tout se relie par des données**, sans mod custom : coûts en JSON (faune, flore, acier d'Overgeared), modificateurs de butin du mod lui-même pour mettre des livres **thématiques** dans les ruines NML (y compris l'archéologie), KubeJS pour donner un livre à la victoire sur un gardien d'Ancient Remnants et à des paliers de Wildex.
6. Deuxième repli, plus risqué : **Enchanting Overhauled** (autel qu'on réveille avec un Tome trouvé, enchantements « thématiques » donnés par les blocs posés autour). Très bonne idée de lieu, mais peu utilisé (4 100 téléchargements), licence ARR, et il rééquilibre beaucoup de choses (3 enchantements par objet, niveau III maximum, protection seulement sur le plastron, courbe d'XP).

---

## 1. Ce qui est confirmé

### 1.1 Le balayage de Modrinth

Recherches faites avec les filtres `categories:neoforge` + `versions:1.21.1` (une quarantaine de requêtes : enchanting, enchantment, rune, engraving, socket, infusion, ritual, altar, glyph, tome, research, scholar, grimoire, pedestal, ancient library…). Plus de 430 projets passés en revue ; ceux qui touchent à l'enchantement sont classés ci-dessous. Versions NeoForge 1.21.1 vérifiées une à une [API].

**Candidats sérieux (jar ouvert)**

| Mod | Version NF 1.21.1 | Taille | Dépendances | Ce qu'il fait vraiment | Verdict |
|---|---|---|---|---|---|
| **Immersive Enchanting** | 6.0.1, 2026-08-07 | 7,1 Mo | AlfinoLib ≥ 1.2.0 | Livres anciens à ranger, coûts en matériaux, arbre d'enchantements par objet (§1.2) | **Choix** |
| **Quark : Matrix Enchanting** | dans Quark 4.1-485 (déjà au pack) | 0 | aucune | Mini-jeu de pièces sur grille, influence des bougies et de blocs choisis (§1.3) | **Repli** |
| **Enchanting Overhauled** | 2.1.6, 2026-06-01 | 668 Ko | aucune | Autel à réveiller avec un Tome, thèmes par blocs voisins, gros rééquilibrage (§1.4) | 2e repli |
| Apothic Enchanting | 1.21.1-1.6.4, 2026-10-08 | 5,3 Mo | Placebo + Apothic Attributes | Étagères à statistiques (Eterna, Quanta, Arcana), infusion à la table, nouveaux enchantements ; 20 mixins [jar] | Écarté : lourd, rééquilibre très haut, Apothic Attributes refait les attributs (côté RPG) |
| Modest Magic | 1.4.2, 2026-09-28 | 421 Ko | EMI, Cloth Config (optionnels) | Autel + piédestaux (infusion, enchantement, **invocation** de golems et du Wither), tablettes posées à la table de forgeron, **un niveau** par enchantement ; aucun mixin commun [jar] | Écarté : progression plate (niveau I seulement), invocations hors ton |
| Enchantment Infusion | 1.3.4, 2026-10-12 | 172 Ko | aucune | Table + piédestaux façon Ars Nouveau, recettes JSON par enchantement et niveau | Écarté : **demande NeoForge ≥ 21.1.252**, le pack a 21.1.251 (`neoforge.mods.toml` du jar) |
| Easy Magic | v21.1.4, 2025-10-26 | 141 Ko | Puzzles Lib | Confort : la table garde l'objet et le lapis, bouton de relance, aperçu des enchantements ; **remplace le bloc vanilla** (`BlockConversionHandler`, `EnchantmentTableWithInventoryBlock`) [jar] | **Retirer du plan** (point 0.3) |
| Enchanting Infuser | v21.1.4, 2026-03-30 | 195 Ko | Puzzles Lib | Nouvelle table où l'on choisit ses enchantements contre un prix fixe | Écarté : supprime le hasard sans créer de phase |
| Create: Enchantment Industry | 2.5.4b (**bêta**), 2026-10-09 | 1,6 Mo | Create, Create: Dragons Plus | Enchanteur à blaze, XP liquide, imprimante à livres, forgeur à blaze [jar, lang] | Écarté : automatise ce qu'on veut rendre précieux, et la règle « pas trop d'addons Create » |

**Écartés sur la description et l'API**

| Mod | Raison (preuve API) |
|---|---|
| Ars Nouveau (5.13.3, 19,4 Mo) | Tout un système de sorts, glyphes, familiers : trop |
| Iron's Spells 'n Spellbooks (3.16.3, 13,6 Mo) | RPG de sorts et d'écoles de magie |
| Occultism (1.224.5, 6,8 Mo, 4 dépendances) | Rituels d'invocation de démons : ton |
| Hexerei (0.5.0.3, 10,2 Mo, 2025) | Sorcellerie, chaudrons, balais : ton, pas d'enchantement |
| Enchanted: Witchcraft (4.2.7, 3 Mo, 3 dépendances) | Sorcellerie complète |
| Hexalia (1.3.8.1, 3,2 Mo) | Herboristerie et rituels « folkloriques » : joli mais ce n'est pas de l'enchantement |
| Nature's Aura (41.10, 1,5 Mo) | Aura du monde et autels naturels ; ne touche pas l'enchantement |
| Botania | **Pas de NeoForge 1.21.1** (dernier : 1.20.1) |
| Malum (1.8.2, **32,8 Mo**) | Âmes de créatures, ton sombre |
| Eidolon: Repraised (0.5.0.7, 4 Mo) | Nécromancie |
| Forbidden & Arcanus (2.6.1, 3,3 Mo + Valhelsia Core) | Forge d'Héphaïstos, ton sombre |
| Theurgy (1.78.0, 5,2 Mo) | Alchimie de transmutation |
| Reactive Alchemy (10.2.1, 1,5 Mo) | Alchimie expérimentale, pas d'enchantement |
| Spectrum (1.12.9, **97 Mo**) | Énorme |
| Grimoire of Gaia (alpha), L_Ender's Cataclysm (71,7 Mo) | Hors sujet (créatures, boss) |
| Tome of Blood: Rebirth | Pas de NeoForge 1.21.1 (Forge 1.20.1) |
| Better Enchanting, Enchanting System Overhaul | Fabric ou Forge 1.20 seulement |
| « Enchantment Tables Revamped » | Introuvable sur Modrinth |
| Enchantment Descriptions (21.1.11) | Infobulles seulement, deux bibliothèques (Prickle, Bookshelf) ; déjà écarté au chapitre 11. Immersive Enchanting affiche ses propres descriptions |
| Runes of Power, Runeweaver, Runology (alpha) | Runes à capacités actives : RPG |
| Craftable Enchanted Books, Enchantment Custom Table, Enchanting Plus+ (bêta), Improved Enchanting (bêta), Advanced Enchanting Table (2024) | Stations de fabrication ou de confort, peu suivies, sans phase |
| Grind Enchantments, Dis-Enchanting Table, Infusion Table, Better Chiseled Bookshelf | Petits outils (désenchanter, puissance des étagères) |

### 1.2 Immersive Enchanting, lu dans le jar

Jar : `beta-immersiveenchanting-6.0.1-neoforge-1.21.1.jar` (le nom contient « beta », mais l'API le donne en `release`), sha512 `9c65fcb0544a20e1…` [API]. Version publiée le 2026-08-07 ; le changelog corrige des bugs numérotés jusqu'à #76 (« XP levels now subtract correctly [#74] ») et des portages 26.1 suivent en septembre : le mod est **suivi** [API].

| Point | Valeur | Preuve |
|---|---|---|
| Dépendances | NeoForge ≥ 21.1.209 (le pack a 21.1.251), Minecraft 1.21.1, **AlfinoLib ≥ 1.2.0** (267 Ko, aucun mixin) | `META-INF/neoforge.mods.toml` des deux jars |
| Mixins | 4 : `EnchantingTableBlockMixin` (remplace le menu de la table **vanilla**), `EnchantingTableBlockEntityMixin` (la table **garde ses objets**), `ItemStackMixin` (infobulle), `VillagerTradeMixin` (retire les livres des échanges) | `immersiveenchanting.mixins.json`, `javap` |
| Livre ancien | Objet `immersiveenchanting:ancient_book`, porte l'enchantement dans le composant vanilla `minecraft:stored_enchantments` (déduit de `EnchantmentUtil.setStoredEnchantment` et `DataComponents.STORED_ENCHANTMENTS`) ; composant `immersiveenchanting:replicated` sur les copies | `RemoveEnchantedBooksModifier`, `ModDataComponents` |
| Débloquer | Le livre doit être **dans une bibliothèque ciselée** (`ChiseledBookShelfBlockEntity`) à portée de la table : 2 blocs en X et Z, 3 en Y par défaut (réglable jusqu'à 8) | `BookshelfChecker`, `ServerConfig` (`bookshelfSearchX/Y/Z`) |
| Payer | Un fichier par enchantement : `data/immersiveenchanting/enchantment_costs/<mod>/<enchantement>.json`, `{"enabled": true, "levels": {"1": [{"item_cost": {...}, "xp_levels": n}]}}`. `enabled` est facultatif (vrai par défaut), `xp_levels` aussi. Le carburant (lapis, tag `neoforge:enchanting_fuels`) se paie en plus : 1 par niveau | `CostData` (`optionalFieldOf("enabled", true)`), `Cost.xpLevels()`, `enchanting_fuels.json` |
| Coûts par défaut | Tranchant : cactus → améthyste → prisme → quartz → écho ; Protection : écaille de tatou → de tortue → nautile → carapace de shulker ; Fortune : pomme d'or → bloc d'émeraude → **plante-amphore** (celle du renifleur) ; Raccommodage : étoile du Nether | `enchantment_costs/minecraft/*.json` (43 fichiers) |
| Enchantement sans fichier | **Désactivé**, avec un avertissement au lancement | lang `warn.cost_files_missing`, commande `disabled_enchantments` |
| Copier un livre | « Replicate » : un livre et plume + 10 niveaux ; la copie ne peut plus être transmutée | `replicate.json`, lang `not_transmutable` |
| Transmuter | Change l'enchantement d'un livre ancien (10 niveaux), seulement si un livre du même enchantement est déjà rangé | `transmute.json`, lang `transmute_hint` |
| Retirer un enchantement | « Hold to remove » : on maintient le clic pour l'enlever d'un objet (réglable) | lang, `allowEnchantmentRemoval = true` |
| Mystère | Les enchantements non débloqués s'affichent en alphabet galactique (`obfuscateLockedEnchantments = true` par défaut) | `ServerConfig` |
| Butin | Les livres enchantés de **tous** les butins sont **remplacés** par des livres anciens (mode `REPLACE` par défaut ; `REMOVE` possible). Plus 22 modificateurs qui en ajoutent dans les coffres vanilla, avec deux modes : `random_enchantment_from_enabled` ou `random_enchantment_from_list` + liste | `loot_modifiers/*.json`, `ServerConfig$EnchantedBookLootMode` |
| Échanges | Plus de livres enchantés chez les villageois (`disableEnchantedBookTrades = true`) | `ServerConfig` |
| Structures | 3 « bibliothèques anciennes » (désert, jungle, End), placées sur les tags vanilla `has_structure/desert_pyramid`, `jungle_temple`, `end_city`, espacement 50 ; un processeur remplit leurs bibliothèques ciselées | `worldgen/`, `FillChiseledBookshelfProcessor` |
| Interface | Deux onglets : « Enchanting » (l'arbre des niveaux pour l'objet posé, zoomable) et « Ancient Books » (filtre débloqués / verrouillés, total) : une **collection** qu'on voit progresser | lang `tab.book`, `filter.unlocked`, `label.total_enchantments` |
| Sons | Sons de survol des nœuds réglables par données (`node_sounds.json`) | `datapack/node_sounds` |
| Config | **SERVEUR** (`ModConfig.Type.SERVER`), donc `serverconfig/immersiveenchanting-server.toml` dans le monde ; à livrer dans `defaultconfigs/` | `ImmersiveEnchanting.registerConfig` |
| Commandes | `disabled_enchantments`, `enabled_enchantments`, `generate_empty_datapack` (génère les fichiers de coût à remplir), `give_random_ancient_book` | `command/` |
| Langues | **en_us seulement** | `assets/immersiveenchanting/lang/` |
| EMI / JEI | Aucun plugin (aucune classe `dev/emi` ni `mezz/jei`) ; les coûts se lisent dans l'interface du mod | `grep` du jar |
| Poids | Code et données : 550 Ko ; `arcane_memories.ogg` 3,7 Mo et `biblioclasm.ogg` 3,1 Mo (deux disques, lus en flux seulement s'ils jouent). Le scan des bibliothèques ne se fait qu'à l'ouverture de la table (5 × 7 × 5 blocs par défaut) : **négligeable** (déduit) | tailles des fichiers |

Note : la page Modrinth dit encore que Raccommodage est « introuvable » et réglable par menu. Dans la 6.0.1, aucune classe ne parle de Raccommodage ; seul son fichier de coût existe (étoile du Nether). On le coupe donc avec `"enabled": false` [jar].

### 1.3 Quark Matrix Enchanting, lu dans le jar de Quark 4.1-485

Le chapitre de la décision 0005 n'en parle pas parce que le module vit dans la catégorie `oddities`, coupée dans `pack/config/quark-common.toml` (`oddities = false`).

| Point | Valeur | Preuve |
|---|---|---|
| Présence | `org/violetmoon/quark/addons/oddities/module/MatrixEnchantingModule` dans le jar du pack ; ses voisins : Backpack, Crate, Magnets, Pipes, Tiny Potato, Totem of Holding | `c-quark` [jar] |
| Mécanique | La table garde l'objet et le lapis. On **génère** des pièces avec du lapis (4 pièces par lapis) et des niveaux, on les **tourne**, les **pose**, les **retire** sur une grille, et on **fusionne** deux pièces du même enchantement pour monter d'un niveau | `OPER_ADD/PLACE/REMOVE/ROTATE/MERGE`, lang `quark.jei.hint.matrix_enchanting` |
| Lieu | Bibliothèques : jusqu'à 15 ; 3 pièces sans bibliothèque. **Bougies de couleur** posées sur les bibliothèques : chaque couleur pousse une liste d'enchantements (blanc → Solidité, orange → Protection contre le feu…) ; posées sur du sable des âmes, elles repoussent | `maxBookshelves = 15`, `baseMaxPieceCount = 3`, `influencesList`, `soulCandlesInvert` |
| Blocs choisis | `statesToInfluences` : n'importe quel état de bloc peut influencer, format `"bloc[état];force;couleur;enchantements"` ; API `IEnchantmentInfluencer` pour d'autres mods | textes de config du module |
| Livres | Possibles (1 pièce de base) ; trésors interdits par défaut (`allowTreasures = false`), enchantements hors du tag `minecraft:in_enchanting_table` interdits | idem |
| Conversion | La table vanilla devient une table matricielle à la pose (`automaticallyConvert = true`) | idem |
| Langues | Quark a `fr_fr.json` : « Table d'enchantement matricielle » | `assets/quark/lang/` |
| Autres modules d'enchantement de Quark | `Ancient Tomes` **off**, `Color Runes` **off**, `Uses For Curses` **off**, `Soul Candles` (puissance d'enchantement des bougies d'âme) **off**, `Improved Tooltips` **on** (avec `enchantingTooltips` : l'infobulle d'un livre montre les objets concernés), `Variant Bookshelves` **on** (bibliothèques de toutes les essences) | `pack/config/quark-common.toml`, chaînes des modules |

### 1.4 Enchanting Overhauled, lu dans le jar (2e repli)

- **L'autel** (`deactivated_enchanting_table` : obsidienne, améthyste, tapis rouge) se réveille quand on y pose un **Tome enchanté** ; il devient une table vanilla qui garde le tome (`TomeStorageAccessor`), avec éclair et particules (coupables : `activation_effects`). Un livre ordinaire provoque une explosion de force 5 (« arcane retribution », coupable). La recette vanilla de la table donne un « autel troublé » [jar : `DeactivatedEnchantingTableBlock`, `recipe/*.json`, `Config`].
- **Les thèmes** : 11 fichiers `enchantment_theme/*.json`. Un thème n'offre ses enchantements que si ses blocs sont autour de la table : glace bleue → Semelles givrantes et Toucher de soie ; éponge mouillée → la mer ; nid d'abeilles → Fléau des arthropodes ; cœur lourd → Brèche, Wind Burst… Tout est en données [jar].
- **Les Tomes** se trouvent dans les coffres (modificateur `tome_injection`) et dans 5 « ruines de bibliothèque » où apparaît un **évocateur** [jar : `worldgen/structure/*.json`].
- **Rééquilibrage** : 3 enchantements par objet, niveau III maximum, Protection coupée en physique et magique et réservée au plastron, courbe d'XP linéaire, enclume sans XP mais **sans livres**. Presque tout est réglable dans `config/enchanting_overhauled.properties`, sauf les définitions d'enchantements qu'il réécrit dans `data/minecraft/enchantment/` (39 fichiers) [jar].
- **Risques** : 25 mixins dont `ItemStack`, `Player` (courbe d'XP), `CombatRules`, `ServerLevel` ; un mixin sur `ItemCombinerMenu`, la classe de la **table de forgeron** vanilla qu'Overgeared utilise pour passer au diamant (l'enclume d'Overgeared, elle, est un `RecipeBookMenu` et n'est pas touchée) ; licence ARR ; 4 100 téléchargements ; pas de fr_fr (de, en, tr) [jar, API].

### 1.5 Overgeared ne touche pas l'enchantement

- Mixins d'Overgeared : refroidissement, empilement, joueur, potions, alambic. **Aucun** sur `AnvilMenu`, `EnchantmentMenu`, `ItemCombinerMenu` ; aucune écoute d'`AnvilUpdateEvent` [jar `overgeared-neoforge-1.21.1-1.6.19`].
- Ses outils et armures sont ajoutés aux tags vanilla `minecraft:pickaxes`, `swords`, `head_armor`… (`data/minecraft/tags/item/`), que les tags `minecraft:enchantable/*` incluent : ils sont enchantables (déduit). Ses paliers ont une valeur d'enchantabilité (`ModToolTiers.getEnchantmentValue`).
- L'enclume vanilla reste utilisable tant qu'on ne la convertit pas au marteau : réparation, renommage, fusion de deux objets enchantés. Immersive Enchanting n'y touche pas.

### 1.6 Les enchantements des autres mods du pack

Il faudra un fichier de coût pour chacun, sinon Immersive Enchanting le désactive [jar] :

| Mod | Enchantements | Proposition |
|---|---|---|
| Kaleidoscope Cookery 1.6.0 | `quick_knife`, `sweep` | Coûts en produits de cuisine (le couteau reste de la cuisine) |
| Alex's Mobs Continued | `serpentfriend`, `straddle_jump`, `lavawax`, `board_return` | Coûts en butin de la créature concernée, ou `enabled: false` |
| Create | `potato_recovery`, `capacity` | Laiton, mécanisme de précision |
| Supplementaries | `stasis` | Coût ou désactivé |

Source : `data/*/enchantment/*.json` dans les jars du pack.

---

## 2. Les liens concrets à construire

### 2.1 La boucle de jeu (Immersive Enchanting)

1. **Trouver un savoir.** On brosse le sable des ruines d'alchimiste NML, on ouvre les jarres anciennes, les puits, le cœur des donjons, le monument du renifleur ; on bat le gardien d'un monolithe ; on remplit le bestiaire. Chaque fois, un **Livre ancien** au titre illisible.
2. **Le ranger chez soi.** On bâtit une petite bibliothèque : une table, des bibliothèques ciselées à moins de 2 blocs (3 en hauteur), des bougies, des lanternes. Chaque livre posé se voit sur l'étagère, et le titre devient lisible dans la table.
3. **Réunir ce qu'il coûte.** Chaque niveau demande des matériaux du monde : butin de la faune (Wildex), plantes de saison, acier d'Overgeared, laiton de Create pour les hauts niveaux, plus un lapis par niveau et peu d'XP.
4. **Enchanter.** On pose l'objet forgé sur la table, on choisit le nœud dans l'arbre, on paie. On peut retirer un enchantement en maintenant le clic, pour en essayer un autre.
5. **Transmettre.** On copie un livre (livre et plume + 10 niveaux) pour un ami ou pour une seconde base ; un livre trouvé en double se transmute vers un savoir qu'on a déjà.

Le parallèle avec Overgeared est direct : **plan trouvé → table à dessin → forge**, **livre trouvé → bibliothèque → table**.

### 2.2 Ce qu'on garde de vanilla, ce qu'on coupe

| Élément | Décision | Outil |
|---|---|---|
| Table d'enchantement vanilla | **Gardée** comme bloc (recette inchangée : diamants, obsidienne, livre). Son menu est remplacé par Immersive Enchanting | aucun |
| Hasard de la table | Disparaît | natif |
| Livres enchantés (butin, pêche, échanges) | Remplacés par des livres anciens / retirés | config `enchantedBookLootMode = "REPLACE"`, `disableEnchantedBookTrades = true` (défauts) |
| Enclume vanilla | Gardée : réparation, renommage. Elle n'a plus de livres à appliquer | aucun |
| Raccommodage | **Coupé** : il annulerait l'entretien d'Overgeared (meule qui répare en perdant 5 % de durabilité, objets « cassés ») | `kubejs/data/immersiveenchanting/enchantment_costs/minecraft/mending.json` = `{"enabled": false, "levels": {}}` |
| Bibliothèques d'IE (structures) | Coupées : le pack préfère les ruines NML et Sparse Structures ; les disques de musique ne sortent qu'avec elles (déduit) | `data/immersiveenchanting/tags/worldgen/biome/ancient_library_{desert,jungle,end}_biomes.json` = `{"replace": true, "values": []}` |
| Easy Magic | **Non ajouté** | `docs/mods.md` |
| Quark Matrix, Ancient Tomes | Restent coupés | `quark-common.toml` inchangé |

### 2.3 Fichiers à écrire

| Quoi | Où | Outil |
|---|---|---|
| Ajouter le mod | `pack/mods/immersive-enchanting.pw.toml`, `pack/mods/alfinolib.pw.toml` | `packwiz modrinth add immersive-enchanting` (tire AlfinoLib) |
| Config serveur | `pack/defaultconfigs/immersiveenchanting-server.toml` : défauts gardés ; `bookshelfSearchY = 3` ; décider `allowEnchantmentRemoval` | config |
| Coûts Fallow | `kubejs/data/immersiveenchanting/enchantment_costs/minecraft/*.json` (43) + les 9 enchantements des autres mods (§1.6). Partir de `/immersiveenchanting generate_empty_datapack` | datapack |
| Livres dans les ruines NML | `kubejs/data/fallow/loot_modifiers/savoirs_*.json` de type `immersiveenchanting:ancient_book_loot_modifier`, condition `neoforge:loot_table_id` + `minecraft:random_chance`, et `kubejs/data/neoforge/loot_modifiers/global_loot_modifiers.json` (`replace: false`) | datapack (modificateur du mod, **pas besoin de LootJS**) |
| Livres des gardiens et du bestiaire | `kubejs/server_scripts/savoirs.js` | KubeJS (§2.5, §2.6) |
| Traduction | `kubejs/assets/immersiveenchanting/lang/fr_fr.json` (« Livre ancien », « Savoir de », « Reproduire », « Transmuter »…) | KubeJS assets |
| Registre | `docs/mods.md` : Immersive Enchanting ➕ dans « Construction et ateliers », AlfinoLib dans les bibliothèques, Easy Magic ❌ | doc |

### 2.4 Les ruines NML (exemples de tables)

Tables lues dans le jar NML 1.6.1 : `nomansland:archaeology/alchemist_ruins`, `remains_dungeon`, `sniffer_monument`, `trinket`, `well` ; `nomansland:chests/ancient_pot_cave`, `small_dungeon_core`, `sniffer_monument`, `water_well`, `alchemist_pot_*` [jar]. Proposition, à doser en jeu :

| Lieu | Savoirs (mode `random_enchantment_from_list`) | Pourquoi |
|---|---|---|
| Ruines d'alchimiste (archéologie, au pinceau) | Efficacité, Solidité, Protection, Toucher de soie | Le geste du pinceau donne le savoir |
| Monument du renifleur | Fortune, Butin, Chance de la mer | La plante-amphore paie déjà Fortune III |
| Puits (`well`, `water_well`) | Respiration, Affinité aquatique, Agilité aquatique | |
| Donjons (`remains_dungeon`, `small_dungeon_core`) | Tranchant, Châtiment, Recul, Protection contre les projectiles | |
| Jarres des grottes | Chute amortie, Protection contre les explosions | |

Avec Lootr, chaque joueur trouve son propre livre dans les coffres (déduit : Lootr génère le butin par joueur, les modificateurs de butin s'appliquent à chaque génération).

### 2.5 Ancient Remnants : un savoir par monument

Le script des gardiens (chapitre 12, rapport 6) rend la bénédiction et donne la page du codex à la mort du gardien. On y ajoute un livre ancien choisi :

```js
// server_scripts/savoirs.js — esquisse (syntaxe du composant à vérifier en jeu)
const livre = (ench) => Item.of('immersiveenchanting:ancient_book',
  `[minecraft:stored_enchantments={levels:{"${ench}":1}}]`)
// dans le rappel de victoire du gardien (même script que la clairière) :
// player.give(livre('minecraft:power'))
```

| Monument (gardien) | Savoir proposé |
|---|---|
| Monolithe chasseur (Sculpteur) | Puissance, Infinité, Perforation |
| Monolithe guerrier (Wroughtnaut) | Tranchant, Affilage |
| Monolithe sentinelle (aucun) | Protection, Semelles givrantes |
| Monument du cerisier (aucun) | Chute amortie, Furtivité rapide |
| Monument du chêne (Sacred Pontiff) | Fortune, Efficacité |
| Monument des conifères (Frostmaw) | Toucher de soie, Solidité |

Pour les monuments sans gardien, le livre peut passer par le même polling de l'Elderheart que la clairière (rapport 4, §2.3), la première fois qu'un joueur reçoit la bénédiction.

### 2.6 Wildex : le bestiaire ouvre des savoirs

Wildex a un pont KubeJS (`WildexKubeJsScriptBridge.onDiscoveryChanged`, rapport 4 §1). À certains paliers (`discoveredCount` 10, 25, 50, ou complétion d'un groupe), on donne un livre : Fléau des arthropodes après les insectes, Empalement après la faune marine. Et l'XP des découvertes (`newEntryXpAmount = 5`) paie les petits `xp_levels` des coûts : **explorer nourrit l'enchantement**.

### 2.7 Coûts : faune, saisons, forge

Principes pour réécrire les 43 fichiers (à valider avec l'équipe) :
- **Faune** : un niveau sur deux demande un butin animal (écailles, plumes, cuir, fourrure de Naturalist ou d'Alex's Mobs), pas un butin de monstre : le pack en a très peu.
- **Saisons** : les niveaux d'Efficacité et de Fortune demandent des fleurs ou des récoltes d'Ecliptic Seasons, de Farm & Charm ou de Kaleidoscope qui ne poussent qu'à une saison (liste à tirer des tags de saison).
- **Forge** : les hauts niveaux d'armes et d'armures demandent des **plaques d'acier** d'Overgeared (`overgeared:steel_plate`), ceux des outils du **laiton** de Create.
- **Lapis** : un par niveau (défaut). **XP** : faible ou nulle.

### 2.8 Ce qu'il ne faut pas lier

- **Lunes** d'Enhanced Celestials : rien dans Immersive Enchanting ne lit la lune. Un lien demanderait une matière « lunaire » fabriquée par KubeJS, et la détection de la lune passe déjà par le futur mod `fallow` (chapitre 12.4). À garder pour plus tard.
- **Qualité d'Overgeared** : aucun crochet. Brider les enchantements selon la qualité demanderait un petit mod (mixin sur le menu d'IE). Pas nécessaire.
- **Codex d'Ancient Remnants** : ne pas utiliser les « Research Scrap » comme coût : ce sont les pages du codex.

### 2.9 Si l'on prend le repli Quark Matrix

- `pack/config/quark-common.toml` : `oddities = true` dans `[categories]`, puis une section `[oddities]` avec `"Matrix Enchanting" = true` et `false` pour Backpack, Crate, Magnets, Pipes, Tiny Potato, Totem of Holding.
- Influences Fallow par `statesToInfluences` : blocs de NML, lanterne de clairière, crânes de créatures…
- Savoirs rares : retirer Toucher de soie, Fortune, Butin, Raccommodage du tag `minecraft:in_enchanting_table` (datapack) pour qu'ils ne viennent que des **livres** trouvés (LootJS dans les ruines NML), appliqués à l'enclume vanilla.
- Easy Magic reste hors du pack : la conversion de Quark attend `minecraft:enchanting_table`, que Easy Magic remplace (déduit).

---

## 3. Conflits et risques

| Risque | Gravité | Détail et parade |
|---|---|---|
| Easy Magic + Immersive Enchanting | Haute | Deux mods qui remplacent la table (Easy Magic par conversion du bloc, IE par mixin sur la table vanilla) : ne pas ajouter Easy Magic [jar des deux] |
| Mod jeune, auteur seul | Moyenne | « My first mod » (page Modrinth), 37 000 téléchargements, mais suivi en 2026 (6.0.1, bugs #74 et #76 corrigés) [API]. Repli prêt : Quark Matrix |
| Enchantements des autres mods désactivés | Moyenne | Sans fichier de coût, ils disparaissent : écrire les 9 fichiers (§1.6) |
| Raccommodage | Moyenne | Activé par défaut si l'on trouve le livre (étoile du Nether) : le couper |
| Fusion d'objets enchantés à l'enclume | Faible | On peut toujours combiner deux objets enchantés trouvés sur des créatures. Rare dans Fallow |
| Interface 3D | Faible | L'écran d'IE est une interface à lui : à tester avec Spatial GUI et Shoulder Surfing |
| Pas d'EMI | Faible | Les coûts ne sont pas dans EMI ; l'interface d'IE les montre au survol. L'infobulle de Quark (`enchantingTooltips`) reste |
| Pas de français | Faible | Fichier `fr_fr.json` à livrer, comme pour Overgeared |
| Poids | Faible | 7,4 Mo au total, dont 6,8 Mo de musique lue à la demande ; 4 mixins ; scan à l'ouverture seulement (déduit) |
| Quark `Variant Bookshelves` | Info | Ces bibliothèques ne sont pas ciselées : elles ne rangent pas de livres anciens. Seules les bibliothèques ciselées comptent (`ChiseledBookShelfBlockEntity`) [jar] |
| Enchanting Overhauled (s'il était choisi) | Moyenne | Mixin sur `ItemCombinerMenu` = table de forgeron, utilisée par Overgeared pour le diamant ; évocateurs dans ses ruines ; rééquilibrage du combat |

---

## 4. Ce qu'il faut adapter, supprimer ou ajouter dans le plan

| Où | Changement | Raison |
|---|---|---|
| `docs/mods.md`, « Construction et ateliers » | **Easy Magic** ➕ → ❌ « Remplacé par Immersive Enchanting, qui garde lui aussi l'objet sur la table » | §0.3, §3 |
| `docs/mods.md` | **Immersive Enchanting** ➕ (CS) : « L'enchantement par les livres anciens trouvés, rangés et payés en matériaux » ; **AlfinoLib** dans « À ajouter avec les mods ci-dessus » | §1.2 |
| Chapitre 11, tableau du confort (ligne Easy Magic) | Barrer Easy Magic, renvoyer à ce rapport | idem |
| Chapitre 10 §10.7 (liens) | Ajouter la chaîne « ruine NML / gardien AR / palier Wildex → livre ancien → bibliothèque → table » | §2 |
| Chapitre 10 §10.10 (scripts) | Ajouter `savoirs.js` (gardiens, Wildex) et le datapack de coûts et de butin | §2.3 |
| Décision à écrire | `docs/decisions/0008-enchantement.md` : Immersive Enchanting, repli Quark Matrix, Easy Magic sorti, Raccommodage coupé | traçabilité |
| Décision 0005 (Quark) | Noter que Matrix Enchanting existe dans 4.1 et reste coupé avec `oddities` | §1.3 |

---

## 5. Questions qui demandent un test en jeu

1. **Interface** : l'écran d'IE s'ouvre-t-il bien avec Spatial GUI (menus en 3D) et Shoulder Surfing ? Lisible en français ?
2. **Livre donné par KubeJS** : `Item.of('immersiveenchanting:ancient_book', '[minecraft:stored_enchantments={levels:{"minecraft:power":1}}]')` est-il reconnu comme un savoir une fois rangé ? (Sinon : `/immersiveenchanting give_random_ancient_book` et lire le composant avec `/data get`.)
3. **Archéologie NML** : un modificateur de butin d'IE sur `nomansland:archaeology/alchemist_ruins` donne-t-il bien un livre au pinceau ?
4. **Lootr** : chaque joueur trouve-t-il son livre dans le même coffre ?
5. **`"enabled": false`** coupe-t-il Raccommodage sans avertissement au lancement ?
6. **Overgeared** : la qualité (« Expert », « Master ») et l'état « cassé » restent-ils après enchantement et après retrait d'un enchantement ?
7. **Portée** : à quelle distance réelle une bibliothèque ciselée compte-t-elle (2 / 3 / 2) ? Une pièce de 5 × 5 suffit-elle ?
8. **Multijoueur** : la bibliothèque est partagée (les livres sont dans des blocs) ; la copie et la transmutation marchent-elles sur serveur ?
9. **Butin** : les livres enchantés qui restent (créatures, pêche) sont-ils tous remplacés ?
10. **spark** : aucun coût mesurable à l'ouverture de la table avec 10 bibliothèques ciselées pleines ?
11. **Repli Quark Matrix** (si testé) : la config `statesToInfluences` accepte-t-elle un état de bloc partiel, ou faut-il tous ses états ?
