<template>
    <div>
        <h1>Upload Hi-C Map</h1>

        <FileLoaderComponent :extensions="['xml', 'json']" @loaded="loaded" />

        <UploadHiCMapFormComponent upload-id="uploadId" @submit="upload" />
    </div>
</template>

<script setup lang="ts">
import UploadHiCMapFormComponent from '@/components/forms/UploadHiCMapFormComponent.vue';
import FileLoaderComponent from '@/components/FileLoaderComponent.vue';
import { type Ref, ref } from 'vue';
import { publishHiCMap } from '@/core/server-requests';
import type { HiCCreationForm, HiCMap } from '@/core/types';
import { notify } from '@/core/config';
import $ from 'jquery';

const file: Ref<File | undefined> = ref(undefined);

const emit = defineEmits<{
    (e: 'uploaded'): void;
}>();

function loaded(uploaded: File) {
    $('#uploadId').click();
    file.value = uploaded;
}

function upload(form: HiCCreationForm) {
    const fileValue = file.value;

    if (!fileValue) {
        return;
    }

    const formData = new FormData();
    formData.append('file', fileValue);
    formData.append(
        'form',
        new Blob([JSON.stringify(form)], {
            type: 'application/json',
        })
    );

    publishHiCMap(formData, (hicMap: HiCMap) => {
        notify('info', `Uploaded: ${hicMap.meta.name}`);
        emit('uploaded');
    });
}
</script>

<style scoped lang="scss"></style>
