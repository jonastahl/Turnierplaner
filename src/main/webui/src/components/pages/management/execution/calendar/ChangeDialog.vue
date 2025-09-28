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
				v-for="match in props.changeSet"
				:key="match.id"
				class="border-round"
			>
				<UpdatedMatch :match="match" />
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
						reschedule(props.changeSet)
					}
				"
			/>
		</div>
	</Dialog>
</template>

<script setup lang="ts">
import { useI18n } from "vue-i18n"
import { MatchCalEvent } from "@/components/pages/management/prepare/scheduleMatches/ScheduleMatchesHelper"
import UpdatedMatch from "@/components/pages/management/execution/calendar/UpdatedMatch.vue"
import MatchEventColor from "@/components/items/MatchEventColor.vue"
import { useToast } from "primevue/usetoast"
import { useRescheduleMatches } from "@/backend/match"
import { useRoute } from "vue-router"

const visible = defineModel<boolean>()
const props = defineProps<{ changeSet: MatchCalEvent[] }>()

const route = useRoute()
const { t } = useI18n()
const toast = useToast()

const emit = defineEmits<{
	publishing: []
}>()

const { mutate: reschedule } = useRescheduleMatches(route, t, toast)
</script>

<style scoped></style>
