import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import '@fontsource/alegreya/latin-500.css'
import '@fontsource/alegreya/latin-700.css'
import '@fontsource/alegreya/latin-500-italic.css'
import '@fontsource/alegreya-sans/latin-400.css'
import '@fontsource/alegreya-sans/latin-500.css'
import '@fontsource/alegreya-sans/latin-700.css'
import '@fontsource/alegreya-sc/latin-700.css'
import '@fontsource/silkscreen/latin-400.css'
import './index.css'
import App from './App'

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <App />
  </StrictMode>,
)
