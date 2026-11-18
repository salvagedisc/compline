// L'image finie : halo des hautes lumières, exposition, courbe de tons, étalonnage. Le résultat, déjà en tons d'écran,
// passe par l'anticrénelage (smaa.fsh) avant l'écran.
#include "/lib/settings.glsl"

/*
const int colortex0Format = R11F_G11F_B10F;
const int colortex7Format = RGBA8;
*/
const vec4 colortex0ClearColor = vec4(0.0, 0.0, 0.0, 1.0);
#ifdef BLOOM
const bool colortex0MipmapEnabled = true;
#endif
uniform float viewWidth;
uniform float viewHeight;
uniform sampler2D lensTex;
uniform sampler2D depthtex0;
uniform float near;
uniform float far;

uniform sampler2D colortex0;
uniform vec3 sunPosition;
uniform mat4 gbufferModelViewInverse;
uniform ivec2 eyeBrightnessSmooth;
in vec2 vUv;

/* RENDERTARGETS: 7 */
layout(location = 0) out vec4 outColor;

const vec3 LUMA = vec3(0.2126, 0.7152, 0.0722);
#define TONE_HUE_NORM 0.5

/** Courbe de tons : une épaule douce pour les hautes lumières, un pied qui garde du noir. */
float tone(float x) {
    return clamp(x * (2.51 * x + 0.03) / (x * (2.43 * x + 0.59) + 0.14), 0.0, 1.0);
}

/**
 * La courbe appliquée à l'intensité plutôt qu'à chaque canal : un sable ou une neige au soleil gardent leur teinte
 * au lieu de blanchir. L'intensité mêle luminance et canal le plus fort, pour qu'un rouge vif ne déborde pas ;
 * seul ce qui dépasse encore le blanc pâlit, comme une pellicule qui sature.
 */
vec3 toneColor(vec3 x) {
    float n = max(mix(dot(x, LUMA), max(x.r, max(x.g, x.b)), TONE_HUE_NORM), 1e-5);
    vec3 c = x * (tone(n) / n);
    float over = max(c.r, max(c.g, c.b));
    if (over > 1.0) c = mix(c / over, vec3(1.0), clamp((over - 1.0) * 0.5, 0.0, 1.0));
    return clamp(c, 0.0, 1.0);
}

float sq(float x) { return x * x; }

/** Distance (blocs) d'un point de l'écran ; -1 pour la main. */
float viewDistance(vec2 uv) {
    float d = texture(depthtex0, uv).r;
    if (d < 0.56) return -1.0;
    if (d >= 1.0) return far * 4.0;
    float z = d * 2.0 - 1.0;
    return 2.0 * near * far / (far + near - z * (far - near));
}

#if DOF > 0
/**
 * La profondeur de champ : ce qui est loin du point devient flou comme dans un objectif, d'un disque dont la taille
 * suit l'écart entre l'inverse des distances. Vingt-quatre prises en spirale ; chacune ne compte que si elle est elle-
 * même assez floue pour s'étendre jusqu'ici, sans quoi un premier plan net bavait sur le fond. La main reste nette.
 */
vec3 depthOfField(vec3 color, float focus) {
    float d = viewDistance(vUv);
    if (d < 0.0) return color;
    float aperture = DOF == 1 ? 18.0 : 42.0;
    float maxR = DOF == 1 ? 6.0 : 12.0;
    float scale = viewHeight / 720.0;
    float coc = min(abs(1.0 / focus - 1.0 / d) * aperture * scale, maxR * scale);
    if (coc < 0.6) return color;
    vec2 pixel = 1.0 / vec2(viewWidth, viewHeight);
    vec3 sum = color;
    float total = 1.0;
    float turn = fract(52.9829189 * fract(dot(gl_FragCoord.xy, vec2(0.06711056, 0.00583715)))) * 6.2831853;
    for (int i = 0; i < 24; i++) {
        float r = sqrt((float(i) + 0.5) / 24.0) * coc;
        float a = float(i) * 2.3999632 + turn;
        vec2 uv = vUv + vec2(cos(a), sin(a)) * r * pixel;
        float sd = viewDistance(uv);
        if (sd < 0.0) continue;
        float scoc = min(abs(1.0 / focus - 1.0 / sd) * aperture * scale, maxR * scale);
        // Une prise plus proche que le point compte selon son propre flou ; plus loin, elle ne peut pas déborder sur
        // ce qui est devant elle.
        float w = clamp(min(scoc, sd < d ? scoc : coc) - r + 1.0, 0.0, 1.0);
        sum += texture(colortex0, uv).rgb * w;
        total += w;
    }
    return sum / total;
}
#endif

#ifdef BLOOM
/** Halo des hautes lumières : l'image relue dans ses réductions successives, sans passe de flou à part. */
vec3 glow(float threshold) {
    vec3 sum = vec3(0.0);
    vec2 pixel = 1.0 / vec2(viewWidth, viewHeight);
    float weight = 0.40;
    // Du plus serré au plus large : un cœur net autour d'une source vive, puis une traîne qui s'étend loin.
    for (int level = 2; level <= 6; level++) {
        vec2 step_ = pixel * exp2(float(level)) * 0.75;
        vec3 c = textureLod(colortex0, vUv, float(level)).rgb * 0.5;
        c += textureLod(colortex0, vUv + step_ * vec2(1.0, 1.0), float(level)).rgb * 0.125;
        c += textureLod(colortex0, vUv + step_ * vec2(-1.0, 1.0), float(level)).rgb * 0.125;
        c += textureLod(colortex0, vUv + step_ * vec2(1.0, -1.0), float(level)).rgb * 0.125;
        c += textureLod(colortex0, vUv + step_ * vec2(-1.0, -1.0), float(level)).rgb * 0.125;
        // Un seuil au genou doux : ce qui approche du seuil luit déjà un peu, sans marche visible.
        float l = max(c.r, max(c.g, c.b));
        float knee = 0.6;
        float soft = sq(clamp(l - threshold + knee, 0.0, 2.0 * knee)) / (4.0 * knee);
        sum += c * (max(soft, l - threshold) / max(l, 1e-4)) * weight;
        weight *= 0.72;
    }
    return sum;
}
#endif

void main() {
    vec3 color = texture(colortex0, vUv).rgb;
    vec4 lens = texelFetch(lensTex, ivec2(0), 0);
#if DOF > 0
    color = depthOfField(color, max(lens.y, 0.5));
#endif
    // L'œil s'habitue : la nuit et sous terre, l'image s'éclaircit un peu.
    float sunUp = normalize(mat3(gbufferModelViewInverse) * sunPosition).y;
    float adapt = mix(2.2, 1.0, smoothstep(-0.18, 0.12, sunUp));
    adapt *= mix(1.5, 1.0, smoothstep(0.0, 0.5, float(eyeBrightnessSmooth.y) / 240.0));
    float exposure = 0.62 * EXPOSURE * adapt;
#if EXPOSURE_AUTO > 0
    // Et à ce qu'il regarde : une scène sombre s'ouvre, une scène éblouissante se ferme, à moitié seulement, comme un
    // œil et pas comme une caméra de surveillance, et en prenant son temps (lens.csh).
    float seen = max(lens.x, 1e-5) * exposure;
    exposure *= clamp(pow(0.10 / seen, 0.3 * EXPOSURE_AUTO_AMOUNT), 0.75, 1.25);
#endif
#ifdef BLOOM
    // Le seuil du halo suit l'exposition : ce qui éblouit l'œil, pas une valeur fixe.
    color += glow(1.5 * 0.62 * EXPOSURE * adapt / exposure) * (0.5 * BLOOM_STRENGTH);
#endif
    color *= exposure;
#if PURKINJE > 0
    // Dans le noir, l'œil voit avec ses bâtonnets : les couleurs s'éteignent et l'image tire vers le bleu.
    float dark = 1.0 - smoothstep(0.004, 0.05, dot(color, LUMA));
    vec3 rod = vec3(0.62, 0.74, 1.0) * dot(color, vec3(0.08, 0.58, 0.34));
    color = mix(color, rod, dark * 0.55);
#endif
#if VIGNETTE > 0
    // L'objectif assombrit un peu les coins.
    vec2 q = (vUv - 0.5) * vec2(viewWidth / viewHeight, 1.0);
    color *= 1.0 - 0.22 * sq(sq(clamp(length(q) * 1.15, 0.0, 1.0)));
#endif
    color = toneColor(color);
    float l = dot(color, LUMA);
    // Ombres un peu froides, lumières un peu chaudes.
    color *= mix(vec3(0.99, 1.0, 1.01), vec3(1.04, 1.0, 0.93), smoothstep(0.05, 0.75, l));
    // Les ombres gardent leur couleur : une pellicule, pas une vidéo délavée.
    color = mix(vec3(l), color, SATURATION * mix(1.3, 1.08, smoothstep(0.04, 0.45, l)));
    color = (color - 0.5) * CONTRAST + 0.5;
    // Une image mate : les noirs à peine levés, les blancs à peine retenus.
    color = color * 0.96 + 0.016;
    color = pow(max(color, 0.0), vec3(1.0 / 2.2));
    color += (fract(52.9829189 * fract(dot(gl_FragCoord.xy, vec2(0.06711056, 0.00583715)))) - 0.5) / 255.0;
    outColor = vec4(color, 1.0);
}
