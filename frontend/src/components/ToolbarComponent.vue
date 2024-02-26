<template>
    <div>
        <img alt="Logo" class="logo" src="/public/img/icon.ico" width="125" height="125" />

        <h1 class="wrapper">{{ __NAME__ }}</h1>

        <div class="authentication">
<!--            fixme-->
<!--            <LoginForm />-->
        </div>

        <nav>
            <div v-for="p in pages" :key="p" @click="emit('goto', p)">{{ p }}</div>
        </nav>
    </div>
</template>

<script setup lang="ts">
import { __NAME__ } from '@/core/config';

const emit = defineEmits<{
    (e: 'goto', page: string): void;
}>();

const pages = ['Home'];
</script>

<style scoped lang="scss">
.authentication {
    display: flex;
    flex-direction: row;
    justify-content: space-around;

    margin-bottom: 2rem;
}
</style>
