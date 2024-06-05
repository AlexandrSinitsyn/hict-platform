<template>
    <div>
        <input
            id="fileLoader"
            type="file"
            :class="'file-loader ' + type"
            required
            @change="fileLoaded($event)"
        />
        <label for="fileLoader">
            Drag and drop here one of [{{ props.extensions.map((e) => `*.${e}`).join(', ') }}]
        </label>
    </div>
</template>

<script setup lang="ts">
import { ref, type Ref } from 'vue';
import { notify } from '@/core/config';
import { FileType } from '@types';

const props = defineProps<{
    extensions: string[];
    type: keyof typeof FileType;
}>();

const emit = defineEmits<{
    (e: 'loaded', file: File): void;
}>();

const filename: Ref<string> = ref('Choose file...');

function fileLoaded(e: Event) {
    const input = (e.target as HTMLInputElement).files;

    if (!input) {
        return;
    }

    const file: File = input[0];

    if (!file) {
        return;
    }

    if (!props.extensions?.find((ext) => file.name.endsWith(ext))) {
        notify('error', `Unsupported file type for "${file.name}"`);
        return;
    }

    filename.value = file.name;

    emit('loaded', file);

    notify('success', `File ${file.name} loaded`);
}
</script>

<style scoped lang="scss"></style>
