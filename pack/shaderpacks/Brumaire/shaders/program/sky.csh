// La table du ciel : pour chaque direction autour du soleil, la lumière diffusée par l'atmosphère vers le joueur,
// éclairée par le soleil et par la lune. La dernière ligne garde la lumière moyenne du ciel et ce qui reste de la
// lumière du soleil et de la lune une fois l'atmosphère traversée.
#define SKY_WRITER
#include "/lib/atmosphere.glsl"

layout(local_size_x = 8, local_size_y = 8) in;
const ivec3 workGroups = ivec3(24, 14, 1);
layout(rgba16f) uniform image2D skyImg;

uniform mat4 gbufferModelViewInverse;
uniform vec3 sunPosition;
uniform vec3 cameraPosition;
uniform int moonPhase;

float gMie;     // brume fine : plus dense dans un biome humide
vec3 gMieTint;  // poussière : la diffusion de Mie jaunit

/** Distance jusqu'à la sphère de rayon r depuis o (vers d), ou -1. La plus proche devant. */
float sphere(vec3 o, vec3 d, float r) {
    float b = dot(o, d);
    float c = dot(o, o) - r * r;
    float disc = b * b - c;
    if (disc < 0.0) return -1.0;
    float s = sqrt(disc);
    if (-b - s > 0.0) return -b - s;
    if (-b + s > 0.0) return -b + s;
    return -1.0;
}

/** Extinction en un point d'altitude h (km) ; xyz : air, brume, ozone (densités). */
vec3 densities(float h) {
    return vec3(exp(-h / 8.0), exp(-h / 1.2), max(0.0, 1.0 - abs(h - 25.0) / 15.0));
}

vec3 extinction(vec3 dens) {
    return RAYLEIGH * dens.x + vec3(MIE / 0.9 * gMie) * dens.y + OZONE * dens.z;
}

/** Part de la lumière d'un astre qui arrive en p (repère centré sur la Terre). */
vec3 toward(vec3 p, vec3 l) {
    if (sphere(p, l, ATM_GROUND) > 0.0) return vec3(0.0);
    float len = sphere(p, l, ATM_TOP);
    float stride = len / 6.0;
    vec3 depth = vec3(0.0);
    for (int i = 0; i < 6; i++) {
        vec3 q = p + l * (float(i) + 0.5) * stride;
        depth += extinction(densities(length(q) - ATM_GROUND)) * stride;
    }
    return exp(-depth);
}

float phaseRayleigh(float mu) { return 0.0596831 * (1.0 + mu * mu); }

float phaseMie(float mu) {
    const float g = 0.8;
    const float k = 0.1193662 * (1.0 - g * g) / (2.0 + g * g);
    return k * (1.0 + mu * mu) / pow(1.0 + g * g - 2.0 * g * mu, 1.5);
}

/** Lumière diffusée vers l'œil le long de d, pour un soleil et une lune d'éclairement donné. */
vec3 scatter(vec3 origin, vec3 d, vec3 sun, float sunPower, float moonPower, int steps) {
    float len = sphere(origin, d, ATM_TOP);
    float ground = sphere(origin, d, ATM_GROUND);
    if (ground > 0.0) len = ground;
    float stride = len / float(steps);
    float muSun = dot(d, sun);
    vec3 trans = vec3(1.0);
    vec3 sum = vec3(0.0);
    for (int i = 0; i < steps; i++) {
        vec3 p = origin + d * (float(i) + 0.5) * stride;
        vec3 dens = densities(length(p) - ATM_GROUND);
        vec3 ext = extinction(dens);
        vec3 air = RAYLEIGH * dens.x;
        vec3 haze = MIE * gMie * gMieTint * dens.y;
        vec3 lit = vec3(0.0);
        if (sunPower > 0.0) {
            vec3 t = toward(p, sun);
            // Diffusion multiple, à peu près : un fond égal dans toutes les directions, à la mesure du soleil reçu.
            lit += sunPower * (t * (air * phaseRayleigh(muSun) + haze * phaseMie(muSun)) + (air + haze) * 0.08 * dot(t, vec3(0.3333)) * vec3(0.85, 0.9, 1.0));
        }
        if (moonPower > 0.0) {
            vec3 t = toward(p, -sun);
            lit += moonPower * (t * (air * phaseRayleigh(-muSun) + haze * phaseMie(-muSun)) + (air + haze) * 0.08 * dot(t, vec3(0.3333)));
        }
        vec3 step_ = exp(-ext * stride);
        sum += trans * lit * (1.0 - step_) / max(ext, vec3(1e-7));
        trans *= step_;
    }
    return sum;
}

void main() {
    ivec2 texel = ivec2(gl_GlobalInvocationID.xy);
    if (texel.x >= SKY_W || texel.y > SKY_H) return;

    gMie = mix(1.4, 3.2, airHumid) + 2.0 * airDust();
    gMieTint = mix(vec3(1.0), vec3(1.0, 0.86, 0.66), airDust());

    vec3 sunWorld = normalize(mat3(gbufferModelViewInverse) * sunPosition);
    // Repère de la table : le soleil à l'azimut zéro.
    vec3 sun = normalize(vec3(length(sunWorld.xz), sunWorld.y, 0.0));
    // Le joueur à quelques centaines de mètres d'altitude ; la montagne éclaircit un peu le ciel.
    vec3 origin = vec3(0.0, ATM_GROUND + 0.2 + clamp((cameraPosition.y - 63.0) / 1000.0, 0.0, 0.3), 0.0);
    // La lune pleine éclaire, la nouvelle presque pas.
    float moon = MOON_POWER * (0.15 + 0.85 * abs(float(moonPhase) - 4.0) / 4.0);

    vec3 color;
    if (texel.y < SKY_H) {
        color = scatter(origin, skyDirection(texel), sun, SUN_POWER, moon, 24);
    } else if (texel.x == 0) {
        // Lumière moyenne du ciel reçue par une surface tournée vers le haut (16 directions en cosinus).
        color = vec3(0.0);
        for (int i = 0; i < 16; i++) {
            float u = (float(i) + 0.5) / 16.0;
            float r = sqrt(u);
            float a = float(i) * 2.3999632;
            color += scatter(origin, vec3(r * cos(a), sqrt(1.0 - u), r * sin(a)), sun, SUN_POWER, moon, 10);
        }
        color /= 16.0;
    } else if (texel.x == 1) {
        color = toward(origin, sun);
    } else if (texel.x == 2) {
        color = toward(origin, -sun) * moon / MOON_POWER;
    } else {
        color = vec3(0.0);
    }
    imageStore(skyImg, texel, vec4(color, 1.0));
}
