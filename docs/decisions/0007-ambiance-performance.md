# 0007 — Atmosphere & performance

- **Date**: 2026-09-24
- **Status**: kept (tested on `test/structures`, merged into `main` on 2026-10-10); Distant Horizons, Separate Sable Render Distance and Particle Rain pending

## Atmosphere (no shader: Aeronautics has known problems with Iris)
| Mod | Side | Role |
|---|---|---|
| **ATMOSPHERICS** | C | Fog, sky, clouds, mist adjustable per biome, separate day/night; shareable presets. Do not enable "Improved Transparency" in Sodium. |
| **AmbientSounds** (+ CreativeCore) | C | Sound ambiences by biome, time, altitude (wind, birds, insects, drips in caves) |
| **Sound Physics Remastered** | CS | Reverberation and occlusion: caves and valleys sound like what they are. The 1.21.1 version is marked alpha but very widely used. |
| **Subtle Effects** | CS | Particle details (sparks, smoke, dust); some effects to be turned off (config) |
| **LambDynamicLights** | C | A torch held in hand gives light |
| **More Darkness** | CS | Nights governed by the moon, black caves, eye adaptation. Young (14 k) but client-side: reversible |

Rejected: Fog (not updated since 2025-10, duplicates ATMOSPHERICS), FogRule (alpha, requires Lootr), Presence Footsteps, Falling Leaves, Visuality, Cave Dust, True Darkness (no NeoForge 1.21.1 version).

## Performance
| Mod | Side | Role / verified compatibility |
|---|---|---|
| **Sodium** 0.8.13 | C | Rendering; Sable requires ≥ 0.8.12, Create ≥ 0.6.9 |
| **ImmediatelyFast**, **Entity Culling** | C | Immediate-mode rendering, hiding of invisible entities |
| **ModernFix**, **FerriteCore** | CS | Loading time, memory |
| **Lithium** 0.15.4 | CS | Server logic; Create requires ≥ 0.14.7 |
| **Chunky** | CS | World pregeneration (server) |
| **spark** | CS | Profiling (diagnosing slowdowns) |
| **Clumps** | CS | Merging of experience orbs |
| **Structure Layout Optimizer** (+ Resourceful Config) | CS | Generation of large structures |

Rejected: C2ME (alpha), Noisium (not up to date), MoreCulling (marginal gain), Alternate Current (little redstone), Ksyxis.

## Pending
- **Distant Horizons**: seeing a mountain hundreds of blocks away directly serves exploration, but is expensive (GPU, RAM, server-side generation). Compatible with Ecliptic. To be tested on its own branch.
- **Separate Sable Render Distance** (beta): a dedicated render distance for Aeronautics contraptions.
- **Particle Rain** (beta): rain/snow particles and storm mist; possible duplicate of Ecliptic's effects.
