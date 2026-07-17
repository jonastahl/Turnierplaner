<template>
	<!-- TODO place button to registration -->
	<div class="w-full flex flex-column align-items-center">
		<Card class="w-full md:w-30rem">
			<template #content>
				<strong>{{ t("general.description") }}:</strong>
				{{ props.competition.description }}<br />
				<strong>{{ t("competition.type.label") }}:</strong>
				{{
					t(
						"competition.type.options." +
							props.competition.tourType.toLowerCase(),
					)
				}}<br />
				<strong>{{ t("competition.mode.label") }}:</strong>
				{{
					t("competition.mode.options." + props.competition.mode.toLowerCase())
				}}
			</template>
		</Card>

		<!-- Show registration is over -->
		<Card class="mt-2 mb-2 w-12 md:w-10 lg:w-8 xl:w-7">
			<template #content>
				<p v-if="!allowRegistration" style="text-align: center">
					{{ t("competition.action.signup.state.over") }}
				</p>
				<template v-else>
					<div class="grid ml-0 mr-0">
						<span class="col-6">
							{{ t("competition.action.signup.warning.register_before") }}
						</span>
						<Button
							class="col-6"
							:label="t('player.action.create.label')"
							@click="router.push({ name: Routes.CreatePlayer })"
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
