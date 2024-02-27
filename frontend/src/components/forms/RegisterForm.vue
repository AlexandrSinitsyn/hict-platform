<template>
    <div>
        <button
            type="button"
            class="btn btn-primary"
            data-bs-toggle="modal"
            data-bs-target="#registerFormModal"
        >
            Register
        </button>
        <AbstractForm id="registerFormModal" title="Register" @submit="submit">
            <div class="register">
                <span class="username-label">Username</span>
                <input class="username" type="text" v-model="username" />
                <span class="login-label">Login</span>
                <input class="login" type="text" v-model="login" />
                <span class="email-label">Email</span>
                <input class="email" type="text" v-model="email" />
                <span class="password-label">Password</span>
                <input class="password" type="password" v-model="password" />
            </div>
        </AbstractForm>
    </div>
</template>

<script setup lang="ts">
import AbstractForm from '@/components/forms/AbstractForm.vue';
import { type Ref, ref } from 'vue';
import { type RegisterForm } from '@/core/types';
import { notify } from '@/core/config';

const emit = defineEmits<{
    (e: 'submit', form: RegisterForm): void;
}>();

const username: Ref<string> = ref('');
const login: Ref<string> = ref('');
const email: Ref<string> = ref('');
const password: Ref<string> = ref('');

function submit() {
    const usernameValue = username.value;
    const loginValue = login.value;
    const emailValue = email.value;
    const passwordValue = password.value;

    if (!usernameValue || !loginValue || !emailValue || !passwordValue) {
        notify('warning', 'Username, login, email and password should not be an empty fields');
        return;
    }

    emit('submit', {
        username: usernameValue,
        login: loginValue,
        email: emailValue,
        password: passwordValue,
    });
}
</script>

<style scoped lang="scss">
.register {
    display: grid;
    grid-template-areas:
        'username-label username'
        'login-label login'
        'email-label email'
        'password-label password';
    grid-row-gap: 0.5rem;

    @each $area in
            'username-label' 'username' 'login-label' 'login' 'email-label' 'email' 'password-label' 'password' {
        & > .#{$area}-label {
            grid-area: unquote($area)-label;
            text-align: right;
            margin-right: 1rem;

            &::after {
                content: ':';
            }
        }

        & > .#{$area} {
            grid-area: unquote($area);
        }
    }
}
</style>
