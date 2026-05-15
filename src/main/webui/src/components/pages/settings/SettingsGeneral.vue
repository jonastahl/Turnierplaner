<template>
	<div class="flex flex-column gap-2">
		<div class="flex flex-row">
			<InputGroup>
				<Dropdown v-model="loc" :options="availableLocales" />
				<Button @click="saveLanguage(loc)">
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
		<div class="mt-2 flex flex-row justify-content-end">
			<Button @click="backupDialog = true" severity="danger">
				{{ t("settings.backup") }}
			</Button>
		</div>
	</div>

	<Dialog v-model:visible="backupDialog" :header="t('settings.backup')">
		<p>{{ t("settings.backup_download_warning") }}</p>
		<p>{{ t("settings.backup_restore_warning") }}</p>
		<div class="flex flex-row justify-content-end">
			<Button
				@click="downloadBackup"
				icon="pi pi-download"
				:label="t('settings.download_backup')"
			/>
			<Button
				class="ml-2"
				@click="restoreBackup"
				severity="danger"
				icon="pi pi-history"
				:label="t('settings.restore_backup')"
			/>
		</div>
	</Dialog>
	<ConfirmDialog />
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
import axios from "axios"
import { useConfirm } from "primevue/useconfirm"

const { t, locale, availableLocales } = useI18n()
const isLoggedIn = inject("loggedIn", ref(false))
const confirm = useConfirm()

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

const backupDialog = ref(false)
const restoreConfirmDialog = ref(false)

async function downloadBackup() {
	const response = await axios.get("/backup/download", {
		responseType: "blob",
	})

	const blob = new Blob([response.data], {
		type: response.headers["content-type"],
	})
	const downloadUrl = window.URL.createObjectURL(blob)
	const link = document.createElement("a")
	link.href = downloadUrl

	const contentDisposition = response.headers["content-disposition"]
	let filename = "backup.zip"
	if (contentDisposition) {
		const filenameMatch = contentDisposition.match(/filename="?([^"]+)"?/)
		if (filenameMatch && filenameMatch[1]) {
			filename = filenameMatch[1]
		}
	}
	link.setAttribute("download", filename)

	document.body.appendChild(link)
	link.click()

	link.remove()
	window.URL.revokeObjectURL(downloadUrl)
}

function restoreBackup() {
	confirm.require({
		message: t("settings.backup_restore_warning"),
		header: t("settings.restore_backup"),
		icon: "pi pi-exclamation-triangle",
		acceptIcon: "pi pi-exclamation-triangle",
		acceptClass: "p-button-danger",
		accept: () => {
			// TODO
		},
	})
}
</script>

<style scoped></style>
