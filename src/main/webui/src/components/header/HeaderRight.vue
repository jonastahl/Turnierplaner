<template>
	<div v-if="windowWidth < 600">
		<span
			class="material-symbols-outlined cursor-pointer bigger pr-2"
			@click="sidebar = true"
		>
			menu
		</span>
		<Sidebar v-model:visible="sidebar" position="right">
			<template #header>
				<div class="flex flex-row align-items-center gap-2">
					<span
						class="material-symbols-outlined cursor-pointer icon"
						style="font-size: 3rem"
						@click="toHome"
					>
						sports_tennis
					</span>
					<h1 class="m-0 cursor-pointer title" @click="toHome">
						{{ config?.title ? t(config.title) : t("title") }}
					</h1>
				</div>
			</template>
			<SidebarContent />
		</Sidebar>
	</div>
	<div v-else class="pr-3 flex flex-row align-items-center gap-2 lg:gap-3">
		<HeaderRightContent />
	</div>
</template>

<script lang="ts" setup>
import HeaderRightContent from "./HeaderRightContent.vue"
import SidebarContent from "./SidebarContent.vue"
import { inject, ref } from "vue"
import { router } from "@/main"
import { getConfig } from "@/backend/config"
import { useI18n } from "vue-i18n"
import { Routes } from "@/routes"

const { t } = useI18n()

const sidebar = ref(false)

const isLoggedIn = inject("loggedIn", ref(false))
const { data: config } = getConfig(isLoggedIn)

let windowWidth = ref(window.innerWidth)
window.addEventListener("resize", () => {
	windowWidth.value = window.innerWidth
})

function toHome() {
	router.push({ name: Routes.Tournaments })
}
</script>

<script lang="ts"></script>

<style>
.bigger {
	font-size: 2.5rem !important;
	font-weight: bold !important;
}

h1 {
	font-weight: 900;
	font-size: 1.2rem;
	color: #044154;
}
</style>
