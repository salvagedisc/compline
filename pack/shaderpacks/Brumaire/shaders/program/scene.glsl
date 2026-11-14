// Position d'un pixel de l'écran : terrain du jeu, ou paysage lointain de Distant Horizons.

uniform sampler2D depthtex0;
#ifdef DISTANT_HORIZONS
uniform sampler2D dhDepthTex0;
uniform mat4 dhProjectionInverse;
#endif

/** Rend la position (repère joueur, caméra à l'origine du regard) ; sky : rien n'est dessiné là ; hand : la main. */
vec3 scenePosition(vec2 uv, out bool sky, out bool hand) {
    float depth = texture(depthtex0, uv).r;
    hand = depth < 0.56;
    sky = depth >= 1.0;
    vec4 view = gbufferProjectionInverse * vec4(vec3(uv, depth) * 2.0 - 1.0, 1.0);
#ifdef DISTANT_HORIZONS
    if (sky) {
        float distant = texture(dhDepthTex0, uv).r;
        sky = distant >= 1.0;
        view = dhProjectionInverse * vec4(vec3(uv, distant) * 2.0 - 1.0, 1.0);
    }
#endif
    return mat3(gbufferModelViewInverse) * (view.xyz / view.w);
}
