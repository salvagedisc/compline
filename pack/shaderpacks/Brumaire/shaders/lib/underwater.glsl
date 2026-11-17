// Sous l'eau : la poussière en suspension (neige marine) qui accroche la lumière, et les rayons qui descendent de
// la surface, découpés par l'ombre de ce qui flotte ou surplombe et tachetés par les vagues comme les caustiques.
// Demande shadow.glsl, water.glsl et motes.glsl avant lui.

/** Direction vers laquelle remonte la lumière sous la surface : celle de l'astre, redressée par la réfraction. */
vec3 underwaterLightDir() {
    vec3 down = refract(-gLightDir, vec3(0.0, 1.0, 0.0), 1.0 / WATER_IOR);
    return dot(down, down) > 1e-4 ? -down : vec3(0.0, 1.0, 0.0);
}

#if LIGHT_SHAFTS > 0
/**
 * Rayons sous l'eau : la longueur éclairée du rayon (blocs), pesée par l'eau traversée. Chaque pas lit la carte
 * d'ombre et les caustiques au point où son rayon de lumière a traversé la surface : des faisceaux droits qui
 * ondulent avec les vagues.
 */
float waterShafts(vec3 dir, float len, vec3 ext) {
    float reach = min(len, 24.0);
    float stride = reach / float(LIGHT_SHAFTS);
    float t = stride * dither(gl_FragCoord.xy);
    vec3 up = underwaterLightDir();
    float fade = luma(ext);
    float lit = 0.0;
    for (int i = 0; i < LIGHT_SHAFTS; i++) {
        vec3 p = dir * t;
        vec3 uv = shadowWarp((shadowProjection * (shadowModelView * vec4(p, 1.0))).xyz) * 0.5 + 0.5;
        float s = uv.z < 1.0 ? texture(shadowtex1, vec3(uv.xy, uv.z - 0.0005)) : 1.0;
        // Le rayon de lumière de ce point a traversé un plan fixe du monde : la même tache y suit tout le faisceau.
        vec3 w = p + cameraPosition;
        vec2 q = w.xz + up.xz / up.y * (64.0 - w.y);
        float c = caustics(vec3(q.x, 64.0, q.y));
        lit += s * c * exp(-fade * t);
        t += stride;
    }
    return lit * stride;
}
#endif

#if WATER_MOTES > 0
/**
 * Neige marine vue le long d'un rayon jusqu'à len : l'éclat (0..1) de la poussière la plus proche. Une poussière
 * par case d'un bloc sur trois environ, d'un pixel de bloc, qui dérive lentement dans sa case et scintille.
 */
float marineSnow(vec3 dir, float len) {
    float dist;
    return moteAlong(dir, min(len, 20.0), 0.3, 0.14, 20.0, dist);
}
#endif
