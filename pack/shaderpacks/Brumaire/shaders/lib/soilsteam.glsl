// Le sol qui fume après l'averse : quand le soleil revient sur une terre trempée et tiède, une vapeur pâle monte
// lentement du sol, à moins d'un bloc, puis se dissipe à mesure que tout sèche. Le sol est l'abri de la pluie
// (lib/precip.glsl). Demande precip.glsl, rain.glsl (wetness) et clouds.glsl (cloudNoise) avant lui.

#if SOIL_STEAM > 0 && SHADER_RAIN > 0
/** Combien le sol fume (0..1) : mouillé, la pluie finie, le soleil haut, un air tiède. */
float soilSteamAmount() {
    float wet = smoothstep(0.3, 0.8, wetness) * (1.0 - smoothstep(0.05, 0.2, rainStrength));
    float sun = smoothstep(0.4, 0.8, gDay) * (1.0 - thunderStrength);
    float mild = smoothstep(0.45, 0.7, airHeat) * (1.0 - smoothstep(1.4, 1.8, airHeat));
    return wet * sun * mild * rainHere;
}

/**
 * L'épaisseur de la vapeur le long d'un rayon jusqu'à t1 (blocs), 0..1 : huit prises sur le bout du rayon qui rase le
 * sol, jusqu'à 40 blocs, chacune pesée par sa hauteur au-dessus du sol et par un bruit qui monte lentement.
 */
float soilSteamAlong(vec3 dir, float t1, float amount) {
    if (amount <= 0.01 || dir.y > -0.005) return 0.0;
    float reach = min(t1, 40.0);
    float span = min(1.3 / -dir.y, min(reach, 16.0));
    float start = reach - span;
    float step_ = span / 8.0;
    float jitter = fract(52.9829189 * fract(dot(gl_FragCoord.xy, vec2(0.06711056, 0.00583715))));
    float t = frameTimeCounter;
    float opacity = 0.0;
    for (int i = 0; i < 8; i++) {
        float s = start + (float(i) + jitter) * step_;
        vec3 rel = dir * s;
        float above = rel.y + cameraPosition.y - shelterAt(rel.xz);
        if (above < 0.0 || above > 1.1) continue;
        vec3 w = rel + cameraPosition;
        float n = cloudNoise(vec3(w.xz * 0.35 + t * 0.02, w.y * 0.8 - t * 0.18)).r * 0.65
                + cloudNoise(vec3(w.xz * 0.9 - t * 0.03, w.y * 1.6 - t * 0.35) + 9.0).r * 0.35;
        float wisp = smoothstep(0.45, 0.78, n) * exp(-above * 2.6) * (1.0 - smoothstep(28.0, 40.0, s));
        opacity += wisp * step_ * 0.5 * (1.0 - opacity);
    }
    return clamp(opacity * amount, 0.0, 0.6);
}
#endif
