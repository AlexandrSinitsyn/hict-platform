<template>
    <div class="files-list">
        <h3>{{ type }} files:</h3>
        <div class="files-list">
            <AttachedFileInfo
                v-for="f in files"
                :key="f.name"
                :file="f"
                :type="type"
                :wrap="wrap"
            />
            <FileLoaderComponent :extensions="[FileType[type]]" :type="type" @loaded="doUpload" />
        </div>
    </div>
</template>

<script setup lang="ts">
import AttachedFileInfo from '@/components/AttachedFileInfo.vue';
import FileLoaderComponent from '@/components/FileLoaderComponent.vue';
import { type File as AttachedFile, FileType } from '@types';
import { uploadFile } from '@/core/files-requests';

const props = defineProps<{
    files: AttachedFile[];
    type: keyof typeof FileType;
    wrap: boolean;
}>();

const emit = defineEmits<{
    (e: 'upload', file: AttachedFile): void;
}>();

function doUpload(f: File): void {
    uploadFile(f, props.type, (attached: AttachedFile) => {
        emit('upload', attached);
    });
}
</script>

<style scoped lang="scss"></style>
