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
#include "/lib/leaffall.glsl"
#include "/lib/fireflies.glsl"
#include "/lib/cavedrips.glsl"
#include "/lib/drift.glsl"
#include "/lib/seasmoke.glsl"
#include "/lib/embers.glsl"
#include "/lib/spray.glsl"
#include "/lib/fluff.glsl"
#include "/lib/eaves.glsl"
#include "/lib/soilsteam.glsl"
#include "/lib/underwater.glsl"
#include "/program/scene.glsl"

/*
const int colortex3Format = RGBA16F;
*/
/*
const int colortex11Format = RGBA16F;
*/
// Les nuages du demi-écran (clouds.fsh) ; sans nuages, le fond laisse tout passer. De même pour la brume proche.
const vec4 colortex3ClearColor = vec4(0.0, 0.0, 0.0, 1.0);
const vec4 colortex11ClearColor = vec4(0.0, 0.0, 0.0, 1.0);

uniform sampler2D colortex0;
uniform sampler2D colortex3;
uniform sampler2D colortex11;

float linearDepthAt(vec2 uv) {
    float d = texture(depthtex0, uv).r;
    return near * far / (far - d * (far - near));
}

/**
 * La brume proche marchée sur le demi-écran (clouds.fsh), ramenée au pixel : les quatre cases autour de lui, pesées
 * comme un filtre bilinéaire et retirées quand elles sont à une autre profondeur (la brume d'un tronc ne déborde pas
 * sur le lointain derrière lui).
 */
vec4 fogAt(vec2 uv) {
    vec2 halfSize = vec2(viewWidth, viewHeight) * 0.5;
    vec2 at = uv * halfSize - 0.5;
    vec2 base = floor(at);
    vec2 f = at - base;
    float centre = linearDepthAt(uv);
    vec4 sum = vec4(0.0);
    float weight = 0.0;
    for (int k = 0; k < 4; k++) {
        vec2 o = vec2(k & 1, k >> 1);
        vec2 cell = (base + o + 0.5) / halfSize;
        float w = (o.x > 0.5 ? f.x : 1.0 - f.x) * (o.y > 0.5 ? f.y : 1.0 - f.y) + 1e-3;
        w /= 1e-3 + abs(linearDepthAt(cell) - centre) / centre * 30.0;
        sum += texture(colortex11, cell * 0.5) * w;
        weight += w;
    }
    return sum / max(weight, 1e-6);
}
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

/**
 * Au-dessus de la lave, l'air brûlant : la part du rayon qui passe à un ou deux blocs au-dessus d'une nappe, lue dans
 * le volume des blocs (une case de lave éclaire au niveau 15), sur les vingt premiers blocs.
 */
float lavaHeat(vec3 dir, float len) {
#if defined VOXELS && CAVE_DETAIL > 0
    float reach = min(len, 20.0);
    float dt = reach / 10.0;
    vec3 origin = gbufferModelViewInverse[3].xyz;
    vec3 camF = fract(cameraPosition);
    float noise = dither(gl_FragCoord.xy + 3.7);
    float heat = 0.0;
    for (int i = 0; i < 10; i++) {
        vec3 p = origin + dir * (float(i) + noise) * dt;
        for (int k = 1; k <= 2; k++) {
            ivec3 c = voxelCell(p - vec3(0.0, float(k), 0.0), camF);
            if (!voxelInside(c)) continue;
            uint v = texelFetch(voxelTex, c, 0).r;
            if (((v >> 4u) & 15u) == 2u && (v & 15u) >= 12u) heat += k == 1 ? 1.0 : 0.55;
        }
    }
    return clamp(heat * dt * 0.10, 0.0, 1.0);
#else
    return 0.0;
#endif
}

/**
 * L'air chaud fait trembler l'image : dans un désert au soleil, le lointain au ras du sol ; partout, ce qu'on voit
 * à travers l'air qui monte d'une nappe de lave, en ondes qui s'élèvent.
 */
vec2 shimmer(vec2 uv) {
#if HEAT_SHIMMER > 0
    if (isEyeInWater != 0) return uv;
    bool sky, hand;
    vec3 pos = scenePosition(uv, sky, hand);
    if (hand) return uv;
    float len = sky ? 512.0 : length(pos);
    vec3 dir = pos / max(len, 1e-3);
    float t = frameTimeCounter;
    vec2 shift = vec2(0.0);
    float heat = airDust() * gDay * (1.0 - rainStrength);
    if (heat >= 0.01) {
        float low = 1.0 - smoothstep(0.02, 0.18, abs(dir.y));
        float wave = sin(uv.y * viewHeight * 0.31 + t * 7.0 + sin(uv.x * 37.0 + t * 1.3) * 2.0)
                   + 0.5 * sin(uv.y * viewHeight * 0.83 - t * 11.0 + uv.x * 23.0);
        shift += vec2(wave, 0.4 * wave) * heat * low * smoothstep(24.0, 120.0, len) * 1.4;
    }
    float lava = lavaHeat(dir, len);
    if (lava > 0.0) {
        // Des ondes plus serrées qui montent : leur phase avance vers le haut de l'image.
        float rise = sin(uv.y * viewHeight * 0.20 - t * 6.0 + sin(uv.x * 53.0 + t * 1.9) * 2.4)
                   + 0.6 * sin(uv.y * viewHeight * 0.47 - t * 9.5 + uv.x * 31.0);
        shift += vec2(rise * 0.8, rise) * lava * 2.2;
    }
    return uv + shift / vec2(viewWidth, viewHeight);
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
#if FOG_STEPS > 0
        {
            // Au-delà de la portée de la marche : la nappe et l'anneau, en une fois, derrière la brume proche.
            float reach = min(len, FOG_RANGE);
            float beyond = mistNearDensity() * (1.0 - MIST_LIFT * brumaireMist.x)
                         * mistLayer(cameraPosition.y, dir.y, reach, len, MIST_HEIGHT, mistAnchor());
            float far_ = beyond + max(depth - nearOnly, 0.0);
            float farVeil = 1.0 - exp(-far_);
            if (farVeil > 0.003) {
                vec3 mist = mistColor(dir, 1.0);
                float ringShare = clamp((depth - nearOnly) / max(far_, 1e-4), 0.0, 1.0);
                mist = mix(mist, skyAt(normalize(vec3(dir.x, 0.03, dir.z))) * mistTint() * 0.9, ringShare * 0.75);
                color = mix(color, mist, farVeil);
            }
            vec4 fog = fogAt(uv);
            color = color * fog.a + fog.rgb;
        }
#else
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
#endif
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
#if AIR_MOTES > 0 && COLORED_LIGHT > 0 && CAVE_DETAIL > 0
        // Sous terre, la poussière ne se voit que dans la lumière d'une torche ou de la lave, de sa couleur.
        {
            float under = 1.0 - smoothstep(0.0, 0.45, float(eyeBrightnessSmooth.y) / 240.0);
            if (under > 0.01) {
                float dist;
                float mote = moteAlong(dir, min(len, 10.0), 0.30, 0.30, 10.0, dist);
                if (mote > 0.0) {
                    vec4 blk = voxelLight(gbufferModelViewInverse[3].xyz + dir * dist, vec3(0.0));
                    color += blk.rgb * blk.a * blk.a * blk.a * mote * under * 0.35;
                }
            }
        }
#endif
#if CAVE_DRIPS > 0 && defined VOXELS && COLORED_LIGHT > 0
        // Sous terre, les gouttes qui tombent des voûtes brillent de la lumière des torches et de la lave.
        {
            float under = 1.0 - smoothstep(0.0, 0.45, float(eyeBrightnessSmooth.y) / 240.0);
            if (under > 0.01) {
                float dist;
                float drip = dripAlong(dir, min(len, 16.0), dist);
                if (drip > 0.0) {
                    vec4 blk = voxelLight(gbufferModelViewInverse[3].xyz + dir * dist, vec3(0.0));
                    color = mix(color, blk.rgb * (0.04 + 1.6 * blk.a * blk.a) + vec3(0.004, 0.006, 0.008), drip * under * 0.85);
                }
            }
        }
#endif
#if FIREFLIES > 0 && SHADER_RAIN > 0
        // Les lucioles des nuits douces : leur lumière s'ajoute, l'éclat (bloom) en fait des points qui rayonnent.
        {
            float amount = fireflySeason();
            if (amount > 0.01) color += vec3(0.62, 1.0, 0.22) * firefliesAlong(dir, min(len, 40.0), amount) * 0.9;
        }
#endif
#if SEED_FLUFF > 0 && SHADER_RAIN > 0
        // Les duvets des journées douces : blancs et translucides, ils s'allument à contre-jour.
        {
            float amount = fluffAmount();
            if (amount > 0.01) {
                float fluffDist;
                float fluff = fluffAlong(dir, min(len, 32.0), amount, fluffDist);
                if (fluff > 0.0) {
                    vec3 p = gbufferModelViewInverse[3].xyz + dir * fluffDist;
                    vec3 suv = shadowWarp((shadowProjection * (shadowModelView * vec4(p, 1.0))).xyz) * 0.5 + 0.5;
                    float lit = suv.z < 1.0 ? texture(shadowtex1, vec3(suv.xy, suv.z - 0.0002)) : 1.0;
                    float mu = max(dot(dir, gLightDir), 0.0);
                    vec3 light = gAmbient * 1.1 + gLightColor * lit * (0.45 + 2.2 * pow(mu, 6.0));
                    color = mix(color, light, fluff * 0.65);
                }
            }
        }
#endif
#if LAVA_EMBERS > 0 && defined VOXELS
        // Les braises de la lave : leur lumière s'ajoute, l'éclat (bloom) en fait des points qui rayonnent.
        if (isEyeInWater == 0) color += embersAlong(dir, min(len, 24.0));
#endif
#if FALL_SPRAY > 0 && defined VOXELS
        // Les embruns autour des cascades : un voile blanc de fines gouttes, éclairé par le ciel et le soleil.
        if (isEyeInWater == 0) {
            float spray = sprayAlong(dir, len);
            if (spray > 0.0) color = mix(color, gAmbient * 1.25 + gLightColor * 0.5, spray);
        }
#endif
#if SOIL_STEAM > 0 && SHADER_RAIN > 0
        // Après l'averse, au soleil, la terre trempée fume : une vapeur blanche, là où le soleil touche le sol.
        if (isEyeInWater == 0) {
            float amount = soilSteamAmount();
            if (amount > 0.01 && !sky && len < shadowDistance * 0.9) {
                vec3 q = gbufferModelViewInverse[3].xyz + dir * max(len - 0.15, 0.0);
                vec3 quv = shadowWarp((shadowProjection * (shadowModelView * vec4(q, 1.0))).xyz) * 0.5 + 0.5;
                float sunOn = quv.z < 1.0 ? texture(shadowtex1, vec3(quv.xy, quv.z - 0.0002)) : 1.0;
                float steam = soilSteamAlong(dir, len, amount * sunOn);
                // Une vapeur blanche que le soleil traverse : plus vive à contre-jour.
                vec3 vapour = gAmbient * 1.3 + gLightColor * (0.45 + 1.2 * pow(max(dot(dir, gLightDir), 0.0), 4.0));
                color = mix(color, vapour, steam);
            }
        }
#endif
#if LEAF_LITTER > 0 && defined VOXELS
        // Les feuilles mortes qui roulent au sol, éclairées comme le feuillage : soleil (avec son ombre) et ciel.
        {
            float litterDist;
            vec3 litterTint;
            float litter = litterAlong(dir, min(len, 24.0), litterDist, litterTint);
            if (litter > 0.0) {
                vec3 p = gbufferModelViewInverse[3].xyz + dir * litterDist;
                vec3 suv = shadowWarp((shadowProjection * (shadowModelView * vec4(p, 1.0))).xyz) * 0.5 + 0.5;
                float lit = suv.z < 1.0 ? texture(shadowtex1, vec3(suv.xy, suv.z - 0.0002)) : 1.0;
                vec3 light = gAmbient * 0.9 + gLightColor * lit * max(gLightDir.y, 0.2) * 0.9;
                color = mix(color, litterTint * light, litter);
            }
        }
#endif
#if WATER_STEAM > 0 && defined VOXELS
        // Les matins frais, l'eau fume : des volutes pâles, de la couleur de l'air, au ras des lacs et des rivières.
        if (isEyeInWater == 0) {
            float amount = steamAmount();
            if (amount > 0.01) {
                float steam = steamAlong(dir, len, amount);
                color = mix(color, mistColor(dir, 1.0) * 1.15, steam);
            }
        }
#endif
#if GROUND_DRIFT > 0 && SHADER_RAIN > 0
        // Par grand vent, la neige ou le sable filent au ras du sol, éclairés comme l'air : soleil et ciel.
        {
            vec2 kind = driftAmount();
            float amount = max(kind.x, kind.y);
            if (amount > 0.01) {
                float drift = driftAlong(dir, len, amount);
                vec3 tint = kind.x >= kind.y ? vec3(0.85, 0.90, 1.0) : vec3(0.95, 0.80, 0.58);
                color = mix(color, tint * (gAmbient * 0.9 + gLightColor * 0.55), drift);
            }
        }
#endif
#if CANOPY_DRIPS > 0 && defined VOXELS
        // Sous les arbres, pendant et après la pluie, l'eau s'égoutte des feuilles ; chargées de neige, les branches
        // en lâchent des paquets quand le vent souffle.
        {
            float water = max(rainStrength, wetness) * rainHere;
            float force = brumaireLink > 0.5 ? brumaireWind.z : 0.3;
            float snowLoad = snowStorm * (0.3 + 0.7 * smoothstep(0.2, 0.8, force));
            float dripDist, dripKind;
            float drip = canopyDripAlong(dir, min(len, 16.0), water, snowLoad, dripDist, dripKind);
            if (drip > 0.0) {
                vec3 p = gbufferModelViewInverse[3].xyz + dir * dripDist;
                vec3 suv = shadowWarp((shadowProjection * (shadowModelView * vec4(p, 1.0))).xyz) * 0.5 + 0.5;
                float lit = suv.z < 1.0 ? texture(shadowtex1, vec3(suv.xy, suv.z - 0.0002)) : 1.0;
                vec3 light = dripKind > 0.5 ? gAmbient * 0.85 + gLightColor * lit * 0.7 : gAmbient * 1.3 + gLightColor * lit * 0.6;
                color = mix(color, light, drip * (dripKind > 0.5 ? 0.9 : 0.55));
            }
        }
#endif
#if EAVES_DRIPS > 0 && SHADER_RAIN > 0 && defined VOXELS
        // Le bord des toits goutte pendant et après la pluie : des traits clairs, éclairés par le ciel.
        {
            float water = max(rainStrength, wetness) * rainHere;
            float eaveDist;
            float eave = eavesAlong(dir, min(len, 20.0), water, eaveDist);
            if (eave > 0.0) color = mix(color, gAmbient * 1.4 + gLightColor * 0.4, eave * 0.6);
        }
#endif
#if FALLING_LEAVES > 0 && defined VOXELS
        // Les feuilles qui tombent des arbres proches, éclairées comme le feuillage : soleil (avec son ombre) et ciel.
        {
            float leafDist;
            vec3 leafTint;
            float leaf = leafAlong(dir, min(len, 16.0), leafDist, leafTint);
            if (leaf > 0.0) {
                vec3 p = gbufferModelViewInverse[3].xyz + dir * leafDist;
                vec3 suv = shadowWarp((shadowProjection * (shadowModelView * vec4(p, 1.0))).xyz) * 0.5 + 0.5;
                float lit = suv.z < 1.0 ? texture(shadowtex1, vec3(suv.xy, suv.z - 0.0002)) : 1.0;
                vec3 light = gAmbient * 0.75 + gLightColor * lit * 0.55;
                color = mix(color, leafTint * light, leaf);
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
        vec3 glow;
        float a = precipitation(dir, len, snow, amount, glow);
        color = mix(color, precipColor(snow, dir), a) + glow;
    }
#endif
    if (blindness > 0.0) color *= exp(-len * 0.35 * blindness);
    outColor = vec4(color, 1.0);
}
