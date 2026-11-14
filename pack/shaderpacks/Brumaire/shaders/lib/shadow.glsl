// Carte d'ombre : déformation et lecture filtrée.

uniform mat4 shadowModelView;
uniform mat4 shadowProjection;

float shadowBend(vec2 p) { return mix(1.0, length(p), SHADOW_BEND); }

vec3 shadowWarp(vec3 clip) {
    clip.xy /= shadowBend(clip.xy);
    clip.z *= SHADOW_DEPTH;
    return clip;
}

#ifdef FSH
uniform sampler2DShadow shadowtex1;

/** Part de lumière directe reçue en un point (repère joueur), 0 à l'ombre. */
float shadowAt(vec3 pos, vec3 normal, float noise, int taps) {
    vec3 clip = (shadowProjection * (shadowModelView * vec4(pos, 1.0))).xyz;
    float bend = shadowBend(clip.xy);
    // Le point est décalé le long de la normale d'à peu près un texel de la carte, vu du monde.
    float texel = 2.0 * shadowDistance / float(shadowMapResolution) * bend;
    clip = (shadowProjection * (shadowModelView * vec4(pos + normal * texel * 2.2, 1.0))).xyz;
    vec3 uv = shadowWarp(clip) * 0.5 + 0.5;
    if (uv.z >= 1.0) return 1.0;
    uv.z -= 0.00012;
    if (taps <= 1) return texture(shadowtex1, uv);
    float radius = 1.4 / float(shadowMapResolution);
    float turn = noise * 6.2831853;
    float sum = 0.0;
    for (int i = 0; i < taps; i++) {
        // Disque de Vogel : des prises bien réparties, tournées d'un pixel à l'autre.
        float r = sqrt((float(i) + 0.5) / float(taps));
        float a = float(i) * 2.3999632 + turn;
        sum += texture(shadowtex1, vec3(uv.xy + vec2(cos(a), sin(a)) * r * radius, uv.z));
    }
    return sum / float(taps);
}
#endif
