<template>
    <div>
        <h1>User account</h1>

        <form v-if="user" class="update-form">
            <div class="form-group row" v-for="[label, val, field] in fields" :key="label">
                <label :for="label + 'Account'" class="col-sm-1 col-form-label col-form-label">{{
                    label
                }}</label>
                <label :for="label + 'Account'" class="col-sm-1 col-form-label col-form-label">{{
                    val
                }}</label>
                <div class="col-sm-5">
                    <input
                        :type="label.toLowerCase()"
                        class="form-control"
                        :id="label + 'Account'"
                        :placeholder="label"
                        v-model="field.value"
                    />
                </div>
            </div>

            <div class="form-group row">
                <label class="col-sm-1 col-form-label col-form-label">Role</label>
                <label class="col-sm-1 col-form-label col-form-label">{{ user?.role }}</label>
            </div>

            <div class="form-group row">
                <label for="colFormLabelLg" class="col-sm-1 col-form-label col-form-label" />
                <div class="col-sm-5">
                    <div class="btn btn-primary" @click="updateInfo">Update</div>
                </div>
            </div>
        </form>

        <form v-if="user" class="update-form">
            <div class="form-group row">
                <label for="oldPasswordAccount" class="col-sm-1 col-form-label col-form-label"
                    >Old password</label
                >
                <div class="col-sm-5">
                    <input
                        type="password"
                        class="form-control"
                        id="oldPasswordAccount"
                        placeholder="password"
                        v-model="newPassword"
                    />
                </div>
            </div>
            <div class="form-group row">
                <label for="newPasswordAccount" class="col-sm-1 col-form-label col-form-label"
                    >New password</label
                >
                <div class="col-sm-5">
                    <input
                        type="password"
                        class="form-control"
                        id="newPasswordAccount"
                        placeholder="password"
                        v-model="oldPassword"
                    />
                </div>
            </div>

            <div class="form-group row">
                <label class="col-sm-1 col-form-label col-form-label" />
                <div class="col-sm-5">
                    <div class="btn btn-primary" @click="updatePassword">Update</div>
                </div>
            </div>
        </form>
        <div v-else>You are not authenticated!</div>
    </div>
</template>

<script setup lang="ts">
import { type Ref, ref } from 'vue';
import { notify } from '@/core/config';
import { updateUserInfo, updateUserPassword } from '@/core/user-account-requests';
import { useAuthStore } from '@/stores/auth-store';
import { storeToRefs } from 'pinia';

const { user } = storeToRefs(useAuthStore());

const username: Ref<string | undefined> = ref(undefined);
const login: Ref<string | undefined> = ref(undefined);
const email: Ref<string | undefined> = ref(undefined);
const oldPassword: Ref<string | undefined> = ref(undefined);
const newPassword: Ref<string | undefined> = ref(undefined);

const fields: [string, string | undefined, Ref<string | undefined>][] = [
    ['Username', user.value?.username, username],
    ['Login', user.value?.login, login],
    ['Email', user.value?.email, email],
];

function updateInfo() {
    const usernameValue = username.value;
    const loginValue = login.value;
    const emailValue = email.value;

    if (!usernameValue && !loginValue && !emailValue) {
        notify('warning', 'Username, login and email should not be an empty fields');
        return;
    }

    const valueOrNull = (x: string | null | undefined) => (!x || /^\s*$/.test(x) ? null : x);

    updateUserInfo({
        username: valueOrNull(usernameValue),
        login: valueOrNull(loginValue),
        email: valueOrNull(emailValue),
    });
}

function updatePassword() {
    const oldPasswordValue = oldPassword.value;
    const newPasswordValue = newPassword.value;

    if (!oldPasswordValue || !newPasswordValue) {
        notify('warning', 'OldPassword and newPassword should not be an empty fields');
        return;
    }

    updateUserPassword({
        oldPassword: oldPasswordValue,
        newPassword: newPasswordValue,
    });
}
</script>

<style scoped lang="scss">
@import '/public/css/main';

.update-form {
    margin-top: 1rem;
    padding: 1rem;
    border: 1px solid $border-color;
    border-radius: $border-radius;

    & > .form-group {
        padding-top: 1rem;
    }
}
</style>
