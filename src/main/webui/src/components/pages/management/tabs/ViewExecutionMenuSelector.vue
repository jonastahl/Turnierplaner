<template>
	<TabMenu
		v-model:active-index="activeTab"
		:model="menuComps"
		@tab-change="tabChange"
	/>
</template>

<script setup lang="ts">
import { useRoute, useRouter } from "vue-router"
import { ref, watch } from "vue"
import { useI18n } from "vue-i18n"
import { TabMenuChangeEvent } from "primevue/tabmenu"
import { Routes } from "@/routes"

const { t } = useI18n()

const route = useRoute()
const router = useRouter()
const activeTab = ref<number>(0)

const menuComps = [
	{
		label: t("general.calendar"),
		icon: "pi pi-calendar-clock",
		route: Routes.ManageExecution,
	},
	{
		label: t("general.gameplans"),
		icon: "pi pi-sitemap",
		route: Routes.ManageExecutionPlans,
	},
]

function tabChange(event: TabMenuChangeEvent) {
	router.push({
		name: menuComps[event.index].route,
		params: {
			compId: route.params.compId,
		},
	})
}

watch(
	route,
	() => {
		activeTab.value = menuComps.findIndex(
			(menu) => menu.route === route.meta.executionsel,
		)
	},
	{ immediate: true },
)
</script>

<style scoped></style>
