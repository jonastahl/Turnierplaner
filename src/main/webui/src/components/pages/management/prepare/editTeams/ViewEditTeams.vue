<template>
	<div class="flex flex-column gap-5">
		<div
			v-if="tournamentSuc && tournament && competition"
			class="grid"
			style="flex-wrap: nowrap"
		>
			<PlayerList
				id="playerA"
				:competition="competition"
				:players="playersA"
				:tournament="tournament"
				:class="{
					'col-4':
						competition.mode === Mode.DOUBLE && competition.playerB.different,
					'col-6':
						competition.mode === Mode.DOUBLE && !competition.playerB.different,
					'col-12': competition.mode === Mode.SINGLE,
				}"
				class="transition-duration-200"
				group="playersA"
				title="Players A"
				:is-updating="isUpdating"
			/>
			<TeamList
				v-if="competition.mode === Mode.DOUBLE"
				:animated="isUpdating"
				:class="{
					'col-4': competition.playerB.different,
					'col-6': !competition.playerB.different,
				}"
				:competition="competition"
				:teams="teams"
				class="transition-duration-200"
			>
				<SplitButton
					:disabled="isUpdating"
					:model="randomizeItems"
					class="w-fit"
					:label="t('ViewPrepare.editTeams.randomize')"
					@click="randomize"
				>
					<template #icon>
						<span
							class="material-symbols-outlined mb-1 symbols-prep"
							style="font-size: 1.2rem"
							>casino</span
						>
					</template>
				</SplitButton>
			</TeamList>
			<PlayerList
				v-if="competition.mode === Mode.DOUBLE && competition.playerB.different"
				id="playerB"
				:competition="competition"
				:is-updating="isUpdating"
				:players="playersB"
				:secondary="true"
				:tournament="tournament"
				class="col-4 overflow-hidden"
				group="playersB"
				title="Players B"
			/>
		</div>
		<div v-else class="grid">
			<div class="col-4">
				<Skeleton class="h-30rem" />
			</div>
			<div class="col-4">
				<Skeleton class="h-30rem" />
			</div>
			<div class="col-4">
				<Skeleton class="h-30rem" />
			</div>
		</div>
	</div>
	<NavigationButtons
		:reset="false"
		reload
		:loading="isUpdating"
		@reload="reload"
		@save="save"
		@complete="save(true)"
	/>
</template>

<script lang="ts" setup>
import { useRoute } from "vue-router"
import { useToast } from "primevue/usetoast"
import { useI18n } from "vue-i18n"
import { getTournamentDetails } from "@/backend/tournament"
import PlayerList from "@/components/pages/management/prepare/editTeams/PlayerList.vue"
import { computed, ref, Ref, watch } from "vue"
import { Player } from "@/interfaces/player"
import {
	TeamArray,
	teamArrayClientToServer,
	teamToArray,
} from "@/interfaces/team"
import { Mode } from "@/interfaces/competition"
import TeamList from "@/components/pages/management/prepare/editTeams/TeamList.vue"
import { v4 as uuidv4 } from "uuid"
import { getCompetitionDetails } from "@/backend/competition"
import { getSignedUpSeparated, useUpdateTeams } from "@/backend/signup"
import NavigationButtons from "@/components/pages/management/prepare/components/NavigationButtons.vue"

const route = useRoute()
const toast = useToast()
const { t } = useI18n()

function $t(name: string) {
	return computed(() => t(name))
}

const isUpdating = ref(false)

const teams = ref<TeamArray[]>([])
const playersA = ref<Player[]>([])
const playersB = ref<Player[]>([])

const { mutate: updateTeams } = useUpdateTeams(route, t, toast)
const { data: tournament, isSuccess: tournamentSuc } = getTournamentDetails(
	route,
	t,
	toast,
)
const { data: competition } = getCompetitionDetails(route, t, toast)
const { data: signedUpTeams, isPlaceholderData: signedUpPlaceholder } =
	getSignedUpSeparated(route, t, toast)

function sleep(milliseconds: number) {
	return new Promise((resolve) => setTimeout(resolve, milliseconds))
}

const duration = 2000
const playerCount = ref(0)
const delay = computed(() =>
	Math.min((duration * 2) / 3 / playerCount.value, 100),
)
const delayBetween = computed(() => delay.value / 2)

function selectRandomElement<T>(players: Ref<T[]>) {
	const r = Math.floor(Math.random() * players.value.length)
	const element = players.value[r]
	players.value.splice(r, 1)
	return element
}

async function randomize() {
	if (!competition.value) return
	isUpdating.value = true

	// first fill up all existing teams
	for (const i in teams.value) {
		if (!teams.value[i].playerA.length && playersA.value.length > 0) {
			const element = selectRandomElement(playersA)
			await sleep(delayBetween.value)
			teams.value[i].playerA.push(element)
			await sleep(delay.value)
		}
		if (!teams.value[i].playerB.length) {
			if (competition.value.playerB.different && playersB.value.length > 0) {
				const element = selectRandomElement(playersB)
				await sleep(delayBetween.value)
				teams.value[i].playerB.push(element)
				await sleep(delay.value)
			} else if (playersA.value.length > 0) {
				const element = selectRandomElement(playersA)
				await sleep(delayBetween.value)
				teams.value[i].playerB.push(element)
				await sleep(delay.value)
			}
		}
	}
	// create teams for all the remaining teams
	if (competition.value.playerB.different) {
		while (playersA.value.length > 0 && playersB.value.length > 0) {
			const elementA = selectRandomElement(playersA)
			const elementB = selectRandomElement(playersB)
			await sleep(delayBetween.value)
			teams.value.push({
				id: uuidv4(),
				playerA: [elementA],
				playerB: [elementB],
			})
			await sleep(delay.value)
		}
	} else {
		while (playersA.value.length >= 2) {
			const element1 = selectRandomElement(playersA)
			const element2 = selectRandomElement(playersA)
			await sleep(delayBetween.value)
			teams.value.push({
				id: uuidv4(),
				playerA: [element1],
				playerB: [element2],
			})
			await sleep(delay.value)
		}
	}

	isUpdating.value = false
}

async function clearTeams() {
	if (!competition.value) return
	isUpdating.value = true

	while (teams.value.length > 0) {
		let i = teams.value.length - 1
		const elementA = teams.value[i].playerA
		if (elementA.length) {
			teams.value[i].playerA = []
			await sleep(delayBetween.value)
			playersA.value.push(elementA[0])
			await sleep(delay.value)
		}
		const elementB = teams.value[i].playerB
		if (elementB.length) {
			teams.value[i].playerB = []
			await sleep(delayBetween.value)
			if (competition.value.playerB.different) playersB.value.push(elementB[0])
			else playersA.value.push(elementB[0])
			await sleep(delay.value)
		}
		teams.value.splice(i, 1)
		await sleep(delay.value)
	}
	isUpdating.value = false
}

async function reroll() {
	await clearTeams()
	await randomize()
}

const randomizeItems = ref([
	{
		label: <string>(<unknown>$t("ViewPrepare.editTeams.reroll")),
		icon: "pi pi-refresh",
		command: reroll,
	},
	{
		label: <string>(<unknown>$t("ViewPrepare.editTeams.reset")),
		icon: "pi pi-times",
		command: clearTeams,
	},
])

async function reload() {
	await loadFromServer()

	toast.add({
		severity: "info",
		summary: t("general.reload"),
		detail: t("ViewPrepare.restored"),
		life: 3000,
	})
}

function save(complete = false) {
	const t = teams.value.map(teamArrayClientToServer)
	playersA.value.forEach((player) =>
		t.push({
			id: uuidv4(),
			playerA: player,
			playerB: null,
		}),
	)
	playersB.value.forEach((player) =>
		t.push({
			id: uuidv4(),
			playerA: null,
			playerB: player,
		}),
	)
	updateTeams({ complete, teams: t })
}

let firstUpdate = true
// extensive anti racing
let loadingFromServer = false
let rerunLoadFromServer = false

async function loadFromServer() {
	if (!signedUpTeams.value || signedUpPlaceholder.value) return

	if (loadingFromServer) {
		rerunLoadFromServer = true
		return
	}
	loadingFromServer = true

	isUpdating.value = true
	teams.value = []
	playersA.value = []
	playersB.value = []
	const anFin = sleep(firstUpdate ? 0 : 300)
	firstUpdate = false
	await anFin
	playerCount.value =
		signedUpTeams.value.playersA.length +
		signedUpTeams.value.playersB.length +
		signedUpTeams.value.teams.length
	playersA.value.push(...signedUpTeams.value.playersA)
	playersB.value.push(...signedUpTeams.value.playersB)
	teams.value.push(...signedUpTeams.value.teams.map(teamToArray))
	await sleep(300)
	isUpdating.value = false
	loadingFromServer = false
	if (rerunLoadFromServer) {
		rerunLoadFromServer = false
		await loadFromServer()
	}
}

watch(signedUpTeams, loadFromServer)
if (!signedUpPlaceholder.value) loadFromServer()
</script>

<style scoped></style>
