<template>
	<div class="flex flex-column gap-2">
		<div class="flex flex-row">
			<InputGroup>
				<Dropdown v-model="loc" :options="availableLocales" />
				<Button @click="() => saveLanguage(loc)">
					{{ t("settings.save_default") }}
				</Button>
			</InputGroup>
		</div>
		<div class="flex flex-row">
			<InputGroup>
				<InputText
					v-model="title"
					:disabled="!config"
					:placeholder="t('general.title')"
				/>
				<Button @click="() => saveTitle(title)">
					{{ t("settings.save_default") }}
				</Button>
			</InputGroup>
		</div>
		<div
			class="mt-2 flex w-full flex-row align-items-center justify-content-between"
		>
			<div class="flex flex-row align-items-center">
				<span class="mr-1">
					{{ t("settings.admin_needed") }}
				</span>
				<span
					v-tooltip.bottom="t('settings.admin_needed_summary')"
					class="material-symbols-outlined cursor-pointer"
				>
					help
				</span>
			</div>
			<InputSwitch
				v-model="adminNeeded"
				@change="() => saveAdminNeeded(adminNeeded)"
			>
				{{ t("settings.save_default") }}
			</InputSwitch>
		</div>
	</div>
</template>

<script setup lang="ts">
import { useI18n } from "vue-i18n"
import { inject, ref, watch } from "vue"
import {
	getConfig,
	useSaveDefault,
	useSaveIsAdminVerificationNeeded,
	useSaveTitle,
} from "@/backend/config"

const { t, locale, availableLocales } = useI18n()
const isLoggedIn = inject("loggedIn", ref(false))

const { data: config } = getConfig(isLoggedIn)
const { mutate: saveLanguage } = useSaveDefault(isLoggedIn)
const { mutate: saveTitle } = useSaveTitle(isLoggedIn)
const { mutate: saveAdminNeeded } = useSaveIsAdminVerificationNeeded(isLoggedIn)
const loc = ref(locale.value)
const title = ref("")
const adminNeeded = ref(false)
watch(
	config,
	() => {
		if (config.value) {
			if (config.value.title === "title") title.value = ""
			else title.value = config.value.title
			adminNeeded.value = config.value.adminVerificationNeeded
		}
	},
	{ immediate: true },
)
</script>

<style scoped></style>
