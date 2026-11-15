// La neige posée sur les arbres et les rebords, l'éclat de la neige et du sable au soleil.
// Demande rain.glsl avant lui (le hasard au pixel).

// Le joueur se tient dans un biome où il neige, lissé sur une dizaine de secondes.
uniform float snowHere;

// Seuil de clarté des aiguilles qui retiennent la neige.
#define SNOW_GRAIN 0.45

/**
 * Part enneigée d'une surface (repère monde) dans un biome froid : le dessus des blocs et des feuillages qui voient le
 * ciel, au seizième de bloc, avec des bords déchiquetés ; sur les côtés d'un feuillage, un liseré qui déborde du haut.
 * sky : lumière du ciel (0..1) ; leaf : 1 pour un feuillage ; grain : clarté du texel (0..1), la neige tient sur les
 * aiguilles claires et laisse voir les creux sombres.
 */
float snowCover(vec3 world, vec3 normal, float sky, float leaf, float grain) {
#if SNOW_COVER > 0
    float cold = snowHere * smoothstep(0.80, 0.95, sky);
    if (cold <= 0.0) return 0.0;
    vec3 q = (floor(world * 16.0) + 0.5) / 16.0;
    if (normal.y > 0.5) {
        // Un rebord est couvert en entier ; un feuillage, sur ses aiguilles claires, avec quelques trous en plus.
        if (leaf <= 0.0) return cold;
        return cold * step(SNOW_GRAIN, grain) * step(0.12, rainHash(q.xz * 7.13));
    }
    if (leaf > 0.0 && abs(normal.y) < 0.2) {
        // Le liseré : trois à cinq pixels sous l'arête du haut, selon la colonne, sur les aiguilles claires.
        vec2 column = abs(normal.x) > 0.5 ? q.zy : q.xy;
        float depth = (3.0 + floor(rainHash(vec2(column.x * 16.0, floor(world.y) + 0.5)) * 3.0)) / 16.0;
        float fromTop = 1.0 - fract(world.y);
        return cold * step(fromTop, depth) * step(SNOW_GRAIN - 0.1, grain);
    }
#endif
    return 0.0;
}

/** Les grains qui scintillent au soleil sur la neige et le sable : quelques pixels, qui changent de temps en temps. */
float glitter(vec3 world, float density) {
    vec3 q = floor(world * 16.0);
    float h = rainHash(q.xz + q.y * 13.1 + floor(frameTimeCounter * 1.5 + rainHash(q.zx) * 4.0) * 0.731);
    return step(1.0 - density, h);
}
