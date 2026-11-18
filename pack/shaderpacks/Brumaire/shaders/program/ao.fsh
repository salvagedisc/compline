// L'occlusion d'écran : la lumière du ciel et des blocs pénètre moins dans les creux, sous les feuillages, au pied
// des murs. Et le premier rebond : la lumière que les surfaces voisines renvoient, cherchée le long de rayons tirés
// dans l'hémisphère au-dessus de chaque point (un mur au soleil éclaire le sol à son pied, une herbe verte teinte le
// dessous d'une avancée, la lave rougit la roche autour). Calculés ici sur un demi-écran, puis lissés et appliqués
// par deferred1 : r l'occlusion, gba la lumière reçue des voisines (à multiplier par la couleur de la surface).
#include "/lib/settings.glsl"

uniform sampler2D depthtex0;
uniform sampler2D colortex1;
uniform sampler2D colortex0;
uniform mat4 gbufferProjection;
uniform mat4 gbufferProjectionInverse;
uniform mat4 gbufferModelView;
uniform float viewWidth;
uniform float viewHeight;
uniform int frameCounter;

in vec2 vUv;

/*
const int colortex1Format = RGBA16;
const int colortex2Format = RGBA16F;
const int colortex4Format = RGBA8;
*/
const vec4 colortex1ClearColor = vec4(0.0, 0.0, 0.0, 0.0);
const vec4 colortex2ClearColor = vec4(1.0, 0.0, 0.0, 0.0);
const vec4 colortex4ClearColor = vec4(0.0, 0.0, 0.0, 0.0);

/* RENDERTARGETS: 2 */
layout(location = 0) out vec4 outAo;

vec3 viewAt(vec2 uv, float depth) {
    vec4 v = gbufferProjectionInverse * vec4(vec3(uv, depth) * 2.0 - 1.0, 1.0);
    return v.xyz / v.w;
}

vec3 unpackNormal(vec2 p) {
    p = p * 2.0 - 1.0;
    vec3 n = vec3(p.x, 1.0 - abs(p.x) - abs(p.y), p.y);
    if (n.y < 0.0) n.xz = (1.0 - abs(n.zx)) * vec2(n.x >= 0.0 ? 1.0 : -1.0, n.z >= 0.0 ? 1.0 : -1.0);
    return normalize(n);
}

/**
 * Le premier rebond en p (repère vue), de normale n : des rayons tirés selon le cosinus, marchés à l'écran par pas
 * qui s'allongent jusqu'à quelques blocs ; le premier qu'ils touchent renvoie sa lumière (lue dans l'image éclairée).
 * Moyenne de ce qu'ils rapportent : la lumière reçue, à multiplier par la couleur de la surface.
 */
vec3 bounceLight(vec3 p, vec3 n, vec3 t, vec3 b, float noise) {
    vec3 gather = vec3(0.0);
#if SSGI > 0
    for (int i = 0; i < SSGI; i++) {
        float u1 = fract(noise * 0.754877 + float(i) * 0.618034);
        float u2 = fract(noise * 0.569840 + float(i) * 0.381966 + 0.25);
        float r = sqrt(u1);
        float a = 6.2831853 * u2;
        vec3 dir = t * (cos(a) * r) + b * (sin(a) * r) + n * sqrt(max(1.0 - u1, 0.0));
        float reach = 0.15 * (0.75 + 0.5 * fract(noise * 3.7 + float(i) * 0.31));
        for (int s = 0; s < 10; s++) {
            vec3 q = p + n * 0.04 + dir * reach;
            reach *= 1.45;
            vec4 clip = gbufferProjection * vec4(q, 1.0);
            vec2 quv = clip.xy / clip.w * 0.5 + 0.5;
            if (any(lessThan(quv, vec2(0.0))) || any(greaterThan(quv, vec2(1.0)))) break;
            float d = texture(depthtex0, quv).r;
            if (d >= 1.0) continue;
            float scene = viewAt(quv, d).z;
            float behind = scene - q.z;
            if (behind > 0.02) {
                // Passé derrière une surface : touchée si elle est mince devant le rayon, sinon le rayon est caché.
                // L'épaisseur admise croît avec le pas : un sol vu de biais saute vite en profondeur.
                if (behind < 0.5 + 0.6 * reach && d >= 0.56) gather += min(texture(colortex0, quv).rgb, vec3(6.0));
                break;
            }
        }
    }
    gather /= float(SSGI);
#endif
    return gather;
}

void main() {
    outAo = vec4(1.0, 0.0, 0.0, 0.0);
#if AO_SAMPLES > 0 || SSGI > 0
    // Ce programme ne dessine qu'un quart de l'écran (scale.deferred) : vUv couvre tout l'écran.
    vec2 uv = vUv;
    float depth = texture(depthtex0, uv).r;
    vec4 surface = texture(colortex1, uv);
    if (depth >= 1.0 || depth < 0.56 || surface.a < 0.5) return;
    vec3 p = viewAt(uv, depth);
    vec3 n = normalize(mat3(gbufferModelView) * unpackNormal(surface.rg));
    // Un repère tangent tourné d'un pixel à l'autre : les prises couvrent l'hémisphère sans motif visible.
    float noise = fract(52.9829189 * fract(dot(gl_FragCoord.xy, vec2(0.06711056, 0.00583715))));
    noise = fract(noise + 0.618034 * float(frameCounter & 15));
    vec3 t = normalize(cross(n, abs(n.y) < 0.9 ? vec3(0.0, 1.0, 0.0) : vec3(1.0, 0.0, 0.0)));
    vec3 b = cross(n, t);
    float ao = 1.0;
#if AO_SAMPLES > 0
    float occlusion = 0.0;
    for (int i = 0; i < AO_SAMPLES; i++) {
        float f = (float(i) + noise) / float(AO_SAMPLES);
        float r = sqrt(fract(f * 7.31 + noise * 0.37));
        float a = 6.2831853 * fract(f * 0.618034 * float(AO_SAMPLES) + noise);
        vec3 h = vec3(cos(a) * r, sin(a) * r, sqrt(max(1.0 - r * r, 0.0)));
        float reach = AO_RADIUS * mix(0.15, 1.0, f * f);
        vec3 q = p + (t * h.x + b * h.y + n * h.z) * reach;
        vec4 clip = gbufferProjection * vec4(q, 1.0);
        vec2 quv = clip.xy / clip.w * 0.5 + 0.5;
        if (any(lessThan(quv, vec2(0.0))) || any(greaterThan(quv, vec2(1.0)))) continue;
        float scene = viewAt(quv, texture(depthtex0, quv).r).z;
        // Le point est caché si la scène passe devant lui ; un objet bien plus proche ne compte pas.
        float range = smoothstep(0.0, 1.0, AO_RADIUS / max(abs(p.z - scene), 1e-3));
        occlusion += step(q.z + 0.03, scene) * range;
    }
    ao = pow(1.0 - occlusion / float(AO_SAMPLES), AO_STRENGTH);
#endif
    // Le rebond s'efface au loin : il n'y a plus assez de pixels pour le chercher, et le lointain ne le montre pas.
    vec3 bounce = bounceLight(p, n, t, b, noise) * (1.0 - smoothstep(48.0, 96.0, -p.z));
    outAo = vec4(ao, bounce);
#endif
}
