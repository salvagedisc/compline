// Les braises : au-dessus de la lave, de petites escarbilles se détachent, montent en tournoyant, poussées par le vent,
// passent du jaune à l'orange puis au rouge et s'éteignent. La lave est lue dans le volume des blocs (lib/voxel.glsl :
// lumière 12 et plus, famille 2). Demande voxel.glsl et motes.glsl (moteHash) avant lui.

#if LAVA_EMBERS > 0 && defined VOXELS
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

/**
 * L'éclat des braises le long d'un rayon jusqu'à t1 (blocs) : la couleur de la plus proche touchée (linéaire, à
 * ajouter), 40 cases au plus. Une colonne de lave sur deux en porte deux, chacune vit deux à trois secondes et revient
 * toutes les trois à six secondes.
 */
vec3 embersAlong(vec3 dir, float t1) {
    if (t1 <= 0.4) return vec3(0.0);
    vec3 camFloor = floor(cameraPosition);
    vec3 o = cameraPosition - camFloor;
    vec3 d = vec3(abs(dir.x) < 1e-5 ? 1e-5 : dir.x, abs(dir.y) < 1e-5 ? 1e-5 : dir.y, abs(dir.z) < 1e-5 ? 1e-5 : dir.z);
    vec3 cell = floor(o + dir * 0.4);
    vec3 stepDir = sign(d);
    vec3 tDelta = abs(1.0 / d);
    vec3 next = (cell + max(stepDir, 0.0) - o) / d;
    float pixel = 2.0 / (gbufferProjection[1][1] * viewHeight);
    float time = frameTimeCounter;
    vec2 wind = brumaireLink > 0.5 ? brumaireWind.xy * (0.3 + brumaireWind.z) : vec2(0.2, 0.1);
    for (int i = 0; i < 40; i++) {
        float tExit = min(min(next.x, next.y), min(next.z, t1));
        vec3 wc = cell + camFloor;
        vec3 col = vec3(wc.x, 0.0, wc.z);
        if (moteHash(col, 41.0) < 0.5) {
            ivec3 c = ivec3(cell) + VOX_HALF;
            float rel = voxelInside(c) && texelFetch(voxelTex, c, 0).r == 0u ? lavaBelow(c - ivec3(0, 1, 0)) : -1e4;
            if (rel > -1e3) {
                // Le dessus de la lave : la case de lave est rel blocs sous la case d'air au-dessous de celle-ci.
                float top = wc.y - 1.0 + rel + 0.9;
                for (int k = 0; k < 2; k++) {
                    float kk = float(k) * 5.0;
                    float period = 3.0 + 3.0 * moteHash(col, 42.0 + kk);
                    float life = 2.0 + moteHash(col, 43.0 + kk);
                    float age = mod(time + moteHash(col, 44.0 + kk) * period, period);
                    if (age > life) continue;
                    float u = age / life;
                    // Elle jaillit, ralentit et flotte : quatre blocs au plus.
                    float y = top + 4.0 * (1.0 - exp(-1.6 * age)) * (0.6 + 0.4 * moteHash(col, 45.0 + kk));
                    if (floor(y) != wc.y) continue;
                    float spin = time * (2.0 + 2.0 * moteHash(col, 46.0 + kk)) + kk;
                    vec2 drift = clamp(vec2(sin(spin), cos(spin * 1.3)) * 0.18 + wind * 0.3 * age, -0.4, 0.4);
                    vec3 p = vec3(wc.x + 0.5 + drift.x, y, wc.z + 0.5 + drift.y) - cameraPosition;
                    float s = dot(p, dir);
                    if (s > 0.4 && s < t1) {
                        float miss = length(dir * s - p);
                        float core = max(0.7 / 16.0, s * pixel * 0.7);
                        float halo = exp(-miss * miss / (core * core * 6.0)) * 0.3 + step(miss, core);
                        if (halo > 0.01) {
                            // Du jaune au rouge en refroidissant ; elle scintille, puis s'éteint.
                            vec3 hue = mix(vec3(1.0, 0.62, 0.18), vec3(0.9, 0.16, 0.03), smoothstep(0.1, 0.8, u));
                            float flick = 0.7 + 0.3 * sin(time * 23.0 + kk + moteHash(col, 47.0) * 40.0);
                            float fade = (1.0 - smoothstep(0.6, 1.0, u)) * smoothstep(0.0, 0.05, u);
                            return hue * halo * flick * fade * 3.0 * (1.0 - smoothstep(t1 * 0.6, t1, s));
                        }
                    }
                }
            }
        }
        if (tExit >= t1) break;
        if (next.x < next.y && next.x < next.z) { next.x += tDelta.x; cell.x += stepDir.x; }
        else if (next.y < next.z) { next.y += tDelta.y; cell.y += stepDir.y; }
        else { next.z += tDelta.z; cell.z += stepDir.z; }
    }
    return vec3(0.0);
}
#endif
