<template>
	<div class="w-full p-2">
		<Card style="margin-top: -10px !important">
			<template #content>
				<SelectButton v-model="calendarView" class="pb-2" :options="views">
					<template #option="{ option }">
						{{ t(option.label) }}
					</template>
				</SelectButton>
				<MatchesCalendar v-if="calendarView.id === 'calendar'" />
				<MatchesList v-else-if="calendarView.id === 'list'" />
			</template>
		</Card>
	</div>
</template>

<script setup lang="ts">
import { ref } from "vue"
import MatchesCalendar from "@/components/pages/matches/MatchesCalendar.vue"
import MatchesList from "@/components/pages/matches/MatchesList.vue"
import { useI18n } from "vue-i18n"

const { t } = useI18n()

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
