"""Builds the tileable cloud texture of the 3D disc: fractal noise shaped in the frequency domain, so it wraps seamlessly.

Output: R = large cloud masses, G = fine detail used to fray their edges. Usage: python3 scripts/nuages.py <output image>
"""
import sys

import numpy as np
from PIL import Image

N = 256


def bruit(beta: float, graine: int, coupe: float = 0.0) -> np.ndarray:
    rng = np.random.default_rng(graine)
    f = np.fft.fftfreq(N)
    k = np.hypot(*np.meshgrid(f, f))
    k[0, 0] = 1
    spectre = np.fft.fft2(rng.standard_normal((N, N))) / k ** beta
    spectre[k < coupe] = 0
    a = np.real(np.fft.ifft2(spectre))
    return (a - a.mean()) / a.std()


def main(sortie: str):
    masses = bruit(1.8, 7, 1.5 / N)
    detail = bruit(1.5, 11, 4 / N)
    r = np.clip(0.5 + masses * 0.22, 0, 1)
    g = np.clip(0.5 + detail * 0.25, 0, 1)
    img = np.dstack([r, g, np.zeros_like(r)]) * 255
    Image.fromarray(img.astype(np.uint8), 'RGB').save(sortie, optimize=True)


if __name__ == '__main__':
    main(sys.argv[1])
