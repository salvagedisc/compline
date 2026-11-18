// Les ombres lointaines : au-delà de la carte d'ombre, le soleil est arrêté par le relief. Le terrain que la caméra
// voit (blocs du jeu et Distant Horizons) note dans une carte, colonne par colonne de deux blocs, la plus haute
// surface vue ; la carte couvre 4 096 blocs de côté autour du joueur et garde ce qu'elle a vu quand on se retourne.
// Un point lointain remonte vers le soleil au-dessus de cette carte : si le relief passe devant, il est à l'ombre,
// avec une pénombre qui s'élargit avec la distance de ce qui le cache.
#if FAR_SHADOWS > 0
#ifndef HAS_FRAME_COUNTER
uniform int frameCounter;
#define HAS_FRAME_COUNTER
#endif
const int FAR_SIZE = 2048;
const float FAR_CELL = 2.0;

/** La colonne de la carte qui contient ce point (repère monde). */
ivec2 farCell(vec2 xz) { return ivec2(floor(xz / FAR_CELL)); }

/** Chaque case de la carte sert à une colonne sur 2 048 : le reste de la position la distingue. */
uint farTag(ivec2 c) { return uint((c.x >> 11) & 1023) | (uint((c.y >> 11) & 1023) << 10); }

/** Seules les colonnes à moins de 2 048 blocs du joueur ont leur case. */
bool farInside(ivec2 c) {
    ivec2 d = abs(c - farCell(cameraPosition.xz));
    return d.x < FAR_SIZE / 2 - 1 && d.y < FAR_SIZE / 2 - 1;
}

#ifdef FAR_RECORD
layout(r32ui) uniform uimage2D farHeightImg;
#define farLoad(t) imageLoad(farHeightImg, t).r
#else
uniform usampler2D farHeightTex;
#define farLoad(t) texelFetch(farHeightTex, t, 0).r
#endif

/** La plus haute surface vue dans cette colonne, ou très bas si elle n'a jamais été vue. */
float farHeight(ivec2 c) {
    uint v = farLoad(c & (FAR_SIZE - 1));
    if (v == 0u || (v >> 12) != farTag(c)) return -1000.0;
    return float(v & 4095u) * 0.25 - 128.0;
}

#ifdef FAR_RECORD
/**
 * Note un sommet du terrain dessiné (repère monde). L'écriture se fait dans le shader de sommets : dans celui des
 * pixels, elle priverait le terrain du test de profondeur anticipé. Une case qui servait à une colonne plus loin est
 * reprise par la colonne proche.
 */
void farRecord(vec3 world) {
    ivec2 c = farCell(world.xz);
    if (!farInside(c)) return;
    uint v = (farTag(c) << 12) | uint(clamp((world.y + 128.0) * 4.0, 1.0, 4095.0));
    ivec2 t = c & (FAR_SIZE - 1);
    uint old = imageAtomicMax(farHeightImg, t, v);
    if (old > v && (old >> 12) != (v >> 12)) imageAtomicCompSwap(farHeightImg, t, old, v);
}
#endif

/** Part du soleil qui passe le relief, pour un point lointain (repère joueur) et sa normale. */
float farShadow(vec3 pos, vec3 normal, float jitter) {
    vec3 dir = gLightDir;
    if (dir.y < 0.01) return 1.0;
    vec3 from = pos + cameraPosition + normal * 0.6;
    float light = 1.0;
    // Les pas s'allongent avec la distance : serrés près du point, ils vont chercher les montagnes jusqu'à 512 blocs.
    for (int i = 0; i < FAR_SHADOWS; i++) {
        float k = (float(i) + jitter) / float(FAR_SHADOWS);
        float t = 3.0 + 509.0 * k * k;
        vec3 q = from + dir * t;
        if (q.y > 330.0) break;
        ivec2 c = farCell(q.xz);
        if (!farInside(c)) break;
        float above = q.y - farHeight(c);
        light = min(light, clamp(above / (0.5 + 0.04 * t), 0.0, 1.0));
        if (light < 0.01) break;
    }
    return light * light * (3.0 - 2.0 * light);
}
#endif
