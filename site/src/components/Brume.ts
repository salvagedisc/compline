// Pixel mist drawn on a low-resolution canvas and scaled up with crisp pixels,
// the way the game draws its fog. Clearings are painted into a mask that
// slowly heals, so the mist lifts where the cursor goes and comes back after.

function hash(x: number, y: number) {
  const n = Math.sin(x * 127.1 + y * 311.7) * 43758.5453
  return n - Math.floor(n)
}
const smooth = (t: number) => t * t * (3 - 2 * t)
function noise(x: number, y: number) {
  const xi = Math.floor(x), yi = Math.floor(y), xf = x - xi, yf = y - yi
  const a = hash(xi, yi), b = hash(xi + 1, yi), c = hash(xi, yi + 1), d = hash(xi + 1, yi + 1)
  const u = smooth(xf), v = smooth(yf)
  return a + (b - a) * u + (c - a) * v + (a - b - c + d) * u * v
}

export interface BrumeOptions {
  base: number // mist density at rest, 0..1
  colour: [number, number, number]
  nuit?: [number, number, number] // colour of the mist far from the disc
}

export class Brume {
  private ctx: CanvasRenderingContext2D
  private img: ImageData
  private clear: Float32Array
  private w: number
  private h: number
  private t = 0
  private raf = 0
  private visible = true
  /** Extra clearing for the whole field, 0..1, animated by the caller. */
  lift = 0
  /** Extra mist over everything, 0..1: the opening veil that lifts on arrival. */
  voile = 0
  /** Fixed clear spots (places on the map), in canvas cells. */
  holes: { x: number; y: number; r: number; a: number }[] = []
  /** The disc on screen, in canvas cells: mist thins over it and thickens beyond its edge. */
  disc: { x: number; y: number; r: number } | null = null
  private canvas: HTMLCanvasElement
  private opts: BrumeOptions
  constructor(canvas: HTMLCanvasElement, opts: BrumeOptions, cols = 200) {
    this.canvas = canvas
    this.opts = opts
    const rect = canvas.getBoundingClientRect()
    this.w = cols
    this.h = Math.max(40, Math.round(cols * (rect.height / Math.max(1, rect.width))))
    canvas.width = this.w
    canvas.height = this.h
    this.ctx = canvas.getContext('2d')!
    this.img = this.ctx.createImageData(this.w, this.h)
    this.clear = new Float32Array(this.w * this.h)
  }
  get cols() { return this.w }
  get rows() { return this.h }

  /** Clears a disc of mist at a client position. */
  stir(clientX: number, clientY: number, radius = 14, strength = 0.35) {
    const r = this.canvas.getBoundingClientRect()
    const cx = ((clientX - r.left) / r.width) * this.w
    const cy = ((clientY - r.top) / r.height) * this.h
    for (let y = Math.max(0, (cy - radius) | 0); y < Math.min(this.h, cy + radius + 1); y++)
      for (let x = Math.max(0, (cx - radius) | 0); x < Math.min(this.w, cx + radius + 1); x++) {
        const d = Math.hypot(x - cx, (y - cy) * 1.1) / radius
        if (d < 1) {
          const i = y * this.w + x
          this.clear[i] = Math.min(1, this.clear[i] + (1 - d) * strength)
        }
      }
  }

  draw() {
    const d = this.img.data
    const [cr, cg, cb] = this.opts.colour
    let k = 0
    for (let y = 0; y < this.h; y++) {
      for (let x = 0; x < this.w; x++, k++) {
        const n = noise(x / 26 + this.t, y / 18) * 0.65 + noise(x / 10 - this.t * 1.7, y / 8 + this.t * 0.4) * 0.35
        let a = this.opts.base + (n - 0.5) * 0.55 - this.clear[k]
        // Beyond the edge the mist darkens into the night around the disc.
        let nuit = 0
        if (this.disc) {
          const dd = Math.hypot(x - this.disc.x, y - this.disc.y) / this.disc.r
          a += dd < 0.85 ? -0.12 : Math.min(0.2, (dd - 0.85) * 0.8)
          nuit = Math.max(0, Math.min(1, (dd - 0.92) * 2.2))
        }
        for (const hole of this.holes) {
          const dd = Math.hypot(x - hole.x, (y - hole.y) * 1.1) / hole.r
          if (dd < 1) a -= (1 - dd * dd) * hole.a
        }
        a *= 1 - this.lift
        a += this.voile * (1 - a)
        if (a < 0) a = 0
        if (a > 0.97) a = 0.97
        const [nr, ng, nb] = this.opts.nuit ?? this.opts.colour
        d[k * 4] = cr + (nr - cr) * nuit
        d[k * 4 + 1] = cg + (ng - cg) * nuit
        d[k * 4 + 2] = cb + (nb - cb) * nuit
        d[k * 4 + 3] = a * 255
      }
    }
    this.ctx.putImageData(this.img, 0, 0)
  }

  start(animate: boolean) {
    const io = new IntersectionObserver((es) => { this.visible = es[0].isIntersecting })
    io.observe(this.canvas)
    const loop = () => {
      if (this.visible) {
        this.t += 0.0022
        for (let i = 0; i < this.clear.length; i++) this.clear[i] *= 0.993
        this.draw()
      }
      this.raf = requestAnimationFrame(loop)
    }
    this.draw()
    if (animate) this.raf = requestAnimationFrame(loop)
    return () => { cancelAnimationFrame(this.raf); io.disconnect() }
  }
}
