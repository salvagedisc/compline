# Recherche — pilier « Vie quotidienne » & survie environnementale (Fallow, 1.21.1 NeoForge)

> Rapport ajusté au registre à jour (46 mods, décisions 0002–0007) : Farm & Charm, Ecliptic Seasons, KC-Automation **déjà retenus** ; Animal Husbandry, Spoiled, Let's Do Bakery/Meadow, Graze **écartés** (non reproposés).
> Priorités traitées en premier : **élevage exigeant** (remplaçant d'Animal Husbandry) et **conservation** (remplaçant de Spoiled).

## 0. Méthode et fiabilité des preuves

- modrinth.com / api.modrinth.com / curseforge / moddex / modpackindex / mado-hosting : **injoignables** depuis la session (proxy 403 / DNS). Le quota WebSearch partagé (200) a été **épuisé en cours de recherche**.
- Disponibilité Modrinth vérifiée par deux sources indirectes mais fiables :
  1. **Miroir de l'API Modrinth** `qacow37/prismnix` sur GitHub (`packages/projects/<id>.nix` : slug → id Modrinth → clés `neoforge-1.21.1` + nom du .jar). Instantané **un peu daté** (ex. KC y est en 1.4.1 alors que le pack a 1.5.1) : une absence peut signifier « sortie plus récente », une présence est une preuve.
  2. Fichiers **packwiz** d'autres modpacks sur GitHub pointant vers `cdn.modrinth.com/data/<id>/…neoforge…1.21.1…`.
- Descriptions lues via le miroir de contenu Modrinth `Voxelum/xmcl-community-content-i18n-extra` (traductions de la page Modrinth) et via le code source GitHub quand il existe (KC, Tavern, Ecliptic, Cold Sweat, TWR, F&C, Kaleidoscope Compat, Nutritional Balance, Nourished, First Aid New).
- Notation : **MR✔** = 1.21.1 NeoForge présent sur Modrinth (id + fichier) · **MR?** = non vérifié · **CF seul** = CurseForge uniquement (bloquant).

---

## 1. Ce que les mods déjà retenus font DÉJÀ (à ne pas racheter ailleurs)

| Mod retenu | Mécaniques utiles au pilier (vérifiées dans le code) |
|---|---|
| **Kaleidoscope Cookery** (1.5.1 dans le pack, 1.6.0 sur GitHub) | Cultures : tomate, laitue, piment, riz (+ riz sauvage), théier. Postes : fourneau, poêle/wok, marmite (stockpot), cuiseur vapeur, meule, planche à découper, broche à shawarma, théière, **plateau en bambou (séchage au soleil / humidification sous la pluie)**, bassin émaillé, chaîne de piments et champignons enfilés (décor de séchage). **Fournit déjà les tags Serene Seasons, Ecliptic Seasons (humidité) et Diet** pour ses cultures. Schémas KubeJS natifs : `bamboo_tray`, `chopping_board`, `millstone`, `pot`, `steamer`, `stockpot`. Compat native : Create, Farmer's Delight, Harvest with Ease, EveryComp, Jade, Ponder, Sable (tags d'entités). **Aucune** mécanique de conservation/glacière. |
| **Ecliptic Seasons** | 24 termes solaires, **durée du jour dynamique** (`DynamicDaylightDuration`), neige qui s'accumule/fond, météo par terme, cultures saison + **humidité**, serres, humidificateur, « Season Core », rituel de prière agricole, **reproduction saisonnière** (`EnableSeasonalBreed`, `EnableTimeBreed`), **abeilles saisonnières** (`EnableSeasonalBee`, `BeePollinateSeasons`, `BeeActiveSeasons`), **pêche saisonnière** (`EnableSeasonalFishing`, `FishingSeasons`, `LessFishInThunder`), `HeatStroke` (coup de chaleur, **doublon de Cold Sweat → désactiver**), `RestrictBoneMeal`. Lit la plupart des **tags Serene Seasons**. Plugin KubeJS natif : bindings `EclipticSeasonsApi` (getSeason, getSubSeason, getSolarTerm, isRainAt, getAdjustedHumidity, getSpecialDays…) et `EclipticSeasonsBindings`. |
| **Farm & Charm** | Engrais, sol/terre fertilisés, charrue, semoir, fourche, épouvantail (croissance), arroseur, puits en bois (`timber_well`, nappe + pluie), silos, charrette, **mangeoire** (`feeding_trough`), **abreuvoir**, nid et **poulailler**, fourrage cheval, grain à poules, nourriture chien/chat + gamelle, cultures orge/avoine/maïs/fraise/oignon (+ tomate, laitue en doublon KC), plantes sauvages. ⚠ La mangeoire **met les animaux en amour automatiquement** (`ApproachFeedingTroughGoal` → `setInLove`) = reproduction autonome (contraire à la fonction B). Clés de config NeoForge : `enableRainGrowthEffect`, `rainGrowthMultiplier`, `bigCropChance`, `enableBonemealEffect`, `fertilizedSoilRange`, `scarecrowGrowthInterval`, `scarecrowRange`, `waterSprinklerRange`, `wellGroundwaterDepth`, `wellRainFillChance`, `feedingTroughRange`, `enableChickenEffects`, `chickenCoopMaxChickens`, `chickenCoopMaxEggs`, `enableHorseTaming/Effects`, feeding times. |
| **Thirst Was Reclaimed** (modid `thirst`) | 4 niveaux de pureté (source stagnante = sale, eau courante +1, altitude >100 / grottes <48 = « légèrement sale »), maladie (nausée + faim, poison possible), purification feu de camp +1 / four +2, **filtre à sable Create** (+1 par passage), soif modulée par température/humidité du biome et **par Cold Sweat** si présent, bols en terre cuite, `item-settings.toml` / `keyword.toml` pour ajouter des boissons. |
| **Cold Sweat 2.4** | Compat native avec `thirst` (TWR), `spoiled`, `sereneseasons`, `supplementaries`, `sable`, `create_aeronautics`, Create. Bindings KubeJS `coldsweat.*` : `getTemperatureAt(level,pos)`, `getBiomeTemperature`, `getBlockTemperature`, `createModifier`, `addModifier`, `getTemperature(entity,trait)`… (utile pour le pont saisons ET pour une conservation « cave froide »). Blocs **Chaudière (boiler)** et **Glacière (icebox)**. |

---

## 2. Tableau de synthèse des candidats

| # | Mod | Slug / id Modrinth | MR 1.21.1 NF | Domaine | Verdict |
|---|---|---|---|---|---|
| 1 | **Salt's Animal Farm** | `salts-animal-farm` / X2m1QNPV | ✔ 0.2 | Élevage (confort, poids, maladie) | **Socle** (à tester) |
| 2 | **Cattlestrophic!** | `cattlestrophic!` / xbg5Ic9n | ✔ 1.3.0 | Élevage (faim, vieillesse, sauvage/domestique) | **Socle** (à tester) |
| 3 | **Animal Genders** | `animal-genders` / qONca06E | ✔ 1.2.0 | Élevage (sexes) | **Recommandé** |
| 4 | Animal Weights | `animal-weight` / L8hKhAzB | ✔ 1.1.0 | Élevage (poids) | Repli de #1 |
| 5 | Butchery | `butchery` / 6YfOmC9t | ✔ 5.2 | Produits animaux (carcasse, peaux) | Optionnel |
| 6 | Aquatic Breeding | `aquatic-breeding` / g326EYcN | ✔ 1.0.2 | Pisciculture, génétique poissons tropicaux | Optionnel |
| 7 | Backported Animal Variants | `backported-animal-variants` / pyFjCT3F | ✔ 0.2 | Variantes biome (génétique visuelle) | Optionnel |
| 8 | Animania Reborn | — | **CF seul** | Élevage complet | Écarté (Modrinth) — à surveiller |
| 9 | [BUB] Gender | — | MR? (absent du miroir) | Sexes + stérilité | Écarté sauf si Modrinth |
| 10 | Barnyard Buddies / Butchercraft | 3lBMOcwT / MT2Xm0ih | ✔ | Races (contenu) / boucherie (dépend d'Extra Delight) | Écartés |
| 11 | **Freeze It and Heat It (FIAHI)** | `freeze-it-and-heat-it` / 6jlFKPwR | ✔ 4.1.0 | Conservation (addon Cold Sweat) | **Socle** (sous condition) |
| 12 | **Système KubeJS « Garde-manger »** | — | — | Conservation (péremption, cave, séchage) | **Socle** (script) |
| 13 | ShelfLife / SpoiledZ / Food Spoiling | — | ✘ | Péremption | Écartés (pas sur Modrinth / pas 1.21.1) |
| 14 | **Kaleidoscope Compat** | `kaleidoscope-compat` / Xm1eP54z | ✔ 2.9.7 | Fusion KC ↔ F&C/Vinery/Tavern | **Socle** |
| 15 | **Kaleidoscope Tavern** | `kaleidoscopetavern` / r9RZvhiJ | ✔ 1.2.0 | Vigne, brasserie, cave, vieillissement | **Recommandé** (remplace Vinery) |
| 16 | Kaleidoscope Contraption | `kaleidoscope-contraption` / JCCAvv9N | ✔ 1.0.3 | Cuisine KC sur engins Create | Optionnel (expéditions) |
| 17 | Kaleidoscope Orchard | `kaleidoscope-orchard` / UQJIT5UV | ✔ 0.0.2 | Arbres fruitiers (eau + sol aux racines) | Optionnel / à surveiller |
| 18 | Kaleidoscope Grilling | `kaleidoscope-grilling` / LPZkep5R | ✔ 1.1.1 | Huile, grillades, nouvelles cultures | Optionnel (plus tard) |
| 19 | Let's Do Vinery | `lets-do-vinery` / 1DWmBJVA | ✔ 1.5.3 | Vin | Écarté (doublon Tavern) |
| 20 | **Tide** (+ Tide Extra Compatibility) | `tide` / die1AF7i (+ goMfyYSL) | ✔ 2.1.1 (+2.3.0) | Pêche | **Recommandé** |
| 21 | **River Fishing** | `river-fishing` / 5acJO3EU | ✔ 0.9.0 | Pêche d'eau douce « simulateur » | **À comparer à Tide** |
| 22 | Fisherman's Trap | `fishermens-trap` / ltRggnDx | ✔ 3.1.0 | Nasse à appât | Optionnel |
| 23 | **Thirst Canteen** | `thirst-canteen` / GV0PyXSv | ✔ 3.7 | Gourdes TWR | **Recommandé** |
| 24 | Create Wells / Just a Well | PNcaol17 / qs7zlvyh | ✔ | Puits | Écartés (doublon F&C, empilement Create) |
| 25 | **Nutritional Balance** | `nutritional-balance` / OECzAr94 | ✔ 1.21.1-7.0.3 | Nutrition par nutriments | **Recommandé** |
| 26 | Nourished (+ MariesLib) | `nourished` / gXy6YNys | ✔ 0.2.7-beta | Nutrition 6 groupes + « santé intestinale » | Optionnel (alternative à #25) |
| 27 | Spice of Life: Onion / Diet | eHGYGKJz / — | ✔ / ✘ (Diet bloqué en 1.20.1) | Variété | Écartés |
| 28 | AppleSkin | `appleskin` / EsAfCjCV | ✔ 3.0.9 | Infobulles (TWR l'utilise) | Recommandé (QoL) |
| 29 | **Comforts** | `comforts` / SaCpeal4 | ✔ 9.0.5 | Sacs de couchage, hamacs | **Recommandé** |
| 30 | Sleep Quality | `sleepquality` / S8fvNvSw | ✔ 2.0.1 | Qualité du sommeil (toit, lumière, météo) | Optionnel |
| 31 | Healthy Sleep | `healthy-sleep` / 3aMt7SZp | ✔ (v4) | Score de sommeil, soin au repos | Optionnel (alternative à #30) |
| 32 | Steve's Realistic Sleep / Campfire Resting | ZPywkPEo / k9O8NrbO | ✔ | Temps accéléré au lit / repos au feu | Optionnels (à tester avec Ecliptic) |
| 33 | Hourglass / Day Length | 1ZqmoFFP / MrMHdkQ6 | ✘ / ✔ | Durée du jour | Écartés (Ecliptic le fait) |
| 34 | **Hardcore Revival** | `hardcore-revival` / HqKoXaXz | ✔ 21.1.x | Mise à terre / réanimation coop | **Recommandé** |
| 35 | First Aid New | `first-aid-new` / yNbhkSj2 | ✔ 1.3.1 | Santé localisée | Optionnel (config lourde) |
| 36 | Complicated Bees | `complicated-bees` / uhqEKIQS | ✔ 3.6.1 | Apiculture génétique diploïde | Optionnel (gros morceau) |
| 37 | Realistic Bees / Buzzier Bees / Productive Bees | tG6HkcWx / b7vOFSIp / jH6iiqkd | ✔ | Abeilles | Optionnel / écarté / écarté |
| 38 | Seasonal Let's Do / Seasonal Integration | yFIPn94r / w3gipv9f | ✔ | Tags saison F&C | Inspiration (recopier les tags) |
| 39 | Serene Seasons (repli) | `serene-seasons` / e0bNACJD | ✔ 10.1.0.x | Saisons | Repli (décision 0002) |

---

## 3. Fiches détaillées

### 3.1 ÉLEVAGE EXIGEANT (priorité 1) — remplacer Animal Husbandry par une **combinaison de trois petits mods ciblés**

Aucun mod unique fiable sur Modrinth 1.21.1 NeoForge ne couvre besoins + maladies + génétique (Animal Husbandry 0.4.x écarté ; Animania Reborn = CurseForge seul, 26 commits, 0 étoile ; Genetic Animals / Realistic Horse Genetics / Animal Genetics = pas de 1.21.1). Proposition : **découper la fonction** en couches indépendantes, chacune remplaçable.

#### Salt's Animal Farm — `salts-animal-farm` (X2m1QNPV) — MR✔ `salts_animal_farm-1.21.1-NeoForge-0.2.jar`
- Preuve : miroir Modrinth (clé `neoforge-1.21.1`) ; description Modrinth (miroir xmcl). Côté : both (logique serveur). Dépendances : aucune déclarée.
- **Mécanique** (aucun objet ajouté) : vaches, champimeuh, cochons, moutons, poules, lapins recherchent jour et nuit **ombre/soleil, eau proche, espace sans surpopulation, congénères, lumière la nuit, couchage moelleux, vue sur le ciel étoilé**. **Poids** caché : confort → prise de poids → plus de butin ; stress/négligence → perte → **maladie** (lenteur, plus de reproduction, plus de production) jusqu'à guérison. **Pluie** : les animaux cherchent un abri, rester sous la pluie fait maigrir → l'étable sert réellement. **Peur/panique** face aux prédateurs, aux coups, au spectacle d'un abattage (difficulté haute). Préréglages Facile/Normal/Difficile ; configurables : animaux concernés, mobs effrayants, blocs « couchage moelleux », comportement sous la pluie, délais, bornes de poids. Poids lisible au clic droit main vide.
- Intégration : fait de l'**architecture de ferme** un gameplay (étable F&C `stable_floor`/`straw_stable_floor`, abreuvoir, paille KC `StrawBlocks`) ; la pluie saisonnière d'Ecliptic rend l'abri vital en automne ; prédateurs Naturalist/Alex's Mobs = « mobs effrayants » (les hostiles vanilla sont supprimés, il faut **réécrire la liste**).
- Conflits : chevauche Animal Weights (ne garder qu'un des deux). Jeune (0.2) → test long serveur (perf des scans de confort).
- KubeJS/datapack : tag de blocs « soft » = paille KC + sols d'étable F&C + `wheat_pile` ; loot tables multipliées par le poids → vérifier l'interaction avec Butchery (carcasses).
- **Verdict : socle (à tester)** — c'est exactement « transformer l'élevage en métier » sans ajouter de contenu.

#### Cattlestrophic! — `cattlestrophic!` (xbg5Ic9n) — MR✔ `cattlestrophic-1.3.0.jar` (licence MIT)
- Mécanique : **faim et vieillesse**. Animaux nés d'œuf/spawn = **sauvages** (vieillissent, ne meurent pas de faim, moins de butin, ne se tiennent pas en laisse, reproduction moins sûre et plus lente) ; nés d'un élevage = **domestiques** (vivent plus longtemps, plus de butin, **meurent de faim s'ils ne sont pas nourris**). Nourrissage à la main (« nourriture animale ») ou **mangeoire** (rayon, quantité, nb max d'animaux). Particules quand âge >75/90 % ou faim <20 %. Outil d'inspection (créatif).
- Clés : `enableMod`, `enableAnimalSettings`, `hungerDepletionRate`, `agingRate`, `showHungerIndicators`, `showAgeIndicators`, `feedingTroughRange`, `feedingAmount`, `maxAnimalsPerTrough`, `breedingCooldownTicks`, `growthTimeTicks`, `wildMaxAge`, `domesticatedMaxAge`, `preventLeashingWildAnimals`, `wildAnimalBreedChance`, `wildBreedingExtraCooldownTicks`, `wildAnimalLootPenaltyMultiplier`, `domesticatedAnimalLootBonusMultiplier` ; par animal : `config/cattlestrophic_animal_settings/<modid>/<entity>.json` (`acceptedFoodItems`, `hungerDepletionRateOverride`, `wild/domesticatedMaxAgeOverride`, `breedingCooldownTicksOverride`, `growthTimeTicksOverride`, `feedingAmountOverride`). Outil tiers : générateur de JSON (GitHub Qdeeme/Cattlestrophic_json_creator).
- Intégration : boucle **agriculture → fourrage → élevage** (`acceptedFoodItems` = orge/avoine F&C, `horse_fodder`, `chicken_feed`, paille). Fichiers JSON pour les canards/oies d'Untitled Duck Mod et les animaux Naturalist qu'on veut élever.
- Conflits : la mangeoire F&C (reproduction auto) — voir §5. `preventLeashingWildAnimals=true` impose une vraie domestication (capturer/attirer deux sauvages, les faire se reproduire).
- **Verdict : socle (à tester)** — apporte la contrainte « il faut nourrir » qui manquait à Salt's.

#### Animal Genders — `animal-genders` (qONca06E) — MR✔ `animalgenders-1.21.1-neoforge-1.2.0.jar` — GitHub acorsicanfrog/animalgenders
- Mécanique : sexe mâle/femelle visible ; **reproduction = couple mâle + femelle** ; **seules les femelles pondent / se traient** (poules, vaches, chèvres). S'applique à toute sous-classe d'`Animal` (donc aux animaux moddés). Config : liste d'entités, ratio mâle/femelle au spawn naturel. Licence : crédit demandé.
- Intégration : rend la gestion du troupeau réelle (garder un mâle, sélectionner les femelles productrices), lisible avec Jade.
- Conflits : poulailler F&C (poules stockées dans le bloc ?) → vérifier dans le .jar que la ponte du poulailler respecte le sexe ; Adorable Hamster Pets a sa propre génétique (l'exclure de la liste).
- **Verdict : recommandé.** ([BUB] Gender est plus riche — stérilité, restrictions d'objets — mais absent du miroir Modrinth : à n'utiliser que s'il est confirmé sur Modrinth.)

#### Animal Weights — `animal-weight` (L8hKhAzB) — MR✔ 1.1.0
- Poids 1–8 selon 4 critères (lumière ≥14, eau à 4 blocs, herbe/mousse, ≤7 congénères en 5×5) ; poids 0 = malade (vert, plus de reproduction) ; **butin et XP multipliés par le poids** (trop généreux par défaut). Commande `/animalweights`.
- **Verdict : repli** si Salt's Animal Farm se révèle instable (plus simple, moins riche, pas de pluie/peur).

#### Butchery — `butchery` (6YfOmC9t) — MR✔ `butchery-5.2-neoforge-1.21.1.jar` (ARR)
- Mécanique : tuer au couteau de boucher → **carcasse** ; crochet (saignée), **séchoir à peaux (mouiller + saler → cuir)**, table de boucher (découpe par morceaux, sortie vanilla ou Farmer's Delight configurable), gril sur feu de camp, hachoir. Addon Alex's Mobs séparé.
- Intégration : « métier de boucher », cuir rare et travaillé (cohérent avec Overgeared), viande pour KC.
- Conflits : ajoute des morceaux de viande → **régler la sortie sur vanilla** (pas FD) et taguer `c:foods/raw_meat` pour KC ; source de sel à vérifier.
- **Verdict : optionnel** (bonne mécanique, mais contenu supplémentaire à aligner sur KC).

#### Aquatic Breeding — `aquatic-breeding` (g326EYcN) — MR✔ `fisheggs-1.0.2+neoforge.jar`
- Poissons attirés par de la **farine de poisson** → œufs → 1–3 alevins ; **poissons tropicaux héritent couleurs/motifs avec mutations (3072 variantes)** ; calmars chassent des proies vivantes et se reproduisent ; tentacules comestibles.
- **Verdict : optionnel** — la seule vraie « génétique » trouvée en 1.21.1, appliquée à la pisciculture (bassins de colonie).

#### Backported Animal Variants — `backported-animal-variants` (pyFjCT3F) — MR✔ 0.2
- Variantes de biome des vaches/cochons/poules de 1.21.5 (froid/tempéré/chaud). Héritage par les parents : **non vérifié**. **Verdict : optionnel** (génétique « visuelle » légère).

#### Écartés en élevage
- **Animania Reborn** (GitHub Steveaaaaa/Animania-Reborn, NeoForge 1.21.1, races, besoins, humeur, fromage, véhicules) : **CurseForge seul**, très jeune → écarté, à surveiller.
- **Butchercraft** : recettes via Extra Delight (famille Farmer's Delight). **Barnyard Buddies** : races = contenu. **Hungry Cows** : Fabric seul. **BreedingControl** : redondant avec KubeJS. **Animal Feeding Trough** : reproduction autonome.

### 3.2 CONSERVATION (priorité 2) — remplacer Spoiled

État du marché 1.21.1 NeoForge : **Spoiled** (écarté) ; **ShelfLife** (xiaofeng96301/ShelfLife — excellent design événementiel piloté par datapack, glacière, température/humidité) mais **GitHub seul, 9 commits, pas sur Modrinth** ; **SpoiledZ**, **Food Spoiling**, **Spoilage Enhanced** : pas de 1.21.1 NeoForge. Proposition en deux étages.

#### Freeze It and Heat It (FIAHI) — `freeze-it-and-heat-it` (6jlFKPwR) — MR✔ `fiahi-4.1.0.jar` (clés `neoforge-1.21` et `neoforge-1.21.1`, GPL-3.0) — dépendance : **Cold Sweat**
- Mécanique : la nourriture (inventaire **et conteneurs**) **gèle** au froid ou **se gâte** à la chaleur ; on la ramène à l'état normal avec la **chaudière** ou la **glacière** de Cold Sweat, sinon manger un aliment altéré donne des effets négatifs. **Sac à provisions** qui ralentit gel et altération. Config : activer/désactiver gel et altération, vitesses, **liste blanche** d'aliments exclus. Fait pour le modpack *The Winter Frontier*. Développement actif (~25 versions).
- Intégration : lie conservation, **température Cold Sweat** et **saisons Ecliptic** (l'été gâte, l'hiver gèle les rations d'expédition) ; la glacière devient le « frigo » de la colonie.
- Conflits/limites : pas une péremption dans le temps (c'est un état thermique réversible) ; compatibilité exacte avec Cold Sweat 2.4.3.1 **à vérifier** dans `neoforge.mods.toml`.
- KubeJS : liste blanche = aliments « conservés » (séchés, salés, bocaux, bouteilles Tavern) → récompense la transformation.
- **Verdict : socle sous condition** (s'il charge avec Cold Sweat 2.4 ; sinon passer au script seul).

#### Système KubeJS « Garde-manger » (péremption lente, cave, séchage) — **socle (script maison)**
Inspiré de l'architecture de ShelfLife (évite le coût d'un tick par objet) :
1. **Tags** : `fallow:perishable/fast` (viande et poisson crus, lait), `…/medium` (fruits, légumes, plats KC), `…/slow` (pain, fromage) ; `fallow:preserved` (séchés au plateau en bambou KC, salés, fumés, bocaux, bouteilles Tavern).
2. **Horodatage par jour de jeu** : `custom_data.fallow_day = floor(gameTime/24000)` posé à la fabrication, au ramassage et à la sortie de conteneur. **Arrondi au jour** pour que les piles d'un même jour fusionnent (le défaut classique de ces mods).
3. **Calcul seulement sur événement** : ouverture de conteneur, ramassage, et inventaire du joueur toutes les ~30 s. Un coffre jamais ouvert ne pourrit pas (compromis assumé, comme ShelfLife).
4. **Facteur de lieu** : `coldsweat.getTemperatureAt(level, pos)` du conteneur → **cave froide** (souterrain) ×0,3 ; **glacière Cold Sweat** ×0,1 ; au soleil en été ×1,5 (`EclipticSeasonsApi.getSeason`).
5. **Résultat** : l'objet périmé devient `minecraft:rotten_flesh`, `farm_and_charm:rotten_tomato` ou compost (F&C `compost`) → ça bouclera vers les engrais F&C.
6. **Infobulle** client : « frais / à consommer vite / avarié », sans compte à rebours précis (sobriété de la DA).
7. **Transformation = récompense** : recettes KubeJS du **plateau en bambou KC** (schéma `result` / `ingredient` / `subtype: "drying"` / `duration`) → viande séchée, poisson séché, fruits secs. Le séchage exige le ciel ouvert et pas de pluie (code KC) : il devient **saisonnier** avec la météo d'Ecliptic.
- Risques : performance (à profiler avec spark), Create (tapis/dépôts), auto-stockage. **Commencer par les seules rations d'expédition** (sac à provisions, panier-repas Supplementaries) avant d'étendre à toute la nourriture.

### 3.3 Agriculture & écosystème Kaleidoscope

#### Kaleidoscope Compat — `kaleidoscope-compat` (Xm1eP54z) — MR✔ `kaleidoscope_compat-2.9.7-neoforge+mc1.21.1.jar` — GitHub SShakusora/Kaleidoscope-Compat (branche 1.21.1-NeoForge, 120 commits, 2.10.0 en cours)
- Mécanique : **unifie les objets de KC qui recoupent ceux d'autres mods** (tags `c:`), et compat Farmer's Delight, **[Let's Do] Farm & Charm**, Bakeries, Overweight Farming, Drop the Meat, Vinery, Tavern, Crock Pot… Pour F&C : les recettes de la **marmite F&C sont rejouées dans le stockpot KC** (`afterStockpotRecipeMatch`) ; options `farm_and_charm_compat_enabled` et **`cooking_pot_recipes_disabled`** (désactive toutes les recettes de la marmite F&C). Le chapeau de paille fait tomber les graines de Vinery. Ajoute aussi des tags `sable` (entités) et `create` (blocs).
- **C'est l'exécution « prête à l'emploi » de la condition de la décision 0002** (KC seul système de cuisine, ingrédients fusionnés).
- **Verdict : socle.** Il reste à vérifier dans le .jar la liste exacte des objets fusionnés (tomate, laitue, farine, pâte) et à compléter en KubeJS.

#### Kaleidoscope Tavern (officiel, même équipe que KC) — `kaleidoscopetavern` (r9RZvhiJ) — MR✔ `kaleidoscopetavern-1.2.0-neoforge+mc1.21.1.jar` — GitHub KaleidoscopeMods/KaleidoscopeTavern (branche 1.21.1)
- Mécanique : **vigne sur treillis** (et vigne sauvage à trouver dans le monde), **cuve de foulage**, **tonneaux** (brassage en plusieurs étapes, **7 niveaux de qualité**), **cave (cellar cabinet)**, robinet, bar, shaker/cocktails (1.2.0), verrerie. Fournit déjà les tags `eclipticseasons`, `sereneseasons`, `diet` et `c`.
- Intégration : donne une **spécialisation de long terme** (cave, vieillissement, qualité) qui colle au pilier « on perfectionne » ; boissons → TWR (hydratation via `item-settings.toml`).
- À désactiver : **cocktail Molotov** (`MolotovBlock` : arme incendiaire, hors ton).
- **Verdict : recommandé** à la place de Vinery (doublon vigne/vin). Les addons World Liquor (marques réelles) et Bloodwine sont écartés.

#### Kaleidoscope Contraption — `kaleidoscope-contraption` (JCCAvv9N) — MR✔ 1.0.3
- Les postes KC (wok, stockpot, broche, cuiseur vapeur, fourneau) **fonctionnent sur des engins Create** (trains, ascenseurs) : wagon-cuisine, cuisine de dirigeable (à vérifier : Aeronautics passe par les sous-niveaux Sable, et non par les contraptions Create, alors que KC fournit déjà des tags Sable).
- **Verdict : optionnel** (pilier « expédition », à tester avec Steam 'n' Rails).

#### Kaleidoscope Orchard — `kaleidoscope-orchard` (UQJIT5UV) — MR✔ 0.0.2 (MIT, auteur non officiel)
- Arbres fruitiers dont la plantation exige **eau et bon sol autour des racines** ; fraise, orange, kiwi, myrtille ; tartes et vins de fruits. Version 0.0.x. **Verdict : optionnel / à surveiller** (bonne mécanique, maturité nulle ; risque de doublon de la fraise F&C).

#### Kaleidoscope Grilling — `kaleidoscope-grilling` (LPZkep5R) — MR✔ 1.1.1 (BSD-3)
- Colza → **pressage d'huile**, huiles pimentées, grillades (brochettes composées à la main, brochettes « signées »), nouvelles cultures (oignon = doublon F&C, patate douce, houttuynia, arbre à poivre du Sichuan). **Verdict : optionnel plus tard** (surtout du contenu ; l'huile pourrait servir de chaîne de transformation).

#### Seasonal Let's Do (yFIPn94r, MR✔ 1.5.0+1.21) / Seasonal Integration (w3gipv9f, datapack)
- Tags Serene Seasons pour les cultures Let's Do. Ecliptic lit ces tags, mais Seasonal Let's Do peut exiger le mod Serene Seasons (à vérifier). Seasonal Integration renvoie vers le *Serene Seasons API Stub* (**CurseForge seul**). **Verdict : s'en inspirer, recopier les tags dans le datapack KubeJS** (zéro dépendance).

#### Écartés en agriculture
Let's Do Vinery (doublon Tavern) ; Hearth & Harvest (dépend de Farmer's Delight) ; Crops Love Rain (F&C a déjà `enableRainGrowthEffect`) ; Fruitful Fun / AgriCraft / Dynamic Trees (pas de 1.21.1 NeoForge dans le miroir) ; Snow! Real Magic (Modrinth OK mais doublon de la neige d'Ecliptic) ; KubeJS Kaleidoscope (KC a déjà ses schémas KubeJS natifs) ; Ponder for KC (intégré à KC depuis 1.2.1) ; Kaleidoscope Doll/Doll Workshop/End/Nether/Twilight/Chinese Food (contenu).

### 3.4 Eau

#### Thirst Canteen — `thirst-canteen` (GV0PyXSv) — MR✔ `ThirstCanteen-1.21.1-3.7.jar`
- Gourdes (plusieurs paliers) pour la famille Thirst ; la page annonce sa transition vers TWR. TWR garde le modid `thirst` → la dépendance devrait être satisfaite, **plage de version à vérifier** dans le .jar.
- Intégration : l'eau devient une **logistique d'expédition** (purifier avant de partir). La gourde de Cold Sweat (eau chaude ou froide pour la température) garde un autre rôle.
- **Verdict : recommandé** (répond à la question « à tester » du registre).

Écartés : Create Wells (puits Create par rotation : doublon du puits F&C, empilement d'addons Create), Just a Well (doublon), Water Flasks (prévu pour TFC), Healthy Water (gadget).
Idées de cohérence (KubeJS) : l'eau du **puits F&C** sort en « acceptable » (nappe filtrée), l'eau de pluie récoltée en « purifiée », et le **filtre à sable TWR** fait office d'usine de traitement à l'ère Create.

### 3.5 Pêche

#### Tide — `tide` (die1AF7i) — MR✔ `tide-neoforge-1.21.1-2.1.1.jar` (+ Tide Extra Compatibility goMfyYSL, datapack 2.3.0)
- Nombreuses espèces par biome, profondeur, heure et météo, appâts, cannes améliorables, **journal de pêche** (collection), mini-jeu. Le datapack Extra Compatibility rend pêchables les poissons de **Critters and Companions (koï)** et d'**Alex's Mobs** (déjà dans le pack).
- Conflits : Hybrid Aquatic a sa propre faune ; vérifier les doublons de poissons et les tags `c:foods/raw_fish` qu'utilise KC. Ecliptic `EnableSeasonalFishing` modifie la pêche vanilla et peut s'ajouter à Tide → à régler.
- **Verdict : recommandé** (couvre eau douce et mer, s'intègre à la faune du pack).

#### River Fishing — `river-fishing` (5acJO3EU) — MR✔ `riverfishing-neoforge-0.9.0+1.21.1.jar` (dépend d'Architectury, déjà présent)
- Vrai **métier** : canne **assemblée** (scion, moulinet, fil de diamètre réel, montage, hameçons numérotés), appâts naturels contre leurres, **amorçage**, combat avec risque de casse, 30 espèces, **population vivante (surpêche, repeuplement au printemps)**, pêche sous la glace (tarière + mormyshka), élevage d'appâts (vers, asticots), aquarium, trophées, filetage. Eau douce seulement. Saisonnalité via Serene Seasons (inactive avec Ecliptic sans pont).
- Risques : 0.9.0 ; ajoute un « pêcheur-colon » PNJ (à confronter au concept « survivants » de la décision 0004).
- **Verdict : à comparer à Tide sur une branche dédiée** : plus profond et plus original, moins mûr. Ne pas garder les deux (doublons de poissons).

#### Fisherman's Trap — `fishermens-trap` (ltRggnDx) — MR✔ 3.1.0
- Nasse immergée en biome aquatique, appât selon l'espèce (pain = universel) : pêche passive mais conditionnée. **Optionnel** (bonne transition vers l'automatisation douce).

Écartés : Aquaculture 2 (contenu, neptunium), Fish of Thieves, Stardew Fishing, Fishing Real, Fishing Overhaul (doublons ou gadgets).

### 3.6 Apiculture
- **Ecliptic** gère déjà l'activité saisonnière des abeilles (`EnableSeasonalBee`, saisons de pollinisation et de sortie) → **socle par configuration**.
- **Complicated Bees** (`complicated-bees`, uhqEKIQS, MR✔ `complicated_bees-1.21.1-3.6.1.jar`) : génétique **diploïde** à la Forestry, croisements et mutations, ruches sauvages, outils, entièrement pilotée par datapack. Par défaut, elle produit aussi des ressources (minerais, diamants, netherite), ce qui casse Overgeared → **retirer ces espèces par datapack** et garder miel, cire, pollen, propolis. **Optionnel (gros morceau)** : c'est la vraie « génétique d'élevage » disponible.
- Realistic Bees (Serilum, besoin de Collective) : abeilles plus petites et plus nombreuses, purement visuel → optionnel. Buzzier Bees (contenu, Blueprint) et Productive Bees (ressources) → écartés.

### 3.7 Nutrition
- **Nutritional Balance** (`nutritional-balance`, OECzAr94, MR✔ `nutritionalbalance-1.21.1-7.0.3.jar`, GPL-3, GitHub dannydjdk/Nutritional-Balance, branche 1.21.1 par défaut, projet de 2021) : nutriments définis par tags (datapack), cibles, zones de **malnutrition** et d'**excès** (bonus si tout est équilibré, malus légers sinon), sucre « non essentiel », légumes jamais en excès, rechargement à chaud. **Parcourt les ingrédients de fabrication** pour déduire les nutriments → **vérifier qu'il suit les recettes KC (pot, stockpot)** ; sinon taguer les plats en KubeJS. **Recommandé** : récompense une agriculture et un élevage variés.
- **Nourished** (`nourished`, gXy6YNys, MR✔ 0.2.7-beta, MIT, nécessite MariesLib + Cloth Config) : 6 groupes (céréales, légumes, protéines, fruits, sucres, laitages) façon Diet, mémoire anti-répétition, **santé intestinale (manger cru = malus)**, compat Cold Sweat/Ecliptic/Serene Seasons, événements KubeJS (`NourishedEvents.nutrientChanged`). **Optionnel**, alternative plus riche mais bêta.
- Écartés : Diet (bloqué en 1.20.1 ; les tags `diet` fournis par KC sont inutiles), Spice of Life: Onion (doublon). **AppleSkin** recommandé (TWR affiche l'hydratation via AppleSkin).

### 3.8 Santé
- **Hardcore Revival** (`hardcore-revival`, HqKoXaXz, MR✔ 21.1.x) : à terre au lieu de mort, les coéquipiers réaniment → **recommandé** pour un monde entre amis loin de la base.
- **First Aid New** (`first-aid-new`, yNbhkSj2, MR✔ `firstaid-1.3.1-hotfix…+neoforge1.21.1`, GitHub maoruiQa/FIrst-Aid-New, 1.3.3 sur GitHub) : PV par membre, malus de blessure, soins dosés, perte de connaissance et réanimation, mais aussi adrénaline, morphine et addiction, défibrillateur (ton militaire). **Optionnel**, seulement si on coupe adrénaline et opioïdes ; il fait doublon avec Hardcore Revival (en choisir un).
- Maladies légères : **déjà couvertes** sans mod dédié (eau sale TWR, animaux malades Salt's, hypothermie et coup de chaleur Cold Sweat, cru → intestin avec Nourished).

### 3.9 Sommeil & temps
- **Comforts** (`comforts`, SaCpeal4, MR✔ 9.0.5) : sacs de couchage (ne déplacent pas le point de réapparition), hamacs (sieste de jour) → **recommandé** (expéditions, campements).
- **Sleep Quality** (S8fvNvSw, MR✔ 2.0.1) : effets au réveil selon le type de lit, la lumière, la présence d'un toit et la météo → optionnel. **Healthy Sleep** (3aMt7SZp) : score de sommeil, soin selon les heures dormies, « bien reposé », impossible de dormir sans fatigue → optionnel (en choisir un des deux).
- **Steve's Realistic Sleep** (ZPywkPEo, MR✔ 1.12.1) : le sommeil accélère le temps au lieu de le sauter (les cultures et les fours avancent) → à tester avec la durée du jour d'Ecliptic et `ClearAfterSleep`. **Campfire Resting** (k9O8NrbO, MR✔ 1.6.0) : attendre au feu de camp (avec une chance de « mal de dos ») → optionnel.
- **Hourglass** : aucune version 1.21.1 dans le miroir Modrinth (dernière : 1.20) → **écarté** ; Ecliptic gère déjà les nuits longues en hiver (`DynamicDaylightDuration`). Day Length : doublon d'Ecliptic.

---

## 4. Proposition de « système cohérent » pour le pilier

| Fonction | Qui fait quoi | Ce qu'on coupe |
|---|---|---|
| Calendrier, climat | **Ecliptic** (termes, météo, neige, durée du jour) → pont KubeJS vers **Cold Sweat** (déjà décidé) | Ecliptic `HeatStroke=false` (Cold Sweat s'en charge) |
| Eau | **TWR** (pureté, filtre Create) + **Thirst Canteen** (gourdes) + **puits F&C** | — |
| Champs | **KC** (cultures de cuisine) + **F&C** (céréales, sol, engrais, épouvantail, arroseur, silo) + saisonnalité/humidité **Ecliptic** + serres Ecliptic | F&C : cuisine et doublons (via **Kaleidoscope Compat** `cooking_pot_recipes_disabled=true` + KubeJS) ; `enableRainGrowthEffect` à réduire ou couper (l'humidité d'Ecliptic suffit) |
| Spécialisation | **Kaleidoscope Tavern** (vigne, tonneaux, cave, qualité) | Vinery, Molotov |
| Élevage | **Cattlestrophic** (nourrir, vieillir, domestiquer) + **Salt's Animal Farm** (confort, abri, poids, maladie) + **Animal Genders** (couples, femelles productrices) + Ecliptic (reproduction saisonnière) + infrastructure F&C (étable, poulailler, abreuvoir) | Reproduction auto de la **mangeoire F&C** (recette retirée, ou mangeoire = nourrissage Cattlestrophic seulement) ; Quark Feeding Trough (déjà coupé) |
| Pêche | **Tide** (ou **River Fishing** après comparaison) + Fisherman's Trap (option) | `EnableSeasonalFishing` d'Ecliptic à régler pour ne pas cumuler avec Tide |
| Abeilles | Ecliptic (saisons) [+ Complicated Bees sans espèces à ressources, option] | — |
| Cuisine | **KC** seul | F&C stove/cooking_pot/roaster/crafting_bowl + plats |
| Conservation | **FIAHI** (gel/altération thermiques, glacière/chaudière Cold Sweat) + **script KubeJS Garde-manger** (péremption lente, cave, séchage au plateau en bambou KC, tonneaux Tavern) | — |
| Nutrition | **Nutritional Balance** (ou Nourished) + AppleSkin | — |
| Santé, repos | **Hardcore Revival**, **Comforts** [+ Sleep Quality] | — |

**Boucle de jeu visée** : eau sûre → champs saisonniers → fourrage → troupeau nourri, abrité, avec couples gérés → produits (lait et œufs des femelles, viande, cuir) → cuisine KC → conservation (sécher, saler, glacière, cave) → rations d'expédition (gourde, sac à provisions, sac de couchage) → exploration.

---

## 5. Idées d'intégration KubeJS / datapack (premiers scripts)
1. **Tags saisonniers F&C** : `sereneseasons:{spring,summer,autumn,winter}_crops` (blocs et graines) et `eclipticseasons:crops/<min>_<max>` (format relevé chez KC : `average_moist`, `dry_average`, `humid_humid`, `moist_humid`) pour orge, avoine, maïs, fraise, oignon et plantes sauvages.
2. **Fusion KC ↔ F&C** (en plus de Kaleidoscope Compat) : masquer tomate, laitue, farine et pâte de F&C ; table de butin des plantes sauvages F&C → graines KC ; retirer les recettes des postes de cuisine F&C.
3. **Mangeoire F&C** : retirer la recette, ou annuler le `setInLove` via `NativeEvents` (`BabyEntitySpawnEvent` / `AnimalTameEvent`) hors saison ou si l'animal a faim (Cattlestrophic).
4. **Cattlestrophic** : JSON `acceptedFoodItems` = `farm_and_charm:barley/oat/corn/horse_fodder/chicken_feed`, `kaleidoscope_cookery:rice_panicle` ; JSON pour `untitledduckmod:*`.
5. **Salt's Animal Farm** : liste des « mobs effrayants » = prédateurs Naturalist et Alex's Mobs ; blocs moelleux = paille KC, sols d'étable F&C.
6. **TWR** `item-settings.toml` : thés et thé au lait KC, boissons Tavern (alcool = peu hydratant), soupes KC.
7. **Séchage** : recettes `kaleidoscope_cookery:bamboo_tray` (`subtype: "drying"`) pour viande séchée, poisson séché, fruits secs ; ajouter ces produits à `fallow:preserved` et à la liste blanche de FIAHI.
8. **Nutritional Balance** : taguer les plats KC et Tavern si la traversée des recettes ne les couvre pas.
9. **Pont saisons → Cold Sweat** : `EclipticSeasonsApi.getSubSeason(level)` → `coldsweat.createModifier/addModifier` (déjà prévu).
10. **Garde-manger** (§3.2) : utiliser `coldsweat.getTemperatureAt` pour l'effet de cave.

---

## 6. Incertitudes à vérifier dans les .jar
1. **FIAHI 4.1.0** : plage de version de Cold Sweat dans `neoforge.mods.toml` (2.4.3.1 acceptée ?), nature exacte de l'« altération » (réversible ou non), clés de config, conteneurs pris en compte (Create, KC, Supplementaries).
2. **Salt's Animal Farm 0.2** : clés de config, animaux moddés pris en charge, coût des scans de confort (spark), interaction avec le poids de Cattlestrophic.
3. **Cattlestrophic** : identifiant du bloc mangeoire, statut sauvage/domestique des animaux déjà présents dans le monde, prise en charge des canards Untitled Duck.
4. **Animal Genders** : poulailler F&C (la ponte respecte-t-elle le sexe ?), liste d'exclusion (hamsters, compagnons).
5. **Kaleidoscope Compat** : liste exacte des objets F&C fusionnés ; compatibilité avec KC 1.5.1 et 1.6.0.
6. **KC 1.5.1** (dans le pack) : contient-il déjà les tags Ecliptic, Serene Seasons et Diet vus en 1.6.0 ? Faut-il monter en 1.6.0 ?
7. **Kaleidoscope Tavern** : fonctionnement de la cave et du vieillissement, désactivation du Molotov, compat soif.
8. **Tide / River Fishing** : tags `c:foods/raw_fish` lus par KC, doublons avec Hybrid Aquatic, rôle du PNJ pêcheur de River Fishing.
9. **Thirst Canteen 3.7** : la dépendance `thirst` accepte-t-elle TWR 3.0.6 ?
10. **Nutritional Balance** : suit-il les types de recettes KC ? Chemin des tags (`c:nutrient/…` ou `forge:nutrient/…`) ?
11. **Ecliptic** : effet de `ForceCompatMode` sur les cultures F&C non taguées ; cumul de `EnableSeasonalBreed` avec la mangeoire F&C ; Seasonal Let's Do exige-t-il Serene Seasons ?
12. **Puits F&C** : quelle pureté TWR pour l'eau qu'il fournit ? `enableRainGrowthEffect` se cumule-t-il avec l'humidité d'Ecliptic ?
13. **Sommeil** : Comforts, Steve's Realistic Sleep et `ClearAfterSleep` d'Ecliptic sont-ils compatibles ?
14. **Butchery** : sortie FD ou vanilla par défaut, source du sel.
15. **Complicated Bees** : retrait des espèces à ressources par datapack ; Ecliptic s'applique-t-il à ses abeilles (entité propre) ?
16. Miroir Modrinth daté : reconfirmer la dernière version 1.21.1 de chaque mod au moment de l'ajout avec packwiz.

**Bloquants hors Modrinth repérés** : Serene Seasons API Stub (Ecliptic Bridge), Ecliptic Seasons MultiMod Patch et Ecliptic Seasons Bundles (CurseForge seul), Animania Reborn (CurseForge seul), ShelfLife (GitHub seul), [BUB] Gender (non vérifié).

**Hors domaine, à transmettre** : *Aeronautics: Winds and Weather* (`aeronautics-winds-and-weather`, MR✔ 1.3.2) : vent réel selon le relief, l'altitude et la météo, voiles de navires et moulins à vent. Intéressant pour le pilier Civilisation.

---

## Sources
- KC : https://github.com/KaleidoscopeMods/KaleidoscopeCookery (branche `1.21.1-neoforge` : `build.gradle`, `compat/`, `src/generated/resources/data/{sereneseasons,eclipticseasons,diet}`, `BambooTrayBlockEntity`)
- Tavern : https://github.com/KaleidoscopeMods/KaleidoscopeTavern (branche 1.21.1) · Modrinth https://modrinth.com/mod/kaleidoscopetavern
- Ecliptic : https://github.com/TeamTeaMC/Ecliptic-Seasons (branche `neoforge-1.21.x`, `CommonConfig.java`, `compat/kubejs/KubeJSPluginImpl.java`, `api/EclipticSeasonsApi.java`) · https://modrinth.com/mod/ecliptic-seasons
- Cold Sweat : https://github.com/Momo-Softworks/Cold-Sweat (branche 1.21-FG, `CompatManager.java`, `compat/kubejs/KubeBindings.java`)
- TWR : https://github.com/mlus-asuka/Thirst-Was-Reclaimed (+ wiki : Water-Purity, Purification, Temperature, Drinks)
- Farm & Charm : https://github.com/Let-s-Do-Collection/FarmAndCharm (branche 1.21 : `ObjectRegistry`, `FarmAndCharmNeoForgeConfig`, `ApproachFeedingTroughGoal`)
- Kaleidoscope Compat : https://github.com/SShakusora/Kaleidoscope-Compat · https://modrinth.com/mod/kaleidoscope-compat
- Nutritional Balance : https://github.com/dannydjdk/Nutritional-Balance · Nourished : https://github.com/kgbcupcake/nourished · First Aid New : https://github.com/maoruiQa/FIrst-Aid-New · Animania Reborn : https://github.com/Steveaaaaa/Animania-Reborn · ShelfLife : https://github.com/xiaofeng96301/ShelfLife
- Miroir de l'API Modrinth (versions, loaders) : https://github.com/qacow37/prismnix (`packages/projects/<id>.nix`) ; miroir des descriptions Modrinth : https://github.com/Voxelum/xmcl-community-content-i18n-extra (`ru/<id>.json`)
- Pages Modrinth (non consultables directement) : https://modrinth.com/mod/salts-animal-farm · https://modrinth.com/mod/cattlestrophic! · https://modrinth.com/mod/animal-genders · https://modrinth.com/mod/animal-weight · https://modrinth.com/mod/freeze-it-and-heat-it · https://modrinth.com/mod/tide · https://modrinth.com/mod/river-fishing · https://modrinth.com/mod/thirst-canteen · https://modrinth.com/mod/comforts · https://modrinth.com/mod/hardcore-revival · https://modrinth.com/mod/nutritional-balance · https://modrinth.com/mod/complicated-bees · https://modrinth.com/mod/butchery · https://modrinth.com/mod/aquatic-breeding · https://modrinth.com/mod/kaleidoscope-contraption · https://modrinth.com/mod/kaleidoscope-orchard
- Recherches web (avant épuisement du quota) : Animal Husbandry, Spoiled et Cattlestrophic sur CurseForge/Modrinth ; Ecliptic Seasons sur CurseForge (0.14.4, août 2026).
