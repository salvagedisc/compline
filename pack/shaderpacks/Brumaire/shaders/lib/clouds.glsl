// Les nuages : une couche de cumulus en volume, poussée par le vent, plus couvrante et plus sombre sous la pluie.
// Le bruit vient de setup.csh (64³ texels qui se répètent). Unités : blocs, repère monde.

uniform sampler3D cloudNoiseTex;

const float CLOUD_TOP_SPAN = 90.0;       // épaisseur de la couche
const float CLOUD_SHAPE_SCALE = 1.0 / 420.0;
const float CLOUD_DETAIL_SCALE = 1.0 / 70.0;

float cloudBase() { return CLOUD_HEIGHT; }
float cloudTop() { return CLOUD_HEIGHT + CLOUD_TOP_SPAN; }

/** Lecture du bruit qui se répète : les coordonnées sont ramenées aux centres des texels du bord. */
vec2 cloudNoise(vec3 p) {
    return texture(cloudNoiseTex, fract(p) * (63.0 / 64.0) + 0.5 / 64.0).rg;
}

/** Décalage des nuages avec le temps : le vent du pack, sinon une brise régulière. */
vec3 cloudDrift() {
    vec2 wind = vec2(0.8, 0.35);
    if (brumaireLink > 0.5 && dot(brumaireWind.xy, brumaireWind.xy) > 0.01) wind = normalize(brumaireWind.xy) * (0.6 + 0.6 * brumaireWind.z);
    return vec3(wind.x, 0.0, wind.y) * frameTimeCounter * 4.0 * CLOUD_SPEED;
}

/** Part du ciel couverte : plus sous la pluie, un peu plus dans un biome humide. */
float cloudCoverage() {
    return clamp(CLOUD_COVERAGE * mix(0.85, 1.15, airHumid) + 0.45 * rainStrength, 0.0, 1.0);
}

/** Densité en un point du monde (0 hors de la couche). detail : ajouter l'érosion fine des bords. */
float cloudDensity(vec3 p, float coverage, bool detail) {
    float h = (p.y - cloudBase()) / CLOUD_TOP_SPAN;
    if (h <= 0.0 || h >= 1.0) return 0.0;
    vec3 q = p + cloudDrift();
    // Grandes masses : le bruit pris en tranche très étirée décide où sont les nuages.
    float weather = cloudNoise(vec3(q.xz * (CLOUD_SHAPE_SCALE * 0.22), 0.37).xzy).r;
    float shape = cloudNoise(q * CLOUD_SHAPE_SCALE).r;
    // Un cumulus : fond plat, sommet arrondi.
    float profile = smoothstep(0.0, 0.12, h) * smoothstep(1.0, mix(0.35, 0.6, coverage), h);
    float v = shape * profile * (0.6 + 0.8 * weather);
    // Un seuil net : des cumulus aux bords francs, du ciel bleu entre eux.
    float d = smoothstep(1.0 - coverage, 1.0 - coverage + 0.22, v);
    if (detail && d > 0.0) {
        float fine = cloudNoise(q * CLOUD_DETAIL_SCALE + vec3(0.0, frameTimeCounter * 0.002, 0.0)).g;
        // Les bords s'effilochent, le bas plus que le haut.
        d = clamp(d - (1.0 - fine) * mix(0.45, 0.2, h) * (1.0 - d * 0.6), 0.0, 1.0);
    }
    return d;
}

/** Ombre des nuages sur le sol, au point p (repère monde) : la lumière qui traverse la couche vers l'astre. */
float cloudShadow(vec3 p, vec3 light) {
    if (light.y < 0.05) return 1.0;
    float coverage = cloudCoverage();
    float depth = 0.0;
    for (int i = 0; i < 3; i++) {
        float y = cloudBase() + CLOUD_TOP_SPAN * (0.2 + 0.3 * float(i));
        vec3 q = p + light * ((y - p.y) / light.y);
        depth += cloudDensity(q, coverage, false);
    }
    return mix(1.0, exp(-depth * 2.2), CLOUD_SHADOWS);
}
