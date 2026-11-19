// Reflets : la scène vue dans un miroir (eau, flaques, surfaces mouillées), et le ciel quand elle n'y est pas.
// Demande mist.glsl et clouds.glsl avant lui, et deux définitions : REFLECT_DEPTH, la profondeur où chercher, et
// REFLECT_COLOR, la couleur de ce qu'on y trouve.

/** Position dans la vue (repère caméra) d'un point de l'écran. */
vec3 viewAt(vec2 uv, float depth) {
    vec4 v = gbufferProjectionInverse * vec4(vec3(uv, depth) * 2.0 - 1.0, 1.0);
    return v.xyz / v.w;
}

vec3 screenOf(vec3 view) {
    vec4 c = gbufferProjection * vec4(view, 1.0);
    return c.xyz / c.w * 0.5 + 0.5;
}

/** Normale (repère monde) relue dans deux canaux, rangée par packNormal. */
vec3 unpackNormal(vec2 p) {
    p = p * 2.0 - 1.0;
    vec3 n = vec3(p.x, 1.0 - abs(p.x) - abs(p.y), p.y);
    if (n.y < 0.0) n.xz = (1.0 - abs(n.zx)) * vec2(n.x >= 0.0 ? 1.0 : -1.0, n.z >= 0.0 ? 1.0 : -1.0);
    return normalize(n);
}

/**
 * Reflet d'écran : le rayon réfléchi avance dans la vue jusqu'à passer derrière la scène.
 * side : 1 si le reflet part au-dessus de la surface, -1 en dessous ; ce qui est de l'autre côté du plan (le fond vu
 * à travers l'eau, ou la rive vue d'en dessous) ne peut pas se refléter.
 * Le point de passage est resserré par dichotomie, puis vérifié : si le rayon y est loin derrière ce que montre
 * l'écran, il est passé derrière un objet (un tronc, une rive) sans le toucher ; il continue sa route au lieu d'en
 * étirer l'image vers le bas.
 * Rend la couleur trouvée et sa part (0 : rien trouvé, le ciel prend le relais) ; dist : chemin parcouru.
 */
vec4 screenReflection(vec3 start, vec3 ray, float jitter, float side, int steps, out float dist) {
    dist = 0.0;
    if (steps <= 0) return vec4(0.0);
    float stride = 0.35 + 0.025 * length(start);
    float widest = 3.0 + 0.12 * length(start);
    float level = (gbufferModelViewInverse * vec4(start, 1.0)).y;
    float t = stride * (0.5 + jitter);
    float last = 0.0;
    for (int i = 0; i < steps; i++) {
        vec3 p = start + ray * t;
        if (p.z > -near) break;
        vec3 s = screenOf(p);
        if (any(lessThan(s.xy, vec2(0.0))) || any(greaterThan(s.xy, vec2(1.0)))) break;
        float d = texture(REFLECT_DEPTH, s.xy).r;
        if (d < 1.0 && d > 0.56) {
            vec3 found = viewAt(s.xy, d);
            float scene = found.z;
            bool across = ((gbufferModelViewInverse * vec4(found, 1.0)).y - level) * side < -0.15;
            if (!across && p.z < scene) {
                // Affinage : on recule de moitié en moitié jusqu'au bord de l'objet.
                float lo = last, hi = t;
                for (int k = 0; k < 6; k++) {
                    float mid = 0.5 * (lo + hi);
                    vec3 q = screenOf(start + ray * mid);
                    if (start.z + ray.z * mid < viewAt(q.xy, texture(REFLECT_DEPTH, q.xy).r).z) hi = mid; else lo = mid;
                }
                vec3 q = start + ray * hi;
                s = screenOf(q);
                float gap = viewAt(s.xy, texture(REFLECT_DEPTH, s.xy).r).z - q.z;
                // L'épaisseur qu'on prête à ce qu'on voit : un bloc de près, un peu plus au loin.
                if (gap < 0.6 + 0.035 * -q.z + (hi - lo) * abs(ray.z) * 2.0) {
                    dist = hi;
                    vec2 edge = min(s.xy, 1.0 - s.xy);
                    float fade = smoothstep(0.0, 0.06, min(edge.x, edge.y)) * (1.0 - smoothstep(0.7, 1.0, float(i) / float(steps)));
                    return vec4(texture(REFLECT_COLOR, s.xy).rgb, fade);
                }
            }
        }
        last = t;
        t += stride;
        stride = min(stride * 1.28, widest);
    }
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
#if WATER_STARS > 0 && defined NIGHTSKY_GLSL
    // Par nuit calme, l'eau et les flaques renvoient les étoiles, la Voie lactée et l'aurore, que la brume voile.
    if (r.y > 0.0) sky += nightSky(r) * 0.9;
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
