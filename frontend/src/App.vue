<template>
    <header>
        <img alt="Logo" class="logo" src="/public/img/icon.ico" width="125" height="125" />

        <h1 class="wrapper">{{ __NAME__ }}</h1>
    </header>

    <main>
        <div>Platform</div>
    </main>

    <footer>
        <p>
            &copy; Powered by <code>{{ __AUTHOR__ }}</code>
        </p>
        <i class="version">v{{ __VERSION__ }}</i>
    </footer>
</template>

<script setup lang="ts">
import { __VERSION__, __AUTHOR__, __NAME__ } from '@/core/config';
</script>

<style lang="scss">
$aside-size: 15vw;

header {
    position: fixed;
    width: $aside-size;
    padding: 1rem;
    text-align: center;
    height: calc(100% - 2rem);
}

main {
    margin: 1rem 1rem 0 calc(1rem + $aside-size);
    padding: 1rem;
    min-height: calc(100vh - 4rem);
    border-left: 1px solid gray;
}

footer {
    position: fixed;
    left: 0;
    bottom: 0;
    width: $aside-size;
    margin: 1rem;
    padding: 1rem;
    border-top: 1px solid gray;
    text-align: center;
    font-size: 0.8rem;

    p {
        margin: 0;
    }
}
</style>
