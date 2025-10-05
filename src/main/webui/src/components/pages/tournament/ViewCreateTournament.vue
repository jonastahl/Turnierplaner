<template>
	<div class="flex justify-content-center w-full">
		<Card id="card">
			<template #title>{{
				t("ViewCreateTournament.tournamentCreation")
			}}</template>
			<template #content>
				<FormTournament
					ref="form"
					:tour-details="TournamentDefault"
					:disabled="false"
					@submit="submit"
				/>
			</template>
			<template #footer>
				<div class="justify-content-end flex">
					<Button
						:label="t('general.create')"
						severity="success"
						@click="() => form !== null && form.onSubmit()"
					/>
				</div>
			</template>
		</Card>
	</div>
</template>

<script lang="ts" setup>
import { router } from "@/main"
import { useToast } from "primevue/usetoast"
import FormTournament from "@/components/pages/forms/FormTournament.vue"
import { TournamentDefault, TournamentServer } from "@/interfaces/tournament"
import { useI18n } from "vue-i18n"
import { useAddTournament } from "@/backend/tournament"
import { Routes } from "@/routes"
import { ref } from "vue"

const { t } = useI18n()
const toast = useToast()

let tourName: string | null = null
const { mutate } = useAddTournament(t, toast, {
	suc: () => {
		if (!tourName) return
		router.push({
			name: Routes.Competitions,
			params: { tourId: tourName },
		})
	},
})
const form = ref<InstanceType<typeof FormTournament> | null>(null)

function submit(server_data: TournamentServer) {
	tourName = server_data.name
	mutate(server_data)
}
</script>

<style scoped>
#card {
	width: min(90dvw, 50rem);
}
</style>
