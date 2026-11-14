uniform sampler2D gtexture;
in vec2 vUv;
in float vAlpha;

layout(location = 0) out vec4 outColor;

void main() {
    float alpha = texture(gtexture, vUv).a * vAlpha;
    if (alpha < 0.1) discard;
    outColor = vec4(0.0, 0.0, 0.0, 1.0);
}
