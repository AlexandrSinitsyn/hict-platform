import { type ContactMap, type Experiment, type File as AttachedFile, FileType } from '@types';
import { __SERVER_HOST__, notify } from '@/core/config';
import axios, { type AxiosResponse } from 'axios';
import {
    authorizedRequest,
    errorHandler,
    getJwt,
    handler,
    type SuccessCallback,
} from '@/core/server-requests';
import type { FileAttachmentForm } from '@/core/entity/experiments';

export function uploadFile(
    file: File,
    fileType: keyof typeof FileType,
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
        .post<FormData, AxiosResponse<AttachedFile>>(
            `${__SERVER_HOST__.value}/files/publish`,
            formData,
            {
                headers: {
                    'Content-Type': 'multipart/form-data',
                    Authorization: `Bearer ${jwt}`,
                },
            }
        )
        .then(handler(onSuccess))
        .catch(errorHandler);
}

function attachFileTo<T = Experiment | ContactMap>(
    obj: T,
    objType: 'experiment' | 'contact-map',
    file: AttachedFile,
    fileType: keyof typeof FileType,
    onSuccess: SuccessCallback<boolean>
): void {
    const url =
        objType === 'experiment'
            ? `experiment/${(obj as Experiment).id}`
            : `contact-map/${(obj as ContactMap).id}`;
    authorizedRequest<FileAttachmentForm, boolean>(
        axios.post,
        `${__SERVER_HOST__.value}/files/attach/${url}`,
        {
            fileId: file.id,
            fileType: fileType,
        },
        onSuccess
    );
}

export function attachFastaToExperiment(
    experiment: Experiment,
    fasta: AttachedFile,
    onSuccess: SuccessCallback<boolean>
): void {
    attachFileTo<Experiment>(experiment, 'experiment', fasta, FileType[FileType.FASTA], onSuccess);
}

export function attachHictToContactMap(
    contactMap: ContactMap,
    hict: AttachedFile,
    onSuccess: SuccessCallback<boolean>
): void {
    attachFileTo<ContactMap>(contactMap, 'contact-map', hict, FileType[FileType.HICT], onSuccess);
}
