#version 150

// Fallow: sun rays (and faint moon rays) through the gaps of the canopy, as a Polytone post effect.
// A screen-space radial blur of the sky seen through the scene: wherever the depth buffer shows open
// sky around the sun, light is smeared towards the eye, so leaves, trunks and ridges cut it into
// shafts. Warm and low at dawn and dusk, paler at noon. Off in the rain (see polytone/post_shaders).
// Adapted from Sunbathing by MehVahdJukaar (MIT): see CREDITS.md.

uniform sampler2D DiffuseSampler;
uniform sampler2D InDepth;

in vec2 texCoord;
out vec4 fragColor;

uniform vec2 InSize;

uniform mat4 PolyProjMat;
uniform mat4 PolyModelViewMat;
uniform float PolySunAngle;

uniform float SunRayIntensity;
uniform float MoonRayIntensity;

const int SAMPLES = 32;
const float DENSITY = 0.9;
const float DECAY = 0.985;
const float EXPOSURE = 0.3;
const float WEIGHT = 0.25;

const float SUN_SIZE = 0.06;
const float SUN_GLOW = 0.4;

const float PI = 3.14159265;
const float TRANSITION_WIDTH = radians(12.0);

const vec3 SUN_LOW = vec3(1.0, 0.70, 0.45);
const vec3 SUN_HIGH = vec3(1.0, 0.92, 0.78);
const vec3 MOON = vec3(0.62, 0.70, 0.85);

float interleavedGradientNoise(vec2 uv) {
    return fract(52.9829189 * fract(dot(uv, vec2(0.06711056, 0.00583715))));
}

vec2 lightScreenPos(float angle, out float screenFade) {
    vec3 dir = vec3(cos(angle), sin(angle), 0.0);
    vec3 camPos = PolyModelViewMat[3].xyz;
    vec4 clip = PolyProjMat * (PolyModelViewMat * vec4(camPos - dir * 1000.0, 1.0));
    if (clip.w <= 0.0) {
        screenFade = 0.0;
        return vec2(0.0);
    }
    vec2 uv = (clip.xy / clip.w) * 0.5 + 0.5;
    screenFade = smoothstep(1.5, 0.2, distance(uv, vec2(0.5)));
    return uv;
}

float lightShape(vec2 uv, vec2 lightUV, float aspect) {
    vec2 d = uv - lightUV;
    d.x *= aspect;
    float dist = length(d);
    return smoothstep(SUN_SIZE, SUN_SIZE * 0.8, dist) + exp(-dist / (SUN_SIZE * SUN_GLOW)) * SUN_GLOW;
}

vec3 rays(vec2 lightUV, float screenFade, vec3 lightColor, float aspect) {
    if (screenFade <= 0.0) {
        return vec3(0.0);
    }
    vec2 delta = (texCoord - lightUV) * (DENSITY / float(SAMPLES));
    vec2 coord = texCoord + delta * interleavedGradientNoise(gl_FragCoord.xy);
    vec3 acc = vec3(0.0);
    float decay = 1.0;
    for (int i = 0; i < SAMPLES; ++i) {
        coord -= delta;
        vec2 b0 = smoothstep(vec2(0.0), vec2(0.08), coord);
        vec2 b1 = smoothstep(vec2(1.0), vec2(0.92), coord);
        float border = b0.x * b0.y * b1.x * b1.y;
        float sky = step(0.999999, texture(InDepth, coord).r);
        acc += lightColor * sky * lightShape(coord, lightUV, aspect) * decay * WEIGHT * border;
        decay *= DECAY;
    }
    return acc * EXPOSURE * screenFade;
}

void lightWeights(float angle, out float sunW, out float moonW) {
    float t = mod(angle + PI, 2.0 * PI);
    if (t < TRANSITION_WIDTH) {
        sunW = t / TRANSITION_WIDTH;
    } else if (t < PI - TRANSITION_WIDTH) {
        sunW = 1.0;
    } else if (t < PI + TRANSITION_WIDTH) {
        sunW = 1.0 - (t - (PI - TRANSITION_WIDTH)) / (2.0 * TRANSITION_WIDTH);
    } else if (t < 2.0 * PI - TRANSITION_WIDTH) {
        sunW = 0.0;
    } else {
        sunW = (t - (2.0 * PI - TRANSITION_WIDTH)) / TRANSITION_WIDTH;
    }
    moonW = 1.0 - sunW;
}

void main() {
    vec4 color = texture(DiffuseSampler, texCoord);
    float aspect = InSize.x / InSize.y;

    float sunW, moonW;
    lightWeights(PolySunAngle, sunW, moonW);

    vec3 light = vec3(0.0);
    float fade;
    if (sunW > 0.0) {
        vec2 sun = lightScreenPos(PolySunAngle, fade);
        float elevation = clamp(-sin(PolySunAngle), 0.0, 1.0);
        vec3 sunColor = mix(SUN_LOW, SUN_HIGH, smoothstep(0.0, 0.5, elevation));
        light += rays(sun, fade, sunColor, aspect) * sunW * SunRayIntensity;
    }
    if (moonW > 0.0) {
        vec2 moon = lightScreenPos(PolySunAngle + PI, fade);
        light += rays(moon, fade, MOON, aspect) * moonW * MoonRayIntensity;
    }

    // only on the scene: the open sky keeps the sun that Atmospherics draws
    float scene = 1.0 - step(0.999999, texture(InDepth, texCoord).r);
    fragColor = vec4(color.rgb + light * scene, 1.0);
}
