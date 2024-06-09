import {
    authorizedGetRequest,
    authorizedRequest,
    nop,
    type SuccessCallback,
    updateNotify,
} from '@/core/server-requests';
import { __SERVER_HOST__ } from '@/core/config';
import axios from 'axios';
import type {
    ContactMap,
    Experiment,
    Group,
    ExperimentCreationForm,
    UpdateContactMapInfo,
    UpdateContactMapName,
    UpdateExperimentInfo,
    UpdateExperimentName,
} from '@types';

export function getAllExperiments(onSuccess: SuccessCallback<Experiment[]>): void {
    authorizedGetRequest(`${__SERVER_HOST__.value}/experiment/all`, onSuccess);
}

export function publishExperiment(group: Group, onSuccess: SuccessCallback<Experiment>): void {
    authorizedRequest<ExperimentCreationForm, Experiment>(
        axios.post,
        `${__SERVER_HOST__.value}/experiment/new`,
        {
            groupName: group.name,
        },
        onSuccess
    );
}

export function publishContactMap(
    experiment: Experiment,
    onSuccess: SuccessCallback<ContactMap>
): void {
    authorizedRequest(
        axios.post,
        `${__SERVER_HOST__.value}/contact-map/new`,
        {
            experimentId: experiment.id,
        },
        onSuccess
    );
}

export function updateExperimentName(experiment: Experiment, form: UpdateExperimentName): void {
    authorizedRequest(
        axios.patch,
        `${__SERVER_HOST__.value}/experiment/${experiment.id}/update/name`,
        form,
        updateNotify
    );
}

export function updateExperimentInfo(experiment: Experiment, form: UpdateExperimentInfo): void {
    authorizedRequest(
        axios.patch,
        `${__SERVER_HOST__.value}/experiment/${experiment.id}/update/name`,
        form,
        updateNotify
    );
}

export function updateContactMapName(contactMap: ContactMap, form: UpdateContactMapName): void {
    authorizedRequest(
        axios.patch,
        `${__SERVER_HOST__.value}/contact-map/${contactMap.id}/update/name`,
        form,
        updateNotify
    );
}

export function updateContactMapInfo(contactMap: ContactMap, form: UpdateContactMapInfo): void {
    authorizedRequest(
        axios.patch,
        `${__SERVER_HOST__.value}/contact-map/${contactMap.id}/update/info`,
        form,
        updateNotify
    );
}

export function acquireContactMap(id: string, onSuccess: SuccessCallback<ContactMap>): void {
    authorizedGetRequest(`${__SERVER_HOST__.value}/contact-map/acquire/${id}`, onSuccess);
}

export function pingContactMap(id: string): void {
    authorizedGetRequest(`${__SERVER_HOST__.value}/contact-map/acquire/${id}/ping`, nop);
}
