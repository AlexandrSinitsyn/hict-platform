import axios, { type AxiosResponse, type AxiosError } from 'axios';
import { AUTH, notify, SERVER } from '@/core/config';
import type { Jwt, LoginForm, RegisterForm, User } from '@/core/types';

export type SuccessCallback<E> = (e: E) => void;

function handler<T>(onSuccess: SuccessCallback<T>): (response: AxiosResponse<T>) => void {
    return (response) => {
        const { data } = response;
        onSuccess(data);
    };
}

const errorHandler = (error: AxiosError) => {
    if (error.response) {
        const { data, status } = error.response;
        notify('error', `Error [${status}]:\n${data}`);
    } else {
        notify('error', error.message);
    }
};

export function authLogin(form: LoginForm, onSuccess: SuccessCallback<Jwt>): void {
    axios
        .post<LoginForm, AxiosResponse<Jwt>>(`${AUTH}/auth/login`, form)
        .then(handler(onSuccess))
        .catch(errorHandler);
}

export function authRegister(form: RegisterForm, onSuccess: SuccessCallback<Jwt>): void {
    axios
        .post<RegisterForm, AxiosResponse<Jwt>>(`${AUTH}/auth/register`, form)
        .then(handler(onSuccess))
        .catch(errorHandler);
}

export function requestUser(jwt: Jwt, onSuccess: SuccessCallback<User | undefined>): void {
    axios
        .get<never, AxiosResponse<User>>(`${SERVER}/users/self`, {
            headers: { Authorization: `Bearer ${jwt}` },
        })
        .then(handler(onSuccess))
        .catch(errorHandler);
}
