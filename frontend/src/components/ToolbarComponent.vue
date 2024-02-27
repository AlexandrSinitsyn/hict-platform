<template>
    <div>
        <img alt="Logo" class="logo" src="/public/img/icon.ico" width="125" height="125" />

        <h1>{{ __NAME__ }}</h1>

        <div class="authentication" v-if="user">
            <div>{{ user?.username ?? 'Not authorized' }}</div>
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
import type { User } from '@/core/types';
import { logout } from '@/core/authentication';

const props = defineProps<{
    user: User | undefined;
}>();

const emit = defineEmits<{
    (e: 'goto', page: string): void;
}>();

const pages = ['Home', 'Database'];
</script>

<style scoped lang="scss">
.authentication {
    display: flex;
    flex-direction: row;
    justify-content: space-around;

    margin-bottom: 2rem;
}
</style>
