// La glace des rives : dans le grand froid, l'eau calme gèle d'abord le long du bord. Une frange de glace mate part
// de la rive, plus large au cœur de la nuit et au petit matin, avec un bord découpé au pixel de la texture, des
// fêlures droites et un liseré de givre là où elle rencontre l'eau libre. Lu par la surface de l'eau (watershade.glsl).
// Demande voxel.glsl avant lui. Fragments seulement.

#if SHORE_ICE > 0 && defined VOXELS
#ifndef SUN_ANGLE_DECLARED
uniform float sunAngle;
#define SUN_ANGLE_DECLARED
#endif
float shoreIceHash(vec2 p) {
    p = fract(p * vec2(0.1031, 0.1030));
    p += dot(p, p.yx + 33.33);
    return fract((p.x + p.y) * p.x);
}

/** La largeur de la frange, en blocs depuis la rive (0 : pas de glace). */
float shoreIceReach() {
    float cold = 1.0 - smoothstep(0.02, 0.30, airHeat);
    if (cold <= 0.0) return 0.0;
    // La glace prend la nuit et tient jusqu'au milieu de la matinée ; au plein jour elle recule sans disparaître.
    float hour = fract(sunAngle);
    float night = 1.0 - smoothstep(0.02, 0.18, hour) * (1.0 - smoothstep(0.38, 0.48, hour));
    return cold * (0.55 + 0.95 * night);
}

/**
 * La glace en un point de la surface (repère joueur) : x la glace (0..1), y le liseré de givre au bord, z une fêlure.
 * reach : shoreIceReach().
 */
vec3 shoreIce(vec3 pos, float reach) {
    vec3 world = pos + cameraPosition;
    // Au seizième de bloc, comme les pixels des textures.
    vec2 q = (floor(world.xz * 16.0) + 0.5) / 16.0;
    ivec3 c = voxelCell(pos - vec3(0.0, 0.05, 0.0), fract(cameraPosition));
    if (!voxelInside(c)) return vec3(0.0);
    vec2 f = q - floor(world.xz);
    float d = 3.0;
    for (int dz = -2; dz <= 2; dz++) {
        for (int dx = -2; dx <= 2; dx++) {
            if (dx == 0 && dz == 0) continue;
            ivec3 n = c + ivec3(dx, 0, dz);
            if (!voxelInside(n) || (texelFetch(voxelTex, n, 0).r & VOX_SOLID) == 0u) continue;
            vec2 lo = vec2(dx, dz);
            d = min(d, length(max(max(lo - f, f - (lo + 1.0)), 0.0)));
        }
    }
    if (d >= 2.9) return vec3(0.0);
    // Un bord découpé : la glace avance plus loin ici, moins là, par plaques d'un quart et d'un demi-bloc.
    float jag = shoreIceHash(floor(q * 2.0)) * 0.6 + shoreIceHash(floor(q * 4.0) + vec2(17.0, 3.0)) * 0.4 - 0.5;
    float edge = reach + jag * 0.6;
    float ice = step(d, edge);
    float rim = ice * (1.0 - step(d, edge - 2.0 / 16.0));
    // Les fêlures : un ou deux traits droits par bloc, d'un seizième de large.
    vec2 cell = floor(q);
    vec2 r = q - cell - 0.5;
    float a = shoreIceHash(cell + 3.7) * 3.14159;
    float crack = step(abs(dot(r, vec2(cos(a), sin(a)))), 0.5 / 16.0) * step(0.45, shoreIceHash(cell + 9.1));
    float b = shoreIceHash(cell + 21.3) * 3.14159;
    crack = max(crack, step(abs(dot(r, vec2(cos(b), sin(b)))), 0.5 / 16.0) * step(0.75, shoreIceHash(cell + 5.5)));
    // Loin de la caméra, la carte des blocs s'arrête : la glace s'efface avant.
    float near_ = 1.0 - smoothstep(40.0, 56.0, length(pos.xz));
    return vec3(ice, rim, crack * ice) * near_;
}
#endif
