// BROUILLON Fallow — feux de clairière (plan/10 §10.3). Trois paliers, rayon croissant.
// Le rayon est lu par server_scripts/clairieres.js dans global.FALLOW_CLAIRIERES.

global.FALLOW_CLAIRIERES = {
  'fallow:feu_de_veille': 16,
  'fallow:lanterne_de_clairiere': 32,
  'fallow:dissipateur': 64
}

StartupEvents.registry('block', event => {
  event.create('fallow:feu_de_veille')
    .displayName('Feu de veille')
    .material('stone').hardness(2.0).resistance(6.0)
    .lightLevel(0.8).requiresTool(true)
    .box(2, 0, 2, 14, 10, 14, true)

  event.create('fallow:lanterne_de_clairiere')
    .displayName('Lanterne de clairière')
    .material('metal').hardness(3.0).resistance(8.0)
    .lightLevel(1.0).requiresTool(true)

  event.create('fallow:dissipateur')
    .displayName('Dissipateur')
    .material('metal').hardness(4.0).resistance(10.0)
    .lightLevel(1.0).requiresTool(true)
})
