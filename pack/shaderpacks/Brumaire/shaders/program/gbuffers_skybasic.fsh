// Le ciel est peint plus tard (deferred) : du ciel du jeu, seules les étoiles sont gardées.
uniform int renderStage;
in vec4 vTint;

/* RENDERTARGETS: 0 */
layout(location = 0) out vec4 outColor;

void main() {
    if (renderStage != MC_RENDER_STAGE_STARS) discard;
    outColor = vec4(pow(vTint.rgb, vec3(2.2)) * vTint.a * 1.6, 1.0);
}
