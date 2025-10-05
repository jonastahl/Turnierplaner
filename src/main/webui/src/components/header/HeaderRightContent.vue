<template>
	<Button v-if="isDirector" rounded outlined size="small" @click="settings">
		<template #icon>
			<span class="material-symbols-outlined">settings</span>
		</template>
	</Button>
	<Dropdown
		v-model="locale"
		:options="availableLocales"
		@change="(event) => saveLanguage(event.value)"
	/>

	<span
		v-if="!isLoggedIn"
		class="material-symbols-outlined cursor-pointer bigger"
		@click="login"
	>
		login
	</span>
	<template v-else>
		<span>{{ currentUser }}</span>
		<span
			class="material-symbols-outlined cursor-pointer bigger"
			@click="logout"
		>
			logout
		</span>
	</template>
</template>

<script setup lang="ts">
import { auth } from "@/security/AuthService"
import { inject, ref, watch } from "vue"
import { router } from "@/main"
import { useI18n } from "vue-i18n"
import { useSaveLanguage } from "@/backend/config"
import { Routes } from "@/routes"
import { getIsDirector } from "@/backend/security"

const currentUser = ref<string>("")
const isLoggedIn = inject("loggedIn", ref(false))
const { locale, availableLocales } = useI18n()
const { data: isDirector } = getIsDirector(isLoggedIn)

const { mutate: saveLanguage } = useSaveLanguage(isLoggedIn)

function settings() {
	router.push({
		name: Routes.Settings,
	})
}

watch(isLoggedIn, async () => {
	auth.getUser().then((user) => {
		if (user !== null && user.profile.preferred_username) {
			currentUser.value = user.profile.preferred_username
		}
	})
})

function login() {
	auth.login()
}

function logout() {
	auth.logout()
}
</script>

<script lang="ts">
export default {
	name: "LocaleChanger",
	data() {
		return { langs: ["en", "en"] }
	},
}
</script>

<style scoped></style>
