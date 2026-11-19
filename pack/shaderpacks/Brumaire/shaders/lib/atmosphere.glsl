// L'atmosphère : l'air diffuse le bleu (Rayleigh), la brume fine et les poussières diffusent autour du soleil
// (Mie), l'ozone retient un peu de rouge au crépuscule. Unités : kilomètres.
// Le ciel est calculé une fois par image dans une petite table (prepare.csh), puis relu partout : ciel, brume,
// reflets, couleur de la lumière du soleil, de la lune et du ciel.

#define SKY_W 192
#define SKY_H 108
// Dernière ligne de la table : 0 ciel moyen, 1 soleil, 2 lune (sky.csh) ; 3 la teinte de la lune, écrite par
// gbuffers_skytextured.vsh (les lunes d'Enhanced Celestials).
#define SKY_LUNAR 3
const float PI = 3.14159265;
const float HALF_PI = 1.57079633;
const float ATM_GROUND = 6360.0;
const float ATM_TOP = 6460.0;
const vec3 RAYLEIGH = vec3(5.802, 13.558, 33.1) * 1e-3;
const vec3 OZONE = vec3(0.650, 1.881, 0.085) * 1e-3;
const float MIE = 3.996e-3;
// Éclairement du soleil hors de l'atmosphère, à l'échelle de l'image ; celui de la lune, pleine.
const float SUN_POWER = 20.0;
const float MOON_POWER = 1.8;

// Air du biome où se tient le joueur, lissé sur une dizaine de secondes (shaders.properties).
uniform float airHumid; // 0 sec, 1 humide (pluviosité du biome)
uniform float airHeat;  // température du biome : 0 froid, 0,8 tempéré, 2 désert

/** Poussière dans l'air : biome chaud et sec. */
float airDust() { return smoothstep(1.1, 1.8, airHeat) * (1.0 - smoothstep(0.1, 0.4, airHumid)); }

/** Coordonnées dans la table d'une direction (repère monde) ; l'azimut est compté depuis le soleil. */
vec2 skyUv(vec3 d, vec3 sun) {
    float el = asin(clamp(d.y, -1.0, 1.0));
    float v = 0.5 + 0.5 * sign(el) * sqrt(abs(el) / HALF_PI);
    vec2 a = d.xz / max(length(d.xz), 1e-5);
    vec2 b = sun.xz / max(length(sun.xz), 1e-5);
    float u = sqrt(acos(clamp(dot(a, b), -1.0, 1.0)) / PI);
    return vec2((u * float(SKY_W - 1) + 0.5) / float(SKY_W), (v * float(SKY_H - 1) + 0.5) / float(SKY_H + 1));
}

/** Direction d'un texel de la table, dans un repère où le soleil est à l'azimut zéro. */
vec3 skyDirection(ivec2 texel) {
    float u = float(texel.x) / float(SKY_W - 1);
    float v = float(texel.y) / float(SKY_H - 1) * 2.0 - 1.0;
    float az = u * u * PI;
    float el = sign(v) * v * v * HALF_PI;
    return vec3(cos(el) * cos(az), sin(el), cos(el) * sin(az));
}

#ifndef SKY_WRITER
uniform sampler2D skyTex;

/** Lumière du ciel diffusée dans une direction, soleil et lune compris, sans les astres eux-mêmes. */
vec3 skyScatter(vec3 d, vec3 sun) { return texture(skyTex, skyUv(d, sun)).rgb; }

/** Valeurs rangées sur la dernière ligne de la table. */
vec3 skyRow(int x) { return texelFetch(skyTex, ivec2(x, SKY_H), 0).rgb; }
#endif
