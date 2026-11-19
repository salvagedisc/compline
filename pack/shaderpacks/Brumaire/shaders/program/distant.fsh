#define FSH
#include "/lib/settings.glsl"
#include "/lib/common.glsl"
#include "/lib/shadow.glsl"
#include "/lib/light.glsl"
#ifdef PROGRAM_DH_WATER
#include "/lib/mist.glsl"
#endif
#ifdef PROGRAM_DH_TERRAIN
#include "/lib/alpenglow.glsl"
#include "/lib/auroraglow.glsl"
#include "/lib/groundtint.glsl"
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
        vec3 mirror = mix(skyAt(r), mistColor(r, 1.0), veil) * mix(0.15, 1.0, sky);
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
