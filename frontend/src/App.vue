<template>
    <header>
        <ToolbarComponent />
    </header>

    <main>
        <RouterView />
    </main>

    <footer>
        <p>
            &copy; Powered by <code>{{ __AUTHOR__ }}</code>
        </p>
        <p>Registered users count: {{ users }}</p>
        <i class="version">v{{ __VERSION__ }}</i>
    </footer>
</template>

<script setup lang="ts">
import ToolbarComponent from '@/components/ToolbarComponent.vue';
import { __VERSION__, __AUTHOR__ } from '@/core/config';
import { onMounted, ref } from 'vue';
import { getAuthorizedUser } from '@/core/authentication';
import { useAuthStore } from '@/stores/auth-store';
import { getUsersCount } from '@/core/server-requests';

const authStore = useAuthStore();

const users = ref(0);

function enter() {
    getAuthorizedUser(authStore.login);
    getUsersCount((x) => (users.value = x));
}

onMounted(enter);
</script>

<style lang="scss">
$aside-size: 15vw;

header {
    position: fixed;
    width: $aside-size;
    padding: 1rem;
    text-align: center;
    height: calc(100% - 2rem);

    display: flex;
    flex-direction: column;
    justify-content: left;
}

main {
    margin: 0 0 0 calc(1rem + $aside-size);
    padding: 1rem 0 1rem 1rem;
    min-height: calc(100vh - 4rem);
    border-left: 1px solid gray;
}

footer {
    position: fixed;
    left: 0;
    bottom: 0;
    width: $aside-size;
    margin: 1rem;
    padding: 1rem;
    border-top: 1px solid gray;
    text-align: center;
    font-size: 0.8rem;

    p {
        margin: 0;
    }
}
</style>
