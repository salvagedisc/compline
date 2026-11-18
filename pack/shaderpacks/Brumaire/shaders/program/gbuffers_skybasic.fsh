// Le ciel est peint plus tard (deferred) : du ciel du jeu, seules les étoiles sont gardées, et seulement quand le
// ciel de nuit (lib/nightsky.glsl) ne dessine pas les siennes.
#include "/lib/settings.glsl"
uniform int renderStage;
in vec4 vTint;

/* RENDERTARGETS: 0 */
layout(location = 0) out vec4 outColor;

void main() {
    if (renderStage != MC_RENDER_STAGE_STARS) discard;
#if SKY_STARS > 0
    discard;
#endif
    outColor = vec4(pow(vTint.rgb, vec3(2.2)) * vTint.a * 1.6, 1.0);
}
