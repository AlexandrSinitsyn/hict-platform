<template>
    <div>
        <div style="display: flex; justify-content: space-between">
            <h1>Groups</h1>
            <CreationGroupFormComponent @submit="createGroup" />
        </div>

        <Vue3EasyDataTable
            :headers="headers"
            :items="items()"
            :filter-options="filterOptions"
            :loading="loading"
        >
            <template #loading>
                <img
                    src="https://i.pinimg.com/originals/94/fd/2b/94fd2bf50097ade743220761f41693d5.gif"
                    alt="space-loading"
                    style="width: 150px"
                />
            </template>

            <template #header-member="header">
                <div class="form-check form-switch">
                    <input
                        class="form-check-input"
                        type="checkbox"
                        id="flexSwitchCheckChecked"
                        v-model="memberFilter"
                    />
                    <label class="form-check-label" for="flexSwitchCheckChecked">{{
                        header.text
                    }}</label>
                </div>
            </template>
            <template #header-name="header">
                <div class="input-group flex-nowrap" style="padding: 1rem">
                    <span class="input-group-text" id="addon-wrapping">{{ header.text }}</span>
                    <input
                        type="text"
                        class="form-control"
                        placeholder="name"
                        aria-label="Username"
                        aria-describedby="addon-wrapping"
                        v-model="nameCriteria"
                    />
                </div>
            </template>

            <template #item-member="item">
                <div v-if="item.member">
                    <button class="btn btn-primary">join</button>
                </div>
                <div>
                    <button class="btn btn-secondary">quit</button>
                </div>
            </template>
            <template #item-creationTime="item">
                {{ (item.creationTime as Date)?.toLocaleString() }}
            </template>
        </Vue3EasyDataTable>
    </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, type Ref } from 'vue';
import type { Group, GroupCreationForm } from '@types';
import { getAllGroups, joinGroup, publishGroup } from '@/core/user-account-requests';
import CreationGroupFormComponent from '@/components/forms/CreationGroupFormComponent.vue';
import Vue3EasyDataTable, { type FilterOption, type Header, type Item } from 'vue3-easy-data-table';
import { useAuthStore } from '@/stores/auth-store';

const { user } = useAuthStore();
const groups: Ref<Group[]> = ref([]);

onMounted(() => {
    getAllGroups((all: Group[]) => {
        groups.value = all;
        loading.value = false;
    });
});

function createGroup(groupForm: GroupCreationForm): void {
    publishGroup(groupForm, (group: Group) => {
        // joinGroup(group);
    });
}

const loading: Ref<boolean> = ref(true);

type RowType = {
    member: boolean;
    name: string;
    affiliation: string;
    creationTime: Date;
};

const headers: Header[] = [
    { text: 'Только в которых участвуете', value: 'member' },
    { text: 'Название группы', value: 'name' },
    { text: 'Описание', value: 'affiliation' },
    { text: 'Дата создания', value: 'creationTime' },
];

function items(): Item[] {
    if (!user) {
        return [];
    }

    return groups.value
        .map<RowType>(({ name, affiliation, creationTime }) => ({
            member: user.groups.map((g) => g.name).includes(name),
            name,
            affiliation: affiliation ?? '/no data/',
            creationTime,
        }))
        .map<Item>((x) => x);
}

const nameCriteria: Ref<string> = ref('');
const memberFilter: Ref<boolean> = ref(false);

const filterOptions = computed((): FilterOption[] => {
    const filterOptionsArray: FilterOption[] = [];
    if (memberFilter.value) {
        filterOptionsArray.push({
            field: 'member',
            comparison: '=',
            criteria: '*',
        });
    }
    if (nameCriteria.value !== '') {
        filterOptionsArray.push({
            field: 'name',
            comparison: (value, criteria): boolean =>
                value != null && criteria != null && value.includes(criteria),
            criteria: nameCriteria.value,
        });
    }
    return filterOptionsArray;
});
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
