import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { resolve } from 'path'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [vue()],
  define: {
    // sockjs-client 依赖 Node.js global 变量，在浏览器中映射为 globalThis
    global: 'globalThis'
  },
  resolve: {
    alias: {
      '@': resolve(__dirname, 'src')
    }
  },
  server: {
    host: '0.0.0.0',
    port: 3000,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      },
      // WebSocket STOMP 端点代理（SockJS）
      '/ws-stomp': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        ws: true
      }
    }
  }
})
