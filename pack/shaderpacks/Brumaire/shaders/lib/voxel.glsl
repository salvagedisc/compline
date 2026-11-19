// La lumière colorée des blocs : un volume de 128 × 64 × 128 blocs centré sur la caméra. La passe d'ombre y range
// chaque bloc vu (plein, laisse passer la lumière, ou lumineux avec sa couleur) ; shadowcomp y fait diffuser la
// lumière de proche en proche, une case par image, arrêtée par les blocs pleins. Le volume ne donne que la teinte : la force
// de la lumière reste celle du jeu, qui sait déjà ce qui l'arrête.

#define VOX_W 128
#define VOX_H 64
const ivec3 VOX_SIZE = ivec3(VOX_W, VOX_H, VOX_W);
const ivec3 VOX_HALF = ivec3(VOX_W / 2, VOX_H / 2, VOX_W / 2);

// Ce que la passe d'ombre range dans une case (voxelImg) : 0 air ; bit 8 bloc plein ; bit 9 bloc qui laisse passer ;
// bits 0..3 la lumière émise (0..15), bits 4..7 sa couleur (lightTint).
// Le volume sert aussi à la pluie : shadowcomp1 y cherche, colonne par colonne, ce qui l'arrête.
const uint VOX_SOLID = 256u;
const uint VOX_CLEAR = 512u;
// Bit 10 : le bloc arrête la pluie (tout sauf l'herbe, les fleurs, les lianes et les torches).
const uint VOX_RAIN = 1024u;
// Bit 11 : le bloc se voit dans un reflet (lib/voxtrace.glsl) : tout sauf l'eau, les herbes, les fleurs, les lianes et les
// torches. Sa couleur moyenne est rangée à part, celle du dessus dans voxTopImg, celle des côtés dans voxSideImg.
const uint VOX_MIRROR = 2048u;
// Bit 12 : de l'eau (lib/waterfoam.glsl : rives, chutes, étendue libre).
const uint VOX_WATER = 4096u;
// Bit 13 : du verre, bloc ou vitre (le verre se raccorde à ses voisins, lib/glass.glsl).
const uint VOX_GLASS = 8192u;
// Bit 14 : un feuillage (les feuilles qui tombent, lib/leaffall.glsl).
const uint VOX_LEAF = 16384u;

#if COLORED_LIGHT > 0 || SHADER_RAIN > 0 || VOXEL_REFLECTIONS > 0
#define VOXELS
#endif

/** Case du volume qui contient un point (repère joueur : depuis la caméra). */
ivec3 voxelCell(vec3 pos, vec3 camFract) {
    return ivec3(floor(pos + camFract)) + VOX_HALF;
}

bool voxelInside(ivec3 c) {
    return all(greaterThanEqual(c, ivec3(0))) && all(lessThan(c, VOX_SIZE));
}

#if defined FSH && defined VOXELS
// Le volume tel que la passe d'ombre l'a rangé à cette image (reflets hors écran, écume des rives).
uniform usampler3D voxelTex;

/**
 * Vrai si la face de côté (normale horizontale) du bloc qui contient world est la contremarche d'un palier : devant
 * elle rien de plein, et sous ce vide un bloc plein (la marche du dessous). below : la case de ce bloc. Loin du
 * centre du volume, de 44 à 60 blocs, les faces renoncent une à une au hasard (seed), puis toutes.
 */
bool voxelRiser(vec3 world, vec3 normal, float seed, out ivec3 below) {
    below = ivec3(0);
    vec3 block = floor(world - normal * 0.01) + 0.5;
    vec3 pos = block - cameraPosition;
    float reach = length(pos.xz);
    if (reach > 60.0 || seed < smoothstep(44.0, 60.0, reach)) return false;
    ivec3 front = voxelCell(pos + normal, fract(cameraPosition));
    below = front - ivec3(0, 1, 0);
    if (!voxelInside(front) || !voxelInside(below)) return false;
    if ((texelFetch(voxelTex, front, 0).r & VOX_SOLID) != 0u) return false;
    return (texelFetch(voxelTex, below, 0).r & VOX_SOLID) != 0u;
}
#endif

/** Couleur d'une lumière, par famille de blocs (numéros de block.properties et item.properties, 10000 + n). */
vec3 lightTint(int family) {
    if (family == 1) return vec3(0.30, 0.78, 1.00);  // âmes : torches, lanternes, feux de camp bleus
    if (family == 2) return vec3(1.00, 0.36, 0.08);  // lave, magma
    if (family == 3) return vec3(1.00, 0.76, 0.42);  // pierre lumineuse, lampe, champylure : un jaune chaud
    if (family == 4) return vec3(0.78, 0.90, 1.00);  // lanterne aquatique, bâton de l'End, grenouilles nacrées
    if (family == 5) return vec3(0.62, 1.00, 0.52);  // grenouillumière verte, lichen lumineux
    if (family == 6) return vec3(0.78, 0.45, 1.00);  // améthyste, obsidienne pleureuse
    if (family == 7) return vec3(1.00, 0.16, 0.08);  // redstone
    return vec3(1.00, 0.60, 0.30);                   // flamme : torche, lanterne, feu, citrouille, bougie
}

/** Poids de la couleur d'une lumière plus faible de gap (en niveau 0..1) que la plus forte : deux niveaux d'écart
 * la font presque disparaître, à égalité les deux couleurs se mêlent. */
float mixWeight(float gap) { return exp(-gap * 12.0); }

/** Famille de lumière d'un numéro de matière (block.properties) ; 0 pour une flamme ou un bloc inconnu. */
int lightFamily(int mat) {
    return mat >= 16 && mat <= 23 ? mat - 15 : 0;
}

#if defined FSH && COLORED_LIGHT > 0
uniform sampler3D lightTexA;
uniform sampler3D lightTexB;
uniform int frameCounter;
#define HAS_FRAME_COUNTER
uniform int heldItemId;
uniform int heldItemId2;
uniform int heldBlockLightValue;
uniform int heldBlockLightValue2;

/** La lumière répandue autour d'un point : teinte (rgb, la plus forte composante à 1) et niveau (a, 0..1). */
vec4 voxelLight(vec3 pos, vec3 normal) {
    vec3 p = pos + normal * 0.55 + fract(cameraPosition);
    vec3 uvw = (p + vec3(VOX_HALF)) / vec3(VOX_SIZE);
    if (any(lessThan(uvw, vec3(0.0))) || any(greaterThan(uvw, vec3(1.0)))) return vec4(0.0);
    // shadowcomp écrit B les images paires, A les impaires.
    vec4 v = (frameCounter & 1) == 0 ? texture(lightTexB, uvw) : texture(lightTexA, uvw);
    // Vers le bord du volume, la teinte s'efface : pas de couture quand on s'éloigne.
    vec3 edge = min(uvw, 1.0 - uvw) * vec3(VOX_SIZE);
    v.a *= smoothstep(2.0, 10.0, min(edge.x, min(edge.y, edge.z)));
    return v;
}

/**
 * D'où vient la lumière des blocs autour d'un point : le sens où son niveau monte (non unitaire, 0 si elle vient de
 * partout ou de nulle part). Lu de part et d'autre du point dans le volume, sur les trois axes.
 */
vec3 voxelLightSlope(vec3 pos, vec3 normal) {
    vec3 p = pos + normal * 0.55 + fract(cameraPosition);
    vec3 uvw = (p + vec3(VOX_HALF)) / vec3(VOX_SIZE);
    if (any(lessThan(uvw, vec3(0.02))) || any(greaterThan(uvw, vec3(0.98)))) return vec3(0.0);
    vec3 e = 0.75 / vec3(VOX_SIZE);
    bool b = (frameCounter & 1) == 0;
    #define LV(o) (b ? texture(lightTexB, uvw + (o)).a : texture(lightTexA, uvw + (o)).a)
    vec3 g = vec3(LV(vec3(e.x, 0.0, 0.0)) - LV(vec3(-e.x, 0.0, 0.0)),
                  LV(vec3(0.0, e.y, 0.0)) - LV(vec3(0.0, -e.y, 0.0)),
                  LV(vec3(0.0, 0.0, e.z)) - LV(vec3(0.0, 0.0, -e.z)));
    #undef LV
    return g * 15.0 / 1.5;
}

/** Couleur d'un objet tenu en main (item.properties : mêmes numéros que les blocs). */
vec3 heldTint(int id) { return lightTint(id >= 10016 && id <= 10023 ? id - 10015 : 0); }

/**
 * Couleur de la lumière des blocs en un point. lmx : lumière des blocs du jeu (0..1), relevée là où ce que tient le
 * joueur éclaire plus que le jeu (lumière tenue en main sans mod de lumière dynamique). La couleur vient de la plus
 * forte des lumières proches, volume ou main ; à force égale elles se mêlent.
 */
vec3 blockLightTint(vec3 pos, vec3 normal, inout float lmx) {
    // La main est un peu sous les yeux ; sa lumière porte un peu plus loin que celle d'un bloc posé.
    float d = length(pos + vec3(0.0, 0.45, 0.0));
    float h1 = max(float(heldBlockLightValue) - 0.75 * d, 0.0) / 15.0;
    float h2 = max(float(heldBlockLightValue2) - 0.75 * d, 0.0) / 15.0;
    lmx = max(lmx, max(h1, h2) * 0.92);
    vec4 v = voxelLight(pos, normal);
    float top = max(v.a, max(h1, h2));
    if (top <= 0.0) return lightTint(0);
    vec3 c = v.rgb * mixWeight(top - v.a) * step(0.001, v.a);
    c += heldTint(heldItemId) * mixWeight(top - h1) * step(0.001, h1);
    c += heldTint(heldItemId2) * mixWeight(top - h2) * step(0.001, h2);
    float peak = max(c.r, max(c.g, c.b));
    return peak > 1e-5 ? c / peak : lightTint(0);
}
#else
vec3 blockLightTint(vec3 pos, vec3 normal, inout float lmx) { return lightTint(0); }
vec3 voxelLightSlope(vec3 pos, vec3 normal) { return vec3(0.0); }
#endif
