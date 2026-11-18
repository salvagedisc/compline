// Les grottes : la lave en fusion, la roche humide des cavernes. L'air qui tremble au-dessus de la lave et la poussière
// des torches sont dans program/mist.fsh. Demande rain.glsl (rainHash) et snow.glsl (snowNoise) avant lui.

/** Couleur d'un corps chauffé selon sa chaleur (0 croûte, 1 cœur) : du rouge sombre au jaune pâle, en passant par l'orange. */
vec3 moltenRamp(float h) {
    vec3 c = mix(vec3(0.14, 0.016, 0.005), vec3(0.80, 0.11, 0.016), smoothstep(0.0, 0.40, h));
    c = mix(c, vec3(1.00, 0.40, 0.05), smoothstep(0.35, 0.75, h));
    return mix(c, vec3(1.00, 0.66, 0.24), smoothstep(0.72, 1.0, h));
}

/**
 * La lumière propre de la lave et du magma au texel. texel : couleur du texel telle que la texture la donne (sRGB) ;
 * world : position ; top : face du dessus d'une nappe ; t : temps (secondes).
 * La chaleur suit la clarté du texel. Sur une nappe, des plaques de croûte plus froides dérivent lentement et des
 * veines vives s'ouvrent entre elles : la nappe ne répète plus le même carreau d'un bloc à l'autre.
 */
vec3 molten(vec3 texel, vec3 world, bool top, float t) {
    float h = smoothstep(0.22, 0.90, dot(texel, vec3(0.2126, 0.7152, 0.0722)));
    if (top) {
        vec2 q = (floor(world.xz * 16.0) + 0.5) / 16.0;
        float crust = snowNoise(q * 0.45 + vec2(t * 0.040, t * 0.025)) * 0.7
                    + snowNoise(q * 1.5 - vec2(t * 0.030, -t * 0.050)) * 0.3;
        // Des plaques de croûte aux bords nets, cernées d'un liseré plus vif là où la lave remonte.
        float plate = smoothstep(0.57, 0.61, crust);
        float rim = smoothstep(0.50, 0.57, crust) * (1.0 - plate);
        h = mix(h, h * 0.28, plate);
        h = max(h, rim * 0.78);
        float vein = 1.0 - abs(snowNoise(q * 2.2 + vec2(-t * 0.07, t * 0.05)) * 2.0 - 1.0);
        h = max(h, smoothstep(0.86, 0.97, vein) * 0.80 * (1.0 - plate));
    }
    return moltenRamp(h) * (0.35 + 2.2 * h * h);
}

/**
 * Humidité de la roche d'une grotte (0..1) : par plaques, davantage sur les voûtes et en profondeur, en traînées
 * qui suintent le long des parois ; rien là où le ciel arrive. sky : lumière du ciel du jeu (0..1).
 */
float caveDamp(vec3 world, vec3 normal, float sky) {
    float under = 1.0 - smoothstep(0.02, 0.25, sky);
    if (under <= 0.0) return 0.0;
    vec3 q = (floor(world * 16.0 + normal * 0.5) + 0.5) / 16.0;
    bool flat_ = abs(normal.y) > 0.5;
    vec2 p = flat_ ? q.xz : vec2(abs(normal.x) > 0.5 ? q.z : q.x, q.y);
    float n = snowNoise(p * 0.45 + 11.0) * 0.7 + snowNoise(p * 1.6 + 5.0) * 0.3;
    // Sur une paroi, l'eau coule : des traînées étroites et hautes.
    if (!flat_) n = max(n * 0.85, smoothstep(0.55, 0.92, snowNoise(vec2(p.x * 3.0, p.y * 0.22 + 3.0))));
    float deep = smoothstep(40.0, -24.0, world.y);
    float amount = 0.34 + 0.22 * deep + 0.18 * float(normal.y < -0.5);
    return under * smoothstep(1.0 - amount, 1.1 - amount, n);
}
