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
    float fade = smoothstep(0.02, 0.13, abs(e));
    vec3 sun = mix(vec3(1.0, 0.50, 0.22), vec3(1.0, 0.94, 0.84), smoothstep(0.03, 0.40, e)) * 3.4;
    vec3 moon = vec3(0.36, 0.48, 0.80) * 0.16;
    gLightColor = (e > 0.0 ? sun : moon) * fade * (1.0 - 0.85 * rainStrength);
    vec3 amb = mix(vec3(0.10, 0.14, 0.24) * 0.30, vec3(0.55, 0.68, 0.86) * 1.15, gDay);
    amb = mix(amb, vec3(0.62, 0.52, 0.56) * 0.5, gDusk * 0.55 * smoothstep(-0.3, 0.0, e));
    gAmbient = mix(amb, vec3(luma(amb)) * vec3(0.86, 0.92, 1.0) * 0.75, rainStrength * 0.7);
}

/** Couleur du ciel dans une direction (repère monde), sans astres ni brume. */
vec3 skyAt(vec3 d) {
    float e = gSunDir.y;
    float up = clamp(d.y, 0.0, 1.0);
    float low = exp(-up * 5.0);
    vec3 zenith = mix(vec3(0.010, 0.016, 0.040), vec3(0.15, 0.34, 0.76), gDay);
    vec3 horizon = mix(vec3(0.030, 0.045, 0.085), vec3(0.60, 0.76, 0.93), gDay);
    float toward = dot(normalize(d.xz + 1e-5), normalize(gSunDir.xz + 1e-5)) * 0.5 + 0.5;
    vec3 warm = mix(vec3(0.80, 0.42, 0.34), vec3(1.0, 0.60, 0.28), toward) * (0.35 + 0.85 * toward);
    float dusk = gDusk * smoothstep(-0.25, 0.05, e);
    horizon = mix(horizon, warm, dusk * 0.85);
    zenith = mix(zenith, zenith * vec3(0.85, 0.80, 1.0), dusk * 0.5);
    vec3 c = mix(zenith, horizon, low);
    float cs = max(dot(d, gSunDir), 0.0);
    c += vec3(1.0, 0.72, 0.42) * (pow(cs, 6.0) * 0.10 + pow(cs, 90.0) * 0.5) * smoothstep(-0.12, 0.05, e) * (1.0 - rainStrength);
    c += vec3(0.25, 0.33, 0.50) * pow(max(dot(d, -gSunDir), 0.0), 40.0) * 0.05 * (1.0 - gDay);
    return mix(c, vec3(luma(c)) * vec3(0.78, 0.82, 0.88) * 0.7, rainStrength * 0.85);
}

/** Le disque du soleil, à ajouter au ciel : rond, à bord doux, chaud près de l'horizon. */
vec3 sunDisc(vec3 d) {
    float e = gSunDir.y;
    float disc = smoothstep(0.99880, 0.99915, dot(d, gSunDir));
    vec3 color = mix(vec3(1.0, 0.45, 0.16), vec3(1.0, 0.92, 0.78), smoothstep(0.0, 0.35, e));
    return color * disc * 28.0 * smoothstep(-0.06, 0.0, e) * (1.0 - rainStrength);
}

/** Les verts tirés vers l'olive, à luminance égale. */
vec3 olive(vec3 c) {
    float green = c.g - max(c.r, c.b);
    float w = clamp(green / max(c.g, 1e-4) * 2.5, 0.0, 1.0) * OLIVE;
    float l = luma(c);
    vec3 hue = vec3(1.02, 0.98, 0.42);
    vec3 target = mix(l * hue / luma(hue), vec3(l), 0.22) * 0.94;
    return mix(c, target, w);
}
