// Les rides de l'eau (lib/wakes.glsl) : une équation d'onde amortie sur la carte, deux demi-pas par image (prepare2 de
// A vers B, prepare3 de B vers A, WAKE_PASS), et l'eau poussée
// sous ce qui la traverse. Une source immobile ne fait que de petits ronds ; en avançant, elle laisse un sillage en
// V et de l'écume derrière elle. Loin de la caméra, la carte est remise à plat : en boucle, elle resservira ailleurs.
#include "/lib/settings.glsl"
#include "/lib/wakes.glsl"

layout(local_size_x = 16, local_size_y = 16, local_size_z = 1) in;
const ivec3 workGroups = ivec3(32, 32, 1);

layout(rgba16f) uniform image2D wakeImgA;
layout(rgba16f) uniform image2D wakeImgB;
uniform vec3 cameraPosition;
uniform float frameTime;
uniform float frameTimeCounter;
uniform float brumaireLink;
uniform vec4 brumaireWake0;
uniform vec4 brumaireWake1;
uniform vec4 brumaireWake2;
uniform vec4 brumaireWake3;
uniform vec4 brumaireWake4;
uniform vec4 brumaireWake5;
uniform vec4 brumaireWakeSpeed0;
uniform vec4 brumaireWakeSpeed1;

/** Vitesse des ondes, en blocs par seconde ; amortissement de leur vitesse et de leur hauteur, par seconde. */
const float WAVE_SPEED = 2.6;
const float DRAG = 0.5;
const float SETTLE = 0.6;

vec4 before(ivec2 c) {
    c &= WAKE_SIZE - 1;
#if WAKE_PASS == 0
    return imageLoad(wakeImgA, c);
#else
    return imageLoad(wakeImgB, c);
#endif
}

void store(ivec2 c, vec4 v) {
#if WAKE_PASS == 0
    imageStore(wakeImgB, c, v);
#else
    imageStore(wakeImgA, c, v);
#endif
}

void main() {
    ivec2 c = ivec2(gl_GlobalInvocationID.xy);
    ivec2 cam = ivec2(floor(cameraPosition.xz * WAKE_RES));
    // Le texel du monde, parmi tous ceux qui partagent cette case de la carte, le plus proche de la caméra.
    ivec2 rel = (c - cam) & (WAKE_SIZE - 1);
    rel -= ivec2(greaterThanEqual(rel, ivec2(WAKE_SIZE / 2))) * WAKE_SIZE;
    if (max(abs(rel.x), abs(rel.y)) > WAKE_SIZE / 2 - 16 || brumaireLink < 0.5) {
        store(c, vec4(0.0));
        return;
    }
    vec2 world = (vec2(cam + rel) + 0.5) / WAKE_RES;

    // Un demi-pas : stable tant que la vague ne franchit pas plus d'un demi-texel (au-dessous de 22 images par seconde,
    // les rides ralentissent un peu plutôt que de diverger).
    float dt = clamp(frameTime * 0.5, 0.0, 1.0 / 45.0);
    vec4 s = before(c);
    float lap = before(c + ivec2(1, 0)).r + before(c - ivec2(1, 0)).r + before(c + ivec2(0, 1)).r
              + before(c - ivec2(0, 1)).r - 4.0 * s.r;
    float k = WAVE_SPEED * WAKE_RES;
    float v = (s.g + k * k * lap * dt) * exp(-DRAG * dt);
    float h = (s.r + v * dt) * exp(-SETTLE * dt);
    float foam = s.b * exp(-dt / 3.0);

    vec4 wakes[6] = vec4[6](brumaireWake0, brumaireWake1, brumaireWake2, brumaireWake3, brumaireWake4, brumaireWake5);
    float speeds[6] = float[6](brumaireWakeSpeed0.x, brumaireWakeSpeed0.y, brumaireWakeSpeed0.z, brumaireWakeSpeed0.w,
                               brumaireWakeSpeed1.x, brumaireWakeSpeed1.y);
    for (int i = 0; i < 6; i++) {
        vec4 w = wakes[i];
        if (w.w <= 0.0) continue;
        float q = length(world - (w.xz + cameraPosition.xz)) / w.w;
        if (q >= 1.0) continue;
        float fall = 1.0 - q * q;
        float speed = min(speeds[i], 6.0);
        // Ce qui flotte creuse l'eau sous lui, un peu plus en avançant, et la rend en partant : c'est ce creux
        // relâché, case après case, que l'équation d'onde change en sillage. Immobile, il ne fait que bercer.
        float target = -fall * (0.08 + 0.04 * speed + 0.015 * sin(frameTimeCounter * 3.0 + float(i) * 1.7));
        h = min(h, mix(h, target, min(fall * 2.0, 1.0)));
        // Derrière ce qui file, l'eau battue blanchit.
        foam = max(foam, fall * smoothstep(0.5, 3.0, speed) * 0.85);
    }
    // Là où la ride devient trop raide, elle se brise en écume.
    vec2 grad = vec2(before(c + ivec2(1, 0)).r - before(c - ivec2(1, 0)).r,
                     before(c + ivec2(0, 1)).r - before(c - ivec2(0, 1)).r) * (WAKE_RES * 0.5);
    foam = max(foam, smoothstep(0.45, 0.9, length(grad)) * 0.45);
    store(c, vec4(clamp(h, -1.0, 1.0), clamp(v, -20.0, 20.0), clamp(foam, 0.0, 1.0), 0.0));
}
