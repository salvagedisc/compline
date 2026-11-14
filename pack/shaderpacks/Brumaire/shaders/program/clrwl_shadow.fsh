uniform sampler2D gtexture;
in vec2 vUv;

layout(location = 0) out vec4 outColor;

void main() {
    vec4 color;
    vec4 overlay;
    vec2 light;
    float ao;
    clrwl_computeFragment(texture(gtexture, vUv), color, light, ao, overlay);
    outColor = vec4(0.0, 0.0, 0.0, 1.0);
}
