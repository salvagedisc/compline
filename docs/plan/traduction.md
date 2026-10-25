# French translation

Everything that is read in game is in French: items, blocks, tooltips, messages, achievements, configuration screens, books and guides. One of the players does not read English; this is a criterion for every mod added.

## How it works

- **Default language**: `pack/config/defaultoptions/options.txt` contains `lang:fr_fr`. Default Options applies it on first launch, when `options.txt` does not exist yet.
- **Our translations**: `pack/kubejs/assets/<namespace>/lang/fr_fr.json`. KubeJS loads `kubejs/assets/` as a resource pack, on top of the mods. Minecraft merges language files key by key: only the keys missing from the mod's `fr_fr.json` need translating, and a mod's translation can be corrected by redefining its key.
- **Books**: the `fr_fr` version follows the same paths as the mod's `en_us`, under `pack/kubejs/assets/<namespace>/`. Translated: the Alex's Mobs animal dictionary (91 pages), one page of the hamster guide (the others go through language keys), the Ecliptic Seasons chronicle of the seasons (9 files). The Icy's Better Horses manual and the Ancient Remnants codex go entirely through language keys.
- **Check**: `npm run check-fr` (also run by CI) reads the pack's jars, including nested jars. It fails if an English text has no French translation, if a file is not valid JSON, or if a translation loses a variable (`%s`, `%1$s`, `{0}`, `§x`).

## Adding a mod

1. Run `npm run check-fr`: it lists the namespaces with missing texts.
2. Write the missing keys in `pack/kubejs/assets/<namespace>/lang/fr_fr.json`, in the order of the `en_us`.
3. Translate its books if it has any (`patchouli_books/…/en_us/`, `book/…/en_us/`).
4. Rerun `npm run check-fr` until it reports 0.

## Glossary

The same word for the same thing throughout the pack. Vanilla terms follow the official French of Minecraft Java.

| English | French |
|---|---|
| backpack | sac à dos |
| blueprint | plan |
| smithing, forge | forge |
| quench | trempe |
| season | saison |
| thirst, purified water | soif, eau purifiée |
| bestiary | bestiaire |
| airship | dirigeable |
| contraption (Aeronautics, Simulated) | engin |
| stress, kinetic (Create) | stress, cinétique, as in Create's official translation |
| monolith, blessing | monolithe, bénédiction |

Style: short sentences, formal "vous" or impersonal form in tooltips and configuration. The informal "tu" stays where the mod already used it in French (hamster guide, messages, achievements). Proper names of mods and music titles are not translated.

## What stays in English or needs watching

- **Longwings**: 4 messages hard-coded in the code ("New Discovery!", "Check your handbook", "Your handbook has been updated!", "This isnt functional yet"). Only a fix to the mod itself would translate them.
- **Envelope**: the descriptions of the configuration options, the message "Mail Service does not operate in this dimension." and the command messages are hard-coded in the code.
- **No Man's Land**: the Friendly Moon dialogues are in `assets/nomansland/lang/nomansland/dialogue_pools/<pool>/fr_fr.json`, outside the usual language files. We provide a complete copy of the `offering` pool, which was missing 9 dialogues. `npm run check-fr` does not check these files: to be reviewed at every NML version upgrade.
- **Ancient Remnants**: the codex title font has no accented letters. `pack/kubejs/assets/ancient_remnants/font/header.json` adds the default font as a fallback: accented letters are displayed, in the vanilla style. To be checked in game.
- **Quark**: its translation calls the "blossom" wood "cerisier" (cherry), like the vanilla cherry tree. The two woods therefore carry the same name.
- **Naturalist and Alex's Mobs**: Naturalist's rattlesnake keeps that name; Alex's Mobs says "Crotale".
- **Every Compat**: the wood names of the other mods (`wood_type.*`, `leaves_type.*`) are defined in the `everycomp` file, since no mod provides them.
- The pages of translated books may lay out slightly differently, French being longer. To be checked in game.
