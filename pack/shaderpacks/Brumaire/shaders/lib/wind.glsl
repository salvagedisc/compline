// Vent : le déplacement d'un sommet de plante, et la pente de la pluie. La direction, la force et les rafales
// viennent du mod du pack (module vent) ; sans lui, un vent de repli qui forcit sous la pluie.

/** Le vent au point rel (depuis la caméra) : direction, force rafales comprises, turbulence. */
void windAt(vec3 rel, out vec2 dir, out float force, out float turbulence) {
    dir = vec2(0.8, 0.6);
    force = 0.30 + 0.45 * rainStrength;
    turbulence = 0.4;
    if (brumaireLink > 0.5 && dot(brumaireWind.xy, brumaireWind.xy) > 0.01) {
        dir = normalize(brumaireWind.xy);
        force = brumaireWind.z;
        turbulence = brumaireWind.w;
        float g0 = dot(rel.xz, brumaireGust0.xy) + brumaireGust0.z;
        float g1 = dot(rel.xz, brumaireGust1.xy) + brumaireGust1.z;
        force += brumaireGust0.w * exp(-1.65 * g0 * g0) + brumaireGust1.w * exp(-1.65 * g1 * g1);
    }
    force *= WIND_STRENGTH;
}

/** Une herbe près des pieds d'un être vivant s'écarte de lui et se couche. */
vec3 pushFrom(vec4 push, vec3 rel) {
    if (push.w <= 0.0) return vec3(0.0);
    vec3 d = rel - push.xyz;
    if (d.y < -0.6 || d.y > 2.4) return vec3(0.0);
    float dist = length(d.xz);
    float k = 1.0 - smoothstep(push.w * 0.35, push.w, dist);
    vec2 away = d.xz / max(dist, 1e-3);
    return vec3(away.x, -0.7, away.y) * k;
}

vec3 pushed(vec3 rel, float amp) {
    if (brumaireLink < 0.5) return vec3(0.0);
    vec3 p = pushFrom(brumairePush0, rel) + pushFrom(brumairePush1, rel) + pushFrom(brumairePush2, rel) + pushFrom(brumairePush3, rel);
    // Plusieurs êtres côte à côte ne couchent pas l'herbe plus loin que le sol.
    float len = length(p.xz);
    if (len > 1.0) p /= len;
    return p * amp * 2.8;
}

vec3 windBend(vec3 world, vec3 rel, float amp) {
    vec2 dir;
    float force, turbulence;
    windAt(rel, dir, force, turbulence);
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
    vec3 bend = vec3(0.0);
    // Les herbes et les fleurs se couchent sous les pas, même à l'abri du vent.
    if (mat <= 3 && amp > 0.0) bend += pushed(rel, amp);
    amp *= smoothstep(0.35, 0.9, sky);
    if (amp <= 0.0) return bend;
    return bend + windBend(world, rel, amp);
}
