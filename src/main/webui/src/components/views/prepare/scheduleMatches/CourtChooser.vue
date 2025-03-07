<template>
	<MultiSelect
		v-model="selectedCourts"
		:loading="!courts || !selectedCourts"
		:options="courts?.toSorted(courtComp)"
		option-label="name"
		placeholder="Select courts"
		class="w-full"
	>
		<template #footer>
			<div class="w-full flex align-items-center p-2">
				<ViewCreateCourtSmall />
			</div>
		</template>
	</MultiSelect>
</template>

<script setup lang="ts">
import ViewCreateCourtSmall from "@/components/views/court/ViewCreateCourtSmall.vue"
import { Court } from "@/interfaces/court"
import { courtComp, getCourts } from "@/backend/court"
import { watch } from "vue"

const selectedCourts = defineModel<Court[]>({ default: [] })
const { data: courts } = getCourts()

watch(
	selectedCourts,
	() => {
		selectedCourts.value.sort(courtComp)
	},
	{ immediate: true },
)
</script>

<style scoped></style>
