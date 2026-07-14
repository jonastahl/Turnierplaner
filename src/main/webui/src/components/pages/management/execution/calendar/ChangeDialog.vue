<template>
	<Dialog
		v-model:visible="visible"
		:header="t('ViewManage.update_schedule_header')"
		modal
		style="min-width: 50vw"
		class="m-3"
	>
		<div class="flex flex-row flex-wrap gap-2">
			<MatchEventColor
				v-for="[from, to] in props.changeSet"
				:key="from.id"
				class="border-round"
			>
				<UpdatedMatch :from="from" :to="to" />
			</MatchEventColor>
		</div>
		<div class="flex justify-content-end">
			<Button
				class="mt-3"
				:size="'small'"
				:severity="'success'"
				:label="t('general.saveandpublish')"
				@click="
					() => {
						emit('publishing')
						reschedule(props.changeSet.map(([, to]) => to))
					}
				"
			/>
		</div>
	</Dialog>
</template>

<script setup lang="ts">
import { useI18n } from "vue-i18n"
import UpdatedMatch from "@/components/pages/management/execution/calendar/UpdatedMatch.vue"
import MatchEventColor from "@/components/items/MatchEventColor.vue"
import { useToast } from "primevue/usetoast"
import { useRescheduleMatches } from "@/backend/match"
import { useRoute } from "vue-router"
import { AnnotatedMatch } from "@/interfaces/match"

const visible = defineModel<boolean>()
const props = defineProps<{ changeSet: [AnnotatedMatch, AnnotatedMatch][] }>()

const route = useRoute()
const { t } = useI18n()
const toast = useToast()

const emit = defineEmits<{
	publishing: []
}>()

const { mutate: reschedule } = useRescheduleMatches(route, t, toast)
</script>

<style scoped></style>
