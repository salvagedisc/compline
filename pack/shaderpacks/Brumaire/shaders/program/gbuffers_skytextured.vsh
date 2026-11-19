#include "/lib/settings.glsl"
out vec2 vUv;
out vec4 vTint;
out vec3 vView;
#if LUNAR_EVENTS > 0
// La couleur dont le jeu teinte la lune (les lunes d'Enhanced Celestials), rangée dans une case libre de la table du
// ciel que lib/common.glsl relit. Écrite par le sommet, pas par le pixel, pour rester à jour quand la lune est hors
// champ.
#define SKY_WRITER
#include "/lib/atmosphere.glsl"
layout(rgba16f) uniform image2D skyImg;
uniform int renderStage;
#endif
void main() {
    gl_Position = ftransform();
    vUv = (gl_TextureMatrix[0] * gl_MultiTexCoord0).xy;
    vTint = gl_Color;
    vView = (gl_ModelViewMatrix * gl_Vertex).xyz;
#if LUNAR_EVENTS > 0
    if (renderStage == MC_RENDER_STAGE_MOON && gl_VertexID == 0) imageStore(skyImg, ivec2(SKY_LUNAR, SKY_H), vec4(gl_Color.rgb, 1.0));
#endif
}
