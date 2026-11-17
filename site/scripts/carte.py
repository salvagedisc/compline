"""Renders the disc map of the site from a biome and height grid sampled on the bench server.

Input: the JSON rows written by the bench command `cartedump <step> <row> <count>`
(one file per batch: {"pas", "n", "rows": [{"z", "h": [...], "b": [...]}]}).
Output: public/captures/carte-disque.png, one pixel per sample, transparent beyond the edge.

Usage: python3 scripts/carte.py <dump dir> <output png>
"""
import glob
import json
import math
import sys

from PIL import Image

EDGE = 6000  # blocks from the centre to the edge of the world
SEA = 63

# Colours by biome family, matched on words of the biome id, first match wins.
FAMILLES = [
    (('frozen_peaks', 'jagged_peaks', 'snowy_slopes', 'ice_spikes', 'frozen', 'glacier', 'icy'), (226, 232, 236)),
    (('snowy', 'tundra', 'grove', 'alpine'), (206, 216, 214)),
    (('stony_peaks', 'stony', 'rocky', 'cliff', 'mountain', 'peak', 'crag', 'slope'), (146, 146, 138)),
    (('badlands', 'mesa', 'red_rock', 'outback'), (196, 116, 70)),
    (('desert', 'dune', 'sand', 'arid', 'dry'), (222, 199, 140)),
    (('savanna', 'steppe', 'shrub', 'prairie', 'grassland', 'scrub'), (176, 170, 96)),
    (('swamp', 'marsh', 'bog', 'fen', 'mangrove', 'bayou', 'wetland'), (92, 112, 74)),
    (('jungle', 'rainforest', 'tropic'), (64, 128, 52)),
    (('cherry', 'blossom', 'magnolia'), (214, 160, 182)),
    (('taiga', 'spruce', 'pine', 'boreal', 'conifer', 'larch', 'fir'), (72, 104, 78)),
    (('maple', 'autumn', 'orchard', 'redwood'), (176, 108, 62)),
    (('birch', 'aspen', 'silver'), (130, 158, 92)),
    (('dark_forest', 'old_growth', 'deep', 'mystic', 'enchanted'), (58, 92, 58)),
    (('forest', 'wood', 'grove', 'thicket'), (86, 128, 66)),
    (('meadow', 'flower', 'field', 'plains', 'pasture', 'clover', 'lupin', 'poppy'), (134, 166, 86)),
    (('beach', 'shore', 'coast', 'gravel'), (220, 206, 160)),
    (('mushroom', 'fungal'), (150, 120, 140)),
]
DEFAUT = (120, 150, 84)


def couleur_biome(nom: str):
    court = nom.split(':', 1)[-1]
    for mots, c in FAMILLES:
        if any(m in court for m in mots):
            return c
    return DEFAUT


def charger(dossier: str):
    lignes = {}
    pas = n = None
    for f in glob.glob(f'{dossier}/rows_*.json'):
        d = json.load(open(f))
        pas, n = d['pas'], d['n']
        for r in d['rows']:
            lignes[int(r['z'])] = r
    zs = sorted(lignes)
    return pas, n, [lignes[z] for z in zs]


def main(dossier: str, sortie: str):
    pas, n, rows = charger(dossier)
    R = 6200
    H = [[r['h'][i] for i in range(n)] for r in rows]
    B = [[r['b'][i] for i in range(n)] for r in rows]
    h_rows = len(rows)
    img = Image.new('RGBA', (n, h_rows), (0, 0, 0, 0))
    px = img.load()
    for j in range(h_rows):
        z = -R + j * pas
        for i in range(n):
            x = -R + i * pas
            d = math.hypot(x, z)
            if d > EDGE:
                continue
            h = H[j][i]
            if h <= -999:
                continue
            if h < SEA - 1:
                # Water: lighter near the shore, deep blue offshore.
                prof = min(1.0, (SEA - h) / 40)
                c = (int(108 - 70 * prof), int(158 - 88 * prof), int(170 - 62 * prof))
            else:
                c = couleur_biome(B[j][i] or '')
                # Hillshade from the north-west.
                hw = H[j][max(0, i - 1)] if H[j][max(0, i - 1)] > -999 else h
                hn = H[max(0, j - 1)][i] if H[max(0, j - 1)][i] > -999 else h
                he = H[j][min(n - 1, i + 1)] if H[j][min(n - 1, i + 1)] > -999 else h
                hs = H[min(h_rows - 1, j + 1)][i] if H[min(h_rows - 1, j + 1)][i] > -999 else h
                dx = (he - hw) / (2 * pas)
                dz = (hs - hn) / (2 * pas)
                ombre = max(-0.45, min(0.45, -(dx + dz) * 2.2))
                alt = max(0.0, min(1.0, (h - SEA) / 160))
                k = 1 + ombre - 0.08 + alt * 0.12
                c = tuple(int(max(0, min(255, v * k))) for v in c)
                if h > 205:  # snow on high summits
                    c = tuple(int(v * 0.4 + 240 * 0.6) for v in c)
                if SEA - 1 <= h <= SEA + 1:
                    c = tuple(int(v * 0.6 + w * 0.4) for v, w in zip(c, (222, 206, 160)))
            # The last few hundred blocks fade into the mist of the edge.
            a = 255 if d < EDGE - 250 else int(255 * (EDGE - d) / 250)
            px[i, j] = (*c, a)
    img.save(sortie, optimize=True)
    print(sortie, img.size)


if __name__ == '__main__':
    main(sys.argv[1], sys.argv[2])
