# 0002 — Agriculture & élevage

- **Date** : 2026-09-23
- **Statut** : Farm & Charm, Ecliptic Seasons et KaleidoscopeCookery-Automation retenus (en test sur `test/agriculture`) ; élevage et conservation en attente

## Fonctions attendues d'une ferme
Retenues : **A** transformation (chaînes de production), **B** élevage exigeant, **C** saisonnalité, **D** conservation, **F** automatisation Create tardive.
Non retenue : E, la spécialisation entre joueurs (pas un objectif en soi).

## A — Infrastructure agricole : Farm & Charm ✅
Kaleidoscope Cookery apporte des cultures, un moulin et un épouvantail, mais aucune infrastructure de ferme. Farm & Charm apporte engrais, sol fertilisé, arroseurs, silos, charrue, charrette, puits, poulailler, étable, et de nouvelles céréales.

**Condition** : Farm & Charm est cantonné à la ferme. Ses stations de cuisine (fourneau, marmite, poêle, rôtissoire) et ses ingrédients en double avec KC (tomate, laitue, farine, pâte) seront masqués ou fusionnés en KubeJS (sous-projet 3). KC reste le seul système de cuisine.

## B — Élevage : en attente
Animal Husbandry (besoins, maladies, génétique) correspond au besoin mais il est jeune (0.4.2). **Mécanique voulue, mod refusé** : Animal Husbandry inquiète (maturité). Chercher une alternative plus tard ; en attendant, élevage vanilla + infrastructure Farm & Charm.

## C — Saisons : Ecliptic Seasons ✅ (sous condition)
| | Serene Seasons | Ecliptic Seasons |
|---|---|---|
| Maturité | ★★★★★ | ★★★☆☆ (rc, 1.21.1 en maintenance) |
| Immersion | ★★★☆☆ | ★★★★★ (24 termes solaires, neige qui s'accumule et fond, durée du jour) |
| Cultures | Saison | Saison + humidité + serres |
| Cold Sweat | Intégration native | Aucune → pont KubeJS |

**Condition** : pont KubeJS Ecliptic → Cold Sweat (température saisonnière ressentie par le joueur). Faisabilité vérifiée : Ecliptic expose `EclipticSeasonsApi` (`getSeason`, `getSubSeason`, `getSolarTerm`), Cold Sweat permet d'enregistrer des modificateurs de température en KubeJS. Premier script du sous-projet 3. Si le pont échoue en jeu : retour à Serene Seasons.

## D — Conservation : en attente
Spoiled (piloté par données, glacière Cold Sweat compatible) proposé mais refusé : le mod inquiète. **Mécanique voulue** ; chercher une alternative plus tard.

## F — Automatisation : KaleidoscopeCookery-Automation ✅
Permet au bras mécanique de Create d'alimenter les stations de KC. Le verrouillage de l'automatisation derrière la progression relève du sous-projet 3. Create: Integrated Farming reste à évaluer plus tard.

## Écartés
- Let's Do Bakery : doublon de la pâte/farine de KC.
- Let's Do Meadow : ajoute des biomes, conflit probable avec No Man's Land.
- Graze, abreuvoirs automatiques : rendent l'élevage autonome, l'inverse de B.
- Vinery, Kaleidoscope Tavern : extensions à revoir une fois le socle validé.
