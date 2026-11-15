// Uniformes communs, heure du jour, ciel.

uniform mat4 gbufferModelView;
uniform mat4 gbufferModelViewInverse;
uniform mat4 gbufferProjection;
uniform mat4 gbufferProjectionInverse;
uniform vec3 cameraPosition;
uniform vec3 sunPosition;
uniform vec3 shadowLightPosition;
uniform vec3 fogColor;
uniform float frameTimeCounter;
uniform float rainStrength;
uniform float far;
uniform float near;
uniform float viewWidth;
uniform float viewHeight;
uniform float nightVision;
uniform float blindness;
uniform float darknessFactor;
uniform int isEyeInWater;
uniform ivec2 eyeBrightnessSmooth;

// Liaison avec le mod du pack. Sans lui, Iris laisse ces valeurs à zéro et le shader garde ses replis.
uniform float brumaireLink;
uniform vec4 brumaireMist;      // x : part de brume levée autour de la caméra, y : altitude du sol lissée
uniform vec4 brumaireWind;      // xy : direction, z : force, w : turbulence
uniform vec4 brumaireClearing0; // xy : centre vu de la caméra, z : rayon, w : force
uniform vec4 brumaireClearing1;
uniform vec4 brumaireClearing2;
uniform vec4 brumaireClearing3;
uniform vec4 brumaireGust0;     // xy : direction / largeur, z : décalage / largeur, w : force de la crête
uniform vec4 brumaireGust1;
uniform vec4 brumairePush0;     // xyz : pieds d'un être vivant proche, depuis la caméra ; w : rayon d'herbes couchées
uniform vec4 brumairePush1;
uniform vec4 brumairePush2;
uniform vec4 brumairePush3;

#include "/lib/atmosphere.glsl"

const vec3 LUMA = vec3(0.2126, 0.7152, 0.0722);

vec3 gSunDir;     // vers le soleil, repère monde
vec3 gLightDir;   // vers l'astre qui éclaire (soleil ou lune)
vec3 gLightColor; // lumière directe
vec3 gAmbient;    // lumière du ciel
float gDay;       // 0 la nuit, 1 le jour
float gDusk;      // 1 quand le soleil rase l'horizon

float luma(vec3 c) { return dot(c, LUMA); }
float sq(float x) { return x * x; }
vec3 toLinear(vec3 c) { return pow(c, vec3(2.2)); }

/** Coordonnées de lumière ramenées à 0..1 (x : blocs, y : ciel). */
vec2 lightCoord(vec2 raw) { return clamp((raw - 0.03125) * 1.0667, 0.0, 1.0); }

/** Bruit de tramage stable d'une image à l'autre. */
float dither(vec2 frag) { return fract(52.9829189 * fract(dot(frag, vec2(0.06711056, 0.00583715)))); }

void initEnv() {
    gSunDir = normalize(mat3(gbufferModelViewInverse) * sunPosition);
    gLightDir = normalize(mat3(gbufferModelViewInverse) * shadowLightPosition);
    float e = gSunDir.y;
    gDay = smoothstep(-0.10, 0.22, e);
    gDusk = 1.0 - smoothstep(0.0, 0.42, abs(e + 0.02));
    // La lumière directe s'éteint quand l'astre passe l'horizon : le relais soleil/lune ne se voit pas.
    float fade = smoothstep(0.0, 0.07, abs(e));
    // Couleurs lues dans la table du ciel : ce que l'atmosphère laisse du soleil (blanc à midi, orangé puis rouge
    // au ras de l'horizon) et de la lune (selon sa phase), tirée vers le bleu comme l'œil la voit la nuit.
    vec3 sun = skyRow(1) / vec3(0.95, 0.88, 0.80) * 3.4;
    vec3 moon = luma(skyRow(2)) * vec3(0.36, 0.48, 0.80) * 0.26;
    gLightColor = (e > 0.0 ? sun : moon) * fade * (1.0 - 0.85 * rainStrength);
    // Lumière du ciel : sa moyenne, un peu désaturée (le ciel n'est jamais d'un seul bleu), plus le soleil renvoyé
    // par le sol et une lueur nocturne qui ne laisse jamais le noir complet.
    vec3 sky = skyRow(0);
    vec3 amb = mix(sky, vec3(luma(sky)), 0.45) * 1.1 + (e > 0.0 ? sun * fade * 0.06 : vec3(0.0));
    amb += vec3(0.020, 0.026, 0.042);
    gAmbient = mix(amb, vec3(luma(amb)) * vec3(0.86, 0.92, 1.0) * 0.75, rainStrength * 0.7);
}

/** Couleur du ciel dans une direction (repère monde), sans astres ni brume. */
vec3 skyAt(vec3 d) {
    vec3 c = skyScatter(d, gSunDir) + vec3(0.004, 0.006, 0.012) * (1.0 - gDay);
    // La nuit, l'œil voit en bleu.
    float night = 1.0 - smoothstep(-0.20, 0.0, gSunDir.y);
    c = mix(c, vec3(luma(c)) * vec3(0.55, 0.75, 1.30), night * 0.5);
    // Un ciel d'été voilé : jamais d'un bleu profond.
    c = mix(c, vec3(luma(c)) * vec3(0.90, 0.96, 1.08), 0.22 * gDay);
    return mix(c, vec3(luma(c)) * vec3(0.78, 0.82, 0.88) * 0.7, rainStrength * 0.85);
}

/** Le disque du soleil, à ajouter au ciel : rond, plus sombre au bord, de la couleur que l'air lui laisse. */
vec3 sunDisc(vec3 d) {
    float c = dot(d, gSunDir);
    float disc = smoothstep(0.99880, 0.99915, c);
    float r = clamp(sqrt(max(1.0 - c, 0.0) / (1.0 - 0.99897)), 0.0, 1.0);
    float limb = 1.0 - 0.55 * (1.0 - sqrt(max(1.0 - r * r, 0.0)));
    return skyRow(1) * (SUN_POWER * 1.8) * disc * limb * (1.0 - rainStrength);
}

/** Les verts tirés vers un vert de sauge, à luminance égale. */
vec3 olive(vec3 c) {
    float green = c.g - max(c.r, c.b);
    float w = clamp(green / max(c.g, 1e-4) * 2.5, 0.0, 1.0) * OLIVE;
    float l = luma(c);
    vec3 hue = vec3(0.89, 1.0, 0.63);
    vec3 target = mix(l * hue / luma(hue), vec3(l), 0.22) * 0.94;
    return mix(c, target, w);
}
