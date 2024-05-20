<template>
    <div v-if="!selectedContactMap" class="experiment">
        <h1 class="experiment-title">New experiment</h1>

        <div class="experiment-info">
            <div class="experiment-info-name">
                <label for="experiment-name">Name:</label>
                <input id="experiment-name" type="text" v-model="name" />
                <div class="btn btn-info" @click="updateName">Reset</div>
            </div>

            <textarea
                class="experiment-info-description"
                placeholder="Description"
                v-model="description"
            ></textarea>

            <div class="experiment-info-name">
                <label for="experiment-link">Link:</label>
                <input id="experiment-link" type="text" v-model="link" />
            </div>

            <div class="experiment-info-name">
                <label for="experiment-acknowledgement">Acknowledgement:</label>
                <input id="experiment-acknowledgement" type="text" v-model="acknowledgement" />
            </div>

            <div class="btn btn-info" style="width: 20%; margin-left: auto" @click="updateInfo">
                Submit
            </div>
        </div>
        <div class="experiment-files">
            <FileListComponent
                :files="fasta"
                type="FASTA"
                :wrap="!fullfasta"
                @upload="(f: File) => fasta.push(f)"
            />
        </div>
        <div class="experiment-activity">
            <div
                v-for="{ type, name, creationTime } in activities()"
                :key="name"
                :class="'experiment-activity-' + type"
            >
                <div>
                    {{ name }}
                </div>
                <div>
                    {{ creationTime.toLocaleString() }}
                </div>
            </div>
            <div class="experiment-activity-new">
                <div class="btn btn-success" style="width: 40%" @click="newContactMap">
                    New contact map
                </div>
                <div class="btn btn-info" style="width: 40%">New assembly</div>
            </div>
        </div>
    </div>
    <NewContactMapView
        v-if="selectedContactMap"
        :experiment="selected"
        :selected="selectedContactMap"
    />
</template>

<script setup lang="ts">
import { ref, type Ref } from 'vue';
import type { Experiment, ContactMap, File, Assembly } from '@types';
import FileListComponent from '@/components/FileListComponent.vue';
import NewContactMapView from '@/view/NewContactMapView.vue';
import {
    publishContactMap,
    updateExperimentInfo,
    updateExperimentName,
} from '@/core/server-requests';

const props = defineProps<{
    selected: Experiment | undefined;
}>();

const selectedContactMap: Ref<ContactMap | undefined> = ref(undefined);

const fullfasta: Ref<boolean> = ref(true);
const name: Ref<string> = ref(props.selected?.name ?? '');
const description: Ref<string | undefined> = ref(props.selected?.description ?? undefined);
const link: Ref<string | undefined> = ref(props.selected?.link ?? undefined);
const acknowledgement: Ref<string | undefined> = ref(props.selected?.acknowledgement ?? undefined);
const contactMaps: Ref<ContactMap[]> = ref(props.selected?.contactMaps ?? []);
const assemblies: Ref<Assembly[]> = ref(props.selected?.assemblies ?? []);
const fasta: Ref<File[]> = ref(props.selected?.fasta ?? []);

type Activity = {
    type: 'contactMap' | 'assembly';
    name: string;
    creationTime: Date;
};

function activities(): Activity[] {
    function getInfo(
        type: 'contactMap' | 'assembly'
    ): (element: ContactMap | Assembly) => Activity {
        return function <T extends { name: string; creationTime: Date }>(element: T): Activity {
            return {
                type: type,
                name: element.name,
                creationTime: element.creationTime,
            };
        };
    }

    return contactMaps.value
        .map(getInfo('contactMap'))
        .concat(assemblies.value.map(getInfo('assembly')))
        .sort((a, b) => b.creationTime.getTime() - a.creationTime.getTime());
}

function updateName(): void {
    const experiment = props.selected;

    if (!experiment) {
        return;
    }

    updateExperimentName(experiment, {
        name: name.value,
    });
}

function updateInfo(): void {
    const experiment = props.selected;

    if (!experiment) {
        return;
    }

    updateExperimentInfo(experiment, {
        description: description.value,
        link: link.value,
        acknowledgement: acknowledgement.value,
    });
}

function newContactMap(): void {
    const experiment = props.selected;

    if (!experiment) {
        return;
    }

    publishContactMap(experiment, (contactMap: ContactMap) => {
        selectedContactMap.value = contactMap;
    });
}
</script>

<style scoped lang="scss"></style>
