// L'occlusion d'écran : la lumière du ciel et des blocs pénètre moins dans les creux, sous les feuillages, au pied
// des murs. Calculée ici sur un demi-écran, puis lissée et appliquée par deferred1.
#include "/lib/settings.glsl"

uniform sampler2D depthtex0;
uniform sampler2D colortex1;
uniform mat4 gbufferProjection;
uniform mat4 gbufferProjectionInverse;
uniform mat4 gbufferModelView;
uniform float viewWidth;
uniform float viewHeight;
uniform int frameCounter;

in vec2 vUv;

/*
const int colortex1Format = RGBA16;
const int colortex2Format = R8;
*/
const vec4 colortex1ClearColor = vec4(0.0, 0.0, 0.0, 0.0);
const vec4 colortex2ClearColor = vec4(1.0, 1.0, 1.0, 1.0);

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

void main() {
    outAo = vec4(1.0);
#if AO_SAMPLES > 0
    // Ce programme ne dessine qu'un quart de l'écran (scale.deferred) : vUv couvre tout l'écran.
    vec2 uv = vUv;
    float depth = texture(depthtex0, uv).r;
    vec4 surface = texture(colortex1, uv);
    if (depth >= 1.0 || depth < 0.56 || surface.a < 0.5) return;
    vec3 p = viewAt(uv, depth);
    vec3 n = normalize(mat3(gbufferModelView) * unpackNormal(surface.rg));
    // Un repère tangent tourné d'un pixel à l'autre : les prises couvrent l'hémisphère sans motif visible.
    float noise = fract(52.9829189 * fract(dot(gl_FragCoord.xy, vec2(0.06711056, 0.00583715))));
    vec3 t = normalize(cross(n, abs(n.y) < 0.9 ? vec3(0.0, 1.0, 0.0) : vec3(1.0, 0.0, 0.0)));
    vec3 b = cross(n, t);
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
    outAo = vec4(pow(1.0 - occlusion / float(AO_SAMPLES), AO_STRENGTH));
#endif
}
