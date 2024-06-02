import type { Jwt, LoginForm, RegisterForm, User } from '@types';
import {
    authorizedGetRequest,
    errorHandler,
    forgetJwt,
    getJwt,
    handler,
    saveJwt,
    type SuccessCallback,
} from '@/core/server-requests';
import { __AUTH_HOST__, __SERVER_HOST__, notify } from '@/core/config';
import axios, { type AxiosResponse } from 'axios';

export function authLogin(form: LoginForm, onSuccess: SuccessCallback<Jwt>): void {
    axios
        .post<LoginForm, AxiosResponse<Jwt>>(`${__AUTH_HOST__.value}/auth/login`, form)
        .then(handler(onSuccess))
        .catch(errorHandler);
}

export function authRegister(form: RegisterForm, onSuccess: SuccessCallback<Jwt>): void {
    axios
        .post<RegisterForm, AxiosResponse<Jwt>>(`${__AUTH_HOST__.value}/auth/register`, form)
        .then(handler(onSuccess))
        .catch(errorHandler);
}

export function requestUser(onSuccess: SuccessCallback<User | undefined>): void {
    authorizedGetRequest(`${__SERVER_HOST__.value}/users/self`, onSuccess);
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

export function getAuthorizedUser(doGet: (user: User) => void) {
    const jwt = getJwt();

    if (!jwt) {
        return;
    }

    requestUser((u) => {
        if (!u) {
            notify('error', 'Invalid JWT');
            return;
        }

        doGet(u);
    });
}
