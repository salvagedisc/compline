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

/** Un feuillage que traverse ou frôle un être vivant s'écarte de lui et frémit, tant qu'il reste contre. */
vec3 brushFrom(vec4 push, vec3 rel, vec3 world) {
    if (push.w <= 0.0) return vec3(0.0);
    vec3 d = rel - push.xyz;
    if (d.y < -0.6 || d.y > 2.4) return vec3(0.0);
    float dist = length(d.xz);
    float k = 1.0 - smoothstep(0.3, 0.3 + push.w, dist);
    if (k <= 0.0) return vec3(0.0);
    vec2 away = d.xz / max(dist, 1e-3);
    float t = frameTimeCounter;
    vec3 quiver = vec3(sin(t * 13.0 + world.x * 5.1 + world.y * 3.3), sin(t * 11.0 + world.z * 4.7) * 0.6, sin(t * 15.0 + world.z * 5.3 + world.y * 2.9));
    return (vec3(away.x, 0.15, away.y) * 0.12 + quiver * 0.05) * k;
}

vec3 brushed(vec3 rel, vec3 world) {
    if (brumaireLink < 0.5) return vec3(0.0);
    return brushFrom(brumairePush0, rel, world) + brushFrom(brumairePush1, rel, world) + brushFrom(brumairePush2, rel, world) + brushFrom(brumairePush3, rel, world);
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
    // Les feuillages s'écartent et frémissent au passage, eux aussi même à l'abri.
    if (mat == 4) bend += brushed(rel, world);
    amp *= smoothstep(0.35, 0.9, sky);
    if (amp <= 0.0) return bend;
    return bend + windBend(world, rel, amp);
}

#if WATER_PLANT_SWAY > 0
/**
 * Le courant sous l'eau : les herbes marines et les algues ondulent lentement, d'un balancement qui monte le long de la
 * tige. Le déplacement ne dépend que de la position (pas du haut ou du bas du quad) : les sommets partagés par deux
 * tronçons d'une algue bougent ensemble, la tige reste d'un seul tenant. Le courant suit le vent de surface, en plus
 * lent. Un nénuphar (flat) se soulève et penche un peu au passage des rides, et glisse sur place.
 */
vec3 currentVertex(vec3 world, vec3 rel, bool flat_) {
    vec2 dir;
    float force, turbulence;
    windAt(rel, dir, force, turbulence);
    float t = frameTimeCounter;
    float phase = dot(world.xz, vec2(0.37, 0.23));
    if (flat_) {
        float bob = sin(t * 1.3 + dot(world.xz, vec2(2.1, 1.7))) * 0.010 * (0.5 + force);
        vec2 drift = dir * sin(t * 0.4 + phase) * 0.03 * (0.4 + force);
        return vec3(drift.x, bob, drift.y);
    }
    float s = sin(t * 0.8 + world.y * 0.55 + phase) + 0.45 * sin(t * 1.7 - world.y * 1.1 + phase * 2.3);
    float k = sin(t * 0.6 + world.y * 0.4 + phase * 1.7 + 1.9);
    vec2 side = vec2(-dir.y, dir.x);
    vec2 push = dir * (0.03 + 0.04 * s) + side * 0.05 * k;
    return vec3(push.x, 0.0, push.y);
}
#endif
