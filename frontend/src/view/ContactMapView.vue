<template>
    <div>
        <div v-if="selected">
            <aside>
                <h1>Hi-C Map</h1>

                <table v-if="selected">
                    <tr v-for="{ columnName, value } in mapInfo()" :key="columnName">
                        <th>{{ columnName }}</th>
                        <td>{{ value }}</td>
                    </tr>
                </table>
            </aside>
            <div class="main-screen">
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
        selectedMap.value = `${map.name}${FileType.HICT}`;
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

$aside-width: calc(20vw + 2rem);

.main-screen {
    position: absolute;
    top: 0;
    right: 0;
    left: calc(15vw + 3rem); // header width

    margin-right: calc($aside-width);
    padding-right: 1rem;
}

aside {
    position: absolute;
    right: 0;
    width: $aside-width;
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
