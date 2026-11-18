// Le ciel de nuit : des étoiles qui tournent avec le ciel autour de son pôle, de toutes les tailles et de toutes les
// couleurs (bleu-blanc à orangé), qui scintillent d'autant plus qu'elles sont basses ; la Voie lactée en travers,
// plus dense vers son centre, coupée de bandes de poussière sombre ; et l'auréole que la brume fait autour de la lune.
// La lune pleine pâlit la Voie lactée et noie les étoiles les plus faibles ; la pluie et l'aube les effacent.
// Demande clouds.glsl avant lui (le bruit de la Voie lactée). Directions en repère monde.

uniform int moonPhase;

float starHash(vec3 p) {
    p = fract(p * vec3(0.1031, 0.1030, 0.0973));
    p += dot(p, p.yxz + 33.33);
    return fract((p.x + p.y) * p.z);
}

/**
 * Le repère du ciel étoilé : le pôle autour duquel tournent les astres (l'axe nord-sud, penché de sunPathRotation),
 * et deux axes qui suivent le soleil. Les étoiles y sont fixes : elles tournent avec lui.
 */
mat3 celestialFrame() {
    float tilt = radians(sunPathRotation);
    vec3 a = vec3(0.0, sin(tilt), cos(tilt));
    vec3 b = vec3(0.0, -sin(tilt), cos(tilt));
    // Le bon pôle est perpendiculaire au soleil ; le doute n'existe qu'au ras de l'horizon, où aucune étoile ne se voit.
    vec3 pole = abs(dot(gSunDir, a)) < abs(dot(gSunDir, b)) ? a : b;
    vec3 e1 = normalize(gSunDir - pole * dot(gSunDir, pole));
    vec3 e2 = cross(pole, e1);
    return mat3(e1, e2, pole);
}

/** Une face de cube et sa position dessus (-1..1) : un découpage du ciel en cellules presque égales. */
vec3 cubeFace(vec3 c, out float face) {
    vec3 a = abs(c);
    if (a.x >= a.y && a.x >= a.z) { face = c.x > 0.0 ? 0.0 : 1.0; return vec3(c.yz / a.x, a.x); }
    if (a.y >= a.z) { face = c.y > 0.0 ? 2.0 : 3.0; return vec3(c.xz / a.y, a.y); }
    face = c.z > 0.0 ? 4.0 : 5.0;
    return vec3(c.xy / a.z, a.z);
}

/** Le point de la sphère qui correspond à la position uv (-1..1) sur une face du cube. */
vec3 cubePoint(float face, vec2 uv) {
    vec3 p = face < 1.5 ? vec3(face < 0.5 ? 1.0 : -1.0, uv) : (face < 3.5 ? vec3(uv.x, face < 2.5 ? 1.0 : -1.0, uv.y)
                                                                          : vec3(uv, face < 4.5 ? 1.0 : -1.0));
    return normalize(p);
}

/** Couleur d'une étoile selon sa température, de 0 (rouge-orangé) à 1 (bleu-blanc). */
vec3 starColor(float k) {
    vec3 warm = vec3(1.00, 0.70, 0.45);
    vec3 sun = vec3(1.00, 0.94, 0.86);
    vec3 hot = vec3(0.70, 0.80, 1.00);
    return k < 0.5 ? mix(warm, sun, k * 2.0) : mix(sun, hot, k * 2.0 - 1.0);
}

/**
 * Une couche d'étoiles : une cellule sur le cube en porte une au plus, à une place, un éclat et une couleur tirés au
 * sort pour elle. density : part des cellules qui en ont une ; pix : taille d'un pixel (radians).
 */
vec3 starLayer(vec3 c, float cells, float density, float seed, float pix, float t, float low) {
    float face;
    vec3 f = cubeFace(c, face);
    vec2 g = f.xy * cells;
    vec2 cell = floor(g);
    vec3 id = vec3(cell, face * 131.0 + seed);
    if (starHash(id) > density) return vec3(0.0);
    vec2 at = 0.25 + 0.5 * vec2(starHash(id + 17.1), starHash(id + 41.7));
    // Distance angulaire au centre de l'étoile, mesurée sur la sphère : ronde partout, même au bord d'une face.
    float d = length(c - cubePoint(face, (cell + at) / cells));
    float r = pix * 0.85;
    float core = exp(-sq(d / r));
    if (core < 0.004) return vec3(0.0);
    float flux = pow(starHash(id + 7.3), 9.0) * 4.0 + 0.025;
    // Le scintillement : l'air qui tremble devant l'étoile, plus fort près de l'horizon où il y en a plus.
    float h = starHash(id + 3.9);
    float twinkle = sin(t * (6.0 + 7.0 * h) + h * 40.0) * sin(t * (2.3 + 3.1 * h) + h * 11.0);
    flux *= 1.0 + twinkle * mix(0.18, 0.75, low);
    return starColor(starHash(id + 29.3)) * flux * core;
}

/** La Voie lactée en c (repère du ciel) : sa lueur, et rend aussi dans band la densité d'étoiles qu'elle ajoute. */
vec3 milkyWay(vec3 c, out float band) {
    // Son plan est penché sur l'équateur du ciel ; son centre, plus épais et plus chaud, est d'un côté.
    vec3 galPole = normalize(vec3(0.46, 0.58, 0.67));
    vec3 galCentre = normalize(cross(galPole, vec3(0.0, 0.0, 1.0)));
    float b = dot(c, galPole);
    float l = dot(c, galCentre);
    // Le bord du disque se froisse : la largeur du ruban varie le long de lui.
    float wobble = cloudNoise(c * 0.8 + 0.31).r - 0.5;
    float lat = b + wobble * 0.07;
    band = exp(-sq(lat / 0.17));
    float bulge = exp(-sq(lat / 0.10)) * smoothstep(0.1, 1.0, l) * smoothstep(0.1, 1.0, l);
    float n1 = cloudNoise(c * 1.7 + 0.11).r;
    float n2 = cloudNoise(c * 4.6 + 0.37).r;
    float n3 = cloudNoise(c * 13.0 + 0.73).g;
    float glow = band * (0.35 + 0.9 * n1 * n2) + bulge * (0.9 + 0.6 * n2);
    // Des grains : les étoiles trop faibles pour se voir seules, en nuées.
    glow += band * pow(n3, 6.0) * 1.6;
    // La poussière : des bandes sombres qui suivent le plan, un peu décalées de son milieu, déchirées.
    float dustShape = cloudNoise(c * 3.2 + 0.53).r * 0.6 + cloudNoise(c * 9.1 + 0.19).g * 0.4;
    float dust = smoothstep(0.42, 0.70, dustShape) * exp(-sq((lat - 0.02) / 0.06));
    glow *= 1.0 - 0.78 * dust;
    vec3 tint = mix(vec3(0.74, 0.82, 1.00), vec3(1.00, 0.86, 0.66), clamp(bulge * 1.5, 0.0, 1.0));
    return tint * glow;
}

/** Le ciel de nuit à ajouter au ciel peint, dans la direction d (repère monde). */
vec3 nightSky(vec3 d) {
#if SKY_STARS > 0 || SKY_MILKY_WAY > 0
    float night = (1.0 - smoothstep(-0.30, -0.05, gSunDir.y)) * (1.0 - rainStrength);
    vec3 result = vec3(0.0);
    // L'auréole de la lune : la brume en diffuse un peu tout autour d'elle.
    vec3 moonDir = -gSunDir;
    if (moonDir.y > -0.08) {
        float a = acos(clamp(dot(d, moonDir), -1.0, 1.0));
        float haze = 0.6 + 0.8 * airHumid + 1.5 * rainStrength;
        result += gLightColor * (exp(-a / 0.030) * 0.30 + exp(-a / 0.20) * 0.05) * haze * smoothstep(-0.08, 0.05, moonDir.y);
    }
    if (night <= 0.001 || d.y < -0.05) return result;
    vec3 c = transpose(celestialFrame()) * d;
    // Plus d'air à traverser vers l'horizon : les astres bas pâlissent.
    float air = exp(-0.10 / max(d.y + 0.04, 0.02));
    float low = 1.0 - smoothstep(0.05, 0.5, d.y);
    float full = 1.0 - abs(float(moonPhase) - 4.0) / 4.0;
    float pix = 2.0 / (viewHeight * gbufferProjection[1][1]);
    float band = 0.0;
    vec3 sky = vec3(0.0);
#if SKY_MILKY_WAY > 0
    sky += milkyWay(c, band) * 0.032 * mix(1.0, 0.35, full);
#endif
#if SKY_STARS > 0
    float t = frameTimeCounter;
    // Trois couches : quelques étoiles vives, beaucoup de moyennes, et une poussière de faibles, dense dans la Voie lactée.
    vec3 stars = starLayer(c, 90.0, 0.10, 1.0, pix, t, low) * 1.6;
    stars += starLayer(c, 210.0, 0.035 + 0.12 * band, 2.0, pix, t, low) * 0.5;
    stars += starLayer(c, 420.0, 0.012 + 0.10 * band, 3.0, pix, t, low) * 0.3 * mix(1.0, 0.25, full);
    sky += stars * 0.22;
#endif
    return result + sky * night * air;
#else
    return vec3(0.0);
#endif
}
