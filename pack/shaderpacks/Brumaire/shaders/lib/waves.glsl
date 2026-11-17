// Les vagues : un spectre de trains d'onde de Gerstner en trois bandes (la houle, les vagues du vent, les rides), qui
// suivent la dispersion de l'eau (une onde longue va plus vite qu'une courte) et s'aplatissent sur un haut-fond. Chaque
// train rend sa hauteur, sa pente, le pli horizontal de sa crête (jacobien du déplacement : là où
// la surface se replie, la vague déferle et blanchit) et sa courbure (hessien : là où la surface fait loupe, la lumière
// se resserre en filets sur le fond).
//
// Les trains trop fins pour la taille du pixel sont retirés en douceur, et leur pente devient de la rugosité : au loin
// l'eau renvoie autant de lumière, mais floue, au lieu de scintiller.
//
// Unités : blocs et secondes (un bloc fait un mètre). Lu par la surface (solid.fsh) et par le fond (light.glsl).

#define WAVE_MAX 32

// La pesanteur, et l'allure du monde : un peu plus lente que la physique, plus paisible à regarder.
const float WAVE_G = 9.81;
const float WAVE_PACE = 0.62;

/** Ce qui fait les vagues en un point. */
struct WaveEnv {
    vec2 wind;   // direction du vent, unitaire
    float force; // 0 calme .. 1 vent fort
    float storm; // 0..1 orage
    float open;  // 0 mare .. 1 grande eau libre : la place qu'a le vent pour lever la houle
    float depth; // profondeur d'eau sous ce point (blocs)
    vec2 flow;   // courant (blocs par seconde), le long de la pente pour l'eau qui coule
};

/** Ce que rendent les vagues en un point. */
struct WaveSample {
    float height;   // au-dessus du niveau moyen (blocs)
    vec2 slope;     // pente (dh/dx, dh/dz), crêtes affûtées par le déplacement de Gerstner
    vec3 jac;       // jacobien du déplacement horizontal : xx, zz, xz (1, 1, 0 pour une eau plate)
    vec3 hess;      // courbure de la surface : d²h/dx², d²h/dz², d²h/dxdz
    float variance; // variance des pentes retirées parce que trop fines pour le pixel (rugosité)
    float curv;     // variance de la courbure gardée : sa force moyenne, pour doser les caustiques
};

/** Un train d'onde. */
struct WaveTrain {
    vec2 dir;    // sens de marche, unitaire
    float k;     // nombre d'onde (radians par bloc)
    float slope; // cambrure k·A : la pente la plus forte du train
    float omega; // pulsation (radians par seconde)
    float phase;
    float q;     // part de déplacement horizontal (Gerstner)
};

float waveHash(float n) { return fract(sin(n * 91.3458 + 0.731) * 47453.5453); }

vec2 waveRotate(vec2 d, float a) {
    float c = cos(a), s = sin(a);
    return vec2(c * d.x - s * d.y, s * d.x + c * d.y);
}

/** L'environnement par défaut : le vent du ciel (donné par le mod), une eau moyenne, sans courant. */
WaveEnv waveEnvDefault() {
    WaveEnv e;
    e.wind = vec2(0.8, 0.6);
    e.force = 0.35;
    if (brumaireLink > 0.5 && dot(brumaireWind.xy, brumaireWind.xy) > 0.01) {
        e.wind = normalize(brumaireWind.xy);
        e.force = clamp(brumaireWind.z, 0.0, 1.0);
    }
    e.storm = clamp(rainStrength, 0.0, 1.0) * (0.35 + 0.65 * smoothstep(0.6, 1.0, e.force));
    e.open = 0.6;
    e.depth = 4.0;
    e.flow = vec2(0.0);
    return e;
}

/**
 * Nombre de trains par bande, et leur pente moyenne (écart type des pentes) : la houle demande de l'eau libre et du
 * vent qui dure, les vagues du vent suivent sa force, les rides naissent du moindre souffle et de la pluie.
 */
void waveBands(int count, WaveEnv env, out ivec3 n, out vec3 rms) {
    n.x = max(count / 4, 1);
    n.y = max(count / 2, 1);
    n.z = max(count - n.x - n.y, 1);
    rms.x = 0.048 * WATER_SWELL * env.open * (0.45 + 0.55 * env.force + 0.9 * env.storm);
    rms.y = 0.074 * (0.42 + 0.58 * env.force) + 0.06 * env.storm;
    rms.z = 0.032 * (0.55 + 0.45 * env.force) + 0.020 * env.storm + 0.025 * rainStrength;
    rms *= WATER_WAVES;
}

/** Le train numéro i sur count, tiré une fois pour toutes (mêmes trains partout, d'une image à l'autre). */
WaveTrain waveTrain(int i, int count, WaveEnv env, ivec3 n, vec3 rms, float chop) {
    float h1 = waveHash(float(i) + 0.17);
    float h2 = waveHash(float(i) * 1.618 + 3.1);
    float h3 = waveHash(float(i) * 2.414 + 7.7);
    int band = i < n.x ? 0 : (i < n.x + n.y ? 1 : 2);
    int idx = band == 0 ? i : (band == 1 ? i - n.x : i - n.x - n.y);
    int size = band == 0 ? n.x : (band == 1 ? n.y : n.z);
    // Longueurs d'onde réparties en suite géométrique dans la bande, un peu dérangées pour ne pas battre.
    vec2 range = band == 0 ? vec2(20.0, 6.0) : (band == 1 ? vec2(6.0, 1.2) : vec2(1.1, 0.25));
    float lambda = range.x * pow(range.y / range.x, (float(idx) + 0.15 + 0.7 * h1) / float(size));
    // Directions : fixées une fois pour toutes et réparties autour du cercle (angle d'or), le vent ne fait que donner
    // plus de force aux trains qui vont dans son sens. Tourner les trains avec le vent ferait glisser tout le motif
    // dès qu'il change un peu (la phase k·p est grande loin de l'origine du monde) : ici rien ne glisse, la mer change
    // de visage en douceur. La houle suit étroitement le vent, les rides presque pas.
    float angle = float(idx) * 2.3999632 + float(band) * 1.1 + (h2 - 0.5) * 0.6;
    WaveTrain w;
    w.dir = vec2(cos(angle), sin(angle));
    w.k = 6.2831853 / lambda;
    float tight = band == 0 ? 6.0 : (band == 1 ? 2.0 : 0.5);
    // Moyenne de la fonction d'étalement sur le cercle, pour que la bande garde sa pente totale.
    float mean = band == 0 ? 0.2256 : (band == 1 ? 0.375 : 0.6366);
    float along = pow(0.5 + 0.5 * dot(w.dir, env.wind), tight) / mean;
    // Toute la pente de la bande partagée entre ses trains (la variance d'un sinus vaut la moitié de son carré).
    float bandRms = band == 0 ? rms.x : (band == 1 ? rms.y : rms.z);
    w.slope = bandRms * sqrt(2.0 / float(size) * along) * (0.8 + 0.4 * h3);
    // Pulsation de l'eau profonde : une onde longue va plus vite. Sur un haut-fond la vague s'aplatit (sa vitesse ne
    // dépend pas de la profondeur ici : elle varie d'un pixel à l'autre, et la phase, qui grandit avec le temps, en
    // ferait du bruit).
    w.omega = sqrt(WAVE_G * w.k) * WAVE_PACE;
    float kd = w.k * max(env.depth, 0.05);
    w.slope *= mix(1.0, tanh(1.4 * kd + 0.08), 0.85);
    w.phase = h1 * 37.0 + h2 * 6.2831853;
    w.q = chop;
    return w;
}

/**
 * Les vagues en p (repère monde, plan xz) au temps t, trains first..last-1 sur count. footprint : la taille du pixel
 * à cet endroit (blocs), 0 pour tout garder.
 */
WaveSample waveSum(vec2 p, float t, WaveEnv env, int count, int first, int last, float footprint) {
    ivec3 n;
    vec3 rms;
    waveBands(count, env, n, rms);
    // Part de déplacement horizontal : bornée pour que la somme des plis ne retourne jamais la surface.
    float total = rms.x * sqrt(2.0 * float(n.x)) + rms.y * sqrt(2.0 * float(n.y)) + rms.z * sqrt(2.0 * float(n.z));
    float chop = (0.35 + 0.25 * env.force + 0.40 * env.storm) * WATER_CHOP;
    float q = min(chop * 0.85 / max(total, 1e-3), 3.0);

    WaveSample s;
    s.height = 0.0;
    s.slope = vec2(0.0);
    s.jac = vec3(1.0, 1.0, 0.0);
    s.hess = vec3(0.0);
    s.variance = 0.0;
    s.curv = 0.0;
    float lift = 1.0;
    for (int i = 0; i < WAVE_MAX; i++) {
        if (i >= last) break;
        if (i < first) continue;
        WaveTrain w = waveTrain(i, count, env, n, rms, q);
        float lambda = 6.2831853 / w.k;
        // Trop fin pour le pixel : le train s'efface, sa pente passe dans la rugosité.
        float keep = footprint > 0.0 ? smoothstep(footprint * 2.0, footprint * 4.5, lambda) : 1.0;
        s.variance += 0.5 * w.slope * w.slope * (1.0 - keep * keep);
        if (keep <= 0.0) continue;
        float slope = w.slope * keep;
        // Les crêtes ondulent sur leur longueur : pas de vagues tirées au cordeau.
        vec2 across = vec2(-w.dir.y, w.dir.x);
        float bend = w.k * 0.29 * dot(across, p) + w.phase * 1.3;
        float theta = w.k * dot(w.dir, p) - w.omega * t + w.phase + 0.7 * sin(bend);
        vec2 grad = w.k * w.dir + 0.7 * cos(bend) * w.k * 0.29 * across;
        float sn = sin(theta), cs = cos(theta);
        float amp = slope / w.k;
        s.curv += 0.5 * sq(slope * w.k) * 0.375;
        s.height += amp * sn;
        s.slope += amp * cs * grad;
        s.hess -= amp * sn * vec3(grad.x * grad.x, grad.y * grad.y, grad.x * grad.y);
        float fold = w.q * slope * sn;
        s.jac -= fold * vec3(w.dir.x * w.dir.x, w.dir.y * w.dir.y, w.dir.x * w.dir.y);
        lift -= fold;
    }
    // Gerstner : l'eau qui se resserre sous la crête la redresse, le creux s'étale.
    s.slope /= max(lift, 0.25);
    return s;
}

WaveSample waveMix(WaveSample a, WaveSample b, float w) {
    WaveSample s;
    s.height = mix(a.height, b.height, w);
    s.slope = mix(a.slope, b.slope, w);
    s.jac = mix(a.jac, b.jac, w);
    s.hess = mix(a.hess, b.hess, w);
    s.variance = mix(a.variance, b.variance, w);
    s.curv = mix(a.curv, b.curv, w);
    return s;
}

/**
 * Les vagues d'une eau qui coule : le motif est emporté par le courant, lu en deux phases décalées d'une demi-période
 * qui se relaient (chacune revient en arrière quand l'autre la cache), pour qu'il descende sans fin sans s'étirer.
 */
WaveSample waveField(vec2 p, float t, WaveEnv env, int count, float footprint) {
    float speed = length(env.flow);
    if (speed < 0.02) return waveSum(p, t, env, count, 0, count, footprint);
    const float period = 1.8;
    float a = fract(t / period);
    float b = fract(t / period + 0.5);
    WaveSample sa = waveSum(p - env.flow * a * period, t, env, count, 0, count, footprint);
    WaveSample sb = waveSum(p - env.flow * b * period + vec2(3.7, 1.9), t, env, count, 0, count, footprint);
    return waveMix(sa, sb, abs(2.0 * a - 1.0));
}

/** Le pli de la surface (déterminant du jacobien) : 1 à plat, près de 0 sur une crête qui va déferler. */
float waveFold(WaveSample s) {
    return s.jac.x * s.jac.y - s.jac.z * s.jac.z;
}
