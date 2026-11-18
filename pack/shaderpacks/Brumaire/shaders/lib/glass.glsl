// Le verre qui se raccorde : un bloc de verre (ou une vitre) qui en touche un autre perd son cadre de ce côté, et
// une verrière se lit d'un seul tenant. Les voisins sont lus dans le volume des blocs (lib/voxel.glsl, bit VOX_GLASS),
// qui ne couvre que les environs de la caméra : plus loin, les cadres restent.
// Demande voxel.glsl avant lui.

#if CONNECTED_GLASS > 0 && defined VOXELS && defined FSH

bool glassAt(vec3 cell) {
    ivec3 c = voxelCell(cell + 0.5 - cameraPosition, fract(cameraPosition));
    return voxelInside(c) && (texelFetch(voxelTex, c, 0).r & VOX_GLASS) != 0u;
}

/**
 * Le texel d'une face de verre, sans son cadre là où le voisin est du verre aussi. color : texel déjà teint ;
 * world : position (repère monde) ; n : normale de la face ; uv : coordonnées dans l'atlas.
 * Un verre clair (peu couvrant en moyenne) laisse alors voir au travers ; un vitrail prend sa teinte moyenne.
 */
vec4 connectGlass(vec4 color, vec3 world, vec3 n, vec2 uv, vec3 tint) {
    vec3 an = abs(n);
    if (max(an.x, max(an.y, an.z)) < 0.9) return color;
    vec3 inside = world - n * (0.5 / 16.0);
    vec3 cell = floor(inside);
    vec3 f = inside - cell;
    // Les deux axes de la face.
    vec3 u = an.x > 0.5 ? vec3(0.0, 1.0, 0.0) : vec3(1.0, 0.0, 0.0);
    vec3 v = an.z > 0.5 ? vec3(0.0, 1.0, 0.0) : vec3(0.0, 0.0, 1.0);
    float fu = dot(f, u), fv = dot(f, v);
    const float EDGE = 1.0 / 16.0;
    float su = fu < EDGE ? -1.0 : (fu > 1.0 - EDGE ? 1.0 : 0.0);
    float sv = fv < EDGE ? -1.0 : (fv > 1.0 - EDGE ? 1.0 : 0.0);
    if (su == 0.0 && sv == 0.0) return color;
    bool gu = su != 0.0 && glassAt(cell + u * su);
    bool gv = sv != 0.0 && glassAt(cell + v * sv);
    bool hide = (su == 0.0 || gu) && (sv == 0.0 || gv);
    // Au coin, le cadre ne s'efface que si le verre continue aussi en diagonale.
    if (su != 0.0 && sv != 0.0) hide = hide && glassAt(cell + u * su + v * sv);
    if (!hide) return color;
    vec4 mean = textureLod(gtexture, uv, 4.0);
    if (mean.a < 0.38) return vec4(color.rgb, 0.0);
    return vec4(mean.rgb * tint, mean.a * 0.9);
}
#else
vec4 connectGlass(vec4 color, vec3 world, vec3 n, vec2 uv, vec3 tint) { return color; }
#endif

#if WINDOW_FROST > 0 && defined FSH
/**
 * Le givre d'une vitre, au seizième de bloc : il part du cadre et avance vers le milieu, plus loin le long de traits en
 * diagonale (des fougères). Une verrière raccordée ne givre que sur son pourtour. cold : froid (0..1).
 * Rend x : part du texel couverte (0..1), y : son éclat.
 */
vec2 windowFrost(vec3 world, vec3 n, float cold) {
    if (cold <= 0.02) return vec2(0.0);
    vec3 an = abs(n);
    if (max(an.x, max(an.y, an.z)) < 0.9) return vec2(0.0);
    vec3 inside = world - n * (0.5 / 16.0);
    vec3 cell = floor(inside);
    vec3 u = an.x > 0.5 ? vec3(0.0, 1.0, 0.0) : vec3(1.0, 0.0, 0.0);
    vec3 v = an.z > 0.5 ? vec3(0.0, 1.0, 0.0) : vec3(0.0, 0.0, 1.0);
    vec2 px = floor(vec2(dot(inside - cell, u), dot(inside - cell, v)) * 16.0);
    // Distance en pixels au bord le plus proche ; un bord qui touche un autre verre ne compte pas.
    vec4 edge = vec4(px.x, 15.0 - px.x, px.y, 15.0 - px.y);
#if CONNECTED_GLASS > 0 && defined VOXELS
    if (glassAt(cell - u)) edge.x += 16.0;
    if (glassAt(cell + u)) edge.y += 16.0;
    if (glassAt(cell - v)) edge.z += 16.0;
    if (glassAt(cell + v)) edge.w += 16.0;
#endif
    float d = min(min(edge.x, edge.y), min(edge.z, edge.w));
    // Dans les coins, le givre s'épaissit.
    d = min(d, 0.62 * (min(edge.x, edge.y) + min(edge.z, edge.w)) - 1.0);
    vec2 gp = floor(vec2(dot(inside, u), dot(inside, v)) * 16.0);
    float face = dot(cell, n);
    float h = rainHash(gp + face * 3.1);
    float fern = 0.0;
    if (mod(gp.x + gp.y, 4.0) < 0.5) fern = rainHash(vec2(gp.x + gp.y, face * 1.7));
    if (mod(gp.x - gp.y, 5.0) < 0.5) fern = max(fern, rainHash(vec2(gp.x - gp.y, face * 2.3)));
    float front = cold * 4.5 + h * 1.4 + fern * 3.5 * cold;
    if (d >= front) return vec2(0.0);
    return vec2(1.0 - 0.35 * smoothstep(front - 2.0, front, d), h);
}
#endif
