<template>
    <div>
        <img alt="Logo" class="logo" src="/public/img/icon.ico" width="125" height="125" />

        <h1>{{ __NAME__ }}</h1>

        <div class="auth">
            <LoginForm @submit="(form) => auth(login, form)" />

            <RegisterForm @submit="(form) => auth(register, form)" />
        </div>
    </div>
</template>

<script setup lang="ts">
import LoginForm from '@/components/forms/LoginForm.vue';
import RegisterForm from '@/components/forms/RegisterForm.vue';
import { __NAME__ } from '@/core/config';
import { login, register } from '@/core/authentication';

const emit = defineEmits<{
    (e: 'entered'): void;
}>();

function auth<F>(method: (f: F, then: () => void) => void, form: F) {
    method(form, () => emit('entered'));
}
</script>

<style scoped lang="scss">
.auth {
    width: 15vw;
    display: flex;
    justify-content: space-around;
}
</style>
