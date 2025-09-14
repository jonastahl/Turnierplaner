<template>
	<div class="grid">
		<div class="col-4 flex flex-column gap-3">
			<Card>
				<template #title>
					<div class="flex flex-row justify-content-between">
						<div>Teams</div>
						<SplitButton
							:model="randomizeItems"
							:disabled="isUpdating"
							class="w-fit"
							:label="t('ViewPrepare.editTeams.randomize')"
							@click="randomize"
						>
							<template #icon>
								<span class="material-symbols-outlined mb-1 symbols-prep"
									>casino</span
								>
							</template>
						</SplitButton>
					</div>
				</template>
				<template #content>
					<TeamContainerDraggable
						v-if="competition"
						:animated="isUpdating"
						:teams="data.teams"
						:competition="competition"
					/>
				</template>
			</Card>
		</div>
		<div class="col-8 flex flex-column gap-3">
			<Card v-for="(group, i) in data.groups" :key="i">
				<template #title
					>{{ t("ViewPrepare.assignMatches.Group") }} {{ i + 1 }}
				</template>
				<template #content>
					<TeamContainerDraggable
						v-if="competition"
						:animated="isUpdating"
						:teams="group"
						:competition="competition"
					/>
				</template>
			</Card>
			<div class="flex flex-row gap-2">
				<Button
					:label="t('ViewPrepare.assignMatches.remove')"
					:disabled="data.noGroups <= 1 || isUpdating"
					severity="danger"
					@click="
						() => {
							data.noGroups /= 2
							adjustSize(data.noGroups)
						}
					"
				></Button>
				<Button
					:label="t('ViewPrepare.assignMatches.add')"
					:disabled="data.noGroups >= 64 || isUpdating"
					severity="success"
					@click="
						() => {
							data.noGroups *= 2
							adjustSize(data.noGroups)
						}
					"
				></Button>
			</div>
		</div>
	</div>
</template>

<script setup lang="ts">
import { useRoute } from "vue-router"
import { useI18n } from "vue-i18n"
import { useToast } from "primevue/usetoast"
import { Ref } from "vue"
import { Team } from "@/interfaces/team"
import { getCompetitionDetails } from "@/backend/competition"
import { getSignedUp } from "@/backend/signup"
import { getGroupsDivision, useInitGroups } from "@/backend/group"
import TeamContainerDraggable from "@/components/pages/management/prepare/components/TeamContainerDraggable.vue"
import { sleep, track } from "@/backend/Tracker"
import {
	genRandomizeItems,
	getDelays,
	selectRandomElement,
} from "@/components/pages/management/prepare/assignMatches/AssginMatchesHelper"

const route = useRoute()
const toast = useToast()
const { t } = useI18n()
const randomizeItems = genRandomizeItems(t, reroll, reset)

const isUpdating = defineModel<boolean>("isUpdating", { default: false })

const { data: competition } = getCompetitionDetails(route, t, toast)
const { data: signedUpTeams, isLoading: signedUpLoading } = getSignedUp(
	route,
	t,
	toast,
)
const { data: groupsServer, isLoading: groupsLoading } = getGroupsDivision(
	route,
	t,
	toast,
)
const { mutate: initGroups } = useInitGroups(route, t, toast)

const { data, reload } = track(
	loadFromServer,
	{
		teams: [],
		groups: [[], []],
		teamCount: 0,
		noGroups: 2,
	},
	{ signedUpTeams, groupsServer },
	[signedUpTeams, groupsServer],
	[signedUpLoading, groupsLoading],
	isUpdating,
	400,
)

function loadFromServer({
	signedUpTeams,
	groupsServer,
}: {
	signedUpTeams: Ref<undefined> | Ref<Team[]>
	groupsServer: Ref<undefined> | Ref<Team[][]>
}) {
	if (!signedUpTeams.value || !groupsServer.value) return null

	const groups: Team[][] = JSON.parse(
		JSON.stringify(groupsServer.value ?? [[], []]),
	).map((g: Team[]) => g.sort())
	const teams: Team[] = signedUpTeams.value
		.filter((t) => !groups.some((group) => group.some((st) => st.id === t.id)))
		.sort()
	return {
		teams,
		groups,
		teamCount: signedUpTeams.value.length,
		noGroups: groups.length,
	}
}

const { delay, delayBetween } = getDelays(data)

async function randomize() {
	isUpdating.value = true
	while (data.value.teams.length) {
		const min = Math.min(...data.value.groups.map((group) => group.length))
		const minInd = data.value.groups.findIndex((group) => group.length === min)

		const element = selectRandomElement(data.value.teams)
		await sleep(delayBetween.value)
		if (element) data.value.groups[minInd].push(element)
		await sleep(delay.value)
	}
	isUpdating.value = false
}

async function reset() {
	isUpdating.value = true
	for (const group of data.value.groups) {
		while (group.length) {
			let i = group.length - 1
			const team = group.splice(i, 1)[0]
			await sleep(delayBetween.value)
			data.value.teams.push(team)
			await sleep(delay.value)
		}
	}
	isUpdating.value = false
}

async function reroll() {
	await reset()
	await randomize()
}

function save(complete = false) {
	if (isUpdating.value) return

	if (data.value.groups.some((g) => g.length <= 1)) {
		toast.add({
			severity: "error",
			summary: t("ViewPrepare.assignMatches.emptyGroupSum"),
			detail: t("ViewPrepare.assignMatches.emptyGroupDet"),
			life: 3000,
			closable: false,
		})
		return
	}

	initGroups({ complete, groups: data.value.groups })
}

function adjustSize(size: number) {
	isUpdating.value = true
	if (data.value.groups.length > size) {
		for (let i = size; i < data.value.groups.length; i++) {
			data.value.groups[i].forEach((e) => data.value.teams.push(e))
		}
		data.value.groups.splice(size, data.value.groups.length - size)
	} else {
		data.value.groups.splice(
			data.value.groups.length,
			0,
			...Array.from({ length: size - data.value.groups.length }, () => []),
		)
	}
	isUpdating.value = false
}

defineExpose({ save, reload })
</script>

<style scoped></style>
