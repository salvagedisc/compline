// Poussières en suspension : une par case d'un bloc tirée au sort, d'un pixel de bloc, qui dérive dans sa case et
// scintille. Sous l'eau (neige marine, underwater.glsl) et dans l'air là où le soleil passe (mist.fsh).

float moteHash(vec3 p, float k) { return fract(sin(dot(p, vec3(127.1, 311.7, 74.7)) + k * 19.19) * 43758.5453); }

/**
 * La poussière la plus proche le long d'un rayon jusqu'à t1 (blocs) : son éclat (0..1), et sa distance dans dist.
 * chance : part des cases qui en portent une ; drift : écart de sa dérive (blocs) ; far : distance où elle s'efface,
 * de far/2 à far. Les cases sont parcourues une à une (48 au plus).
 */
float moteAlong(vec3 dir, float t1, float chance, float drift, float far, out float dist) {
    dist = 0.0;
    if (t1 <= 0.3) return 0.0;
    vec3 camFloor = floor(cameraPosition);
    vec3 o = cameraPosition - camFloor;
    vec3 d = vec3(abs(dir.x) < 1e-5 ? 1e-5 : dir.x, abs(dir.y) < 1e-5 ? 1e-5 : dir.y, abs(dir.z) < 1e-5 ? 1e-5 : dir.z);
    vec3 cell = floor(o + dir * 0.3);
    vec3 stepDir = sign(d);
    vec3 tDelta = abs(1.0 / d);
    vec3 next = (cell + max(stepDir, 0.0) - o) / d;
    float pixel = 2.0 / (gbufferProjection[1][1] * viewHeight);
    vec3 side = normalize(cross(dir, vec3(0.0, 1.0, 0.0)) + vec3(1e-4, 0.0, 0.0));
    vec3 up = cross(side, dir);
    float time = frameTimeCounter;
    for (int i = 0; i < 48; i++) {
        float tExit = min(min(next.x, next.y), min(next.z, t1));
        vec3 wc = cell + camFloor;
        float h = moteHash(wc, 1.0);
        if (h < chance) {
            vec3 wander = drift * vec3(sin(time * 0.37 + h * 40.0), sin(time * 0.23 + h * 70.0), cos(time * 0.31 + h * 55.0));
            vec3 p = cell + 0.22 + 0.56 * vec3(moteHash(wc, 2.0), moteHash(wc, 3.0), moteHash(wc, 4.0)) + wander - o;
            float s = dot(p, dir);
            if (s > 0.3 && s < t1) {
                vec3 off = dir * s - p;
                float size = max(0.35 / 16.0, s * pixel * 0.5);
                if (max(abs(dot(off, side)), abs(dot(off, up))) < size) {
                    dist = s;
                    float twinkle = 0.55 + 0.45 * sin(time * 1.7 + h * 90.0);
                    return twinkle * (1.0 - smoothstep(far * 0.5, far, s)) * smoothstep(0.8, 2.5, s);
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
