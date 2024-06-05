import { toast } from 'vue3-toastify';
import { type Ref, ref } from 'vue';

export const __NAME__: Ref<string> = ref('HiCT Platform');
export const __AUTHOR__: Ref<string> = ref('@AlexSin');
export const __VERSION__: Ref<string> = ref(import.meta.env['VITE_PROJECT_VERSION']);

export const __AUTH_HOST__: Ref<string> = ref(import.meta.env.BASE_URL + '/auth/api/v1');
export const __SERVER_HOST__: Ref<string> = ref(import.meta.env.BASE_URL + '/server/api/v1');
export const __HiCT_CLUSTER__: Ref<string> = ref(import.meta.env.BASE_URL + '/hict-cluster');

export function notify(type: 'info' | 'success' | 'warning' | 'error', message: string) {
    toast(message, {
        type: type,
        autoClose: 4000,
        position: 'top-right',
        theme: 'auto',
        transition: 'slide',
    });
}
