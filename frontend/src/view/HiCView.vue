<template>
    <div>
        <h1>Hi-C Map view</h1>

        <div v-if="selected">
            <p>Name: {{ selected.meta.name }}</p>
            <p>Description: {{ selected.meta.description }}</p>
            <p>Created at: {{ selected.meta.creationTime }}</p>
            <p>Views: {{ selected.views }}</p>
        </div>
        <div v-else>You have not selected any map yet!</div>
    </div>
</template>

<script setup lang="ts">
import { useSelectedHiCStore } from '@/stores/selected-hic-store';
import { storeToRefs } from 'pinia';

const { selected } = storeToRefs(useSelectedHiCStore());
</script>

<style scoped lang="scss"></style>
