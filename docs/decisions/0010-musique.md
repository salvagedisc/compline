# 0010 — Music

- **Date**: 2026-10-19
- **Status**: kept (team choice); 29 tracks chosen by ear by the team, not yet heard in game

## Context
The team wants music "in the style of what we are building, with beautiful transitions", without losing silence. The pack already plays vanilla music every 10 to 20 minutes, with three No Man's Land tracks; it lacks contexts (mist, clearing, season, moon, airship) and transitions. Full study: [music research](../archive/plan/recherche/2026-10-19/musique.md).

## Options
| Criterion | True Adaptive Music | Reactive Music | Music Moods + Ecliptic |
|---|---|---|---|
| Silence between tracks | Adjustable per situation, with a random share | 2 min at most, hard-coded | Vanilla delays, adjustable per biome and season |
| Contexts | 42 predicates, including biome and tag, height, boss, weather, and loot conditions evaluated by the server | Fixed events, biome, dimension; no moon, season or structure | Season, time of day, rain, biome |
| Music supplied | None: you choose | 61 imposed Celtic MP3s, 295 MB | None |
| NML and Ecliptic music | Kept through `vanillaMusic` nodes | Cut | Kept |
| Weight | 11.3 MB | 294.8 MB | 0.33 MB |

## Decision
**True Adaptive Music 2.9.3**, client and server side, with the `fallow` pack ([music](../plan/musique.md)):
- a tree of 11 situations, silences of 4 to 20 minutes, tracks played to the end;
- 29 tracks kept by the team after listening: 12 already in the game (vanilla and No Man's Land), 3 from the *Supernova* resource pack (Modrinth, CC-BY-NC-SA 4.0, free use permitted), 10 free tracks shipped in `fallow-ambiance` with their credits (Scott Buckley and Kevin MacLeod under CC-BY 4.0, HoliznaCC0 under CC0), normalised to −23 LUFS with fades;
- `musique.js` sets on the player the states the client cannot see (monument, moon, aboard); the seasons go through Ecliptic's loot condition;
- a single track on approaching a monument, nothing during boss fights;
- pack selected on first launch by Default Options, music at 40%.

Fallback if TAM causes problems: Music Moods for the fades, Ecliptic's `background_music` files, the `music` field of clearings (research, 2.5).

## Rejected
- Reactive Music: silence of 2 min at most, 295 MB of imposed Celtic music, cuts NML and Ecliptic music, no moon, season or structure condition.
- Music and Melody, The Immersive Music Mod: heavy (246 and 155 MB), copyrighted music, no moon or season.
- Music Triggers: alpha, Serene Seasons only.
- Mini Music Tweaks, Simple Music Control and similar: they shorten the silence.
