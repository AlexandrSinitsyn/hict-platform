import type { User } from '@/core/entity/user';

export interface Experiment {
    id: number;
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
    id: number;
    name: string;
    description: string;
    link: string;
    hic: File;
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
    FASTA,
    HIC,
    MCOOL,
    AGP,
    TRACKS,
}

export interface File {
    name: string;
    filesize: number;
    sequenceLevel: string;
    creationTime: Date;
}
