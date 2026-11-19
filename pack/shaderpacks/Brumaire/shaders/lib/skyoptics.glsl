// Ce que la lumière dessine dans le ciel en passant par l'eau en l'air :
//  - le voile de cirrus, des cristaux de glace qui diffusent vers l'avant (pâles à midi, roses au couchant) ;
//  - le halo de 22°, le grand halo de 46° et les parhélies, les deux taches irisées de part et d'autre d'un astre bas,
//    là où il y a des cirrus ;
//  - l'arc-en-ciel, quand le soleil revient sur la pluie qui s'en va : l'arc primaire (rouge dehors), le secondaire
//    plus pâle et inversé, la bande sombre d'Alexandre entre les deux et le ciel plus clair sous l'arc.
// Angles en degrés, directions en repère monde. Demande clouds.glsl avant lui.

/** Henyey-Greenstein ramenée à une moyenne de 1 sur la sphère. */
float skyHG(float mu, float g) {
    float g2 = g * g;
    return (1.0 - g2) / pow(max(1.0 + g2 - 2.0 * g * mu, 1e-4), 1.5);
}

float angleBetween(vec3 a, vec3 b) {
    return degrees(acos(clamp(dot(a, b), -1.0, 1.0)));
}

/** Un anneau de halo de rayon r : un bord intérieur net, puis une traîne qui s'efface vers l'extérieur. */
float haloRing(float a, float r, float tail) {
    return smoothstep(r - 0.45, r, a) * exp(-max(a - r, 0.0) / tail);
}

/**
 * Les halos des cristaux autour de l'astre qui éclaire : le rouge à l'intérieur, le bleu plus dehors (les prismes
 * de glace dévient moins le rouge). Rend une lumière à multiplier par la part de cirrus.
 */
vec3 iceHalo(vec3 dir) {
#if SKY_HALO > 0
    vec3 ld = gLightDir;
    float a = angleBetween(dir, ld);
    if (a > 52.0) return vec3(0.0);
    vec3 ring = vec3(haloRing(a, 21.7, 1.4), haloRing(a, 22.0, 1.7), haloRing(a, 22.4, 2.0));
    ring += 0.28 * vec3(haloRing(a, 45.5, 2.0), haloRing(a, 46.0, 2.4), haloRing(a, 46.6, 2.8));
    // Les parhélies : les plaquettes de glace couchées renvoient l'astre bas à sa hauteur, un peu au-delà de 22°.
    float e = asin(clamp(ld.y, -1.0, 1.0));
    if (e > -0.05 && e < 0.62) {
        float de = degrees(asin(clamp(dir.y, -1.0, 1.0)) - e);
        float az = angleBetween(normalize(vec3(dir.x, 0.0, dir.z) + 1e-5), normalize(vec3(ld.x, 0.0, ld.z) + 1e-5));
        float r = 22.0 / max(cos(e), 0.5);
        vec3 dog = vec3(haloRing(az, r - 0.3, 1.2), haloRing(az, r, 1.6), haloRing(az, r + 0.4, 2.2));
        dog *= exp(-sq(de / 1.4)) * (1.0 - smoothstep(0.35, 0.62, e));
        ring += dog * 2.2;
    }
    // Les couleurs d'un halo sont pâles : le blanc du soleil y domine.
    ring = mix(vec3(dot(ring, vec3(0.333))), ring, 0.65);
    // Un halo se mesure contre le ciel autour de lui : une fraction de sa lueur, plus la lumière de l'astre.
    return ring * (gLightColor * 0.05 + vec3(luma(skyAt(dir))) * 0.55);
#else
    return vec3(0.0);
#endif
}

/**
 * La nappe d'altocumulus vue le long de dir, depuis eye : sa lumière (rgb) et ce qu'elle cache du fond (a). Chaque
 * cellule a son côté d'ombre, à l'opposé de l'astre, et ses bords minces s'allument à contre-jour.
 */
vec4 altoLayer(vec3 eye, vec3 dir) {
#if CLOUD_ALTO > 0
    float t = (altoHeight() - eye.y) / dir.y;
    if (t <= 0.0) return vec4(0.0);
    vec3 p = eye + dir * t;
    float d = altoCover(p.xz);
    float seen = smoothstep(0.015, 0.12, dir.y) * exp(-t / 14000.0);
    float a = d * seen;
    if (a <= 0.002) return vec4(0.0);
    vec3 ld = gLightDir;
    // L'ombre propre : la nappe un peu plus loin vers l'astre ; une cellule voisine épaisse assombrit ce côté-ci.
    float occl = altoCover(p.xz + ld.xz / max(abs(ld.y), 0.2) * 14.0);
    float mu = dot(dir, ld);
    float phase = 0.75 + 0.06 * skyHG(mu, 0.6);
    float rim = (1.0 - d) * 0.012 * skyHG(mu, 0.85);
    vec3 lit = gLightColor * CLOUD_LIGHT * (0.32 * phase * (1.0 - 0.6 * occl) + rim) + gAmbient * 0.72;
    lit = mix(lit, vec3(luma(lit)) * 0.7, rainStrength * 0.7);
    return vec4(lit * a, a * 0.92);
#else
    return vec4(0.0);
#endif
}

/** Le voile de cirrus vu le long de dir, depuis eye : sa lumière (rgb) et ce qu'il cache du fond (a). */
vec4 cirrusLayer(vec3 eye, vec3 dir) {
#if CLOUD_CIRRUS > 0
    float t = (cirrusHeight() - eye.y) / dir.y;
    if (t <= 0.0) return vec4(0.0);
    vec3 p = eye + dir * t;
    float cover = cirrusCover(p.xz);
    // Au ras de l'horizon le voile se perd dans l'air.
    float seen = smoothstep(0.012, 0.10, dir.y) * exp(-t / 16000.0);
    float a = cover * seen;
    if (a <= 0.002) return vec4(0.0);
    float mu = dot(dir, gLightDir);
    // La glace diffuse très en avant : près de l'astre le voile brille, à l'opposé il est à peine plus clair que le ciel.
    float phase = mix(1.0, skyHG(mu, 0.80), 0.30);
    vec3 lit = gLightColor * phase * 0.10 + gAmbient * 0.78;
    lit = mix(lit, vec3(luma(lit)) * 0.75, rainStrength * 0.7);
    return vec4(lit * a + iceHalo(dir) * min(cover * 2.4, 1.0) * seen, a * 0.85);
#else
    return vec4(0.0);
#endif
}

/** Les couleurs d'un arc, x de 0 (violet, dedans) à 1 (rouge, dehors), noires hors de l'arc. */
vec3 bowSpectrum(float x) {
    float r = smoothstep(0.42, 0.80, x) + 0.30 * (1.0 - smoothstep(0.0, 0.18, x));
    float g = 1.0 - clamp(abs(x - 0.52) / 0.30, 0.0, 1.0);
    float b = 1.0 - smoothstep(0.12, 0.48, x);
    float window = smoothstep(-0.12, 0.08, x) * (1.0 - smoothstep(0.92, 1.12, x));
    return vec3(r, g, b) * window;
}

/**
 * L'arc-en-ciel autour du point opposé au soleil (ou, la nuit, à la pleine lune : un arc blanc), quand il revient sur la pluie qui s'éloigne : il faut le sol encore
 * mouillé (wetness), la pluie presque partie et le soleil assez bas pour que l'arc passe au-dessus de l'horizon.
 * Rend sa lumière (rgb) et la part du ciel qu'il laisse passer (a : la bande d'Alexandre est plus sombre).
 */
#if SKY_RAINBOW > 0 && SKY_MOONBOW > 0
uniform int moonPhase;
#endif

vec4 rainbow(vec3 dir, float wet, vec3 eye) {
#if SKY_RAINBOW > 0
    float amount = smoothstep(0.12, 0.55, wet) * (1.0 - smoothstep(0.08, 0.45, rainStrength));
    // L'astre qui fait l'arc : le soleil, ou la nuit la lune (à l'opposé du soleil), pleine ou presque.
    vec3 source = gSunDir;
    float moon = 0.0;
#if SKY_MOONBOW > 0
    if (gSunDir.y < -0.05) {
        source = -gSunDir;
        float fromFull = float(min(moonPhase, 8 - moonPhase));
        moon = 1.0 - smoothstep(0.5, 1.5, fromFull);
        amount *= moon;
    }
#endif
    amount *= smoothstep(0.02, 0.10, source.y) * (1.0 - smoothstep(0.55, 0.70, source.y));
    if (amount <= 0.001) return vec4(0.0, 0.0, 0.0, 1.0);
    float a = angleBetween(dir, -source);
    if (a > 58.0) return vec4(0.0, 0.0, 0.0, 1.0);
    // La pluie n'est pas partout : l'arc se montre par morceaux, plus franc vers le bas où elle tombe encore.
    float patch_ = cloudNoise(dir * 0.9 + vec3(0.17, 0.0, 0.41) + frameTimeCounter * 0.003).r;
    amount *= smoothstep(0.15, 0.55, patch_) * mix(1.0, 0.55, smoothstep(0.1, 0.6, dir.y));
    amount *= smoothstep(-0.03, 0.02, dir.y);
    vec3 primary = bowSpectrum((a - 40.5) / 1.9);
    // Le secondaire : deux réflexions dans la goutte, les couleurs inversées, presque trois fois plus pâle.
    vec3 secondary = bowSpectrum(1.0 - (a - 50.2) / 3.3) * 0.38;
    // Sous l'arc la lumière que les gouttes renvoient sans couleur éclaircit le ciel ; entre les arcs elle manque.
    float inner = (1.0 - smoothstep(30.0, 41.0, a)) * 0.10;
    float alexander = smoothstep(41.6, 42.6, a) * (1.0 - smoothstep(49.6, 50.6, a));
    // Contre le ciel derrière lui : un arc vif double presque la lumière de la bande où il passe.
    vec3 sun = gLightColor * 0.03 + vec3(luma(skyAt(dir))) * 0.45;
    vec3 bow = (primary + secondary) * sun + inner * sun * 0.6;
    // Sous la lune, l'œil voit à peine les couleurs : un arc blanc, à peine teinté, qui tient à la lumière de la lune.
    if (moon > 0.0) bow = mix(bow, vec3(luma(bow)), 0.75) * 0.8 + luma(primary + secondary) * gLightColor * 0.6;
    return vec4(bow * amount, 1.0 - alexander * 0.14 * amount);
#else
    return vec4(0.0, 0.0, 0.0, 1.0);
#endif
}

#if SKY_NOCTILUCENT > 0
uniform int worldDay;

/**
 * Les nuages noctulescents : un soir ou un matin sur trois, quand le soleil est de 4 à 16 degrés sous l'horizon, un
 * voile de vaguelettes d'un bleu électrique, très haut, bas sur l'horizon du côté du soleil : encore au soleil quand
 * tout le reste du ciel est dans l'ombre de la Terre.
 */
vec3 noctilucent(vec3 dir) {
    float e = gSunDir.y;
    float twilight = (1.0 - smoothstep(-0.10, -0.06, e)) * smoothstep(-0.30, -0.24, e);
    if (twilight <= 0.0 || dir.y <= 0.0) return vec3(0.0);
    if (fract(sin(float(worldDay) * 12.9898 + 4.1) * 43758.5453) > 0.35) return vec3(0.0);
    vec2 sunH = normalize(gSunDir.xz + vec2(1e-5, 0.0));
    vec2 dirH = normalize(dir.xz + vec2(1e-5, 0.0));
    float side = smoothstep(-0.3, 0.7, dot(sunH, dirH));
    float low = smoothstep(0.0, 0.03, dir.y) * (1.0 - smoothstep(0.10, 0.32, dir.y));
    // Projeté sur une couche très haute : les vaguelettes s'y alignent en bandes qui dérivent à peine.
    vec2 q = dir.xz / max(dir.y + 0.04, 0.04) * 0.6 + vec2(frameTimeCounter * 0.002, 0.0);
    float base = cloudNoise(vec3(q * 0.35, 0.37)).r;
    float ripple = 0.5 + 0.5 * sin(dot(q, vec2(5.3, 2.1)) * 2.0 + base * 6.0);
    float veil = smoothstep(0.42, 0.72, base) * (0.5 + 0.5 * ripple);
    float glow = luma(skyAt(dir)) * 2.5 + 0.0015;
    return vec3(0.35, 0.68, 1.0) * glow * veil * side * low * twilight;
}
#endif

#if SUN_PILLAR > 0
/**
 * Le pilier de soleil : par grand froid clair, au lever et au coucher, les plaquettes de glace en suspension renvoient
 * le soleil bas en une colonne de lumière qui monte au-dessus de lui (et le prolonge un peu sous l'horizon).
 */
vec3 sunPillar(vec3 dir) {
    float e = gSunDir.y;
    float lowSun = smoothstep(-0.07, -0.01, e) * (1.0 - smoothstep(0.06, 0.18, e));
    float cold = 1.0 - smoothstep(0.15, 0.50, airHeat);
    float amount = lowSun * cold * (1.0 - smoothstep(0.05, 0.3, rainStrength));
    if (amount <= 0.001) return vec3(0.0);
    float az = angleBetween(normalize(vec3(dir.x, 0.0, dir.z) + vec3(1e-5, 0.0, 0.0)),
                            normalize(vec3(gSunDir.x, 0.0, gSunDir.z) + vec3(1e-5, 0.0, 0.0)));
    if (az > 4.0) return vec3(0.0);
    float above = degrees(asin(clamp(dir.y, -1.0, 1.0)) - asin(clamp(e, -1.0, 1.0)));
    float column = exp(-sq(az / 1.1)) * (above > 0.0 ? exp(-above / 7.0) : exp(above / 2.0) * 0.6);
    return (gLightColor * 0.18 + vec3(luma(skyAt(dir))) * 1.4) * vec3(1.0, 0.85, 0.65) * column * amount;
}
#endif
