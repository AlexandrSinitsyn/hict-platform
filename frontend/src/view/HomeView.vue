<template>
    <div v-if="!selectedExperiment">
        <h1>{{ __NAME__ }}</h1>
        <h3>Experiments</h3>

        <div class="experiments-filter">
            <label for="experiments-filter">Filter:</label>
            <input id="experiments-filter" type="text" />
            <div class="btn btn-outline-primary" @click="newExperiment">Create new</div>
        </div>

        <div class="experiments">
            <div v-for="e in experiments" :key="e.name">
                <div @click="selectedExperiment = e" class="view-experiment">
                    [Experiment] {{ e.name }}
                </div>
                <div
                    v-for="cm in e.contactMaps"
                    :key="cm.name"
                    @click="selectMap(cm)"
                    class="view-contact-map"
                    style="margin-left: 2rem"
                >
                    [Contact map] {{ cm.name }}
                </div>
                <div v-for="a in e.assemblies" :key="a.name" class="view-assembly">
                    [Assembly] {{ a.name }}
                </div>
            </div>
        </div>
    </div>
    <NewExperimentView v-if="selectedExperiment" :selected="selectedExperiment" />
</template>

<script setup lang="ts">
import { onMounted, ref, type Ref } from 'vue';
import type { ContactMap, Experiment } from '@types';
import { getAllExperiments, publishExperiment } from '@/core/experiment-requests';
import NewExperimentView from '@/view/NewExperimentView.vue';
import router from '@/router';
import { __NAME__ } from '@/core/config';

const experiments: Ref<Experiment[]> = ref([]);
const selectedExperiment: Ref<Experiment | undefined> = ref(undefined);

onMounted(() => {
    getAllExperiments((all: Experiment[]) => {
        experiments.value = all;
    });
});

function newExperiment() {
    publishExperiment((experiment: Experiment) => {
        selectedExperiment.value = experiment;
    });
}

function selectMap(map: ContactMap): void {
    router.push({
        name: 'view',
        params: {
            contactMapName: map.name,
        },
    });
}
</script>

<style scoped lang="scss">
@import '/public/css/main';

.experiments {
    padding: 1rem;
    border: 1px solid $border-color;
    border-radius: $border-radius;

    @each $type, $color in ('view-experiment': $danger, 'view-contact-map': $success, 'view-assembly': $info) {
        .#{$type} {
            padding: 0.5rem;
            color: $color;
            font-weight: bolder;

            &:hover {
                background-color: opacity($color, 0.2);
            }
        }
    }
}

.experiments-filter {
    margin: 3rem;
    display: flex;
    justify-content: space-between;
    flex-direction: row;

    & > label {
        margin: auto 0;
    }

    & > input {
        width: 70%;
        padding: 0.5rem;
        border: 1px solid $border-color;
        border-radius: $border-radius;
        resize: none;
    }
}
</style>
