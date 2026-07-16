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
				<div
					v-tooltip.bottom="t('settings.admin_needed_summary')"
					class="flex align-items-center"
				>
					<i-material-symbols-help-outline class="cursor-pointer" />
				</div>
			</div>
			<InputSwitch
				v-model="adminNeeded"
				@change="() => saveAdminNeeded(adminNeeded)"
			>
				{{ t("settings.save_default") }}
			</InputSwitch>
		</div>
		<div class="mt-2 flex flex-row justify-content-end">
			<Button severity="danger" @click="backupDialog = true">
				{{ t("settings.backup") }}
			</Button>
		</div>
	</div>

	<Dialog v-model:visible="backupDialog" :header="t('settings.backup')">
		<p>{{ t("settings.backup_download_warning") }}</p>
		<p>{{ t("settings.backup_restore_warning") }}</p>
		<div class="flex flex-row justify-content-end">
			<Button
				icon="pi pi-download"
				:label="t('settings.download_backup')"
				@click="downloadBackup"
			/>
			<Button
				class="ml-2"
				severity="danger"
				icon="pi pi-history"
				:label="t('settings.restore_backup')"
				@click="restoreBackup"
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
import { useToast } from "primevue/usetoast"

const { t, locale, availableLocales } = useI18n()
const toast = useToast()
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

async function downloadBackup() {
	axios
		.post<string>("/backup/generateDownload")
		.then((resp) => {
			window.open(`/backup/download?token=${resp.data}`, "_blank")
		})
		.catch(() => {
			toast.add({
				severity: "error",
				summary: t("general.failure"),
				life: 3000,
			})
		})
}

function restoreBackup() {
	confirm.require({
		message: t("settings.backup_restore_warning"),
		header: t("settings.restore_backup"),
		icon: "pi pi-exclamation-triangle",
		acceptIcon: "pi pi-exclamation-triangle",
		acceptClass: "p-button-danger",
		acceptLabel: t("general.yes"),
		rejectLabel: t("general.no"),
		accept: () => {
			const fileInput = document.createElement("input")
			fileInput.type = "file"
			fileInput.accept = ".turnier" // Limit selection to your custom extension
			fileInput.style.display = "none"

			// Listen for the user selecting a file
			fileInput.onchange = async (event) => {
				if (!event.target) return

				const target = event.target as HTMLInputElement
				if (!target || !target.files) return

				const file = target.files[0]
				if (!file) return

				try {
					await axios.post("/backup/upload", file, {
						headers: {
							"Content-Type": "application/octet-stream",
						},
					})

					console.log("Backup successfully restored.")
					window.location.reload()
				} catch (error) {
					console.error("Failed to upload backup:", error)
				} finally {
					// Clean up the DOM
					fileInput.remove()
				}
			}

			document.body.appendChild(fileInput)
			fileInput.click()
		},
	})
}
</script>

<style scoped></style>
