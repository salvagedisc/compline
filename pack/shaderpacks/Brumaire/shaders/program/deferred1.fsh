// Occlusion lissée et appliquée à la lumière indirecte, le reflet des surfaces mouillées et des flaques, puis le ciel
// peint derrière le terrain. La scène opaque est aussi copiée dans colortex6 : l'eau la relit pour ce qu'on voit à
// travers elle et ce qu'elle reflète.
#define FSH
#include "/lib/settings.glsl"
#include "/lib/common.glsl"
#include "/program/scene.glsl"
#include "/lib/shadow.glsl"
#include "/lib/mist.glsl"
#include "/lib/clouds.glsl"
#include "/lib/rain.glsl"

uniform sampler2D colortex0;
uniform sampler2D colortex1;
uniform sampler2D colortex2;
in vec2 vUv;

#define REFLECT_DEPTH depthtex0
#define REFLECT_COLOR colortex0
#include "/lib/reflect.glsl"
#include "/lib/voxel.glsl"
#include "/lib/voxtrace.glsl"

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

/**
 * Ce que renvoie une surface qui luit : une flaque reflète le paysage comme un miroir ridé par les gouttes, la glace
 * tassée comme un miroir terni ; un sol trempé ou la neige luisent du ciel, sans image nette.
 * gloss : 1 flaque, 0.75 glace, jusqu'à 0.4 sol mouillé ou neige.
 */
vec3 wetReflection(vec3 color, vec3 pos, vec3 normal, float gloss) {
    vec3 world = pos + cameraPosition;
    vec3 eye = normalize(-pos);
    bool puddle = gloss > 0.95;
    vec3 n = normal;
    if (puddle) {
        vec2 slope = rippleSlope(world.xz, frameTimeCounter) * rainStrength;
        n = normalize(vec3(-slope.x, 1.0, -slope.y));
    }
    vec3 r = reflect(-eye, n);
    r.y = max(r.y, 0.01);
    float f = 0.03 + 0.97 * pow(1.0 - max(dot(n, eye), 0.0), 5.0);
    vec3 mirror = skyReflection(world, r);
    if (gloss > 0.6) {
        float dist;
        vec3 view = (gbufferModelView * vec4(pos, 1.0)).xyz;
        vec4 hit = screenReflection(view, mat3(gbufferModelView) * r, dither(gl_FragCoord.xy), 1.0, WET_REFLECTIONS, dist);
#if VOXEL_REFLECTIONS > 0
        if (hit.a < 0.99 && WET_REFLECTIONS > 0) {
            float reach;
            vec4 vox = voxelReflection(pos + normal * 0.02, r, reach);
            if (vox.a > 0.0) mirror = mix(mirror, veiled(vox.rgb, r * reach * 0.4), vox.a);
        }
#endif
        if (hit.a > 0.0) mirror = mix(mirror, veiled(hit.rgb, r * dist * 0.4), hit.a);
        return mix(color, mirror, puddle ? min(f * 1.2 + 0.18, 1.0) : f * 0.85 + 0.04);
    }
    // Un sol trempé : un éclat diffus, surtout à jour frisant.
    return mix(color, mirror, f * gloss * 1.4);
}

void main() {
    vec3 color = texture(colortex0, vUv).rgb;
    bool sky, hand;
    vec3 pos = scenePosition(vUv, sky, hand);
    initEnv();
    if (sky) {
        vec3 dir = normalize(pos);
        color += skyAt(dir) + sunDisc(dir);
    } else if (!hand) {
        vec4 surface = texture(colortex1, vUv);
#if AO_SAMPLES > 0
        if (surface.a > 0.5) color *= mix(1.0, smoothAo(vUv, texture(depthtex0, vUv).r), surface.b);
#endif
        float gloss = surface.a > 0.5 ? (1.0 - surface.a) / 0.45 : 0.0;
        if (gloss > 0.02) color = wetReflection(color, pos, unpackNormal(surface.rg), gloss);
    }
    outColor = vec4(color, 1.0);
    outScene = outColor;
}
