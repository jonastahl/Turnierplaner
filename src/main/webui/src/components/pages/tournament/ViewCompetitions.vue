<template>
	<div id="container">
		<div id="content">
			<div id="competitions">
				<template v-if="competitions === null">
					<Skeleton v-for="i in Array(5)" :key="i" class="w-23rem h-12rem" />
				</template>
				<item
					v-for="competition in competitions"
					v-else
					:key="competition.id === null ? undefined : competition.id"
					:can-edit="!!canEdit"
					:description="competition.description"
					:name="competition.name"
					:type="competition.tourType"
					@selected="selected"
					@settings="settingsItem"
				/>
				<AddItem
					v-if="canEdit"
					:title="t('ViewCompetitions.add_title')"
					:content="t('ViewCompetitions.add_content')"
					@selected="addCompetition"
				/>
			</div>
			<div id="aside">
				<template v-if="isLoading || !data">
					<Skeleton class="h-2rem mb-2" />
					<Skeleton class="h-2rem" />
				</template>
				<Timeline v-else id="progress" op :value="status">
					<template #marker="slotProps">
						<span
							class="progress-icon"
							:style="{ borderColor: slotProps.item.color }"
						>
							<i
								class="pi"
								:class="[slotProps.item.icon]"
								:style="{ color: slotProps.item.color }"
							></i>
						</span>
					</template>
					<template #content="slotProps">
						<div style="height: 3px"></div>
						<template v-if="slotProps.item.registration">
							<strong>{{ t("TournamentSettings.registration_phase") }}</strong>
							<br />
							<strong>{{ t("ViewCompetitions.from") }}</strong>
							{{ formatDate(data?.registration_phase.begin) }}<br />
							<strong>{{ t("ViewCompetitions.till") }}</strong>
							{{ formatDate(data?.registration_phase.end) }}
						</template>
						<template v-else>
							<strong>{{ t("TournamentSettings.game_phase") }}</strong
							><br />
							<strong>{{ t("ViewCompetitions.from") }}</strong>
							{{ formatDate(data?.game_phase.begin) }}<br />
							<strong>{{ t("ViewCompetitions.till") }}</strong>
							{{ formatDate(data?.game_phase.end) }}
						</template>
					</template>
				</Timeline>
				<Button
					v-if="isLoggedIn"
					class="button"
					:label="t('ViewCompetition.manage') + ' >>'"
					@click="gotoManage"
				/>
				<Button
					v-if="openRegistration"
					class="button"
					:label="t('general.register') + ' >>'"
					@click="router.push({ name: Routes.PlayerRegistration })"
				/>
				<Button
					class="button"
					:label="t('general.matches') + ' >>'"
					@click="router.push({ name: Routes.MatchesOverview })"
				/>
			</div>
		</div>
	</div>
</template>

<script lang="ts" setup>
import Item from "./ItemCompetition.vue"
import { inject, ref, watch } from "vue"
import { useRoute } from "vue-router"
import AddItem from "@/components/items/ItemAdd.vue"
import { router } from "@/main"
import { useI18n } from "vue-i18n"
import Button from "primevue/button"
import Timeline from "primevue/timeline"
import { getCanEdit } from "@/backend/security"
import { getTournamentDetails } from "@/backend/tournament"
import { useToast } from "primevue/usetoast"
import { getCompetitionsList } from "@/backend/competition"
import { Routes } from "@/routes"

const { t } = useI18n()
const toast = useToast()

const route = useRoute()

const isLoggedIn = inject("loggedIn", ref(false))
const { data: canEdit } = getCanEdit(<string>route.params.tourId, isLoggedIn)
const { data: competitions } = getCompetitionsList(route, isLoggedIn, t, toast)

const options: Intl.DateTimeFormatOptions = {
	weekday: "long",
	year: "numeric",
	month: "long",
	day: "numeric",
	hour: "numeric",
	minute: "numeric",
}

const status = ref([
	{
		registration: true,
		icon: "pi-pencil",
		color: "#000000",
	},
	{
		registration: false,
		icon: "pi-play",
		color: "#000000",
	},
])

const openRegistration = ref(false)
const { data, isLoading } = getTournamentDetails(route, t, toast)

watch(
	data,
	() => {
		if (data.value === undefined) return

		const date = new Date()
		if (date > data.value.registration_phase.end) {
			status.value[0].color = "green"
			status.value[0].icon = "pi-check"
			openRegistration.value = false
		} else if (date > data.value.registration_phase.begin) {
			status.value[0].color = "blue"
			status.value[0].icon = "pi-pencil"
			openRegistration.value = true
		} else {
			status.value[0].color = "#000000"
			status.value[0].icon = "pi-pencil"
			openRegistration.value = false
		}
		if (date > data.value.game_phase.end) {
			status.value[1].color = "green"
			status.value[1].icon = "pi-check"
		} else if (date > data.value.game_phase.begin) {
			status.value[1].color = "blue"
			status.value[1].icon = "pi-play"
		} else {
			status.value[1].color = "#000000"
			status.value[1].icon = "pi-play"
		}
	},
	{ immediate: true },
)

function formatDate(d: Date) {
	return d.toLocaleString(t("lang"), options)
}

function selected(competition: string) {
	router.push({
		name: Routes.Competition,
		params: { tourId: route.params.tourId, compId: competition },
	})
}

function gotoManage() {
	if (competitions.value)
		router.push({
			name: Routes.ManageCompetition,
			params: { tourId: route.params.tourId },
		})
}

function settingsItem(competition: string) {
	router.push({
		name: Routes.ManageSettings,
		params: { tourId: route.params.tourId, compId: competition },
	})
}

function addCompetition() {
	router.push({
		name: Routes.CreateCompetition,
		params: { tourId: route.params.tourId },
	})
}
</script>

<style scoped>
#competitions {
	margin: 10px;
	display: flex;
	flex-wrap: wrap;
	flex-direction: row;
	justify-content: center;
	flex-shrink: 2;
}

#container {
	width: 100%;
}

#content {
	display: flex;
	flex-direction: row;
	justify-content: center;
}

#competitions > * {
	margin: 0 10px 10px 10px;
}

#aside {
	flex-shrink: 0.5;
	margin-top: 20px;
	margin-right: 10px;
	display: flex;
	flex-direction: column;
	justify-content: flex-start;
	align-items: flex-start;
	width: 25dvw;
	height: fit-content;
}

#progress {
	height: fit-content;
}

.button {
	margin-top: 20px;
	width: min(100%, 30rem);
}

h2 {
	text-align: center;
	font-size: 30px;
}

@media only screen and (max-width: 900px) {
	#competitions {
		width: auto;
		left: 0;
		right: 0;
		margin: 10px 0 10px 0;
	}
}

@media only screen and (max-width: 1300px) {
	#content {
		flex-direction: column-reverse;
	}

	#aside {
		padding: 10px;
		width: 100%;
	}
}

::v-deep(.p-timeline-event-opposite) {
	display: none;
}

.progress-icon {
	width: 2rem;
	height: 2rem;
	border: 2px solid;
	border-radius: 100%;
	box-sizing: border-box;
	display: flex;
	justify-content: center;
	align-items: center;
}

.progress-icon > i {
	font-weight: bold;
}
</style>
