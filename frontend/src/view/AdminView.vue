<template>
    <div v-if="isAtLeast(user?.role, Role.ADMIN)">
        <h1>Admin area</h1>

        <table class="table table-striped">
            <thead>
                <tr>
                    <th scope="col">ID</th>
                    <th scope="col">Username</th>
                    <th scope="col">Login</th>
                    <th scope="col">Email</th>
                    <th scope="col">Role</th>
                    <th scope="col"></th>
                </tr>
            </thead>
            <tbody>
                <tr v-for="u in allUsers" :key="u.id">
                    <th scope="row">{{ u.id }}</th>
                    <td>{{ u.username }}</td>
                    <td>{{ u.login }}</td>
                    <td>{{ u.email }}</td>
                    <td>{{ u.role }}</td>
                    <td>
                        <UpdateUserRoleFormComponent :user="u" @submit="setRole" />
                    </td>
                </tr>
            </tbody>
        </table>
    </div>
    <div v-else>You has no access!</div>
</template>

<script setup lang="ts">
import UpdateUserRoleFormComponent from '@/components/forms/UpdateUserRoleFormComponent.vue';
import { type User, Role } from '@types';
import { isAtLeast } from '@/core/extensions';
import { onMounted, type Ref, ref } from 'vue';
import { getAllUsers, updateUserRole } from '@/core/user-account-requests';
import { notify } from '@/core/config';
import { useAuthStore } from '@/stores/auth-store';
import { storeToRefs } from 'pinia';

const { user } = storeToRefs(useAuthStore());

const allUsers: Ref<User[]> = ref([]);

onMounted(() => {
    getAllUsers((us) => {
        allUsers.value = us;
    });
});

function setRole(acceptor: User, newRole: Role) {
    const me = user.value;

    if (!me) {
        notify('error', 'You should be authorized!');
        return;
    }

    if (!isAtLeast(me.role, Role.ADMIN)) {
        notify('error', 'You has no access!');
        return;
    }

    if (isAtLeast(acceptor.role, me.role)) {
        notify('error', "Can not change role to a higher than self' grade");
        return;
    }

    updateUserRole({
        id: acceptor.id,
        newRole: newRole,
    });
}
</script>

<style scoped lang="scss"></style>
