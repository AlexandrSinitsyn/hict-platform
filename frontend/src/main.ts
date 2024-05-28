import '/public/css/normalize.css';
import '/public/css/main.scss';
import 'jquery/dist/jquery.slim';
import 'vue3-toastify/dist/index.css';
import 'bootstrap/dist/css/bootstrap.min.css';
import 'bootstrap';
import 'bootstrap-icons/font/bootstrap-icons.css';

import { createApp } from 'vue';
import { createPinia } from 'pinia';
import App from '@/App.vue';
import router from '@/router';
import axios, { type AxiosError, type AxiosResponse } from 'axios';
import { env, notify } from '@/core/config';

const app = createApp(App);

app.use(createPinia());
app.use(router);

axios
    .get<never, AxiosResponse<Record<string, string>>>('/assets/env.json')
    .then((response: AxiosResponse<Record<string, string>>): void => {
        env.value = response.data;

        app.mount('#app');
    })
    .catch((error: AxiosError) => {
        notify('error', 'Internal server error!');
        console.log('Internal server error!', error);
    });
