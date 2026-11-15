// Fragments de toute la géométrie éclairée. Chaque programme d'Iris inclut ce fichier avec son PROGRAM_*.
#define FSH
#include "/lib/settings.glsl"
#include "/lib/common.glsl"
#include "/lib/shadow.glsl"
#include "/lib/light.glsl"
#ifdef PROGRAM_WATER
#include "/lib/mist.glsl"
uniform sampler2D depthtex1;
#endif

uniform sampler2D gtexture;
uniform float alphaTestRef;
uniform vec4 entityColor;

in vec2 vUv;
in vec2 vLm;
in vec4 vTint;
in vec3 vNormal;
in vec3 vPos;
flat in int vMat;

#if defined PROGRAM_TERRAIN || defined PROGRAM_BLOCK || defined PROGRAM_ENTITIES || defined PROGRAM_CLRWL
#define WRITE_SURFACE
#endif

#ifdef WRITE_SURFACE
// Couche 1 : normale de la surface et part de lumière indirecte, lues par l'occlusion d'écran (deferred).
/* RENDERTARGETS: 0,1 */
layout(location = 0) out vec4 outColor;
layout(location = 1) out vec4 outSurface;
#else
/* RENDERTARGETS: 0 */
layout(location = 0) out vec4 outColor;
#endif

#ifdef PROGRAM_WATER
/** Pente de la surface de l'eau : quelques trains d'onde qui se croisent, poussés par le vent. */
vec2 waveSlope(vec2 p, float t) {
    vec2 d = vec2(0.8, 0.6);
    if (brumaireLink > 0.5 && dot(brumaireWind.xy, brumaireWind.xy) > 0.01) d = normalize(brumaireWind.xy);
    vec2 slope = vec2(0.0);
    float f = 0.55, a = 0.060, s = 1.1;
    for (int i = 0; i < 5; i++) {
        float phase = dot(p, d) * f + t * s + float(i) * 1.7;
        phase += sin(dot(p, vec2(-d.y, d.x)) * f * 0.6) * 0.8;
        slope += d * (f * a * cos(phase));
        d = mat2(0.62, 0.78, -0.78, 0.62) * d;
        f *= 1.7; a *= 0.62; s *= 1.25;
    }
    return slope * WATER_WAVES;
}

float viewDistance(float depth) {
    vec4 p = gbufferProjectionInverse * vec4(gl_FragCoord.xy / vec2(viewWidth, viewHeight) * 2.0 - 1.0, depth * 2.0 - 1.0, 1.0);
    return length(p.xyz / p.w);
}

vec4 water(vec3 tint, vec3 normal, vec2 lm) {
    vec3 world = vPos + cameraPosition;
    vec3 n = normal;
    if (normal.y > 0.5) {
        vec2 slope = waveSlope(world.xz, frameTimeCounter);
        n = normalize(vec3(-slope.x, 1.0, -slope.y));
    }
    vec3 eye = normalize(-vPos);
    if (dot(n, eye) < 0.0) n = -n;
    float fresnel = 0.02 + 0.98 * pow(1.0 - max(dot(n, eye), 0.0), 5.0);
    float lit = shadowAt(vPos, normal, dither(gl_FragCoord.xy), 4);
    float sky = lm.y * lm.y;

    // Le fond s'efface avec l'épaisseur d'eau traversée.
    float thickness = max(viewDistance(texelFetch(depthtex1, ivec2(gl_FragCoord.xy), 0).r) - length(vPos), 0.0);
    float murk = 1.0 - exp(-thickness * 0.16);
    vec3 body = tint * vec3(0.55, 0.80, 0.75) * (gAmbient * sky * 0.55 + gLightColor * lit * 0.10 + vec3(0.012));

    vec3 color = body;
    float alpha = mix(0.30, 0.96, murk);
    if (isEyeInWater != 1) {
        vec3 r = reflect(-eye, n);
        r.y = abs(r.y);
        // Le ciel reflété est celui qu'on voit : voilé par la même brume.
        float nearOnly;
        float veil = 1.0 - exp(-mistDepth(r, 4096.0, nearOnly));
        vec3 mirror = mix(skyAt(r), mistColor(r, 1.0), veil) * mix(0.15, 1.0, sky);
        mirror += gLightColor * lit * pow(max(dot(r, gLightDir), 0.0), 420.0) * 18.0;
        color = mix(body, mirror, fresnel);
        alpha = max(alpha, fresnel);
    }
    return vec4(color, alpha);
}
#endif

void main() {
    initEnv();
    vec3 normal = normalize(vNormal);
    vec2 lm = vLm;
    float ao = 1.0;

#if defined PROGRAM_BASIC
    vec4 color = vTint;
#elif defined PROGRAM_CLRWL
    // Flywheel (Create) : Colorwheel applique sa matière, sa lumière et sa découpe.
    vec4 color;
    vec4 overlay;
    vec2 light;
    clrwl_computeFragment(texture(gtexture, vUv), color, light, ao, overlay);
    color.rgb = mix(color.rgb, overlay.rgb, overlay.a);
    lm = lightCoord(light);
#elif defined PROGRAM_TERRAIN || defined PROGRAM_WATER
    // Sur le terrain, l'alpha du sommet porte l'occlusion (separateAo).
    vec4 color = texture(gtexture, vUv);
    color.rgb *= vTint.rgb;
    ao = vTint.a;
#else
    vec4 color = texture(gtexture, vUv) * vTint;
#endif

#if defined PROGRAM_DAMAGEDBLOCK
    // Les fissures multiplient ce qui est déjà dessiné : la texture part telle quelle.
    outColor = color;
    return;
#endif

#ifndef PROGRAM_CLRWL
    if (color.a < alphaTestRef) discard;
#endif
#if defined PROGRAM_ENTITIES
    color.rgb = mix(color.rgb, entityColor.rgb, entityColor.a);
#endif

    vec3 albedo = toLinear(color.rgb);

#if defined PROGRAM_EMISSIVE
    outColor = vec4(albedo * 2.4, color.a);
#elif defined PROGRAM_UNLIT
    outColor = vec4(albedo, color.a);
#elif defined PROGRAM_GLINT
    outColor = vec4(albedo * 0.8, color.a);
#elif defined PROGRAM_WEATHER
    outColor = vec4(albedo * (gAmbient * 1.3 + gLightColor * 0.08 + vec3(0.02)), color.a * 0.75);
#elif defined PROGRAM_CLOUDS
    float under = 0.75 + 0.25 * normal.y;
    outColor = vec4((gAmbient * 1.15 + gLightColor * 0.30 * max(gLightDir.y, 0.0)) * under, 0.82 * color.a);
#else
    float leaf = 0.0;
    int taps = SHADOW_FILTER;
#if defined PROGRAM_TERRAIN || defined PROGRAM_WATER
    albedo = olive(albedo);
    if (vMat >= 1 && vMat <= 5) leaf = vMat == 4 ? 0.55 : 1.0;
#endif
#if defined PROGRAM_PARTICLES || defined PROGRAM_BASIC
    leaf = 1.0;
    taps = 1;
#endif
#ifdef PROGRAM_WATER
    if (vMat == 6) {
        outColor = water(toLinear(vTint.rgb), normal, lm);
        return;
    }
#endif
    float indirect;
    vec3 lit = shade(albedo, normal, lm, ao, vPos, leaf, gl_FragCoord.xy, taps, indirect);
#ifdef WRITE_SURFACE
    // Le feuillage laisse passer la lumière : l'occlusion y est moitié moindre.
    outSurface = vec4(packNormal(normal), indirect * (1.0 - 0.2 * leaf), 1.0);
#endif
#if defined PROGRAM_TERRAIN
    // Les blocs qui éclairent brillent par eux-mêmes.
    if (vMat == 8) lit += albedo * 1.7 * smoothstep(0.25, 0.85, luma(color.rgb));
#endif
    outColor = vec4(lit, color.a);
#endif
}
