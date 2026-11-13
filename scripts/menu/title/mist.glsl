// Tilth title screen: mist flowing through the mountains of Caspar David
// Friedrich's "Riesengebirge Landscape with Rising Fog" (c. 1820, public domain).
// The painting is never moved or warped. A mask drawn from the painting itself
// (iChannel1: white where the painted mist lies between the ridges, black on
// the sky and the dark slopes in front) keeps the mist in the valleys; there,
// ribbons of the painting's own mist colour flow slowly sideways over a thick
// base, so the mist moves through the mountains and never in front of them.
// Shadertoy style: iChannel0 is the painting (16:9), iChannel1 the mask, iTime in seconds.

const float IMG_ASPECT = 16.0 / 9.0;
const float STRENGTH = 0.75;               // how thick the mist gets in the valleys
const float SPEED = 5.0;                   // 5.0: a near ribbon crosses the screen in about thirteen seconds
const vec3 MIST = vec3(0.92, 0.88, 0.83);  // the painted mist, sampled from the canvas

float hash(vec2 p) { return fract(sin(dot(p, vec2(127.1, 311.7))) * 43758.5453); }

float noise(vec2 p) {
    vec2 i = floor(p), f = fract(p);
    f = f * f * (3.0 - 2.0 * f);
    return mix(mix(hash(i), hash(i + vec2(1.0, 0.0)), f.x),
               mix(hash(i + vec2(0.0, 1.0)), hash(i + vec2(1.0, 1.0)), f.x), f.y);
}

float fbm(vec2 p) {
    float v = 0.0, a = 0.5;
    for (int k = 0; k < 5; k++) { v += a * noise(p); p = p * 2.03 + vec2(17.0, 9.0); a *= 0.5; }
    return v;
}

// the painting covers the screen whatever its shape, like a cover-fit image
vec2 coverUV(vec2 fragCoord) {
    vec2 uv = fragCoord / iResolution.xy;
    float sa = iResolution.x / iResolution.y;
    if (sa > IMG_ASPECT) uv.y = (uv.y - 0.5) * (IMG_ASPECT / sa) + 0.5;
    else uv.x = (uv.x - 0.5) * (sa / IMG_ASPECT) + 0.5;
    return uv;
}

// FancyMenu hands the images over as Minecraft loads them, top row first,
// where Shadertoy flips them: uv runs bottom to top, the images top to bottom
vec4 picture(sampler2D channel, vec2 uv) { return texture(channel, vec2(uv.x, 1.0 - uv.y)); }

void mainImage(out vec4 fragColor, in vec2 fragCoord) {
    vec2 uv = coverUV(fragCoord);
    vec3 c = picture(iChannel0, uv).rgb;
    float valley = picture(iChannel1, uv).r;
    float t = iTime * SPEED;
    vec2 p = vec2(uv.x * IMG_ASPECT, 1.0 - uv.y);

    // ribbons stretched sideways, the far ones slower, gently undulating
    float wave = sin(p.x * 1.3 + t * 0.05) * 0.02;
    float far  = fbm(vec2(p.x * 2.2 + t * 0.030, (p.y + wave) * 9.0));
    float near = fbm(vec2(p.x * 3.5 + t * 0.096 + 5.0, (p.y - wave) * 14.0 + 2.0));
    float ribbons = smoothstep(0.40, 0.72, far * 0.55 + near * 0.55);

    // a thick base everywhere in the valleys, the ribbons flowing over it
    float k = clamp(valley * 1.25, 0.0, 1.0) * mix(0.15, 1.0, ribbons) * STRENGTH;

    // the light at the upper right breathes, very slowly
    float sun = 0.035 * (0.5 + 0.5 * sin(iTime * 0.09)) * smoothstep(0.9, 0.0, distance(uv, vec2(0.85, 0.9)));
    fragColor = vec4(mix(c, MIST, k) + sun, 1.0);
}
