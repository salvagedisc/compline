#version 150

// Compline : flou dans un sens (horizontal puis vertical), neuf points en cinq lectures.

uniform sampler2D DiffuseSampler;

in vec2 texCoord;

uniform vec2 InSize;
uniform vec2 Sens;

out vec4 fragColor;

void main() {
    vec2 t = Sens / InSize * 1.6;
    vec3 c = texture(DiffuseSampler, texCoord).rgb * 0.2270270270;
    c += texture(DiffuseSampler, texCoord + t * 1.3846153846).rgb * 0.3162162162;
    c += texture(DiffuseSampler, texCoord - t * 1.3846153846).rgb * 0.3162162162;
    c += texture(DiffuseSampler, texCoord + t * 3.2307692308).rgb * 0.0702702703;
    c += texture(DiffuseSampler, texCoord - t * 3.2307692308).rgb * 0.0702702703;
    fragColor = vec4(c, 1.0);
}
