<template>
	<div class="w-full p-2">
		<Card style="margin-top: -10px !important">
			<template #header>
				<TabMenu
					:active-index="activeTab"
					:model="menuComps"
					@tab-change="tabChange"
				>
					<template #item="{ item, props }">
						<div v-bind="props.action" class="cursor-pointer">
							<span v-if="!item.overview" v-bind="props.label">{{
								item.name
							}}</span>
							<strong v-else v-bind="props.label">{{
								t("ViewPrepare.overview")
							}}</strong>
						</div>
					</template>
				</TabMenu>
				<ViewPrepareSteps
					v-if="route.name !== 'Prepare competition overview'"
					:active-step="<number>route.meta.step - 1"
					class="mt-5"
				/>
			</template>
			<template #content>
				<template v-if="route.name !== 'Prepare competition overview'">
					<router-view v-slot="{ Component }">
						<component
							:is="Component"
							ref="curPrepStep"
							v-model:is-updating="isUpdating"
						/>
					</router-view>
				</template>
				<ViewPrepareOverview v-else />
			</template>
			<template #footer>
				<div
					v-if="route.name !== 'Prepare competition overview'"
					class="mt-2 grid grid-nogutter justify-content-between"
				>
					<Button
						:style="{
							visibility: route.meta.step !== 1 ? 'visible' : 'hidden',
						}"
						:disabled="route.meta.step === 1 || isUpdating"
						:label="t('general.back')"
						icon="pi pi-angle-left"
						icon-pos="left"
						@click="prevPage"
					/>
					<Button
						v-if="route.meta.reset"
						:disabled="isUpdating"
						:label="t('general.reset')"
						severity="danger"
						@click="reset"
					/>
					<Button
						:disabled="isUpdating"
						v-if="route.meta.step !== 5"
						:label="t('general.save')"
						severity="success"
						@click="save"
					/>
					<Button
						v-if="<number>route.meta.step !== 4"
						:disabled="
							isUpdating ||
							!competition ||
							<number>route.meta.step >= progressOrder(competition.cProgress)
						"
						icon="pi pi-angle-right"
						icon-pos="right"
						:label="t('general.next')"
						@click="nextPage"
					/>
					<Button
						v-else
						:disabled="
							isUpdating ||
							!competition ||
							<number>route.meta.step >= progressOrder(competition.cProgress)
							"
						label="Complete"
						icon="pi pi-check"
						icon-pos="right"
						class="p-button-success"
						@click="nextPage"
					/>
				</div>
			</template>
		</Card>
	</div>
</template>

<script setup lang="ts">
import { useRoute, useRouter } from "vue-router"
import { computed, inject, ref, watch } from "vue"
import { useI18n } from "vue-i18n"
import { useToast } from "primevue/usetoast"
import { TabMenuChangeEvent } from "primevue/tabmenu"
import {
	getCompetitionDetails,
	getCompetitionsList,
} from "@/backend/competition"
import { Progress, progressOrder } from "@/interfaces/competition"
import Button from "primevue/button"
import ViewEditTeams from "@/components/views/compManage/prepare/editTeams/ViewEditTeams.vue"
import ViewPrepareOverview from "@/components/views/compManage/prepare/ViewPrepareOverview.vue"
import ViewPrepareSteps from "@/components/views/compManage/prepare/ViewPrepareSteps.vue"

const { t } = useI18n()

const router = useRouter()
const route = useRoute()
const toast = useToast()

const isUpdating = ref(false)
const curPrepStep = ref<InstanceType<typeof ViewEditTeams> | null>()

const isLoggedIn = inject("loggedIn", ref(false))
const { data: competitions } = getCompetitionsList(route, isLoggedIn, t, toast)
const { data: competition } = getCompetitionDetails(route, t, toast)
const activeTab = ref<number>(0)
watch([competitions, route], () => updateRoute(), { immediate: true })

const menuComps = computed(() => {
	if (!competitions.value) return []

	return [
		{
			disabled: false,
			overview: true,
		},
		...competitions.value.map((competition) => {
			return {
				disabled:
					progressOrder(competition.cProgress) < <number>route.meta.step,
				...competition,
			}
		}),
	]
})

/**
 * Updates the route defaulting to the overview page
 * @param compId null equals the overview page
 */
function updateRoute(compId?: string | null) {
	if (!competitions.value || competitions.value.length === 0) return

	if (compId === undefined) compId = <string>route.params.compId
	if (!competitions.value.find((c) => c.name === compId)) compId = null

	activeTab.value =
		compId === null
			? 0
			: competitions.value.findIndex((c) => c.name === compId) + 1

	if (compId === null) {
		router.replace({
			name: "Prepare competition overview",
			params: { tourId: route.params.tourId, compId },
		})
	} else {
		let step = route.name
		if (
			competition.value &&
			<number>route.meta.step > progressOrder(competition.value.cProgress)
		) {
			toast.add({
				severity: "error",
				summary: t("ViewPrepare.steps.invalid_step.summary"),
				detail: t("ViewPrepare.steps.invalid_step.detail"),
				life: 3000,
			})
			step = undefined
		}
		if (route.meta.step === undefined || !step) {
			switch (competitions.value[activeTab.value - 1].cProgress) {
				case Progress.TEAMS:
					step = "editTeams"
					break
				case Progress.GAMES:
					step = "scheduleMatches"
					break
				case Progress.SCHEDULING:
					step = "scheduleMatches"
					break
				default:
					step = "settings"
			}
			step = "editTeams"
		}

		router.replace({
			name: step,
			params: { tourId: route.params.tourId, compId },
		})
	}
}

function tabChange(event: TabMenuChangeEvent) {
	if (!competitions.value) return

	activeTab.value = event.index
	updateRoute(event.index > 0 ? competitions.value[event.index - 1].name : null)
}

function prevPage() {
	if (curPrepStep.value) curPrepStep.value.prevPage()
}

function reset() {
	if (curPrepStep.value) curPrepStep.value.reset()
}

function save() {
	if (curPrepStep.value) curPrepStep.value.save()
}

function nextPage() {
	if (curPrepStep.value) curPrepStep.value.nextPage()
}
</script>

<style scoped>
::v-deep(b) {
	display: block;
}

::v-deep(.p-card-body) {
	padding: 2rem;
}
</style>
