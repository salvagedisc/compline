// Les papillons : par les journées douces ou chaudes, au-dessus des prés, de petits papillons (blancs, jaunes,
// orangés, bleus) volettent à hauteur de fleurs, chacun sur sa boucle paresseuse, ailes battantes. Dessinés en gros pixels
// (un douzième de bloc), face à l'œil, comme les sprites du jeu. Demande precip.glsl (shelterAt) et motes.glsl (moteHash).

#if BUTTERFLIES > 0 && SHADER_RAIN > 0
const float BUTTERFLY_CELL = 3.0;

/** Combien de papillons ici (0..1) : le plein jour, un air chaud mais pas brûlant, pas désertique, à découvert. */
float butterflyAmount() {
    float day = smoothstep(0.2, 0.5, gDay);
    float warm = smoothstep(0.3, 0.5, airHeat) * (1.0 - smoothstep(1.3, 1.6, airHeat));
    float green = smoothstep(0.2, 0.45, airHumid);
    float open = smoothstep(0.4, 0.8, float(eyeBrightnessSmooth.y) / 240.0);
    return day * warm * green * open * (1.0 - smoothstep(0.05, 0.3, rainStrength)) * (1.0 - snowHere);
}

/**
 * Le papillon le plus proche le long d'un rayon jusqu'à t1 (blocs) : rend sa couverture (0 ou 1, au pixel), sa
 * distance dans dist et sa couleur (linéaire) dans tint. Une colonne de trois blocs sur huit en porte un ; il vole
 * entre un demi-bloc et un bloc et demi au-dessus du sol (28 cases au plus).
 */
float butterfliesAlong(vec3 dir, float t1, float amount, out float dist, out vec3 tint) {
    dist = 0.0;
    tint = vec3(0.0);
    if (t1 <= 0.5 || amount <= 0.01) return 0.0;
    vec3 eye = cameraPosition / BUTTERFLY_CELL;
    vec3 camFloor = floor(eye);
    vec3 o = eye - camFloor;
    vec3 d = vec3(abs(dir.x) < 1e-5 ? 1e-5 : dir.x, abs(dir.y) < 1e-5 ? 1e-5 : dir.y, abs(dir.z) < 1e-5 ? 1e-5 : dir.z);
    vec3 cell = floor(o);
    vec3 stepDir = sign(d);
    vec3 tDelta = abs(1.0 / d);
    vec3 next = (cell + max(stepDir, 0.0) - o) / d;
    float tc = t1 / BUTTERFLY_CELL;
    float time = frameTimeCounter;
    vec3 right = normalize(cross(dir, vec3(0.0, 1.0, 0.0)) + vec3(1e-4, 0.0, 0.0));
    vec3 up = cross(right, dir);
    for (int i = 0; i < 28; i++) {
        float tExit = min(min(next.x, next.y), min(next.z, tc));
        vec3 wc = cell + camFloor;
        float h = moteHash(vec3(wc.x, 0.0, wc.z), 61.0);
        if (h < 0.125 * amount) {
            float ph = h * 97.0;
            // Une boucle lente dans sa colonne, avec des écarts brusques.
            vec2 loop = vec2(sin(time * 0.23 + ph) * 0.32 + sin(time * 0.93 + ph * 1.3) * 0.07,
                             cos(time * 0.19 + ph * 0.7) * 0.32 + cos(time * 1.10 + ph) * 0.07);
            vec3 at = vec3((wc.x + 0.5 + loop.x) * BUTTERFLY_CELL, 0.0, (wc.z + 0.5 + loop.y) * BUTTERFLY_CELL);
            float ground = shelterAt(at.xz - cameraPosition.xz);
            float beat = time * 13.0 + ph;
            at.y = ground + 0.5 + 0.5 * (0.5 + 0.5 * sin(time * 0.37 + ph)) + 0.06 * sin(beat);
            // Il n'appartient qu'à la case qui le contient, sans quoi deux cases du rayon le dessineraient.
            if (floor(at.y / BUTTERFLY_CELL) == wc.y) {
                vec3 rel = at - cameraPosition;
                float s = dot(rel, dir);
                if (s > 0.6 && s < t1) {
                    vec3 miss = dir * s - rel;
                    vec2 q = floor(vec2(dot(miss, right), dot(miss, up)) * 12.0) + 0.5;
                    float half_ = 1.0 + floor(2.99 * abs(sin(beat)));
                    float ax = abs(q.x);
                    bool body = ax < 1.0 && abs(q.y) < 2.0;
                    bool wing = ax > 0.5 && ax < half_ + 0.6 && q.y > -2.0 && q.y < 2.0
                        && !(ax > half_ && q.y < -1.0);
                    if (body || wing) {
                        dist = s;
                        float k = moteHash(vec3(wc.x, 1.0, wc.z), 62.0);
                        vec3 c = k < 0.4 ? vec3(0.80, 0.80, 0.72) : (k < 0.65 ? vec3(0.85, 0.62, 0.08)
                            : (k < 0.85 ? vec3(0.85, 0.25, 0.03) : vec3(0.12, 0.28, 0.85)));
                        tint = body ? vec3(0.02, 0.015, 0.01) : c;
                        return 1.0 - smoothstep(t1 * 0.7, t1, s);
                    }
                }
            }
        }
        if (tExit >= tc) break;
        if (next.x < next.y && next.x < next.z) { next.x += tDelta.x; cell.x += stepDir.x; }
        else if (next.y < next.z) { next.y += tDelta.y; cell.y += stepDir.y; }
        else { next.z += tDelta.z; cell.z += stepDir.z; }
    }
    return 0.0;
}
#endif
