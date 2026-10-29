"""Builds the fallow-textures resource pack from Vanilla Evolved.

Base: Vanilla Evolved 1.17.0 by Vixel (CC BY-NC-SA 4.0), the release made for
Minecraft 1.21 and 1.21.1. Vixel also drew GUI Revision, the base of
fallow-interface, so the world and the interface share one hand.

The script downloads the pinned release, checks its hash and keeps what
retextures the world's blocks and items. It drops what would undo a choice
already made for the pack:
- textures/environment: the moon stays vanilla (the team's choice of
  2026-10-29) and ATMOSPHERICS draws the sun; rain and snow stay as Where Winds
  Blow expects them;
- textures/colormap: grass and foliage colours come from Polytone, biome by
  biome (fallow-ambiance);
- textures/entity, except block entities (chests, chest boats, shulker boxes,
  the enchanting table's book, beams): creatures keep the textures Fresh
  Animations is drawn on;
- lang: the game is in French, and the pack only renamed one flower in English;
- disabled files (names starting with "#"), templates and drawing sources,
  and one model whose parent is missing from the release (DROP_FILES);
- optifine: no mod of the pack reads OptiFine's connected textures, emissive
  maps, custom items or map colours.

Authoring tool, not run by CI: needs Python 3 only.
    python3 scripts/textures/build.py
"""
import hashlib
import io
import json
import shutil
import urllib.request
import zipfile
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
OUT = ROOT / "pack" / "resourcepacks" / "fallow-textures"

URL = "https://cdn.modrinth.com/data/5Wz8MWHt/versions/ltHEptpg/VanillaEvolved_1.17.0.zip"
SHA512 = ("4af2df14c115345a30221fe69003c760fc64d4ae9452c2db779c10dbbd9558c3"
          "e614b34559496f8905d7f94f374da487b63bf5915bef071d5d9f4b1793ef1415")

DROP_PREFIXES = (
    "assets/minecraft/textures/environment/",
    "assets/minecraft/textures/colormap/",
    "assets/minecraft/lang/",
    "assets/minecraft/optifine/",
)
# Broken upstream: its parent model (block/observerextra) is not in the release.
DROP_FILES = ("assets/minecraft/models/block/observer_on.json",)
ENTITY = "assets/minecraft/textures/entity/"
KEEP_ENTITIES = (
    "chest/", "chest_boat/", "shulker/",
    "enchanting_table_book.png", "beacon_beam.png", "end_gateway_beam.png",
)

MCMETA = {
    "pack": {
        "pack_format": 34,
        "description": "Fallow : blocs et objets, d'après Vanilla Evolved",
    }
}


def keep(name: str) -> bool:
    base = name.rsplit("/", 1)[-1]
    if name.endswith("/") or name == "pack.mcmeta" or base.startswith("#") or "_template" in base:
        return False
    if not base.endswith((".png", ".json", ".mcmeta")):
        return False
    if name.startswith(DROP_PREFIXES) or name in DROP_FILES:
        return False
    if name.startswith(ENTITY):
        return name[len(ENTITY):].startswith(KEEP_ENTITIES)
    return name == "pack.png" or name.startswith("assets/minecraft/")


def main() -> None:
    data = urllib.request.urlopen(urllib.request.Request(URL, headers={"User-Agent": "fallow-build"})).read()
    if hashlib.sha512(data).hexdigest() != SHA512:
        raise SystemExit("Vanilla Evolved: unexpected hash, check the pinned release")
    credits = (OUT / "CREDITS.md").read_text(encoding="utf-8") if (OUT / "CREDITS.md").exists() else None
    if OUT.exists():
        shutil.rmtree(OUT)
    kept = dropped = 0
    with zipfile.ZipFile(io.BytesIO(data)) as z:
        for name in z.namelist():
            if not keep(name):
                dropped += not name.endswith("/")
                continue
            dst = OUT / name
            dst.parent.mkdir(parents=True, exist_ok=True)
            dst.write_bytes(z.read(name))
            kept += 1
    (OUT / "pack.mcmeta").write_text(json.dumps(MCMETA, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    if credits:
        (OUT / "CREDITS.md").write_text(credits, encoding="utf-8")
    print(f"fallow-textures: {kept} files kept, {dropped} dropped")


if __name__ == "__main__":
    main()
