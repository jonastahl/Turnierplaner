<template>
	<router-link
		v-if="routeTarget"
		class="cursor-pointer hover:underline no-underline"
		:class="white ? 'text-white' : 'text-color'"
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

withDefaults(
	defineProps<{
		routeTarget: RouteLocationRaw | null
		white?: boolean
	}>(),
	{
		white: false,
	},
)
</script>

<style scoped></style>
