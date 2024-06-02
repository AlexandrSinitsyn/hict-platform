import axios, { type AxiosResponse, type AxiosError } from 'axios';
import { notify } from '@/core/config';
import {type Experiment, type Jwt} from '@types';

const JWT_STORAGE_KEY = 'JWT_STORAGE_KEY';

export function getJwt(): Jwt | undefined {
    return localStorage.getItem(JWT_STORAGE_KEY) ?? undefined;
}

export function saveJwt(jwt: Jwt) {
    localStorage.setItem(JWT_STORAGE_KEY, jwt);
}

export function forgetJwt() {
    localStorage.removeItem(JWT_STORAGE_KEY);
}

export type SuccessCallback<E> = (e: E) => void;

export function handler<T>(onSuccess: SuccessCallback<T>): (response: AxiosResponse<T>) => void {
    return (response) => {
        const { data } = response;
        onSuccess(data);
    };
}

export const errorHandler = (error: AxiosError) => {
    if (error.response) {
        const { data, status } = error.response;
        notify('error', `Error [${status}]:\n${data}`);
    } else {
        notify('error', error.message);
    }
};

export function authorizedRequest<T, R>(
    method: typeof axios.post,
    url: string,
    data: T,
    onSuccess: SuccessCallback<R>
): void {
    const jwt = getJwt();

    if (!jwt) {
        notify('error', 'Not authorized');
        return;
    }

    method<T, AxiosResponse<R>>(url, data, {
        headers: {
            Authorization: `Bearer ${jwt}`,
        },
    })
        .then(handler(onSuccess))
        .catch(errorHandler);
}

export function authorizedGetRequest<R>(url: string, onSuccess: SuccessCallback<R>): void {
    const jwt = getJwt();

    if (!jwt) {
        notify('error', 'Not authorized');
        return;
    }

    axios
        .get<never, AxiosResponse<R>>(url, {
            headers: {
                Authorization: `Bearer ${jwt}`,
            },
        })
        .then(handler(onSuccess))
        .catch(errorHandler);
}

export const updateNotify: SuccessCallback<boolean> = (updated) => {
    if (updated) {
        notify('info', `Successfully updated`);
    } else {
        notify('warning', `Has errors. Not updated`);
    }
};

// eslint-disable-next-line @typescript-eslint/no-empty-function
export const nop: SuccessCallback<never> = () => {};
