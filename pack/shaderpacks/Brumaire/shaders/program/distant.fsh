#define FSH
#include "/lib/settings.glsl"
#include "/lib/common.glsl"
#include "/lib/shadow.glsl"
#include "/lib/light.glsl"
#ifdef PROGRAM_DH_WATER
#include "/lib/mist.glsl"
#if FAR_WATER_SKY > 0
#if WATER_STARS > 0
#include "/lib/nightsky.glsl"
#endif
#include "/lib/reflect.glsl"
#endif
#endif
#ifdef PROGRAM_DH_TERRAIN
#include "/lib/alpenglow.glsl"
#include "/lib/auroraglow.glsl"
#include "/lib/groundtint.glsl"
#if SNOW_COVER > 0
uniform float snowHere;
#endif
#if WETNESS > 0
uniform float wetness;
uniform float rainHere;
#endif
#endif

uniform sampler2D depthtex0;

in vec2 vLm;
in vec4 vTint;
in vec3 vNormal;
in vec3 vPos;
flat in int vMat;

#ifdef PROGRAM_DH_TERRAIN
/* RENDERTARGETS: 0,1 */
layout(location = 0) out vec4 outColor;
layout(location = 1) out vec4 outMark;
#else
/* RENDERTARGETS: 0 */
layout(location = 0) out vec4 outColor;
#endif

void main() {
    // Le terrain du jeu passe devant : le lointain s'efface là où il existe, et tout près de la caméra.
    float dist = length(vPos);
    if (dist < far * 0.78 - 20.0 * dither(gl_FragCoord.xy)) discard;
#ifdef PROGRAM_DH_WATER
    if (texelFetch(depthtex0, ivec2(gl_FragCoord.xy), 0).r < 1.0) discard;
#endif
#ifdef PROGRAM_DH_TERRAIN
    // La marque du lointain dans la couche des surfaces (0,25 : sous le seuil des surfaces de solid.fsh, sans
    // occlusion ni reflet) : program/scene.glsl ne croit la profondeur de Distant Horizons que là où elle est posée.
    outMark = vec4(0.0, 0.0, 0.0, 0.25);
#endif
    initEnv();
    vec3 normal = normalize(vNormal);
    vec3 albedo = olive(toLinear(vTint.rgb));
    float leaf = vMat == DH_BLOCK_LEAVES ? 0.55 : 0.0;
#if defined PROGRAM_DH_TERRAIN && GROUND_TINT > 0
    if (vMat != DH_BLOCK_ILLUMINATED && vMat != DH_BLOCK_LAVA) albedo = groundTint(albedo, vPos + cameraPosition, groundGreen(vTint.rgb));
#endif
    float snowed = 0.0;
#if defined PROGRAM_DH_TERRAIN && SNOW_COVER > 0
    // La neige que le terrain proche reçoit (lib/cover.glsl) gagne aussi le lointain : le dessus de ce qui voit le
    // ciel blanchit, un feuillage en partie, sans quoi l'hiver s'arrête net au bord des blocs du jeu. Le sable des
    // déserts et des badlands, qui ne voit jamais la neige, reste nu.
    if (snowHere > 0.0 && vMat != DH_BLOCK_WATER && vMat != DH_BLOCK_LAVA && vMat != DH_BLOCK_ILLUMINATED) {
        float sandy = smoothstep(0.10, 0.20, albedo.r - albedo.b) * step(albedo.g, albedo.r) * smoothstep(0.15, 0.30, luma(albedo));
        float top = smoothstep(0.5, 0.8, normal.y) * smoothstep(0.80, 0.95, vLm.y);
        float s = snowHere * top * (vMat == DH_BLOCK_LEAVES ? 0.65 : 1.0) * (1.0 - sandy);
        albedo = mix(albedo, vec3(0.56, 0.59, 0.64) * 0.95, s);
        snowed = s;
    }
#endif
#if defined PROGRAM_DH_TERRAIN && WETNESS > 0
    // Le sol trempé du proche (solid.fsh) fonce aussi au loin, sans quoi l'averse s'arrête au bord des blocs du jeu :
    // le dessus plus que les flancs, les feuillages un peu, le sable et la neige pas du tout.
    if (vMat != DH_BLOCK_WATER && vMat != DH_BLOCK_LAVA && vMat != DH_BLOCK_ILLUMINATED && vMat != DH_BLOCK_SNOW) {
        float wet = clamp(wetness * rainHere * WETNESS, 0.0, 1.0) * smoothstep(0.82, 0.96, vLm.y);
        if (wet > 0.0) {
            float sandy = smoothstep(0.10, 0.20, albedo.r - albedo.b) * step(albedo.g, albedo.r) * smoothstep(0.15, 0.30, luma(albedo));
            float soak = wet * 0.75 * mix(0.55, 1.0, max(normal.y, 0.0)) * (1.0 - sandy) * (1.0 - snowed);
            if (vMat == DH_BLOCK_LEAVES) albedo *= 1.0 - 0.10 * wet * (1.0 - snowed);
            else albedo = mix(vec3(luma(albedo)), albedo, 1.0 + 0.2 * soak) * mix(1.0, 0.62, soak);
        }
    }
#endif

#ifdef PROGRAM_DH_WATER
    if (vMat == DH_BLOCK_WATER) {
        // L'eau au loin : les mêmes lois que de près (lib/water.glsl), mais ses vagues sont plus fines que le pixel ;
        // toute leur pente est devenue rugosité, qui étale l'éclat de l'astre en un chemin de lumière.
        vec3 eye = normalize(-vPos);
        float sky = vLm.y * vLm.y;
        vec3 world = vPos + cameraPosition;
        float lit = cloudShadow(world, gLightDir);
        vec3 hue = waterHue(vTint.rgb);
        WaveEnv env = waveEnvDefault();
        env.open = 1.0;
        ivec3 trains;
        vec3 rms;
        waveBands(WATER_WAVE_COUNT, env, trains, rms);
        float rough = clamp(sqrt(2.0 * dot(rms, rms) + 0.0009 + 0.004 * rainStrength), 0.04, 0.45);
        float cosV = max(dot(normal, eye), 0.0);
        float F = min(fresnel(cosV, 1.0, WATER_IOR) * 1.35 + 0.012, 1.0);
        vec3 down = refract(-eye, normal, 1.0 / WATER_IOR);
        float sunIn = 1.0 - fresnel(max(gLightDir.y, 0.0), 1.0, WATER_IOR);
        vec3 body = waterInscatter(hue, down, 64.0, gLightColor * lit * sunIn, gAmbient * max(sky, 0.06));
        vec3 r = reflect(-eye, normal);
        r.y = abs(r.y);
        float nearOnly;
        float veil = 1.0 - exp(-mistDepth(r, 4096.0, nearOnly));
#if FAR_WATER_SKY > 0
        // Le même ciel que dans l'eau proche (lib/reflect.glsl) : les nuages qui passent et, la nuit, les étoiles ;
        // sans quoi le lac au loin reste d'un bleu uni sous un ciel chargé.
        vec3 mirror = skyReflection(world, r) * mix(0.15, 1.0, sky);
#else
        vec3 mirror = mix(skyAt(r), mistColor(r, 1.0), veil) * mix(0.15, 1.0, sky);
#endif
        vec3 color = mix(body, mirror, F);
        float nl = max(gLightDir.y, 0.0);
        if (nl > 0.0) {
            vec3 h = normalize(gLightDir + eye);
            float spec = ggx(max(h.y, 0.0), rough) * smithG1(nl, rough) * smithG1(max(cosV, 1e-3), rough)
                * fresnel(max(dot(eye, h), 0.0), 1.0, WATER_IOR) / (4.0 * max(cosV, 0.05));
            color += gLightColor * lit * min(spec, 30.0) * (1.0 - 0.8 * rainStrength) * (1.0 - veil);
        }
        outColor = vec4(color, max(0.9, F));
        return;
    }
#endif
    float indirect;
    vec3 lit = shade(albedo, normal, vLm, 1.0, vPos, leaf, gl_FragCoord.xy, 1, indirect);
    if (vMat == DH_BLOCK_ILLUMINATED || vMat == DH_BLOCK_LAVA) lit += albedo * 1.5;
#if defined PROGRAM_DH_TERRAIN && ALPENGLOW > 0 && defined DH_BLOCK_SNOW
    if (vMat == DH_BLOCK_SNOW) lit += alpenglow(albedo, normal, vPos + cameraPosition, vLm.y * vLm.y);
#endif
#if defined PROGRAM_DH_TERRAIN && AURORA_GLOW > 0 && SKY_AURORA > 0
    lit += auroraGlow(albedo, normal, vPos + cameraPosition, vLm.y * vLm.y);
#endif
    outColor = vec4(lit, vTint.a);
}
