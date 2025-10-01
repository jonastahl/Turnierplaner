<template>
	<span
		v-if="match.sets && match.sets.length"
		:class="{
			'cursor-pointer hover:underline': editResult,
		}"
		@click="updatePoints"
	>
		<i class="pi pi-pen-to-square mr-1 ml-2"></i>
		<template v-for="(set, index) in match.sets" :key="set.index">
			{{ set.scoreA }}:{{ set.scoreB }}
			<template v-if="index < match.sets.length - 1"> / </template>
		</template> </span
	><span
		v-else-if="editResult"
		:class="{
			'cursor-pointer hover:underline': editResult,
		}"
		@click="updatePoints"
	>
		<i class="pi pi-pen-to-square mr-1 ml-2"></i> _:_ / _:_
	</span>
	<UpdatePointsDialog
		v-if="editResult && competition"
		ref="dialog"
		:number-sets="competition.numberSets"
		:comp-id="props.match.compName"
	/>
</template>

<script setup lang="ts">
import { computed, ref } from "vue"
import UpdatePointsDialog from "@/components/pages/competition/reporting/UpdatePointsDialog.vue"
import { getCompetitionDetailsCustom } from "@/backend/competition"
import { AnnotatedMatch } from "@/interfaces/match"
import { useRoute } from "vue-router"
import { useToast } from "primevue/usetoast"
import { useI18n } from "vue-i18n"

const props = defineProps<{
	match: AnnotatedMatch
	editResult?: boolean
}>()

const route = useRoute()
const toast = useToast()
const { t } = useI18n()

const dialog = ref<InstanceType<typeof UpdatePointsDialog>>()

const { data: competition } = getCompetitionDetailsCustom(
	route,
	t,
	computed(() => props.match.compName),
	toast,
)

function updatePoints() {
	if (props.editResult) {
		dialog.value?.showPopUp(props.match)
	}
}
</script>

<style scoped></style>
