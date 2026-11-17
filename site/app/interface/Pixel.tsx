// 16x16 pixel icons drawn from character grids, like item sprites.
// Each character maps to a palette colour; '.' is transparent.

const PAL: Record<string, string> = {
  k: '#1b1d1c', w: '#e8e6dc', g: '#9aa39b', d: '#5d655f', b: '#4f8fc2', B: '#9cc7ea',
  o: '#e3a74b', O: '#a8641f', r: '#c2533b', n: '#7a5131', N: '#4e321f', v: '#7fae55',
  V: '#4f7a37', s: '#3f6a93', S: '#8fb4d6', y: '#e9d07a', p: '#9a5fc0', c: '#5fb3a8',
}

const ICONES: Record<string, string[]> = {
  disque: [
    '................', '.....kkkkkk.....', '...kkbbbbbbkk...', '..kbbvvvbbbbbk..', '.kbbvvVvvvbbbbk.', '.kbvvVVvvyvvbbk.',
    'kbbvvvvvvyyvvbbk', 'kbvvwwvvvvvvvbbk', 'kbvvwwvvvoovvbbk', 'kbbvvvvvoOovvbbk', '.kbbvvvvvoovbbk.', '.kbbbvvvvvvbbbk.',
    '..kbbbbvvvbbbk..', '...kkbbbbbbkk...', '.....kkkkkk.....', '................',
  ],
  lac: [
    '................', '..........o.....', '.........ooo....', '..........o.....', '................', '...V......V.....',
    '..VVV....VVV....', '.VVVVV..VVVVV...', 'kkNkkkkkkkNkkkkk', 'bbbbbbbbbbbbbbbb', 'bBBbbbbbBBbbbbbb', 'bbbbbBBbbbbbBBbb',
    'sbbbbbbbbbbbbbbs', 'sssbbbbbBBbbbsss', 'ssssssssssssssss', '................',
  ],
  cimes: [
    '................', '.......w........', '......www.......', '.....wwwww......', '....wwgwwww.....', '...wwgggwwww....',
    '...dgggggwwwg...', '..ddggggggwggg..', '..dddggggggggd..', '.ddddgggggggddd.', '.dddddgggggdddd.', 'ddddddddgggddddd',
    'dddddddddddddddd', '................', '................', '................',
  ],
  foyer: [
    '................', '......g.g.......', '.....g.g.g......', '......g.g.......', '..kkkkkkkkkkkk..', '.kddddddddddddk.',
    '.kdggggggggggdk.', '.kdgyyyyyyyygdk.', '.kdgyooyyooygdk.', '.kdggggggggggdk.', '..kddddddddddk..', '...kkkkkkkkkk...',
    '....o..oo..o....', '...ooroorrooo...', '..NNnnNNnnNNnn..', '................',
  ],
  riviere: [
    '................', '................', '.........kk.....', '.......kkSSk.....', '.....kkSSSSSk...', '...kkSSSSSkSSk..',
    '.kkSSSSSSSSSSSk.', 'kSSSwSSSSSSSSSk.', 'kSSSSSSSSSSSSk.k', '.kkSSSSSSSSSSk.k', '...kkSSSSSSkk.kk', '.....kkSSSk.....',
    '.......kkk......', '................', '................', '................',
  ],
  forge: [
    '................', '................', '................', '.kkkkkkkkkkkk...', 'kddddddddddddkkk', 'kdgggggggggggddk',
    '.kkkkddddkkkkkk.', '.....kddk.......', '.....kddk.......', '....kddddk......', '...kddddddk.....', '..kddddddddk....',
    '..kkkkkkkkkk....', '................', '................', '................',
  ],
  zoo: [
    '................', '...kk.....kk....', '..kNNk...kNNk...', '..kNNk...kNNk...', '...kk.kk..kk....', '.kk..kNNk...kk..',
    'kNNk.kNNk..kNNk.', 'kNNk..kk...kNNk.', '.kk.........kk..', '.....kkkkk......', '....kNNNNNk.....', '...kNNNNNNNk....',
    '...kNNNNNNNk....', '....kNNNNNk.....', '.....kkkkk......', '................',
  ],
  avancement: [
    '................', '..kkkkkkkkkkk...', '..kwwwwwwwwwkk..', '..kwkkwwwwwwk.k.', '..kwwwwwwwwwkkkk', '..kwvvvvvvvvwwk.',
    '..kwvvvvvvwwwwk.', '..kwwwwwwwwwwwk.', '..kwooooowwwwwk.', '..kwwwwwwwwwwwk.', '..kwggwwwwwwwwk.', '..kwwwwwwwwwwwk.',
    '..kkkkkkkkkkkkk.', '................', '................', '................',
  ],
  journal: [
    '................', '...kkkkkkkkkk...', '..krrrrrrrrrrk..', '..krkkkkkkkkrk..', '..krkwwwwwwkrk..', '..krkwggggwkrk..',
    '..krkwwwwwwkrk..', '..krkwggggwkrk..', '..krkwwwwwwkrk..', '..krkwggwwwkrk..', '..krkwwwwwwkrk..', '..krkkkkkkkkrk..',
    '..krrrrrrrrrrk..', '...kkkkkkkkkk...', '................', '................',
  ],
}

export function Pixel({ nom, taille = 32 }: { nom: string; taille?: number }) {
  const g = ICONES[nom] ?? ICONES.disque
  const rects: React.ReactElement[] = []
  g.forEach((ligne, y) => {
    for (let x = 0; x < ligne.length; x++) {
      const col = PAL[ligne[x]]
      if (col) rects.push(<rect key={`${x}-${y}`} x={x} y={y} width={1.02} height={1.02} fill={col} />)
    }
  })
  return (
    <svg className="pixel" viewBox="0 0 16 16" width={taille} height={taille} shapeRendering="crispEdges" aria-hidden="true">
      {rects}
    </svg>
  )
}
