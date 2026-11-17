// Anticrénelage temporel (lib/taa.glsl) : l'image du moment, fondue avec l'historique ramené là où chaque point se
// trouvait à l'image d'avant. L'historique est tenu dans la boîte des couleurs voisines du moment : ce qui vient
// d'apparaître ou a bougé ne laisse pas de traînée.
#include "/lib/settings.glsl"
#include "/lib/common.glsl"
#include "/lib/reproject.glsl"

uniform sampler2D colortex7;
uniform sampler2D colortex10;
uniform sampler2D depthtex0;
in vec2 vUv;

/*
const int colortex10Format = RGBA16;
*/
const bool colortex10Clear = false;

/* RENDERTARGETS: 7,10 */
layout(location = 0) out vec4 outColor;
layout(location = 1) out vec4 outHistory;

vec3 toYCoCg(vec3 c) {
    return vec3(0.25 * c.r + 0.5 * c.g + 0.25 * c.b, 0.5 * c.r - 0.5 * c.b, -0.25 * c.r + 0.5 * c.g - 0.25 * c.b);
}

vec3 fromYCoCg(vec3 c) {
    float t = c.x - c.z;
    return vec3(t + c.y, c.x + c.z, t - c.y);
}

/** L'historique lu en Catmull-Rom (cinq lectures filtrées) : il ne s'adoucit pas d'une image à l'autre. */
vec3 historyAt(vec2 uv, vec2 size) {
    vec2 pos = uv * size;
    vec2 c = floor(pos - 0.5) + 0.5;
    vec2 f = pos - c;
    vec2 w0 = f * (-0.5 + f * (1.0 - 0.5 * f));
    vec2 w1 = 1.0 + f * f * (-2.5 + 1.5 * f);
    vec2 w2 = f * (0.5 + f * (2.0 - 1.5 * f));
    vec2 w3 = f * f * (-0.5 + 0.5 * f);
    vec2 w12 = w1 + w2;
    vec2 t0 = (c - 1.0) / size;
    vec2 t3 = (c + 2.0) / size;
    vec2 t12 = (c + w2 / w12) / size;
    vec3 sum = texture(colortex10, vec2(t12.x, t0.y)).rgb * (w12.x * w0.y)
             + texture(colortex10, vec2(t0.x, t12.y)).rgb * (w0.x * w12.y)
             + texture(colortex10, t12).rgb * (w12.x * w12.y)
             + texture(colortex10, vec2(t3.x, t12.y)).rgb * (w3.x * w12.y)
             + texture(colortex10, vec2(t12.x, t3.y)).rgb * (w12.x * w3.y);
    float weight = w12.x * w0.y + w0.x * w12.y + w12.x * w12.y + w3.x * w12.y + w12.x * w3.y;
    return max(sum / weight, 0.0);
}

void main() {
    ivec2 pixel = ivec2(gl_FragCoord.xy);
    vec3 color = texelFetch(colortex7, pixel, 0).rgb;
#if TAA > 0
    vec2 size = vec2(viewWidth, viewHeight);
    // La boîte des couleurs voisines (moyenne et écart), et le point le plus proche autour : un bord d'objet
    // emporte son historique avec lui, pas celui du fond.
    vec3 m1 = vec3(0.0), m2 = vec3(0.0);
    float depth = 1.0;
    for (int y = -1; y <= 1; y++) {
        for (int x = -1; x <= 1; x++) {
            ivec2 at = clamp(pixel + ivec2(x, y), ivec2(0), ivec2(size) - 1);
            vec3 c = toYCoCg(texelFetch(colortex7, at, 0).rgb);
            m1 += c;
            m2 += c * c;
            depth = min(depth, texelFetch(depthtex0, at, 0).r);
        }
    }
    vec3 mean = m1 / 9.0;
    vec3 spread = sqrt(max(m2 / 9.0 - mean * mean, 0.0));
    vec3 lo = mean - 1.25 * spread, hi = mean + 1.25 * spread;

    vec2 uv = gl_FragCoord.xy / size;
    vec2 before = previousUv(uv, depth);
    if (all(greaterThanEqual(before, vec2(0.0))) && all(lessThanEqual(before, vec2(1.0)))) {
        vec3 now = toYCoCg(color);
        vec3 past = toYCoCg(historyAt(before, size));
        // Tenu dans la boîte : on ramène l'historique vers la moyenne jusqu'à ce qu'il y entre.
        vec3 toPast = past - mean;
        vec3 reach = max(hi - mean, vec3(1e-4)) / max(abs(toPast), vec3(1e-4));
        past = mean + toPast * clamp(min(reach.x, min(reach.y, reach.z)), 0.0, 1.0);
        // Plus le point a bougé à l'écran, moins on garde du passé : le flou de reprise reste court.
        float moved = length((uv - before) * size);
        float keep = mix(0.9, 0.72, clamp(moved / 12.0, 0.0, 1.0));
        // Un pixel de texture plus grand qu'un pixel d'écran n'a pas besoin d'être lissé : on garde surtout l'image
        // du moment pour que le dessin des blocs proches reste net. Le lissage complet ne sert qu'au loin, là où
        // les pixels de texture deviennent plus fins que l'écran et scintillent.
        float here = texelFetch(depthtex0, pixel, 0).r;
        if (here < 1.0) {
            vec4 view = gbufferProjectionInverse * vec4(vec3(uv, here) * 2.0 - 1.0, 1.0);
            float texel = gbufferProjection[1][1] * 0.5 * size.y / (16.0 * max(length(view.xyz / view.w), 1e-3));
            keep = min(keep, mix(0.9, 0.5, smoothstep(0.6, 1.4, texel)));
        }
        color = clamp(fromYCoCg(mix(now, past, keep)), 0.0, 1.0);
    }
#endif
    outColor = vec4(color, 1.0);
    outHistory = vec4(color, 1.0);
}
