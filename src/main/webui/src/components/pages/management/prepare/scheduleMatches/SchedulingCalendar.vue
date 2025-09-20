<template>
	<ViewCalendar
		v-if="tournament"
		ref="calendar"
		v-model="events"
		style="height: 1000px"
		:selected-date="tournament.game_phase.begin"
		:min-date="tournament.game_phase.begin"
		:max-date="tournament.game_phase.end"
		:split-days="splitDays"
		:editable-events="!!props.courts.length"
		deletable-events
		@on-event-drop="onEventDrop"
		@on-event-delete="onEventDelete"
		@on-view-change="onViewChange"
	>
		<template #event="{ event }">
			<EventMatch
				:match="<AnnotatedMatch>event.data"
				:tournament="tournament"
			/>
		</template>
	</ViewCalendar>
</template>

<script setup lang="ts">
import { Court } from "@/interfaces/court"
import { computed, ref, watch } from "vue"
import { getTournamentDetails } from "@/backend/tournament"
import { useRoute } from "vue-router"
import { useToast } from "primevue/usetoast"
import { useI18n } from "vue-i18n"
import { getCompetitionDetails } from "@/backend/competition"
import { getKnockout } from "@/backend/knockout"
import { CompType } from "@/interfaces/competition"
import { getGroup } from "@/backend/group"
import { AnnotatedMatch, Match } from "@/interfaces/match"
import {
	extractGroupMatches,
	extractKnockoutMatches,
	MatchCalEvent,
} from "@/components/pages/management/prepare/scheduleMatches/ScheduleMatchesHelper"
import EventMatch from "@/components/pages/management/prepare/scheduleMatches/MatchEvent.vue"
import ViewCalendar from "@/calendar/ViewCalendar.vue"
import { v4 as uuidv4 } from "uuid"
import { ComponentExposed } from "vue-component-type-helpers"
import { getScheduledMatchEventsExceptCompetition } from "@/backend/match"

const calendar = ref<ComponentExposed<typeof ViewCalendar> | null>(null)

function reload() {
	if (calendar.value) calendar.value.reload()
}

const emit = defineEmits<{
	removeId: [id: string]
	deleteSchedule: [event: AnnotatedMatch]
}>()
const props = defineProps<{
	courts: Court[]
}>()
watch(() => props.courts, reload)

const { t } = useI18n()
const route = useRoute()
const toast = useToast()
const { data: tournament } = getTournamentDetails(route, t, toast)
const { data: competition } = getCompetitionDetails(route, t, toast)
const { data: knockout } = getKnockout(
	route,
	computed(() => competition.value?.tourType === CompType.KNOCKOUT),
)
const { data: groups } = getGroup(
	route,
	computed(() => competition.value?.tourType === CompType.GROUPS),
)

const curStart = ref<Date | undefined>()
const curEnd = ref<Date | undefined>()
const { data: exMatches } = getScheduledMatchEventsExceptCompetition(
	route,
	curStart,
	curEnd,
)

const events = defineModel<MatchCalEvent[]>({ default: [] })
watch(
	[knockout, groups],
	() => {
		events.value.splice(0, events.value.length)
		if (competition.value?.tourType === CompType.KNOCKOUT && knockout.value) {
			extractKnockoutMatches(knockout.value, addMatch)
		} else if (
			competition.value?.tourType === CompType.GROUPS &&
			groups.value
		) {
			extractGroupMatches(groups.value, addMatch)
		}

		updateExisting()
	},
	{ immediate: true },
)

watch([exMatches, props], updateExisting)

function updateExisting() {
	console.log("update")
	for (let i = events.value.length - 1; i >= 0; i--) {
		if (events.value[i].secondary) events.value.splice(i, 1)
	}

	if (exMatches.value) {
		exMatches.value
			.filter((match) =>
				props.courts.some((court) => court.name === match.data.court),
			)
			.forEach((match) => {
				events.value.push({
					draggable: false,
					resizable: false,
					deletable: false,
					secondary: true,
					class:
						match.data.tourName == route.params.tourId
							? "extern"
							: "superextern",
					...match,
				})
			})
	}
}

// on onViewChange: load events from begin to end for courts
// -> display as unchangeable event
function onViewChange(startDate: Date, endDate: Date) {
	curStart.value = startDate
	curEnd.value = endDate
}

function addMatch(match: Match, title: AnnotatedMatch["title"]) {
	if (match.begin && match.end && match.court)
		events.value.push({
			id: match.id || uuidv4(),
			start: match.begin,
			end: match.end,
			split: match.court,
			data: {
				title,
				compName: <string>route.params.compId,
				tourName: <string>route.params.tourId,
				...match,
			},
		})
}

function onEventDrop(
	event: MatchCalEvent,
	originalEvent: MatchCalEvent,
	external: boolean,
) {
	if (external) {
		if (originalEvent.data.id) emit("removeId", originalEvent.data.id)
		else throw "Match id is missing"

		event.end = new Date(event.start)
		event.end.setHours(event.end.getHours() + 1)
		event.end.setMinutes(event.end.getMinutes() + 30)
		events.value.push({
			id: event.data.id || uuidv4(),
			start: event.start,
			end: event.end,
			split: event.split,
			data: originalEvent.data,
		})
	}
}

function onEventDelete(event: MatchCalEvent) {
	emit("deleteSchedule", event.data)
}

const splitDays = computed(() => {
	return props.courts.map((court, index) => {
		return {
			id: court.name,
			label: court.name,
			class: "court" + (index + 1),
		}
	})
})
</script>

<style></style>
