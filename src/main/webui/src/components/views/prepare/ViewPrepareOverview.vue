<template>
	<div>
		<div
			v-for="comp in competitions"
			:key="comp.name"
			class="flex flex-column mb-5"
		>
			<div class="flex flex-row justify-content-between">
				<strong class="text-xl col-10">
					{{ comp.name }}
				</strong>
				<Button
					class="col-2"
					@click="
						router.push({
							name: 'Prepare competition',
							params: { tourId: route.params.tourId, compId: comp.name },
						})
					"
				>
					{{ t("ViewPrepare.prepare") }}
				</Button>
			</div>
			<ViewSteps
				class="mt-4"
				:active-step="progressOrder(comp.cProgress) - 2"
				overview
			>
			</ViewSteps>
		</div>
	</div>
</template>

<script setup lang="ts">
import { getCompetitionsList } from "@/backend/competition"
import { useRoute, useRouter } from "vue-router"
import { useI18n } from "vue-i18n"
import { inject, ref } from "vue"
import { useToast } from "primevue/usetoast"
import ViewSteps from "@/components/views/prepare/ViewSteps.vue"
import { progressOrder } from "@/interfaces/competition"

const route = useRoute()
const router = useRouter()
const { t } = useI18n()
const toast = useToast()

const isLoggedIn = inject("loggedIn", ref(false))

const { data: competitions } = getCompetitionsList(route, isLoggedIn, t, toast)
</script>

<style scoped></style>
