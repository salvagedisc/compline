// Le feuillage touffu : chaque face de feuilles proche reçoit deux plans de plus, de la même texture, un peu plus
// grands qu'elle, décollés et penchés au hasard ; le contour des arbres n'est plus une suite de cubes, et le feuillage
// gagne de l'épaisseur. Seul le terrain découpé (feuilles, herbes, fleurs) passe par ici (gbuffers_terrain_cutout) ;
// les autres faces sont rendues telles quelles.
#include "/lib/settings.glsl"
#include "/lib/common.glsl"
#include "/lib/taa.glsl"

layout(triangles) in;
layout(triangle_strip, max_vertices = 9) out;

in vec2 gUv[];
in vec2 gLm[];
in vec4 gTint[];
in vec3 gNormal[];
in vec3 gPos[];
flat in int gMat[];
flat in int gSurf[];
in vec2 gBend[];
#if RELIEF > 0
in vec4 gTangent[];
in vec2 gSpriteMid[];
in vec2 gSpriteHalf[];
#endif

out vec2 vUv;
out vec2 vLm;
out vec4 vTint;
out vec3 vNormal;
out vec3 vPos;
flat out int vMat;
flat out int vSurf;
out vec2 vBend;
#if RELIEF > 0
out vec4 vTangent;
out vec2 vSpriteMid;
out vec2 vSpriteHalf;
#endif

void pass(int i) {
    vUv = gUv[i];
    vLm = gLm[i];
    vTint = gTint[i];
    vNormal = gNormal[i];
    vPos = gPos[i];
    vMat = gMat[i];
    vSurf = gSurf[i];
    vBend = gBend[i];
#if RELIEF > 0
    vTangent = gTangent[i];
    vSpriteMid = gSpriteMid[i];
    vSpriteHalf = gSpriteHalf[i];
#endif
}

float bushHash(vec3 p) { return fract(sin(dot(p, vec3(127.1, 311.7, 74.7))) * 43758.5453); }

void main() {
    for (int i = 0; i < 3; i++) {
        pass(i);
        gl_Position = gl_in[i].gl_Position;
        EmitVertex();
    }
    EndPrimitive();
#if BUSHY_LEAVES > 0
    if (gMat[0] == 4) {
    // Le centre de la face : le milieu du plus long côté (la diagonale du carré coupé en deux triangles).
    float d01 = distance(gPos[0], gPos[1]), d12 = distance(gPos[1], gPos[2]), d20 = distance(gPos[2], gPos[0]);
    vec3 centre = d01 >= d12 && d01 >= d20 ? (gPos[0] + gPos[1]) * 0.5 : (d12 >= d20 ? (gPos[1] + gPos[2]) * 0.5 : (gPos[2] + gPos[0]) * 0.5);
    if (dot(centre, centre) > float(BUSHY_DISTANCE * BUSHY_DISTANCE)) return;
    vec3 n = normalize(gNormal[0]);
    // La face du bloc, repérée dans le monde : les deux triangles d'une face tirent les mêmes plans.
    vec3 face = floor(centre + cameraPosition - n * 0.25) + n * 0.37;
    vec3 a = normalize(abs(n.y) > 0.5 ? vec3(1.0, 0.0, 0.0) : vec3(0.0, 1.0, 0.0));
    vec3 b = cross(n, a);
    for (int k = 0; k < 2; k++) {
        float h = bushHash(face + float(k) * 5.3);
        if (h < 0.15) continue;
        float spin = bushHash(face + float(k) * 9.1 + 2.0) * 6.2831853;
        vec3 tiltDir = a * cos(spin) + b * sin(spin);
        float scale = k == 0 ? 1.22 : 1.05;
        float lift = k == 0 ? 0.07 : 0.18;
        float tilt = (k == 0 ? 0.30 : 0.45) * (0.6 + 0.4 * h);
        vec3 shift = (a * (bushHash(face + 3.7 + float(k)) - 0.5) + b * (bushHash(face + 6.1 + float(k)) - 0.5)) * 0.30;
        for (int i = 0; i < 3; i++) {
            pass(i);
            vec3 r = gPos[i] - centre;
            vec3 p = centre + r * scale + shift + n * (lift + dot(r, tiltDir) * tilt);
            vPos = p;
            gl_Position = taaJitter(gbufferProjection * (gbufferModelView * vec4(p, 1.0)));
            EmitVertex();
        }
        EndPrimitive();
    }
    return;
    }
#endif
#if GRASS_TUFTS > 0
    // Les touffes plus fournies : chaque plan d'herbe, de fleur ou de culture proche reçoit une copie tournée autour de
    // l'axe de son bloc (de 30 à 60 degrés, tirés par colonne : les deux moitiés d'une grande herbe tournent ensemble),
    // une herbe courte un peu plus haute ou plus basse. Une touffe en X devient une étoile, un champ en # se resserre.
    if (gMat[0] < 1 || gMat[0] > 3) return;
    vec3 n = normalize(gNormal[0]);
    if (abs(n.y) > 0.5) return;
    // Le centre du quad (le milieu de sa diagonale) reste dans son bloc, décalage du jeu compris : les deux triangles
    // trouvent la même colonne.
    float d01 = distance(gPos[0], gPos[1]), d12 = distance(gPos[1], gPos[2]), d20 = distance(gPos[2], gPos[0]);
    vec3 mid = d01 >= d12 && d01 >= d20 ? (gPos[0] + gPos[1]) * 0.5 : (d12 >= d20 ? (gPos[1] + gPos[2]) * 0.5 : (gPos[2] + gPos[0]) * 0.5);
    float reach = length(mid);
    vec2 column = floor(mid.xz + cameraPosition.xz);
    float pick = bushHash(vec3(column.x, 7.0, column.y));
    if (reach > float(BUSHY_DISTANCE) || pick < smoothstep(float(BUSHY_DISTANCE) * 0.7, float(BUSHY_DISTANCE), reach)) return;
    float angle = mix(0.52, 1.05, bushHash(vec3(column.x, 3.0, column.y)));
    float c = cos(angle), si = sin(angle);
    mat2 spin = mat2(c, si, -si, c);
    vec2 axis = column + 0.5 - cameraPosition.xz;
    float base = min(gPos[0].y, min(gPos[1].y, gPos[2].y));
    float stretch = gMat[0] == 1 ? mix(0.75, 1.15, bushHash(vec3(column.x, 11.0, column.y))) : 1.0;
    for (int i = 0; i < 3; i++) {
        pass(i);
        vec3 p = gPos[i];
        p.xz = axis + spin * (p.xz - axis);
        p.y = base + (p.y - base) * stretch;
        vPos = p;
        vec2 turned = spin * gNormal[i].xz;
        vNormal = vec3(turned.x, gNormal[i].y, turned.y);
        gl_Position = taaJitter(gbufferProjection * (gbufferModelView * vec4(p, 1.0)));
        EmitVertex();
    }
    EndPrimitive();
#endif
}
