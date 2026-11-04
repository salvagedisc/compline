#version 150

// Fallow: lightning pierces the mist. In Fallow's mist the game's bolt melts into the grey twenty
// blocks away; here it only softens with distance, far beyond the fog of the terrain.

#moj_import <fog.glsl>

uniform vec4 ColorModulator;
uniform float FogStart;
uniform float FogEnd;

in float vertexDistance;
in vec4 vertexColor;

out vec4 fragColor;

void main() {
    float fade = linear_fog_fade(vertexDistance, max(FogStart, 0.0) + 96.0, FogEnd + 320.0);
    fragColor = vertexColor * ColorModulator * fade;
}
