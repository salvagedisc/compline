# Le pack de ressources `fallow-interface`

Demande de l'équipe du 2026-10-19 : adapter le ton de Kaleidoscope, Overgeared et Wildex à Dark Coffee GUI, qu'aucun pack ne couvre. Formats et couleurs lus dans les jars et les textures ; **rien n'a été vu en jeu.**

## Ce qu'il contient

`pack/resourcepacks/fallow-interface/`, 24 textures recolorées, mêmes tailles et même transparence que les originaux :
- **Overgeared** (14) : les 9 écrans (enclumes, fours, table d'empennage, établi à plans, taille de la pierre), la barre du mini-jeu de forge, le fond EMI de l'enclume et les 4 boutons de filtre du livre de recettes ;
- **Kaleidoscope** (8) : les fonds de recettes affichés dans EMI ; seules les cases et les flèches changent, les dessins des blocs restent ;
- **Wildex** (2) : le fond du livre (thème VINTAGE, par défaut) et le fond rouge des trophées.

Correspondance des couleurs, reprise de Dark Coffee (des valeurs, aucun pixel) : fond `#C6C6C6` → `#451D21`, contour → `#2A0614`, biseaux → liseré `#B18469`, intérieur des cases `#8B8B8B` → `#815648` (ombre `#693F35`), bord bas des cases → `#582E2B`, flèche de progression → `#F1BA75`. Sur le livre de Wildex, l'acier devient du laiton et le bois tire vers le brun ; les pages restent claires, parce que son texte est écrit en `#2B1A10` dans le code.

Les flammes, la barre jaune et les zones vertes du mini-jeu ne changent pas : elles doivent rester lisibles.

## Licences

Les trois mods permettent de publier des textures modifiées : Kaleidoscope en CC BY-NC-SA 4.0 (nos fichiers sous la même licence), Overgeared en MIT, Wildex en CC BY-NC 4.0. Le détail et l'avis MIT sont dans `CREDITS.md` du pack. Les clauses « NC » interdisent tout usage commercial : pas de monétisation de Fallow sur Modrinth tant que ces textures sont dans le pack. Le titulaire exact de la licence MIT d'Overgeared n'a pas pu être vérifié (aucun fichier LICENSE dans le jar) ; l'auteur déclaré est StirDrem.

## Ordre

Default Options l'active au premier lancement, après Dark Coffee et Modded Coffee et avant `fallow-ambiance`. Il n'a aucun fichier en commun avec eux. Il ne contient pas de shader : il remplacerait celui de Dark Coffee, qui recolore les titres.

## Limites et tests en jeu

1. **Titres des écrans.** Ils sont écrits en `0x404040` par le code. Le shader de texte de Dark Coffee les passe en `#B08569`, mais il cherche une valeur un peu différente (0,248 contre 0,251) ; s'il les manque, les titres sont illisibles sur le fond brun, sur les écrans vanilla comme sur ceux d'Overgeared. Ouvrir un four suffit à le voir.
2. **Texte de Kaleidoscope dans EMI** : `#555555` sur le fond brun, peu lisible. Aucune texture ne le corrige.
3. **Wildex** : sa palette et ses remplissages sont écrits dans le code ; seul le fond change. Autre piste sans texture : `designStyle = "STEAMPUNK"` dans sa config client, un thème déjà en laiton et brun.
4. À chaque mise à jour de ces mods, comparer leurs textures d'interface aux nôtres : une texture qui change de taille ou de disposition doit être refaite.
