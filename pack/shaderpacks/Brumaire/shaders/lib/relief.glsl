// Le relief des blocs, tiré de leurs textures, sans pack de normales : chaque texel est une petite marche dont la
// hauteur suit sa clarté (un texel plus sombre que la moyenne de sa texture est un creux, un plus clair une bosse).
//  - la normale de chaque texel, de la pente vers ses quatre voisins : la lumière rasante modèle la pierre, l'écorce,
//    les planches, au pixel près ;
//  - l'occlusion de parallaxe : l'œil glisse sur ce relief, les bosses cachent les creux derrière elles, à jour frisant ;
//  - l'ombre propre : un creux tourné à l'opposé de l'astre reste dans l'ombre de la bosse qui le borde ;
//  - un biseau d'un texel au bord de chaque face, qui accroche la lumière aux arêtes des blocs.
// Tout est au seizième de bloc, comme les textures : pas de relief lisse.
// Demande common.glsl avant lui ; gtexture doit être déclaré.

#if RELIEF > 0
uniform ivec2 atlasSize;
#if LABPBR > 0
// Les cartes d'un pack de ressources au format labPBR : normale (rg), occlusion (b) et hauteur (a) ; lissé (r),
// réflectance ou métal (g), porosité (b), émission (a).
uniform sampler2D normals;
uniform sampler2D specular;
#endif
// Vrai quand la face en cours a une carte labPBR : la hauteur vient alors de la carte, pas de la clarté du texel.
bool gPbrHeight = false;

/** Le rectangle de la texture du bloc dans l'atlas : son centre et sa demi-taille (uv). */
struct Sprite { vec2 mid; vec2 half_; vec2 texel; };

/** Ramène uv dans la texture du bloc : un relief qui déborde d'un bord reprend au bord opposé, la texture se répète. */
vec2 spriteWrap(Sprite s, vec2 uv) {
    vec2 lo = s.mid - s.half_;
    return lo + mod(uv - lo, 2.0 * s.half_);
}

/** Centre du texel qui contient uv. */
vec2 texelCentre(Sprite s, vec2 uv) {
    return (floor(uv / s.texel) + 0.5) * s.texel;
}

/** Hauteur d'un texel, de 0 (creux) à 1 (bosse) ; mean : clarté moyenne de la texture. Un texel vide est un creux. */
float reliefHeight(Sprite s, vec2 uv, float mean) {
#if LABPBR > 0
    if (gPbrHeight) return textureLod(normals, texelCentre(s, spriteWrap(s, uv)), 0.0).a;
#endif
    vec4 t = textureLod(gtexture, texelCentre(s, spriteWrap(s, uv)), 0.0);
    float l = luma(t.rgb);
    return clamp(0.5 + (l - mean) / max(mean, 0.05) * 0.9, 0.0, 1.0) * step(0.1, t.a);
}

/**
 * L'occlusion de parallaxe : où l'œil touche vraiment le relief, en marchant le long du regard dans le repère de la
 * face (tv : direction vers l'œil, z vers le dehors). Rend l'uv vu, et sa hauteur dans h.
 */
vec2 reliefParallax(Sprite s, vec2 uv, vec3 tv, float mean, float fade, out float h) {
    h = reliefHeight(s, uv, mean);
#if RELIEF_STEPS > 0
    if (fade <= 0.0 || tv.z <= 0.05) return uv;
    // Le relief fait RELIEF_DEPTH bloc de profondeur ; une face d'un bloc fait 2 × demi-taille en uv.
    vec2 span = -tv.xy / tv.z * (RELIEF_DEPTH * fade) * 2.0 * s.half_;
    // Pas plus nombreux à jour frisant, où le regard traverse plus de texels.
    float steps = mix(float(RELIEF_STEPS), float(RELIEF_STEPS) * 0.5, tv.z);
    float layer = 1.0;
    float dl = 1.0 / steps;
    vec2 at = uv;
    vec2 prevAt = uv;
    float prevH = h;
    for (int i = 0; i < RELIEF_STEPS; i++) {
        if (h >= layer || float(i) >= steps) break;
        prevAt = at;
        prevH = h;
        layer -= dl;
        at = uv + span * (1.0 - layer);
        h = reliefHeight(s, at, mean);
    }
    return spriteWrap(s, at);
#else
    return uv;
#endif
}

/** L'ombre propre du relief vers l'astre (tl : direction de l'astre dans le repère de la face). 1 : à l'ombre. */
float reliefShadow(Sprite s, vec2 uv, float h, vec3 tl, float mean, float fade) {
#if RELIEF_SHADOWS > 0
    if (fade <= 0.0 || tl.z <= 0.02 || h >= 0.98) return 0.0;
    vec2 span = tl.xy / tl.z * (RELIEF_DEPTH * fade) * 2.0 * s.half_;
    float shadow = 0.0;
    for (int i = 1; i <= 6; i++) {
        float k = float(i) / 6.0;
        // Le rayon monte vers l'astre ; une bosse au-dessus de lui le coupe, d'autant plus franchement qu'elle le dépasse.
        float ray = h + k * (1.0 - h);
        float above = reliefHeight(s, uv + span * k * (1.0 - h), mean) - ray;
        shadow = max(shadow, smoothstep(0.0, 0.12, above) * (1.0 - 0.5 * k));
    }
    return shadow;
#else
    return 0.0;
#endif
}

/**
 * La normale du texel (repère monde), de la pente de sa hauteur vers ses voisins, et le biseau du bord de la face.
 * t, b, n : le repère de la face (n la normale géométrique).
 */
vec3 reliefNormal(Sprite s, vec2 uv, vec3 t, vec3 b, vec3 n, float mean, float fade) {
    float hl = reliefHeight(s, uv - vec2(s.texel.x, 0.0), mean);
    float hr = reliefHeight(s, uv + vec2(s.texel.x, 0.0), mean);
    float hd = reliefHeight(s, uv - vec2(0.0, s.texel.y), mean);
    float hu = reliefHeight(s, uv + vec2(0.0, s.texel.y), mean);
    float k = 0.55 * RELIEF_STRENGTH * fade;
    vec2 slope = vec2(hl - hr, hd - hu) * k;
#if RELIEF_BEVEL > 0
    // Le biseau : le texel du bord de la face penche vers le dehors, l'arête du bloc accroche la lumière.
    vec2 e = (uv - (s.mid - s.half_)) / (2.0 * s.half_);
    vec2 one = s.texel / (2.0 * s.half_);
    slope += (step(1.0 - one, e) - step(e, one)) * 0.20 * fade;
#endif
    return normalize(t * slope.x + b * slope.y + n);
}

#if LABPBR > 0
/**
 * Une carte labPBR est-elle là ? Sans pack, Iris lie une carte neutre : toute noire, ou une normale plate pleine
 * (0.5, 0.5, 1, 1). Un texel qui ressemble à l'une ou l'autre compte comme absent.
 */
bool labPbrPresent(vec2 uv) {
    vec4 n = textureLod(normals, uv, 0.0);
    bool black = all(lessThan(n, vec4(0.004)));
    bool flat_ = all(lessThan(abs(n - vec4(0.5, 0.5, 1.0, 1.0)), vec4(0.004)));
    return !black && !flat_;
}

/** Normale d'une carte labPBR (repère de la face) : x et y rangés dans rg, z reconstruit. */
vec3 labPbrNormal(vec4 n) {
    vec2 xy = n.xy * 2.0 - 1.0;
    return vec3(xy, sqrt(max(1.0 - dot(xy, xy), 0.0)));
}
#endif
#endif
