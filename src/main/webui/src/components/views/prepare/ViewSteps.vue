<template>
	<Steps :active-step="props.activeStep" :model="stepList" :readonly="true" />
</template>

<script setup lang="ts">
import { computed } from "vue"
import { useI18n } from "vue-i18n"
import Steps from "primevue/steps"

const { t } = useI18n()

const props = withDefaults(
	defineProps<{
		activeStep: number
		overview?: boolean
	}>(),
	{
		overview: false,
	},
)

function $t(name: string) {
	return computed(() => t(name))
}

const stepList = computed(() => [
	...(props.overview
		? []
		: [
				{
					label: <string>(<unknown>$t("general.settings")),
					name: "first",
					index: 1,
				},
			]),
	{
		label: <string>(<unknown>$t("ViewPrepare.steps.editTeams")),
		name: "editTeams",
		index: 2,
	},
	{
		label: <string>(<unknown>$t("ViewPrepare.steps.assignMatches")),
		name: "assignMatches",
		index: 3,
	},
	{
		label: <string>(<unknown>$t("ViewPrepare.steps.scheduleMatches")),
		name: "scheduleMatches",
		index: 4,
	},
	{
		label: <string>(<unknown>$t("ViewPrepare.steps.done")),
		name: "done",
		index: 5,
	},
])
</script>

<style scoped></style>
