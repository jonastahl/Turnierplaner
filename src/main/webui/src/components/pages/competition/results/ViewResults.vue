<template>
	<Suspense>
		<ViewGroupSystem
			v-if="props.tourType === CompType.GROUPS"
			:number-sets="props.numberSets"
		/>
		<ViewKnockoutSystem
			v-else-if="props.tourType === CompType.KNOCKOUT"
			:mode="props.mode"
			:number-sets="props.numberSets"
		/>
		<p v-else>Invalid type</p>
	</Suspense>
</template>

<script lang="ts" setup>
import ViewGroupSystem from "@/components/pages/competition/results/group/ViewGroupSystem.vue"
import ViewKnockoutSystem from "@/components/pages/competition/results/knockout/ViewKnockoutSystem.vue"
import { Mode, NumberSets, CompType } from "@/interfaces/competition"

const props = defineProps<{
	tourType: CompType
	mode: Mode
	numberSets: NumberSets
}>()
</script>

<style scoped></style>
