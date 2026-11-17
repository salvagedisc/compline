// L'anticrénelage temporel : chaque image décale toute la géométrie d'une fraction de pixel (suite de Halton sur huit
// images) ; composite3 (program/taa.fsh) fond l'image du moment avec celles d'avant, ramenées à leur place.
// Les arêtes fines (feuillage, clôtures, bords lointains) cessent de scintiller quand la caméra bouge.

#if TAA > 0
uniform int frameCounter;

/** Décalage de cette image, en pixels, entre -0,5 et 0,5. */
vec2 taaOffset() {
    const vec2 halton[8] = vec2[8](
        vec2(0.5, 0.333333), vec2(0.25, 0.666667), vec2(0.75, 0.111111), vec2(0.125, 0.444444),
        vec2(0.625, 0.777778), vec2(0.375, 0.222222), vec2(0.875, 0.555556), vec2(0.0625, 0.888889));
    return halton[frameCounter & 7] - 0.5;
}

/** Décale une position de découpe (gl_Position) du décalage de l'image. */
vec4 taaJitter(vec4 clip) {
    clip.xy += taaOffset() * 2.0 / vec2(viewWidth, viewHeight) * clip.w;
    return clip;
}
#else
vec4 taaJitter(vec4 clip) { return clip; }
#endif
