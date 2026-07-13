<template>
	<router-link
		v-if="routeTarget"
		class="cursor-pointer hover:underline no-underline text-color"
		:to="routeTarget"
	>
		<slot></slot>
	</router-link>
	<span v-else>
		<slot></slot>
	</span>
</template>

<script setup lang="ts">
import { RouteLocationRaw } from "vue-router"

defineProps<{
	routeTarget: RouteLocationRaw | null
}>()
</script>

<style scoped></style>
