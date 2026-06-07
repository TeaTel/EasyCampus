import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// https://vitejs.dev/config/
export default defineConfig(({ mode }) => ({
  plugins: [vue()],
  server: {
    port: 3000,
    // 只在开发环境使用代理
    proxy: mode === 'development' ? {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      },
      '/uploads': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    } : undefined
  },
  // 生产环境构建配置
  build: {
    outDir: 'dist',
    sourcemap: mode === 'development',
    rollupOptions: {
      output: {
        manualChunks: {
          vendor: ['vue', 'vue-router', 'axios']
        }
      }
    }
  },
  // 基础路径配置，用于子路径部署
  base: process.env.NODE_ENV === 'production' ? '/' : '/'
}))