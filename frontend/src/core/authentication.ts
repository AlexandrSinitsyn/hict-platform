import type { Jwt, LoginForm, RegisterForm } from '@/core/types';
import { authLogin, authRegister } from '@/core/server-requests';
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

export function register(form: RegisterForm) {
    authRegister(form, (jwt: Jwt) => {
        saveJwt(jwt);
        notify('info', 'Successful register');
    });
}

export function logout() {
    forgetJwt();
}
