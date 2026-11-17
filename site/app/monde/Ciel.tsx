// The disc seen from the evening sky: relief and colours sampled on the bench server, the sea, the ring of mist
// on the edge, and layers of cloud the camera dives through when a place is chosen.
// Units: 1 = 100 blocks. The disc edge (6 000 blocks) sits at 60, the sampled square spans -62..62.

import { Suspense, useEffect, useLayoutEffect, useMemo, useRef } from 'react'
import { Canvas, useFrame, useLoader, useThree } from '@react-three/fiber'
import { easing } from 'maath'
import {
  Color, LinearFilter, TextureLoader, LinearSRGBColorSpace, RepeatWrapping, SRGBColorSpace, Vector2, Vector3,
  type PerspectiveCamera, type ShaderMaterial, type Texture,
} from 'three'
import { LIEUX, type Lieu } from '../donnees/lieux'
import { lireEtat, signalerMondePret } from '../interface/etat'

export type Vue = { type: 'accueil' } | { type: 'lieu'; lieu: Lieu } | { type: 'haut' }

interface Props {
  vue: Vue
  calme: boolean // reduced motion: no drift, cuts instead of flights
  actif: boolean // false once a page covers the sky: no frame is drawn until something changes
  mobile: boolean
  base: string
  reperes: React.RefObject<(HTMLElement | null)[]> // pins of the places, moved over the canvas every frame
}

const DEMI = 62 // half the side of the sampled square
const EXAG = 2.1 // relief exaggeration, so the mountains read from the sky
const BAS = -40, HAUT = 260 // height range of hauteurs.png, in blocks around sea level
const hauteurBlocs = (v: number) => BAS + v * (HAUT - BAS)
const versY = (blocs: number) => (Math.max(blocs, 0) / 100) * EXAG

const COUL = {
  brume: new Color('#5c6672'),
  mer: new Color('#132530'),
  merLoin: new Color('#2a3540'),
  soleil: new Color('#ffcf9c'),
  nuageLumiere: new Color('#efe3d2'),
  nuageOmbre: new Color('#6c7482'),
  murBrume: new Color('#b7bec4'),
}
const SOLEIL = new Vector3(-0.62, 0.42, -0.36).normalize()
const VENT = new Vector2(0.0042, 0.0016)
// 0 while the title screen hides the disc under thick cloud, 1 once the mist has lifted.
const DEGAGE = { value: 0 }
// Distance from the camera where the evening haze starts: a little short of what it looks at.
const DEBUT_BRUME = { value: 90 }

// Shared uniforms: time, cloud texture and wind, so cloud shadows on the ground follow the clouds above.
function useCommuns(nuages: Texture) {
  return useMemo(() => ({
    uTemps: { value: 0 },
    uNuages: { value: nuages },
    uVent: { value: VENT },
    uBrume: { value: COUL.brume },
    uSoleil: { value: SOLEIL },
    uDegage: { value: 0 },
    uDebutBrume: { value: 90 },
  }), [nuages])
}

/**
 * R3F copies each uniform when it builds a material, so a shared uniform object would not stay shared:
 * time and the mist are pushed into every material on each frame instead.
 */
function useAnimer(mats: React.RefObject<(ShaderMaterial | null)[]>, vitesse = 1) {
  useFrame((s) => {
    for (const m of mats.current ?? []) {
      if (!m) continue
      m.uniforms.uTemps.value = s.clock.elapsedTime * vitesse
      m.uniforms.uDegage.value = DEGAGE.value
      m.uniforms.uDebutBrume.value = DEBUT_BRUME.value
    }
  })
}

const BRUME_GLSL = /* glsl */ `
  uniform vec3 uBrume;
  uniform float uDebutBrume;
  vec3 brumer(vec3 c, float dist) {
    float f = 1.0 - exp(-pow(max(dist - uDebutBrume, 0.0) * 0.0062, 1.6));
    return mix(c, uBrume, clamp(f, 0.0, 0.92));
  }
`

const SOL_VERT = /* glsl */ `
  uniform sampler2D uHauteurs;
  uniform float uDemi, uExag, uTexel;
  varying vec2 vUv;
  varying vec3 vMonde;
  varying vec3 vNormale;
  float y(vec2 uv) {
    float b = -40.0 + texture2D(uHauteurs, uv).r * 300.0;
    return max(b, 0.0) / 100.0 * uExag;
  }
  void main() {
    vUv = uv;
    // Texture rows are flipped on upload, so v = 1 is the north edge (z = -62), like the plane's top edge.
    vec2 t = uv;
    float h = y(t);
    float e = uTexel;
    float dx = y(t + vec2(e, 0.0)) - y(t - vec2(e, 0.0));
    float dz = y(t + vec2(0.0, e)) - y(t - vec2(0.0, e));
    float pas = 2.0 * e * 2.0 * uDemi;
    vNormale = normalize(vec3(-dx / pas, 1.0, dz / pas)); // v grows towards -z
    vec4 m = modelMatrix * vec4(position.x, h, -position.y, 1.0);
    vMonde = m.xyz;
    gl_Position = projectionMatrix * viewMatrix * m;
  }
`

const SOL_FRAG = /* glsl */ `
  uniform sampler2D uCouleurs, uNuages;
  uniform vec2 uVent;
  uniform vec3 uSoleil;
  uniform float uTemps;
  varying vec2 vUv;
  varying vec3 vMonde;
  varying vec3 vNormale;
  ${BRUME_GLSL}
  void main() {
    vec4 c = texture2D(uCouleurs, vUv);
    if (c.a < 0.01) discard;
    // Relief is baked in the colours; a little live light adds depth when the camera comes close.
    float l = dot(normalize(vNormale), uSoleil);
    vec3 col = c.rgb * (0.86 + 0.22 * l);
    // Evening grade: warm on the sunward slopes, cool in the hollows.
    col *= mix(vec3(0.82, 0.86, 0.96), vec3(1.06, 0.98, 0.88), clamp(l * 1.6, 0.0, 1.0));
    // Shadows of the clouds drifting above, cast slightly away from the sun.
    vec2 p = (vMonde.xz - uSoleil.xz * 22.0) * 0.0045 + uVent * uTemps;
    float n = texture2D(uNuages, p).r + (texture2D(uNuages, p * 2.6 + 0.21).g - 0.5) * 0.22;
    col *= 1.0 - 0.3 * smoothstep(0.74, 0.9, n);
    col = brumer(col * 0.9, length(vMonde - cameraPosition));
    gl_FragColor = vec4(col, c.a);
    #include <colorspace_fragment>
  }
`

const MER_VERT = /* glsl */ `
  varying vec3 vMonde;
  void main() {
    vec4 m = modelMatrix * vec4(position, 1.0);
    vMonde = m.xyz;
    gl_Position = projectionMatrix * viewMatrix * m;
  }
`

const MER_FRAG = /* glsl */ `
  uniform vec3 uMer, uMerLoin, uSoleil;
  uniform sampler2D uNuages;
  uniform vec2 uVent;
  uniform float uTemps;
  varying vec3 vMonde;
  ${BRUME_GLSL}
  void main() {
    float r = length(vMonde.xz);
    vec3 col = mix(uMer, uMerLoin, smoothstep(60.0, 140.0, r));
    // Slow swell catching the low sun.
    vec2 p = vMonde.xz * 0.02 + vec2(uTemps * 0.004, uTemps * -0.003);
    float h = texture2D(uNuages, p).g;
    vec3 v = normalize(cameraPosition - vMonde);
    vec3 n = normalize(vec3((h - 0.5) * 0.5, 1.0, (texture2D(uNuages, p + 0.37).g - 0.5) * 0.5));
    float spec = pow(max(dot(reflect(-uSoleil, n), v), 0.0), 40.0);
    col += vec3(1.0, 0.78, 0.55) * spec * 0.35;
    gl_FragColor = vec4(brumer(col, length(vMonde - cameraPosition)), 1.0);
    #include <colorspace_fragment>
  }
`

const ANNEAU_FRAG = /* glsl */ `
  uniform vec3 uCouleur;
  uniform sampler2D uNuages;
  uniform float uTemps;
  varying vec3 vMonde;
  ${BRUME_GLSL}
  void main() {
    float r = length(vMonde.xz);
    float a = atan(vMonde.z, vMonde.x);
    vec2 p = vec2(a * 3.0 + uTemps * 0.01, r * 0.05 - uTemps * 0.006);
    float n = texture2D(uNuages, p * 0.5).r + (texture2D(uNuages, p * 1.2).g - 0.5) * 0.3;
    // A band of mist that thickens from the shore to the edge, then thins out over open sea.
    float bande = smoothstep(55.0, 61.0, r) * (1.0 - smoothstep(64.0, 82.0, r));
    float alpha = bande * smoothstep(0.3, 0.75, n) * 0.85;
    gl_FragColor = vec4(brumer(uCouleur, length(vMonde - cameraPosition)), alpha);
    #include <colorspace_fragment>
  }
`

const NUAGE_FRAG = /* glsl */ `
  uniform sampler2D uNuages;
  uniform vec2 uVent, uDecal;
  uniform vec3 uSoleil, uLumiere, uOmbre;
  uniform float uTemps, uCouverture, uOpacite, uY, uDegage;
  varying vec3 vMonde;
  ${BRUME_GLSL}
  float densite(vec2 p) {
    return texture2D(uNuages, p).r + (texture2D(uNuages, p * 2.6 + 0.21).g - 0.5) * 0.22;
  }
  void main() {
    vec2 p = vMonde.xz * 0.0045 + uVent * uTemps + uDecal;
    float d = densite(p);
    float couv = uCouverture - 0.42 * (1.0 - uDegage);
    float dens = smoothstep(couv, couv + 0.24, d);
    if (dens < 0.003) discard;
    dens = pow(dens, 1.3);
    // Lit edge: the cloud is brighter where it thins towards the sun.
    float versSoleil = densite(p + uSoleil.xz * 0.012);
    float lum = clamp(0.6 + (d - versSoleil) * 6.0, 0.0, 1.0);
    vec3 col = mix(uOmbre, uLumiere, lum);
    // Thin edges let the evening light through; thick cores sit in their own shade.
    col = mix(col, uOmbre * 0.85, smoothstep(couv + 0.14, couv + 0.45, d) * 0.3);
    col = mix(col, uLumiere * 1.04, (1.0 - dens) * 0.35);
    // Fade a layer as the camera crosses it, so no hard cut ever shows.
    float travers = smoothstep(0.6, 6.0, abs(cameraPosition.y - uY));
    float alpha = dens * mix(0.97, uOpacite, uDegage) * travers;
    gl_FragColor = vec4(brumer(col, length(vMonde - cameraPosition)), alpha);
    #include <colorspace_fragment>
  }
`

function Sol({ couleurs, hauteurs, nuages, mobile }: { couleurs: Texture; hauteurs: Texture; nuages: Texture; mobile: boolean }) {
  const mats = useRef<(ShaderMaterial | null)[]>([])
  const communs = useCommuns(nuages)
  const uniforms = useMemo(() => ({
    ...communs, uCouleurs: { value: couleurs }, uHauteurs: { value: hauteurs },
    uDemi: { value: DEMI }, uExag: { value: EXAG }, uTexel: { value: 1 / 775 },
  }), [communs, couleurs, hauteurs])
  useAnimer(mats)
  const n = mobile ? 255 : 383
  return (
    <mesh renderOrder={1}>
      <planeGeometry args={[DEMI * 2, DEMI * 2, n, n]} />
      <shaderMaterial ref={(m) => { mats.current[0] = m }} transparent uniforms={uniforms} vertexShader={SOL_VERT} fragmentShader={SOL_FRAG} />
    </mesh>
  )
}

function Mer({ nuages }: { nuages: Texture }) {
  const mats = useRef<(ShaderMaterial | null)[]>([])
  const communs = useCommuns(nuages)
  const uniforms = useMemo(() => ({ ...communs, uMer: { value: COUL.mer }, uMerLoin: { value: COUL.merLoin } }), [communs])
  useAnimer(mats)
  return (
    <mesh rotation-x={-Math.PI / 2} position-y={-0.04} renderOrder={0}>
      <circleGeometry args={[420, 64]} />
      <shaderMaterial ref={(m) => { mats.current[0] = m }} uniforms={uniforms} vertexShader={MER_VERT} fragmentShader={MER_FRAG} />
    </mesh>
  )
}

function AnneauDeBrume({ nuages }: { nuages: Texture }) {
  const mats = useRef<(ShaderMaterial | null)[]>([])
  const communs = useCommuns(nuages)
  const uniforms = useMemo(() => ({ ...communs, uCouleur: { value: COUL.murBrume } }), [communs])
  useAnimer(mats)
  return (
    <>
      {[0.6, 2.2].map((y, i) => (
        <mesh key={y} rotation-x={-Math.PI / 2} position-y={y} renderOrder={2 + i}>
          <ringGeometry args={[52, 86, 160, 1]} />
          <shaderMaterial ref={(m) => { mats.current[i] = m }} transparent depthWrite={false} uniforms={uniforms} vertexShader={MER_VERT} fragmentShader={ANNEAU_FRAG} />
        </mesh>
      ))}
    </>
  )
}

// Cloud layers between the camera and the ground: thin near the top, a little thicker lower down.
// Each layer covers about a tenth of the sky; together they leave most of the ground in view.
const COUCHES = [
  { y: 9, couv: 0.8, op: 0.6 },
  { y: 18, couv: 0.78, op: 0.78 },
  { y: 30, couv: 0.77, op: 0.85 },
  { y: 46, couv: 0.8, op: 0.72 },
]

function Nuage({ y, couv, op, i, nuages }: { y: number; couv: number; op: number; i: number; nuages: Texture }) {
  const mats = useRef<(ShaderMaterial | null)[]>([])
  const communs = useCommuns(nuages)
  const uniforms = useMemo(() => ({
    ...communs,
    uDecal: { value: new Vector2(i * 0.173, i * -0.311) },
    uLumiere: { value: COUL.nuageLumiere }, uOmbre: { value: COUL.nuageOmbre },
    uCouverture: { value: couv }, uOpacite: { value: op }, uY: { value: y },
  }), [communs, couv, op, y, i])
  useAnimer(mats, 1 + i * 0.12)
  return (
    <mesh rotation-x={-Math.PI / 2} position-y={y} renderOrder={10 + i}>
      <planeGeometry args={[520, 520]} />
      <shaderMaterial ref={(m) => { mats.current[0] = m }} transparent depthWrite={false} uniforms={uniforms} vertexShader={MER_VERT} fragmentShader={NUAGE_FRAG} />
    </mesh>
  )
}

/** Height of the ground under a map position (fractions of the map image), read once from hauteurs.png. */
function lecteurHauteurs(img: HTMLImageElement | ImageBitmap) {
  const c = document.createElement('canvas')
  c.width = img.width; c.height = img.height
  const g = c.getContext('2d', { willReadFrequently: true })!
  g.drawImage(img, 0, 0)
  const d = g.getImageData(0, 0, c.width, c.height).data
  return (fx: number, fy: number) => {
    const x = Math.min(c.width - 1, Math.max(0, Math.round(fx * (c.width - 1))))
    const y = Math.min(c.height - 1, Math.max(0, Math.round(fy * (c.height - 1))))
    return versY(hauteurBlocs(d[(y * c.width + x) * 4] / 255))
  }
}

const versMonde = (l: Lieu, h: (fx: number, fy: number) => number) =>
  new Vector3((l.x - 0.5) * DEMI * 2, h(l.x, l.y), (l.y - 0.5) * DEMI * 2)

/** Camera director: a slow drift over the whole disc at home, a dive through the clouds onto a place. */
function Camera({ vue, calme, mobile, points, reperes }: {
  vue: Vue; calme: boolean; mobile: boolean; points: Map<string, Vector3>; reperes: Props['reperes']
}) {
  const { camera, size, invalidate } = useThree()
  const pointer = useMemo(() => new Vector2(), [])
  useEffect(() => {
    const f = (e: PointerEvent) => pointer.set((e.clientX / window.innerWidth) * 2 - 1, -(e.clientY / window.innerHeight) * 2 + 1)
    window.addEventListener('pointermove', f, { passive: true })
    return () => window.removeEventListener('pointermove', f)
  }, [pointer])
  const cible = useRef(new Vector3(0, 0, 4))
  const premier = useRef(true)
  const tmp = useMemo(() => new Vector3(), [])
  const voulu = useMemo(() => ({ pos: new Vector3(), cible: new Vector3() }), [])

  const placer = (t: number) => {
    const portrait = size.width / size.height < 1
    if (vue.type === 'lieu') {
      const p = points.get(vue.lieu.id)!
      voulu.cible.copy(p)
      voulu.pos.set(p.x + 3, p.y + 16, p.z + 11)
    } else {
      // Far enough for the whole disc to fit; phones in portrait stand further back.
      const recul = portrait ? 290 : mobile ? 175 : 158
      const derive = calme ? 0 : 1
      // On wide screens the disc sits right of centre, leaving the sea on the left for the title.
      const decale = portrait ? 0 : -Math.min(24, (size.width / size.height - 1) * 32)
      // In portrait the title takes the top of the screen: the camera looks north of the disc so it sits lower.
      voulu.cible.set(decale + Math.sin(t * 0.021) * 3 * derive, 0, (portrait ? -44 : 5) + Math.cos(t * 0.017) * 2 * derive)
      voulu.pos.set(
        voulu.cible.x + (calme ? 0 : pointer.x * 4),
        recul * 0.9,
        voulu.cible.z + recul * 0.44 + (calme ? 0 : -pointer.y * 3),
      )
      if (vue.type === 'haut') voulu.pos.y += 30
    }
  }

  useFrame((s, dt) => {
    // A visitor already inside (a later page of the visit) finds the mist lifted at once.
    const entre = lireEtat().entre || 'entre' in document.documentElement.dataset
    if (calme || (entre && premier.current)) DEGAGE.value = entre ? 1 : 0
    else easing.damp(DEGAGE, 'value', entre ? 1 : 0, 1.4, Math.min(dt, 0.1))
    placer(s.clock.elapsedTime)
    if (premier.current || calme) {
      camera.position.copy(voulu.pos)
      cible.current.copy(voulu.cible)
      premier.current = false
    } else {
      const lent = vue.type === 'lieu' ? 0.9 : 1.3
      easing.damp3(camera.position, voulu.pos, lent, Math.min(dt, 0.05))
      easing.damp3(cible.current, voulu.cible, lent * 0.8, Math.min(dt, 0.05))
    }
    camera.lookAt(cible.current)
    DEBUT_BRUME.value = camera.position.distanceTo(cible.current) * 0.85

    // Move the place pins over the canvas, without going through React.
    const els = reperes.current
    LIEUX.forEach((l, i) => {
      const el = els?.[i]
      const p = points.get(l.id)
      if (!el || !p) return
      tmp.copy(p); tmp.y += 0.4
      tmp.project(camera)
      const x = (tmp.x * 0.5 + 0.5) * size.width
      const y = (-tmp.y * 0.5 + 0.5) * size.height
      el.style.transform = `translate3d(${x.toFixed(1)}px, ${y.toFixed(1)}px, 0)`
    })
  })

  useEffect(() => { invalidate() }, [vue, size, invalidate])
  useLayoutEffect(() => {
    const c = camera as PerspectiveCamera
    c.fov = size.width / size.height < 1 ? 46 : 34
    c.updateProjectionMatrix()
  }, [camera, size])
  return null
}

function Scene({ vue, calme, mobile, base, reperes }: Props) {
  const [couleurs, hauteurs, nuages] = useLoader(TextureLoader, [`${base}monde/couleurs.webp`, `${base}monde/hauteurs.png`, `${base}monde/nuages.webp`])
  const { gl } = useThree()

  useLayoutEffect(() => {
    couleurs.colorSpace = SRGBColorSpace
    couleurs.anisotropy = Math.min(8, gl.capabilities.getMaxAnisotropy())
    hauteurs.colorSpace = LinearSRGBColorSpace
    hauteurs.minFilter = hauteurs.magFilter = LinearFilter
    hauteurs.generateMipmaps = false
    nuages.colorSpace = LinearSRGBColorSpace
    nuages.wrapS = nuages.wrapT = RepeatWrapping
    for (const t of [couleurs, hauteurs, nuages]) t.needsUpdate = true
  }, [couleurs, hauteurs, nuages, gl])

  const points = useMemo(() => {
    const h = lecteurHauteurs(hauteurs.image as HTMLImageElement)
    return new Map(LIEUX.map((l) => [l.id, versMonde(l, h)]))
  }, [hauteurs])

  useEffect(() => {
    // Two frames after the textures are up, the sky can be shown.
    const id = requestAnimationFrame(() => requestAnimationFrame(() => signalerMondePret()))
    return () => cancelAnimationFrame(id)
  }, [])

  const couches = mobile ? COUCHES.filter((_, i) => i % 2 === 1) : COUCHES
  return (
    <>
      <Mer nuages={nuages} />
      <Sol couleurs={couleurs} hauteurs={hauteurs} nuages={nuages} mobile={mobile} />
      <AnneauDeBrume nuages={nuages} />
      {couches.map((c, i) => <Nuage key={c.y} {...c} i={i} nuages={nuages} />)}
      <Camera vue={vue} calme={calme} mobile={mobile} points={points} reperes={reperes} />
    </>
  )
}

export default function Ciel(props: Props) {
  return (
    <Canvas
      className="ciel-canvas"
      dpr={props.mobile ? [1, 1.5] : [1, 1.75]}
      gl={{ antialias: true, powerPreference: 'high-performance', alpha: false }}
      camera={{ fov: 34, near: 0.5, far: 900, position: [0, 140, 70] }}
      frameloop={props.actif && !props.calme ? 'always' : 'demand'}
      onCreated={({ gl }) => gl.setClearColor(COUL.brume)}
      aria-hidden="true"
    >
      <Suspense fallback={null}>
        <Scene {...props} />
      </Suspense>
    </Canvas>
  )
}
