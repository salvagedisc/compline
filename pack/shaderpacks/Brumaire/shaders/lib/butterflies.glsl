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

#if DRAGONFLIES > 0 && SHADER_RAIN > 0 && defined VOXELS
const float DRAGONFLY_CELL = 4.0;

/**
 * Les libellules : au-dessus de l'eau seulement, les mêmes jours que les papillons. Une colonne de quatre blocs sur six
 * en porte une, à trente centimètres à un bloc au-dessus de la surface ; elle reste en vol sur place, puis file d'un
 * trait vers un autre point. Vue de côté : un corps long de cinq pixels (bleu, vert ou rouge) et des ailes pâles,
 * translucides, qui vibrent. Rend sa couverture, sa distance et sa couleur.
 */
float dragonfliesAlong(vec3 dir, float t1, float amount, out float dist, out vec3 tint) {
    dist = 0.0;
    tint = vec3(0.0);
    if (t1 <= 0.5 || amount <= 0.01) return 0.0;
    vec3 eye = cameraPosition / DRAGONFLY_CELL;
    vec3 camFloor = floor(eye);
    vec3 o = eye - camFloor;
    vec3 d = vec3(abs(dir.x) < 1e-5 ? 1e-5 : dir.x, abs(dir.y) < 1e-5 ? 1e-5 : dir.y, abs(dir.z) < 1e-5 ? 1e-5 : dir.z);
    vec3 cell = floor(o);
    vec3 stepDir = sign(d);
    vec3 tDelta = abs(1.0 / d);
    vec3 next = (cell + max(stepDir, 0.0) - o) / d;
    float tc = t1 / DRAGONFLY_CELL;
    float time = frameTimeCounter;
    vec3 right = normalize(cross(dir, vec3(0.0, 1.0, 0.0)) + vec3(1e-4, 0.0, 0.0));
    vec3 up = cross(right, dir);
    for (int i = 0; i < 24; i++) {
        float tExit = min(min(next.x, next.y), min(next.z, tc));
        vec3 wc = cell + camFloor;
        float h = moteHash(vec3(wc.x, 0.0, wc.z), 71.0);
        if (h < 0.17 * amount) {
            // Sur place, puis d'un trait vers le point suivant : deux points tirés par étape, le trajet en un quart d'étape.
            float clock = time * 0.45 + h * 13.0;
            float leg = floor(clock);
            float go = smoothstep(0.75, 1.0, fract(clock));
            vec3 a = vec3(moteHash(vec3(wc.x, leg, wc.z), 72.0), moteHash(vec3(wc.x, leg, wc.z), 73.0), moteHash(vec3(wc.x, leg, wc.z), 74.0));
            vec3 b = vec3(moteHash(vec3(wc.x, leg + 1.0, wc.z), 72.0), moteHash(vec3(wc.x, leg + 1.0, wc.z), 73.0), moteHash(vec3(wc.x, leg + 1.0, wc.z), 74.0));
            vec3 spot = mix(a, b, go);
            vec3 at = vec3((wc.x + 0.15 + 0.7 * spot.x) * DRAGONFLY_CELL, 0.0, (wc.z + 0.15 + 0.7 * spot.z) * DRAGONFLY_CELL);
            float surface = shelterAt(at.xz - cameraPosition.xz);
            at.y = surface - 0.1 + 0.3 + 0.7 * spot.y + 0.02 * sin(time * 9.0 + h * 40.0);
            if (floor(at.y / DRAGONFLY_CELL) == wc.y) {
                ivec3 below = voxelCell(vec3(at.x, surface - 0.5, at.z) - cameraPosition, fract(cameraPosition));
                if (voxelInside(below) && (texelFetch(voxelTex, below, 0).r & VOX_WATER) != 0u) {
                    vec3 rel = at - cameraPosition;
                    float s = dot(rel, dir);
                    if (s > 0.6 && s < t1) {
                        vec3 miss = dir * s - rel;
                        vec2 q = floor(vec2(dot(miss, right), dot(miss, up)) * 12.0) + 0.5;
                        // Tournée vers la gauche ou la droite selon sa colonne.
                        if (moteHash(vec3(wc.x, 2.0, wc.z), 75.0) < 0.5) q.x = -q.x;
                        bool body = abs(q.y) < 1.0 && q.x > -3.0 && q.x < 3.0;
                        bool wings = q.y > 0.0 && q.y < 2.0 && q.x > -2.0 && q.x < 1.0;
                        if (body || wings) {
                            dist = s;
                            float k = moteHash(vec3(wc.x, 3.0, wc.z), 76.0);
                            vec3 c = k < 0.5 ? vec3(0.04, 0.18, 0.75) : (k < 0.8 ? vec3(0.06, 0.45, 0.12) : vec3(0.60, 0.06, 0.03));
                            tint = body ? (q.x > 2.0 ? c * 0.4 : c) : vec3(0.75, 0.80, 0.85);
                            float fade = 1.0 - smoothstep(t1 * 0.7, t1, s);
                            return (body ? 1.0 : 0.35 + 0.2 * sin(time * 60.0)) * fade;
                        }
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

#if MOTHS > 0 && defined VOXELS
/** Combien de papillons de nuit (0..1) : la nuit, un air doux, sans pluie là où l'on est. */
float mothAmount() {
    float night = 1.0 - smoothstep(0.08, 0.3, gDay);
    float warm = smoothstep(0.25, 0.45, airHeat) * (1.0 - smoothstep(1.4, 1.7, airHeat));
    float open = float(eyeBrightnessSmooth.y) / 240.0;
    return night * warm * (1.0 - smoothstep(0.1, 0.4, rainStrength * open));
}

/**
 * Les papillons de nuit autour des lumières : le long d'un rayon jusqu'à t1 (24 cases d'un bloc au plus), chaque case
 * qui contient une lumière forte (torche, lanterne, feu de camp…) ou se tient juste au-dessus d'elle porte un ou deux
 * papillons de nuit qui tournent vite et de travers autour de la flamme, sans quitter la case. Pâles, éclairés par la
 * lumière qu'ils visitent. Rend leur couverture, leur distance et leur couleur (linéaire).
 */
float mothsAlong(vec3 dir, float t1, float amount, out float dist, out vec3 tint) {
    dist = 0.0;
    tint = vec3(0.0);
    if (t1 <= 0.5 || amount <= 0.01) return 0.0;
    vec3 camFloor = floor(cameraPosition);
    vec3 o = cameraPosition - camFloor;
    vec3 d = vec3(abs(dir.x) < 1e-5 ? 1e-5 : dir.x, abs(dir.y) < 1e-5 ? 1e-5 : dir.y, abs(dir.z) < 1e-5 ? 1e-5 : dir.z);
    vec3 cell = floor(o);
    vec3 stepDir = sign(d);
    vec3 tDelta = abs(1.0 / d);
    vec3 next = (cell + max(stepDir, 0.0) - o) / d;
    float time = frameTimeCounter;
    vec3 right = normalize(cross(dir, vec3(0.0, 1.0, 0.0)) + vec3(1e-4, 0.0, 0.0));
    vec3 up = cross(right, dir);
    vec3 camFract = fract(cameraPosition);
    for (int i = 0; i < 24; i++) {
        float tExit = min(min(next.x, next.y), min(next.z, t1));
        vec3 wc = cell + camFloor;
        ivec3 vc = voxelCell(wc + 0.5 - cameraPosition, camFract);
        if (voxelInside(vc) && voxelInside(vc - ivec3(0, 1, 0))) {
            uint here = texelFetch(voxelTex, vc, 0).r;
            uint under = texelFetch(voxelTex, vc - ivec3(0, 1, 0), 0).r;
            bool own = (here & 15u) >= 11u;
            uint lamp = own ? here : under;
            // Ni au-dessus de la lave, ni sous l'eau.
            if ((lamp & 15u) >= 11u && ((lamp >> 4) & 15u) != 2u && (here & VOX_WATER) == 0u) {
                // La lumière (sa case) et une chance sur trois qu'aucun papillon ne la visite.
                vec3 lc = own ? wc : wc - vec3(0.0, 1.0, 0.0);
                float hl = moteHash(lc, 101.0);
                int count = hl < 0.33 * amount ? 0 : (hl < 0.75 ? 1 : 2);
                for (int k = 0; k < 2; k++) {
                    if (k >= count) break;
                    float ph = moteHash(lc + vec3(float(k) * 3.1, 0.0, 0.0), 102.0) * 6.2831853;
                    float speed = (3.0 + 2.5 * moteHash(lc + float(k), 103.0)) * (k == 0 ? 1.0 : -1.0);
                    float a = time * speed + ph + 0.8 * sin(time * 7.3 + ph * 2.0);
                    float rad = 0.22 + 0.16 * (0.5 + 0.5 * sin(time * 2.1 + ph));
                    vec3 at = wc + vec3(0.5 + cos(a) * rad, own ? 0.62 : 0.30, 0.5 + sin(a) * rad);
                    at.y += 0.18 * sin(time * 5.7 + ph * 1.7) + 0.06 * sin(time * 23.0 + ph);
                    at = clamp(at, wc + 0.04, wc + 0.96);
                    vec3 rel = at - cameraPosition;
                    float s = dot(rel, dir);
                    if (s > 0.4 && s < t1) {
                        vec3 miss = dir * s - rel;
                        // Des pixels d'un vingtième de bloc : un petit papillon de nuit, ailes en triangle qui battent vite.
                        vec2 q = floor(vec2(dot(miss, right), dot(miss, up)) * 20.0) + 0.5;
                        float ax = abs(q.x);
                        float beat = abs(sin(time * 31.0 + ph * 5.0));
                        float span = 1.0 + floor(1.99 * beat);
                        bool body = ax < 0.6 && abs(q.y) < 1.6;
                        bool wing = ax > 0.5 && ax < span + 0.6 && q.y > -1.6 && q.y < 1.0 - (ax - 1.0) * 0.5;
                        if (body || wing) {
                            dist = s;
                            vec3 glow = lightTint(int((lamp >> 4) & 15u));
                            tint = (body ? vec3(0.22, 0.18, 0.14) : vec3(0.66, 0.60, 0.50)) * glow * 1.3;
                            return 1.0 - smoothstep(t1 * 0.7, t1, s);
                        }
                    }
                }
            }
        }
        if (tExit >= t1) break;
        if (next.x < next.y && next.x < next.z) { next.x += tDelta.x; cell.x += stepDir.x; }
        else if (next.y < next.z) { next.y += tDelta.y; cell.y += stepDir.y; }
        else { next.z += tDelta.z; cell.z += stepDir.z; }
    }
    return 0.0;
}
#endif
