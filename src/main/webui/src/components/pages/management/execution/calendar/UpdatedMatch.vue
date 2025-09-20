<template>
	<div class="flex flex-column p-1 text-left">
		<strong>{{ genTitle(props.match.data.title, t) }}</strong>
		<i>
			<LinkCompetition
				:tournament="props.match.data.tourName"
				:competition="props.match.data.compName"
			/>
		</i>
		<span
			>{{ t("general.begin") }}:
			<ValOrEdited
				:original="
					props.match.data.begin?.toLocaleString(t('lang'), dateOptions) || ''
				"
				:edited="props.match.start.toLocaleString(t('lang'), dateOptions) || ''"
			/>
		</span>
		<span
			>{{ t("general.end") }}:
			<ValOrEdited
				:original="
					props.match.data.end?.toLocaleString(t('lang'), dateOptions) || ''
				"
				:edited="props.match.end.toLocaleString(t('lang'), dateOptions) || ''"
			/>
		</span>
		<span
			>{{ t("general.court") }}:
			<ValOrEdited
				:original="props.match.data.court || ''"
				:edited="props.match.split || ''"
			/>
		</span>
		<ul class="mt-1 pt-0 pl-4">
			<li v-if="props.match.data.teamA">
				<template v-if="props.match.data.teamA.playerA">
					<ViewTeamNames :team="props.match.data.teamA" :inverted="true" />
				</template>
			</li>
			<li v-if="props.match.data.teamB">
				<template v-if="props.match.data.teamB.playerA">
					<ViewTeamNames :team="props.match.data.teamB" :inverted="true" />
				</template>
			</li>
		</ul>
	</div>
</template>

<script setup lang="ts">
import { useI18n } from "vue-i18n"
import ViewTeamNames from "@/components/links/LinkTeamNames.vue"
import LinkCompetition from "@/components/links/LinkCompetition.vue"
import { MatchCalEvent } from "@/components/pages/management/prepare/scheduleMatches/ScheduleMatchesHelper"
import ValOrEdited from "@/components/pages/management/execution/calendar/ValOrEdited.vue"

const { t } = useI18n()

const props = defineProps<{
	match: MatchCalEvent
}>()

const dateOptions: Intl.DateTimeFormatOptions = {
	year: "2-digit",
	month: "numeric",
	day: "numeric",
	hour: "numeric",
	minute: "numeric",
}
</script>

<style scoped></style>
