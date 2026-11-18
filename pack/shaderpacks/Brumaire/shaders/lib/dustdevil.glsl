// Les tourbillons de poussière : dans les déserts et les savanes sèches, par beau temps et vent faible, le sol
// surchauffé lève de temps en temps une colonne de sable qui tourne sur elle-même, haute d'une quinzaine de blocs,
// erre lentement puis se défait. Demande clouds.glsl (cloudNoise) et atmosphere.glsl (airDust) avant lui.

#if DUST_DEVILS > 0
const float DEVIL_CELL = 56.0;

/** Combien le pays en lève (0..1) : sec et chaud, le soleil haut, pas de pluie, un vent qui ne les couche pas. */
float devilAmount() {
    float force = brumaireLink > 0.5 ? brumaireWind.z : 0.3;
    return airDust() * smoothstep(0.5, 0.85, gDay) * (1.0 - rainStrength) * (1.0 - smoothstep(0.55, 0.85, force));
}

/**
 * L'épaisseur des tourbillons le long d'un rayon jusqu'à t1 (blocs), 0..1, et la distance du plus proche dans dist.
 * Une case de 56 blocs sur trois en porte un, qui vit une minute à une minute et demie sur deux à trois, dans les
 * neuf cases autour de la caméra. ground : hauteur du sol (repère monde) sous la caméra, faute de mieux.
 */
float devilsAlong(vec3 dir, float t1, float ground, out float dist) {
    dist = 0.0;
    float t = frameTimeCounter;
    vec2 base = floor(cameraPosition.xz / DEVIL_CELL);
    vec2 d2 = dir.xz;
    float flat_ = dot(d2, d2);
    if (flat_ < 1e-6) return 0.0;
    float best = 0.0;
    vec2 wind = brumaireLink > 0.5 ? brumaireWind.xy : vec2(0.3, 0.1);
    for (int i = 0; i < 9; i++) {
        vec2 cell = base + vec2(float(i % 3) - 1.0, float(i / 3) - 1.0);
        float h = fract(sin(dot(cell, vec2(127.1, 311.7))) * 43758.5453);
        if (h > 0.34) continue;
        float period = 120.0 + 60.0 * fract(h * 17.3);
        float life = 60.0 + 30.0 * fract(h * 29.1);
        float age = mod(t + h * period, period);
        if (age > life) continue;
        float u = age / life;
        float grow = smoothstep(0.0, 0.12, u) * (1.0 - smoothstep(0.8, 1.0, u));
        // Il erre dans sa case, poussé par le vent.
        vec2 centre = (cell + 0.5) * DEVIL_CELL
                    + vec2(sin(t * 0.031 + h * 40.0), cos(t * 0.027 + h * 70.0)) * DEVIL_CELL * 0.3
                    + wind * (u - 0.5) * 12.0;
        vec2 rel = centre - cameraPosition.xz;
        // Le point du rayon le plus proche de l'axe, en projection sur le sol.
        float s = dot(rel, d2) / flat_;
        if (s <= 1.0 || s >= t1) continue;
        float floorY = ground;
#if SHADER_RAIN > 0
        // Le sol sous le tourbillon, quand il est assez près pour être connu.
        float under = shelterAt(rel);
        if (under > -1e5) floorY = under;
#endif
        float y = cameraPosition.y + dir.y * s - floorY;
        float height = 14.0 + 6.0 * fract(h * 7.7);
        if (y < -0.5 || y > height) continue;
        float r = length(d2 * s - rel);
        // Un entonnoir : étroit au pied, qui s'évase et se dilue en haut.
        float radius = (1.2 + 2.2 * y / height) * grow;
        if (r > radius * 1.4) continue;
        float ang = atan(d2.y * s - rel.y, d2.x * s - rel.x);
        float swirl = cloudNoise(vec3(ang * 0.6 + t * 1.8 - y * 0.25, y * 0.35 - t * 0.9, h * 9.0)).r;
        // Plein jusqu'à ses parois, plus dense contre elles, strié par la rotation.
        float ring = exp(-sq((r - radius * 0.6) / (radius * 0.55)));
        float profile = (1.0 - smoothstep(radius * 0.7, radius * 1.2, r)) * (0.55 + 0.45 * ring);
        float streaks = 0.4 + 0.6 * smoothstep(0.25, 0.65, swirl);
        float body = profile * streaks * (1.0 - smoothstep(height * 0.55, height, y))
                   * (0.6 + 0.4 * smoothstep(-0.5, 1.5, y)) * grow;
        float fade = 1.0 - smoothstep(DEVIL_CELL * 1.2, DEVIL_CELL * 1.5, s);
        float v = body * fade * 0.6;
        if (v > best) { best = v; dist = s; }
    }
    return best;
}
#endif
