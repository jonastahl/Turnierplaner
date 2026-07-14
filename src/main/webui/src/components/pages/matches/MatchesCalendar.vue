<template>
	<ViewCalendar
		v-if="tournament"
		v-model:events="events"
		v-model:start-date="curStart"
		v-model:end-date="curEnd"
		style="height: 800px"
		:selected-date="selected_date"
		:min-date="tournament.game_phase.begin"
		:max-date="tournament.game_phase.end"
		:split-days="splitDays"
	>
		<template #event="{ event }">
			<MatchEvent
				:match="<AnnotatedMatch>event.data"
				:edit-result="
					mayEditMatch(!!isDirector, !!isReporter, <AnnotatedMatch>event.data)
				"
			/>
		</template>
	</ViewCalendar>
</template>

<script setup lang="ts">
import { AnnotatedMatch } from "@/interfaces/match"
import ViewCalendar from "@/calendar/ViewCalendar.vue"
import MatchEvent from "@/components/items/MatchEvent.vue"
import { useRoute } from "vue-router"
import { useI18n } from "vue-i18n"
import { useToast } from "primevue/usetoast"
import { computed, inject, ref, watch } from "vue"
import { getTournamentCourts } from "@/backend/court"
import { getTournamentDetails } from "@/backend/tournament"
import { MatchCalEvent } from "@/components/pages/management/prepare/scheduleMatches/ScheduleMatchesHelper"
import { getScheduledTournamentMatchEvents } from "@/backend/match"
import { mayEditMatch } from "@/backend/set"
import { getIsDirector, getIsReporter } from "@/backend/security"

const route = useRoute()
const { t } = useI18n()
const toast = useToast()

const curStart = ref<Date | undefined>()
const curEnd = ref<Date | undefined>()

const isLoggedIn = inject("loggedIn", ref(false))
const { data: isReporter } = getIsReporter(isLoggedIn)
const { data: isDirector } = getIsDirector(isLoggedIn)

const selected_date = computed(() => {
	const now = new Date()
	if (!tournament.value) return now
	const game_phase = tournament.value.game_phase
	if (game_phase.begin <= now && game_phase.end >= now) return now
	else return game_phase.begin
})

const { data: courts } = getTournamentCourts(route)
const { data: tournament } = getTournamentDetails(route, t, toast)
const { data: matches } = getScheduledTournamentMatchEvents(
	route,
	t,
	curStart,
	curEnd,
)

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
