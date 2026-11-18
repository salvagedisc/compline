// Les lucioles : les nuits douces, dans les herbes et au bord des bois humides, des points jaune-vert qui flottent
// à hauteur d'homme au-dessus du sol et s'allument par éclairs lents. Le sol de chaque colonne est l'abri de la pluie
// (shadowcomp1, lib/precip.glsl). Demande precip.glsl et motes.glsl (moteHash) avant lui.

#if FIREFLIES > 0 && SHADER_RAIN > 0
const float FIREFLY_CELL = 2.0;

/** Combien de lucioles cette nuit, ici (0..1) : nuit, biome tiède et pas sec, ni pluie ni neige, à l'air libre. */
float fireflySeason() {
    float night = 1.0 - smoothstep(0.0, 0.25, gDay);
    float mild = smoothstep(0.45, 0.6, airHeat) * (1.0 - smoothstep(1.1, 1.4, airHeat));
    float wet = smoothstep(0.3, 0.6, airHumid);
    float open = smoothstep(0.3, 0.7, float(eyeBrightnessSmooth.y) / 240.0);
    return night * mild * wet * open * (1.0 - smoothstep(0.05, 0.3, rainStrength)) * (1.0 - snowHere);
}

/**
 * L'éclat des lucioles le long d'un rayon jusqu'à t1 (blocs) : la somme de leurs halos (une case de deux blocs sur
 * huit en porte une, 36 cases au plus). Chacune vole lentement dans sa case, entre un demi-bloc et trois blocs au-dessus
 * du sol, et s'allume une demi-seconde toutes les quelques secondes.
 */
float firefliesAlong(vec3 dir, float t1, float amount) {
    if (t1 <= 0.5 || amount <= 0.01) return 0.0;
    vec3 eye = cameraPosition / FIREFLY_CELL;
    vec3 camFloor = floor(eye);
    vec3 o = eye - camFloor;
    vec3 d = vec3(abs(dir.x) < 1e-5 ? 1e-5 : dir.x, abs(dir.y) < 1e-5 ? 1e-5 : dir.y, abs(dir.z) < 1e-5 ? 1e-5 : dir.z);
    vec3 cell = floor(o);
    vec3 stepDir = sign(d);
    vec3 tDelta = abs(1.0 / d);
    vec3 next = (cell + max(stepDir, 0.0) - o) / d;
    float tc = t1 / FIREFLY_CELL;
    float pixel = 2.0 / (gbufferProjection[1][1] * viewHeight);
    float time = frameTimeCounter;
    float glow = 0.0;
    for (int i = 0; i < 36; i++) {
        float tExit = min(min(next.x, next.y), min(next.z, tc));
        vec3 wc = cell + camFloor;
        float h = moteHash(wc, 11.0);
        if (h < 0.125 * amount) {
            vec3 wander = vec3(sin(time * 0.31 + h * 50.0), sin(time * 0.53 + h * 80.0) * 0.6, cos(time * 0.27 + h * 65.0)) * 0.35;
            vec3 at = (wc + 0.5 + wander) * FIREFLY_CELL;
            vec3 rel = at - cameraPosition;
            float ground = shelterAt(rel.xz);
            float above = at.y - ground;
            if (above > 0.5 && above < 3.0) {
                float s = dot(rel, dir);
                if (s > 0.5 && s < t1) {
                    float miss = length(dir * s - rel);
                    // Un cœur d'un pixel de bloc, et un halo qui ne grandit pas avec la distance.
                    float core = max(0.6 / 16.0, s * pixel * 0.7);
                    float halo = exp(-miss * miss / (core * core * 9.0)) * 0.35 + step(miss, core);
                    float flash = pow(max(sin(time * (0.55 + 0.4 * moteHash(wc, 12.0)) + h * 90.0), 0.0), 3.0);
                    glow += halo * flash * (1.0 - smoothstep(t1 * 0.6, t1, s));
                }
            }
        }
        if (tExit >= tc) break;
        if (next.x < next.y && next.x < next.z) { next.x += tDelta.x; cell.x += stepDir.x; }
        else if (next.y < next.z) { next.y += tDelta.y; cell.y += stepDir.y; }
        else { next.z += tDelta.z; cell.z += stepDir.z; }
    }
    return glow;
}
#endif
