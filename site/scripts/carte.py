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

# Exact colours for biomes whose names would mislead the keyword match below.
EXACTS = {
    'mangrove_swamp': (78, 104, 64), 'cherry_grove': (214, 160, 182), 'wisteria_grove': (170, 150, 200),
    'magnolia_woodland': (206, 168, 186), 'frozen_pine_taiga': (196, 208, 204), 'snowy_taiga': (190, 204, 200),
    'snowy_beach': (226, 228, 222), 'stony_shore': (150, 148, 140), 'rocky_shore': (150, 148, 140),
    'windswept_hills': (128, 140, 112), 'windswept_gravelly_hills': (144, 142, 134), 'windswept_forest': (84, 118, 76),
    'tide_pools': (170, 176, 150), 'ant_gardens': (150, 160, 86), 'dripstone_caves': (146, 132, 112),
    'pine_slopes': (96, 120, 96), 'towering_cliffs': (140, 138, 128), 'dry_bushland': (190, 172, 106),
    'bamboo_forest': (98, 146, 66), 'willow_forest': (92, 132, 82), 'redwoods': (128, 92, 62),
    'sparse_redwoods': (138, 112, 72), 'orchard': (150, 156, 80),
}

# Colours by biome family, matched on words of the biome id, first match wins.
FAMILLES = [
    (('frozen_peaks', 'jagged_peaks', 'snowy_slopes', 'ice_spikes', 'frozen', 'glacier', 'icy'), (230, 236, 240)),
    (('snowy', 'tundra', 'alpine', 'grove'), (206, 216, 214)),
    (('stony_peaks', 'stony', 'rocky', 'cliff', 'mountain', 'peak', 'crag', 'slope'), (146, 146, 138)),
    (('badlands', 'mesa', 'red_rock', 'outback'), (188, 112, 68)),
    (('desert', 'dune', 'sand', 'arid', 'dry'), (222, 199, 140)),
    (('savanna', 'steppe', 'shrub', 'prairie', 'grassland', 'scrub'), (170, 166, 94)),
    (('swamp', 'marsh', 'bog', 'fen', 'bayou', 'wetland'), (88, 108, 72)),
    (('jungle', 'rainforest', 'tropic'), (58, 120, 50)),
    (('cherry', 'blossom'), (214, 160, 182)),
    (('taiga', 'spruce', 'pine', 'boreal', 'conifer', 'larch', 'fir'), (70, 100, 76)),
    (('maple', 'autumn'), (172, 104, 60)),
    (('birch', 'aspen', 'silver'), (126, 154, 90)),
    (('dark_forest', 'old_growth', 'deep', 'mystic', 'enchanted'), (56, 88, 56)),
    (('forest', 'wood', 'thicket'), (84, 124, 64)),
    (('meadow', 'flower', 'field', 'plains', 'pasture', 'clover', 'lupin', 'poppy'), (132, 162, 84)),
    (('beach', 'shore', 'coast', 'gravel'), (220, 206, 160)),
    (('mushroom', 'fungal'), (150, 120, 140)),
    (('river', 'delta'), (104, 150, 168)),
]
DEFAUT = (120, 150, 84)


def couleur_biome(nom: str):
    court = nom.split(':', 1)[-1]
    if court in EXACTS:
        return EXACTS[court]
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


def lisser(H, n, rows):
    """3x3 mean of the valid heights, used only for the hillshade so single-block noise does not speckle."""
    L = [[0.0] * n for _ in range(rows)]
    for j in range(rows):
        for i in range(n):
            t = c = 0
            for dj in (-1, 0, 1):
                for di in (-1, 0, 1):
                    jj, ii = j + dj, i + di
                    if 0 <= jj < rows and 0 <= ii < n and H[jj][ii] > -999:
                        t += H[jj][ii]
                        c += 1
            L[j][i] = t / c if c else SEA
    return L


def main(dossier: str, sortie: str):
    pas, n, rows = charger(dossier)
    R = 6200
    H = [[r['h'][i] for i in range(n)] for r in rows]
    B = [[r['b'][i] for i in range(n)] for r in rows]
    h_rows = len(rows)
    L = lisser(H, n, h_rows)
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
                # Water: clear blue in rivers and shallows, deep blue offshore.
                prof = min(1.0, (SEA - h) / 36) ** 0.8
                c = (int(92 - 62 * prof), int(150 - 86 * prof), int(178 - 72 * prof))
            else:
                c = couleur_biome(B[j][i] or '')
                # Hillshade from the north-west, on smoothed heights.
                dx = (L[j][min(n - 1, i + 1)] - L[j][max(0, i - 1)]) / (2 * pas)
                dz = (L[min(h_rows - 1, j + 1)][i] - L[max(0, j - 1)][i]) / (2 * pas)
                ombre = max(-0.32, min(0.32, -(dx + dz) * 1.7))
                alt = max(0.0, min(1.0, (h - SEA) / 160))
                k = 1 + ombre - 0.06 + alt * 0.1
                c = tuple(int(max(0, min(255, v * k))) for v in c)
                if h > 205:  # snow on high summits
                    c = tuple(int(v * 0.4 + 240 * 0.6) for v in c)
                if SEA - 1 <= h <= SEA + 1:
                    c = tuple(int(v * 0.7 + w * 0.3) for v, w in zip(c, (222, 206, 160)))
            # The last few hundred blocks fade into the mist of the edge.
            a = 255 if d < EDGE - 250 else int(255 * (EDGE - d) / 250)
            px[i, j] = (*c, a)
    img.save(sortie, optimize=True)
    print(sortie, img.size)


if __name__ == '__main__':
    main(sys.argv[1], sys.argv[2])
