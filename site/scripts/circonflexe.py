"""Redraw the lowercase circumflex of Cormorant Garamond.

Cormorant draws it tall, narrow and slightly off the letter's centre, which reads as a misprint at display sizes
("Pêche"). This makes the accent of â ê î ô û lower and wider, centres it where the eye puts the middle of the letter
(along the slant in italics), and writes the result as the site's own copy of the font.
Usage: python3 scripts/circonflexe.py   (needs fonttools and brotli)
"""
import math
from pathlib import Path
from fontTools.ttLib import TTFont
from fontTools.ttLib.tables import ttProgram

RACINE = Path(__file__).resolve().parent.parent
SOURCE = RACINE / 'node_modules/@fontsource/cormorant-garamond/files'
CIBLE = RACINE / 'app/polices'
STYLES = ['300-normal', '400-normal', '500-normal', '400-italic']
LETTRES = [0xE2, 0xEA, 0xEE, 0xF4, 0xFB]  # â ê î ô û
LARGEUR, HAUTEUR = 1.3, 0.68  # the accent comes out wider and lower, closer to a Garamond circumflex


def contours(g):
    debut = 0
    for fin in g.endPtsOfContours:
        yield range(debut, fin + 1)
        debut = fin + 1


def recentrer(police: TTFont) -> list[str]:
    glyf, cmap = police['glyf'], police.getBestCmap()
    hauteur_x = police['OS/2'].sxHeight
    pente = math.tan(math.radians(-police['post'].italicAngle))
    faits = []
    for code in LETTRES:
        nom = cmap[code]
        g = glyf[nom]
        if g.isComposite() or g.numberOfContours <= 0:
            continue
        pts = g.coordinates
        accent, lettre = [], []
        for c in contours(g):
            (accent if min(pts[i][1] for i in c) > hauteur_x * 0.92 else lettre).extend(c)
        if not accent or not lettre:
            continue
        # Work upright: undo the italic slant, reshape, then slant back.
        droit = lambda x, y: x - pente * y
        sur = lambda idx: [i for i in idx if g.flags[i] & 1] or list(idx)
        ax = [droit(*pts[i]) for i in sur(accent)]
        ay0 = min(pts[i][1] for i in accent)
        centre_a = (min(ax) + max(ax)) / 2
        base = sur(lettre)
        if chr(code) in 'aeo':
            # Round letters: the eye reads their centre at the top of the bowl.
            haut = max(pts[i][1] for i in base)
            sommet = [droit(*pts[i]) for i in base if pts[i][1] >= haut - 2]
            lx = [droit(*pts[i]) for i in base]
            voulu = (sum(sommet) / len(sommet) + (min(lx) + max(lx)) / 2) / 2
        else:
            lx = [droit(*pts[i]) for i in base if pts[i][1] > hauteur_x * 0.6]
            voulu = (min(lx) + max(lx)) / 2
        for i in accent:
            x, y = pts[i]
            u = (droit(x, y) - centre_a) * LARGEUR + voulu
            v = ay0 + (y - ay0) * HAUTEUR
            pts[i] = (round(u + pente * v), round(v))
        g.program = ttProgram.Program()
        g.program.fromBytecode(b'')
        g.recalcBounds(glyf)
        faits.append(f'{chr(code)} {voulu - centre_a:+.0f}')
    return faits


def main():
    CIBLE.mkdir(exist_ok=True)
    for style in STYLES:
        police = TTFont(SOURCE / f'cormorant-garamond-latin-{style}.woff2')
        faits = recentrer(police)
        police.flavor = 'woff2'
        police.save(CIBLE / f'cormorant-{style}.woff2')
        print(style, ', '.join(faits))


if __name__ == '__main__':
    main()
