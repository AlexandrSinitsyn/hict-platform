import {
    authorizedGetRequest,
    authorizedRequest,
    errorHandler,
    handler,
    nop,
    type SuccessCallback,
    updateNotify,
} from '@/core/server-requests';
import { __SERVER_HOST__ } from '@/core/config';
import axios, { type AxiosResponse } from 'axios';
import type { Group, GroupCreationForm, UpdateUserInfo, UpdateUserPassword, User } from '@types';

export function updateUserInfo(form: UpdateUserInfo): void {
    authorizedRequest(
        axios.patch,
        `${__SERVER_HOST__.value}/users/update/info`,
        form,
        updateNotify
    );
}

export function updateUserPassword(form: UpdateUserPassword): void {
    authorizedRequest(
        axios.patch,
        `${__SERVER_HOST__.value}/users/update/password`,
        form,
        updateNotify
    );
}

export function getAllUsers(onSuccess: SuccessCallback<User[]>): void {
    authorizedGetRequest(`${__SERVER_HOST__.value}/users/all`, onSuccess);
}

export function getUsersCount(onSuccess: SuccessCallback<number>): void {
    axios
        .get<never, AxiosResponse<number>>(`${__SERVER_HOST__.value}/users/count`)
        .then(handler(onSuccess))
        .catch(errorHandler);
}

export function publishGroup(
    groupForm: GroupCreationForm,
    onSuccess: SuccessCallback<Group>
): void {
    authorizedRequest(axios.post, `${__SERVER_HOST__.value}/groups/new`, groupForm, onSuccess);
}

export function getAllGroups(onSuccess: SuccessCallback<Group[]>): void {
    authorizedGetRequest(`${__SERVER_HOST__.value}/groups/all`, onSuccess);
}

export function joinGroup(group: Group): void {
    authorizedRequest(axios.post, `${__SERVER_HOST__.value}/groups/${group.name}/join`, {}, nop);
}
