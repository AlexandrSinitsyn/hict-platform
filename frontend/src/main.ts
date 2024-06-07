import '/public/css/normalize.css';
import '/public/css/main.scss';
import 'jquery/dist/jquery.slim';
import 'vue3-toastify/dist/index.css';
import 'vue3-easy-data-table/dist/style.css';
import 'bootstrap/dist/css/bootstrap.min.css';
import 'bootstrap';
import 'bootstrap-icons/font/bootstrap-icons.css';

import { createApp } from 'vue';
import { createPinia } from 'pinia';
import App from '@/App.vue';
import router from '@/router';

const app = createApp(App);

app.use(createPinia());
app.use(router);

app.mount('#app');
