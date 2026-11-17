// L'abri de la pluie : pour chaque colonne du volume (lib/voxel.glsl), le haut du plus haut bloc qui arrête la
// pluie, en blocs au-dessus du bloc de la caméra. Relu par lib/precip.glsl.
#include "/lib/settings.glsl"
#include "/lib/voxel.glsl"

layout(local_size_x = 8, local_size_y = 8, local_size_z = 1) in;
const ivec3 workGroups = ivec3(16, 16, 1);

layout(r32ui) uniform readonly uimage3D voxelImg;
layout(r16f) uniform writeonly image2D shelterImg;

void main() {
    ivec2 c = ivec2(gl_GlobalInvocationID.xy);
    float top = -1000.0;
    for (int y = VOX_H - 1; y >= 0; y--) {
        if ((imageLoad(voxelImg, ivec3(c.x, y, c.y)).r & VOX_RAIN) != 0u) {
            top = float(y - VOX_HALF.y + 1);
            break;
        }
    }
    imageStore(shelterImg, c, vec4(top));
}
