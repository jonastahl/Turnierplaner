<template>
	<div class="flex flex-column p-1 text-left">
		<strong>{{ genTitle(props.from.title, t) }}</strong>
		<i>
			<LinkCompetition
				:tournament="props.from.tourName"
				:competition="props.from.compName"
			/>
		</i>
		<span
			>{{ t("general.begin") }}:
			<ValOrEdited
				:original="
					props.from.begin?.toLocaleString(t('lang'), dateOptions) || ''
				"
				:edited="props.to.begin?.toLocaleString(t('lang'), dateOptions) || ''"
			/>
		</span>
		<span
			>{{ t("general.end") }}:
			<ValOrEdited
				:original="props.from.end?.toLocaleString(t('lang'), dateOptions) || ''"
				:edited="props.to.end?.toLocaleString(t('lang'), dateOptions) || ''"
			/>
		</span>
		<span
			>{{ t("general.court") }}:
			<ValOrEdited
				:original="props.from.court || ''"
				:edited="props.to.court || ''"
			/>
		</span>
		<ul class="mt-1 pt-0 pl-4">
			<li v-if="props.from.teamA">
				<template v-if="props.from.teamA.playerA">
					<ViewTeamNames :team="props.from.teamA" :inverted="true" />
				</template>
			</li>
			<li v-if="props.from.teamB">
				<template v-if="props.from.teamB.playerA">
					<ViewTeamNames :team="props.from.teamB" :inverted="true" />
				</template>
			</li>
		</ul>
	</div>
</template>

<script setup lang="ts">
import { useI18n } from "vue-i18n"
import ViewTeamNames from "@/components/links/LinkTeamNames.vue"
import LinkCompetition from "@/components/links/LinkCompetition.vue"
import ValOrEdited from "@/components/pages/management/execution/calendar/ValOrEdited.vue"
import { genTitle } from "@/backend/match"
import { AnnotatedMatch } from "@/interfaces/match"

const { t } = useI18n()

const props = defineProps<{
	from: AnnotatedMatch
	to: AnnotatedMatch
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
