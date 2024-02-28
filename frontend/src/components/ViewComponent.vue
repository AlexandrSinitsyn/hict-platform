<template>
    <div>
        <h1>Hi-C Map view</h1>

        <div v-if="map">
            <p>{{ map.meta.name }}: {{ map.meta.description }}</p>
            <p>Created at: {{ map.meta.creationTime }}</p>
        </div>
        <div v-else>You have not selected any map yet!</div>
    </div>
</template>

<script setup lang="ts">
import type { HiCMap } from '@/core/types';

const props = defineProps<{
    map: HiCMap | undefined;
}>();
</script>

<style scoped lang="scss"></style>
