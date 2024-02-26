<template>
    <div>
        <img alt="Logo" class="logo" src="/public/img/icon.ico" width="125" height="125" />

        <h1 class="wrapper">{{ __NAME__ }}</h1>
        <LoginForm @submit="login" />
    </div>
</template>

<script setup lang="ts">
import LoginForm from '@/components/forms/LoginForm.vue';
import { __NAME__ } from '@/core/config';
import { login } from '@/core/authentication';
</script>

<style scoped lang="scss"></style>
