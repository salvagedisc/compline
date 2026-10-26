#version 150

#moj_import <fog.glsl>

uniform sampler2D Sampler0;

uniform vec4 ColorModulator;
uniform float FogStart;
uniform float FogEnd;
uniform vec4 FogColor;

in float vertexDistance;
in vec4 vertexColor;
in vec2 texCoord0;

out vec4 fragColor;

void main() {
    vec4 color = texture(Sampler0, texCoord0) * vertexColor * ColorModulator;
    if (color.a < 0.1) {
        discard;
    }
    // Fallow: texts written in 0x404040 become moss green
    if (all(lessThan(abs(vertexColor.rgb - vec3(0.25098)), vec3(0.002)))) {
        color.rgb = texture(Sampler0, texCoord0).rgb * vec3(0.2353, 0.2980, 0.2196) * ColorModulator.rgb;
    }
    fragColor = linear_fog(color, vertexDistance, FogStart, FogEnd, FogColor);
}
