# Feuille de route

État au 2026-10-10. Le projet est découpé en sous-projets, chacun avec son cycle spec → plan → implémentation (voir [conception/](conception/)).

| # | Sous-projet | État |
|---|---|---|
| 1 | Dépôt & outillage : packwiz, sync depuis l'instance, CI, release | ✅ Fait ([spec](conception/specs/2026-09-23-repo-outillage-design.md)) |
| 2 | Choix des systèmes : agriculture, faune, structures, ambiance, performance | 🟡 Décisions 0002 à 0007 prises ; plan complet en cours dans [plan/](plan/README.md) |
| 3 | Couche de règles : configs, scripts KubeJS, données datapack | ○ Seules les configs Quark et Supplementaries existent |
| 4 | Serveur : hébergeur, déploiement, sauvegardes | ○ Pas commencé |

## Avant la première release (`v0.1.0`)

- [ ] Test de lancement en jeu : création d'un monde sans crash avec le pack complet.
- [ ] Comparer `pack/config/quark-common.toml` à la config générée au premier lancement (les clés ont été déduites des classes de Quark 4.1, [décision 0005](decisions/0005-quark.md)).
- [ ] Passer `[Unreleased]` du changelog en `[0.1.0]` et poser le tag.

## Chantiers ouverts issus des décisions

### Règles et configs (sous-projet 3)
- [ ] **Pont Ecliptic Seasons → Cold Sweat** en KubeJS : température saisonnière ressentie. Repli : Serene Seasons ([0002](decisions/0002-agriculture.md)).
- [ ] **Farm & Charm cantonné à la ferme** : masquer ou fusionner ses stations de cuisine et ses ingrédients en double avec Kaleidoscope Cookery ([0002](decisions/0002-agriculture.md)).
- [ ] Verrouiller l'automatisation de KC-Automation derrière la progression ([0002](decisions/0002-agriculture.md)).
- [ ] **Supprimer les monstres vanilla hostiles** ; décider du sort des monuments océaniques, cabanes de sorcière et avant-postes ([0003](decisions/0003-faune.md), [0004](decisions/0004-structures.md)).
- [ ] **Table d'apparition par biome** : une seule version par espèce entre Naturalist et Alex's Mobs, retrait des créatures qui cassent la DA ([0003](decisions/0003-faune.md)).
- [ ] Friends & Foes : garder les pacifiques, désactiver les hostiles. Adorable Hamster Pets : désactiver le flair à diamants. Companions! : encadrer hostiles, armes et boss ([0003](decisions/0003-faune.md)).
- [ ] **Supprimer l'End** (dimension, portails, Elytra) et les strongholds ([0004](decisions/0004-structures.md)).
- [ ] **Villageois « survivants »** : villages en ruines, survivants rares à ramener, reproduction bloquée, échanges filtrés (pas de Mending, pas de contournement d'Overgeared) ([0004](decisions/0004-structures.md)).
- [ ] Subtle Effects : couper les effets superflus ([0007](decisions/0007-ambiance-performance.md)).

### Recherches en attente (sous-projet 2)
- [ ] Élevage exigeant (besoins, maladies, génétique) : alternative à Animal Husbandry ([0002](decisions/0002-agriculture.md)).
- [ ] Conservation des aliments : alternative à Spoiled ([0002](decisions/0002-agriculture.md)).
- [ ] Mod remplaçant les structures vanilla par des versions intégrant Create ; candidats : Create: Let The Adventure Begin, Create Aeronautics Structures, Create: Structures Arise, Create Rustic Structures ([0004](decisions/0004-structures.md)).
- [ ] Comparaison réelle NML vs Terralith + Streams Reflowing sur `test/terralith-vs-nml` ([0001](decisions/0001-nml-vs-terralith.md)).
- [ ] Distant Horizons, Separate Sable Render Distance, Particle Rain ([0007](decisions/0007-ambiance-performance.md)).
- [ ] Autres candidats « à tester » du [registre](mods.md#à-tester).

### Surveillance
- Sinytra Connector (bêta) porte Antique Atlas 4 et Surveyor, deux mods Fabric : à surveiller à chaque mise à jour.
- Steam 'n' Rails est un portage NeoForge non officiel (bêta).
