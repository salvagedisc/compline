#include "/lib/clouds.glsl"
#include "/lib/water.glsl"
#include "/lib/voxel.glsl"
#include "/lib/farshadow.glsl"
/** Bloc qui éclaire par lui-même (block.properties : 8, 16 à 22). */
bool emissive(int mat) { return mat == 8 || (mat >= 16 && mat <= 22); }

// Éclairage direct d'une surface : soleil ou lune avec ombre, ciel, blocs lumineux.

/** Normale (repère monde) rangée dans deux canaux, et relue. */
vec2 packNormal(vec3 n) {
    n /= abs(n.x) + abs(n.y) + abs(n.z);
    vec2 p = n.y >= 0.0 ? n.xz : (1.0 - abs(n.zx)) * vec2(n.x >= 0.0 ? 1.0 : -1.0, n.z >= 0.0 ? 1.0 : -1.0);
    return p * 0.5 + 0.5;
}

/**
 * albedo : couleur linéaire ; normal : repère monde ; lm : lumière des blocs et du ciel (0..1) ; ao : occlusion ;
 * pos : repère joueur ; leaf : 0 surface dure, 1 feuillage que la lumière traverse ; frag : gl_FragCoord.xy.
 */
vec3 shade(vec3 albedo, vec3 normal, vec2 lm, float ao, vec3 pos, float leaf, vec2 frag, int taps, out float indirect) {
    float facing = dot(normal, gLightDir);
    // Un feuillage reste plus clair du côté du soleil : il garde du volume tout en laissant passer la lumière.
    float diffuse = mix(max(facing, 0.0) * 0.8 + 0.2 * leaf, 0.72, leaf * 0.55);
    float lit = 0.0;
    if (diffuse > 0.0) {
        float dist = length(pos);
        // Au-delà de la carte d'ombre, la lumière du ciel du jeu tient lieu d'ombre.
        float beyond = smoothstep(shadowDistance * 0.82, shadowDistance * 0.98, dist);
        float fallback = smoothstep(0.80, 0.94, lm.y);
#if FAR_SHADOWS > 0
        // Plus loin encore, le relief vu par la caméra fait de l'ombre (lib/farshadow.glsl).
        if (beyond > 0.0) fallback *= farShadow(pos, normal, dither(frag + 5.588238 * float(frameCounter & 63)));
#endif
        lit = beyond < 1.0 ? mix(shadowAt(pos, normal, dither(frag), taps), fallback, beyond) : fallback;
    }
    // Une feuille à l'ombre d'une autre reçoit encore le soleil passé à travers le feuillage.
    lit = max(lit, 0.28 * leaf);
    // L'ombre des nuages qui passent.
    if (diffuse > 0.0) lit *= cloudShadow(pos + cameraPosition, gLightDir);
    vec3 light = gLightColor * diffuse * lit;
    // Sous l'eau, la lumière arrive affaiblie et teintée, rassemblée en filets par les vagues.
    if (lit > 0.0) {
        float water = waterAbove(pos, normal);
        if (water > 0.0) {
            vec3 hue = waterHue(vec3(0.25, 0.46, 0.89));
            light *= exp(-waterExtinction(hue) * water) * causticLight(pos + cameraPosition, water, WATER_WAVE_COUNT);
        }
        // Passé par un vitrail, le soleil en prend la couleur.
        light *= glassTint(pos, normal);
    }
    // Le feuillage à contre-jour laisse passer un peu de soleil.
    vec3 view = normalize(pos);
    light += gLightColor * leaf * lit * 0.35 * pow(max(dot(view, gLightDir), 0.0), 4.0);
    float direct = luma(light);

    float sky = lm.y * lm.y;
    float occl = ao * ao;
    light += gAmbient * (0.70 + 0.30 * normal.y) * sky * occl;
    // La lumière des blocs prend la couleur de ce qui éclaire autour (lib/voxel.glsl) : orangée pour une flamme,
    // bleue pour une âme, rouge pour la lave…
    float blockLm = lm.x;
    vec3 tint = blockLightTint(pos, normal, blockLm);
    float glow = blockLm * blockLm * (0.30 + 2.3 * blockLm * blockLm * blockLm);
    light += tint * glow * mix(occl, 1.0, blockLm * blockLm);
    light += vec3(0.016, 0.018, 0.022) * occl;
    light += vec3(0.30, 0.32, 0.34) * nightVision;
    light *= 1.0 - 0.7 * darknessFactor;
    // Part de la lumière qui vient du ciel et des blocs : c'est elle que l'occlusion d'écran assombrit.
    indirect = clamp(1.0 - direct * (1.0 - 0.7 * darknessFactor) / max(luma(light), 1e-5), 0.0, 1.0);
    return albedo * light;
}
