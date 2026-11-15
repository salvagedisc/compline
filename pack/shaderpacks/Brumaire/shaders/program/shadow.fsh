uniform sampler2D gtexture;
in vec2 vUv;
flat in int vWater;

/*
const int shadowcolor0Format = R8;
*/
const vec4 shadowcolor0ClearColor = vec4(0.0, 0.0, 0.0, 0.0);

layout(location = 0) out vec4 outColor;

void main() {
    if (vWater == 1) {
        outColor = vec4(1.0, 0.0, 0.0, 1.0);
        return;
    }
    if (texture(gtexture, vUv).a < 0.1) discard;
    outColor = vec4(0.0, 0.0, 0.0, 1.0);
}
