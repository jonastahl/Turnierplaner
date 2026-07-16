<template>
	<LinkStub v-if="props.player" :route-target="routeTarget" :white>
		<template v-if="inverted">
			{{ props.player.lastName }}, {{ firstShort }}
		</template>
		<template v-else>{{ firstShort }} {{ props.player.lastName }}</template>
	</LinkStub>
</template>

<script setup lang="ts">
import { Player } from "@/interfaces/player"
import { computed } from "vue"
import { Routes } from "@/routes"
import LinkStub from "@/components/links/LinkStub.vue"

const props = withDefaults(
	defineProps<{
		player?: Player | null
		inverted?: boolean
		short?: boolean
		white?: boolean
	}>(),
	{
		player: null,
		inverted: false,
		short: false,
		white: false,
	},
)

const firstShort = computed(() => {
	if (!props.player) return ""
	if (props.short) return props.player.firstName.charAt(0) + "."
	else return props.player.firstName
})

const routeTarget = computed(() => {
	if (props.player) {
		return {
			name: Routes.PlayerOverview,
			params: {
				playerId: props.player.id,
			},
		}
	}
	return null
})
</script>

<style scoped>
span {
	white-space: nowrap;
}
</style>
