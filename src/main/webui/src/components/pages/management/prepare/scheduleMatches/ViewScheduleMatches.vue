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
				<Message
					v-if="!selectedCourts.length"
					severity="warn"
					icon="pi pi-info-circle"
					class="m-0"
					:closable="false"
				>
					{{ t("court.warning_select_courts") }}
				</Message>
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
	<NavigationButtons
		:loading="isUpdating"
		is-complete
		@reset="resetDialog = true"
		@save="save"
		@complete="complete"
	/>
	<DialogResetProgress v-model="resetDialog" />
</template>

<script setup lang="ts">
import { useRoute } from "vue-router"
import { useToast } from "primevue/usetoast"
import SchedulingCalendar from "@/components/pages/management/prepare/scheduleMatches/SchedulingCalendar.vue"
import { ref, watch } from "vue"
import { Court } from "@/interfaces/court"
import MatchesContainerDraggable from "@/components/pages/management/prepare/scheduleMatches/MatchesContainerDraggable.vue"
import { MatchCalEvent } from "@/components/pages/management/prepare/scheduleMatches/ScheduleMatchesHelper"
import { useUpdateMatches } from "@/backend/match"
import { useI18n } from "vue-i18n"
import CourtChooser from "@/components/pages/management/prepare/scheduleMatches/CourtChooser.vue"
import { getTournamentCourts, useUpdateTournamentCourts } from "@/backend/court"
import { getTournamentDetails } from "@/backend/tournament"
import { AnnotatedMatch } from "@/interfaces/match"
import DialogResetProgress from "@/components/pages/management/prepare/DialogResetProgress.vue"
import NavigationButtons from "@/components/pages/management/prepare/components/NavigationButtons.vue"
import { router } from "@/main"
import { Routes } from "@/routes"

const route = useRoute()
const { t } = useI18n()
const toast = useToast()

const isUpdating = ref(false)

const matches = ref<AnnotatedMatch[]>([])
const scheduledMatches = ref<MatchCalEvent[]>([])

const resetDialog = ref(false)

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

function save(complete = false) {
	isUpdating.value = true
	updateCourts(selectedCourts.value)
	updateMatches(
		{
			complete,
			matches: [
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
			],
		},
		{
			onSettled() {
				isUpdating.value = false
			},
			onSuccess() {
				if (complete) {
					router.push({
						name: Routes.ManagePrepare,
					})
				}
			},
		},
	)
}

function complete() {
	if (matches.value.length) {
		toast.add({
			severity: "error",
			summary: t("general.failure"),
			detail: t("ViewPrepare.scheduleMatches.error_cont_not_scheduled"),
			life: 3000,
		})
		return
	}
	save(true)
}

function remSchedule(match: AnnotatedMatch) {
	match.begin = null
	match.end = null
	match.court = null
	return match
}
</script>

<style scoped></style>
