#include "/lib/settings.glsl"
#include "/lib/common.glsl"
#include "/lib/shadow.glsl"
#include "/lib/wind.glsl"

uniform mat4 shadowModelViewInverse;
in vec2 mc_Entity;
in vec2 mc_midTexCoord;
out vec2 vUv;
flat out int vWater;

void main() {
    vUv = (gl_TextureMatrix[0] * gl_MultiTexCoord0).xy;
    vec4 view = gl_ModelViewMatrix * gl_Vertex;
    int mat = int(mc_Entity.x + 0.5) - 10000;
    // L'eau ne porte pas d'ombre (shadowtex1 l'ignore) mais se marque : le fond saura qu'il est sous l'eau.
    // Les plantes ondulent comme dans la vue.
    vWater = mat == 6 ? 1 : 0;
    if (mat >= 1 && mat <= 5) {
        vec3 pos = (shadowModelViewInverse * view).xyz;
        vec2 lm = lightCoord((gl_TextureMatrix[1] * gl_MultiTexCoord1).xy);
        pos += windVertex(mat, gl_MultiTexCoord0.t < mc_midTexCoord.t, pos + cameraPosition, pos, lm.y);
        view = shadowModelView * vec4(pos, 1.0);
    }
    vec4 clip = gl_ProjectionMatrix * view;
    gl_Position = vec4(shadowWarp(clip.xyz), clip.w);
}
