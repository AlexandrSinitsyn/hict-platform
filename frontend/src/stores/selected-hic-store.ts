import { ref, type Ref } from 'vue';
import { defineStore } from 'pinia';
import type { HiCMap } from '@types';

export const useSelectedHiCStore = defineStore('selected-hic', () => {
    const selected: Ref<HiCMap | undefined> = ref(undefined);

    function select(hic: HiCMap) {
        selected.value = hic;
    }

    return { selected, select };
});
