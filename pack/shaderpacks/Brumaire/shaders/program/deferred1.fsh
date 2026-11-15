// Occlusion lissée et appliquée à la lumière indirecte, puis le ciel peint derrière le terrain. La scène opaque est
// aussi copiée dans colortex6 : l'eau la relit pour ce qu'on voit à travers elle et ce qu'elle reflète.
#include "/lib/settings.glsl"
#include "/lib/common.glsl"
#include "/program/scene.glsl"

uniform sampler2D colortex0;
uniform sampler2D colortex1;
uniform sampler2D colortex2;
in vec2 vUv;

/*
const int colortex6Format = R11F_G11F_B10F;
const bool colortex6Clear = false;
*/

/* RENDERTARGETS: 0,6 */
layout(location = 0) out vec4 outColor;
layout(location = 1) out vec4 outScene;

float linearDepth(float d) { return near * far / (far - d * (far - near)); }

/** L'occlusion du demi-écran, lissée sans déborder d'un objet sur ce qui est derrière lui. */
float smoothAo(vec2 uv, float depth) {
    vec2 texel = 2.0 / vec2(viewWidth, viewHeight);
    float centre = linearDepth(depth);
    float sum = 0.0, weight = 0.0;
    for (int y = -2; y <= 1; y++) {
        for (int x = -2; x <= 1; x++) {
            vec2 o = (vec2(x, y) + 0.5) * texel;
            float d = linearDepth(texture(depthtex0, uv + o).r);
            float w = 1.0 / (1e-3 + abs(d - centre) / centre * 40.0);
            sum += texture(colortex2, (uv + o) * 0.5).r * w;
            weight += w;
        }
    }
    return sum / weight;
}

void main() {
    vec3 color = texture(colortex0, vUv).rgb;
    bool sky, hand;
    vec3 pos = scenePosition(vUv, sky, hand);
    if (sky) {
        initEnv();
        vec3 dir = normalize(pos);
        color += skyAt(dir) + sunDisc(dir);
    }
#if AO_SAMPLES > 0
    else if (!hand) {
        vec4 surface = texture(colortex1, vUv);
        if (surface.a > 0.5) color *= mix(1.0, smoothAo(vUv, texture(depthtex0, vUv).r), surface.b);
    }
#endif
    outColor = vec4(color, 1.0);
    outScene = outColor;
}
