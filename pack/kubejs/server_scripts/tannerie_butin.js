// Compline : la tannerie, le butin. Les animaux à cuir lâchent une peau brute, qui se
// racle, se tanne et sèche avant de donner du cuir. Les recettes sont dans tannerie.js.
// Voir docs/plan/tannerie.md.
//
// On modifie les tables elles-mêmes (LootJS.lootTables) et non le butin à la volée :
// les chevaux d'Icy's Better Horses renvoient vers minecraft:entities/horse, et une
// table modifiée l'est pour tous ceux qui l'appellent.

const TANNERIE_PEAU = 'compline:peau_brute'

// Tables où le cuir (ou la peau de Naturalist) devient une peau brute.
// Les quantités et le bonus de Butin d'origine sont gardés.
const TANNERIE_TABLES_A_PEAU = [
  // Vanilla
  'minecraft:entities/cow',
  'minecraft:entities/mooshroom',
  'minecraft:entities/horse', // aussi les 15 races d'Icy's Better Horses
  'minecraft:entities/donkey',
  'minecraft:entities/mule',
  'minecraft:entities/llama',
  'minecraft:entities/trader_llama',
  'minecraft:entities/hoglin', // Nether fermé : sans effet en jeu
  // Naturalist (cuir)
  'naturalist:entities/deer',
  'naturalist:entities/zebra',
  // Naturalist (peau « hide », qui cuisait en cuir au four)
  'naturalist:entities/boar',
  'naturalist:entities/elephant',
  'naturalist:entities/rhino',
  'naturalist:entities/komodo_dragon',
  // Hybrid Aquatic
  'hybrid_aquatic:entities/dugong',
  'hybrid_aquatic:entities/manatee',
  // Friends & Foes
  'friendsandfoes:entities/moobloom',
  // Wilder Nature
  'wildernature:entities/bison',
  'wildernature:entities/deer',
  // Spawn (la vache de mer, et le champignon qui en lâchait aussi)
  'spawn:entities/sea_cow',
  'spawn:entities/flukeshroom',
  // Quark (Nether fermé : sans effet en jeu)
  'quark:entities/foxhound' // renard de feu
]

// Animaux qui ne lâchaient pas de cuir : une peau ajoutée, 0 ou 1, plus 0 à 1 par
// niveau de Butin.
const TANNERIE_TABLES_PEAU_AJOUTEE = [
  'minecraft:entities/pig',
  'minecraft:entities/sheep',
  'minecraft:entities/goat'
]

function tannerieTable(event, id) {
  if (event.hasLootTable(id)) return event.getLootTable(id)
  console.warn(`[tannerie] table absente, ignorée : ${id}`)
  return null
}

LootJS.lootTables(event => {
  TANNERIE_TABLES_A_PEAU.forEach(id => {
    let table = tannerieTable(event, id)
    if (!table) return
    table.replaceItem('minecraft:leather', TANNERIE_PEAU)
    if (Platform.isLoaded('naturalist')) table.replaceItem('naturalist:hide', TANNERIE_PEAU)
  })

  TANNERIE_TABLES_PEAU_AJOUTEE.forEach(id => {
    let table = tannerieTable(event, id)
    if (!table) return
    table.createPool(pool => {
      pool.addEntry(
        LootEntry.of(TANNERIE_PEAU)
          .setCount([0, 1])
          .applyEnchantmentBonus([0, 1])
      )
    })
  })

  event.forEachTable(table => {
    // Contrôle : une table de créature qui lâche encore du cuir est signalée dans le
    // journal du serveur (nouveau mod de faune, table oubliée).
    if (String(table.getLootType()) === 'ENTITY' && table.hasItem('minecraft:leather')) {
      console.info(`[tannerie] créature qui lâche encore du cuir : ${table.getLocation()}`)
    }
  })
})
