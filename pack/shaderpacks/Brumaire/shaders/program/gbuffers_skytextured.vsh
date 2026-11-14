out vec2 vUv;
out vec4 vTint;
out vec3 vView;
void main() {
    gl_Position = ftransform();
    vUv = (gl_TextureMatrix[0] * gl_MultiTexCoord0).xy;
    vTint = gl_Color;
    vView = (gl_ModelViewMatrix * gl_Vertex).xyz;
}
