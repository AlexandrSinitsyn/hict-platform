import { createRouter, createWebHistory } from 'vue-router';
import HomeView from '@/view/HomeView.vue';
import UploadView from '@/view/UploadView.vue';
import DatabaseView from '@/view/DatabaseView.vue';

const router = createRouter({
    history: createWebHistory(import.meta.env.BASE_URL),
    routes: [
        {
            path: '/',
            name: 'index',
            component: HomeView,
        },
        {
            path: '/home',
            name: 'home',
            component: HomeView,
        },
        {
            path: '/database',
            name: 'database',
            component: DatabaseView,
        },
        {
            path: '/upload',
            name: 'upload',
            component: UploadView,
        },
        {
            path: '/account',
            name: 'account',
            component: () => import('@/view/AccountView.vue'),
        },
        {
            path: '/admin',
            name: 'admin',
            component: () => import('@/view/AdminView.vue'),
        },
    ],
});

export default router;
