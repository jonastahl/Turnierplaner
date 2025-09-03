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
				@click="
					publish(
						competitions
							.filter((comp) => comp.cProgress === Progress.PUBLISHING)
							.map((comp) => comp.name),
					)
				"
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
								name: Routes.ManagePrepare,
								params: { tourId: route.params.tourId, compId: comp.name },
							})
						"
					/>
					<Button
						:disabled="comp.cProgress !== Progress.PUBLISHING"
						:severity="'secondary'"
						:label="t('ViewManage.publish')"
						@click="publish([comp.name])"
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
	<ConfirmDialog group="publish">
		<template #message="slotProps">
			<div class="flex flex-row gap-3">
				<i
					:class="slotProps.message.icon"
					class="text-5xl text-red-500 align-content-center"
				></i>
				<p style="white-space: pre-line">{{ slotProps.message.message }}</p>
			</div>
		</template>
	</ConfirmDialog>
</template>

<script setup lang="ts">
import {
	getCompetitionsList,
	usePublishCompetitions,
} from "@/backend/competition"
import { useRoute, useRouter } from "vue-router"
import { useI18n } from "vue-i18n"
import { inject, ref } from "vue"
import { useToast } from "primevue/usetoast"
import ViewPrepareSteps from "@/components/views/compManage/prepare/ViewPrepareSteps.vue"
import { Progress, progressOrder } from "@/interfaces/competition"
import { Routes } from "@/routes"
import { useConfirm } from "primevue/useconfirm"

const route = useRoute()
const router = useRouter()
const { t } = useI18n()
const toast = useToast()
const confirm = useConfirm()

const isLoggedIn = inject("loggedIn", ref(false))

const { data: competitions } = getCompetitionsList(route, isLoggedIn, t, toast)
const { mutate: publishCompetition } = usePublishCompetitions(route, t, toast)

function publish(competitions: string[]) {
	console.log("publish")
	confirm.require({
		header: t(
			competitions.length === 1
				? "ViewManage.publish"
				: "ViewManage.publishall",
		),
		message:
			t("ViewPrepare.publish_warning") +
			(competitions.length > 1
				? ""
				: "\n\n" + t("ViewPrepare.publish_consider_all")),
		icon: "pi pi-exclamation-triangle",
		acceptClass: "p-button-success",
		rejectLabel: t("general.cancel"),
		acceptLabel: t("ViewManage.publish"),
		accept: () => publishCompetition(competitions),
		group: "publish",
	})
}
</script>

<style scoped></style>
