// Les rides de ce qui passe sur l'eau : une carte de 512 × 512 texels au huitième de bloc (64 blocs de côté),
// accrochée au monde et réutilisée en boucle autour de la caméra. Chaque texel garde la hauteur de la surface (r), sa
// vitesse (g) et l'écume (b). prepare2 (program/wakes.csh) y fait courir les ondes d'une image à l'autre et y pousse
// l'eau sous les barques, les nageurs et les bêtes qui pataugent (brumaireWake0..5, donnés par le mod compline) :
// une source qui avance laisse d'elle-même un sillage en V. La surface de l'eau (program/solid.fsh) s'y penche et y
// prend l'écume. Chaque image fait deux demi-pas, de A vers B puis de B vers A : A tient toujours le dernier état.

#define WAKE_SIZE 512
#define WAKE_RES 8.0

#if defined FSH && WATER_WAKES > 0
uniform sampler2D wakeTexA;

vec4 wakeAt(ivec2 c) {
    return texelFetch(wakeTexA, c & (WAKE_SIZE - 1), 0);
}

/** Pente des rides (x, z) et écume (0..1) en un point du monde. */
vec3 wakeSlope(vec2 world) {
    ivec2 c = ivec2(floor(world * WAKE_RES));
    float hx = wakeAt(c + ivec2(1, 0)).r - wakeAt(c - ivec2(1, 0)).r;
    float hz = wakeAt(c + ivec2(0, 1)).r - wakeAt(c - ivec2(0, 1)).r;
    return vec3(vec2(hx, hz) * (WAKE_RES * 0.5), wakeAt(c).b);
}
#endif

#ifdef FSH
float foamHash(vec2 p) {
    p = fract(p * vec2(0.1031, 0.1030));
    p += dot(p, p.yx + 33.33);
    return fract((p.x + p.y) * p.x);
}

/**
 * L'écume des rives : une frange le long des blocs pleins qui bordent l'eau, qui avance et recule comme un ressac.
 * pos : repère joueur, à la surface de l'eau.
 */
float shoreFoam(vec3 pos) {
#if WATER_FOAM > 0 && defined VOXELS
    vec3 world = pos + cameraPosition;
    ivec3 c = voxelCell(pos - vec3(0.0, 0.05, 0.0), fract(cameraPosition));
    if (!voxelInside(c)) return 0.0;
    vec2 f = fract(world.xz);
    float d = 2.0;
    for (int dz = -1; dz <= 1; dz++) {
        for (int dx = -1; dx <= 1; dx++) {
            if (dx == 0 && dz == 0) continue;
            ivec3 n = c + ivec3(dx, 0, dz);
            if (!voxelInside(n) || (texelFetch(voxelTex, n, 0).r & VOX_SOLID) == 0u) continue;
            vec2 lo = vec2(dx, dz);
            vec2 q = max(max(lo - f, f - (lo + 1.0)), 0.0);
            d = min(d, length(q));
        }
    }
    if (d > 1.0) return 0.0;
    float surf = 0.5 + 0.5 * sin(frameTimeCounter * 0.9 + dot(floor(world.xz), vec2(1.3, 0.7)));
    // Une frange étroite et trouée : de l'écume posée au bord, pas un liseré.
    return 0.75 * (1.0 - smoothstep(0.02 + 0.10 * surf, 0.16 + 0.22 * surf, d));
#else
    return 0.0;
#endif
}

/** L'écume au pixel près : plus il y en a, plus de pixels blancs, tirés au sort et qui changent lentement. */
float foamPixels(vec3 world, float amount) {
    if (amount <= 0.01) return 0.0;
    vec2 q = floor(world.xz * 16.0);
    float n = foamHash(q + floor(frameTimeCounter * 0.7 + foamHash(q) * 3.0) * vec2(17.0, 31.0));
    return step(n, amount * 1.15 - 0.1);
}
#endif
