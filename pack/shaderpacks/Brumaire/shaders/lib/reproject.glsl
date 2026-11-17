// Où se trouvait, à l'image d'avant, ce que l'écran montre maintenant : sert à l'anticrénelage temporel
// (program/taa.fsh) et au flou de mouvement (program/motion.fsh). Seule la caméra bouge ici : un animal qui court
// reste à sa place d'une image à l'autre.
uniform mat4 gbufferPreviousProjection;
uniform mat4 gbufferPreviousModelView;
uniform vec3 previousCameraPosition;

/** La main suit la caméra (profondeur sous 0,56) : elle ne bouge pas à l'écran. */
bool isHand(float depth) { return depth < 0.56; }

/** Où se trouvait, à l'image d'avant, le point vu en uv à cette profondeur. */
vec2 previousUv(vec2 uv, float depth) {
    if (isHand(depth)) return uv;
    vec4 view = gbufferProjectionInverse * vec4(vec3(uv, depth) * 2.0 - 1.0, 1.0);
    view /= view.w;
    vec3 pos = mat3(gbufferModelViewInverse) * view.xyz;
    // Le ciel est à l'infini : seule la rotation de la caméra le déplace.
    if (depth < 1.0) pos += gbufferModelViewInverse[3].xyz + cameraPosition - previousCameraPosition;
    vec4 clip = gbufferPreviousProjection * (gbufferPreviousModelView * vec4(pos, 1.0));
    return clip.xy / clip.w * 0.5 + 0.5;
}
