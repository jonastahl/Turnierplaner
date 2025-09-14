<template>
	<TabMenu
		v-model:active-index="activeTab"
		:model="menuComps"
		@tab-change="tabChange"
	>
		<template #item="{ label, props }: { label: any; props: any }">
			<a v-bind="props.action" :class="props.class">
				<span :class="props.icon.class"></span>
				<span :class="props.label.class">{{ t(String(label)) }}</span>
			</a>
		</template>
	</TabMenu>
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
		label: "general.settings",
		icon: "pi pi-cog",
		route: Routes.ManageSettings,
	},
	{
		label: "ViewPrepare.preparation",
		icon: "pi pi-hammer",
		route: Routes.ManagePrepare,
	},
	{
		label: "ViewManage.execution",
		icon: "pi pi-wave-pulse",
		route: Routes.ManageExecution,
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
			(menu) => menu.route === route.meta.managesel,
		)
	},
	{ immediate: true },
)
</script>

<style scoped></style>
