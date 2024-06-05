<template>
    <div :class="'attached-file ' + type">
        <div v-if="!wrap" class="attached-file-info">
            <div>
                {{ file.name }}
            </div>
            <div>
                {{ file.sequenceLevel }}
            </div>
            <div>
                {{ human(file.filesize) }}
            </div>
            <div class="attached-file-creation-time">
                {{ file.creationTime.toLocaleString() }}
            </div>
        </div>
        <div v-else class="attached-file-info">
            {{ file.name }} # {{ file.creationTime.toLocaleString() }}
        </div>

        <div class="attached-file-delete">
            <span class="bi bi-x-lg"></span>
        </div>
    </div>
</template>

<script setup lang="ts">
import { type File, FileType } from '@types';

const props = defineProps<{
    file: File;
    type: keyof typeof FileType;
    wrap: boolean;
}>();

function human(bytes: number): string {
    if (bytes === 0) {
        return '0 Bytes';
    }

    const unit = 1024;
    const sizes: string[] = ['Bytes', 'KB', 'MB', 'GB', 'TB', 'PB', 'EB', 'ZB', 'YB'];

    const i = Math.floor(Math.log(bytes) / Math.log(unit));

    return parseFloat((bytes / Math.pow(unit, i)).toFixed(2)) + ' ' + sizes[i];
}
</script>

<style scoped lang="scss"></style>
