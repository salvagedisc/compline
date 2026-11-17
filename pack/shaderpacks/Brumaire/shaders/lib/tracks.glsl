// Les traces de pas dans le sable et la neige. Une carte de 1024 × 1024 texels au seizième de bloc (64 blocs de
// côté), accrochée au monde et réutilisée en boucle : chaque texel garde la profondeur d'une empreinte (r), l'heure
// où elle a été faite (g, en quarts de seconde), la hauteur du pied (b) et la case de 64 blocs où elle se trouve
// (a), pour ne pas montrer ailleurs une empreinte d'une autre case. prepare1 (program/tracks.csh) y imprime les pas
// des êtres vivants proches ; le terrain s'y creuse (program/solid.fsh).

#define TRACK_SIZE 1024
#define TRACK_RES 16.0

ivec2 trackTexel(vec2 world) { return ivec2(floor(world * TRACK_RES)) & (TRACK_SIZE - 1); }

float trackTile(vec2 world) {
    ivec2 tile = ivec2(floor(world / (float(TRACK_SIZE) / TRACK_RES)));
    return float((tile.x & 15) + (tile.y & 15) * 16);
}

#if defined FSH && TRACKS > 0
uniform sampler2D trackTex;

/** Profondeur d'une empreinte (0..1) en un point du monde, sur un sol qui garde les traces. */
float trackDepth(vec3 world, float now, float fill) {
    vec4 v = texelFetch(trackTex, trackTexel(world.xz), 0);
    if (v.r <= 0.0 || abs(v.a - trackTile(world.xz)) > 0.5) return 0.0;
    if (abs(v.b - mod(world.y, 256.0)) > 0.4) return 0.0;
    float age = now - v.g * 4.0;
    if (age < 0.0) age += 3600.0;
    // Une empreinte s'efface en quelques minutes, plus vite sous la neige qui tombe ou la pluie.
    return v.r * (1.0 - smoothstep(0.0, TRACK_MINUTES * 60.0, age * (1.0 + fill)));
}

/**
 * Creuse un sol de sable ou de neige là où l'on a marché : rend la profondeur (0..1) et penche la normale sur le
 * bord des empreintes, pour qu'elles prennent la lumière d'un côté et l'ombre de l'autre.
 */
float tracksAt(vec3 world, inout vec3 normal, float now, float fill) {
    float d = trackDepth(world, now, fill);
    float step_ = 1.0 / TRACK_RES;
    float dx = trackDepth(world + vec3(step_, 0.0, 0.0), now, fill) - trackDepth(world - vec3(step_, 0.0, 0.0), now, fill);
    float dz = trackDepth(world + vec3(0.0, 0.0, step_), now, fill) - trackDepth(world - vec3(0.0, 0.0, step_), now, fill);
    if (d <= 0.0 && dx == 0.0 && dz == 0.0) return 0.0;
    normal = normalize(normal + vec3(dx, 0.0, dz) * 0.9);
    return d;
}
#endif
