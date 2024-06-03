import type { User } from '@/core/entity/user';

export interface Experiment {
    id: string;
    name: string;
    author: User;
    description: string | undefined;
    link: string | undefined;
    acknowledgement: string | undefined;
    contactMaps: ContactMap[];
    assemblies: Assembly[];
    fasta: File[];
    creationTime: Date;
}

export interface ContactMap {
    id: string;
    name: string;
    description: string | undefined;
    link: string | undefined;
    hict: File | undefined;
    agp: File[];
    mcool: File | undefined;
    tracks: File[];
    creationTime: Date;
}

export interface Assembly {
    name: string;
    description: string;
    agp: File;
    creationTime: Date;
}

export enum FileType {
    FASTA = 'fasta',
    HICT = 'hict.hdf5',
    MCOOL = 'mcool',
    AGP = 'agp',
    TRACKS = 'bed',
}

export interface File {
    id: string;
    name: string;
    filesize: number;
    sequenceLevel: string;
    creationTime: Date;
}

export interface FileAttachmentForm {
    fileId: string;
    fileType: keyof typeof FileType;
}

export interface UpdateExperimentName {
    name: string;
}

export interface UpdateExperimentInfo {
    description: string | undefined;
    link: string | undefined;
    acknowledgement: string | undefined;
}

export interface UpdateContactMapName {
    name: string;
}

export interface UpdateContactMapInfo {
    description: string | undefined;
    link: string | undefined;
}
