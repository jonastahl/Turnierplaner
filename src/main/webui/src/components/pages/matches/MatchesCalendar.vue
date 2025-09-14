<template>
	<!-- TODO add report and rescheduling -->
	<ViewCalendar
		v-if="tournament"
		v-model:events="events"
		style="height: 800px"
		:selected-date="tournament.game_phase.begin"
		:min-date="tournament.game_phase.begin"
		:max-date="tournament.game_phase.end"
		:split-days="splitDays"
		@on-view-change="onViewChange"
	>
		<template #event="{ event }">
			<MatchEvent :match="<AnnotatedMatch>event.data" />
		</template>
	</ViewCalendar>
</template>

<script setup lang="ts">
import { AnnotatedMatch } from "@/interfaces/match"
import ViewCalendar from "@/calendar/ViewCalendar.vue"
import MatchEvent from "@/components/pages/management/prepare/scheduleMatches/MatchEvent.vue"
import { useRoute } from "vue-router"
import { useI18n } from "vue-i18n"
import { useToast } from "primevue/usetoast"
import { computed, ref, watch } from "vue"
import { getTournamentCourts } from "@/backend/court"
import {
	getTournamentDetails,
	getTournamentMatchEvents,
} from "@/backend/tournament"
import { MatchCalEvent } from "@/components/pages/management/prepare/scheduleMatches/ScheduleMatchesHelper"

const route = useRoute()
const { t } = useI18n()
const toast = useToast()

const curStart = ref<Date | undefined>()
const curEnd = ref<Date | undefined>()

const { data: courts } = getTournamentCourts(route)
const { data: tournament } = getTournamentDetails(route, t, toast)
const { data: matches } = getTournamentMatchEvents(route, t, curStart, curEnd)

function onViewChange(startDate: Date, endDate: Date) {
	curStart.value = startDate
	curEnd.value = endDate
}

const events = ref<MatchCalEvent[]>([])

watch(matches, () => {
	events.value.splice(0, events.value.length)

	if (!matches.value) return

	matches.value.forEach((match) => {
		events.value.push(match)
	})
})

const splitDays = computed(() => {
	if (!courts.value) return []

	return courts.value.map((court, index) => {
		return {
			id: court.name,
			label: court.name,
			class: "court" + (index + 1),
		}
	})
})
</script>

<style scoped></style>
