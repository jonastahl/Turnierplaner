<template>
	<template v-if="competition">
		<ViewGroupSystem v-if="competition.tourType === CompType.GROUPS" />
		<ViewKnockoutSystem
			v-else-if="competition.tourType === CompType.KNOCKOUT"
		/>
		<p v-else>Invalid type</p>
	</template>
</template>

<script lang="ts" setup>
import ViewGroupSystem from "@/components/pages/competition/results/group/ViewGroupSystem.vue"
import ViewKnockoutSystem from "@/components/pages/competition/results/knockout/ViewKnockoutSystem.vue"
import { CompType } from "@/interfaces/competition"
import { getCompetitionDetails } from "@/backend/competition"
import { useI18n } from "vue-i18n"
import { useToast } from "primevue/usetoast"
import { useRoute } from "vue-router"

const { t } = useI18n()
const route = useRoute()
const toast = useToast()

const { data: competition } = getCompetitionDetails(route, t, toast)
</script>

<style scoped></style>
