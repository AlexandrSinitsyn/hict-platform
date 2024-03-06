<template>
    <div
        class="modal fade"
        :id="id"
        tabindex="-1"
        role="dialog"
        :aria-labelledby="id + 'Label'"
        aria-hidden="true"
    >
        <div class="modal-dialog" role="document">
            <div class="modal-content">
                <div class="modal-header">
                    <h5 class="modal-title" :id="id + 'Label'">{{ title }}</h5>
                    <button
                        :id="id + 'Close'"
                        type="button"
                        class="btn-close"
                        data-bs-dismiss="modal"
                        aria-label="Close"
                    ></button>
                </div>
                <div class="modal-body">
                    <slot />
                </div>
                <div class="modal-footer">
                    <button
                        type="button"
                        class="btn btn-secondary"
                        data-dismiss="modal"
                        @click="$(`#${id}Close`).click()"
                    >
                        Close
                    </button>
                    <button type="button" class="btn btn-primary" @click="submit">Ok</button>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import $ from 'jquery';

const props = defineProps<{
    title: string;
    id: string;
}>();

const emit = defineEmits<{
    (e: 'submit'): void;
}>();

function submit() {
    emit('submit');
    $(`#${props.id}Close`).click();
}
</script>

<style scoped lang="scss"></style>
