// Bruit en trois dimensions des nuages, calculé une seule fois au chargement du pack (setup) : 64³ texels qui se
// répètent sans couture. Rouge : la forme (bruit lisse creusé par des cellules) ; vert : le détail (cellules fines).
layout(local_size_x = 4, local_size_y = 4, local_size_z = 4) in;
const ivec3 workGroups = ivec3(16, 16, 16);
layout(rg8) uniform image3D cloudNoiseImg;

const int SIZE = 64;

vec3 hash3(ivec3 c) {
    uvec3 v = uvec3(c) * uvec3(1597334673u, 3812015801u, 2798796415u);
    v = (v.x ^ v.y ^ v.z) * uvec3(1597334673u, 3812015801u, 2798796415u);
    return vec3(v) / 4294967295.0;
}

/** Bruit lisse qui se répète tous les period carreaux. */
float smoothNoise(vec3 p, int period) {
    ivec3 i = ivec3(floor(p));
    vec3 f = fract(p);
    f = f * f * (3.0 - 2.0 * f);
    float v = 0.0;
    for (int z = 0; z < 2; z++)
    for (int y = 0; y < 2; y++)
    for (int x = 0; x < 2; x++) {
        ivec3 c = (i + ivec3(x, y, z)) % period;
        float w = (x == 1 ? f.x : 1.0 - f.x) * (y == 1 ? f.y : 1.0 - f.y) * (z == 1 ? f.z : 1.0 - f.z);
        v += hash3(c).x * w;
    }
    return v;
}

/** Cellules : 1 au centre d'une cellule, 0 entre deux ; cells cellules par côté, qui se répètent. */
float cells(vec3 p, int count) {
    vec3 q = p * float(count);
    ivec3 base = ivec3(floor(q));
    float best = 1e9;
    for (int z = -1; z <= 1; z++)
    for (int y = -1; y <= 1; y++)
    for (int x = -1; x <= 1; x++) {
        ivec3 c = base + ivec3(x, y, z);
        ivec3 w = ((c % count) + count) % count;
        vec3 feature = vec3(c) + hash3(w + 7);
        vec3 d = q - feature;
        best = min(best, dot(d, d));
    }
    return 1.0 - clamp(sqrt(best), 0.0, 1.0);
}

void main() {
    ivec3 t = ivec3(gl_GlobalInvocationID);
    vec3 p = (vec3(t) + 0.5) / float(SIZE);
    float smooth_ = 0.0, amp = 0.5;
    int period = 4;
    for (int i = 0; i < 4; i++) {
        smooth_ += smoothNoise(p * float(period), period) * amp;
        amp *= 0.5;
        period *= 2;
    }
    smooth_ /= 0.9375;
    float worley = cells(p, 4) * 0.625 + cells(p, 8) * 0.25 + cells(p, 16) * 0.125;
    // La forme : le bruit lisse, creusé là où les cellules sont faibles.
    float shape = clamp((smooth_ - (worley - 1.0)) / (1.0 - (worley - 1.0)), 0.0, 1.0);
    shape = clamp((shape - 0.35) / 0.65, 0.0, 1.0);
    float detail = cells(p, 8) * 0.625 + cells(p, 16) * 0.25 + cells(p, 32) * 0.125;
    imageStore(cloudNoiseImg, t, vec4(shape, detail, 0.0, 1.0));
}
