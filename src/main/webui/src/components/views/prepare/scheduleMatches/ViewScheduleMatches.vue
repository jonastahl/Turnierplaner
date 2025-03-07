<template>
	<div class="grid">
		<div class="col-4 flex flex-column gap-3">
			<Card>
				<template #title>{{ t("general.courts") }}</template>
				<template #content>
					<CourtChooser v-if="selectedCourts" v-model="selectedCourts" />
				</template>
			</Card>
			<Card>
				<template #title>{{ t("general.matches") }}</template>
				<template #content>
					<MatchesContainerDraggable
						v-model="matches"
						:is-updating="isUpdating"
					/>
				</template>
			</Card>
		</div>
		<div class="col-8">
			<div class="flex flex-column gap-3">
				<SchedulingCalendar
					v-model="scheduledMatches"
					:courts="selectedCourts"
					@remove-id="
						(id) => {
							const extEventToDeletePos = matches.findIndex(
								(match) => id === match.id,
							)
							if (extEventToDeletePos > -1)
								matches.splice(extEventToDeletePos, 1)
						}
					"
					@delete-schedule="(match) => matches.push(remSchedule(match))"
				/>
			</div>
		</div>
	</div>
</template>

<script setup lang="ts">
import { useRoute, useRouter } from "vue-router"
import { useToast } from "primevue/usetoast"
import SchedulingCalendar from "@/components/views/prepare/scheduleMatches/SchedulingCalendar.vue"
import { ref, watch } from "vue"
import { Court } from "@/interfaces/court"
import MatchesContainerDraggable from "@/components/views/prepare/scheduleMatches/MatchesContainerDraggable.vue"
import { MatchCalEvent } from "@/components/views/prepare/scheduleMatches/ScheduleMatchesHelper"
import { useUpdateMatches } from "@/backend/match"
import { useI18n } from "vue-i18n"
import CourtChooser from "@/components/views/prepare/scheduleMatches/CourtChooser.vue"
import { getTournamentCourts, useUpdateTournamentCourts } from "@/backend/court"
import { getTournamentDetails } from "@/backend/tournament"
import { AnnotatedMatch } from "@/interfaces/match"

const route = useRoute()
const router = useRouter()
const { t } = useI18n()
const toast = useToast()

const isUpdating = defineModel<boolean>("isUpdating", { default: false })

const matches = ref<AnnotatedMatch[]>([])
const scheduledMatches = ref<MatchCalEvent[]>([])

const { data: tournament } = getTournamentDetails(route, t, toast)
const { data: tournamentCourts } = getTournamentCourts(route)
const { mutate: updateMatches } = useUpdateMatches(route, t, toast)
const { mutate: updateCourts } = useUpdateTournamentCourts(tournament)

const selectedCourts = ref<Court[]>([])
watch(
	tournamentCourts,
	() => {
		if (!tournamentCourts.value) return

		selectedCourts.value = JSON.parse(JSON.stringify(tournamentCourts.value))
	},
	{ immediate: true },
)

function prevPage() {
	router.replace({
		name: "assignMatches",
		params: { tourId: route.params.tourId, compId: route.params.compId },
	})
}

function nextPage() {
	// TODO
	toast.add({
		severity: "success",
		summary: "Competition updated",
		detail: "TODO",
		life: 3000,
	})
}

function save() {
	isUpdating.value = true
	updateMatches([
		...scheduledMatches.value
			.filter((event) => !event.secondary)
			.map((event) => {
				return {
					...event.data,
					begin: event.start,
					end: event.end,
					court: event.split,
				}
			}),
		...matches.value.map((match) => {
			return match
		}),
	])
	updateCourts(selectedCourts.value)
	isUpdating.value = false
}

function remSchedule(match: AnnotatedMatch) {
	match.begin = null
	match.end = null
	match.court = null
	return match
}

defineExpose({ prevPage, save, nextPage })
</script>

<style scoped></style>
