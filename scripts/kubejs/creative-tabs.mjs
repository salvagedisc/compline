// Builds pack/kubejs/startup_scripts/onglets_creatifs.js: every item the KubeJS server scripts
// remove from the pack (the `*_OBJETS` lists, the keys of FUSIONS, the Steam 'n' Rails
// patterns) is also taken out of the creative tabs. EMI hiding is done by the client
// scripts; KubeJS startup scripts cannot read either, so the list is written out here.
// Run `npm run gen-creative` after changing a list; a test checks the file is up to date.
import { readFile, writeFile } from 'node:fs/promises';
import { dirname, join } from 'node:path';
import { fileURLToPath } from 'node:url';

const root = join(dirname(fileURLToPath(import.meta.url)), '..', '..');
const kubejs = join(root, 'pack', 'kubejs');
export const outputPath = join(kubejs, 'startup_scripts', 'onglets_creatifs.js');

// Creative tabs of the pack, read from the test server's registry on 2026-11-15 (the hotbar,
// inventory and search tabs are not lists of their own).
const TABS = [
  'adorablehamsterpets:adorable_hamster_pets', 'alexsmobs:alexsmobs', 'cold_sweat:cold_sweat',
  'cold_sweat:cs_insulation_items', 'companions:companions_tab', 'create:base', 'create:palettes',
  'create_dragons_plus:base', 'crittersandcompanions:main', 'ec_ad:items',
  'eclipticseasons:eclipticseasons', 'everycomp:everycomp', 'exposure:exposure',
  'farm_and_charm:farm_and_charm', 'farmersdelight:farmersdelight', 'friendsandfoes:main_tab',
  'ghosts:ghosts_creative_tab', 'handcrafted:main', 'hybrid_aquatic:blocks', 'hybrid_aquatic:items',
  'hybrid_aquatic:spawn_eggs', 'icys-better-horses:stable_supplies',
  'immersiveenchanting:enchanting', 'kubejs:tab', 'longwings:data/longwings', 'lootr:lootr',
  'minecraft:building_blocks', 'minecraft:colored_blocks', 'minecraft:combat',
  'minecraft:food_and_drinks', 'minecraft:functional_blocks', 'minecraft:ingredients',
  'minecraft:natural_blocks', 'minecraft:op_blocks', 'minecraft:redstone_blocks',
  'minecraft:spawn_eggs', 'minecraft:tools_and_utilities', 'modonomicon:modonomicon',
  'mowziesmobs:mowziesmobs_tab', 'naturalist:item_group', 'overgeared:blueprint_tab',
  'overgeared:lingering_arrows_tab', 'overgeared:overgeared_tab', 'railways:main',
  'railways:palettes', 'railways:tracks', 'regions_unexplored:main', 'simulated:main_tab',
  'surfacesamples:surface_samples_blocks_tab', 'surfacesamples:surface_samples_items_tab',
  'thirst:thirst', 'twigs:twig'
];

// Server scripts whose `*_OBJETS` lists remove items from the pack.
const LISTS = ['sans_lapin', 'doublons', 'faune_coupee', 'premiers_gestes', 'tri'];

const quotedIds = (text) => [...text.matchAll(/'([a-z0-9_.-]+:[a-z0-9_/.-]+)'/g)].map((match) => match[1]);
const server = (name) => readFile(join(kubejs, 'server_scripts', `${name}.js`), 'utf8');

export async function removedItems() {
  const ids = [];
  for (const name of LISTS) {
    for (const list of (await server(name)).matchAll(/const \w*_OBJETS\w* = \[([\s\S]*?)\]/g)) {
      ids.push(...quotedIds(list[1]));
    }
  }
  const fusions = (await server('fusions')).match(/const FUSIONS = \{([\s\S]*?)\n\}/);
  ids.push(...[...fusions[1].matchAll(/'([a-z0-9_.-]+:[a-z0-9_/.-]+)':/g)].map((match) => match[1]));
  // Modonomicon's book (Icy's Better Horses' stable manual), hidden from EMI as well.
  ids.push('modonomicon:modonomicon');
  return [...new Set(ids)].sort();
}

async function patterns() {
  const list = (await server('tri')).match(/const TRI_MOTIFS = \[([\s\S]*?)\n\]/);
  return list[1].split('\n').map((line) => line.trim().replace(/,$/, '')).filter(Boolean);
}

const wrap = (items, indent) => {
  const lines = [];
  let line = indent;
  for (const item of items) {
    const piece = `'${item}', `;
    if (line.length + piece.length > 100 && line.trim()) {
      lines.push(line.trimEnd());
      line = indent;
    }
    line += piece;
  }
  lines.push(line.replace(/, $/, ''));
  return lines.join('\n');
};

export async function buildScript() {
  return `// Fallow : les objets retirés du pack sortent aussi des onglets du mode créatif. Fichier
// généré par scripts/kubejs/creative-tabs.mjs (npm run gen-creative) à partir des listes des
// scripts serveur (*_OBJETS, FUSIONS, motifs de Steam 'n' Rails) : ne pas l'éditer à la main.

const ONGLETS_CREATIFS = [
${wrap(TABS, '  ')}
]

const ONGLETS_RETIRES = [
${wrap(await removedItems(), '  ')}
]

const ONGLETS_MOTIFS = [
${(await patterns()).map((pattern) => `  ${pattern}`).join(',\n')}
]

ONGLETS_CREATIFS.forEach(onglet => {
  StartupEvents.modifyCreativeTab(onglet, event => {
    ONGLETS_RETIRES.forEach(id => {
      if (Item.exists(id)) event.remove(id)
    })
    ONGLETS_MOTIFS.forEach(motif => event.remove(motif))
  })
})
`;
}

if (process.argv[1] === fileURLToPath(import.meta.url)) {
  await writeFile(outputPath, await buildScript());
  console.log(`written ${outputPath}`);
}
