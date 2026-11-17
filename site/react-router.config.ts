import type { Config } from '@react-router/dev/config'
import { CHEMINS } from './app/donnees/chemins'

// No runtime server: every page is rendered to HTML at build time and served as static files.
export default {
  appDirectory: 'app',
  ssr: false,
  prerender: ['/', ...CHEMINS],
} satisfies Config
