// Fragments de toute la géométrie éclairée. Chaque programme d'Iris inclut ce fichier avec son PROGRAM_*.
#define FSH
#include "/lib/settings.glsl"
#include "/lib/common.glsl"
#include "/lib/shadow.glsl"
#include "/lib/light.glsl"
#ifdef PROGRAM_WATER
#include "/lib/mist.glsl"
uniform sampler2D depthtex1;
uniform sampler2D colortex6;
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
/** Position dans la vue (repère caméra) d'un point de l'écran. */
vec3 viewAt(vec2 uv, float depth) {
    vec4 v = gbufferProjectionInverse * vec4(vec3(uv, depth) * 2.0 - 1.0, 1.0);
    return v.xyz / v.w;
}

vec3 screenOf(vec3 view) {
    vec4 c = gbufferProjection * vec4(view, 1.0);
    return c.xyz / c.w * 0.5 + 0.5;
}

/**
 * Reflet d'écran : le rayon réfléchi avance dans la vue jusqu'à passer derrière la scène opaque (colortex6).
 * side : 1 si le reflet part au-dessus de l'eau, -1 en dessous ; ce qui est de l'autre côté de la surface (le fond vu
 * à travers l'eau, ou la rive vue d'en dessous) ne peut pas se refléter.
 * Rend la couleur trouvée et sa part (0 : rien trouvé, le ciel prend le relais) ; dist : chemin parcouru.
 */
vec4 screenReflection(vec3 start, vec3 ray, float jitter, float side, out float dist) {
    dist = 0.0;
#if WATER_REFLECTIONS > 0
    float stride = 0.35 + 0.025 * length(start);
    float level = (gbufferModelViewInverse * vec4(start, 1.0)).y;
    float t = stride * (0.5 + jitter);
    float last = 0.0;
    for (int i = 0; i < WATER_REFLECTIONS; i++) {
        vec3 p = start + ray * t;
        if (p.z > -near) break;
        vec3 s = screenOf(p);
        if (any(lessThan(s.xy, vec2(0.0))) || any(greaterThan(s.xy, vec2(1.0)))) break;
        float d = texture(depthtex1, s.xy).r;
        if (d < 1.0 && d > 0.56) {
            vec3 found = viewAt(s.xy, d);
            float scene = found.z;
            bool across = ((gbufferModelViewInverse * vec4(found, 1.0)).y - level) * side < -0.15;
            if (!across && p.z < scene && scene - p.z < stride * 2.0 + 0.6) {
                // Affinage : on recule de moitié en moitié jusqu'au bord de l'objet.
                float lo = last, hi = t;
                for (int k = 0; k < 4; k++) {
                    float mid = 0.5 * (lo + hi);
                    vec3 q = screenOf(start + ray * mid);
                    if (start.z + ray.z * mid < viewAt(q.xy, texture(depthtex1, q.xy).r).z) hi = mid; else lo = mid;
                }
                s = screenOf(start + ray * hi);
                dist = hi;
                vec2 edge = min(s.xy, 1.0 - s.xy);
                float fade = smoothstep(0.0, 0.06, min(edge.x, edge.y)) * (1.0 - smoothstep(0.7, 1.0, float(i) / float(WATER_REFLECTIONS)));
                return vec4(texture(colortex6, s.xy).rgb, fade);
            }
        }
        last = t;
        t += stride;
        stride *= 1.28;
    }
#endif
    return vec4(0.0);
}

/** Le ciel reflété : voilé par la même brume que le vrai, avec les nuages à mi-hauteur de leur couche. */
vec3 skyReflection(vec3 world, vec3 r) {
    float nearOnly;
    float veil = 1.0 - exp(-mistDepth(r, 4096.0, nearOnly));
    vec3 sky = skyAt(r);
#if CLOUD_STEPS > 0
    if (r.y > 0.03) {
        float y = cloudBase() + CLOUD_TOP_SPAN * 0.3;
        float reach = (y - world.y) / r.y;
        if (reach > 0.0 && reach < CLOUD_RANGE) {
            float d = cloudDensity(world + r * reach, cloudCoverage(), false);
            vec3 lit = gLightColor * (0.30 + 0.25 * max(dot(r, gLightDir), 0.0)) + gAmbient * 0.85;
            lit = mix(lit, vec3(luma(lit)) * 0.75, rainStrength);
            sky = mix(sky, lit, d * 0.85 * (1.0 - reach / CLOUD_RANGE));
        }
    }
#endif
    return mix(sky, mistColor(r, 1.0), veil);
}

/** Le voile de l'air sur un trajet (repère joueur, depuis la caméra) : la brume que traverse un reflet. */
vec3 veiled(vec3 color, vec3 world) {
    vec3 dir = normalize(world);
    float len = length(world);
    vec3 inscat;
    color = color * airVeil(dir, len, inscat) + inscat;
    float nearOnly;
    float veil = 1.0 - exp(-mistDepth(dir, len, nearOnly));
    return mix(color, mistColor(dir, 1.0), veil);
}

vec4 water(vec3 biome, vec3 normal, vec2 lm) {
    vec3 world = vPos + cameraPosition;
    vec3 n = normal;
    if (abs(normal.y) > 0.5) {
        vec2 slope = waveSlope(world.xz, frameTimeCounter);
        n = normalize(vec3(-slope.x, 1.0, -slope.y));
    }
    vec3 eye = normalize(-vPos);
    bool below = isEyeInWater == 1;
    if (below) n = -n;
    if (dot(n, eye) < 0.05) n = normalize(n + eye * (0.05 - dot(n, eye)));
    float lit = shadowAt(vPos, normal, dither(gl_FragCoord.xy), 4) * cloudShadow(world, gLightDir);
    float sky = lm.y * lm.y;
    vec3 hue = waterHue(biome);
    vec3 ext = waterExtinction(hue);
    vec3 murk = waterMurk(hue, max(sky, 0.08), lit);

    vec2 frag = gl_FragCoord.xy / vec2(viewWidth, viewHeight);
    vec3 view = (gbufferModelView * vec4(vPos, 1.0)).xyz;
    float jitter = dither(gl_FragCoord.xy);

    if (below) {
        // Vue d'en dessous : le monde du dessus passe par la fenêtre de Snell, au-delà l'eau se reflète elle-même.
        vec3 t = refract(-eye, n, WATER_IOR);
        vec3 r = reflect(-eye, n);
        float dist;
        vec4 hit = screenReflection(view, mat3(gbufferModelView) * r, jitter, -1.0, dist);
        vec3 inner = mix(murk, hit.rgb, hit.a);
        if (dot(t, t) < 1e-4) return vec4(inner, 1.0);
        vec3 s = screenOf(view + mat3(gbufferModelView) * t * 12.0);
        vec3 above = texture(colortex6, clamp(s.xy, 0.001, 0.999)).rgb;
        if (s.z > 1.0 || any(lessThan(s.xy, vec2(0.0))) || any(greaterThan(s.xy, vec2(1.0)))) above = skyAt(t) * sky;
        float cosT = max(dot(t, -n), 0.0);
        float f = 0.02 + 0.98 * pow(1.0 - cosT, 5.0);
        return vec4(mix(above, inner, f), 1.0);
    }

    // Ce qu'on voit à travers : le fond décalé par les vagues, éteint et voilé par l'épaisseur d'eau.
    float surface = -view.z;
    float straight = texelFetch(depthtex1, ivec2(gl_FragCoord.xy), 0).r;
    float deep0 = straight >= 1.0 ? 64.0 : length(viewAt(frag, straight)) - length(view);
    vec3 bend = mat3(gbufferModelView) * vec3(n.x, 0.0, n.z);
    vec2 uv = frag + bend.xy * gbufferProjection[1][1] * 0.35 * min(deep0, 3.0) / max(surface, 1.0);
    float depth = texture(depthtex1, uv).r;
    if (depth < 1.0 && viewAt(uv, depth).z > view.z - 0.02) { uv = frag; depth = straight; }
    float deep = depth >= 1.0 ? 64.0 : max(length(viewAt(uv, depth)) - length(view), 0.0);
    vec3 pass = exp(-ext * deep);
    vec3 under = texture(colortex6, uv).rgb * pass + murk * (1.0 - pass);

    // Ce qu'elle reflète : la scène quand le rayon la trouve à l'écran, sinon le ciel.
    vec3 r = reflect(-eye, n);
    r.y = max(r.y, 0.01);
    // Un peu plus de reflet que la physique ne le veut : l'eau se lit d'abord par ce qu'elle reflète.
    float f = 0.04 + 0.96 * pow(1.0 - max(dot(n, eye), 0.0), 4.0);
    vec3 mirror = skyReflection(world, r) * mix(0.12, 1.0, sky);
    float dist;
    vec4 hit = screenReflection(view, mat3(gbufferModelView) * r, jitter, 1.0, dist);
    if (hit.a > 0.0) {
        // Le point reflété, voilé par l'air et la brume de la surface jusqu'à lui (le chemin de la caméra à la
        // surface est voilé par composite1 comme le reste). La brume basse est déjà comptée une fois sur ce
        // chemin : le reflet n'en reprend qu'une part, sans quoi les rives disparaîtraient de l'eau.
        mirror = mix(mirror, veiled(hit.rgb, r * dist * 0.4), hit.a);
    }

    // L'éclat du soleil sur les rides.
    vec3 h = normalize(gLightDir + eye);
    float glint = min(pow(max(dot(n, h), 0.0), 900.0) * (0.6 + 40.0 * f), 24.0);
    vec3 color = mix(under, mirror, f) + gLightColor * lit * glint * 0.05;
    return vec4(color, 1.0);
}
#endif

void main() {
    initEnv();
    vec3 normal = normalize(vNormal);
    vec2 lm = vLm;
    float ao = 1.0;

#if defined PROGRAM_BASIC
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
#elif defined PROGRAM_WEATHER
    outColor = vec4(albedo * (gAmbient * 1.3 + gLightColor * 0.08 + vec3(0.02)), color.a * 0.75);
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
#endif
    float indirect;
    vec3 lit = shade(albedo, normal, lm, ao, vPos, leaf, gl_FragCoord.xy, taps, indirect);
#ifdef WRITE_SURFACE
    // Le feuillage laisse passer la lumière : l'occlusion y est moitié moindre.
    outSurface = vec4(packNormal(normal), indirect * (1.0 - 0.2 * leaf), 1.0);
#endif
#if defined PROGRAM_TERRAIN
    // Les blocs qui éclairent brillent par eux-mêmes.
    if (vMat == 8) lit += albedo * 1.7 * smoothstep(0.25, 0.85, luma(color.rgb));
#endif
    outColor = vec4(lit, color.a);
#endif
}
