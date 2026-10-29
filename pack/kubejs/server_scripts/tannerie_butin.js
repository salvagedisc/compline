// Fallow : la tannerie, le butin. Les animaux à cuir lâchent une peau de Tannery.
// Les recettes sont dans tannerie.js. Voir docs/plan/tannerie.md.
//
// Tannery remplace les tables vanilla de la vache, du cochon, du mouton, de la chèvre,
// du cheval et de l'âne par les siennes, qui perdent le bonus de Butin et la viande cuite
// par le feu. pack/kubejs/data/minecraft/loot_table/entities/ remet ces six tables
// vanilla (KubeJS passe après les mods), et ce script y met les peaux et le gras.
//
// On modifie les tables elles-mêmes (LootJS.lootTables) et non le butin à la volée :
// les chevaux d'Icy's Better Horses renvoient vers minecraft:entities/horse, et une
// table modifiée l'est pour tous ceux qui l'appellent.
//
// Le séchoir de Tannery ne prend que ses cinq peaux : chaque animal du pack reçoit
// celle qui lui ressemble le plus. Toutes sèchent en la même peau séchée.

// Tables où le cuir (ou la peau de Naturalist) devient une peau de Tannery.
// Les quantités et le bonus de Butin d'origine sont gardés.
const TANNERIE_TABLES_A_PEAU = {
  // Vanilla
  'minecraft:entities/cow': 'tannery:cow_hide',
  'minecraft:entities/mooshroom': 'tannery:cow_hide',
  'minecraft:entities/horse': 'tannery:horse_hide',        // aussi les 15 races d'Icy's Better Horses
  'minecraft:entities/donkey': 'tannery:horse_hide',
  'minecraft:entities/mule': 'tannery:horse_hide',
  'minecraft:entities/llama': 'tannery:horse_hide',
  'minecraft:entities/trader_llama': 'tannery:horse_hide',
  'minecraft:entities/hoglin': 'tannery:pig_hide',         // Nether fermé : sans effet en jeu
  // No Man's Land
  'nomansland:entities/moose': 'tannery:cow_hide',
  // Naturalist (cuir)
  'naturalist:entities/deer': 'tannery:goat_hide',
  'naturalist:entities/zebra': 'tannery:horse_hide',
  // Naturalist (peau « hide », qui cuisait en cuir au four)
  'naturalist:entities/boar': 'tannery:pig_hide',
  'naturalist:entities/elephant': 'tannery:cow_hide',
  'naturalist:entities/rhino': 'tannery:cow_hide',
  'naturalist:entities/komodo_dragon': 'tannery:goat_hide',
  // Hybrid Aquatic
  'hybrid_aquatic:entities/dugong': 'tannery:cow_hide',
  'hybrid_aquatic:entities/manatee': 'tannery:cow_hide',
  // Friends & Foes
  'friendsandfoes:entities/moobloom': 'tannery:cow_hide',
  // Quark (Nether fermé : sans effet en jeu)
  'quark:entities/foxhound': 'tannery:goat_hide'           // renard de feu
}

// Animaux qui ne lâchaient pas de cuir : une peau ajoutée, 0 ou 1, plus 0 à 1 par
// niveau de Butin (les quantités de Tannery, avec le Butin en plus).
const TANNERIE_TABLES_PEAU_AJOUTEE = {
  'minecraft:entities/pig': 'tannery:pig_hide',
  'minecraft:entities/sheep': 'tannery:sheep_hide',
  'minecraft:entities/goat': 'tannery:goat_hide',
  'nomansland:entities/deer': 'tannery:goat_hide'
}

// Gras animal : un morceau par bête, comme dans les tables de Tannery.
// Il fait deux torches avec un bâton, ou entre dans le cuir traité.
const TANNERIE_TABLES_GRAS = [
  'minecraft:entities/cow',
  'minecraft:entities/mooshroom',
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
  Object.keys(TANNERIE_TABLES_A_PEAU).forEach(id => {
    let table = tannerieTable(event, id)
    if (!table) return
    let peau = TANNERIE_TABLES_A_PEAU[id]
    table.replaceItem('minecraft:leather', peau)
    if (Platform.isLoaded('naturalist')) table.replaceItem('naturalist:hide', peau)
  })

  Object.keys(TANNERIE_TABLES_PEAU_AJOUTEE).forEach(id => {
    let table = tannerieTable(event, id)
    if (!table) return
    table.createPool(pool => {
      pool.addEntry(
        LootEntry.of(TANNERIE_TABLES_PEAU_AJOUTEE[id])
          .setCount([0, 1])
          .applyEnchantmentBonus([0, 1])
      )
    })
  })

  TANNERIE_TABLES_GRAS.forEach(id => {
    let table = tannerieTable(event, id)
    if (!table) return
    table.createPool(pool => pool.addEntry(LootEntry.of('tannery:animal_fat_chunk')))
  })

  event.forEachTable(table => {
    // Les sacoches retirées (tannerie.js) ne sortent d'aucun coffre.
    TANNERIE_OBJETS.forEach(id => table.removeItem(id))
    // Contrôle : une table de créature qui lâche encore du cuir est signalée dans le
    // journal du serveur (nouveau mod de faune, table oubliée).
    if (String(table.getLootType()) === 'ENTITY' && table.hasItem('minecraft:leather')) {
      console.info(`[tannerie] créature qui lâche encore du cuir : ${table.getLocation()}`)
    }
  })
})
