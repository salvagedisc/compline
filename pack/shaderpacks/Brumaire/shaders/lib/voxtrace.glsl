// Reflets hors écran : quand le reflet d'écran (lib/reflect.glsl) ne trouve rien, parce que ce qui se reflète est
// derrière la caméra, hors du cadre ou caché, le rayon réfléchi avance de bloc en bloc dans le volume de la lumière
// colorée (lib/voxel.glsl). Le premier bloc qui se voit dans un reflet (bit VOX_MIRROR) donne sa couleur moyenne,
// celle du dessus si le rayon y entre par le haut, sinon celle des côtés, éclairée simplement : soleil avec son
// ombre, ciel s'il n'est pas couvert, lumière des blocs alentour. Pas de détail de texture : à la distance où l'écran
// ne montre plus la chose, une rive ou un tronc reflétés se lisent par leur masse et leur couleur.
// Demande avant lui : common.glsl, shadow.glsl, clouds.glsl, voxel.glsl.

#if VOXEL_REFLECTIONS > 0
uniform usampler3D voxTopTex;
uniform usampler3D voxSideTex;

vec3 voxColor(uint v) {
    return vec3(float(v & 255u), float((v >> 8) & 255u), float((v >> 16) & 255u)) / 255.0;
}

/** Vrai si un bloc plein couvre la case dans les huit blocs au-dessus : le ciel n'y éclaire plus guère. */
bool voxCovered(ivec3 c) {
    for (int k = 1; k <= 8; k++) {
        ivec3 u = c + ivec3(0, k, 0);
        if (u.y >= VOX_SIZE.y) return false;
        if ((texelFetch(voxelTex, u, 0).r & VOX_SOLID) != 0u) return true;
    }
    return false;
}

/**
 * Ce que voit un rayon parti de start (repère joueur) dans la direction dir : couleur éclairée et part (0 : rien trouvé
 * dans le volume, le ciel reste). dist : chemin jusqu'au bloc.
 */
vec4 voxelReflection(vec3 start, vec3 dir, out float dist) {
    dist = 0.0;
    vec3 p = start + fract(cameraPosition) + vec3(VOX_HALF);
    ivec3 cell = ivec3(floor(p));
    vec3 sgn = sign(dir);
    ivec3 stepv = ivec3(sgn);
    vec3 inv = 1.0 / max(abs(dir), vec3(1e-5));
    vec3 tMax = mix(fract(p), 1.0 - fract(p), step(0.0, sgn)) * inv;
    int axis = 1;
    float t = 0.0;
    for (int i = 0; i < VOXEL_REFLECTIONS; i++) {
        if (tMax.x < tMax.y && tMax.x < tMax.z) {
            cell.x += stepv.x; t = tMax.x; tMax.x += inv.x; axis = 0;
        } else if (tMax.y < tMax.z) {
            cell.y += stepv.y; t = tMax.y; tMax.y += inv.y; axis = 1;
        } else {
            cell.z += stepv.z; t = tMax.z; tMax.z += inv.z; axis = 2;
        }
        if (!voxelInside(cell)) break;
        uint code = texelFetch(voxelTex, cell, 0).r;
        if ((code & VOX_MIRROR) == 0u) continue;

        vec3 n = vec3(0.0);
        n[axis] = -sgn[axis];
        uint top = texelFetch(voxTopTex, cell, 0).r;
        uint side = texelFetch(voxSideTex, cell, 0).r;
        uint word = axis == 1 && stepv.y < 0 ? (top != 0u ? top : side) : (side != 0u ? side : top);
        vec3 albedo = word != 0u ? olive(toLinear(voxColor(word))) : vec3(0.18);

        vec3 pos = start + dir * t;
        dist = t;
        float facing = max(dot(n, gLightDir), 0.0);
        float lit = 0.0;
        if (facing > 0.0) {
            lit = length(pos) < shadowDistance * 0.9 ? shadowAt(pos + n * 0.05, n, 0.5, 1) : 1.0;
            lit *= cloudShadow(pos + cameraPosition, gLightDir);
        }
        float sky = voxCovered(cell) ? 0.15 : 1.0;
        vec3 light = gLightColor * facing * lit + gAmbient * (0.70 + 0.30 * n.y) * sky + vec3(0.016, 0.018, 0.022);
#if COLORED_LIGHT > 0
        vec4 around = voxelLight(pos, n);
        light += around.rgb * around.a * around.a * (0.30 + 2.3 * around.a * around.a * around.a);
#endif
        light += vec3(0.30, 0.32, 0.34) * nightVision;
        vec3 color = albedo * light;
        // Un bloc qui éclaire brille aussi dans le reflet.
        if ((code & 15u) > 0u) color += albedo * 1.7;

        // Vers le bord du volume, le reflet s'efface : pas de couture quand on s'éloigne.
        ivec3 edge = min(cell, VOX_SIZE - 1 - cell);
        float fade = smoothstep(0.0, 6.0, float(min(edge.x, min(edge.y, edge.z))));
        return vec4(color, fade);
    }
    return vec4(0.0);
}
#endif
