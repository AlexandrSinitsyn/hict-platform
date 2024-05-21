import { createRouter, createWebHistory } from 'vue-router';
import HomeView from '@/view/HomeView.vue';
import GroupsView from '@/view/GroupsView.vue';
import ExperimentsView from '@/view/ExperimentsView.vue';

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
            path: '/groups',
            name: 'groups',
            component: GroupsView,
        },
        {
            path: '/experiments',
            name: 'experiments',
            component: ExperimentsView,
        },
        {
            path: '/view/:hiCMapName',
            name: 'view',
            sensitive: true,
            component: () => import('@/view/ContactMapView.vue'),
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
