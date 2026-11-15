// Le ciel, peint derrière le terrain avant l'eau et les nuages. Les astres du jeu s'y ajoutent.
#include "/lib/settings.glsl"
#include "/lib/common.glsl"
#include "/program/scene.glsl"

uniform sampler2D colortex0;
in vec2 vUv;

/* RENDERTARGETS: 0 */
layout(location = 0) out vec4 outColor;

void main() {
    vec3 color = texture(colortex0, vUv).rgb;
    bool sky, hand;
    vec3 pos = scenePosition(vUv, sky, hand);
    if (sky) {
        initEnv();
        vec3 dir = normalize(pos);
        color += skyAt(dir) + sunDisc(dir);
    }
    outColor = vec4(color, 1.0);
}
