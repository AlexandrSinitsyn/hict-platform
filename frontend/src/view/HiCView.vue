<template>
    <div>
        <h1>Hi-C Map view</h1>

        <div v-if="selected">
            <p>Name: {{ selected.meta.name }}</p>
<!--            <p>Description: {{ selected.meta.description }}</p>-->
<!--            <p>Created at: {{ selected.meta.creationTime }}</p>-->
<!--            <p>Views: {{ selected.views }}</p>-->

            <App />
        </div>
        <div v-else>You have not selected any map yet!</div>
    </div>
</template>

<script setup lang="ts">
import { onMounted, type Ref, ref } from 'vue';
import { acquireHiCMap, pingHiCMap } from '@/core/server-requests';
import type { HiCMap } from '@types';
import { HiCT, notify } from '@/core/config';
import { useAuthStore } from '@/stores/auth-store';
import { storeToRefs } from 'pinia';
import { useRoute } from 'vue-router';
import { useRemoteHostStore } from '@hict/app/stores/remoteHost';

import App from '@hict/App.vue';

const { user } = storeToRefs(useAuthStore());
const selected: Ref<HiCMap | undefined> = ref(undefined);

const doRequest = () => {
    pingHiCMap(useRoute().params.hiCMapName as string);

    const uid = user.value?.id;

    if (!uid) {
        notify('error', 'You should be authorized');
    }

    return `${HiCT}/${uid}`;
};

onMounted(() => {
    useRemoteHostStore().setDynamicRemoteHost(doRequest);

    const router = useRoute();
    acquireHiCMap(router.params.hiCMapName as string, (map) => {
        selected.value = map;
    });
});
</script>

<style scoped lang="scss"></style>
