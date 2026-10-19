# Décisions

Une fiche par décision tranchée : le contexte, les options comparées, le choix et ses conditions. Une fiche n'est pas réécrite après coup ; si une décision change, une nouvelle fiche la remplace et l'ancienne passe au statut « remplacée par NNNN ».

| N° | Sujet | Statut |
|---|---|---|
| [0001](0001-nml-vs-terralith.md) | Worldgen : No Man's Land vs Terralith + Streams Reflowing | NML retenu ; Terralith à tester en preset concurrent |
| [0002](0002-agriculture.md) | Agriculture & élevage | Farm & Charm, Ecliptic Seasons, KC-Automation retenus ; élevage et conservation en attente |
| [0003](0003-faune.md) | Faune | Naturalist, Alex's Mobs Continued, Hybrid Aquatic, Mowzie's Mobs et compagnons retenus |
| [0004](0004-structures.md) | Structures & villageois | Sparse Structures retenu, End supprimé, concept « survivants » |
| [0005](0005-quark.md) | Quark : l'essentiel uniquement | Config fournie |
| [0006](0006-supplementaries.md) | Supplementaries : l'essentiel uniquement | Config fournie |
| [0007](0007-ambiance-performance.md) | Ambiance & performance | Retenus ; Distant Horizons en attente |
| [0008](0008-enchantement.md) | Enchantement | Immersive Enchanting retenu ; Matrix de Quark en repli |
| [0009](0009-xp.md) | Retrait de l'expérience | Retenu ; module `xp` du mod `fallow` 0.2.0 |
| [0010](0010-musique.md) | Musique | True Adaptive Music et paquet `fallow` retenus ; Reactive Music écarté |

## Modèle

```markdown
# NNNN — Sujet

- **Date** : AAAA-MM-JJ
- **Statut** : retenu / en test sur `test/<sujet>` / en attente / remplacée par NNNN

## Contexte
Pourquoi la question se pose, ce que la vision exige.

## Options
| Critère | Option A | Option B |
|---|---|---|

## Décision
Le choix, et les conditions qui l'accompagnent (configs, scripts KubeJS, réglages).

## Écartés
- Mod : raison.
```
