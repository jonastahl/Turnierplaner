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

const { t } = useI18n()

const route = useRoute()
const router = useRouter()
const activeTab = ref<number>(0)

const menuComps = [
	{ label: t("general.settings"), icon: "pi pi-cog", route: "Manage settings" },
	{
		label: t("ViewPrepare.preparation"),
		icon: "pi pi-hammer",
		route: "Manage prepare",
	},
	{
		label: t("ViewManage.execution"),
		icon: "pi pi-wave-pulse",
		route: "Manage execution",
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
			(menu) => menu.route === route.meta.mStep,
		)
	},
	{ immediate: true },
)
</script>

<style scoped></style>
