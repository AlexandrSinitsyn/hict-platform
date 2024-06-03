<template>
    <div>
        <h1>Hi-C Map view</h1>

        <div v-if="selected">
            <p>Name: {{ selected.name }}</p>
            <p>Description: {{ selected.description }}</p>
            <p>Created at: {{ selected.creationTime }}</p>
<!--            <p>Views: {{ selected.views }}</p>-->
            <App />
        </div>
        <div v-else>You have not selected any map yet!</div>
    </div>
</template>

<script setup lang="ts">
import { onMounted, type Ref, ref } from 'vue';
import { acquireContactMap, pingContactMap } from '@/core/experiment-requests';
import type { ContactMap } from '@types';
import { __HiCT_CLUSTER__, notify } from '@/core/config';
import { useAuthStore } from '@/stores/auth-store';
import { storeToRefs } from 'pinia';
import { useRoute } from 'vue-router';
import { useRemoteHostStore } from '@hict/app/stores/remoteHost';

import App from '@hict/App.vue';
import { useSelectedMapStore } from '@hict/app/stores/selectedMapStore';

const { user } = storeToRefs(useAuthStore());
const selected: Ref<ContactMap | undefined> = ref(undefined);

const doRequest = () => {
    const uid = user.value?.id;

    if (!uid) {
        notify('error', 'You should be authorized');
        return;
    }

    pingContactMap(uid);

    return `${__HiCT_CLUSTER__.value}/${uid}`;
};

onMounted(() => {
    useRemoteHostStore().setDynamicRemoteHost(doRequest);
    const { selectedMap } = storeToRefs(useSelectedMapStore());

    const router = useRoute();
    acquireContactMap(router.params.contactMapName as string, (map) => {
        selected.value = map;
        selectedMap.value = map.name;
    });
});
</script>

<style scoped lang="scss"></style>
