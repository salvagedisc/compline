// Carte d'ombre. shadowcolor0 : 1 là où c'est l'eau que la lumière de l'astre touche en premier. shadowcolor1 : la
// couleur que prend cette lumière en passant le vitrail qu'elle touche en premier (blanc sans vitrail).
#include "/lib/settings.glsl"

uniform sampler2D gtexture;
in vec2 vUv;
flat in int vWater;
flat in int vGlass;

/*
const int shadowcolor0Format = R8;
const int shadowcolor1Format = RGBA8;
*/
const vec4 shadowcolor0ClearColor = vec4(0.0, 0.0, 0.0, 0.0);
const vec4 shadowcolor1ClearColor = vec4(1.0, 1.0, 1.0, 1.0);

/* RENDERTARGETS: 0,1 */
layout(location = 0) out vec4 outWater;
layout(location = 1) out vec4 outTint;

void main() {
    outTint = vec4(1.0);
    if (vWater == 1) {
        outWater = vec4(1.0, 0.0, 0.0, 1.0);
        return;
    }
    vec4 tex = texture(gtexture, vUv);
    if (tex.a < 0.1) discard;
    outWater = vec4(0.0, 0.0, 0.0, 1.0);
#if GLASS_TINT > 0
    if (vGlass == 1 && tex.a < 0.99) {
        // Un verre teinté laisse passer sa couleur, d'autant plus franche qu'il est opaque ; un verre sombre
        // (teinté, noir) retient presque tout.
        vec3 lin = pow(tex.rgb, vec3(2.2));
        float top = max(max(lin.r, lin.g), max(lin.b, 1e-4));
        vec3 pass = lin / top * clamp(top * 2.5, 0.08, 0.9);
        outTint = vec4(mix(vec3(1.0), pass, clamp(tex.a * 1.8, 0.0, 1.0)), 1.0);
    }
#endif
}
