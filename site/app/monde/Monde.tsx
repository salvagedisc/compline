// The disc in 3D: voxel relief from the bench sample, sea, mist layers, the wall of mist on the edge,
// and a camera that drifts around the world or dives onto the chosen place.

import { useEffect, useMemo, useRef, useState } from 'react'
import { Canvas, useFrame, useThree } from '@react-three/fiber'
import { Html } from '@react-three/drei'
import { easing } from 'maath'
import {
  BackSide, Color, DoubleSide, FogExp2, Plane, Vector2, Vector3, type BufferGeometry, type ShaderMaterial,
} from 'three'
import { LIEUX, type Lieu } from '../donnees/lieux'
import { Pixel } from '../interface/Pixel'
import { signalerMondePret } from '../interface/etat'
import { BORD, chargerRelief, construireRelief, hauteurEn, versMonde, type Relief } from './relief'
import { BRUME_FRAG, BRUME_VERT, CIEL_FRAG, MER_FRAG, MUR_FRAG } from './shaders'

export type Vue =
  | { type: 'accueil' }
  | { type: 'lieu'; lieu: Lieu }
  | { type: 'haut' } // the whole disc seen from above, behind the journal and the road map

interface Props {
  vue: Vue
  calme: boolean // reduced motion: no drift, cuts instead of flights
  onChoisir: (l: Lieu) => void
  reliefUrl: string
}

const C = {
  brouillard: new Color('#7d8a86'),
  horizon: new Color('#8b9893'),
  zenith: new Color('#18232a'),
  brume: new Color('#c3cac1'),
  murBrume: new Color('#d3d8cf'),
  merProfond: new Color('#284f5f'),
  merClair: new Color('#4a8193'),
  soleil: new Color('#ffd2a1'),
  ciel: new Color('#a7b9c0'),
  sol: new Color('#2a2924'),
}
const DENS = 0.0052

const communs = () => ({
  uTemps: { value: 0 },
  uBrouillard: { value: C.brouillard },
  uDensBrouillard: { value: DENS },
})

function Ciel() {
  const uniforms = useMemo(() => ({ uHorizon: { value: C.horizon }, uZenith: { value: C.zenith } }), [])
  return (
    <mesh renderOrder={-1}>
      <sphereGeometry args={[400, 32, 16]} />
      <shaderMaterial side={BackSide} depthWrite={false} uniforms={uniforms} vertexShader={BRUME_VERT} fragmentShader={CIEL_FRAG} />
    </mesh>
  )
}

function Mer() {
  const mat = useRef<ShaderMaterial>(null)
  const uniforms = useMemo(() => ({ ...communs(), uProfond: { value: C.merProfond }, uClair: { value: C.merClair } }), [])
  useFrame((s) => { if (mat.current) mat.current.uniforms.uTemps.value = s.clock.elapsedTime })
  return (
    <mesh rotation-x={-Math.PI / 2} position-y={-0.06}>
      <circleGeometry args={[BORD + 30, 96]} />
      <shaderMaterial ref={mat} uniforms={uniforms} vertexShader={BRUME_VERT} fragmentShader={MER_FRAG} />
    </mesh>
  )
}

function MurDeBrume() {
  const mat = useRef<ShaderMaterial>(null)
  const H = 26
  const uniforms = useMemo(() => ({ ...communs(), uCouleur: { value: C.murBrume }, uHaut: { value: H } }), [])
  useFrame((s) => { if (mat.current) mat.current.uniforms.uTemps.value = s.clock.elapsedTime })
  return (
    <mesh position-y={H / 2 - 1}>
      <cylinderGeometry args={[BORD + 0.5, BORD + 0.5, H, 160, 1, true]} />
      <shaderMaterial ref={mat} side={DoubleSide} transparent depthWrite={false} uniforms={uniforms} vertexShader={BRUME_VERT} fragmentShader={MUR_FRAG} />
    </mesh>
  )
}

interface Couche { y: number; echelle: number; derive: [number, number]; densite: number }
const COUCHES: Couche[] = [
  { y: 2.2, echelle: 0.055, derive: [0.025, 0.012], densite: 0.55 },
  { y: 3.8, echelle: 0.085, derive: [-0.02, 0.018], densite: 0.42 },
  { y: 6.0, echelle: 0.12, derive: [0.015, -0.025], densite: 0.3 },
]

/** One drifting layer of mist. Clearings follow the chosen place and the pointer. */
function Brume({ c, trou, pointeur }: { c: Couche; trou: Vector3; pointeur: Vector3 }) {
  const mat = useRef<ShaderMaterial>(null)
  const uniforms = useMemo(() => ({
    ...communs(),
    uDensite: { value: c.densite }, uEchelle: { value: c.echelle }, uDerive: { value: new Vector2(...c.derive) },
    uCouleur: { value: C.brume }, uTrou: { value: trou }, uPointeur: { value: pointeur }, uBord: { value: BORD },
  }), [c, trou, pointeur])
  useFrame((s) => { if (mat.current) mat.current.uniforms.uTemps.value = s.clock.elapsedTime })
  return (
    <mesh rotation-x={-Math.PI / 2} position-y={c.y}>
      <circleGeometry args={[BORD + 18, 96]} />
      <shaderMaterial ref={mat} side={DoubleSide} transparent depthWrite={false} uniforms={uniforms} vertexShader={BRUME_VERT} fragmentShader={BRUME_FRAG} />
    </mesh>
  )
}

function Repere({ lieu, relief, actif, discret, onChoisir }: { lieu: Lieu; relief: Relief; actif: boolean; discret: boolean; onChoisir: (l: Lieu) => void }) {
  const { x, z } = versMonde(lieu.x, lieu.y)
  const y = hauteurEn(relief, x, z) + 1.2
  return (
    <Html position={[x, y, z]} center zIndexRange={[20, 10]} style={{ pointerEvents: discret ? 'none' : 'auto' }}>
      <button
        type="button"
        className={`repere${actif ? ' actif' : ''}${discret ? ' discret' : ''}`}
        onClick={() => onChoisir(lieu)}
        tabIndex={-1} // the hotbar is the keyboard route to every place
        aria-hidden="true"
      >
        <span className="repere-icone"><Pixel nom={lieu.id} taille={26} /></span>
        <span className="repere-nom">{lieu.nom}</span>
      </button>
    </Html>
  )
}

/** Moves the camera towards where the current view wants it, and keeps the clearings in place. */
function Camera({ vue, calme, relief, trou, pointeur }: { vue: Vue; calme: boolean; relief: Relief; trou: Vector3; pointeur: Vector3 }) {
  const { camera, size, pointer, raycaster } = useThree()
  const regard = useRef(new Vector3(0, 0, 0))
  const angle = useRef(0.6)
  const premier = useRef(true)
  const plan = useMemo(() => new Plane(new Vector3(0, 1, 0), -1), [])
  const touche = useMemo(() => new Vector3(), [])
  const but = useMemo(() => ({ pos: new Vector3(), regard: new Vector3(), trou: 0, decal: new Vector2() }), [])
  const decal = useRef(new Vector2())

  useFrame((_, dt) => {
    const large = size.width > 900
    if (vue.type === 'lieu') {
      const { x, z } = versMonde(vue.lieu.x, vue.lieu.y)
      const sol = hauteurEn(relief, x, z)
      const dir = new Vector2(x, z)
      if (dir.length() < 4) dir.set(0, 1)
      dir.normalize()
      but.pos.set(x + dir.x * 21, sol + 17, z + dir.y * 21)
      but.regard.set(x, sol, z)
      but.trou = 10
      // Keep the place clear of the text panel: to the right on wide screens, above it on narrow ones.
      but.decal.set(large ? -0.2 : 0, large ? 0 : 0.18)
    } else if (vue.type === 'haut') {
      but.pos.set(0, 120, 34)
      but.regard.set(0, 0, 2)
      but.trou = 0
      but.decal.set(0, 0)
    } else {
      if (!calme) angle.current += dt * 0.018
      const a = angle.current
      but.pos.set(Math.sin(a) * 118, 96, Math.cos(a) * 118)
      but.regard.set(0, -6, 0)
      but.trou = 0
      // The disc sits right of the title on wide screens.
      but.decal.set(large ? -0.17 : 0, large ? 0 : 0.12)
    }
    const lent = vue.type === 'lieu' ? 1.1 : 1.6
    if (calme || premier.current) {
      camera.position.copy(but.pos)
      regard.current.copy(but.regard)
      decal.current.copy(but.decal)
      trou.z = but.trou
      premier.current = false
    } else {
      easing.damp3(camera.position, but.pos, lent, dt)
      easing.damp3(regard.current, but.regard, lent * 0.8, dt)
      easing.damp2(decal.current, but.decal, lent, dt)
      trou.z += (but.trou - trou.z) * Math.min(1, dt * 1.5)
    }
    camera.lookAt(regard.current)
    trou.x = but.regard.x
    trou.y = but.regard.z
    const w = size.width, h = size.height
    if (decal.current.lengthSq() > 1e-6) camera.setViewOffset(w, h, decal.current.x * w, decal.current.y * h, w, h)
    else camera.clearViewOffset()

    // The pointer pushes the mist away where it touches the world.
    raycaster.setFromCamera(pointer, camera)
    if (raycaster.ray.intersectPlane(plan, touche)) {
      pointeur.x += (touche.x - pointeur.x) * Math.min(1, dt * 6)
      pointeur.y += (touche.z - pointeur.y) * Math.min(1, dt * 6)
      pointeur.z = vue.type === 'lieu' ? 3 : 5
    }
  })
  return null
}

function Scene({ vue, calme, onChoisir, reliefUrl }: Props) {
  const [relief, setRelief] = useState<Relief | null>(null)
  const [geo, setGeo] = useState<BufferGeometry | null>(null)
  const trou = useMemo(() => new Vector3(0, 0, 0), [])
  const pointeur = useMemo(() => new Vector3(999, 999, 0), [])
  const { scene } = useThree()

  useEffect(() => {
    scene.fog = new FogExp2(C.brouillard, DENS)
    let vivant = true
    chargerRelief(reliefUrl).then((r) => {
      if (!vivant) return
      setRelief(r)
      setGeo(construireRelief(r))
      signalerMondePret()
    })
    return () => { vivant = false }
  }, [reliefUrl, scene])
  useEffect(() => () => geo?.dispose(), [geo])

  const actif = vue.type === 'lieu' ? vue.lieu.id : null
  return (
    <>
      <Ciel />
      <hemisphereLight args={[C.ciel, C.sol, 1.1]} />
      <directionalLight color={C.soleil} intensity={1.9} position={[-60, 34, 22]} />
      <Mer />
      {geo && (
        <mesh geometry={geo}>
          <meshLambertMaterial vertexColors />
        </mesh>
      )}
      {COUCHES.map((c) => <Brume key={c.y} c={c} trou={trou} pointeur={pointeur} />)}
      <MurDeBrume />
      {relief && LIEUX.map((l) => (
        <Repere key={l.id} lieu={l} relief={relief} actif={actif === l.id} discret={vue.type !== 'accueil' && actif !== l.id} onChoisir={onChoisir} />
      ))}
      {relief && <Camera vue={vue} calme={calme} relief={relief} trou={trou} pointeur={pointeur} />}
    </>
  )
}

/** Renders at a low resolution and scales up without smoothing, so the world reads as pixels. */
export default function Monde(props: Props) {
  return (
    <Canvas
      className="monde-canvas"
      dpr={0.42}
      gl={{ antialias: false, powerPreference: 'high-performance' }}
      camera={{ fov: 38, near: 0.5, far: 900, position: [0, 46, 66] }}
      frameloop={props.calme ? 'demand' : 'always'}
      aria-hidden="true"
    >
      <Scene {...props} />
    </Canvas>
  )
}
