# 9. Disponibilité sur Modrinth

Vérifié le 2026-10-10 sur l'API Modrinth (`/v2/project` puis `/v2/project/{id}/version?loaders=["neoforge"]&game_versions=["1.21.1"]`). La version indiquée est la plus récente pour NeoForge 1.21.1 à cette date ; elle ne remplace pas le `packwiz mr add`, qui fige la version au moment de l'ajout.

## Résultat

- **98 des 104 mods proposés existent en 1.21.1 NeoForge sur Modrinth**, ainsi que les 3 remplaçants. Les 46 mods déjà retenus sont figés dans `pack/mods/` et ne sont pas repris ici.
- **6 sont absents** et sont remplacés ou retirés (tableau ci-dessous). Deux autres avaient un mauvais identifiant dans le plan et sont bien présents.
- Deux mods passent par Sinytra Connector comme Antique Atlas 4 et Surveyor, déjà dans le pack : **AA4 Atlas** et **Antique Trains**. Ce sont des fichiers multi-chargeurs de la même famille (Surveyor), publiés pour NeoForge par leurs auteurs. L'alerte « aucun autre mod Fabric » de 6.1 devient « aucun mod Fabric hors de la famille Surveyor ».
- **12 bibliothèques nouvelles** arrivent avec les ajouts : YUNG's API, Supermartijn642's Config Lib et Core Lib, Easy NPC Core et Config UI, Cloth Config, Balm, CorgiLib, Data Anchor, Configurable, MezzConfig, Collective. Les optionnels en ajoutent 4 autres (Integrated API, Marie's Lib, Konkrete, Melody).
- **Versions non stables** à surveiller : alpha pour COE Deposits (optionnel) ; beta pour JEI, In Control!, KubeJS Create, Simple Backups, OPAC, Simple Voice Chat, Create Factory Logistics, Salt's Animal Farm, Separate Sable Render Distance, Create Crafts & Additions, Kaleidoscope Orchard, Simple Clouds, Longwings, Better Advancements, Polymorph, Nourished, Sound Physics: Aeronautics. Pour JEI, In Control!, Simple Voice Chat et OPAC, c'est le canal habituel de l'auteur, pas un signe de fragilité.
- **Peu entretenus** (dernier fichier 1.21.1 de plus d'un an) : Traveler's Titles, MCG's RPG Campsites, Create: New Beginnings, Create: Bells & Whistles, Fisherman's Trap, Controlling, Searchables, Mouse Tweaks, Better Advancements. Ils fonctionnent tels quels ; ils ne suivront pas les mises à jour de Create.

## Absents et remplaçants

| Mod prévu | Ce que Modrinth propose | Remplaçant retenu |
|---|---|---|
| Dead Reckoning | Fabric seulement, pas publié pour NeoForge | **GUI Compass** (`gui-compass`, natif, Serilum) : `guiCompassFormat = "F"` (direction seule, pas de coordonnées) et `mustHaveCompassInInventory = true`. Second repli : NeoCompass, toujours affiché. |
| Villager Workers | Devenu « Hired Workers », 1.21.8 seulement | **Create Villager Labor** (`create-villager-labor`) : un villageois s'assoit à un poste Create et fait tourner la chaîne. C'est une autre idée que des ouvriers autonomes ; à tester en phase 7. Sans lui, les survivants restent des PNJ Easy NPC. |
| Villager Recruits | Forge 1.20.1 au plus | Retiré. Pas d'escorte de convoi. |
| Presence Not Position | Introuvable sur Modrinth | **Reactive Music** (`reactive-music`, client, 7,5 M de téléchargements) (remplacé par True Adaptive Music, [0010](../decisions/0010-musique.md)) : musique selon le lieu, l'heure, le danger et l'abri. Ses morceaux fantasy et celtiques doivent être remplacés par un songpack plus sobre ou réduits ; à écouter en phase 6. Le titre à l'entrée d'un lieu reste porté par Traveler's Titles. |
| Biome Music | Fabric seulement | Couvert par Reactive Music. |
| Configured | Fabric 1.21.8 et plus | Retiré (optionnel). |
| Create: New Beginnings, CTOV | Mauvais identifiant dans le plan, mais bien présents | `create-new-beginnings` 0.1.1 et `ct-overhaul-village` 3.6.3. CTOV modifie aussi les avant-postes de pillards, que le plan désactive : sans effet. |

**MineColonies** n'a pas de fichier 1.21.1 sur Modrinth (seulement Forge 1.18.2) ; il reste exclu par la règle « Modrinth seulement », en plus du choix de 3.6.

## Tableau complet

Côté : **C** client seul, **S** serveur seul, **CS** les deux. Les dépendances en gras ne sont pas encore dans le pack.

| Mod | Slug Modrinth | Version 1.21.1 NeoForge | Canal | Date | Côté | Dépendances requises |
|---|---|---|---|---|---|---|
| Lootr | `lootr` | 1.21.1-1.11.38.127 | stable | 2026-09-25 | CS | — |
| Paraglider | `paragliders` | 21.1.5 | stable | 2026-03-22 | CS | — |
| ParCool! | `parcool` | 1.21.1-4.0.1.2 | stable | 2026-10-09 | CS | — |
| Create Aeronautics Structures | `aeronautics-structures` | 1.2 | stable | 2026-08-13 | CS | sable, create, create-aeronautics |
| Create: New Beginnings | `create-new-beginnings` | 0.1.1 | stable | 2025-03-09 | S | create |
| Create: Let The Adventure Begin | `create-let-the-adventure-begin` | 4.1.0 | stable | 2026-08-09 | CS | — |
| MCG's RPG Campsites | `mcgs-rpg-campsites` | 1.0.0 | stable | 2025-03-29 | S | — |
| Cinch's Abandoned Houses | `cinchs-abandoned-houses` | (NeoForge-1.21.x-26.2)-2.0.1 | stable | 2026-06-20 | S | — |
| Exposure | `exposure` | 1.9.19 | stable | 2026-09-23 | CS | — |
| Traveler's Backpack | `travelersbackpack` | 1.21.1-10.1.39 | stable | 2026-08-29 | CS | — |
| Comforts | `comforts` | 9.0.5+1.21.1 | stable | 2026-01-24 | CS | — |
| Traveler's Titles | `travelers-titles` | 1.21.1-NeoForge-5.1.3 | stable | 2025-03-11 | C | **yungs-api** |
| Dead Reckoning | `dead-reckoning` | **absent** | — | — | — | seulement fabric 1.20.1, 1.21.1 |
| AA4 Atlas | `aa4-atlas` | 1.1.2 | stable | 2025-07-31 | CS | **fabric-api**, surveyor, antique-atlas-4 |
| NeoCompass | `neocompass` | 1.0 | stable | 2025-11-12 | C | — |
| Streams Reflowing | `streams-reflowing` | 2.14.4 | stable | 2026-10-04 | CS | — |
| Distant Horizons | `distanthorizons` | 3.3.3-1.21.1 | stable | 2026-09-29 | CS | — |
| Separate Sable Render Distance | `ssrd` | 1.8.7 | beta | 2026-09-21 | CS | sodium, sable |
| Better Archeology | `better-archeology` | 1.21.1-1.3.9 | stable | 2026-10-08 | CS | resourceful-config, architectury-api |
| CTOV | `ct-overhaul-village` | 3.6.3 | stable | 2026-03-21 | S | — |
| Towers of the Wild Modded | `totw-modded` | 1.0.9 | stable | 2026-01-17 | CS | — |
| Create: Structures Arise | `create-structures-arise` | 176.49.49 | stable | 2026-09-08 | CS | create |
| Create: Rustic Structures | `create-rustic-structures` | 1.0.1 | stable | 2026-05-21 | CS | create |
| IDAS | `idas` | 1.13.7+1.21.1-neoforge | stable | 2026-06-11 | CS | **integrated-api**, quark, supplementaries, create |
| Antique Trains | `antique-trains` | 1.0.2 | stable | 2025-05-11 | C | create, antique-atlas-4 |
| Create: Copycats+ | `copycats` | 3.0.9+mc.1.21.1-neoforge | stable | 2026-09-06 | CS | create |
| Create: Connected | `create-connected` | 1.3.3-mc1.21.1 | stable | 2026-08-31 | CS | create |
| Create Factory Logistics | `create_factory_logistics` | 1.6.1 | beta | 2026-10-05 | CS | — |
| Create Ore Excavation | `create-ore-excavation` | 1.21.1-1.6.8 | stable | 2025-11-08 | CS | create |
| Prospecting Picks | `prospecting-picks` | 1.2.1 | stable | 2026-01-16 | CS | — |
| Create: Bells & Whistles | `bellsandwhistles` | v0.4.7-1.21.1 | stable | 2025-03-13 | CS | create |
| Create: Diesel Generators | `create-diesel-generators` | 1.21.1-1.3.15 | stable | 2026-07-30 | CS | create |
| Create: Power Loader | `create-power-loader` | 2.0.5-mc1.21.1 | stable | 2026-05-22 | CS | create |
| Rechiseled | `rechiseled` | 1.2.6-neoforge-mc1.21 | stable | 2026-09-13 | CS | **supermartijn642s-config-lib**, **supermartijn642s-core-lib**, **fusion-connected-textures** |
| Rechiseled: Create | `rechiseled-create` | 1.1.1-neoforge-mc1.21 | stable | 2026-06-22 | CS | create, **rechiseled** |
| Fusion (Connected Textures) | `fusion-connected-textures` | 1.3.16-neoforge-mc1.21.1 | stable | 2026-10-05 | C | — |
| Easy NPC | `easy-npc` | 7.14.0 | stable | 2026-10-02 | CS | **easy-npc-config-ui**, **easy-npc-core** |
| Villager Workers | `workers` | **absent** | — | — | — | seulement neoforge 1.21.8 |
| Villager Recruits | `villager-recruits` | **absent** | — | — | — | seulement forge 1.18.2, 1.19.2, 1.19.4, 1.20.1 |
| Open Parties and Claims | `open-parties-and-claims` | neoforge-1.21.1-0.32.7 | beta | 2026-10-05 | CS | — |
| Create Crafts & Additions | `createaddition` | neoforge-1.21.1-1.7.2 | beta | 2026-10-01 | CS | — |
| Create: Numismatics | `numismatics` | 1.1.0+neoforge-mc1.21.1 | stable | 2026-08-27 | CS | create |
| COE Deposits | `create-ore-excavation-deposits` | 0.2.0 | alpha | 2026-06-13 | CS | **create-ore-excavation** |
| Create: Interiors | `interiors` | 0.6.1 | stable | 2026-01-29 | CS | — |
| SableCat | `sablecat` | 1.0.1 | stable | 2026-08-07 | CS | create, create-aeronautics |
| Villagers Reborn | `villagers-reborn` | 1.0.9b | stable | 2026-08-05 | CS | — |
| Aeronautics: Winds and Weather | `aeronautics-winds-and-weather` | 1.3.1 | stable | 2026-08-18 | CS | create, sable, create-aeronautics |
| Kaleidoscope Compat | `kaleidoscope-compat` | 2.9.7-neoforge+mc1.21.1-Patch.2 | stable | 2026-10-10 | CS | — |
| Cattlestrophic! | `cattlestrophic!` | 1.3.0 | stable | 2025-11-20 | CS | — |
| Salt's Animal Farm | `salts-animal-farm` | 0.2 | beta | 2026-06-08 | CS | — |
| Freeze It and Heat It | `freeze-it-and-heat-it` | 4.1.1 | stable | 2026-09-19 | CS | — |
| Animal Genders | `animal-genders` | 1.2.0 | stable | 2026-03-26 | CS | — |
| Kaleidoscope Tavern | `kaleidoscopetavern` | 1.2.0-neoforge+mc1.21.1 | stable | 2026-07-01 | CS | — |
| Thirst Canteen | `thirst-canteen` | 1.21.1-3.7 | stable | 2026-07-17 | CS | — |
| Tide | `tide` | 2.1.1 | stable | 2026-08-03 | CS | **cloth-config** |
| Nutritional Balance | `nutritional-balance` | 1.21.1-7.0.3 | stable | 2026-04-25 | CS | — |
| Hardcore Revival | `hardcore-revival` | 21.1.22+neoforge-1.21.1 | stable | 2026-09-08 | CS | **balm** |
| AppleSkin | `appleskin` | 3.0.9+mc1.21 | stable | 2026-04-01 | CS | — |
| River Fishing | `river-fishing` | 1.0.0-1.21.1+NeoForge | stable | 2026-09-21 | CS | architectury-api |
| Kaleidoscope Contraption | `kaleidoscope-contraption` | 1.0.4 | stable | 2026-10-03 | CS | — |
| Kaleidoscope Orchard | `kaleidoscope-orchard` | 0.0.2 | beta | 2026-05-31 | CS | — |
| Fisherman's Trap | `fishermens-trap` | 3.1.0 | stable | 2025-04-13 | CS | — |
| Butchery | `butchery` | 5.3.1 | stable | 2026-10-09 | CS | — |
| Complicated Bees | `complicated-bees` | 1.21.1-3.6.2 | stable | 2026-09-30 | CS | — |
| Cosy Critters & Creepy Crawlies | `cosy-critters` | v0.3.3+1.21.1-neoforge | stable | 2026-07-07 | C | — |
| Enhanced Celestials | `enhanced-celestials` | 6.0.2.6-neoforge | stable | 2026-08-07 | CS | **corgilib**, **data-anchor** |
| Simple Voice Chat | `simple-voice-chat` | neoforge-1.21.1-2.6.24 | beta | 2026-09-20 | CS | — |
| Presence Not Position | `?` | **absent** | — | — | — | introuvable |
| Biome Music | `biome-music` | **absent** | — | — | — | seulement fabric 1.20.6, 1.21.1 |
| Particle Rain | `particle-rain` | v4.0.2+1.21.1-neoforge | stable | 2026-10-10 | C | — |
| Simple Clouds | `simple-clouds` | 0.7.3+1.21.1 | beta | 2025-08-26 | CS | — |
| Longwings | `longwings` | 0.9.5-neoforge | beta | 2026-05-02 | CS | — |
| Neruina | `neruina` | 3.3.3 | stable | 2026-07-17 | CS | **configurable** |
| Simple Backups | `simple-backups` | 1.21-4.0.30 | beta | 2026-04-22 | CS | — |
| JEI | `jei` | 19.57.0.451 | beta | 2026-10-05 | CS | **mezzconfig** |
| In Control! | `in-control` | 1.21-10.3.0 | beta | 2026-09-02 | S | — |
| LootJS | `lootjs` | 1.21.1-3.7.0+neoforge | stable | 2026-04-29 | S | kubejs |
| KubeJS Create | `kubejs-create` | 2101.3.1-build.18 | beta | 2025-11-12 | CS | kubejs, create |
| MoreJS | `morejs` | 1.21.1-0.16.0+neoforge | stable | 2025-10-13 | CS | kubejs |
| Almost Unified | `almostunified` | 1.21.1-1.4.2+neoforge | stable | 2026-05-22 | CS | — |
| Jade | `jade` | 15.10.6+neoforge | stable | 2026-08-06 | CS | — |
| Mouse Tweaks | `mouse-tweaks` | 1.21-2.26.1-neoforge | stable | 2024-08-17 | C | — |
| Controlling | `controlling` | 19.0.5 | stable | 2025-03-30 | C | **searchables** |
| Searchables | `searchables` | 1.0.2 | stable | 2024-10-21 | C | — |
| GraveStone Mod | `gravestone-mod` | neoforge-1.21.1-1.0.40 | stable | 2026-08-17 | CS | — |
| Crash Assistant | `crash-assistant` | 1.11.12 | stable | 2026-08-13 | C | — |
| Default Options | `default-options` | 21.1.8+neoforge-1.21.1 | stable | 2026-07-24 | C | **balm** |
| ProbeJS | `probejs` | 8.0.3 | stable | 2026-06-22 | CS | kubejs |
| EMI | `emi` | 1.1.24+1.21.1+neoforge | stable | 2026-05-13 | CS | — |
| Configured | `configured` | **absent** | — | — | — | seulement fabric 1.21.8, 1.21.10, 1.21.11, 26.1.2, 26.2, 26.3 |
| Packet Fixer | `packet-fixer` | 3.3.1 | stable | 2025-11-14 | CS | — |
| Better Advancements | `better-advancements` | 0.4.3.21 | beta | 2024-10-27 | C | — |
| GUI Compass | `gui-compass` | 1.21.1-5.0-fabric+forge+neo | stable | 2026-10-02 | C | **collective** |
| Create Villager Labor | `create-villager-labor` | 1.5.3.1-neoforge-1.21.1 | stable | 2026-09-28 | CS | create |
| Reactive Music | `reactive-music` | 1.3.5+1.21.1-neoforge | stable | 2026-07-29 | C | — |
| Create Aeronautics: Transmission & Linkage | `create-aeronautics-transmission-linkage` | 0.2.8 | stable | 2026-09-02 | CS | — |
| Kaleidoscope Grilling | `kaleidoscope-grilling` | 1.1.1 | stable | 2026-08-24 | CS | kaleidoscope-cookery |
| Aquatic Breeding | `aquatic-breeding` | 1.0.2+neoforge | stable | 2025-12-13 | CS | — |
| Nourished | `nourished` | 0.2.7-beta.2 | beta | 2026-10-02 | CS | **marieslib**, modonomicon, **cloth-config** |
| Sleep Quality | `sleepquality` | 2.0.1 | stable | 2025-08-04 | CS | — |
| Polymorph | `polymorph` | 1.2.0+1.21.1 | beta | 2026-09-10 | CS | — |
| First-person Model | `first-person-model` | 2.7.3 | stable | 2026-09-27 | C | **not-enough-animations** |
| Not Enough Animations | `not-enough-animations` | 1.12.6 | stable | 2026-09-19 | C | — |
| Shoulder Surfing Reloaded | `shoulder-surfing-reloaded` | 1.21.1-5.2.0+neoforge | stable | 2026-09-29 | C | — |
| FancyMenu | `fancymenu` | 3.9.14-1.21.1-neoforge | stable | 2026-09-28 | CS | **konkrete**, **melody** |
| Drippy Loading Screen | `drippy-loading-screen` | 3.1.5-1.21.1-neoforge | stable | 2026-08-01 | C | **konkrete**, **fancymenu**, **melody** |
| Sound Physics: Aeronautics | `sound-physics-aeronautics` | 2.0.1 | beta | 2026-09-27 | CS | — |
