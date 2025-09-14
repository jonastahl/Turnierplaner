<template>
	<template v-if="competition">
		<!-- SINGLE -->
		<template v-if="competition.mode === Mode.SINGLE">
			<!-- Registration player A -->
			<div class="flex flex-column gap-2">
				<ViewConditions />
				<SignUpDropDown />
			</div>
		</template>
		<!-- DOUBLE TOGETHER -->
		<template v-else-if="competition.signUp === SignUp.TOGETHER">
			<div class="flex flex-column">
				<div class="grid">
					<div class="col-6">
						<ViewConditions />
					</div>
					<div class="col-6">
						<ViewConditions :second="true" />
					</div>
				</div>
				<SignUpDoubleDropdown />
			</div>
		</template>
		<!-- DOUBLE INDIVIDUAL SAME -->
		<template v-else-if="!competition.playerB.different">
			<div class="flex flex-column gutter gap-2">
				<!-- Registration player A -->
				<ViewConditions />
				<SignUpDropDown />
			</div>
		</template>
		<!-- DOUBLE INDIVIDUAL DIFFERENT -->
		<template v-else>
			<div id="regDoubIndDif">
				<ViewConditions id="regDoubIndDifCondA" />
				<SignUpDropDown id="regDoubIndDifRegA" />
				<ViewConditions id="regDoubIndDifCondB" :second="true" />
				<SignUpDropDown id="regDoubIndDifRegB" :is-player-b="true" />
			</div>
		</template>
	</template>
</template>

<script lang="ts" setup>
import ViewConditions from "@/components/pages/competition/signup/ViewConditions.vue"
import { useRoute } from "vue-router"
import { Mode, SignUp } from "@/interfaces/competition"
import { useI18n } from "vue-i18n"
import { useToast } from "primevue/usetoast"
import { getCompetitionDetails } from "@/backend/competition"
import SignUpDropDown from "@/components/pages/competition/signup/SignUpDropdown.vue"
import SignUpDoubleDropdown from "@/components/pages/competition/signup/SignUpDoubleDropdown.vue"

const { t } = useI18n()
const toast = useToast()

const route = useRoute()

const { data: competition } = getCompetitionDetails(route, t, toast)
</script>

<style scoped>
#regDoubIndDif {
	display: grid;
	grid-template-columns: 1fr 1rem 1fr;
	grid-template-rows: auto 0.5rem auto;
}

#regDoubIndDifCondA {
	grid-column: 1;
	grid-row: 1;
}

#regDoubIndDifRegA {
	grid-column: 1;
	grid-row: 3;
}

#regDoubIndDifCondB {
	grid-column: 3;
	grid-row: 1;
}

#regDoubIndDifRegB {
	grid-column: 3;
	grid-row: 3;
}

@media only screen and (max-width: 750px) {
	#regDoubIndDif {
		grid-template-columns: 1fr;
		grid-template-rows: auto auto auto auto;
	}

	#regDoubIndDifCondA {
		grid-column: 1;
		grid-row: 1;
	}

	#regDoubIndDifRegA {
		grid-column: 1;
		grid-row: 2;
		margin-bottom: 10px;
	}

	#regDoubIndDifCondB {
		grid-column: 1;
		grid-row: 3;
	}

	#regDoubIndDifRegB {
		grid-column: 1;
		grid-row: 4;
	}
}
</style>
