import { type RouteConfig, index, route } from '@react-router/dev/routes'

export default [
  index('routes/accueil.tsx'),
  route('journal', 'routes/journal.tsx'),
  route('feuille-de-route', 'routes/feuille-de-route.tsx'),
  route(':salle/:plan?', 'routes/salle.tsx'),
  route('*', 'routes/introuvable.tsx'),
] satisfies RouteConfig
