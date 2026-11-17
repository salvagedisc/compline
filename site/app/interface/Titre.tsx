// A heading whose letters come out of the mist one by one (GSAP SplitText), or simply appear
// when the visitor prefers less motion.
import { useRef } from 'react'
import gsap from 'gsap'
import { SplitText } from 'gsap/SplitText'
import { useGSAP } from '@gsap/react'

interface Props { children: string; id?: string; className?: string; niveau?: 1 | 2; delai?: number }

export function Titre({ children, id, className, niveau = 1, delai = 0.15 }: Props) {
  const ref = useRef<HTMLHeadingElement>(null)
  useGSAP(() => {
    gsap.registerPlugin(SplitText)
    gsap.ticker.lagSmoothing(0) // keep titles on time even when the first frames are slow
    const mm = gsap.matchMedia()
    mm.add('(prefers-reduced-motion: no-preference)', () => {
      const decoupe = SplitText.create(ref.current!, { type: 'chars,words', aria: 'auto' })
      gsap.from(decoupe.chars, {
        opacity: 0, filter: 'blur(12px)', yPercent: 30, duration: 1.2, delay: delai, ease: 'power3.out',
        stagger: { each: 0.04, from: 'random' },
      })
      return () => decoupe.revert()
    })
    return () => mm.revert()
  }, { scope: ref, dependencies: [children] })
  const H = niveau === 1 ? 'h1' : 'h2'
  return <H ref={ref} id={id} className={className}>{children}</H>
}
