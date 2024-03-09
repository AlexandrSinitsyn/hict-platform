import type { User } from '@/core/entity/user';

export interface HiCCreationForm {
    name: string;
    description: string;
}

export interface HiCMap {
    id: number;
    author: User;
    meta: HiCMapMeta;
    views: number;
}

export interface HiCMapMeta {
    name: string;
    description: string;
    creationTime: Date;
}
