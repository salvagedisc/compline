// Les matières des blocs : rugosité, métal, éclat des reflets, part qui luit. block.properties range chaque bloc dans
// une famille (le numéro modulo 32, lu partout ailleurs) et une matière (le numéro divisé par 32, lue ici).
//
// Matières : 0 ordinaire (pierre, bois, terre) ; 1 pierre polie ; 2 terre cuite émaillée ; 3 béton ; 4 fer ; 5 or ;
// 6 cuivre neuf ; 7 cuivre terni ; 8 cuivre oxydé ; 9 netherite ; 10 pierres précieuses ; 11 obsidienne ;
// 12 minerais ; 13 prismarine ; 14 améthyste ; 15 sculk ; 16 terre cuite ; 17 laine ; 18 boue et argile ;
// 19 blackstone dorée ; 20 laiton ; 21 zinc et acier.

struct Surface {
    float rough;   // rugosité perçue (0 miroir, 1 mat)
    float metal;   // 0 diélectrique, 1 métal
    float gloss;   // part du ciel renvoyée par deferred1 (0..0.6)
    float sheen;   // velours des tissus au soleil rasant
};

Surface surfaceOf(int kind) {
    Surface s = Surface(0.82, 0.0, 0.0, 0.0);
    if (kind == 1) s = Surface(0.34, 0.0, 0.32, 0.0);
    else if (kind == 2) s = Surface(0.16, 0.0, 0.50, 0.0);
    else if (kind == 3) s = Surface(0.55, 0.0, 0.12, 0.0);
    else if (kind == 4) s = Surface(0.38, 1.0, 0.40, 0.0);
    else if (kind == 5) s = Surface(0.26, 1.0, 0.48, 0.0);
    else if (kind == 6) s = Surface(0.30, 1.0, 0.45, 0.0);
    else if (kind == 7) s = Surface(0.50, 0.55, 0.25, 0.0);
    else if (kind == 8) s = Surface(0.66, 0.0, 0.10, 0.0);
    else if (kind == 9) s = Surface(0.42, 1.0, 0.35, 0.0);
    else if (kind == 10) s = Surface(0.14, 0.0, 0.50, 0.0);
    else if (kind == 11) s = Surface(0.12, 0.0, 0.55, 0.0);
    else if (kind == 12) s = Surface(0.82, 0.0, 0.0, 0.0);
    else if (kind == 13) s = Surface(0.30, 0.0, 0.34, 0.0);
    else if (kind == 14) s = Surface(0.20, 0.0, 0.42, 0.0);
    else if (kind == 15) s = Surface(0.62, 0.0, 0.08, 0.0);
    else if (kind == 16) s = Surface(0.66, 0.0, 0.06, 0.0);
    else if (kind == 17) s = Surface(1.0, 0.0, 0.0, 1.0);
    else if (kind == 18) s = Surface(0.48, 0.0, 0.16, 0.0);
    else if (kind == 19) s = Surface(0.82, 0.0, 0.0, 0.0);
    else if (kind == 20) s = Surface(0.30, 1.0, 0.45, 0.0);
    else if (kind == 21) s = Surface(0.40, 1.0, 0.36, 0.0);
    return s;
}

/** Saturation d'une couleur (0 gris, 1 pure). */
float chroma(vec3 c) {
    float hi = max(c.r, max(c.g, c.b));
    return hi > 1e-4 ? (hi - min(c.r, min(c.g, c.b))) / hi : 0.0;
}

/**
 * Part minérale d'un texel de minerai ou de blackstone dorée (0..1) : un texel plus coloré et plus vif que la pierre
 * qui l'entoure (sa texture vue de loin). texel, mean : couleurs du texel et de la moyenne de sa texture.
 */
float mineralTexel(vec3 texel, vec3 mean) {
    float more = chroma(texel) - chroma(mean);
    float bright = luma(texel) - luma(mean);
    return smoothstep(0.10, 0.28, more + max(bright, 0.0) * 0.6);
}

/** Facteur de Fresnel (Schlick). */
vec3 fresnel(vec3 f0, float cosine) {
    return f0 + (1.0 - f0) * pow(1.0 - clamp(cosine, 0.0, 1.0), 5.0);
}

/**
 * Reflet de l'astre sur une surface (GGX, ombrage de Smith par Schlick, Fresnel de Schlick), déjà multiplié par le
 * cosinus d'incidence. n : normale ; v : direction vers l'œil ; l : direction vers l'astre.
 */
vec3 sunSpecular(vec3 n, vec3 v, vec3 l, float rough, vec3 f0) {
    float nl = dot(n, l);
    float nv = dot(n, v);
    if (nl <= 0.0 || nv <= 0.0) return vec3(0.0);
    vec3 h = normalize(l + v);
    float nh = max(dot(n, h), 0.0);
    float a = max(rough * rough, 0.004);
    float a2 = a * a;
    float d = nh * nh * (a2 - 1.0) + 1.0;
    float D = a2 / (3.14159 * d * d);
    float k = (rough + 1.0) * (rough + 1.0) / 8.0;
    float G = nl / (nl * (1.0 - k) + k) * (nv / (nv * (1.0 - k) + k));
    vec3 F = fresnel(f0, dot(h, v));
    return min(D * G * F / (4.0 * nv), vec3(24.0));
}
