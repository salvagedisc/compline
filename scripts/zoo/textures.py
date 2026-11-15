"""Images du zoo dans le guide d'Archie's Dinosaurs, dessinées pixel par pixel (aucune image du mod n'est reprise).

Écrit dans mods-custom/compline/src/main/resources/assets/compline/textures/gui/zoo/ :
- tampons.png (96 x 120) : les quatre tampons (Apercevoir, Photographier, Apprivoiser ou héberger, Maîtriser),
  ligne 0 à l'encre pâle (pas encore obtenus), lignes 1 à 4 aux couleurs des milieux (terre, eaux, ciel, créatures) ;
- cadre.png (24 x 24) : le cadre des portraits ;
- marque.png (64 x 48) : le marque-page du bas, normal puis survolé ;
- onglets.png (128 x 160) : les onglets de gauche (accueil puis les quatre milieux), normaux puis survolés.

Lancer depuis la racine du dépôt : python3 scripts/zoo/textures.py (Pillow).
"""
from pathlib import Path

from PIL import Image

OUT = Path(__file__).resolve().parents[2] / "mods-custom/compline/src/main/resources/assets/compline/textures/gui/zoo"

INK = (87, 61, 25, 255)
INK_LIGHT = (148, 117, 70, 255)
PAPER = (220, 198, 162, 255)
WHITE = (255, 255, 255, 255)
CLEAR = (0, 0, 0, 0)

# Milieux : (clair, moyen, sombre)
TABS = [
    ((150, 196, 104), (98, 150, 66), (52, 92, 38)),     # terre
    ((132, 186, 226), (72, 134, 186), (36, 82, 128)),   # eaux
    ((196, 226, 244), (128, 182, 220), (70, 122, 168)),  # ciel
    ((192, 150, 214), (132, 92, 166), (80, 50, 112)),   # créatures
]
INTRO = ((176, 132, 92), (132, 92, 58), (84, 56, 34))


def mask(rows):
    return [list(row) for row in rows]


# Icônes des tampons (13 x 13) : o contour, f couleur, h reflet, s ombre, d détail
MAGNIFIER = mask([
    "...ooooo.....",
    "..ohhfffo....",
    ".ohhfffffo...",
    ".ohfffffso...",
    ".offfffsso...",
    ".offffssso...",
    "..ossssso....",
    "...oooooddo..",
    "........oddo.",
    ".........oddo",
    "..........odo",
    "...........o.",
    ".............",
])
CAMERA = mask([
    ".............",
    "...ooo.......",
    "..oddo.......",
    ".ooooooooooo.",
    ".oddddddddhd.",
    ".odddoooddddo",
    ".oddohhfoddo.",
    ".oddofffsoddo",
    ".oddoffssoddo",
    ".odddoooddddo",
    ".oddddddddddo",
    ".ooooooooooo.",
    ".............",
])
HEART = mask([
    ".............",
    "..ooo...ooo..",
    ".ohhfo.offfo.",
    "ohhfffoffffso",
    "ohffffffffsso",
    "offfffffffsso",
    ".offffffffso.",
    "..offfffffso.",
    "...offfffso..",
    "....offfso...",
    ".....offo....",
    "......oo.....",
    ".............",
])
STAR = mask([
    "......o......",
    ".....ofo.....",
    ".....ohfo....",
    "....ohffo....",
    "ooooohfffoooo",
    "ohhhhffffffso",
    ".ohffffffffo.",
    "..offffffso..",
    "..offfsffso..",
    ".offso.osffo.",
    ".ofso...osfo.",
    ".oo.......oo.",
    ".............",
])
ICON_COLORS = [
    # (contour, couleur, reflet, ombre, détail)
    ((92, 58, 26), (176, 214, 232), (240, 250, 255), (120, 168, 196), (110, 72, 38)),   # loupe
    ((44, 36, 32), (120, 170, 210), (220, 240, 255), (70, 110, 150), (78, 66, 58)),     # appareil
    ((110, 24, 34), (214, 58, 68), (255, 170, 170), (160, 34, 46), (0, 0, 0)),          # cœur
    ((128, 82, 14), (236, 186, 52), (255, 236, 140), (198, 140, 30), (0, 0, 0)),        # étoile
]
ICONS = [MAGNIFIER, CAMERA, HEART, STAR]
SEPIA = ((150, 120, 80), (206, 182, 142), (228, 210, 176), (182, 154, 110), (160, 128, 86))


def put(img, x, y, color):
    if 0 <= x < img.width and 0 <= y < img.height:
        img.putpixel((x, y), color + (255,) if len(color) == 3 else color)


def diamond(img, ox, oy, fill, border, ring):
    """Losange de 24 px : un liseré clair, un bord sombre, le fond."""
    for y in range(24):
        for x in range(24):
            d = abs(x - 11.5) + abs(y - 11.5)
            if d <= 11.5:
                color = ring if d > 10.5 else border if d > 9.0 else fill
                put(img, ox + x, oy + y, color)


def icon(img, ox, oy, rows, colors):
    keys = {"o": 0, "f": 1, "h": 2, "s": 3, "d": 4}
    for y, row in enumerate(rows):
        for x, ch in enumerate(row):
            if ch in keys:
                put(img, ox + x, oy + y, colors[keys[ch]])


def shade(color, factor):
    return tuple(max(0, min(255, int(c * factor))) for c in color[:3])


def stamps():
    img = Image.new("RGBA", (96, 120), CLEAR)
    for i, rows in enumerate(ICONS):
        # Pas encore obtenu : losange estampé à l'encre pâle
        diamond(img, i * 24, 0, (210, 186, 148), (176, 146, 102), (232, 216, 186))
        icon(img, i * 24 + 6, 6, rows, SEPIA)
        for t, (light, mid, dark) in enumerate(TABS):
            oy = (t + 1) * 24
            diamond(img, i * 24, oy, light, dark, (240, 226, 196))
            # un reflet en haut à gauche du fond
            for y in range(24):
                for x in range(24):
                    d = abs(x - 11.5) + abs(y - 11.5)
                    if d <= 9.0 and x + y < 16 and d > 7.0:
                        put(img, i * 24 + x, oy + y, shade(light, 1.12))
            icon(img, i * 24 + 6, oy + 6, rows, ICON_COLORS[i])
    return img


def frame():
    img = Image.new("RGBA", (24, 24), CLEAR)
    for y in range(24):
        for x in range(24):
            edge = x in (0, 23) or y in (0, 23)
            corner = (x in (0, 23)) and (y in (0, 23))
            if corner:
                continue
            if edge:
                put(img, x, y, INK_LIGHT)
            elif x == 1 or y == 1:
                put(img, x, y, (196, 172, 132))
            else:
                put(img, x, y, (212, 190, 152))
    return img


PAW = mask([
    "............",
    "...oo..oo...",
    "...oo..oo...",
    ".oo......oo.",
    ".oo.oooo.oo.",
    "...oooooo...",
    "..oooooooo..",
    "..oooooooo..",
    "...oo..oo...",
    "............",
])
LEAF = mask([
    "........oooo",
    "......oooooo",
    "....ooooo.oo",
    "...oooo..ooo",
    "..oooo..ooo.",
    "..ooo..oooo.",
    ".ooo..oooo..",
    ".oo.oooo....",
    ".o.ooo......",
    "o...........",
])
FISH = mask([
    "............",
    "....oooo....",
    "..oooooooo.o",
    ".oo.oooooooo",
    "ooooooooooo.",
    ".oooooooooo.",
    "..ooooooooo.",
    "....oooo...o",
    "............",
    "............",
])
FEATHER = mask([
    ".........oo.",
    "........oooo",
    ".......ooooo",
    "......oooo.o",
    ".....oooo.oo",
    "....oooo.oo.",
    "...oooo.oo..",
    "...ooo.oo...",
    "..o..oo.....",
    ".o..........",
])
EYE = mask([
    "............",
    "....oooo....",
    "..oo....oo..",
    ".o..oooo..o.",
    "o..oo..oo..o",
    "o..oo..oo..o",
    ".o..oooo..o.",
    "..oo....oo..",
    "....oooo....",
    "............",
])
TAB_ICONS = [PAW, LEAF, FISH, FEATHER, EYE]


def ribbon_vertical(img, ox, colors, hovered):
    """Marque-page du bas : 32 x 48, le haut glisse sous la couverture, une queue d'aronde en bas."""
    light, mid, dark = colors
    for y in range(48):
        for x in range(32):
            notch = y >= 44 and abs(x - 15.5) < (y - 43) * 2.2
            if notch:
                continue
            color = mid if y < 22 else light
            if x < 2 or x > 29:
                color = shade(color, 0.86)
            if y in (22, 23):
                color = shade(light, 0.92)
            put(img, ox + x, y, color)
    for y, row in enumerate(PAW):
        for x, ch in enumerate(row):
            if ch == "o":
                put(img, ox + 10 + x, 28 + y, WHITE)
    if hovered:
        outline(img, ox, 0, 32, 48)


def tab_side(img, ox, oy, colors, rows, hovered):
    """Onglet de gauche : 64 x 32, pointe à gauche, assombri vers la droite (il passe sous le livre)."""
    light, mid, dark = colors
    for y in range(32):
        for x in range(64):
            tip = 14 - min(y, 31 - y)
            if x < tip:
                continue
            t = min(1.0, max(0.0, (x - 30) / 30))
            base = tuple(int(mid[i] * (1 - t) + dark[i] * t) for i in range(3))
            if y == 31 or x < tip + 1:
                base = shade(base, 0.8)
            put(img, ox + x, oy + y, base)
    for y, row in enumerate(rows):
        for x, ch in enumerate(row):
            if ch == "o":
                put(img, ox + 16 + x, oy + 11 + y, WHITE)
    if hovered:
        outline(img, ox, oy, 64, 32)


def outline(img, ox, oy, w, h):
    """Liseré blanc d'un élément survolé, autour des pixels opaques."""
    src = img.crop((ox, oy, ox + w, oy + h))
    for y in range(h):
        for x in range(w):
            if src.getpixel((x, y))[3] == 0:
                continue
            border = any(
                not (0 <= x + dx < w and 0 <= y + dy < h) or src.getpixel((x + dx, y + dy))[3] == 0
                for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1))
            )
            if border:
                img.putpixel((ox + x, oy + y), WHITE)


def bookmark():
    img = Image.new("RGBA", (64, 48), CLEAR)
    green = TABS[0]
    ribbon_vertical(img, 0, green, False)
    ribbon_vertical(img, 32, green, True)
    return img


def side_tabs():
    img = Image.new("RGBA", (128, 160), CLEAR)
    for row, colors in enumerate([INTRO] + TABS):
        tab_side(img, 0, row * 32, colors, TAB_ICONS[row], False)
        tab_side(img, 64, row * 32, colors, TAB_ICONS[row], True)
    return img


def main():
    OUT.mkdir(parents=True, exist_ok=True)
    stamps().save(OUT / "tampons.png")
    frame().save(OUT / "cadre.png")
    bookmark().save(OUT / "marque.png")
    side_tabs().save(OUT / "onglets.png")
    print("écrit dans", OUT)


if __name__ == "__main__":
    main()
