// La brume et ses rayons de lumière, puis l'eau quand la caméra y plonge.
#define FSH
#include "/lib/settings.glsl"
#include "/lib/common.glsl"
#include "/lib/shadow.glsl"
#include "/lib/mist.glsl"
#include "/program/scene.glsl"

uniform sampler2D colortex0;
in vec2 vUv;

/* RENDERTARGETS: 0 */
layout(location = 0) out vec4 outColor;

/**
 * Marche vers le fond de la vue dans la carte d'ombre. mistSun : part de soleil qui atteint la brume (pondérée par
 * sa densité) ; rend la longueur éclairée du rayon, en blocs, qui fait briller l'air : les rayons sous les arbres.
 */
float shaftMarch(vec3 dir, float reach, float density, out float mistSun) {
    mistSun = 1.0;
#if LIGHT_SHAFTS > 0
    float base = mistAnchor();
    float stride = reach / float(LIGHT_SHAFTS);
    float t = stride * dither(gl_FragCoord.xy);
    vec3 origin = gbufferModelViewInverse[3].xyz;
    float sum = 0.0, weight = 0.0, lit = 0.0;
    for (int i = 0; i < LIGHT_SHAFTS; i++) {
        vec3 p = origin + dir * t;
        vec3 uv = shadowWarp((shadowProjection * (shadowModelView * vec4(p, 1.0))).xyz) * 0.5 + 0.5;
        float s = uv.z < 1.0 ? texture(shadowtex1, vec3(uv.xy, uv.z - 0.0002)) : 1.0;
        float w = exp((base - cameraPosition.y - p.y) / MIST_HEIGHT - density * t);
        sum += w * s;
        weight += w;
        lit += s;
        t += stride;
    }
    mistSun = sum / max(weight, 1e-6);
    return lit * stride;
#else
    return 0.0;
#endif
}

void main() {
    initEnv();
    vec3 color = texture(colortex0, vUv).rgb;
    bool sky, hand;
    vec3 pos = scenePosition(vUv, sky, hand);
    float len = sky ? 16384.0 : length(pos);
    vec3 dir = normalize(pos);

    if (isEyeInWater == 1) {
        float t = 1.0 - exp(-min(len, 512.0) * 0.055);
        vec3 deep = vec3(0.03, 0.14, 0.20) * (gAmbient * 1.2 + 0.02) * (float(eyeBrightnessSmooth.y) / 240.0 * 0.8 + 0.2);
        color = mix(color * mix(vec3(1.0), vec3(0.55, 0.85, 0.90), t), deep, t);
    } else if (isEyeInWater >= 2) {
        float t = 1.0 - exp(-len * (isEyeInWater == 2 ? 1.2 : 0.7));
        color = mix(color, isEyeInWater == 2 ? vec3(1.6, 0.35, 0.04) : vec3(0.75, 0.82, 0.90), t);
    } else if (!hand) {
        if (!sky) {
            vec3 inscat;
            vec3 pass = airVeil(dir, len, inscat);
            color = color * pass + inscat;
        }
        float nearOnly;
        float depth = mistDepth(dir, len, nearOnly);
        float veil = 1.0 - exp(-depth);
        float sun = 1.0;
#if LIGHT_SHAFTS > 0
        if (dot(gLightColor, vec3(1.0)) > 0.01) {
            float reach = min(len, shadowDistance * 0.9);
            float density = nearOnly / max(len, 1.0);
            float mistSun;
            float litLength = shaftMarch(dir, reach, density, mistSun);
            // La part de brume traversée dans la portée de la carte d'ombre ; au-delà, elle est tenue pour éclairée.
            float inside = 1.0 - exp(-nearOnly * reach / max(len, 1e-3));
            sun = mix(1.0, mistSun, clamp(inside / max(veil, 1e-4), 0.0, 1.0));
            // L'air lui-même renvoie un peu de soleil, surtout vers l'avant : là où l'ombre des feuillages le
            // découpe, des rayons. Seulement à l'air libre.
            float mu = dot(dir, gLightDir);
            float phase = 0.7 * mistPhase(mu, 0.75) + 0.3 * mistPhase(mu, 0.2);
            float open = smoothstep(0.2, 0.7, float(eyeBrightnessSmooth.y) / 240.0);
            color += gLightColor * phase * litLength * (0.006 * SHAFT_STRENGTH) * mix(0.8, 1.6, airHumid) * open;
        }
#endif
        if (veil > 0.003) {
            // La brume lointaine (l'anneau) prend la couleur du ciel bas dans cette direction : au couchant, elle
            // rougeoit du côté du soleil ; la brume proche garde sa propre lumière.
            vec3 mist = mistColor(dir, sun);
            float far_ = clamp((depth - nearOnly) / max(depth, 1e-4), 0.0, 1.0);
            mist = mix(mist, skyAt(normalize(vec3(dir.x, 0.03, dir.z))) * mistTint() * 0.9, far_ * 0.75);
            color = mix(color, mist, veil);
        }
    }
    if (blindness > 0.0) color *= exp(-len * 0.35 * blindness);
    outColor = vec4(color, 1.0);
}
