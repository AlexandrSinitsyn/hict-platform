<template>
    <div class="files-list">
        <h3>{{ type }} files:</h3>
        <div class="files-list">
            <AttachedFileInfo
                v-for="f in files"
                :key="f.name"
                :file="f"
                :type="FileType[filetype]"
                :wrap="wrap"
            />
            <FileLoaderComponent
                :extensions="[filetype.toLowerCase()]"
                :type="FileType[filetype]"
                @loaded="doUpload"
            />
        </div>
    </div>
</template>

<script setup lang="ts">
import AttachedFileInfo from '@/components/AttachedFileInfo.vue';
import FileLoaderComponent from '@/components/FileLoaderComponent.vue';
import { type File as AttachedFile, FileType } from '@types';
import { uploadFile } from '@/core/server-requests';

const props = defineProps<{
    files: AttachedFile[];
    type: string;
    wrap: boolean;
}>();

const emit = defineEmits<{
    (e: 'upload', file: File): void;
}>();

const filetype = props.type?.trim().replace('-', '');

function doUpload(f: File): void {
    uploadFile(f, FileType[filetype], (attached: AttachedFile) => {
        emit('upload', attached);
    });
}
</script>

<style scoped lang="scss"></style>
