// Les rives : pour chaque colonne du volume (lib/voxel.glsl), la plus haute eau à deux blocs au plus,
// son écart (xy, en blocs) et le haut de son eau (z, en blocs au-dessus du bloc de la caméra), w = 1 s'il y en a une.
// Relu par lib/shore.glsl pour le sol mouillé au bord de l'eau.
#include "/lib/settings.glsl"
#include "/lib/voxel.glsl"

layout(local_size_x = 8, local_size_y = 8, local_size_z = 1) in;
const ivec3 workGroups = ivec3(16, 16, 1);

layout(r32ui) uniform readonly uimage3D voxelImg;
layout(rgba16f) uniform writeonly image2D shoreImg;

// Le haut de l'eau des colonnes du groupe et de leur bord de deux colonnes.
shared float surfaceTop[12][12];

/** Le haut de la plus haute eau à l'air libre d'une colonne (rien de plein juste au-dessus), ou -1000 sans eau. */
float surfaceOf(ivec2 c) {
    if (any(lessThan(c, ivec2(0))) || any(greaterThanEqual(c, ivec2(VOX_W)))) return -1000.0;
    bool solidAbove = false;
    for (int y = VOX_H - 1; y >= 0; y--) {
        uint v = imageLoad(voxelImg, ivec3(c.x, y, c.y)).r;
        // Une eau pleine s'arrête à 8/9 de son bloc. Une eau sous un bloc plein (une nappe enfouie) ne mouille rien.
        if ((v & VOX_WATER) != 0u && !solidAbove) return float(y - VOX_HALF.y) + 0.89;
        solidAbove = (v & VOX_SOLID) != 0u && (v & VOX_WATER) == 0u;
    }
    return -1000.0;
}

void main() {
    ivec2 base = ivec2(gl_WorkGroupID.xy) * 8 - 2;
    for (int i = int(gl_LocalInvocationIndex); i < 144; i += 64) {
        ivec2 l = ivec2(i % 12, i / 12);
        surfaceTop[l.x][l.y] = surfaceOf(base + l);
    }
    barrier();
    ivec2 me = ivec2(gl_LocalInvocationID.xy) + 2;
    // La plus haute eau des environs, la plus proche à hauteur égale : une eau de grotte sous la rive ne compte pas
    // quand un lac la borde.
    vec4 result = vec4(0.0, 0.0, -1000.0, 0.0);
    float best = -1e9;
    for (int dz = -2; dz <= 2; dz++) {
        for (int dx = -2; dx <= 2; dx++) {
            float s = surfaceTop[me.x + dx][me.y + dz];
            if (s < -999.0) continue;
            float score = s - 0.01 * float(dx * dx + dz * dz);
            if (score <= best) continue;
            best = score;
            result = vec4(float(dx), float(dz), s, 1.0);
        }
    }
    imageStore(shoreImg, ivec2(gl_GlobalInvocationID.xy), result);
}
