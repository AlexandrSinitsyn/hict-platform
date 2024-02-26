import type { Jwt, LoginForm } from '@/core/types';
import { authLogin } from '@/core/server-requests';
import { notify } from '@/core/config';

const JWT_STORAGE_KEY = 'JWT_STORAGE_KEY';

export function getJwt(): Jwt {
    localStorage.getItem(JWT_STORAGE_KEY);
}

function saveJwt(jwt: Jwt) {
    localStorage.setItem(JWT_STORAGE_KEY, jwt);
}

function forgetJwt() {
    localStorage.removeItem(JWT_STORAGE_KEY);
}

export function login(form: LoginForm) {
    authLogin(form, (jwt: Jwt) => {
        saveJwt(jwt);
        notify('info', 'Successful log in');
    });
}
