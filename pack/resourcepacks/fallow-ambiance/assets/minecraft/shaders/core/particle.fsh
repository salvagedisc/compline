#version 150

// Fallow: the vanilla particle shader, plus the look of the rain (see particle.vsh).
// The drops lose their own colour and take a light grey with a third of the current mist in it
// (Atmospherics sets the fog colour), and are a little thinner than the game's, so the landscape
// stays visible through the shower.

#moj_import <fog.glsl>

uniform sampler2D Sampler0;

uniform vec4 ColorModulator;
uniform float FogStart;
uniform float FogEnd;
uniform vec4 FogColor;

in float vertexDistance;
in vec2 texCoord0;
in vec4 vertexColor;
flat in int fallowRain;

out vec4 fragColor;

const vec3 RAIN_GREY = vec3(0.88, 0.89, 0.90);
const float RAIN_MIST = 0.35;
const float RAIN_ALPHA = 0.7;
// Where Winds Blow tints its drops (0.82, 0.86, 0.72): only the brightness of that colour is kept
const float WWB_LUMA = 0.8340;

void main() {
    vec4 color;
    if (fallowRain == 1) {
        vec4 tex = texture(Sampler0, texCoord0);
        float light = clamp(dot(vertexColor.rgb, vec3(0.299, 0.587, 0.114)) / WWB_LUMA, 0.0, 1.0);
        vec3 tint = mix(RAIN_GREY, FogColor.rgb, RAIN_MIST);
        color = vec4(tex.rgb * tint * light, tex.a * vertexColor.a * RAIN_ALPHA) * ColorModulator;
    } else {
        color = texture(Sampler0, texCoord0) * vertexColor * ColorModulator;
    }
    if (color.a < 0.1) {
        discard;
    }
    fragColor = linear_fog(color, vertexDistance, FogStart, FogEnd, FogColor);
}
