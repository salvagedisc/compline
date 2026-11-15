#define FSH
#include "/lib/settings.glsl"
#include "/lib/common.glsl"
#include "/lib/shadow.glsl"
#include "/lib/light.glsl"
#ifdef PROGRAM_DH_WATER
#include "/lib/mist.glsl"
#endif

uniform sampler2D depthtex0;

in vec2 vLm;
in vec4 vTint;
in vec3 vNormal;
in vec3 vPos;
flat in int vMat;

/* RENDERTARGETS: 0 */
layout(location = 0) out vec4 outColor;

void main() {
    // Le terrain du jeu passe devant : le lointain s'efface là où il existe, et tout près de la caméra.
    float dist = length(vPos);
    if (dist < far * 0.78 - 20.0 * dither(gl_FragCoord.xy)) discard;
#ifdef PROGRAM_DH_WATER
    if (texelFetch(depthtex0, ivec2(gl_FragCoord.xy), 0).r < 1.0) discard;
#endif
    initEnv();
    vec3 normal = normalize(vNormal);
    vec3 albedo = olive(toLinear(vTint.rgb));
    float leaf = vMat == DH_BLOCK_LEAVES ? 0.55 : 0.0;

#ifdef PROGRAM_DH_WATER
    if (vMat == DH_BLOCK_WATER) {
        vec3 eye = normalize(-vPos);
        float fresnel = 0.02 + 0.98 * pow(1.0 - max(dot(normal, eye), 0.0), 5.0);
        float sky = vLm.y * vLm.y;
        vec3 body = toLinear(vTint.rgb) * vec3(0.55, 0.80, 0.75) * (gAmbient * sky * 0.55 + gLightColor * 0.10 + vec3(0.012));
        vec3 r = reflect(-eye, normal);
        r.y = abs(r.y);
        float nearOnly;
        float veil = 1.0 - exp(-mistDepth(r, 4096.0, nearOnly));
        vec3 mirror = mix(skyAt(r), mistColor(r, 1.0), veil) * mix(0.15, 1.0, sky);
        outColor = vec4(mix(body, mirror, fresnel), max(0.9, fresnel));
        return;
    }
#endif
    float indirect;
    vec3 lit = shade(albedo, normal, vLm, 1.0, vPos, leaf, gl_FragCoord.xy, 1, indirect);
    if (vMat == DH_BLOCK_ILLUMINATED || vMat == DH_BLOCK_LAVA) lit += albedo * 1.5;
    outColor = vec4(lit, vTint.a);
}
