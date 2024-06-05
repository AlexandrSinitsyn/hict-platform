import { fileURLToPath, URL } from 'node:url';

import { defineConfig, loadEnv } from 'vite';
import vue from '@vitejs/plugin-vue';

// https://vitejs.dev/config/
export default ({ mode }: { mode: string }) => {
    const env = loadEnv(mode, process.cwd(), '');

    return defineConfig({
        base: mode === 'dev' ? '/' : '/hict',
        define: {
            'process.env': env,
        },
        plugins: [vue()],
        resolve: {
            alias: {
                '@': fileURLToPath(new URL('./src', import.meta.url)),
                '@types': fileURLToPath(new URL('./src/core/entity/types.ts', import.meta.url)),
                '@hict': fileURLToPath(new URL('./HiCT_WebUI/src', import.meta.url)),
            },
        },
        server: {
            port: 80,
        },
        build: {
            sourcemap: false,
        },
        esbuild: {
            supported: {
                'top-level-await': true,
            },
        },
    });
};
