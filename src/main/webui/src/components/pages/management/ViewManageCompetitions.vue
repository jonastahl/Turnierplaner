<template>
	<div class="w-full p-2">
		<Card style="margin-top: -10px !important">
			<template #header>
				<ViewMenuSelector />
				<ViewExecutionMenuSelector v-if="route.meta.executionMenu" />
				<ViewCompSelector v-if="route.meta.compSelector" />
			</template>
			<template #content>
				<router-view />
			</template>
		</Card>
	</div>
</template>

<script setup lang="ts">
import ViewCompSelector from "@/components/pages/management/tabs/ViewCompSelector.vue"
import ViewMenuSelector from "@/components/pages/management/tabs/ViewMenuSelector.vue"
import ViewExecutionMenuSelector from "@/components/pages/management/tabs/ViewExecutionMenuSelector.vue"
import { inject, ref, watch } from "vue"
import { auth } from "@/security/AuthService"
import { useRoute, useRouter } from "vue-router"
import { Routes } from "@/routes"
import { getCompetitionsList } from "@/backend/competition"
import { useI18n } from "vue-i18n"
import { useToast } from "primevue/usetoast"
import { Progress } from "@/interfaces/competition"

const route = useRoute()
const router = useRouter()
const { t } = useI18n()
const toast = useToast()
const isLoggedIn = inject("loggedIn", ref(false))
const { data: competitions } = getCompetitionsList(route, isLoggedIn, t, toast)
if (!isLoggedIn.value) {
	auth.login()
}

watch(
	route,
	() => {
		if (route.name === Routes.ManageCompetition) {
			router.push({
				name: competitions.value?.every(
					(comp) => comp.cProgress == Progress.DONE,
				)
					? Routes.ManageExecution
					: Routes.ManagePrepare,
			})
		}
	},
	{ immediate: true },
)
</script>

<style scoped></style>
