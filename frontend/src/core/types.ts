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

export enum Role {
    ANONYMOUS,
    USER,
    ADMIN,
    SUPERUSER,
}

export interface User {
    id: number;
    username: string;
    login: string;
    email: string;
    role: Role;
}

export interface UpdateUserInfo {
    username: string | null;
    login: string | null;
    email: string | null;
}

export interface UpdateUserPassword {
    oldPassword: string | undefined;
    newPassword: string | undefined;
}

export interface UpdateUserRole {
    id: number;
    newRole: Role;
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
