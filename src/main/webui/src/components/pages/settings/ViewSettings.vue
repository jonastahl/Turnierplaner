<template>
	<div class="w-full p-2 pt-0 grid">
		<SettingsItem v-if="isAdmin">
			<Card>
				<template #title>{{ t("general.general") }}</template>
				<template #content>
					<SettingsGeneral />
				</template>
			</Card>
		</SettingsItem>
		<SettingsItem v-if="isDirector">
			<Card>
				<template #title>{{ t("general.courts") }}</template>
				<template #content>
					<SettingsCourts />
				</template>
			</Card>
		</SettingsItem>
		<SettingsItem v-if="isDirector">
			<Card>
				<template #title>{{ t("settings.verification") }}</template>
				<template #content>
					<SettingsPlayerVerify />
				</template>
			</Card>
		</SettingsItem>
		<SettingsItem v-if="isAdmin">
			<Card>
				<template #title>{{ t("settings.user") }}</template>
				<template #content>
					<SettingsUser />
				</template>
			</Card>
		</SettingsItem>
		<SettingsItem v-if="isDirector">
			<Card>
				<template #title>
					<div class="flex justify-content-between">
						<span>{{ t("settings.player") }}</span>
						<Button
							:label="t('general.register')"
							@click="router.push({ name: Routes.PlayerRegistration })"
						/>
					</div>
				</template>
				<template #content>
					<SettingsPlayer />
				</template>
			</Card>
		</SettingsItem>
	</div>
</template>

<script setup lang="ts">
import { useI18n } from "vue-i18n"
import SettingsCourts from "@/components/pages/settings/SettingsCourts.vue"
import SettingsItem from "@/components/pages/settings/SettingsItem.vue"
import SettingsGeneral from "@/components/pages/settings/SettingsGeneral.vue"
import { getIsAdmin, getIsDirector } from "@/backend/security"
import { inject, ref, watch } from "vue"
import SettingsPlayerVerify from "@/components/pages/settings/SettingsPlayerVerify.vue"
import SettingsUser from "@/components/pages/settings/SettingsUser.vue"
import SettingsPlayer from "@/components/pages/settings/SettingsPlayer.vue"
import { router } from "@/main"
import { Routes } from "@/routes"
import Button from "primevue/button"

const { t } = useI18n()

const isLoggedIn = inject("loggedIn", ref(false))
const { data: isAdmin } = getIsAdmin(isLoggedIn)
const { data: isDirector, isSuccess: directorSuccess } =
	getIsDirector(isLoggedIn)
watch(
	[isLoggedIn, isDirector, directorSuccess],
	() => {
		if (!isLoggedIn.value || (isDirector.value && !directorSuccess.value))
			router.push({
				name: Routes.Tournaments,
			})
	},
	{ immediate: true },
)
</script>

<style scoped></style>
