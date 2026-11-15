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
