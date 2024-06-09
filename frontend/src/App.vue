<template>
    <header v-if="!hidden">
        <ToolbarComponent />
    </header>

    <aside>
        <div class="btn btn-outline-primary" @click="hidden = !hidden">
            <span class="bi bi-justify"></span>
        </div>
    </aside>

    <main :style="{ margin: `0 0 0 ${hidden ? '3rem' : 'calc(1rem + 15vw)'}` }">
        <RouterView />
    </main>

    <footer v-if="!hidden">
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
import { onMounted, type Ref, ref } from 'vue';
import { getAuthorizedUser } from '@/core/authentication';
import { useAuthStore } from '@/stores/auth-store';
import { getUsersCount } from '@/core/user-account-requests';

const authStore = useAuthStore();
const hidden: Ref<boolean> = ref(false);

const users = ref(0);

onMounted(() => {
    getAuthorizedUser(authStore.login);
    getUsersCount((x) => (users.value = x));
});
</script>

<style scoped lang="scss">
header {
    position: fixed;
    width: 15vw;
    padding: 1rem;
    text-align: center;
    height: calc(100% - 2rem);

    display: flex;
    flex-direction: column;
    justify-content: left;
}

aside {
    position: absolute;
    top: 1.5rem;
    left: 0.7rem;
}

main {
    padding: 1rem 0 1rem 1rem;
    min-height: calc(100vh - 4rem);
    border-left: 1px solid gray;
}

footer {
    position: fixed;
    left: 0;
    bottom: 0;
    width: 15vw;
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
