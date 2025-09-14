<template>
	<MultiSelect
		v-model="selectedCourts"
		:loading="!courts || !selectedCourts"
		:options="courts?.toSorted(courtComp)"
		option-label="name"
		:placeholder="t('court.select')"
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
import ViewCreateCourtSmall from "@/components/pages/court/ViewCreateCourtSmall.vue"
import { Court } from "@/interfaces/court"
import { courtComp, getCourts } from "@/backend/court"
import { watch } from "vue"
import { useI18n } from "vue-i18n"

const { t } = useI18n()

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
