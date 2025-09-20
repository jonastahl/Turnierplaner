<template>
	<div class="w-full p-2">
		<Card style="margin-top: -10px !important">
			<template #content>
				<div class="flex flex-row justify-content-between mb-2">
					<SelectButton v-model="calendarView" :options="views">
						<template #option="{ option }">
							{{ t(option.label) }}
						</template>
					</SelectButton>
					<Button
						:size="'small'"
						:label="t('general.saveandpublish')"
						@click="
							toast.add({
								severity: 'info',
								summary: 'Coming soon',
								detail: 'Not yet implemented',
								life: 3000,
							})
						"
					/>
				</div>
				<ExecutionCalendar v-if="calendarView.id === 'calendar'" />
				<MatchesList v-else-if="calendarView.id === 'list'" :tour-id="tournament?.id || undefined" />
			</template>
		</Card>
	</div>
	<!-- TODO add click action for every game to edit details -->
</template>

<script setup lang="ts">
import { useI18n } from "vue-i18n"
import { ref } from "vue"
import MatchesList from "@/components/pages/matches/MatchesList.vue"
import ExecutionCalendar from "@/components/pages/management/execution/calendar/ExecutionCalendar.vue"
import { useToast } from "primevue/usetoast"
import { getTournamentDetails } from "@/backend/tournament"
import { useRoute } from "vue-router"

const route = useRoute()
const { t } = useI18n()
const toast = useToast()

const {data: tournament} = getTournamentDetails(route, t, toast)

const views = [
	{
		id: "calendar",
		label: "general.calendar",
	},
	{
		id: "list",
		label: "general.list",
	},
]

const calendarView = ref(views[0])
</script>

<style scoped></style>
