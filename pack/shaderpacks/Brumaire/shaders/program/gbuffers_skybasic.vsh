out vec4 vTint;
void main() {
    gl_Position = ftransform();
    vTint = gl_Color;
}
