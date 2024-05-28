import { toast } from 'vue3-toastify';
import { type Ref, ref, watch } from 'vue';

export const env: Ref<Record<string, string>> = ref({});

export const __NAME__: Ref<string> = ref('HiCT Platform');
export const __AUTHOR__: Ref<string> = ref('@AlexSin');
export const __VERSION__: Ref<string> = ref('unknown');

export const __AUTH_HOST__: Ref<string> = ref('unknown');
export const __SERVER_HOST__: Ref<string> = ref('unknown');
export const __HiCT_CLUSTER__: Ref<string> = ref('unknown');

watch(env, () => {
    __VERSION__.value = getEnv('VITE_PROJECT_VERSION', 'projectVersion');
    __AUTH_HOST__.value = getEnv('VITE_AUTH_HOST', 'authHost', '/api/v1');
    __SERVER_HOST__.value = getEnv('VITE_SERVER_HOST', 'serverHost', '/api/v1');
    __HiCT_CLUSTER__.value = getEnv('VITE_HICT_CLUSTER_HOST', 'hictClusterHost');
});

function getEnv(envName: string, jsonEnvName: string, postfix = ''): string {
    let res = 'unknown';

    if (import.meta.env && import.meta.env[envName]) {
        res = import.meta.env[envName] + postfix;
    } else {
        if (env.value && env.value[jsonEnvName]) {
            res = env.value[jsonEnvName] + postfix;
        }
    }

    return res;
}

export function notify(type: 'info' | 'success' | 'warning' | 'error', message: string) {
    toast(message, {
        type: type,
        autoClose: 4000,
        position: 'top-right',
        theme: 'auto',
        transition: 'slide',
    });
}
