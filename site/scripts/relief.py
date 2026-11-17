"""Packs the bench height and biome sample into the relief the 3D disc is built from.

Input: the same `cartedump` rows as carte.py. Output: a binary grid of N x N cells, four bytes each:
height code (0 = nothing, else (y + 64) / 1.5 + 1), then the flat biome or water colour as R, G, B.
Cells are taken every `saut` samples, rows from north (-z) to south, columns from west to east.

Usage: python3 scripts/relief.py <dump dir> <output .bin> [saut]
"""
import math
import sys

from carte import EDGE, SEA, charger, couleur_biome


def main(dossier: str, sortie: str, saut: int = 3):
    pas, n, rows = charger(dossier)
    R = 6200
    out = bytearray()
    idx = range(0, n, saut)
    for j in idx:
        r = rows[j]
        z = -R + j * pas
        for i in idx:
            x = -R + i * pas
            h = r['h'][i]
            if math.hypot(x, z) > EDGE + 150 or h <= -999:
                out += b'\0\0\0\0'
                continue
            if h < SEA - 1:
                prof = min(1.0, (SEA - h) / 36) ** 0.8
                c = (int(92 - 62 * prof), int(150 - 86 * prof), int(178 - 72 * prof))
            else:
                c = couleur_biome(r['b'][i] or '')
                if h > 205:
                    c = tuple(int(v * 0.4 + 240 * 0.6) for v in c)
            code = max(1, min(255, int(round((h + 64) / 1.5)) + 1))
            out += bytes((code, *c))
    open(sortie, 'wb').write(out)
    print(sortie, len(idx), 'x', len(idx), len(out), 'bytes')


if __name__ == '__main__':
    main(sys.argv[1], sys.argv[2], int(sys.argv[3]) if len(sys.argv) > 3 else 3)
