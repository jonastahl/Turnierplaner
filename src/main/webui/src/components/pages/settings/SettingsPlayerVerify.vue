<template>
	<DataTable :value="unverified" striped-rows class="w-full mb-2" size="small">
		<template #empty>
			{{ t("settings.no_unverified") }}
		</template>
		<Column field="name" :header="t('general.name')">
			<template #body="{ data }">
				<div class="flex flex-row justify-content-between align-items-center">
					<div>
						{{ data.name }}
					</div>
					<div>
						<Button
							severity="success"
							text
							rounded
							size="small"
							class="h-2rem"
							@click="verifyPlayer(data)"
						>
							<span class="material-symbols-outlined">check_circle</span>
						</Button>
						<Button
							severity="danger"
							text
							rounded
							size="small"
							class="h-2rem"
							@click="deletePlayer(data)"
						>
							<span class="material-symbols-outlined">delete_forever</span>
						</Button>
					</div>
				</div>
			</template>
		</Column>
	</DataTable>
</template>

<script setup lang="ts">
import { useI18n } from "vue-i18n"
import {
	getUnverified,
	useAdminVerify,
	useDeletePlayer,
} from "@/backend/player"
import { useToast } from "primevue/usetoast"

const { t } = useI18n()
const toast = useToast()

const { data: unverified } = getUnverified(t, toast)
const { mutate: verifyPlayer } = useAdminVerify()
const { mutate: deletePlayer } = useDeletePlayer()
</script>

<style scoped></style>
