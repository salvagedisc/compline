"""Pulls the vanilla grass and foliage colour maps toward olive (Python 3 + Pillow).

Vanilla biomes take their grass and leaf colour from these two maps (temperature x
humidity). Fallow keeps the map, so the colour still changes from one biome to the
next, but pulls every entry a quarter of the way toward olive and takes off some
saturation: warm, muted greens, like the biomes that set their own colours
(scripts/ambiance/biomes.py).

Usage, from the repository root, with the NeoForge client resources jar that
ModDevGradle puts in mods-custom/fallow/build/moddev/artifacts/:
  python3 scripts/ambiance/palette.py <neoforge-...-client-extra-aka-minecraft-resources.jar>
"""
import colorsys
import io
import os
import sys
import zipfile

from PIL import Image

ROOT = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
OUT = os.path.join(ROOT, "pack/resourcepacks/fallow-ambiance/assets/minecraft/textures/colormap")

OLIVE_HUE = 68 / 360  # yellow-green
HUE_PULL = 0.25
SATURATION = 0.85
VALUE = 0.96


def olive(rgb):
    h, s, v = colorsys.rgb_to_hsv(*(c / 255 for c in rgb[:3]))
    h += (OLIVE_HUE - h) * HUE_PULL
    r, g, b = colorsys.hsv_to_rgb(h, s * SATURATION, v * VALUE)
    return tuple(round(c * 255) for c in (r, g, b))


def convert(data):
    im = Image.open(io.BytesIO(data)).convert("RGB")
    out = Image.new("RGB", im.size)
    w, h = im.size
    out.putdata([olive(im.getpixel((x, y))) for y in range(h) for x in range(w)])
    return out


if __name__ == "__main__":
    if len(sys.argv) != 2:
        sys.exit(__doc__)
    os.makedirs(OUT, exist_ok=True)
    with zipfile.ZipFile(sys.argv[1]) as jar:
        for name in ("grass", "foliage"):
            data = jar.read(f"assets/minecraft/textures/colormap/{name}.png")
            convert(data).save(os.path.join(OUT, f"{name}.png"), optimize=True)
    print("olive colour maps written")
