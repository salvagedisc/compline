// Flou de mouvement : quand la caméra tourne ou file, chaque point s'étire le long du chemin qu'il a fait à l'écran
// depuis l'image d'avant, comme sur la pellicule d'une caméra dont l'obturateur reste ouvert une partie de l'image.
// Immobile, rien ne change ; la main ne se brouille jamais et ne se mêle pas au paysage qui défile derrière elle.
#include "/lib/settings.glsl"
#include "/lib/common.glsl"
#include "/lib/reproject.glsl"

uniform sampler2D colortex7;
uniform sampler2D depthtex0;
uniform int frameCounter;
in vec2 vUv;

/* RENDERTARGETS: 7 */
layout(location = 0) out vec4 outColor;

const int MOTION_SAMPLES = 12;

void main() {
    ivec2 pixel = ivec2(gl_FragCoord.xy);
    vec3 color = texelFetch(colortex7, pixel, 0).rgb;
#if MOTION_BLUR > 0
    vec2 size = vec2(viewWidth, viewHeight);
    float depth = texelFetch(depthtex0, pixel, 0).r;
    if (!isHand(depth)) {
        vec2 uv = gl_FragCoord.xy / size;
        // Le chemin parcouru pendant que l'obturateur est ouvert : un quart d'image par cran du réglage.
        vec2 move = (uv - previousUv(uv, depth)) * size * (float(MOTION_BLUR) * 0.25);
        float len = length(move);
        if (len > 0.5) {
            // Un demi-tour brusque ne doit pas tout effacer : la traînée ne dépasse pas 6 % de la hauteur de l'écran.
            move *= min(len, size.y * 0.06) / len;
            float start = dither(gl_FragCoord.xy + float(frameCounter & 63) * 5.588238);
            vec3 sum = color;
            float weight = 1.0;
            for (int i = 0; i < MOTION_SAMPLES; i++) {
                vec2 at = (gl_FragCoord.xy + move * ((float(i) + start) / float(MOTION_SAMPLES) - 0.5)) / size;
                if (any(lessThan(at, vec2(0.0))) || any(greaterThan(at, vec2(1.0)))) continue;
                if (isHand(texture(depthtex0, at).r)) continue;
                sum += texture(colortex7, at).rgb;
                weight += 1.0;
            }
            // Un tout petit mouvement (la marche, le balancement de la tête) ne floute qu'à peine.
            color = mix(color, sum / weight, smoothstep(0.5, 3.0, len));
        }
    }
#endif
    outColor = vec4(color, 1.0);
}
