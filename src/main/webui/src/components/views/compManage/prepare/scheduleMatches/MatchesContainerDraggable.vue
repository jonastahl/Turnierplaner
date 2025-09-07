<template>
	<DraggablePanel
		id="match"
		:component-data="{
			tag: 'div',
			name: props.isUpdating || isUpdating ? 'playerList' : 'default',
			type: 'transition',
		}"
		group="match"
		:list="matches"
		:tag="TransitionGroup"
		class="flex flex-row flex-wrap gap-2 border-3 min-player-height border-round border-dashed"
		item-key="id"
		style="box-sizing: content-box; width: calc(100% - 6px)"
	>
		<template #default="{ item }">
			<div style="background-color: rgba(164, 230, 210, 0.9); width: 200px">
				<MatchEvent :match="item" :competition="competition" />
			</div>
		</template>
	</DraggablePanel>
</template>

<script setup lang="ts">
import { computed, TransitionGroup, watch } from "vue"
import DraggablePanel from "@/draggable/DraggablePanel.vue"
import { getKnockout } from "@/backend/knockout"
import { useRoute } from "vue-router"
import { AnnotatedMatch, Match } from "@/interfaces/match"
import { getCompetitionDetails } from "@/backend/competition"
import { useI18n } from "vue-i18n"
import { useToast } from "primevue/usetoast"
import { getGroup } from "@/backend/group"
import { CompType } from "@/interfaces/competition"
import {
	extractGroupMatches,
	extractKnockoutMatches,
} from "@/components/views/compManage/prepare/scheduleMatches/ScheduleMatchesHelper"
import MatchEvent from "@/components/views/compManage/prepare/scheduleMatches/MatchEvent.vue"

const route = useRoute()
const { t } = useI18n()
const toast = useToast()

const { data: competition } = getCompetitionDetails(route, t, toast)
const { data: knockout } = getKnockout(
	route,
	computed(() => competition.value?.tourType === CompType.KNOCKOUT),
)
const { data: groups } = getGroup(
	route,
	computed(() => competition.value?.tourType === CompType.GROUPS),
)

const matches = defineModel<AnnotatedMatch[]>({ default: [] })
watch(
	[knockout, groups],
	() => {
		matches.value.splice(0, matches.value.length)
		if (competition.value?.tourType === CompType.KNOCKOUT && knockout.value) {
			extractKnockoutMatches(knockout.value, addMatch)
		} else if (
			competition.value?.tourType === CompType.GROUPS &&
			groups.value
		) {
			extractGroupMatches(groups.value, addMatch)
		}
	},
	{
		immediate: true,
	},
)

function addMatch(match: Match, title: AnnotatedMatch["title"]) {
	if (!match.begin)
		matches.value.push({
			title,
			compName: <string>route.params.compId,
			tourName: <string>route.params.tourId,
			...match,
		})
}

const props = withDefaults(
	defineProps<{
		isUpdating: boolean
	}>(),
	{
		isUpdating: false,
	},
)
</script>

<style scoped></style>
