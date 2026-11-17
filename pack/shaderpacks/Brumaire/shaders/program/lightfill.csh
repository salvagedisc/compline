// La lumière colorée se répand d'une case par image dans le volume (lib/voxel.glsl), comme celle du jeu : chaque
// case garde le niveau de sa plus forte voisine moins un (alpha), et prend la couleur de ses voisines, pesée selon
// leur niveau : la lumière la plus forte donne sa couleur, deux lumières de même force se mêlent. Les blocs pleins
// l'arrêtent, les blocs lumineux la relancent. Le volume suit la caméra bloc par bloc.
#include "/lib/settings.glsl"
#include "/lib/voxel.glsl"

layout(local_size_x = 8, local_size_y = 8, local_size_z = 8) in;
const ivec3 workGroups = ivec3(16, 8, 16);

layout(r32ui) uniform readonly uimage3D voxelImg;
layout(rgba16f) uniform image3D lightImgA;
layout(rgba16f) uniform image3D lightImgB;
uniform vec3 cameraPosition;
uniform vec3 previousCameraPosition;
uniform int frameCounter;


vec4 previous(ivec3 c) {
    if (!voxelInside(c)) return vec4(0.0);
    return (frameCounter & 1) == 0 ? imageLoad(lightImgA, c) : imageLoad(lightImgB, c);
}

void main() {
    ivec3 c = ivec3(gl_GlobalInvocationID);
    if (!voxelInside(c)) return;
    // La même case du monde, dans le volume de l'image précédente.
    ivec3 shift = ivec3(floor(cameraPosition)) - ivec3(floor(previousCameraPosition));
    ivec3 p = c + shift;
    uint code = imageLoad(voxelImg, c).r;
    vec4 light = vec4(0.0);
    if ((code & VOX_SOLID) == 0u || (code & 15u) > 0u) {
        vec4 n[6] = vec4[6](previous(p + ivec3(1, 0, 0)), previous(p - ivec3(1, 0, 0)), previous(p + ivec3(0, 1, 0)),
                            previous(p - ivec3(0, 1, 0)), previous(p + ivec3(0, 0, 1)), previous(p - ivec3(0, 0, 1)));
        float top = max(max(max(n[0].a, n[1].a), max(n[2].a, n[3].a)), max(n[4].a, n[5].a));
        vec3 hue = vec3(0.0);
        for (int i = 0; i < 6; i++) hue += n[i].rgb * mixWeight(top - n[i].a);
        hue /= max(max(hue.r, max(hue.g, hue.b)), 1e-6);
        light = vec4(top > 0.0 ? hue : vec3(0.0), max(top - 1.0 / 15.0, 0.0));
        uint level = code & 15u;
        if (float(level) / 15.0 >= light.a && level > 0u) light = vec4(lightTint(int((code >> 4) & 15u)), float(level) / 15.0);
    }
    if ((frameCounter & 1) == 0) imageStore(lightImgB, c, light);
    else imageStore(lightImgA, c, light);
}
