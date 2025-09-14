<template>
	<div class="w-full flex flex-column p-1 text-left">
		<strong>{{ genTitle(props.match.title, t) }}</strong>
		<i v-if="props.match.tourName !== tournament?.name">
			<LinkTournament :tournament="props.match.tourName" />
		</i>
		<i>
			<LinkCompetition
				:tournament="props.match.tourName"
				:competition="props.match.compName"
			/>
		</i>
		<ul class="mt-1 pt-0 pl-4">
			<li v-if="props.match.teamA">
				<template v-if="props.match.teamA.playerA">
					<ViewTeamNames :team="props.match.teamA" :inverted="true" />
				</template>
			</li>
			<li v-if="props.match.teamB">
				<template v-if="props.match.teamB.playerA">
					<ViewTeamNames :team="props.match.teamB" :inverted="true" />
				</template>
			</li>
		</ul>
	</div>
</template>

<script setup lang="ts">
import { AnnotatedMatch } from "@/interfaces/match"
import { genTitle } from "@/backend/tournament"
import { useI18n } from "vue-i18n"
import ViewTeamNames from "@/components/links/LinkTeamNames.vue"
import LinkCompetition from "@/components/links/LinkCompetition.vue"
import LinkTournament from "@/components/links/LinkTournament.vue"
import { Tournament } from "@/interfaces/tournament"

const { t } = useI18n()

const props = defineProps<{
	match: AnnotatedMatch
	tournament?: Tournament
}>()
</script>

<style scoped></style>
