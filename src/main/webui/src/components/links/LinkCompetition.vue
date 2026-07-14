<template>
	<LinkStub :route-target="routeTarget">
		{{ props.competition }}
	</LinkStub>
</template>

<script setup lang="ts">
import { Routes } from "@/routes"
import { computed } from "vue"
import LinkStub from "@/components/links/LinkStub.vue"

const props = defineProps<{
	tournament?: string
	competition?: string
}>()

const routeTarget = computed(() => {
	if (props.tournament && props.competition) {
		return {
			name: Routes.Competition,
			params: {
				tourId: props.tournament,
				compId: props.competition,
			},
		}
	}
	return null
})
</script>

<style scoped></style>
