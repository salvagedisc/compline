// The disc seen from the evening sky, drawn with plain three.js: relief and colours sampled on the bench server,
// the sea, the ring of mist on the edge, and layers of cloud the camera dives through when a place is chosen.
// Units: 1 = 100 blocks. The disc edge (6 000 blocks) sits at 60, the sampled square spans -62..62.
import {
  type BufferGeometry, CircleGeometry, Color, LinearFilter, LinearSRGBColorSpace, Mesh, PerspectiveCamera, PlaneGeometry, RepeatWrapping,
  RingGeometry, SRGBColorSpace, Scene, ShaderMaterial, TextureLoader, Vector2, Vector3, WebGLRenderer, type Texture,
} from 'three'
import { damp, damp2, damp3 } from 'maath/easing'
import { LIEUX, type Lieu } from '../donnees/lieux'
import { lireEtat } from '../interface/etat'

export type Vue = { type: 'accueil' } | { type: 'lieu'; lieu: Lieu } | { type: 'haut' }

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
    // A long, slow swell catching the low sun: a broad sheen rather than glints, which read as specks from the sky.
    vec2 p = vMonde.xz * 0.006 + vec2(uTemps * 0.0016, uTemps * -0.0011);
    vec2 q = vMonde.xz * 0.013 + vec2(uTemps * -0.0009, uTemps * 0.0014);
    vec2 pente = vec2(texture2D(uNuages, p).g + texture2D(uNuages, q + 0.37).g * 0.6,
                      texture2D(uNuages, p + 0.51).g + texture2D(uNuages, q + 0.13).g * 0.6) - 0.8;
    vec3 v = normalize(cameraPosition - vMonde);
    vec3 n = normalize(vec3(pente.x * 0.22, 1.0, pente.y * 0.22));
    float spec = pow(max(dot(reflect(-uSoleil, n), v), 0.0), 14.0);
    col += vec3(1.0, 0.8, 0.6) * spec * 0.16 * (1.0 - smoothstep(110.0, 260.0, r));
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

// Cloud layers between the camera and the ground: thin near the top, a little thicker lower down.
// Each layer covers about a tenth of the sky; together they leave most of the ground in view.
const COUCHES = [
  { y: 9, couv: 0.8, op: 0.6 },
  { y: 18, couv: 0.78, op: 0.78 },
  { y: 30, couv: 0.77, op: 0.85 },
  { y: 46, couv: 0.8, op: 0.72 },
]
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

/**
 * Where the whole disc should sit on screen at home, in CSS pixels: the part of the screen the text leaves free.
 * Wide screens keep a column on the left for the title; phones held upright keep the top for it.
 */
function zoneLibre(w: number, h: number) {
  if (w / h < 1) return { x0: 0, x1: w, y0: h * 0.36, y1: h - 56 }
  if (h < 520) return { x0: w * 0.4, x1: w, y0: 0, y1: h }
  return { x0: w * (w < 1200 ? 0.4 : 0.36), x1: w, y0: 40, y1: h }
}

const RAYON = 60 // the disc edge
const PENTE = new Vector3(0, 0.9, 0.44).normalize() // direction from what the camera looks at, back to the camera

type Uniforme<T> = { value: T }

/** Size and lift of the place picture shown on hovering a pin (matches .repere-carte in styles.css). */
const CARTE = { w: 240, h: 150, dessus: 92 }

/** One sky in one canvas. The page tells it where to look; it draws on its own and moves the place pins. */
export class Monde {
  private readonly rendu: WebGLRenderer
  private readonly scene = new Scene()
  private readonly camera = new PerspectiveCamera(34, 1, 0.5, 900)
  // Uniforms shared by every material (plain three keeps a shared uniform object shared).
  private readonly communs = {
    uTemps: { value: 0 } as Uniforme<number>,
    uNuages: { value: null } as Uniforme<Texture | null>,
    uVent: { value: VENT },
    uBrume: { value: COUL.brume },
    uSoleil: { value: SOLEIL },
    uDegage: { value: 0 } as Uniforme<number>,
    uDebutBrume: { value: 90 } as Uniforme<number>,
  }
  private readonly tempsNuages: Uniforme<number>[] = []
  private readonly jetables: { dispose(): void }[] = []
  private readonly points = new Map<string, Vector3>()
  private vue: Vue = { type: 'accueil' }
  private calme = false
  private actif = true
  private charge = false
  private images = 0
  private premier = true
  private raf = 0
  private readonly debut = performance.now()
  private dernier = performance.now()
  private W = 1
  private H = 1
  private readonly pointeur = new Vector2()
  private readonly cible = new Vector3(0, 0, 4)
  private readonly voulu = { pos: new Vector3(), cible: new Vector3() }
  // Where the centre of the view sits on screen, as an offset from the middle in pixels (eased like the camera).
  private readonly centre = { voulu: new Vector2(), actuel: new Vector2() }
  private readonly tmp = new Vector3()
  private readonly regul = { n: 0, total: 0, dpr: 1 }
  private readonly observateur: ResizeObserver
  private fini = false

  private readonly toile: HTMLCanvasElement
  private readonly reperes: () => (HTMLElement | null)[] | null
  private readonly surPret: () => void

  constructor(
    toile: HTMLCanvasElement,
    reglages: { mobile: boolean; base: string },
    reperes: () => (HTMLElement | null)[] | null,
    surPret: () => void,
  ) {
    this.toile = toile
    this.reperes = reperes
    this.surPret = surPret
    this.rendu = new WebGLRenderer({ canvas: toile, antialias: true, powerPreference: 'high-performance', alpha: false })
    this.regul.dpr = Math.min(window.devicePixelRatio || 1, reglages.mobile ? 1.5 : 1.75)
    this.rendu.setPixelRatio(this.regul.dpr)
    this.rendu.setClearColor(COUL.brume)
    this.observateur = new ResizeObserver(() => this.mesurer())
    this.observateur.observe(toile)
    this.mesurer()
    window.addEventListener('pointermove', this.bouger, { passive: true })
    void this.charger(reglages)
  }

  /** Where to look, and how: called by the page whenever the address or the settings change. */
  regler(r: { vue: Vue; calme: boolean; actif: boolean }) {
    this.vue = r.vue
    this.calme = r.calme
    this.actif = r.actif
    this.demander()
  }

  detruire() {
    this.fini = true
    cancelAnimationFrame(this.raf)
    this.observateur.disconnect()
    window.removeEventListener('pointermove', this.bouger)
    for (const j of this.jetables) j.dispose()
    this.rendu.dispose()
  }

  private bouger = (e: PointerEvent) => {
    this.pointeur.set((e.clientX / window.innerWidth) * 2 - 1, -(e.clientY / window.innerHeight) * 2 + 1)
  }

  private mesurer() {
    this.W = Math.max(1, this.toile.clientWidth)
    this.H = Math.max(1, this.toile.clientHeight)
    this.rendu.setSize(this.W, this.H, false)
    this.camera.aspect = this.W / this.H
    this.camera.updateProjectionMatrix()
    this.demander()
  }

  private async charger({ mobile, base }: { mobile: boolean; base: string }) {
    const chargeur = new TextureLoader()
    const [couleurs, hauteurs, nuages] = await Promise.all(
      ['couleurs.webp', 'hauteurs.png', 'nuages.webp'].map((f) => chargeur.loadAsync(`${base}monde/${f}`)),
    )
    if (this.fini) { couleurs.dispose(); hauteurs.dispose(); nuages.dispose(); return }
    couleurs.colorSpace = SRGBColorSpace
    couleurs.anisotropy = Math.min(8, this.rendu.capabilities.getMaxAnisotropy())
    hauteurs.colorSpace = LinearSRGBColorSpace
    hauteurs.minFilter = hauteurs.magFilter = LinearFilter
    hauteurs.generateMipmaps = false
    nuages.colorSpace = LinearSRGBColorSpace
    nuages.wrapS = nuages.wrapT = RepeatWrapping
    this.jetables.push(couleurs, hauteurs, nuages)
    this.communs.uNuages.value = nuages

    const h = lecteurHauteurs(hauteurs.image as HTMLImageElement)
    for (const l of LIEUX) this.points.set(l.id, versMonde(l, h))

    // The sea, far below everything.
    this.ajouter(new CircleGeometry(420, 64), new ShaderMaterial({
      uniforms: { ...this.communs, uMer: { value: COUL.mer }, uMerLoin: { value: COUL.merLoin } },
      vertexShader: MER_VERT, fragmentShader: MER_FRAG,
    }), (m) => { m.rotation.x = -Math.PI / 2; m.position.y = -0.04; m.renderOrder = 0 })

    // The ground: one plane displaced by the height map.
    const n = mobile ? 255 : 383
    const sol = new PlaneGeometry(DEMI * 2, DEMI * 2, n, n)
    this.ajouter(sol, new ShaderMaterial({
      uniforms: { ...this.communs, uCouleurs: { value: couleurs }, uHauteurs: { value: hauteurs }, uDemi: { value: DEMI }, uExag: { value: EXAG }, uTexel: { value: 1 / 775 } },
      vertexShader: SOL_VERT, fragmentShader: SOL_FRAG, transparent: true,
    }), (m) => { m.renderOrder = 1; m.frustumCulled = false })

    // The ring of mist on the edge, in two thin sheets.
    const anneau = new RingGeometry(52, 86, 160, 1)
    ;[0.6, 2.2].forEach((y, i) => this.ajouter(anneau, new ShaderMaterial({
      uniforms: { ...this.communs, uCouleur: { value: COUL.murBrume } },
      vertexShader: MER_VERT, fragmentShader: ANNEAU_FRAG, transparent: true, depthWrite: false,
    }), (m) => { m.rotation.x = -Math.PI / 2; m.position.y = y; m.renderOrder = 2 + i }))

    // Cloud layers, each drifting at its own pace.
    const plan = new PlaneGeometry(520, 520)
    const couches = mobile ? COUCHES.filter((_, i) => i % 2 === 1) : COUCHES
    couches.forEach((c, i) => {
      const temps = { value: 0 }
      this.tempsNuages.push(temps)
      this.ajouter(plan, new ShaderMaterial({
        uniforms: {
          ...this.communs, uTemps: temps,
          uDecal: { value: new Vector2(i * 0.173, i * -0.311) },
          uLumiere: { value: COUL.nuageLumiere }, uOmbre: { value: COUL.nuageOmbre },
          uCouverture: { value: c.couv }, uOpacite: { value: c.op }, uY: { value: c.y },
        },
        vertexShader: MER_VERT, fragmentShader: NUAGE_FRAG, transparent: true, depthWrite: false,
      }), (m) => { m.rotation.x = -Math.PI / 2; m.position.y = c.y; m.renderOrder = 10 + i })
    })

    this.charge = true
    this.demander()
  }

  private ajouter(g: BufferGeometry, mat: ShaderMaterial, regler: (m: Mesh) => void) {
    const m = new Mesh(g, mat)
    regler(m)
    this.scene.add(m)
    if (!this.jetables.includes(g)) this.jetables.push(g)
    this.jetables.push(mat)
  }

  /** Draws one frame soon; the loop keeps going by itself while the sky is in view and allowed to move. */
  private demander() {
    if (!this.raf && !this.fini) this.raf = requestAnimationFrame(this.boucle)
  }

  private boucle = (maintenant: number) => {
    this.raf = 0
    const dt = Math.max(0, (maintenant - this.dernier) / 1000)
    this.dernier = maintenant
    if (!this.charge) return
    this.dessiner((maintenant - this.debut) / 1000, dt)
    if (++this.images === 2) this.surPret()
    if (this.actif && !this.calme) this.demander()
  }

  private fov() { return this.W / this.H < 1 ? 46 : 34 }

  private placer(t: number) {
    const { voulu, centre, W, H } = this
    const vue = this.vue
    if (vue.type === 'lieu') {
      const p = this.points.get(vue.lieu.id)!
      voulu.cible.copy(p)
      voulu.pos.set(p.x + 3, p.y + 16, p.z + 11)
      centre.voulu.set(0, 0)
      return
    }
    // Far enough back for the disc to fill the free part of the screen, and that part's centre as the view's centre.
    const z = vue.type === 'accueil' ? zoneLibre(W, H) : { x0: 0, x1: W, y0: 0, y1: H }
    const echelle = H / (2 * Math.tan((this.fov() * Math.PI) / 360)) // pixels per unit at distance 1
    const recul = Math.max(
      (2 * RAYON * echelle) / ((z.x1 - z.x0) * 1.04),
      (2 * RAYON * PENTE.y * echelle) / ((z.y1 - z.y0) * 1.02),
    ) * (vue.type === 'haut' ? 1.15 : 1)
    centre.voulu.set((z.x0 + z.x1) / 2 - W / 2, (z.y0 + z.y1) / 2 - H / 2)
    const derive = this.calme ? 0 : 1
    voulu.cible.set(Math.sin(t * 0.021) * 3 * derive, 0, 2 + Math.cos(t * 0.017) * 2 * derive)
    voulu.pos.copy(voulu.cible).addScaledVector(PENTE, recul)
    if (!this.calme) { voulu.pos.x += this.pointeur.x * 4; voulu.pos.z -= this.pointeur.y * 3 }
  }

  private dessiner(t: number, dt: number) {
    const { camera, voulu, centre, communs, W, H } = this

    // The mist lifts once the visitor comes in; a visitor already inside finds it lifted at once.
    const entre = lireEtat().entre || 'entre' in document.documentElement.dataset
    if (this.calme || (entre && this.premier)) communs.uDegage.value = entre ? 1 : 0
    else damp(communs.uDegage, 'value', entre ? 1 : 0, 1.4, Math.min(dt, 0.1))
    communs.uTemps.value = t
    this.tempsNuages.forEach((u, i) => { u.value = t * (1 + i * 0.12) })

    // Camera: a slow drift over the whole disc at home, a dive through the clouds onto a place.
    this.placer(t)
    if (this.premier || this.calme) {
      camera.position.copy(voulu.pos)
      this.cible.copy(voulu.cible)
      centre.actuel.copy(centre.voulu)
      this.premier = false
    } else {
      const lent = this.vue.type === 'lieu' ? 0.9 : 1.3
      const pas = Math.min(dt, 0.05)
      damp3(camera.position, voulu.pos, lent, pas)
      damp3(this.cible, voulu.cible, lent * 0.8, pas)
      damp2(centre.actuel, centre.voulu, lent * 0.8, pas)
    }
    camera.lookAt(this.cible)

    // Off-axis view: the camera still looks straight at the disc, only the picture is slid towards the free part.
    const fov = this.fov()
    const cx = W / 2 + centre.actuel.x, cy = H / 2 + centre.actuel.y
    const FW = 2 * Math.max(cx, W - cx), FH = 2 * Math.max(cy, H - cy)
    camera.fov = (360 / Math.PI) * Math.atan(Math.tan((fov * Math.PI) / 360) * (FH / H))
    camera.setViewOffset(FW, FH, FW / 2 - cx, FH / 2 - cy, W, H)
    communs.uDebutBrume.value = camera.position.distanceTo(this.cible) * 0.85

    this.epingler()
    this.reguler(dt)
    this.rendu.render(this.scene, camera)
  }

  /**
   * Moves the place pins over the canvas, without going through React. Each name goes to the right of its mark
   * unless that would leave the screen or cover another pin; then it hangs on the left.
   */
  private epingler() {
    const els = this.reperes()
    if (!els) return
    const { W, H, tmp, camera } = this
    const petit = W <= 760 || H <= 520
    const ecart = petit ? 20 : 28, dessous = petit ? 33 : 40, marque = petit ? 16 : 20, haut = petit ? 62 : 76
    const boites: number[][] = []
    const ordre: { el: HTMLElement; x: number; y: number }[] = []
    LIEUX.forEach((l, i) => {
      const el = els[i]
      const p = this.points.get(l.id)
      if (!el || !p) return
      tmp.copy(p); tmp.y += 0.4
      tmp.project(camera)
      const x = (tmp.x * 0.5 + 0.5) * W
      const y = (-tmp.y * 0.5 + 0.5) * H
      el.style.transform = `translate3d(${x.toFixed(1)}px, ${y.toFixed(1)}px, 0)`
      ordre.push({ el, x, y })
      boites.push([x - marque, y - haut, x + marque, y - haut + marque * 2])
    })
    const touche = (b: number[]) => boites.some((o) => b[0] < o[2] && b[2] > o[0] && b[1] < o[3] && b[3] > o[1])
    ordre.sort((a, b) => a.y - b.y).forEach(({ el, x, y }) => {
      const nom = el.lastElementChild as HTMLElement
      const w = nom.offsetWidth, h = nom.offsetHeight
      const droite = [x + ecart, y - dessous - h, x + ecart + w, y - dessous]
      const gauche = [x - ecart - w, droite[1], x - ecart, droite[3]]
      const aGauche = (droite[2] > W - 8 || touche(droite)) && gauche[0] > 8 && !touche(gauche)
      el.classList.toggle('a-gauche', aGauche)
      boites.push(aGauche ? gauche : droite)
      // The picture shown on hover hangs above the pin, or below it near the top, and stays on screen.
      el.classList.toggle('carte-bas', y - CARTE.dessus - CARTE.h < 64)
      const decal = Math.max(0, 12 - (x - CARTE.w / 2)) + Math.min(0, W - 12 - (x + CARTE.w / 2))
      el.style.setProperty('--carte-dx', `${decal.toFixed(0)}px`)
    })
  }

  /**
   * Keeps the sky smooth on modest machines: when frames come too slowly for a couple of seconds, the canvas draws
   * fewer pixels (down to one per screen pixel). It never goes back up during the visit.
   */
  private reguler(dt: number) {
    const r = this.regul
    if (dt > 0.25 || !(this.actif && !this.calme)) return // a stall, a hidden tab or a single requested frame
    r.n++; r.total += dt
    if (r.n < 90) return
    const moyenne = r.total / r.n
    r.n = 0; r.total = 0
    if (moyenne > 1 / 42 && r.dpr > 1) {
      r.dpr = Math.max(1, Math.round((r.dpr - 0.25) * 100) / 100)
      this.rendu.setPixelRatio(r.dpr)
      this.rendu.setSize(this.W, this.H, false)
    }
  }
}
