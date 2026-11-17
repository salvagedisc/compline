#include "/lib/settings.glsl"
#include "/lib/common.glsl"
#include "/lib/shadow.glsl"
#include "/lib/wind.glsl"
#include "/lib/voxel.glsl"

uniform mat4 shadowModelViewInverse;
in vec2 mc_Entity;
in vec2 mc_midTexCoord;
out vec2 vUv;
flat out int vWater;
#if COLORED_LIGHT > 0
in vec4 at_midBlock;
layout(r32ui) uniform writeonly uimage3D voxelImg;
uniform int renderStage;

/** Range le bloc de ce sommet dans le volume de la lumière colorée : plein, laisse passer, ou lumineux. */
void storeVoxel(vec3 pos, int mat) {
    // Seul le terrain : un objet tenu ou posé porte lui aussi le décalage vers le centre de son bloc, mais sa
    // lumière passe par la main (lib/voxel.glsl) ou par le jeu.
    if (renderStage != MC_RENDER_STAGE_TERRAIN_SOLID && renderStage != MC_RENDER_STAGE_TERRAIN_CUTOUT
        && renderStage != MC_RENDER_STAGE_TERRAIN_CUTOUT_MIPPED && renderStage != MC_RENDER_STAGE_TERRAIN_TRANSLUCENT) return;
    if (dot(at_midBlock.xyz, at_midBlock.xyz) < 1.0) return;
    ivec3 c = voxelCell(pos + at_midBlock.xyz / 64.0, fract(cameraPosition));
    if (!voxelInside(c)) return;
    uint level = uint(clamp(at_midBlock.w + 0.5, 0.0, 15.0));
    bool clear = (mat >= 1 && mat <= 6) || mat == 10 || mat == 13 || mat == 14;
    uint code = level > 0u ? level | (uint(lightFamily(mat)) << 4) | VOX_CLEAR : (clear ? VOX_CLEAR : VOX_SOLID);
    imageStore(voxelImg, c, uvec4(code));
}
#endif

void main() {
    vUv = (gl_TextureMatrix[0] * gl_MultiTexCoord0).xy;
    vec4 view = gl_ModelViewMatrix * gl_Vertex;
    int mat = int(mc_Entity.x + 0.5) - 10000;
    // L'eau ne porte pas d'ombre (shadowtex1 l'ignore) mais se marque : le fond saura qu'il est sous l'eau.
    // Les plantes ondulent comme dans la vue.
    vWater = mat == 6 ? 1 : 0;
#if COLORED_LIGHT > 0
    storeVoxel((shadowModelViewInverse * view).xyz, mat);
#endif
    if (mat >= 1 && mat <= 5) {
        vec3 pos = (shadowModelViewInverse * view).xyz;
        vec2 lm = lightCoord((gl_TextureMatrix[1] * gl_MultiTexCoord1).xy);
        pos += windVertex(mat, gl_MultiTexCoord0.t < mc_midTexCoord.t, pos + cameraPosition, pos, lm.y);
        view = shadowModelView * vec4(pos, 1.0);
    }
    vec4 clip = gl_ProjectionMatrix * view;
    gl_Position = vec4(shadowWarp(clip.xyz), clip.w);
}
