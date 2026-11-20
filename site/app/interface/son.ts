// Ambient sound made in the browser: wind through the mist and, now and then, a distant bell.
// Nothing is downloaded and nothing plays before the visitor asks for it.

let ctx: AudioContext | null = null
let maitre: GainNode | null = null
let cloches: number | null = null

function bruitBrun(c: AudioContext, secondes: number) {
  const b = c.createBuffer(1, c.sampleRate * secondes, c.sampleRate)
  const d = b.getChannelData(0)
  let dernier = 0
  for (let i = 0; i < d.length; i++) {
    dernier = (dernier + 0.02 * (Math.random() * 2 - 1)) / 1.02
    d[i] = dernier * 3.5
  }
  return b
}

function reverberation(c: AudioContext) {
  const long = c.sampleRate * 4
  const b = c.createBuffer(2, long, c.sampleRate)
  for (let k = 0; k < 2; k++) {
    const d = b.getChannelData(k)
    for (let i = 0; i < long; i++) d[i] = (Math.random() * 2 - 1) * (1 - i / long) ** 3
  }
  const r = c.createConvolver()
  r.buffer = b
  return r
}

function vent(c: AudioContext, sortie: AudioNode) {
  const src = c.createBufferSource()
  src.buffer = bruitBrun(c, 8)
  src.loop = true
  const filtre = c.createBiquadFilter()
  filtre.type = 'lowpass'
  filtre.frequency.value = 420
  filtre.Q.value = 0.6
  // Slow gusts: the cutoff and the level breathe on two unrelated periods.
  const lfo = c.createOscillator(), lfoGain = c.createGain()
  lfo.frequency.value = 0.07; lfoGain.gain.value = 260
  lfo.connect(lfoGain).connect(filtre.frequency)
  const g = c.createGain(); g.gain.value = 0.55
  const lfo2 = c.createOscillator(), lfo2Gain = c.createGain()
  lfo2.frequency.value = 0.043; lfo2Gain.gain.value = 0.22
  lfo2.connect(lfo2Gain).connect(g.gain)
  src.connect(filtre).connect(g).connect(sortie)
  src.start(); lfo.start(); lfo2.start()
}

// Partials of a struck bell, relative to its nominal note.
const PARTIELS: [number, number][] = [[0.5, 0.6], [1, 1], [1.183, 0.45], [1.506, 0.35], [2, 0.3], [2.514, 0.18], [2.662, 0.12], [3.011, 0.08]]
const NOTES = [146.8, 164.8, 196, 220, 246.9] // D3 E3 G3 A3 B3

function cloche(c: AudioContext, sortie: AudioNode) {
  const f = NOTES[Math.floor(Math.random() * NOTES.length)]
  const t = c.currentTime + 0.05
  const g = c.createGain()
  g.gain.value = 0.05 + Math.random() * 0.03
  g.connect(sortie)
  for (const [r, a] of PARTIELS) {
    const o = c.createOscillator(), e = c.createGain()
    o.frequency.value = f * r
    e.gain.setValueAtTime(0, t)
    e.gain.linearRampToValueAtTime(a, t + 0.01)
    e.gain.exponentialRampToValueAtTime(0.0001, t + 7 / r ** 0.6)
    o.connect(e).connect(g)
    o.start(t)
    o.stop(t + 8)
  }
}

export function demarrerSon() {
  if (!ctx) {
    ctx = new AudioContext()
    maitre = ctx.createGain()
    maitre.gain.value = 0
    maitre.connect(ctx.destination)
    const rev = reverberation(ctx), sec = ctx.createGain(), mouille = ctx.createGain()
    sec.gain.value = 0.7; mouille.gain.value = 0.5
    sec.connect(maitre); rev.connect(mouille).connect(maitre)
    vent(ctx, sec)
    const versCloches = ctx.createGain()
    versCloches.connect(rev)
    const sonner = () => {
      if (ctx && ctx.state === 'running') cloche(ctx, versCloches)
      cloches = window.setTimeout(sonner, 9000 + Math.random() * 14000)
    }
    cloches = window.setTimeout(sonner, 2500)
  }
  void ctx.resume()
  maitre!.gain.cancelScheduledValues(ctx.currentTime)
  maitre!.gain.linearRampToValueAtTime(0.9, ctx.currentTime + 2.5)
}

export function couperSon() {
  if (!ctx || !maitre) return
  maitre.gain.cancelScheduledValues(ctx.currentTime)
  maitre.gain.setValueAtTime(maitre.gain.value, ctx.currentTime)
  maitre.gain.linearRampToValueAtTime(0, ctx.currentTime + 0.6)
  const c = ctx
  window.setTimeout(() => { if (maitre && maitre.gain.value < 0.01) void c.suspend() }, 700)
}

export function arreterTout() {
  if (cloches) window.clearTimeout(cloches)
  void ctx?.close()
  ctx = null; maitre = null; cloches = null
}
