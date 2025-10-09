<template>
	<div class="flex flex-row gap-3 w-full">
		<div
			class="w-6 border-round-sm bg-primary font-bold flex flex-row align-items-center justify-content-center"
		>
			<LinkTeamNames :team="props.team" inverted short />
		</div>
		<div v-if="gamePoints" class="w-6 flex flex-row gap-2">
			<InputNumber
				v-for="(p, i) in gamePoints"
				:key="p"
				v-model="gamePoints[i]"
				fluid
				:max="100"
				:min="0"
				:invalid="props.errors.includes(i)"
				:pt="{
					input: {
						root: {
							class: 'w-full text-center',
							tabindex: 2 * i + (props.second ? 1 : 0) + 1,
						},
					},
				}"
				input-id="minmax-buttons"
				mode="decimal"
				style="min-width: 3rem"
				@focusin="emit('selected', i)"
			/>
		</div>
	</div>
</template>

<script setup lang="ts">
import { Team } from "@/interfaces/team"
import LinkTeamNames from "@/components/links/LinkTeamNames.vue"

const gamePoints = defineModel("gamePoints", { type: Array<number> })
const props = defineProps<{
	team: Team | undefined | null
	errors: number[]
	second?: boolean
}>()

const emit = defineEmits<{
	selected: [number]
}>()
</script>

<style scoped></style>
