<template>
    <div>
        <button
            type="button"
            class="btn btn-primary"
            data-bs-toggle="modal"
            data-bs-target="#groupCreationFormModal"
        >
            Create new
        </button>
        <AbstractFormComponent id="groupCreationFormModal" title="Group creation" @submit="submit">
            <div class="group-creation">
                <span class="group-name-label">Group name</span>
                <input class="group-name" type="text" v-model="groupName" />
            </div>
        </AbstractFormComponent>
    </div>
</template>

<script setup lang="ts">
import AbstractFormComponent from '@/components/forms/AbstractFormComponent.vue';
import { type Ref, ref } from 'vue';
import type { GroupCreationForm } from '@types';
import { notify } from '@/core/config';

const emit = defineEmits<{
    (e: 'submit', form: GroupCreationForm): void;
}>();

const groupName: Ref<string> = ref('');

function submit() {
    const groupNameValue = groupName.value;

    if (!groupNameValue) {
        notify('warning', 'Name should not be an empty field');
        return;
    }

    emit('submit', {
        name: groupNameValue,
    });
}
</script>

<style scoped lang="scss">
.group-creation {
    display: grid;
    grid-template-areas: 'group-name-label group-name';
    grid-row-gap: 0.5rem;

    @each $area in 'group-name' {
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
