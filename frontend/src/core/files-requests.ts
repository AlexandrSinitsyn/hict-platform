import { type ContactMap, type Experiment, type File as AttachedFile, FileType } from '@types';
import { __SERVER_HOST__, notify } from '@/core/config';
import axios, { type AxiosResponse } from 'axios';
import {
    authorizedRequest,
    errorHandler,
    getJwt,
    type SuccessCallback,
} from '@/core/server-requests';
import type { FileAttachmentForm, FileUploadingStreamForm } from '@/core/entity/experiments';
import { fileType } from '@/core/extensions';

export async function uploadFile(
    file: File,
    fileType: keyof typeof FileType,
    onSuccess: SuccessCallback<AttachedFile>
) {
    const jwt = getJwt();

    if (!jwt) {
        notify('error', 'Not authorized');
        return;
    }

    const session = (
        await axios.get<never, AxiosResponse<string>>(`${__SERVER_HOST__.value}/files/session/init`)
    ).data;

    // noinspection PointlessArithmeticExpressionJS
    const $1mb = 1 * 1024 * 1024 - 1;

    function append(formData: FormData, partName: string, value: string) {
        formData.append(
            partName,
            new Blob([JSON.stringify(value)], {
                type: 'application/json',
            })
        );
    }

    // async function convertToBase64Async(file: File): Promise<string | undefined> {
    //     return new Promise((resolve, reject) => {
    //         const reader = new FileReader();
    //         reader.onload = () => resolve(reader.result?.toString()?.replace(/^data:(.*,)?/, ''));
    //         reader.onerror = (error) => reject(error);
    //         reader.readAsDataURL(file);
    //     });
    // }
    //
    // const base64 = await convertToBase64Async(file);
    //
    // if (!base64) {
    //     notify('error', 'Invalid file was converted to a null base64!');
    //     return;
    // }

    const fileSize = file.size;//base64.length;

    const parts: Promise<void>[] = [];

    let start = 0;
    while (start < fileSize) {
        const end = Math.min(start + $1mb, fileSize);

        const formData = new FormData();

        append(formData, 'session', session);

        append(formData, 'partIndex', `${start}`);

        // formData.append(
        //     'file',
        //     new Blob([base64.slice(start, end)], {
        //         type: 'application/text',
        //     })
        // );
        //
        // console.log(start, '>', base64.slice(start, end));

        formData.append(`file`, file.slice(start, end));

        // parts.push(
            await axios
                .post<FormData, never>(`${__SERVER_HOST__.value}/files/publish`, formData, {
                    headers: {
                        Authorization: `Bearer ${jwt}`,
                    },
                })
                .catch(errorHandler)
        // );
        start = end;
    }

    // for (const p of parts) {
    //     await p;
    // }

    authorizedRequest<FileUploadingStreamForm, AttachedFile>(
        axios.post,
        `${__SERVER_HOST__.value}/files/session/${session}/close`,
        {
            type: fileType,
            filename: file.name,
            fileSize: file.size,
        },
        onSuccess
    );
}

function attachFileTo<T = Experiment | ContactMap>(
    obj: T,
    objType: 'experiment' | 'contact-map',
    file: AttachedFile,
    type: FileType,
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
            fileType: fileType(type),
        },
        onSuccess
    );
}

export function attachFastaToExperiment(
    experiment: Experiment,
    fasta: AttachedFile,
    onSuccess: SuccessCallback<boolean>
): void {
    attachFileTo<Experiment>(experiment, 'experiment', fasta, FileType.FASTA, onSuccess);
}

export function attachHictToContactMap(
    contactMap: ContactMap,
    hict: AttachedFile,
    onSuccess: SuccessCallback<boolean>
): void {
    attachFileTo<ContactMap>(contactMap, 'contact-map', hict, FileType.HICT, onSuccess);
}
