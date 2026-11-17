// La pluie et la neige dessinées par le shader, à la place de celles du jeu : des gouttes et des flocons rangés
// dans des colonnes du monde (un tous les trois quarts de bloc), qui tombent au seizième de bloc comme les pixels
// des textures, penchés par le vent, arrêtés par les toits et les feuillages (abri calculé par shadowcomp1 dans le
// volume de lib/voxel.glsl). Le vent pousse des averses : la pluie forcit et faiblit en passant.
// Demande rain.glsl et clouds.glsl avant lui.

#if SHADER_RAIN > 0
uniform sampler2D shelterTex;
uniform float snowHere;

const float PRECIP_CELL = 0.75;
const float PRECIP_REACH = 26.0;

/** Hauteur (repère monde) au-dessus de laquelle la pluie tombe librement ; rel : point vu de la caméra. */
float shelterAt(vec2 rel) {
    ivec2 c = ivec2(floor(rel + fract(cameraPosition.xz))) + ivec2(VOX_W / 2);
    if (any(lessThan(c, ivec2(0))) || any(greaterThanEqual(c, ivec2(VOX_W)))) return -1e6;
    return texelFetch(shelterTex, c, 0).r + floor(cameraPosition.y);
}

/** Force des averses en un point (repère monde) : des nappes d'une centaine de blocs que le vent fait passer. */
float showerAt(vec2 world) {
    vec2 wind = brumaireLink > 0.5 ? brumaireWind.xy * (0.4 + brumaireWind.z) : vec2(0.3, 0.1);
    vec2 q = (world - wind * frameTimeCounter * 6.0) / 230.0;
    float n = cloudNoise(vec3(q, 0.43)).r * 0.65 + cloudNoise(vec3(q * 2.3, 0.77)).r * 0.35;
    return smoothstep(0.32, 0.62, n);
}

float precipHash(vec2 p, float k) { return fract(sin(dot(p, vec2(127.1, 311.7)) + k * 74.7) * 43758.5453); }

/**
 * Gouttes ou flocons vus le long d'un rayon ; rend leur opacité (0..1). dir : direction (repère monde) ; len :
 * distance de ce qu'on voit ; snow : 0 pluie, 1 neige ; amount : densité (0..1).
 */
float precipitation(vec3 dir, float len, float snow, float amount) {
    if (amount <= 0.001) return 0.0;
    float t0 = 0.35;
    float t1 = min(len, PRECIP_REACH);
    if (t1 <= t0) return 0.0;
    // Le vent penche la chute ; dans un repère cisaillé, chaque goutte tombe droit.
    vec2 wind = brumaireLink > 0.5 ? brumaireWind.xy * brumaireWind.z : vec2(0.2, 0.1);
    vec2 slant = wind * mix(0.35, 0.12, snow);
    vec3 o = vec3(mod(cameraPosition.xz, 4096.0), cameraPosition.y).xzy;
    vec2 q0 = o.xz + slant * o.y;
    vec2 qd = dir.xz + slant * dir.y;
    float speed = mix(11.0, 1.1, snow);
    float period = mix(5.0, 1.6, snow);
    float drop = mix(0.62, 0.0, snow);
    // La largeur d'un pixel à une distance donnée : une goutte n'est jamais plus fine qu'un pixel.
    float pixel = 2.0 / (gbufferProjection[1][1] * viewHeight);
    float size = 1.0 / 16.0;
    float density = amount * mix(0.55, 0.45, snow);

    // Traversée des colonnes du sol cisaillé que croise le rayon (DDA).
    vec2 qd2 = vec2(abs(qd.x) < 1e-5 ? 1e-5 : qd.x, abs(qd.y) < 1e-5 ? 1e-5 : qd.y);
    vec2 cellSize = vec2(PRECIP_CELL);
    vec2 p = q0 + qd * t0;
    vec2 cell = floor(p / cellSize);
    vec2 stepDir = sign(qd2);
    vec2 tDelta = abs(cellSize / qd2);
    vec2 next = ((cell + max(stepDir, 0.0)) * cellSize - q0) / qd2;
    float t = t0;
    float clear = 1.0;
    for (int i = 0; i < 48; i++) {
        float tExit = min(min(next.x, next.y), t1);
        float h = precipHash(cell, 1.0);
        if (h < density) {
            vec2 centre = (cell + 0.15 + 0.7 * vec2(precipHash(cell, 2.0), precipHash(cell, 3.0))) * cellSize;
            float phase = precipHash(cell, 4.0) * period;
            float tc = t;
            if (snow > 0.5) {
                // Les flocons dérivent un peu d'un côté et de l'autre en tombant.
                centre += 0.18 * vec2(sin(frameTimeCounter * 1.3 + phase * 4.0), cos(frameTimeCounter * 0.9 + phase * 3.0));
            }
            float dd = dot(qd, qd);
            tc = clamp(dd > 1e-8 ? dot(centre - q0, qd) / dd : t, t, tExit);
            vec2 off = q0 + qd * tc - centre;
            float y = o.y + dir.y * tc;
            float width = mix(tc * pixel * 0.6 + 0.004, max(size * 0.5, tc * pixel * 0.75), snow);
            if (dot(off, off) < width * width) {
                // Le long de la colonne, une goutte tous les period blocs, qui tombe ; au seizième de bloc.
                float yy = floor((y + frameTimeCounter * speed + phase) * 16.0) / 16.0;
                float f = mod(yy, period);
                float on = snow > 0.5 ? step(f, max(size, width * 2.0)) : step(f, drop) * (0.55 + 0.45 * f / drop);
                if (on > 0.0) {
                    vec2 ground = centre - slant * y - o.xz;
                    if (y > shelterAt(ground)) {
                        float fade = (1.0 - smoothstep(PRECIP_REACH * 0.55, PRECIP_REACH, tc)) * smoothstep(mix(1.0, 1.8, snow), mix(4.0, 5.0, snow), tc);
                        clear *= 1.0 - on * fade * mix(0.32, 0.85, snow);
                    }
                }
            }
        }
        if (tExit >= t1) break;
        t = tExit;
        if (next.x < next.y) { next.x += tDelta.x; cell.x += stepDir.x; }
        else { next.y += tDelta.y; cell.y += stepDir.y; }
    }
    return 1.0 - clear;
}

/** Couleur des gouttes et des flocons : la lumière du ciel, un peu de soleil, rien de bleu. */
vec3 precipColor(float snow) {
    vec3 light = gAmbient * 1.35 + gLightColor * 0.14 + vec3(0.02);
    return light * mix(vec3(0.82, 0.85, 0.88), vec3(1.05), snow);
}
#endif
