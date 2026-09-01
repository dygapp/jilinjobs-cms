import { defineConfig, type Plugin } from 'vite'
import vue from '@vitejs/plugin-vue'

function partyEntryFallback(): Plugin {
  return {
    name: 'party-entry-fallback',
    configureServer(server) {
      server.middlewares.use((request, _response, next) => {
        if (request.url === '/party' || request.url?.startsWith('/party/')) {
          request.url = '/party.html'
        }
        next()
      })
    },
  }
}

export default defineConfig({
  plugins: [partyEntryFallback(), vue()],
  build: {
    rollupOptions: {
      input: { main: 'index.html', party: 'party.html' },
    },
  },
  server: {
    port: 5173,
    proxy: {
      '/api': { target: 'http://127.0.0.1:8080', changeOrigin: true },
      '/static': { target: 'http://127.0.0.1:8080', changeOrigin: true },
    },
  },
})
