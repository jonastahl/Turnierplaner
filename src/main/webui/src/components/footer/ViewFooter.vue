<template>
	<div class="bg-primary-900 p-6 flex justify-content-between">
		<div class="flex items-center sm:flex-row flex-column gap-2">
			<u class="text-white transition-colors cursor-pointer" @click="selected"
				>Impressum</u
			>
			<div class="flex gap-2">
				<a
					href="https://github.com/jonastahl/Turnierplaner"
					target="_blank"
					rel="noopener noreferrer"
					class="hover:text-primary transition-colors text-white"
				>
					<i class="pi pi-globe text-xl"></i>
				</a>
				<a
					href="https://github.com/jonastahl/Turnierplaner"
					target="_blank"
					rel="noopener noreferrer"
					class="hover:text-primary transition-colors text-white"
				>
					<i class="pi pi-github text-xl"></i>
				</a>
			</div>
		</div>
		<span>Turnierplaner v{{ version }}</span>
	</div>
</template>

<script setup lang="ts">
import { router } from "@/main"
import { version } from "../../../package.json"
import { Routes } from "@/routes"

function selected() {
	router.push({
		name: Routes.Impressum,
	})
}
</script>

<style scoped>
footer {
	height: 70px;
	display: flex;
	flex-direction: row;
	flex-wrap: nowrap;
	align-items: center;
	justify-content: space-between;
	position: sticky;
	left: 0;
	right: 0;
	top: 0;
	background-color: white;
}
</style>
