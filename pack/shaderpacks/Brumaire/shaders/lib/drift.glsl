// La neige et le sable soufflés au ras du sol : par grand vent, sur un pays de neige ou dans le désert, des traînées
// pâles filent sur le sol dans le sens du vent, épaisses d'un demi-bloc, et se défont. Le sol de chaque colonne est
// l'abri de la pluie (lib/precip.glsl). Demande precip.glsl et clouds.glsl (cloudNoise) avant lui.

#if GROUND_DRIFT > 0 && SHADER_RAIN > 0
/** Combien le vent soulève ici (0..1) et de quoi : x neige, y sable. */
vec2 driftAmount() {
    float force = brumaireLink > 0.5 ? brumaireWind.z : 0.0;
    float gale = smoothstep(0.45, 0.9, force);
    return vec2(snowHere, airDust()) * gale * (1.0 - 0.6 * rainStrength * (1.0 - snowHere));
}

/**
 * L'épaisseur des traînées le long d'un rayon jusqu'à t1 (blocs), 0..1 : huit prises sur le bout du rayon qui rase le
 * sol (la couche fait trois quarts de bloc), jusqu'à 48 blocs, chacune pesée par sa hauteur au-dessus du sol et par un
 * bruit étiré dans le sens du vent, qui file à quelques blocs par seconde.
 */
float driftAlong(vec3 dir, float t1, float amount) {
    if (amount <= 0.01) return 0.0;
    vec2 wind = normalize(brumaireWind.xy + vec2(1e-4, 0.0));
    vec2 across = vec2(-wind.y, wind.x);
    if (dir.y > -0.005) return 0.0;
    float reach = min(t1, 48.0);
    // La couche ne se traverse qu'au bout du rayon, juste avant le sol : on n'échantillonne que là.
    float span = min(0.9 / -dir.y, min(reach, 20.0));
    float start = reach - span;
    float step_ = span / 8.0;
    float jitter = fract(52.9829189 * fract(dot(gl_FragCoord.xy, vec2(0.06711056, 0.00583715))));
    float t = frameTimeCounter;
    float opacity = 0.0;
    for (int i = 0; i < 8; i++) {
        float s = start + (float(i) + jitter) * step_;
        vec3 rel = dir * s;
        float above = rel.y + cameraPosition.y - shelterAt(rel.xz);
        if (above < 0.0 || above > 0.75) continue;
        vec2 w = rel.xz + cameraPosition.xz;
        // Les traînées : longues dans le sens du vent, serrées en travers, emportées par lui.
        vec2 q = vec2(dot(w, wind) * 0.12 - t * 0.9, dot(w, across) * 0.9);
        float n = cloudNoise(vec3(q, above * 0.6 + 0.17)).r * 0.7 + cloudNoise(vec3(q * 2.3 + 5.0, 0.53)).r * 0.3;
        float streak = smoothstep(0.42, 0.72, n) * exp(-above * 4.5) * (1.0 - smoothstep(32.0, 48.0, s));
        opacity += streak * step_ * 0.45 * (1.0 - opacity);
    }
    return clamp(opacity * amount, 0.0, 0.85);
}
#endif
