<template>
	<div class="flex flex-column-reverse align-items-end gap-3">
		<div class="flex flex-row align-items-center gap-3">
			<Dropdown
				v-model="locale"
				:options="availableLocales"
				@change="(event) => saveLanguage(event.value)"
			/>
			<Button v-if="isLoggedIn" rounded outlined size="small" @click="settings">
				<template #icon>
					<span class="material-symbols-outlined">settings</span>
				</template>
			</Button>
		</div>

		<span
			v-if="!isLoggedIn"
			class="material-symbols-outlined cursor-pointer bigger"
			@click="login"
		>
			login
		</span>
		<template v-else>
			<div class="flex flex-row align-items-center gap-3">
				<span>{{ currentUser }}</span>
				<span
					class="material-symbols-outlined cursor-pointer bigger"
					@click="logout"
				>
					logout
				</span>
			</div>
		</template>
	</div>
</template>

<script setup lang="ts">
import { auth } from "@/security/AuthService"
import { inject, ref, watch } from "vue"
import { router } from "@/main"
import { useI18n } from "vue-i18n"
import { useSaveLanguage } from "@/backend/config"
import { Routes } from "@/routes"

const currentUser = ref<string>("")
const isLoggedIn = inject("loggedIn", ref(false))
const { locale, availableLocales } = useI18n()

const { mutate: saveLanguage } = useSaveLanguage(isLoggedIn)

function settings() {
	router.push({
		name: Routes.Settings,
	})
}

watch(
	isLoggedIn,
	async () => {
		auth.getUser().then((user) => {
			if (user !== null && user.profile.preferred_username) {
				currentUser.value = user.profile.preferred_username
			}
		})
	},
	{ immediate: true },
)

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
