<template>
    <div>
        <h1>Hi-C Maps database</h1>

        <div class="database">
            <div class="hic" v-for="hic in hicMaps" :key="hic.id" @click="emit('selected', hic)">
                {{ hic.meta.name }}
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import { onMounted, type Ref, ref } from 'vue';
import { getAllHiCMaps } from '@/core/server-requests';
import type { HiCMap } from '@/core/types';

const emit = defineEmits<{
    (e: 'selected', map: HiCMap): void;
}>();

const hicMaps: Ref<HiCMap[]> = ref([]);

onMounted(() => {
    getAllHiCMaps((lst: HiCMap[]) => {
        hicMaps.value = lst;
    });
});
</script>

<style scoped lang="scss">
@import '/public/css/main';

.database {
    padding: 1rem;
    border: 1px solid $border-color;
    border-radius: $border-radius;
}
</style>
