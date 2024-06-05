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

export interface GroupCreationForm {
    name: string;
}
