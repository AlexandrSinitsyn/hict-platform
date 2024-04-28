import { toast } from 'vue3-toastify';

export const __NAME__ = 'HiCT Platform';
export const __AUTHOR__ = '@AlexSin';
export const __VERSION__: string = process.env?.VITE_PROJECT_VERSION ?? '0.1';

export const __AUTH_HOST__: string = process.env?.VITE_AUTH_HOST ?? 'unknown';
export const __SERVER_HOST__: string = process.env?.VITE_SERVER_HOST ?? 'unknown';
export const __HiCT_CLUSTER__: string = process.env?.VITE_HICT_CLUSTER_HOST ?? 'unknown';

export const AUTH = `http://${__AUTH_HOST__}/api/v1`;
export const SERVER = `http://${__SERVER_HOST__}/api/v1`;
export const HiCT = `http://${__HiCT_CLUSTER__}`;

export function notify(type: 'info' | 'success' | 'warning' | 'error', message: string) {
    toast(message, {
        type: type,
        autoClose: 4000,
        position: 'top-right',
        theme: 'auto',
        transition: 'slide',
    });
}
