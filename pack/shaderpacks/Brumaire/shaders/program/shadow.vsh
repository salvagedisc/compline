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
flat out int vGlass;
#ifdef VOXELS
in vec4 at_midBlock;
layout(r32ui) uniform writeonly uimage3D voxelImg;
uniform int renderStage;
#if VOXEL_REFLECTIONS > 0
layout(r32ui) uniform writeonly uimage3D voxTopImg;
layout(r32ui) uniform writeonly uimage3D voxSideImg;
uniform sampler2D gtexture;

/** La couleur moyenne de la face (le dernier niveau de réduction de sa texture), teinte du biome comprise. */
void storeColor(ivec3 c) {
    vec3 col = textureLod(gtexture, mc_midTexCoord, 4.0).rgb * gl_Color.rgb;
    uvec3 q = uvec3(clamp(col, 0.0, 1.0) * 255.0 + 0.5);
    uint word = q.r | (q.g << 8) | (q.b << 16) | (1u << 24);
    if (gl_Normal.y > 0.5) imageStore(voxTopImg, c, uvec4(word));
    else imageStore(voxSideImg, c, uvec4(word));
}
#endif

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
    bool open = (mat >= 1 && mat <= 3) || mat == 5 || mat == 8 || mat == 16 || mat == 22;
    if (!open) code |= VOX_RAIN;
    if (mat == 6) code |= VOX_WATER;
    if (mat == 13) code |= VOX_GLASS;
#if VOXEL_REFLECTIONS > 0
    // Ce qui se voit dans un reflet : pas l'eau ni les petites plantes, trop fines pour un bloc entier.
    if (!open && mat != 6) {
        code |= VOX_MIRROR;
        storeColor(c);
    }
#endif
    imageStore(voxelImg, c, uvec4(code));
}
#endif

void main() {
    vUv = (gl_TextureMatrix[0] * gl_MultiTexCoord0).xy;
    vec4 view = gl_ModelViewMatrix * gl_Vertex;
    int mat = int(mc_Entity.x + 0.5) - 10000;
    // Le numéro porte aussi la matière (au-delà de 32) : seule la famille compte ici.
    if (mat > 0) mat %= 32;
    // L'eau ne porte pas d'ombre (shadowtex1 l'ignore) mais se marque : le fond saura qu'il est sous l'eau.
    // Les plantes ondulent comme dans la vue.
    vWater = mat == 6 ? 1 : 0;
    vGlass = mat == 13 ? 1 : 0;
#ifdef VOXELS
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
