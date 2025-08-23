<template>
	<span class="cursor-pointer hover:underline" @click="click">
		{{ props.tournament }}
	</span>
</template>

<script setup lang="ts">
import { router } from "@/main"
import { Routes } from "@/routes"

const props = defineProps<{
	tournament?: string
}>()

function click() {
	if (props.tournament)
		router.push({
			name: Routes.Competitions,
			params: {
				tourId: props.tournament,
			},
		})
}
</script>

<style scoped></style>
