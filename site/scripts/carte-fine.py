"""Builds the two textures of the 3D disc from the bench sample (one sample every 16 blocks).

Input: the JSON rows written by the bench command `cartedump 16 <row> <count>`.
Output, in the given folder:
  couleurs.webp ground colour, muted natural palette, soft hillshade baked in, transparent beyond the edge
  hauteurs.png  height, 0 = 40 blocks under sea level, 255 = 260 blocks above it, smoothed

Usage: python3 scripts/carte-fine.py <dump dir> <output dir>
"""
import colorsys
import os
import sys

import numpy as np
from PIL import Image

from carte import EDGE, SEA, charger, couleur_biome

BAS, HAUT = SEA - 40, SEA + 260


def flou(a: np.ndarray, r: int) -> np.ndarray:
    """Box blur of radius r, run twice (close to a gaussian), edges clamped."""
    for _ in range(2):
        p = np.pad(a, r, mode='edge')
        c = p.cumsum(0)
        c = np.vstack([np.zeros((1, c.shape[1])), c])
        a = (c[2 * r + 1:] - c[:-2 * r - 1]) / (2 * r + 1)
        a = a[:, r:-r] if r else a
        p = np.pad(a, ((0, 0), (r, r)), mode='edge')
        c = p.cumsum(1)
        c = np.hstack([np.zeros((c.shape[0], 1)), c])
        a = (c[:, 2 * r + 1:] - c[:, :-2 * r - 1]) / (2 * r + 1)
    return a


def adoucir(c):
    """Pulls a biome colour towards a muted, natural tone so the map reads as land seen from the sky."""
    h, l, s = colorsys.rgb_to_hls(*(v / 255 for v in c))
    s *= 0.5
    l = 0.2 + l * 0.66
    # Pinks, purples and oranges are the loudest patches: pull them further down.
    if h > 0.75 or h < 0.1:
        s *= 0.7
    # Greens drift towards olive, the way foliage looks from altitude in the evening.
    if 0.18 < h < 0.45:
        h = h * 0.85 + 0.2 * 0.15
    r, g, b = colorsys.hls_to_rgb(h, l, s)
    return np.array([r, g, b]) * 255


def main(dossier: str, sortie: str):
    pas, n, rows = charger(dossier)
    R = 6200
    H = np.array([r['h'] for r in rows], dtype=float)
    B = [r['b'] for r in rows]
    m = H.shape[0]
    zs = -R + np.arange(m) * pas
    xs = -R + np.arange(n) * pas
    X, Z = np.meshgrid(xs, zs)
    D = np.hypot(X, Z)
    vide = H <= -999
    H[vide] = SEA - 20
    eau = H < SEA - 1

    # Heights: land keeps its relief, water takes its depth; smoothed so single blocks do not speckle.
    Hl = flou(H, 1)

    # Colours.
    cache = {}
    C = np.zeros((m, n, 3))
    for j in range(m):
        for i in range(n):
            nom = B[j][i] or ''
            if nom not in cache:
                cache[nom] = adoucir(couleur_biome(nom))
            C[j, i] = cache[nom]

    # Biome borders melt into each other over a few samples, the way they read from the air.
    for k in range(3):
        C[..., k] = flou(C[..., k], 2)

    # Snow settles on high, gentle ground; steep faces stay rock. Never fully white.
    alt = np.clip((Hl - SEA) / 200, 0, 1)
    Hp = flou(H, 1)
    pente = np.hypot(np.gradient(Hp, axis=1), np.gradient(Hp, axis=0)) / pas
    plat = np.clip(1 - pente / 1.4, 0, 1)
    neige = (np.clip((Hl - 195) / 70, 0, 1) ** 1.4 * (0.22 + 0.45 * plat))
    neige = flou(neige, 1)[..., None]
    roche = np.clip((Hl - 135) / 70, 0, 1)[..., None] * 0.5
    C = C * (1 - roche) + np.array([116, 114, 108]) * roche
    C = C * (1 - neige) + np.array([214, 218, 222]) * neige

    # Hillshade: a low evening sun from the west-north-west plus a soft sky light, on smoothed heights.
    Hs = flou(H, 2)
    gx = np.gradient(Hs, axis=1) / pas * 2.2
    gz = np.gradient(Hs, axis=0) / pas * 2.2
    nrm = np.dstack([-gx, np.ones_like(gx), -gz])
    nrm /= np.linalg.norm(nrm, axis=2, keepdims=True)
    soleil = np.array([-0.62, 0.55, -0.32]); soleil /= np.linalg.norm(soleil)
    lum = np.clip((nrm * soleil).sum(2), 0, 1)
    ciel = nrm[..., 1]
    k = 0.58 + 0.52 * lum + 0.12 * (ciel - 0.9) + 0.06 * alt
    # Valleys a touch darker than ridges (cheap ambient occlusion).
    ao = np.clip((Hs - flou(Hs, 6)) / 12, -1, 1)
    k = k + ao * 0.08
    k = np.minimum(k, 1.06)
    C = C * k[..., None]

    # Water: shallow and clear near the shore, deep and dark offshore, a thin light line on the beach.
    prof = np.clip((SEA - H) / 40, 0, 1) ** 0.7
    peu = np.array([70, 112, 116]); fond = np.array([16, 34, 46])
    Ce = peu * (1 - prof[..., None]) + fond * prof[..., None]
    we = flou(eau.astype(float), 1)
    sable = np.clip(we * 3, 0, 1) * (~eau) * 0.35
    C = C * (1 - sable[..., None]) + np.array([196, 186, 150]) * sable[..., None]
    melange = np.clip(flou(eau.astype(float), 1) * 1.6 - 0.3, 0, 1)[..., None]
    C = C * (1 - melange) + Ce * melange

    # The last few hundred blocks fade out; the 3D mist wall takes over from there.
    A = np.clip((EDGE + 120 - D) / 300, 0, 1)
    A[vide & (D > EDGE)] = 0

    img = np.dstack([np.clip(C, 0, 255), A * 255]).astype(np.uint8)
    os.makedirs(sortie, exist_ok=True)
    Image.fromarray(img, 'RGBA').save(f'{sortie}/couleurs.webp', quality=88, method=6)

    h8 = np.clip((Hl - BAS) / (HAUT - BAS), 0, 1) * 255
    h8[D > EDGE + 100] = 0
    Image.fromarray(h8.astype(np.uint8), 'L').save(f'{sortie}/hauteurs.png', optimize=True)
    print(sortie, n, 'x', m)


if __name__ == '__main__':
    main(sys.argv[1], sys.argv[2])
