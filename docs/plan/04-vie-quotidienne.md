# 4. Vie quotidienne : eau, champs, troupeau, cuisine, réserves

Ce qui existe déjà (0002) : Kaleidoscope Cookery (seul système de cuisine), Farm & Charm (infrastructure de ferme), Ecliptic Seasons, KaleidoscopeCookery-Automation, Cold Sweat, Thirst Was Reclaimed. La décision 0002 laissait deux trous : un **élevage exigeant** sans Animal Husbandry et une **conservation** sans Spoiled. Ce chapitre les comble, et règle la cohabitation Farm & Charm / Kaleidoscope Cookery.

Détail, preuves et clés : [recherche/research-vie.md](recherche/research-vie.md).

## 4.1 La boucle de jeu

Eau sûre → champs saisonniers → fourrage → troupeau nourri, abrité, avec des couples → produits → cuisine KC → conservation → rations d'expédition (gourde, sac à provisions, sac de couchage) → exploration. L'hiver, ce qu'on n'a pas mis de côté manque.

## 4.2 Ce que les mods retenus font déjà

À ne pas racheter ailleurs (vérifié dans le code source) :
- **Kaleidoscope Cookery** fournit déjà les **tags de saison** (Serene Seasons, humidité d'Ecliptic) et de nutrition pour ses cultures, et un **plateau en bambou** qui sèche au soleil et humidifie sous la pluie. Il a ses propres schémas KubeJS : `bamboo_tray`, `chopping_board`, `millstone`, `pot`, `steamer`, `stockpot`.
- **Ecliptic Seasons** gère la durée du jour, la **reproduction saisonnière** (`EnableSeasonalBreed`), les **abeilles saisonnières** et la **pêche saisonnière**. Il expose à KubeJS `EclipticSeasonsApi` (`getSeason`, `getSubSeason`, `getSolarTerm`, `getAdjustedHumidity`…). Son `HeatStroke` fait doublon avec Cold Sweat : **à désactiver**.
- **Cold Sweat 2.4** expose à KubeJS `coldsweat.getTemperatureAt`, `createModifier`, `addModifier`… Il a une glacière et une chaudière, et une compat native avec Thirst Was Reclaimed, Supplementaries, Sable et Aeronautics.
- **Thirst Was Reclaimed** : quatre niveaux de pureté, maladie, purification au feu de camp et au four, **filtre à sable Create**.
- **Farm & Charm** : attention, sa **mangeoire met les animaux en amour automatiquement** : c'est la reproduction autonome que la décision 0002 refuse.

## 4.3 Mods proposés

| Mod | Rôle | Dispo 1.21.1 NeoForge Modrinth | Statut proposé |
|---|---|---|---|
| **Kaleidoscope Compat** | Fusionne les doublons KC ↔ Farm & Charm par tags `c:` ; rejoue les recettes de la marmite F&C dans le stockpot KC ; option qui coupe la marmite F&C. **C'est la condition de 0002, toute prête** | oui (2.9.7) | socle |
| **Cattlestrophic!** | Faim et vieillesse des animaux ; sauvages (peu de butin, pas de laisse) et domestiques (meurent de faim s'ils ne sont pas nourris) | oui (1.3.0) | socle, à tester |
| **Salt's Animal Farm** | Confort, abri, poids caché, maladie par négligence, panique. Aucun objet ajouté : l'étable de F&C et la paille deviennent du gameplay | oui (0.2) | socle, à tester longtemps (jeune) |
| **Animal Genders** | Un mâle et une femelle pour se reproduire ; seules les femelles pondent ou donnent du lait | oui (1.2.0) | recommandé |
| **Freeze It and Heat It** | Addon Cold Sweat : la nourriture gèle au froid ou se gâte à la chaleur, réparable à la glacière ou à la chaudière | oui (4.1.0) | socle, s'il charge avec Cold Sweat 2.4.3.1 |
| **Script « Garde-manger »** | Péremption lente en KubeJS (voir 4.5) | — | socle (script) |
| **Kaleidoscope Tavern** | Même équipe que KC : vigne, foulage, tonneaux à 7 niveaux de qualité, cave, vieillissement | oui (1.2.0) | recommandé, **à la place de Vinery** |
| **Thirst Canteen** | Gourdes pour Thirst Was Reclaimed | oui (3.7) | recommandé |
| **Tide** (+ Tide Extra Compatibility) | Pêche par espèce selon biome, profondeur, heure et météo ; appâts ; journal | oui (2.1.1) | recommandé |
| **Nutritional Balance** | Nutriments par tags, malnutrition et excès : la variété de KC devient utile | oui (7.0.3) | recommandé |
| **Hardcore Revival** | À terre au lieu de mort ; les coéquipiers réaniment | oui | recommandé |
| **Comforts** | Sacs de couchage et hamacs (déjà proposé en [2](02-exploration.md)) | oui | recommandé |
| **AppleSkin** | Aperçu faim et saturation (déjà proposé en [6](06-technique-et-serveur.md)) | oui | recommandé |

**Optionnels** :
- River Fishing : un vrai simulateur d'eau douce (canne assemblée, surpêche, pêche sous la glace), mais il ajoute un PNJ pêcheur. À comparer à Tide sur une branche ; n'en garder qu'un.
- Fisherman's Trap : nasse à appât.
- Butchery : carcasses, saignée, séchoir à peaux (sortie réglée sur vanilla).
- Aquatic Breeding : la seule vraie génétique trouvée en 1.21.1 (poissons tropicaux).
- Kaleidoscope Contraption : cuisine KC sur les engins Create, donc un wagon-restaurant (sur les dirigeables, à vérifier avec Sable).
- Kaleidoscope Orchard : arbres fruitiers exigeants, encore très jeune (0.0.2).
- Kaleidoscope Grilling : plus tard.
- Complicated Bees : génétique des abeilles, sans les espèces qui produisent du minerai.
- Nourished : alternative à Nutritional Balance.
- Sleep Quality ou Healthy Sleep : un seul des deux.
- Animal Weights : repli si Salt's s'avère instable.

**Écartés** :
- Let's Do Vinery : doublon de Kaleidoscope Tavern.
- Animania Reborn : CurseForge seulement, à surveiller.
- ShelfLife : GitHub seulement.
- SpoiledZ, Food Spoiling : pas de version 1.21.1.
- Diet : bloqué en 1.20.1 ; Spice of Life: Onion.
- Hourglass : pas de 1.21.1, et Ecliptic gère déjà la durée du jour.
- Create Wells, Just a Well : doublons du puits F&C.
- First Aid New : config lourde, en concurrence avec Hardcore Revival.
- Butchercraft (dépend d'Extra Delight), Barnyard Buddies (contenu), Animal Feeding Trough (reproduction autonome).
- Serene Seasons API Stub (Ecliptic Seasons Bridge) : il ferait voir les saisons d'Ecliptic à Cold Sweat sans script, mais il n'est que sur CurseForge.

## 4.4 Qui fait quoi

| Fonction | Mods | Ce qu'on coupe |
|---|---|---|
| Climat | Ecliptic + pont KubeJS vers Cold Sweat | `HeatStroke = false` |
| Eau | Thirst Was Reclaimed + Thirst Canteen + puits F&C ; filtre à sable Create à l'ère industrielle | — |
| Champs | KC + F&C + saisons et humidité d'Ecliptic | `enableRainGrowthEffect` réduit (cumul avec l'humidité d'Ecliptic) |
| Cuisine | KC seul (avec Kaleidoscope Compat) | Postes et plats de F&C |
| Boissons | Kaleidoscope Tavern | Vinery, cocktail Molotov |
| Élevage | Cattlestrophic + Salt's + Animal Genders + reproduction saisonnière d'Ecliptic + bâtiments F&C | Reproduction automatique par la mangeoire F&C ; mangeoire de Quark (déjà coupée) |
| Pêche | Tide (ou River Fishing après comparaison) | Pêche saisonnière d'Ecliptic à régler pour ne pas cumuler |
| Abeilles | Ecliptic (+ Complicated Bees en option) | — |
| Conservation | Freeze It and Heat It + Garde-manger (séchage KC, cave, glacière, tonneaux Tavern) | — |
| Nutrition | Nutritional Balance + AppleSkin | — |
| Santé et repos | Hardcore Revival + Comforts | — |

## 4.5 Le « Garde-manger » (script KubeJS)

Faute de mod fiable sur Modrinth, la péremption lente est écrite en KubeJS, sur le modèle de ShelfLife :
1. Tags `fallow:perishable/fast`, `fallow:perishable/medium`, `fallow:perishable/slow` et `fallow:preserved`.
2. La date de fabrication est arrondie **au jour** (`custom_data.fallow_day`), pour que les piles d'un même jour fusionnent.
3. Le calcul ne se fait qu'à l'ouverture d'un conteneur, au ramassage, et environ toutes les 30 s pour l'inventaire du joueur : aucun coût par tick.
4. Le lieu compte : température du conteneur via `coldsweat.getTemperatureAt` (cave froide ×0,3, glacière ×0,1), plus vite en été via `EclipticSeasonsApi`.
5. Un aliment périmé devient de la chair putréfiée ou du compost, qui retourne aux engrais de F&C.
6. Infobulle sobre (« frais », « à consommer bientôt », « avarié »), sans compte à rebours précis.
7. La transformation est récompensée : séché au plateau en bambou de KC (ciel ouvert, sans pluie, donc saisonnier), salé, mis en tonneau, l'aliment passe dans `fallow:preserved`.

**Mise en place progressive** : commencer par les seules **rations d'expédition**, puis étendre si la mécanique plaît.

## 4.6 Pont Ecliptic Seasons → Cold Sweat

Premier script du sous-projet 3 (décision 0002). Ecliptic expose la saison et le terme solaire, Cold Sweat expose la création de modificateurs de température. Le script lit le sous-terme une fois par minute de jeu et applique au joueur un modificateur saisonnier (par exemple −0,4 en plein hiver, +0,3 au cœur de l'été, en unités Cold Sweat à étalonner). Le brouillon est dans [kubejs-brouillons/server_scripts/saisons_cold_sweat.js](kubejs-brouillons/server_scripts/saisons_cold_sweat.js). Si le pont échoue en jeu, le repli reste Serene Seasons (intégration native avec Cold Sweat).

## 4.7 Réglages à appliquer

| Mod | Réglage | Raison |
|---|---|---|
| Ecliptic Seasons | `HeatStroke = false` ; `EnableSeasonalBreed = true` ; `EnableSeasonalFishing` à régler avec Tide | Pas de doublon avec Cold Sweat ; élevage saisonnier |
| Kaleidoscope Compat | `farm_and_charm_compat_enabled = true`, `cooking_pot_recipes_disabled = true` | KC seul système de cuisine (0002) |
| Farm & Charm | Recette de la mangeoire retirée, ou mise en amour annulée en KubeJS ; `enableRainGrowthEffect` réduit | Pas de reproduction autonome |
| Cattlestrophic | `acceptedFoodItems` avec l'orge, l'avoine, le maïs, le fourrage et le grain de F&C et le riz de KC ; JSON pour Untitled Duck Mod ; décider du statut des animaux déjà présents | Boucle champ → fourrage → troupeau |
| Salt's Animal Farm | Liste des mobs effrayants réécrite avec les prédateurs Naturalist et Alex's Mobs ; blocs moelleux : paille KC, sol d'étable F&C | Les hostiles vanilla n'existent plus |
| Animal Genders | Exclure les hamsters (génétique propre) ; vérifier le poulailler F&C | — |
| Freeze It and Heat It | Liste blanche alignée sur `fallow:perishable/*` ; les aliments séchés exemptés | Une seule logique de conservation |
| Kaleidoscope Tavern | Cocktail Molotov désactivé | Pas d'arme incendiaire |
| Thirst Was Reclaimed | `item-settings.toml` : boissons de KC et de Tavern ; eau du puits F&C « acceptable », pluie récoltée « purifiée » | Eau cohérente |
| Nutritional Balance | Vérifier qu'il déduit bien les nutriments des recettes KC ; taguer sinon | — |
| Complicated Bees (si retenu) | Espèces à minerais retirées | Équilibre Overgeared |

**Régénération naturelle** (décision 12 du [8.4](08-mise-en-place.md#84-décisions-à-valider-ensemble)) : défaut proposé, la **garder** (`naturalRegeneration true`). Hardcore Revival, la faim et la soif suffisent à rendre la survie exigeante sans transformer chaque blessure en corvée.

## 4.8 À vérifier dans les .jar

1. Freeze It and Heat It : versions de Cold Sweat acceptées, conteneurs pris en compte, clés.
2. Salt's Animal Farm : clés, animaux des autres mods, performance, cumul avec Cattlestrophic.
3. Cattlestrophic : identifiant de sa mangeoire, animaux préexistants, canards.
4. Animal Genders : poulailler F&C, exclusions.
5. Kaleidoscope Compat : liste des objets fusionnés, compatibilité avec KC 1.5.1 (ou passage en 1.6.0).
6. KC 1.5.1 : contient-il déjà les tags de saison, ou faut-il la 1.6.0 ?
7. Kaleidoscope Tavern : cave, Molotov, soif.
8. Tide ou River Fishing : doublons avec Hybrid Aquatic, PNJ pêcheur.
9. Thirst Canteen 3.7 avec TWR 3.0.6.
10. Ecliptic : `ForceCompatMode` avec F&C ; cumul de la reproduction saisonnière avec la mangeoire.
11. Puits F&C : pureté de l'eau pour TWR.
