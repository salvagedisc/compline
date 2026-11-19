// L'embrasement des sommets : quand le soleil vient de passer sous l'horizon, ou juste avant qu'il se lève, les cimes
// enneigées, plus hautes que la vallée déjà dans l'ombre, voient encore le soleil rasant et rougeoient d'un rose
// orangé, plus vif sur les pentes tournées vers lui. Lu par le terrain proche (solid.fsh) et lointain (distant.fsh).

#if ALPENGLOW > 0
/** La lumière rose à ajouter à une neige d'albédo albedo (linéaire), en un point (repère monde) qui voit le ciel. */
vec3 alpenglow(vec3 albedo, vec3 normal, vec3 world, float sky) {
    float e = gSunDir.y;
    float glow = smoothstep(-0.12, -0.03, e) * (1.0 - smoothstep(0.0, 0.06, e));
    if (glow <= 0.0) return vec3(0.0);
    vec3 toward = normalize(vec3(gSunDir.x, 0.35, gSunDir.z));
    float face = 0.3 + 0.7 * max(dot(normal, toward), 0.0);
    // Plus la cime est haute, plus elle garde le soleil : le pied des montagnes s'est déjà éteint.
    float high = smoothstep(90.0, 200.0, world.y);
    return albedo * vec3(1.0, 0.46, 0.42) * glow * face * high * sky * 0.12;
}
#endif
