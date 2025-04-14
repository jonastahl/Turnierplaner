<template>
	<div v-if="competitions">
		<div class="flex flex-row justify-content-end mb-5">
			<Button
				:disabled="
					competitions.findIndex(
						(comp) => comp.cProgress === Progress.PUBLISHING,
					) === -1
				"
				:severity="'success'"
				:label="t('ViewManage.publishall')"
			/>
		</div>
		<div
			v-for="comp in competitions"
			:key="comp.name"
			class="flex flex-column mb-5"
		>
			<div class="flex flex-row justify-content-between">
				<strong class="text-xl">
					{{ comp.name }}
				</strong>
				<div class="flex flex-row gap-3">
					<Button
						:disabled="progressOrder(comp.cProgress) > 3"
						:label="t('ViewManage.prepare')"
						@click="
							router.push({
								name: 'Manage prepare',
								params: { tourId: route.params.tourId, compId: comp.name },
							})
						"
					/>
					<Button
						:disabled="comp.cProgress !== Progress.PUBLISHING"
						:severity="'secondary'"
						:label="t('ViewManage.publish')"
					/>
				</div>
			</div>
			<ViewPrepareSteps
				class="mt-4"
				:active-step="progressOrder(comp.cProgress)"
				overview
			>
			</ViewPrepareSteps>
		</div>
	</div>
</template>

<script setup lang="ts">
import { getCompetitionsList } from "@/backend/competition"
import { useRoute, useRouter } from "vue-router"
import { useI18n } from "vue-i18n"
import { inject, ref } from "vue"
import { useToast } from "primevue/usetoast"
import ViewPrepareSteps from "@/components/views/compManage/prepare/ViewPrepareSteps.vue"
import { Progress, progressOrder } from "@/interfaces/competition"

const route = useRoute()
const router = useRouter()
const { t } = useI18n()
const toast = useToast()

const isLoggedIn = inject("loggedIn", ref(false))

const { data: competitions } = getCompetitionsList(route, isLoggedIn, t, toast)
</script>

<style scoped></style>
