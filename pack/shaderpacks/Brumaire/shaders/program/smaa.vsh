// Les trois passes de l'anticrénelage SMAA (lib/smaa.glsl) : côté sommets, les décalages de chaque passe.
#include "/lib/settings.glsl"
#define SMAA_VERTEX
#include "/program/smaa.glsl"

out vec2 vUv;
out vec4 vOffset[3];
#if defined SMAA_PASS_WEIGHTS
out vec2 vPixel;
#endif

void main() {
    gl_Position = ftransform();
    vUv = gl_MultiTexCoord0.xy;
    vOffset[0] = vOffset[1] = vOffset[2] = vec4(0.0);
#if SMAA > 0
#if defined SMAA_PASS_EDGES
    SMAAEdgeDetectionVS(vUv, vOffset);
#elif defined SMAA_PASS_WEIGHTS
    SMAABlendingWeightCalculationVS(vUv, vPixel, vOffset);
#else
    SMAANeighborhoodBlendingVS(vUv, vOffset[0]);
#endif
#endif
}
