<template>
    <div>
        <img alt="Logo" class="logo" src="/public/img/icon.ico" width="125" height="125" />

        <h1>{{ __NAME__ }}</h1>

        <div class="authentication" v-if="user">
            <div>{{ user.username }}</div>
            <div class="btn btn-outline-primary" @click="logout">
                <span class="bi bi-box-arrow-right"></span>
            </div>
        </div>

        <nav>
            <RouterLink
                v-for="p in pages"
                :key="p[0]"
                :to="p.length === 1 ? `/${p[0].toLowerCase()}` : p[1]"
            >
                <div>{{ p[0] }}</div>
            </RouterLink>
        </nav>
    </div>
</template>

<script setup lang="ts">
import { __NAME__ } from '@/core/config';
import { logout } from '@/core/authentication';
import { useAuthStore } from '@/stores/auth-store';
import { storeToRefs } from 'pinia';

const { user } = storeToRefs(useAuthStore());

const pages: ([string] | [string, string])[] = [
    ['Home'],
    ['Groups'],
    ['Experiments'],
    ['Account'],
    ['Admin'],
];
</script>

<style scoped lang="scss">
.authentication {
    display: flex;
    flex-direction: row;
    justify-content: space-around;

    margin-bottom: 2rem;
}
</style>
