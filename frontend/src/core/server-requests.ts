import axios, { type AxiosResponse, type AxiosError } from 'axios';
import { AUTH, notify, SERVER } from '@/core/config';
import type {
    ContactMap,
    Experiment, Group, GroupCreationForm,
    Jwt,
    LoginForm,
    RegisterForm,
    UpdateUserInfo,
    UpdateUserPassword,
    UpdateUserRole,
    User,
} from '@types';
import { getJwt } from '@/core/authentication';
import { type File as AttachedFile, FileType } from '@types';
import type {
    UpdateContactMapInfo,
    UpdateContactMapName,
    UpdateExperimentInfo,
    UpdateExperimentName,
} from '@/core/entity/experiments';

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

function authorizedGetRequest<R>(url: string, onSuccess: SuccessCallback<R>): void {
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

const updateNotify: SuccessCallback<boolean> = (updated) => {
    if (updated) {
        notify('info', `Successfully updated`);
    } else {
        notify('warning', `Has errors. Not updated`);
    }
};

// eslint-disable-next-line @typescript-eslint/no-empty-function
const nop: SuccessCallback<never> = () => {};

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
    authorizedGetRequest(`${SERVER}/users/self`, onSuccess);
}

export function updateUserInfo(form: UpdateUserInfo): void {
    authorizedRequest(axios.patch, `${SERVER}/users/update/info`, form, updateNotify);
}

export function updateUserPassword(form: UpdateUserPassword): void {
    authorizedRequest(axios.patch, `${SERVER}/users/update/password`, form, updateNotify);
}

export function updateUserRole(form: UpdateUserRole): void {
    authorizedRequest(axios.patch, `${SERVER}/users/update/role`, form, updateNotify);
}

export function getAllUsers(onSuccess: SuccessCallback<User[]>): void {
    authorizedGetRequest(`${SERVER}/users/all`, onSuccess);
}

export function getUsersCount(onSuccess: SuccessCallback<number>): void {
    axios
        .get<never, AxiosResponse<number>>(`${SERVER}/users/count`)
        .then(handler(onSuccess))
        .catch(errorHandler);
}

export function uploadFile(
    file: File,
    fileType: string,
    onSuccess: SuccessCallback<AttachedFile>
): void {
    const jwt = getJwt();

    if (!jwt) {
        notify('error', 'Not authorized');
        return;
    }

    const formData = new FormData();
    formData.append('file', file);
    formData.append(
        'type',
        new Blob([JSON.stringify(fileType)], {
            type: 'application/json',
        })
    );

    axios
        .post<FormData, AxiosResponse<AttachedFile>>(`${SERVER}/files/publish`, formData, {
            headers: {
                'Content-Type': 'multipart/form-data',
                Authorization: `Bearer ${jwt}`,
            },
        })
        .then(handler(onSuccess))
        .catch(errorHandler);
}

export function getAllExperiments(onSuccess: SuccessCallback<Experiment[]>): void {
    authorizedGetRequest(`${SERVER}/experiment/all`, onSuccess);
}

export function getAllGroups(onSuccess: SuccessCallback<Group[]>): void {
    authorizedGetRequest(`${SERVER}/groups/all`, onSuccess);
}

export function publishExperiment(onSuccess: SuccessCallback<Experiment>): void {
    authorizedRequest(axios.post, `${SERVER}/experiment/new`, {}, onSuccess);
}

export function publishGroup(
    groupForm: GroupCreationForm,
    onSuccess: SuccessCallback<Group>
): void {
    authorizedRequest(axios.post, `${SERVER}/groups/new`, groupForm, onSuccess);
}

export function publishContactMap(
    experiment: Experiment,
    onSuccess: SuccessCallback<ContactMap>
): void {
    authorizedRequest(axios.post, `${SERVER}/contact-map/new`, experiment, onSuccess);
}

export function joinGroup(group: Group): void {
    authorizedRequest(axios.post, `${SERVER}/groups/${group.name}/join`, {}, nop);
}

export function updateExperimentName(experiment: Experiment, form: UpdateExperimentName): void {
    authorizedRequest(
        axios.patch,
        `${SERVER}/experiment/${experiment.id}/update/name`,
        form,
        updateNotify
    );
}

export function updateExperimentInfo(experiment: Experiment, form: UpdateExperimentInfo): void {
    authorizedRequest(
        axios.patch,
        `${SERVER}/experiment/${experiment.id}/update/name`,
        form,
        updateNotify
    );
}

export function updateContactMapName(contactMap: ContactMap, form: UpdateContactMapName): void {
    authorizedRequest(
        axios.patch,
        `${SERVER}/contact-map/${contactMap.id}/update/name`,
        form,
        updateNotify
    );
}

export function updateContactMapInfo(contactMap: ContactMap, form: UpdateContactMapInfo): void {
    authorizedRequest(
        axios.patch,
        `${SERVER}/contact-map/${contactMap.id}/update/info`,
        form,
        updateNotify
    );
}

export function acquireContactMap(id: string, onSuccess: SuccessCallback<ContactMap>): void {
    authorizedGetRequest(`${SERVER}/contact-map/acquire/${id}`, onSuccess);
}

export function pingContactMap(id: string): void {
    authorizedGetRequest(`${SERVER}/contact-map/acquire/${id}/ping`, nop);
}
