// Content of the site: every place on the disc map and what it shows.
// Texts are in French (the site is for French-speaking friends); code and comments stay in English.

export type Statut = 'teste' | 'atester' | 'encours' | 'prevu'

export const STATUTS: Record<Statut, { nom: string; court: string }> = {
  teste: { nom: 'Testé en jeu', court: 'Testé' },
  atester: { nom: 'Fait, à essayer en jeu', court: 'À essayer' },
  encours: { nom: 'En cours', court: 'En cours' },
  prevu: { nom: 'Prévu', court: 'Prévu' },
}

/**
 * A detail inside a screenshot. x and y place its centre as fractions of the image width and height;
 * r is half its width as a fraction of the image width; ratio is its height over its width (1, a square,
 * when omitted). The detail is pushed back inside the image when it would overflow an edge.
 */
export interface Zoom { x: number; y: number; r: number; ratio?: number; label: string }

export type Media =
  | { type: 'image'; capture: string; alt: string; zooms?: Zoom[]; provisoire?: boolean }
  | { type: 'comparer'; scenes: { nom: string; avant: string; apres: string }[]; avantNom: string; apresNom: string; provisoire?: boolean }
  | { type: 'echelle' }
  | { type: 'zoo' }
  | { type: 'carte' }

export interface Fonction {
  id: string
  titre: string
  statut: Statut
  texte: string
  points?: string[]
  media: Media
}

export interface Lieu {
  id: string
  chemin: string // URL of its page, also listed in chemins.ts
  touche: number // hotbar slot, 1 to 7
  nom: string // place name on the map
  theme: string // what the place is about
  x: number // position on the map, fraction of its width
  y: number
  intro: string
  fond: string // capture file name of the place's background
  fonctions: Fonction[]
}


export const LIEUX: Lieu[] = [
  {
    id: 'disque', chemin: 'le-disque', touche: 1, nom: 'Le disque', theme: 'Le monde', x: 0.47, y: 0.5,
    intro: "Un seul continent rond, large de douze kilomètres, froid au nord et chaud au sud. Au bord, la brume ne laisse passer personne.",
    fond: 'bru-vallee.jpg',
    fonctions: [
      { id: 'forme', titre: 'Un monde rond', statut: 'atester', media: { type: 'carte' },
        texte: "Quelle que soit la graine, le monde est un disque : la côte vers 5 700 blocs du centre, le bord à 6 000.",
        points: ['Environ 74 km² de terre', 'Climat réglé par la latitude', 'Mer tout autour'] },
      { id: 'bord', titre: 'Le bord gardé par la brume', statut: 'atester',
        texte: "Qui s'approche du bord est prévenu, puis ramené en arrière, à pied, à la nage ou en bateau.",
        media: { type: 'image', capture: 'bord.jpg', alt: 'En barque à quelques dizaines de blocs du bord, le message de la brume affiché',
          zooms: [{ x: 0.5, y: 0.745, r: 0.3, ratio: 0.3, label: 'Le message du bord' }] } },
      { id: 'atlas', titre: "L'atlas dessiné à la main", statut: 'teste',
        texte: "Pas de coordonnées ni de minicarte : la carte se dessine pendant qu'on explore, et trains et navires y apparaissent.",
        media: { type: 'image', capture: 'atlas-carte.jpg', alt: "L'atlas ouvert autour du point de départ : forêts, collines, rivières et rivages dessinés au fil de l'exploration" } },
      { id: 'dinos', titre: 'Les dinosaures', statut: 'atester',
        texte: "Les dinosaures d'Archie vivent sur le disque. Apprivoisés, ils donnent les armures qui ouvrent les terres extrêmes.",
        media: { type: 'image', capture: 'dinos.jpg', alt: 'Un lushsaur et un obsidicératops dans une forêt de pins',
          zooms: [{ x: 0.378, y: 0.806, r: 0.095, label: 'Le lushsaur' }, { x: 0.584, y: 0.595, r: 0.065, label: "L'obsidicératops" }] } },
    ],
  },
  {
    id: 'lac', chemin: 'brumaire', touche: 2, nom: 'Le lac', theme: 'Le rendu', x: 0.51, y: 0.25,
    intro: "Brumaire, un shader écrit de zéro pour Compline et allumé par défaut. Il garde les pixels du jeu nets et donne au monde sa lumière.",
    fond: 'bru-foret.jpg',
    fonctions: [
      { id: 'comparer', titre: 'Avec et sans Brumaire', statut: 'atester',
        texte: "Les mêmes points de vue, avec le jeu de base puis avec Brumaire. Faites glisser la séparation.",
        media: { type: 'comparer', avantNom: 'Sans shader', apresNom: 'Avec Brumaire', scenes: [
          { nom: 'Vallée', avant: 'van-vallee.jpg', apres: 'bru-vallee.jpg' },
          { nom: 'Forêt, fin du jour', avant: 'van-foret.jpg', apres: 'bru-foret.jpg' },
          { nom: 'Nuit', avant: 'van-nuit.jpg', apres: 'bru-nuit.jpg' },
          { nom: 'Couchant', avant: 'van-couchant.jpg', apres: 'bru-couchant.jpg' },
        ] } },
      { id: 'pluie', titre: 'Pluie et orage', statut: 'atester',
        texte: "Des gouttes fines poussées par le vent, rien ne tombe sous les toits ; le sol s'assombrit puis sèche lentement.",
        media: { type: 'image', capture: 'orage.jpg', alt: 'Orage sur une vallée boisée, un éclair au loin',
          zooms: [{ x: 0.386, y: 0.137, r: 0.095, label: "L'éclair" }, { x: 0.247, y: 0.751, r: 0.13, label: 'La rivière sous la pluie' }] } },
      { id: 'neige', titre: 'Neige, glace et sable', statut: 'atester',
        texte: "La neige tient sur le haut des sapins, la glace reflète comme l'eau, le sable scintille au soleil.",
        media: { type: 'image', capture: 'sapins-neige.jpg', alt: 'Taïga enneigée sous les flocons',
          zooms: [{ x: 0.5, y: 0.22, r: 0.11, label: 'La neige sur les aiguilles' }] } },
      { id: 'traces', titre: 'Traces de pas', statut: 'atester',
        texte: "Le joueur et les bêtes proches laissent des traces dans la neige et le sable, qui s'effacent en quelques minutes.",
        media: { type: 'image', capture: 'traces-pas.jpg', alt: 'Une file de traces de pas laissée dans le sable' } },
      { id: 'lumiere', titre: 'Lumière colorée', statut: 'atester',
        texte: "Torches orange, lumières des âmes bleues, lichen vert : deux lumières voisines mélangent leurs couleurs.",
        media: { type: 'image', capture: 'bru-nuit.jpg', alt: 'De nuit, un feu de camp et des lanternes des âmes',
          zooms: [{ x: 0.633, y: 0.806, r: 0.1, label: 'Le bleu des âmes sur l’herbe' }, { x: 0.443, y: 0.421, r: 0.08, label: 'Le feu sur le tronc' }] } },
      { id: 'eau', titre: "Sous l'eau", statut: 'atester',
        texte: "Des rayons qui descendent de la surface, des particules en suspension, une eau plus claire près de la surface.",
        media: { type: 'image', capture: 'sous-eau.jpg', alt: "Rayons sous l'eau", provisoire: true } },
      { id: 'vent', titre: 'Le vent', statut: 'atester',
        texte: "Un vent commun à tous les joueurs : la fumée des feux le suit, l'herbe se couche sous les pas.",
        media: { type: 'image', capture: 'fumee.jpg', alt: 'Fumée de feux de camp au petit matin',
          zooms: [{ x: 0.532, y: 0.586, r: 0.095, label: 'La fumée' }] } },
    ],
  },
  {
    id: 'cimes', chemin: 'survie', touche: 3, nom: 'Les cimes', theme: 'La survie', x: 0.72, y: 0.33,
    intro: "La difficulté vient du milieu, pas des hordes : le froid qui monte avec l'altitude, la soif, les saisons et le prix de chaque mort.",
    fond: 'sapins-neige.jpg',
    fonctions: [
      { id: 'temperature', titre: 'Le froid et la chaleur', statut: 'atester',
        texte: "La température dépend du biome, de l'heure et de l'altitude : plus on monte, plus il fait froid.",
        media: { type: 'image', capture: 'froid.jpg', alt: 'Dans la taïga enneigée, la jauge de température au milieu de la barre',
          zooms: [{ x: 0.589, y: 0.864, r: 0.105, ratio: 0.5, label: 'Température, soif et faim' }] } },
      { id: 'mort', titre: 'Chaque mort coûte un cœur', statut: 'atester',
        texte: "On perd un cœur à chaque mort, jusqu'à cinq ; des éclats de vie se trouvent dans le monde.",
        points: ['Pas de monstres ordinaires', 'Au plus huit créatures hostiles par joueur', 'Journées de 30 min, nuits de 15'],
        media: { type: 'image', capture: 'mort-coeur.jpg', alt: 'Au réveil après une mort, il reste neuf cœurs',
          zooms: [{ x: 0.42, y: 0.86, r: 0.19, ratio: 0.35, label: 'Neuf cœurs au réveil' }] } },
      { id: 'xp', titre: "Pas d'expérience", statut: 'atester',
        texte: "Les outils s'usent et se réparent à l'enclume ; le savoir se trouve dans des livres, pas dans des niveaux.",
        media: { type: 'image', capture: 'ecran-avec.jpg', alt: "La barre d'action sans barre d'expérience",
          zooms: [{ x: 0.5, y: 0.907, r: 0.19, ratio: 0.45, label: "Pas de barre d'expérience" }] } },
      { id: 'ecran', titre: "L'interface s'efface", statut: 'atester',
        texte: "Après six secondes sans geste, la barre et les jauges disparaissent ; elles reviennent au moindre danger.",
        media: { type: 'comparer', avantNom: 'En action', apresNom: 'Au repos', scenes: [
          { nom: 'Clairière', avant: 'ecran-avec.jpg', apres: 'ecran-sans.jpg' },
        ] } },
    ],
  },
  {
    id: 'foyer', chemin: 'cuisine', touche: 4, nom: 'Le foyer', theme: 'La cuisine', x: 0.36, y: 0.43,
    intro: "On cuisine avec les mains : couper sur la planche, remuer la marmite, pétrir la pâte. Les stations du jeu s'utilisent dans le monde.",
    fond: 'cuisine.jpg',
    fonctions: [
      { id: 'planche', titre: 'La planche et la marmite', statut: 'teste',
        texte: "Farmer's Delight et Hands-On Delight : chaque plat se prépare geste par geste.",
        media: { type: 'image', capture: 'planche.jpg', alt: 'Cuisinière et marmite, planche à découper, poêle sur le feu, poulet rôti et tarte',
          zooms: [{ x: 0.275, y: 0.394, r: 0.105, label: 'La marmite' }, { x: 0.5, y: 0.421, r: 0.07, label: 'La planche' }, { x: 0.707, y: 0.443, r: 0.08, label: 'La poêle' }] } },
      { id: 'stations', titre: 'Les stations en scène', statut: 'teste',
        texte: "L'établi, les fours, l'enclume s'utilisent en scènes à la première personne (Cinematic Interactions).",
        media: { type: 'image', capture: 'stations-etabli.jpg', alt: "L'établi en scène, en quatre temps : on s'approche, la caméra plonge sur le plateau, puis les deux mains travaillent dessus" } },
    ],
  },
  {
    id: 'riviere', chemin: 'peche', touche: 5, nom: 'La rivière', theme: 'La pêche', x: 0.56, y: 0.63,
    intro: "Avec Starcatcher, chaque poisson se gagne : un mini-jeu d'adresse, des prises selon le lieu, l'heure et l'appât.",
    fond: 'bru-couchant.jpg',
    fonctions: [
      { id: 'ferrer', titre: 'Ferrer le poisson', statut: 'atester',
        texte: "Chaque espèce a son rythme : il faut ferrer au bon moment, plus ou moins vite.",
        media: { type: 'image', capture: 'mini-jeu-riviere.jpg', alt: 'Mini-jeu de pêche', provisoire: true } },
      { id: 'catalogue', titre: 'Le catalogue', statut: 'atester',
        texte: "Chaque prise se range avec sa rareté, son biome et ses records ; trophées et aquarium à la clé.",
        media: { type: 'image', capture: 'peche-catalogue.jpg', alt: 'Le guide de pêche : les poissons du coin à gauche, toutes les espèces à droite, les prises déjà faites dessinées en couleur' } },
      { id: 'fiche', titre: 'Où pêcher quoi', statut: 'atester',
        texte: "Biome, profondeur, heure, météo et appât décident de ce qui mord.",
        media: { type: 'image', capture: 'peche-fiche.jpg', alt: 'La fiche du poisson-chat vaseux : peu commun, marais, en surface, préfère l\'appât des eaux' } },
    ],
  },
  {
    id: 'forge', chemin: 'metal', touche: 6, nom: 'La forge', theme: 'Le métal', x: 0.76, y: 0.75,
    intro: "Pas de lingot d'acier ni de diamant au fond des coffres : chaque palier se forge, jusqu'à l'orichalque du cœur brûlant.",
    fond: 'forge.jpg',
    fonctions: [
      { id: 'paliers', titre: 'Les paliers du métal', statut: 'atester', media: { type: 'echelle' },
        texte: "Fer, acier, acier noir, diamant, orichalque : choisissez un palier." },
      { id: 'patine', titre: 'Armures qui se salissent', statut: 'atester',
        texte: "La boue s'accumule en courant ou en combattant ; l'eau la lave en quelques secondes, la pluie lentement.",
        media: { type: 'image', capture: 'armures.jpg', alt: 'Quatre armures de fer, de la plus propre à la plus sale',
          zooms: [{ x: 0.783, y: 0.467, r: 0.09, label: 'La plus sale' }] } },
      { id: 'combat', titre: 'Le combat animé', statut: 'atester',
        texte: "Better Combat : chaque arme frappe avec tout le corps, en deux ou trois coups enchaînés.",
        media: { type: 'image', capture: 'combat.jpg', alt: "Coup d'épée en quatre images", provisoire: true } },
    ],
  },
  {
    id: 'zoo', chemin: 'zoo', touche: 7, nom: 'Le zoo', theme: 'Les bêtes', x: 0.24, y: 0.56,
    intro: "Chaque bête du monde a sa page dans le guide d'Archie et quatre tampons à gagner. Aucun ne demande de la tuer.",
    fond: 'dinos.jpg',
    fonctions: [
      { id: 'tampons', titre: 'Les quatre tampons', statut: 'atester', media: { type: 'zoo' },
        texte: "Apercevoir, photographier, apprivoiser ou héberger, maîtriser." },
      { id: 'guide', titre: 'Le guide', statut: 'atester',
        texte: "Touche J, marque-page vert : les bêtes en 3D, une silhouette à l'encre tant qu'on ne les a pas vues.",
        media: { type: 'image', capture: 'zoo-guide.jpg', alt: "Le guide ouvert sur le zoo : les quatre tampons à gauche, les quatre familles et leurs compteurs à droite" } },
      { id: 'grandbleu', titre: 'Le Grand bleu', statut: 'atester',
        texte: '96 espèces marines à recenser, sans compter les poissons du catalogue de pêche.',
        media: { type: 'image', capture: 'zoo-grand-bleu.jpg', alt: "Première page du Grand bleu : les bêtes pas encore vues restent des silhouettes, nommées « ??? »" } },
    ],
  },
]

export interface Chantier { statut: Statut; titre: string; texte: string; quand?: string; lieu?: string }

export const CHANTIERS: Chantier[] = [
  { statut: 'teste', titre: 'Atlas dessiné à la main', texte: "La carte se dessine pendant qu'on explore.", quand: 'Validé le 15 nov.', lieu: 'disque' },
  { statut: 'teste', titre: 'Cuisine à la main', texte: 'Planche, marmite, pâte pétrie, stations en scène.', quand: 'Validé le 15 nov.', lieu: 'foyer' },
  { statut: 'atester', titre: 'Le monde-disque', texte: 'Un continent de 12 km et son bord.', quand: 'Depuis le 13 nov.', lieu: 'disque' },
  { statut: 'atester', titre: 'Clairières contre la brume', texte: 'Feu de veille, lanterne et dissipateur.', quand: 'À revoir avec le nouveau rendu' },
  { statut: 'atester', titre: 'Brumaire', texte: 'Ciel, eau, pluie, neige, traces, lumière colorée.', quand: 'Depuis le 16 nov.', lieu: 'lac' },
  { statut: 'atester', titre: 'Le zoo du guide', texte: 'Quatre tampons par bête, 266 espèces.', quand: 'Depuis le 16 nov.', lieu: 'zoo' },
  { statut: 'atester', titre: "Quatre mods d'animaux", texte: "Koopa's Critters, Spawn, Hybrid Birds, WilderNature.", quand: 'Depuis le 16 nov.', lieu: 'zoo' },
  { statut: 'atester', titre: 'La pêche', texte: 'Starcatcher, catalogue, trophées.', quand: 'Depuis le 17 nov.', lieu: 'riviere' },
  { statut: 'atester', titre: "L'acier noir", texte: "Le palier entre l'acier et le diamant.", quand: 'Depuis le 17 nov.', lieu: 'forge' },
  { statut: 'atester', titre: 'Armures de dinosaures', texte: 'Chaque armure ouvre une partie du monde.', quand: 'Depuis le 17 nov.', lieu: 'forge' },
  { statut: 'atester', titre: 'Cœur brûlant et orichalque', texte: 'Le dernier palier, près de la lave profonde.', quand: 'Depuis le 17 nov.', lieu: 'forge' },
  { statut: 'atester', titre: 'Combat animé', texte: 'Better Combat, coups enchaînés.', quand: 'Depuis le 17 nov.', lieu: 'forge' },
  { statut: 'atester', titre: 'Armures qui se salissent', texte: "La boue s'accumule, l'eau la lave.", quand: 'Depuis le 15 nov.', lieu: 'forge' },
  { statut: 'atester', titre: "Interface qui s'efface", texte: 'Barre et jauges cachées au repos.', quand: 'Depuis le 17 nov.', lieu: 'cimes' },
  { statut: 'encours', titre: 'Filons et affleurements', texte: 'Des filons signalés par des pierres en surface.' },
  { statut: 'encours', titre: 'Brumaire, finitions', texte: 'Effet par effet, avec une comparaison à chaque fois.', lieu: 'lac' },
  { statut: 'prevu', titre: 'Sessions de test entre amis', texte: "Huit séances d'environ une heure.", quand: 'Prochaine étape' },
  { statut: 'prevu', titre: 'Saisons ressenties', texte: 'La saison change la température ressentie.' },
  { statut: 'prevu', titre: 'Le hameau de survivants', texte: 'Des survivants retrouvés dans la brume.' },
  { statut: 'prevu', titre: 'Quêtes et chapitres', texte: 'Premier feu, forge, zoo, monuments, dirigeable.' },
  { statut: 'prevu', titre: 'Le logo', texte: 'À dessiner de zéro.' },
  { statut: 'prevu', titre: 'Le monde permanent', texte: 'Un serveur et un monde prégénéré, version 0.1.0.' },
]

export interface Etape { iso: string; date: string; titre: string; texte: string; image: string; lieu?: string }

// Newest last.
export const JOURNAL: Etape[] = [
  { iso: '2026-09-23', image: 'van-vallee.jpg', date: '23 sept.', titre: 'Premier jour', texte: 'Le dépôt, les outils et un pack de base de douze mods.' },
  { iso: '2026-10-19', image: 'van-foret.jpg', date: '19 oct.', titre: 'Les choix de la bande', texte: '57 choix appliqués, et tout le pack traduit en français.' },
  { iso: '2026-10-25', image: 'van-couchant.jpg', date: '25 oct.', titre: 'Le relief', texte: 'Montagnes de Tectonic, rivières, horizon de Distant Horizons.' },
  { iso: '2026-11-11', image: 'van-nuit.jpg', date: '11 nov.', titre: 'Le recentrage', texte: "On quitte No Man's Land ; naît l'idée d'un monde en disque." },
  { iso: '2026-11-13', image: 'bord.jpg', date: '13 nov.', titre: 'Le disque', texte: 'Un continent de 12 km et son bord gardé par la brume.', lieu: 'disque' },
  { iso: '2026-11-14', image: 'bru-foret.jpg', date: '14 nov.', titre: 'Compline', texte: 'Le pack prend son nom ; premier jet de Brumaire.', lieu: 'lac' },
  { iso: '2026-11-15', image: 'planche.jpg', date: '15 nov.', titre: 'Premiers essais', texte: "L'atlas et la cuisine validés ; le zoo dans le guide d'Archie.", lieu: 'foyer' },
  { iso: '2026-11-16', image: 'traces-pas.jpg', date: '16 nov.', titre: 'Plus de détails', texte: 'Quatre mods d’animaux ; pluie, traces, givre, fonds marins.', lieu: 'zoo' },
  { iso: '2026-11-17', image: 'bru-couchant.jpg', date: '17 nov.', titre: 'Pêche et métal', texte: "Starcatcher, l'acier noir, l'orichalque, le combat animé.", lieu: 'riviere' },
]
