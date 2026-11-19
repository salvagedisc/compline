// Les oiseaux : le jour, de loin en loin, un vol d'oiseaux traverse le ciel en V, à quelques dizaines de blocs
// au-dessus du joueur, battant des ailes puis planant. Des silhouettes sombres dessinées en gros pixels, cachées par
// ce qui est devant. Demande motes.glsl (moteHash).

#if BIRDS > 0
const float BIRD_PERIOD = 75.0;

/** Combien d'oiseaux (0..1) : le jour, à découvert, ni orage ni neige qui tombe. */
float birdAmount() {
    float day = smoothstep(0.1, 0.35, gDay);
    float open = smoothstep(0.3, 0.7, float(eyeBrightnessSmooth.y) / 240.0);
    return day * open * (1.0 - smoothstep(0.2, 0.6, rainStrength));
}

/**
 * Le vol d'oiseaux le long d'un rayon jusqu'à t1 (blocs) : rend la couverture de la silhouette la plus proche (0..1)
 * et sa distance. Un vol par période de 75 secondes, une fois sur deux à peu près : de 5 à 9 oiseaux en V qui passent
 * d'un bord à l'autre du ciel, à 35 à 70 blocs au-dessus de la caméra et à 20 à 110 blocs d'elle au plus près.
 */
float birdsAlong(vec3 dir, float t1, float amount, out float dist) {
    dist = 0.0;
    if (amount <= 0.01 || dir.y < 0.02) return 0.0;
    float time = frameTimeCounter;
    float era = floor(time / BIRD_PERIOD);
    float phase = fract(time / BIRD_PERIOD);
    vec3 seed = vec3(era, 0.0, 17.0);
    if (moteHash(seed, 81.0) > 0.55 * amount + 0.1) return 0.0;
    float heading = moteHash(seed, 82.0) * 6.2831853;
    vec2 along = vec2(cos(heading), sin(heading));
    vec2 side = vec2(-along.y, along.x);
    float pass = mix(20.0, 110.0, moteHash(seed, 83.0)) * (moteHash(seed, 84.0) < 0.5 ? -1.0 : 1.0);
    float height = mix(35.0, 70.0, moteHash(seed, 85.0));
    // Le vol est ancré au monde à son départ : le joueur qui marche le voit filer, pas le suivre.
    vec2 origin = floor(cameraPosition.xz / 64.0) * 64.0 + 32.0;
    vec2 lead = origin + side * pass + along * mix(-260.0, 260.0, phase);
    float leadY = floor(cameraPosition.y / 16.0) * 16.0 + height;
    int count = 5 + int(moteHash(seed, 86.0) * 4.99);
    vec3 right = normalize(cross(dir, vec3(0.0, 1.0, 0.0)) + vec3(1e-4, 0.0, 0.0));
    vec3 up = cross(right, dir);
    float best = 0.0;
    for (int k = 0; k < 9; k++) {
        if (k >= count) break;
        // Le V : chaque oiseau un rang derrière, alternativement à gauche et à droite, avec un peu de désordre.
        float rank = float((k + 1) / 2);
        float flank = k == 0 ? 0.0 : (k % 2 == 0 ? 1.0 : -1.0);
        float bk = float(k);
        vec2 off = -along * rank * 2.6 + side * flank * rank * 2.2
            + vec2(sin(time * 0.7 + bk * 1.9), cos(time * 0.6 + bk * 2.7)) * 0.4;
        vec3 at = vec3(lead.x + off.x, leadY + sin(time * 0.5 + bk) * 0.6, lead.y + off.y);
        vec3 rel = at - cameraPosition;
        float s = dot(rel, dir);
        if (s < 5.0 || s > t1) continue;
        vec3 miss = dir * s - rel;
        // Des pixels d'un cinquième de bloc : l'envergure d'un oiseau fait près de deux blocs (une oie, un héron).
        vec2 q = floor(vec2(dot(miss, right), dot(miss, up)) * 5.0) + 0.5;
        float ax = abs(q.x);
        if (ax > 4.6 || abs(q.y) > 3.0) continue;
        // Il bat des ailes quelques secondes, puis plane.
        float flapping = step(0.45, fract(time * 0.13 + bk * 0.37));
        float beat = mix(0.25, sin(time * 11.0 + bk * 2.3), flapping);
        float wingY = floor(ax * beat * 0.55 + 0.5);
        bool shape = (ax < 0.6 && q.y > -1.0 && q.y < 1.0) || (ax < 4.6 && abs(q.y - wingY) < 0.6);
        if (shape) {
            float cover = 0.9 * (1.0 - smoothstep(180.0, 260.0, s)) * smoothstep(0.0, 0.08, phase) * (1.0 - smoothstep(0.92, 1.0, phase));
            if (cover > best) { best = cover; dist = s; }
        }
    }
    return best;
}
#endif
