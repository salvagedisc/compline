// L'herbe qui déborde : sur le côté d'un bloc d'herbe, la bordure verte du jeu (sa seconde couche, teinte par le
// biome et découpée) s'arrête à trois ou quatre pixels, d'un trait presque droit. Elle descend ici de quelques
// pixels de plus, d'une longueur qui change d'une colonne à l'autre, avec les mêmes gris que la bordure du jeu.
// Lu par solid.fsh, avant la découpe.

#if GRASS_OVERHANG > 0
float grassEdgeHash(vec3 p) { return fract(sin(dot(p, vec3(127.1, 311.7, 74.7))) * 43758.5453); }

/**
 * Le texel (couleur du jeu, avant la teinte) que prend un pixel transparent de la bordure, ou rien (alpha 0).
 * world : point de la face (repère monde) ; normal : sa normale, horizontale.
 */
vec4 grassOverhang(vec3 world, vec3 normal) {
    vec3 cell = floor(world - normal * 0.01);
    float across = abs(normal.x) > 0.5 ? world.z : world.x;
    float col = floor(fract(across) * 16.0);
    float row = floor((1.0 - fract(world.y)) * 16.0);
    // De 3 à 6 pixels, plus souvent court que long ; la face voisine du même bloc tire d'autres longueurs.
    float h = grassEdgeHash(cell + normal * 3.1 + vec3(col * 0.37, 0.0, col * 0.11));
    float reach = 3.0 + floor(h * h * 4.0) * float(GRASS_OVERHANG);
    if (row >= reach) return vec4(0.0);
    // Les gris de la bordure du jeu, tirés texel par texel ; le bout de la frange un peu plus sombre.
    float g = grassEdgeHash(cell * 1.7 + normal + vec3(col, row, 0.0));
    float grey = g < 0.25 ? 0.51 : (g < 0.5 ? 0.56 : (g < 0.75 ? 0.59 : 0.66));
    grey *= row >= reach - 1.0 ? 0.90 : 1.0;
    return vec4(vec3(grey), 1.0);
}
#endif
