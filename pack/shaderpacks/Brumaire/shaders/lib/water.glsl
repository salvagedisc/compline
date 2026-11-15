// L'eau : sa couleur, ce qu'elle absorbe, ses vagues et la lumière qu'elle dessine au fond.
// Unités : blocs. Lu par la surface (solid.fsh), le fond (light.glsl), la vue sous l'eau (mist.fsh) et le lointain.
// Demande clouds.glsl avant lui (le bruit des caustiques est celui des nuages).

const float WATER_IOR = 1.333;

/** Teinte de l'eau : celle du biome, ramenée vers un vert-de-gris d'eau douce. */
vec3 waterHue(vec3 biome) {
    vec3 h = biome / max(max(biome.r, max(biome.g, biome.b)), 1e-4);
    vec3 sage = vec3(0.50, 0.78, 0.68);
    return mix(h, sage * (0.4 + 0.6 * luma(h) / luma(sage)), 0.85);
}

/** Lumière perdue par bloc traversé : le rouge part le premier ; une eau verte ou brune est plus trouble. */
vec3 waterExtinction(vec3 hue) {
    return (vec3(0.46, 0.30, 0.32) + (1.0 - hue) * 0.12) * WATER_MURK;
}

/** Ce que renvoie l'eau profonde : la lumière du ciel et de l'astre diffusée dans la masse. */
vec3 waterMurk(vec3 hue, float sky, float lit) {
    return hue * hue * (gAmbient * sky * 0.24 + gLightColor * lit * 0.04 + vec3(0.004, 0.006, 0.007));
}

/** Pente de la surface : quelques trains d'onde qui se croisent, poussés par le vent. */
vec2 waveSlope(vec2 p, float t) {
#if WATER_PIXELS > 0
    // Les vagues se lisent au seizième de bloc, comme les pixels des textures.
    p = (floor(p * 16.0) + 0.5) / 16.0;
#endif
    vec2 d = vec2(0.8, 0.6);
    if (brumaireLink > 0.5 && dot(brumaireWind.xy, brumaireWind.xy) > 0.01) d = normalize(brumaireWind.xy);
    vec2 slope = vec2(0.0);
    float f = 0.55, a = 0.095, s = 1.1;
    for (int i = 0; i < 5; i++) {
        float phase = dot(p, d) * f + t * s + float(i) * 1.7;
        phase += sin(dot(p, vec2(-d.y, d.x)) * f * 0.6 + float(i)) * 0.8;
        slope += d * (f * a * cos(phase));
        d = mat2(0.62, 0.78, -0.78, 0.62) * d;
        f *= 1.7; a *= 0.62; s *= 1.25;
    }
    slope *= WATER_WAVES;
#if WATER_PIXELS > 0
    // Et par paliers : des plages de reflet aux bords francs, comme une texture dessinée.
    slope = floor(slope / 0.03 + 0.5) * 0.03;
#endif
    return slope;
}

/** Caustiques : la lumière que les vagues concentrent en filets sur le fond, au point p (repère monde). */
float caustics(vec3 p) {
#if WATER_CAUSTICS > 0
    vec2 q = p.xz;
#if WATER_PIXELS > 0
    q = (floor(q * 16.0) + 0.5) / 16.0;
#endif
    float t = frameTimeCounter * 0.035;
    // Deux nappes de cellules qui glissent l'une sur l'autre : là où elles se croisent, un filet clair.
    float a = cloudNoise(vec3(q * 0.085, t)).g;
    float b = cloudNoise(vec3(q.yx * 0.11 + 0.37, 0.5 - t * 0.8)).g;
    float net = clamp(1.0 - abs(a - b) * 5.0, 0.0, 1.0);
    return mix(1.0, 0.45 + 2.6 * net * net * net, 0.85 * float(WATER_CAUSTICS) / 2.0);
#else
    return 1.0;
#endif
}
