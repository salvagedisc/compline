// La matière de la neige posée : couverture par plaques, grain, rides du vent, pente au texel et éclats qui suivent
// le regard. Tout se calcule au seizième de bloc pour garder le dessin en pixels du jeu.
// Demande rain.glsl (rainHash) et cover.glsl avant lui.

// Bruit de valeur lissé, au pas donné (repère monde, plan horizontal).
float snowNoise(vec2 p) {
    vec2 i = floor(p);
    vec2 f = fract(p);
    f = f * f * (3.0 - 2.0 * f);
    float a = rainHash(i), b = rainHash(i + vec2(1.0, 0.0));
    float c = rainHash(i + vec2(0.0, 1.0)), d = rainHash(i + vec2(1.0, 1.0));
    return mix(mix(a, b, f.x), mix(c, d, f.x), f.y);
}

/**
 * Hauteur de la neige au texel (0..1, repère monde, plan horizontal) : des congères larges sur quelques blocs, des
 * rides que le vent dessine en travers de sa direction, et un grain au texel. wind : direction du vent (xz).
 */
float snowHeight(vec2 q, vec2 wind) {
    vec2 across = vec2(-wind.y, wind.x);
    float drift = snowNoise(q * 0.35) * 0.55 + snowNoise(q * 0.9 + 17.0) * 0.25;
    // Les rides : des crêtes serrées perpendiculaires au vent, tordues par un bruit lent pour ne pas faire de lignes.
    float warp = snowNoise(q * 0.5 + 41.0) * 3.0;
    float ripple = sin((dot(q, wind) * 5.5 + warp) * 3.14159) * 0.5 + 0.5;
    ripple *= smoothstep(0.25, 0.75, snowNoise(q * 0.6 + dot(q, across) * 0.05 + 7.0));
    float grain = rainHash(floor(q * 16.0) + 0.5);
    return drift + ripple * 0.12 + grain * 0.06;
}

/**
 * Part de la surface couverte (0..1) au texel, quand la couverture n'est que partielle (sous les arbres, au bord des
 * lumières) : au lieu d'un voile uniforme, des plaques qui se rejoignent quand la couverture monte.
 */
float snowPatch(vec3 world, float cover) {
#if SNOW_DETAIL > 0
    if (cover >= 0.999) return 1.0;
    if (cover <= 0.001) return 0.0;
    vec2 q = (floor(world.xz * 16.0) + 0.5) / 16.0;
    float n = snowNoise(q * 1.3) * 0.7 + snowNoise(q * 4.0 + 3.0) * 0.2 + rainHash(q * 16.0 + 9.0) * 0.1;
    return step(1.0 - cover, n);
#else
    return cover;
#endif
}

/**
 * La neige plaquée par le vent sur les faces qui lui font front (0..1 au texel) : les troncs, les murs et les rochers
 * exposés se couvrent par plaques pendant une chute, d'abord en haut et sur les reliefs clairs, et le gardent tant
 * qu'il fait froid. sky : lumière du ciel (0..1) ; wind : direction du vent (xz) ; grain : clarté du texel (0..1).
 */
float snowPlaster(vec3 world, vec3 normal, float sky, vec2 wind, float grain) {
#if SNOW_BUILDUP > 0
    if (abs(normal.y) > 0.2) return 0.0;
    float front = smoothstep(0.15, 0.75, dot(normal.xz, -wind));
    float amount = snowHere * snowStorm * front * smoothstep(0.75, 0.92, sky);
    if (amount <= 0.01) return 0.0;
    vec3 q = (floor(world * 16.0 + normal * 0.5) + 0.5) / 16.0;
    vec2 p = vec2(abs(normal.x) > 0.5 ? q.z : q.x, q.y);
    float n = snowNoise(p * 1.2 + 23.0) * 0.6 + snowNoise(p * 4.0 + 9.0) * 0.25 + rainHash(p * 16.0 + 3.0) * 0.15;
    // Plus haut dans le bloc, plus de neige ; les texels clairs (reliefs) la retiennent d'abord.
    n += fract(world.y) * 0.18 + (grain - 0.5) * 0.25;
    return step(1.0 - amount * 0.45, n);
#else
    return 0.0;
#endif
}

/**
 * Pente de la neige au texel, sous forme de normale (repère monde) pour un dessus de bloc. depth : force des reliefs
 * (0..1). Les dérivées sont prises d'un texel à l'autre, pour un relief en marches de pixels.
 */
vec3 snowNormal(vec3 world, vec2 wind, float depth) {
    vec2 q = (floor(world.xz * 16.0) + 0.5) / 16.0;
    float h = snowHeight(q, wind);
    float hx = snowHeight(q + vec2(1.0 / 16.0, 0.0), wind);
    float hz = snowHeight(q + vec2(0.0, 1.0 / 16.0), wind);
    vec2 slope = vec2(hx - h, hz - h) * 16.0 * 0.16 * depth;
    return normalize(vec3(-slope.x, 1.0, -slope.y));
}

/** Teinte de la neige au texel : un blanc légèrement bleuté, plus clair sur les crêtes, plus gris dans les creux. */
vec3 snowAlbedo(vec3 world, vec2 wind) {
    vec2 q = (floor(world.xz * 16.0) + 0.5) / 16.0;
    float h = snowHeight(q, wind);
    float grain = rainHash(q * 16.0 + 5.0);
    return vec3(0.56, 0.59, 0.64) * (0.86 + 0.16 * h + 0.07 * grain);
}

/**
 * Éclats de la neige : chaque texel tiré au sort porte une facette de glace orientée au hasard, qui renvoie le soleil
 * seulement quand l'œil se trouve dans son reflet ; les éclats bougent donc quand on se déplace, comme sur une vraie
 * neige. view : direction de l'œil vers le point ; light : direction de l'astre.
 */
float snowGlints(vec3 world, vec3 normal, vec3 view, vec3 light, float density) {
    vec3 q = floor(world * 16.0 + normal * 0.5);
    float pick = rainHash(q.xz + q.y * 13.1);
    if (pick < 1.0 - density) return 0.0;
    vec3 facet = normalize(normal * 2.2 + vec3(rainHash(q.zx + 1.7), rainHash(q.xz + 4.3), rainHash(q.zx * 1.3 + 8.1)) * 2.0 - 1.0);
    float mirror = max(dot(reflect(-light, facet), -view), 0.0);
    return pow(mirror, 180.0) * 6.0;
}
