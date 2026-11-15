// Passe plein écran.
out vec2 vUv;
void main() {
    gl_Position = ftransform();
    vUv = gl_MultiTexCoord0.xy;
}
