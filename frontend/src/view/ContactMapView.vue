<template>
    <aside class="hiding-switcher">
        <div class="btn btn-outline-primary" @click="hidden = !hidden">
            <span class="bi bi-info-circle"></span>
        </div>
    </aside>
    <div>
        <div v-if="selected">
            <aside v-if="!hidden" class="contact-map-info">
                <h1>Hi-C Map</h1>

                <table v-if="selected">
                    <tr v-for="{ columnName, value } in mapInfo()" :key="columnName">
                        <th>{{ columnName }}</th>
                        <td>{{ value }}</td>
                    </tr>
                </table>
            </aside>
            <div class="main-screen" :style="hidden ? '' : 'margin-right: calc(20vw + 2rem)'">
                <App />
            </div>
        </div>
        <div v-else>You have not selected any map yet!</div>
    </div>
</template>

<script setup lang="ts">
import { onMounted, type Ref, ref } from 'vue';
import { acquireContactMap, pingContactMap } from '@/core/experiment-requests';
import { type ContactMap, FileType } from '@types';
import { __HiCT_CLUSTER__, notify } from '@/core/config';
import { useAuthStore } from '@/stores/auth-store';
import { storeToRefs } from 'pinia';
import { useRoute } from 'vue-router';
import { useRemoteHostStore } from '@hict/app/stores/remoteHost';

import App from '@hict/App.vue';
import { useSelectedMapStore } from '@hict/app/stores/selectedMapStore';

const { user } = storeToRefs(useAuthStore());
const selected: Ref<ContactMap | undefined> = ref(undefined);
const hidden: Ref<boolean> = ref(false);

const doRequest = () => {
    const uid = user.value?.id;

    if (!uid) {
        notify('error', 'You should be authorized');
        throw 'Not authorized';
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

        const hicMap = map.hict;

        if (!hicMap) {
            notify('error', 'Nothing to show! No Hi-C map attached for this experiment!');
            return;
        }

        selectedMap.value = `${hicMap.id}.${FileType.HICT}`;
    });
});

function mapInfo(): { columnName: string; value: string }[] {
    const map = selected.value;

    if (!map) {
        return [];
    }

    return [
        { columnName: 'Id', value: map.id },
        { columnName: 'Name', value: map.name },
        { columnName: 'Description', value: map.description ?? '/No data/' },
        { columnName: 'Link', value: map.link ?? '/No data/' },
        { columnName: 'Creation time', value: map.creationTime },
    ];
}
</script>

<style scoped lang="scss">
@import '/public/css/main';

.hiding-switcher {
    position: absolute;
    top: 5rem;
    left: 0.7rem;
}

.main-screen {
    position: inherit;
    margin-top: -2rem;
    top: 0;
    right: 0;

    padding-right: 1rem;
}

.contact-map-info {
    position: absolute;
    right: 0;
    width: calc(20vw + 2rem);
    //margin: 0 1rem;
    padding: 1rem;
    font-size: 0.8rem;
    border-left: 1px solid gray;

    & th {
        vertical-align: top;
        text-align: right;
        padding-right: 0.5rem;

        &::after {
            content: ':';
        }
    }

    & td {
        text-align: left;
    }
}
</style>
