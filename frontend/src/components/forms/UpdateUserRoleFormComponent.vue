<template>
    <div>
        <button
            id="updateRoleForm"
            type="button"
            class="btn btn-primary"
            data-bs-toggle="modal"
            data-bs-target="#updateUserRoleFormModal"
        >
            Update user role
        </button>
        <AbstractFormComponent id="updateUserRoleFormModal" title="Update user role" @submit="submit">
            <div class="update">
                <span class="username-label">Username</span>
                <span class="username">{{ user.username }}</span>
                <span class="role-label">New role</span>
                <select
                    class="role form-select"
                    aria-label="Default select example"
                    v-model="newRole"
                >
                    <option v-for="r in roleNames" :key="r" :value="r">{{ r }}</option>
                </select>
            </div>
        </AbstractFormComponent>
    </div>
</template>

<script setup lang="ts">
import AbstractFormComponent from '@/components/forms/AbstractFormComponent.vue';
import { type Ref, ref } from 'vue';
import { type User, Role } from '@/core/types';
import { roleNames } from '@/core/extensions';

const props = defineProps<{
    user: User;
}>();

const emit = defineEmits<{
    (e: 'submit', acceptor: User, newRole: Role): void;
}>();

const newRole: Ref<Role> = ref(props.user.role);

function submit() {
    emit('submit', props.user, newRole.value);
}
</script>

<style scoped lang="scss">
.update {
    display: grid;
    grid-template-areas:
        'username-label username'
        'role-label role';
    grid-row-gap: 0.5rem;

    @each $area in 'username-label' 'username' 'role-label' 'role' {
        & > .#{$area}-label {
            grid-area: unquote($area)-label;
            text-align: right;
            margin-right: 1rem;

            &::after {
                content: ':';
            }
        }

        & > .#{$area} {
            grid-area: unquote($area);
        }
    }
}
</style>
