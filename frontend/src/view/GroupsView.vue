<template>
    <div>
        <h1>Groups</h1>

        <div class="groups-filter">
            <label for="groups-filter">Filter:</label>
            <input id="groups-filter" type="text" />
            <CreationGroupFormComponent @submit="createGroup" />
        </div>

        <div class="groups">
            <div v-for="g in groups" :key="g.name" @click="joinGroup(g)">
                {{ g.name }}
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import { onMounted, ref, type Ref } from 'vue';
import type { Group, GroupCreationForm } from '@types';
import { getAllGroups, joinGroup, publishGroup } from '@/core/user-account-requests';
import CreationGroupFormComponent from '@/components/forms/CreationGroupFormComponent.vue';

const groups: Ref<Group[]> = ref([]);

onMounted(() => {
    getAllGroups((all: Group[]) => {
        groups.value = all;
    });
});

function createGroup(groupForm: GroupCreationForm): void {
    publishGroup(groupForm, (group: Group) => {
        joinGroup(group);
    });
}
</script>

<style scoped lang="scss">
@import '/public/css/main';

.groups {
    padding: 1rem;
    border: 1px solid $border-color;
    border-radius: $border-radius;
}

.groups-filter {
    margin: 3rem;
    display: flex;
    justify-content: space-between;
    flex-direction: row;

    & > label {
        margin: auto 0;
    }

    & > input {
        width: 70%;
        padding: 0.5rem;
        border: 1px solid $border-color;
        border-radius: $border-radius;
        resize: none;
    }
}
</style>
