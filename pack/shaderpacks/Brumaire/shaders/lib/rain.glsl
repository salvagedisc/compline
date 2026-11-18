// La pluie : le sol qui se mouille et sèche lentement, les flaques et leurs ronds, les rideaux de pluie au loin.
// Demande clouds.glsl avant lui (le bruit des flaques est celui des nuages).

uniform float wetness;
// La pluie tombe bien là où se tient le joueur (ni neige, ni désert), lissé sur quelques secondes.
uniform float rainHere;

/** Part mouillée d'une surface : il a plu ici, et elle voit le ciel (lm.y : lumière du ciel, 0..1). */
float wetAt(float sky) {
    return clamp(wetness * rainHere * WETNESS, 0.0, 1.0) * smoothstep(0.82, 0.96, sky);
}

/** Flaques sur un sol plat (repère monde) : des taches aux bords francs, au seizième de bloc, qui s'étendent. */
float puddleAt(vec3 world, vec3 normal, float wet) {
#if PUDDLES > 0
    if (normal.y < 0.9 || wet <= 0.0) return 0.0;
    vec2 q = (floor(world.xz * 16.0) + 0.5) / 16.0;
    float n = cloudNoise(vec3(q * 0.035, 0.61)).r * 0.7 + cloudNoise(vec3(q * 0.11, 0.23)).r * 0.3;
    float edge = mix(0.82, 0.63, wet * PUDDLE_AMOUNT);
    return smoothstep(edge, edge + 0.025, n) * smoothstep(0.35, 0.8, wet);
#else
    return 0.0;
#endif
}

float rainHash(vec2 p) { return fract(sin(dot(p, vec2(127.1, 311.7))) * 43758.5453); }

/** Pente des ronds que font les gouttes sur une flaque, au seizième de bloc comme les pixels des textures. */
vec2 rippleSlope(vec2 p, float t) {
    p = (floor(p * 16.0) + 0.5) / 16.0;
    vec2 slope = vec2(0.0);
    for (int k = 0; k < 3; k++) {
        vec2 o = vec2(0.37, 0.71) * float(k);
        vec2 cell = floor(p * 2.0 + o);
        vec2 centre = (cell + 0.3 + 0.4 * vec2(rainHash(cell + 3.1 * float(k)), rainHash(cell.yx + 7.7 * float(k))) - o) / 2.0;
        float phase = fract(t * 0.9 + rainHash(cell + 11.0 * float(k)));
        vec2 d = p - centre;
        float r = length(d);
        float ring = exp(-sq((r - phase * 0.17) / 0.028)) * (1.0 - phase);
        slope += d / max(r, 1e-3) * ring;
    }
    return slope * 0.45;
}

float rainHash3(vec3 p) { return fract(sin(dot(p, vec3(127.1, 311.7, 74.7))) * 43758.5453); }

/**
 * Le grain d'une surface mouillée, texel par texel. relief : de -1 (texel plus sombre que la moyenne du bloc, un creux)
 * à 1 (plus clair, une bosse). Les creux boivent l'eau et foncent le plus ; les bosses foncent peu mais luisent.
 * Quand la pluie cesse, la surface sèche par plaques au seizième de bloc : les bosses et les endroits exposés
 * d'abord, les creux et le pied des murs en dernier. Rend x : part trempée (qui fonce), y : éclat (0..1).
 */
vec2 wetSurface(vec3 world, vec3 normal, float wet, float relief) {
    if (wet <= 0.0) return vec2(0.0);
    float local = wet;
#if PATCHY_DRYING > 0
    // Le seuil de séchage de ce texel : une tache large du bruit des nuages, un grain par texel, et son relief.
    vec3 q = (floor(world * 16.0 - normal * 0.5) + 0.5) / 16.0;
    float patch_ = cloudNoise(q * 0.23 + 0.17).g * 0.6 + rainHash3(floor(q * 16.0)) * 0.25;
    // Sous un mur, l'eau qui coule le long garde le pied mouillé plus longtemps.
    float foot = normal.y > 0.5 ? 0.0 : 0.15 * (1.0 - fract(world.y));
    float hold = clamp(patch_ + 0.25 * relief - foot, 0.0, 1.0);
    // Tant qu'il pleut fort, tout est trempé ; ensuite chaque texel sèche quand l'eau tombe sous son seuil.
    float dry = smoothstep(hold * 0.85, hold * 0.85 + 0.12, wet);
    local = mix(dry * wet, wet, smoothstep(0.35, 0.75, rainStrength));
#endif
    float soak = local * clamp(0.80 - 0.35 * relief, 0.35, 1.0);
    float shine = local * clamp(0.32 + 0.30 * max(relief, 0.0) + 0.12 * normal.y, 0.0, 0.7);
    return vec2(soak, shine);
}

/**
 * Les filets d'eau qui coulent le long d'un mur sous la pluie, au seizième de bloc : certaines colonnes de pixels
 * restent plus sombres (l'eau y ruisselle) et une goutte claire y descend par moments, suivie d'une traînée.
 * Rend x : assombrissement du filet, y : éclat de la goutte.
 */
vec2 rivuletAt(vec3 world, vec3 normal, float t, float wet) {
#if WALL_RIVULETS > 0
    if (abs(normal.y) > 0.3 || wet <= 0.05) return vec2(0.0);
    vec3 tang = normalize(cross(normal, vec3(0.0, 1.0, 0.0)));
    float face = dot(floor(world - normal * 0.5), normal);
    float u = floor(dot(world, tang) * 16.0);
    float h = rainHash(vec2(u, face * 7.31));
    if (h > 0.16) return vec2(0.0);
    // Un filet suit sa colonne sur deux blocs, puis saute d'un pixel de côté : l'eau trouve un autre chemin.
    float v = world.y * 16.0;
    float run = floor(v / 32.0);
    float jog = rainHash(vec2(u + run * 3.0, face));
    if (jog > 0.75) return vec2(0.0);
    float streak = 0.45 + 0.35 * rainHash(vec2(u, run + face));
    // La goutte : sa tête descend la colonne, d'un pixel à la fois ; derrière elle, l'eau luit encore quelques pixels.
    float speed = 9.0 + 9.0 * h / 0.16;
    float head = fract((floor(v) + floor(t * speed)) / 40.0 + rainHash(vec2(u * 1.7, face)));
    float tail = head < 0.025 ? 1.0 : (head < 0.15 ? 1.0 - (head - 0.025) / 0.125 : 0.0);
    return vec2(streak * wet, tail * wet * rainStrength);
#else
    return vec2(0.0);
#endif
}

/**
 * La pluie sur une vitre, au seizième de bloc : des perles d'un pixel posées çà et là, qui sèchent et reviennent, et
 * sur les faces debout des gouttes qui dévalent une colonne de pixels en laissant une traînée. wet : pluie reçue (0..1).
 * Rend x : part du texel couverte d'eau, y : éclat (la tête de la goutte, les perles qui prennent la lumière).
 */
vec2 windowRain(vec3 world, vec3 normal, float t, float wet) {
#if WINDOW_RAIN > 0
    if (wet <= 0.02) return vec2(0.0);
    bool upright = abs(normal.y) < 0.5;
    vec3 tang = upright ? normalize(cross(normal, vec3(0.0, 1.0, 0.0))) : vec3(1.0, 0.0, 0.0);
    float face = dot(floor(world - normal * 0.5), normal);
    vec2 px = floor(vec2(dot(world, tang), upright ? world.y : world.z) * 16.0);
    // Les perles : une case de trois pixels sur trois en porte une de temps en temps, à une place tirée pour elle.
    vec2 cell = floor(px / 3.0);
    float k = t * 0.18 + rainHash(cell + face * 3.7);
    vec2 seed = cell + floor(k) * 7.31 + face;
    float bead = 0.0;
    if (rainHash(seed) < 0.22 * wet && all(equal(px, cell * 3.0 + floor(vec2(rainHash(seed + 1.3), rainHash(seed + 2.9)) * 3.0)))) {
        bead = 1.0 - smoothstep(0.7, 1.0, fract(k));
    }
    float streak = 0.0, head = 0.0;
    if (upright) {
        // Les coulures : une colonne sur dix, une goutte qui descend vite et laisse quelques pixels mouillés derrière elle.
        float h = rainHash(vec2(px.x, face * 5.13));
        if (h < 0.10 * wet) {
            float pos = fract((px.y + floor(t * (14.0 + 30.0 * h))) / 48.0 + rainHash(vec2(px.x * 1.9, face)));
            head = step(pos, 1.0 / 48.0);
            streak = pos < 0.18 ? 1.0 - pos / 0.18 : 0.0;
        }
    }
    return vec2(max(bead, streak * 0.7), max(bead * 0.6, head)) * min(wet * 1.5, 1.0);
#else
    return vec2(0.0);
#endif
}

/**
 * Les gouttes qui éclatent sur un sol mouillé : au seizième de bloc, un pixel clair puis une petite couronne de quatre
 * pixels qui s'efface, en quelques centièmes de seconde, ici et là. Rend leur éclat (0..1).
 */
float splashAt(vec3 world, vec3 normal, float t, float amount) {
#if RAIN_SPLASHES > 0
    if (normal.y < 0.9 || amount <= 0.01) return 0.0;
    vec2 px = floor(world.xz * 16.0);
    // Des cases de quatre pixels : chacune reçoit une goutte de temps à autre, à une place tirée pour cette chute.
    vec2 cell = floor(px / 4.0);
    float rate = 1.6 + 1.4 * amount;
    float k = t * rate + rainHash(cell * 1.3);
    float n = floor(k);
    float age = fract(k) * 6.0;
    if (age > 1.0) return 0.0;
    if (rainHash(cell + n * 0.71) > amount * 0.55) return 0.0;
    vec2 at = cell * 4.0 + floor(vec2(rainHash(cell + n), rainHash(cell.yx + n * 1.9)) * 4.0);
    vec2 d = abs(px - at);
    float centre = float(d.x + d.y < 0.5) * (1.0 - smoothstep(0.0, 0.4, age));
    float crown = float(abs(d.x + d.y - 1.0) < 0.5) * smoothstep(0.1, 0.35, age) * (1.0 - smoothstep(0.5, 1.0, age));
    return max(centre * 0.8, crown * 0.45);
#else
    return 0.0;
#endif
}

/**
 * Les rideaux de pluie au loin, au-delà des gouttes que dessine le jeu : de fins traits clairs qui tombent, accrochés
 * à la direction du regard dans le monde. dir : direction (repère monde) ; len : distance de ce qu'on voit.
 */
float rainCurtain(vec3 dir, float len, float t) {
#if RAIN_CURTAINS > 0
    float far_ = smoothstep(8.0, 40.0, len);
    if (far_ <= 0.0 || abs(dir.y) > 0.75) return 0.0;
    float az = atan(dir.z, dir.x);
    // Le vent penche les traits : sa part qui traverse le regard.
    vec2 across = normalize(vec2(-dir.z, dir.x));
    vec2 wind = brumaireLink > 0.5 ? brumaireWind.xy * brumaireWind.z : vec2(0.25);
    float lean = dot(wind, across) * 0.35;
    float sum = 0.0;
    for (int k = 0; k < 2; k++) {
        float cols = k == 0 ? 520.0 : 340.0;
        float u = (az + dir.y * lean) * cols;
        float col = floor(u);
        float h = rainHash(vec2(col, float(k) * 17.0));
        float v = dir.y * (k == 0 ? 9.0 : 6.0) + t * (k == 0 ? 2.6 : 1.9) + h * 7.0;
        float seg = floor(v);
        float on = step(0.55, rainHash(vec2(col, seg + float(k) * 31.0)));
        float streak = on * smoothstep(0.0, 0.05, fract(v)) * (1.0 - smoothstep(0.12, 0.2, fract(v)));
        // Un trait d'un pixel : bord net au centre de la colonne.
        streak *= 1.0 - smoothstep(0.25, 0.45, abs(fract(u) - 0.5));
        sum += streak * (k == 0 ? 1.0 : 0.6);
    }
    return sum * far_ * (1.0 - smoothstep(0.45, 0.75, abs(dir.y))) * RAIN_CURTAINS * 0.5;
#else
    return 0.0;
#endif
}
