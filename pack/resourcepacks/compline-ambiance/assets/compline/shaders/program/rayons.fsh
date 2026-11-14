#version 150

// Compline : la lumière du soleil (et un peu celle de la lune) qui passe entre les feuillages.
// Pour chaque pixel, on remonte vers l'astre à l'écran et on compte le ciel dégagé rencontré :
// troncs, feuilles et crêtes découpent ainsi la lumière en rayons. La passe travaille sur une
// petite image (640 x 360) que les passes suivantes adoucissent.

uniform sampler2D InDepth;

in vec2 texCoord;
flat in vec3 versSoleil;
flat in vec3 astreEcran;

uniform vec2 OutSize;
uniform float Pluie;
uniform float Ciel;
uniform float ForceRayons;

out vec4 fragColor;

const int PAS = 28;
const float PORTEE = 0.85;
const float PERTE = 0.955;

const vec3 SOLEIL_BAS = vec3(1.00, 0.66, 0.38);
const vec3 SOLEIL_HAUT = vec3(1.00, 0.93, 0.80);
const vec3 LUNE = vec3(0.55, 0.66, 0.90);

float bruit(vec2 p) {
    return fract(52.9829189 * fract(dot(p, vec2(0.06711056, 0.00583715))));
}

void main() {
    float hauteur = versSoleil.y;
    float jour = smoothstep(-0.05, 0.10, hauteur);
    float nuit = smoothstep(-0.05, -0.20, hauteur);
    vec3 teinte = mix(SOLEIL_BAS, SOLEIL_HAUT, smoothstep(0.05, 0.55, hauteur)) * jour + LUNE * nuit * 0.30;

    float force = ForceRayons * smoothstep(0.25, 0.7, Ciel) * (1.0 - Pluie) * astreEcran.z;
    // l'astre loin hors de l'écran ne donne plus rien
    force *= smoothstep(1.3, 0.55, distance(astreEcran.xy, vec2(0.5)));
    if (force <= 0.002) {
        fragColor = vec4(0.0, 0.0, 0.0, 1.0);
        return;
    }

    float aspect = OutSize.x / OutSize.y;
    vec2 pas = (astreEcran.xy - texCoord) * (PORTEE / float(PAS));
    vec2 p = texCoord + pas * bruit(gl_FragCoord.xy);
    float somme = 0.0;
    float poids = 1.0;
    for (int i = 0; i < PAS; ++i) {
        vec2 bord = smoothstep(vec2(0.0), vec2(0.06), p) * smoothstep(vec2(1.0), vec2(0.94), p);
        float ciel = step(0.99999, texture(InDepth, p).r);
        vec2 d = (p - astreEcran.xy) * vec2(aspect, 1.0);
        // halo large autour de l'astre : le ciel proche du soleil éclaire plus
        float halo = exp(-dot(d, d) * 9.0);
        somme += ciel * halo * poids * bord.x * bord.y;
        poids *= PERTE;
        p += pas;
    }
    fragColor = vec4(teinte * somme * (1.8 / float(PAS)) * force, 1.0);
}
