<template>
	<div class="flex justify-content-center">
		<Card id="card" :pt="{ content: 'p-0' }">
			<template #content>
				<FormCompetition
					v-if="!competition || isUpdating"
					:competition="CompetitionDefault"
					:disabled="true"
					@submit="submit"
				/>
				<FormCompetition
					v-else
					ref="form"
					:competition="competition"
					:disabled="false"
					@submit="submit"
				/>
			</template>
			<template #footer>
				<div class="flex flex-row justify-content-between">
					<Button
						:label="t('general.action.delete')"
						severity="danger"
						@click="askDeleteCompetition"
					/>
					<Button
						:label="t('general.action.save.label')"
						severity="success"
						@click="save"
					/>
				</div>
			</template>
		</Card>
	</div>
</template>

<script lang="ts" setup>
import { useRoute, useRouter } from "vue-router"
import FormCompetition from "@/components/pages/forms/FormCompetition.vue"
import { useI18n } from "vue-i18n"
import { CompetitionDefault, CompetitionServer } from "@/interfaces/competition"
import {
	getCompetitionDetails,
	useDeleteCompetition,
	useUpdateCompetition,
} from "@/backend/competition"
import { useToast } from "primevue/usetoast"
import { ref, watch } from "vue"
import { useConfirm } from "primevue/useconfirm"

const { t } = useI18n()
const toast = useToast()

const route = useRoute()
const router = useRouter()
const confirm = useConfirm()
const form = ref<InstanceType<typeof FormCompetition> | null>(null)

const isUpdating = defineModel<boolean>("isUpdating", { default: false })

function sleep(milliseconds: number) {
	return new Promise((resolve) => setTimeout(resolve, milliseconds))
}

const { data: competition } = getCompetitionDetails(route, t, toast)
watch(competition, async () => {
	isUpdating.value = true
	await sleep(100)
	isUpdating.value = false
})

const { mutate } = useUpdateCompetition(route, t, toast, {})
const { mutate: deleteCompetition } = useDeleteCompetition(route, t, toast)

function save() {
	form.value?.onSubmit()
}

function submit(server_data: CompetitionServer) {
	mutate(server_data)
}

function askDeleteCompetition() {
	confirm.require({
		message: t("competition.action.delete.confirm"),
		header: t("competition.action.delete.label"),
		icon: "pi pi-exclamation-triangle",
		rejectClass: "p-button-secondary p-button-outlined",
		rejectLabel: t("general.action.cancel"),
		acceptClass: "p-button-danger",
		acceptLabel: t("general.action.delete"),
		accept: () =>
			deleteCompetition(undefined, {
				onSuccess: () => router.back(),
			}),
	})
}
</script>

<style scoped>
#card {
	width: min(90dvw, 50rem);
}
</style>
