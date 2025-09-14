<template>
	<!-- TODO place button to registration -->
	<div class="w-full flex flex-column align-items-center">
		<Card class="w-full md:w-30rem">
			<template #content>
				<strong>{{ t("general.description") }}:</strong>
				{{ props.competition.description }}<br />
				<strong>{{ t("ViewCompetition.tournament_system") }}:</strong>
				{{ t("CompetitionSettings." + props.competition.tourType.toLowerCase())
				}}<br />
				<strong>{{ t("ViewCompetition.game_mode") }}:</strong>
				{{ t("CompetitionSettings." + props.competition.mode.toLowerCase()) }}
			</template>
		</Card>

		<!-- Show registration is over -->
		<Card class="mt-2 mb-2 w-12 md:w-10 lg:w-8 xl:w-7">
			<template #content>
				<p v-if="!allowRegistration" style="text-align: center">
					{{ t("ViewCompetition.registration_over") }}
				</p>
				<template v-else>
					<div
						class="mb-2 w-12 flex flex-row justify-content-between align-items-center"
					>
						<span>
							{{ t("ViewCompetition.warning_register_before_signup") }}
						</span>
						<Button
							:label="t('general.register')"
							@click="router.push({ name: Routes.PlayerRegistration })"
						/>
					</div>
					<divider />
					<ViewSignUpForm />
				</template>

				<div class="mt-2">
					<ViewTable />
				</div>
			</template>
		</Card>

		<!-- TODO add options for admin -->
	</div>
</template>

<script lang="ts" setup>
import { ref } from "vue"
import ViewTable from "@/components/pages/competition/signup/ViewTable.vue"
import ViewSignUpForm from "@/components/pages/competition/signup/ViewSignUpForm.vue"
import { Competition } from "@/interfaces/competition"
import { useI18n } from "vue-i18n"
import { router } from "@/main"
import Button from "primevue/button"
import { Routes } from "@/routes"

const { t } = useI18n()

let windowWidth = ref(window.innerWidth)
window.addEventListener("resize", () => {
	windowWidth.value = window.innerWidth
})

const props = defineProps<{
	allowRegistration: boolean
	beginGamePhase: Date
	competition: Competition
}>()
</script>

<style scoped></style>
