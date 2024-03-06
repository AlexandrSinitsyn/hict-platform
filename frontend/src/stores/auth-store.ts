import { ref, type Ref } from 'vue';
import { defineStore } from 'pinia';
import type { User } from '@/core/types';

export const useAuthStore = defineStore('auth', () => {
    const user: Ref<User | undefined> = ref(undefined);

    function login(u: User) {
        user.value = u;
    }

    function logout() {
        user.value = undefined;
    }

    return { user, login, logout };
});
