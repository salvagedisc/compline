// Les embruns des cascades : là où l'eau tombe, un nuage de fines gouttes l'entoure, plus épais à son pied, qui roule
// et monte en se défaisant. Une chute est une case d'eau sous une autre case d'eau, à côté d'une case d'air (lib/voxel.glsl,
// bit VOX_WATER). Demande voxel.glsl et clouds.glsl (cloudNoise) avant lui.

#if (FALL_SPRAY > 0 || SHORE_SPRAY > 0) && defined VOXELS
#if FALL_SPRAY > 0
bool fallingWater(ivec3 c) {
    if (!voxelInside(c) || !voxelInside(c + ivec3(0, 1, 0))) return false;
    return (texelFetch(voxelTex, c, 0).r & VOX_WATER) != 0u && (texelFetch(voxelTex, c + ivec3(0, 1, 0), 0).r & VOX_WATER) != 0u;
}

/** Combien de chute d'eau autour d'une case d'air (0..1) : les quatre voisines à un et deux blocs, et celles du dessous. */
float fallNear(ivec3 c) {
    float n = 0.0;
    for (int k = 0; k < 4; k++) {
        ivec3 side = k == 0 ? ivec3(1, 0, 0) : k == 1 ? ivec3(-1, 0, 0) : k == 2 ? ivec3(0, 0, 1) : ivec3(0, 0, -1);
        if (fallingWater(c + side)) n += 1.0;
        else if (fallingWater(c + side * 2)) n += 0.5;
        else if (fallingWater(c + side - ivec3(0, 1, 0))) n += 0.6;
    }
    return min(n / 2.0, 1.0);
}

/** L'épaisseur des embruns le long d'un rayon jusqu'à t1 (blocs), 0..1 : quatorze prises jusqu'à 28 blocs. */
float sprayAlong(vec3 dir, float t1) {
    float reach = min(t1, 28.0);
    if (reach <= 0.5) return 0.0;
    float step_ = reach / 14.0;
    float jitter = fract(52.9829189 * fract(dot(gl_FragCoord.xy, vec2(0.06711056, 0.00583715))));
    float t = frameTimeCounter;
    float opacity = 0.0;
    for (int i = 0; i < 14; i++) {
        float s = (float(i) + jitter) * step_;
        vec3 rel = dir * s;
        ivec3 c = voxelCell(rel, fract(cameraPosition));
        if (!voxelInside(c) || (texelFetch(voxelTex, c, 0).r & (VOX_SOLID | VOX_WATER)) != 0u) continue;
        float near = fallNear(c);
        if (near <= 0.0) continue;
        // Au pied de la chute (de l'eau juste dessous), le nuage s'épaissit.
        ivec3 below = c - ivec3(0, 1, 0);
        float foot = voxelInside(below) && (texelFetch(voxelTex, below, 0).r & VOX_WATER) != 0u ? 1.6 : 1.0;
        vec3 w = rel + cameraPosition;
        float n = cloudNoise(vec3(w.xz * 0.45 + t * 0.07, w.y * 0.5 - t * 0.6)).r * 0.6
                + cloudNoise(vec3(w.xz * 1.2 - t * 0.11, w.y * 1.4 - t * 1.1) + 7.0).r * 0.4;
        float puff = smoothstep(0.3, 0.75, n) * near * foot * (1.0 - smoothstep(20.0, 28.0, s));
        opacity += puff * step_ * 0.3 * (1.0 - opacity);
    }
    return clamp(opacity, 0.0, 0.75);
}
#endif

#if SHORE_SPRAY > 0
/** Une case d'air au-dessus de l'eau, contre une rive (un bloc plein à un ou deux blocs, au niveau de l'eau) ? */
float shoreNear(ivec3 c) {
    ivec3 below = c - ivec3(0, 1, 0);
    if (!voxelInside(below) || (texelFetch(voxelTex, below, 0).r & VOX_WATER) == 0u) return 0.0;
    for (int k = 0; k < 8; k++) {
        ivec3 side = (k % 4 == 0 ? ivec3(1, 0, 0) : k % 4 == 1 ? ivec3(-1, 0, 0) : k % 4 == 2 ? ivec3(0, 0, 1) : ivec3(0, 0, -1)) * (k < 4 ? 1 : 2);
        ivec3 n = below + side;
        if (voxelInside(n) && (texelFetch(voxelTex, n, 0).r & VOX_SOLID) != 0u) return k < 4 ? 1.0 : 0.6;
    }
    return 0.0;
}

/**
 * Les embruns des rivages par grand vent (0..1) : au-dessus de l'eau qui bat une rive, des bouffées blanches que le
 * vent arrache et emporte, jusqu'à deux blocs. Douze prises jusqu'à 36 blocs. gale : force du coup de vent (0..1).
 */
float shoreSprayAlong(vec3 dir, float t1, float gale) {
    float reach = min(t1, 36.0);
    if (reach <= 0.5 || gale <= 0.01) return 0.0;
    float step_ = reach / 12.0;
    float jitter = fract(52.9829189 * fract(dot(gl_FragCoord.xy, vec2(0.06711056, 0.00583715))));
    float t = frameTimeCounter;
    vec2 wind = normalize(brumaireWind.xy + vec2(1e-4, 0.0));
    float opacity = 0.0;
    for (int i = 0; i < 12; i++) {
        float s = (float(i) + jitter) * step_;
        vec3 rel = dir * s;
        ivec3 c = voxelCell(rel, fract(cameraPosition));
        ivec3 c2 = c - ivec3(0, 1, 0);
        float lift = 0.0;
        if (!voxelInside(c) || texelFetch(voxelTex, c, 0).r != 0u) continue;
        float near = shoreNear(c);
        // Une case plus haut aussi : la bouffée monte jusqu'à deux blocs.
        if (near <= 0.0 && voxelInside(c2) && texelFetch(voxelTex, c2, 0).r == 0u) { near = shoreNear(c2) * 0.5; lift = 1.0; }
        if (near <= 0.0) continue;
        vec3 w = rel + cameraPosition;
        float above = fract(w.y) + lift;
        // Des bouffées qui partent par à-coups, emportées dans le sens du vent.
        vec2 q = vec2(dot(w.xz, wind) - t * 6.0 * gale, dot(w.xz, vec2(-wind.y, wind.x)));
        float burst = cloudNoise(vec3(q * 0.35, t * 0.25)).r;
        float n = cloudNoise(vec3(q * 1.1, w.y * 0.9 - t * 0.8) + 3.0).r;
        float puff = smoothstep(0.2, 0.5, burst) * smoothstep(0.15, 0.5, n) * near * exp(-above * 1.0)
                   * (1.0 - smoothstep(26.0, 36.0, s));
        opacity += puff * step_ * 0.75 * (1.0 - opacity);
    }
    return clamp(opacity * gale, 0.0, 0.8);
}
#endif
#endif
