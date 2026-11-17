// Fragments de toute la géométrie éclairée. Chaque programme d'Iris inclut ce fichier avec son PROGRAM_*.
#define FSH
#include "/lib/settings.glsl"
#include "/lib/common.glsl"
#include "/lib/shadow.glsl"
#include "/lib/light.glsl"
#ifdef PROGRAM_TERRAIN
#include "/lib/rain.glsl"
#include "/lib/cover.glsl"
#include "/lib/tracks.glsl"
#endif
#ifdef PROGRAM_WATER
#include "/lib/mist.glsl"
uniform sampler2D depthtex1;
uniform sampler2D colortex6;
#define REFLECT_DEPTH depthtex1
#define REFLECT_COLOR colortex6
#include "/lib/reflect.glsl"
#include "/lib/voxtrace.glsl"
#include "/lib/wakes.glsl"
#include "/lib/rain.glsl"
#include "/lib/waterfoam.glsl"
#endif

uniform sampler2D gtexture;
uniform float alphaTestRef;
uniform vec4 entityColor;

in vec2 vUv;
in vec2 vLm;
in vec4 vTint;
in vec3 vNormal;
in vec3 vPos;
flat in int vMat;

#if defined PROGRAM_TERRAIN || defined PROGRAM_BLOCK || defined PROGRAM_ENTITIES || defined PROGRAM_CLRWL
#define WRITE_SURFACE
#endif

#ifdef WRITE_SURFACE
// Couche 1 : normale de la surface et part de lumière indirecte, lues par l'occlusion d'écran (deferred).
/* RENDERTARGETS: 0,1 */
layout(location = 0) out vec4 outColor;
layout(location = 1) out vec4 outSurface;
#else
/* RENDERTARGETS: 0 */
layout(location = 0) out vec4 outColor;
#endif

#ifdef PROGRAM_WATER
#include "/lib/watershade.glsl"

/** La glace : sa couleur laisse voir au travers, sa surface lisse reflète le paysage, plus à jour frisant. */
vec4 ice(vec3 albedo, float alpha, vec3 normal, vec2 lm, float ao) {
    float indirect;
    vec3 lit = shade(albedo, normal, lm, ao, vPos, 0.0, gl_FragCoord.xy, SHADOW_FILTER, indirect);
    vec3 world = vPos + cameraPosition;
    vec3 eye = normalize(-vPos);
    vec3 r = reflect(-eye, normal);
    float f = 0.03 + 0.97 * pow(1.0 - max(dot(normal, eye), 0.0), 5.0);
    vec3 mirror = skyReflection(world, r) * mix(0.12, 1.0, lm.y * lm.y);
    float dist;
    vec3 view = (gbufferModelView * vec4(vPos, 1.0)).xyz;
    vec4 hit = screenReflection(view, mat3(gbufferModelView) * r, dither(gl_FragCoord.xy), normal.y > 0.5 ? 1.0 : 0.0, WATER_REFLECTIONS, dist);
#if VOXEL_REFLECTIONS > 0
    if (hit.a < 0.99) {
        float reach;
        vec4 vox = voxelReflection(vPos + normal * 0.02, r, reach);
        if (vox.a > 0.0) mirror = mix(mirror, veiled(vox.rgb, r * reach * 0.4), vox.a);
    }
#endif
    if (hit.a > 0.0) mirror = mix(mirror, veiled(hit.rgb, r * dist * 0.4), hit.a);
    f = f * 0.85 + 0.04;
    // Mélange sur ce qui est derrière : la glace garde sa transparence, le reflet s'y ajoute.
    float a = alpha + f * (1.0 - alpha);
    return vec4((lit * alpha * (1.0 - f) + mirror * f) / max(a, 1e-4), a);
}
#endif

void main() {
    initEnv();
    vec3 normal = normalize(vNormal);
    vec2 lm = vLm;
    float ao = 1.0;

#if defined PROGRAM_BASIC || defined PROGRAM_LIGHTNING
    vec4 color = vTint;
#elif defined PROGRAM_CLRWL
    // Flywheel (Create) : Colorwheel applique sa matière, sa lumière et sa découpe.
    vec4 color;
    vec4 overlay;
    vec2 light;
    clrwl_computeFragment(texture(gtexture, vUv), color, light, ao, overlay);
    color.rgb = mix(color.rgb, overlay.rgb, overlay.a);
    lm = lightCoord(light);
#elif defined PROGRAM_TERRAIN || defined PROGRAM_WATER
    // Sur le terrain, l'alpha du sommet porte l'occlusion (separateAo).
    vec4 color = texture(gtexture, vUv);
    color.rgb *= vTint.rgb;
    ao = vTint.a;
#else
    vec4 color = texture(gtexture, vUv) * vTint;
#endif

#if defined PROGRAM_DAMAGEDBLOCK
    // Les fissures multiplient ce qui est déjà dessiné : la texture part telle quelle.
    outColor = color;
    return;
#endif

#ifndef PROGRAM_CLRWL
    if (color.a < alphaTestRef) discard;
#endif
#if defined FAR_RECORD && FAR_SHADOWS > 0
    // Le relief pour les ombres lointaines : les plantes et les lianes n'arrêtent pas le soleil de loin.
    if (!(vMat >= 1 && vMat <= 3) && vMat != 5) farRecord(vPos + cameraPosition, gl_FragCoord.xy);
#endif
#if defined PROGRAM_ENTITIES
    color.rgb = mix(color.rgb, entityColor.rgb, entityColor.a);
#endif

    vec3 albedo = toLinear(color.rgb);

#if defined PROGRAM_EMISSIVE
    outColor = vec4(albedo * 2.4, color.a);
#elif defined PROGRAM_UNLIT
    outColor = vec4(albedo, color.a);
#elif defined PROGRAM_GLINT
    outColor = vec4(albedo * 0.8, color.a);
#elif defined PROGRAM_LIGHTNING
    // L'éclair : un trait blanc bleuté ; les couches du jeu s'additionnent, le cœur éblouit et le halo reste léger.
    outColor = vec4(vec3(0.80, 0.86, 1.0) * 1.8, color.a);
#elif defined PROGRAM_WEATHER
#if SHADER_RAIN > 0
    // La pluie et la neige sont dessinées par composite1 (lib/precip.glsl).
    discard;
#endif
    // Gouttes et flocons prennent la lumière du ciel : de fins traits gris clair, pas les traits bleus du jeu. Ceux
    // qui tombent tout contre l'œil s'effacent, sans quoi ils barrent l'écran.
    bool rain = color.b > color.r + 0.04;
    vec3 tone = rain ? vec3(0.92, 0.95, 1.0) * mix(0.75, 1.0, luma(color.rgb)) : vec3(luma(color.rgb)) * 1.05;
    vec3 light = gAmbient * 1.25 + gLightColor * 0.10 + vec3(0.025);
    float close = smoothstep(1.5, 6.0, length(vPos));
    outColor = vec4(tone * light, color.a * (rain ? 0.22 : 0.8) * mix(0.2, 1.0, close));
#elif defined PROGRAM_CLOUDS
#if CLOUD_STEPS > 0
    // Les nuages du jeu laissent la place à ceux du pack (composite).
    discard;
#endif
    float under = 0.75 + 0.25 * normal.y;
    outColor = vec4((gAmbient * 1.15 + gLightColor * 0.30 * max(gLightDir.y, 0.0)) * under, 0.82 * color.a);
#else
    float leaf = 0.0;
    int taps = SHADOW_FILTER;
#if defined PROGRAM_TERRAIN || defined PROGRAM_WATER
    albedo = olive(albedo);
    if (vMat >= 1 && vMat <= 5) leaf = vMat == 4 ? 0.55 : 1.0;
#endif
#if defined PROGRAM_PARTICLES || defined PROGRAM_BASIC
    leaf = 1.0;
    taps = 1;
#endif
#ifdef PROGRAM_WATER
    if (vMat == 6) {
        outColor = water(vTint.rgb, normal, lm);
        return;
    }
    if (vMat == 10) {
        outColor = ice(albedo, color.a, normal, lm, ao);
        return;
    }
#endif
    // Sous la pluie, le sol fonce et luit ; dans les creux plats, des flaques (leur reflet : deferred1). La glace
    // tassée luit comme un miroir terni, la neige à peine.
    float gloss = 0.0;
    float sparkle = 0.0;
#if defined PROGRAM_TERRAIN
    vec3 world = vPos + cameraPosition;
    float snow = 0.0;
    if (vMat == 0 || vMat == 4 || vMat == 9 || vMat == 14) snow = snowCover(world, normal, lm.y, vMat == 4 ? 1.0 : 0.0, luma(color.rgb / max(vTint.rgb, vec3(0.05))));
    if (snow > 0.0) albedo = mix(albedo, vec3(0.64, 0.67, 0.72), snow);
    // Le givre : les herbes et les feuillages blanchissent au pixel les matins froids.
    bool grass = vMat == 0 && vTint.g > vTint.b + 0.08;
    if (snow < 0.5 && (grass || (vMat >= 1 && vMat <= 4))) {
        float frost = frostCover(world, normal, lm, vMat >= 1 && vMat <= 3 ? 1.0 : 0.0, luma(color.rgb / max(vTint.rgb, vec3(0.05))));
        albedo = mix(albedo, vec3(0.56, 0.60, 0.66), frost * 0.7);
    }
    if (vMat == 9 || snow > 0.5) {
        // La neige du jeu est d'un blanc pur : au soleil elle brûlerait l'image, sans plus rien laisser voir de son grain.
        if (vMat == 9) albedo *= vec3(0.60, 0.62, 0.66);
        gloss = 0.3;
        sparkle = glitter(world, 0.003) * max(normal.y, 0.0);
    } else if (vMat == 11) {
        gloss = 0.75;
    } else if (vMat == 12) {
        // Le sable boit un peu de lumière et garde sa couleur, sinon il blanchit au soleil comme de la neige.
        albedo = mix(vec3(luma(albedo)), albedo, 1.25) * 0.55;
        sparkle = glitter(world, 0.0015) * max(normal.y, 0.0) * 0.6;
    }
#if TRACKS > 0
    // Les pas creusent le sable et la neige : un creux un peu plus sombre, dont le bord prend la lumière.
    if ((vMat == 9 || vMat == 12 || snow > 0.5) && normal.y > 0.9) {
        float track = tracksAt(world, normal, frameTimeCounter, rainStrength * 3.0);
        // Au fond d'un pas, la neige tassée prend le bleu de l'ombre ; le sable remué est plus sombre.
        albedo *= mix(vec3(1.0), vMat == 12 ? vec3(0.76, 0.72, 0.66) : vec3(0.80, 0.86, 0.96), track);
        gloss *= 1.0 - 0.6 * track;
    }
#endif
    float wet = leaf > 0.0 || emissive(vMat) || (vMat >= 9 && vMat <= 13) || snow > 0.0 ? 0.0 : wetAt(lm.y);
    float rainWet = wet;
    float print = 0.0;
#if defined PROGRAM_TERRAIN && TRACKS > 0
    // Qui sort de l'eau laisse des pas humides sur le sol, sable compris : la semelle fonce et luit, puis sèche.
    if (normal.y > 0.9 && leaf == 0.0 && !emissive(vMat) && (vMat < 9 || vMat > 13 || vMat == 12) && snow < 0.5) {
        print = trackWet(world, frameTimeCounter, lm.y * lm.y * (1.0 - rainStrength)) * (1.0 - rainWet);
        wet = max(wet, 0.9 * print);
    }
#endif
    if (wet > 0.0) {
        float puddle = rainWet > 0.0 ? puddleAt(vPos + cameraPosition, normal, rainWet) : 0.0;
        float soak = wet * mix(0.55, 1.0, max(normal.y, 0.0));
        albedo = mix(vec3(luma(albedo)), albedo, 1.0 + 0.2 * soak) * mix(1.0, 0.62, soak);
        albedo = mix(albedo, vec3(luma(albedo)) * 0.5, puddle * 0.8);
        gloss = max(0.4 * soak, puddle);
        // Au soleil, une pierre claire trempée paraît encore blanche : une semelle mouillée fonce davantage.
        albedo *= 1.0 - 0.55 * print;
    }
#endif
    float indirect;
    vec3 lit = shade(albedo, normal, lm, ao, vPos, leaf, gl_FragCoord.xy, taps, indirect);
#ifdef WRITE_SURFACE
    // Le feuillage laisse passer la lumière : l'occlusion y est moitié moindre. L'alpha marque une surface (> 0.5) et
    // porte son éclat (1 : mat ; 0.55 : flaque, voir deferred1).
    outSurface = vec4(packNormal(normal), indirect * (1.0 - 0.2 * leaf), 1.0 - 0.45 * gloss);
#endif
    // Les grains de neige et de sable qui scintillent, là où le soleil donne.
    lit += gLightColor * (1.0 - indirect) * sparkle * 2.5;
#if defined PROGRAM_TERRAIN
    // Les blocs qui éclairent brillent par eux-mêmes.
    if (emissive(vMat)) lit += albedo * 1.7 * smoothstep(0.25, 0.85, luma(color.rgb));
#endif
    outColor = vec4(lit, color.a);
#endif
}
