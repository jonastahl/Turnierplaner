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
						v-if="calendarView.id === CALENDAR"
						:size="'small'"
						:label="t('general.saveandpublish')"
						:disabled="changeSize === 0"
						:badge="changeSize.toString()"
						@click="calendar?.save()"
					/>
				</div>
				<ExecutionCalendar
					v-if="calendarView.id === CALENDAR"
					ref="calendar"
					v-model:change-size="changeSize"
				/>
				<MatchesList
					v-else-if="calendarView.id === LIST"
					:tour-id="tournament?.id || undefined"
				/>
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

const { data: tournament } = getTournamentDetails(route, t, toast)

const calendar = ref<InstanceType<typeof ExecutionCalendar>>()

const changeSize = ref(0)

const CALENDAR = "calendar"
const LIST = "list"
const views = [
	{
		id: CALENDAR,
		label: "general.calendar",
	},
	{
		id: LIST,
		label: "general.list",
	},
]

const calendarView = ref(views[0])
</script>

<style scoped></style>
