// Les nuages, marchés sur un demi-écran : lumière diffusée vers l'œil (rgb) et part du fond qui passe (a).
// composite1 les relit, les lisse et les pose sur le ciel et le paysage.
#define FSH
#include "/lib/settings.glsl"
#include "/lib/common.glsl"
#include "/lib/clouds.glsl"
#include "/program/scene.glsl"

in vec2 vUv;

/*
const int colortex3Format = RGBA16F;
*/
const vec4 colortex3ClearColor = vec4(0.0, 0.0, 0.0, 1.0);

/* RENDERTARGETS: 3 */
layout(location = 0) out vec4 outClouds;

/** Diffusion des gouttelettes : vers l'avant surtout (Henyey-Greenstein). */
float cloudPhase(float mu, float g) {
    return 0.0795775 * (1.0 - g * g) / pow(1.0 + g * g - 2.0 * g * mu, 1.5);
}

void main() {
    outClouds = vec4(0.0, 0.0, 0.0, 1.0);
#if CLOUD_STEPS > 0
    bool sky, hand;
    vec3 pos = scenePosition(vUv, sky, hand);
    if (hand) return;
    initEnv();
    vec3 dir = normalize(pos);
    float sceneLen = sky ? 1e9 : length(pos);
    vec3 eye = cameraPosition;

    // Entrée et sortie de la couche le long du regard.
    float t0, t1;
    if (abs(dir.y) < 1e-4) return;
    float a = (cloudBase() - eye.y) / dir.y;
    float b = (cloudTop() - eye.y) / dir.y;
    t0 = max(min(a, b), 0.0);
    t1 = min(max(a, b), min(sceneLen, CLOUD_RANGE));
    if (t1 <= t0) return;

    float coverage = cloudCoverage();
    int steps = CLOUD_STEPS;
    float stride = (t1 - t0) / float(steps);
    float noise = dither(gl_FragCoord.xy);
    float t = t0 + stride * noise;
    float mu = dot(dir, gLightDir);
    float phase = mix(cloudPhase(mu, 0.65), cloudPhase(mu, -0.25), 0.3);
    vec3 sun = gLightColor * CLOUD_LIGHT;
    // Le ciel éclaire le haut des nuages plus que leur ventre.
    vec3 skyLight = gAmbient * 0.9;
    vec3 light = vec3(0.0);
    float pass = 1.0;
    const float SIGMA = 0.09;
    for (int i = 0; i < steps; i++) {
        vec3 p = eye + dir * t;
        float d = cloudDensity(p, coverage, true);
        if (d > 0.002) {
            // Lumière de l'astre qui arrive jusque-là : quatre pas de plus en plus longs vers lui.
            float od = 0.0;
            float reach = 6.0;
            vec3 q = p;
            for (int j = 0; j < 4; j++) {
                q += gLightDir * reach;
                od += cloudDensity(q, coverage, j == 0) * reach;
                reach *= 2.0;
            }
            float beer = exp(-od * SIGMA) * 0.7 + exp(-od * SIGMA * 0.25) * 0.3;
            float powder = 1.0 - exp(-d * stride * SIGMA * 2.0);
            float h = clamp((p.y - cloudBase()) / CLOUD_TOP_SPAN, 0.0, 1.0);
            vec3 inscatter = sun * beer * phase * mix(0.5, 1.0, powder) * 4.0 + skyLight * mix(0.25, 0.9, h * h);
            float ext = d * SIGMA;
            float step_ = exp(-ext * stride);
            light += pass * inscatter * (1.0 - step_);
            pass *= step_;
            if (pass < 0.02) break;
        }
        t += stride;
    }
    // Au loin, les nuages se fondent dans le ciel bas.
    float fade = exp(-max(t0, 0.0) / (CLOUD_RANGE * 0.45));
    vec3 horizon = skyAt(normalize(vec3(dir.x, max(dir.y, 0.03), dir.z)));
    light = mix(horizon * (1.0 - pass), light, fade);
    // Sous la pluie, des nuages gris.
    light = mix(light, vec3(luma(light)) * 0.6, rainStrength * 0.6);
    outClouds = vec4(light, pass);
#endif
}
