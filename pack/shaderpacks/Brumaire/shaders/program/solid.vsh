// Sommets de toute la géométrie éclairée : terrain, eau, entités, blocs-entités, main, particules.
#include "/lib/settings.glsl"
#include "/lib/common.glsl"
#include "/lib/taa.glsl"
#ifdef FAR_RECORD
#include "/lib/farshadow.glsl"
#endif

#ifdef BUSHY_PASS
// Le feuillage touffu (program/bushy.gsh) : les sorties passent par le shader de géométrie, qui les rend sous leur nom.
#define vUv gUv
#define vLm gLm
#define vTint gTint
#define vNormal gNormal
#define vPos gPos
#define vMat gMat
#define vSurf gSurf
#define vBend gBend
#define vTangent gTangent
#define vSpriteMid gSpriteMid
#define vSpriteHalf gSpriteHalf
#endif

#if defined PROGRAM_TERRAIN || defined PROGRAM_WATER
#define HAS_BLOCK_ID
#include "/lib/wind.glsl"
in vec2 mc_Entity;
in vec2 mc_midTexCoord;
#if defined PROGRAM_TERRAIN && RELIEF > 0
in vec4 at_tangent;
out vec4 vTangent;
out vec2 vSpriteMid;
out vec2 vSpriteHalf;
#endif
#elif defined PROGRAM_WEATHER
#include "/lib/wind.glsl"
#endif

out vec2 vUv;
out vec2 vLm;
out vec4 vTint;
out vec3 vNormal;
out vec3 vPos;
flat out int vMat;
// La matière du bloc (lib/material.glsl) : le numéro de block.properties divisé par 32.
flat out int vSurf;
#if defined PROGRAM_TERRAIN
out vec2 vBend;
#endif

void main() {
    vUv = (gl_TextureMatrix[0] * gl_MultiTexCoord0).xy;
    vLm = lightCoord((gl_TextureMatrix[1] * gl_MultiTexCoord1).xy);
    vTint = gl_Color;
    vNormal = mat3(gbufferModelViewInverse) * (gl_NormalMatrix * gl_Normal);
    vec4 view = gl_ModelViewMatrix * gl_Vertex;
    vPos = (gbufferModelViewInverse * view).xyz;
    vMat = 0;
    vSurf = 0;
#ifdef HAS_BLOCK_ID
    int id = int(mc_Entity.x + 0.5) - 10000;
    vMat = id > 0 ? id % 32 : 0;
    vSurf = id >= 32 ? id / 32 : 0;
#if defined FAR_RECORD && FAR_SHADOWS > 0
    // Le relief pour les ombres lointaines : les plantes et les lianes n'arrêtent pas le soleil de loin.
    if (!(vMat >= 1 && vMat <= 3) && vMat != 5) farRecord(vPos + cameraPosition);
#endif
#if defined PROGRAM_TERRAIN && RELIEF > 0
    // Le repère de la face (pour le relief) et le rectangle de sa texture dans l'atlas : chaque coin est à la même
    // distance du centre, la demi-taille reste donc la même sur toute la face.
    vTangent = vec4(mat3(gbufferModelViewInverse) * (gl_NormalMatrix * at_tangent.xyz), at_tangent.w);
    vSpriteMid = mc_midTexCoord;
    vSpriteHalf = abs(vUv - mc_midTexCoord);
#endif
#if defined PROGRAM_TERRAIN
    vBend = vec2(0.0);
#endif
    if (vMat >= 1 && vMat <= 5) {
        bool top = gl_MultiTexCoord0.t < mc_midTexCoord.t;
        vec3 sway = windVertex(vMat, top, vPos + cameraPosition, vPos, vLm.y);
        vPos += sway;
        view = gbufferModelView * vec4(vPos, 1.0);
#if defined PROGRAM_TERRAIN
        // Combien ce sommet est couché par le vent ou les pas, rapporté à ce que sa plante peut plier : les brins
        // couchés montrent leur revers plus clair (solid.fsh).
        vBend.x = clamp(length(sway.xz) / (vMat == 4 ? 0.07 : 0.22), 0.0, 1.0);
        // La hauteur dans la touffe (0 au sol, 1 en haut d'une herbe, 2 en haut d'une grande herbe) : le pied des
        // plantes s'assombrit (solid.fsh).
        vBend.y = (top ? 1.0 : 0.0) + (vMat == 3 ? 1.0 : 0.0);
#endif
    }
#endif
#ifdef PROGRAM_WEATHER
    // La pluie et la neige penchent avec le vent : le haut des rideaux part vers l'amont.
    vec2 dir;
    float force, turbulence;
    windAt(vPos, dir, force, turbulence);
    vPos.xz -= dir * force * 0.35 * vPos.y;
    view = gbufferModelView * vec4(vPos, 1.0);
#endif
    gl_Position = taaJitter(gl_ProjectionMatrix * view);
}
