import { createRouter, createWebHistory } from 'vue-router';
import HomeView from '@/view/HomeView.vue';
import GroupsView from '@/view/GroupsView.vue';
import ExperimentsView from '@/view/ExperimentsView.vue';

const router = createRouter({
    history: createWebHistory(import.meta.env.BASE_URL),
    routes: [
        {
            path: import.meta.env.BASE_URL + '/',
            name: 'index',
            component: HomeView,
        },
        {
            path: import.meta.env.BASE_URL + '/home',
            name: 'home',
            component: HomeView,
        },
        {
            path: import.meta.env.BASE_URL + '/groups',
            name: 'groups',
            component: GroupsView,
        },
        {
            path: import.meta.env.BASE_URL + '/experiments',
            name: 'experiments',
            component: ExperimentsView,
        },
        {
            path: import.meta.env.BASE_URL + '/view/:contactMapName',
            name: 'view',
            sensitive: true,
            component: () => import('@/view/ContactMapView.vue'),
        },
        {
            path: import.meta.env.BASE_URL + '/account',
            name: 'account',
            component: () => import('@/view/AccountView.vue'),
        },
        {
            path: import.meta.env.BASE_URL + '/admin',
            name: 'admin',
            component: () => import('@/view/AdminView.vue'),
        },
    ],
});

export default router;
