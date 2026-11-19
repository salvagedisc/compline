// La lueur des aurores sur le paysage : les nuits où l'aurore danse (même nuit, même froid que le ciel de
// lib/nightsky.glsl), le sol à découvert, et surtout la neige, prennent un reflet vert qui ondule lentement, à mesure
// que les rideaux passent au-dessus. Lu par le terrain proche (solid.fsh) et lointain (distant.fsh).

#if AURORA_GLOW > 0 && SKY_AURORA > 0
#ifndef NIGHTSKY_GLSL
uniform int worldDay;
#endif

// Le même tirage que starHash (lib/nightsky.glsl) : la terre s'éclaire les nuits où le ciel s'allume.
float auroraGlowHash(vec3 p) {
    p = fract(p * vec3(0.1031, 0.1030, 0.0973));
    p += dot(p, p.yxz + 33.33);
    return fract((p.x + p.y) * p.z);
}

float auroraGlowNoise(vec2 p) {
    vec2 i = floor(p), f = fract(p);
    f = f * f * (3.0 - 2.0 * f);
    float a = auroraGlowHash(vec3(i, 1.0)), b = auroraGlowHash(vec3(i + vec2(1.0, 0.0), 1.0));
    float c = auroraGlowHash(vec3(i + vec2(0.0, 1.0), 1.0)), d = auroraGlowHash(vec3(i + 1.0, 1.0));
    return mix(mix(a, b, f.x), mix(c, d, f.x), f.y);
}

/** La force de l'aurore cette nuit (0 quand il n'y en a pas). */
float auroraTonight() {
    float night = (1.0 - smoothstep(-0.30, -0.05, gSunDir.y)) * (1.0 - rainStrength);
    float cold = 1.0 - smoothstep(0.05, 0.35, airHeat);
    float tonight = smoothstep(0.62, 0.85, auroraGlowHash(vec3(float(worldDay), 3.0, 11.0)));
    return night * cold * tonight * SKY_AURORA_STRENGTH;
}

/** La lumière verte à ajouter à une surface d'albédo albedo (linéaire), en un point (repère monde) qui voit le ciel. */
vec3 auroraGlow(vec3 albedo, vec3 normal, vec3 world, float sky) {
    float s = auroraTonight();
    if (s <= 0.001 || sky <= 0.0) return vec3(0.0);
    float t = frameTimeCounter;
    // Les rideaux qui passent : de larges bandes étirées, qui glissent sur le paysage.
    vec2 p = world.xz / 40.0;
    float n = auroraGlowNoise(p * vec2(1.0, 0.3) + vec2(t * 0.020, -t * 0.011)) * 0.65
            + auroraGlowNoise(p * 2.3 - vec2(t * 0.034, 0.0)) * 0.35;
    float wave = 0.15 + 1.5 * smoothstep(0.25, 0.85, n);
    // L'aurore respire : elle s'avive puis s'apaise en une minute ou deux.
    float breath = 0.70 + 0.30 * sin(t * 0.21) * sin(t * 0.083 + 1.7);
    // Elle vient d'en haut : un toit l'arrête, un mur la reçoit moins.
    float up = 0.35 + 0.65 * max(normal.y, 0.0);
    // Sous les rideaux les plus vifs, la lumière tire vers le violet de leur haut.
    vec3 hue = mix(vec3(0.10, 1.00, 0.32), vec3(0.50, 0.32, 0.95), 0.30 * smoothstep(0.65, 1.0, n));
    return albedo * hue * s * wave * breath * up * sky * 0.045;
}
#endif
