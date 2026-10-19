# 0010 — Musique

- **Date** : 2026-10-19
- **Statut** : retenu (choix de l'équipe) ; 29 morceaux choisis à l'écoute par l'équipe, pas encore entendus en jeu

## Contexte
L'équipe veut de la musique « dans le style de ce que l'on construit, avec de belles transitions », sans perdre le silence. Le pack joue déjà la musique vanilla toutes les 10 à 20 minutes, avec trois morceaux de No Man's Land ; il manque des contextes (brume, clairière, saison, lune, dirigeable) et des transitions. Étude complète : [recherche musique](../plan/recherche/2026-10-19/musique.md).

## Options
| Critère | True Adaptive Music | Reactive Music | Music Moods + Ecliptic |
|---|---|---|---|
| Silence entre morceaux | Réglable par situation, avec part de hasard | 2 min au plus, codé en dur | Délais vanilla, réglables par biome et saison |
| Contextes | 42 prédicats, dont biome et tag, hauteur, boss, météo, et conditions de butin évaluées par le serveur | Évènements fixes, biome, dimension ; ni lune, ni saison, ni structure | Saison, moment, pluie, biome |
| Musique livrée | Aucune : on choisit | 61 MP3 celtiques imposés, 295 Mo | Aucune |
| Musique de NML et d'Ecliptic | Gardée par les nœuds `vanillaMusic` | Coupée | Gardée |
| Poids | 11,3 Mo | 294,8 Mo | 0,33 Mo |

## Décision
**True Adaptive Music 2.9.3**, côté client et serveur, avec le paquet `fallow` ([musique](../plan/musique.md)) :
- un arbre de 11 situations, des silences de 4 à 20 minutes, les morceaux joués jusqu'au bout ;
- 29 morceaux gardés par l'équipe à l'écoute : 12 déjà dans le jeu (vanilla et No Man's Land), 3 du pack de ressources *Supernova* (Modrinth, CC-BY-NC-SA 4.0, usage gratuit permis), 10 libres livrés dans `fallow-ambiance` avec leurs crédits (Scott Buckley et Kevin MacLeod en CC-BY 4.0, HoliznaCC0 en CC0), ramenés à −23 LUFS avec des fondus ;
- `musique.js` pose sur le joueur les états que le client ne voit pas (monument, lune, à bord) ; les saisons passent par la condition de butin d'Ecliptic ;
- un seul morceau à l'approche d'un monument, rien pendant les combats de boss ;
- paquet choisi au premier lancement par Default Options, musique à 40 %.

Repli si TAM pose problème : Music Moods pour les fondus, fichiers `background_music` d'Ecliptic, champ `music` des clairières (recherche, 2.5).

## Écartés
- Reactive Music : silence de 2 min au plus, 295 Mo de musiques celtiques imposées, coupe la musique de NML et d'Ecliptic, aucune condition de lune, de saison ni de structure.
- Music and Melody, The Immersive Music Mod : lourds (246 et 155 Mo), musiques sous droits, ni lune ni saison.
- Music Triggers : alpha, Serene Seasons seulement.
- Mini Music Tweaks, Simple Music Control et semblables : ils raccourcissent le silence.
