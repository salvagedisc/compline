#version 150

// Compline : ne garde que les parties très claires de l'image (torches, lave, ciel autour du
// soleil), en petit, pour le halo que la passe « tons » rajoute.

uniform sampler2D DiffuseSampler;

in vec2 texCoord;
flat in vec3 versSoleil;

uniform vec2 InSize;

out vec4 fragColor;

vec3 clair(vec2 uv, float seuil) {
    vec3 c = texture(DiffuseSampler, uv).rgb;
    float l = max(c.r, max(c.g, c.b));
    return c * smoothstep(seuil, seuil + 0.22, l);
}

void main() {
    // de jour la brume est claire partout : le seuil monte pour qu'elle ne bave pas
    float seuil = mix(0.52, 0.80, smoothstep(-0.10, 0.15, versSoleil.y));
    vec2 t = 1.0 / InSize;
    vec3 c = clair(texCoord + t * vec2(-1.0, -1.0), seuil)
           + clair(texCoord + t * vec2( 1.0, -1.0), seuil)
           + clair(texCoord + t * vec2(-1.0,  1.0), seuil)
           + clair(texCoord + t * vec2( 1.0,  1.0), seuil);
    fragColor = vec4(c * 0.25, 1.0);
}
