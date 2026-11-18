// Occlusion lissée et appliquée à la lumière indirecte, le reflet des surfaces mouillées et des flaques, puis le ciel
// peint derrière le terrain. La scène opaque est aussi copiée dans colortex6 : l'eau la relit pour ce qu'on voit à
// travers elle et ce qu'elle reflète.
#define FSH
#include "/lib/settings.glsl"
#include "/lib/common.glsl"
#include "/program/scene.glsl"
#include "/lib/shadow.glsl"
#include "/lib/mist.glsl"
#include "/lib/clouds.glsl"
#include "/lib/nightsky.glsl"
#include "/lib/rain.glsl"

uniform sampler2D colortex0;
uniform sampler2D colortex1;
uniform sampler2D colortex2;
uniform sampler2D colortex4;
in vec2 vUv;

#define REFLECT_DEPTH depthtex0
#define REFLECT_COLOR colortex0
#include "/lib/reflect.glsl"
#include "/lib/voxel.glsl"
#include "/lib/voxtrace.glsl"
#ifndef HAS_FRAME_COUNTER
uniform int frameCounter;
#endif

/*
const int colortex6Format = R11F_G11F_B10F;
const bool colortex6Clear = false;
*/

/* RENDERTARGETS: 0,6 */
layout(location = 0) out vec4 outColor;
layout(location = 1) out vec4 outScene;

float linearDepth(float d) { return near * far / (far - d * (far - near)); }
vec3 sq3(vec3 v) { return v * v; }

/** L'occlusion (r) et le rebond (gba) du demi-écran, lissés sans déborder d'un objet sur ce qui est derrière lui. */
vec4 smoothAo(vec2 uv, float depth, vec3 normal) {
    vec2 texel = 2.0 / vec2(viewWidth, viewHeight);
    float centre = linearDepth(depth);
    vec4 sum = vec4(0.0);
    float weight = 0.0;
    for (int y = -2; y <= 1; y++) {
        for (int x = -2; x <= 1; x++) {
            vec2 o = (vec2(x, y) + 0.5) * texel;
            float d = linearDepth(texture(depthtex0, uv + o).r);
            float w = 1.0 / (1e-3 + abs(d - centre) / centre * 40.0);
            // Le rebond ne passe pas d'une face à l'autre d'un même bloc.
            vec4 s = texture(colortex1, uv + o);
            w *= s.a > 0.5 ? pow(max(dot(unpackNormal(s.rg), normal), 0.0), 8.0) + 0.01 : 0.01;
            sum += texture(colortex2, (uv + o) * 0.5) * w;
            weight += w;
        }
    }
    return sum / weight;
}

/**
 * Ombre de contact : la carte d'ombre est trop grossière pour les petites choses (herbes, rebords, objets posés) ;
 * un court trajet vers l'astre à l'écran retrouve ce qui les cache de tout près.
 */
float contactShadow(vec3 pos, vec3 normal, float noise) {
#if CONTACT_SHADOWS > 0
    float dist = length(pos);
    if (dist > 48.0 || dot(normal, gLightDir) <= 0.02) return 1.0;
    vec3 view = (gbufferModelView * vec4(pos + normal * (0.02 + dist * 0.002), 1.0)).xyz;
    vec3 dir = mat3(gbufferModelView) * gLightDir;
    float span = 0.35 + dist * 0.012;
    float shadow = 0.0;
    for (int i = 0; i < 10; i++) {
        float f = (float(i) + noise) / 10.0;
        vec3 q = view + dir * span * f;
        vec4 clip = gbufferProjection * vec4(q, 1.0);
        vec2 quv = clip.xy / clip.w * 0.5 + 0.5;
        if (any(lessThan(quv, vec2(0.0))) || any(greaterThan(quv, vec2(1.0)))) break;
        float d = texture(depthtex0, quv).r;
        if (d < 0.56) continue;
        vec4 v = gbufferProjectionInverse * vec4(vec3(quv, d) * 2.0 - 1.0, 1.0);
        float behind = v.z / v.w - q.z;
        // Caché par une surface juste devant le trajet (pas par un objet bien plus proche de l'œil).
        if (behind > 0.01 && behind < 0.12 + span * 0.25) {
            shadow = 1.0 - f * 0.6;
            break;
        }
    }
    return 1.0 - shadow * (1.0 - smoothstep(32.0, 48.0, dist));
#else
    return 1.0;
#endif
}

/**
 * Ce que renvoie une surface qui luit : une flaque reflète le paysage comme un miroir ridé par les gouttes, la glace
 * tassée comme un miroir terni ; un sol trempé ou la neige luisent du ciel, sans image nette.
 * gloss : 1 flaque, 0.75 glace, jusqu'à 0.4 sol mouillé ou neige.
 */
vec3 wetReflection(vec3 color, vec3 pos, vec3 normal, float gloss) {
    vec3 world = pos + cameraPosition;
    vec3 eye = normalize(-pos);
    bool puddle = gloss > 0.95;
    vec3 n = normal;
    if (puddle) {
        vec2 slope = rippleSlope(world.xz, frameTimeCounter) * rainStrength;
        n = normalize(vec3(-slope.x, 1.0, -slope.y));
    }
    vec3 r = reflect(-eye, n);
    r.y = max(r.y, 0.01);
    float f = 0.03 + 0.97 * pow(1.0 - max(dot(n, eye), 0.0), 5.0);
    vec3 mirror = skyReflection(world, r);
    if (gloss > 0.6) {
        float dist;
        vec3 view = (gbufferModelView * vec4(pos, 1.0)).xyz;
        vec4 hit = screenReflection(view, mat3(gbufferModelView) * r, dither(gl_FragCoord.xy), 1.0, WET_REFLECTIONS, dist);
#if VOXEL_REFLECTIONS > 0
        if (hit.a < 0.99 && WET_REFLECTIONS > 0) {
            float reach;
            vec4 vox = voxelReflection(pos + normal * 0.02, r, reach);
            if (vox.a > 0.0) mirror = mix(mirror, veiled(vox.rgb, r * reach * 0.4), vox.a);
        }
#endif
        if (hit.a > 0.0) mirror = mix(mirror, veiled(hit.rgb, r * dist * 0.4), hit.a);
        return mix(color, mirror, puddle ? min(f * 1.2 + 0.18, 1.0) : f * 0.85 + 0.04);
    }
    // Un sol trempé : un éclat diffus, surtout à jour frisant.
    return mix(color, mirror, f * gloss * 1.4);
}

/**
 * Reflet d'une surface lisse sans être un miroir (pierre polie, gemme, métal, sol trempé) : un seul rayon par pixel,
 * penché au hasard autour du reflet parfait selon la rugosité (une facette tirée suivant GGX) ; l'anticrénelage
 * temporel en fait la moyenne d'une image à l'autre, d'où un reflet net sur une gemme et flou sur une pierre polie.
 * Le rayon cherche d'abord dans l'image, puis dans le volume des blocs ; le ciel n'y entre que si la surface le voit
 * (rien de plein au-dessus d'elle). Un métal teint son reflet de sa couleur.
 */
vec3 roughReflection(vec3 color, vec3 pos, vec3 normal, float gloss, bool metal) {
    vec3 eye = normalize(-pos);
    float nv = max(dot(normal, eye), 0.0);
    float rough = clamp(1.0 - 1.6 * gloss, 0.06, 1.0);
    float a = rough * rough;
    vec2 frame = vec2(float(frameCounter & 255) * 5.588238, float(frameCounter & 255) * 3.1415);
    float u1 = dither(gl_FragCoord.xy + frame);
    float u2 = dither(gl_FragCoord.yx * 1.37 + frame.yx + 11.0);
    float phi = 6.2831853 * u1;
    float cosT = sqrt((1.0 - u2) / (1.0 + (a * a - 1.0) * u2));
    float sinT = sqrt(max(1.0 - cosT * cosT, 0.0));
    vec3 tx = normalize(abs(normal.y) < 0.99 ? cross(vec3(0.0, 1.0, 0.0), normal) : cross(vec3(1.0, 0.0, 0.0), normal));
    vec3 ty = cross(normal, tx);
    vec3 h = normalize(tx * cos(phi) * sinT + ty * sin(phi) * sinT + normal * cosT);
    vec3 r = reflect(-eye, h);
    if (dot(r, normal) < 0.02) r = reflect(-eye, normal);

    // Le ciel, s'il est vu ; sous un toit ou dans une grotte, la pénombre du lieu.
    vec3 world = pos + cameraPosition;
    vec3 mirror = skyReflection(world, r);
#if VOXEL_REFLECTIONS > 0
    ivec3 here = voxelCell(pos + normal * 0.5, fract(cameraPosition));
    if (voxelInside(here) && voxCovered(here)) mirror = gAmbient * 0.12 + vec3(0.010, 0.011, 0.013);
#endif
    float dist;
    vec3 view = (gbufferModelView * vec4(pos, 1.0)).xyz;
    vec4 hit = screenReflection(view, mat3(gbufferModelView) * r, dither(gl_FragCoord.xy + frame.yx), 1.0, WET_REFLECTIONS, dist);
#if VOXEL_REFLECTIONS > 0
    if (hit.a < 0.99) {
        float reach;
        vec4 vox = voxelReflection(pos + normal * 0.02, r, reach);
        if (vox.a > 0.0) mirror = mix(mirror, veiled(vox.rgb, r * reach * 0.4), vox.a);
    }
#endif
    if (hit.a > 0.0) mirror = mix(mirror, veiled(hit.rgb, r * dist * 0.4), hit.a);

    float fres = pow(1.0 - nv, 5.0);
    float strength = clamp(gloss / 0.55, 0.0, 1.0);
    if (metal) {
        // Le métal : sa couleur était gardée dans colortex4, assombrie pour la diffusion (solid.fsh).
        vec3 albedo = sq3(texture(colortex4, vUv).rgb) / 0.35;
        vec3 f0 = clamp(albedo * 1.7, 0.04, 1.0);
        vec3 fm = f0 + (1.0 - f0) * fres * 0.35;
        return color + mirror * fm * (0.65 - 0.3 * rough) * ROUGH_REFLECTIONS_STRENGTH;
    }
    // Un peu plus que la seule réflectance d'une pierre (4 % de face) : la pierre polie et les gemmes doivent montrer ce
    // qui les entoure, comme le jeu les dessine plus lisses qu'elles ne sont.
    float f = clamp((0.05 + 0.95 * fres) * 2.2, 0.0, 0.8);
    return mix(color, mirror, f * strength * ROUGH_REFLECTIONS_STRENGTH);
}

void main() {
    vec3 color = texture(colortex0, vUv).rgb;
    bool sky, hand;
    vec3 pos = scenePosition(vUv, sky, hand);
    initEnv();
    if (sky) {
        vec3 dir = normalize(pos);
        color += skyAt(dir) + sunDisc(dir) + nightSky(dir);
    } else if (!hand) {
        vec4 surface = texture(colortex1, vUv);
        if (surface.a > 0.5) {
            vec3 normal = unpackNormal(surface.rg);
            // La part directe (1 - b) passe l'ombre de contact ; la part indirecte (b) l'occlusion.
            float contact = contactShadow(pos, normal, dither(gl_FragCoord.xy));
            color *= 1.0 - (1.0 - surface.b) * (1.0 - contact);
#if AO_SAMPLES > 0 || SSGI > 0
            vec4 g = smoothAo(vUv, texture(depthtex0, vUv).r, normal);
#if AO_SAMPLES > 0
            color *= mix(1.0, g.r, surface.b);
#endif
#if SSGI > 0
            // Le rebond, reçu par la couleur propre de la surface. Les rayons qui sortent de l'écran ou s'arrêtent avant
            // de toucher ne rapportent rien : le rebond trouvé est relevé d'autant.
            vec3 albedo = sq3(texture(colortex4, vUv).rgb);
            color += albedo * g.gba * (1.6 * SSGI_STRENGTH) * mix(1.0, g.r, 0.5);
#endif
#endif
        }
        // L'éclat (gloss, au 1/255) et, dans le demi-pas qui suit, la marque d'un métal (solid.fsh).
        float gx = (1.0 - surface.a) / 0.45 * 255.0;
        float gloss = surface.a > 0.5 ? floor(gx + 0.1) / 255.0 : 0.0;
        bool metal = surface.a > 0.5 && fract(gx + 0.1) > 0.35;
#if ROUGH_REFLECTIONS > 0
        if (gloss >= 0.15 && gloss < 0.6) color = roughReflection(color, pos, unpackNormal(surface.rg), gloss, metal);
        else if (gloss >= 0.6 || (gloss > 0.02 && !metal)) color = wetReflection(color, pos, unpackNormal(surface.rg), gloss);
#else
        if (gloss > 0.02) color = wetReflection(color, pos, unpackNormal(surface.rg), gloss);
#endif
    }
    outColor = vec4(color, 1.0);
    outScene = outColor;
}
