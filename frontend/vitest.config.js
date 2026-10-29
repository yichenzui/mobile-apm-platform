import { defineConfig } from 'vitest/config'
import vue from '@vitejs/plugin-vue'
import { fileURLToPath, URL } from 'node:url'

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    }
  },
  test: {
    globals: true,
    // 现有测试只测 Pinia store，不需要 DOM；项目未安装 jsdom，用 node 环境即可运行
    environment: 'node',
    include: ['src/**/*.{test,spec}.{js,ts}']
  }
})
