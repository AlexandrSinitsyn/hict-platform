<template>
    <div>
        <img alt="Logo" class="logo" src="/public/img/icon.ico" width="125" height="125" />

        <h1 class="headername">{{ __NAME__ }}</h1>

        <div v-if="user" class="btn-group headername" role="group" aria-label="Basic example">
            <button type="button" class="btn btn-light" style="color: black">
                {{ user.username }}
            </button>
            <button type="button" class="btn btn-danger" @click="logout">Logout</button>
        </div>
        <div v-else class="btn-group headername" role="group" aria-label="Basic example">
            <LoginFormComponent @submit="loginSubmit" />

            <RegisterFormComponent @submit="registerSubmit" />
        </div>

        <nav>
            <ul>
                <li v-for="p in pages" :key="p">
                    <RouterLink :to="{ name: p.toLowerCase() }">{{ p }}</RouterLink>
                </li>
            </ul>
        </nav>
    </div>
</template>

<script setup lang="ts">
import RegisterFormComponent from '@/components/forms/RegisterFormComponent.vue';
import LoginFormComponent from '@/components/forms/LoginFormComponent.vue';
import { __NAME__ } from '@/core/config';
import { getAuthorizedUser, login, logout, register } from '@/core/authentication';
import { useAuthStore } from '@/stores/auth-store';
import { storeToRefs } from 'pinia';
import type { LoginForm, RegisterForm } from '@types';

const { user } = storeToRefs(useAuthStore());
const authStore = useAuthStore();

const pages: string[] = ['Home', 'Groups', 'Account', 'Admin'];

function loginSubmit(form: LoginForm) {
    login(form, () => getAuthorizedUser(authStore.login));
}

function registerSubmit(form: RegisterForm) {
    register(form, () => getAuthorizedUser(authStore.login));
}
</script>

<style scoped lang="scss">
@import '/public/css/main';

.headername {
    width: 100%;
}

button {
    color: white;
}

nav > ul {
    list-style: none;
    padding: 2rem 0 0 0;

    & > li {
        text-decoration: underline;
        text-align: start;
        margin-left: 2rem;
        font-size: 1.5rem;

        a {
            text-decoration: none;
        }

        :hover {
            font-weight: bolder;

            &::before {
                content: '> ';
            }
        }
    }
}
</style>
