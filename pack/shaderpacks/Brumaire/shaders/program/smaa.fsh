// Les trois passes de l'anticrénelage SMAA : les bords des formes (composite3), le poids de chaque bord selon sa forme
// (composite4), puis le mélange avec les pixels voisins, dans la passe finale.
#include "/lib/settings.glsl"
#include "/program/smaa.glsl"

in vec2 vUv;
in vec4 vOffset[3];
#if defined SMAA_PASS_WEIGHTS
in vec2 vPixel;
#endif

#if defined SMAA_PASS_EDGES
/*
const int colortex8Format = RG8;
*/
const vec4 colortex8ClearColor = vec4(0.0, 0.0, 0.0, 0.0);
uniform sampler2D colortex7;
uniform sampler2D depthtex0;
uniform float near;
uniform float far;
/* RENDERTARGETS: 8 */
layout(location = 0) out vec4 outColor;

float linearDepth(vec2 uv) {
    float d = texture(depthtex0, uv).r * 2.0 - 1.0;
    return 2.0 * near * far / (far + near - d * (far - near));
}

/** Un bord de forme : la profondeur saute entre ce pixel et son voisin (de plus de 2 %). */
float shapeEdge(float here, vec2 uv) {
    float there = linearDepth(uv);
    return step(0.02, abs(here - there) / min(here, there));
}

void main() {
#if SMAA > 0
    // Les bords trouvés à la clarté, gardés seulement là où une forme se détache d'une autre : le grain des textures
    // reste net au pixel près, seules les silhouettes s'adoucissent.
    vec2 edges = SMAALumaEdgeDetectionPS(vUv, vOffset, colortex7);
    float here = linearDepth(vUv);
    edges *= vec2(shapeEdge(here, vOffset[0].xy), shapeEdge(here, vOffset[0].zw));
    outColor = vec4(edges, 0.0, 1.0);
#else
    outColor = vec4(0.0);
#endif
}

#elif defined SMAA_PASS_WEIGHTS
/*
const int colortex9Format = RGBA8;
*/
const vec4 colortex9ClearColor = vec4(0.0, 0.0, 0.0, 0.0);
uniform sampler2D colortex8;
uniform sampler2D smaaArea;
uniform sampler2D smaaSearch;
/* RENDERTARGETS: 9 */
layout(location = 0) out vec4 outColor;

void main() {
#if SMAA > 0
    outColor = SMAABlendingWeightCalculationPS(vUv, vPixel, vOffset, colortex8, smaaArea, smaaSearch, vec4(0.0));
#else
    outColor = vec4(0.0);
#endif
}

#else
uniform sampler2D colortex7;
uniform sampler2D colortex9;
layout(location = 0) out vec4 outColor;

void main() {
#if SMAA > 0
    outColor = vec4(SMAANeighborhoodBlendingPS(vUv, vOffset[0], colortex7, colortex9).rgb, 1.0);
#else
    outColor = vec4(texture(colortex7, vUv).rgb, 1.0);
#endif
}
#endif
