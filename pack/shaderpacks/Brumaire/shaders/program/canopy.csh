// Le feuillage au-dessus de chaque colonne du volume (lib/voxel.glsl) : la hauteur du dessous du plus haut feuillage,
// en blocs au-dessus du bloc de la caméra (a, -1000 sans feuillage), et sa couleur moyenne (rgb, sRGB) quand les
// reflets hors écran la rangent. Relu par lib/leaffall.glsl (gouttes des arbres) et lib/watershade.glsl (feuilles sur l'eau).
#include "/lib/settings.glsl"
#include "/lib/voxel.glsl"

layout(local_size_x = 8, local_size_y = 8, local_size_z = 1) in;
const ivec3 workGroups = ivec3(16, 16, 1);

layout(r32ui) uniform readonly uimage3D voxelImg;
#if VOXEL_REFLECTIONS > 0
layout(r32ui) uniform readonly uimage3D voxSideImg;
#endif
layout(rgba16f) uniform writeonly image2D canopyImg;

void main() {
    ivec2 c = ivec2(gl_GlobalInvocationID.xy);
    vec4 result = vec4(0.30, 0.42, 0.16, -1000.0);
    bool inLeaves = false;
    for (int y = VOX_H - 1; y >= 0; y--) {
        uint v = imageLoad(voxelImg, ivec3(c.x, y, c.y)).r;
        bool leaf = (v & VOX_LEAF) != 0u;
        if (leaf && !inLeaves) {
            inLeaves = true;
#if VOXEL_REFLECTIONS > 0
            uint s = imageLoad(voxSideImg, ivec3(c.x, y, c.y)).r;
            if ((s >> 24) != 0u) result.rgb = vec3(float(s & 255u), float((s >> 8) & 255u), float((s >> 16) & 255u)) / 255.0;
#endif
        }
        // Le dessous du feuillage : la première case sans feuille sous lui.
        if (inLeaves && !leaf) {
            result.a = float(y - VOX_HALF.y + 1);
            break;
        }
        // Un toit plein au-dessus d'un arbre : on garde le premier feuillage trouvé seulement.
        if (!inLeaves && (v & VOX_SOLID) != 0u) break;
    }
    imageStore(canopyImg, c, result);
}
