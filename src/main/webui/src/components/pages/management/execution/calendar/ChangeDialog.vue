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
						toast.add({
							severity: 'info',
							summary: 'Coming soon',
							life: 3000,
						})
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

const visible = defineModel<boolean>()
const props = defineProps<{ changeSet: MatchCalEvent[] }>()

const { t } = useI18n()
const toast = useToast()
</script>

<style scoped></style>
