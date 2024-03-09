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
