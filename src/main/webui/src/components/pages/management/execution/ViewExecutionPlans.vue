<template>
	<div class="w-full flex justify-content-center">
		<template v-if="competition">
			<ViewResults v-if="competition.cProgress === Progress.DONE" />
			<span v-else>{{ t("ViewExecution.not_yet_published") }}</span>
		</template>
		<ProgressSpinner v-else />
	</div>
</template>

<script setup lang="ts">
import ViewResults from "@/components/pages/competition/results/ViewResults.vue"
import { useI18n } from "vue-i18n"
import { useRoute } from "vue-router"
import { useToast } from "primevue/usetoast"
import { getCompetitionDetails } from "@/backend/competition"
import { Progress } from "@/interfaces/competition"

const { t } = useI18n()
const route = useRoute()
const toast = useToast()

const { data: competition } = getCompetitionDetails(route, t, toast)
</script>

<style scoped></style>
