<template>
	<div class="flex flex-column gap-1">
		<Transition v-if="competition">
			<Groups
				v-if="competition.tourType === CompType.GROUPS"
				ref="groupsRef"
				v-model:is-updating="isUpdating"
			/>
			<Knockout v-else ref="knockoutRef" v-model:is-updating="isUpdating" />
		</Transition>
		<NavigationButtons
			:loading="isUpdating"
			reload
			@reset="resetDialog = true"
			@reload="reload"
			@save="save"
			@complete="complete"
		/>
	</div>
	<DialogResetProgress v-model="resetDialog" />
</template>

<script setup lang="ts">
import { useRoute } from "vue-router"
import { useI18n } from "vue-i18n"
import { useToast } from "primevue/usetoast"
import { CompType } from "@/interfaces/competition"
import Groups from "@/components/pages/management/prepare/assignMatches/AssignMatchesGroups.vue"
import AssignMatchesGroups from "@/components/pages/management/prepare/assignMatches/AssignMatchesGroups.vue"
import Knockout from "@/components/pages/management/prepare/assignMatches/AssignMatchesKnockout.vue"
import AssignMatchesKnockout from "@/components/pages/management/prepare/assignMatches/AssignMatchesKnockout.vue"
import { ref } from "vue"
import { getCompetitionDetails } from "@/backend/competition"
import NavigationButtons from "@/components/pages/management/prepare/components/NavigationButtons.vue"
import DialogResetProgress from "@/components/pages/management/prepare/DialogResetProgress.vue"

const route = useRoute()
const toast = useToast()
const { t } = useI18n()

const isUpdating = ref(false)
const knockoutRef = ref<InstanceType<typeof AssignMatchesKnockout> | null>(null)
const groupsRef = ref<InstanceType<typeof AssignMatchesGroups> | null>(null)

const { data: competition } = getCompetitionDetails(route, t, toast)

const resetDialog = ref(false)

function save() {
	if (competition.value?.tourType === CompType.GROUPS) {
		if (groupsRef.value === null) return
		groupsRef.value.save()
	} else {
		if (knockoutRef.value === null) return
		knockoutRef.value.save()
	}
}

function complete() {
	if (competition.value?.tourType === CompType.GROUPS) {
		if (groupsRef.value === null) return
		groupsRef.value.save(true)
	} else {
		if (knockoutRef.value === null) return
		knockoutRef.value.save(true)
	}
}

function reload() {
	if (competition.value?.tourType === CompType.GROUPS) {
		if (groupsRef.value === null) return
		groupsRef.value.reload()
	} else {
		if (knockoutRef.value === null) return
		knockoutRef.value.reload()
	}
}
</script>

<style scoped>
.v-enter-active,
.v-leave-active {
	transition: opacity 0.2s ease;
	width: 100%;
}

.v-enter-active {
	transition-delay: 0.2s;
}

.v-enter-from,
.v-leave-to {
	opacity: 0;
}
</style>
