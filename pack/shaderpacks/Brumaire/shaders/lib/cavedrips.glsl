// Les gouttes des grottes : sous les voûtes humides, de l'eau suinte et tombe goutte à goutte, toujours aux mêmes
// endroits, et ne se voit que dans la lumière d'une torche ou de la lave qui s'y reflète. La voûte et le sol de chaque
// colonne sont lus dans le volume des blocs (lib/voxel.glsl). Demande voxel.glsl et motes.glsl (moteHash) avant lui.

#if CAVE_DRIPS > 0 && defined VOXELS && COLORED_LIGHT > 0
bool dripSolid(vec3 block) {
    ivec3 c = ivec3(block - floor(cameraPosition)) + VOX_HALF;
    return voxelInside(c) && (texelFetch(voxelTex, c, 0).r & VOX_SOLID) != 0u;
}

/**
 * La goutte la plus proche le long d'un rayon jusqu'à t1 (blocs) : 1 si le rayon la touche, sa distance dans dist.
 * Une colonne sur vingt, sous une voûte à douze blocs au plus, laisse tomber une goutte toutes les deux à cinq
 * secondes : elle pend un instant, puis chute en accélérant jusqu'au sol. Les cases sont parcourues une à une.
 */
float dripAlong(vec3 dir, float t1, out float dist) {
    dist = 0.0;
    if (t1 <= 0.4) return 0.0;
    vec3 camFloor = floor(cameraPosition);
    vec3 o = cameraPosition - camFloor;
    vec3 d = vec3(abs(dir.x) < 1e-5 ? 1e-5 : dir.x, abs(dir.y) < 1e-5 ? 1e-5 : dir.y, abs(dir.z) < 1e-5 ? 1e-5 : dir.z);
    vec3 cell = floor(o + dir * 0.4);
    vec3 stepDir = sign(d);
    vec3 tDelta = abs(1.0 / d);
    vec3 next = (cell + max(stepDir, 0.0) - o) / d;
    float pixel = 2.0 / (gbufferProjection[1][1] * viewHeight);
    float time = frameTimeCounter;
    for (int i = 0; i < 40; i++) {
        float tExit = min(min(next.x, next.y), min(next.z, t1));
        vec3 wc = cell + camFloor;
        vec3 col = vec3(wc.x, 0.0, wc.z);
        float h = moteHash(col, 21.0);
        if (h < 0.05 && !dripSolid(wc)) {
            // La voûte au-dessus de cette case, et le sol en dessous.
            float ceiling = -1.0, ground = -1.0;
            for (int k = 1; k <= 12; k++) {
                if (dripSolid(wc + vec3(0.0, float(k), 0.0))) { ceiling = wc.y + float(k); break; }
            }
            if (ceiling > 0.0) {
                for (int k = 1; k <= 12; k++) {
                    if (dripSolid(wc - vec3(0.0, float(k), 0.0))) { ground = wc.y - float(k) + 1.0; break; }
                }
            }
            if (ceiling > 0.0 && ground > -1.0) {
                float period = 2.0 + 3.0 * moteHash(col, 22.0);
                float age = mod(time + moteHash(col, 23.0) * period, period);
                // Elle pend une demi-seconde sous la voûte, puis tombe (g = 20 blocs par seconde au carré).
                float fall = max(age - 0.5, 0.0);
                float y = ceiling - 0.06 - 10.0 * fall * fall;
                if (y > ground && floor(y) == wc.y) {
                    vec3 p = vec3(wc.x + 0.3 + 0.4 * moteHash(col, 24.0), y, wc.z + 0.3 + 0.4 * moteHash(col, 25.0)) - camFloor - o;
                    float s = dot(p, dir);
                    if (s > 0.4 && s < t1) {
                        vec3 off = dir * s - p;
                        float size = max(0.5 / 16.0, s * pixel * 0.6);
                        // Une goutte qui tombe s'étire : deux pixels de haut.
                        float len_ = fall > 0.0 ? size * 2.5 : size;
                        if (length(off.xz) < size && abs(off.y) < len_) {
                            dist = s;
                            return 1.0 - smoothstep(t1 * 0.6, t1, s);
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
    return 0.0;
}
#endif
