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
			<template v-if="windowWidth < 768">
				<TabView :pt="{ panelContainer: 'p-0 pt-3' }">
					<TabPanel :header="t('ViewCompetition.playerA')">
						<div class="flex flex-column gap-2">
							<ViewConditions />
							<SignUpDropDown />
						</div>
					</TabPanel>
					<TabPanel :header="t('ViewCompetition.playerB')">
						<div class="flex flex-column gap-2">
							<ViewConditions second />
							<SignUpDropDown is-player-b />
						</div>
					</TabPanel>
				</TabView>
				<divider />
			</template>
			<div v-else class="grid">
				<div class="col-6 flex flex-column gap-2">
					<ViewConditions />
					<SignUpDropDown />
				</div>
				<div class="col-6 flex flex-column gap-2">
					<ViewConditions :second="true" />
					<SignUpDropDown :is-player-b="true" />
				</div>
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
import { ref } from "vue"

const { t } = useI18n()
const toast = useToast()

const route = useRoute()

let windowWidth = ref(window.innerWidth)
window.addEventListener("resize", () => {
	windowWidth.value = window.innerWidth
})

const { data: competition } = getCompetitionDetails(route, t, toast)
</script>

<style scoped></style>
