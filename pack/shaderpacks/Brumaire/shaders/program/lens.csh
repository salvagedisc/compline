// L'œil et l'objectif, une fois par image : la clarté moyenne de la scène (moyenne géométrique, plus de poids au
// centre), à laquelle l'œil s'habitue peu à peu (plus vite à la lumière qu'au noir), et la distance de ce qu'on
// regarde, sur laquelle l'objectif fait le point. Rangées dans lensImg (un seul texel), relues par grade.fsh.
#include "/lib/settings.glsl"

layout(local_size_x = 16, local_size_y = 9) in;
const ivec3 workGroups = ivec3(1, 1, 1);
layout(rgba16f) uniform image2D lensImg;

uniform sampler2D colortex0;
uniform sampler2D depthtex0;
uniform float frameTime;
uniform float near;
uniform float far;

shared float sumLog[144];
shared float sumWeight[144];

/** Distance (blocs) d'un point de l'écran ; la main et le ciel comptent pour loin. */
float distanceAt(vec2 uv) {
    float d = texture(depthtex0, uv).r;
    if (d < 0.56 || d >= 1.0) return far * 2.0;
    float z = d * 2.0 - 1.0;
    return 2.0 * near * far / (far + near - z * (far - near));
}

void main() {
    ivec2 id = ivec2(gl_LocalInvocationID.xy);
    float s = 0.0;
    float w = 0.0;
    // Une grille de 32 × 18 points, chacun la moyenne d'une petite croix pour ne pas tomber sur un seul pixel.
    for (int j = 0; j < 2; j++) {
        for (int i = 0; i < 2; i++) {
            vec2 uv = (vec2(id * 2 + ivec2(i, j)) + 0.5) / vec2(32.0, 18.0);
            vec2 px = 3.0 / vec2(textureSize(colortex0, 0));
            vec3 c = texture(colortex0, uv).rgb * 0.4
                   + (texture(colortex0, uv + vec2(px.x, 0.0)).rgb + texture(colortex0, uv - vec2(px.x, 0.0)).rgb
                    + texture(colortex0, uv + vec2(0.0, px.y)).rgb + texture(colortex0, uv - vec2(0.0, px.y)).rgb) * 0.15;
            float l = dot(c, vec3(0.2126, 0.7152, 0.0722));
            vec2 q = (uv - 0.5) * vec2(1.6, 1.0);
            float cw = exp(-dot(q, q) * 3.0);
            s += log(max(l, 1e-4)) * cw;
            w += cw;
        }
    }
    int k = id.y * 16 + id.x;
    sumLog[k] = s;
    sumWeight[k] = w;
    memoryBarrierShared();
    barrier();
    if (k != 0) return;
    float S = 0.0;
    float W = 0.0;
    for (int i = 0; i < 144; i++) {
        S += sumLog[i];
        W += sumWeight[i];
    }
    float scene = exp(S / max(W, 1e-4));
    vec4 prev = imageLoad(lensImg, ivec2(0));
    bool fresh = !(prev.x > 0.0 && prev.x < 1e4) || !(prev.y > 0.0 && prev.y < 1e5);
    // L'œil s'habitue à la lumière en une seconde ou deux, au noir en plusieurs.
    float rate = (scene > prev.x ? 1.8 : 0.45) * EXPOSURE_SPEED;
    float lum = fresh ? scene : mix(prev.x, scene, 1.0 - exp(-frameTime * rate));
    // Le point : la plus proche de cinq visées autour du centre, suivie en un quart de seconde environ.
    float target = distanceAt(vec2(0.5));
    target = min(target, distanceAt(vec2(0.47, 0.5)));
    target = min(target, distanceAt(vec2(0.53, 0.5)));
    target = min(target, distanceAt(vec2(0.5, 0.46)));
    target = min(target, distanceAt(vec2(0.5, 0.54)));
    float focus = fresh ? target : exp(mix(log(prev.y), log(max(target, 0.1)), 1.0 - exp(-frameTime * 4.0)));
    imageStore(lensImg, ivec2(0), vec4(lum, focus, scene, 1.0));
}
