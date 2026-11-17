// GLSL shared by the mist, the edge wall, the sea and the sky.
// Transparency is drawn with an ordered (Bayer) dither instead of blending, so the mist breaks into
// crisp pixels like the game's own fog and never needs sorting.

export const BRUIT = /* glsl */ `
float hash(vec2 p) { return fract(sin(dot(p, vec2(127.1, 311.7))) * 43758.5453); }
float bruit(vec2 p) {
  vec2 i = floor(p), f = fract(p);
  vec2 u = f * f * (3.0 - 2.0 * f);
  return mix(mix(hash(i), hash(i + vec2(1, 0)), u.x), mix(hash(i + vec2(0, 1)), hash(i + vec2(1, 1)), u.x), u.y);
}
float fbm(vec2 p) {
  float s = 0.0, a = 0.5;
  for (int k = 0; k < 4; k++) { s += a * bruit(p); p = p * 2.03 + vec2(1.7, 9.2); a *= 0.5; }
  return s;
}
uniform vec3 uBrouillard;
uniform float uDensBrouillard;
vec3 brouiller(vec3 c, vec3 p) {
  float d = uDensBrouillard * distance(cameraPosition, p);
  return mix(c, uBrouillard, 1.0 - exp(-d * d));
}
float bayer(vec2 f) {
  ivec2 p = ivec2(mod(f, 4.0));
  int i = p.x + p.y * 4;
  int m[16] = int[16](0, 8, 2, 10, 12, 4, 14, 6, 3, 11, 1, 9, 15, 7, 13, 5);
  return (float(m[i]) + 0.5) / 16.0;
}
`

/** A drifting layer of mist, thicker towards the edge, cleared around the pointer and the chosen place. */
export const BRUME_VERT = /* glsl */ `
varying vec3 vMonde;
void main() {
  vec4 m = modelMatrix * vec4(position, 1.0);
  vMonde = m.xyz;
  gl_Position = projectionMatrix * viewMatrix * m;
}
`

export const BRUME_FRAG = /* glsl */ `
uniform float uTemps;
uniform float uDensite;
uniform float uEchelle;
uniform vec2 uDerive;
uniform vec3 uCouleur;
uniform vec3 uTrou;     // xz centre and radius of the clearing over the chosen place
uniform vec3 uPointeur; // xz centre and radius of the clearing under the pointer
uniform float uBord;
varying vec3 vMonde;
${BRUIT}
void main() {
  vec2 p = vMonde.xz;
  float d = length(p);
  float n = fbm(p * uEchelle + uDerive * uTemps);
  float a = smoothstep(0.38, 0.85, n) * uDensite;
  a *= mix(0.55, 1.6, smoothstep(uBord * 0.55, uBord, d));
  a *= smoothstep(uTrou.z * 0.35, uTrou.z, length(p - uTrou.xy));
  a *= smoothstep(uPointeur.z * 0.3, uPointeur.z, length(p - uPointeur.xy));
  a *= 1.0 - smoothstep(uBord + 8.0, uBord + 16.0, d);
  // Few opacity steps, nudged by the dither so the steps break into pixels only along their edges.
  float q = floor(clamp(a, 0.0, 1.0) * 6.0 + bayer(gl_FragCoord.xy) * 0.6) / 6.0;
  if (q <= 0.0) discard;
  gl_FragColor = vec4(brouiller(uCouleur * (0.9 + 0.2 * n), vMonde), q * 0.78);
}
`

/** The wall of mist standing on the edge of the world. */
export const MUR_FRAG = /* glsl */ `
uniform float uTemps;
uniform vec3 uCouleur;
uniform float uHaut;
varying vec3 vMonde;
${BRUIT}
void main() {
  float ang = atan(vMonde.z, vMonde.x);
  float h = vMonde.y / uHaut;
  float n = fbm(vec2(ang * 9.0 + uTemps * 0.03, h * 3.0 - uTemps * 0.05));
  float a = (1.0 - smoothstep(0.35, 1.0, h)) * (0.55 + 0.6 * n);
  a *= smoothstep(-0.05, 0.08, h);
  float q = floor(clamp(a, 0.0, 1.0) * 6.0 + bayer(gl_FragCoord.xy) * 0.6) / 6.0;
  if (q <= 0.0) discard;
  gl_FragColor = vec4(uCouleur * (0.85 + 0.3 * n), q * 0.9);
}
`

/** The sea: dark in the open, lighter on the shallows, with slow dithered glints. */
export const MER_FRAG = /* glsl */ `
uniform float uTemps;
uniform vec3 uProfond;
uniform vec3 uClair;
varying vec3 vMonde;
${BRUIT}
void main() {
  vec2 p = vMonde.xz;
  float vague = fbm(p * 0.9 + vec2(uTemps * 0.05, -uTemps * 0.03));
  vec3 c = mix(uProfond, uClair, vague * 0.35);
  float reflet = step(0.74, fbm(p * 2.4 + uTemps * 0.08)) * step(bayer(gl_FragCoord.xy), 0.35);
  c += reflet * 0.06;
  gl_FragColor = vec4(brouiller(c, vMonde), 1.0);
}
`

/** The sky: a vertical gradient from the mist on the horizon to the night above, dithered into bands. */
export const CIEL_FRAG = /* glsl */ `
uniform vec3 uHorizon;
uniform vec3 uZenith;
varying vec3 vMonde;
${BRUIT}
void main() {
  float h = normalize(vMonde - cameraPosition).y;
  float t = smoothstep(-0.05, 0.6, h);
  t = floor(t * 14.0 + bayer(gl_FragCoord.xy)) / 14.0;
  gl_FragColor = vec4(mix(uHorizon, uZenith, t), 1.0);
}
`
