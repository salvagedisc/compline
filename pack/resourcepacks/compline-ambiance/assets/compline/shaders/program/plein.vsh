#version 150

// Compline : sommet des passes qui n'ont besoin que des coordonnées d'écran (flou, copie).
// Le jeu place ce rectangle d'après la taille de l'écran, pas celle de la cible : dans une cible
// plus petite il n'en couvrirait qu'un coin. On le place donc nous-mêmes.

in vec4 Position;

uniform vec2 OutSize;

out vec2 texCoord;

void main() {
    texCoord = Position.xy / OutSize;
    gl_Position = vec4(texCoord * 2.0 - 1.0, 0.2, 1.0);
}
