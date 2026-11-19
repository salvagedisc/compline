// Les nuances du paysage : une prairie n'est jamais d'un seul vert. Par larges plaques (une vingtaine à une
// soixantaine de blocs), l'herbe et les feuilles teintées par le biome tirent ici vers un vert plus sec et jauni, là
// vers un vert plus gras et profond ; le sol nu est un peu plus clair ou plus sombre. Le motif répété des textures se
// voit moins de loin. Lu par le terrain proche (solid.fsh) et lointain (distant.fsh), au même dessin.

#if GROUND_TINT > 0
float groundTintHash(vec2 p) {
    p = fract(p * vec2(0.1031, 0.1030));
    p += dot(p, p.yx + 33.33);
    return fract((p.x + p.y) * p.x);
}

float groundTintNoise(vec2 p) {
    vec2 i = floor(p), f = fract(p);
    f = f * f * (3.0 - 2.0 * f);
    return mix(mix(groundTintHash(i), groundTintHash(i + vec2(1.0, 0.0)), f.x),
               mix(groundTintHash(i + vec2(0.0, 1.0)), groundTintHash(i + vec2(1.0, 1.0)), f.x), f.y);
}

/** La part « verte » d'une couleur : 1 pour une herbe ou un feuillage teinté, 0 pour une pierre, une terre, un bois. */
float groundGreen(vec3 c) {
    float chroma = max(c.r, max(c.g, c.b)) - min(c.r, min(c.g, c.b));
    return smoothstep(0.04, 0.12, chroma) * step(c.r, c.g) * step(c.b, c.g);
}

/** albedo (linéaire) nuancé en un point (repère monde) ; green : groundGreen() de sa teinte. */
vec3 groundTint(vec3 albedo, vec3 world, float green) {
    // Au seizième de bloc : la nuance ne coupe pas un texel en deux.
    vec2 p = (floor(world.xz * 16.0) + 0.5) / 16.0;
    float n = groundTintNoise(p / 61.0) * 0.6 + groundTintNoise(p / 17.0 + vec2(5.3, 1.7)) * 0.4;
    float v = clamp((n - 0.5) * 3.0, -1.0, 1.0);
    vec3 dry = vec3(1.10, 1.03, 0.80);
    vec3 lush = vec3(0.86, 0.95, 0.88);
    vec3 tintG = v > 0.0 ? mix(vec3(1.0), dry, v) : mix(vec3(1.0), lush, -v);
    vec3 tintB = vec3(1.0 + 0.05 * v);
    return albedo * mix(tintB, tintG, green);
}
#endif
