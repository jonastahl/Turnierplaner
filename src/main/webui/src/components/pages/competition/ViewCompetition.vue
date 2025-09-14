<template>
	<div id="container">
		<template v-if="tournamentSuc && tournament">
			<template v-if="new Date() < tournament.registration_phase.begin">
				{{ t("ViewCompetition.registration_not_started") }}
				{{
					tournament.registration_phase.begin.toLocaleString(
						t("lang"),
						dateOptions,
					)
				}}
			</template>
			<template v-else-if="new Date() < tournament.game_phase.begin">
				<!-- show registration page -->
				<ViewSignUp
					v-if="competition"
					:allow-registration="tournament.registration_phase.end > new Date()"
					:begin-game-phase="tournament.game_phase.begin"
					:competition="competition"
				/>
			</template>
			<template v-else>
				<!-- TODO show after plan has been published -->
				<!-- show game page -->
				<ViewResults
					v-if="competition"
					:tour-type="competition.tourType"
					:mode="competition.mode"
					:number-sets="competition.numberSets"
				/>
			</template>
		</template>
	</div>
</template>

<script lang="ts" setup>
import { useRoute } from "vue-router"
import ViewSignUp from "@/components/pages/competition/signup/ViewSignUp.vue"
import ViewResults from "@/components/pages/competition/results/ViewResults.vue"
import { useI18n } from "vue-i18n"
import { getTournamentDetails } from "@/backend/tournament"
import { useToast } from "primevue/usetoast"
import { getCompetitionDetails } from "@/backend/competition"

const { t } = useI18n()
const toast = useToast()

const route = useRoute()

const { data: tournament, isSuccess: tournamentSuc } = getTournamentDetails(
	route,
	t,
	toast,
)
const { data: competition } = getCompetitionDetails(route, t, toast)

const dateOptions: Intl.DateTimeFormatOptions = {
	weekday: "long",
	year: "numeric",
	month: "long",
	day: "numeric",
	hour: "numeric",
	minute: "numeric",
}
</script>

<style scoped>
#container {
	width: 100%;
	padding: 0 10px 0 10px;
	display: flex;
	flex-direction: column;
	align-items: center;
}

h2 {
	font-size: 30px;
	display: inline;
	margin-left: 15px;
}

#settings {
	color: #303030;
	margin-right: 5px;
}

#settings:hover {
	filter: drop-shadow(0 0 6px #808080);
}

#settings:active {
	color: #505050;
}

#tourName {
	display: inline;
	color: gray;
	font-size: 18px;
}
</style>
