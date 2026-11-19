// La brume qui fume sur l'eau : les matins frais, et à toute heure par grand froid, l'eau plus tiède que l'air fume ;
// des volutes pâles montent lentement d'un bloc ou deux au-dessus des lacs et des rivières proches. L'eau est lue dans
// le volume des blocs (lib/voxel.glsl, bit VOX_WATER). Demande voxel.glsl et clouds.glsl (cloudNoise) avant lui.

#if WATER_STEAM > 0 && defined VOXELS
uniform float sunAngle;
#define HAS_SUN_ANGLE

/** Combien l'eau fume ici (0..1) : fraîcheur du biome, heure (la nuit et le matin), ni pluie ni vent fort. */
float steamAmount() {
    float cool = 1.0 - smoothstep(0.55, 0.95, airHeat);
    float frozen = 1.0 - smoothstep(0.05, 0.25, airHeat);
    float hour = sunAngle < 0.5 ? 1.0 - smoothstep(0.05, 0.16, sunAngle) : smoothstep(0.55, 0.75, sunAngle);
    float calm = 1.0 - smoothstep(0.5, 0.9, brumaireLink > 0.5 ? brumaireWind.z : 0.3);
    return max(cool * hour, frozen) * calm * (1.0 - rainStrength);
}

/** Hauteur (blocs) au-dessus d'une eau à l'air libre, ou -1 : la case du point est de l'air et une des deux dessous de l'eau. */
float aboveWater(vec3 rel) {
    ivec3 c = voxelCell(rel, fract(cameraPosition));
    if (!voxelInside(c) || texelFetch(voxelTex, c, 0).r != 0u) return -1.0;
    float frac_ = fract(rel.y + cameraPosition.y);
    for (int k = 1; k <= 2; k++) {
        ivec3 b = c - ivec3(0, k, 0);
        if (!voxelInside(b)) return -1.0;
        uint v = texelFetch(voxelTex, b, 0).r;
        if ((v & VOX_WATER) != 0u) return float(k - 1) + frac_ + 0.1;
        if (v != 0u) return -1.0;
    }
    return -1.0;
}

/**
 * L'épaisseur des volutes le long d'un rayon jusqu'à t1 (blocs), 0..1 : dix prises jusqu'à 56 blocs, sur le bout du
 * rayon qui rase l'eau quand il plonge vers elle, sur tout le rayon quand il file à l'horizontale.
 */
float steamAlong(vec3 dir, float t1, float amount) {
    if (amount <= 0.01) return 0.0;
    float reach = min(t1, 56.0);
    float span = dir.y < -0.005 ? min(2.0 / -dir.y, reach) : reach;
    float start = reach - span;
    float step_ = span / 10.0;
    float jitter = fract(52.9829189 * fract(dot(gl_FragCoord.xy, vec2(0.06711056, 0.00583715))));
    float t = frameTimeCounter;
    float opacity = 0.0;
    for (int i = 0; i < 10; i++) {
        float s = start + (float(i) + jitter) * step_;
        vec3 rel = dir * s;
        float h = aboveWater(rel);
        if (h < 0.0 || h > 1.8) continue;
        vec3 w = rel + cameraPosition;
        // Des volutes qui montent et dérivent doucement : plus denses au ras de l'eau, effilochées en haut.
        float n = cloudNoise(vec3(w.xz * 0.18 + t * 0.03, w.y * 0.35 - t * 0.12)).r * 0.65
                + cloudNoise(vec3(w.xz * 0.5 - t * 0.05, w.y * 0.9 - t * 0.25) + 3.0).r * 0.35;
        float wisp = smoothstep(0.42, 0.75, n) * exp(-h * 1.6) * (1.0 - smoothstep(40.0, 56.0, s));
        opacity += wisp * step_ * 0.22 * (1.0 - opacity);
    }
    return clamp(opacity * amount, 0.0, 0.7);
}
#endif
