<template>
	<div class="flex justify-content-center w-full">
		<Card id="card">
			<template #title>{{ t("ViewCreateCompetition.header") }}</template>
			<template #content>
				<FormCompetition
					ref="form"
					:competition="CompetitionDefault"
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
import { ref } from "vue"
import { useRoute } from "vue-router"
import FormCompetition from "@/components/pages/forms/FormCompetition.vue"
import { useI18n } from "vue-i18n"
import { CompetitionServer, CompetitionDefault } from "@/interfaces/competition"
import { useToast } from "primevue/usetoast"
import { useAddCompetition } from "@/backend/competition"
import { Routes } from "@/routes"

const { t } = useI18n()
const toast = useToast()

const route = useRoute()
const form = ref<InstanceType<typeof FormCompetition> | null>(null)

const { mutate } = useAddCompetition(route, t, toast, {
	suc: () => {
		router.push({
			name: Routes.Competitions,
			params: { tourId: route.params.tourId },
		})
	},
})

function submit(server_data: CompetitionServer) {
	mutate(server_data)
}
</script>

<style scoped>
#card {
	width: min(90dvw, 50rem);
}
</style>
