<template>
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

        <template #header-username="header">
            <div class="input-group flex-nowrap" style="padding: 1rem">
                <span class="input-group-text" id="addon-wrapping">{{ header.text }}</span>
                <input
                    type="text"
                    class="form-control"
                    placeholder="username"
                    aria-label="Username"
                    aria-describedby="addon-wrapping"
                    v-model="usernameCriteria"
                />
            </div>
        </template>
        <template #header-login="header">
            <div class="input-group flex-nowrap" style="padding: 1rem">
                <span class="input-group-text" id="addon-wrapping">{{ header.text }}</span>
                <input
                    type="text"
                    class="form-control"
                    placeholder="login"
                    aria-label="login"
                    aria-describedby="addon-wrapping"
                    v-model="loginCriteria"
                />
            </div>
        </template>
        <template #header-email="header">
            <div class="input-group flex-nowrap" style="padding: 1rem">
                <span class="input-group-text" id="addon-wrapping">{{ header.text }}</span>
            </div>
        </template>

        <template #expand="item">
            <div class="badge bg-primary text-wrap fs-6" style="width: 6rem; margin: 1rem">
                Groups:
            </div>
            <ul class="list-group list-group-numbered" style="margin: 0 1rem 1rem 1rem">
                <li v-for="g in item.groups" :key="g.name" class="list-group-item">{{ g.name }}</li>
            </ul>
        </template>
    </Vue3EasyDataTable>
</template>

<script setup lang="ts">
import { type User } from '@types';
import { computed, onMounted, type Ref, ref } from 'vue';
import { getAllUsers } from '@/core/user-account-requests';
import Vue3EasyDataTable, { type FilterOption, type Header, type Item } from 'vue3-easy-data-table';

const allUsers: Ref<User[]> = ref([]);

onMounted(() => {
    getAllUsers((us) => {
        allUsers.value = us;
    });
});

const loading: Ref<boolean> = ref(true);

const headers: Header[] = [
    { text: 'Username', value: 'username' },
    { text: 'Login', value: 'login' },
    { text: 'Email', value: 'email' },
];

function items(): Item[] {
    return allUsers.value.map(
        ({ username, login, email, groups }) => ({ username, login, email, groups } as Item)
    );
}

const usernameCriteria: Ref<string> = ref('');
const loginCriteria: Ref<string> = ref('');

const filterOptions = computed((): FilterOption[] => {
    const filterOptionsArray: FilterOption[] = [];
    if (usernameCriteria.value !== '') {
        filterOptionsArray.push({
            field: 'username',
            comparison: (value, criteria): boolean =>
                value != null && criteria != null && value.includes(criteria),
            criteria: usernameCriteria.value,
        });
    }
    if (loginCriteria.value !== '') {
        filterOptionsArray.push({
            field: 'login',
            comparison: (value, criteria): boolean =>
                value != null && criteria != null && value.includes(criteria),
            criteria: loginCriteria.value,
        });
    }
    return filterOptionsArray;
});
</script>

<style scoped lang="scss"></style>
