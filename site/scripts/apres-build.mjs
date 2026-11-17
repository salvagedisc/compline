// After the build: the SPA fallback page doubles as the 404 page served by the host.
import { copyFileSync } from 'node:fs'

copyFileSync('build/client/__spa-fallback.html', 'build/client/404.html')
