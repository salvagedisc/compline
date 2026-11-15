"""Images du zoo dans le guide d'Archie's Dinosaurs, dessinées pixel par pixel (aucune image du mod n'est reprise).

Elles suivent les règles de dessin du guide, relevées sur ses planches : un texel par unité du livre (le livre
mesure 576 x 320), des diagonales à marches d'un pixel, un bord clair en haut et sombre en bas, un tissu tramé
pour les rubans et des pictogrammes blancs. Écrit dans
mods-custom/compline/src/main/resources/assets/compline/textures/gui/zoo/ :
- tampons.png (136 x 180) : les quatre tampons (Apercevoir, Photographier, Apprivoiser ou héberger, Maîtriser),
  losanges de 34 x 36 comme ceux d'Archie ; ligne 0 en relief sépia (pas encore obtenus), lignes 1 à 4 aux
  couleurs des milieux (terre, eaux, ciel, créatures) ;
- cadre.png (34 x 36) : le cadre des portraits, de la même facture que les tampons ;
- marque.png (64 x 48) : le marque-page du bas, 32 x 48 comme ceux d'Archie, normal puis survolé ;
- onglets.png (96 x 120) : les onglets de gauche (accueil puis les quatre milieux), 48 x 24, normaux puis survolés.

Lancer depuis la racine du dépôt : python3 scripts/zoo/textures.py (Pillow).
"""
import random
from pathlib import Path

from PIL import Image

OUT = Path(__file__).resolve().parents[2] / "mods-custom/compline/src/main/resources/assets/compline/textures/gui/zoo"

CLEAR = (0, 0, 0, 0)
WHITE = (255, 255, 255, 255)

# Papier et encres du cadre des tampons (haut éclairé, bas dans l'ombre)
EDGE_TOP = (172, 139, 97)
EDGE_BOTTOM = (146, 115, 70)
LIGHT = (236, 219, 190)
PAPER = (211, 185, 145)
PAPER_SHADE = (200, 172, 131)
INNER_TOP = (172, 139, 97)
INNER_LIGHT = (224, 204, 170)
INNER_BOTTOM = (214, 192, 156)
FILL_OFF = ((206, 181, 142), (199, 173, 133), (191, 164, 123))

# Milieux : (clair, moyen, sombre) pour le fond des tampons obtenus
HABITATS = [
    ((156, 198, 112), (112, 160, 78), (78, 122, 54)),    # terre
    ((136, 186, 226), (84, 140, 192), (56, 102, 156)),   # eaux
    ((200, 228, 244), (148, 196, 228), (104, 156, 200)),  # ciel
    ((206, 150, 228), (166, 104, 196), (120, 70, 150)),  # créatures
]

# Icônes des tampons, 16 x 16, une lettre par couleur
MAGNIFIER = [
    "................",
    "....oooo........",
    "..oorrrroo......",
    "..orwwggrro.....",
    ".orwgggggRo.....",
    ".orggggggRo.....",
    ".orgggggGRo.....",
    ".orggggGGRo.....",
    "..orGGGGRoo.....",
    "..ooRRRRoHho....",
    "....oooo.oHho...",
    ".........oHhho..",
    "..........oHhho.",
    "...........oHho.",
    "............oo..",
    "................",
]
CAMERA = [
    "................",
    "................",
    "................",
    "...oooo....oo...",
    "..oSSSSo..oRRo..",
    ".oooooooooooooo.",
    ".oBBBBooooBBBBo.",
    ".obbbowllLobbbo.",
    ".obbbollllobbbo.",
    ".obbbollLLobbbo.",
    ".obbbolLLLobbbo.",
    ".okkkkookkkkkko.",
    ".oooooooooooooo.",
    "................",
    "................",
    "................",
]
HEART = [
    "................",
    "................",
    "..oooo....oooo..",
    ".ohhrro..orrrro.",
    "ohhrrrroorrrrrRo",
    "ohrrrrrrrrrrrrRo",
    "orrrrrrrrrrrrrRo",
    ".orrrrrrrrrrrRo.",
    "..orrrrrrrrrRo..",
    "...orrrrrrrRo...",
    "....orrrrRRo....",
    ".....orrRRo.....",
    "......oRRo......",
    ".......oo.......",
    "................",
    "................",
]
MEDAL = [
    "......oooo......",
    "....ooYYYYoo....",
    "....oYYyyydo....",
    "...oYyyYYyydo...",
    "...oYyYYYYydo...",
    "...oYyyYYyydo...",
    "...oyyyyyyddo...",
    "....oyyyyddo....",
    "....ooydddoo....",
    "......oooo......",
    "....oBBoobbo....",
    "....oBBoobbo....",
    "....oBBoobbo....",
    "....oBo..obo....",
    "....oo....oo....",
    "................",
]
ICONS = [MAGNIFIER, CAMERA, HEART, MEDAL]
ICON_COLORS = {
    "o": (44, 30, 16),
    "w": (255, 255, 255), "g": (200, 228, 246), "G": (138, 184, 224),
    "r": (222, 64, 64), "R": (164, 38, 44), "h": (255, 168, 160), "H": (98, 62, 30),
    "S": (204, 204, 196), "B": (116, 92, 74), "b": (86, 66, 52), "k": (62, 46, 36),
    "l": (172, 212, 238), "L": (92, 142, 192),
    "Y": (255, 238, 150), "y": (238, 194, 66), "d": (190, 136, 34), "p": (140, 90, 22),
}
# Les icônes de la loupe : cercle cuivré et manche de bois, couleurs propres
ICON_OVERRIDES = {0: {"r": (222, 176, 96), "R": (160, 108, 50), "h": (150, 96, 48)}, 3: {"B": (78, 118, 196), "b": (58, 88, 160)}}
# En relief sépia : contour, clair, moyen, sombre
SEPIA = {"o": (134, 102, 62), "light": (238, 222, 194), "mid": (214, 191, 154), "dark": (172, 141, 99)}
SEPIA_LIGHT = set("wgYhSl")
SEPIA_DARK = set("RGdkLHbpBr")

# Pictogrammes blancs des onglets (12 x 12) et du marque-page (16 x 16)
TAB_GLYPHS = [
    [  # accueil : patte
        "............",
        "............",
        "...oo..oo...",
        "...oo..oo...",
        ".o........o.",
        "oo..oooo..oo",
        "...oooooo...",
        "..oooooooo..",
        "..oooooooo..",
        "...oo..oo...",
        "............",
        "............",
    ],
    [  # terre : feuille
        "..........oo",
        "........oooo",
        "......oooo.o",
        ".....ooo.ooo",
        "....ooo.oooo",
        "...ooo.oooo.",
        "...oo.oooo..",
        "..oo.oooo...",
        "..o.oooo....",
        ".o.ooo......",
        "o...........",
        "............",
    ],
    [  # eaux : poisson
        "............",
        "............",
        "...oooo....o",
        ".oooooooo.oo",
        "oo.ooooooooo",
        "oooooooooo..",
        "oooooooooo..",
        "oooooooooooo",
        ".oooooooo.oo",
        "...oooo....o",
        "............",
        "............",
    ],
    [  # ciel : oiseau en vol
        "............",
        "............",
        "o..........o",
        "oo........oo",
        ".oo......oo.",
        "..ooo..ooo..",
        "...oooooo...",
        "....oooo....",
        ".....oo.....",
        "............",
        "............",
        "............",
    ],
    [  # créatures : griffures
        "............",
        "...o...o...o",
        "..oo..oo..oo",
        "..oo..oo..oo",
        ".oo..oo..oo.",
        ".oo..oo..oo.",
        ".oo..oo..oo.",
        "oo..oo..oo..",
        "oo..oo..oo..",
        "o...o...o...",
        "............",
        "............",
    ],
]
PAW = [
    "................",
    "....oo....oo....",
    "...oooo..oooo...",
    "...oooo..oooo...",
    ".oo.oo....oo.oo.",
    "oooo........oooo",
    "oooo..oooo..oooo",
    ".oo..oooooo..oo.",
    ".....oooooo.....",
    "....oooooooo....",
    "...oooooooooo...",
    "...oooooooooo...",
    "...oooooooooo...",
    "....oooooooo....",
    "....ooo..ooo....",
    "................",
]

# Rubans : cuir de l'accueil puis milieux, en tons doux comme ceux du livre (clair -> sombre)
RIBBONS = [
    (146, 112, 92),   # accueil
    (110, 140, 84),   # terre
    (82, 124, 160),   # eaux
    (120, 158, 188),  # ciel
    (128, 94, 150),   # créatures
]
BOOKMARK = (96, 138, 82)


def rgba(color):
    return color + (255,) if len(color) == 3 else color


def mix(a, b, t):
    return tuple(round(a[i] + (b[i] - a[i]) * t) for i in range(3))


def diamond_ring(x, y):
    """Anneau d'un pixel du losange de 34 : 17 au bord, 0 au centre."""
    return int(abs(x + 0.5 - 17) + abs(y + 0.5 - 17))


def draw_tile(img, ox, oy, fill, inner, shaped):
    """Un losange (shaped) ou un carré de 34 x 34, avec son ombre portée de 2 pixels sous lui."""
    def inside(x, y):
        return diamond_ring(x, y) <= 17 if shaped else 0 <= x < 34 and 0 <= y < 34

    def ring(x, y):
        return diamond_ring(x, y) if shaped else 17 - min(x, y, 33 - x, 33 - y)

    for y in range(34):
        for x in range(34):
            if inside(x, y):
                img.putpixel((ox + x, oy + y + 2), rgba(PAPER_SHADE))
    for y in range(34):
        for x in range(34):
            if not inside(x, y):
                continue
            r = ring(x, y)
            top = (y + 0.5) < 17 if shaped else (x + y) < 33
            if r == 17:
                color = EDGE_TOP if top else EDGE_BOTTOM
            elif r == 16:
                color = LIGHT if top else PAPER
            elif r >= 14:
                color = PAPER
            elif r == 13:
                color = inner[2] if not top else inner[0]
            elif r == 12:
                color = inner[1] if top else inner[3]
            else:
                if fill is None:
                    continue
                s = x + y
                color = fill[0] if s < 30 else fill[1] if s < 40 else fill[2]
            img.putpixel((ox + x, oy + y), rgba(color))


def draw_icon(img, ox, oy, rows, colors):
    for y, row in enumerate(rows):
        for x, ch in enumerate(row):
            if ch != ".":
                img.putpixel((ox + x, oy + y), rgba(colors(ch)))


def sepia(ch):
    if ch == "o":
        return SEPIA["o"]
    if ch in SEPIA_LIGHT:
        return SEPIA["light"]
    if ch in SEPIA_DARK:
        return SEPIA["dark"]
    return SEPIA["mid"]


def stamps():
    img = Image.new("RGBA", (136, 180), CLEAR)
    for row in range(5):
        if row == 0:
            fill = FILL_OFF
            inner = (INNER_TOP, INNER_LIGHT, EDGE_BOTTOM, INNER_BOTTOM)
        else:
            light, mid, dark = HABITATS[row - 1]
            fill = (light, mid, dark)
            inner = (mix(dark, (40, 28, 16), 0.3), mix(light, (255, 255, 255), 0.35), mix(dark, (40, 28, 16), 0.45), light)
        for col, icon in enumerate(ICONS):
            ox, oy = col * 34, row * 36
            draw_tile(img, ox, oy, fill, inner, True)
            if row == 0:
                draw_icon(img, ox + 9, oy + 9, icon, sepia)
            else:
                palette = dict(ICON_COLORS)
                palette.update(ICON_OVERRIDES.get(col, {}))
                draw_icon(img, ox + 9, oy + 9, icon, lambda ch: palette[ch])
    return img


def frame():
    img = Image.new("RGBA", (34, 36), CLEAR)
    draw_tile(img, 0, 0, ((228, 210, 178), (224, 205, 172), (219, 199, 165)),
              (INNER_TOP, INNER_LIGHT, EDGE_BOTTOM, INNER_BOTTOM), False)
    return img


def dither(rng, color, amount=6):
    d = rng.choice((-amount, 0, 0, 0, amount))
    return tuple(max(0, min(255, c + d)) for c in color)


def bookmark():
    """Ruban de 32 x 48 : le haut passe sous le livre, le bas finit en queue d'aronde."""
    img = Image.new("RGBA", (64, 48), CLEAR)
    rng = random.Random(7)
    base = BOOKMARK
    dark = mix(base, (24, 20, 16), 0.35)
    edge = mix(base, (255, 255, 255), 0.12)
    for state in range(2):
        ox = state * 32
        for y in range(48):
            for x in range(32):
                # queue d'aronde : encoche en V au milieu du bas, pente de 1 sur 4
                if y >= 43 and abs(x + 0.5 - 16) < (y - 42) * 4 - 1:
                    continue
                if y >= 43 and abs(x + 0.5 - 16) < (y - 42) * 4 + 1:
                    color = dark
                elif y < 21:
                    color = mix(base, (24, 20, 16), 0.08)
                elif x in (0, 31):
                    color = dark
                elif x in (1, 30):
                    color = edge
                else:
                    color = dither(rng, base)
                img.putpixel((ox + x, y), rgba(color))
        draw_icon(img, ox + 8, 24, PAW, lambda ch: WHITE)
        if state == 1:
            for y in range(21, 48):
                for x in (0, 31):
                    if img.getpixel((ox + x, y))[3]:
                        img.putpixel((ox + x, y), WHITE)
            for x in range(32):
                for y in range(47, 20, -1):
                    if img.getpixel((ox + x, y))[3]:
                        img.putpixel((ox + x, y), WHITE)
                        break
    return img


def side_tabs():
    """Onglets de 48 x 24 : pointe à gauche en marches d'un pixel, cuir dégradé vers la droite, sombre sous le livre."""
    img = Image.new("RGBA", (96, 120), CLEAR)
    rng = random.Random(11)
    for row, base in enumerate(RIBBONS):
        tones = [mix(base, (255, 240, 220), 0.12), base, mix(base, (30, 22, 20), 0.15),
                 mix(base, (30, 22, 20), 0.3), mix(base, (30, 22, 20), 0.45), mix(base, (30, 22, 20), 0.6)]
        for state in range(2):
            ox, oy = state * 48, row * 24
            for y in range(24):
                left = 11 - y if y < 12 else y - 12
                for x in range(left, 48):
                    if y == 0 or (y < 12 and x == left):
                        color = tones[0]
                    elif y == 23 or (y >= 12 and x == left):
                        color = tones[5]
                    elif x >= 38:
                        color = tones[4]
                    else:
                        t = min(1.0, max(0.0, (x - 14) / 24.0))
                        color = dither(rng, mix(tones[1], tones[3], t), 4)
                    img.putpixel((ox + x, oy + y), rgba(color))
            draw_icon(img, ox + 14, oy + 6, TAB_GLYPHS[row], lambda ch: WHITE)
            if state == 1:
                for y in range(24):
                    left = 11 - y if y < 12 else y - 12
                    img.putpixel((ox + left, oy + y), WHITE)
                for x in range(12, 48):
                    img.putpixel((ox + x, oy), WHITE)
                    img.putpixel((ox + x, oy + 23), WHITE)
    return img


def main():
    OUT.mkdir(parents=True, exist_ok=True)
    stamps().save(OUT / "tampons.png")
    frame().save(OUT / "cadre.png")
    bookmark().save(OUT / "marque.png")
    side_tabs().save(OUT / "onglets.png")


if __name__ == "__main__":
    main()
