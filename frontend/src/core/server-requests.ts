import axios, { type AxiosResponse, type AxiosError } from 'axios';
import { AUTH, notify, SERVER } from '@/core/config';
import type {
    HiCMap,
    Jwt,
    LoginForm,
    RegisterForm,
    UpdateUserInfo,
    UpdateUserPassword,
    User,
} from '@/core/types';
import { getJwt } from '@/core/authentication';

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

export function getAllHiCMaps(onSuccess: SuccessCallback<HiCMap[]>): void {
    axios
        .get<never, AxiosResponse<HiCMap[]>>(`${SERVER}/hi-c/all`)
        .then(handler(onSuccess))
        .catch(errorHandler);
}

function authorizedRequest<T, R>(
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

export function publishHiCMap(formData: FormData, onSuccess: SuccessCallback<HiCMap>): void {
    const jwt = getJwt();

    if (!jwt) {
        notify('error', 'Not authorized');
        return;
    }

    axios
        .post<FormData, AxiosResponse<HiCMap>>(`${SERVER}/hi-c/publish`, formData, {
            headers: {
                'Content-Type': 'multipart/form-data',
                Authorization: `Bearer ${jwt}`,
            },
        })
        .then(handler(onSuccess))
        .catch(errorHandler);
}

export function updateUserInfo(form: UpdateUserInfo, onSuccess: SuccessCallback<boolean>): void {
    authorizedRequest(axios.patch, `${SERVER}/users/update/info`, form, onSuccess);
}

export function updateUserPassword(
    form: UpdateUserPassword,
    onSuccess: SuccessCallback<boolean>
): void {
    authorizedRequest(axios.patch, `${SERVER}/users/update/password`, form, onSuccess);
}
