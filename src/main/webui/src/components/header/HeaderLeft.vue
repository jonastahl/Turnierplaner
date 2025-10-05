<template>
	<div
		id="header"
		class="flex flex-row pl-2 lg:pl-3 align-items-center gap-2 lg:gap-3"
	>
		<span class="material-symbols-outlined cursor-pointer icon" @click="toHome">
			sports_tennis
		</span>
		<div class="flex flex-column lg:flex-row align-items-center lg:gap-3">
			<h1 id="colorHeadLine" class="m-0 cursor-pointer title" @click="toHome">
				{{
					config?.title && config.title != "title" ? config.title : t("title")
				}}
			</h1>
			<div class="flex flex-column xl:flex-row align-items-center xl:gap-3">
				<h2
					v-if="route.params.tourId"
					class="m-0 cursor-pointer"
					@click="toTournament"
				>
					{{ route.params.tourId }}
				</h2>
				<h2 v-if="route.name === 'Settings'" class="cursor-pointer">
					{{ t("general.settings") }}
				</h2>
				<h3
					v-if="route.params.compId"
					class="m-0 cursor-pointer"
					@click="toCompetition"
				>
					{{ route.params.compId }}
				</h3>
			</div>
		</div>
	</div>
</template>

<script lang="ts" setup>
import { router } from "@/main"
import { useI18n } from "vue-i18n"
import { useRoute } from "vue-router"
import { getConfig } from "@/backend/config"
import { inject, ref } from "vue"
import { Routes } from "@/routes"

const route = useRoute()
const { t } = useI18n()

const isLoggedIn = inject("loggedIn", ref(false))
const { data: config } = getConfig(isLoggedIn)

function toHome() {
	router.push({ name: Routes.Tournaments })
}

function toTournament() {
	router.push({
		name: Routes.Competitions,
	})
}

function toCompetition() {
	router.push({
		name: Routes.Competition,
	})
}
</script>

<style scoped>
#colorHeadLine {
	color: #044154;
}

h1 {
	font-weight: 900;
	font-size: 3rem;
}

h1,
h2,
h3 {
	line-height: 1;
	padding: 0;
}

.icon {
	font-size: 3rem;
}

@media only screen and (max-width: 991px) {
	h1,
	h2,
	h3 {
		font-size: 1em;
	}
}

span:hover,
h1:hover,
h2:hover,
h3:hover {
	filter: drop-shadow(0 0 6px #808080);
}
</style>
