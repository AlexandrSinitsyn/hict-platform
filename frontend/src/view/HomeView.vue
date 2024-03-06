<template>
    <div>
        <img alt="Logo" class="logo" src="/public/img/icon.ico" width="125" height="125" />

        <h1>{{ __NAME__ }}</h1>

        <div class="auth">
            <LoginFormComponent @submit="loginSubmit" />

            <RegisterFormComponent @submit="registerSubmit" />
        </div>
    </div>
</template>

<script setup lang="ts">
import LoginFormComponent from '@/components/forms/LoginFormComponent.vue';
import RegisterFormComponent from '@/components/forms/RegisterFormComponent.vue';
import { __NAME__ } from '@/core/config';
import { getAuthorizedUser, login, register } from '@/core/authentication';
import { useAuthStore } from '@/stores/auth-store';
import type { LoginForm, RegisterForm } from '@/core/types';

const authStore = useAuthStore();

function loginSubmit(form: LoginForm) {
    login(form, () => getAuthorizedUser(authStore.login));
}

function registerSubmit(form: RegisterForm) {
    register(form, () => getAuthorizedUser(authStore.login));
}
</script>

<style scoped lang="scss">
.auth {
    width: 15vw;
    display: flex;
    justify-content: space-around;
}
</style>
