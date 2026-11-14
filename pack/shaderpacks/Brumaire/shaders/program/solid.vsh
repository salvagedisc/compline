// Sommets de toute la géométrie éclairée : terrain, eau, entités, blocs-entités, main, particules.
#include "/lib/settings.glsl"
#include "/lib/common.glsl"

#if defined PROGRAM_TERRAIN || defined PROGRAM_WATER
#define HAS_BLOCK_ID
#include "/lib/wind.glsl"
in vec2 mc_Entity;
in vec2 mc_midTexCoord;
#endif

out vec2 vUv;
out vec2 vLm;
out vec4 vTint;
out vec3 vNormal;
out vec3 vPos;
flat out int vMat;

void main() {
    vUv = (gl_TextureMatrix[0] * gl_MultiTexCoord0).xy;
    vLm = lightCoord((gl_TextureMatrix[1] * gl_MultiTexCoord1).xy);
    vTint = gl_Color;
    vNormal = mat3(gbufferModelViewInverse) * (gl_NormalMatrix * gl_Normal);
    vec4 view = gl_ModelViewMatrix * gl_Vertex;
    vPos = (gbufferModelViewInverse * view).xyz;
    vMat = 0;
#ifdef HAS_BLOCK_ID
    int id = int(mc_Entity.x + 0.5) - 10000;
    vMat = id > 0 && id < 32 ? id : 0;
    if (vMat >= 1 && vMat <= 5) {
        bool top = gl_MultiTexCoord0.t < mc_midTexCoord.t;
        vPos += windVertex(vMat, top, vPos + cameraPosition, vPos, vLm.y);
        view = gbufferModelView * vec4(vPos, 1.0);
    }
#endif
    gl_Position = gl_ProjectionMatrix * view;
}
