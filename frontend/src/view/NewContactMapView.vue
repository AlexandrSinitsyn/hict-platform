<template>
    <h1 v-if="!experiment">No experiment selected!</h1>
    <div v-else class="contact-map">
        <h1 class="contact-map-title">New contact-map ({{ experiment?.name }})</h1>

        <div class="contact-map-info">
            <div class="contact-map-info-name">
                <label for="contact-map-name">Name:</label>
                <input id="contact-map-name" type="text" v-model="name" />
                <div class="btn btn-info" @click="updateName">Reset</div>
            </div>

            <textarea
                class="contact-map-info-description"
                placeholder="Description"
                v-model="description"
            ></textarea>

            <div class="contact-map-info-name">
                <label for="contact-map-link">Link:</label>
                <input id="contact-map-link" type="text" v-model="link" />
            </div>

            <div class="btn btn-info" style="width: 20%; margin-left: auto" @click="updateInfo">
                Submit
            </div>
        </div>
        <input type="checkbox" checked v-model="fullfileinfo" />
        <label style="padding-left: 0.5rem">full info</label>
        <div class="contact-map-attached">
            <SinglefileComponent
                :file="hic"
                type="HI-C"
                :wrap="!fullfileinfo"
                @upload="(f: File) => hic = f"
            />
            <FileListComponent
                :files="agp"
                type="AGP"
                :wrap="!fullfileinfo"
                @upload="(f: File) => agp.push(f)"
            />
            <SinglefileComponent
                :file="mcool"
                type="MCOOL"
                :wrap="!fullfileinfo"
                @upload="(f: File) => mcool = f"
            />
            <FileListComponent
                :files="tracks"
                type="TRACKS"
                :wrap="!fullfileinfo"
                @upload="(f: File) => tracks.push(f)"
            />
        </div>
    </div>
</template>

<script setup lang="ts">
import { ref, type Ref } from 'vue';
import type { Experiment, ContactMap, File } from '@types';
import FileListComponent from '@/components/FileListComponent.vue';
import SinglefileComponent from '@/components/SinglefileComponent.vue';
import { updateContactMapInfo, updateContactMapName } from '@/core/server-requests';

const props = defineProps<{
    experiment: Experiment | undefined;
    selected: ContactMap | undefined;
}>();

const fullfileinfo: Ref<boolean> = ref(true);
const name: Ref<string> = ref(props.selected?.name ?? '');
const description: Ref<string | undefined> = ref(props.selected?.description ?? '');
const link: Ref<string | undefined> = ref(props.selected?.link ?? '');
const hic: Ref<File | undefined> = ref(props.selected?.hic);
const agp: Ref<File[]> = ref(props.selected?.agp ?? []);
const mcool: Ref<File | undefined> = ref(props.selected?.mcool);
const tracks: Ref<File[]> = ref(props.selected?.tracks ?? []);

function updateName(): void {
    const contactMap = props.selected;

    if (!contactMap) {
        return;
    }

    updateContactMapName(contactMap, {
        name: name.value,
    });
}

function updateInfo(): void {
    const contactMap = props.selected;

    if (!contactMap) {
        return;
    }

    updateContactMapInfo(contactMap, {
        description: description.value,
        link: link.value,
    });
}
</script>

<style scoped lang="scss">
@import '/public/css/main';
</style>