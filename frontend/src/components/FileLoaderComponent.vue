<template>
    <div>
        <label for="fileLoader"
            >Drag and drop here one of [{{
                props.extensions.map((e) => `*.${e}`).join(', ')
            }}]</label
        >
        <br />
        <input
            id="fileLoader"
            type="file"
            class="file-loader"
            required
            @change="fileLoaded($event)"
        />
    </div>
</template>

<script setup lang="ts">
import { ref, type Ref } from 'vue';
import { notify } from '@/core/config';

const props = defineProps<{
    extensions: string[];
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

    const ext = (() => {
        const tmp = file.name.split(/\./);
        return tmp[tmp.length - 1];
    })();

    if (!props.extensions.includes(ext)) {
        notify('error', `Unsupported file type for "${file.name}"`);
        return;
    }

    filename.value = file.name;

    emit('loaded', file);

    notify('success', `File ${file.name} loaded`);
}
</script>

<style scoped lang="scss">
@import 'public/css/main';

.file-loader {
    padding: 3rem;
    text-align: center;
    border: 2px dashed $border-color;
    background-color: opacity($border-color, 0.3);
}
</style>
