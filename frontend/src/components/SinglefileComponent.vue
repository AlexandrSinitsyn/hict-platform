<template>
    <div class="files-list">
        <h3>{{ type }} file:</h3>
        <div v-if="file">
            <AttachedFileInfo :file="file" :type="type" :wrap="wrap" />
        </div>
        <div v-else>
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
    file: AttachedFile | undefined;
    type: keyof typeof FileType;
    wrap: boolean;
}>();

const emit = defineEmits<{
    (e: 'upload', file: AttachedFile): void;
    // (e: 'remove', file: File): void;
}>();

function doUpload(f: File): void {
    uploadFile(f, props.type, (attached: AttachedFile) => {
        emit('upload', attached);
    });
}
</script>

<style scoped lang="scss"></style>
