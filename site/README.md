# Compline website

The public showcase of the pack for friends. The home page is the disc seen from the evening sky, its relief and colours sampled on a test world, under drifting clouds; a title screen lifts the mist. Each of the seven places pinned on the disc (world, Brumaire, survival, cooking, fishing, metal, zoo) is told as a short film: a title card, then one full-screen chapter per feature, with details marked inside the screenshot itself. A full-screen index, a journal and a road map complete it. The site text is in French, like the game.

## Stack and commands

React 19 + TypeScript, React Router 8 in framework mode with every page prerendered (`ssr: false`), Vite 8. 3D with three.js and React Three Fiber (custom shaders for ground, sea, mist ring and cloud layers); every interface motion is plain CSS. Type: Cormorant Garamond and Cormorant SC for titles, Spectral for reading. Icons are hand-drawn line SVG (`app/interface/icones.tsx`). Captures go through `vite-imagetools` (AVIF and WebP at three widths, blurred previews). Ambient sound is synthesised with the Web Audio API, nothing is downloaded. No backend: `npm run build` writes static files to `build/client/`.

| Command | Purpose |
|---|---|
| `npm install` | Install dependencies |
| `npm run dev` | Local server on http://localhost:5173 |
| `npm run typecheck` | Generate route types and type-check |
| `npm run build` | Build and prerender every page into `build/client/` |
| `cd scripts && python3 carte-fine.py <dump dir> ../public/monde` | Build the colour and height textures of the disc from a 16-block sample taken on a test server (`cartedump 16 …`) |
| `python3 scripts/nuages.py /tmp/n.png` | Build the tileable cloud noise (then save it as `public/monde/nuages.webp`) |

## Layout

- `app/donnees/lieux.ts`: all the content (places, features, road map items, journal). Most updates only touch this file. `chemins.ts` derives the prerendered addresses from it.
- `app/donnees/details.ts`: content of the special chapters (metal tiers, zoo stamps and families).
- `app/routes/`: home, a place and its chapters (`salle.tsx`, `/cuisine/` and `/cuisine/planche/`), journal, road map, not found.
- `app/monde/Ciel.tsx`: the 3D sky, its shaders and the camera (drift over the disc, dive onto a place).
- `app/interface/`: header, place pins, index, title screen, captures, icons, sound, small shared state; `scenes/` holds the full-screen chapter views (still with details, before/after, metal tiers, zoo page, measured map).
- `app/tampons/`: the zoo stamps and family emblems, cut from the guide screenshot.
- `app/captures/`: screenshots taken in game, 1920 × 1080, optimised at build time.
- `public/monde/`: colour, height and cloud textures of the 3D sky, served as is (the colour map is also the flat map without WebGL).

## Conventions

- Screenshots are taken in game with the full pack and Brumaire on, HUD hidden unless the HUD is the subject. Mark a stand-in screenshot `provisoire: true` until the real one exists.
- Keyboard: 1 to 7 open the places, J the journal, R the road map, I the index; ← → move along the chapters (and on to the next place), Escape goes back to the disc.
- Everything moving has a still version under `prefers-reduced-motion`; without WebGL the flat map stands in for the 3D disc.
- Deployment target: Cloudflare Workers static assets (`wrangler.jsonc`), nothing is published without the owner's approval.
