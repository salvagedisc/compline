# Wayfarer, le modpack et le serveur de l'équipe de No Man's Land

Recherche du 2026-10-19 pour Fallow. Légende : **[V]** vérifié (lien dans la ligne ou dans les sources), **[D]** déduit de ce qu'on voit ou entend dans les vidéos, sans confirmation écrite. « Déjà là » veut dire présent dans Fallow tel que décrit par `docs/mods.md`. **Correction du 2026-10-19 :** le pack contient encore NML **1.5.12** ; ce qui vient de la 1.6 (outils ancestraux, épée du serment) n'arrive qu'avec la montée en 1.6.1, déjà prévue.

## Résumé

1. Wayfarer est un **modpack privé** (environ 200 mods, NeoForge 1.21.1) fait par **Monad Modding**, l'équipe de No Man's Land (Farcr, tazer…), avec un **serveur SMP officiel** ouvert vers juillet 2026. On y accède par le rang Patreon « Wayfarer » ; le rang « Discoverer » donne seulement les préversions **[V]**.
2. Une grande partie de ce qu'on voit dans les vidéos vient de **NML 1.5 et 1.6**, donc **Fallow l'a déjà** : la Lune Amie qui parle, les ruines des alchimistes, les outils ancestraux, les effigies, les pansements, les strates, les jarres anciennes **[V]**.
3. Le reste mélange des **mods publics** (Envelope, Tree Physics, Sable Ragdolls, Tolerable Creepers, Clutter No More, Supplementaries, Farmer's Delight, Exposure, Distant Horizons) et du **contenu maison** introuvable en ligne : âge du bronze, bas fourneau, santé max perdue à la mort et « éclats de vie », sédiment de faille, « anomalies », minerais denses, pas d'XP **[D]**.
4. Le youtubeur dit que la liste des mods est « publique », mais **je ne l'ai trouvée nulle part** ; elle est sans doute sur le Discord, qui demande un compte **[D]**. La transcription de la vidéo de GreenLikeable sur le SMP n'a pas pu être récupérée non plus.
5. À retenir pour Fallow : **Envelope** (le courrier par pigeons et les lettres perdues), **la quête de la Lune Amie déjà dans NML**, **une santé max qui baisse à la mort et qu'on regagne en explorant** (KubeJS), une **tannerie** (KubeJS) et, à étudier, **l'XP supprimée**.

## Ce qu'est Wayfarer

- **Qui le fait** : Monad Modding (rebaptisé « Monad Mods » le 2026-08-24). La page Patreon se présente comme « The Developers behind No Man's Land, Wayfarer & More ». L'équipe fait aussi Clutter No More, Tree Physics, Vanity, Tolerable Creepers, Glow, Mixed Litter et, depuis septembre 2026, Brewin' and Chewin' **[V]** ([Patreon](https://www.patreon.com/monad_modding), [annonce du nouveau nom](https://www.patreon.com/monad_modding/posts/fresh-coat-of-167571384), [équipe NML sur Modrinth](https://modrinth.com/mod/no-mans-land)). Sur Modrinth, l'équipe de NML compte Farcr (propriétaire) et tazer **[V]**.
- **Ce que c'est** : un modpack « fait main » autour de NML, qui touche aux visuels, à la progression et à la génération de toutes les dimensions **[V]** (description de [la vidéo 2](https://www.youtube.com/watch?v=WpwAmPJzb7A)). Le youtubeur parle d'environ 200 mods **[D]**. Le pack se met à jour tout seul : il suffit de relancer le jeu **[V]** (vidéo 1, 04:10).
- **Accès et prix** : réservé aux membres Patreon. Le rang « Wayfarer » coûtait 10 USD. Il a augmenté après l'ouverture du SMP, et le rang « Discoverer » a été créé pour ceux qui ne veulent que les préversions **[V]** ([billet du 2026-07-20](https://www.patreon.com/monad_modding/posts/wayfarer-rank-164357333)). Le youtubeur parle de « 20 $ en un seul paiement » **[V]**, alors que le billet parle d'un abonnement : **le prix actuel n'est pas confirmé**. Il dit aussi que le paiement ne durera que le temps du développement **[V]** (description de la vidéo 3). Au 2026-10-19, la campagne compte 793 membres, dont 195 payants **[V]** (API publique de Patreon).
- **État** : en développement actif, avec des mises à jour fréquentes **[V]**. Brewin' and Chewin' 5.0 était « déjà jouable sur le SMP Wayfarer » le 2026-09-12 **[V]** ([billet](https://www.patreon.com/monad_modding/posts/brewin-and-5-0-3-169338425)).
- **Le serveur** : pas de `/spawn`, distance de rendu d'environ 8 chunks, 10 à 15 joueurs connectés au moment de la visite, chat vocal en appuyer-pour-parler. Les joueurs ont bâti un village commun avec du courrier et une tyrolienne, et laissé des maisons abandonnées avec des mots (« nous sommes partis vers des terres plus riches, nous avons laissé nos ressources à ceux qui en ont besoin ») **[V]** (vidéo 1, 61:30 à 65:00). Le Discord Monad Modding compte 4 162 membres **[V]** (métadonnées publiques de [l'invitation](https://discord.gg/JW4utPut9x)).
- **Introuvable** : la liste des mods, l'adresse du serveur, des saisons ou des remises à zéro du monde, des règles écrites, une page wiki. Le serveur « Wayfarer SMP » de [minecraftservers.org](https://minecraftservers.org/server/689670) est un **homonyme sans rapport** (client vanilla, quêtes et métro).

## Tableau des fonctionnalités

Colonne « Disponible pour nous » : NeoForge 1.21.1 vérifié par l'API Modrinth le 2026-10-19 pour chaque mod cité.

### Monde et mystère (NML)

| Fonctionnalité | Source | Disponible pour nous | Comment l'intégrer | Avis |
|---|---|---|---|---|
| **Lune Amie** : une lune qui parle, des rêves, des « compagnons » (Buddy, Frienderman), un rituel d'ascension ; c'est la quête principale de la « Friendship Update » | NML 1.5 **[V]** ([billets](https://www.patreon.com/monad_modding/posts/nml-1-5-0-build-155392325), langues du jar 1.6.1) | **Déjà là** (NML 1.6.1). Dialogues traduits en français **[V]** | Trancher la décision en attente (conflit avec Enhanced Celestials, voir [le rapport 4](../verif-2026-10-18/4-monde-et-objectifs.md)) en faveur de la Lune Amie | **Oui**. C'est le fil énigmatique du pack et il est gratuit |
| **Ruines des alchimistes** (ruines ancestrales souterraines, « étrangement paisibles »), cartes des ruines | NML 1.5 **[V]** | **Déjà là** | Garder ; y glisser des fragments du codex des monolithes (LootJS, déjà prévu pour les ruines NML) | Oui |
| **Outils ancestraux** : pioche rituelle qui « résonne » pour écouter la terre, épée du serment pacifiste, ciseau et cloche en bronze ancien, avec dialogues | NML 1.6 **[V]** ([RC1](https://www.patreon.com/monad_modding/posts/no-mans-land-1-6-170725863)) | **Déjà là**. **47 textes de la 1.6 sans français** **[V]** (dont la pioche rituelle et l'épée) | Monter NML en 1.6.1, puis traduire dans `pack/kubejs/assets/nomansland/lang/fr_fr.json` (`npm run check-fr` les signalera) | Oui, à traduire |
| **Effigies de protection** : aucune créature hostile n'apparaît autour | NML **[V]** ([billet](https://www.patreon.com/monad_modding/posts/warding-effigies-125052343)) | **Déjà là** | Rien | Faible effet dans Fallow, qui a peu de monstres |
| **Pansements** (soin, antidote, effets de potion) avec animation | NML 1.5 **[V]** | **Déjà là**, en français | Rien | Oui, colle à la survie par l'environnement |
| **Strates rocheuses et filons rares** : granite, diorite et andésite rares mais en gros gisements | NML 1.5 **[V]** (classes `StrataStateProvider`, `OreVeinSystem` du jar) | **Déjà là** | Rien | Oui |
| **Jarres anciennes** qu'il faut casser, archéologie dans les ruines | NML 1.5 + vanilla **[V]** | **Déjà là** | Rien | Oui |
| Parhélies, cerfs (bois qui tombent), tortues, robinet à résine, bombes incendiaires, échafaudage en bois, chaume, lit de paille | NML **[V]** | **Déjà là** | Rien | Oui |
| Panneaux qui indiquent un village et sa distance (« 700 blocs par là ») | Introuvable (absent des fichiers de NML) **[D]** | Non | — | Non : les villages de Fallow sont des ruines |

### Progression et artisanat

| Fonctionnalité | Source | Disponible pour nous | Comment l'intégrer | Avis |
|---|---|---|---|---|
| **Âge de pierre** : pas d'outils en bois, cailloux et silex ramassés au sol, couteau en silex, pioche en os qui laboure aussi | Maison **[D]** (absent de NML et de Modrinth) | Équivalent : la taille de cailloux d'**Overgeared** | Déjà couvert par Overgeared | Rien à ajouter |
| **Âge du bronze** : étain (seulement au-dessus du niveau de la mer) et cuivre fondus au creuset en terre cuite, moules à lingot qui s'usent, outils en bronze équivalents au fer | Maison **[D]** | Équivalent : moules et fours d'alliage d'**Overgeared** | Déjà couvert | Rien à ajouter |
| **Fer au bas fourneau** (boue tassée, paille, cendre d'os, allume-feu), loupe cassée au marteau ; haut fourneau en briques « clinker » ; acier au coke | Maison **[D]** | Équivalent : haut fourneau et acier d'**Overgeared** | Déjà couvert | Rien à ajouter |
| **Minerais denses** qu'on ne casse qu'avec une foreuse mécanique (Create) | Maison **[D]** | Non | — | Non : ressemble à Create Ore Excavation, retiré par l'équipe |
| **Cinabre → mercure → mécanisme gyroscopique** (pièce des engins volants) | Recette maison sur Create Aeronautics **[D]** | Faisable | Recette KubeJS | Petite idée : relie la mine à l'aviation |
| **Cuir à tanner** : peau brute (vaches, cerfs) + cire de cochon, ou cendre + chair pourrie ; l'armure de cuir sert de base aux armures plaquées | Maison **[D]** (vidéo 1, 18:00 et 52:00) | Faisable | KubeJS : peau brute en butin, cuir au chaudron ou au tonneau | **Bonne idée légère** (recommandation 4) |
| **Papier à la main** (écorce → pâte au mortier → papier) ; une carte demande un nécessaire d'écriture (encre, plume, fiole) et une table de scribe | Maison **[D]** | Faisable | KubeJS sur la recette de l'atlas d'Antique Atlas | Possible, à garder en réserve |
| **Pas de barre d'XP** ni de progrès affichés ; l'enchantement existe mais demande des diamants | Maison ou réglage **[D]** (vidéo 1, 34:12 ; vidéo 2, 10:30) | Faisable | KubeJS (supprimer les orbes) + masquer la barre ; vérifier Immersive Enchanting, l'enclume et Envelope (adresse payée en niveaux, réglable) | **À étudier** (recommandation 5) |
| Marteaux qui minent en 3×3 | Introuvable **[D]** | — | — | Non |
| Cuisine Farmer's Delight (planche à découper, sandwichs, effets) | Farmer's Delight **[V]** (vidéo 1) | Oui | — | Non : la cuisine de Fallow est Kaleidoscope |
| Brewin' and Chewin' 5.0 (vin, fût de vieillissement) | Mod Monad **[V]** | `4.5.0` (la 5.0 est en préversion) | — | Non : extension de Farmer's Delight |
| Sacs qui ralentissent, bundles, corde, flèche-corde, lin → ficelle, panier-repas, souffleur de bulles | **Supplementaries** **[D]** (noms identiques) | **Déjà là** (décision 0006 du dépôt, `docs/decisions/0006-supplementaries.md`) | Rien | — |
| Appareil photo, pellicule noir et blanc, photo encadrée | **Exposure** **[D]** | **Déjà là** | Rien | — |
| Changer la forme d'un bloc avec Alt gauche (escalier, dalle, dalle verticale, marche) | [Clutter No More](https://modrinth.com/mod/clutternomore) (Monad) **[V]** | `3.0.2`, **français inclus** | Ajouter un mod | Confort ; fait doublon avec les dalles verticales de Quark. Plus tard |
| Table de style : skins d'armes et d'armures | [Vanity: Core](https://modrinth.com/mod/vanity-core) **[V]** | `5.0.3` | — | Non : cosmétique |
| Filet de sécurité, cadres d'échafaudage, luges, volets ; anneaux (« bague solaire ») ; grille de construction (touche C) | **Introuvable** | — | — | — |

### Corps, mort et créatures

| Fonctionnalité | Source | Disponible pour nous | Comment l'intégrer | Avis |
|---|---|---|---|---|
| **La santé max baisse à chaque mort**, jusqu'à 5 cœurs ; on la regagne avec des **éclats de vie** trouvés dans des **géodes de chair**, plus fréquentes sous les océans | Maison **[D]** (vidéo 1, 42:20 ; vidéo 2, 57:56 et 63:33 ; vidéo 3, 77:00 et 85:37) | Mod proche : [Unhealthy Dying](https://modrinth.com/mod/unhealthy-dying) `3.2.2` **[V]**, mais on y regagne la santé **en tuant**, et il n'a qu'anglais et russe | **KubeJS** : modificateur de santé max à la mort, objet « éclat » dans les géodes, les ruines NML, les clairières ou en bénédiction de monolithe | **Fort** (recommandation 3) : la mort compte, et l'exploration la répare |
| Le joueur laisse un **corps** qui garde son inventaire | Probablement [Sable: Ragdoll Corpse](https://modrinth.com/mod/sable-ragdoll-corpse) **[D]** | `0.3.0` alpha | — | Non : alpha, et proche de GraveStone retiré |
| Créatures en **ragdoll** à leur mort (il faut frapper une seconde fois pour le butin) | Probablement [Sable: Ragdolls](https://modrinth.com/mod/sable-ragdolls) **[D]** | `0.7.5` | — | Non : les ragdolls ont été jugés « hors ton » (chapitre 11) |
| **Arbres qui tombent** avec une vraie physique, flottent, et sur lesquels on peut marcher | [Tree Physics](https://modrinth.com/mod/tree-physics) (Monad, exige Sable) **[V]** | `neoforge-2.4`, anglais seul | Remplacerait HT's TreeChop | Non : très proche de Dragged's Simulated Tree Chopper (« c'est de trop ») |
| Creepers qui libèrent des « creepies » | [Tolerable Creepers](https://modrinth.com/mod/tolerable-creepers) (Monad) **[V]** | `2.2.4` | — | Non : aucun monstre vanilla dans Fallow |
| « **Anomalies** » : silhouettes qui copient le joueur et son équipement, très résistantes, qui lâchent du « vif-argent anomal » | Maison **[D]** (vidéo 3, 10:00 et 72:00) | Non | — | **Non** : le youtubeur les trouve « terrifiantes ». C'est de l'horreur |

### Nether (fermé dans Fallow)

| Fonctionnalité | Source | Disponible pour nous | Comment l'intégrer | Avis |
|---|---|---|---|---|
| Portail qu'on allume avec du « sédiment de faille » (lâché par les endermites) ; le sédiment enflammé montre le portail en ruine le plus proche | Maison **[D]** (vidéo 3, 52:54) | Non | — | Non : Nether fermé, et c'est une boussole à structure |
| Forteresses refaites, cristaux de glowstone, tornades de feu | [Glow](https://modrinth.com/mod/glows) (Monad) **[V]** pour les cristaux, reste introuvable | — | — | Non : Nether fermé |

### Vivre à plusieurs

| Fonctionnalité | Source | Disponible pour nous | Comment l'intégrer | Avis |
|---|---|---|---|---|
| **Courrier par pigeons** : lettres, colis, sceaux, adresses de boîtes aux lettres, paiement à la livraison ; des **lettres perdues** dans un relais postal effondré au-dessus des nuages, des pigeonniers | [Envelope](https://modrinth.com/mod/envelope) (mortuusars, l'auteur d'Exposure) **[V]**, retrouvé grâce aux lettres lues dans la vidéo 2 (19:48 à 25:00) | `0.7.5` (2026-08-15), GPL-3. **Pas de français** (anglais, japonais, russe, chinois) **[V]** | Ajouter un mod ; traduire environ 29 Ko de textes dans `pack/kubejs/assets/envelope/lang/fr_fr.json` ; régler le coût en niveaux d'une adresse et la vitesse des pigeons | **Recommandation 1** |
| Tyrolienne sur le serveur | Probablement [Ziplines: Rezipped](https://modrinth.com/mod/ziplines-rezipped) **[D]** | `1.5.3`, anglais et japonais | — | Moyen : le rail et les dirigeables couvrent déjà les liaisons |
| Mots et lettres laissés dans les maisons abandonnées | Joueurs du serveur **[V]** | Oui | Rien à installer : les tableaux d'affichage de Supplementaries ou les lettres d'Envelope suffisent | Oui, à encourager entre amis |
| Distant Horizons | **[V]** (vidéo 2, 29:37) | — | — | Non : déjà écarté |
| Chat vocal | **[V]** | — | — | Non : refusé (Simple Voice Chat) |
| Pack mis à jour tout seul au lancement | **[V]** (vidéo 1) | Oui | `packwiz-installer-bootstrap`, déjà prévu côté serveur ([06](../../06-technique-et-serveur.md)) ; côté client, le repo privé l'empêche | Note technique |
| Journées très longues (« la journée a duré une heure ») | **Introuvable** **[D]** (vidéo 1, 25:20 et 52:00) | — | Ecliptic Seasons règle déjà la durée du jour | À vérifier |
| Noms des joueurs affichés seulement quand c'est utile | [Subtle Name Tags](https://modrinth.com/mod/subtle-name-tags) (Monad) **[V]** ; **pas vu dans les vidéos** | `0.3` (2025) | Ajouter un mod client | Petit bonus : des silhouettes dans la brume plutôt que des étiquettes |

## Recommandations classées

1. **Envelope : le courrier par pigeons** (nouveau mod). Fallow décrit une poignée de joueurs qui finissent par vivre loin les uns des autres. Envelope leur donne un lien lent et physique, sans téléportation : un pigeon met du temps selon la distance, peut être chassé par un renard, et ne passe que par des boîtes qui ont un nom, pas des coordonnées. Il ajoute une espèce à Wildex et un échange d'objets entre colonies. Les lettres perdues du relais postal effondré racontent, par petits bouts, ceux qui vivaient là avant : de l'énigme sans noirceur. Il a un correctif pour Sable, donc une boîte aux lettres sur un dirigeable devrait marcher **[D]**. À faire : traduire en français, régler le coût d'une adresse en XP (`MAILBOX_ADDRESS_EXPERIENCE_LEVELS_COST`), décider si le relais au-dessus des nuages reste (il casse un peu le « presque vide ») et tester avec Sable et Aeronautics.
2. **Garder la quête de la Lune Amie de NML 1.6.1** (déjà là, aucun mod à ajouter). C'est le cœur de ce qui rend Wayfarer « énigmatique » : une lune qui parle, des rêves, des ruines paisibles et des outils au passé mystérieux. Il faut trancher la décision en attente face à Enhanced Celestials (par exemple, la lune blanche reste rare et la Lune Amie reste en rêve), traduire les 47 textes de la 1.6 et faire des ruines des alchimistes un lieu où l'on trouve des fragments du codex des monolithes.
3. **Une santé max qui baisse à la mort et qu'on regagne en explorant** (script KubeJS). La mort a enfin un prix sans combat de plus, et la réparer pousse à partir : des éclats de vie au fond des grottes ou des géodes sous la mer, dans les ruines NML, en bénédiction de monolithe, ou au calme d'une clairière. Pas de jauge nouvelle, seulement les cœurs. Mieux qu'Unhealthy Dying, où l'on regagne la santé en tuant. À régler : un plancher (5 cœurs comme Wayfarer), une perte d'un cœur par mort, puis un essai à deux.
4. **Une tannerie** (script KubeJS). Les vaches, les cerfs de NML et les chevaux donnent une peau brute. Le cuir demande de la cire, ou de la cendre et de la chair pourrie, et du temps. Le cuir devient un petit métier lié à la faune et à l'élevage, qui alimente le sac, la selle et les armures. Léger, en français par nos propres textes. À vérifier avant : les recettes de cuir d'Overgeared et de Traveler's Backpack.
5. **Supprimer l'XP** (KubeJS et masquage de la barre), à étudier. Fallow paie déjà l'enchantement en matériaux, donc la barre d'XP n'a presque plus de rôle. L'enlever retire une jauge, rend le monde moins « jeu vidéo » et donne plus de valeur au savoir trouvé (livres, plans). Risques à lister avant : l'enclume, Quark, Create, Immersive Enchanting, Envelope et les fermes à XP. Si le coût est trop grand, on garde l'XP et on écarte l'idée.

Écartées : les anomalies (horreur), les minerais denses (comme Create Ore Excavation), Tree Physics (comme Dragged's), les ragdolls et le corps (hors ton), tout le Nether (fermé), Tolerable Creepers (pas de monstres vanilla), Brewin' and Chewin' (Farmer's Delight), Distant Horizons et le chat vocal (déjà refusés).

## Sources

Vidéos de uncstatus (titre et chaîne vérifiés par oEmbed, description et transcription automatique en anglais récupérées) :
- Partie 1 : [This Mod Makes Minecraft Feel Brand New](https://www.youtube.com/watch?v=31qxa34zFz8), 67 min, publiée il y a environ 2 mois.
- Partie 2 : [This Modpack feels like Minecraft Premium](https://www.youtube.com/watch?v=WpwAmPJzb7A), 79 min, il y a environ 1 mois.
- Partie 3 : [This is My New Favorite Modpack of All Time](https://youtu.be/9PXN0emzmnI), 86 min, 2026-08-29. Pas de chapitres.

Autres :
- GreenLikeable, [This Amazing SMP is Reinventing Minecraft](https://www.youtube.com/watch?v=LQYShHl1448), 2026-08-17 : seule la description est lisible (YouTube a bloqué la transcription).
- Patreon Monad Modding : [page](https://www.patreon.com/monad_modding), [rang Wayfarer](https://www.patreon.com/monad_modding/posts/wayfarer-rank-164357333), [nouveau nom](https://www.patreon.com/monad_modding/posts/fresh-coat-of-167571384), [Brewin' and Chewin' 5.0](https://www.patreon.com/monad_modding/posts/brewin-and-5-0-3-169338425), [NML 1.6 RC1](https://www.patreon.com/monad_modding/posts/no-mans-land-1-6-170725863), [première préversion Wayfarer de NML 1.5](https://www.patreon.com/monad_modding/posts/no-mans-land-out-152709685), [effigies](https://www.patreon.com/monad_modding/posts/warding-effigies-125052343). Les billets réservés aux membres ne montrent que leur accroche.
- [No Man's Land sur Modrinth](https://modrinth.com/mod/no-mans-land) ; jar `nomansland-1.6.1.jar` ouvert pour vérifier les objets, les classes et les traductions.
- Mods vérifiés sur l'API Modrinth (NeoForge 1.21.1) : [Envelope](https://modrinth.com/mod/envelope) (jar ouvert : langues, réglages, structures), [Tree Physics](https://modrinth.com/mod/tree-physics), [Sable: Ragdolls](https://modrinth.com/mod/sable-ragdolls), [Sable: Ragdoll Corpse](https://modrinth.com/mod/sable-ragdoll-corpse), [Tolerable Creepers](https://modrinth.com/mod/tolerable-creepers), [Glow](https://modrinth.com/mod/glows), [Clutter No More](https://modrinth.com/mod/clutternomore), [Vanity: Core](https://modrinth.com/mod/vanity-core), [Brewin' and Chewin'](https://modrinth.com/mod/brewin-and-chewin), [Ziplines: Rezipped](https://modrinth.com/mod/ziplines-rezipped), [Unhealthy Dying](https://modrinth.com/mod/unhealthy-dying), [Subtle Name Tags](https://modrinth.com/mod/subtle-name-tags), [Overgeared](https://modrinth.com/mod/overgeared).
- Discord Monad Modding : seulement le nom, la description et le nombre de membres, via [l'invitation](https://discord.gg/JW4utPut9x). Le contenu demande un compte.
- Homonyme sans rapport : [Wayfarer SMP sur minecraftservers.org](https://minecraftservers.org/server/689670).
- Recherches sans résultat utile : Reddit (inaccessible), CurseForge (non consulté, seulement cité par les descriptions), « rift sediment », « flesh geode », « anomalous quicksilver », « sturdy mineral block ».

Brouillons, transcriptions et jars : `/tmp/travail`.
