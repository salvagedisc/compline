// Les trois passes de l'anticrénelage SMAA : les bords des formes (composite4), le poids de chaque bord selon sa forme
// (composite5), puis le mélange avec les pixels voisins, dans la passe finale.
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
    vec3 color = SMAANeighborhoodBlendingPS(vUv, vOffset[0], colortex7, colortex9).rgb;
#else
    vec3 color = texture(colortex7, vUv).rgb;
#endif
#if TAA > 0
    // L'anticrénelage temporel adoucit un peu le grain des textures : on le redonne, sans dépasser les teintes
    // voisines (pas de liseré clair autour des formes).
    ivec2 p = ivec2(gl_FragCoord.xy);
    ivec2 top = ivec2(viewWidth, viewHeight) - 1;
    vec3 c = texelFetch(colortex7, p, 0).rgb;
    vec3 a = texelFetch(colortex7, clamp(p + ivec2(1, 0), ivec2(0), top), 0).rgb;
    vec3 b = texelFetch(colortex7, clamp(p - ivec2(1, 0), ivec2(0), top), 0).rgb;
    vec3 d = texelFetch(colortex7, clamp(p + ivec2(0, 1), ivec2(0), top), 0).rgb;
    vec3 e = texelFetch(colortex7, clamp(p - ivec2(0, 1), ivec2(0), top), 0).rgb;
    vec3 sharp = c + (4.0 * c - a - b - d - e) * 0.45;
    color += clamp(sharp, min(c, min(min(a, b), min(d, e))), max(c, max(max(a, b), max(d, e)))) - c;
#endif
    outColor = vec4(color, 1.0);
}
#endif
