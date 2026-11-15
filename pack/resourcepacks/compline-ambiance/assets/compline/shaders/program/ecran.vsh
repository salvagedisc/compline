#version 150

// Compline : sommet commun aux passes du rendu. En plus des coordonnées d'écran, il donne à
// chaque pixel son rayon de vue (pour retrouver la distance d'un point à partir de la
// profondeur) et la place du soleil et de la lune, calculés une fois par coin et non par pixel.

in vec4 Position;

uniform vec2 OutSize;

uniform mat4 PolyProjMat;
uniform mat4 PolyModelViewMat;
uniform float PolySunAngle;

out vec2 texCoord;
// rayon de vue de ce pixel dans le repère du monde, long d'un bloc de profondeur : le point vu
// est à rayon * profondeur de la caméra
out vec3 rayon;
// les deux termes de la projection qui donnent la profondeur en blocs
flat out vec2 plans;
// direction du soleil (la lune est à l'opposé)
flat out vec3 versSoleil;
// place à l'écran de l'astre visible (xy) et s'il est devant la caméra (z)
flat out vec3 astreEcran;

void main() {
    // le jeu place ce rectangle d'après la taille de l'écran, pas celle de la cible : dans une
    // cible plus petite il n'en couvrirait qu'un coin. On le place donc nous-mêmes.
    texCoord = Position.xy / OutSize;
    gl_Position = vec4(texCoord * 2.0 - 1.0, 0.2, 1.0);

    // le rayon de vue se déduit du plan proche : le plan lointain est trop loin pour que le calcul
    // reste précis (Distant Horizons le repousse à des milliers de blocs)
    vec4 pres = inverse(PolyProjMat) * vec4(texCoord * 2.0 - 1.0, -1.0, 1.0);
    pres.xyz /= pres.w;
    rayon = (inverse(PolyModelViewMat) * vec4(pres.xyz / max(-pres.z, 1.0e-6), 0.0)).xyz;

    plans = vec2(PolyProjMat[2][2], PolyProjMat[3][2]);

    versSoleil = vec3(-cos(PolySunAngle), -sin(PolySunAngle), 0.0);
    vec3 astre = versSoleil.y >= -0.05 ? versSoleil : -versSoleil;
    vec4 clip = PolyProjMat * (PolyModelViewMat * vec4(astre * 1000.0, 1.0));
    astreEcran = clip.w > 0.0 ? vec3(clip.xy / clip.w * 0.5 + 0.5, 1.0) : vec3(0.5, 0.5, 0.0);
}
