<template>
	<ViewCalendar
		v-if="tournament"
		v-model="events"
		style="height: 800px"
		:selected-date="tournament.game_phase.begin"
		:min-date="tournament.game_phase.begin"
		:max-date="tournament.game_phase.end"
		:split-days="splitDays"
		editable-events
		@on-view-change="onViewChange"
	>
		<template #event="{ event }">
			<MatchEvent
				:match="<AnnotatedMatch>event.data"
				:tournament="tournament"
			/>
		</template>
	</ViewCalendar>
	<ChangeDialog
		v-model:visible="chgoverviewvisible"
		:change-set="changeSet"
		@publishing="
			() => {
				events.splice(0, events.length)
				chgoverviewvisible = false
			}
		"
	/>
</template>

<script setup lang="ts">
import { AnnotatedMatch } from "@/interfaces/match"
import ViewCalendar from "@/calendar/ViewCalendar.vue"
import MatchEvent from "@/components/items/MatchEvent.vue"
import { useRoute } from "vue-router"
import { useI18n } from "vue-i18n"
import { useToast } from "primevue/usetoast"
import { computed, ref, watch } from "vue"
import { getTournamentCourts } from "@/backend/court"
import { getTournamentDetails } from "@/backend/tournament"
import { MatchCalEvent } from "@/components/pages/management/prepare/scheduleMatches/ScheduleMatchesHelper"
import { getScheduledMatchesEvents } from "@/backend/match"
import ChangeDialog from "@/components/pages/management/execution/calendar/ChangeDialog.vue"

const changeSize = defineModel<number>("changeSize", { default: 0 })
const chgoverviewvisible = ref(false)

const route = useRoute()
const { t } = useI18n()
const toast = useToast()

const curStart = ref<Date | undefined>()
const curEnd = ref<Date | undefined>()

const { data: courts } = getTournamentCourts(route)
const { data: tournament } = getTournamentDetails(route, t, toast)
const { data: matches, isSuccess } = getScheduledMatchesEvents(curStart, curEnd)

function onViewChange(startDate: Date, endDate: Date) {
	curStart.value = startDate
	curEnd.value = endDate
}

const events = ref<MatchCalEvent[]>([])

watch(matches, () => {
	if (!isSuccess.value || events.value.length || !matches.value) return

	matches.value.forEach((match) => {
		const otherTour = match.data.tourName !== route.params.tourId
		events.value.push({
			draggable: !otherTour,
			resizable: !otherTour,
			deletable: !otherTour,
			secondary: otherTour,
			class: otherTour ? "superextern" : "",
			...match,
		})
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

const changeSet = computed<MatchCalEvent[]>(() => {
	return events.value.filter(
		(event) =>
			event.start.getTime() !== event.data.begin?.getTime() ||
			event.end.getTime() !== event.data.end?.getTime() ||
			event.split !== event.data.court,
	)
})
watch(changeSet, () => {
	changeSize.value = changeSet.value.length
})

function save() {
	chgoverviewvisible.value = true
}

defineExpose({ save })
</script>

<style scoped></style>
