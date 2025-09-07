<template>
	<template v-if="!!route.params.compId">
		<ViewPrepareSteps
			class="antiCard mb-6"
			:active-step="<number>route.meta.step"
		/>
		<template v-if="competition">
			<router-view
				v-if="progressOrder(competition.cProgress) <= 3"
				v-slot="{ Component }"
			>
				<component :is="Component" ref="curPrepStep" />
			</router-view>
			<template v-else-if="competition.cProgress === Progress.PUBLISHING">
				<div class="flex flex-column gap-3">
					<span>
						{{ t("ViewPrepare.prepared_not_published") }}
					</span>
					<div class="flex flex-row">
						<Button
							severity="danger"
							:label="t('general.reset')"
							@click="resetDialog = true"
						/>
					</div>
				</div>
			</template>
			<template v-else-if="competition.cProgress === Progress.DONE">
				<span>
					{{ t("ViewPrepare.prepared_published") }}
				</span>
			</template>
		</template>
	</template>
	<ViewPrepareOverview v-else />
	<DialogResetProgress v-model="resetDialog" />
</template>

<script setup lang="ts">
import { useRoute, useRouter } from "vue-router"
import ViewPrepareOverview from "@/components/views/compManage/prepare/ViewPrepareOverview.vue"
import { ref, watch } from "vue"
import ViewPrepareSteps from "@/components/views/compManage/prepare/ViewPrepareSteps.vue"
import { getCompetitionDetails } from "@/backend/competition"
import { useI18n } from "vue-i18n"
import { useToast } from "primevue/usetoast"
import { Progress, progressOrder } from "@/interfaces/competition"
import DialogResetProgress from "@/components/views/compManage/prepare/DialogResetProgress.vue"
import { Routes } from "@/routes"

const route = useRoute()
const router = useRouter()
const { t } = useI18n()
const toast = useToast()

const { data: competition } = getCompetitionDetails(route, t, toast)

const resetDialog = ref(false)

watch(
	[route, competition],
	() => {
		if (!competition.value) return

		switch (competition.value.cProgress) {
			case Progress.TEAMS:
				router.replace({
					name: Routes.EditTeams,
				})
				break
			case Progress.GAMES:
				router.replace({
					name: Routes.AssignMatches,
				})
				break
			case Progress.SCHEDULING:
				router.replace({
					name: Routes.ScheduleMatches,
				})
				break
			default:
				router.replace({
					name: Routes.ManagePrepare,
					params: {
						compId: route.params.compId,
					},
				})
		}
	},
	{ immediate: true },
)
</script>

<style scoped>
.antiCard {
	margin-left: -1.25rem;
	margin-right: -1.25rem;
}
</style>
