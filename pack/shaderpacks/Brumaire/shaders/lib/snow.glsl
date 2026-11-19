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

#if SNOW_SLOPES > 0 && defined VOXELS
/**
 * La neige d'une contremarche (0..1 au texel) : sur un coteau en paliers, la marche entre deux paliers enneigés se
 * couvre de neige sur presque toute sa hauteur, son pied découpé au pixel ; le coteau se lit blanc d'un seul tenant
 * au lieu de bandes de terre. Seulement là où le dessus du bloc est à l'air (le bord du palier) et voit le ciel.
 */
float snowRiser(vec3 world, vec3 normal, float sky) {
    if (abs(normal.y) > 0.2 || snowHere <= 0.0) return 0.0;
    float amount = snowHere * smoothstep(0.80, 0.95, sky);
    if (amount <= 0.01) return 0.0;
    vec3 cell = floor(world - normal * 0.01);
    ivec3 below;
    if (!voxelRiser(world, normal, rainHash3(cell + normal * 5.1), below)) return 0.0;
    ivec3 above = voxelCell(cell + vec3(0.5, 1.5, 0.5) - cameraPosition, fract(cameraPosition));
    if (voxelInside(above) && (texelFetch(voxelTex, above, 0).r & VOX_SOLID) != 0u) return 0.0;
    float col = floor(fract(abs(normal.x) > 0.5 ? world.z : world.x) * 16.0);
    float row = floor(fract(world.y) * 16.0);
    float foot = 1.0 + floor(rainHash3(cell + normal * 2.7 + vec3(col, 0.0, 3.0)) * 3.0);
    return row >= foot ? amount : 0.0;
}
#endif

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

/**
 * Hauteur du sable au texel (repère monde, plan horizontal) : des rides serrées en travers du vent, en pente douce
 * côté vent et raide de l'autre, tordues lentement et effacées par endroits, sur de larges ondulations.
 */
float sandHeight(vec2 q, vec2 wind) {
    float warp = snowNoise(q * 0.4 + 61.0) * 2.4 + snowNoise(q * 1.3 + 7.0) * 0.5;
    float x = fract(dot(q, wind) * 2.9 + warp);
    float ripple = x < 0.72 ? x / 0.72 : (1.0 - x) / 0.28;
    float keep = smoothstep(0.25, 0.60, snowNoise(q * 0.5 + 13.0));
    return ripple * 0.25 * (0.35 + 0.65 * keep) + snowNoise(q * 0.18 + 31.0) * 0.5;
}

/** Pente du sable au texel, en normale (repère monde) pour un dessus de bloc ; depth : force (0..1). */
vec3 sandNormal(vec3 world, vec2 wind, float depth) {
    vec2 q = (floor(world.xz * 16.0) + 0.5) / 16.0;
    float h = sandHeight(q, wind);
    float hx = sandHeight(q + vec2(1.0 / 16.0, 0.0), wind);
    float hz = sandHeight(q + vec2(0.0, 1.0 / 16.0), wind);
    vec2 slope = vec2(hx - h, hz - h) * 16.0 * 0.45 * depth;
    return normalize(vec3(-slope.x, 1.0, -slope.y));
}

/** Clarté du sable au texel : les crêtes des rides un peu plus claires, leurs creux un peu plus sombres. */
float sandShade(vec3 world, vec2 wind) {
    vec2 q = (floor(world.xz * 16.0) + 0.5) / 16.0;
    float x = fract(dot(q, wind) * 2.9 + snowNoise(q * 0.4 + 61.0) * 2.4 + snowNoise(q * 1.3 + 7.0) * 0.5);
    float keep = smoothstep(0.25, 0.60, snowNoise(q * 0.5 + 13.0));
    return 1.0 + (smoothstep(0.45, 0.72, x) * (1.0 - smoothstep(0.72, 0.80, x)) * 0.14 - smoothstep(0.80, 1.0, x) * 0.12) * keep;
}

/**
 * La glace au texel (repère monde) : des fissures blanches qui suivent les bords de grandes cellules, quelques fêlures
 * plus fines, et des bulles prises dedans. Rend (fissure, bulle), 0..1.
 */
vec2 iceDetail(vec3 world, vec3 normal) {
    vec3 q = (floor(world * 16.0 + normal * 0.5) + 0.5) / 16.0;
    vec2 p = abs(normal.y) > 0.5 ? q.xz : vec2(abs(normal.x) > 0.5 ? q.z : q.x, q.y);
    // Les cellules : la distance au bord le plus proche (deuxième voisin moins le premier).
    vec2 cell = floor(p / 1.6);
    float d1 = 9.0, d2 = 9.0;
    for (int j = -1; j <= 1; j++) {
        for (int i = -1; i <= 1; i++) {
            vec2 c = cell + vec2(i, j);
            vec2 at = (c + vec2(rainHash(c + 0.3), rainHash(c + 7.7)) * 0.8 + 0.1) * 1.6;
            float d = length(p - at);
            if (d < d1) { d2 = d1; d1 = d; } else if (d < d2) d2 = d;
        }
    }
    float crack = 1.0 - step(0.045, d2 - d1);
    // Une fissure n'est pas continue : elle s'interrompt et reprend.
    crack *= step(0.35, snowNoise(p * 2.0 + 17.0));
    // Les fêlures : de courts traits pris dans un bruit étiré.
    float hair = step(0.93, 1.0 - abs(snowNoise(p * vec2(3.0, 0.8) + 41.0) * 2.0 - 1.0)) * step(0.55, snowNoise(p * 1.1 + 5.0));
    float bubble = step(0.985, rainHash(p * 16.0 + 23.0)) * step(0.4, snowNoise(p * 0.9 + 3.0));
    return vec2(max(crack, hair * 0.6), bubble);
}

