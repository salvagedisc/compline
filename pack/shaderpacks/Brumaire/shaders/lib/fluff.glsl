// Les duvets au vent : par les journées douces et sèches, des aigrettes de graines (pissenlit, peuplier, chardon)
// flottent au-dessus des prés, portées par le vent, et s'allument quand le soleil les prend à contre-jour.
// Demande precip.glsl (shelterAt) et motes.glsl (moteHash) avant lui.

#if SEED_FLUFF > 0 && SHADER_RAIN > 0
const float FLUFF_CELL = 2.0;

/** Combien de duvets ici (0..1) : le jour, un air doux et pas trop humide, à découvert, ni pluie ni neige. */
float fluffAmount() {
    float day = smoothstep(0.15, 0.45, gDay);
    float mild = smoothstep(0.5, 0.7, airHeat) * (1.0 - smoothstep(1.2, 1.5, airHeat));
    float dry = 1.0 - smoothstep(0.75, 0.95, airHumid);
    float open = smoothstep(0.4, 0.8, float(eyeBrightnessSmooth.y) / 240.0);
    return day * mild * dry * open * (1.0 - smoothstep(0.05, 0.3, rainStrength)) * (1.0 - snowHere);
}

/**
 * L'éclat des duvets le long d'un rayon jusqu'à t1 (blocs), 0..1, et la distance du plus proche dans dist : une case
 * de deux blocs sur dix en porte un, qui dérive dans le sens du vent et se balance, d'un demi-bloc à quatre blocs
 * au-dessus du sol (32 cases au plus).
 */
float fluffAlong(vec3 dir, float t1, float amount, out float dist) {
    dist = 0.0;
    if (t1 <= 0.5 || amount <= 0.01) return 0.0;
    vec3 eye = cameraPosition / FLUFF_CELL;
    vec3 camFloor = floor(eye);
    vec3 o = eye - camFloor;
    vec3 d = vec3(abs(dir.x) < 1e-5 ? 1e-5 : dir.x, abs(dir.y) < 1e-5 ? 1e-5 : dir.y, abs(dir.z) < 1e-5 ? 1e-5 : dir.z);
    vec3 cell = floor(o);
    vec3 stepDir = sign(d);
    vec3 tDelta = abs(1.0 / d);
    vec3 next = (cell + max(stepDir, 0.0) - o) / d;
    float tc = t1 / FLUFF_CELL;
    float pixel = 2.0 / (gbufferProjection[1][1] * viewHeight);
    float time = frameTimeCounter;
    vec2 wind = brumaireLink > 0.5 ? brumaireWind.xy * (0.3 + brumaireWind.z) : vec2(0.25, 0.1);
    for (int i = 0; i < 32; i++) {
        float tExit = min(min(next.x, next.y), min(next.z, tc));
        vec3 wc = cell + camFloor;
        float h = moteHash(wc, 51.0);
        if (h < 0.1 * amount) {
            // Le vent l'emporte en boucle à travers sa case ; elle monte et descend doucement.
            vec2 carry = fract(wind * time * 0.12 + h * 7.0) - 0.5;
            vec3 sway = vec3(sin(time * 0.7 + h * 40.0) * 0.15, sin(time * 0.45 + h * 60.0) * 0.25, cos(time * 0.6 + h * 30.0) * 0.15);
            vec3 at = (wc + 0.5 + vec3(carry.x * 0.7, 0.0, carry.y * 0.7) + sway) * FLUFF_CELL;
            vec3 rel = at - cameraPosition;
            float above = at.y - shelterAt(rel.xz);
            if (above > 0.5 && above < 4.0) {
                float s = dot(rel, dir);
                if (s > 0.6 && s < t1) {
                    float miss = length(dir * s - rel);
                    float core = max(0.8 / 16.0, s * pixel * 0.6);
                    // Une boule floue : un cœur et un voile autour.
                    float puff = exp(-miss * miss / (core * core * 2.5)) * 0.8 + step(miss, core * 0.5) * 0.2;
                    if (puff > 0.02) {
                        dist = s;
                        // Elle entre et sort de sa case en s'effaçant.
                        float edge = 1.0 - smoothstep(0.3, 0.5, max(abs(carry.x), abs(carry.y)));
                        return puff * edge * (1.0 - smoothstep(t1 * 0.6, t1, s));
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
