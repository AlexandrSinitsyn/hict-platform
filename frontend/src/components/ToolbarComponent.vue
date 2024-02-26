<template>
    <div>
        <img alt="Logo" class="logo" src="/public/img/icon.ico" width="125" height="125" />

        <h1>{{ __NAME__ }}</h1>

        <div class="authentication" v-if="jwt">
            <div>{{ jwt ?? 'Not authorized' }}</div>
            <div class="btn btn-outline-primary" @click="logout">
                <span class="bi bi-box-arrow-right"></span>
            </div>
        </div>

        <nav>
            <div v-for="p in pages" :key="p" @click="emit('goto', p)">{{ p }}</div>
        </nav>
    </div>
</template>

<script setup lang="ts">
import { __NAME__ } from '@/core/config';
import type { Jwt } from '@/core/types';
import { logout } from '@/core/authentication';

const props = defineProps<{
    jwt: Jwt | undefined;
}>();

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
