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
