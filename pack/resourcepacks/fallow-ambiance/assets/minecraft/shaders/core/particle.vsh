#version 150

// Fallow: the vanilla particle shader, which also draws the rain (vanilla and Where Winds Blow).
// The rain texture carries a marker texel at (0, 0) (see scripts/ambiance/weather.py): when it is
// bound, the fragment shader gives the drops the colour of the mist (resource-pack-only rain tint).
// Keep the lightmap read exactly as vanilla writes it: Polytone patches that line for coloured lights.

#moj_import <fog.glsl>

in vec3 Position;
in vec2 UV0;
in vec4 Color;
in ivec2 UV2;

uniform sampler2D Sampler0;
uniform sampler2D Sampler2;

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;
uniform int FogShape;

out float vertexDistance;
out vec2 texCoord0;
out vec4 vertexColor;
flat out int fallowRain;

// "FAL", alpha 1: invisible (the fragment shader discards alpha < 0.1), and never found in an atlas
const vec4 RAIN_MARKER = vec4(70.0, 65.0, 76.0, 1.0);

void main() {
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);

    vertexDistance = fog_distance(Position, FogShape);
    texCoord0 = UV0;
    vertexColor = Color * texelFetch(Sampler2, UV2 / 16, 0);

    ivec2 size = textureSize(Sampler0, 0);
    vec4 marker = texelFetch(Sampler0, ivec2(0, 0), 0) * 255.0;
    bool marked = all(lessThan(abs(marker - RAIN_MARKER), vec4(0.5)));
    fallowRain = (size.y == size.x * 4 && marked) ? 1 : 0;
}
