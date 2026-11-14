#include "/lib/settings.glsl"
#include "/lib/shadow.glsl"
out vec2 vUv;

void main() {
    vUv = (gl_TextureMatrix[0] * gl_MultiTexCoord0).xy;
    vec4 clip = ftransform();
    gl_Position = vec4(shadowWarp(clip.xyz), clip.w);
}
