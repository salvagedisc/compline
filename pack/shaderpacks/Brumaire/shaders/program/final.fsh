// Mise à l'écran : exposition, courbe de tons, étalonnage.
#include "/lib/settings.glsl"

/*
const int colortex0Format = R11F_G11F_B10F;
*/
const vec4 colortex0ClearColor = vec4(0.0, 0.0, 0.0, 1.0);
#ifdef BLOOM
const bool colortex0MipmapEnabled = true;
uniform float viewWidth;
uniform float viewHeight;
#endif

uniform sampler2D colortex0;
uniform vec3 sunPosition;
uniform mat4 gbufferModelViewInverse;
uniform ivec2 eyeBrightnessSmooth;
in vec2 vUv;

layout(location = 0) out vec4 outColor;

const vec3 LUMA = vec3(0.2126, 0.7152, 0.0722);

/** Courbe de tons : une épaule douce pour les hautes lumières, un pied qui garde du noir. */
vec3 tone(vec3 x) {
    return clamp(x * (2.51 * x + 0.03) / (x * (2.43 * x + 0.59) + 0.14), 0.0, 1.0);
}

#ifdef BLOOM
/** Halo des hautes lumières : l'image relue dans ses réductions successives, sans passe de flou à part. */
vec3 glow() {
    vec3 sum = vec3(0.0);
    vec2 pixel = 1.0 / vec2(viewWidth, viewHeight);
    float weight = 0.40;
    for (int level = 2; level <= 5; level++) {
        vec2 step_ = pixel * exp2(float(level)) * 0.75;
        vec3 c = textureLod(colortex0, vUv, float(level)).rgb * 0.5;
        c += textureLod(colortex0, vUv + step_ * vec2(1.0, 1.0), float(level)).rgb * 0.125;
        c += textureLod(colortex0, vUv + step_ * vec2(-1.0, 1.0), float(level)).rgb * 0.125;
        c += textureLod(colortex0, vUv + step_ * vec2(1.0, -1.0), float(level)).rgb * 0.125;
        c += textureLod(colortex0, vUv + step_ * vec2(-1.0, -1.0), float(level)).rgb * 0.125;
        sum += max(c - 1.5, 0.0) * weight;
        weight *= 0.75;
    }
    return sum;
}
#endif

void main() {
    vec3 color = texture(colortex0, vUv).rgb;
#ifdef BLOOM
    color += glow() * (0.5 * BLOOM_STRENGTH);
#endif
    // L'œil s'habitue : la nuit et sous terre, l'image s'éclaircit un peu.
    float sunUp = normalize(mat3(gbufferModelViewInverse) * sunPosition).y;
    float adapt = mix(2.2, 1.0, smoothstep(-0.18, 0.12, sunUp));
    adapt *= mix(1.5, 1.0, smoothstep(0.0, 0.5, float(eyeBrightnessSmooth.y) / 240.0));
    color *= 0.62 * EXPOSURE * adapt;
    color = tone(color);
    float l = dot(color, LUMA);
    // Ombres un peu froides, lumières un peu chaudes.
    color *= mix(vec3(0.96, 1.0, 1.04), vec3(1.03, 1.0, 0.95), smoothstep(0.05, 0.75, l));
    color = mix(vec3(l), color, SATURATION * 0.88);
    color = (color - 0.5) * CONTRAST + 0.5;
    // Une image mate : les noirs à peine levés, les blancs à peine retenus.
    color = color * 0.95 + 0.022;
    color = pow(max(color, 0.0), vec3(1.0 / 2.2));
    color += (fract(52.9829189 * fract(dot(gl_FragCoord.xy, vec2(0.06711056, 0.00583715)))) - 0.5) / 255.0;
    outColor = vec4(color, 1.0);
}
