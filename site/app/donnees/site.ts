// Public address of the site, used for absolute links in link previews. Empty until the domain is chosen.
export const SITE = ''
export const MAJ = '17 novembre 2026' // date of the latest update shown on the site

export function metas(titre: string, description: string, image?: string) {
  const t = titre === 'Compline' ? titre : `${titre} · Compline`
  return [
    { title: t },
    { name: 'description', content: description },
    { property: 'og:title', content: t },
    { property: 'og:description', content: description },
    { property: 'og:type', content: 'website' },
    { property: 'og:locale', content: 'fr_FR' },
    ...(image ? [{ property: 'og:image', content: `${SITE}${image}` }, { name: 'twitter:card', content: 'summary_large_image' }] : []),
  ]
}
