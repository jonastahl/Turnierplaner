<template>
	<span
		v-if="props.player"
		class="cursor-pointer hover:underline"
		@click="click"
	>
		<template v-if="inverted">
			{{ props.player.lastName }}, {{ firstShort }}
		</template>
		<template v-else>{{ firstShort }} {{ props.player.lastName }}</template>
	</span>
</template>

<script setup lang="ts">
import { Player } from "@/interfaces/player"
import { router } from "@/main"
import { computed } from "vue"
import { Routes } from "@/routes"

const props = withDefaults(
	defineProps<{
		player?: Player | null
		inverted?: boolean
		short?: boolean
	}>(),
	{
		player: null,
		inverted: false,
		short: false,
	},
)

const firstShort = computed(() => {
	if (!props.player) return ""
	if (props.short) return props.player.firstName.charAt(0) + "."
	else return props.player.firstName
})

function click() {
	if (props.player)
		router.push({
			name: Routes.PlayerOverview,
			params: {
				playerId: props.player.id,
			},
		})
}
</script>

<style scoped></style>
