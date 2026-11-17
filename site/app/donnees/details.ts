// Content of the special chapters (metal tiers, zoo stamps, guide families). Texts in French.
import apercevoir from '../tampons/apercevoir.png'
import photographier from '../tampons/photographier.png'
import apprivoiser from '../tampons/apprivoiser.png'
import maitriser from '../tampons/maitriser.png'
import terre from '../tampons/terre.png'
import mer from '../tampons/mer.png'
import ciel from '../tampons/ciel.png'
import noires from '../tampons/noires.png'

export const ROMAINS = ['I', 'II', 'III', 'IV', 'V', 'VI', 'VII', 'VIII', 'IX', 'X', 'XI', 'XII']

export interface Palier { nom: string; ou: string; teinte: string; objet: string; lignes: string[] }

export const PALIERS: Palier[] = [
  { nom: 'Fer', ou: 'La forge', teinte: '#c9c3b8', objet: 'Lingot de fer',
    lignes: ["On chauffe, on martèle sur l'enclume, et chaque pièce sort avec une qualité.", 'Plus la pièce est bien forgée, plus elle dure.'] },
  { nom: 'Acier', ou: 'Le four à alliage', teinte: '#8e9aa3', objet: 'Acier brut',
    lignes: ['Trois fers et un charbon au four à alliage donnent trois aciers bruts, à marteler.', "On n'en trouve plus tout prêt dans les coffres."] },
  { nom: 'Acier noir', ou: "L'enclume de maître", teinte: '#47709a', objet: "Lingot d'acier noir",
    lignes: ["Acier, magnétite et charbon de bois, martelé puis refroidi : une teinte d'acier bleui.", "Seule sa pioche peut miner le diamant."] },
  { nom: 'Diamant', ou: 'Près de la lave profonde', teinte: '#8ad8d2', objet: 'Diamant brut',
    lignes: ['Les diamants ne viennent plus que de pierres brutes près de la lave, tout en bas.', "Le gabarit se gagne en réveillant le cœur ancien d'un monument des conifères."] },
  { nom: 'Orichalque', ou: 'Le cœur brûlant', teinte: '#c08454', objet: "Lingot d'orichalque",
    lignes: ['Des débris antiques dorment près des lacs de lave, sous la couche −40.', "Le four se bâtit en magma ; Archie vend le gabarit une fois l'obsidicératops apprivoisé."] },
]

export const PIOCHE = "Une pioche experte, parfaite ou de maître donne 10, 20 ou 30 % de chances d'un minerai en plus."

export const DINOS = [
  { nom: 'Obsidicératops', milieu: 'Chaleur', ouvre: 'Approcher la lave' },
  { nom: 'Chorusoatl', milieu: 'Froid', ouvre: 'Les hauteurs glacées' },
  { nom: 'Coralosaure', milieu: 'Eau', ouvre: 'Les fonds marins' },
  { nom: 'Therimoss', milieu: 'Chaleur sèche', ouvre: 'Les terres arides' },
]

/** The four stamps of every beast, cut from the guide as it appears in the game. */
export interface Tampon { id: string; nom: string; image: string; comment: string; geste: string }

export const TAMPONS: Tampon[] = [
  { id: 'apercevoir', nom: 'Apercevoir', image: apercevoir, geste: 'Vous la voyez',
    comment: "L'avoir en vue, sans obstacle, à moins de 25 blocs. Sa silhouette à l'encre prend ses couleurs." },
  { id: 'photographier', nom: 'Photographier', image: photographier, geste: 'Clic',
    comment: "La prendre en photo avec un appareil. Le tampon tombe quand la bête est dans le cadre." },
  { id: 'apprivoiser', nom: 'Apprivoiser ou héberger', image: apprivoiser, geste: 'Elle vous suit',
    comment: "L'apprivoiser, la faire naître ou la mettre en seau. Aucun tampon ne demande de la tuer." },
  { id: 'maitriser', nom: 'Maîtriser', image: maitriser, geste: 'Une minute à ses côtés',
    comment: "Avoir les trois autres tampons, puis l'observer de près. C'est le seul qui se mérite." },
]

export interface Famille { id: string; nom: string; image: string; especes: number; couleur: string }

// Species counted on the bench server with the full pack, as the guide shows them.
export const FAMILLES: Famille[] = [
  { id: 'terre', nom: 'Terre ferme', image: terre, especes: 131, couleur: '#3f8f3a' },
  { id: 'mer', nom: 'Grand bleu', image: mer, especes: 96, couleur: '#2aa6b8' },
  { id: 'ciel', nom: 'Plein ciel', image: ciel, especes: 31, couleur: '#4a9ad8' },
  { id: 'noires', nom: 'Bêtes noires', image: noires, especes: 8, couleur: '#9a3bb8' },
]

export const TOTAL_ESPECES = FAMILLES.reduce((n, f) => n + f.especes, 0)
