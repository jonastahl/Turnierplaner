<template>
	<FormTournament
		:submit-text="t('general.create')"
		:disabled="false"
		header="ViewCreateTournament.tournamentCreation"
		@submit="submit"
	/>
</template>

<script lang="ts" setup>
import { router } from "@/main"
import { useToast } from "primevue/usetoast"
import FormTournament from "@/components/views/tournaments/FormTournament.vue"
import { TournamentServer } from "@/interfaces/tournament"
import { useI18n } from "vue-i18n"
import { useAddTournament } from "@/backend/tournament"
import { Routes } from "@/routes"

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

function submit(server_data: TournamentServer) {
	tourName = server_data.name
	mutate(server_data)
}
</script>

<style scoped></style>
