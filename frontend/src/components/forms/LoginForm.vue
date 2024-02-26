<template>
    <div>
        <button
            type="button"
            class="btn btn-primary"
            data-bs-toggle="modal"
            data-bs-target="#loginFormModal"
        >
            Login
        </button>
        <AbstractForm id="loginFormModal" title="Log in" @submit="submit">
            <div class="login">
                <span class="loginOrEmail-label">Login or email</span>
                <input class="loginOrEmail" type="text" v-model="loginOrEmail" />
                <span class="password-label">Password</span>
                <input class="password" type="password" v-model="password" />
            </div>
        </AbstractForm>
    </div>
</template>

<script setup lang="ts">
import AbstractForm from '@/components/forms/AbstractForm.vue';
import { type Ref, ref } from 'vue';
import { type LoginForm } from '@/core/types';
import { notify } from '@/core/config';

const emit = defineEmits<{
    (e: 'submit', form: LoginForm): void;
}>();

const loginOrEmail: Ref<string> = ref('');
const password: Ref<string> = ref('');

function submit() {
    const loginOrEmailValue = loginOrEmail.value;
    const passwordValue = password.value;

    if (!loginOrEmailValue || !passwordValue) {
        notify('warning', 'Login, email and password should not be an empty fields');
        return;
    }

    const isEmail = loginOrEmailValue.includes('@');

    emit('submit', {
        login: !isEmail ? loginOrEmailValue : undefined,
        email: isEmail ? loginOrEmailValue : undefined,
        password: passwordValue,
    });
}
</script>

<style scoped lang="scss">
.login {
    display: grid;
    grid-template-areas:
        'loginOrEmail-label loginOrEmail'
        'password-label password';
    grid-row-gap: 0.5rem;

    @each $area in 'loginOrEmail-label' 'loginOrEmail' 'password-label' 'password' {
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
