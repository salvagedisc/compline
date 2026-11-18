// Les nuages posés sur le ciel, l'air, la brume et ses rayons de lumière, puis l'eau quand la caméra y plonge.
#define FSH
#include "/lib/settings.glsl"
#include "/lib/common.glsl"
#include "/lib/shadow.glsl"
#include "/lib/mist.glsl"
#include "/lib/clouds.glsl"
#include "/lib/water.glsl"
#include "/lib/rain.glsl"
#include "/lib/voxel.glsl"
#include "/lib/precip.glsl"
#include "/lib/motes.glsl"
#include "/lib/underwater.glsl"
#include "/program/scene.glsl"

uniform sampler2D colortex0;
uniform sampler2D colortex3;
in vec2 vUv;

/* RENDERTARGETS: 0 */
layout(location = 0) out vec4 outColor;

/**
 * Marche vers le fond de la vue dans la carte d'ombre. mistSun : part de soleil qui atteint la brume (pondérée par
 * sa densité) ; rend la longueur éclairée du rayon, en blocs, qui fait briller l'air : les rayons sous les arbres,
 * colorés là où ils ont passé un vitrail.
 */
vec3 shaftMarch(vec3 dir, float reach, float density, out float mistSun) {
    mistSun = 1.0;
#if LIGHT_SHAFTS > 0
    float base = mistAnchor();
    float stride = reach / float(LIGHT_SHAFTS);
    float t = stride * dither(gl_FragCoord.xy);
    vec3 origin = gbufferModelViewInverse[3].xyz;
    float sum = 0.0, weight = 0.0;
    vec3 lit = vec3(0.0);
    for (int i = 0; i < LIGHT_SHAFTS; i++) {
        vec3 p = origin + dir * t;
        vec3 uv = shadowWarp((shadowProjection * (shadowModelView * vec4(p, 1.0))).xyz) * 0.5 + 0.5;
        float s = uv.z < 1.0 ? texture(shadowtex1, vec3(uv.xy, uv.z - 0.0002)) : 1.0;
        float w = exp((base - cameraPosition.y - p.y) / MIST_HEIGHT - density * t);
        sum += w * s;
        weight += w;
        if (s > 0.0) lit += s * tintAt(uv);
        t += stride;
    }
    mistSun = sum / max(weight, 1e-6);
    return lit * stride;
#else
    return vec3(0.0);
#endif
}

/** Dans un désert au soleil, l'air chaud fait trembler le lointain au ras du sol : un décalage de l'image. */
vec2 shimmer(vec2 uv) {
#if HEAT_SHIMMER > 0
    float heat = airDust() * gDay * (1.0 - rainStrength) * float(isEyeInWater == 0);
    if (heat < 0.01) return uv;
    bool sky, hand;
    vec3 pos = scenePosition(uv, sky, hand);
    if (hand) return uv;
    float len = sky ? 512.0 : length(pos);
    vec3 dir = pos / max(len, 1e-3);
    float low = 1.0 - smoothstep(0.02, 0.18, abs(dir.y));
    float t = frameTimeCounter;
    float wave = sin(uv.y * viewHeight * 0.31 + t * 7.0 + sin(uv.x * 37.0 + t * 1.3) * 2.0)
               + 0.5 * sin(uv.y * viewHeight * 0.83 - t * 11.0 + uv.x * 23.0);
    float amount = heat * low * smoothstep(24.0, 120.0, len) * 1.4;
    return uv + vec2(wave, 0.4 * wave) * amount / vec2(viewWidth, viewHeight);
#else
    return uv;
#endif
}

void main() {
    initEnv();
    vec2 uv = shimmer(vUv);
    vec3 color = texture(colortex0, uv).rgb;
    bool sky, hand;
    vec3 pos = scenePosition(uv, sky, hand);
    float len = sky ? 16384.0 : length(pos);
    vec3 dir = normalize(pos);

#if CLOUD_STEPS > 0
    // Le ciel en volume (demi-écran, lissé sur quatre prises) ; jamais sur ce qui est plus près que lui.
    if (!hand && isEyeInWater == 0) {
        float toBase = (cloudBase() - cameraPosition.y) / dir.y;
        float toTop = (cloudTop() - cameraPosition.y) / dir.y;
        float enter = max(min(toBase, toTop), 0.0);
        // Le ciel les reçoit toujours (l'arc-en-ciel et les cirrus sont plus loin que tout), le paysage seulement
        // quand la couche est plus près que lui.
        if (sky || (dir.y != 0.0 && max(toBase, toTop) > 0.0 && len > enter)) {
            vec2 px = 1.0 / vec2(viewWidth, viewHeight);
            vec2 half_ = vUv * 0.5;
            vec4 c = texture(colortex3, half_ + vec2(px.x, px.y) * 0.5) + texture(colortex3, half_ + vec2(-px.x, px.y) * 0.5)
                   + texture(colortex3, half_ + vec2(px.x, -px.y) * 0.5) + texture(colortex3, half_ - px * 0.5);
            c *= 0.25;
            color = color * c.a + c.rgb;
        }
    }
#endif

    if (isEyeInWater == 1) {
        // Sous l'eau : la même eau que vue d'en haut, teintée par le biome, qui éteint et voile le lointain.
        // La couleur du brouillard sous l'eau ne donne que le caractère du biome : bleu franc presque partout.
        vec3 hue = waterHue(mix(vec3(0.25, 0.46, 0.89), fogColor / max(max(fogColor.r, fogColor.b), 1e-3), 0.35));
        float open = float(eyeBrightnessSmooth.y) / 240.0;
        // On y voit un peu plus loin qu'à travers la surface : l'œil s'est fait à la pénombre.
        vec3 ext = waterExtinction(hue) * 0.55;
        vec3 pass = exp(-ext * min(len, 512.0));
        // La lumière du jour baisse d'un cran par bloc d'eau au-dessus de l'œil : près de la surface l'eau reste claire.
        // Ce que l'eau diffuse d'elle-même entre l'œil et ce qu'on regarde : plus clair vers la surface et vers l'astre.
        color = color * pass + waterScatterPath(ext, waterAlbedo(hue), dir, len, gLightColor * open, gAmbient * max(open, 0.06))
            + vec3(0.004, 0.006, 0.007) * hue * (1.0 - pass);
        float mu = dot(dir, underwaterLightDir());
#if LIGHT_SHAFTS > 0
        // Les rayons qui descendent de la surface, plus vifs quand on regarde vers elle, à contre-jour.
        if (dot(gLightColor, vec3(1.0)) > 0.01 && !hand) {
            float lit = waterShafts(dir, len, ext);
            float phase = 0.25 + 1.6 * pow(max(mu, 0.0), 4.0);
            color += hue * gLightColor * lit * phase * (0.04 * SHAFT_STRENGTH) * open;
        }
#endif
#if WATER_MOTES > 0
        // La poussière en suspension, qui brille surtout à contre-jour.
        if (!hand) {
            float mote = marineSnow(dir, len);
            vec3 glint = hue * (gAmbient * 0.5 + gLightColor * (0.25 + 0.9 * pow(max(mu, 0.0), 3.0))) * max(open, 0.15);
            color = mix(color, glint + color * 0.4, mote * 0.4);
        }
#endif
    } else if (isEyeInWater >= 2) {
        float t = 1.0 - exp(-len * (isEyeInWater == 2 ? 1.2 : 0.7));
        color = mix(color, isEyeInWater == 2 ? vec3(1.6, 0.35, 0.04) : vec3(0.75, 0.82, 0.90), t);
    } else if (!hand) {
        if (!sky) {
            vec3 inscat;
            vec3 pass = airVeil(dir, len, inscat);
            color = color * pass + inscat;
        }
        float nearOnly;
        float depth = mistDepth(dir, len, nearOnly);
        float veil = 1.0 - exp(-depth);
        float sun = 1.0;
#if LIGHT_SHAFTS > 0
        if (dot(gLightColor, vec3(1.0)) > 0.01) {
            float reach = min(len, shadowDistance * 0.9);
            float density = nearOnly / max(len, 1.0);
            float mistSun;
            vec3 litLength = shaftMarch(dir, reach, density, mistSun);
            // La part de brume traversée dans la portée de la carte d'ombre ; au-delà, elle est tenue pour éclairée.
            float inside = 1.0 - exp(-nearOnly * reach / max(len, 1e-3));
            sun = mix(1.0, mistSun, clamp(inside / max(veil, 1e-4), 0.0, 1.0));
            // L'air lui-même renvoie un peu de soleil, surtout vers l'avant : là où l'ombre des feuillages le
            // découpe, des rayons. Seulement à l'air libre.
            float mu = dot(dir, gLightDir);
            float phase = 0.7 * mistPhase(mu, 0.75) + 0.3 * mistPhase(mu, 0.2);
            float open = smoothstep(0.2, 0.7, float(eyeBrightnessSmooth.y) / 240.0);
            color += gLightColor * phase * litLength * (0.0045 * SHAFT_STRENGTH) * mix(0.8, 1.6, airHumid) * open;
        }
#endif
        if (veil > 0.003) {
            // La brume lointaine (l'anneau) prend la couleur du ciel bas dans cette direction : au couchant, elle
            // rougeoit du côté du soleil ; la brume proche garde sa propre lumière.
            vec3 mist = mistColor(dir, sun);
            float far_ = clamp((depth - nearOnly) / max(depth, 1e-4), 0.0, 1.0);
            mist = mix(mist, skyAt(normalize(vec3(dir.x, 0.03, dir.z))) * mistTint() * 0.9, far_ * 0.75);
            color = mix(color, mist, veil);
        }
#if AIR_MOTES > 0
        // Poussière dans la lumière : seulement là où le soleil passe (une trouée sous les arbres, une fenêtre),
        // à peine visible dos au soleil, nette à contre-jour. Le jour seulement, pas sous la pluie.
        if (dot(gLightColor, vec3(1.0)) > 0.01 && rainStrength < 0.5) {
            float dist;
            float mote = moteAlong(dir, min(len, 12.0), 0.35, 0.25, 12.0, dist);
            if (mote > 0.0) {
                vec3 p = gbufferModelViewInverse[3].xyz + dir * dist;
                vec3 suv = shadowWarp((shadowProjection * (shadowModelView * vec4(p, 1.0))).xyz) * 0.5 + 0.5;
                float lit = suv.z < 1.0 ? texture(shadowtex1, vec3(suv.xy, suv.z - 0.0002)) : 1.0;
                // Une poussière ne se voit que sur un fond sombre : devant le ciel ou un sol au soleil, elle se perd
                // (sauf à contre-jour). Le fond est lu dans la carte d'ombre, un peu en avant de la surface.
                float back = 1.0;
                if (!sky && len < shadowDistance * 0.9) {
                    vec3 q = gbufferModelViewInverse[3].xyz + dir * max(len - 0.15, 0.0);
                    vec3 buv = shadowWarp((shadowProjection * (shadowModelView * vec4(q, 1.0))).xyz) * 0.5 + 0.5;
                    back = buv.z < 1.0 ? texture(shadowtex1, vec3(buv.xy, buv.z - 0.0002)) : 1.0;
                }
                float mu = dot(dir, gLightDir);
                float glow = lit * gDay * (0.2 * (1.0 - 0.9 * back) + 1.8 * pow(max(mu, 0.0), 4.0) * (1.0 - 0.8 * back))
                           * (1.0 - 2.0 * rainStrength);
                color += gLightColor * mote * glow * 0.7;
            }
        }
#endif
        // Au loin, là où le jeu ne dessine plus de gouttes, les rideaux de pluie.
        float curtain = rainCurtain(dir, len, frameTimeCounter) * rainStrength * rainHere;
#if SHADER_RAIN > 0
        // Les averses passent : les rideaux sont plus épais là où il pleut fort.
        curtain *= mix(0.35, 1.3, showerAt(cameraPosition.xz + dir.xz * min(len, 160.0)));
#endif
        if (curtain > 0.0) color = mix(color, gAmbient * 1.25 + gLightColor * 0.1 + vec3(0.02), min(curtain * 0.2, 0.35));
    }
#if SHADER_RAIN > 0
    if (isEyeInWater == 0 && rainStrength > 0.0) {
        float snow = step(0.5, snowHere);
        float local = showerAt(cameraPosition.xz);
        float amount = rainStrength * (snow > 0.5 ? snowHere * mix(0.5, 1.0, local) : rainHere * mix(0.5, 1.0, local));
        float a = precipitation(dir, len, snow, amount);
        color = mix(color, precipColor(snow), a);
    }
#endif
    if (blindness > 0.0) color *= exp(-len * 0.35 * blindness);
    outColor = vec4(color, 1.0);
}
