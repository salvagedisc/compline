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
uniform sampler2DShadow shadowtex1; // comparaison matérielle : la part éclairée d'une prise
uniform sampler2D shadowtex0;       // profondeur brute : la distance de ce qui fait de l'ombre
uniform sampler2D shadowcolor0;     // rouge : 1 là où l'eau est ce que le soleil touche en premier
uniform sampler2D shadowcolor1;     // la teinte du vitrail que le soleil passe en premier, blanc sinon

/**
 * Part de lumière directe reçue en un point (repère joueur), 0 à l'ombre. depth : l'épaisseur (blocs) entre le point
 * et ce qui lui cache l'astre, 0 en pleine lumière ; une feuille la relit pour savoir combien de feuillage la lumière
 * a traversé avant elle.
 */
float shadowDepth(vec3 pos, vec3 normal, float noise, int taps, out float depth) {
    depth = 0.0;
    vec3 clip = (shadowProjection * (shadowModelView * vec4(pos, 1.0))).xyz;
    float bend = shadowBend(clip.xy);
    // Le point est décalé le long de la normale d'à peu près un texel de la carte, vu du monde.
    float texel = 2.0 * shadowDistance / float(shadowMapResolution) * bend;
    clip = (shadowProjection * (shadowModelView * vec4(pos + normal * texel * 2.2, 1.0))).xyz;
    vec3 uv = shadowWarp(clip) * 0.5 + 0.5;
    if (uv.z >= 1.0) return 1.0;
    uv.z -= 0.00012;
    float perDepth = 2.0 / (SHADOW_DEPTH * abs(shadowProjection[2][2]));
    if (taps <= 1) {
        float lit = texture(shadowtex1, uv);
        depth = max(uv.z - texture(shadowtex0, uv.xy).r, 0.0) * perDepth * (1.0 - lit);
        return lit;
    }
    float turn = noise * 6.2831853;
    // Blocs par unité de la carte, en largeur et en profondeur.
    float perUv = 2.0 * shadowDistance * bend;

    // Ombre douce : on cherche d'abord à quelle distance se trouve ce qui cache le soleil. Plus il est loin,
    // plus le bord de l'ombre s'élargit (le soleil n'est pas un point), comme sous un arbre haut.
    // La recherche pèse les prises proches du centre davantage : un bord lointain ne doit pas élargir une ombre nette.
    float search = SHADOW_SOFTNESS * 6.0 / perUv;
    float blocker = 0.0, found = 0.0, nearest = 1.0;
    for (int i = 0; i < SHADOW_SEARCH; i++) {
        float r = sqrt((float(i) + 0.5) / float(SHADOW_SEARCH));
        float a = float(i) * 2.3999632 + turn;
        float d = texture(shadowtex0, uv.xy + vec2(cos(a), sin(a)) * r * search).r;
        if (d < uv.z) {
            float w = 1.0 - 0.6 * r;
            blocker += d * w;
            found += w;
            if (r < 0.5) nearest = min(nearest, d);
        }
    }
    if (found < 0.01) return 1.0;
    float gap = (uv.z - blocker / found) * perDepth;
    depth = max(uv.z - min(nearest, blocker / found), 0.0) * perDepth;
    // Le soleil fait un demi-degré : la pénombre s'ouvre d'un centième de la distance à ce qui cache, élargie au goût.
    float penumbra = clamp(0.03 + gap * 0.035 * SHADOW_SOFTNESS, 0.03, 0.9);
    float radius = max(penumbra / perUv, 1.2 / float(shadowMapResolution));

    float sum = 0.0;
    for (int i = 0; i < taps; i++) {
        // Disque de Vogel : des prises bien réparties, tournées d'un pixel à l'autre.
        float r = sqrt((float(i) + 0.5) / float(taps));
        float a = float(i) * 2.3999632 + turn;
        sum += texture(shadowtex1, vec3(uv.xy + vec2(cos(a), sin(a)) * r * radius, uv.z));
    }
    float lit = sum / float(taps);
    depth *= 1.0 - lit;
    return lit;
}

float shadowAt(vec3 pos, vec3 normal, float noise, int taps) {
    float depth;
    return shadowDepth(pos, normal, noise, taps, depth);
}

/** Épaisseur d'eau (en blocs) que la lumière de l'astre traverse avant d'atteindre le point ; 0 hors de l'eau. */
float waterAbove(vec3 pos, vec3 normal) {
    vec3 clip = (shadowProjection * (shadowModelView * vec4(pos + normal * 0.05, 1.0))).xyz;
    vec3 uv = shadowWarp(clip) * 0.5 + 0.5;
    if (uv.z >= 1.0 || any(lessThan(uv.xy, vec2(0.0))) || any(greaterThan(uv.xy, vec2(1.0)))) return 0.0;
    float surface = texture(shadowtex0, uv.xy).r;
    if (surface >= uv.z - 0.00005 || texture(shadowcolor0, uv.xy).r < 0.5) return 0.0;
    return (uv.z - surface) * 2.0 / (SHADOW_DEPTH * abs(shadowProjection[2][2]));
}

/** Couleur de la lumière de l'astre en un point de la carte d'ombre : celle du vitrail passé avant lui, sinon blanc. */
vec3 tintAt(vec3 uv) {
#if GLASS_TINT > 0
    if (uv.z >= 1.0 || texture(shadowtex0, uv.xy).r >= uv.z - 0.0002) return vec3(1.0);
    return texture(shadowcolor1, uv.xy).rgb;
#else
    return vec3(1.0);
#endif
}

/** La même teinte en un point (repère joueur) : le soleil passé par une verrière colore ce qu'il éclaire. */
vec3 glassTint(vec3 pos, vec3 normal) {
#if GLASS_TINT > 0
    vec3 clip = (shadowProjection * (shadowModelView * vec4(pos + normal * 0.05, 1.0))).xyz;
    vec3 uv = shadowWarp(clip) * 0.5 + 0.5;
    if (any(lessThan(uv.xy, vec2(0.0))) || any(greaterThan(uv.xy, vec2(1.0)))) return vec3(1.0);
    return tintAt(uv);
#else
    return vec3(1.0);
#endif
}
#endif
