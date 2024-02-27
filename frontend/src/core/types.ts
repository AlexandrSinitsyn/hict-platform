export type Jwt = string;

export interface LoginForm {
    login?: string;
    email?: string;
    password: string;
}

export interface RegisterForm {
    username: string;
    login: string;
    email: string;
    password: string;
}

export interface User {
    id: number;
    username: string;
    login: string;
    email: string;
}

export interface HiCCreationForm {
    name: string;
    description: string;
}

export interface HiCMap {
    id: number;
    author: User;
    meta: HiCMapMeta;
}

export interface HiCMapMeta {
    name: string;
    description: string;
    creationTime: Date;
}
