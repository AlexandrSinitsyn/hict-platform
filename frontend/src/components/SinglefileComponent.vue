<template>
    <div class="files-list">
        <h3>{{ type }} file:</h3>
        <div v-if="file">
            <AttachedFileInfo :file="file" :type="FileType[filetype]" :wrap="wrap" />
        </div>
        <div v-else>
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
    file: AttachedFile;
    type: string;
    wrap: boolean;
}>();

const emit = defineEmits<{
    (e: 'upload', file: AttachedFile): void;
    // (e: 'remove', file: File): void;
}>();

const filetype = props.type?.trim().replace('-', '');

function doUpload(f: File): void {
    uploadFile(f, FileType[filetype], (attached: AttachedFile) => {
        emit('upload', attached);
    });
}
</script>

<style scoped lang="scss"></style>
