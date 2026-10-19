// Fallow : la tannerie, le butin. Les animaux à cuir lâchent une peau brute.
// Les objets sont dans startup_scripts/tannerie.js, les recettes dans tannerie.js.
// Voir docs/plan/tannerie.md.
//
// On modifie les tables elles-mêmes (LootJS.lootTables) et non le butin à la volée :
// les chevaux d'Icy's Better Horses renvoient vers minecraft:entities/horse, et une
// table modifiée l'est pour tous ceux qui l'appellent.

// Tables où le cuir (ou la peau de Naturalist) devient une peau brute.
// Les quantités et le bonus de butin d'origine sont gardés.
const TANNERIE_TABLES_A_PEAU = [
  // Vanilla
  'minecraft:entities/cow',
  'minecraft:entities/mooshroom',
  'minecraft:entities/horse',        // aussi les 15 races d'Icy's Better Horses
  'minecraft:entities/donkey',
  'minecraft:entities/mule',
  'minecraft:entities/llama',
  'minecraft:entities/trader_llama',
  'minecraft:entities/hoglin',       // Nether fermé : sans effet en jeu
  // No Man's Land
  'nomansland:entities/moose',
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
  // Quark (Nether fermé : sans effet en jeu)
  'quark:entities/foxhound'          // renard de feu
]

// Les cerfs de NML ne lâchaient pas de cuir : on leur ajoute une peau brute,
// 0 ou 1, plus 0 à 1 par niveau de Butin.
const TANNERIE_TABLES_PEAU_AJOUTEE = [
  'nomansland:entities/deer'
]

LootJS.lootTables(event => {
  TANNERIE_TABLES_A_PEAU.forEach(id => {
    if (!event.hasLootTable(id)) {
      console.warn(`[tannerie] table absente, ignorée : ${id}`)
      return
    }
    event.getLootTable(id)
      .replaceItem('minecraft:leather', 'fallow:peau_brute')
      .replaceItem('naturalist:hide', 'fallow:peau_brute')
  })

  TANNERIE_TABLES_PEAU_AJOUTEE.forEach(id => {
    if (!event.hasLootTable(id)) {
      console.warn(`[tannerie] table absente, ignorée : ${id}`)
      return
    }
    event.getLootTable(id).createPool(pool => {
      pool.addEntry(
        LootEntry.of('fallow:peau_brute')
          .setCount([0, 1])
          .applyEnchantmentBonus([0, 1])
      )
    })
  })

  // Contrôle : une table de créature qui lâche encore du cuir est signalée dans le
  // journal du serveur (nouveau mod de faune, table oubliée).
  event.forEachTable(table => {
    if (String(table.getLootType()) === 'ENTITY' && table.hasItem('minecraft:leather')) {
      console.info(`[tannerie] créature qui lâche encore du cuir : ${table.getLocation()}`)
    }
  })
})
