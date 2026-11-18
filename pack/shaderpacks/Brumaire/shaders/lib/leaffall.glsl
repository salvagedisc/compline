// Les feuilles qui tombent : sous les arbres, de temps en temps, une feuille se détache, descend en se balançant et en
// tournant sur elle-même, couchée par le vent, de la couleur du feuillage d'où elle tombe. Un feuillage roussi (à
// l'automne) en perd bien davantage. Le feuillage de chaque colonne est tiré du volume des blocs par shadowcomp2.
// Demande voxel.glsl et motes.glsl (moteHash) avant lui.

#if FALLING_LEAVES > 0 && defined VOXELS
uniform sampler2D canopyTex;

/**
 * Le feuillage au-dessus d'un point (repère monde) : sa couleur (sRGB) et 1 si le point est sous lui, à moins de
 * dix blocs de son dessous (shadowcomp2, program/canopy.csh). Le vent emporte les feuilles un peu au-delà du bord :
 * une colonne voisine de deux blocs au plus compte aussi.
 */
vec4 canopyAbove(vec3 at) {
    ivec2 c0 = ivec2(floor(at.xz - cameraPosition.xz + fract(cameraPosition.xz))) + ivec2(VOX_W / 2);
    for (int k = 0; k < 9; k++) {
        ivec2 c = c0 + (k == 0 ? ivec2(0) : ivec2(k % 3 - 1, k / 3 - 1) * 2);
        if (any(lessThan(c, ivec2(0))) || any(greaterThanEqual(c, ivec2(VOX_W)))) continue;
        vec4 v = texelFetch(canopyTex, c, 0);
        float under = v.a + floor(cameraPosition.y);
        if (v.a < -999.0 || at.y > under + 0.5 || at.y < under - 10.0) continue;
        return vec4(v.rgb, 1.0);
    }
    return vec4(0.0);
}

/**
 * La feuille la plus proche le long d'un rayon jusqu'à t1 (blocs) : 1 si le rayon en touche une, sa distance dans dist
 * et sa couleur (linéaire) dans tint. Chaque colonne d'un bloc tirée au sort porte une feuille qui descend en boucle
 * sur une tranche de huit blocs, si elle se trouve sous un feuillage ; les cases sont parcourues une à une (40 au plus).
 */
float leafAlong(vec3 dir, float t1, out float dist, out vec3 tint) {
    dist = 0.0;
    tint = vec3(0.0);
    if (t1 <= 0.4) return 0.0;
    vec3 camFloor = floor(cameraPosition);
    vec3 o = cameraPosition - camFloor;
    vec3 d = vec3(abs(dir.x) < 1e-5 ? 1e-5 : dir.x, abs(dir.y) < 1e-5 ? 1e-5 : dir.y, abs(dir.z) < 1e-5 ? 1e-5 : dir.z);
    vec3 cell = floor(o + dir * 0.4);
    vec3 stepDir = sign(d);
    vec3 tDelta = abs(1.0 / d);
    vec3 next = (cell + max(stepDir, 0.0) - o) / d;
    float pixel = 2.0 / (gbufferProjection[1][1] * viewHeight);
    vec3 side = normalize(cross(dir, vec3(0.0, 1.0, 0.0)) + vec3(1e-4, 0.0, 0.0));
    vec3 up = cross(side, dir);
    float time = frameTimeCounter;
    vec2 wind = brumaireLink > 0.5 ? brumaireWind.xy * (0.3 + brumaireWind.z) : vec2(0.25, 0.10);
    for (int i = 0; i < 40; i++) {
        float tExit = min(min(next.x, next.y), min(next.z, t1));
        vec3 wc = cell + camFloor;
        vec3 col = vec3(wc.x, 0.0, wc.z);
        float h = moteHash(col, 5.0);
        if (h < 0.42) {
            // La tranche de huit blocs où cette feuille descend, à 0,7 bloc par seconde.
            float band = floor(wc.y / 8.0) * 8.0;
            float y = band + 8.0 - mod(time * 0.7 + moteHash(col, 6.0) * 8.0, 8.0);
            if (floor(y) == wc.y) {
                vec4 canopy = canopyAbove(vec3(wc.x, y, wc.z));
                // Un feuillage roussi en perd davantage : on garde plus de colonnes sous lui.
                float autumn = smoothstep(0.02, 0.15, canopy.r - canopy.g);
                if (canopy.a > 0.0 && h < 0.42 * (0.5 + 0.5 * autumn)) {
                    float sway = sin(time * 1.3 + h * 40.0);
                    // Le balancement et le vent la poussent de côté, sans la sortir de sa case : le rayon ne la cherche
                    // que dans la case où elle se trouve.
                    vec2 drift = clamp(vec2(sway * 0.22, cos(time * 0.9 + h * 25.0) * 0.16) + wind * 0.25, -0.34, 0.34);
                    vec3 p = vec3(wc.x + 0.5 + drift.x, y, wc.z + 0.5 + drift.y) - camFloor - o;
                    float s = dot(p, dir);
                    if (s > 0.4 && s < t1) {
                        vec3 off = dir * s - p;
                        // Une feuille de deux pixels de bloc, qui tourne : vue tantôt de face, tantôt par la tranche.
                        float size = max(1.0 / 16.0, s * pixel * 0.6);
                        float turn = 0.25 + 0.75 * abs(sin(time * 2.6 + h * 60.0));
                        vec2 uv = vec2(dot(off, side) / (size * turn), dot(off, up) / size);
                        // Deux pixels sur deux, un coin ôté : la forme d'une feuille, une moitié plus claire que l'autre.
                        bool cut = (uv.x > 0.0) != (uv.y > 0.0) && (uv.x > 0.0) == (sway > 0.0);
                        if (all(lessThan(abs(uv), vec2(1.0))) && !cut) {
                            dist = s;
                            tint = toLinear(canopy.rgb) * (0.75 + 0.5 * moteHash(col, 7.0)) * (uv.y > 0.0 ? 1.15 : 0.85);
                            return (1.0 - smoothstep(t1 * 0.6, t1, s));
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
