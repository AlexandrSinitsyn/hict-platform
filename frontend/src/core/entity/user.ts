export enum Role {
    ANONYMOUS,
    USER,
    ADMIN,
    SUPERUSER,
}

export interface User {
    id: string;
    username: string;
    login: string;
    email: string;
    groups: Group[];
}

export interface Group {
    id: string;
    name: string;
    affiliation: string | undefined;
    creationTime: Date;
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
    id: string;
    newRole: Role;
}
