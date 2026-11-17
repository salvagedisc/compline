// Les pas des êtres vivants proches (brumairePush0..3, donnés par le mod compline) dans la carte des traces
// (lib/tracks.glsl). Un groupe par être : le premier fil pose les pieds le long du chemin parcouru depuis le dernier
// (tous les 0,7 bloc, le gauche puis le droit, jusqu'à cinq par image), puis chaque fil imprime un texel de chaque
// empreinte, une semelle au seizième de bloc tournée dans le sens de la marche. trackStateImg garde, pour chacun, où
// il a posé le dernier pied et lequel.
#include "/lib/settings.glsl"
#include "/lib/tracks.glsl"

layout(local_size_x = 16, local_size_y = 16, local_size_z = 1) in;
const ivec3 workGroups = ivec3(4, 1, 1);

layout(rgba16f) uniform image2D trackImg;
layout(rgba32f) uniform image2D trackStateImg;
uniform vec3 cameraPosition;
uniform float frameTimeCounter;
uniform float frameTime;
uniform float brumaireLink;
uniform vec4 brumairePush0;
uniform vec4 brumairePush1;
uniform vec4 brumairePush2;
uniform vec4 brumairePush3;
uniform vec4 brumaireWet;

shared int sCount;
shared vec3 sPrint[5];
shared vec2 sDir;
shared float sScale;
shared float sWet;

const float STRIDE = 0.7;

float sq(float x) { return x * x; }

/**
 * Imprime une empreinte : ce fil s'occupe d'un texel autour du pied, à ± 8 seizièmes de bloc. wet (0..6) : à quel
 * point le pied était mouillé, gardé avec la case (lib/tracks.glsl).
 */
void stamp(vec3 print, vec2 dir, float scale, float wet) {
    vec2 texel = floor(print.xz * TRACK_RES) + vec2(gl_LocalInvocationID.xy) - 8.0;
    vec2 world = (texel + 0.5) / TRACK_RES;
    vec2 rel = world - print.xz;
    vec2 local = vec2(dot(rel, vec2(-dir.y, dir.x)), dot(rel, dir)) / scale;
    // Une semelle de quatre pixels de large : l'avant plus large que le talon, un pli entre les deux.
    float front = sq(local.x / 0.14) + sq((local.y - 0.08) / 0.13);
    float heel = sq(local.x / 0.11) + sq((local.y + 0.13) / 0.09);
    float depth = max(1.0 - smoothstep(0.45, 1.0, front), 0.85 * (1.0 - smoothstep(0.45, 1.0, heel)));
    if (depth <= 0.0) return;
    ivec2 at = ivec2(texel) & (TRACK_SIZE - 1);
    vec4 old = imageLoad(trackImg, at);
    float tile = trackTile(world);
    // Un pas sur une vieille empreinte la recreuse ; elle garde sa profondeur si elle était plus marquée.
    float keep = abs(trackCase(old.a) - tile) < 0.5 && abs(old.b - mod(print.y, 256.0)) < 0.4 ? old.r : 0.0;
    imageStore(trackImg, at, vec4(max(depth, keep * 0.6), frameTimeCounter * 0.25, mod(print.y, 256.0), tile + 256.0 * wet));
}

void main() {
    int who = int(gl_WorkGroupID.x);
    vec4 push = who == 0 ? brumairePush0 : who == 1 ? brumairePush1 : who == 2 ? brumairePush2 : brumairePush3;
    if (gl_LocalInvocationIndex == 0u) {
        sCount = 0;
        if (brumaireLink > 0.5 && push.w > 0.0) {
            vec3 feet = push.xyz + cameraPosition;
            vec4 last = imageLoad(trackStateImg, ivec2(who, 0));
            vec2 moved = feet.xz - last.xy;
            float dist = length(moved);
            // Plus loin qu'un galop ne mène en une image : un autre être a pris cette place, ou il a été
            // téléporté ; on repart d'ici, sans empreinte.
            if (dist > 2.0 + 16.0 * frameTime || abs(feet.y - last.w) > 1.5) {
                imageStore(trackStateImg, ivec2(who, 0), vec4(feet.xz, last.z, feet.y));
            } else if (dist > STRIDE) {
                // Un pas tous les STRIDE blocs le long du chemin, même si l'image a sauté plusieurs pas d'un coup.
                vec2 dir = moved / dist;
                int n = min(int(dist / STRIDE), 5);
                sScale = clamp(push.w / 0.8, 0.6, 1.6);
                // Des pieds qui sortent de l'eau laissent des pas humides, de moins en moins à mesure qu'ils sèchent.
                float wet = who == 0 ? brumaireWet.x : who == 1 ? brumaireWet.y : who == 2 ? brumaireWet.z : brumaireWet.w;
                sWet = floor(clamp(wet, 0.0, 1.0) * 6.0 + 0.5);
                sDir = dir;
                float side = last.z;
                for (int k = 0; k < n; k++) {
                    vec2 at = last.xy + dir * STRIDE * float(k + 1);
                    float s = side > 0.5 ? 1.0 : -1.0;
                    sPrint[k] = vec3(at.x, feet.y, at.y) + vec3(-dir.y, 0.0, dir.x) * s * 0.13 * sScale;
                    side = 1.0 - side;
                }
                sCount = n;
                imageStore(trackStateImg, ivec2(who, 0), vec4(last.xy + dir * STRIDE * float(n), side, feet.y));
            }
        }
    }
    barrier();
    for (int k = 0; k < sCount; k++) stamp(sPrint[k], sDir, sScale, sWet);
}
