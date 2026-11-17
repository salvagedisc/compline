# Compline website

The public showcase of the pack for friends: the disc map is the home page, each place on it opens one part of the pack (world, Brumaire, survival, cooking, fishing, metal, zoo), and two panels show the progress and the log. The site text is in French, like the game.

## Stack and commands

React 19 + TypeScript + Vite, `motion` for animation, self-hosted fonts (`@fontsource`). No backend: `npm run build` produces a static `dist/`.

| Command | Purpose |
|---|---|
| `npm install` | Install dependencies |
| `npm run dev` | Local server on http://localhost:5173 |
| `npm run build` | Type-check and build `dist/` |
| `python3 scripts/carte.py <dump dir> public/captures/carte-disque.png` | Render the disc map from a biome and height grid sampled on a test server |

## Layout

- `src/data/lieux.ts`: all the content (places, features, progress items, log). Most updates only touch this file.
- `src/components/`: the map and its mist (`Carte`, `Brume`), a place page (`LieuVue`), the media viewers (`Medias`: screenshot with lens and zooms, before/after slider, metal ladder, zoo stamps, measuring map), the progress and log panels (`Panneaux`), pixel icons (`Pixel`).
- `public/captures/`: screenshots taken in game, 1920 × 1080.

## Conventions

- Screenshots are taken in game with the full pack and Brumaire on, HUD hidden unless the HUD is the subject.
- Keyboard: 1 to 7 open the places, 8 the progress, 9 the log, Escape goes back to the map, ↑ ↓ change feature.
- The address keeps the open place (`#zoo`, `#avancement`) so a link can be shared.
