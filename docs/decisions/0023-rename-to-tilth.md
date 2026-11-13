# 0023 — The pack is renamed Tilth

- **Date**: 2026-11-13
- **Status**: kept (the team's choice of 2026-11-13)

## Context
The pack was called Fallow. The `fallow` project slug is already taken on Modrinth, where the pack is to be published, so it needed a new name. The team wanted a short English name, a little mysterious, in the same field as Fallow: land, rest and work of the soil. Tilth (the state of tilled soil, and the work of tilling it) was free on Modrinth on 2026-11-13.

## Decision
Every mention of Fallow becomes Tilth, identifiers included:

| Before | After |
|---|---|
| Pack name (`pack/pack.toml`), exports `Fallow-*.mrpack` | `Tilth`, `Tilth-*.mrpack` |
| Custom mod: mod id and namespace `fallow`, package `io.github.salvagedisc.fallow`, `mods-custom/fallow/`, `Fallow*` classes, `config/fallow-*.toml` | `tilth`, `io.github.salvagedisc.tilth`, `mods-custom/tilth/`, `Tilth*`, `config/tilth-*.toml` |
| Datapack and assets namespace (`pack/kubejs/data/fallow/`, `assets/fallow/`), language keys `*.fallow.*` | `tilth` |
| Resource packs `fallow-ambiance`, `fallow-interface`, `fallow-textures` | `tilth-ambiance`, `tilth-interface`, `tilth-textures` |
| Questlog chapters, music pack, FancyMenu layouts and assets `fallow_*` | `tilth_*` |
| Title screen logo | the word TILTH, its lantern hung under the arch of the h (`scripts/interface/logo.py`) |
| GitHub repository `salvagedisc/fallow` | `salvagedisc/tilth` (GitHub redirects the old address) |

Older entries of the changelog, decisions and archive were renamed too, so that every path they cite still exists.

## Consequences
- Every registry id changes (`fallow:dissipateur` becomes `tilth:dissipateur`, the clearing looks, the Chorusoatl nest…). Worlds created before the rename lose the custom mod's blocks and items. No permanent world exists yet (`0.x` worlds are disposable), so no alias is kept.
- Players' resource pack selection is reset to the new folder names by Default Options on a new instance only: an existing instance must re-enable the three `tilth-*` packs, or be imported again from the `dev` pre-release.
