<template>
	<TabMenu :active-index="activeTab" :model="menuComps" @tab-change="tabChange">
		<template #item="{ item, props }">
			<div v-bind="props.action" class="cursor-pointer">
				<span v-if="!item.overview" v-bind="props.label">{{ item.name }}</span>
				<strong v-else v-bind="props.label">
					{{ t("ViewPrepare.overview") }}
				</strong>
			</div>
		</template>
	</TabMenu>
</template>

<script setup lang="ts">
import { useRoute, useRouter } from "vue-router"
import { computed, inject, ref, watch } from "vue"
import { useI18n } from "vue-i18n"
import { useToast } from "primevue/usetoast"
import { TabMenuChangeEvent } from "primevue/tabmenu"
import { getCompetitionsList } from "@/backend/competition"
import { Routes } from "@/routes"

const { t } = useI18n()

const route = useRoute()
const router = useRouter()
const toast = useToast()

const isLoggedIn = inject("loggedIn", ref(false))
const { data: competitions } = getCompetitionsList(route, isLoggedIn, t, toast)
const activeTab = ref<number>(0)

const menuComps = computed(() => {
	if (!competitions.value) return []

	return [
		...(<boolean>route.meta.overview
			? [
					{
						overview: true,
					},
				]
			: []),
		...competitions.value,
	]
})

function tabChange(event: TabMenuChangeEvent) {
	if (!competitions.value) return

	activeTab.value = event.index
	let comp = null
	if (!route.meta.overview || activeTab.value > 0) {
		comp =
			competitions.value[
				activeTab.value - (<boolean>route.meta.overview ? 1 : 0)
			]
	}
	router.push({
		name: <Routes>route.meta.mStep,
		params: {
			compId: comp?.name,
		},
	})
}

watch(
	[route, competitions],
	() => {
		if (!competitions.value) return

		if (route.params.compId) {
			activeTab.value =
				competitions.value.findIndex(
					(comp) => comp.name === route.params.compId,
				) + (route.meta.overview ? 1 : 0)
		} else {
			activeTab.value = 0
			if (!route.meta.overview) {
				router.replace({
					params: {
						compId: competitions.value[0].name,
					},
				})
			}
		}
	},
	{ immediate: true },
)
</script>

<style scoped></style>
