<template>
    <header>
        <ToolbarComponent :user="user" @goto="goto" />
    </header>

    <main>
        <HomeComponent v-if="page == 'Home'" @entered="enter" />
        <DatabaseComponent v-if="page == 'Database'" />
        <UploadComponent v-if="page == 'Upload'" @uploaded="() => goto('Database')" />
    </main>

    <footer>
        <p>
            &copy; Powered by <code>{{ __AUTHOR__ }}</code>
        </p>
        <i class="version">v{{ __VERSION__ }}</i>
    </footer>
</template>

<script setup lang="ts">
import ToolbarComponent from '@/components/ToolbarComponent.vue';
import HomeComponent from '@/components/HomeComponent.vue';
import DatabaseComponent from '@/components/DatabaseComponent.vue';
import UploadComponent from '@/components/UploadComponent.vue';
import { __VERSION__, __AUTHOR__ } from '@/core/config';
import { onMounted, type Ref, ref } from 'vue';
import type { User } from '@/core/types';
import { getAuthorizedUser } from '@/core/authentication';

const page: Ref<string> = ref('Home');
const user: Ref<User | undefined> = ref(undefined);

function goto(pagename: string) {
    page.value = pagename;
}

function enter() {
    getAuthorizedUser((u) => {
        user.value = u;
    });
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
    margin: 1rem 1rem 0 calc(1rem + $aside-size);
    padding: 1rem;
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
