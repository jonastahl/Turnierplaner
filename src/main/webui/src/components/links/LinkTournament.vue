<template>
	<LinkStub :route-target="routeTarget">
		{{ props.tournament }}
	</LinkStub>
</template>

<script setup lang="ts">
import { Routes } from "@/routes"
import { computed } from "vue"

const props = defineProps<{
	tournament?: string
}>()

const routeTarget = computed(() => {
	if (props.tournament) {
		return {
			name: Routes.Competitions,
			params: {
				tourId: props.tournament,
			},
		}
	}
	return null
})
</script>

<style scoped></style>
