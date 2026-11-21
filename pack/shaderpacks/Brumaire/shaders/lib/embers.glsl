// La lave sous la vapeur des averses (LAVA_STEAM, program/mist.fsh) : son dessus et le ciel ouvert au-dessus,
// lus dans le volume des blocs (lib/voxel.glsl : lumière 12 et plus, famille 2). Demande voxel.glsl avant lui.
// Les braises qui en montaient sont parties le 2026-11-21 : les étincelles de lave de Subtle Effects le font déjà.

#if LAVA_STEAM > 0 && defined VOXELS
/** Le dessus de la lave (repère monde, haut de la case de lave) sous une case, à six blocs au plus, ou -1e4. */
float lavaBelow(ivec3 c) {
    for (int k = 0; k <= 6; k++) {
        ivec3 b = c - ivec3(0, k, 0);
        if (!voxelInside(b)) return -1e4;
        uint v = texelFetch(voxelTex, b, 0).r;
        if ((v & 15u) >= 12u && ((v >> 4u) & 15u) == 2u) return float(-k);
        if ((v & VOX_SOLID) != 0u) return -1e4;
    }
    return -1e4;
}
#endif


#if LAVA_STEAM > 0 && defined VOXELS
/** Vrai si rien n'arrête la pluie au-dessus de la case c, sur huit blocs (au-delà, le volume ne dit plus rien). */
bool openAbove(ivec3 c) {
    for (int k = 1; k <= 8; k++) {
        ivec3 b = c + ivec3(0, k, 0);
        if (!voxelInside(b)) return true;
        if ((texelFetch(voxelTex, b, 0).r & VOX_RAIN) != 0u) return false;
    }
    return true;
}

/**
 * La vapeur de la lave sous la pluie : les gouttes qui tombent sur la lave à ciel ouvert bouillent aussitôt en grosses
 * volutes blanches qui montent jusqu'à quatre blocs, s'effilochent et filent avec le vent. Épaisseur 0..1 le long d'un
 * rayon jusqu'à t1 (blocs) : seize prises jusqu'à 28 blocs.
 */
float lavaSteamAlong(vec3 dir, float t1, float amount) {
    if (amount <= 0.01) return 0.0;
    float reach = min(t1, 28.0);
    float step_ = reach / 16.0;
    float jitter = fract(52.9829189 * fract(dot(gl_FragCoord.xy, vec2(0.06711056, 0.00583715))));
    float t = frameTimeCounter;
    vec2 wind = brumaireLink > 0.5 ? brumaireWind.xy * (0.3 + brumaireWind.z) : vec2(0.2, 0.1);
    float opacity = 0.0;
    for (int i = 0; i < 16; i++) {
        float s = (float(i) + jitter) * step_;
        vec3 rel = dir * s;
        ivec3 c = voxelCell(rel, fract(cameraPosition));
        if (!voxelInside(c) || texelFetch(voxelTex, c, 0).r != 0u) continue;
        float rel_ = lavaBelow(c - ivec3(0, 1, 0));
        if (rel_ < -1e3 || !openAbove(c)) continue;
        vec3 w = rel + cameraPosition;
        // Hauteur au-dessus de la surface de la lave (haut de sa case, un peu plus bas qu'un bloc plein).
        float h = w.y - (floor(w.y) - 1.0 + rel_ + 0.9);
        if (h < 0.0 || h > 4.5) continue;
        vec2 q = w.xz - wind * h * 0.6;
        float n = cloudNoise(vec3(q * 0.35 + t * 0.04, w.y * 0.45 - t * 0.55)).r * 0.6
                + cloudNoise(vec3(q * 0.9 - t * 0.07, w.y * 1.1 - t * 0.9) + 5.0).r * 0.4;
        float puff = smoothstep(0.30, 0.62, n) * exp(-h * 0.45) * (1.0 - smoothstep(20.0, 28.0, s));
        opacity += puff * step_ * 0.9 * (1.0 - opacity);
    }
    return clamp(opacity * amount, 0.0, 0.85);
}
#endif
