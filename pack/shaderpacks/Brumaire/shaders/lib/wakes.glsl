// Les rides de ce qui passe sur l'eau : une carte de 512 × 512 texels au huitième de bloc (64 blocs de côté),
// accrochée au monde et réutilisée en boucle autour de la caméra. Chaque texel garde la hauteur de la surface (r), sa
// vitesse (g) et l'écume (b). prepare2 (program/wakes.csh) y fait courir les ondes d'une image à l'autre et y pousse
// l'eau sous les barques, les nageurs et les bêtes qui pataugent (brumaireWake0..5, donnés par le mod fallow) :
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
