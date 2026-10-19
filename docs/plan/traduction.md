# Traduction française

Tout ce qui se lit en jeu est en français : objets, blocs, infobulles, messages, succès, écrans de configuration, livres et guides. Un ami de l'équipe ne lit pas l'anglais ; c'est un critère pour chaque mod ajouté.

## Comment ça marche

- **Langue par défaut** : `pack/config/defaultoptions/options.txt` contient `lang:fr_fr`. Default Options l'applique au premier lancement, quand `options.txt` n'existe pas encore.
- **Nos traductions** : `pack/kubejs/assets/<namespace>/lang/fr_fr.json`. KubeJS charge `kubejs/assets/` comme pack de ressources, au-dessus des mods. Minecraft fusionne les fichiers de langue clé par clé : on ne traduit que les clés absentes du `fr_fr.json` du mod, et on peut corriger une traduction du mod en redéfinissant sa clé.
- **Livres** : la version `fr_fr` suit les mêmes chemins que l'`en_us` du mod, sous `pack/kubejs/assets/<namespace>/`. Traduits : le dictionnaire animalier d'Alex's Mobs (91 pages), une page du guide des hamsters (les autres passent par les clés de langue), la chronique des saisons d'Ecliptic Seasons (9 fichiers). Le manuel d'Icy's Better Horses et le codex d'Ancient Remnants passent entièrement par les clés de langue.
- **Contrôle** : `npm run check-fr` (aussi lancé par la CI) lit les jars du pack, y compris les jars imbriqués. Il échoue si un texte anglais n'a pas de traduction française, si un fichier n'est pas un JSON valide, ou si une traduction perd une variable (`%s`, `%1$s`, `{0}`, `§x`).

## Ajouter un mod

1. Lancer `npm run check-fr` : il liste les namespaces dont des textes manquent.
2. Écrire les clés manquantes dans `pack/kubejs/assets/<namespace>/lang/fr_fr.json`, dans l'ordre de l'`en_us`.
3. Traduire ses livres s'il en a (`patchouli_books/…/en_us/`, `book/…/en_us/`).
4. Relancer `npm run check-fr` jusqu'à 0.

## Glossaire

Le même mot pour la même chose dans tout le pack. Les termes vanilla suivent le français officiel de Minecraft Java.

| Anglais | Français |
|---|---|
| backpack | sac à dos |
| blueprint | plan |
| smithing, forge | forge |
| quench | trempe |
| season | saison |
| thirst, purified water | soif, eau purifiée |
| bestiary | bestiaire |
| airship | dirigeable |
| contraption (Aeronautics, Simulated) | engin |
| stress, kinetic (Create) | stress, cinétique, comme la traduction officielle de Create |
| monolith, blessing | monolithe, bénédiction |

Style : phrases courtes, vouvoiement ou forme impersonnelle dans les infobulles et la configuration. Le tutoiement reste là où le mod l'employait déjà en français (guide des hamsters, messages, succès). Les noms propres de mods et les titres de musiques ne sont pas traduits.

## Ce qui reste en anglais ou à surveiller

- **Longwings** : 4 messages écrits en dur dans le code (« New Discovery! », « Check your handbook », « Your handbook has been updated! », « This isnt functional yet »). Seul un correctif du mod les traduirait.
- **Envelope** : les descriptions des options de configuration, le message « Mail Service does not operate in this dimension. » et les messages des commandes sont écrits en dur dans le code.
- **No Man's Land** : les dialogues de la Lune Amie sont dans `assets/nomansland/lang/nomansland/dialogue_pools/<pool>/fr_fr.json`, hors des fichiers de langue habituels. Nous fournissons une copie complète du pool `offering`, où il manquait 9 dialogues. `npm run check-fr` ne contrôle pas ces fichiers : à revoir à chaque montée de version de NML.
- **Ancient Remnants** : la police des titres du codex n'a pas de lettres accentuées. `pack/kubejs/assets/ancient_remnants/font/header.json` ajoute la police par défaut en repli : les lettres accentuées s'affichent, dans le style vanilla. À vérifier en jeu.
- **Quark** : sa traduction appelle le bois « blossom » « cerisier », comme le cerisier vanilla. Les deux bois portent donc le même nom.
- **Naturalist et Alex's Mobs** : le serpent à sonnettes de Naturalist garde ce nom ; Alex's Mobs dit « Crotale ».
- **Every Compat** : les noms de bois des autres mods (`wood_type.*`, `leaves_type.*`) sont définis dans le fichier `everycomp`, puisqu'aucun mod ne les fournit.
- Les pages des livres traduits peuvent se mettre en page un peu différemment, le français étant plus long. À vérifier en jeu.
