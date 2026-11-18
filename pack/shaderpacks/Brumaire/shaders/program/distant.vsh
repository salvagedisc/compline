// Paysage lointain de Distant Horizons : des blocs unis, sans texture.
#include "/lib/settings.glsl"
#include "/lib/common.glsl"
#include "/lib/taa.glsl"
#ifdef FAR_RECORD
#include "/lib/farshadow.glsl"
#endif


out vec2 vLm;
out vec4 vTint;
out vec3 vNormal;
out vec3 vPos;
flat out int vMat;

void main() {
    vLm = lightCoord((gl_TextureMatrix[1] * gl_MultiTexCoord1).xy);
    vTint = gl_Color;
    vNormal = mat3(gbufferModelViewInverse) * (gl_NormalMatrix * gl_Normal);
    vec4 view = gl_ModelViewMatrix * gl_Vertex;
    vPos = (gbufferModelViewInverse * view).xyz;
    vMat = dhMaterialId;
#if defined FAR_RECORD && FAR_SHADOWS > 0
    farRecord(vPos + cameraPosition);
#endif
    gl_Position = taaJitter(gl_ProjectionMatrix * view);
}
