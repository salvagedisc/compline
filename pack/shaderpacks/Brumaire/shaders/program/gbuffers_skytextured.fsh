// Lune et autres astres du jeu ou des mods : gardés, ajoutés au ciel peint plus tard.
uniform sampler2D gtexture;
uniform vec3 sunPosition;
in vec2 vUv;
in vec4 vTint;
in vec3 vView;

/* RENDERTARGETS: 0 */
layout(location = 0) out vec4 outColor;

void main() {
    // Le soleil est repeint avec le ciel (deferred). Sa texture carrée n'est pas dessinée, qu'elle vienne du
    // jeu ou d'un mod qui la déplace : tout ce qui est dessiné autour du soleil est écarté.
    if (dot(normalize(vView), normalize(sunPosition)) > 0.86) discard;
    vec4 color = texture(gtexture, vUv) * vTint;
    outColor = vec4(pow(color.rgb, vec3(2.2)) * 1.4, color.a);
}
