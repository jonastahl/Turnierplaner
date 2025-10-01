<template>
	<Dialog
		v-model:visible="visible"
		style="width: 70rem"
		:header="t('DialogUpdateScore.header')"
		modal
	>
		<span class="text-surface-500 dark:text-surface-400 block mb-2">
			{{ t("DialogUpdateScore.update_score") }}
		</span>
		<div class="grid">
			<div class="col-fixed" style="width: 300px"></div>
			<div
				v-for="n in numberSets"
				:key="n"
				class="col-fixed flex justify-content-center flex-wrap"
				style="width: 140px"
			>
				<RadioButton
					v-model="selectedSet"
					:input-id="n.toString()"
					name="sets"
					:value="n - 1"
				/>
				<label class="ml-2" :for="n.toString()">Set {{ n }}</label>
			</div>
		</div>
		<PlayerPointRow
			v-if="visible"
			v-model:game-points="teamAGamePoints"
			:team="currentMatch?.teamA"
			:errors="errors"
		/>
		<PlayerPointRow
			v-if="visible"
			v-model:game-points="teamBGamePoints"
			:team="currentMatch?.teamB"
			:errors="errors"
		/>
		<divider />
		<div class="flex flex-row w-12 justify-content-between mb-2">
			<template v-for="n in curTill + 1" :key="n">
				<Button
					v-if="n - 1 <= curTill - 2"
					:label="`${curTill}:${n - 1}`"
					@click="updatePoints(curTill, n - 1)"
				/>
				<Button
					v-else-if="curTill <= 6"
					:label="`${curTill + 1}:${n - 1}`"
					@click="updatePoints(curTill + 1, n - 1)"
				/>
			</template>
		</div>
		<div class="flex flex-row w-12 justify-content-between">
			<template v-for="n in curTill + 1" :key="n">
				<Button
					v-if="n - 1 <= curTill - 2"
					:label="`${n - 1}:${curTill}`"
					@click="updatePoints(n - 1, curTill)"
				/>
				<Button
					v-else-if="curTill <= 6"
					:label="`${n - 1}:${curTill + 1}`"
					@click="updatePoints(n - 1, curTill + 1)"
				/>
			</template>
		</div>
		<divider />
		<div class="flex justify-content-end flex-wrap gap-2">
			<Button
				label="Cancel"
				severity="secondary"
				type="button"
				@click="cancel"
			></Button>
			<Button label="Save" type="button" @click="savePoints"></Button>
		</div>
	</Dialog>
</template>
<script lang="ts" setup>
import { Match } from "@/interfaces/match"
import { computed, ref } from "vue"
import { useI18n } from "vue-i18n"
import PlayerPointRow from "@/components/pages/competition/reporting/PlayerPointRow.vue"
import { NumberSets } from "@/interfaces/competition"
import { checkSets, useUpdateSet } from "@/backend/set"
import { useRoute } from "vue-router"
import { useToast } from "primevue/usetoast"

const { t } = useI18n()
const toast = useToast()
const route = useRoute()

const props = defineProps<{
	numberSets: NumberSets
}>()

const numberSets = computed(() =>
	props.numberSets === NumberSets.THREE ? 3 : 5,
)

const currentMatch = ref<Match | null>(null)
const visible = ref(false)
const selectedSet = ref(0)
const curTill = computed(() =>
	selectedSet.value === numberSets.value - 1 ? 10 : 6,
)

const teamAGamePoints = ref<number[]>([])
const teamBGamePoints = ref<number[]>([])

const errors = ref<number[]>([])

const showPopUp = function (match: Match) {
	selectedSet.value = 0
	currentMatch.value = match
	teamAGamePoints.value = match.sets?.map((set) => set.scoreA) ?? []
	teamBGamePoints.value = match.sets?.map((set) => set.scoreB) ?? []
	// fill the rest up with 0
	for (let i = teamAGamePoints.value.length; i < numberSets.value; i++) {
		teamAGamePoints.value.push(0)
		teamBGamePoints.value.push(0)
	}
	visible.value = true
}
const { mutate: updateSet } = useUpdateSet(route, t, toast)

const cancel = function () {
	visible.value = false
}

const savePoints = function () {
	if (!currentMatch.value || !currentMatch.value.id) {
		return
	}
	errors.value = checkSets(getAllSets(), numberSets.value)
	if (errors.value.length > 0) {
		toast.add({
			severity: "error",
			summary: t("general.failure"),
			detail: t("set.invalid_sets"),
			life: 3000,
		})
		selectedSet.value = errors.value[0]
		return
	}

	const sets = getAllChangedSets()
	if (sets.length > 0)
		updateSet({
			matchId: currentMatch.value!.id,
			sets: sets,
		})
	visible.value = false
}

// return list of sets that contain all set that differ from the beginning
function getAllChangedSets() {
	let changedSets = []
	for (let i = 0; i < numberSets.value; i++) {
		// case set does not exist yet
		if (
			!currentMatch.value?.sets?.length ||
			i > currentMatch.value?.sets?.length - 1
		) {
			// don't create a new set for 0 points
			if (teamAGamePoints.value[i] !== 0 || teamBGamePoints.value[i] !== 0) {
				changedSets.push({
					index: i,
					scoreA: teamAGamePoints.value[i],
					scoreB: teamBGamePoints.value[i],
				})
			}
			continue
		}
		// you can set a existing set to 0, additionally check that the value is not the same as before
		const a = currentMatch.value?.sets?.[i].scoreA
		const b = currentMatch.value?.sets?.[i].scoreB
		if (teamAGamePoints.value[i] !== a || teamBGamePoints.value[i] !== b) {
			changedSets.push({
				index: i,
				scoreA: teamAGamePoints.value[i],
				scoreB: teamBGamePoints.value[i],
			})
		}
	}
	return changedSets
}

function getAllSets() {
	let sets = []
	for (let i = 0; i < numberSets.value; i++) {
		sets.push({
			index: i,
			scoreA: teamAGamePoints.value[i],
			scoreB: teamBGamePoints.value[i],
		})
	}
	return sets
}

function updatePoints(point1: number, point2: number) {
	teamAGamePoints.value[selectedSet.value] = point1
	teamBGamePoints.value[selectedSet.value] = point2
	if (selectedSet.value < numberSets.value - 1) selectedSet.value++
}

defineExpose({
	showPopUp,
})
</script>

<style scoped>
.p-inputnumber input {
	width: 100%;
}

.p-inputnumber-input input {
	width: 100%;
}
</style>
