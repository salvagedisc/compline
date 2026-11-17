# Compline website

The public showcase of the pack for friends. The home page is the disc itself in 3D, rebuilt in voxels from a height and biome sample of a test world, under drifting mist. Each place on the disc opens one part of the pack (world, Brumaire, survival, cooking, fishing, metal, zoo) and each feature of a place has its own page; a journal and a road map complete it. The site text is in French, like the game.

## Stack and commands

React 19 + TypeScript, React Router 8 in framework mode with every page prerendered (`ssr: false`), Vite 8. 3D with three.js and React Three Fiber, UI motion with `motion`; titles come out of the mist letter by letter in plain CSS. Captures go through `vite-imagetools` (AVIF and WebP at three widths, blurred previews). Ambient sound is synthesised with the Web Audio API, nothing is downloaded. No backend: `npm run build` writes static files to `build/client/`.

| Command | Purpose |
|---|---|
| `npm install` | Install dependencies |
| `npm run dev` | Local server on http://localhost:5173 |
| `npm run typecheck` | Generate route types and type-check |
| `npm run build` | Build and prerender every page into `build/client/` |
| `python3 scripts/carte.py <dump dir> public/monde/carte-disque.png` | Render the flat disc map from a sample taken on a test server (`cartedump`) |
| `python3 scripts/relief.py <dump dir> public/monde/relief.bin` | Pack the same sample into the relief of the 3D disc |

## Layout

- `app/donnees/lieux.ts`: all the content (places, features, road map items, journal). Most updates only touch this file. `chemins.ts` derives the prerendered addresses from it.
- `app/routes/`: home, a place and its features (`salle.tsx`, `/cuisine` and `/cuisine/planche`), journal, road map, not found.
- `app/monde/`: the 3D disc (`Monde.tsx`), its relief builder (`relief.ts`) and shaders (mist, edge wall, sea, sky).
- `app/interface/`: header, hotbar, title screen, captures, media viewers, animated titles, sound, small shared state.
- `app/captures/`: screenshots taken in game, 1920 × 1080, optimised at build time.
- `public/monde/`: the relief grid and the flat map, served as is.

## Conventions

- Screenshots are taken in game with the full pack and Brumaire on, HUD hidden unless the HUD is the subject. Mark a stand-in screenshot `provisoire: true` until the real one exists.
- Keyboard: 1 to 9 follow the hotbar (seven places, journal, road map), ← → move between the features of a place, Escape goes up one level.
- Everything moving has a still version under `prefers-reduced-motion`; without WebGL the flat map stands in for the 3D disc.
- Deployment target: Cloudflare Workers static assets (`wrangler.jsonc`), nothing is published without the owner's approval.
