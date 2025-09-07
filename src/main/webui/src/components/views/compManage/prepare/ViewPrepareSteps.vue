<template>
	<Steps
		:active-step="props.activeStep - 1"
		:model="stepList"
		:readonly="true"
	/>
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
	{
		label: <string>(<unknown>$t("ViewPrepare.steps.editTeams")),
		name: "editTeams",
	},
	{
		label: <string>(<unknown>$t("ViewPrepare.steps.assignMatches")),
		name: "assignMatches",
	},
	{
		label: <string>(<unknown>$t("ViewPrepare.steps.scheduleMatches")),
		name: "scheduleMatches",
	},
	...(props.overview
		? [
				{
					label: <string>(<unknown>$t("ViewPrepare.steps.publishing")),
					name: "publishing",
				},
				{
					label: <string>(<unknown>$t("ViewPrepare.steps.done")),
					name: "done",
				},
			]
		: []),
])
</script>

<style scoped></style>
