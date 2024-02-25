import { toast } from 'vue3-toastify';

export const __NAME__ = 'HiCT Platform';
export const __AUTHOR__ = '@AlexSin';
export const __VERSION__: string = process.env?.VITE_PROJECT_VERSION ?? '0.1';

export function notify(type: 'info' | 'success' | 'warning' | 'error', message: string) {
    toast(message, {
        type: type,
        autoClose: 4000,
        position: 'top-right',
        theme: 'auto',
        transition: 'slide',
    });
}
