# 0008 — Enchantement

- **Date** : 2026-10-19
- **Statut** : retenu (choix de l'équipe) ; non testé en jeu

## Contexte
L'équipe veut que l'enchantement devienne une vraie étape du jeu, comme la forge avec Overgeared et la cuisine avec Kaleidoscope, plutôt qu'un simple coût en niveaux. Recherche complète : [enchantement.md](../plan/recherche/2026-10-19/enchantement.md).

## Options
| Critère | Immersive Enchanting | Matrix Enchanting (Quark) | Enchanting Overhauled |
|---|---|---|---|
| Boucle | Trouver des livres anciens, les ranger près de la table, payer chaque niveau en matériaux | Mini-jeu de pièces sur une grille | Autel réveillé par un tome trouvé |
| Lien au monde | Fort : livres dans le butin, coûts en matériaux du pack | Faible : rien à trouver | Moyen |
| Poids | 7,1 Mo + AlfinoLib | 0 (déjà dans Quark) | Gros rééquilibrage du combat |
| Risques | Jeune, un seul auteur, pas de plugin EMI | Aucun | Mixin sur la table de forgeron d'Overgeared, licence ARR |

## Décision
**Immersive Enchanting** 6.0.1 (+ AlfinoLib), avec :
- ses trois structures « bibliothèques anciennes » coupées (`pack/kubejs/data/immersiveenchanting/tags/worldgen/biome/`, tags vidés) : les livres viendront des ruines de No Man's Land et des monuments ;
- Raccommodage déjà désactivé par le mod (`"enabled": false` dans son fichier de coût) : il annulerait l'entretien des outils d'Overgeared ;
- une traduction française (`pack/kubejs/assets/immersiveenchanting/lang/fr_fr.json`), le mod n'ayant que l'anglais.

À faire ensuite (voir la recherche) : fichiers de coût pour les enchantements des autres mods du pack (sinon ils sont désactivés), livres dans le butin des ruines NML, un savoir par monument d'Ancient Remnants, des livres aux paliers de Wildex.

## Écartés
- Easy Magic : remplace la table d'enchantement vanilla dont dépend Immersive Enchanting.
- Matrix Enchanting de Quark : reste le repli si Immersive Enchanting déçoit en jeu.
- Enchanting Overhauled, Apothic Enchanting, Ars Nouveau, Iron's Spells, Occultism, Malum, Create: Enchantment Industry : trop RPG, trop lourds ou contraires au ton.
