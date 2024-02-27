<template>
    <div>
        <button
            :id="uploadId"
            type="button"
            class="btn btn-primary"
            data-bs-toggle="modal"
            data-bs-target="#uploadFormModal"
            hidden
        >
            Upload
        </button>
        <AbstractForm id="uploadFormModal" title="Upload Hi-C Maps" @submit="submit">
            <div class="upload">
                <span class="name-label">Name</span>
                <input class="name" type="text" v-model="name" />
                <span class="description-label">Description</span>
                <input class="description" type="text" v-model="description" />
            </div>
        </AbstractForm>
    </div>
</template>

<script setup lang="ts">
import AbstractForm from '@/components/forms/AbstractForm.vue';
import { type Ref, ref } from 'vue';
import { type HiCCreationForm } from '@/core/types';
import { notify } from '@/core/config';

const props = defineProps<{
    uploadId: string;
}>();

const emit = defineEmits<{
    (e: 'submit', form: HiCCreationForm): void;
}>();

const name: Ref<string> = ref('');
const description: Ref<string> = ref('');

function submit() {
    const nameValue = name.value;
    const descriptionValue = description.value;

    if (!nameValue || !descriptionValue) {
        notify('warning', 'Name and description should not be an empty fields');
        return;
    }

    emit('submit', {
        name: nameValue,
        description: descriptionValue,
    });
}
</script>

<style scoped lang="scss">
.upload {
    display: grid;
    grid-template-areas:
        'name-label name'
        'description-label description';
    grid-row-gap: 0.5rem;

    @each $area in 'name-label' 'name' 'description-label' 'description' {
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
