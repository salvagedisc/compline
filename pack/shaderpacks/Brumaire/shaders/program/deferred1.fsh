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
const int colortex1Format = RGBA16;
const int colortex2Format = RGBA16F;
const int colortex4Format = RGBA8;
*/
// La couche des surfaces (solid.fsh), l'occlusion et le rebond (ao.fsh, qui peut être coupé : l'occlusion reste à 1),
// la couleur propre des surfaces.
const vec4 colortex1ClearColor = vec4(0.0, 0.0, 0.0, 0.0);
const vec4 colortex2ClearColor = vec4(1.0, 0.0, 0.0, 0.0);
const vec4 colortex4ClearColor = vec4(0.0, 0.0, 0.0, 0.0);

/* RENDERTARGETS: 0,6 */
layout(location = 0) out vec4 outColor;
layout(location = 1) out vec4 outScene;

float linearDepth(float d) { return near * far / (far - d * (far - near)); }
vec3 sq3(vec3 v) { return v * v; }

/**
 * L'occlusion (r) et le rebond (gba) du demi-écran, ramenés à l'écran entier : les quatre cases du demi-écran autour
 * du pixel, pesées comme un filtre bilinéaire et retirées quand elles sont à une autre profondeur ou tournées
 * autrement (le rebond ne passe pas d'un objet à ce qui est derrière, ni d'une face à l'autre d'un même bloc).
 * L'anticrénelage temporel finit de lisser le grain.
 */
vec4 smoothAo(vec2 uv, float depth, vec3 normal) {
    vec2 halfSize = vec2(viewWidth, viewHeight) * 0.5;
    vec2 at = uv * halfSize - 0.5;
    vec2 base = floor(at);
    vec2 f = at - base;
    float centre = linearDepth(depth);
    vec4 sum = vec4(0.0);
    float weight = 0.0;
    for (int k = 0; k < 4; k++) {
        vec2 o = vec2(k & 1, k >> 1);
        vec2 cell = (base + o + 0.5) / halfSize;
        // La case du demi-écran couvre deux pixels sur deux : sa profondeur et sa normale sont lues à son centre.
        float d = linearDepth(texture(depthtex0, cell).r);
        float w = (o.x > 0.5 ? f.x : 1.0 - f.x) * (o.y > 0.5 ? f.y : 1.0 - f.y) + 1e-3;
        w /= 1e-3 + abs(d - centre) / centre * 40.0;
        vec4 sf = texture(colortex1, cell);
        w *= sf.a > 0.5 ? pow(max(dot(unpackNormal(sf.rg), normal), 0.0), 8.0) + 0.01 : 0.01;
        sum += texture(colortex2, cell * 0.5) * w;
        weight += w;
    }
    return sum / max(weight, 1e-6);
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

#if PUDDLE_STEPS > 0
uniform vec3 previousCameraPosition;
uniform float frameTime;

/**
 * Les rides que lèvent les pas du joueur dans une flaque : tant qu'il marche, un rond toutes les deux tiers de seconde
 * part de l'endroit où était son pied et s'élargit d'un bloc en s'effaçant. Rend la pente de la surface.
 */
vec2 stepRipples(vec3 world) {
    vec3 vel = (cameraPosition - previousCameraPosition) / max(frameTime, 1e-3);
    float speed = length(vel.xz);
    float moving = smoothstep(0.8, 2.5, speed) * (1.0 - smoothstep(12.0, 20.0, speed));
    float feet = cameraPosition.y - 1.62;
    if (moving <= 0.0 || abs(world.y - feet) > 0.7) return vec2(0.0);
    vec2 slope = vec2(0.0);
    float t = frameTimeCounter;
    for (int k = 0; k < 3; k++) {
        float phase = fract(t * 1.6 + float(k) / 3.0);
        vec2 centre = cameraPosition.xz - vel.xz * phase / 1.6;
        vec2 d = world.xz - centre;
        float r = length(d);
        float ring = exp(-sq((r - 0.1 - phase * 1.1) / 0.06)) * (1.0 - phase);
        slope += d / max(r, 1e-3) * ring * 0.6;
    }
    return slope * moving;
}
#endif

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
    // Les ronds des pas : ils plient le reflet, et leurs crêtes s'éclairent ou s'assombrissent selon leur pente vers l'œil.
    float ringShade = 1.0;
    vec2 slope = puddle ? rippleSlope(world.xz, frameTimeCounter) * rainStrength : vec2(0.0);
#if PUDDLE_STEPS > 0
    // Les pas ridant toute eau qui fait miroir, même une flaque qui commence à sécher.
    if (gloss > 0.6 && normal.y > 0.9) {
        vec2 steps = stepRipples(world);
        slope += steps;
        ringShade = 1.0 + 0.8 * dot(steps, normalize(eye.xz + vec2(1e-4, 0.0)));
    }
#endif
    if (dot(slope, slope) > 0.0) n = normalize(vec3(-slope.x, 1.0, -slope.y));
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
        return mix(color, mirror, puddle ? min(f * 1.2 + 0.18, 1.0) : f * 0.85 + 0.04) * ringShade;
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
        vec3 painted = skyAt(dir);
#if SKY_TWILIGHT_BELT > 0
        painted = twilightBelt(dir, painted);
#endif
        color += painted + sunDisc(dir) + nightSky(dir);
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
