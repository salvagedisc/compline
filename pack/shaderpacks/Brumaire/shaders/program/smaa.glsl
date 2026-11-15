// Réglage de SMAA pour Brumaire, avant d'inclure lib/smaa.glsl (SMAA de Jorge Jimenez et al., licence MIT, voir
// textures/SMAA-LICENSE.txt). Les tables areaTex et searchTex sont textures/smaa_area.png et smaa_search.png.
uniform float viewWidth;
uniform float viewHeight;

#if SMAA > 0
#define SMAA_GLSL_3
#define SMAA_RT_METRICS vec4(1.0 / viewWidth, 1.0 / viewHeight, viewWidth, viewHeight)
#if SMAA == 1
#define SMAA_PRESET_MEDIUM
#elif SMAA == 2
#define SMAA_PRESET_HIGH
#else
#define SMAA_PRESET_ULTRA
#endif
#ifdef SMAA_VERTEX
#define SMAA_INCLUDE_PS 0
#else
#define SMAA_INCLUDE_VS 0
#endif
#include "/lib/smaa.glsl"
#endif
