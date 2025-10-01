<template>
	<div class="grid">
		<div class="col-fixed" style="width: 300px">
			<div class="text-center p-3 border-round-sm bg-primary font-bold">
				<template v-if="props.team?.playerA">
					{{ props.team?.playerA?.name }}
					{{ props.team?.playerB?.name }}
				</template>
				<template v-else> ??? </template>
			</div>
		</div>
		<div
			v-if="gamePoints"
			class="col flex flex-row flex-wrap gap-2"
			style="width: 100%"
		>
			<InputNumber
				v-for="(p, i) in gamePoints"
				:key="p"
				v-model="gamePoints[i]"
				:max="100"
				:min="0"
				:invalid="props.errors.includes(i)"
				:pt="{
					input: {
						root: { style: 'width: 120px; height: 50px', class: 'text-center' },
					},
				}"
				class="p-inputnumber-input"
				input-style-class="p-inputnumber"
				input-id="minmax-buttons"
				mode="decimal"
				style-class="p-inputnumber"
			/>
		</div>
	</div>
</template>

<script setup lang="ts">
import { Team } from "@/interfaces/team"

const gamePoints = defineModel("gamePoints", { type: Array<number> })
const props = defineProps<{
	team: Team | undefined | null
	errors: number[]
}>()
</script>

<style scoped></style>
