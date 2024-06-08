<template>
    <button
        type="button"
        class="btn btn-primary"
        data-bs-toggle="modal"
        data-bs-target="#experimentCreationFormModal"
        style="color: white"
    >
        Create new
        <AbstractFormComponent
            v-if="user"
            id="experimentCreationFormModal"
            title="Experiment creation"
            @submit="submit"
        >
            <div class="experiment-creation">
                <span class="fs-4 mx-3">Group</span>
                <select class="form-select" aria-label=".form-select example">
                    <option value="public" selected>public</option>
                    <option v-for="group in user.groups" :key="group.name" :value="group.name">
                        {{ group.name }}
                    </option>
                </select>
            </div>
        </AbstractFormComponent>
    </button>
</template>

<script setup lang="ts">
import AbstractFormComponent from '@/components/forms/AbstractFormComponent.vue';
import { type Ref, ref } from 'vue';
import type { ExperimentCreationForm, Group } from '@types';
import { useAuthStore } from '@/stores/auth-store';
import { storeToRefs } from 'pinia';

const emit = defineEmits<{
    (e: 'submit', form: ExperimentCreationForm): void;
}>();

const group: Ref<Group> = ref({
    name: 'public',
});
const { user } = storeToRefs(useAuthStore());

function submit() {
    const groupValue = group.value;

    emit('submit', {
        groupName: groupValue.name,
    });
}
</script>

<style scoped lang="scss">
.experiment-creation {
    display: flex;
    justify-content: space-evenly;

    & > label {
        &::after {
            content: ':';
        }
    }
}
</style>
