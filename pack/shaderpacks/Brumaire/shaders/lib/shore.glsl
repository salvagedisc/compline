// Le sol mouillé au bord de l'eau : la terre, la pierre et le sable qui touchent une eau sont sombres et luisants
// jusqu'à un bloc et demi de la rive, et sur les parois jusqu'à une ligne un peu au-dessus de l'eau. Cette ligne
// monte et descend lentement, comme une eau qui vient lécher la rive, et son bord suit les pixels de la texture.
// La carte des rives vient de shadowcomp3 (program/shore.csh). Demande voxel.glsl et clouds.glsl avant lui.

#if SHORE_WET > 0 && defined VOXELS
uniform sampler2D shoreTex;

/** La part mouillée par l'eau voisine d'un point (repère monde) de normale normal, à l'instant t (0..1). */
float shoreWet(vec3 world, vec3 normal, float t) {
    vec2 rel = world.xz - floor(cameraPosition.xz);
    ivec2 c = ivec2(floor(rel)) + ivec2(VOX_W / 2);
    if (any(lessThan(c, ivec2(0))) || any(greaterThanEqual(c, ivec2(VOX_W)))) return 0.0;
    vec4 s = texelFetch(shoreTex, c, 0);
    if (s.w < 0.5) return 0.0;
    // Au pixel de la texture, comme le reste du bloc.
    vec3 q = (floor(world * 16.0 - normal * 0.5) + 0.5) / 16.0;
    // La distance à la colonne d'eau, et la hauteur au-dessus de son eau.
    vec2 water = floor(world.xz) + s.xy + 0.5;
    float d = length(max(abs(q.xz - water) - 0.5, 0.0));
    float h = q.y - (s.z + floor(cameraPosition.y));
    if (h < -1.0 || h > 1.2) return 0.0;
    // La vague qui vient sur la rive : une lente montée, une descente plus lente encore, décalée le long du bord.
    float phase = t * 0.55 + dot(q.xz, vec2(0.37, 0.23));
    float lap = pow(0.5 + 0.5 * sin(phase), 3.0);
    float edge = cloudNoise(vec3(q.xz * 0.21 + q.y * 0.13, 0.83)).r - 0.5;
    float reach = 0.55 + 0.45 * lap + edge * 0.5;
    float line = 0.16 + 0.14 * lap + edge * 0.12;
    float wet = (1.0 - step(reach, d)) * (1.0 - step(line, h));
    // Plus loin et plus haut, le sol ne fait que garder un peu d'humidité.
    float damp = (1.0 - smoothstep(0.6, 1.6, d)) * (1.0 - smoothstep(0.2, 0.6, h)) * 0.45;
    float far_ = 1.0 - smoothstep(44.0, 60.0, length(world.xz - cameraPosition.xz));
    return max(wet, damp) * far_;
}
#endif
