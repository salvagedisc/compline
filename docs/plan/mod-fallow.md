# Le mod custom `fallow`

Le mod proposé au [12.4](12-verification.md#124-le-mod-custom-fallow) et retenu par la décision 17 du [12.5](12-verification.md#125-décisions-qui-sajoutent-suite-du-1012). Il est fait de modules qu'on active un par un. La version 0.2.0 contient les modules **ciel**, **sac** et **xp**.

> **État au 2026-10-19.** La version 0.2.0 compile (`./gradlew build`), mais **rien n'a été testé en jeu**. Tout ce qui suit vient de la lecture du code des mods visés (jars ouverts avec `javap`). La liste des [tests en jeu](#tests-en-jeu) dit ce qu'il faut vérifier avant de s'y fier.

## Construire et mettre à jour

| Quoi | Où |
|---|---|
| Sources | `mods-custom/fallow/` : MDK officiel ModDevGradle 1.21.1, wrapper Gradle 9.2.1, Java 21 |
| Paquet Java | `io.github.salvagedisc.fallow` |
| Jar dans le pack | `pack/mods/fallow-<version>.jar`, un jar brut (pas de `.pw.toml`) |
| CI | job `mod-fallow` de `.github/workflows/build.yml` : il compile le mod et garde le jar en artefact |

Pour sortir une version :

```sh
cd mods-custom/fallow
./gradlew build                     # jar dans build/libs/fallow-<version>.jar
cp build/libs/fallow-<version>.jar ../../pack/mods/   # retirer l'ancien jar
cd ../../pack && packwiz refresh
```

La version se règle dans `gradle.properties` (`mod_version`). Les mods visés ne servent qu'à la compilation (`compileOnly`, dépôt Maven de Modrinth). `build.gradle` lit leur version dans `pack/mods/<slug>.pw.toml` : le mod se compile toujours contre les versions exactes du pack, et une mise à jour d'un de ces mods se voit à la compilation suivante.

## Principes

- **Un module = un interrupteur.** Chaque module a son booléen `enabled` dans la config : `config/fallow-client.toml` pour `ciel`, `config/fallow-common.toml` pour `sac` et `xp`. Les valeurs sont relues à chaque usage, donc un changement en jeu prend effet tout de suite.
- **Un module ne charge que si ses mods sont là.** Au démarrage, `Fallow` vérifie avec `ModList` les mods dont dépend chaque module. Il écrit « Module X chargé » ou « Module X ignoré » dans le log. Les mixins passent par `FallowMixinPlugin`, qui ne les applique que si les mods visés sont dans `LoadingModList`. La seule dépendance dure est NeoForge.
- **Pas de code recopié.** Le mod passe par les API publiques des mods visés et par des mixins ciblés, sur du code vanilla quand c'est possible.

| Module | Côté | Mods demandés | Mixins |
|---|---|---|---|
| `ciel` | client | ATMOSPHERICS ; Enhanced Celestials pour la lune | 4, tous client |
| `sac` | les deux (l'effet s'exécute sur le serveur) | Traveler's Backpack et Thirst Was Reclaimed | aucun |
| `xp` | les deux (l'enclume des deux côtés, la barre chez le client) | aucun | 1, commun (`xp.AnvilMenuMixin`, sur du code vanilla) |

## Module `ciel`

### La lune d'Enhanced Celestials sous ATMOSPHERICS

**Le problème.** ATMOSPHERICS annule `LevelRenderer.renderSky` (injection au début) et redessine tout le ciel. Sa lune utilise toujours la texture vanilla `moon_phases.png`, de taille 20 et en blanc. Les mixins d'Enhanced Celestials sur `renderSky` ne s'exécutent donc plus : la lune blanche, la lune de sang et les autres évènements gardent l'aspect de la lune vanilla. La lune blanche est d'ailleurs plus petite (14) que la lune vanilla : la redessiner par-dessus ne suffit pas, il faut d'abord cacher celle d'ATMOSPHERICS.

**Le mécanisme** (`MoonOverlay` et trois mixins) :

1. `LevelRendererMixin` entoure l'appel à `renderSky` dans `renderLevel` (`@WrapOperation`). Juste avant l'appel, le module s'arme si la dimension a des données lunaires Enhanced Celestials.
2. Le même mixin repère le premier `Runnable.run()` de `renderSky` : c'est le réglage du brouillard, au début du corps vanilla. S'il s'exécute, ATMOSPHERICS n'a pas pris la main (il ne le fait pas dans les dimensions de No Man's Land ni pendant la Friend Moon) ; Enhanced Celestials dessine alors sa lune lui-même, et `fallow` ne touche à rien.
3. Sinon, `RenderSystemMixin` remplace, pendant le seul ciel d'ATMOSPHERICS, la texture `moon_phases.png` par une texture transparente (`fallow:textures/environment/hidden_moon.png`). Le quad de lune d'ATMOSPHERICS ne dessine plus rien.
4. Après `renderSky`, si la lune a bien été cachée, `fallow` dessine celle d'Enhanced Celestials au même endroit, avec les mêmes matrices :
   - la texture de l'évènement en cours ;
   - la taille fondue entre l'évènement précédent et l'actuel, comme le fait Enhanced Celestials ;
   - la couleur, par `ECWorldRenderer.changeMoonColor`, la fonction qu'Enhanced Celestials appelle dans le ciel vanilla (elle gère aussi l'effacement par la pluie) ;
   - l'échelle (`moonTextureScale`) et la rotation (`moonTextureRotation`) réglées dans la config d'ATMOSPHERICS.

Si une étape manque (ATMOSPHERICS ne dessine pas de lune, Enhanced Celestials absent…), rien n'est caché et rien n'est dessiné. Une erreur de dessin est notée une seule fois dans le log, sans faire planter le rendu.

Config : `ciel.redrawMoon` (vrai par défaut).

**Limites connues.**
- La lune redessinée passe après tout le ciel d'ATMOSPHERICS, donc par-dessus ses nuages ou son voile de ciel s'il en dessine après la lune. À regarder en jeu.
- Elle n'est pas dessinée quand elle est sous l'horizon (test sur l'heure, avec une marge pour les grosses lunes).

### Vider le cache de brume d'ATMOSPHERICS

ATMOSPHERICS calcule la brume à partir des biomes autour du joueur. Il garde le résultat en cache (`BiomeBlendUtil`) tant que le joueur reste dans la même cellule de 4 blocs. Si les biomes changent sous un joueur immobile (conversion en clairière, `/fillbiome`), la brume ne réagit qu'au prochain déplacement.

- **Automatique** : `ClientPacketListenerMixin` vide le cache chaque fois que le serveur envoie de nouveaux biomes (paquet `ClientboundChunksBiomesPacket`, celui que produit `/fillbiome`). Config : `ciel.clearFogOnBiomeUpdate` (vrai par défaut).
- **À la main** : une méthode statique publique, utilisable plus tard depuis KubeJS (scripts client) :

  ```js
  Java.loadClass('io.github.salvagedisc.fallow.api.FallowSky').clearFogCache()
  ```

  Elle renvoie `true` si le cache a été vidé, ou programmé pour l'être sur le fil de rendu. Elle renvoie `false` si ATMOSPHERICS est absent, si le module est coupé ou si on est sur un serveur. `FallowSky.isFogCacheAvailable()` répond à la même question sans rien vider. KubeJS peut demander d'autoriser la classe dans son filtre de classes. Un script serveur qui veut déclencher le vidage chez les joueurs doit leur envoyer un paquet, par exemple avec `NetworkEvents.dataReceived` côté client.

Le cache est vidé par `BiomeBlendUtil.resetCache()`, une méthode privée d'ATMOSPHERICS, atteinte par un accesseur mixin (`@Invoker`).

## Module `sac`

**Le mécanisme.** Traveler's Backpack a une API d'effets de fluide (`api.fluids.EffectFluid`). Chaque effet enregistré pour un fluide s'exécute quand le joueur boit ce fluide au tuyau. `fallow` ajoute l'effet `fallow:thirst_water` sur l'eau, à côté de celui du sac, qui éteint le feu. Le sac vide son registre d'effets pendant son initialisation commune : l'effet de `fallow` est donc enregistré à la fin du chargement (`FMLLoadCompleteEvent`). Le log l'annonce par « sac : effet fallow:thirst_water enregistré ». Le module n'a aucun mixin.

À chaque gorgée, sur le serveur :

1. la pureté de l'eau du réservoir est lue par l'API de Thirst Was Reclaimed (`WaterPurity.getPurity`) ;
2. les effets de cette pureté s'appliquent (`WaterPurity.givePurityEffects` : nausée, poison, selon la config de TWR). Si l'eau rend malade et que TWR ne désaltère pas dans ce cas (`quenchThirstWhenDebuffed`), on s'arrête là ;
3. sinon le joueur gagne l'hydratation et la désaltération d'une bouteille d'eau (`minecraft:potion`) selon TWR (6 d'hydratation par défaut), multipliées par `sac.bottlesPerDrink`.

Config : `sac.enabled` (vrai par défaut) et `sac.bottlesPerDrink` (1 à 4, défaut 1).

### Ce qui est déduit du code

La pureté de l'eau du réservoir existe bien : TWR la range dans le composant `thirst:purity` du `FluidStack`, de 0 (sale) à 3 (purifiée). Ce que montrent les jars :

- **Une gorgée retire 1000 mB.** Le tuyau retire la plus grande quantité demandée par les effets du fluide (1000 pour l'eau). Avec moins de 1000 mB dans le réservoir, il ne se passe rien.
- **Un seau garde sa pureté.** Le sac remplit ses réservoirs avec `FluidUtil` ; TWR réécrit le passage par `FluidBucketWrapper` et y ajoute la pureté du seau.
- **Un mélange garde la pureté la plus basse.** Les réservoirs du sac sont des `FluidTank` NeoForge, que TWR modifie aussi.
- **L'eau aspirée au tuyau dans le monde compte comme purifiée.** Le mode aspiration remplit le réservoir avec `new FluidStack(eau, 1000)`, sans composant de pureté. Or TWR lit un composant absent comme une eau purifiée (`MISSING_COMPONENT_PURITY = 3`). Il retire même le composant quand il vaut 3, donc « absent » et « purifiée » ne se distinguent pas. Le module suit la règle de TWR : **l'eau aspirée directement à une source est propre**. C'est un raccourci qui évite de purifier l'eau, à trancher après le test en jeu. Correction possible : un mixin ciblé sur l'aspiration du tuyau, qui donnerait à l'eau la pureté du bloc aspiré.

## Module `xp`

Fallow n'a plus d'expérience ([xp.md](xp.md)). Les orbes et les gains de points sont coupés par `pack/kubejs/server_scripts/sans_xp.js`. Le module fait les deux choses que KubeJS ne sait pas faire proprement.

**L'enclume sans niveaux.** Vanilla ne rend l'objet que si `player.experienceLevel >= coût` et `coût > 0` (`AnvilMenu.mayPickup`). `AnvilMenuMixin` remplace, dans cette seule méthode, la lecture de `experienceLevel` par `Integer.MAX_VALUE` (`@ModifyExpressionValue` de MixinExtras). Le reste du calcul ne change pas :

- le coût vaut toujours au moins 1, et la limite de 40 posée par `createResult` reste. Elle devient une **usure** : « Usure : n », puis « Trop usé ! » (clés vanilla `container.repair.cost` et `container.repair.expensive`, traduites dans `pack/kubejs/assets/minecraft/lang/`) ;
- les matériaux (lingots, objet sacrifié) sont toujours pris ; `onTake` retire encore `coût` niveaux, ce qui ne fait rien à 0 ;
- la lune bleue d'Enhanced Celestials, qui divise le coût dans `createResult`, réduit donc l'usure.

Le mixin s'applique des deux côtés : le client refuse lui aussi de prendre l'objet s'il croit que les niveaux manquent, et il colore le coût en rouge. Client et serveur doivent avoir la même version du mod et la même valeur de `xp.enabled`. Aucun mod du pack ne patche `mayPickup`.

**La fusion d'enchantements (facultatif).** Avec `xp.blockEnchantMerge`, `XpModule` annule `AnvilUpdateEvent` quand l'objet de droite porte des enchantements (livre enchanté ou objet enchanté) : l'enclume ne donne pas de résultat, l'enchantement passe par la table. Coupé par défaut : L'équipe garde la fusion.

**La barre cachée.** Chez le client, `XpClient` annule les couches `VanillaGuiLayers.EXPERIENCE_BAR` et `EXPERIENCE_LEVEL` dans `RenderGuiLayerEvent.Pre`. La jauge de saut à cheval (`JUMP_METER`) est une autre couche et reste. Les cœurs et la faim gardent leur hauteur : un petit vide reste à la place de la barre.

Config (`fallow-common.toml`, section `[xp]`) : `enabled` (vrai), `hideBar` (vrai), `blockEnchantMerge` (faux). Les deux derniers ne jouent que si `enabled` est vrai. Revenir en arrière : `enabled = false` et supprimer `sans_xp.js`.

## Tests en jeu

Rien de ceci n'a été fait. À passer sur une instance du pack, puis à cocher ici.

### Ciel

| # | Test | Attendu |
|---|---|---|
| C1 | Nuit claire normale, dans l'Overworld | Une seule lune, à sa place, avec la bonne phase. Pareil avec une échelle et une rotation changées dans la config d'ATMOSPHERICS |
| C2 | `/setLunarEvent` vers la lune blanche (peut demander un ciel dégagé) | La lune blanche visible, plus petite, sans la lune vanilla derrière |
| C3 | Lune de sang, lune bleue, lune des moissons, super lune (taille 40) | Couleur et taille propres à chaque évènement, et fondu à la transition |
| C4 | Pluie, puis orage | La lune s'efface avec la pluie, comme le reste du ciel |
| C5 | Sous l'eau, dans la neige poudreuse, avec cécité | Pas de double lune ni de lune qui disparaît |
| C6 | Dimension de No Man's Land, Friend Moon | Le ciel de ces cas reste celui de leur mod (log sans erreur `fallow`) |
| C7 | `ciel.redrawMoon = false`, puis `ciel.enabled = false` | Retour à la lune vanilla d'ATMOSPHERICS |
| C8 | `/fillbiome` autour d'un joueur immobile, avec `clearFogOnBiomeUpdate` vrai puis faux | La brume change tout de suite, puis seulement après un pas |
| C9 | Appel KubeJS de `FallowSky.clearFogCache()` | Renvoie `true`, la brume se recalcule |
| C10 | Log au démarrage | « Module ciel chargé », aucun avertissement de mixin `fallow` |
| C11 | Images par seconde la nuit, avec et sans le module | Pas d'écart visible |
| C12 | Instance sans Enhanced Celestials, puis sans ATMOSPHERICS | Le jeu démarre ; mixins de la lune non appliqués, ou module ignoré |

### Sac

| # | Test | Attendu |
|---|---|---|
| S1 | Seau d'eau sale (rempli à une source) vidé dans le réservoir, puis bu au tuyau | Effets de l'eau sale de TWR ; désaltère ou non selon sa config |
| S2 | Seau d'eau purifiée (four ou feu de camp) | Désaltère, sans effet négatif |
| S3 | Eau aspirée au tuyau dans un lac | Désaltère comme une eau purifiée (raccourci à trancher, voir plus haut) |
| S4 | Mélange d'eau sale et d'eau purifiée dans un réservoir | La pureté la plus basse l'emporte |
| S5 | Une gorgée, avec `bottlesPerDrink` à 1 puis à 3 | 1000 mB retirés ; 6 puis 18 d'hydratation (valeurs par défaut de TWR) |
| S6 | Boire en feu | Le feu s'éteint toujours (effet du sac) |
| S7 | Serveur dédié | La barre de soif se met à jour chez le joueur |
| S8 | `sac.enabled = false` | Le tuyau ne désaltère plus |
| S9 | Log au démarrage | « Module sac chargé » puis « sac : effet fallow:thirst_water enregistré » |
| S10 | Réservoir avec moins de 1000 mB | Rien ne se passe, comme avant |

### XP

| # | Test | Attendu |
|---|---|---|
| X1 | Log au démarrage | « Module xp chargé (aucun mod requis) », aucun avertissement de mixin `fallow` |
| X2 | À 0 niveau : nommer une étiquette, réparer une pioche avec des lingots, fusionner deux pioches enchantées | Coût en vert (« Usure : n »), l'objet se prend, les matériaux partent |
| X3 | Réparer le même objet jusqu'à l'usure 40 | « Trop usé ! », pas de résultat (sauf en créatif) |
| X4 | Serveur dédié | Le client prend l'objet sans retour en arrière de l'emplacement |
| X5 | `xp.enabled = false` | L'enclume redemande des niveaux, la barre revient |
| X6 | `xp.blockEnchantMerge = true`, livre enchanté à droite | Pas de résultat ; une réparation aux lingots marche toujours |
| X7 | À pied, à cheval, en visant au canon de Supplementaries | Ni barre ni chiffre d'XP ; la jauge de saut reste à cheval |
| X8 | Lune bleue d'Enhanced Celestials | Le coût affiché baisse |

## Entretien

À chaque mise à jour d'ATMOSPHERICS, d'Enhanced Celestials, de Traveler's Backpack, de Thirst Was Reclaimed ou de NeoForge :

1. recompiler le mod (la CI le fait). Une erreur montre qu'une API a changé ;
2. lancer le jeu et lire le log : un mixin qui ne trouve plus sa cible y laisse un avertissement ;
3. repasser les tests du module concerné.

Points fragiles connus :
- ATMOSPHERICS : son mixin en tête de `renderSky`, la texture `moon_phases.png`, le nom de `BiomeBlendUtil.resetCache` ;
- Enhanced Celestials : `ECWorldRenderer.changeMoonColor` et les champs de `LunarEventClientSettings` ;
- Traveler's Backpack : le constructeur d'`EffectFluid` et le moment où il vide son registre ;
- Thirst Was Reclaimed : `WaterPurity` et `ThirstHelper`.
- Minecraft et NeoForge : la lecture de `Player.experienceLevel` dans `AnvilMenu.mayPickup`, les noms des couches `EXPERIENCE_BAR` et `EXPERIENCE_LEVEL`, l'annulation de `AnvilUpdateEvent`.

## Modules suivants

Dans l'ordre du [12.4](12-verification.md#124-le-mod-custom-fallow) : **dissipateur** (bloc cinétique Create, après le prototype KubeJS), puis, selon les tests, tombe, voies, plans et finitions. Chacun aura sa section ici, son interrupteur et ses tests.
