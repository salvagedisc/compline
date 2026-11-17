// Sous l'eau : la poussière en suspension (neige marine) qui accroche la lumière, et les rayons qui descendent de
// la surface, découpés par l'ombre de ce qui flotte ou surplombe et tachetés par les vagues comme les caustiques.
// Demande shadow.glsl et water.glsl avant lui.

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
float moteHash(vec3 p, float k) { return fract(sin(dot(p, vec3(127.1, 311.7, 74.7)) + k * 19.19) * 43758.5453); }

/**
 * Neige marine vue le long d'un rayon jusqu'à len : l'éclat (0..1) de la poussière la plus proche. Une poussière
 * par case d'un bloc sur trois environ, d'un pixel de bloc, qui dérive lentement dans sa case et scintille.
 */
float marineSnow(vec3 dir, float len) {
    float t1 = min(len, 20.0);
    if (t1 <= 0.3) return 0.0;
    vec3 camFloor = floor(cameraPosition);
    vec3 o = cameraPosition - camFloor;
    vec3 d = vec3(abs(dir.x) < 1e-5 ? 1e-5 : dir.x, abs(dir.y) < 1e-5 ? 1e-5 : dir.y, abs(dir.z) < 1e-5 ? 1e-5 : dir.z);
    vec3 cell = floor(o + dir * 0.3);
    vec3 stepDir = sign(d);
    vec3 tDelta = abs(1.0 / d);
    vec3 next = (cell + max(stepDir, 0.0) - o) / d;
    float pixel = 2.0 / (gbufferProjection[1][1] * viewHeight);
    vec3 side = normalize(cross(dir, vec3(0.0, 1.0, 0.0)) + vec3(1e-4, 0.0, 0.0));
    vec3 up = cross(side, dir);
    float time = frameTimeCounter;
    for (int i = 0; i < 48; i++) {
        float tExit = min(min(next.x, next.y), min(next.z, t1));
        vec3 wc = cell + camFloor;
        float h = moteHash(wc, 1.0);
        if (h < 0.3) {
            vec3 drift = 0.14 * vec3(sin(time * 0.37 + h * 40.0), sin(time * 0.23 + h * 70.0), cos(time * 0.31 + h * 55.0));
            vec3 p = cell + 0.22 + 0.56 * vec3(moteHash(wc, 2.0), moteHash(wc, 3.0), moteHash(wc, 4.0)) + drift - o;
            float s = dot(p, dir);
            if (s > 0.3 && s < t1) {
                vec3 off = dir * s - p;
                float size = max(0.35 / 16.0, s * pixel * 0.5);
                if (max(abs(dot(off, side)), abs(dot(off, up))) < size) {
                    float twinkle = 0.55 + 0.45 * sin(time * 1.7 + h * 90.0);
                    return twinkle * (1.0 - smoothstep(10.0, 20.0, s)) * smoothstep(0.8, 2.5, s);
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
