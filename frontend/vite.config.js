import { defineConfig, loadEnv } from 'vite'
import vue from '@vitejs/plugin-vue'
import { resolve } from 'path'

export default defineConfig(({ mode }) => {
  // 加载环境变量
  const env = loadEnv(mode, process.cwd(), '')
  
  return {
    plugins: [vue()],
    resolve: {
      alias: {
        '@': resolve(__dirname, 'src')
      }
    },
    server: {
      host: '0.0.0.0',
      port: 5173, // 开发服务器端口
      // 代理配置，解决开发环境跨域问题
      proxy: {
        '/api': {
          target: env.VITE_API_BASE_URL || 'http://localhost:8085',
          changeOrigin: true,
          secure: false,
          rewrite: (path) => path,
          configure: (proxy, options) => {
            // 代理连接事件
            proxy.on('proxyReq', (proxyReq, req, res) => {
              console.log(`🔄 [DEV Proxy] ${req.method} ${req.url} -> ${options.target}${proxyReq.path}`)
            })
          }
        }
      },
      // 开发服务器配置
      open: false, // 是否自动打开浏览器
      cors: true, // 允许跨域
      hmr: {
        overlay: true // 热更新错误覆盖
      }
    },
    // 构建配置
    build: {
      target: 'es2015',
      outDir: 'dist',
      assetsDir: 'assets',
      sourcemap: mode !== 'production',
      minify: 'terser',
      terserOptions: {
        compress: {
          drop_console: mode === 'production',
          drop_debugger: mode === 'production'
        }
      },
      rollupOptions: {
        output: {
          chunkFileNames: 'js/[name]-[hash].js',
          entryFileNames: 'js/[name]-[hash].js',
          assetFileNames: '[ext]/[name]-[hash].[ext]'
        }
      }
    },
    // 预览配置 - 使用不同的端口避免冲突
    preview: {
      host: '0.0.0.0',
      port: 5174, // 预览服务器使用5174端口，避免与开发服务器冲突
      proxy: {
        '/api': {
          target: env.VITE_API_BASE_URL || 'http://localhost:8085',
          changeOrigin: true,
          secure: false
        }
      }
    },
    // 环境变量配置
    define: {
      __APP_ENV__: JSON.stringify(env.NODE_ENV || mode),
      global: 'window'
    }
  }
})