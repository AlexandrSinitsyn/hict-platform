import type { Jwt, LoginForm, RegisterForm, User } from '@/core/types';
import { authLogin, authRegister, requestUser } from '@/core/server-requests';
import { notify } from '@/core/config';

const JWT_STORAGE_KEY = 'JWT_STORAGE_KEY';

export function getJwt(): Jwt | undefined {
    return localStorage.getItem(JWT_STORAGE_KEY) ?? undefined;
}

function saveJwt(jwt: Jwt) {
    localStorage.setItem(JWT_STORAGE_KEY, jwt);
}

function forgetJwt() {
    localStorage.removeItem(JWT_STORAGE_KEY);
}

export function login(form: LoginForm, then: () => void) {
    authLogin(form, (jwt: Jwt) => {
        saveJwt(jwt);
        notify('info', 'Successful log in');
        then();
    });
}

export function register(form: RegisterForm, then: () => void) {
    authRegister(form, (jwt: Jwt) => {
        saveJwt(jwt);
        notify('info', 'Successful register');
        then();
    });
}

export function logout() {
    forgetJwt();
}

export function getAuthorizedUser(doGet: (user: User | undefined) => void) {
    const jwt = getJwt();

    if (!jwt) {
        return;
    }

    requestUser(jwt, doGet);
}
