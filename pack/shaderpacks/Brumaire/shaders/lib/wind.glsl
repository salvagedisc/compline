// Vent : le déplacement d'un sommet de plante. La direction, la force et les rafales viennent du mod du pack
// (qui les lit dans Where Winds Blow) ; sans lui, un vent de repli qui forcit sous la pluie.

vec3 windBend(vec3 world, vec3 rel, float amp) {
    vec2 dir = vec2(0.8, 0.6);
    float force = 0.30 + 0.45 * rainStrength;
    float turbulence = 0.4;
    if (brumaireLink > 0.5 && dot(brumaireWind.xy, brumaireWind.xy) > 0.01) {
        dir = normalize(brumaireWind.xy);
        force = brumaireWind.z;
        turbulence = brumaireWind.w;
        float g0 = dot(rel.xz, brumaireGust0.xy) + brumaireGust0.z;
        float g1 = dot(rel.xz, brumaireGust1.xy) + brumaireGust1.z;
        force += brumaireGust0.w * exp(-1.65 * g0 * g0) + brumaireGust1.w * exp(-1.65 * g1 * g1);
    }
    force *= WIND_STRENGTH;
    vec2 side = vec2(-dir.y, dir.x);
    float t = frameTimeCounter;
    float along = dot(world.xz, dir);
    float across = dot(world.xz, side);
    // Une houle lente qui voyage dans le sens du vent, et un frémissement plus vif par-dessus.
    float swell = sin(along * 0.22 - t * 0.9 + sin(across * 0.13 + t * 0.2) * 1.3);
    float quiver = sin(along * 1.3 - t * 2.6 + across * 0.9) * 0.5 + sin(across * 2.1 + t * 3.4 + world.y) * 0.5;
    vec2 push = dir * force * (0.55 + 0.45 * swell);
    push += (dir * 0.35 + side * 0.25) * quiver * (0.25 + turbulence) * (0.4 + force);
    push *= amp;
    return vec3(push.x, -dot(push, push) * 0.9, push.y);
}

/** Déplacement d'un sommet selon la matière (voir block.properties) ; top : le sommet est en haut du quad. */
vec3 windVertex(int mat, bool top, vec3 world, vec3 rel, float sky) {
    float amp = 0.0;
    if (mat == 1) amp = top ? 0.22 : 0.0;
    else if (mat == 2) amp = top ? 0.12 : 0.0;
    else if (mat == 3) amp = top ? 0.30 : 0.12;
    else if (mat == 4) amp = 0.055;
    else if (mat == 5) amp = 0.04;
    amp *= smoothstep(0.35, 0.9, sky);
    if (amp <= 0.0) return vec3(0.0);
    return windBend(world, rel, amp);
}
