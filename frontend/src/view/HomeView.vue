<template>
    <div v-if="!selectedExperiment">
        <h1>Experiments</h1>

        <div style="display: flex; justify-content: space-between; margin: 3rem">
            <div class="experiments-filter">
                <label for="experiments-filter">Filter:</label>
                <input
                    id="experiments-filter"
                    type="text"
                    placeholder="name"
                    v-model="nameFilter"
                />
            </div>
            <div class="btn btn-success" style="color: white" @click="newExperiment">
                Create new
            </div>
        </div>

        <div class="experiments">
            <div v-if="filteredExperiments.length === 0">No experiments found</div>
            <div v-else v-for="e in filteredExperiments" :key="e.name">
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
import { onMounted, ref, type Ref, watch } from 'vue';
import type { ContactMap, Experiment } from '@types';
import { getAllExperiments, publishExperiment } from '@/core/experiment-requests';
import NewExperimentView from '@/view/NewExperimentView.vue';
import router from '@/router';

const experiments: Ref<Experiment[]> = ref([]);
const selectedExperiment: Ref<Experiment | undefined> = ref(undefined);
const nameFilter: Ref<string> = ref('');
const filteredExperiments: Ref<Experiment[]> = ref(experiments.value);

watch(
    () => nameFilter.value,
    (value) => {
        if (!value) {
            filteredExperiments.value = experiments.value;
            return;
        }

        filteredExperiments.value = experiments.value
            .map<Experiment | undefined>((e) => {
                if (e.name.includes(value)) {
                    return e;
                }

                const maps = e.contactMaps.filter(({ name }) => name.includes(value));
                const assemblies = e.assemblies.filter(({ name }) => name.includes(value));

                if (maps.length === 0 && assemblies.length === 0) {
                    return undefined;
                }

                return {
                    ...e,
                    contactMaps: maps,
                    assemblies: assemblies,
                };
            })
            .filter((e) => !!e) as Experiment[];
    }
);

onMounted(() => {
    getAllExperiments((all: Experiment[]) => {
        experiments.value = all;
        filteredExperiments.value = all;
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

    @each $type,
        $color in ('view-experiment': $danger, 'view-contact-map': $success, 'view-assembly': $info)
    {
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
    width: 90%;

    & > label {
        margin-right: 1rem;
    }

    & > input {
        width: 80%;
        padding: 0.5rem;
        border: 1px solid $border-color;
        border-radius: $border-radius;
        resize: none;
    }
}
</style>
