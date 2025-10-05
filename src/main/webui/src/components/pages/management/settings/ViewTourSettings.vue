<template>
	<div class="flex justify-content-center">
		<Card id="card" :pt="{ content: 'p-0' }">
			<template #content>
				<FormTournament
					v-if="!tournament || isUpdating"
					:tour-details="TournamentDefault"
					:disabled="true"
					:submit-text="t('general.update')"
					header="ViewEditTournament.tournamentUpdate"
					@submit="submit"
				/>
				<FormTournament
					v-else
					ref="form"
					:tour-details="tournament"
					:disabled="false"
					:submit-text="t('general.update')"
					header="ViewEditTournament.tournamentUpdate"
					@submit="submit"
				/>
			</template>
			<template #footer>
				<div class="flex flex-row justify-content-end">
					<Button :label="t('general.save')" severity="success" @click="save" />
				</div>
			</template>
		</Card>
	</div>
</template>

<script lang="ts" setup>
import { useRoute } from "vue-router"
import { useToast } from "primevue/usetoast"
import { ref, watch } from "vue"
import FormTournament from "@/components/pages/forms/FormTournament.vue"
import { TournamentDefault, TournamentServer } from "@/interfaces/tournament"
import { getTournamentDetails, useUpdateTournament } from "@/backend/tournament"
import { useI18n } from "vue-i18n"

const { t } = useI18n()
const toast = useToast()

const route = useRoute()
const form = ref<InstanceType<typeof FormTournament> | null>(null)

const isUpdating = defineModel<boolean>("isUpdating", { default: false })

function sleep(milliseconds: number) {
	return new Promise((resolve) => setTimeout(resolve, milliseconds))
}

const { data: tournament } = getTournamentDetails(route, t, toast)
watch(tournament, async () => {
	isUpdating.value = true
	await sleep(100)
	isUpdating.value = false
})

const { mutate } = useUpdateTournament(t, toast, {})

function save() {
	form.value?.onSubmit()
}

function submit(server_data: TournamentServer) {
	mutate(server_data)
}
</script>

<style scoped>
#card {
	width: min(90dvw, 50rem);
}
</style>
